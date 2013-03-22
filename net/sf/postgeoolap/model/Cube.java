package net.sf.postgeoolap.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import net.sf.postgeoolap.connect.DBConnection;
import net.sf.postgeoolap.gui.OutputProgress;
import net.sf.postgeoolap.locale.Local;

public class Cube
{
    private int code;
    private String name;
    private Schema schema;
    //private Map dimensions_;
    //private Map aggregations;
    private Map aggregationsCache;
    private long minimumAggregation;
    
    public static final int MAX_LEVEL = 9;
    
    public int getCode()
    {
        return this.code;
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public Map getDimensions()
    {
        return Dimension.getDimensionList(this);
    }
    
    public Map getAggregations()
    {
        return Aggregation.getAggregationList(this);
    }
    
    public Map getAggregationsCache()
    {
        return (this.aggregationsCache == null) ?
            this.getAggregations() :
            this.aggregationsCache;
    }
    
    public Schema getSchema()
    {
        return this.schema;
    }
    
    public long getMinimumAggregation()
    {
        return this.minimumAggregation;
    }
    
    public void retrieve(Schema schema, long code)
    {
        String sql = "SELECT * FROM cubo WHERE cubecode = " + code;
        this.retrieveSQL(schema, sql);
    }
    
    public void retrieve(Schema schema, String name)
    {
        String sql = "SELECT * FROM cubo WHERE name = '" + name + "'";
        this.retrieveSQL(schema, sql);
    }
    
    private void retrieveSQL(Schema schema, String sql)
    {
        try
        {
            Statement statement = DBConnection.getMetadataConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            
            if (resultSet.next())
            {
	            this.code = resultSet.getInt("cubecode");
	            this.name = resultSet.getString("name");
	            this.schema = schema;
	            this.minimumAggregation = resultSet.getLong("MinimumAggregation");
            }
            resultSet.close();
        }
        catch (SQLException e)
        {
            System.out.println(Local.getString("CubeRetrievingError"));
            e.printStackTrace(System.err);
        }
    }
   
    public boolean create(Schema schema, String name)
    {
        return this.create(schema, name, 0);
    }
    
    public boolean create(Schema schema, String name, long minimumAggregation)
    {
        String sql = "INSERT INTO cubo " +
        			   "(name, schemacode, minimumaggregation) " +
        			   "VALUES ('" + name + "', " + schema.getCode() + ", " + minimumAggregation + ")";
        
        try
        {
            Statement statement = DBConnection.getMetadataConnection().createStatement();
            statement.execute(sql);
        }
        catch (SQLException e)
        {
            System.err.println(Local.getString("CubeRecordingError"));
            e.printStackTrace(System.err);
            return false;
        }
        
        this.retrieve(schema, name);
        return true;
    }
    
    public boolean delete()
    {
        String sql = "DELETE FROM cubo WHERE cubecode = " + this.getCode();
        try
        {
            Statement statement = DBConnection.getMetadataConnection().createStatement();
            statement.execute(sql);
            return true;
        }
        catch (SQLException e)
        {
            System.err.println(Local.getString("CubeDeletingError"));
            e.printStackTrace(System.err);
            return false;
        }
    }
    
    public static Map getCubeList(Schema schema)
    {
        String sql = "SELECT * FROM cubo WHERE schemacode = " + schema.getCode();
        Map map = new TreeMap();
        try
        {
            Statement statement = DBConnection.getMetadataConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            
            while (resultSet.next())
            {
                Cube cube = new Cube();
                cube.code = resultSet.getInt(1);
                cube.name = resultSet.getString(2);
                cube.minimumAggregation = resultSet.getLong(4);
                cube.schema = schema;
                map.put(cube.getName(), cube);
            }
            resultSet.close();
            return map;
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.err);
            return null;
        }
    }
    
    public boolean addDimension(String dimensionName, String dimensionType, 
    	String dimensionSQL, String dimensionClause, long tableCode)
    {
        Dimension dimension = new Dimension();
        return dimension.create(this, dimensionName, dimensionType, dimensionSQL, 
            dimensionClause, tableCode);
    }
    
