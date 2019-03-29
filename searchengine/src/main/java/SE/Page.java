
package SE;

import java.util.Date;
import java.io.Serializable;
import java.util.ArrayList;

public class Page implements Serializable
{
	String title;
	String url;
	String lastModifiedDate;
	int size;
	ArrayList<String> childLinks;
	
	public Page(String title, String url, String lastModifiedDate, int size, ArrayList<String> childLinks)
	{
		this.title = title;
		this.url = url;
		this.lastModifiedDate = lastModifiedDate;
		this.size = size;
		this.childLinks = childLinks;
	}

	public String getModifiedDate(){
		return lastModifiedDate;
	}

	public void setModifiedDate(String date){
		this.lastModifiedDate = date;
	}
	
	public String getTitle(){
		return title;
	}

	public String getURL(){
		return url;
	}

	public int getPageSize(){
		return size;
	}
	
	public void setTitle(String _title) {
		title = _title;
	}

	public ArrayList<String> getChildLinks(){
		return childLinks;
	}
}
/*Page class only contain simple imformation of a page,
 * to print out <word, freq> and child link, retrieve from inverted files
  */
//