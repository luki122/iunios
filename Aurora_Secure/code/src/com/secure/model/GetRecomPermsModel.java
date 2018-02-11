package com.secure.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lbe.protocol.LBERecommendPkgPerm;
import com.lbe.protocol.LBERecommendPkgPermProtoBuf.PkgPermResponse;
import com.lbe.protocol.LBERecommendPkgPermProtoBuf.PkgPermResponse.PkgPermission;
import com.secure.data.AppInfo;
import com.secure.data.AppsInfo;
import com.secure.data.MyPkgPermission;
import com.secure.utils.LogUtils;
import com.secure.utils.StringUtils;
import com.secure.utils.mConfig;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 *从LBE后台获取应用推荐的权限配置表
 */
public class GetRecomPermsModel{
	private static GetRecomPermsModel instance;
	private AtomicBoolean isDuringGetRecomPerms = new AtomicBoolean(false);
	private Context context;
	private HashMap<String,MyPkgPermission> appsRecomPermsMap;
	private boolean isNeedClear;
	private MyHandler handler;
	
	/**
	 * 如果instance为null，不会创建
	 * @return
	 */
	public static synchronized GetRecomPermsModel getInstance() {
		return instance;
	}
	
	/**
	 * 必须在UI线程中初始化 ,如果instance为null，则会创建一个
	 * @param context
	 * @return
	 */
	public static synchronized GetRecomPermsModel getInstance(Context context) {
		if (instance == null) {
			instance = new GetRecomPermsModel(context);
		}
		return instance;
	}

	private GetRecomPermsModel(Context context){
		this.context = context.getApplicationContext();
		appsRecomPermsMap = new HashMap<String,MyPkgPermission>();
		
		//下面代码必须指定UI线程的Looper，防止如果该类定义在子线程内没有Looper而出错
		handler = new MyHandler(Looper.getMainLooper());
		readCacheStr(context);
	}

