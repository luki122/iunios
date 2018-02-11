/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.gallery3d.ui;

import android.app.Activity;
import aurora.app.AuroraAlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.gallery3d.R;
import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.app.PhotoPage;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.LocalVideo;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.filtershow.FilterShowActivity;
import com.android.gallery3d.local.widget.MediaFileInfo;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.StatisticsUtils;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.android.gallery3d.xcloudalbum.tools.LocalPopupWindowUtil;
import com.android.gallery3d.xcloudalbum.tools.OperationUtil;
import com.android.gallery3d.xcloudalbum.tools.PopupWindowUtil;

import aurora.app.AuroraProgressDialog;
import aurora.widget.AuroraTextView;

import com.android.gallery3d.data.LocalImage;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;

import java.util.ArrayList;
import java.util.List;
import com.android.gallery3d.xcloudalbum.uploaddownload.UploadToXCloudListener;
import com.android.gallery3d.xcloudalbum.uploaddownload.XCloudUploadFilter;
import com.android.gallery3d.xcloudalbum.widget.InformationDialog;
import com.android.gallery3d.util.Globals;

import android.text.TextUtils;
import android.content.ContentValues;
import android.provider.MediaStore;

public class MenuExecutor implements UploadToXCloudListener{
	@SuppressWarnings("unused")
	private static final String TAG = "MenuExecutor";

	private static final int MSG_TASK_COMPLETE = 1;
	private static final int MSG_TASK_UPDATE = 2;
	private static final int MSG_TASK_START = 3;
	private static final int MSG_DO_SHARE = 4;

	public static final int EXECUTION_RESULT_SUCCESS = 1;
	public static final int EXECUTION_RESULT_FAIL = 2;
	public static final int EXECUTION_RESULT_CANCEL = 3;

	// private ProgressDialog mDialog;

	private AuroraProgressDialog mDialog;
	private Future<?> mTask;
	// wait the operation to finish when we want to stop it.
	private boolean mWaitOnStop;

	private final AbstractGalleryActivity mActivity;
	private final SelectionManager mSelectionManager;
	private final Handler mHandler;

	// Aurora <SQF> <2014-6-20> for NEW_UI begin

	private AlertDialogShowStatusListener mAlertDialogShowStatusListener;

	public interface AlertDialogShowStatusListener {
		public void notifyAlertDialogStatus(boolean isShowing);
	}

	public void setAlertDialogShowStatusListener(
			AlertDialogShowStatusListener listener) {
		mAlertDialogShowStatusListener = listener;
	}

	// private void setAlertDialogStatusAndNotify(boolean isShowing);

	// Aurora <SQF> <2014-6-20> for NEW_UI end
	
	
	//SQF ADDED ON 2015.4.28 begin
	private ArrayList<Path> mSelectedPaths;
	
	@Override
	public void uploadFinished(boolean uploaded) {
		//Log.i("SQF_LOG", "MenuExecutor::uploadFinished uploaded:" + uploaded);
		if(mSelectedPaths != null) {
			mSelectedPaths.clear();
		}
		mSelectionManager.leaveSelectionMode();
		
		if(uploaded) {
			mActivity.showUploadProgress();
		}
	}
	//SQF ADDED ON 2015.4.28 end

	private static AuroraProgressDialog createAuroraProgressDialog(
			Context context, int titleId, int progressMax) {
		AuroraProgressDialog dialog = new AuroraProgressDialog(context);
		dialog.setTitle(titleId);
		dialog.setMax(progressMax);
		dialog.setCancelable(false);
		dialog.setIndeterminate(false);
		if (progressMax > 1) {
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		}
		return dialog;
	}

	private static ProgressDialog createProgressDialog(Context context,
			int titleId, int progressMax) {
		ProgressDialog dialog = new ProgressDialog(context);
		dialog.setTitle(titleId);
		dialog.setMax(progressMax);
		dialog.setCancelable(false);
		dialog.setIndeterminate(false);
		if (progressMax > 1) {
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		}
		return dialog;
	}

	public interface ProgressListener {
		public void onConfirmDialogShown();

		public void onConfirmDialogDismissed(boolean confirmed);

		public void onProgressStart();

		public void onProgressUpdate(int index);

		public void onProgressComplete(int result);
	}

