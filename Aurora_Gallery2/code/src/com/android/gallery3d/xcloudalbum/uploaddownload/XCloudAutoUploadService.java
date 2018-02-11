package com.android.gallery3d.xcloudalbum.uploaddownload;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.baidu.xcloud.pluginAlbum.IAlbumTaskListener;
import com.android.gallery3d.xcloudalbum.inter.IBaiduinterface;
import com.android.gallery3d.xcloudalbum.tools.BaiduAlbumUtils;

import com.baidu.xcloud.pluginAlbum.AlbumConfig;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;
import com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean;
import com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo;
import com.android.gallery3d.app.GalleryAppImpl;
import com.android.gallery3d.xcloudalbum.uploaddownload.UploadTaskListManager;

import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.LocalImage;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.data.FilterDeleteSet;
import com.android.gallery3d.util.MediaSetUtils;
import com.android.gallery3d.util.MyLog;
import com.android.gallery3d.util.NetworkUtil;
import com.android.gallery3d.util.PrefUtil;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.android.gallery3d.xcloudalbum.uploaddownload.FakeTaskManager;
import com.android.gallery3d.xcloudalbum.account.AccountHelper;
import com.android.gallery3d.xcloudalbum.uploaddownload.XCloudAutoUploadBroadcastReceiver;

public class XCloudAutoUploadService extends Service implements IBaiduinterface, XCloudTaskListenerManager.GetPhotoTaskListFinishListener {
	public class AutoLoadTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			//mMediaSet.getMediaItem();
			mIsAutoUploading = true;
			mMediaSet = (FilterDeleteSet) mDataManager.getMediaSet(mFilterDeleteSetPath);
			mMediaItemsCount = mMediaSet.getMediaItemCount();
			mMediaItems = mMediaSet.getMediaItem(0, mMediaItemsCount);
			//Log.i("SQF_LOG", "XCloudAutoUploadService --->  auto upload: mMediaItems.size() :" + mMediaItems.size());
			long lastAutoUploadTime = PrefUtil.getLong(mContext, PREF_KEY_LAST_AUTO_UPLOAD_TIME, -1);
			long autoUploadTimeToRecord = -1;
			ArrayList<String> filePaths = new ArrayList<String>();
			for(int i = mMediaItems.size() - 1; i >= 0; i--) {
				MediaItem item = mMediaItems.get(i);
				if( !(item instanceof LocalImage)) {
					continue;
				}
				LocalImage localImage = (LocalImage)item;
				if(localImage.fileSize == 0) {
					continue;
				}
				if(XCloudUploadFilter.postfixShouldBeFiletered(localImage.filePath) || 
						XCloudUploadFilter.filePathShouldBeFiltered(localImage.filePath)) {
					continue;
				}
				if(localImage.dateTakenInMs > lastAutoUploadTime) {
					++ mCurrentAddedUploadNum; 
					filePaths.add(localImage.filePath);
					//autoUploadTimeToRecord = localImage.dateTakenInMs - 1;//there may be some photos has same dateTakenInMs, so we minus 1 here.
					//Log.i("SQF_LOG", "XCloudAutoUploadService --->  auto upload: " + mCurrentAddedUploadNum + " "+ localImage.filePath + " time:" + localImage.dateTakenInMs);
				}
				if(mCurrentAddedUploadNum >= MAX_UPLOAD_NUM) {
					autoUploadTimeToRecord = localImage.dateTakenInMs - 1;
					break;
				} else {
					autoUploadTimeToRecord = localImage.dateTakenInMs;
				}
			}
			CommonFileInfo remoteTargetAlbum = new CommonFileInfo();
			remoteTargetAlbum.path = AlbumConfig.REMOTEPATH + "相册";
			remoteTargetAlbum.isDir = true;
			uploadToAlbum(filePaths, remoteTargetAlbum);
			//PrefUtil.setLong(mContext, PREF_KEY_LAST_AUTO_UPLOAD_TIME, System.currentTimeMillis());
			PrefUtil.setLong(mContext, PREF_KEY_LAST_AUTO_UPLOAD_TIME, autoUploadTimeToRecord);
			mCurrentAddedUploadNum = 0;
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			mIsAutoUploading = false;
		}

		@Override
		protected void onCancelled(Void result) {
			// TODO Auto-generated method stub
			super.onCancelled(result);
			mIsAutoUploading = false;
		}
		
	};
	
	private static final String PREF_KEY_LAST_AUTO_UPLOAD_TIME = "PREF_KEY_LAST_AUTO_UPLOAD_TIME";
	
	public static final int MAX_UPLOAD_NUM = 300;
	private int mCurrentAddedUploadNum = 0;
	private ArrayList<FileUpDownloadInfo> mInfos = new ArrayList<FileUpDownloadInfo>();
	
	private String mDefultPath = Path.fromString("/local/allsets/" + MediaSetUtils.CAMERA_BUCKET_ID).toString();
	private String mFilterDeleteSetPath = "/filter/delete/{" + mDefultPath + "}";
	private MediaSet mMediaSet;
	private Context mContext;
	private DataManager mDataManager;
	private ArrayList<MediaItem> mMediaItems = new ArrayList<MediaItem>();
	private int mMediaItemsCount;
	private BaiduAlbumUtils mBaiduAlbumUtils;
	private volatile boolean mIsAutoUploading;
	
	protected AccountHelper mAccountHelper; 
	private static final int MSG_UPLOAD = 100;
	private static final int MSG_JUDGE_GET_PHOTO_TASK_LIST_OR_NOT = 101;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what) {
			case MSG_UPLOAD:
				//Log.i("SQF_LOG", "XCloudAutoUploadService::MSG_UPLOAD");
				if( ! mInfos.isEmpty()) {
					GalleryAppImpl app = (GalleryAppImpl)getApplication();
					UploadTaskListManager manager = app.getUploadTaskListManager();
					manager.addTaskList(mInfos);
					mBaiduAlbumUtils.uploadToAlbum(mInfos);
				}
				break;
			case MSG_JUDGE_GET_PHOTO_TASK_LIST_OR_NOT:
				XCloudTaskListenerManager manager = XCloudTaskListenerManager.getInstance(mContext);
				manager.registerListener(mAlbumTaskListener);
				if( ! manager.isPhotoTaskListGot()) {
					//Log.i("SQF_LOG", "XCloudAutoUploadService:: Photo Task List Not Got");
					manager.sendGetPhotoTaskListDelayed(XCloudAutoUploadService.this);//SQF ADDED ON 2015.6.1
				} else {
					startUploading();
				}
				break;
			}
		}
	};

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		//Log.i("SQF_LOG", "XCloudAutoUploadService::onCreate");
		
		mContext = getBaseContext();
		mBaiduAlbumUtils = BaiduAlbumUtils.getInstance(mContext);
		GalleryAppImpl app = (GalleryAppImpl)getApplication();
		mDataManager = app.getDataManager();
		
       	mAccountHelper = new AccountHelper(this);
		mAccountHelper.registerAccountContentResolver();
		mAccountHelper.update();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
