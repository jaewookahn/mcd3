package conn;

public class BasicInfo {
	Conn rs= new Conn();
	Conn rs1 = new Conn();
	Conn rs2 = new Conn();
	int pflableRID;
	int mapRID;


	
	public String getPreferredString(int entityID){
		String result = "";
		String sql = "select st.StringText from UniversalRelationshipTable as urt1";
		sql+= " left join UniversalRelationshipTable as urt2 on urt1.entityInstance2 = urt2.entityInstance1";
		sql+= " left join StringTable as st on urt2.entityInstance2 = st.StringID";
		sql+= " where urt1.entityInstance1="+entityID+" and urt1.RelTypeID="+pflableRID+" and urt2.RelTypeID="+mapRID;
		rs1.setRs(sql);
		if(rs1.next())result = rs1.get("StringText");
		return result;
		
	}
	public void printAllRelTypes(){
		
		

		if(!rs.conn("mcd")){System.out.print("wrong connection");return;}	
		if(!rs1.conn("mcd")){System.out.print("wrong connection");return;}	
		if(!rs2.conn("mcd")){System.out.print("wrong connection");return;}	
		/*rs.setRs("*", "EntityTypeListTable");
		while(rs.next())System.out.print(rs.get(1)+"\t"+rs.get(3)+"\t"+rs.get(2)+"\n");*/
		
		
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
		
				
		rs.setRs("*", "RelTypeListTable order by RelTypeCateID");
		
		while(rs.next()){
			
			String content= getPreferredString(rs.getInt("RelTypeID"))+"\t";
			rs1.setRs("*", "EntityTypeListTable","EntityTypeID="+rs.get("EntityTypeID1"));
			if(rs1.next())content+=rs1.get("EntityTypeCode")+"\t";
			rs1.setRs("*", "EntityTypeListTable","EntityTypeID="+rs.get("EntityTypeID2"));
			if(rs1.next())content+=rs1.get("EntityTypeCode")+"\t";
			content+=getPreferredString(rs.getInt("RelTypeCateID"))+"\t\n";
			System.out.print(content);
			
			
		}
				
		
				
		
		
		rs.close();
		rs.closeconn();
		rs1.close();
		rs1.closeconn();
	}
	
	public static void main(String[] arg){
		BasicInfo bi = new BasicInfo();
		bi.printAllRelTypes();
	}

}
