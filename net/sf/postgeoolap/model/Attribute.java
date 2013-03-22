package net.sf.postgeoolap.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import net.sf.postgeoolap.connect.DBConnection;
import net.sf.postgeoolap.locale.Local;

public class Attribute 
{
    private static final int MAX_LEVEL = 9;
    
	private long code;
	private String name;
	private String type;
	private int size;
	private int level;
	private String aggregationType;
	private boolean geographic;
	private Table table;
	
	public long getCode()
	{
		return this.code;	
	}
	
	public String getName()
	{
		return this.name; 
	}
	
	public String getType()
	{
		return this.type; 
	}
	
	public int getSize()
	{
		return this.size; 
	}
	
	public int getLevel()
	{
		return this.level;
	}
	
	public String getAggregationType()
	{
		return this.aggregationType;
	}
	
	public boolean isGeographic()
	{
		return this.geographic;
	}
	
	public Table getTable()
	{
	    return this.table;
	}
	
	public void setLevel(int level)
	{
	    this.level = level;
	}
	
	public void retrieve(Dimension dimension, long attributeCode,
		String attributeName)
	{
		String sql;
	
		if (attributeCode != 0)
		    sql = "SELECT * FROM attribute WHERE attributecode = " + attributeCode;
		else
		    sql = "SELECT * FROM attribute WHERE name = '" + attributeName + "' " +
		    		"AND dimensioncode = " + dimension.getCode();
		
	    try
		{
			 Statement statement = DBConnection.getMetadataConnection().createStatement();
			 ResultSet resultSet = statement.executeQuery(sql);
			 resultSet.next();
			 			 
			 int code = resultSet.getInt("attributecode");
			 String name = resultSet.getString("name");
			 String type = resultSet.getString("_type");
			 int size = resultSet.getInt("size"); // if returns 0, field value is null
			 int level = resultSet.getInt("_level"); // idem
			 String aggregationType = resultSet.getString("aggregationtype");
			 if (resultSet.wasNull())
			 	aggregationType = null;
			 boolean geographic = resultSet.getBoolean("geographic");
			 
			// Caso o atributo seja Geografico, seu tipo 
			// deve estar em MaiUSCULAS, pois a geração de 
			// Atributos Geo no PostGIS exige que na função 
			// armazenada "SELECT ADDGEOMETRYCOLUMN...., os 
			// nomes tipos geograficos estejam em Maiúsculas
			//if (geographic)
			//	type.toUpperCase();
			
			resultSet.close();
			
			// object assembling
			this.code = code;
			this.name = name;
			this.type = type;
			this.size = size;
			this.level = level;
			this.aggregationType = aggregationType;
			this.geographic = geographic;
			this.table = dimension;					 
		}
		catch (SQLException e)
		{
			e.printStackTrace(System.err);
			System.err.println(Local.getString("UnknownSQLException"));
		}
	}
	
	public boolean create(Dimension dimension, String name, 
		String type, int size, String aggregationType, boolean geographic)
	{
		String sql;
		sql = "INSERT INTO attribute (name, _type, size, _level, " + 
			"aggregationtype, geographic, dimensioncode) " + 
			"VALUES ('" + name + "', '" + type + "', " + size + ", " + Attribute.MAX_LEVEL + ", '" +
			aggregationType + "', '" + (geographic ? "S" : "N") + "', " + 
			dimension.getCode() + ")";
		
		try
		{
			Statement statement = DBConnection.getMetadataConnection().createStatement();
			statement.executeUpdate(sql);
			
			this.retrieve(dimension, 0, name);						
			return true;
		}
		catch (SQLException e)
		{
			e.printStackTrace(System.err);
			System.err.println(Local.getString("UnknownSQLException"));
			return false;		
		}
	}
	
	
	public boolean delete()
	{
		
		String sql = "DELETE FROM attribute " +
			"WHERE attributecode = " + this.getCode();
	
	    try
		{
			Statement statement = DBConnection.getMetadataConnection().createStatement();
			statement.executeUpdate(sql);
			
			return true;
		}
		catch (SQLException e)
		{
			e.printStackTrace(System.err);
			System.err.println(Local.getString("UnknownSQLException"));
			return false;
		}
	}
	
	
	public static Map getDimensionAttributeList(Dimension dimension)
	{
	    String sql = "SELECT * FROM attribute " + 
					 "  WHERE dimensioncode = " + dimension.getCode();
	    Map col = new TreeMap();
	    
	    try
		{
			Statement statement = DBConnection.getMetadataConnection().createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			
			while (resultSet.next())
			{
				Attribute attribute = new Attribute();
				
				attribute.code = resultSet.getInt("attributecode");
				attribute.name = resultSet.getString("name");
				attribute.type = resultSet.getString("_type");
				attribute.size = resultSet.getInt("size");  // if returns zero, field 
				attribute.level = resultSet.getInt("_level"); // value is null
				attribute.aggregationType = resultSet.getString("aggregationtype");
				attribute.geographic = resultSet.getBoolean("geographic");
				attribute.table = dimension;
				
				col.put(attribute.getName(), attribute);
			}
			resultSet.close();
			return col;
		}
		catch (SQLException e)
		{
			e.printStackTrace(System.err);
			System.err.println(Local.getString("UnknownSQLException"));
			return null;
		}
	}
	
