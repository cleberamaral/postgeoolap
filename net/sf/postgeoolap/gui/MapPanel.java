package net.sf.postgeoolap.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import net.sf.postgeoolap.locale.Local;
import net.sf.postgeoolap.model.Attribute;

import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.LayerTreeModel;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelContext;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelListener;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelProxy;
import com.vividsolutions.jump.workbench.ui.TreeLayerNamePanel;
import com.vividsolutions.jump.workbench.ui.WorkbenchToolBar;
import com.vividsolutions.jump.workbench.ui.cursortool.MeasureTool;
import com.vividsolutions.jump.workbench.ui.zoom.PanTool;
import com.vividsolutions.jump.workbench.ui.zoom.ZoomTool;

public class MapPanel extends JPanel implements LayerViewPanelContext
{
    //private static final String LAYERS = Local.getString("Layers");
    
    private ErrorHandler errorHandler;
    private WorkbenchToolBar toolBar;
    private JPanel toolBarPanel;
    private LayerManager layerManager;
    private LayerViewPanel layerViewPanel;
    private TreeLayerNamePanel layerNamePanel;
    //private WorkbenchContext workbenchContext;
    private JLabel statusLabel;
    
    private String mapName;
    
    public MapPanel(ErrorHandler errorHandler)
    {
        this.errorHandler = errorHandler;
        this.initialize();
        this.buildPanel();
    }
    
    public void initialize()
    {
        // layerManager        
        this.layerManager = new LayerManager();
        
        // layerViewPanel
        this.layerViewPanel = new LayerViewPanel(this.layerManager, this);
        
        // layerNamePanel
        this.layerNamePanel = new TreeLayerNamePanel(this.layerViewPanel, 
            new LayerTreeModel(this.layerViewPanel), 
            this.layerViewPanel.getRenderingManager(), new HashMap());
        
        // workbenchContext
/*        this.workbenchContext = new WorkbenchContext()
        	{
            	public ErrorHandler getErrorHandler()
            	{
            	    return MapPanel.this;
            	}
            	
            	public LayerNamePanel getLayerNamePanel()
            	{
            	    return MapPanel.this.layerNamePanel;
            	}
            	
            	public LayerViewPanel getLayerViewPanel()
            	{
            	    return MapPanel.this.layerViewPanel;
            	}
            	
            	public LayerManager getLayerManager()
            	{
            	    return MapPanel.this.layerManager;
            	}
        	};
        */
        // toolBar  
        this.toolBar = new WorkbenchToolBar(
            new LayerViewPanelProxy()
            {
                public LayerViewPanel getLayerViewPanel()
                {
                    return MapPanel.this.layerViewPanel;
                }
            }
        );
        this.toolBar.setFloatable(false);
        
        this.layerViewPanel.addListener(
            new LayerViewPanelListener()
            {
                public void painted(Graphics graphics)
                {
                }
                public void selectionChanged()
                {
                }
                public void cursorPositionChanged(String x, String y)
                {
                    MapPanel.this.setStatusMessage("(" + x + ", " + y + ")");
                }
            }
        );
        
        this.statusLabel = new JLabel(" ");
    }    
    
    private void buildPanel()
    {
        this.toolBar.setOrientation(SwingConstants.VERTICAL);
        this.toolBarPanel = new JPanel();
        this.toolBarPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        this.toolBarPanel.add(this.toolBar);
        
        this.setLayout(new BorderLayout());
        
        JPanel statusPanel = new JPanel();
        statusPanel.add(this.statusLabel);
        this.layerViewPanel.setPreferredSize(new Dimension(300, 200));
        this.layerViewPanel.setSize(new Dimension(300, 200));
        
        this.add(this.toolBarPanel, BorderLayout.WEST);
        this.add(this.layerViewPanel, BorderLayout.CENTER);
        this.add(statusPanel, BorderLayout.SOUTH);       
        
/*        
        BorderLayout borderLayout = new BorderLayout();
        borderLayout.setHgap(10);
        borderLayout.setVgap(10);
        
        this.setLayout(borderLayout);
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(this.toolBarPanel, BorderLayout.WEST);
        bottomPanel.add(this.layerNamePanel, BorderLayout.CENTER);
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        statusPanel.add(this.statusLabel);
        bottomPanel.add(statusPanel, BorderLayout.SOUTH);
        
        this.add(this.layerViewPanel, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);
        
        this.layerViewPanel.setPreferredSize(new Dimension(300, 200));
                
        this.layerViewPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        this.layerNamePanel.setBorder(BorderFactory.createLoweredBevelBorder());
    */
    }
    
