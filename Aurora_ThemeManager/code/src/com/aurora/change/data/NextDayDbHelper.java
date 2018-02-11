package com.aurora.change.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.aurora.thememanager.R;
import android.os.SystemProperties;
import com.aurora.change.imagecache.DiskLruCache;
import com.aurora.change.imagecache.ImageCache;
import com.aurora.change.model.DbInfoModel;
import com.aurora.change.model.NextDayDbInfoModel;
import com.aurora.change.model.PictureGroupInfo;
import com.aurora.change.model.PictureInfo;
import com.aurora.change.model.ThemeInfo;
import com.aurora.change.utils.CommonLog;
import com.aurora.change.utils.Consts;
import com.aurora.change.utils.LogFactory;
import com.aurora.change.utils.WallpaperConfigUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore.Files;
import android.util.Log;

public class NextDayDbHelper {
    private static final String TAG = "Wallpaper_DEBUG";

    public DatabaseHelper mNextDayDBHelper;
    public SQLiteDatabase mNextDaySqlDb;
    
    public NextDayDbHelper(Context context) {
    	openDb(context);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		private Context mContext;
		private boolean isUpdate = false;
		private String mType;
		
        public DatabaseHelper(Context context) {
            super(context, NextDayDbInfoModel.DATABASE_NAME, null, NextDayDbInfoModel.DATABASE_VERSION);
            mContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "NextDayDbHelper================oncreate database " + db.getPath());
            db.execSQL("CREATE TABLE " + NextDayDbInfoModel.NEXTDAY_TABLE_NAME + " ("
                    + NextDayDbInfoModel.PictureColumns.NAME + " TEXT UNIQUE,"
                    + NextDayDbInfoModel.PictureColumns.TIME + " TEXT UNIQUE, "
                    + NextDayDbInfoModel.PictureColumns.DIMENSION + " TEXT, "
                    + NextDayDbInfoModel.PictureColumns.THUMNAIL_URL + " TEXT, "
                    + NextDayDbInfoModel.PictureColumns.ORIGINAL_URL + " TEXT, "
                    + NextDayDbInfoModel.PictureColumns.TIME_BLACK + " TEXT,"
                    + NextDayDbInfoModel.PictureColumns.STATUSBAR_BLACK + " TEXT,"
                    + NextDayDbInfoModel.PictureColumns.COMMENT_CITY + " TEXT,"
                    + NextDayDbInfoModel.PictureColumns.COMMENT + " TEXT" + ");");
                        
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
        	isUpdate = true;
            Log.d(TAG, "NextDayDbHelper================onUpgrade change database from version " + oldVersion + " to " + currentVersion);
            db.execSQL("DROP TABLE IF EXISTS " + NextDayDbInfoModel.NEXTDAY_TABLE_NAME);
            onCreate(db);
        }
        
        public void refreshDb(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + NextDayDbInfoModel.NEXTDAY_TABLE_NAME);
            onCreate(db);
        }
        
        
    }

    public void openDb(Context context) {
    	mNextDayDBHelper = new DatabaseHelper(context);
        try {
        	mNextDaySqlDb = mNextDayDBHelper.getWritableDatabase();
        } catch (Exception e) {
        	mNextDaySqlDb = mNextDayDBHelper.getReadableDatabase();
            e.printStackTrace();
        }
        Log.d(TAG, "NextDayDbHelper================DATA=openDb()");
    }

    public void closeDb() throws SQLiteException {
        if (mNextDayDBHelper != null) {
        	mNextDayDBHelper.close();
        	mNextDayDBHelper = null;
        }
        if (mNextDaySqlDb != null) {
        	mNextDaySqlDb.close();
        	mNextDaySqlDb = null;
        }
    }
    
    public void refreshDb() {
    	Log.d("Wallpaper_DEBUG", "NextDayDbHelper================refreshDb = "+mNextDayDBHelper+" mSqlDb = "+mNextDaySqlDb);
    	if (mNextDayDBHelper != null && mNextDaySqlDb != null) {
    		mNextDayDBHelper.isUpdate = true;
    		mNextDayDBHelper.refreshDb(mNextDaySqlDb);
		}
    }
    
}