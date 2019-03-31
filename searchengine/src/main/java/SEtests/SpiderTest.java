package SEtests;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.htmlparser.util.ParserException;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import SE.Indexer;
import SE.MappingIndex;
import SE.Page;
import SE.PageContent;

public class SpiderTest {
    private static final String FILENAME = "spider_result.txt";

    private static Options options;
    // define paths to DBs
    private static final String workingDirectory = System.getProperty("user.dir");
    private static final String uPath = workingDirectory + "/urlToPageID";
    private static final String uInversePath = workingDirectory + "/pageIDToUrl";
    private static final String wPath = workingDirectory + "/wordIDToWord";

    private static final String pPath = workingDirectory + "/pageInfo";
    private static final String iPath = workingDirectory + "/invertedIndex";
    private static final String fPath = workingDirectory + "/forwardIndex";

    // private RocksDB urlToPageID;
    private static RocksDB urlToPageID;
    private static RocksDB wordIDToWord;
    // private RocksDB forwardIndex;
    private static PageContent pageInfo;
    private static RocksDB invertedIndex;
    private static RocksDB forwardIndex;

    public static void printInfo() throws RocksDBException, ParserException {
        options = new Options();
        options.setCreateIfMissing(true);

        Indexer i = new Indexer();
        FileWriter fw = null;

        //i.printAll();
        try{
            fw = new FileWriter(FILENAME);
        }catch(Exception e){
            e.printStackTrace();
        }


        //loop through urls
        RocksIterator iter = i.getMappingIndex().getIterator();  
        for(iter.seekToFirst(); iter.isValid(); iter.next()) {
            String url = new String(iter.key(), StandardCharsets.UTF_8);
            int pageID = i.getMappingIndex().getID(url);
            System.out.println(url + " -> " + pageID);

            //child links
            ArrayList<String> children = Indexer.extractLinks(url);
            System.out.println("number of children: " + children.size());

            //check pageDB
            PageContent pc = i.getPageContent();
            Page p = pc.getPageContent(pageID);
            String title = "[untitled]";
            String date = "[date]";
            int size = -1;

            try{
                title = p.getTitle();
                date = p.getModifiedDate();
                size = p.getPageSize();
            }catch(Exception e){
            }

            //wordlist            
            HashMap<Integer, ArrayList<Integer>> words = i.extractWordIDs(url);
            System.out.println("words: " + words.size());
            System.out.println();

            writeToFile(i, fw, title, url, date, size, words, children);
        }

    }

    public static void writeToFile(Indexer indexer, FileWriter fw, String title, String url, String date, int pageSize, HashMap<Integer, ArrayList<Integer>> words, ArrayList<String> childLinks) throws RocksDBException {
        
        BufferedWriter bw = null;

        try {

            bw = new BufferedWriter(fw);
            bw.write(title + "\n" + url + "\n" + date + ", " + pageSize + "\n");

            //write words
            Iterator it = words.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                Integer wordID = (Integer) pair.getKey();
                ArrayList<Integer> locations = (ArrayList<Integer>) pair.getValue();
                //todo convert wordID to word
                bw.write(indexer.getWordfromID(wordID) + " " + locations.size() + "; ");
            }

            bw.write("\n");

            //write child links
            for(String s: childLinks){
                bw.write(s + "\n");
            }

            bw.write("------------------------------------------------------------------------------" + "\n");
            bw.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } 
    }            
    
    public static void main(String[] args) throws RocksDBException, ParserException {
        printInfo();
    }
}