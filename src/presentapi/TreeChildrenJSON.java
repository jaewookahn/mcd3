package presentapi;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONObject;

import conn.Conn;

public class TreeChildrenJSON {
	Conn rs= new Conn();
	Conn rs1= new Conn();
	//JSONObject concept = new JSONObject();
	//int cid = -1;
	int pflableRID=-1;
	int mapRID=-1;
	int reltypeEID=-1;
	int conceptTypeID=-1;
	int showrootid = -1;
	
	
	int ntRID;
	int ntgRID;
	int ntpRID;
	int ntiRID;

	
	//open database
		public void iniDB(int rootid) throws Exception{
			
			//cid = queryid;
			showrootid = rootid;

			if(!rs.conn("mcd")){System.out.print("wrong connection");return;}	
			if(!rs1.conn("mcd")){System.out.print("wrong connection");return;}	
			
			reltypeEID = getEntityTypeID("RelType");
			
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
			
			conceptTypeID=getEntityID("hasConceptType",reltypeEID);
			
		}
		
		public int getEntityTypeID(String entitytypecode){
			rs.setRs("select * from EntityTypeListTable where EntityTypeCode='"+entitytypecode+"'");
			if(!rs.next())return -1;
			return rs.getInt("EntityTypeID");
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
		
		public String getRelatedEntitySQL(int entityID, int relTypeID){
			String sql = "select urt1.entityInstance2 as id, st.StringText from UniversalRelationshipTable as urt1";
			sql+= " left join UniversalRelationshipTable as urt2 on urt1.entityInstance2 = urt2.entityInstance1";
			sql+= " left join UniversalRelationshipTable as urt3 on urt3.entityInstance1 = urt2.entityInstance2";
			sql+= " left join StringTable as st on st.StringID=urt3.entityInstance2";
			sql+= " where urt1.entityInstance1="+entityID+" and urt1.RelTypeID="+relTypeID+" and urt2.RelTypeID="+pflableRID+" and urt3.RelTypeID="+mapRID;
			sql+=" order by st.StringText";
			return sql;
		}


		//close database
		public void endDB(){
			rs.close();
			rs.closeconn();
			rs1.close();
			rs1.closeconn();
		}
		
		//used for the root node
		public JSONArray getCurrentConcept(int conceptID)throws Exception{
			JSONArray term = new JSONArray();
			
			String sql = "select st.StringText from UniversalRelationshipTable as urt1";
			sql+= " left join UniversalRelationshipTable as urt2 on urt1.entityInstance2 = urt2.entityInstance1";
			sql+= " left join StringTable as st on st.StringID=urt2.entityInstance2";
			sql+= " where urt1.entityInstance1="+conceptID+" and urt1.RelTypeID="+pflableRID+" and urt2.RelTypeID="+mapRID;
			
			rs.setRs(sql);
			if(rs.next()){
				JSONObject perchild = new JSONObject();
				perchild.put("id",""+conceptID+"");
				String text = rs.get("StringText");
				
				sql = "select urt1.entityInstance2 as id, st.StringText from UniversalRelationshipTable as urt1";
				sql+= " left join UniversalRelationshipTable as urt2 on urt1.entityInstance2 = urt2.entityInstance1";
				sql+= " left join UniversalRelationshipTable as urt3 on urt3.entityInstance1 = urt2.entityInstance2";
				sql+= " left join StringTable as st on st.StringID=urt3.entityInstance2";
				sql+= " where urt1.entityInstance1="+conceptID+" and urt1.RelTypeID="+conceptTypeID+" and urt2.RelTypeID="+pflableRID+" and urt3.RelTypeID="+mapRID;
				rs1.setRs(sql);
				if(rs1.next())text+="["+rs1.get("StringText")+"]";	
				text="<a href=\"javascript:showConcept('"+text+"',"+conceptID+","+showrootid+")\">"+text+"</a>";
				perchild.put("text",text);
		
				perchild.put("hasChildren", true);

						
				term.put(perchild);
			}
					
			return term;
		}
		
		
		
		public JSONArray getPreferredChildren(int cid) throws Exception{
			
			JSONArray terms = new JSONArray();
			
			int childRID = getEntityID("hasChild",reltypeEID);
			rs.setRs(getRelatedEntitySQL(cid,childRID));			
			while(rs.next()){
				JSONObject perchild = new JSONObject();
				
				//the id of this child
				int conceptID= rs.getInt("id");
				perchild.put("id",""+conceptID+"");
				
				String text = rs.get("StringText");
				//search for the concpet type
				String sql = "select urt1.entityInstance2 as id, st.StringText from UniversalRelationshipTable as urt1";
				sql+= " left join UniversalRelationshipTable as urt2 on urt1.entityInstance2 = urt2.entityInstance1";
				sql+= " left join UniversalRelationshipTable as urt3 on urt3.entityInstance1 = urt2.entityInstance2";
				sql+= " left join StringTable as st on st.StringID=urt3.entityInstance2";
				sql+= " where urt1.entityInstance1="+conceptID+" and urt1.RelTypeID="+conceptTypeID+" and urt2.RelTypeID="+pflableRID+" and urt3.RelTypeID="+mapRID;
				rs1.setRs(sql);
				if(rs1.next())text+="["+rs1.get("StringText")+"]";	
				text="<a href=\"javascript:showConcept('"+text+"',"+conceptID+","+showrootid+")\">"+text+"</a>";
				perchild.put("text",text);
				
				//check if has children
				int count=rs1.count("UniversalRelationshipTable","entityInstance1="+conceptID+" and RelTypeID="+childRID);
				boolean haschild = false;
				if(count>0)haschild = true;			
				perchild.put("hasChildren", haschild);

						
				terms.put(perchild);
			}
			return terms;
		}
				
		
		
		public JSONArray getAllChildren(int cid)throws Exception{
			JSONArray terms = new JSONArray();
			

			int ntRID=getEntityID("narrower",reltypeEID);
			int ntgRID=getEntityID("hasNarrowerTermGeneric",reltypeEID);
			int ntpRID=getEntityID("hasNarrowerTermPartitive",reltypeEID);
			int ntiRID=getEntityID("hasNarrowerTermInstance",reltypeEID);
			
			
			
			rs.setRs(getRelatedEntitySQL(cid,ntRID));			
			while(rs.next()){
				JSONObject perchild = new JSONObject();
				
				//the id of this child
				int conceptID= rs.getInt("id");
				perchild.put("id",""+conceptID+"");
				
				String text = rs.get("StringText");
				//search for the concpet type
				String sql = "select urt1.entityInstance2 as id, st.StringText from UniversalRelationshipTable as urt1";
				sql+= " left join UniversalRelationshipTable as urt2 on urt1.entityInstance2 = urt2.entityInstance1";
				sql+= " left join UniversalRelationshipTable as urt3 on urt3.entityInstance1 = urt2.entityInstance2";
				sql+= " left join StringTable as st on st.StringID=urt3.entityInstance2";
				sql+= " where urt1.entityInstance1="+conceptID+" and urt1.RelTypeID="+conceptTypeID+" and urt2.RelTypeID="+pflableRID+" and urt3.RelTypeID="+mapRID;
				rs1.setRs(sql);
				if(rs1.next())text+="["+rs1.get("StringText")+"]";	
				text="<a href=\"javascript:showConcept('"+text+"',"+conceptID+","+showrootid+")\">"+text+"</a>";
				perchild.put("text",text);
				
				//check if has children
				int count=rs1.count("UniversalRelationshipTable","entityInstance1="+conceptID+" and RelTypeID in ("+ntRID+","+ntgRID+","+ntpRID+","+ntiRID+")");
				boolean haschild = false;
				if(count>0)haschild = true;			
				perchild.put("hasChildren", haschild);

						
				terms.put(perchild);
			}
			
			rs.setRs(getRelatedEntitySQL(cid,ntgRID));			
			while(rs.next()){
				JSONObject perchild = new JSONObject();
				
				//the id of this child
				int conceptID= rs.getInt("id");
				perchild.put("id",""+conceptID+"");
				
				String text = rs.get("StringText");
				//search for the concpet type
				String sql = "select urt1.entityInstance2 as id, st.StringText from UniversalRelationshipTable as urt1";
				sql+= " left join UniversalRelationshipTable as urt2 on urt1.entityInstance2 = urt2.entityInstance1";
				sql+= " left join UniversalRelationshipTable as urt3 on urt3.entityInstance1 = urt2.entityInstance2";
				sql+= " left join StringTable as st on st.StringID=urt3.entityInstance2";
				sql+= " where urt1.entityInstance1="+conceptID+" and urt1.RelTypeID="+conceptTypeID+" and urt2.RelTypeID="+pflableRID+" and urt3.RelTypeID="+mapRID;
				rs1.setRs(sql);
				if(rs1.next())text+="["+rs1.get("StringText")+"]";	
				text="<a href=\"javascript:showConcept('"+text+"',"+conceptID+","+showrootid+")\">"+text+"</a>";
				perchild.put("text",text);
				
				//check if has children
				int count=rs1.count("UniversalRelationshipTable","entityInstance1="+conceptID+" and RelTypeID in ("+ntRID+","+ntgRID+","+ntpRID+","+ntiRID+")");
				boolean haschild = false;
				if(count>0)haschild = true;			
				perchild.put("hasChildren", haschild);

						
				terms.put(perchild);
			}
			
			rs.setRs(getRelatedEntitySQL(cid,ntpRID));			
			while(rs.next()){
				JSONObject perchild = new JSONObject();
				
				//the id of this child
				int conceptID= rs.getInt("id");
				perchild.put("id",""+conceptID+"");
				
				String text = rs.get("StringText");
				//search for the concpet type
				String sql = "select urt1.entityInstance2 as id, st.StringText from UniversalRelationshipTable as urt1";
				sql+= " left join UniversalRelationshipTable as urt2 on urt1.entityInstance2 = urt2.entityInstance1";
				sql+= " left join UniversalRelationshipTable as urt3 on urt3.entityInstance1 = urt2.entityInstance2";
				sql+= " left join StringTable as st on st.StringID=urt3.entityInstance2";
				sql+= " where urt1.entityInstance1="+conceptID+" and urt1.RelTypeID="+conceptTypeID+" and urt2.RelTypeID="+pflableRID+" and urt3.RelTypeID="+mapRID;
				rs1.setRs(sql);
				if(rs1.next())text+="["+rs1.get("StringText")+"]";	
				text="<a href=\"javascript:showConcept('"+text+"',"+conceptID+","+showrootid+")\">"+text+"</a>";
				perchild.put("text",text);
				
				//check if has children
				int count=rs1.count("UniversalRelationshipTable","entityInstance1="+conceptID+" and RelTypeID in ("+ntRID+","+ntgRID+","+ntpRID+","+ntiRID+")");
				boolean haschild = false;
				if(count>0)haschild = true;			
				perchild.put("hasChildren", haschild);

						
				terms.put(perchild);
			}
			
			
			rs.setRs(getRelatedEntitySQL(cid,ntiRID));			
			while(rs.next()){
				JSONObject perchild = new JSONObject();
				
				//the id of this child
				int conceptID= rs.getInt("id");
				perchild.put("id",""+conceptID+"");
				
				String text = rs.get("StringText");
				//search for the concpet type
				String sql = "select urt1.entityInstance2 as id, st.StringText from UniversalRelationshipTable as urt1";
				sql+= " left join UniversalRelationshipTable as urt2 on urt1.entityInstance2 = urt2.entityInstance1";
				sql+= " left join UniversalRelationshipTable as urt3 on urt3.entityInstance1 = urt2.entityInstance2";
				sql+= " left join StringTable as st on st.StringID=urt3.entityInstance2";
				sql+= " where urt1.entityInstance1="+conceptID+" and urt1.RelTypeID="+conceptTypeID+" and urt2.RelTypeID="+pflableRID+" and urt3.RelTypeID="+mapRID;
				rs1.setRs(sql);
				if(rs1.next())text+="["+rs1.get("StringText")+"]";	
				text="<a href=\"javascript:showConcept('"+text+"',"+conceptID+","+showrootid+")\">"+text+"</a>";
				perchild.put("text",text);
				
				//check if has children
				int count=rs1.count("UniversalRelationshipTable","entityInstance1="+conceptID+" and RelTypeID in ("+ntRID+","+ntgRID+","+ntpRID+","+ntiRID+")");
				boolean haschild = false;
				if(count>0)haschild = true;			
				perchild.put("hasChildren", haschild);

						
				terms.put(perchild);
			}
			
			return terms;
		}
		
		
		public JSONArray getPreDecendants(int rootID)throws Exception{
			JSONArray terms = new JSONArray();
			JSONObject rootobject = recPreDecendants(getCurrentConcept(rootID).getJSONObject(0));
			terms.put(rootobject);			
			return terms;
		}
		
		public JSONObject recPreDecendants(JSONObject older)throws Exception{
			JSONObject newer = older;
			
			if(older.getBoolean("hasChildren")==true){				
				JSONArray children = getPreferredChildren(Integer.parseInt(newer.getString("id")));
				for(int i=0;i<children.length();i++)children.put(i, recPreDecendants(children.getJSONObject(i)));
				newer.put("children", children);
			}	
			newer.remove("hasChildren");
			return newer;
		}
		
		
		
		
		
		public static void main(String[] arg) {
			TreeChildrenJSON jas = new TreeChildrenJSON();
			String result = "";
			try{
				jas.iniDB(2580);
				//result = jas.getPreferredChildren(25530).toString();
				result = jas.getPreDecendants(2580).toString();
				jas.endDB();
			
			}
			catch (Exception e){}
			
			System.out.print(result+"\n");

		}
			
		

}
