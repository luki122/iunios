package com.aurora.weatherdata.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.aurora.weatherdata.bean.CacheItem;
import com.aurora.weatherdata.util.FileLog;

import java.util.ArrayList;
import java.util.List;

public class CacheDataAdapter extends DBAdapter {

	public static final String TAG = "CacheDataAdapter";
	public static final String TABLE_NAME = "cache_data"; // 数据库表名
	public static final String CITY_ID = "city_id";
	public static final String CITY_NAME = "city_name";
	public static final String WEATHER_DATA = "weather_data"; // 缓存的数据
	public static final String UPDATE_TIME = "update_time"; // 更新时间

	private DBOpenHelper mDBOpenHelper;
	private SQLiteDatabase mDb;
	private Context mContext;

	public CacheDataAdapter(Context context) {
		this.mContext = context;
	}

	/**
	 * 空间不够存储的时候设为只读
	 * 
	 * @throws SQLiteException
	 */
	public void open() throws SQLiteException {
		mDBOpenHelper = new DBOpenHelper(mContext);
		try {
			mDb = mDBOpenHelper.getWritableDatabase();
		} catch (SQLiteException e) {
			mDb = mDBOpenHelper.getReadableDatabase();
			FileLog.e(TAG, e.toString());
		}
	}

	/**
	 * 
	 * 调用SQLiteDatabase对象的close()方法关闭数据库
	 */
	public void close() {
		if (mDb != null) {
			mDb.close();
			mDb = null;
		}
	}

	public void insert(List<CacheItem> list) {
		mDb.beginTransaction();

		try {
			for (CacheItem item : list) {
				insert(item);
			}
			mDb.setTransactionSuccessful();
		} catch (Exception e) {
			FileLog.e(TAG, e.toString());
		} finally {
			mDb.endTransaction();
		}
	}

	public void insert(CacheItem item) {
		ContentValues values = new ContentValues();
		values.put(CITY_ID, item.getCityId());
		values.put(CITY_NAME, item.getCityName());
		values.put(WEATHER_DATA, item.getWeatherData());;
		values.put(UPDATE_TIME, item.getUpdateTime());

		mDb.insert(TABLE_NAME, null, values);
	}

	public ArrayList<CacheItem> queryAllData() {
		Cursor cursor = mDb.query(TABLE_NAME, new String[] { CITY_ID, CITY_NAME, WEATHER_DATA, UPDATE_TIME },
				null, null, null, null, UPDATE_TIME + " desc");

		ArrayList<CacheItem> cacaheList = convertToCache(cursor);
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
			cursor = null;
		}
		return cacaheList;
	}

	private ArrayList<CacheItem> convertToCache(Cursor cursor) {
		int resultCounts = cursor.getCount();
		if (resultCounts == 0 || !cursor.moveToFirst()) {
			return null;
		}

		ArrayList<CacheItem> cacheList = new ArrayList<CacheItem>();
		for (int i = 0; i < resultCounts; i++) {
			CacheItem cacheItem = new CacheItem();
			cacheItem.setCityId(cursor.getString(cursor.getColumnIndex(CITY_ID)));
			cacheItem.setCityName(cursor.getString(cursor.getColumnIndex(CITY_NAME)));
			cacheItem.setWeatherData(cursor.getString(cursor.getColumnIndex(WEATHER_DATA)));
			cacheItem.setUpdateTime(cursor.getLong(cursor.getColumnIndex(UPDATE_TIME)));

			cacheList.add(cacheItem);
			cursor.moveToNext();
		}
		return cacheList;
	}

	public String queryData(String cityId) {
		Cursor cursor = mDb.query(TABLE_NAME, new String[] { WEATHER_DATA },
				CITY_ID + "=?", new String[] { cityId }, null, null, UPDATE_TIME + " desc");

		String result = null;

		if (cursor.moveToFirst()) {
			result = cursor.getString(cursor.getColumnIndex(WEATHER_DATA));
		}

		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}

		return result;
	}

	public int deleteAllData() {
		return mDb.delete(TABLE_NAME, null, null);
	}

	public int deleteData(String cityId) {
		return mDb.delete(TABLE_NAME, CITY_ID + "=?", new String[] { cityId });
	}

	public int updateData(CacheItem item) {
		ContentValues values = new ContentValues();
		values.put(CITY_ID, item.getCityId());
		values.put(CITY_NAME, item.getCityName());
		values.put(WEATHER_DATA, item.getWeatherData());
		values.put(UPDATE_TIME, item.getUpdateTime());

		return mDb.update(TABLE_NAME, values, CITY_ID + "=?", new String[] { item.getCityId() });
	}

}