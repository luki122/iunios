package com.android.settings.lscreen;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class LSContentProvideImp extends LSContentProvider {
	
	private static final String URL_STR="content://com.android.settings.lscreen.LSContentProvideImp";

	private static final Uri CONTENT_URI=Uri.parse(URL_STR);
	
	
	@Override
	public String getTableName() {
		return LSDataSqlite.TABLE_NAME_LSCREEN_APP;
	}

	@Override
	public Uri getContentUri() {
		return CONTENT_URI;
	}
	
	/*
	 * 插入数据 
	 */
	public static boolean insertOrUpdateDate(Context mContext , String packageName)
	{
		if(mContext==null || packageName==null)
		{
			return false;
		}
		
		ContentValues values=new ContentValues();
	    values.put(LSDataSqlite.PACKAGE_NAME, packageName);
	    
	    Log.d("gd", "insert packageName="+packageName);
	    
	    if(isHave(mContext, getQueryWhereOfPkgName(), getQueryValueOfPkgName(packageName), CONTENT_URI))
	    {
	    	Log.d("gd", " The presence of this data ");
	    	return false;
	    }else
	    {
	        mContext.getContentResolver().insert(CONTENT_URI, values);
	        mContext.getContentResolver().notifyChange(CONTENT_URI, null);
	        return true;
	    }
	}
	
	
	/*
	 *  删除数据 
	 */
	public static void deleteData(Context mContext,String packageName)
	{
		if(mContext==null || packageName == null)
		{
			return;
		}
		mContext.getContentResolver().delete(CONTENT_URI, getQueryWhereOfPkgName(), getQueryValueOfPkgName(packageName));
		mContext.getContentResolver().notifyChange(CONTENT_URI, null);
	}
	
	/*
	 *  获取数据
	 */
	public static DataArrayList<AppInfo> getLSAppInfo(Context mContext) {
		synchronized (CONTENT_URI) {
			DataArrayList<AppInfo> LSAppList = new DataArrayList<AppInfo>();
			if (mContext == null) {
				return LSAppList;
			}

			String[] columes = {LSDataSqlite.PACKAGE_NAME};

			Cursor cursor = null;

			try {
				cursor = mContext.getContentResolver().query(CONTENT_URI,
						columes,null, null, null, null);
				if (cursor != null) {
					while (cursor.moveToNext()) {
						String packageName = cursor.getString(cursor
								.getColumnIndex(LSDataSqlite.PACKAGE_NAME));
						AppInfo lsAppData = new AppInfo();
						lsAppData.setPackageName(packageName);
						LSAppList.add(lsAppData);
					}
				}else
				{
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if(cursor!=null)
				{
					cursor.close();
					cursor = null;
				}
			}
			return LSAppList;
		}
	}
	
    
    private static String getQueryWhereOfPkgName(){
    	return LSDataSqlite.PACKAGE_NAME+" = ?";
    }
    
    private static String[] getQueryValueOfPkgName(String pkgName){
    	String[] whereValue = {pkgName};
    	return whereValue;
    }   
	

}
