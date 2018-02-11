package com.baidu.xcloud.pluginAlbum;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.baidu.xcloud.account.AccountInfo;
import com.baidu.xcloud.pluginAlbum.bean.ErrorCode;
import com.baidu.xcloud.pluginAlbum.bean.FileFromToInfo;
import com.baidu.xcloud.pluginAlbum.bean.FileFromToResponse;
import com.baidu.xcloud.pluginAlbum.bean.FileInfoResponse;
import com.baidu.xcloud.pluginAlbum.bean.FileLinkResponse;
import com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo;
import com.baidu.xcloud.pluginAlbum.bean.ListInfoResponse;
import com.baidu.xcloud.pluginAlbum.bean.MetaResponse;
import com.baidu.xcloud.pluginAlbum.bean.QuotaResponse;
import com.baidu.xcloud.pluginAlbum.bean.SimpleResponse;
import com.baidu.xcloud.pluginAlbum.bean.ThumbnailResponse;

/**
 * 客户端服务代理
 * 
 * @author zhaopeng05
 * 
 */
public class AlbumClientProxy {

    protected static final String TAG = "AlbumClient";

    private static AlbumClientProxy mInstance = null;
    private volatile boolean mHasBinded = false;
    private Context mContext = null;
    private BlockingQueue<Runnable> mTaskQueue = new LinkedBlockingDeque<Runnable>();
    private AlbumCallback mCallback = null;
    private IAlbum mIAlbum = null;
    private AccountInfo mAccountInfo = null;
    private Handler mHandler;

    // 日志开关
    public static final String LOG_SWITCH = "LogSwitch";
    // 上传,下载线程数
    public static final String UPLOAD_THREAD_COUNT = "uploadThreadCount";
    public static final String DOWNLOAD_THREAD_COUNT = "downloadThreadCount";
    // 默认线程数
    public static final int DEFAULT_THREAD_COUNT = 3;

