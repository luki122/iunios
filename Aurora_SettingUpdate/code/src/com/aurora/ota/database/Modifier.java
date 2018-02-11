package com.aurora.ota.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Modifier implements DatabaseModifier{

	
	private DataBaseCreator helper;
	private Context mContextt;
	
	public Modifier(Context context){
		mContextt = context;
		helper = DataBaseCreator.getInstance(mContextt);
	}
	@Override
	public long insert(String table, String nullColumnHack, ContentValues values) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = helper.getWritableDatabase();
		
		return db.insert(table, nullColumnHack, values);
	}

	@Override
	public long insert(ContentValues values) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(String table, ContentValues values, String whereClause,
			String[] whereArgs) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = helper.getWritableDatabase();
		return db.update(table, values, whereClause, whereArgs);
	}

	@Override
	public int delete(String table, String whereClause, String[] whereArgs) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = helper.getWritableDatabase();
		
		return db.delete(table, whereClause, whereArgs);
	}
	@Override
	public Cursor query(String table,String[] columns,String selection,String[]selectionArgs,String groupBy,
	        String having,String orderBy){
	    SQLiteDatabase db = helper.getWritableDatabase();
	    return db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
	}
	
	public void closeDatabase(){
	    helper.close();
	}

}
