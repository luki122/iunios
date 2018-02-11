package com.aurora.puremanager.utils;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;

/**
 * Created by joy on 12/24/15.
 */
public class StateController {
    private final ProviderHelper mProviderHelper;
    private final Context mContext;
    private final WifiManager mWifiManager;
    private final BluetoothAdapter mBtAdapter;
    private final ActivityManager mActivityManager;
    private final ContentResolver mResolver;
    private SuperPowerSaverUtil mPowerSaverUtil;
    private Handler mHandler = null;
    private static final int TIME_OUT = 5 * 1000;
    private static final int CONTROL_BT_DELAY_TIME = 1000;
    private static final int EVENT_EXIT_POWERSAVE_LANCHER = 1;
    private static final int EVENT_START_SUPERMODE = 2;
    private static final int EVENT_EXIT_SUPERMODE = 3;
    private static final int EVENT_SET_BT_ENABLE = 4;
    private static final int EVENT_SET_BT_DISABLE = 5;
    public static final String EVENT_START_SUPERMODE_FINISH = "com.gionee.softmanager.powersaver.utils.START_SUPERMODE_FINISH";
    public static final String EVENT_EXIT_SUPERMODE_FINISH = "com.gionee.softmanager.powersaver.utils.EXIT_SUPERMODE_FINISH";
    public static final String EVENT_FORCESTOP_PKG = "com.gionee.softmanager.powersaver.utils.KILL";
    public static final String EVENT_FORCESTOP_KEY = "com.gionee.softmanager.powersaver.utils.pkg";
    private static final String TAG = "StateController";
    private static final int SUPER_MODE_BRIGHTNESS = 128; // 50% brightness
    private static final int SUPER_MODE_TIMEOUT = 15000; // 15s
    public static final int NORMAL_MODE_BRIGHTNESS = 70;


    public StateController(Context context) {
        mContext = context;
        mResolver = context.getContentResolver();
        mProviderHelper = new ProviderHelper(context);
        mPowerSaverUtil = new SuperPowerSaverUtil(mContext);
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        initMainHandler();
    }

