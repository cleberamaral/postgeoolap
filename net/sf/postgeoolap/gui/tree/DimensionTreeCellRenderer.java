package net.sf.postgeoolap.gui.tree;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.sf.postgeoolap.locale.Local;
import net.sf.postgeoolap.model.Attribute;
import net.sf.postgeoolap.model.Cube;
import net.sf.postgeoolap.model.Dimension;

public class DimensionTreeCellRenderer extends DefaultTreeCellRenderer
{
    private final String RESOURCE_PACKAGE = "/net/sf/postgeoolap/gui/resources/";
    
    public Component getTreeCellRendererComponent(JTree tree, Object value,
        boolean selected, boolean expanded, boolean leaf, int row, 
        boolean hasFocus)
    {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf,
            row, hasFocus);
        
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        Object object =  node.getUserObject();
        if (object instanceof Attribute)
            this.setIcon(new ImageIcon(Class.class.getResource(this.RESOURCE_PACKAGE + "attribute.GIF")));
        else if (object instanceof Cube)
            this.setIcon(new ImageIcon(Class.class.getResource(this.RESOURCE_PACKAGE + "cube.GIF")));
        else if (object instanceof Dimension)
        {
            Dimension dimension = (Dimension) object;
            if (dimension.getType().equals("Fact"))
                this.setIcon(new ImageIcon(Class.class.getResource(this.RESOURCE_PACKAGE + "fact.GIF")));
            else if (dimension.getType().equals("Dimension"))
                this.setIcon(new ImageIcon(Class.class.getResource(this.RESOURCE_PACKAGE + "dimension.GIF")));
            else if (dimension.getType().equals("NonAggregable"))
                this.setIcon(new ImageIcon(Class.class.getResource(this.RESOURCE_PACKAGE + "non_aggregable.GIF")));
        }
        else if (object instanceof String)
        {
            String string = (String) object;
            if (string.equals(Local.getString("Dimensions")))
            {
                if (expanded)
                    this.setIcon(new ImageIcon(Class.class.getResource(this.RESOURCE_PACKAGE + "folder_open.GIF")));
                else
                    this.setIcon(new ImageIcon(Class.class.getResource(this.RESOURCE_PACKAGE + "folder_close.GIF")));
            }
            else if (string.equals(Local.getString("Cubes")))
            {
                if (expanded)
                    this.setIcon(new ImageIcon(Class.class.getResource(this.RESOURCE_PACKAGE + "folder_open.GIF")));
                else
                    this.setIcon(new ImageIcon(Class.class.getResource(this.RESOURCE_PACKAGE + "folder_close.GIF")));
            }
            else if (string.startsWith(Local.getString("Schema")))
                this.setIcon(new ImageIcon(Class.class.getResource(this.RESOURCE_PACKAGE + "computer.GIF")));
        }
        
        return this;
    }
}