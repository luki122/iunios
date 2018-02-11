package com.aurora.timer;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;

import aurora.app.AuroraActivity;
import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.sip.SipProfile;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.db.AlarmAddUpHelp;
import com.android.deskclock.AlarmClock;
import com.android.deskclock.AlarmPreference;
import com.android.deskclock.AlarmReceiver;
import com.android.deskclock.Alarms;
import com.android.deskclock.R;
import com.aurora.stopwatch.StopWatchFragment;
import com.aurora.utils.NotificationOperate;
import com.aurora.utils.GnRingtoneUtil;

import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Xml;
import android.util.AttributeSet;
import aurora.widget.*;
import android.app.NotificationManager;
import com.aurora.AnimationView.*;
import com.aurora.AnimationView.AnimationState.OnHourGlassAnimationCompleteListener;
import com.aurora.AnimationView.AuroraAnimationDrawable.OnFrameAnimationCompleteListener;
import aurora.widget.AuroraNumberPicker.OnValueChangeListener;

import android.graphics.drawable.AnimationDrawable;
import android.view.GnSurface;
import com.aurora.utils.Blur;
import android.graphics.drawable.BitmapDrawable;
import java.io.InputStream;
import java.util.Observable;
import java.util.Observer;
import android.util.Log;
import aurora.widget.*;
import aurora.app.*;
import android.os.MessageQueue.IdleHandler;

/**
 * the fragment of chronometer
 * 
 * @author liujianyu
 * 
 */
public class TimerFragment extends Fragment implements OnClickListener,
		Runnable,  IdleHandler{

	public static final String TAG = "TimerFragment";
	public static final int TABNUM = 101;
	private String contentTitle;
	private String contentText;
	private View startBtnContainer;
	private View pauseBtnContainer;
	private View restartBtnContainer;
	private View cancelBtnContainer;
	private View endBtnContainer;
	private ImageView startBtn;
	private ImageView pauseBtn;
	private ImageView restartBtn;
	private ImageView cancelBtn;
	private ImageView endBtn;
	private TextView startBtnText;
	private TextView pauseBtnText;
	private TextView restartBtnText;
	private TextView cancelBtnText;
	private TextView endBtnText;
	private float degrees;
	private String musicName;
	private long period;
	public long lastSystemTime;

	private static final int ALARM_RINGTONE = 1;

	private TimePicker mTimePicker;
	private View mHeadLayout;
	private View mTimePickerContainer;
	private AuroraNumberPicker mTimeHourPicker, mTimeMinPicker;
	private AuroraNumberPicker mTimeSecPicker;
	private TextView chronometeMusic;
	private View AnimationContainer;
	private AuroraHourGlassView guangquan1;
	private ImageView water;
	private boolean normalExit;
	private boolean mStartRoundToGlassAnimation = true;
	private boolean mStartGlassToRoundAnimation = false;

	public static TimerFragment instanse;

	private volatile boolean mStopAnimation = false;

	public static boolean timeChanged = false;
	
	public static final String DEFAULT_TIMEALARM = "content://media/internal/audio/media/13";

	/**
	 * The countdown time
	 */
	private long time;

	// private String mDefaultUri;

	/**
	 * The countdown time remaining
	 */
	public long timeLeft;

	/**
	 * Countdown to stop the time
	 */
	public long stopTime;

	private Handler handler;

	/**
	 * state : stop,running,pause,init
	 */

	public static final int STATE_STOP = 0;
	public static final int STATE_RUNNING = STATE_STOP + 1;
	public static final int STATE_PAUSE = STATE_STOP + 2;
	public static final int STATE_INIT = STATE_STOP + 3;
	public static final int STATE_END = STATE_STOP + 4;

	private int state = STATE_INIT;
	private int realState = STATE_INIT;
	/**
	 * dismiss the alert dialog or not
	 */
	public static boolean alertDismiss = true;
	public static final String ALARM_ALERT_ACTION = "com.android.intent.chronometer.ALARM.ALERT";
	public static final String COUNTDOWN_TIME = "countdownTime";
	public static final String SHOW_TITLE = "show_title";

	/**
	 * Screen constant light switch
	 */

	private WakeLock wakeLock;

	/**
	 * Screen constant light switch state, is enabled by default, the screen
	 * bright state
	 */
	private boolean isCheck = true;

	private SharedPreferences sharedPreferences;

	private AuroraActivity mActivity;
	private View mRootView;

	private View chronometerTimeContainer;
	private TextView chronometerTime;
	private TextView chronometerTimeSecond;
	// private TextView chronometerTimeSecondDisappear;
	private static boolean mStartMusic = true;

	private final int ORIGIN = 60;
	private int mOriginTime = 3;
	private long mFirstOriginTime = -1;

	private ImageView mImageviewSet;

	private DisplayMetrics mDisplayMetrics;

	private AlarmManager am;
	PendingIntent sender;
	
	OnValueChangeListener mValueChangedListener;
	boolean mIsStartBtnEnabled = true;
	boolean mIsAlertShowDialog = true;
	 private PowerManager powerManager=null;
		private WakeLock wakeLocked=null;
		public WakeLock getWakeLock(){
			 
			 powerManager = (PowerManager) getActivity().getSystemService(getActivity().POWER_SERVICE);    
			    wakeLocked = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Lock");    
			 
			 return wakeLocked;
		 }


	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		initState();
		am = (AlarmManager) mActivity.getSystemService(Context.ALARM_SERVICE);
		mAinmationController = new AinmationController();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.v(TAG, "onCreateView");
		mRootView = inflater.inflate(R.layout.timer, container, false);
		initResources();
//		if (state == STATE_RUNNING
//				&& !sharedPreferences.getBoolean(
//						StopWatchFragment.iS_LOCALE_CHANED, false)) {
//			am.cancel(sender);
//			handler.post(this);
//			NotificationOperate.createNotifaction(mActivity, TABNUM,
//					contentTitle, contentText);
//			ChronometerAlarmAlertWakeLock.acquireCpuWakeLock(mActivity,
//					ChronometerAlarmAlertWakeLock.CHRONMENTER);
//		}
		saveLocalChangedData();
		return mRootView;
	}
	
	public static final String CLOCK_ACTION_BAR_TITLE_FONT = "/system/fonts/title.ttf";
	TextView titleText;

	private void initResources() {
		Log.v(TAG, "initResources");
		
		
		titleText = (TextView)mRootView.findViewById(R.id.titleText);
//		Typeface auroraTitleFace = Typeface.createFromFile(CLOCK_ACTION_BAR_TITLE_FONT);
//        titleText.setTypeface(auroraTitleFace);
		
		AnimationContainer = (View) mRootView
				.findViewById(R.id.aurora_chronometer_Animation);

		int textcolor = getResources().getColor(
				R.color.aurora_chronometer_color);
		mHeadLayout = mRootView.findViewById(R.id.head_layout);
		mTimePickerContainer = mRootView
				.findViewById(R.id.aurora_time_picker_container);
		mTimeHourPicker = (AuroraNumberPicker) mRootView
				.findViewById(R.id.aurora_time_picker_hour);
		mTimeMinPicker = (AuroraNumberPicker) mRootView
				.findViewById(R.id.aurora_time_picker_minute);
		mTimeSecPicker = (AuroraNumberPicker) mRootView
				.findViewById(R.id.aurora_time_picker_second);
		mTimeHourPicker.setMaxValue(99);
		mTimeHourPicker.setMinValue(0);
		mTimeHourPicker.setValue(0);
		mTimeHourPicker.setTextColor(mActivity.getResources().getColor(R.color.aurora_number_picker_text_color2), Color.WHITE, mActivity.getResources().getColor(R.color.aurora_number_picker_text_color));
		mTimeHourPicker.setFormatter(AuroraNumberPicker.TWO_DIGIT_FORMATTER);
		mTimeHourPicker.setLabel(mActivity.getResources().getString(R.string.label_hour));
		mTimeMinPicker.setMaxValue(60);
		mTimeMinPicker.setMinValue(0);
		mTimeMinPicker.setValue(15);
		mTimeMinPicker.setTextColor(mActivity.getResources().getColor(R.color.aurora_number_picker_text_color2), Color.WHITE, mActivity.getResources().getColor(R.color.aurora_number_picker_text_color));
		mTimeMinPicker.setFormatter(AuroraNumberPicker.TWO_DIGIT_FORMATTER);
		mTimeMinPicker.setLabel(mActivity.getResources().getString(R.string.label_minute));
		mTimeSecPicker.setMaxValue(60);
		mTimeSecPicker.setMinValue(0);
		mTimeSecPicker.setValue(0);
		mTimeSecPicker.setTextColor(mActivity.getResources().getColor(R.color.aurora_number_picker_text_color2), Color.WHITE, mActivity.getResources().getColor(R.color.aurora_number_picker_text_color));
		mTimeSecPicker.setFormatter(AuroraNumberPicker.TWO_DIGIT_FORMATTER);
		mTimeSecPicker.setLabel(mActivity.getResources().getString(
				R.string.label_second));
		
		
		mValueChangedListener = new OnValueChangeListener() {
			@Override
			public void onValueChange(AuroraNumberPicker picker, int oldValue, int newValue) {
				Log.v(TAG, "-Chronometer--onValueChange mTimeHourPicker.getValue()=" + mTimeHourPicker.getValue() + " mTimeMinPicker.getValue()= " + mTimeMinPicker.getValue());
				if(mTimeHourPicker.getValue() == 0 && mTimeMinPicker.getValue() == 0 && mTimeSecPicker.getValue() == 0) {
					mIsStartBtnEnabled = false;
//					startBtnText.setTextColor(mActivity.getResources().getColor(R.color.aurora_gray_text_color));
					startBtn.setEnabled(false);
					startBtnText.setEnabled(false);
				} else {
					mIsStartBtnEnabled = true;
//					startBtnText.setTextColor(mActivity.getResources().getColor(R.color.aurora_btn_color));
					startBtn.setEnabled(true);
					startBtnText.setEnabled(true);
				}
			}
	    };
		mTimeHourPicker.setOnValueChangedListener(mValueChangedListener);
		mTimeMinPicker.setOnValueChangedListener(mValueChangedListener);
		mTimeSecPicker.setOnValueChangedListener(mValueChangedListener);


		guangquan1 = (AuroraHourGlassView) mRootView
				.findViewById(R.id.guangquan1);
		guangquan1.setTotalTime(time);
		guangquan1.setTimeleft(timeLeft);
		water = (ImageView) mRootView.findViewById(R.id.water);

		chronometerTimeContainer = mRootView
				.findViewById(R.id.chronometer_time_container);
		;
		chronometerTime = (TextView) mRootView
				.findViewById(R.id.chronometer_time);
		chronometerTimeSecond = (TextView) mRootView
				.findViewById(R.id.chronometer_time_second);
		// chronometerTimeSecondDisappear = (TextView) mRootView
		// .findViewById(R.id.chronometer_time_second_disappear);

		chronometeMusic = (TextView) mRootView
				.findViewById(R.id.chronomete_music);

		chronometeMusic.setText(mActivity.getSharedPreferences("Chronometer",
				Activity.MODE_PRIVATE).getString("ChronometerMusicName",
				mActivity.getResources().getString(R.string.default_ringtone)));

		contentTitle = this.getText(R.string.chronometerNotiTitle).toString();
		contentText = this.getText(R.string.chronometerNotiText).toString();

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				Log.v(TAG, "-Chronometer--handleMessage start!---msg" + msg.what);

				switch (msg.what) {
				case STATE_INIT:
					refreshUI(STATE_INIT);
					break;
				case STATE_PAUSE:
					refreshUI(STATE_PAUSE);
					break;
				case STATE_RUNNING:
					refreshUI(STATE_RUNNING);
					break;
				case STATE_STOP:
					refreshUI(STATE_STOP);
				case STATE_END:
					refreshUI(STATE_END);
					break;
				default:
					break;
				}

			}
		};

		startBtnContainer = mRootView
				.findViewById(R.id.chronometer_start_btn_container);
		pauseBtnContainer = mRootView
				.findViewById(R.id.chronometer_pause_btn_container);
		restartBtnContainer = mRootView
				.findViewById(R.id.chronometer_restart_btn_container);
		cancelBtnContainer = mRootView
				.findViewById(R.id.chronometer_cancel_btn_container);
		endBtnContainer = mRootView
				.findViewById(R.id.chronometer_end_btn_container);
		startBtn = (ImageView) mRootView
				.findViewById(R.id.chronometer_start_btn);
		pauseBtn = (ImageView) mRootView
				.findViewById(R.id.chronometer_pause_btn);
		restartBtn = (ImageView) mRootView
				.findViewById(R.id.chronometer_restart_btn);
		cancelBtn = (ImageView) mRootView
				.findViewById(R.id.chronometer_cancel_btn);
		endBtn = (ImageView) mRootView.findViewById(R.id.chronometer_end_btn);
		startBtn.setOnClickListener(this);
		pauseBtn.setOnClickListener(this);
		restartBtn.setOnClickListener(this);
		cancelBtn.setOnClickListener(this);
		endBtn.setOnClickListener(this);
