package com.secure.provider.open;

import java.util.HashMap;
import java.util.HashSet;

import com.secure.data.AppInfo;
import com.secure.data.AppsInfo;
import com.secure.data.AutoStartData;
import com.secure.data.MyArrayList;
import com.secure.model.AutoStartModel;
import com.secure.model.ConfigModel;
import com.secure.sqlite.OpenDataSqlite;
import com.secure.utils.LogUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class AutoStartAppProvider extends BaseOpenContentProvider {
      
    private static final String URL_STR  = "content://com.secure.provider.open.AutoStartAppProvider";
    public static final Uri CONTENT_URI = Uri.parse(URL_STR); 
    private static final String TAG = AutoStartAppProvider.class.getName();

	@Override
	public String getTableName() {
		return OpenDataSqlite.TABLE_NAME_OF_AllowAutoStartApp;
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
    public static HashSet<String> loadAutoStartAppListInDB(Context context) {
    	MyArrayList<String> autoStartAppList = queryAllAppsInfo(context);
    	mAutoStartAppList.clear();
    	for(String app: autoStartAppList.getDataList()) {
    		mAutoStartAppList.add(app);
    	}
    	return mAutoStartAppList;
    }
    
    /**
     * 
     * Vulcan created this method in 2015年1月14日 下午2:41:40 .
     * @param context
     */
	public static void initProvider1(Context context){
		if(context == null){
			return ;
		}
		MyArrayList<String> autoStartAppList = getAutoStartAppsInfo(context);
		MyArrayList<String> sqliteAppList = queryAllAppsInfo(context);
		MyArrayList<String> needAddToSqliteList = new MyArrayList<String>();
		
		for(String app1: autoStartAppList.getDataList()) {
			LogUtils.printWithLogCat("vautostart", "initProvider: =====autostart app in actual: " + app1);
		}
		
		for(String app: sqliteAppList.getDataList()) {
			LogUtils.printWithLogCat("vautostart", "initProvider: =====autostart app in cfg: " + app);
		}
		
		for(int i=0;i<autoStartAppList.size();i++){
			String target = autoStartAppList.get(i);
			if(target == null){
				continue ;
			}
			if(canFindInList(target,sqliteAppList)){
				sqliteAppList.remove(target);
			}else{
				needAddToSqliteList.add(target);
			}
		}
		
		//添加数据
        for(int i=0;i<needAddToSqliteList.size();i++){
			insertOrUpdateDate(context,needAddToSqliteList.get(i));
		}
        
        //删除数据
        for(int i=0;i<sqliteAppList.size();i++){
        	deleteDate(context,sqliteAppList.get(i));
        }    	
	}
	
	/**
	 * new requirement: autostart list in database only could be edited by user.
	 * Vulcan created this method in 2015年1月13日 下午2:18:21 .
	 * @param context
	 */
	public static void initProvider2(Context context){
		if(context == null){
			return ;
		}
		MyArrayList<String> autoStartAppList = getAutoStartAppsInfo(context);
		MyArrayList<String> sqliteAppList = queryAllAppsInfo(context);
		
		for(String app1: autoStartAppList.getDataList()) {
			LogUtils.printWithLogCat("vautostart", "initProvider: =====autostart app in actual: " + app1);
		}
		
		for(String app: sqliteAppList.getDataList()) {
			LogUtils.printWithLogCat("vautostart", "initProvider: =====autostart app in cfg: " + app);
		}

		return;
	}
	
	/**
	 * 在数据库中增加一个应用包，意味着该应用配置成可自启动。
	 * Vulcan created this method in 2015年1月15日 下午2:33:17 .
	 * @param pkgName
	 */
	public static void addAppInDB(Context context, String pkgName) {
		LogUtils.printWithLogCat("vautostart", "addAppInDB: pkgName = " + pkgName);
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
	 * 打开某个应用的自启动
	 * @param context
	 * @param pkgName
	 */
	public static void openAutoStart(Context context,String pkgName){
		insertOrUpdateDate(context,pkgName);
	}
	
	/**
	 * 关掉某个应用的自启动
	 * @param context
	 * @param pkgName
	 */
	public static void closeAutoStart(Context context,String pkgName){
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
	 * 获取当前允许自启动的应用列表
	 * @param context
	 * @return 返回值不为null
	 */
	private static MyArrayList<String> getAutoStartAppsInfo(Context context){
		MyArrayList<String> autoStartAppList = new MyArrayList<String>();
		AppsInfo userAppsInfo = ConfigModel.getInstance(context).
				getAppInfoModel().getThirdPartyAppsInfo();
    	if(userAppsInfo == null){
    		return autoStartAppList;
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
				autoStartAppList.add(appInfo.getPackageName());
			}
    	}
    	return autoStartAppList;
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
    
    private final static HashSet<String> mAutoStartAppList = new HashSet<String>();
}