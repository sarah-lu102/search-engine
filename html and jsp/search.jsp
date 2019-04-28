<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>


	<head>
		<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css" integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">
		<link href="//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap-glyphicons.css" rel="stylesheet">
		<link rel="stylesheet" type="text/css" href="style.css">

	</head>

	<body>
	<div class="container h-100 d-flex justify-content-center searchResults">
		<div class="searchResultsWrapper">
	<img src="logo.png" height="150">
	<!-- <a href="project.html">Back To Search?</a> -->
	<div class="text-center">
		<a class="btn btn-primary btn-lg" href="project.html" role="button">Search Again?</a> <br>
		</div>
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
		if(request.getParameter("search_terms")!=null) {
			
			
			out.println("<h2> Search Results for query: <b>"+ request.getParameter("search_terms") + "</b></h2>");	
			/*
			StringTokenizer st = new StringTokenizer(request.getParameter("search_terms"));
			Vector<String> key = new Vector<String>();
			while(st.hasMoreTokens()) {
				//out.println(st.nextToken() + "<br>");
				key.add(st.nextToken());
			}
			*/

			se = new Searcher();
			List<Map.Entry<Integer, Double>> result = se.search(request.getParameter("search_terms"));
			se.finalise();


			indexer = new Indexer();
			//indexer.printAll(); //TO TEST that indexer can pull from database file - will replace with stuff to print out
			if((result != null) && (result.size() != 0)){
				for(int i=0; i< result.size();i++){
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
		            ArrayList<String> children = null;
		            HashSet<String> parents = null;

		            try{
		                title = p.getTitle();
		                date = p.getModifiedDate();
		                size = p.getPageSize();
		                url = p.getURL();
		                words = indexer.extractWordIDs(url);
		                children = p.getChildLinks();
		                parents = p.getParentLinks();

		            }catch(Exception e){
		            }

		            out.println("Title: <a href='" + url + "'> " + title + "</a><br>");
		            out.println("URL: <a href='" + url + "'> " + url + "</a><br>");
		            out.println("Score: " + result.get(i).getValue() + "<br>");
		            //URL
		            out.println("Date Last Modified: " + date + "<br>");
		            out.println("Size: " + size + "<br>");

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

		            //sortedWords = sortedWords.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,LinkedHashMap::new));
		             //   Map<String, Integer> sortedWordScores = wordScores.entrySet().stream().sorted(comparingByValue()).collect(toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2,LinkedHashMap::new));
		            Map<String, Integer> sortedWordScores = indexer.sortByValue(wordScores);
		           /* for (Map.Entry<String, Integer> pair : sortedWordScores.entrySet()) { 
			           // System.out.println("Key = " + en.getKey() +  
			                          //", Value = " + en.getValue()); 
			            out.println("(" + pair.getKey() + ", " + pair.getValue() + ")");
			        } 
			        */
		            Iterator iWords = sortedWordScores.entrySet().iterator();
		            int count = 0;
		            while(iWords.hasNext() && count < 5){
		            	Map.Entry pair = (Map.Entry) iWords.next();
		            	out.println("(" + pair.getKey() + ", " + pair.getValue() + ")");
		            	count++;
		            } 

		            out.println("<br>");

		            out.println("Parent Links: ");
		            if((parents.size() - 5) > 0){
		            	out.println("(Showing 5 out of " + parents.size() + " links) <br>");
		            }

		            count = 0;
		            for(String parent : parents){
		            	if(count < 5){
		               		out.println(parent + "<br>");
   							count++;	
		            	} else {
		            		break;
		            	}

		            }

   					out.println("Child Links: ");
   					if((children.size() - 5) > 0){
		            	out.println("(Showing 5 out of " + children.size() + " links) <br>");
		            }
   					count=0;
		            for(String child : children){
		            	if(count < 5){
		               		out.println(child + "<br>");
   							count++;	
		            	} else {
		            		break;
		            	}
		            }

		            out.println("------------------------------------------------------------------------------<br>");

				}
			} else {
				out.println("No page Found for the query! <br> Please Try again");
			}

			indexer.finalise();

		} else {
			out.println("You input nothing");
		}
		
		%>
		</div>
		</div>
	</body>
</html>