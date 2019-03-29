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
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.beans.LinkBean;
import org.htmlparser.util.ParserException;

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
    // URLs already visited
    static Set<String> marked = new HashSet<>();

    // URL Pattern regex

    // Start from here
    static String root = "http://www.cse.ust.hk";

    // BFS Routine
    public static void bfs() throws IOException, RocksDBException {
        Queue<String> urlQ = new LinkedList<>();
        urlQ.add(root);
        marked.add(root);
        int count = 1;

        while(!urlQ.isEmpty())
        {
            String currentURL = urlQ.poll();
            System.out.println("Site expanded: "+ currentURL);

            try {
                ArrayList<String> children = Indexer.extractLinks(currentURL);

                for (String child : children) {
                    if (count < 30 && indexer.validURL(currentURL) && !marked.contains(child)) {
                        count++;
                        urlQ.add(child);
                        marked.add(child);
                    }
                }
            }catch(ParserException e){
                System.out.println("Parser exception: "+e);
            }

            indexer.index(currentURL);
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
    public static void main(String[] args) throws RocksDBException{
        // a static method that loads the RocksDB C++ library.
        RocksDB.loadLibrary();
        
        try {
            indexer = new Indexer();
        } catch (RocksDBException e) {
               System.out.println(e.getStackTrace());
        }

        try{
            bfs();
          //  displayResults();
            indexer.printAll();

        }catch(IOException e){
            System.out.println("IOException caught : "+e);
        }
    }
}
