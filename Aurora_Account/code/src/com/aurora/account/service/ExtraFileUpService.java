package com.aurora.account.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.codehaus.jackson.map.ObjectMapper;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;

import com.aurora.account.AccountApp;
import com.aurora.account.bean.AppConfigInfo;
import com.aurora.account.bean.UpLoadReturnDataObject;
import com.aurora.account.bean.syncDataItemObject;
import com.aurora.account.bean.syncDataObject;
import com.aurora.account.contentprovider.AccountsAdapter;
import com.aurora.account.db.ExtraFileDownloadDao;
import com.aurora.account.db.ExtraFileUploadDao;
import com.aurora.account.download.FileDownloadManage;
import com.aurora.account.download.FileDownloader;
import com.aurora.account.http.data.HttpRequestGetAccountData;
import com.aurora.account.upload.FileUploadManage;
import com.aurora.account.upload.FileUploader;
import com.aurora.account.upload.UploadUpdateListener;
import com.aurora.account.util.AccountPreferencesUtil;
import com.aurora.account.util.BooleanPreferencesUtil;
import com.aurora.account.util.CommonUtil;
import com.aurora.account.util.FileLog;
import com.aurora.account.util.Globals;
import com.aurora.account.util.Log;
import com.aurora.account.util.SystemUtils;
import com.aurora.account.util.TimeUtils;
import com.aurora.datauiapi.data.bean.BaseResponseObject;

public class ExtraFileUpService extends IntentService {

	public ExtraFileUpService() {
		super("ExtraFileUpService");
	}

	private static final String TAG = "ExtraFileUpService";

	public static final String EXTRA_DATA = "extra_data";
	public static final String OPERATION = "operation";

	public static final int OPERATION_START_UPLOAD = 100; // 开始上传
	public static final int OPERATION_START_DOWNLOAD = OPERATION_START_UPLOAD + 1; // 开始下载
	public static final int OPERATION_PAUSE_UPLOAD = OPERATION_START_DOWNLOAD + 1; // 暂停上传
	public static final int OPERATION_PAUSE_DOWNLOAD = OPERATION_PAUSE_UPLOAD + 1; // 暂停下载

	public static final int OPERATION_STOP_UPLOAD = OPERATION_PAUSE_DOWNLOAD + 1; // 停止当前模块上传
	public static final int OPERATION_STOP_DOWNLOAD = OPERATION_STOP_UPLOAD + 1; // 停止当前模块下载

	public static final int OPERATION_CONTINUE_UPLOAD = OPERATION_STOP_DOWNLOAD + 1; // 继续上传
	public static final int OPERATION_CONTINUE_DOWNLOAD = OPERATION_CONTINUE_UPLOAD + 1; // 继续下载

	public static final int OPERATION_NETWORK_CHANGE = OPERATION_CONTINUE_DOWNLOAD + 1; // 网络改变
	public static final int OPERATION_NETWORK_MOBILE_PAUSE = OPERATION_NETWORK_CHANGE + 1; // 网络改变为手机网络需要暂停情况下
	public static final int OPERATION_NETWORK_MOBILE_CONTINUE = OPERATION_NETWORK_MOBILE_PAUSE + 1; // 网络改变为手机网络需要继续情况下

	private static Context context; // 上下文对象

	private static Map<String, FileUploader> uploaders; // 正在上传的任务上传器
	private static Map<String, FileDownloader> downloaders; // 正在下载的任务下载器

	private static ExtraFileUploadDao extraFileUploadDao; // 上传数据库操作对象
	private static ExtraFileDownloadDao extraFileDownloadDao; // 下载数据库操作对象

	private static int sTotalModuleCount = -1; // 要同步的模块的数量

	private static int module_index = 0; // 同步到那个模块的标示量

	private static int index = 0; // 同步到模块的那条记录标示量

	private static int sync_type = 0; // 0--上传 1--下载

	public static boolean isCanContinue = true;

	/**
	 * 同步的状态
	 * 
	 * @author JimXia
	 *
	 * @date 2014年10月24日 下午3:35:00
	 */
	public static enum SyncStatus {
		/** 未启动 */
		SYNC_STATUS_NOT_STARTED,

		/** 正在同步 */
		SYNC_STATUS_SYNCING,

		/** 暂停 */
		SYNC_STATUS_PAUSED,

		/** 同步完成 */
		SYNC_STATUS_DONE,

		/** 同步出错 */
		SYNC_STATUS_ERROR
	}

	private static SyncStatus sSyncStatus = SyncStatus.SYNC_STATUS_NOT_STARTED;