	public MenuExecutor(AbstractGalleryActivity activity,
			SelectionManager selectionManager) {
		mActivity = Utils.checkNotNull(activity);
		mSelectionManager = Utils.checkNotNull(selectionManager);
		mHandler = new SynchronizedHandler(mActivity.getGLRoot()) {
			@Override
			public void handleMessage(Message message) {
				switch (message.what) {
				case MSG_TASK_START: {
					if (message.obj != null) {
						ProgressListener listener = (ProgressListener) message.obj;
						listener.onProgressStart();
					}
					break;
				}
				case MSG_TASK_COMPLETE: {
					stopTaskAndDismissDialog();
					if (message.obj != null) {
						ProgressListener listener = (ProgressListener) message.obj;
						listener.onProgressComplete(message.arg1);
					}
					mSelectionManager.leaveSelectionMode();
					break;
				}
				case MSG_TASK_UPDATE: {
					if (mDialog != null)
						mDialog.setProgress(message.arg1);
					if (message.obj != null) {
						ProgressListener listener = (ProgressListener) message.obj;
						listener.onProgressUpdate(message.arg1);
					}
					break;
				}
				case MSG_DO_SHARE: {
					((Activity) mActivity).startActivity((Intent) message.obj);
					break;
				}
				}
			}
		};
	}

	private void stopTaskAndDismissDialog() {
		if (mTask != null) {
			if (!mWaitOnStop)
				mTask.cancel();
			mTask.waitDone();
			mDialog.dismiss();
			mDialog = null;
			mTask = null;
		}
	}

	public void pause() {
		stopTaskAndDismissDialog();
	}

	private void onProgressUpdate(int index, ProgressListener listener) {
		mHandler.sendMessage(mHandler.obtainMessage(MSG_TASK_UPDATE, index, 0,
				listener));
	}

	private void onProgressStart(ProgressListener listener) {
		mHandler.sendMessage(mHandler.obtainMessage(MSG_TASK_START, listener));
	}

	private void onProgressComplete(int result, ProgressListener listener) {
		mHandler.sendMessage(mHandler.obtainMessage(MSG_TASK_COMPLETE, result,
				0, listener));
	}

	public static void updateMenuOperation(Menu menu, int supported) {
		boolean supportDelete = (supported & MediaObject.SUPPORT_DELETE) != 0;
		boolean supportRotate = (supported & MediaObject.SUPPORT_ROTATE) != 0;
		boolean supportCrop = (supported & MediaObject.SUPPORT_CROP) != 0;
		boolean supportTrim = (supported & MediaObject.SUPPORT_TRIM) != 0;
		boolean supportShare = (supported & MediaObject.SUPPORT_SHARE) != 0;
		boolean supportSetAs = (supported & MediaObject.SUPPORT_SETAS) != 0;
		boolean supportShowOnMap = (supported & MediaObject.SUPPORT_SHOW_ON_MAP) != 0;
		boolean supportCache = (supported & MediaObject.SUPPORT_CACHE) != 0;
		boolean supportEdit = (supported & MediaObject.SUPPORT_EDIT) != 0;
		boolean supportInfo = (supported & MediaObject.SUPPORT_INFO) != 0;
		boolean supportImport = (supported & MediaObject.SUPPORT_IMPORT) != 0;

		setMenuItemVisible(menu, R.id.action_delete, supportDelete);
		setMenuItemVisible(menu, R.id.action_rotate_ccw, supportRotate);
		setMenuItemVisible(menu, R.id.action_rotate_cw, supportRotate);
		setMenuItemVisible(menu, R.id.action_crop, supportCrop);
		setMenuItemVisible(menu, R.id.action_trim, supportTrim);
		// Hide panorama until call to updateMenuForPanorama corrects it
		setMenuItemVisible(menu, R.id.action_share_panorama, false);
		setMenuItemVisible(menu, R.id.action_share, supportShare);
		setMenuItemVisible(menu, R.id.action_setas, supportSetAs);
		setMenuItemVisible(menu, R.id.action_show_on_map, supportShowOnMap);
		setMenuItemVisible(menu, R.id.action_edit, supportEdit);
		setMenuItemVisible(menu, R.id.action_details, supportInfo);
		setMenuItemVisible(menu, R.id.action_import, supportImport);
	}

	public static void updateMenuForPanorama(Menu menu,
			boolean shareAsPanorama360, boolean disablePanorama360Options) {
		setMenuItemVisible(menu, R.id.action_share_panorama, shareAsPanorama360);
		if (disablePanorama360Options) {
			setMenuItemVisible(menu, R.id.action_rotate_ccw, false);
			setMenuItemVisible(menu, R.id.action_rotate_cw, false);
		}
	}

