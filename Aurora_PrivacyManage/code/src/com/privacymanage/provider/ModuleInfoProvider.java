package com.privacymanage.provider;

import com.privacymanage.data.ModuleInfoData;
import com.privacymanage.sqlite.AuroraPrivacySqlite;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class ModuleInfoProvider extends BaseContentProvider {      
    private static final String URL_STR  = "content://com.privacymanage.provider.ModuleInfoProvider";
    public static final Uri CONTENT_URI = Uri.parse(URL_STR);  

	@Override
	public String getTableName() {
		return AuroraPrivacySqlite.TABLE_NAME_OF_moduleInfo;
	}

	@Override
	public Uri getContentUri() {
		return CONTENT_URI;
	}
	
	/**
	 * 
	 * @param context
	 * @param moduleInfoData
	 * @param accountId
	 */
	public static void insertOrUpdateDate(Context context,
			ModuleInfoData moduleInfoData,long accountId){
		if(context == null || 
				moduleInfoData == null){
        	return ;
        }
		
		ContentValues values = new ContentValues();
		values.put(AuroraPrivacySqlite.ACCOUNT_ID, accountId);
		values.put(AuroraPrivacySqlite.PACKAGE_NAME, moduleInfoData.getPkgName());
		values.put(AuroraPrivacySqlite.CLASS_NAME, moduleInfoData.getClassName());
		values.put(AuroraPrivacySqlite.ITEM_NUM, moduleInfoData.getItemNum());
		
		if(isHave(context, 
				getQueryWhere(),
				getQueryValue(accountId,
						moduleInfoData.getPkgName(),
						moduleInfoData.getClassName()),
				CONTENT_URI)){
			context.getContentResolver().update(
					CONTENT_URI, 
					values,
					getQueryWhere(),
					getQueryValue(accountId,
							moduleInfoData.getPkgName(),
							moduleInfoData.getClassName()));
		}else{
			context.getContentResolver().insert(CONTENT_URI,values);
		}
	}
	
	/**
	 * @param context
	 * @param accountId
	 */
	public static void deleteDate(Context context,long accountId){
		if(context == null){
        	return ;
        }
		
		context.getContentResolver().delete(
				CONTENT_URI, 
				getQueryWhereOfAccountId(),
				getQueryValueOfAccountId(accountId));	
	}
	
	/**
	 * 
	 * @param context
	 * @param moduleInfoData
	 * @param accountId
	 */
	public static void deleteDate(Context context,
			ModuleInfoData moduleInfoData,long accountId){
		if(moduleInfoData == null){
        	return ;
        }		
		deleteDate(context,accountId,
				moduleInfoData.getPkgName(),
				moduleInfoData.getClassName());
	}
	
	public static void deleteDate(Context context,
    		long accountId,String pkgName,String className){
		if(context == null){
        	return ;
        }
		
		context.getContentResolver().delete(
				CONTENT_URI, 
				getQueryWhere(),
				getQueryValue(accountId,pkgName,className));	
	}
	
	public static void delete(Context context,String pkgName,String className){
		if(context == null){
        	return ;
        }
		
		context.getContentResolver().delete(
				CONTENT_URI, 
				getQueryWhereOfPkgClass(),
				getQueryValueOfPkgClass(pkgName,className));
	}
	
	/**
	 * 查找指定账户，指定模块的隐私条目
	 * @param context
	 * @param accountId
	 * @param pkgName
	 * @param className
	 * @return
	 */
	public static int getPrivacyItemNum(Context context,
			long accountId,String pkgName,String className){	
		int privacyItemNum = 0;
		if(context == null){
        	return privacyItemNum;
        }
		String[] columns={AuroraPrivacySqlite.ITEM_NUM}; //需要返回的列名
		
		Cursor cursor = null;
		try{
			cursor =context.getContentResolver().query(CONTENT_URI,
					columns,
					getQueryWhere(),
					getQueryValue(accountId,pkgName,className),
					null);
		}catch(Exception e){
			//nothing
		}		 
		
		synchronized(CONTENT_URI){			
	    	if (cursor != null){
	    		if(cursor.moveToFirst()){
	    			privacyItemNum = cursor.getInt(
	    					cursor.getColumnIndex(AuroraPrivacySqlite.ITEM_NUM));
	    		}   
	    		cursor.close();  
	    	}	
		}
    	return privacyItemNum;
	}
    	
    private static String getQueryWhere(){
    	return AuroraPrivacySqlite.ACCOUNT_ID+" = ?"+ 
          " AND "+AuroraPrivacySqlite.PACKAGE_NAME+" = ?"+ 
          " AND "+AuroraPrivacySqlite.CLASS_NAME+" = ?";  	
    }
    
    private static String[] getQueryValue(long accountId,String pkgName,String className){
    	String[] whereValue = {""+accountId,pkgName,className};
    	return whereValue;
    }
    
	/**
	 * 查找条件为：ACCOUNT_ID
	 */
    private static String getQueryWhereOfAccountId(){
    	return AuroraPrivacySqlite.ACCOUNT_ID+" = ?";
    }
    
    /**
     * 查找条件为：ACCOUNT_ID
     * @param accountId
     * @return
     */
    private static String[] getQueryValueOfAccountId(long accountId){
    	String[] whereValue = {""+accountId};
    	return whereValue;
    }
    
    /**
     * 子模块的包名与完整类名作为查询条件
     * @return
     */
    private static String getQueryWhereOfPkgClass(){
    	return AuroraPrivacySqlite.PACKAGE_NAME+" = ?"+ 
          " AND "+AuroraPrivacySqlite.CLASS_NAME+" = ?";  	
    }
    
    /**
     * 子模块的包名与完整类名作为查询条件
     * @param pkgName
     * @param className
     * @return
     */
    private static String[] getQueryValueOfPkgClass(String pkgName,String className){
    	String[] whereValue = {pkgName,className};
    	return whereValue;
    }
}