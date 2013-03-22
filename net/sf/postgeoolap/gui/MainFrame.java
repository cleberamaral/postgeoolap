package net.sf.postgeoolap.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.sf.postgeoolap.connect.DBConnection;
import net.sf.postgeoolap.gui.tree.DimensionTreeCellRenderer;
import net.sf.postgeoolap.locale.Local;
import net.sf.postgeoolap.model.Attribute;
import net.sf.postgeoolap.model.Cube;
import net.sf.postgeoolap.model.Dimension;
import net.sf.postgeoolap.model.Schema;
import net.sf.postgeoolap.metadata.ddl.*;

public class MainFrame extends JFrame
{
    private Schema schema;
    private Map cubeMap;
    
    private JMenuBar menuBar;
    private JMenu schemaMenu;
    private JMenu metadataMenu; //**
    private JPopupMenu schemaPopup;
    
    private JMenuItem selectSchemaItem;
    private JMenuItem generateMetadataItem; //**
    private JMenuItem createCubeItem;
    private JMenuItem exitItem;
    
    private JMenuItem addDimensionItem;
    private JMenuItem addFactItem;
    private JMenuItem editHierarchyItem;
    private JMenuItem addNonAggregableItem;
    private JMenuItem processCubeItem;
    private JMenuItem analyzeDataItem;
    
    private DefaultTreeModel schemaTreeModel;
    private DefaultMutableTreeNode schemaRootNode;
    private JTree schemaTree;
    
