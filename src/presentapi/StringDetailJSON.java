package presentapi;

import org.json.JSONObject;

import conn.Conn;

/*
 * all concepts with a string ID
 */
public class StringDetailJSON {
	
	Conn rs= new Conn();
	Conn rs1= new Conn();
	JSONObject string = new JSONObject();
	int sid = -1;
	int reltypeEID=-1;
	int pflableRID=-1;
	int mapRID=-1;
	
	
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
	
	//open database
			public void iniDB(int queryid) throws Exception{
				
				sid = queryid;

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
			

			public JSONObject getStringElement() {
				return string;
			}

			//close database
			public void endDB(){
				rs.close();
				rs.closeconn();
				rs1.close();
				rs1.closeconn();
			}
			
			public void getStringDetail()throws Exception{
				
				int altlableRID=getEntityID("altLabel",reltypeEID);
				int manpRID=getEntityID("manifestedAsNP",reltypeEID);	
				int conceptEID = getEntityTypeID("Concept");
				
				
				String sql = "select urt2.entityInstance1 as id from StringTable as st";
				sql+= " left join UniversalRelationshipTable as urt1 on st.StringID = urt1.entityInstance2";
				sql+= " left join UniversalRelationshipTable as urt2 on urt1.entityInstance1 = urt2.entityInstance2";
				sql+= " where st.StringID='"+sid+"' and urt1.RelTypeID in ("+mapRID+","+manpRID+") and urt2.RelTypeID in ("+pflableRID+","+altlableRID+")";
				rs.setRs(sql);
				while(rs.next()){
					int conceptid=rs.getInt("id");
					rs1.setRs("select EntityTypeID from UniversalEntityTable where EntityInstanceID="+conceptid);
					if(!rs1.next() || rs1.getInt("EntityTypeID")!=conceptEID)continue;
					
					JSONObject conceptobject = new JSONObject();
					conceptobject.put("conceptID", conceptid);
					
					rs1.setRs("*", "UniversalSourceTable left join KOSTable on UniversalSourceTable.KOSID=KOSTable.KOSID", "UniversalSourceTable.EntityInstanceID="+conceptid);
					if(rs1.next())conceptobject.put("KOS", rs1.get("KOSAcronym"));
					
					sql="select st.StringID, st.StringText from UniversalRelationshipTable as urt1 ";
					sql+=" left join UniversalRelationshipTable as urt2 on urt1.entityInstance2=urt2.entityInstance1";
					sql+=" left join StringTable as st on urt2.entityInstance2=st.StringID";
					sql+=" where urt1.entityInstance1="+conceptid+" and urt1.RelTypeID="+pflableRID+" and urt2.RelTypeID="+mapRID;
					
					rs1.setRs(sql);
					if(rs1.next()){
						JSONObject stringobject = new JSONObject();
						stringobject.put("stringID", rs1.getInt("StringID"));
						stringobject.put("stringText", rs1.get("StringText"));
						conceptobject.put("preferredString", stringobject);
					}
					
					string.append("concepts", conceptobject);						
					
				}
			}
			
			public static void main(String[] arg) throws Exception{
				StringDetailJSON sdj = new StringDetailJSON();
				sdj.iniDB(600429);
				sdj.getStringDetail();
				sdj.endDB();
				System.out.print(sdj.getStringElement());
			}

}
