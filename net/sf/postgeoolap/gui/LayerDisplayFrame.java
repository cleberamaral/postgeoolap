package net.sf.postgeoolap.gui;

import javax.swing.JFrame;

import net.sf.postgeoolap.locale.Local;

import com.vividsolutions.jump.workbench.ui.TreeLayerNamePanel;

public class LayerDisplayFrame extends JFrame
{
    private TreeLayerNamePanel layerNamePanel;
    
    public LayerDisplayFrame(TreeLayerNamePanel layerNamePanel)
    {
        super(Local.getString("Displays"));
        this.layerNamePanel = layerNamePanel;
        this.initialize();
        this.build();
        this.pack();
    }
    
    private void initialize()
    {

    }
    
    private void build()
    {
        this.getContentPane().add(this.layerNamePanel);
    }
}
