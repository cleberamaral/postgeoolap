package net.sf.postgeoolap.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import net.sf.postgeoolap.connect.DBConnection;
import net.sf.postgeoolap.locale.Local;


public class Aggregation
{
    private int code;
    private String name;
    private boolean base;
    private String sqlBase;
    private Cube cube;
    private int sorting;
    private Map attributes;
    
    public int getCode()
    {
        return this.code;
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public boolean isBasic()
    {
        return this.base;
    }
    
    public String getSQLBase()
    {
        return this.sqlBase;
    }
    
    public Cube getCube()
    {
        return this.cube;
    }
    
    public int getSorting()
    {
        return this.sorting;
    }
    
    public Map getAttributes()
    {
        if (attributes.size() == 0)
            this.attributes = Attribute.getAttributeAggegationList(this);
        return this.attributes;
    }
    
    public void setAttributes(Map attributes)
    {
        this.attributes = attributes;
    }
    
    public Aggregation()
    {
        super();
        this.attributes = new TreeMap();
    }
    
    public static Map getAggregationList(Cube cube)
    {
          
        String sql = "SELECT * FROM aggregation" +
          			   "  WHERE cubecode = " + cube.getCode() + 
          			   "  ORDER BY sorting";
        Map map = new TreeMap();
        
        try
        {
            Statement statement = DBConnection.getMetadataConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next())
            {
                Aggregation aggregation = new Aggregation();
                aggregation.code = resultSet.getInt(1);
                aggregation.name = resultSet.getString(2);
                aggregation.base = resultSet.getString(3).equals("S");
                aggregation.sqlBase = resultSet.getString(4);
                aggregation.sorting = resultSet.getInt(6);
                aggregation.cube = cube;
                map.put(aggregation.name, aggregation);
            }
            resultSet.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.err);
			System.err.println(Local.getString("UnknownSQLException"));
			return null;
        }
        
