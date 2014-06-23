package presentapi;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import conn.Conn;
import org.json.JSONArray;
import org.json.JSONObject;

public class ConceptSearch {
	
	class ScoredString{
		public int stringID;
		public String stringText;
		public double score;
		
		public ScoredString(int id, String text, double sc){
			this.stringID=id;
			this.stringText=text;
			this.score=sc;
		}
	}
	
	class ScoredTerm{
		public int termID;
		public LinkedList <ScoredString> strings;
		public double score;
		
		public ScoredTerm(int id){
			this.termID=id;
			this.strings = new LinkedList();
			this.score=0;
		}
		
		public void addString(ScoredString ss){
			strings.add(ss);
			if(ss.score>this.score)this.score=ss.score;
		}
		
		public void sortString(){
			Collections.sort(strings,new Comparator<ScoredString>(){
				public int compare(ScoredString ss1, ScoredString ss2){
					return ss1.score>=ss2.score?1:0;
				}
			});
		}
		
	}

	Conn rs= new Conn();
	Conn rs1 = new Conn();
	Conn rs2 = new Conn();
	
	int limit = 0;	
	String query = "";
	int kos = -1;
	int pflableRID=-1;
	int mapRID=-1;
	int reltypeEID = -1;
	int conceptEID=-1;
	
	
	int btRID=-1;
	int btgRID=-1;
	int btpRID=-1;
	int btiRID=-1;
	
	//open database
	public void iniDB(String q)throws Exception{
		query=q;
		openDB();	
	}
	
	//open database
	public void iniDB(String q, int lim) throws Exception{
		query = q;
		limit = lim;
		openDB();	

	}
	
	//open database
	public void iniDB(String q, int lim, String source) throws Exception{
		query = q;
		limit = lim;		
		openDB();		
		rs.setRs("*", "KOSTable where KOSAcronym='"+source+"'");
		if(!rs.next()){System.out.print("the KOS of '"+source+"' is not found in the database.");return;}
		kos = rs.getInt("KOSID");
		
	}
	
	//open database
	public void iniDB(String q,  String source) throws Exception{
		query = q;
		limit = 0;
		openDB();		
		rs.setRs("*", "KOSTable where KOSAcronym='"+source+"'");
		if(!rs.next()){System.out.print("the KOS of '"+source+"' is not found in the database.");return;}
		kos = rs.getInt("KOSID");
		
	}
	
	public void openDB(){
		if(!rs.conn("mcd")){System.out.print("wrong connection");return;}
		if(!rs1.conn("mcd")){System.out.print("wrong connection");return;}
		reltypeEID = getEntityTypeID("RelType");
		conceptEID= getEntityTypeID("Concept");
		
		//get the reltype id of preferred lable
		String sql = "select urt2.entityInstance1 as id from StringTable as st";
		sql+= " left join UniversalRelationshipTable as urt1 on st.StringID = urt1.entityInstance2";
		sql+= " left join UniversalRelationshipTable as urt2 on urt1.entityInstance1 = urt2.entityInstance2";
		sql+= " where st.StringText='prefLabel'";
		rs.setRs(sql);
		if(!rs.next())System.out.print("the relationtype of 'prefLabel' is not built in database");
		pflableRID=rs.getInt("id");
		
		//get the reltype id of manifestedas
		sql = "select urt2.entityInstance1 as id from StringTable as st";
		sql+= " left join UniversalRelationshipTable as urt1 on st.StringID = urt1.entityInstance2";
		sql+= " left join UniversalRelationshipTable as urt2 on urt1.entityInstance1 = urt2.entityInstance2";
		sql+= " where st.StringText='manifestedAsPreferred'";
		rs.setRs(sql);
		if(!rs.next())System.out.print("the relationtype of 'manifestedAsPreferred' is not built in database");
		mapRID=rs.getInt("id");		
	}
	
	
	public int getEntityID(String text, int EntityTypeID){
		String sql = "select urt2.entityInstance1 as id, urt1.RelTypeID as rt1, urt2.RelTypeID as rt2 from StringTable as st";
		sql+= " left join UniversalRelationshipTable as urt1 on st.StringID = urt1.entityInstance2";
		sql+= " left join UniversalRelationshipTable as urt2 on urt1.entityInstance1 = urt2.entityInstance2";
		sql+= " where st.StringText='"+text+"'";
		rs.setRs(sql);
		while(rs.next()){
			if(rs.getInt("rt1")!=mapRID || rs.getInt("rt2")!=pflableRID)continue;
			rs1.setRs("select EntityTypeID from UniversalEntityTable where EntityInstanceID="+rs.getInt("id"));
			if(rs1.next() && rs1.getInt("EntityTypeID")==EntityTypeID)return rs.getInt("id");		
		}
		return -1;		
	}
	