    private void initMainHandler() {
        mHandler = new Handler(mContext.getMainLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case EVENT_EXIT_POWERSAVE_LANCHER:
                        break;
                    case EVENT_START_SUPERMODE:
                        Log.e(TAG, "mHandler:EVENT_START_SUPERMODE");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                handlerStartSuperMode();
                            }
                        }).start();

                        //handlerStartSuperMode();
                        break;
                    case EVENT_EXIT_SUPERMODE:
                        Log.e(TAG, "mHandler:EVENT_EXIT_SUPERMODE");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                handlerExitSuperMode();
                            }
                        }).start();
                        //handlerExitSuperMode();
                        break;
                    case EVENT_SET_BT_ENABLE:
                        mBtAdapter.enable();
                        break;
                    case EVENT_SET_BT_DISABLE:
                        mBtAdapter.disable();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void handlerStartSuperMode() {
        // 冻结应用 ＆ 启用特定桌面
        Log.e(TAG, "handlerStartSuperMode");
        mPowerSaverUtil.intoSuperPowerSaveMode();
        /*while (PowerApp.pkg_to_stop_num > 0) {
            Log.e(TAG, "pkg_to_stop_num = ? " + PowerApp.pkg_to_stop_num);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
        mPowerSaverUtil.intoSuperPowerSaveMode2();
        decreaseCpuCore();
        //PowerModeHelper.setModeProcessing(mContext, Consts.DONE);
    }

    public void decreaseCpuCore() {
        Log.e(TAG, "StateController->decreaseCpuCore(), decrease cpu core");
        //CpuInfoUtils.limitMutliCore(true);
    }

    public void startSuperMode() {
        Log.e(TAG, "startSuperMode");
        setMasterSyncAutomatically(false);
        setScreenTimeout(SUPER_MODE_TIMEOUT);
        //setScreenSaverState(true);
        setBrightnessMode(Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        setBrightnessValue(SUPER_MODE_BRIGHTNESS);

        mHandler.sendEmptyMessage(EVENT_START_SUPERMODE);
    }

    public void restoreNoneModeState(int form) {
        if (form == Consts.NORMAL_MODE) {
            restoreFromNormal2None();
        } else if (form == Consts.SUPER_MODE) {
            restoreFromSuper2None();
        }
    }

    private void handlerExitSuperMode() {
        recoverCpuCore();
        mPowerSaverUtil.outSuperPowerSaveMode();
        Intent intent = new Intent(EVENT_EXIT_SUPERMODE_FINISH);
        mContext.sendBroadcast(intent);
    }

    public void exitSuperMode() {
        mHandler.sendEmptyMessage(EVENT_EXIT_SUPERMODE);
    }

    /* cpu core controller */
    public void recoverCpuCore() {
        Log.e(TAG, "StateController->recoverCpuCore(), recover cpu core");
        //CpuInfoUtils.limitMutliCore(false);
    }

    /* wifi controller */
    public int getWifiState() {
        if (mWifiManager == null) {
            return WifiManager.WIFI_STATE_UNKNOWN;
        }
        return mWifiManager.getWifiState();
    }

    public void setWifiApState(boolean enabled) {
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

    public void setAdbState(boolean enabled) {
        boolean isOn = isAdbOn();
        if (enabled) {
            if (!isOn) {
                Settings.Secure.putInt(mContext.getContentResolver(),
                        Settings.Secure.ADB_ENABLED, Consts.ADB_ON);
                Log.e(TAG, "mWifiManager.setWifiEnabled ----> true");
            }
        } else {
            if (isOn) {
                Settings.Secure.putInt(mContext.getContentResolver(),
                        Settings.Secure.ADB_ENABLED, Consts.ADB_OFF);
                Log.e(TAG, "mWifiManager.setWifiEnabled ----> false");
            }
        }
    }

    public void setWifiState(boolean enabled) {
        boolean isOn = isWifiOn();
        if (enabled) {
            if (!isOn) {
                mWifiManager.setWifiEnabled(true);
                Log.e(TAG, "mWifiManager.setWifiEnabled ----> true");
            }
        } else {
            if (isOn) {
                mWifiManager.setWifiEnabled(false);
                Log.e(TAG, "mWifiManager.setWifiEnabled ----> false");
            }
        }
    }

    private void restoreFromNormal2None() {
        Log.e(TAG, "StateController->restoreFromNormal2None() begin");
        restoreWifiState();
        //restoreDataConnectionState();
        restoreBtState();
        restoreGpsState();
        restoreAdbState();
        restoreSyncState();
        restoreScreenTimeout();
        //restoreBrightnessMode();
        //restoreScreenSaverState();
        restoreBrightnessState();
        Log.e(TAG, "StateController->restoreFromNormal2None() end");
    }

    private void restoreBrightnessState() {
        Log.e(TAG, "StateController->restoreBrightnessState() mode=always NONE Lom NORMAL ");
        int curBrightness = getSaveBrightnessMode(Consts.NONE_POWER);
        // 相等则说明用户未手动改动亮度值，恢复默认
        if (curBrightness == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            setBrightnessMode(Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        } else {
            int savedValue = mProviderHelper.getInt(Consts.NONE_POWER + Consts.BRIGHTNESS_KEY,
                    NORMAL_MODE_BRIGHTNESS);
            setBrightnessValue(savedValue);
        }
    }

    public void restoreScreenTimeout() {
        int timeout = mProviderHelper.getInt(Consts.NONE_POWER + Consts.TIMEOUT_KEY, 15000);
        if (timeout != 15000) {
            Settings.System.putInt(mResolver, Settings.System.SCREEN_OFF_TIMEOUT, timeout);
            Log.e(TAG, "restoreScreenTimeout " + timeout);
        }
    }

    public void setGpsState(boolean enabled) {
        boolean isOn = isGpsOn();
        Log.d(TAG, "setGpsState Set GPS state val=" + enabled);
        if (enabled) {
            if (!isOn) {
                Settings.Secure.putInt(mResolver, Settings.Secure.LOCATION_MODE,
                        Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
            }
        } else {
            if (isOn) {
                Settings.Secure.putInt(mResolver, Settings.Secure.LOCATION_MODE,
                        Settings.Secure.LOCATION_MODE_OFF);
            }
        }
    }

    public void setNetDisplayStatus(boolean enable) {
        Intent intent = new Intent("action_isdisplay_network_speed");
        intent.putExtra("isdisplay", enable);
        mContext.sendBroadcast(intent);
    }

    public void restoreNetDisplayStatus() {
        String status = Settings.System.getString(mContext.getContentResolver(), "network_display_status");
        if ("true".equals(status)) {
            setNetDisplayStatus(true);
        }
    }

    public boolean isNetDisplayOn() {
        String status = Settings.System.getString(mContext.getContentResolver(),
                "network_display_status");
        if ("true".equals(status)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isWifiOn() {
        int status = getWifiState();
        if (status == WifiManager.WIFI_STATE_ENABLED || status == WifiManager.WIFI_STATE_ENABLING) {
            return true;
        }

        return false;
    }

    private boolean isWifiApOn() {
        int apState = mWifiManager.getWifiApState();
        if (apState == WifiManager.WIFI_AP_STATE_ENABLING || apState == WifiManager.WIFI_AP_STATE_ENABLED) {
            return true;
        }
        return false;
    }

    /* data connection controller */
    public void setDataConnectionState(boolean enabled) {
        TelephonyManager telManager = TelephonyManager.from(mContext);
        Log.e(TAG, "telManager.setDataEnabled ----> " + enabled);
        telManager.setDataEnabled(enabled);
    }

    public boolean isDataConnectionOn() {
        TelephonyManager telManager = TelephonyManager.from(mContext);
        return telManager.getDataEnabled();
    }

    private void restoreDataConnectionState() {
        if (isDataConnectionOn()) {

        } else {
            boolean savedState = getSavedState(Consts.DATA_KEY);
            setDataConnectionState(savedState);
        }
    }

    /* bt controller */
    public int getBluetoothState() {
        if (mBtAdapter == null) {
            return BluetoothAdapter.STATE_OFF;
        }
        return mBtAdapter.getState();
    }

    public void setBluetoothState(boolean enabled) {
        boolean isOn = isBluetoothOn();
        boolean result = false;
        if (enabled) {
            if (!isOn) {
                //result = mBtAdapter.enable();

                mBtAdapter.enable();
                //mHandler.sendEmptyMessageDelayed(EVENT_SET_BT_ENABLE, CONTROL_BT_DELAY_TIME);
                Log.e(TAG, "mWifiManager.setBluetoothState ----> ? " + "enable " + result);
            }
        } else {
            if (isOn) {
                //mHandler.sendEmptyMessageDelayed(EVENT_SET_BT_DISABLE, CONTROL_BT_DELAY_TIME);
                result = mBtAdapter.disable();
                Log.e(TAG, "mWifiManager.setBluetoothState ----> ? " + "disable " + result);
            }
        }
    }

    public void saveConnectionState(String mode) {
        android.util.Log.d(TAG, "enter saveConnectionState mode=" + mode);
        mProviderHelper.putBoolean(mode + Consts.DATA_KEY, isDataConnectionOn());
    }

    private void restoreBtState() {
        /* 恢复状态使发现蓝牙是打开状态，一定是用户在日常省电模式打开的，不处理 */
        if (isBluetoothOn()) {

        } else {
            boolean savedState = getSavedState(Consts.BLUETOOTH_KEY);
            setBluetoothState(savedState);
        }
    }

    public boolean isBluetoothOn() {
        int status = mBtAdapter.getState();
        if (status == BluetoothAdapter.STATE_ON || status == BluetoothAdapter.STATE_TURNING_ON) {
            return true;
        }

        return false;
    }

    public boolean isGpsOn() {
        int mode = Settings.Secure.getInt(mResolver, Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF);
        android.util.Log.d(TAG, "isGpsOn Get GPS state val=" + mode);
        if (mode != Settings.Secure.LOCATION_MODE_OFF) {
            return true;
        }
        return false;
    }

    private boolean getSavedState(String key) {
        return mProviderHelper.getBoolean(Consts.NONE_POWER + key, true);
    }

    public void saveWifiState(String mode) {
        mProviderHelper.putBoolean(mode + Consts.WIFI_KEY, isWifiOn());
    }

    public void saveBtState(String mode) {
        mProviderHelper.putBoolean(mode + Consts.BLUETOOTH_KEY, isBluetoothOn());
    }

    public void saveGpsState(String mode) {
        mProviderHelper.putBoolean(mode + Consts.GPS_KEY, isGpsOn());
    }

    public void saveSyncState(String mode) {
        mProviderHelper.putBoolean(mode + Consts.SYNC_KEY, isMasterSyncOn());
    }

    public void saveAdbState(String mode) {
        mProviderHelper.putBoolean(mode + Consts.ADB_KEY, isAdbOn());
    }

    public boolean isMasterSyncOn() {
        return ContentResolver.getMasterSyncAutomatically();
    }

    public boolean isAdbOn() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.ADB_ENABLED, 0) == Consts.ADB_ON;
    }

    public int getBrightnessValue() {
        return Settings.System.getInt(mResolver, Settings.System.SCREEN_BRIGHTNESS, 128);
    }

    public void saveBrightnessState(String mode) {
        Log.e(TAG, "StateController->saveBrightnessState() mode= " + mode + " value="
                + getBrightnessValue());
        if (getBrightnessMode() == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) {
            mProviderHelper.putInt(mode + Consts.BRIGHTNESS_KEY, getBrightnessValue());
        }
        mProviderHelper.putInt(mode + Consts.BRIGHTNESS_STATE_KEY, getBrightnessMode());
    }

    public int getSaveBrightnessMode(String mode) {
        return mProviderHelper.getInt(mode + Consts.BRIGHTNESS_STATE_KEY,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
    }

    public void saveScreenTimoutState(String mode) {
        mProviderHelper.putInt(mode + Consts.TIMEOUT_KEY, getScreenTimeout());
    }

    public int getScreenTimeout() {
        int defTimeout = 15000;
        return Settings.System.getInt(mResolver, Settings.System.SCREEN_OFF_TIMEOUT, defTimeout);
    }

    public void saveState(String mode) {
        Log.e(TAG, "StateController->saveState() begin, mode = " + mode);
        saveAdbState(mode);
        saveWifiState(mode);
        saveBtState(mode);
        saveConnectionState(mode);
        saveGpsState(mode);
        saveSyncState(mode);
        saveBrightnessState(mode);
        saveScreenTimoutState(mode);
        Log.e(TAG, "StateController->saveState() end");
    }

    private void restoreWifiState() {
        if (isWifiOn()) {

        } else {
            boolean savedState = getSavedState(Consts.WIFI_KEY);
            setWifiState(savedState);
        }
    }

    private void restoreAdbState() {
        if (isAdbOn()) {

        } else {
            boolean savedState = getSavedState(Consts.ADB_KEY);
            setAdbState(savedState);
        }
    }

    private void restoreGpsState() {
        if (isGpsOn()) {

        } else {
            boolean savedState = getSavedState(Consts.GPS_KEY);
            setGpsState(savedState);
        }
    }

    private void restoreSyncState() {
        if (isMasterSyncOn()) {

        } else {
            boolean savedState = getSavedState(Consts.SYNC_KEY);
            setMasterSyncAutomatically(savedState);
        }
    }

    /* sync controller */
    public void setMasterSyncAutomatically(boolean enabled) {
        Log.e(TAG, "StateController->setMasterSyncAutomatically(), " + enabled);
        boolean isOn = isMasterSyncOn();
        if (enabled) {
            if (!isOn) {
                ContentResolver.setMasterSyncAutomatically(true);
            }
        } else {
            if (isOn) {
                ContentResolver.setMasterSyncAutomatically(false);
            }
        }
    }

    public void restoreTimeoutFromSuper2None() {
        int timeout = mProviderHelper.getInt(Consts.NONE_POWER + Consts.TIMEOUT_KEY, 15000);
        Settings.System.putInt(mResolver, Settings.System.SCREEN_OFF_TIMEOUT, timeout);
        Log.e(TAG, "restoreTimeoutFromSuper2None " + timeout);
    }

    public void setBrightnessMode(int mode) {
        Settings.System.putInt(mResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
    }

    private int getSavedInt(String mode, String key, int defValue) {
        return mProviderHelper.getInt(mode + key, defValue);
    }

    public int getBrightnessMode() {
        return Settings.System.getInt(mResolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

    public boolean isBrightnessAutomaticMode() {
        if (getBrightnessMode() == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            return true;
        } else {
            return false;
        }
    }

    private void restoreBrightnessFromSuper2None() {
        int savedValue = mProviderHelper.getInt(Consts.NONE_POWER + Consts.BRIGHTNESS_KEY, 128);
        Log.e(TAG,
                "StateController->restoreBrightnessFromSuper2None() mode=always NONE from SUPER value="
                        + savedValue);
        setBrightnessValue(savedValue);
    }

    /* brigness controller */
    public void setBrightnessValue(int value) {
        Settings.System.putInt(mResolver, Settings.System.SCREEN_BRIGHTNESS, value);
    }

    /* screen timeout controller */
    public void setScreenTimeout(int timeout) {
        int value = (timeout == -1) ? -1 : timeout;
        Settings.System.putInt(mResolver, Settings.System.SCREEN_OFF_TIMEOUT, value);
        Log.e(TAG, "setScreenTimeout " + value);
    }

    private void restoreFromSuper2None() {
        Log.e(TAG, "StateController->restoreFromSuper2None() begin");
        restoreWifiState();
        restoreDataConnectionState();
        restoreGpsState();
        restoreSyncState();
        restoreTimeoutFromSuper2None();
        // 退出时先恢复多核，再解冻应用
        //recoverCpuCore();
        //restoreScreenSaverState();
        restoreBrightnessState();
        restoreBtState();
        restoreNetDisplayStatus();
        //restoreAdbState();
    }
}
