package conn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSetMetaData;

public class Conn {
	int i;
    private Connection con;
    private Statement stmt;
    private ResultSet rs;
    String dbUser;
    String dbPwd;
    String dbUrl;
    String dbCharset;
    private boolean connopen;
    public Conn()
    {
      connopen = false;
     
   dbUser = "mzhang";
      dbPwd = "Luaph6x";
      dbUrl = "mcd.ischool.drexel.edu:3306";
      
/*     dbUser = "root";
      dbPwd = "123";
      dbUrl = "localhost:3306";*/
      
      
      dbCharset = "utf8";
    }

    public void setConn(String user, String pass, String url, String charset)
    {
        dbUser = user;
        dbPwd = pass;
        dbUrl = url;
        dbCharset = charset;
    }

    public void setConn(String user, String pass)
    {
        dbUser = user;
        dbPwd = pass;
    }

    public boolean conn(String source)
    {
        if(source == null)
            return false;
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch(ClassNotFoundException e)
        {
        	System.out.print(e);
            return false;
        }
        String dbUrl = "jdbc:mysql://" + this.dbUrl + "/" + source + "?useUnicode=true&characterEncoding=" + dbCharset;
        try
        {
            con = DriverManager.getConnection(dbUrl, dbUser, dbPwd);
            dbUrl = null;
        }
        catch(SQLException e1)
        {
        	System.out.print(e1);
            return false;
        }
        try
        {
            stmt = con.createStatement();
        }
        catch(SQLException e2)
        {
        	System.out.print(e2);
            return false;
        }
        connopen = true;
        return true;
    }

    public boolean setRs(String sql)
    {
        try
        {
            rs = stmt.executeQuery(sql);
        }
        catch(SQLException e)
        {
        	System.out.print(e.getSQLState()+"\n"+e.getErrorCode()+"\n"+e.getMessage()+"\n");
            return false;
        }
        return true;
    }

    public boolean setRs(String select, String from)
    {
        try
        {
            rs = stmt.executeQuery("select " + select + " from " + from);
        }
        catch(SQLException e)
        {
            return false;
        }
        return true;
    }

    public boolean setRs(String select, String from, String where1)
    {
        boolean setRs = false;
        try
        {
            rs = stmt.executeQuery("select " + select + " from " + from + " where " + where1);
            setRs = true;
        }
        finally
        {
            return setRs;
        }
    }

    public boolean setRs(String select, String from, String where1, String order1)
    {
        try
        {
            rs = stmt.executeQuery("select " + select + " from " + from + " where " + where1 + " order by " + order1);
        }
        catch(SQLException e)
        {
            return false;
        }
        return true;
    }

  

    public boolean next()
    {
        boolean next = false;
        try
        {
            next = rs.next();
        }
        finally
        {
            return next;
        }
    }

    public boolean previous()
    {
        try
        {
            return rs.previous();
        }
        catch(SQLException e)
        {
            return false;
        }
    }

    public boolean first()
    {
        try
        {
            return rs.first();
        }
        catch(SQLException e)
        {
            return false;
        }
    }

    public String getDate(String field)
    {
        String getDate = "";
        try
        {
            getDate = rs.getDate(field).toString();
        }
        finally
        {
            return getDate;
        }
    }

    public String getTime(String field)
    {
        String getDate = "";
        try
        {
            getDate = rs.getTime(field).toString();
        }
        finally
        {
            return getDate;
        }
    }

    public String get(String field)
    {
        String get = "";
        try
        {
            get = rs.getString(field);
        }
        catch(SQLException e)
        {
            return "no data";
        }
        return get;
    }

    public String getString(String field)
    {
        String get = "";
        try
        {
            get = rs.getString(field);
        }
        catch(SQLException e)
        {
            return "no data";
        }
        return get;
    }

    public String get(int field)
    {
        String get = "";
        try
        {
            get = rs.getString(field);
        }
        catch(SQLException e)
        {
            return "no data";
        }
        return get;
    }

    public int getInt(String field)
    {
        int getInt = 0;
        try
        {
            getInt = rs.getInt(field);
        }
        catch(SQLException e)
        {
            return 0;
        }
        return getInt;
    }
    
    public ResultSetMetaData getMetaData(){
    	ResultSetMetaData md = null;
    	try{
    		md = rs.getMetaData();
    	}
    	catch(SQLException e){
    		return null;
    	}
    	return md;
    }

    public void update(String sql)
        throws SQLException
    {
        stmt.executeUpdate(sql);
    }

    public int count(String from, String where)
    {
        int zq = 0;
        try
        {
            rs = stmt.executeQuery("select count(*) from " + from + " where " + where);
        }
        catch(SQLException e)
        {
            return 0;
        }
        try
        {
            rs.next();
        }
        catch(SQLException e1)
        {
            return 0;
        }
        try
        {
            zq = rs.getInt(1);
        }
        catch(SQLException e2)
        {
            return 0;
        }
        try
        {
            rs.close();
        }
        catch(SQLException e3)
        {
            return 0;
        }
        return zq;
    }

    public int count(String from)
    {
        int i = 0;
        try
        {
            rs = stmt.executeQuery("select count(*) from " + from);
        }
        catch(SQLException e)
        {
            return 0;
        }
        try
        {
            rs.next();
        }
        catch(SQLException e1)
        {
            return 0;
        }
        try
        {
            i = rs.getInt(1);
        }
        catch(SQLException e2)
        {
            return 0;
        }
        try
        {
            rs.close();
        }
        catch(SQLException e3)
        {
            return 0;
        }
        return i;
    }
    

    public boolean insert(String into, String values)
    {
        try
        {
            stmt.executeUpdate("insert into " + into + " values(" + values + ")");
        }
        catch(SQLException e)
        {
            return false;
        }
        return true;
    }

    public boolean edit(String edit, String values, String where)
    {
        try
        {
            stmt.executeUpdate("update " + edit + " set " + values + " where " + where);
        }
        catch(SQLException e)
        {
            return false;
        }
        return true;
    }

    public boolean del(String from, String where)
    {
        try
        {
            stmt.executeUpdate("delete from " + from + " where " + where);
        }
        catch(SQLException e)
        {
            return false;
        }
        return true;
    }

    public void close()
    {
        try
        {
            rs.close();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    public boolean closeall()
    {
        boolean close = true;
        if(connopen)
        {
            closeconn();
            connopen = false;
        }
        return close;
    }

    public void closeconn()
    {
        try
        {
            stmt.close();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        try
        {
            con.close();
        }
        catch(SQLException e1)
        {
            e1.printStackTrace();
        }
        connopen = false;
    }

    

    public static void main(String arg[])
    {
        Conn rs = new Conn();
        if(!rs.conn("getty_aat"))
            System.out.print("wrong connection");
        else System.out.print("ss");
        
    }
}
