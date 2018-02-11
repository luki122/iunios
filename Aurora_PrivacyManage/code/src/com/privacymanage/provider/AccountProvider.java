package com.privacymanage.provider;

import java.util.ArrayList;
import java.util.List;
import com.privacymanage.data.AccountData;
import com.privacymanage.sqlite.AuroraPrivacySqlite;
import com.privacymanage.utils.DES;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class AccountProvider extends BaseContentProvider {      
    private static final String URL_STR  = "content://com.privacymanage.provider.AccountProvider";
    public static final Uri CONTENT_URI = Uri.parse(URL_STR);  

	@Override
	public String getTableName() {
		return AuroraPrivacySqlite.TABLE_NAME_OF_account;
	}

	@Override
	public Uri getContentUri() {
		return CONTENT_URI;
	}
	
	public static void insertOrUpdateDate(Context context,
			AccountData accountData){
		if(context == null || 
				accountData == null){
        	return ;
        }
		
		ContentValues values = new ContentValues();
		values.put(AuroraPrivacySqlite.ACCOUNT_ID, accountData.getAccountId());
		values.put(AuroraPrivacySqlite.PASSWORD,DES.encryptDES(accountData.getPassword()));
	
		values.put(AuroraPrivacySqlite.E_MAIL, accountData.getEMail());
		values.put(AuroraPrivacySqlite.HOME_PATH, accountData.getHomePath());
		values.put(AuroraPrivacySqlite.CREATE_TIME, accountData.getCreateTime());
		values.put(AuroraPrivacySqlite.ACCOUNT_STATE, accountData.getState());
		
		if(isHave(context, 
				getQueryWhereOfAccountId(),
				getQueryValueOfAccountId(accountData.getAccountId()),
				CONTENT_URI)){
			context.getContentResolver().update(
					CONTENT_URI, 
					values,
					getQueryWhereOfAccountId(),
					getQueryValueOfAccountId(accountData.getAccountId()));
		}else{
			context.getContentResolver().insert(CONTENT_URI,values);
		}
	}
	
	public static void deleteDate(Context context,
			AccountData accountData){
		if(accountData == null){
        	return ;
        }		
		deleteDate(context,accountData.getAccountId());
	}
	
	public static void deleteDate(Context context,
    		long accountId){
		if(context == null){
        	return ;
        }
		
		context.getContentResolver().delete(
				CONTENT_URI, 
				getQueryWhereOfAccountId(),
				getQueryValueOfAccountId(accountId));	
	}
	
	/**
	 * 根据密码查找对用的账户信息
	 * @param outAccountData 查询后的结果存放在该对象中
	 * @param context
	 * @param password
	 * @return true：查询成功；false：查询失败
	 */
	public static AccountData getAccountInfoByPassword(Context context,String password){	
		AccountData accountData = null;
		if(context == null || password == null){
        	return accountData;
        }
		String[] columns={
				AuroraPrivacySqlite.ACCOUNT_ID,
				AuroraPrivacySqlite.E_MAIL,
				AuroraPrivacySqlite.HOME_PATH,
				AuroraPrivacySqlite.CREATE_TIME,
				AuroraPrivacySqlite.ACCOUNT_STATE}; //需要返回的列名
		
		Cursor cursor = null;
		try{
			cursor =context.getContentResolver().query(CONTENT_URI,
					columns,
					getQueryWhereOfPassword(),
					getQueryValueOfPassword(DES.encryptDES(password)),
					null);
		}catch(Exception e){
			//nothing
		}		 
		
		synchronized(CONTENT_URI){			
	    	if (cursor != null){
	    		if(cursor.moveToFirst()){
	    			accountData = new AccountData(); 
	    			accountData.setAccountId(cursor.getLong(
	    					cursor.getColumnIndexOrThrow(AuroraPrivacySqlite.ACCOUNT_ID)));
	    			accountData.setEMail(cursor.getString(
			    			cursor.getColumnIndexOrThrow(AuroraPrivacySqlite.E_MAIL)));
	    			accountData.setHomePath(cursor.getString(
			    			cursor.getColumnIndexOrThrow(AuroraPrivacySqlite.HOME_PATH)));
	    			accountData.setPassword(password);
	    			accountData.setCreateTime(cursor.getLong(
	    					cursor.getColumnIndex(AuroraPrivacySqlite.CREATE_TIME)));
	    			accountData.setState(cursor.getInt(
	    					cursor.getColumnIndex(AuroraPrivacySqlite.ACCOUNT_STATE)));
	    		}   
	    		cursor.close();  
	    	}	
		}
    	return accountData;
	}
	
	/**
	 * 从数据库中读取所有应用信息
	 * @param context
	 * @param packname
	 * @return 返回可能为null
	 */
	public static long[] getAllAccountId(Context context){
		long[] accoundIds = null; 
		
		if(context == null){
        	return accoundIds;
        }
		
		String[] columns={AuroraPrivacySqlite.ACCOUNT_ID}; //需要返回的列名
		
		Cursor cursor = null;
		try{
			cursor =context.getContentResolver().query(CONTENT_URI,columns,null,null,null);	
		}catch(Exception e){
			//nothing
		}
		
		synchronized(CONTENT_URI){			
	    	if (cursor != null){
	    		int length = cursor.getColumnCount();
	    		if(length > 0){
	    			accoundIds = new long[length];
		    		int index = 0;
	    			while (cursor.moveToNext()) { 
	    				if(index<length){
	        				accoundIds[index] = cursor.getLong(
	    	    					cursor.getColumnIndexOrThrow(AuroraPrivacySqlite.ACCOUNT_ID));
	    				}
	    				index++;
	    		    }
	    		}	    		
    			cursor.close();      			     			    
	    	}	
		}
    	return accoundIds;
	} 
	
	/**
	 * 查找指定账户的密码
	 * @param context
	 * @param email
	 * @return 返回值可能为null
	 */
	public static String getAppointAccountPassword(Context context,long accountId){		
		if(context == null){
        	return null;
        }
		
		String[] columns={AuroraPrivacySqlite.PASSWORD}; //需要返回的列名
		
		Cursor cursor = null;
		try{
			cursor =context.getContentResolver().query(CONTENT_URI,
					columns,
					getQueryWhereOfAccountId(),
                    getQueryValueOfAccountId(accountId),
                    null);	
		}catch(Exception e){
			//nothing
		}
		String password = null;
		synchronized(CONTENT_URI){			
	    	if (cursor != null){
	    		if(cursor.moveToFirst()){
    				password = cursor.getString(
    		    			cursor.getColumnIndexOrThrow(AuroraPrivacySqlite.PASSWORD));
    				password = DES.decryptDES(password);
    		    }
    			cursor.close();      			     			    
	    	}	
		}
    	return password;
	} 
	
	/**
	 * 查找指定账户的密保邮箱
	 * @param context
	 * @param email
	 * @return 返回值不可能为null
	 */
	public static String getAppointAccountEmail(Context context,long accountId){
		String email = "";
		if(context == null){
        	return email;
        }
		
		String[] columns={AuroraPrivacySqlite.E_MAIL}; //需要返回的列名
		
		Cursor cursor = null;
		try{
			cursor =context.getContentResolver().query(CONTENT_URI,
					columns,
					getQueryWhereOfAccountId(),
                    getQueryValueOfAccountId(accountId),
                    null);	
		}catch(Exception e){
			//nothing
		}
		
		synchronized(CONTENT_URI){			
	    	if (cursor != null){
	    		if(cursor.moveToFirst()){
	    			email = cursor.getString(
    		    			cursor.getColumnIndexOrThrow(AuroraPrivacySqlite.E_MAIL));
    		    }
    			cursor.close();      			     			    
	    	}	
		}
    	return email;
	} 
	
	/**
	 * 查找拥有此邮箱的所有密码
	 * @param context
	 * @param email
	 * @return 返回值不可能为null
	 */
	public static List<String> queryPasswordByEmail(Context context,String email){
		List<String> passwordList = new ArrayList<String>();
		
		if(context == null || email == null){
        	return passwordList;
        }
		
		String[] columns={AuroraPrivacySqlite.PASSWORD}; //需要返回的列名
		
		Cursor cursor = null;
		try{
			cursor =context.getContentResolver().query(CONTENT_URI,
					columns,
					getQueryWhereOfEmail(),
                    getQueryValueOfEmail(email),
                    null);	
		}catch(Exception e){
			//nothing
		}
		
		synchronized(CONTENT_URI){			
	    	if (cursor != null){
    			while (cursor.moveToNext()) { 
    				String password = cursor.getString(
    		    			cursor.getColumnIndexOrThrow(AuroraPrivacySqlite.PASSWORD));
    		    	passwordList.add(DES.decryptDES(password));
    		    }
    			cursor.close();      			     			    
	    	}	
		}
    	return passwordList;
	} 
	
	/**
     * 判断数据库中是不是已经使用过该密码
     * @param password
     * @return
     */
    public static synchronized boolean isHadEmail(Context context,String email){
    	if(context == null || email == null){
    		return false;
    	}
    	return isHave(context, 
    			getQueryWhereOfEmail(),
    			getQueryValueOfEmail(email),
				CONTENT_URI);
    }
    
    /**
     * 更新指定账户的邮箱地址
     * @param context
     * @param accountData
     * @param newEmail
     * @return
     */
    public static synchronized boolean updateEmail(Context context,
    		AccountData accountData,
    		String newEmail){
    	boolean resutl = false;
    	if(context == null || accountData == null){
    		return false;
    	}
    	
		ContentValues values = new ContentValues();
		values.put(AuroraPrivacySqlite.E_MAIL, newEmail);
		
		if(isHave(context, 
				getQueryWhereOfAccountId(),
				getQueryValueOfAccountId(accountData.getAccountId()),
				CONTENT_URI)){
			context.getContentResolver().update(
					CONTENT_URI, 
					values,
					getQueryWhereOfAccountId(),
					getQueryValueOfAccountId(accountData.getAccountId()));
			accountData.setEMail(newEmail);
			resutl = true;
		} 	
    	return resutl;
    }
    
    /**
     * 判断数据库中是不是已经使用过该密码
     * @param password
     * @return
     */
    public static synchronized boolean isHadPassword(Context context,String password){
    	if(context == null || password == null){
    		return false;
    	}
    	return isHave(context, 
    			getQueryWhereOfPassword(),
    			getQueryValueOfPassword(DES.encryptDES(password)),
				CONTENT_URI);
    }
    
    /**
     * 更新指定账户的密码
     * @param context
     * @param accountData
     * @param newEmail
     * @return
     */
    public static synchronized boolean updatePassword(Context context,
    		AccountData accountData,
    		String newPassword){
    	boolean resutl = false;
    	if(context == null || accountData == null || newPassword == null){
    		return false;
    	}
    	
		ContentValues values = new ContentValues();
		values.put(AuroraPrivacySqlite.PASSWORD, DES.encryptDES(newPassword));
		
		if(isHave(context, 
				getQueryWhereOfAccountId(),
				getQueryValueOfAccountId(accountData.getAccountId()),
				CONTENT_URI)){
			context.getContentResolver().update(
					CONTENT_URI, 
					values,
					getQueryWhereOfAccountId(),
					getQueryValueOfAccountId(accountData.getAccountId()));
			accountData.setPassword(newPassword);
			resutl = true;
		} 	
    	return resutl;
    }
    
    /**
     * 判断数据库中是否存在指定账户
     * @param password
     * @return
     */
    public static synchronized boolean isHadAccountId(Context context,long accountId){
    	if(context == null){
    		return false;
    	}
    	return isHave(context, 
    			getQueryWhereOfAccountId(),
    			getQueryValueOfAccountId(accountId),
				CONTENT_URI);
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
     * 查找条件为：PASSWORD
     * @return
     */
    private static String getQueryWhereOfPassword(){
    	return AuroraPrivacySqlite.PASSWORD+" = ?";
    }
    
    /**
     * 查找条件为：PASSWORD
     * @param password
     * @return
     */
    private static String[] getQueryValueOfPassword(String password){
    	String[] whereValue = {password};
    	return whereValue;
    }
    
    
    /**
     * 查找条件为：E_MAIL
     * @return
     */
    private static String getQueryWhereOfEmail(){
    	return AuroraPrivacySqlite.E_MAIL+" = ?";
    }
    
    /**
     * 查找条件为：E_MAIL
     * @param password
     * @return
     */
    private static String[] getQueryValueOfEmail(String email){
    	String[] whereValue = {email};
    	return whereValue;
    }
    
}