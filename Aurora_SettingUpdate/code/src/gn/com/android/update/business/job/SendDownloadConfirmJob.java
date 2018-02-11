package gn.com.android.update.business.job;

import android.content.Context;
import gn.com.android.update.business.IBaseCallback;
import gn.com.android.update.business.NetworkConfig;
import gn.com.android.update.settings.ApplicationDataManager;
import gn.com.android.update.settings.ApplicationDataManager.DataOwner;
import gn.com.android.update.utils.HttpUtils;
import gn.com.android.update.utils.LogUtils;
import gn.com.android.update.utils.Util;

public class SendDownloadConfirmJob extends Job {
    private static final String TAG = "SendDownloadConfirmJob";
    private Context mContext = null;
    private boolean mIsStart = true;
    private String mDownloadUrl = null;
    private ApplicationDataManager mDataManager = null;
    private static final String SERVER_HOST = "http://red.gionee.com";

    public SendDownloadConfirmJob(Context context, boolean isStart, String downloadUrl) {
        super(TAG);
        mContext = context;
        mIsStart = isStart;
        mDataManager = ApplicationDataManager.getInstance(mContext);
        mDownloadUrl = downloadUrl;
    }

    private String getLastBeginDownloadUrl() {
        return mDataManager.getString(DataOwner.OTA_SETTING,
                ApplicationDataManager.OtaSettingKey.SETTING_KEY_LAST_BEGIN_DOWNLOAD_URL, "");
    }

    private String getLastEndDownloadUrl() {
        return mDataManager.getString(DataOwner.OTA_SETTING,
                ApplicationDataManager.OtaSettingKey.SETTING_KEY_LAST_END_DOWNLOAD_URL, "");
    }

    private void setLastBeginDownloadUrl() {
        mDataManager.putString(DataOwner.OTA_SETTING,
                ApplicationDataManager.OtaSettingKey.SETTING_KEY_LAST_BEGIN_DOWNLOAD_URL, mDownloadUrl);
    }

    private void setLastEndDownloadUrl() {
        mDataManager.putString(DataOwner.OTA_SETTING,
                ApplicationDataManager.OtaSettingKey.SETTING_KEY_LAST_END_DOWNLOAD_URL, mDownloadUrl);
    }

    @Override
    public void runTask() {
        try {

            if (needSendDownloadConfirm(mIsStart)) {
                String imei = Util.getImei(mContext);
                String url = getRequestUrl(mIsStart, imei);
                sendDownloadConfirm(url, imei);
            }

        } finally {
            mContext = null;
            mDataManager = null;
            mDownloadUrl = null;
        }

    }

    private boolean needSendDownloadConfirm(boolean isStart) {
        if (isStart) {
            String lastDownloadUrl = getLastBeginDownloadUrl();
            if (lastDownloadUrl.equals(mDownloadUrl)) {
                return false;
            }
        } else {
            String lastEndDownloadUrl = getLastEndDownloadUrl();
            if (lastEndDownloadUrl.equals(mDownloadUrl)) {
                return false;
            }
        }
        return true;
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

    private String getRequestUrl(boolean isStart, String imei) {
        StringBuffer urlString = new StringBuffer();

        if (Util.isTestEnvironment()) {
            //urlString.append(NetworkConfig.TEST_HOST);
            urlString.append(NetworkConfig.IUNIOS_TEST_HOST);
        } else {
            urlString.append(SERVER_HOST);
        }

        urlString.append("/cllt/ota/");

        if (isStart) {
            urlString.append("12100/");
        } else {
            urlString.append("11100/");
        }

        urlString.append(mContext.getPackageName());
        urlString.append("&imei=" + Util.getEncryptionImei(imei));

        return urlString.toString();
    }

    private void sendDownloadConfirm(String url, String imei) {
        boolean isWapNetwork = HttpUtils.isWapConnection(mContext);
        String result = HttpUtils.executeHttpPost(isWapNetwork, url, null, imei);
        LogUtils.log(TAG, "sendDownloadConfirm result = " + result);
        if (result != null) {
            if (mIsStart) {
                setLastBeginDownloadUrl();
            } else {
                setLastEndDownloadUrl();
            }
        }

    }
}
