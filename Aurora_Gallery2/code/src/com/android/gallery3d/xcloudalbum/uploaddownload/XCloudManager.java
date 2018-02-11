package com.android.gallery3d.xcloudalbum.uploaddownload;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.xcloud.account.AccountInfo;
import com.baidu.xcloud.account.AuthInfo;
import com.baidu.xcloud.account.AuthResponse;
import com.baidu.xcloud.account.IAuthLoginListener;
import com.baidu.xcloud.pluginAlbum.AbstractAlbumListener;
//import com.baidu.xcloud.album.demo.ListViewAdapter.Cache;
import com.baidu.xcloud.pluginAlbum.AccountProxy;
import com.baidu.xcloud.pluginAlbum.AlbumClientProxy;
import com.baidu.xcloud.pluginAlbum.AlbumConfig;
import com.baidu.xcloud.pluginAlbum.IAlbumTaskListener;
import com.baidu.xcloud.pluginAlbum.AccountProxy.IAccountInfoListener;
import com.baidu.xcloud.pluginAlbum.bean.AsyncTaskBaseBean;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;
import com.baidu.xcloud.pluginAlbum.bean.ErrorCode;
import com.baidu.xcloud.pluginAlbum.bean.FileInfoResponse;
import com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean;
import com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo;
import com.baidu.xcloud.pluginAlbum.bean.ListInfoResponse;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class XCloudManager {
	
    static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getPath();
    static final String LOCAL_PATH = SDCARD_PATH + "/albumTest/";// 相册的主目录位置:sdcard/albumTest
    static final String DOWNLOAD_PATH = LOCAL_PATH + "download";// 相册的下载位置:sdcard/albumTest/download
	
	private AccountInfo mAccount;
	private final static String IUNI_THIRD_TOKEN = AlbumConfig.IUNI_TEST_TOKEN;
	private String mToken;
	private String mUsername;
	private AlbumClientProxy mAlbumClient;
	
	private Activity mActivity;

	private List<FileUpDownloadInfo> fileList;
	private HashMap<String, Cache> cacheMap = new HashMap<String, Cache>();
	
    static class Cache {
        int position;
        String path;
        String name;
        boolean isDir;
        boolean isChecked;
    }
	
	public XCloudManager(Activity activity) {
		mActivity = activity;
		initAccountProxy();
	}
	
    void initAccountProxy() {
        AccountProxy.getInstance().init(mActivity.getApplicationContext(), AlbumConfig.APPID, AlbumConfig.APIKEY,
        		IUNI_THIRD_TOKEN, AlbumConfig.BAIDU_PERMISSIONS);
    }
    
    public AccountInfo getAccountInfo() {
    	return mAccount;
    }
	
    private Handler mHandler = new Handler() {

        public void handleMessage(android.os.Message msg) {
        	/*
            switch (msg.what) {
                case MSG_INIT_BEGIN:
                    mInitBtn.setEnabled(false);
                    break;

                case MSG_INIT_END:
                    Toast.makeText(getApplicationContext(), "初始化完成.....", Toast.LENGTH_SHORT).show();
                    mInitBtn.setEnabled(true);
                    break;

                case MSG_REFRESH:
                    String path = msg.obj.toString();
                    currentPath = path;
                    list(path);
                    break;
                case MSG_LOAD_ICON:
                    Cache cache = (Cache) msg.obj;
                    loadIcon(cache);
                    break;
                case MSG_DOWNLAOD:
                    String downloadPath = msg.obj.toString();
                    download();
                    break;
                case MSG_PREVIEW:
                    String previewPath = msg.obj.toString();
                    previewImage(previewPath);
                    break;

                case MSG_SELECTED:
                    Cache cache1 = (Cache) msg.obj;
                    updateCache(cache1);
                    break;
                default:
                    break;
            }
            */
        };
    };
	
	final AccountProxy.IAccountInfoListener loginListener = new AccountProxy.IAccountInfoListener() {
		
        public void onException(final String errorMsg) {
        	/*
            Log.e(TAG, "onException:::");
            if (!errorMsg.contains("Fail to do task expantion")) {
                mThirdAccessToken = "";
            }
            */
            //showToast("Login error:" + errorMsg);
        	Log.i("SQF_LOG", "Login error-===================== " + errorMsg);
        }

        public void onComplete(AccountInfo account) {
            //Log.d(TAG, "onComplete:::");
            mAccount = account;
            //showToast("Login complete:" + (account != null));
            
            mToken = mAccount.getAccessToken();
            mUsername = mAccount.getUid();
            Log.i("SQF_LOG", "Login complete-============>mToken:" + mToken + " mUsername:" + mUsername);
//            if (mPref == null) { 
//                mPref = getApplicationContext().getSharedPreferences(AccountProxy.NAME_PREF_XCLOUD_ACCOUNT, 0);
//                mPref.edit().putString(AccountProxy.KEY_PREF_THIRD_TOKEN, mThirdAccessToken);
//            }
            setup();
        }

        public void onCancel() {
            Log.d("SQF_LOG", "Login onCancel:::");
            //showToast("Login cancelled");
        }
    };
    
    private String getDeviceMsg() {
        String msg = "";
        msg = Build.MODEL;
        return msg;
    }
    
    /**
     * 设置(设置服务端打开日志,设置下载/上传线程数等)
     */
    private void setup() {

        if (TextUtils.isEmpty(mToken)) {
            Toast.makeText(mActivity.getApplicationContext(), "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        AccountInfo accoutInfo = new AccountInfo(mToken);
        accoutInfo.setUid(mUsername);

        if (TextUtils.isEmpty(mAccount.getUid())) {
            String msg = "请设定一个Uid字段(该字段能够区分不同用户以及不同App的不同用户)，以标志要启动的文件任务属于哪个用户。";
            Toast.makeText(mActivity.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            return;
        }

        mAlbumClient = AlbumClientProxy.getInstance(mHandler);

        Bundle config = new Bundle();
        config.putInt(AlbumClientProxy.UPLOAD_THREAD_COUNT, AlbumClientProxy.DEFAULT_THREAD_COUNT);
        config.putInt(AlbumClientProxy.DOWNLOAD_THREAD_COUNT, AlbumClientProxy.DEFAULT_THREAD_COUNT);
        config.putBoolean(AlbumClientProxy.LOG_SWITCH, true);
        mAlbumClient.setup(mActivity.getApplicationContext(), mAccount, config);
    }
    
    public void login() {
    	explicitLogin(IUNI_THIRD_TOKEN);
    }

    public void explicitLogin(String thirdToken) {
        AccountProxy proxy = AccountProxy.getInstance();
        proxy.setThirdToken(thirdToken);
        proxy.getAccountInfo(loginListener, false, AuthInfo.AUTHTYPE_NEWEXPLICITBIND);
    }
    
    /**
     * 相册列表
     * 
     * @param path 云端路径
     */
    public void list(String path) {
        if (mAccount == null) {
            Toast.makeText(mActivity.getApplicationContext(), "请先登陆!", Toast.LENGTH_SHORT).show();
            return;
        }
        setup();
        // cacheMap.clear();

        mAlbumClient.getPhotoList(mAccount, path, "name", "asc", new AbstractAlbumListener() {
            @Override
            public void onGetFileList(ListInfoResponse listInfoResponse) {
                String taskId = listInfoResponse.getTaskId();
                int statusType = listInfoResponse.getStatusType();
                int errorCode = listInfoResponse.getErrorCode();
                String errorMsg = listInfoResponse.getMessage();
                Log.d("SQF_LOG", "onGetFileList:" + statusType + " taskID:" + taskId);
                if (null != listInfoResponse) {
                    if (ErrorCode.No_Error == errorCode) {
                        if (statusType == AsyncTaskBaseBean.STATUS_TYPE_END) {
                        	 List<CommonFileInfo> list = listInfoResponse.list;
                             for(int i=0; i<list.size(); i++) {
                             	CommonFileInfo info = list.get(i);
                             }
                             //refreshView(list);
                        }
                    } else {
                        Toast.makeText(mActivity.getApplicationContext(),
                                "getFileList failed. errorCode:" + errorCode + " errorMsg:" + errorMsg,
                                Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }
    
    
    public void mkPhothdir() {
        if (mAccount == null) {
            Toast.makeText(mActivity.getApplicationContext(), "请先登陆!", Toast.LENGTH_SHORT).show();
            return;
        }

        String path = AlbumConfig.REMOTEPATH + "/" + getDeviceMsg() + "/mkTestDir";

        mAlbumClient.makePhotoDir(mAccount, path, new AbstractAlbumListener() {
            public void onMakeDir(FileInfoResponse fileInfoResponse) {
                String taskId = fileInfoResponse.getTaskId();
                int statusType = fileInfoResponse.getStatusType();
                int errorCode = fileInfoResponse.getErrorCode();
                String errorMsg = fileInfoResponse.getMessage();
                //Log.d(TAG, "onMakeDir:" + statusType + " taskID:" + taskId);
                Log.d("SQF_LOG", "onMakeDir:" + statusType + " taskID:" + taskId);
                if (null != fileInfoResponse) {
                    if (ErrorCode.No_Error == errorCode) {
                        if (statusType == AsyncTaskBaseBean.STATUS_TYPE_END) {
                            Toast.makeText(mActivity.getApplicationContext(),
                                    "MakeDir success. file:" + fileInfoResponse.commonFileInfo.path, Toast.LENGTH_LONG)
                                    .show();
                        }
                    } else {
                        Toast.makeText(mActivity.getApplicationContext(),
                                "MakeDir failed. errorCode:" + errorCode + " errorMsg:" + errorMsg, Toast.LENGTH_LONG)
                                .show();
                    }
                }
            }
        });
    }
    
    
    
    /**
     * 任务相关监听器(上传,下载统称为任务.)
     */
    IAlbumTaskListener fileTaskListener = new IAlbumTaskListener() {
        @Override
        public long progressInterval() {
            return 2000;
        }

        /**
         * 调用AlbumClientProxy.getPhotoTaskList()时的回调.
         */
        @Override
        public void onGetTaskListFinished(List<FileTaskStatusBean> fileTaskStatusBeanList) {

        }

        /**
         * 调用AlbumClientProxy.upload/download等方法时的回调.
         */
        @Override
        public void onGetTaskStatus(FileTaskStatusBean bean) {
            Log.w("SQF_LOG",
                    "...........IAlbumTaskListener onFileTaskStatus taskId:" + bean.getFileTaskId() + " currentSize:" + bean.getCurrentSize()
                            + " totalSize:" + bean.getTotalSize() + " type:" + bean.getType() + " statusType:"
                            + bean.getStatusType() + " errorCode:" + bean.getErrorCode() + " message:"
                            + bean.getMessage());
            if (bean != null) {
                if (bean.getStatusTaskCode() == FileTaskStatusBean.STATE_TASK_DONE) {

                    if (bean.getType() == 0) {
                    	/*
                        int seek = temp++;
                        //mProgress.setProgress(seek);
                        System.out.println("progress2:" + mProgress.getProgress());
                        if (seek == mProgress.getMax()) {
                            mProgress.setVisibility(View.GONE);
                            temp = 0;
                        }
                        */
                    	
                    	
                    	
                    } else if (bean.getType() == 1) {
                        Toast.makeText(mActivity.getApplicationContext(), bean.getFileName() + ",下载完成.", Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            }
        }
    };
    
    
    /**
     * 上传
     */
    public void upload() {
        if (mAccount == null) {
            Toast.makeText(mActivity.getApplicationContext(), "请先登陆!", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.i("SQF_LOG", "upload");

        //mProgress.setVisibility(View.VISIBLE);

        setup();

        fileList = new ArrayList<FileUpDownloadInfo>();
        
        //SD卡 /albumTest/
        
        File fRoot = new File(LOCAL_PATH);

        if (fRoot.exists() && fRoot.isDirectory()) {
            File[] fs = fRoot.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    String tmp = pathname.getName().toLowerCase();
                    System.out.println(tmp);
                    if (tmp.equals("camera") || tmp.equals("image")) {
                        return true;
                    }
                    return false;
                }
            });
            for (File f : fs) {
                if (f.isDirectory()) {

                    File[] f1 = f.listFiles();

                    for (File f2 : f1) {
                        try {
                            String sourcePath = f2.getPath();

                            StringBuffer remotePath = new StringBuffer();
                            remotePath.append(AlbumConfig.REMOTEPATH).append("/");
                            remotePath.append(getDeviceMsg());

                            int index = sourcePath.indexOf(SDCARD_PATH);
                            if (index != -1) {
                                remotePath.append(sourcePath.subSequence(SDCARD_PATH.length(), sourcePath.length()));
                            }
                            FileUpDownloadInfo info = 
                                    new FileUpDownloadInfo(sourcePath, remotePath.toString(),
                                            FileUpDownloadInfo.TYPE_UPLOAD, FileUpDownloadInfo.OVER_WRITE);
                            fileList.add(info);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        System.out.println(fileList.size());
        if (fileList.size() > 0) {
            //mProgress.setVisibility(View.VISIBLE);
            //mProgress.setMax(fileList.size());
            //mProgress.setProgress(0);
            //System.out.println("progress:" + mProgress.getProgress());

            //mAlbumClient.uploadPhotos(mAccount, fileList, fileTaskListener);
        	mAlbumClient.uploadPhotos(mAccount, fileList, XCloudTaskListenerManager.getInstance(mActivity).getAlbumTaskListener());
        }

    }
    
    /**
     * 下载
     */
    public void download() {
    	
    	/*
        if (mAccount == null) {
            Toast.makeText(mActivity.getApplicationContext(), "请先登陆!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cacheMap.isEmpty()) {
            Toast.makeText(mActivity.getApplicationContext(), "请先选中一条!", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.i("SQF_LOG", "download");

        fileList = new ArrayList<FileUpDownloadInfo>();

        FileUpDownloadInfo info = null;
        Set<Entry<String, Cache>> set = cacheMap.entrySet();
        for (Entry<String, Cache> entry : set) {
            String path = entry.getValue().path;

            int index = path.lastIndexOf("/");
            if (index != -1) {
                String downFile = path.substring(index, path.length());
                try {
                    info = new FileUpDownloadInfo(path, DOWNLOAD_PATH + downFile, FileUpDownloadInfo.TYPE_DOWNLOAD);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fileList.add(info);
            }
        }

        mAlbumClient.downloadPhotos(mAccount, fileList, fileTaskListener);
		*/
    	
    	fileList = new ArrayList<FileUpDownloadInfo>();
    	FileUpDownloadInfo info = null;
        // 这个云端照片地址,可以在相册列表中的 cache.path中得到.在此处是一个已在云端有的地址.
        String remotePath = AlbumConfig.REMOTEPATH + "/" + Build.MODEL + "/albumTest/image/103.jpg";
        String targetPath = AlbumConfig.DOWNLOADPATH + "/103.jpg";
        // 先创建一个空的目录结构,如果没有/download这个目录时.
        File file = new File(targetPath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            info = new FileUpDownloadInfo(remotePath, targetPath, FileUpDownloadInfo.TYPE_DOWNLOAD);
            fileList.add(info);
        } catch (IOException e) {
            Toast.makeText(mActivity, "FileUpDownloadInfo init exception", Toast.LENGTH_LONG).show();
            return;
        }

        mAlbumClient.downloadPhotos(mAccount, fileList, XCloudTaskListenerManager.getInstance(mActivity).getAlbumTaskListener());
    }
}
