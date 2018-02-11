package com.aurora.puremanager.service;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;

import com.aurora.puremanager.model.AutoSleepModel;
import com.aurora.puremanager.utils.AppStateUtil;
import com.aurora.puremanager.utils.LogUtils;

import java.util.HashSet;
import java.util.List;

public class AutoSleepService extends Service {
    private static final String TAG = "AutoSleepService";
    private static final long AUTO_CLEAN_TRIGGER_TIME = 5 * 60 * 1000;
    private static final String ACTION_AUTO_SLEEP = "com.aurora.puremanager.action.AUTO_SLEEP";
    private boolean initFlag = false;
    private Context mContext;
    private AlarmManager mAlarmManager = null;
    private AutoSleepReceiver mAutoSleepReceiver;
    private PendingIntent mAutoSleepIntent = null;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.printWithLogCat(TAG, "onCreate");

        mContext = AutoSleepService.this;
        mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.printWithLogCat(TAG, "onStartCommand");

        if (!initFlag) {
            registerIntentFilterReceiver();
            initFlag = true;
        }

        return Service.START_STICKY;
    }

    private void registerIntentFilterReceiver() {
        mAutoSleepReceiver = new AutoSleepReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(AutoSleepService.ACTION_AUTO_SLEEP);
        mContext.registerReceiver(mAutoSleepReceiver, filter);
    }

    private void triggerByScreenOnIntent() {
        cancelScrenOffAutoSleepAlarmManager();
    }

    public void cancelScrenOffAutoSleepAlarmManager() {
        if (mAlarmManager != null && mAutoSleepIntent != null) {
            mAlarmManager.cancel(mAutoSleepIntent);
        }
    }

    private void triggerByScreenOffIntent() {
        setScrenOffAutoSleepAlarmManager();
    }

    private void setScrenOffAutoSleepAlarmManager() {
        Intent timeoutIntent = new Intent();
        timeoutIntent.setAction(AutoSleepService.ACTION_AUTO_SLEEP);
        mAutoSleepIntent = PendingIntent.getBroadcast(this, 0, timeoutIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        if (mAutoSleepIntent != null) {
            mAlarmManager.cancel(mAutoSleepIntent);
            mAlarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()
                    + AUTO_CLEAN_TRIGGER_TIME, mAutoSleepIntent);
        } else {
            RuntimeException e = new RuntimeException("here");
            e.fillInStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LogUtils.printWithLogCat(TAG, "onDestroy");
    }

    private class AutoSleepReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_SCREEN_ON)) {
                LogUtils.printWithLogCat(TAG, "ACTION_SCREEN_ON");
                triggerByScreenOnIntent();
            } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                LogUtils.printWithLogCat(TAG, "ACTION_SCREEN_OFF");
                triggerByScreenOffIntent();
            } else if (action.equals(AutoSleepService.ACTION_AUTO_SLEEP)) {
                LogUtils.printWithLogCat(TAG, "ACTION_AUTO_SLEEP");
                new AutoSleepThread().start();
            } else {
                //do nothing
            }
        }
    }

    class AutoSleepThread extends Thread {
        AutoSleepThread() {
        }

        @Override
        public void run() {
            ActivityManager am =
                    (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();

            String inputMethod = null;
            if (AppStateUtil.getDefInputMethod(mContext) != null) {
                inputMethod = AppStateUtil.getDefInputMethod(mContext).getPackageName();
            }

            List<String> musicApps = null;
            if (AppStateUtil.isPlayMusic(mContext)) {
                musicApps = AppStateUtil.getMusicApps(mContext);
            }

            List<String> fmApps = null;
            if (AppStateUtil.isFmOn(mContext)) {
                fmApps = AppStateUtil.getFMApps(mContext);
            }

            HashSet<String> autoSleepOpenApps = AutoSleepModel.getInstance(mContext).getAutoSleepOpenApp();
            for (ActivityManager.RunningAppProcessInfo runinfo : runningAppProcesses) {
                for (String pkgName : runinfo.pkgList) {
                    if (inputMethod != null && pkgName.equalsIgnoreCase(inputMethod)) {
                        continue;
                    }

                    if (musicApps != null && musicApps.contains(pkgName)) {
                        continue;
                    }

                    if (fmApps != null && fmApps.contains(pkgName)) {
                        continue;
                    }

                    LogUtils.printWithLogCat(TAG, "running->" + pkgName);
                    if (autoSleepOpenApps.contains(pkgName)) {
                        am.forceStopPackage(pkgName);
                        LogUtils.printWithLogCat(TAG, "kill ->" + pkgName);
                    }
                }
            }
        }
    }
}
