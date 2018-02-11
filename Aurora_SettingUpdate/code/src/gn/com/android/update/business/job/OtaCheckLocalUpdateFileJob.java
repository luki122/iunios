package gn.com.android.update.business.job;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import android.content.Context;
import gn.com.android.update.business.IBaseCallback;
import gn.com.android.update.business.IOtaCheckLocalUpgradeFileCallback;
import gn.com.android.update.utils.Error;
import gn.com.android.update.utils.FileUtil;
import gn.com.android.update.utils.LogUtils;
import gn.com.android.update.utils.StorageUtil;
import gn.com.android.update.utils.Util;

public class OtaCheckLocalUpdateFileJob extends Job {
    private static final String TAG = "UpgradeFromLoaclFileJob";
    private BufferedReader mReader;
    private File mUpdateFile;
    private ZipFile mZipFile;
    private Context mContext;
    private IOtaCheckLocalUpgradeFileCallback mCallback = null;

    public OtaCheckLocalUpdateFileJob(Context context, File updateFile) {
        super(TAG);
        mContext = context;
        mUpdateFile = updateFile;
    }

    @Override
    public void runTask() {
        try {

            if (!checkFileExists()) {
                LogUtils.logd(TAG, "checkFileExists");
                return;
            }

            if (!checkUpdatePackage()) {
                mErrorCode = Error.ERROR_CODE_WRONG_UPDATE_FILE;
                LogUtils.logd(TAG, "ERROR_CODE_WRONG_UPDATE_FILE");
                return;
            }
            LogUtils.logd(TAG, "runTask()");
        } finally {
            mContext = null;
        }

    }

    private boolean checkUpdatePackage() {
        BufferedReader reader = null;
        LogUtils.logd(TAG, "checkUpdatePackage begin");
        try {
            mZipFile = new ZipFile(mUpdateFile);
            ZipEntry propEntry = mZipFile.getEntry("system/build.prop");

            if (propEntry != null) {
                return checkFullPackage(mZipFile, propEntry);

            } else {
                return checkIncPackage(mZipFile);
            }

        } catch (ZipException ze) {
            ze.printStackTrace();
            LogUtils.loge(TAG, "checkUpdatePackage() " + ze.toString());
        } catch (IOException ie) {
            ie.printStackTrace();
            LogUtils.loge(TAG, "checkUpdatePackage() " + ie.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FileUtil.closeReader(reader);
            closeZipFile(mZipFile);
        }
        LogUtils.logd(TAG, "checkUpdatePackage end");
        return false;
    }

    /*if is full package, we check the "ro.product.model' prop is same as phone  
     * if same, we check the build time , if later than the phone;
     * otherwise check failed
     * */
    private boolean checkFullPackage(ZipFile zipFile, ZipEntry propEntry) throws IOException {
        String line = null;
        String buildTime = "";
        String projectID = Util.getModel();

        mReader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(propEntry)));

        while ((line = mReader.readLine()) != null) {
            if (line.contains("ro.build.date.utc")) {
                buildTime = line.substring(line.indexOf("=") + 1);
                LogUtils.logd(TAG, "checkFullPackage() buildTime = " + buildTime);
            }

            if (line.indexOf(projectID) != -1) {

                return checkBuildTimeIsLater(buildTime);
            }
        }

        LogUtils.loge(TAG, "checkUpdatePackage() build.prop not contains ro.gn.gnproductid");
        return false;
    }

    private boolean checkBuildTimeIsLater(String buildTime) throws IOException {
        try {
            if (Integer.valueOf(buildTime.trim()) > Integer.valueOf(Util.getBuildTime().trim())) {
                return true;
            }

        } catch (NumberFormatException e) {
            LogUtils.loge(TAG, "checkFullPackage () " + e.toString());
        }
        return false;
    }

    /*if is inc package, we check the "ro.build.fingerprint' prop is same as phone  
     * if same, go on;
     * otherwise check failed
     * */
    private boolean checkIncPackage(ZipFile zipFile) throws IOException {
        ZipEntry scriptEntry = zipFile.getEntry("META-INF/com/google/android/updater-script");

        if (scriptEntry == null) {
            LogUtils.loge(TAG, "checkIncPackage() updater-script is null");
            return false;
        }

        mReader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(scriptEntry)));
        String line = null;
        String fingerPrint = Util.getfingerPrint();

        while ((line = mReader.readLine()) != null) {
            if (line.indexOf(fingerPrint) != -1) {
                return true;

            } else {
                LogUtils.loge(TAG, "checkUpdatePackage() build.prop not contains ro.gn.fingerPrint");
            }
        }
        return false;
    }

    private void closeZipFile(ZipFile zipFile) {
        if (zipFile != null) {
            try {
                zipFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            zipFile = null;
        }
    }

    @Override
    public void registerCallback(IBaseCallback callback) {
        if (callback instanceof IOtaCheckLocalUpgradeFileCallback) {
            mCallback = (IOtaCheckLocalUpgradeFileCallback) callback;
        } else {
            throw new IllegalArgumentException("wrong callback type");
        }
    }

    @Override
    public void unRegisterCallback() {
        mCallback = null;
    }

    @Override
    public void onResult() {
        logd("onResult() mStatus = " + mStatus);

        if (mCallback == null) {
            loge("onResult() mCheckVersionCallback is null");
            return;
        }

        switch (mStatus) {
            case STATUS_ERROR:
                mCallback.onError(mErrorCode);
                break;
            case STATUS_COMPLETE:
                mCallback.onCheckComplete();
            default:
                break;
        }

        mCallback = null;
    }

    private boolean checkFileExists() {
        try {
            if (StorageUtil.isNoStorageMounted(mContext)) {
                mErrorCode = Error.ERROR_CODE_STORAGE_NOT_MOUNTED;
                LogUtils.log(TAG, "Error.ERROR_CODE_STORAGE_NOT_MOUNTED;");
                return false;
            }

            if (!mUpdateFile.exists()) {
                mErrorCode = Error.ERROR_CODE_FILE_NOT_FOUND;
                LogUtils.logd(TAG, "Error.ERROR_CODE_FILE_NOT_FOUND");
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

}
