package com.secure.provider.open;

import com.secure.data.MyArrayList;
import com.secure.data.PrivacyAppData;
import com.secure.sqlite.OpenDataSqlite;
import com.secure.utils.LogUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class PrivacyAppProvider extends BaseOpenContentProvider {      
    private static final String URL_STR  = "content://com.secure.provider.open.PrivacyAppProvider";
    public static final Uri CONTENT_URI = Uri.parse(URL_STR);  
    private static final String TAG = PrivacyAppProvider.class.getName();

	@Override
	public String getTableName() {
		return OpenDataSqlite.TABLE_NAME_OF_privacyApp;
	}

	@Override
	public Uri getContentUri() {
		return CONTENT_URI;
	}
	
	public static boolean insertOrUpdateDate(Context context,
			long accountId,String pkgName){
		if(context == null || 
				pkgName == null){
        	return false;
        }
		
		ContentValues values = new ContentValues();
		values.put(OpenDataSqlite.ACCOUNT_ID, accountId);
		values.put(OpenDataSqlite.PACKAGE_NAME, pkgName);
		
		if(isHave(context, 
				getExactQueryWhere(),
				getExactQueryValue(accountId,pkgName),
				CONTENT_URI)){
             return false;
		}else{
			context.getContentResolver().insert(CONTENT_URI,values);
			return true;
		}
	}
	
	public static void deleteDate(Context context,
			long accountId,String pkgName){
		if(context == null || 
				pkgName == null){
        	return ;
        }
		LogUtils.printWithLogCat(TAG,"delete pkg="+pkgName);
		context.getContentResolver().delete(
				CONTENT_URI, 
				getExactQueryWhere(),
				getExactQueryValue(accountId,pkgName));	
	}
	
	public static void deleteDate(Context context,String pkgName){
		if(context == null || 
				pkgName == null){
        	return ;
        }
		
		context.getContentResolver().delete(
				CONTENT_URI, 
				getQueryWhereOfPkgName(),
				getQueryValueOfPkgName(pkgName));	
	}
	
	/**
	 * 查找指定隐私身份下的隐私应用
	 * @param context
	 * @param accountId
	 * @return 返回不可能为null
	 */
	public static MyArrayList<PrivacyAppData> getPrivacyAppInfo(Context context,long accountId){	
		MyArrayList<PrivacyAppData> privacyAppList = new MyArrayList<PrivacyAppData>();
		if(context == null){
        	return privacyAppList;
        }
		String[] columns={
				OpenDataSqlite.PACKAGE_NAME}; //需要返回的列名
		
		Cursor cursor = null;
		try{
			cursor =context.getContentResolver().query(CONTENT_URI,
					columns,
					getQueryWhere(),
					getQueryValue(accountId),
					null);
		}catch(Exception e){
			//nothing
		}		 
		
		synchronized(CONTENT_URI){			
	    	if (cursor != null){
    			while (cursor.moveToNext()) {
    				String pkgName = cursor.getString(
			    			cursor.getColumnIndexOrThrow(OpenDataSqlite.PACKAGE_NAME));
	    			PrivacyAppData privacyAppData = new PrivacyAppData();
	    			privacyAppData.setAccountId(accountId);
	    			privacyAppData.setPkgName(pkgName);
	    			privacyAppList.add(privacyAppData);
    		    }
    			cursor.close();      			     			    
	    	}	
		}
    	return privacyAppList;
	}
	
	/**
	 * 查找所有隐私应用
	 * @param context
	 * @param accountId
	 * @return 返回不可能为null
	 */
	public static MyArrayList<PrivacyAppData> getPrivacyAppInfo(Context context){	
		MyArrayList<PrivacyAppData> privacyAppList = new MyArrayList<PrivacyAppData>();
		if(context == null){
        	return privacyAppList;
        }
		String[] columns={
				OpenDataSqlite.ACCOUNT_ID,
				OpenDataSqlite.PACKAGE_NAME}; //需要返回的列名
		
		Cursor cursor = null;
		try{
			cursor =context.getContentResolver().query(CONTENT_URI,
					columns,
					null,
					null,
					null);
		}catch(Exception e){
			//nothing
		}		 
		
		synchronized(CONTENT_URI){			
	    	if (cursor != null){
    			while (cursor.moveToNext()) {
    				long accountId = cursor.getLong(
	    					cursor.getColumnIndexOrThrow(OpenDataSqlite.ACCOUNT_ID));
	    			String pkgName = cursor.getString(
			    			cursor.getColumnIndexOrThrow(OpenDataSqlite.PACKAGE_NAME));
	    			
	    			PrivacyAppData privacyAppData = new PrivacyAppData();
	    			privacyAppData.setAccountId(accountId);
	    			privacyAppData.setPkgName(pkgName);
	    			privacyAppList.add(privacyAppData);
    		    }
    			cursor.close();      			     			    
	    	}	
		}
		
    	return privacyAppList;
	}
    	
    private static String getQueryWhere(){
    	return OpenDataSqlite.ACCOUNT_ID+" = ?";
    }
    
    private static String[] getQueryValue(long accountId){
    	String[] whereValue = {""+accountId};
    	return whereValue;
    }
    
    private static String getExactQueryWhere(){
    	return OpenDataSqlite.ACCOUNT_ID+" = ?"+ 
    	          " AND "+OpenDataSqlite.PACKAGE_NAME+" = ?";
    }
    
    private static String[] getExactQueryValue(long accountId,String pkgName){
    	String[] whereValue = {""+accountId,pkgName};
    	return whereValue;
    } 
    
    private static String getQueryWhereOfPkgName(){
    	return OpenDataSqlite.PACKAGE_NAME+" = ?";
    }
    
    private static String[] getQueryValueOfPkgName(String pkgName){
    	String[] whereValue = {pkgName};
    	return whereValue;
    }   
}