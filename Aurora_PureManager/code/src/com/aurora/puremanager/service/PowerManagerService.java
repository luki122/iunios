package com.aurora.puremanager.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.format.DateFormat;

import com.aurora.puremanager.utils.Consts;
import com.aurora.puremanager.utils.Debug;
import com.aurora.puremanager.utils.Log;
import com.aurora.puremanager.utils.ModeChangeController;
import com.aurora.puremanager.utils.ProviderHelper;

public class PowerManagerService extends Service {
    private Context mContext;
    private ProviderHelper mProviderHelper;
    private MyIntentReceiver mIntentMonitorReceiver;
    private AlarmManager mAlarmManager = null;
    private PendingIntent mPendingIntent = null;
    private static final String TAG = "PowerManagerService";
    private static final boolean DEBUG = true;
    private static final int EVENT_KILL_PROCESS = 1;
    private static final int KILL_PROCESS_DELAY_TIME = 500;

    private int mPreviousMode;

    private boolean mRegistedFlag = false;

    private static final String SAVING_SERVICE_INTENT_ACTION = "com.opera.max.sdk.saving.SavingService";
    private static final String SAVING_SERVICE_CLASS_NAME = "com.opera.max.sdk.saving.SavingService";
    private static final String EXTRA_SHOW_NOTIFICATION = "EXTRA_SHOW_NOTIFICATION";
    private static final String EXTRA_HIDE_NOTIFICATION_AFTER_SAVING_OFF = "EXTRA_HIDE_NOTIFICATION_AFTER_SAVING_OFF";
    private final String[] pkgList = {"com.oupeng.max.sdk", "com.trafficctr.miui"};
    private String mCurrentPackageName = pkgList[0];

    @Override
    public void onCreate() {
        mContext = PowerManagerService.this;
        mProviderHelper = new ProviderHelper(mContext);
        mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
    }

    class ModeChangeThread extends Thread {
        private int from;
        private int to;

        ModeChangeThread(int from, int to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public void run() {
            Log.e(TAG, "ModeChangeThread$run");
            ModeChangeController mcc = new ModeChangeController(mContext);
            mcc.savePreviousMode(from);
            mcc.startCurrentMode(from, to);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        parserModeChangeIntent(intent);
        return Service.START_STICKY;
    }

    private void parserModeChangeIntent(Intent intent) {
        Log.e(TAG, "parserModeChangeIntent");
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        if (action.equals(Consts.MODE_CHANGE_INTENT)) {
            Bundle bundle = intent.getExtras();
            int from = bundle.getInt("from");
            int to = bundle.getInt("to");
            ModeChangeThread thread = (new ModeChangeThread(from, to));
            //thread.setPriority(Thread.MAX_PRIORITY);
            thread.start();
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_KILL_PROCESS:
                    int processId = ((Integer) msg.obj).intValue();
                    killBackgroundProcesses(processId);
                    break;
                default:
                    break;
            }
        }
    };

