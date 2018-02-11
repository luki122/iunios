package com.aurora.community.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;

import com.aurora.community.CommunityApp;

public class AccountHelper {

	private static final String TAG = "AccountHelper";

	private final String AUTHORITY = "com.aurora.account.accountprovider";
	private final String ACCOUNT_CONTENT_URI = "content://" + AUTHORITY
			+ "/account_info";

	private ContentResolver cr;
	private AccountContentObserver ob;

	public static final String ACCOUNT_INFO_HAS_LOGIN = "hasLogin";

	/** user_id */
	public static final String ACCOUNT_INFO_USERID = "user_id";
	/** cookie */
	public static final String ACCOUNT_INFO_COOKIE = "cookie";

	/** login flag */
	public static boolean mLoginStatus = false;

	/** user_id */
	public static String user_id = "";

	private static String old_user_id = "";
	
	/** cookie */
	public static String cookie = "";

	public AccountHelper(Context context) {
		this.cr = context.getContentResolver();
	}

	/** register account content observer.we can used it later but not this time */
	public void registerAccountContentResolver() {
		Uri uri = Uri.parse(ACCOUNT_CONTENT_URI);
		ob = new AccountContentObserver(new Handler());
		cr.registerContentObserver(uri, true, ob);
	}

	public void unregisterAccountContentResolver() {
		cr.unregisterContentObserver(ob);
		mLoginStatus = false;
		user_id = "";
		old_user_id = "";
	}

	public static void logout(){
		mLoginStatus = false;
		user_id = "";
		old_user_id = "";
	}
	
	private IAccountChange iAccountChangeListener;

	public void setIAccountChangeListener(IAccountChange iAccountChangeListener){
		this.iAccountChangeListener = iAccountChangeListener;
	}
	
	public static interface IAccountChange{
		void changeAccount();
		void unLogin();
	}
	
	class AccountContentObserver extends ContentObserver {

		public AccountContentObserver(Handler handler) {
			super(handler);

		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			// TODO Auto-generated method stub
			super.onChange(selfChange, uri);
			update();
			/** obtain message and send to main */
			// mHandler.obtainMessage(MainActivity.MSG_LOGIN).sendToTarget();
		}
	}

	public void update() {
		Cursor cursor = cr.query(Uri.parse(ACCOUNT_CONTENT_URI), null, null,
				null, null);
		if (cursor != null && cursor.moveToFirst()) {

			
			mLoginStatus = cursor.getInt(cursor
					.getColumnIndex("hasLogin")) == 1 ? true : false;
			user_id = cursor.getString(cursor.getColumnIndex("user_id"));
			if (mLoginStatus) {
				if (!TextUtils.isEmpty(old_user_id)
						&& !TextUtils.isEmpty(user_id)) {
					if (!old_user_id.equals(user_id)
							&& iAccountChangeListener != null) {
						iAccountChangeListener.changeAccount();
					}
				}
			}else if(!TextUtils.isEmpty(old_user_id)){
				if(iAccountChangeListener != null)
				{
					iAccountChangeListener.unLogin();
				}
			}
			old_user_id = user_id;
			cookie = cursor.getString(cursor.getColumnIndex("cookie"));
			Log.i("zhangwei", "zhangwei the user_id=" + user_id
					+ " the cookie=" + cookie + " the hasLogin=" + mLoginStatus);
		}
		cursor.close();
	}

	public void updateFromLocal() {
		AccountPreferencesUtil pref = AccountPreferencesUtil
				.getInstance(CommunityApp.getInstance());
		boolean hasLogin = BooleanPreferencesUtil
				.getInstance(CommunityApp.getInstance()).hasLogin();
		
		mLoginStatus = hasLogin;
		user_id = pref.getUserID();
		if (mLoginStatus) {
			if (!TextUtils.isEmpty(old_user_id) && !TextUtils.isEmpty(user_id)) {
				if (!old_user_id.equals(user_id)
						&& iAccountChangeListener != null) {
					iAccountChangeListener.changeAccount();
				}
			}
		} else if (!TextUtils.isEmpty(old_user_id)) {
			if (iAccountChangeListener != null) {
				iAccountChangeListener.unLogin();
			}
		}
		old_user_id = user_id;
		cookie = pref.getUserCookie();
		
		Log.i(TAG, "updateFromLocal the user_id=" + user_id
				+ " the cookie=" + cookie + " the hasLogin=" + mLoginStatus);
	}


}
