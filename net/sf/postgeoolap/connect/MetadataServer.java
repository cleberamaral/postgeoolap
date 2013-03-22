package net.sf.postgeoolap.connect;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class MetadataServer
{
    private Document document;
    
    public MetadataServer()
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            //this.document = builder.parse(this.getClass().getResourceAsStream("E:\\postgeoolap\\deploy\\postgeoolap-0.82\\conf\\metadata.xml"));
            this.document = builder.parse(this.getClass().getResourceAsStream("/metadata.xml"));
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace(System.err);
        }
        catch (SAXException e)
        {
            e.printStackTrace(System.err);
        }
        catch (IOException e)
        {
            e.printStackTrace(System.err);
        }
    }
    
    public String getServerLocation()
    {
        return this.getContent("server-location");
    }
    
    public String getDatabaseName()
    {
        return this.getContent("database-name");
    }
    
    public String getUserName()
    {
        return this.getContent("user");
    }
    
    public String getPassword()
    {
        return this.getContent("password");
    }
    
    private String getContent(String tagName)
    {
        return this.document.getElementsByTagName(tagName).item(0).getTextContent();
    }
}
