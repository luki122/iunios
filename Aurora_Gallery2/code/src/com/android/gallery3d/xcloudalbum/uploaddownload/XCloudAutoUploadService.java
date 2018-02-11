package com.android.gallery3d.xcloudalbum.uploaddownload;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.app.GalleryAppImpl;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.FilterDeleteSet;
import com.android.gallery3d.data.LocalImage;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.local.tools.AuroraAlbumsFilenameFilter;
import com.android.gallery3d.local.tools.GalleryLocalUtils;
import com.android.gallery3d.local.widget.MediaFileInfo;
import com.android.gallery3d.setting.SettingsActivity;
import com.android.gallery3d.setting.tools.SettingLocalUtils;
import com.android.gallery3d.util.MediaSetUtils;
import com.android.gallery3d.util.NetworkUtil;
import com.android.gallery3d.util.PrefUtil;
import com.android.gallery3d.xcloudalbum.account.AccountHelper;
import com.android.gallery3d.xcloudalbum.inter.IBaiduinterface;
import com.android.gallery3d.xcloudalbum.tools.BaiduAlbumUtils;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.android.gallery3d.xcloudalbum.tools.ToastUtils;
import com.baidu.xcloud.pluginAlbum.AlbumConfig;
import com.baidu.xcloud.pluginAlbum.IAlbumTaskListener;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;
import com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean;
import com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo;
import com.android.gallery3d.R;

public class XCloudAutoUploadService extends Service implements IBaiduinterface, XCloudTaskListenerManager.GetPhotoTaskListFinishListener {

	public class AutoLoadTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			// mMediaSet.getMediaItem();
			mIsAutoUploading = true;
			mUploadFileEntityList.clear();	
			mMediaSet = (FilterDeleteSet) mDataManager.getMediaSet(mFilterDeleteSetPath);
			mMediaItemsCount = mMediaSet.getMediaItemCount();
			mMediaItems = mMediaSet.getMediaItem(0, mMediaItemsCount);
			//wenyongzhe2016.3.17
			for (MediaItem item : mMediaItems) {
				if (!(item instanceof LocalImage)) {
					continue;
				}
				LocalImage localImage = (LocalImage) item;
				if (localImage.fileSize == 0) {
					continue;
				}
				UploadFileEntity  mUploadFileEntity = new UploadFileEntity();
				mUploadFileEntity.filePath = localImage.filePath;
				mUploadFileEntity.dateTakenInMs = localImage.dateTakenInMs;
				mUploadFileEntityList.add(mUploadFileEntity);
			}
			 
			//wenyongzhe2016.3.11
			getUploadPhotoList();
			
			Collections.sort(mUploadFileEntityList, new SortByDate());	//wenyongzhe2016.3.17
			
