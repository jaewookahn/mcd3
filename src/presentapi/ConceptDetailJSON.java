package presentapi;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONObject;


import conn.Conn;


public class ConceptDetailJSON {
	
	Conn rs= new Conn();
	Conn rs1= new Conn();
	JSONObject concept = new JSONObject();
	int cid = -1;
	int pflableRID=-1;
	int mapRID=-1;
	int reltypeEID=-1;
	
	HashSet <Integer> decendantsID;
	int ntRID;
	int ntgRID;
	int ntpRID;
	int ntiRID;
	
	//open database
		public void iniDB(int queryid) throws Exception{
			
			cid = queryid;

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
		

		public JSONObject getConceptElement() {
			return concept;
		}

		//close database
		public void endDB(){
			rs.close();
			rs.closeconn();
			rs1.close();
			rs1.closeconn();
		}
		
		//get basic info
		public void getConcept()throws Exception{
			concept.put("id", cid);
			
			//get KOS
			rs.setRs("*", "UniversalSourceTable left join KOSTable on UniversalSourceTable.KOSID=KOSTable.KOSID", "UniversalSourceTable.EntityInstanceID="+cid);
			if(rs.next())concept.put("KOS", rs.get("KOSAcronym"));			
			
			//get preferred string
			String sql = "select st.StringID, st.StringText from UniversalRelationshipTable as urt1";
			sql+= " left join UniversalRelationshipTable as urt2 on urt1.entityInstance2 = urt2.entityInstance1";
			sql+= " left join StringTable as st on urt2.entityInstance2 = st.StringID";
			sql+= " where urt1.entityInstance1="+cid+" and urt1.RelTypeID="+pflableRID+" and urt2.RelTypeID="+mapRID;
			rs.setRs(sql);
			
			if(rs.next()){				
				//concept.put("originalID", rs.get("originalID"));					
				JSONObject stringobject = new JSONObject();
				stringobject.put("stringID", rs.getInt("StringID"));
				stringobject.put("string", rs.get("StringText"));
				concept.put("preferredstring", stringobject);				
			}
			
			//get concept Type			
			rs.setRs(getRelatedEntitySQL(cid,getEntityID("hasConceptType",reltypeEID)));			
			if(rs.next()){
				JSONObject stringobject = new JSONObject();
				stringobject.put("conceptTypeID", rs.getInt("id"));
				stringobject.put("conceptTypeString", rs.get("StringText"));
				concept.put("conceptType", stringobject);
			}

			//get facet 			
			rs.setRs(getRelatedEntitySQL(cid,getEntityID("inFacet",reltypeEID)));
			if(rs.next()){
				JSONObject stringobject = new JSONObject();
				stringobject.put("facetConceptID", rs.getInt("id"));
				stringobject.put("facetConceptString", rs.get("StringText"));
				concept.put("facetConcept", stringobject);
			}
			
		}
		
		//get terms and strings
		public void getTerms() throws Exception{
			
			int altlableRID=getEntityID("altLabel",reltypeEID);
			int manpRID=getEntityID("manifestedAsNP",reltypeEID);
/*			System.out.print(pflableRID+","+altlableRID+"\n");
			System.out.print(mapRID+","+manpRID+"\n");*/
			
			rs.setRs("*","UniversalRelationshipTable","entityInstance1="+cid+" and RelTypeID in ("+pflableRID+","+altlableRID+")");
			
			while(rs.next()){
				int termid = rs.getInt("entityInstance2");
				JSONObject term = new JSONObject();
				term.put("termID", termid);
				//term.put("language", rs.get("Language"));
				
				//get strings
				rs1.setRs("*", "UniversalRelationshipTable left join StringTable on entityInstance2=StringID", 
						"entityInstance1="+termid+" and RelTypeID in ("+mapRID+","+manpRID+")");
				while(rs1.next()){
					JSONObject string  = new JSONObject();
					string.put("stringID", rs1.getInt("StringID"));
					string.put("string", rs1.get("StringText"));
					string.put("language", rs1.get("Language"));					
					term.append("strings", string);					
				}				
				concept.append("terms", term);				
			}			
		}
		
		
		//get notes
		public void getNote() throws Exception{
			
			int dnRID=getEntityID("hasDescriptiveNote",reltypeEID);
			
			rs.setRs("*", "UniversalRelationshipTable left join LongStringTable on entityInstance2=StringID", 
					"entityInstance1="+cid+" and RelTypeID="+dnRID);	
			
			while(rs.next()){
				JSONObject note = new JSONObject();
				note.put("noteID", rs.get("StringID"));
				note.put("noteText", rs.get("StringText"));
				note.put("language", rs.get("Language"));
				concept.append("notes", note);
			}
			
		}
		
		
		public void getAllChildren()throws Exception{
			

			int ntRID=getEntityID("narrower",reltypeEID);
			int ntgRID=getEntityID("hasNarrowerTermGeneric",reltypeEID);
			int ntpRID=getEntityID("hasNarrowerTermPartitive",reltypeEID);
			int ntiRID=getEntityID("hasNarrowerTermInstance",reltypeEID);
			
			
			rs.setRs(getRelatedEntitySQL(cid,ntRID));			
			while(rs.next()){
				JSONObject stringobject = new JSONObject();
				stringobject.put("hierarchyType", "NarrowerTerm");
				stringobject.put("conceptID", rs.getInt("id"));
				stringobject.put("conceptString", rs.get("StringText"));				
				concept.append("children", stringobject);
			}
			rs.setRs(getRelatedEntitySQL(cid,ntgRID));			
			while(rs.next()){
				JSONObject stringobject = new JSONObject();
				stringobject.put("hierarchyType", "NarrowerTermGeneric");
				stringobject.put("conceptID", rs.getInt("id"));
				stringobject.put("conceptString", rs.get("StringText"));				
				concept.append("children", stringobject);
			}
			rs.setRs(getRelatedEntitySQL(cid,ntpRID));			
			while(rs.next()){
				JSONObject stringobject = new JSONObject();
				stringobject.put("hierarchyType", "NarrowerTermPartitive");
				stringobject.put("conceptID", rs.getInt("id"));
				stringobject.put("conceptString", rs.get("StringText"));				
				concept.append("children", stringobject);
			}
			rs.setRs(getRelatedEntitySQL(cid,ntiRID));			
			while(rs.next()){
				JSONObject stringobject = new JSONObject();
				stringobject.put("hierarchyType", "NarrowerTermInstance");
				stringobject.put("conceptID", rs.getInt("id"));
				stringobject.put("conceptString", rs.get("StringText"));				
				concept.append("children", stringobject);
			}
		}
		
		//get the preferred parent, siblings and preferredchildren
		
		public void getHierarchy()throws Exception{
			JSONObject hier = new JSONObject();
			JSONObject parent = new JSONObject();
			JSONArray children = new JSONArray();
			
			int parentid = -1;
			
			//get the parent
			int parentRID = getEntityID("hasParent",reltypeEID);
			rs.setRs(getRelatedEntitySQL(cid,parentRID));			
			if(rs.next()){			
				parentid = rs.getInt("id");
				parent.put("conceptID", parentid);
				parent.put("conceptString", rs.get("StringText"));				
			}
			
			//get the siblings
			int childRID = getEntityID("hasChild",reltypeEID);
			rs.setRs(getRelatedEntitySQL(parentid,childRID));			
			while(rs.next()){
				JSONObject oneitem = new JSONObject();
				int currentid = rs.getInt("id");
				
				oneitem.put("conceptID", rs.getInt("id"));
				oneitem.put("conceptString", rs.get("StringText"));
				
				if(currentid==cid){
					oneitem.put("self", "self");
					//get the children
					rs1.setRs(getRelatedEntitySQL(currentid,childRID));
					while(rs1.next()){
						JSONObject onechild = new JSONObject();
						onechild.put("conceptID", rs1.getInt("id"));
						onechild.put("conceptString", rs1.get("StringText"));
						oneitem.append("children", onechild);
						
					}				
				}				
				parent.append("siblings", oneitem);			
			}
			
			hier.put("parent", parent);
			concept.put("hierarchy", hier);
			
		}
		
		public void getDirectHierarchy()throws Exception{
			
			JSONObject hier = new JSONObject();
			rs.setRs("select * from ConceptHierarchy where EntityInstanceID="+cid);
			if(rs.next())hier=new JSONObject(rs.get("HierarchyJSON"));
			concept.put("hierarchy", hier);
			
		}
		
       public void getAllParents()throws Exception{			
			
			int btRID=getEntityID("broader",reltypeEID);
			int btgRID=getEntityID("hasBroaderTermGeneric",reltypeEID);
			int btpRID=getEntityID("hasBroaderTermPartitive",reltypeEID);
			int btiRID=getEntityID("hasBroaderTermInstance",reltypeEID);
			
			
			rs.setRs(getRelatedEntitySQL(cid,btRID));			
			while(rs.next()){
				JSONObject stringobject = new JSONObject();
				stringobject.put("hierarchyType", "BroaderTerm");
				stringobject.put("conceptID", rs.getInt("id"));
				stringobject.put("conceptString", rs.get("StringText"));				
				concept.append("parents", stringobject);
			}
			rs.setRs(getRelatedEntitySQL(cid,btgRID));			
			while(rs.next()){
				JSONObject stringobject = new JSONObject();
				stringobject.put("hierarchyType", "BroaderTermGeneric");
				stringobject.put("conceptID", rs.getInt("id"));
				stringobject.put("conceptString", rs.get("StringText"));				
				concept.append("parents", stringobject);
			}
			rs.setRs(getRelatedEntitySQL(cid,btpRID));			
			while(rs.next()){
				JSONObject stringobject = new JSONObject();
				stringobject.put("hierarchyType", "BroaderTermPartitive");
				stringobject.put("conceptID", rs.getInt("id"));
				stringobject.put("conceptString", rs.get("StringText"));				
				concept.append("parents", stringobject);
			}
			rs.setRs(getRelatedEntitySQL(cid,btiRID));			
			while(rs.next()){
				JSONObject stringobject = new JSONObject();
				stringobject.put("hierarchyType", "BroaderTermInstance");
				stringobject.put("conceptID", rs.getInt("id"));
				stringobject.put("conceptString", rs.get("StringText"));				
				concept.append("parents", stringobject);
			}
		}
		
		
		public void getPreferredChildren() throws Exception{
	
			int childRID = getEntityID("hasChild",reltypeEID);
			rs.setRs(getRelatedEntitySQL(cid,childRID));			
			while(rs.next()){
				JSONObject stringobject = new JSONObject();
				
				stringobject.put("conceptID", rs.getInt("id"));
				stringobject.put("conceptString", rs.get("StringText"));
				
				String sql = "select st.StringText from UniversalRelationshipTable as urt1";
				sql+= " left join UniversalRelationshipTable as urt2 on urt1.RelTypeID=urt2.entityInstance1";
				sql+= " left join UniversalRelationshipTable as urt3 on urt2.entityInstance2=urt3.entityInstance1";
				sql+= " left join StringTable as st on urt3.entityInstance2=st.StringID";
				sql+= " where urt1.entityInstance1="+cid+" and urt1.entityInstance2="+rs.getInt("id")+" and urt1.RelTypeID<>"+childRID+" and urt2.RelTypeID="+pflableRID+" and urt3.RelTypeID="+mapRID;
				
				rs1.setRs(sql);
				
				if(rs1.next())stringobject.put("hierarchyType", rs1.get("StringText"));
				concept.append("preferredChildren", stringobject);
			}
		}
		
		//get all decendants of this concept
		public JSONArray getDecendants() throws Exception{
			decendantsID = new HashSet();
			LinkedList <Integer> list = new LinkedList();
			list.add(cid);
			
			ntRID=getEntityID("narrower",reltypeEID);
			ntgRID=getEntityID("hasNarrowerTermGeneric",reltypeEID);
			ntpRID=getEntityID("hasNarrowerTermPartitive",reltypeEID);
			ntiRID=getEntityID("hasNarrowerTermInstance",reltypeEID);
			recAddDecendants(list);
			
			int altlableRID=getEntityID("altLabel",reltypeEID);
			int manpRID=getEntityID("manifestedAsNP",reltypeEID);
			
			JSONArray ds = new JSONArray();
			for(Iterator it=decendantsID.iterator();it.hasNext();)
			{
				int conceptID = (Integer) it.next();
				JSONObject one = new JSONObject();
				one.put("conceptID", conceptID);
				//get the preferred String ID
				String sql = "select urt2.entityInstance2, st.StringText from UniversalRelationshipTable as urt1";
				sql+= " left join UniversalRelationshipTable as urt2 on urt1.entityInstance2 = urt2.entityInstance1";
				sql+=" left join StringTable as st on urt2.entityInstance2 = st.StringID";
				sql+= " where urt1.entityInstance1="+conceptID+" and urt1.RelTypeID="+pflableRID+" and urt2.RelTypeID="+mapRID;
				rs.setRs(sql);
				if(rs.next()){
					JSONObject onestring= new JSONObject();
					onestring.put("ID", rs.getInt("entityInstance2"));
					onestring.put("String", rs.get("StringText"));
					one.put("preferredString", onestring);
					}
				
				sql = "select urt2.entityInstance2, st.StringText from UniversalRelationshipTable as urt1";
				sql+= " left join UniversalRelationshipTable as urt2 on urt1.entityInstance2 = urt2.entityInstance1";
				sql+=" left join StringTable as st on urt2.entityInstance2 = st.StringID";
				sql+= " where urt1.entityInstance1="+conceptID+" and urt1.RelTypeID="+altlableRID+" and urt2.RelTypeID="+manpRID;
				rs.setRs(sql);
				while(rs.next()){
					JSONObject onestring= new JSONObject();
					onestring.put("ID", rs.getInt("entityInstance2"));
					onestring.put("String", rs.get("StringText"));
					one.append("nonpreferredString", onestring);
				}
		
				ds.put(one);	
			}
			return ds;
		
			
		}
		
		public void recAddDecendants(LinkedList<Integer>list){
			if (list==null || list.size()==0)return;
			String in = "("+ntRID+","+ntgRID+","+ntpRID+","+ntiRID+")";
			LinkedList <Integer> newlist = new LinkedList();

			for(int i=0;i<list.size();i++){
				String sql = "select * from UniversalRelationshipTable where entityInstance1="+list.get(i)+" and RelTypeID in "+in;
				rs.setRs(sql);
				while(rs.next()){
					int curid = rs.getInt("entityInstance2");
				
					if(curid!=list.get(i)){decendantsID.add(curid);newlist.add(curid);}
				}
			}
			recAddDecendants(newlist);	
			
		}
		
		public void getPreferredParent() throws Exception{
			int parentRID = getEntityID("hasParent",reltypeEID);
			rs.setRs(getRelatedEntitySQL(cid,parentRID));			
			if(rs.next()){
				JSONObject stringobject = new JSONObject();
				
				stringobject.put("conceptID", rs.getInt("id"));
				stringobject.put("conceptString", rs.get("StringText"));
				
				String sql = "select st.StringText from UniversalRelationshipTable as urt1";
				sql+= " left join UniversalRelationshipTable as urt2 on urt1.RelTypeID=urt2.entityInstance1";
				sql+= " left join UniversalRelationshipTable as urt3 on urt2.entityInstance2=urt3.entityInstance1";
				sql+= " left join StringTable as st on urt3.entityInstance2=st.StringID";
				sql+= " where urt1.entityInstance1="+cid+" and urt1.entityInstance2="+rs.getInt("id")+" and urt1.RelTypeID<>"+parentRID+" and urt2.RelTypeID="+pflableRID+" and urt3.RelTypeID="+mapRID;
				
				rs1.setRs(sql);
				
				if(rs1.next())stringobject.put("hierarchyType", rs1.get("StringText"));
				concept.put("preferredParent", stringobject);
			}
		}
		
		public void getAssociation() throws Exception{
			
			//get Getty associations
			int reltypecatEID = getEntityTypeID("RelTypeCate");			
			int gettyCateID=getEntityID("Getty",reltypecatEID);
			
			String sql = "select rt.RelTypeID, st.StringText from UniversalRelationshipTable as urt1";
			sql+= " left join RelTypeListTable as rt on rt.RelTypeID=urt1.RelTypeID";
			sql+= " left join UniversalRelationshipTable as urt2 on rt.RelTypeID = urt2.entityInstance1";
			sql+= " left join UniversalRelationshipTable as urt3 on urt3.entityInstance1 = urt2.entityInstance2";
			sql+= " left join StringTable as st on st.StringID=urt3.entityInstance2";
			sql+= " where urt1.entityInstance1="+cid+" and rt.RelTypeCateID="+gettyCateID+" and urt2.relTypeID="+pflableRID+" and urt3.relTypeID="+mapRID;
			sql+= " group by urt1.RelTypeID";
			
			
			rs.setRs(sql);
			
			while(rs.next()){
				JSONObject association = new JSONObject();
				association.put("associationID", rs.getInt("RelTypeID"));
				association.put("associationString", rs.get("StringText"));
				
				rs1.setRs(getRelatedEntitySQL(cid,rs.getInt("RelTypeID")));
				
				while(rs1.next()){
					JSONObject stringobject = new JSONObject();
					stringobject.put("conceptID", rs1.getInt("id"));
					stringobject.put("preferredString", rs1.get("StringText"));
					association.append("associatedConcepts", stringobject);
				}				
				concept.append("associations", association);				
			}		
		
		}
		
		public static void main(String[] arg) throws Exception{
			ConceptDetailJSON cdj = new ConceptDetailJSON();
			
			cdj.iniDB(19513);

			
            //cdj.getPreferredChildren();
			//System.out.print(cdj.concept.toString());
		
			System.out.print("ini\n");
/*			cdj.getConcept();
			System.out.print("basic\n");
			cdj.getTerms();
			System.out.print("term\n");
			cdj.getNote();
			System.out.print("note\n");*/
			/*cdj.getAssociation();
			System.out.print("association\n");
			cdj.getHierarchy();*/
			 
			 
    		cdj.getAllChildren();
			System.out.print("all children\n");
			cdj.getPreferredChildren();
			System.out.print("preferred children\n");
			cdj.getPreferredParent();
			System.out.print("preferred parent\n");
			cdj.getAllParents();
			System.out.print("all parents\n");
			
			System.out.print(cdj.getConceptElement().toString());
			
			//System.out.print(cdj.getDecendants().toString());
		cdj.endDB();
			
		}
}
