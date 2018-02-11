package com.gionee.astro;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.gionee.astro.GNAstroUtils.DayAstroInfo;
import com.gionee.astro.GNAstroUtils.WeekAstroInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;


public class DBOperations {

	DBOpenHelper helper = null;

	private static DBOperations operations;

	private DBOperations(){
		
	}
	
	public synchronized static DBOperations getInstances(){
		if (operations == null) {
			operations = new DBOperations();
		}
		return operations;
	}
	

	private DBOperations(Context context) {
		if(helper == null){
			helper = new DBOpenHelper(context);
		}
	}
	
	public synchronized static DBOperations getInstances(Context context){
		if (operations == null) {
			operations = new DBOperations(context);
		}
		return operations;
	}
	
	public synchronized static void release(){
		if(operations != null){
			operations = null;
		}
	}

	private SQLiteDatabase getDatabase(Context context){
		SQLiteDatabase db = null;
		try{
			if(helper == null){
				Log.d(DBOpenHelper.DB_NAME,"DBOperations------helper == null!");
				helper = new DBOpenHelper(context);
			}
			db = helper.getWritableDatabase();
		}catch(SQLiteException e){
			Log.e(DBOpenHelper.DB_NAME,"DBOperations------get database exception!", e);
			db = helper.getReadableDatabase();
		}catch(Exception ex){
			Log.e(DBOpenHelper.DB_NAME,"DBOperations------get database exception!", ex);
		}
		return db;
	}

	
	private synchronized void close(SQLiteDatabase db) {
		if (db != null) {
			db.close();
		}
	}

	private void close(Cursor c) {
		if (c != null) {
			c.close();
		}
	}

	/**
	 * 
	 * @param context
	 *            the context of the application
	 * @param note
	 *            the new note will be created
	 * @return
	 */
	public synchronized long createWeekAstroRecord(Context context, GNAstroUtils.WeekAstroInfo weekAstroRecord) {
		Log.i(DBOpenHelper.DB_NAME,"DBOperation------createWeekAstroRecord start!");
		Log.d(DBOpenHelper.DB_NAME,"DBOperation------insert: " + weekAstroRecord.toString());

		long id = -1;

		SQLiteDatabase db = getDatabase(context);
		if(db == null){
			Log.e(DBOpenHelper.DB_NAME,"DBOperation------db == null, createWeekAstroRecord fail, return -1!");
			return id;
		}


		ContentValues cv = new ContentValues();
		cv.put(DBOpenHelper.ASTRO, weekAstroRecord.astro);
		cv.put(DBOpenHelper.WEEKBEGIN, weekAstroRecord.weekBegin);
		cv.put(DBOpenHelper.WEEKEND, weekAstroRecord.weekEnd);
		cv.put(DBOpenHelper.FORTUNETYPE, weekAstroRecord.fortuneType[0]);
		cv.put(DBOpenHelper.FORTUNECONTENT, weekAstroRecord.fortuneContent[0]);
		
		cv.put(DBOpenHelper.YEAR, String.valueOf(weekAstroRecord.year));
		cv.put(DBOpenHelper.MONTH, String.valueOf(weekAstroRecord.month));
		cv.put(DBOpenHelper.MONTH_DAY, String.valueOf(weekAstroRecord.monthDay));
		cv.put(DBOpenHelper.ASTRO_PARA, weekAstroRecord.astroName);
		
		db.beginTransaction();
		
		try {

			id = db.insert(DBOpenHelper.WEEK_ASTRO_TABLE_NAME, DBOpenHelper.CONTENT,
					cv);
			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(DBOpenHelper.DB_NAME,"DBOperations------create WeekAstroRecord error", e);
		} finally {
			db.endTransaction();
			close(db);
		}

		Log.i(DBOpenHelper.DB_NAME,"DBOperation------createWeekAstroRecord end!");
		return id;
	}

