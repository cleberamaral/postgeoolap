package net.sf.postgeoolap.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.TreeMap;

import net.sf.postgeoolap.connect.DBConnection;
import net.sf.postgeoolap.locale.Local;

public class Table 
{
	private long code;
    private String name;
    private Map attributes;
    private Schema schema;
    
    public long getCode()
    {
        return this.code;
    }
    
    public void setCode(long code)
    {
        this.code = code;
    }
    
    public String getName()
	{
		return this.name;
	}
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public Schema getSchema()
    {
        return this.schema;
    }
    
    public void setSchema(Schema schema)
    {
        this.schema = schema;
    }
    
    public Map getAttributes()
    {
        this.attributes = Attribute.getTableAttributeList(this);
        return this.attributes;
    }
    
    public void setAttributes(Map attributes)
    {
        this.attributes = attributes;
    }
    
    public static Map getTableList(Schema schema)
    {
        String sql = "SELECT relfilenode, relname AS TABELA " +
        		     "  FROM PG_CLASS, PG_NAMESPACE " +
        		     "  WHERE relkind = 'r' " +
        		     "    AND pg_namespace.nspname = 'public' " +
        		     "    AND pg_class.relnamespace = pg_namespace.oid";
        Map map = new TreeMap();
        
        
        try
        {
            Statement statement = DBConnection.getPostGreSQLConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            
            while (resultSet.next())
            {
                Table table = new Table();
                table.code = resultSet.getInt(1);
                table.name = resultSet.getString(2);
                table.schema = schema;
                map.put(table.getName(), table);
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
    
    public String getSQLClauseWithFactDimension(Dimension factDimension)
    {
        String sql;
        String sqlClause;
        String factAttributeCode = "", factAttributeName = "";
        String dimensionAttributeCode = "", dimensionAttributeName = "";
        
        
        // busca em pg_constraint as chaves dos campos relacionados 
        // (atual e referenciado)
        sql = "SELECT conkey[1] AS CodAtributoAtual, confkey[1] AS CodAtributoDestino " +
        		"FROM pg_constraint " +
        		"WHERE conrelid = " + factDimension.getTableCode() + 
        		"  AND confrelid = " + this.getCode() + 
        		"  AND contype = 'f'";
        
        try
        {
            Statement statement = DBConnection.getPostGreSQLConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next())
            {	
                factAttributeCode = resultSet.getString(1);
                dimensionAttributeCode = resultSet.getString(2);
            }
            resultSet.close();
            
            // busca, pelas chaves encontradas, os nomes dos atributos da fato e 
            // o referenciado, obtendo o atributo da fato
            sql = "SELECT attname AS AtributoAtual FROM pg_attribute " +
            		"WHERE attrelid = " + factDimension.getTableCode() + 
            		"  AND attnum = " + factAttributeCode;
            resultSet = statement.executeQuery(sql);
            while (resultSet.next())
            {
                factAttributeName = resultSet.getString(1);
            }
            resultSet.close();
            
            // obtendo o atributo de dimensão
            sql = "SELECT attname AS AtributoDestino FROM pg_attribute " +
            		"WHERE attrelid = " + this.getCode()  +
            		"  AND attnum = " + dimensionAttributeCode;
            resultSet = statement.executeQuery(sql);
            while (resultSet.next())
            {
                dimensionAttributeName = resultSet.getString(1);
            }
            resultSet.close();
            
            sqlClause = factDimension.getName() + "." + factAttributeName + " = " + 
            	this.getName() + "." + dimensionAttributeName;
            return sqlClause;
        }
        catch (SQLException e)
    	{
            e.printStackTrace(System.err);
			System.err.println(Local.getString("UnknownSQLException"));
			return null;
    	}
    }

}