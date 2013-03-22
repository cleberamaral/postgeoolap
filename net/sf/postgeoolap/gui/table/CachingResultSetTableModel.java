package net.sf.postgeoolap.gui.table;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/*
 *  This class is based on example code from Horstmann and Cornell's 
 *  Core Java 2, vol. 2.
 */

public class CachingResultSetTableModel extends ResultSetTableModel
{
    private List cache;

    public CachingResultSetTableModel(ResultSet _resultSet)
    {
        super(_resultSet);
        try
        {
            this.cache = new ArrayList();
            int columns = this.getColumnCount();
            ResultSet resultSet = this.getResultSet();

            /* inserts all data on a list of Object arrays, each array 
             * representing one row of data set */
            while (resultSet.next())
            {
                Object[] row = new Object[columns];
                for (int j = 0; j < row.length; j++)
                    row[j] = resultSet.getObject(j + 1);
                this.cache.add(row);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.err);
        }
    }

    public Object getValueAt(int row, int column)
    {
        if (row < this.cache.size())
            return ((Object[]) this.cache.get(row))[column];
        else
            return null;
    }
    
    public int getRowCount()
    {
        return this.cache.size();
    }
}
