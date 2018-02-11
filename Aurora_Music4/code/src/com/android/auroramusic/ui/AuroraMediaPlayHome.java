package com.android.auroramusic.ui;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import android.app.ActivityManager;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import android.service.dreams.DreamService;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraMenuItem;
import aurora.widget.AuroraTextView;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraActionBar.Type;
import aurora.widget.AuroraMenuBase;

import com.android.auroramusic.downloadex.DownloadManager;
import com.android.auroramusic.ui.AuroraHomeFragment.OnMainPageChangeListener;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.AuroraMusicUtil.SDCardInfo;
import com.android.auroramusic.util.DisplayUtil;
import com.android.auroramusic.util.FlowTips;
import com.android.auroramusic.util.Globals;
import com.android.auroramusic.util.LogUtil;
import com.android.auroramusic.util.ReportUtils;
import com.android.auroramusic.util.ThreadPoolExecutorUtils;
import com.android.music.Application;
import com.android.music.MediaPlaybackService;
import com.android.music.MusicUtils;
import com.android.music.R;
import com.android.music.MusicUtils.ServiceToken;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * create time:20140428
 * @author chenhl
 */
public class AuroraMediaPlayHome extends AbstractBaseActivity implements OnClickListener, MusicUtils.Defs {

	private static final String TAG = "MediaPlayHome";
	private static final int PLAY_BUTTON = 0;
	private AuroraTextView myMusic, findMusic;
	private int mMainMenuMode = 0; // 主菜单开关 0为我的音乐 1为发现音乐
	private boolean isPlaying = false; // 动画是否在运行
	private View playView; // 播放按钮
	private Drawable playViewDrawable;
	private Animation operatingAnim; // 播放按钮动画
	private ServiceToken mToken;
	private AuroraHomeFragment mAuroraHomeFragment = null;
	private AuroraAlertDialog mAuroraAlertDialog;
	private float betHeight = 0, betWidth = 0;
	private int betHeightOffset = 0, pageScrollPositionOffset = 0, changeColor = 0, changeColorTwo = 0;// add
	private float betWidthOffset = 0;// add by JXH
	private ImageView baiduLogo;

	private ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			setPlayAnimation();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {

		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Debug.startMethodTracing("music");

		LogUtil.d(TAG, "----------scale:" + getResources().getDisplayMetrics().density);

		setAuroraContentView(R.layout.aurora_main, Type.Custom);
		mAuroraHomeFragment = new AuroraHomeFragment();
		mAuroraHomeFragment.setOnMainPageChangeListener(mOnMainPageChangeListener);
		Fragment fragment = getFragmentManager().findFragmentById(R.id.activity_base_content);
		if (fragment != null) {
			LogUtil.d(TAG, " -------------old fragment !!!!!!");
			fragment.onDetach();
		}
		getFragmentManager().beginTransaction().replace(R.id.activity_base_content, mAuroraHomeFragment).commitAllowingStateLoss();
		// add by JXH 2015-6-2 begin

		if (playViewDrawable == null) {
			playViewDrawable = getResources().getDrawable(R.drawable.aurora_left_bar_clicked);
		}

		mToken = MusicUtils.bindToService(AuroraMediaPlayHome.this, mServiceConnection);
		MusicUtils.registerDbObserver(this);

		betHeightOffset = getResources().getInteger(R.integer.bet_height_offset);
		pageScrollPositionOffset = getResources().getInteger(R.integer.page_scroll_position_offset);
		changeColor = getResources().getInteger(R.integer.change_color);
		changeColorTwo = getResources().getInteger(R.integer.change_color_two);
		betWidthOffset = Float.parseFloat(getResources().getString(R.dimen.bet_width_offset));
		// add by JXH 2015-6-2 end

		try {
			setAuroraSystemMenuCallBack(auroraActionBarItemCallBack);
			AuroraActionBar mActionBar = getAuroraActionBar();// 获取actionbar

			View tView = mActionBar.getHomeButton();
			mActionBar.getHomeTextView().setVisibility(View.GONE);
			if (tView != null) {
				tView.setVisibility(View.GONE);
			}
			mActionBar.setCustomView(R.layout.aurora_custom_view_bar);
			mActionBar.setmOnActionBarBackItemListener(mOnActionBarBackItemListener);
			mActionBar.setOnAuroraActionBarListener(mOnAuroraActionBarItemClickListener);
			View customView = mActionBar.getCustomView(R.id.id_custom_view);
			myMusic = (AuroraTextView) customView.findViewById(R.id.id_my_music);
			findMusic = (AuroraTextView) customView.findViewById(R.id.id_find_music);
			baiduLogo = (ImageView) customView.findViewById(R.id.aurora_baidu_logo);
			baiduLogo.setAlpha(0f);
			// 是否隐藏在线音乐
			if (Globals.SWITCH_FOR_ONLINE_MUSIC) {
				findMusic.setOnClickListener(this);
				myMusic.setOnClickListener(this);
			} else {
				findMusic.setVisibility(View.GONE);
				baiduLogo.setVisibility(View.GONE);
			}
			// 旋转动画方式
			mActionBar.addItem(R.drawable.song_playing, PLAY_BUTTON, null);
			playView = mActionBar.getItem(PLAY_BUTTON).getItemView();
			operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_anim);
			LinearInterpolator lin = new LinearInterpolator();
			operatingAnim.setInterpolator(lin);
			setMenuEnable(true);
			setAuroraMenuItems(R.menu.menu_main,com.aurora.R.layout.aurora_menu_fillparent);
			getAuroraMenu().setOnDismissListener(new PopupWindow.OnDismissListener() {
				
				@Override
				public void onDismiss() {
					removeMusicCoverView();
				}
			});
		} catch (Exception e) {
			Log.e(TAG, " --- MediaPlay onCreate ActionBar fail");
		}

