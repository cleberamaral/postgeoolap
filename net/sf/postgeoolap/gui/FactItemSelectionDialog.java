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

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.sf.postgeoolap.locale.Local;
import net.sf.postgeoolap.model.Attribute;
import net.sf.postgeoolap.model.Cube;
import net.sf.postgeoolap.model.Dimension;
import net.sf.postgeoolap.model.Table;

public class FactItemSelectionDialog extends OkCancelDialog
{
    private Cube cube;
    private Table table;
    
    private JLabel attributeLabel;
    private JList attributeList;
    private DefaultListModel attributeListModel;
    private JScrollPane attributeScrollPane;
    private JButton goButton;
    private JButton backButton;
    private JButton allBackButton;
    private JLabel itemLabel;
    private JList itemList;
    private DefaultListModel itemListModel;
    private JScrollPane itemScrollPane;
    
    public FactItemSelectionDialog()
    {
        super(Local.getString("SelectItemFromFactTable"));
        this.cube = new Cube();
        this.table = new Table();
        this.guiMount();
    }
    
    protected void initialize()
    {
        super.initialize();
        
        this.attributeLabel = new JLabel(Local.getString("FactTableAttributes"));
        this.attributeListModel = new DefaultListModel();
        this.attributeList = new JList(this.attributeListModel);
        this.attributeScrollPane = new JScrollPane();
        this.attributeScrollPane.setViewportView(this.attributeList);
        
        this.goButton = new JButton(">");
        this.goButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    goAction(e);
                }
            }
        );
        this.backButton = new JButton("<");
        this.backButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    backAction(e);
                }
            }
        );
        
        this.allBackButton = new JButton("<<<");
        this.allBackButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    allBackAction(e);
                }
            }
        );
        
        this.itemLabel = new JLabel(Local.getString("NumericalItems"));
        this.itemListModel = new DefaultListModel();
        this.itemList = new JList(this.itemListModel);
        this.itemScrollPane = new JScrollPane();
        this.itemScrollPane.setViewportView(this.itemList);
        
        this.addWindowFocusListener(
            new WindowAdapter()
            {
                public void windowGainedFocus(WindowEvent event)
                {
                    FactItemSelectionDialog.this.fillAttributeList();
                }
            }
        );
        
        this.setOkEnabled(false);
    }
    
    protected void build()
    {
        JPanel panel = this.getPanel();
        
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 3, 10);
        panel.add(this.attributeLabel, gbc);
        
        gbc.gridy = 1;
        gbc.gridheight = 3;
        gbc.insets = new Insets(3, 10, 10, 3);
        panel.add(this.attributeScrollPane, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(this.goButton, gbc);
        
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(this.backButton, gbc);
        
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(this.allBackButton, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 3, 3, 10);
        panel.add(this.itemLabel, gbc);
        
        gbc.gridy = 1;
        gbc.gridheight = 3;
        gbc.insets = new Insets(3, 3, 10, 10);
        panel.add(this.itemScrollPane, gbc);
    }
    
    protected void okAction(ActionEvent e)
    {
        // Creates fact dimension
        if (this.cube.addDimension(this.table.getName(), "Fact", "", "", 
            this.table.getCode()))
        {
            Map attributeMap = this.table.getAttributes();
            Dimension dimension = new Dimension();
            dimension.retrieve(this.cube, this.table.getName());
            
            for (int i = 0; i < this.itemListModel.size(); i++)
            {
                Attribute attribute = (Attribute) attributeMap.get((String) this.itemListModel.get(i));
                if (!dimension.addAttribute(attribute.getName(), attribute.getType(), 
                    0, "S", attribute.isGeographic()))
                {
                    JOptionPane.showMessageDialog(null, Local.getString("FactTableNumericalItemError"));
                    return;
                }
            }
            
            if (JOptionPane.showConfirmDialog(null, Local.getString("NumericalItemsSuccessfullyCreated"), 
                "PostGeoOlap", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
            {
                DimensionSelectionDialog dimensionSelectionDialog = new DimensionSelectionDialog();
                dimensionSelectionDialog.selectDimensionTable(this.cube);
            }
        }
        else
            JOptionPane.showMessageDialog(null, Local.getString("FactTableCreationError"));
        
        this.setVisible(false);
    }

    protected void cancelAction()
    {
        this.setVisible(false);
    }
    
    private void addItems(DefaultListModel listModel, Object [] items)
    {
        for (int i = 0; i < items.length; i ++)
            listModel.addElement(items[i]);
    }
    
    private void removeItems(DefaultListModel listModel, Object [] items)
    {
        for (int i = 0; i < items.length; i++)
            listModel.removeElement(items[i]);
    }

    private void goAction(ActionEvent e)
    {
        if (this.attributeList.getSelectedIndex() != -1)
        {
            Object [] selection = this.attributeList.getSelectedValues();
            this.addItems(this.itemListModel, selection);
            this.removeItems(this.attributeListModel, selection);
            this.validateButtons();
        }
        else
        {
            JOptionPane.showMessageDialog(null, Local.getString("SelectAnAttribute"));
            this.attributeList.requestFocus();
        }
    }
    
    private void backAction(ActionEvent e)
    {
        if (this.itemList.getSelectedIndex() != -1)
        {
            Object [] selection = this.itemList.getSelectedValues();
            this.removeItems(this.itemListModel, selection);
            this.addItems(this.attributeListModel, selection);
            this.validateButtons();
        }
        else
        {
            JOptionPane.showMessageDialog(null, Local.getString("SelectAnAttribute"));
            this.itemList.requestFocus();
        } 
    }
    
    private void allBackAction(ActionEvent e)
    {
        for (int i = 0; i < this.itemListModel.size(); i++)
            this.attributeListModel.addElement(this.itemListModel.get(i));
        this.itemListModel.clear();
        this.validateButtons();
    }
    
    // metodos
    
    public void selectFactItem(Cube cube, Table table)
    {
        this.cube = cube;
        this.table = table;
        this.setVisible(true);
    }
    
    private void fillAttributeList()
    {
        if (!this.table.getName().equals(""))
        {
            Map attributeMap = this.table.getAttributes();
            this.attributeListModel.clear();
            Iterator iterator = attributeMap.keySet().iterator();
            while (iterator.hasNext())
            {
                Attribute attribute = (Attribute) attributeMap.get((String) iterator.next());
                this.attributeListModel.addElement(attribute.getName());
            }
        }
    }
    
    private void validateButtons()
    {
        this.setOkEnabled(this.itemListModel.size() > 0);
    }
}
