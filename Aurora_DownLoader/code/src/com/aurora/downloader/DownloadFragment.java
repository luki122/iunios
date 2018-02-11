package com.aurora.downloader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Fragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Toast;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraListView.AuroraBackOnClickListener;
import aurora.widget.AuroraListView.AuroraDeleteItemListener;

import com.aurora.downloader.DownloadActivity.IBackPressedListener;
import com.aurora.downloader.util.AuroraDownloadManager;
import com.aurora.downloader.util.AuroraLog;
import com.aurora.downloader.util.IntentBuilder;
import com.aurora.downloader.util.NetWorkConectUtil;
import com.aurora.downloader.util.OperationAction;
import com.aurora.downloader.util.OperationAction.OperationInterfaceLisenter;
import com.aurora.downloader.util.Util;
import com.aurora.downloader.widget.AuroraDownLoadListViewCache;
import com.aurora.downloader.widget.AuroraLoadAndEmptyView;
import com.aurora.downloader.widget.DownloadAdapter;
import com.aurora.downloader.R;
import com.aurora.downloader.util.DownloadStateUtil.Status;

/**
 * 
 * @author jiangxh
 * @CreateTime 2014年5月28日 上午10:56:20
 * @Description com.aurora.downloader DownloadFragment.java
 */
public class DownloadFragment extends Fragment implements IBackPressedListener,
		OperationInterfaceLisenter {
	private AuroraListView downloadListView;
	private DownloadAdapter downloadAdapter;
	private DownloadActivity downloadActivity;
	private Cursor sortCursor;
	private AuroraDownloadManager downloadManager;
	private AuroraActionBar auroraActionBar;
	private static final String TAG = "DownloadFragment";
	private List<FileInfo> selectPath = new ArrayList<FileInfo>();
	private int mFileNameColumnId;
	private OperationAction operationAction;
	private AuroraLoadAndEmptyView loadAndEmptyView;

	/**
	 * @return the selectPath
	 */
	public List<FileInfo> getSelectPath() {
		return selectPath;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		downloadActivity = (DownloadActivity) activity;
		operationAction = downloadActivity.operationAction;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_download, container,
				false);
		downloadListView = (AuroraListView) view
				.findViewById(R.id.download_listView);
		loadAndEmptyView = new AuroraLoadAndEmptyView(view);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		auroraActionBar = downloadActivity.auroraActionBar;
		auroraActionBar.initActionBottomBarMenu(R.menu.download_menu, 2);

		downloadListView.auroraEnableSelector(true);
		downloadListView.auroraSetNeedSlideDelete(true);
		downloadActivity.setAuroraActionBarTitle(R.string.category_download);
		downloadManager = new AuroraDownloadManager(
				downloadActivity.getContentResolver(),
				downloadActivity.getPackageName());
		downloadManager.setAccessAllDownloads(true);
		final AuroraDownloadManager.Query baseQuery = new AuroraDownloadManager.Query()
				.setOnlyIncludeVisibleInDownloadsUi(true);

		sortCursor = downloadManager.query(baseQuery.orderBy(
				DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP,
				DownloadManager.Query.ORDER_DESCENDING));
		downloadActivity.showAllOperationBar(sortCursor);

		AuroraLog.elog(TAG, "sortCursor==" + sortCursor.getCount());
		if (sortCursor != null && !sortCursor.isClosed()) {
			mFileNameColumnId = sortCursor
					.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_FILENAME);
			downloadAdapter = new DownloadAdapter(downloadActivity, sortCursor);
			downloadListView.setAdapter(downloadAdapter);
			showEmptyOrEditView();

		}
		downloadListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (sortCursor == null) {
					AuroraLog.elog(TAG, "sortCursor is null");
					return;
				}
				sortCursor.moveToPosition(position);
				FileInfo fileInfo = Util.getFileInfo(sortCursor);
				// AuroraLog.elog(TAG, fileInfo.toString());
				if (downloadAdapter.isOperation()) {
					View convertView = downloadAdapter.getView(position, view,
							parent);
					AuroraDownLoadListViewCache cache = (AuroraDownLoadListViewCache) convertView
							.getTag();
					AuroraCheckBox checkBox = cache.getCheckBox();
					if (fileInfo != null && (fileInfo.downloadId != 0)) {
						if (selectPath.contains(fileInfo)) {
							selectPath.remove(fileInfo);
							checkBox.setChecked(false);
						} else {
							selectPath.add(fileInfo);
							checkBox.setChecked(true);
						}
						updateAuroraItemBottomBarState();
						updateAuroraitemActionBarState();
					}

				} else if (fileInfo != null && !fileInfo.isExists) {
					if (fileInfo.status.equals(Status.WAITING)) {
						Toast.makeText(
								downloadActivity,
								downloadActivity.getResources().getString(
										R.string.dialog_file_running), 0)
								.show();
					} else {
						showFailedDialog(fileInfo.downloadId,
								getString(R.string.dialog_file_missing_body));
					}
				} else if (fileInfo != null && fileInfo.isCanShare) {
					IntentBuilder.viewFile(downloadActivity, fileInfo.filePath);
				} else if (fileInfo.status.equals(Status.FAILED)) {
					File root = Environment.getExternalStorageDirectory();

					if (root.getUsableSpace() < fileInfo.fileSize) {
						Toast.makeText(
								downloadActivity,
								downloadActivity.getResources().getString(
										R.string.has_no_free_memory), 0).show();
					} else {
						showFailedDialog(fileInfo.downloadId,
								getString(R.string.download_error));
					}
				} else {
					Toast.makeText(
							downloadActivity,
							downloadActivity.getResources().getString(
									R.string.download_no_complete), 0).show();
				}

			}
		});

		downloadListView
				.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
							View view, int position, long id) {
						String path;
						if (!downloadAdapter.isOperation()) {
							downloadAdapter.setAotuChanged(false);
							AuroraLog.elog(TAG,
									"downloadAdapter.isAotuChanged()=="
											+ downloadAdapter.isAotuChanged());
							selectPath.clear();
							if (sortCursor != null) {
								sortCursor.moveToPosition(position);
								FileInfo fileInfo = Util
										.getFileInfo(sortCursor);
								if (fileInfo != null) {
									selectPath.add(fileInfo);
								}
							}
							View convertView = downloadAdapter.getView(
									position, view, parent);
							AuroraDownLoadListViewCache cache = (AuroraDownLoadListViewCache) convertView
									.getTag();
							AuroraCheckBox checkBox = cache.getCheckBox();
							checkBox.setChecked(true);
							// add By jxh 2014-8-28 begin
							// if (position ==
							// downloadListView.getLastVisiblePosition()) {
							// isLastitem = true;
							// }
							// lastBottom = view.getBottom();
							// add By jxh 2014-8-28 end
							doAllOperation();
							downloadAdapter.isShowAnim();
							return true;
						}
						return false;
					}
				});

		downloadListView
				.auroraSetAuroraBackOnClickListener(new AuroraBackOnClickListener() {

					@Override
					public void auroraPrepareDraged(int position) {
						AuroraLog.elog(TAG, "auroraPrepareDraged ");
						downloadAdapter.setAotuChanged(false);
						downloadAdapter.handlerRemomve();
					}

					@Override
					public void auroraOnClick(int position) {
						AuroraLog.log(TAG, "auroraOnClick ");
						if (sortCursor == null || sortCursor.isClosed()) {
							AuroraLog.elog(TAG,
									"sortCursor==null or sortCursor is closed");
							return;
						}
						if (sortCursor.moveToPosition(position)) {
							FileInfo fileInfo = Util.getFileInfo(sortCursor);
							if (operationAction.fastDeleteFiles(fileInfo)) {
								downloadListView.auroraDeleteSelectedItemAnim();
							}
						} else {
							AuroraLog.elog(TAG,
									"sortCursor can not move to position");
						}
					}

					@Override
					public void auroraDragedUnSuccess(int position) {
						AuroraLog.elog(TAG, "auroraDragedUnSuccess ");
						downloadAdapter.setAotuChanged(true);
						downloadAdapter.onContentChanged();
					}

					@Override
					public void auroraDragedSuccess(int position) {
						AuroraLog.elog(TAG, "auroraDragedSuccess ");
						downloadAdapter.setAotuChanged(false);
						downloadAdapter.handlerRemomve();

					}
				});

		downloadListView
				.auroraSetDeleteItemListener(new AuroraDeleteItemListener() {

					@Override
					public void auroraDeleteItem(View view, int position) {
						downloadListView.auroraSetRubbishBack();
						downloadAdapter.setAotuChanged(true);
						downloadAdapter.onContentChanged();
					}
				});
		downloadListView.setOnScrollListener(onScrollListener);

	}

	private OnScrollListener onScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
				downloadAdapter.setAotuChanged(true);
				downloadAdapter.setIconImage(false);
			} else {
				downloadAdapter.setAotuChanged(false);
				downloadAdapter.setIconImage(true);
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {

		}
	};

	private Handler handler = new Handler() {
	};
	public static boolean isPause = false;

	@Override
	public void onResume() {
		super.onResume();
		AuroraLog.elog(TAG, "onResume");
		isPause = false;
		downloadListView.auroraOnResume();
		downloadAdapter.setAotuChanged(true);
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				downloadAdapter.onContentChanged();
			}
		}, 200);
		if (operationAction != null) {
			operationAction.sendBroadcastHidNotif(true);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		AuroraLog.elog(TAG, "onPause");
		isPause = true;
		downloadListView.auroraOnPause();
		downloadAdapter.setAotuChanged(false);
		downloadAdapter.getFileIconHelper().pause();

	}

	@Override
	public void onStop() {
		super.onStop();
		if (operationAction != null) {
			operationAction.sendBroadcastHidNotif(false);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onBack() {
		AuroraLog.elog(TAG, "onBack 0");
		if (downloadActivity.actionBarIsAnimRunning()) {
			return true;
		}
		AuroraLog.elog(TAG, "onBack 1");
		if (downloadListView != null && downloadListView.auroraIsRubbishOut()) {
			downloadListView.auroraSetRubbishBack();
			return true;
		}
		AuroraLog.elog(TAG, "onBack 2");
		if (downloadAdapter.isOperation()) {
			showEditView(false);
			downloadAdapter.isShowAnim();
			return true;
		}

		return false;
	}

	/**
	 * Delete a download from the Download Manager.
	 */
	public void deleteDownload(long downloadId) {
		if (isAdded()) {
			downloadManager.markRowDeleted(downloadId);
		}
	}

	public void notifyDataSetChanged() {
		if (isAdded() && downloadAdapter != null) {
			if (downloadListView != null
					&& downloadListView.auroraIsRubbishOut()) {
				downloadListView.auroraSetRubbishBack();// 执行auroraDeleteItem
														// 会刷新数据
			} else {// 在下拉时 刷新完成数据
				downloadAdapter.onContentChanged();
				downloadAdapter.notifyDataSetChanged();
			}

		}
	}

	private void showFailedDialog(long downloadId, String dialogBody) {
		new AuroraAlertDialog.Builder(downloadActivity)
				.setTitle(R.string.dialog_title_not_available)
				.setMessage(dialogBody)
				.setNegativeButton(R.string.delete_download,
						getDeleteClickHandler(downloadId))
				.setPositiveButton(R.string.retry_download,
						getRestartClickHandler(downloadId)).show();
	}

	/**
	 * @return an OnClickListener to delete the given downloadId from the
	 *         Download Manager
	 */
	private DialogInterface.OnClickListener getDeleteClickHandler(
			final long downloadId) {
		return new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				deleteDownload(downloadId);
			}
		};
	}

	/**
	 * @return an OnClickListener to restart the given downloadId in the
	 *         Download Manager
	 */
	private DialogInterface.OnClickListener getRestartClickHandler(
			final long downloadId) {
		return new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (NetWorkConectUtil.hasNetWorkConection(downloadActivity)) {
					try {
						downloadManager.restartDownload(downloadId);
						downloadAdapter.onContentChanged();
					} catch (Exception e) {
						Toast.makeText(
								downloadActivity,
								downloadActivity.getResources().getString(
										R.string.can_not_redownload), 0).show();
						e.printStackTrace();
						AuroraLog.elog(TAG, e.getMessage());
					}
				} else {
					Toast.makeText(
							downloadActivity,
							downloadActivity.getResources().getString(
									R.string.no_network), 0).show();
				}
			}
		};
	}

	public void auroraSetRubbishBack() {
		if (isAdded()) {
			downloadListView.auroraSetRubbishBack();
		}
	}

	public boolean auroraSetRubbishBackBool() {
		if (isAdded()) {
			if (downloadListView != null
					&& downloadListView.auroraIsRubbishOut()) {
				downloadListView.auroraSetRubbishBack();
				return true;
			}
		}
		return false;
	}

	private void doAllOperation() {
		showEditView(true);

	}

	public void showEditViewByActivity() {
		if (isAdded()) {
			selectPath.clear();
			showEditView(true);
			downloadAdapter.isShowAnim();
		}
	}

	/**
	 * add by jxh 2014-8-27 判断是否是长按状态下
	 */
	private boolean isLastitem = false;

	private int lastBottom;

	/**
	 * 显示或者隐藏ActionBar
	 * 
	 * @param show
	 */
	private void showEditView(boolean show) {
		if (show) {
			downloadAdapter.setOperation(true);
			downloadListView.setLongClickable(false);
			auroraActionBar.setShowBottomBarMenu(true);
			downloadListView.auroraEnableSelector(false);
			downloadListView.auroraSetNeedSlideDelete(false);
			actionBarSetOnClickListener();
		} else {
			auroraActionBar.setShowBottomBarMenu(false);
			downloadListView.setLongClickable(true);
			downloadAdapter.setOperation(false);
			downloadListView.auroraEnableSelector(true);
			downloadListView.auroraSetNeedSlideDelete(true);
		}
		auroraActionBar.showActionBarDashBoard();
		updateAuroraItemBottomBarState();

		// final int editHeight = (int) downloadActivity.getResources()
		// .getDimension(
		// com.aurora.R.dimen.aurora_action_bottom_bar_height);
		// final int fristBottom = (int) downloadListView.getResources()
		// .getDimension(R.dimen.frist_bottom);
		//
		// if (isLastitem || lastBottom - fristBottom < 0
		// && lastBottom - fristBottom + editHeight > 0) {
		// isLastitem = false;
		// handler.postDelayed(new Runnable() {
		//
		// @Override
		// public void run() {
		// downloadListView.smoothScrollBy(lastBottom - fristBottom
		// + editHeight, 0);
		// }
		// }, 100);
		// }
	}

	private TextView leftView, rightView;

	/**
	 * 设置actionBar 左右键监听
	 */
	private void actionBarSetOnClickListener() {
		leftView = (TextView) auroraActionBar.getSelectLeftButton();
		rightView = (TextView) auroraActionBar.getSelectRightButton();
		rightView.setText(R.string.all_select);
		leftView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!downloadActivity.actionBarIsAnimRunning()) {
					AuroraLog.log(TAG, "leftView");
					showEditView(false);
					downloadAdapter.isShowAnim();
				}
			}
		});
		rightView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (downloadActivity.actionBarIsAnimRunning()) {
					return;
				}
				AuroraLog.log(TAG, "rightView");
				if (sortCursor == null || sortCursor.isClosed()) {
					AuroraLog.elog(TAG, "sortCursor is null or isClosed");
					return;
				}
				if (selectPath.size() == sortCursor.getCount()) {
					selectPath.clear();
				} else {
					selectPath.clear();
					for (sortCursor.moveToFirst(); !sortCursor.isAfterLast(); sortCursor
							.moveToNext()) {
						FileInfo fileInfo = Util.getFileInfo(sortCursor);
						if (fileInfo != null) {
							selectPath.add(fileInfo);
						}
					}
				}
				AuroraLog.elog(TAG, "selectPath.size()==" + selectPath.size());
				updateAuroraitemActionBarState();
				updateAuroraItemBottomBarState();
			}
		});
	}

	/**
	 * 改变actionBar 文字显示
	 */
	private void updateAuroraitemActionBarState() {
		if (sortCursor == null || sortCursor.isClosed()) {
			AuroraLog.elog(TAG, "sortCursor is null or isClosed");
			return;
		}
		if (isAdded()) {
			if (selectPath.size() == sortCursor.getCount() && rightView != null) {
				rightView.setText(getResources().getString(
						R.string.invert_select));
			} else if (rightView != null) {
				rightView
						.setText(getResources().getString(R.string.all_select));
			}
		}
	}

	/**
	 * 改变BottomBar 状态
	 */
	public void updateAuroraItemBottomBarState() {
		if (sortCursor == null || sortCursor.isClosed()) {
			AuroraLog.elog(TAG, "sortCursor is null or isClosed");
			return;
		}
		if (isAdded()) {
			AuroraMenu auroraMenu = auroraActionBar
					.getAuroraActionBottomBarMenu();
			downloadAdapter.setSelectPath(selectPath);
			operationAction.setSelectPath(selectPath);
			if (selectPath.size() == 0) {
				auroraMenu.setBottomMenuItemEnable(1, false);
				auroraMenu.setBottomMenuItemEnable(2, false);
			} else {
				auroraMenu.setBottomMenuItemEnable(1, true);
				auroraMenu.setBottomMenuItemEnable(2, true);
			}
			for (int i = 0; i < selectPath.size(); i++) {
				AuroraLog.elog(TAG, "selectPath.get(i).isCanShare=="
						+ selectPath.get(i).isCanShare);
				if (!selectPath.get(i).isExists
						|| !selectPath.get(i).isCanShare) {
					auroraMenu.setBottomMenuItemEnable(1, false);
					break;
				}
			}
			downloadAdapter.notifyDataSetChanged();
		}
	}

	public void showEmptyOrEditView() {
		if (isAdded()) {
			if (sortCursor.getCount() == 0) {
				downloadActivity.showEmptyBarItem();
				loadAndEmptyView.getEmptyView().setVisibility(View.VISIBLE);
			} else {
				downloadActivity.showEditBarItem();
				loadAndEmptyView.getEmptyView().setVisibility(View.GONE);
			}
		}

	}

	@Override
	public void deleteComplete(List<FileInfo> ids) {
		operationAction.mDialogDismiss();
		showEditView(false);
		downloadAdapter.onContentChanged();
		if (sortCursor == null || sortCursor.isClosed()) {
			AuroraLog.elog(TAG, "sortCursor is null or isClosed");
			return;
		}
	}

}
