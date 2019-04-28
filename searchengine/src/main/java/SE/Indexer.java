package SE;
import java.util.Vector;
import lib.StopStem;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.htmlparser.beans.LinkBean;
import org.htmlparser.beans.StringBean;
import org.htmlparser.util.ParserException;

import org.rocksdb.RocksDB;
import org.rocksdb.Options;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import java.util.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;

import org.apache.commons.lang3.*;


public class Indexer {
    // define paths to DBs
    private final String workingDirectory = System.getProperty("user.dir");
    private final String uPath = workingDirectory + "/urlToPageID";
    private final String uInversePath = workingDirectory + "/pageIDToUrl";
    private final String wPath = workingDirectory + "/wordIDToWord";
    
    private final String pPath = workingDirectory + "/pageInfo";
    private final String iPath = workingDirectory + "/invertedIndex";
    private final String fPath = workingDirectory + "/forwardIndex";

    //private RocksDB urlToPageID;
    private MappingIndex urlToPageID;
    private static RocksDB wordIDToWord;
    //private RocksDB forwardIndex;
    private PageContent pageInfo;
    private RocksDB invertedIndex;
    private RocksDB forwardIndex;


    private Options options;

    //private int pageIDs = 0;

    public Indexer() throws RocksDBException {
        this.options = new Options();
        this.options.setCreateIfMissing(true);
       // urlToPageID = RocksDB.open(options, uPath);
        urlToPageID = new MappingIndex(uPath, uInversePath);
        //wordToWordID = RocksDB.open(options, wPath);
        //forwardIndex = RocksDB.open(options, fPath);
        pageInfo = new PageContent(pPath);
        invertedIndex = RocksDB.open(options, iPath);
        forwardIndex = RocksDB.open(options, fPath);
        wordIDToWord = RocksDB.open(options, wPath);

    }

    public void finalise(){
        urlToPageID.finalise();
        invertedIndex.close();
        forwardIndex.close();
        wordIDToWord.close();
        pageInfo.finalise();
    }
    
    public PageContent getPageContent(){
        return pageInfo;
    }

    public MappingIndex getMappingIndex(){
        return urlToPageID;
    }

    public String getWordfromID(Integer i) throws RocksDBException {
        byte [] val = wordIDToWord.get(SerializationUtils.serialize(i));
        if(val == null) System.out.println("null");
        String s = new String(val);
        return s;
    }

    public static Boolean validURL(String url){
        String s = getBody(url);
        if(s != null && !s.isEmpty()) return true;
        return false;
    }

    public void reweight(int N){ //change weights from tf to tfxidf, N=# of documents
        try {
            RocksIterator iter = invertedIndex.newIterator();
            iter.seekToFirst();
            while (iter.isValid()) {
                int wordID =  (int)SerializationUtils.deserialize(iter.key());
                HashMap<Integer, Double> pageIdToTf = (HashMap<Integer, Double>)SerializationUtils.deserialize(iter.value());
                int df = pageIdToTf.size();
                for(Map.Entry<Integer, Double> pair : pageIdToTf.entrySet()) {
                    Page currentPage = pageInfo.getPageContent(pair.getKey());
                    int tfmax = currentPage.tfmax;
                    double tf = pair.getValue();
                    double idf = Math.log(((double)N)/((double)df))/Math.log(2);
                    double tfidf = tf*idf/((double)tfmax);
                    pageIdToTf.put(pair.getKey(), tfidf);
                }
                invertedIndex.put(iter.key(), SerializationUtils.serialize(pageIdToTf));
                iter.next();
            }

            //invertedIndex.close();
        } catch (RocksDBException rdbe) {
            rdbe.printStackTrace(System.err);
        }
    }

