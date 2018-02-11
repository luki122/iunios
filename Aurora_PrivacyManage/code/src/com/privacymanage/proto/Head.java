package com.privacymanage.proto;


import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class Head {
	private String version;
	private String imsi;
		
	public Head(String version,String imsi){
		this.version = version;
		this.imsi = imsi;
	}
	
	public Object getHead(){
		try {
			JSONObject headparam = new JSONObject();			
			headparam.put("imsi", imsi);
			headparam.put("version",version);
			return headparam;		
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
}
