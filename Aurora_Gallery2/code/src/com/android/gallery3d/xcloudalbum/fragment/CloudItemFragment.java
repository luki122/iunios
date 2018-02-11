package com.android.gallery3d.xcloudalbum.fragment;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;

import com.android.gallery3d.R;
import com.android.gallery3d.app.Gallery;
import com.android.gallery3d.app.GalleryAppImpl;
import com.android.gallery3d.filtershow.CustomActionBarManager.ActionBarType;
import com.android.gallery3d.filtershow.category.AuroraAction.AuroraActionType;
import com.android.gallery3d.xcloudalbum.CloudActivity;
import com.android.gallery3d.xcloudalbum.tools.BaiduAlbumUtils;
import com.android.gallery3d.xcloudalbum.tools.DownloadTaskListManager;
import com.android.gallery3d.xcloudalbum.tools.LocalPopupWindowUtil;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.android.gallery3d.xcloudalbum.tools.OperationUtil;
import com.android.gallery3d.xcloudalbum.tools.PopupWindowUtil;
import com.android.gallery3d.xcloudalbum.tools.ToastUtils;
import com.android.gallery3d.xcloudalbum.tools.Utils;
import com.android.gallery3d.xcloudalbum.tools.DownloadTaskListManager.FileDownloadInfo;
import com.android.gallery3d.xcloudalbum.uploaddownload.UploadTaskListManager;
import com.android.gallery3d.xcloudalbum.uploaddownload.XCloudTaskListenerManager;
import com.android.gallery3d.xcloudalbum.widget.CloudItemAdapter;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;
import com.baidu.xcloud.pluginAlbum.bean.ErrorCode;
import com.android.gallery3d.xcloudalbum.inter.IBaiduTaskListener;
import com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean;
import com.baidu.xcloud.pluginAlbum.AlbumConfig;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;

import java.io.File;

import android.text.TextUtils;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;

import com.android.gallery3d.selectfragment.XcloudMoveFragmentUtil;
import com.android.gallery3d.util.NetworkUtil;

public class CloudItemFragment extends BasicFragment {
	private static final String TAG = "CloudItemFragment";

	//wenyongzhe 2.15.9.14 popo-> fragemet
//	private PopupWindowUtil popupWindowUtil;
	private XcloudMoveFragmentUtil mFragmentUtil;

	public static final int CLOUDREQUESTCODE = 112;
	public static final String FROM_XCLOUD_MULTLI_SELECTION = "FROM_XCLOUD_MULTLI_SELECTION";

	private CloudItemAdapter adapter;

	public CloudItemAdapter getAdapter() {
		return adapter;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.aurora_cloud_album_item, container,
				false);
	}

	//wenyongzhe 2015.10.9 start
	@Override
	public void cancelOperation() {
		//TODO Auto-generated method stub
		super.cancelOperation();
		if(adapter != null) {
			adapter.notifyDataSetInvalidated();
		}
	}
	//wenyongzhe 2015.10.9 end
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		setGridView((GridView) getView().findViewById(R.id.cloud_album_item));
		super.onActivityCreated(savedInstanceState);
		setChange(0);
		getCloudActivity().setRootView(getView());

		getAuroraActionBar().changeAuroraActionbarType(
				AuroraActionBar.Type.Normal);
		getCloudActivity().showHomeButtonView(true);
		try {
			LogUtil.d(TAG, "getCloudActivity().getFileInfo()::"
					+ getCloudActivity().getFileInfo());
			getCloudActivity()
					.setAuroraActionBarTitle(
							Utils.getPathNameFromPath(getCloudActivity()
									.getFileInfo().path));
			getCloudActivity().changeActionBarLayout();
			getCloudActivity().setAuroraMenuCallBackListener();
		} catch (Exception e) {
			e.printStackTrace();
		}

		getAuroraActionBar().initActionBottomBarMenu(
				R.menu.aurora_cloud_album_item_menu, 3);
		showLoadingImage(false);

		setImageInfos(getCloudActivity().getItemFileInfos());
		if (getImageInfos() == null) {
			setImageInfos(new ArrayList<CommonFileInfo>());
		}
		adapter = new CloudItemAdapter(getImageInfos(), this);
		getGridView().setAdapter(adapter);
		getBaiduAlbumUtils().setAlbumInfos(getImageInfos());
		showEmptyView();
		// Aurora <paul> <2015-4-22> add start
		getBaiduAlbumUtils().setBaiduTaskListener(this);
		// Aurora <paul> <2015-4-22> add end

		//wenyongzhe popo->fragment start
