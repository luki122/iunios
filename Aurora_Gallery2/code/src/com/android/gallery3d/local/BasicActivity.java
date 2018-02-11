package com.android.gallery3d.local;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.android.gallery3d.app.Gallery;
import com.android.gallery3d.app.GalleryAppImpl;
import com.android.gallery3d.local.tools.AuroraFilenameFilter;
import com.android.gallery3d.local.tools.GalleryLocalUtils;
import com.android.gallery3d.local.tools.MediaFileOperationUtil;
import com.android.gallery3d.local.tools.StorageUtils;
import com.android.gallery3d.local.widget.GalleryAdapter;
import com.android.gallery3d.local.widget.GalleryBaseAdapter;
import com.android.gallery3d.local.widget.GalleryItemAdapter;
import com.android.gallery3d.local.widget.MediaFileInfo;
import com.android.gallery3d.local.widget.PopupWindowUtil;
import com.android.gallery3d.ui.Log;
import com.android.gallery3d.util.NetworkUtil;
import com.android.gallery3d.viewpager.ViewpagerActivity;
import com.android.gallery3d.xcloudalbum.CloudActivity;
import com.android.gallery3d.xcloudalbum.account.AccountHelper;
import com.android.gallery3d.xcloudalbum.fragment.CloudItemFragment;
import com.android.gallery3d.xcloudalbum.fragment.CloudMainFragment;
import com.android.gallery3d.xcloudalbum.tools.BaiduAlbumUtils;
import com.android.gallery3d.xcloudalbum.tools.LocalPopupWindowUtil;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.android.gallery3d.xcloudalbum.tools.OperationUtil;
import com.android.gallery3d.xcloudalbum.uploaddownload.UploadToXCloudListener;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraEditText;
import aurora.widget.AuroraTextView;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;

import com.android.gallery3d.R;
import com.baidu.xcloud.pluginAlbum.AccountProxy;

public abstract class BasicActivity extends AuroraActivity {

	private static final int IMAGES = 10000;
	private static final int VIDEOS = 10001;
	private static final String TAG = "BasicActivity";
	protected List<MediaFileInfo> selectFileInfo = Collections.synchronizedList(new ArrayList<MediaFileInfo>());
	protected List<String> foldersPath = Collections.synchronizedList(new ArrayList<String>());
	protected MediaFileOperationUtil operationUtil;
	private AuroraActionBar auroraActionBar;
	protected List<String> allNoShowPaths = new ArrayList<String>();
	protected int mMenuPaddingBottom, mMenuPaddingRight;

	private static final int CREATE_ALBUM = 10001, DEL_ALBUM = 10003, DEL_FILES = 10004, SHIELD_ALBUM = 10005, DIALOG_NET = 10006;
	protected AuroraAlertDialog mDialog;
	protected AuroraEditText inputName;
	private String newFilePath;
	private String message;


	public void setAuroraActionBar(AuroraActionBar auroraActionBar) {
		this.auroraActionBar = auroraActionBar;
	}

	private GalleryBaseAdapter baseAdapter;

	public void setBaseAdapter(GalleryBaseAdapter baseAdapter) {
		this.baseAdapter = baseAdapter;
	}

	public abstract void imageDataChange();

