package com.aurora.ota.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

public interface DatabaseModifier {
	 /**
     * Use this method to insert a value which you would otherwise do using the
     * {@link SQLiteDatabase#insert(String, String, ContentValues)} method.
     */
    public  long insert(String table, String nullColumnHack, ContentValues values);
    /**
     * Use this method to insert a value which you would otherwise do using the
     * {@link DatabaseUtils.InsertHelper#insert(ContentValues)} method.
     */
    public  long insert(ContentValues values);
    /**
     * Use this method to update a table which you would otherwise do using the
     * {@link SQLiteDatabase#update(String, ContentValues, String, String[])} method.
     */
    public  int update(String table, ContentValues values,
            String whereClause, String[] whereArgs);
    /**
     * Use this method to delete entries from a table which you would otherwise do using the
     * {@link SQLiteDatabase#delete(String, String, String[])} method.
     */
    public  int delete(String table, String whereClause, String[] whereArgs);
    
    public Cursor query(String table,String[] columns,String selection,String[]selectionArgs,String groupBy,
            String having,String orderBy);
    
    public void closeDatabase();
}
