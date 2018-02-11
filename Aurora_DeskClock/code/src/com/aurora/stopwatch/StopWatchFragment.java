package com.aurora.stopwatch;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.android.db.AlarmAddUpHelp;
import com.android.deskclock.AlarmClock;
import com.android.deskclock.Alarms;
import com.android.deskclock.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import aurora.app.AuroraActivity;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LayoutAnimationController;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.aurora.AnimationView.AuroraHourGlassView;
import com.aurora.AnimationView.HourGlassEndAnimationState;
import com.aurora.AnimationView.AnimationState.OnHourGlassAnimationCompleteListener;
import com.aurora.AnimationView.HourGlassRunningAnimationState;
import com.aurora.AnimationView.StopWatchEndAnimationState;
import com.aurora.AnimationView.StopWatchInitAnimationState;
import com.aurora.AnimationView.StopWatchRunningAnimationState;
import com.aurora.timer.ChronometerAlarmAlertWakeLock;
import android.util.Log;
import com.aurora.utils.Blur;
import com.aurora.utils.NotificationOperate;
import android.content.Intent;
import android.widget.Button;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

public class StopWatchFragment extends Fragment implements Runnable{


	public static final String TAG = "StopWatchFragment";
	/**
	 * notification info
	 */
    public static final int TABNUM = 102;
	private  String contentTitle;
	private  String contentText;
	
	/**
	 * mark time info
	 */
	private static final String MARK_NO = "MARK_NO";
	private static final String MARK_VALUE = "MARK_VALUE";
	private static final String TIME_DIFF = "TIME_DIFF";

	/**
	 * the state of stopwatch
	 * running, stop or pause
	 */
	private int state = 0;

	private static final int STATE_RUNNING = 1;
	private static final int STATE_STOP = 0;
	private static final int STATE_PAUSE = 2;

	// the max time is 99 hour 59 minute 59 second 999 millisecond
	private static final long MAXTIME = 359999900;

	private Activity mActivity;
	private View mRootView;

	public static StopWatchFragment instanse;
	
	/**
	 * elapsed time
	 */
	public  long time = 0;

	public  long startTime;
	public  long lastSystemTime;

	/**
	 * the checkbox state of screen light 
	 * default is true
	 */
	private boolean isCheck = true;
	
	/**
	 * mark time list
	 */
	private List<Long> marks;
	private static int markIndex;

	/**
	 * if the last finish is normal or not.
	 * 1 is normal finish.
	 * 0 is abnormal.
	 */
	private Integer quitState;
	
	private String[] adapterFrom = null;
	private int[] adapterTo = null;
	private List<Map<String, Object>> data;
	private MarkListAdapter simpleAdapter;


	private Handler handler = new Handler();

	/**
	 * show the elapse time
	 */
	private View mTimeViewContainer;
	
	private TextView timeView;
	
	private TextView timeViewMillisecond;
	
	private TextView timeViewOneTenthMillisecond;
	
	private TextView timeViewHour;
	
    private AuroraHourGlassView guangquan;    

	private boolean isAnimationstart = false; 
	/**
	 * mark time listview
	 */
	//private ListView listView;
	private ViewGroup mFirstMarkItem;
	private ImageView mFirstMarkItemBg;
	private TextView mFirstMarkItemNo;
	private TextView mFirstMarkItemTime;
	private TextView mFirstMarkItemDiff;
	private int firstNo;
	private String firstTime;
	private String firstDiff;
	private ViewGroup mLastMarkItem;
	private ImageView mLastMarkItemBg;
	private TextView mLastMarkItemNo;
	private TextView mLastMarkItemTime;
	private TextView mLastMarkItemDiff;
	private int LastNo;
	private String LastTime;
	private String LastDiff;
	public StopWatchListView listView;

	
	private View startButtonContainer;
	private View continueButtonContainer;
	private View pauseButtonContainer;
	private View markButtonContainer;
	private View resetButtonContainer;
	private ImageView startButton;
	private ImageView continueButton;
	private ImageView pauseButton;
	private ImageView markButton;
	private ImageView resetButton;
	private TextView startButtonText;
	private TextView continueButtonText;
	private TextView pauseButtonText;
	private TextView markButtonText;
	private TextView resetButtonText;
	 PowerManager powerManager = null;    
     WakeLock wakeLock1 = null;
	    
    

	/**
	 * the screen light checkbox
	 */

	private WakeLock wakeLock;

	private SharedPreferences sharedPreferences;
	private SharedPreferences sharedPreferencesMarks;
	
	public static final  String iS_LOCALE_CHANED="isLocaleChanged"; 

	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG,  "---onAttach() start!---" );
		super.onAttach(activity);
		mActivity = activity;
        try {
        	  mOnAnimationCompleteListener = (OnStopWatchAnimationCompleteListener) activity;
           } catch (ClassCastException e) {
              throw new ClassCastException(activity.toString() + " must implement OnStopWatchAnimationCompleteListener");
        }
		Log.d(TAG,  "---onAttach() end!---" );
	}
	public WakeLock getWakeLock(){
		 
		 powerManager = (PowerManager) getActivity().getSystemService(getActivity().POWER_SERVICE);    
		    wakeLock1 = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Lock");    
		 
		 return wakeLock1;
	 }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG,  "---onCreate() start!---" );
		super.onCreate(savedInstanceState);
		
		instanse = this;

		sharedPreferences = mActivity.getSharedPreferences("stopWatchStateData",Activity.MODE_PRIVATE);
		sharedPreferencesMarks = mActivity.getSharedPreferences("marks",Activity. MODE_PRIVATE);

		state = sharedPreferences.getInt("state", STATE_STOP);
		Log.d(TAG,  "--StopWatchActivity-state: " + state + " 1111111---" );
//		if(state!=STATE_PAUSE && !sharedPreferences.getBoolean(iS_LOCALE_CHANED, false)){
//			// if the state is running or stop, then set the state stop.
//			state = STATE_STOP;		
//		}else if(state== STATE_RUNNING && sharedPreferences.getBoolean(iS_LOCALE_CHANED, false)){
//			handler.post(this);
//			handler.post(mOneTenthMillisecondRunnable);
//		}
		
		Log.d(TAG,  "--StopWatchActivity-state: " + state + " ---" );
		
		readData();
		saveLocalChangedData();
		markIndex = marks.size(); 
		Log.d(TAG,  "---markIndex: " + markIndex + "---" );
		
		Log.d(TAG,  "---marks.size(): " + marks.size() + "---" );
		Log.d(TAG,  "---onCreate() end!---" );
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG,  "---onCreateView() start!---" );
	    mRootView = inflater.inflate(R.layout.stopwatch, container, false);
		initResources();
		initState();
