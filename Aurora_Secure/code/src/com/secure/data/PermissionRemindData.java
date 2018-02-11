package com.secure.data;

import java.util.ArrayList;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 记录某个应用的某个版本已经弹出了权限提示界面，并且用户点击了确认按钮
 *
 */
public class PermissionRemindData extends BaseData {
	private String packageName;
	private int versionCode;
	private ArrayList<Integer> perIdList;

	public PermissionRemindData() {
		super("PermissionRemindData");
	}
	
	public void setPackageName(String packageName){
		this.packageName = packageName;
	}
	
	public String getPackageName(){
		return this.packageName;
	}
	
	public void setPerIdList(ArrayList<Integer> perIdList){
		this.perIdList = perIdList;
	}
			
	public ArrayList<Integer> getPerIdList(){
		return this.perIdList;
	}
	
	/**
	 * 判断权限类表中是否有这个权限
	 * @param perId
	 * @return
	 */
	public boolean isHavePermission(int perId){
		if(perIdList == null){
			return false;
		}
		for(int i=0;i<perIdList.size();i++){
			Integer tmp = perIdList.get(i);
			if(tmp != null && tmp.intValue() == perId){
				return true;
			}
		}
		return false;
	}
	
	public void setVersionCode(int versionCode){
		this.versionCode = versionCode;
	}
	
	public int getVersionCode(){
		return this.versionCode;
	}
		
	public JSONObject getJson() {
		JSONObject json = new JSONObject();	
		json.put("packageName", packageName);
		json.put("versionCode", versionCode);
			
		JSONArray perIdJsonList = new JSONArray();
		int size = perIdList==null?0:perIdList.size();
		for (int i = 0; i < size; i++) {
			JSONObject item = new JSONObject();	
			item.put("perId", perIdList.get(i));
			perIdJsonList.add(item);		
		}
		json.put("perIdList", perIdJsonList);
		return json;
	}
	
	/**
	 * 解析json对象
	 * @param json
	 * @return true 解析成功  false 解析失败
	 */
	public boolean parseJson(JSONObject json) throws Exception{	
		boolean result = false;
	    if (json != null && !json.isEmpty()) {
		   packageName = json.getString("packageName");
		   versionCode = json.getIntValue("versionCode");
		  
		   JSONArray perIdJsonList = json.getJSONArray("perIdList");
		   if(perIdJsonList != null) {
			   if(perIdList == null){
				   perIdList = new ArrayList<Integer>(); 
			   }else{
				   perIdList.clear();
			   }
			   for(int i=0;i<perIdJsonList.size();i++){
				   JSONObject item = perIdJsonList.getJSONObject(i);
				   if (!item.isEmpty()) {
					   perIdList.add(item.getInteger("perId"));
				   }
			   }
		   }
		   result = true;			 			 
	   }		  
	   return result;
	}
}
