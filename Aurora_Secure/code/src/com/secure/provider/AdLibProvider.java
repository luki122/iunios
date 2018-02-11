package com.secure.provider;

import java.util.ArrayList;
import com.adblock.data.AdProviderData;
import com.secure.sqlite.AuroraSecureSqlite;
import com.secure.utils.LogUtils;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class AdLibProvider extends BaseContentProvider {
      
    private static final String URL_STR  = "content://com.provider.AdLibProvider";
    public static final Uri CONTENT_URI = Uri.parse(URL_STR); 

	@Override
	public String getTableName() {
		return AuroraSecureSqlite.TABLE_NAME_OF_adProviderInfo;
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
		ContentValues values = new ContentValues();
		values.put(AuroraSecureSqlite.AD_PROVIDER_NAME, adProviderData.getProviderName());
		values.put(AuroraSecureSqlite.AD_CLASS_COMM_NAME, adProviderData.getAdClassCommName());
		values.put(AuroraSecureSqlite.AD_URL, adProviderData.getUrl());	
		values.put(AuroraSecureSqlite.IS_HAVE_NOTIFY_AD, adProviderData.getIsHaveNotifyAd());	
		values.put(AuroraSecureSqlite.IS_HAVE_VIEW_AD, adProviderData.getIsHaveViewAd());	
		if(isHave(context, 
				getQueryWhere(),
				getQueryValue(adProviderData.getProviderName()),
				CONTENT_URI)){
			context.getContentResolver().update(
					CONTENT_URI, 
					values,
					getQueryWhere(),
					getQueryValue(adProviderData.getProviderName()));
		}else{
			context.getContentResolver().insert(CONTENT_URI,values);
		}
		AdClassProvider.insertOrUpdateDate(context, adProviderData);
		AdPermProvider.insertOrUpdateDate(context, adProviderData);
	}
	
	/**
	 * 插入数据(如果数据库中已经存在该数据，则先删掉以前的，在插入)
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
		ContentValues values = new ContentValues();
		values.put(AuroraSecureSqlite.AD_PROVIDER_NAME, adProviderData.getProviderName());
		values.put(AuroraSecureSqlite.AD_CLASS_COMM_NAME, adProviderData.getAdClassCommName());
		values.put(AuroraSecureSqlite.AD_URL, adProviderData.getUrl());	
		values.put(AuroraSecureSqlite.IS_HAVE_NOTIFY_AD, adProviderData.getIsHaveNotifyAd());	
		values.put(AuroraSecureSqlite.IS_HAVE_VIEW_AD, adProviderData.getIsHaveViewAd());	
		if(isHave(context, 
				getQueryWhere(),
				getQueryValue(adProviderData.getProviderName()),
				CONTENT_URI)){
			deleteDate(context,adProviderData.getProviderName());
		}
		context.getContentResolver().insert(CONTENT_URI,values);
		AdClassProvider.insertDate(context, adProviderData);
		AdPermProvider.insertDate(context, adProviderData);
	}

	/**
	 * 删除对应的广告提供商
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
	 * 删除对应的广告提供商
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
				getQueryWhere(),
				getQueryValue(providerName));
		
		AdClassProvider.deleteDate(context, providerName);
		AdPermProvider.deleteDate(context, providerName);
	}
	
	/**
	 * 获取数据库中存放的所有广告提供商的信息
	 * @param context
	 * @return 返回有不可能为null
	 */
	public static ArrayList<AdProviderData> queryAllAdProvider(Context context){
		ArrayList<AdProviderData> adLibList = new ArrayList<AdProviderData>();
		if(context == null){
			return adLibList;
		}	
		String[] columns={AuroraSecureSqlite.AD_PROVIDER_NAME,
				AuroraSecureSqlite.AD_CLASS_COMM_NAME,
    			AuroraSecureSqlite.AD_URL,
    			AuroraSecureSqlite.IS_HAVE_NOTIFY_AD,
    			AuroraSecureSqlite.IS_HAVE_VIEW_AD}; 	
		Cursor cursor = null;
		try{
			cursor =context.getContentResolver().query(CONTENT_URI,columns,null,null,null);	
		}catch(Exception e){
			LogUtils.printWithLogCat(AdLibProvider.class.getName(), e.toString());
		}	

		synchronized(CONTENT_URI){			
	    	if (cursor != null){
    			while (cursor.moveToNext()) {      				
    				AdProviderData adProviderData = new AdProviderData();
					adProviderData.setProviderName(cursor.getString(
    		    			cursor.getColumnIndexOrThrow(AuroraSecureSqlite.AD_PROVIDER_NAME)));
					adProviderData.setAdClassCommName(cursor.getString(
			    		cursor.getColumnIndexOrThrow(AuroraSecureSqlite.AD_CLASS_COMM_NAME)));
					adProviderData.setUrl(cursor.getString(
			    		cursor.getColumnIndexOrThrow(AuroraSecureSqlite.AD_URL))); 
					
					if(cursor.getInt(cursor.getColumnIndex(AuroraSecureSqlite.IS_HAVE_NOTIFY_AD)) == 0){
						adProviderData.setIsHaveNotifyAd(false);
    		    	}else{
    		    		adProviderData.setIsHaveNotifyAd(true);
    		    	}
					
					if(cursor.getInt(cursor.getColumnIndex(AuroraSecureSqlite.IS_HAVE_VIEW_AD)) == 0){
						adProviderData.setIsHaveViewAd(false);
    		    	}else{
    		    		adProviderData.setIsHaveViewAd(true);
    		    	}
					
					AdClassProvider.queryAdClassList(context, adProviderData);
					AdPermProvider.queryAdPermList(context, adProviderData);					
					adLibList.add(adProviderData);					
    		    }
    			cursor.close();      			     			    
	    	}	
		}	
		return adLibList;
	}

	/**
	 * @param context
	 * @param providerName
	 * @return 返回的值可能为null
	 */
	public static AdProviderData getAdProviderData(Context context,
			String providerName){
		AdProviderData adProviderData = null;
		if(context == null || 
				providerName == null){
        	return adProviderData;
        }	
		String[] columns={
				AuroraSecureSqlite.AD_CLASS_COMM_NAME,
    			AuroraSecureSqlite.AD_URL,
    			AuroraSecureSqlite.IS_HAVE_NOTIFY_AD,
    			AuroraSecureSqlite.IS_HAVE_VIEW_AD}; //需要返回的列名			
		
		String where = AuroraSecureSqlite.AD_PROVIDER_NAME+" = ?";		
		String[] whereValue = {providerName};
		
		Cursor cursor = null;
		try{
			cursor =context.getContentResolver().query(CONTENT_URI,columns,where,whereValue,null);
		}catch(Exception e){
			LogUtils.printWithLogCat(AdLibProvider.class.getName(), e.toString());
		} 

		synchronized(CONTENT_URI){				   	
	    	if (cursor != null){				    		
	    		if(cursor.moveToFirst()){
		    		adProviderData = new AdProviderData();
					adProviderData.setProviderName(providerName);
					adProviderData.setAdClassCommName(cursor.getString(
			    		cursor.getColumnIndexOrThrow(AuroraSecureSqlite.AD_CLASS_COMM_NAME)));
					adProviderData.setUrl(cursor.getString(
			    		cursor.getColumnIndexOrThrow(AuroraSecureSqlite.AD_URL))); 
					
					if(cursor.getInt(cursor.getColumnIndex(AuroraSecureSqlite.IS_HAVE_NOTIFY_AD)) == 0){
						adProviderData.setIsHaveNotifyAd(false);
    		    	}else{
    		    		adProviderData.setIsHaveNotifyAd(true);
    		    	}
					
					if(cursor.getInt(cursor.getColumnIndex(AuroraSecureSqlite.IS_HAVE_VIEW_AD)) == 0){
						adProviderData.setIsHaveViewAd(false);
    		    	}else{
    		    		adProviderData.setIsHaveViewAd(true);
    		    	}
					
					AdClassProvider.queryAdClassList(context, adProviderData);
					AdPermProvider.queryAdPermList(context, adProviderData);
	    		}				
    			cursor.close();	      			     			    
	    	}
		}
    	return adProviderData;
	} 
	
    private static String getQueryWhere(){
    	return AuroraSecureSqlite.AD_PROVIDER_NAME+" = ?";
    }
    
    private static String[] getQueryValue(String providerName){
    	String[] whereValue = {providerName};
    	return whereValue;
    } 
}
