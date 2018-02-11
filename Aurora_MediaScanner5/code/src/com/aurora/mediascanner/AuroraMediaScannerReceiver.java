package com.aurora.mediascanner;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.os.storage.StorageVolume;


import android.R.string;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.provider.MediaStore.Files;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import android.widget.Toast;

/**
 * 接受扫描广播
 * 
 * @author root
 * 
 */
public class AuroraMediaScannerReceiver extends BroadcastReceiver {

	// private static final long time = 0;
	// private static final long pathTime = 0;
	private static final long time = 10 * 60 * 1000;
	private static final long pathTime = 10 * 1000;// ;4 * 60 * 1000;

	private final static String TAG = "AuroraMediaScannerReceiver";
	private Context context;

	@Override
	public void onReceive(final Context context, Intent intent) {
		this.context = context;

		final String sAction = intent.getAction();
		LogUtil.d(TAG, "Receive BroadCast:" + sAction + "");
		// Toast.makeText(context, sAction, 0).show();

		// add by JXH 2014-7-17 MTP 模式扫描全盘 begin 验证
		// boot启动完成
		if (Intent.ACTION_BOOT_COMPLETED.equals(sAction)) {
			ShareXmlTools.setScannerTime(context, System.currentTimeMillis());
			ShareXmlTools.setBootTime(context, System.currentTimeMillis());
			ShareXmlTools.setBootScannerTag(context, false);
			return;
		} else if (UsbManager.ACTION_USB_STATE.equals(sAction)) {
			handleUsbState(context, intent);
			return;
		}
		// add by JXH 2014-7-17 MTP 模式扫描全盘 end 验证

		// add by JXH 2014-7-17 扫描拔掉的OTG begin
		if (sAction.equals(Intent.ACTION_MEDIA_EJECT)
				|| sAction.equals(Intent.ACTION_MEDIA_UNMOUNTED)
				|| sAction.equals(Intent.ACTION_MEDIA_CHECKING)
				|| sAction.equals(Intent.ACTION_MEDIA_MOUNTED)) {

			notifiedStateChanged(context);
			if (storagesStrings.size() > 1) {
				newStorage = storagesStrings.get(storagesStrings.size() - 1);
				lastStorage = storagesStrings.get(storagesStrings.size() - 1);
				ScannerApplication.setLastStorage(lastStorage);
			}

			if (sAction.equals(Intent.ACTION_MEDIA_EJECT)
					|| sAction.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
				lastStorage = ScannerApplication.getLastStorage();
				if (!TextUtils.isEmpty(lastStorage)
						&& !AuroraMediaScannerService.sdIsMounted(lastStorage)
						&& !isMediaScannerScanning(context.getContentResolver())) {// &&
					auroraScanDirectory(context, lastStorage);
				}
			} else if(sAction.equals(Intent.ACTION_MEDIA_MOUNTED)) {
				if(!TextUtils.isEmpty(newStorage)&&!isBootScan()){
					ScannerUtils.updateStatistics(context);
				}
				
			}
			return;
		}

		if (!isBootScan()
				&& !isMediaScannerScanning(context.getContentResolver())
				&& !ShareXmlTools.getBootScannerTag(context)) {
			ShareXmlTools.setBootScannerTag(context, true);
			LogUtil.d(TAG, "setBootScannerTag true");
		} else if (!ShareXmlTools.getBootScannerTag(context)) {
			// boot scan is not end
			LogUtil.d(TAG, "boot scan is not end");
			return;
		}

		// add by JXH 2014-7-17 扫描拔掉的OTG end

		if (ScannerUtils.ACTION_FILE_SCAN.equals(sAction)) {
			/*LogUtil.d(TAG, "ACTION_SCAN_FILE");
			if (isBootScan()) {
				LogUtil.d(TAG, "no scan ACTION_SCAN_DIR isBootScan");
				return;
			}*/
			final Uri mUri = intent.getData();

			if (mUri.getScheme().equals("file")) {
				auroraScanFile(context, mUri.getPath());
			}
		}
		// 扫描指定目录
		else if (ScannerUtils.ACTION_DIR_SCAN.equals(sAction)) {
			if (isBootScan()) {
				LogUtil.d(TAG, "no scan ACTION_SCAN_DIR isBootScan");
				return;
			}
			final Uri mUri = intent.getData();
			if (mUri.getScheme().equals("file")) {
				auroraScanDirectory(context, mUri.getPath());
				 
			}
		}
		// 扫描根目录
		else if (ScannerUtils.ACTION_EXT_SCAN.equals(sAction)) {
			long dbTime = ShareXmlTools.getScannerTime(context);
			long nowTime = System.currentTimeMillis();
			boolean isScan = isMediaScannerScanning(context
					.getContentResolver());
			if (nowTime - dbTime > time) {
					ShareXmlTools.setScannerTime(context, nowTime);
					LogUtil.d(TAG, "ACTION_SCAN_EXTERNAL");
					auroaraScanRoot(context);
			}
		}

	}

	private boolean isBootScan() {
		if (!isLowVersion()) {
			return false;
		}
		long dbTime = ShareXmlTools.getBootTime(context);
		long nowTime = System.currentTimeMillis();
		/*LogUtil.d("JXH", "dbTime:" + dbTime + " nowTime:" + nowTime
				+ " (nowTime - dbTime < pathTime):"
				+ (nowTime - dbTime < pathTime));*/
		if (nowTime - dbTime < pathTime) {
			return true;
		}
		return false;
	}