	public static Map getTableAttributeList(Table table)
	{
	    String sql = "SELECT AttName AS Attribute, TypName AS Type" +
	 				 "  FROM PG_ATTRIBUTE, PG_CLASS, PG_TYPE, PG_NAMESPACE" + 
	 				 "  WHERE PG_ATTRIBUTE.attrelid = PG_CLASS.relfilenode" + 
	 				 "    AND PG_ATTRIBUTE.atttypid = PG_TYPE.oid" + 
	 				 "    AND relkind = 'r' AND attnum >= 1" + 
	 				 "    AND PG_NAMESPACE.nspname = 'public'" + 
	 				 "    AND PG_CLASS.relnamespace = PG_NAMESPACE.oid" + 
	 				 "    AND relname = '" + table.getName() + "'";
	    Map col = new TreeMap();
	    
	    try
		{
			Statement statement = DBConnection.getPostGreSQLConnection().createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			
			while (resultSet.next())
			{
				Attribute attribute = new Attribute();
				String type;
				type = resultSet.getString(2).toUpperCase(Local.getLocale());
				attribute.name = resultSet.getString(1);
				attribute.type = resultSet.getString(2);
				attribute.geographic = 
					(type.equals("GEOMETRY") | type.equals("POINT") | 
					 type.equals("LINESTRING") | type.equals("POLYGON") |
					 type.equals("MULTIPOLYGON") | type.equals("MULTILINESTRING") |
					 type.equals("MULTIPOINT"));
				attribute.table = table;
				col.put(attribute.getName(), attribute);					
			}
			
			return col;
		}
		catch (SQLException e)
		{
			e.printStackTrace(System.err);
			System.err.println(Local.getString("UnknownSQLException"));
			return null;
		}
	}
	
	// Returns a Map containing all Attribute objects for required dimension. 	
	public static Map getAttributeSet(Dimension dimension)
	{
		String sql = "SELECT * FROM attribute WHERE dimensioncode = " + 
			dimension.getCode();
		
		try
		{
		    Statement statement = DBConnection.getMetadataConnection().createStatement();
		    ResultSet resultSet = statement.executeQuery(sql);
		    Map map = new TreeMap();
		    while (resultSet.next())
		    {
		        Attribute attribute = new Attribute();
		        attribute.retrieve(dimension, resultSet.getInt("attributecode"), 
		            resultSet.getString("name"));
		        map.put(attribute.getName(), attribute);
		    }
		    return map;
		}
		catch (SQLException e)
		{
		    e.printStackTrace(System.err);
			System.err.println(Local.getString("UnknownSQLException"));
			return null;
		}
	}
	
	// Records to metadatabase the attribute levels chosen to attribute
	public static boolean saveAttributeSet(Map attributeMap)
	{
	    String sql =  "UPDATE attribute SET _level = ? WHERE attributecode = ?";
	    
	    try
	    {
	        PreparedStatement preparedStatement = 
	            DBConnection.getMetadataConnection().prepareStatement(sql);
	        
	        Iterator iterator = attributeMap.keySet().iterator();
	        while (iterator.hasNext())
	        {
	            Attribute attribute = (Attribute) attributeMap.get((String) iterator.next());
	            preparedStatement.setInt(1, attribute.getLevel());
	            preparedStatement.setInt(2, (int) attribute.getCode());
	            preparedStatement.execute();
	        }
	        
	        return true;
	    }
	    catch (SQLException e)
		{
		    e.printStackTrace(System.err);
			System.err.println(Local.getString("UnknownSQLException"));
			return false;
		}
	}
	
	public static Map getAttributeAggegationList(Aggregation aggregation)
	{
	    String sql = "SELECT * FROM cnsaggregation WHERE aggregationcode = " + 
	    	aggregation.getCode();
	    Map map;
	    String dimensionName;
	    
	    try
	    {
	        Statement statement = DBConnection.getMetadataConnection().createStatement();
	        ResultSet resultSet = statement.executeQuery(sql);
	        map = new TreeMap();
	        
	        while (resultSet.next())
	    	{
	           Attribute attribute = new Attribute();
	           attribute.code = resultSet.getInt(7);
	           attribute.name = resultSet.getString(8);
	           attribute.type = resultSet.getString(9);
	           attribute.level = resultSet.getInt(10);
	           attribute.aggregationType = resultSet.getString(11);
	           attribute.geographic = resultSet.getBoolean(12);
	           dimensionName = resultSet.getString(14);
	           Dimension dimension = new Dimension();
	           dimension.retrieve2(aggregation.getCube(), dimensionName);
	           attribute.table = dimension;
	           map.put(attribute.getName(), attribute);
	    	}
	        resultSet.close();
	        return map;
		}
		catch (SQLException e)
		{
		    e.printStackTrace(System.err);
			System.err.println(Local.getString("UnknownSQLException"));
			return null;
		}  
	}
	
