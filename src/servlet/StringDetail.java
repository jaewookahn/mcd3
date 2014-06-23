package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import presentapi.StringDetailJSON;

public class StringDetail extends HttpServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		   
		   request.setCharacterEncoding("utf-8");
		   response.setContentType("text/html");
		   response.setCharacterEncoding("utf-8");
		   int sid = Integer.parseInt(request.getParameter("sid"));
		 
		   JSONObject string = new JSONObject();
		   
		   StringDetailJSON sdj = new StringDetailJSON();
		   
		   try{
			   sdj.iniDB(sid);
			   sdj.getStringDetail();
			   sdj.endDB();		 		   
		   }
		   catch (Exception e){
		   }		
		   string = sdj.getStringElement();
		   
		   PrintWriter out = response.getWriter();
		   
		   
		   
		 out.println(string.toString());
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException
	{
		doGet(req, resp);
	}

}
