package net.sf.postgeoolap.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import net.sf.postgeoolap.gui.table.CachingResultSetTableModel;
import net.sf.postgeoolap.gui.table.ResultSetTableModel;
import net.sf.postgeoolap.gui.tree.DimensionTreeCellRenderer;
import net.sf.postgeoolap.locale.Local;
import net.sf.postgeoolap.model.Aggregation;
import net.sf.postgeoolap.model.Attribute;
import net.sf.postgeoolap.model.Cube;
import net.sf.postgeoolap.model.Dimension;

import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import com.vividsolutions.jump.workbench.ui.GUIUtil;

public class DataAnalysisFrame extends JFrame implements ErrorHandler
{
    private Cube cube;
    private Map dimensionMap; // stores all dimensions of current cube
    private Map attributeMap = new TreeMap(); // stores all attributes for current cube
    private Attribute attribute;// references the attribute selected by query
    // stores collection of attributes to be submitted to query
    private Map queryAttributeMap = new TreeMap();
    // stores the collection of attributes to be showed on grid (same to 
    // queryAttributeList, except those appear only on WHERE clauses)
    private Map selectAttributeMap = new TreeMap();
    private String whereString = ""; // stores WHERE clause to be used on query
    
    private JTree dimensionTree;
    private DefaultTreeModel dimensionTreeModel;
    private DefaultMutableTreeNode rootNode;
   
    private JPanel queryCriteriaPanel;
    private JTextArea queryCriteriaArea;
    private JCheckBox viewMapCheck;
    private JButton executeButton;
    private JButton newQueryButton;
    private JButton displayButton;
    private JTable resultTable;
    private ResultSetTableModel resultSetTableModel;
    private ColumnTableModel columnTableModel;
    
    private MapPanel mapPanel;
    private LayerDisplayFrame layerDisplayFrame = null;
    
    private JPopupMenu popupMenu;
    private JMenuItem criteriaItem;
    private JMenuItem whereItem;
    
    public DataAnalysisFrame()
    {
        super(Local.getString("DataAnalysis"));
        this.initialize();
        this.buildForm();
        this.setSize(800, 600);
        this.setPreferredSize(new java.awt.Dimension(800, 600));
        this.pack();
    }
    