//		if(state== STATE_RUNNING && !sharedPreferences.getBoolean(iS_LOCALE_CHANED, false)) {
//			handler.post(this);
////			handler.post(mOneTenthMillisecondRunnable);
//			NotificationOperate.createNotifaction(mActivity, TABNUM, contentTitle, contentText);
//		}

		Log.d(TAG, "---onCreateView() end!---");
		return mRootView;
	}

	
	public static final String CLOCK_ACTION_BAR_TITLE_FONT = "/system/fonts/title.ttf";
	TextView titleText;
	/**
	 * init activity resources
	 */
	private void initResources(){
		
		Log.d(TAG,  "---init resources start!---");
		
		
		titleText = (TextView)mRootView.findViewById(R.id.titleText);
//		Typeface auroraTitleFace = Typeface.createFromFile(CLOCK_ACTION_BAR_TITLE_FONT);
//        titleText.setTypeface(auroraTitleFace);
		
		data = new ArrayList<Map<String,Object>>();
		
		
		startButton = (ImageView)mRootView.findViewById(R.id.start);
		startButtonContainer = mRootView.findViewById(R.id.start_btn_container);
		startButtonText = (TextView)mRootView.findViewById(R.id.start_btn_text);
//		startButton.setOnTouchListener(mOnTouchListener);
		startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				AlarmAddUpHelp.getInstance(mActivity).add(AlarmAddUpHelp.AlarmAddUpType.ITEM_TAG_STOP_WATCH);//启用一次秒表，添加一次
				onStartClick(view);
				ChronometerAlarmAlertWakeLock.acquireCpuWakeLock(mActivity,ChronometerAlarmAlertWakeLock.STOPWATCH);
			}
		});
		
		continueButton = (ImageView)mRootView.findViewById(R.id.continuee);
		continueButtonContainer = mRootView.findViewById(R.id.continuee_btn_container);
		continueButtonText = (TextView)mRootView.findViewById(R.id.continuee_btn_text);
//		continueButton.setOnTouchListener(mOnTouchListener);
		continueButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				onContinueClick(view);
				ChronometerAlarmAlertWakeLock.acquireCpuWakeLock(mActivity,ChronometerAlarmAlertWakeLock.STOPWATCH);
			}
		});

		pauseButton = (ImageView)mRootView.findViewById(R.id.pause);
		pauseButtonContainer = mRootView.findViewById(R.id.pause_btn_container);
		pauseButtonText = (TextView)mRootView.findViewById(R.id.pause_btn_text);
//		pauseButton.setOnTouchListener(mOnTouchListener);
		pauseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				onPauseClick(view);
				ChronometerAlarmAlertWakeLock.releaseCpuLock(ChronometerAlarmAlertWakeLock.STOPWATCH);
			}
		});

		markButton = (ImageView)mRootView.findViewById(R.id.mark);
		markButtonContainer = mRootView.findViewById(R.id.mark_btn_container);
		markButtonText = (TextView)mRootView.findViewById(R.id.mark_btn_text);
//		markButton.setOnTouchListener(mOnTouchListener);
		markButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				onMarkClick(view);
			}
		});

		resetButton = (ImageView)mRootView.findViewById(R.id.reset);
		resetButtonContainer = mRootView.findViewById(R.id.reset_btn_container);
		resetButtonText = (TextView)mRootView.findViewById(R.id.reset_btn_text);
//		resetButton.setOnTouchListener(mOnTouchListener);
		resetButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				onResetClick(view);
				ChronometerAlarmAlertWakeLock.releaseCpuLock(ChronometerAlarmAlertWakeLock.STOPWATCH);
			}
		});
		

		mTimeViewContainer =  mRootView.findViewById(R.id.timeViewContainer);
		timeView = (TextView) mRootView.findViewById(R.id.timeView);
		timeViewHour =  (TextView) mRootView.findViewById(R.id.timeView_hour);
		timeViewMillisecond = (TextView) mRootView.findViewById(R.id.timeView_millisecond);
		timeViewOneTenthMillisecond = (TextView) mRootView.findViewById(R.id.timeView_one_tenth_millisecond);
		
		listView = (StopWatchListView) mRootView.findViewById(R.id.lv_markList);
//		listView.setOverScrollMode(View.OVER_SCROLL_NEVER);
//		listView.setDivider(null);
		mFirstMarkItem = (ViewGroup)mRootView.findViewById(R.id.first_mark_item);
		mFirstMarkItemNo = (TextView)mFirstMarkItem.findViewById(R.id.no);
		mFirstMarkItemTime= (TextView)mFirstMarkItem.findViewById(R.id.time);
		mFirstMarkItemDiff = (TextView)mFirstMarkItem.findViewById(R.id.time_diff);
		mLastMarkItem = (ViewGroup)mRootView.findViewById(R.id.last_mark_item);
		mLastMarkItemNo = (TextView)mLastMarkItem.findViewById(R.id.no);
		mLastMarkItemTime= (TextView)mLastMarkItem.findViewById(R.id.time);
		mLastMarkItemDiff = (TextView)mLastMarkItem.findViewById(R.id.time_diff);
        guangquan = (AuroraHourGlassView)mRootView.findViewById(R.id.guangquan);
        mFirstMarkItem.setBackground(null);        
        mLastMarkItem.setBackground(null);
        mFirstMarkItemBg = (ImageView)mRootView.findViewById(R.id.first_mark_item_bg);
        mLastMarkItemBg = (ImageView)mRootView.findViewById(R.id.last_mark_item_bg);

		adapterFrom = new String[]{MARK_NO, MARK_VALUE, TIME_DIFF};
		adapterTo = new int[]{R.id.no, R.id.time, R.id.time_diff};
		
		contentTitle = this.getText(R.string.stopWatchNotiTitle).toString();
		contentText = this.getText(R.string.stopWatchNotiText).toString();		
		
        