//		if (popupWindowUtil == null) {
//			popupWindowUtil = getCloudActivity().getPopupWindowUtil();
//		}
		if (mFragmentUtil == null) {
			mFragmentUtil = getCloudActivity().getmFragmentUtil();
		}
		//wenyongzhe popo->fragment end
	}

	//wenyongzhe 2015/9/15 move to cloudActivity
//	public void changeActionBarLayout() {
//		getAuroraActionBar().changeItemLayout(R.layout.aurora_album_add,
//				R.id.aurora_album_add_liner);
//		ImageButton imageButton = (ImageButton) getAuroraActionBar()
//				.getRootView().findViewById(R.id.aurora_album_add);
//		if (imageButton != null) {
//			if (!NetworkUtil.checkWifiNetwork(getCloudActivity())) {
//				imageButton.setClickable(false);
//				return;
//			}
//			imageButton.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					// 本地相册
//					Intent intent = new Intent(getCloudActivity(),
//							Gallery.class);
//					intent.putExtra(FROM_XCLOUD_MULTLI_SELECTION, true);
//					startActivityForResult(intent, CLOUDREQUESTCODE);
//				}
//			});
//		}
//		getCloudActivity().setAuroraActionBarTitle(Utils.getPathNameFromPath(getCloudActivity().getFileInfo().path));
//	}

	//wenyongzhe 2015.9.11
	@Override
	public void onHiddenChanged(boolean hidden) {
		//TODO Auto-generated method stub
		super.onHiddenChanged(hidden);
		if( ! hidden ){
			//changeActionBarLayout();
			getCloudActivity().setAuroraActionBarTitle(Utils.getPathNameFromPath(getCloudActivity().getFileInfo().path));
			getCloudActivity().setAuroraMenuCallBackListener();
		}else{
			
		}
	}

	//wenyongzhe 2015/9/15 move to cloudActivity
