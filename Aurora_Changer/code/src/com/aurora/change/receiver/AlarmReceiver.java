package com.aurora.change.receiver;

import com.aurora.change.utils.Consts;
import com.aurora.change.utils.FileHelper;
import com.aurora.change.utils.WallpaperUtil;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver{
	
	private static final String TAG = "AlarmReceiver";
	public static final String ACTION_INTENT_ALARM = "com.aurora.change.alarm.action";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (ACTION_INTENT_ALARM.equals(intent.getAction())) {
			Log.d(ChangeReceiver.LOCK_TAG, TAG + " -->receiver = ACTION_INTENT_ALARM");
			new MyAsyncTask().execute(context);
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
        Log.d(ChangeReceiver.LOCK_TAG, TAG + " refreshData = "+path);
        if (path == null) {
            return bool;
        }
        
        //M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 start
//      bool = FileHelper.copyFile(path, Consts.LOCKSCREEN_WALLPAPER_PATH);
        bool = FileHelper.copyFile(path, Consts.LOCKSCREEN_WALLPAPER_PATH, context);
        //M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 end
      
        setNextAlarmManager(context);
        return bool;
    }

    private class MyAsyncTask extends AsyncTask<Context, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Context... params) {
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
            long nextTime = WallpaperUtil.getNextTime();
            long firstTime = System.currentTimeMillis();
            Log.d(ChangeReceiver.LOCK_TAG, TAG+" firstTime=" + firstTime + ",nextTime=" + nextTime);
            alarmManager.cancel(refreshIntent);
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
            Log.d(ChangeReceiver.LOCK_TAG, TAG+" pending intent is null");
        }
    }
}
