package com.aurora.puremanager.provider;

import com.aurora.puremanager.sqlite.AuroraSecureSqlite;
import com.aurora.puremanager.utils.StringUtils;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public abstract class BaseContentProvider extends ContentProvider {

    private AuroraSecureSqlite dbHelper;
    
    /**
     * 获取当前操作的表名
     * @return
     */
    public abstract String getTableName();
    
    /**
     * 获取当前ContentProvider的uri
     * @return
     */
    public abstract Uri getContentUri();
  
    @Override
    public boolean onCreate() {
        dbHelper = new AuroraSecureSqlite(getContext());
        return (dbHelper == null) ? false : true;
    }
    
    @Override
    public Uri insert(Uri uri, ContentValues contentvalues) { 
    	synchronized (getContentUri()) {
    		if(uri == null || contentvalues == null || dbHelper == null){
        		return null;
        	}
        	
        	Uri rowUri = null;
    		SQLiteDatabase db = dbHelper.getWritableDatabase();
    		if(db.isOpen()){
    	        long rowId = db.insert(getTableName(), null, contentvalues);
    	        if (rowId > 0) {
    	            rowUri = ContentUris.appendId(getContentUri().buildUpon(), rowId).build();
    	        }
    			db.close(); 			
    		}
    		return rowUri; 
        }   	
    }
    
    @Override
    public int delete(Uri uri, String s, String[] as) {
        synchronized (getContentUri()) {
        	if(dbHelper == null || as == null){
        		return 0;
        	}
    		SQLiteDatabase db = dbHelper.getWritableDatabase();
    		if(db.isOpen()){
    			db.delete(getTableName(),s,as);
    			db.close();
    		}	
    		return 0;
        }  	
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }
    
    @Override
   	public Cursor query(Uri uri, String[] projection, String selection,
   			String[] selectionArgs, String sortOrder) {
       synchronized (getContentUri()) {
        	SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            qb.setTables(getTableName());
            return qb.query(db, projection, selection, selectionArgs, null, null, sortOrder); 
        }     
   	}
    
    @Override
    public int update(Uri uri, ContentValues contentvalues, String s, String[] as) {
        synchronized (getContentUri()) {
        	int rows = 0;
        	
        	if(contentvalues == null || dbHelper == null){
        		return rows;
        	}   	 	
        	SQLiteDatabase db = dbHelper.getWritableDatabase();
        	if(db.isOpen()){
        		rows = db.update(getTableName(), contentvalues, s, as);
        		db.close();
        	}
    		return rows;
       }   	
    } 
    
    /**
     * 判断是不是存在指定元素
     * @param context
     * @param selection
     * @param selectionArgs
     * @return  true:ContentProvider中已经存在；
     *          false：ContentProvider中不存在；
     */
    public static boolean isHave(Context context,String selection,
   			String[] selectionArgs,Uri content_uri){
        boolean flag = false;
        if(context == null || StringUtils.isEmpty(selection) || selectionArgs == null){
        	return flag;
        }
    	
        Cursor cursor = null;
        try{
        	cursor =context.getContentResolver().query(
        			content_uri , 
        			null, 
        			selection,
        			selectionArgs, 
        			null);
        }catch(Exception e){
        	e.printStackTrace();
        }
		
    	if (cursor != null){
    		if(cursor.moveToFirst()){
    			flag = true;
    		}   		
            cursor.close();
    	}  	
    	return flag;
    }
}