    public Dimension getFactDimension()
    {
        Map dimensionMap = this.getDimensions();
        Iterator iterator = dimensionMap.keySet().iterator();
        while (iterator.hasNext())
        {
            Dimension dimension = (Dimension) dimensionMap.get((String) iterator.next());
            if (dimension.getType().equals("Fact"))
                return dimension;
        }
        return null;
    }
    
    public ArrayList getNonFactDimensions()
    {
        Map dimensions = this.getDimensions();
        ArrayList arrayList = new ArrayList();
        Iterator iterator = dimensions.keySet().iterator();
        while (iterator.hasNext())
        {
            Dimension dimension = (Dimension) dimensions.get((String) iterator.next());
            if (dimension.getType().equals("Dimension"))
                arrayList.add(dimension);
        }
        return arrayList;
    }
    
    // This method returns the best aggregation that match to attributes in a query
    public Aggregation queryNavigator(Map attributeCollection, boolean cache)
    {
        // collection of aggregations of this cube
        Map aggregationCollection = (cache) ?
            this.getAggregationsCache():
            this.getAggregations();
        Map attributeMap;
        Aggregation baseAggregation = null;
        
        // Corre todos os atributos contra uma agregação (na ordem da mais agregada
        // para a menos), em busca de uma que atenda a toda a coleção de atributos
        Iterator iterator = aggregationCollection.keySet().iterator();
        while (iterator.hasNext())
        {
            Aggregation aggregation = (Aggregation) aggregationCollection.get((String) iterator.next());
            // Já deixa armazenada a agregação base, para caso seja necessário
            if (aggregation.isBasic()){
                baseAggregation = aggregation;
            }else{
                ArrayList matchList = new ArrayList();
                attributeMap = aggregation.getAttributes();
                for (Iterator iterator2 = attributeCollection.keySet().iterator(); iterator2.hasNext();)
                {
                    Attribute attribute = (Attribute) attributeCollection.get(iterator2.next());
                    Attribute attrib2 = (Attribute) attributeMap.get(attribute.getName());
                    if (attrib2 == null)
                        //return baseAggregation;
                        break;
                    else
                        matchList.add(attrib2);
                    // Testa se a quantidade de atributos em matchList é igual à
                    // quantidade em attributeCollection (ou seja, são iguais) para
                    // encerrar a busca
                    if (matchList.size() == attributeCollection.size())
                    {
                        // se iguais, então achou a agregação correta
                        return aggregation;
                    }
                }
            }
            
        }
        
        // Se a busca pela agregação ideal chegar aqui, é porque nenhuma agregação
        // possuía todos os atributos necessários. O cubo deve então responder com
        // a agregação base.
        return baseAggregation;
    }
    
    // Load metadata to this cube (attributes and aggregations), in order to 
    // keep these informations on cache.
    /*
    public void loadMetadata()
    {
        Map map = this.getAggregationsCache();
        //Map attributeCollection = new TreeMap();
        Iterator iterator = map.keySet().iterator();
        while (iterator.hasNext())
        {
            //Aggregation aggregation = (Aggregation) map.get((String) iterator.next());
            // TODO: ????
            //attributeCollection = aggregation.getAttributes();
        }
    }
    */
    
    public boolean wasProcessed()
    {
        return this.getAggregations().size() > 0;
    }
    
