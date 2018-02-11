package com.aurora.puremanager.provider;

import java.util.ArrayList;
import java.util.List;
import com.aurora.puremanager.data.AppInfo;
import com.aurora.puremanager.sqlite.AuroraSecureSqlite;
import com.aurora.puremanager.utils.ApkUtils;
import com.aurora.puremanager.utils.mConfig;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class AuroraAppInfosProvider extends BaseContentProvider {
      
    private static final String URL_STR  = "content://com.provider.AuroraAppInfosProvider";
    public static final Uri CONTENT_URI = Uri.parse(URL_STR);  

	@Override
	public String getTableName() {
		return AuroraSecureSqlite.TABLE_NAME_OF_appsInfo;
	}

	@Override
	public Uri getContentUri() {
		return CONTENT_URI;
	}
	
	public static void insertOrUpdateDate(Context context,
    		AppInfo appInfo){
		if(context == null || 
				appInfo == null){
        	return ;
        }
		
		ContentValues values = new ContentValues();
		values.put(AuroraSecureSqlite.PACKAGE_NAME, appInfo.getPackageName());
		values.put(AuroraSecureSqlite.APP_VERSION, appInfo.getVersionCode());
		values.put(AuroraSecureSqlite.IS_USER_APP, appInfo.getIsUserApp());
		values.put(AuroraSecureSqlite.IS_SYS_WHITE_APP, appInfo.getIsSysWhiteApp());
//		values.put(AuroraSecureSqlite.IS_HAVE_NET_PERM, appInfo.getIsHaveNetworkingPermission());
		
		if(isHave(context, 
				getQueryWhere(),
				getQueryValue(appInfo.getPackageName()),
				CONTENT_URI)){
			context.getContentResolver().update(
					CONTENT_URI, 
					values,
					getQueryWhere(),
					getQueryValue(appInfo.getPackageName()));
		}else{
			context.getContentResolver().insert(CONTENT_URI,values);
		}
		UseOperateInfoProvider.insertOrUpdateDate(context,appInfo);
	}
	
	public static void deleteDate(Context context,
    		AppInfo appInfo){
		if(appInfo == null){
        	return ;
        }		
		deleteDate(context,appInfo.getPackageName());
	}
	
	public static void deleteDate(Context context,
    		String packageName){
		if(context == null || 
				packageName == null){
        	return ;
        }
		
		context.getContentResolver().delete(
				CONTENT_URI, 
				getQueryWhere(),
				getQueryValue(packageName));	
		UseOperateInfoProvider.deleteDate(context, packageName);
	}
	
	/**
	 * 获取数据中存放所有应用的个数
	 * @param context
	 * @return
	 */
	public static int getAllAppsNum(Context context){
		int appNums = 0;
		if(context == null){
			return appNums;
		}
		
		String[] columns={AuroraSecureSqlite.FIELD_ID}; //需要返回的列名
		
		Cursor cursor = null;
		try{
			cursor =context.getContentResolver().query(CONTENT_URI,columns,null,null,null);	
		}catch(Exception e){
			//nothing
		}
		
		synchronized(CONTENT_URI){			
	    	if (cursor != null){
	    		appNums = cursor.getCount();
    			cursor.close();      			     			    
	    	}	
		}
		return appNums;
	}
	
	/**
	 * 从数据库中读取所有应用信息
	 * @param context
	 * @param packname
	 * @return 返回不可能为null
	 */
	public static List<AppInfo> queryAllAppsInfo(Context context){
		List<AppInfo> appInfoList = new ArrayList<AppInfo>();
		
		if(context == null){
        	return appInfoList;
        }
		
		String[] columns={AuroraSecureSqlite.PACKAGE_NAME,
    			AuroraSecureSqlite.APP_NAME,
    			AuroraSecureSqlite.APP_NAME_PINGYIN,
    			AuroraSecureSqlite.APP_VERSION,
    			AuroraSecureSqlite.IS_USER_APP,
    			AuroraSecureSqlite.IS_SYS_WHITE_APP,
    			AuroraSecureSqlite.IS_HAVE_NET_PERM}; //需要返回的列名
		
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
    		    			cursor.getColumnIndexOrThrow(AuroraSecureSqlite.PACKAGE_NAME));
    		    	AppInfo appInfo = new AppInfo(); 
    		    	appInfo.setPackageName(pkgName);
    		    	appInfo.setApplicationInfo(ApkUtils.getApplicationInfo(context, pkgName));
    		    	appInfo.setVersionCode(cursor.getInt(
    		    			cursor.getColumnIndexOrThrow(AuroraSecureSqlite.APP_VERSION)));
    		    	if(cursor.getInt(cursor.getColumnIndex(AuroraSecureSqlite.IS_USER_APP)) == 0){
    		    		appInfo.setIsUserApp(false);
    		    	}else{
    		    		appInfo.setIsUserApp(true);
    		    	}
    		    	if(cursor.getInt(cursor.getColumnIndex(AuroraSecureSqlite.IS_SYS_WHITE_APP)) == 0){
    		    		appInfo.setIsSysWhiteApp(false);
    		    	}else{
    		    		appInfo.setIsSysWhiteApp(true);
    		    	}
    		    	if(cursor.getInt(cursor.getColumnIndex(AuroraSecureSqlite.IS_HAVE_NET_PERM)) == 0){
    		    		appInfo.setIsHaveNetworkingPermission(false);
    		    	}else{
    		    		appInfo.setIsHaveNetworkingPermission(true);
    		    	}
    		    	
    		    	appInfo.setIsBlockAd(UseOperateInfoProvider.queryAdBlockState(context, pkgName));
    		    	appInfoList.add(appInfo);
    		    }
    			cursor.close();      			     			    
	    	}	
		}
    	return appInfoList;
	}  
	
	public static AppInfo getAppsInfo(Context context,String pkgName){		
		AppInfo appInfo = null;
		if(context == null || pkgName == null){
        	return appInfo;
        }
		String[] columns={AuroraSecureSqlite.PACKAGE_NAME,
    			AuroraSecureSqlite.APP_NAME,
    			AuroraSecureSqlite.APP_NAME_PINGYIN,
    			AuroraSecureSqlite.APP_VERSION,
    			AuroraSecureSqlite.IS_USER_APP,
    			AuroraSecureSqlite.IS_SYS_WHITE_APP,
    			AuroraSecureSqlite.IS_HAVE_NET_PERM}; //需要返回的列名
		
		Cursor cursor = null;
		try{
			cursor =context.getContentResolver().query(CONTENT_URI,
					columns,
					getQueryWhere(),
					getQueryValue(pkgName),
					null);
		}catch(Exception e){
			//nothing
		}		 
		
		synchronized(CONTENT_URI){			
	    	if (cursor != null){
	    		if(cursor.moveToFirst()){
		    		appInfo = new AppInfo(); 
			    	appInfo.setPackageName(cursor.getString(
			    			cursor.getColumnIndexOrThrow(AuroraSecureSqlite.PACKAGE_NAME)));
			    	appInfo.setApplicationInfo(ApkUtils.getApplicationInfo(context, pkgName));
			    	appInfo.setVersionCode(cursor.getInt(
			    			cursor.getColumnIndexOrThrow(AuroraSecureSqlite.APP_VERSION)));
			    	if(cursor.getInt(cursor.getColumnIndex(AuroraSecureSqlite.IS_USER_APP)) == 0){
			    		appInfo.setIsUserApp(false);
			    	}else{
			    		appInfo.setIsUserApp(true);
			    	}
			    	if(cursor.getInt(cursor.getColumnIndex(AuroraSecureSqlite.IS_SYS_WHITE_APP)) == 0){
			    		appInfo.setIsSysWhiteApp(false);
			    	}else{
			    		appInfo.setIsSysWhiteApp(true);
			    	}
			    	if(cursor.getInt(cursor.getColumnIndex(AuroraSecureSqlite.IS_HAVE_NET_PERM)) == 0){
			    		appInfo.setIsHaveNetworkingPermission(false);
			    	}else{
			    		appInfo.setIsHaveNetworkingPermission(true);
			    	}
			    	appInfo.setIsBlockAd(UseOperateInfoProvider.queryAdBlockState(context, pkgName));
	    		}   
	    		cursor.close();  
	    	}	
		}
    	return appInfo;
	}
    
	/**
	 * 从数据库中查询指定应用的版本号
	 * @param context
	 * @param packname
	 * @return 应用的版本号，-1表示没有找到指定应用
	 */
	public static int queryAppVersion(Context context,String pkgName){
		int versionCode = -1;
		if(context == null || pkgName == null){
        	return versionCode;
        }
		String[] columns={AuroraSecureSqlite.APP_VERSION}; 
		
		Cursor cursor = null;
		try{
			cursor =context.getContentResolver().query(CONTENT_URI,
					columns,
					getQueryWhere(),
					getQueryValue(pkgName),
					null);
		}catch(Exception e){
			//nothing
		}
			
		synchronized(CONTENT_URI){			
	    	if (cursor != null){
	    		if(cursor.moveToFirst()){
	    			versionCode = cursor.getInt(
		    				cursor.getColumnIndexOrThrow(AuroraSecureSqlite.APP_VERSION));	
	    		}	    		
    			cursor.close();      			     			    
	    	}	
		}
    	return versionCode;
	} 
	
    private static String getQueryWhere(){
    	return AuroraSecureSqlite.PACKAGE_NAME+" = ?";
    }
    
    private static String[] getQueryValue(String packageName){
    	String[] whereValue = {packageName};
    	return whereValue;
    }
    
    /**
     * 当contentProvider中数据变化时，通知“联网管理“应用更新数据
     * @param context
     * @param changeAppInfo 如果为null，表示是应用批量改变
     */
    public static void notifyChangeForNetManageApp(Context context,AppInfo changeAppInfo){
    	if(context != null){
    		if(changeAppInfo == null){
    			context.getContentResolver().notifyChange(CONTENT_URI, null);	
    		}/*else if(changeAppInfo.getIsHaveNetworkingPermission()){
    			//对于含有联网权限的当前应用数据的改变，才需要通知“联网管理“应用更新数据
    			context.getContentResolver().notifyChange(CONTENT_URI, null);
    		}   */ 		
    	}  	
    }
}