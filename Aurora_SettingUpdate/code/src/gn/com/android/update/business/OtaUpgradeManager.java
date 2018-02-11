package gn.com.android.update.business;

import gn.com.android.update.R;
import gn.com.android.update.business.NetworkConfig.ConnectionType;
import gn.com.android.update.business.cache.OtaUpgradeInfoPreferencOperator;
import gn.com.android.update.business.cache.OtaUpgradeStatePreferencOperator;
import gn.com.android.update.business.job.CheckVersionJob;
import gn.com.android.update.business.job.DownloadJob;
import gn.com.android.update.business.job.Job;
import gn.com.android.update.business.job.Job.JobCompleteListener;
import gn.com.android.update.business.job.Job.JobEvent;
import gn.com.android.update.business.job.Job.JobEventListener;
import gn.com.android.update.business.job.Job.JobEventType;
import gn.com.android.update.business.job.OtaCheckLocalUpdateFileJob;
import gn.com.android.update.business.job.OtaUpgaradeJob;
import gn.com.android.update.business.job.SendDownloadConfirmJob;
import gn.com.android.update.push.PushHelper;
import gn.com.android.update.settings.ApplicationDataManager;
import gn.com.android.update.settings.ApplicationDataManager.DataOwner;
import gn.com.android.update.settings.ApplicationDataManager.OtaSettingKey;
import gn.com.android.update.settings.OtaSettingSharedPreferenceOperator;
import gn.com.android.update.settings.OtaSettings;
import gn.com.android.update.ui.UpdateUiActivity;
import gn.com.android.update.utils.BatteryUtil;
import gn.com.android.update.utils.CursorUtil;
import gn.com.android.update.utils.Error;
import gn.com.android.update.utils.FileUtil;
import gn.com.android.update.utils.HttpUtils;
import gn.com.android.update.utils.LogUtils;
import gn.com.android.update.utils.MSG;
import gn.com.android.update.utils.NotificationUtil;
import gn.com.android.update.utils.OtaInent;
import gn.com.android.update.utils.StorageUtil;
import gn.com.android.update.utils.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.android.providers.downloads.GnDownloadManagerTools;

import android.R.integer;
import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.util.Log;
import aurora.app.AuroraAlertDialog;

public class OtaUpgradeManager {
    private static final String TAG = "OtaUpgradeManager";
    private static OtaUpgradeManager sOtaUpgradeManager = null;
    private Context mContext = null;
    private Job mOtaRunningJob;
    private OtaUpgradeState mOtaUpgradeState;
    private int mHasDownloadSize = 0;
    private boolean mIsAutoCheck = false;
    private boolean mMountReceiverRegistered = false;
    private boolean mConnectivityReceiverRegistered = false;
    private OtaUpgradeInfo mOtaUpgradeInfo = null;
    private ThreadPoolManager mThreadPoolManager = ThreadPoolManager.getInstance();
    private OtaUpgradeStatePreferencOperator mOtaUpgradeStatePreferencOperator = null;
    private Builder mDownloadNotificationBuilder;
    private int mLastErrorCode = 0;
    private int mInterruptReason = 0;
    private PendingIntent mPendingIntent;
    private ContinueDownloadNeedInfo mContinueDownloadNeedInfo = null;
    private ApplicationDataManager mDataManager = null;
    private boolean mActivityIsShowing = false;
    private boolean mIsAutoDownload = false;
    private JobCompleteListener mJobCompleteListener = new JobCompleteListener() {

        @Override
        public void onComplete(Job job, int state, int errorCode, Object resultObject) {
            job.unregisterJobCompleteListener();
            job.unregisterJobEventListener();

            if (job == mOtaRunningJob) {
                synchronized (mOtaUpgradeState) {
                    mOtaRunningJob = null;

                    switch (state) {
                        case Job.JOB_COMPLETE_STATE_SUCCESSFULE:
                            handleJobComplete(resultObject);
                            break;
                        case Job.JOB_COMPLETE_STATE_ERROE:
                            handleJobError(errorCode);
                            break;

                        case Job.JOB_COMPLETE_STATE_CANCELED:
                            handleJobCanceled();
                            break;

                        default:
                            break;
                    }
                }

            } else {
                LogUtils.loge(TAG, "onComplete() job != mOtaRunningJob  mOtaRunningJob is null ?"
                        + (mOtaRunningJob == null));
            }

        }

    };

    private JobEventListener mJobEventListener = new JobEventListener() {

        @Override
        public void onJobEvent(JobEvent jobEvent) {
            JobEventType type = jobEvent.mEventType;
            LogUtils.log(TAG, "onJobEvent    JobEventType   = "  + type);
            boolean downloadBegin = false;
            switch (type) {

                case EVENT_UPDATE_PROGRESS:
                    mHasDownloadSize = jobEvent.mEventContent;
                    updateDownloadNotification();
                    break;

                case EVENT_DOWNLOAD_BEGIN:
                    downloadBegin = true;
                case EVENT_DOWNLOAD_END:
                    SendDownloadConfirmJob job = new SendDownloadConfirmJob(mContext, downloadBegin,
                            mOtaUpgradeInfo.getDownloadurl());
                    submitjob(job);
                    break;

                default:
                    break;
            }

        }
    };

