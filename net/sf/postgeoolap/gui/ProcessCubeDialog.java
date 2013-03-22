package net.sf.postgeoolap.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import net.sf.postgeoolap.locale.Local;
import net.sf.postgeoolap.model.Cube;

public class ProcessCubeDialog extends OkCancelDialog implements Observer
{
    private Cube cube;
    
    private JLabel progressLabel;
    private JLabel logLabel;
    private JTextArea logArea;
    
    public ProcessCubeDialog()
    {
        super(Local.getString("ProcessCube"));
        this.guiMount();        
    }
    
    protected void initialize()
    {
        super.initialize();
        this.progressLabel = new JLabel(Local.getString("ExecutingOperation") + ": ");
        this.logLabel = new JLabel(Local.getString("ProcessingLog"));
        this.logArea = new JTextArea(25, 40);
    }
    
    protected void build()
    {
        JPanel panel = this.getPanel(); 
        
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 3, 10);
        panel.add(this.progressLabel, gbc);
        
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 10, 10, 3);
        panel.add(this.logLabel, gbc);
        
        gbc.gridx = 1;
        gbc.insets = new Insets(3, 3, 10, 10);
        this.logArea.setFont(new JTextField().getFont());
        this.logArea.setLineWrap(true);
        this.logArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(this.logArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), gbc);
        
        this.setOkCaption(Local.getString("Process"));
    }

    protected void okAction(ActionEvent e)
    {
        this.logArea.setText(Local.getString("InitializingProcessing") + 
            DateFormat.getDateInstance().format(new Date()));
        OutputCubeModel outputCubeModel = new OutputCubeModel();
        outputCubeModel.addObserver(this);
        this.cube.process(outputCubeModel);
        this.logArea.setText(this.logArea.getText() + "\n" + 
            Local.getString("FinalizingProcessing") + 
            DateFormat.getDateInstance().format(new Date()));
        JOptionPane.showMessageDialog(null, Local.getString("EndProcessingCube"));
    }

    protected void cancelAction()
    {
        this.setVisible(false);
    }
    
    // metodos
    public void processCube(Cube cube)
    {
        this.cube = cube;
        this.setTitle(Local.getString("ProcessCube") + ": " + cube.getName());
        this.setVisible(true);
    }
    
    public void showMessage(String message)
    {
        this.logArea.setText(this.logArea.getText() + "\n" + message);
        // this.currentOperationLabel.setText(message);
        this.invalidate();
    }
    
    // method from Observer
    public void update(Observable observable, Object object)
    {
        OutputProgress output = (OutputProgress) observable;
        String last = output.getLastString();
        this.progressLabel.setText(Local.getString("ExecutingOperation") + ": " +  last);
        this.logArea.setText(this.logArea.getText() + "\n" + last); 
        this.progressLabel.repaint();
        this.logArea.repaint();
    }
    
}