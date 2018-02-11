package com.aurora.market.activity.setting;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraListView.AuroraBackOnClickListener;
import aurora.widget.AuroraListView.AuroraDeleteItemListener;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;

import com.aurora.market.MarketMainActivity;
import com.aurora.market.R;
import com.aurora.market.activity.BaseActivity;
import com.aurora.market.adapter.DownloadManagerAdapter;
import com.aurora.market.db.AppDownloadDao;
import com.aurora.market.download.DownloadInitListener;
import com.aurora.market.download.DownloadUpdateListener;
import com.aurora.market.download.FileDownloader;
import com.aurora.market.install.InstallNotification;
import com.aurora.market.model.DownloadData;
import com.aurora.market.model.DownloadManagerBean;
import com.aurora.market.service.AppDownloadService;
import com.aurora.market.util.Globals;
import com.aurora.market.widget.stickylistheaders.StickyListHeadersListView;
import com.aurora.market.widget.stickylistheaders.StickyListHeadersListView.OnHeaderClickListener;

public class DownloadManagerActivity extends BaseActivity {

	public static final String TAG = "DownloadManagerActivity";

	private AuroraActionBar mActionBar;
	private TextView leftView;
	private TextView rightView;
	public StickyListHeadersListView mListView;
	private LinearLayout ll_nodata;
	private Button btn_goto;

	private View alertDialogView;
	private TextView tv_message;
	private AuroraCheckBox checkbox;

	private List<DownloadManagerBean> list;
	private List<DownloadManagerBean> downloadingList;
	private List<DownloadManagerBean> downloadedList;
	private DownloadManagerAdapter adapter;

	private boolean editMode = false; // 编辑模式

	private boolean stopFlag = false;

	private int showMoreCount = 10;
	private boolean needShowMore = true;
	private boolean hasShowExpansion = false;
	private boolean isSelectAll = false;
	private boolean alowUpdate = true;
	private int isOpenInstall = 0;
	
	private String mFailPackageName = null;
    private int mInitListPos = -1;
	
	private long lastRefresh;
	public int flagPostion = -1;

	private boolean showDialog = false; // 是否正在显示dialog
	
	private final long openOrCloseEditModeDelay = 300;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setAuroraContentView(R.layout.activity_download_manager,
				AuroraActionBar.Type.Normal);
		getIntentData();

