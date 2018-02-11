package gn.com.android.update.business.job;

import gn.com.android.update.business.Config;
import gn.com.android.update.business.DownloadNeedInfo;
import gn.com.android.update.business.IBaseCallback;
import gn.com.android.update.business.IOtaDownloadCallback;
import gn.com.android.update.business.IOtaPauseDownloadCallback;
import gn.com.android.update.business.NetworkConfig.ConnectionType;
import gn.com.android.update.business.job.Job.JobEvent;
import gn.com.android.update.utils.CursorUtil;
import gn.com.android.update.utils.Error;
import gn.com.android.update.utils.FileUtil;
import gn.com.android.update.utils.HttpUtils;
import gn.com.android.update.utils.LogUtils;
import gn.com.android.update.utils.StorageUtil;
import gn.com.android.update.utils.Util;

import java.io.File;

import com.android.providers.downloads.GnDownloadManagerTools;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.Downloads.Impl;

public class DownloadJob extends Job {
    private static final String TAG = "DownloadJob";
    private DownloadChangeObserver mDownloadChangeObserver = new DownloadChangeObserver();
    private DownloadManager.Query mQuery;
    private Cursor mCursor = null;
    private long mDownLoadId = Config.ERROR_DOWNLOAD_ID;
    private String mUrl = null;
    private String mFilePath = null;
    private int mFileTotalSize = 0;
    private String mMd5 = null;
    public static final Uri CONTENT_URI = Uri.parse("content://downloads/my_downloads");
    public static final String COLUMN_NOTIFICATION_PACKAGE = "notificationpackage";
    public static final String _ID = "_id";
    private Context mContext = null;
    private boolean mObserverRegistered = false;
    private IBaseCallback mCallback = null;
    private long mHaveDownloadSize = 0;
    private boolean mDownloadManagerHasStoped = false;
    private boolean mCanUseMobileNetwork = false;

    public DownloadJob(Context context, DownloadNeedInfo downloadNeedInfo, boolean canUseMobileNetwork) {

        super(TAG);
        mContext = context;
        mQuery = new DownloadManager.Query();
        mUrl = downloadNeedInfo.mUrl;
        mFileTotalSize = downloadNeedInfo.mSize;
        mFilePath = downloadNeedInfo.mFilePath;
        mMd5 = downloadNeedInfo.mMd5;
        mCanUseMobileNetwork = canUseMobileNetwork;
    }