    public MainFrame()
    {
        super("PostGeoOLAP - " + Local.getString("GeneralSchema"));
        this.initialize();
        this.build();
        //this.setResizable(false);
        this.pack();
        this.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - this.getWidth() / 2, 
            Toolkit.getDefaultToolkit().getScreenSize().height / 2 - this.getHeight() / 2);
        DBConnection.clearMetadata();
    }
    
    private void initialize()
    {
        this.addWindowFocusListener(
            new WindowAdapter()
            {
                public void windowGainedFocus(WindowEvent e)
                {
                    MainFrame.this.frameFocusGained(e);
                }
            }
        );
        
        this.schema = new Schema();
        
        this.menuBar = new JMenuBar();
        this.schemaMenu = new JMenu(Local.getString("Schema")); 
        this.metadataMenu = new JMenu(Local.getString("Metadata"));
        this.menuBar.add(schemaMenu);
        this.menuBar.add(metadataMenu);
        this.selectSchemaItem = new JMenuItem(Local.getString("SelectSchema"));
        this.selectSchemaItem.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    MainFrame.this.selectSchema();
                }
            }
        );
        
        this.generateMetadataItem = new JMenuItem(Local.getString("GenerateMetadata"));
        this.generateMetadataItem.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    MainFrame.this.generateMetadata();
                }
            }
        );
        
        this.createCubeItem = new JMenuItem(Local.getString("CreateCube"));
        this.createCubeItem.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    MainFrame.this.createCube();
                }
            }
        );
        this.exitItem = new JMenuItem(Local.getString("Exit"));
        this.exitItem.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    MainFrame.this.exit();
                }
            }
        );
        
        this.schemaMenu.add(this.selectSchemaItem);
        this.schemaMenu.addSeparator();
        this.schemaMenu.add(this.createCubeItem);
        this.schemaMenu.addSeparator();
        this.schemaMenu.add(this.exitItem);
        
        this.metadataMenu.add(generateMetadataItem);
        
        this.createCubeItem.setEnabled(false);
        
        this.schemaPopup = new JPopupMenu();
        this.addDimensionItem = new JMenuItem(Local.getString("AddDimension"));
        this.addFactItem = new JMenuItem(Local.getString("AddFact"));
        this.editHierarchyItem = new JMenuItem(Local.getString("EditHierarchy"));
        this.addNonAggregableItem = new JMenuItem(Local.getString("AddNonAggregableDimension"));
        this.processCubeItem = new JMenuItem(Local.getString("ProcessCube"));
        this.analyzeDataItem = new JMenuItem(Local.getString("AnalyzeData"));
        
        this.addDimensionItem.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    MainFrame.this.addDimension();
                }
            }
        );
        
        this.addFactItem.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    MainFrame.this.addFact();
                }
            }
        );
        
        this.editHierarchyItem.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    MainFrame.this.editHierarchy();
                }
            }
        );
        
        this.addNonAggregableItem.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    MainFrame.this.addNonAggregableDimension();
                }
            }
        );
        
        this.processCubeItem.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    MainFrame.this.processCube();
                }
            }
        );
        
        this.analyzeDataItem.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    MainFrame.this.analyzeData();
                }
            }
        );
        
        this.schemaPopup.add(this.addDimensionItem);
        this.schemaPopup.add(this.addFactItem);
        this.schemaPopup.add(this.editHierarchyItem);
        this.schemaPopup.addSeparator();
        this.schemaPopup.add(this.addNonAggregableItem);
        this.schemaPopup.addSeparator();
        this.schemaPopup.add(this.processCubeItem);
        this.schemaPopup.add(this.analyzeDataItem);
        
        this.schemaPopup.addPopupMenuListener(
            new PopupMenuListener()
            {
                public void popupMenuWillBecomeVisible(PopupMenuEvent e)
                {
                    MainFrame frame = MainFrame.this;
                    
                    frame.addDimensionItem.setEnabled(frame.isDimensionLabelSelectedOnTree());
                    frame.addFactItem.setEnabled(frame.canAddFact());
                    frame.editHierarchyItem.setEnabled(frame.isDimensionSelectedOnTree());
                    frame.addNonAggregableItem.setEnabled(frame.isDimensionLabelSelectedOnTree());
                    frame.processCubeItem.setEnabled(frame.isCubeSelectedOnTree());
                    frame.analyzeDataItem.setEnabled(frame.isCubeSelectedOnTree());                    
                }
                
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
                {
                }
                
                public void popupMenuCanceled(PopupMenuEvent e)
                {
                }
            }
        );
        
        this.schemaRootNode = new DefaultMutableTreeNode("RootNode");
        this.schemaTreeModel = new DefaultTreeModel(this.schemaRootNode);
        this.schemaTree = new JTree(this.schemaTreeModel);
        this.schemaTree.setRootVisible(false);
        this.schemaTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        this.schemaTree.addTreeSelectionListener(
            new TreeSelectionListener()
            {
                public void valueChanged(TreeSelectionEvent event)
                {
                    JTree tree = (JTree) event.getSource();
                    TreePath treePath = tree.getSelectionPath();
                    if (treePath != null)
                    {
                        // 	selected node as parameter
                        //MainFrame.this.schemaTreeSelection(
                        //    (DefaultMutableTreeNode) treePath.getLastPathComponent());
                    }
                }
            }
        );
        this.schemaTree.setCellRenderer(new DimensionTreeCellRenderer());
        
        this.schemaTree.addMouseListener(
            new MouseAdapter()
            {
                public void mouseReleased(MouseEvent event)
                {
                    if (event.getButton()==MouseEvent.BUTTON3)
                    {
                        MainFrame.this.showPopup(event.getComponent(), event.getX(), event.getY());
                    }
                }
            }
        );
        
        
        this.addComponentListener(
            new ComponentAdapter()
            {
                public void componentShown(ComponentEvent e)
                {
                    MainFrame.this.mountSchemaTree();
                }
            }
        );
        
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(
            new WindowAdapter()
            {
                public void windowClosing(WindowEvent event)
                {
                    MainFrame.this.exit();
                }
            }
        );
        
        // enable decorations (i.e., title bar, icons, borders) for all dialogs
        JDialog.setDefaultLookAndFeelDecorated(true);
        
        this.setJMenuBar(this.menuBar);
    }
    
    private void build()
    {
        Container c = this.getContentPane();
        c.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        JScrollPane schemaTreeScroll = new JScrollPane(this.schemaTree, 
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        schemaTreeScroll.setPreferredSize(new java.awt.Dimension(300, 500));
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        c.add(schemaTreeScroll, gbc);
    }
    
    private DefaultMutableTreeNode getSelectedNode()
    {
        return (DefaultMutableTreeNode) this.schemaTree.getLastSelectedPathComponent();
    }
    
    private DefaultMutableTreeNode getAttributeNode(Dimension dimension)
    {
        Map attributeMap = dimension.getAttributeList();
        if (attributeMap == null)
            return null;
        
        DefaultMutableTreeNode attributeNode = new DefaultMutableTreeNode(Local.getString("Attributes"));
        Iterator iterator = attributeMap.keySet().iterator();
        while (iterator.hasNext())
        {
            Attribute attribute = (Attribute) attributeMap.get(iterator.next());
            DefaultMutableTreeNode attNode = new DefaultMutableTreeNode(attribute);
            attributeNode.add(attNode);
        }
        return attributeNode;
    }
    
    private DefaultMutableTreeNode getDimensionNode(Cube cube)
    {
        Map dimensionMap = cube.getDimensions();
        if (dimensionMap == null)
            return null;
        
        DefaultMutableTreeNode dimensionNode = new DefaultMutableTreeNode(Local.getString("Dimensions"));
        Iterator iterator = dimensionMap.keySet().iterator();
        while (iterator.hasNext())
        {
            Dimension dimension = (Dimension) dimensionMap.get(iterator.next());
            DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(dimension);
            dimensionNode.add(treeNode);
            
            // Creates a "attributes" title and adds it the existing attributes
            treeNode.add(this.getAttributeNode(dimension));
        }
        return dimensionNode;
    }
    
    private void mountSchemaTree()
    {
        DefaultMutableTreeNode rootNode = this.schemaRootNode;
        
        rootNode.removeAllChildren();
        
        if ((this.schema != null) && (this.schema.getName() != null))
        {
        
            // Add data for current schema
            DefaultMutableTreeNode schemaNode = 
                new DefaultMutableTreeNode(Local.getString("Schema") +  " - " + 
                    this.schema.getName());

            DefaultMutableTreeNode cubeTitleNode = 
                new DefaultMutableTreeNode(Local.getString("Cubes"));
            this.cubeMap = this.schema.getCubes();
            if (this.cubeMap != null)
            {
                    Iterator iterator = this.cubeMap.keySet().iterator();
                    while (iterator.hasNext())
                    {
                        Cube cube = (Cube) this.cubeMap.get(iterator.next());
                        DefaultMutableTreeNode cubeNode = 
                            new DefaultMutableTreeNode(cube);
                        cubeTitleNode.add(cubeNode);

                        cubeNode.add(this.getDimensionNode(cube));
                    }
            }

            schemaNode.add(cubeTitleNode);
            rootNode.add(schemaNode);

            this.schemaTreeModel.reload();
            //this.schemaTreeModel.nodeChanged(rootNode);
        }
    }
    
    private void showPopup(Component component, int x, int y)
    {
        this.schemaPopup.show(component, x, y);
    }
    
    private void frameFocusGained(WindowEvent e)
    {
        this.mountSchemaTree();
    }
    
    private void selectSchema()
    {
        SchemaSelectionDialog schemaSelectionFrame = new SchemaSelectionDialog();
        this.schema = schemaSelectionFrame.getSchema();
        if ((this.schema == null) || "".equals(this.schema.getName()))
            return;
        this.mountSchemaTree();
        // shows the schema name in header
        this.setTitle("PostGeoOLAP - " + Local.getString("ConnectedTo") +	 " " + 
            this.schema.getName());
        this.createCubeItem.setEnabled(true);
    }
    
    private void createCube()
    {
        if (this.schema.getName().equals(""))
            return;
        CreateCubeDialog createCubeFrame = new CreateCubeDialog();
        Cube cube = createCubeFrame.createCube(this.schema);
        if (cube != null)
        {
            this.cubeMap.put(cube.getName(), cube);
            this.mountSchemaTree();
        }
    }
    
    private void exit()
    {
        DBConnection.closeConnections();
        System.exit(0);
    }
    
    private void generateMetadata(){
        
        if(JOptionPane.showConfirmDialog(null,Local.getString("SureToCreateMetadata"))!=0){
            return;
        }
        String st[]= null;
        MetadataGenerator.main(st);
        
        
    }
    
    private boolean canAddFact()
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.getSelectedNode().getParent();
        if (!this.isDimensionLabelSelectedOnTree())
            return false;
        Cube cube = (Cube) node.getUserObject();
        Map map = cube.getDimensions();
        Iterator iterator = map.keySet().iterator();
        while (iterator.hasNext())
        {
            Dimension dimension = (Dimension) map.get(iterator.next());
            if ("fact".equalsIgnoreCase(dimension.getType()))
                return false;
        }
        return true;
    }
    
    private boolean isDimensionLabelSelectedOnTree()
    {
        DefaultMutableTreeNode node = this.getSelectedNode();
        if (node == null)
            return false;
        if (node.getUserObject() instanceof String)
        {
            String string = (String) node.getUserObject();
            return string.equals(Local.getString("Dimensions"));
        }
        return false;
    }
    
    private boolean isDimensionSelectedOnTree()
    {
        DefaultMutableTreeNode node = this.getSelectedNode();
        if (node == null)
            return false;
        DefaultMutableTreeNode parentNode = 
            (DefaultMutableTreeNode) node.getParent();
        if (parentNode.getUserObject() instanceof String)
        {
            String string = (String) parentNode.getUserObject();
            return string.equals(Local.getString("Dimensions"));
        }
        return false;
    }
    
    private boolean isCubeSelectedOnTree()
    {
        DefaultMutableTreeNode node = this.getSelectedNode();
        if (node == null)
            return false;
        DefaultMutableTreeNode parentNode =
            (DefaultMutableTreeNode) node.getParent();
        if (parentNode.getUserObject() instanceof String)
        {
            String string = (String) parentNode.getUserObject();
            return string.equals(Local.getString("Cubes"));
        }
        return false;
    }   
    
    private void addDimension()
    {
        if (isDimensionLabelSelectedOnTree())
        {
            DefaultMutableTreeNode parentNode = 
                (DefaultMutableTreeNode) this.getSelectedNode().getParent();
            Cube cube = (Cube) parentNode.getUserObject();
            DimensionSelectionDialog dimensionSelectionFrame = 
                new DimensionSelectionDialog();
            dimensionSelectionFrame.selectDimensionTable(cube);
            this.mountSchemaTree();
        }
    }
    
    private void addFact()
    {
        if (this.isDimensionLabelSelectedOnTree())
        {
            DefaultMutableTreeNode parentNode = 
                (DefaultMutableTreeNode) this.getSelectedNode().getParent();
            Cube cube = (Cube) parentNode.getUserObject();
            FactTableDialog factTableDialog = new FactTableDialog();
            factTableDialog.selectFactTable(cube);
            this.mountSchemaTree();
        }
        
    }
    
    private void editHierarchy()
    {
        if (this.isDimensionSelectedOnTree())
        {
            Cube cube = (Cube) ((DefaultMutableTreeNode) this.getSelectedNode().getParent().getParent()).getUserObject();
            Dimension dimension = (Dimension) this.getSelectedNode().getUserObject();
            // checks and forbids hierarchies for fact table
            if (dimension.getType().equals("Fact"))
            {
                JOptionPane.showMessageDialog(null, Local.getString("FactTableHasNoHierarchy"));
                return;	
            }
            HierarchySelectionDialog dimensionHierarchyLevelFrame = 
                new HierarchySelectionDialog();
            dimensionHierarchyLevelFrame.selectDimensionLevel(cube, dimension);                
        }
    }
    
    private void addNonAggregableDimension()
    {
        if (this.isDimensionLabelSelectedOnTree())
        {
            DefaultMutableTreeNode parentNode =	
                (DefaultMutableTreeNode) this.getSelectedNode().getParent();
            Cube cube = (Cube) parentNode.getUserObject();
            NonAggregableDimensionDialog nonAggregableDimensionFrame =
                new NonAggregableDimensionDialog();
            nonAggregableDimensionFrame.selectDimensionTable(cube);
            this.mountSchemaTree();
        }
    }
    
    private void processCube()
    {
        if (this.isCubeSelectedOnTree())
        {
            Cube cube = (Cube) this.getSelectedNode().getUserObject();
            ProcessCubeDialog processCubeFrame = new ProcessCubeDialog();
            processCubeFrame.processCube(cube);
        }
    }
    
    private void analyzeData()
    {
        if (this.isCubeSelectedOnTree())
        {
            Cube cube = (Cube) this.getSelectedNode().getUserObject();
            DataAnalysisFrame dataAnalysisFrame = new DataAnalysisFrame();
            dataAnalysisFrame.openOLAPFrame(cube);
        }
    }
}