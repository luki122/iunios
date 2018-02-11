//Gionee <jianghuan> <2013-09-29> add for CR00975553 begin
package com.aurora.puremanager.traffic;

import java.util.List;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.aurora.puremanager.R;
import com.aurora.puremanager.utils.mConfig;

public class TrafficProcessorService extends Service {

    private static final String serviceName = "com.aurora.puremanager.traffic.TrafficProcessorService";
    private static final int MSG_STOP = 0;
    private static final int MSG_WARNING = 1;
    private static final int MSG_SETTING = 2;
    private static final int MSG_START_RUNNABLE = 3;
    private static final int MSG_START_RUNNABLE1 = 4;
    private static final int MSG_START_RUNNABLE2 = 5;
    private static boolean isRunning = true;
    private int mActivatedSimIndex;
    private Context mContext;
    private Handler mHandler;
    private SharedPreferences mShare;
    
    private static final String TAG = "TrafficProcessorService";
    private static final int DELAY_TIME = 60000;
    private boolean mSettingFlag = true;
    private int mTimeZone = 0;
    private boolean mStopFlag = true;
    private boolean mWarningStopFlag = false;
    
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub

        terminateThread();
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        Log.i(TAG, "TrafficProcessorService onCreate()");
        startThread();

        mContext = TrafficProcessorService.this;

        SIMInfoWrapper simInfo = SIMInfoWrapper.getDefault(mContext);
        mActivatedSimIndex = simInfo.getSimIndex_CurrentNetworkActivated();
        if (!isValid(simInfo, mActivatedSimIndex)) {
            return;
        }
        
        mShare =  PreferenceManager.getDefaultSharedPreferences(mContext);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_STOP:

                        Intent intent = new Intent(mContext, TrafficPopWindows.class);
                        intent.putExtra("sim_activatedindex", mActivatedSimIndex);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                        break;

                    case MSG_WARNING:

                        NotificationController notification = NotificationController.getDefault(mContext);
                        notification.setSimIndex(mActivatedSimIndex);
                        notification.setSoundId(false);
                        notification.setTitle(R.string.notification_warn_exceed_warning_title);
                        notification.setContent(R.string.notification_warn_exceed_warning_content);
                        notification.setTickerText(R.string.notification_warn_exceed_warning_title);
                        notification.setSmallIcon(R.drawable.ic_launcher);
                        notification.setClass(com.aurora.puremanager.traffic.TrafficAssistantMainActivity.class);
                        notification.show(notification.getWarningId());
                        break;