//		startBtn.setOnTouchListener(mOnTouchListener);
//		pauseBtn.setOnTouchListener(mOnTouchListener);
//		restartBtn.setOnTouchListener(mOnTouchListener);
//		cancelBtn.setOnTouchListener(mOnTouchListener);
//		endBtn.setOnTouchListener(mOnTouchListener);
		startBtnText = (TextView) mRootView
				.findViewById(R.id.chronometer_start_btn_text);
		pauseBtnText = (TextView) mRootView
				.findViewById(R.id.chronometer_pause_btn_text);
		restartBtnText = (TextView) mRootView
				.findViewById(R.id.chronometer_restart_btn_text);
		cancelBtnText = (TextView) mRootView
				.findViewById(R.id.chronometer_cancel_btn_text);
		endBtnText = (TextView) mRootView
				.findViewById(R.id.chronometer_end_btn_text);
//		if(!mIsStartBtnEnabled) {
//			startBtnText.setTextColor(mActivity.getResources().getColor(R.color.aurora_gray_text_color));	
//		}

		mImageviewSet = (ImageView) mRootView.findViewById(R.id.iv_set);
		mImageviewSet.setOnClickListener(this);
		
		// Blur.showBgBlurView(mActivity, mRootView);
		mIsInitAnimationState = true;
		mIdleAnim = new HourGlassIdleAnimationState(mActivity, handler, guangquan1);	
		guangquan1.setAnimState(mIdleAnim);
		if(state == STATE_INIT) {
	        sThreadHandler  = new Handler();
	        sThreadHandler.getLooper().myQueue().addIdleHandler(this);
	        sThreadHandler.obtainMessage().sendToTarget();
		} else {			
			initAnimationRes();
			mRunAnim =  new HourGlassRunningAnimationState(mActivity, handler, guangquan1, water);	
			mPauseAnim = new HourGlassRunningPauseAnimationState(mActivity, handler, guangquan1);
		}
		
		mNormalTextColor = startBtnText.getTextColors();		
	}

//	OnTouchListener mOnTouchListener = new OnTouchListener() {
//		public boolean onTouch(View v, MotionEvent event) {
//			TextView BtnText = null;
//			if(!mIsStartBtnEnabled && state == STATE_INIT) {
//				return true;
//			}
//
//			if (v.getId() == R.id.chronometer_start_btn) {
//				BtnText = (TextView) mRootView
//						.findViewById(R.id.chronometer_start_btn_text);
//			} else if (v.getId() == R.id.chronometer_pause_btn) {
//				BtnText = (TextView) mRootView
//						.findViewById(R.id.chronometer_pause_btn_text);
//			} else if (v.getId() == R.id.chronometer_restart_btn) {
//				BtnText = (TextView) mRootView
//						.findViewById(R.id.chronometer_restart_btn_text);
//			} else if (v.getId() == R.id.chronometer_cancel_btn) {
//				BtnText = (TextView) mRootView
//						.findViewById(R.id.chronometer_cancel_btn_text);
//			} else if (v.getId() == R.id.chronometer_end_btn) {
//				BtnText = (TextView) mRootView
//						.findViewById(R.id.chronometer_end_btn_text);
//			}
//			if (BtnText != null) {
//				if (event.getAction() == MotionEvent.ACTION_UP) {
//					BtnText.setTextColor(mActivity.getResources().getColor(
//							R.color.aurora_btn_color));
//				} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
//					BtnText.setTextColor(mActivity.getResources().getColor(
//							R.color.aurora_btn_press_color));
//				}
//			}
//
//			return false;
//		}
//	};

	@Override
	public void onClick(View v) {

		// if (state == STATE_INIT && v.getId() != R.id.iv_set) {
		// return;
		// }
		removeAllMessages();

		switch (v.getId()) {
		case R.id.chronometer_start_btn:
			AlarmAddUpHelp.getInstance(mActivity).add(AlarmAddUpHelp.AlarmAddUpType.ITEM_TAG_TIMER);//启用一次计时器，添加一次
			onStartClick();
			ChronometerAlarmAlertWakeLock.acquireCpuWakeLock(mActivity,
					ChronometerAlarmAlertWakeLock.CHRONMENTER);
			break;
		case R.id.chronometer_pause_btn:
			onPauseClick();
			ChronometerAlarmAlertWakeLock
					.releaseCpuLock(ChronometerAlarmAlertWakeLock.CHRONMENTER);
			break;
		case R.id.chronometer_restart_btn:
			onReStartClick();
			ChronometerAlarmAlertWakeLock.acquireCpuWakeLock(mActivity,
					ChronometerAlarmAlertWakeLock.CHRONMENTER);
			break;
		case R.id.chronometer_cancel_btn:
			onResetClick();
			ChronometerAlarmAlertWakeLock
					.releaseCpuLock(ChronometerAlarmAlertWakeLock.CHRONMENTER);
			break;
		case R.id.iv_set:
			// Intent intent = new Intent(mActivity,
			// GnChronometerSettingActivity.class);
			onMusicSelect();
			break;
		case R.id.chronometer_end_btn:
			onEndClick();
			ChronometerAlarmAlertWakeLock
					.releaseCpuLock(ChronometerAlarmAlertWakeLock.CHRONMENTER);
			break;
		default:
			break;
		}

	}

	private void onMusicSelect() {
		// Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
		Intent intent = new Intent(
				"gn.com.android.audioprofile.action.RINGTONE_PICKER");
		intent.putExtra(
				RingtoneManager.EXTRA_RINGTONE_TITLE,
				mActivity.getResources().getString(
						R.string.aurora_chronometer_pick_ringer_title));
		// Allow user to pick 'Default'
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
				RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
		// Show only ringtones
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
				RingtoneManager.TYPE_ALARM);
		// Don't show 'Silent'
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
//    	intent.putExtra("deskclock", AlarmClock.mScreenBitmapMatrix);
		//intent.putExtra("lockscreenpath", AlarmClock.lockscreenDefaultPath);
		intent.putExtra("lockscreenpath", AlarmClock.TIMEPICKERBACKGROUND);
    	intent.putExtra("fullscreen", true);
    	intent.putExtra("chronometer", true);