	private static void setMenuItemVisible(Menu menu, int itemId,
			boolean visible) {
		MenuItem item = menu.findItem(itemId);
		if (item != null)
			item.setVisible(visible);
	}

	private Path getSingleSelectedPath() {
		ArrayList<Path> ids = mSelectionManager.getSelected(true);
		Utils.assertTrue(ids.size() == 1);
		return ids.get(0);
	}

	private Intent getIntentBySingleSelectedPath(String action) {
		/*
		 * DataManager manager = mActivity.getDataManager(); Path path =
		 * getSingleSelectedPath(); String mimeType =
		 * getMimeType(manager.getMediaType(path)); return new
		 * Intent(action).setDataAndType(manager.getContentUri(path), mimeType);
		 */

		DataManager manager = mActivity.getDataManager();
		Path path = getSingleSelectedPath();
		String mimeType = getMimeType(manager.getMediaType(path));
		MediaObject mediaObject = manager.getMediaObject(path);
		Intent intent = new Intent(action).setDataAndType(
				manager.getContentUri(path), mimeType);
		if (mediaObject instanceof LocalImage) {
			intent.putExtra(
					FilterShowActivity.INTENT_DATA_USER_SELECTED_FILE_PATH,
					((LocalImage) mediaObject).filePath);
		}
		return intent;
	}

	private void onMenuClicked(int action, ProgressListener listener) {
		onMenuClicked(action, listener, false, true);
	}

	public void onMenuClicked(int action, ProgressListener listener,
			boolean waitOnStop, boolean showDialog) {
		int title;
		switch (action) {
		case R.id.action_select_all:
			if (mSelectionManager.inSelectAllMode()) {
				mSelectionManager.deSelectAll();
			} else {
				mSelectionManager.selectAll();
			}
			return;
		case R.id.action_crop: {
			/*
			 * Intent intent =
			 * getIntentBySingleSelectedPath(FilterShowActivity.CROP_ACTION)
			 * .setClass((Activity) mActivity, FilterShowActivity.class);
			 * ((Activity) mActivity).startActivity(intent);
			 */
			return;
		}
		case R.id.action_edit: {
			// Aurora <paul> <2014-06-13>
			Intent intent = getIntentBySingleSelectedPath(
					"android.intent.action.AURORA_EDIT").setFlags(
					Intent.FLAG_GRANT_READ_URI_PERMISSION);
			// Aurora <SQF> <2014-6-17> for NEW_UI begin
			// ORIGINALLY:
			// ((Activity) mActivity).startActivity(Intent.createChooser(intent,
			// null));
			// SQF MODIFIED TO:
			((Activity) mActivity).startActivityForResult(
					Intent.createChooser(intent, null), PhotoPage.REQUEST_EDIT);
			// Aurora <SQF> <2014-6-17> for NEW_UI end
			/*
			 * Intent intent = getIntentBySingleSelectedPath(Intent.ACTION_EDIT)
			 * .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); ((Activity)
			 * mActivity).startActivity(Intent.createChooser(intent, null));
			 */
			return;
		}
		case R.id.action_setas: {
			Intent intent = getIntentBySingleSelectedPath(
					Intent.ACTION_ATTACH_DATA).addFlags(
					Intent.FLAG_GRANT_READ_URI_PERMISSION);
			intent.putExtra("mimeType", intent.getType());
			Activity activity = mActivity;
			activity.startActivity(Intent.createChooser(intent,
					activity.getString(R.string.set_as)));
			return;
		}
		case R.id.action_delete:
			title = R.string.delete;
			break;
		case R.id.action_rotate_cw:
			title = R.string.rotate_right;
			break;
		case R.id.action_rotate_ccw:
			title = R.string.rotate_left;
			break;
		case R.id.action_show_on_map:
			title = R.string.show_on_map;
			break;
		case R.id.action_import:
			title = R.string.Import;
			break;
		// SQF ADDED ON 2014.4.23 begin
		case R.id.action_upload_to_xcloud:// XCould ....
			//Log.i("SQF_LOG", "MenuExecutor::onMenuClicked   action_upload_to_xcloud===============");
			title = R.string.aurora_upload_to_xcloud;
			mSelectedPaths = mSelectionManager.getSelected(false);
			mPopupHandler.sendEmptyMessageDelayed(MSG_POPUP, 200);
			return;
			//break;
		// SQF ADDED ON 2014.4.23 end
		default:
			return;
		}
		startAction(action, title, listener, waitOnStop, showDialog);
	}

