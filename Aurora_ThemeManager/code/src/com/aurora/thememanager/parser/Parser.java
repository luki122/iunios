package com.aurora.thememanager.parser;

import java.util.List;

import com.aurora.thememanager.parser.JsonParser.CallBack;

import android.util.Log;

/**
 * 
 * if you want to parse  object from json or xml,use it.
 * 
 *  <h3>Example</h3>
* Suppose we'd like to parse a stream of messages from json such as the following: <pre> {@code
* [
*   {
*     "id": 912345678901,
*     "text": "How do I read JSON on Android?",
*     "geo": null,
*     "user": {
*       "name": "android_newb",
*       "followers_count": 41
*      }
*   },
*   {
*     "id": 912345678902,
*     "text": "@android_newb just use android.util.JsonReader!",
*     "geo": [50.454722, -104.606667],
*     "user": {
*       "name": "jesse",
*       "followers_count": 2
*     }
*   }
* ]}</pre>
* This code implements the parser for the above structure: <pre>   {@code
*
*   public List<Object> getData(String json){
*         //JsonParser is an abstract class
*         Parser parser = new Parser(new JsonParser());
*        return parser.startParse(json);
*  }}</pre>
*  </br>
*  or, parse xml from string or inputstream: <pre>   {@code
*
*   public List<Object> getData(String xml){
*         //JsonParser is an abstract class
*         Parser parser = new Parser(new XmlParser());
*        return parser.startParse(xml);
*  }}</pre>
*  </br>
*  so easy !!!
*/
public class Parser extends Thread implements DataParser{
	
	private static final String TAG = "Parser";
	
	public static final int TYPE_THEME_PAKAGE = 0;
	
	public static final int TYPE_WALLPAPER = 1;
	
	public static final int TYPE_TIME_WALLPAPER = 2;
	
	public static final int TYPE_RINGTONG = 3;
	
	private DataParser mParser;
	
	private List<Object> mResult;
	
	private Object mSource;
	
	public Parser(){
		
	}
	
	public Parser(DataParser parser){
		mParser = parser;
	}
	
	
	
	/**
	 * invoker call this method to parse json from server of inputstream
	 * @param source   json need to parse
	 * @return  target object
	 */
	public List<Object> startParser(Object source){
		try {
			return parser(source);
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "paser data catched exception-->"+e+" \n"+ source);
		}
		return null;
	}

	@Override
	public List<Object> parser(Object source) throws ParserException {
		mSource = source;
		return mParser.parser(mSource);
	}

	public void printParserName(){
		if(mParser != null){
			Log.d("Parser", mParser.getClass().getName());
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		if(mParser != null){
			try {
				mResult =  mParser.parser(mSource);
				
			} catch (ParserException e) {
				Log.d("parser", ""+e);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void  setCallBack(CallBack callback){
		if(mParser != null){
			if(mParser instanceof JsonParser){
				((JsonParser)mParser).setCallBack(callback);
			}
		}
	}
	
	
	

}
