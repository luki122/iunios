package com.aurora.note.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

import com.aurora.note.bean.MarkInfo;
import com.aurora.note.bean.RecorderInfo;
import com.aurora.note.util.FileLog;

import java.util.ArrayList;
import java.util.List;

/**
 * 对备忘录录音表的操作
 * @author JimXia
 * 2014-7-25 下午1:53:56
 */
public class RecorderAdapter extends DBAdapter {

	public static final String TAG = "RecorderAdapter";
	public static final String TABLE_NAME = "tbl_recorder";// 数据库表名
	public static final String ID = "_id"; // 表属性ID
	public static final String PATH = "path"; // 录音路径
	public static final String NAME = "name"; // 录音文件名
	public static final String MARK = "mark"; // 录音标注
	public static final String CREATE_TIME = "createTime"; // 录音创建时间
	public static final String DURATION = "duration"; // 音频时长
	public static final String SAMPLERATE = "sampleRate"; // 采样率
	
	private DBOpenHelper mDBOpenHelper;
	private SQLiteDatabase mDb;
	private Context mContext;

	public RecorderAdapter(Context context) {
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
	 * 保存录音信息(多条)
	 * @param recorderInfos
	 */
	public void insert(List<RecorderInfo> recorderInfos) {
		mDb.beginTransaction();

		try {
			for (RecorderInfo recorderInfo : recorderInfos) {
				insertOrUpdate(recorderInfo);
			}
			mDb.setTransactionSuccessful();
		} catch (Exception e) {
			FileLog.e(TAG, e.toString());
		} finally {
			mDb.endTransaction();
		}
	}

	/**
	 * 保存录音信息(单条)
	 * @param recorderInfo
	 * @return
	 */
	public long insertOrUpdate(RecorderInfo recorderInfo) {
	    RecorderInfo existInfo = queryDataByName(recorderInfo.getName());
	    
	    ContentValues values = new ContentValues();
	    values.put(DURATION, recorderInfo.getDuration());
	    values.put(MARK, recorderInfo.getMarkInfo());
	    if (existInfo == null) {
	        values.put(PATH, recorderInfo.getPath());
	        values.put(NAME, recorderInfo.getName());
	        values.put(CREATE_TIME, System.currentTimeMillis());
	        values.put(SAMPLERATE, recorderInfo.getSampleRate());
	        return mDb.insert(TABLE_NAME, null, values);
	    } else {
	        return mDb.update(TABLE_NAME, values, ID + "=?", new String[] {String.valueOf(existInfo.getId())});
	    }
	}

	public RecorderInfo queryDataByName(String name) {
	    String selection = null;

        selection="(" + NAME + "='" + name + "')"; 
        Cursor cursor = mDb.query(TABLE_NAME, new String[] { ID, PATH, NAME, MARK, CREATE_TIME, DURATION, SAMPLERATE},
                selection, null, null, null, null);
        
        RecorderInfo info = convert2RecorderInfo(cursor);;
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        
        return info;
	}
	
	public RecorderInfo queryDataByPath(String path) {
        String selection = null;

        selection="(" + PATH + "='" + path + "')"; 
        Cursor cursor = mDb.query(TABLE_NAME, new String[] { ID, PATH, NAME, MARK, CREATE_TIME, DURATION, SAMPLERATE},
                selection, null, null, null, null);
        
        RecorderInfo info = convert2RecorderInfo(cursor);
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        
        return info;
    }
	
	private RecorderInfo convert2RecorderInfo(Cursor cursor) {
	    RecorderInfo info = null;
        if (cursor.moveToFirst()) {
            info = new RecorderInfo();
            info.setId(cursor.getInt(cursor.getColumnIndex(ID)));
            info.setPath(cursor.getString(cursor.getColumnIndex(PATH)));
            info.setName(cursor.getString(cursor.getColumnIndex(NAME)));
            String markInfo = cursor.getString(cursor.getColumnIndex(MARK));
            if (!TextUtils.isEmpty(markInfo)) {
                markInfo = markInfo.substring(1, markInfo.length() - 1);
                if (!TextUtils.isEmpty(markInfo)) {
                    String[] markArr = markInfo.split(",");
                    int index = 1;
                    ArrayList<MarkInfo> marks = new ArrayList<MarkInfo>();
                    for (String mark: markArr) {
                        marks.add(new MarkInfo(index, Long.parseLong(mark.trim())));
                        index ++;
                    }
                    info.setMarks(marks);
                }
            }
            info.setCreateTime(cursor.getLong(cursor.getColumnIndex(CREATE_TIME)));
            info.setDuration(cursor.getLong(cursor.getColumnIndex(DURATION)));
            info.setSampleRate(cursor.getInt(cursor.getColumnIndex(SAMPLERATE)));
        }
        
        return info;
	}
}