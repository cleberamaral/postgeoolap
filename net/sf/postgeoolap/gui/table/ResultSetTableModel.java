package net.sf.postgeoolap.gui.table;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.swing.table.AbstractTableModel;

/*
 *  This class is based on example code from Horstmann and Cornell's 
 *  Core Java 2, vol. 2.
 */

public abstract class ResultSetTableModel extends AbstractTableModel
{
    private ResultSet resultSet;
    private ResultSetMetaData resultSetMetaData;
    
    public ResultSetTableModel(ResultSet resultSet)
    {
        this.resultSet = resultSet;
        try
        {
            this.resultSetMetaData = this.resultSet.getMetaData();
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.err);
        }
    }
    
    public String getColumnName(int column)
    {
        try
        {
            return this.resultSetMetaData.getColumnName(column + 1);
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.err);
            return " ";
        }
    }
    
    public int getColumnCount()
    {
        try
        {
            return this.resultSetMetaData.getColumnCount();
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.err);
            return 0;
        }
    }
    
    protected ResultSet getResultSet()
    {
        return this.resultSet;
    }
}
