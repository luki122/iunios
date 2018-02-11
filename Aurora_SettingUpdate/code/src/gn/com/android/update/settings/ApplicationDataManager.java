package gn.com.android.update.settings;

import android.content.Context;

public class ApplicationDataManager {
    private static ApplicationDataManager mInstance = null;
    private OtaSettingSharedPreferenceOperator mOtaSettingSharedPreferenceOperator;
    private OtaPushSettingSharedPreferenceOperator mOtaPushSettingSharedPreferenceOperator;
    private NameValueCache mNameValueCache = new NameValueCache();

    private ApplicationDataManager(Context context) {
        mOtaSettingSharedPreferenceOperator = new OtaSettingSharedPreferenceOperator(context);
        mOtaPushSettingSharedPreferenceOperator = new OtaPushSettingSharedPreferenceOperator(context);

    }

    public static synchronized ApplicationDataManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ApplicationDataManager(context);
        }
        return mInstance;
    }

    private SharePreferenceOperator getSharePreferenceOperatorByOwner(DataOwner dataOwner) {
        SharePreferenceOperator sharePreferenceOperator = null;
        switch (dataOwner) {
            case OTA_SETTING:
                sharePreferenceOperator = mOtaSettingSharedPreferenceOperator;
                break;
            case OTA_PUSH_SETTING:
                sharePreferenceOperator = mOtaPushSettingSharedPreferenceOperator;
                break;
            case APPS_UPGRADE_SETTING:
                break;
            default:
                throw new IllegalArgumentException("wrong DataOwner");
        }
        return sharePreferenceOperator;
    }

    public synchronized String getString(DataOwner dataOwner, String name, String defValue) {
        SharePreferenceOperator sharePreferenceOperator = getSharePreferenceOperatorByOwner(dataOwner);
        return mNameValueCache.getString(name, defValue, sharePreferenceOperator);
    }

    public synchronized int getInt(DataOwner dataOwner, String name, int defValue) {
        if (OtaSettingKey.SETTING_KEY_AUTO_CHECK_DURATION.equals(name)) {
            return mOtaSettingSharedPreferenceOperator.getIntValue(name, defValue);
        }

        SharePreferenceOperator sharePreferenceOperator = getSharePreferenceOperatorByOwner(dataOwner);
        return mNameValueCache.getInt(name, defValue, sharePreferenceOperator);
    }

    public synchronized boolean getBoolean(DataOwner dataOwner, String name, boolean defValue) {
        if (OtaSettingKey.SETTING_KEY_AUTO_CHECK_ENABLE.equals(name)
                || OtaSettingKey.SETTING_KEY_AUTO_DOWNLOAD_ENABLED.equals(name)) {
            return mOtaSettingSharedPreferenceOperator.getBooleanValue(name, defValue);
        }

        SharePreferenceOperator sharePreferenceOperator = getSharePreferenceOperatorByOwner(dataOwner);
        return mNameValueCache.getBoolean(name, defValue, sharePreferenceOperator);
    }

    public synchronized long getLong(DataOwner dataOwner, String name, long defValue) {
        SharePreferenceOperator sharePreferenceOperator = getSharePreferenceOperatorByOwner(dataOwner);
        return mNameValueCache.getLong(name, defValue, sharePreferenceOperator);
    }

    public synchronized void putString(DataOwner dataOwner, String name, String value) {
        SharePreferenceOperator sharePreferenceOperator = getSharePreferenceOperatorByOwner(dataOwner);
        mNameValueCache.putString(name, value, sharePreferenceOperator);
    }

    public synchronized void putInt(DataOwner dataOwner, String name, int value) {
        SharePreferenceOperator sharePreferenceOperator = getSharePreferenceOperatorByOwner(dataOwner);
        mNameValueCache.putInt(name, value, sharePreferenceOperator);
    }

    public synchronized void putBoolean(DataOwner dataOwner, String name, boolean value) {
        SharePreferenceOperator sharePreferenceOperator = getSharePreferenceOperatorByOwner(dataOwner);
        mNameValueCache.putBoolean(name, value, sharePreferenceOperator);
    }

    public synchronized void putLong(DataOwner dataOwner, String name, long value) {
        SharePreferenceOperator sharePreferenceOperator = getSharePreferenceOperatorByOwner(dataOwner);
        mNameValueCache.putLong(name, value, sharePreferenceOperator);
    }

    public synchronized void remove(DataOwner dataOwner, String name) {
        SharePreferenceOperator sharePreferenceOperator = getSharePreferenceOperatorByOwner(dataOwner);
        mNameValueCache.remove(name, sharePreferenceOperator);
    }

    public static enum DataOwner {
        OTA_SETTING, OTA_PUSH_SETTING, APPS_UPGRADE_SETTING
    }

    public void destroy() {
//        mInstance = null;
//        mOtaSettingSharedPreferenceOperator = null;
//        mOtaPushSettingSharedPreferenceOperator = null;
//        mNameValueCache.clearCache();
//        mNameValueCache = null;

    }

    public static final class OtaSettingKey {
        public static final String SETTING_KEY_AUTO_CHECK_DURATION = "auto_update_key_duration";
        public static final String SETTING_KEY_AUTO_CHECK_ENABLE = "auto_update_key_enable";
        public static final String SETTING_KEY_AUTO_DOWNLOAD_ENABLED = "auto_download_only_wlan";

        public static final String SETTING_KEY_IS_HAVE_NEW_VERSION_ICON = "lancher_have_one_or_not";
        public static final String SETTING_KEY_LAST_UPDATE_TIME = "last_update_time_key";
        public static final String SETTING_KEY_LAST_CHECK_TIME = "last_check_time_key";
        public static final String SETTING_KEY_LAST_AUTO_CHECK_TIME = "last_auto_check_time_key";

        public static final String SETTING_KEY_UPGRADE_VERSION = "update_version";
        public static final String SETTING_KEY_CURRENT_VERSION_RELEASE_NOTE = "current_version_desc";
        public static final String SETTING_KEY_CURRENT_VERSION_RELEASE_NOTE_URL = "current_version_releasenote";

        public static final String SETTING_KEY_THIS_DOWNLOAD_IS_AUTO = "wifi_auto_download";

        public static final String SETTING_KEY_WIFI_CONNECT_DATE = "wifi_connect_date";
        public static final String SETTING_KEY_LAST_NOTIFY_SYSTEM_VERSION = "notify_version_key";
        public static final String SETTING_KEY_AUTOCHECK_AFTER_BOOTCOMPLETE = "autocheck_after_bootcomplete_key";

        public static final String SETTING_KEY_LAST_BEGIN_DOWNLOAD_URL = "ota_last_begin_download_url";
        public static final String SETTING_KEY_LAST_END_DOWNLOAD_URL = "ota_last_end_download_url";

        public static final String SETTING_KEY_NOTIFY_SAME_VERSION = "notify_same_version";
        public static final String SETTING_KEY_NOTIFY_NEW_VERSION = "notify_new_version";
        
        public static final String SETTING_KEY_SHOW_NOTIFY = "show_notify";
    }

    public static final class OtaPushSettingKey {
        public static final String PUSH_NEED_REGISTERED_TO_GPE = "receiver_notifier_first_register";
        public static final String PUSH_RID = "receiver_notifier_rid";
        public static final String PUSH_REGISTER_OR_UNREGISTER_TO_APS = "receiver_notifier_register_or_unregister";
        public static final String PUSH_REGISTER_OR_UNREGISTER_TO_APS_STATE = "receiver_notifier_register_or_unregister_state";
        public static final String PUSH_ID = "receiver_notifier_push_id";
    }

}
