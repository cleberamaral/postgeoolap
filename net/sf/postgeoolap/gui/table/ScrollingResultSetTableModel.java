package net.sf.postgeoolap.gui.table;

import java.sql.ResultSet;
import java.sql.SQLException;

/*
 *  This class is based on example code from Horstmann and Cornell's 
 *  Core Java 2, vol. 2.
 */

public class ScrollingResultSetTableModel extends ResultSetTableModel
{
    public ScrollingResultSetTableModel(ResultSet resultSet)
    {
        super(resultSet);
    }
    
    public Object getValueAt(int row, int column)
    {
        try
        {
            ResultSet resultSet = this.getResultSet();
            resultSet.absolute(row + 1);
            return resultSet.getObject(column + 1);
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.err);
            return null;
        }
    }
    
    public int getRowCount()
    {
        try
        {
            ResultSet resultSet = this.getResultSet();
            resultSet.last();
            return resultSet.getRow();
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.err);
            return 0;
        }
    }

}
