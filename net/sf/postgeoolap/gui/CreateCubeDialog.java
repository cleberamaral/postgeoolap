package net.sf.postgeoolap.gui;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import net.sf.postgeoolap.locale.Local;
import net.sf.postgeoolap.model.Cube;
import net.sf.postgeoolap.model.Schema;
  
public class CreateCubeDialog extends OkCancelDialog
{
    private Schema schema;
    private Cube cube;
    
	public CreateCubeDialog()
	{
	    super(Local.getString("CreateNewCube"));
	    this.addField(Local.getString("Schema"), 20);
	    this.addField(Local.getString("CubeName"), 20);
        this.addField(Local.getString("MinimumAggregation"), 10);
	    
	    this.setEditable(Local.getString("Schema"), false);

	    this.guiMount();
	}
	
	protected void okAction(ActionEvent e)
	{
	    if (this.getValue(Local.getString("CubeName")).equals("") || this.getValue(Local.getString("CubeName")) == null)
	    {
	        JOptionPane.showMessageDialog(null, Local.getString("CubeNameCantBeEmpty"));
	        this.setFocus(Local.getString("CubeName"));
	    }
	    else
	    {
	        Cube cube = new Cube();
	        if (!cube.create(this.schema, this.getValue(Local.getString("CubeName")), Long.parseLong(this.getValue(Local.getString("MinimumAggregation")))))
	            JOptionPane.showMessageDialog(null, Local.getString("CubeCreationError"));
	        else
	        {
	            JOptionPane.showMessageDialog(null, Local.getString("SuccessfullyCubeCreation"));
	            FactTableDialog selectFactTableDialog = new FactTableDialog();
	            selectFactTableDialog.selectFactTable(cube);
	            this.setVisible(false);
	            this.cube = cube;
	        }
	    }   
	}
	
	protected void cancelAction()
	{
	    this.setVisible(false);
	}
	
	public Cube createCube(Schema schema)
	{
	    this.schema = schema;
	    this.setValue(Local.getString("Schema"), this.schema.getName());
	    this.cube = null;
	    this.setVisible(true);
	    return this.cube;
	}
}