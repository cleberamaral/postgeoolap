package net.sf.postgeoolap.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import net.sf.postgeoolap.locale.Local;

public abstract class OkCancelDialog extends JDialog 
{
    private Map textMap;
    private Map labelMap;
    private List keyList;

    private JPanel panel;
    private JPanel buttonPanel;
    private JButton okButton;
    private JButton cancelButton;
    
    private String okCaption;
    private String cancelCaption;
    
    public OkCancelDialog() 
    {
        this("OkCancelFrame");
    }
    
    public OkCancelDialog(String caption) 
   {
        super();
        this.setTitle(caption);
        this.okCaption = Local.getString("Ok");
        this.cancelCaption = Local.getString("Cancel");
        this.setModal(true);
        this.initialize();
    }
    
    public OkCancelDialog(String caption, String okCaption,
        String cancelCaption) 
    {
        super();
        this.setTitle(caption);
        this.okCaption = okCaption;
        this.cancelCaption = cancelCaption;
        this.initialize();
    }
    
    protected void initialize() 
    {
        this.panel = new JPanel();
        this.buttonPanel = new JPanel();
        this.okButton = new JButton(this.okCaption);
        this.cancelButton = new JButton(this.cancelCaption);

        Container c = this.getContentPane();
        c.setLayout(new BorderLayout());
        c.add(this.panel, BorderLayout.CENTER);
        c.add(this.buttonPanel, BorderLayout. SOUTH);
        
        this.okButton.addActionListener(
            new ActionListener() 
            {
                public void actionPerformed(ActionEvent e) 
                {
                    OkCancelDialog.this.okAction(e);
                }
            }
        );
        
        this.cancelButton.addActionListener(
            new ActionListener() 
            {
                public void actionPerformed(ActionEvent e) 
                {
                    OkCancelDialog.this.cancelAction();
                }
            }
        );
    }
    
    protected void guiMount() 
    {

        this.pack();
        int proportion = (int) Math.round(this.getWidth() * 0.15);
        
        // put Ok and Cancel buttons, standard for all subclasses
        this.buttonPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(8, 0, 8, proportion);
        this.buttonPanel.add(this.okButton, gbc);
        
        gbc.gridx = 1;
        gbc.insets = new Insets(8, proportion, 8, 0);
        this.buttonPanel.add(this.cancelButton, gbc);
        
        this.build();
        this.pack();
        int width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int height = Toolkit.getDefaultToolkit().getScreenSize().height;
        this.setLocation(Math.round(width / 2 - this.getWidth() / 2), 
                		 Math.round(height / 2 - this.getHeight() / 2));
    }
    
    protected void build()
    {
        int row = 0;
        
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        for (int i = 0; i < this.keyList.size(); i++)
        {
            String key = (String) this.keyList.get(i);
            JTextField textField = (JTextField) this.textMap.get(key);
            JLabel label = (JLabel) this.labelMap.get(key);
            
            gbc.gridx = 0;
            gbc.gridy = row++;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.insets = new Insets(4, 8, 4, 4);
            gbc.anchor = GridBagConstraints.WEST;
            panel.add(label, gbc);
            
            gbc.gridx = 1;
            gbc.insets = new Insets(4, 4, 4, 8);
            panel.add(textField, gbc);
        }
    }
    
    protected void addField(String fieldName, int size) 
    {
        if (this.textMap == null)
            this.textMap = new HashMap();
        if (this.labelMap == null)
            this.labelMap = new HashMap();
        if (this.keyList == null)
            this.keyList = new ArrayList();
        
        this.keyList.add(fieldName);
        
        JLabel label = new JLabel(fieldName);
        JTextField textField = new JTextField(size);
        
        this.textMap.put(fieldName, textField);
        this.labelMap.put(fieldName, label);
    }
    
    protected void addPasswordField(String fieldName, int size) 
    {
        if (this.textMap == null)
            this.textMap = new HashMap();
        if (this.labelMap == null)
            this.labelMap = new HashMap();
        if (this.keyList == null)
            this.keyList = new ArrayList();
        
        this.keyList.add(fieldName);
        
        JLabel label = new JLabel(fieldName);
        JPasswordField textField = new JPasswordField(size);
        
        this.textMap.put(fieldName, textField);
        this.labelMap.put(fieldName, label);
    }
    
    protected String getValue(String fieldName) 
    {
        try
        {
            return ((JTextField) textMap.get(fieldName)).getText();
        }
        catch (NullPointerException e)
        {
            System.err.println("Erro em " + fieldName);
            return null;
        }
    }
    
    protected void setValue(String fieldName, String value)
    {
        ((JTextField) textMap.get(fieldName)).setText(value);
    }
    
    protected void setFocus(String fieldName)
    {
        ((JTextField) textMap.get(fieldName)).requestFocusInWindow();
    }
        
    protected abstract void okAction(ActionEvent e);
    
    protected abstract void cancelAction();
    
    public String getCancelCaption()
    {
        return cancelCaption;
    }
    
    public void setCancelCaption(String cancelCaption)
    {
        this.cancelCaption = cancelCaption;
    }
    
    public String getOkCaption()
    {
        return okCaption;
    }
    
    public void setOkCaption(String okCaption)
    {
        this.okCaption = okCaption;
    }
    
    protected JPanel getPanel()
    {
        return this.panel;
    }
    
    protected void setOkEnabled(boolean enabled)
    {
        this.okButton.setEnabled(enabled);
    }
    
    public void setEditable(String fieldName, boolean editable)
    {
        ((JTextField) this.textMap.get(fieldName)).setEditable(editable);
    }

}