package SE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.rocksdb.RocksDB;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.rocksdb.Options;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import SE.Indexer;

public class Crawler {
    private Options options;
    private static Indexer indexer;
    // Queue for BFS
    static Queue<String> q = new LinkedList<>();

    // URLs already visited
    static Set<String> marked = new HashSet<>();

    // URL Pattern regex
    static String regex = "http[s]*://(\\w+\\.)*(\\w+)";

    // Start from here
    static String root = "http://www.cse.ust.hk";

    // BFS Routine
    public static void bfs() throws IOException {
        q.add(root);
        while (!q.isEmpty()) {
            String s = q.poll();

            if (marked.size() > 30)
                return;

            boolean ok = false;
            String body = null;
            URL url = null;
            BufferedReader br = null;

            while (!ok) {
                try {
                    url = new URL(s);
                    br = new BufferedReader(new InputStreamReader(url.openStream()));
                    ok = true;
                }catch(MalformedURLException e){
                    System.out.println("\nMalformedURL : "+s+"\n");
                    marked.remove(s);
                    //Get next URL from queue
                    s = q.poll();
                    ok = false;
                }catch(IOException e){
                    System.out.println("\nIOException for URL : "+s+"\n");
                    marked.remove(s);
                    //Get next URL from queue
                    s = q.poll();
                    ok = false;
                }catch(Exception e){
                    System.out.println("\nException for URL : "+s+"\n");
                    marked.remove(s);
                    //Get next URL from queue
                    s = q.poll();
                    ok = false;
                }
            }         
            
            StringBuilder sb = new StringBuilder();
            String currURL = s;

            //TODO put s into url->pageID mapping, or check if it exists already
            
            while((s = br.readLine())!=null){
                sb.append(s);
            }
            s = sb.toString();
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(s);
            
            while(matcher.find()){
                String w = matcher.group(); 
                
                if(!marked.contains(w)){
                    if(marked.size()>=30) return;
                    try { //check if the web page body has text. if not, don't include.
                        Document document = Jsoup.connect(w).get();
                        body = document.body().text();
                    } catch (Exception e) {
                        body = null;
                    }
                    if(body != null && !body.isEmpty()){
                        marked.add(w);
                        /*
                        /*TODO: url->PageID mapping
                        * (call the indexing function)
                        * add to currURL's children
                        */
                        System.out.println("Site : "+w);
                    }
                    q.add(w);
                }
            } 
        }
    }
    
    //Display results from SET marked
    public static void displayResults(){
        System.out.println("\n\nResults: ");
        System.out.println("\nWeb sites crawled : "+marked.size()+"\n");
        for(String s:marked){
            System.out.println(s + " -- date: " + indexer.getDate(s) + " -- title: " + indexer.getTitle(s));
            System.out.println ("page size: " + indexer.getPageSize(s));
            System.out.println(indexer.getBody(s) + "\n");
        }
    }
    
    //Run
    public static void main(String[] args){
        // a static method that loads the RocksDB C++ library.
        RocksDB.loadLibrary();
        
        try {
            indexer = new Indexer();
        } catch (RocksDBException e) {
               System.out.println(e.getStackTrace());
        }

        try{
            bfs();
            displayResults();

        }catch(IOException e){
            System.out.println("IOException caught : "+e);
        }
    }
}
