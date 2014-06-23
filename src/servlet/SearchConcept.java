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

public class SearchConcept extends HttpServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		   
		   request.setCharacterEncoding("utf-8");
		   response.setContentType("text/html");
		   response.setCharacterEncoding("utf-8");
		   String query = request.getParameter("q");
		   String kos = request.getParameter("kos");
		   String limit = request.getParameter("limit");
		   String ancestor = request.getParameter("aid");
		 
		   JSONArray concepts = new JSONArray();
		   
		   ConceptSearch cs = new ConceptSearch();
		   
		   try{
			   if(kos!=null && limit!=null)cs.iniDB(query, Integer.parseInt(limit),kos);
			   else if(limit!=null)cs.iniDB(query, Integer.parseInt(limit));
			   else if(kos!=null) cs.iniDB(query,kos);
			   else cs.iniDB(query);
			   if(ancestor==null)concepts = cs.searchPTerm();
			   else concepts = cs.searchPTerm(Integer.parseInt(ancestor));
			   cs.endDB();		 		   
		   }
		   catch (Exception e){
		   }		
		   
		   
		   PrintWriter out = response.getWriter();
		   
		   StringBuffer content = new StringBuffer();
		   
				   
		   
		 out.println(concepts.toString());
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException
	{
		doGet(req, resp);
	}

}

