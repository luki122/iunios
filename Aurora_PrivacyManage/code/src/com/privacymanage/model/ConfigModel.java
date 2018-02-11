package com.privacymanage.model;

import android.content.Context;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.privacymanage.activity.CustomApplication;
import com.privacymanage.data.ConfigData;
import com.privacymanage.utils.LogUtils;
import com.privacymanage.utils.StringUtils;
import com.privacymanage.utils.mConfig;

public class ConfigModel {   
	private static Object sGlobalLock = new Object();
	private static ConfigModel instance;
	private final String TAG = ConfigModel.class.getName();
	private ConfigData configData = null;
	private Context mApplicationContext;    

	public static ConfigModel getInstance(Context context) {
		synchronized (sGlobalLock) {
			if (instance == null) {
				instance = new ConfigModel();
			}
			return instance;
		}		
	}
	
	private ConfigModel() {
        this.mApplicationContext = CustomApplication.getApplication();
        configData = new ConfigData();
        configData.setLastAccountId(0);
        parseJson(mApplicationContext,mConfig.CONFIG_FILE);
	}
	
	/**
	 * 获取配置信息
	 * @return 返回值不会为null
	 */
	public ConfigData getConfigData(){
		return configData;
	}
	
	public void saveConfigData(){
		writeJson(mApplicationContext,mConfig.CONFIG_FILE);
	}
	
	private void parseJson(Context context,String fileName) {	
		String jsonData = FileSyncModel.getInstance(context).readAndSynFile(fileName);
		LogUtils.printWithLogCat(TAG, ""+jsonData);
	    try {	   
		    JSONObject json = JSON.parseObject(jsonData);
		    if (json != null && !json.isEmpty()) {		 	
			  configData.setLastAccountId(json.getLongValue("lastAccountId"));		 
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	private void writeJson(Context context,String fileName) {		
		if(StringUtils.isEmpty(fileName) || context == null){
			return ;
		}		
		if(configData == null){
			return ;
		}	
		JSONObject json = new JSONObject();	
		json.put("lastAccountId", configData.getLastAccountId());		
		FileSyncModel.getInstance(context).writeAndSynFile(fileName, json.toJSONString());
	}
}
