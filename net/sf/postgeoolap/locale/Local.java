package net.sf.postgeoolap.locale;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Local 
{
	private static Locale locale;
	private static ResourceBundle resources;
	
	public static void setLocale(String language, String country)
	{
		Local.locale = new Locale(language, country);
		Local.loadResources();
	}
	
	private static void initialize()
	{
	    Local.locale = Locale.getDefault();
	    Local.loadResources();
	}
	
	private static void loadResources()
	{
	    try
	    {
	        Local.resources = ResourceBundle.getBundle("net.sf.postgeoolap.locale.Resources", Local.locale);
	    }
	    catch (MissingResourceException e)
	    {
	        Local.resources = ResourceBundle.getBundle("net.sf.postgeoolap.locale.Resources", Locale.ENGLISH);
	    }
	}
	
	public static Locale getLocale()
	{
		return Local.locale;
	}
	
	public static String getString(String key)
	{
		if (Local.locale == null)
		    Local.initialize();
		return Local.resources.getString(key);
	}
	
	public static Object getObject(String key)
	{
		if (Local.locale == null)
		    Local.initialize();
		return Local.resources.getObject(key);
	}
}