    public ResultSet executeQuery(Map attributeMap, Aggregation aggregation, 
        String whereClause) throws SQLException
    {
        String sql;
        int aggregableQuantity = 0;
        
        sql = "SELECT ";
        
        // Adds SELECT part to SQL
        String attributeName = "";
        if (aggregation.isBasic())
        {
            // If it uses the base aggregation, the table name is original name
            Iterator iterator = attributeMap.keySet().iterator();
            while (iterator.hasNext())
            {
                Attribute attribute = (Attribute) attributeMap.get(iterator.next());
                // Checks if attribute is aggregable and its type
                if (attribute.getAggregationType().equals("R"))
                    attributeName = attribute.getTable().getName() + "." + attribute.getName();
                else if (attribute.getAggregationType().equals("S"))
                    attributeName = "SUM(" + attribute.getTable().getName() + "." + attribute.getName() + ") AS SUM_" + attribute.getName();
                else if (attribute.getAggregationType().equals("C"))
                    attributeName = "COUNT(" + attribute.getTable().getName() + "." + attribute.getName() + ") AS COUNT_" + attribute.getName();
                else if (attribute.getAggregationType().equals("A"))
                    attributeName = "AVG(" + attribute.getTable().getName() + "." + attribute.getName() + ") AS AVG_" + attribute.getName();
                else if (attribute.getAggregationType().equals("M"))
                    attributeName = "MAX(" + attribute.getTable().getName() + "." + attribute.getName() + ") AS MAX_" + attribute.getName();
                else if (attribute.getAggregationType().equals("I"))
                    attributeName = "MIN(" + attribute.getTable().getName() + "." + attribute.getName() + ") AS MIN_" + attribute.getName();
                
                // Adds attribute name to SQL string
                if (!sql.endsWith("SELECT "))
                    sql += ", ";
                sql += attributeName;
                
                // count aggregable attributes
                if (attribute.getAggregationType().equals("R"))
                    aggregableQuantity++;
            }
        }
        else
        // If not using base aggregation, the table name is aggregation name
        {
            Iterator iterator = attributeMap.keySet().iterator();
            while (iterator.hasNext())
            {
                Attribute attribute = (Attribute) attributeMap.get(iterator.next());
                // Checks if attribute is aggregable and its type
                if (attribute.getAggregationType().equals("R"))
                    attributeName = aggregation.getName() + "." + attribute.getName();
                else if (attribute.getAggregationType().equals("S"))
                    attributeName = "SUM(" + aggregation.getName() + "." + attribute.getName() + ") AS SUM_" + attribute.getName();
                else if (attribute.getAggregationType().equals("C"))
                    attributeName = "COUNT(" + aggregation.getName() + "." + attribute.getName() + ") AS COUNT_" + attribute.getName();
                else if (attribute.getAggregationType().equals("A"))
                    attributeName = "AVG(" + aggregation.getName() + "." + attribute.getName() + ") AS AVG_" + attribute.getName();
                else if (attribute.getAggregationType().equals("M"))
                    attributeName = "MAX(" + aggregation.getName() + "." + attribute.getName() + ") AS MAX_" + attribute.getName();
                else if (attribute.getAggregationType().equals("I"))
                    attributeName = "MIN(" + aggregation.getName() + "." + attribute.getName() + ") AS MIN_" + attribute.getName();
                
                // Adds attribute name to SQL string
                if (!sql.endsWith("SELECT "))
                    sql += ", ";
                sql += attributeName;
                
                // count aggregable attributes
                if (attribute.getAggregationType().equals("R"))
                    aggregableQuantity++;
            }
        }
        
        // Creates FROM clause
        // Checks if aggregation passed as parameter is base
        if (aggregation.isBasic())
            sql += " " + aggregation.getSQLBase();
        else
            sql += " FROM " + aggregation.getName();
        
        // Mounts WHERE clause (if exists)
        if ((whereClause != null) && (!whereClause.equals("")))
        {
            // Checks if it's the base (because it has where clause assembled, 
            // being necessary only add new constraints)
            if (aggregation.isBasic())
                sql += " AND (" + whereClause;
            else
                sql += " WHERE (" + whereClause;
            sql += ")";
        }
        
        // Mounts GROUP BY clause (that use all attributes having 
        // aggregationType = "R"
        // Checks if exist aggregable attributes of "R" type (already counted),
        // otherwise abandons.
        if (aggregableQuantity > 0)
        {
            sql += " GROUP BY ";
            if (aggregation.isBasic())
            {
                Iterator iterator = attributeMap.keySet().iterator();
                while (iterator.hasNext())
                {
                    Attribute attribute = (Attribute) attributeMap.get(iterator.next());
                    // Checks if attribute is aggregable "R" type
                    // (only these will be part of GROUP BY clause)
                    if (attribute.getAggregationType().equals("R"))
                    {
                        if (!sql.endsWith("GROUP BY "))
                            sql += ", ";
                        sql += attribute.getTable().getName() + "." + attribute.getName();
                    } 
                }
            }
            else
            {
                // If not using base aggregation, the table name is aggregation name
                Iterator iterator = attributeMap.keySet().iterator();
                while (iterator.hasNext())
                {
                    Attribute attribute = (Attribute) attributeMap.get(iterator.next());
                    // Checks if attribute is aggregable "R" type
                    // (only these will be part of GROUP BY clause)
                    if (attribute.getAggregationType().equals("R"))
                    {
                        if (!sql.endsWith("GROUP BY "))
                            sql += ", ";
                        sql += aggregation.getName() + "." + attribute.getName();
                    } 
                }
            }
        }
        
        // Do data selection on PostGreSQL
        Statement statement = DBConnection.getPostGreSQLConnection().createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        return resultSet;
    }

