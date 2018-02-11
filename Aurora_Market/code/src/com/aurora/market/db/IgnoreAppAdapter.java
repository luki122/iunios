package com.aurora.market.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.aurora.datauiapi.data.bean.iconItem;
import com.aurora.datauiapi.data.bean.upappListtem;
import com.aurora.market.util.FileLog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jason
 * 
 *         对忽略应用更新的数据库操作
 * 
 */
public class IgnoreAppAdapter extends DBAdapter {

	public static final String TAG = "IgnoreAdapter";
	public static final String TABLE_NAME = "tbl_ignore";// 数据库表名
	public static final String ID = "_id"; // 表属性ID
	public static final String TITLE = "title"; // 应用名称
	public static final String PACKAGENAME = "packageName"; // 应用的包名
	public static final String APPSIZE = "appSize"; // 服务器端应用的大小
	public static final String CLIENTAPPSIZE = "clientAppSize"; // 客户端应用的大小
	public static final String CLIENTVERSIONNAME = "clientVersionName"; // 客户端应用的版本名称
	public static final String VERSIONNAME = "versionName"; // 版本名
	public static final String CLIENTVERSIONCODE = "clientVersionCode"; // 客户端版本号
	public static final String APPSIZESTR = "appSizeStr"; // 应用的大小显示
	public static final String VERSIONCODE = "versionCode"; // 版本号
	public static final String DOWNLOADURL = "downloadURL"; // 应用下载地址
	public static final String ICONS = "icons"; // 应用的一组图标地址
	public static final String UPDATE_TIME = "update_time"; // 更新时间
	private DBOpenHelper mDBOpenHelper;
	private SQLiteDatabase mDb;
	private Context mContext;

	public IgnoreAppAdapter(Context context) {
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

	/**
	 * 保存备忘录信息(多条)
	 * 
	 * @param List
	 *            <DownloadResult> result
	 * 
	 */
	public void insert(List<upappListtem> result) {
		mDb.beginTransaction();

		try {
			for (upappListtem upResult : result) {
				insert(upResult);
			}
			mDb.setTransactionSuccessful();
		} catch (Exception e) {
			FileLog.e(TAG, e.toString());
		} finally {
			mDb.endTransaction();
		}
	}

	/**
	 * 保存忽略应用(单条)
	 * 
	 * @param NoteResult
	 *            result
	 * 
	 */
	public void insert(upappListtem result) {
		ContentValues value = new ContentValues();
		value.put(ID, result.getId());
		value.put(TITLE, result.getTitle());
		value.put(PACKAGENAME, result.getPackageName());
		value.put(APPSIZE, result.getAppSize());
		value.put(CLIENTAPPSIZE, result.getClientAppSize());
		value.put(CLIENTVERSIONNAME, result.getClientVersionName());
		value.put(VERSIONNAME, result.getVersionName());
		value.put(CLIENTVERSIONCODE, result.getClientVersionCode());
		value.put(APPSIZESTR, result.getAppSizeStr());
		value.put(VERSIONCODE, result.getVersionCode());
		value.put(DOWNLOADURL, result.getDownloadURL());
		value.put(ICONS, result.getIcons().getPx256());

		value.put(UPDATE_TIME, System.currentTimeMillis());
		mDb.insert(TABLE_NAME, null, value);
	}

	/**
	 * 
	 * 查询全部数据
	 * 
	 * @return
	 */

	public ArrayList<upappListtem> queryAllData() {
		String selection = null;

		/* selection="(" + DRAFT_FID + "='" + fid+"')"; */
		Cursor result = mDb.query(TABLE_NAME, new String[] { ID, TITLE,
				PACKAGENAME, APPSIZE, CLIENTAPPSIZE, CLIENTVERSIONNAME,
				VERSIONNAME, CLIENTVERSIONCODE, APPSIZESTR, VERSIONCODE,
				DOWNLOADURL, ICONS, UPDATE_TIME }, selection, null, null, null,
				UPDATE_TIME + " desc");
		ArrayList<upappListtem> note = ConvertToNote(result);
		if (result != null && !result.isClosed()) {
			result.close();
			result = null;
		}
		return note;
	}

	
	public ArrayList<String> queryAllPackageData() {
		String selection = null;

		/* selection="(" + DRAFT_FID + "='" + fid+"')"; */
		Cursor result = mDb.query(TABLE_NAME, new String[] { ID, TITLE,
				PACKAGENAME }, selection, null, null, null,
				UPDATE_TIME + " desc");
		ArrayList<String> upPac = ConvertToPac(result);
		if (result != null && !result.isClosed()) {
			result.close();
			result = null;
		}
		return upPac;
	}

	private ArrayList<String> ConvertToPac(Cursor cursor) {
		int resultCounts = cursor.getCount();
		if (resultCounts == 0 || !cursor.moveToFirst()) {
			return null;
		}
		ArrayList<String> rentalCar = new ArrayList<String>();
		// Log.i(TAG, "ChatMessage len:" + chatMessage.length);
		for (int i = 0; i < resultCounts; i++) {
			rentalCar.add(cursor.getString(cursor
					.getColumnIndex(PACKAGENAME)));

			cursor.moveToNext();
		}
		return rentalCar;
	}
	/**
	 * 
	 * ConvertToRentalCar(Cursor cursor)是私有函数，
	 * 
	 * 作用是将查询结果转换为用来存储数据自定义的upappListtem类对象
	 * 
	 */
	private ArrayList<upappListtem> ConvertToNote(Cursor cursor) {
		int resultCounts = cursor.getCount();
		if (resultCounts == 0 || !cursor.moveToFirst()) {
			return null;
		}
		ArrayList<upappListtem> rentalCar = new ArrayList<upappListtem>();
		// Log.i(TAG, "ChatMessage len:" + chatMessage.length);
		for (int i = 0; i < resultCounts; i++) {
			upappListtem m_result = new upappListtem();
			m_result.setId(cursor.getInt(0));

			m_result.setTitle(cursor.getString(cursor.getColumnIndex(TITLE)));
			m_result.setPackageName(cursor.getString(cursor
					.getColumnIndex(PACKAGENAME)));

			m_result.setAppSize(cursor.getInt(cursor.getColumnIndex(APPSIZE)));

			m_result.setClientAppSize(cursor.getInt(cursor
					.getColumnIndex(CLIENTAPPSIZE)));

			m_result.setClientVersionName(cursor.getString(cursor
					.getColumnIndex(CLIENTVERSIONNAME)));
			m_result.setVersionName(cursor.getString(cursor
					.getColumnIndex(VERSIONNAME)));
			m_result.setClientVersionCode(cursor.getInt(cursor
					.getColumnIndex(CLIENTVERSIONCODE)));
			m_result.setAppSizeStr(cursor.getString(cursor
					.getColumnIndex(APPSIZESTR)));
			m_result.setVersionCode(cursor.getInt(cursor
					.getColumnIndex(VERSIONCODE)));
			m_result.setDownloadURL(cursor.getString(cursor
					.getColumnIndex(DOWNLOADURL)));
			iconItem icons = new iconItem();
			icons.setPx256(cursor.getString(cursor.getColumnIndex(ICONS)));
			m_result.setIcons(icons);
			rentalCar.add(m_result);
			cursor.moveToNext();
		}
		return rentalCar;
	}

	// 根据id删除此条数据
	public int deleteDataById(String packagename) {
		return mDb.delete(TABLE_NAME, PACKAGENAME + "=?",
				new String[] { packagename });
	}

}