    private class MyIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_SCREEN_ON)) {
                triggerByScreenOnIntent();
            } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                triggerByScreenOffIntent();
            }
        }
    }

    private void triggerByScreenOnIntent() {
    }

    private void showToast(int cleanMemNumber) {
    }

    private void triggerByScreenOffIntent() {

    }

    private void triggerByKillProcessIntent(Intent intent) {
        // System.gc();
        int processId = intent.getIntExtra("processid", -1);
        // killBackgroundProcesses(processId);
        Message msg = Message.obtain();
        msg.what = EVENT_KILL_PROCESS;
        msg.obj = Integer.valueOf(processId);
        mHandler.sendMessageDelayed(msg, KILL_PROCESS_DELAY_TIME);
    }

    private void triggerByAutoCleanMemIntent() {

    }

    private void triggerByLowMemCleanIntent(Context context) {

    }

    /*可能已废弃不用，后续查证*/
    private void triggerByCleanByCleanButton(Intent intent) {
        String stauts = intent.getStringExtra("status");
        if (stauts == null) {
            return;
        }
        if ("true".equals(stauts)) {
            setLowMemCleanAlarmManager();
        } else {
            cancelLowMemCleanAlarmManager();
        }
    }

    private PendingIntent mAutoCleanIntent = null;
    private PendingIntent mLowMemCleanIntent = null;
    private PendingIntent mAutoEnterPowerSaverIntent = null;
    private static final long AUTO_CLEAN_TRIGGER_TIME = 2 * 60 * 1000;
    private static final long LOW_MEM_CLEAN_TRIGGER_TIME = 5 * 60 * 1000;
    private static final long NIGHT_AUTO_ENTER_TIMER = 3 * 60 * 60 * 1000;
    private static final long MIDNIGHT_AUTO_ENTER_TIMER = 2 * 60 * 60 * 1000;
    private static final long WEE_HOURS_AUTO_ENTER_TIMER = 30 * 60 * 1000;

    private static final int NIGHT_TIME = 1;
    private static final int MIDNIGHT_TIME = 2;
    private static final int WEE_HOURS_TIME = 3;

    /*
     * 灭屏十分钟后自动清理后台应用
     */
    public void setAutoCleanAlarmManager() {

    }

    /*
     * 夜间（22点至凌晨3点）灭屏两小时之后关闭耗电项，亮屏后恢复。
     */
    public void setAutoEnterPowerSaver() {

    }

    /*
     * 晚 8点 到 10点之间，返回 1；此时3小时后关闭所有耗电项 晚 10点 到 12点之间，返回 2；此时2小时后关闭所有耗电项 凌晨 0点 到
     * 3点之间，返回 3； 此时半小时后关闭所有耗电项 其它时间返回 0，不做任何处理
     */
    private int getNightTime() {
        long currentTime = System.currentTimeMillis();
        String sHour = DateFormat.format("kk", currentTime).toString();

        Long lHour = Long.valueOf(sHour);
        if (lHour >= 20 && lHour < 22) {
            return NIGHT_TIME;
        } else if (lHour >= 22 && lHour < 24) {
            return MIDNIGHT_TIME;
        } else if (lHour >= 0 && lHour <= 3) {
            return WEE_HOURS_TIME;
        }
        return 0;
    }

    private void killBackgroundProcesses(int processId) {
        try {
            //ExecuteAsRoot.execute("kill -9 " + processId);
        } catch (Exception e) {
            Debug.log(DEBUG, TAG, "killBackgroundProcesses throw exception");
            return;
        }
    }

    /*
     * 灭屏2分钟后清理后台应用
     */
    public void setScrenOffAutoCleanAlarmManager() {

    }

    /*
     * 内存低于20%清理后台应用
     */
    public void setLowMemCleanAlarmManager() {

    }

    public void cancelLowMemCleanAlarmManager() {
        if (mAlarmManager != null && mLowMemCleanIntent != null) {
            mAlarmManager.cancel(mLowMemCleanIntent);
        }
    }

    public void cancelScrenOffAutoCleanAlarmManager() {
        if (mAlarmManager != null && mAutoCleanIntent != null) {
            mAlarmManager.cancel(mAutoCleanIntent);
        }
    }

    private Intent getSavingServiceIntent() {
        Intent intent = new Intent(SAVING_SERVICE_INTENT_ACTION);
        intent.setClassName(mCurrentPackageName, SAVING_SERVICE_CLASS_NAME);
        intent.putExtra(EXTRA_SHOW_NOTIFICATION, false);
        intent.putExtra(EXTRA_HIDE_NOTIFICATION_AFTER_SAVING_OFF, false);
        return intent;
    }

    @Override
    public void onDestroy() {
        mContext.unregisterReceiver(mIntentMonitorReceiver);
        mRegistedFlag = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