//		String uriString = mActivity.getSharedPreferences("Chronometer",
//				Activity.MODE_PRIVATE).getString("ChronometerMusicUri",
//				Settings.System.DEFAULT_ALARM_ALERT_URI.toString());
		
		String uriString = mActivity.getSharedPreferences("Chronometer",
				Activity.MODE_PRIVATE).getString("ChronometerMusicUri",
				DEFAULT_TIMEALARM);

		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
				Uri.parse(uriString));
		startActivityForResult(intent, ALARM_RINGTONE);

	}

	private Uri uri;

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {

			if (requestCode == 1) {
				Editor editor = sharedPreferences.edit();
				uri = data
						.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
				// yuchunfei start
			  //if (uri.equals(Settings.System.DEFAULT_ALARM_ALERT_URI)) {
				if (uri.equals(Uri.parse(DEFAULT_TIMEALARM))) {
					musicName = mActivity.getResources().getString(
							R.string.default_ringtone);
				} else {
					Cursor cursor = mActivity.getContentResolver().query(uri,
							null, null, null, null);
					while (cursor.moveToNext()) {
						if ( cursor.getColumnIndex("title") >= 0 ) {
							musicName = cursor.getString(cursor
									.getColumnIndex("title"));
						}
					}
				}
				Log.e("111111", "TimeFragment ---- uri = " + uri);
				// yuchunfei end
				editor.putString("ChronometerMusicUri", uri.toString());
				editor.putString("ChronometerMusicName", musicName);
				chronometeMusic.setText(musicName);
				editor.commit();
			}
		} else if (resultCode == Activity.RESULT_CANCELED) {
			if (requestCode == 1) {
				String musicName11 = sharedPreferences.getString(
						"ChronometerMusicName",
						getResources().getString(R.string.default_ringtone));
				chronometeMusic.setText(musicName11);
			}
		}
	}

	public void refreshUI(int state) {
		if (state == STATE_INIT) {
			timeLeft = -1;
		}
		String[] times = getFormatTime(timeLeft);
		Log.v(TAG, "-Chronometer--refreshUI start!---state" + state + "times="
				+ times[0] + "times[1]=" + times[1]);

		switch (state) {
		case STATE_INIT:
		case STATE_STOP:
			chronometerTimeContainer.setVisibility(View.GONE);
			AnimationContainer.setVisibility(View.GONE);
			mTimePickerContainer.setVisibility(View.VISIBLE);
			startBtnContainer.setVisibility(View.VISIBLE);
			pauseBtnContainer.setVisibility(View.GONE);
			restartBtnContainer.setVisibility(View.GONE);
			cancelBtnContainer.setVisibility(View.GONE);
			endBtnContainer.setVisibility(View.GONE);

//			startBtn.setEnabled(true);

			mTimeHourPicker.setValue(0);
			mTimeMinPicker.setValue(15);
			mTimeSecPicker.setValue(0);
			mIsStartBtnEnabled = true;
//			startBtnText.setTextColor(mActivity.getResources().getColor(R.color.aurora_btn_color));
			chronometerTime.setText(times[0]);
			chronometerTimeSecond.setText(times[1]);
			water.setBackgroundResource(0);

			clearData();

			break;
		case STATE_RUNNING:
			if(wakeLocked ==null){
				  getWakeLock();
				  wakeLocked.acquire();  
			  }

			if (!mStopAnimation) {
				chronometerTimeContainer.setVisibility(View.VISIBLE);
			}
			AnimationContainer.setVisibility(View.VISIBLE);
			mTimePickerContainer.setVisibility(View.GONE);
			startBtnContainer.setVisibility(View.GONE);
			pauseBtnContainer.setVisibility(View.VISIBLE);
			restartBtnContainer.setVisibility(View.GONE);
			cancelBtnContainer.setVisibility(View.VISIBLE);
			endBtnContainer.setVisibility(View.GONE);

//			pauseBtn.setEnabled(true);
//			cancelBtn.setEnabled(true);

			chronometerTime.setText(times[0]);
			chronometerTimeSecond.setText(times[1]);
			if(mIsInitAnimationState && !mStopAnimation) {
				mIsInitAnimationState = false;
				guangquan1.setAnimState(mRunAnim);
			}
			break;
		case STATE_PAUSE:
			chronometerTimeContainer.setVisibility(View.VISIBLE);
			AnimationContainer.setVisibility(View.VISIBLE);
			mTimePickerContainer.setVisibility(View.GONE);
			startBtnContainer.setVisibility(View.GONE);
			pauseBtnContainer.setVisibility(View.GONE);
			restartBtnContainer.setVisibility(View.VISIBLE);
			cancelBtnContainer.setVisibility(View.VISIBLE);
			endBtnContainer.setVisibility(View.GONE);

//			restartBtn.setEnabled(true);
//			cancelBtn.setEnabled(true);

			chronometerTime.setText(times[0]);
			chronometerTimeSecond.setText(times[1]);
			handler.removeCallbacks(this);
			if(mIsInitAnimationState && !mStopAnimation) {
				mIsInitAnimationState = false;
				guangquan1.setAnimState(mPauseAnim);
			}
			break;
		case STATE_END:
			chronometerTimeContainer.setVisibility(View.GONE);
			AnimationContainer.setVisibility(View.GONE);
			mTimePickerContainer.setVisibility(View.VISIBLE);
			startBtnContainer.setVisibility(View.GONE);
			pauseBtnContainer.setVisibility(View.GONE);
			restartBtnContainer.setVisibility(View.GONE);
			cancelBtnContainer.setVisibility(View.GONE);
			endBtnContainer.setVisibility(View.VISIBLE);
			chronometerTime.setText(times[0]);
			chronometerTimeSecond.setText(times[1]);
//			endBtn.setEnabled(true);
			handler.removeCallbacks(this);
			break;
		default:
			break;
		}

	}

	/**
	 * acquire or release Wake Lock
	 */

	protected void onWakeLock() {
		if (state == STATE_RUNNING && isCheck) {
			acquireWakeLock();
		} else {
			releaseWakeLock();
		}
	}

	boolean mIsInitAnimationState = false;
	@Override
	public void onResume() {
		Log.v(TAG, "-Chronometer--onResume() start!---" + state);
		super.onResume();
		Resources resources = getResources();
		Configuration configuration = resources.getConfiguration();
		configuration.setToDefaults();
		resources.updateConfiguration(configuration, resources.getDisplayMetrics());
		state = realState;
		handler.obtainMessage(state).sendToTarget();

		// if (!alertDismiss) {
		// alert();
		// }

		if (state != STATE_RUNNING) {
			if (Alarms.mIfDismiss == false) {
				NotificationOperate.cancelNotification(mActivity, TABNUM);
			}
			ChronometerAlarmAlertWakeLock
					.releaseCpuLock(ChronometerAlarmAlertWakeLock.CHRONMENTER);
			if(mIsFirst) {
				mIsFirst = false;
				timePickerEnterAnimation();
			}
		} else {
//			if(!mIsFirst) {
//				readData();
//			}
			am.cancel(sender);
			handler.post(this);
			NotificationOperate.createNotifaction(mActivity, TABNUM,contentTitle, contentText);
			ChronometerAlarmAlertWakeLock.acquireCpuWakeLock(mActivity, ChronometerAlarmAlertWakeLock.CHRONMENTER);
//			guangquan1.setAnimState(new HourGlassRunningAnimationState(mActivity, handler, guangquan1, water));
//			guangquan1.getAnimState().startAnimation(null);
			mIsInitAnimationState = false;
			mAinmationController.setAnimState(new PauseSwitchToRunState(mActivity));
		}
		// if((state == STATE_STOP||state == STATE_INIT)
		// && guangquan1.getInitStep() !=
		// AuroraHourGlassView.ANIMATION_ROUND_TO_HOURGLASS) {
		// handler.postDelayed(mInitRunnable,1000);
		// }
		Log.v(TAG, "---onResume() end!---");
		
		if(getActivity() != null)
		{
			if(!isHidden())
			{
				if(((AlarmClock)getActivity()).isCtsTest)
				{
					startTimer30Sec();
					((AlarmClock)getActivity()).isCtsTest = false;
				}else{
					
				}
			}
		}
		
		if(titleText != null)
		{
			if(AlarmClock.ctsShowTitle != null)
			{
				isCtsTest = true;
			}
			titleText.setText(!TextUtils.isEmpty(AlarmClock.ctsShowTitle)||isCtsTest ? "Start Timer Test" :contentTitle);
		}
	}

	private boolean isCtsTest = false;
	
	/**
	 * Initialize the UI state
	 */
	private void initState() {
		instanse = this;
		mActivity = (AuroraActivity) getActivity();
		Intent intent = new Intent(mActivity, TimerReceiver.class);
		intent.setAction("coma.aurora.timer.alert");
		sender = PendingIntent.getBroadcast(mActivity, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		sharedPreferences = mActivity.getSharedPreferences("Chronometer",
				Activity.MODE_PRIVATE);

		Log.v(TAG, "--Chronometer-init ui state start!---");
		readData();
		Log.v(TAG, "--Chronometer-init ui state start!--timeLeft=" + timeLeft
				+ "normalExit=" + normalExit);
		// if (!sharedPreferences.getBoolean(StopWatchFragment.iS_LOCALE_CHANED,
		// false)&&(state != STATE_PAUSE || timeLeft <= 0 || !normalExit)) {
		// state = STATE_INIT;
		// }

	}

	private void readData() {
		Log.v(TAG, "---readData start!---");

		normalExit = sharedPreferences.getBoolean("normalExit", false);
		Editor editor = sharedPreferences.edit();
		editor.putBoolean("normalExit", false);
		editor.commit();

		setState(sharedPreferences.getInt("state", STATE_INIT));
		period = sharedPreferences.getLong("period", 0);
		time = sharedPreferences.getLong("time", -1);
		timeLeft = sharedPreferences.getLong("timeLeft", time);
		stopTime = sharedPreferences.getLong("stopTime",
				System.currentTimeMillis() + timeLeft);

		// isCheck = sharedPreferences.getBoolean("isCheck", true);
		isCheck = false;

		Log.v(TAG, "-Chronometer--readData end!-state=--" + state + "normalExit="
				+ normalExit + "timeLeft=" + timeLeft);
	}

	private void saveData() {
		Log.v(TAG, "---saveData start!---");

		Editor editor = sharedPreferences.edit();

		editor.putLong("time", time);
		editor.putLong("timeLeft", timeLeft);
		editor.putLong("stopTime", stopTime);
		editor.putLong("period", period);
		editor.putBoolean("isCheck", isCheck);
		editor.putBoolean("normalExit", normalExit);
		editor.putInt("state", state);
		// editor.putBoolean("show", chronometerView.isShow());
		// editor.putInt("lightAngle", chronometerView.getLightAngle());

		editor.commit();

		Log.v(TAG, "---saveData end!---");
	}

	/**
	 * save the UI state
	 */
	private void saveState() {
		Log.v(TAG, "---save state start!---");
		releaseWakeLock();
		// save data
		saveData();
		Log.v(TAG, "---save state end!---");
		if(wakeLocked !=null){
			wakeLocked.release();
			wakeLocked=null;
		
		}
	}

	@Override
	public void onPause() {
		Log.v(TAG, "---onPause start");
		releaseWakeLock();
		if (!sharedPreferences.getBoolean(StopWatchFragment.iS_LOCALE_CHANED,
				false)) {
			mAinmationController.cancelCurrentAnim();
			guangquan1.getAnimState().cancelAnimation();
			if (state == STATE_RUNNING) {
				lastSystemTime = 0;
				handler.removeCallbacks(this);	
				sendAlarmOnDestory();
				ChronometerAlarmAlertWakeLock.releaseCpuLock(ChronometerAlarmAlertWakeLock.CHRONMENTER);
			}
		}
		saveState();
		super.onPause();
	}

	@Override
	public void onDestroy() {
		Log.v(TAG, "-Chronometer--onDestroy() start!---state=" + state);
		normalExit = true;
		if (!sharedPreferences.getBoolean(StopWatchFragment.iS_LOCALE_CHANED,
				false)) {
			if (state == STATE_RUNNING) {
				// state = STATE_PAUSE;
				handler.removeCallbacks(this);
				guangquan1.setAnimState(null);
				sendAlarmOnDestory();
			} else if (state == STATE_STOP) {
				setState(STATE_INIT);
				
			}
		}
		saveState();
		ChronometerAlarmAlertWakeLock
				.releaseScreenOnLock(ChronometerAlarmAlertWakeLock.CHRONMENTER);
//		mActivity.stopService(new Intent(TimerFragment.ALARM_ALERT_ACTION));
		if(mAlertDialog != null) {
			mAlertDialog.dismiss();
		}

		mAnimationDrawable = null;
		mAnimationDrawableEnd = null;
		mIdleAnim = null;	
		mRunAnim = null;	
		mPauseAnim = null;

		super.onDestroy();
		Log.v(TAG, "--Choronometer-onDestroy() end!---state=" + state);
	}

	private void startTimer30Sec(){
		mTimeHourPicker.setValue(0);
		mTimeMinPicker.setValue(0);
		mTimeSecPicker.setValue(30);
		removeAllMessages();
		onStartClick();
		ChronometerAlarmAlertWakeLock.acquireCpuWakeLock(mActivity,
				ChronometerAlarmAlertWakeLock.CHRONMENTER);
	}
	
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		// TODO Auto-generated method stub
		super.onHiddenChanged(hidden);
		
	}
	
	/**
	 * click to start
	 */
	protected void onStartClick() {
		if(wakeLocked ==null){
			  getWakeLock();
			  wakeLocked.acquire();  
		  }

		// time = (mTimePicker.getCurrentHour() * 3600 +
		// mTimePicker.getCurrentMinute() * 60) * 1000;
		time = (mTimeHourPicker.getValue() * 3600 + mTimeMinPicker.getValue()
				* 60 + mTimeSecPicker.getValue()) * 1000;
		mIsInitAnimationState = false;
//		time = (mTimeHourPicker.getValue() * 3600 + mTimeMinPicker.getValue() * 60 ) * 1000;
		Log.v(TAG, "---click start!--- time = " + time);
		if (time <= 0) {
			return;
		}
		timeLeft = time;
		stopTime = System.currentTimeMillis() + timeLeft;
		Log.v(TAG, "---stopTime: " + stopTime + ",timeLeft: " + timeLeft + "---");
		mAinmationController.setAnimState(new StopSwitchToRunOutState(mActivity));
		// change thr state to running

		NotificationOperate.createNotifaction(mActivity, TABNUM, contentTitle,
				contentText);
		onWakeLock();
	}

	/**
	 * click to pause
	 */
	protected void onPauseClick() {
		if(wakeLocked !=null){
			 wakeLocked.release();  
			 wakeLocked=null; 
		    }
		Log.v(TAG, "---onPauseClick!---");
		handler.removeCallbacks(this);	
		startBtn.clearAnimation();
		pauseBtn.clearAnimation();
		restartBtn.clearAnimation();
		cancelBtn.clearAnimation();
		endBtn.clearAnimation();
		startBtnText.clearAnimation();
		pauseBtnText.clearAnimation();
		restartBtnText.clearAnimation();
		cancelBtnText.clearAnimation();
		endBtnText.clearAnimation();
		NotificationOperate.cancelNotification(mActivity, TABNUM);
		mAinmationController.setAnimState(new RunSwitchToPauseState(mActivity));
		mFirstWater = false;
		releaseWakeLock();
	}

	private void onReStartClick() {
		if(wakeLocked==null){
			  getWakeLock();
			  wakeLocked.acquire();  
		  }
		stopTime = System.currentTimeMillis() + timeLeft;
		Log.v(TAG, "---stopTime: " + stopTime + ",timeLeft: " + timeLeft + "---");
		mAinmationController.setAnimState(new PauseSwitchToRunState(mActivity));
		handler.post(this);

		NotificationOperate.createNotifaction(mActivity, TABNUM, contentTitle,
				contentText);
		onWakeLock();
	}

	/**
	 * click to reset
	 */
	protected void onResetClick() {
		if(wakeLocked !=null){
			 wakeLocked.release();  
			 wakeLocked=null; 
		  }
		Log.v(TAG, "---click reset!---");

		handler.removeCallbacks(this);
		timeLeft = 0;
		time = 0;

		AnimationDrawable animationDrawable = (AnimationDrawable) guangquan1.getBackground();
		if (animationDrawable != null) {
			animationDrawable.stop();
		}
		if (state == STATE_RUNNING) {
			mAinmationController.setAnimState(new RunSwitchToStopOutState(mActivity));
		} else {
			mAinmationController.setAnimState(new PauseSwitchToStopOutState(mActivity));
		}

		NotificationOperate.cancelNotification(mActivity, TABNUM);
		releaseWakeLock();

	}

	/**
	 * click to reset
	 */
	protected void onEndClick() {
		Log.v(TAG, "---click end!---");
		NotificationManager nm = (NotificationManager) mActivity
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(0);
		mActivity.stopService(new Intent(TimerFragment.ALARM_ALERT_ACTION));
		ChronometerAlarmAlertWakeLock
				.releaseScreenOnLock(ChronometerAlarmAlertWakeLock.CHRONMENTER);
		setAlertDismiss(true);

		handler.removeCallbacks(this);
		timeLeft = 0;
		time = 0;

		mAinmationController.setAnimState(new EndSwitchToStopState(mActivity));

		NotificationOperate.cancelNotification(mActivity, TABNUM);
		releaseWakeLock();
		((AlarmClock) mActivity).updateKeyguardPolicy(false);
		saveState();

	}

	@Override
	public void run() {
		// If the system time, date, and time change, repositioning startTime

		long tempSystemTime = System.currentTimeMillis();
		if (lastSystemTime != 0
				&& (tempSystemTime - lastSystemTime > 300 || tempSystemTime
						- lastSystemTime < -300)) {
			stopTime = tempSystemTime + timeLeft;
		}

		timeLeft = stopTime - tempSystemTime;

		final String[] times = getFormatTime(timeLeft);
		chronometerTime.setText(times[0]);
		chronometerTimeSecond.setText(times[1]);
		if (!mStopAnimation) {
			if(!mFirstWater) {
				guangquan1.setTimeleft(timeLeft);
			}
		}

		lastSystemTime = tempSystemTime;
		if (timeLeft <= 0) {
			handler.removeCallbacks(this);
			if(mIsAlertShowDialog) {
				mAinmationController.setAnimState(new RunSwitchToEndOutState2(mActivity));
			} else  {
				mAinmationController.setAnimState(new RunSwitchToEndOutState(mActivity));
			}
			// alert();
			// onResetClick();
		} else {
			handler.postDelayed(this, 50);
		}
	}

	private void clearData() {
		time = 0;
		timeLeft = time;

		Editor editor = sharedPreferences.edit();
		editor.putInt("state", STATE_INIT);
		editor.putLong("period", 0);
		period = 0;
		editor.commit();
	}

	public static void setAlertDismiss(boolean alertDismiss) {
		TimerFragment.alertDismiss = alertDismiss;
	}

	/**
	 * foemate the time
	 * 
	 * @param time
	 * @return
	 */
	private String[] getFormatTime(long time) {
		
//		if(this.time - timeLeft > 1000) {
			time += 1000;
//		}
		if(time>this.time) {
			time = this.time;
		}
		long second = (time / 1000) % 60;
		long minute = (time / 1000 / 60) % 60;
		long hour = time / 1000 / 60 / 60;

		// SEC Display two
		String strSecond = ("00" + second)
				.substring(("00" + second).length() - 2);
		// minute Display two
		String strMinute = ("00" + minute)
				.substring(("00" + minute).length() - 2);
		// hour Display two
		String strHour = ("00" + hour).substring(("00" + hour).length() - 2);

		return new String[] { strHour + ":" + strMinute + ":", "" + strSecond };
	}

	/**
	 * Return time
	 * 
	 * @param time
	 * @return
	 */
	private String getTime(long time) {

		long second = (time / 1000) % 60;
		long minute = (time / 1000 / 60) % 60;
		long hour = time / 1000 / 60 / 60;

		String result = getResources().getString(
				R.string.chronometer_alart_title);
		result += hour <= 0 ? "" : hour
				+ getResources().getString(
						R.string.chronometer_alart_title_hour);
		result += minute <= 0 ? "" : minute
				+ getResources().getString(
						R.string.chronometer_alart_title_minute);
		result += second <= 0 ? "" : second
				+ getResources().getString(
						R.string.chronometer_alart_title_second);

		return result;
	}

	/**
	 * The screen is often bright state
	 */
	private void acquireWakeLock() {
		Log.v(TAG, "---set screen light on!---");
		// AlarmClock alarmClock = (AlarmClock) mActivity;
		// alarmClock.getmViewPager().setKeepScreenOn(true);
	}

	/**
	 * Cancel the screen often bright state
	 */
	private void releaseWakeLock() {
		Log.v(TAG, "---set screen light off!---");
		// AlarmClock alarmClock = (AlarmClock) mActivity;
		// alarmClock.getmViewPager().setKeepScreenOn(false);
	}

	/**
	 * set timeChanged
	 * 
	 * @param timeChanged
	 */
	public static void setTimeChanged(boolean timeChanged) {
		TimerFragment.timeChanged = timeChanged;
	}

	private void alert() {

		Log.v(TAG, "alert start!");
		
		Log.e("222222", "---send com.android.deskclock.stoptalarmring------");
		Intent intent = new Intent("com.android.deskclock.stoptalarmring");
        mActivity.sendBroadcast(intent);

		if (mStartMusic == true) {
			startMusic();
		}

	//	showAlertDialog();
		showAlertDialog2();
		Log.v(TAG, "alert end!");
	}

	private void showAlertDialog() {
		// Close dialogs and window shade
		Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		mActivity.sendBroadcast(closeDialogs);

		Class<?> clazz = ChronometerAlarmAlert.class;
		KeyguardManager km = (KeyguardManager) mActivity
				.getSystemService(Context.KEYGUARD_SERVICE);
		if (km.inKeyguardRestrictedInputMode()) {
			Log.v(TAG, "KeyguardManager.inKeyguardRestrictedInputMode(): "
					+ km.inKeyguardRestrictedInputMode());
			// Use the full screen activity for security.
			clazz = ChronometerAlertFullScreen.class;
		} else {
			Log.v(TAG, "KeyguardManager.inKeyguardRestrictedInputMode(): "
					+ km.inKeyguardRestrictedInputMode());
		}

		Intent intent = new Intent(mActivity, clazz);
		if (isAdded()) {
			intent.putExtra(COUNTDOWN_TIME, getTime(time));
			startActivityForResult(intent, ALARM_RINGTONE);
		}

		setAlertDismiss(false);
		Log.v(TAG, "show dialog end!");
	}

	
    private static final String ALARM_DISMISS_ACTION = "com.android.intent.chronometer.ALARM_DISMISS";
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ALARM_DISMISS_ACTION)) {
                NotificationManager nm = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);;
                nm.cancel(0);
                mActivity.stopService(new Intent(TimerFragment.ALARM_ALERT_ACTION));
                TimerFragment.setAlertDismiss(true);
                ChronometerAlarmAlertWakeLock.releaseCpuLock(ChronometerAlarmAlertWakeLock.CHRONMENTER);
        		ChronometerAlarmAlertWakeLock.releaseScreenOnLock(ChronometerAlarmAlertWakeLock.CHRONMENTER);  
                TimerFragment.setStartMusic(true);
                mActivity.unregisterReceiver(mReceiver);
            }
        }
    };
    
    
    AuroraAlertDialog mAlertDialog;
	private void showAlertDialog2() {
		// Close dialogs and window shade
		Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		mActivity.sendBroadcast(closeDialogs);
        IntentFilter filter = new IntentFilter(ALARM_DISMISS_ACTION);
		mActivity.registerReceiver(mReceiver, filter);
        TimerFragment.setStartMusic(false);
		setAlertDismiss(false);
        final DialogInterface.OnDismissListener mDialogListener =
                new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                                NotificationManager nm = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);;
                                nm.cancel(0);
                                Intent intent = new Intent(TimerFragment.ALARM_ALERT_ACTION);
                                intent.setPackage(mActivity.getPackageName());  //cjs add
                                mActivity.stopService(intent);
                                TimerFragment.setAlertDismiss(true);
                                ChronometerAlarmAlertWakeLock.releaseCpuLock(ChronometerAlarmAlertWakeLock.CHRONMENTER);
                        		ChronometerAlarmAlertWakeLock.releaseScreenOnLock(ChronometerAlarmAlertWakeLock.CHRONMENTER);  
                                TimerFragment.setStartMusic(true);
                        		((AlarmClock) mActivity).updateKeyguardPolicy(false);
                                try {
                        		mActivity.unregisterReceiver(mReceiver);
				} catch (IllegalArgumentException e) {
					// TODO: handle exception
				}
                                mAlertDialog = null;
                                AlarmClock.ctsShowTitle = null;
                                isCtsTest = false;
                    }
                };
                //声音键监听屏蔽
                final DialogInterface.OnKeyListener mOnKeyListener=new DialogInterface.OnKeyListener() {
					
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						// TODO Auto-generated method stub
						boolean up = event.getAction() == KeyEvent.ACTION_UP;
						 switch (event.getKeyCode()) {
//			            // Volume keys and camera keys dismiss the alarm
			            case KeyEvent.KEYCODE_VOLUME_UP:
			            case KeyEvent.KEYCODE_VOLUME_DOWN:
			            case KeyEvent.KEYCODE_VOLUME_MUTE:
			            case KeyEvent.KEYCODE_CAMERA:
			            case KeyEvent.KEYCODE_FOCUS:
			                if (up) {

			                }
			                return true;
			            default:
			                break;
			        }
						return false;
					}
				};
		
        mAlertDialog = new AuroraAlertDialog.Builder(mActivity)
	        .setMessage(getTime(time)).setOnKeyListener(mOnKeyListener)
	        .setTitle(!TextUtils.isEmpty(AlarmClock.ctsShowTitle) || isCtsTest?titleText.getText():getString(R.string.gn_chronometer_alarm_alert_title))
	        .setPositiveButton(R.string.chronometer_alarm_alert_dismiss_text, null)
	        .setOnDismissListener(mDialogListener)
	        .setCancelable(false)
	        .create();
        mAlertDialog.show();
		Log.v(TAG, "show dialog end!");
	}
	
	private void startMusic() {
		Log.v(TAG, "startService!");
//		((AlarmClock) mActivity).updateKeyguardPolicy(true);
		ChronometerAlarmAlertWakeLock.acquireScreenOnLock(mActivity,
				ChronometerAlarmAlertWakeLock.CHRONMENTER);
		Intent playAlarm = new Intent(ALARM_ALERT_ACTION);
		
		playAlarm.setPackage(mActivity.getPackageName());  //cjs add  //cjs add
		
		mActivity.startService(playAlarm);
		handler.removeCallbacks(mOneEndMusicRunnable);
		handler.postDelayed(mOneEndMusicRunnable, 3 * 60 * 1000);
		Intent intent = new Intent(mActivity, AlarmClock.class);
		intent.setAction(Intent.ACTION_MAIN);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("isFromTimerReceiver", true);
		mActivity.startActivity(intent);
//		 ((AlarmClock)mActivity).setTabSelection(3);
		Log.v(TAG, "startService end!");
	}
	
    Runnable mOneEndMusicRunnable = new Runnable() {
		public void run() {
			Intent intent = new Intent(TimerFragment.ALARM_ALERT_ACTION);
			intent.setPackage(mActivity.getPackageName());
//			mActivity.stopService(new Intent(
//					TimerFragment.ALARM_ALERT_ACTION));
			mActivity.stopService(intent);
		}
    };
	

	@Override
	public void onDestroyView() {

		super.onDestroyView();
	}

	public synchronized void setState(int state) {
		this.state = state;
		this.realState = state;		
	}

	public int getState() {
		return state;
	}

	public boolean isCheck() {
		return isCheck;
	}

	public void setCheck(boolean isCheck) {
		this.isCheck = isCheck;
	}

	public static void setStartMusic(boolean mStartMusic) {
		TimerFragment.mStartMusic = mStartMusic;
	}

	public void gnSaveData() {
		Log.v(TAG, "---gnSaveData start!---");

		Editor editor = sharedPreferences.edit();
		editor.putBoolean("isCheck", isCheck);
		editor.commit();

		Log.v(TAG, "---gnSaveData end!---");
	}

	private void saveLocalChangedData() {
		Editor editor = sharedPreferences.edit();
		editor.putBoolean(StopWatchFragment.iS_LOCALE_CHANED, false);
		editor.commit();
	}

	public void endAnimation() {
	    mAinmationController.cancelCurrentAnim();
		if (state == STATE_RUNNING) {
			if(wakeLocked !=null){
				wakeLocked.release();  
				 wakeLocked=null; 
					  }
			mAinmationController.setAnimState(new RunExitState(mActivity));
		} else if (state == STATE_INIT) {
			mAinmationController.setAnimState(new StopExitState(mActivity));
		} else if (state == STATE_PAUSE) {
			mAinmationController.setAnimState(new PauseExitState(mActivity));
		} else if (state == STATE_END) {
			mAinmationController.setAnimState(new EndExitState(mActivity));
		}
		titleExitAnimation();
		ringerExitAnimation();
	}



	public interface OnTimerAnimationCompleteListener {
		void onTimerFragmentAnimationComplete();
	}

	private OnTimerAnimationCompleteListener mOnAnimationCompleteListener = null;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mOnAnimationCompleteListener = (OnTimerAnimationCompleteListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement mOnAnimationCompleteListener");
		}
	}

	boolean mIsFirst = false;
	public void timePickerEnterAnimation() {
		Log.v(TAG, "---timePickerEnterAnimation--");
		mStopAnimation = false;
		if(mRootView != null) {
			guangquan1.invalidate();
			handler.obtainMessage(state).sendToTarget();			
			if (state == STATE_RUNNING) {
				mAinmationController.setAnimState(new RunEnterState(mActivity));
			} else if (state == STATE_INIT) {
				mAinmationController.setAnimState(new StopEnterState(mActivity));
			} else if (state == STATE_PAUSE) {
				mAinmationController.setAnimState(new PauseEnterState(mActivity));
			} else if (state == STATE_END) {
				mAinmationController.setAnimState(new EndEnterState(mActivity));
			}
			titleEnterAnimation();
			ringerEnterAnimation();
		} else {
			mIsFirst = true;
		}
	}


	AuroraAnimationDrawable mAnimationDrawable;
	AuroraAnimationDrawable mAnimationDrawableEnd;

	private void sendAlarmOnDestory() {
		Log.v(TAG, "---sendAlarmOnDestory--");
		long now = System.currentTimeMillis();
		am.set(AlarmManager.RTC_WAKEUP, now + timeLeft, sender);
	}

	AinmationController mAinmationController;

	public class AinmationController {
		TimerState mState;

		public void setAnimState(TimerState s) {
//			if(mState != null) {
//				mState.cancelAinmation();
//			}
			mState = s;
			if(s != null) {
				s.doRefresh();
				s.doAinmation();
			}
		}
		

		TimerState getAnimaState() {
			return mState;
		}
		
		public void cancelCurrentAnim() {
			if(mState != null) {
				mState.cancelAinmation();
			}
		}

	}

	private abstract class TimerState {
		Context mContext;

		public TimerState(Context context) {
			mContext = context;
		}

		public abstract void doRefresh();

		public abstract void doAinmation();
		
		public abstract void doAinmationInternal();

		public abstract void cancelAinmation();
		
		public abstract void onAnimationStart();
		
		public abstract void onAnimationEndInternal();
	}
	
	public interface OnStateAnimationEndListener {
		void onAnimationStart();
		void onAnimationEnd();
	}


	private class TimerStateBase extends TimerState implements OnStateAnimationEndListener{
		
		protected OnStateAnimationEndListener mOnStateAnimationEndListener = null;		
		
		public TimerStateBase(Context context) {
			super(context);
			mOnStateAnimationEndListener = (OnStateAnimationEndListener) this;
		}

		public void doRefresh() {
			clearAllAnimation2();
		}

		public void doAinmation() {
			doAinmationInternal();
		}

		public void doAinmationInternal() {
		}

		public void cancelAinmation() {
		}
		
	    public void onAnimationStartInternal() {
	    	setButtonsEnabled(false);		
	    }
		
		public void onAnimationEndInternal() {
			setButtonsEnabled(true);	
		}
		
		public void onAnimationEnd() {
			Log.v(TAG, this.getClass().getName() +"---onAnimationEnd!---");
			onAnimationEndInternal();
		};
		
		public void onAnimationStart() {
			Log.v(TAG, this.getClass().getName() + "---onAnimationStart!---");
			onAnimationStartInternal();
		};
		
		protected void setStateAnimationListener(Animation a) {
			a.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
					mOnStateAnimationEndListener.onAnimationStart();
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					mOnStateAnimationEndListener.onAnimationEnd();
				}
			});
		}
	}

	private class RunEnterState extends TimerStateBase {
		public RunEnterState(Context context) {
			super(context);
		}

		public void doAinmationInternal() {
			Animation buttonInAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_in);
			Animation buttonTextInAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_text_in);
			cancelBtn.startAnimation(buttonInAnimation);
			cancelBtnText.startAnimation(buttonTextInAnimation);
			pauseBtn.startAnimation(buttonInAnimation);
			pauseBtnText.startAnimation(buttonTextInAnimation);
			Animation timeContainerInAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.time_container_in);
			chronometerTimeContainer.startAnimation(timeContainerInAnimation);
			chronometerTimeContainer.setVisibility(View.VISIBLE);
			setStateAnimationListener(timeContainerInAnimation);
