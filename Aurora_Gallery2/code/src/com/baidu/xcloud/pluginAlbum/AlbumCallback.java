/*
 * AbsractCloudStroageListener.java
 * 
 * Version:
 *
 * Date: 2013-6-7
 *
 * Changes:
 * [Date@Author]:Content
 * 
 * Copyright 2012-2013 Baidu. All Rights Reserved
 */

package com.baidu.xcloud.pluginAlbum;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

import com.baidu.xcloud.account.AccountInfo;
import com.baidu.xcloud.pluginAlbum.bean.AsyncTaskBaseBean;
import com.baidu.xcloud.pluginAlbum.bean.DiffResponse;
import com.baidu.xcloud.pluginAlbum.bean.FileFromToResponse;
import com.baidu.xcloud.pluginAlbum.bean.FileInfoResponse;
import com.baidu.xcloud.pluginAlbum.bean.FileLinkResponse;
import com.baidu.xcloud.pluginAlbum.bean.ListInfoResponse;
import com.baidu.xcloud.pluginAlbum.bean.MetaResponse;
import com.baidu.xcloud.pluginAlbum.bean.QuotaResponse;
import com.baidu.xcloud.pluginAlbum.bean.SimpleResponse;
import com.baidu.xcloud.pluginAlbum.bean.ThumbnailResponse;

/**
 * 
 * @author zhaopeng05
 */
public class AlbumCallback extends IAlbumCallback.Stub {

    private static final String TAG = "AlbumCallback";
    public static final String TASKID_KEY = "TASKIDLISTEN";
    private ConcurrentHashMap<String, IAlbumListener> mTaskIdToListenerMap = null;
    protected final static AtomicLong sGlobalTaskListenerId = new AtomicLong(0L);
    Handler mbUiThreadHandler;

    public AlbumCallback(Handler handler) {
        this.mbUiThreadHandler = handler;
        mTaskIdToListenerMap = new ConcurrentHashMap<String, IAlbumListener>();
    }

    /**
     * @param quotaResponse
     * @return
     */
    private IAlbumListener getReplyListener(AsyncTaskBaseBean quotaResponse) {
        IAlbumListener ret = null;
        String key = quotaResponse.get(TASKID_KEY);
        if (key != null) {
            // WeakReference<ICloudStorageListener> ref = mTaskIdToListenerMap
            // .get(key);
            ret = mTaskIdToListenerMap.get(key);
            // Log.d(TAG, "recordTaskListener -------ret:" + ret);
            // no need to use this listener
            if (ret != null && quotaResponse.getStatusType() == AsyncTaskBaseBean.STATUS_TYPE_END) {
                mTaskIdToListenerMap.remove(key);
            }
        }
        return ret;

    }

