package com.secure.model;

import java.util.HashMap;
import java.util.Set;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.secure.data.NetForbidHintData;
import com.secure.utils.LogUtils;
import com.secure.utils.StringUtils;
import com.secure.utils.mConfig;
import android.content.Context;

/**
 *记录并判断应用需不需要弹禁止联网的提示框
 */
public class NetForbidHintModel{
	private static NetForbidHintModel instance;
	private HashMap<String,NetForbidHintData> appsMap;
	private Context context;
	
	/**
	 * 如果instance为null，不会创建
	 * @return
	 */
	public static synchronized NetForbidHintModel getInstance() {
		return instance;
	}
	
	/**
	 * 必须在UI线程中初始化 ,如果instance为null，则会创建一个
	 * @param context
	 * @return
	 */
	public static synchronized NetForbidHintModel getInstance(Context context) {
		if (instance == null) {
			instance = new NetForbidHintModel(context);
		}
		return instance;
	}

	private NetForbidHintModel(Context context){
		this.context = context.getApplicationContext();
		appsMap = new HashMap<String,NetForbidHintData>();
		readCacheStr(context);
	}

	private boolean readCacheStr(Context context){	
		if(context == null){
			return false;
		}
		boolean result = true;	
		String str = null;
		synchronized (mConfig.cache_file_name_of_netHint){
		   str = FileModel.getInstance(context).readFile(mConfig.cache_file_name_of_netHint);
		   LogUtils.printWithLogCat(
				   NetForbidHintModel.class.getName(),
					"read:"+str);
		}	
		if(StringUtils.isEmpty(str)){
			return false;
		}
		
		try {
            parseItem(str);
            result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}
	
	private void parseItem(String str) throws Exception {						
		JSONObject json = JSON.parseObject(str);	
		if (json != null && !json.isEmpty())  {			
			JSONArray list = json.getJSONArray("list");
			if ( list != null) {
				for (int i = 0; i < list.size(); i++) {
					JSONObject item = list.getJSONObject(i);
					if (!item.isEmpty()) {
						NetForbidHintData netForbidHintData = new NetForbidHintData();
						if(netForbidHintData.parseJson(item)){
							appsMap.put(netForbidHintData.getPackageName(), netForbidHintData);
						}
					}
				}
			}			
		}
	}
	
	public NetForbidHintData getNetForbidHintData(String packageName){
		if(packageName == null || 
				appsMap == null){
			return null;
		}			
		return appsMap.get(packageName);		
	}
	
	/**
	 * 添加或修改
	 * @param perRemindData
	 */
	public void addOrModifyNetForbidHintData(NetForbidHintData netForbidHintData){
		if(netForbidHintData == null){
			return ;
		}
		if(appsMap == null){
			appsMap = new HashMap<String,NetForbidHintData>();
		}
		appsMap.put(netForbidHintData.getPackageName(), netForbidHintData);
		saveWebStr(context);
	}
	
	private void saveWebStr(Context context){
		String needSaveStr = getNeedSaveStr();
		if(context == null || StringUtils.isEmpty(needSaveStr)){
			return ;
		}

		synchronized (mConfig.cache_file_name_of_netHint){
			LogUtils.printWithLogCat(
					NetForbidHintModel.class.getName(),
					"save:"+needSaveStr);
			FileModel.getInstance(context).writeFile(mConfig.cache_file_name_of_netHint,needSaveStr);
		}		
	}
		
	private String getNeedSaveStr(){
		if(appsMap == null || 
				appsMap.size() == 0){
			return null;
		}	
		
		JSONObject json = new JSONObject();
		JSONArray jsonList = new JSONArray();
		
		Set<String> keySet = appsMap.keySet();
	    for (String packageName : keySet){
	    	NetForbidHintData netForbidHintData = appsMap.get(packageName);
	    	jsonList.add(netForbidHintData.getJson());
	    } 
		json.put("list", jsonList);		
		return json.toJSONString();
	}
	 
	public static void releaseObject(){
		if(instance != null){
			if(instance.appsMap != null){
				instance.appsMap.clear();
			}
			
			if(mConfig.SET_NULL_OF_CONTEXT){
				instance.context = null;
			}
			instance = null;
		}		
	}
}
