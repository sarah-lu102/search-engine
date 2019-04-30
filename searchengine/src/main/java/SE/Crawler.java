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
import java.util.HashMap;
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
    private Indexer indexer;

    // Start from here
    static String root = "http://www.cse.ust.hk";

    public Crawler(){
        try {
            this.indexer = new Indexer();
        } catch (RocksDBException e) {
            System.out.println(e.getStackTrace());
        }
    }

    // BFS Routine
    public void bfs() throws IOException, RocksDBException {
        Queue<String> urlQ = new LinkedList<>();
        Set<Integer> marked = new HashSet<>();
        urlQ.add(root);
        marked.add(root.hashCode());
        ArrayList<String> rootParent = new ArrayList<>();
        Page rootPage = new Page(root, rootParent);
        indexer.pageInfo.addEntry(root.hashCode(), rootPage);

        int count = 1;

        while(!urlQ.isEmpty())
        {
            String currentURL = urlQ.poll();
            System.out.println("Site expanded: "+ currentURL);

            try {
                ArrayList<String> children = Indexer.extractLinks(currentURL);
                {
                    Page currentPage = indexer.pageInfo.getPageContent(currentURL.hashCode());
                    indexer.pageInfo.delEntry(currentURL.hashCode());
                    currentPage.childLinks = children;
                    indexer.pageInfo.addEntry(currentURL.hashCode(), currentPage);
                }

                for (String child : children) {
                    int childHash = child.hashCode();
                    if(marked.contains(childHash)){
                        //add to its parent list
                        Page childPage = indexer.pageInfo.getPageContent(childHash);
                        indexer.pageInfo.delEntry(childHash);
                        childPage.parentLinks.add(currentURL);
                        indexer.pageInfo.addEntry(childHash, childPage);
                    }
                    else if (count < 30 && indexer.validURL(currentURL)) {
                        if(count%100==0)
                            System.out.print(count);
                        count++;
                        urlQ.add(child);
                        marked.add(childHash);
                        //create a parent list
                        ArrayList<String> parentList = new ArrayList<>();
                        parentList.add(currentURL);
                        Page newPage = new Page(child, parentList);
                        indexer.pageInfo.delEntry(childHash);
                        indexer.pageInfo.addEntry(childHash, newPage);
                    }

                }
                if(count>=30 && urlQ.size()%100==0){
                    System.out.println(urlQ.size()+" left to index");
                }
            }catch(ParserException e) {
                System.out.println("Parser exception: " + e);
            }
            indexer.index(currentURL);
        }
    }
    
    //Display results from SET marked
    /*public static void displayResults(){
        System.out.println("\n\nResults: ");
        System.out.println("\nWeb sites crawled : "+marked.size()+"\n");
        for(String s:marked){
            System.out.println(s + " -- date: " + indexer.getDate(s) + " -- title: " + indexer.getTitle(s));
            System.out.println ("page size: " + indexer.getPageSize(s));
            System.out.println(indexer.getBody(s) + "\n");
        }
    }*/

    /*public static void printParents(){
        for(String url:marked){
            try {
                System.out.println("url: " + url);
                for(String s: parents.get(url)){
                    System.out.println(s);
                }
                System.out.println("--------------------------------");
                
            } catch (Exception e) {
                //TODO: handle exception
            }
        }
    }*/
    
    //Run
    public static void main(String[] args) throws RocksDBException{
        // a static method that loads the RocksDB C++ library.
        RocksDB.loadLibrary();
        Crawler crawl = new Crawler();




        try{
            crawl.bfs();
            System.out.println("Reweighing: ");
            crawl.indexer.reweight(2000);
            System.out.print("Done reweighting");
            crawl.indexer.updatePageInfo();
            //printParents();
            //indexer.printAll();

        }catch(IOException e){
            System.out.println("IOException caught : "+e);
        }
    }
}