	public int getEntityTypeID(String entitytypecode){
		rs.setRs("select * from EntityTypeListTable where EntityTypeCode='"+entitytypecode+"'");
		if(!rs.next())return -1;
		return rs.getInt("EntityTypeID");
	}
	
	public int getRelTypeID(String reltype){
		String sql = "select urt2.entityInstance1 as id, urt1.RelTypeID as rtid1,  urt2.RelTypeID as rtid2 from StringTable as st";
		sql+= " left join UniversalRelationshipTable as urt1 on st.StringID = urt1.entityInstance2";
		sql+= " left join UniversalRelationshipTable as urt2 on urt1.entityInstance1 = urt2.entityInstance2";
		sql+= " where st.StringText='"+reltype+"'";
		rs.setRs(sql);
		while(rs.next()){
			if(rs.getInt("rtid1")!=mapRID || rs.getInt("rtid2")!=pflableRID)continue;
			rs1.setRs("select EntityTypeID from UniversalEntityTable where EntityInstanceID="+rs.getInt("id"));
			if(rs1.next() && rs1.getInt("EntityTypeID")==reltypeEID)return rs.getInt("id");		
		}
		return -1;		
	}
	
	//close database
	public void endDB(){
		rs.close();
		rs.closeconn();
		try{
		rs1.close();
		rs1.closeconn();}
		catch (Exception e){}; 
	}
	
	//search in the preferred string, return concepts
	public JSONArray searchPTerm() throws Exception{
		
	
		String sql = "select urt2.entityInstance1 as id, st.StringText, MATCH (st.StringText) AGAINST ('"+query+"') as score from StringTable as st";
		sql+= " left join UniversalRelationshipTable as urt1 on st.StringID = urt1.entityInstance2";
		sql+= " left join UniversalRelationshipTable as urt2 on urt1.entityInstance1 = urt2.entityInstance2";
		sql+= " where MATCH (st.StringText) AGAINST ('"+query+"') and urt1.RelTypeID="+mapRID+" and urt2.RelTypeID="+pflableRID;
			
		sql+=" order by score desc";
		if(limit>0)sql+=" limit 0,"+limit;
		
		rs.setRs(sql);	
		
		JSONArray result =  new JSONArray();		
		
		rs.setRs(sql);
		while(rs.next()){
			if(kos<0)rs1.setRs("select EntityTypeID from UniversalEntityTable where EntityInstanceID="+rs.getInt("id"));
			else rs1.setRs("select EntityTypeID from UniversalEntityTable left join UniversalSourceTable on UniversalEntityTable.EntityInstanceID=UniversalSourceTable.EntityInstanceID  where UniversalEntityTable.EntityInstanceID="+rs.getInt("id")+" and UniversalSourceTable.KOSID="+kos);
			if(rs1.next() && rs1.getInt("EntityTypeID")==conceptEID){
				JSONObject oneresult = new JSONObject();
				oneresult.put("id", rs.getInt("id"));
				oneresult.put("preferredstring", rs.get("StringText"));
				oneresult.put("score", rs.get("score"));	
				result.put(oneresult);
			}	
		}

		return result;	
	}
	