    public HashMap<Integer, ArrayList<Integer>> extractWordIDs(String url) throws ParserException
    {
        StopStem stopStem = new StopStem("stopwords.txt");
        HashMap<Integer, ArrayList<Integer>> words = new HashMap<>();
        StringBean bean = new StringBean();
        bean.setURL(url);
        bean.setLinks(false);
        int index = 0;
        String contents = bean.getStrings();
        StringTokenizer st = new StringTokenizer(contents);
        while (st.hasMoreTokens()) {
            String nextWord = st.nextToken();
            if(stopStem.isStopWord(nextWord)){
                //System.out.println("Stop word skipped: "+nextWord);
                continue;
            }
            else{
                nextWord = stopStem.stem(nextWord);
                if(nextWord.length()==0){
                    continue;
                }

                try{
                    if(wordIDToWord.get(SerializationUtils.serialize(nextWord.hashCode()))==null){ //Check for collision here?
                        wordIDToWord.put(SerializationUtils.serialize(nextWord.hashCode()), nextWord.getBytes());
                    }
                }catch (RocksDBException e){
                    System.out.println(e);
                }

                //System.out.println("Stemmed word added: "+ nextWord);
            }

            Integer next = nextWord.hashCode();
            ArrayList<Integer> indexList = new ArrayList<>();
            if(words.containsKey(next)){
                indexList = words.get(next);
            }

            indexList.add(index++);
            words.put(next, indexList);
        }

        return words;

    }


    public static ArrayList<String> extractLinks(String url) throws ParserException

    {
    
            ArrayList<String> result = new ArrayList<String>();
            LinkBean bean = new LinkBean();
            bean.setURL(url);
            URL[] urls = bean.getLinks();
            for (URL s : urls) {
                String curr = s.toString();
                while(curr.charAt(curr.length()-1)=='#' || curr.charAt(curr.length()-1)=='/'){
                    curr = curr.substring(0, curr.length() - 1);
                }
                result.add(curr);
            }
            return result;

    }

    //     public static long getDate(String url) {
    //     try {
    //         URL u = new URL(url);
    //         HttpURLConnection httpCon = (HttpURLConnection) u.openConnection();
            
            
    //         return httpCon.getLastModified();
    //     } catch (Exception e) {
    //         System.out.println(e.getStackTrace());
    //     }
    //     return -1;
    // }

    public static String getDate(String url) {
        try {
            URL u = new URL(url);
            //URLConnection connection = place.openConnection();
            HttpURLConnection connection = (HttpURLConnection) u.openConnection();
            long date = connection.getLastModified();
            SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE, MMMM d, yyyy");
            if(date == 0) {
                date = connection.getDate();
            }
            return dateFormatter.format(new Date(date));
        }catch (Exception e) {
            return null;
        }
    }

    public static int getPageSize(String url) {
        try {
            URL u = new URL(url);
            HttpURLConnection httpCon = (HttpURLConnection) u.openConnection();
            BufferedReader b = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));

            String input = "";
            String temp = "";
            while((input = b.readLine())!=null) 
                temp += input;

