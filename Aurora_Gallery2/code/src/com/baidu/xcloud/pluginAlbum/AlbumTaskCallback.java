package com.baidu.xcloud.pluginAlbum;

import java.util.List;

import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

import com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean;

public class AlbumTaskCallback extends IAlbumTaskCallback.Stub {

    private static final String TAG = "FileTaskCallback";
    private IAlbumTaskListener mFileTaskListener = null;
    private Handler mUiHandler;

    public AlbumTaskCallback(IAlbumTaskListener fileTaskListener, Handler handler) {
        this.mFileTaskListener = fileTaskListener;
        this.mUiHandler = handler;
    }

    @Override
    public void onGetTaskStatus(FileTaskStatusBean fileTaskStatusBean) throws RemoteException {
        if (mFileTaskListener != null) {
            final FileTaskStatusBean bean = fileTaskStatusBean;
            mUiHandler.post(new Runnable() {

                @Override
                public void run() {
                    mFileTaskListener.onGetTaskStatus(bean);
                }
            });
        } else {
            Log.d(TAG, "onFileTaskStatus null");
        }
    }

    @Override
    public long progressInterval() throws RemoteException {
        return mFileTaskListener.progressInterval();
    }

    @Override
    public void onGetTaskListFinished(List<FileTaskStatusBean> fileTaskStatusBeanList) throws RemoteException {
        if (mFileTaskListener != null && fileTaskStatusBeanList != null) {
            final List<FileTaskStatusBean> list = fileTaskStatusBeanList;
            mUiHandler.post(new Runnable() {

                @Override
                public void run() {
                    mFileTaskListener.onGetTaskListFinished(list);
                }
            });
        } else {
            Log.d(TAG, "onGetFileTaskListFinished null");
        }
    }

    @Override
    public void onXcloudError(int errorCode) throws RemoteException {
        Log.d(TAG, "xcloud engine errorCode:" + errorCode);
//        switch (errorCode) {
//            case BaiduAlbumMain.CODE_ENGIN_HAVETO_UPGRADE_ERROR:
//                mUiHandler.sendMessage(mUiHandler.obtainMessage(BaiduAlbumMain.MSG_XLOUD_ENGINE_UPGREAD, "引擎升级中."));
//                break;
//
//            case BaiduAlbumMain.CODE_XCLOUD_ACCOUNT_LOGOUT:
//                mUiHandler.sendMessage(mUiHandler.obtainMessage(BaiduAlbumMain.MSG_XLOUD_ACCOUNT_LOGOUT, "引擎帐号已登出."));
//                break;
//            default:
//                break;
//        }
    }

}
