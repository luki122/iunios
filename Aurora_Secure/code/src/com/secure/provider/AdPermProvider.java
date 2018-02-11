package com.secure.provider;

import com.adblock.data.AdProviderData;
import com.secure.data.MyArrayList;
import com.secure.sqlite.AuroraSecureSqlite;
import com.secure.utils.LogUtils;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;

public class AdPermProvider extends BaseContentProvider {
      
    private static final String URL_STR  = "content://com.provider.AdPermProvider";
    public static final Uri CONTENT_URI = Uri.parse(URL_STR); 

	@Override
	public String getTableName() {
		return AuroraSecureSqlite.TABLE_NAME_OF_adPermission;
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
			AdProviderData adProviderData){
		if(context == null || 
				adProviderData == null || 
				adProviderData.getProviderName() == null){
        	return ;
        }
		MyArrayList<String> adPermList = adProviderData.getAdPermList();
		int size = adPermList==null?0:adPermList.size();
		for(int i=0;i<size;i++){
			String permStr = adPermList.get(i);
			if(permStr == null){
				continue ;
			}
			ContentValues values = new ContentValues();
			values.put(AuroraSecureSqlite.AD_PROVIDER_NAME, adProviderData.getProviderName());
			values.put(AuroraSecureSqlite.PERMISSION_NAME, permStr);
			
			if(isHave(context, 
					getQueryWhere(),
					getQueryValue(adProviderData.getProviderName(),permStr),
					CONTENT_URI)){
				context.getContentResolver().update(
						CONTENT_URI, 
						values,getQueryWhere(),
						getQueryValue(adProviderData.getProviderName(),permStr));
			}else{
				context.getContentResolver().insert(CONTENT_URI,values);
			}
		}		
	}
	
	/**
	 * 插入或者更新数据
	 * @param context
	 * @param appInfo
	 */
	public static void insertDate(Context context,
			AdProviderData adProviderData){
		if(context == null || 
				adProviderData == null || 
				adProviderData.getProviderName() == null){
        	return ;
        }
		MyArrayList<String> adPermList = adProviderData.getAdPermList();
		int size = adPermList==null?0:adPermList.size();
		for(int i=0;i<size;i++){
			String permStr = adPermList.get(i);
			if(permStr == null){
				continue ;
			}
			ContentValues values = new ContentValues();
			values.put(AuroraSecureSqlite.AD_PROVIDER_NAME, adProviderData.getProviderName());
			values.put(AuroraSecureSqlite.PERMISSION_NAME, permStr);
			
			context.getContentResolver().insert(CONTENT_URI,values);
		}		
	}
	
	/**
	 * 删除某个广告插件对应的所有的权限
	 * @param context
	 * @param adProviderData
	 */
	public static void deleteDate(Context context,
			AdProviderData adProviderData){
		if(adProviderData == null){
        	return ;
        }
		deleteDate(context,adProviderData.getProviderName());
	}
	
	/**
	 * 删除某个广告插件对应的所有的权限
	 * @param context
	 * @param packageName
	 */
	public static void deleteDate(Context context,
    		String providerName){
		if(context == null || 
				providerName == null){
        	return ;
        }	
		context.getContentResolver().delete(
				CONTENT_URI, 
				AuroraSecureSqlite.AD_PROVIDER_NAME+" = ?",
				new String[]{providerName});
	}

	/**
	 * 获取指定广告商包含的"权限",并将查找的"权限"存在adProviderData中
	 * @param context
	 * @param adProviderData
	 */
	public static void queryAdPermList(Context context,AdProviderData adProviderData){
		if(context == null || 
				adProviderData == null ||
				adProviderData.getProviderName() == null){
        	return ;
        }	
		PackageManager packageManager = context.getPackageManager();
		String[] columns={
				AuroraSecureSqlite.PERMISSION_NAME}; //需要返回的列名			
		
		String where = AuroraSecureSqlite.AD_PROVIDER_NAME+" = ?";		
		String[] whereValue = {adProviderData.getProviderName()};
		
		Cursor cursor = null;
		try{
			cursor =context.getContentResolver().query(CONTENT_URI,columns,where,whereValue,null);
		}catch(Exception e){
			LogUtils.printWithLogCat(AdPermProvider.class.getName(), e.toString());
		} 

		synchronized(CONTENT_URI){				   	
	    	if (cursor != null){			
    			while (cursor.moveToNext()) {			
    				adProviderData.addAdPermStr(packageManager,cursor.getString(
        					cursor.getColumnIndexOrThrow(AuroraSecureSqlite.PERMISSION_NAME)));
    		    }
    			cursor.close();	      			     			    
	    	}
		}
	} 
	
    private static String getQueryWhere(){
    	return AuroraSecureSqlite.AD_PROVIDER_NAME+" = ?"+ 
             " AND "+AuroraSecureSqlite.PERMISSION_NAME+" = ?";
    }
    
    private static String[] getQueryValue(String providerName,String permStr){
    	String[] whereValue = {providerName,permStr};
    	return whereValue;
    } 
}
