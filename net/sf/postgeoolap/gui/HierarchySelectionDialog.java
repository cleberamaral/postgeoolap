package net.sf.postgeoolap.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;

import net.sf.postgeoolap.locale.Local;
import net.sf.postgeoolap.model.Attribute;
import net.sf.postgeoolap.model.Cube;
import net.sf.postgeoolap.model.Dimension;

public class HierarchySelectionDialog extends OkCancelDialog
{
    private Dimension dimension;
    //private Cube cube;
    
    private JTextArea explainArea;
    private JTable hierarchyTable;
    private HierarchyTableModel hierarchyTableModel;
    private JScrollPane scroll;
        
    public HierarchySelectionDialog()
    {
        super(Local.getString("SelectHierarchy"));
        this.guiMount();
    }
    
    protected void initialize()
    {
        super.initialize();
       
        this.explainArea = new JTextArea(Local.getString("SelectHierarchyText"), 4, 40);
        
        this.hierarchyTable = new JTable(new HierarchyTableModel(new TreeMap()));
        this.scroll = new JScrollPane(this.hierarchyTable, 
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }
    
    protected void build()
    { 
        JPanel panel = this.getPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        this.explainArea.setLineWrap(true);
        this.explainArea.setWrapStyleWord(true);
        this.explainArea.setBackground(panel.getBackground());
        this.explainArea.setFont(this.hierarchyTable.getFont());
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 3, 10);
        panel.add(this.explainArea, gbc);
        
        this.hierarchyTable.setCellSelectionEnabled(true);
        this.hierarchyTable.setEditingColumn(1);
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 10, 10, 10);
        panel.add(this.scroll, gbc);
        this.pack();
    }

    protected void okAction(ActionEvent e)
    {
        if (Attribute.saveAttributeSet(this.hierarchyTableModel.getMap()))
            JOptionPane.showMessageDialog(null, Local.getString("HierarchyRecordedSuccessfully"));
        else
            JOptionPane.showMessageDialog(null, Local.getString("HierarchyRecordingError"));
        this.setVisible(false);
        this.dispose();
    }

    protected void cancelAction()
    {
        this.setVisible(false);
        this.dispose();
    }
    
    // Methods
    
    public void selectDimensionLevel(Cube cube, Dimension dimension)
    {
        //this.cube = cube;
        this.dimension = dimension;
        Map attributeMap = Attribute.getAttributeSet(this.dimension);
        
        this.hierarchyTableModel = new HierarchyTableModel(attributeMap);
        this.setTitle(Local.getString("SelectHierarchy") + " - " + 
            Local.getString("Dimension") + ": " + this.dimension.getName());
        this.hierarchyTable.setModel(this.hierarchyTableModel);
        this.setVisible(true);
    }

}

class HierarchyTableModel extends AbstractTableModel
{
    private Map map;
    private List col1, col2;
    
    public HierarchyTableModel(Map map)
    {
        this.col1 = new ArrayList();
        this.col2 = new ArrayList();
        this.setMap(map);
    }
    
    public Map getMap()
    {
        this.map.clear();
        for (int i = 0; i < this.col1.size(); i++)
            this.map.put(this.col1.get(i), this.col2.get(i));
        return this.map;
    }
    
    public void setMap(Map map)
    {
        this.map = map;
        this.col1.clear();
        this.col2.clear();
        for (Iterator i = map.keySet().iterator(); i.hasNext(); )
        {
            Object key = i.next(); 
            this.col1.add(key);
            this.col2.add(map.get(key));
        }
    }
    
    public int getRowCount()
    {
        return map.size(); 
    }
    
    public int getColumnCount()
    {
        return 2;
    }
    
    public void setValueAt(Object value, int row, int col)
    {
        if (col == 0)
            return;
        List list = this.col2;
        Attribute attribute = (Attribute) list.remove(row);
        attribute.setLevel(Integer.parseInt((String) value));
        list.add(row, attribute);
    }
    
    public Object getValueAt(int row, int col)
    {
        return (col == 0) ?
            this.col1.get(row) :
            Integer.toString(((Attribute) this.col2.get(row)).getLevel());
    }

    public String getColumnName(int col)
    {
        if (col == 0)
            return Local.getString("Name");
        else if (col == 1)
            return Local.getString("HierarchicLevel");
        else
            return null;
    }
    
    public boolean isCellEditable(int row, int col)
    {
        return col == 1;     
    }
}