	//search in the preferred string, return concepts grouped by Facet
		public JSONArray searchGroupedPTerm() throws Exception{
			
			int inFacet = getRelTypeID("inFacet");		
			System.out.print(inFacet+"\n");
		
			/*String sql = "select urt2.entityInstance1 as id, urt3.entityInstance2 as facetID, st.StringText, MATCH (st.StringText) AGAINST ('"+query+"') as score from StringTable as st";
			sql+= " left join UniversalRelationshipTable as urt1 on st.StringID = urt1.entityInstance2";
			sql+= " left join UniversalRelationshipTable as urt2 on urt1.entityInstance1 = urt2.entityInstance2";
			sql+= " left join UniversalRelationshipTable as urt3 on urt2.entityInstance1 = urt3.entityInstance1";
			sql+= " where MATCH (st.StringText) AGAINST ('"+query+"') and urt1.RelTypeID="+mapRID+" and urt2.RelTypeID="+pflableRID+" and urt3.RelTypeID="+inFacet;
		*/		
			
			String where = "MATCH (st.StringText) AGAINST ('"+query+"')";
			if(query.length()<4)where = " st.StringText like '%"+query+"%'";
			
			String sql = "select urt2.entityInstance1 as id, urt3.entityInstance2 as facetID, st.StringText";
			if(query.length()<4)sql+=", MATCH (st.StringText) AGAINST ('"+query+"') as score";
			sql+= " from StringTable as st left join UniversalRelationshipTable as urt1 on st.StringID = urt1.entityInstance2";
			sql+= " left join UniversalRelationshipTable as urt2 on urt1.entityInstance1 = urt2.entityInstance2";
			sql+= " left join UniversalRelationshipTable as urt3 on urt2.entityInstance1 = urt3.entityInstance1";
			sql+= " where "+where+" and urt1.RelTypeID="+mapRID+" and urt2.RelTypeID="+pflableRID+" and urt3.RelTypeID="+inFacet;	
			
			
			if(query.length()<4)sql+=" order by score desc";
			if(limit>0)sql+=" limit 0,"+limit;
			
			rs.setRs(sql);	
		
		    HashMap <Integer,JSONObject> facetresults = new HashMap();
		    HashSet <Integer> removedfacet = new HashSet();
		    while(rs.next()){
		    	int fid=rs.getInt("facetID");
		    	//not in the specific KOS
		    	if(removedfacet.contains(fid))continue;
		    	
		    	JSONObject onefacet = null;
		    	
		    	//not have this facet in the result
		    	if(!facetresults.containsKey(fid)){
		    		//get the kos of this facet
			    	int kosid = -1;
			    	String kosstring = "";
			    	rs1.setRs("select UniversalSourceTable.KOSID as kosid, KOSTable.KOSAcronym as string from UniversalSourceTable left join KOSTable on UniversalSourceTable.KOSID=KOSTable.KOSID where UniversalSourceTable.EntityInstanceID="+fid);
			    	if(rs1.next()){kosid = rs1.getInt("kosid");kosstring = rs1.get("string");}
			    	if(kos>=0 && kosid!=kos){removedfacet.add(fid);continue;}
			    	if(kosid==-1)continue;
			    	
			    	//add this facet to the result
			    	onefacet = new JSONObject();
			    	onefacet.put("kosID", kosid);
			    	onefacet.put("kosString", kosstring);
			    	onefacet.put("facetID", fid);	
			    	//get the name of the facet
			    	rs1.setRs("select st.StringText from UniversalRelationshipTable as urt1 left join UniversalRelationshipTable as urt2 on urt1.entityInstance2 = urt2.entityInstance1" +
							" left join StringTable as st on st.StringID = urt2.entityInstance2" +
							" where urt1.entityInstance1="+fid+" and urt1.RelTypeID="+pflableRID+" and urt2.RelTypeID="+mapRID);
					if(rs1.next())onefacet.put("facetString", rs1.get("StringText"));
		    	}
		    	
		    	else onefacet = facetresults.get(fid);
		    	
		    	//add the concept
		    	rs1.setRs("select EntityTypeID from UniversalEntityTable where EntityInstanceID="+rs.getInt("id"));
		    	if(rs1.next() && rs1.getInt("EntityTypeID")==conceptEID){
					JSONObject oneresult = new JSONObject();
					oneresult.put("id", rs.getInt("id"));
					oneresult.put("preferredstring", rs.get("StringText"));
					onefacet.append("concepts", oneresult);					
				}
		    	facetresults.put(fid, onefacet);  
		    }  
		  
		    
		    JSONArray result = new JSONArray();
		    Iterator it = facetresults.keySet().iterator();
		    while(it.hasNext())
		    	result.put(facetresults.get(it.next()));
		    return result;	
		}
	
