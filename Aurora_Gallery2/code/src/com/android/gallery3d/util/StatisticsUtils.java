package com.android.gallery3d.util;

import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

public class StatisticsUtils {
	
	public static final int MODULE_ID = 120;
	
	public static final int STATISTICS_ID_PHOTO_PAGE_SHARE = 31;
	public static final int STATISTICS_ID_PHOTO_PAGE_SET_AS = 32;
	public static final int STATISTICS_ID_PHOTO_PAGE_DELETE = 33;
	public static final int STATISTICS_ID_ENTER_TTPIC = 37;
	
//	public static final int REPORT_ID_PHOTO_PAGE_SHARE = 120031;
//	public static final int REPORT_ID_PHOTO_PAGE_SET_AS = 120032;
//	public static final int REPORT_ID_PHOTO_PAGE_DELETE = 120033;
	
	public static final String MODULE_KEY = "module_key";
	public static final String ITEM_TAG = "item_tag";
	public static final String VALUE = "value";
	
	public static final Uri GALLERY_STATISTICS_URI = Uri.parse("content://com.iuni.reporter/module/");
	public static final int TOKEN = 1;
	
	public static void addPhotoPageShareStatistics(Context context) {
		
		AsyncQueryHandler  handler = new AsyncQueryHandler (context.getContentResolver()) {
			@Override
			protected void onUpdateComplete(int token, Object cookie, int result) {
				// TODO Auto-generated method stub
				super.onUpdateComplete(token, cookie, result);
				if(token == TOKEN) {
					//Log.i("SQF_LOG", "StatisticsUtils::addPhotoPageShareStatistics:  result-->" + result);
				}
			}
			
		};
		
		
		ContentValues values = new ContentValues();
		values.put(MODULE_KEY, MODULE_ID);
		values.put(ITEM_TAG, STATISTICS_ID_PHOTO_PAGE_SHARE);
		values.put(VALUE, 1);
		//int id = context.getContentResolver().update(GALLERY_STATISTICS_URI, values, null, null);
		try {
			handler.startUpdate(TOKEN, null, GALLERY_STATISTICS_URI, values, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//Log.i("SQF_LOG", "StatisticsUtils::addPhotoPageShareStatistics: " + id);
	}
	
	public static void addPhotoPageSetAsStatistics(Context context) {
		AsyncQueryHandler  handler = new AsyncQueryHandler (context.getContentResolver()) {
			@Override
			protected void onUpdateComplete(int token, Object cookie, int result) {
				// TODO Auto-generated method stub
				super.onUpdateComplete(token, cookie, result);
				if(token == TOKEN) {
					//Log.i("SQF_LOG", "StatisticsUtils::addPhotoPageSetAsStatistics:  result-->" + result);
				}
			}
			
		};
		ContentValues values = new ContentValues();
		values.put(MODULE_KEY, MODULE_ID);
		values.put(ITEM_TAG, STATISTICS_ID_PHOTO_PAGE_SET_AS);
		values.put(VALUE, 1);
		try {
			handler.startUpdate(TOKEN, null, GALLERY_STATISTICS_URI, values, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//int id = context.getContentResolver().update(GALLERY_STATISTICS_URI, values, null, null);
		//Log.i("SQF_LOG", "StatisticsUtils::addPhotoPageSetAsStatistics: " + id);
	}
	
	public static void addPhotoPageDeleteStatistics(Context context) {
		AsyncQueryHandler  handler = new AsyncQueryHandler (context.getContentResolver()) {
			@Override
			protected void onUpdateComplete(int token, Object cookie, int result) {
				// TODO Auto-generated method stub
				super.onUpdateComplete(token, cookie, result);
				if(token == TOKEN) {
					//Log.i("SQF_LOG", "StatisticsUtils::addPhotoPageDeleteStatistics:  result-->" + result);
				}
			}
			
		};
		ContentValues values = new ContentValues();
		values.put(MODULE_KEY, MODULE_ID);
		values.put(ITEM_TAG, STATISTICS_ID_PHOTO_PAGE_DELETE);
		values.put(VALUE, 1);
		try {
			handler.startUpdate(TOKEN, null, GALLERY_STATISTICS_URI, values, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//int id = context.getContentResolver().update(GALLERY_STATISTICS_URI, values, null, null);
		//Log.i("SQF_LOG", "StatisticsUtils::addPhotoPageDeleteStatistics: " + id);
	}
	
	public static void addEnterTTPicStatistics(Context context) {
		//Log.i("SQF_LOG", "StatisticsUtils::addEnterTTPicStatistics:  result--======================================>");
		AsyncQueryHandler  handler = new AsyncQueryHandler (context.getContentResolver()) {
			@Override
			protected void onUpdateComplete(int token, Object cookie, int result) {
				// TODO Auto-generated method stub
				super.onUpdateComplete(token, cookie, result);
				if(token == TOKEN) {
					//Log.i("SQF_LOG", "StatisticsUtils::addEnterTTPicStatistics:  result-->" + result);
				}
			}
			
		};
		ContentValues values = new ContentValues();
		values.put(MODULE_KEY, MODULE_ID);
		values.put(ITEM_TAG, STATISTICS_ID_ENTER_TTPIC);
		values.put(VALUE, 1);
		try {
			handler.startUpdate(TOKEN, null, GALLERY_STATISTICS_URI, values, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//int id = context.getContentResolver().update(GALLERY_STATISTICS_URI, values, null, null);
		//Log.i("SQF_LOG", "StatisticsUtils::addPhotoPageDeleteStatistics: " + id);
	}
}
