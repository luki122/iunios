package com.android.auroramusic.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.http.HttpStatus;

import com.android.auroramusic.adapter.AuroraPlayRadioAdapter;
import com.android.auroramusic.adapter.AuroraPlayerListViewAdapter;
import com.android.auroramusic.adapter.AuroraPlayerPagerAdapter;
import com.android.auroramusic.db.AuroraMusicInfo;
import com.android.auroramusic.downloadex.BitmapUtil;
import com.android.auroramusic.downloadex.DownloadInfo;
import com.android.auroramusic.downloadex.DownloadManager;
import com.android.auroramusic.downloadex.DownloadStatusListener;
import com.android.auroramusic.downloadex.DownloadTask;
import com.android.auroramusic.model.AuroraAnimationModel;
import com.android.auroramusic.model.OTAFrameAnimation;
import com.android.auroramusic.model.OTAMainPageFrameLayout;
import com.android.auroramusic.model.XiaMiSdkUtils;
import com.android.auroramusic.model.AuroraAnimationModel.OnAnimationListener;
import com.android.auroramusic.online.AuroraNetSearchActivity;
import com.android.auroramusic.online.AuroraRadioListActivity;
import com.android.auroramusic.online.AuroraSearchLyricActivity;
import com.android.auroramusic.share.AuroraShareXLWb;
import com.android.auroramusic.share.AuroraWxShare;
import com.android.auroramusic.share.AuroraShareXLWb.AuroraWeiBoCallBack;
import com.android.auroramusic.ui.AuroraDialogFragment.AuroraDilogCallBack;
import com.android.auroramusic.util.AuroraIListItem;
import com.android.auroramusic.util.AuroraListItem;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.Blur;
import com.android.auroramusic.util.DialogUtil;
import com.android.auroramusic.util.DisplayUtil;
import com.android.auroramusic.util.FlowTips;
import com.android.auroramusic.util.FlowTips.OndialogClickListener;
import com.android.auroramusic.util.Globals;
import com.android.auroramusic.util.IntentFactory;
import com.android.auroramusic.util.LogUtil;
import com.android.auroramusic.util.ReportUtils;
import com.android.auroramusic.util.ThreadPoolExecutorUtils;
import com.android.auroramusic.widget.AuroraAnimationImageView;
import com.android.auroramusic.widget.AuroraImageButton;
import com.android.auroramusic.widget.AuroraLyricView;
// import com.android.auroramusic.widget.AuroraPlayerSurfaceView;
import com.android.auroramusic.widget.AuroraViewPager;
import com.android.auroramusic.widget.AuroraScrollView;
import com.android.auroramusic.widget.AuroraScrollView.ScrollViewListener;
import com.android.music.Application;
import com.android.music.IMediaPlaybackService;
import com.android.music.MediaPlaybackService;
import com.android.music.MusicUtils;
import com.android.music.R;
import com.android.music.RepeatingImageButton;
import com.android.music.MusicUtils.LogEntry;
import com.android.music.MusicUtils.ServiceToken;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.constant.WBConstants;
import com.xiami.music.model.RadioInfo;
import com.xiami.sdk.callback.OnlineSongsCallback;
import com.xiami.sdk.entities.OnlineSong;
import com.xiami.sdk.entities.OnlineSong.Quality;
import com.xiami.sdk.utils.ImageUtil;

import android.R.integer;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.AudioManager;
import android.media.audiofx.AudioEffect;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.EdgeEffectCompat;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView.ScaleType;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EdgeEffect;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraCheckBox;

@SuppressLint("NewApi")
public class AuroraPlayerActivity extends AbstractBaseActivity implements OnAnimationListener {
	private static final String TAG = AuroraPlayerActivity.class.getSimpleName();
	private boolean mbShowPagerBk = false;
	private AuroraViewPager mViewPager = null;
	private List<View> mViews = null;
	private AuroraPlayerPagerAdapter mPagerAdapter = null;
	private EdgeEffectCompat mleftCompat;
	private EdgeEffectCompat mrightCompat;
	private OTAMainPageFrameLayout mPrevButton;
	// private ImageButton mPauseButton;
	private OTAMainPageFrameLayout mPauseButton;
	private OTAMainPageFrameLayout mNextButton;
	private IMediaPlaybackService mService = null;
	private ImageButton mShuffleButton;
	private ImageButton mLoveButton;
	private ImageButton mBackButton;
	private ImageButton mShareButton;
	private ImageView dotImgView0 = null;
	private ImageView dotImgView1 = null;
	private ImageView dotImgView2 = null;
	private ServiceToken mToken;
	private Toast mToast;
	private View mDotLayout;
	private boolean mPlayPause = false;
	private AuroraWorker mAlbumArtWorker;
	private AlbumArtHandler mAlbumArtHandler;
	private ListView mListView = null;
	private AuroraPlayerListViewAdapter<AuroraListItem> mListAdapter = null;
	// private ImageView mAlbum = null;
	private AuroraAnimationImageView mAlbum = null;
	private AuroraLyricView mLyricView;
	private View mPlayerListView = null;
	private View mPlayerView = null;
	private View mPlayerLrcView = null;
	private TextView mArtistName;
	private TextView mAlbumName;
	private TextView mTrackName;
	private TextView mCurrentTime;
	private TextView mTotalTime;
	private SeekBar mProgress;
	private boolean mbUriPath = false;
	private int mPlayMode = 0;
	private int mCurrentPlaying = -1;
	private long mDuration;
	private long mPosOverride = -1;
	private long mLastSeekEventTime;
	private long mStartSeekPos = 0;
	private boolean mSeeking = false;
	private boolean mFromTouch = false;
	private boolean paused;
	private boolean onPaused = false;
	private boolean mFromNotification = false;
	// public final static String ACTION_MY_NOTIFICATION =
	// "com.android.music.AURORA_NOTIFICATION_PLAYBACK";
	public final static String ACTION_FROM_MAINACTIVITY = "android.intent.action.mainactivity";
	private final static String ACTION_FROM_URI = "android.intent.action.VIEW";
	private static final String ACTION_CHANGE_STATUSBAR_BG = "aurora.action.CHANGE_STATUSBAR_BG";
	private boolean mFromMain = false;
	private boolean mFromUri = false;
	private LinearLayout mViewLayout;
	private FrameLayout mBgLayout;
	private int mNavigationBar;
	private ImageView mbgView;
	private Drawable mDefautDrawable = null;
	private AuroraScrollView mLycScrollView = null;
	private Bitmap mDefautBitmap = null;
	private int mPaddingOffset = 0;
	private Bitmap mDefaultBg = null;
	private ImageView mPlaySelect;
	// private ImageView mBaiduLog;
	private View mBaiduLog;
	private int mPagePos = 1;
	private boolean isShowAnimator = false;
	private View mPlayerControl = null;
	private AuroraImageButton mSongLayout = null;
	private AuroraImageButton mArtistLayout = null;
	private AuroraImageButton mRingerLayout = null;
	private AuroraImageButton mSoundLayout = null;
	// private AuroraImageButton mAlumLayout = null;
	private AuroraImageButton mDownLoadLayout = null;
	private boolean mbDowned = false;
	private boolean mbRepeat = false;
	private boolean mbSelected = false;
	private boolean mbNotValid = false;
	private boolean mbSmallSize = false;
	// animation start
	private AuroraAnimationModel mAnimationModel1 = null;
	private AuroraAnimationModel mAnimationModel2 = null;
	private boolean mAnimationStop = false;
	private ImageView mNote1;
	private ImageView mNote2;
	private ScaleAnimation inScaleAnimation;
	private ScaleAnimation outScaleAnimation;
	private AlphaAnimation inAnimation;
	private AnimationSet mAnimationSet;
	private AlphaAnimation outAlphaAnimation;
	private ImageView mAnimView = null;
	private Bitmap mAnimationBitmap = null;
	private boolean mbFirstAnim = true;
	// private AuroraPlayerSurfaceView mSurfaceView = null;
	private boolean mOnCreate = false;
	private boolean mFirstAnim = false;
	// animation end
	private long firstAid;
	private long firstSid;
	private ImageView mLrcBlur1 = null;
	private ImageView mLrcBlur2 = null;
	private Uri mPlayUri = null;
	private boolean misMms = false;
	private int offset = 0;
	// net start
	private boolean mBNet = true;
	private String mTitleNameStr;
	private String mArtistNameStr;
	private int mbNetSong = 0;
	private String mDefaultPicUri = "http_pic";
	private AuroraListItem mItemInfo = null;
	// private static final String mDownLoadPath =
	// "/sdcard/Music/download/download/";
	// private DownloadEntry mDownloadEntry = null;
	private String mPicurl = null;
	private boolean mBtnDown = false;
	// private View mAllView = null;
	// net end
	// 分享 start
	private AuroraWxShare mWxShare = null;
	private int mWidth = 0;
	private DisplayImageOptions mOptions = null;
	private static final int AURORA_REFRESH = 1;
	private static final int AURORA_REFRESH_LRC = 19;
	private static final int AURORA_QUIT = 2;
	private static final int AURORA_GET_ALBUM_ART = 3;
	private static final int AURORA_ALBUM_ART_DECODED = 4;
	private static final int AURORA_REFRESH_LYRIC = 5;
	private static final int AURORA_REFRESH_LISTVIEW = 6;
	private static final int AURORA_ALBUMBG_DECODED = 7;
	private static final int AURORA_REFRESH_LISTVIEW_BYURI = 8;
	private static final int AURORA_DB_CHANGED = 9;
	private static final int AURORA_REFRESH_LISTVIEW_NOCHANGED = 10;
	private static final int AURORA_ANIMATION_REFRESH = 11;
	// net msg
	private static final int AURORA_DECODE_ALBUMIMG_NET = 12;
	private static final int AURORA_ANIMATION_STASRT = 13;
	private static final int AURORA_GET_ALBUM_ARTDEFAULT = 14;
	private static final int AURORA_GET_ALBUM_ERROR = 15;
	private static final int AURORA_META_CHANGED = 16; // add by JXH
	private static final int AURORA_SEEK = 17; // add by JXH
	private static final int AURORA_STOPTRACKINGTOUCH = 18; // add by JXH
	private ImageView mSearchLyric;
	private List<String> mPathsXml = new ArrayList<String>();
	private SearchImgTask mSearchImgTask;
	private LoadLrtThread mLoadLrtThread = null;
	private boolean isdestory = false;
	// add by chenhl for xiami radio
	private boolean isRadioType = false;
	private AuroraPlayRadioAdapter mAuroraPlayRadioAdapter = null;

	// add by chenhl for xiami radio
	@Override
	protected void onNewIntent(Intent intent) {
		LogUtil.d(TAG, "onNewIntent");
		setIntent(intent);
		AuroraShareXLWb.getInstance(getApplicationContext()).handleWeiboResponse(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtil.d(TAG, "onCreate()");
		isdestory = false;
		mFromMain = false;
		mbfirstIn = false;
		mbSelected = true;
		mbNotValid = false;
		mbUriPath = false;
		mFromUri = false;
		mPlayUri = null;
		misMms = false;
		mbSmallSize = false;
		mPlayPause = false;
		mFirstAnim = false;
		mPicurl = null;
		offset = DisplayUtil.dip2px(this, 6f);
		Intent tIntent = getIntent();
		if (tIntent != null) {
			if (ACTION_FROM_MAINACTIVITY.equalsIgnoreCase(tIntent.getAction())) {
				mFromMain = true;
			} else if (ACTION_FROM_URI.equalsIgnoreCase(tIntent.getAction())) {
				mFromUri = true;
				MusicUtils.registerDbObserver(this);
			}
		}
		mPathsXml = AuroraMusicUtil.doParseXml(this, "paths.xml");
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		// LogUtil.getInstance(this).start();
		AuroraMusicUtil.initData(this);
		try {
			setContent();
			initViews();
			findViews();
			initData();
		} catch (Exception e) {
			Log.i(TAG, "----- ---- AuroraPlayerActivity fail");
			e.printStackTrace();
		}
		initNotify();
		mOnCreate = true;
	}

	// 透明通知栏
	private void initNotify() {
		if (Build.VERSION.SDK_INT >= 19 && Globals.SWITCH_FOR_TRANSPARENT_STATUS_BAR) {
			RelativeLayout layoutView = (RelativeLayout) findViewById(R.id.title_layout);
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layoutView.getLayoutParams();
			params.topMargin = getResources().getDimensionPixelOffset(com.aurora.R.dimen.status_bar_height);
			layoutView.setLayoutParams(params);
			findViewById(R.id.id_actionbar_bg).setVisibility(View.GONE);
			if (Build.VERSION.SDK_INT < 21) {
				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				Notification.Builder builder = new Notification.Builder(this);
				builder.setSmallIcon(com.aurora.R.drawable.aurora_switch_on);
				String tag = "aurorawhiteBG653";
				notificationManager.notify(tag, 0, builder.build());
			}
		}
	}

	private void initData() {
		mOptions = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
				.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).bitmapConfig(Bitmap.Config.RGB_565).build();
	}

	@Override
	public void onMediaDbChange(boolean selfChange) {
		// mHandler.removeMessages(AURORA_DB_CHANGED);
		// mHandler.obtainMessage(AURORA_DB_CHANGED).sendToTarget();
	}

