package com.secure.provider;

import com.adblock.data.AdClassData;
import com.adblock.data.AdProviderData;
import com.secure.data.MyArrayList;
import com.secure.sqlite.AuroraSecureSqlite;
import com.secure.utils.LogUtils;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class AdClassProvider extends BaseContentProvider {
      
    private static final String URL_STR  = "content://com.provider.AdClassProvider";
    public static final Uri CONTENT_URI = Uri.parse(URL_STR); 

	@Override
	public String getTableName() {
		return AuroraSecureSqlite.TABLE_NAME_OF_adClassInfo;
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
				adProviderData == null){
        	return ;
        }
		MyArrayList<AdClassData> adClassList = adProviderData.getAdClassList();
		int size = adClassList==null?0:adClassList.size();
		for(int i=0;i<size;i++){
			AdClassData adClassData = adClassList.get(i);
			if(adClassData == null){
				continue ;
			}
			ContentValues values = new ContentValues();
			values.put(AuroraSecureSqlite.AD_PROVIDER_NAME, adProviderData.getProviderName());		
			values.put(AuroraSecureSqlite.CLASS_NAME, adClassData.getName());
			values.put(AuroraSecureSqlite.DESC, adClassData.getDesc());
			
			if(isHave(context, 
					getQueryWhere(),
					getQueryValue(adProviderData.getProviderName(),adClassData.getName()),
					CONTENT_URI)){
				context.getContentResolver().update(
						CONTENT_URI, 
						values,getQueryWhere(),
						getQueryValue(adProviderData.getProviderName(),adClassData.getName()));
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
				adProviderData == null){
        	return ;
        }
		MyArrayList<AdClassData> adClassList = adProviderData.getAdClassList();
		int size = adClassList==null?0:adClassList.size();
		for(int i=0;i<size;i++){
			AdClassData adClassData = adClassList.get(i);
			if(adClassData == null){
				continue ;
			}
			ContentValues values = new ContentValues();
			values.put(AuroraSecureSqlite.AD_PROVIDER_NAME, adProviderData.getProviderName());		
			values.put(AuroraSecureSqlite.CLASS_NAME, adClassData.getName());
			values.put(AuroraSecureSqlite.DESC, adClassData.getDesc());			
			context.getContentResolver().insert(CONTENT_URI,values);
		}		
	}
	
	/**
	 * 删除某个广告插件对应的所有的广告类
	 * @param context
	 * @param adProviderData
	 */
	public static void deleteDate(Context context,
			AdProviderData adProviderData){
		if( adProviderData == null){
        	return ;
        }
		deleteDate(context,adProviderData.getProviderName());
	}
	
	/**
	 * 删除某个广告插件对应的所有的广告类
	 * @param context
	 * @param providerName
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
	 * 获取指定广告商包含的广告类,并将查找的广告类存在adProviderData中
	 * @param context
	 * @param adProviderData
	 */
	public static void queryAdClassList(Context context,AdProviderData adProviderData){
		if(context == null || 
			adProviderData == null ||
			adProviderData.getProviderName() == null){
        	return ;
        }	
		String[] columns={
    			AuroraSecureSqlite.CLASS_NAME,
    			AuroraSecureSqlite.DESC}; //需要返回的列名			
		
		String where = AuroraSecureSqlite.AD_PROVIDER_NAME+" = ?";		
		String[] whereValue = {adProviderData.getProviderName()};
		
		Cursor cursor = null;
		try{
			cursor =context.getContentResolver().query(CONTENT_URI,columns,where,whereValue,null);
		}catch(Exception e){
			LogUtils.printWithLogCat(AdClassProvider.class.getName(), e.toString());
		} 

		synchronized(CONTENT_URI){				   	
	    	if (cursor != null){			
    			while (cursor.moveToNext()) { 				
    				AdClassData adClassData = new AdClassData();
    				adClassData.setName(cursor.getString(
        					cursor.getColumnIndexOrThrow(AuroraSecureSqlite.CLASS_NAME)));
    				adClassData.setDesc(cursor.getString(
    		    		cursor.getColumnIndexOrThrow(AuroraSecureSqlite.DESC)));   								
    				adProviderData.addAdClassData(adClassData);
    		    }
    			cursor.close();	      			     			    
	    	}
		}
	} 
	
    private static String getQueryWhere(){
    	return AuroraSecureSqlite.AD_PROVIDER_NAME+" = ?"+ " AND "+AuroraSecureSqlite.CLASS_NAME+" = ?";
    }
    
    private static String[] getQueryValue(String providerName,String className){
    	String[] whereValue = {providerName,className};
    	return whereValue;
    } 
}
