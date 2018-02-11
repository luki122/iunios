package com.aurora.note.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.aurora.note.bean.LabelResult;
import com.aurora.note.util.FileLog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jason
 * 
 *         对标签的数据库操作
 * 
 */
public class LabelAdapter extends DBAdapter {

	public static final String TAG = "LabelAdapter";
	public static final String TABLE_NAME = "tbl_label";// 数据库表名
	public static final String ID = "_id"; // 表属性ID
	public static final String CONTENT = "content"; // 标签内容
	public static final String UPDATE_TIME = "update_time"; // 更新时间
	public static final String IS_ENCRYPTED = "is_encrypted"; // 是否加密

	/**未加密*/
	private static final int NOT_ENCRYPTED = 0;

	/**加密*/
	private static final int ENCRYPTED = 1;

	private DBOpenHelper mDBOpenHelper;
	private SQLiteDatabase mDb;
	private Context mContext;

	public LabelAdapter(Context context) {
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
	public void insert(List<LabelResult> result) {
		mDb.beginTransaction();

		try {
			for (LabelResult downloadResult : result) {
				insert(downloadResult);
			}
			mDb.setTransactionSuccessful();
		} catch (Exception e) {
			FileLog.e(TAG, e.toString());
		} finally {
			mDb.endTransaction();
		}
	}

	/**
	 * 保存新建标签(单条)
	 * 
	 * @param NoteResult
	 *            result
	 * 
	 */
	public void insert(LabelResult result) {
		ContentValues value = new ContentValues();
		value.put(CONTENT, result.getContent());
		value.put(IS_ENCRYPTED, result.isEncrypted() ? ENCRYPTED: NOT_ENCRYPTED);
		value.put(UPDATE_TIME, result.getUpdate_time());
		mDb.insert(TABLE_NAME, null, value);
	}

	/**
	 * 
	 * 查询全部数据
	 * 
	 * @return
	 */

	public ArrayList<LabelResult> queryAllData() {
		String selection = null;

		/* selection="(" + DRAFT_FID + "='" + fid+"')"; */
		Cursor result = mDb.query(TABLE_NAME, new String[] { ID, IS_ENCRYPTED, CONTENT, UPDATE_TIME }, selection, null, null,
				null, UPDATE_TIME + " asc");
		ArrayList<LabelResult> note = convertToNote(result);
		if (result != null && !result.isClosed()) {
			result.close();
			result = null;
		}
		return note;
	}
	/**
	 * 
	 * 通过名字查询ID
	 * 
	 * @return
	 */

	public String queryIDByName(String name) {
		String selection = null;

		selection="(" + CONTENT + "='" + name+"')"; 
		Cursor cursor = mDb.query(TABLE_NAME, new String[] { ID }, selection, null, null,
				null, UPDATE_TIME + " desc");
		String  note_id = null;
		if (cursor.moveToFirst()) {
			note_id = cursor.getString(cursor
					.getColumnIndex(ID));
		}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		return note_id;
	}
	
	/**
	 * 
	 * 通过ID查询名字
	 * 
	 * @return
	 */

	public String queryNameById(String id) {
		String selection = null;

		selection="(" + ID + "='" + id+"')"; 
		Cursor cursor = mDb.query(TABLE_NAME, new String[] { CONTENT }, selection, null, null,
				null, UPDATE_TIME + " desc");
		String  note_name = null;
		if (cursor.moveToFirst()) {
			note_name = cursor.getString(cursor
					.getColumnIndex(CONTENT));
		}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		return note_name;
	}
	/**
	 * 
	 * ConvertToRentalCar(Cursor cursor)是私有函数，
	 * 
	 * 作用是将查询结果转换为用来存储数据自定义的LabelResult类对象
	 * 
	 */
	private ArrayList<LabelResult> convertToNote(Cursor cursor) {
		int resultCounts = cursor.getCount();
		if (resultCounts == 0 || !cursor.moveToFirst()) {
			return null;
		}
		ArrayList<LabelResult> rentalCar = new ArrayList<LabelResult>();
		// Log.i(TAG, "ChatMessage len:" + chatMessage.length);
		for (int i = 0; i < resultCounts; i++) {
			LabelResult m_result = new LabelResult();
			m_result.setId(cursor.getInt(0));

			m_result.setContent(cursor.getString(cursor
					.getColumnIndex(CONTENT)));

			m_result.setUpdate_time(cursor.getLong(cursor
					.getColumnIndex(UPDATE_TIME)));

			m_result.setEncrypted(cursor.getInt(cursor.getColumnIndex(IS_ENCRYPTED)) == ENCRYPTED);

			rentalCar.add(m_result);
			cursor.moveToNext();
		}
		return rentalCar;
	}

	// 更新标签信息根据id号
	public int updateNoteByID(LabelResult note,String id) {
		ContentValues values = new ContentValues();
		values.put(CONTENT, note.getContent());
		values.put(IS_ENCRYPTED, note.isEncrypted() ? ENCRYPTED: NOT_ENCRYPTED);
		
		values.put(UPDATE_TIME, note.getUpdate_time());
		
		int rowNum = mDb.update(TABLE_NAME, values, ID + "=?",
				new String[] { id});
		return rowNum;
	}
	
	// 更新标签信息根据标签名字
	public int updateNoteByName(LabelResult note,String name) {
		ContentValues values = new ContentValues();
		values.put(CONTENT, note.getContent());
		values.put(IS_ENCRYPTED, note.isEncrypted() ? ENCRYPTED: NOT_ENCRYPTED);
		
		values.put(UPDATE_TIME, note.getUpdate_time());
		
		int rowNum = mDb.update(TABLE_NAME, values, CONTENT + "=?",
				new String[] { name });
		return rowNum;
	}

	
	// 根据id删除此条数据
	public int deleteDataById(String id) {
		return mDb.delete(TABLE_NAME, ID + "=?", new String[] { id });
	}
	
	// 根据标签名字删除标签
	public int deleteDataByName(String name) {
		return mDb.delete(TABLE_NAME, CONTENT + "=?", new String[] { name });
	}

}
