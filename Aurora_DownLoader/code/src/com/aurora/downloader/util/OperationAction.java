package com.aurora.downloader.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.AsyncQueryHandler;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnKeyListener;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;
import android.provider.Downloads;

import com.aurora.downloader.DownloadActivity;
import com.aurora.downloader.FileInfo;
import com.aurora.downloader.R;

public class OperationAction {
	private static final String TAG = "OperationAction";
	private DownloadActivity downloadActivity;

	private OperationInterfaceLisenter operationInterfaceLisenter;

	public OperationAction(DownloadActivity downloadActivity,
			OperationInterfaceLisenter operationInterfaceLisenter) {
		super();
		this.downloadActivity = downloadActivity;
		this.operationInterfaceLisenter = operationInterfaceLisenter;
		bacAsyncQueryHandler = new AsyncQueryHandler(
				downloadActivity.getContentResolver()) {
		};
	}

	public interface OperationInterfaceLisenter {
		void deleteComplete(List<FileInfo> ids);

	}

	private List<FileInfo> selectPath;

	/**
	 * @return the selectPath
	 */
	public List<FileInfo> getSelectPath() {
		return selectPath;
	}

	/**
	 * @param selectPath
	 *            the selectPath to set
	 */
	public void setSelectPath(List<FileInfo> selectPath) {
		this.selectPath = selectPath;
	}

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

