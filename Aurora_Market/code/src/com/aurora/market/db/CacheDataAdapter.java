package com.aurora.market.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.aurora.datauiapi.data.bean.cacheitem;
import com.aurora.datauiapi.data.bean.iconItem;
import com.aurora.datauiapi.data.bean.upappListtem;
import com.aurora.market.model.InstalledAppInfo;
import com.aurora.market.util.FileLog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jason
 * 
 *         对忽略应用更新的数据库操作
 * 
 */
public class CacheDataAdapter extends DBAdapter {

	public static final String TAG = "CacheDataAdapter";
	public static final String TABLE_NAME = "tbl_cache";// 数据库表名
	public static final String TYPE = "type";// 0主界面 1 新品 2排行 3分类 4.专题  5 必备  6 设计
	public static final String APP_TYPE = "app_type"; // APP --应用  GAME--游戏  在 type参数为 排行和分类时此参数有用
	public static final String CAT_ID = "cat_id"; //分类id 仅在type为3时有用
	public static final String SPE_ID = "spe_id"; //专题id 仅在type为4时有用
	public static final String CONTEXT = "context"; // 缓存的数据
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

	/**
	 * 保存缓存数据(多条)
	 * 
	 * @param List
	 *            <DownloadResult> result
	 * 
	 */
	public void insert(List<cacheitem> result) {
		mDb.beginTransaction();

		try {
			for (cacheitem upResult : result) {
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
	 * 保存缓存数据(单条)
	 * 
	 * @param NoteResult
	 *            result
	 * 
	 */
	public void insert(cacheitem result) {
		ContentValues value = new ContentValues();
		value.put(TYPE, result.getType());
		value.put(CONTEXT, result.getContext());
		value.put(APP_TYPE, result.getApp_type());
		value.put(CAT_ID, result.getCat_id());
		value.put(SPE_ID, result.getSpe_id());
		value.put(UPDATE_TIME, result.getUpdate_time());

		mDb.insert(TABLE_NAME, null, value);
	}

	/**
	 * 
	 * 查询全部数据
	 * 
	 * @return
	 */

	public ArrayList<cacheitem> queryAllData() {
		String selection = null;

		/* selection="(" + DRAFT_FID + "='" + fid+"')"; */
		Cursor result = mDb.query(TABLE_NAME, new String[] { TYPE,APP_TYPE,CAT_ID,SPE_ID, CONTEXT,
				UPDATE_TIME }, selection, null, null, null, UPDATE_TIME
				+ " desc");
		ArrayList<cacheitem> note = ConvertToCache(result);
		if (result != null && !result.isClosed()) {
			result.close();
			result = null;
		}
		return note;
	}

	// 删除除首页外的所有数据
	public int deleteDataByType(int type) {
		int result = 0;
		if(type ==0)
			result = mDb.delete(TABLE_NAME, TYPE + "<>?",
					new String[] { String.valueOf(type) });
		else
			result = mDb.delete(TABLE_NAME, null,null);
		
		return result;
	}



	/**
	 * 
	 * ConvertToRentalCar(Cursor cursor)是私有函数，
	 * 
	 * 作用是将查询结果转换为用来存储数据自定义的upappListtem类对象
	 * 
	 */
	private ArrayList<cacheitem> ConvertToCache(Cursor cursor) {
		int resultCounts = cursor.getCount();
		if (resultCounts == 0 || !cursor.moveToFirst()) {
			return null;
		}
		ArrayList<cacheitem> rentalCar = new ArrayList<cacheitem>();
		// Log.i(TAG, "ChatMessage len:" + chatMessage.length);
		for (int i = 0; i < resultCounts; i++) {
			cacheitem m_result = new cacheitem();
			m_result.setType(cursor.getInt(0));

			m_result.setContext(cursor.getString(cursor.getColumnIndex(CONTEXT)));
			
			m_result.setApp_type(cursor.getString(cursor.getColumnIndex(APP_TYPE)));
			m_result.setCat_id(cursor.getInt(cursor.getColumnIndex(CAT_ID)));
			m_result.setSpe_id(cursor.getInt(cursor.getColumnIndex(SPE_ID)));
			
			m_result.setUpdate_time(cursor.getString(cursor
					.getColumnIndex(UPDATE_TIME)));

			rentalCar.add(m_result);
			cursor.moveToNext();
		}
		return rentalCar;
	}

	/**
	 * 
	 * 通过类型查询缓存
	 * 
	 * @return
	 */

	public String queryCacheByType(int type,String app_type,int cat_id,int spe_id) {
		String selection = null;
		// 0主界面 1 新品 2排行 3分类 4.专题  5 必备  6 设计 7 专题主页 8 分类主页
		if((type == 0)||(type == 1)||(type == 5)||(type == 6)||(type == 7))
			selection = "(" + TYPE + "='" + String.valueOf(type) + "')";
		else if((type == 2)||(type == 8))
			selection = "(" + TYPE + "='" + String.valueOf(type) + "') and (" + APP_TYPE + "='" + app_type + "')" ;
		else if(type == 3)
			selection = "(" + TYPE + "='" + String.valueOf(type) + "') and (" + APP_TYPE + "='" + app_type + "') and (" + CAT_ID + "='" + cat_id + "')" ;
		else if(type == 4)
			selection = "(" + TYPE + "='" + String.valueOf(type) + "')  and (" + SPE_ID + "='" + spe_id + "')" ;
		
		Cursor cursor = mDb.query(TABLE_NAME, new String[] { CONTEXT },
				selection, null, null, null, UPDATE_TIME + " desc");
		String context = null;
		boolean bl = cursor.moveToFirst();

		if (bl) {
			context = cursor.getString(cursor.getColumnIndex(CONTEXT));
		}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		return context;
	}

	/**
	 * 
	 * 通过类型更新缓存
	 * 
	 * @return
	 */
	public int updateCacheByType(cacheitem item) {
		ContentValues values = new ContentValues();
		values.put(CONTEXT, item.getContext());
		values.put(TYPE, item.getType());
		values.put(UPDATE_TIME, item.getUpdate_time());

		int rowNum = mDb.update(TABLE_NAME, values, TYPE + "=?",
				new String[] { String.valueOf(item.getType()) });
		return rowNum;
	}

}
