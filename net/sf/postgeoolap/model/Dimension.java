package net.sf.postgeoolap.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JOptionPane;

import net.sf.postgeoolap.connect.DBConnection;
import net.sf.postgeoolap.locale.Local;

public class Dimension extends Table
{
    private long tableCode;
    private String type;
    //private String sql;
    private String clause;
    private Cube cube;
    
    public long getTableCode()
    {
        return this.tableCode;
    }
    
    public String getType()
    {
        return this.type;
    }
    
    public String getClause()
    {
        return this.clause;
    }
    
    public Cube getCube()
    {
        return this.cube;
    }
    
    public void retrieve(Cube cube, String dimensionName, long dimensionCode)
    {
        String sql;
        
        sql = (dimensionCode != 0) ?
            "SELECT * FROM dimension WHERE dimensioncode = " + dimensionCode
        :
            "SELECT * FROM dimension" +
            "  WHERE name = '" + dimensionName + "'" + 
            "    AND cubecode = " + cube.getCode();
        
        try
        {
            Statement statement = DBConnection.getMetadataConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            resultSet.next();
            this.setCode(resultSet.getInt(1));
            this.setName(resultSet.getString(2));
            this.type = resultSet.getString(3);
            //this.sql = resultSet.getString(4);
            this.clause = resultSet.getString(5);
            this.tableCode = resultSet.getInt(7);
            this.cube = cube;
            resultSet.close();
        }
        catch (SQLException e)
    	{
            e.printStackTrace(System.err);
			System.err.println(Local.getString("UnknownSQLException"));
    	}
    }
    
    public void retrieve(Cube cube, String dimensionName)
	{
	    this.retrieve(cube, dimensionName, 0);
	}

    public void retrieve2(Cube cube, String dimensionName)
	{
	    this.retrieve(cube, dimensionName, 0);
	}
    
    public boolean create(Cube cube, String name, String type, String sql, 
        String clause, long tableCode)
    {
        return this.create(cube, name, type, sql, clause, tableCode, 0);
    }
    
    public boolean create(Cube cube, String name, String type, String sql, 
        String clause, long tableCode, long dimensionCode)
    {
        sql = (clause.length() == 0) ?
            "INSERT INTO dimension " +
            "  (name, _type, cubecode, tablecode) " +
            "  VALUES('" + name + "', '" + type + "', " + cube.getCode() + 
               ", " + tableCode + ")"
        :
            "INSERT INTO dimension " +
            "  (name, _type, clause, cubecode, tablecode) " +
            "  VALUES('" + name + "', '" + type + "', '"  + clause + 
               "', " + cube.getCode() + ", " + tableCode + ")";
        
        try
        {
            Statement statement = DBConnection.getMetadataConnection().createStatement();
            statement.execute(sql);
        }
        catch (SQLException e)
    	{
            e.printStackTrace(System.err);
			System.err.println(Local.getString("UnknownSQLException"));
			return false;
    	}
        
        this.retrieve(cube, name);
        return true;
    }
    
    public boolean delete()
    {
        String sql;
        
        sql = "DELETE FROM dimension WHERE dimensioncode = " + this.getCode();
        
        try
        {
            Statement statement = DBConnection.getMetadataConnection().createStatement();
            statement.execute(sql);
            return true;
        }
        catch (SQLException e)
    	{
            e.printStackTrace(System.err);
			System.err.println(Local.getString("UnknownSQLException"));
			return false;
    	}
    }
    
    public static Map getDimensionList(Cube cube)
    {
        String sql;
        Map map = new TreeMap();
        
        sql = "SELECT * FROM dimension " +
        		"WHERE cubecode = " + cube.getCode();
        
        try
        {
            Statement statement = DBConnection.getMetadataConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next())
            {
                Dimension dimension = new Dimension();
                //dimension.dimensionCode = resultSet.getInt(1);
                dimension.setCode(resultSet.getInt(1));
                dimension.setName(resultSet.getString(2));
                dimension.type = resultSet.getString(3);
                //dimension.sql = resultSet.getString(4);
                dimension.clause = resultSet.getString(5);
                dimension.tableCode = resultSet.getInt(7);
                dimension.cube = cube;
                map.put(dimension.getName(), dimension);
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
    
    public boolean addAttribute(String name, String type, int size, 
        String aggregationType, boolean geographic)
    {
        Attribute attribute = new Attribute();
        return attribute.create(this, name, type, size, aggregationType, geographic);
    }
    
    public int getMinimumHierarchicalRank()
    {
        String sql;
        int rank = 0;
        
        sql = "SELECT MIN(_level) FROM attribute " +
        	  "  WHERE dimensioncode = " + this.getCode();
        
        try
        {
            Statement statement = DBConnection.getMetadataConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next())
                rank = resultSet.getInt(1);
            else
                JOptionPane.showMessageDialog(null, Local.getString("TheresNoAttributesForDimension"));
            resultSet.close();
        }
        catch (SQLException e)
    	{
            e.printStackTrace(System.err);
			System.err.println(Local.getString("UnknownSQLException"));
			return rank;
    	}        
        return rank;
    }
    
    public Map getAttributesByLevel(int hierarchicLevel)
    {
        return Attribute.getAttributeByLevel(this, hierarchicLevel);
    }
    
    public Map getFactAggregableAttributes()
    {
        return Attribute.getFactAggregableAttributes(this);
    }
    
    public static Map getAggregationDimensions(Aggregation aggregation)
    {
        Map map = new TreeMap();
        String sql;
        
        // TODO: CnsAgregaçao???
        sql = "SELECT DISTINCT dimensioncode, dimensionname, _type, clause, tablecode " +
        		"FROM cnsaggregation " +
        		"WHERE AggregationCode = " + aggregation.getCode();
        
        try
        {
            Statement statement = DBConnection.getMetadataConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            
            while (resultSet.next())
            {
                Dimension dimension = new Dimension();
                //dimension.dimensionCode = resultSet.getInt(1);
                dimension.setCode(resultSet.getInt(1));
                dimension.setName(resultSet.getString(2));
                dimension.type = resultSet.getString(3);
                dimension.clause = resultSet.getString(4);
                dimension.tableCode = resultSet.getInt(5);
                dimension.cube = aggregation.getCube();
                map.put(dimension.getName(), dimension);
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
    
    public Map getAttributeList()
    {
        return Attribute.getDimensionAttributeList(this);
    }
    
    public Map getAggregationDimensionAttributes(Aggregation aggregation)
    { 
        return Attribute.getAggregationDimensionAttributeList(aggregation, this);
    }
    
    public String toString()
    {
        return this.getName();
    }
}