package com.android.auroramusic.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraListView.AuroraBackOnClickListener;
import aurora.widget.AuroraListView.AuroraDeleteItemListener;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraMenuBase;

import com.android.auroramusic.adapter.AuroraFoldListAdapter;
import com.android.auroramusic.util.AuroraMainMenuData;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.Globals;
import com.android.auroramusic.util.LogUtil;
import com.android.music.MediaPlaybackService;
import com.android.music.MusicUtils;
import com.android.music.MusicUtils.ServiceToken;
import com.android.music.R;

public class AuroraFoldActivity extends AbstractBaseActivity implements OnItemClickListener, LoaderCallbacks<Cursor> {

	private static final String TAG = "AuroraFoldActivity";
	private AuroraActionBar mAuroraActionBar;
	private static final int PLAY_BUTTON = 0;
	private Animation operatingAnim; // 播放按钮动画
	private boolean isPlaying = false; // 动画是否在运行
	private View playView; // 播放按钮
	private ServiceToken mToken;
	private TextView totalFold;
	private AuroraListView mAuroraListView;
	private AuroraFoldListAdapter mAuroraFoldListAdapter;
	private int startMode = 0; // 0为默认模式 1为添加歌单模式
	private Handler mHandler = new Handler();
	private AuroraMenu bottomMenu;
	private TextView bottomBarTitle;
	public static final String EXTR_FOLD_START_MODE = "fold_start_mode";
	public static final String EXTR_FOLD_PATH = "fold_path";
	private ArrayList<String> mAddList = null;
	private AuroraMainMenuData playlistInfo;
	private List<String> mIgnorPathList = null;
	private int totalSongs = 0;
	private int totalFolds = 0;
	private TextView noSongs;
	private Button mIgnoreButton;
	private static final int RETAIN_CODE = 1000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.aurora_fold_main);
		mToken = MusicUtils.bindToService(this);
		Intent intent = getIntent();
		if (intent != null) {
			startMode = intent.getIntExtra(EXTR_FOLD_START_MODE, 0);
			if (startMode == 1) {
				playlistInfo = (AuroraMainMenuData) intent.getSerializableExtra(AuroraSongSingle.EXTR_PLAYLIST_INFO);
				mAddList = intent.getStringArrayListExtra(AuroraNewPlayListActivity.EXTR_ADDLIST_DATA);
				if (mAddList == null) {
					finish();
				}
				AuroraMusicActivityManiger.getInstance().addActivity(this);
			}
		} else {
			finish();
		}

		initActionBar();
		initView();
	}

	/**
	 * 初始化view
	 */
	protected void initView() {
		totalFold = (TextView) findViewById(R.id.aurora_fold_total);
		mAuroraListView = (AuroraListView) findViewById(R.id.aurora_fold);
		noSongs = (TextView) findViewById(R.id.aurora_fold_no_songs);
		mIgnoreButton = (Button) findViewById(R.id.aurora_ignore_button);

		mAuroraFoldListAdapter = new AuroraFoldListAdapter(this);
		mAuroraListView.setAdapter(mAuroraFoldListAdapter);
		mAuroraListView.setOnItemClickListener(this);
		mAuroraListView.setSelector(R.drawable.aurora_playlist_item_clicked);
		if (startMode == 0) {
			mAuroraListView.auroraSetNeedSlideDelete(false);//del ignore
			mAuroraListView.auroraSetAuroraBackOnClickListener(mAuroraBackOnClickListener);
			mAuroraListView.auroraSetDeleteItemListener(mAuroraDeleteItemListener); // item删除监听
			findViewById(R.id.aurora_title_contain).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.aurora_title_contain).setVisibility(View.GONE);
		}
		totalFold.setText(AuroraMusicUtil.getTransColorText(AuroraFoldActivity.this, totalFolds, totalSongs));
		getLoaderManager().initLoader(0, null, this);
		mIgnoreButton.setOnClickListener(mOnClickListener);
	}

	/**
	 * 初始化actionbar
	 */
	protected void initActionBar() {
		try {
			mAuroraActionBar = getAuroraActionBar();
			mAuroraActionBar.setTitle(R.string.folder);
			if (startMode == 1) {// 添加歌曲到歌单
				setAuroraBottomBarMenuCallBack(auroraMenuCallBack);
				mAuroraActionBar.initActionBottomBarMenu(R.menu.menu_add_playlist_song_bottombar, 1);
				bottomMenu = mAuroraActionBar.getAuroraActionBottomBarMenu();
				bottomBarTitle = bottomMenu.getTitleViewByPosition(0);
				bottomBarTitle.setText(getString(R.string.aurora_add_playlist_song_num, mAddList.size()));
				if (mAddList.size() > 0) {
					bottomMenu.setBottomMenuItemEnable(0, true);
				} else {
					bottomMenu.setBottomMenuItemEnable(0, false);
				}
				mAuroraActionBar.setmOnActionBarBackItemListener(mOnActionBarBackItemListener);
			} else {
				mAuroraActionBar.addItem(R.drawable.song_playing, PLAY_BUTTON, null);
				playView = mAuroraActionBar.getItem(PLAY_BUTTON).getItemView();
				operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_anim);
				LinearInterpolator lin = new LinearInterpolator();
				operatingAnim.setInterpolator(lin);
				mAuroraActionBar.setOnAuroraActionBarListener(mOnAuroraActionBarItemClickListener);
			}
		} catch (Exception e) {
			LogUtil.e(TAG, "initActionBar error", e);
		}

	}

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Intent intent = new Intent(AuroraFoldActivity.this, AuroraIgnoreFoldActivity.class);
			startActivityForResult(intent, RETAIN_CODE);
		}
	};

	/**
	 * item 删除监听
	 */
	private AuroraDeleteItemListener mAuroraDeleteItemListener = new AuroraDeleteItemListener() {

		@Override
		public void auroraDeleteItem(View arg0, int arg1) {
			String path = (String) mAuroraFoldListAdapter.getItem(arg1);
			LogUtil.d(TAG, "path:" + path);
			if (mIgnorPathList != null && !mIgnorPathList.contains(path)) {
				mIgnorPathList.add(path);
			}
			AuroraMusicUtil.writeToXml(AuroraFoldActivity.this, AuroraMusicUtil.writeToString(mIgnorPathList), "paths.xml");
			mAuroraFoldListAdapter.deleteItem(arg1);
			AuroraMusicUtil.getMountedStorage(AuroraFoldActivity.this);
			getLoaderManager().restartLoader(0, null, AuroraFoldActivity.this);
			Toast.makeText(AuroraFoldActivity.this, R.string.aurora_not_scan_the_fold, Toast.LENGTH_SHORT).show();
		}

	};

	private AuroraBackOnClickListener mAuroraBackOnClickListener = new AuroraBackOnClickListener() {
		// 点击了垃圾桶的响应事件
		@Override
		public void auroraOnClick(int position) {
			AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(AuroraFoldActivity.this).setTitle(R.string.aurora_ignor_scan).setMessage(R.string.aurora_ignoer_scan_message)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							mAuroraFoldListAdapter.hasDeleted = true;
							mAuroraListView.auroraDeleteSelectedItemAnim();

						}
					}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							mAuroraListView.auroraSetRubbishBack();
						}
					});
			builder.show();
		}

		// 准备滑动删除的响应事件，滑动之前允许用户做一些初始化操作
		@Override
		public void auroraPrepareDraged(int position) {

		}

		// 成功拖出垃圾桶之后的响应事件
		@Override
		public void auroraDragedSuccess(int position) {

		}

		// 进行了拖动垃圾桶操作，但是没有成功，比如只拖动了一点点
		@Override
		public void auroraDragedUnSuccess(int position) {

		}
	};

	private OnAuroraActionBarBackItemClickListener mOnActionBarBackItemListener = new OnAuroraActionBarBackItemClickListener() {

		@Override
		public void onAuroraActionBarBackItemClicked(int arg0) {

			Intent tIntent = new Intent();
			tIntent.putExtra(AuroraNewPlayListActivity.EXTR_RESULT_MODE, 0);
			tIntent.putStringArrayListExtra(AuroraNewPlayListActivity.EXTR_RESULT_DATA, mAddList);
			setResult(RESULT_OK, tIntent);
			finish();
		}

	};

	private OnAuroraActionBarItemClickListener mOnAuroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		@Override
		public void onAuroraActionBarItemClicked(int itemId) {
			switch (itemId) {
			case PLAY_BUTTON:
				if (mAuroraListView.auroraIsRubbishOut()) {
					mAuroraListView.auroraSetRubbishBack();
				}
				Intent intent = new Intent(AuroraFoldActivity.this, AuroraPlayerActivity.class);
				startActivity(intent);
//				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
				break;
			}
		}

	};

	/**
	 * 设置播放动画
	 */
	private void setPlayAnimation() {
		try {
			if (playView == null) {
				return;
			}
			if (MusicUtils.sService != null) {
				if (MusicUtils.sService.isPlaying()) {
					if (!isPlaying) {
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
				LogUtil.d(TAG, "--------PLAYSTATE_CHANGED:");

				setPlayAnimation();

			} else if (action.equals(MediaPlaybackService.META_CHANGED)) {
				LogUtil.d(TAG, "-----META_CHANGED");
			}
		}
	};

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
		if (isPlaying && playView != null) {
			playView.clearAnimation();
			playView.setBackgroundResource(R.drawable.aurora_left_bar_clicked);
			isPlaying = false;
		}
		unregisterReceiver(mStatusListener);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		MusicUtils.unbindFromService(mToken);

		if (startMode == 1) {
			mAuroraActionBar.setShowBottomBarMenu(false);
			mAuroraActionBar.showActionBottomeBarMenu();
			AuroraMusicActivityManiger.getInstance().removeActivity(this);
		}
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		mAuroraListView.auroraOnPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mAuroraListView.auroraOnResume();
	}

	/**
	 * 添加按钮响应
	 */
	private AuroraMenuBase.OnAuroraMenuItemClickListener auroraMenuCallBack = new AuroraMenuBase.OnAuroraMenuItemClickListener() {
		@Override
		public void auroraMenuItemClick(int itemId) {
			switch (itemId) {
			case R.id.song_add:
				if (mAddList != null && mAddList.size() != 0) {

					MusicUtils.addToPlaylist(AuroraFoldActivity.this, mAddList, playlistInfo.getPlaylistId(), playlistInfo.getName());
					Intent intent = new Intent(AuroraFoldActivity.this, AuroraSongSingle.class);
					intent.putExtra(AuroraSongSingle.EXTR_PLAYLIST_INFO, playlistInfo);
					startActivity(intent);
					// gobackToSongSingle();
//					overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
					AuroraMusicActivityManiger.getInstance().exit();
				}
				break;
			default:
				break;
			}
		}
	};

	/**
	 * 回到歌单页面
	 */
	private void gobackToSongSingle() {

		Intent tIntent = new Intent();
		tIntent.putExtra(AuroraNewPlayListActivity.EXTR_RESULT_MODE, 1);
		setResult(RESULT_OK, tIntent);
		finish();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {

		if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK && startMode == 1) {
			Intent tIntent = new Intent();
			tIntent.putExtra(AuroraNewPlayListActivity.EXTR_RESULT_MODE, 0);
			tIntent.putStringArrayListExtra(AuroraNewPlayListActivity.EXTR_RESULT_DATA, mAddList);
			setResult(RESULT_OK, tIntent);
			finish();
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

		String path = (String) arg0.getAdapter().getItem(arg2);
		Intent intent = new Intent(this, AuroraTrackBrowserActivity.class);
		intent.putExtra(EXTR_FOLD_START_MODE, 1);
		intent.putExtra(EXTR_FOLD_PATH, path);
		if (startMode == 1) {
			intent.putExtra(AuroraTrackBrowserActivity.FROM_CODE, 1);
			intent.putExtra(AuroraSongSingle.EXTR_PLAYLIST_INFO, playlistInfo);
			intent.putStringArrayListExtra(AuroraNewPlayListActivity.EXTR_ADDLIST_DATA, mAddList);
			startActivityForResult(intent, AuroraNewPlayListActivity.REQUEST_CODE_SONGS);
		} else {
			startActivity(intent);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		LogUtil.d(TAG, "requestCode:" + requestCode + " resultCode:" + resultCode);

		switch (requestCode) {
		case AuroraNewPlayListActivity.REQUEST_CODE_SONGS:
			if (resultCode != RESULT_OK) {
				return;
			}
			if (data == null) {
				return;
			}
			int mode = data.getIntExtra(AuroraNewPlayListActivity.EXTR_RESULT_MODE, 0);
			if (mode == 0) {
				mAddList = data.getStringArrayListExtra(AuroraNewPlayListActivity.EXTR_RESULT_DATA);
				if (mAddList.size() > 0) {
					bottomMenu.setBottomMenuItemEnable(0, true);
				} else {
					bottomMenu.setBottomMenuItemEnable(0, false);
				}
				bottomBarTitle.setText(getString(R.string.aurora_add_playlist_song_num, mAddList.size()));
			} else {
				gobackToSongSingle();
			}
			break;
		case RETAIN_CODE:
			getLoaderManager().restartLoader(0, null, AuroraFoldActivity.this);
			break;
		}
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (startMode == 1) {
			mAuroraActionBar.setShowBottomBarMenu(true);
			mAuroraActionBar.showActionBottomeBarMenu();
		}
	}

	@Override
	public void onMediaDbChange(boolean selfChange) {
		LogUtil.d(TAG, "onMediaDbChange:" + selfChange);
		// AuroraMusicUtil.getMountedStorage(this);//lory del 2014.9.2
		getLoaderManager().restartLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		StringBuilder where = new StringBuilder();
		where.append(Globals.QUERY_SONG_FILTER);
		String[] cons = new String[] { MediaStore.Audio.Media.DATA };
		return new CursorLoader(this, uri, cons, where.toString(), null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		List<String> list = new ArrayList<String>();
		mIgnorPathList = AuroraMusicUtil.doParseXml(this, "paths.xml");
		if (cursor != null && mIgnorPathList != null) {
			totalSongs = cursor.getCount();

			if (cursor.moveToFirst()) {
				do {
					String path = cursor.getString(0);
					path = path.substring(0, path.lastIndexOf("/"));
					if (!list.contains(path) && !mIgnorPathList.contains(path)) {
						list.add(path);
					}
					// 去掉忽略文件夹中的歌曲数
					if (mIgnorPathList.contains(path)) {
						totalSongs--;
					}
				} while (cursor.moveToNext());
			}
			totalFolds = list.size();
		}
		totalFold.setText(AuroraMusicUtil.getTransColorText(AuroraFoldActivity.this, totalFolds, totalSongs));
		mAuroraFoldListAdapter.addFoldData(list);
		changeViewState();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {

	}

	private void changeViewState() {

		if (totalFolds > 0) {
			noSongs.setVisibility(View.GONE);
			mAuroraListView.setVisibility(View.VISIBLE);
		} else {
			noSongs.setVisibility(View.VISIBLE);
			mAuroraListView.setVisibility(View.GONE);
		}

	}

	@Override
	public void onBackPressed() {

		if (mAuroraListView.auroraIsRubbishOut()) {
			mAuroraListView.auroraSetRubbishBack();
		} else {
			super.onBackPressed();
		}
	}
}