	public synchronized void deleteWeekAstroRecord(Context context, GNAstroUtils.WeekAstroInfo weekAstroRecord) {
		Log.i(DBOpenHelper.DB_NAME,"DBOperations------delete WeekAstroRecord start!");
		Log.d(DBOpenHelper.DB_NAME,"DBOperations------delete," + weekAstroRecord.toString());

		SQLiteDatabase db = getDatabase(context);
		if(db == null){
			Log.e(DBOpenHelper.DB_NAME,"DBOperation------db == null, deleteWeekAstroRecord fail, return!");
			return;
		}

		db.beginTransaction();
		try {
			
			Time date = new Time();
			date.set(weekAstroRecord.monthDay, weekAstroRecord.month-1, weekAstroRecord.year);
			String stringDate = date.toString().substring(0, 8);
			
			db.delete(DBOpenHelper.WEEK_ASTRO_TABLE_NAME, 
					DBOpenHelper.ASTRO_PARA + " = ? "+" and "+ 
					DBOpenHelper.WEEKEND+ " >= ? ", 
					new String[] { weekAstroRecord.astroName,stringDate });
		
			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(DBOpenHelper.DB_NAME,"DBOperations------delete WeekAstroRecord error: " + e.toString());
		} finally {
			db.endTransaction();
			close(db);
		}
		Log.i(DBOpenHelper.DB_NAME,"DBOperations------delete WeekAstroRecord end!");
	}

	public synchronized void updateNote(Context context, GNAstroUtils.WeekAstroInfo weekAstroRecord) {
		Log.i(DBOpenHelper.DB_NAME,"DBOperations------update weekAstroRecord start!");
		Log.d(DBOpenHelper.DB_NAME,"DBOperations------update: " + weekAstroRecord.toString());


		SQLiteDatabase db = getDatabase(context);
		if(db == null){
			Log.e(DBOpenHelper.DB_NAME,"DBOperation------db == null, updateWeekAstroRecord fail, return!");
			return;
		}

		ContentValues cv = new ContentValues();
		cv.put(DBOpenHelper.ASTRO, weekAstroRecord.astro);
		cv.put(DBOpenHelper.WEEKBEGIN, weekAstroRecord.weekBegin);
		cv.put(DBOpenHelper.WEEKEND, weekAstroRecord.weekEnd);
		cv.put(DBOpenHelper.FORTUNETYPE, weekAstroRecord.fortuneType[0]);
		cv.put(DBOpenHelper.FORTUNECONTENT, weekAstroRecord.fortuneContent[0]);
		
		String whereClause = 
		DBOpenHelper.ASTRO_PARA + " = ? "+" and "+ 
		DBOpenHelper.WEEKBEGIN+ " = ? "+" and "+
		DBOpenHelper.WEEKEND+ " = ? ";
		
		String[] whereArgs =new String[] { weekAstroRecord.astroName,weekAstroRecord.weekBegin,weekAstroRecord.weekEnd };
		

		try {
			db.beginTransaction();
			db.update(DBOpenHelper.WEEK_ASTRO_TABLE_NAME, cv, whereClause, whereArgs);
			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(DBOpenHelper.DB_NAME,"DBOperations------update error: " + e.toString());
		} finally {
			db.endTransaction();
			close(db);
		}
		Log.i(DBOpenHelper.DB_NAME,"DBOperations------update weekAstroRecord end!");
	}

	
	
	
	
	
	
	
	

	


