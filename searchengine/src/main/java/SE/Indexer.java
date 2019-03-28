package SE;
import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class Indexer{

    public long getDate(String url){
        try{
            URL u = new URL(url);
            HttpURLConnection httpCon = (HttpURLConnection) u.openConnection();
            return httpCon.getLastModified();
        } catch (Exception e){
            System.out.println(e.getStackTrace());
        }
        return 0;
    }

    public long getPageSize(String url){
        try{
            URL u = new URL(url);
            HttpURLConnection httpCon = (HttpURLConnection) u.openConnection();
            return httpCon.getContentLength();
        } catch (Exception e){
            System.out.println(e.getStackTrace());
        }
        return 0;
    }

    public String getTitle(String url){
        Document doc = Jsoup.parse(url);
        return doc.title();
    }

    public String getBody(String url){
        Document doc = Jsoup.parse(url);
        return doc.body().text();
    }
}