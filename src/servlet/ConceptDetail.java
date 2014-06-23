package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.json.JSONArray;

import presentapi.ConceptDetailJSON;

public class ConceptDetail extends HttpServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		   
		   request.setCharacterEncoding("utf-8");
		   response.setContentType("text/html");
		   response.setCharacterEncoding("utf-8");
		   int cid = Integer.parseInt(request.getParameter("cid"));
		 
		   JSONObject concept = new JSONObject();
		   
		   ConceptDetailJSON cdj = new ConceptDetailJSON();
		   
		   try{
			   cdj.iniDB(cid);
			   cdj.getConcept();
			   cdj.getTerms();
			   cdj.getNote();
			   cdj.getAssociation();
			   
			   
				cdj.getAllChildren();
				cdj.getPreferredChildren();
				cdj.getPreferredParent();
				cdj.getAllParents();
				
				cdj.endDB(); 
			   
		   }
		   catch (Exception e){
		   }		
		   concept = cdj.getConceptElement();
		   
		   PrintWriter out = response.getWriter();
		   
		   StringBuffer content = new StringBuffer();	   
		   
		   
		    out.println(concept.toString());
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException
	{
		doGet(req, resp);
	}

}
