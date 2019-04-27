package SE;
//import javax.servlet.*;
//import javax.servlet.html.*;
import java.io.*;
import static java.util.Comparator.comparing;
import lib.StopStem;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.rocksdb.RocksDB;
import org.rocksdb.Options;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import java.util.*;
import org.apache.commons.lang3.*;


public class Searcher {
    private final String fPath = "/home/sam/search-engine/searchengine/forwardIndex";
    private final String iPath = "/home/sam/search-engine/searchengine/invertedIndex";
    private PageContent pageContent = new PageContent("/home/sam/search-engine/searchengine/pageInfo");
    StopStem stopStem = new StopStem("stopwords.txt");
    private RocksDB forwardIndex;
    private RocksDB invertedIndex;
    private Options options;

    public Searcher() throws RocksDBException{
        RocksDB.loadLibrary();
        this.options = new Options();
        forwardIndex = RocksDB.open(options, fPath);
        invertedIndex = RocksDB.open(options, iPath);


    }

    public List<Map.Entry<Integer, Double>> search(String s) throws RocksDBException{
        HashMap<Integer, Double> pageIdToScore = new HashMap<>();
        ArrayList<String> terms = getTerms(s);

        for(String term : terms){
            if(term.charAt(0)!='"')
                singleWordSearch(term, pageIdToScore);
            else
                quotesSearch(term, pageIdToScore);
        }
        ArrayList<Map.Entry<Integer, Double>> entries = new ArrayList<Map.Entry<Integer, Double>>(pageIdToScore.entrySet());
        Collections.sort(entries, new MyMapComparator());


        int end = 50;
        if(entries.size()==0){
            return null;
        }
        if(entries.size()<50){
            end = entries.size();
        }
        return entries.subList(0, end); //
    }

    public void singleWordSearch(String term, HashMap<Integer, Double> scores){
        //CHANGE YA DUMMY
        try {
            if (stopStem.isStopWord(term)) {
                System.out.println("Skipped stopword: "+term);
                return;
            }
            term = stopStem.stem(term);
            if (term.length() == 0) {
                System.out.println("Skipped term that got stemmed to empty");
                return;
            }

            HashMap<Integer, Double> invertedEntry = (HashMap<Integer, Double>) SerializationUtils.deserialize(invertedIndex.get(SerializationUtils.serialize(term.hashCode())));
            System.out.println("Size of "+term+"'s invertedentry: "+invertedEntry.size());
            for (Map.Entry<Integer, Double> page : invertedEntry.entrySet()) {
                int id = page.getKey();
                double tfidf = page.getValue();
                System.out.println(term+"'s tfidf: "+tfidf);
                if (scores.containsKey(id)) {
                    double newScore = scores.get(id) + tfidf;
                    scores.put(id, newScore);
                } else {
                    System.out.println("adding score for: "+id);
                    scores.put(id, tfidf);
                }
            }
        }catch (RocksDBException e){
            System.out.println(e.toString());
        }
    }