		//search in the preferred string, return concepts grouped by Facet
				public JSONArray searchGroupedAllTerm() throws Exception{
					
					int altlableRID=getRelTypeID("altLabel");
					int manpRID=getRelTypeID("manifestedAsNP");						
					int inFacet = getRelTypeID("inFacet");		
	
					
					String where = "MATCH (st.StringText) AGAINST ('"+query+"')";
					if(query.length()<4)where = " st.StringText like '%"+query+"%'";
					
					
					String sql = "select urt2.entityInstance1 as id, urt3.entityInstance2 as facetID, st.StringText";
					if(query.length()<4)sql+=", MATCH (st.StringText) AGAINST ('"+query+"') as score";
					sql+= " from StringTable as st left join UniversalRelationshipTable as urt1 on st.StringID = urt1.entityInstance2";
					sql+= " left join UniversalRelationshipTable as urt2 on urt1.entityInstance1 = urt2.entityInstance2";
					sql+= " left join UniversalRelationshipTable as urt3 on urt2.entityInstance1 = urt3.entityInstance1";
					sql+= " where "+where+" and urt1.RelTypeID in ("+mapRID+","+manpRID+") and urt2.RelTypeID in ("+pflableRID+","+altlableRID+") and urt3.RelTypeID="+inFacet;
								
					/*String sql = "select urt2.entityInstance1 as id, urt3.entityInstance2 as facetID, st.StringText";
					if(query.length()<4)sql+=", MATCH (st.StringText) AGAINST ('"+query+"') as score";
					sql+= " from StringTable as st left join UniversalRelationshipTable as urt1 on st.StringID = urt1.entityInstance2";
					sql+= " left join UniversalRelationshipTable as urt2 on urt1.entityInstance1 = urt2.entityInstance2";
					sql+= " left join UniversalRelationshipTable as urt3 on urt2.entityInstance1 = urt3.entityInstance1";
					sql+= " where "+where+" and urt1.RelTypeID="+mapRID+" and urt2.RelTypeID="+pflableRID+" and urt3.RelTypeID="+inFacet;	*/
										
					if(query.length()<4)sql+=" order by score desc";
					if(limit>0)sql+=" limit 0,"+limit;

					rs.setRs(sql);	
				
				    HashMap <Integer,JSONObject> facetresults = new HashMap();
				    HashSet <Integer> removedfacet = new HashSet();
				    HashSet <Integer> processedID = new HashSet();
				    while(rs.next()){
				    	int fid=rs.getInt("facetID");
				    	int conceptid = rs.getInt("id");
				    	//not in the specific KOS
				    	if(removedfacet.contains(fid))continue;
				    	if(processedID.contains(conceptid))continue;
				    	processedID.add(conceptid);
				    	
				    	JSONObject onefacet = null;
				    	
				    	//not have this facet in the result
				    	if(!facetresults.containsKey(fid)){
				    		//get the kos of this facet
					    	int kosid = -1;
					    	String kosstring = "";
					    	rs1.setRs("select UniversalSourceTable.KOSID as kosid, KOSTable.KOSAcronym as string from UniversalSourceTable left join KOSTable on UniversalSourceTable.KOSID=KOSTable.KOSID where UniversalSourceTable.EntityInstanceID="+fid);
					    	if(rs1.next()){kosid = rs1.getInt("kosid");kosstring = rs1.get("string");}
					    	if(kos>=0 && kosid!=kos){removedfacet.add(fid);continue;}
					    	if(kosid==-1)continue;
					    	
					    	//add this facet to the result
					    	onefacet = new JSONObject();
					    	onefacet.put("kosID", kosid);
					    	onefacet.put("kosString", kosstring);
					    	onefacet.put("facetID", fid);	
					    	//get the name of the facet
					    	rs1.setRs("select st.StringText from UniversalRelationshipTable as urt1 left join UniversalRelationshipTable as urt2 on urt1.entityInstance2 = urt2.entityInstance1" +
									" left join StringTable as st on st.StringID = urt2.entityInstance2" +
									" where urt1.entityInstance1="+fid+" and urt1.RelTypeID="+pflableRID+" and urt2.RelTypeID="+mapRID);
							if(rs1.next())onefacet.put("facetString", rs1.get("StringText"));
				    	}
				    	
				    	else onefacet = facetresults.get(fid);
				    	
				    	//add the concept
				    	rs1.setRs("select EntityTypeID from UniversalEntityTable where EntityInstanceID="+rs.getInt("id"));
				    	if(rs1.next() && rs1.getInt("EntityTypeID")==conceptEID){
							JSONObject oneresult = new JSONObject();
							oneresult.put("id", rs.getInt("id"));
							oneresult.put("preferredstring", rs.get("StringText"));
							onefacet.append("concepts", oneresult);					
						}
				    	facetresults.put(fid, onefacet);  
				    }  
				  
				    
				    JSONArray result = new JSONArray();
				    Iterator it = facetresults.keySet().iterator();
				    while(it.hasNext())
				    	result.put(facetresults.get(it.next()));
				    return result;	
				}
	
