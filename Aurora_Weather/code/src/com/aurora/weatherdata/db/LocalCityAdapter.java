package com.aurora.weatherdata.db;

import java.util.ArrayList;
import java.util.List;

import datas.WeatherCityInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LocalCityAdapter extends SQLiteOpenHelper {

	private final static String DATABASE_NAME = "local_city.db";  
    private final static int DATABASE_VERSION = 1;  
    private final static String TABLE_NAME = "local_city_table";  
    public final static String CITY_ID = "city_id";  
    public final static String CITY_NAME = "city_name";  
    public final static String IS_LOCAL_CITY = "is_local_city";  
    public LocalCityAdapter(Context context) {  
      // TODO Auto-generated constructor stub  
        super(context, DATABASE_NAME, null, DATABASE_VERSION);  
  }  
  //创建table  
  @Override  
  public void onCreate(SQLiteDatabase db) {  
        String sql = "CREATE TABLE " + TABLE_NAME + " (" + CITY_ID  
          + " INTEGER, " + CITY_NAME + " text, "+  IS_LOCAL_CITY +" INTEGER);";  
        db.execSQL(sql);  
  }  
  
  @Override  
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  
      String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;  
      db.execSQL(sql);  
      onCreate(db);  
  }  
    
  public List<WeatherCityInfo> selectAll() {  
	  List<WeatherCityInfo> citys=new ArrayList<WeatherCityInfo>();
	  synchronized (LocalCityAdapter.class) {
      SQLiteDatabase db = this.getReadableDatabase();  
      Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);  
      if(cursor.getCount()==0)
      {
    	 cursor.close();
		 return citys;
      }
		cursor.moveToFirst();
		do{
			WeatherCityInfo info=new WeatherCityInfo();
			info.setID(cursor.getInt(0));
			info.setCityName(cursor.getString(1));
			info.setIsLocalCity(cursor.getInt(2)==1);
			citys.add(info);
		}while(cursor.moveToNext());
		cursor.close();
       db.close();
	  }
      return citys;  
  }  
   
   public void inserCityList(List<WeatherCityInfo> citys){
	   synchronized (LocalCityAdapter.class) {
	   SQLiteDatabase db = this.getWritableDatabase();  
	   db.delete(TABLE_NAME, null, null);  
	   for (WeatherCityInfo weatherCityInfo : citys) {
		   ContentValues cv = new ContentValues();  
		   cv.put(CITY_NAME, weatherCityInfo.getCityName());  
		   cv.put(CITY_ID,weatherCityInfo.getID());
		   cv.put(IS_LOCAL_CITY, weatherCityInfo.getIsLocalCity()?1:0);  
		   db.insert(TABLE_NAME, null, cv);  
	 }
	 db.close();
	   }
   }
}
