package com.aurora.puremanager.provider;

import com.aurora.puremanager.data.AppInfo;
import com.aurora.puremanager.sqlite.AuroraSecureSqlite;
import com.aurora.puremanager.utils.ApkUtils;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class UseOperateInfoProvider extends BaseContentProvider {
      
    private static final String URL_STR  = "content://com.provider.AuroraUseOperateInfoProvider";
    public static final Uri CONTENT_URI = Uri.parse(URL_STR);  

	@Override
	public String getTableName() {
		return AuroraSecureSqlite.TABLE_NAME_OF_appUseOperate;
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
		if(isHave(context, 
				getQueryWhere(),
				getQueryValue(appInfo.getPackageName()),
				CONTENT_URI)){
			context.getContentResolver().update(
					CONTENT_URI, 
					values,
					getQueryWhere(),
					getQueryValue(appInfo.getPackageName()));
			appInfo.setIsBlockAd(queryAdBlockState(context,appInfo.getPackageName()));
		}else{
			values.put(AuroraSecureSqlite.IS_BLOCKED_AD, appInfo.getIsBlockAd());
			context.getContentResolver().insert(CONTENT_URI,values);
		}			
	}
	
	/**
	 * 更新权限状态
	 * @param context
	 * @param appInfo
	 */
	public static void UpdateAdBlockState(Context context,
    		AppInfo appInfo){
		if(context == null || appInfo == null){
        	return ;
        }
		
		int bolckState = appInfo.getIsBlockAd()?1:0;
		ContentValues values = new ContentValues();
		values.put(AuroraSecureSqlite.IS_BLOCKED_AD, bolckState);
			
		context.getContentResolver().update(
				CONTENT_URI, 
				values,getQueryWhere(),
				getQueryValue(appInfo.getPackageName()));
	}
	
	/**
	 * 从数据库中查询指定应用的广告拦截状态
	 * @param context
	 * @param packname
	 * @return 
	 */
	public static boolean queryAdBlockState(Context context,String pkgName){
		boolean blockState = false;
		if(context == null || pkgName == null){
        	return blockState;
        }
		String[] columns={AuroraSecureSqlite.IS_BLOCKED_AD}; 
		
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
	    			if(cursor.getInt(cursor.getColumnIndex(AuroraSecureSqlite.IS_BLOCKED_AD)) == 0){
	    				 blockState = false;
    		    	}else{
    		    		 blockState = true;
    		    	}
	    		}	    		
    			cursor.close();      			     			    
	    	}	
		}
    	return blockState;
	} 
	
	/**
	 * 说明：为了业务逻辑需要，在删除该条记录时，
	 * 会先判断该应用是否真的被卸载，如果没有被卸载，是不会执行删除操作的。
	 * @param context
	 * @param appInfo
	 */
	public static void deleteDate(Context context,
    		AppInfo appInfo){
		if(appInfo == null){
        	return ;
        }		
		deleteDate(context,appInfo.getPackageName());
	}
	
	/**
	 * 说明：为了业务逻辑需要，在删除该条记录时，
	 * 会先判断该应用是否真的被卸载，如果没有被卸载，是不会执行删除操作的。
	 * @param context
	 * @param packageName
	 */
	public static void deleteDate(Context context,
    		String packageName){
		if(context == null || 
				packageName == null ||
				ApkUtils.getApplicationInfo(context, packageName) != null){
        	return ;
        }
		
		context.getContentResolver().delete(
				CONTENT_URI, 
				getQueryWhere(),
				getQueryValue(packageName));	
	}
	
    private static String getQueryWhere(){
    	return AuroraSecureSqlite.PACKAGE_NAME+" = ?";
    }
    

    private static String[] getQueryValue(String packageName){
    	String[] whereValue = {packageName};
    	return whereValue;
    }
}
