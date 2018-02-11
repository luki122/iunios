package com.privacymanage.request.http;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.privacymanage.proto.BodyOfSendEmail;
import com.privacymanage.proto.Head;
import com.privacymanage.proto.HttpProtocol;
import com.privacymanage.utils.LogUtils;
import com.privacymanage.utils.NetworkUtils;
import com.privacymanage.utils.UrlUtils;
import com.privacymanage.utils.Utils;
import android.content.Context;
import com.privacymanage.data.HttpData;

public class SendEmailModel extends HttpModel {	
	private String errorCode;
	private int type;
	private String address;
	private String password;

	/**
	 * @param context
	 * @param type 取值0或1，0表示：找回密码；1表示：密码重复提示
	 * @param address
	 * @param password
	 */
	public SendEmailModel(Context context,int type,String address,String password){
		super(context,UrlUtils.getSendEmailUrl());
		TAG = SendEmailModel.class.getName();	
		this.type = type;
		this.address = address;
		this.password = password;
	}
	
	@Override
	public boolean postRequest() {
		LogUtils.printWithLogCat(TAG,"postRequest()");
		if(!NetworkUtils.isConn(context)){
			setHttpData(false,HttpData.STATUS.ERROR_OF_NET);
			return false;
		}
		return super.postRequest();
	}

	@Override
	public String createPostReqData() {
		String version = "1.0";
		try {
			version = Utils.getVersionName(context);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Head head = new Head(version,Utils.getImsi(context));
		
		BodyOfSendEmail body = new BodyOfSendEmail();
		body.setType(type);
		body.setAddress(address);
		body.setPassword(password);
				
		String postStr = HttpProtocol.createPostData(context,head,body);
		LogUtils.printWithLogCat(TAG,postStr);
		return postStr;
	}
	
	@Override
	public void resetData() {
		errorCode = "";
		super.resetData();
	}

	@Override
	public boolean DoThing(InputStream in) {
		try {			
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			for (String s = reader.readLine(); s != null; s = reader.readLine()) {
				builder.append(s);
			}
			String str = builder.toString();
			LogUtils.printWithLogCat(TAG,str);
            parseItem(str);
		} catch (Exception e) {
			LogUtils.printWithLogCat(TAG,e.toString());
			e.printStackTrace();
		}
		return true;
	}

	private void parseItem(String str) throws Exception {	
		JSONObject json = JSON.parseObject(str);
		if(json == null || json.isEmpty()){
			return ;
		}		
		errorCode = json.getString("code");	
		if(SUCCESS_CODE.equals(errorCode)){
            //sucess
		}
	}
		
	public String getErrorCode(){
		return errorCode;
	}
}
