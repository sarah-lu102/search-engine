
package SE;

import java.util.Date;
import java.util.HashSet;
import java.io.Serializable;
import java.util.ArrayList;

public class Page implements Serializable
{
	String title;
	String url;
	String lastModifiedDate;
	int size;
	int tfmax;
	double magnitude;
	ArrayList<String> childLinks;
	HashSet<String> parentLinks;
	
	public Page(String title, String url, String lastModifiedDate, int size, ArrayList<String> childLinks, int tfmax)
	{
		this.title = title;
		this.url = url;
		this.lastModifiedDate = lastModifiedDate;
		this.size = size;
		this.childLinks = childLinks;
		this.tfmax = tfmax;
	}

	public void setMagnitude(double magnitude){ this.magnitude = magnitude; }

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

	public void setParents(HashSet<String> parents){
		parentLinks = parents;
	}

	public HashSet<String> getParentLinks(){
		return parentLinks;
	}
}
/*Page class only contain simple imformation of a page,
 * to print out <word, freq> and child link, retrieve from inverted files
  */
//