//			hourglassEnterAnimation();
		}
		
	}

	private class RunExitState extends TimerStateBase {
		public RunExitState(Context context) {
			super(context);
		}


		public void doAinmationInternal() {
			mStopAnimation = true;
			Animation buttonOutAnimation = AnimationUtils.loadAnimation(
					mActivity, R.anim.bottom_button_out);
			cancelBtnContainer.startAnimation(buttonOutAnimation);
			pauseBtnContainer.startAnimation(buttonOutAnimation);
			Animation timeContainerOutAnimation = AnimationUtils.loadAnimation(
					mActivity, R.anim.time_container_out);
			timeContainerOutAnimation.setAnimationListener(new Animation.AnimationListener() {
						public void onAnimationStart(Animation animation) {
							mOnStateAnimationEndListener.onAnimationStart();
						}

						public void onAnimationRepeat(Animation animation) {
						}

						public void onAnimationEnd(Animation animation) {
							pauseBtnContainer.setVisibility(View.GONE);
							cancelBtnContainer.setVisibility(View.GONE);
							if (mOnAnimationCompleteListener != null) {
								mOnAnimationCompleteListener.onTimerFragmentAnimationComplete();
							}
							mOnStateAnimationEndListener.onAnimationEnd();
						}
					});
			chronometerTimeContainer.startAnimation(timeContainerOutAnimation);
		}

	}

	private class RunSwitchToPauseState extends TimerStateBase {
		public RunSwitchToPauseState(Context context) {
			super(context);
		}

		public void doRefresh() {
			super.doRefresh();
			setState(STATE_PAUSE);
			handler.obtainMessage(state).sendToTarget();
		}

		public void doAinmationInternal() {	
			mOnStateAnimationEndListener.onAnimationStart();
//			guangquan1.setAnimState(new HourGlassEndingAnimationState(mActivity, handler, guangquan1));
//			Log.v(TAG, "---RunSwitchToPauseState start!---");
//			guangquan1.getAnimState().startAnimation(
//					new OnHourGlassAnimationCompleteListener() {
//						public void onHourGlassAnimationComplete() {
//							Log.v(TAG, "---RunSwitchToPauseState end!---");
//							guangquan1.setAnimState(new HourGlassRunningPauseAnimationState(mActivity, handler, guangquan1));
//							guangquan1.getAnimState().startAnimation(null);
//							mOnStateAnimationEndListener.onAnimationEnd();
//						}
//					});

			Log.v(TAG, "---RunSwitchToPauseState end!---");
			guangquan1.setAnimState(mPauseAnim);
			guangquan1.getAnimState().startAnimation(null);
			mOnStateAnimationEndListener.onAnimationEnd();
		
		}

	}

	private class RunSwitchToStopOutState extends TimerStateBase {
		public RunSwitchToStopOutState(Context context) {
			super(context);
		}

		public void doRefresh() {
			super.doRefresh();
			realState = STATE_INIT;
		}

		public void doAinmationInternal() {
			Animation timeContainerOutAnimation = AnimationUtils.loadAnimation(
					mActivity, R.anim.time_container_out);
			Animation leftButtonAnimation = AnimationUtils.loadAnimation(
					mActivity, R.anim.bottom_button_left_to_center);
			Animation rightButtonAnimation = AnimationUtils.loadAnimation(
					mActivity, R.anim.bottom_button_right_to_center);
			Animation leftButtonTextAnimation = AnimationUtils.loadAnimation(
					mActivity, R.anim.bottom_button_text_left_to_center);
			Animation rightButtonTextAnimation = AnimationUtils.loadAnimation(
					mActivity, R.anim.bottom_button_text_right_to_center);
			final Animation centerButtonAnimation = AnimationUtils.loadAnimation(
					mActivity, R.anim.center_button_in_delay);
			rightButtonTextAnimation.setAnimationListener(new Animation.AnimationListener() {				
  				@Override
  				public void onAnimationStart(Animation animation) {  
  					mOnStateAnimationEndListener.onAnimationStart();
  				}
  				
  				@Override
  				public void onAnimationRepeat(Animation animation) {			
  				}
  				
  				@Override
  				public void onAnimationEnd(Animation animation) {
  					chronometerTimeContainer.setVisibility(View.GONE);
  		 			cancelBtnContainer.setVisibility(View.GONE);
  		 			pauseBtnContainer.setVisibility(View.GONE);
  				}
  			});
			centerButtonAnimation.setAnimationListener(new Animation.AnimationListener() {
						@Override
						public void onAnimationStart(Animation animation) {					
						}

						@Override
						public void onAnimationRepeat(Animation animation) {
						}

						@Override
						public void onAnimationEnd(Animation animation) {					
							mAinmationController.setAnimState(new RunSwitchToStopEnterState(mActivity));
						}
					});
			chronometerTimeContainer.startAnimation(timeContainerOutAnimation);
			cancelBtn.startAnimation(leftButtonAnimation);
			cancelBtnText.startAnimation(leftButtonTextAnimation);
			pauseBtn.startAnimation(rightButtonAnimation);
			pauseBtnText.startAnimation(rightButtonTextAnimation);
//			handler.postDelayed(new Runnable(){
//				public void run(){
					startBtnContainer.startAnimation(centerButtonAnimation);
//				}
//			}, mActivity.getResources().getInteger(R.integer.center_button_show_delay_duration));

		}

		public void cancelAinmation() {
			clearAllAnimation();
			setState(STATE_INIT);
			mIsStartBtnEnabled = true;
			handler.obtainMessage(state).sendToTarget();
			mOnStateAnimationEndListener.onAnimationEnd();
		}
	}

	private class RunSwitchToStopEnterState extends TimerStateBase {
		public RunSwitchToStopEnterState(Context context) {
			super(context);
		}
		
		public void doRefresh() {
			setState(STATE_INIT);
			mIsStartBtnEnabled = true;
			handler.obtainMessage(state).sendToTarget();			
		}
		
		

		public void doAinmationInternal() {
			Animation timepickerInAnimation = AnimationUtils.loadAnimation(
					mActivity, R.anim.timepicker_scale_in);
			mTimePickerContainer.startAnimation(timepickerInAnimation);
			mOnStateAnimationEndListener.onAnimationEnd();
		}

	}

	private class RunSwitchToEndOutState extends TimerStateBase {
		public RunSwitchToEndOutState(Context context) {
			super(context);
		}

		public void doRefresh() {
			super.doRefresh();
			realState = STATE_END;
		}

		public void doAinmationInternal() {
			mOnStateAnimationEndListener.onAnimationStart();
			guangquan1.getAnimState().cancelAnimation();
//			guangquan1.setAnimState(new HourGlassEndingAnimationState(
//					mActivity, handler, guangquan1));
//			guangquan1.getAnimState().startAnimation(
//					new OnHourGlassAnimationCompleteListener() {
//						public void onHourGlassAnimationComplete() {
//
//							guangquan1.setBackgroundDrawable(mAnimationDrawableEnd);
//							mAnimationDrawableEnd.start(new OnFrameAnimationCompleteListener() {
//										public void onFrameAnimationComplete() {
//											Log.v(TAG, "---hourglassViewEndFrameAnimation  onFrameAnimationComplete");
//
//											if (mStartMusic == true) {
//												startMusic();
//											}
//											mAinmationController.setAnimState(new RunSwitchToEndEnterState(mActivity));
//
//										}
//									});							
//					    	Animation leftButtonAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_left_to_center);
//					       	Animation rightButtonAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_right_to_center); 
//					    	Animation leftButtonTextAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_text_left_to_center);
//					       	Animation rightButtonTextAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_text_right_to_center); 
//					     	final Animation centerButtonAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.center_button_in); 
//							rightButtonTextAnimation.setAnimationListener(new Animation.AnimationListener() {				
//				  				@Override
//				  				public void onAnimationStart(Animation animation) {  
//				  				}
//				  				
//				  				@Override
//				  				public void onAnimationRepeat(Animation animation) {			
//				  				}
//				  				
//				  				@Override
//				  				public void onAnimationEnd(Animation animation) {  
//			    					chronometerTimeContainer.setVisibility(View.GONE);
//				  		 			cancelBtnContainer.setVisibility(View.GONE);
//				  		 			pauseBtnContainer.setVisibility(View.GONE);
//				  				}
//				  			});
//					    	cancelBtn.startAnimation(leftButtonAnimation);
//					     	pauseBtn.startAnimation(rightButtonAnimation);
//					    	cancelBtnText.startAnimation(leftButtonTextAnimation);
//					     	pauseBtnText.startAnimation(rightButtonTextAnimation);
//							handler.postDelayed(new Runnable(){
//								public void run(){
//							     	endBtnContainer.startAnimation(centerButtonAnimation);
//								}
//							}, 50);		
//							Animation timeContainerOutAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.time_container_out);
//							centerButtonAnimation.setAnimationListener(new Animation.AnimationListener() {				
//									@Override
//									public void onAnimationStart(Animation animation) {}
//									
//									@Override
//									public void onAnimationRepeat(Animation animation) {}
//									
//									@Override
//									public void onAnimationEnd(Animation animation) {
//										// TODO Auto-generated method stub				
////					    					chronometerTimeContainer.setVisibility(View.GONE);
////					    					pauseBtnContainer.setVisibility(View.GONE);
////					    					cancelBtnContainer.setVisibility(View.GONE);
//					    					endBtnContainer.setVisibility(View.VISIBLE);
//									}
//								});
//					     	chronometerTimeContainer.startAnimation(timeContainerOutAnimation);	     
//							guangquan1.setAnimState(new HourGlassIdleAnimationState(mActivity, handler, guangquan1));
//							guangquan1.getAnimState().startAnimation(null);
//
//						}
//
//					});


			guangquan1.setBackgroundDrawable(mAnimationDrawableEnd);
			mAnimationDrawableEnd.start(new OnFrameAnimationCompleteListener() {
						public void onFrameAnimationComplete() {
							Log.v(TAG, "---hourglassViewEndFrameAnimation  onFrameAnimationComplete");

							if (mStartMusic == true) {
								startMusic();
							}
							mAinmationController.setAnimState(new RunSwitchToEndEnterState(mActivity));

						}
					});							
	    	Animation leftButtonAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_left_to_center);
	       	Animation rightButtonAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_right_to_center); 
	    	Animation leftButtonTextAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_text_left_to_center);
	       	Animation rightButtonTextAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_text_right_to_center); 
	     	final Animation centerButtonAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.center_button_in_delay); 
			rightButtonTextAnimation.setAnimationListener(new Animation.AnimationListener() {				
  				@Override
  				public void onAnimationStart(Animation animation) {  
  				}
  				
  				@Override
  				public void onAnimationRepeat(Animation animation) {			
  				}
  				
  				@Override
  				public void onAnimationEnd(Animation animation) {  
					chronometerTimeContainer.setVisibility(View.GONE);
  		 			cancelBtnContainer.setVisibility(View.GONE);
  		 			pauseBtnContainer.setVisibility(View.GONE);
  				}
  			});
	    	cancelBtn.startAnimation(leftButtonAnimation);
	     	pauseBtn.startAnimation(rightButtonAnimation);
	    	cancelBtnText.startAnimation(leftButtonTextAnimation);
	     	pauseBtnText.startAnimation(rightButtonTextAnimation);
