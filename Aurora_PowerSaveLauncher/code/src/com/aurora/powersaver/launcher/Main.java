package com.aurora.powersaver.launcher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BatteryStats;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.CallLog;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.internal.app.IBatteryStats;
import com.aurora.powersaver.launcher.util.BatteryStateHelper;
import com.aurora.powersaver.launcher.util.CommonUtil;
import com.aurora.powersaver.launcher.util.DebouncedClickAction;
import com.aurora.powersaver.launcher.util.FrameworkUtility;
import com.aurora.powersaver.launcher.util.PowerModeHelper;

import java.util.Arrays;
import java.util.List;

import aurora.app.AuroraAlertDialog;

public class Main extends Activity implements OnLongClickListener, View.OnClickListener {
    private static final String TAG = "PowerSaverLauncher/Main";
    private static int mStatusBarFlag = StatusBarManager.DISABLE_NONE;
    private static final String BATTERY_PERCENTAGE = "battery_percentage";
    private static final String SETTING_VALUE = "setting_value";
    private static final String IS_FIRST = "is_first";
    private static final String PHONE = "com.android.contacts/.activities.AuroraDialActivity";
    private static final String SMS = "com.android.mms/com.aurora.mms.ui.AuroraConvListActivity";
    private static final String TIME = "com.android.deskclock/.AlarmClock";
    private static final String EXIT_ACTION = "com.action.exit.super.power.save.mode";
    private static final String EXIT_ACTION_CATEGORY = "com.action.exit.super.power.save.mode.category";
    private static final String ACTION_BATTERY_PERCENTAGE_SWITCH = "mediatek.intent.action.BATTERY_PERCENTAGE_SWITCH";
    public final String DISABLE_HANDLER = "com.android.systemui.recent.AURORA_DISABLE_HANDLER";
    public final String ENABLE_HANDLER = "com.android.systemui.recent.AURORA_ENABLE_HANDLER";
    private static final int TASK_MAX = 50;
    private static int SMS_NUM = 0;
    private static int CALL_NUM = 0;

    private RelativeLayout mPhoneView;
    private RelativeLayout mSmsView;
    private RelativeLayout mTimeView;
    private Context mContext;
    private Resources mRes;
    private LinearLayout mExitView;
    private TextView mTimeTextView;
    private CommonUtil mCommonUtil;
    private int mSettingValue;
    private SharedPreferences mSettingValuePreferences;
    private IBatteryStats mBatteryInfo;
    private List<String> mTaskWhiteList;
    private TelephonyManager mTelManager;
    private WifiManager mWifiManager;

    private final ContentObserver mSmsObserver = new SmsContentObserver();
    private final ContentObserver mPhoneObserver = new PhoneContentObserver();
    private TextView callTx, smsTx;

