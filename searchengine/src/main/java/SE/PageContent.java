/* --
COMP4321 Lab1 Exercise
Student Name:
Student ID:
Section:
Email:
*/
package SE;
import org.rocksdb.RocksDB;
import org.rocksdb.Options;
import org.rocksdb.RocksDBException;  
import org.rocksdb.RocksIterator;
import org.apache.commons.lang3.*;

public class PageContent
{
    private RocksDB db;
    private Options options;

    PageContent(String dbPath) throws RocksDBException
    {
        // the Options class contains a set of configurable DB options
        // that determines the behaviour of the database.
        this.options = new Options();
        this.options.setCreateIfMissing(true);

        // creat and open the database
        this.db = RocksDB.open(options, dbPath);
    }

    public void addEntry(int pageID, Page page) throws RocksDBException
    {
        // Add a "docX Y" entry for the key "word" into hashtable
        // ADD YOUR CODES HERE
    byte[] data = SerializationUtils.serialize(page); //Seralize
//YourObject yourObject = SerializationUtils.deserialize(data) //DeSeralize
//System.out.println(data);
            byte[] content = db.get(Integer.toString(pageID).getBytes());
        if (content == null) {
            content = data;
        } else {
            //content = (new String(content) + " doc 1").getBytes();
            System.out.println("Already Done Page " + pageID);
        }
    // byte[] content;
    // try{
    //     content =db.get(Integer.toString(pageID).getBytes())) ;
    //     System.out.println("Already Done Page " + pageID);
    // } catch (IllegalArgumentException e){
    //     content = data;
    // }
      
        //Page content = (Page) content;
        //System.out.println("HELLO");
       // if (content == null) {
            
           // content = page;
            //content = (page).getBytes();
        //} else {

            
            //content = (new String(content) + " doc 1").getBytes();
        //}
        db.put(Integer.toString(pageID).getBytes(), content);
    }
    public void delEntry(int pageID) throws RocksDBException
    {
        // Delete the word and its list from the hashtable
        // ADD YOUR CODES HERE
        db.remove(Integer.toString(pageID).getBytes());
    } 

    public Page getPageContent(int pageID) throws RocksDBException {
        byte[] content = db.get(Integer.toString(pageID).getBytes());
        if(content == null) {
            return null;
        } else {
            Page page = SerializationUtils.deserialize(content);
            return page;
        }
    }
    // public void printAll() throws RocksDBException
    // {
    //     // Print all the data in the hashtable
    //     // ADD YOUR CODES HERE
    //     RocksIterator iter = db.newIterator();
                    
    //     for(iter.seekToFirst(); iter.isValid(); iter.next()) {
    //         System.out.println(new String(iter.key()) + "=" + new String(iter.value()));
    //     }
    // }    
    
    // public static void main(String[] args)
    // {
    //     try
    //     {
    //         // a static method that loads the RocksDB C++ library.
    //         RocksDB.loadLibrary();

    //         // modify the path to your database
    //         String path = "/home/amgfeuer/lab1/db";
            
    //         InvertedIndex index = new InvertedIndex(path);
    
    //         index.addEntry("cat", 2, 6);
    //         index.addEntry("dog", 1, 33);
    //         System.out.println("First print");
    //         index.printAll();
            
    //         index.addEntry("cat", 8, 3);
    //         index.addEntry("dog", 6, 73);
    //         index.addEntry("dog", 8, 83);
    //         index.addEntry("dog", 10, 5);
    //         index.addEntry("cat", 11, 106);
    //         System.out.println("Second print");
    //         index.printAll();
            
    //         index.delEntry("dog");
    //         System.out.println("Third print");
    //         index.printAll();
    //     }
    //     catch(RocksDBException e)
    //     {
    //         System.err.println(e.toString());
    //     }
   // }
}
