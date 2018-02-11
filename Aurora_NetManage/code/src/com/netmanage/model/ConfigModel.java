package com.netmanage.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netmanage.data.ConfigData;
import com.netmanage.utils.StringUtils;
import com.netmanage.utils.mConfig;

import android.content.Context;
import android.util.Log;

/**
 * @author Administrator
 *
 */
public class ConfigModel {   
	private static ConfigModel instance;
	static Object sGlobalLock = new Object();
	private final String tag = "ConfigModel";
	private Context context = null;
	private ConfigData configData = null;

	
	private ConfigModel(Context context) {
		this.context = context.getApplicationContext();
	}
	
	/**
	 * 如果instance为null，不会创建
	 * @return 返回值有可能为空
	 */
	public static ConfigModel getInstance(){
		synchronized (sGlobalLock) {
			return instance;
		}		
	}

	/**
	 * 如果instance为null，则会创建一个
	 * @param context
	 * @return 返回值不可能为null
	 */
	public static ConfigModel getInstance(Context context) {
		synchronized (sGlobalLock) {
			if (instance == null) {
				instance = new ConfigModel(context);
			}
			return instance;
		}
	}
	
	/**
	 * 返回值不可能为null
	 * @return
	 */
	public synchronized ConfigData getConfigData(){
		if(configData == null){
			parseJson(context,mConfig.FILE_CONFIG);
		}	
		return configData;
	}
	
	/**
	 * 解析得到configData数据
	 * @param context
	 * @param fileName
	 */
	private void parseJson(Context context,String fileName) {	
		configData = new ConfigData(); 
		String jsonData = FileModel.getInstance(context).readFile(fileName);		
		Log.i(tag,""+jsonData); 	
        if(StringUtils.isEmpty(jsonData)){
			return ;
		}
	    try {	   
		   JSONObject json = JSON.parseObject(jsonData);	   
		   configData.parseJson(json);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 保存configData数据到SD卡
	 */
	public synchronized void saveConfigData(){
		if(configData == null || context == null){
			return ;
		}
		JSONObject json = configData.getJson();
		if(json != null){
			FileModel.getInstance(context).writeFile(mConfig.FILE_CONFIG, json.toJSONString());
		}				
	}
	
//	public static void releaseObject(){
//		if(instance != null){
//			instance.context = null;
//		}
//		instance = null;
//	}
}
