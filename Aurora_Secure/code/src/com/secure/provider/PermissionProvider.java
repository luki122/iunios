package com.secure.provider;

import java.util.ArrayList;

import com.lbe.security.service.sdkhelper.SDKConstants;
import com.secure.data.AppInfo;
import com.secure.data.PermissionInfo;
import com.secure.model.LBEmodel;
import com.secure.sqlite.AuroraSecureSqlite;
import com.secure.utils.ApkUtils;
import com.secure.utils.mConfig;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class PermissionProvider extends BaseContentProvider {
      
    private static final String URL_STR  = "content://com.provider.PermissionProvider";
    public static final Uri CONTENT_URI = Uri.parse(URL_STR);   
    private static final String TAG = "PermissionProvider";

	@Override
	public String getTableName() {
		return AuroraSecureSqlite.TABLE_NAME_OF_permission;
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
    		AppInfo appInfo){
		if(context == null || 
				appInfo == null){
        	return ;
        }
		
		if(appInfo.getPermission() != null){   		
			for(int i=0;i<appInfo.getPermission().size();i++){
				PermissionInfo tmp = appInfo.getPermission().get(i);
				if(tmp == null ||  
						tmp.permId<=0
						){
					continue;
				}
				ContentValues values = new ContentValues();
				values.put(AuroraSecureSqlite.PACKAGE_NAME, appInfo.getPackageName());		
				values.put(AuroraSecureSqlite.PERMISSION_ID, tmp.permId);
				values.put(AuroraSecureSqlite.PERMISSION_STATE, tmp.getCurState());						
				if(isHave(context, 
						getQueryWhere(),
						getQueryValue(appInfo.getPackageName(),tmp.permId),
						CONTENT_URI)){
					context.getContentResolver().update(
							CONTENT_URI, 
							values,getQueryWhere(),
							getQueryValue(appInfo.getPackageName(),tmp.permId));
				}else{
					context.getContentResolver().insert(CONTENT_URI,values);
				}					
			}					
		}		
	}
	
	/**
	 * 更新权限状态
	 * @param context
	 * @param appInfo
	 */
	public static void UpdatePermState(Context context,
    		String pkgName,PermissionInfo permInfo){
		if(context == null || 
				pkgName == null || 
				permInfo == null || 
						permInfo.permId<=0){
        	return ;
        }
		
		ContentValues values = new ContentValues();
		values.put(AuroraSecureSqlite.PERMISSION_STATE, permInfo.getCurState());
			
		context.getContentResolver().update(
				CONTENT_URI, 
				values,getQueryWhere(),
				getQueryValue(pkgName,permInfo.permId)
				);
	}
	
	/**
	 * 删除该应用对应的所有的权限
	 * @param context
	 * @param appInfo
	 */
	public static void deleteDate(Context context,
    		AppInfo appInfo){
		if( appInfo == null){
        	return ;
        }
		deleteDate(context,appInfo.getPackageName());
	}
	
	/**
	 * 删除该应用对应的所有的权限
	 * @param context
	 * @param packageName
	 */
	public static void deleteDate(Context context,
    		String packageName){
		if(context == null || 
				packageName == null){
        	return ;
        }
		
		context.getContentResolver().delete(
				CONTENT_URI, 
				AuroraSecureSqlite.PACKAGE_NAME+" = ?",
				new String[]{packageName});
	}
	
	/**
	 * 查询指定应用的全部权限
	 * @param context
	 * @param appInfo
	 */
	public static void queryAppPerm(Context context,
    		AppInfo appInfo){
		if(context == null || 
				appInfo == null){
        	return ;
        }
		ArrayList<PermissionInfo> permList = new ArrayList<PermissionInfo>();
		
		String[] columns={
				AuroraSecureSqlite.PERMISSION_NAME,
    			AuroraSecureSqlite.PERMISSION_DESC,
    			AuroraSecureSqlite.PERMISSION_STATE,
    			AuroraSecureSqlite.PERMISSION_ID}; //需要返回的列名		
		String where = AuroraSecureSqlite.PACKAGE_NAME+" = ?";		
		String[] whereValue = {appInfo.getPackageName()};
		
		Cursor cursor = null;
		try{
			cursor =context.getContentResolver().query(CONTENT_URI,columns,where,whereValue,null);
		}catch(Exception e){
			e.printStackTrace();
		} 

		synchronized(CONTENT_URI){				   	
	    	if (cursor != null){			
    			while (cursor.moveToNext()) { 
    				PermissionInfo tmp = new PermissionInfo();				
    				tmp.permId = cursor.getInt(
    						cursor.getColumnIndexOrThrow(AuroraSecureSqlite.PERMISSION_ID));    
    				tmp.setCurState(cursor.getInt(cursor.getColumnIndexOrThrow(AuroraSecureSqlite.PERMISSION_STATE)));   	
    				permList.add(tmp);
    		    }
    			cursor.close();	      			     			    
	    	}
		}
		
    	if(permList.size() > 0){
			appInfo.setPermission(permList);
		}
    	
    	if(appInfo.getIsUserApp()){
        	if(ApkUtils.isNeedOptimize(context,appInfo)){
    			appInfo.setIsNeedOptimize(true);
    		}else{
    			appInfo.setIsNeedOptimize(false); 
    		}
    	}
	} 
	
	/**
	 * 根据包名查找发送彩信的权限
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static ArrayList<PermissionInfo> getMMSPermissonsByPackageName(Context context,String packageName){
		ArrayList<PermissionInfo> permList = new ArrayList<PermissionInfo>();
		
		String[] columns={
				AuroraSecureSqlite.PERMISSION_NAME,
    			AuroraSecureSqlite.PERMISSION_DESC,
    			AuroraSecureSqlite.PERMISSION_STATE,
    			AuroraSecureSqlite.PERMISSION_ID}; //需要返回的列名		
		String where = AuroraSecureSqlite.PACKAGE_NAME+" = ? and  "+AuroraSecureSqlite.PERMISSION_ID +" = "+SDKConstants.PERM_ID_SENDMMS;		
		String[] whereValue = {packageName};
		
		Cursor cursor = null;
		try{
			cursor =context.getContentResolver().query(CONTENT_URI,columns,where,whereValue,null);
		}catch(Exception e){
			e.printStackTrace();
		} 

		synchronized(CONTENT_URI){				   	
	    	if (cursor != null){			
    			while (cursor.moveToNext()) { 
    				PermissionInfo tmp = new PermissionInfo();				
    				tmp.permId = cursor.getInt(
    						cursor.getColumnIndexOrThrow(AuroraSecureSqlite.PERMISSION_ID));    
    				tmp.setCurState(cursor.getInt(
    						cursor.getColumnIndexOrThrow(AuroraSecureSqlite.PERMISSION_STATE)));   	
    				permList.add(tmp);
    		    }
    			cursor.close();	      			     			    
	    	}
		}
		
        return permList;
		
	}
	
	
	
	/**
     * 获取查找条件,注意这个ContentProvider的查找条件有两个，一个是packageName，一个是permissionName
     */
    private static String getQueryWhere(){
    	return AuroraSecureSqlite.PACKAGE_NAME+" = ?"+ " AND "+AuroraSecureSqlite.PERMISSION_ID+" = ?";
    }
    
    /**
     * 获取查找条件的值,注意这个ContentProvider的查找条件有两个，一个是packageName，一个是permissionName
     */
    private static String[] getQueryValue(String packageName,int permId){
    	String[] whereValue = {packageName,""+permId};
    	return whereValue;
    } 
}