//			handler.postDelayed(new Runnable(){
//				public void run(){
			     	endBtnContainer.startAnimation(centerButtonAnimation);
//				}
//			}, mActivity.getResources().getInteger(R.integer.center_button_show_delay_duration));		
			Animation timeContainerOutAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.time_container_out);
			centerButtonAnimation.setAnimationListener(new Animation.AnimationListener() {				
					@Override
					public void onAnimationStart(Animation animation) {}
					
					@Override
					public void onAnimationRepeat(Animation animation) {}
					
					@Override
					public void onAnimationEnd(Animation animation) {
						// TODO Auto-generated method stub				
//	    					chronometerTimeContainer.setVisibility(View.GONE);
//	    					pauseBtnContainer.setVisibility(View.GONE);
//	    					cancelBtnContainer.setVisibility(View.GONE);
	    					endBtnContainer.setVisibility(View.VISIBLE);
					}
				});
	     	chronometerTimeContainer.startAnimation(timeContainerOutAnimation);	     
			guangquan1.setAnimState(mIdleAnim);
			guangquan1.getAnimState().startAnimation(null);		

		}

		public void cancelAinmation() {
			clearAllAnimation();
			setState(STATE_END);
			handler.obtainMessage(state).sendToTarget();
			mOnStateAnimationEndListener.onAnimationEnd();
			if (mStartMusic == true) {
				startMusic();
			}
		}
	}

	private class RunSwitchToEndEnterState extends TimerStateBase {
		public RunSwitchToEndEnterState(Context context) {
			super(context);
		}
		
		public void doRefresh() {
			setState(STATE_END);
			handler.obtainMessage(state).sendToTarget();			
		}

		public void doAinmationInternal() {

			Animation timepickerInAnimation = AnimationUtils.loadAnimation(
					mActivity, R.anim.timepicker_scale_in);
			mTimePickerContainer.startAnimation(timepickerInAnimation);
			mOnStateAnimationEndListener.onAnimationEnd();
		}

	}
	
	
	private class RunSwitchToEndOutState2 extends TimerStateBase {
		public RunSwitchToEndOutState2(Context context) {
			super(context);
		}

		
		public void doRefresh() {
			super.doRefresh();
			realState = STATE_INIT;
		}

		public void doAinmationInternal() {
			mOnStateAnimationEndListener.onAnimationStart();
			guangquan1.getAnimState().cancelAnimation();
			guangquan1.setBackgroundDrawable(mAnimationDrawableEnd);
			mAnimationDrawableEnd.start(new OnFrameAnimationCompleteListener() {
						public void onFrameAnimationComplete() {
							Log.v(TAG, "---hourglassViewEndFrameAnimation  onFrameAnimationComplete");
							mAinmationController.setAnimState(new RunSwitchToEndEnterState2(mActivity));

						}
					});							
	    	Animation leftButtonAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_left_to_center);
	       	Animation rightButtonAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_right_to_center); 
	    	Animation leftButtonTextAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_text_left_to_center);
	       	Animation rightButtonTextAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_text_right_to_center); 
	     	final Animation centerButtonAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.center_button_in_delay); 
			rightButtonTextAnimation.setAnimationListener(new Animation.AnimationListener() {				
  				@Override
  				public void onAnimationStart(Animation animation) {  
  				}
  				
  				@Override
  				public void onAnimationRepeat(Animation animation) {			
  				}
  				
  				@Override
  				public void onAnimationEnd(Animation animation) {  
					chronometerTimeContainer.setVisibility(View.GONE);
  		 			cancelBtnContainer.setVisibility(View.GONE);
  		 			pauseBtnContainer.setVisibility(View.GONE);
  				}
  			});
	    	cancelBtn.startAnimation(leftButtonAnimation);
	     	pauseBtn.startAnimation(rightButtonAnimation);
	    	cancelBtnText.startAnimation(leftButtonTextAnimation);
	     	pauseBtnText.startAnimation(rightButtonTextAnimation);