    private Bundle mConfig = null;

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected");
            mIAlbum = IAlbum.Stub.asInterface(service);
            if (mIAlbum != null) {
                setupWithConfig(mAccountInfo, mConfig);
                handlePendingRequest();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected");
            mIAlbum = null;
        }
    };

    public static AlbumClientProxy getInstance(Handler handler) {
        Log.i(TAG, "getInstance");
        if (mInstance == null) {
            synchronized (AlbumClientProxy.class) {
                if (mInstance == null) {
                    mInstance = new AlbumClientProxy(handler);
                }
            }
        }
        mInstance.mHandler = handler;
        return mInstance;
    }

    private AlbumClientProxy(Handler handler) {
        mCallback = new AlbumCallback(handler);
    }

    /**
     * 设置服务端参数,其中包括打开/关闭日志,设置上传/下载的线程数等
     * 
     * @param context
     * @param accountInfo
     * @param configBundle
     */
    public void setup(Context context, final AccountInfo accountInfo, Bundle configBundle) {
        if (!mHasBinded) {
            mContext = context;
            mAccountInfo = accountInfo;
            mConfig = configBundle;
            bindRemoteService();
            mHasBinded = true;
        }
    }

    /**
     * 处理请求
     * 
     * @param request
     */
    private void handleRequest(Runnable request) {
        Log.i(TAG, "handleRequest");
        synchronized (mTaskQueue) {
			mTaskQueue.add(request);
			if (mIAlbum == null) {
				bindRemoteService();
			} else {
				handlePendingRequest();
			}
		}
    }

    /**
     * 处理挂起的请求
     */
    private void handlePendingRequest() {
        Log.i(TAG, "handlePendingRequest");
        Runnable request;
        synchronized (mTaskQueue) {
			while ((request = mTaskQueue.peek()) != null) {
				if (mIAlbum != null) {
					runRequest(request);
					mTaskQueue.remove();
				} else {
					bindRemoteService();
					return;
				}
			}
		}
    }

    private void runRequest(Runnable request) {
        request.run();
    }

    /**
     * 绑定远程服务
     */
    private void bindRemoteService() {
        Log.i(TAG, "bindRemoteService context==null:" + (mContext == null));
        if (mContext != null) {
            Intent mIntent = new Intent();
            mIntent.setComponent(new ComponentName(AlbumConfig.SERVICE_PKG, AlbumConfig.SERVICE_CLS));
            mContext.bindService(mIntent, mConnection, Context.BIND_AUTO_CREATE);
            mContext.startService(mIntent);
        }
    }

    public void destroy() {
        if (mIAlbum != null) {
            mContext.unbindService(mConnection);
            Intent mIntent = new Intent();
            mIntent.setComponent(new ComponentName(AlbumConfig.SERVICE_PKG, AlbumConfig.SERVICE_CLS));
            mContext.stopService(mIntent);
            mIAlbum = null;
        }
        mHasBinded = false;
        mInstance = null;
    }

    /**
     * 设置
     * 
     * @param accountInfo
     * @param configBundle
     */
    private void setupWithConfig(final AccountInfo accountInfo, Bundle configBundle) {
        try {
            if (mIAlbum != null) {
                mIAlbum.setupWithConfig(accountInfo, configBundle, new IAlbumSetupCallback.Stub() {
                    // 回调
                    @Override
                    public void setupFileDescriptor(List<FileUpDownloadInfo> fileUpDownloadInfoList)
                            throws RemoteException {
                        for (FileUpDownloadInfo fileUpDownloadInfo : fileUpDownloadInfoList) {
                            fileUpDownloadInfo.setupFileDescriptor();
                        }
                    }

                    @Override
                    public void onXcloudError(int errorCode) throws RemoteException {
                        Log.d(TAG, "xcloud engine errorCode:" + errorCode);
                    }
                });
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void getQuota(final AccountInfo accountInfo, final IAlbumListener albumListener) {
        handleRequest(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mCallback != null) {
                        mCallback.recordTaskListener(accountInfo, "getQuota", albumListener);
                    }
                    if (mIAlbum != null) {
                        mIAlbum.getQuota(accountInfo, mCallback);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                    albumListener.onGetQuota(new QuotaResponse(ErrorCode.Error_Server_Remote_Exception,
                            ErrorCode.Message_Remote_Exception));
                }
            }
        });
    }

    public void deletePhotos(final AccountInfo accountInfo, final List<String> files, final IAlbumListener albumListener) {
        handleRequest(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mCallback != null) {
                        mCallback.recordTaskListener(accountInfo, "deleteFiles", albumListener);
                    }
                    if (mIAlbum != null)
                        mIAlbum.deletePhotos(accountInfo, files, mCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    albumListener.onDeleteFiles(new SimpleResponse(ErrorCode.Error_Server_Remote_Exception,
                            ErrorCode.Message_Remote_Exception));
                }
            }
        });
    }

    public void makePhotoDir(final AccountInfo accountInfo, final String path, final IAlbumListener albumListener) {
        handleRequest(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mCallback != null) {
                        mCallback.recordTaskListener(accountInfo, "makeDir", albumListener);
                    }
                    if (mIAlbum != null)
                        mIAlbum.makePhotoDir(accountInfo, path, mCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    albumListener.onMakeDir(new FileInfoResponse(ErrorCode.Error_Server_Remote_Exception,
                            ErrorCode.Message_Remote_Exception));
                }
            }
        });
    }

    public void getPhotoMeta(final AccountInfo accountInfo, final String file, final boolean showDirSize,
            final IAlbumListener albumListener) {
        handleRequest(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mCallback != null) {
                        mCallback.recordTaskListener(accountInfo, "getFileMeta", albumListener);
                    }
                    if (mIAlbum != null)
                        mIAlbum.getPhotoMeta(accountInfo, file, showDirSize, mCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    albumListener.onGetFileMeta(new MetaResponse(ErrorCode.Error_Server_Remote_Exception,
                            ErrorCode.Message_Remote_Exception));
                }
            }
        });
    }

    public void getPhotoList(final AccountInfo accountInfo, final String path, final String by, final String order,
            final IAlbumListener albumListener) {
        handleRequest(new Runnable() {
            @Override
            public void run() {

                try {
                    if (mCallback != null) {
                        mCallback.recordTaskListener(accountInfo, "getFileList", albumListener);
                    }
                    if (mIAlbum != null)
                        mIAlbum.getPhotoList(accountInfo, path, by, order, mCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    albumListener.onGetFileList(new ListInfoResponse(ErrorCode.Error_Server_Remote_Exception,
                            ErrorCode.Message_Remote_Exception));
                }
            }
        });
    }

    public void getStreamList(final AccountInfo accountInfo, final String type, final int start, final int limit,
            final IAlbumListener albumListener) {
        handleRequest(new Runnable() {
            @Override
            public void run() {

                try {
                    if (mCallback != null) {
                        mCallback.recordTaskListener(accountInfo, "getPhotoList", albumListener);
                    }
                    if (mIAlbum != null)
                        mIAlbum.getStreamFileList(accountInfo, type, start, limit, mCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    albumListener.onGetFileList(new ListInfoResponse(ErrorCode.Error_Server_Remote_Exception,
                            ErrorCode.Message_Remote_Exception));
                }
            }
        });
    }

    public void diffWithCursor(final AccountInfo accountInfo, final String cursor, final IAlbumListener albumListener) {
        handleRequest(new Runnable() {
            @Override
            public void run() {

                try {
                    if (mCallback != null) {
                        mCallback.recordTaskListener(accountInfo, "diffWithCursor", albumListener);
                    }
                    if (mIAlbum != null)
                        mIAlbum.diffWithCursor(accountInfo, cursor, mCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    albumListener.onGetFileList(new ListInfoResponse(ErrorCode.Error_Server_Remote_Exception,
                            ErrorCode.Message_Remote_Exception));
                }
            }
        });
    }

    public void movePhotos(final AccountInfo accountInfo, final List<FileFromToInfo> info,
            final IAlbumListener albumListener) {
        handleRequest(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mCallback != null) {
                        mCallback.recordTaskListener(accountInfo, "moveFiles", albumListener);
                    }
                    if (mIAlbum != null)
                        mIAlbum.movePhotos(accountInfo, info, mCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    albumListener.onMoveFiles(new FileFromToResponse(ErrorCode.Error_Server_Remote_Exception,
                            ErrorCode.Message_Remote_Exception));
                }
            }
        });
    }

    public void renamePhotos(final AccountInfo accountInfo, final List<FileFromToInfo> info,
            final IAlbumListener albumListener) {
        handleRequest(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mCallback != null) {
                        mCallback.recordTaskListener(accountInfo, "renameFiles", albumListener);
                    }
                    if (mIAlbum != null)
                        mIAlbum.renamePhotos(accountInfo, info, mCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    albumListener.onRenameFiles(new FileFromToResponse(ErrorCode.Error_Server_Remote_Exception,
                            ErrorCode.Message_Remote_Exception));
                }
            }
        });
    }

    public void copyPhotos(final AccountInfo accountInfo, final List<FileFromToInfo> info,
            final IAlbumListener albumListener) {
        handleRequest(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mCallback != null) {
                        mCallback.recordTaskListener(accountInfo, "copyFiles", albumListener);
                    }
                    if (mIAlbum != null)
                        mIAlbum.copyPhotos(accountInfo, info, mCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    albumListener.onCopyFiles(new FileFromToResponse(ErrorCode.Error_Server_Remote_Exception,
                            ErrorCode.Message_Remote_Exception));
                }
            }
        });
    }

    public void thumbnail(final AccountInfo accountInfo, final String path, final int quality, final int width,
            final int height, final IAlbumListener albumListener) {
        handleRequest(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mCallback != null) {
                        mCallback.recordTaskListener(accountInfo, "thumbnail", albumListener);
                    }
                    if (mIAlbum != null)
                        mIAlbum.thumbnail(accountInfo, path, quality, width, height, mCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    albumListener.onThumbnail(new ThumbnailResponse(ErrorCode.Error_Server_Remote_Exception,
                            ErrorCode.Message_Remote_Exception));
                }
            }
        });
    }

    public void shareLink(final AccountInfo accountInfo, final String path, final IAlbumListener albumListener) {
        handleRequest(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mCallback != null) {
                        mCallback.recordTaskListener(accountInfo, "shareLink", albumListener);
                    }
                    if (mIAlbum != null)
                        mIAlbum.shareLink(accountInfo, path, mCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    albumListener.onShare(new FileLinkResponse(ErrorCode.Error_Server_Remote_Exception,
                            ErrorCode.Message_Remote_Exception));
                }
            }
        });
    }

    public void cancelShare(final AccountInfo accountInfo, final String path, final IAlbumListener albumListener) {
        handleRequest(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mCallback != null) {
                        mCallback.recordTaskListener(accountInfo, "cancelShare", albumListener);
                    }
                    if (mIAlbum != null)
                        mIAlbum.cancelShare(accountInfo, path, mCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    albumListener.onCancelShare(new FileLinkResponse(ErrorCode.Error_Server_Remote_Exception,
                            ErrorCode.Message_Remote_Exception));
                }
            }
        });
    }

    public void uploadPhotos(final AccountInfo accountInfo, final List<FileUpDownloadInfo> fileList,
            final IAlbumTaskListener fileTaskListener) {
        Log.i(TAG, "uploadFile------------begin----");
        handleRequest(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, "uploadFile------------running----mIAlbum==null:" + (mIAlbum == null));
                    if (mIAlbum != null) {
                        mIAlbum.uploadPhotos(accountInfo, fileList, new AlbumTaskCallback(fileTaskListener, mHandler));
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void downloadPhotos(final AccountInfo accountInfo, final List<FileUpDownloadInfo> fileList,
            final IAlbumTaskListener fileTaskListener) {
        handleRequest(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mIAlbum != null) {
                        mIAlbum.downloadPhotos(accountInfo, fileList, new AlbumTaskCallback(fileTaskListener, mHandler));
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void downloadFileAsSpecificType(final AccountInfo accountInfo, final List<FileUpDownloadInfo> fileList,
            final String type, final IAlbumTaskListener fileTaskListener) {
        handleRequest(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mIAlbum != null) {
                        mIAlbum.downloadFileAsSpecificType(accountInfo, fileList, type, new AlbumTaskCallback(
                                fileTaskListener, mHandler));
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void downloadFileFromStream(final AccountInfo accountInfo, final List<FileUpDownloadInfo> fileList,
            final IAlbumTaskListener fileTaskListener) {
        handleRequest(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mIAlbum != null) {
                        mIAlbum.downloadFileFromStream(accountInfo, fileList, new AlbumTaskCallback(fileTaskListener,
                                mHandler));
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void processPhotoTask(final AccountInfo accountInfo, final String type, final long taskId,
            final IAlbumTaskListener fileTaskListener) {
        handleRequest(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mIAlbum != null)
                        mIAlbum.processPhotoTask(accountInfo, type, taskId, new AlbumTaskCallback(fileTaskListener,
                                mHandler));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void processPhotoTaskList(final AccountInfo accountInfo, final String type, final String fileType,
            final IAlbumTaskListener fileTaskListener) {
        handleRequest(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mIAlbum != null) {
                        mIAlbum.processPhotoTaskList(accountInfo, type, fileType, new AlbumTaskCallback(
                                fileTaskListener, mHandler));
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getPhotoTaskList(final AccountInfo accountInfo, final IAlbumTaskListener fileTaskListener) {
        handleRequest(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mIAlbum != null) {
                        mIAlbum.getPhotoTaskList(accountInfo, new AlbumTaskCallback(fileTaskListener, mHandler));
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
