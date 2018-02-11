package com.secure.provider;

import com.adblock.data.AdProviderData;
import com.adblock.data.AppAdData;
import com.secure.data.MyArrayList;
import com.secure.sqlite.AuroraSecureSqlite;
import com.secure.utils.LogUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class AppAdInfoProvider extends BaseContentProvider {
      
    private static final String URL_STR  = "content://com.provider.AppAdInfoProvider";
    public static final Uri CONTENT_URI = Uri.parse(URL_STR); 

	@Override
	public String getTableName() {
		return AuroraSecureSqlite.TABLE_NAME_OF_appsAdInfo;
	}

	@Override
	public Uri getContentUri() {
		return CONTENT_URI;
	}
	
	/**
	 * 插入或者更新数据
	 * @param context
	 * @param appInfo
	 */
	public static void insertOrUpdateDate(Context context,
			AppAdData appAdData){
		if(context == null || 
				appAdData == null ||
				appAdData.getPkgName() == null){
        	return ;
        }
		MyArrayList<AdProviderData> adProviderList = appAdData.getAdProviderList();
		int size = adProviderList==null?0:adProviderList.size();
		AdProviderData adProviderData = null;
		for(int i=0;i<size;i++){
			adProviderData = adProviderList.get(i);
			if(adProviderData == null || 
					adProviderData.getProviderName() == null){
				continue ;
			}
			ContentValues values = new ContentValues();
			values.put(AuroraSecureSqlite.PACKAGE_NAME, appAdData.getPkgName());
			values.put(AuroraSecureSqlite.AD_PROVIDER_NAME, adProviderData.getProviderName());
			values.put(AuroraSecureSqlite.APP_VERSION, appAdData.getVersionCode());	
			
			if(isHave(context, 
					getQueryWhere(),
					getQueryValue(appAdData.getPkgName(),adProviderData.getProviderName()),
					CONTENT_URI)){
				context.getContentResolver().update(
						CONTENT_URI, 
						values,
						getQueryWhere(),
						getQueryValue(appAdData.getPkgName(),adProviderData.getProviderName()));
			}else{
				context.getContentResolver().insert(CONTENT_URI,values);
			}
		}		
	}
	
	/**
	 * 删除该应用对应的所有的权限
	 * @param context
	 * @param appInfo
	 */
	public static void deleteDate(Context context,
			AppAdData appAdData){
		if( appAdData == null){
        	return ;
        }
		deleteDate(context,appAdData.getPkgName());
	}
	
	/**
	 * 删除该应用对应的所有的权限
	 * @param context
	 * @param packageName
	 */
	public static void deleteDate(Context context,
    		String pkgName){
		if(context == null || 
				pkgName == null){
        	return ;
        }		
		context.getContentResolver().delete(
				CONTENT_URI, 
				AuroraSecureSqlite.PACKAGE_NAME+" = ?",
				new String[]{pkgName});
	}
	
	/**
	 * 清空数据库中的内容
	 * @param context
	 */
	public static void clearData(Context context){
		if(context == null){
        	return ;
        }		
		context.getContentResolver().delete(
				CONTENT_URI, 
				AuroraSecureSqlite.FIELD_ID+"!=?",
				new String[]{"-1"});
	}

	/**
	 * 查询指定广告库数据
	 * @param context
	 * @param pkgName
	 * @return 返回可能为null
	 */
	public static AppAdData getAppAdData(Context context,
			String pkgName){
		AppAdData appAdData = null;
		if(context == null || 
				pkgName == null){
        	return appAdData;
        }	
		String[] columns={
				AuroraSecureSqlite.AD_PROVIDER_NAME,
    			AuroraSecureSqlite.APP_VERSION}; //需要返回的列名			
		
		String where = AuroraSecureSqlite.PACKAGE_NAME+" = ?";		
		String[] whereValue = {pkgName};
		
		Cursor cursor = null;
		try{
			cursor =context.getContentResolver().query(CONTENT_URI,columns,where,whereValue,null);
		}catch(Exception e){
			LogUtils.printWithLogCat(AppAdInfoProvider.class.getName(), e.toString());
		} 

		synchronized(CONTENT_URI){				   	
	    	if (cursor != null){			
    			while (cursor.moveToNext()) {
    				if(appAdData == null){
    					appAdData = new AppAdData();
    				}
    				if(cursor.isFirst()){ 				
    					appAdData.setPkgName(pkgName);
    					appAdData.setVersionCode(cursor.getInt(
        		    		cursor.getColumnIndexOrThrow(AuroraSecureSqlite.APP_VERSION)));
    				} 
    				AdProviderData adProviderData = AdLibProvider.getAdProviderData(
    						context,cursor.getString(
        		    		cursor.getColumnIndexOrThrow(AuroraSecureSqlite.AD_PROVIDER_NAME)));
    				if(adProviderData != null){
    					appAdData.addAdProviderData(adProviderData);
    				}
    		    }
    			cursor.close();	      			     			    
	    	}
		}
    	return appAdData;
	} 
	
	/**
     * 获取查找条件,注意这个ContentProvider的查找条件有两个，一个是packageName，一个是permissionName
     */
    private static String getQueryWhere(){
    	return AuroraSecureSqlite.PACKAGE_NAME+" = ?"+ 
               " AND "+AuroraSecureSqlite.AD_PROVIDER_NAME+" = ?";
    }
    
    private static String[] getQueryValue(String pkgName,String providerName){
    	String[] whereValue = {pkgName,providerName};
    	return whereValue;
    } 
}
