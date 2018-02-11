package com.secure.request.http;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import com.adblock.data.AdClassData;
import com.adblock.data.AdProviderData;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.secure.proto.BodyOfGetAdLib;
import com.secure.proto.Head;
import com.secure.proto.HttpProtocol;
import com.secure.utils.LogUtils;
import com.secure.utils.MySharedPref;
import com.secure.utils.NetworkUtils;
import com.secure.utils.UrlUtils;
import com.secure.utils.Utils;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;

import com.secure.data.HttpData;
import com.secure.data.MyArrayList;

public class GetAdLibModel extends HttpModel {	
	private String errorCode;
	private MyArrayList<String> needDeleteProviderName;
	private MyArrayList<AdProviderData> needAddOrModifyProvider;	
	private PackageManager packageManager;

	public GetAdLibModel(Context context){
		super(context,UrlUtils.getAdLibUrl());
		TAG = "GetAdLibModel";
		if(context != null){
			packageManager = context.getPackageManager();
		}		
	}
	
	/**
	 * 读取存放在assets中的adlib文件
	 */
	public void readLocalAdLib(){
		LogUtils.printWithLogCat(TAG,"readLocalAdLib()");
		resetData();
		DoThing(getAssert());
		MySharedPref.saveAlreadyReadAssetsAdlib(context,true);
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
	
	private InputStream getAssert(){
    	InputStream fs=null;
		try {
			AssetManager am = context.getAssets();
			fs = am.open("adlib.json");			
		}catch (Exception e) {
			e.printStackTrace();
		}return fs;
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
		
		BodyOfGetAdLib body = new BodyOfGetAdLib();
		body.setLibVersion(MySharedPref.getAdLibVersion(context));
				
		String postStr = HttpProtocol.createPostData(context,head,body);
		LogUtils.printWithLogCat(TAG,postStr);
		return postStr;
	}
	
	@Override
	public void resetData() {
		errorCode = "";
		if(needDeleteProviderName == null){
			needDeleteProviderName = new MyArrayList<String>();
		}else{
			needDeleteProviderName.clear();
		}
		
		if(needAddOrModifyProvider == null){
			needAddOrModifyProvider = new MyArrayList<AdProviderData>();
		}else{
			needAddOrModifyProvider.clear();
		}
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
//		{"error_code":"0000",
//			"result":{
//			  "lib_version ":1,
//			  "content":[
//			       {"type":1,
//			        "provider_name":"多盟",
//			        "url":"http://duomeng",
//			        "notify_ad":0,//是否有通知栏广告，0：没有，1表示有
//			        "view_ad":1,//是否有视图广告，0：没有，1表示有
//			        "class_comm_name":"cn.domob.android.ads",
//			        "class_list":[{"name":"cn.domob.android.DomobAdView"," desc ":"广告描述1"},
//			                      {"name":"cn.domob.android.ads.DomobFeedsAdView"," desc ":"广告描述2"},
//			                      {"name":"cn.domob.android.ads.DomobFeedsAdView1"," desc ":"广告描述3"}
//			                     ],
//			        "perm_list":[{"name":"android.permission.INTERNET"},
//			                     {"name":"android.permission.ACCESS_NETWORK_STATE"},
//			                     {"name":"android.permission.READ_PHONE_STATE"}  
//			                    ]
//			       }
//			   ]
//			  }
//			} 
		
		JSONObject json = JSON.parseObject(str);
		if(json == null || json.isEmpty()){
			return ;
		}		
		errorCode = json.getString("error_code");	
		if(SUCCESS_CODE.equals(errorCode)){
			JSONObject resultObject = json.getJSONObject("result");
			if (resultObject != null && !resultObject.isEmpty()) {
				int lib_version = resultObject.getIntValue("lib_version") ;
				JSONArray content = resultObject.getJSONArray("content");
				int size = content==null?0:content.size();
				for(int i=0;i<size;i++){
					parseAdProvider(content.getJSONObject(i));
				}
				MySharedPref.saveAdLibVersion(context, lib_version);
			}
		}
	}
			
	public void parseAdProvider(JSONObject item) throws Exception {
		if(item == null || item.isEmpty()){
		   return ;	
		}
		String provider_name = item.getString("provider_name");
		switch(item.getIntValue("type")){
			case -1://-1:表示删除
				needDeleteProviderName.add(provider_name);
				break;
			case 0://0:表示修改
			case 1://1:表示增加
				AdProviderData adProviderData = new AdProviderData();
				adProviderData.setUrl(item.getString("url"));
				adProviderData.setProviderName(provider_name);
				adProviderData.setAdClassCommName(item.getString("class_comm_name"));				
				adProviderData.setIsHaveNotifyAd(item.getIntValue("notify_ad")==1?true:false);
				adProviderData.setIsHaveViewAd(item.getIntValue("view_ad")==1?true:false);
				
				//解析class_list
				JSONArray class_list = item.getJSONArray("class_list");	
				int size = class_list==null?0:class_list.size();
				for(int i=0;i<size;i++){
					AdClassData adClassData = parseAdClass(class_list.getJSONObject(i));
					if(adClassData != null){
						adProviderData.addAdClassData(adClassData);
					}					
				}
				//解析perm_list
				JSONArray perm_list = item.getJSONArray("perm_list");	
				size = perm_list==null?0:perm_list.size();
				for(int i=0;i<size;i++){
					String permName = parsePermName(perm_list.getJSONObject(i));
					if(permName != null){
						adProviderData.addAdPermStr(packageManager,permName);
					}				
				}					
				needAddOrModifyProvider.add(adProviderData);
				break;
		}
	}
	
	private AdClassData parseAdClass(JSONObject item) throws Exception {
		if(item == null || item.isEmpty()){
			return null;
		}
		AdClassData adClassData = new AdClassData();
		adClassData.setName(item.getString("name"));
		adClassData.setDesc(item.getString("desc"));
		return adClassData;
	}
	
	private String parsePermName(JSONObject item) throws Exception {
		if(item == null || item.isEmpty()){
			return null;
		}
		return item.getString("name");
	}
		
	public String getErrorCode(){
		return errorCode;
	}
	
	public MyArrayList<String> getNeedDeleteProviderName(){
		return this.needDeleteProviderName;
	}
	
	public MyArrayList<AdProviderData> getNeedAddOrModifyProvider(){
		return this.needAddOrModifyProvider;
	}
}
