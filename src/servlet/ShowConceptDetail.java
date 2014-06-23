package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import presentapi.ConceptDetailJSON;

public class ShowConceptDetail extends HttpServlet {
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
			   cdj.endDB(); 
			   
		   }
		   catch (Exception e){
		   }		
		   concept = cdj.getConceptElement();
		   
		   PrintWriter out = response.getWriter();
		   
		   StringBuffer content = new StringBuffer();	   
		   
		   try{
			   //append preferred string
			   content.append("<div><a href = \"javascript:showVisual("+cid+")\">" +
			   		"<b>"+concept.getJSONObject("preferredstring").getString("string")+"</b></a>  (id:"+cid+")</div><br>");
			   
			   //append concept type
			   if(concept.has("conceptType"))
				   content.append("<div><font color=\"red\">Concept Type: </font>"+concept.getJSONObject("conceptType").getString("conceptTypeString")+"</div><br>");
			    
			   JSONArray array ;
			   int arraynumber ;
			  
			   //append notes
			   if(concept.has("notes")){
			    array = concept.getJSONArray("notes");
			    arraynumber = array.length();
			   for(int i=0;i<arraynumber;i++)
				   content.append("<div><font color=\"red\">Note: </font>"+array.getJSONObject(i).getString("noteText")+"</div><br>");
			   }
			   //append terms
			    array = concept.getJSONArray("terms");
			    arraynumber = array.length();			   
			    for(int i=0;i<arraynumber;i++){
					   JSONObject term = array.getJSONObject(i);
					   
					   JSONArray strings = term.getJSONArray("strings");
					   int n = strings.length();	
					   String stringconent = "";
					   for(int j=0;j<n;j++){
						   JSONObject string = strings.getJSONObject(j);
						   stringconent+="<a href = \"javascript:showString("+string.getInt("stringID")+")\">"+string.getString("string")+"</a> ("+string.getInt("stringID")+"), ";
					   }
					   
					   content.append("<div><font color=\"red\">Term_"+(i+1)+" ("+term.getInt("termID")+"): </font>"+stringconent+"</div><br>");
					   
				   }
			   
			   //append associations
			   if(concept.has("associations")){
			   array = concept.getJSONArray("associations");
			   arraynumber = array.length();	
			   for(int i=0;i<arraynumber;i++){
				   JSONObject association = array.getJSONObject(i);
				   String associationString = association.getString("associationString");
				   JSONArray concepts = association.getJSONArray("associatedConcepts");
				   int cn = concepts.length();
				   for(int j=0;j<cn;j++){
					   JSONObject thisconcept=concepts.getJSONObject(j);
					   content.append("<div><font color=\"red\">Association_"+(i+1)+": </font>"+associationString+" <a href = \"javascript:showConcept("+thisconcept.getInt("conceptID")+")\"><b>"+thisconcept.getString("preferredstring")+"</b></a> (id: "+thisconcept.getInt("iconceptID")+")</div><br>");
					   
					   
				   }
	
				   
			   }
			   }
				   			   
		   }
		   catch (Exception e){
		   }		
		   out.println(content.toString());
		   
		  
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException
	{
		doGet(req, resp);
	}


}