        return map;
    }
    
    public void retrieve(Cube cube, String name)
    {
        this.retrieve(cube, name, 0);
    }
    
    public void retrieve(Cube cube, String name, long code)
    {
        String sql;
        
        sql = (code != 0) ? 
            "SELECT * FROM aggregation" +
            "  WHERE aggregationcode = " + code 
        : 
            "SELECT * FROM aggregation" +
            "  WHERE name = '" + name + "'" +
            "    AND cubecode = " + cube.getCode();
        
        try
        {
            Statement statement = DBConnection.getMetadataConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next())
            {
                this.code = resultSet.getInt(1);
                this.name = resultSet.getString(2);
                this.base = resultSet.getString(3).equals("S");
                this.sqlBase = resultSet.getString(4);
                this.sorting = resultSet.getInt(6);
                this.cube = cube;
            }
            resultSet.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.err);
			System.err.println(Local.getString("UnknownSQLException"));
        }
    }
    
    // creates the basic aggregation for a cube, i.e. the aggregation that is
    // composed by the join operation of all tables of cube. 
    public static boolean createBasicAggregation(Cube cube)
    {
        String sqlBase;
        String name; 
        Dimension dimension;
        int dimensionQuantity;
        Map dimensionMap;
        int order;
        String orderName = ""; // apenas para fazer a concatenação de "9" para a 
        				        // ordenação (que será depois convertida para número)
        dimensionMap = cube.getDimensions();
        dimensionQuantity = dimensionMap.size() - 1; // menos um, a tabela fato
        
        // cria agora um nome para a agregação base atrbuindo tantos "9" 
        // quantas forem as dimensões
        int i;
        name = cube.getName() + "Base";
        // cria o numero para agregação base dentre as demais agregações
        for (i = 1; i <= dimensionQuantity; i++)
            orderName += "9";
        order = Integer.parseInt(orderName);
        // monta a SQL "FROM..." para a agregação base
        sqlBase = "FROM ";

        Iterator iterator = dimensionMap.keySet().iterator();
        String key;
        while (iterator.hasNext())
        {
            key = (String) iterator.next();
            dimension = (Dimension) dimensionMap.get(key);
            if (!sqlBase.equals("FROM "))
                sqlBase += ", ";
            sqlBase += "\"" + dimension.getName() + "\"";
        }
        
        // completa a parte WHERE da SQL para a agregação base
        sqlBase += " WHERE ";
        iterator = dimensionMap.keySet().iterator();
        while (iterator.hasNext())
        {
            key = (String) iterator.next();
            dimension = (Dimension) dimensionMap.get(key);
            if (!dimension.getType().equals("Fact"))
            {
                if (!sqlBase.endsWith("WHERE "))
                   sqlBase += " AND ";
                sqlBase += dimension.getClause();
            }
        }
        
        // insere os dados e cria a agregação
        return save(name, true, sqlBase, cube.getCode(), order);
    }
    
    private static boolean save(String name, boolean base, String sqlAgreg, 
        int cubeCode, int order)
    {
        String sql;
        sql = sqlAgreg.equals("") ?
            "INSERT INTO aggregation " +
            "  (name, base, cubecode, sorting) " +
            "  VALUES ('" + name + "', '" + (base ? "S" : "N") + "', " + 
                cubeCode + ", " + order + ")"
        :
            "INSERT INTO aggregation " +
            "  (name, base, sqlbase, cubecode, sorting) " +
            "  VALUES ('" + name + "', '" + (base ? "S" : "N") + "', '" + 
                sqlAgreg  + "', " + cubeCode + ", " + order + ")";
        
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
        return true;
    }
    
    public boolean deleteCubeAggregation(Cube cube)
    {
        String sql = "DROP TABLE \"" + this.getName() + "\"";
        
        try
        {
            Statement statement = DBConnection.getPostGreSQLConnection().createStatement();
            statement.execute(sql);
        }
	    catch (SQLException e)
	    {
	        e.printStackTrace(System.err);
			System.err.println(Local.getString("UnknownSQLException"));
			//return false;
	    }
	    
	    sql = "DELETE FROM aggregation WHERE aggregationcode = " + this.getCode();	    
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

	    return true;
    }
    
    public boolean create(String aggregationName, Map attributeMap, Cube cube, int sorting)
    {
        String sql;
        String attributeName;
        String attributeType;
        Attribute attribute = new Attribute();
        
        sql = "CREATE TABLE " + aggregationName + " (";
        
        for (Iterator iterator = attributeMap.keySet().iterator(); iterator.hasNext();)
        {
            attribute = (Attribute) attributeMap.get(iterator.next());
            attributeName = attribute.getName();
            // standardizes character types to varchar(255) 
            if (attribute.getType().equals("char") || attribute.getType().equals("varchar"))
                attributeType = "varchar(255)";
            else
            	attributeType = attribute.getType();
            
            if (sql.charAt(sql.length() - 1) != '(')
                sql += ", ";
            sql += attributeName + " " + attributeType;
        }
        
        sql += " )";
        
        // Generates table on PostGreSQL
        try
        {
            Statement statement = DBConnection.getPostGreSQLConnection().createStatement();
            statement.execute(sql);
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.err);
            System.err.println(Local.getString("PGAggregationGenerationError"));
        }
        
        for (Iterator iterator = attributeMap.keySet().iterator(); iterator.hasNext();)
        {
            attribute = (Attribute) attributeMap.get(iterator.next());
            // this call aplies only on geographic attributes
            if (attribute.isGeographic())
            {
                // the last "2" indicates 2D; "3", #D and so on. 
                // Geo type must be in uppercase; aggregate name is in lowercase
                // for compensating PostGreSQL effect of converting to lowercase
                // all object names out of quotes.
                sql = "SELECT addgeometrycolumn('" + cube.getSchema().getName() + 
                	"', '" + aggregationName.toLowerCase() + "', '" + 
                	attribute.getName() + "', '" + cube.getSchema().getSrid() + 
                	"', '" + attribute.getType().toUpperCase() + "', 2)";
                try
                {
                    Statement statement = DBConnection.getPostGreSQLConnection().createStatement();
                    statement.execute(sql);
                }
                catch (SQLException e)
                {
                    e.printStackTrace(System.err);
                    System.err.println(Local.getString("AddingGeographicFieldError"));
                    return false;
                }
            }
        }
        
        // Save aggregation on metadata
        if (!Aggregation.save(aggregationName, false, "", cube.getCode(), sorting))
            return false;
        
        this.retrieve(cube, aggregationName);
        
        try
        {
            Statement statement = DBConnection.getMetadataConnection().createStatement();
        
            for (Iterator iterator = attributeMap.keySet().iterator(); iterator.hasNext();)
	        {
                attribute = (Attribute) attributeMap.get(iterator.next());
	            sql = "INSERT INTO aggregationitem (aggregationcode, attributecode) " +
	            		"VALUES (" + this.getCode() + ", " + attribute.getCode() + ")";
	            statement.execute(sql);
	        }
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.err);
            System.err.println(Local.getString("AggregationItemRecordingError"));
            return false;
        }
        
        return true;
    }
    
    public boolean loadData(Aggregation referenceAggregation)
    {
        String sql;
        Map attributeMap;
        // quantity of attributes to be repeated and futurely form GROUP BY clause
        int aggregableQuantity = 0;
        
        attributeMap = this.getAttributes();
        
        sql = "INSERT INTO " + this.getName() + " (";
        Iterator iterator = attributeMap.keySet().iterator();
        while (iterator.hasNext())
        {
            Attribute attribute = (Attribute) attributeMap.get((String) iterator.next());
            if (sql.charAt(sql.length() - 1) != '(')
                sql += ", ";
            sql += attribute.getName();
            
            // counts aggregable attributes
            if (attribute.getAggregationType().equals("R"))
                aggregableQuantity++;
        }
        
        sql += ") (SELECT ";
        
        String attributeName;
        
        // If aggregation is base, the original dimension's names must be used; 
        // otherwise uses the reference aggregation's name as the owner of 
        // attributes of FROM clause.
        if (referenceAggregation.isBasic())
        {
            attributeName = "";
            iterator = attributeMap.keySet().iterator();
            while (iterator.hasNext())
            {
                Attribute attribute = (Attribute) attributeMap.get((String) iterator.next());
                if (attribute.getAggregationType().equals("R"))
                    attributeName = attribute.getTable().getName() + "." + attribute.getName();
                else if (attribute.getAggregationType().equals("S"))
                    attributeName = "sum(" + attribute.getTable().getName() + "." + attribute.getName() + ")";
                else if (attribute.getAggregationType().equals("C"))
                    attributeName = "count(" + attribute.getTable().getName() + "." + attribute.getName() + ")";
                else if (attribute.getAggregationType().equals("A"))
                    attributeName = "avg(" + attribute.getTable().getName() + "." + attribute.getName() + ")";
                else if (attribute.getAggregationType().equals("M"))
                    attributeName = "max(" + attribute.getTable().getName() + "." + attribute.getName() + ")";
                else if (attribute.getAggregationType().equals("I"))
                    attributeName = "min(" + attribute.getTable().getName() + "." + attribute.getName() + ")";
                
                if (!sql.substring(sql.length() - 7).equals("SELECT "))
                    sql += ", ";
                sql += attributeName;
            }
        }
        else // if (referenceAggregation.getBase())
        {
            attributeName = "";
            iterator = attributeMap.keySet().iterator();
            while (iterator.hasNext())
            {
                Attribute attribute = (Attribute) attributeMap.get((String) iterator.next());
                if (attribute.getAggregationType().equals("R"))
                    attributeName = referenceAggregation.getName() + "." + attribute.getName();
                else if (attribute.getAggregationType().equals("S"))
                    attributeName = "sum(" + referenceAggregation.getName() + "." + attribute.getName() + ")";
                else if (attribute.getAggregationType().equals("C"))
                    attributeName = "count(" + referenceAggregation.getName() + "." + attribute.getName() + ")";
                else if (attribute.getAggregationType().equals("A"))
                    attributeName = "avg(" + referenceAggregation.getName() + "." + attribute.getName() + ")";
                else if (attribute.getAggregationType().equals("M"))
                    attributeName = "max(" + referenceAggregation.getName() + "." + attribute.getName() + ")";
                else if (attribute.getAggregationType().equals("I"))
                    attributeName = "min(" + referenceAggregation.getName() + "." + attribute.getName() + ")";
                
                if (!sql.substring(sql.length() - 7).equals("SELECT "))
                    sql += ", ";
                sql += attributeName;
            }
        }
        
        // mounts FROM clause 
        sql += " FROM ";
        // if some aggregation on which data must be searched was provided, use it;
        // otherwise, search data on base aggregation  
        if (!referenceAggregation.isBasic())
            sql += referenceAggregation.getName();
        else
        {
            Map dimensionMap = this.getAggregationDimensionCollection();
            iterator = dimensionMap.keySet().iterator();
            while (iterator.hasNext())
            {
                Dimension dimension = (Dimension) dimensionMap.get((String) iterator.next());
                if (!sql.substring(sql.length() - 5).equals("FROM "))
                    sql += ", ";
                sql += dimension.getName();
            }
            
            // mounts WHERE clause
            // checks if exist some dimension (i.e. non-fact tables)
            int nonFactDimensionNumber = dimensionMap.size() - 1;
            // if nonFactDimensionNumber == 0, WHERE clause must be ignored
            if (nonFactDimensionNumber > 0)
            {
                sql += " WHERE ";
                iterator = dimensionMap.keySet().iterator();
                while (iterator.hasNext())
                {
                    Dimension dimension = (Dimension) dimensionMap.get((String) iterator.next());
                    if (!sql.substring(sql.length() - 6).equals("WHERE "))
                        sql += " AND ";
                    sql += dimension.getClause();
                }
            }
            sql = sql.substring(0, sql.length() - 9);
        }
        
        // Mounts "GROUP BY" clause (that uses all attributes with AggregationType equals to "R".
        // Checks if exist aggregable attributes of "R" type (already count); otherwise
        // abandons.
        if (aggregableQuantity > 0)
        {
            sql += " GROUP BY ";
            iterator = attributeMap.keySet().iterator();
            while (iterator.hasNext())
            {
                Attribute attribute = (Attribute) attributeMap.get((String) iterator.next());
                if (attribute.getAggregationType().equals("R"))
                {
                    if (!referenceAggregation.isBasic())
                    {
                        if (!sql.substring(sql.length() - 9).equals("GROUP BY "))
                            sql += ", ";
                        sql += referenceAggregation.getName() + "." + attribute.getName();
                    }
                    else
                    {
                        if (!sql.substring(sql.length() - 9).equals("GROUP BY "))
                            sql += ", ";
                        sql += attribute.getTable().getName() + "." + attribute.getName();
                    }
                }
            }
        }
        
        sql += ")";
        
        // Inserts data on PostGreSQL
        try
        {
            Statement statement = DBConnection.getPostGreSQLConnection().createStatement();
            statement.execute(sql);
        }
        catch (SQLException e)
        {
            
            e.printStackTrace(System.err);
            System.err.println(Local.getString("AggregationInsertDataError"));
            return false;
        }
        
        return true;
    }
    
    public Map getAggregationDimensionCollection()
    {
        return Dimension.getAggregationDimensions(this);        
    }
    
    // Na criação de índices, estão sendo criados um índice combinado para cada 
    // conjunto de atributos presente em cada uma das dimensoes participantes de 
    // uma agregaçao e ainda um outro conjunto geral, combinando todos os atributos 
    // não convencionais de todas as dimensoes envolvidas em dada agregaçao.
    // Para os geograficos, estão sendo criados ïndices de apenas um Campo. Não 
    // estão sendo criados indices combinados para os atributos Geo. Caso se deseje 
    // tal comportamento, basta descomentar parte do código e copiar alguns 
    // procedimentos nos moldes dos praticados para os atributos não-geo.
    public boolean createIndexes()
    {
        // Configuration variable (can be posteriorly programmed to be read from 
        // database - Configuration) that says if it must (or not) use combined indexes
        // (standard value is "not" = false)
        boolean combinedIndexes = false;
        String sql;
        
        Map factlessDimensionMap = new TreeMap(); 
        Map dimensionMap = this.getAggregationDimensionCollection();
        Iterator iterator = dimensionMap.keySet().iterator();
        while (iterator.hasNext())
        {
            Dimension dimension = (Dimension) dimensionMap.get((String) iterator.next());
            if (dimension.getType().equals("Dimension"))
                factlessDimensionMap.put(dimension.getName(), dimension);                
        }
        
        // Checks if exist dimensions for this aggregations, and not create 
        // any index
        if (factlessDimensionMap.size() == 0)
            return false;
        
        // Returns, for each dimension in this aggregation, its attributes (for 
        // indexing all attributes of a dimension)
        // In this way, it'll be created: one index for each set of attributes of each
        // dimension, and one index for the total set of attributes (except Fact) of
        // this aggregation.
        int i = 1, j = 1;
        String fieldNames; // field (attribute) sequence of a single dimension
        //String geoFieldNames; // stores only geographic attributes of a dimension
        String totalFieldNames; // field sequence of all dimensions
        //String totalGeoFieldNames; // stores geographic attributes of all dimesions
        String indexName;
        
        iterator = factlessDimensionMap.keySet().iterator();
        while (iterator.hasNext())
        {
            Dimension dimension = (Dimension) factlessDimensionMap.get((String) iterator.next());
            Map attributeMap = dimension.getAggregationDimensionAttributes(this);
            fieldNames = "";
            //geoFieldNames = "";
            totalFieldNames = "";
            //totalGeoFieldNames = "";
            j = 1;
            Iterator iterator2 = attributeMap.keySet().iterator();
            while (iterator2.hasNext())
            {
                Attribute attribute = (Attribute) attributeMap.get((String) iterator2.next());
                if (!attribute.isGeographic()) // only works with non-geo attributes
                {
                    if (combinedIndexes)  // works with combined indexes
                    {
                        if (!fieldNames.equals(""))
                            fieldNames += ", ";
                        fieldNames += attribute.getName();
                        
                        if (!totalFieldNames.equals(""))
                            totalFieldNames += ", ";
                        totalFieldNames += attribute.getName();
                    }
                    else	// works with individual indexes (one index per field)
                    {
                        sql = "CREATE INDEX " + "ix" + this.getName() + i + j + 
                        	  "  ON " + this.getName() + " (" + attribute.getName() + ")";
                        // Creates conventional indexes on PostGreSQL
                        if (!this.createPostGreSQLIndex(sql))
                            return false;
                    }
                }
                else  // only works with geographic attributes
                {
                    // SQL for geographic index (adds "Geo" to index name)
                    sql = "CREATE INDEX " + this.getName() + attribute.getName() +"ixgeo" + 
                    	  "  ON " + this.getName() + 
                    	  "  USING GIST (" + attribute.getName() + " GIST_GEOMETRY_OPS)";
                    // creates PostGreSQL geographic indexes
                    if (!this.createPostGreSQLIndex(sql))
                        return false;
                    
                    // the code above will be used if combined geographic indexes 
                    // be necessary
                    //if (!geoFieldNames.equals(""))
                    //    geoFieldNames += ", ";
                    //geoFieldNames += attribute.getName();
                    //
                    //if (!totalGeoFieldNames.equals(""))
                    //    totalGeoFieldNames += ", ";
                    //totalGeoFieldNames += attribute.getName();
                }
                j++;
            }
            if (combinedIndexes)
            {
                // Generates a name for index
                indexName = this.getName() + i;
                // SQL for combined index
                sql = "CREATE INDEX " + indexName + 
                	  "  ON " + this.getName() + " (" + fieldNames + ")";
                if (!this.createPostGreSQLIndex(sql))
                    return false;
            }
            i++;
        }
        
        // Finally, creates total indexes on PostGreSQL - only the conventional ones
        // (Caso em estudos posteriores seja determinado que devem-se implementar
        // índices combinados para os atributos geográficos, basta utilizar a mesma
        // função de criação com a string contendo os totais de campos geográficos
        
        // THIS ROUTINE WAS DISABLED CAUSE IT GENERATED INDEXES W/ MORE THAN 32 ATTRIBUTES
        //if (combinedIndexes)  // only if it's enabled for combined indexes       
        //{
        //    indexName = this.getName() + "Total";
        //    sql = "CREATE INDEX " + indexName + 
        //    	  "  ON " + this.getName() + " (" + totalFieldNames + ")"; 
        //    if (!this.createPostGreSQLIndex(sql))
        //        return false;
        //}
        
        return true;
    }
    
    private boolean createPostGreSQLIndex(String sqlIndex)
    {
        try
        {
            DBConnection.getPostGreSQLConnection().createStatement().execute(sqlIndex);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            System.err.println(Local.getString("CreatingPostGreSQLIndexError"));
            return false;
        }
        
        return true;
    }
    
    public String toString()
    {
        return this.name;
        
    }
}