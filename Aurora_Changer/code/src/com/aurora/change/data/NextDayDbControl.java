package com.aurora.change.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.aurora.change.model.DbInfoModel;
import com.aurora.change.model.NextDayDbInfoModel;
import com.aurora.change.model.NextDayPictureInfo;
import com.aurora.change.model.PictureGroupInfo;
import com.aurora.change.model.PictureInfo;
import com.aurora.change.utils.CommonLog;
import com.aurora.change.utils.Consts;
import com.aurora.change.utils.LogFactory;
import com.aurora.change.utils.WallpaperConfigUtil;
import com.aurora.change.utils.WallpaperUtil;

import com.aurora.change.AuroraChangeApp;
import com.aurora.change.R;

public class NextDayDbControl extends NextDayDbHelper {

    private Context mContext;

    public NextDayDbControl(Context context) {
    	super(context);
        //super.openDb(context);
        mContext = context;
    }

    public void close() {
        super.closeDb();
    }
    
    public boolean insertPictureInfo(ContentValues values) {
    	long id = 0;
    	if (mNextDaySqlDb != null) {
			id = mNextDaySqlDb.insert(NextDayDbInfoModel.NEXTDAY_TABLE_NAME, null, values);
		}
    	Log.d("Wallpaper_DEBUG", "NextDayDbControl================insertPictureInfo = "+values.get(NextDayDbInfoModel.PictureColumns.NAME) + " status = "+id);
    	return id > 0? true : false;
    }
    
    public void insertPictureInfoSafety(JSONArray array) {
    	new InsertNextDayDBThread(array).run();
    }

    public boolean updatePictureInfoByName(ContentValues values, String[] name) {
    	long id = 0;
    	if (mNextDaySqlDb != null) {
			id = mNextDaySqlDb.update(NextDayDbInfoModel.NEXTDAY_TABLE_NAME, values, NextDayDbInfoModel.PictureColumns.NAME + "=?", name);
		}
    	Log.d("Wallpaper_DEBUG", "NextDayDbControl================updatePictureInfoByName = "+id);
    	return id > 0? true : false;
    }

    public boolean deletePictureInfoByName(String name) {
        long id = 0;
        if (mNextDaySqlDb.isOpen()) {
            id = mNextDaySqlDb.delete(NextDayDbInfoModel.NEXTDAY_TABLE_NAME, NextDayDbInfoModel.PictureColumns.NAME + "=?",
                    new String[] {name});
        }
        Log.d("Wallpaper_DEBUG", "NextDayDbControl================deletePictureInfoByName = "+id);
        return id > 0 ? true : false;
    }

    public List<NextDayPictureInfo> queryAllPictureInfos() {
        List<NextDayPictureInfo> list = new ArrayList<NextDayPictureInfo>();
        Cursor mCursor = mNextDaySqlDb.query(NextDayDbInfoModel.NEXTDAY_TABLE_NAME, null, null, null, null, null, null);
        Log.d("Wallpaper_DEBUG", "NextDayDbControl-----------queryAllPictureInfos-------mCursor.getCount = "+mCursor.getCount());
        while (mCursor.moveToNext()) {
            NextDayPictureInfo pictureInfo = new NextDayPictureInfo();
            pictureInfo.setPictureName(mCursor.getString(NextDayDbInfoModel.PictureColumns.NAME_INDEX));
            pictureInfo.setPictureTime(mCursor.getString(NextDayDbInfoModel.PictureColumns.TIME_INDEX));
            pictureInfo.setPictureDimension(mCursor.getString(NextDayDbInfoModel.PictureColumns.DIMENSION_INDEX));
            pictureInfo.setPictureThumnailUrl(mCursor.getString(NextDayDbInfoModel.PictureColumns.THUMNAIL_URL_INDEX));
            pictureInfo.setPictureOriginalUrl(mCursor.getString(NextDayDbInfoModel.PictureColumns.ORIGINAL_URL_INDEX));
            pictureInfo.setPictureTimeColor(mCursor.getString(NextDayDbInfoModel.PictureColumns.TIME_BLACK_INDEX));
            pictureInfo.setPictureStatusColor(mCursor.getString(NextDayDbInfoModel.PictureColumns.STATUSBAR_BLACK_INDEX));
            pictureInfo.setPictureCommentCity(mCursor.getString(NextDayDbInfoModel.PictureColumns.COMMENT_CITY_INDEX));
            pictureInfo.setPictureComment(mCursor.getString(NextDayDbInfoModel.PictureColumns.COMMENT_INDEX));
            
            list.add(pictureInfo);
        }
        mCursor.close();
        return list;
    }

