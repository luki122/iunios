package com.privacymanage.provider;

import com.privacymanage.data.AccountConfigData;
import com.privacymanage.sqlite.AuroraPrivacySqlite;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class ConfigProvider extends BaseContentProvider {      
    private static final String URL_STR  = "content://com.privacymanage.provider.ConfigProvider";
    public static final Uri CONTENT_URI = Uri.parse(URL_STR);  

	@Override
	public String getTableName() {
		return AuroraPrivacySqlite.TABLE_NAME_OF_accountConfig;
	}

	@Override
	public Uri getContentUri() {
		return CONTENT_URI;
	}
	
	public static void insertOrUpdateDate(Context context,
			AccountConfigData accountConfigData,long accountId){
		if(context == null || 
				accountConfigData == null){
        	return ;
        }
		
		ContentValues values = new ContentValues();
		values.put(AuroraPrivacySqlite.ACCOUNT_ID, accountId);
		values.put(AuroraPrivacySqlite.MSG_NOTIFY_SWITCH, accountConfigData.getMsgNotifySwitch());
		values.put(AuroraPrivacySqlite.MSG_NOTIFY_HINT, accountConfigData.getMsgNotifyHintStr());
		
		if(isHave(context, 
				getQueryWhere(),
				getQueryValue(accountId),
				CONTENT_URI)){
			context.getContentResolver().update(
					CONTENT_URI, 
					values,
					getQueryWhere(),
					getQueryValue(accountId));
		}else{
			context.getContentResolver().insert(CONTENT_URI,values);
		}
	}
	
	public static void deleteDate(Context context,
    		long accountId){
		if(context == null){
        	return ;
        }
		
		context.getContentResolver().delete(
				CONTENT_URI, 
				getQueryWhere(),
				getQueryValue(accountId));	
	}
	
	public static AccountConfigData getAccountConfigInfo(Context context,long accountId){		
		AccountConfigData accountConfigData = null;
		if(context == null){
        	return accountConfigData;
        }
		String[] columns={
				AuroraPrivacySqlite.MSG_NOTIFY_SWITCH,
				AuroraPrivacySqlite.MSG_NOTIFY_HINT}; //需要返回的列名
		
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
	    		if(cursor.moveToFirst()){
	    			accountConfigData = new AccountConfigData(); 
	    			accountConfigData.setMsgNotifyHintStr(cursor.getString(
			    			cursor.getColumnIndexOrThrow(AuroraPrivacySqlite.MSG_NOTIFY_HINT)));
	    			if(cursor.getInt(
	    					cursor.getColumnIndex(AuroraPrivacySqlite.MSG_NOTIFY_SWITCH)) == 0){
	    				accountConfigData.setMsgNotifySwitch(false);
	    			}else{
	    				accountConfigData.setMsgNotifySwitch(true);
	    			}	    			
	    		}   
	    		cursor.close();  
	    	}	
		}
    	return accountConfigData;
	}
    	
    private static String getQueryWhere(){
    	return AuroraPrivacySqlite.ACCOUNT_ID+" = ?";
    }
    
    private static String[] getQueryValue(long accountId){
    	String[] whereValue = {""+accountId};
    	return whereValue;
    }
}