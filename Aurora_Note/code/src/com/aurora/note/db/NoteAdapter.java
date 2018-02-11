package com.aurora.note.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.aurora.note.bean.NoteResult;
import com.aurora.note.util.FileLog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jason
 * 
 *         对应备忘录的数据库操作
 * 
 */
public class NoteAdapter extends DBAdapter {

	public static final String TAG = "NoteAdapter";
	public static final String TABLE_NAME = "tbl_note";// 数据库表名

	public static final String ID = "_id"; // 表属性ID
	public static final String IS_PRESET = "is_preset"; // 是否为预置备忘录
	public static final String UUID = "uuid"; // UUID
	public static final String CONTENT = "content"; // 备忘录内容
	public static final String CHARACTER = "character"; // 备忘录内容
	public static final String IMAGE_COUNT = "image_count"; // 图片数量
	public static final String VIDEO_COUNT = "video_count"; // 视频数量
	public static final String SOUND_COUNT = "sound_count"; // 音频数量
	public static final String LABEL1 = "label1"; // 标签1内容
	public static final String LABEL2 = "label2"; // 标签2内容
	public static final String IS_WARN = "is_warn"; // 是否提醒
	public static final String WARN_TIME = "warn_time"; // 提醒时间
	public static final String CREATE_TIME = "create_time"; // 创建时间
	public static final String UPDATE_TIME = "update_time"; // 更新时间
	public static final String BACKGROUND_PATH = "background_path"; // 背景图路径

	public static final String[] NOTE_COLUMNS = new String[] {
		ID,
		BACKGROUND_PATH,
		IS_PRESET,
		UUID,
		CONTENT,
		CHARACTER,
		IMAGE_COUNT,
		VIDEO_COUNT,
		SOUND_COUNT,
		LABEL1,
		LABEL2,
		IS_WARN,
		WARN_TIME,
		CREATE_TIME,
		UPDATE_TIME
	};

	private DBOpenHelper mDBOpenHelper;
	private SQLiteDatabase mDb;
	private Context mContext;