    private DownloadManager getDownloadManager() {
        return (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    public void runTask() {
        logd("download start");
        try {

            boolean isMobileNetwork = HttpUtils.isMobileNetwork(mContext);
            if (isMobileNetwork && !mCanUseMobileNetwork) {
                mErrorCode = Error.ERROR_CODE_MOBILE_NETWORK;
                return;
            }

            File file = new File(mFilePath);
            if(file == null || !file.exists()){
            	mHaveDownloadSize = 0;
            }else {
	            mDownLoadId = CursorUtil.getDownloadIdByUrl(mContext, mUrl);
	            mHaveDownloadSize = FileUtil.getFileLengthByDownloadId(mContext,mDownLoadId);
            }
            logd("runTask mHaveDownloadSize = " + mHaveDownloadSize);

            updateProgress((int) mHaveDownloadSize);

            if (mHaveDownloadSize == 0) {
                sendDownloadStartEvent();
            }

            if (mHaveDownloadSize < mFileTotalSize) {
                startDownload(file);
            }

            if (mHaveDownloadSize == mFileTotalSize) {
                sendDownloadEndEvent();
                verifyFile(file);

            } else if (mHaveDownloadSize > mFileTotalSize) {
                mErrorCode = Error.ERROR_CODE_FILE_VERIFY_FAILED;
            }

            logd("download end");

        } finally {
            mContext = null;
            unregisterObserver();
            logd("unregisterObserver end");
            CursorUtil.closeCursor(mCursor);
        }

    }

    private void startDownload(File downloadFile) {

        synchronized (this) {
            mDownLoadId = CursorUtil.getDownloadIdByUrl(mContext, mUrl);

            logd("startDownload() mDownLoadId =  " + mDownLoadId + " submit task begin ");

            if (mCanceled) {
                return;
            }

            if (mDownLoadId == Config.ERROR_DOWNLOAD_ID) {
                FileUtil.deleteFileIfExists(downloadFile);
                mDownLoadId = addDownloadTask(mUrl, downloadFile);

            } else {
                restartDownload(mDownLoadId, downloadFile);
            }
            mQuery.setFilterById(mDownLoadId);
            mCursor = getDownloadManager().query(mQuery);
            registerObserver();
        }

        logd("startDownload mDownLoadId =  " + mDownLoadId + " submit task end ");
        if (mCanceled && mDownloadManagerHasStoped) {
            return;
        }

        synchronized (this) {
            try {
                logd("wait begin");
                wait();
                logd("wait end");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void restartDownload(long downloadId, File downloadFile) {

        if (needRemoveDownloadTask(downloadId, downloadFile)) {
            getDownloadManager().remove(mDownLoadId);
            FileUtil.deleteFileIfExists(downloadFile);
            mDownLoadId = addDownloadTask(mUrl, downloadFile);
            LogUtils.log(TAG, "restartDownload   The New mDownLoadId   = " + mDownLoadId);
        } else {
            GnDownloadManagerTools.restart(mContext.getContentResolver(), mDownLoadId);
        }
    }

    private boolean needRemoveDownloadTask(long downloadId, File downloadFile) {
        Cursor newCursor = null;
        try {
            Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            newCursor = getDownloadManager().query(query);

            if (newCursor == null || !newCursor.moveToFirst()) {
                return false;
            }

            int status = CursorUtil.getInt(newCursor, DownloadManager.COLUMN_STATUS);
            if (status != DownloadManager.STATUS_PENDING && !downloadFile.exists()) {
                loge("needRemoveDownloadTask() status == " + status + " && !downloadFile.exists()");
                return true;

            }

            String pathInDownloadProvider = CursorUtil.getString(newCursor,
                    DownloadManager.COLUMN_LOCAL_FILENAME);
            if (!mFilePath.equals(pathInDownloadProvider)) {
                loge("needRemoveDownloadTask() !mFilePath.equals( " + pathInDownloadProvider + " )");
                return true;
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
            CursorUtil.closeCursor(newCursor);
        }

        return false;

    }

    private void pauseDownloadManager() {

        synchronized (this) {
            logd("pauseDownloadManager mDownLoadId = " + mDownLoadId + " mDownloadManagerHasStoped = "
                    + mDownloadManagerHasStoped);

            if (mContext == null) {
                logd("pauseDownloadManager () mContext is null, maybe already canceled");
                return;
            }

            if (mDownLoadId != -1) {
                GnDownloadManagerTools.pause(mContext.getContentResolver(), mDownLoadId);
            }
        }

        if (mDownloadManagerHasStoped) {
            notifyWhenWaiting();
        }

    }

    private long addDownloadTask(String downUrl, File downloadFile) {

        long downloadId = Config.ERROR_DOWNLOAD_ID;
        Request request = new DownloadManager.Request(Uri.parse(downUrl));
        request.setDestinationInExternalPublicDir("", mMd5 + ".zip");
        request.setNotificationVisibility(Request.VISIBILITY_HIDDEN);
        request.setVisibleInDownloadsUi(false);
       // request.setAllowedNetworkTypes(  DownloadManager.Request.NETWORK_MOBILE  | DownloadManager.Request.NETWORK_WIFI);

        if (StorageUtil.isFileInInternalStoarge(mContext, downloadFile)) {
            logd("download into STORAGE_INTERNAL");
            downloadId = GnDownloadManagerTools.enqueueAndChoiceStorage(mContext, request,
                    GnDownloadManagerTools.STORAGE_INTERNAL);
        } else {
            logd("download into STORAGE_SDCARD");
            downloadId = GnDownloadManagerTools.enqueueAndChoiceStorage(mContext, request,
                    GnDownloadManagerTools.STORAGE_SDCARD);
        }

        GnDownloadManagerTools.setPriority(mContext.getContentResolver(), downloadId,
                GnDownloadManagerTools.MAX_PRIORITY);
        return downloadId;

    }

    private class DownloadChangeObserver extends ContentObserver {
        public DownloadChangeObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {

            if (mContext == null) {
                loge("onChange() mContext is null, already canceled");
                return;
            }

            Cursor newCursor = getDownloadManager().query(mQuery);

            try {
                if (newCursor == null || !newCursor.moveToFirst()) {
                    throw new Exception("cursor is null");
                }

                int status = CursorUtil.getInt(newCursor, DownloadManager.COLUMN_STATUS);
                int reason = CursorUtil.getInt(newCursor, DownloadManager.COLUMN_REASON);
                logd("onChange() status = " + status + "\treason = " + reason);

                switch (status) {
                    case DownloadManager.STATUS_FAILED:
                        mDownloadManagerHasStoped = true;
                        handleDownloadFiled(reason);
                        break;

                    case DownloadManager.STATUS_PAUSED:
                        mDownloadManagerHasStoped = true;
                        handleDownloadPaused(reason);
                        break;

                    case DownloadManager.STATUS_RUNNING:
                        mDownloadManagerHasStoped = false;
                        long currentBytes = CursorUtil.getLong(newCursor,
                                DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                        logd("onChange() currentBytes = " + currentBytes);
                        updateProgress((int) currentBytes);
                        break;

                    case DownloadManager.STATUS_SUCCESSFUL:
                        notifyWhenWaiting();
                        break;

                    case DownloadManager.STATUS_PENDING:
                        if (mCanceled) {
                            notifyWhenWaiting();
                            mDownloadManagerHasStoped = true;
                        }
                        break;

                    default:
                        break;
                }

            } catch (Exception e) {
                e.printStackTrace();
                sendErrorEvent(Error.ERROR_CODE_NETWORK_ERROR);

            } finally {
                CursorUtil.closeCursor(newCursor);
            }

        }

        private void handleDownloadPaused(int reason) {

            if (reason == 5/*DownloadManager.PAUSED_BY_APP*/ || mCanceled) {
                notifyWhenWaiting();

            } else {
                sendErrorEvent(Error.ERROR_CODE_NETWORK_ERROR);
            }

        }

        private void handleDownloadFiled(int reason) {

            if (mCanceled) {
                pauseDownloadManager();
                notifyWhenWaiting();
                return;
            }

            switch (reason) {
                case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                    stopDownloadWhenError(Error.ERROR_CODE_STORAGE_NOT_MOUNTED);
                    break;

                case DownloadManager.ERROR_FILE_ERROR:
                    stopDownloadWhenError(Error.ERROR_CODE_DOWNLOADFILE_DELETED);
                    break;

                case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                    handleNoSpaceError();
                    break;

                default:
                    if (reason >= 400 && reason <= 600) {
                        handleRemoteFileNotFound();

                    } else {
                        sendErrorEvent(Error.ERROR_CODE_NETWORK_ERROR);
                    }
                    break;
            }

        }

        private void handleRemoteFileNotFound() {
            stopDownloadWhenError(Error.ERROR_CODE_REMOTE_FILE_NOT_FOUND);
        }

        private void stopDownloadWhenError(int errorCode) {
            mErrorCode = errorCode;
            pauseDownloadManager();
            notifyWhenWaiting();
        }

        private void handleNoSpaceError() {
            if (StorageUtil.isExSdcardInserted(mContext) && StorageUtil.isFileInInternalStoarge(mContext, new File(mFilePath))) {
                stopDownloadWhenError(Error.ERROR_CODE_INTERNAL_STORAGE_NO_SPACE);
            } else {
                stopDownloadWhenError(Error.ERROR_CODE_STORAGE_NO_SPACE);
            }

        }
    }

    private void sendErrorEvent(int errorCode) {
        sendEvent(new JobEvent(JobEventType.EVENT_ERROE, errorCode));
    }

    @Override
    public void registerCallback(IBaseCallback callback) {
        if (callback == null) {
            loge("register Download Callback callback is null");
            return;
        }
        if (callback instanceof IOtaDownloadCallback || callback instanceof IOtaPauseDownloadCallback) {
            mCallback = callback;
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

        logd("onResult() mStatus = " + mStatus + " mCanceled = " + mCanceled);

        if (mCallback == null) {
            loge("onResult() mCallback is null");
            return;
        }

        sendResult();

        mCallback = null;
    }

    private void sendResult() {
        switch (mStatus) {
            case STATUS_ERROR:
                mCallback.onError(mErrorCode);
                break;

            case STATUS_COMPLETE:
                callbackDownloadComplete();
                break;

            case STATUS_CANCELED:
                callbackDownloadCanceled();
                break;

            default:
                break;
        }
    }

    private void callbackDownloadCanceled() {
        if (mCallback instanceof IOtaPauseDownloadCallback) {
            ((IOtaPauseDownloadCallback) mCallback).onPauseComplete();
        } else {
            loge("callbackDownloadComplete wrong callback type");
        }
    }

    private void callbackDownloadComplete() {
        if (mCallback instanceof IOtaDownloadCallback) {
            ((IOtaDownloadCallback) mCallback).onVerifySucessful();
        } else {
            loge("callbackDownloadComplete wrong callback type");
        }
    }

    protected void logd(String msg) {
        LogUtils.logd(TAG,  msg);
    }

    protected void loge(String msg) {
        LogUtils.loge(TAG,  msg);
    }

    private void verifyFile(File file) {
        if (mCanceled) {
            return;
        }

        boolean result = FileUtil.verifyFileByMd5(file, mMd5);

        if (result == false) {
        	//校验失败的时候，把数据库中的这条记录删除
        	CursorUtil.deleteOneRecordById(mContext, mDownLoadId);
            FileUtil.deleteFileIfExists(file);
            mErrorCode = Error.ERROR_CODE_FILE_VERIFY_FAILED;
        }
    }

    private void registerObserver() {
        synchronized (mDownloadChangeObserver) {
            logd("registerObserver() mObserverRegistered " + mObserverRegistered + " mCursor is null ? "
                    + (mCursor == null));
            if (!mObserverRegistered && mCursor != null) {
                try {
                    mCursor.registerContentObserver(mDownloadChangeObserver);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mObserverRegistered = true;
                logd("registerObserver() mObserverRegistered sucessful");
            }
        }

    }

    private void unregisterObserver() {
        synchronized (mDownloadChangeObserver) {
            logd("unregisterObserver() mObserverRegistered = " + mObserverRegistered + " mCursor is null ? "
                    + (mCursor == null));
            if (mObserverRegistered && mCursor != null) {
                try {
                    mCursor.unregisterContentObserver(mDownloadChangeObserver);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mObserverRegistered = false;
                logd("unregisterObserver() mObserverRegistered sucessful");
            }
        }
    }

    public void onDownloadComplete() {
        LogUtils.logd(TAG, "onDownloadComplete");
        notifyWhenWaiting();
    }

    private void notifyWhenWaiting() {
        logd("notify");
        synchronized (this) {
            notify();
        }

    }

    public void onErrorHappend(int errorCode) {
        loge("onErrorHappend errorCode = " + errorCode);
        mErrorCode = errorCode;
        pauseDownloadManager();
    }
    
    public void setPauseFlag(boolean flag){
    	mDownloadManagerHasStoped = flag;
    }

    @Override
    public void cancelTask() {
        pauseDownloadManager();

    }

    @Override
    protected void onEvent(JobEvent event) {
        super.onEvent(event);
        LogUtils.log(TAG, "run the onEvent !!!");
        if (mCanceled) {
            return;
        }
        if (mCallback == null) {
            return;
        }

        if (mCallback instanceof IOtaDownloadCallback) {
            handleEvent(event);
        }

    }

    private void updateProgress(int currentBytes) {
//      logd("updateProgress currentBytes  = " + currentBytes);
        mHaveDownloadSize = currentBytes;
        sendEvent(new JobEvent(JobEventType.EVENT_UPDATE_PROGRESS, currentBytes));

    }

    private void sendDownloadStartEvent() {
        sendEvent(new JobEvent(JobEventType.EVENT_DOWNLOAD_BEGIN, 0));
    }

    private void sendDownloadEndEvent() {
        sendEvent(new JobEvent(JobEventType.EVENT_DOWNLOAD_END, 0));
    }

    private void handleEvent(JobEvent event) {

        IOtaDownloadCallback downloadCallback = (IOtaDownloadCallback) mCallback;
        switch (event.mEventType) {
            case EVENT_UPDATE_PROGRESS:
                downloadCallback.onProgress(event.mEventContent);
                break;

            case EVENT_ERROE:
                downloadCallback.onError(event.mEventContent);
                break;

            case EVENT_DOWNLOAD_END:
                downloadCallback.onDownloadComplete();
                break;
            default:
                break;
        }
    }
}
