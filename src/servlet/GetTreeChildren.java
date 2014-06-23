package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;

import presentapi.TreeChildrenJSON;

public class GetTreeChildren extends HttpServlet{
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		request.setCharacterEncoding("utf-8");
		   response.setContentType("text/html");
		   response.setCharacterEncoding("utf-8");
		   int cid = Integer.parseInt(request.getParameter("cid"));
		   int rootid = cid;
		   JSONArray children = new JSONArray();

		   boolean isRoot = false;
		   try{cid = Integer.parseInt(request.getParameter("root"));}
		   catch (Exception e){isRoot=true;}
		   
		   boolean getAll = false;
		   if(request.getParameter("data")!=null && request.getParameter("data").equals("all"))getAll=true;

		   TreeChildrenJSON jas = new TreeChildrenJSON();
		   try{
			   jas.iniDB(rootid);
			   if(getAll)children=jas.getPreDecendants(rootid);
			   else{
				   if(isRoot)children = jas.getCurrentConcept(cid);
				   else
				      children = jas.getPreferredChildren(cid);
				      //children = jas.getAllChildren(cid);
			   }
			   
			   jas.endDB();			   
		   }
		   catch (Exception e){
			   
		   }
		   		   
		   
		PrintWriter out = response.getWriter();
		out.println(children.toString());
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException
	{
		doGet(req, resp);
	}	

}