	//search in the preferred string, return concepts under certain ancestor
	public JSONArray searchPTerm(int ancestorID) throws Exception{		
		
		btRID=getEntityID("broader",reltypeEID);
		btgRID=getEntityID("hasBroaderTermGeneric",reltypeEID);
		btpRID=getEntityID("hasBroaderTermPartitive",reltypeEID);
		btiRID=getEntityID("hasBroaderTermInstance",reltypeEID);	
	    rs2 = new Conn();
	    if(!rs2.conn("mcd")){System.out.print("wrong connection");return null;}
		
		
			String sql = "select urt2.entityInstance1 as id, st.StringText, MATCH (st.StringText) AGAINST ('"+query+"') as score from StringTable as st";
			sql+= " left join UniversalRelationshipTable as urt1 on st.StringID = urt1.entityInstance2";
			sql+= " left join UniversalRelationshipTable as urt2 on urt1.entityInstance1 = urt2.entityInstance2";
			sql+= " where MATCH (st.StringText) AGAINST ('"+query+"') and urt1.RelTypeID="+mapRID+" and urt2.RelTypeID="+pflableRID;
				
			sql+=" order by score desc";
			if(limit>0)sql+=" limit 0,"+limit;
			
			rs.setRs(sql);	
			
			JSONArray result =  new JSONArray();		
			
			rs.setRs(sql);
			while(rs.next()){
				if(kos<0)rs1.setRs("select EntityTypeID from UniversalEntityTable where EntityInstanceID="+rs.getInt("id"));
				else rs1.setRs("select EntityTypeID from UniversalEntityTable left join UniversalSourceTable on UniversalEntityTable.EntityInstanceID=UniversalSourceTable.EntityInstanceID  where UniversalEntityTable.EntityInstanceID="+rs.getInt("id")+" and UniversalSourceTable.KOSID="+kos);
				if(rs1.next() && rs1.getInt("EntityTypeID")==conceptEID){
					LinkedList <Integer> decendantIDs = new LinkedList();
					decendantIDs.add(rs.getInt("id"));
					if(!hasAncestor(decendantIDs,ancestorID))continue;
					JSONObject oneresult = new JSONObject();
					oneresult.put("id", rs.getInt("id"));
					oneresult.put("preferredstring", rs.get("StringText"));
					oneresult.put("score", rs.get("score"));	
					result.put(oneresult);
				}	
			}
			
			rs2.closeall();

			return result;	
		}
	