	/**
	 * 4.2及以下版本
	 * 
	 * @return
	 */
	public static boolean isLowVersion() {
		int version = android.os.Build.VERSION.SDK_INT;
		if (version <= 17) {
			return true;
		}
		return false;
	}

	/**
	 * 判断是MTP 或者是PTP模式扫描全盘
	 * 
	 * @param context
	 * @param intent
	 */
	private void handleUsbState(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
		boolean connected = extras.getBoolean(UsbManager.USB_CONFIGURED);
		boolean mtpEnabled = extras.getBoolean(UsbManager.USB_FUNCTION_MTP);
		boolean ptpEnabled = extras.getBoolean(UsbManager.USB_FUNCTION_PTP);
		// Start MTP service if USB is connected and either the MTP or PTP
		// function is enabled
		if (connected && (mtpEnabled || ptpEnabled)) {
			LogUtil.d(TAG, "MTP OR PTP");
			long dbTime = ShareXmlTools.getScannerTime(context);
			if (dbTime == 0) {
				LogUtil.d(TAG, "ScannerTime is 0 task not begin");
				return;
			}
			long nowTime = System.currentTimeMillis();
//			LogUtil.d(TAG, "Scan time:" + (nowTime - dbTime > time)
//					+ "  isMediaScannerScanning:"
//					+ isMediaScannerScanning(context.getContentResolver()));
			if ((nowTime - dbTime > time)) {
				if (isMediaScannerScanning(context.getContentResolver())) {
					LogUtil.d(TAG, "isMediaScannerScanning ture ");
					return;
				} else {
					LogUtil.d(TAG,
							"isMediaScannerScanning false auroaraScanRoot");
					ShareXmlTools.setScannerTime(context, nowTime);
					auroaraScanRoot(context);
				}
			}
		}
	}

	private List<String> storagesStrings = new ArrayList<String>();

	private StorageManager mStorageManager;
	private String lastStorage;
	private String newStorage;

	private void notifiedStateChanged(Context context) {
		if (mStorageManager == null) {
			mStorageManager = (StorageManager) context
					.getSystemService(Context.STORAGE_SERVICE);
		}
		StorageVolume[] storageVolume = mStorageManager.getVolumeList();
		storagesStrings.clear();
		for (int i = 0; i < storageVolume.length; i++) {
			String temp = storageVolume[i].getPath();
			if (AuroraMediaScannerService.sdIsMounted(temp)) {
				storagesStrings.add(temp);
//				LogUtil.d(TAG, "notifiedStateChanged==" + temp);
			}
		}
	}


	/**
	 * author abel
	 * 
	 * @param context
	 * @param filepath
	 *            扫描文件入口
	 */
	private void auroraScanFile(Context context, String filepath) {
		LogUtil.d(TAG, "auroraScanFile" + filepath);
		Bundle args = new Bundle();
		args.putString("filepath", filepath);
		args.putString("scantype", ScannerUtils.ACTION_FILE_SCAN);

		context.startService(new Intent(context,
				AuroraMediaScannerService.class).putExtras(args));
	}

	/**
	 * author abel
	 * 
	 * @param context
	 * @param dirpath
	 *            扫描路径入口
	 */
	private void auroraScanDirectory(Context context, String dirpath) {
		LogUtil.d(TAG, "auroraScanDirectory" + dirpath);
		Bundle args = new Bundle();
		args.putString("dirpath", dirpath);
		args.putString("scantype", ScannerUtils.ACTION_DIR_SCAN);
		context.startService(new Intent(context,
				AuroraMediaScannerService.class).putExtras(args));
	}

	/**
	 * 扫描根目录
	 */
	private void auroaraScanRoot(Context context) {
		LogUtil.d(TAG, "auroaraScanRoot");
		Bundle args = new Bundle();
		args.putString("scantype", ScannerUtils.ACTION_EXT_SCAN);
		context.startService(new Intent(context,
				AuroraMediaScannerService.class).putExtras(args));
	}

	/**
	 * 判断原生mediascanner正在扫描
	 */

	public static boolean isMediaScannerScanning(final ContentResolver cr) {
		Cursor cursor = null;
		try {
			cursor = cr.query(MediaStore.getMediaScannerUri(),
					new String[] { MediaStore.MEDIA_SCANNER_VOLUME }, null,
					null, null);
			if (cursor != null && cursor.getCount() > 0) {
				return true;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return false;
	}

	/**
	 * 查询数据量扫描器状态
	 * 
	 * @param resolver
	 * @param uri
	 * @param projection
	 * @param selection
	 * @param selectionArgs
	 * @param sortOrder
	 * @return
	 */
	private static final Cursor query(final ContentResolver resolver,
			final Uri uri, final String[] projection, final String selection,
			final String[] selectionArgs, final String sortOrder) {
		try {
			if (resolver == null) {
				return null;
			}
			return resolver.query(uri, projection, selection, selectionArgs,
					sortOrder);
		} catch (UnsupportedOperationException ex) {
			return null;
		}
	}

}
