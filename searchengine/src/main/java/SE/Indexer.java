package SE;
import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import org.rocksdb.RocksDB;
import org.rocksdb.Options;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

public class Indexer {
    // define paths to
    private final String uPath = "/urlToPageID";
    private final String wPath = "/wordToWordID";
    private final String fPath = "/forwardIndex";
    private final String iPath = "/invertedIndex";
    private RocksDB urlToPageID;
    private RocksDB wordToWordID;
    private RocksDB forwardIndex;
    private RocksDB invertedIndex;

    private Options options;

    public Indexer() throws RocksDBException {
        this.options = new Options();
        this.options.setCreateIfMissing(true);
        RocksDB.open(options, uPath);
        RocksDB.open(options, wPath);
        RocksDB.open(options, fPath);
        RocksDB.open(options, iPath);
    }

    public long getDate(String url) {
        try {
            URL u = new URL(url);
            HttpURLConnection httpCon = (HttpURLConnection) u.openConnection();
            return httpCon.getLastModified();
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
        return -1;
    }

    public long getPageSize(String url) {
        try {
            URL u = new URL(url);
            HttpURLConnection httpCon = (HttpURLConnection) u.openConnection();
            return httpCon.getContentLength();
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
        return -1;
    }

    public String getTitle(String url) {
        try {
            Document document = Jsoup.connect(url).get();
            return document.title();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return "[untitled]";
    }

    public String getBody(String url){
        try {
            Document document = Jsoup.connect(url).get();
            return document.body().text();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return null;        
    }

    public void index(String url){
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
}