    public void quotesSearch(String term, HashMap<Integer, Double> scores) {
        try {
            int df = 0;
            int firstRealWord = -1;
            String[] terms = term.replace("\"", "").split(" ");
            ArrayList<String> stemmedTerms = new ArrayList<>();
            for (int i = 0; i < terms.length; i++) {
                if (!stopStem.isStopWord(terms[i])) {
                    String stemmed = stopStem.stem(terms[i]);
                    if (stemmed.length() > 0) {
                        stemmedTerms.add(stemmed);
                        if (firstRealWord == -1)
                            firstRealWord = i;
                    }
                    terms[i] = stemmed;
                } else {
                    terms[i] = "";
                }
            }

            Set<Integer> pageCandidates = getPageCandidates(stemmedTerms);
            System.out.println("For term: "+term + " "+pageCandidates.size() +  " page candidates were found"+" search will begin at "+firstRealWord+"'th term");
            HashMap<Integer, Integer> pageIdToTf = new HashMap<>();
            for (Integer pageId : pageCandidates) {
                ArrayList<ArrayList<Integer>> termIndexes = new ArrayList<ArrayList<Integer>>();
                HashMap<Integer, ArrayList<Integer>> pageTerms = (HashMap<Integer, ArrayList<Integer>>) SerializationUtils.deserialize(forwardIndex.get(SerializationUtils.serialize(pageId)));
                int[] minIndex = new int[terms.length];
                for (int i = 0; i < terms.length; i++) {
                    termIndexes.add(terms[i].length() != 0 ? pageTerms.get(terms[i].hashCode()) : new ArrayList<Integer>());
                    System.out.println("TermIndexes["+i+"] = "+termIndexes.get(i));
                }

                for (int i = firstRealWord; i < termIndexes.get(firstRealWord).size(); i++) {
                    System.out.println("Starting from i = "+i +" going to "+termIndexes.get(firstRealWord).size());
                    int startingIndex = termIndexes.get(firstRealWord).get(i);
                    //
                    boolean candidate = true;
                    for (int j = firstRealWord + 1; j < termIndexes.size(); j++) {
                        boolean found = false;
                        ArrayList<Integer> indexes = termIndexes.get(j);
                        if (indexes.size() == 0) { //word was stop word or stemmed out of existance
                            startingIndex += 1;
                            continue;
                        }
                        for (int k = minIndex[j]; k < indexes.size(); k++) {
                            System.out.println("Starting from "+j+"'s min index of "+minIndex[j]+" a word index of "+startingIndex+" will be searched for in:");
                            System.out.println(indexes);
                            if (indexes.get(k) <= startingIndex) {
                                minIndex[j]++;
                            } else if (indexes.get(k) == startingIndex + 1) {
                                minIndex[j]++;
                                found = true;
                                break;
                            } else {
                                break;
                            }
                        }
                        if (!found) {
                            candidate = false;
                            break;
                        }
                    }
                    if (candidate) {
                        df++;
                        if (pageIdToTf.containsKey(pageId))
                            pageIdToTf.put(pageId, pageIdToTf.get(pageId) + 1);
                        else
                            pageIdToTf.put(pageId, 1);
                    }
                }

            }
            for (Map.Entry<Integer, Integer> page : pageIdToTf.entrySet()) {
                //Load tf Max
                int pageId = page.getKey();
                int tf = page.getValue();
                Page pageInfo = pageContent.getPageContent(pageId);
                double tfmax = (double)pageInfo.tfmax;

                double tfidf = ((double)tf)*Math.log(30.0/((double)pageIdToTf.size()))/Math.log(2)/tfmax; //Change 30 to # of docs indexed

                if (scores.containsKey(pageId)) {
                    double newScore = scores.get(pageId) + tfidf;
                    scores.put(pageId, newScore);
                } else {
                    scores.put(pageId, tfidf);
                }
            }
        }catch (RocksDBException e){
                System.out.println(e.toString());
            }



    }

    public Set<Integer> getPageCandidates(ArrayList<String> terms) {
        try {
            Set<Integer> pageCandidates = new HashSet<>();
            HashMap<Integer, Integer> firstWord = (HashMap<Integer, Integer>) SerializationUtils.deserialize(invertedIndex.get(SerializationUtils.serialize(terms.get(0).hashCode())));
            for (Integer pageId : firstWord.keySet())
                pageCandidates.add(pageId);

            for (int i = 1; i < terms.size(); i++) {
                HashMap<Integer, Integer> invertedEntry = (HashMap<Integer, Integer>) SerializationUtils.deserialize(invertedIndex.get(SerializationUtils.serialize(terms.get(i).hashCode())));
                Set<Integer> newCandidates = new HashSet<>();
                for (Integer pageId : invertedEntry.keySet()) {
                    if (pageCandidates.contains(pageId))
                        newCandidates.add(pageId);
                }
                pageCandidates = newCandidates;
            }

            return pageCandidates;
        }catch (RocksDBException e){
            System.out.println(e.toString());
            return null;
        }

    }


    public ArrayList<String> getTerms(String s){
        ArrayList<String> terms = new ArrayList<>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(s);
        while (m.find())
            terms.add(m.group(1));
        return terms;
    }

    public static void main(String[] args) throws RocksDBException{
        Searcher s = new Searcher();
        System.out.println(s.search("\"Hong Kong\""));
        System.out.println(s.search("Hong Kong"));

    }

    private static class MyMapComparator implements Comparator<Map.Entry<Integer, Double>>
    {
        @Override
        public int compare(Map.Entry<Integer, Double> a, Map.Entry<Integer, Double> b) {
            return b.getValue().compareTo(a.getValue());
        }
    }

}