    private BroadcastReceiver mNetworkReveiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String actionStr = intent.getAction();
            if (actionStr.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                Log.d(TAG, "receive CONNECTIVITY_ACTION disable wifi and dataconnect");
                if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED
                        || mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
                    mWifiManager.setWifiEnabled(false);
                    Log.d(TAG, "mWifiManager.setWifiEnabled ----> false");
                }
                if (mTelManager.getDataEnabled()) {
                    mTelManager.setDataEnabled(false);
                    Log.d(TAG, "mTelManager.setDataEnabled ----> false");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        initData();
        initViews();
        registerIntentReceiver();
        registerObserver();
        if (PowerModeHelper.getCurrentMode(this) == PowerModeHelper.SUPER_MODE) {
            sendBroadcastToChameleon();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        enableStatusBar(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //setBatteryDisPlayMode(2);
        //enableStatusBar(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void registerObserver() {
        //getContentResolver().registerContentObserver(Uri.parse("content://sms"), true, mSmsObserver);
        getContentResolver().registerContentObserver(Uri.parse("content://mms-sms/"), true, mSmsObserver);
        getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, mPhoneObserver);
    }

    private void clearTask() {
        final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<RecentTaskInfo> tasks = am.getRecentTasks(Integer.MAX_VALUE,
                ActivityManager.RECENT_WITH_EXCLUDED |
                        ActivityManager.RECENT_IGNORE_UNAVAILABLE |
                        ActivityManager.RECENT_INCLUDE_PROFILES);
        if (tasks != null) {
            for (RecentTaskInfo recentTaskInfo : tasks) {
                am.removeTask(recentTaskInfo.persistentId);
            }
        }
    }

    public void enableStatusBar(boolean enable) {
        Log.e(TAG, "enableStatusBar " + enable);
        StatusBarManager sm = (StatusBarManager) mContext.getSystemService(Context.STATUS_BAR_SERVICE);
        if (enable) {
            sm.disable(StatusBarManager.DISABLE_NONE);
            Intent intent = new Intent();
            intent.setAction(ENABLE_HANDLER);
            sendBroadcastAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
        } else {
            sm.disable(StatusBarManager.DISABLE_EXPAND);
            Intent intent = new Intent();
            intent.setAction(DISABLE_HANDLER);
            sendBroadcastAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
        }
    }

    private void unregisterObserver() {
        getContentResolver().unregisterContentObserver(mSmsObserver);
        getContentResolver().unregisterContentObserver(mPhoneObserver);
    }

    private final int UP_MSG = 0;
    private final int UP_CALL = 1;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UP_MSG:
                    upMsg();
                    break;
                case UP_CALL:
                    upCall();
                    break;
            }
        }
    };

    private void upCall() {
        if (CALL_NUM > 0) {
            callTx.setVisibility(View.VISIBLE);
            callTx.setText(String.valueOf(CALL_NUM));
        } else {
            callTx.setVisibility(View.GONE);
        }
    }

    private void upMsg() {
        if (SMS_NUM > 0) {
            smsTx.setVisibility(View.VISIBLE);
            smsTx.setText(String.valueOf(SMS_NUM));
        } else {
            smsTx.setVisibility(View.GONE);
        }
    }

    private class SmsContentObserver extends ContentObserver {

        public SmsContentObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            Log.i(TAG, "SmsContentObserver , onChange");
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    upSms();
                }
            });
            super.onChange(selfChange);
        }
    }

    private void upSms() {
        UpdateUnreadMmsSms(getApplicationContext());
        Message msg = mHandler.obtainMessage();
        msg.what = UP_MSG;
        mHandler.sendMessage(msg);
    }

    private void upPhone() {
        UpdateUnAnsweredCalls(getApplicationContext());
        Message msg = mHandler.obtainMessage();
        msg.what = UP_CALL;
        mHandler.sendMessage(msg);
    }

    private class PhoneContentObserver extends ContentObserver {

        public PhoneContentObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            Log.i(TAG, "PhoneContentObserver , onChange");
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    upPhone();
                }
            });
            super.onChange(selfChange);
        }
    }

    public static void UpdateUnAnsweredCalls(Context context) {
        Cursor cur = null;

        try {
            cur = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null,
                    "type = 3 and new = 1", null, null);
            if (null != cur) {
                CALL_NUM = cur.getCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cur != null) {
                cur.close();
            }
        }
    }

    public static void UpdateUnreadMmsSms(Context context) {
        Cursor cur = null;
        try {
            cur = context.getContentResolver().query(
                    Uri.parse("content://sms"), null,
                    "type = 1 and read = 0", null, null);
                    /*Uri.parse("content://sms"),
                    null, "type = 1 and read = 0", null, null);*/
            if (null != cur) {
                SMS_NUM = cur.getCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cur != null) {
                cur.close();
            }
        }
    }

    private void initData() {
        mContext = this;
        Settings.System.putString(getContentResolver(), "aurora_power_mode", "super");

        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mTelManager = TelephonyManager.from(mContext);

        mRes = mContext.getResources();
        mBatteryInfo = IBatteryStats.Stub.asInterface(ServiceManager.getService(BatteryStats.SERVICE_NAME));
        mCommonUtil = new CommonUtil(mContext);
        mSettingValuePreferences = mContext.getSharedPreferences(SETTING_VALUE, Context.MODE_PRIVATE);
        if (mSettingValuePreferences.getBoolean(IS_FIRST, true)) {
            mSettingValue = Settings.Secure.getInt(mContext.getContentResolver(), BATTERY_PERCENTAGE, 0);
            mSettingValuePreferences.edit().putInt(SETTING_VALUE, mSettingValue).commit();
            mSettingValuePreferences.edit().putBoolean(IS_FIRST, false).commit();
            String[] whitearray = mContext.getResources().getStringArray(R.array.super_power_save_task);
            mTaskWhiteList = Arrays.asList(whitearray);
            killTask(mContext);
        }
    }

    private void registerIntentReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        mContext.registerReceiver(mBatteryReveiver, filter);
        IntentFilter netFilter = new IntentFilter();
        netFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        netFilter.setPriority(1000);
        //mContext.registerReceiver(mNetworkReveiver, netFilter);
    }

    @Override
    protected void onDestroy() {
        //unregisterReceiver(mBatteryReveiver);
        //unregisterReceiver(mNetworkReveiver);
        //unregisterObserver();
        //enableStatusBar(true);
        super.onDestroy();
    }

    @SuppressLint("NewApi")
    private void initViews() {
        mPhoneView = (RelativeLayout) findViewById(R.id.phone);
        initView(mPhoneView, PHONE);

        mSmsView = (RelativeLayout) findViewById(R.id.sms);
        initView(mSmsView, SMS);

        mTimeView = (RelativeLayout) findViewById(R.id.time);
        initView(mTimeView, TIME);

        mExitView = (LinearLayout) findViewById(R.id.exit_view);
        exitSuperSaveModeClickListener();

        mTimeTextView = (TextView) findViewById(R.id.time_textview);
        updateTimeAlert();
        callTx = (TextView) findViewById(R.id.phone_num);
        smsTx = (TextView) findViewById(R.id.sms_num);
        upSms();
        upPhone();
    }

    private void updateTimeAlert() {
        String str = null;
        int time = 0;
        long timeFromSystem = 0;
        try {
            if (BatteryStateHelper.isChargingNow(mContext)) {
                timeFromSystem = mBatteryInfo.computeChargeTimeRemaining();
                if (timeFromSystem > 0) {
                    str = getResources().getString(R.string.need_charging_time);
                    String formattedTime = Formatter.formatShortElapsedTime(mContext, timeFromSystem);
                    str = str.format(str, formattedTime);
                    mTimeTextView.setText(str);
                } else {
                    if (BatteryStateHelper.getBatteryLevel(mContext) != 100) {
                        str = getResources().getString(R.string.is_charging_now);
                    } else {
                        str = getResources().getString(R.string.charged_completely);
                    }
                    mTimeTextView.setText(str);
                }
            } else {
                str = getResources().getString(R.string.can_use_time);
                time = mCommonUtil.getTimeInSuperMode();
                if (time < 0) {
                    String timeStr = str + mRes.getString(R.string.power_cannotget);
                    mTimeTextView.setText(timeStr);
                } else {
                    int hours = time / 60;
                    int minutes = time % 60;
                    StringBuilder builder = new StringBuilder(str);
                    builder.append(String.valueOf(hours));
                    builder.append(getString(R.string.power_hours));
                    builder.append(String.valueOf(minutes));
                    builder.append(getString(R.string.power_minutes));
                    mTimeTextView.setText(builder.toString());
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "call computeChargeTimeRemaining throw remote exception");
            return;
        } catch (NotFoundException e) {
            Log.e(TAG, "updateTimeAlert, getString() throw exception, " + e.toString());
            return;
        }
    }

    private void exitSuperSaveModeClickListener() {
        mExitView.setOnClickListener(new OnClickListener() {
            DebouncedClickAction createDialogAction = new DebouncedClickAction() {
                @Override
                public void debouncedAction() {
                    createDialog(true);
                }
            };

            @Override
            public void onClick(View v) {
                createDialogAction.onClick();
            }
        });
    }

    //fix me
    private void initView(ViewGroup view, String componentName) {
        ComponentName component = ComponentName.unflattenFromString(componentName);
        Intent intent = new Intent();
        intent.setComponent(component);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        /*PackageManager manager = this.getPackageManager();
        List<ResolveInfo> list = manager.queryIntentActivities(intent, 0);
        ResolveInfo info = list.get(0);
        String title = (String) info.loadLabel(manager);*/
        view.setTag(intent);
        view.setOnClickListener(this);
    }

    @Override
    public boolean onLongClick(View v) {
        Log.d(TAG, "onLongClick v: " + v);
        return false;
    }

    @Override
    public void onClick(View v) {
        Intent intent = (Intent) v.getTag();
        intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startActivity(intent);
    }

    public void onBackPressed() {

    }

    private void exitSuperSaveMode() {
        Settings.System.putString(getContentResolver(), "aurora_power_mode", "normal");
        unregisterReceiver(mBatteryReveiver);
        unregisterObserver();

        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        intent.setAction(EXIT_ACTION);
        intent.addCategory(EXIT_ACTION_CATEGORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        bundle.putInt("power_flag", 1);
        intent.putExtras(bundle);
        mContext.startActivity(intent);
    }

    private void sendBroadcastToChameleon() {
        // 省电模式的广播action: "amigo.intent.action.chameleon.POWER_SAVING_MODE"
        Intent mPowerSavingModeIntent = new Intent("amigo.intent.action.chameleon.POWER_SAVING_MODE");
        // 省电模式开关：isChecked为true,通知变色龙进入省电模式；isChecked为false,关闭省电模式
        mPowerSavingModeIntent.putExtra("is_power_saving_mode", true);
        Log.i(TAG, "PowerSaveLauncher sendBroadcastToChameleon set is_power_saving_mode--> true");
        // 发送打开/关闭省电模式广播
        sendBroadcast(mPowerSavingModeIntent);
    }

    // Gionee <yangxinruo> <2015-09-06> modify for CR01548328 begin

    private void disableControlCenterAndStatusBar() {
        updateDatabase(mContext, 0);
        //setStatusBarExpandState(mStatusBarFlag);
        setSatusBarExpand(false);
    }

    private void setSatusBarExpand(boolean isEnable) {
        if (isEnable) {
            mStatusBarFlag &= ~StatusBarManager.DISABLE_EXPAND;
        } else {
            mStatusBarFlag |= StatusBarManager.DISABLE_EXPAND;
        }
        setStatusBarExpandState(mStatusBarFlag);
    }

    private void setStatusBarExpandState(int state) {
        StatusBarManager sm = (StatusBarManager) mContext.getSystemService(Context.STATUS_BAR_SERVICE);
        sm.disable(state);
    }

    private void updateDatabase(final Context context, final int value) {
        Log.i(TAG, "updateDatabase in PowerSaverLauncher StatusbarController--->" + value);
        //AmigoSettings.putInt(context.getContentResolver(), AMIGO_SETTING_CC_SWITCH, value);
    }

    public void killTask(Context context) {
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RecentTaskInfo> recentTasks = am.getRecentTasks(TASK_MAX,
                ActivityManager.RECENT_IGNORE_UNAVAILABLE | ActivityManager.RECENT_INCLUDE_PROFILES);
        int numTasks = recentTasks.size();

        Log.d(TAG, "task num=" + recentTasks.size() + " before remove task");

        List<RunningTaskInfo> runTasks = am.getRunningTasks(TASK_MAX);
        RunningTaskInfo topTask = null;
        RunningTaskInfo secTask = null;
        if (runTasks != null && runTasks.size() > 0) {
            topTask = runTasks.get(0);
            if (runTasks.size() > 1) {
                secTask = runTasks.get(1);
                Log.d(TAG, "sec task: " + secTask.baseActivity.getPackageName());
                Log.d(TAG, "sec task: " + secTask.id);
            }
            Log.d(TAG, "top task: " + topTask.baseActivity.getPackageName());
            Log.d(TAG, "top task: " + topTask.id);
        }
        if (recentTasks == null) {
            // recentTasks = new ArrayList<AppTask>();
            Log.d(TAG, "recentTasks == null");
            return;
        }

        for (int i = 0; i < numTasks; i++) {
            RecentTaskInfo taskInfo = recentTasks.get(i);
            // Gionee <yangxinruo> <2015-08-12> add for CR01525446 begin
            String basePkgName = "";
            if (taskInfo.baseIntent.getComponent() != null)
                basePkgName = taskInfo.baseIntent.getComponent().getPackageName();
            Log.d(TAG, "process task: intent=" + taskInfo.baseIntent + " pkg=" + basePkgName);
            if (mTaskWhiteList.contains(basePkgName)) {
                Log.d(TAG, "in whitelist do not kill " + basePkgName);
                continue;

            }
            // Gionee <yangxinruo> <2015-08-12> add for CR01525446 end
            else {
                Log.d(TAG, "task-persistentId: " + taskInfo.persistentId);
                Log.d(TAG, "task-id: " + taskInfo.id);
                if ((topTask != null && topTask.id == taskInfo.persistentId)) {
                    Log.d(TAG, "task: it's me, don't kill ");
                } else {
                    removeTask(am, taskInfo.persistentId);
                }
            }
        }
        recentTasks = am.getRecentTasks(TASK_MAX, ActivityManager.RECENT_IGNORE_UNAVAILABLE
                | ActivityManager.RECENT_INCLUDE_PROFILES);
        Log.d(TAG, "task num=" + recentTasks.size() + " after remove task");
    }

    private void removeTask(ActivityManager am, int taskId) {
        if (Build.VERSION.SDK_INT >= 22) {
            Log.d(TAG, "remove task task-id: " + taskId);
            FrameworkUtility.invokeMethod(ActivityManager.class, am, "removeTask", new Class[]{int.class},
                    new Object[]{(int) taskId});
        } else {
            Log.d(TAG, "remove task task-id(old version) : " + taskId);
            FrameworkUtility.invokeMethod(ActivityManager.class, am, "removeTask", new Class[]{int.class,
                    int.class}, new Object[]{(int) taskId, 0});
        }
    }

    private void setBatteryDisPlayMode(int value) {
        try {
            Settings.Secure.putInt(mContext.getContentResolver(), BATTERY_PERCENTAGE, value);
            // Post the intent
            Intent intent = new Intent(ACTION_BATTERY_PERCENTAGE_SWITCH);
            intent.putExtra("state", value);
            Log.d(TAG, "sendBroadcast battery percentage switch");
            mContext.sendBroadcast(intent);
        } catch (NumberFormatException e) {
            Log.e(TAG, "could not persist battery display style setting", e);
        }
    }

    private BroadcastReceiver mBatteryReveiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String actionStr = intent.getAction();
            if (Intent.ACTION_POWER_CONNECTED.equals(actionStr)) {
                dissmissDialog();
                createDialog(false);
            } else if (Intent.ACTION_POWER_DISCONNECTED.equals(actionStr)) {
                dissmissDialog();
            } else if (Intent.ACTION_BATTERY_CHANGED.equals(actionStr)) {
                updateTimeAlert();
            }
        }

    };

    private void dissmissDialog() {
        if (mPermDialog != null && mPermDialog.isShowing()) {
            mPermDialog.dismiss();
        }
    }

    private AuroraAlertDialog mPermDialog;

    private void createDialog(final boolean fromClick) {
        String message = null;
        mPermDialog = new AuroraAlertDialog.Builder(mContext, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
                .create();
        mPermDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        //mPermDialog.setTitle(mRes.getString(R.string.exit_string));
        if (fromClick) {
            message = mRes.getString(R.string.exit_msg);
        } else {
            message = mRes.getString(R.string.exit_msg1);
        }
        //mPermDialog.setMessage(message);
        mPermDialog.setTitle(message);

        DialogInterface.OnClickListener dialogClickLsn = new DialogInterface.OnClickListener() {
            DebouncedClickAction postiveAction = new DebouncedClickAction() {
                @Override
                public void debouncedAction() {
                    mSettingValuePreferences.edit().putBoolean(IS_FIRST, true).commit();
                    setBatteryDisPlayMode(mSettingValuePreferences.getInt(SETTING_VALUE, 0));
                    exitSuperSaveMode();
                    Main.this.finish();
                }
            };

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case AuroraAlertDialog.BUTTON_POSITIVE:
                        postiveAction.onClick();
                        break;

                    case AuroraAlertDialog.BUTTON_NEGATIVE:
                        break;
                    default:
                        break;
                }
            }
        };

        mPermDialog.setButton(AuroraAlertDialog.BUTTON_POSITIVE, mRes.getString(R.string.exit_string),
                dialogClickLsn);
        mPermDialog.setButton(AuroraAlertDialog.BUTTON_NEGATIVE, mRes.getString(R.string.cancel_string),
                dialogClickLsn);
        mPermDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

            }
        });

        mPermDialog.show();
    }
}
