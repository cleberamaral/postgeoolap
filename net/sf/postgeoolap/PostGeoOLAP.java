package net.sf.postgeoolap;

import javax.swing.JFrame;
import javax.swing.UIManager;

import net.sf.postgeoolap.gui.MainFrame;

public class PostGeoOLAP
{
    public static void main(String[] args)
    {
        
        try
        {
            UIManager.setLookAndFeel(
                "com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        }
        catch (Exception e)
        {
        }
        
        JFrame mainFrame = new MainFrame();
        mainFrame.setVisible(true);
    }
}