    public void process(OutputProgress output)
    {
        output.addString(Local.getString("DeletingPreExistingAggregations"));
        Map aggregationMap = this.getAggregations();
        Iterator iterator = aggregationMap.keySet().iterator();
        while (iterator.hasNext())
        {
            Aggregation aggregation = (Aggregation) aggregationMap.get((String) iterator.next());
            if (!aggregation.deleteCubeAggregation(this))
            {
                output.addString(Local.getString("DeletingAggregationError"));
                //return;
            }
        }
        
        output.addString(Local.getString("AgregationsDeleted"));
/**        
        if (JOptionPane.showConfirmDialog(null, Local.getString("FormerAggregationsDeleted"), 
            "PostGeoOLAP", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
            return;
*/        
        // sending message to viewer
        output.addString(Local.getString("GeneratingBaseAggregation"));
        
        // Creates base aggregation
        if (!Aggregation.createBasicAggregation(this))	
        {
            output.addString(Local.getString("BasicAggregationCreatingError"));
            return;
        }
        
        // Checks quantity of aggregable dimensions of the cube        
        List dimensionList = this.getNonFactDimensions();
        // Stores quantity of dimensions of the cube 
        // (except Fact and Non-aggregable)
        int dimensionCount = dimensionList.size();
        
        // Sends message to viewer
        output.addString(Local.getString("CurrentCubeHas") + " " + 
            dimensionCount + " " + Local.getString("dimensions"));
        
        if (dimensionCount < 2)
        {
            output.addString(Local.getString("LessThanTwoDimensions"));
            return;
        }
        //A principio, este pedaço é descartável.......DAQUI.....
        Dimension _d = this.getFactDimension();
        Map map = _d.getFactAggregableAttributes();
        //Map map = this.getFactDimension().getFactAggregableAttributes();
        List factAttributeList = new ArrayList();
        iterator = map.keySet().iterator();
        while (iterator.hasNext())
        {
            Attribute attribute = (Attribute) map.get((String) iterator.next());
            factAttributeList.add(attribute);
        }
        //......até AQUI
        
        // ===================BEGIN WORK========================
        // stack collection (each element has a stack)
        Stack stackList = new Stack();
                
        int levelNumber; // quantity of levels (hierarchy) of dimension
        Dimension dimension1 = (Dimension) dimensionList.get(0);
        System.out.println("A dimensao 1 e (pelo codigo): " + dimension1.getTableCode());
        // If the most aggregated is level 7, we'll have: 10 - 7 = 3 levels 
        levelNumber = dimension1.getMinimumHierarchicalRank();
        //Map dimensionAttributesMap = dimension1.getAttributes();
        // Puts each hierarchy of first dimension in level 1 of each stack
        // Will be created one stack for each hierarchy.
        Stack stack = new Stack();
        stack.push(new Integer(0)); // create by anticipation the level 0
        stackList.add(stack);
        
        for (int i = levelNumber; i <= Cube.MAX_LEVEL; i++) 
        // Runs all hierarchic levels (including the zero, that is the "ALL", 
        // via creation of a empty attribute) in a descending way. 
        {
            stack = new Stack();
            stack.push(new Integer(i));
            stackList.add(stack);
        }
        
        // Now, exists a stack collection and each stack with a single element
        // (level), all from dimension1
        
        // Begins a loop that joins this elements to levels of dimension2 and so on
        int j;
        while (stackList.size() != 0)
        {
            // Gets first element from stack collection
            // Pops last stack to activeStack variable
            Stack activeStack = (Stack) stackList.pop(); 
            
            //========== CUBE PROCESSING BEGIN ===================
            // Checks if current stack is complete. If yes, process and remove it
            // If attribute count equals to dimension count, stack is complete.
            if (activeStack.size() == dimensionCount)
            {
                // at beginning, doesn't process (assumes that all levels are "9") 
                boolean canProcess = false;
                String sortingName = "";
                
                for (int i = 0; i < activeStack.size() - 1; i++)
                    if (((Integer) activeStack.get(i)).intValue() != Cube.MAX_LEVEL)
                    {
                        canProcess = true;
                        break;
                    }
                
                // Doesn't permit aggregation of attributes of basic structure
                // (levels "9" of all dimensions)
                if (canProcess)
                {
                    // Calls processing
                    Dimension posDimension;
                    Map attributeMap = new TreeMap();
                    int sorting;
                    int pos;
                    String aggregationName = "";
                    
                    // Searches the attribute collection
                    for (int i = 0; i < factAttributeList.size(); i++)
                    {
                        // Copies attributes from Fact
                        Attribute attribute = (Attribute) factAttributeList.get(i);
                        attributeMap.put(attribute.getName(), attribute);
                    }
                    
                    // For each level of stack, searches corresponding attributes
                    /// on respective dimension
                    for (int i = 0; i < activeStack.size(); i++)
                    {
                        // Finds dimension corresponding to current position
                        posDimension = (Dimension) dimensionList.get(i);
                        // Gets the level of the position on stack
                        pos = ((Integer) activeStack.get(i)).intValue();
                        // composes name for sorting and for aggregation
                        sortingName += Integer.toString(pos).trim();
                        
                        map = posDimension.getAttributesByLevel(pos); 
                        iterator = map.keySet().iterator();
                        while (iterator.hasNext())
                        {
                            Attribute attribute = (Attribute) map.get((String) iterator.next());
                            attributeMap.put(attribute.getName(), attribute);
                        }
                    }
                    
                    sorting = Integer.parseInt(sortingName);
                    aggregationName = this.getName() + sortingName;
                    
                    // Checks if is needed to create current aggregation
                    output.addString(Local.getString("CheckingCostBenefitRelation") + 
                        " " + aggregationName + "...");
                    Aggregation referenceAggregation = this.verifyAggregationCost(attributeMap);
                    if (!referenceAggregation.getName().equals(""))
                    {
                        // Creates aggregation
                        output.addString(Local.getString("CreatingAggregation") + " " + 
                            aggregationName);
                        // Calls the method that assemblies the aggregation
                        Aggregation aggregation = new Aggregation();
                        if (!aggregation.create(aggregationName, attributeMap, this, sorting))
                        {
                            output.addString(Local.getString("CreationOfAggregation") + " " + 
                                aggregationName + " " + Local.getString("failed"));
                            return;
                        }
                        else
                            output.addString(Local.getString("Aggregation") + " " + aggregationName + 
                                    " " + Local.getString("CreatedSuccessfully"));
                        output.addString(Local.getString("CopyingDataToAggregation"));
                        
                        // Load aggregation data
                        if (aggregation.loadData(referenceAggregation))
                            output.addString(Local.getString("DataCopiedTo") + " " + aggregation.getName() + " " +  
                                Local.getString("successfully"));
                        else
                            output.addString(Local.getString("CopyOfDataToAggregation") + " " + aggregation.getName() + " " +  
                                Local.getString("failed"));
                        
                        // creates aggregation indexes 
                        output.addString(Local.getString("CreatingIndexesToAggregation") + 
                            " " + aggregation.getName());
                        if (aggregation.createIndexes())
                            output.addString(Local.getString("IndexesCreatedTo") + " " +  
                                aggregation.getName() + " " + Local.getString("successfully"));
                        else
                            output.addString(Local.getString("CreatingOfIndexesToAggregation"));
                        
                        // update statistics
                        // after end of processing, updates statistics to query optimizator
                        String optimize = "VACUUM ANALYZE " + aggregation.getName();
                        
                        output.addString(Local.getString("UpdatingStatisticsForAggregation") + " " + 
                            aggregation.getName());
                        try
                        {
                            Statement statement = DBConnection.getPostGreSQLConnection().createStatement();
                            statement.execute(optimize);
                        }
                        catch (SQLException e)
                        {
                            System.err.println(Local.getString("StatisticsUpdatingError"));
                            e.printStackTrace(System.err);
                        }
                        
                        output.addString(Local.getString("StatisticsSuccessfullyUpdated"));
                    }
                    else
                        output.addString(Local.getString("LowCostBenefitRelation") + " " + 
                            aggregationName);
                }
            }
            else
            {
                // END CUBE PROCESSING
                // Faz a combinação dos itens ja existentes na pilha com os atributos 
                // da proxima dimensao, até que o nr de atributos em uma pilha seja 
                // igual ao nr de dimensoes (ou seja, completou a linha de atributos de 
                // todas as dimensoes possiveis)
                
                int nextDimension = activeStack.size();
                Dimension activeDimension = (Dimension) dimensionList.get(nextDimension);
                levelNumber = activeDimension.getMinimumHierarchicalRank();
               
                for (int i = levelNumber; i <= Cube.MAX_LEVEL; i++)
                {
                    Stack s = new Stack();
                    //s.push(new Integer(0));
                    for (j = 0; j < activeStack.size(); j++)
                        s.push(activeStack.get(j));//copy the contents of activeStack to s
                    s.push(new Integer(i));
                    stackList.add(s);
                }
                //**adds also the 0 level to a Stack and then adds this new Stack to stackList
                Stack stackTemp = new Stack();
                for (j = 0; j < activeStack.size(); j++){ //copy the contents of activeStack to stackTemp
                      stackTemp.push(activeStack.get(j));
                }
                stackTemp.push(new Integer(0));
                stackList.add(stackTemp);
          
                /**
                // A dimensao ativa (aquela com a qual serão feitas as combinações) 
                // é obtida pela contagem de atributos da PilhaAtiva, somando-se um
                int stackAttributesNumber = activeStack.size();
                Dimension activeDimension = (Dimension) dimensionList.get(stackAttributesNumber);
                // quantity of levels of current dimension
                levelNumber = activeDimension.getMinimumHierarchicalRank();
                Stack newStack = new Stack();
                // copies activeStack contents to new stack 
                for (int i = 0; i < activeStack.size(); i++)
                    newStack.push(activeStack.get(i));
                
                newStack.push(new Integer(0));
                //stackList.push(newStack);
                
                for (int i = levelNumber; i <= Cube.MAX_LEVEL; i++)
                {
                    // runs all levels of current dimension until level 9
                    // (zero was inserted to minor granularities be processed
                    // before and new aggregations can base it on them, and not	
                    // on base to be formed)
                    newStack = new Stack();
                    for (int k = 0; k < activeStack.size() - 1; k++)
                        newStack.push(activeStack.get(k));
                    newStack.push(new Integer(i));
                    stackList.add(newStack);                    
                }
                **/
            }
        }
        
        output.addString(Local.getString("EndOfCubeProcessing"));
    }
    
