package com.aurora.weatherdata.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

/**
 * Created by joy on 10/25/14.
 */
public class CityConditionAdapter extends CityAdapter {

    public static final String TAG = "WeatherConditionAdapter";
    public static final String TABLE_NAME = "weather_data";// 数据库表名

    public static final String ID = "_id"; // 表属性ID
    public static final String WEATHER = "weather"; // 表属性ID
    public static final String SHORT_WEATHER = "shortWeather";

    private DBOpenHelper mDBOpenHelper;
    private SQLiteDatabase mDb;
    private Context mContext;

    public CityConditionAdapter(Context context) {
        this.mContext = context;
    }

    /**
     * 空间不够存储的时候设为只读
     *
     * @throws android.database.sqlite.SQLiteException
     */

    public void open() throws SQLiteException {
        mDBOpenHelper = new DBOpenHelper(mContext);
        try {
            mDb = mDBOpenHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            mDb = mDBOpenHelper.getReadableDatabase();
        }
    }

    /**
     * 调用SQLiteDatabase对象的close()方法关闭数据库
     */
    public void close() {
        if (mDb != null) {
            mDb.close();
            mDb = null;
        }
    }

    /**
     * 保存缓存数据(单条)
     *
     * @param
     */
    public void insert(String weather, String shortWeather, int id) {
        ContentValues value = new ContentValues();

        value.put(ID, id);
        value.put(WEATHER, weather);
        value.put(SHORT_WEATHER, shortWeather);

        if (mDb != null) {
            mDb.insert(TABLE_NAME, null, value);
        }
    }

    /**
     * 通过天气文字，获取对应的图片ID，如果没有找到，则返回－1
     * @param weather
     * @return
     */
    public int queryPicId(String weather) {
        if (mDb != null) {
            Cursor cursor = mDb.query(TABLE_NAME,
                    null, WEATHER + "=?", new String[]{weather}, null, null, null);
            if (cursor == null || cursor.getCount() == 0) {
                cursor = mDb.query(TABLE_NAME,
                        null, SHORT_WEATHER + "=?", new String[]{weather}, null, null, null);
                if (cursor == null || cursor.getCount() == 0)
                    return -1;
            }
            cursor.moveToNext();
            return cursor.getInt(0);
        }
        return -1;
    }

    public Cursor getItemFromId(int id) {
        if (mDb != null) {
            Cursor cursor = mDb.query(TABLE_NAME,
                    null, ID + "=?", new String[]{id + ""}, null, null, null);
            if (cursor == null || cursor.getCount() == 0)
                return null;
            return cursor;
        } else {
            return null;
        }
    }

    public Cursor getItemFromSW(String shortWeather) {
        if (mDb != null) {
            Cursor cursor = mDb.query(TABLE_NAME,
                    null, SHORT_WEATHER + "=?", new String[]{shortWeather}, null, null, null);
            if (cursor == null || cursor.getCount() == 0)
                return null;
            return cursor;
        } else {
            return null;
        }
    }

    public void updateItem(String shortWeather, int id) {
        if (mDb != null) {
            ContentValues values = new ContentValues();
            values.put(ID, id);

            mDb.update(TABLE_NAME, values, SHORT_WEATHER + "=?",
                    new String[]{shortWeather});
        }
    }
}
