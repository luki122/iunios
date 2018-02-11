package com.aurora.commemoration.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.aurora.calendar.util.TimeUtils;
import com.aurora.commemoration.model.RememberDayInfo;
import com.gionee.calendar.view.Log;

import java.util.ArrayList;
import java.util.List;

public class RememberDayDao {

	private RememberDayDBHelper dbHelper; // 数据库帮助类
	private SQLiteDatabase db; // 数据库对象

	public RememberDayDao(Context context) {
		dbHelper = new RememberDayDBHelper(context);
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
	 * 插入一条信息
	 *
	 * @param dayInfo
	 */
	public Long insert(RememberDayInfo dayInfo) {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		// if (db != null) {
		ContentValues values = new ContentValues();
		values.put(RememberDayDBHelper.TITLE, dayInfo.getTitle());
		values.put(RememberDayDBHelper.DAY, dayInfo.getDay());
		values.put(RememberDayDBHelper.REMINDER_DATA, dayInfo.getReminderData());
		values.put(RememberDayDBHelper.PIC_PATH, dayInfo.getPicPath());
		values.put(RememberDayDBHelper.SCHEDULE_FLAG, dayInfo.getScheduleFlag());
		values.put(RememberDayDBHelper.MILL_TIME,
				String.valueOf(dayInfo.getMillTime()));
		return db.insert(RememberDayDBHelper.REMEMBER_DAY_TABLE, null, values);
		// }

		// Log.e("JOY:" + dayInfo.getTitle() + " " + dayInfo.getDay());
		// return getItemByMILL_TIME(dayInfo.getMillTime());
	}

	/**
	 * 获取纪念日数目
	 *
	 * @return
	 */
	public int getRememberDayCount() {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(RememberDayDBHelper.REMEMBER_DAY_TABLE,
					null, null, null, null, null, null);
			if (cursor != null) {
				int count = cursor.getCount();
				cursor.close();
				return count;
			}
		}
		return 0;
	}

	public RememberDayInfo getItemByMILL_TIME(Long milltime) {
		int id = 0;
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		RememberDayInfo dayInfo = new RememberDayInfo();
		if (db != null) {
			Cursor cursor = db.query(RememberDayDBHelper.REMEMBER_DAY_TABLE,
					null, RememberDayDBHelper.MILL_TIME + "=?",
					new String[] { milltime + "" }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				dayInfo.setId(cursor.getInt(0));
				dayInfo.setTitle(cursor.getString(1));
				dayInfo.setPicPath(cursor.getString(2));
				dayInfo.setDay(cursor.getString(3));
				dayInfo.setReminderData(cursor.getLong(4));
				dayInfo.setScheduleFlag(cursor.getInt(5));
				dayInfo.setMillTime(Long.valueOf(cursor.getString(6)));

			}
			cursor.close();
		}

		return dayInfo;
	}

	public RememberDayInfo getItemById(int id) {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		RememberDayInfo dayInfo = new RememberDayInfo();
		if (db != null) {
			Cursor cursor = db.query(RememberDayDBHelper.REMEMBER_DAY_TABLE,
					null, RememberDayDBHelper.ID + "=?",
					new String[] { id + "" }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				dayInfo.setId(cursor.getInt(0));
				dayInfo.setTitle(cursor.getString(1));
				dayInfo.setPicPath(cursor.getString(2));
				dayInfo.setDay(cursor.getString(3));
				dayInfo.setReminderData(cursor.getLong(4));
				dayInfo.setScheduleFlag(cursor.getInt(5));
				dayInfo.setMillTime(Long.valueOf(cursor.getString(6)));
			}
			cursor.close();
		}

		return dayInfo;
	}

	public List<RememberDayInfo> getRememberDayList() {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		List<RememberDayInfo> infos = new ArrayList<RememberDayInfo>();
		if (db != null) {
			String curTime = TimeUtils.getStringDateShort();
			Cursor cursor = db.query(RememberDayDBHelper.REMEMBER_DAY_TABLE,
					null, RememberDayDBHelper.DAY + ">=?",
					new String[] { curTime + "" }, null, null,
					RememberDayDBHelper.DAY + " asc");
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					Log.e("JOY" + cursor.getString(3) + " " + curTime);
					RememberDayInfo dayInfo = new RememberDayInfo();
					dayInfo.setId(cursor.getInt(0));
					dayInfo.setTitle(cursor.getString(1));
					dayInfo.setPicPath(cursor.getString(2));
					dayInfo.setDay(cursor.getString(3));
					dayInfo.setFutureFlag(true);
					dayInfo.setReminderData(cursor.getLong(4));
					dayInfo.setScheduleFlag(cursor.getInt(5));
					dayInfo.setMillTime(Long.valueOf(cursor.getString(6)));
					infos.add(dayInfo);
				}
			}
			cursor.close();