		// Iuni <lory><2014-06-25> add begin

		float myheight = getFontHeight(myMusic);
		float findHeight = getFontHeight(findMusic);
		betHeight = (myheight - findHeight) / 2 - betHeightOffset;
		float mywidth = getFontwidth(findMusic, myMusic.getTextSize());
		float findwidth = getFontwidth(findMusic, DisplayUtil.sp2px(this, betWidthOffset));
		betWidth = (mywidth - findwidth) / 2;
		ThreadPoolExecutor executor = executorUtil.getExecutor();
		executor.submit(new Runnable() {

			@Override
			public void run() {
				if (MusicUtils.mSongDb != null) {
					MusicUtils.mSongDb.updateFavoritesEx();

				}
			}
		});
		ReportUtils.getInstance(getApplicationContext()).reportMessage(ReportUtils.TAG_START);
		executorUtil.getExecutor().submit(mScanRunnable);

		showNetworkDialog();

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (AuroraMusicUtil.isStartTimingClose()) {
			mHandler.postDelayed(mRunnable, 1000);
			getAuroraMenu().setMenuTextByItemId(AuroraMusicUtil.getTimeString(AuroraMediaPlayHome.this, AuroraMusicUtil.getLiveAlarmTime()), R.id.aurora_close);
		} else {
			getAuroraMenu().setMenuTextByItemId(R.string.aurora_close_on_time, R.id.aurora_close);
		}
	}

	@Override
	protected void onPause() {
		mHandler.removeCallbacks(mRunnable);
		super.onPause();
	}

	private AuroraMenuBase.OnAuroraMenuItemClickListener auroraActionBarItemCallBack = new AuroraMenuBase.OnAuroraMenuItemClickListener() {
		@Override
		public void auroraMenuItemClick(int itemId) {
			switch (itemId) {
			case R.id.aurora_exit: {
				if (DownloadManager.getInstance(getApplicationContext()).isDownloading()) {
					showDeleteDialog();
				} else {
					finish();
					killProcess();
				}
				break;
			}
			case R.id.aurora_close:
				showTimeSelect();
				break;
			case R.id.aurora_net_setting:
				FlowTips.showWifiSwitch(AuroraMediaPlayHome.this);
				// FlowTips.showPlayFlowTips(AuroraMediaPlayHome.this);
				break;
			case R.id.aurora_storage_setting:
				showStorageSelect();
				break;
			default:
				break;
			}
		}
	};

	private Handler mHandler = new Handler();

	private Runnable mRunnable = new Runnable() {

		@Override
		public void run() {
			long time = AuroraMusicUtil.getLiveAlarmTime();
			if (time > 0) {
				getAuroraMenu().setMenuTextByItemId(AuroraMusicUtil.getTimeString(AuroraMediaPlayHome.this, time), R.id.aurora_close);
				mHandler.postDelayed(mRunnable, 1000);
			} else {
				getAuroraMenu().setMenuTextByItemId(R.string.aurora_close_on_time, R.id.aurora_close);
			}
		}
	};

	private void showDeleteDialog() {
		if (mAuroraAlertDialog == null) {
			AuroraAlertDialog.Builder build = new AuroraAlertDialog.Builder(this).setTitle(R.string.aurora_exit_tips)//.setMessage(R.string.aurora_exit_tips)
					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {

						}
					}).setPositiveButton(R.string.aurora_exit, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// SDKEngine.getInstance().destory();
							finish();
							killProcess();
						}
					});
			mAuroraAlertDialog = build.create();
		}
		mAuroraAlertDialog.show();

	}

	// Iuni <lory><2014-06-25> add begin
	public final static String ACTION_DIR_SCAN = "android.intent.action.AURORA_DIRECTORY_SCAN";
	public final static String ACTION_FILE_SCAN = "android.intent.action.AURORA_FILE_SCAN";
	private Runnable mScanRunnable = new Runnable() {

		@Override
		public void run() {
			if (AuroraMusicUtil.mMusicPath != null) {
				int num = AuroraMusicUtil.mMusicPath.length;
				for (int i = 0; i < num; i++) {
					String path = AuroraMusicUtil.getExternalStoragePath(getApplicationContext()) + AuroraMusicUtil.mMusicPath[i];

					Intent intent = new Intent();
					intent.setAction(ACTION_DIR_SCAN);
					intent.setData(Uri.fromFile(new File(path)));
					// LogUtil.d(TAG, "scan path:" + path);
					AuroraMediaPlayHome.this.sendBroadcast(intent);
				}

			}
		}
	};

	// Iuni <lory><2014-06-25> add end
	@Override
	public void onBackPressed() {
		ImageLoader.getInstance().stop();
		super.onBackPressed();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		LogUtil.d(TAG, "----onKeyDown keyCode:" + keyCode);
		if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0) {
			addMusicCoverView();
			if (!Globals.STORAGE_PATH_SETTING) {
				getAuroraMenu().removeMenuByItemId(R.id.aurora_storage_setting);
				return false;
			}
			if (!((Application) getApplication()).isHaveSdStorage()) {
				getAuroraMenu().setMenuItemEnable(R.id.aurora_storage_setting, false);
			} else {
				getAuroraMenu().setMenuItemEnable(R.id.aurora_storage_setting, true);
			}
		}
		/*
		 * if (keyCode == KeyEvent.KEYCODE_BACK && !isSearchBack()) {
		 * moveTaskToBack(false); LogUtil.d(TAG,
		 * "----onKeyDown moveTaskToBack"); return super.onKeyDown(keyCode,
		 * event); }
		 */
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onDestroy() {

		MusicUtils.unregisterDbObserver(this);
		MusicUtils.setIntPref(this, Globals.PREF_EXIT_NORMAL, 0);
		if (mToken != null) {
			MusicUtils.unbindFromService(mToken);
			mToken = null;
		}
		LogUtil.d(TAG, "-----------onDestroy");
		super.onDestroy();
	}

	private ThreadPoolExecutorUtils executorUtil = ThreadPoolExecutorUtils.getThreadPoolExecutor();

	private void killProcess() {

		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		try {
			executorUtil.shutdown();
			Method method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
			method.invoke(am, getPackageName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		LogUtil.d(TAG, "---------------------onStart");
		setPlayAnimation();
		IntentFilter f = new IntentFilter();
		f.addAction(MediaPlaybackService.PLAYSTATE_CHANGED);
		f.addAction(MediaPlaybackService.META_CHANGED);
		f.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(mStatusListener, f);

		// add by tanegjie 2014/07/30
		// IntentFilter inputFilter = new IntentFilter();
		// inputFilter.addAction(Intent.ACTION_INPUT_METHOD_HIDE);
		// inputFilter.addAction(Intent.ACTION_INPUT_METHOD_SHOW);
		// registerReceiver(inputReceiver, new IntentFilter(inputFilter));
		// add end

	}

	@Override
	protected void onStop() {
		// Debug.stopMethodTracing();
		if (isPlaying) {
			playView.clearAnimation();
			playView.setBackground(playViewDrawable);
			isPlaying = false;
		}
		unregisterReceiver(mStatusListener);
		// unregisterReceiver(inputReceiver);
		super.onStop();
	}

	private BroadcastReceiver mStatusListener = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(MediaPlaybackService.PLAYSTATE_CHANGED)) {
				LogUtil.d(TAG, "--------mStatusListener:");
				setPlayAnimation();
				if (mAuroraHomeFragment != null)
					mAuroraHomeFragment.setPlayAnimation();

			} else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				LogUtil.d(TAG, "connectiviy change");
				if (AuroraMusicUtil.isNetWorkActive(context) && mMainMenuMode == 1) {
					LogUtil.d(TAG, "network active!!");
					mAuroraHomeFragment.setOnlineMusic(mMainMenuMode);
				}
			}
		}
	};

	// add by tangjie 2014/07/30 start
	// private BroadcastReceiver inputReceiver = new BroadcastReceiver() {
	//
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// // TODO Auto-generated method stub
	// String action = intent.getAction();
	// if (action.equals(Intent.ACTION_INPUT_METHOD_SHOW)) {
	// if(mAuroraHomeFragment!=null){
	// mAuroraHomeFragment.changeButton(1);
	// }
	//
	// }
	//
	// if (action == Intent.ACTION_INPUT_METHOD_HIDE) {
	// if(mAuroraHomeFragment!=null){
	//
	// }
	//
	// }
	// }
	// };;
	// add end
	/**
	 * 设置播放动画
	 */
	private void setPlayAnimation() {
		LogUtil.d(TAG, "----isPlaying:"+isPlaying);
		try {
			if (MusicUtils.sService != null) {
				if (MusicUtils.sService.isPlaying()) {
					if (!isPlaying) {
						playView.startAnimation(operatingAnim);
						playView.setBackgroundResource(android.R.color.transparent);
						isPlaying = true;
					}
				} else if (isPlaying) {
					playView.clearAnimation();
					playView.setBackground(playViewDrawable);
					isPlaying = false;
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			if (isPlaying) {
				playView.clearAnimation();
				playView.setBackground(playViewDrawable);
				isPlaying = false;
			}
		}
	}

	// 屏蔽标题返回功能
	private OnAuroraActionBarBackItemClickListener mOnActionBarBackItemListener = new OnAuroraActionBarBackItemClickListener() {
		@Override
		public void onAuroraActionBarBackItemClicked(int itemid) {

			return;
		}
	};

	private OnAuroraActionBarItemClickListener mOnAuroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		@Override
		public void onAuroraActionBarItemClicked(int itemId) {
			switch (itemId) {
			case PLAY_BUTTON:

				Intent intent = new Intent();
				intent.setClass(AuroraMediaPlayHome.this, AuroraPlayerActivity.class);
				intent.setAction(AuroraPlayerActivity.ACTION_FROM_MAINACTIVITY);
				startActivity(intent);
//				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
				break;
			}
		}

	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.id_find_music:
			if (mMainMenuMode == 0)
				switchTitleText();
			break;
		case R.id.id_my_music:
			if (mMainMenuMode == 1)
				switchTitleText();
			break;
		}
	}

	private void switchTitleText() {
		if (mMainMenuMode == 0) {
			mMainMenuMode = 1;
		} else {
			mMainMenuMode = 0;
		}
		mAuroraHomeFragment.setCurrentPage(mMainMenuMode);
	}

	@Override
	public void onMediaDbChange(boolean selfChange) {
		mAuroraHomeFragment.onMediaDbChange();
	}

	private OnMainPageChangeListener mOnMainPageChangeListener = new OnMainPageChangeListener() {

		@Override
		public void onPageSelected(int position) {
			switch (position) {
			case 0:
				mMainMenuMode = 0;
				break;
			case 1:
				mMainMenuMode = 1;
				ReportUtils.getInstance(getApplicationContext()).reportMessage(ReportUtils.TAG_OL_MUSIC);
				break;
			}

		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			if (positionOffset != 0) {
				float offset = 1 + (float) (positionOffset * 0.2);
				float offsetMy = 1 - (float) (positionOffset * 0.2);
				scaleView(findMusic, offset);
				scaleView(myMusic, offsetMy);

				myMusic.setTranslationY(positionOffset * betHeight);
				findMusic.setTranslationY(-positionOffset * betHeight);
				baiduLogo.setTranslationX(positionOffset * betWidth);
				baiduLogo.setAlpha(positionOffset);
				myMusic.setTextColor(changeColor((int) (positionOffset * pageScrollPositionOffset)));
				findMusic.setTextColor(changeColor2((int) (positionOffset * pageScrollPositionOffset)));
			} else {

				if (position == 0) {
					myMusic.setTranslationY(0);
					findMusic.setTranslationY(0);
					baiduLogo.setTranslationX(0);
					findMusic.setTextColor(getResources().getColor(R.color.aurora_actionbar_text_2));
					myMusic.setTextColor(getResources().getColor(R.color.aurora_actionbar_text_1));
				} else if (position == 1) {
					myMusic.setTranslationY(betHeight);
					findMusic.setTranslationY(-betHeight);
					baiduLogo.setTranslationX(betWidth);
					baiduLogo.setAlpha(1.0f);
					findMusic.setTextColor(getResources().getColor(R.color.aurora_actionbar_text_1));
					myMusic.setTextColor(getResources().getColor(R.color.aurora_actionbar_text_2));
				}
			}
		}
	};

	private int changeColor(int increase) {

		String colors = "#";
		for (int i = 0; i < 3; i++) {
			colors += Integer.toHexString(changeColor + increase);
		}

		return Color.parseColor(colors);
	}

	private int changeColor2(int decrease) {

		String colors = "#";
		for (int i = 0; i < 3; i++) {
			colors += Integer.toHexString(changeColorTwo - decrease);
		}

		return Color.parseColor(colors);
	}

	private void scaleView(View view, float scale) {
		view.setScaleX(scale);
		view.setScaleY(scale);
	}

	private float getFontHeight(TextView view) {
		FontMetrics fm = view.getPaint().getFontMetrics();
		return (float) Math.ceil(fm.descent - fm.ascent);
	}

	private float getFontwidth(TextView view, float textsize) {
		LogUtil.d(TAG, "textsize:" + textsize);
		Paint paint = new Paint();
		paint.setTextSize(textsize);
		return paint.measureText(view.getText().toString());
	}

	@Override
	public void hideSearchviewLayout() {
		mAuroraHomeFragment.hideSearchviewLayout();
		getAuroraActionBar().getAuroraActionbarSearchView().getQueryTextView().setText("");
		super.hideSearchviewLayout();
		setPlayAnimation();

	}
	
	@Override
	public void showSearchviewLayout() {
		super.showSearchviewLayout();
		if (isPlaying) {
			playView.clearAnimation();
			playView.setBackground(playViewDrawable);
			isPlaying = false;
		}
	}

	/**
	 * is search View back
	 * @return
	 */
	private boolean isSearchBack() {
		if (mAuroraHomeFragment != null) {
			return mAuroraHomeFragment.isSearchBack();
		}
		return false;
	}

	private String[] choice = null;

	/**
	 * 存储设置
	 */
	private void showStorageSelect() {
		final List<String> storagePath = ((Application) getApplication()).getStoragePath();
		if (storagePath.size() <= 1) {
			LogUtil.e(TAG, "---error no sd");
			return;
		}
		final SDCardInfo phoneInfo = AuroraMusicUtil.getSDCardInfo(true, storagePath.get(0));
		final SDCardInfo cardInfo = AuroraMusicUtil.getSDCardInfo(true, storagePath.get(1));
		if (phoneInfo == null || cardInfo == null) {
			LogUtil.e(TAG, "---error phoneInfo:" + phoneInfo + " cardInfo:" + cardInfo);
			return;
		}
		String phone = AuroraMusicUtil.convertStorage(phoneInfo.inUse) + "/" + AuroraMusicUtil.convertStorage(phoneInfo.total);
		String sd = AuroraMusicUtil.convertStorage(cardInfo.inUse) + "/" + AuroraMusicUtil.convertStorage(cardInfo.total);
		if (cardInfo.total == 0 && phoneInfo.total == 0) {
			LogUtil.e(TAG, "-----error total is 0");
			return;
		} else if (phoneInfo.total == 0) {
			choice = new String[] { getResources().getString(R.string.sd, sd) };
		} else if (cardInfo.total == 0) {
			choice = new String[] { getResources().getString(R.string.this_phone, phone) };
		} else {
			choice = new String[] { getResources().getString(R.string.this_phone, phone), getResources().getString(R.string.sd, sd) };
		}

		AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(this).setTitleDividerVisible(true)
				.setSingleChoiceItems(choice, MusicUtils.getIntPref(getApplicationContext(), "storage_select", 0), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int position) {
						if (storagePath.size() <= position) {
							AuroraMusicUtil.showToast(AuroraMediaPlayHome.this, getResources().getString(R.string.error_no_storages));
							return;
						}
						String mount = storagePath.get(position);
						if (AuroraMusicUtil.sdIsMounted(mount)) {
							if (choice.length > 1) {
								if (position == 0) {
									if (Globals.AURORA_LOW_MEMORY > phoneInfo.free && AuroraMusicUtil.sdIsMounted(storagePath.get(1))) {
										if (Globals.AURORA_LOW_MEMORY > cardInfo.free) {// 两个存储器满，默认手机存储
											Globals.initPath(mount);
											MusicUtils.setIntPref(getApplicationContext(), "storage_select", 0);
											return;
										}
										// 手机存储满，选择SD卡
										Globals.initPath(storagePath.get(1));
										MusicUtils.setIntPref(getApplicationContext(), "storage_select", 1);
										AuroraMusicUtil.showToast(AuroraMediaPlayHome.this, getResources().getString(R.string.error_no_storages_space));
										return;
									}
								} else {
									if (Globals.AURORA_LOW_MEMORY > cardInfo.free) {// SD卡满，选择手机存储
										Globals.initPath(storagePath.get(0));
										MusicUtils.setIntPref(getApplicationContext(), "storage_select", 0);
										AuroraMusicUtil.showToast(AuroraMediaPlayHome.this, getResources().getString(R.string.error_no_storages_space));
										return;
									}
								}
							}
							Globals.initPath(mount);
							MusicUtils.setIntPref(getApplicationContext(), "storage_select", position);
						} else {
							AuroraMusicUtil.showToast(AuroraMediaPlayHome.this, getResources().getString(R.string.error_no_storages));
						}
					}
				}).setTitle(R.string.aurora_storage_setting).create();
		dialog.setCanceledOnTouchOutside(true);
		DownloadManager downloadManager = DownloadManager.getInstance(getApplication());
		if (downloadManager.isDownloading()) {
			View footerView = LayoutInflater.from(this).inflate(R.layout.storage_select, null);
			dialog.getListView().addFooterView(footerView, null, false);
		}
		dialog.show();
	}

	/**
	 * 网络提示
	 */
	private void showNetworkDialog() {
		boolean gprsNetDialog = MusicUtils.getIntPref(getApplicationContext(), "network_dialog_gprs", 0) != 1;
		if(!gprsNetDialog){
			return;
		}
		View view = LayoutInflater.from(this).inflate(R.layout.net_dialog_view, null);
		final AuroraCheckBox checkBox = (AuroraCheckBox)view.findViewById(R.id.dialog_check_box);
		AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(this).setTitleDividerVisible(true).setTitle(R.string.use_network_title)
				.setPositiveButton(R.string.net_continue, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						if(checkBox.isChecked()){
							MusicUtils.setIntPref(getApplicationContext(), "network_dialog_gprs", 1);
						}else {
							MusicUtils.setIntPref(getApplicationContext(), "network_dialog_gprs", 2);
						}
					}
				}).setNegativeButton(R.string.aurora_exit, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						MusicUtils.setIntPref(getApplicationContext(), "network_dialog_gprs", 0);
						finish();
						killProcess();
					}
				}).create();
		
		/*dialog.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				if(MusicUtils.getIntPref(getApplicationContext(), "network_dialog_gprs", 0)!=2){
					MusicUtils.setIntPref(getApplicationContext(), "network_dialog_gprs", checkBox.isChecked()?1:0);
				}
			}
		});*/
		dialog.setView(view);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
		dialog.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_BACK){
					return true;
				}
				return false;
			}
		});
	}

	private void showTimeSelect() {

		AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(this).setTitleDividerVisible(true)
				.setSingleChoiceItems(R.array.aurora_time_choice, MusicUtils.getIntPref(getApplicationContext(), "time_select", 0), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {

						int time = 0;
						switch (arg1) {
						case 0:
							time = 0;
							break;
						case 1:
							time = 10;
							break;
						case 2:
							time = 15;
							break;
						case 3:
							time = 30;
							break;
						case 4:
							time = 45;
							break;
						case 5:
							time = 60;
							break;
						}
						MusicUtils.setIntPref(getApplicationContext(), "time_select", arg1);
						AuroraMusicUtil.startAlarmToClose(AuroraMediaPlayHome.this, time);
						if (time > 0) {
							mHandler.removeCallbacks(mRunnable);
							mHandler.postDelayed(mRunnable, 1000);
							getAuroraMenu().setMenuTextByItemId(AuroraMusicUtil.getTimeString(AuroraMediaPlayHome.this, AuroraMusicUtil.getLiveAlarmTime()), R.id.aurora_close);
						} else {
							getAuroraMenu().setMenuTextByItemId(R.string.aurora_close_on_time, R.id.aurora_close);
						}
					}
				}).setTitle(R.string.aurora_close_on_time).create();
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
	}

	
	/*************** 蒙板开始 ****************/
	private View mCoverView = null;
	private FrameLayout windowLayout;
	private Animation coverAnimation;

	private void loadAnimation(int animId) {
		try {
			coverAnimation = AnimationUtils.loadAnimation(AuroraMediaPlayHome.this, animId);
			mCoverView.startAnimation(coverAnimation);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void addMusicCoverView() {
		AuroraMusicUtil.justTest();
		mCoverView = new TextView(AuroraMediaPlayHome.this);
		mCoverView.setBackgroundColor(Color.parseColor("#666666"));
		mCoverView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
		windowLayout = (FrameLayout)getWindow().getDecorView();
		windowLayout.addView(mCoverView);
		LogUtil.d(TAG, "----windowLayout:"+windowLayout+" mCoverView:"+mCoverView);
		loadAnimation(com.aurora.R.anim.aurora_menu_cover_enter);
	}

	public void removeMusicCoverView() {
		LogUtil.d(TAG, "----windowLayout:"+windowLayout+" mCoverView:"+mCoverView);
		if (mCoverView != null && windowLayout != null) {
			windowLayout.removeView(mCoverView);
		}
		loadAnimation(com.aurora.R.anim.aurora_menu_cover_exit);
	}

	/*************** 蒙板end ****************/
}
