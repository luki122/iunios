package com.aurora.iunivoice.utils;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;

import com.aurora.iunivoice.IuniVoiceApp;

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
	public static boolean mLoginStatus = true;

	/** user_id */
	public static String user_id = "";

	private static String old_user_id = "";
	
	/** cookie */
	public static String cookie = "";

	private AccountHelper(Context context) {
		this.cr = context.getContentResolver();
	}

	private static AccountHelper instance;
	
	public static AccountHelper getInstance(Context context){
		if(instance == null)
		{
			instance = new AccountHelper(context);
		}
		return instance;
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
		cookie = "";
	}
	

	private ArrayList<IAccountChangeListener > iAccountChangeListeners = new ArrayList<AccountHelper.IAccountChangeListener>();
	
	public void addIAccountChangeListener(IAccountChangeListener iAccountChangeListener){
		iAccountChangeListeners.add(iAccountChangeListener);
	}
	
	public void removeIAccountChangeListener(IAccountChangeListener iAccountChangeListener){
		for (IAccountChangeListener ichange : iAccountChangeListeners) {
			
			if(ichange == iAccountChangeListener)
			{
				iAccountChangeListeners.remove(ichange);
			}
		}
	}
	
	public static interface IAccountChangeListener{
		void changeAccount();
		void unLogin();
		void loginSuccess();
		void userInfoChange();
	}
	
	class AccountContentObserver extends ContentObserver {

		public AccountContentObserver(Handler handler) {
			super(handler);

		}

		@SuppressLint("NewApi") @Override
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
			
			if(!mLoginStatus){
				SharedPreferences sp = IuniVoiceApp.getInstance().getSharedPreferences("config", Context.MODE_PRIVATE);
				sp.edit().putBoolean("isLoginFromApp", false).commit();
				com.aurora.iunivoice.utils.Log.v("lmjssjj",
						"isLoginFromApp:" + false);
			}
			
			user_id = cursor.getString(cursor.getColumnIndex("user_id"));
			if (mLoginStatus) {
				if (!TextUtils.isEmpty(old_user_id)
						&& !TextUtils.isEmpty(user_id)) {
					if (!old_user_id.equals(user_id)
							) {
						loginstateChange(ACCOUNT_CHANGE);
					}
				}else{
					loginstateChange(LOGIN_SUCCESS);
				}
			}else if(!TextUtils.isEmpty(old_user_id)){
				loginstateChange(STATE_LOGOUT);
			}
			old_user_id = user_id;
			cookie = cursor.getString(cursor.getColumnIndex("cookie"));
			Log.i("zhangwei", "zhangwei the user_id=" + user_id
					+ " the cookie=" + cookie + " the hasLogin=" + mLoginStatus);
		}
		cursor.close();
	}

	public void userInfoChange(){
		loginstateChange(USERINFO_CHANGE);
	}
	
	private static final int STATE_LOGOUT = 1,ACCOUNT_CHANGE = 2,LOGIN_SUCCESS = 3,USERINFO_CHANGE = 4;
	private void loginstateChange(int state){
		for (IAccountChangeListener listener : iAccountChangeListeners) {
			
			if(state == STATE_LOGOUT )
			{
				listener.unLogin();
			}else if(state == ACCOUNT_CHANGE)
			{
				listener.changeAccount();
			}else if(state == LOGIN_SUCCESS)
			{
				 listener.loginSuccess();
			}else if(state == USERINFO_CHANGE)
			{
				listener.userInfoChange();
			}
		}
	}
	
	public void updateFromLocal() {
		AccountPreferencesUtil pref = AccountPreferencesUtil
				.getInstance(IuniVoiceApp.getInstance());
		boolean hasLogin = BooleanPreferencesUtil
				.getInstance(IuniVoiceApp.getInstance()).hasLogin();
		
		mLoginStatus = hasLogin;
		user_id = pref.getUserID();
		if (mLoginStatus) {
			if (!TextUtils.isEmpty(old_user_id) && !TextUtils.isEmpty(user_id)) {
				if (!old_user_id.equals(user_id)
						) {
					loginstateChange(ACCOUNT_CHANGE);
				}
			}
		} else if (!TextUtils.isEmpty(old_user_id)) {
			loginstateChange(STATE_LOGOUT);
		}
		old_user_id = user_id;
		cookie = pref.getUserCookie();
		
		Log.i(TAG, "updateFromLocal the user_id=" + user_id
				+ " the cookie=" + cookie + " the hasLogin=" + mLoginStatus);
	}


}