	public synchronized GNAstroUtils.WeekAstroInfo queryWeekAstro(Context context, int year, int month, int monthDay, String astroName) {
		Log.i(DBOpenHelper.DB_NAME,"DBOperations------query one weekAstroRecord start!");
		Log.d(DBOpenHelper.DB_NAME,"DBOperations------queryOneNote," + astroName);

		WeekAstroInfo response = new WeekAstroInfo();
		
		response.fortuneType = new String[1];
		response.fortuneContent = new String[1];
		response.num = 1;
		

		SQLiteDatabase db = getDatabase(context);
		if(db == null){
			Log.e(DBOpenHelper.DB_NAME,"DBOperation------db == null, queryOneWeekAstro fail!");
			return null;
		}

		Time date = new Time();
		date.set(monthDay, month-1, year);
		String stringDate = date.toString().substring(0, 8);
		
		String[] whereArgs =new String[] { astroName,stringDate,stringDate};

		
		String[] columns = new String[] { DBOpenHelper.NOTE_ALL };
		Cursor c = null;
		try {
			c = db.query(DBOpenHelper.WEEK_ASTRO_TABLE_NAME, columns, 
					DBOpenHelper.ASTRO_PARA + " = ? "+" and "+ 
					DBOpenHelper.WEEKBEGIN+ " <= ? "+" and "+
					DBOpenHelper.WEEKEND+ " >= ? ",					
					whereArgs, null, null, null);
		} catch (Exception e) {
			Log.e(DBOpenHelper.DB_NAME,"DBOperations------queryOneWeekAstro error :" + e);
			close(c);
		}

		if (c != null && c.moveToFirst() != false) {
			
			
			response.astro =c.getString(c.getColumnIndex(DBOpenHelper.ASTRO));
			response.weekBegin =c.getString(c.getColumnIndex(DBOpenHelper.WEEKBEGIN));
			response.weekEnd =c.getString(c.getColumnIndex(DBOpenHelper.WEEKEND));
			response.fortuneType[0] =c.getString(c.getColumnIndex(DBOpenHelper.FORTUNETYPE));
			response.fortuneContent[0] =c.getString(c.getColumnIndex(DBOpenHelper.FORTUNECONTENT));
			
			
			
			
		}
		else
		{
			return null; 
		}
		this.close(c);
		this.close(db);

		Log.i(DBOpenHelper.DB_NAME,"DBOperations------query one weekAstroRecord end!");
		return response;
	}

	
	
	
	
	
	

//	public synchronized String getDate() {
//		Log.i("DBOperations------getDate!");
//
//		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//		return format.format(new Date());
//	}
//
//	public synchronized String getTime() {
//		Log.i("DBOperations------getTime!");
//
//		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
//		return format.format(new Date());
//	}
//
//	private synchronized void cursorToNoteForNote(Cursor cursor, List<Note> noteList) {
//		if (cursor != null && cursor.moveToFirst() != false) {
//			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
//			.moveToNext()) {
//				Note note = new Note();
//				note.setId(cursor.getString(cursor
//						.getColumnIndex(DBOpenHelper.ID)));
//				noteList.add(note);
//			}
//		}
//	}
//
//	private synchronized void cursorToNote(Cursor cursor, List<Note> noteList) {
//		if (cursor != null && cursor.moveToFirst() != false) {
//			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
//			.moveToNext()) {
//				Note note = new Note();
//				note.setId(cursor.getString(cursor
//						.getColumnIndex(DBOpenHelper.ID)));
//				note.setContent(cursor.getString(cursor
//						.getColumnIndex(DBOpenHelper.CONTENT)));
//				note.setUpdateDate(cursor.getString(cursor
//						.getColumnIndex(DBOpenHelper.UPDATE_DATE)));
//				note.setUpdateTime(cursor.getString(cursor
//						.getColumnIndex(DBOpenHelper.UPDATE_TIME)));
//				note.setAlarmTime(cursor.getString(cursor
//						.getColumnIndex(DBOpenHelper.ALARM_TIME)));
//				note.setBgColor(cursor.getString(cursor
//						.getColumnIndex(DBOpenHelper.BG_COLOR)));
//				note.setIsFolder(cursor.getString(cursor
//						.getColumnIndex(DBOpenHelper.IS_FOLDER)));
//				note.setParentFile(cursor.getString(cursor
//						.getColumnIndex(DBOpenHelper.PARENT_FOLDER)));
//				note.setNoteFontSize(cursor.getString(cursor
//						.getColumnIndex(DBOpenHelper.NOTE_FONT_SIZE)));
//				note.setNoteListMode(cursor.getString(cursor
//						.getColumnIndex(DBOpenHelper.NOTE_LIST_MODE)));
//
//				note.setWidgetId(cursor.getString(cursor
//						.getColumnIndex(DBOpenHelper.WIDGET_ID)));
//				note.setWidgetType(cursor.getString(cursor
//						.getColumnIndex(DBOpenHelper.WIDGET_TYPE)));
//
//				note.setHaveNoteCount(Integer.parseInt(cursor.getString(cursor
//						.getColumnIndex(DBOpenHelper.FOLDER_HAVE_NOTE_COUNTS))));
//				note.setTitle(cursor.getString(cursor
//						.getColumnIndex(DBOpenHelper.NOTE_TITLE)));
//				note.setMediaFolderName(cursor.getString(cursor
//						.getColumnIndex(DBOpenHelper.MEDIA_FOLDER_NAME)));
//				note.setNoteMediaType(cursor.getInt(cursor.getColumnIndex(DBOpenHelper.NOTE_MEDIA_TYPE)));
//				//				if(!Constants.MEDIA_FOLDER_NAME.equals(cursor.getString(cursor
//				//						.getColumnIndex(DBOpenHelper.MEDIA_FOLDER_NAME)))){
//				//					//TODO jiating
//				//					
//				//					note.setMediaInfos(queryMeidas( ))
//				//				}
//				note.setAddressName(cursor.getString(cursor.getColumnIndex(DBOpenHelper.ADDRESS_NAME)));
//				note.setAddressDetail(cursor.getString(cursor.getColumnIndex(DBOpenHelper.ADDRESS_DETAIL)));
//				noteList.add(note);
//			}
//		}
//	}

	


	
	
	
	