			cursor = db.query(RememberDayDBHelper.REMEMBER_DAY_TABLE, null,
					RememberDayDBHelper.DAY + "<?", new String[] { curTime
							+ "" }, null, null, RememberDayDBHelper.DAY
							+ " asc");
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					Log.e("JOY" + cursor.getString(3) + " " + curTime);
					RememberDayInfo dayInfo = new RememberDayInfo();
					dayInfo.setId(cursor.getInt(0));
					dayInfo.setTitle(cursor.getString(1));
					dayInfo.setPicPath(cursor.getString(2));
					dayInfo.setDay(cursor.getString(3));
					dayInfo.setFutureFlag(false);
					dayInfo.setReminderData(cursor.getLong(4));
					dayInfo.setScheduleFlag(cursor.getInt(5));
					dayInfo.setMillTime(Long.valueOf(cursor.getString(6)));
					infos.add(dayInfo);
				}
			}
			cursor.close();
		}
		return infos;
	}

	public List<RememberDayInfo> getNotifiDaTaList() {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		List<RememberDayInfo> infos = new ArrayList<RememberDayInfo>();
		if (db != null) {
			Cursor cursor = db.query(RememberDayDBHelper.REMEMBER_DAY_TABLE,
					null, RememberDayDBHelper.REMINDER_DATA + ">?",
					new String[] { System.currentTimeMillis() + "" }, null, null,
					RememberDayDBHelper.REMINDER_DATA + " asc");
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					RememberDayInfo dayInfo = new RememberDayInfo();
					dayInfo.setId(cursor.getInt(0));
					dayInfo.setTitle(cursor.getString(1));
					dayInfo.setPicPath(cursor.getString(2));
					dayInfo.setDay(cursor.getString(3));
					dayInfo.setFutureFlag(true);
					dayInfo.setReminderData(cursor.getLong(4));
					dayInfo.setScheduleFlag(cursor.getInt(5));
					dayInfo.setMillTime(Long.valueOf(cursor.getString(6)));
					infos.add(dayInfo);
					
				}
			}
			cursor.close();

			
		}
		return infos;
	}
	
	
	public List<RememberDayInfo> getScheduleRememberDayList() {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		List<RememberDayInfo> infos = new ArrayList<RememberDayInfo>();
		if (db != null) {
			String curTime = TimeUtils.getStringDateShort();
			Cursor cursor = db.query(RememberDayDBHelper.REMEMBER_DAY_TABLE,
					null, RememberDayDBHelper.DAY + ">=?" + " and "
							+ RememberDayDBHelper.SCHEDULE_FLAG + "=?",
					new String[] { curTime + "", "1" }, null, null,
					RememberDayDBHelper.DAY + " asc");
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					RememberDayInfo dayInfo = new RememberDayInfo();
					dayInfo.setId(cursor.getInt(0));
					dayInfo.setTitle(cursor.getString(1));
					dayInfo.setPicPath(cursor.getString(2));
					dayInfo.setDay(cursor.getString(3));
					dayInfo.setFutureFlag(true);
					dayInfo.setReminderData(cursor.getLong(4));
					dayInfo.setScheduleFlag(cursor.getInt(5));
					dayInfo.setMillTime(Long.valueOf(cursor.getString(6)));
					infos.add(dayInfo);
				}
			}
			cursor.close();

			cursor = db.query(RememberDayDBHelper.REMEMBER_DAY_TABLE, null,
					RememberDayDBHelper.DAY + "<?" + " and "
							+ RememberDayDBHelper.SCHEDULE_FLAG + "=?",
					new String[] { curTime + "", "1" }, null, null,
					RememberDayDBHelper.DAY + " asc");
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					RememberDayInfo dayInfo = new RememberDayInfo();
					dayInfo.setId(cursor.getInt(0));
					dayInfo.setTitle(cursor.getString(1));
					dayInfo.setPicPath(cursor.getString(2));
					dayInfo.setDay(cursor.getString(3));
					dayInfo.setFutureFlag(false);
					dayInfo.setReminderData(cursor.getLong(4));
					dayInfo.setScheduleFlag(cursor.getInt(5));
					dayInfo.setMillTime(Long.valueOf(cursor.getString(6)));
					infos.add(dayInfo);
				}
			}
			cursor.close();
		}
		return infos;
	}

	/**
	 * 更新纪念日
	 *
	 * @param dayInfo
	 */
	public void updateRememberDay(RememberDayInfo dayInfo) {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		if (db != null) {
			ContentValues values = new ContentValues();
			values.put(RememberDayDBHelper.TITLE, dayInfo.getTitle());
			values.put(RememberDayDBHelper.DAY, dayInfo.getDay());
			values.put(RememberDayDBHelper.PIC_PATH, dayInfo.getPicPath());
			// fix me
			values.put(RememberDayDBHelper.REMINDER_DATA,
					dayInfo.getReminderData());
			values.put(RememberDayDBHelper.SCHEDULE_FLAG,
					dayInfo.getScheduleFlag());
			db.update(RememberDayDBHelper.REMEMBER_DAY_TABLE, values,
					RememberDayDBHelper.MILL_TIME + "=?",
					new String[] { String.valueOf(dayInfo.getMillTime()) });
		}
	}

	protected boolean checkDb() {
		if (db == null) {
			openDatabase();
		}
		if (db != null && db.isOpen()) {
			return true;
		}
		return false;
	}

	/**
	 * 更新提醒时间
	 * 
	 * @param
	 * @param
	 */
	public synchronized void updateReminderData(String id, String reminder_date) {
		if (checkDb()) {
			ContentValues values = new ContentValues();
			values.put(RememberDayDBHelper.REMINDER_DATA, reminder_date);
			db.update(RememberDayDBHelper.REMEMBER_DAY_TABLE, values,
					RememberDayDBHelper.ID + "=?", new String[] { id + "" });
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
			db.delete(RememberDayDBHelper.REMEMBER_DAY_TABLE, null, null);
		}
	}

	public void deleteRememberDay(int id) {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		if (db != null) {
			db.delete(RememberDayDBHelper.REMEMBER_DAY_TABLE,
					RememberDayDBHelper.ID + "=?",
					new String[] { String.valueOf(id) });
		}
	}

	public void deleteLastDay(String title, String millTime) {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		if (db != null) {
			db.delete(RememberDayDBHelper.REMEMBER_DAY_TABLE,
					RememberDayDBHelper.TITLE + "=?" + " and "
							+ RememberDayDBHelper.MILL_TIME + "=?",
					new String[] { title, millTime });
		}
	}
}
