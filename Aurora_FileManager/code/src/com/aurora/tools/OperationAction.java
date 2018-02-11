package com.aurora.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.R.fraction;
import android.R.id;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Files.FileColumns;
import android.test.UiThreadTest;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;

import com.aurora.config.AuroraConfig;
import com.aurora.dbutil.FileCategoryHelper;
import com.aurora.dbutil.FileCategoryHelper.FileCategory;
import com.aurora.tools.MediaFile.MediaFileType;
import com.aurora.widget.InformationDialog;
import com.aurora.widget.TextInputDialog;
import com.aurora.widget.TextInputDialog.OnFinishListener;
import com.aurora.filemanager.FileExplorerTabActivity;
import com.aurora.filemanager.fragment.FileCategoryFragment;
import com.aurora.filemanager.fragment.FileViewFragment;
import com.aurora.filemanager.fragment.PictureCategoryFragment;
import com.aurora.filemanager.fragment.PictureFragment;
import com.aurora.filemanager.inter.OperationInterfaceLisenter;
import com.aurora.filemanager.R;
import com.aurora.lazyloader.PriorityThreadFactory;

import android.provider.Downloads;

public class OperationAction {

	private OperationInterfaceLisenter operationInterface;

	private InformationDialogDismissLisenter dismissLisenter;

	public interface InformationDialogDismissLisenter {
		void dialogDismiss();
	}

	/**
	 * @param dismissLisenter
	 *            the dismissLisenter to set
	 */
	public void setDismissLisenter(InformationDialogDismissLisenter dismissLisenter) {
		this.dismissLisenter = dismissLisenter;
	}

	private TextInputDialog textInputDialog;

	public TextInputDialog getTextInputDialog() {
		return textInputDialog;
	}

	public void setTextInputDialog(TextInputDialog textInputDialog) {
		this.textInputDialog = textInputDialog;
	}

	private int deleteFolderCount = 0;

	/**
	 * @param deleteFolderCount
	 *            the deleteFolderCount to set
	 */
	public void setDeleteFolderCount(int deleteFolderCount) {
		this.deleteFolderCount = deleteFolderCount;
	}

	/**
	 * @param OperationInterfaceLisenter
	 *            the OperationInterfaceLisenter to set
	 */
	public void setOperationInterfaceLisenter(OperationInterfaceLisenter operationInterface) {
		this.operationInterface = operationInterface;
	}

	private Context context;
	private static final String TAG = "OperationAction";

	public static enum Operation {
		cut, copy, del, noOperation, rename
	}

	private FileExplorerTabActivity activity;
	private List<FileInfo> selectFiles;

	public static Operation lastOperation;

	private AuroraProgressDialog mDialog;
	private AuroraAlertDialog auroraAlertDialog;
	private int size = 0;
	private FileCategoryHelper fileCategoryHelper;
	private static final int maxProgress = 100;
	private static final int TOASTSHOW = 101;

	/**
	 * 关闭所有对话框 和进度条
	 */
	public void mDialogDismiss() {
		if (mDialog != null) {
			mDialog.dismiss();
		}
		if (auroraAlertDialog != null) {
			auroraAlertDialog.dismiss();
		}
	}

	/**
	 * @param lastOperation
	 *            the lastOperation to set
	 */
	public static void setLastOperation(Operation lastOperation) {
		 LogUtil.d(TAG, "setLastOperation==" + lastOperation);
		OperationAction.lastOperation = lastOperation;
	}

	/**
	 * @return the lastOperation
	 */
	public static Operation getLastOperation() {
		if (lastOperation == null) {
			lastOperation = Operation.noOperation;
		}
		return lastOperation;
	}

	// private boolean isCopyOrCut = false;

	private static final int showError = 102;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case maxProgress:
				if (mDialog == null) {
					return;
				}
				if (!mDialog.isShowing()) {
					return;
				}
				mDialog.setProgress(msg.arg1);
				break;
			case TOASTSHOW:
				if (msg.arg1 == maxProgress) {
					// LogUtil.elog(TAG, "mDialog.dismiss()");
					mDialog.dismiss();// 调用接口后 消失
				}
				if (msg.obj == null) {
					ToastUtils.showTast(context, context.getResources().getString(msg.arg2));

				} else {
					ToastUtils.showTast(context, context.getResources().getString(msg.arg2, msg.obj.toString()));
				}
				break;
			case showError:
				ToastUtils.showTast(context, context.getResources().getString(R.string.del_file, msg.obj.toString()));
				break;

