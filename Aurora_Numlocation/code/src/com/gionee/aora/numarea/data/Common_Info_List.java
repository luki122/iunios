package com.gionee.aora.numarea.data;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.base.compare.ISearchable;
import com.gionee.aora.numarea.util.GetXMLValue;

/**
 * Copyright (c) 2001, ÉîÛÚÊÐ°ÂÈíÍøÂç¿Æ¼¼¹«Ë¾ÑÐ·¢²¿
 * All rights reserved.
 *
 * @file Common_Info_List.java
 * ÕªÒª:³£ÓÃÐÅÏ¢ÁÐ±í
 *
 * @author yewei
 * @data 2011-5-30
 * @version 
 *
 */
public class Common_Info_List
{
	private Common_Info[] iCommon_Info;
	
	public Common_Info[] getiCommon_Info() {
		return iCommon_Info;
	}

	public void setiCommon_Info(Common_Info[] iCommon_Info) {
		this.iCommon_Info = iCommon_Info;
	}

	public Common_Info_List(DataInputStream aDis) throws Exception
	{
		int len = aDis.readInt();
		byte[] data = new byte[len];
		aDis.read(data);
		GetXMLValue xmlParser = new GetXMLValue(iXmlHandler);
		xmlParser.parserXml(data);
	}
	
	/**
	 * ¸ù¾ÝºÅÂë²éÕÒ³£ÓÃÐÅÏ¢
	 * @param aSearch
	 * @param aTelNum
	 * @return
	 */
	public Common_Info searchInfo(ISearchable aSearch, String aTelNum)
	{
		int index = aSearch.search(iCommon_Info, aTelNum, 0);
		if(index != -1)
			return iCommon_Info[index];
		else
			return null;
	}
	private DefaultHandler iXmlHandler = new DefaultHandler()
	{
		private String iTag;
		final private String TAG_TYPE = "type";
		final private String TAG_ITEM = "item";
		private String iName;//ÖÖÀàÃû³Æ
		private String iTelNum;//µç»°ºÅÂë
		private String iGroupName;//·þÎñÖÖÀà
		private List<Common_Info> iList = new ArrayList< Common_Info >();
		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
		 */
		@Override
		public void characters(char[] ch , int start , int length) throws SAXException
		{
			// TODO Auto-generated method stub
			if(TAG_ITEM.equals(iTag))
			{//item±êÇ©Ôò½ØÈ¡textÇøÓòÄÚÈÝ,
				String value = new String(ch,start,length);
				int c = value.indexOf(',');
				iTelNum = value.substring(0,c);
				iName = value.substring(c + 1, value.length());
			}
			else
				super.characters(ch, start, length);
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public void endElement(String uri , String localName , String qName) throws SAXException
		{
			// TODO Auto-generated method stub
			if(TAG_ITEM.equals(localName))
			{//¶ÁÈ¡ÍêÒ»¸öItem,Ìí¼Óµ½listÖÐ
				iList.add(new Common_Info(iGroupName, iTelNum, iName));
			}
			else
				super.endElement(uri, localName, qName);
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public void startElement(String uri , String localName , String qName , Attributes attributes) throws SAXException
		{
			// TODO Auto-generated method stub
			iTag = localName;
			if(TAG_TYPE.equals(iTag))
			{//type±êÇ©Ôò¼ÇÂ¼nameµÄÄÚÈÝ
				iGroupName = attributes.getValue(0);
			}
			else
				super.startElement(uri, localName, qName, attributes);
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#endDocument()
		 */
		@Override
		public void endDocument() throws SAXException
		{
			// TODO Auto-generated method stub
			iCommon_Info = new Common_Info[iList.size()];
			iCommon_Info = iList.toArray(iCommon_Info);
			super.endDocument();
		}
		
		
		
	};
}
