package com.aurora.puremanager.service;

import android.app.AppOpsManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.UserHandle;
import android.provider.Settings;

import com.aurora.puremanager.utils.Log;
import com.aurora.puremanager.utils.SettingValueUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by joy on 1/7/16.
 */
public class SmartPowerService extends Service {

    private final String TAG = "SmartPowerService";
    private Context mContext;
    private Handler mHandler;
    private int mBatteryLevel = 100;
    private final int middleLevel = 50;
    private final int lowLevel = 15;
    private int mPlugType = 0;
    private SettingValueUtil settingsUtil;
    public final static String SETTINGS_VALUE = "settings_value";
    public final static String SMART_STATE = "smart_switch";
    private SharedPreferences sp;
    private final int PLUG_BRI_VALUE = 140;
    private final int LOW_LEVEL = 0;
    private final int MIDDLE_LEVEL = 1;
    private final int HIGH_LEVEL = 2;
    private int batteryLevel;
    private final int middle_brightness = 102;
    private final int low_brightness = 89;
    private final int middle_timeout = 30000;
    private final int low_timeout = 15000;
    private final int long_timeout = 2 * 60 * 1000;
    private int userTimeout;
    private int userBrightness;
    private final int three_delay = 3 * 60 * 1000;//3 * 60 * 60;
    private final int two_delay = 2 * 60 * 1000;
    private final int one_delay = 1 * 60 * 1000;
    public final static int STATE_NORMAL = 0; // 0 normal 1 smart
    public final static int STATE_SMART = 1; // 0 normal 1 smart
    private BatteryReceiver mReceiver;
    public final static String ACTION = "aurora.smart.action";
    private Timer timer = null;
    private boolean INIT_FLAG = false;
    private boolean CURRENT_STATE = false;
    private static final int RECENT_TIME_INTERVAL_MILLIS = 30 * 1000;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        initData();
    }

    private void initData() {
        mHandler = new Handler();
        settingsUtil = new SettingValueUtil(getContentResolver());
        sp = getSharedPreferences(SETTINGS_VALUE, MODE_PRIVATE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initData();
        boolean state;
        if (intent == null) {
            state = sp.getInt(SmartPowerService.SMART_STATE, SmartPowerService.STATE_NORMAL)
                    == STATE_SMART;
        } else {
            state = intent.getBooleanExtra(SMART_STATE, false);
        }

        if (CURRENT_STATE == state) {
            return 0;
        } else {
            CURRENT_STATE = state;
        }
        Log.e(TAG, "onStartCommand " + state);
        if (CURRENT_STATE) {
            intoSmartMode();
        } else {
            exitSmartMode();
        }

        Intent backIntent = new Intent(ACTION);
        sendBroadcastAsUser(backIntent, new UserHandle(UserHandle.USER_CURRENT));

        return super.onStartCommand(intent, flags, startId);
    }

    private void intoSmartMode() {
        Log.e(TAG, "intoSmartMode");
        saveData();

        mReceiver = new BatteryReceiver();
        mReceiver.registerReceivers();
        timer = new Timer();

        checkNet();
    }

    private void checkNet() {
        Log.e(TAG, "checkNet");
        int level = getCurrentLevel();
        int time_delay = two_delay;
        if (level == HIGH_LEVEL) {
            //return;
        } else if (level == MIDDLE_LEVEL) {
            time_delay = three_delay;
        } else {
            time_delay = two_delay;
        }

        final ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        dealDevices(connectivityManager, time_delay);
    }

    private void saveData() {
        int value;
        SharedPreferences.Editor editor = sp.edit();
        if (settingsUtil.isBrightnessAutomaticMode()) {
        } else {
            value = settingsUtil.getBrightnessValue();
            Log.e(TAG, "getBrightnessValue = " + value);
            editor.putInt(SettingValueUtil.BRIGHTNESS_VALUE, value);
            userBrightness = value;
        }

        value = settingsUtil.getScreenTimeout();
        editor.putInt(SettingValueUtil.TIMEOUT_VALUE, value);
        userTimeout = value;
        Log.e(TAG, "getScreenTimeout = " + value);

        //切换状态
        editor.putInt(SMART_STATE, STATE_SMART);
        editor.commit();
    }

    private void exitSmartMode() {
        Log.e(TAG, "exitSmartMode");
        mReceiver.unRegisterReceiver();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        recoverData();

        int value;
        if (settingsUtil.isBrightnessAutomaticMode()) {

        } else {
            value = sp.getInt(SettingValueUtil.BRIGHTNESS_VALUE, 50);
            settingsUtil.setBrightnessValue(value);
        }

        value = sp.getInt(SettingValueUtil.TIMEOUT_VALUE, 50);
        //恢复状态
        sp.edit().putInt(SMART_STATE, STATE_NORMAL).commit();
        settingsUtil.setScreenTimeout(value);
    }

    private void recoverData() {
        INIT_FLAG = false;
    }

    private final class BatteryReceiver extends BroadcastReceiver {

        private void registerReceivers() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            mContext.registerReceiver(this, filter, null, mHandler);
        }

        private void unRegisterReceiver() {
            mContext.unregisterReceiver(this);
            mReceiver = null;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                Log.e(TAG, "BatteryReceiver$onReceive ACTION_BATTERY_CHANGED");

                final int oldBatteryLevel = mBatteryLevel;
                final int oldPlugType = mPlugType;

                mPlugType = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 1);
                mBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 100);
                batteryLevel = getCurrentLevel();

                if (!INIT_FLAG) {
                    INIT_FLAG = true;
                    initMode();
                }

                if (mPlugType != oldPlugType) {
                    // 电池状态发生转变
                    if (mPlugType != 0) {
                        //--->充电状态
                        toPlugedStatus();
                    } else {
                        //--->电池状态
                        toUnplugedStatus();
                    }
                }

                if (mBatteryLevel < oldBatteryLevel) {
                    Log.e(TAG, "mBatteryLevel " + mBatteryLevel + " oldBatteryLevel " + oldBatteryLevel);
                    if (mBatteryLevel == middleLevel) {
                        highToMiddle();
                    } else if (mBatteryLevel == lowLevel) {
                        middleToLow();
                    }
                } else {

                }
            }
        }
    }

    private void initMode() {
        Log.e(TAG, "initMode");
        switch (batteryLevel) {
            case HIGH_LEVEL:
                intoSmartHigh();
                break;
            case MIDDLE_LEVEL:
                intoSmartMiddle();
                break;
            case LOW_LEVEL:
                intoSmartLow();
                break;
        }
    }

    private void intoSmartLow() {
        setBrightnessValue(low_brightness);
        setTimeoutValue(low_timeout);

        final ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        // wifi连接状态
        dealDevices(connectivityManager, two_delay);
    }

    private void intoSmartMiddle() {
        setBrightnessValue(middle_brightness);
        setTimeoutValue(middle_timeout);

        final ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        dealDevices(connectivityManager, three_delay);
    }

    private void intoSmartHigh() {
        setBrightnessValue(PLUG_BRI_VALUE);
        setTimeoutValue(long_timeout);
    }

    private void middleToLow() {
        Log.e(TAG, "middleToLow");
        setBrightnessValue(low_brightness);
        setTimeoutValue(low_timeout);

        final ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        dealDevices(connectivityManager, two_delay);
    }

    private void dealDevices(final ConnectivityManager connectivityManager, int delay) {
        dealWifi(connectivityManager, delay);
        dealAp();
        dealBt(delay);
        dealGps();
    }

    private void dealWifi(final ConnectivityManager connectivityManager, int delay) {
        if (needCloseWifi()) {
            Log.e(TAG, "dealDevices needCloseWifi delay = " + delay);
            TimerTask task = new TimerTask() {
                public void run() {
                    Log.e(TAG, "task RUN");
                    NetworkInfo.State wifi = connectivityManager.getNetworkInfo(
                            ConnectivityManager.TYPE_WIFI).getState();
                    if (wifi != NetworkInfo.State.CONNECTED) {
                        WifiManager mWifiManager = (WifiManager) mContext.
                                getSystemService(Context.WIFI_SERVICE);
                        mWifiManager.setWifiEnabled(false);
                        Log.e(TAG, "close wifi");
                    }
                }
            };
            if (timer == null) {
                timer = new Timer();
            }
            timer.schedule(task, delay);
        }
    }

    private void dealAp() {
        if (isWifiApOn()) {
            TimerTask task = new TimerTask() {
                public void run() {
                    Log.e(TAG, "task RUN");
                    if (isWifiApOn()) {
                        if (needCloseAp()) {
                            setWifiApState(false);
                        }
                    }
                }
            };
            if (timer == null) {
                timer = new Timer();
            }
            timer.schedule(task, one_delay);
        }
    }

    private void dealBt(int delay) {
        if (needCloseBt()) {
            TimerTask task = new TimerTask() {
                public void run() {
                    Log.e(TAG, "task RUN");
                    BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
                    int state = mBtAdapter.getConnectionState();
                    if (state != BluetoothAdapter.STATE_CONNECTED) {
                        mBtAdapter.disable();
                        Log.e(TAG, "close bt");
                    }
                }
            };
            if (timer == null) {
                timer = new Timer();
            }
            timer.schedule(task, delay);
        }
    }

    private void dealGps() {
        if (isGpsOn()) {
            TimerTask task = new TimerTask() {
                public void run() {
                    Log.e(TAG, "task RUN");
                    if (isGpsOn()) {
                        if (needCloseGps()) {
                            setGpsState(false);
                        }
                    }
                }
            };
            if (timer == null) {
                timer = new Timer();
            }
            timer.schedule(task, three_delay);
        }
    }

    private void highToMiddle() {
        Log.e(TAG, "highToMiddle");
        setBrightnessValue(middle_brightness);
        setTimeoutValue(middle_timeout);

        final ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        dealDevices(connectivityManager, two_delay);
    }

    public int getWifiState() {
        WifiManager mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager == null) {
            return WifiManager.WIFI_STATE_UNKNOWN;
        }
        return mWifiManager.getWifiState();
    }

    public boolean needCloseWifi() {
        int status = getWifiState();
        if (status == WifiManager.WIFI_STATE_ENABLED || status == WifiManager.WIFI_STATE_ENABLING) {
            ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo.State wifi = connectivityManager.getNetworkInfo(
                    ConnectivityManager.TYPE_WIFI).getState();
            if (wifi != NetworkInfo.State.CONNECTED) {
                Log.e(TAG, "needCloseWifi true");
                return true;
            }
        }

        Log.e(TAG, "needCloseWifi false");
        return false;
    }

    public boolean needCloseBt() {
        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        int status = mBtAdapter.getState();

        if (status == BluetoothAdapter.STATE_ON || status == BluetoothAdapter.STATE_TURNING_ON) {
            status = mBtAdapter.getConnectionState();
            if (status != BluetoothAdapter.STATE_CONNECTED) {
                Log.e(TAG, "needCloseBt true");
                return true;
            }
        }

        Log.e(TAG, "needCloseBt false");
        return false;
    }

    public boolean needCloseGps() {
        AppOpsManager aoManager =
                (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        List<AppOpsManager.PackageOps> appOps = aoManager.getPackagesForOps(new int[]{
                AppOpsManager.OP_MONITOR_LOCATION, AppOpsManager.OP_MONITOR_HIGH_POWER_LOCATION,});
        Log.e(TAG, "appOps.size " + appOps.size());
        long now = System.currentTimeMillis();
        long recentLocationCutoffTime = now - RECENT_TIME_INTERVAL_MILLIS;
        for (AppOpsManager.PackageOps ops : appOps) {
            List<AppOpsManager.OpEntry> entries = ops.getOps();
            for (AppOpsManager.OpEntry entry : entries) {
                if (entry.getTime() >= recentLocationCutoffTime ) {
                    switch (entry.getOp()) {
                        case AppOpsManager.OP_MONITOR_LOCATION:
                        case AppOpsManager.OP_MONITOR_HIGH_POWER_LOCATION:
                            if (ops.getPackageName().equals("android")) {
                                continue;
                            }
                            Log.e(TAG, "packagename = " + ops.getPackageName());
                            Log.e(TAG, "needCloseGps false " + entry.getTime() +
                                    " now = " + now + " " + "intervel = " + (now - entry.getTime()));
                            return false;
                        default:
                            break;
                    }
                }
            }
        }
        Log.e(TAG, "needCloseGps true");
        return true;
    }

    public void setGpsState(boolean enabled) {
        boolean isOn = isGpsOn();
        Log.d(TAG, "setGpsState Set GPS state val=" + enabled);
        if (enabled) {
            if (!isOn) {
                Settings.Secure.putInt(getContentResolver(), Settings.Secure.LOCATION_MODE,
                        Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
            }
        } else {
            if (isOn) {
                Settings.Secure.putInt(getContentResolver(), Settings.Secure.LOCATION_MODE,
                        Settings.Secure.LOCATION_MODE_OFF);
            }
        }
    }

    public boolean isGpsOn() {
        int mode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF);
        android.util.Log.d(TAG, "isGpsOn Get GPS state val=" + mode);
        if (mode != Settings.Secure.LOCATION_MODE_OFF) {
            return true;
        }
        return false;
    }

    private boolean isWifiApOn() {
        WifiManager mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        int apState = mWifiManager.getWifiApState();
        if (apState == WifiManager.WIFI_AP_STATE_ENABLING || apState == WifiManager.WIFI_AP_STATE_ENABLED) {
            return true;
        }
        return false;
    }

    public void setWifiApState(boolean enabled) {
        WifiManager mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        boolean isOn = isWifiApOn();
        if (enabled) {
            if (!isOn) {
                mWifiManager.setWifiApEnabled(null, true);
            }
        } else {
            if (isOn) {
                mWifiManager.setWifiApEnabled(null, false);
            }
        }
    }

    private boolean needCloseAp() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            int length;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                length = splitted.length;
                if (splitted[length - 1].startsWith("ap")) {
                    return false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private void setTimeoutValue(int value) {
        if (value < userTimeout) {
            settingsUtil.setScreenTimeout(value);
        }
    }

    private void toUnplugedStatus() {
        Log.e(TAG, "toUnplugedStatus");
        int value;
        int level = getCurrentLevel();
        if (level == HIGH_LEVEL) {
            setBrightnessValue(settingsUtil.getBrightnessValue());

            value = sp.getInt(SettingValueUtil.TIMEOUT_VALUE, low_timeout);
            settingsUtil.setScreenTimeout(value);
        } else if (level == MIDDLE_LEVEL) {
            setBrightnessValue(middle_brightness);
            setTimeoutValue(middle_timeout);
        } else {
            setBrightnessValue(low_brightness);
            setTimeoutValue(low_timeout);
        }
    }

    private void toPlugedStatus() {
        Log.e(TAG, "toPlugedStatus");
        int value;
        if (settingsUtil.isBrightnessAutomaticMode()) {

        } else {
            value = settingsUtil.getBrightnessValue();
            if (value > PLUG_BRI_VALUE) {
                settingsUtil.setBrightnessValue(PLUG_BRI_VALUE);
            }
        }
        value = sp.getInt(SettingValueUtil.TIMEOUT_VALUE, low_timeout);
        settingsUtil.setScreenTimeout(value);
    }

    private void setBrightnessValue(int value) {
        Log.e(TAG, "setBrightnessValue " + value);
        if (settingsUtil.isBrightnessAutomaticMode()) {

        } else if (userBrightness > value) {
            settingsUtil.setBrightnessValue(value);
        }
    }

    private int getCurrentLevel() {
        int level;
        if (mBatteryLevel > 50) {
            level = HIGH_LEVEL;
        } else if (mBatteryLevel > 15) {
            level = MIDDLE_LEVEL;
        } else {
            level = LOW_LEVEL;
        }
        return level;
    }


}
