
package gn.com.android.update.business.job;

import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;
import java.util.regex.Pattern;

import gn.com.android.update.business.CheckVersionNeedInfo;
import gn.com.android.update.business.Config;
import gn.com.android.update.business.EnvironmentConfig;
import gn.com.android.update.business.IAppsCheckVersionCallback;
import gn.com.android.update.business.IBaseCallback;
import gn.com.android.update.business.IOtaCheckVersionCallback;
import gn.com.android.update.business.NetworkConfig;
import gn.com.android.update.business.OtaUpgradeInfo;
import gn.com.android.update.business.PackageUpgradeInfo;
import gn.com.android.update.business.parser.OtaUpgradeInfoParser;
import gn.com.android.update.business.parser.PackageUpgradeInfoParser;
import gn.com.android.update.utils.Error;
import gn.com.android.update.utils.HttpUtils;
import gn.com.android.update.utils.LogUtils;
import gn.com.android.update.utils.StorageUtil;
import gn.com.android.update.utils.Util;

import org.json.JSONException;
import android.os.SystemProperties;

public class CheckVersionJob extends Job {
    private static final String TAG = "CheckOtaVersionRunnable";
    private CheckVersionNeedInfo mCheckNeedInfo = null;
    private IBaseCallback mCheckVersionCallback = null;

    public static boolean mNeedTwiceCheck = false;

    public CheckVersionJob(CheckVersionNeedInfo checkVersionNeedInfo) {
        super(TAG);
        mCheckNeedInfo = checkVersionNeedInfo;
    }

    public CheckVersionJob(CheckVersionNeedInfo checkVersionNeedInfo, boolean needTwiceCheck) {
        super(TAG);
        mCheckNeedInfo = checkVersionNeedInfo;
        mNeedTwiceCheck = needTwiceCheck;
    }

    public void runTask() {
        boolean hasStorage = StorageUtil.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);
        LogUtils.logd(TAG, " runTask hasStorage =   " + hasStorage);
        int errorCode = NO_ERROR;
        if (!hasStorage) {
            mErrorCode = Error.ERROR_CODE_STORAGE_NOT_MOUNTED;
            return;
        }

        String checkUrl = null;
        if (mCheckNeedInfo.mIsOtaCheck) {
            checkUrl = creatOtaCheckUrl();
        } else {
            checkUrl = creatAppsCheckUrl();
        }

        String result = HttpUtils.executeHttpPost(mCheckNeedInfo.mIsWapNetwork,
                checkUrl.toString(), null,
                mCheckNeedInfo.mImei);
        LogUtils.logd(TAG, " runTask executeHttpPost  CheckVersion  'result   =   " + result);
        errorCode = getError(result);
        if(errorCode != NO_ERROR){
            mErrorCode = errorCode;
            return;
        }
        if (!isRightResult(result)) {
            mErrorCode = Error.ERROR_CODE_NETWORK_ERROR;
            return;
        }

        if (result.length() == 0) {
            return;
        }

