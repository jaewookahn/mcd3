package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;

import presentapi.ConceptDetailJSON;

public class GetConceptDecendants extends HttpServlet{
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		   
		   request.setCharacterEncoding("utf-8");
		   response.setContentType("text/html");
		   response.setCharacterEncoding("utf-8");
		   int cid = Integer.parseInt(request.getParameter("cid"));
		 
		   JSONArray d = new JSONArray();
		   
		   ConceptDetailJSON cdj = new ConceptDetailJSON();
		   
		   try{
			   cdj.iniDB(cid);
			   d = cdj.getDecendants();
			  
				cdj.endDB(); 
			   
		   }
		   catch (Exception e){
		   }		
		
		   
		   PrintWriter out = response.getWriter();
		    out.println(d.toString());
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException
	{
		doGet(req, resp);
	}


}
