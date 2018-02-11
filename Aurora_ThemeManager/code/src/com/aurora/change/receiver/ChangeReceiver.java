package com.aurora.change.receiver;

import com.aurora.change.data.WallpaperValue;
import com.aurora.change.utils.CommonUtil;
import com.aurora.change.utils.Consts;
import com.aurora.change.utils.FileHelper;
import com.aurora.change.utils.WallpaperConfigUtil;
import com.aurora.change.utils.WallpaperUtil;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

// Aurora liugj 2014-05-20 modified for bug-6857
public class ChangeReceiver extends BroadcastReceiver {
	
	public static final String LOCK_TAG = "LockpaperChange";
	private static final String TAG = "ChangeReceiver";
	private static final String ACTION_CHMOD_FILE = "com.aurora.change.CHMOD_FILE";
	private static final String ACTION_RESET_ALARM = "com.aurora.thememanager.RESET_ALARM";

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(LOCK_TAG, TAG + " action=" + action);
        Log.e("101010", "---ChangeReceiver action = -----" + action);
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            //Log.d(LOCK_TAG, TAG + "-->receiver = ACTION_BOOT_COMPLETED");
			  // Aurora liugj 2014-09-01 modified for bug-6857 start
            /*new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    new MyAsyncTask().execute(context);
                }
            }, 1000 * 60 * 2);*/
            setNextAlarmManager(context);
			 // Aurora liugj 2014-10-09 add for S4 4.4 file permission start
            CommonUtil.chmodFile(FileHelper.PATH_DATA_AURORA);
			CommonUtil.chmodFile(FileHelper.PATH_DATA_AURORA_CHANGE);
			CommonUtil.chmodFile(FileHelper.PATH_DATA_AURORA_CHANGE_LOCKSCREEN);
			// Aurora liugj 2014-10-09 add for S4 4.4 file permission end
            refreshCurrentData(context);
			  // Aurora liugj 2014-09-01 modified for bug-6857 end
        } else if (action.equals(Consts.ACTION_COPY_FILE)) {
        	//Log.d(LOCK_TAG, TAG + " receiver = ACTION_COPY_FILE");
            new MyAsyncTask().execute(context);
        } else if (action.equals(Intent.ACTION_TIME_CHANGED) || action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
        	//Log.d(LOCK_TAG, TAG + " receiver = ACTION_TIME_CHANGED");
        	new MyAsyncTask().execute(context);
            setNextAlarmManager(context);
        } else if (action.equals(ACTION_RESET_ALARM)) {
        	setNextAlarmManager(context);
        	//shigq add start
        	WallpaperConfigUtil.checkNextDayWallpaperSetting(context);
        	//shigq add end
        	
		} else if (action.equals(ACTION_CHMOD_FILE)) {
        	//Log.d(LOCK_TAG, TAG + " receiver = com.aurora.change.chmodfile");
        	//CommonUtil.chmodFile(FileHelper.PATH_DATA_AURORA_CHANGE);
			//CommonUtil.chmodFile(FileHelper.PATH_DATA_AURORA_CHANGE_LOCKSCREEN);
			CommonUtil.chmodFile(Consts.LOCKSCREEN_WALLPAPER_PATH);
		} else if (action.equals(Intent.ACTION_WALLPAPER_CHANGED)) {
			Log.e("101010", "---Intent.ACTION_WALLPAPER_CHANGED-----");
            Consts.isWallPaperChanged = true;
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            if (Consts.isChangedByLocal == 0 && (!sp.getString("selectpath", "-1").equals("-1") || sp.getInt("selectpos", -1) != -1)) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("selectpath", "-1");
                editor.putInt("selectpos", -1);
                editor.commit();
                Consts.isChangedByLocal = 1;
            }
        } else if (action.equals(WallpaperValue.ACTION_WALLPAPER_SET)) {
        	Log.e("101010", "---WallpaperValue.ACTION_WALLPAPER_SET-----");
            Consts.isChangedByLocal = 2;
            /*SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("selectpos", Consts.LOCAL_WALLPAPERS.length);
            editor.commit();*/
        }
    }

    private boolean refreshCurrentData(Context context) {
        boolean bool = false;
        String path = "";
        try {
            path = WallpaperUtil.getCurrentLockPaperPath(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(LOCK_TAG, TAG + " refreshData = "+path);
        if (path == null) {
            return bool;
        }
        
        //M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 start
//      bool = FileHelper.copyFile(path, Consts.LOCKSCREEN_WALLPAPER_PATH);
        bool = FileHelper.copyFile(path, Consts.LOCKSCREEN_WALLPAPER_PATH, context);
        //M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 end
        
        return bool;
    }

    private class MyAsyncTask extends AsyncTask<Context, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Context... params) {
        	Log.d(LOCK_TAG, TAG + "-->MyAsyncTask ：doInBackground");
            return refreshCurrentData(params[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
        }
    }

    private void setNextAlarmManager(Context context) {
        Intent intent = new Intent(AlarmReceiver.ACTION_INTENT_ALARM);
        PendingIntent refreshIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        if (refreshIntent != null) {
            AlarmManager alarmManager = ( AlarmManager ) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(refreshIntent);
            long nextTime = WallpaperUtil.getNextTime();
            long firstTime = System.currentTimeMillis();
            Log.d(LOCK_TAG, TAG+" firstTime=" + firstTime + ",nextTime=" + nextTime);
            if (nextTime <= firstTime) {
                //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, firstTime + 5 * 1000, 1000 * 60 * 60 * 2, refreshIntent);
            	alarmManager.set(AlarmManager.RTC_WAKEUP, firstTime + 5 * 1000, refreshIntent);
            } else {
                //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, nextTime, 1000 * 60 * 60 * 2, refreshIntent);
            	alarmManager.set(AlarmManager.RTC_WAKEUP, nextTime, refreshIntent);
            }
        } else {
            /*RuntimeException e = new RuntimeException("here");
            e.fillInStackTrace();*/
            Log.d(LOCK_TAG, TAG+" pending intent is null");
        }
    }
}