        parseData(result);

    }

    private String creatAppsCheckUrl() {
        String url = "";
        return url;
    }

    private void parseData(String data) {
        try {
            if (mCheckNeedInfo.mIsOtaCheck) {
                mResultObject = OtaUpgradeInfoParser.parseOtaUpgradeInfo(data);
            } else {
                mResultObject = PackageUpgradeInfoParser.parseOtaUpgradeInfo(data);
            }

        } catch (JSONException e) {
            mErrorCode = Error.ERROR_CODE_NETWORK_ERROR;
            e.printStackTrace();
        }
    }

    private boolean isRightResult(String result) {

        if (result == null) {
            return false;
        } else if (result.length() != 0 && !result.contains("extPkg")) {
            return false;
        }

        return true;
    }

    /**
     * parse connection error
     * 
     * @param result
     * @return
     */
    private int getError(String result) {
        if (result == null) {
            return Error.ERROR_CODE_BAD_REQUEST;
        }

        if (Error.ERROR_STRING_CONNECTION_TIME_OUT.equals(result)) {
            return Error.ERROR_CODE_CONNECTION_TIME_OUT;
        }

        if (Error.ERROR_STRING_SERVER_ERROR.equals(result)) {
            return Error.ERROR_CODE_SERVER_ERROR;
        }

        if (Error.ERROR_STRING_SERVER_NOT_FOUND.equals(result)) {
            return Error.ERROR_CODE_SERVER_NOT_FOUND;
        }
        if (Error.ERROR_STRING_INTERNET_NOT_USERD.equals(result)) {
            return Error.ERROR_CODE_INTERNET_NOT_USED;
        }

        return NO_ERROR;
    }

    private boolean isNumeric(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    private String creatOtaCheckUrl() {
        StringBuffer checkUrl = new StringBuffer();
        String version = Util.getInternalVersion();// "IUNI-E6-AlphaV1.0-201312131351";//"IUNI-MI3W-AlphaV1.0-201405200658";//
        Log.e("luofu", "version");
        String[] subVersions = version.split("-");
        StringBuilder verBuilder = new StringBuilder();
        String lastNumberStr = null;
        String lessVersionNumber = null;
        if (subVersions != null && subVersions.length > 0) {
            lastNumberStr = subVersions[subVersions.length - 1];
            if (!TextUtils.isEmpty(lastNumberStr)) {
                for (int i = 0; i < lastNumberStr.length(); i++) {
                    String subStr = lastNumberStr.charAt(i) + "";
                    if (!isNumeric(subStr)) {
                        lastNumberStr = lastNumberStr.substring(0, i);
                        Log.e("ver", lastNumberStr);
                        break;
                    }
                }
            }

        }

        try {
            if (lastNumberStr != null) {
                if (isNumeric(lastNumberStr)) {
                    String lastNum = (Long.parseLong(lastNumberStr) - 1) + "";
                    Log.e("luofu", "last number:" + lastNum);
                    subVersions[subVersions.length - 1] = lastNum;
                }
                for (int i = 0; i < subVersions.length; i++) {
                    if (i == subVersions.length - 1) {
                        verBuilder.append(subVersions[i]);
                    } else {
                        verBuilder.append(subVersions[i] + "-");
                    }

                }
            }

            Log.e("luofu", "less version:" + verBuilder.toString());
            Log.e("luofu", "mNeedTwiceCheck:" + mNeedTwiceCheck);
            if (mNeedTwiceCheck) {
                version = verBuilder.toString();
                CheckVersionJob.mNeedTwiceCheck = false;
            }
        } catch (NumberFormatException e) {
            // TODO: handle exception
            version = Util.getInternalVersion();
        } catch (Exception e) {
            // TODO: handle exception
            version = Util.getInternalVersion();
        }

        checkUrl.append(HttpUtils.getServerHost());
        // checkUrl.append(NetworkConfig.GIONEE_HTTP_CHECK);
        checkUrl.append(NetworkConfig.IUNIOS_HTTP_CHECK);
        checkUrl.append("?pkg=COM.IUNI." + Util.getProduct());
        checkUrl.append("&vc=" + version);// verBuilder.toString());//Util.getInternalVersion());
        String recovery = getRecoveryVersion();
        if (!TextUtils.isEmpty(recovery)) {
            checkUrl.append("&rc=" + recovery);
        }
        // checkUrl.append("&rc="+"IUNI-U2-16G-Alpha-Recovery-V1.0-201405061052");//getRecoveryVersion()
       //version = Util.getInternalVersion();
        if (Util.getEnvironment() == EnvironmentConfig.TEST_ENVIRONMENT_TEST_VERSION) {
        	
            checkUrl.append("&state=3");
        }else if(Util.getEnvironment() == EnvironmentConfig.NORMAL_ENVIRONMENT_TEST_VERSION){
          
        	checkUrl.append("&normal_test=" + NetworkConfig.IUNIOS_NORMAL_TEST);
        }
        checkUrl.append("&pid=");
        int checkType = mCheckNeedInfo.mCheckType;
        int pushId = mCheckNeedInfo.mPushId;
        if (checkType == NetworkConfig.CHECK_TYPE_AUTO) {
            checkUrl.append("-1");
        } else if (checkType == NetworkConfig.CHECK_TYPE_PUSH && pushId != Config.ERROR_PUSH_ID) {
            checkUrl.append(pushId);
        } else {
            checkUrl.append("-2");
        }
        checkUrl.append("&vi=1");
        //Alpha版本 和 Beta版本默认都是没有root过
        if(version.contains("Alpha") || version.contains("Beta")){
        	 checkUrl.append("&isRoot=0");
        }else{
        	if(Util.isRoot()){
	        	 checkUrl.append("&isRoot=1");
	        	 
	        }else{
	        	 checkUrl.append("&isRoot=0");
	        }
        }
        HttpUtils.getAppendNetworkTypeUrl(checkUrl, mCheckNeedInfo.mConnectionType,
                mCheckNeedInfo.mIsWapNetwork);
        checkUrl.append("&imei=" + mCheckNeedInfo.mImei);
        Log.e("luofu", "checkURL:" + checkUrl.toString().replaceAll(" ", ""));

        return checkUrl.toString().replaceAll(" ", "");
    }

    private String getRecoveryVersion() {
        // "IUNI-E6-Alpha-Recovery-V1.0-201405061055";//
    	
    	//Begin modify by gary.gou 20140707
    	
        //return Util.getRecovery();
    	return VerifyRecovery(Util.getRecovery());
    	
    	//End modify by gary.gou 20140707
    }

    //Begin add by gary.gou 20140707
    private  String VerifyRecovery(String str){
    	String returnStr = str;
    	
    	if(str.equals("")){
    		return str;
    	}
    	
    	if(Build.MODEL.contains("U810")){
    		 String iuniBuildNumber = SystemProperties.get("ro.gn.iuniznvernumber","Unknown");
    		 
    		 if(!iuniBuildNumber.equals("Unknown")){
    			 if(iuniBuildNumber.contains("16G") && str.contains("32G")){
    				 returnStr = str.replaceAll("32G", "16G");
    			 }else if(iuniBuildNumber.contains("32G") && str.contains("16G")){
    				 returnStr = str.replaceAll("16G", "32G");
    			 }
    	 	} 
    	}
    	
    	return returnStr;
    }
    //End add by gary.gou 20140707
    
    @Override
    public void registerCallback(IBaseCallback callback) {
        if (callback == null) {
            LogUtils.loge(TAG, "registerCallback callback is null");
            return;
        }

        if (callback instanceof IOtaCheckVersionCallback
                || callback instanceof IAppsCheckVersionCallback) {
            mCheckVersionCallback = callback;
        } else {
            throw new IllegalArgumentException("wrong callback type");
        }

    }

    @Override
    public void unRegisterCallback() {
        mCheckVersionCallback = null;

    }

    @Override
    public void onResult() {

        try {
            if (mCanceled) {
                loge("onResult() already canceled");
                return;
            }

            logd("onResult() mStatus = " + mStatus);

            if (mCheckVersionCallback == null) {
                loge("onResult() mCheckVersionCallback is null");
                return;
            }

            switch (mStatus) {
                case STATUS_ERROR:
                    mCheckVersionCallback.onError(mErrorCode);
                    break;

                case STATUS_COMPLETE:
                    handleCompleteStatus();
                    break;

                default:
                    break;
            }
        } finally {
            mCheckNeedInfo = null;
            mCheckVersionCallback = null;
        }
    }

    @SuppressWarnings("unchecked")
    private void handleCompleteStatus() {
        if (mCheckNeedInfo.mIsOtaCheck) {
            if (mResultObject == null) {
                ((IOtaCheckVersionCallback) mCheckVersionCallback).onCheckResult(false, null);

            } else {
                ((IOtaCheckVersionCallback) mCheckVersionCallback).onCheckResult(true,
                        (OtaUpgradeInfo) mResultObject);
            }
        } else {
            if (mResultObject == null) {
                ((IAppsCheckVersionCallback) mCheckVersionCallback).onCheckResult(false, null);

            } else {
                ((IAppsCheckVersionCallback) mCheckVersionCallback).onCheckResult(true,
                        (List<PackageUpgradeInfo>) mResultObject);
            }
        }
    }

    public void onEvent(JobEvent event) {

    }

}
