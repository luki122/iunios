package com.android.settings.lscreen;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public abstract class LSContentProvider extends ContentProvider {
	
	private LSDataSqlite dbHelper;
	
	/*
	 * 获取当前操作的表名
	 */
	public abstract String getTableName();
    /*
     * 获取当前ContentPrivider的Uri
     */
	public abstract Uri getContentUri();
	
	@Override
	public boolean onCreate() {
		dbHelper=new LSDataSqlite(getContext());
		return dbHelper==null ? false : true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) 
	{
	       synchronized (getContentUri()) 
	       {
	        	SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
	            SQLiteDatabase db = dbHelper.getReadableDatabase();
	            qb.setTables(getTableName());
	            return qb.query(db, projection, selection, selectionArgs, null, null, sortOrder); 
	        }    
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		synchronized (getContentUri()) 
		{
	        if(uri==null || values == null || dbHelper==null)
	        {
	        	return null;
	        }
	        
	        Uri rowUri=null;
	        SQLiteDatabase db= dbHelper.getWritableDatabase();
	        if(db.isOpen())
	        {
	        	long rowId=db.insert(getTableName(), null, values);
	        	if(rowId>0)
	        	{
	                rowUri=ContentUris.appendId(getContentUri().buildUpon(), rowId).build();
	        	}
	        	db.close();
	        }
			return rowUri;
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) 
	{
		synchronized (getContentUri()) 
		{
			if(dbHelper!=null || selectionArgs!=null)
			{
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				if(db.isOpen())
				{				
					db.delete(getTableName(), selection,selectionArgs);
					db.close();
				}
			}
			return 0;
		}
	}
	
	

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) 
	{
		synchronized (getContentUri()) 
		{
			int rows=0;
			
			if(values==null || dbHelper == null)
			{
				return rows;
			}
			
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			if(db.isOpen())
			{
				rows=db.update(getTableName(), values, selection, selectionArgs);
				db.close();
				db=null;
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
    public static boolean isHave(Context mContext, String selection,String[] selectionArgs,Uri content_uri)
    {
		boolean flag = false;
		if (mContext == null || StringUtils.isEmpty(selection) || selectionArgs == null) 
		{
			return flag;
		}
		Cursor cursor = null;
		try {
			cursor = mContext.getContentResolver().query(content_uri, null,
					selection, selectionArgs, null);

		} catch (Exception e) 
		{
			e.printStackTrace();
		}
		if (cursor != null)
		{
			if (cursor.moveToFirst()) 
			{
				flag = true;
			}
			cursor.close();
			cursor=null;
		}
		Log.d("gd", "this data that db has "+flag);
		return flag;
	}
	

}