			default:
				break;
			}

		}
	};

	private PowerManager.WakeLock mWakeLock;

	public OperationAction(Context context) {
		super();
		this.context = context;
		activity = (FileExplorerTabActivity) context;
		fileCategoryHelper = FileCategoryHelper.getInstance(context);
		inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		PowerManager mPowerManger = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
		mWakeLock = mPowerManger.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
	}

	public PowerManager.WakeLock getmWakeLock() {
		return mWakeLock;
	}

	public OperationAction(Context context, boolean scan) {
		super();
		this.context = context;
	}

	/**
	 * @return the selectFiles
	 */
	public List<FileInfo> getSelectFiles() {
		return selectFiles;
	}

	/**
	 * @param selectFiles
	 *            the selectFiles to set modify by Jxh 2014-8-12 synchronized
	 */
	public synchronized void setSelectFiles(List<FileInfo> selectFiles) {
		this.selectFiles = selectFiles;
	}

	/**
	 * 清理选择数据
	 */
	public void clearSelectFiles() {
		if (selectFiles != null) {
			// LogUtil.elog(TAG, "selectFiles is empty now");
			selectFiles.clear();
		}
	}

	private InformationDialog dialog;

	public InformationDialog getDialog() {
		return dialog;
	}

	public void setDialog(InformationDialog dialog) {
		this.dialog = dialog;
	}

	private String parenPath;
	private InputMethodManager inputMethodManager;

	public void createNewFolder(String parentPath) {
		String name = activity.getString(R.string.newfolder);
		String title = activity.getString(R.string.create_newfolder);
		StringBuffer sb = new StringBuffer(parentPath);
		this.parenPath = parentPath;
		if (parentPath.endsWith(File.separator)) {
			sb.append(name);
		} else {
			sb.append(File.separator).append(name);
		}
		File newFile = new File(sb.toString());
		int i = 1;
		while (newFile.exists()) {
			newFile = new File(sb.toString() + i);
			i++;
		}
		textInputDialog = null;
		textInputDialog = new TextInputDialog(activity, title, newFile.getName(), createFolderlistener, inputMethodManager);
		textInputDialog.show();
		Util.showInputMethod(activity);

	}

	/**
	 * 新建文件夹监听
	 */
	private OnFinishListener createFolderlistener = new OnFinishListener() {

		@Override
		public boolean onFinish(String text) {
			return doCreateFolder(text);
		}
	};

	private List<FileInfo> addFileInfos = new ArrayList<FileInfo>();
	private static final String format = "format";
	private static final String external = "external";
	private static final int formatstyle = 12289;

	// add by Jxh 2014-8-11 begin
	private boolean isCreateFolder;

	public boolean isCreateFolder() {
		return isCreateFolder;
	}

	public void setCreateFolder(boolean isCreateFolder) {
		this.isCreateFolder = isCreateFolder;
	}

	// add by Jxh 2014-8-11 end

	/**
	 * 新建文件夹
	 * @param name
	 * @return
	 */
	private boolean doCreateFolder(String name) {
		if (TextUtils.isEmpty(name)) {
			return false;
		}
		File file = new File(parenPath);
		if (!file.canWrite()) {
			LogUtil.e(TAG, "file can not write " + file.getPath());
			ToastUtils.showTast(activity, R.string.mkdir_no_permission);
			return false;
		}
		if (Util.getFreeSpace(parenPath) > 0) {
			String newFolder = Util.makePath(parenPath, name.trim());
			file = new File(newFolder);
			int i = 1;
			while (file.exists()) {
				file = new File(newFolder + i);
				i++;
			}
			if (file.mkdirs()) {
				ContentValues cv = new ContentValues();
				cv.put(MediaStore.Files.FileColumns.DATA, file.getPath());
				cv.put(format, formatstyle);// 格式化为文件夹
				activity.getContentResolver().insert(MediaStore.Files.getContentUri(external), cv);
				if (operationInterface != null) {
					addFileInfos.clear();
					FileInfo fileInfo = Util.getFileInfo(file, false);
					if (fileInfo != null) {
						addFileInfos.add(fileInfo);
					}
					// add by Jxh 2014-8-11 begin
					setCreateFolder(true);
					// add by Jxh 2014-8-11 end
					operationInterface.completeRefresh(addFileInfos, null);
				}
				return true;
			} else {
				ToastUtils.showTast(context, R.string.fail_to_create_folder);
			}

		} else {
			ToastUtils.showTast(context, R.string.has_no_free_memory);
		}
		return false;

	}

	/**
	 * 文件信息 包括文件夹信息
	 */
	public void onOperationInfo() {
		if (selectFiles.size() == 0) {
			LogUtil.e(TAG, "selectFiles is empty");
			return;
		}
		FileInfo fileInfo = selectFiles.get(0);
		if (fileInfo == null) {
			LogUtil.e(TAG, "fileInfo is null");
			return;
		}
		onOperationInfo(fileInfo, false);
	}

	// add by Jxh 2014-8-26 begin
	public void onOperationInfo(FileInfo allFileInfo, boolean isSearch) {
		setStatisticsCount(18, true);
		dialog = new InformationDialog(context, allFileInfo, isSearch);
		dialog.show();
		dialog.setOnDismissListener(ondismissListener);
		dialog.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (KeyEvent.KEYCODE_BACK == keyCode) {
					activity.setSearchKey("");
					activity.setSearchPostion(0);
				}
				return false;
			}
		});
	}

	// add by Jxh 2014-8-26 end
	/**
	 * 详情Dismiss监听
	 */
	private OnDismissListener ondismissListener = new OnDismissListener() {

		@Override
		public void onDismiss(DialogInterface dialog) {
			if (dismissLisenter != null) {
				dismissLisenter.dialogDismiss();
			}
		}
	};

	/**
	 * 重命名操作
	 */
	public void renameOperation() {
		if (selectFiles.size() == 0) {
			LogUtil.e(TAG, "selectFiles is empty");
			return;
		}
		if (textInputDialog != null) {
			textInputDialog = null;
		}
		textInputDialog = new TextInputDialog(selectFiles.get(0), activity, activity.getString(R.string.operation_rename), selectFiles.get(0).fileName, finishListener, inputMethodManager);
		textInputDialog.show();
		activity.beforeDisMissAuroraOperationBarMoreMenu();
		Util.showInputMethod(activity);
	}

	/**
	 * 重命名完成监听
	 */
	private OnFinishListener finishListener = new OnFinishListener() {

		@Override
		public boolean onFinish(String text) {
			FileInfo fileInfo = selectFiles.get(0);
			return doRename(fileInfo, text.trim());
		}
	};

	private Dialog renameDialog;

	/**
	 * 重命名
	 * @param fileInfo
	 * @param newName
	 * @return
	 */
	private boolean doRename(FileInfo fileInfo, String newName) {
		if (fileInfo == null) {
			return false;
		}
		if (TextUtils.isEmpty(newName) || fileInfo.fileName.equals(newName)) {
			return false;
		}

		File file = new File(fileInfo.filePath);
		String newPath = Util.makePath(Util.getPathFromFilepath(fileInfo.filePath), newName);
		File newFile = new File(newPath);
		if (newFile.exists()) {
			ToastUtils.showTast(activity, R.string.rename_exist);
			return false;
		}
		// LogUtil.elog(TAG, Util.getExtFromFilename(fileInfo.fileName) + "?="
		// + Util.getExtFromFilename(newName));
		setLastOperation(Operation.rename);
		// LogUtil.elog(TAG, "Operation.rename");
		// LogUtil.elog(TAG,
		// "" + file.lastModified() + "<<<<<" + newFile.lastModified()
		// + "+++");
		if ((!fileInfo.IsDir) && !(Util.getExtFromFilename(fileInfo.fileName).equals(Util.getExtFromFilename(newName)))) {
			renameDialog = alertRenameExtensionDialog(file, newFile, fileInfo);
			renameDialog.show();

		} else if (file.renameTo(newFile)) {
			FileInfo newFileInfo = Util.GetFileInfo(newFile.getAbsolutePath());
			newFileInfo.Count = fileInfo.Count;
			newFileInfo.fileSize = fileInfo.fileSize;
			// newFileInfo.ModifiedDate = new Date().getTime();
			/*
			 * newFileInfo.order = Util.getSpell(newFileInfo.fileName);// //
			 * 4.2获取字母14427 10889ms >4.2 6621ms if (newFileInfo.order != null &&
			 * !newFileInfo.order.equals("")) { newFileInfo.isLetterOrDigit =
			 * Character .isLetterOrDigit(newFileInfo.order.charAt(0));//
			 * 判断是否一个数字 }
			 */

			renameSetDb(file, newFile);
			if (fileInfo.IsDir) {
				sendBroadcastScan(AuroraConfig.ACTION_DIR_SCAN, newPath);
			} else {
				renameDownload(newFile.getPath(), file.getPath());
			}
			if (operationInterface != null && newFileInfo != null) {
				operationInterface.renameComplete(fileInfo, newFileInfo);
			}
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					// LogUtil.elog(TAG,
					// "rename==setLastOperation(Operation.noOperation)");
					setLastOperation(Operation.noOperation);

				}
			}, 500);

			setStatisticsCount(19, true);
			return true;
		} else {
			LogUtil.e(TAG, "rename error");
		}
		setLastOperation(Operation.noOperation);
		return false;
	}

	/**
	 * @param position
	 * @param isList
	 */
	private void setStatisticsCount(int position, boolean isList) {
		if (activity == null || fileCategoryHelper == null) {
			return;
		}
		if ((activity.getCurrentFragment() instanceof PictureCategoryFragment && !isList)
				|| ((activity.getCurrentFragment() instanceof FileCategoryFragment || activity.getCurrentFragment() instanceof FileViewFragment) && isList)) {
			fileCategoryHelper.updateStatistics(fileCategoryHelper.getStatisticsContentValues(AuroraConfig.statisticsTag.get(position)));
		}
	}

	/**
	 * 重命名修改数据库
	 * @param old
	 * @param newfile
	 */
	private void renameSetDb(File old, File newfile) {
		ContentValues cv = new ContentValues(3);
		cv.put(MediaStore.Files.FileColumns.DATA, newfile.getPath());
		cv.put(MediaStore.Files.FileColumns.DISPLAY_NAME, newfile.getName());
		cv.put(MediaStore.Files.FileColumns.TITLE, Util.getNameFromFilename(newfile.getName()));
		// cv.put(FileCategoryHelper.DATETAKEN, newfile.lastModified());
		// cv.put(MediaStore.Files.FileColumns.DATE_MODIFIED,
		// newfile.lastModified());
		int update = 0;
		try {
			update = activity.getContentResolver().update(MediaStore.Files.getContentUri(external), cv, MediaStore.Files.FileColumns.DATA + "= ?", new String[] { old.getPath() });
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.e(TAG, "renameSetDb error " + e.getMessage() + " path =" + old.getPath());
		} finally {
			if (update == 0) {
				if (old.isFile()) {
					sendBroadcastScan(AuroraConfig.ACTION_FILE_SCAN, old.getAbsolutePath());
					sendBroadcastScan(AuroraConfig.ACTION_FILE_SCAN, newfile.getAbsolutePath());
				}
			}
		}
	}

	/**
	 * The method creates an alert delete dialog
	 * @param args
	 *            argument, the boolean value who will indicates whether the
	 *            selected files just only one. The prompt message will be
	 *            different.
	 * @return a dialog
	 */
	protected aurora.app.AuroraAlertDialog alertRenameExtensionDialog(final File file, final File newFile, final FileInfo fileInfo) {
		aurora.app.AuroraAlertDialog.Builder builder = new aurora.app.AuroraAlertDialog.Builder(activity);
		String alertMsg = activity.getString(R.string.msg_rename_ext);

		builder.setTitle(R.string.warning_rename_ext).setMessage(alertMsg).setPositiveButton(activity.getString(R.string.rename_ext), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				if (file.renameTo(newFile)) {
					FileInfo newFileInfo = Util.GetFileInfo(newFile.getAbsolutePath());
					setLastOperation(Operation.rename);
					if (fileInfo.IsDir) {
						sendBroadcastScan(AuroraConfig.ACTION_DIR_SCAN, newFile.getAbsolutePath());
						// sendBroadcastScan(
						// AuroraConfig.ACTION_DIR_SCAN,
						// Util.getPathFromFilepath(newFile
						// .getAbsolutePath()));
					} else {
						renameSetDb(file, newFile);
						// sendBroadcastScan(
						// AuroraConfig.ACTION_FILE_SCAN,
						// newFile.getAbsolutePath());//
						// Util.getPathFromFilepath(
					}
					/*
					 * newFileInfo.order = Util
					 * .getSpell(newFileInfo.fileName);// // 4.2获取字母14427
					 * 10889ms >4.2 6621ms if (newFileInfo.order != null &&
					 * !newFileInfo.order.equals("")) {
					 * newFileInfo.isLetterOrDigit = Character
					 * .isLetterOrDigit(newFileInfo.order .charAt(0));//
					 * 判断是否一个数字 }
					 */
					if (operationInterface != null && newFileInfo != null) {
						operationInterface.renameComplete(fileInfo, newFileInfo);
					}
					handler.postDelayed(new Runnable() {

						@Override
						public void run() {
							// LogUtil
							// .elog(TAG,
							// "rename==setLastOperation(Operation.noOperation)");
							setLastOperation(Operation.noOperation);

						}
					}, 500);
					setStatisticsCount(19, true);
				}
			}
		}).setNegativeButton(activity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				setLastOperation(Operation.noOperation);
			}
		});
		return builder.create();
	}

	/**
	 * 分享
	 */
	public void shareOperation() {
		if (selectFiles.size() > Integer.valueOf(context.getString(R.string.share_max))) {
			ToastUtils.showTast(context, R.string.share_too_much);
			return;
		}
		Intent intent = IntentBuilder.buildSendFile(selectFiles);
		try {
			setStatisticsCount(17, true);
			context.startActivity(Intent.createChooser(intent, context.getString(R.string.send_file)));
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.e(TAG, "no app share " + e.getMessage());
		}
	}

	/**
	 * 通过线程判断是否处于复制或者剪切状态
	 * @return
	 */
	public boolean isCutOrCopy() {
		if (thread != null && thread.isAlive()) {
			return true;
		}
		return false;
	}

	/**
	 * 判断两个路径是否是同一个存储器 add by JXH 2014-8-15
	 * @param f
	 * @param d
	 * @return
	 */
	public boolean isSomeRootPath(String f, String d) {
		if (activity == null) {
			LogUtil.e(TAG, "activity is null");
			return false;
		}
		List<String> root = activity.getStoragesStrings();
		if (root != null && !TextUtils.isEmpty(f) && !TextUtils.isEmpty(d)) {
			for (int i = 0; i < root.size(); i++) {
				if (f.startsWith(root.get(i)) && d.startsWith(root.get(i))) {
					return true;
				}
			}
		}
		return false;
	}

	// add by jxh 2014-6-18 BUG 5833 begin
	private boolean isScanPath;

	// add by jxh 2014-6-18 BUG 5833 end

	// add by Jxh 2014-9-10 添加隐私文件 begin
	private boolean isAddPri;

	public synchronized void addPrivacyFiles(FileCategory category) {
		String home = FileExplorerTabActivity.getPrivacyHomePath();
		if (TextUtils.isEmpty(home)) {
			LogUtil.e(TAG, "PrivacyHomePath is null");
			return;
		}

		isAddPri = true;
		moveFileOrDir(FileExplorerTabActivity.getPrivacyMedioPath(category));
	}

	private List<FileInfo> removeFiles = new ArrayList<FileInfo>();

	// add by Jxh 2014-9-10 添加隐私文件 end
	/**
	 * 剪切文件
	 * @param destPath
	 */
	public void moveFileOrDir(final String destPath) {
		if (selectFiles.size() == 0) {
			LogUtil.e(TAG, "selectFiles is empty");
			return;
		}
		mDialog = new AuroraProgressDialog(context);
		mDialog.setProgressStyle(aurora.app.AuroraProgressDialog.STYLE_HORIZONTAL);
		mDialog.setMax(maxProgress);
		mDialog.setTitle(R.string.operation_moving);
		mDialog.setOnKeyListener(keylistener);
		mDialog.show();
		thread = new Thread(new Runnable() {

			@Override
			public void run() {
				// modfiy by Jxh 2014-8-12 synchronized
				synchronized (selectFiles) {
					if (mWakeLock != null) {
						mWakeLock.acquire();
					}
					size = selectFiles.size();
					// LogUtil.log(TAG, "move size==" + size);
					successInfos.clear();
					removeFiles.clear();
					File destDir = new File(destPath);
					int oldCount = 0;
					long total = 0;
					long freeSpace = destDir.getFreeSpace();
					if (isSomeRootPath(selectFiles.get(0).filePath, destPath)) {// 相同存储器
						total = selectFiles.size();
					} else {// 不同存储器 需计算文件大小
						total = Util.getAllSize(selectFiles);
						if (freeSpace <= total) {
							LogUtil.e(TAG, "moveFileOrDir no free memory");
							Message message = handler.obtainMessage();
							message.what = TOASTSHOW;
							message.arg2 = R.string.has_no_free_memory;
							handler.sendMessage(message);
							handler.post(new Runnable() {

								@Override
								public void run() {
									mDialogDismiss();
								}
							});
							if (mWakeLock != null) {
								mWakeLock.release();
							}
							return;
						}

					}
					FileTools.initValues(total, thread);
					for (int i = 0; i < size; i++) {
						if (thread.isInterrupted()) {
							break;
						}
						// modfiy by Jxh 2014-8-12 begin
						FileInfo fileInfo = null;

						try {
							fileInfo = selectFiles.get(i);
							File file = new File(fileInfo.filePath);
							// modfiy by Jxh 2014-8-12 end
							if (Util.getPathFromFilepath(file.getPath()).equals(destPath)) {
								successInfos.add(fileInfo);
								removeFiles.add(fileInfo);

							} else {
								FileInfo fileInfo2 = FileTools.moveToDirectory(file, destDir, handler);
								if (fileInfo2 != null) {
									successInfos.add(fileInfo2);
									isScanPath = true;
									removeFiles.add(fileInfo);

								}

							}
						} catch (Exception e) {
							e.printStackTrace();
							LogUtil.e(TAG, "moveFileOrDir erorr" + e.getMessage());
							Message message = handler.obtainMessage();
							message.what = TOASTSHOW;
							if (destDir.exists()) {

								if (freeSpace <= 0) {
									message.arg2 = R.string.has_no_free_memory;
									handler.sendMessage(message);
									break;

								} else {
									message.arg2 = R.string.move_file;
									// modfiy by Jxh 2014-8-12 begin
									if (fileInfo != null) {
										message.obj = fileInfo.fileName;
									} else {
										message.obj = "";
									}
									// modfiy by Jxh 2014-8-12 end
									handler.sendMessage(message);
								}
							} else {
								message.arg2 = R.string.move_file;
								if (fileInfo != null) {
									message.obj = fileInfo.fileName;
								} else {
									message.obj = "";
								}
								handler.sendMessage(message);
							}

						}
					}
					setStatisticsCount(16, true);
					if (operationInterface != null) {

						handler.post(new Runnable() {

							@Override
							public void run() {
								operationInterface.completeRefresh(successInfos, removeFiles);
								mDialogDismiss();
								LogUtil.e(TAG, "cut complete ");
							}
						});
						if (isAddPri) {
							setLastOperation(Operation.cut);
							fileCategoryHelper.delete(removeFiles);
						}
						if (successInfos.size() > 0 || isScanPath) {
							isScanPath = false;
							if (!isAddPri) {
								setLastOperation(Operation.cut);
								sendBroadcastScan(AuroraConfig.ACTION_DIR_SCAN, destPath);
							}
						}
					}
					// add by Jxh 2014-8-27 begin
					else {
						mDialogDismiss();
						if (successInfos.size() > 0 || isScanPath) {
							isScanPath = false;
							if (!isAddPri) {
								setLastOperation(Operation.cut);
								sendBroadcastScan(AuroraConfig.ACTION_DIR_SCAN, destPath);
							}
						}
					}
					// add by Jxh 2014-8-27 end
					if (isAddPri) {
						activity.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								activity.beforeDisMissAuroraOperationBarMoreMenu();
								activity.unDoAllOperation();
							}
						});
						mDialogDismiss();
						isAddPri = false;
						doSetLastOperation();
					} else if (activity.isDoPrivacy()) {
						// LogUtil.elog(TAG, "isDoPrivacy");
						activity.QueryPrivacy();
						activity.setDoPrivacy(false);
					}
				}
				if (mWakeLock != null) {
					mWakeLock.release();
				}
			}
		});
		thread.setPriority(Thread.NORM_PRIORITY - 1);
		thread.start();

	}

	private void doSetLastOperation() {
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// LogUtil.log(TAG, "setLastOperation noOperation");
				setLastOperation(Operation.noOperation);
			}
		}, 1000);
	}

	// 公用线程
	private Thread thread;

	private List<FileInfo> successInfos = new ArrayList<FileInfo>();

	/**
	 * 复制文件 文件夹
	 * @param destPath
	 */
	public void copyFileOrDir(final String destPath) {
		if (selectFiles.size() == 0) {
			LogUtil.e(TAG, "selectFiles is empty");
			return;
		}
		mDialog = new AuroraProgressDialog(context);
		mDialog.setProgressStyle(aurora.app.AuroraProgressDialog.STYLE_HORIZONTAL);
		mDialog.setMax(maxProgress);
		mDialog.setTitle(R.string.operation_pasting);
		mDialog.setOnKeyListener(keylistener);
		mDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				LogUtil.d(TAG, "------mDialog onDismiss:"+mDialog.isShowing());
			}
		});
		mDialog.show();
		thread = new Thread(new Runnable() {

			@Override
			public void run() {
				// modfiy by Jxh 2014-8-12 synchronized
				synchronized (selectFiles) {
					if (mWakeLock != null) {
						mWakeLock.acquire();
					}
					size = selectFiles.size();
					File destDir = new File(destPath);
					int oldCount = 0;
					successInfos.clear();
					long total = Util.getAllSize(selectFiles);
					// LogUtil.log(TAG, "total==" + total);
					long freeSpace = destDir.getFreeSpace();
					if (freeSpace <= total) {
						LogUtil.e(TAG, "no free memory");
						Message message = handler.obtainMessage();
						message.what = TOASTSHOW;
						message.arg2 = R.string.has_no_free_memory;
						handler.sendMessage(message);
						handler.post(new Runnable() {

							@Override
							public void run() {
								selectFiles.clear();
								mDialogDismiss();
							}
						});
						if (mWakeLock != null) {
							mWakeLock.release();
						}
						return;
					}
					FileTools.initValues(total, thread);
					for (int i = 0; i < size; i++) {
						if (thread.isInterrupted()) {
							LogUtil.d(TAG, "----isInterrupted");
							break;
						}
						// modfiy by Jxh 2014-8-12 begin
						FileInfo fileInfo = null;
						try {
							fileInfo = selectFiles.get(i);
							File file = new File(fileInfo.filePath);
							// modfiy by Jxh 2014-8-12 end
							FileInfo fileInfo2;
							fileInfo2 = FileTools.copyAllToDirectory(file, destDir, handler);
							if (fileInfo2 != null) {
								successInfos.add(fileInfo2);
							}
						} catch (Exception e) {
							e.printStackTrace();
							LogUtil.e(TAG, "copyFileOrDir erorr" + e.getMessage() + "  " + destDir.getFreeSpace());
							Message message = handler.obtainMessage();
							message.what = TOASTSHOW;
							if (destDir.exists()) {

								if (freeSpace <= 0) {
									message.arg2 = R.string.has_no_free_memory;
									handler.sendMessage(message);
									break;

								} else {
									message.arg2 = R.string.copy_file;
									// modfiy by Jxh 2014-8-12 begin
									if (fileInfo != null) {
										message.obj = fileInfo.fileName;
									} else {
										message.obj = "";
									}
									// modfiy by Jxh 2014-8-12 end
									handler.sendMessage(message);
								}
							} else {
								message.arg2 = R.string.copy_file;
								// modfiy by Jxh 2014-8-12 begin
								if (fileInfo != null) {
									message.obj = fileInfo.fileName;
								} else {
									message.obj = "";
								}
								// modfiy by Jxh 2014-8-12 end
								handler.sendMessage(message);
							}
						}
					}
					setStatisticsCount(15, true);
					if (operationInterface != null) {
						handler.post(new Runnable() {

							@Override
							public void run() {
								operationInterface.completeRefresh(successInfos, null);
								LogUtil.e(TAG, destPath + " copy Complete " + isAddPri);
							}
						});
						if (successInfos.size() > 0) {
							sendBroadcastScan(AuroraConfig.ACTION_DIR_SCAN, destPath);
						}
					}
				}
				if (mWakeLock != null) {
					mWakeLock.release();
				}
			}
		});
		thread.setPriority(Thread.NORM_PRIORITY - 1);
		thread.start();

	}

	/**
	 * 快速删除
	 * @param fileInfo
	 */
	public boolean fastDeleteFileOrDir(final FileInfo fileInfo) {
		if (fileInfo == null || fileInfo.filePath == null) {
			// 需要刷新数据
			LogUtil.e(TAG, "fileInfo==null ");
			return false;
		}
		final File file = new File(fileInfo.filePath);
		if (file == null) {
			// 数据库里面存在数据，文件系统不存在
			LogUtil.e(TAG, "file==null ");
			return false;
		}
		final long[] dbIds = new long[1];
		LogUtil.d(TAG, "----fileInfo:"+fileInfo.toString());
		if (!fileInfo.IsDir || (fileInfo.IsDir && fileInfo.Count == 0)) {
			setLastOperation(Operation.del);
			boolean del = FileTools.deleteQuietly(file, handler);
			if (del) {
				long id = fileInfo.dbId;
				if (id == 0) {
					id = getDbId(fileInfo.filePath);
					LogUtil.e(TAG, "get db id =" + id);
				}
				dbIds[0] = id;
				fileCategoryHelper.delete(dbIds);
			}
			return del;
		} else {
			if (isCutOrCopy()) {
				ToastUtils.showTast(activity, R.string.wait_a_moment);
				return false;
			}

			String message = context.getString(R.string.operation_delete_single_folder_confirm_message);//.setMessage(message)
			auroraAlertDialog = new AuroraAlertDialog.Builder(context).setTitle(message).setCancelable(false).setOnKeyListener(keylistener)
					.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							auroraAlertDialog.dismiss();

							mDialog = new AuroraProgressDialog(context);
							mDialog.setProgressStyle(aurora.app.AuroraProgressDialog.STYLE_HORIZONTAL);
							mDialog.setMax(maxProgress);
							mDialog.setTitle(R.string.operation_deleting);
							mDialog.setOnKeyListener(keylistener);
							mDialog.show();

							thread = new Thread(new Runnable() {

								@Override
								public void run() {
									// FileUtils.thread = thread;
									FileTools.initValues(1, thread);
									setLastOperation(Operation.del);
									boolean del = FileTools.deleteQuietly(file, handler);
									if (del) {
										long id = fileInfo.dbId;
										if (id == 0) {
											id = getDbId(fileInfo.filePath);
											LogUtil.e(TAG, "get db id =" + id);
										}
										dbIds[0] = id;
										sendBroadcastScan(AuroraConfig.ACTION_DIR_SCAN, fileInfo.filePath);
										fileCategoryHelper.delete(dbIds);
										handler.post(new Runnable() {

											@Override
											public void run() {
												operationInterface.folderDelet(fileInfo);
												OperationAction.setLastOperation(Operation.noOperation);
											}
										});
									}
									Message msg = handler.obtainMessage();
									msg.what = maxProgress;
									msg.arg1 = maxProgress;
									handler.sendMessage(msg);
								}
							});
							thread.setPriority(Thread.NORM_PRIORITY - 1);
							thread.start();

						}
					}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							auroraAlertDialog.dismiss();
							operationInterface.folderDelet(null);
						}
					}).create();

			auroraAlertDialog.show();
			return false;
		}

	}

	private boolean isInterrupted = false;

	/**
	 * 删除处理
	 * @param isShow
	 */
	public void deleteFileOrDirBefor(boolean isShow) {

		String message;

		if (deleteFolderCount != 0) {
			if (deleteFolderCount > 1) {
				message = context.getString(R.string.operation_delete_double_folder_confirm_message);
				message = String.format(message, deleteFolderCount);
			} else {
				message = context.getString(R.string.operation_delete_single_folder_confirm_message);
			}
			deleteFolderCount = 0;

		} else if (selectFiles != null && selectFiles.size() == 1) {
			if (selectFiles.get(0).IsDir) {
				message = context.getString(R.string.operation_delete_single_folder_confirm_message);
			} else {
				message = context.getString(R.string.operation_delete_single_file_confirm_message);
			}

		} else {
			int dirCount = 0;
			int fileCount = 0;
			for (FileInfo fileInfo : selectFiles) {
				if (fileInfo.IsDir) {
					dirCount++;
				} else {
					fileCount++;
				}
			}
			if (dirCount == 0) {

				if (fileCount > 1) {
					message = context.getString(R.string.operation_delete_double_file_confirm_message);
					message = String.format(message, fileCount);
				} else {
					message = context.getString(R.string.operation_delete_single_file_confirm_message);
				}
			} else if (fileCount == 0) {
				if (dirCount > 1) {
					message = context.getString(R.string.operation_delete_double_folder_confirm_message);
					message = String.format(message, dirCount);
				} else {
					message = context.getString(R.string.operation_delete_single_folder_confirm_message);
				}
			} else {
				if (dirCount == 1 && fileCount == 1) {
					message = context.getString(R.string.operation_delete_mix_confirm_message);
					message = String.format(message, fileCount, dirCount);
				} else if (dirCount == 1) {
					message = context.getString(R.string.operation_delete_mix_confirm_messages_s);
					message = String.format(message, fileCount, dirCount);
				} else if (fileCount == 1) {
					message = context.getString(R.string.operation_delete_mix_confirm_message_s);
					message = String.format(message, fileCount, dirCount);
				} else {
					message = context.getString(R.string.operation_delete_mix_confirm_messagess);
					message = String.format(message, fileCount, dirCount);
				}

			}

		}
//		.setTitle(R.string.operation_delete)
		auroraAlertDialog = new AuroraAlertDialog.Builder(context).setTitle(message).setCancelable(false).setOnKeyListener(keylistener)
				.setPositiveButton(R.string.operation_delete, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						auroraAlertDialog.dismiss();
						setStatisticsCount(10, false);

						deleteFolderCount = 0;
						if (operationInterface == null) {
							return;
						}

						if (selectFiles.size() == 0) {
							return;
						}
						size = selectFiles.size();
						mDialog = new AuroraProgressDialog(context);
						mDialog.setProgressStyle(aurora.app.AuroraProgressDialog.STYLE_HORIZONTAL);
						mDialog.setMax(100);
						mDialog.setTitle(R.string.operation_deleting);
						mDialog.setOnKeyListener(keylistener);
						mDialog.show();

						if (fileInfos == null) {
							fileInfos = new ArrayList<FileInfo>();
						}
						fileInfos.clear();

						thread = new Thread(new Runnable() {

							@Override
							public void run() {
								// modfiy by Jxh 2014-8-12 synchronized
								synchronized (selectFiles) {
									int oldCount = 0;
									long[] dbIds = new long[size];
									int count = Util.getCounts(selectFiles);
									// LogUtil.log(TAG,
									// "count=="+count);
									FileTools.initValues(count, thread);
									for (int i = 0; i < size; i++) {
										if (thread.isInterrupted()) {
											LogUtil.e(TAG, "del isInterrupted1");
											break;
										}
										FileInfo fileInfo = null;
										try {
											fileInfo = selectFiles.get(i);
											File file = new File(fileInfo.filePath);
											setLastOperation(Operation.del);
											boolean del = FileTools.deleteQuietly(file, handler);
											if (del) {
												long id = fileInfo.dbId;
												if (id == 0) {
													id = getDbId(fileInfo.filePath);
													LogUtil.e(TAG, "get db id =" + id);
												}

												dbIds[i] = id;
												fileInfos.add(fileInfo);
											}
											LogUtil.d(TAG, "del::" + fileInfo.filePath + " " + del);

										} catch (Exception e) {
											e.printStackTrace();
											LogUtil.e(TAG, "del error " + (e.getLocalizedMessage() == null ? "" : e.getLocalizedMessage()));
											Message msg = handler.obtainMessage();
											msg.what = showError;
											if (fileInfo != null) {
												msg.obj = fileInfo.fileName;
											} else {
												msg.obj = "";
											}
											handler.sendMessage(msg);
										}
									}
									setStatisticsCount(14, true);
									Message msg = handler.obtainMessage();
									msg.what = maxProgress;
									msg.arg1 = maxProgress;
									handler.sendMessage(msg);
									fileCategoryHelper.delete(dbIds);
									handler.post(new Runnable() {

										@Override
										public void run() {
											AuroraConfig.setOtherAppDel(activity, true);
											operationInterface.deleteComplete(fileInfos);
											selectFiles.clear();
										}
									});
									for (int i = 0; i < fileInfos.size(); i++) {
										final FileInfo fileInfo = fileInfos.get(i);
										if (fileInfo.IsDir) {
											handler.postDelayed(new Runnable() {

												@Override
												public void run() {
													sendBroadcastScan(AuroraConfig.ACTION_DIR_SCAN, fileInfo.filePath);
												}
											}, 1000);
										}
									}

									handler.postDelayed(new Runnable() {

										@Override
										public void run() {
											setLastOperation(Operation.noOperation);
										}
									}, 500);
								}
							}
						});
						thread.setPriority(Thread.NORM_PRIORITY - 1);
						thread.start();
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						auroraAlertDialog.dismiss();
						setLastOperation(Operation.noOperation);
					}
				}).create();

		auroraAlertDialog.show();
	}

	/**
	 * 通过path 获取id
	 * @param path
	 * @return
	 */
	public long getDbId(String path) {
		String volumeName = "external";
		Uri uri = Files.getContentUri(FileCategoryHelper.volumeName);
		String selection = FileColumns.DATA + "=?";

		String[] selectionArgs = new String[] { path };

		String[] columns = new String[] { FileColumns._ID };
		Cursor c = null;
		long id = 0;
		try {
			c = activity.getContentResolver().query(uri, columns, selection, selectionArgs, null);
			if (c == null) {
				return 0;
			}
			if (c.moveToNext()) {
				id = c.getLong(0);
			}
		} catch (AndroidRuntimeException e) {
			Log.e(TAG, "AndroidRuntimeException " + e != null ? e.getMessage() : "");
		} catch (Exception e) {
			Log.e(TAG, "Exception " + e != null ? e.getMessage() : "");
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}

		return id;
	}

	/**
	 * 删除对话框 返回监听
	 */
	private OnKeyListener keylistener = new DialogInterface.OnKeyListener() {
		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			// LogUtil.log(TAG, "keyCode == KeyEvent.KEYCODE_BACK"
			// + (keyCode == KeyEvent.KEYCODE_BACK));
			LogUtil.d(TAG, "-----keylistener KEYCODE_BACK");
			if (keyCode == KeyEvent.KEYCODE_BACK&&event.getRepeatCount()==0) {
				if (auroraAlertDialog != null) {
					auroraAlertDialog.dismiss();
				}
				if (mDialog != null) {
					mDialog.dismiss();

				}
				if (thread != null) {
					LogUtil.d(TAG, "thread.interrupt() mDialog:"+mDialog.isShowing());
					thread.interrupt();
					isInterrupted = true;
				}
				return true;
			} else {
				return false;
			}
		}
	};

	private List<FileInfo> fileInfos;

	/**
	 * 发送扫描广播
	 */
	public void sendBroadcastScan(String scan, String path) {
		// LogUtil.log(TAG, "sendBroadcast==" + scan + " path==" + path);
		Intent intent = new Intent();
		intent.setAction(scan);
		intent.setData(Uri.fromFile(new File(path)));
		context.sendBroadcast(intent);
	}

	/**
	 * 发送扫描广播
	 * @param scan
	 */
	public void sendBroadcastScan(String scan) {
		Intent intent = new Intent();
		intent.setAction(scan);
		context.sendBroadcast(intent);
	}

	/**
	 * 发送全盘扫描广播
	 */
	/*
	 * public static void sendBroadcastScan(Context context) { LogUtil.elog(TAG,
	 * "send scan all broadcast"); Intent intent = new
	 * Intent(AuroraConfig.ACTION_DIR_SCAN);
	 * intent.setClassName("com.android.providers.media",
	 * "com.android.providers.media.MediaScannerReceiver"); intent.setData(Uri
	 * .fromFile(new File(FileExplorerTabActivity.mSDCardPath)));
	 * context.sendBroadcast(intent); }
	 */
	/**
	 * 发送指定目录扫描广播
	 */
	public void sendBroadcastScanByPath() {

		new Thread(new Runnable() {

			@Override
			public void run() {
				// try {
				List<String> paths = AuroraConfig.getPathString();
				for (final String path : paths) {
					// Thread.sleep(1000);
					File file = new File(Environment.getExternalStorageDirectory() + path);
					if (file.exists()) {
						// LogUtil.log(TAG, "send scan broadcast path=="
						// + Environment.getExternalStorageDirectory()
						// + path);
						OperationAction.setLastOperation(Operation.copy);
						sendBroadcastScan(AuroraConfig.ACTION_DIR_SCAN, Environment.getExternalStorageDirectory() + path);
					}
				}

				// } catch (InterruptedException e) {
				// e.printStackTrace();
				// }

			}
		}).start();
		// OperationAction.setLastOperation(Operation.copy);
		sendBroadcastScan(AuroraConfig.ACTION_EXT_SCAN);
	}

	// add by JXH 2014-7-16 begin 重命名下载
	private Uri mBaseUri = Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI;

	public void renameDownload(String _data, String old) {
		int id = 0;

		ContentResolver contentResolver = activity.getContentResolver();
		Cursor cursor = contentResolver.query(mBaseUri, new String[] { Downloads.Impl._ID }, "_data = ?", new String[] { old }, null);
		if (cursor != null && cursor.getCount() == 1 && cursor.moveToFirst()) {
			id = cursor.getInt(cursor.getColumnIndex(Downloads.Impl._ID));
		} else {
			LogUtil.e(TAG, "cursor is null or cursor count >1 or 0");
		}
		cursor.close();
		if (id == 0) {
			LogUtil.e(TAG, "id==" + id);
			return;
		}
		ContentValues values = new ContentValues();
		values.put(Downloads.Impl.COLUMN_TITLE, Util.getNameFromFilepath(_data));
		values.put(Downloads.Impl._DATA, _data);
		int i = contentResolver.update(ContentUris.withAppendedId(mBaseUri, id), values, null, null);
		// LogUtil.log(TAG, "_data==" + _data + " old==" + old + " i=" + i
		// + " id==" + id);
	}
	// add by JXH 2014-7-16 end
}
