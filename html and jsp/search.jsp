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
		
		<%@page import="lib.*"%>
		<%@page import="SE.*"%>
		


		<% Indexer indexer; %>

		<%
		if(request.getParameter("search_terms")!=null) {
			indexer = new Indexer();			
			out.println("<h2> Search Results for query: <b>"+ request.getParameter("search_terms") + "</b></h2>");	
			out.println("Inputed <br>");
			indexer.printAll(); //TO TEST that indexer can pull from database file - will replace with stuff to print out
			StringTokenizer st = new StringTokenizer(request.getParameter("search_terms"));
			Vector<String> key = new Vector<String>();
			while(st.hasMoreTokens()) {
				//out.println(st.nextToken() + "<br>");
				key.add(st.nextToken());
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