//		if(intent.hasExtra(XCloudAutoUploadBroadcastReceiver.XCLOUD_AUTO_UPLOAD_FROM_BROADCAST)) {
//			Log.i("SQF_LOG", "HAS XCLOUD_AUTO_UPLOAD_FROM_BROADCAST ------> ");
//		}
//		Bundle bundle = intent.getExtras();
//		boolean isFromBroadcast = bundle.getBoolean(XCloudAutoUploadBroadcastReceiver.XCLOUD_AUTO_UPLOAD_FROM_BROADCAST);
//		if( ! isFromBroadcast) {
//			Log.i("SQF_LOG", "NOT FROM BROADCAST ------> ");
//			return;
//		}
//		if(mIsAutoUploading) return;
//		if(mBaiduAlbumUtils.getAccountInfo() == null) {
//			//Log.i("SQF_LOG", "XCloudAutoUploadService::login Baidu...");
//			mBaiduAlbumUtils.setBaiduinterface(this);
//			String token = mAccountHelper.user_id;
//			if(AlbumConfig.IUNI_TEST) {
//				token = AlbumConfig.IUNI_TEST_TOKEN;
//			}
//			mBaiduAlbumUtils.loginBaidu(token, false);
//		} else {
//			MyLog.i2("SQF_LOG", "XCloudAutoUploadService::no need to login Baidu... execute AutoLoadTask     new AutoLoadTask().execute()");
//			new AutoLoadTask().execute();
//		}
//		return;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		int ret = START_NOT_STICKY;
		boolean isFromBroadcast = intent.getBooleanExtra(XCloudAutoUploadBroadcastReceiver.XCLOUD_AUTO_UPLOAD_FROM_BROADCAST, false);
		if (!isFromBroadcast) {
			return ret;
		} 
		if(mIsAutoUploading) return ret;
		
		//wenyongzhe 2015.12.8 bind_account
		if(!PrefUtil.getBoolean(mContext, "ACCOUNT_BIND", false)){
			return ret;
		}
				
		if(mBaiduAlbumUtils.getAccountInfo() == null ) {
			mBaiduAlbumUtils.setBaiduinterface(this);
			String token = mAccountHelper.user_id;
			if(AlbumConfig.IUNI_TEST) {
				token = AlbumConfig.IUNI_TEST_TOKEN;
			}
			mBaiduAlbumUtils.loginBaidu(token, false);
		} else {
			//MyLog.i2("SQF_LOG", "XCloudAutoUploadService::no need to login Baidu... execute AutoLoadTask     new AutoLoadTask().execute()");
			
			//wenyongzhe 2015.10.12 start
			if( ! NetworkUtil.checkWifiNetwork(mContext)) {
				return ret;
			}
			//wenyongzhe 2015.10.12 end
			
			new AutoLoadTask().execute();
		}
		return ret;
		
		
	}
	
	public void uploadToAlbum(ArrayList<String> localFilePaths,
			CommonFileInfo remoteAlbum) {
		//MyLog.i2("SQF_LOG", "XCloudAutoUploadService::uploadToAlbum");
		try {
			mInfos.clear();
			for (String filePath : localFilePaths) {
				/*
				Log.i("SQF_LOG", "XCloudAutoUploadService:: uploadToAlbum ---> UPLOAD ----> :" + filePath
						+ " " + remoteAlbum.path.toString() + File.separator
						+ getFileNameFromPath(filePath));
						*/
				String fileName = getFileNameFromPath(filePath);
				String target = remoteAlbum.path.toString() + File.separator + fileName;
				FileUpDownloadInfo info = new FileUpDownloadInfo(filePath,
						target,
						FileUpDownloadInfo.TYPE_UPLOAD,
						FileUpDownloadInfo.OVER_WRITE);
				mInfos.add(info);
				
				FileTaskStatusBean bean = new FileTaskStatusBean();
				bean.setSource(filePath);
				bean.setType(FileTaskStatusBean.TYPE_TASK_UPLOAD);
				bean.setTarget(target);
				bean.setFileName(fileName);
				bean.setStatusTaskCode(FileTaskStatusBean.STATE_TASK_CREATE);
				FakeTaskManager.getInstance().addUploadFakeBeans(bean);
				//XCloudTaskListenerManager.getInstance(mContext).updateList(bean, false);
			}
			mHandler.sendEmptyMessage(MSG_UPLOAD);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getFileNameFromPath(String filePath) {
		if (TextUtils.isEmpty(filePath)) {
			Log.e("SQF_LOG", "error occurs in getFileNameFromPath @1");
			return "";
		}
		int i = filePath.lastIndexOf("/");
		if (i != -1) {
			String fileName = filePath.substring(i + 1);
			return fileName;
		}
		Log.e("SQF_LOG", "error occurs in getFileNameFromPath @2");
		return "";
	}
	
	@Override
	public void baiduPhotoList(List<CommonFileInfo> list, boolean isDirPath, CommonFileInfo info) {
		return;
	}
	
	@Override
	public void loginComplete(boolean success) {
		if(success) {
			//MyLog.i2("SQF_LOG", "XCloudAutoUploadService::login Baidu...SUCCESS");
			mHandler.sendEmptyMessage(MSG_JUDGE_GET_PHOTO_TASK_LIST_OR_NOT);
		}
	}
	
	public void startUploading() {
		if( ! NetworkUtil.checkWifiNetwork(mContext)) {
			return;
		}
		if(mIsAutoUploading) return;
		//MyLog.i2("SQF_LOG", "XCloudAutoUploadService::startUploading new AutoLoadTask().execute()");
		new AutoLoadTask().execute();
	}
	
	@Override
	public void getPhotoTaskListFinished() {
		//Log.i("SQF_LOG", "XCloudAutoUploadService::getPhotoTaskListFinished ----");
		startUploading();
	}
	
    private IAlbumTaskListener mAlbumTaskListener = new IAlbumTaskListener() {

		@Override
		public void onGetTaskStatus(FileTaskStatusBean bean) {
			// TODO Auto-generated method stub
			//Log.i("SQF_LOG", "AbstractGalleryActivity::onGetTaskStatus --> " + bean.getSource() + " " + bean.getTarget());
			UploadTaskListManager manager = ((GalleryAppImpl)getApplication()).getUploadTaskListManager();
			manager.updateFileUploadTaskInfo(bean);
			//updateUploadProgress(bean);
			//updateNewUploadRedDot(bean);
		}

		@Override
		public long progressInterval() {
			// TODO Auto-generated method stub
			return 200;
		}

		@Override
		public void onGetTaskListFinished(
				List<FileTaskStatusBean> fileTaskStatusBeanList) {
			// TODO Auto-generated method stub
			
		}
    	
    };
}