//	@Override
//	public void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//		LogUtil.d(TAG, "requestCode::" + requestCode + " resultCode::"
//				+ resultCode + " data::" + data);
//		if (!NetworkUtil.checkWifiNetwork(getCloudActivity())) {
//			ToastUtils.showTast(getCloudActivity(), R.string.aurora_network_not_wifi);
//			return;
//		}
//		if (requestCode == CLOUDREQUESTCODE && resultCode == Activity.RESULT_OK) {
//			ArrayList<String> temps = ((GalleryAppImpl) getCloudActivity()
//					.getApplication()).getSelectedFilesForXCloud();
//			ArrayList<String> paths = new ArrayList<String>(temps);
//			temps.clear();
//			getCloudActivity().getOperationUtil().uploadToAlbum(paths,
//					getCloudActivity().getFileInfo());
//		}
//	}

	private Runnable mTimeOutRunnable = new Runnable() {
		@Override
		public void run() {
			Log.i(TAG, "time out!!");
			loadingEnd();
		}
	};

	private Handler mHandler = new Handler() {
	};

	private void loadingEnd() {
		mHandler.removeCallbacks(mTimeOutRunnable);
		mViewingImg = null;
		showLoadingImage(false);
	}

	@Override
	public void auroraOnItemClick(AdapterView<?> parent, View view,
			int position, long id) {
		super.auroraOnItemClick(parent, view, position, id);
		// Aurora <paul> <2015-4-22> add start
		//wenyongzhe 2015.9.21
		if (null != mViewingImg|| !NetworkUtil.checkNetwork(getCloudActivity())) {
//		if (null != mViewingImg|| !NetworkUtil.checkWifiNetwork(getCloudActivity())) {

			return;
		}

		showLoadingImage(true);
		startToView(position);
		getBaiduAlbumUtils().downloadFromBaidu(position);
		mHandler.postDelayed(mTimeOutRunnable, 40000);
		// Aurora <paul> <2015-4-22> add end
	}

	@Override
	public boolean auroraOnItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		if (isOperationFile()) {
			return true;
		}
		CommonFileInfo fileInfo = (CommonFileInfo) adapter.getItem(position);
		getSelectImages().add(fileInfo);
		setOperationFile(true);
		updateAuroraitemActionBarState();
		updateAuroraItemBottomBarState(false);
		showOrHideMenu(true);
		return true;
	}

	@Override
	public void onRightClick() {
		super.onRightClick();
		updateAuroraitemActionBarState();
	}

	@Override
	public boolean onFragmentBack() {
		getCloudActivity().selectPage(CloudActivity.CLOUDMAIN);
		return true;
	}

	@Override
	public void delComplete(boolean success) {
		if (!isAdded()) {
			return;
		}
		if (success) {
			getBaiduAlbumUtils().clearAfterDelete(getCloudActivity());// paul
																		// add
			getImageInfos().removeAll(getSelectImages());
			cancelOperation();
			adapter.notifyDataSetChanged();
			ToastUtils.showTast(getCloudActivity(), R.string.aurora_del_ok);
			getBaiduAlbumUtils().getHttpCacheManager().removeCache(
					getCloudActivity().getFileInfo().path);
			getCloudActivity().setFragmentBackRefresh(true);
		} else {
			ToastUtils.showTast(getCloudActivity(), R.string.aurora_del_error);
		}
		showLoadingImage(false);
		showEmptyView();
	}

	@Override
	public void moveOrCopyComplete(boolean success, boolean isMove,
			int errorCode) {
		if (!isAdded()) {
			return;
		}
		if (success) {
			if (getCloudActivity().isShowPopupWindow()) {
				getCloudActivity().dismissSelectPopupWindow();
			}
			if (isMove) {
				getImageInfos().removeAll(getSelectImages());
				getBaiduAlbumUtils().getHttpCacheManager().removeCache(
						getCloudActivity().getFileInfo().path);
			}
			CommonFileInfo fileInfo = getOperationUtil().getMoveOrCopyTarget();
			if (fileInfo != null) {
				getBaiduAlbumUtils().getHttpCacheManager().removeCache(
						fileInfo.path);
				getOperationUtil().setMoveOrCopyTarget(null);
			}
			cancelOperation();
			adapter.notifyDataSetChanged();
			ToastUtils.showTast(getCloudActivity(), R.string.aurora_move_ok);
			getCloudActivity().setFragmentBackRefresh(true);
		} else {
			if (errorCode == ErrorCode.Error_File_Already_Exist) {
				ToastUtils.showTast(getCloudActivity(),
						R.string.aurora_move_error_file_already_exist);
				return;
			}
			ToastUtils.showTast(getCloudActivity(), R.string.aurora_move_error);
		}
		showEmptyView();
	}

	@Override
	public void createAlbumComplete(boolean success) {
		super.createAlbumComplete(success);
		if (!isAdded()) {
			return;
		}
		if (success) {
			getOperationUtil().moveOrCopyPhoto();

		}
	}

	private void downloadNotification() {
		if (Utils.isRunningForeground(getCloudActivity())) {
			ToastUtils.showTast(getCloudActivity(),
					R.string.aurora_download_complete);
			return;
		}
		NotificationManager mNotifManager = (NotificationManager) getCloudActivity()
				.getSystemService(Context.NOTIFICATION_SERVICE);
		final Notification.Builder builder = new Notification.Builder(
				getCloudActivity());
		builder.setWhen(System.currentTimeMillis());
		builder.setAutoCancel(true);
		builder.setSmallIcon(android.R.drawable.stat_sys_download_done);
		builder.setContentTitle(getString(R.string.aurora_download_complete));
		Notification notification = builder.build();
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		mNotifManager.notify(0, notification);
	}

	private List<List<CommonFileInfo>> getDownloadTask() {
		return ((GalleryAppImpl) getCloudActivity().getApplication())
				.getDownloadTask();
	}

	private List<CommonFileInfo> getDownlaodList(int position) {
		return getDownloadTask().get(position);
	}

	private int getDownloadProgress(int poistion, int total) {
		return (int) ((Float.valueOf(poistion) / Float.valueOf(total)) * 100);
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	//wenyongzhe 2015.9.18 start
	private String fragmentState = "";
	@Override
	public void onPause() {
		//TODO Auto-generated method stub
		super.onPause();
		fragmentState = "onPause";
		if (mFragmentUtil.isShowProgressPopupWindow()) {
			mFragmentUtil.dismissProgressPopupWindow();
		}
		getCloudActivity().dismissUploadProgress();
	}
	//wenyongzhe 2015.9.18 end

	@Override
	public void onResume() {
		//TODO Auto-generated method stub
		super.onResume();
		getBaiduAlbumUtils().setBaiduTaskListener(this);//wenyongzhe 2015.11.5 Toash
		fragmentState = "onResume";
	}

	@Override
	public void onDestroy() {

	//wenyongzhe 2015.9.14 popo->fragment
//		if (popupWindowUtil.isShowProgressPopupWindow()) {
//			popupWindowUtil.dismissProgressPopupWindow();
//		}
		if (mFragmentUtil.isShowProgressPopupWindow()) {
			mFragmentUtil.dismissProgressPopupWindow();
		}

		getCloudActivity().dismissUploadProgress();
		super.onDestroy();
		LogUtil.d(TAG, "onDestroy");
	}

	private int indexDown = 1;
	private int progress = 0;
	private int taskDown = 0;
	private List<CommonFileInfo> infos = null;

	@Override
	public void baiduTaskStatus(final FileTaskStatusBean bean) {
		if (!isAdded()) {
			return;
		}
		// downloadTaskstatus(bean);

//wenyongzhe popo->fragment
//		if (!popupWindowUtil.isShowProgressPopupWindow()) {
//			popupWindowUtil.setActivity(getCloudActivity());
//			popupWindowUtil.showProgressPopupWindow(getView());
//			popupWindowUtil.getProgressPopupWindow().setJumpDownLoadTab(true);
//		}
		if (!mFragmentUtil.isShowProgressPopupWindow() &&
				(bean.getStatusType() == FileTaskStatusBean.STATUS_PROGRESS||bean.getStatusType() == FileTaskStatusBean.STATUS_BEGIN) && ! "onPause".equals(fragmentState)) {
			mFragmentUtil.setActivity(getCloudActivity());
			mFragmentUtil.showProgressPopupWindow(getView());
			mFragmentUtil.getProgressPopupWindow().setJumpDownLoadTab(true);
		}

//		LogUtil.d(TAG, "bean::"+bean);
		DownloadTaskListManager manager = ((GalleryAppImpl) getCloudActivity()
				.getApplication()).getDownloadTaskListManager();
		manager.updateDownloadStatus(bean);
//		FileDownloadInfo info = manager.getCurrentTaskInfo();
		int complete = manager.getCompleteIndex();
		int total = manager.getDownloadSize();
		int progress = total == 0 ? 0 : (complete * 100 / total);
		LogUtil.d(TAG, "progress+++++++++++++++++++++++"+complete);
//		if(info==null)return;

//wenyongzhe popo->fragment
//		popupWindowUtil.getProgressPopupWindow().displayIconDownLoadImage(bean.getTarget());
//		popupWindowUtil.getProgressPopupWindow().setLoadStatusText(
//				complete + File.separator + total);
//		popupWindowUtil.getProgressPopupWindow().setLoadNumText(
//				manager.getDownloadTaskSize() + "");
//		popupWindowUtil.getProgressPopupWindow().setLoadProgressBar(progress);
//		if (manager.getDownloadTaskSize() == 0) {
//			popupWindowUtil.dismissProgressPopupWindow();
//			downloadNotification();
//			sendBroadcastScan();
//		}
		mFragmentUtil.getProgressPopupWindow().displayIconDownLoadImage(bean.getTarget());
		mFragmentUtil.getProgressPopupWindow().setLoadStatusText(
				complete + File.separator + total);
		mFragmentUtil.getProgressPopupWindow().setLoadNumText(
				manager.getDownloadTaskSize() + "");
		mFragmentUtil.getProgressPopupWindow().setLoadProgressBar(progress);
		if (manager.getDownloadTaskSize() == 0) {
			mFragmentUtil.dismissProgressPopupWindow();
			downloadNotification();
			sendBroadcastScan();
		}

	}

/*	private void downloadTaskstatus(final FileTaskStatusBean bean) {
		if (bean != null) {
			if (getDownloadTask().size() != 0) {
				infos = getDownlaodList(taskDown);
				if (infos == null) {
					return;
				}
				if (!popupWindowUtil.isShowProgressPopupWindow()) {
					if (getView() == null) {
						return;
					}
					popupWindowUtil.setActivity(getCloudActivity());
					popupWindowUtil.showProgressPopupWindow(getView());
					popupWindowUtil.getProgressPopupWindow()
							.setJumpDownLoadTab(true);
				}
				if (bean.getStatusTaskCode() == FileTaskStatusBean.STATE_TASK_RUNNING) {
					CommonFileInfo info = infos.get(indexDown - 1);
					popupWindowUtil.getProgressPopupWindow().displayIconImage(
							info);
					popupWindowUtil.getProgressPopupWindow().setLoadStatusText(
							indexDown + File.separator + infos.size());
					popupWindowUtil.getProgressPopupWindow().setLoadNumText(
							getDownloadTask().size() + "");
				} else if (bean.getStatusTaskCode() == FileTaskStatusBean.STATE_TASK_DONE) {
					progress = getDownloadProgress(indexDown, infos.size());
					popupWindowUtil.getProgressPopupWindow()
							.setLoadProgressBar(progress);
					if (indexDown < infos.size()) {
						indexDown++;
					}
					LogUtil.d(TAG, "StatusType::" + bean.getStatusType()
							+ " TaskCode::" + bean.getStatusTaskCode()
							+ " indexDown::" + indexDown + " progress::"
							+ progress + " size::" + infos.size());

					if (progress == 100 && bean.getStatusType() == 2) {
						handler.postDelayed(new Runnable() {

							@Override
							public void run() {

								indexDown = 1;
								LogUtil.d(TAG, "getDownloadTask() start:::"
										+ getDownloadTask().size());
								synchronized (getDownloadTask()) {
									if (getDownloadTask().size() > 0) {
										getDownloadTask().remove(taskDown);
									}
								}
								LogUtil.d(TAG, "getDownloadTask() end:::"
										+ getDownloadTask().size());
								popupWindowUtil.getProgressPopupWindow()
										.setLoadProgressBar(0);
								getCloudActivity().getOperationUtil()
										.downloadPhoto();// next task
								if (getDownloadTask().size() == 0) {
									popupWindowUtil
											.dismissProgressPopupWindow();
									ToastUtils.showTast(getCloudActivity(),
											R.string.aurora_download_complete);
									downloadNotification();
									sendBroadcastScan();
								}

							}
						}, 1000);
					}

				} else if (bean.getStatusTaskCode() == FileTaskStatusBean.STATE_TASK_PAUSE) {
					synchronized (getDownloadTask()) {
						if (getDownloadTask().size() > 0) {
							getDownloadTask().remove(taskDown);
						}
					}
					if (taskDown < getDownloadTask().size()) {
						taskDown++;
					} else {
						taskDown = 0;
					}
					popupWindowUtil.getProgressPopupWindow()
							.setLoadProgressBar(0);
					getCloudActivity().getOperationUtil().downloadPhoto(
							taskDown);// next task

				} else if (bean.getStatusTaskCode() == FileTaskStatusBean.STATE_TASK_CANCELLED) {
					if (indexDown < infos.size()) {
						indexDown++;
					}
				}

			}
		}
	}*/

	public final static String ACTION_DIR_SCAN = "android.intent.action.AURORA_DIRECTORY_SCAN";

	public void sendBroadcastScan() {
		Intent intent = new Intent();
		intent.setAction(ACTION_DIR_SCAN);
		intent.setData(Uri.fromFile(new File(Environment
				.getExternalStorageDirectory() + "/DCIM/cloud")));
		getCloudActivity().sendBroadcast(intent);
	}

	@Override
	public void baiduUploadTaskStatus(final FileTaskStatusBean bean) {
		LogUtil.d(TAG, " baiduUploadTaskStatus isAdded()::" + isAdded());
		if (!isAdded()) {
			return;
		}
		getCloudActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				getCloudActivity().showUploadProgress();
				UploadTaskListManager manager = ((GalleryAppImpl) getCloudActivity()
						.getApplication()).getUploadTaskListManager();
				manager.updateFileUploadTaskInfo(bean);
				getCloudActivity().updateUploadProgress(bean);
			}
		});
	}

	private Handler handler = new Handler() {
	};

	// move here // Aurora <paul> <2015-4-22> add start

	@Override
	public boolean onBack() {
		if (null != mViewingImg) {
			loadingEnd();
			return true;
		}
		return super.onBack();
	}

	@Override
	public void baiduDownloadTaskStatus(FileTaskStatusBean bean) {
		if (!isAdded()) {
			return;
		}
		if (bean != null) {
			if (bean.getStatusTaskCode() == FileTaskStatusBean.STATE_TASK_DONE) {
				final String name = bean.getFileName();
				getBaiduAlbumUtils().setDownloaded(name);

				if (null != mViewingImg) {
					if (mViewingImg.equals(name)) {
						viewImg(mViewingPos);
						loadingEnd();
					}
				}
			} else if (bean.getStatusTaskCode() == FileTaskStatusBean.STATE_TASK_FAILED) {
				getBaiduAlbumUtils().onDownloadError(bean.getFileName());
			}
		}
	}

	private String mViewingImg = null;
	private int mViewingPos = -1;

	public void viewImg(int pos) {
		Log.d(TAG, "============= viewImg ============");
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		intent.setAction("aurora.cloud.action.VIEW");
		Bundle bundle = new Bundle();
		intent.putExtra("position", pos);
		intent.setDataAndType(
				Uri.fromFile(new File(getBaiduAlbumUtils().getViewingPath())),
				"image/*");
		try {
			getCloudActivity().startActivity(intent);
		} catch (Exception e) {

		}
	}

	private void startToView(int position) {
		mViewingPos = -1;
		mViewingImg = null;
		String filePath = ((CommonFileInfo) adapter.getItem(position)).path;
		if (TextUtils.isEmpty(filePath)) {
			return;
		}
		mViewingImg = Utils.getNameFromFilepath(filePath);
		mViewingPos = position;
	}

	// Aurora <paul> <2015-4-22> add end

	@Override
	public void baiduPhotoList(List<CommonFileInfo> list, boolean isDirPath,
			CommonFileInfo infos) {
		//wenyongzhe  2015.10.29 click back  crash BUG 
		if (!isAdded()) {
			return;
		}
		super.baiduPhotoList(list, isDirPath, infos);
		if (isDirPath || infos == null) {
			return;
		}
		getBaiduAlbumUtils().setAlbumInfos(list);
		setImageInfos(list);
		LogUtil.d(TAG, "baiduPhotoList infos ::" + infos + " isVisible()::"
				+ isVisible());
		LogUtil.d(TAG, "remove::" + getCloudActivity().getFileInfo().path);
		getBaiduAlbumUtils().getHttpCacheManager().removeCache(
				getCloudActivity().getFileInfo().path);
		adapter.setFileInfos(list);
		adapter.notifyDataSetChanged();
		getCloudActivity().setFragmentBackRefresh(true);
		showEmptyView();

	}

	private void showEmptyView() {
		//wenyongzhe  2015.10.29 click back  crash BUG
		if (!isAdded()) {
			return;
		}
		if (adapter.getCount() == 0) {
			setEmptyViewText(R.string.aurora_album_no_picture);
		} else {
			setEmptyViewText("");
		}
	}

}
