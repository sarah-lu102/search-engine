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


public class MappingIndex
{
    private RocksDB db;
    private RocksDB inverse_db;
    private Options options;

    MappingIndex(String dbPath, String inverse_dbPath) throws RocksDBException
    {
        // the Options class contains a set of configurable DB options
        // that determines the behaviour of the database.

        this.options = new Options();
        this.options.setCreateIfMissing(true);

        // creat and open the database

        this.db = RocksDB.open(options, dbPath);
        this.inverse_db = RocksDB.open(options, inverse_dbPath);

    }

    public boolean addEntry(String word, int id) throws RocksDBException
    {
        // Add a "docX Y" entry for the key "word" into hashtable
        // ADD YOUR CODES HERE
        byte[] content = db.get(word.getBytes());
        if (content == null) {
            content = Integer.toString(id).getBytes();

        } else {
            System.out.println("Already done: " + word);
            return false;
         //   content = (new String(content) + " doc " + id).getBytes();
        }
        db.put(word.getBytes(), content);
        inverse_db.put(content, word.getBytes());
        return true;
    }
    public void delEntry(String word) throws RocksDBException
    {
        // Delete the word and its list from the hashtable
        // ADD YOUR CODES HERE
        int tempID = this.getID(word);
        if(tempID==-1){
            return;
        }
        db.remove(word.getBytes());
        inverse_db.remove(Integer.toString(tempID).getBytes());
    } 

    public void printAll() throws RocksDBException
    {
        // Print all the data in the hashtable
        // ADD YOUR CODES HERE
        RocksIterator iter = db.newIterator();
                    
        for(iter.seekToFirst(); iter.isValid(); iter.next()) {
            System.out.println(new String(iter.key()) + "=" + new String(iter.value()));
        }
    } 

    public int getID(String value) throws RocksDBException
    {
        int tempID=-1;
        try{
            tempID = Integer.parseInt(new String(db.get(value.getBytes())));
        }catch (NullPointerException e){

        }
        
        return tempID;
    }

    public String getURL(int id) throws RocksDBException
    {
        String tempURL = null;
        try{
            tempURL = new String(inverse_db.get(Integer.toString(id).getBytes()));
           
        }catch (NullPointerException e){

        }
        
        return tempURL;
    }

    public int getSize() throws RocksDBException{
        int num=0;
                RocksIterator iter = db.newIterator();       
        for(iter.seekToFirst(); iter.isValid(); iter.next()) {
            num++;
        }
        return num;
    }
    
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
