package net.sf.postgeoolap.gui;
		
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import net.sf.postgeoolap.locale.Local;
import net.sf.postgeoolap.model.Schema;

public class SchemaDialog extends OkCancelDialog
{
    private boolean editing = false;
    private Schema currentSchema = new Schema();
    
    public SchemaDialog()
    {
        super(Local.getString("Schema"));
        this.addField(Local.getString("Server"), 20);
		this.addField(Local.getString("User"), 20);
		this.addPasswordField(Local.getString("Password"), 20);
		this.addField(Local.getString("Database"), 20);
		this.addField(Local.getString("Map"), 20);
		this.addField(Local.getString("MapSRID"), 6);
		this.setOkCaption(Local.getString("Connect"));       
        this.guiMount();
        this.setModal(true);
    }
    
    protected void okAction(ActionEvent e)
    {
        if (!this.validData())
        {
            JOptionPane.showMessageDialog(null, Local.getString("MissingData"));
            return;
        }

        String srid = this.getValue(Local.getString("MapSRID"));
        int nsrid = srid.equals("") ? 0 : Integer.parseInt(srid); 
        if (this.editing)
        {
            // editing current schema
            if (currentSchema.create(this.getValue(Local.getString("Database")), this.getValue(Local.getString("User")), 
                this.getValue(Local.getString("Password")), this.getValue(Local.getString("Server")), this.getValue(Local.getString("Map")),
                nsrid, true))
                JOptionPane.showMessageDialog(null, Local.getString("SchemaUpdatedSuccessfully"));
        }
        else
        {
            // creating new schema
            if (currentSchema.create(this.getValue(Local.getString("Database")), this.getValue(Local.getString("User")), 
                this.getValue(Local.getString("Password")), this.getValue(Local.getString("Server")), this.getValue(Local.getString("Map")),
                nsrid, false))
                JOptionPane.showMessageDialog(null, Local.getString("SchemaCreatedSuccessfully"));
            DataAnalysisFrame dataAnalysisFrame = new DataAnalysisFrame();
            dataAnalysisFrame.mountSchemaTree();
            this.setVisible(false);
        }
    }
    
    protected void cancelAction()
    {
        this.setVisible(false);
    }
    
    // SchemaDialog 
    
    private boolean validData()
    {
        return !this.getValue(Local.getString("Database")).equals("") && 
        	   !this.getValue(Local.getString("User")).equals("") && 
        	   !this.getValue(Local.getString("Server")).equals("") &&
        	   !"".equals(this.getValue(Local.getString("Password"))) &&
        	   !"".equals(this.getValue(Local.getString("Map"))) &&
        	   !"".equals(this.getValue(Local.getString("MapSRID")));
    }
    
    public void updateSchema(Schema schema)
    {
        this.editing = true;
        this.currentSchema = schema;
        this.setValue(Local.getString("Database"), schema.getName());
        this.setValue(Local.getString("User"), schema.getUser());
        this.setValue(Local.getString("Password"), schema.getPassword());
        this.setValue(Local.getString("Server"), schema.getServer());
        this.setValue(Local.getString("Map"), schema.getMap() != null ? schema.getMap() : "");
        this.setValue(Local.getString("MapSRID"), Long.toString(schema.getSrid()));
        this.setVisible(true);
    }
    
    public Schema newSchema()
    {
    	this.setVisible(true);
        return this.currentSchema;
    }
}