	public synchronized long createDayAstroRecord(Context context, GNAstroUtils.DayAstroInfo DayAstroRecord) {
		Log.i(DBOpenHelper.DB_NAME,"DBOperation------createDayAstroRecord start!");
		Log.d(DBOpenHelper.DB_NAME,"DBOperation------insert: " + DayAstroRecord.toString());

		long id = -1;

		SQLiteDatabase db = getDatabase(context);
		if(db == null){
			Log.e(DBOpenHelper.DB_NAME,"DBOperation------db == null, createDayAstroRecord fail, return -1!");
			return id;
		}

		

		ContentValues cv = new ContentValues();
		cv.put(DBOpenHelper.SELF_ASTRO, DayAstroRecord.selfAstro);
		cv.put(DBOpenHelper.INDEX_COMPREHENSION, DayAstroRecord.indexComprehension);
		cv.put(DBOpenHelper.INDEX_LOVE, DayAstroRecord.indexLove);
		
		cv.put(DBOpenHelper.INDEX_CAREER, DayAstroRecord.indexCareer);
		cv.put(DBOpenHelper.INDEX_FINANCE, DayAstroRecord.indexFinance);
		cv.put(DBOpenHelper.INDEX_HEALTH, DayAstroRecord.indexHealth);
		cv.put(DBOpenHelper.LUCKY_COLOR, DayAstroRecord.LuckyColor);
		cv.put(DBOpenHelper.LUCKY_NUMBER, DayAstroRecord.luckyNumber);
		cv.put(DBOpenHelper.Q_FRIEND, DayAstroRecord.QFriend);
		cv.put(DBOpenHelper.DESCRIPTION, DayAstroRecord.description);
		cv.put(DBOpenHelper.DAY, DayAstroRecord.day);
		
		cv.put(DBOpenHelper.YEAR, String.valueOf(DayAstroRecord.year));
		cv.put(DBOpenHelper.MONTH, String.valueOf(DayAstroRecord.month));
		cv.put(DBOpenHelper.MONTH_DAY, String.valueOf(DayAstroRecord.monthDay));
		cv.put(DBOpenHelper.ASTRO_PARA, DayAstroRecord.astroName);
		
		db.beginTransaction();
		
		try {

			id = db.insert(DBOpenHelper.DAY_ASTRO_TABLE_NAME, DBOpenHelper.CONTENT,
					cv);
			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(DBOpenHelper.DB_NAME,"DBOperations------create WeekAstroRecord error", e);
		} finally {
			db.endTransaction();
			close(db);
		}

		Log.i(DBOpenHelper.DB_NAME,"DBOperation------createWeekAstroRecord end!");
		return id;
	}

	public synchronized void deleteDayAstroRecord(Context context, GNAstroUtils.DayAstroInfo dayAstroRecord) {
		Log.i(DBOpenHelper.DB_NAME,"DBOperations------delete WeekAstroRecord start!");
		Log.d(DBOpenHelper.DB_NAME,"DBOperations------delete," + dayAstroRecord.toString());

		SQLiteDatabase db = getDatabase(context);
		if(db == null){
			Log.e(DBOpenHelper.DB_NAME,"DBOperation------db == null, deleteWeekAstroRecord fail, return!");
			return;
		}

		db.beginTransaction();
		try {
			
			Time date = new Time();
			date.set(dayAstroRecord.monthDay, dayAstroRecord.month-1, dayAstroRecord.year);
			String stringDate = date.toString().substring(0, 8);
			
			db.delete(DBOpenHelper.DAY_ASTRO_TABLE_NAME, 
					DBOpenHelper.ASTRO_PARA + " = ? "+" and "+ 
					DBOpenHelper.DAY+ " <= ? ", 
					new String[] { dayAstroRecord.astroName,stringDate });
		
			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(DBOpenHelper.DB_NAME,"DBOperations------delete WeekAstroRecord error: " + e.toString());
		} finally {
			db.endTransaction();
			close(db);
		}
		Log.i(DBOpenHelper.DB_NAME,"DBOperations------delete WeekAstroRecord end!");
	}

