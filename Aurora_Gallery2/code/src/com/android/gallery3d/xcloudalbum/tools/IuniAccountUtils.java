package com.android.gallery3d.xcloudalbum.tools;


import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class IuniAccountUtils {
	
	public static final int MSG_LOGIN = 1;
	
	private static IuniAccountUtils mInstance;
	
	private Context mContext;
	private ContentResolver mContentResolver; 
	private String mToken;
	
	private AccountProviderObserver mAccountProviderObserver;
	
	private IuniAccountUtils (Context context) {
		mContext = context;
		mContentResolver = mContext.getContentResolver();
		initAccountProviderObserver();
	}
	
	public static IuniAccountUtils getInstance(Context context) {
		if(mInstance == null) {
			mInstance = new IuniAccountUtils(context);
		}
		return mInstance;
	}
	
	public void setToken(String token) {
		mToken = token;
	}
	
	public String getToken() {
		return mToken;
	}
	


	private void initAccountProviderObserver() {
		mAccountProviderObserver = new AccountProviderObserver(new Handler());
		mAccountProviderObserver.observe();
		mAccountProviderObserver.update();
	}
	
	public void login() {
		try {
			Log.i("SQF_LOG", "IUNI login");
			Uri uri = Uri
					.parse("openaccount://com.aurora.account.login");
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			intent.putExtra("type", 1);
			intent.setData(uri);
			// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(intent);
		} catch (Exception e) {
		}
	}
	
	public void getAccountInfo() {

		try {
			Uri uri = Uri
					.parse("openaccountinfo://com.aurora.account.accountInfo");
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			
			intent.setData(uri);
			// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(intent);
		} catch (Exception e) {
		}
	}
	
	
	
	class AccountProviderObserver extends ContentObserver {
		private Uri uri;

		AccountProviderObserver(Handler handler) {
			super(handler);
			uri = Uri.parse("content://com.aurora.account.accountprovider/account_info");
		}

		void observe() {
			mContentResolver.registerContentObserver(uri, false, this);
		}
		void unobserve() {
			mContentResolver.unregisterContentObserver(this);
		}
		@Override
		public void onChange(boolean selfChange) {
			update();
		}

		public void update() {
			// String[] projection = {"nick", "iconPath",
			// "lastSyncFinishedTime", "hasLogin"};
			Cursor cursor = mContentResolver.query(uri, null, null, null,
					null);
			if (cursor != null && cursor.moveToFirst()) {
				String nickName = cursor.getString(cursor
						.getColumnIndex("nick"));
				String iconPathName = cursor.getString(cursor
						.getColumnIndex("iconPath"));
				
				long lastSyncFinishedTimeName = cursor.getLong(cursor
						.getColumnIndex("lastSyncFinishedTime"));
				boolean hasLoginName = cursor.getInt(cursor
						.getColumnIndex("hasLogin")) == 1 ? true : false;
				String token = cursor.getString(cursor
						.getColumnIndex("token"));
				
				String  user_id= cursor.getString(cursor
						.getColumnIndex("user_id"));
				
				String cookie = cursor.getString(cursor
						.getColumnIndex("cookie"));
				//Log.i("SQF_LOG", "zhangwei the user_id="+user_id+" the token"+token+ " the cookie="+cookie+" the hasLogin="+hasLoginName);
			}
		}
	}
}