    private void initialize()
    {
        this.rootNode = new DefaultMutableTreeNode("RootNode");
        this.dimensionTreeModel = new DefaultTreeModel(this.rootNode);
        this.dimensionTree = new JTree(this.dimensionTreeModel);
        this.dimensionTree.setRootVisible(false);
        this.dimensionTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        this.dimensionTree.setCellRenderer(new DimensionTreeCellRenderer());
        this.dimensionTree.addMouseListener( 
            new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    if (e.getClickCount() == 2)
                        DataAnalysisFrame.this.dimensionTreeDoubleClick();
                }
            }
        );
        this.dimensionTree.addMouseListener(
                new MouseAdapter()
                {             
                    public void mouseReleased(MouseEvent e)
                    {
                        DataAnalysisFrame.this.showPopupMenu(e);
                    }
                    
                    public void mousePressed(MouseEvent e)
                    {
                        DataAnalysisFrame.this.showPopupMenu(e);
                    }
                }
            );
        
        
        this.queryCriteriaPanel = new JPanel();
        this.queryCriteriaPanel.setBorder(BorderFactory.createTitledBorder(Local.getString("QueryCriteria")));
        this.queryCriteriaArea = new JTextArea();
                
        this.viewMapCheck = new JCheckBox(Local.getString("ShowResultsOnMap"));
        
        this.executeButton = new JButton(Local.getString("Execute"));  
        this.executeButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    DataAnalysisFrame.this.executeQuery();
                }
            }
        );
        
        this.newQueryButton = new JButton(Local.getString("NewQuery"));
        this.newQueryButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    DataAnalysisFrame.this.newQuery();
                }
            }
        );
        
        this.displayButton = new JButton(Local.getString("ShowDisplays"));
        this.displayButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    DataAnalysisFrame.this.showDisplay();
                }
            }
        );

        this.columnTableModel = new ColumnTableModel();
        this.resultTable = new JTable(this.columnTableModel);
        
        this.mapPanel = new MapPanel(this);
        
        this.addWindowListener(
            new WindowAdapter()
            {
                // adding a layer to a LayerManager before LayerViewPanel is visible
                // raises an NonInvertibleTransformException because LVP tries to do
                // a zoom, but his height is zero, resulting in that exception.
                public void windowOpened(WindowEvent e)
                {
                    try
                    {
                        mapPanel.initJUMP();
                    }
                    catch (Throwable t)
                    {
                        handleThrowable(t);
                    }
                }
            }
        );
        
        this.popupMenu = new JPopupMenu();
        this.criteriaItem = new JMenuItem(Local.getString("Criteria") + "...");
        this.criteriaItem.setMnemonic('C');
        this.criteriaItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, InputEvent.CTRL_MASK));
        this.criteriaItem.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    DataAnalysisFrame.this.criteriaItemClick();
                }
            }
        );
        this.popupMenu.add(this.criteriaItem);
        this.whereItem = new JMenuItem(Local.getString("WhereClause") + "...");
        this.whereItem.setMnemonic('W');
        this.whereItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 
            InputEvent.CTRL_MASK));
        this.whereItem.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    DataAnalysisFrame.this.whereItemClick();
                }
            }
        );
        this.popupMenu.add(this.whereItem);
    }
    
    private void buildForm()
    {
        Container container = this.getContentPane();
/*        
        GriddedPanel panel = new GriddedPanel();
        
        JScrollPane dimensionTreeScrollPane = new JScrollPane();
        dimensionTreeScrollPane.getViewport().setView(this.dimensionTree);
        panel.addFilledComponent(dimensionTreeScrollPane, 0, 0, 1, 2, GridBagConstraints.BOTH);
        
        JScrollPane queryCriteriaScrollPane = new JScrollPane();
        queryCriteriaScrollPane.getViewport().setView(this.queryCriteriaArea);
        GriddedPanel criteriaPanel = new GriddedPanel();
        criteriaPanel.setBorder(BorderFactory.createTitledBorder(Local.getString("QueryCriteria")));
        criteriaPanel.addFilledComponent(queryCriteriaScrollPane, 0, 0, 1, 1, GridBagConstraints.BOTH);
        panel.addFilledComponent(criteriaPanel, 0, 2, 1, 1, GridBagConstraints.BOTH);
        
        panel.addFilledComponent(this.mapPanel, 1, 0, 4, 1, GridBagConstraints.BOTH);
        panel.addComponent(this.viewMapCheck, 1, 1, 1, 1);
        panel.addComponent(this.executeButton, 2, 1, 1, 1);
        panel.addComponent(this.newQueryButton, 3, 1, 1, 1);
        panel.addComponent(this.displayButton, 2, 1, 1, 1);
        
        JScrollPane resultTableScrollPane = new JScrollPane();
        resultTableScrollPane.getViewport().setView(this.resultTable);
        panel.addFilledComponent(resultTableScrollPane, 1, 2, 4, 1, GridBagConstraints.BOTH);
        
        container.add(panel);
  */      

        JPanel panel = new JPanel();
        
        JScrollPane dimensionTreeScrollPane = new JScrollPane();
        dimensionTreeScrollPane.getViewport().setView(this.dimensionTree);
        dimensionTreeScrollPane.setPreferredSize(new java.awt.Dimension(194, 344));
        dimensionTreeScrollPane.setSize(194, 344);
        JPanel treePanel = new JPanel();
        treePanel.add(dimensionTreeScrollPane);
        treePanel.setSize(200, 350);
        treePanel.setLocation(0, 0);
        
        JScrollPane resultTableScrollPane = new JScrollPane();
        resultTableScrollPane.getViewport().setView(this.resultTable);
        resultTableScrollPane.setPreferredSize(new java.awt.Dimension(574, 194));
        JPanel tablePanel = new JPanel();
        tablePanel.add(resultTableScrollPane);
        tablePanel.setSize(580, 200);
        tablePanel.setLocation(200,350);
        
        Box buttonBox = Box.createHorizontalBox();
        buttonBox.add(this.viewMapCheck);
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(this.executeButton);
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(this.newQueryButton);
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(this.displayButton);
       
        JPanel mapButtonPanel = new JPanel();
        mapButtonPanel.setLayout(new BorderLayout());
        mapButtonPanel.add(this.mapPanel, BorderLayout.CENTER);
        mapButtonPanel.add(buttonBox, BorderLayout.SOUTH);
        mapButtonPanel.setSize(574, 344);
        mapButtonPanel.setLocation(200, 0);
        
        JScrollPane criteriaScroll = new JScrollPane();
        criteriaScroll.getViewport().setView(this.queryCriteriaArea);
        this.queryCriteriaArea.setPreferredSize(new java.awt.Dimension(194, 244));
        this.queryCriteriaArea.setFont(this.viewMapCheck.getFont());
        this.queryCriteriaPanel.add(criteriaScroll);
        this.queryCriteriaPanel.setSize(200, 250);
        this.queryCriteriaPanel.setLocation(0, 350);
        
        panel.setLayout(null);
        panel.add(treePanel, null);
        panel.add(mapButtonPanel, null);
        panel.add(queryCriteriaPanel, null);
        panel.add(tablePanel, null);
        
        //GridBagLayout gridBagLayout = new GridBagLayout();
        //GridBagConstraints gbc = new GridBagConstraints();
        //panel.setLayout(gridBagLayout);
        
        //OkCancelDialog.addComponent(treePanel, panel, gbc, 0, 0, 1, 1);
        //OkCancelDialog.addComponent(mapButtonPanel, panel, gbc, 1, 0, 1, 1);
        //OkCancelDialog.addComponent(queryCriteriaPanel, panel, gbc, 0, 1, 1, 1);
        //OkCancelDialog.addComponent(tablePanel, panel, gbc, 1, 1, 1, 1);
        
        container.add(panel);
        
/*
        BorderLayout borderLayout = new BorderLayout();
        borderLayout.setHgap(15);
        borderLayout.setVgap(15);
        container.setLayout(borderLayout);
        
        JPanel centerPanel = new JPanel();
        BorderLayout centerBorderLayout = new BorderLayout();
        centerBorderLayout.setHgap(15);
        centerBorderLayout.setVgap(15);
        centerPanel.setLayout(centerBorderLayout);
        
        JPanel mapButtonPanel = new JPanel();
        BorderLayout mapButtonBorderLayout = new BorderLayout();
        mapButtonBorderLayout.setHgap(15);
        mapButtonBorderLayout.setVgap(15);
        mapButtonPanel.setLayout(mapButtonBorderLayout);
        
        Box buttonBox = Box.createHorizontalBox();
        buttonBox.add(this.viewMapCheck);
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(this.executeButton);
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(this.newQueryButton);
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(this.displayButton);
        
        mapButtonPanel.add(this.mapPanel, BorderLayout.CENTER);
        mapButtonPanel.add(buttonBox, BorderLayout.SOUTH);
        
        centerPanel.add(this.dimensionTree, BorderLayout.WEST);
        centerPanel.add(mapButtonPanel, BorderLayout.CENTER);
        
        JPanel southPanel = new JPanel();
        BorderLayout southBorderLayout = new BorderLayout();
        southBorderLayout.setHgap(15);
        southBorderLayout.setVgap(15);
        southPanel.setLayout(southBorderLayout);
        
        this.queryCriteriaPanel.add(this.queryCriteriaArea);
        
        southPanel.add(this.queryCriteriaPanel, BorderLayout.WEST);
        southPanel.add(this.resultTable, BorderLayout.CENTER);
        
        container.add(centerPanel, BorderLayout.CENTER);
        container.add(southPanel, BorderLayout.SOUTH);
        */
    }
    
    // ErrorHandler
    public void handleThrowable(Throwable t)
    {
        JOptionPane.showMessageDialog(null, "Throwable Error");
        GUIUtil.handleThrowable(t, DataAnalysisFrame.this);
    }
    
    
    // DataAnalysisFrame
    
    private void showDisplay()
    {
        if (this.layerDisplayFrame == null)
            this.layerDisplayFrame = new LayerDisplayFrame(this.mapPanel.getLayerNamePanel());
        layerDisplayFrame.setVisible(true);
    }
    
    private void executeQuery()
    {
        String whereClause = this.whereString;
        
        // checks if exist selected attributes for execution of operations
        if (this.queryAttributeMap.size() == 0)
        {
            JOptionPane.showMessageDialog(null, 
                Local.getString("TheresNoSelectedAttributes"));
            return;
        }
        
        Aggregation aggregation = this.cube.queryNavigator(this.queryAttributeMap, true);
        
        try
        {
	        ResultSet resultSet = cube.executeQuery(this.selectAttributeMap, 
	            aggregation, whereClause);
	        
	        // Checks if result set is empty
            if (resultSet.isBeforeFirst() && resultSet.isAfterLast())
            {
                JOptionPane.showMessageDialog(null, 
                    Local.getString("QueryProcessingError"));
                return;
            }
            
            // envia o resultado da consulta à table
            this.resultSetTableModel = new CachingResultSetTableModel(resultSet);
            this.resultTable.setModel(this.resultSetTableModel);
            
	        /************ GEOGRAPHIC PART *************/
	        if (this.viewMapCheck.isSelected())
	        {
	            // executes ploting to every geo-type attribute in queryAttributeList
	            Iterator iterator = this.queryAttributeMap.keySet().iterator();
	            while (iterator.hasNext())
	            {
	                Attribute attribute = (Attribute) iterator.next();
	                if (attribute.isGeographic())
	                {
	                    // call the GIS tool's ploting procedure
	                    List list = new ArrayList();
	                    while (resultSet.next())
	                        list.add(resultSet.getString(attribute.getName()));
	                    this.plotGeographicAttribute(attribute, list);
	                }
	            }
	        }
        }
        catch (SQLException e)
        {
            System.out.println(Local.getString("RetrievingDataError"));
            return;
        }
    }
    
    public void openOLAPFrame(Cube cube)
    {
        // checks if cube already was processed
        if (!cube.wasProcessed())
        {
            JOptionPane.showMessageDialog(null, Local.getString("SelectedCubeNotProcessed"));
            return;
        }
        
        this.cube = cube;
        //this.cube.loadMetadata();
        
        // TODO: loads map to viewer
        this.mapPanel.loadData("", this.cube.getSchema().getMap());
        
        this.mountSchemaTree();
        
        this.setTitle(Local.getString("DataAnalysisCube") + ": " + this.cube.getName());
        this.setVisible(true);
    }
    
    public void mountSchemaTree()
    {
        // deletes all content of tree
        this.rootNode.removeAllChildren();
        
        if (this.cube == null)
            return;
        
        // retrieves the dimension collection to selected cube
        this.dimensionMap = cube.getDimensions();
        
        // creates "Dimensions" title and add existing dimensions to it
        DefaultMutableTreeNode dimensionRootNode = new DefaultMutableTreeNode(Local.getString("Dimensions"));
        
        Iterator iterator = this.dimensionMap.keySet().iterator();
        while (iterator.hasNext())
        {
            Dimension dimension = (Dimension) this.dimensionMap.get(iterator.next());
            DefaultMutableTreeNode currentDimensionNode = new DefaultMutableTreeNode(dimension);
            
            Map tempAttributeMap = dimension.getAttributeList(); 
            Iterator iterator2 = tempAttributeMap.keySet().iterator();
            while (iterator2.hasNext())
            {
                Attribute attribute = (Attribute) tempAttributeMap.get(iterator2.next());
                currentDimensionNode.add(new DefaultMutableTreeNode(attribute));
                // fills this.attributeMap
                if (!this.attributeMap.keySet().contains(attribute.getName()))
                    this.attributeMap.put(attribute.getName(), attribute);
                else
                {
                    JOptionPane.showMessageDialog(null, Local.getString("AttributesHavingSameName"));
                    this.rootNode.removeAllChildren();
                    return;
                }
            }
            
            dimensionRootNode.add(currentDimensionNode);
        }
       
        // appends nodes' structure to invisible root node
        this.rootNode.add(dimensionRootNode);
        
        // reloads newly created nodes' structure to model
        this.dimensionTreeModel.reload();
    }
    
    private void dimensionTreeDoubleClick()
    {
        
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) 
        	DataAnalysisFrame.this.dimensionTree.getLastSelectedPathComponent();
        if (selectedNode == null)
            return;
        
        if (selectedNode.getUserObject() instanceof Attribute)
        {
            Attribute attribute = (Attribute) selectedNode.getUserObject();
            // Don't check if already exists the attribute in map.
            // If key already exists, Map collections replace the object. 
            this.queryAttributeMap.put(attribute.getName(), attribute);
            this.selectAttributeMap.put(attribute.getName(), attribute);
            this.columnTableModel.addColumn(attribute.getName());
        }
    }
    
    private void newQuery()
    {
        // clears attribute collections
        this.attributeMap.clear();
        this.selectAttributeMap.clear();
      
        // clear WHERE clause
        this.whereString = "";
        this.queryCriteriaArea.setText("");
        
        // clears table
        this.columnTableModel.clear();
        this.resultTable.setModel(this.columnTableModel);
    }
    
    private void showPopupMenu(MouseEvent e)
    {
        // disables "criteria" popup item if there's no selected attribute 
        // in dimensionTree
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)
        	dimensionTree.getLastSelectedPathComponent();
        if (selectedNode == null)
            this.criteriaItem.setEnabled(false);
        else 
            this.criteriaItem.setEnabled(selectedNode.getUserObject() instanceof Attribute);
        
        /* For cross-plataforming purposes, call MouseEvent.isPopupTrigger() to
         * determine if current event (mousePressed or mouseReleased) is the popup
         * menu trigger for current platform. */        
        if (e.isPopupTrigger())
            this.popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }
    
    private void criteriaItemClick()
    {
        /* There's no tests here because popup menu item is enabled only if
         * there's a selected node and this node contains an instance of Attribute
         * If this method will be called from other place, it's necessary 
         * bring tests to here */        
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)
    		dimensionTree.getLastSelectedPathComponent();
        this.attribute = (Attribute) selectedNode.getUserObject();
        
        // Checks if attribute belongs to a non-aggregable dimension
        String dimensionName = attribute.getTable().getName();
        Dimension dimension = (Dimension) this.dimensionMap.get(dimensionName);
        if (dimension.getType().equals("NonAggregable"))
        {
            JOptionPane.showMessageDialog(null, Local.getString("OnlyAggregableDimensions"));
            return;
        }
        
        // Checks if attribute is geographic, for show suitable frame
        String newWhere;
        
        if (this.attribute.isGeographic())
        {
            GeographicalCriteriaDialog geographicCriteriaFrame = new GeographicalCriteriaDialog();
            newWhere = geographicCriteriaFrame.addWhereClause(this.attribute, 
                this.whereString, this.dimensionMap, this.cube.getSchema().getSrid());
        }
        else
        {
            CriteriaDialog criteriaFrame = new CriteriaDialog();
            newWhere = criteriaFrame.addWhereClause(this.attribute, this.whereString);
        }
        
        if ("".equals(newWhere) || newWhere == null)
            return;
        
        // if criteriaFrame has added anything, adds attribute to query collection
        if (!("".equals(whereString) || this.whereString == null))
            this.whereString += "\n";

        this.whereString += newWhere;
        this.queryCriteriaArea.setText(this.whereString);
        this.queryAttributeMap.put(this.attribute.getName(), this.attribute);
        this.columnTableModel.addColumn(attribute.getName());
    }
    
    private void whereItemClick()
    {
        String newWhere = JOptionPane.showInputDialog(null, 
            Local.getString("EnterWhereClause"));
        if (!("".equals(newWhere) || newWhere == null))
        {
            if (JOptionPane.showConfirmDialog(null, 
                Local.getString("ReplaceWhereClausePt1") + "\n" + this.whereString + 
                "\n" + Local.getString("ReplaceWhereClausePt2") + "\n" + newWhere + "?", 
                "PostGeoOLAP", JOptionPane.YES_NO_OPTION) == 
                JOptionPane.YES_OPTION)
            {
                this.whereString = newWhere;
                this.queryCriteriaArea.setText(this.whereString);
            }
        }
    }
    
    private void plotGeographicAttribute(Attribute attribute, List list) 
    {
        this.mapPanel.plotAttribute(attribute, list);
    }
}

class ColumnTableModel extends AbstractTableModel
{
    private List columnNameList;
    
    public ColumnTableModel()
    {
        super();
        this.columnNameList = new ArrayList();
    }
    
    public int getRowCount()
    {
        return this.getColumnCount() == 0 ? 0 : 1;
    }
    
    public int getColumnCount()
    {
        return this.columnNameList.size();
    }
    
    public String getColumnName(int column)
    {
        return (String) this.columnNameList.get(column);
    }
    
    public Object getValueAt(int row, int column)
    {
        return null;
    }
    
    public void addColumn(String columnName)
    {
        this.columnNameList.add(columnName);
        this.fireTableStructureChanged();
    }
    
    public void clear()
    {
        this.columnNameList.clear();
        this.fireTableStructureChanged();
    }
}


