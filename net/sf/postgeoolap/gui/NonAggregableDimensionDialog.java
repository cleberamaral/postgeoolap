package net.sf.postgeoolap.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Map;

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


public class NonAggregableDimensionDialog extends OkCancelDialog 
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
    
    public NonAggregableDimensionDialog()
    {
        super();
        this.guiMount();
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
    }
    
    protected void build()
    {
        JPanel panel = this.getPanel();
        
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 3, 3);
        panel.add(this.tableLabel, gbc);
                
        gbc.gridy = 1;
        gbc.gridheight = 3;
        gbc.insets = new Insets(3, 10, 10, 3);
        panel.add(new JScrollPane(this.tableList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(this.doButton, gbc);
        
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 3, 3, 10);
        panel.add(this.attributeLabel, gbc);
        
        gbc.gridy = 1;
        gbc.gridheight = 3;
        gbc.insets = new Insets(3, 3, 10, 10);
        panel.add(new JScrollPane(this.attributeList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), gbc);
    }
    
    protected void okAction(ActionEvent e) 
	{
        String connection = "";
        // connection = this.table.getSQLClauseWithFactDimension(cube.getFactDimension());
        if (!cube.addDimension(this.table.getName(), "NonAggregable", "", connection,
            this.table.getCode()))
        {
            JOptionPane.showMessageDialog(null, Local.getString("DimensionCreationError"));
            return;
        }
    
        // copy all selected table's attributes to new dimension
        Dimension dimension = new Dimension();
        dimension.retrieve(this.cube, this.table.getName()); // retrieve dimension
        Map attributeMap = this.table.getAttributes();
        for (int i = 0; i < this.attributeListModel.size() - 1; i++)
        {
            Attribute attribute = (Attribute) attributeMap.get(this.attributeListModel.get(i));
            if (!dimension.addAttribute(attribute.getName(), attribute.getType(), 
                0, "R", attribute.isGeographic()))
            {
                JOptionPane.showMessageDialog(null, Local.getString("AttributeSavingError"));
                return;
            }
        }
        
        if (JOptionPane.showConfirmDialog(null, Local.getString("SuccessfullyNonAggregableDimensionCreation"),
            "PostGeoOLAP", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == 
            JOptionPane.NO_OPTION)
            this.setVisible(false);
        else
            this.attributeListModel.clear();
	}

	protected void cancelAction() 
	{
	    this.setVisible(false);
	}
	
	protected void tableListAction(ListSelectionEvent e)
	{
	    
	}
	
	protected void doButtonAction(ActionEvent e)
	{
        if (this.tableList.getSelectedIndex() == -1) // list empty
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
            // okButton.setEnabled(true);
        }	    
	}
	
	// metodos
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
	    if (!table.getName().equals(""))
	    {
	        Map attributeMap = table.getAttributes();
	        this.attributeListModel.clear();
	        Iterator iterator = attributeMap.keySet().iterator();
	        while (iterator.hasNext())
	        {
	            Attribute attribute = (Attribute) attributeMap.get((String) iterator.next());
	            this.attributeListModel.addElement(attribute.getName());
	        }
	    }
	}
	
	public void selectDimensionTable(Cube cube)
	{
	    this.cube = cube;
	    this.fillTableList();
	    this.setVisible(true);
	}

}
