/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.deviceinfo;

import aurora.app.AuroraAlertDialog;
import android.R.integer;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageDataObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StatFs;
import android.os.storage.IMountService;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraPreferenceCategory;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;
import android.os.SystemProperties;

import com.android.internal.os.storage.ExternalStorageFormatter;

import aurora.app.AuroraActivity;

import com.android.settings.ChooseLockSettingsHelper;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Panel showing storage usage on disk for known {@link StorageVolume} returned
 * by {@link StorageManager}. Calculates and displays usage of data types.
 */
public class Memory extends SettingsPreferenceFragment {
	private static final String TAG = "MemorySettings";

	private static final String TAG_CONFIRM_CLEAR_CACHE = "confirmClearCache";

	private static final int DLG_CONFIRM_UNMOUNT = 1;
	private static final int DLG_ERROR_UNMOUNT = 2;

	// The mountToggle AuroraPreference that has last been clicked.
	// Assumes no two successive unmount event on 2 different volumes are
	// performed before the first
	// one's preference is disabled
	private static AuroraPreference sLastClickedMountToggle;
	private static String sClickedMountPoint;

	// Access using getMountService()
	private IMountService mMountService;
	private StorageManager mStorageManager;
	private UsbManager mUsbManager;
	public static final String KEY_FORMAT_SDCARD = "format_sdcard";
	public static final String KEY_FORMAT_OTG = "format_otg";
	public static final String KEY_ERASE_FLAG = "erase_flag";
	private UsageBarPreference mUsageBarPreference;
	private AuroraPreferenceCategory sdcardCategory;
	private AuroraPreference formatSdcardPref;// 清空
	private UsageBarPreference mUsageExtraBarPreference;
	private AuroraPreferenceCategory otgCategory;
	private AuroraPreference formatOtgPref;
	private UsageBarPreference mSystemSaveBarPreference;
	private UsageBarPreference mInternalSaveBarPreference;
	private boolean mAllowFormat = false;
	private AuroraPreference mFormatInternalSavePreference;
	private StorageVolume mVolume;
	private boolean isInternalSaveBarPreference = false;
	private HashMap<String, String> pathFlage = new HashMap<String, String>();

	private ArrayList<StorageVolumePreferenceCategory> mCategories = Lists
			.newArrayList();
	private boolean isRunningUpdateThread;
	private static final int UPDATE_INTERNAL_STORAGE_VOLUME = 1;
	private static final int UPDATE_SDCARD_STORAGE_VOLUME = 2;
	private static final int UPDATE_OTG_STORAGE_VOLUME = 3;
	private boolean isUpdateSdCard;
	private boolean isUpdateOTG;
	private AuroraPreferenceCategory mInternalSaveCategory;

	private String mExternalStoragePath = null;
	public static final String KEY_UNMOUNT_SDCARD = "unmount_sdcard";// 卸载SDcard
	private AuroraPreference mUnmountSdcardPref;// 卸载