    // Esta função verifica, para dado conj. de atributos, a melhor (ou menos 
    // custosa) agregação sobre a qual ele deve ser obtido (em último caso 
    // sobre a base), ou mesmo SE deve ser gerada (caso não seja necessária 
    // sua geração, retorna uma agregação vazia) 
    public Aggregation verifyAggregationCost(Map map)
    {
        Aggregation aggregation = this.queryNavigator(map, false);
        Dimension dimension = null;
        String factName = "", sql = "";
        int tupleCount = 0;
        // If aggregation is basic, returns fact table 
        if (aggregation.isBasic())
        {
            dimension = this.getFactDimension();
            factName = dimension.getName();
            sql = "SELECT COUNT(*) FROM " + factName;
        }
        else
            sql = "SELECT COUNT(*) FROM " + aggregation.getName();
        
        try
        {
            Statement statement = DBConnection.getPostGreSQLConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            resultSet.next();
            tupleCount = resultSet.getInt(1);
            resultSet.close();
        }
        catch (SQLException e)
        {
            System.out.println(Local.getString("UnknownSQLException"));
            e.printStackTrace(System.err);
        }
        
        // verifies query cost by some criterium; here is used record count
        if (tupleCount <= this.minimumAggregation)
            return new Aggregation();
        else
            return aggregation;
    }
    
    public String toString()
    {
        return this.getName();
    }
}