		initViews();
		initActionBar();
		setListener();
		AppDownloadService.checkInit(this, new DownloadInitListener() {

			@Override
			public void onFinishInit() {
				
				initdata();
			}
		});
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		getIntentData();
	}

	/**
	 * @Title: getIntentData
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param
	 * @return void
	 * @throws
	 */
	private void getIntentData() {
		// TODO Auto-generated method stub

		InstallNotification.cancleInsatlledNotify();
		InstallNotification.cancleInstallFailedNotify();
		InstallNotification.cancleInsatllingNotify();

		isOpenInstall = getIntent().getIntExtra("openinstall", 0);

		if (isOpenInstall == 1) {
			InstallNotification.install_success.clear();
		} else if (isOpenInstall == 2) {
		    mFailPackageName = getIntent().getStringExtra("packageName");
			InstallNotification.install_failed.clear();
		}
		Bundle myBundle = getIntent().getExtras();
		if (myBundle != null) {
			ArrayList<DownloadData> upLists = myBundle
					.getParcelableArrayList("updatedata");
			if (upLists != null) {
				for (int i = 0; i < upLists.size(); i++) {
					AppDownloadService.startDownload(this, upLists.get(i));
				}
				InstallNotification.cancleUpdateNotify();
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (stopFlag) {
			updateListener.downloadProgressUpdate();
			stopFlag = false;
		}
		AppDownloadService.registerUpdateListener(updateListener);
	}

	@Override
	protected void onStop() {
		super.onStop();

		stopFlag = true;
		AppDownloadService.unRegisterUpdateListener(updateListener);
		
		if (adapter != null) {
			adapter.clearProgressBtnTag(mListView);
		}
	}

	@Override
	protected void onPause() {
		mListView.auroraOnPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mListView.auroraOnResume();
		super.onResume();
	}

	private void initViews() {
		ll_nodata = (LinearLayout) findViewById(R.id.ll_nodata);
		btn_goto = (Button) findViewById(R.id.btn_goto);
		mListView = (StickyListHeadersListView) findViewById(R.id.mListView);

		mListView.setSelector(R.drawable.list_item_selector);
		mListView.auroraSetNeedSlideDelete(true);

		alertDialogView = getLayoutInflater().inflate(
				R.layout.alert_dialog_delete_confirm, null);
		tv_message = (TextView) alertDialogView.findViewById(R.id.message);
		checkbox = (AuroraCheckBox) alertDialogView.findViewById(R.id.checkbox);
	}

	private void initActionBar() {
		mActionBar = getAuroraActionBar();
		mActionBar.setTitle(R.string.download_manager_pref);
		mActionBar.setBackground(getResources().getDrawable(
				R.drawable.aurora_action_bar_top_bg_green));
		
		setAuroraBottomBarMenuCallBack(new OnAuroraMenuItemClickListener() {
			@Override
			public void auroraMenuItemClick(int paramInt) {
				switch (paramInt) {
				case R.id.delete:
					showSelectDeleteDialog();
					break;
				}
			}
		});
		
//		setAuroraMenuCallBack(new OnAuroraMenuItemClickListener() {
//			@Override
//			public void auroraMenuItemClick(int paramInt) {
//				switch (paramInt) {
//				case R.id.delete:
//					showSelectDeleteDialog();
//					break;
//				}
//			}
//		});
		
		mActionBar.initActionBottomBarMenu(R.menu.menu_download_manager, 1);
		
		mActionBar.setmOnActionBarBackItemListener(new OnAuroraActionBarBackItemClickListener() {
			
			@Override
			public void onAuroraActionBarBackItemClicked(int itemId) {
				// TODO Auto-generated method stub
				Log.i(TAG, "setmOnActionBarBackItemListener back");
				Intent intent = new Intent(DownloadManagerActivity.this,
						MarketManagerPreferenceActivity.class);
				
				Intent intent1 = new Intent(DownloadManagerActivity.this,
						MarketMainActivity.class);
				ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
				List<RunningTaskInfo> appTask = am.getRunningTasks(1);
				boolean ifFront = false;
				for(RunningTaskInfo app:appTask)
				{
					if(app.baseActivity.equals(intent1.getComponent()))
					{
						ifFront = true;
						break;
					}
				}
				
				if (ifFront) {
					
					//startActivity(intent);
					finish();
					//overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
					Log.i(TAG, "setmOnActionBarBackItemListener back1");
				} else {
					
					finish();
					startActivity(intent);
					overridePendingTransition(com.aurora.R.anim.aurora_activity_close_enter,com.aurora.R.anim.aurora_activity_close_exit);
					Log.i(TAG, "setmOnActionBarBackItemListener back2");
				}
			}
		});
	}

	// ============处理StickyListHeadersListView头部悬浮点击事件问题(start)============

	private int screenHeadHeight = 0; // 通知栏高度
	private int screenWidth = 0; // 屏幕宽度

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (screenHeadHeight == 0) {
			Rect frame = new Rect();
			getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
			screenHeadHeight = frame.top;
		}
		if (screenWidth == 0) {
			screenWidth = getResources().getDisplayMetrics().widthPixels;
		}
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			if (mListView.getHeaderBottomPosition() != 0
					&& ev.getRawY() > (mActionBar.getHeight() + screenHeadHeight)
					&& ev.getRawY() < (mActionBar.getHeight()
							+ mListView.getHeaderBottomPosition() + screenHeadHeight)) {

				if (ev.getRawX() > screenWidth * (4 / (5 * 1.0f))
						&& mListView.getmCurrentHeaderId() == DownloadManagerBean.TYPE_DOWNLOADED) {
					mListView.auroraSetRubbishBack();
					showDeleteAllFinishDialog();
				}
				return false;

			}
		}

		return super.dispatchTouchEvent(ev);
	}

	// ============处理StickyListHeadersListView头部悬浮点击事件问题(end)============

	private void setListener() {
		btn_goto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent i = new Intent(DownloadManagerActivity.this,
						MarketMainActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
			}
		});

		mListView.setOnHeaderClickListener(new OnHeaderClickListener() {

			@Override
			public void onHeaderClick(StickyListHeadersListView l, View header,
					int itemPosition, long headerId, boolean currentlySticky) {
				if (currentlySticky) {
					long id = adapter.getHeaderId(itemPosition);
					if (id == DownloadManagerBean.TYPE_DOWNLOADING) {

					} else {
						mListView.auroraSetRubbishBack();
						showDeleteAllFinishDialog();
					}
				}
			}
		});

		mListView
				.auroraSetAuroraBackOnClickListener(new AuroraBackOnClickListener() {

					@Override
					public void auroraPrepareDraged(int position) {
						adapter.setmDeleteFlag(false);
						alowUpdate = false;
					}

					@Override
					public void auroraOnClick(final int listViewPosition) {
						Log.i(TAG, "auroraOnClick: " + listViewPosition);
						showDeleteDialog(list.get(listViewPosition));
					}

					@Override
					public void auroraDragedUnSuccess(int position) {
						Log.i(TAG, "auroraDragedUnSuccess: " + position);

						flagPostion = -1;

						alowUpdate = true;
						if (System.currentTimeMillis() - lastRefresh > 1000) {
							updateListener.downloadProgressUpdate();
						}

						int firstVisiblePosition = mListView
								.getFirstVisiblePosition();
						int vIndex = position - firstVisiblePosition;
						View v = mListView.getChildAt(vIndex).findViewById(
								R.id.progressBtn);
						if (v != null) {
							v.setClickable(true);
							v.setEnabled(true);
						}
						v = mListView.getChildAt(vIndex).findViewById(
								R.id.btn_operation);
						if (v != null) {
							v.setEnabled(true);
						}
					}

					@Override
					public void auroraDragedSuccess(int position) {
						Log.i(TAG, "auroraDragedSuccess: " + position);

						flagPostion = position;

						alowUpdate = true;
						if (System.currentTimeMillis() - lastRefresh > 1000) {
							updateListener.downloadProgressUpdate();
						}

						int firstVisiblePosition = mListView
								.getFirstVisiblePosition();
						int vIndex = position - firstVisiblePosition;
						View parent = mListView.getChildAt(vIndex);
						View v = parent.findViewById(R.id.progressBtn);
						if (v != null) {
							v.setClickable(false);
							v.setEnabled(false);
						}
						v = parent.findViewById(R.id.btn_operation);
						if (v != null) {
							v.setEnabled(false);
						}
					}
				});

		mListView.auroraSetDeleteItemListener(new AuroraDeleteItemListener() {

			@Override
			public void auroraDeleteItem(View view, int position) {
				Log.i(TAG, "auroraDeleteItem: " + position);
				adapter.setmDeleteFlag(true);
				DownloadManagerBean bean = list.get(position);
				if (bean.getDownloadData() != null) {
					if (bean.getDownloadStatus() >= FileDownloader.STATUS_INSTALL_WAIT) { // 删除已完成任务
						if (checkbox.isChecked()) {
							Log.i(TAG, "File Delete: "
									+ bean.getDownloadData().getApkName());
							File file = new File(bean.getFilePath());
							file.delete();
						}
						AppDownloadService.getAppDownloadDao().delete(
								bean.getDownloadData().getApkId());
						AppDownloadService.updateDownloadProgress();
					} else {
						AppDownloadService.cancelDownload(
								DownloadManagerActivity.this, list
										.get(position).getDownloadData());
					}
				}

				int firstVisiblePosition = mListView.getFirstVisiblePosition();
				int vIndex = position - firstVisiblePosition;
				View v = mListView.getChildAt(vIndex).findViewById(
						R.id.progressBtn);
				if (v != null) {
					v.setClickable(true);
					v.setEnabled(true);
				}
				v = mListView.getChildAt(vIndex).findViewById(
						R.id.btn_operation);
				if (v != null) {
					v.setEnabled(true);
				}
				
				Intent broadcase = new Intent(Globals.BROADCAST_ACTION_DOWNLOAD);
				sendBroadcast(broadcase);
			}
		});

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				if (hasShowExpansion && position == list.size() - 1) {
					hasShowExpansion = false;
					needShowMore = false;
					updateData(true);
				} else {
					if (editMode) {
						if (!adapter.getSelectSet().contains(
								Integer.valueOf(list.get(position)
										.getDownloadData().getApkId()))) {
							adapter.getSelectSet().add(
									list.get(position).getDownloadData()
											.getApkId());
							
							AuroraCheckBox cb = (AuroraCheckBox) view
									.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
							cb.auroraSetChecked(true, true);
						} else {
							adapter.getSelectSet().remove(
									list.get(position).getDownloadData()
											.getApkId());
							
							AuroraCheckBox cb = (AuroraCheckBox) view
									.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
							cb.auroraSetChecked(false, true);
						}
						checkSelect();
					}
				}
			}
		});

		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (hasShowExpansion && position == list.size() - 1) {
					return false;
				}

				AuroraCheckBox cb = (AuroraCheckBox) view
						.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
				cb.setChecked(true);
				cb.postInvalidate();

				if (!editMode) {
					adapter.getSelectSet().add(
							list.get(position).getDownloadData().getApkId());
					
					alowUpdate = false;
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							alowUpdate = true;
						}
					}, openOrCloseEditModeDelay);
					
					openEditMode();
				}

				return false;
			}
		});
	}

	private void initdata() {
		list = new ArrayList<DownloadManagerBean>();
		downloadingList = new ArrayList<DownloadManagerBean>();
		downloadedList = new ArrayList<DownloadManagerBean>();
		adapter = new DownloadManagerAdapter(this, list);
		mListView.setAdapter(adapter);
		updateData(true);


	}

	private DownloadUpdateListener updateListener = new DownloadUpdateListener() {
		@Override
		public void downloadProgressUpdate() {
			if (alowUpdate) {
				updateData(true);
				lastRefresh = System.currentTimeMillis();
			}
		}
	};

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			MessageObject obj = (MessageObject) msg.obj;
			downloadingList.clear();
			downloadingList.addAll(obj.tempDownloadingList);
			
			downloadedList.clear();
			downloadedList.addAll(obj.tempDownloadedList);
			
			list.clear();
			list.addAll(obj.tempList);
			
			if (list.size() == 0) {
				ll_nodata.setVisibility(View.VISIBLE);
			} else {
				ll_nodata.setVisibility(View.GONE);
			}
	
			adapter.setDownloadingCount(downloadingList.size());
			adapter.setDownloadedCount(downloadedList.size());
			adapter.updateSectionIndice();
			if (obj.notify) {
				adapter.notifyDataSetChanged();
			} else {
				adapter.updateListData(mListView, list);
			}
			if ((isOpenInstall != 0) && (downloadedList.size() > 0)
					&& (downloadingList.size() > 0)) {
				isOpenInstall = 0;
				int size = downloadingList.size();
				mListView.setSelection(size);

			}
			
			if(mInitListPos > 0){
			    mListView.setSelection(mInitListPos);
			}
			
		}
	};

	private void updateData(final boolean notify) {
		new Thread() {
			@Override
			public void run() {
				
				long start = System.currentTimeMillis();
				
				List<DownloadManagerBean> tempList = new ArrayList<DownloadManagerBean>();

				// 获取下载管理列表
				List<DownloadManagerBean> tempDownloadingList = new ArrayList<DownloadManagerBean>();
				Map<Integer, FileDownloader> map = AppDownloadService.getDownloaders();
				if (map == null) {
					return;
				}
				List<FileDownloader> downloaders = new ArrayList<FileDownloader>();
				for (int key : map.keySet()) {
					downloaders.add(map.get(key));
				}

				for (FileDownloader downloader : downloaders) {
					if (!(downloader.getStatus() >= FileDownloader.STATUS_INSTALL_WAIT)) {
						DownloadManagerBean bean = new DownloadManagerBean();
						bean.setType(DownloadManagerBean.TYPE_DOWNLOADING);
						bean.setDownloadData(downloader.getDownloadData());
						long fileSize = downloader.getFileSize();
						long downloadSize = downloader.getDownloadSize();
						int progress = (int) ((float) (downloadSize * 1.0)
								/ (fileSize * 1.0) * 100);
						bean.setFileSize(fileSize);
						bean.setDownloadSize(downloadSize);
						bean.setProgress(progress);
						bean.setDownloadStatus(downloader.getStatus());
						bean.setCreateTime(downloader.getCreateTime());
						tempDownloadingList.add(bean);
					}
				}

				sortDownloadingList(tempDownloadingList);
//				downloadingList.clear();
//				downloadingList.addAll(tempDownloadingList);

				// 获取已完成列表
				List<DownloadManagerBean> tempDownloadedList = new ArrayList<DownloadManagerBean>();
				AppDownloadDao downloadDao = AppDownloadService.getAppDownloadDao();
				List<DownloadData> tempDownloaded = downloadDao.getDownloadedApp();
				for (DownloadData data : tempDownloaded) {
					String filePath = data.getFileDir() + File.separator
							+ data.getFileName();
					DownloadManagerBean bean = new DownloadManagerBean();
					bean.setType(DownloadManagerBean.TYPE_DOWNLOADED);
					bean.setDownloadData(data);
					bean.setFilePath(filePath);
					bean.setDownloadStatus(data.getStatus());

					tempDownloadedList.add(bean);
				}
				
				sortDownloadedList(tempDownloadedList);
//				downloadedList.clear();
//				downloadedList.addAll(tempDownloadedList);

//				list.clear();
//				list.addAll(downloadingList);
				tempList.addAll(tempDownloadingList);

				if (needShowMore) {
					if (tempDownloadedList.size() > showMoreCount) {
						hasShowExpansion = true;
						for (int i = 0; i < showMoreCount; i++) {
							tempList.add(tempDownloadedList.get(i));
						}
						DownloadManagerBean bean = new DownloadManagerBean();
						bean.setType(DownloadManagerBean.TYPE_DOWNLOADED);
						tempList.add(bean);
					} else {
						tempList.addAll(tempDownloadedList);
						needShowMore = false;
					}
				} else {
					tempList.addAll(tempDownloadedList);
				}
				
			    
				mInitListPos = locateFailIntalledApp(mFailPackageName, tempList);
				
				MessageObject obj = new MessageObject(tempList, tempDownloadingList, tempDownloadedList, notify);
				handler.sendMessage(handler.obtainMessage(100, obj));

//				System.out.println("time: " + (System.currentTimeMillis() - start));
				
			}
		}.start();
		
		// Log.i(TAG, "downloadingList size: " + downloadingList.size());
		// Log.i(TAG, "downloadedList size: " + downloadedList.size());
	}
	
	private int locateFailIntalledApp(String pPackageName, List<DownloadManagerBean> pList){
	    
		if (pPackageName != null && pList != null) {
			for (int i = 0; i < pList.size(); i++) {
				if (pList.get(i).getDownloadData() != null) {
					if (pPackageName.equals(pList.get(i).getDownloadData()
							.getPackageName())) {
						return i;
					}
				}
			}
	    }
	    
	    return -1;
	}
	
	public static final class MessageObject {
		protected List<DownloadManagerBean> tempList;
		protected List<DownloadManagerBean> tempDownloadingList;
		protected List<DownloadManagerBean> tempDownloadedList;
		protected boolean notify;
		
		public MessageObject(List<DownloadManagerBean> tempList, 
				List<DownloadManagerBean> tempDownloadingList,
				List<DownloadManagerBean> tempDownloadedList,
				boolean notify) {
			this.tempList = tempList;
			this.tempDownloadingList = tempDownloadingList;
			this.tempDownloadedList = tempDownloadedList;
			this.notify = notify;
		}
	}

	private void sortDownloadingList(List<DownloadManagerBean> downloadingList) {
		Collections.sort(downloadingList,
				new Comparator<DownloadManagerBean>() {
					@Override
					public int compare(DownloadManagerBean bean1,
							DownloadManagerBean bean2) {
						if (bean1.getCreateTime() < bean2.getCreateTime()) {
							return -1;
						} else if (bean1.getCreateTime() > bean2
								.getCreateTime()) {
							return 1;
						}
						return 0;
					}
				});
	}

	private void sortDownloadedList(List<DownloadManagerBean> downloadedList) {
		Collections.sort(downloadedList, new Comparator<DownloadManagerBean>() {
			@Override
			public int compare(DownloadManagerBean bean1,
					DownloadManagerBean bean2) {
				if (bean1.getDownloadData().getFinishTime() > bean2
						.getDownloadData().getFinishTime()) {
					return -1;
				} else if (bean1.getDownloadData().getFinishTime() < bean2
						.getDownloadData().getFinishTime()) {
					return 1;
				}
				return 0;
			}
		});
	}

	/**
	 * @Title: openEditMode
	 * @Description: 打开编辑模式
	 * @param
	 * @return void
	 * @throws
	 */
	private void openEditMode() {
		if (hasShowExpansion && needShowMore) {
			hasShowExpansion = false;
			needShowMore = false;
			updateData(true);
		}

		editMode = true;

		mListView.auroraSetNeedSlideDelete(false);
		mListView.setLongClickable(false);
		mListView.auroraEnableSelector(false);

		mActionBar.setShowBottomBarMenu(true);
		mActionBar.showActionBarDashBoard();

		leftView = (TextView) mActionBar.getSelectLeftButton();
		rightView = (TextView) mActionBar.getSelectRightButton();
		leftView.setTextColor(getResources().getColor(R.color.white));
		rightView.setTextColor(getResources().getColor(R.color.white));

		rightView.setText(getString(R.string.downloadman_select_all));
		leftView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (editMode) {
					
					alowUpdate = false;
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							alowUpdate = true;
						}
					}, openOrCloseEditModeDelay);
					
					closeEditMode();
				}
			}
		});

		rightView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isSelectAll) {
					adapter.getSelectSet().clear();
					adapter.notifyDataSetChanged();
				} else {
					for (DownloadManagerBean bean : list) {
						if (bean.getDownloadData() != null) {
							adapter.getSelectSet().add(
									Integer.valueOf(bean.getDownloadData()
											.getApkId()));
						}
					}
					adapter.notifyDataSetChanged();
				}
				checkSelect();
			}
		});

		adapter.setEditMode(true);
		adapter.setNeedAnim(true);
		adapter.notifyDataSetChanged();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				adapter.setNeedAnim(false);
			}
		}, 1);

		checkSelect();
	}

	/**
	 * @Title: closeEditMode
	 * @Description: 关闭编辑模式
	 * @param
	 * @return void
	 * @throws
	 */
	private void closeEditMode() {
		editMode = false;
		mListView.auroraSetNeedSlideDelete(true);
		mListView.setLongClickable(true);
		mListView.auroraEnableSelector(true);
		mListView.setSelector(R.drawable.list_item_selector);

		mActionBar.setShowBottomBarMenu(false);
		mActionBar.showActionBarDashBoard();

		for (int i = 0; i < mListView.getChildCount(); i++) {
			AuroraCheckBox cb = (AuroraCheckBox) mListView.getChildAt(i)
					.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
			cb.setChecked(false);
			cb.postInvalidate();
		}
		adapter.getSelectSet().clear();

		adapter.setEditMode(false);
		adapter.setNeedAnim(true);
		adapter.notifyDataSetChanged();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				adapter.setNeedAnim(false);
			}
		}, 1);
	}

	/**
	 * @Title: checkSelect
	 * @Description: 检查是否全选状态
	 * @param
	 * @return void
	 * @throws
	 */
	private void checkSelect() {
		int allCount = list.size();
		if (needShowMore) {
			allCount = allCount - 1;
		}
		if (adapter.getSelectSet().size() == allCount) {
			isSelectAll = true;
		} else {
			isSelectAll = false;
		}
		if (isSelectAll) {
			rightView
					.setText(getString(R.string.downloadman_reverse_selection));
		} else {
			rightView.setText(getString(R.string.downloadman_select_all));
		}
		if (adapter.getSelectSet().size() == 0) {
			mActionBar.getAuroraActionBottomBarMenu().setBottomMenuItemEnable(
					1, false);
		} else {
			mActionBar.getAuroraActionBottomBarMenu().setBottomMenuItemEnable(
					1, true);
		}
	}

	private OnDismissListener dismissListener = new OnDismissListener() {
		@Override
		public void onDismiss(DialogInterface arg0) {
			showDialog = false;
		}
	};

	/**
	 * @Title: showDeleteDialog
	 * @Description: 显示删除单个任务对话框
	 * @param @param bean
	 * @return void
	 * @throws
	 */
	private void showDeleteDialog(final DownloadManagerBean bean) {
		if (showDialog) {
			return;
		}
		showDialog = true;
		if (bean.getDownloadData() == null) {
			return;
		}
		AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this);
		builder.setTitle(getString(R.string.downloadman_dlg_delete_confirm));
		checkbox.setChecked(true);
		if (bean.getDownloadStatus() >= FileDownloader.STATUS_INSTALL_WAIT) {
			tv_message.setText(getString(R.string.downloadman_delete_the_record));
			File file = new File(bean.getFilePath());
			if (file.exists()) {
				checkbox.setVisibility(View.VISIBLE);
			} else {
				checkbox.setVisibility(View.GONE);
			}
		} else {
			tv_message.setText(getString(R.string.downloadman_delete_the_task));
			checkbox.setVisibility(View.GONE);
		}

		builder.setView(alertDialogView);
		builder.setPositiveButton(getString(R.string.dialog_confirm),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						mListView.auroraDeleteSelectedItemAnim();
					}
				});
		builder.setNegativeButton(getString(R.string.dialog_cancel), null);
		builder.setOnDismissListener(dismissListener);
		builder.create().show();
	}

	/**
	 * @Title: showSelectDeleteDialog
	 * @Description: 显示删除已选择对话框
	 * @param
	 * @return void
	 * @throws
	 */
	private void showSelectDeleteDialog() {
		if (showDialog) {
			return;
		}
		showDialog = true;
		if (adapter.getSelectSet().size() == 0) {
			Toast.makeText(this, getString(R.string.downloadman_no_select),
					Toast.LENGTH_SHORT).show();
		} else {
			boolean hasFinishAndExist = false;
			for (int i : adapter.getSelectSet()) {
				for (DownloadManagerBean bean : list) {
					if (bean.getDownloadData() != null
							&& (bean.getDownloadData().getApkId() == i)) {
						// 已完成
						if (bean.getDownloadStatus() >= FileDownloader.STATUS_INSTALL_WAIT) {
							File file = new File(bean.getFilePath());
							if (file.exists()) {
								hasFinishAndExist = true;
								break;
							}
						}
					}
				}
			}

			AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(
					this);
			builder.setTitle(getString(R.string.downloadman_delete_select_task_and_record));
		//	builder.setMessage(R.string.downloadman_delete_select_task_and_record);
			tv_message.setText(getString(R.string.downloadman_delete_select_task_and_record));
			checkbox.setChecked(true);
			if (hasFinishAndExist) {
				checkbox.setVisibility(View.VISIBLE);
			} else {
				checkbox.setVisibility(View.GONE);
			}

		//	builder.setView(alertDialogView);
			builder.setPositiveButton(getString(R.string.downloadman_delete),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							menuDelete();
						}
					});
			builder.setNegativeButton(getString(R.string.dialog_cancel), null);
			builder.setOnDismissListener(dismissListener);
			builder.create().show();
		}
	}

	/**
	 * @Title: showDeleteAllFinishDialog
	 * @Description: 显示删除所有已下载对话框
	 * @param
	 * @return void
	 * @throws
	 */
	public void showDeleteAllFinishDialog() {
		if (showDialog) {
			return;
		}
		showDialog = true;
		AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this);
		builder.setTitle(getString(R.string.downloadman_clear_record));
	//	builder.setMessage(R.string.downloadman_clear_record);
		tv_message.setText(getString(R.string.downloadman_clear_record));
		checkbox.setChecked(true);
		
		boolean existFile = false;
		for (DownloadManagerBean bean : downloadedList) {
			if (bean.getType() == DownloadManagerBean.TYPE_DOWNLOADED) {
				File file = new File(bean.getFilePath());
				if (file.exists()) {
					existFile = true;
					break;
				}
			}
		}
		if (existFile) {
			checkbox.setVisibility(View.VISIBLE);
		} else {
			checkbox.setVisibility(View.GONE);
		}

	//	builder.setView(alertDialogView);
		builder.setPositiveButton(getString(R.string.downloadman_clear),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						DownloadManagerBean bean = null;
						AppDownloadDao dao = AppDownloadService
								.getAppDownloadDao();
						for (int i = 0; i < downloadedList.size(); i++) {
							bean = downloadedList.get(i);
							if (bean.getType() == DownloadManagerBean.TYPE_DOWNLOADED) {
								if (checkbox.isChecked() && checkbox.getVisibility() == View.VISIBLE) {
									File file = new File(bean.getFilePath());
									file.delete();
								}
								dao.delete(bean.getDownloadData().getApkId());
							}
						}
						updateData(true);
					}
				});
		builder.setNegativeButton(getString(R.string.dialog_cancel), null);
		builder.setOnDismissListener(dismissListener);
		builder.create().show();
	}

	/**
	 * @Title: menuDelete
	 * @Description: 选择后删除操作
	 * @param
	 * @return void
	 * @throws
	 */
	public void menuDelete() {
		alowUpdate = false;
		for (int i : adapter.getSelectSet()) {
			for (DownloadManagerBean bean : list) {
				if (bean.getDownloadData() != null
						&& (bean.getDownloadData().getApkId() == i)) {
					// 已完成
					if (bean.getDownloadStatus() >= FileDownloader.STATUS_INSTALL_WAIT) {
						if (checkbox.isChecked()) {
							File file = new File(bean.getFilePath());
							file.delete();
						}
						AppDownloadService.getAppDownloadDao().delete(
								bean.getDownloadData().getApkId());
					} else {
						AppDownloadService.cancelDownload(this,
								bean.getDownloadData());
					}
					break;
				}
			}
		}
		
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				alowUpdate = true;
				AppDownloadService.updateDownloadProgress();
			}
		}, openOrCloseEditModeDelay);

		closeEditMode();
		
		Intent broadcase = new Intent(Globals.BROADCAST_ACTION_DOWNLOAD);
		sendBroadcast(broadcase);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mActionBar.auroraIsExitEditModeAnimRunning()
					|| mActionBar.auroraIsEntryEditModeAnimRunning()) {
				
				return true;
			}
			if (editMode) {
				alowUpdate = false;
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						alowUpdate = true;
					}
				}, openOrCloseEditModeDelay);
				
				closeEditMode();
				
				return true;
			}
			Intent intent = new Intent(DownloadManagerActivity.this,
					MarketManagerPreferenceActivity.class);
			
			Intent intent1 = new Intent(DownloadManagerActivity.this,
					MarketMainActivity.class);
			ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
			List<RunningTaskInfo> appTask = am.getRunningTasks(1);
			boolean ifFront = false;
			for(RunningTaskInfo app:appTask)
			{
				if(app.baseActivity.equals(intent1.getComponent()))
				{
					ifFront = true;
					break;
				}
			}
			
			if (ifFront) {
				
				//startActivity(intent);
				finish();
				Log.i(TAG, "setmOnActionBarBackItemListener back1");
				return true;
				//overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
				
			} else {
				
				finish();
				startActivity(intent);
				overridePendingTransition(com.aurora.R.anim.aurora_activity_close_enter,com.aurora.R.anim.aurora_activity_close_exit);
				Log.i(TAG, "setmOnActionBarBackItemListener back2");
				return true;
				
			}
			
			
		}

		return super.onKeyDown(keyCode, event);
	}

}