//			handler.postDelayed(new Runnable(){
//				public void run(){
			     	startBtnContainer.startAnimation(centerButtonAnimation);
//				}
//			}, mActivity.getResources().getInteger(R.integer.center_button_show_delay_duration));		
			Animation timeContainerOutAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.time_container_out);
//			centerButtonAnimation.setAnimationListener(new Animation.AnimationListener() {				
//					@Override
//					public void onAnimationStart(Animation animation) {}
//					
//					@Override
//					public void onAnimationRepeat(Animation animation) {}
//					
//					@Override
//					public void onAnimationEnd(Animation animation) {
//						// TODO Auto-generated method stub				
//						startBtnContainer.setVisibility(View.VISIBLE);
//					}
//				});
	     	chronometerTimeContainer.startAnimation(timeContainerOutAnimation);	     
			guangquan1.setAnimState(mIdleAnim);
			guangquan1.getAnimState().startAnimation(null);		

		}

		public void cancelAinmation() {
			clearAllAnimation();
			setState(STATE_INIT);
			handler.obtainMessage(state).sendToTarget();
			mOnStateAnimationEndListener.onAnimationEnd();
			alert(); 
			handler.removeCallbacks(TimerFragment.this);
			timeLeft = 0;
			time = 0;
			NotificationOperate.cancelNotification(mActivity, TABNUM);
			saveState();
		}
	}

	private class RunSwitchToEndEnterState2 extends TimerStateBase {
		public RunSwitchToEndEnterState2(Context context) {
			super(context);
		}
		
		public void doRefresh() {
			setState(STATE_INIT);
			handler.obtainMessage(state).sendToTarget();			
		}

		public void doAinmationInternal() {

			Animation timepickerInAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.timepicker_scale_in);
			mTimePickerContainer.startAnimation(timepickerInAnimation);
			mOnStateAnimationEndListener.onAnimationEnd();
			alert(); 
			handler.removeCallbacks(TimerFragment.this);
			timeLeft = 0;
			time = 0;
			NotificationOperate.cancelNotification(mActivity, TABNUM);
			saveState();
		}

	}
	

	private class StopEnterState extends TimerStateBase {
		public StopEnterState(Context context) {
			super(context);
		}


		public void doAinmationInternal() {
			Animation timepickerInAnimation = AnimationUtils.loadAnimation(
					mActivity, R.anim.timepicker_scale_in);
			Animation ringerInAnimation = AnimationUtils.loadAnimation(
					mActivity, R.anim.ringpicker_in);
			Animation buttonInAnimation = AnimationUtils.loadAnimation(
					mActivity, R.anim.bottom_button_in);
			Animation buttonTextInAnimation = AnimationUtils.loadAnimation(
					mActivity, R.anim.bottom_button_text_in);
			mTimePickerContainer.startAnimation(timepickerInAnimation);
			mImageviewSet.startAnimation(ringerInAnimation);
			startBtn.startAnimation(buttonInAnimation);
			startBtnText.startAnimation(buttonTextInAnimation);
			setStateAnimationListener(buttonTextInAnimation);
		}

	}

	private class StopExitState extends TimerStateBase {
		public StopExitState(Context context) {
			super(context);
		}


		public void doAinmationInternal() {						

			Animation timepickerOutAnimation = AnimationUtils.loadAnimation(
					mActivity, R.anim.timepicker_scale_out);
			Animation buttonOutAnimation = AnimationUtils.loadAnimation(
					mActivity, R.anim.bottom_button_out);
			buttonOutAnimation.setAnimationListener(new Animation.AnimationListener() {
						@Override
						public void onAnimationStart(Animation animation) {
							// TODO Auto-generated method stub
							mOnStateAnimationEndListener.onAnimationStart();
						}

						@Override
						public void onAnimationRepeat(Animation animation) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onAnimationEnd(Animation animation) {
							if (mOnAnimationCompleteListener != null) {
								mOnAnimationCompleteListener.onTimerFragmentAnimationComplete();
							}
							mOnStateAnimationEndListener.onAnimationEnd();
						}
					});
			mTimePickerContainer.startAnimation(timepickerOutAnimation);
			startBtnContainer.startAnimation(buttonOutAnimation);

		}


	}

	private class StopSwitchToRunOutState extends TimerStateBase {
		public StopSwitchToRunOutState(Context context) {
			super(context);
		}
		
		public void doRefresh() {
			super.doRefresh();
			realState = STATE_RUNNING;
		}


		public void doAinmationInternal() {

			guangquan1.setAnimState(mIdleAnim);
			guangquan1.getAnimState().startAnimation(null);
			Animation timepickerOutAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.timepicker_scale_out);
			Animation leftButtonAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_center_to_left);
			Animation rightButtonAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_center_to_right);
			Animation centerButtonAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.center_button_out);
			Animation leftButtonTextAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_text_center_to_left);
			Animation rightButtonTextAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_text_center_to_right);
			timepickerOutAnimation.setAnimationListener(new Animation.AnimationListener() {
						@Override
						public void onAnimationStart(Animation animation) {
							// TODO Auto-generated method stub
							guangquan1.setAnimState(mIdleAnim);
							guangquan1.getAnimState().startAnimation(null);
							guangquan1.setTimeleft(timeLeft);
							guangquan1.setTotalTime(time);
							mStopAnimation = true;
//							handler.post(TimerFragment.this);

							mOnStateAnimationEndListener.onAnimationStart();

						}

						@Override
						public void onAnimationRepeat(Animation animation) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onAnimationEnd(Animation animation) {
							mAinmationController.setAnimState(new StopSwitchToRunEnterState(mActivity));
						}
					});
			mTimePickerContainer.startAnimation(timepickerOutAnimation);
			cancelBtn.startAnimation(leftButtonAnimation);
			pauseBtn.startAnimation(rightButtonAnimation);
			cancelBtnText.startAnimation(leftButtonTextAnimation);
			pauseBtnText.startAnimation(rightButtonTextAnimation);
			startBtnContainer.startAnimation(centerButtonAnimation);
			cancelBtnContainer.setVisibility(View.VISIBLE);
			pauseBtnContainer.setVisibility(View.VISIBLE);
			startBtnContainer.setVisibility(View.GONE);
		}

		public void cancelAinmation() {
			clearAllAnimation();
			setState(STATE_RUNNING);
			handler.obtainMessage(state).sendToTarget();
			mStopAnimation = false;
			mFirstWater = false;
			guangquan1.setBackgroundResource(0);
			handler.post(TimerFragment.this);
			guangquan1.setAnimState(mRunAnim);
			guangquan1.getAnimState().startAnimation(null);
			mOnStateAnimationEndListener.onAnimationEnd();
		}
		
	}

	boolean mFirstWater = false;
	OnHourGlassAnimationCompleteListener mFirstWaterListener = 
		new OnHourGlassAnimationCompleteListener() {
			public void onHourGlassAnimationComplete() {
				mFirstWater = false;
			}
	};
	
	private class StopSwitchToRunEnterState extends TimerStateBase {
		public StopSwitchToRunEnterState(Context context) {
			super(context);
		}

		public void doRefresh() {
			setState(STATE_RUNNING);
			handler.obtainMessage(state).sendToTarget();			
		}
		
		public void doAinmationInternal() {
			//aurora add liguangyu 20140410 for #4018 start
			if(mAnimationDrawable == null) {
		        if(sThreadHandler != null) {
		            sThreadHandler.getLooper().myQueue().removeIdleHandler(TimerFragment.this);
		        }
				initAnimationRes();
				mRunAnim =  new HourGlassRunningAnimationState(mActivity, handler, guangquan1, water);	
				mPauseAnim = new HourGlassRunningPauseAnimationState(mActivity, handler, guangquan1);
			}
			//aurora add liguangyu 20140410 for #4018 end
			guangquan1.setBackgroundDrawable(mAnimationDrawable);
			Log.v(TAG, "---hourglassViewEnterFrameAnimation  start");
			mAnimationDrawable.start(new OnFrameAnimationCompleteListener() {
				public void onFrameAnimationComplete() {
					Log.v(TAG, "---hourglassViewEnterFrameAnimation  onFrameAnimationComplete");
					mStopAnimation = false;
					mFirstWater = true;
					guangquan1.setBackgroundResource(0);
//					guangquan1.setAnimState(new HourGlassStartingAnimationState(mActivity, handler, guangquan1));
//					guangquan1.getAnimState().startAnimation(
//							new OnHourGlassAnimationCompleteListener() {
//								public void onHourGlassAnimationComplete() {
//									handler.post(TimerFragment.this);
//									guangquan1.setAnimState(new HourGlassRunningAnimationState(mActivity, handler, guangquan1, water));
//									guangquan1.getAnimState().startAnimation(null);
//									mOnStateAnimationEndListener.onAnimationEnd();
//								}
//							});

					handler.post(TimerFragment.this);
					guangquan1.setAnimState(mRunAnim);
					guangquan1.getAnimState().startAnimation(mFirstWaterListener);
					mOnStateAnimationEndListener.onAnimationEnd();
				

				}
			});

			Animation timeContainerInAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.time_container_in);
			chronometerTimeContainer.startAnimation(timeContainerInAnimation);
			chronometerTimeContainer.setVisibility(View.VISIBLE);

		}

		public void cancelAinmation() {
			clearAllAnimation();
			mStopAnimation = false;
			mFirstWater = false;
			guangquan1.setBackgroundResource(0);
			handler.post(TimerFragment.this);
			guangquan1.setAnimState(mRunAnim);
			guangquan1.getAnimState().startAnimation(null);
			mOnStateAnimationEndListener.onAnimationEnd();
		}

	}

	private class PauseEnterState extends TimerStateBase {
		public PauseEnterState(Context context) {
			super(context);
		}


		public void doAinmationInternal() {

			Animation buttonInAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_in);
			Animation buttonTextInAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_text_in);

			cancelBtn.startAnimation(buttonInAnimation);
			cancelBtnText.startAnimation(buttonTextInAnimation);

			restartBtn.startAnimation(buttonInAnimation);
			restartBtnText.startAnimation(buttonTextInAnimation);

			Animation timeContainerInAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.time_container_in);
			chronometerTimeContainer.startAnimation(timeContainerInAnimation);
			chronometerTimeContainer.setVisibility(View.VISIBLE);
			setStateAnimationListener(timeContainerInAnimation);
			hourglassEnterAnimation();

		}


	}

	private class PauseExitState extends TimerStateBase {
		public PauseExitState(Context context) {
			super(context);
		}


		public void doAinmationInternal() {

			mStopAnimation = true;

			Animation buttonOutAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_out);

			cancelBtnContainer.startAnimation(buttonOutAnimation);

			restartBtnContainer.startAnimation(buttonOutAnimation);

			Animation timeContainerOutAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.time_container_out);
			timeContainerOutAnimation.setAnimationListener(new Animation.AnimationListener() {
						@Override
						public void onAnimationStart(Animation animation) {
							// TODO Auto-generated method stub
							mOnStateAnimationEndListener.onAnimationStart();
						}

						@Override
						public void onAnimationRepeat(Animation animation) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onAnimationEnd(Animation animation) {
							// TODO Auto-generated method stub

							restartBtnContainer.setVisibility(View.GONE);
							cancelBtnContainer.setVisibility(View.GONE);

							if (mOnAnimationCompleteListener != null) {
								mOnAnimationCompleteListener.onTimerFragmentAnimationComplete();
							}
							mOnStateAnimationEndListener.onAnimationEnd();
						}
					});
			chronometerTimeContainer.startAnimation(timeContainerOutAnimation);
			hourglassExitAnimation();
		}


	}

	private class PauseSwitchToRunState extends TimerStateBase {
		public PauseSwitchToRunState(Context context) {
			super(context);
		}

		public void doRefresh() {
			super.doRefresh();
			setState(STATE_RUNNING);
			handler.obtainMessage(state).sendToTarget();
		}

		public void doAinmationInternal() {
			mOnStateAnimationEndListener.onAnimationStart();
//			guangquan1.setAnimState(new HourGlassStartingAnimationState(
//					mActivity, handler, guangquan1));
//			guangquan1.getAnimState().startAnimation(
//					new OnHourGlassAnimationCompleteListener() {
//						public void onHourGlassAnimationComplete() {
//							guangquan1.setAnimState(new HourGlassRunningAnimationState(mActivity, handler, guangquan1, water));
//							guangquan1.getAnimState().startAnimation(null);
//	    					mOnStateAnimationEndListener.onAnimationEnd();
//						}
//					});

			guangquan1.setAnimState(mRunAnim);
			guangquan1.getAnimState().startAnimation(null);
			mOnStateAnimationEndListener.onAnimationEnd();
		
		}


	}

	private class PauseSwitchToStopOutState extends TimerStateBase {
		public PauseSwitchToStopOutState(Context context) {
			super(context);
		}

		public void doRefresh() {
			super.doRefresh();
			realState = STATE_INIT;
		}

		public void doAinmationInternal() {
			
			Animation timeContainerOutAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.time_container_out);
			Animation leftButtonAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_left_to_center);
			Animation rightButtonAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_right_to_center);
			Animation leftButtonTextAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_text_left_to_center);
			Animation rightButtonTextAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_text_right_to_center);
			rightButtonTextAnimation.setAnimationListener(new Animation.AnimationListener() {				
  				@Override
  				public void onAnimationStart(Animation animation) {  
  					mOnStateAnimationEndListener.onAnimationStart();
  				}
  				
  				@Override
  				public void onAnimationRepeat(Animation animation) {			
  				}
  				
  				@Override
  				public void onAnimationEnd(Animation animation) {  	
  		 			cancelBtnContainer.setVisibility(View.GONE);
  		 			restartBtnContainer.setVisibility(View.GONE);
  		 			chronometerTimeContainer.setVisibility(View.GONE);
  				}
  			});
			final Animation centerButtonAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.center_button_in_delay);
			centerButtonAnimation
					.setAnimationListener(new Animation.AnimationListener() {
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
							mAinmationController.setAnimState(new PauseSwitchToStopEnterState(mActivity));
						}
					});
			chronometerTimeContainer.startAnimation(timeContainerOutAnimation);
			cancelBtn.startAnimation(leftButtonAnimation);
			cancelBtnText.startAnimation(leftButtonTextAnimation);

			restartBtn.startAnimation(rightButtonAnimation);
			restartBtnText.startAnimation(rightButtonTextAnimation);

