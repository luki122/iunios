package com.gionee.aora.numarea.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class GetXMLValue
{
  private SAXParserFactory iSaxFactory;
  private SAXParser iSaxParser;
  private XMLReader iXmlReader;

  public GetXMLValue(DefaultHandler aHandler)
  {
    this.iSaxFactory = SAXParserFactory.newInstance();
    try
    {
      this.iSaxParser = this.iSaxFactory.newSAXParser();
      this.iXmlReader = this.iSaxParser.getXMLReader();
      this.iXmlReader.setContentHandler(aHandler);
    }
    catch (ParserConfigurationException e)
    {
      e.printStackTrace();
    }
    catch (SAXException e)
    {
      e.printStackTrace();
    }
  }

  public void release()
  {
    this.iXmlReader = null;
    this.iSaxParser = null;
    this.iSaxFactory = null;
  }

  public void parserXml(byte[] aData)
    throws Exception, SAXException
  {
    parserXml(new InputSource(new ByteArrayInputStream(aData)));
  }

  public void parserXml(InputStream aData)
    throws Exception, SAXException
  {
    parserXml(new InputSource(aData));
  }

  public void parserXml(InputSource aData)
    throws Exception, SAXException
  {
    this.iXmlReader.parse(aData);
  }
}
