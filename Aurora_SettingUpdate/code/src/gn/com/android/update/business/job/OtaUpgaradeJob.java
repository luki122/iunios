package gn.com.android.update.business.job;

import gn.com.android.update.business.IBaseCallback;
import gn.com.android.update.business.IOtaUpgradeCallback;
import gn.com.android.update.business.OtaUpgradeInfo;
import gn.com.android.update.business.OtaUpgradeManager;
import gn.com.android.update.settings.OtaSettings;
import gn.com.android.update.utils.FileUtil;
import gn.com.android.update.utils.StorageUtil;
import gn.com.android.update.utils.Util;
import gn.com.android.update.utils.Error;

import java.io.File;

import android.content.Context;

public class OtaUpgaradeJob extends Job {
    private static final String TAG = "OtaUpgaradeJob";
    private File mUpdateFile;
    private String mMd5;
    private Context mContext;
    private IOtaUpgradeCallback mCallback = null;

    public OtaUpgaradeJob(Context context, File updateFile, String md5) {
        super(TAG);
        mUpdateFile = updateFile;
        mMd5 = md5;
        mContext = context;

    }

    private boolean checkFileExists() {
        if (StorageUtil.isNoStorageMounted(mContext)) {
            mErrorCode = Error.ERROR_CODE_STORAGE_NOT_MOUNTED;
            return false;
        }

        if (!mUpdateFile.exists()) {
            mErrorCode = Error.ERROR_CODE_FILE_NOT_FOUND;
            return false;
        }

        return true;
    }

    public void runTask() {
        try {

            if (!checkFileExists()) {
                return;
            }

            if (FileUtil.verifyFileByMd5(mUpdateFile, mMd5)) {
                storeCurrentVersionImproveInfo();

                Util.sendUpgradBroadcast(mContext, mUpdateFile);

            } else {
                mErrorCode = Error.ERROR_CODE_FILE_VERIFY_FAILED;
            }

        } finally {
            mContext = null;
        }
    }

    private void storeCurrentVersionImproveInfo() {
        OtaUpgradeManager otaUpgradeManager = OtaUpgradeManager.getInstance(mContext);
        OtaUpgradeInfo otaUpgradeInfo = otaUpgradeManager.getOtaUpgradeInfo();

        OtaSettings.setCurrentVersionImproveInfo(mContext, otaUpgradeInfo.getReleaseNote(),
                otaUpgradeInfo.getReleaseNoteUrl(), otaUpgradeInfo.getInternalVer());
        OtaSettings.setNotifyNewVersionAfterBootComplete(mContext, true);
    }

    @Override
    public void unRegisterCallback() {

        mCallback = null;

    }

    @Override
    public void onResult() {
        logd("onResult() mStatus = " + mStatus);
        try {
            if (mCallback == null) {
                loge("onResult() mCheckVersionCallback is null");
                return;
            }

            switch (mStatus) {
                case STATUS_ERROR:
                    mCallback.onError(mErrorCode);
                    break;
                default:
                    break;
            }
        } finally {

            mCallback = null;
        }

    }

    @Override
    public void registerCallback(IBaseCallback callback) {
        if (callback instanceof IOtaUpgradeCallback) {
            mCallback = (IOtaUpgradeCallback) callback;
        } else {
            throw new IllegalArgumentException("wrong callback type");
        }

    }

}