			// Log.i("SQF_LOG",
			// "XCloudAutoUploadService --->  auto upload: mMediaItems.size() :"
			// + mMediaItems.size());
			long lastAutoUploadTime = PrefUtil.getLong(mContext, PREF_KEY_LAST_AUTO_UPLOAD_TIME, -1);
			long autoUploadTimeToRecord = -1;
			ArrayList<String> filePaths = new ArrayList<String>();
			for (int i = mUploadFileEntityList.size() - 1; i >= 0; i--) {
//				filePaths.clear();
				UploadFileEntity localImage = mUploadFileEntityList.get(i);
				//wenyongzhe2016.3.17 disable
//				if (!(item instanceof LocalImage)) {
//					continue;
//				}
//				LocalImage localImage = (LocalImage) item;
//				if (localImage.fileSize == 0) {
//					continue;
//				}
				
				// wenyongzhe delet new_ui 2016.1.21 start
				// if(XCloudUploadFilter.postfixShouldBeFiletered(localImage.filePath)
				// ||
				// XCloudUploadFilter.filePathShouldBeFiltered(localImage.filePath))
				// {
				// continue;
				// }
				// wenyongzhe delet new_ui 2016.1.21 end
				if (localImage.dateTakenInMs > lastAutoUploadTime) {// 判断上一次上传时间与照片拍摄时间关系
					++mCurrentAddedUploadNum;
					filePaths.add(localImage.filePath);
					// autoUploadTimeToRecord = localImage.dateTakenInMs -
					// 1;//there may be some photos has same dateTakenInMs, so
					// we minus 1 here.
					// Log.i("SQF_LOG",
					// "XCloudAutoUploadService --->  auto upload: " +
					// mCurrentAddedUploadNum + " "+ localImage.filePath +
					// " time:" + localImage.dateTakenInMs);
					if (mCurrentAddedUploadNum >= MAX_UPLOAD_NUM) {
						autoUploadTimeToRecord = localImage.dateTakenInMs - 1;
						break;
					} else {
						autoUploadTimeToRecord = localImage.dateTakenInMs;
					}
				}
					
				
			
//				CommonFileInfo remoteTargetAlbum = new CommonFileInfo();
				// wenyongzhe new_ui 2016.1.21 start
				// SettingMediaFileOperationUtil operationUtil
				// =SettingMediaFileOperationUtil.getMediaFileOperationUtil(mContext);


				// remoteTargetAlbum.path = AlbumConfig.REMOTEPATH + "相册";
				// String str =
				// localImage.filePath.substring(localImage.filePath.lastIndexOf("/")
				// + 1,localImage.filePath.length());
				// wenyongzhe new_ui 2016.1.21 end
			}
			
			if(analyzeFilePathList(filePaths)){
			}else{
				return null;
			}
			
			// PrefUtil.setLong(mContext, PREF_KEY_LAST_AUTO_UPLOAD_TIME,
			// System.currentTimeMillis());
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

	//wenyongzhe2016.3.17
	class SortByDate implements Comparator {
		public int compare(Object o1, Object o2) {
			UploadFileEntity s1 = (UploadFileEntity) o1;
			UploadFileEntity s2 = (UploadFileEntity) o2;
			return (s1.dateTakenInMs == s2.dateTakenInMs ? 0 : (s1.dateTakenInMs > s2.dateTakenInMs ? -1 : 1));  
		}
	}
	
	//wenyongzhe2016.3.17
	private class UploadFileEntity{
		public String filePath = "";
		public long dateTakenInMs = -1;
	}
	
