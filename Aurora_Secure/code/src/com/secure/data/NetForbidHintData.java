package com.secure.data;

import com.alibaba.fastjson.JSONObject;

/**
 * 记录某个应用在联网权限被禁止时是否需要弹提示框
 *
 */
public class NetForbidHintData extends BaseData {
	private String packageName;
	private boolean needHintForSim;
	private boolean needHintForWifi;

	public NetForbidHintData() {
		super("NetForbidHintData");
	}
	
	public void setPackageName(String packageName){
		this.packageName = packageName;
	}
	
	public String getPackageName(){
		return this.packageName;
	}
	
	public void setNeedHintForSim(boolean needHintForSim){
		this.needHintForSim = needHintForSim;
	}
	
	public boolean getNeedHintForSim(){
		return this.needHintForSim ;
	}
	
	public void setNeedHintForWifi(boolean needHintForWifi){
		this.needHintForWifi = needHintForWifi;
	}
	
	public boolean getNeedHintForWifi(){
		return this.needHintForWifi ;
	}
		
	public JSONObject getJson() {
		JSONObject json = new JSONObject();	
		json.put("packageName", packageName);
		json.put("needHintForSim", needHintForSim);
		json.put("needHintForWifi", needHintForWifi);
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
		   needHintForSim = json.getBooleanValue("needHintForSim");
		   needHintForWifi = json.getBooleanValue("needHintForWifi");
		   result = true;			 			 
	   }		  
	   return result;
	}
}
