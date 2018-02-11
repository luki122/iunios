package com.android.aurora;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;


//add by JXH 2014-7-10 begin
import android.os.storage.IMountService;
import android.os.ServiceManager;

//add by JXH 2014-7-10 end

/**
 * 扫描服务
 * 
 * @author hujianwei
 * 
 * 
 */
public class AuroraMediaScannerService extends Service implements Runnable {

	private static int m_ExtHandleCount = 1;
	private static final String TAG = "AuroraMediaScannerService";
	private volatile Looper mServiceLooper;
	private PowerManager.WakeLock mWakeLock;
	private volatile AuroraServiceHandler mServiceHandler;

	private String[] mExternalStoragePaths;

	// add by JXH 2014-7-8 begin
	private List<String> scanPaths = new ArrayList<String>();
	// add by JXH 2014-7-8 end

	// add by JXh 2014-7-10 begin
	private List<String> ext = new ArrayList<String>();
	// add by JXh 2014-7-10 end

	// add by JXh 2014-7-10 begin 添加判断存储器可用方法
	private static IMountService mountService = IMountService.Stub
			.asInterface(ServiceManager.getService("mount"));

	public static boolean sdIsMounted(String mount) {
		try {
			if (mountService.getVolumeState(mount).equals(
					android.os.Environment.MEDIA_MOUNTED)) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	// add by JXH 2014-7-10 end

	@Override
	public void onCreate() {
		PowerManager mPowerManger = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = mPowerManger.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				TAG);
		StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
		mExternalStoragePaths = storageManager.getVolumePaths();
		for (String exPath : mExternalStoragePaths) {
			if (sdIsMounted(exPath)) {
				ext.add(exPath);
			}
		}
		mExternalStoragePaths = new String[ext.size()];
		mExternalStoragePaths = ext.toArray(mExternalStoragePaths);
		Thread mThead = new Thread(null, this, "AuroraMediaScannerService");
		mThead.start();
	}

	/**
	 * 扫描工作子线程
	 * 
	 * @author root
	 * 
	 */
	private final class AuroraServiceHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {

			Bundle mBundle = (Bundle) msg.obj;
			final String sScanType = mBundle.getString("scantype");

//			AuroraLog.dLog(TAG, "sScanType:" + sScanType);
//			AuroraLog.eLog(TAG, "scanPaths.size==" + scanPaths.size());
			try {
				if (sScanType != null) {
					// 扫描文件
					if (sScanType.equals(AuroraObserverUtils.ACTION_FILE_SCAN)) {

						final String sFilePath = mBundle.getString("filepath");
						if (sFilePath != null) {
							IBinder binder = mBundle.getIBinder("listener");
							IAuroraMediaScannerListener listener = (binder == null ? null
									: IAuroraMediaScannerListener.Stub
											.asInterface(binder));
							Uri uri = null;
							try {
								uri = auroraScanFile(sFilePath,
										mBundle.getString("mimetype"));
								// add by JXH 2014-7-8 begin
								scanPaths.remove(sFilePath);
								// add by JXH 2014-7-8 end
							} catch (Exception e) {
								Log.e(TAG, "Exception scanning file", e);
							}
							if (listener != null) {
								listener.auroraScanCompleted(sFilePath, uri);
							}
						}
					}
					// 扫描指定的目录
					else if (sScanType
							.equals(AuroraObserverUtils.ACTION_DIR_SCAN)) {

						final String sDirPath = mBundle.getString("dirpath");
						if (sDirPath != null) {
							String[] mDirectories = sDirPath.split(",");

							if (mDirectories != null) {
								auroraScanDirectory(mDirectories);
								// add by JXH 2014-7-8 begin
								scanPaths.remove(sDirPath);
								// add by JHX 2014-7-8 end
							}
						}

					}
					// 扫描根目录只扫描外部存储卷
					else if (sScanType
							.equals(AuroraObserverUtils.ACTION_EXT_SCAN)) {

//						AuroraLog.vLog(TAG,
//								" scan external start!");
						m_ExtHandleCount++;
						String[] directories = mExternalStoragePaths;

						if (directories != null && directories.length > 0) {
							auroraScanDirectory(directories);
							// add by Jxh 2014-9-1 begin
							scanPaths
									.remove(AuroraObserverUtils.ACTION_EXT_SCAN);
							// add by Jxh 2014-9-1 end
						}

					}
				}

			} catch (Exception e) {
				Log.e(TAG, "Exception in handleMessage", e);
			}

			stopSelf(msg.arg1);
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		while (mServiceHandler == null) {
			synchronized (this) {
				try {
					wait(100);
				} catch (InterruptedException e) {
				}
			}
		}

		if (intent == null) {
			Log.e(TAG, "Intent is null in onStartCommand: ",
					new NullPointerException());
			return Service.START_NOT_STICKY;
		}

		Message mMsg = mServiceHandler.obtainMessage();
		mMsg.arg1 = startId;
		mMsg.obj = intent.getExtras();

		Bundle mBundle = (Bundle) mMsg.obj;
		final String sScanType = mBundle.getString("scantype");
		// modify by JXH 2014-7-8 begin
		String scanPath = mBundle.getString("filepath");
		if (TextUtils.isEmpty(scanPath)) {
			scanPath = mBundle.getString("dirpath");
		}
		if (!TextUtils.isEmpty(scanPath)) {
			if (!scanPaths.contains(scanPath)) {
				scanPaths.add(scanPath);
				if (!sScanType.equals(AuroraObserverUtils.ACTION_EXT_SCAN)) {
					mServiceHandler.sendMessage(mMsg);
				}
			}
		}
		// Log.e(TAG, "scanPath==" + scanPath + " scanPaths==" +
		// scanPaths.size());
		// modify by JXH 2014-7-8 end
		if (sScanType.equals(AuroraObserverUtils.ACTION_EXT_SCAN)) {
			// modify by Jxh 2014-9-1 begin
			if (!scanPaths.contains(AuroraObserverUtils.ACTION_EXT_SCAN)) {
				scanPaths.add(AuroraObserverUtils.ACTION_EXT_SCAN);
				AuroraLog.dLog(TAG, "m_ExtHandleCount:" + m_ExtHandleCount);
				if (m_ExtHandleCount > 0) {
					m_ExtHandleCount--;
					mServiceHandler.sendMessage(mMsg);
				}
			}
			// modify by Jxh 2014-9-1 end
		}

		return Service.START_REDELIVER_INTENT;
	}

	@Override
	public void onDestroy() {
		// Make sure thread has started before telling it to quit.
		while (mServiceLooper == null) {
			synchronized (this) {
				try {
					wait(100);
				} catch (InterruptedException e) {
				}
			}
		}
		mServiceLooper.quit();
	}

	@Override
	public void run() {

		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND
				+ Process.THREAD_PRIORITY_LESS_FAVORABLE);
		Looper.prepare();

		mServiceLooper = Looper.myLooper();
		mServiceHandler = new AuroraServiceHandler();

		Looper.loop();
	}