    private BroadcastReceiver mMountReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                LogUtils.loge(TAG, "mMountReceiver intent is null");
                return;
            }
            LogUtils.logd(TAG, " receive the mount change broadcast" );
            String action = intent.getAction();
            LogUtils.logd(TAG, "mMountReceiver action is " + action);
            if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {

                if (!canContinueWhenReceiveMountedBroadcast(context)) {
                    return;
                }

                if (mOtaUpgradeState == OtaUpgradeState.DOWNLOAD_INTERRUPT
                        && mInterruptReason == Error.ERROR_CODE_STORAGE_NOT_MOUNTED) {
                    continueDownload();
                }

            } else if (Intent.ACTION_MEDIA_EJECT.equals(action)
                    || Intent.ACTION_MEDIA_BAD_REMOVAL.equals(action)) {

                if (mOtaUpgradeState == OtaUpgradeState.DOWNLOADING) {
                    if (mOtaRunningJob != null) {
                        ((DownloadJob) mOtaRunningJob).onErrorHappend(Error.ERROR_CODE_STORAGE_NOT_MOUNTED);
                    }

                }
            }

        }

        private synchronized boolean canContinueWhenReceiveMountedBroadcast(Context context) {
        	LogUtils.log(TAG, "Run  canContinueWhenReceiveMountedBroadcast");
            List<String> storageList = StorageUtil.getStorageVolumesPath(context);
            int size = storageList.size();
            if (size < 2) {
                return true;
            }

            for (String storage : storageList) {
                if (!StorageUtil.isRightStorageName(storage)) {
                    continue;
                }

                String state = StorageUtil.getStorageVolumeState(context, storage);
                if (Environment.MEDIA_CHECKING.equals(state) || Environment.MEDIA_UNMOUNTED.equals(state)
                        || Environment.MEDIA_SHARED.equals(state)) {
                    return false;
                }
            }

            return true;
        }
    };

    private BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (networkInfo == null) {
                return;
            }
            LogUtils.log(TAG, "receive the connect change broadcast  action is  android.net.conn.CONNECTIVITY_CHANGE");
            LogUtils.log(TAG,
                    "mConnectivityReceiver ()networkInfo.getTypeName() = " + networkInfo.getTypeName()
                            + " , state = " + networkInfo.getState());
           if("mobile_hipri".equals( networkInfo.getTypeName())){ //wifi网络切换到移动网络的时候，系统会发回来一个错误的网络状态，在这里忽略这个状态
            	if(HttpUtils.isNetworkAvailable(mContext)){
            		LogUtils.log(TAG, "network TypeName is mobile_hipri ,ignore this state");
            		return;
            	}
            }