	private class ConfirmDialogListener implements OnClickListener,
			OnCancelListener {
		private final int mActionId;
		private final ProgressListener mListener;

		public ConfirmDialogListener(int actionId, ProgressListener listener) {
			mActionId = actionId;
			mListener = listener;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// Aurora <SQF> <2014-6-20> for NEW_UI begin
			if (mAlertDialogShowStatusListener != null) {
				mAlertDialogShowStatusListener.notifyAlertDialogStatus(false);
			}
			// Aurora <SQF> <2014-6-20> for NEW_UI end
			if (which == DialogInterface.BUTTON_POSITIVE) {
				if (mListener != null) {
					mListener.onConfirmDialogDismissed(true);
				}
				onMenuClicked(mActionId, mListener);
			} else {

				if (mListener != null) {
					mListener.onConfirmDialogDismissed(false);
					// Log.i(TAG, "zll ---- onSelectionModeChange onClick");
				}
			}
		}

		@Override
		public void onCancel(DialogInterface dialog) {
			// Aurora <SQF> <2014-6-20> for NEW_UI begin
			if (mAlertDialogShowStatusListener != null) {
				mAlertDialogShowStatusListener.notifyAlertDialogStatus(false);
			}
			// Aurora <SQF> <2014-6-20> for NEW_UI end
			if (mListener != null) {
				mListener.onConfirmDialogDismissed(false);
				// Log.i(TAG, "zll ---- onSelectionModeChange onCancel");
			}
		}
	}

	public void onMenuClicked(MenuItem menuItem, String confirmMsg,
			final ProgressListener listener) {
		final int action = menuItem.getItemId();

		if (confirmMsg != null) {
			if (listener != null)
				listener.onConfirmDialogShown();
			ConfirmDialogListener cdl = new ConfirmDialogListener(action,
					listener);
			new AuroraAlertDialog.Builder(mActivity.getAndroidContext())
					.setMessage(confirmMsg).setOnCancelListener(cdl)
					.setPositiveButton(R.string.ok, cdl)
					.setNegativeButton(R.string.cancel, cdl).create().show();
		} else {
			onMenuClicked(action, listener);
		}
	}

	public void startAction(int action, int title, ProgressListener listener) {
		startAction(action, title, listener, false, true);
	}

	public void startAction(int action, int title, ProgressListener listener,
			boolean waitOnStop, boolean showDialog) {
		ArrayList<Path> ids = mSelectionManager.getSelected(false);
		stopTaskAndDismissDialog();

		Activity activity = mActivity;
		mDialog = createAuroraProgressDialog(activity, title, ids.size());
		// mDialog = createProgressDialog(activity, title, ids.size());
		if (showDialog) {
			mDialog.show();
		}
		MediaOperation operation = new MediaOperation(action, ids, listener);
		mTask = mActivity.getThreadPool().submit(operation, null);
		mWaitOnStop = waitOnStop;
	}

	public static String getMimeType(int type) {
		switch (type) {
		case MediaObject.MEDIA_TYPE_IMAGE:
			return GalleryUtils.MIME_TYPE_IMAGE;
		case MediaObject.MEDIA_TYPE_VIDEO:
			return GalleryUtils.MIME_TYPE_VIDEO;
		default:
			return GalleryUtils.MIME_TYPE_ALL;
		}
	}
	//paul add for UI_20 start
	private boolean mToFavorite = false;//wenyongzhe new_ui 2016.1.27
	public void setFavoriteState(boolean toSet){
		mToFavorite = toSet;
	}

