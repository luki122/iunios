package com.aurora.ota.config;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.util.Xml;

public class RegionXMLParser implements ConfigParser {

	@Override
	public List<Config> parse(InputStream is) throws Exception {
		
		List<Config> configs = null;  
		Config config = null;  
          
//      XmlPullParserFactory factory = XmlPullParserFactory.newInstance();  
//      XmlPullParser parser = factory.newPullParser();  
          
        XmlPullParser parser = Xml.newPullParser(); //由android.util.Xml创建一个XmlPullParser实例  
        parser.setInput(is, "UTF-8");               //设置输入流 并指明编码方式  
  
        int eventType = parser.getEventType();  
        while (eventType != XmlPullParser.END_DOCUMENT) {  
            switch (eventType) {  
            case XmlPullParser.START_DOCUMENT:  
                configs = new ArrayList<Config>();  
                break;  
            case XmlPullParser.START_TAG:  
                if (parser.getName().equals("region")) {  
                    config = new Config();  
                } else if (parser.getName().equals("id")) {  
                    eventType = parser.next();  
                    config.setRegion(Integer.parseInt(parser.getText()));  
                } 
                break;  
            case XmlPullParser.END_TAG:  
                if (parser.getName().equals("region")) {  
                    configs.add(config);  
                    config = null;      
                }  
                break;  
            }  
            eventType = parser.next();  
        }  
        return configs;  
	}

}
