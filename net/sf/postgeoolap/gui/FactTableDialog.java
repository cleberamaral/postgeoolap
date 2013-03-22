package net.sf.postgeoolap.gui;

import java.awt.Component;
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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.postgeoolap.locale.Local;
import net.sf.postgeoolap.model.Attribute;
import net.sf.postgeoolap.model.Cube;
import net.sf.postgeoolap.model.Table;

public class FactTableDialog extends OkCancelDialog
{
    private Cube cube;
    private Table table;
    private Map tableMap;
    
    private JLabel tableLabel;
    private JLabel attributeLabel;
    private JButton pointButton;
    private JList tableList;
    private DefaultListModel tableListModel;
    private JList attributeList;
    private DefaultListModel attributeListModel; 
    
    public FactTableDialog()
    {
        super(Local.getString("SelectFactTable"));
        this.cube = new Cube();
        this.table = new Table();
        this.tableMap = new TreeMap();
        this.guiMount();
    }
    
    protected void initialize()
    {
        super.initialize();
        
        this.tableLabel = new JLabel(Local.getString("Tables"));
        this.attributeLabel = new JLabel(Local.getString("Attributes"));
        this.pointButton = new JButton(">>>");        	
        this.pointButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    pointAction();
                }
            }
        );
        
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
        
        this.attributeListModel = new DefaultListModel();
        this.attributeList = new JList(this.attributeListModel);
        
        this.tableList.setName("tableList");
        this.tableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.tableList.setCellRenderer(new SelectFactTableCellRenderer());
        
        this.attributeList.setName("attributeList");
        this.attributeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.attributeList.setCellRenderer(new SelectFactTableCellRenderer());
        
        this.addWindowFocusListener(
            new WindowAdapter()
            {
                public void windowGainedFocus(WindowEvent event)
                {
                    FactTableDialog.this.fillTableList();
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
        gbc.insets = new Insets(3, 10, 10, 3);
        panel.add(new JScrollPane(this.tableList, 
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(this.pointButton, gbc);
        
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 3, 3, 10);
        panel.add(this.attributeLabel, gbc);
        
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 10, 10);
        panel.add(new JScrollPane(this.attributeList, 
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), gbc);

    }
    
    protected void okAction(ActionEvent e)
    {
        FactItemSelectionDialog factItemSelectionDialog = new FactItemSelectionDialog();
        factItemSelectionDialog.selectFactItem(this.cube, this.table);
        this.setVisible(false);
    }
    
    protected void cancelAction()
    {
        this.setVisible(false);
    }
    
    private void pointAction()
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
    
    private void tableListAction(ListSelectionEvent e)
    {
    }
    
    // métodos
    private void fillTableList()
    {
        this.tableMap = this.cube.getSchema().getTables();
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
        if (!this.table.getName().equals(""))
        {
            this.attributeListModel.clear();
            Map attributeMap = this.table.getAttributes();
            Iterator iterator = attributeMap.keySet().iterator();
            while (iterator.hasNext())
            {
                Attribute attribute = (Attribute) attributeMap.get((String) iterator.next());
                this.attributeListModel.addElement(attribute.getName());
            }
        }
    }
    
    public void selectFactTable(Cube cube)
    {
    	this.cube = cube;
    	this.setVisible(true);
    }    
}


class SelectFactTableCellRenderer extends JLabel implements ListCellRenderer
{
    private ImageIcon tableIcon = new ImageIcon(ImageIcon.class.getResource("/net/sf/postgeoolap/gui/resources/table.GIF"));
    private ImageIcon attributeIcon = new ImageIcon(ImageIcon.class.getResource("/net/sf/postgeoolap/gui/resources/attribute.GIF"));
    
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus)
    {
        String s = value.toString();
        this.setText(s);
        if (list.getName() != null)
        {
	        if (list.getName().equals("tableList"))
	            this.setIcon(tableIcon);
	        else if (list.getName().equals("attributeList"))
	            this.setIcon(attributeIcon);
        }
	        
        
        if (isSelected)
        {
            this.setBackground(list.getSelectionBackground());
            this.setForeground(list.getSelectionForeground());
        }
        else
        {
            this.setBackground(list.getBackground());
            this.setForeground(list.getForeground());
        }
        
        this.setEnabled(list.isEnabled());
        this.setFont(list.getFont());
        this.setOpaque(true);
           
        return this;
    }
    
}