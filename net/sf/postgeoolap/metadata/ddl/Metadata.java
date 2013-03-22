package net.sf.postgeoolap.metadata.ddl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Metadata
{
    private final String CREATE_TABLE_AGGREGATION = 
        "CREATE TABLE aggregation " +
        "( " +
        "  aggregationcode serial NOT NULL, " +
        "  name varchar(50) NOT NULL, " +
        "  base varchar(1), " +
        "  sqlbase varchar(500), " +
        "  cubecode int4, " +
        "  sorting int4, " +
        "  CONSTRAINT pk_aggregation PRIMARY KEY (aggregationcode) " +
        ")";
    
    private final String CREATE_TABLE_AGGREGATIONITEM = 
        "CREATE TABLE aggregationitem " +
        "( " +
        "  aggregationcode int4 NOT NULL, " +
        "  attributecode int4 NOT NULL, " +
        "  CONSTRAINT pk_aggregationitem PRIMARY KEY (aggregationcode, attributecode) " +
        "  " +
        ")";
    
    private final String CREATE_TABLE_ATTRIBUTE =
        "CREATE TABLE attribute " +
        "( " +
        "  attributecode serial NOT NULL, " +
        "  name varchar(50) NOT NULL, " +
        "  size int4, " +
        "  _level int4, " +
        "  aggregationtype char(1), " +
        "  geographic char(1), " +
        "  dimensioncode int4, " +
        "  _type varchar(50), " +
        "  CONSTRAINT pk_attribute PRIMARY KEY (attributecode) " +
        ")";
    
    private final String CREATE_TABLE_CUBE = 
        "CREATE TABLE cubo " +
        "( " +
        "  cubecode serial NOT NULL, " +
        "  name varchar(50) NOT NULL, " +
        "  schemacode int4, " +
        "  minimumaggregation int4, " +
        "  CONSTRAINT pk_cube PRIMARY KEY (cubecode) " +
        ")";
    
    private final String CREATE_TABLE_DIMENSION =
        "CREATE TABLE dimension " +
        "( " +
        "  dimensioncode serial NOT NULL, " +
        "  name varchar(50) NOT NULL, " +
        "  _type varchar(50), " +
        "  sql varchar(500), " +
        "  clause varchar(255), " +
        "  cubecode int4, " +
        "  tablecode int4, " +
        "  CONSTRAINT pk_dimension PRIMARY KEY (dimensioncode) " +
        ")";
    
    private final String CREATE_TABLE_SCHEMA = 
        "CREATE TABLE schema " +
        "( " +
        "  schemacode serial NOT NULL, " +
        "  name varchar(50) NOT NULL, " +
        "  _user varchar(50), " +
        "  _password varchar(50), " +
        "  server varchar(50), " +
        "  map varchar(255), " +
        "  srid int4, " +
        "  CONSTRAINT pk_schema PRIMARY KEY (schemacode) " +
        ")";
    
    private final String CREATE_VIEW_CNSAGGREGATION = 
        "CREATE OR REPLACE VIEW cnsaggregation AS " +
        "  SELECT aggregation.aggregationcode, aggregation.name AS aggregationname, " +
        "		aggregation.base, aggregation.sqlbase, aggregation.sorting, " +
        "		aggregation.cubecode, attribute.attributecode, attribute.name AS attributename, " +
        "		attribute._type AS attributetype, attribute._level, attribute.aggregationtype, " +
        "		attribute.geographic, dimension.dimensioncode, dimension.name AS dimensionname, " +
        "		dimension._type, dimension.clause, dimension.tablecode " +
        "   FROM dimension dimension " +
        "   JOIN (attribute attribute " +
        "   JOIN (aggregation aggregation " +
        "   JOIN aggregationitem aggregationitem ON aggregation.aggregationcode = aggregationitem.aggregationcode) ON attribute.attributecode = aggregationitem.attributecode) ON dimension.dimensioncode = attribute.dimensioncode; ";
    
    public String getAggregationDDL()
    {
        return this.CREATE_TABLE_AGGREGATION;
    }
    
    public String getAggregationItemDDL()
    {
        return this.CREATE_TABLE_AGGREGATIONITEM;
    }
    
    public String getAttributeDDL()
    {
        return this.CREATE_TABLE_ATTRIBUTE;
    }
    
    public String getCubeDDL()
    {
        return this.CREATE_TABLE_CUBE;
    }
    
    public String getDimensionDDL()
    {
        return this.CREATE_TABLE_DIMENSION;
    }

    public String getSchemaDDL()
    {
        return this.CREATE_TABLE_SCHEMA;
    }
    
    public String getAggregationViewDDL()
    {
        return this.CREATE_VIEW_CNSAGGREGATION;
    }
    
    public void generate(String serverName, String serverLocation, String userName, 
        String password) throws MetadataGenerationException
    {
        try
        {
            Class.forName("org.postgresql.Driver");            
	        Connection connection = DriverManager.getConnection(
	            "jdbc:postgresql://" + serverLocation + ":5432/" + serverName, 
	            userName, password);
	        Statement statement = connection.createStatement();
	        
	        statement.execute(this.CREATE_TABLE_AGGREGATION);
	        statement.execute(this.CREATE_TABLE_ATTRIBUTE);
            statement.execute(this.CREATE_TABLE_AGGREGATIONITEM);
	        statement.execute(this.CREATE_TABLE_CUBE);
	        statement.execute(this.CREATE_TABLE_DIMENSION);
	        statement.execute(this.CREATE_TABLE_SCHEMA);
	        statement.execute(this.CREATE_VIEW_CNSAGGREGATION);
	        
	        statement.close();
	        connection.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new MetadataGenerationException(e.getMessage());
        }
        catch (ClassNotFoundException e)
        {
            throw new MetadataGenerationException(e.getMessage());
        }
    }
}
