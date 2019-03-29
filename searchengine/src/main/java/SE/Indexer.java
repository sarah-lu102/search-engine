package SE;
import java.util.Vector;

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

public class Indexer {
    // define paths to DBs
    private final String workingDirectory = System.getProperty("user.dir");
    private final String uPath = workingDirectory + "/urlToPageID";
    private final String uInversePath = workingDirectory + "/pageIDToUrl";
    private final String wPath = workingDirectory + "/wordToWordID";
    
    private final String fPath = workingDirectory + "/forwardIndex";
    private final String iPath = workingDirectory + "/invertedIndex";
    //private RocksDB urlToPageID;
    private MappingIndex urlToPageID;
    private MappingIndex wordToWordID;
    //private RocksDB forwardIndex;
     private PageContent forwardIndex;
    private RocksDB invertedIndex;

    private Options options;

    //private int pageIDs = 0;

    public Indexer() throws RocksDBException {
        this.options = new Options();
        this.options.setCreateIfMissing(true);
       // urlToPageID = RocksDB.open(options, uPath);
        urlToPageID = new MappingIndex(uPath, uInversePath);
        //wordToWordID = new MappingIndex(wPath);
        //wordToWordID = RocksDB.open(options, wPath);
        //forwardIndex = RocksDB.open(options, fPath);
        forwardIndex = new PageContent(fPath);
        invertedIndex = RocksDB.open(options, iPath);
    }

    public static Boolean validURL(String url){
        String s = getBody(url);
        if(s != null && !s.isEmpty()) return true;
        return false;
    }

    public static ArrayList<String> extractWords(String url) throws ParserException
    {
        ArrayList<String> result = new ArrayList<String>();
        StringBean bean = new StringBean();
        bean.setURL(url);
        bean.setLinks(false);
        String contents = bean.getStrings();
        StringTokenizer st = new StringTokenizer(contents);
        while (st.hasMoreTokens()) {
            result.add(st.nextToken());
        }

        return result;

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

    // public static Date getDate(String url) {
    //     try {
    //         URL u = new URL(url);
    //         HttpURLConnection httpCon = (HttpURLConnection) u.openConnection();
    //         System.out.println(httpCon.getLastModified());
    //         Date date = new Date(httpCon.getLastModified());
            
    //         return date;
    //     } catch (Exception e) {
    //         System.out.println(e.getStackTrace());
    //     }
    //     return Calendar.getInstance().getTime();
    // }

    // public static long getPageSize(String url) {
    //     try {
    //         URL u = new URL(url);
    //         HttpURLConnection httpCon = (HttpURLConnection) u.openConnection();
    //         return httpCon.getContentLength();
    //     } catch (Exception e) {
    //         System.out.println(e.getStackTrace());
    //     }
    //     return -1;
    // }

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
        // byte[] content = urlToPageID.get(url.getBytes());
        // if (content == null) {
        //     content = ("doc 1").getBytes();
        // } else {
        //     content = (new String(content) + " doc 1").getBytes();
        //     //content = ("doc 1").getBytes();
        // }
        // urlToPageID.put(url.getBytes(), content);
        //int pageID = urlToPageID.getSize();
        //System.out.println(pageID);

       

        int pageID = url.hashCode();
        //int pageID = urlToPageID.getSize();
        
        if(urlToPageID.addEntry(url, pageID)){ //if new then need to store rest of infomation
            //pageIDs++;
            Page new_page;
            new_page =  new Page(getTitle(url), url, getDate(url), getPageSize(url));
            forwardIndex.addEntry(pageID, new_page);
        } //if false then need to check date
        
        /*
        * TODO:
        * check if page has already been indexed
        * check if date has been updated
        * if date = -1, set to current date
        * if size = -1, get length of content (count the number of characters = size of body string?)
        * function: url to pageID mapping in RocksDB
        * function: word to wordID mapping in RocksDB
        * function: create/update forward index in RocksDB
        * function: create/update inverted index in RocksDB
        */
    }

    public void printAll() throws RocksDBException
    {
        // Print all the data in the hashtable
        // ADD YOUR CODES HERE
        urlToPageID.printAll();
        System.out.println("Getting ID");
        System.out.println(urlToPageID.getID("http://www.cse.ust.hk"));
        System.out.println("Getting Page From ID");
System.out.println(urlToPageID.getURL(2));

System.out.println("Getting page info from id 0");
System.out.println(forwardIndex.getPageContent(1).getTitle());
System.out.println(forwardIndex.getPageContent(1).getModifiedDate());
System.out.println(forwardIndex.getPageContent(1).getPageSize());
System.out.println(forwardIndex.getPageContent(1).getURL());
    //urlToPageID.delEntry("http://www.cse.ust.hk");
     //System.out.println(urlToPageID.getID("http://www.cse.ust.hk"));
      //System.out.println(urlToPageID.getURL(0));
        //System.out.println("Getting page");
    }    
}