	public boolean hasAncestor(LinkedList <Integer> decendantIDs, int ancestorID){
		
		if(decendantIDs==null || decendantIDs.size()==0)return false;
		LinkedList <Integer>  newlist = new LinkedList();
		
		String in = "("+btRID+","+btgRID+","+btpRID+","+btiRID+")";
		
		for(int i=0;i<decendantIDs.size();i++){
			String sql = "select * from UniversalRelationshipTable where entityInstance1="+decendantIDs.get(i)+" and RelTypeID in "+in;
			rs2.setRs(sql);
			while(rs2.next()){
				int curid = rs2.getInt("entityInstance2");
				if(curid==ancestorID)return true;
				if(curid!=decendantIDs.get(i))newlist.add(curid);
			}
		}
		return hasAncestor(newlist,ancestorID);		
	
	}
	
	
	
	//search in string, return terms
	public JSONArray searchAllTerm() throws Exception{
		//String sql = "select termID, MATCH (string) AGAINST ('"+query+"') as score FROM string where MATCH (string) AGAINST ('"+query+"') group by termID order by score desc";
		
		int altlableRID=getRelTypeID("altLabel");
		int manpRID=getRelTypeID("manifestedAsNP");		
		
		String sql = "select urt2.entityInstance1 as conceptid, urt2.entityInstance2 as termid, st.StringID, st.StringText, MATCH (st.StringText) AGAINST ('"+query+"') as score from StringTable as st";
		sql+= " left join UniversalRelationshipTable as urt1 on st.StringID = urt1.entityInstance2";
		sql+= " left join UniversalRelationshipTable as urt2 on urt1.entityInstance1 = urt2.entityInstance2";
		sql+= " where MATCH (st.StringText) AGAINST ('"+query+"') and urt1.RelTypeID in ("+mapRID+","+manpRID+") and urt2.RelTypeID in ("+pflableRID+","+altlableRID+")";
		sql+= " order by score desc";	
		
		if(limit>0)sql+=" limit 0,"+limit;		
		
		rs.setRs(sql);
		
		LinkedList <ScoredTerm> terms = new LinkedList();
		while(rs.next()){
			ScoredString ss = new ScoredString(rs.getInt("StringID"),rs.get("StringText"),Double.parseDouble(rs.get("score")));
			int termID = rs.getInt("termid");
			int i=0;
			for(i=0;i<terms.size();i++){
				ScoredTerm st = terms.get(i);
				st.addString(ss);
				terms.set(i, st);
			}
			if(i==terms.size()){
				ScoredTerm st = new ScoredTerm(termID);
				st.addString(ss);
				terms.add(st);
			}
		}
		
		Collections.sort(terms,new Comparator<ScoredTerm>(){
			public int compare(ScoredTerm st1, ScoredTerm st2){
				return st1.score>=st2.score?1:0;
			}
		});
		
		
		JSONArray results = new JSONArray();
		
		for(int i=0;i<terms.size();i++){
			ScoredTerm st = terms.get(i);
			st.sortString();
			JSONObject oneterm = new JSONObject();
			oneterm.put("termID", st.termID);			
			
			for(int j=0;j<st.strings.size();j++){
				ScoredString ss = st.strings.get(j);
				JSONObject onestring = new JSONObject();
				onestring.put("stringID", ss.stringID);
				onestring.put("string", ss.stringText);
				onestring.put("score", ss.score);
				oneterm.append("strings", onestring);					
			}
			results.put(oneterm);
		}
		
		return results;	
	}
		
	public static void main(String[] arg) throws Exception{
		ConceptSearch cs = new ConceptSearch();
		
		cs.iniDB("paint");
		//JSONArray terms = cs.searchAllTerm();
		JSONArray concepts = cs.searchGroupedPTerm();
		//JSONArray concepts = cs.searchPTerm(2580);
		cs.endDB();
		/*
		System.out.print(cs.pflableRID+"\n");
		System.out.print(cs.mapRID+"\n");
		System.out.print(cs.conceptEID+"\n");
		System.out.print(cs.reltypeEID+"\n");
		System.out.print(cs.kos+"\n");*/
		System.out.print(concepts.toString());
	}

}