	//wenyongzhe2016.3.11
	private boolean analyzeFilePathList(ArrayList<String> filePaths){
		ArrayList<String> mFilePaths = new ArrayList<>();
		ArrayList<CommonFileInfo> remoteTargetAlbum = new ArrayList<CommonFileInfo>();
		List<String> noShowPaths = SettingLocalUtils.doParseXml(mContext);
		for (int k = 0; k < noShowPaths.size(); k++) {
			for (int i = 0; i < filePaths.size(); i++) {
				String path = noShowPaths.get(k);
				String pathSub = path.subSequence(path.lastIndexOf("/") + 1, path.length()).toString();
				String localImagePath = filePaths.get(i).subSequence(0,  filePaths.get(i).lastIndexOf("/")).toString();
				String localImagePathSub = localImagePath.substring(localImagePath.lastIndexOf("/") + 1, localImagePath.length());
				if (localImagePathSub.equals(pathSub)) {
					CommonFileInfo mCommonFileInfo = new CommonFileInfo();
					mCommonFileInfo.path = AlbumConfig.REMOTEPATH + localImagePathSub;
					mCommonFileInfo.isDir = true;
					remoteTargetAlbum.add(mCommonFileInfo);
					mFilePaths.add( filePaths.get(i));
				}
				if( !XCloudAutoUploadBroadcastReceiver.wifiAndCharging){
					new Thread(new Runnable() {    
						public void run() {    
							Looper.prepare();    
							Toast.makeText(getBaseContext(),  R.string.aurora_auto_updata_stop_toash, Toast.LENGTH_LONG).show();  
							Looper.loop();    
						}    
					}).start();    
					return false;
				}
			}
		}
		if(mFilePaths.size()>0){
			uploadToAlbumList(mFilePaths, remoteTargetAlbum);
			return true;
		}
		return false;
	}
	protected List<String> allNoShowPaths = new ArrayList<String>();
	private ArrayList<String> getUploadPhotoList(){
			ArrayList<String> mediaFileInfos = new ArrayList<String>();
			List<String> noShowPaths = GalleryLocalUtils.doParseXml(this);
			allNoShowPaths.clear();
			allNoShowPaths.addAll(noShowPaths);
			
			File file = new File(SettingsActivity.dcimPath);
			File fileScreenShots  = new File(SettingsActivity.screenShotsPath);//wenyongzhe 2016.2.27 
			//File[] fs = file.listFiles(AuroraFilenameFilter.getInstance());
			File[] fs = file.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					if(pathname.isHidden()||pathname.isFile()){
						return false;
					}
					return true;
				}
			});
			
			if(fileScreenShots.exists() && !fileScreenShots.isHidden() && !fileScreenShots.isFile()){
				MediaFileInfo info = new MediaFileInfo();
				File[] fileFs = fileScreenShots.listFiles(AuroraAlbumsFilenameFilter.getInstance());
//				info.filePath = fileScreenShots.getAbsolutePath();
//				info.IsDir = fileScreenShots.isDirectory();
//				info.createDate = fileScreenShots.lastModified();
				if (fileFs != null&&fileFs.length>0) {
					List<File> fileList = Arrays.asList(fileFs);
					//wenyongzhe2016.3.17
					for(File mFile : fileList){
						UploadFileEntity mUploadFileEntity = new UploadFileEntity();
						mUploadFileEntity.filePath = mFile.getAbsolutePath();
						mUploadFileEntity.dateTakenInMs = mFile.lastModified();
						mUploadFileEntityList.add(mUploadFileEntity);
//						mediaFileInfos.add(mFile.getAbsolutePath());
					}
//					info.Count = fileList.size();
//					Collections.sort(fileList, new FolderComparator());
//					info.filePath = fileList.get(0).getAbsolutePath();
//					info.firstPhotoPath = fileScreenShots.getAbsolutePath();
//					info.IsDir = false;
//					info.createDate = fileList.get(0).lastModified();
				}
//				else{
//					info.firstPhotoPath = fileScreenShots.getAbsolutePath();
//				}
//				info.fileName = fileScreenShots.getName();
//				if( !allNoShowPaths.contains(info.firstPhotoPath)){
//					mediaFileInfos.add(info.filePath);
//				}
			}
			return mediaFileInfos;
	}
	private class FolderComparator implements Comparator<File> {

		@Override
		public int compare(File lhs, File rhs) {
			return longToCompareInt(rhs.lastModified() - lhs.lastModified());
		}
	}
	private int longToCompareInt(long result) {
		return result > 0 ? 1 : (result < 0 ? -1 : 0);
	}
	
	// public static String subString(String text, int length, String endWith) {
	// int textLength = text.length();
	// int byteLength = 0;
	// StringBuffer returnStr = new StringBuffer();
	// for(int i = 0; i<textLength && byteLength < length*2; i++){
	// String str_i = text.substring(i, i+1);
	// if(str_i.getBytes().length == 1){//英文
	// byteLength++;
	// }else{//中文
	// byteLength += 2 ;
	// }
	// returnStr.append(str_i);
	// }
	// try {
	// if(byteLength<text.getBytes("GBK").length){//getBytes("GBK")每个汉字长2，getBytes("UTF-8")每个汉字长度为3
	// returnStr.append(endWith);
	// }
	// } catch (UnsupportedEncodingException e) {
	// e.printStackTrace();
	// }
	// return returnStr.toString();
	// }

	private static final String PREF_KEY_LAST_AUTO_UPLOAD_TIME = "PREF_KEY_LAST_AUTO_UPLOAD_TIME";

	public static final int MAX_UPLOAD_NUM = 300;
	private int mCurrentAddedUploadNum = 0;
	// private ArrayList<FileUpDownloadInfo> mInfos = new
	// ArrayList<FileUpDownloadInfo>();

	private String mDefultPath = Path.fromString("/local/allsets/" + MediaSetUtils.CAMERA_BUCKET_ID).toString();
	private String mFilterDeleteSetPath = "/filter/delete/{" + mDefultPath + "}";
	private MediaSet mMediaSet;
	private Context mContext;
	private DataManager mDataManager;
	private ArrayList<MediaItem> mMediaItems = new ArrayList<MediaItem>();
	private ArrayList<UploadFileEntity> mUploadFileEntityList = new ArrayList<UploadFileEntity>();	//wenyongzhe2016.3.17
	private int mMediaItemsCount;
	private BaiduAlbumUtils mBaiduAlbumUtils;
	private volatile boolean mIsAutoUploading;
	private ArrayList<MediaFileInfo> mMediaFileInfo = new ArrayList<MediaFileInfo>();

	protected AccountHelper mAccountHelper;
	private static final int MSG_UPLOAD = 100;
	private static final int MSG_JUDGE_GET_PHOTO_TASK_LIST_OR_NOT = 101;

	private boolean isFromBroadcast = false;
	private static final String TAG = "XCloudAutoUploadService";

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
			switch (msg.what) {
			case MSG_UPLOAD:

				// wenyongzhe delete new_ui 2016.1.21
				// Log.i("SQF_LOG", "XCloudAutoUploadService::MSG_UPLOAD");
				// if( ! mInfos.isEmpty()) {
				// GalleryAppImpl app = (GalleryAppImpl)getApplication();
				// UploadTaskListManager manager =
				// app.getUploadTaskListManager();
				// manager.addTaskList(mInfos);
				// mBaiduAlbumUtils.uploadToAlbum(mInfos);
				// }

				break;
			case MSG_JUDGE_GET_PHOTO_TASK_LIST_OR_NOT:
				XCloudTaskListenerManager manager = XCloudTaskListenerManager.getInstance(mContext);
				manager.registerListener(mAlbumTaskListener);
				if (!manager.isPhotoTaskListGot()) {
					// Log.i("SQF_LOG",
					// "XCloudAutoUploadService:: Photo Task List Not Got");
					manager.sendGetPhotoTaskListDelayed(XCloudAutoUploadService.this);// SQF
																						// ADDED
																						// ON
																						// 2015.6.1
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
		LogUtil.d(TAG, "--------------onCreate");
		// Log.i("SQF_LOG", "XCloudAutoUploadService::onCreate");

		mContext = getBaseContext();
		mBaiduAlbumUtils = BaiduAlbumUtils.getInstance(mContext);
		GalleryAppImpl app = (GalleryAppImpl) getApplication();
		mDataManager = app.getDataManager();

		mAccountHelper = new AccountHelper(this);
		mAccountHelper.registerAccountContentResolver();
		mAccountHelper.update();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		LogUtil.d(TAG, "--------------onDestroy");
		super.onDestroy();
		stopForeground(true);
	}

	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		LogUtil.d(TAG, "--------------onStart");
		// if(intent.hasExtra(XCloudAutoUploadBroadcastReceiver.XCLOUD_AUTO_UPLOAD_FROM_BROADCAST))
		// {
		// Log.i("SQF_LOG", "HAS XCLOUD_AUTO_UPLOAD_FROM_BROADCAST ------> ");
		// }
		// Bundle bundle = intent.getExtras();
		// boolean isFromBroadcast =
		// bundle.getBoolean(XCloudAutoUploadBroadcastReceiver.XCLOUD_AUTO_UPLOAD_FROM_BROADCAST);
		// if( ! isFromBroadcast) {
		// Log.i("SQF_LOG", "NOT FROM BROADCAST ------> ");
		// return;
		// }
		// if(mIsAutoUploading) return;
		// if(mBaiduAlbumUtils.getAccountInfo() == null) {
		// //Log.i("SQF_LOG", "XCloudAutoUploadService::login Baidu...");
		// mBaiduAlbumUtils.setBaiduinterface(this);
		// String token = mAccountHelper.user_id;
		// if(AlbumConfig.IUNI_TEST) {
		// token = AlbumConfig.IUNI_TEST_TOKEN;
		// }
		// mBaiduAlbumUtils.loginBaidu(token, false);
		// } else {
		// MyLog.i2("SQF_LOG",
		// "XCloudAutoUploadService::no need to login Baidu... execute AutoLoadTask     new AutoLoadTask().execute()");
		// new AutoLoadTask().execute();
		// }
		// return;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		XCloudTaskListenerManager smanager = XCloudTaskListenerManager.getInstance(mContext);
		LogUtil.d(TAG, "--------------onStartCommand isPhotoTaskListGot:" + smanager.isPhotoTaskListGot());
		if (intent != null) {
			isFromBroadcast = intent.getBooleanExtra(XCloudAutoUploadBroadcastReceiver.XCLOUD_AUTO_UPLOAD_FROM_BROADCAST, false);
		}
		if (isFromBroadcast) {// 自动上传
			if (mIsAutoUploading)
				return START_NOT_STICKY;

			// wenyongzhe 2015.12.8 bind_account
			if (!PrefUtil.getBoolean(mContext, "ACCOUNT_BIND", false)) {
				return START_NOT_STICKY;
			}

			if (mBaiduAlbumUtils.getAccountInfo() == null) {
				mBaiduAlbumUtils.setBaiduinterface(this);
				String token = mAccountHelper.user_id;
				if (AlbumConfig.IUNI_TEST) {
					token = AlbumConfig.IUNI_TEST_TOKEN;
				}
				mBaiduAlbumUtils.loginBaidu(token, false);
			}

			// MyLog.i2("SQF_LOG",
			// "XCloudAutoUploadService::no need to login Baidu... execute AutoLoadTask     new AutoLoadTask().execute()");

			// wenyongzhe 2015.10.12 start
			if (!NetworkUtil.checkWifiNetwork(mContext)) {
				return START_NOT_STICKY;
			}
			// wenyongzhe 2015.10.12 end

			// wenyongzhe 2016.1.19 setting start
			// 得到我们的存储Preferences值的对象，然后对其进行相应操作
			SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(this);
			boolean apply_wifiChecked = shp.getBoolean("auto_upload", false);
			if (!apply_wifiChecked) {
				//return START_NOT_STICKY;// wenyongzhe 2016.4.8
			}
			// wenyongzhe 2016.1.19 setting end

			new AutoLoadTask().execute();
		} else {
			if (mBaiduAlbumUtils.getAccountInfo() == null) {
				return START_NOT_STICKY;
			}
			XCloudTaskListenerManager manager = XCloudTaskListenerManager.getInstance(mContext);
			manager.registerListener(mAlbumTaskListener);
			LogUtil.d(TAG, "----START_STICKY");
		}
		return START_NOT_STICKY;

	}

	//wenyongzhe
	public void uploadToAlbumList(ArrayList<String> localFilePaths, ArrayList<CommonFileInfo> remoteAlbumList) {
		// MyLog.i2("SQF_LOG", "XCloudAutoUploadService::uploadToAlbum");
		try {
			ArrayList<FileUpDownloadInfo> mInfos = new ArrayList<FileUpDownloadInfo>();
			mInfos.clear();
			for (int j=0;  j< localFilePaths.size(); j++) {
				String filePath = localFilePaths.get(j);
				CommonFileInfo remoteAlbum = remoteAlbumList.get(j);
				/*
				 * Log.i("SQF_LOG",
				 * "XCloudAutoUploadService:: uploadToAlbum ---> UPLOAD ----> :"
				 * + filePath + " " + remoteAlbum.path.toString() +
				 * File.separator + getFileNameFromPath(filePath));
				 */
				String fileName = getFileNameFromPath(filePath);
				String target = remoteAlbum.path.toString() + File.separator + fileName;
				FileUpDownloadInfo info = new FileUpDownloadInfo(filePath, target, FileUpDownloadInfo.TYPE_UPLOAD, FileUpDownloadInfo.OVER_WRITE);
				mInfos.add(info);

				FileTaskStatusBean bean = new FileTaskStatusBean();
				bean.setSource(filePath);
				bean.setType(FileTaskStatusBean.TYPE_TASK_UPLOAD);
				bean.setTarget(target);
				bean.setFileName(fileName);
				bean.setStatusTaskCode(FileTaskStatusBean.STATE_TASK_CREATE);
				FakeTaskManager.getInstance().addUploadFakeBeans(bean);
				// XCloudTaskListenerManager.getInstance(mContext).updateList(bean,
				// false);

			}
			GalleryAppImpl app = (GalleryAppImpl) getApplication();
			UploadTaskListManager manager = app.getUploadTaskListManager();
			manager.addTaskList(mInfos);
			mBaiduAlbumUtils.uploadToAlbum(mInfos);
			// mHandler.sendEmptyMessage(MSG_UPLOAD);
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
		if (success) {
			// MyLog.i2("SQF_LOG",
			// "XCloudAutoUploadService::login Baidu...SUCCESS");
			mHandler.sendEmptyMessage(MSG_JUDGE_GET_PHOTO_TASK_LIST_OR_NOT);
		}
	}

	public void startUploading() {
		if (!NetworkUtil.checkWifiNetwork(mContext)) {
			return;
		}
		if (mIsAutoUploading)
			return;
		// MyLog.i2("SQF_LOG",
		// "XCloudAutoUploadService::startUploading new AutoLoadTask().execute()");
		new AutoLoadTask().execute();
	}

	@Override
	public void getPhotoTaskListFinished() {
		// Log.i("SQF_LOG",
		// "XCloudAutoUploadService::getPhotoTaskListFinished ----");
		startUploading();
	}

	private static final int notificationId = 27822;

	private void updateNotification(String msg) {
		Notification.Builder builder = new Notification.Builder(this).setAutoCancel(true).setContentText(msg).setContentTitle(getString(R.string.app_name));
		Notification status = builder.build();
		status.icon = R.mipmap.ic_launcher_gallery2;
		status.contentIntent = PendingIntent.getActivity(this, 0, new Intent(), 0);
		startForeground(notificationId, status);
	}

	private IAlbumTaskListener mAlbumTaskListener = new IAlbumTaskListener() {

		@Override
		public void onGetTaskStatus(FileTaskStatusBean bean) {
			// TODO Auto-generated method stub
			// Log.i("SQF_LOG", "AbstractGalleryActivity::onGetTaskStatus --> "
			// + bean.getSource() + " " + bean.getTarget());

			if (isFromBroadcast) {
				UploadTaskListManager manager = ((GalleryAppImpl) getApplication()).getUploadTaskListManager();
				manager.updateFileUploadTaskInfo(bean);
			}
			UploadTaskListManager manager = ((GalleryAppImpl) getApplication()).getUploadTaskListManager();
			// wenyongzhe 2015.11.4 upload toash bug start
			if (manager.updateFileUploadTaskInfo(bean) == UploadTaskListManager.UPDATE_FILE_UPLOAD_TASK_OK) {
				try {
					// ToastUtils.showTast(XCloudAutoUploadService.this,R.string.aurora_upload_complete);
//					updateNotification(getString(R.string.aurora_upload_complete));//wenyongzhe 2016.1.31 disable
				} catch (Exception e) {
				}
			}
			// updateUploadProgress(bean);
			// updateNewUploadRedDot(bean);
		}

		@Override
		public long progressInterval() {
			// TODO Auto-generated method stub
			return 200;
		}

		@Override
		public void onGetTaskListFinished(List<FileTaskStatusBean> fileTaskStatusBeanList) {
			// TODO Auto-generated method stub
		}

	};

}
