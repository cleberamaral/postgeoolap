package net.sf.postgeoolap.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.sf.postgeoolap.gui.tree.DimensionTreeCellRenderer;
import net.sf.postgeoolap.locale.Local;
import net.sf.postgeoolap.model.Attribute;
import net.sf.postgeoolap.model.Dimension;

public class GeographicalCriteriaDialog extends OkCancelDialog
{  
    private JTextArea explainArea;
    
    private JPanel radioPanel;
    private JRadioButton andRadio;
    private JRadioButton orRadio;
    private JRadioButton andNotRadio;
    private JRadioButton orNotRadio;
    private ButtonGroup radioGroup;
    
    private JLabel functionLabel;
    private JList functionList;
    private FunctionListModel functionListModel;

    private JPanel operatorPanel;
    private JLabel comparatorLabel;
    private JComboBox comparatorCombo;
    private JLabel valueLabel;
    private JComboBox valueCombo;
    private JLabel distanceLabel;
    private JTextField distanceField;
    
    private JLabel fieldLabel;
    private JTree fieldTree;
    private DefaultTreeModel fieldTreeModel;
    private DefaultMutableTreeNode rootNode;
    
    private JLabel whereLabel;
    private JButton addToWhereButton;
    
    private JPopupMenu popupMenu;
    private JMenuItem criteriaItem;
    
    //private long srid;
    private Attribute attribute;
    private String formerClause;
    private String currentClause;
    private Map dimensionMap;
    private Map geoAttributeMap;
    private String internalWhere;
    
    private boolean canClose = false;
        
    public GeographicalCriteriaDialog()
    {
        super(Local.getString("SpecifyGeographicCriteria"));
        this.guiMount();
    }
    
    public GeographicalCriteriaDialog(String what)
    {
        super(Local.getString("SpecifyGeographicCriteria") + what);
        this.guiMount();
    }
    