	private boolean readCacheStr(Context context){
		if(context == null){
			return false;
		}
		boolean result = true;	
		String str = null;
		synchronized (mConfig.cache_file_name_of_RecomPerms){
		   str = FileModel.getInstance(context).readFile(mConfig.cache_file_name_of_RecomPerms);
		   LogUtils.printWithLogCat(
				   GetRecomPermsModel.class.getName(),
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
						MyPkgPermission myPkgPermission = new MyPkgPermission();
						if(myPkgPermission.parseJson(item)){
							synchronized(appsRecomPermsMap){
								appsRecomPermsMap.put(myPkgPermission.getPackageName(), 
										myPkgPermission);
							}						
						}
					}
				}
			}			
		}
	}
	
	/**
	 * 应用启动时调用这个函数，
	 * 必须是在所有应用数据获取完毕以后才能调用该函数
	 */
	public void applicationStart(){
		if(appsRecomPermsMap == null){
			return ;
		}
		AppInfoModel appInfoModel = ConfigModel.getInstance(context).getAppInfoModel();
		AppsInfo userAppsInfo = appInfoModel.getThirdPartyAppsInfo();
		if(userAppsInfo == null){
			return ;
		}
		ArrayList <String> needGetRecomPermsPkgList = new ArrayList <String>();
		for(int i=0;i<userAppsInfo.size();i++){
			AppInfo tmp = (AppInfo)userAppsInfo.get(i);
			if(tmp == null){
				continue;
			}
			if(!appsRecomPermsMap.containsKey(tmp.getPackageName())){
				needGetRecomPermsPkgList.add(tmp.getPackageName());
			}
		}		
		if(needGetRecomPermsPkgList.size() == 0){
			return ;
		}
		String[] needGetRecomPermsPkgStrs = new String[needGetRecomPermsPkgList.size()];
		for(int i=0;i<needGetRecomPermsPkgList.size();i++){
			needGetRecomPermsPkgStrs[i] = needGetRecomPermsPkgList.get(i);
		}
		if(needGetRecomPermsPkgStrs.length == userAppsInfo.size()){
			GetRecomPermsFromLBE(needGetRecomPermsPkgStrs,true);
		}else{
			GetRecomPermsFromLBE(needGetRecomPermsPkgStrs,false);
		}	
	}
	
	/**
	 * 安装应用的时候调用该函数
	 * @param packageName
	 */
	public void inStallApp(AppInfo appInfo){
		if(appInfo == null){
			return ;
		}
		
		String[] needGetRecomPermsPkgStrs = new String[1];
		needGetRecomPermsPkgStrs[0] = appInfo.getPackageName();
		
		GetRecomPermsFromLBE(needGetRecomPermsPkgStrs,false);	
	}
	
	/**
	 * 删除应用时调用该函数
	 * @param packageName
	 */
	public void unInStallApp(String pkgName){
		if(pkgName == null){
			return ;
		}
		if(appsRecomPermsMap != null && 
				appsRecomPermsMap.containsKey(pkgName)){
			synchronized(appsRecomPermsMap){
				appsRecomPermsMap.remove(pkgName);
			}			
		}
	}
	
	/**
	 * 获取某个应用推荐的权限配置
	 * @param packageName
	 * @return
	 */
	public MyPkgPermission getPkgPermission(String packageName){
		MyPkgPermission myPkgPermission = null;
		if(StringUtils.isEmpty(packageName) || appsRecomPermsMap == null){
			return myPkgPermission;
		}
		myPkgPermission = appsRecomPermsMap.get(packageName);
		return myPkgPermission;
	}
	
	/**
	 * 从LBE后台获取指定应用的推荐权限配置
	 * @param packageNames
	 * @param isNeedClear 是否需要清除之前缓存的数据
	 * @return
	 */
	private boolean GetRecomPermsFromLBE(final String[] packageNames,boolean isNeedClear){
		if(isDuringGetRecomPerms.get()){
			return false;
		}
		isDuringGetRecomPerms.set(true);
		this.isNeedClear = isNeedClear;
		
		new Thread() {
			@Override
			public void run() { 
				PkgPermResponse response = getRecomPermsFunc(packageNames);			
				Message msg = new Message();
				msg.obj = response;
				handler.sendMessage(msg);
				isDuringGetRecomPerms.set(false);
			}
		}.start();
		
		return true;
	}
	
	private PkgPermResponse getRecomPermsFunc(String[] packageNames){	
		PkgPermResponse response = null;	
		try{
			response = LBERecommendPkgPerm.getRecommendPerms(packageNames, context, mConfig.LBE_ID);
		} catch (Exception e) {
			//return new GetPermTaskResult (false, null, "Invalid protocol buffer exception.");
		} 
		return response;
	}
	
	private class MyHandler extends Handler{		
		public MyHandler(Looper looper){
           super(looper);
        }

		@Override
	    public void handleMessage(Message msg) {    	
			dealData((PkgPermResponse)msg.obj,isNeedClear);
			saveWebStr(context);
			if(ConfigModel.getInstance() != null){
				ConfigModel.getInstance().getAppInfoModel().recomPermsChange();
			}			
	    }
	}
	
	private void dealData(PkgPermResponse response ,boolean isNeedClear){
		if(response == null || appsRecomPermsMap == null){
			return ;
		}	
		List<PkgPermission> permissions = response.getPermissionsList();
		if (permissions.size() == 0) {
			return;
		}	
		if(isNeedClear){
			synchronized(appsRecomPermsMap){
				appsRecomPermsMap.clear();
			}			
		}	
		for (PkgPermission perm : permissions) {
			MyPkgPermission myPkgPermission = new MyPkgPermission();
			myPkgPermission.setPackageName(perm.getPackageName());
			myPkgPermission.setPermissionsAccept(perm.getPermissionsAccept());
			myPkgPermission.setPermissionsPrompt(perm.getPermissionsPrompt());
			myPkgPermission.setPermissionsReject(perm.getPermissionsReject());
			synchronized(appsRecomPermsMap){
				appsRecomPermsMap.put(perm.getPackageName(), myPkgPermission);
			}		
		}		
	}
	
	private void saveWebStr(Context context){
		String needSaveStr = getNeedSaveStr();
		if(context == null || StringUtils.isEmpty(needSaveStr)){
			return ;
		}

		synchronized (mConfig.cache_file_name_of_RecomPerms){
			 LogUtils.printWithLogCat(
					 GetRecomPermsModel.class.getName(),
					"save:"+needSaveStr);
			FileModel.getInstance(context).writeFile(mConfig.cache_file_name_of_RecomPerms,needSaveStr);
		}		
	}
		
	private String getNeedSaveStr(){
		if(appsRecomPermsMap == null ||
				appsRecomPermsMap.size() == 0){
			return null;
		}	
		
		JSONObject json = new JSONObject();
		JSONArray jsonList = new JSONArray();
		
		synchronized(appsRecomPermsMap){
			Set<String> keySet = appsRecomPermsMap.keySet();
		    for (String packageName : keySet){
		    	MyPkgPermission myPkgPermission = appsRecomPermsMap.get(packageName);
		    	jsonList.add(myPkgPermission.getJson());
		    } 
		}
		
		json.put("list", jsonList);		
		return json.toJSONString();
	}
	 
	public static void releaseObject(){
		if(instance != null){
			if(instance.appsRecomPermsMap != null){
				synchronized(instance.appsRecomPermsMap){
					instance.appsRecomPermsMap.clear();
				}				
			}
			if(mConfig.SET_NULL_OF_CONTEXT){
				instance.context = null;
			}
			instance = null;
		}
		
	}
}
