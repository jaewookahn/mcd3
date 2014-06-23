package conn;

import java.util.HashSet;

import org.json.JSONObject;

/*
 * import the hearichyTable, for each concept, store all the parents of all levels and the first level of its children
 */

public class HierarchTable {
	Conn rs= new Conn();
	Conn rs1= new Conn();
	Conn rs2 = new Conn();


	int pflableRID=-1;
	int mapRID=-1;
	int reltypeEID=-1;
	int conceptEID=-1;
	
	int parentRID = -1;
	int childRID = -1;
	


	
	
	public void iniDB() throws Exception{


		if(!rs.conn("mcd")){System.out.print("wrong connection");return;}	
		if(!rs1.conn("mcd")){System.out.print("wrong connection");return;}	
		if(!rs2.conn("mcd")){System.out.print("wrong connection");return;}	
		
		reltypeEID = getEntityTypeID("RelType");
		conceptEID = getEntityTypeID("concept");
		
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
		
		parentRID = getEntityID("hasParent",reltypeEID);
		childRID = getEntityID("hasChild",reltypeEID);
		
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
	
	//get the whole hierarchy of a concept with a an ID
	public JSONObject getHierachy(int cid) throws Exception{
		JSONObject concept  = new JSONObject();
		concept.put("conceptID", cid);
		concept.put("self", "self");
		
		//get the current concept and its children
		//get preferred string
		String sql = "select st.StringID, st.StringText from UniversalRelationshipTable as urt1";
		sql+= " left join UniversalRelationshipTable as urt2 on urt1.entityInstance2 = urt2.entityInstance1";
		sql+= " left join StringTable as st on urt2.entityInstance2 = st.StringID";
		sql+= " where urt1.entityInstance1="+cid+" and urt1.RelTypeID="+pflableRID+" and urt2.RelTypeID="+mapRID;
		rs.setRs(sql);		
		if(rs.next())concept.put("preferredString", rs.get("StringText"));	
		
		//get the children
		
		rs.setRs(getRelatedEntitySQL(cid,childRID));			
		while(rs.next()){
			JSONObject stringobject = new JSONObject();			
			stringobject.put("conceptID", rs.getInt("id"));
			stringobject.put("preferredString", rs.get("StringText"));			
			concept.append("children", stringobject);
		}
		
		//get the preferred parent up to the top
		concept = recParent(concept);
		
		return concept;		
	}
	
	public JSONObject recParent(JSONObject concept)throws Exception{
		
		int conceptid = concept.getInt("conceptID");
		JSONObject newconcept = new JSONObject();
		boolean hasP = false;
		//get the parent
		rs.setRs(getRelatedEntitySQL(conceptid,parentRID));			
		if(rs.next()){
			int id = rs.getInt("id");
			if(id==conceptid)return concept;
			hasP=true;
			newconcept.put("conceptID", id);
			newconcept.put("preferredString", rs.get("StringText"));
			newconcept.put("child", concept);
			
		}
		if(hasP)return recParent(newconcept);		
		else return concept;
	}
	
	public void updateTable()throws Exception{
		rs1.setRs("*", "UniversalEntityTable as uet left join UniversalSourceTable as ust on uet.EntityInstanceID=ust.EntityInstanceID", "uet.EntityTypeID="+conceptEID+" and uet.EntityInstanceID>19247858 and ust.KOSID<18 order by uet.EntityInstanceID");
		System.out.print("begin");
		while(rs1.next()){
			int thisid = rs1.getInt("EntityInstanceID");
			JSONObject thisconcept = getHierachy(thisid);
			rs2.insert("ConceptHierarchy", "("+thisid+"),'"+thisconcept.toString().replaceAll("\"", "\\\"")+"'");
		}
	}
	
	public void endDB(){
		rs.close();
		rs.closeconn();
		rs1.close();
		rs1.closeconn();
		rs2.close();
		rs2.closeconn();
	}
	
	public static void main(String[] arg) throws Exception{
		HierarchTable ht = new HierarchTable();
		ht.iniDB();
		//System.out.print(ht.getHierachy(2651).toString());
		ht.updateTable();
		ht.endDB();
	}
	
	

}
