package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import conn.Conn;

public class GetConceptID extends HttpServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		   
		   request.setCharacterEncoding("utf-8");
		   response.setContentType("text/html");
		   response.setCharacterEncoding("utf-8");
		   String originalID = request.getParameter("oid");
		   String kos = request.getParameter("kos");
		   
		   int conceptid = -1;
		   int kosid=-1;
		   
		   Conn rs = new Conn();
		   if(!rs.conn("mcd")){System.out.print("wrong connection");return;}
		   
		   rs.setRs("*", "KOSTable where KOSAcronym='"+kos+"'");
		   if(!rs.next()){System.out.print("the KOS of '"+kos+"' is not found in the database.");}
		   else kosid = rs.getInt("KOSID");
		   System.out.print(kosid);
		   rs.setRs("*", "ExternalIDTable", "ExternalID="+"'"+originalID+"' and KOSID="+kosid);
		   if(!rs.next()){System.out.print("not found");}
		   else conceptid = rs.getInt("ExternalIDID");		   
		   rs.close();
		   
		   PrintWriter out = response.getWriter();
		   out.println(conceptid);	   
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException
	{
		doGet(req, resp);
	}


}