//        Blur.showBgBlurView(mActivity, mRootView);
//		mRootView.getBackground().setAlpha(0);

		Log.d(TAG,  "---init resources end!---" );
	}

	/**
	 * init ui state
	 */
	private void initState(){
		Log.d(TAG,  "---init ui state start!---" );

		// init buttons
		setButtons();

		if (state == STATE_STOP){
			Log.d(TAG,  "---state: " + state + "---" );
//			timeView.setText("00:00:00");
			timeView.setText("00:00");
			timeViewMillisecond.setText(":0");
			timeViewOneTenthMillisecond.setText("0");
			timeViewHour.setText("");
			startTime = 0;
			time = 0;
		} else {
			timeView.setText(getDisplayTime(time));
			timeViewHour.setText(getDisplayHour(time));
			timeViewMillisecond.setText(getDisplayMillisecond(time));
			timeViewOneTenthMillisecond.setText(getDisplayOneTenthMillisecond(time));
    		guangquan.setAnimState(new StopWatchRunningAnimationState(mActivity, handler, guangquan));  
		}

		// show the list of time marked 
		initMarkList();

		Log.d(TAG,  "---init ui state end!---" );
	}

	boolean mIsFirst = true;
	@Override
	public void onResume() {
		Log.d(TAG,  "---onResume() start!---" );
		super.onResume();

		readData();
		markIndex = marks.size(); 
		if(state == STATE_RUNNING) {
			 if(wakeLock1 ==null){
				  getWakeLock();
				  wakeLock1.acquire();  
			
			  }
			handler.post(this);
			NotificationOperate.createNotifaction(mActivity, TABNUM, contentTitle, contentText);
			ChronometerAlarmAlertWakeLock.acquireCpuWakeLock(mActivity,ChronometerAlarmAlertWakeLock.STOPWATCH);
		} else if(state == STATE_STOP && !mIsFirst) {
			initAnimation();
		}
		
		if(state != STATE_RUNNING){
			if (Alarms.mIfDismiss == false) {
				NotificationOperate.cancelNotification(mActivity, TABNUM);
			}
		}
		
		if(mIsFirst) {
			mIsFirst = false;
			initAnimation();
		}
		Log.d(TAG,  "---onResume() end!---" );
	}

	/**
	 * set the state for the buttons
	 */
	private void setButtons() {
		Log.d(TAG,  "---set buttons start!---" );
			mTimeViewContainer.setVisibility(View.VISIBLE);
		switch (state) {
		// state of runnint
		case STATE_RUNNING:
			 if(wakeLock1 ==null){
				  getWakeLock();
				  wakeLock1.acquire();  
			
			  }
			Log.d(TAG,  "---state: running!---" );
			startButtonContainer.setVisibility(View.GONE);
			continueButtonContainer.setVisibility(View.GONE);
			pauseButtonContainer.setVisibility(View.VISIBLE);	
			markButtonContainer.setVisibility(View.VISIBLE);
			resetButtonContainer.setVisibility(View.GONE);
			markButton.setEnabled(true);
			pauseButton.setEnabled(true);
			break;
		// state of pause
		case STATE_PAUSE:
			Log.d(TAG,  "---state: pause!---" );
			guangquan.setLightRunningPoints(number);
			startButtonContainer.setVisibility(View.GONE);
			pauseButtonContainer.setVisibility(View.GONE);
			markButtonContainer.setVisibility(View.GONE);
			continueButtonContainer.setVisibility(View.VISIBLE);
			resetButtonContainer.setVisibility(View.VISIBLE);
			continueButton.setEnabled(true);
			resetButton.setEnabled(true);
			break;
		// state of stop
		case STATE_STOP:
			Log.d(TAG,  "---state: stop!---");
			startButtonContainer.setVisibility(View.VISIBLE);
			continueButtonContainer.setVisibility(View.GONE);
			pauseButtonContainer.setVisibility(View.GONE);
			markButtonContainer.setVisibility(View.GONE);
			resetButtonContainer.setVisibility(View.GONE);
			startButton.setEnabled(true);
			lastNumber=0;
			break;
		default:
			break;
		}

		Log.d(TAG,  "---set buttons end!---" );
	}

	/**
	 * read the state data from sharedPreferences
	 */
	@SuppressWarnings("unchecked")
	private void readData() {
		Log.d(TAG,  "---readData start!---");

		startTime = sharedPreferences.getLong("startTime", 0);
		time = sharedPreferences.getLong("time", 0);
//		isCheck = sharedPreferences.getBoolean("isCheck", true);
		isCheck = false;
		quitState = sharedPreferences.getInt("quitState", 0);
		
		number = sharedPreferences.getInt("number", 0);
		// init quitState value
		Editor editor = sharedPreferences.edit();
		editor.putInt("quitState", 0);
		editor.commit();

		marks = new ArrayList<Long>();
		Map<String, Long> mapMarks = (Map<String, Long>) sharedPreferencesMarks.getAll();
        if (state == STATE_STOP) {
            if (mapMarks.size() != 0) {
                clearMarkShardPreferences();
                mapMarks = (Map<String, Long>) sharedPreferencesMarks.getAll();
            }
        }
		for (int i = 0; i < mapMarks.size(); i++){
			Long mark = mapMarks.get("" + i);
			marks.add(mark);
			Log.d(TAG,  "---sharedPreferencesMarks get key: " + i + ", value: " + mapMarks.get("" + i) + "---");
		}
		Log.d(TAG,  "---readData end!---");
	}

	/**
	 * save the state data
	 */
	private void saveData() {
		Log.d(TAG,  "--StopWatchActivity-saveData start!---+state="+state );
		Editor editor = sharedPreferences.edit();
		editor.putInt("state", state);
		editor.putLong("time", time);
		editor.putLong("startTime", startTime);
		editor.putBoolean("isCheck", isCheck);
		editor.putInt("number" , number);
//		editor.putInt("animation", value);
		editor.commit();

		Log.d(TAG,  "-StopWatchActivity--saveData end!---" );
		if(wakeLock1 !=null){
			 wakeLock1.release();  
			 wakeLock1=null; 
		  }
	}
	
	private void saveMarkToSharedPreferences(long time){
		Editor editorMarks = sharedPreferencesMarks.edit();
		editorMarks.putLong("" + markIndex, time);
		editorMarks.commit();
		Log.d(TAG,  "---sharedPreferencesMarks put key: " + markIndex + ", value: " + time + "---" );
		markIndex ++;
	}
	
	private void clearMarkShardPreferences(){
		
		Editor editorMarks = sharedPreferencesMarks.edit();
		editorMarks.clear();
		editorMarks.commit();
		marks = new ArrayList<Long>();
		markIndex = 0;
	}
	
	private void saveQuitStateToSharedPreferences(){
		Editor editor = sharedPreferences.edit();
		editor.putInt("quitState", 1);
		editor.commit();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.d(TAG,  "---onSaveInstanceState() start!---");

		saveState();

		super.onSaveInstanceState(outState);
		Log.d(TAG,  "---onSaveInstanceState() end!---");
	}

	@Override
	public void onPause() {
		Log.d(TAG,  "--StopWatchActivity-onPause() start!---");
		super.onPause();
		if (state != STATE_PAUSE ) {
			lastSystemTime = 0;
			if(guangquan.getAnimState()!=null){
				guangquan.getAnimState().cancelAnimation();	
			}			
			handler.removeCallbacks(this);	
			ChronometerAlarmAlertWakeLock.releaseCpuLock(ChronometerAlarmAlertWakeLock.STOPWATCH);
		}
		saveState();
		releaseWakeLock();
		
		Log.d(TAG,  "-StopWatchActivity--onPause() end!---");
	}

	/**
	 * save the ui state
	 */
	private void saveState(){
		Log.d(TAG,  "-StopWatchActivity--save state start!---");

		saveData();

		Log.d(TAG,  "-StopWatchActivity--save state end!---");
	}

	@Override
	public void onDestroyView() {
		Log.d(TAG,  "---onDestroyview() start!---");
		super.onDestroyView();
		Log.d(TAG,  "---onDestroyview() end!---");
	}

	@Override
	public void onDestroy() {
		Log.d(TAG,  "--StopWatchActivity-onDestroy() start!---");
		if(state != STATE_STOP  &&  !sharedPreferences.getBoolean(iS_LOCALE_CHANED, false)){
//			state = STATE_PAUSE;
			handler.removeCallbacks(this);
//			handler.removeCallbacks(mOneTenthMillisecondRunnable);
			saveData();
			Log.d(TAG,  "-StopWatchActivity-onDestroy-marks.size(): " + marks.size() + "---");
		}
		releaseWakeLock();
		
		saveQuitStateToSharedPreferences();
		
		Log.d(TAG,  "-StopWatchActivity--onDestroy() end!---");
		super.onDestroy();
	}

	protected void onResetClick(View view) {
		Log.d(TAG,  "---click reset!---");

		// stop timing
		if (state == STATE_RUNNING){
			handler.removeCallbacks(this);
//			handler.removeCallbacks(mOneTenthMillisecondRunnable);
		}

		buttonMergeAnimation();
		state = STATE_STOP;

		NotificationOperate.cancelNotification(mActivity, TABNUM);
		
		marks = new ArrayList<Long>();
		clearMarkShardPreferences();
		initMarkList();

		time = 0;
		timeView.setText(getDisplayTime(time));
		timeViewHour.setText(getDisplayHour(time));
		timeViewMillisecond.setText(getDisplayMillisecond(time));
		timeViewOneTenthMillisecond.setText(getDisplayOneTenthMillisecond(time));		

		//reset data
		saveData();


	}

	protected void onPauseClick(View view) {
		if(wakeLock1 !=null){
			 wakeLock1.release();  
			 wakeLock1=null; 
		  }
		Log.d(TAG,  "---click pause!---");
		clearAnimations();
		// stop timing
		if (state == STATE_RUNNING){
			handler.removeCallbacks(this);
//			handler.removeCallbacks(mOneTenthMillisecondRunnable);
		}

		state = STATE_PAUSE;
		
		NotificationOperate.cancelNotification(mActivity, TABNUM);		

		setButtons();
		releaseWakeLock();
	}

	protected void onStartClick(View view) {
		if(wakeLock1 ==null){
			  getWakeLock();
			  wakeLock1.acquire();  
		         }
		Log.d(TAG,  "---click start!---");
		//init startTime
		startTime = System.currentTimeMillis() - time;

		guangquan.setAnimState(new StopWatchRunningAnimationState(mActivity, handler, guangquan));      				
		handler.post(this);
//		handler.post(mOneTenthMillisecondRunnable);

		state = STATE_RUNNING;
		buttonSeperateAnimation();		
		
		NotificationOperate.createNotifaction(mActivity, TABNUM, contentTitle, contentText);
		
		setButtons();
		onWakeLock();
		
		Log.d(TAG,  "------------quitState: " + quitState);
		// if lash finish is abnormal, init the mark list.
		if(quitState != 1){
			Log.d(TAG,  "quitState: " + quitState);
			clearMarkShardPreferences();
			initMarkList();
			quitState = 1;
		}
		
	}
	
	protected void onContinueClick(View view){
		if(wakeLock1 ==null){
			  getWakeLock();
			  wakeLock1.acquire();  
		  }
		Log.d(TAG,  "---click continue!---");
		//init startTime
		startTime = System.currentTimeMillis() - time;
		
		handler.post(this);
//		handler.post(mOneTenthMillisecondRunnable);

		state = STATE_RUNNING;

		NotificationOperate.createNotifaction(mActivity, TABNUM, contentTitle, contentText);
		
		setButtons();
		onWakeLock();
	}

	protected void onMarkClick(View view) {
		Log.d(TAG,  "---click mark!---");
		if(time == 0){
			return;
		}

		if(marks == null){
			Log.e(TAG, "marks == null is " + (marks == null) + ".");
			return;
		}

		// add time marked
		marks.add(time);
		saveMarkToSharedPreferences(time);
		
		Animation firstMarkItmeInAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.first_mark_list_item_show);
		mFirstMarkItem.startAnimation(firstMarkItmeInAnimation);	
		Animation firstMarkItmebgInAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.first_mark_list_item_bg_show);
		mFirstMarkItemBg.startAnimation(firstMarkItmebgInAnimation);
		//设计要求去掉listview大于四条时的重影动画
