package gn.com.android.update.business.job;

import android.content.Context;
import gn.com.android.update.business.IBaseCallback;
import gn.com.android.update.business.NetworkConfig;
import gn.com.android.update.push.PushHelper;
import gn.com.android.update.settings.ApplicationDataManager;
import gn.com.android.update.utils.HttpUtils;
import gn.com.android.update.utils.LogUtils;
import gn.com.android.update.utils.Util;

public class ConnectApsJob extends Job {
    private Context mContext = null;
    private static final String TAG = "ConnectApsJob";
    private static final String APS_SERVER = "http://apsota.gionee.com";
    private ApplicationDataManager mDataManager;
    private String mRid = null;
    private boolean mRegister = false;

    public ConnectApsJob(Context context, boolean register, String rid) {
        mContext = context;
        mDataManager = ApplicationDataManager.getInstance(mContext);
        mRegister = register;
        mRid = rid;
    }

    @Override
    public void runTask() {
        try {
            sendInfoToAps(mRid, mRegister);

        } finally {
            mContext = null;
            mDataManager = null;
            mRid = null;
        }
    }

    @Override
    public void registerCallback(IBaseCallback callback) {

    }

    @Override
    public void unRegisterCallback() {

    }

    @Override
    public void onResult() {

    }

    private void sendInfoToAps(String rid, boolean register) {
        String imei = Util.getImei(mContext);
        boolean isWapNetwork = HttpUtils.isWapConnection(mContext);
        try {
            String result = HttpUtils.executeHttpPost(isWapNetwork, createServerUrl(register, mRid), null,
                    imei);
            String action = PushHelper.getLastApsRegisterAction(mDataManager, "register");
            if (result != null && result.contains("success")) {
                if (!register) {
                    PushHelper.remove(mDataManager, ApplicationDataManager.OtaPushSettingKey.PUSH_ID);
                    PushHelper.remove(mDataManager, ApplicationDataManager.OtaPushSettingKey.PUSH_RID);
                }

                if (action.equals(PushHelper.ACTION_REGISTER) == register) {
                    PushHelper.setApsRegisterOrUnregisterState(true, mDataManager);
                    LogUtils.logd(TAG, "sendInfoToAps() register or unregister ok");
                }

            } else {

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String createServerUrl(boolean register, String rid) {
        StringBuilder apsServer = new StringBuilder();

        if (Util.isTestEnvironment()) {
            //apsServer.append(NetworkConfig.TEST_HOST);
            apsServer.append(NetworkConfig.IUNIOS_TEST_HOST);
        } else {
            apsServer.append(APS_SERVER);
        }

        if (register) {
            apsServer.append("/apsota/reg.do?");
            String imei = Util.getImei(mContext);
            apsServer.append("at=" + imei);
            apsServer.append("&rid=" + rid);
            apsServer.append("&mod=" + Util.getModel());
            apsServer.append("&ver=" + Util.getAndroidVersion());
            apsServer.append("&vc=" + Util.getGioneeVersion());

        } else {
            apsServer.append("/apsota/logout.do?");
            apsServer.append("&rid=" + rid);
        }

        return apsServer.toString();
    }
}
