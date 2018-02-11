package com.android.auroramusic.ui.lock;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.LogUtil;
import com.android.auroramusic.widget.AuroraLyricView;
import com.android.auroramusic.widget.AuroraScrollView;
import com.android.auroramusic.widget.TimeShowView;
import com.android.music.Application;
import com.android.music.MediaPlaybackService;
import com.android.music.MusicUtils;
import com.android.music.R;
import com.android.music.MusicUtils.ServiceToken;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Typeface;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;

public class AuroraMusicLockView extends RelativeLayout implements OnClickListener {

	private static final String TAG = "AuroraMusicLockView";
	private static final int AURORA_LOCK_REFRESH_LYRIC = 115;
	private static final int AURORA_LOCK_REFRESH_STATUS = 116;
	private static final int AURORA_LOCK_REFRESH_TEXTLYRIC = 117;
	private static final int AURORA_LOCK_REFRESH_VIEW = 118;
	private static final int AURORA_LOCK_ONLINELRC_REFRESH = 119;

	private Context mContext = null;
	private ImageView mAlbumArt = null;
	private ImageButton mPreButton = null;
	private ImageButton mPlayButton = null;
	private ImageButton mNextButton = null;
	private TextView mTrackTitle;
	private TextView mTrackAlbum;
	private TimeShowView mLockTime;
	// private TextView mLockTime;
	private TextView mLockDate;
	private TextView mLockDay;

	private String mTrackTitleTxt = "";
	private String mTrackAlbumTxt = "";
	private boolean mbpause = false;

	private AuroraScrollView mLockLycScrollView = null;
	private AuroraLyricView mLockLycView = null;// 歌词

	public ContentObserver mFormatChangeObserver;
	public BroadcastReceiver mIntentReceiver;
	private Calendar mCalendar;
	private final Handler mHandler = new Handler();

	private final SimpleDateFormat sdf = new SimpleDateFormat("MM-dd EEE");// "yyyy'.'MM'.'dd"
	private final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy'-'MM'-'dd''EEE");// yyyy-MM-dd
																						// EEE
	private final static String M12 = "hh:mm";
	private final static String M24 = "kk:mm";
	private String mFormat;
	private String[] mAmPm;
	private String mFilePath;

	private static final String AURORA_DEFAULT_NUMBER_FONT_PATH = "system/fonts/Roboto-Light.ttf";
	private Typeface m_auroraNumberTf;

	private static final String AURORA_SERVICECMD = "com.android.music.musicservicecommand";
	private static final String AURORA_CMDNAME = "command";
	private static final String AURORA_CMDSTOP = "stop";
	private static final String AURORA_CMDPAUSE = "pause";
	private static final String AURORA_CMDPLAY = "play";
	private static final String AURORA_CMDPREV = "previous";
	private static final String AURORA_CMDNEXT = "next";
	private static final String AURORA_CMDTOGGLEPAUSE = "togglepause";
	private boolean isShowLrc=false;
	private ServiceToken mToken;
	private int m_Num = 0;
	private String mlrcPath = null;
	private boolean bOnlineMusic = false;

	private static final String LOCK_SYSTEM_SEPARATOR = System.getProperty("file.separator");
	private static String lockLycPath = Environment.getExternalStorageDirectory().getPath() + "/Music/auroramusic" + LOCK_SYSTEM_SEPARATOR + "/lyric";
	private String mTrackName = null;
	private String mArtistName = null;
	private boolean mbNet = false;

	public View getAuroraLockView(Context context) {
		m_Num = 0;
		return new AuroraMusicLockView(context);
	}

	public AuroraMusicLockView(Context context) {
		this(context, null, 0);
	}