	public boolean setItemsFavorite(){
		
		String Ids = mSelectionManager.getSelected();
		if(TextUtils.isEmpty(Ids)) {
			return false;
		}

		ContentValues values = new ContentValues();
		if(mToFavorite){
			values.put("favorite", 1);
		} else {
			values.put("favorite", 0);
		}
		mActivity.getContentResolver().update(MediaStore.Files.getContentUri("external"), values, MediaStore.Files.FileColumns._ID+" in ("+Ids+")", null);
		mToFavorite = true;
		return true;
	}
	//paul add for UI_20 end
	private boolean execute(DataManager manager, JobContext jc, int cmd,
			Path path) {
		boolean result = true;
		// long startTime = System.currentTimeMillis();

		switch (cmd) {
		case R.id.action_delete:
			manager.delete(path);
			break;
		case R.id.action_rotate_cw:
			manager.rotate(path, 90);
			break;
		case R.id.action_rotate_ccw:
			manager.rotate(path, -90);
			break;
		case R.id.action_toggle_full_caching: {
			MediaObject obj = manager.getMediaObject(path);
			int cacheFlag = obj.getCacheFlag();
			if (cacheFlag == MediaObject.CACHE_FLAG_FULL) {
				cacheFlag = MediaObject.CACHE_FLAG_SCREENNAIL;
			} else {
				cacheFlag = MediaObject.CACHE_FLAG_FULL;
			}
			obj.cache(cacheFlag);
			break;
		}
		case R.id.action_show_on_map: {
			MediaItem item = (MediaItem) manager.getMediaObject(path);
			double latlng[] = new double[2];
			item.getLatLong(latlng);
			if (GalleryUtils.isValidLocation(latlng[0], latlng[1])) {
				GalleryUtils.showOnMap(mActivity, latlng[0], latlng[1]);
			}
			break;
		}
		case R.id.action_import: {
			MediaObject obj = manager.getMediaObject(path);
			result = obj.Import();
			break;
		}
		// SQF ADDED ON 2015.4.24 begin
		case R.id.action_upload_to_xcloud:
			//mPopupHandler.sendEmptyMessageDelayed(MSG_POPUP, 200);
			break;
		// SQF ADDED ON 2015.4.24 end
		default:
			throw new AssertionError();
		}
		// Log.v(TAG, "zll It takes " + (System.currentTimeMillis() - startTime)
		// + " ms to execute cmd for " + path);
		return result;
	}

	// SQF ADDED ON 2015.4.24 begin
	private int MSG_POPUP = 100;
	Handler mPopupHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (msg.what == MSG_POPUP) {
				Log.i("SQF_LOG", "action_upload_to_xcloud : pop up window ");
				View rootView = mActivity.findViewById(R.id.gallery_root);
				ArrayList<MediaFileInfo> filePaths = getSelectedFilePath();
				if(mSelectedFilesAreAllVideo || mSelectedFilesContainsVideo) {
					Toast.makeText(mActivity, R.string.aurora_not_support_uplaod_video, Toast.LENGTH_SHORT).show();
				}
				if(mSelectedFilesContainsUnspportedFormat) {
					Toast.makeText(mActivity, R.string.aurora_not_support_file_format, Toast.LENGTH_SHORT).show();
				}
				if(mSelectedFilesAreAllVideo) {
					return;
				}
				if(filePaths.isEmpty()) return; 
				String format = mActivity.getResources().getString(R.string.aurora_upload_sum_string_format);
				String tip = String.format(format, ""+ filePaths.size());
				//wenyongzhe 2015.9.30  popowindow->fragment start
				mActivity.getLocalPopupWindowUtil()
						.showSelectPopupWindow(tip, rootView,
								mActivity.getOperationUtil(), filePaths, MenuExecutor.this);
				//wenyongzhe 2016.1.30
				mSelectionManager.leaveSelectionMode();
				
//				mActivity.getmLocalFragmentWindowUtil()
//				.showSelectPopupWindow(tip, rootView,
//						mActivity.getOperationUtil(), filePaths, MenuExecutor.this);
				//wenyongzhe 2015.9.30  popowindow->fragment end
			}
		}

	};
	
	private boolean mSelectedFilesAreAllVideo = false;
	private boolean mSelectedFilesContainsVideo = false;
	private boolean mSelectedFilesContainsUnspportedFormat = false;
