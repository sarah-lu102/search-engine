<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>


	<head>
		<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css" integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">
		<link href="//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap-glyphicons.css" rel="stylesheet">
		<link rel="stylesheet" type="text/css" href="style.css">
		<script src="https://code.jquery.com/jquery-3.3.1.slim.min.js" integrity="sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo" crossorigin="anonymous"></script>
		<script>

		
		$(function() {
			parent_count=5;
			child_count=5;

		  	$(".show-more-parents").click(function(){
		  		num = $('.num-parent-links').data('num');
				if(parent_count != num){
						$(".parents a").slice(parent_count, parent_count+5).removeClass('d-none');
					parent_count=parent_count+5;
					if(parent_count > num) {
						parent_count = num;
					}
					$('.num-parent-links').html("<b>Parent Links: (Showing "+ parent_count+" out of " + num + " links) </b>");			
				}
			});

			$(".show-more-children").click(function(){
		  		num = $('.num-child-links').data('num');
				if(child_count != num){
						$(".children a").slice(child_count, child_count+5).removeClass('d-none');
					child_count=child_count+5;
					if(child_count > num) {
						child_count = num;
					}
					$('.num-child-links').html("<b>Child Links: (Showing "+ child_count+" out of " + num + " links) </b>");			
				}
			});
		});

		</script>
	</head>

	<body>
	<div class="container h-100 d-flex justify-content-center searchResults">
		<div class="searchResultsWrapper">
	<img src="logo.png" height="150">
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
			
			
			out.println("<h2 class='query'> Search Results for query: <b>"+ request.getParameter("search_terms") + "</b></h2>");	


			se = new Searcher();
			List<Map.Entry<Integer, Double>> result = se.search(request.getParameter("search_terms"));
			se.finalise();


			indexer = new Indexer();
			
			if((result != null) && (result.size() != 0)){
				for(int i=0; i< result.size();i++){
				%>
				<div class = "card mx-auto mb-3 w-75">
					

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
		           // HashMap<Integer, ArrayList<Integer>> words = null;
		            ArrayList<String> children = null;
		            ArrayList<String> parents = null;
		            int[][] top5Words = null;
		            //
		            int count = 0;
		            try{
		                title = p.getTitle();
		                date = p.getModifiedDate();
		                size = p.getPageSize();
		                url = p.getURL();
		                //words = indexer.extractWordIDs(url);
		                children = p.getChildLinks();
		                parents = p.getParentLinks();
		                
		                top5Words = p.getTopKeywords();
		            }catch(Exception e){
		            }
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
			            
			            for(int j=0; j<5;j++){ //top 5 words
			            	Integer wordID = top5Words[j][0];
			            	Integer wordCount = top5Words[j][1];
			            	out.println("(" + indexer.getWordfromID(wordID) + ", " + wordCount+")");
			            }
			            
			            /*
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
			            count = 0;
			            while(iWords.hasNext() && count < 5){
			            	Map.Entry pair = (Map.Entry) iWords.next();
			            	out.println("(" + pair.getKey() + ", " + pair.getValue() + ")");
			            	count++;
			            } 
			            */
			             out.println("</p>");
			            %>
			            <ul class="list-group list-group-flush parents">

			            <%
			            out.println("<h4 class='num-parent-links' data-num='"+parents.size()+"'> <b>Parent Links: ");
			            if((parents.size() - 5) > 0){
			            	out.println("(Showing 5 out of " + parents.size() + " links) </b></h4>");
			            } else {
			           		out.println("(Showing " + parents.size()+" out of " + parents.size() + " links) </b></h4>");
			            }
			            %>

			            <div class="show-more-parents text-primary">
			            Show More...
			            </div>
			            
			            <%
			            count = 0;
			            for(String parent : parents){
			            	if(count < 5){
			               		out.println("<a href='" + parent+"' class='list-group-item list-group-item-action'  target='_blank'>"+parent+"</a>");
	   							
			            	} else {
			            	
			            		out.println("<a href='" + parent+"' class='list-group-item list-group-item-action d-none'  target='_blank'>"+parent+"</a>");
			            	}
			            	count++;	

			            }
			            %>
			            </ul>
			            <br>
			            <ul class="list-group list-group-flush children">
			            <%
	   					out.println("<h4 class='num-child-links' data-num='"+children.size()+"'> <b>Child Links: ");
	   					if((children.size() - 5) > 0){
			            	out.println("(Showing 5 out of " + children.size() + " links)</b> </h4>");
			            } else {
			            	out.println("(Showing " + children.size()+ " out of " + children.size() + " links)</b> </h4>");
			            }
			            		            %>
			            <div class="show-more-children text-primary">
			            	Show More...
			            </div>
			            
			            <%
	   					count=0;
			            for(String child : children){
			            	if(count < 5){
			            		out.println("<a href='" + child+"' class='list-group-item list-group-item-action'  target='_blank'>"+child+"</a>");
	   								
			            	} else {
			            		out.println("<a href='" + child+"' class='list-group-item list-group-item-action d-none'  target='_blank'>"+child+"</a>");
			            	}
			            	count++;
			            }
			            %>
			            </ul>
			        </div>
		        	<div class="card-footer text-muted">
		        		<%
		            		out.println("Date Last Modified: " + date + "  |  ");
		           			out.println("Size: " + size + "<br>");
		           		%>	
		            </div>
		       	</div>
		        <%

		            //out.println("------------------------------------------------------------------------------<br>");
				}
			} else {
				
				%>
				<div class = "card mx-auto mb-3 w-75">
					<h3 class="card-header">
						No Results!
		            </h3>
		            <div class = "card-body">
		            	<p class = "card-text">
		            	No page found for the query! <br> Try Searching Again with a different query!
		            	</p>
		            </div>
		        </div>
				<%
			}

			indexer.finalise();

		} else {
			%>
				<div class = "card mx-auto mb-3 w-75">
					<h3 class="card-header">
						No Input!
		            </h3>
		            <div class = "card-body">
		            	<p class = "card-text">
		            	No Input - Please try searching again!
		            	</p>
		            </div>
		        </div>
		<%
		}
		
		%>
		</div>
		</div>
<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.6/umd/popper.min.js" integrity="sha384-wHAiFfRlMFy6i5SRaxvfOCifBUQy1xHdJ/yoi7FRNXMRBu5WHdZYu1hA6ZOblgut" crossorigin="anonymous"></script>
<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.2.1/js/bootstrap.min.js" integrity="sha384-B0UglyR+jN6CkvvICOB2joaf5I4l3gm9GU6Hc1og6Ls7i6U/mkkaduKaBhlAXv9k" crossorigin="anonymous"></script>
	</body>
</html>