	public synchronized void updateDay(Context context, GNAstroUtils.DayAstroInfo dayAstroRecord) {
		Log.i(DBOpenHelper.DB_NAME,"DBOperations------update weekAstroRecord start!");
		Log.d(DBOpenHelper.DB_NAME,"DBOperations------update: " + dayAstroRecord.toString());


		SQLiteDatabase db = getDatabase(context);
		if(db == null){
			Log.e(DBOpenHelper.DB_NAME,"DBOperation------db == null, updateWeekAstroRecord fail, return!");
			return;
		}

		ContentValues cv = new ContentValues();
//		cv.put(DBOpenHelper.ASTRO, weekAstroRecord.astro);
//		cv.put(DBOpenHelper.WEEKBEGIN, weekAstroRecord.weekBegin);
//		cv.put(DBOpenHelper.WEEKEND, weekAstroRecord.weekEnd);
//		cv.put(DBOpenHelper.FORTUNETYPE, weekAstroRecord.fortuneType[0]);
//		cv.put(DBOpenHelper.FORTUNECONTENT, weekAstroRecord.fortuneContent[0]);
		
		String whereClause = 
		DBOpenHelper.ASTRO_PARA + " = ? "+" and "+ 
		DBOpenHelper.DAY+ " = ? ";
		
		String[] whereArgs =new String[] { dayAstroRecord.astroName,dayAstroRecord.day};
		

		try {
			db.beginTransaction();
			db.update(DBOpenHelper.WEEK_ASTRO_TABLE_NAME, cv, whereClause, whereArgs);
			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(DBOpenHelper.DB_NAME,"DBOperations------update error: " + e.toString());
		} finally {
			db.endTransaction();
			close(db);
		}
		Log.i(DBOpenHelper.DB_NAME,"DBOperations------update weekAstroRecord end!");
	}

	
	
	
	
	
	
	
	

	


	public synchronized GNAstroUtils.DayAstroInfo queryDayAstro(Context context, int year, int month, int monthDay, String astroName) {
		Log.i(DBOpenHelper.DB_NAME,"DBOperations------query one weekAstroRecord start!");
		Log.d(DBOpenHelper.DB_NAME,"DBOperations------queryOneNote," + astroName);

		DayAstroInfo response = new DayAstroInfo();
		

		SQLiteDatabase db = getDatabase(context);
		if(db == null){
			Log.e(DBOpenHelper.DB_NAME,"DBOperation------db == null, queryOneWeekAstro fail!");
			return null;
		}

		Time date = new Time();
		date.set(monthDay, month-1, year);
		String stringDate = date.toString().substring(0, 8);
		
		String[] whereArgs =new String[] { astroName,stringDate};

		Log.e("lsying1","test        "+
				DBOpenHelper.ASTRO_PARA + " = ? "+" and "+ 
				DBOpenHelper.DAY+ " = ? "
        );
		Log.e("lsying2","test        "+
				stringDate 
        );
		
		String[] columns = new String[] { DBOpenHelper.NOTE_ALL };
		Cursor c = null;
		try {
			c = db.query(DBOpenHelper.DAY_ASTRO_TABLE_NAME, columns, 
					DBOpenHelper.ASTRO_PARA + " = ? "+" and "+ 
					DBOpenHelper.DAY+ " = ? ",					
					whereArgs, null, null, null);
		} catch (Exception e) {
			Log.e(DBOpenHelper.DB_NAME,"DBOperations------queryOneWeekAstro error :" + e);
			close(c);
		}

		if (c != null && c.moveToFirst() != false) {
			
			
			response.selfAstro = c.getString(c.getColumnIndex(DBOpenHelper.SELF_ASTRO));
			response.day = c.getString(c.getColumnIndex(DBOpenHelper.DAY));
			response.indexComprehension = c.getInt(c.getColumnIndex(DBOpenHelper.INDEX_COMPREHENSION));
			response.indexLove = c.getInt(c.getColumnIndex(DBOpenHelper.INDEX_LOVE));
			response.indexCareer = c.getInt(c.getColumnIndex(DBOpenHelper.INDEX_CAREER));
			response.indexFinance = c.getInt(c.getColumnIndex(DBOpenHelper.INDEX_FINANCE));
			response.indexHealth = c.getInt(c.getColumnIndex(DBOpenHelper.INDEX_HEALTH));
			response.LuckyColor = c.getString(c.getColumnIndex(DBOpenHelper.LUCKY_COLOR));
			response.luckyNumber = c.getString(c.getColumnIndex(DBOpenHelper.LUCKY_NUMBER));
			response.QFriend = c.getString(c.getColumnIndex(DBOpenHelper.Q_FRIEND));
			response.description = c.getString(c.getColumnIndex(DBOpenHelper.DESCRIPTION));
			
			
			
			
		}
		else
		{
			return null; 
		}
		this.close(c);
		this.close(db);

		Log.i(DBOpenHelper.DB_NAME,"DBOperations------query one weekAstroRecord end!");
		return response;
	}

	
	
	
}