	// Add begin by aurora.jiangmx
	private static final int KEYGUARD_REQUEST = 55;
	public static final String ACTION_CONFIRM_KEY = "com.android.settings.ACTION_CONFIRM_KEY";
	public static final int FORMAT_TYPE_INTERNAL = 0;
	public static final int FORMAT_TYPE_SDCARD = 1;
	public static final int FORMAT_TYPE_OTG = 2;
	private int mFormatType = -1;
	// Add end
	private final BroadcastReceiver mMountBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// SD卡已经成功挂载
			if (action.equals("android.intent.action.MEDIA_MOUNTED")) {
				if (isUpdateSdCard) {
					updateSdCardUsageBarPreference();
					isUpdateSdCard = false;
				} else if (isUpdateOTG) {
					updateOTGUsageBarPreference();
					isUpdateOTG = false;
				}
			} else if (action.equals("android.intent.action.MEDIA_REMOVED")// 各种未挂载状态
					|| action
							.equals("android.intent.action.ACTION_MEDIA_UNMOUNTED")
					|| action
							.equals("android.intent.action.ACTION_MEDIA_BAD_REMOVAL")) {
			} else if (action.equals(Intent.ACTION_MEDIA_SCANNER_STARTED)) {// 开始扫描
			} else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {// 扫描完成
			} else if (action.equals(Intent.ACTION_MEDIA_SHARED)) {// 扩展介质的挂载被解除
																	// (unmount)。因为它已经作为
																	// USB
																	// 大容量存储被共享
			} else {
			}
		}
	};

	// Add begin by aurora.jiangmx
	private BroadcastReceiver lConfirmKeyReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub

			if (intent.getAction().equals(ACTION_CONFIRM_KEY)) {
				if (FORMAT_TYPE_INTERNAL == mFormatType) {
					showInternalFormatConfirmation();
				} else if (FORMAT_TYPE_SDCARD == mFormatType) {
					showSdcardFormatConfirmation();
				} else if (FORMAT_TYPE_OTG == mFormatType) {
					showOTGFormatConfirmation();
				}
			}
		}
	};

	// Add end

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		final Context context = getActivity();

		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

		mStorageManager = StorageManager.from(context);
		mStorageManager.registerListener(mStorageListener);

		addPreferencesFromResource(R.xml.device_info_memory);

		// addCategory(StorageVolumePreferenceCategory.buildForInternal(context));
		// // qy del

		final StorageVolume[] storageVolumes = mStorageManager.getVolumeList();
		// for (StorageVolume volume : storageVolumes) {
		// //if (!volume.isEmulated()) {
		// android.util.Log.e("hanping", "isEmulated->" + volume.isEmulated());
		// android.util.Log.e("hanping", "isRemovable->" +
		// volume.isRemovable());
		// // if (!volume.isEmulated() && !volume.isRemovable()) {
		// ////
		// addCategory(StorageVolumePreferenceCategory.buildForPhysical(context,
		// volume)); // qy del
		// //
		// // mAllowFormat = volume != null ? volume.isPrimary() : false;
		// // android.util.Log.e("hanping", "mAllowFormat->" + mAllowFormat);
		// // if(mAllowFormat){
		// // mVolume = volume;
		// // break;
		// // }
		// // }
		// if(volume != null && volume.isPrimary()) {
		// android.util.Log.e("hanping", "volume->" + volume.isEmulated());
		// mVolume = volume;
		// }
		// }

		for (int i = 0; i < storageVolumes.length; i++) {
			// android.util.Log.e("hanping", "volume->" +
			// storageVolumes[i].toString());
			if (storageVolumes != null && storageVolumes[i].isPrimary()) {
				mVolume = storageVolumes[i];
			}
		}
		// qy add
		isInternalSaveBarPreference = isExistInternalSave();

		AuroraPreferenceCategory systemSaveCategory = new AuroraPreferenceCategory(
				getActivity());
		systemSaveCategory.setTitle(getActivity().getResources().getString(
				R.string.internal_storage));
		getPreferenceScreen().addPreference(systemSaveCategory);
		mSystemSaveBarPreference = new UsageBarPreference(getActivity());
		getPreferenceScreen().addPreference(mSystemSaveBarPreference);
		// add the internal save storage preference
		if (isInternalSaveBarPreference) {
			mInternalSaveCategory = new AuroraPreferenceCategory(getActivity());
			mInternalSaveCategory.setTitle(getActivity().getResources()
					.getString(R.string.aurora_internal_storage));

			mInternalSaveBarPreference = new UsageBarPreference(getActivity());

			mFormatInternalSavePreference = new AuroraPreference(context);
			mFormatInternalSavePreference
					.setTitle(R.string.internal_storage_format);
			mFormatInternalSavePreference
					.setSummary(R.string.internal_storage_format_summary);
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				getPreferenceScreen().addPreference(mInternalSaveCategory);
				getPreferenceScreen().addPreference(mInternalSaveBarPreference);
				getPreferenceScreen().addPreference(
						mFormatInternalSavePreference);
			}

		} else {
			systemSaveCategory.setTitle(getActivity().getResources().getString(
					R.string.aurora_internal_storage));
		}

		if (existSDCard()) {
			sdcardCategory = new AuroraPreferenceCategory(getActivity());
			sdcardCategory.setTitle(getActivity().getResources().getString(
					R.string.sd_card_title));
			getPreferenceScreen().addPreference(sdcardCategory);

			mUsageBarPreference = new UsageBarPreference(getActivity());

			// updateSdCardUsageBarPreference();
			sdcardCategory.addPreference(mUsageBarPreference);

			formatSdcardPref = new AuroraPreference(getActivity());
			formatSdcardPref.setTitle(getActivity().getResources().getString(
					R.string.clear_sd_card));
			formatSdcardPref.setKey(KEY_FORMAT_SDCARD);
			formatSdcardPref.setKey(KEY_UNMOUNT_SDCARD);
			sdcardCategory.addPreference(formatSdcardPref);
			// Unmount the SD card
			mUnmountSdcardPref = new AuroraPreference(getActivity());
			mUnmountSdcardPref.setTitle(getActivity().getResources().getString(
					R.string.sd_eject));
			mUnmountSdcardPref.setKey(KEY_UNMOUNT_SDCARD);
			sdcardCategory.addPreference(mUnmountSdcardPref);

			Log.v(TAG, "----mExternalStoragePath======" + mExternalStoragePath);
		}

		String external = SystemProperties.get("ro.external.storage");
		if (null == external) {
			external = "";
		}

		String internalPath = gionee.os.storage.GnStorageManager.getInstance(
				getActivity()).getInternalStoragePath();
		if (null == internalPath) {
			internalPath = "";
		}

		Log.v(TAG, "----external======" + external);
		Log.v(TAG, "----internalPath======" + internalPath);

		for (int i = 0; i < storageVolumes.length; i++) {
			String state = mStorageManager.getVolumeState(storageVolumes[i]
					.getPath());
			Log.v(TAG, "--111--state======" + state);
			Log.v(TAG, "--111--storageVolumes[i].getPath().toString()======"
					+ storageVolumes[i].getPath().toString());

			if (storageVolumes[i].getPath().toString().equals(external)) {
				pathFlage.put(storageVolumes[i].getPath().toString(), "sdcard");
			} else if (storageVolumes[i].getPath().toString()
					.equals(internalPath)) {
				pathFlage.put(storageVolumes[i].getPath().toString(),
						"internal");
			} else {
				if (existSDCard()
						&& storageVolumes[i].getPath().toString()
								.equals(mExternalStoragePath)) {
					continue;
				} else {
					Log.v(TAG,
							"--2222--storageVolumes[i].getPath().toString()======"
									+ storageVolumes[i].getPath().toString());
					pathFlage
							.put(storageVolumes[i].getPath().toString(), "otg");
				}
			}
		}

		for (int i = 1; i < storageVolumes.length; i++) {
			String state = mStorageManager.getVolumeState(storageVolumes[i]
					.getPath());
			if (state.equals("mounted")) {
				if (existSDCard()
						&& storageVolumes[i].getPath().toString()
								.equals(mExternalStoragePath)) {
					continue;
				} else if (storageVolumes[i].getPath().toString()
						.equals(internalPath)) {
					continue;
				} else if (storageVolumes[i].getPath().toString()
						.equals(external)) {
					continue;
				} else {

					Log.v(TAG,
							"--3333--storageVolumes[i].getPath().toString()======"
									+ storageVolumes[i].getPath().toString());

					otgCategory = new AuroraPreferenceCategory(getActivity());
					otgCategory.setTitle(getActivity().getResources()
							.getString(R.string.otg_card_title));
					getPreferenceScreen().addPreference(otgCategory);
					mUsageExtraBarPreference = new UsageBarPreference(
							getActivity());
					getPreferenceScreen().addPreference(
							mUsageExtraBarPreference);

					formatOtgPref = new AuroraPreference(getActivity());
					formatOtgPref.setTitle(getActivity().getResources()
							.getString(R.string.clear_otg_card));
					formatOtgPref.setKey(KEY_FORMAT_OTG);
					getPreferenceScreen().addPreference(formatOtgPref);
				}
			}
		}

		/*
		 * gionee.os.storage.GnStorageManager
		 * .getInstance(getActivity()).getExternalStoragePath();
		 */

		IntentFilter intentFilter = new IntentFilter(
				Intent.ACTION_MEDIA_MOUNTED);// sd卡被插入，且已经挂载
		intentFilter.setPriority(1000);// 设置最高优先级
		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);// sd卡存在，但还没有挂载
		intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);// sd卡被移除
		intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);// sd卡作为
															// USB大容量存储被共享，挂载被解除
		intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);// sd卡已经从sd卡插槽拔出，但是挂载点还没解除
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);// 开始扫描
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);// 扫描完成
		intentFilter.addDataScheme("file");
		getActivity().registerReceiver(mMountBroadcastReceiver, intentFilter);// 注册监听函数

		setHasOptionsMenu(true);

		// Add begin by aurora.jiangmx
		IntentFilter lIntFilter = new IntentFilter(ACTION_CONFIRM_KEY);
		getActivity().registerReceiver(lConfirmKeyReceiver, lIntFilter);
		// Add end
	}

	// qy add
	private ProgressDialog mProgressDialog;
	private Handler mClearSDcardHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case UPDATE_INTERNAL_STORAGE_VOLUME:
				updateInternalSaveUsageBarPreference();
				// 关闭ProgressDialog
				// mProgressDialog.setMessage(getActivity().getResources().getString(R.string.clear_complete));
				mProgressDialog.dismiss();
				break;
			case UPDATE_SDCARD_STORAGE_VOLUME:
				// updateSdCardUsageBarPreference();
				// 关闭ProgressDialog
				// isUpdateSdCard = true;
				// mProgressDialog.setMessage(getActivity().getResources().getString(R.string.clear_complete));
				// mProgressDialog.dismiss();
			case UPDATE_OTG_STORAGE_VOLUME:
				// isUpdateOTG = true;
				// mProgressDialog.setMessage(getActivity().getResources().getString(R.string.clear_complete));
				// mProgressDialog.dismiss();
			}

		}
	};

	private boolean isExistInternalSave() {
		String systemSavePath = Environment.getDataDirectory().getPath();
		String internalSavePath = Environment.getExternalStorageDirectory()
				.getPath();
		String sysStr = formatSize(getTotalSize(systemSavePath));
		String internalStr = formatSize(getTotalSize(internalSavePath));

		double sysInt = Double.parseDouble(sysStr.substring(0,
				sysStr.length() - 2));
		double internalInt = Double.parseDouble(internalStr.substring(0,
				sysStr.length() - 2));

		Log.v(TAG,
				"sysStr.substring(sysStr.length()-2)========="
						+ sysStr.substring(sysStr.length() - 2));
		if (((sysStr.substring(sysStr.length() - 2).equals(internalStr
				.substring(internalStr.length() - 2))) && (sysInt >= internalInt && (sysInt - internalInt) < 1.0f))
				|| (internalStr.substring(0, 1).equals("0") && internalStr
						.substring(internalStr.length() - 1).equals("B"))) {
			return false;
		}
		return true;
	}

	private void updateSystemSaveUsageBarPreference() {
		String systemSavePath = Environment.getDataDirectory().getPath();
		String totalSize = "";
		totalSize = android.os.SystemProperties.get("ro.iuni.internalmemory");

		if (getTotalSize(systemSavePath) > 0) {
			mSystemSaveBarPreference
					.setProgressAndMax(
							(int) ((getTotalSize(systemSavePath) - getAvailSize(systemSavePath)) * 1000 / getTotalSize(systemSavePath)),
							1000);
		} else {
			mSystemSaveBarPreference.setProgressAndMax(0, 1000);
		}
		mSystemSaveBarPreference.setSummary(getActivity().getResources()
				.getString(R.string.avail_size)
				+ formatSize(getAvailSize(systemSavePath)));

		if (totalSize.trim().equals("") || totalSize.equals(null)) {
			mSystemSaveBarPreference.setTitle(getActivity().getResources()
					.getString(R.string.total_size)
					+ formatSize(getTotalSize(systemSavePath)));
		} else {
			if (!isInternalSaveBarPreference) {
				String dataPath = Environment.getDataDirectory().getPath();
				String dataStr = formatSize(getTotalSize(dataPath));
				double dataInt = Double.parseDouble(dataStr.substring(0,
						dataStr.length() - 2));

				if (dataInt < 16.0f) {
					totalSize = "" + 16;
				} else if (dataInt < 32.0f) {
					totalSize = "" + 32;
				} else {
					totalSize = "" + 64;
				}
			}

			mSystemSaveBarPreference.setTitle(getActivity().getResources()
					.getString(R.string.total_size) + totalSize + "GB");
			long total = (long) (Float.valueOf(totalSize) * 1024 * 1024 * 1024);
			mSystemSaveBarPreference
					.setProgressAndMax(
							(int) ((total - getAvailSize(systemSavePath)) * 1000 / total),
							1000);
		}

	}

	private void updateInternalSaveUsageBarPreference() {
		String internalSavePath = Environment.getExternalStorageDirectory()
				.getPath();
		if (getTotalSize(internalSavePath) > 0) {
			if (getTotalSize(internalSavePath) > 0) {
				mInternalSaveBarPreference
						.setProgressAndMax(
								(int) ((getTotalSize(internalSavePath) - getAvailSize(internalSavePath)) * 1000 / getTotalSize(internalSavePath)),
								1000);
			} else {
				mInternalSaveBarPreference.setProgressAndMax(0, 1000);
			}
			mInternalSaveBarPreference.setSummary(getActivity().getResources()
					.getString(R.string.avail_size)
					+ formatSize(getAvailSize(internalSavePath)));
			mInternalSaveBarPreference.setTitle(getActivity().getResources()
					.getString(R.string.total_size)
					+ formatSize(getTotalSize(internalSavePath)));
		}

	}

	private void updateOTGUsageBarPreference() {
		final StorageVolume[] storageVolumes = mStorageManager.getVolumeList();
		String external = SystemProperties.get("ro.external.storage");
		if (null == external) {
			Log.v(TAG,
					"-----SystemProperties.get(ro.external.storage)---is null");
			external = "";
		}

		String internalPath = gionee.os.storage.GnStorageManager.getInstance(
				getActivity()).getInternalStoragePath();
		Log.d(TAG, " is internalPath =" + (internalPath == null));
		if (null == internalPath) {
			internalPath = "";
		}

		for (int i = 1; i < storageVolumes.length; i++) {
			String state = mStorageManager.getVolumeState(storageVolumes[i]
					.getPath());
			if (state.equals("mounted")) {
				if (existSDCard()
						&& storageVolumes[i].getPath().toString()
								.equals(mExternalStoragePath)) {
					continue;
				} else if (storageVolumes[i].getPath().toString()
						.equals(internalPath)) {
					continue;
				} else if (storageVolumes[i].getPath().toString()
						.equals(external)) {
					continue;
				} else {
					String externalSDFilePath = storageVolumes[i].getPath()
							.toString();

					Log.v(TAG, "-----aaaa----externalSDFilePath---==="
							+ externalSDFilePath);
					if (getTotalSize(externalSDFilePath) > 0) {
						mUsageExtraBarPreference
								.setProgressAndMax(
										(int) ((getTotalSize(externalSDFilePath) - getAvailSize(externalSDFilePath)) * 1000 / getTotalSize(externalSDFilePath)),
										1000);
					} else {
						mUsageExtraBarPreference.setProgressAndMax(0, 1000);
					}
					mUsageExtraBarPreference.setSummary(getActivity()
							.getResources().getString(R.string.avail_size)
							+ formatSize(getAvailSize(externalSDFilePath)));
					mUsageExtraBarPreference.setTitle(getActivity()
							.getResources().getString(R.string.total_size)
							+ formatSize(getTotalSize(externalSDFilePath)));
				}
			}
		}
		//
		// if((storageVolumes.length > 2) && (null != storageVolumes[2]) &&
		// (getTotalSize(storageVolumes[2].getPath().toString()) > 0)) {
		// String externalSDFilePath = storageVolumes[2].getPath().toString();
		// if(getTotalSize(externalSDFilePath) > 0){
		// mUsageExtraBarPreference.setProgressAndMax((int)((getTotalSize(externalSDFilePath)-getAvailSize(externalSDFilePath))*1000/getTotalSize(externalSDFilePath)),1000);
		// }else{
		// mUsageExtraBarPreference.setProgressAndMax(0,1000);
		// }
		// mUsageExtraBarPreference.setSummary(getActivity().getResources().getString(R.string.avail_size)+formatSize(getAvailSize(externalSDFilePath)));
		// mUsageExtraBarPreference.setTitle(getActivity().getResources().getString(R.string.total_size)+formatSize(getTotalSize(externalSDFilePath)));
		// }
	}

	private void updateSdCardUsageBarPreference() {
		String externalSDFilePath = gionee.os.storage.GnStorageManager
				.getInstance(getActivity()).getExternalStoragePath();
		if (getTotalSize(externalSDFilePath) > 0) {
			mUsageBarPreference
					.setProgressAndMax(
							(int) ((getTotalSize(externalSDFilePath) - getAvailSize(externalSDFilePath)) * 1000 / getTotalSize(externalSDFilePath)),
							1000);
		} else {
			mUsageBarPreference.setProgressAndMax(0, 1000);
		}
		mUsageBarPreference.setSummary(getActivity().getResources().getString(
				R.string.avail_size)
				+ formatSize(getAvailSize(externalSDFilePath)));
		mUsageBarPreference.setTitle(getActivity().getResources().getString(
				R.string.total_size)
				+ formatSize(getTotalSize(externalSDFilePath)));
	}

	public boolean existSDCard() {
		if (gionee.os.storage.GnStorageManager.getInstance(getActivity())
				.getExternalStoragePath() != null) {
			if (null == mExternalStoragePath) {
				mExternalStoragePath = gionee.os.storage.GnStorageManager
						.getInstance(getActivity()).getExternalStoragePath();
			}

			return true;
		} else {
			mExternalStoragePath = null;
			return false;
		}
	}

	// 可用空间
	public long getAvailSize(String filePath) {
		long availSize = 0;
		try {
			File path = new File(filePath);
			StatFs sf = new StatFs(path.getPath());

			long blockSize = sf.getBlockSize();

			long availBlocks = sf.getAvailableBlocks();
			availSize = blockSize * availBlocks;
		} catch (IllegalArgumentException e) {
			Log.e(TAG, e.toString());
		}
		return availSize; // Byte
	}

	// 总大小
	public long getTotalSize(String filePath) {
		long totalSize = 0;
		try {
			// File path = Environment.getExternalStorageDirectory();
			File path = new File(filePath);
			StatFs sf = new StatFs(path.getPath());

			long blockSize = sf.getBlockSize();

			long totalBlocks = sf.getBlockCount();
			totalSize = blockSize * totalBlocks;
		} catch (IllegalArgumentException e) {
			Log.e(TAG, e.toString());

		}
		return totalSize;
	}

	private String formatSize(long size) {
		return Formatter.formatFileSize(getActivity(), size);
	}

	private void wipeSDCard(String path) {
		File deleteMatchingFile = new File(path);
		try {
			File[] filenames = deleteMatchingFile.listFiles();
			if (filenames != null && filenames.length > 0) {
				for (File tempFile : filenames) {
					if (tempFile.isDirectory()) {
						wipeDirectory(tempFile.toString());
						tempFile.delete();
					} else {
						tempFile.delete();
					}
				}
			} else {
				deleteMatchingFile.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void wipeDirectory(String name) {
		File directoryFile = new File(name);
		File[] filenames = directoryFile.listFiles();
		if (filenames != null && filenames.length > 0) {
			for (File tempFile : filenames) {
				if (tempFile.isDirectory()) {
					wipeDirectory(tempFile.toString());
					tempFile.delete();
				} else {
					tempFile.delete();
				}
			}
		} else {
			directoryFile.delete();
		}
	}

	// qy end

	private void addCategory(StorageVolumePreferenceCategory category) {
		mCategories.add(category);
		getPreferenceScreen().addPreference(category);
		category.init();
	}

	private boolean isMassStorageEnabled() {
		// Mass storage is enabled if primary volume supports it
		final StorageVolume[] volumes = mStorageManager.getVolumeList();
		final StorageVolume primary = StorageManager.getPrimaryVolume(volumes);
		return primary != null && primary.allowMassStorage();
	}

	@Override
	public void onResume() {
		super.onResume();
		IntentFilter intentFilter = new IntentFilter(
				Intent.ACTION_MEDIA_SCANNER_STARTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		intentFilter.addDataScheme("file");
		getActivity().registerReceiver(mMediaScannerReceiver, intentFilter);

		intentFilter = new IntentFilter();
		intentFilter.addAction(UsbManager.ACTION_USB_STATE);
		getActivity().registerReceiver(mMediaScannerReceiver, intentFilter);

		for (StorageVolumePreferenceCategory category : mCategories) {
			category.onResume();
		}
		// update
		updateSystemSaveUsageBarPreference();
		if (isInternalSaveBarPreference) {
			updateInternalSaveUsageBarPreference();
		}

		if (existSDCard()) {
			updateSdCardUsageBarPreference();
		}

		updateOTGUsageBarPreference();
	}

	StorageEventListener mStorageListener = new StorageEventListener() {
		@Override
		public void onStorageStateChanged(String path, String oldState,
				String newState) {
			Log.i(TAG, "Received storage state changed notification that "
					+ path + " changed state from " + oldState + " to "
					+ newState);
			for (StorageVolumePreferenceCategory category : mCategories) {
				final StorageVolume volume = category.getStorageVolume();
				if (volume != null && path.equals(volume.getPath())) {
					category.onStorageStateChanged();
					break;
				}
			}

			// qy add 2014 04 08
			if (isInternalSaveBarPreference) {
				if (newState.equals(Environment.MEDIA_SHARED)) {
					getPreferenceScreen().removePreference(
							mInternalSaveCategory);
					getPreferenceScreen().removePreference(
							mInternalSaveBarPreference);
					getPreferenceScreen().removePreference(
							mFormatInternalSavePreference);

				} else if (newState.equals(Environment.MEDIA_MOUNTED)) {
					getPreferenceScreen().addPreference(mInternalSaveCategory);
					getPreferenceScreen().addPreference(
							mInternalSaveBarPreference);
					getPreferenceScreen().addPreference(
							mFormatInternalSavePreference);
					updateInternalSaveUsageBarPreference();

				}
			}

			if (mExternalStoragePath != null
					&& path.equals(mExternalStoragePath)) {
				return;
			}

			if (pathFlage.get(path).equals("otg")) {
				if (newState.equals(Environment.MEDIA_MOUNTED)) {
					otgCategory = new AuroraPreferenceCategory(getActivity());
					otgCategory.setTitle(getActivity().getResources()
							.getString(R.string.otg_card_title));
					getPreferenceScreen().addPreference(otgCategory);
					mUsageExtraBarPreference = new UsageBarPreference(
							getActivity());
					getPreferenceScreen().addPreference(
							mUsageExtraBarPreference);
					formatOtgPref = new AuroraPreference(getActivity());
					formatOtgPref.setTitle(getActivity().getResources()
							.getString(R.string.clear_otg_card));
					formatOtgPref.setKey(KEY_FORMAT_OTG);
					getPreferenceScreen().addPreference(formatOtgPref);
					updateOTGUsageBarPreference();
				} else {
					if (null != otgCategory) {
						getPreferenceScreen().removePreference(otgCategory);
						getPreferenceScreen().removePreference(
								mUsageExtraBarPreference);
						getPreferenceScreen().removePreference(formatOtgPref);
					}
				}
			} else if (pathFlage.get(path).equals("sdcard")) {
				if (newState.equals(Environment.MEDIA_MOUNTED)) {
					sdcardCategory = new AuroraPreferenceCategory(getActivity());
					sdcardCategory.setTitle(getActivity().getResources()
							.getString(R.string.sd_card_title));
					getPreferenceScreen().addPreference(sdcardCategory);
					mUsageBarPreference = new UsageBarPreference(getActivity());
					sdcardCategory.addPreference(mUsageBarPreference);
					formatSdcardPref = new AuroraPreference(getActivity());
					formatSdcardPref.setTitle(getActivity().getResources()
							.getString(R.string.clear_sd_card));
					formatSdcardPref.setKey(KEY_FORMAT_SDCARD);
					sdcardCategory.addPreference(formatSdcardPref);
					// Unmount the SD card by aurora.JM
					mUnmountSdcardPref = new AuroraPreference(getActivity());
					mUnmountSdcardPref.setTitle(getActivity().getResources()
							.getString(R.string.sd_eject));
					mUnmountSdcardPref.setKey(KEY_UNMOUNT_SDCARD);
					sdcardCategory.addPreference(mUnmountSdcardPref);
					// Add begin by aurora.jiangmx
					gionee.os.storage.GnStorageManager.getInstance(
							getActivity()).getMountedSDCard();
					// Add end
					updateSdCardUsageBarPreference();

				} else {
					if (sdcardCategory != null) {
						getPreferenceScreen().removePreference(sdcardCategory);
						sdcardCategory = null;
					}
				}
			}
		}

	};

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(mMediaScannerReceiver);
		for (StorageVolumePreferenceCategory category : mCategories) {
			category.onPause();
		}
		isRunningUpdateThread = false;
	}

	@Override
	public void onDestroy() {
		if (mStorageManager != null && mStorageListener != null) {
			mStorageManager.unregisterListener(mStorageListener);
		}
		super.onDestroy();
		getActivity().unregisterReceiver(mMountBroadcastReceiver);// 取消注册
		// Add begin by aurora.jiangmx
		getActivity().unregisterReceiver(lConfirmKeyReceiver);
		// Add end
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.storage, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		final MenuItem usb = menu.findItem(R.id.storage_usb);
		usb.setVisible(!isMassStorageEnabled());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.storage_usb:
			if (getActivity() instanceof AuroraPreferenceActivity) {
				((AuroraPreferenceActivity) getActivity())
						.startPreferencePanel(
								UsbSettings.class.getCanonicalName(), null,
								R.string.storage_title_usb, null, this, 0);
			} else {
				startFragment(this, UsbSettings.class.getCanonicalName(), -1,
						null);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private synchronized IMountService getMountService() {
		if (mMountService == null) {
			IBinder service = ServiceManager.getService("mount");
			if (service != null) {
				mMountService = IMountService.Stub.asInterface(service);
			} else {
				Log.e(TAG, "Can't get mount service");
			}
		}
		return mMountService;
	}

	public void updateProgressDialog(int msg) {
		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(getActivity());
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setCancelable(false);
			mProgressDialog.getWindow().setType(
					WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			mProgressDialog.show();
		}

		mProgressDialog.setMessage(getText(msg));
	}

	@Override
	public boolean onPreferenceTreeClick(
			AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
		if (StorageVolumePreferenceCategory.KEY_CACHE.equals(preference
				.getKey())) {
			ConfirmClearCacheFragment.show(this);
			return true;
		}

		// qy
		if (preference == mFormatInternalSavePreference) {
			// Modify begin by aurora.jiangmx
			/*
			 * new
			 * AlertDialog.Builder(getActivity()).setTitle(R.string.format_title
			 * ).setCancelable(false) .setMessage(R.string.format_message)
			 * .setNegativeButton(R.string.format_cancel, new
			 * DialogInterface.OnClickListener() {
			 * 
			 * public void onClick(DialogInterface dialog, int which) { } })
			 * .setPositiveButton(R.string.format_continue, new
			 * DialogInterface.OnClickListener() {
			 * 
			 * public void onClick(DialogInterface dialog, int which) {
			 * 
			 * // build confirm dialog new
			 * AlertDialog.Builder(getActivity()).setTitle
			 * (R.string.format_title).setCancelable(false)
			 * .setMessage(R.string.format_confirm_message
			 * ).setNegativeButton(R.string.format_cancel, new
			 * DialogInterface.OnClickListener() {
			 * 
			 * public void onClick(DialogInterface dialog, int which) { }
			 * }).setPositiveButton(R.string.format_ok, new
			 * DialogInterface.OnClickListener() {
			 * 
			 * public void onClick(DialogInterface dialog, int which) { if
			 * (Utils.isMonkeyRunning()) { return; } // clear
			 * if(Build.MODEL.contains("MI 3")) {
			 * updateProgressDialog(R.string.in_progress_erasing); String
			 * extStoragePath = mVolume.getPath(); wipeSDCard(extStoragePath);
			 * mClearSDcardHandler
			 * .obtainMessage(UPDATE_INTERNAL_STORAGE_VOLUME).sendToTarget(); }
			 * else if (Build.MODEL.contains("MI 2")) {
			 * updateProgressDialog(R.string.in_progress_erasing); String
			 * extStoragePath = mVolume.getPath(); wipeSDCard(extStoragePath);
			 * mClearSDcardHandler
			 * .obtainMessage(UPDATE_INTERNAL_STORAGE_VOLUME).sendToTarget(); }
			 * else { Intent intent = new
			 * Intent("com.android.settings.deviceinfo.storageFormatter"); //
			 * intent.putExtra(StorageVolume.EXTRA_STORAGE_VOLUME, mVolume);
			 * intent.putExtra(KEY_ERASE_FLAG, "in");
			 * getActivity().startService(intent); } // end }
			 * }).create().show(); } }).create().show();
			 */
			// ------------div------------
			mFormatType = FORMAT_TYPE_INTERNAL;
			boolean lIsShowComfirmation = runKeyguardConfirmation(KEYGUARD_REQUEST);
			if (!lIsShowComfirmation) {
				showInternalFormatConfirmation();
			}
			// Modify end

			/*
			 * intent = new Intent(Intent.ACTION_VIEW);
			 * intent.setClass(getContext(),
			 * com.android.settings.MediaFormat.class);
			 * intent.putExtra(StorageVolume.EXTRA_STORAGE_VOLUME, mVolume);
			 */
			// end
		}

		if (KEY_FORMAT_SDCARD.equals(preference.getKey())) {

			// Modify begin by aurora.jiangmx
			/*
			 * new
			 * AlertDialog.Builder(getActivity()).setTitle(R.string.clear_sd_card
			 * ).setCancelable(false) .setMessage(R.string.clear_sd_card_message
			 * ).setNegativeButton(R.string.format_cancel, new
			 * DialogInterface.OnClickListener() {
			 * 
			 * public void onClick(DialogInterface dialog, int which) {
			 * 
			 * } }).setPositiveButton(R.string.format_continue, new
			 * DialogInterface.OnClickListener() {
			 * 
			 * public void onClick(DialogInterface dialog, int which) {
			 * 
			 * // build confirm dialog new
			 * AlertDialog.Builder(getActivity()).setTitle
			 * (R.string.clear_sd_card).setCancelable(false)
			 * .setMessage(R.string.clear_sd_card_confirm_message
			 * ).setNegativeButton(R.string.format_cancel, new
			 * DialogInterface.OnClickListener() {
			 * 
			 * public void onClick(DialogInterface dialog, int which) { }
			 * }).setPositiveButton(R.string.clear_ok, new
			 * DialogInterface.OnClickListener() {
			 * 
			 * public void onClick(DialogInterface dialog, int which) { if
			 * (Utils.isMonkeyRunning()) { return; } // clear final
			 * StorageVolume[] storageVolumes = mStorageManager.getVolumeList();
			 * StorageVolume mSDVolume = null; String externalSDFilePath =
			 * gionee.os.storage.GnStorageManager
			 * .getInstance(getActivity()).getExternalStoragePath(); for
			 * (StorageVolume volume : storageVolumes) {
			 * if(externalSDFilePath.equals(volume.getPath().toString())) {
			 * mSDVolume = volume; } } Intent intent = new
			 * Intent("com.android.settings.deviceinfo.storageFormatter");
			 * intent.putExtra(KEY_ERASE_FLAG, "sdcard");
			 * intent.putExtra(StorageVolume.EXTRA_STORAGE_VOLUME, mSDVolume);
			 * getActivity().startService(intent); isUpdateSdCard = true; //
			 * clear end } }).create().show(); } }).create().show();
			 */
			// ------------div------------
			mFormatType = FORMAT_TYPE_SDCARD;
			boolean lIsShowComfirmation = runKeyguardConfirmation(KEYGUARD_REQUEST);
			if (!lIsShowComfirmation) {
				showSdcardFormatConfirmation();
			}
			// Modify end

		} // end

		if (KEY_FORMAT_OTG.equals(preference.getKey())) {

			// Modify begin by aurora.jiangmx
			/*
			 * new
			 * AlertDialog.Builder(getActivity()).setTitle(R.string.clear_otg_card
			 * ).setCancelable(false) .setMessage(R.string.clear_sd_card_message
			 * ).setNegativeButton(R.string.format_cancel, new
			 * DialogInterface.OnClickListener() {
			 * 
			 * public void onClick(DialogInterface dialog, int which) {
			 * 
			 * } }).setPositiveButton(R.string.format_continue, new
			 * DialogInterface.OnClickListener() {
			 * 
			 * public void onClick(DialogInterface dialog, int which) {
			 * 
			 * // build confirm dialog new
			 * AlertDialog.Builder(getActivity()).setTitle
			 * (R.string.clear_otg_card).setCancelable(false)
			 * .setMessage(R.string.clear_sd_card_confirm_message
			 * ).setNegativeButton(R.string.format_cancel, new
			 * DialogInterface.OnClickListener() {
			 * 
			 * public void onClick(DialogInterface dialog, int which) {
			 * 
			 * } }).setPositiveButton(R.string.clear_ok, new
			 * DialogInterface.OnClickListener() {
			 * 
			 * public void onClick(DialogInterface dialog, int which) {
			 * 
			 * if (Utils.isMonkeyRunning()) { return; } // clear final
			 * StorageVolume[] storageVolumes = mStorageManager.getVolumeList();
			 * StorageVolume mOTGVolume = null; String externalSDFilePath =
			 * gionee.os.storage.GnStorageManager
			 * .getInstance(getActivity()).getExternalStoragePath(); for (int i
			 * = 1; i < storageVolumes.length; i++) { String state =
			 * mStorageManager
			 * .getVolumeState(storageVolumes[i].getPath().toString());
			 * if(Environment.MEDIA_MOUNTED.equals(state)) { if ((null !=
			 * externalSDFilePath) &&
			 * !externalSDFilePath.equals(storageVolumes[i
			 * ].getPath().toString())) { mOTGVolume = storageVolumes[i]; } else
			 * { mOTGVolume = storageVolumes[i]; } } } Intent intent = new
			 * Intent("com.android.settings.deviceinfo.storageFormatter");
			 * intent.putExtra(KEY_ERASE_FLAG, "otg");
			 * intent.putExtra(StorageVolume.EXTRA_STORAGE_VOLUME, mOTGVolume);
			 * getActivity().startService(intent); isUpdateOTG = true; // clear
			 * end } }).create().show(); } }).create().show();
			 */
			// ------------div------------
			mFormatType = FORMAT_TYPE_OTG;
			boolean lIsShowComfirmation = runKeyguardConfirmation(KEYGUARD_REQUEST);
			if (!lIsShowComfirmation) {
				showOTGFormatConfirmation();
			}
			// Modify end

		} // end

		if (KEY_UNMOUNT_SDCARD.equals(preference.getKey())) {
			if (existSDCard()) {
				String state = mStorageManager
						.getVolumeState(mExternalStoragePath);
				if (state.equals("mounted")) {
					sLastClickedMountToggle = preference;
					sClickedMountPoint = mExternalStoragePath;
					// 卸载SD的点击事件
					unmount();
				} else {
					Toast.makeText(getActivity(),
							R.string.dlg_error_unmount_text, Toast.LENGTH_SHORT)
							.show();
				}
				return true;
			}
		}

		for (StorageVolumePreferenceCategory category : mCategories) {
			Intent intent = category.intentForClick(preference);
			if (intent != null) {
				// Don't go across app boundary if monkey is running
				if (!Utils.isMonkeyRunning()) {
					startActivity(intent);
				}
				return true;
			}

			final StorageVolume volume = category.getStorageVolume();
			if (volume != null && category.mountToggleClicked(preference)) {
				sLastClickedMountToggle = preference;
				sClickedMountPoint = volume.getPath();
				String state = mStorageManager.getVolumeState(volume.getPath());
				if (Environment.MEDIA_MOUNTED.equals(state)
						|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
					// ...............
					unmount();
				} else {
					mount();
				}
				return true;
			}
		}

		return false;
	}

	// Add begin by aurora.jiangmx
	private boolean runKeyguardConfirmation(int request) {
		Resources res = getActivity().getResources();
		return new ChooseLockSettingsHelper((AuroraActivity) getActivity())
				.launchConfirmationActivity(request,
						res.getText(R.string.master_clear_gesture_prompt),
						res.getText(R.string.master_clear_gesture_explanation));
	}

	private void showInternalFormatConfirmation() {
		new AlertDialog.Builder(getActivity())
				.setTitle(R.string.format_title)
				.setCancelable(false)
				.setMessage(R.string.format_message)
				.setNegativeButton(R.string.format_cancel,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
							}
						})
				.setPositiveButton(R.string.format_continue,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {

								// build confirm dialog
								new AlertDialog.Builder(getActivity())
										.setTitle(R.string.format_title)
										.setCancelable(false)
										.setMessage(
												R.string.format_confirm_message)
										.setNegativeButton(
												R.string.format_cancel,
												new DialogInterface.OnClickListener() {

													public void onClick(
															DialogInterface dialog,
															int which) {
													}
												})
										.setPositiveButton(
												R.string.format_ok,
												new DialogInterface.OnClickListener() {

													public void onClick(
															DialogInterface dialog,
															int which) {
														if (Utils
																.isMonkeyRunning()) {
															return;
														}
														// clear
														if (Build.MODEL
																.contains("MI 3")) {
															updateProgressDialog(R.string.in_progress_erasing);
															String extStoragePath = mVolume
																	.getPath();
															wipeSDCard(extStoragePath);
															mClearSDcardHandler
																	.obtainMessage(
																			UPDATE_INTERNAL_STORAGE_VOLUME)
																	.sendToTarget();
														} else if (Build.MODEL
																.contains("MI 2")) {
															updateProgressDialog(R.string.in_progress_erasing);
															String extStoragePath = mVolume
																	.getPath();
															wipeSDCard(extStoragePath);
															mClearSDcardHandler
																	.obtainMessage(
																			UPDATE_INTERNAL_STORAGE_VOLUME)
																	.sendToTarget();
														} else {
															Intent intent = new Intent(
																	"com.android.settings.deviceinfo.storageFormatter");
															// intent.putExtra(StorageVolume.EXTRA_STORAGE_VOLUME,
															// mVolume);
															intent.putExtra(
																	KEY_ERASE_FLAG,
																	"in");
															getActivity()
																	.startService(
																			intent);
														}
														// end
													}
												}).create().show();
							}
						}).create().show();
	}

	private void showSdcardFormatConfirmation() {
		new AlertDialog.Builder(getActivity())
				.setTitle(R.string.clear_sd_card)
				.setCancelable(false)
				.setMessage(R.string.clear_sd_card_message)
				.setNegativeButton(R.string.format_cancel,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {

							}
						})
				.setPositiveButton(R.string.format_continue,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {

								// build confirm dialog
								new AlertDialog.Builder(getActivity())
										.setTitle(R.string.clear_sd_card)
										.setCancelable(false)
										.setMessage(
												R.string.clear_sd_card_confirm_message)
										.setNegativeButton(
												R.string.format_cancel,
												new DialogInterface.OnClickListener() {

													public void onClick(
															DialogInterface dialog,
															int which) {
													}
												})
										.setPositiveButton(
												R.string.clear_ok,
												new DialogInterface.OnClickListener() {

													public void onClick(
															DialogInterface dialog,
															int which) {
														if (Utils
																.isMonkeyRunning()) {
															return;
														}
														// clear

														if (!existSDCard()) {
															return;
														}

														final StorageVolume[] storageVolumes = mStorageManager
																.getVolumeList();
														StorageVolume mSDVolume = null;
														String externalSDFilePath = gionee.os.storage.GnStorageManager
																.getInstance(
																		getActivity())
																.getExternalStoragePath();
														for (StorageVolume volume : storageVolumes) {
															if (externalSDFilePath
																	.equals(volume
																			.getPath()
																			.toString())) {
																mSDVolume = volume;
															}
														}
														Intent intent = new Intent(
																"com.android.settings.deviceinfo.storageFormatter");
														intent.putExtra(
																KEY_ERASE_FLAG,
																"sdcard");
														intent.putExtra(
																StorageVolume.EXTRA_STORAGE_VOLUME,
																mSDVolume);
														getActivity()
																.startService(
																		intent);
														isUpdateSdCard = true;
														// clear end
													}
												}).create().show();
							}
						}).create().show();
	}

	private void showOTGFormatConfirmation() {
		new AlertDialog.Builder(getActivity())
				.setTitle(R.string.clear_otg_card)
				.setCancelable(false)
				.setMessage(R.string.clear_sd_card_message)
				.setNegativeButton(R.string.format_cancel,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {

							}
						})
				.setPositiveButton(R.string.format_continue,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {

								// build confirm dialog
								new AlertDialog.Builder(getActivity())
										.setTitle(R.string.clear_otg_card)
										.setCancelable(false)
										.setMessage(
												R.string.clear_sd_card_confirm_message)
										.setNegativeButton(
												R.string.format_cancel,
												new DialogInterface.OnClickListener() {

													public void onClick(
															DialogInterface dialog,
															int which) {

													}
												})
										.setPositiveButton(
												R.string.clear_ok,
												new DialogInterface.OnClickListener() {

													public void onClick(
															DialogInterface dialog,
															int which) {

														if (Utils
																.isMonkeyRunning()) {
															return;
														}
														// clear

														if (null == SystemProperties
																.get("ro.external.storage")) {
															return;
														}

														final StorageVolume[] storageVolumes = mStorageManager
																.getVolumeList();
														StorageVolume mOTGVolume = null;
														String externalSDFilePath = gionee.os.storage.GnStorageManager
																.getInstance(
																		getActivity())
																.getExternalStoragePath();
														for (int i = 1; i < storageVolumes.length; i++) {
															String state = mStorageManager
																	.getVolumeState(storageVolumes[i]
																			.getPath()
																			.toString());
															if (Environment.MEDIA_MOUNTED
																	.equals(state)) {
																if ((null != externalSDFilePath)
																		&& !externalSDFilePath
																				.equals(storageVolumes[i]
																						.getPath()
																						.toString())) {
																	mOTGVolume = storageVolumes[i];
																} else {
																	mOTGVolume = storageVolumes[i];
																}
															}
														}
														Intent intent = new Intent(
																"com.android.settings.deviceinfo.storageFormatter");
														intent.putExtra(
																KEY_ERASE_FLAG,
																"otg");
														intent.putExtra(
																StorageVolume.EXTRA_STORAGE_VOLUME,
																mOTGVolume);
														getActivity()
																.startService(
																		intent);
														isUpdateOTG = true;
														// clear end
													}
												}).create().show();
							}
						}).create().show();
	}

	// Add end

	private final BroadcastReceiver mMediaScannerReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(UsbManager.ACTION_USB_STATE)) {
				boolean isUsbConnected = intent.getBooleanExtra(
						UsbManager.USB_CONNECTED, false);
				String usbFunction = mUsbManager.getDefaultFunction();
				for (StorageVolumePreferenceCategory category : mCategories) {
					category.onUsbStateChanged(isUsbConnected, usbFunction);
				}
			} else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
				for (StorageVolumePreferenceCategory category : mCategories) {
					category.onMediaScannerFinished();
				}
			}
		}
	};

	@Override
	public Dialog onCreateDialog(int id) {
		switch (id) {
		case DLG_CONFIRM_UNMOUNT:
			return new AuroraAlertDialog.Builder(getActivity())
					.setTitle(R.string.dlg_confirm_unmount_title)
					.setPositiveButton(R.string.dlg_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									doUnmount();
								}
							}).setNegativeButton(R.string.cancel, null)
					.setMessage(R.string.dlg_confirm_unmount_text).create();
		case DLG_ERROR_UNMOUNT:
			return new AuroraAlertDialog.Builder(getActivity())
					.setTitle(R.string.dlg_error_unmount_title)
					.setNeutralButton(R.string.dlg_ok, null)
					.setMessage(R.string.dlg_error_unmount_text).create();
		}
		return null;
	}

	private void doUnmount() {
		// Present a toast here
		Toast.makeText(getActivity(), R.string.unmount_inform_text,
				Toast.LENGTH_SHORT).show();
		IMountService mountService = getMountService();
		try {
			/*
			 * sLastClickedMountToggle.setEnabled(false);
			 * sLastClickedMountToggle
			 * .setTitle(getString(R.string.sd_ejecting_title));
			 * sLastClickedMountToggle
			 * .setSummary(getString(R.string.sd_ejecting_summary));
			 */
			getPreferenceScreen().removePreference(sdcardCategory); // add
			sdcardCategory = null;

			mountService.unmountVolume(sClickedMountPoint, true, false);
		} catch (RemoteException e) {
			// Informative dialog to user that unmount failed.
			showDialogInner(DLG_ERROR_UNMOUNT);
		}
	}

	private void showDialogInner(int id) {
		removeDialog(id);
		showDialog(id);
	}

	private boolean hasAppsAccessingStorage() throws RemoteException {
		IMountService mountService = getMountService();
		int stUsers[] = mountService.getStorageUsers(sClickedMountPoint);
		if (stUsers != null && stUsers.length > 0) {
			return true;
		}
		// TODO FIXME Parameterize with mountPoint and uncomment.
		// On HC-MR2, no apps can be installed on sd and the emulated internal
		// storage is not
		// removable: application cannot interfere with unmount
		/*
		 * ActivityManager am =
		 * (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
		 * List<ApplicationInfo> list = am.getRunningExternalApplications(); if
		 * (list != null && list.size() > 0) { return true; }
		 */
		// Better safe than sorry. Assume the storage is used to ask for
		// confirmation.
		return true;
	}

	/**
	 * //卸载SDcard
	 */
	private void unmount() {
		// Check if external media is in use.
		try {
			if (hasAppsAccessingStorage()) {
				// Present dialog to user
				showDialogInner(DLG_CONFIRM_UNMOUNT);
			} else {
				doUnmount();
			}
		} catch (RemoteException e) {
			// Very unlikely. But present an error dialog anyway
			Log.e(TAG, "Is MountService running?");
			showDialogInner(DLG_ERROR_UNMOUNT);
		}
	}

	private void mount() {
		IMountService mountService = getMountService();
		try {
			if (mountService != null) {
				mountService.mountVolume(sClickedMountPoint);
			} else {
				Log.e(TAG, "Mount service is null, can't mount");
			}
		} catch (RemoteException ex) {
			// Not much can be done
		}
	}

	private void onCacheCleared() {
		for (StorageVolumePreferenceCategory category : mCategories) {
			category.onCacheCleared();
		}
	}

	private static class ClearCacheObserver extends IPackageDataObserver.Stub {
		private final Memory mTarget;
		private int mRemaining;

		public ClearCacheObserver(Memory target, int remaining) {
			mTarget = target;
			mRemaining = remaining;
		}

		@Override
		public void onRemoveCompleted(final String packageName,
				final boolean succeeded) {
			synchronized (this) {
				if (--mRemaining == 0) {
					mTarget.onCacheCleared();
				}
			}
		}
	}

	/**
	 * Dialog to request user confirmation before clearing all cache data.
	 */
	public static class ConfirmClearCacheFragment extends DialogFragment {
		public static void show(Memory parent) {
			if (!parent.isAdded())
				return;

			final ConfirmClearCacheFragment dialog = new ConfirmClearCacheFragment();
			dialog.setTargetFragment(parent, 0);
			dialog.show(parent.getFragmentManager(), TAG_CONFIRM_CLEAR_CACHE);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Context context = getActivity();

			final AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(
					context);
			builder.setTitle(R.string.memory_clear_cache_title);
			builder.setMessage(getString(R.string.memory_clear_cache_message));

			builder.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							final Memory target = (Memory) getTargetFragment();
							final PackageManager pm = context
									.getPackageManager();
							final List<PackageInfo> infos = pm
									.getInstalledPackages(0);
							final ClearCacheObserver observer = new ClearCacheObserver(
									target, infos.size());
							for (PackageInfo info : infos) {
								pm.deleteApplicationCacheFiles(
										info.packageName, observer);
							}
						}
					});
			builder.setNegativeButton(android.R.string.cancel, null);

			return builder.create();
		}
	}
}