	public NoteAdapter(Context context) {
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
	public void insert(List<NoteResult> result) {
		mDb.beginTransaction();

		try {
			for (NoteResult downloadResult : result) {
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
	 * 保存新建备忘录(单条)
	 * 
	 * @param NoteResult
	 *            result
	 * 
	 */
	public long insert(NoteResult result) {
		ContentValues value = new ContentValues();
		value.put(IS_PRESET, result.getIs_preset());
		value.put(BACKGROUND_PATH, result.getBackgroundPath());
		value.put(UUID, result.getUuid());

		value.put(CONTENT, result.getContent());
		value.put(CHARACTER, result.getCharacter());
		value.put(IMAGE_COUNT, result.getImage_count());
		value.put(VIDEO_COUNT, result.getVideo_count());
		value.put(SOUND_COUNT, result.getSound_count());
		value.put(LABEL1, result.getLabel1());
		value.put(LABEL2, result.getLabel2());

		value.put(IS_WARN, result.getIs_warn());
		value.put(WARN_TIME, result.getWarn_time());
		value.put(CREATE_TIME, System.currentTimeMillis());
		value.put(UPDATE_TIME, result.getUpdate_time());
		return mDb.insert(TABLE_NAME, null, value);
	}

	/**
	 * 
	 * 查询全部数据
	 * 
	 * @return
	 */
	public ArrayList<NoteResult> queryAllData() {
		String selection = null;

		/* selection="(" + DRAFT_FID + "='" + fid+"')"; */
		Cursor result = mDb.query(TABLE_NAME, NOTE_COLUMNS,
				selection, null, null, null, UPDATE_TIME + " desc");
		ArrayList<NoteResult> note = convertToNote(result);
		if (result != null && !result.isClosed()) {
			result.close();
			result = null;
		}
		return note;
	}

	/**
	 * 
	 * 查询指定行数数据
	 * 
	 * @return
	 */
	public ArrayList<NoteResult> queryDataByLine(int row, int count) {
		String selection = null;

		String limitStr = " limit " + count + " OFFSET " +  (row - 1) * count ;

		Cursor result = mDb.query(TABLE_NAME, NOTE_COLUMNS,
				selection, null, null, null, UPDATE_TIME + " desc" + limitStr);

		ArrayList<NoteResult> note = convertToNote(result);
		if (result != null && !result.isClosed()) {
			result.close();
			result = null;
		}
		return note;
	}

	/**
	 * 
	 * 查询某标签下指定行数数据
	 * 
	 * @return
	 */
	public ArrayList<NoteResult> queryDataOfLabelByLine(String labelId,
			int row, int count) {
		String selection = null;

		selection = "(" + LABEL1 + " = '" + labelId + "' or " + LABEL2
				+ " = '" + labelId + "')";

		String limitStr = " limit " + count + " OFFSET " + (row - 1) * count;

		Cursor result = mDb.query(TABLE_NAME, NOTE_COLUMNS,
				selection, null, null, null, UPDATE_TIME + " desc" + limitStr);

		ArrayList<NoteResult> note = convertToNote(result);
		if (result != null && !result.isClosed()) {
			result.close();
			result = null;
		}
		return note;
	}

	/**
	 * 
	 * 查询某标签下指定行数数据
	 * 
	 * @return
	 */
	public NoteResult queryDataByIndex(String labelId, int row, int count) {
		String selection = null;
		selection = "(" + LABEL1 + " = '" + labelId + "' or " + LABEL2
				+ " = '" + labelId + "')";

		String limitStr = " limit " + 1 + " OFFSET " + (row * count - 1);

		Cursor cursor = mDb.query(TABLE_NAME, NOTE_COLUMNS,
				selection, null, null, null, UPDATE_TIME + " desc" + limitStr);

		NoteResult m_result = null;
		if (cursor.moveToFirst()) {
			m_result = new NoteResult();
			m_result.setId(cursor.getInt(0));
			m_result.setIs_preset(cursor.getInt(cursor.getColumnIndex(IS_PRESET)));
			m_result.setBackgroundPath(cursor.getString(cursor.getColumnIndex(BACKGROUND_PATH)));
			m_result.setUuid(cursor.getString(cursor.getColumnIndex(UUID)));

			m_result.setContent(cursor.getString(cursor.getColumnIndex(CONTENT)));
			m_result.setCharacter(cursor.getString(cursor.getColumnIndex(CHARACTER)));
			m_result.setImage_count(cursor.getInt(cursor.getColumnIndex(IMAGE_COUNT)));
			m_result.setVideo_count(cursor.getInt(cursor.getColumnIndex(VIDEO_COUNT)));
			m_result.setSound_count(cursor.getInt(cursor.getColumnIndex(SOUND_COUNT)));
			m_result.setLabel1(cursor.getString(cursor.getColumnIndex(LABEL1)));
			m_result.setLabel2(cursor.getString(cursor.getColumnIndex(LABEL2)));

			m_result.setIs_warn(cursor.getInt(cursor.getColumnIndex(IS_WARN)));
			m_result.setWarn_time(cursor.getLong(cursor.getColumnIndex(WARN_TIME)));
			m_result.setCreate_time(cursor.getLong(cursor.getColumnIndex(CREATE_TIME)));
			m_result.setUpdate_time(cursor.getLong(cursor.getColumnIndex(UPDATE_TIME)));
		}

		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
			cursor = null;
		}
		return m_result;
	}

	/**
	 * 
	 * 查询指定行数数据
	 * 
	 * @return
	 */
	public NoteResult queryDataByIndex(int row, int count) {
		String selection = null;

		String limitStr = " limit " + 1 + " OFFSET " + (row * count - 1);

		Cursor cursor = mDb.query(TABLE_NAME, NOTE_COLUMNS,
				selection, null, null, null, UPDATE_TIME + " desc" + limitStr);

		NoteResult m_result = null;
		if (cursor.moveToFirst()) {
			m_result = new NoteResult();
			m_result.setId(cursor.getInt(0));
			m_result.setIs_preset(cursor.getInt(cursor.getColumnIndex(IS_PRESET)));
			m_result.setBackgroundPath(cursor.getString(cursor.getColumnIndex(BACKGROUND_PATH)));
			m_result.setUuid(cursor.getString(cursor.getColumnIndex(UUID)));

			m_result.setContent(cursor.getString(cursor.getColumnIndex(CONTENT)));
			m_result.setCharacter(cursor.getString(cursor.getColumnIndex(CHARACTER)));
			m_result.setImage_count(cursor.getInt(cursor.getColumnIndex(IMAGE_COUNT)));
			m_result.setVideo_count(cursor.getInt(cursor.getColumnIndex(VIDEO_COUNT)));
			m_result.setSound_count(cursor.getInt(cursor.getColumnIndex(SOUND_COUNT)));
			m_result.setLabel1(cursor.getString(cursor.getColumnIndex(LABEL1)));
			m_result.setLabel2(cursor.getString(cursor.getColumnIndex(LABEL2)));

			m_result.setIs_warn(cursor.getInt(cursor.getColumnIndex(IS_WARN)));
			m_result.setWarn_time(cursor.getLong(cursor.getColumnIndex(WARN_TIME)));
			m_result.setCreate_time(cursor.getLong(cursor.getColumnIndex(CREATE_TIME)));
			m_result.setUpdate_time(cursor.getLong(cursor.getColumnIndex(UPDATE_TIME)));
		}

		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
			cursor = null;
		}
		return m_result;
	}
	
	public long getCountByLabel(String labelId) {
		String selection = "";

		selection = "(" + LABEL1 + " = '" + labelId + "' or " + LABEL2
				+ " = '" + labelId + "')";

		String sql = "SELECT count(*) FROM " + TABLE_NAME + " WHERE "
				+ selection + "";
		// String sql = "SELECT count(*) FROM "+TABLE_NAME+"";
		Cursor cursor = mDb.rawQuery(sql, null);

		cursor.moveToFirst();
		long result = cursor.getLong(0);
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
			cursor = null;
		}
		return result;
	}

	public long getCount() {
		String sql = "SELECT count(*) FROM " + TABLE_NAME;
		// String sql = "SELECT count(*) FROM "+TABLE_NAME+"";
		Cursor cursor = mDb.rawQuery(sql, null);

		cursor.moveToFirst();
		long result = cursor.getLong(0);
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
			cursor = null;
		}
		return result;
	}

