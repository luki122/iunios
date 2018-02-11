package com.privacymanage.proto;

import android.content.Context;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class BodyOfSendEmail implements ProtoBody{
	
	private int type;
	private String address;
	private String password;
	
	/**
	 * 取值0或1，0表示：找回密码；1表示：密码重复提示
	 * @param type
	 */
	public void setType(int type){
		this.type = type;
	}
	
	public void setAddress(String address){
		this.address = address;
	}
	
	public void setPassword(String password){
		this.password = password;
	}
	
	@Override
	public Object getBody(Context context) {
		if(context == null){
			return null;
		}				
		try {				
			JSONObject bodyparam = new JSONObject();
			bodyparam.put("type",type);
			bodyparam.put("address",address);
			bodyparam.put("code",password);
			return bodyparam;		
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
}