	/**
	 * 扫描目录
	 * 
	 * @param directories
	 */
	private void auroraScanDirectory(String[] directories) {

		Uri mUri = Uri.parse("file://" + directories[0]);
		mWakeLock.acquire();

		long lStartTime = 0;
		long lEndTime = 0;
		try {
			ContentValues values = new ContentValues();
			values.put(MediaStore.MEDIA_SCANNER_VOLUME, "external");
			Uri scanUri = getContentResolver().insert(
					MediaStore.getMediaScannerUri(), values);

			sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_STARTED, mUri));

//			AuroraLog.vLog(TAG, ">>>start to scan!");
			lStartTime = System.currentTimeMillis();
			try {
				AuroraMediaScanner mScaner = auroraCreateMediaSacanner();
				mScaner.auroraScannerDirectories(directories);

			} catch (Exception e) {
				Log.e(TAG,
						"exception in AuroraMediaScannerService auroraScanDirectory",
						e);
			}

			getContentResolver().delete(scanUri, null, null);

		} catch (Exception e) {
			Log.e(TAG,"auroraScanDirectory  exception:"+e.getLocalizedMessage());
		} finally {
			lEndTime = System.currentTimeMillis();
//			AuroraLog.vLog(TAG, ">>scanner done!");
//			AuroraLog.vLog(TAG, ">>scanner time: " + (lEndTime - lStartTime)
//					+ "ms");
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_FINISHED, mUri));
			mWakeLock.release();
		}
	}

	/**
	 * 根据文件路径扫描单个文件
	 * 
	 * @param path
	 * @param mimeType
	 * @return
	 */
	private Uri auroraScanFile(String path, String mimeType) {
		String volumeName = "external";
		AuroraMediaScanner mScanner = auroraCreateMediaSacanner();
		try {
			String canonicalPath = new File(path).getCanonicalPath();
			return mScanner.auroraScanSingleFile(canonicalPath, volumeName,
					mimeType);
		} catch (Exception e) {
			Log.e(TAG, "bad path " + path + " in auroraScanFile()", e);
			return null;
		}
	}

	/**
	 * 创建扫描器设置本地言语
	 * 
	 * @return
	 */
	private AuroraMediaScanner auroraCreateMediaSacanner() {
		AuroraMediaScanner mScanner = new AuroraMediaScanner(this);

		Locale locale = getResources().getConfiguration().locale;
		if (locale != null) {
			String language = locale.getLanguage();
			String country = locale.getCountry();

			if (language != null) {
				if (country != null) {
					mScanner.auroraSetLocale(language + "_" + country);
				} else {
					mScanner.auroraSetLocale(language);
				}
			}
		}

		return mScanner;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO 自动生成的方法存根
		return mBinder;
	}

	private final IAuroraMediaScannerService.Stub mBinder = new IAuroraMediaScannerService.Stub() {
		public void auroraRequestScanFile(String path, String mimeType,
				IAuroraMediaScannerListener listener) {
			if (false) {
				AuroraLog.dLog(TAG, "IAuroraMediaScannerService.scanFile: "
						+ path + " mimeType: " + mimeType);
			}
			Bundle args = new Bundle();
			args.putString("filepath", path);
			args.putString("mimetype", mimeType);
			if (listener != null) {
				args.putIBinder("listener", listener.asBinder());
			}

			startService(new Intent(AuroraMediaScannerService.this,
					AuroraMediaScannerService.class).putExtras(args));
		}

		public void auroraScanFile(String path, String mimeType) {
			auroraRequestScanFile(path, mimeType, null);
		}
	};

}