    public NextDayPictureInfo queryPictureInfoByName(String name) {
        NextDayPictureInfo pictureInfo = new NextDayPictureInfo();
        Cursor mCursor = mNextDaySqlDb.query(NextDayDbInfoModel.NEXTDAY_TABLE_NAME, null,
                NextDayDbInfoModel.PictureColumns.NAME + "=?", new String[] {name}, null, null, null);
        while (mCursor.moveToNext()) {
            pictureInfo.setPictureName(mCursor.getString(NextDayDbInfoModel.PictureColumns.NAME_INDEX));
            pictureInfo.setPictureTime(mCursor.getString(NextDayDbInfoModel.PictureColumns.TIME_INDEX));
            pictureInfo.setPictureDimension(mCursor.getString(NextDayDbInfoModel.PictureColumns.DIMENSION_INDEX));
            pictureInfo.setPictureThumnailUrl(mCursor.getString(NextDayDbInfoModel.PictureColumns.THUMNAIL_URL_INDEX));
            pictureInfo.setPictureOriginalUrl(mCursor.getString(NextDayDbInfoModel.PictureColumns.ORIGINAL_URL_INDEX));
            pictureInfo.setPictureTimeColor(mCursor.getString(NextDayDbInfoModel.PictureColumns.TIME_BLACK_INDEX));
            pictureInfo.setPictureStatusColor(mCursor.getString(NextDayDbInfoModel.PictureColumns.STATUSBAR_BLACK_INDEX));
            pictureInfo.setPictureCommentCity(mCursor.getString(NextDayDbInfoModel.PictureColumns.COMMENT_CITY_INDEX));
            pictureInfo.setPictureComment(mCursor.getString(NextDayDbInfoModel.PictureColumns.COMMENT_INDEX));            
        }
        mCursor.close();
        return pictureInfo;
    }
    
    class InsertNextDayDBThread implements Runnable {
    	JSONArray resultArray;
    	
    	public InsertNextDayDBThread(JSONArray array) {
			// TODO Auto-generated constructor stub
    		resultArray = array;
		};
    	
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.d("Wallpaper_DEBUG", "NextDayDbControl-----------InsertNextDayDBThread------run = ");
			if (mNextDaySqlDb == null) return;
			
			String resolution = String.valueOf(((AuroraChangeApp) mContext.getApplicationContext()).getDisplayWidth()) + "*" + 
								String.valueOf(((AuroraChangeApp) mContext.getApplicationContext()).getDisplayHeight());
			
			synchronized (mNextDaySqlDb) {
				for (int i = 0; i < resultArray.length(); i++) {
					try {
						JSONObject resultJson = resultArray.getJSONObject(i);
						
						NextDayPictureInfo pictureInfo = queryPictureInfoByName(resultJson.optString("date"));
						if (pictureInfo != null && pictureInfo.getPictureName() != null) continue;
												
						ContentValues values = new ContentValues();
						values.put(NextDayDbInfoModel.PictureColumns.NAME, resultJson.optString("date"));
	                    values.put(NextDayDbInfoModel.PictureColumns.TIME, resultJson.optString("date"));
	                    values.put(NextDayDbInfoModel.PictureColumns.DIMENSION, resolution);
	                    values.put(NextDayDbInfoModel.PictureColumns.THUMNAIL_URL, resultJson.optString("picUrl" + resolution.replace("*", "X")));
	                    values.put(NextDayDbInfoModel.PictureColumns.ORIGINAL_URL, resultJson.optString("picUrl" + resolution.replace("*", "X")));
	                    values.put(NextDayDbInfoModel.PictureColumns.TIME_BLACK, resultJson.optString("timeWidgetColor"));
	                    values.put(NextDayDbInfoModel.PictureColumns.STATUSBAR_BLACK, resultJson.optString("timeWidgetColor"));
	                    values.put(NextDayDbInfoModel.PictureColumns.COMMENT_CITY, resultJson.optString("commentCity"));
	                    values.put(NextDayDbInfoModel.PictureColumns.COMMENT, resultJson.optString("comment"));
	                    
	                    insertPictureInfo(values);
						
					} catch (Exception e) {
						// TODO: handle exception
						Log.d("Wallpaper_DEBUG", "NextDayDbControl-----------InsertNextDayDBThread------Exception = "+e);
					}
					
				}
				close();
			}
		}
    }

}
