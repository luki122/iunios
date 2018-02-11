package com.aurora.weather.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class CTWOpenHelper extends SQLiteOpenHelper {
    
    public static final String DB_NAME = "city_weather.db";
    private static final int DB_VERSION = 1;
    
    private static final String CTW_TABLE_NAME = "ctw";
    
    private static final String ID                  = "_id";
    //city name
    private static final String CITY_NAME            = "cn";
    //format yyyy:MM:dd:HH
    //time section
    private static final String TIME_SECTION         = "ts";
    //weather info
    private static final String WEATHER_INFO         =  "wi";
    
    private String[] selectColumn = new String[] {
            WEATHER_INFO
    };
    
    public CTWOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        // TODO Auto-generated constructor stub
    }

    private void createCWtable(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + CTW_TABLE_NAME + " ("
                          + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                          + CITY_NAME + " TEXT NOT NULL, "
                          + TIME_SECTION + " TEXT NOT NULL, "
                          + WEATHER_INFO + " TEXT NOT NULL "
                          + ");";
        db.execSQL(sql);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        createCWtable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        String sql = "DROP TABLE IF EXISTS " + CTW_TABLE_NAME;  
        db.execSQL(sql);  
        onCreate(db);  
    }
    
    private void closeDb(SQLiteDatabase db) {
        if (db != null) {
            db.close();
            db = null;
        }
    }
    
    private void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
            cursor = null;
        }
    }
    
    private void close(SQLiteDatabase db, Cursor cusror) {
        closeDb(db);
        closeCursor(cusror);
    }
    
    public String getWeatherInfoCache(String cityName, String timeSection) {
        Cursor result = null;
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
        } catch (SQLiteException e) {
            db = this.getReadableDatabase();
        }
        String selection = CITY_NAME + " = ? AND " + TIME_SECTION + " = ?";
        String[] selectionArgs = new String[] {
                cityName,
                timeSection
        };
        try {
            result = db.query(CTW_TABLE_NAME, selectColumn, selection, selectionArgs, null, null, null);
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            if (result != null && result.moveToFirst()) {
                String weatherInfo = result.getString(0);
                close(db, result);
                return weatherInfo;
            }
        }
        close(db, result);
        return null;
    }
    
    public void insert(String cityName, String timeSection, String weatherInfo) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
        } catch (SQLiteException e) {
            db = this.getReadableDatabase();
        }
        ContentValues values = new ContentValues();
        values.put(CITY_NAME, cityName);
        values.put(TIME_SECTION, timeSection);
        values.put(WEATHER_INFO, weatherInfo);
        try {
            db.insert(CTW_TABLE_NAME, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDb(db);
        }
    }
}
