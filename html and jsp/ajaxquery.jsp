<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
	<body>

		<%@page language="java"%>
		<%@page import="java.util.StringTokenizer"%>
		<%@page import="java.util.Vector"%>
		<%@page import="java.util.*"%>
		<%@page import="java.io.IOException"%>
		<%@page import="java.util.Map"%>
		<%@page import="java.io.*"%>
		<%@page import="java.util.Iterator"%>
		
		
		<%@page import="lib.*"%>
		<%@page import="SE.*"%>

		<% Indexer indexer; %>
		<% Searcher se; %>	

		<%

		if(request.getParameter("val")!=null) {
			
			if(request.getParameter("val").length() > 2){
			//out.println("Helo");
			//out.println("Working");
			try{
			//List<Map.Entry<Integer, Double>> result = null;
			//out.println("searching: " + request.getParameter("val") + "<br>");
				se = new Searcher();
				List<Map.Entry<Integer, Double>> result = se.search(request.getParameter("val"));
				se.finalise();
			//out.println("finished searching: " + request.getParameter("val") + "<br>");

				indexer = new Indexer();
				
				if((result != null) && (result.size() != 0)){
					//out.println("results");
					for(int i=0; ((i< result.size()) && (i < 3));i++){
					%>
						<div class = "card m-3 w-25 d-inline-block">
							

						<%
							int pageID = result.get(i).getKey();
							double score = result.get(i).getValue();
							//out.println("PageID: " + pageID + " Score: " + score + "<br>");
							PageContent pc = indexer.getPageContent();
		            		Page p = pc.getPageContent(pageID);
		            		String title = "[untitled]";
				          String date = "[date]";
				            int size = -1;
				            String url = "[url]";
				            HashMap<Integer, ArrayList<Integer>> words = null;
				            //ArrayList<String> children = null;
				            //HashSet<String> parents = null;

				            try{
				                title = p.getTitle();
				                date = p.getModifiedDate();
				                size = p.getPageSize();
				                url = p.getURL();
				                words = indexer.extractWordIDs(url);
				                //children = p.getChildLinks();
				                //parents = p.getParentLinks();

				            }catch(Exception e){
				            }

				            //out.println(pageID + ", " + score + ", " + title);
				            %>
				           	<h3 class="card-header">
				           		<%
								out.println("<a href='" + url + "' target='_blank'> " + title + "</a>");
								out.println("<a href='" + url + "'  target='_blank'> (" + url + ")</a><br>");
								
				            	%>
		            		</h3>
		            		<div class = "card-body">

				            	<h3 class="card-title">
					           	<%
									
									out.println(" Score: " + result.get(i).getValue() + "<br>");
					            %>
				            	</h3>

				            	<p class="card-text">
					            <%
					            
					            out.println("Words: ");
					            //out.println("words: " + words.size());
				            	Iterator it = words.entrySet().iterator();
				            	HashMap<String, Integer> wordScores = new HashMap<String, Integer>();
					            while (it.hasNext()) {
					                Map.Entry pair = (Map.Entry) it.next();
					                Integer wordID = (Integer) pair.getKey();
					                ArrayList<Integer> locations = (ArrayList<Integer>) pair.getValue();
					                //todo convert wordID to word
					                //out.println("(" + indexer.getWordfromID(wordID) + " , " + locations.size() + ") ");
					                wordScores.put(indexer.getWordfromID(wordID), locations.size());
					            }

					            Map<String, Integer> sortedWordScores = indexer.sortByValue(wordScores);
					            Iterator iWords = sortedWordScores.entrySet().iterator();
					            int count = 0;
					            while(iWords.hasNext() && count < 5){
					            	Map.Entry pair = (Map.Entry) iWords.next();
					            	out.println("(" + pair.getKey() + ", " + pair.getValue() + ")");
					            	count++;
					            } 
					             out.println("</p>");
					            %>
					        </div>
				        	<div class="card-footer text-muted">
				        		<%
				            		out.println("Date Last Modified: " + date + "  |  ");
				           			out.println("Size: " + size + "<br>");
				           		%>	
		            		</div>
		       			</div>
				            
				        <%
				    }
				} else {
					//out.println("No results");
				}

				indexer.finalise();
				} catch(Exception e) {
					System.err.println(e.toString());
				}
				//out.println("Work?");
			}	

		} else {
			out.println("No data");
		}
		%>

	</body>

</html>