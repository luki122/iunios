package com.aurora.puremanager.provider.open;

import java.util.HashSet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.aurora.puremanager.data.AppInfo;
import com.aurora.puremanager.data.AppsInfo;
import com.aurora.puremanager.data.AutoStartData;
import com.aurora.puremanager.data.MyArrayList;
import com.aurora.puremanager.model.AutoStartModel;
import com.aurora.puremanager.model.ConfigModel;
import com.aurora.puremanager.sqlite.OpenDataSqlite;
import com.aurora.puremanager.utils.LogUtils;

public class FreezedAppProvider extends BaseOpenContentProvider {
      
    private static final String URL_STR  = "content://com.aurora.puremanager.provider.open.AuroraFreezedAppProvider";
    public static final Uri CONTENT_URI = Uri.parse(URL_STR); 
    private static final String TAG = FreezedAppProvider.class.getName();
    private final static HashSet<String> mFreezedAppList = new HashSet<String>();
//    private MyArrayList<String> mFreezedAppList;

	@Override
	public String getTableName() {
		return OpenDataSqlite.TABLE_NAME_OF_FreezedApp;
	}

	@Override
	public Uri getContentUri() {
		return CONTENT_URI;
	}
	
    /**
     * 
     * Vulcan created this method in 2015年1月13日 下午2:33:46 .
     * @param context
     */
    public static HashSet<String> loadFreezedAppListInDB(Context context) {
    	MyArrayList<String> freezedAppList = queryAllAppsInfo(context);
    	mFreezedAppList.clear();
    	for(String app: freezedAppList.getDataList()) {
    		mFreezedAppList.add(app);
    	}
    	return mFreezedAppList;
    }
    
	/**
	 * new requirement: autostart list in database only could be edited by user.
	 * Vulcan created this method in 2015年1月13日 下午2:18:21 .
	 * @param context
	 * @return 
	 */
	public static void initProvider2(Context context){
		if(context == null){
			return ;
		}
		MyArrayList<String> freezedAppList = getFreezedAppsInfo(context);
		MyArrayList<String> sqliteAppList = queryAllAppsInfo(context);
		
		for(String app1: freezedAppList.getDataList()) {
			LogUtils.printWithLogCat("vfreezed", "initProvider: =====freezed app in actual: " + app1);
		}
		
		for(String app: sqliteAppList.getDataList()) {
			LogUtils.printWithLogCat("vfreezed", "initProvider: =====freezed app in cfg: " + app);
		}

		return;
	}
	
	/**
	 * 在数据库中增加一个应用包，意味着该应用配置成可自启动。
	 * Vulcan created this method in 2015年1月15日 下午2:33:17 .
	 * @param pkgName
	 */
	public static void addAppInDB(Context context, String pkgName) {
		LogUtils.printWithLogCat("vfreezed", "addAppInDB: pkgName = " + pkgName);
		insertOrUpdateDate(context,pkgName);
		return;
	}
	
	/**
	 * This method sychcronize the autostart list in database and that in actual.
	 * Vulcan created this method in 2015年1月13日 下午2:17:09 .
	 * @param context
	 */
	public static void initProvider(Context context){
		initProvider2(context);
	}

	/**
	 * 冻结某个应用
	 * @param context
	 * @param pkgName
	 */
	public static void freezedApp(Context context,String pkgName){
		insertOrUpdateDate(context,pkgName);
	}
	
	/**
	 * 解冻某个应用
	 * @param context
	 * @param pkgName
	 */
	public static void freezeApp(Context context,String pkgName){
		deleteDate(context,pkgName);
	}
	
	private static boolean canFindInList(String target,MyArrayList<String> list){
		boolean result = false;
		if(target == null){
			return result;
		}
		int size = list == null?0:list.size();
		for(int i=0;i<size;i++){
			if(target.equals(list.get(i))){
				result = true;
				break;
			}
		}
		return result;
	}
	
	private static void insertOrUpdateDate(Context context,
    		String pkgName){
		if(context == null || 
				pkgName == null){
        	return ;
        }
		
		ContentValues values = new ContentValues();
		values.put(OpenDataSqlite.PACKAGE_NAME, pkgName);
		
		if(isHave(context, 
				getQueryWhere(),
				getQueryValue(pkgName),
				CONTENT_URI)){
            //do nothing
		}else{
			Log.i(TAG,"insert "+pkgName);
			context.getContentResolver().insert(CONTENT_URI,values);
		}
	}
	
	private static void deleteDate(Context context,
    		String packageName){
		if(context == null || 
				packageName == null){
        	return ;
        }
		
		context.getContentResolver().delete(
				CONTENT_URI, 
				getQueryWhere(),
				getQueryValue(packageName));	
		Log.i(TAG,"delete "+packageName);
	}
	
	/**
	 * 获取当前冻结的应用列表
	 * @param context
	 * @return 返回值不为null
	 */
	private static MyArrayList<String> getFreezedAppsInfo(Context context){
		MyArrayList<String> freezedAppList = new MyArrayList<String>();
		AppsInfo userAppsInfo = ConfigModel.getInstance(context).
				getAppInfoModel().getThirdPartyAppsInfo();
    	if(userAppsInfo == null){
    		return freezedAppList;
    	}
    	
    	for(int i=0;i<userAppsInfo.size();i++){
    		AppInfo appInfo = (AppInfo)userAppsInfo.get(i);
    		if(appInfo == null || !appInfo.getIsInstalled()){
    			continue;
    		}
    		
    		AutoStartData autoStartData = AutoStartModel.getInstance(context).
    				getAutoStartData(appInfo.getPackageName());
    		if(autoStartData == null){
    			continue ;
    		}
    		
			if(autoStartData.getIsOpen()){
				freezedAppList.add(appInfo.getPackageName());
			}
    	}
    	return freezedAppList;
	}
	
	/**
	 * 从数据库中读取所有应用信息
	 * @param context
	 * @param packname
	 * @return 返回不可能为null
	 */
	private static MyArrayList<String> queryAllAppsInfo(Context context){
		MyArrayList<String> appInfoList = new MyArrayList<String>();
		
		if(context == null){
        	return appInfoList;
        }
		
		String[] columns={OpenDataSqlite.PACKAGE_NAME}; //需要返回的列名
		
		Cursor cursor = null;
		try{
			cursor =context.getContentResolver().query(CONTENT_URI,columns,null,null,null);	
		}catch(Exception e){
			//nothing
		}
		
		synchronized(CONTENT_URI){			
	    	if (cursor != null){
    			while (cursor.moveToNext()) { 
    				String pkgName = cursor.getString(
    		    			cursor.getColumnIndexOrThrow(OpenDataSqlite.PACKAGE_NAME));
    		    	appInfoList.add(pkgName);
    		    }
    			cursor.close();      			     			    
	    	}	
		}
    	return appInfoList;
	}  

    private static String getQueryWhere(){
    	return OpenDataSqlite.PACKAGE_NAME+" = ?";
    }
   
    private static String[] getQueryValue(String packageName){
    	String[] whereValue = {packageName};
    	return whereValue;
    }
}