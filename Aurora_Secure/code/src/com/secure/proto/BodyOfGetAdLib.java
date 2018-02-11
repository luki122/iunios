package com.secure.proto;

import android.content.Context;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class BodyOfGetAdLib implements ProtoBody{
	
	private int libVersion = 0;
	
	public void setLibVersion(int libVersion){
		this.libVersion = libVersion;
	}
	
	@Override
	public Object getBody(Context context) {
		if(context == null){
			return null;
		}				
		try {				
			JSONObject bodyparam = new JSONObject();
			bodyparam.put("lib_version",libVersion);
			return bodyparam;		
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
}
