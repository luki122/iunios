package gn.com.android.update.push;

import gn.com.android.update.business.Config;
import gn.com.android.update.business.OtaUpgradeManager;
import gn.com.android.update.business.OtaUpgradeState;
import gn.com.android.update.settings.ApplicationDataManager;
import gn.com.android.update.utils.LogUtils;
import gn.com.android.update.utils.Util;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import org.json.JSONException;
import org.json.JSONObject;

public class PushReceiver extends BroadcastReceiver {
    private static final String TAG = "PushReceiver";
    private ApplicationDataManager mDataManager = null;

    public void onReceive(Context context, Intent intent) {
        if (!Config.PUSH_SUPPORT) {
            return;
        }

        String action = intent.getAction();
        mDataManager = ApplicationDataManager.getInstance(context);

        if ("com.gionee.cloud.intent.REGISTRATION".equals(action)) {
            handleRegistrationAction(intent, context);

        } else if ("com.gionee.cloud.intent.RECEIVE".equals(action)) {
            handleReceiveIntent(intent, context);

        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            handleConnectivityIntent(context);
        }
    }

    private void handleConnectivityIntent(Context context) {
        String rid = PushHelper.getRid(mDataManager, null);
        if (rid == null || rid.length() == 0) {
            return;
        }

        String pushAction = PushHelper.getLastApsRegisterAction(mDataManager, null);
        if (pushAction == null) {
            return;
        }

        if (PushHelper.getLastRegisterOrUnregisterState(mDataManager, false) == false) {
            if (pushAction.equals(PushHelper.ACTION_REGISTER)) {
                PushHelper.registerRid(rid, context);
            } else {
                PushHelper.unregisterRid(rid, context);
            }

        }
    }

    private void handleReceiveIntent(Intent intent, Context context) {
        String message = intent.getStringExtra("message");
        LogUtils.logv(TAG, "handleReceiveIntent  message = " + message);
        if (message == null || message.length() == 0) {
            return;
        }

        OtaUpgradeManager otaUpgradeManager = OtaUpgradeManager.getInstance(context);
        otaUpgradeManager.sendVersionBroadcastToLauncher(true);

        OtaUpgradeState otaUpgradeState = otaUpgradeManager.getOtaUpgradeState();
        if (otaUpgradeState == OtaUpgradeState.INITIAL) {
            String newVersionNum = parsePushMessage(message);
            otaUpgradeManager.sendNewVersionNotification(newVersionNum, true);

        } else {
            LogUtils.loge(TAG, "handleReceiveIntent() otaUpgradeState = " + otaUpgradeState);
        }

    }

    private String parsePushMessage(String message) {
        String newVersionNum = "";
        int pushID = Config.ERROR_PUSH_ID;

        try {
            JSONObject jsonObj = new JSONObject(message);
            pushID = jsonObj.getInt("push_id");
            newVersionNum = jsonObj.getString("content");
        } catch (JSONException e) {
            LogUtils.loge(TAG, e.toString());
        }

        if (isNewVersion(newVersionNum)) {
            PushHelper.setPushID(mDataManager, pushID);
        }

        return newVersionNum;
    }

    private boolean isNewVersion(String newVersionNum) {
        if (newVersionNum == null || newVersionNum.length() == 0) {
            return false;
        }

        String phoneVersion = Util.getGioneeVersion();
        String newVersionProjectName = "";
        String phoneProjectName = "";

        try {
            newVersionProjectName = newVersionNum.substring(0, newVersionNum.lastIndexOf("_T"));
            phoneProjectName = phoneVersion.substring(0, newVersionNum.lastIndexOf("_T"));
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            return false;
        }

        if (!newVersionProjectName.equals(phoneProjectName)) {
            return false;
        }

        if (newVersionNum.compareTo(phoneVersion) > 0) {
            return true;
        } else {
            return false;
        }
    }

    private void handleRegistrationAction(Intent intent, Context context) {
        String rid = intent.getStringExtra("registration_id");
        String cancelRid = intent.getStringExtra("cancel_RID");
        String error = intent.getStringExtra("error");

        LogUtils.logd(TAG, "handleRegistrationAction rid = " + "---" + rid);

        if (rid != null && rid.length() != 0) {
            PushHelper.setIfNeedRegisteredToGpe(mDataManager, false);

            PushHelper.setRid(mDataManager, rid);
            PushHelper.registerRid(rid, context);

        } else if (cancelRid != null && cancelRid.length() != 0) {
            LogUtils.logd(TAG, "handleRegistrationAction cancelRid = " + "---" + cancelRid);
            PushHelper.remove(mDataManager, ApplicationDataManager.OtaPushSettingKey.PUSH_RID);
            PushHelper.unregisterRid(cancelRid, context);

        } else if (error != null) {
            LogUtils.loge(TAG, "handleRegistrationAction() error = " + error);
        }

    }

}
