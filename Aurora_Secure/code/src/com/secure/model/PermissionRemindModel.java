package com.secure.model;

import java.util.HashMap;
import java.util.Set;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.secure.data.PermissionRemindData;
import com.secure.utils.LogUtils;
import com.secure.utils.StringUtils;
import com.secure.utils.mConfig;
import android.content.Context;

/**
 *记录某个应用的某个版本已经弹出了权限提示界面，并且用户点击了确认按钮
 */
public class PermissionRemindModel{
	private static PermissionRemindModel instance;
	private HashMap<String,PermissionRemindData> perRemindMap;
	private Context context;
	
	/**
	 * 如果instance为null，不会创建
	 * @return
	 */
	public static synchronized PermissionRemindModel getInstance() {
		return instance;
	}
	
	/**
	 * 必须在UI线程中初始化 ,如果instance为null，则会创建一个
	 * @param context
	 * @return
	 */
	public static synchronized PermissionRemindModel getInstance(Context context) {
		if (instance == null) {
			instance = new PermissionRemindModel(context);
		}
		return instance;
	}

	private PermissionRemindModel(Context context){
		this.context = context.getApplicationContext();
		perRemindMap = new HashMap<String,PermissionRemindData>();
		readCacheStr(this.context );
	}

	private boolean readCacheStr(Context context){
		if(context == null){
			return false;
		}
		boolean result = true;	
		String str = null;
		synchronized (mConfig.cache_file_name_of_perRemind){
		   str = FileModel.getInstance(context).readFile(mConfig.cache_file_name_of_perRemind);
		   LogUtils.printWithLogCat(
				   PermissionRemindModel.class.getName(),
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
						PermissionRemindData perRemindData = new PermissionRemindData();
						if(perRemindData.parseJson(item)){
							perRemindMap.put(perRemindData.getPackageName(), perRemindData);
						}
					}
				}
			}			
		}
	}
	
	public PermissionRemindData getPermissionRemindData(String packageName){
		if(packageName == null || 
				perRemindMap == null){
			return null;
		}			
		return perRemindMap.get(packageName);		
	}
	
	/**
	 * 添加或修改用户已经确认的对应的应用的权限提示信息
	 * @param perRemindData
	 */
	public void addOrModifyPermissionRemindData(PermissionRemindData perRemindData){
		if(perRemindData == null){
			return ;
		}
		if(perRemindMap == null){
			perRemindMap = new HashMap<String,PermissionRemindData>();
		}
		perRemindMap.put(perRemindData.getPackageName(), perRemindData);
		saveWebStr(context);
	}
	
	private void saveWebStr(Context context){
		String needSaveStr = getNeedSaveStr();
		if(context == null || StringUtils.isEmpty(needSaveStr)){
			return ;
		}

		synchronized (mConfig.cache_file_name_of_perRemind){
			LogUtils.printWithLogCat(
					PermissionRemindModel.class.getName(),
					"save:"+needSaveStr);
			FileModel.getInstance(context).writeFile(mConfig.cache_file_name_of_perRemind,needSaveStr);
		}		
	}
		
	private String getNeedSaveStr(){
		if(perRemindMap == null || 
				perRemindMap.size() == 0){
			return null;
		}	
		
		JSONObject json = new JSONObject();
		JSONArray jsonList = new JSONArray();
		
		Set<String> keySet = perRemindMap.keySet();
	    for (String packageName : keySet){
	    	PermissionRemindData perRemindData = perRemindMap.get(packageName);
	    	jsonList.add(perRemindData.getJson());
	    } 
		json.put("list", jsonList);		
		return json.toJSONString();
	}
	 
	public static void releaseObject(){
		if(instance != null){
			if(instance.perRemindMap != null){
				instance.perRemindMap.clear();
			}
			if(mConfig.SET_NULL_OF_CONTEXT){
				instance.context = null;
			}	
			instance = null;
		}
		
	}
}
