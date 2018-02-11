package com.android.settings.config;



import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.util.Log;
import android.os.SystemProperties;


public class SettingConfigUtils {
	
	public static final String SETTING_CONFIG_FILE_NAME =  File.separator +"system" + File.separator +"iuni" +
		    File.separator  + "aurora" +File.separator +"setting" + File.separator + "SettingConfig.xml" ;
	public static final String TAG = "SettingConfigUtils";
	private static HashMap<String, Boolean> statusMap = null ;
	public static String prefixName = getModel();
	/**
	 * 配置文件中没有此项的话，默认返回false
	 */
	 public static boolean isHaveThisOption(String configName) {
		 String name = prefixName +"_" + configName;
		 Log.i(TAG,"qury name =  " + name);
		 if(null == statusMap){
			 parseConfig();
			 
		 } else {
			 if(statusMap.containsKey(name)){
				 return statusMap.get(name);
			 } else {
				 Log.i(TAG,"This name is not exit " );
				 return false;
			 }
		 }
    	return false;
    }
	 
	 public static void parseConfig(){
		 new Thread(new Runnable() {
			
			@Override
			public void run() {
/*				if(statusMap != null){
					Log.e(TAG," statusMap is already parse" );
					return;
				}*/
				File configFile = new File(SETTING_CONFIG_FILE_NAME);
				
				if(configFile.exists()){
	     			if(configFile.isFile()){
	     				 try {  
	     					Log.e(TAG,"configFile is exist " );
	     	             InputStream is = new FileInputStream(configFile);  
	     	 				 SettingXMLParser  parser = new SettingXMLParser();
	     	 				 statusMap = (HashMap<String, Boolean>) parser.parse(is);  //解析输入流  
	     	 				Log.e(TAG,"statusMap  =  "   + statusMap);
	     	                    
	     	                } catch (Exception e) {  
	     	                  Log.e(TAG,"parse the config_file exception " );
	     	                  e.printStackTrace();
	     	                } 
	     			}
	     	}
				 
				
			}
		}).start();
	 }
	 
	 public static String getModel() {
	    	String modelString = SystemProperties.get("ro.gn.iuniznvernumber");
	    	if(modelString == null) return "Unknow";
	    	String array [] = modelString.split("-");
	    	if(array.length > 1){
	    		return array[1];
	    	}else {
	    		return "Unknow";
	    	}
	        //return SystemProperties.get("ro.product.model");
	    }
	 public static void test(){
		 if(null == statusMap){
			 Log.i(TAG,"statusMap is null" );
			 return;
		 }
		/* Log.i(TAG,"1111111111" );
		 Iterator iter = statusMap.entrySet().iterator();  
		 Log.i(TAG,"hasNext  = "  + iter.hasNext());
		 while (iter.hasNext()) {  
		     Map.Entry entry = (Map.Entry) iter.next();  
		     Object key = entry.getKey();  
		     Object val = entry.getValue();  
		     
		     Log.i(TAG,"qury key =  " +(String) key);
		     Log.i(TAG,"qury val =  " +(Boolean)( val));
		 }  */
		 Log.i(TAG,"qury   " + statusMap.get("wid"));
	 }
}
