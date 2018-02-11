package com.secure.proto;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import android.content.Context;

public class HttpProtocol {

	public static synchronized String createPostData(
			Context context ,
			Head head ,
			ProtoBody body){			
		try {
			JSONObject param = new JSONObject();
			if(head != null){
				param.put("head", head.getHead());	
			}
			
			if(body != null){
				Object bodyObject = body.getBody(context);
				if(bodyObject != null){
					param.put("body", bodyObject);
				}				
			}		
			return param.toString();	
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}		
	}	
}