	public abstract void videoDataChange();

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == IMAGES) {
				LogUtil.d(TAG, "----------IMAGES");
				imageDataChange();
			} else if (msg.what == VIDEOS) {
				LogUtil.d(TAG, "----------VIDEOS");
				videoDataChange();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		operationUtil = MediaFileOperationUtil.getMediaFileOperationUtil(BasicActivity.this);
	}

	@Override
	protected void onStart() {
		registerMediaContentObserver();
		super.onStart();
	}

	@Override
	protected void onStop() {
		unregisterMediaObserver();
		super.onStop();
	}

	private void registerMediaContentObserver() {
		registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mImageContentObserver);
		registerContentObserver(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mVideoContentObserver);
	}

	private void unregisterMediaObserver() {
		unregisterObserver(mImageContentObserver);
		unregisterObserver(mVideoContentObserver);
	}

	/**
	 * 注册contentObserver
	 * @param uri
	 * @param contentObserver
	 */
	private void registerContentObserver(Uri uri, ContentObserver contentObserver) {
		getContentResolver().registerContentObserver(uri, true, contentObserver);
	}

	/**
	 * 注销contentObserver
	 * @param contentObserver
	 */
	private void unregisterObserver(ContentObserver contentObserver) {
		getContentResolver().unregisterContentObserver(contentObserver);
	}

	private ContentObserver mImageContentObserver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			if(BasicActivity.this instanceof GalleryItemActivity){
				StorageUtils.setIntPref(getApplication(), "DataChange", 1);
			}
			handler.removeMessages(IMAGES);
			Message msg = handler.obtainMessage(IMAGES);
			handler.sendMessageDelayed(msg, 200);
		};
	};

	private ContentObserver mVideoContentObserver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			if(BasicActivity.this instanceof GalleryItemActivity){
				StorageUtils.setIntPref(getApplication(), "DataChange", 1);
			}
			handler.removeMessages(VIDEOS);
			Message msg = handler.obtainMessage(VIDEOS);
			handler.sendMessageDelayed(msg, 200);
		};
	};

	/**
	 * actionBar监听回调 全部的增删改 {@link AuroraOperationBarMoreMenu.buttonClick}
	 */
	protected OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {

		@Override
		public void auroraMenuItemClick(int id) {
			LogUtil.d(TAG, "--------operation_id:" + id);
			baseAdapter.setItemPicAnim(false);
			switch (id) {
			case R.id.operation_send:
				LogUtil.d(TAG, "--------operation_send");
				operationUtil.shareOperation(selectFileInfo);
				break;
			case R.id.operation_delete:
				LogUtil.d(TAG, "--------operation_delete");
				if (BasicActivity.this instanceof GalleryItemActivity) {
//					buildmDialogMessage(DEL_FILES);
					showDialog(DEL_FILES);
				} else {
//					buildmDialogMessage(DEL_ALBUM);
					showDialog(DEL_ALBUM);
				}
//				if (mDialog != null) {
//					mDialog.setMessage(message);
//				}
				break;
			case R.id.operation_favorite:
				LogUtil.d(TAG, "--------operation_favorite");
				operationUtil.addFavoriteOperation(selectFileInfo);
				break;
			case R.id.operation_no_favorite:
				LogUtil.d(TAG, "--------operation_no_favorite");
				operationUtil.removeFavoriteOperation(selectFileInfo);
				break;
			case R.id.operation_more:
				LogUtil.d(TAG, "--------operation_more");
				// Intent intent = new Intent("aurora.intent.action.shield");
				// startActivity(intent);
				if (BasicActivity.this instanceof GalleryItemActivity) {
					GalleryItemActivity activity = (GalleryItemActivity) BasicActivity.this;
					activity.showAuroraMenu(activity.getWindow().getDecorView(), Gravity.RIGHT | Gravity.BOTTOM, mMenuPaddingRight, mMenuPaddingBottom);
				}

				break;
			case R.id.operation_shield:
//				buildmDialogMessage(SHIELD_ALBUM);
				showDialog(SHIELD_ALBUM);
//				if (mDialog != null) {
//					mDialog.setMessage(message);
//				}
				LogUtil.d(TAG, "-----operation_shield");
				break;
			case R.id.operation_unshield:
				if (operationUtil.unShieldOperation(foldersPath, allNoShowPaths)) {
					reloadMapdata();
				} else {
					LogUtil.d(TAG, "--fail---operation_unshield");
				}
				break;
			case R.id.menu_upload_to_xcloud:
				if (!NetworkUtil.checkNetwork(BasicActivity.this)) {
					Toast.makeText(BasicActivity.this, R.string.aurora_album_network_fail, Toast.LENGTH_SHORT).show();
					return;
				}
				AccountHelper accountHelper = new AccountHelper(BasicActivity.this);
				accountHelper.update();
				if (!AccountHelper.mLoginStatus || BaiduAlbumUtils.getInstance(getApplicationContext()).getAccountInfo() == null || AccountProxy.getInstance().hasLogout()) {
					Toast.makeText(BasicActivity.this, R.string.aurora_cloud_login, Toast.LENGTH_SHORT).show();
					return;
				}
				 if (!NetworkUtil.checkWifiNetwork(BasicActivity.this)) {
					showDialog(DIALOG_NET);
				} else {
					uploadXClode();
				}
				break;
			case R.id.menu_move:
				PopupWindowUtil popupWindowUtil = PopupWindowUtil.getInstance(BasicActivity.this);
				popupWindowUtil.showSelectPopupWindow(getResources().getString(R.string.aurora_actionbar_move_to), BasicActivity.this.getCurrentFocus());
				popupWindowUtil.setSelectFileInfos(selectFileInfo);
				break;

			}
			if (id != R.id.operation_more && id != R.id.operation_shield && id != R.id.operation_delete) {
				baseAdapter.setOperation(false);
				baseAdapter.setItemPicAnim(false);
				GalleryLocalUtils.showOrHideMenu(auroraActionBar, false);
			}

		}

	};

	private void uploadXClode() {
		LocalPopupWindowUtil uitl = new LocalPopupWindowUtil(BasicActivity.this);
		String tip = String.format(getResources().getString(R.string.aurora_upload_sum_string_format), "" + selectFileInfo.size());
		ArrayList<MediaFileInfo> filePaths = new ArrayList<MediaFileInfo>();//wenyongzhe 2016.3.8 new_ui
		boolean isVedio = false;
		for (MediaFileInfo info : selectFileInfo) {
			if (!info.isImage) {
				isVedio = true;
				continue;
			}
			filePaths.add(info);
		}
		if (isVedio) {
			Toast.makeText(BasicActivity.this, R.string.aurora_not_support_uplaod_video, Toast.LENGTH_SHORT).show();
		}
		if (filePaths.size() > 0) {
			uitl.showSelectPopupWindow(tip, BasicActivity.this.getCurrentFocus(), new OperationUtil(BasicActivity.this), filePaths, new UploadToXCloudListener() {

				@Override
				public void uploadFinished(boolean uploaded) {

				}
			});
		}
	}

	private void reloadMapdata() {
		GalleryLocalActivity activity = ((GalleryLocalActivity) BasicActivity.this);
		ConcurrentHashMap<Integer, String> folders = activity.getFoldersMap();
		ConcurrentHashMap<Integer, String> foldersTemp = new ConcurrentHashMap<Integer, String>();
		int size = folders.size();
		LogUtil.d(TAG, "---folders:" + size);
		int index = 0;
		for (int i = 0; i < size; i++) {
			String path = folders.get(i);
			if (foldersPath.contains(path)) {
				folders.remove(i);
				LogUtil.d(TAG, "---remove:" + path);
				continue;
			}
			foldersTemp.put(index, path);
			index++;
		}
		LogUtil.d(TAG, "---foldersTemp:" + foldersTemp.size());
		activity.setFoldersMap(foldersTemp);
	}

	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		if(id==DEL_FILES||id ==DEL_ALBUM||id ==SHIELD_ALBUM){
			buildmDialogMessage(id);
			AuroraAlertDialog alertDialog = (AuroraAlertDialog) dialog;
			alertDialog.setTitle(message);
		}
	}
	@Override
	protected Dialog onCreateDialog(final int id) {
		if (id == CREATE_ALBUM) {
			LogUtil.d(TAG, "-----------onCreateDialog");
			MediaFileOperationUtil operationUtil = MediaFileOperationUtil.getMediaFileOperationUtil(BasicActivity.this);
			operationUtil.setDialogParams(newFilePath);
			return operationUtil.createNewAlbumDialog();
		} else if (id == DEL_ALBUM || id == DEL_FILES) {
			AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(BasicActivity.this).setTitle(R.string.aurora_delete)//.setMessage("")
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
							boolean del = false;
							if (id == DEL_FILES) {
								del = operationUtil.delOperation(selectFileInfo);
							} else {
								GalleryLocalActivity activity = ((GalleryLocalActivity) BasicActivity.this);
								ConcurrentHashMap<String, List<MediaFileInfo>> mediaFilesMap = activity.getMediaFilesMap();
								if (foldersPath.removeAll(activity.getSystemPaths())) {
									Toast.makeText(BasicActivity.this, getString(R.string.album_sys_del), Toast.LENGTH_SHORT).show();
								}
								del = operationUtil.delAlbumsOperation(foldersPath, mediaFilesMap);
							}
							baseAdapter.setOperation(false);
							baseAdapter.setItemPicAnim(false);
							GalleryLocalUtils.showOrHideMenu(auroraActionBar, false);
							GalleryLocalUtils.hideInputMethod(BasicActivity.this, mDialog.getCurrentFocus());
							if (BasicActivity.this instanceof GalleryLocalActivity) {
								GalleryLocalActivity activity = ((GalleryLocalActivity) BasicActivity.this);
								if (!activity.isShieldActivity()) {
									((ViewpagerActivity) ((GalleryAppImpl) getApplicationContext()).getmActivityContext()).subActivitySelectionModeChange(false);
								}
							}
						}

					}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							GalleryLocalUtils.hideInputMethod(BasicActivity.this, mDialog.getCurrentFocus());

						}
					});
			mDialog = builder.create();
			mDialog.setCanceledOnTouchOutside(false);
			mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
			return mDialog;
		} else if (id == SHIELD_ALBUM) {
			AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(BasicActivity.this).setTitle(R.string.aurora_shield)//setMessage("")
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
							if (BasicActivity.this instanceof GalleryLocalActivity) {
								GalleryLocalActivity activity = ((GalleryLocalActivity) BasicActivity.this);
								if (foldersPath.removeAll(activity.getSystemPaths())) {
									Toast.makeText(BasicActivity.this, getString(R.string.album_sys_shield), Toast.LENGTH_SHORT).show();
								}
								List<String> noShowPaths = GalleryLocalUtils.doParseXml(activity);
								noShowPaths.addAll(foldersPath);
								if (operationUtil.shieldOperation(noShowPaths)) {
									reloadMapdata();
								}
								baseAdapter.setOperation(false);
								baseAdapter.setItemPicAnim(false);
								GalleryLocalUtils.showOrHideMenu(auroraActionBar, false);
								GalleryLocalUtils.hideInputMethod(BasicActivity.this, mDialog.getCurrentFocus());
								if (!activity.isShieldActivity()) {
									((ViewpagerActivity) ((GalleryAppImpl) getApplicationContext()).getmActivityContext()).subActivitySelectionModeChange(false);
								}
							}
						}

					}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							GalleryLocalUtils.hideInputMethod(BasicActivity.this, mDialog.getCurrentFocus());

						}
					});
			mDialog = builder.create();
			mDialog.setCanceledOnTouchOutside(false);
			mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
			return mDialog;
		}
		if (id == DIALOG_NET) {
			AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(BasicActivity.this).setTitle(R.string.photo_upload_no_wifi_title)//wenyongzhe
					.setMessage(R.string.aurora_cloud_download_dialog_message)
					.setPositiveButton(R.string.aurora_upload_download_continue_task, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							uploadXClode();
						}
					}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			mDialog = builder.create();
			mDialog.setCanceledOnTouchOutside(false);
			mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
			return mDialog;
		}
		return super.onCreateDialog(id);
	}

	private void buildmDialogMessage(int id) {
		if (id == SHIELD_ALBUM) {
			message = getResources().getString(R.string.number_of_albums_selected_shield_one);
			if (foldersPath.size() > 1) {
				message = getResources().getString(R.string.number_of_albums_selected_shield_other, foldersPath.size());
			}
		} else if (id == DEL_ALBUM || id == DEL_FILES) {
			message = getResources().getString(R.string.number_of_albums_selected_del_one);
			if (foldersPath.size() > 1) {
				message = getResources().getString(R.string.number_of_albums_selected_del_other, foldersPath.size());
			}
			if (id == DEL_FILES) {
				message = getResources().getString(R.string.number_of_files_selected_del_one);
				if (selectFileInfo.size() > 1) {
					message = getResources().getString(R.string.number_of_files_selected_del_other, selectFileInfo.size());
				}
			}
			
		}
		LogUtil.d(TAG, "----selectFileInfo.size():" + selectFileInfo.size() + " foldersPath.size():" + foldersPath.size());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		LogUtil.d(TAG, "-----requestCode::" + requestCode + " resultCode::" + resultCode + " data::" + data);
		if (requestCode == CREATE_ALBUM && resultCode == Activity.RESULT_OK) {
			ArrayList<String> temps = ((GalleryAppImpl) getApplication()).getSelectedFilesForXCloud();
			List<MediaFileInfo> mediaFileInfos = new ArrayList<MediaFileInfo>();
			for (String path : temps) {
				MediaFileInfo fileInfo = new MediaFileInfo();
				fileInfo.filePath = path;
				mediaFileInfos.add(fileInfo);
			}
			newFilePath = operationUtil.getNewFilePath();
			if(operationUtil.createNewAlbum(new File(newFilePath))){
				LogUtil.d(TAG, "---mediaFileInfos:" + mediaFileInfos.size() + " newFilePath:" + newFilePath);
				operationUtil.moveOperation(mediaFileInfos, newFilePath);
			}
		}
	}

	/**
	 * 判断编辑动画是否播放完成
	 * @return
	 */
	public boolean actionBarIsAnimRunning(AuroraActionBar auroraActionBar) {
		if (auroraActionBar == null) {// 快速点击图片和手机返回键
			return false;
		}
		if (auroraActionBar.auroraIsEntryEditModeAnimRunning() || auroraActionBar.auroraIsExitEditModeAnimRunning()) {
			return true;
		}
		return false;
	}

	/**
	 * 用于判断item是否播放动画
	 */
	private int selectPosition = -1;

	public int getSelectPosition() {
		return selectPosition;
	}

	public void setSelectPostion(int selectPosition) {
		this.selectPosition = selectPosition;
	}



}
