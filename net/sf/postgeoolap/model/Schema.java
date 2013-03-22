package net.sf.postgeoolap.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import net.sf.postgeoolap.connect.DBConnection;
import net.sf.postgeoolap.locale.Local;


public class Schema
{
    private long code;
    private String name;
    private String user;
    private String password;
    private String server;
    private String map;
    private long srid;
    private Map cubes;
    private Map tables;

    public Schema()
    {
        super();
    }
    
    public long getCode()
    {
        return this.code;
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public String getUser()
    {
        return this.user;
    }
    
    public String getPassword()
    {
        return this.password;
    }
    
    public String getServer()
    {
        return this.server;
    }
    
    public String getMap()
    {
        return this.map;
    }
    
    public long getSrid()
    {
        return this.srid;
    }
    
    public Map getTables()
    {
        this.tables = Table.getTableList(this);
        return this.tables;
    }
    
    public Map getCubes()
    {
        this.cubes = Cube.getCubeList(this);
        return this.cubes;
    }
        
    public boolean create(String name, String user, String password, String server,
    	String map, int srid, boolean edit)
    {
        Statement statement;
        String strSQL;
        	
        if (edit)
            strSQL = "UPDATE schema " +
 		    	  	 "  SET name = '" + name + "', " +
		    		 "  	_user = '" + user + "', " +
		    		 "  	_password = '" + password + "', " +
		    		 "  	server = '" + server + "', " +
		    		 "  	map = '" + map + "', " +
		    		 "  	srid = '" + srid + "'" +
		    		 "	WHERE schemacode = " + this.code; 
        else
        {
            strSQL = "INSERT INTO schema " +
       		 		 "  (name, _user, _password, server, map, srid)" +
       		 		 "  VALUES ('" + name + "', '" + user + "', '" + password + "', '" + 
       		 		 server + "', '" + map + "', '" + srid + "')";
        }
        
        try
        {
            statement = DBConnection.getMetadataConnection().createStatement();
            statement.executeUpdate(strSQL);
            statement.close();
        }
        catch (SQLException e)
        {
            JOptionPane.showMessageDialog(null, Local.getString("CreatingSchemaError"));
            e.printStackTrace(System.err);
            return false;
        }
        // retrieve the object (in order to retrieve the autoincrement number)
        this.retrieve(name);
        
        return true;
    }
    
    public void retrieve(long code)
    {
        String sql = "SELECT * FROM schema WHERE schemacode = " + code;
        this.retrieveSQL(sql);
    }
    
    public void retrieve(String name)
    {
        String sql = "SELECT * FROM schema WHERE name = '" + name + "'";
        this.retrieveSQL(sql);
    }
    
    private void retrieveSQL(String sql)
    {
        ResultSet resultSet;
        Statement statement;
        
        try
        {
            statement = DBConnection.getMetadataConnection().createStatement();
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            this.code = resultSet.getInt("schemacode");
            this.name = resultSet.getString("name");
            this.user = resultSet.getString("_user");
            this.password = resultSet.getString("_password");
            this.server = resultSet.getString("server");
            this.map = resultSet.getString("map");
            this.srid = resultSet.getInt("srid");
        }
        catch (SQLException e)
        {
            JOptionPane.showMessageDialog(null, "RetrievingSchemaError");
            e.printStackTrace(System.err);
        }
    }
           
    public static Map getCollection()
    {
        String strSQL;
        ResultSet resultSet;
        Statement statement;
        
        strSQL = "SELECT * FROM Schema";
        Map schemaMap = new TreeMap();
        
        try
        {
            statement = DBConnection.getMetadataConnection().createStatement();
            resultSet = statement.executeQuery(strSQL);
            
            while (resultSet.next())
            {
                Schema schema = new Schema();
                schema.code = resultSet.getInt("schemacode");
                schema.name = resultSet.getString("name");
                schema.password = resultSet.getString("_password");
                schema.user = resultSet.getString("_user");
                schema.server = resultSet.getString("server");
                schema.map = resultSet.getString("map");
                schema.srid = resultSet.getLong("srid");
                
                // Put the object (schema) and its key in the Map
                schemaMap.put(schema.getName(), schema);
            }            
        }
        catch (SQLException e)
        {
            JOptionPane.showMessageDialog(null, Local.getString("CouldNotRetrieveSchemaCollection"));
            e.printStackTrace(System.err);
            return null;
        }
        
        return schemaMap;
    }
    
    public boolean connect()
    {
        if (DBConnection.configPostGreSQLConnection(this))
        {
            JOptionPane.showMessageDialog(null, Local.getString("PostGreSQLConnectionSucceeded"));
            return true;
        }
        else
        {
            JOptionPane.showMessageDialog(null, Local.getString("PostGreSQLConnectionFailed"));
            return false;    
        }
   }
}