    public void onGetQuota(QuotaResponse quotaResponse) throws RemoteException {
        final IAlbumListener mAlbumListener = getReplyListener(quotaResponse);
        if (mAlbumListener != null && quotaResponse != null) {
            final QuotaResponse fquotaResponse = quotaResponse;
            mbUiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAlbumListener.onGetQuota(fquotaResponse);
                }
            });
        } else {
            Log.d(TAG, "onGetQuota parameter null");
        }
    }

    @Override
    public void onDeletePhotos(SimpleResponse simplefiedResponse) throws RemoteException {
        final IAlbumListener mAlbumListener = getReplyListener(simplefiedResponse);
        if (mAlbumListener != null && simplefiedResponse != null) {
            final SimpleResponse fsimplefiedResponse = simplefiedResponse;
            mbUiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAlbumListener.onDeleteFiles(fsimplefiedResponse);
                }
            });
        } else {
            Log.d(TAG, "onDeleteFiles parameter null");
        }
    }

    @Override
    public void onMakePhotoDir(FileInfoResponse fileInfoResponse) throws RemoteException {
        final IAlbumListener mAlbumListener = getReplyListener(fileInfoResponse);
        if (mAlbumListener != null && fileInfoResponse != null) {
            final FileInfoResponse ffileInfoResponse = fileInfoResponse;
            mbUiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAlbumListener.onMakeDir(ffileInfoResponse);
                }
            });
        } else {
            Log.d(TAG, "onMakeDir parameter null");
        }
    }

    @Override
    public void onGetPhotoMeta(MetaResponse metaResponse) throws RemoteException {
        final IAlbumListener mAlbumListener = getReplyListener(metaResponse);
        if (mAlbumListener != null && metaResponse != null) {
            final MetaResponse fmetaResponse = metaResponse;
            mbUiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAlbumListener.onGetFileMeta(fmetaResponse);
                }
            });
        } else {
            Log.d(TAG, "onGetFileMeta parameter null");
        }
    }

    @Override
    public void onGetPhotoList(ListInfoResponse listInfoResponse) throws RemoteException {
        final IAlbumListener mAlbumListener = getReplyListener(listInfoResponse);
        if (mAlbumListener != null && listInfoResponse != null) {
            final ListInfoResponse flistInfoResponse = listInfoResponse;
            mbUiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAlbumListener.onGetFileList(flistInfoResponse);
                }
            });
        } else {
            Log.d(TAG, "onGetFileList parameter null");
        }
    }

    @Override
    public void onMovePhotos(FileFromToResponse fileFromToResponse) throws RemoteException {
        final IAlbumListener mAlbumListener = getReplyListener(fileFromToResponse);
        if (mAlbumListener != null && fileFromToResponse != null) {
            final FileFromToResponse ffileFromToResponse = fileFromToResponse;
            mbUiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAlbumListener.onMoveFiles(ffileFromToResponse);
                }
            });
        } else {
            Log.d(TAG, "onMoveFiles parameter null");
        }
    }

    @Override
    public void onThumbnail(ThumbnailResponse thumbnailResponse) throws RemoteException {
        final IAlbumListener mAlbumListener = getReplyListener(thumbnailResponse);
        if (mAlbumListener != null && thumbnailResponse != null) {
            final ThumbnailResponse fthumbnailResponse = thumbnailResponse;
            mbUiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAlbumListener.onThumbnail(fthumbnailResponse);
                }
            });
        } else {
            Log.d(TAG, "onThumbnail parameter null");
        }
    }

    /**
     * @param accountInfo
     * @param string
     * @param mAlbumListener
     */
    public void recordTaskListener(AccountInfo accountInfo, String string, IAlbumListener mAlbumListener) {
        long id = sGlobalTaskListenerId.getAndIncrement();
        String key = "callBackKey" + id;// 组合键避免重复
        mTaskIdToListenerMap.put(key, mAlbumListener);
        accountInfo.add(TASKID_KEY, key);
    }

    @Override
    public void onRenamePhotos(FileFromToResponse fileFromToResponse) throws RemoteException {
        final IAlbumListener mAlbumListener = getReplyListener(fileFromToResponse);
        if (mAlbumListener != null && fileFromToResponse != null) {
            final FileFromToResponse ffileFromToResponse = fileFromToResponse;
            mbUiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAlbumListener.onRenameFiles(ffileFromToResponse);
                }
            });
        } else {
            Log.d(TAG, "onMoveFiles parameter null");
        }
    }

    @Override
    public void onCopyPhotos(FileFromToResponse fileFromToResponse) throws RemoteException {
        final IAlbumListener mAlbumListener = getReplyListener(fileFromToResponse);
        if (mAlbumListener != null && fileFromToResponse != null) {
            final FileFromToResponse response = fileFromToResponse;
            mbUiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAlbumListener.onCopyFiles(response);
                }
            });
        } else {
            Log.d(TAG, "onCopyPhotos parameter null");
        }
    }

    @Override
    public void onShare(FileLinkResponse fileLinkResponse) throws RemoteException {
        final IAlbumListener mAlbumListener = getReplyListener(fileLinkResponse);
        if (mAlbumListener != null && fileLinkResponse != null) {
            final FileLinkResponse response = fileLinkResponse;
            mbUiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAlbumListener.onShare(response);
                }
            });
        } else {
            Log.d(TAG, "onShare parameter null");
        }
    }

    @Override
    public void onCancelShare(FileLinkResponse fileLinkResponse) throws RemoteException {
        final IAlbumListener mAlbumListener = getReplyListener(fileLinkResponse);
        if (mAlbumListener != null && fileLinkResponse != null) {
            final FileLinkResponse response = fileLinkResponse;
            mbUiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAlbumListener.onCancelShare(response);
                }
            });
        } else {
            Log.d(TAG, "onCopyPhotos parameter null");
        }
    }

    @Override
    public void onXcloudError(int errorCode) throws RemoteException {
        Log.d(TAG, "xcloud engine errorCode:" + errorCode);
//        switch (errorCode) {
//            case BaiduAlbumMain.CODE_ENGIN_HAVETO_UPGRADE_ERROR:
//                mbUiThreadHandler.sendMessage(mbUiThreadHandler.obtainMessage(BaiduAlbumMain.MSG_XLOUD_ENGINE_UPGREAD,
//                        "引擎升级中."));
//                break;
//
//            case BaiduAlbumMain.CODE_XCLOUD_ACCOUNT_LOGOUT:
//                mbUiThreadHandler.sendMessage(mbUiThreadHandler.obtainMessage(BaiduAlbumMain.MSG_XLOUD_ACCOUNT_LOGOUT,
//                        "引擎帐号已登出."));
//                break;
//            default:
//                break;
//        }

    }

    @Override
    public void onDiffWithCursor(DiffResponse diffResponse) throws RemoteException {
        final IAlbumListener mAlbumListener = getReplyListener(diffResponse);
        if (mAlbumListener != null && diffResponse != null) {
            final DiffResponse fdiffResponse = diffResponse;
            mbUiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAlbumListener.onDiffWithCursor(fdiffResponse);
                }
            });
        } else {
            Log.d(TAG, "onGetFileList parameter null");
        }
    }
}