//wenyongzhe 2016.3.8 new_ui
	public ArrayList<MediaFileInfo> getSelectedFilePath() {
		mSelectedFilesContainsUnspportedFormat = false;
		ArrayList<MediaFileInfo> filePaths = new ArrayList<MediaFileInfo>();
		if(mSelectedPaths == null || mSelectedPaths.size() == 0) return filePaths;
		mSelectedFilesAreAllVideo = true;
		mSelectedFilesContainsVideo = false;
		for(int i = mSelectedPaths.size() - 1; i >= 0; i--) {
		//for(int i=0; i<mSelectedPaths.size(); i++) {
			Log.i("SQF_LOG", "paths:" + mSelectedPaths.get(i));
			MediaObject obj = mActivity.getDataManager().getMediaObject(mSelectedPaths.get(i));
			if(obj instanceof LocalImage) {
				LocalImage image = (LocalImage)obj;
				if( ! XCloudUploadFilter.postfixShouldBeFiletered(image.filePath)) {
					MediaFileInfo mMediaFileInfo = new MediaFileInfo();
					mMediaFileInfo.filePath = image.filePath;
					mMediaFileInfo.favorite = image.isFavorite();
					filePaths.add(mMediaFileInfo);
				} else {
					mSelectedFilesContainsUnspportedFormat = true;
				}
				mSelectedFilesAreAllVideo = false;
				Log.i("SQF_LOG", "MenuExecutor::getSelectedFilePath --> LocalImage");
			} else if(obj instanceof LocalVideo) {
				mSelectedFilesContainsVideo = true;
				Log.i("SQF_LOG", "MenuExecutor::getSelectedFilePath --> LocalVideo");
			} else {
				Log.i("SQF_LOG", "MenuExecutor::getSelectedFilePath --> eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
			}
		}
		return filePaths;
	}

	// SQF ADDED ON 2015.4.24 end

	private class MediaOperation implements Job<Void> {
		private final ArrayList<Path> mItems;
		private final int mOperation;
		private final ProgressListener mListener;

		public MediaOperation(int operation, ArrayList<Path> items,
				ProgressListener listener) {
			mOperation = operation;
			mItems = items;
			mListener = listener;
		}

		@Override
		public Void run(JobContext jc) {
			int index = 0;
			DataManager manager = mActivity.getDataManager();
			int result = EXECUTION_RESULT_SUCCESS;
			try {
				onProgressStart(mListener);
				for (Path id : mItems) {
					if (jc.isCancelled()) {
						result = EXECUTION_RESULT_CANCEL;
						break;
					}
					if (!execute(manager, jc, mOperation, id)) {
						result = EXECUTION_RESULT_FAIL;
					}
					onProgressUpdate(++index, mListener);
				}
			} catch (Throwable th) {
				Log.e(TAG, "zll failed to execute operation " + mOperation
						+ " : " + th);
			} finally {
				onProgressComplete(result, mListener);
			}
			return null;
		}
	}

	// Aurora <zhanggp> <2013-12-06> added for gallery begin
	public void onMenuClickedEx(int action, String confirmMsg,
			final ProgressListener listener) {
		if (confirmMsg != null) {
			Log.i("SQF_LOG", "===================================1111");
			if (listener != null)
				listener.onConfirmDialogShown();
			ConfirmDialogListener cdl = new ConfirmDialogListener(action,
					listener);
			// Aurora <SQF> <2014-6-20> for NEW_UI begin
			if (mAlertDialogShowStatusListener != null) {
				mAlertDialogShowStatusListener.notifyAlertDialogStatus(true);
			}
			// Aurora <SQF> <2014-6-20> for NEW_UI end
			//View mView = LayoutInflater.from(mActivity).inflate(R.layout.photo_dele_dialog, null);//wenyongzhe 2016.3.9
			new AuroraAlertDialog.Builder(mActivity.getAndroidContext())
					//.setView(mView)
					.setTitle(confirmMsg)
					.setOnCancelListener(cdl)
					.setPositiveButton(R.string.ok, cdl)
					.setNegativeButton(R.string.cancel, cdl).create().show();
			//AuroraTextView	dialogText =(AuroraTextView)mView.findViewById(R.id.dialog_text);
			//dialogText.setText(confirmMsg);
		} else {
			Log.i("SQF_LOG", "===================================22222");
			onMenuClicked(action, listener);
		}
	}

	// Aurora <zhanggp> <2013-12-06> added for gallery end

	// Iuni <lory><2014-04-01> add begin
	private boolean mbAlbumPage = false;

	public void onAuroraMenuClickedEx(int action, String confirmMsg,
			final ProgressListener listener, boolean bAlbumPage) {
		// Log.i(TAG, "zll ---- onAuroraMenuClickedEx bAlbumPage:"+bAlbumPage);
		mbAlbumPage = bAlbumPage;
		onMenuClickedEx(action, confirmMsg, listener);
	}

	public void auroraPause() {
		// Log.i(TAG, "zll ---- auroraPause ----");
		stopTaskAndDismissDialog();
	}

	public boolean getDialogActive() {
		if (mDialog != null) {
			return mDialog.isShowing();
		} else {
			return false;
		}
	}
	// Iuni <lory><2014-04-01> add end
}
