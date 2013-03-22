package net.sf.postgeoolap.gui;

public interface OutputProgress
{
    public void addString(String string);
    public String getLastString();
    public String getString(int index);
}
