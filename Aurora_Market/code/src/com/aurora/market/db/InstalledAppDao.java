package com.aurora.market.db;

import java.util.ArrayList;
import java.util.List;

import com.aurora.market.model.InstalledAppInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;



public class InstalledAppDao {

	private InstalledAppDBHelper dbHelper; // 数据库帮助类
	private SQLiteDatabase db; // 数据库对象

	public InstalledAppDao(Context context) {
		dbHelper = new InstalledAppDBHelper(context);
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
	 * 插入一条已安装APP信息
	 * 
	 * @param appInfo
	 */
	public void insert(InstalledAppInfo appInfo) {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		if (db != null) {
			ContentValues values = new ContentValues();
			values.put(InstalledAppDBHelper.APPNAME, appInfo.getName());
			values.put(InstalledAppDBHelper.ICONID, appInfo.getIconId());
			values.put(InstalledAppDBHelper.VERSIONCODE,
					appInfo.getVersionCode());
			values.put(InstalledAppDBHelper.VERSION, appInfo.getVersion());
			values.put(InstalledAppDBHelper.PACKAGENAME,
					appInfo.getPackageName());
			values.put(InstalledAppDBHelper.APKPATH, appInfo.getApkPath());
			values.put(InstalledAppDBHelper.FLAG, appInfo.getAppFlag());
			db.insert(InstalledAppDBHelper.INSTALLED_TABLE, null, values);
		}
	}

	public int getInstalledCount() {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(InstalledAppDBHelper.INSTALLED_TABLE,
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
	 * 获取已安装APP列表
	 * 
	 * @return
	 */
	public List<InstalledAppInfo> getInstalledAppList() {
		List<InstalledAppInfo> infos = new ArrayList<InstalledAppInfo>();
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(InstalledAppDBHelper.INSTALLED_TABLE,
					null, null, null, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					InstalledAppInfo appInfo = new InstalledAppInfo();
					appInfo.setName(cursor.getString(0));
					appInfo.setIconId(cursor.getInt(1));
					appInfo.setVersionCode(cursor.getInt(2));
					appInfo.setVersion(cursor.getString(3));
					appInfo.setPackageName(cursor.getString(4));
					appInfo.setApkPath(cursor.getString(5));
					appInfo.setAppFlag(cursor.getInt(6));
					infos.add(appInfo);
				}
			}
			cursor.close();
		}
		return infos;
	}

	public InstalledAppInfo getInstalledAppInfo(String packageName) {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(InstalledAppDBHelper.INSTALLED_TABLE,
					null, InstalledAppDBHelper.PACKAGENAME + "=?",
					new String[] { packageName }, null, null, null);
			InstalledAppInfo appInfo = null;
			if (cursor.moveToFirst()) {
				appInfo = new InstalledAppInfo();
				appInfo.setName(cursor.getString(0));
				appInfo.setIconId(cursor.getInt(1));
				appInfo.setVersionCode(cursor.getInt(2));
				appInfo.setVersion(cursor.getString(3));
				appInfo.setPackageName(cursor.getString(4));
				appInfo.setApkPath(cursor.getString(5));
				appInfo.setAppFlag(cursor.getInt(6));
			}
			cursor.close();
			return appInfo;
		}
		return null;
	}

	public void deleteInstalledApp(String packageName) {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		if (db != null) {
			db.delete(InstalledAppDBHelper.INSTALLED_TABLE,
					InstalledAppDBHelper.PACKAGENAME + "=?",
					new String[] { packageName });
		}
	}

	public void updateInstalledApp(InstalledAppInfo appInfo) {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		if (db != null) {
			ContentValues values = new ContentValues();
			values.put(InstalledAppDBHelper.APPNAME, appInfo.getName());
			values.put(InstalledAppDBHelper.ICONID, appInfo.getIconId());
			values.put(InstalledAppDBHelper.VERSIONCODE,
					appInfo.getVersionCode());
			values.put(InstalledAppDBHelper.VERSION, appInfo.getVersion());
			values.put(InstalledAppDBHelper.PACKAGENAME,
					appInfo.getPackageName());
			values.put(InstalledAppDBHelper.APKPATH, appInfo.getApkPath());
			db.update(InstalledAppDBHelper.INSTALLED_TABLE, values,
					InstalledAppDBHelper.PACKAGENAME + "=?",
					new String[] { appInfo.getPackageName() });
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
			db.delete(InstalledAppDBHelper.INSTALLED_TABLE, null, null);
		}
	}

	/**
	 * 获取已安装APP列表
	 */
	public void printInstalledAppList() {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(InstalledAppDBHelper.INSTALLED_TABLE,
					null, null, null, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					InstalledAppInfo appInfo = new InstalledAppInfo();
					appInfo.setName(cursor.getString(0));
					appInfo.setIconId(cursor.getInt(1));
					appInfo.setVersionCode(cursor.getInt(2));
					appInfo.setVersion(cursor.getString(3));
					appInfo.setPackageName(cursor.getString(4));
					appInfo.setApkPath(cursor.getString(5));
					System.out.println(appInfo);
				}
			}
			cursor.close();
		}
	}

}