//            if("mobile".equals( networkInfo.getTypeName())){
//            	
//            }
            LogUtils.log(TAG, "NetworkAvailable  =  " + HttpUtils.isNetworkAvailable(mContext));
            LogUtils.log(TAG, "networkInfo available  =  " + networkInfo.isAvailable());
            LogUtils.log(TAG, "NetworkMobileConnect  =  " + HttpUtils.isMobileNetwork(mContext));
          //增加判断实际的网络连通状况，在wifi与移动网络切换的时候，会传上来错误的网络状态，在这里做一下规避 modify by jiyouguang
            if (!networkInfo.isConnectedOrConnecting()) { 
                handleNetworkDisconnectedBoradcast(networkInfo.getType(), context);
            } else if (networkInfo.isConnected()) {
                handleNetworkConnectedBoradcast();
            }
        }

        private void handleNetworkDisconnectedBoradcast(int networkType, Context context) {
           LogUtils.log(TAG, "handleNetworkDisconnectedBoradcast  The NetworkType =  "  + networkType);
            if (mOtaUpgradeState != OtaUpgradeState.DOWNLOADING) {
                return;
            }
           
//            if (!needStopDownload(networkType, context)) {
//                Log.e(TAG, "ERROR_CODE_NETWORK_DISCONNECT 33");
//                return;
//            }
            //移动网络断开，但是wifi还是连通的，此时不停止下载
            if(networkType == ConnectivityManager.TYPE_MOBILE  && HttpUtils.isWIFIConnection(mContext)){
            	return;
            }
            //断开wifi网络，但是移动网络还是联通的
            if((networkType == ConnectivityManager.TYPE_WIFI || networkType == ConnectivityManager.TYPE_WIMAX) && HttpUtils.isMobileNetwork(mContext)){
            	LogUtils.log(TAG, "wifi is disconnect,but mobileNet is connect,so pause the download " );
            	((DownloadJob) mOtaRunningJob).setPauseFlag(true);
            }
            
            if (mOtaRunningJob != null && mOtaRunningJob instanceof DownloadJob) {
            	LogUtils.log(TAG, "handleNetworkDisconnectedBoradcast   The DownloadJob is runnning " );
            	mInterruptReason = Error.ERROR_CODE_NETWORK_DISCONNECT;
                ((DownloadJob) mOtaRunningJob).onErrorHappend(Error.ERROR_CODE_NETWORK_DISCONNECT);
            }
        }

        private boolean needStopDownload(int networkType, Context context) {
            if (isWifiNetworkType(networkType)) {
                if (!mContinueDownloadNeedInfo.mCanUseMobileNetwork) {
                    return true;
                }

            } else if (!HttpUtils.isNetworkAvailable(context)) {
                return true;
            }else if(isMobileNetWork(networkType)){
                return true;
            }

            return false;
        }

        private boolean isWifiNetworkType(int networkType) {
            return networkType == ConnectivityManager.TYPE_WIFI
                    || networkType == ConnectivityManager.TYPE_WIMAX;
        }

        private boolean isMobileNetWork(int networkType){
            return networkType == ConnectivityManager.TYPE_MOBILE;
        }
        private void handleNetworkConnectedBoradcast() {
            LogUtils.log(TAG, "network connected, try resume download:        mOtaUpgradeState =   "+mOtaUpgradeState+" mInterruptReason:"+mInterruptReason);
            if (mOtaUpgradeState != OtaUpgradeState.DOWNLOAD_INTERRUPT) {
                return;
            }
            
            if (mInterruptReason == Error.ERROR_CODE_MOBILE_NETWORK
                    || mInterruptReason == Error.ERROR_CODE_NETWORK_DISCONNECT) {
            		LogUtils.log(TAG, "network connected, try resume download");
                continueDownload();
            }

        }

    };

    private OtaUpgradeManager() {

    }

    private OtaUpgradeManager(Context context) {
        LogUtils.logd(TAG, "OtaUpgradeManager");
        mContext = context.getApplicationContext();
        mOtaUpgradeStatePreferencOperator = new OtaUpgradeStatePreferencOperator(mContext);
        mDataManager = ApplicationDataManager.getInstance(mContext);
        mOtaUpgradeInfo = getOtaUpgradeInfo();
        mPendingIntent = PendingIntent.getActivity(mContext, 0, new Intent(mContext, UpdateUiActivity.class),
                0);
        initOtaUpgradeState();
        mIsAutoDownload = OtaSettings.IsAutoDownload(mContext, false);
    }

    public static synchronized OtaUpgradeManager getInstance(Context context) {
        if (sOtaUpgradeManager == null) {
            sOtaUpgradeManager = new OtaUpgradeManager(context);
        }
        return sOtaUpgradeManager;
    }

    private void initOtaUpgradeState() {
        mOtaUpgradeState = mOtaUpgradeStatePreferencOperator.getOtaUpgradeState();
        switch (mOtaUpgradeState) {
            case CHECKING:
                setOtaUpgradeState(OtaUpgradeState.INITIAL);
                break;

            case DOWNLOADING:
            case DOWNLOAD_INTERRUPT:
                pauseDownloadManagerWhenInit();
                setOtaUpgradeState(OtaUpgradeState.DOWNLOAD_PAUSE);
                break;

            case DOWNLOAD_COMPLETE:
                String md5 = mOtaUpgradeInfo.getMd5();
                int totalFileSize = mOtaUpgradeInfo.getFileSize();
                File file = FileUtil.getAlreadyDownloadedFile(mContext,
                        FileUtil.getOtaFileNameWithoutStoragePath(md5));
                if (file != null && FileUtil.getFileLengthByUrl(mContext, mOtaUpgradeInfo.getDownloadurl()) == totalFileSize) {
                    return;

                } else {
                    setOtaUpgradeState(OtaUpgradeState.READY_TO_DOWNLOAD);
                }
                break;

            case INSTALLING:
                setOtaUpgradeState(OtaUpgradeState.INITIAL);
                break;

            default:
                break;
        }
    }

    private void pauseDownloadManagerWhenInit() {
        long downloadId = CursorUtil.getDownloadIdByUrl(mContext, mOtaUpgradeInfo.getDownloadurl());
        if (downloadId == Config.ERROR_DOWNLOAD_ID) {
            return;
        }

        GnDownloadManagerTools.pause(mContext.getContentResolver(), downloadId);

    }

    private Handler mOtaHandler = new Handler() {
        public void handleMessage(Message msg) {
            LogUtils.logd(TAG, "handleMessage() msg = " + msg.what);

            switch (msg.what) {

                case MSG.NOTIFY_ACTIVITY_CREATE:
                    prepareWhenActivityCreate();
                    break;

                default:
                    break;
            }

        }

        private void prepareWhenActivityCreate() {
            setThisDownloadIsAutoDownload(false);

            resumeDownloadIfInterruptByLowBattery();

            NotificationUtil.clearNotification(mContext, Config.VERSION_NOTIFICATION_ID);

            int stateValue = mOtaUpgradeState.getValue();
            if (stateValue > OtaUpgradeState.READY_TO_DOWNLOAD.getValue()
                    && stateValue < OtaUpgradeState.INSTALLING.getValue()) {
                updateDownloadNotification();
            }

            if (PushHelper.needRegisteredToGpe(mDataManager, true)) {
                PushHelper.registerGpe(mContext);
                return;
            }

            String rid = PushHelper.getRid(mDataManager, null);
            if (rid == null) {
                return;
            }

            if (PushHelper.getLastRegisterOrUnregisterState(mDataManager, false) == false) {
                if (PushHelper.getLastApsRegisterAction(mDataManager, PushHelper.ACTION_REGISTER).equals(
                        PushHelper.ACTION_REGISTER)) {
                    PushHelper.registerRid(rid, mContext);
                } else {
                    PushHelper.unregisterRid(rid, mContext);
                }

            }

        }

    };

    private void handleJobError(int errorCode) {
        LogUtils.logd(TAG, "handleJobError mState = " + mOtaUpgradeState);
        pushErrorCode(errorCode);

        switch (mOtaUpgradeState) {
            case CHECKING:
                setOtaUpgradeState(OtaUpgradeState.INITIAL);
                break;
            case DOWNLOADING:
                handleDownloadJobError(errorCode);
                break;
            case INSTALLING:
                handleFileNotFoundWhenUpgrading();
                break;

            default:
                break;
        }

    }

    private void handleJobCanceled() {
        if (mOtaUpgradeState == OtaUpgradeState.DOWNLOADING
                || mOtaUpgradeState == OtaUpgradeState.DOWNLOAD_INTERRUPT) {
            handleDownloadStop();
        }

    }

    private void handleDownloadJobError(int errorCode) {
        LogUtils.logd(TAG, "handleDownloadJobError errorCode = " + errorCode);
        switch (errorCode) {

            case Error.ERROR_CODE_REMOTE_FILE_NOT_FOUND:
                handleRemoteFileNotFoundWhenDownload();
                break;

            case Error.ERROR_CODE_FILE_VERIFY_FAILED:
                handleFileVerifyFailedWhenDownload();
                break;

            case Error.ERROR_CODE_MOBILE_NETWORK:
            case Error.ERROR_CODE_NETWORK_DISCONNECT:
            case Error.ERROR_CODE_BATTREY_LEVEL_LOW:
            case Error.ERROR_CODE_STORAGE_NOT_MOUNTED:
                setOtaUpgradeState(OtaUpgradeState.DOWNLOAD_INTERRUPT);
                updateDownloadNotification();
                break;

            case Error.ERROR_CODE_STORAGE_NO_SPACE:
            case Error.ERROR_CODE_INTERNAL_STORAGE_NO_SPACE:
            case Error.ERROR_CODE_DOWNLOADFILE_DELETED:
                handleDownloadStop();

                break;

            default:
                break;
        }

    }

    private void handleStorageNotMountedWhenDownload() {
        setOtaUpgradeState(OtaUpgradeState.DOWNLOAD_INTERRUPT);

    }

    private void handleFileVerifyFailedWhenDownload() {
        unregisterDownloadJobNeedReceiver();
        setOtaUpgradeState(OtaUpgradeState.READY_TO_DOWNLOAD);
        NotificationUtil.clearNotification(mContext, Config.DOWNLOAD_NOTIFICATION_ID);
    }

    private void handleRemoteFileNotFoundWhenDownload() {
        unregisterDownloadJobNeedReceiver();
        setOtaUpgradeState(OtaUpgradeState.INITIAL);
        NotificationUtil.clearNotification(mContext, Config.DOWNLOAD_NOTIFICATION_ID);
    }

    private void handleJobComplete(Object resultObject) {
        switch (mOtaUpgradeState) {
            case CHECKING:
                handleCheckJobComplete(resultObject);
                break;
            case DOWNLOADING:
                handleDownloadJobComplete();
                break;
            default:
                break;
        }

    }

    private void handleDownloadJobComplete() {
        LogUtils.log(TAG, "handleDownloadJobComplete");
        setOtaUpgradeState(OtaUpgradeState.DOWNLOAD_COMPLETE);

        NotificationUtil.clearNotification(mContext, Config.VERSION_NOTIFICATION_ID);

        updateDownloadNotification();

        unregisterDownloadJobNeedReceiver();

        stopService();

        mContinueDownloadNeedInfo = null;
    }

    private void handleCheckJobComplete(Object resultObject) {
        boolean flag = true;

        if (resultObject == null) {
            setOtaUpgradeState(OtaUpgradeState.INITIAL);
            stopService();
            flag = false;

        } else {
            OtaUpgradeInfo newOtaUpgradeInfo = (OtaUpgradeInfo) resultObject;
            deleteLastVersionDownloadFile(newOtaUpgradeInfo);
            setOtaUpgradeInfo(newOtaUpgradeInfo);
            OtaSettings.setShowNotify(mContext, true);//有新版本标记
            handleCheckHaveNewVersion(newOtaUpgradeInfo);
        }

        sendVersionBroadcastToLauncher(flag);

    }

    private void deleteLastVersionDownloadFile(OtaUpgradeInfo newOtaUpgradeInfo) {
        String md5 = mOtaUpgradeInfo.getMd5();
        if (md5.equals("")) {
            return;
        }

        String newMd5 = newOtaUpgradeInfo.getMd5();
        if (md5.equals(newMd5)) {
            return;
        }

        File file = FileUtil.getAlreadyDownloadedFile(mContext,
                FileUtil.getOtaFileNameWithoutStoragePath(md5));
        FileUtil.deleteFileIfExists(file);
    }
