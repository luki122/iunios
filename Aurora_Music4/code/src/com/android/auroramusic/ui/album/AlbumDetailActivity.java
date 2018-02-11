package com.android.auroramusic.ui.album;

import com.android.auroramusic.ui.AbstractBaseActivity;
import com.android.auroramusic.ui.AuroraPlayerActivity;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.Globals;
import com.android.music.MediaPlaybackService;
import com.android.music.MusicUtils;
import com.android.music.R;
import com.android.music.MusicUtils.ServiceToken;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;

/**
 * An activity representing a single Album detail screen. This activity is only
 * used on handset devices. On tablet-size devices, item details are presented
 * side-by-side with a list of items in a {@link AlbumListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link AlbumDetailFragment}.
 */
public class AlbumDetailActivity extends AuroraActivity implements MusicUtils.Defs, ServiceConnection {

	public String mArtistId;
	public String mArtistName;
	private int numTracks;

	public int getNumTracks() {
		return numTracks;
	}

	public void setNumTracks(int numTracks) {
		this.numTracks = numTracks;
	}

	private ServiceToken mToken;
	AuroraActivity mActivity;
	AlbumDetailFragment mFragment;
	private View vNowPlaying;
	View btn_cancel;
	View btn_selectAll;

	String selectAll;
	String unselectAll;

	Parcelable albumParcelable;
	public static int albumCount;

	private OnAuroraActionBarItemClickListener mOnAuroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {

		@Override
		public void onAuroraActionBarItemClicked(int itemId) {
			switch (itemId) {
			case Globals.NOW_PLAYING:
				Intent intent = new Intent(mActivity, AuroraPlayerActivity.class);
				startActivity(intent);
				break;

			default:
				break;
			}

		}
	};

	private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {
		@Override
		public void auroraMenuItemClick(int itemId) {
			switch (itemId) {
			case R.id.menu_album_delete:
				Log.e("liumx", "!!!!!!!!!!!!!!!!!!!!!!!---------------------");
				mFragment.deleteAlbums();
				break;
			case R.id.menu_album_addtolist:
				mFragment.addToPlaylist();
				break;
			default:
				break;
			}
		}
	};
	private AuroraActionBar mActionBar;

	public AlbumDetailActivity() {
		mActivity = AlbumDetailActivity.this;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		mToken = MusicUtils.bindToService(this, this);

		if (getIntent() != null) {
			mArtistName = getIntent().getStringExtra(Globals.KEY_ARTIST_NAME);
			mArtistId = getIntent().getStringExtra(Globals.KEY_ARTIST_ID);
			albumCount = getIntent().getIntExtra("artistofalbum", 1);
		} else {
			mArtistName = savedInstanceState.getString(Globals.KEY_ARTIST_NAME);
			mArtistId = savedInstanceState.getString(Globals.KEY_ARTIST_ID);
		}

		setAuroraContentView(R.layout.activity_album_detail, AuroraActionBar.Type.Normal);
		Bundle arguments = new Bundle();
		if (savedInstanceState == null) {
			// Create the detail fragment and add it to the activity
			// using a fragment transaction.
			arguments.putString(Globals.KEY_ARTIST_ID, getIntent().getStringExtra(Globals.KEY_ARTIST_ID));
			// if (getIntent().hasExtra("artistofalbum")) {
			arguments.putInt("artistofalbum", getIntent().getIntExtra("artistofalbum", 1));
			// }
			albumParcelable = getIntent().getParcelableExtra(Globals.KEY_ALBUM_ITEM);

		} else {
			if (savedInstanceState.containsKey(Globals.KEY_ALBUM_ITEM)) {
				albumParcelable = savedInstanceState.getParcelable(Globals.KEY_ALBUM_ITEM);
			} else {
				albumParcelable = null;
			}
		}
		if (albumParcelable != null) {
			AlbumContent.reset();
			arguments.putParcelable(Globals.KEY_ALBUM_ITEM, albumParcelable);
		} else {
			// 初始化专辑列表
			AlbumContent ac = new AlbumContent(mActivity, mArtistId);
			Log.e("liumx", "onCreate()::total albums-------------" + AlbumContent.ITEMS.size());
		}
		mFragment = new AlbumDetailFragment();
		mFragment.setArguments(arguments);
		getFragmentManager().beginTransaction().add(R.id.album_detail_container, mFragment).commitAllowingStateLoss();

		selectAll = mActivity.getResources().getString(R.string.selectAll);
		unselectAll = mActivity.getResources().getString(R.string.selectAllNot);
		initActionBar();
		iniView();
	}

