package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import presentapi.ConceptSearch;

public class SearchString extends HttpServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		   
		   request.setCharacterEncoding("utf-8");
		   response.setContentType("text/html");
		   response.setCharacterEncoding("utf-8");
		   String query = request.getParameter("q");
		   String limit = request.getParameter("limit");
		 
		   JSONArray terms = new JSONArray();
		   
		   ConceptSearch cs = new ConceptSearch();
		   
		   try{
			  if(limit!=null)cs.iniDB(query, Integer.parseInt(limit));
			  else cs.iniDB(query);
			  terms = cs.searchAllTerm();
			  cs.endDB();		 		   
		   }
		   catch (Exception e){
		   }		
		   		                    
		   PrintWriter out = response.getWriter();
		   out.println(terms.toString());		
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException
	{
		doGet(req, resp);
	}

}

