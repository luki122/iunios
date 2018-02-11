package gn.com.android.update.settings;

import gn.com.android.update.business.OtaReceiver;
import gn.com.android.update.settings.ApplicationDataManager;
import gn.com.android.update.settings.ApplicationDataManager.DataOwner;
import gn.com.android.update.settings.ApplicationDataManager.OtaSettingKey;
import gn.com.android.update.utils.LogUtils;
import gn.com.android.update.utils.OtaInent;
import gn.com.android.update.utils.Util;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class OtaSettings {
    public static final long ONE_DAY_MILLISECOND = 24 * 60 * 60 * 1000;
    private static final String TAG = "OtaSettings";

    public enum AutoCheckCycle {

        SEVEN_DAYS, FOURTEEN_DAYS, THIRTY_DAYS, NINETY_DAYS;
    }

    public static boolean getAutoCheckEnabled(Context context, boolean defValue) {
        ApplicationDataManager dataManager = ApplicationDataManager.getInstance(context);
        return dataManager.getBoolean(DataOwner.OTA_SETTING, OtaSettingKey.SETTING_KEY_AUTO_CHECK_ENABLE,
                true);
    }

    public static void setAutoCheckEnable(Context context, boolean enabled) {
        Intent intent = new Intent(context, OtaReceiver.class);
        intent.setAction(OtaInent.AUTO_CHECK_ACTION);
        PendingIntent alarmAutoCheck = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (enabled) {
            startAutoCheck(context, alarmManager, alarmAutoCheck);

        } else {
            remove(context, OtaSettingKey.SETTING_KEY_LAST_AUTO_CHECK_TIME);
            alarmManager.cancel(alarmAutoCheck);
        }
    }

    private static void startAutoCheck(Context context, AlarmManager alarmManager,
            PendingIntent alarmAutoCheck) {
        alarmManager.cancel(alarmAutoCheck);
        long cycle = getAutoCheckCycleMillis(getAutoCheckCycle(context));
        long nextStartTime = getAutoCheckNextStartTime(context, cycle);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, nextStartTime, cycle, alarmAutoCheck);
    }

    public static void setLastAutoCheckTime(Context context, long value) {
        ApplicationDataManager dataManager = ApplicationDataManager.getInstance(context);
        dataManager.putLong(DataOwner.OTA_SETTING, OtaSettingKey.SETTING_KEY_LAST_AUTO_CHECK_TIME, value);
    }

    public static void remove(Context context, String key) {
        ApplicationDataManager dataManager = ApplicationDataManager.getInstance(context);
        dataManager.remove(DataOwner.OTA_SETTING, key);
    }

    public static long getLastAutoCheckTime(Context context, long defValue) {
        ApplicationDataManager dataManager = ApplicationDataManager.getInstance(context);
        return dataManager.getLong(DataOwner.OTA_SETTING, OtaSettingKey.SETTING_KEY_LAST_AUTO_CHECK_TIME,
                defValue);

    }

    private static long getAutoCheckNextStartTime(Context context, long cycle) {
        long currentTime = System.currentTimeMillis();
        long lastAutoCheckTime = getLastAutoCheckTime(context, 0);
        long nextStartTime = currentTime + cycle;

        long intervalTime = currentTime - lastAutoCheckTime;

        if (intervalTime < cycle) {
            nextStartTime = nextStartTime - intervalTime;
        }

        LogUtils.logd(TAG, "getAutoCheckNextStartTime nextStartTime = " + nextStartTime);
        return nextStartTime;
    }

    public static void changeAutoCheckCycle(Context context) {
        setAutoCheckEnable(context, true);
    }

    public static long getAutoCheckCycleMillis(AutoCheckCycle autoCheckCycle) {
        switch (autoCheckCycle) {
            case SEVEN_DAYS:
                return 7 * ONE_DAY_MILLISECOND;

            case FOURTEEN_DAYS:
                return 14 * ONE_DAY_MILLISECOND;

            case THIRTY_DAYS:
                return 30 * ONE_DAY_MILLISECOND;

            case NINETY_DAYS:
                return 90 * ONE_DAY_MILLISECOND;

            default:
                return 30 * ONE_DAY_MILLISECOND;
        }
    }

    public static AutoCheckCycle getAutoCheckCycle(Context context) {
        ApplicationDataManager dataManager = ApplicationDataManager.getInstance(context);
        int cycleIndex = dataManager.getInt(DataOwner.OTA_SETTING,
                OtaSettingKey.SETTING_KEY_AUTO_CHECK_DURATION, 0);
        switch (cycleIndex) {
            case 0:
                return AutoCheckCycle.SEVEN_DAYS;

            case 1:
                return AutoCheckCycle.FOURTEEN_DAYS;

            case 2:
                return AutoCheckCycle.THIRTY_DAYS;

            case 3:
                return AutoCheckCycle.NINETY_DAYS;

            default:
                return AutoCheckCycle.THIRTY_DAYS;
        }
    }

    public static boolean getAutoDownloadEnabled(Context context, boolean defValue) {
        ApplicationDataManager dataManager = ApplicationDataManager.getInstance(context);
        return dataManager.getBoolean(DataOwner.OTA_SETTING, OtaSettingKey.SETTING_KEY_AUTO_DOWNLOAD_ENABLED,
                false);
    }

    public static void startAutoCheck(Context context) {
        Intent intent = new Intent(context, OtaReceiver.class);
        intent.setAction(OtaInent.AUTO_CHECK_ACTION);
        PendingIntent alarmAutoCheck = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        startAutoCheck(context, alarmManager, alarmAutoCheck);
    }

    public static void setThisDownloadIsAutoDownload(Context context, boolean flag) {
        ApplicationDataManager dataManager = ApplicationDataManager.getInstance(context);
        dataManager.putBoolean(DataOwner.OTA_SETTING, OtaSettingKey.SETTING_KEY_THIS_DOWNLOAD_IS_AUTO, flag);
    }

    public static boolean IsAutoDownload(Context context, boolean defValue) {
        ApplicationDataManager dataManager = ApplicationDataManager.getInstance(context);
        return dataManager.getBoolean(DataOwner.OTA_SETTING, OtaSettingKey.SETTING_KEY_THIS_DOWNLOAD_IS_AUTO,
                defValue);
    }

    public static void setCurrentVersionImproveInfo(Context context, String currentReleanote,
            String currentReleanoteUrl, String currentVersion) {
        ApplicationDataManager dataManager = ApplicationDataManager.getInstance(context);
        dataManager.putString(DataOwner.OTA_SETTING, OtaSettingKey.SETTING_KEY_UPGRADE_VERSION,
                currentVersion);
        dataManager.putString(DataOwner.OTA_SETTING, OtaSettingKey.SETTING_KEY_CURRENT_VERSION_RELEASE_NOTE,
                currentReleanote);
        dataManager.putString(DataOwner.OTA_SETTING,
                OtaSettingKey.SETTING_KEY_CURRENT_VERSION_RELEASE_NOTE_URL, currentReleanoteUrl);
    }

    public static String getLastUpgradeVersion(Context context, String defValue) {
        ApplicationDataManager dataManager = ApplicationDataManager.getInstance(context);
        return dataManager.getString(DataOwner.OTA_SETTING, OtaSettingKey.SETTING_KEY_UPGRADE_VERSION,
                defValue);
    }

    public static String getCurrVerReleasenote(Context context, String defCurrentReleasenote) {
        ApplicationDataManager dataManager = ApplicationDataManager.getInstance(context);
        if (Util.getInternalVersion().equals(getLastUpgradeVersion(context, null))) {
            return dataManager.getString(DataOwner.OTA_SETTING,
                    OtaSettingKey.SETTING_KEY_CURRENT_VERSION_RELEASE_NOTE, defCurrentReleasenote);
        }
        return defCurrentReleasenote;
    }

    public static String getCurrVerReleasenoteUrl(Context context, String defCurrentReleasenoteUrl) {
        ApplicationDataManager dataManager = ApplicationDataManager.getInstance(context);
        if (Util.getInternalVersion().equals(getLastUpgradeVersion(context, null))) {
            return dataManager.getString(DataOwner.OTA_SETTING,
                    OtaSettingKey.SETTING_KEY_CURRENT_VERSION_RELEASE_NOTE_URL, defCurrentReleasenoteUrl);
        }
        return defCurrentReleasenoteUrl;
    }

    public static String getLastWifiConnectDate(Context context, String defValue) {
        ApplicationDataManager dataManager = ApplicationDataManager.getInstance(context);
        return dataManager.getString(DataOwner.OTA_SETTING, OtaSettingKey.SETTING_KEY_WIFI_CONNECT_DATE,
                defValue);
    }

    public static void setWifiConnectDate(Context context, String value) {
        ApplicationDataManager dataManager = ApplicationDataManager.getInstance(context);
        dataManager.putString(DataOwner.OTA_SETTING, OtaSettingKey.SETTING_KEY_WIFI_CONNECT_DATE, value);
    }

    public static void setLastCheckTime(Context context, long value) {
        ApplicationDataManager dataManager = ApplicationDataManager.getInstance(context);
        dataManager.putLong(DataOwner.OTA_SETTING, OtaSettingKey.SETTING_KEY_LAST_CHECK_TIME, value);
    }

    public static long getLastCheckTime(Context context, long defValue) {
        ApplicationDataManager dataManager = ApplicationDataManager.getInstance(context);
        return dataManager
                .getLong(DataOwner.OTA_SETTING, OtaSettingKey.SETTING_KEY_LAST_CHECK_TIME, defValue);
    }

    public static boolean isHaveNewVersionApplicationIcon(Context context, boolean defValue) {
        ApplicationDataManager dataManager = ApplicationDataManager.getInstance(context);
        return dataManager.getBoolean(DataOwner.OTA_SETTING,
                OtaSettingKey.SETTING_KEY_IS_HAVE_NEW_VERSION_ICON, defValue);
    }

    public static String getLastNotifyVersion(Context context, String defValue) {
        ApplicationDataManager dataManager = ApplicationDataManager.getInstance(context);
        return dataManager.getString(DataOwner.OTA_SETTING,
                ApplicationDataManager.OtaSettingKey.SETTING_KEY_LAST_NOTIFY_SYSTEM_VERSION, defValue);
    }

    public static void setNotifyVersion(Context context, String newVersionNum) {
        ApplicationDataManager dataManager = ApplicationDataManager.getInstance(context);
        dataManager.putString(DataOwner.OTA_SETTING,
                ApplicationDataManager.OtaSettingKey.SETTING_KEY_LAST_NOTIFY_SYSTEM_VERSION, newVersionNum);
    }
    
    public static boolean isAutoCheckAfterBootComplete(Context context, boolean defValue) {
        ApplicationDataManager dataManager = ApplicationDataManager.getInstance(context);
        return dataManager.getBoolean(DataOwner.OTA_SETTING,
                ApplicationDataManager.OtaSettingKey.SETTING_KEY_AUTOCHECK_AFTER_BOOTCOMPLETE, defValue);
    }
    
    public static void setAutoCheckAfterBootComplete(Context context, boolean autochek) {
        ApplicationDataManager dataManager = ApplicationDataManager.getInstance(context);
        dataManager.putBoolean(DataOwner.OTA_SETTING,
                ApplicationDataManager.OtaSettingKey.SETTING_KEY_AUTOCHECK_AFTER_BOOTCOMPLETE, autochek);
    }
    
    public static void setShowNotify(Context context, boolean value) {
        ApplicationDataManager dataManager = ApplicationDataManager.getInstance(context);
        dataManager.putBoolean(DataOwner.OTA_SETTING,
                ApplicationDataManager.OtaSettingKey.SETTING_KEY_SHOW_NOTIFY, value);
    }

    public static void setNotifySameVersion(Context context, boolean flag) {
        ApplicationDataManager dataManager = ApplicationDataManager.getInstance(context);
        dataManager.putBoolean(DataOwner.OTA_SETTING, OtaSettingKey.SETTING_KEY_NOTIFY_SAME_VERSION, flag);
    }

    public static boolean getNotifySameVersion(Context context, boolean defValue) {
        ApplicationDataManager dataManager = ApplicationDataManager.getInstance(context);
        return dataManager.getBoolean(DataOwner.OTA_SETTING, OtaSettingKey.SETTING_KEY_NOTIFY_SAME_VERSION, defValue);
    }

    public static void setNotifyNewVersionAfterBootComplete(Context context, boolean flag) {
        ApplicationDataManager dataManager = ApplicationDataManager.getInstance(context);
        dataManager.putBoolean(DataOwner.OTA_SETTING, OtaSettingKey.SETTING_KEY_NOTIFY_NEW_VERSION, flag);
    }

    public static boolean getNotifyNewVersionAfterBootComplete(Context context, boolean defValue) {
        ApplicationDataManager dataManager = ApplicationDataManager.getInstance(context);
        return dataManager.getBoolean(DataOwner.OTA_SETTING, OtaSettingKey.SETTING_KEY_NOTIFY_NEW_VERSION, defValue);
    }
}