                    case MSG_SETTING:
                        NotificationController notification1 = NotificationController.getDefault(mContext);
                        notification1.setSimIndex(mActivatedSimIndex);
                        notification1.setSoundId(false);
                        notification1.setTitle(R.string.popup_notification_title);
                        notification1.setContent(R.string.popup_notification_ticker_title);
                        notification1.setSmallIcon(R.drawable.ic_launcher);
                        notification1.setTickerText(R.string.popup_notification_title);
                        notification1.setClass(com.aurora.puremanager.traffic.TrafficLimitActivity.class);
                        notification1.show(notification1.getBootedId());
                        break;
                    case MSG_START_RUNNABLE:
                        startRunnable();
                        break;
                    case MSG_START_RUNNABLE1:
                        startRunnable1();
                        break;
                    case MSG_START_RUNNABLE2:
                        startRunnable2();
                        break;
                    default:
                        break;
                }
            }
        };
        
        if (!getTrafficSettings(mShare, mActivatedSimIndex)) {
            mSettingFlag = true;
            mHandler.sendEmptyMessage(MSG_START_RUNNABLE1);
        } else if (getMonitorFlag(mShare, mActivatedSimIndex)) {
            int mTimeZone = 0;
            mHandler.sendEmptyMessage(MSG_START_RUNNABLE2);
        } else {
            mStopFlag = true;
            mWarningStopFlag = false;
            mHandler.sendEmptyMessage(MSG_START_RUNNABLE);
        }
    }
    
    private void startRunnable1() {
        if (mContext == null) {
            return;
        }
        try {
            if (isRunning) {
                int[] date = TrafficAssistantMainActivity.initDateInterval(1);
                String str = TrafficPreference.getSimNotification(mActivatedSimIndex);
                float actualFlow  = TrafficAssistantMainActivity
                        .trafficStatistic(mContext, date, mActivatedSimIndex);

                if (getTrafficSettings(mShare, mActivatedSimIndex)) {
                    return;
                }

                if (actualFlow >= 1 && mSettingFlag) {
//                    sendMessage(MSG_SETTING);
                    mSettingFlag = false;
                }

//                TrafficSettingsPrefsFragment.commitTrafficNotiAction(mContext);

                mHandler.sendEmptyMessageDelayed(MSG_START_RUNNABLE1, DELAY_TIME);
            }
        } catch (Exception ex) {
            Log.e(TAG, "startRunnable1", ex);
        }
    }

    private void startRunnable2() {
        if (mContext == null) {
            return;
        }
        try {
            if (isRunning) {
                mTimeZone++;
                if (mTimeZone >= 6) {
//                    TrafficSettingsPrefsFragment.commitTrafficNotiAction(mContext);
                    mTimeZone = 0;
                }
                mHandler.sendEmptyMessageDelayed(MSG_START_RUNNABLE2, DELAY_TIME/20);
            }
        } catch (Exception ex) {
            Log.e(TAG, "startRunnable2", ex);
        }
    }

    private void startRunnable() {
        if (mContext == null) {
            return;
        }
        try {
            if (isRunning) {
                String[] data = TrafficPreference.getPreference(mContext, mActivatedSimIndex);

                int[] date = TrafficAssistantMainActivity.initDateInterval(Integer.valueOf(data[2]));
                float definedFlow = Float.valueOf(data[3]), actualFlow = 0;
                boolean isWarning = false, isStop = false;
                String stopWarningFlag = TrafficPreference.getSimStopWarning(mActivatedSimIndex);

                actualFlow = TrafficAssistantMainActivity
                        .trafficStatistic(mContext, date, mActivatedSimIndex);

                isStop = (actualFlow + definedFlow >= Float.valueOf(data[0]));
                print("runnable", " isStop : " + isStop + " stopFlag : " + mStopFlag);
                if (isStop && mStopFlag) {
                    sendMessage(MSG_STOP);
                    mStopFlag = false;
                }

                isWarning = ((actualFlow + definedFlow) >= ((double) (Integer.valueOf(data[0]) * Integer
                        .valueOf(data[1]))) / 100);
                if (!mWarningStopFlag && isWarning && !isStop) {
                	
                    if (!mShare.getBoolean(stopWarningFlag, false)) {
                        sendMessage(MSG_WARNING);
                    }

                    if (!mShare.getBoolean(stopWarningFlag, false)) {
                        mShare.edit().putBoolean(stopWarningFlag, true).commit();
                        mWarningStopFlag = true;
                    }
                }
//                TrafficSettingsPrefsFragment.commitTrafficNotiAction(mContext);
                print("runnable", actualFlow + " " + definedFlow + " " + Float.valueOf(data[0]));
                mHandler.sendEmptyMessageDelayed(MSG_START_RUNNABLE, DELAY_TIME);
            }

        } catch (Exception ex) {
            Log.e(TAG, "startRunnable", ex);
        }
    }

    private boolean getTrafficSettings(SharedPreferences share, int simIndex) {
        boolean settings = share.getBoolean(TrafficPreference.getSimSetting(simIndex), false);
        return settings;
    }

    private boolean getMonitorFlag(SharedPreferences share, int simIndex) {
        int value = share.getInt(TrafficPreference.getSimFlowlinkFlag(simIndex), 0);
        return (value != 0) ? true : false;
    }

    private void sendMessage(int message) {
        Message m = new Message();
        m.what = message;
        mHandler.sendMessage(m);
    }

    private void print(String symbol, String value) {
        int[] times = TimeFormat.getNowTimeArray();
        String time = times[3] + ":" + times[4] + ":" + times[5];
        Debuger.print(symbol + "-->" + time + " " + isRunning + " , " + value);
    }

    private static void startThread() {
        isRunning = true;
    }

    private static void terminateThread() {
        isRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        return super.onStartCommand(intent, flags, startId);
    }

    public static void processIntent(Context context, boolean flag) {

        if (flag) {
            startIntent(context);
        } else {
            stopIntent(context);
        }

    }

    private static void startIntent(Context context) {

        if (!isServiceRunning(context, serviceName)) {
            Debuger.print(" start service");
            waitTime(3000);

            Intent i = new Intent(context, TrafficProcessorService.class);
            context.startService(i);
        } else {
            Debuger.print(" service is already start");
        }
    }

    private static void stopIntent(Context context) {

        if (isServiceRunning(context, serviceName)) {
            Debuger.print(" stop service");

            waitTime(3000);
            Intent i = new Intent(context, TrafficProcessorService.class);
            context.stopService(i);
        } else {
            Debuger.print(" service is already stop ");
        }
    }

    private boolean isValid(SIMInfoWrapper simInfo, int simIndex) {
        return (simIndex > -1) && !simInfo.isWiFiActived()
                && simInfo.gprsIsOpenMethod("getMobileDataEnabled");
    }

    

    private static boolean isServiceRunning(Context context, String className) {
        boolean serviceRunning = false;
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(100);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {

            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                Debuger.print(serviceList.get(i).service.getClassName().toString());
                serviceRunning = true;
                break;
            }
        }

        return serviceRunning;
    }

    private static void waitTime(long ms) {
        Object obj = new Object();
        synchronized (obj) {
            try {
                obj.wait(ms);
            } catch (Exception e) {
                Log.i("TrafficProcessorService", " obj.wait ", e);
            }
        }
    }
}
// Gionee <jianghuan> <2013-09-29> add for CR00975553 end