/*
 * 在这里会判断是否可以自动下载更新
 */
    private void handleCheckHaveNewVersion(OtaUpgradeInfo newOtaUpgradeInfo) {
        setOtaUpgradeState(OtaUpgradeState.READY_TO_DOWNLOAD);
        LogUtils.logd(TAG, "handleCheckHaveNewVersion () mIsAutoCheck = " + mIsAutoCheck);
        if (!mIsAutoCheck) {
            return;
        }

        if (canStartAutoDownload()) {
            startAutoDownload();
            return;
        }

        sendNewVersionNotification(newOtaUpgradeInfo.getInternalVer(), false);
    }

    private Notification getVersionNotification(boolean isPushNotification) {
        Intent activityIntent = new Intent(OtaInent.START_MAIN_ACTIVITY_ACTION);

        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Builder builder = new Builder(mContext);
        builder.setSmallIcon(R.drawable.stat_sys_download_anim0);
        builder.setAutoCancel(true);

        if (isPushNotification) {
            setupPushNotification(activityIntent, builder);

        } else {
            setupAutoCheckNotification(builder);
        }

        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, activityIntent, 0);
        builder.setContentIntent(contentIntent);
        return builder.getNotification();
    }

    private void setupPushNotification(Intent activityIntent, Builder builder) {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.getVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION) == AudioManager.VIBRATE_SETTING_ON) {

            builder.setDefaults(Notification.DEFAULT_ALL);
        } else {
            builder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND);
        }

        builder.setTicker(getString(R.string.push_content));
        activityIntent.putExtra("who", "push");
        SimpleDateFormat formatter = new SimpleDateFormat(Config.DATE_FORMAT_BY_DAY);
        Date curDate = new Date(System.currentTimeMillis());
        builder.setContentTitle(mContext.getString(R.string.push_title, formatter.format(curDate)));
        builder.setContentText(getString(R.string.push_content));
    }

    private void setupAutoCheckNotification(Builder builder) {
        builder.setTicker(getString(R.string.notify_auto_update_title));
        builder.setVibrate(new long[] {100, 250, 100, 400});
        builder.setContentTitle(getString(R.string.notify_auto_update_title));
        builder.setContentText(getString(R.string.notify_auto_check_notify));
    }

    protected void startAutoDownload() {
        LogUtils.logd(TAG, "startAutoDownload");
        synchronized (mOtaUpgradeState) {
            if (mActivityIsShowing) {
                LogUtils.loge(TAG, "startAutoDownload() mActivityIsShowing");
                return;
            }

            setThisDownloadIsAutoDownload(true);
            mContinueDownloadNeedInfo = new ContinueDownloadNeedInfo(null, false);
            try {
                startDownloadOtaFile(null, false);
            } catch (NoSpaceException e) {
                e.printStackTrace();
            } catch (ErrorStateException e) {
                e.printStackTrace();
            }
        }

    }

    protected boolean canStartAutoDownload() {
        if (!OtaSettings.getAutoDownloadEnabled(mContext, true)) {
            return false;
        }

        if (HttpUtils.isMobileNetwork(mContext)) {
            return false;
        }

        if (BatteryUtil.getBatteryLevel() < Config.AUTO_DOWNLOAD_BEGIN_MIN_BATTERY_LEVEl
                && !BatteryUtil.isCharging()) {
            LogUtils.logd(TAG, "canStartAutoDownload() BatteryLevel = " + BatteryUtil.getBatteryLevel()
                    + " isCharging() = " + BatteryUtil.isCharging());
            return false;
        }

        if (StorageUtil.isNoStorageMounted(mContext)) {
            return false;
        }

        String fileNameWithoutStoragePath = FileUtil.getOtaFileNameWithoutStoragePath(mOtaUpgradeInfo
                .getMd5());
        if (!StorageUtil.hasEnoughSpaceForDownload(mContext, fileNameWithoutStoragePath,
                mOtaUpgradeInfo.getFileSize())) {
            LogUtils.logd(TAG, "canStartAutoDownload() no space");
            return false;
        }

        return true;
    }

    private void setOtaUpgradeInfo(OtaUpgradeInfo otaUpgradeInfo) {
        mOtaUpgradeInfo = otaUpgradeInfo;
        OtaUpgradeInfoPreferencOperator otaUpgradeInfoPreferencOperator = new OtaUpgradeInfoPreferencOperator(
                mContext);
        otaUpgradeInfoPreferencOperator.storeOtaUpgradeInfo(otaUpgradeInfo);
    }

    public void checkOtaVersion(IOtaCheckVersionCallback otaCheckVersionCallback) throws ErrorStateException {
        mIsAutoCheck = false;
        int pushID = PushHelper.getPushID(mDataManager, Config.ERROR_PUSH_ID);

        CheckVersionNeedInfo checkVersionNeedInfo = getCheckVersionNeedInfo(NetworkConfig.CHECK_TYPE_DEFAULT,
                pushID);

        startCheck(checkVersionNeedInfo, otaCheckVersionCallback);
    }

    public void autoCheckOtaVersion() throws ErrorStateException {

        if (mActivityIsShowing) {
            LogUtils.loge(TAG, "autoCheckOtaVersion() begin mActivityIsShowing");
            return;
        }

        startService();

        doBeforeStartAutoCheck();

        synchronized (mOtaUpgradeState) {
            if (mActivityIsShowing) {
                LogUtils.loge(TAG, "autoCheckOtaVersion() mActivityIsShowing");
                stopService();
                return;
            }

            mIsAutoCheck = true;

            CheckVersionNeedInfo checkVersionNeedInfo = getCheckVersionNeedInfo(
                    NetworkConfig.CHECK_TYPE_AUTO, Config.ERROR_PUSH_ID);
            startCheck(checkVersionNeedInfo, null);
        }

        OtaSettings.setLastAutoCheckTime(mContext, System.currentTimeMillis());

    }

    private synchronized void startCheck(CheckVersionNeedInfo checkVersionNeedInfo,
            IOtaCheckVersionCallback otaCheckVersionCallback) throws ErrorStateException {
        if (mOtaUpgradeState != OtaUpgradeState.INITIAL) {
            throw new ErrorStateException("current state is " + mOtaUpgradeState);
        }
        OtaSettings.setLastCheckTime(mContext, System.currentTimeMillis());

        mOtaRunningJob = new CheckVersionJob(checkVersionNeedInfo);
        mOtaRunningJob.registerCallback(otaCheckVersionCallback);

        setupJob();

        submitjob(mOtaRunningJob);

        setOtaUpgradeState(OtaUpgradeState.CHECKING);
    }

    private CheckVersionNeedInfo getCheckVersionNeedInfo(int checkType, int pushId) {
        CheckVersionNeedInfo checkVersionNeedInfo = new CheckVersionNeedInfo();
        checkVersionNeedInfo.mImei = Util.getImei(mContext);
        checkVersionNeedInfo.mIsWapNetwork = HttpUtils.isWapConnection(mContext);
        checkVersionNeedInfo.mConnectionType = HttpUtils.getConnectionType(mContext);
        checkVersionNeedInfo.mCheckType = checkType;
        checkVersionNeedInfo.mPushId = pushId;
        return checkVersionNeedInfo;
    }

    private void setupJob() {
        popLastErrorCode();
        mOtaRunningJob.registerJobCompleteListener(mJobCompleteListener);
        mOtaRunningJob.registerJobEventListener(mJobEventListener);
    }

    public OtaUpgradeState getOtaUpgradeState() {
        synchronized (mOtaUpgradeState) {
            return mOtaUpgradeState;
        }
    }

    private void setOtaUpgradeState(OtaUpgradeState otaUpgradeState) {
        synchronized (mOtaUpgradeState) {
            LogUtils.log(TAG, "setOtaUpgradeState " + otaUpgradeState);

            mOtaUpgradeState = otaUpgradeState;

            mOtaUpgradeStatePreferencOperator.setOtaUpgradeState(mOtaUpgradeState);
        }
    }

    public OtaUpgradeInfo getOtaUpgradeInfo() {
        if (mOtaUpgradeInfo == null) {
            OtaUpgradeInfoPreferencOperator otaUpgradeInfoPreferencOperator = new OtaUpgradeInfoPreferencOperator(
                    mContext);

            mOtaUpgradeInfo = otaUpgradeInfoPreferencOperator.getOtaUpgradeInfo();
        }
        return mOtaUpgradeInfo;

    }

    public void downloadOtaFile(IOtaDownloadCallback otaDownloadCallback) throws ErrorStateException,
            NoSpaceException {
        LogUtils.log(TAG, "start  downloadOtaFile  mOtaUpgradeState ==  "  + mOtaUpgradeState);
        if (mOtaUpgradeState != OtaUpgradeState.READY_TO_DOWNLOAD
                && mOtaUpgradeState != OtaUpgradeState.DOWNLOAD_PAUSE) {
            throw new ErrorStateException("downloadOtaFile current state is " + mOtaUpgradeState);
        }

        boolean isMobileNetwork = HttpUtils.isMobileNetwork(mContext);
        mContinueDownloadNeedInfo = new ContinueDownloadNeedInfo(otaDownloadCallback, isMobileNetwork);

        startDownloadOtaFile(otaDownloadCallback, isMobileNetwork);

    }

    private void startDownloadOtaFile(IOtaDownloadCallback otaDownloadCallback, boolean canUseMobileNetwork)
            throws NoSpaceException, ErrorStateException {
        synchronized (mOtaUpgradeState) {
            if (mOtaUpgradeState != OtaUpgradeState.READY_TO_DOWNLOAD
                    && mOtaUpgradeState != OtaUpgradeState.DOWNLOAD_PAUSE
                    && mOtaUpgradeState != OtaUpgradeState.DOWNLOAD_INTERRUPT) {
                throw new ErrorStateException("current state is " + mOtaUpgradeState);
            }

            DownloadNeedInfo downloadNeedInfo = getDownloadNeedInfo();

            mOtaRunningJob = new DownloadJob(mContext, downloadNeedInfo, canUseMobileNetwork);

            mOtaRunningJob.registerCallback(otaDownloadCallback);

            setupJob(); //注册事件监听器

            registerDownloadJobNeedReceiver(); //注册网络变化和磁盘挂载变化的广播接收器

            submitjob(mOtaRunningJob);

            setOtaUpgradeState(OtaUpgradeState.DOWNLOADING);

            showDownloadNotification();
        }

        startService();

    }

    private void showDownloadNotification() {
        if (mIsAutoDownload) {
            return;
        }

        String contentText = mContext.getString(R.string.down_upgrade_file);
        int progress = Util.getDownloadProgress(mHasDownloadSize, mOtaUpgradeInfo.getFileSize());
        Notification notification = getDownloadNotification(mHasDownloadSize, contentText);
        NotificationUtil.showNotication(mContext, notification, Config.DOWNLOAD_NOTIFICATION_ID);
    }

    private void updateDownloadNotification() {
        if (mIsAutoDownload && mOtaUpgradeState != OtaUpgradeState.DOWNLOAD_COMPLETE) {
            return;
        }

        String contentText = null;
        int progress = Util.getDownloadProgress(mHasDownloadSize, mOtaUpgradeInfo.getFileSize());

        switch (mOtaUpgradeState) {
            case DOWNLOADING:
                contentText = mContext.getString(R.string.down_upgrade_file) + progress + "%";
                break;

            case DOWNLOAD_INTERRUPT:
                contentText = mContext.getString(R.string.down_suspend);
                break;

            case DOWNLOAD_PAUSE:
                contentText = mContext.getString(R.string.download_pause);
                break;

            case DOWNLOAD_COMPLETE:
                contentText = mContext.getString(R.string.down_ok_continue);
                break;
            default:
                break;
        }
        Notification notification = getDownloadNotification(mHasDownloadSize, contentText);
        NotificationUtil.showNotication(mContext, notification, Config.DOWNLOAD_NOTIFICATION_ID);
    }

    private DownloadNeedInfo getDownloadNeedInfo() throws NoSpaceException {
        int totalLength = mOtaUpgradeInfo.getFileSize();
        String md5 = mOtaUpgradeInfo.getMd5();
        String url = mOtaUpgradeInfo.getDownloadurl();

        DownloadNeedInfo downloadNeedInfo = new DownloadNeedInfo();
        String otaFilePathWithoutStoragePath = FileUtil.getOtaFileNameWithoutStoragePath(md5);
        String filePath = FileUtil.getDownloadFilePath(mContext, otaFilePathWithoutStoragePath, totalLength);
        LogUtils.log(TAG, "The DownLoad FilePath is     " + filePath);
        LogUtils.log(TAG, "The DownLoad Url  is     " + url);
        downloadNeedInfo.mFilePath = filePath;
        downloadNeedInfo.mMd5 = md5;
        downloadNeedInfo.mSize = totalLength;
        downloadNeedInfo.mUrl = url;

        return downloadNeedInfo;
    }

    private void handleFileNotFoundWhenUpgrading() {
        setOtaUpgradeState(OtaUpgradeState.READY_TO_DOWNLOAD);
        NotificationUtil.clearNotification(mContext, Config.DOWNLOAD_NOTIFICATION_ID);
    }

    public void upgradeOta(IOtaUpgradeCallback otaUpgradeCallback) throws FileNotFoundException,
            ErrorStateException {
        if (mOtaUpgradeState != OtaUpgradeState.DOWNLOAD_COMPLETE) {
            throw new ErrorStateException("wrong state");
        }

        String md5 = mOtaUpgradeInfo.getMd5();
        File file = FileUtil.getAlreadyDownloadedFile(mContext,
                FileUtil.getOtaFileNameWithoutStoragePath(md5));

        if (file == null) {
            handleFileNotFoundWhenUpgrading();
            throw new FileNotFoundException("update file not found");
        }

        mOtaRunningJob = new OtaUpgaradeJob(mContext, file, md5);

        mOtaRunningJob.registerCallback(otaUpgradeCallback);

        setupJob();

        submitjob(mOtaRunningJob);

        setOtaUpgradeState(OtaUpgradeState.INSTALLING);

    }

    private void submitjob(Job job) {
        mThreadPoolManager.submitTask(job);
    }

    private void handleDownloadStop() {
        unregisterDownloadJobNeedReceiver();
        mContinueDownloadNeedInfo = null;
        setOtaUpgradeState(OtaUpgradeState.DOWNLOAD_PAUSE);
        updateDownloadNotification();
        stopService();
    }

    public void pauseOtaDownload(IOtaPauseDownloadCallback otaPauseDownloadCallback)
            throws ErrorStateException {
        synchronized (mOtaUpgradeState) {
        	LogUtils.log(TAG, "pauseOtaDownload  mOtaUpgradeState  =   "  + mOtaUpgradeState);
            if (mOtaUpgradeState == OtaUpgradeState.DOWNLOAD_INTERRUPT) {
                handleDownloadStop();
                
                if (otaPauseDownloadCallback != null) {
                    otaPauseDownloadCallback.onPauseComplete();
                }
                return;
            }

            if (mOtaUpgradeState != OtaUpgradeState.DOWNLOADING) {
                throw new ErrorStateException("error state , now state is " + mOtaUpgradeState);
            }

            LogUtils.log(TAG, "pauseOtaDownload");

            mOtaRunningJob.registerCallback(otaPauseDownloadCallback);

            mThreadPoolManager.stopTask(mOtaRunningJob);

            unregisterDownloadJobNeedReceiver();
        }

    }

    // Aurora <likai> add begin
    public void setOtaInitial() {
        setOtaUpgradeState(OtaUpgradeState.INITIAL);
    }

    public void setOtaDownloadPause() {
        setOtaUpgradeState(OtaUpgradeState.DOWNLOAD_PAUSE);
    }

    public void cancelOtaDownload() {
        if (mOtaUpgradeState == OtaUpgradeState.INSTALLING) {
            LogUtils.loge(TAG, "cancelOtaDownload() state is INSTALLING");
            return;
        }

        cancelRunningJob();
        NotificationUtil.clearNotification(mContext, Config.DOWNLOAD_NOTIFICATION_ID);
        mThreadPoolManager.removeAllTask();
        setOtaUpgradeState(OtaUpgradeState.READY_TO_DOWNLOAD);
        stopService();
        releaseResource();
    }
    // Aurora <likai> add begin

    public void cancelOtaUpgrade() {
        if (mOtaUpgradeState == OtaUpgradeState.INSTALLING) {
            LogUtils.loge(TAG, "cancelOtaUpgrade() state is INSTALLING");
            return;
        }

        cancelRunningJob();
        NotificationUtil.clearNotification(mContext, Config.DOWNLOAD_NOTIFICATION_ID);
        mThreadPoolManager.removeAllTask();
        setOtaUpgradeState(OtaUpgradeState.INITIAL);
        stopService();
        releaseResource();
    }

    private void releaseResource() {

//        mOtaUpgradeState = null;
//        mThreadPoolManager.stop();
//        mThreadPoolManager = null;
//        sOtaUpgradeManager = null;
//        mOtaUpgradeInfo = null;
//        mOtaUpgradeStatePreferencOperator = null;
        unregisterDownloadJobNeedReceiver();
        mContinueDownloadNeedInfo = null;
        mOtaHandler.removeCallbacksAndMessages(null);
        mHasDownloadSize = 0;
        mLastErrorCode = 0;
        mInterruptReason = 0;
//        mOtaHandler = null;
//        mPendingIntent = null;
//        mDataManager.destroy();
//        mDownloadNotificationBuilder = null;
//        mContext = null;

    }

    private void startService() {
        LogUtils.logd(TAG, "startService");
        Intent intent = new Intent(mContext, OtaUpgradeService.class);
        mContext.startService(intent);
    }

    private void stopService() {
        LogUtils.logd(TAG, "stopService");
        Intent intent = new Intent(mContext, OtaUpgradeService.class);
        mContext.stopService(intent);

    }

    public int getDownloadedSize() {
        return mHasDownloadSize;
    }

    private void cancelRunningJob() {
        if (mOtaRunningJob != null) {
            mOtaRunningJob.unRegisterCallback();
            mOtaRunningJob.unregisterJobCompleteListener();
            mOtaRunningJob.unregisterJobEventListener();
            mThreadPoolManager.stopTask(mOtaRunningJob);
            mOtaRunningJob = null;
            mContinueDownloadNeedInfo = null;
        }
    }

    public void resumeCallback(IBaseCallback baseCallback) {
        if (baseCallback == null) {
            throw new NullPointerException("callback is null");
        }
        if (mOtaRunningJob != null) {
            mOtaRunningJob.registerCallback(baseCallback);
        }

        if (mOtaUpgradeState != OtaUpgradeState.DOWNLOADING
                && mOtaUpgradeState != OtaUpgradeState.DOWNLOAD_INTERRUPT) {
            return;
        }

        if (mContinueDownloadNeedInfo != null && baseCallback instanceof IOtaDownloadCallback) {
            mContinueDownloadNeedInfo.mDownloadCallback = (IOtaDownloadCallback) baseCallback;
        }
    }

    public void pauseCallback() {
        if (mOtaRunningJob != null) {
            mOtaRunningJob.unRegisterCallback();
        }

        if (mContinueDownloadNeedInfo != null) {
            mContinueDownloadNeedInfo.mDownloadCallback = null;
        }
    }

    public void onActivityCreate() {
        mActivityIsShowing = true;
        mOtaHandler.sendEmptyMessage(MSG.NOTIFY_ACTIVITY_CREATE);
    }

    public void onActivityDestroy() {
        mActivityIsShowing = false;
    }

    private void registerMountReceiver() {
        synchronized (mMountReceiver) {
            LogUtils.logd(TAG, "registerMountReceiver() mMountReceiverRegistered ="
                    + mMountReceiverRegistered);

            if (!mMountReceiverRegistered) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
                intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
                intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
                intentFilter.addDataScheme("file");
                mContext.registerReceiver(mMountReceiver, intentFilter);
                mMountReceiverRegistered = true;
            }
        }

    }

    private void unregisterMountReceiver() {
        synchronized (mMountReceiver) {
            LogUtils.logd(TAG, "unregisterMountReceiver() mMountReceiverRegistered ="
                    + mMountReceiverRegistered);

            if (mMountReceiverRegistered) {
                mContext.unregisterReceiver(mMountReceiver);
                mMountReceiverRegistered = false;
            }
        }

    }

    private void registerConnectivityReceiver() {
        synchronized (mConnectivityReceiver) {
            LogUtils.logd(TAG, "registerConnectivityReceiver() mConnectivityReceiverRegistered ="
                    + mConnectivityReceiverRegistered);

            if (!mConnectivityReceiverRegistered) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                mContext.registerReceiver(mConnectivityReceiver, intentFilter);
                mConnectivityReceiverRegistered = true;
            }
        }

    }

    private void unregisterConnectivityReceiver() {
        synchronized (mConnectivityReceiver) {
            LogUtils.logd(TAG, "unregisterConnectivityReceiver() mConnectivityReceiverRegistered ="
                    + mConnectivityReceiverRegistered);

            if (mConnectivityReceiverRegistered) {
                mContext.unregisterReceiver(mConnectivityReceiver);
                mConnectivityReceiverRegistered = false;
            }
        }

    }

    public void checkLocalUpgradeFile(File file, IOtaCheckLocalUpgradeFileCallback otaLocalUpgradeCallback) {
        LogUtils.logd(TAG, "checkLocalUpgradeFile()");

        mOtaRunningJob = new OtaCheckLocalUpdateFileJob(mContext, file);

        mOtaRunningJob.registerCallback(otaLocalUpgradeCallback);

        setupJob();

        submitjob(mOtaRunningJob);
    }

    private Notification getDownloadNotification(int currentBytes, String contentText) {
        if (mDownloadNotificationBuilder == null) {
            mDownloadNotificationBuilder = new Notification.Builder(mContext);
            mDownloadNotificationBuilder.setOngoing(true);
            mDownloadNotificationBuilder.setSmallIcon(R.drawable.stat_sys_download_anim0);
        }
        mDownloadNotificationBuilder.setContentIntent(mPendingIntent);
        String contentTitle = mContext.getString(R.string.notify_auto_update_title);
        mDownloadNotificationBuilder.setContentTitle(contentTitle);
        mDownloadNotificationBuilder.setContentText(contentText);
        int progress = currentBytes / (mOtaUpgradeInfo.getFileSize() / 100);
        mDownloadNotificationBuilder.setProgress(Config.DEFAULT_MAX_PROGRESS, progress, false);
        return mDownloadNotificationBuilder.getNotification();
    }

    public int popLastErrorCode() {
        try {
            LogUtils.logd(TAG, "popLastErrorCode mLastErrorCode = " + mLastErrorCode);
            return mLastErrorCode;
        } finally {
            mLastErrorCode = 0;
        }

    }

    private void pushErrorCode(int errorCode) {
        mLastErrorCode = errorCode;
        mInterruptReason = errorCode;
        LogUtils.logd(TAG, "pushErrorCode errorCode = " + errorCode);
    }

    public void setAutoCheckEnable(boolean enabled) {
        Intent intent = new Intent(mContext, OtaReceiver.class);
        intent.setAction(OtaInent.AUTO_CHECK_ACTION);
        PendingIntent alarmAutoCheck = PendingIntent.getBroadcast(mContext, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        if (enabled) {

        } else {
            alarmManager.cancel(alarmAutoCheck);
        }
    }

    private synchronized void continueDownload() {
        if (mOtaUpgradeState != OtaUpgradeState.DOWNLOAD_INTERRUPT) {
            LogUtils.loge(TAG, "continueDownload() wrong state mOtaUpgradeState =" + mOtaUpgradeState);
            return;
        }

        if (mContinueDownloadNeedInfo == null) {
            LogUtils.loge(TAG, "continueDownload() mContinueDownloadNeedInfo is null =");
            return;
        }

        IOtaDownloadCallback otaDownloadCallback = mContinueDownloadNeedInfo.mDownloadCallback;
        try {
            mInterruptReason = 0;
            if(HttpUtils.isMobileNetwork(mContext)){
                LogUtils.log(TAG, "download resume   isMobileNetwork");
                mContext.sendBroadcast(new Intent(UpdateUiActivity.ACTION_CHANGE_TO_MOBILE));
            }
            else{
                startDownloadOtaFile(otaDownloadCallback, HttpUtils.isMobileNetwork(mContext));
            }

        } catch (NoSpaceException e) {
            LogUtils.loge(TAG, "continueDownload no space");

            handleDownloadStop();

            if (otaDownloadCallback != null) {
                otaDownloadCallback.onError(Error.ERROR_CODE_STORAGE_NO_SPACE);
            }

            pushErrorCode(Error.ERROR_CODE_STORAGE_NO_SPACE);
        } catch (ErrorStateException e) {
            e.printStackTrace();
        }
    }

    private void registerDownloadJobNeedReceiver() {
        registerConnectivityReceiver();
        registerMountReceiver();
    }

    private void unregisterDownloadJobNeedReceiver() {
        unregisterConnectivityReceiver();
        unregisterMountReceiver();
    }

    public void sendNewVersionNotification(String newVersion, boolean isPush) {
        LogUtils.logd(TAG, "sendNewVersionNotification() newVersion = " + newVersion + " isPush = " + isPush);
        if (OtaSettings.getLastNotifyVersion(mContext, "").equals(newVersion)) {
            if (OtaSettings.getNotifySameVersion(mContext, false)) {
                return;
            }
            OtaSettings.setNotifySameVersion(mContext, true);
        } else {
            OtaSettings.setNotifySameVersion(mContext, false);
        }

        OtaSettings.setNotifyVersion(mContext, newVersion);

        Notification notification = getVersionNotification(isPush);
        NotificationUtil.showNotication(mContext, notification, Config.VERSION_NOTIFICATION_ID);
    }

    public void sendVersionBroadcastToLauncher(boolean haveNewVersion) {
        Intent intent1 = new Intent(OtaInent.NOTIFY_LAUNCHER_ACTION);
        intent1.putExtra("className", mContext.getPackageName() + ".ui.UpdateUiActivity");
        intent1.putExtra("packageName", mContext.getPackageName());

        int haveNewVersionFlag = 0;

        if (haveNewVersion) {
            haveNewVersionFlag = 1;
        } else {
            haveNewVersionFlag = 0;
        }

        LogUtils.logd(TAG, "sendVersionBroadcastToLauncher() have new version = " + haveNewVersionFlag);
        intent1.putExtra("newVersion", haveNewVersionFlag);
        mContext.sendBroadcast(intent1);
    }

    private class ContinueDownloadNeedInfo {
        public IOtaDownloadCallback mDownloadCallback = null;
        public boolean mCanUseMobileNetwork = false;

        public ContinueDownloadNeedInfo(IOtaDownloadCallback downloadCallback, boolean canUseMobileNetwork) {
            mDownloadCallback = downloadCallback;
            mCanUseMobileNetwork = canUseMobileNetwork;
        }
    }

    private String getString(int resId) {
        return mContext.getString(resId);
    }

    public void resumeInitialStateAfterOneDay() {
        if (mOtaUpgradeState == OtaUpgradeState.READY_TO_DOWNLOAD) {
            setOtaUpgradeState(OtaUpgradeState.INITIAL);
        }

    }

    private void doBeforeStartAutoCheck() {
        if (mOtaUpgradeState != OtaUpgradeState.INITIAL) {
            setOtaUpgradeState(OtaUpgradeState.INITIAL);
        }
    }

    public synchronized void onBatteryChangedToLow() {
        if (mOtaUpgradeState != OtaUpgradeState.DOWNLOADING) {
            return;
        }

        if (!mIsAutoDownload) {
            LogUtils.logd(TAG, "onBatteryChangedToLow() not auto download");
            return;
        }

        if (mOtaRunningJob != null) {
            ((DownloadJob) mOtaRunningJob).onErrorHappend(Error.ERROR_CODE_BATTREY_LEVEL_LOW);
        }
    }

    public synchronized void onBatteryChangedToHigh() {
        resumeDownloadIfInterruptByLowBattery();
    }

    private void resumeDownloadIfInterruptByLowBattery() {
        if (mOtaUpgradeState == OtaUpgradeState.DOWNLOAD_INTERRUPT
                && mInterruptReason == Error.ERROR_CODE_BATTREY_LEVEL_LOW) {
            popLastErrorCode();
            continueDownload();
        }
    }

    private void setThisDownloadIsAutoDownload(boolean isAuto) {
        mIsAutoDownload = isAuto;
        OtaSettings.setThisDownloadIsAutoDownload(mContext, isAuto);
    }
}
