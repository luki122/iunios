package com.aurora.account.xmlparser;

import java.io.InputStream;
import java.util.List;

import com.aurora.account.bean.AppConfigInfo;



import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;


public class PullAppParser implements AppParser {
	
	@Override
    public ArrayList<AppConfigInfo> parse(InputStream is) throws Exception {
		ArrayList<AppConfigInfo> apps = null;
		AppConfigInfo app = null;
		
//		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//		XmlPullParser parser = factory.newPullParser();
		
		XmlPullParser parser = Xml.newPullParser();	//由android.util.Xml创建一个XmlPullParser实例
    	parser.setInput(is, "UTF-8");				//设置输入流 并指明编码方式

		int eventType = parser.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			switch (eventType) {
			case XmlPullParser.START_DOCUMENT:
				apps = new ArrayList<AppConfigInfo>();
				break;
			case XmlPullParser.START_TAG:
				if (parser.getName().equals("app")) {
					app = new AppConfigInfo();
				} else if (parser.getName().equals("app_name")) {
					eventType = parser.next();
					app.setApp_name(parser.getText());
				} else if (parser.getName().equals("app_packagename")) {
					eventType = parser.next();
					app.setApp_packagename(parser.getText());
				} else if (parser.getName().equals("app_uri")) {
					eventType = parser.next();
					app.setApp_uri(parser.getText());
				} else if (parser.getName().equals("app_type")) {
					eventType = parser.next();
					app.setApp_type(parser.getText());
				} else if (parser.getName().equals("app_syncself")) {
					eventType = parser.next();
					app.setApp_syncself(parser.getText().equalsIgnoreCase("true") ? true : false);
				}
				break;
			case XmlPullParser.END_TAG:
				if (parser.getName().equals("app")) {
					apps.add(app);
					app = null;	
				}
				break;
			}
			eventType = parser.next();
		}
		return apps;
	}
    
	@Override
    public String serialize(List<AppConfigInfo> apps) throws Exception {
//		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//		XmlSerializer serializer = factory.newSerializer();
		
    	XmlSerializer serializer = Xml.newSerializer();	//由android.util.Xml创建一个XmlSerializer实例
    	StringWriter writer = new StringWriter();
    	serializer.setOutput(writer);	//设置输出方向为writer
		serializer.startDocument("UTF-8", true);
		serializer.startTag("", "apps");
		for (AppConfigInfo app : apps) {
			serializer.startTag("", "app");
			//serializer.attribute("", "app_name", app.getApp_name());
			
			serializer.startTag("", "app_name");
			serializer.text(app.getApp_name());
			serializer.endTag("", "app_name");
			
			serializer.startTag("", "app_packagename");
			serializer.text(app.getApp_packagename());
			serializer.endTag("", "app_packagename");
			
			serializer.startTag("", "app_uri");
			serializer.text(app.getApp_uri());
			serializer.endTag("", "app_uri");
			
			serializer.endTag("", "app");
		}
		serializer.endTag("", "apps");
		serializer.endDocument();
		
		return writer.toString();
    }
}
