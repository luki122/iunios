package com.privacymanage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.rtp.RtpStream;
import android.os.PowerManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Base64;

import com.aurora.config.AuroraConfig;
import com.aurora.dbutil.FileCategoryHelper.FileCategory;
import com.aurora.filemanager.FileExplorerTabActivity;
import com.aurora.tools.AESTools;
import com.aurora.tools.LogUtil;
import com.aurora.tools.FileInfo;
import com.aurora.tools.FileUtils;
import com.aurora.tools.ThreadPoolExecutorUtils;
import com.aurora.tools.Util;
import com.privacymanage.data.AidlAccountData;
import com.aurora.filemanager.R;

import libcore.io.Libcore;

public class PrivacyBroadcastReceiver extends BroadcastReceiver {
	private AidlAccountData accountOld;
	private static final String TAG = "PrivacyBroadcastReceiver";
	private List<AidlAccountData> list = new ArrayList<AidlAccountData>();
	private ThreadPoolExecutor executor;
	private PowerManager.WakeLock mWakeLock;

	public PrivacyBroadcastReceiver() {
		super();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// privacyInterface = (PrivacyInterface) context;
		if (mWakeLock == null) {
			PowerManager mPowerManger = (PowerManager) context
					.getSystemService(Context.POWER_SERVICE);
			mWakeLock = mPowerManger.newWakeLock(
					PowerManager.PARTIAL_WAKE_LOCK, TAG);
		}
		String action = intent.getAction();
//		LogUtil.d(TAG, "onReceive:" + action);
		executor = ThreadPoolExecutorUtils.getPrivacyUtils().getExecutor();
		final AidlAccountData accountData = intent
				.getParcelableExtra(AuroraConfig.KEY_ACCOUNT);
//		FileExplorerTabActivity.
		if (accountData == null) {
			LogUtil.e(TAG, "accountData is null");
			return;
		}

		if (action.equals(AuroraConfig.SWITCH_ACCOUNT_ACTION)) {
			if (list.contains(accountData)) {
				return;
			}
			list.add(accountData);// 入栈
			accountOld = Util.getPrivacyAccount(context, true);
			if (accountOld != null && accountOld.getAccountId() != 0) {
//				LogUtil.d(TAG, "oldId:" + accountOld.getAccountId()
//						+ " oldPath:" + accountOld.getHomePath());
				FileExplorerTabActivity.setStaticPrivacy(accountOld
						.getAccountId() != 0);
				FileExplorerTabActivity.setPrivacyHomePath(accountOld
						.getHomePath());
				if (!FileExplorerTabActivity.isStaticPrivacy()
						|| (accountData.getAccountId() != accountOld
								.getAccountId())) {
					encryption(FileExplorerTabActivity.getPrivacyHomePath());
				}
				if (!TextUtils.isEmpty(accountOld.getHomePath())) {
					sendBroadcastScan(AuroraConfig.ACTION_DIR_SCAN,
							FileExplorerTabActivity.getPrivacyMedioPath(
									FileCategory.Picture,
									accountOld.getHomePath()), context);
				}
			}
			list.remove(accountData);// 出栈
			Util.savePrivacyAccount(context, accountData);

		} else if (action.equals(AuroraConfig.DELETE_ACCOUNT_ACTION)) {
			boolean delete = intent.getBooleanExtra(AuroraConfig.KEY_DELETE,
					false);
			String path = accountData.getHomePath();
			if (accountData.getAccountId() != 0 && !TextUtils.isEmpty(path)) {
				if (delete) {
					deleteDirectory(path);
				} else {
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"yyyyMMddHHmmss");
					String mSDCardPath = gionee.os.storage.GnStorageManager
							.getInstance(context).getInternalStoragePath();
					String dPath = mSDCardPath + "/"
							+ context.getString(R.string.pri_path)
							+ dateFormat.format(new Date());
					String audioPath = mSDCardPath + "/"
							+ context.getString(R.string.phone_audios);
					moveDirectory(path, dPath, audioPath, context,
							accountData.getHomePath());
				}
			} else {
				LogUtil.e(TAG, "delete privacy error path::" + path + " id::"
						+ accountData.getAccountId());
			}
		}

	}

	private static final Object eLock = new Object();

	private void deleteDirectory(final String path) {
		if (executor == null) {
			LogUtil.e(TAG, "executor is null");
			return;
		}
		executor.execute(new Runnable() {

			@Override
			public void run() {
				try {
					FileUtils.deleteDirectory(new File(path));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private String baseAudioPath;

	private void moveDirectory(final String path, final String dPath,
			final String audioPath, final Context context, String homePath) {
		if (baseAudioPath == null) {
			baseAudioPath = homePath
					+ Base64.encodeToString((AuroraConfig.AUDIOID).getBytes(),
							Base64.URL_SAFE);
			baseAudioPath = Util.replaceBlank(baseAudioPath);
		}
		if (executor == null) {
			LogUtil.e(TAG, "executor is null");
			return;
		}
		executor.execute(new Runnable() {

			@Override
			public void run() {
				try {
					List<FileInfo> fileInfos = Util
							.getPathFilesByBase64All(path);
					// /storage/sdcard0/.privacy/1/YXVkaW8=/G.E.M.邓紫棋-龙卷风-龙卷风-128.mp3,
					// /storage/sdcard0/.privacy/1/YXVkaW8=
					if (fileInfos != null && fileInfos.size() > 0) {
						for (FileInfo fileInfo : fileInfos) {
							if ((fileInfo.filePath).trim().startsWith(
									baseAudioPath.trim())) {
								FileUtils.moveFileToDirectory(new File(
										fileInfo.filePath),
										new File(audioPath), true, null, false);
							} else {
								FileUtils.moveFileToDirectory(new File(
										fileInfo.filePath), new File(dPath),
										true, null, false);
							}

						}
					}
					Intent intent = new Intent();
					intent.setAction(AuroraConfig.ACTION_DIR_SCAN);
					intent.setData(Uri.fromFile(new File(dPath)));
					context.sendBroadcast(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void encryption(String path) {
		if (executor == null) {
			LogUtil.e(TAG, "executor is null");
			return;
		}
		executor.execute(new EncryptionRunnable(eLock, path));
	}

	private class EncryptionRunnable implements Runnable {
		private Object lock;
		private String homePath;

		public EncryptionRunnable(Object lock, String path) {
			super();
			this.lock = lock;
			this.homePath = path;
		}

		@Override
		public void run() {
			synchronized (lock) {
				try {
					if (mWakeLock != null) {
						mWakeLock.acquire();
					}
					final List<FileInfo> fileInfos = Util
							.getPathFiles(homePath);
					if (fileInfos != null) {

						for (FileInfo fileInfo : fileInfos) {
							if (fileInfo.fileName.contains(".")) {

								FileUtils.changeFile(fileInfo.filePath);

								try {
									String fileName = fileInfo.fileName;
									String name = getBase64String(fileName);
									while ((name.getBytes().length) > 240) {
										fileName = fileName.substring(0,
												fileName.length() / 2 - 1)
												+ fileName.substring(
														fileName.length() / 2,
														fileName.length());
										name = getBase64String(fileName);
									}
									String path = Util
											.getPathFromFilepath(fileInfo.filePath)
											+ File.separator + name;
									Libcore.os.rename(fileInfo.filePath,
											Util.replaceBlank(path));
								} catch (Exception e) {
									e.printStackTrace();
									LogUtil.e(
											TAG,
											" rename error "
													+ e.getLocalizedMessage());
								}

							}

						}

						// long end = SystemClock.currentThreadTimeMillis();
						// LogUtil.log(TAG, "Encryption ok time==" + (end -
						// start));
					}
					
				} catch (Exception e) {
					e.printStackTrace();
					LogUtil.e(TAG, " encode error " + e.getLocalizedMessage());
				}finally{
					if (mWakeLock != null) {
						mWakeLock.release();
					}
				}

			}

		}
	}

	private String getBase64String(String s) {
		return Util.replaceBlank(Base64.encodeToString(s.getBytes(),
				Base64.URL_SAFE));
	}

	private int length(String value) {
		int valueLength = 0;
		String chinese = "[\u0391-\uFFE5]";
		/* 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1 */
		for (int i = 0; i < value.length(); i++) {
			/* 获取一个字符 */
			String temp = value.substring(i, i + 1);
			/* 判断是否为中文字符 */
			if (temp.matches(chinese)) {
				/* 中文字符长度为2 */
				valueLength += 2;
			} else {
				/* 其他字符长度为1 */
				valueLength += 1;
			}
		}
		return valueLength;
	}

	/**
	 * 
	 * @param scan
	 * @param path
	 * @param context
	 */
	public void sendBroadcastScan(String scan, String path, Context context) {
		Intent intent = new Intent();
		intent.setAction(scan);
		intent.setData(Uri.fromFile(new File(path)));
		context.sendBroadcast(intent);
	}

}