    public void initJUMP() throws Exception
    {
        // toolBar
        this.toolBar.addCursorTool("Zoom In/Out", new ZoomTool());
        this.toolBar.addCursorTool("Pan", new PanTool());
        this.toolBar.addCursorTool("Measure", new MeasureTool());
/*        this.toolBar.addSeparator();
        this.toolBar.addPlugIn(
            IconLoader.icon("World.gif"),
            new ZoomToFullExtentPlugIn(),
            new MultiEnableCheck(),
            workbenchContext);
*/     
        this.loadData();
    }
    
    private void loadData() throws Exception
    {
        // TODO: here we use only "Layers" category, rather than the half-dozen
        //		 standard JUMP categories
        this.removeAllCategories(this.layerManager);
        
        // código de teste
/*        this.layerManager.addLayer(
            LAYERS, "Tenures Extract",
            new SimpleGMLReader().readFMEFile(this.toFile("tenures-extract.xml")));
        this.layerManager.addLayer(
            LAYERS, "Ownership Extract",
            new SimpleGMLReader().readFMEFile(this.toFile("ownership-extract.xml")));
*/
        /*
         * CÓDIGO DE TESTE
         */
        
        /* ******** definição da camada RUAS */
        // metadados
/*        
 		FeatureSchema ruaSchema = new FeatureSchema();
        ruaSchema.addAttribute("Rua", AttributeType.GEOMETRY); // atributo geográfico
        ruaSchema.addAttribute("Logradouro", AttributeType.STRING); // não-geográficos
        ruaSchema.addAttribute("CEP", AttributeType.INTEGER);
        
        FeatureCollection ruaFeatureCollection = new FeatureDataset(ruaSchema);
        
        GeometryFactory geometryFactory = new GeometryFactory();
        Feature ruaFeature = new BasicFeature(ruaFeatureCollection.getFeatureSchema());
        ruaFeature.setAttribute("Rua", 
            geometryFactory.createLineString(
                new Coordinate [] 
                {
                    new Coordinate(1243020.8, 523000),
                    new Coordinate(1243020.8, 520890)
                }
            )            
        ); 
        ruaFeature.setAttribute("Logradouro", "Rua Principal");
        ruaFeature.setAttribute("CEP", new Integer(28013));
        
        Feature rua2Feature = new BasicFeature(ruaFeatureCollection.getFeatureSchema());
        rua2Feature.setAttribute("Rua", new WKTReader().read("LINESTRING(1243020.8 520890, 1248020.8 528890, 1243020.8 523000)"));
        rua2Feature.setAttribute("Logradouro", "Rua Principal 2");
        rua2Feature.setAttribute("CEP", new Integer(28100));
        
        ruaFeatureCollection.add(ruaFeature);
        ruaFeatureCollection.add(rua2Feature);
        layerManager.addLayer("Layers", "Ruas", ruaFeatureCollection);
        
        this.mapName = new String("Tenures & Ownership");*/
    }
    
    public void loadData(String title, String input)
    {
        this.removeAllCategories(this.layerManager);
        
//        this.layerManager.addLayer(
//            MapPanel.LAYERS, title,
//
//        );
    }
    
    public void removeAllCategories(LayerManager layerManager)
    {
        for (Iterator i = layerManager.getCategories().iterator(); i.hasNext();)
        {
            Category category = (Category) i.next();
            layerManager.removeIfEmpty(category);
        }
    }
    
    public File toFile(String fileName)
    {
        String parent = "net/sf/postgeoolap/gui";
        return new File(parent, fileName);
    }

    public void setStatusMessage(String message)
    {
        // message must have at least a space
        this.statusLabel.setText(
            ((message == null) || (message.length() == 0)) ? " " : message);
    }
    
    public void warnUser(String warning)
    {
        this.setStatusMessage(warning);
    }

    public void handleThrowable(Throwable t)
    {
        this.errorHandler.handleThrowable(t);
    }
    
    public TreeLayerNamePanel getLayerNamePanel()
    {
        return this.layerNamePanel; 
    }
    
    public String getMapName()
    {
        return this.mapName;
    }
    
    public void repaintMap()
    {
        this.layerViewPanel.repaint();
    }
    
    public void plotAttribute(Attribute attribute, List list)
    {
        FeatureSchema featureSchema = new FeatureSchema();
        featureSchema.addAttribute(attribute.getName(), AttributeType.GEOMETRY);
        FeatureCollection featureCollection = new FeatureDataset(featureSchema);
        try
        {
	        for (Iterator iterator = list.iterator(); iterator.hasNext(); )
	        {
	            Feature feature = new BasicFeature(featureCollection.getFeatureSchema());
	            feature.setAttribute(attribute.getName(), new WKTReader().read((String) iterator.next()));
	            featureCollection.add(feature);
	        }
        }
        catch (ParseException e)
        {
            JOptionPane.showMessageDialog(null, Local.getString("WKTParsingError"));
        }
        this.layerManager.addLayer(attribute.getName(), attribute.getName(), 
            featureCollection);        
    }

}