	@Override
	protected void onStart() {
		super.onStart();
		registerStateChangeReceiver();
		setPlayAnimation();
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiverSafe(mStatusListener);
	}

	private void initActionBar() {
		setAuroraBottomBarMenuCallBack(auroraMenuCallBack);
		mActionBar = getAuroraActionBar();
		if (null != mActionBar) {
			mActionBar.setOnAuroraActionBarListener(mOnAuroraActionBarItemClickListener);
			mActionBar.addItem(R.drawable.song_playing, Globals.NOW_PLAYING, null);
			vNowPlaying = mActionBar.getItem(0).getItemView();
			mActionBar.initActionBottomBarMenu(R.menu.menu_albumedit, 2);

			btn_cancel = mActionBar.getSelectLeftButton();
			btn_cancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (null != mFragment) {
						mFragment.quitEditMode();
					}
				}
			});
			mActionBar.setTitle(mArtistName);
			btn_selectAll = mActionBar.getSelectRightButton();
			btn_selectAll.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (((TextView) btn_selectAll).getText().equals(selectAll)) {
						((TextView) btn_selectAll).setText(unselectAll);
						mFragment.getAdapter().selectAll();
						changeMenuState();
						mFragment.getAdapter().notifyDataSetChanged();
					} else {
						((TextView) btn_selectAll).setText(selectAll);
						mFragment.getAdapter().selectAllNot();
						changeMenuState();
						mFragment.getAdapter().notifyDataSetChanged();
					}
				}
			});
		}
	}

	private void iniView() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpTo(this, new Intent(this, AlbumListActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// 动态更改actionbar 显示全选/反选
	public void changeMenuState() {

		if (mFragment == null) {
			return;
		}
		Log.e("liumx", "mAdapter:size-----------------" + mFragment.getAdapter().getCount());
		if (mFragment.getAdapter().getCheckedCount() == mFragment.getAdapter().getCount()) {
			((TextView) btn_selectAll).setText(unselectAll);
		} else {
			((TextView) btn_selectAll).setText(selectAll);
		}
		if (mFragment.getAdapter().getCheckedCount() == 0) {
			mActionBar.getAuroraActionBottomBarMenu().setBottomMenuItemEnable(0, false);
			mActionBar.getAuroraActionBottomBarMenu().setBottomMenuItemEnable(1, false);
		} else {
			mActionBar.getAuroraActionBottomBarMenu().setBottomMenuItemEnable(0, true);
			mActionBar.getAuroraActionBottomBarMenu().setBottomMenuItemEnable(1, true);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK: {

			if (mActionBar != null && (mActionBar.auroraIsExitEditModeAnimRunning() || mActionBar.auroraIsEntryEditModeAnimRunning())) {
				return true;
			}
			try {
				boolean deleteIsShow = mFragment.getAuroraListView().auroraIsRubbishOut();
				if (deleteIsShow) {
					mFragment.getAuroraListView().auroraSetRubbishBack();
					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (mFragment != null && mFragment.getAdapter().isEditMode()) {
				mFragment.quitEditMode();
				return true;
			}
			break;
		}
		default: {

		}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void finish() {
		setResult(Globals.RESULT_CODE_ALBUM_TRACK_BROWSER);
		super.finish();
	}

	@Override
	public void onSaveInstanceState(Bundle outcicle) {
		// need to store the selected item so we don't lose it in case
		// of an orientation switch. Otherwise we could lose it while
		// in the middle of specifying a playlist to add the item to.
		// outcicle.putString("selectedalbum", mCurrentAlbumId);
		outcicle.putString(Globals.KEY_ARTIST_ID, mArtistId);
		outcicle.putString(Globals.KEY_ARTIST_NAME, mArtistName);
		outcicle.putInt("artistoftrack", numTracks);
		if (albumParcelable != null) {
			outcicle.putParcelable(Globals.KEY_ALBUM_ITEM, albumParcelable);
		}
		super.onSaveInstanceState(outcicle);
	}

	@Override
	protected void onResume() {
		if (albumParcelable == null) {
			// 初始化专辑列表
			AlbumContent ac = new AlbumContent(mActivity, mArtistId);
			Log.e("liumx", "onResume()::total albums-------------" + AlbumContent.ITEMS.size());
		}
		super.onResume();
	}

	@Override
	public void onServiceConnected(ComponentName arg0, IBinder arg1) {
		MusicUtils.updateNowPlaying(this);
	}

	@Override
	public void onServiceDisconnected(ComponentName arg0) {
		finish();
	}

	// 启动播放的动画
	public void startAnimation() {
		Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_anim);
		LinearInterpolator lin = new LinearInterpolator();
		animation.setInterpolator(lin);
		vNowPlaying.startAnimation(animation);
		vNowPlaying.setBackgroundResource(android.R.color.transparent);// add by
																		// chenhl
																		// 20140529
	}

	/**
	 * 设置播放动画
	 */
	public void setPlayAnimation() {
		try {
			if (MusicUtils.sService != null) {
				if (MusicUtils.sService.isPlaying()) {
					startAnimation();
				} else {
					vNowPlaying.clearAnimation();
					vNowPlaying.setBackgroundResource(R.drawable.aurora_left_bar_clicked);// add
																							// by
																							// chenhl
																							// 20140529
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			vNowPlaying.clearAnimation();
			vNowPlaying.setBackgroundResource(R.drawable.aurora_left_bar_clicked);// add
																					// by
																					// chenhl
																					// 20140529
		}
	}

	// 监听播放状态的变化
	private BroadcastReceiver mStatusListener = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(MediaPlaybackService.PLAYSTATE_CHANGED)) {
				setPlayAnimation();
			} else if (action.equals(MediaPlaybackService.META_CHANGED)) {
				if (mFragment != null) {
					mFragment.updatePlayingPosition();
				}

			}
		}
	};

	// 注册监听播放器状态更改的广播
	private void registerStateChangeReceiver() {
		IntentFilter f = new IntentFilter();
		f.addAction(MediaPlaybackService.PLAYSTATE_CHANGED);
		f.addAction(MediaPlaybackService.META_CHANGED);
		registerReceiver(mStatusListener, new IntentFilter(f));
	}

	// private BroadcastReceiver mScanListener = new BroadcastReceiver() {
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// MusicUtils.setSpinnerState(mActivity);
	// mReScanHandler.sendEmptyMessage(0);
	// if (intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
	// MusicUtils.clearAlbumArtCache();
	// }
	// }
	// };
	//
	// private Handler mReScanHandler = new Handler() {
	// @Override
	// public void handleMessage(Message msg) {
	// if (mFragment.getAdapter() != null) {
	// Log.e("liumx", "mScanListener ----------------reload()");
	// mFragment.reloadData();
	// }
	// }
	// };

	private BroadcastReceiver mTrackListListener = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			mFragment.getAuroraListView().invalidateViews();
			if (!mFragment.getAdapter().isEditMode()) {
				MusicUtils.updateNowPlaying(AlbumDetailActivity.this);
			}
		}
	};

	@Override
	public void onDestroy() {
		AlbumContent.reset();
		MusicUtils.unbindFromService(mToken);
		// unregisterReceiverSafe(mScanListener);
		super.onDestroy();
	}

	private void unregisterReceiverSafe(BroadcastReceiver receiver) {
		try {
			unregisterReceiver(receiver);
		} catch (IllegalArgumentException e) {
			// ignore
		}
	}

	// add by tangjie 2014/08/04
	public int[] getFlyendPoint() {
		int[] point = new int[2];
		if (vNowPlaying != null)
			vNowPlaying.getLocationInWindow(point);
		return point;
	}
	// add end

}
