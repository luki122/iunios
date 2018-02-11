//package com.aurora.change.services;
//
//import com.aurora.change.data.AdapterDataFactory;
//import com.aurora.change.receiver.ChangeReceiver;
//import com.aurora.change.utils.Consts;
//import com.aurora.change.utils.FileHelper;
//import com.aurora.change.utils.WallpaperUtil;
//
//import android.app.AlarmManager;
//import android.app.PendingIntent;
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.os.AsyncTask;
//import android.os.IBinder;
//import android.os.SystemClock;
//import android.util.Log;
//
//public class ChangeServices extends Service {
//
//	private static final String TAG = "ChangeServices";
//    private Context mContext;
//    private AlarmManager mAlarmManager;
//    private PendingIntent mRefreshIntent;
//    private static final int TRIGGER_TIME = 3 * 60 * 1000; //三分钟后启动服务
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        mContext = this;
//        mAlarmManager = ( AlarmManager ) mContext.getSystemService(Context.ALARM_SERVICE);
//        AdapterDataFactory adapterDataFactory = new AdapterDataFactory(mContext,
//                Consts.WALLPAPER_LOCKSCREEN_TYPE);
//        adapterDataFactory.initWallpaperItems();
//        adapterDataFactory.clearData();
//        Log.d(ChangeReceiver.LOCK_TAG, TAG+" onCreate");
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        setAlarmManager();
//        Log.d(ChangeReceiver.LOCK_TAG, TAG+" onStartCommand");
//        new MyAsyncTask().execute(mContext);
//        return Service.START_STICKY;
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//    }
//
//    private void setAlarmManager() {
//        Intent intent = new Intent(mContext, ChangeServices.class);
//        mRefreshIntent = PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//        if (mRefreshIntent != null) {
//            long firstTime = System.currentTimeMillis();
//            mAlarmManager.cancel(mRefreshIntent);
//            long nextTime = WallpaperUtil.getNextTime();
//            if (nextTime <= firstTime) {
//                mAlarmManager.set(AlarmManager.RTC_WAKEUP, firstTime + TRIGGER_TIME, mRefreshIntent);
//            } else {
//                mAlarmManager.set(AlarmManager.RTC_WAKEUP, nextTime, mRefreshIntent);
//            }
//            Log.d(ChangeReceiver.LOCK_TAG, TAG+" setAlarmManager=" + nextTime);
//        } else {
//            RuntimeException e = new RuntimeException("here");
//            e.fillInStackTrace();
//            Log.d(ChangeReceiver.LOCK_TAG, TAG+" pending intent is null");
//        }
//    }
//
//    private class MyAsyncTask extends AsyncTask<Context, Integer, Boolean> {
//
//        @Override
//        protected Boolean doInBackground(Context... params) {
//            return refreshData(params[0]);
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            super.onPostExecute(result);
//        }
//    }
//
//    private boolean refreshData(Context context) {
//        boolean bool = false;
//        if (WallpaperUtil.isReallyModify()) {
//            String path = "";
//            try {
//                path = WallpaperUtil.getNextLockPaperPath(context);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            Log.d(ChangeReceiver.LOCK_TAG, TAG+" refreshData = "+path);
//            bool = FileHelper.copyFile(path, Consts.LOCKSCREEN_WALLPAPER_PATH);
//        }
//        return bool;
//    }
//
//    /*private void setNextAlarmManager() {
//        Intent intent = new Intent(mContext, ChangeReceiver.class);
//        intent.setAction(Consts.ACTION_COPY_FILE);
//        mRefreshIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//        if (mRefreshIntent != null) {
//            long firstTime = SystemClock.elapsedRealtime();
//            long nextTime = WallpaperUtil.getNextTime();
//            mAlarmManager.cancel(mRefreshIntent);
//            mAlarmManager.set(AlarmManager.RTC_WAKEUP, nextTime, mRefreshIntent);
//            Log.d("ChangeServices", "setAlarmManager");
//        } else {
//            RuntimeException e = new RuntimeException("here");
//            e.fillInStackTrace();
//            Log.d("ChangeServices", "pending intent is null");
//        }
//    }*/
//
//}
