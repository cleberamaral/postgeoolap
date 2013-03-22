package net.sf.postgeoolap.gui;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.SortedSet;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import net.sf.postgeoolap.locale.Local;
import net.sf.postgeoolap.model.Attribute;

public class CriteriaDialog extends OkCancelDialog
{
    private JPanel radioPanel;
    private ButtonGroup buttonGroup;
    private JRadioButton andRadio;
    private JRadioButton orRadio;
    private JRadioButton andNotRadio;
    private JRadioButton orNotRadio;
    
    private JLabel operatorLabel;
    private JComboBox operatorCombo;
    
    private JTextArea criteriaArea;
    
    private JList criteriaList;
    private DefaultListModel listModel;
    private JScrollPane criteriaScroll;
    
    private JLabel whereLabel;
    
    private Attribute attribute;
    private String formerClause;
    private String currentClause;
    
    public CriteriaDialog()
    {
        super(Local.getString("SpecifyCriteria"));
        this.guiMount();
    }
    
    public CriteriaDialog(String what)
    {
        super(Local.getString("SpecifyCriteria") + what);
        this.guiMount();
    }
    
    protected void initialize()
    {
        super.initialize();
        
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        this.radioPanel = new JPanel();
        this.buttonGroup = new ButtonGroup();
        this.andRadio = new JRadioButton(Local.getString("AND"));
        this.orRadio = new JRadioButton(Local.getString("OR"));
        this.andNotRadio = new JRadioButton(Local.getString("ANDNOT"));
        this.orNotRadio = new JRadioButton(Local.getString("ORNOT"));
        this.buttonGroup.add(this.andRadio);
        this.buttonGroup.add(this.orRadio);
        this.buttonGroup.add(this.andNotRadio);
        this.buttonGroup.add(this.orNotRadio);
        
        this.andRadio.setSelected(true);
        
        this.operatorLabel = new JLabel(Local.getString("ComparatingOperator"));
        this.operatorCombo = new JComboBox(new String[] {"=", ">", "<", ">=", "<=", "LIKE"});
        
        this.criteriaArea = new JTextArea(Local.getString("AddingCriteria"), 4, 40);
              
        this.listModel = new DefaultListModel();
        this.criteriaList = new JList(this.listModel);
        this.criteriaScroll = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.criteriaScroll.getViewport().setView(this.criteriaList);
        this.criteriaList.addMouseListener(
            new MouseAdapter()
            {
                public void mouseClicked(MouseEvent mouseEvent)
                {
                    if (mouseEvent.getClickCount() == 2)
                        CriteriaDialog.this.chooseSelectedCriterion();
                }
            }
        );
        
        this.whereLabel = new JLabel("   ");
    }
    
    protected void build()
    {
        this.radioPanel.setBorder(
            BorderFactory.createTitledBorder(Local.getString("LogicalOperator")));
        
        this.radioPanel.setLayout(new GridLayout(1, 4));
        this.radioPanel.add(this.andRadio);
        this.radioPanel.add(this.orRadio);
        this.radioPanel.add(this.andNotRadio);
        this.radioPanel.add(this.orNotRadio);
        
        JPanel panel = this.getPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        int row = 0;
        
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(10, 10, 4, 10);
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(this.radioPanel, gbc);
        
        JPanel operatorPanel = new JPanel();
        operatorPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        operatorPanel.add(this.operatorLabel);
        operatorPanel.add(this.operatorCombo);
        
        gbc.gridy = row++;
        gbc.insets = new Insets(4, 10, 4, 10);
        panel.add(operatorPanel, gbc);
        
        this.criteriaArea.setLineWrap(true);
        this.criteriaArea.setWrapStyleWord(true);
        this.criteriaArea.setBackground(panel.getBackground());
        this.criteriaArea.setEditable(false);
        this.criteriaArea.setFont(this.andRadio.getFont());
        gbc.gridy = row++;
        panel.add(this.criteriaArea, gbc);
        
        gbc.gridy = row++;
        panel.add(this.criteriaScroll, gbc);
        
        gbc.gridy = row++;
        gbc.insets = new Insets(4, 10, 10, 10);
        JPanel wherePanel = new JPanel();
        wherePanel.setBorder(BorderFactory.createBevelBorder(1));
        panel.add(wherePanel, gbc);
        
        wherePanel.setLayout(new GridBagLayout());
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        wherePanel.add(this.whereLabel, gbc);
    }

    protected void okAction(ActionEvent e)
    {
        this.setVisible(false);
    }

    protected void cancelAction()
    {
        this.setVisible(false);
        this.currentClause = "";
    }

    public String addWhereClause(Attribute attribute, String formerClause)
    {
        this.attribute = attribute;
        if (formerClause == null)
            this.radioPanel.setEnabled(false);
        else if (formerClause.equals(""))
            this.radioPanel.setEnabled(false);
        else
            this.formerClause = formerClause;
        
        this.operatorCombo.setSelectedItem("=");
        this.setTitle(this.getTitle() + attribute.getName());
        this.fillList();
        this.setVisible(true);
        return this.currentClause;
    }
    
    private void fillList()
    {
        this.listModel.clear();
        SortedSet sortedSet = this.attribute.getAttributeInstanceSet();
        for (Iterator iterator = sortedSet.iterator(); iterator.hasNext();)
            this.listModel.addElement(iterator.next());        
    }
    
    private void chooseSelectedCriterion()
    {
        String selectedItem = (String) this.criteriaList.getSelectedValue();
        if ("".equals(this.formerClause) || this.formerClause == null)
            this.currentClause = this.attribute.getName() + " " + 
            	(String) operatorCombo.getSelectedItem() + " '" + 
            	selectedItem + "' ";
        else if (this.buttonGroup.isSelected(this.andRadio.getModel()))
            this.currentClause = " AND " + this.attribute.getName() + " " + 
        		(String) operatorCombo.getSelectedItem() + " '" + 
        		selectedItem + "' ";
        else if (this.buttonGroup.isSelected(this.orRadio.getModel()))
            this.currentClause = " OR " + this.attribute.getName() + " " + 
        		(String) operatorCombo.getSelectedItem() + " '" + 
        		selectedItem + "' ";
        else if (this.buttonGroup.isSelected(this.andNotRadio.getModel()))
            this.currentClause = " AND NOT " + this.attribute.getName() + " " + 
        		(String) operatorCombo.getSelectedItem() + " '" + 
        		selectedItem + "' ";
        else if (this.buttonGroup.isSelected(this.orNotRadio.getModel()))
            this.currentClause = " OR NOT " + this.attribute.getName() + " " + 
        		(String) operatorCombo.getSelectedItem() + " '" + 
        		selectedItem + "' ";
        
        this.whereLabel.setText(this.currentClause);
    }
}