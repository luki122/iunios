package com.aurora.thememanager.db;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.aurora.thememanager.entities.Theme;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;



public class LoadedThemeDao {

	private LoadedThemeDbHelper dbHelper; // 数据库帮助类
	private SQLiteDatabase db; // 数据库对象

	public LoadedThemeDao(Context context) {
		dbHelper = new LoadedThemeDbHelper(context);
	}

	/**
	 * 打开数据库
	 */
	public void openDatabase() {
		db = dbHelper.getWritableDatabase();
	}

	/**
	 * 关闭数据库
	 */
	public void closeDatabase() {
		if (db != null && db.isOpen()) {
			db.close();
		}
	}

	/**
	 * 插入一条已安装Theme信息
	 * 
	 * @param theme
	 */
	public void insert(Theme theme) {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		if (db != null) {
			ContentValues values = new ContentValues();
			values.put(LoadedThemeDbHelper.THEME_NAME, theme.name);
			values.put(LoadedThemeDbHelper.THEME_ID, theme.themeId);
			values.put(LoadedThemeDbHelper.VERSION, theme.version);
			values.put(LoadedThemeDbHelper.SAVE_PATH, theme.importPathName);
			
			
			values.put(LoadedThemeDbHelper.AUTHOR, theme.author);
			values.put(LoadedThemeDbHelper.TYPE, theme.type);
			values.put(LoadedThemeDbHelper.SIZE, theme.sizeStr);
			values.put(LoadedThemeDbHelper.DESC, theme.description);
			db.insert(LoadedThemeDbHelper.TABLE, null, values);
		}
	}

	public int getInstalledCount() {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(LoadedThemeDbHelper.TABLE,
					null, null, null, null, null, null);
			if (cursor != null) {
				int count = cursor.getCount();
				cursor.close();
				return count;
			}
		}
		return 0;
	}

	/**
	 * 获取已安装Theme列表
	 * 
	 * @return
	 */
	public List<Theme> getLoadedThemes() {
		List<Theme> infos = new ArrayList<Theme>();
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(LoadedThemeDbHelper.TABLE,
					null, null, null, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					Theme theme = new Theme();
					theme.name = (cursor.getString(1));
					theme.themeId = (cursor.getInt(0));
					theme.version = (cursor.getString(2));
					theme.importPathName = (cursor.getString(3));
					
					theme.author = cursor.getString(4);
					theme.type = Integer.parseInt(cursor.getString(5));
					theme.sizeStr = cursor.getString(6);
					theme.description = cursor.getString(7);
					infos.add(theme);
				}
			}
			cursor.close();
		}
		return infos;
	}

	public Theme getTheme(int themeId) {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(LoadedThemeDbHelper.TABLE,
					null, LoadedThemeDbHelper.THEME_ID + "=?",
					new String[] { themeId+"" }, null, null, null);
			Theme theme = null;
			if (cursor.moveToFirst()) {
				theme = new Theme();
				theme.name = (cursor.getString(1));
				theme.themeId = (cursor.getInt(0));
				theme.version = (cursor.getString(2));
				theme.importPathName = (cursor.getString(3));
				
				theme.author = cursor.getString(4);
				theme.type = Integer.parseInt(cursor.getString(5));
				theme.sizeStr = cursor.getString(6);
				theme.description = cursor.getString(7);
			}
			cursor.close();
			return theme;
		}
		return null;
	}

	public void deleteLoadedTheme(String packageName) {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		if (db != null) {
			db.delete(LoadedThemeDbHelper.TABLE,
					LoadedThemeDbHelper.THEME_NAME + "=?",
					new String[] { packageName });
		}
	}
	
	public void deleteLoadedTheme(Theme theme) {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		Log.d("delete", "id:"+theme.themeId);
		if (db != null) {
			db.delete(LoadedThemeDbHelper.TABLE,
					LoadedThemeDbHelper.THEME_ID + "=?",
					new String[] { theme.themeId+"" });
		}
	}

	public void updateLoadedTheme(Theme theme) {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		if (db != null) {
			ContentValues values = new ContentValues();
			values.put(LoadedThemeDbHelper.THEME_NAME, theme.name);
			values.put(LoadedThemeDbHelper.THEME_ID, theme.themeId);
			values.put(LoadedThemeDbHelper.VERSION, theme.version);
			values.put(LoadedThemeDbHelper.SAVE_PATH, theme.importPathName);
			
			values.put(LoadedThemeDbHelper.AUTHOR, theme.author);
			values.put(LoadedThemeDbHelper.TYPE, theme.type);
			values.put(LoadedThemeDbHelper.SIZE, theme.sizeStr);
			values.put(LoadedThemeDbHelper.DESC, theme.description);
			db.update(LoadedThemeDbHelper.TABLE, values,
					LoadedThemeDbHelper.THEME_ID + "=?",
					new String[] { theme.themeId+"" });
		}
	}

	/**
	 * 删除所有数据
	 */
	public void deleteAll() {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		if (db != null) {
			db.delete(LoadedThemeDbHelper.TABLE, null, null);
		}
	}

	/**
	 * 获取已安装Theme列表
	 */
	public void printLoadedThemeList() {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(LoadedThemeDbHelper.TABLE,
					null, null, null, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					Theme theme = new Theme();
					theme.name = (cursor.getString(1));
					theme.themeId = (cursor.getInt(0));
					theme.version = (cursor.getString(2));
					theme.importPathName = (cursor.getString(3));
					
					theme.author = cursor.getString(4);
					theme.type = Integer.parseInt(cursor.getString(5));
					theme.sizeStr = cursor.getString(6);
					theme.description = cursor.getString(7);
				}
			}
			cursor.close();
		}
	}

}
