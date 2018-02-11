package com.android.gallery3d.xcloudalbum.uploaddownload;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.android.gallery3d.util.MyLog;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

public class FakeProgressDb extends ContentProvider {
	
	private Context mContext;
	private DatabaseHelper mDbHelper;
	
	private static final String DB_NAME = "fakeprogress.db";
	private static final int DB_VERSION = 1;
	
	private static final String TABLE_NAME_FAKE_PROGRESS = "fakeprogress";
	
	public static final String MSG_TABLE_COLUMN_ID = "_id";
	public static final String MSG_TABLE_COLUMN_TASK_ID = "_task_id";
	public static final String MSG_TABLE_COLUMN_TASK_STATE = "_task_state";
	public static final String MSG_TABLE_COLUMN_CURRENT_SIZE = "_current_size";
	public static final String MSG_TABLE_COLUMN_TOTAL_SIZE = "_total_size";
	
	private static final String DEFAULT_SORT_ORDER = MSG_TABLE_COLUMN_ID + " DESC";
	
	private static final String SCHEME = "content://";
	private static final String AUTHORITY = "com.android.gallery3d.FakeProgressDb";

	private static final String PATH_FAKE_PROGRESS = "fakeprogress";
	private static final String PATH_FAKE_PROGRESS_ID = "fakeprogress/#";
	
