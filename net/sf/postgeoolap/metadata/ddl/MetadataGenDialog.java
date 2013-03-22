package net.sf.postgeoolap.metadata.ddl;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import net.sf.postgeoolap.gui.OkCancelDialog;
import net.sf.postgeoolap.locale.Local;

public class MetadataGenDialog extends OkCancelDialog
{
    public MetadataGenDialog()
    {
        super(Local.getString("MetadataStructureGeneration"));
        this.addField(Local.getString("Server"), 20);
        this.addField(Local.getString("Database"), 20);
        this.addField(Local.getString("User"), 20);
        this.addPasswordField(Local.getString("Password"), 20);
        
        this.setOkCaption(Local.getString("Generate"));
        this.guiMount();
    }
    
    public void okAction(ActionEvent event)
    {
        try
        {
            Metadata metadata = new Metadata();
            metadata.generate(this.getValue(Local.getString("Database")),
                this.getValue(Local.getString("Server")),  
                this.getValue(Local.getString("User")),
                this.getValue(Local.getString("Password")));
            
            JOptionPane.showMessageDialog(null, Local.getString("SuccessfullyMetadataGeneration"));
        }
        catch (MetadataGenerationException e)
        {
            JOptionPane.showMessageDialog(null, Local.getString("MetadataGenerationError"));
            e.printStackTrace(System.err);
        }
        System.exit(0);
    }
    
    public void cancelAction()
    {
        System.exit(0);
    }
}
