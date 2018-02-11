package gn.com.android.update.push;

import android.content.Context;
import android.content.Intent;
import gn.com.android.update.business.ThreadPoolManager;
import gn.com.android.update.business.job.ConnectApsJob;
import gn.com.android.update.settings.ApplicationDataManager;
import gn.com.android.update.settings.ApplicationDataManager.DataOwner;
import gn.com.android.update.utils.LogUtils;

public class PushHelper {
    private static final String TAG = "PushHelper";
    public static final String ACTION_REGISTER = "register";
    public static final String ACTION_UNREGISTER = "unregister";

    public static boolean getLastRegisterOrUnregisterState(ApplicationDataManager dataManager,
            boolean defValue) {
        return dataManager.getBoolean(DataOwner.OTA_PUSH_SETTING,
                ApplicationDataManager.OtaPushSettingKey.PUSH_REGISTER_OR_UNREGISTER_TO_APS_STATE, defValue);
    }

    public static void setApsRegisterOrUnregisterState(boolean flag, ApplicationDataManager dataManager) {
        dataManager.putBoolean(DataOwner.OTA_PUSH_SETTING,
                ApplicationDataManager.OtaPushSettingKey.PUSH_REGISTER_OR_UNREGISTER_TO_APS_STATE, flag);
    }

    public static String getLastApsRegisterAction(ApplicationDataManager dataManager, String defValue) {
        return dataManager.getString(DataOwner.OTA_PUSH_SETTING,
                ApplicationDataManager.OtaPushSettingKey.PUSH_REGISTER_OR_UNREGISTER_TO_APS, defValue);
    }

    public static void setApsRegisterAction(ApplicationDataManager dataManager, String action) {
        dataManager.putString(DataOwner.OTA_PUSH_SETTING,
                ApplicationDataManager.OtaPushSettingKey.PUSH_REGISTER_OR_UNREGISTER_TO_APS, action);
    }

    public static boolean needRegisteredToGpe(ApplicationDataManager dataManager, boolean defValue) {
        return dataManager.getBoolean(DataOwner.OTA_PUSH_SETTING,
                ApplicationDataManager.OtaPushSettingKey.PUSH_NEED_REGISTERED_TO_GPE, defValue);
    }

    public static void setIfNeedRegisteredToGpe(ApplicationDataManager dataManager, boolean value) {
        dataManager.putBoolean(DataOwner.OTA_PUSH_SETTING,
                ApplicationDataManager.OtaPushSettingKey.PUSH_NEED_REGISTERED_TO_GPE, value);
    }

    public static void registerGpe(Context context) {
        LogUtils.logd(TAG, "registerGpe");
       /*Intent intent = new Intent().setAction("com.gionee.cloud.intent.REGISTER").putExtra("packagename",
                "gn.com.android.update");
        context.startService(intent);*/
    }

    public static void unregisterGpe(Context context) {
        Intent intent = new Intent().setAction("com.gionee.cloud.intent.UNREGISTER").putExtra("packagename",
                "gn.com.android.update");
        context.startService(intent);
    }

    public static String getRid(ApplicationDataManager dataManager, String defValue) {
        return dataManager.getString(DataOwner.OTA_PUSH_SETTING,
                ApplicationDataManager.OtaPushSettingKey.PUSH_RID, defValue);
    }

    public static void setRid(ApplicationDataManager dataManager, String value) {
        dataManager.putString(DataOwner.OTA_PUSH_SETTING, ApplicationDataManager.OtaPushSettingKey.PUSH_RID,
                value);
    }

    public static int getPushID(ApplicationDataManager dataManager, int defValue) {
        return dataManager.getInt(DataOwner.OTA_PUSH_SETTING,
                ApplicationDataManager.OtaPushSettingKey.PUSH_ID, defValue);
    }

    public static void setPushID(ApplicationDataManager dataManager, int value) {
        dataManager.putInt(DataOwner.OTA_PUSH_SETTING, ApplicationDataManager.OtaPushSettingKey.PUSH_ID,
                value);
    }

    public static void remove(ApplicationDataManager dataManager, String key) {
        dataManager.remove(DataOwner.OTA_PUSH_SETTING, key);
    }

    public static void registerRid(String rid, Context context) {
        ApplicationDataManager dataManager = ApplicationDataManager.getInstance(context);
        PushHelper.setApsRegisterAction(dataManager, PushHelper.ACTION_REGISTER);
        startRegisterOrUnregisterRid(context, true, rid, dataManager);
    }

    public static void unregisterRid(String rid, Context context) {
        ApplicationDataManager dataManager = ApplicationDataManager.getInstance(context);
        PushHelper.setApsRegisterAction(dataManager, PushHelper.ACTION_UNREGISTER);
        startRegisterOrUnregisterRid(context, false, rid, dataManager);
    }

    private static void startRegisterOrUnregisterRid(Context context, boolean register, String rid,
            ApplicationDataManager dataManager) {
        PushHelper.setApsRegisterOrUnregisterState(false, dataManager);
        ConnectApsJob job = new ConnectApsJob(context, register, rid);
        ThreadPoolManager threadPoolManager = ThreadPoolManager.getInstance();
        threadPoolManager.submitTask(job);
    }
}