//		if(marks.size() > 4) {
//			Animation lastMarkItmebgInAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.last_mark_list_item_bg_show);
//			mLastMarkItemBg.startAnimation(lastMarkItmebgInAnimation);	
//			Animation lastMarkItmeInAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.last_mark_list_item_show);
//			mLastMarkItem.startAnimation(lastMarkItmeInAnimation);
//		}
		refreshMarkList();
//		Animation animation=AnimationUtils.loadAnimation(mActivity, R.anim.mark_list_item_show);
//	    LayoutAnimationController lac=new LayoutAnimationController(animation);
//	    lac.setOrder(LayoutAnimationController.ORDER_REVERSE);
//	    lac.setDelay(0);
//	    listView.setLayoutAnimation(lac);
//	    listView.startLayoutAnimation();
//		Animation a=AnimationUtils.loadAnimation(mActivity, R.anim.mark_list_item_show);
//		listView.startAnimation(a);

	}

    private int mLightPoints;
    private int lastNumber;
    private int number;

	@Override
	public void run() {
		Log.d(TAG,  "-StopWatchActivity-run!---");
		if(isMaxTime(time)){
			onResetWhenExcepiton();
			return;
		}

		// if timeZone, date or time is manual changed, init startTime 
		long tempSystemTime = System.currentTimeMillis();
		if(lastSystemTime != 0 && (tempSystemTime - lastSystemTime > 200 || tempSystemTime - lastSystemTime < -200)){
			startTime = System.currentTimeMillis() - time;
			if(startTime < 0) {
				onResetWhenExcepiton();
				return;
			}
		}
		
		time = tempSystemTime - startTime;
		if(time < 0) {
			onResetWhenExcepiton();
			return;
		}
		timeView.setText(getDisplayTime(time));
		timeViewHour.setText(getDisplayHour(time));
		timeViewMillisecond.setText(getDisplayMillisecond(time));

		lastSystemTime = tempSystemTime;
		number = (int)(104*(time%60000)/60000);
		if(!mStopAnimation) {
			if(lastNumber!=number || number == 0) {
				guangquan.setLightRunningPoints(number);
				lastNumber= number;
			}
		}
		
		handler.postDelayed(this, 100);
			
	}

	/**
	 * init the list of the mark times 
	 */
	private void initMarkList() {
		Log.d(TAG,  "---refresh markList!---");

		if(marks == null){
			Log.e(TAG, "marks == null is " + (marks == null) + ".");
//			mFirstMarkItem.setVisibility(View.GONE);
			return;
		}
		
		Map<Integer, String> diffTimeMap = getDiffTimeList(marks);
		data.clear();
		
		int markNo = marks.size();
		if(markNo == 0) {
//			mFirstMarkItem.setVisibility(View.GONE);
		}
		for( ; markNo > 0; markNo--){
			Map<String, Object> map = new HashMap<String, Object>();
//			map.put(MARK_NO, this.getString(R.string.btnMark) + markNo);
			if(markNo == marks.size()) {
//				mFirstMarkItem.setVisibility(View.VISIBLE);
				firstNo = markNo;
				firstTime = getFormatTime(marks.get(markNo -1));
				firstDiff = diffTimeMap.get(markNo);			
//			}else {
			}
				map.put(MARK_NO, markNo);
				map.put(MARK_VALUE, getFormatTime(marks.get(markNo -1)));
				map.put(TIME_DIFF, diffTimeMap.get(markNo));
				data.add(map);
//			}
				
			if(markNo == marks.size() -3) {
				LastNo = markNo;
				LastTime = getFormatTime(marks.get(markNo - 1));
				LastDiff = diffTimeMap.get(markNo);			
			}
		}		
		
		mFirstMarkItemNo.setText(firstNo+"");
		mFirstMarkItemTime.setText(firstTime);
		mFirstMarkItemDiff.setText(firstDiff);
		
		mLastMarkItemNo.setText(LastNo+"");
		mLastMarkItemTime.setText(LastTime);
		mLastMarkItemDiff.setText(LastDiff);
		
		if(listView.getAdapter() == null){		
			simpleAdapter = new MarkListAdapter(mActivity, data, R.layout.stopwatch_mark_item, adapterFrom, adapterTo);

			listView.setAdapter(simpleAdapter);
		}else{
			simpleAdapter.notifyDataSetChanged();
		}
	}
	
	/**
	 * get the different time of two adjacent mark time
	 * @param marks the list of the mark time
	 * @return
	 */
	private Map<Integer, String> getDiffTimeList(List<Long> marks){
		
		Map<Integer, String> diffTimeMap = new HashMap<Integer, String>();
		
		if(marks == null){
			Log.e(TAG, "marks == null is " + (marks == null) + ".");
			return diffTimeMap;
		}

		long lastMark = 0;
		int markNo = 1;

		String diff = "";
		for(long mark : marks){
			diff = getFormatDiffTime(lastMark, mark);
			diffTimeMap.put(markNo, diff);
			lastMark = mark;
			markNo ++;
		}
		return diffTimeMap;
	}
	
	/**
	 * refresh mark list
	 */
	private void refreshMarkList() {
		Log.d(TAG,  "---refresh markList!---");

		if(marks == null){
			Log.e(TAG, "marks == null is " + (marks == null) + ".");
//			mFirstMarkItem.setVisibility(View.GONE);
			return;
		}
		
		
		Map<Integer, String> diffTimeMap = getDiffTime(marks);

		int markNo = marks.size();
		if(markNo <= 0){
//			mFirstMarkItem.setVisibility(View.GONE);
			return;
		}
//		mFirstMarkItem.setVisibility(View.VISIBLE);
		Map<String, Object> map = new HashMap<String, Object>();
//		map.put(MARK_NO, this.getString(R.string.btnMark) + markNo);
		map.put(MARK_NO, markNo);
		map.put(MARK_VALUE, getFormatTime(marks.get(markNo -1)));
		map.put(TIME_DIFF, diffTimeMap.get(markNo));
		data.add(0, map);	
//		if(markNo > 1) {
//			Map<String, Object> map = new HashMap<String, Object>();
//			map.put(MARK_NO, firstNo);
//			map.put(MARK_VALUE, firstTime);
//			map.put(TIME_DIFF, firstDiff);
//			data.add(0, map);
//		}
		
		firstNo = markNo;
		firstTime = getFormatTime(marks.get(markNo -1));
		firstDiff = diffTimeMap.get(markNo);
		
		mFirstMarkItemNo.setText(firstNo+"");
		mFirstMarkItemTime.setText(firstTime);
		mFirstMarkItemDiff.setText(firstDiff);
		
		if(markNo-4>0) {
//			LastNo = markNo-4;
//			LastTime = getFormatTime(marks.get(markNo -5));
//			LastDiff = diffTimeMap.get(markNo-4);
			Map<String, Object> m = new HashMap<String, Object>();
			m = data.get(4); 
			LastNo = (Integer) m.get(MARK_NO);
			LastTime = (String) m.get(MARK_VALUE);
			LastDiff = (String) m.get(TIME_DIFF);
			
			mLastMarkItemNo.setText(LastNo + "");
			mLastMarkItemTime.setText(LastTime);
			mLastMarkItemDiff.setText(LastDiff);
		}
		
		
		if(listView.getAdapter() == null || listView.getFirstVisiblePosition() != 0){
		    simpleAdapter = new MarkListAdapter(mActivity, data, R.layout.stopwatch_mark_item, adapterFrom, adapterTo);	
			listView.setAdapter(simpleAdapter);
		}else{
			simpleAdapter.notifyDataSetChanged();
		}
		simpleAdapter.setAnim(true);

		Log.d(TAG,  "---refresh markList!---end!");
	}

	/**
	 * calculate the difference time, between this time and last time
	 * @param marks
	 * @return
	 */
	private Map<Integer, String> getDiffTime(List<Long> marks){
		
		Map<Integer, String> diffTimeMap = new HashMap<Integer, String>();
		
		if(marks == null){
			Log.e(TAG, "marks == null is " + (marks == null) + ".");
			return diffTimeMap;
		}

		int markLength = marks.size();
		
		String diff = "";
		if(markLength == 1){
			diff = getFormatDiffTime(0, marks.get(markLength - 1));
			diffTimeMap.put(markLength, diff);
		}
		
		if(markLength > 1){
			diff = getFormatDiffTime(marks.get(markLength - 2), marks.get(markLength - 1));
			diffTimeMap.put(markLength, diff);
		}
		return diffTimeMap;
	}

	/**
	 * format the time 
	 * @param time
	 * @return
	 */
	private String getFormatTime(long time) {
		
		long millisecond = time % 1000;
		long second = (time / 1000) % 60;
		long minute = (time / 1000 / 60 ) % 60;
		long hour = time / 1000 / 60 / 60;

		String strMillisecond = "" + (millisecond / 100);
		String strSecond = ("00" + second).substring(("00" + second).length() - 2);
		String strMinute = ("00" + minute).substring(("00" + minute).length() - 2);
		String strHour = ("00" + hour).substring(("00" + hour).length() -2);

		if(hour == 0) {
			return strMinute + ":" + strSecond + ":" + strMillisecond;
		} else {
			return strHour + ":" + strMinute + ":" + strSecond + ":" + strMillisecond;
		}
	}

	/**
	 * format the time to display
	 * @param time
	 * @return
	 */
	private String mStrHour = "";
	private String getDisplayTime(long time) {

		long second = (time / 1000) % 60;
		long minute = (time / 1000 / 60 ) % 60;
//		long hour = time / 1000 / 60 / 60;

		String strSecond = ("00" + second).substring(("00" + second).length() - 2);
		String strMinute = ("00" + minute).substring(("00" + minute).length() - 2);
//		String strHour = ("00" + hour).substring(("00" + hour).length() -2);

//		return strHour + ":" + strMinute + ":" + strSecond;
		return  strMinute + ":" + strSecond;
	}
	
	private String getDisplayHour(long time) {
		long hour = time / 1000 / 60 / 60;
		if(hour != 0) {
			mStrHour = this.getString(R.string.hours, ("00" + hour).substring(("00" + hour).length() -2));
//			mStrHour += this.getString(R.string.chronometer_alart_title_hour);
		} else {
			mStrHour = "";
		}
		return mStrHour;
	}

	/**
	 * format the time to display
	 * @param time
	 * @return
	 */
	private String getDisplayMillisecond(long time) {
		
		return ":" + ( time % 1000 / 100 );
	}
	
	private String getDisplayOneTenthMillisecond(long time) {
		
		return   time % 1000 / 10 % 10 + "";
	}

	
	
	/**
	 * calculate the difference time, between this time and last time
	 * @param lastMark the time last mark
	 * @param mark	the time this mark
	 * @return
	 */
	private String getFormatDiffTime(long lastMark, long mark){
		Log.d(TAG,  "---lastmark: " + lastMark + ", mark: " + mark + " ---");
//		String result = "(+00:00:00.0)";
		String result = "+00:00.0";
		if(lastMark == 0){
			return result;
		}
		
		long diff = parseTime(getFormatTime(mark)) - parseTime(getFormatTime(lastMark));
		return  "+" + getFormatTime(diff);
	}
	
	private Long parseTime(String timeStr){
		long result = 0l;
		Log.d(TAG,  "---lastmark: timeStr = " + timeStr);
		if("".equals(timeStr) || (timeStr.split(":").length != 4 && timeStr.split(":").length != 3)){
			return result;
		}
		
		String[] timeArr = timeStr.split(":");
		if(timeArr.length == 4) {
			Long hour = Long.parseLong(timeArr[0]);
			Long minute = Long.parseLong(timeArr[1]);
			Long second = Long.parseLong(timeArr[2]);
			Long millisecond = Long.parseLong(timeArr[3]);		
			return hour * 60 * 60 * 1000 + minute * 60 *1000 + second * 1000 + millisecond * 100;
		} else {
			Long minute = Long.parseLong(timeArr[0]);
			Long second = Long.parseLong(timeArr[1]);
			Long millisecond = Long.parseLong(timeArr[2]);		
			return minute * 60 *1000 + second * 1000 + millisecond * 100;
		}
	}
	
    protected void onWakeLock() {
		if(state == STATE_RUNNING && isCheck){
			acquireWakeLock();
		}else{
			releaseWakeLock();
		}
	}

	/**
	 * set screen light always
	 */
	private void acquireWakeLock() {
//		AlarmClock alarmClock = (AlarmClock) mActivity;
//		alarmClock.getmViewPager().setKeepScreenOn(true);
	}

	/**
	 * cancel screen light 
	 */
	private void releaseWakeLock() {
		Log.d(TAG,  "---set screen light off!---");
//		AlarmClock alarmClock = (AlarmClock) mActivity;
//		alarmClock.getmViewPager().setKeepScreenOn(false);
	}

	/**
	 * if the elapse time is the max time
	 * @param time  the elapse time 
	 * @return
	 */
	private boolean isMaxTime(long time){
		Log.d(TAG,  " time = " + time + "MAXTIME=" + MAXTIME);
		if(time > MAXTIME){
			return true;
		}
		return false;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public boolean isCheck() {
		return isCheck;
	}

	public void setCheck(boolean isCheck) {
		this.isCheck = isCheck;
	}
	
    public void gnSaveData() {
        Log.d(TAG, "---gnSaveData start!---");
        Editor editor = sharedPreferences.edit();
        editor.putBoolean("isCheck", isCheck);
        editor.commit();

        Log.d(TAG, "---gnSaveData end!---");
    }

    private void saveLocalChangedData(){
    	Editor editor = sharedPreferences.edit();
        editor.putBoolean(iS_LOCALE_CHANED, false);
        editor.commit();
    }

    Runnable mInitRunnable = new Runnable() {
		public void run() {
	    	if(state == STATE_STOP) {   
	    		  Log.v(TAG, "---mInitRunnable run");
	    		stopWatchEnterAnimation();
				guangquan.setAnimState(new StopWatchInitAnimationState(mActivity, handler, guangquan) );      				
    		    guangquan.getAnimState().startAnimation(null);
	    	} 
		}
    };
    
    
    public void initAnimation() {
    	if(mRootView != null) {
			if(state == STATE_STOP) {
				handler.removeCallbacks(mInitRunnable);
				handler.post(mInitRunnable);
			} else {
				stopWatchEnterAnimation();
	  	        Log.v(TAG, "---initAnimation liguangyu");
    			guangquan.setAnimState(new StopWatchRunningAnimationState(mActivity, handler, guangquan));
    			listViewEnterAnimation();
			}
			titleEnterAnimation();
    	} 
    }
    
    public interface OnStopWatchAnimationCompleteListener {
        void onStopWatchFragmentAnimationComplete();
    }
    
    private OnStopWatchAnimationCompleteListener mOnAnimationCompleteListener = null;
    
    
    public boolean endAnimation() {
    	if(wakeLock1 !=null){
 			
			  wakeLock1.release(); 
			  wakeLock1=null; 
		  }
	    Log.v(TAG, "---endAnimation liguangyu mRootView = " + mRootView);
    	if(mRootView != null) {
			mStopAnimation = true;
			handler.post(mEndRunnable);
			titleExitAnimation();
			return true;        		
    	}
    	return false;
  }
      
   public boolean restoreAnimation() {
	    Log.v(TAG, "---restoreAnimation liguangyu");
	    mStopAnimation = false;
	  	if(mRootView != null) {
	  		clearAnimations();
	      	setButtons();
	  	}
	  	return false;
  	}
	private volatile boolean mStopAnimation = false;
	
	
    
    
//    Runnable mOneTenthMillisecondRunnable = new Runnable() {
//		public void run() {
//			timeViewOneTenthMillisecond.setText(getDisplayOneTenthMillisecond(time));
//			handler.postDelayed(this, 10);
//		}
//    };
	
    Runnable mEndRunnable = new Runnable() {
  		public void run() {	
  		    Log.v(TAG, "mEndRunnable ");
  	       	Animation buttonOutAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_out);  
  	       	if(state == STATE_STOP) {
  				if(startButton.getVisibility() == View.VISIBLE) {
  					startButtonContainer.startAnimation(buttonOutAnimation);
  				}
  	       	} else if (state == STATE_RUNNING) {
  				if(pauseButton.getVisibility() == View.VISIBLE) {
  					pauseButtonContainer.startAnimation(buttonOutAnimation);
  				}
  				if(markButton.getVisibility() == View.VISIBLE) {
  					markButtonContainer.startAnimation(buttonOutAnimation);
  				}
  	       	} else {
  				if(continueButton.getVisibility() == View.VISIBLE) {
  					continueButtonContainer.startAnimation(buttonOutAnimation);
  				}
  				if(resetButton.getVisibility() == View.VISIBLE) {
  					resetButtonContainer.startAnimation(buttonOutAnimation);
  				}	
  	       	}
  	       	
  	       	if(state != STATE_STOP) {
  	  	        listViewExitAnimation();
	       	}

		    Animation timeContainerOutAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.stopwatch_time_container_out);	
		    timeContainerOutAnimation.setDuration(270);
		    timeContainerOutAnimation.setAnimationListener(new Animation.AnimationListener() {				
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
//		  	        Log.v(TAG, "---mEndRunnable onAnimationEnd");
//					mTimeViewContainer.setVisibility(View.INVISIBLE);
//					startButtonContainer.setVisibility(View.GONE);
//					continueButtonContainer.setVisibility(View.GONE);
//					pauseButtonContainer.setVisibility(View.GONE);
//					markButtonContainer.setVisibility(View.GONE);
//					resetButtonContainer.setVisibility(View.GONE);	
  					if(mOnAnimationCompleteListener!= null ) {
  						mOnAnimationCompleteListener.onStopWatchFragmentAnimationComplete();
  					}
					
				}
			});
			mTimeViewContainer.startAnimation(timeContainerOutAnimation);

			guangquan.setAnimState(new StopWatchEndAnimationState(mActivity, handler, guangquan) );      				
		    guangquan.getAnimState().startAnimation(null);  			      		
	       }
      };
      
      public void stopWatchEnterAnimation() {
        	if(mRootView != null) {
	  	        Log.v(TAG, "---stopWatchEnterAnimation");
    		    Animation buttonInAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_in);	
    		    Animation buttonTextInAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_text_in);	 	 
    		    if(state == STATE_STOP){
      		    	if(startButtonContainer.getVisibility() == View.VISIBLE) {
      		    		startButton.startAnimation(buttonInAnimation);   
      		    		startButtonText.startAnimation(buttonTextInAnimation); ;
        			}
    		    	
    		    } else if(state == STATE_RUNNING) {
    		    	if(pauseButtonContainer.getVisibility() == View.VISIBLE) {
        				pauseButton.startAnimation(buttonInAnimation);   
        				pauseButtonText.startAnimation(buttonTextInAnimation); ;
        			}
    		  		
        			if(markButtonContainer.getVisibility() == View.VISIBLE) {
        				markButton.startAnimation(buttonInAnimation);   
        				markButtonText.startAnimation(buttonTextInAnimation); 
        			}
    		    } else {
    	  			if(continueButtonContainer.getVisibility() == View.VISIBLE) {
        				continueButton.startAnimation(buttonInAnimation);   
        				continueButtonText.startAnimation(buttonTextInAnimation); 
        			}
      
        			if(resetButtonContainer.getVisibility() == View.VISIBLE) {
        				resetButton.startAnimation(buttonInAnimation);   
        				resetButtonText.startAnimation(buttonTextInAnimation); 
        			}
    		    }
 
    			Animation timeViewInAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.stopwatch_time_container_in);
    			mTimeViewContainer.startAnimation(timeViewInAnimation);  
        		
        	}
      }

     public void buttonSeperateAnimation() {
    	  	if(mRootView != null) {
	  	        Log.v(TAG, "---buttonSeperateAnimation");
		    	Animation leftButtonAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_center_to_left);
		       	Animation rightButtonAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_center_to_right); 
		     	Animation centerButtonAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.center_button_out); 
		     	Animation leftButtonTextAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_text_center_to_left);
		       	Animation rightButtonTextAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_text_center_to_right);
		       	leftButtonAnimation.setAnimationListener(new Animation.AnimationListener() {				
	  				@Override
	  				public void onAnimationStart(Animation animation) {  
	  					setButtonsEnable(false);
	  				}
	  				
	  				@Override
	  				public void onAnimationRepeat(Animation animation) {			
	  				}
	  				
	  				@Override
	  				public void onAnimationEnd(Animation animation) {  
	  					setButtonsEnable(true);	 
	  				}
	  			});
		       	startButtonContainer.startAnimation(centerButtonAnimation);
		       	pauseButton.startAnimation(rightButtonAnimation);
		       	markButton.startAnimation(leftButtonAnimation);
		       	pauseButtonText.startAnimation(rightButtonTextAnimation);
		       	markButtonText.startAnimation(leftButtonTextAnimation);
        		
        	}
     } 
     
     public void buttonMergeAnimation() {
 	  	if(mRootView != null) {
	  	    Log.v(TAG, "---buttonMergeAnimation");
	    	Animation leftButtonAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_left_to_center);
	       	Animation rightButtonAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_right_to_center); 
	     	final Animation centerButtonAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.center_button_in_delay); 
	     	Animation leftButtonTextAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_text_left_to_center);
	       	Animation rightButtonTextAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.bottom_button_text_right_to_center); 
	       	rightButtonTextAnimation.setAnimationListener(new Animation.AnimationListener() {				
  				@Override
  				public void onAnimationStart(Animation animation) {  
  					setButtonsEnable(false);
  				}
  				
  				@Override
  				public void onAnimationRepeat(Animation animation) {			
  				}
  				
  				@Override
  				public void onAnimationEnd(Animation animation) {  	
//  		 			continueButtonContainer.setVisibility(View.GONE);
//  			       	resetButtonContainer.setVisibility(View.GONE);
  					setButtons();
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
  					setButtonsEnable(true);
  				}
  			});
