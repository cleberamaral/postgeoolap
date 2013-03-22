package net.sf.postgeoolap.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.postgeoolap.locale.Local;
import net.sf.postgeoolap.model.Schema;

public class SchemaSelectionDialog extends JDialog
{
    private Schema schema = new Schema();
    private Map schemaMap = new TreeMap();
    
    private JList schemaList;
    private DefaultListModel schemaListModel;
    private JButton newButton;
    private JButton editButton;
    private JButton connectButton;
    
    public SchemaSelectionDialog()
    {
        super();
        this.setTitle(Local.getString("SelectSchema"));
        this.setModal(true);
        
        this.initialize();
        this.build();
        this.pack();
        this.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - this.getWidth() / 2, 
            Toolkit.getDefaultToolkit().getScreenSize().height / 2 - this.getHeight() / 2);
    }
    
    private void initialize()
    {
        this.addWindowFocusListener(
            new WindowAdapter()
            {
                public void windowGainedFocus(WindowEvent e)
                {
                    SchemaSelectionDialog.this.dialogFocusGained(e);
                }
            }
    	);
            
        this.schemaListModel = new DefaultListModel();
        this.schemaList = new JList(this.schemaListModel);
        this.schemaList.addMouseListener(
            new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    if (e.getClickCount() == 2)
                    {
                        SchemaSelectionDialog.this.connectButtonAction(null);
                    }
                }
            }
        );
        this.schemaList.addFocusListener(
            new FocusAdapter()
            {
                public void focusLost(FocusEvent e)
                {
                    SchemaSelectionDialog.this.schemaListFocusLost(e);
                }
            }
        );
        this.schemaList.addListSelectionListener(
            new ListSelectionListener()
            {
                public void valueChanged(ListSelectionEvent e)
                {
                    SchemaSelectionDialog.this.loadSchema();
                }
            }
        );        
        this.schemaList.setCellRenderer(new SchemaCellRenderer());

        this.newButton = new JButton(Local.getString("New"));
        this.newButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    SchemaSelectionDialog.this.newButtonAction(e);
                }
            }
        );
        
        this.editButton = new JButton(Local.getString("Edit"));
        this.editButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    SchemaSelectionDialog.this.editButtonAction(e);
                }
            }
        );
        
        this.connectButton = new JButton(Local.getString("Connect"));
        this.connectButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    SchemaSelectionDialog.this.connectButtonAction(e);
                }
            }
        );
    }
    
    private void build()
    {
        Container c = this.getContentPane();
        
        c.setLayout(new GridBagLayout());        
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 3;
        gbc.insets = new Insets(10, 10, 10, 3);
        c.add(new JScrollPane(this.schemaList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridheight = 1;
        gbc.insets = new Insets(10, 3, 10, 10);
        c.add(this.newButton, gbc);
        
        gbc.gridy++;
        c.add(this.editButton, gbc);
        
        gbc.gridy++;
        c.add(this.connectButton, gbc);
        
        this.editButton.setEnabled(false);
        this.connectButton.setEnabled(false);
    }
   
    private void newButtonAction(ActionEvent e)
    {
        SchemaDialog schemaDialog = new SchemaDialog();
        Schema schema;
        schema = schemaDialog.newSchema();
        if (!schema.getName().equals(""))
        {
            this.schemaMap.put(schema.getName(), schema);
            this.fillSchemaList();
            this.schema = schema;
        }
    }
    
    private void editButtonAction(ActionEvent e)
    {
        SchemaDialog schemaDialog = new SchemaDialog();
        schemaDialog.updateSchema(this.schema);
    }
    
    private void connectButtonAction(ActionEvent e)
    {
        if (this.schema.connect())
        {
            this.setVisible(false);
            this.dispose();
        }
    }
    
    private void dialogFocusGained(WindowEvent e)
    {
        this.fillSchemaList();
    }
    
    private void schemaListFocusLost(FocusEvent e)
    {
        if (this.schemaList.getSelectedIndex() != -1)
        {
            String name = (String) this.schemaList.getSelectedValue();
            // retrieve item from schema collection to pass it as parameter
            this.schema = (Schema) this.schemaMap.get(name);
            this.editButton.setEnabled(true);
            this.connectButton.setEnabled(true);
        }
        else
        {
            //JOptionPane.showMessageDialog(null, Local.getString("SelectASchema"));
            this.editButton.setEnabled(false);
            this.connectButton.setEnabled(false);
        }
    }
    
    // metodos
    
    public Schema getSchema()
    {
        this.setVisible(true);
        return this.schema;
    }
    
    private void fillSchemaList()
    {
        this.schemaListModel.clear();
        this.schemaMap = Schema.getCollection();
        Iterator iterator = schemaMap.keySet().iterator();
        while (iterator.hasNext())
        {
            Schema schema = (Schema) schemaMap.get((String) iterator.next());
            this.schemaListModel.addElement(schema.getName());
        }
    }
    
    private void loadSchema()
    {
        String schemaName = (String) this.schemaList.getSelectedValue();
        if (schemaName != null)
        {
            this.schema = (Schema) this.schemaMap.get(schemaName);
            this.connectButton.setEnabled(true);
            this.editButton.setEnabled(true);
        }
        else
        {
            this.schema = null;
            this.connectButton.setEnabled(false);
            this.editButton.setEnabled(false);
        }
    }
}

class SchemaCellRenderer extends JLabel implements ListCellRenderer
{
    private static final ImageIcon schemaIcon = new ImageIcon(ImageIcon.class.getResource("/net/sf/postgeoolap/gui/resources/schema.GIF"));
    
    public Component getListCellRendererComponent(JList list, Object value, 
        int index, boolean isSelected, boolean cellHasFocus)
    {
        String s = value.toString();
        this.setText(s);
        this.setIcon(SchemaCellRenderer.schemaIcon);
        this.setForeground(isSelected ? list.getBackground() : list.getForeground());
        this.setBackground(isSelected ? list.getForeground() : list.getBackground());
        this.setEnabled(list.isEnabled());
        this.setFont(list.getFont());
        this.setOpaque(true);
        
        return this;
    }
}