	/**
	 * 
	 * 查询数据通过ID号
	 * 
	 * @return NoteResult
	 */
	public NoteResult queryDataByID(int id) {
		String selection = null;

		selection = "(" + ID + "='" + id + "')";
		Cursor cursor = mDb.query(TABLE_NAME, NOTE_COLUMNS,
				selection, null, null, null, UPDATE_TIME + " desc");

		NoteResult m_result = null;
		if (cursor.moveToFirst()) {
			m_result = new NoteResult();
			m_result.setId(cursor.getInt(0));
			m_result.setIs_preset(cursor.getInt(cursor.getColumnIndex(IS_PRESET)));
			m_result.setBackgroundPath(cursor.getString(cursor.getColumnIndex(BACKGROUND_PATH)));
			m_result.setUuid(cursor.getString(cursor.getColumnIndex(UUID)));

			m_result.setContent(cursor.getString(cursor.getColumnIndex(CONTENT)));
			m_result.setCharacter(cursor.getString(cursor.getColumnIndex(CHARACTER)));
			m_result.setImage_count(cursor.getInt(cursor.getColumnIndex(IMAGE_COUNT)));
			m_result.setVideo_count(cursor.getInt(cursor.getColumnIndex(VIDEO_COUNT)));
			m_result.setSound_count(cursor.getInt(cursor.getColumnIndex(SOUND_COUNT)));
			m_result.setLabel1(cursor.getString(cursor.getColumnIndex(LABEL1)));
			m_result.setLabel2(cursor.getString(cursor.getColumnIndex(LABEL2)));

			m_result.setIs_warn(cursor.getInt(cursor.getColumnIndex(IS_WARN)));
			m_result.setWarn_time(cursor.getLong(cursor.getColumnIndex(WARN_TIME)));
			m_result.setCreate_time(cursor.getLong(cursor.getColumnIndex(CREATE_TIME)));
			m_result.setUpdate_time(cursor.getLong(cursor.getColumnIndex(UPDATE_TIME)));
		}

		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
			cursor = null;
		}
		return m_result;
	}

	/**
	 * 
	 * 根据输入的关键词查询内容包含关键词的记录
	 * 
	 * @return NoteResult
	 */
	public ArrayList<NoteResult> queryDataByKey(String keyWord) {
		String selection = null;
		keyWord = keyWord.replace("'", "''");
		selection = "(" + CHARACTER + " LIKE '%" + keyWord + "%')";
		Cursor cursor = mDb.query(TABLE_NAME, NOTE_COLUMNS,
				selection, null, null, null, UPDATE_TIME + " desc");
		ArrayList<NoteResult> notes = convertToNote(cursor);
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
			cursor = null;
		}
		return notes;
	}

	/**
	 * 
	 * ConvertToRentalCar(Cursor cursor)是私有函数，
	 * 
	 * 作用是将查询结果转换为用来存储数据自定义的NoteResult类对象
	 * 
	 */
	private ArrayList<NoteResult> convertToNote(Cursor cursor) {
		int resultCounts = cursor.getCount();
		if (resultCounts == 0 || !cursor.moveToFirst()) {
			return null;
		}
		ArrayList<NoteResult> rentalCar = new ArrayList<NoteResult>();
		// Log.i(TAG, "ChatMessage len:" + chatMessage.length);
		for (int i = 0; i < resultCounts; i++) {
			NoteResult m_result = new NoteResult();
			m_result.setId(cursor.getInt(0));
			m_result.setIs_preset(cursor.getInt(cursor.getColumnIndex(IS_PRESET)));
			m_result.setBackgroundPath(cursor.getString(cursor.getColumnIndex(BACKGROUND_PATH)));
			m_result.setUuid(cursor.getString(cursor.getColumnIndex(UUID)));

			m_result.setContent(cursor.getString(cursor.getColumnIndex(CONTENT)));
			m_result.setCharacter(cursor.getString(cursor.getColumnIndex(CHARACTER)));
			m_result.setImage_count(cursor.getInt(cursor.getColumnIndex(IMAGE_COUNT)));
			m_result.setVideo_count(cursor.getInt(cursor.getColumnIndex(VIDEO_COUNT)));
			m_result.setSound_count(cursor.getInt(cursor.getColumnIndex(SOUND_COUNT)));
			m_result.setLabel1(cursor.getString(cursor.getColumnIndex(LABEL1)));
			m_result.setLabel2(cursor.getString(cursor.getColumnIndex(LABEL2)));

			m_result.setIs_warn(cursor.getInt(cursor.getColumnIndex(IS_WARN)));
			m_result.setWarn_time(cursor.getLong(cursor.getColumnIndex(WARN_TIME)));
			m_result.setCreate_time(cursor.getLong(cursor.getColumnIndex(CREATE_TIME)));
			m_result.setUpdate_time(cursor.getLong(cursor.getColumnIndex(UPDATE_TIME)));

			rentalCar.add(m_result);
			cursor.moveToNext();
		}
		return rentalCar;
	}

	// 更新备忘录信息根据id号
	public int updateNoteByID(NoteResult note, String id) {
		ContentValues values = new ContentValues();
		values.put(IS_PRESET, note.getIs_preset());
		values.put(BACKGROUND_PATH, note.getBackgroundPath());
		values.put(CONTENT, note.getContent());
		values.put(CHARACTER, note.getCharacter());
		values.put(IMAGE_COUNT, note.getImage_count());
		values.put(VIDEO_COUNT, note.getVideo_count());
		values.put(SOUND_COUNT, note.getSound_count());
		values.put(LABEL1, note.getLabel1());
		values.put(LABEL2, note.getLabel2());
		values.put(IS_WARN, note.getIs_warn());
		values.put(WARN_TIME, note.getWarn_time());
		values.put(UPDATE_TIME, note.getUpdate_time());

		int rowNum = mDb.update(TABLE_NAME, values, ID + "=?", new String[] { id });
		return rowNum;
	}

	// 更新备忘录信息根据标签信息
	public int updateNoteByLabel(NoteResult note, String label1, String label2) {
		ContentValues values = new ContentValues();
		values.put(CONTENT, note.getContent());
		values.put(CHARACTER, note.getCharacter());
		values.put(IMAGE_COUNT, note.getImage_count());
		values.put(VIDEO_COUNT, note.getVideo_count());
		values.put(SOUND_COUNT, note.getSound_count());
		values.put(LABEL1, note.getLabel1());
		values.put(LABEL2, note.getLabel2());
		values.put(IS_WARN, note.getIs_warn());
		values.put(WARN_TIME, note.getWarn_time());
		values.put(CREATE_TIME, note.getCreate_time());
		values.put(UPDATE_TIME, note.getUpdate_time());

		int rowNum = mDb.update(TABLE_NAME, values, LABEL1 + "=?" + " or "
				+ LABEL2 + "=?", new String[] { label1, label2 });
		return rowNum;
	}

	// 根据id删除此条数据
	public int deleteDataById(String id) {
		return mDb.delete(TABLE_NAME, ID + "=?", new String[] { id });
	}

	public Cursor queryDataForAlarm() {
		long currentMillis = System.currentTimeMillis();
		Cursor cursor = mDb.query(TABLE_NAME, new String[] { ID, WARN_TIME },
				IS_WARN + "=" + 1 + " AND " + WARN_TIME + ">=" + currentMillis,
				null, null, null, WARN_TIME + " ASC");
		return cursor;
	}

	public ArrayList<NoteResult> queryDataForBackup() {
		Cursor result = mDb.query(TABLE_NAME, NOTE_COLUMNS,
				IS_PRESET + "=0 OR " + IS_PRESET + "=3", null, null, null, UPDATE_TIME + " desc");
		ArrayList<NoteResult> note = convertToNote(result);
		if (result != null && !result.isClosed()) {
			result.close();
			result = null;
		}
		return note;
	}

	public ArrayList<String> queryNoteUUID() {
		Cursor cursor = mDb.query(TABLE_NAME, new String[] { UUID, UPDATE_TIME },
				null, null, null, null, UPDATE_TIME + " desc");

		ArrayList<String> uuidList = new ArrayList<String>();
		cursor.moveToPosition(-1);
		while (cursor.moveToNext()) {
			String uuid = cursor.getString(0);
			uuidList.add(uuid);
		}

		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
			cursor = null;
		}

		return uuidList;
	}

}
