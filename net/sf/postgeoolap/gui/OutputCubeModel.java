package net.sf.postgeoolap.gui;

import java.util.ArrayList;
import java.util.Observable;

public class OutputCubeModel extends Observable implements OutputProgress
{
    private ArrayList stringList;
    
    public OutputCubeModel()
    {
        this.stringList = new ArrayList();
    }
    
    public void addString(String string)
    {
        this.stringList.add(string);
        this.setChanged();
        this.notifyObservers();	
    }
    
    public String getLastString()
    {
        return (String) this.stringList.get(this.stringList.size() - 1);
    }
    
    public String getString(int index)
    {
        return (String) this.stringList.get(index);
    }
}