	public AuroraMusicLockView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return super.onInterceptTouchEvent(ev);
	}

	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}
	
	private PowerManager pm;

	public AuroraMusicLockView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (context == null) {
			return;
		}

		m_Num = 0;

		this.mContext = context;
		LayoutInflater.from(mContext).inflate(R.layout.aurora_lockview, this);

		try {
			m_auroraNumberTf = Typeface.createFromFile(AURORA_DEFAULT_NUMBER_FONT_PATH);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(pm==null){
			pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		}

		initView();
		refreshLockDate();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		if (MusicUtils.sService == null) {
			mLockHandler.removeMessages(AURORA_LOCK_REFRESH_VIEW);
			mLockHandler.sendEmptyMessageDelayed(AURORA_LOCK_REFRESH_VIEW, 200);
			return;
		}

		setViewText();
		onRefreshPosition(false);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		mLockHandler.removeCallbacksAndMessages(null);
		MusicUtils.unbindFromService(mToken);
		unregisterComponent();
		m_Num = 0;
	}

	

	private void initView() {
		mToken = MusicUtils.bindToService(mContext);
		mCalendar = Calendar.getInstance();

		mLockLycView = (AuroraLyricView) findViewById(R.id.lock_lyric_view);
		mLockLycScrollView = (AuroraScrollView) findViewById(R.id.lock_lyric_scrollview);
		mLockLycScrollView.setViewCanScroll(true);
		mLockLycView.setHideText(true);
		mLockLycView.setHasLyric(true);

		mLockTime = (TimeShowView) findViewById(R.id.lock_time);
		mLockDate = (TextView) findViewById(R.id.lock_date);
		mLockDay = (TextView) findViewById(R.id.lock_day);

		mTrackTitle = (TextView) findViewById(R.id.lock_music_line1);
		mTrackTitle.setSelected(true); // enable marquee
		mTrackAlbum = (TextView) findViewById(R.id.lock_music_line2);
		mTrackAlbum.setSelected(true);
		mAlbumArt = (ImageView) findViewById(R.id.lock_bg_image);
		mPreButton = (ImageButton) findViewById(R.id.lock_pre);
		mPreButton.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					((ImageButton) v).setImageDrawable(getResources().getDrawable(R.drawable.lock_pre_select));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					((ImageButton) v).setImageDrawable(getResources().getDrawable(R.drawable.lock_pre));
					onAuroraClick(v);
				}
				return true;
			}
		});

		mPlayButton = (ImageButton) findViewById(R.id.lock_playpause);
		mPlayButton.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					onAuroraClick(v);
				}
				return true;
			}
		});

		mNextButton = (ImageButton) findViewById(R.id.lock_next);
		mNextButton.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					((ImageButton) v).setImageDrawable(getResources().getDrawable(R.drawable.lock_next_select));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					((ImageButton) v).setImageDrawable(getResources().getDrawable(R.drawable.lock_next));
					onAuroraClick(v);
				}
				return true;
			}
		});

		mAmPm = new DateFormatSymbols().getAmPmStrings();
		registerComponent();

		
		
		return;
	}

	private void setViewText() {
		if (MusicUtils.sService == null) {
			return;
		}
		try {
			String ta1 = MusicUtils.sService.getTrackName();
			String ta2 = MusicUtils.sService.getArtistName();
			String ta3 = MusicUtils.sService.getAlbumName();
			String ta4 = ta2 + " · <" + ta3 + ">";
			if(!MusicUtils.sService.isOnlineSong()){
				String filePath = MusicUtils.sService.getFilePath();
				ta3 = AuroraMusicUtil.doAlbumName(filePath, ta3);
				ta4 = ta2 + " · " + ta3 + "";
			}
						
			boolean ta5 = MusicUtils.sService.isPlaying();
			if (mTrackTitle != null) {
				mTrackTitle.setText(ta1);
				mTrackName = ta1;
			}

			if (mTrackAlbum != null) {
				mTrackAlbum.setText(ta4);
			}

			bOnlineMusic = MusicUtils.sService.isOnlineSong();
			mArtistName = ta2;
			if (mPlayButton != null) {
				if (ta5) {
					mPlayButton.setImageResource(R.drawable.lock_pause);
				} else {
					mPlayButton.setImageResource(R.drawable.lock_play);
				}
			}

			if (MusicUtils.sService.getRadioType()) {
				mPreButton.setEnabled(false);
			} else {
				mPreButton.setEnabled(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return;
	}

	private void registerComponent() {

		setDateFormat();

		if (mIntentReceiver == null) {
			mIntentReceiver = new AuroraTimeChangedReceiver(this);
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_TIME_TICK);
			filter.addAction(Intent.ACTION_TIME_CHANGED);
			filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
			mContext.registerReceiver(mIntentReceiver, filter);
		}

		if (mFormatChangeObserver == null) {
			mFormatChangeObserver = new AuroraFormatChangeObserver(this);
			mContext.getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true, mFormatChangeObserver);
		}

		IntentFilter f = new IntentFilter();
		f.addAction(MediaPlaybackService.PLAYSTATE_CHANGED);
		f.addAction(MediaPlaybackService.META_CHANGED);
		f.addAction(MediaPlaybackService.AURORALRC_CHANGED);
		mContext.registerReceiver(mLockStatusListener, f);

		updateLockTime();
		return;
	}

	private void unregisterComponent() {

		if (mIntentReceiver != null) {
			mContext.unregisterReceiver(mIntentReceiver);
			mIntentReceiver = null;
		}

		mContext.unregisterReceiver(mLockStatusListener);

		if (mFormatChangeObserver != null) {
			mContext.getContentResolver().unregisterContentObserver(mFormatChangeObserver);
			mFormatChangeObserver = null;
		}

		return;
	}

	private BroadcastReceiver mLockStatusListener = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			String t1 = intent.getStringExtra("artist");
			String t2 = intent.getStringExtra("album");
			String t3 = intent.getStringExtra("track");
			String t4 = intent.getStringExtra("filepath");// playing
			boolean t5 = intent.getBooleanExtra("playing", false);
			bOnlineMusic = intent.getBooleanExtra("isnet", false);

			boolean flag = false;
			if (mTrackName != null && mArtistName != null && mTrackName.equalsIgnoreCase(t3) && mArtistName.equalsIgnoreCase(t1) && bOnlineMusic == mbNet) {
				flag = true;
			}

			String[] info = new String[3];
			mlrcPath = t4;
			mTrackName = t3;
			mArtistName = t1;
			mbNet = bOnlineMusic;
			info[0] = t3;
			info[1] = t1 + " · <" + t2 + ">";
			info[2] = String.valueOf(t5);
			int bmeta = 0;
			if (action.equals(MediaPlaybackService.META_CHANGED)) {
				bmeta = 1;
			} else if (action.equals(MediaPlaybackService.PLAYSTATE_CHANGED)) {
			} else if (action.equals(MediaPlaybackService.AURORALRC_CHANGED)) {
				mLockHandler.removeMessages(AURORA_LOCK_ONLINELRC_REFRESH);
				mLockHandler.obtainMessage(AURORA_LOCK_ONLINELRC_REFRESH).sendToTarget();
				mContext.removeStickyBroadcast(intent);
				return;
			}
			mLockHandler.removeMessages(AURORA_LOCK_REFRESH_STATUS);
			mLockHandler.obtainMessage(AURORA_LOCK_REFRESH_STATUS, bmeta, -1, info).sendToTarget();
			mContext.removeStickyBroadcast(intent);
		}
	};

	private boolean findOnlineLyric(String filename) {
		if (filename == null) {
			return false;
		}
    	
    	try {
    		File file = new File(filename);
    		if (file.exists()) {
    			mLockLycView.read(filename);
				mLockHandler.removeMessages(AURORA_LOCK_REFRESH_LYRIC);
				mLockHandler.obtainMessage(AURORA_LOCK_REFRESH_LYRIC).sendToTarget();
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	private void onRefreshPosition(boolean is) {
		try {
			if (MusicUtils.sService != null) {
				if (!is) {
					mlrcPath = MusicUtils.sService.getLrcUri();
				}
				if (mlrcPath != null && !mlrcPath.isEmpty() && findOnlineLyric(mlrcPath)) {
					mLockHandler.removeMessages(AURORA_LOCK_REFRESH_TEXTLYRIC);
					mLockHandler.obtainMessage(AURORA_LOCK_REFRESH_TEXTLYRIC).sendToTarget();
				} else {
					mLockLycView.setHasLyric(false);
					mLockHandler.removeMessages(AURORA_LOCK_REFRESH_TEXTLYRIC);
				}

			} else {
				mLockHandler.removeMessages(AURORA_LOCK_REFRESH_TEXTLYRIC);
			}
		} catch (Exception e) {
			Log.i(TAG, "zll ---- onRefreshPosition --- 4 ");
			e.printStackTrace();
		}
		return;
	}

	private void onAuroraClick(View v) {
		LogUtil.d(TAG, "onAuroraClick");
		String command = null;
		if (v == mPreButton) {
			command = MediaPlaybackService.CMDPREVIOUS;
		} else if (v == mPlayButton) {
			command = MediaPlaybackService.CMDTOGGLEPAUSE;
		} else if (v == mNextButton) {
			command = MediaPlaybackService.CMDNEXT;
		}

		if (command != null) {
			Intent intent = new Intent(mContext, MediaPlaybackService.class);
			intent.putExtra(MediaPlaybackService.CMDNAME, command);
			mContext.startService(intent);
		}
		return;
	}

	@Override
	public void onClick(View v) {
		LogUtil.d(TAG, "onClick(View v) MediaPlaybackService");
		String command = null;
		if (v == mPreButton) {
			command = MediaPlaybackService.CMDPREVIOUS;
		} else if (v == mPlayButton) {
			command = MediaPlaybackService.CMDTOGGLEPAUSE;
		} else if (v == mNextButton) {
			command = MediaPlaybackService.CMDNEXT;
		}

		if (command != null) {
			Intent intent = new Intent(mContext, MediaPlaybackService.class);
			intent.putExtra(MediaPlaybackService.CMDNAME, command);
			mContext.startService(intent);
		}

		return;
	}

	private void refreshLockDate() {

		if (mContext != null) {
			long time = System.currentTimeMillis();
			String weekStr = (String) DateUtils.formatDateTime(mContext, time, DateUtils.FORMAT_SHOW_WEEKDAY);
			if (mLockDay != null) {
				mLockDay.setText(weekStr);
			}

			String ti = sdf.format(new Date());
			if (mLockDate != null) {
				mLockDate.setText(ti.substring(0, 5));
			}
		} else {
			String ti = sdf.format(new Date());

			if (mLockDate != null) {
				mLockDate.setText(ti.substring(0, 5));
			}

			if (mLockDay != null) {
				mLockDay.setText(ti.substring(6, ti.length()));
			}
		}

		return;
	}

	private void setDateFormat() {
		mFormat = android.text.format.DateFormat.is24HourFormat(mContext) ? M24 : M12;
	}

	private void updateLockTime() {
		mCalendar.setTimeInMillis(System.currentTimeMillis());

		if (mFormat == null) {
			Log.i(TAG, "zll --- updateTime null");
			return;
		}

		if (mLockTime != null) {
			mLockTime.updateTime(mFormat);
		}
		refreshLockDate();
	}

	/*
	 * 监听12小时制Or24小时制变化,即监听URI为Settings.System.CONTENT_URI的数据变化
	 */
	private class AuroraFormatChangeObserver extends ContentObserver {

		public AuroraFormatChangeObserver(AuroraMusicLockView status) {
			super(new Handler());
		}

		@Override
		public void onChange(boolean selfChange) {
			try {
				setDateFormat();
				updateLockTime();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class AuroraTimeChangedReceiver extends BroadcastReceiver {
		private WeakReference<AuroraMusicLockView> mStatusViewManager;

		public AuroraTimeChangedReceiver(AuroraMusicLockView status) {
			mStatusViewManager = new WeakReference<AuroraMusicLockView>(status);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			final boolean timezoneChanged = intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED);
			final AuroraMusicLockView status = mStatusViewManager.get();

			if (status != null) {
				status.mHandler.post(new Runnable() {
					public void run() {
						if (timezoneChanged) {
							status.mCalendar = Calendar.getInstance();
						}
						status.updateLockTime();
					}
				});
			} else {
				try {
					status.mContext.unregisterReceiver(this);
				} catch (RuntimeException e) {
					e.printStackTrace();
				}
			}
		}
	};

	private final Handler mLockHandler = new Handler() {

		@SuppressWarnings("deprecation")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AURORA_LOCK_REFRESH_LYRIC:
				if (mLockLycView != null) {
					mLockLycView.setHasLyric(true);
					mLockLycView.setTextsEx(mLockLycScrollView);
				}
				break;

			case AURORA_LOCK_REFRESH_STATUS:

				String[] tinfo = (String[]) (msg.obj);
				int ismeta = msg.arg1;
				if (tinfo != null) {
					if (tinfo[0] != null) {
						mTrackTitleTxt = tinfo[0];
						if (mTrackTitle != null) {
							mTrackTitle.setText(mTrackTitleTxt);
						}
					}

					if (tinfo[1] != null) {
						mTrackAlbumTxt = tinfo[1];
						if (mTrackAlbum != null) {
							mTrackAlbum.setText(mTrackAlbumTxt);
						}
					}
					try {
						if (MusicUtils.sService != null) {
							boolean mbpause = MusicUtils.sService.isPlaying();
							if (mbpause) {
								mPlayButton.setImageResource(R.drawable.lock_pause);
							} else {
								mPlayButton.setImageResource(R.drawable.lock_play);
							}
						}
					} catch (Exception e) {

					}

				}
				if (ismeta == 1) {
					onRefreshPosition(false);
				}

				break;

			case AURORA_LOCK_REFRESH_TEXTLYRIC:
				int delay = 500;
				if (pm.isScreenOn()) {
					if (MusicUtils.sService != null && mLockLycView != null) {
						try {
							float position = MusicUtils.sService.position();
							if (MusicUtils.sService.isPlaying()) {
								delay = mLockLycView.setCurrentIndex((int) position);
							}else {
								mLockLycView.setCurrentIndex((int) position);
								delay=500;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				queueAuroraLycRefresh(delay);
				break;

			case AURORA_LOCK_ONLINELRC_REFRESH:
				onRefreshPosition(true);
				break;

			case AURORA_LOCK_REFRESH_VIEW:
				if (MusicUtils.sService == null) {
					m_Num++;
					if (m_Num < 10) {
						mLockHandler.sendEmptyMessageDelayed(AURORA_LOCK_REFRESH_VIEW, 200);
					}
				} else {
					m_Num = 0;
					setViewText();
					onRefreshPosition(false);
				}
				break;

			default:
				break;
			}
		}
	};

	private void queueAuroraLycRefresh(long delay) {
		Message msg = mLockHandler.obtainMessage(AURORA_LOCK_REFRESH_TEXTLYRIC);
		mLockHandler.removeMessages(AURORA_LOCK_REFRESH_TEXTLYRIC);
		mLockHandler.sendMessageDelayed(msg, delay);
	}

	private class LyricLockThread extends Thread {
		private String filename = null;

		public LyricLockThread(String filename) {
			this.filename = filename;
		}

		@Override
		public void run() {
			if (mLockLycView != null) {
				mLockHandler.removeMessages(AURORA_LOCK_REFRESH_LYRIC);
				mLockLycView.read(filename);
				mLockHandler.obtainMessage(AURORA_LOCK_REFRESH_LYRIC).sendToTarget();
			}
		}
	}

}
