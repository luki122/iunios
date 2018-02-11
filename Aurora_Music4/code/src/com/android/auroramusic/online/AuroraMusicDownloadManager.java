package com.android.auroramusic.online;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.android.auroramusic.adapter.AuroraMusicDownloadAdapter;
import com.android.auroramusic.downloadex.DownloadInfo;
import com.android.auroramusic.downloadex.DownloadManager;
import com.android.auroramusic.ui.AuroraPlayerActivity;
import com.android.auroramusic.ui.BasicActivity;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.Globals;
import com.android.auroramusic.util.LogUtil;
import com.android.auroramusic.util.ThreadPoolExecutorUtils;
import com.android.music.MediaPlaybackService;
import com.android.music.MusicUtils;
import com.android.music.R;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.renderscript.Element;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraMenuBase;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraListView.AuroraBackOnClickListener;
import aurora.widget.AuroraListView.AuroraDeleteItemListener;

public class AuroraMusicDownloadManager extends BasicActivity implements
		OnClickListener {

	private static final String TAG = "AuroraMusicDownloadManager";
	private static final int PLAY_BUTTON = 0;
	private boolean isPlaying = false; // 动画是否在运行
	private View playView; // 播放按钮
	private Animation operatingAnim; // 播放按钮动画
	private AuroraListView mListView;
	private View mHeadView;
	private TextView mSongSize;
	private Button button;
	private AuroraMusicDownloadAdapter mAuroraMusicDownloadAdapter;
	private Handler mHandler = new Handler();
	private final int AURORA_ID_REMOVE_DOWNLOADS = 101;
	private AuroraActionBar mAuroraActionBar;
	private boolean isEditeMode = false;
	private TextView selectLeftBtn, selectRightBtn, noDownloadTask;
	private ColorStateList oldColer;
	private boolean isOntouch = false;
	private AuroraAlertDialog mAuroraAlertDialog;
	private DownloadInfo deleteInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.aurora_songsingle_main,
				AuroraActionBar.Type.Normal);
		try {
			initActionbar();
			initView();
		} catch (Exception e) {
			e.printStackTrace();
		}
		new ScanDownloadTask().executeOnExecutor(ThreadPoolExecutorUtils.getThreadPoolExecutor().getExecutor());
	}

	private void initView() {
		mListView = (AuroraListView) findViewById(R.id.aurora_id_song_list);
		// 给listview 添加head
		mHeadView = LayoutInflater.from(this).inflate(
				R.layout.downloadmanagerhead, null);
		mSongSize = (TextView) mHeadView.findViewById(R.id.id_song_size);
		button = (Button) mHeadView.findViewById(R.id.id_all_pause);
		noDownloadTask = (TextView) findViewById(R.id.aurora_no_download_task);

		button.setOnClickListener(this);
		mListView.addHeaderView(mHeadView);
		mAuroraMusicDownloadAdapter = new AuroraMusicDownloadAdapter(this,
				mListView);
		mListView.setAdapter(mAuroraMusicDownloadAdapter);
		mListView.setSelector(R.drawable.aurora_playlist_item_clicked);
		mListView.auroraSetNeedSlideDelete(true);
		mListView
				.auroraSetAuroraBackOnClickListener(mAuroraBackOnClickListener);
		mListView.auroraSetDeleteItemListener(mAuroraDeleteItemListener);
		mListView.setOnItemLongClickListener(mOnLongClickListener);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				DownloadInfo info = (DownloadInfo) arg0.getAdapter().getItem(
						arg2);
				boolean is = mAuroraMusicDownloadAdapter
						.onItemclick(info, arg2);
				if (!is) {
					button.setText(R.string.aurora_all_download);
				} else {
					button.setText(R.string.aurora_all_pause);
				}
			}
		});
		mListView.setVisibility(View.GONE);
		noDownloadTask.setVisibility(View.VISIBLE);
		mListView.setOnTouchListener(mOnTouchListener);
		mListView.setOnScrollListener(mOnScrollListener);
	}

	private void initActionbar() {
		setAuroraMenuCallBack(auroraMenuCallBack);
		mAuroraActionBar = getAuroraActionBar();// 获取actionbar
		mAuroraActionBar.setTitle(R.string.aurora_download_manager);
		// 旋转动画方式
		mAuroraActionBar.addItem(R.drawable.song_playing, PLAY_BUTTON, null);
		playView = mAuroraActionBar.getItem(PLAY_BUTTON).getItemView();
		operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_anim);

		LinearInterpolator lin = new LinearInterpolator();
		operatingAnim.setInterpolator(lin);
		mAuroraActionBar
				.setOnAuroraActionBarListener(mOnAuroraActionBarItemClickListener);

		mAuroraActionBar.initActionBottomBarMenu(
				R.menu.menu_songsingle_bottombar, 1);
		initActionbarMenu();
	}

	private OnTouchListener mOnTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {

			switch (arg1.getAction()) {

			case MotionEvent.ACTION_DOWN:

				mAuroraMusicDownloadAdapter.setLeftDelete(true, true);
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:

				mAuroraMusicDownloadAdapter.setLeftDelete(isOntouch, true);
				break;
			}
			return false;
		}

	};

	private void initActionbarMenu() {

		selectLeftBtn = (TextView) mAuroraActionBar.getSelectLeftButton();
		selectRightBtn = (TextView) mAuroraActionBar.getSelectRightButton();
		oldColer = selectRightBtn.getTextColors();

		if (selectLeftBtn != null) {
			selectLeftBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// 退出编辑模式
					exitEditMode();
				}
			});
		}

		if (selectRightBtn != null) {
			selectRightBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					LogUtil.d(TAG,
							"selectRightBtn.getText():"
									+ selectRightBtn.getText());
					if (selectRightBtn.getText().equals(
							getResources().getString(R.string.selectAll))) {
						selectRightBtn
								.setText(getString(R.string.selectAllNot));
						mAuroraMusicDownloadAdapter.selectAll();
					} else if (selectRightBtn.getText().equals(
							getString(R.string.selectAllNot))) {
						selectRightBtn.setText(getString(R.string.selectAll));
						mAuroraMusicDownloadAdapter.selectAllNot();
					}
					changeMenuState();
				}
			});
		}
	}

	private AuroraMenuBase.OnAuroraMenuItemClickListener auroraMenuCallBack = new AuroraMenuBase.OnAuroraMenuItemClickListener() {

		@Override
		public void auroraMenuItemClick(int arg0) {

			switch (arg0) {
			case R.id.song_backup:
				showDeleteDialog();
				break;
			}
		}

	};

	private AuroraDeleteItemListener mAuroraDeleteItemListener = new AuroraDeleteItemListener() {

		@Override
		public void auroraDeleteItem(View arg0, final int arg1) {

			// mAuroraMusicDownloadAdapter.setLeftDelete(false);
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					mAuroraMusicDownloadAdapter.deleteItem(deleteInfo);
					notifySongsizeChange();
				}
			});
			isOntouch = false;
		}

	};

	private OnItemLongClickListener mOnLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			// 进入编辑模式
			entryEditMode(arg2 - 1);
			if (!mAuroraActionBar.auroraIsEntryEditModeAnimRunning()) {
				mAuroraActionBar.setShowBottomBarMenu(true);
				mAuroraActionBar.showActionBarDashBoard();
				changeMenuState();
			}
			return false;
		}

	};

	public boolean isEditAnimRunning() {

		return mAuroraActionBar.auroraIsEntryEditModeAnimRunning()
				|| mAuroraActionBar.auroraIsExitEditModeAnimRunning();
	}

	private void entryEditMode(int postion) {
		if (isEditeMode) {
			return;
		}
		isEditeMode = true;
		// mAuroraMusicDownloadAdapter.setLeftDelete(true);
		setEditeMode(true, postion);
	}

	private void exitEditMode() {
		if (!isEditeMode) {
			return;
		}
		isEditeMode = false;
		if (!mAuroraActionBar.auroraIsExitEditModeAnimRunning()) {
			mAuroraActionBar.setShowBottomBarMenu(false);
			mAuroraActionBar.showActionBarDashBoard();
		}
		// mAuroraMusicDownloadAdapter.setLeftDelete(false);
		setEditeMode(false, -1);
		selectRightBtn.setText(getResources().getString(R.string.selectAll));
	}

	private void setEditeMode(boolean is, int position) {
		if (is) {
			if (position == -1) {
				mAuroraMusicDownloadAdapter.setNeedIn();
			} else {
				mAuroraMusicDownloadAdapter.setNeedIn(position);
			}
			if (isPlaying) {

				playView.clearAnimation();
				playView.setBackgroundResource(R.drawable.aurora_left_bar_clicked);
				isPlaying = false;
			}
			mListView.auroraSetNeedSlideDelete(false);
			mListView.setSelector(android.R.color.transparent);
			button.setEnabled(false);
		} else {
			mListView.setSelector(R.drawable.aurora_playlist_item_clicked);
			mAuroraMusicDownloadAdapter.setNeedOut();
			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					setPlayAnimation();
				}
			}, 500);
			mListView.auroraSetNeedSlideDelete(true);
			button.setEnabled(true);
		}

	}

	public void changeMenuState() {
		if (mAuroraMusicDownloadAdapter.getCheckedCount() == mAuroraMusicDownloadAdapter
				.getCount()) {
			if (mAuroraMusicDownloadAdapter.getCount() == 0) {
				selectRightBtn.setText(getResources().getString(
						R.string.selectAll));
				selectRightBtn.setEnabled(false);
				selectRightBtn.setTextColor(getResources().getColor(
						R.color.aurora_select_disable));
			} else {
				((TextView) mAuroraActionBar.getSelectRightButton())
						.setText(getResources()
								.getString(R.string.selectAllNot));
			}
		} else {
			selectRightBtn
					.setText(getResources().getString(R.string.selectAll));
			selectRightBtn.setEnabled(true);
			selectRightBtn.setTextColor(oldColer);
		}

		if (mAuroraMusicDownloadAdapter.getCheckedCount() == 0) {

			mAuroraActionBar.getAuroraActionBottomBarMenu()
					.setBottomMenuItemEnable(1, false);
		} else {
			mAuroraActionBar.getAuroraActionBottomBarMenu()
					.setBottomMenuItemEnable(1, true);
		}
	}

	private AuroraBackOnClickListener mAuroraBackOnClickListener = new AuroraBackOnClickListener() {

		@Override
		public void auroraDragedSuccess(int arg0) {
			LogUtil.d(TAG, "auroraDragedSuccess");
			mAuroraMusicDownloadAdapter.setLeftDelete(true, false);
		}

		@Override
		public void auroraDragedUnSuccess(int arg0) {
			LogUtil.d(TAG, "auroraDragedUnSuccess");
			isOntouch = false;
			mAuroraMusicDownloadAdapter.setLeftDelete(false, false);
		}

		@Override
		public void auroraOnClick(int arg0) {
			LogUtil.d(TAG, "auroraOnClick:" + arg0);
			deleteInfo = (DownloadInfo) mAuroraMusicDownloadAdapter
					.getItem(arg0 - 1);
			LogUtil.d(TAG, "auroraOnClick:" + deleteInfo);
			showDialog(AURORA_ID_REMOVE_DOWNLOADS);
		}

		@Override
		public void auroraPrepareDraged(int arg0) {
			isOntouch = true;
			mAuroraMusicDownloadAdapter.setLeftDelete(true, false);
			LogUtil.d(TAG, "auroraPrepareDraged:" + arg0);
		}

	};

	private OnAuroraActionBarItemClickListener mOnAuroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		@Override
		public void onAuroraActionBarItemClicked(int itemId) {
			switch (itemId) {
			case PLAY_BUTTON:

				Intent intent = new Intent();
				intent.setClass(AuroraMusicDownloadManager.this,
						AuroraPlayerActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_right_in,
						R.anim.slide_left_out);
				break;
			}
		}

	};

	@Override
	protected void onPause() {
		mListView.auroraOnPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mListView.auroraOnResume();
	}

	@Override
	protected void onStart() {
		super.onStart();
		setPlayAnimation();
		IntentFilter f = new IntentFilter();
		f.addAction(MediaPlaybackService.PLAYSTATE_CHANGED);
		f.addAction(MediaPlaybackService.META_CHANGED);
		registerReceiver(mStatusListener, new IntentFilter(f));
	}

	@Override
	protected void onStop() {
		if (isPlaying) {
			playView.clearAnimation();
			playView.setBackgroundResource(R.drawable.aurora_left_bar_clicked);
			isPlaying = false;
		}
		unregisterReceiver(mStatusListener);
		super.onStop();
	}

	/**
	 * 设置播放动画
	 */
	private void setPlayAnimation() {
		try {
			if (MusicUtils.sService != null) {
				LogUtil.d(TAG, "isplaying:" + MusicUtils.sService.isPlaying());
				if (MusicUtils.sService.isPlaying()) {
					if (!isPlaying && !isEditeMode) {
						playView.startAnimation(operatingAnim);
						playView.setBackgroundResource(android.R.color.transparent);
						isPlaying = true;
					}
				} else if (isPlaying) {
					playView.clearAnimation();
					playView.setBackgroundResource(R.drawable.aurora_left_bar_clicked);
					isPlaying = false;
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			if (isPlaying) {
				playView.clearAnimation();
				playView.setBackgroundResource(R.drawable.aurora_left_bar_clicked);
				isPlaying = false;
			}
		}
	}

	private BroadcastReceiver mStatusListener = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(MediaPlaybackService.PLAYSTATE_CHANGED)) {
				LogUtil.d(TAG, "--------mStatusListener:");

				setPlayAnimation();

			}
		}
	};

	class ScanDownloadTask extends AsyncTask<Void, Void, List<DownloadInfo>> {

		private DownloadManager mDownloadManager;

		@Override
		protected List<DownloadInfo> doInBackground(Void... arg0) {

			HashMap<Long, DownloadInfo> list = mDownloadManager
					.getDownloadingMapData();
			List<DownloadInfo> datas = new ArrayList<DownloadInfo>();
			Iterator<DownloadInfo> iterator = list.values().iterator();
			while (iterator.hasNext()) {
				DownloadInfo info = iterator.next();
				if (!info.isDownloadOver()) {
					datas.add(info);
				}
			}
			return datas;
		}

		@Override
		protected void onPostExecute(List<DownloadInfo> result) {
			if (result == null) {
				return;
			}
			mAuroraMusicDownloadAdapter.setDatas(result);
			notifySongsizeChange();
		}

		@Override
		protected void onPreExecute() {
			mDownloadManager = DownloadManager
					.getInstance(getApplicationContext());
		}

	}

	@Override
	public void onClick(View arg0) {

		switch (arg0.getId()) {
		case R.id.id_all_pause:
			if (!AuroraMusicUtil.isNetWorkActive(this)) {
				Toast.makeText(this, R.string.aurora_network_error,
						Toast.LENGTH_SHORT).show();
				return ;
			}
			boolean is = mAuroraMusicDownloadAdapter.isDownloading();
			if (is) {
				button.setText(R.string.aurora_all_download);
			} else {
				button.setText(R.string.aurora_all_pause);
			}
			mAuroraMusicDownloadAdapter.pauseOrDownloadAll(is);
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle bundle) {
		Dialog dilog;

		switch (id) {
		case AURORA_ID_REMOVE_DOWNLOADS:
			String title1 = getString(R.string.remove_download);
			String message1 = getString(R.string.remove_download_message);

			AuroraAlertDialog.Builder build = new AuroraAlertDialog.Builder(
					this)
					.setTitle(title1)
//					.setIcon(android.R.drawable.ic_dialog_info)
					.setMessage(message1)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									if (mAuroraMusicDownloadAdapter
											.canDelete(deleteInfo)) {
										mListView
												.auroraDeleteSelectedItemAnim();
									}
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {

									mListView.auroraSetRubbishBack();
								}
							});
			dilog = build.create();
			break;
		default:
			dilog = super.onCreateDialog(id);
			break;
		}
		return dilog;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		// TODO Auto-generated method stub
		super.onPrepareDialog(id, dialog, args);
	}

	@Override
	public void onBackPressed() {

		LogUtil.d(TAG, "onBackPressed():" + isEditeMode);
		if (isEditeMode) {
			exitEditMode();
		} else if (mListView.auroraIsRubbishOut()) {
			mListView.auroraSetRubbishBack();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& event.getKeyCode() == KeyEvent.KEYCODE_BACK && (isEditeMode)) {
			if (mAuroraActionBar.auroraIsEntryEditModeAnimRunning()
					|| mAuroraActionBar.auroraIsExitEditModeAnimRunning()) {
				return true;
			}
			exitEditMode();
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	public void notifySongsizeChange(boolean is) {
		if (!is) {
			button.setText(R.string.aurora_all_download);
		} else {
			button.setText(R.string.aurora_all_pause);
		}
	}

	public void notifySongsizeChange() {
		mSongSize.setText(getString(R.string.aurora_num_songs_of_single,
				mAuroraMusicDownloadAdapter.getCount()));
		if (mAuroraMusicDownloadAdapter.getCount() == 0) {
			mListView.setVisibility(View.GONE);
			noDownloadTask.setVisibility(View.VISIBLE);

			// 如果在编辑模式退出编辑模式
			if (isEditeMode) {
				exitEditMode();
			}
		} else {
			mListView.setVisibility(View.VISIBLE);
			noDownloadTask.setVisibility(View.GONE);
		}
		boolean is = mAuroraMusicDownloadAdapter.isDownloading();
		LogUtil.d(TAG, "notifySongsizeChange:" + is);
		if (!is) {
			button.setText(R.string.aurora_all_download);
		} else {
			button.setText(R.string.aurora_all_pause);
		}
		isRubbishout();
	}

	public boolean isRubbishout() {
		if (mListView.auroraIsRubbishOut()) {
			mListView.auroraSetRubbishBack();
			return true;
		}
		return false;
	}

	@Override
	protected void onDestroy() {
		DownloadManager.getInstance(getApplicationContext()).clearListenerMap();
		super.onDestroy();
	}

	private void showDeleteDialog() {
		if (mAuroraAlertDialog == null) {
			AuroraAlertDialog.Builder build = new AuroraAlertDialog.Builder(
					this)
					.setTitle(R.string.remove_download)
					.setMessage(R.string.remove_download_message)
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {

								}
							})
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									mAuroraMusicDownloadAdapter
											.deleteSelectItem();
									exitEditMode();
									notifySongsizeChange();
								}
							});
			mAuroraAlertDialog = build.create();
		}
		mAuroraAlertDialog.show();
	}

	private OnScrollListener mOnScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView abslistview, int arg1) {
			if (arg1 == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {


			} else if (arg1 == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {

			}
		}

		@Override
		public void onScroll(AbsListView abslistview, int i, int j, int k) {

		}
	};
}