            b.close();
            return temp.length();
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
        return -1;
    }

    public static String getTitle(String url) {
        try {
            Document document = Jsoup.connect(url).get();
            return document.title();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return "[untitled]";
    }

    public static String getBody(String url){
        try {
            Document document = Jsoup.connect(url).get();
            return document.body().text();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return null;        
    }

    public void index(String url) throws RocksDBException {
        try{
            int pageID = url.hashCode();
            //int pageID = urlToPageID.getSize();
            int tfmax = indexWords(url);
            if(urlToPageID.addEntry(url, pageID)){ //if new then need to store rest of infomation
                //pageIDs++;
                Page new_page;
                new_page =  new Page(getTitle(url), url, getDate(url), getPageSize(url), extractLinks(url), tfmax);
                pageInfo.addEntry(pageID, new_page);
            } else { //if false then need to check date
                Page curr_page = pageInfo.getPageContent(pageID);
                
                SimpleDateFormat f = new SimpleDateFormat("EEEE, MMMM d, yyyy");
                Date d1 = f.parse(curr_page.getModifiedDate());
                Date d2 = f.parse(getDate(url));
                  
                if(d1.compareTo(d2) < 0) {//if old date is before new date update
                    System.out.println("Updating Page");
                    pageInfo.delEntry(pageID);
                    Page new_page;
                    new_page =  new Page(getTitle(url), url, getDate(url), getPageSize(url), extractLinks(url), tfmax);
                    pageInfo.addEntry(pageID, new_page);
                }
            }
        } catch (Exception e){
            System.out.println("Exception Caught");
        }

    }

    public int indexWords(String url){
        int pageID = url.hashCode();

        try{
            HashMap<Integer, ArrayList<Integer>> words = extractWordIDs(url);
            forwardIndex.put(SerializationUtils.serialize(pageID), SerializationUtils.serialize(words));

            int maxTf = 0;
            for(Map.Entry<Integer, ArrayList<Integer>> pair : words.entrySet()) {
                int id = pair.getKey();

                byte[] invertedEntryBytes = invertedIndex.get(SerializationUtils.serialize(id));
                HashMap<Integer, Double> invertedEntry = new HashMap<>();
                if(invertedEntryBytes!=null){
                    invertedEntry = (HashMap<Integer, Double>)SerializationUtils.deserialize(invertedEntryBytes);
                }

                int count = pair.getValue().size();
                invertedEntry.put(pageID, (double)count);
                invertedIndex.put(SerializationUtils.serialize(id), SerializationUtils.serialize(invertedEntry));

                if(count>maxTf){
                    maxTf = count;
                }
            }

            return maxTf;


        }catch(ParserException e){
            System.out.print("Parsing exception while indexing: "+url+" Error: "+e);
        }catch(RocksDBException e){
            System.out.print("RocksDBException while indexing: "+url+" Error: "+e);
        }
        return -1;
    }

    public static void main(String[] args) throws RocksDBException{ //FOR TESTING ALREADY CREATED DBs.
        Indexer self = new Indexer();
        String url = "http://www.cse.ust.hk";
        //self.index(url);

        byte[] in = self.forwardIndex.get(SerializationUtils.serialize(url.hashCode()));
        if(in == null){
            System.out.println("NULL!!!!");
        }
        else {
            HashMap<Integer, ArrayList<Integer>> forward = (HashMap<Integer, ArrayList<Integer>>)SerializationUtils.deserialize(in);
            System.out.println(forward.get("Hong".hashCode()).get(0));
        }
    }

        // function to sort hashmap by values 
    public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> hm) 
    { 
        // Create a list from elements of HashMap 
        List<Map.Entry<String, Integer> > list = new LinkedList<Map.Entry<String, Integer> >(hm.entrySet()); 
  
        // Sort the list 
        Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() { 
            public int compare(Map.Entry<String, Integer> o1,  Map.Entry<String, Integer> o2) 
            { 
                return (o2.getValue()).compareTo(o1.getValue());  //COMPARE o2 to o1 to get reverse order
            } 
        }); 
          
        // put data from sorted list to hashmap  
        HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>(); 
        for (Map.Entry<String, Integer> aa : list) { 
            temp.put(aa.getKey(), aa.getValue()); 
        } 
        return temp; 
    } 

    public void printAll() throws RocksDBException
    {
        // Print all the data in the hashtable
        // ADD YOUR CODES HERE
//         urlToPageID.printAll();
//         System.out.println("Getting ID");
//         System.out.println(urlToPageID.getID("http://www.cse.ust.hk"));
//         System.out.println("Getting Page From ID");
// System.out.println(urlToPageID.getURL(2));

System.out.println("Getting page info from id 0");
int testID=urlToPageID.getID("http://www.cse.ust.hk");
System.out.println(pageInfo.getPageContent(testID).getTitle());
System.out.println(pageInfo.getPageContent(testID).getModifiedDate());
System.out.println(pageInfo.getPageContent(testID).getPageSize());
System.out.println(pageInfo.getPageContent(testID).getURL());
ArrayList<String> children = pageInfo.getPageContent(testID).getChildLinks();
for(String child : children)
    System.out.println(child);
    //urlToPageID.delEntry("http://www.cse.ust.hk");
     //System.out.println(urlToPageID.getID("http://www.cse.ust.hk"));
      //System.out.println(urlToPageID.getURL(0));
        //System.out.println("Getting page");
    }    
}