	/**
	 * 加载view
	 */
	protected void setContent() {
		mAlbumArtWorker = new AuroraWorker("aurora album art worker");
		mAlbumArtHandler = new AlbumArtHandler(mAlbumArtWorker.getLooper());
		setContentView(R.layout.aurora_player);
		mBgLayout = (FrameLayout) findViewById(R.id.layout_bg);
		mNavigationBar = AuroraMusicUtil.getNavigationBarPixelHeight(AuroraPlayerActivity.this);
		int hight = getWindowManager().getDefaultDisplay().getHeight() + mNavigationBar - DisplayUtil.dip2px(this, 170f);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, hight);
		mBgLayout.setLayoutParams(lp);
		mPaddingOffset = DisplayUtil.dip2px(this, 2.0f);
		mWidth = DisplayUtil.dip2px(this, 288f);
		mbgView = (ImageView) findViewById(R.id.layout_bg_image);
		if (mDefaultBg == null) {
			mDefaultBg = BitmapUtil.decodeSampledBitmapFromResource(getResources(), R.drawable.default_back, 100, 100);
		}
		if (mbgView != null) {
			mbgView.setScaleType(ScaleType.FIT_XY);
			mbgView.setImageBitmap(mDefaultBg);
		}
		if (mDefautBitmap == null) {
			int rid = -1;
			if (AuroraMusicUtil.isIndiaVersion()) {
				rid = R.drawable.default_album_india_bg;
			} else {
				rid = R.drawable.default_album_bg;
			}
			mDefautBitmap = BitmapFactory.decodeResource(getResources(), rid);
		}
	}

	/**
	 * 页面初始化
	 */
	protected void initViews() {
		mViews = new ArrayList<View>();
		LayoutInflater inflater = LayoutInflater.from(this);
		mPlayerListView = inflater.inflate(R.layout.aurora_viewpager_listview, null);
		mViews.add(mPlayerListView);
		mPlayerView = inflater.inflate(R.layout.aurora_viewpager_player, null);
		mViews.add(mPlayerView);
		mPlayerLrcView = inflater.inflate(R.layout.aurora_viewpager_lrc, null);
		mViews.add(mPlayerLrcView);
		mViewPager = (AuroraViewPager) findViewById(R.id.auroraplayer_viewpager);
		mViewPager.setViewPageOnScrolled(false);
		mPagerAdapter = new AuroraPlayerPagerAdapter(mViews);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setCurrentItem(1);
		mViewPager.setOnPageChangeListener(mPageChangeListener);
		mPagePos = 1;
		initDots();
		initListView(mPlayerListView);
		initLyricView(mPlayerLrcView);
		if (mbShowPagerBk) {
			initViewPagerEdgeEffect();
		}
	}

	private void initDots() {
		mDotLayout = (View) findViewById(R.id.dot_layout);
		dotImgView0 = (ImageView) findViewById(R.id.dot_player_listview);
		dotImgView1 = (ImageView) findViewById(R.id.dot_player_ui);
		dotImgView2 = (ImageView) findViewById(R.id.dot_player_lrc);
		dotImgView0.setImageResource(R.drawable.dot_unselect);
		dotImgView1.setImageResource(R.drawable.dot_select);
		dotImgView2.setImageResource(R.drawable.dot_unselect);
		return;
	}

	private void updateDots(int position) {
		boolean bclearAnim = false;
		boolean showLog = false;
		if (position == 0) {
			bclearAnim = true;
			dotImgView0.setImageResource(R.drawable.dot_select);
			dotImgView1.setImageResource(R.drawable.dot_unselect);
			dotImgView2.setImageResource(R.drawable.dot_unselect);
			showLog = true;
		} else if (position == 1) {
			dotImgView0.setImageResource(R.drawable.dot_unselect);
			dotImgView1.setImageResource(R.drawable.dot_select);
			dotImgView2.setImageResource(R.drawable.dot_unselect);
		} else {
			// showLog = true; // add by chenhl 第三个界面不显示百度logo
			bclearAnim = true;
			dotImgView0.setImageResource(R.drawable.dot_unselect);
			dotImgView1.setImageResource(R.drawable.dot_unselect);
			dotImgView2.setImageResource(R.drawable.dot_select);
			if (mLyricView != null) {
				mLyricView.setFirstScroll();
			}
		}
		// add by chenhl start 20141016
		if (!Globals.SWITCH_FOR_ONLINE_MUSIC || !Globals.SHOW_SEARCH_LRC) {
			mSearchLyric.setVisibility(View.GONE);
		}
		// add by chenhl end 20141016
		if (mBaiduLog != null) {
			if (!showLog && mbNetSong == 1) {
				mBaiduLog.setVisibility(View.VISIBLE);
				mSearchLyric.setVisibility(View.GONE);
			} else {
				mBaiduLog.setVisibility(View.GONE);
				if (position != 0 && Globals.SHOW_SEARCH_LRC) {
					mSearchLyric.setVisibility(View.VISIBLE);
				} else {
					mSearchLyric.setVisibility(View.GONE);
				}
			}
		}
		if (bclearAnim) {
			if (mPlayerControl != null && mPlayerControl.getVisibility() == View.VISIBLE) {
				mPlayerControl.clearAnimation();
				mPlayerControl.setVisibility(View.GONE);
			}
			if (mAlbum != null) {
				mAlbum.setStartAnimation(false);
			}
		} else {
			try {
				if (mAlbum != null && mService != null && mService.isPlaying()) {
					mAlbum.setStartAnimation(true);
				}
			} catch (Exception e) {
			}
		}
		return;
	}

	boolean isOnTounch = false;

	private void initLyricView(View lrcView) {
		if (lrcView == null) {
			return;
		}
		mLyricView = (AuroraLyricView) (lrcView.findViewById(R.id.lyric_view));
		mLycScrollView = (AuroraScrollView) (lrcView.findViewById(R.id.lyric_scrollview));
		mLyricView.setHasLyric(true);
		mLycScrollView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (mService != null) {
					boolean tplaying = false;
					try {
						tplaying = mService.isPlaying();
					} catch (Exception e) {
						e.printStackTrace();
					}
					mLyricView.setTouchMode(v, event, tplaying);
				} else {
					mLyricView.setTouchMode(v, event, false);
				}
				return false;
			}
		});
		mLycScrollView.setScrollViewListener(mLyricView);
		return;
	}

	private long[] mNowPlaying;
	private int mDataSize = 0;
	private boolean mbfirstIn = false;
	// private Cursor mCurrentPlaylistCursor;
	private ArrayList<AuroraListItem> tmp_ListData = null;
	private ArrayList<AuroraListItem> tListData = null;
	private ArrayList<AuroraListItem> tServiceListData = null;
	static final String[] mCursorCols = { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
			// MediaStore.Audio.Media.DISPLAY_NAME,
			MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ARTIST_ID,
			MediaStore.Audio.Media.DURATION };

	private void updateLoveButton(long id) {
		if (mService != null) {
			try {
				boolean bfavorite = false;
				if (id > 0) {
					bfavorite = mService.isFavorite(id);
				} else {
					bfavorite = mService.isFavorite(mService.getAudioId());
				}
				if (bfavorite) {
					mLoveButton.setImageResource(R.drawable.aurora_play_love_select);
				} else {
					mLoveButton.setImageResource(R.drawable.aurora_play_love);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return;
	}

	private String mWhereData = null;

	private class RunThreadAsyncTask extends AsyncTask<Void, Void, Integer> {
		private long[] mListPlaying;
		private int mListSize = 0;

		// private boolean bQuery = false;
		public RunThreadAsyncTask() {
			super();
		}

		@Override
		protected Integer doInBackground(Void... params) {
			synchronized (AuroraPlayerActivity.this) {
					if (mService == null) {
						return -1;
					}
					mbfirstIn = false;
					try {
						mListPlaying = mService.getQueue();
					} catch (RemoteException ex) {
						mListPlaying = new long[0];
					}
					mListSize = mListPlaying.length;
					if (mListSize == 0) {
						mbfirstIn = true;
					}
					StringBuilder where = new StringBuilder();
					HashMap<Long, Integer> tMap = new HashMap<Long, Integer>();
					if (!mbfirstIn && !isCancelled()) {
						where.append(MediaStore.Audio.Media._ID + " IN (");
						for (int i = 0; i < mListSize; i++) {
							where.append(mListPlaying[i]);
							tMap.put(mListPlaying[i], i);
							if (i < mListSize - 1) {
								where.append(",");
							}
						}
						// where.append(")");//Globals.QUERY_SONG_FILTER
						where.append(") AND " + Globals.QUERY_SONG_FILTER);
					}
//					LogUtil.d(TAG, "----- ----- doInBackground 1 ---- where:" + where.toString());
					if (mWhereData != null && mWhereData.equalsIgnoreCase(where.toString()) && tListData != null && tListData.size() > 0) {
						return 1;
					}
					mWhereData = where.toString();
					Cursor mCurrenlistCursor = null;
					try {
						if (!isCancelled()) {
							if (!mbfirstIn) {
								mCurrenlistCursor = MusicUtils.query(AuroraPlayerActivity.this, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
										mCursorCols, where.toString(), null, null);
							} else {
								where.append(Globals.QUERY_SONG_FILTER);
								mCurrenlistCursor = MusicUtils.query(AuroraPlayerActivity.this, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
										mCursorCols, where.toString(), null, null);
							}
							if (mCurrenlistCursor == null) {
								mListSize = 0;
								return -1;
							}
							int size = mCurrenlistCursor.getCount();
							if (size == 0 && !mbfirstIn) {// lory modify for bug
															// 9113
								if (mCurrenlistCursor != null) {
									mCurrenlistCursor.close();
									mCurrenlistCursor = null;
								}
								StringBuilder where2 = new StringBuilder();
								where2.append(Globals.QUERY_SONG_FILTER);
								mCurrenlistCursor = MusicUtils.query(AuroraPlayerActivity.this, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
										mCursorCols, where2.toString(), null, null);
								if (mCurrenlistCursor == null) {
									mListSize = 0;
									return -1;
								}
								if (mCurrenlistCursor.getCount() > 0) {
									mbfirstIn = true;
								}
							}
							tmp_ListData = null;
							tmp_ListData = new ArrayList<AuroraListItem>();
							if (mCurrenlistCursor.moveToFirst()) {
								do {
									int mId = mCurrenlistCursor.getInt(0);
									String mTitle = mCurrenlistCursor.getString(1);
									String mPath = mCurrenlistCursor.getString(2);
									String mAlbumName = mCurrenlistCursor.getString(3);
									String mArtistName = mCurrenlistCursor.getString(4);
									String albumUri = AuroraMusicUtil.getImgPath(getApplication(),
											AuroraMusicUtil.MD5(mTitle + mArtistName + mAlbumName));
									String mUri = mPath;
									AuroraListItem item = new AuroraListItem(mId, mTitle, mUri, mAlbumName, 0, mArtistName, 0, albumUri, null, null,
											0);
									String dir = mUri.substring(0, mUri.lastIndexOf("/"));
									if (!mPathsXml.contains(dir) || !mbfirstIn) {
										tmp_ListData.add(item);
									}
								} while (mCurrenlistCursor.moveToNext());
							}
						}
						if (!mbfirstIn && tmp_ListData.size() > 0 && !isCancelled()) {
							Collections.sort(tmp_ListData, new MyListComparator(tMap));
						}
						return 2;
					} catch (Exception e) {
						LogUtil.d(TAG, "----- ----- RunThreadAsyncTask fail ----");
						// e.printStackTrace();
					} finally {
						if (mCurrenlistCursor != null) {
							mCurrenlistCursor.close();
							mCurrenlistCursor = null;
						}
					}
				}
			return 0;
		}

		@Override
		protected void onPostExecute(Integer result) {
			LogUtil.d(TAG, "--------------result:" + result);
			if (tListData == null) {
				tListData = new ArrayList<AuroraListItem>();
				if (mListAdapter != null) {
					mListAdapter.notifyDataSetChanged();
				}
			}
			if (isCancelled()) {
				return;
			}
			if (result == 1) {
				if (mListAdapter != null) {
					tListData.clear();
					tListData.addAll(tmp_ListData);
					mListAdapter.notifyDataSetChanged();
					upDateListItemSelection();
				}
			} else if (result == 2) {
				try {
					mService.setListInfo(tmp_ListData);
				} catch (Exception e) {
				}
				if (mListView != null && tListData != null) {
					tListData.clear();
					tListData.addAll(tmp_ListData);
					if (mListAdapter != null) {
						mListAdapter.notifyDataSetChanged();
					}
					if (mbfirstIn) {
						try {
							mPlayPause = false;
							MusicUtils.playAll(AuroraPlayerActivity.this, tListData, 0, 0, tListData.get(0).getIsDownLoadType() == 1);
							mbfirstIn = false;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					updateAuroraTrackInfo();
					mListAdapter = new AuroraPlayerListViewAdapter<AuroraListItem>(AuroraPlayerActivity.this, tListData);
					mListView.setAdapter(mListAdapter);
					mListAdapter.notifyDataSetChanged();
					upDateListItemSelection();
				}
			}
		}

		
	}

	private void setAuroraListInfo() {
		synchronized (this) {
			if (mListView != null && tServiceListData != null) {
				// Log.i(TAG,
				// "----- ----- setAuroraListInfo mbfirstIn:"+mbfirstIn);
				if (mbfirstIn) {
					try {
						mService.setListInfo(tServiceListData);
						mPlayPause = false;
						MusicUtils.playAll(AuroraPlayerActivity.this, tServiceListData, 0, 0, tServiceListData.get(0).getIsDownLoadType() == 1);
						mbfirstIn = false;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (tListData == null) {
					tListData = new ArrayList<AuroraListItem>(tServiceListData);
				} else {
					tListData.clear();
					tListData.addAll(tServiceListData);
					if (isRadioType) {
						if (mAuroraPlayRadioAdapter == null) {
							mAuroraPlayRadioAdapter = new AuroraPlayRadioAdapter(AuroraPlayerActivity.this, Application.getRadioInfoList());
							mAuroraPlayRadioAdapter.setPlayingPosition(Application.mRadioPosition);
							mListView.setAdapter(mAuroraPlayRadioAdapter);
						} else {
							mAuroraPlayRadioAdapter.setPlayingPosition(Application.mRadioPosition);
							mAuroraPlayRadioAdapter.notifyDataSetChanged();
						}
						if (mbSelected) {
							mbSelected = false;
							mListView.setSelection(Application.mRadioPosition);
						}
						return;
					}
					if (mListAdapter != null) {
						mListAdapter.notifyDataSetChanged();
					} else {
						mListAdapter = new AuroraPlayerListViewAdapter<AuroraListItem>(AuroraPlayerActivity.this, tListData);
						mListView.setAdapter(mListAdapter);
					}
				}
				upDateListItemSelection();
			}
		}
		return;
	}

	private RunThreadAsyncTask mTask = null;

	private void reFreshListData() {
		if (mService == null) {
			return;
		}
		updateLoveButton(-1);
		try {
			tServiceListData = (ArrayList) mService.getListInfo();
			if (mListAdapter != null) {
				mListAdapter.notifyDataSetChanged();
			}
		} catch (Exception e) {
			Log.i(TAG, "----- ---- reFreshListData failed");
			// e.printStackTrace();
		}
		if (tServiceListData != null) {
			if (tServiceListData.size() == 0) {
				setPlayShuffleMode();
				if (mTask != null) {
					mTask.cancel(true);
					mTask = null;
				}
				mTask = new RunThreadAsyncTask();
				mTask.executeOnExecutor(executor);
				return;
			} else {
				setPlayShuffleMode();
				setAuroraListInfo();
			}
		} else {
			LogUtil.d(TAG, "----- reFreshListData ----- 2");
			// Log.i(TAG, "----- reFreshListData ----- 2");
			setPlayShuffleMode();
			// new RunThreadAsyncTask().execute();
			if (mTask != null) {
				mTask.cancel(true);
				mTask = null;
			}
			mTask = new RunThreadAsyncTask();
			mTask.executeOnExecutor(executor);
			return;
		}
		return;
	}

	private static class MyListComparator implements Comparator<AuroraListItem> {
		private HashMap<Long, Integer> map = new HashMap<Long, Integer>();

		public MyListComparator(HashMap<Long, Integer> tMap) {
			this.map = tMap;
		}

		@Override
		public int compare(AuroraListItem lhs, AuroraListItem rhs) {
			return map.get(Long.valueOf((long) lhs.getSongId())).compareTo(map.get(Long.valueOf((long) rhs.getSongId())));
		}
	}

	private void initListView(View lView) {
		if (lView == null) {
			return;
		}
		mListView = (ListView) (lView.findViewById(R.id.list_songs));
		mListView.setOnItemClickListener(OnListViewListener);
		mPlaySelect = (ImageView) (lView.findViewById(R.id.player_list_selected));
		// initListViewEdgeEffect();
		// cancelFadingEdge(mListView);
		return;
	}

	/**
	 * 去掉pager的最左/最右蓝色背景
	 */
	private void initViewPagerEdgeEffect() {
		try {
			Field leftEdge = mViewPager.getClass().getDeclaredField("mLeftEdge");
			Field rightEdge = mViewPager.getClass().getDeclaredField("mRightEdge");
			if (leftEdge != null && rightEdge != null) {
				leftEdge.setAccessible(true);
				rightEdge.setAccessible(true);
				mleftCompat = (EdgeEffectCompat) leftEdge.get(mViewPager);
				mrightCompat = (EdgeEffectCompat) rightEdge.get(mViewPager);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		// Log.i(TAG,
		// "----- ----- onWindowFocusChanged --- hasFocus:"+hasFocus);
		/*
		 * if (hasFocus && mOnCreate) { mOnCreate = false; onFirstAnimation(mAnimView); }
		 */
	}

	/**
	 * 查找页面控件
	 */
	protected void findViews() {
		// ------------ common widget start -------------//
		mNote1 = (ImageView) findViewById(R.id.img_note1);
		mNote2 = (ImageView) findViewById(R.id.img_note2);
		Drawable d = this.getResources().getDrawable(R.drawable.aurora_note1);
		mCenterWidth1 = d.getIntrinsicWidth() / 2;
		mCenterHeight1 = d.getIntrinsicHeight() / 2;
		Drawable d2 = this.getResources().getDrawable(R.drawable.aurora_note2);
		mCenterWidth2 = d2.getIntrinsicWidth() / 2;
		mCenterHeight2 = d2.getIntrinsicHeight() / 2;
		mArtistName = (TextView) findViewById(R.id.song_text);
		mAlbumName = (TextView) findViewById(R.id.album_text);
		mProgress = (SeekBar) findViewById(R.id.aurora_progress);
		mProgress.setOnSeekBarChangeListener(mSeekBarListener);
		mProgress.setMax(1000);
		mCurrentTime = (TextView) findViewById(R.id.aurora_currenttime);
		mTotalTime = (TextView) findViewById(R.id.aurora_totaltime);
		mPrevButton = (OTAMainPageFrameLayout) findViewById(R.id.aurora_prev);
		// mPrevButton.setOnClickListener(mAuroraPrevListener);
		// mPrevButton.setRepeatListener(mAuroraRewListener, 260);
		mPrevButton.setOnTouchListener(onButtonListener);
		mPauseButton = (OTAMainPageFrameLayout) findViewById(R.id.aurora_pause);
		mPauseButton.requestFocus();
		// mPauseButton.setOnClickListener(mAuroraPauseListener);
		mPauseButton.setOnTouchListener(onButtonListener);
		mNextButton = (OTAMainPageFrameLayout) findViewById(R.id.aurora_next);
		// mNextButton.setOnClickListener(mAuroraNextListener);
		// mNextButton.setRepeatListener(mAuroraFfwdListener, 260);
		mNextButton.setOnTouchListener(onButtonListener);
		mShuffleButton = (ImageButton) findViewById(R.id.aurora_shuffle);
		mShuffleButton.setOnClickListener(mShuffleListener);
		mLoveButton = (ImageButton) findViewById(R.id.aurora_love);
		mLoveButton.setOnClickListener(mLoveListener);
		mBackButton = (ImageButton) findViewById(R.id.img_bt_back);
		mBackButton.setOnClickListener(mBackListener);
		mShareButton = (ImageButton) findViewById(R.id.img_bt_share);
		mShareButton.setOnClickListener(mShareListener);
		mBaiduLog = (View) findViewById(R.id.player_baidu_log);
		// ------------ common widget end -------------//
		// ------------ player widget start -------------//
		if (mPlayerView != null) {
			mPlayerView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mPlayerControl != null) {
						if (mPlayerControl.getVisibility() == View.VISIBLE) {
							AlphaAnimationOut(mPlayerControl, 150);
							if (mViewPager != null) {
								mViewPager.setViewPageOnScrolled(false);
							}
						} else {
							AlphaAnimationIn(mDotLayout, 150);
							if (mViewPager != null) {
								mViewPager.setViewPageOnScrolled(true);
							}
						}
						updateNotValidPlayerControl();
					}
				}
			});
			mAlbum = (AuroraAnimationImageView) (mPlayerView.findViewById(R.id.image_player_album));
			mPlayerControl = (View) (mPlayerView.findViewById(R.id.image_player_control));
			mPlayerControl.setVisibility(View.GONE);
			mSongLayout = (AuroraImageButton) (mPlayerView.findViewById(R.id.song_player_control));
			mArtistLayout = (AuroraImageButton) (mPlayerView.findViewById(R.id.artist_player_control));
			mRingerLayout = (AuroraImageButton) (mPlayerView.findViewById(R.id.ringer_player_control));
			mDownLoadLayout = (AuroraImageButton) (mPlayerView.findViewById(R.id.download_player_control));
			mSongLayout.setImgResource(R.drawable.aurora_player_song_select);
			mSongLayout.setText(R.string.player_songs);
			mSongLayout.setOnClickListener(mOnClickListener);
			mArtistLayout.setImgResource(R.drawable.aurora_player_artist_select);
			mArtistLayout.setText(R.string.appwidget_artisttitle);
			mArtistLayout.setOnClickListener(mOnClickListener);
			// modify by chenhl start 20140923
			mRingerLayout.setImgResource(R.drawable.aurora_album);
			mRingerLayout.setText(R.string.player_albums);
			mRingerLayout.setOnClickListener(mOnClickListener);
			mSoundLayout = (AuroraImageButton) mPlayerView.findViewById(R.id.sound_player_control);
			mSoundLayout.setImgResource(R.drawable.aurora_sound_control);
			mSoundLayout.setText(R.string.player_sound);
			mSoundLayout.setOnClickListener(mOnClickListener);
			mSoundLayout.setVisibility(View.VISIBLE);// 打开音效按钮 lory 2014.10.10
			// modify by chenhl end 20140923
			if (Globals.SWITCH_FOR_ONLINE_MUSIC) {
				mDownLoadLayout.setImgResource(R.drawable.aurora_player_down_select);
				mDownLoadLayout.setText(R.string.player_downs);
				mDownLoadLayout.setOnClickListener(mOnClickListener);
			} else {
				mDownLoadLayout.setVisibility(View.GONE);
			}
			mAnimView = (ImageView) (mPlayerView.findViewById(R.id.image_player_anim));
			mAnimView.setImageBitmap(mDefautBitmap);
			onFirstAnimation(mAnimView);
		}
		if (mPlayerLrcView != null) {
			mLrcBlur1 = (ImageView) (mPlayerLrcView.findViewById(R.id.lrc_image_blur1));
		}
		// ------------ lrc widget end -------------//
		mSearchLyric = (ImageView) findViewById(R.id.player_search_lyric);
		mSearchLyric.setOnClickListener(mSearchLyricListener);
	}

	public void updateNotValidPlayerControl() {
		if (mPlayerControl != null) {
			if (misMms || mbSmallSize) {// mbUriPath
				if (mArtistLayout != null) {
					mArtistLayout.setEnabled(false);
					mArtistLayout.setAlpha(0.2f);
				}
			} else {
				if (mArtistLayout != null) {
					mArtistLayout.setEnabled(mbNotValid ? false : true);
					mArtistLayout.setAlpha(mbNotValid ? 0.2f : 1f);
				}
			}
			boolean isNotDownLoad = (mItemInfo != null && mItemInfo.getIsDownLoadType() == 1);
			if (isNotDownLoad) {
				if (mSongLayout != null) {
					mSongLayout.setEnabled(isNotDownLoad ? false : true);
					mSongLayout.setAlpha(isNotDownLoad ? 0.2f : 1f);
				}
				// paul add for BUG #14926 start
				if (mRingerLayout != null) {
					String album = null;
					try {
						album = mService.getAlbumName();
					} catch (Exception e) {
					}
					if (TextUtils.isEmpty(album)) {
						mRingerLayout.setEnabled(false);
						mRingerLayout.setAlpha(0.2f);
					}
				}
				// paul add for BUG #14926 end
			} else {
				if (misMms || mbSmallSize) {// mbUriPath
					if (mSongLayout != null) {
						mSongLayout.setEnabled(false);
						mSongLayout.setAlpha(0.2f);
					}
				} else {
					if (mSongLayout != null) {
						mSongLayout.setEnabled(mbNotValid ? false : true);
						mSongLayout.setAlpha(mbNotValid ? 0.2f : 1f);
					}
				}
				if (mRingerLayout != null) {
					mRingerLayout.setEnabled(mbNotValid ? false : true);
					mRingerLayout.setAlpha(mbNotValid ? 0.2f : 1f);
				}
			}
		}
		return;
	}

	private void updateNotValidImageButton() {
		String tfilename = "";
		if (mbUriPath) {
			mbNotValid = false;// true;
		} else {
			try {
				tfilename = mService.getFilePath();
				if (!TextUtils.isEmpty(tfilename)) {
					if ((tfilename.equalsIgnoreCase("default net") || tfilename.toLowerCase().startsWith("http://"))) {
						mbNotValid = false;
					} else {
						File f1 = new File(tfilename);
						if (!f1.exists()) {
							mbNotValid = true;
							return;
						} else {
							mbNotValid = (f1.length() < Globals.FILE_SIZE_FILTER) ? true : false;
						}
					}
				}else {
					mbNotValid = true;
				}
			} catch (Exception e) {
				mbNotValid = true;
				e.printStackTrace();
			}
		}
		mFromUri = false;
		if (mbNotValid) {
			if (mLoveButton != null) {
				mLoveButton.setAlpha(0.2f);
				mLoveButton.setEnabled(false);
			}
			if (mFromUri) {
				if (mShuffleButton != null) {
					mShuffleButton.setAlpha(0.2f);
					mShuffleButton.setEnabled(false);
				}
				if (mNextButton != null) {
					mNextButton.setAlpha(0.2f);
					mNextButton.setEnabled(false);
				}
				if (mPrevButton != null) {
					mPrevButton.setAlpha(0.2f);
					mPrevButton.setEnabled(false);
				}
			}
		} else {
			if (misMms || mbSmallSize) {
				mLoveButton.setAlpha(0.2f);
				mLoveButton.setEnabled(false);
			} else {
				mLoveButton.setAlpha(1f);
				mLoveButton.setEnabled(true);
			}
			if (mFromUri) {
				mShuffleButton.setAlpha(1f);
				mShuffleButton.setEnabled(true);
				mNextButton.setAlpha(1f);
				mNextButton.setEnabled(true);
				mPrevButton.setAlpha(1f);
				mPrevButton.setEnabled(true);
			}
		}
		// add for radio start
		if (isRadioType) {
			if (mShuffleButton != null) {
				mShuffleButton.setAlpha(0.2f);
				mShuffleButton.setEnabled(false);
			}
			if (mPrevButton != null) {
				mPrevButton.setAlpha(0.2f);
				mPrevButton.setEnabled(false);
			}
		} else {
			if (mShuffleButton != null) {
				mShuffleButton.setAlpha(1f);
				mShuffleButton.setEnabled(true);
			}
			if (mPrevButton != null) {
				mPrevButton.setAlpha(1f);
				mPrevButton.setEnabled(true);
			}
		}
		// add for radio end 20150421
		return;
	}

	@Override
	protected void onStart() {
		super.onStart();
		// LogUtil.d("", "----- ---- AuroraPlayerActivity onStart");
		mbRepeat = false;
		paused = false;
		mToken = MusicUtils.bindToService(this, AuroraOsc);
		if (mToken == null) {
			mHandler.sendEmptyMessage(AURORA_QUIT);
		}
		IntentFilter f = new IntentFilter();
		f.addAction(MediaPlaybackService.PLAYSTATE_CHANGED);
		f.addAction(MediaPlaybackService.META_CHANGED);
		f.addAction(MediaPlaybackService.QUEUE_CHANGED);
		// f.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		// registerReceiver(mStatusListener, new IntentFilter(f));
		registerReceiver(mStatusListener, f);
		// updateAuroraTrackInfo();
		long next = refreshAuroraNow();
		queueAuroraNextRefresh(next);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mAnimationStop = false;
		mPlayPause = false;// BUG #16411 add by JXH
		if (mSeekBarWidth == 0) {
			mSeekBarstart = mProgress.getPaddingLeft() + mProgress.getThumbOffset();
			mSeekBarWidth = getWindowManager().getDefaultDisplay().getWidth() - mSeekBarstart - mProgress.getPaddingRight();
			mStarth = (float) mSeekBarWidth / 1000;
		}
		if (mAnimationModel1 == null) {
			mAnimationModel1 = AuroraAnimationModel.createAnimation(mNote1);
			mAnimationModel1.setFillAfter(true);
			mAnimationModel1.setMyAnimationListener(this);
			mAnimationModel1.setDuration(3000);
			mAnimationModel1.setInterpolator(new AccelerateInterpolator());
		}
		if (mAnimationModel2 == null) {
			mAnimationModel2 = AuroraAnimationModel.createAnimation(mNote2);
			mAnimationModel2.setFillAfter(true);
			mAnimationModel2.setMyAnimationListener(this);
			mAnimationModel2.setDuration(3000);
			mAnimationModel2.setInterpolator(new AccelerateInterpolator());
		}
		if (mPlayerControl != null && mPlayerControl.getVisibility() == View.VISIBLE) {
			mPlayerControl.clearAnimation();
			mPlayerControl.setVisibility(View.GONE);
			AuroraMusicUtil.AlphaAnimationIn(mDotLayout, 100);
		}
		if (mLyricView != null) {
			mLyricView.setIsTouchScrollView(false);
		}
		if (mViewPager != null) {
			mViewPager.setViewPageOnScrolled(false);
		}
		initXiamiRadio();
		setPauseButtonImage();
		updateAuroraTrackInfo();
		onStopPlayAnimation(false);
		mWxShare = AuroraWxShare.getInstance(this);
		mWxShare.setHandleIntentAndCallBack(getIntent(), null);
		if (Build.VERSION.SDK_INT > 19 && Globals.SWITCH_FOR_TRANSPARENT_STATUS_BAR) {
			AuroraMusicUtil.changeBarColor(AuroraPlayerActivity.this, false);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		onPaused = true;
		mOnCreate = false;
		if (misMms && mService != null) {
			try {
				mService.pause();
			} catch (Exception e) {
			}
		}
		onStopPlayAnimation(true);
		if (mAlbum != null) {
			mAlbum.setStartAnimation(false);
		}
		DownloadManager.getInstance(this).clearListenerMap();
		clearData();
	}

	@Override
	protected void onStop() {
		paused = true;
		mAlbumArtHandler.removeCallbacksAndMessages(null);
		mHandler.removeCallbacksAndMessages(null);
		try {
			unregisterReceiver(mStatusListener);
		} catch (Exception e) {
		}
		MusicUtils.unbindFromService(mToken);
		mService = null;
		AuroraAnimationModel.clear();
		super.onStop();
	}

	private void clearData() {
		AuroraWxShare.unRegisterApp();
		mWxShare = null;
		return;
	}

	@Override
	protected void onDestroy() {
		// LogUtil.getInstance(this).stop();
		LogUtil.d(TAG, "----- ---- AuroraPlayerActivity onDestroy");
		mPlayUri = null;
		mbSelected = false;
		mFromMain = false;
		mbNotValid = false;
		isdestory = true;
		// clearData();
		AuroraDialogFragment.registerItemClickCallback(null);
		AuroraShareXLWb.getInstance(this).clearData();
		if (mPauseButton != null) {
			mPauseButton.stopAnim();
		}
		if (mLoveButton != null) {
			mLoveButton.clearAnimation();
		}
		if (mAnimView != null) {
			mAnimView.clearAnimation();
		}
		if (mNote1 != null) {
			mNote1.clearAnimation();
		}
		if (mNote2 != null) {
			mNote2.clearAnimation();
		}
		if (mAlbum != null) {
			mAlbum.clearAnimation();
		}
		if (mAlbumArtWorker != null) {
			mAlbumArtWorker.quit();
		}
		if (mAnimationBitmap != null && !mAnimationBitmap.isRecycled()) {
			mAnimationBitmap.recycle();
		}
		if (mDefaultBg != null && !mDefaultBg.isRecycled()) {
			mDefaultBg.recycle();
			mDefaultBg = null;
		}
		if (mDefautBitmap != null && !mDefautBitmap.isRecycled()) {
			mDefautBitmap.recycle();
			mDefautBitmap = null;
		}
		if (mTask != null && !mTask.isCancelled()) {
			// Log.i(TAG, "----- ----- onDestroy mTask --");
			mTask.cancel(true);
			mTask = null;
		}
		super.onDestroy();
	}

	static final String[] ARTIST_CLOS = { MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.ARTIST,
			"COUNT(*) AS num_aurora" };
	private static final String BUCKET_GROUP_BY_IN_TABLE = "2) GROUP BY (2";

	private void startArtistActivity() {
		if (mService == null || mbRepeat) {
			return;
		}
		// add by tangjie 2014/07/18 start
		if (mItemInfo != null && mItemInfo.getIsDownLoadType() == 1) {
			if (!AuroraMusicUtil.isNetWorkActive(AuroraPlayerActivity.this)) {
				showToast(R.string.aurora_network_error);
				return;
			}
			long artist_id = -1;
			try {
				artist_id = mService.getArtistId();
			} catch (Exception e) {
				// TODO: handle exception
			}
			Intent intent = new Intent(this, AuroraNetSearchActivity.class);
			intent.putExtra("title", mItemInfo.getArtistName());
			if (artist_id != -1)
				intent.putExtra("artist_id", artist_id);
			else
				intent.putExtra("artist_id", mItemInfo.getArtistId());
			intent.putExtra("song_count", -1);
			intent.putExtra("album_count", -1);
			startActivity(intent);
			// add by tangjie end
		} else {
			Cursor tCursor = null;
			try {
				if (mbUriPath) {
					long audioId = mService.getAudioId();
					if (audioId == -1) {
						showToast(R.string.player_failed);
						return;
					}
				}
				long audioID = mService.getArtistId();
				mbRepeat = true;
				StringBuilder where = new StringBuilder();
				where.append(MediaStore.Audio.Media.ARTIST_ID + "=" + Long.valueOf(audioID).toString());
				where.append(" AND " + Globals.QUERY_SONG_FILTER);
				where.append(" AND " + BUCKET_GROUP_BY_IN_TABLE);
				tCursor = MusicUtils.query(AuroraPlayerActivity.this, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ARTIST_CLOS, where.toString(),
						null, null);
				if (tCursor != null) {
					int trackCont = tCursor.getCount();
					int albumCont = 0;
					String albumname = "";
					tCursor.moveToFirst();
					if (!tCursor.isAfterLast()) {
						albumname = tCursor.getString(2);
						do {
							long artist_id = tCursor.getLong(0);
							long album_id = tCursor.getLong(1);
							String name = tCursor.getString(2);
							int num = tCursor.getInt(3);
							albumCont += num;
						} while (tCursor.moveToNext());
						Intent tIntent = IntentFactory.newAlbumListIntent(AuroraPlayerActivity.this, String.valueOf(audioID), albumname, trackCont,
								albumCont);
						startActivity(tIntent);
					}
				} else {
					// showToast(R.string.player_nofile);
					showToast(R.string.player_failed);
				}
			} catch (Exception e) {
				e.printStackTrace();
				showToast(R.string.player_failed);
				// showToast(R.string.player_filefailed);
			} finally {
				if (tCursor != null) {
					tCursor.close();
					tCursor = null;
					mbRepeat = false;
				}
			}
		}
		mbRepeat = false;
		return;
	}

	private void quitFullScreen() {
		final WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setAttributes(attrs);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
	}

	private void enterFullScreen() {
		WindowManager.LayoutParams params = getWindow().getAttributes();
		params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
		getWindow().setAttributes(params);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		return;
	}

	private boolean isDownExist(long songid) {
		return DownloadManager.getInstance(getApplicationContext()).isFinished(songid);
	}

	private void startOnlineMusicDownLoad() {
		if (mItemInfo == null || mService == null) {
			return;
		}
		if (DownloadManager.getInstance(this).isFinished(mItemInfo.getSongId())) {
			showToast(R.string.player_download_exist);
			if (Globals.SWITCH_FOR_ONLINE_MUSIC) {
				mDownLoadLayout.setEnabled(false);
				mDownLoadLayout.setAlpha(0.2f);
			}
			return;
		}
		try {
			DownloadManager.getInstance(this).addNewDownload(mItemInfo.getSongId(), mItemInfo.getTitle(), mItemInfo.getArtistName(),
					mItemInfo.getAlbumName(), mItemInfo.getAlbumImgUri(), null, mItemInfo.getLrcUri());
			DownloadManager.getInstance(this).setDownloadListener(mItemInfo.getSongId(), mDownloadListener);
			showToast(R.string.player_download_start);
			if (Globals.SWITCH_FOR_ONLINE_MUSIC) {
				mDownLoadLayout.setEnabled(false);
				mDownLoadLayout.setAlpha(0.2f);
			}
		} catch (Exception e) {
			LogUtil.e(TAG, "startOnlineMusicDownLoad error", e);
		}
		return;
	}

	private final DownloadStatusListener mDownloadListener = new DownloadStatusListener() {
		@Override
		public void onDownload(String url, long id, int status, long downloadSize, long fileSize) {
			DownloadTask.State state = DownloadTask.State.valueOf(status);
			if (state == DownloadTask.State.SUCCESS) {
				if (Globals.SWITCH_FOR_ONLINE_MUSIC) {
					mDownLoadLayout.setEnabled(false);
					mDownLoadLayout.setAlpha(0.2f);
				}
			} else if (state == DownloadTask.State.FAILURE) {
				showToast(R.string.player_download_failed);
			}
		}
	};
	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			// add by chenhl start 2010924
			// 音效
			case R.id.sound_player_control:
				try {
					Intent intent = new Intent("com.android.auroramusic.AuroraSoundControl");
					intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, mService.getAudioSessionId());
					startActivity(intent);
					// test MusicFX
					if (Globals.mTestMode) {
						Intent intent2 = new Intent("android.media.action.DISPLAY_AUDIO_EFFECT_CONTROL_PANEL");
						intent2.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, mService.getAudioSessionId());
						startActivityForResult(intent2, REQUESTCODE);
					}
				} catch (Exception e) {
					LogUtil.e(TAG, "sound_player_control error", e);
				}
				if (mPlayerControl != null && mPlayerControl.getVisibility() == View.VISIBLE) {
					mPlayerControl.clearAnimation();
					mPlayerControl.setVisibility(View.GONE);
				}
				AuroraMusicUtil.AlphaAnimationIn(mDotLayout, 100);
				ReportUtils.getInstance(getApplicationContext()).reportMessage(ReportUtils.TAG_BTN_SOUND);
				break;
			// add by chenhl end 2010924
			// 下载
			case R.id.download_player_control:
				if (!AuroraMusicUtil.isNetWorkActive(AuroraPlayerActivity.this)) {
					showToast(R.string.aurora_network_error);
					return;
				}
				if (!FlowTips.showDownloadFlowTips(AuroraPlayerActivity.this, new OndialogClickListener() {
					@Override
					public void OndialogClick() {
						startOnlineMusicDownLoad();
					}
				})) {
					startOnlineMusicDownLoad();
				}
				// startOnlineImgDownLoad(download);
				if (mPlayerControl != null && mPlayerControl.getVisibility() == View.VISIBLE) {
					mPlayerControl.clearAnimation();
					mPlayerControl.setVisibility(View.GONE);
				}
				AuroraMusicUtil.AlphaAnimationIn(mDotLayout, 100);
				ReportUtils.getInstance(getApplicationContext()).reportMessage(ReportUtils.TAG_BTN_DOWNLOAD);
				break;
			// 添加到歌单
			case R.id.song_player_control:
				List<String> list = new ArrayList<String>();
				long tid = MusicUtils.getCurrentAudioId();
				if (tid == -1) {
					showToast(R.string.player_failed);
					return;
				}
				list.add(String.valueOf(tid));
				DialogUtil.showAddDialog(AuroraPlayerActivity.this, list, null);
				if (mPlayerControl != null && mPlayerControl.getVisibility() == View.VISIBLE) {
					mPlayerControl.clearAnimation();
					mPlayerControl.setVisibility(View.GONE);
				}
				AuroraMusicUtil.AlphaAnimationIn(mDotLayout, 100);
				ReportUtils.getInstance(getApplicationContext()).reportMessage(ReportUtils.TAG_BTN_PL);
				break;
			// 歌手
			case R.id.artist_player_control:
				startArtistActivity();
				ReportUtils.getInstance(getApplicationContext()).reportMessage(ReportUtils.TAG_BTN_ARTISIT);
				break;
			// 专辑
			case R.id.ringer_player_control:
				if (mItemInfo != null) {
					try {
						// -----modify by tangjie 2015/01/06-----//
						MusicUtils.goAlbumActivity(AuroraPlayerActivity.this, mService.getArtistId(), mService.getArtistName(),
								mService.getAlbumId(), mService.getAlbumName(), mItemInfo.getIsDownLoadType(),
								ImageUtil.transferImgUrl(mItemInfo.getAlbumImgUri(), 330));
					} catch (Exception e) {
						LogUtil.e(TAG, "ringer_player_control error", e);
					}
				}
				ReportUtils.getInstance(getApplicationContext()).reportMessage(ReportUtils.TAG_BTN_ALBUM);
				break;
			default:
				break;
			}
			if (mViewPager != null) {
				mViewPager.setViewPageOnScrolled(false);
			}
		}
	};

	private void queueAuroraNextRefresh(long delay) {
		if (!paused) {
			Message msg = mHandler.obtainMessage(AURORA_REFRESH);
			mHandler.removeMessages(AURORA_REFRESH);
			mHandler.sendMessageDelayed(msg, delay);
		}
	}

	private void restartNoteAnima(boolean flag) {
		mAnimationStop = !flag;
		if (flag) {
			LogUtil.d(TAG, "restartNoteAnima");
			ani1 = false;// add by chenhl
			ani2 = false;
			mNote1.startAnimation(mAnimationModel1);
		} else {
			mNote1.setVisibility(View.GONE);
			mNote2.setVisibility(View.GONE);
		}
		return;
	}

	private boolean onPauseTouch(View v, MotionEvent event) {
		if (mService == null) {
			return false;
		}
		boolean pause = false;
		try {
			if (mService.isPlaying()) {
				pause = true;
			}
		} catch (Exception e) {
			LogUtil.e(TAG, "---onPauseTouch error", e);
			return false;
		}
		int action = event.getAction();
		LogUtil.d(TAG, "----- ----- onPauseTouch 3 pause:" + pause + ",action:" + action);
		if (action == MotionEvent.ACTION_UP) {
			doPauseResume();
		} else if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_CANCEL) {
			if (pause) {
				((OTAMainPageFrameLayout) v).setBackgroundResource(R.drawable.aurora_play_select);
			} else {
				((OTAMainPageFrameLayout) v).setBackgroundResource(R.drawable.aurora_pause_select);// aurora_pause_select
			}
		}
		return true;
	}

	private void onNextButtonAnimation(final OTAMainPageFrameLayout view, final boolean isNextButton) {
		if (view == null) {
			return;
		}
		mPauseButton.stopAnim();
		mNextButton.stopAnim();
		mPrevButton.stopAnim();
		view.setAnimationListener(new OTAFrameAnimation.AnimationImageListener() {
			@Override
			public void onRepeat(int repeatIndex) {}

			@Override
			public void onFrameChange(int repeatIndex, int frameIndex, int currentTime) {}

			@Override
			public void onAnimationStart() {}

			@Override
			public void onAnimationEnd() {
				if (isNextButton) {
					view.setBackgroundResource(R.drawable.aurora_next);
					nextClicked();
				} else {
					view.setBackgroundResource(R.drawable.aurora_prev);
					prevClicked();
				}
			}
		});
		if (isNextButton) {
			view.setFrameAnimationList(R.drawable.next_play_anima);
		} else {
			view.setFrameAnimationList(R.drawable.prev_play_anima);
		}
		view.startAnim();
		return;
	}

	private boolean onNextTouch(View v, MotionEvent event) {
		int action = event.getAction();
		// Log.i(TAG,
		// "----- ----- onNextTouch 1 pause:"+pause+",action:"+action);
		if (action == MotionEvent.ACTION_UP) {
			onNextButtonAnimation(mNextButton, true);
		} else if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_CANCEL) {
			((OTAMainPageFrameLayout) v).setBackgroundResource(R.drawable.aurora_next_select);
		}
		return true;
	}

	private boolean onPrevTouch(View v, MotionEvent event) {
		int action = event.getAction();
		if (action == MotionEvent.ACTION_UP) {
			onNextButtonAnimation(mPrevButton, false);
		} else if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_CANCEL) {
			((OTAMainPageFrameLayout) v).setBackgroundResource(R.drawable.aurora_prev_select);
		}
		return true;
	}

	private final View.OnTouchListener onButtonListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(final View v, final MotionEvent event) {
			if (mService == null) {
				return false;
			}
			boolean isAction = false;
			try {
				if (mService.isOnlineSong()&&FlowTips.showPlayFlowTips(AuroraPlayerActivity.this, new OndialogClickListener() {
					@Override
					public void OndialogClick() {
						if (mPauseButton == v) {
							onPauseTouch(v, event);
						} else if (mNextButton == v) {
							onNextTouch(v, event);
						} else if (mPrevButton == v) {
							onPrevTouch(v, event);
						}
					}
				})) {
					return true;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (mPauseButton == v) {
				return onPauseTouch(v, event);
			} else if (mNextButton == v) {
				return onNextTouch(v, event);
			} else if (mPrevButton == v) {
				return onPrevTouch(v, event);
			}
			return false;
		}
	};

	private void setPauseButtonImage() {
		try {
			boolean flag = false;
			LogUtil.d(TAG, "---mService:" + mService + " " + (mService != null ? mService.isPlaying() : "null") + "---mPauseButton:" + mPauseButton);
			if (mService != null && mService.isPlaying() && mPauseButton != null) {
				flag = true;
				// mPauseButton.setImageResource(R.drawable.aurora_play);//aurora_play_status
				LogUtil.i(TAG, "----- ----- setPauseButtonImage 1 mPlayPause:" + mPlayPause);
				if (mPlayPause) {
					onRotationAnimation(mPauseButton, flag);
				} else {
					mPauseButton.stopAnim();
					mPauseButton.setBackgroundResource(R.drawable.aurora_play);
				}
			} else {
				LogUtil.i(TAG, "----- ----- setPauseButtonImage 2 mPlayPause:" + mPlayPause);
				if (mPlayPause) {
					onRotationAnimation(mPauseButton, flag);
				} else {
					mPauseButton.stopAnim();
					mPauseButton.setBackgroundResource(R.drawable.aurora_pause);
				}
			}
			// Log.i(TAG, "----- ----- setPauseButtonImage 3 flag:"+flag);
			if (mAlbum != null) {
				mAlbum.setStartAnimation(flag);
			}
			restartNoteAnima(flag);
		} catch (Exception ex) {
			LogUtil.e(TAG, "-----setPauseButtonImage error", ex);
		}
	}

	private long refreshAuroraNow() {
		if (mService == null)
			return 500;
		if (mFromTouch) {
			LogUtil.d(TAG, "---seeking");
			return 500;
		}
		try {
			mDuration = mService.duration();
			if (mDuration == -1) {
				// is onPrepared
				return 500;
			}
			mTotalTime.setText(MusicUtils.makeTimeString(this, mDuration / 1000));
			long pos = mPosOverride < 0 ? mService.position() : mPosOverride;
			if ((pos >= 0) && (mDuration > 0)) {
				mCurrentTime.setText(MusicUtils.makeTimeString(this, pos / 1000));
				int progress = (int) (1000 * pos / mDuration);
				mProgress.setProgress(progress);
				if (mItemInfo != null && mItemInfo.getIsDownLoadType() == 1) {
					mProgress.setSecondaryProgress((int) mService.secondaryPosition() * 10);
				}
				if (mLyricView != null) {
					Message msg = mHandler.obtainMessage(AURORA_REFRESH_LRC);
					mHandler.sendMessage(msg);
				}
				mDistance = progress * mStarth;
			} else {
				mCurrentTime.setText("00:00");
				mProgress.setProgress(0);
			}
			// calculate the number of milliseconds until the next full second,
			// so
			// the counter can be updated at just the right time
			long remaining = 1000 - (pos % 1000);
			// approximate how often we would need to refresh the slider to
			// move it smoothly
			int width = mProgress.getWidth();
			if (width == 0)
				width = 320;
			long smoothrefreshtime = mDuration / width;
			if (smoothrefreshtime > remaining)
				return remaining;
			if (smoothrefreshtime < 20)
				return 20;
			return smoothrefreshtime;
		} catch (Exception e) {
			Log.i(TAG, "----- ---- refreshAuroraNow fail ----");
			e.printStackTrace();
		}
		return 500;
	}

	/**
	 * 立即刷新歌词
	 */
	private void refreshAuroraLrcNow() {
		if (mLyricView == null) {
			return;
		}
		mLyricView.setRefreshNow(true);
		refreshAuroraLrc(true);
	}

	/**
	 * 用于刷新歌词
	 */
	private int refreshAuroraLrc(boolean isFrist) {
		if (mViewPager.getCurrentItem() != 2 && !isFrist) {
			return 500;
		}
		try {
			if (mService == null) {
				return 500;
			}
			mDuration = mService.duration();
			if (mDuration == -1 || mDuration == 0) {
				// is onPrepared
				return 500;
			}
			long pos = mPosOverride < 0 ? mService.position() : mPosOverride;
			if (mLyricView != null) {
				return mLyricView.setCurrentIndex((int) pos);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogUtil.e(TAG, "---refreshAuroraLrc error", e);
		}
		return 500;
	}

	private void queueAuroraNextRefreshLrc(long nextLrc) {
		if (!paused) {
			Message msg = mHandler.obtainMessage(AURORA_REFRESH_LRC);
			mHandler.removeMessages(AURORA_REFRESH_LRC);
			mHandler.sendMessageDelayed(msg, nextLrc);
		}
	}

	private void updateAuroraTrackInfo() {
		if (mService == null) {
			return;
		}
		try {
			if (mBNet && tListData != null && tListData.size() > 0) {
				long songid1 = mService.getAudioId();
				if (songid1 >= 0) {
					AuroraListItem item = null;
					int len = tListData.size();
					for (int i = 0; i < len; i++) {
						AuroraListItem tmpitem = tListData.get(i);
						if (songid1 == tmpitem.getSongId()) {
							item = tmpitem;
							LogUtil.d(TAG, "------lrc:" + item.getLrcUri());
							break;
						}
					}
					String artistName1 = "";
					String albumName1 = "";
					long albumid1 = mService.getAlbumId();
					if (item == null) {
						String tmp = getString(R.string.unknown_artist_name);
						mArtistName.setText(tmp);
						artistName1 = getString(R.string.unknown_artist_name);
						albumName1 = getString(R.string.unknown_album_name);
						albumid1 = -1;
						mbNetSong = 0;
						mTitleNameStr = tmp;
					} else {
						mArtistName.setText(item.getTitle());
						artistName1 = item.getArtistName();
						if (artistName1 == null || artistName1.equals(MediaStore.UNKNOWN_STRING)) {
							artistName1 = getString(R.string.unknown_artist_name);
						}
						albumName1 = item.getAlbumName();
						albumName1 = AuroraMusicUtil.doAlbumName(item.getFilePath(), albumName1);
						if (albumName1 == null || albumName1.equals(MediaStore.UNKNOWN_STRING)) {
							albumName1 = getString(R.string.unknown_album_name);
						}
						mbNetSong = item.getIsDownLoadType();
						mTitleNameStr = item.getTitle();
					}
					mArtistNameStr = artistName1;
					mItemInfo = item;
					LogUtil.d(TAG, "-----1---mItemInfo:" + mItemInfo.getSongId());
					StringBuffer tBuffer = new StringBuffer();
					tBuffer.append(artistName1).append("·").append(albumName1);
					mAlbumName.setText(tBuffer.toString());
					LogUtil.d(TAG, "aaa  mItemInfo---set----songid1:" + songid1 + " albumid1:" + albumid1 + " mbNetSong:" + mbNetSong);
					if (!mbFirstAnim) {
						mAlbumArtHandler.removeMessages(AURORA_GET_ALBUM_ART);
						mAlbumArtHandler.obtainMessage(AURORA_GET_ALBUM_ART, new AuroraAlbumSongIdWrapper(albumid1, songid1)).sendToTarget();
					} else {
						if (mbNetSong == 1) {
							firstAid = songid1;
							firstSid = albumid1;
						} else {
							firstAid = albumid1;
							firstSid = songid1;
						}
					}
					mAlbum.setVisibility(View.VISIBLE);
					mDuration = mService.duration();
					mTotalTime.setText(MusicUtils.makeTimeString(this, mDuration / 1000));
				} else {
					try {
						String trackName3 = mService.getTrackName();
						String artistName3 = mService.getArtistName();
						if (MediaStore.UNKNOWN_STRING.equals(artistName) || artistName == null) {
							artistName3 = getString(R.string.unknown_artist_name);
						}
						String albumName3 = mService.getAlbumName();
						if (albumName3 == null) {
							albumName3 = getString(R.string.unknown_album_name);
						}
						StringBuffer tBuffer = new StringBuffer();
						tBuffer.append(artistName3).append("·").append(albumName3);
						mAlbumName.setText(tBuffer.toString());
						mArtistName.setText(artistName3);
					} catch (Exception e) {
					}
				}
				if (mbNetSong == 1 && mPagePos != 0) {
					mBaiduLog.setVisibility(View.VISIBLE);
				} else {
					mBaiduLog.setVisibility(View.GONE);
				}
				if (mbNetSong == 1) {
					mBtnDown = isDownExist(songid1);
				} else {
					mBtnDown = true;
				}
			} else {
				String path = mService.getPath();
				if (path == null && mService.getFilePath() == null) {
					mBtnDown = true;
					updateWidgets(mBtnDown, false);
					return;
				}
				long songid = mService.getAudioId();
				if (songid < 0 && path != null && path.toLowerCase().startsWith("http://")) {
				} else {
					String trackName = mService.getTrackName();
					if (trackName == null) {
						if (mbUriPath && path != null) {
							if (!misMms) {
								int idx_start = path.lastIndexOf('/');
								int idx_end = path.lastIndexOf('.');
								mArtistName.setText(path.substring(idx_start + 1, idx_end));
							} else {
								mArtistName.setText(path);
							}
						} else {
							mArtistName.setText(getString(R.string.unknown_artist_name));
						}
					} else {
						if (misMms) {
							mArtistName.setText(path);
						} else {
							mArtistName.setText(trackName);
						}
					}
					String artistName = mService.getArtistName();
					if (MediaStore.UNKNOWN_STRING.equals(artistName) || artistName == null) {
						artistName = getString(R.string.unknown_artist_name);
					}
					String albumName = mService.getAlbumName();
					long albumid = mService.getAlbumId();
					if (MediaStore.UNKNOWN_STRING.equals(albumName) || albumName == null) {
						albumName = getString(R.string.unknown_album_name);
						albumid = -1;
					}
					mbNetSong = 0;
					StringBuffer tBuffer = new StringBuffer();
					tBuffer.append(artistName);
					tBuffer.append("·");
					tBuffer.append(albumName);
					mAlbumName.setText(tBuffer.toString());
					if (!mbFirstAnim) {
						mAlbumArtHandler.removeMessages(AURORA_GET_ALBUM_ART);
						mAlbumArtHandler.obtainMessage(AURORA_GET_ALBUM_ART, new AuroraAlbumSongIdWrapper(albumid, songid)).sendToTarget();
					} else {
						firstAid = albumid;
						firstSid = songid;
					}
					LogUtil.d(TAG, "aaa  11----albumid:" + albumid + " songid:" + songid + " mbNetSong:" + mbNetSong);
					mAlbum.setVisibility(View.VISIBLE);
					mDuration = mService.duration();
					mTotalTime.setText(MusicUtils.makeTimeString(this, mDuration / 1000));
					if (mbNetSong == 1 && mPagePos != 0) {
						mBaiduLog.setVisibility(View.VISIBLE);
					} else {
						mBaiduLog.setVisibility(View.GONE);
					}
				}
				mBtnDown = true;
			}
			updateWidgets(mBtnDown, true);
			if (mbNetSong == 0 && mPagePos != 0 && Globals.SHOW_SEARCH_LRC) {
				mSearchLyric.setVisibility(View.VISIBLE);
			} else {
				mSearchLyric.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			LogUtil.e(TAG, "----updateAuroraTrackInfo error", e);
			showToast(R.string.player_failed);
		}
		return;
	}

	private void updateWidgets(boolean bshow, boolean beffect) {
		if (Globals.SWITCH_FOR_ONLINE_MUSIC) {
			if (mItemInfo != null && DownloadManager.getInstance(this).isAddDownload(mItemInfo.getSongId())) {
				mDownLoadLayout.setEnabled(false);
				mDownLoadLayout.setAlpha(0.2f);
			} else {
				mDownLoadLayout.setEnabled(bshow ? false : true);
				mDownLoadLayout.setAlpha(bshow ? 0.2f : 1f);
			}
		}
		if (!Globals.SWITCH_FOR_SOUND_CONTROL) {
			if (!beffect) {
				mSoundLayout.setEnabled(false);
				mSoundLayout.setAlpha(0.2f);
			} else {
				try {
					if (mService.isOnlineSong()) {
						mSoundLayout.setEnabled(false);
						mSoundLayout.setAlpha(0.2f);
					} else {
						mSoundLayout.setEnabled(true);
						mSoundLayout.setAlpha(1f);
					}
				} catch (Exception e) {
				}
			}
		}
		return;
	}

	private ServiceConnection AuroraOsc = new ServiceConnection() {
		public void onServiceConnected(ComponentName classname, IBinder obj) {
			mService = IMediaPlaybackService.Stub.asInterface(obj);
			startPlayback();
			try {
				// Assume something is playing when the service says it is,
				// but also if the audio ID is valid but the service is paused.
				if (mService.getAudioId() >= 0 || mService.isPlaying() || mService.getPath() != null) {
					if (!mService.isPlaying()) {
						/*
						 * int bfirst = MusicUtils.getIntPref(AuroraPlayerActivity.this, MusicUtils.AURORA_FITST_ENTNER, 0); if (bfirst == 0) {
						 * mService.setShuffleMode(MediaPlaybackService.SHUFFLE_NONE); mService.setRepeatMode(MediaPlaybackService.REPEAT_ALL);
						 * doStartPlay(); MusicUtils.setIntPref(AuroraPlayerActivity.this, MusicUtils.AURORA_FITST_ENTNER, 1); } else
						 */if (mFromMain) {
							setPlayShuffleMode();
							doStartPlay();
						}
						mFromMain = false;
					} else {
						setPauseButtonImage();
					}
					return;
				}
			} catch (RemoteException ex) {
				LogUtil.d(TAG, "-----ServiceConnection error ", ex);
			} finally {
				mFromMain = false;
				onPaused = false;
				updateNotValidImageButton();
			}
			return;
		}

		public void onServiceDisconnected(ComponentName classname) {
			mService = null;
		}
	};

	private void onSeekBarAnimation() {
		LogUtil.d(TAG, "onSeekBarAnimation()");
		mAnimationModel1.reset();
		mAnimationModel2.reset();
		ani1 = false;
		ani2 = false;
		distance1 = mDistance;
		degree1 = 0;
		hdistance1 = 0;
		distance2 = mDistance;
		degree2 = 0;
		hdistance2 = 0;
		mNote1.startAnimation(mAnimationModel1);
		return;
	}

	private void onStopPlayAnimation(boolean stop) {
		if (mbNetSong == 1 && mPagePos != 0) {
			mBaiduLog.setVisibility(View.VISIBLE);
		} else {
			mBaiduLog.setVisibility(View.GONE);
		}
		boolean bplaying = false;
		try {
			if (mService != null) {
				bplaying = mService.isPlaying();
			}
		} catch (Exception e) {
		}
		if (!bplaying) {
			return;
		}
		if (mAnimationStop == stop) {
			return;
		}
		mAnimationStop = stop;
		if (stop) {
			mHandler.removeMessages(AURORA_ANIMATION_STASRT);
			mNote1.setVisibility(View.GONE);
			mNote2.setVisibility(View.GONE);
			mNote1.clearAnimation();
			mNote2.clearAnimation();
		} else {
			mHandler.removeMessages(AURORA_ANIMATION_STASRT);
			mHandler.sendEmptyMessageDelayed(AURORA_ANIMATION_STASRT, 500);
		}
		return;
	}

	private OnSeekBarChangeListener mSeekBarListener = new OnSeekBarChangeListener() {
		public void onStartTrackingTouch(SeekBar bar) {
			mLastSeekEventTime = 0;
			mFromTouch = true;
			onStopPlayAnimation(true);
		}

		public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
			if (!fromuser || (mService == null))
				return;
			mHandler.removeMessages(AURORA_SEEK);
			Message message = mHandler.obtainMessage(AURORA_SEEK, progress);
			mHandler.sendMessageDelayed(message, 250);
		}

		public void onStopTrackingTouch(SeekBar bar) {
			mHandler.removeMessages(AURORA_STOPTRACKINGTOUCH);
			mHandler.sendEmptyMessageDelayed(AURORA_STOPTRACKINGTOUCH, 250);
		}
	};

	private void seek(int progress) {
		mPosOverride = mDuration * progress / 1000;
		LogUtil.d(TAG, "-----mPosOverride:" + mPosOverride + " mDuration:" + mDuration + " progress:" + progress);
		try {
			mService.seek(mPosOverride);
		} catch (RemoteException ex) {
		}
		// trackball event, allow progress updates
		if (!mFromTouch) {
			refreshAuroraNow();
			mPosOverride = -1;
			onStopPlayAnimation(false);
		}
	}

	private void prevClicked() {
		if (mService == null)
			return;
		try {
			mFirstAnim = true;
			mPlayPause = false;
			mAnimationStop = true;
			mService.prev();
			if (mPlayerControl.getVisibility() == View.VISIBLE) {
				AlphaAnimationOut(mPlayerControl, 150);
				// AlphaAnimationIn(mDotLayout, 150);
				if (mViewPager != null) {
					mViewPager.setViewPageOnScrolled(false);
				}
			}
		} catch (RemoteException ex) {
		}
		return;
	}

	private void nextClicked() {
		if (mService == null)
			return;
		try {
			mFirstAnim = true;
			mPlayPause = false;
			mAnimationStop = true;
			mService.next();
			// add by JXH begin BUG #13810
			long curId = mService.getAudioId();
			updateLoveButton(curId);
			// add by JXH end
			if (mPlayerControl.getVisibility() == View.VISIBLE) {
				AlphaAnimationOut(mPlayerControl, 150);
				if (mViewPager != null) {
					mViewPager.setViewPageOnScrolled(false);
				}
			}
		} catch (RemoteException ex) {
			LogUtil.e(TAG, "---nextClicked error ", ex);
		}
		return;
	}

	private View.OnClickListener mAuroraNextListener = new View.OnClickListener() {
		public void onClick(View v) {
			nextClicked();
		}
	};
	private ObjectAnimator aima;

	private void setPlayingPosition(int pos, BaseAdapter adapter) {
		if (adapter == null) {
			return;
		}
		if (adapter instanceof AuroraPlayRadioAdapter) {
			((AuroraPlayRadioAdapter) adapter).setPlayingPosition(pos);
		} else {
			((AuroraPlayerListViewAdapter<AuroraIListItem>) adapter).setPlayingPosition(pos);
		}
	}

	private int getCurrentPlayPosition(BaseAdapter adapter) {
		if (adapter == null) {
			return 0;
		}
		if (adapter instanceof AuroraPlayRadioAdapter) {
			return ((AuroraPlayRadioAdapter) adapter).getCurrentPlayPosition();
		} else {
			return ((AuroraPlayerListViewAdapter<AuroraIListItem>) adapter).getCurrentPlayPosition();
		}
	}

	public void startPlayAnimation(final int position) {
		final BaseAdapter mAdapter = isRadioType ? mAuroraPlayRadioAdapter : mListAdapter;
		if (aima != null && aima.isStarted() && mAdapter != null) {
			setPlayingPosition(position, mAdapter);
			aima.end();
		}
		int[] location = new int[2];
		int[] location1 = new int[2];
		int[] location2 = new int[2];
		int distance = 0; // 移动距离
		mListView.getLocationInWindow(location);
		int currentPosition = getCurrentPlayPosition(mAdapter);
		View arg1 = mListView.getChildAt(position - mListView.getFirstVisiblePosition());
		if (arg1 == null) {
			setPlayingPosition(position, mAdapter);
			mListView.invalidateViews();
			playStart(position);
			return;
		}
		arg1.getLocationInWindow(location1);
		if (currentPosition < 0) {
			// 无动画
			setPlayingPosition(position, mAdapter);
			mListView.invalidateViews();
			playStart(position);
			return;
		} else if (currentPosition < mListView.getFirstVisiblePosition()) {
			// 从最上面飞进来
			mPlaySelect.setY(-mPlaySelect.getHeight());
			distance = location1[1] - location[1] + mPlaySelect.getHeight();
		} else if (currentPosition > mListView.getLastVisiblePosition()) {
			// 从最下面飞进来
			mPlaySelect.setY(mListView.getHeight());
			distance = mListView.getHeight() - location1[1] + location[1];
		} else {
			// 具体位置飞进
			View view = mListView.getChildAt(currentPosition - mListView.getFirstVisiblePosition());
			view.getLocationInWindow(location2);
			mPlaySelect.setY(location2[1] - location[1]);
			distance = Math.abs(location2[1] - location1[1]);
		}
		aima = ObjectAnimator.ofFloat(mPlaySelect, "y", location1[1] - location[1]);
		aima.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator arg0) {
				mPlaySelect.setVisibility(View.VISIBLE);
				setPlayingPosition(-1, mAdapter);
				mListView.invalidateViews();
				isShowAnimator = true;
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {}

			@Override
			public void onAnimationEnd(Animator arg0) {
				isShowAnimator = false;
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						setPlayingPosition(position, mAdapter);
						mListView.invalidateViews();
						mPlaySelect.setVisibility(View.GONE);
						playStart(position);
					}
				});
			}

			@Override
			public void onAnimationCancel(Animator arg0) {}
		});
		if (distance < 300) {
			aima.setDuration(150);
		} else {
			aima.setDuration(200);
		}
		aima.start();
	}

	private void playStart(int position) {
		// play radio start
		LogUtil.d(TAG, "playStart isRadioType:" + isRadioType + " position:" + position);
		if (isRadioType) {
			startPlayRadio(position);
			return;
		}
		// play radio end
		if (mService == null) {
			return;
		}
		try {
			mPlayPause = false;
			if (mbUriPath && mService.getFilePath() == null) {
				mService.openFile(mService.getPath());
				mService.play();
				if (mAlbum != null) {
					mAlbum.setStartAnimation(true);
				}
				return;
			}
			if (mLyricView != null) {
				mLyricView.setHasLyric(false);
			}
			mFirstAnim = true;
			mService.playListView(position);
			// mService.online_start(list, 0);
		} catch (Exception e) {
			Log.i(TAG, "----- --- playStart fail and position:" + position);
		}
		return;
	}

	private boolean isOnline(int position) {
		if (tListData == null || position >= tListData.size()) {
			return false;
		}
		return tListData.get(position).getIsDownLoadType() == 1;
	}

	private OnItemClickListener OnListViewListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
			if (mService == null)
				return;
			if (position >= 0) {
				if (isOnline(position)) {
					AuroraListItem item = tListData.get(position);
					if (item == null || !item.isAvailable()) {
						if (!Application.isRadioType()) {
							Toast.makeText(AuroraPlayerActivity.this, R.string.aurora_play_permission, Toast.LENGTH_SHORT).show();
							return;
						}
					}
					boolean isshow = FlowTips.showPlayFlowTips(AuroraPlayerActivity.this, new OndialogClickListener() {
						@Override
						public void OndialogClick() {
							startPlayAnimation(position);
						}
					});
					if (isshow) {
						return;
					}
				}
				startPlayAnimation(position);
			}
			return;
		}
	};
	private RepeatingImageButton.RepeatListener mAuroraFfwdListener = new RepeatingImageButton.RepeatListener() {
		public void onRepeat(View v, long howlong, int repcnt) {
			// scanForward(repcnt, howlong);
		}
	};
	private View.OnClickListener mLoveListener = new View.OnClickListener() {
		public void onClick(View v) {
			// showToast(R.string.add_to_playlist);
			if (mService != null) {
				try {
					// mService.toggleMyFavorite();
					long audioId = mService.getAudioId();
					if (audioId == -1) {
						showToast(R.string.player_failed);
						return;
					}
					if (!mService.isFavorite(audioId)) {
						mService.addToFavorite(audioId);
						mLoveButton.setImageResource(R.drawable.aurora_play_love_select);
						onLoveBtnScaleAnimation(mLoveButton);
					} else {
						mService.removeFromFavorite(audioId);
						onLoveBtnHideAnimation(mLoveButton);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};
	private View.OnClickListener mBackListener = new View.OnClickListener() {
		public void onClick(View v) {
			finish();
			/*
			 * if ( ! mFromNotification ) { overridePendingTransition ( R . anim . slide_left_in , R . anim . slide_right_out ) ; }
			 */
		}
	};
	private View.OnClickListener mShareListener = new View.OnClickListener() {
		public void onClick(View v) {
			// createShareIntent();
			if (mItemInfo == null) {
				return;
			}
			int isnet = mItemInfo.getIsDownLoadType();
			if (isnet == 0) {
				createShareIntent();
				return;
			}
			showSendConfirmDialog(R.string.aurora_playerdialog_share);
		}
	};
	private View.OnClickListener mShuffleListener = new View.OnClickListener() {
		public void onClick(View v) {
			// auroraToggleShuffle();
			startAlaphAnimation();
		}
	};

	private void auroraToggleShuffle() {
		if (mService == null) {
			return;
		}
		try {
			int mode = mService.getRepeatMode();
			if (mode == MediaPlaybackService.REPEAT_NONE) {
				mService.setShuffleMode(MediaPlaybackService.SHUFFLE_NONE);
				mService.setRepeatMode(MediaPlaybackService.REPEAT_ALL);
				// showToast(R.string.repeat_all_notif);
			} else if (mode == MediaPlaybackService.REPEAT_ALL) {
				int shuffle = mService.getShuffleMode();
				if (shuffle == MediaPlaybackService.SHUFFLE_NORMAL || shuffle == MediaPlaybackService.SHUFFLE_AUTO) {// shuffle
																														// ->
																														// CURRENT
					mService.setShuffleMode(MediaPlaybackService.SHUFFLE_NONE);
					mService.setRepeatMode(MediaPlaybackService.REPEAT_ALL);
					// showToast(R.string.repeat_all_notif);
				} else {
					mService.setShuffleMode(MediaPlaybackService.SHUFFLE_NONE);
					mService.setRepeatMode(MediaPlaybackService.REPEAT_CURRENT);
					// showToast(R.string.repeat_current_notif);
				}
			} else if (mode == MediaPlaybackService.REPEAT_CURRENT) {
				mService.setShuffleMode(MediaPlaybackService.SHUFFLE_NORMAL);
				mService.setRepeatMode(MediaPlaybackService.REPEAT_ALL);
				// showToast(R.string.shuffle_on_notif);
			}
			setAuroraShuffleButtonImage();
		} catch (RemoteException ex) {
		}
	}

	private void setAuroraShuffleButtonImage() {
		if (mService == null)
			return;
		try {
			switch (mService.getRepeatMode()) {
			case MediaPlaybackService.REPEAT_ALL:
				if (mService.getShuffleMode() == MediaPlaybackService.SHUFFLE_NORMAL) {
					mShuffleButton.setImageResource(R.drawable.aurora_shuffle);
				} else {
					mShuffleButton.setImageResource(R.drawable.aurora_repeat_all);
				}
				break;
			case MediaPlaybackService.REPEAT_CURRENT:
				mShuffleButton.setImageResource(R.drawable.aurora_repeat_one);
				break;
			default:
				mShuffleButton.setImageResource(R.drawable.aurora_repeat_all);
				break;
			}
		} catch (RemoteException ex) {
		}
	}

	private void showToast(int resid) {
		if (mToast == null) {
			mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		}
		mToast.setText(resid);
		mToast.show();
	}

	private void startPlayback() {
		if (mService == null)
			return;
		Intent intent = getIntent();
		String filename = "";
		Uri uri = intent.getData();
		if (uri != null && uri.toString().length() > 0) {
			mPlayUri = uri;
			// If this is a file:// URI, just use the path directly instead
			// of going through the open-from-filedescriptor codepath.
			String scheme = uri.getScheme();
			boolean flag = false;
			if ("file".equals(scheme)) {
				filename = uri.getPath();
				flag = true;
			} else {
				misMms = true;
				filename = uri.toString();
			}
			try {
				mService.stop();
				mbSmallSize = false;
				if (flag) {
					// lory add
					File f = new File(filename);
					if (!f.exists()) {
						mbNotValid = true;
						LogUtil.d(TAG, "----444444444444444444444444");
						return;
					} else {
						if (f.length() < 500 * 1024) {
							mbSmallSize = true;
						}
					}
				}
				mPlayPause = false;
				mService.openFile(filename);
				mService.play();
				setIntent(new Intent());
				mbUriPath = true;
				mHandler.removeMessages(AURORA_REFRESH_LISTVIEW_BYURI);
				mHandler.obtainMessage(AURORA_REFRESH_LISTVIEW_BYURI).sendToTarget();
			} catch (Exception ex) {
				mbUriPath = false;
				Log.d(TAG, "----- couldn't start playback: " + ex);
			}
		} else {
			// setPlayShuffleMode();
			if (mPlayUri == null) {
				mbUriPath = false;
				misMms = false;
				mbSmallSize = false;
				reFreshListData();
			} else {
				mHandler.removeMessages(AURORA_REFRESH_LISTVIEW_BYURI);
				mHandler.obtainMessage(AURORA_REFRESH_LISTVIEW_BYURI).sendToTarget();
			}
		}
		updateAuroraTrackInfo();
		long next = refreshAuroraNow();
		queueAuroraNextRefresh(next);
	}

	private void setPlayShuffleMode() {
		try {
			int mode = mService.getShuffleMode();
			if (mode == MediaPlaybackService.SHUFFLE_NORMAL) {
				mService.setRepeatMode(MediaPlaybackService.REPEAT_ALL);
			} else if (mode == MediaPlaybackService.SHUFFLE_AUTO) {
				mService.setShuffleMode(MediaPlaybackService.SHUFFLE_NORMAL);
				mService.setRepeatMode(MediaPlaybackService.REPEAT_ALL);
			} else if (mode == MediaPlaybackService.SHUFFLE_NONE) {
				int mode2 = mService.getRepeatMode();
				if (mode2 == MediaPlaybackService.REPEAT_CURRENT) {
					mService.setShuffleMode(MediaPlaybackService.SHUFFLE_NONE);
					mService.setRepeatMode(MediaPlaybackService.REPEAT_CURRENT);
				} else {
					mService.setShuffleMode(MediaPlaybackService.SHUFFLE_NONE);
					mService.setRepeatMode(MediaPlaybackService.REPEAT_ALL);
				}
			}
			setAuroraShuffleButtonImage();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	private void doStartPlay() {
		LogUtil.d(TAG, "------doStartPlay ");
		try {
			if (mService != null) {
				mPlayPause = false;
				if (mService.isPlaying()) {
				} else {
					mService.play();
				}
				refreshAuroraNow();
			}
		} catch (RemoteException ex) {
			LogUtil.e(TAG, "---doStartPlay error", ex);
		}
	}

	private void doPauseResume() {
		try {
			if (mService != null) {
				if (mService.isPlaying()) {
					mService.pause();
					LogUtil.d(TAG, "-----doPauseResume pause");
				} else {
					mService.play();
					LogUtil.d(TAG, "-----doPauseResume play");
				}
				refreshAuroraNow();
				mPlayPause = true;
			} else {
				mPauseButton.setBackgroundResource(R.drawable.aurora_pause);
			}
		} catch (RemoteException ex) {
			LogUtil.e(TAG, "---doPauseResume Error ", ex);
			mPauseButton.setBackgroundResource(R.drawable.aurora_pause);
		}
	}

	private boolean showOnlineLyric(String filename) {
		LogUtil.d(TAG, "-----showOnlineLyric :" + filename);
		if (filename == null) {
			return false;
		}
		try {
			File file = new File(filename);
			if (file.exists()) {
				executor.execute(new LyricThread(filename));
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * 取歌词
	 * @return
	 */
	private boolean showAndReadLyc() {
		LogUtil.d(TAG, "------showAndReadLyc");
		synchronized (this) {
			try {
				String lrcpath = mService.getLrcUri();
				if (!TextUtils.isEmpty(lrcpath)) {
					executor.submit(new LyricThread(lrcpath));
					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
				LogUtil.e(TAG, "--showAndReadLyc error", e);
			}
			return false;
		}
	}

	private class LyricThread extends Thread {
		private String filename = null;

		public LyricThread(String filename) {
			this.filename = filename;
		}

		@Override
		public void run() {
			if (mLyricView != null) {
				mHandler.removeMessages(AURORA_REFRESH_LYRIC);
				mLyricView.read(filename);
				mHandler.obtainMessage(AURORA_REFRESH_LYRIC).sendToTarget();
			}
		}
	}

	private ThreadPoolExecutor executor = ThreadPoolExecutorUtils.getThreadPoolExecutor().getExecutor();

	private void upDateLayoutBg(final Bitmap bitmap) {
		if (mbgView == null) {
			return;
		}
		executor.submit(new Runnable() {
			@Override
			public void run() {
				boolean flag = ((bitmap != null) ? true : false);
				Bitmap newImg = Blur.getBgBlurView(AuroraPlayerActivity.this, bitmap);
				mHandler.removeMessages(AURORA_ALBUMBG_DECODED);
				mHandler.obtainMessage(AURORA_ALBUMBG_DECODED, newImg).sendToTarget();
				if (flag && !bitmap.isRecycled()) {
					bitmap.recycle();
				}
			}
		});
		return;
	}

	private void upDateListViewByUri() {
		if (mService == null) {
			return;
		}
		Cursor tCursor = null;
		try {
			if (mService.getFilePath() == null) {
				if (mbUriPath) {
					mDataSize = 1;
					tServiceListData = new ArrayList<AuroraListItem>();
					String tpath = mService.getPath();
					String mTitle = "";
					if (!misMms) {
						int idx_start1 = tpath.lastIndexOf('/');
						int idx_end1 = tpath.lastIndexOf('.');
						mTitle = tpath.substring(idx_start1 + 1, idx_end1);
					} else {
						if (tpath != null) {
							mTitle = tpath;
						} else {
							mTitle = getString(R.string.unknown_artist_name);
						}
					}
					String mPath = tpath;
					long mId = -1;
					String mAlbumName = getString(R.string.unknown_album_name);
					String mArtistName = getString(R.string.unknown_artist_name);
					String mUri = mPath;
					String albumUri = AuroraMusicUtil.getImgPath(getApplication(), AuroraMusicUtil.MD5(mTitle + mArtistName + mAlbumName));
					AuroraListItem item = new AuroraListItem(mId, mTitle, mUri, mAlbumName, 0, mArtistName, 0, albumUri, null, null, 0);
					tServiceListData.add(item);
					mService.setListInfo(tServiceListData);
					if (mListView != null) {
						tListData.clear();
						tListData.addAll(tServiceListData);
						if (mListAdapter == null) {
							mListAdapter = new AuroraPlayerListViewAdapter<AuroraListItem>(AuroraPlayerActivity.this, tListData);
							mListView.setAdapter(mListAdapter);
						} else {
							mListAdapter.notifyDataSetChanged();
						}
						upDateListItemSelection();
					}
					return;
				}
			}
			long id = mService.getAudioId();
			tCursor = MusicUtils.query(AuroraPlayerActivity.this, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mCursorCols,
					MediaStore.Audio.Media._ID + "=?", new String[] { Long.valueOf(id).toString() }, null);
			if (tCursor == null) {
				mDataSize = 0;
				return;
			}
			tServiceListData = new ArrayList<AuroraListItem>();
			tCursor.moveToFirst();
			if (!tCursor.isAfterLast()) {
				int mId = tCursor.getInt(0);
				String mTitle = tCursor.getString(1);
				String mPath = tCursor.getString(2);
				String mAlbumName = tCursor.getString(3);
				String mArtistName = tCursor.getString(4);
				String albumUri = Globals.mSongImagePath + File.separator
						+ AuroraMusicUtil.MD5(mTitle.trim() + mArtistName.trim() + mAlbumName.trim());
				String mUri = mPath;
				AuroraListItem item = new AuroraListItem(mId, mTitle, mUri, mAlbumName, 0, mArtistName, 0, albumUri, null, null, 0);
				tServiceListData.add(item);
			}
			mService.setListInfo(tServiceListData);
			if (mListView != null) {
				tListData.clear();
				tListData.addAll(tServiceListData);
				if (mListAdapter != null) {
					mListAdapter.notifyDataSetChanged();
				} else {
					mListAdapter = new AuroraPlayerListViewAdapter<AuroraListItem>(AuroraPlayerActivity.this, tListData);
					mListView.setAdapter(mListAdapter);
				}
				upDateListItemSelection();
			}
		} catch (Exception e) {
			Log.d(TAG, "----- upDateListViewByUri fail --------");
			// e.printStackTrace();
		} finally {
			if (tCursor != null) {
				tCursor.close();
				tCursor = null;
			}
		}
		return;
	}

	private void onImgBackGroudUpdate(Bitmap tb, int my_args) {
		if (tb == null) {
			if (mAnimationBitmap == null) {
				mAnimationBitmap = mDefautBitmap;
			}
			if (mbFirstAnim) {
				if (mAnimView != null) {
					mAnimView.setVisibility(View.GONE);
				}
				setAlphImageDrawable(mDefautBitmap);
			} else {
				setBgAlphDrawable(mDefautBitmap);
			}
			mAlbum.startAnimDefaultBitmap(true);
			// mAlbum.setBackground(null);
		} else {
			if (my_args == 1) {
				mAnimationBitmap = mDefautBitmap;
			}
			if (mAnimationBitmap == null) {
				mAnimationBitmap = tb;
			}
			// mAlbum.setAlphaBitmap((Bitmap)msg.obj);
			if (mbFirstAnim) {
				if (mAnimView != null) {
					mAnimView.setVisibility(View.GONE);
				}
				setAlphImageDrawable(tb);
			} else {
				if (my_args == 1) {
					setBgAlphDrawable(mDefautBitmap);
				} else {
					setBgAlphDrawable(tb);
				}
			}
		}
		// modify by chenhl end 20140909
		mbFirstAnim = false;
		return;
	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AURORA_META_CHANGED:
				upDateListItemSelection();
				updateAuroraTrackInfo();
				// setPauseButtonImage();
				onStopPlayAnimation(false);
				queueAuroraNextRefresh(1);
				updateNotValidImageButton();
				break;
			// add by chenhl start
			case AURORA_GET_ALBUM_ERROR:
				if (mLyricView != null) {
					mLyricView.setHasLyric(false);
				}
				break;
			// add by chenhl end
			case AURORA_REFRESH_LYRIC:
				if (mLyricView != null) {
					mLyricView.setHasLyric(true);
					mLyricView.setTextsEx(mLycScrollView);
					refreshAuroraLrc(true);
				}
				break;
			case AURORA_REFRESH_LISTVIEW_NOCHANGED:
				if (mListAdapter != null) {
					upDateListItemSelection();
					mListAdapter.notifyDataSetChanged();
				}
				break;
			case AURORA_REFRESH_LISTVIEW_BYURI:
				upDateListViewByUri();
				updateAuroraTrackInfo();
				break;
			case AURORA_REFRESH_LISTVIEW:
				reFreshListData();
				break;
			case AURORA_ALBUMBG_DECODED:
				if (mbgView != null) {
					Bitmap bitmap = (Bitmap) msg.obj;
					// modify by chenhl start 20140909
					if (bitmap == null) {
						mbgView.setScaleType(ScaleType.FIT_XY);
						setLayoutBgAlph(mDefaultBg);
					} else {
						mbgView.setScaleType(ScaleType.CENTER_CROP);
						setLayoutBgAlph(bitmap);
					}
					// modify by chenhl end 20140909
				}
				break;
			case AURORA_ANIMATION_STASRT:
				onSeekBarAnimation();
				break;
			case AURORA_ALBUM_ART_DECODED:
				Bitmap tb = (Bitmap) msg.obj;
				int my_args = msg.arg1;
				if (mAnimView != null && mAnimView.getVisibility() != View.GONE) {
					mAnimView.setVisibility(View.GONE);
				}
				onImgBackGroudUpdate(tb, my_args);
				break;
			case AURORA_REFRESH:
				long next = 500;
				try {
					if (!mFromTouch && mService.isPlaying()) {
						next = refreshAuroraNow();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				queueAuroraNextRefresh(next);
				break;
			case AURORA_REFRESH_LRC:
				long nextLrc = 500;
				try {
					if (!mFromTouch && mService.isPlaying()) {
						nextLrc = refreshAuroraLrc(false);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				queueAuroraNextRefreshLrc(nextLrc);
				break;
			case AURORA_QUIT:
				// This can be moved back to onCreate once the bug that prevents
				// Dialogs from being started from onCreate/onResume is fixed.
				new AlertDialog.Builder(AuroraPlayerActivity.this).setTitle(R.string.service_start_error_title)
						.setMessage(R.string.service_start_error_msg)
						.setPositiveButton(R.string.service_start_error_button, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								finish();
							}
						}).setCancelable(false).show();
				break;
			// add by JXH 20150910 begin
			case AURORA_SEEK:
				int p = (Integer) msg.obj;
				seek(p);
				break;
			case AURORA_STOPTRACKINGTOUCH:
				mPosOverride = -1;
				mFromTouch = false;
				onStopPlayAnimation(false);
				break;
			// add by JXH 20150910 end
			default:
				break;
			}
		}
	};

	private void upDateListItemSelection() {
		if (mService == null || mListView == null || mListAdapter == null) {
			return;
		}
		try {
			if (mService.getFilePath() == null && mbUriPath) {
				mListAdapter.setPlayingPosition(0);
				mListView.invalidateViews();
				return;
			}
			long curId = mService.getAudioId();
			updateLoveButton(curId);
			int len = tListData.size();
			for (int i = 0; i < len; i++) {
				if (curId != -1 && curId == tListData.get(i).getSongId()) {
					mListAdapter.setPlayingPosition(i);
					mCurrentPlaying = i;
					break;
				}
			}
			// cur = mService.getQueuePosition();
			mListView.invalidateViews();
			if (mbSelected) {
				mbSelected = false;
				mListView.setSelection(mCurrentPlaying);
			}
		} catch (Exception e) {
			Log.i(TAG, "----- --- upDateListItemSelection fail ---- ");
			// e.printStackTrace();
		}
		// mListView.invalidateViews();
		return;
	}

	/**
	 * 搜索加载歌词
	 */
	private void findNetLyric() {
		if (mLoadLrtThread != null) {
			LogUtil.d(TAG, "-----remove-mLoadLrtThread");
			executor.remove(mLoadLrtThread);
			if (!mLoadLrtThread.isInterrupted() || mLoadLrtThread.isAlive()) {
				mLoadLrtThread.interrupt();
			}
			mLoadLrtThread.canCel();
		}
		mLoadLrtThread = new LoadLrtThread(mItemInfo);
		LogUtil.d(TAG, "-----execute-mLoadLrtThread");
		executor.submit(mLoadLrtThread);
		// mLoadLrtThread.start();
	}

	private void showSendConfirmDialog(final int messageId) {
		final DialogFragment frag = AuroraDialogFragment.newInstance(AuroraPlayerActivity.this, messageId);
		frag.show(getFragmentManager(), "share confirm");
		AuroraDialogFragment.registerItemClickCallback(mDilogCallBack);
	}

	private final AuroraDilogCallBack mDilogCallBack = new AuroraDilogCallBack() {
		@Override
		public void onFinishDialogFragment(int ret) {
			if (!AuroraMusicUtil.isNetWorkActive(AuroraPlayerActivity.this)) {
				showToast(R.string.aurora_network_error);
				return;
			}
			LogUtil.d(TAG, "-------ret:" + ret);
			if (ret == 0) {// 微信会话
				shareOnWeiXin(ret);
			} else if (ret == 1) {// 朋友圈
				shareOnWeiXin(ret);
				ReportUtils.getInstance(getApplicationContext()).reportMessage(ReportUtils.TAG_BTN_SHARE_FR);
			} else if (ret == 2) {// 微薄
				shareOnXLWb();
				// showToast(R.string.aurora_share_failed);
				ReportUtils.getInstance(getApplicationContext()).reportMessage(ReportUtils.TAG_BTN_SHARE_TW);
			} else {// 其他
				createShareIntent();
			}
		}
	};

	private void shareOnXLWb() {
		AuroraShareXLWb.getInstance(this).startWeiBoEx(AuroraPlayerActivity.this, mAuroraWeiBoCallBack);
		LogUtil.d(TAG, "-------------------------shareOnXLWb");
		return;
	}

	private final AuroraWeiBoCallBack mAuroraWeiBoCallBack = new AuroraWeiBoCallBack() {
		@Override
		public void onSinaWeiBoCallBack(int ret) {
			LogUtil.d(TAG, " ----- onSinaWeiBoCallBack ret:" + ret);
			if (ret == AuroraShareXLWb.AURORA_WEIBO_SUCCESS) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						startWeiBoEx();
					}
				});
			}
		}
	};

	// add by chenhl start
	private void startWeiBoEx() {
		ImageLoader.getInstance().loadImage(ImageUtil.transferImgUrl(mItemInfo.getAlbumImgUri(), 80), mOptions, new SimpleImageLoadingListener() {
			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				LogUtil.d(TAG, "----- ---- startWeiBoEx onLoadingComplete");
				startWeiBo(loadedImage);
			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
				LogUtil.d(TAG, "----- ---- startWeiBoEx onLoadingFailed ");
				startWeiBo(null);
			}
		});
	}

	// add by chenhl end
	private void startWeiBo(Bitmap bm) {
		IMediaPlaybackService tmpService = null;
		if (mService == null) {
			LogUtil.d(TAG, "----- startWeiBo 0 MusicUtils.sService:" + MusicUtils.sService);
			if (MusicUtils.sService == null) {
				showToast(R.string.xlweibosdk_share_failed);
				return;
			} else {
				tmpService = MusicUtils.sService;
			}
		} else {
			tmpService = mService;
		}
		String url = null;
		String title = null;
		String artist = null;
		String ttString = null;
		String albumStr = null;
		String picPath = null;
		String bitrate = null;
		int duration = 0;
		long id = 0;
		try {
			LogUtil.d(TAG, "----- ----- startWeiBo 0.1");
			id = tmpService.getAudioId();
			title = tmpService.getTrackName();
			artist = tmpService.getArtistName();
			if (TextUtils.isEmpty(artist)) {
				artist = mArtistNameStr;
			}
			albumStr = tmpService.getAlbumName();
			bitrate = tmpService.getMusicbitrate();
			if (TextUtils.isEmpty(bitrate)) {
				bitrate = "128";
			}
			url = "http://www.xiami.com/song/" + id + "?ref=acopy";
			ttString = tmpService.getFilePath();
			AuroraMusicInfo info = MusicUtils.getDbMusicInfo(AuroraPlayerActivity.this, id, title, artist, 1);// 1:表示网络
			LogUtil.d(TAG, "----- ----- startWeiBo 0.2 info:" + info);
			if (info != null) {
				picPath = info.getPicPath();
			} else {
				picPath = null;
			}
			duration = (int) tmpService.duration();
			LogUtil.d(TAG, "----- ----- startWeiBo 1 id:" + id + ",title:" + title + ",url:" + url + ",picPath:" + picPath);
			LogUtil.d(TAG, "----- ----- startWeiBo 2 ttString:" + ttString);
		} catch (Exception e) {
			LogUtil.d(TAG, "----- ----- startWeiBo 3");
			e.printStackTrace();
			showToast(R.string.xlweibosdk_share_failed);
			return;
		}
		AuroraShareXLWb.getInstance(this).sendMusic2XLWb(url, url, title, artist, bm, duration, AuroraPlayerActivity.this);
		return;
	}

	private void shareOnWeiXin(final int type) {
		ImageLoader.getInstance().loadImage(ImageUtil.transferImgUrl(mItemInfo.getAlbumImgUri(), 80), mOptions, new SimpleImageLoadingListener() {
			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				LogUtil.d(TAG, "----- ---- shareOnWeiXin onLoadingComplete");
				shareOnNet(type, loadedImage);
			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
				LogUtil.d(TAG, "----- ---- shareOnWeiXin onLoadingFailed ");
				shareOnNet(type, null);
			}
		});
		return;
	}

	private void shareOnNet(int type, Bitmap bm) {
		if (mService == null) {
			showToast(R.string.aurora_share_failed);
			return;
		}
		String url = null;
		String title = null;
		String artist = null;
		String ttString = null;
		String albumStr = null;
		String picPath = null;
		String bitrate = null;
		try {
			long id = mService.getAudioId();
			title = mService.getTrackName();
			artist = mService.getArtistName();
			if (artist == null || (artist != null && TextUtils.isEmpty(artist))) {
				artist = mArtistNameStr;
			}
			albumStr = mService.getAlbumName();
			bitrate = mService.getMusicbitrate();
			if (bitrate == null || (bitrate != null && bitrate.isEmpty())) {
				bitrate = "128";
			}
			// url = "music.baidu.com/song/"+id;
			url = "m.xiami.com/song/" + id;
			ttString = mService.getFilePath();
			if (mItemInfo != null) {
				picPath = mItemInfo.getAlbumImgUri();
			} else {
				picPath = null;
			}
			if (picPath != null) {
				LogUtil.d(TAG, "----- ---- shareOnNet 1 ------");
			}
			// Log.i(TAG,
			// "----- ----- shareOnNet 1 id:"+id+",title:"+title+",url:"+url+",ttString:"+ttString);
		} catch (Exception e) {
			// e.printStackTrace();
			// Log.i(TAG, "----- ----- shareOnNet 2 ");
			showToast(R.string.aurora_share_failed);
			return;
		}
		if (mWxShare == null) {
			return;
		}
		if (type == 0) {// 微信会话
			mWxShare.sendMusic2Wx(ttString, url, false, title, artist, picPath, albumStr, bitrate, bm);
		} else if (type == 1) {// 朋友圈
			mWxShare.sendMusic2Wx(ttString, url, true, title, artist, picPath, albumStr, bitrate, bm);
		}
		return;
	}

	private void createShareIntent() {
		synchronized (this) {
			if (mItemInfo == null || mService == null) {
				Log.i(TAG, "----- ---- createShareIntent fail ---- mItemInfo:" + mItemInfo);
				return;
			}
			int isdown = mItemInfo.getIsDownLoadType();
			String tilte = getString(R.string.aurora_player_share);// mItemInfo.getTitle();
			Uri tUri = null;
			Intent intent = new Intent(Intent.ACTION_SEND);
			if (isdown == 0) {
				tUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, String.valueOf(mItemInfo.getSongId()));
				if (tUri == null) {
					return;
				}
				intent.setType("audio/*");
				intent.putExtra(Intent.EXTRA_SUBJECT, tilte);
				intent.putExtra(Intent.EXTRA_STREAM, tUri);
			} else if (isdown == 1) {
				String uriStr = getString(R.string.aurora_player_shareinfo) + "<" + mItemInfo.getTitle() + ">";
				try {
					uriStr += mService.getFilePath();
				} catch (Exception e) {
					uriStr = null;
				}
				if (uriStr == null) {
					uriStr = mItemInfo.getAlbumName();
				}
				// intent.setType("audio/*");
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_SUBJECT, tilte);
				intent.putExtra(Intent.EXTRA_TEXT, uriStr);
			}
			// intent.putExtra(Intent.EXTRA_SUBJECT, title);
			// intent.putExtra(Intent.EXTRA_STREAM, mUri);
			startActivity(Intent.createChooser(intent, tilte));
		}
		return;
	}

	private BroadcastReceiver mStatusListener = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			LogUtil.i(TAG, "----- ---- action:" + action);
			if (action.equals(MediaPlaybackService.META_CHANGED)) {
				// redraw the artist/title info and
				// set new max for progress bar
				// modify by JXH 20150812 begin
				mHandler.removeMessages(AURORA_META_CHANGED);
				mHandler.sendEmptyMessageDelayed(AURORA_META_CHANGED, 100);
				// upDateListItemSelection();
				// updateAuroraTrackInfo();
				// // setPauseButtonImage();
				// onStopPlayAnimation(false);
				// queueAuroraNextRefresh(1);
				// updateNotValidImageButton();
				// modify by JXH 20150812 end
			} else if (action.equals(MediaPlaybackService.PLAYSTATE_CHANGED)) {
				setPauseButtonImage();
			} else if (action.equals(MediaPlaybackService.QUEUE_CHANGED)) {
				if (mPlayUri == null) {
					mHandler.removeMessages(AURORA_REFRESH_LISTVIEW);
					mHandler.obtainMessage(AURORA_REFRESH_LISTVIEW).sendToTarget();
				}
			} else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				if (!AuroraMusicUtil.isNetWorkActive(AuroraPlayerActivity.this)) {
					showToast(R.string.aurora_network_error);
				}
			}
		}
	};

	private static class AuroraAlbumSongIdWrapper {
		public long albumid;
		public long songid;

		AuroraAlbumSongIdWrapper(long aid, long sid) {
			albumid = aid;
			songid = sid;
		}
	}

	//
	public class AlbumArtHandler extends Handler {
		private long mSongId = -1;
		private long mSongId2 = -1;

		public AlbumArtHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			long albumid = ((AuroraAlbumSongIdWrapper) msg.obj).albumid;
			long songid = ((AuroraAlbumSongIdWrapper) msg.obj).songid;
			// String albumuri = ((AuroraAlbumSongIdWrapper)
			// msg.obj).netAlbumUri;
			// Log.i(TAG, "----- ---- handleMessage 1 msg.what:"+msg.what);
			if (msg.what == AURORA_GET_ALBUM_ART) {
				if (mSongId != songid || songid < 0) {
					LogUtil.d(TAG, "---- handleMessage 3: loading local lyric");
					if (!showAndReadLyc()) {// 显示本地歌词
						LogUtil.d(TAG, "------ handleMessage 3.1: loading local lyric null");
						if (AuroraMusicUtil.isWifiNetActvie(AuroraPlayerActivity.this) && Globals.SWITCH_FOR_ONLINE_MUSIC) {
							LogUtil.d(TAG, "----- ---- handleMessage 3.2: find net lyric");
							findNetLyric();// 网络搜索歌词
						} else {// BUG FIX 16875//显示无歌词
							LogUtil.d(TAG, "----- ---- handleMessage 3.2.1 no net");
							mHandler.obtainMessage(AURORA_GET_ALBUM_ERROR).sendToTarget();
						}
					}
					String imgpathString = null;
					if (mItemInfo != null) {
						imgpathString = MusicUtils.getDbImg(AuroraPlayerActivity.this, songid, mItemInfo.getTitle(), mItemInfo.getArtistName(), 1);
						LogUtil.d(TAG, "----- ---- handleMessage 3.3 imgpathString:" + imgpathString);
					}
					if (imgpathString != null && imgpathString.equals(AuroraSearchLyricActivity.DEFUALT_IMG_URL)) {// 默认图片
						LogUtil.d(TAG, "----- ---- handleMessage 3.4:");
						mHandler.removeMessages(AURORA_ALBUMBG_DECODED);
						mHandler.obtainMessage(AURORA_ALBUMBG_DECODED, null).sendToTarget();
						mHandler.removeMessages(AURORA_ALBUM_ART_DECODED);
						Message numsg = mHandler.obtainMessage(AURORA_ALBUM_ART_DECODED, 1, -1, null);
						mHandler.sendMessage(numsg);
					} else {
						// add by tangjie 2014/08/25 start
						String path = null;
						Bitmap bm = null;
						if (mItemInfo != null) {
							path = mItemInfo.getAlbumImgUri();
							// imgpathString:null,path:null songid:3338
							// albumid:40
							LogUtil.d(TAG, "----- ---- handleMessage 3.5 imgpathString:" + imgpathString + ",path:" + path + " songid:" + songid
									+ " albumid:" + albumid);
							if (imgpathString != null && imgpathString.equals(AuroraSearchLyricActivity.AURORA_ORIGINAL_IMG_URL)) {
								LogUtil.d(TAG, "AURORA_ORIGINAL_IMG_URL----- ---- handleMessage 3.6");
								bm = MusicUtils.getAuroraArtwork(AuroraPlayerActivity.this, songid, -1, mWidth, mWidth);
							} else {
								LogUtil.d(TAG, "----- ---- handleMessage 3.7");
								bm = MusicUtils.getArtwork(AuroraPlayerActivity.this, songid, albumid, false, path, 0, mWidth, mWidth);
								LogUtil.d(TAG, "---------------bm:" + bm + " path:" + path);
							}
						}
						// add by tangjie end
						if (bm == null) {
							// modify by chenhl start 20140919
							if (AuroraMusicUtil.isWifiNetActvie(AuroraPlayerActivity.this) && mItemInfo != null && Globals.SWITCH_FOR_ONLINE_MUSIC) {
								LogUtil.d(TAG, "----- ---- handleMessage 3.8 isnet:" + mItemInfo.getIsDownLoadType());
								mSearchImgTask = new SearchImgTask(mItemInfo);
								mSearchImgTask.executeOnExecutor(executor);
							} else {
								LogUtil.d(TAG, "----- ---- handleMessage 3.9");
								mHandler.removeMessages(AURORA_ALBUMBG_DECODED);
								mHandler.obtainMessage(AURORA_ALBUMBG_DECODED, null).sendToTarget();
								mHandler.removeMessages(AURORA_ALBUM_ART_DECODED);
								Message numsg = mHandler.obtainMessage(AURORA_ALBUM_ART_DECODED, 1, -1, null);
								mHandler.sendMessage(numsg);
							}
							// modify by chenhl end 20140919
							// albumid = -1;
						} else {
							LogUtil.d(TAG, "----- ---- handleMessage 3.10");
							Bitmap tBitmap = AuroraMusicUtil.getCircleBitmap(bm);
							mHandler.removeMessages(AURORA_ALBUM_ART_DECODED);
							Message numsg = mHandler.obtainMessage(AURORA_ALBUM_ART_DECODED, 0, -1, tBitmap);
							mHandler.sendMessage(numsg);
							upDateLayoutBg(bm);
							System.gc();
						}
					}
					// }
					mSongId = songid;
					mSongId2 = -1;
				} else {
					LogUtil.d(TAG, "----- ---- handleMessage 3.11");
					if (!showAndReadLyc()) {
						LogUtil.d(TAG, "----- ---- handleMessage 3.12");
						if (AuroraMusicUtil.isWifiNetActvie(AuroraPlayerActivity.this) && Globals.SWITCH_FOR_ONLINE_MUSIC) {
							findNetLyric();
						}
					}
				}
			} else if (msg.what == AURORA_GET_ALBUM_ARTDEFAULT && (mSongId2 != songid || songid < 0)) {
				LogUtil.d(TAG, "----- ---- handleMessage 4");
				mHandler.removeMessages(AURORA_ALBUMBG_DECODED);
				mHandler.obtainMessage(AURORA_ALBUMBG_DECODED, null).sendToTarget();
				mHandler.removeMessages(AURORA_ALBUM_ART_DECODED);
				Message numsg = mHandler.obtainMessage(AURORA_ALBUM_ART_DECODED, 1, -1, null);
				mHandler.sendMessage(numsg);
				mSongId2 = songid;
			}
		}
	}

	private static class AuroraWorker implements Runnable {
		private final Object mLock = new Object();
		private Looper mLooper;

		AuroraWorker(String name) {
			Thread t = new Thread(null, this, name);
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();
			synchronized (mLock) {
				while (mLooper == null) {
					try {
						mLock.wait();
					} catch (InterruptedException ex) {
					}
				}
			}
		}

		public Looper getLooper() {
			return mLooper;
		}

		public void run() {
			synchronized (mLock) {
				Looper.prepare();
				mLooper = Looper.myLooper();
				mLock.notifyAll();
			}
			Looper.loop();
		}

		public void quit() {
			mLooper.quit();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (!mFromNotification) {
			if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
				finish();
				// overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	/*-------------animation start ---------------------------------*/
	private float mDistance = 0f;
	private float distance1 = 0f;
	// private float defautdistance1 = 0f;
	private int mCenterWidth1, mCenterHeight1;
	private float hdistance1 = 0f;
	private float degree1 = 0f;
	// private float scale1 = 0.3f;
	private float alpha1 = 0f;
	private int mSeekBarWidth = 0;
	private int mSeekBarstart = 0;
	private float mStarth = 0f;
	private boolean ani1 = false;
	// private float defautdistance2 = 0f;
	private float distance2 = 0f;
	private int mCenterWidth2, mCenterHeight2;
	private float hdistance2 = 0f;
	private float degree2 = 0f;
	// private float scale2 = 0.3f;
	private float alpha2 = 0f;
	private boolean ani2 = false;

	@Override
	public void onAnimationCallBack(View view, float interpolatedTime, Transformation t) {
		if (mAnimationStop) {
			t.setAlpha(0);
			return;
		}
		Matrix matrix = t.getMatrix();
		if (view == mNote1) {
			if (interpolatedTime <= 0f) {
				distance1 = mDistance;
				// defautdistance1 = mDistance;
				degree1 = 0;
				hdistance1 = 0;
			}
			ani1 = true;
			boolean overHalf = (interpolatedTime > 0.5f);
			if (overHalf) {
				alpha1 = 1f - alpha1;
			} else {
				// alpha1 = 0.3f + (1.0f - 0f) * interpolatedTime;
				alpha1 = (1.0f - 0f) * interpolatedTime;
			}
			float tscale = 0.5f + 1.0f * interpolatedTime;
			if (overHalf) {
				tscale = 0.5f + 1.0f * (1.0f - interpolatedTime);
			}
			matrix.preTranslate(-mCenterWidth1, -mCenterHeight1);
			matrix.setScale(tscale, tscale);
			matrix.postTranslate(mCenterWidth1, mCenterHeight1);
			if (interpolatedTime > 0.45f && !ani2) {
				mNote2.startAnimation(mAnimationModel2);
			}
			if (interpolatedTime >= 1.0f) {
				ani1 = false;
				alpha1 = 0;
			}
			if (!overHalf && alpha1 < 0.3f) {
				distance1 += 0.1f;
			} else {
				distance1 += 0.4f;
			}
			hdistance1 += 0.8f;
			degree1 += 0.3f;
			if (degree1 > 35) {
				degree1 = 35;
			}
			matrix.postRotate(-degree1 * interpolatedTime);
			matrix.postTranslate(distance1, -hdistance1 / 4);
			t.setAlpha(alpha1);
			mNote1.setVisibility(View.VISIBLE);
		} else if (view == mNote2) {
			if (interpolatedTime <= 0f) {
				distance2 = mDistance - offset;
				// defautdistance2 = mDistance;
				degree2 = 0;
				hdistance2 = 0;
			}
			ani2 = true;
			boolean overHalf = (interpolatedTime > 0.5f);
			if (overHalf) {
				alpha2 = 1f - alpha2;
			} else {
				// alpha2 = 0.3f + (1.0f - 0f) * interpolatedTime;
				alpha2 = (1.0f - 0f) * interpolatedTime;
			}
			float tscale = 0.5f + 1.0f * interpolatedTime;
			if (overHalf) {
				tscale = 0.5f + 1.0f * (1.0f - interpolatedTime);
			}
			matrix.preTranslate(-mCenterWidth2, -mCenterHeight2);
			matrix.setScale(tscale, tscale);
			matrix.postTranslate(mCenterWidth2, mCenterHeight2);
			if (interpolatedTime > 0.45f && !ani1) {
				mNote1.startAnimation(mAnimationModel1);
			}
			if (interpolatedTime >= 1.0f) {
				ani2 = false;
				alpha2 = 0;
			}
			if (!overHalf && alpha2 < 0.3f) {
				distance2 += 0.1f;
			} else {
				distance2 += 0.4f;
			}
			hdistance2 += 0.8f;
			degree2 += 0.5f;
			if (degree2 > 45) {
				degree2 = 45;
			}
			matrix.postRotate(-degree2 * interpolatedTime);
			matrix.postTranslate(distance2, -hdistance2 / 6);
			t.setAlpha(alpha2);
			mNote2.setVisibility(View.VISIBLE);
		}
		return;
	}

	private void onLoveBtnHideAnimation(final ImageButton view) {
		if (view == null) {
			return;
		}
		if (outAlphaAnimation == null) {
			outAlphaAnimation = new AlphaAnimation(1.0f, 0f);
			outAlphaAnimation.setDuration(500);
			outAlphaAnimation.setInterpolator(new DecelerateInterpolator());
			outAlphaAnimation.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {}

				@Override
				public void onAnimationRepeat(Animation animation) {}

				@Override
				public void onAnimationEnd(Animation animation) {
					if (view != null) {
						view.clearAnimation();
						view.setImageResource(R.drawable.aurora_play_love);
					}
				}
			});
		}
		outAlphaAnimation.reset();
		view.startAnimation(outAlphaAnimation);
		return;
	}

	private void onLoveBtnScaleAnimation(final ImageButton view) {
		if (view == null) {
			return;
		}
		if (inScaleAnimation == null) {
			inScaleAnimation = new ScaleAnimation(0.3f, 1.0f, 0.3f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		}
		if (inAnimation == null) {
			inAnimation = new AlphaAnimation(0.3f, 1.f);
		}
		if (mAnimationSet == null) {
			mAnimationSet = new AnimationSet(true);
			mAnimationSet.setDuration(500);
			mAnimationSet.addAnimation(inAnimation);
			mAnimationSet.addAnimation(inScaleAnimation);
			mAnimationSet.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation arg0) {}

				@Override
				public void onAnimationRepeat(Animation arg0) {}

				@Override
				public void onAnimationEnd(Animation arg0) {
					if (view != null) {
						// view.startAnimation(inScaleAnimation2);
					}
				}
			});
			mAnimationSet.setInterpolator(new OvershootInterpolator(3.0f));
		}
		mAnimationSet.reset();
		view.startAnimation(mAnimationSet);
		return;
	}

	private void setAlphImageDrawable(Bitmap bitmap) {
		if (mAlbum == null || bitmap == null) {
			return;
		}
		int rid = -1;
		if (AuroraMusicUtil.isIndiaVersion()) {
			rid = R.drawable.default_album_india_bg;
		} else {
			rid = R.drawable.default_album_bg;
		}
		TransitionDrawable trDrawable = new TransitionDrawable(new Drawable[] { getResources().getDrawable(rid),
				new BitmapDrawable(getResources(), bitmap) });
		if (bitmap != mDefautBitmap) {
			mAlbum.setPadding(mPaddingOffset, mPaddingOffset, mPaddingOffset, mPaddingOffset);
		} else {
			mAlbum.setPadding(0, 0, 0, 0);
		}
		// add by chenhl start 20140909
		trDrawable.setId(0, 0);
		trDrawable.setId(1, 1);
		// add by chenhl end 20140909
		mAlbum.setImageDrawable(trDrawable);
		if (bitmap != mDefautBitmap) {
			mAlbum.startAnimDefaultBitmap(false);
			mAlbum.setBackgroundResource(R.drawable.default_album_image);
		} else {
			mAlbum.startAnimDefaultBitmap(true);
			mAlbum.setBackground(null);
		}
		trDrawable.startTransition(400);
		return;
	}

	private void onFirstAnimation(final ImageView view) {
		if (view == null) {
			return;
		}
		// mAnimView
		AlphaAnimation startAnimation = new AlphaAnimation(0.5f, 1f);
		RotateAnimation rotateAnimation = new RotateAnimation(90, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		TranslateAnimation tAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.3f, Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
		/* TranslateAnimation tAnimation = new TranslateAnimation(500,0,500,0); */
		final AnimationSet tSet = new AnimationSet(true);
		tSet.addAnimation(startAnimation);
		tSet.addAnimation(rotateAnimation);
		tSet.addAnimation(tAnimation);
		tSet.setDuration(1000);
		tSet.setInterpolator(new DecelerateInterpolator());
		tSet.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {}

			@Override
			public void onAnimationRepeat(Animation arg0) {}

			@Override
			public void onAnimationEnd(Animation arg0) {
				onShowFirstPic();
				mFirstAnim = true;
				return;
			}
		});
		view.startAnimation(tSet);
		return;
	}

	private void onShowFirstPic() {
		LogUtil.d(TAG, "-------onShowFirstPic----mbNetSong:" + mbNetSong);
		AuroraAlbumSongIdWrapper wrapper = new AuroraAlbumSongIdWrapper(firstAid, firstSid);
		mAlbumArtHandler.removeMessages(AURORA_GET_ALBUM_ART);
		LogUtil.d(TAG, "aaa----firstAid:" + firstAid + " firstSid:" + firstSid + " mbNetSong:" + mbNetSong);
		mAlbumArtHandler.obtainMessage(AURORA_GET_ALBUM_ART, wrapper).sendToTarget();
	}

	private void onRotationAnimation(final OTAMainPageFrameLayout view, final boolean flag) {
		if (view == null || !mPlayPause) {
			return;
		}
		mPlayPause = false;
		view.stopAnim();
		view.setAnimationListener(new OTAFrameAnimation.AnimationImageListener() {
			@Override
			public void onRepeat(int repeatIndex) {}

			@Override
			public void onFrameChange(int repeatIndex, int frameIndex, int currentTime) {}

			@Override
			public void onAnimationStart() {}

			@Override
			public void onAnimationEnd() {
				if (view != null) {
					boolean tflag = flag;
					if (tflag) {
						view.setBackgroundResource(R.drawable.aurora_play);
					} else {
						view.setBackgroundResource(R.drawable.aurora_pause);
					}
				}
			}
		});
		if (flag) {
			// 暂停
			view.setFrameAnimationList(R.drawable.pause_play_anima);
			view.startAnim();
		} else {
			// 播放
			view.setFrameAnimationList(R.drawable.play_pause_anima);
			view.startAnim();
		}
		return;
	}

	/**
	 * 渐显动画 lory add 2014.5.27
	 */
	private void AlphaAnimationIn(final View v, final long durationMillis) {
		if (v == null) {
			return;
		}
		v.clearAnimation();
		Animation mInAlphaAnimation = new AlphaAnimation(1.0f, 0f);
		mInAlphaAnimation.setDuration(durationMillis);
		mInAlphaAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}

			@Override
			public void onAnimationRepeat(Animation animation) {}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (v != null) {
					v.clearAnimation();
				}
				AuroraMusicUtil.AlphaAnimationIn(mPlayerControl, durationMillis);
			}
		});
		v.startAnimation(mInAlphaAnimation);
		v.setVisibility(View.GONE);
		return;
	}

	/**
	 * 渐隐动画 lory add 2014.5.27
	 */
	private void AlphaAnimationOut(final View v, final long durationMillis) {
		if (v == null) {
			return;
		}
		v.clearAnimation();
		Animation mOutAlphaAnimation = new AlphaAnimation(1.0f, 0.0f);
		mOutAlphaAnimation.setDuration(durationMillis);
		mOutAlphaAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}

			@Override
			public void onAnimationRepeat(Animation animation) {}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (v != null) {
					v.clearAnimation();
				}
				AuroraMusicUtil.AlphaAnimationIn(mDotLayout, durationMillis);
			}
		});
		v.startAnimation(mOutAlphaAnimation);
		v.setVisibility(View.GONE);
		return;
	}

	/*-------------animation end  ---------------------------------*/
	private int mlastValue = -1;
	private boolean misScrolling = false;
	private OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {
		@Override
		public void onPageSelected(int position) {
			mPagePos = position;
			updateDots(position);
			// LogUtil.d(TAG, "---onPageSelected position:" + position);
			if (position == 2 && mPlayerLrcView != null) {
				refreshAuroraLrcNow();
				queueAuroraNextRefreshLrc(500);
			} else {
				mHandler.removeMessages(AURORA_REFRESH_LRC);
			}
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			// LogUtil.d(TAG, "---onPageScrolled position:" + position +
			// " positionOffset:" + positionOffset + " positionOffsetPixels:" +
			// positionOffsetPixels);
			if (mbShowPagerBk && mleftCompat != null && mrightCompat != null) {
				mleftCompat.finish();
				mrightCompat.finish();
				mleftCompat.setSize(0, 0);
				mrightCompat.setSize(0, 0);
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {}
	};
	private OnClickListener mSearchLyricListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			showSearchLyricDialog();
		}
	};
	private AuroraAlertDialog mSearchLyricDialog;
	private EditText songName, artistName;
	private AuroraCheckBox modifyCheckBox;
	private int REQUESTCODE = 1000;

	private void showSearchLyricDialog() {
		try {
			if (mService == null || (mService != null && mService.getAudioId() < 0) || mItemInfo == null) {
				showToast(R.string.search_failed);
				return;
			}
		} catch (Exception e) {
			return;
		}
		if (mSearchLyricDialog == null) {
			View viewLayout = LayoutInflater.from(this).inflate(R.layout.aurora_search_lyric_dialog, null);
			songName = (EditText) viewLayout.findViewById(R.id.aurora_search_songs);
			artistName = (EditText) viewLayout.findViewById(R.id.aurora_search_artist);
			modifyCheckBox = (AuroraCheckBox) viewLayout.findViewById(R.id.aurora_check);
			songName.addTextChangedListener(mTextWatcher);
			artistName.addTextChangedListener(mTextWatcher);
			mSearchLyricDialog = new AuroraAlertDialog.Builder(this).setTitle(R.string.aurora_search_lyric).setView(viewLayout)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							AuroraMusicUtil.hideInputMethod(AuroraPlayerActivity.this, mSearchLyricDialog.getCurrentFocus());
							if (!AuroraMusicUtil.isNetWorkActive(AuroraPlayerActivity.this)) {
								Toast.makeText(AuroraPlayerActivity.this, R.string.aurora_network_error, Toast.LENGTH_SHORT).show();
								return;
							}
							long id = 0;
							try {
								if (mService != null) {
									id = mService.getAudioId();
								}
							} catch (Exception e) {
							}
							String path = null;
							if (mItemInfo != null) {
								path = mItemInfo.getFilePath();
							}
							Intent intent = new Intent(AuroraPlayerActivity.this, AuroraSearchLyricActivity.class);
							if (path != null && !TextUtils.isEmpty(path) && mItemInfo.getIsDownLoadType() == 0) {
								path = path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf("."));
								String strpath = AuroraMusicUtil.getImgPath(getApplication(), AuroraMusicUtil.MD5(path));
								intent.putExtra(AuroraSearchLyricActivity.EXTR_IMG_PATH, strpath);
							}
							intent.putExtra(AuroraSearchLyricActivity.EXTR_ID, String.valueOf(id));
							intent.putExtra(AuroraSearchLyricActivity.EXTR_NAME, songName.getText().toString());
							intent.putExtra(AuroraSearchLyricActivity.EXTR_ARTIST, artistName.getText().toString());
							intent.putExtra(AuroraSearchLyricActivity.EXTR_ALBUM, mItemInfo.getAlbumName());
							LogUtil.d(TAG, "--------------mItemInfo.getFilePath():" + mItemInfo.getFilePath());
							intent.putExtra(AuroraSearchLyricActivity.EXTR_SONGPATH, mItemInfo.getFilePath());
							startActivityForResult(intent, REQUESTCODE);
							// 修改id3信息
							if (modifyCheckBox.isChecked() && (mItemInfo != null && mItemInfo.getIsDownLoadType() == 0)) {
								ContentValues values = new ContentValues();
								values.put(MediaStore.Audio.Media.TITLE, songName.getText().toString());
								values.put(MediaStore.Audio.Media.ARTIST, artistName.getText().toString());
								getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values,
										MediaStore.Audio.Media._ID + "=" + mItemInfo.getSongId(), null);
								mItemInfo.setTitle(songName.getText().toString());
								mItemInfo.setArtistName(artistName.getText().toString());
								if (MusicUtils.sService != null) {
									try {
										MusicUtils.sService.updateCursor();
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
						}
					}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							AuroraMusicUtil.hideInputMethod(AuroraPlayerActivity.this, mSearchLyricDialog.getCurrentFocus());
						}
					}).create();
		}
		String path = mItemInfo.getFilePath();
		Boolean isHasId3 = false;
		if (path != null && !TextUtils.isEmpty(path) && mItemInfo.getIsDownLoadType() == 0) {
			path = path.substring(path.lastIndexOf("."));
			if (path.equals(".mp3")) {
				isHasId3 = true;
			}
		}
		modifyCheckBox.setChecked(true);
		modifyCheckBox.setEnabled(isHasId3);
		songName.requestFocus();
		songName.setText(mArtistName.getText());
		String tmpArtist = null;
		try {
			if (mService != null) {
				tmpArtist = mService.getArtistName();
			}
		} catch (Exception e) {
			tmpArtist = null;
		}
		if (tmpArtist == null) {
			tmpArtist = mItemInfo.getArtistName();
		}
		artistName.setText(tmpArtist);
		// 设置光标位置
		CharSequence text = songName.getText();
		if (text instanceof Spannable) {
			Spannable spanText = (Spannable) text;
			Selection.setSelection(spanText, text.length());
		}
		if (!mSearchLyricDialog.isShowing()) {
			mSearchLyricDialog.show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		LogUtil.d(TAG, "requestCode:" + requestCode + " resultCode:" + resultCode);
		if (requestCode == REQUESTCODE && resultCode == RESULT_OK && data != null) {
			String name = data.getStringExtra(AuroraSearchLyricActivity.EXTR_NAME);
			String path = data.getStringExtra(AuroraSearchLyricActivity.EXTR_PATH);
			String imgpath = data.getStringExtra(AuroraSearchLyricActivity.EXTR_IMG_PATH);
			String songid = data.getStringExtra(AuroraSearchLyricActivity.EXTR_ID);
			long id = -1;
			try {
				id = Long.valueOf(songid);
			} catch (Exception e) {
				id = -1;
			}
			if (name.equals(songName.getText().toString())) {
				showOnlineLyric(path);
				if (MusicUtils.sService != null) {
					try {
						MusicUtils.sService.notifyLrcPath(path);// 更新琐屏
						MusicUtils.sService.updateNotification();// 更新通知栏
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				// 更新图片
				if (imgpath == null) {
					return;
				}
				Bitmap bitmap = null;
				if (imgpath.equals(AuroraSearchLyricActivity.DEFUALT_IMG_URL)) {
					mHandler.removeMessages(AURORA_ALBUMBG_DECODED);
					mHandler.obtainMessage(AURORA_ALBUMBG_DECODED, null).sendToTarget();
					mHandler.removeMessages(AURORA_ALBUM_ART_DECODED);
					Message numsg = mHandler.obtainMessage(AURORA_ALBUM_ART_DECODED, 0, -1, null);
					mHandler.sendMessage(numsg);
					return;
				} else if (id > 0 && imgpath.equals(AuroraSearchLyricActivity.AURORA_ORIGINAL_IMG_URL)) {
					bitmap = MusicUtils.getAuroraArtwork(AuroraPlayerActivity.this, id, -1, mWidth, mWidth);
				} else {
					bitmap = BitmapUtil.decodeSampledBitmapFromFile(imgpath, mWidth, mWidth);
				}
				// Bitmap bitmap = BitmapFactory.decodeFile(imgpath);
				if (bitmap == null) {
					return;
				}
				Bitmap tBitmap = AuroraMusicUtil.getCircleBitmap(bitmap);
				mHandler.removeMessages(AURORA_ALBUM_ART_DECODED);
				Message numsg = mHandler.obtainMessage(AURORA_ALBUM_ART_DECODED, 0, -1, tBitmap);
				mHandler.sendMessage(numsg);
				upDateLayoutBg(bitmap);
				System.gc();
			}
		} else {
			// 新浪微薄分享
			AuroraShareXLWb.getInstance(this).authorizeCallBack(requestCode, resultCode, data);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private TextWatcher mTextWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence charsequence, int i, int j, int k) {
			// LogUtil.d(TAG,
			// "songName:"+songName.getText().length()+" artistName:"+artistName.getText().length());
			Button okButton = mSearchLyricDialog.getButton(AuroraAlertDialog.BUTTON_POSITIVE);
			if (okButton == null) {
				return;
			}
			if (songName.getText().length() == 0 || artistName.getText().length() == 0) {
				okButton.setEnabled(false);
			} else {
				okButton.setEnabled(true);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence charsequence, int i, int j, int k) {}

		@Override
		public void afterTextChanged(Editable editable) {}
	};

	private void setBgAlphDrawable(Bitmap bitmap) {
		if (mAlbum == null || bitmap == null) {
			return;
		}
		Drawable oldDrawable = mAlbum.getDrawable();
		TransitionDrawable trDrawable = null;
		BitmapDrawable oldBitmapDrawable = null;
		if (oldDrawable instanceof TransitionDrawable) {
			trDrawable = (TransitionDrawable) oldDrawable;
			oldBitmapDrawable = (BitmapDrawable) trDrawable.findDrawableByLayerId(trDrawable.getId(1));
		} else if (oldDrawable instanceof BitmapDrawable) {
			oldBitmapDrawable = (BitmapDrawable) oldDrawable;
		}
		if (trDrawable == null) {
			trDrawable = new TransitionDrawable(new Drawable[] { oldBitmapDrawable, new BitmapDrawable(getResources(), bitmap) });
			trDrawable.setId(0, 0);
			trDrawable.setId(1, 1);
		} else {
			trDrawable.setDrawableByLayerId(trDrawable.getId(0), oldBitmapDrawable);
			trDrawable.setDrawableByLayerId(trDrawable.getId(1), new BitmapDrawable(getResources(), bitmap));
		}
		if (bitmap != mDefautBitmap) {
			mAlbum.setPadding(mPaddingOffset, mPaddingOffset, mPaddingOffset, mPaddingOffset);
		} else {
			mAlbum.setPadding(0, 0, 0, 0);
		}
		mAlbum.setImageDrawable(trDrawable);
		if (bitmap != mDefautBitmap) {
			mAlbum.startAnimDefaultBitmap(false);
			mAlbum.setBackgroundResource(R.drawable.default_album_image);
		} else {
			mAlbum.startAnimDefaultBitmap(true);
			mAlbum.setBackground(null);
		}
		trDrawable.setCrossFadeEnabled(true);
		trDrawable.startTransition(1000);
		System.gc();
		return;
	}

	private void setLayoutBgAlph(Bitmap bitmap) {
		if (mbgView == null || bitmap == null) {
			return;
		}
		Drawable oldDrawable = mbgView.getDrawable();
		TransitionDrawable trDrawable = null;
		BitmapDrawable oldBitmapDrawable = null;
		if (oldDrawable instanceof TransitionDrawable) {
			trDrawable = (TransitionDrawable) oldDrawable;
			oldBitmapDrawable = (BitmapDrawable) trDrawable.findDrawableByLayerId(trDrawable.getId(1));
		} else if (oldDrawable instanceof BitmapDrawable) {
			oldBitmapDrawable = (BitmapDrawable) oldDrawable;
		}
		if (trDrawable == null) {
			trDrawable = new TransitionDrawable(new Drawable[] { oldBitmapDrawable, new BitmapDrawable(getResources(), bitmap) });
			trDrawable.setId(0, 0);
			trDrawable.setId(1, 1);
		} else {
			trDrawable.setDrawableByLayerId(trDrawable.getId(0), oldBitmapDrawable);
			trDrawable.setDrawableByLayerId(trDrawable.getId(1), new BitmapDrawable(getResources(), bitmap));
		}
		mbgView.setImageDrawable(trDrawable);
		trDrawable.setCrossFadeEnabled(true);
		trDrawable.startTransition(1000);
		System.gc();
	}

	/**
	 * search LRC
	 */
	private class LoadLrtThread extends Thread {
		private boolean isCancel = false;
		private AuroraListItem mItemInfoTemp;

		public void canCel() {
			isCancel = true;
		}

		public LoadLrtThread(AuroraListItem mItemInfo) {
			isCancel = false;
			this.mItemInfoTemp = mItemInfo;
			mHandler.obtainMessage(AURORA_GET_ALBUM_ERROR).sendToTarget();
		}

		@Override
		public void run() {
			if (isCancel || isInterrupted()) {
				return;
			}
			LogUtil.d(TAG, "---------LoadLrtThread " + mItemInfoTemp.getSongId());
			String lrcUrl = null;
			try {
				if (mService != null) {
					lrcUrl = mService.getLryFile();
				}
			} catch (Exception e) {
				e.printStackTrace();
				lrcUrl = null;
			}
			LogUtil.d(TAG, " LoadLrtThread lrcUrl1:" + lrcUrl);
			if (mItemInfoTemp == null || TextUtils.isEmpty(mItemInfoTemp.getTitle()) || TextUtils.isEmpty(mItemInfoTemp.getArtistName())) {
				return;
			}
			if (TextUtils.isEmpty(lrcUrl)) {
				lrcUrl = mItemInfoTemp.getLrcUri();
			}
			// 本地音乐只在wifi下搜词
			if (TextUtils.isEmpty(lrcUrl) && !AuroraMusicUtil.isWifiNetActvie(getApplicationContext())) {
				return;
			}
			if (TextUtils.isEmpty(lrcUrl)) {
				// get Lrc 1
				if (null != mService) {// paul modify for BUG #14791
					try {
						DownloadInfo downloadInfo = mService.queryDownloadSong(mItemInfoTemp.getTitle(), mItemInfoTemp.getArtistName());
						if (downloadInfo != null) {
							// lrcUrl = downloadInfo.getLrcUrl();
							// LogUtil.d(TAG, " --------------1 lrcUrl:" + lrcUrl + " downloadInfo:" +
							// downloadInfo.toString());
							// get Lrc 2
							if (TextUtils.isEmpty(lrcUrl)) {
								OnlineSong song = XiaMiSdkUtils.findSongByIdSync(getApplicationContext(), downloadInfo.getId(),
										AuroraMusicUtil.getOnlineSongQuality());
								if (song != null)
									lrcUrl = song.getLyric();
								LogUtil.d(TAG, " --------------2 lrcUrl:" + lrcUrl);
							}
						}
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
			if (TextUtils.isEmpty(lrcUrl)) {
				// get Lrc 3
				String artistName = mItemInfoTemp.getArtistName();
				String albumName = mItemInfoTemp.getAlbumName();
				String title = mItemInfoTemp.getTitle();
				albumName = AuroraMusicUtil.doAlbumName(mItemInfoTemp.getFilePath(), albumName);
				List<OnlineSong> list = XiaMiSdkUtils.findSongByNameSync(getApplicationContext(), title, artistName, albumName);
				if (list != null && list.size() > 0) {
					lrcUrl = list.get(0).getLyric();
					LogUtil.d(TAG, " --------------3 lrcUrl:" + lrcUrl);
				}
			}
			LogUtil.d(TAG, "lrcUrl2:" + lrcUrl);
			if (TextUtils.isEmpty(lrcUrl) || isCancel) {
				LogUtil.d(TAG, "-------------- no lrcUrl");
				return;
			}
			URL url = null;
			HttpURLConnection http = null;
			InputStream inputStream = null;
			File dirFile = new File(Globals.mLycPath);
			if (!dirFile.exists()) {
				dirFile.mkdirs();
			}
			String ext = AuroraMusicUtil.getExtFromFilename(lrcUrl).toLowerCase();
			if (TextUtils.isEmpty(ext)) {
				ext = "lrc";
			}
			String name = mItemInfoTemp.getTitle() + "_" + mItemInfoTemp.getArtistName() + "." + ext;
			if (!mItemInfoTemp.getArtistName().equalsIgnoreCase(mItemInfoTemp.getSingers()) && !TextUtils.isEmpty(mItemInfoTemp.getSingers())) {
				name = mItemInfoTemp.getTitle() + "_" + mItemInfoTemp.getArtistName() + "_" + mItemInfoTemp.getSingers() + "." + ext;
			}
			final String filepath = Globals.mLycPath + File.separator + name;
			FileOutputStream outputStream = null;
			try {
				outputStream = new FileOutputStream(filepath);
				url = new URL(lrcUrl);
				http = (HttpURLConnection) url.openConnection();
				http.setConnectTimeout(8 * 1000);
				http.connect();
				int code = http.getResponseCode();
				int offset = 0;
				if (code == HttpStatus.SC_OK) {
					inputStream = http.getInputStream();
					byte[] buffer = new byte[4096];
					while ((offset = inputStream.read(buffer)) != -1) {
						if (isCancel) {
							break;
						}
						outputStream.write(buffer, 0, offset);
					}
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							if (isCancel) {
								return;
							}
							if (mItemInfoTemp != null && mItemInfoTemp.equals(mItemInfo)) {
								showOnlineLyric(filepath);
							}
							if (mItemInfoTemp != null) {
								MusicUtils.addToAudioInfoLrc(AuroraPlayerActivity.this, mItemInfoTemp.getSongId(), mItemInfoTemp.getTitle(),
										mItemInfoTemp.getArtistName(), filepath);
							}
							try {
								if (mService != null && mItemInfoTemp != null && mItemInfoTemp.equals(mItemInfo)) {//
									mService.notifyLrcPath(filepath);
								}
							} catch (Exception e) {
							}
						}
					});
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (http != null) {
					http.disconnect();
				}
				try {
					if (outputStream != null)
						outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					if (inputStream != null)
						inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void startAlaphAnimation() {
		if (mShuffleButton == null) {
			return;
		}
		mShuffleButton.clearAnimation();
		AnimationSet set = new AnimationSet(true);
		Animation inAlphaAnimation = new AlphaAnimation(1.0f, 0.25f);
		inAlphaAnimation.setDuration(250);
		inAlphaAnimation.setInterpolator(new AccelerateInterpolator());
		inAlphaAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				auroraToggleShuffle();
				Animation outAlphaAnimation = new AlphaAnimation(0.25f, 1.0f);
				outAlphaAnimation.setInterpolator(new AccelerateInterpolator());
				outAlphaAnimation.setDuration(250);
				mShuffleButton.startAnimation(outAlphaAnimation);
			}
		});
		mShuffleButton.startAnimation(inAlphaAnimation);
	}

	private class SearchImgTask extends AsyncTask<Void, Void, String> {
		private AuroraListItem mItemInfo;

		public SearchImgTask(AuroraListItem mItemInfo) {
			super();
			this.mItemInfo = mItemInfo;
		}

		@Override
		protected String doInBackground(Void... params) {
			if (mItemInfo == null) {
				LogUtil.d(TAG, "----mItemInfo is null");
				return null;
			}
			String imageUrl = null;
			try {
				if (null != mService && mService.isOnlineSong()) {
					OnlineSong onlineSong = XiaMiSdkUtils.findSongByIdSync(getApplicationContext(), mItemInfo.getSongId(),
							AuroraMusicUtil.getOnlineSongQuality());
					if (onlineSong != null) {
						imageUrl = onlineSong.getImageUrl();
						LogUtil.d(TAG, " ---1-----------imageUrl:" + imageUrl);
					}
				}
				if (TextUtils.isEmpty(imageUrl)) {
					String artistName = mItemInfo.getArtistName();
					String albumName = mItemInfo.getAlbumName();
					String title = mItemInfo.getTitle();
					albumName = AuroraMusicUtil.doAlbumName(mItemInfo.getFilePath(), albumName);
					List<OnlineSong> list = XiaMiSdkUtils.findSongByNameSync(getApplicationContext(), title, artistName, albumName);
					if (list != null && list.size() > 0) {
						imageUrl = list.get(0).getImageUrl();
						LogUtil.d(TAG, " 1-----------imageUrl:" + imageUrl);
					}
					if (TextUtils.isEmpty(imageUrl) && null != mService) {
						DownloadInfo downloadInfo = mService.queryDownloadSong(mItemInfo.getTitle(), mItemInfo.getArtistName());
						if (downloadInfo != null) {
							imageUrl = downloadInfo.getImgUrl();
							LogUtil.d(TAG, "2-----------imageUrl:" + imageUrl);
							if (TextUtils.isEmpty(imageUrl)) {
								List<OnlineSong> lists = XiaMiSdkUtils.findSongByNameSync(getApplicationContext(), downloadInfo.getTitle(),
										downloadInfo.getArtist(), downloadInfo.getAlbum());
								if (list != null && list.size() > 0) {
									imageUrl = list.get(0).getImageUrl();
									LogUtil.d(TAG, "3-----------imageUrl:" + imageUrl);
								}
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			LogUtil.d(TAG, "-----------imageUrl:" + imageUrl);
			return imageUrl;
		}

		@Override
		protected void onPostExecute(String result) {
			if (mItemInfo == null || (mItemInfo != null && TextUtils.isEmpty(result))) {
				mPicurl = null;
				long tmpId1 = -1;
				long tmpId2 = -1;
				LogUtil.d(TAG, "----- ---- SearchImgTask 1 -------");
				mAlbumArtHandler.removeMessages(AURORA_GET_ALBUM_ARTDEFAULT);
				mAlbumArtHandler.obtainMessage(AURORA_GET_ALBUM_ARTDEFAULT, new AuroraAlbumSongIdWrapper(tmpId1, tmpId2)).sendToTarget();
			} else {
				ImageLoader.getInstance().loadImage(ImageUtil.transferImgUrl(result, 330), mOptions, new SimpleImageLoadingListener() {
					@Override
					public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
						LogUtil.d(TAG, "----- ----SearchImgTask onLoadingComplete 1 ------- imageUri:" + imageUri);
						if (isdestory) {
							return;
						}
						final Bitmap bm = loadedImage;
						saveBitmap(bm);
						if (mHandler != null) {
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									if (bm != null) {//
										Bitmap tBitmap2 = AuroraMusicUtil.getCircleBitmap(bm);
										int arg = tBitmap2 == null ? 1 : 0;
										onImgBackGroudUpdate(tBitmap2, arg);
										upDateLayoutBg(bm);
									}
									if (MusicUtils.sService != null) {
										try {
											MusicUtils.sService.updateNotification();// 更新通知栏
										} catch (RemoteException e) {
											e.printStackTrace();
										}
									}
								}
							});
						}
					}

					@Override
					public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
						LogUtil.d(TAG,
								"----- ----SearchImgTask onLoadingFailed 1 ------- imageUri:" + imageUri + ",failReason:" + failReason.getType());
						mPicurl = null;
						mAlbumArtHandler.removeMessages(AURORA_GET_ALBUM_ARTDEFAULT);
						mAlbumArtHandler.obtainMessage(AURORA_GET_ALBUM_ARTDEFAULT, new AuroraAlbumSongIdWrapper(-1, -1)).sendToTarget();
					}
				});
			}
		}

		@Override
		protected void onPreExecute() {}
	}

	private void saveBitmap(Bitmap bm) {
		LogUtil.d(TAG, "----saveBitmap:" + bm);
		if (bm != null) {
			File dir = new File(Globals.mSongImagePath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			if (mItemInfo == null) {
				return;
			}
			File file = new File(Globals.mSongImagePath + File.separator
					+ AuroraMusicUtil.MD5(mItemInfo.getTitle().trim() + mItemInfo.getArtistName().trim() + mItemInfo.getAlbumName().trim()));
			if (file.exists()) {
				LogUtil.d(TAG, "----- saveBitmap exists rt------");
				return;
			}
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(file);
				bm.compress(Bitmap.CompressFormat.PNG, 100, out);
			} catch (Exception e) {
				LogUtil.d(TAG, "----- saveBitmap fail ------");
				e.printStackTrace();
			} finally {
				if (out != null) {
					try {
						out.flush();
					} catch (Exception e2) {
					}
					try {
						out.close();
					} catch (Exception e2) {
					}
					out = null;
				}
			}
		}
		return;
	}

	private void initXiamiRadio() {
		LogUtil.d(TAG, "initXiamiRadio:" + Application.mRadiotype);
		if (Application.mRadiotype >= 0) {
			isRadioType = true;
		} else {
			isRadioType = false;
		}
	}

	private void startPlayRadio(int posion) {
		((Application) getApplication()).startPlayRadio(posion, mHandler, this);
	}
}