		default:
			break;
		}
		}

	};

	private List<FileInfo> successPath = new ArrayList<FileInfo>();
	private AuroraAlertDialog auroraAlertDialog;
	private AuroraProgressDialog mDialog;
	private int size = 0;

	private Thread thread;
	private static final int maxProgress = 100;
	private boolean isInterrupted = false;
	

	public boolean fastDeleteFiles(FileInfo fileInfo) {
		if(fileInfo!=null&&fileInfo.filePath==null&&fileInfo.downloadId!=0){
			try {
				downloadActivity.deleteDownload(fileInfo.downloadId);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		if(fileInfo==null||fileInfo.filePath==null){
			AuroraLog.elog(TAG, "fileInfo "+fileInfo);
			return false;
		}
		boolean del=false;
		try {
			del = FileUtil.deleteFile(fileInfo.filePath);
			if(del){
				if(fileInfo.dbId==0){
					fileInfo.dbId = getDbId(fileInfo.filePath);
				}
				long ids[] = {fileInfo.dbId};
				long dids[] = {fileInfo.downloadId};
				deleteDownload(dids);
				delete(ids);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return del;
	}

	public void deleteFiles() {
		if (selectPath == null || selectPath.size() == 0) {
			AuroraLog.elog(TAG, "selectPath is null or size is 0");
			return;
		}
		String message;
		if (selectPath.size() == 1) {
			message = downloadActivity
					.getString(R.string.operation_delete_single_file_confirm_message);
		} else {
			message = downloadActivity
					.getString(R.string.operation_delete_double_file_confirm_message);
			message = String.format(message, selectPath.size());
		}
		auroraAlertDialog = new AuroraAlertDialog.Builder(downloadActivity)
				.setTitle(message)
//				.setTitle(R.string.operation_delete)
				.setCancelable(false)
				.setOnKeyListener(keylistener)
				.setPositiveButton(R.string.operation_delete,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								auroraAlertDialog.dismiss();
								if (operationInterfaceLisenter == null) {
									return;
								}

								mDialog = new AuroraProgressDialog(
										downloadActivity);
								mDialog.setProgressStyle(aurora.app.AuroraProgressDialog.STYLE_HORIZONTAL);
								mDialog.setMax(maxProgress);
								mDialog.setTitle(R.string.operation_deleting);
								mDialog.setOnKeyListener(keylistener);
								mDialog.show();

								thread = new Thread(new Runnable() {

									@Override
									public void run() {
										int oldCount = 0;
										size = selectPath.size();
										long[] dbIds = new long[size];
										long[] downIds = new long[size];
										for (int i = 0; i < size; i++) {
											if (thread.isInterrupted()
													|| isInterrupted) {
												isInterrupted = false;
												thread.interrupt();
												AuroraLog.elog(TAG,
														"del isInterrupted");
												break;
											}
											successPath.clear();
											FileInfo fileInfo = selectPath
													.get(i);
											final String tempPath = fileInfo.filePath;
											try {
												if(tempPath==null&&fileInfo.downloadId!=0){//该文件还没有开始下载
													downIds[i] = fileInfo.downloadId;
													successPath.add(fileInfo);
												}else {
													boolean del = FileUtil
															.deleteFile(tempPath);
													if (del) {
														long id = getDbId(tempPath);
														dbIds[i] = id;
														downIds[i] = fileInfo.downloadId;
														fileInfo.dbId = id;
														successPath.add(fileInfo);
													}
												}
												Message msg = handler
														.obtainMessage();
												msg.what = maxProgress;
												AuroraLog.log(TAG, "size=="
														+ size);
												msg.arg1 = (int) ((Float
														.valueOf(i + 1) / Float
														.valueOf(size)) * maxProgress);
												AuroraLog.elog(TAG, "msg.arg1=="+msg.arg1);
												if (msg.arg1 > oldCount) {
													oldCount = msg.arg1;
													handler.sendMessage(msg);
												}
											} catch (Exception e1) {
												e1.printStackTrace();
												handler.post(new Runnable() {

													@Override
													public void run() {
														try {
															Toast.makeText(
																	downloadActivity,
																	downloadActivity
																			.getResources()
																			.getString(
																					R.string.del_file,
																					Util.getNameFromFilepath(tempPath)),
																	Toast.LENGTH_SHORT)
																	.show();
														} catch (Exception e) {
															// TODO Auto-generated catch block
															e.printStackTrace();
														}
													}
												});
											}

										}
										// 删除数据库ID
										deleteDownload(downIds);
										delete(dbIds);
										handler.post(new Runnable() {

											@Override
											public void run() {
												operationInterfaceLisenter
														.deleteComplete(successPath);
												selectPath.clear();
											}
										});
									}
								});
								thread.setPriority(Thread.NORM_PRIORITY - 1);
								thread.start();
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								auroraAlertDialog.dismiss();
							}
						}).create();

		auroraAlertDialog.show();
	}

	/**
	 * 删除对话框 返回监听
	 */
	private OnKeyListener keylistener = new DialogInterface.OnKeyListener() {
		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			AuroraLog.elog(TAG, "keyCode == KeyEvent.KEYCODE_BACK"
					+ (keyCode == KeyEvent.KEYCODE_BACK));
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				if (auroraAlertDialog != null) {
					auroraAlertDialog.dismiss();
				}
				if (mDialog != null) {
					mDialog.dismiss();

				}
				if (thread != null) {
					AuroraLog.elog(TAG, "thread.interrupt()");
					isInterrupted = true;
					thread.interrupt();
				}
				return true;
			} else {
				return false;
			}
		}
	};

	/**
	 * 通过path 获取id
	 * 
	 * @param path
	 * @return
	 */
	public long getDbId(String path) {
		String volumeName = "external";
		Uri uri = Files.getContentUri(volumeName);
		String selection = FileColumns.DATA + "=?";

		String[] selectionArgs = new String[] { path };

		String[] columns = new String[] { FileColumns._ID };
		Cursor c = null;
		long id = 0;
		try {
			c = downloadActivity.getContentResolver().query(uri, columns,
					selection, selectionArgs, null);
			if (c == null) {
				return 0;
			}
			if (c.moveToNext()) {
				id = c.getLong(0);
			}
		} catch (AndroidRuntimeException e) {
			Log.e(TAG, "AndroidRuntimeException " + e != null ? e.getMessage()
					: "");
		} catch (Exception e) {
			Log.e(TAG, "Exception " + e != null ? e.getMessage() : "");
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}

		return id;
	}

	private AsyncQueryHandler bacAsyncQueryHandler;

	/**
	 * 删除数据库数据
	 * 
	 * @param fc
	 * @param ids
	 */
	public void delete(long[] ids) {
		String volumeName = "external";
		if (ids == null || ids.length <= 0) {
			return;
		}
		Uri baseUri = Files.getContentUri(volumeName);

		String selection = "_id in (";
		for (int i = 0; i < ids.length; i++) {
			selection += ids[i];
			if (i != ids.length - 1) {
				selection += ",";
			} else {
				selection += ")";
			}
		}
		AuroraLog.elog(TAG, "baseUri==" + baseUri + " selection==" + selection);
		bacAsyncQueryHandler.startDelete(0, null, baseUri, selection, null);
	}

	public void deleteDownload(long[] ids) {
		if (ids == null || ids.length <= 0) {
			return;
		}
		for (int i = 0; i < ids.length; i++) {
			downloadActivity.deleteDownload(ids[i]);
		}
	}

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
	
	//add by Jxh 2014-8-13 begin
	public static final String ACTION_COMPLETE="android.intent.action.CANCLE_COMPLETE";
	public void sendBroadcastHidNotif(boolean hid) {
		Intent intent = new Intent();
		intent.setAction(ACTION_COMPLETE);
		intent.putExtra("hid", hid);
		downloadActivity.sendBroadcast(intent);
	}
	//add by Jxh 2014-8-13 end

}
