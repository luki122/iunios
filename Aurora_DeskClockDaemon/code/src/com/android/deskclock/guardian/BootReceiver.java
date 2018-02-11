package com.android.deskclock.guardian;

import java.lang.Thread.State;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

	private boolean flag;
	private static boolean mWidgetEnable = true;
	private Handler mHandler = new Handler();

	private DoubleKillThread mDoubleKillThread;

	@Override
	public void onReceive(final Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		flag = true;
		mDoubleKillThread = DoubleKillThread.getInstance(context, flag);
		if (action == Intent.ACTION_BOOT_COMPLETED) {

			SharedPreferences sharedPreferences = context.getSharedPreferences(
					"servicestate", Context.MODE_PRIVATE);
			mWidgetEnable = sharedPreferences.getBoolean("timewidgetexsist",
					false);
			// intent.setAction("com.android.deskclock.GUARDIAN");
			// intent.setPackage(context.getPackageName());
			// context.startService(intent);
			if (mWidgetEnable) {

				Intent intent2 = new Intent("com.android.deskclock.GUARDIAN");
				intent2.setPackage(context.getPackageName());
				context.startService(intent2);
			} else {
				flag = false;
				if (mDoubleKillThread != null && mDoubleKillThread.getFlag() == true) {
					mDoubleKillThread.setFlag(flag);
				}
			}

			// Log.d("cjslog", "boot_complete");
			return;
		}

		if (action.equals("com.android.deskclock.TIMEWIDGETENABLE")) {
			Intent intent2 = new Intent("com.android.deskclock.GUARDIAN");
			intent2.setPackage(context.getPackageName());
			context.startService(intent2);
		}

		if (action.equals("com.android.deskclock.TIMEWIDGETDISABLED")) {
			mWidgetEnable = false;
			SharedPreferences sharedPreferences = context.getSharedPreferences(
					"servicestate", Context.MODE_PRIVATE);
			Editor editor = sharedPreferences.edit();
			editor.putBoolean("timewidgetexsist", false);
			editor.apply();
			return;
		}

		// mWidgetEnable = intent.getBooleanExtra("widgetenable", true);
		// Log.d("cjslog", "onReceive323232 " + action + " " + mWidgetEnable);
		if (action.equals("com.android.widget.WIDGET_TIME_CHANGE")) {
			Log.d("cjslog", "WIDGET_TIME_CHANGE");
			mWidgetEnable = intent.getBooleanExtra("widgetenable", true);
			flag = false;
			if (mDoubleKillThread != null && mDoubleKillThread.getFlag() == true) {
				Log.d("cjslog", "setFlag " + flag);
				mDoubleKillThread.setFlag(flag);
			}
			return;
		}

		// Log.d("cjslog", "time widget exsist " + mWidgetEnable);
		if (mWidgetEnable) {
			
			/*
			 * mHandler.postDelayed(new Runnable() {
			 * 
			 * @Override public void run() { Log.d("cjslog", "flag " + flag);
			 * 
			 * if (flag) { Log.d("cjslog", "double dead"); Intent intent = new
			 * Intent("com.android.deskclock.GUARDIAN");
			 * intent.setPackage(context.getPackageName());
			 * context.startService(intent); } } }, 61500);
			 */
			// mDoubleKillThread = new DoubleKillThread(context, intent, flag);
			// Log.d("cjslog", "mDoubleKillThread " +
			// mDoubleKillThread.getState().toString());
			if (!mDoubleKillThread.getPostToQueue()) {
				boolean succeed = mHandler.postDelayed(mDoubleKillThread, 61500);
				mDoubleKillThread.setPostToQueue(succeed);
			}

			// Log.d("cjslog", "mDoubleKillThread1 " +
			// mDoubleKillThread.getState().toString());

		}

	}

	private static class DoubleKillThread extends Thread {

		private Context mContext;
		private boolean mFlag;
		private boolean mPostToQueue = false;

		private DoubleKillThread(Context context, boolean flag) {
			// TODO Auto-generated constructor stub
			super();
			mContext = context;

			mFlag = flag;
		}

		private static DoubleKillThread mSingleInstance = null;

		public static DoubleKillThread getInstance(Context context, boolean flag) {
			if (mSingleInstance == null) {
				mSingleInstance = new DoubleKillThread(context, flag);
			}
			return mSingleInstance;
		}

		public void setFlag(boolean flag) {
			mFlag = flag;
		}
		
		public boolean getFlag(){
			return mFlag;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.d("cjslog", "flag " + mFlag);
			if (mFlag) {
				Log.d("cjslog", "double dead");
				Intent intent = new Intent("com.android.deskclock.GUARDIAN");
				intent.setPackage(mContext.getPackageName());
				mContext.startService(intent);
			}
			mPostToQueue = false;
		}
		
		public void setPostToQueue(boolean succeed){
			mPostToQueue = succeed;
		}
		
		public boolean getPostToQueue(){
			return mPostToQueue;
		}

	}

}