    public void initialize()
    {
        super.initialize();
        
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(
            new WindowAdapter()
            {
                public void windowClosing(WindowEvent e)
                {
                    GeographicalCriteriaDialog.this.tryToClose();
                }
            }
        );
        
        this.explainArea = new JTextArea(Local.getString("AddingGeographicCriteria"), 4, 40);
        
        this.radioPanel = new JPanel();
        this.radioPanel.setBorder(BorderFactory.createTitledBorder(Local.getString("LogicalOperator")));
        this.radioGroup = new ButtonGroup();
        this.andRadio = new JRadioButton(Local.getString("AND"));
        this.orRadio = new JRadioButton(Local.getString("OR"));
        this.andNotRadio = new JRadioButton(Local.getString("AND NOT"));
        this.orNotRadio = new JRadioButton(Local.getString("OR NOT"));
        this.radioGroup.add(this.andRadio);
        this.radioGroup.add(this.orRadio);
        this.radioGroup.add(this.andNotRadio);
        this.radioGroup.add(this.orNotRadio);
        
        this.functionLabel = new JLabel(Local.getString("GeographicFunctions"));
        this.functionListModel = new FunctionListModel(
            new String[] {
                "within", 
                "touches",
                "contains",
                "crosses",
                "disjoint",
                "overlaps",
                "equals",
                "intersects",
                "distance",
                "max_distance",
                "Geometry_Gt",
                "Geometry_Left",
                "Geometry_Right",
                "Geometry_Same",
                "Geometry_Overleft",
                "Geometry_Overright"
            }
        );
        this.functionList = new JList(this.functionListModel);
        
        this.operatorPanel = new JPanel();
        this.operatorPanel.setBorder(BorderFactory.createTitledBorder(Local.getString("ComparatingOperators")));
        this.comparatorLabel = new JLabel(Local.getString("Comparator"));
        this.comparatorCombo = new JComboBox(new String [] {"=", ">=", ">", "<=", "<"});
        this.valueLabel = new JLabel(Local.getString("Value"));
        this.valueCombo = new JComboBox(new String [] {"TRUE", "FALSE"});
        this.distanceLabel = new JLabel(Local.getString("Distance"));
        this.distanceField = new JTextField(5);
        
        this.fieldLabel = new JLabel(Local.getString("FieldsForComparating"));
        
        this.rootNode = new DefaultMutableTreeNode("RootNode");
        this.fieldTreeModel = new DefaultTreeModel(this.rootNode);
        this.fieldTree = new JTree(this.fieldTreeModel);
        this.fieldTree.setRootVisible(false);
        this.fieldTree.setCellRenderer(new DimensionTreeCellRenderer());
                
        this.whereLabel = new JLabel(" ");
        this.addToWhereButton = new JButton(Local.getString("AddToWHEREClause"));
        this.addToWhereButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    GeographicalCriteriaDialog.this.addToWhereClause();
                }
            }
        );
        
        this.popupMenu = new JPopupMenu();
        this.criteriaItem = new JMenuItem(Local.getString("Criteria") + "...");
        this.criteriaItem.setMnemonic('C');
        this.criteriaItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 
            InputEvent.CTRL_MASK));
        this.criteriaItem.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    GeographicalCriteriaDialog.this.specifyCriterion();
                }
            }
        );
        this.popupMenu.add(this.criteriaItem);
        // TODO: adicionar este menu a alguém
    }
    
    public void build()
    {
        JPanel panel = this.getPanel();
        
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        this.explainArea.setBackground(panel.getBackground());
        this.explainArea.setLineWrap(true);
        this.explainArea.setWrapStyleWord(true);
        this.explainArea.setEditable(false);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 3, 10);
        panel.add(this.explainArea, gbc);
        
        this.radioPanel.setLayout(new GridLayout(1, 4));
        this.radioPanel.add(this.andRadio);
        this.radioPanel.add(this.orRadio);
        this.radioPanel.add(this.andNotRadio);
        this.radioPanel.add(this.orNotRadio);
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 10, 3, 10);
        panel.add(this.radioPanel, gbc);
        
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 10, 3, 3);
        panel.add(this.functionLabel, gbc);
        
        gbc.gridy = 3;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(this.functionList, 
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), gbc);
        
        gbc.gridx = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(this.getOperatorPanel(), gbc);
        
        gbc.gridy = 4;
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        panel.add(this.addToWhereButton, gbc);
        
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 10);
        panel.add(this.fieldLabel, gbc);
        
        gbc.gridy = 3;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(this.fieldTree, 
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), gbc);
    }
    
    private JPanel getOperatorPanel()
    {
        JPanel panel = this.operatorPanel;
        
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(3, 3, 3, 10);
        panel.add(this.comparatorLabel, gbc);
        
        gbc.gridy = 1;
        panel.add(this.comparatorCombo, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 10, 3, 3);
        panel.add(this.valueLabel, gbc);
        
        gbc.gridy = 1;
        panel.add(this.valueCombo, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(this.distanceLabel, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(this.distanceField, gbc);
        
        return panel;
    }
    
    private void tryToClose()
    {
        if (this.canClose)
        {
            this.setVisible(false);
            this.dispose();
        }
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
    
    // -------------
    
    public String addWhereClause(Attribute attribute, String formerClause, 
        Map dimensionMap, long srid)
    {
        //this.srid = srid;
        this.attribute = attribute;
        this.dimensionMap = dimensionMap;
        if (formerClause.equals(""))
            this.operatorPanel.setEnabled(false);
        else
            this.formerClause = formerClause;
        
        this.functionList.setSelectedValue("within", true);
        this.setTitle(this.getTitle() + this.attribute.getName());
        this.mountTree();
        this.setVisible(true);
        
        return this.currentClause;
    }
    
    private void mountTree()
    {
        // clears tree
        this.rootNode.removeAllChildren();
        
        DefaultMutableTreeNode dimensionTreeNode = new DefaultMutableTreeNode(
            Local.getString("Dimensions"));
        
        Iterator iterator = this.dimensionMap.keySet().iterator();
        while (iterator.hasNext())
        {
            Dimension dimension = (Dimension) this.dimensionMap.get(iterator.next());
            // Checks to show only non-aggregable dimensions
            if (dimension.getType().equals("NonAggregable"))
            {
                DefaultMutableTreeNode dimensionNode = 
                    new DefaultMutableTreeNode(dimension);
                
                Map attributeMap = dimension.getAttributeList();
                Iterator iterator2 = attributeMap.keySet().iterator();
                while (iterator2.hasNext())
                {
                    Attribute attribute = (Attribute) attributeMap.get(iterator2.next());
                    DefaultMutableTreeNode attributeNode = 
                        new DefaultMutableTreeNode(attribute);
                    dimensionNode.add(attributeNode);
                    
                  // fills geoAttributeMap
                    this.geoAttributeMap.put(attribute.getName(), attribute);
                }
                dimensionTreeNode.add(dimensionNode);
            }
        }
        
        this.rootNode.add(dimensionTreeNode);
    }
    
    private void addToWhereClause()
    {
        Object object = null;
        if (!this.checkTree(object))
            return;
        
        Attribute attribute = (Attribute) object;
        if (!attribute.isGeographic())
        {
            JOptionPane.showMessageDialog(null, Local.getString("GeographicComparations"));
            return;
        }
        
        double distanceValue;
        try
        {
            distanceValue = Integer.parseInt(this.distanceField.getText());
        }
        catch (NumberFormatException e)
        {
            this.distanceField.setText("0");
            distanceValue = 0;
        }
        
        String geoQuery = mountGeoQuery((String) this.functionList.getSelectedValue(), 
        	attribute, (String) this.comparatorCombo.getSelectedItem(),
        	distanceValue); 
        if (this.formerClause.equals(""))
            this.currentClause = "(" + geoQuery + ")";
        else
        {
            if (this.radioGroup.isSelected(this.andRadio.getModel())) 
                this.currentClause = " AND (";
            else if (this.radioGroup.isSelected(this.orRadio.getModel())) 
                this.currentClause = " OR (";
            else if (this.radioGroup.isSelected(this.andNotRadio.getModel())) 
                this.currentClause = " AND NOT (";
            else if (this.radioGroup.isSelected(this.orNotRadio.getModel())) 
                this.currentClause = " OR NOT (";
            this.currentClause += geoQuery + ")";
        }
        
        this.whereLabel.setText(this.currentClause);
    }
    
    private String mountGeoQuery(String functionName, Attribute geoAttribute, 
        String distanceComparator, double distance)
    {
        String sql;
        if (functionName.equals("distance"))
            sql = "distance( " + this.attribute.getName() + ", (select " +
            	geoAttribute.getName() + " from " + geoAttribute.getTable().getName() + 
            	" " + this.internalWhere + " )) " + distanceComparator + " " + 
            	distance;
        else if (functionName.equals("max_distance"))
            sql = "max_distance( " + this.attribute.getName() + ", (select " + 
            	geoAttribute.getName() + " from " + geoAttribute.getTable().getName() + 
            	" " + this.internalWhere + " )) " + distanceComparator + " " +
            	distance;
        else
            sql = functionName + "( " + this.attribute.getName() + ", (select " + 
            	geoAttribute.getName() + " from " + geoAttribute.getTable().getName() + 
            	" " + this.internalWhere + " )) = " + 
            	(String) this.comparatorCombo.getSelectedItem();
        
        return sql;
    }
    
    /*
    private String mountGeoQuery(String functionName, Attribute geoAttribute)
    {
        return this.mountGeoQuery(functionName, geoAttribute, "=", 0);
    }
    */
    private void specifyCriterion()
    {
        Object object = null;
        if (!this.checkTree(object))
            return;
        
        Attribute attribute = (Attribute) object;
        
        if (attribute.isGeographic())
        {
            JOptionPane.showMessageDialog(null, Local.getString("SelectNonGeographicAttributes"));
            return;
        }
        
        CriteriaDialog specifyCriteriaDialog = new CriteriaDialog();
        String newWhere = specifyCriteriaDialog.addWhereClause(attribute, this.internalWhere);
        if (newWhere.equals(""))
            return;
        
        // If something was added, add attribute to query collection
        if (this.internalWhere.equals(""))
            this.internalWhere = " where " + newWhere;
        else
            this.internalWhere += " " + newWhere;
        this.whereLabel.setText(this.internalWhere);
    }

    // Checks if there is selection and if selected item is an Attribute
    private boolean checkTree(Object object)
    {
        if (this.fieldTree.getLastSelectedPathComponent() == null)
            return false;
        DefaultMutableTreeNode selectedNode = 
            (DefaultMutableTreeNode) this.fieldTree.getLastSelectedPathComponent();
        object = selectedNode.getUserObject();
        if (!(object instanceof Attribute))
        {
            JOptionPane.showMessageDialog(null, Local.getString("SelectAnAttribute"));
            return false;
        }
        return true;
    }
}

class FunctionListModel extends AbstractListModel
{
    public List list;
    
    public FunctionListModel()
    {
        this(null);
    }
    
    public FunctionListModel(Object[] o)
    {
        super();
        this.list = new ArrayList();
        if (o != null && o.length > 0)
        {
            for (int i = 0; i < o.length; i++)
                this.list.add(o[i]);
            this.fireIntervalAdded(this, 1, this.getSize());
        }
    }
    
    public Object getElementAt(int n)
    {
        return this.list.get(n);
    }
    
    public int getSize()
    {
        return this.list.size(); 
    }
    
    public void addElement(Object element)
    {
        this.list.add(element);
        this.fireIntervalAdded(this, this.getSize() - 1, this.getSize());
    }
    
    public void removeElement(Object element)
    {
        int n = this.list.indexOf(element);
        this.removeElement(n);
    }
    
    public void removeElement(int n)
    {
        this.list.remove(n);
        this.fireIntervalRemoved(this, n-1, n);
    }
}