package com.aurora.weatherdata.db;

import datas.WeatherWarningInfo;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class WarnInfoAdapter extends SQLiteOpenHelper {

	private final static String DATABASE_NAME = "warn_info.db";  
    private final static int DATABASE_VERSION = 1;  
    private final static String TABLE_NAME = "warn_info_table";  
    public final static String CITY_ID = "city_id";  
    public final static String WARN_TITLE = "warn_title";  
    public final static String WARN_CONTENT = "warn_content";
    public final static String WARN_LEVEL="warn_level";
    public final static String WARN_WEATHER="warn_weather";
    
    private SQLiteDatabase database;
    public WarnInfoAdapter(Context context) {  
      // TODO Auto-generated constructor stub  
        super(context, DATABASE_NAME, null, DATABASE_VERSION);  
  }  
  //创建table  
  @Override  
  public void onCreate(SQLiteDatabase db) {  
        String sql = "CREATE TABLE " + TABLE_NAME + " (" + CITY_ID  
          + " INTEGER, " + WARN_TITLE + " text, "+  WARN_CONTENT +" text,"+
          WARN_LEVEL +" text,"+WARN_WEATHER +" text"+");";  
        db.execSQL(sql);  
  }  
  
  @Override  
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  
      String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;  
      db.execSQL(sql);  
      onCreate(db);  
  }  
  
  public boolean isExistWarnInfo(int cityId,WeatherWarningInfo info){
	  database = getReadableDatabase();
	  Cursor cursor=database.query(TABLE_NAME, null, "city_id=?", new String[]{cityId+""}, null, null, null);
	  if(cursor==null)
	  {
		  database.close();
		  return false;
	  }else{
		  if(cursor.moveToFirst())
		  {
			  WeatherWarningInfo dbWarnInfo=new WeatherWarningInfo();
			  dbWarnInfo.setTitle(cursor.getString(1));
			  dbWarnInfo.setDetail(cursor.getString(2));
			  dbWarnInfo.setLevel(cursor.getString(3));
			  dbWarnInfo.setWeather(cursor.getString(4));
			  cursor.close();
			  database.close();
			  database=null;
			  return info.equals(dbWarnInfo);
		  }else{
			  return false;
		  }
	  }
  }
  
  public void addWarnInfo(int cityId,WeatherWarningInfo info){
	  ContentValues values = new ContentValues();
	  values.put(CITY_ID, cityId);
	  values.put(WARN_TITLE, info.getTitle());
	  values.put(WARN_CONTENT, info.getDetail());
	  values.put(WARN_LEVEL, info.getLevel());
	  values.put(WARN_WEATHER, info.getWeather());
	  database = getWritableDatabase();
	  Cursor cursor=database.query(TABLE_NAME, null, "city_id=?", new String[]{cityId+""}, null, null, null);
	  if(cursor!=null&&cursor.getCount()!=0)
	  {
		  database.update(TABLE_NAME, values, "city_id=?", new String[]{cityId+""});
		  cursor.close();
		  cursor=null;
	  }else{
		  database.insert(TABLE_NAME, null, values);
	  }
	  database.close();
	  database=null;
  }
  
  public void deleteWarnInfo(int cityId){
	  database = getWritableDatabase();
	  database.delete(TABLE_NAME, "city_id=?", new String[]{cityId+""});
	  database.close();
	  database=null;
  }
  
}
