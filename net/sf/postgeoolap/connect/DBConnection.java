package net.sf.postgeoolap.connect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JOptionPane;

import net.sf.postgeoolap.locale.Local;
import net.sf.postgeoolap.model.Schema;


public class DBConnection
{
    private static Connection postGreSQLConnection = null;
    private static Connection metaDataConnection = null;
    
    public static Connection getPostGreSQLConnection()
    {
        if (postGreSQLConnection == null)
            JOptionPane.showMessageDialog(null, Local.getString("SchemaConnectionError"));
        return postGreSQLConnection;
    }
    
    public static boolean configPostGreSQLConnection(Schema schema)
    {
        // opening postgresql database
        String database;
        String userName = schema.getUser();
        String password = schema.getPassword();
        String dbServer = schema.getServer();
        String dbName = schema.getName();
        
        Connection connection;
        database = "jdbc:postgresql://" + dbServer + ":5432/" + dbName;
        
        try
        {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(database, userName, password); 
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace(System.err);
            return false;
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.err);
            return false;
        }
        
        postGreSQLConnection = connection;        
        return true;
    }
    
    public static boolean checkPostGreSQLConnection(Schema schema)
    {
        String database;
        String userName = schema.getUser();
        String password = schema.getPassword();
        String dbServer = schema.getServer();
        String dbName = schema.getName();
        
        database = "jdbc:postgresql://" + dbServer + ":5432/" + dbName;
        
        try
        {
            Class.forName("org.postgresql.Driver");
            DriverManager.getConnection(database, userName, password);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace(System.err);
            return false;
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.err);
            return false;
        }
        
        return true;
    }
    
/*    
    // metadata are stored in XML and accessed using Ashpool XML Database
    // (http://ashpool.sourceforge.net) 
    public static Connection getMetadataConnection()
    {
        // tests if metadata connection already exists. if not, create it
        if (metaDataConnection == null)
        {
            Connection connection;
            try
            {
                // loads Ashpool XML JDBC driver
                Class.forName("com.rohanclan.ashpool.jdbc.Driver");
                connection = DriverManager.getConnection(
                    "jdbc:ashpool:file://D:/Mestrado/Dissertação/PostGeoOLAP/source/net/sf/postgeoolap/metadata/xml/", null);               
            }
            catch (ClassNotFoundException e)
            {
                JOptionPane.showMessageDialog(null, Local.getString("CouldNotLoadAccessDriver"));
                e.printStackTrace(System.err);
                return null;
            }
            catch (SQLException e)
            {
                JOptionPane.showMessageDialog(null, Local.getString("CouldNotConnectMetadataDatabase"));
                e.printStackTrace(System.err);
                return null;
            }
            metaDataConnection = connection;
        }
        
        return metaDataConnection;
    }
    */
    
    public static Connection getMetadataConnection()
    {
        // tests if metadata connection already exists. if not, create it
        if (metaDataConnection == null)
        {
            Connection connection;
            try
            {
                // loads PostGreSQL JDBC driver
                Class.forName("org.postgresql.Driver");
                MetadataServer metadata = new MetadataServer();
                String location = metadata.getServerLocation();
                String database = metadata.getDatabaseName();
                String user = metadata.getUserName();
                String password = metadata.getPassword();
                connection = DriverManager.getConnection(
                    "jdbc:postgresql://" + location + ":5432/" + database, user, password);
            }
            catch (ClassNotFoundException e)
            {
                JOptionPane.showMessageDialog(null, Local.getString("CouldNotLoadAccessDriver"));
                e.printStackTrace(System.err);
                return null;
            }
            catch (SQLException e)
            {
                JOptionPane.showMessageDialog(null, Local.getString("CouldNotConnectMetadataDatabase"));
                e.printStackTrace(System.err);
                return null;
            }
            metaDataConnection = connection;
        }
        
        return metaDataConnection;
    }

    
    
    // This method clears orphan metadata tuples
    public static void clearMetadata()
    {
       
        String sql = "DELETE FROM cubo" +
        			 "  WHERE cubecode NOT IN " +
        			 "    (SELECT cubecode FROM dimension)";
        
        try
        {
            Statement statement = DBConnection.getMetadataConnection().createStatement();
            statement.execute(sql);
            statement.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.err);
        }
        
/*       
       // TODO: substituir isso pela consulta SQL acima
        String sql;
        try
        {
            Statement statement = DBConnection.getMetadataConnection().createStatement();
            sql = "SELECT DISTINCT CubeCode FROM Dimension";
            ResultSet resultSet = statement.executeQuery(sql);
            List dimensionList = new ArrayList();
            while (resultSet.next())
                dimensionList.add(new Integer(resultSet.getInt(1)));
            resultSet.close();
            
            sql = "SELECT DISTINCT CubeCode FROM Cube";
            resultSet = statement.executeQuery(sql);
            List cubeList = new ArrayList();
            while (resultSet.next())
                cubeList.add(new Integer(resultSet.getInt(1)));
            resultSet.close();
            
            for (Iterator iterator = cubeList.iterator(); iterator.hasNext(); )
            {
                Integer integer = (Integer) iterator.next();
                if (dimensionList.indexOf(integer) == -1)
                {
                    sql = "DELETE FROM Cube WHERE CubeCode = " + integer.intValue();
                    statement.execute(sql);
                }
            }
        }
        catch (SQLException e)
        {
        }
*/        
    }
    
/*    public static int getNextMetadataTableCode(String table)
    {
        int num;
        try
        {
        	String sql = null;
        	if (!"cube".equals(table))
        		sql = "SELECT MAX(" + table + "code) FROM " + table;
        	else
        		sql = "SELECT MAX(cubecode) FROM cubo";
            Statement st = DBConnection.getMetadataConnection().createStatement();
            ResultSet result = st.executeQuery(sql);
            num = result.next() ? result.getInt(1) + 1 : 1;
            st.close();
            return num;
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.err);
        	return -1;
        }
    }
    */
        
    public static void closeConnections()
    {
        DBConnection.closeConnection(DBConnection.metaDataConnection);
        DBConnection.closeConnection(DBConnection.postGreSQLConnection);
    }
    
    private static void closeConnection(Connection connection)
    {
        try
        {
            if (connection != null)
                if (!connection.isClosed())
                    connection.close();
        }
        catch (SQLException e)
        {
            
        }
    }
}