//			handler.postDelayed(new Runnable(){
//				public void run(){
				 	startButtonContainer.startAnimation(centerButtonAnimation);
//				}
//			}, mActivity.getResources().getInteger(R.integer.center_button_show_delay_duration));			      
  			continueButton.startAnimation(rightButtonAnimation);
	       	resetButton.startAnimation(leftButtonAnimation);
	       	continueButtonText.startAnimation(rightButtonTextAnimation);
	       	resetButtonText.startAnimation(leftButtonTextAnimation);
			stopWatchEnterAnimation2();
 	  	}
     }
     
     public void stopWatchEnterAnimation2() {
     	if(mRootView != null) {
	  	     Log.v(TAG, "---stopWatchEnterAnimation2");        		    		    
			Animation timeViewInAnimation=AnimationUtils.loadAnimation(mActivity, R.anim.stopwatch_time_container_in);
			mTimeViewContainer.startAnimation(timeViewInAnimation);
			guangquan.setAnimState(new StopWatchInitAnimationState(mActivity, handler, guangquan) );      				
		    guangquan.getAnimState().startAnimation(null);     		
     	}  			    					       
     }
     
// 	OnTouchListener mOnTouchListener = new OnTouchListener() {
//	    public boolean onTouch(View v, MotionEvent event) {
//        	TextView BtnText = null;
//	        
//	        if(v.getId() == R.id.start) {
//	        	BtnText = (TextView) mRootView.findViewById(R.id.start_btn_text);
//	        } else  if(v.getId() == R.id.pause) {
//	        	BtnText = (TextView) mRootView.findViewById(R.id.pause_btn_text);
//	        } else  if(v.getId() == R.id.mark) {
//	        	BtnText = (TextView) mRootView.findViewById(R.id.mark_btn_text);
//	        } else  if(v.getId() == R.id.reset) {
//	        	BtnText = (TextView) mRootView.findViewById(R.id.reset_btn_text);
//	        } else  if(v.getId() == R.id.continuee) {
//	        	BtnText = (TextView) mRootView.findViewById(R.id.continuee_btn_text);
//	        }
//	        if(BtnText!=null) {
//		        if(event.getAction() == MotionEvent.ACTION_UP){  
//	            	BtnText.setTextColor(mActivity.getResources().getColor(R.color.aurora_btn_color));  
//	            } else if(event.getAction() == MotionEvent.ACTION_DOWN){  
//	            	BtnText.setTextColor(mActivity.getResources().getColor(R.color.aurora_btn_press_color));  
//	            }
//	        }
//	        
//	        return false;  
//	    }  
//	};
	
	private void setButtonsEnable(boolean value) {
		startButton.setEnabled(value);
		continueButton.setEnabled(value);
		pauseButton.setEnabled(value);
		markButton.setEnabled(value);
		resetButton.setEnabled(value);
	}
	
	private void clearAnimations() {
		mTimeViewContainer.clearAnimation();
		startButtonContainer.clearAnimation();
		continueButtonContainer.clearAnimation();
		pauseButtonContainer.clearAnimation();
		markButtonContainer.clearAnimation();
		resetButtonContainer.clearAnimation();
	  	  startButton.clearAnimation();
		  continueButton.clearAnimation();
		  pauseButton.clearAnimation();
		  markButton.clearAnimation();
		  resetButton.clearAnimation();
		 startButtonText.clearAnimation();
		 continueButtonText.clearAnimation();
		 pauseButtonText.clearAnimation();
		 markButtonText.clearAnimation();
		 resetButtonText.clearAnimation();
	}
	
	private void titleEnterAnimation() {
		Animation a = AnimationUtils.loadAnimation(mActivity, R.anim.alarmtitle_enter);
		titleText.startAnimation(a);
	}
	
	private void titleExitAnimation() {
		Animation a = AnimationUtils.loadAnimation(mActivity, R.anim.alarmtitle_exit);
		titleText.startAnimation(a);
	}
	
	private void listViewEnterAnimation() {
		Animation ringerInAnimation = AnimationUtils.loadAnimation(
				mActivity, R.anim.center_button_in);
		listView.startAnimation(ringerInAnimation);
	}
	
	private void listViewExitAnimation() {
		Animation ringerInAnimation = AnimationUtils.loadAnimation(
				mActivity, R.anim.center_button_out);
		listView.startAnimation(ringerInAnimation);
	}
	
	private void onResetWhenExcepiton() {
		startButtonContainer.setVisibility(View.VISIBLE);
		continueButtonContainer.setVisibility(View.GONE);
		pauseButtonContainer.setVisibility(View.GONE);
		markButtonContainer.setVisibility(View.GONE);
		resetButtonContainer.setVisibility(View.GONE);
		startButton.setEnabled(false);
		resetButton.setEnabled(true);
		onResetClick(null);
	}
}