	// 包名 uri 应用标示
	private static String m_packageName, m_uri, m_apptype;
	// 当前上传的数量
	private static int m_currentcount = 0;
	// 总的上传数量
	private static int m_uptotalcount = 0;
	// 总的上传数量
	private static int m_downtotalcount = 0;
	// 当前上传（下载）的index
	private static int m_currentIndex = 0;

	private static List<String> m_ids = new ArrayList<String>();

	// 每次需要获取的数量
	private static int m_percount = 100;
	// 多少条数据开始返回给各模块一次结果
	private static int m_count = 50;

	// 源数据
	private static syncDataObject module_obj = new syncDataObject();

	// 需要同步到客户端的数据
	private static syncDataObject sync_obj = new syncDataObject();
	// 是否丢掉此条数据
	private static boolean isDeleteOneData = false;
	private static int error_count = 0; // 同步错误数量
	// 当前的操作状态
	private static int current_status = ExtraFileUpService.OPERATION_START_UPLOAD;

	public static final String ACTION_SYNC_DONE = "com.aurora.account.SyncDone"; // 同步完成发送这个广播

	private static UploadUpdateListener updateListenerList;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "service onCreate");

		if (isNeedInitData()) {
			context = this;

			uploaders = new ConcurrentHashMap<String, FileUploader>();
			downloaders = new ConcurrentHashMap<String, FileDownloader>();

			extraFileUploadDao = new ExtraFileUploadDao(this);
			extraFileUploadDao.openDatabase();
			extraFileDownloadDao = new ExtraFileDownloadDao(this);
			extraFileDownloadDao.openDatabase();
		}

	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "service onDestroy");
	}

	/**
	 * 上传附件
	 * 
	 * @param uploader
	 */
	private void upload(FileUploader uploader) {
		// 开始上传
		if (uploader != null) {
			Log.i(TAG, "start uploader");
			uploader.uploadFile();
		}
	}

	/**
	 * 下载附件
	 * 
	 * @param downloader
	 */
	private void download(FileDownloader downloader) {
		// 开始下载
		if (downloader != null) {
			Log.i(TAG, "start downloader");
			FileLog.i(TAG, "start downloader");
			downloader.downloadFile();
		}
	}

	/**
	 * 获取是否需要重新构建Service的数据(静态变量可能会被回收)
	 * 
	 * @return
	 */
	private static boolean isNeedInitData() {
		if (extraFileUploadDao == null || uploaders == null || context == null
				|| extraFileDownloadDao == null || downloaders == null) {
			return true;
		}
		return false;
	}

	// =================================已下为public方法=================================//

	/**
	 * 刷新进度
	 * 
	 */
	public static void updateProgress() {
		Log.i(TAG, "updateProgress()");
		FileLog.i(TAG, "updateProgress()");
		if (updateListenerList != null) {
			updateListenerList.downloadProgressUpdate();
		}
	}

	/**
	 * 注册刷新监听
	 * 
	 * @param updateListener
	 */
	public static void registerUpdateListener(
			UploadUpdateListener updateListener) {
		updateListenerList = updateListener;
	}

	/**
	 * 取消刷新监听
	 * 
	 * @param updateListener
	 */
	public static void unRegisterUpdateListener(
			UploadUpdateListener updateListener) {
		if (updateListenerList != null && updateListener != null) {
			updateListenerList = null;
		}
	}

	public static ExtraFileUploadDao getExtraFileUploadDao() {
		return extraFileUploadDao;
	}

	public static ExtraFileDownloadDao getExtraFileDownloadDao() {
		return extraFileDownloadDao;
	}

	public static int getModule_index() {
		return module_index;
	}

	public static void setModule_index(int module_index) {
		ExtraFileUpService.module_index = module_index;
	}

	public static void setTotalModuleCount(int totalModuleCount) {
		sTotalModuleCount = totalModuleCount;
	}

	public static int getTotalModuleCount() {
		return sTotalModuleCount;
	}

	public static int getSync_type() {
		return sync_type;
	}

	public static void setSync_type(int sync_type) {
		ExtraFileUpService.sync_type = sync_type;
	}

	public static int getError_count() {
		return error_count;
	}

	public static void setError_count(int error_count) {
		ExtraFileUpService.error_count = error_count;
	}

	public static String getM_packageName() {
		return m_packageName;
	}

	public static void setM_packageName(String m_packageName) {
		ExtraFileUpService.m_packageName = m_packageName;
	}

	public static String getM_apptype() {
		return m_apptype;
	}

	public static void setM_apptype(String m_apptype) {
		ExtraFileUpService.m_apptype = m_apptype;
	}

	public static int getM_currentcount() {
		return m_currentcount;
	}

	public static void setM_currentcount(int m_currentcount) {
		ExtraFileUpService.m_currentcount = m_currentcount;
	}

	public static int getM_currentIndex() {
		return m_currentIndex;
	}

	public static void setM_currentIndex(int m_currentIndex) {
		ExtraFileUpService.m_currentIndex = m_currentIndex;
	}

	public static int getM_uptotalcount() {
		return m_uptotalcount;
	}

	public static void setM_uptotalcount(int m_totalcount) {
		ExtraFileUpService.m_uptotalcount = m_totalcount;
	}

	public static int getM_percount() {
		return m_percount;
	}

	public static void setM_percount(int m_percount) {
		ExtraFileUpService.m_percount = m_percount;
	}

	public static int getM_downtotalcount() {
		return m_downtotalcount;
	}

	public static void setM_downtotalcount(int m_downtotalcount) {
		ExtraFileUpService.m_downtotalcount = m_downtotalcount;
		Log.i(TAG, "zhangwei the download total_count=" + m_downtotalcount);
		FileLog.i(TAG, "zhangwei the download total_count=" + m_downtotalcount);
	}

	public static List<String> getM_ids() {
		return m_ids;
	}

	public static void setM_ids(List<String> m_ids) {
		ExtraFileUpService.m_ids = m_ids;
	}

	public static String getM_uri() {
		return m_uri;
	}

	public static void setM_uri(String m_uri) {
		ExtraFileUpService.m_uri = m_uri;
	}

	public static Map<String, FileUploader> getUploaders() {
		if (null == uploaders) {
			uploaders = new ConcurrentHashMap<String, FileUploader>();
			if (AccountApp.getInstance().getApplicationContext() != null) {
				Intent i = new Intent(AccountApp.getInstance()
						.getApplicationContext(), ExtraFileUpService.class);
				AccountApp.getInstance().getApplicationContext()
						.startService(i);
			}
		}
		return uploaders;
	}

	public static Map<String, FileDownloader> getDownloaders() {
		if (null == downloaders) {
			downloaders = new ConcurrentHashMap<String, FileDownloader>();
			if (AccountApp.getInstance().getApplicationContext() != null) {
				Intent i = new Intent(AccountApp.getInstance()
						.getApplicationContext(), ExtraFileUpService.class);
				AccountApp.getInstance().getApplicationContext()
						.startService(i);
			}
		}
		return downloaders;
	}

	public static syncDataObject getModule_obj() {
		return module_obj;
	}

	public static void setModule_obj(syncDataObject module_obj) {
		ExtraFileUpService.module_obj = module_obj;
	}

	public static syncDataObject getSync_obj() {
		return sync_obj;
	}

	public static void setSync_obj(syncDataObject sync_obj) {
		ExtraFileUpService.sync_obj = sync_obj;
	}

	public static int getCurrent_status() {
		return current_status;
	}

	public static void setCurrent_status(int current_status) {
		ExtraFileUpService.current_status = current_status;
	}

	public static int getIndex() {
		return index;
	}

	public static void setIndex(int index) {
		ExtraFileUpService.index = index;
	}

	/**
	 * 开始上传请求方法
	 * 
	 * @param context
	 * @param obj
	 */
	public static void startUpload(Context context, syncDataObject obj) {
		if (ExtraFileUpService.isPaused())
			return;
		setModule_obj(obj);
		if (obj.getSycndata().size() > 0) {

			Intent startDownload = new Intent(context, ExtraFileUpService.class);
			Bundle startDownloadBundle = new Bundle();
			/*
			 * startDownloadBundle.putParcelable(ExtraFileUpService.EXTRA_DATA,
			 * m_obj);
			 */
			startDownloadBundle.putInt(ExtraFileUpService.OPERATION,
					ExtraFileUpService.OPERATION_START_UPLOAD);
			startDownload.putExtras(startDownloadBundle);
			context.startService(startDownload);

		} else {
			dotheindex();
		}
	}

	/**
	 * 开始下载请求方法
	 * 
	 * @param context
	 * @param obj
	 */
	public static void startDownload(Context context, syncDataObject obj) {
		if (ExtraFileUpService.isPaused())
			return;
		setModule_obj(obj);
		if (obj.getSycndata().size() > 0) {
			// for (syncDataItemObject m_obj : obj.getSycndata()) {
			Intent startDownload = new Intent(context, ExtraFileUpService.class);
			Bundle startDownloadBundle = new Bundle();
			/*
			 * startDownloadBundle.putParcelable( ExtraFileUpService.EXTRA_DATA,
			 * m_obj);
			 */
			startDownloadBundle.putInt(ExtraFileUpService.OPERATION,
					ExtraFileUpService.OPERATION_START_DOWNLOAD);
			startDownload.putExtras(startDownloadBundle);
			context.startService(startDownload);
			// }
		} else {
			List<AppConfigInfo> apps = SystemUtils.getAppConfigInfo(context);

			SystemUtils.updateAppSyncTime(context, apps.get(module_index)
					.getApp_packagename(), System.currentTimeMillis());

			if (!hasSyncFinished(apps.size())) {
				doNextModule();
			}
		}

	}

	/**
	 * 暂停或继续请求方法
	 * 
	 * @param context
	 * @param downloadData
	 */
	public static void pauseOperation(Context context,int type) {
		if(type == 0)
			isCanContinue = false;
		/*
		 * Intent startPause = new Intent(context, ExtraFileUpService.class);
		 * Bundle startPauseBundle = new Bundle(); if (sync_type == 0) {
		 * startPauseBundle.putInt(ExtraFileUpService.OPERATION,
		 * ExtraFileUpService.OPERATION_PAUSE_UPLOAD); } else {
		 * startPauseBundle.putInt(ExtraFileUpService.OPERATION,
		 * ExtraFileUpService.OPERATION_PAUSE_DOWNLOAD); }
		 * startPause.putExtras(startPauseBundle);
		 * context.startService(startPause);
		 */
		
		if (sync_type == 0) {
	
			 setCurrent_status(ExtraFileUpService.OPERATION_PAUSE_UPLOAD);
			 } 
		else {
			setCurrent_status(ExtraFileUpService.OPERATION_PAUSE_DOWNLOAD);
	
	}
		
		Log.i(TAG, "zhangwei the SYNC_STATUS_PAUSED");
		FileLog.i(TAG, "zhangwei the SYNC_STATUS_PAUSED");
		ExtraFileUpService.setSyncStatus(SyncStatus.SYNC_STATUS_PAUSED);

		// 取消通知栏同步动画
		CommonUtil.cancelSyncNotificaiton(context);
		FileLog.i(TAG, "  call cancelSyncNotificaiton in pauseOperation()");

		updateProgress();
	}

	/**
	 * 暂停或继续请求方法
	 * 
	 * @param context
	 * @param downloadData
	 */
	public static void continueOperation(Context context) {
		if (!isCanContinue)
			return;

		ExtraFileUpService.setSyncStatus(SyncStatus.SYNC_STATUS_SYNCING);

		// 显示通知栏同步动画
		CommonUtil.showSyncNotification(context);
		FileLog.i(TAG, "  call showSyncNotification in continueOperation()");

		updateProgress();

		if ((index == 0) && (module_obj.getSycndata().size() == 0)) {
			error_count = 0;
			ModuleDataWorker worker = new ModuleDataWorker(context, 0);
			new Thread(worker).start();
		} else {
			//for (int i = index; i < module_obj.getSycndata().size(); i++) {

				Intent startPause = new Intent(context,
						ExtraFileUpService.class);
				Bundle startPauseBundle = new Bundle();
				/*startPauseBundle.putParcelable(ExtraFileUpService.EXTRA_DATA,
						module_obj.getSycndata().get(i));*/
				if (sync_type == 0) {
					startPauseBundle.putInt(ExtraFileUpService.OPERATION,
							ExtraFileUpService.OPERATION_START_UPLOAD);
				} else {
					startPauseBundle.putInt(ExtraFileUpService.OPERATION,
							ExtraFileUpService.OPERATION_START_DOWNLOAD);
				}
				startPause.putExtras(startPauseBundle);
				context.startService(startPause);
			//}
		}
	}

	/**
	 * 开始做上传操作
	 * 
	 * @param obj
	 *            数据集合
	 */
	private void startUploadOperation(/* syncDataItemObject obj */) {
		for (; index < module_obj.getSycndata().size(); ) {

			Log.i(TAG, "zhangwei up 1");
			if (ExtraFileUpService.isPaused()) {
				isCanContinue = true;
				return;
			}
			Log.i(TAG, "zhangwei up 2");
			// 如果手动关掉当前模块 直接开始下一个模块的同步
			if ((current_status == ExtraFileUpService.OPERATION_STOP_UPLOAD)
					|| (current_status == ExtraFileUpService.OPERATION_STOP_DOWNLOAD)) {
				sync_type = 0;
				module_index++;
				m_currentcount = 0;
				m_currentIndex = 0;
				m_downtotalcount = 0;
				m_uptotalcount = 0;
				ModuleDataWorker worker = new ModuleDataWorker(context, 0);
				new Thread(worker).start();
				return;
			}

			syncDataItemObject obj = module_obj.getSycndata().get(index);
			int count = obj.getAccOjb().getAccessory().size();
			Log.i(TAG, "the accessory up count=" + count);
			FileLog.i(TAG, "the accessory up count=" + count);

			if (count > 0) {
				CountDownLatch downLatch = new CountDownLatch(count);
				isDeleteOneData = false;
				uploaders.clear();
				// 把附件上传放在队列
				for (int i = 0; i < count; i++) {
					FileUploader uploader = new FileUploader(m_packageName,
							m_uri, m_apptype, obj.getId(), obj.getAccOjb()
									.getAccessory().get(i), this, downLatch);
					uploaders.put(obj.getAccOjb().getAccessory().get(i)
							.getAccessoryid(), uploader);
				}
				Log.i(TAG,
						"zhangwei start up the time="
								+ System.currentTimeMillis());
				List<FileUploader> list = new ArrayList<FileUploader>();

				for (String key : uploaders.keySet()) {
					FileUploader uploader = uploaders.get(key);
					list.add(uploader);
				}

				for (FileUploader uploader : list) {
					if (uploader.getStatus() != FileUploader.STATUS_FINISH)
						upload(uploader);
				}
				try {
					downLatch.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				FileUploadManage.getThreadPoolExecutor().shutdownNow();
				FileUploadManage.threadPool = null;
				Log.i(TAG,
						"zhangwei start up the time1="
								+ System.currentTimeMillis());

			}
			if (ExtraFileUpService.isPaused())
			{
				FileLog.i(TAG, "zhangwei the index2=" + index);
				isCanContinue = true;
				return;
			}
			if (!isDeleteOneData) {
				if (index >= module_obj.getSycndata().size()) {
					FileLog.i(TAG, "do clear the index="
							+ module_obj.getSycndata().size());
					index = 0;
					sync_obj.getSycndata().clear();
					sync_obj.getSycndata().add(obj);
				} else {
					if (!sync_obj.getSycndata().contains(
							module_obj.getSycndata().get(index)))
						sync_obj.getSycndata().add(
								module_obj.getSycndata().get(index));
				}
			} else
				isDeleteOneData = false;

			if ((((index + 1) % m_count == 0) && (index != 0))
					|| (index == module_obj.getSycndata().size() - 1)) {
				FileLog.i(TAG, "sendUpDataObject");
				Log.i(TAG,
						"zhangwei start up the time2="
								+ System.currentTimeMillis());
				try {
					SharedPreferences mSyncTime = getSharedPreferences(
							Globals.SHARED_WIFI_SYNC, MODE_PRIVATE);

					String servertime = mSyncTime.getString(
							Globals.SHARED_SERVERTIME_SYNC_KEY, "0");
					AccountPreferencesUtil pref = AccountPreferencesUtil
							.getInstance(this);
					String result = HttpRequestGetAccountData.sendUpDataObject(
							pref.getUserID(), pref.getUserKey(), m_apptype,
							servertime, ExtraFileUpService.getSync_obj()
									.getSycndata());
					ObjectMapper mapper = new ObjectMapper();
					UpLoadReturnDataObject obj1 = mapper.readValue(result,
							UpLoadReturnDataObject.class);
					if (obj1.getCode() == BaseResponseObject.CODE_SUCCESS) {
						syncDataObject sync = new syncDataObject();
						sync.getSycndata().addAll(sync_obj.getSycndata());
						for (int i = 0; i < obj1.getRecords().size(); i++) {
							for (int j = 0; j < sync.getSycndata().size(); j++) {
								if (obj1.getRecords()
										.get(i)
										.getId()
										.equals(sync.getSycndata().get(j)
												.getId())) {

									sync.getSycndata()
											.get(j)
											.setBody(
													obj1.getRecords().get(i)
															.getBody());
									sync.getSycndata()
											.get(j)
											.setSyncid(
													obj1.getRecords().get(i)
															.getSyncid());
									sync.getSycndata()
											.get(j)
											.setResult(
													obj1.getRecords().get(i)
															.getResult());
									Log.i(TAG, "zhangwei the syncid="
											+ obj1.getRecords().get(i)
													.getSyncid());
									FileLog.i(TAG, "zhangwei the syncid="
											+ obj1.getRecords().get(i)
													.getSyncid());
									break;
								}
							}
						}
						Log.i(TAG,
								"zhangwei start up the time3="
										+ System.currentTimeMillis());
						AccountsAdapter db = new AccountsAdapter(context,
								m_packageName, m_uri);
						db.upResultUpdata(m_packageName, sync);
						Log.i(TAG,
								"zhangwei start up the time4="
										+ System.currentTimeMillis());
						ExtraFileUpService.updateProgress();
					}

					sync_obj.getSycndata().clear();
					index++;
				} catch (Exception e) {
					// index++;
					FileLog.i(TAG, "zhangwei the network ie exception");
					e.printStackTrace();
					ExtraFileUpService.pauseOperation(this,0);
				}
			} else {
				index++;
			}

			m_currentIndex++;
			if (m_currentIndex % 5 == 0) {
				ExtraFileUpService.updateProgress();
			}
			Log.i(TAG, "uploader end0");
			dotheindex();
			Log.i(TAG, "uploader end");
			FileLog.i(TAG, "uploader end");
		}
	}

	/**
	 * 开始做下载操作
	 * 
	 * @param obj
	 *            数据集合
	 */
	private void startDownloadOperation() {
		for (;index < module_obj.getSycndata().size();) {
			Log.i(TAG, "zhangwei down 1");
			FileLog.i(TAG, "zhangwei the index=" + index);
			if (ExtraFileUpService.isPaused())
			{
				FileLog.i(TAG, "zhangwei the index1=" + index);
				isCanContinue = true;
				return;
			}
			Log.i(TAG, "zhangwei down 2");
			syncDataItemObject obj = module_obj.getSycndata().get(index);
			int count = obj.getAccOjb().getAccessory().size();
			Log.i(TAG, "the accessory down count=" + count);
			FileLog.i(TAG, "the accessory down count=" + count);
			if (count > 0) {
				CountDownLatch downLatch = new CountDownLatch(count);
				isDeleteOneData = false;
				downloaders.clear();
				// 把附件上传放在队列
				for (int i = 0; i < count; i++) {
					if (obj.getAccOjb().getAccessory().get(i).getSyncid() == null) {
						Log.i(TAG, "obj syicId : " + obj.getSyncid()
								+ " accessory postion : " + i + " syncId null");
						FileLog.i(TAG, "obj syicId : " + obj.getSyncid()
								+ " accessory postion : " + i + " syncId null");
						downLatch.countDown();
						isDeleteOneData = true;
						continue;
					}
					FileDownloader downloader = new FileDownloader(
							m_packageName, m_uri, obj.getAccOjb()
									.getAccessory().get(i), null, context,
							downLatch);
					downloaders.put(obj.getSyncid(), downloader);
				}

				List<FileDownloader> list = new ArrayList<FileDownloader>();

				if (!isDeleteOneData) {
					for (String key : downloaders.keySet()) {
						FileDownloader downloader = downloaders.get(key);
						list.add(downloader);
						// 未完成的开始下载
						if (downloader.getStatus() != FileDownloader.STATUS_FINISH) {
							download(downloader);
						}
					}
					try {
						downLatch.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				FileDownloadManage.getThreadPoolExecutor().shutdownNow();
				FileDownloadManage.threadPool = null;
			}

			if (ExtraFileUpService.isPaused())
			{
				FileLog.i(TAG, "zhangwei the index2=" + index);
				isCanContinue = true;
				return;
			}
			

			if (!isDeleteOneData) {
				if (index >= module_obj.getSycndata().size()) {
					index = 0;
					sync_obj.getSycndata().add(obj);
				} else {
					sync_obj.getSycndata().add(
							module_obj.getSycndata().get(index));
				}
			} else
				isDeleteOneData = false;

			if ((((index + 1) % m_count == 0) && (index != 0))
					|| (index == module_obj.getSycndata().size() - 1)) {

				AccountsAdapter db = new AccountsAdapter(context,
						m_packageName, m_uri);
				db.upResultDowndata(m_packageName, sync_obj);

				sync_obj.getSycndata().clear();
				index++;

				ExtraFileUpService.updateProgress();

			} else {
				index++;
			}

			m_currentIndex++;
			if (m_currentIndex % 5 == 0) {
				ExtraFileUpService.updateProgress();
			}

			dotheindex();

			Log.i(TAG, "downloader end");
			FileLog.i(TAG, "downloader end");
		}
	}

	private static void dotheindex() {

		if (index < module_obj.getSycndata().size()) {
			Log.i(TAG, "uploader end1");
			
			Log.i(TAG, "the index="+index+"  the module_obj.getSycndata().size()="+module_obj.getSycndata().size());
			return;
		} else {
			FileLog.i(TAG, "do clear");
			Log.i(TAG, "uploader end2");
			module_obj.getSycndata().clear();
			m_currentcount += index;
			index = 0;
			if (sync_type == 0) {
				if (m_currentcount >= m_uptotalcount) {
					sync_type = 1;
					m_currentcount = 0;
					m_currentIndex = 0;
					error_count = 0;
					Log.i(TAG, "uploader end3");
					ModuleDataWorker worker = new ModuleDataWorker(context, 1);
					new Thread(worker).start();
				} else {
					Log.i(TAG, "uploader end4");
					ModuleDataWorker worker = new ModuleDataWorker(context, 1);
					new Thread(worker).start();
				}
			} else if (sync_type == 1) {
				if (m_currentcount >= m_downtotalcount) {
					List<AppConfigInfo> apps = SystemUtils
							.getAppConfigInfo(context);
					Log.i(TAG, "zhangwei update the switch");
					SystemUtils.updateAppSyncTime(context,
							apps.get(module_index).getApp_packagename(),
							System.currentTimeMillis());
					SystemUtils.updateAppSwitch(context, apps.get(module_index)
							.getApp_packagename(), false);
					SystemUtils.updateAppRepeat(context, apps.get(module_index)
							.getApp_packagename(), false);
					if (!hasSyncFinished(apps.size())) { // 已经完成所有模块
						// 进行下一个模块的同步
						Log.i(TAG, "sync next done");
						isCanContinue = true;
						doNextModule();
					}
				} else {
					ModuleDataWorker worker = new ModuleDataWorker(context, 1);
					new Thread(worker).start();
				}
			}

		}

	}

	private static boolean hasSyncFinished(int totalModuleCount) {
		if (module_index >= /* apps.size() - 1 */(sTotalModuleCount != -1 ? sTotalModuleCount - 1
				: totalModuleCount - 1)) { // 已经完成所有模块
			Log.i(TAG, "sync all done");
			FileLog.i(TAG, "sync all done");
			dotheFinish(module_index >= totalModuleCount - 1);

			return true;
		}

		return false;
	}

	public static void doNextModule() {
		sync_type = 0;
		module_index++;
		Log.i(TAG, "zhangwei the doNextModule module_index=" + module_index);
		m_currentcount = 0;
		m_currentIndex = 0;
		m_downtotalcount = 0;
		m_uptotalcount = 0;
		ModuleDataWorker worker = new ModuleDataWorker(context, 0);
		new Thread(worker).start();
	}

	public static void dotheFinish(boolean fullSync) {
		sSyncStatus = SyncStatus.SYNC_STATUS_DONE;

		// 取消通知栏同步动画
		CommonUtil.cancelSyncNotificaiton(context);
		FileLog.i(TAG, "  call cancelSyncNotificaiton in dotheFinish()");

		// ToastUtil.shortToast("同步完成");
		Log.i(TAG, "zhangwei the all finish");
		FileLog.i(TAG, "zhangwei the all finish");
		resetData();
		/*if ((null == extraFileUploadDao) || (null == extraFileDownloadDao))
			return;*/

		AccountPreferencesUtil pref = AccountPreferencesUtil
				.getInstance(AccountApp.getInstance());
		if (fullSync) {
			// 全同步才记录同步日期
			if ((null != extraFileUploadDao) && (null != extraFileDownloadDao))
			{
				extraFileUploadDao.deleteAll();
				extraFileDownloadDao.deleteAll();
			}
				
			BooleanPreferencesUtil.getInstance(AccountApp.getInstance())
					.setFirstTimeSync(false);
		}
		notifySyncDone();
		pref.setSyncDate(TimeUtils.getSyncDate()); // 记录同步完成时间，以便判断某天是否有同步过
		pref.setLastSyncFinishedTime(System.currentTimeMillis());

		ExtraFileUpService.updateProgress();
		// 防止完成时，最后一个模块在切换进度动画中，没有切换到状态，1秒后再刷
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				ExtraFileUpService.updateProgress();
			}
		}).start();
	}

	public static void stopCurrentModule(Context context) {

		Intent startPause = new Intent(context, ExtraFileUpService.class);
		Bundle startPauseBundle = new Bundle();
		if (sync_type == 0) {
			startPauseBundle.putInt(ExtraFileUpService.OPERATION,
					ExtraFileUpService.OPERATION_STOP_UPLOAD);
		} else {
			startPauseBundle.putInt(ExtraFileUpService.OPERATION,
					ExtraFileUpService.OPERATION_STOP_DOWNLOAD);
		}
		startPause.putExtras(startPauseBundle);
		context.startService(startPause);
	}

	static void notifySyncDone() {
		if (null != context) {
			Intent intent = new Intent(ACTION_SYNC_DONE);
			context.sendBroadcast(intent);
		}
	}

	static public void resetData() {
		module_index = 0;
		Log.i(TAG, "zhangwei the resetData module_index=" + module_index);
		index = 0;
		sync_type = 0;
		m_currentIndex = 0;
		m_currentcount = 0;
		m_uptotalcount = 0;
		m_downtotalcount = 0;
		sTotalModuleCount = -1;
		error_count = 0;
		isCanContinue = true;
		module_obj.getSycndata().clear();
		sync_obj.getSycndata().clear();
		current_status = ExtraFileUpService.OPERATION_START_UPLOAD;
		sSyncStatus = SyncStatus.SYNC_STATUS_NOT_STARTED;

		// 取消通知栏同步动画
		CommonUtil.cancelSyncNotificaiton(context);
		FileLog.i(TAG, "  call cancelSyncNotificaiton in resetData()");
	}

	/**
	 * @Title: PauseUploadOperation
	 * @Description: TODO上传暂停
	 * @param
	 * @return void
	 * @throws
	 */
	private void PauseUploadOperation() {
		Intent startDownload = new Intent(context, ExtraFileUpService.class);
		context.stopService(startDownload);
		for (String key : uploaders.keySet()) {
			FileUploader uploader = uploaders.get(key);
			uploader.pause();
		}
	}

	/**
	 * @Title: PauseUploadAcc
	 * @Description: TODO上传附件出错后这条数据丢掉
	 * @param
	 * @return void
	 * @throws
	 */
	public static void PauseUploadAcc() {
		isDeleteOneData = true;
		error_count++;
		for (String key : uploaders.keySet()) {
			FileUploader uploader = uploaders.get(key);
			uploader.pause();
		}
	}

	/**
	 * @Title: PauseUploadAcc
	 * @Description: TODO上传附件出错后这条数据丢掉
	 * @param
	 * @return void
	 * @throws
	 */
	public static void PauseDownloadAcc() {
		isDeleteOneData = true;
		for (String key : downloaders.keySet()) {
			FileDownloader downloader = downloaders.get(key);
			downloader.pause();
		}
	}

	/**
	 * @Title: PauseUploadOperation
	 * @Description: TODO上传继续
	 * @param
	 * @return void
	 * @throws
	 */
	private void ContinueUploadOperation() {
		Intent startDownload = new Intent(context, ExtraFileUpService.class);
		context.stopService(startDownload);
		for (String key : uploaders.keySet()) {
			FileUploader uploader = uploaders.get(key);
			uploader.pause();
		}
	}

	/**
	 * @Title: pauseDownloadOperation
	 * @param
	 * @return void
	 * @throws
	 */
	private void pauseDownloadOperation() {
		Intent stopDownload = new Intent(context, ExtraFileUpService.class);
		context.stopService(stopDownload);
		for (String key : downloaders.keySet()) {
			FileDownloader downloader = downloaders.get(key);
			downloader.pause();
		}
	}

	/**
	 * 处理指令
	 * 
	 * @param operation
	 */
	private void handleOperation(Bundle bundle) {
		int operation = bundle.getInt(OPERATION);
		setCurrent_status(operation);
		switch (operation) {
		// 开始上传
		case ExtraFileUpService.OPERATION_START_UPLOAD:
			/*
			 * syncDataItemObject upObj = (syncDataItemObject) bundle
			 * .getParcelable(ExtraFileUpService.EXTRA_DATA);
			 */
			startUploadOperation();
			break;
		case ExtraFileUpService.OPERATION_PAUSE_UPLOAD:
			PauseUploadOperation();
			break;
		case ExtraFileUpService.OPERATION_STOP_UPLOAD:
			PauseUploadOperation();
			break;
		case ExtraFileUpService.OPERATION_CONTINUE_UPLOAD:

			break;
		case ExtraFileUpService.OPERATION_START_DOWNLOAD:
			/*
			 * syncDataItemObject downObj = (syncDataItemObject) bundle
			 * .getParcelable(ExtraFileUpService.EXTRA_DATA);
			 */
			startDownloadOperation();
			break;
		case ExtraFileUpService.OPERATION_PAUSE_DOWNLOAD:
			pauseDownloadOperation();
			break;
		case ExtraFileUpService.OPERATION_STOP_DOWNLOAD:
			pauseDownloadOperation();
			break;
		case ExtraFileUpService.OPERATION_CONTINUE_DOWNLOAD:

			break;
		default:
			break;
		}
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// 获取要操作的数据信息
		Log.i(TAG, "onHandleIntent");
		if (intent != null) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				handleOperation(bundle);
			}
		}
	}

	public static void setSyncStatus(SyncStatus syncStatus) {
		sSyncStatus = syncStatus;
	}

	public static SyncStatus getSyncStatus() {
		return sSyncStatus;
	}

	public static boolean isSyncing() {
		return sSyncStatus == SyncStatus.SYNC_STATUS_SYNCING;
	}

	public static boolean isPaused() {
		return sSyncStatus == SyncStatus.SYNC_STATUS_PAUSED;
	}

	public static boolean canSyncNow() {
		return (sSyncStatus == SyncStatus.SYNC_STATUS_NOT_STARTED
				|| sSyncStatus == SyncStatus.SYNC_STATUS_DONE || sSyncStatus == SyncStatus.SYNC_STATUS_ERROR);
	}

}