//			handler.postDelayed(new Runnable(){
//				public void run(){
					startBtnContainer.startAnimation(centerButtonAnimation);
//				}
//			}, mActivity.getResources().getInteger(R.integer.center_button_show_delay_duration));		

		}

		
		public void cancelAinmation() {
			clearAllAnimation();
			setState(STATE_INIT);
			mIsStartBtnEnabled = true;
			handler.obtainMessage(state).sendToTarget();
			mOnStateAnimationEndListener.onAnimationEnd();
		}

	}

	private class PauseSwitchToStopEnterState extends TimerStateBase {
		public PauseSwitchToStopEnterState(Context context) {
			super(context);
		}

		public void doRefresh() {
			setState(STATE_INIT);
			mIsStartBtnEnabled = true;
			handler.obtainMessage(state).sendToTarget();			
		}
		
		public void doAinmationInternal() {
			Animation timepickerInAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.timepicker_scale_in);
			mTimePickerContainer.startAnimation(timepickerInAnimation);
			mOnStateAnimationEndListener.onAnimationEnd();
		}


	}

	private class EndEnterState extends TimerStateBase {
		public EndEnterState(Context context) {
			super(context);
		}


		public void doAinmationInternal() {

			Animation buttonInAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_in);
			Animation buttonTextInAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_text_in);
			endBtn.startAnimation(buttonInAnimation);
			endBtnText.startAnimation(buttonTextInAnimation);

			Animation timeContainerInAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.time_container_in);
			chronometerTimeContainer.startAnimation(timeContainerInAnimation);
			chronometerTimeContainer.setVisibility(View.VISIBLE);
			setStateAnimationListener(timeContainerInAnimation);

		}


	}

	private class EndExitState extends TimerStateBase {
		public EndExitState(Context context) {
			super(context);
		}


		public void doAinmationInternal() {

			mStopAnimation = true;

			Animation buttonOutAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_out);
			buttonOutAnimation.setAnimationListener(new Animation.AnimationListener() {
						@Override
						public void onAnimationStart(Animation animation) {
							// TODO Auto-generated method stub
							mOnStateAnimationEndListener.onAnimationStart();
						}

						@Override
						public void onAnimationRepeat(Animation animation) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onAnimationEnd(Animation animation) {
							// TODO Auto-generated method stub
							endBtnContainer.setVisibility(View.GONE);
							if (mOnAnimationCompleteListener != null) {
								mOnAnimationCompleteListener.onTimerFragmentAnimationComplete();
							}
							mOnStateAnimationEndListener.onAnimationEnd();
						}
					});
			endBtnContainer.startAnimation(buttonOutAnimation);

		}

	}

	private class EndSwitchToStopState extends TimerStateBase {
		public EndSwitchToStopState(Context context) {
			super(context);
		}
		
		public void doRefresh() {
			super.doRefresh();
			setState(STATE_INIT);
			mIsStartBtnEnabled = false;
			handler.obtainMessage(state).sendToTarget();
		}
	}

	public boolean isViewReady() {
		return mRootView != null;
	}

	
	private ColorStateList mNormalTextColor = null;	
	
	private void setButtonsEnabled(boolean value) {
    	if(!value) {
			startBtn.setEnabled(false);
			pauseBtn.setEnabled(false);
			restartBtn.setEnabled(false);
			cancelBtn.setEnabled(false);
			endBtn.setEnabled(false);	
//			startBtnText.setTextColor(mActivity.getResources().getColor(R.color.aurora_gray_text_color));
//			pauseBtnText.setTextColor(mActivity.getResources().getColor(R.color.aurora_gray_text_color));
//			restartBtnText.setTextColor(mActivity.getResources().getColor(R.color.aurora_gray_text_color));
//			cancelBtnText.setTextColor(mActivity.getResources().getColor(R.color.aurora_gray_text_color));
//			endBtnText.setTextColor(mActivity.getResources().getColor(R.color.aurora_gray_text_color));
			if(mNormalTextColor != null) {
				int color = mActivity.getResources().getColor(R.color.aurora_btn_color);
				startBtnText.setTextColor(color);
				pauseBtnText.setTextColor(color);
				restartBtnText.setTextColor(color);
				cancelBtnText.setTextColor(color);
				endBtnText.setTextColor(color);
			}
			startBtnText.setEnabled(false);
			pauseBtnText.setEnabled(false);
			restartBtnText.setEnabled(false);
			cancelBtnText.setEnabled(false);
			endBtnText.setEnabled(false);			
		} else {
			startBtn.setEnabled(true);
			pauseBtn.setEnabled(true);
			restartBtn.setEnabled(true);
			cancelBtn.setEnabled(true);
			endBtn.setEnabled(true);
			if(mIsStartBtnEnabled) {
//				startBtnText.setTextColor(mActivity.getResources().getColor(R.color.aurora_btn_color));
				startBtnText.setEnabled(true);
			}
//			pauseBtnText.setTextColor(mActivity.getResources().getColor(R.color.aurora_btn_color));
//			restartBtnText.setTextColor(mActivity.getResources().getColor(R.color.aurora_btn_color));
//			cancelBtnText.setTextColor(mActivity.getResources().getColor(R.color.aurora_btn_color));
//			endBtnText.setTextColor(mActivity.getResources().getColor(R.color.aurora_btn_color));
			if(mNormalTextColor != null) {
				startBtnText.setTextColor(mNormalTextColor);
				pauseBtnText.setTextColor(mNormalTextColor);
				restartBtnText.setTextColor(mNormalTextColor);
				cancelBtnText.setTextColor(mNormalTextColor);
				endBtnText.setTextColor(mNormalTextColor);
			}
			pauseBtnText.setEnabled(true);
			restartBtnText.setEnabled(true);
			cancelBtnText.setEnabled(true);
			endBtnText.setEnabled(true);
		}					    
	}
	
	private void removeAllMessages() {
		handler.removeMessages(STATE_STOP);
		handler.removeMessages(STATE_RUNNING);
		handler.removeMessages(STATE_PAUSE);
		handler.removeMessages(STATE_INIT);
		handler.removeMessages(STATE_END);
	}
	
	private void titleEnterAnimation() {
		Animation a = AnimationUtils.loadAnimation(mActivity, R.anim.alarmtitle_enter);
		titleText.startAnimation(a);
	}
	
	private void titleExitAnimation() {
		Animation a = AnimationUtils.loadAnimation(mActivity, R.anim.alarmtitle_exit);
		titleText.startAnimation(a);
	}
	
	private void ringerEnterAnimation() {
		Animation ringerInAnimation = AnimationUtils.loadAnimation(
				mActivity, R.anim.ringpicker_in);
		mImageviewSet.startAnimation(ringerInAnimation);
	}
	
	private void ringerExitAnimation() {
		Animation ringerInAnimation = AnimationUtils.loadAnimation(
				mActivity, R.anim.ringpicker_out);
		mImageviewSet.startAnimation(ringerInAnimation);
	}
	
	
	private void hourglassEnterAnimation() {
		Animation ringerInAnimation = AnimationUtils.loadAnimation(
				mActivity, R.anim.center_button_in);
		guangquan1.startAnimation(ringerInAnimation);
	}
	
	private void hourglassExitAnimation() {
		Animation ringerInAnimation = AnimationUtils.loadAnimation(
				mActivity, R.anim.center_button_out);
		guangquan1.startAnimation(ringerInAnimation);
	}
	
	
	HourGlassIdleAnimationState mIdleAnim;	
	HourGlassRunningAnimationState mRunAnim;	
	HourGlassRunningPauseAnimationState mPauseAnim;
	
	   public boolean queueIdle () {
		    initAnimationRes();
			mRunAnim =  new HourGlassRunningAnimationState(mActivity, handler, guangquan1, water);	
			mPauseAnim = new HourGlassRunningPauseAnimationState(mActivity, handler, guangquan1);
	    	return false;
	    }
	private Handler sThreadHandler;
	
	
	
	private void initAnimationRes() {
		mAnimationDrawable = new AuroraAnimationDrawable();
		mAnimationDrawableEnd = new AuroraAnimationDrawable();
		String index;
		int resID;
		InputStream is;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inPreferredConfig = Bitmap.Config.RGB_565;
		opts.inPurgeable = true;
		opts.inInputShareable = true;
		opts.inSampleSize = 2;
		for (int i = 2; i <= 60; i += 2) {
			if (i < 10) {
				index = "0" + i;
			} else {
				index = String.valueOf(i);
			}
			resID = getResources().getIdentifier("hourglass_enter_" + index,
					"drawable", "com.android.deskclock");
			try {
				is = getResources().openRawResource(resID);
				mAnimationDrawable.addFrame(Drawable.createFromResourceStream(
						getResources(), null, is, "src", opts), 15);
			} catch (Exception e) {
				e.printStackTrace();
			}
			resID = getResources().getIdentifier(
					"hourglass_disappear_" + index, "drawable",
					"com.android.deskclock");
			try {
				is = getResources().openRawResource(resID);
				mAnimationDrawableEnd.addFrame(Drawable
						.createFromResourceStream(getResources(), null, is,
								"src", opts), 15);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void clearAllAnimation() {
		if(startBtnContainer.getAnimation() != null) {
			startBtnContainer.getAnimation().setAnimationListener(null);
		}
		if(pauseBtnContainer.getAnimation() != null) {
			pauseBtnContainer.getAnimation().setAnimationListener(null);
		}
		if(restartBtnContainer.getAnimation() != null) {
			restartBtnContainer.getAnimation().setAnimationListener(null);
		}
		if(cancelBtnContainer.getAnimation() != null) {
			cancelBtnContainer.getAnimation().setAnimationListener(null);
		}
		if(endBtnContainer.getAnimation() != null) {
			endBtnContainer.getAnimation().setAnimationListener(null);
		}
		if(startBtn.getAnimation() != null) {
			startBtn.getAnimation().setAnimationListener(null);
		}
		if(pauseBtn.getAnimation() != null) {
			pauseBtn.getAnimation().setAnimationListener(null);
		}
		if(restartBtn.getAnimation() != null) {
			restartBtn.getAnimation().setAnimationListener(null);
		}
		if(cancelBtn.getAnimation() != null) {
			cancelBtn.getAnimation().setAnimationListener(null);
		}
		
		if(endBtn.getAnimation() != null) {
			endBtn.getAnimation().setAnimationListener(null);
		}
		if(startBtnText.getAnimation() != null) {
			startBtnText.getAnimation().setAnimationListener(null);
		}
		if(pauseBtnText.getAnimation() != null) {
			pauseBtnText.getAnimation().setAnimationListener(null);
		}
		if(restartBtnText.getAnimation() != null) {
			restartBtnText.getAnimation().setAnimationListener(null);
		}
		if(cancelBtnText.getAnimation() != null) {
			cancelBtnText.getAnimation().setAnimationListener(null);
		}
		if(endBtnText.getAnimation() != null) {
			endBtnText.getAnimation().setAnimationListener(null);
		}
		if(mTimePickerContainer.getAnimation() != null) {
			mTimePickerContainer.getAnimation().setAnimationListener(null);
		}
		
		if(mImageviewSet.getAnimation() != null) {
			mImageviewSet.getAnimation().setAnimationListener(null);
		}
		if(chronometerTimeContainer.getAnimation() != null) {
			chronometerTimeContainer.getAnimation().setAnimationListener(null);
		}
		if(guangquan1.getAnimation() != null) {
			guangquan1.getAnimation().setAnimationListener(null);
		}			
		
		clearAllAnimation2();
	}
	
	public void clearAllAnimation2() {
		AnimationDrawable animationDrawable = (AnimationDrawable) guangquan1.getBackground();
		if (animationDrawable != null) {
			animationDrawable.stop();
		}
		startBtnContainer.clearAnimation();
		pauseBtnContainer.clearAnimation();
		restartBtnContainer.clearAnimation();
		cancelBtnContainer.clearAnimation();
		endBtnContainer.clearAnimation();
		startBtn.clearAnimation();
		pauseBtn.clearAnimation();
		restartBtn.clearAnimation();
		cancelBtn.clearAnimation();
		endBtn.clearAnimation();
		startBtnText.clearAnimation();
		pauseBtnText.clearAnimation();
		restartBtnText.clearAnimation();
		cancelBtnText.clearAnimation();
		endBtnText.clearAnimation();
		mTimePickerContainer.clearAnimation();
		mImageviewSet.clearAnimation();
		chronometerTimeContainer.clearAnimation();
		guangquan1.clearAnimation();
	}
}