	public static Map getAttributeByLevel(Dimension dimension, int level)
	{
	    String sql;
	    // checks if the level is greater than or equal to 1 (the level zero
	    // shouldn't return attributes to permit tne maximum aggregation) 
	    // máxima)
	    sql = (level >= 1) ?
	        "SELECT * FROM attribute " +
	        "  WHERE dimensioncode = " + dimension.getCode() +
	        // returns this level and that more aggegated
	        "    AND _level <= " + level 
	        :
	        "SELECT * FROM attribute " +
	        "  WHERE dimensioncode = " + dimension.getCode() +
	        "    AND _level = " + level;
	    
	    Map map = new TreeMap();
	    
	    try
	    {
	        Statement statement = DBConnection.getMetadataConnection().createStatement();
	        ResultSet resultSet = statement.executeQuery(sql);
	        
	        while (resultSet.next())
	        {
	            Attribute attribute = new Attribute();
	            attribute.code = resultSet.getInt("attributecode");
	            attribute.name = resultSet.getString("name");
	            attribute.type = resultSet.getString("_type");
	            attribute.size = resultSet.getInt("size");
	            attribute.level = resultSet.getInt("_level");
	            attribute.aggregationType = resultSet.getString("aggregationtype");
	            attribute.geographic = resultSet.getBoolean("geographic"); 
	            attribute.table = dimension;
	            map.put(attribute.getName(), attribute);
	            
	        }
	        resultSet.close();
	        return map;
	    }
	    catch (SQLException e)
	    {
	        e.printStackTrace(System.err);
			System.err.println(Local.getString("UnknownSQLException"));
			return null;
	    }
	}
	
	public static Map getFactAggregableAttributes(Dimension dimension)
	{
	    String sql = "SELECT * FROM attribute " +
	    			 "  WHERE dimensioncode = " + dimension.getCode();
	    Map map = new TreeMap();
	    
	    try
	    {
	        Statement statement = DBConnection.getMetadataConnection().createStatement();
	        ResultSet resultSet = statement.executeQuery(sql);
	        
	        while (resultSet.next())
	        {
	            // checks if Attribute.aggregationType is "N" 
	            if (!resultSet.getString("_type").equals("N"))
	            {
		            Attribute attribute = new Attribute();
		            attribute.code = resultSet.getInt("attributecode");
		            attribute.name = resultSet.getString("name");
		            attribute.type = resultSet.getString("_type");
		            attribute.size = resultSet.getInt("size");
		            attribute.level = resultSet.getInt("_level");
		            attribute.aggregationType = resultSet.getString("aggregationtype");
		            attribute.geographic = resultSet.getBoolean("geographic"); 
		            attribute.table = dimension;
		            map.put(attribute.getName(), attribute);
	            }
	        }
	        resultSet.close();
	        return map; 
	    }
	    catch (SQLException e)
	    {
	        e.printStackTrace(System.err);
			System.err.println(Local.getString("UnknownSQLException"));
			return null;
	    }
	}
	
	
	public SortedSet getAttributeInstanceSet()
	{
	    String sql = "SELECT DISTINCT " + this.getName() + " FROM " + 
	    	this.getTable().getName();
	    SortedSet sortedSet = new TreeSet();
	    try
	    {
	        Statement statement = DBConnection.getPostGreSQLConnection().createStatement();
	        ResultSet resultSet = statement.executeQuery(sql);
	        while (resultSet.next())
	            sortedSet.add(resultSet.getString(this.getName()));
	        return sortedSet;
	    }
	    catch (SQLException e)
	    {
	        e.printStackTrace(System.err);
			System.err.println(Local.getString("UnknownSQLException"));
			return null;
	    }
	}
	
	public static Map getAggregationDimensionAttributeList(Aggregation aggregation,
	    Dimension dimension)
	{
	    String sql = "SELECT attributecode, attributename, attributetype, _level, " +
	    			 "  aggregationtype, geographic " +
	    			 "  FROM cnsaggregation " +
	    			 "  WHERE aggregationcode = " + aggregation.getCode() + 
	    			 "    AND dimensioncode = " + dimension.getCode();
	    
	    Map map = new TreeMap();
	    
	    try
	    {	        
	        Statement statement = DBConnection.getMetadataConnection().createStatement();
	        ResultSet resultSet = statement.executeQuery(sql);
	        
	        while (resultSet.next())
	        {
	            Attribute attribute = new Attribute();
	            attribute.code = resultSet.getInt("attributecode");
	            attribute.name = resultSet.getString("attributename");
	            attribute.type = resultSet.getString("attributetype");
	            attribute.level = resultSet.getInt("_level");
	            attribute.aggregationType = resultSet.getString("aggregationtype");
	            attribute.geographic = resultSet.getBoolean("geographic"); 
	            attribute.table = dimension;
	            map.put(attribute.getName(), attribute);	            
	        }
	        resultSet.close();
	        return map;
	    }
	    catch (SQLException e)
	    {
	        e.printStackTrace(System.err);
			System.err.println(Local.getString("UnknownSQLException"));
			return null;
	    }
	}
	
    public String toString()
    {
        return this.getName();
    }	
}