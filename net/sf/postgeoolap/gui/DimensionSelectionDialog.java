package net.sf.postgeoolap.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.postgeoolap.locale.Local;
import net.sf.postgeoolap.model.Attribute;
import net.sf.postgeoolap.model.Cube;
import net.sf.postgeoolap.model.Dimension;
import net.sf.postgeoolap.model.Table;

public class DimensionSelectionDialog extends OkCancelDialog
{
    private Cube cube;
    private Table table;
    private Map tableMap;
    
    private JLabel tableLabel;
    private JList tableList;
    private DefaultListModel tableListModel;
    private JLabel attributeLabel;
    private JList attributeList;
    private DefaultListModel attributeListModel;
    private JButton doButton;
    
    public DimensionSelectionDialog()
    {
        super(Local.getString("SelectDimension"));
        this.cube = new Cube();
        this.table = new Table();
        this.tableMap = new TreeMap();
        this.guiMount();
        this.pack();
    }
    
    protected void initialize()
    {
        super.initialize();
        
        this.tableLabel = new JLabel(Local.getString("Tables"));
        this.tableListModel = new DefaultListModel();
        this.tableList = new JList(this.tableListModel);
        this.tableList.addListSelectionListener( 
            new ListSelectionListener()
            {
                public void valueChanged(ListSelectionEvent e)
                {
                    tableListAction(e);
                }
            }
        );
  
        this.attributeLabel = new JLabel(Local.getString("Attributes"));
        this.attributeListModel = new DefaultListModel();
        this.attributeList = new JList(this.attributeListModel);
        
        this.doButton = new JButton(">>>");
        this.doButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    doButtonAction(e);
                }
            }
        );
        
        this.addWindowFocusListener(
            new WindowAdapter()
            {
                public void windowGainedFocus(WindowEvent event)
                {
                    DimensionSelectionDialog.this.fillTableList();
                }
            }
        );
    }
    
    protected void build()
    {
        JPanel panel = this.getPanel();
        
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 4, 10);
        panel.add(this.tableLabel, gbc);
        
        gbc.gridy = 1;
        gbc.insets = new Insets(4, 10, 10, 4);
        panel.add(new JScrollPane(this.tableList, 
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 4, 10, 4);
        panel.add(this.doButton, gbc);
        
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 4, 4, 10);
        panel.add(this.attributeLabel, gbc);
        
        gbc.gridy = 1;
        gbc.insets = new Insets(4, 4, 10, 10);
        panel.add(new JScrollPane(this.attributeList, 
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), gbc);
    }

    protected void okAction(ActionEvent e)
    {
        // Creates dimension
        String connectionString = 
            this.table.getSQLClauseWithFactDimension(this.cube.getFactDimension());
        
        if (!this.cube.addDimension(this.table.getName(), "Dimension", "", 
            connectionString, this.table.getCode()))
        {
            JOptionPane.showMessageDialog(null, Local.getString("DimensionCreationError"));
            return;
        }
        
        // Copy all attributes from selected table to new dimension
        Dimension dimension = new Dimension();
        dimension.retrieve(this.cube, this.table.getName());
        Map attributeMap = new TreeMap();
        attributeMap = this.table.getAttributes();
        for (int i = 0; i < this.attributeListModel.size(); i++)
        {
            Attribute attribute = (Attribute) attributeMap.get(this.attributeListModel.get(i));
            if (!dimension.addAttribute(attribute.getName(), attribute.getType(), 0, 
                "R", attribute.isGeographic()))
            {
                JOptionPane.showMessageDialog(null, Local.getString("AttributeSavingError"));
                return;
            }
        }
        
        if (JOptionPane.showConfirmDialog(null, Local.getString("SuccessfullyDimensionCreation"),
            "PostGeoOLAP", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == 
            JOptionPane.YES_OPTION)
        {
            // Shows next form, to determine the hierarchy within dimension.
            HierarchySelectionDialog dimensionHierarchyLevelDialog = 
                new HierarchySelectionDialog();
            dimensionHierarchyLevelDialog.selectDimensionLevel(this.cube, dimension);
        }
    }

    protected void cancelAction()
    {
        this.setVisible(false);
    }
    
    protected void doButtonAction(ActionEvent e)
    {
        if (this.tableList.getSelectedIndex() == -1)
        {
            this.table = new Table();
            JOptionPane.showMessageDialog(null, Local.getString("SelectATable"));
            this.tableList.requestFocus();
        }
        else
        {
            String name = (String) this.tableList.getSelectedValue();
            this.table = (Table) this.tableMap.get(name);
            this.fillAttributeList();
        }
    }
    
    protected void tableListAction(ListSelectionEvent e)
    {
        
    }
    
    // métodos
    
    private void fillTableList()
    {
        this.tableMap = cube.getSchema().getTables();
        this.tableListModel.clear();
        Iterator iterator = this.tableMap.keySet().iterator();
        while (iterator.hasNext())
        {
            Table table = (Table) this.tableMap.get((String) iterator.next());
            this.tableListModel.addElement(table.getName());
        }
    }
    
    private void fillAttributeList()
    {
        if (this.table.getName().equals(""))
            return;
        
        this.attributeListModel.clear();
        Map treeMap = this.table.getAttributes();
        Iterator iterator = treeMap.keySet().iterator();
        while (iterator.hasNext())
        {
            Attribute attribute = (Attribute) treeMap.get((String) iterator.next());
            this.attributeListModel.addElement(attribute.getName());
        }
    }
    
    public void selectDimensionTable(Cube cube)
    {
        this.cube = cube;
        this.setVisible(true);
    } 

}