	public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/" + PATH_FAKE_PROGRESS);
	public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + "/" + PATH_FAKE_PROGRESS_ID);
	//public static final Uri CONTENT_DISTINCE_FROM_USER_URI_BASE = Uri.parse(SCHEME + AUTHORITY + "/" + PATH_MSG_DISTINCT_FROM_USER);
	
	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	
	private static final int URI_MATCH_CODE_FAKE_PROGRESS = 1;
	private static final int URI_MATCH_CODE_TASK_ID = 2;
	
	private static final String CONTENT_TYPE_FAKE_PROGRESS = "CONTENT_TYPE_FAKE_PROGRESS";
	private static final String CONTENT_TYPE_TASK_ID = "CONTENT_TYPE_TASK_ID";
	
	private static final HashMap<String, String> sMsgProjectionMap;

	//public final static SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	static {
		//sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		if(Build.VERSION.SDK_INT >= 18) {//andoid 4.3 and later
			sUriMatcher.addURI(AUTHORITY, "/" + PATH_FAKE_PROGRESS, URI_MATCH_CODE_FAKE_PROGRESS);
			sUriMatcher.addURI(AUTHORITY, "/" + PATH_FAKE_PROGRESS + "/#", URI_MATCH_CODE_TASK_ID);
		} else {
			sUriMatcher.addURI(AUTHORITY, PATH_FAKE_PROGRESS, URI_MATCH_CODE_FAKE_PROGRESS);
			sUriMatcher.addURI(AUTHORITY, "/" + PATH_FAKE_PROGRESS + "/#", URI_MATCH_CODE_TASK_ID);
		}
		
		sMsgProjectionMap = new HashMap<String, String>();
		sMsgProjectionMap.put(MSG_TABLE_COLUMN_ID, MSG_TABLE_COLUMN_ID);
		sMsgProjectionMap.put(MSG_TABLE_COLUMN_TASK_ID, MSG_TABLE_COLUMN_TASK_ID);
		sMsgProjectionMap.put(MSG_TABLE_COLUMN_TASK_STATE, MSG_TABLE_COLUMN_TASK_STATE);
		sMsgProjectionMap.put(MSG_TABLE_COLUMN_CURRENT_SIZE, MSG_TABLE_COLUMN_CURRENT_SIZE);
		sMsgProjectionMap.put(MSG_TABLE_COLUMN_TOTAL_SIZE, MSG_TABLE_COLUMN_TOTAL_SIZE);
	}
	
	public static class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(Context ctx) {
			// TODO Auto-generated constructor stub
			super(ctx, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			
			StringBuffer sb = new StringBuffer();
			sb.append("CREATE TABLE IF NOT EXISTS ");
			sb.append(TABLE_NAME_FAKE_PROGRESS);
			sb.append("( ");
			sb.append(MSG_TABLE_COLUMN_ID);
			sb.append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
			sb.append(MSG_TABLE_COLUMN_TASK_ID);
			sb.append(" LONG,");
			sb.append(MSG_TABLE_COLUMN_TASK_STATE);
			sb.append(" INTEGER,");
			sb.append(MSG_TABLE_COLUMN_CURRENT_SIZE);
			sb.append(" LONG,");
			sb.append(MSG_TABLE_COLUMN_TOTAL_SIZE);
			sb.append(" LONG");
			sb.append(");");
			db.execSQL(sb.toString());
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_FAKE_PROGRESS);
			onCreate(db);
		}
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		// TODO Auto-generated method stub
		
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int count;
		switch(sUriMatcher.match(uri)) {
		case URI_MATCH_CODE_FAKE_PROGRESS:
			count = db.delete(TABLE_NAME_FAKE_PROGRESS, where, whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		switch(sUriMatcher.match(uri)) {
		case URI_MATCH_CODE_FAKE_PROGRESS:
			return CONTENT_TYPE_FAKE_PROGRESS;
		case URI_MATCH_CODE_TASK_ID:
			return CONTENT_TYPE_TASK_ID;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		// TODO Auto-generated method stub
		if(sUriMatcher.match(uri) != URI_MATCH_CODE_FAKE_PROGRESS) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		ContentValues values;
		if(initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		long rowId = db.insert(TABLE_NAME_FAKE_PROGRESS, MSG_TABLE_COLUMN_TASK_ID, values);
		if(rowId > 0) {
			Uri insertedUri = ContentUris.withAppendedId(CONTENT_ID_URI_BASE, rowId);
			getContext().getContentResolver().notifyChange(insertedUri, null);
			return insertedUri;
		}
		throw new IllegalArgumentException("Unknown URI " + uri);
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		mContext = getContext();
		if(mDbHelper == null) {
			mDbHelper = new DatabaseHelper(mContext);
		}
		//mDbHelper.getWritableDatabase();//call this to create table
		return (mDbHelper == null) ? false : true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		qb.setTables(TABLE_NAME_FAKE_PROGRESS);

		switch(sUriMatcher.match(uri)) {
		case URI_MATCH_CODE_FAKE_PROGRESS:
			qb.setProjectionMap(sMsgProjectionMap);
			break;
		case URI_MATCH_CODE_TASK_ID:
			qb.setProjectionMap(sMsgProjectionMap);
			qb.appendWhere(MSG_TABLE_COLUMN_TASK_ID + " = " + uri.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		String orderBy = "";
		if(TextUtils.isEmpty(sortOrder)) {
			orderBy = DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}
	
	public static Uri withAppendedTaskId(long taskId) {
		return Uri.parse(SCHEME + AUTHORITY + "/" + PATH_FAKE_PROGRESS + "/" + taskId);
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count = 0;
        String finalWhere;
        switch(sUriMatcher.match(uri)) {
		case URI_MATCH_CODE_FAKE_PROGRESS:
			count = db.update(TABLE_NAME_FAKE_PROGRESS, values, where, whereArgs);
			break;
		case URI_MATCH_CODE_TASK_ID:
			String msgId = uri.getPathSegments().get(1);
			finalWhere = MSG_TABLE_COLUMN_TASK_ID + " = " + msgId;
			if (where != null) {
                finalWhere = finalWhere + " AND " + where;
            }
			count = db.update(TABLE_NAME_FAKE_PROGRESS, values, finalWhere, whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}
	
	
	public static class TaskInfo {
		public long taskId;
		public int taskState;
		public long currentSize;
		public long totalSize;
	}
	
	/*
	public static boolean isAppInWhiteList(Context context, String packageName) {
		String [] projection = new String [] {
			MSG_TABLE_COLUMN_TASK_ID
		};
		String selection = MSG_TABLE_COLUMN_TASK_ID + " = ?";
		String [] selectionArgs = new String [] { packageName };
		String sortOrder = null;
		Cursor cursor = context.getContentResolver().query(CONTENT_URI, projection, selection, selectionArgs, sortOrder);
		DbUtil.debugCursor(context, cursor);
		if(cursor.getCount() >= 1) {
			return true;
		}
		return false;
	}
	*/

	public static void saveTaskInfo(Context context, TaskInfo info) {
		
		/*
		MyLog.i2("SQF_LOG", "saveTaskInfo info: taskId:" + info.taskId + " taskState:" + info.taskState + 
						" currentSize:" + info.currentSize + " totalSize:" + info.totalSize);
						*/
		
		ContentValues values = new ContentValues();
		values.put(FakeProgressDb.MSG_TABLE_COLUMN_TASK_ID, info.taskId);
		values.put(FakeProgressDb.MSG_TABLE_COLUMN_TASK_STATE, info.taskState);
		if(info.currentSize != -1) {
			values.put(FakeProgressDb.MSG_TABLE_COLUMN_CURRENT_SIZE, info.currentSize);
		}
		if(info.totalSize != -1) {
			values.put(FakeProgressDb.MSG_TABLE_COLUMN_TOTAL_SIZE,  info.totalSize);
		}
		
		if(isTaskIdInserted(context, info.taskId)) {
			//update. don't insert
			Uri uri = FakeProgressDb.withAppendedTaskId(info.taskId);
			context.getContentResolver().update(uri, values, null, null);
			//Log.i("SQF_LOG" , "saveTaskInfo::update ..............");
			return;
		}
		
		Uri uri = FakeProgressDb.CONTENT_URI;
		context.getContentResolver().insert(uri, values);
		//Log.i("SQF_LOG" , "saveTaskInfo::insert ..............");
	}
	
	public static boolean isTaskIdInserted(Context context, long taskId) {
		if(taskId == -1) {
			//MyLog.i2("SQF_LOG", "taskId is -1");
			return false;
		}
		Uri uri = FakeProgressDb.withAppendedTaskId(taskId);
		String [] projection = new String [] {
				FakeProgressDb.MSG_TABLE_COLUMN_TASK_ID,
				FakeProgressDb.MSG_TABLE_COLUMN_TASK_STATE,
				FakeProgressDb.MSG_TABLE_COLUMN_CURRENT_SIZE,
				FakeProgressDb.MSG_TABLE_COLUMN_TOTAL_SIZE
			};
		
		//wenyongzhe modify 2015.12.11 cursor close start
		Cursor cursor = null;
		try{
			cursor = context.getContentResolver().query(uri, projection, null, null, null);
			if(cursor == null || cursor.getCount() == 0) {
				//MyLog.i("SQF_LOG", "isTaskIdInserted   taskId : " + taskId + " FALSE !");
				return false;
			}
		}finally{
			if(cursor != null){
				cursor.close();
			}
		}
		//wenyongzhe modify 2015.12.11 cursor close end
		
		//MyLog.i("SQF_LOG", "isTaskIdInserted   taskId : " + taskId + " TRUE !");
		return true;
	}
	
	public static ArrayList<TaskInfo> getAllSavedTaskInfo(Context context) {
		//long time1 = System.currentTimeMillis();
		ArrayList<TaskInfo> taskInfos = new ArrayList<TaskInfo>();
		Uri uri = CONTENT_URI;
		String [] projection = new String [] {
				FakeProgressDb.MSG_TABLE_COLUMN_TASK_ID,
				FakeProgressDb.MSG_TABLE_COLUMN_TASK_STATE,
				FakeProgressDb.MSG_TABLE_COLUMN_CURRENT_SIZE,
				FakeProgressDb.MSG_TABLE_COLUMN_TOTAL_SIZE
			};
		Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
		while(cursor.moveToNext()) {
			TaskInfo info = new TaskInfo();
			info.taskId = cursor.getLong(0);
			info.taskState = cursor.getInt(1);
			info.currentSize = cursor.getLong(2);
			info.totalSize = cursor.getLong(3);
			taskInfos.add(info);
		}
		//time1 = System.currentTimeMillis() - time1;
		//Log.i("SQF_LOG", "getAllSavedTaskInfo TIME USED :" + time1 );
		return taskInfos;
	}
	
	public static TaskInfo getTaskInfoByTaskId(Context context, long taskId) {
		//long time1 = System.currentTimeMillis();
		if(taskId == -1) {
			//MyLog.i2("SQF_LOG", "taskId is -1");
			return null;
		}
		Uri uri = FakeProgressDb.withAppendedTaskId(taskId);
		String [] projection = new String [] {
				FakeProgressDb.MSG_TABLE_COLUMN_TASK_ID,
				FakeProgressDb.MSG_TABLE_COLUMN_TASK_STATE,
				FakeProgressDb.MSG_TABLE_COLUMN_CURRENT_SIZE,
				FakeProgressDb.MSG_TABLE_COLUMN_TOTAL_SIZE
			};
		Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
		DbUtil.debugCursor(context, cursor);
		if(cursor == null || cursor.getCount() == 0) return null;
		cursor.moveToFirst();
		TaskInfo info = new TaskInfo();
		info.taskId = cursor.getLong(0);
		info.taskState = cursor.getInt(1);
		info.currentSize = cursor.getLong(2);
		info.totalSize = cursor.getLong(3);
		//time1 = System.currentTimeMillis() - time1;
		//Log.i("SQF_LOG", "getTaskInfoByTaskId TIME USED :" + time1 );
		/*
		MyLog.i2("SQF_LOG", "FakeProgressDb::getTaskInfoByTaskId info.taskId:" + info.taskId + " info.taskState:" + info.taskState + 
							" info.currentSize:" + info.currentSize + " info.totalSize:" + info.totalSize);
		*/
		
		return info;
	}
	
	public static void deleteTaskInfoByTaskId(Context context, long taskId) {
		Uri uri = FakeProgressDb.CONTENT_URI;
		String where = MSG_TABLE_COLUMN_TASK_ID + " = ?";
		String [] whereArgs = {""+taskId};
		context.getContentResolver().delete(uri, where, whereArgs);
	}
}




