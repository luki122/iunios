package com.android.settings.config;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;
import android.util.Xml;

public class SettingXMLParser implements IConfigParser {

	@Override
	public Map<String,Boolean> parse(InputStream is) throws Exception {
		
		HashMap<String, Boolean> configs = null;  
		String keyName = null;
          
      XmlPullParserFactory factory = XmlPullParserFactory.newInstance();  
     XmlPullParser parser = factory.newPullParser();  
          
        /*XmlPullParser parser = Xml.newPullParser(); //由android.util.Xml创建一个XmlPullParser实例  */  
        parser.setInput(is, "UTF-8");               //设置输入流 并指明编码方式  

        int eventType = parser.getEventType();  
        while (eventType != XmlPullParser.END_DOCUMENT) {  
            switch (eventType) {  
            case XmlPullParser.START_DOCUMENT:  
                configs = new HashMap<String, Boolean>();  
                break;  
            case XmlPullParser.START_TAG:  
              /*  if (parser.getName().equals("region")) {  
                  
                } else if (parser.getName().equals("id")) {  
                    eventType = parser.next();  
                    config.setRegion(Integer.parseInt(parser.getText()));  
                } */
            	/*if(Integer.parseInt(parser.getText()) == 0){
            		configs.put(parser.getName(), false);
            	} else if(Integer.parseInt(parser.getText()) == 1){
            		configs.put(parser.getName(), true);
            	} else {
            		configs.put(parser.getName(), false);
            	}*/
            	
            	keyName = parser.getName();
            	if(keyName.equals("configs")){
            		
            	}else{
            		parser.next();
	            	if(null !=parser.getText() ){
		            	if("false".equals(parser.getText())){
		            		configs.put(keyName, false);
		            	} else if("true".equals(parser.getText())){
		            		configs.put(keyName, true);
		            	} else {
		            		configs.put(keyName, false);
		            	}
	            	}
            	}
            	
            	Log.e("SettingConfigUtils","START_TAG  name =   "   + keyName);
                break;  
            case XmlPullParser.TEXT:
            //	Log.e("SettingConfigUtils","TEXT  text =   "   + parser.getText());
            	/*if(keyName.equals("configs")){
            		
            	}else{
	            	if(null !=parser.getText() ){
		            	if(Integer.parseInt(parser.getText()) == 0){
		            		configs.put(keyName, false);
		            	} else if(Integer.parseInt(parser.getText())== 1){
		            		configs.put(keyName, true);
		            	} else {
		            		configs.put(keyName, false);
		            	}
	            	}
            	}*/
            	break;
            
            case XmlPullParser.END_TAG:  
               /* if (parser.getName().equals("region")) {  
                    configs.add(config);  
                    config = null;      
                }  */
            	Log.e("SettingConfigUtils","END_TAG " );
                break;  
            }  
            eventType = parser.next();  
        }  
        return configs;  
	}

}
