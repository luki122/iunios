package com.android.deskclock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.android.db.AlarmAddUpHelp;
import com.android.deskclock.R;
import com.android.deskclock.SettingsActivity;
import com.android.deskclock.AlarmClockFragment.onAlarmClockAnimationCompleteListener;
import com.android.deskclock.RepeatListPopupWindow.OnButtonListClickListener;
import com.android.deskclock.SettingPopupWindow.OnButtonSettingClickListener;
import com.aurora.AnimationView.HourGlassEndingAnimationState;
import com.aurora.AnimationView.HourGlassIdleAnimationState;
import com.aurora.AnimationView.HourGlassRunningAnimationState;
import com.aurora.AnimationView.HourGlassRunningPauseAnimationState;
import com.aurora.AnimationView.HourGlassStartingAnimationState;
import com.aurora.AnimationView.AnimationState.OnHourGlassAnimationCompleteListener;
import com.aurora.AnimationView.AuroraAnimationDrawable.OnFrameAnimationCompleteListener;
import com.aurora.stopwatch.StopWatchFragment;
import com.aurora.stopwatch.StopWatchFragment.OnStopWatchAnimationCompleteListener;
import com.aurora.timer.TimerFragment;
import com.aurora.worldtime.Constants;
import com.aurora.worldtime.WorldTimeFragment;
import com.aurora.worldtime.WorldTimeSearchActivity;
import com.aurora.worldtime.WorldTimeFragment.onWorldTimeAnimationCompleteListener;

import android.R.bool;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraMenuBase;
import aurora.widget.AuroraUtil;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import android.os.Handler;
import android.os.Build.VERSION;
import android.provider.ContactsContract.CommonDataKinds.Im;

import com.aurora.timer.TimerFragment.OnTimerAnimationCompleteListener;
import com.aurora.utils.Blur;
import com.aurora.utils.NotificationOperate;
import com.aurora.stopwatch.StopWatchFragment.OnStopWatchAnimationCompleteListener;

public class AlarmClock extends AuroraActivity implements OnClickListener,OnTimerAnimationCompleteListener,
						OnStopWatchAnimationCompleteListener, onWorldTimeAnimationCompleteListener, onAlarmClockAnimationCompleteListener{
	
	public static final String PREFERENCES = "AlarmClock";
	
	/**
	 * This must be false for production. If true, turns on logging, test code,
	 * etc.
	 */
	public static final boolean DEBUG = true;
	
	private RelativeLayout mainLayout;

    private AlarmClockFragment alarmclockFragment;  
  
    private StopWatchFragment stopwatchFragment;  

    private WorldTimeFragment worldtimeFragment;  
  
    private TimerFragment timerFragment;  

    private ImageView alarmclockLayout;  
  
    private ImageView stopwatchLayout;  
 
    private ImageView timerLayout;  

    private ImageView worldtimeLayout;  
    
 //tab 布局   
   private LinearLayout tablinear;
 
  
    private FragmentManager fragmentManager;
    
    private ImageView addImageView;
    private ImageView settingImageView;
    private View addImageViewContainer;
    private TextView titleText;
    
    
    private boolean isTabUp = true;
    
    private int tabnum = 0;
    private int lastTabnum = 0;
    
    //private SettingPopupWindow mSetting;
    
    //aurora add by tangjun 2014.1.22 退出动画停止前不响应标签点击
    private boolean isEndAnimRun = false;
    
    public static final String TIMEPICKERBACKGROUND = "/system/iuni/aurora/change/lockscreen/RingPicker/Default-Wallpaper.png";
    public static final String LOCKSCREENPATH = "/data/aurora/change/lockscreen/wallpaper.png";
    private static final String LOCKSCREENDEFAULTPATH = "/system/iuni/aurora/change/lockscreen/City/";
    private static final String LOCKSCREENDEFAULTPATH2 = "/system/iuni/aurora/change/lockscreen/Dream/";
    private static final String LOCKSCREENDEFAULTPATH3 = "/system/iuni/aurora/change/lockscreen/Fascinating/";//女性化主题
    private static final String LOCKSCREENDEFAULTPATH4 ="/system/iuni/aurora/change/lockscreen/Timely/";
    private static final int STARTHOUR = 6;
    
    private Typeface auroraTitleFace;
    public static final String CLOCK_ACTION_BAR_TITLE_FONT = "/system/fonts/Roboto-Thin.ttf";
    
    public static String lockscreenDefaultPath;
    
    private boolean isOnPause = false;
    
    //适配4.4以下版本不存在的变量
    public static final String VALUE_RINGTONE_SILENT = "silent";
    public static final String EXTRA_LENGTH = "android.intent.extra.alarm.LENGTH";
    public static final String EXTRA_MESSAGE = "android.intent.extra.alarm.MESSAGE";
    public static final String EXTRA_RINGTONE = "android.intent.extra.alarm.RINGTONE";
    public static final String EXTRA_HOUR = "android.intent.extra.alarm.HOUR";
    public static final String EXTRA_MINUTES = "android.intent.extra.alarm.MINUTES";
    public static final String EXTRA_SKIP_UI = "android.intent.extra.alarm.SKIP_UI";
    public static final String ACTION_SET_TIMER = "android.intent.action.SET_TIMER";
    public static final String ACTION_SHOW_ALARMS = "android.intent.action.SHOW_ALARMS";
    public static final String ACTION_SET_ALARM = "android.intent.action.SET_ALARM";
    public static final String EXTRA_DAYS = "android.intent.extra.alarm.DAYS";
    public static boolean mIsProcessExist;

    /**
     * @param context    从固定路径取默认的12张壁纸
     * @return
     */
    
    static int lastIndex = -1;
    private void  refreshBgIfNeeded() {
    	int index = getBgIndex();
		if (lastIndex != index) {
			getLockScreenPath();
			Blur.showBgBlurView(this, mainLayout, lockscreenDefaultPath);
//			if (mScreenBitmapMatrix != null) {
//				mScreenBitmapMatrix.recycle();
//				mScreenBitmapMatrix = null;
//			}
//			mScreenBitmapMatrix = Blur.showBgBlurView(this, mainLayout);
		}
    }
    
    private int getBgIndex(){
    	int index;
    	Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        int hour = c.get(Calendar.HOUR_OF_DAY);
        
        if ( hour >= STARTHOUR ) {
        	index = (hour - STARTHOUR) / 2 + 1;
        } else {
        	index = hour / 2 + 10;
        }
    	
        return index;
        
    }
    
    private boolean isWomanTheme(){
    	File file = new File(LOCKSCREENDEFAULTPATH3);
    	return file.exists();
    }
    
    
    private void getLockScreenPath() {
    	boolean isWomanTheme = isWomanTheme();
    	android.util.Log.e("jadon", "isWomanTheme = "+isWomanTheme);
    	int index = getBgIndex();
        lastIndex = index;
        if ( index < 10 ) {
        	lockscreenDefaultPath = "0" + String.valueOf(index);
        } else {
        	lockscreenDefaultPath = String.valueOf(index);
        }
        if(isWomanTheme)
        {
        	lockscreenDefaultPath = LOCKSCREENDEFAULTPATH4 + "data" + lockscreenDefaultPath + ".png";
        }else{
	        if (VERSION.SDK_INT > 18) {
	        	 lockscreenDefaultPath = LOCKSCREENDEFAULTPATH2 + "data" + lockscreenDefaultPath + ".png";				
			}else{			
				 lockscreenDefaultPath = LOCKSCREENDEFAULTPATH + "data" + lockscreenDefaultPath + ".png";					
			}
        }
        
        Log.e("--lockscreenDefaultPath = ----" + lockscreenDefaultPath);
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//this.setTheme(com.aurora.R.style.Theme_Aurora_Dark_Transparent);  //cjs delete
		super.onCreate(savedInstanceState);
		hideNaviBar();
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		isCtsTest = getIntent().getIntExtra(EXTRA_LENGTH, 0) != 0;
		ctsShowTitle = getIntent().getStringExtra(EXTRA_MESSAGE);
        //this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.mainactivity);
		initViews();
		fragmentManager = getFragmentManager();
      	alarmclockFragment = (AlarmClockFragment) fragmentManager.findFragmentByTag("" + 0);
     	worldtimeFragment = (WorldTimeFragment) fragmentManager.findFragmentByTag("" + 1);
    	stopwatchFragment = (StopWatchFragment) fragmentManager.findFragmentByTag("" + 2);
    	timerFragment = (TimerFragment) fragmentManager.findFragmentByTag("" + 3);
        FragmentTransaction transaction = fragmentManager.beginTransaction();  
    	if(alarmclockFragment != null) {
    		transaction.remove(alarmclockFragment);
    		alarmclockFragment = null;
        }
    	if(worldtimeFragment != null) {
    		transaction.remove(worldtimeFragment);
    		worldtimeFragment = null;
        }
    	if(stopwatchFragment != null) {
    		transaction.remove(stopwatchFragment);
    		stopwatchFragment = null;
        }
    	if(timerFragment != null) {
    		transaction.remove(timerFragment);
    		timerFragment = null;
        }
        transaction.commit();  
//    	alarmclockFragment = new AlarmClockFragment();  
//    	worldtimeFragment = new WorldTimeFragment();  
//    	stopwatchFragment = new TimerFragment();  
//    	timerFragment = new TimerFragment();  
		// 第一次启动时选中第0个tab  
        int tabNum = 0;
        Intent intent = this.getIntent();
        if (intent != null) {
            tabNum = intent.getIntExtra("tabNum", tabNum);
        }
		boolean isFromTimerReceiver = getIntent().getBooleanExtra("isFromTimerReceiver", false);
        if(isFromTimerReceiver)  {
        	tabNum=3;
        	updateKeyguardPolicy(true);
        }
        setTabSelection(tabNum); 

		registerReceiverForFontChange();
		
		if ( !mIsProcessExist ) {
			android.util.Log.e("333333", "---onCreate resetAlarm-----");
			Alarms.disableExpiredAlarms(this);
            Alarms.setNextAlert(this);
		}
		
		mIsProcessExist = true;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	protected void hideNaviBar() {
		int version = Build.VERSION.SDK_INT;
		if (version >= 14) {

			View decorView = getWindow().getDecorView();
			int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
			if (true) {
				
					//	View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					//	| View.SYSTEM_UI_FLAG_FULLSCREEN
				uiOptions = 	View.SYSTEM_UI_FLAG_LAYOUT_STABLE
						| View.SYSTEM_UI_FLAG_LOW_PROFILE;
			}
			decorView.setSystemUiVisibility(uiOptions);

			decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

						@Override
						public void onSystemUiVisibilityChange(int visibility) {

							if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {

								delayShowNaviBar();

							} else {

							}
						}
					});

		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			hideNaviBar();
		}
	}

	protected void delayShowNaviBar() {
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				hideNaviBar();
			}
		}, 3000);
	}
	
	public boolean isCtsTest = false;
	
    protected void onNewIntent(Intent intent) {
    	Log.e("AlarmClock onNewIntent--------");
    	setIntent(intent);
	    int tabNum = -1;
        tabNum = intent.getIntExtra("tabNum", tabNum);   
        ctsShowTitle = getIntent().getStringExtra(EXTRA_MESSAGE);
        isCtsTest = getIntent().getIntExtra(EXTRA_LENGTH, 0) != 0;
		boolean isFromTimerReceiver = intent.getBooleanExtra("isFromTimerReceiver", false);
        if(isFromTimerReceiver)  {
        	tabNum=3;
        	updateKeyguardPolicy(true);
        	setTabSelection3();
        	return;
        }
        //aurora modify liguangyu 20140410 for #3990  start
        if(tabNum!=-1) setTabSelectionNoAnimation(tabNum);
        //aurora modify liguangyu 20140410 for #3990  end
	}
    /**
	 * 功能描述：判断按键 菜单的显示与隐藏
	 */	
/*	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean nVats = true;
		if ( tabnum != 0 ) {
			return super.onKeyUp(keyCode, event);
		}
		if ( keyCode == KeyEvent.KEYCODE_MENU ) {
			if( !mSetting.isSettingListPopupWindowShow() ) {
				mSetting.showSettingListPopupWindow();
			} else {
				mSetting.dismissSettingListPopupWindow();
			}
		} else if (keyCode == KeyEvent.KEYCODE_BACK ) {
			if(  mSetting.isSettingListPopupWindowShow() ) {
				mSetting.dismissSettingListPopupWindow();
			} else {
				nVats = super.onKeyUp(keyCode, event);
			}
		} else {
			nVats = super.onKeyUp(keyCode, event);
		}
		return nVats;
	}*/
	
	/** 
     * 在这里获取到每个需要用到的控件的实例，并给它们设置好必要的点击事件。 
     */  
//	public static Bitmap mScreenBitmap;
//	public static Bitmap mScreenBitmapMatrix;
    private void initViews() {  
    	
    	mainLayout = (RelativeLayout)findViewById(R.id.mainlayout);
    	tablinear=(LinearLayout)findViewById(R.id.tablinear);
    		
    	alarmclockLayout = (ImageView)findViewById(R.id.alarmclock_layout);  
    	stopwatchLayout = (ImageView)findViewById(R.id.stopwatch_layout);  
    	timerLayout = (ImageView)findViewById(R.id.timer_layout);  
    	worldtimeLayout = (ImageView)findViewById(R.id.worldtime_layout);  
        
        alarmclockLayout.setOnClickListener(this);  
        stopwatchLayout.setOnClickListener(this);  
        timerLayout.setOnClickListener(this);  
        worldtimeLayout.setOnClickListener(this);
        
        addImageView = (ImageView)findViewById(R.id.addimageview);
        addImageView.setOnClickListener(this);
        settingImageView = (ImageView)findViewById(R.id.settingimageview);
        settingImageView.setOnClickListener(this);
        addImageViewContainer = findViewById(R.id.addimageview_container);      
        titleText = (TextView)findViewById(R.id.titleText);
//        auroraTitleFace = Typeface.createFromFile(CLOCK_ACTION_BAR_TITLE_FONT);
//        titleText.setTypeface(auroraTitleFace);
        
        //mSetting = new SettingPopupWindow(AlarmClock.this, R.layout.settingpopup);
        //mSetting.setOnButtonSettingClickListener(new OnButtonSettingClickListener() {

		//	@Override
		//	public void onButtonSettingClick(int position) {
				// TODO Auto-generated method stub
		//		Intent settingIntent = new Intent(AlarmClock.this, SettingsActivity.class);
        //        startActivity(settingIntent);
		//	}
			
		//});
        
        startAddImageViewAnim( true);

//		WallpaperManager wallpaperManager = WallpaperManager  
//                .getInstance(this);  
//	    Drawable wallpaperDrawable = wallpaperManager.getDrawable();  
//	    BitmapFactory.Options opts = new BitmapFactory.Options();
//		opts.inPreferredConfig = Bitmap.Config.RGB_565;
//		opts.inPurgeable = true;
//		opts.inInputShareable = true;
//		opts.inSampleSize = 10;
//		BitmapDrawable bd = (BitmapDrawable) wallpaperDrawable;
//		Bitmap  bitmap = bd.getBitmap();		
//	    Matrix matrix = new Matrix(); 
//	    matrix.postScale(0.2f,0.2f); 
	    //mScreenBitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
//	    mScreenBitmap = getLockScreenFromDefaultPath(this);
//	    mScreenBitmapMatrix = Blur.showBgBlurView(this, mainLayout);
	    //getLockScreenPath();
	    //Blur.showBgBlurView(this, mainLayout, lockscreenDefaultPath);
	    Bitmap background = Blur.getBackgroundPic(this, "Default-Wallpaper-bg.png");
	    mainLayout.setBackgroundDrawable(new BitmapDrawable(background));
	    //background.recycle();
    }  
    

    
    
    /**
     * 播放+号进入动画
     */
    private void startAddImageViewAnim(boolean showsettingImage ) {
    	
    	Log.e("--AlarmClock startAddImageViewAnim---");

    	addImageView.clearAnimation();
    	settingImageView.clearAnimation();
    	titleText.clearAnimation();
    	
    	// TODO Auto-generated method stub
    	Animation animation = AnimationUtils.loadAnimation(AlarmClock.this, R.anim.addimageview_enter);
    	addImageView.startAnimation(animation);
    	if(showsettingImage){
    		settingImageView.startAnimation(animation);
    	}
    	
    	animation = AnimationUtils.loadAnimation(AlarmClock.this, R.anim.alarmtitle_enter);
    	titleText.startAnimation(animation);
    }
    
    private void endAddImageViewAnim(boolean showsettingImage ) {
    	
    	Log.e("--AlarmClock endAddImageViewAnim---");

    	addImageView.clearAnimation();
    	settingImageView.clearAnimation();
    	titleText.clearAnimation();
    	
    	Animation animation = AnimationUtils.loadAnimation(AlarmClock.this, R.anim.addimageview_exit);
    	addImageView.startAnimation(animation);
    	if (showsettingImage) {
			
    		settingImageView.startAnimation(animation);
		}
    	
    	animation = AnimationUtils.loadAnimation(AlarmClock.this, R.anim.alarmtitle_exit);
    	titleText.startAnimation(animation);
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
        switch (v.getId()) {  
        case R.id.alarmclock_layout:  
            setTabSelection(0);  
            break;
        case R.id.worldtime_layout:  
            setTabSelection(1);  
            break;  
        case R.id.stopwatch_layout:  
            setTabSelection(2);  
            break;  
        case R.id.timer_layout:  

            setTabSelection(3);  
            break;
        case R.id.addimageview:  

        	if ( tabnum == 0 ) {
				alarmclockFragment.auroraAddNewAlarm();
			} else {
				if ( !worldtimeFragment.isSortOver ) {
					return;
				}
                Intent worldTimeSearchIntent = new Intent(AlarmClock.this, WorldTimeSearchActivity.class);
                worldtimeFragment.startActivityForResult(worldTimeSearchIntent, Constants.TIME_ZONE_SEARCH);
			}  
            break;
            
            case R.id.settingimageview:
            	Intent intent = new Intent(AlarmClock.this, SettingsActivity.class);
            	intent.setPackage(getPackageName());
            	startActivity(intent);
        default:  
            break;  
        }  
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	public static String ctsShowTitle = null;
	
    /** 
     * 根据传入的index参数来设置选中的tab页。 
     *  
     * @param index 
     *            每个tab页对应的下标。0表示消息，1表示联系人，2表示动态，3表示设置。 
     */  
    public void setTabSelection(int index) {  
    	
    	if( isEndAnimRun ) {
    		return;
    	}
    	
        Log.e("--setTabSelection = ----" + index);
        // 每次选中之前先清楚掉上次的选中状态
        clearSelection();
 
        if(lastTabnum!=index
        	 && lastTabnum==3
        	 && timerFragment!=null
        	 && timerFragment.isViewReady()&&timerFragment.isAdded()) {
        	timerFragment.endAnimation();
        	isEndAnimRun = true;
        	//aurora add liguangyu 20140410 for #4019 start
        	mRestoreHandler.sendEmptyMessageDelayed(RESTORE_MESSAGE, RESTORE_MESSAGE_DELAY);
          	//aurora add liguangyu 20140410 for #4019 end
        } else if(lastTabnum!=index
           	 && lastTabnum==2
           	 && stopwatchFragment!=null
           	 && stopwatchFragment.isAdded()&&stopwatchFragment.endAnimation()){ 
        	isEndAnimRun = true;
        	//aurora add liguangyu 20140410 for #4019 start
        	mRestoreHandler.sendEmptyMessageDelayed(RESTORE_MESSAGE, RESTORE_MESSAGE_DELAY);
          	//aurora add liguangyu 20140410 for #4019 end
        } else if(lastTabnum!=index
        		&& lastTabnum==1
        		&& worldtimeFragment!=null
        		&& worldtimeFragment.isAdded()&&worldtimeFragment.endAnalogClockAnim()){
        	worldtimeFragment.worldtimeAuroraListviewOnPause();
        	endAddImageViewAnim(false);
        	isEndAnimRun = true;
        	//aurora add liguangyu 20140410 for #4019 start
        	mRestoreHandler.sendEmptyMessageDelayed(RESTORE_MESSAGE, RESTORE_MESSAGE_DELAY);
          	//aurora add liguangyu 20140410 for #4019 end
        } else if(lastTabnum!=index
        		&& lastTabnum==0
        		&& alarmclockFragment!=null
        		&& alarmclockFragment.isAdded()&&alarmclockFragment.endAlarmClockFragmentAnim()){  
        	alarmclockFragment.alarmClockAuroraListviewOnPause();
        	endAddImageViewAnim(true);
        	isEndAnimRun = true;
        	//aurora add liguangyu 20140410 for #4019 start
        	mRestoreHandler.sendEmptyMessageDelayed(RESTORE_MESSAGE, RESTORE_MESSAGE_DELAY);
          	//aurora add liguangyu 20140410 for #4019 end
        } else{
        	setTabSelectionDelay(index);
        }
        lastTabnum = index;
    }  
        
     //aurora add liguangyu 20140410 for #3990  start
     public void setTabSelectionNoAnimation(int index) {  
    	
    	if( isEndAnimRun ) {
    		return;
    	}
    	
        Log.e("--setTabSelectionNoAnimation = ----" + index);
        clearSelection();
        setTabSelectionDelay(index);
        lastTabnum = index;
     }  
    //aurora add liguangyu 20140410 for #3990  end
    
        private void setTabSelectionDelay(int index) {
            Log.e("-AlarmClock-setTabSelectionDelay = ----" + index);
        	boolean isDiff = false;
        	if(tabnum != index) {
        		isDiff = true;
            } 
        	tabnum = index;
            // 开启一个Fragment事务  
            FragmentTransaction transaction = fragmentManager.beginTransaction();  
            // 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况  
            hideFragments(transaction);  
            switch (index) {  
            case 0:  
            	 // 当点击了动态tab时，改变控件的图片和文字颜色  
            	addImageViewContainer.setVisibility(View.VISIBLE);
            	alarmclockLayout.setImageResource(R.drawable.tab_alarm_click);
            	titleText.setText(R.string.alarm_list_title);
                if (alarmclockFragment == null) {  
                    // 如果alarmclockFragment为空，则创建一个并添加到界面上  
                	alarmclockFragment = new AlarmClockFragment();  
                    transaction.add(R.id.content, alarmclockFragment, "" + index);  
                } else {  
                    // 如果MessageFragment不为空，则直接将它显示出来  
                    transaction.show(alarmclockFragment); 
                    if ( isDiff ) {
                    	alarmclockFragment.startAlarmClockFragmentAnim2();
                    	alarmclockFragment.alarmClockAuroraListviewOnResume();
                    }
                }  
                if ( isDiff ) {
                	startAddImageViewAnim(true );
                }
                break;  
            case 1:  
                // 当点击了动态tab时，改变控件的图片和文字颜色  
            	worldtimeLayout.setImageResource(R.drawable.tab_worldtime_click);
            	addImageViewContainer.setVisibility(View.VISIBLE);
            	settingImageView.setVisibility(View.GONE);
            	titleText.setText(R.string.world_time);
                if (worldtimeFragment == null) {  
                    // 如果worldtimeFragment为空，则创建一个并添加到界面上  
                	worldtimeFragment = new WorldTimeFragment();  
                    transaction.add(R.id.content, worldtimeFragment, "" + index);  
                } else {  
                    // 如果worldtimeFragment不为空，则直接将它显示出来  
                    transaction.show(worldtimeFragment);  
                    if ( isDiff ) {
                    	worldtimeFragment.startAnalogClockAnim();
                    	worldtimeFragment.worldtimeAuroraListviewOnResume();
                    }
                }  
                if ( isDiff ) {
                	new Handler().postDelayed(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							startAddImageViewAnim( false);
						}
					}, 50);
                	
                }
                break;  
            case 2:  
                // 当点击了联系人tab时，改变控件的图片和文字颜色  
            	addImageViewContainer.setVisibility(View.GONE);
            	stopwatchLayout.setImageResource(R.drawable.tab_stopwatch_click);  
                if (stopwatchFragment == null) {  
                    // 如果stopwatchFragment为空，则创建一个并添加到界面上  
                	stopwatchFragment = new StopWatchFragment();  
                    transaction.add(R.id.content, stopwatchFragment, "" + index);  
                } else {  
                    // 如果stopwatchFragment不为空，则直接将它显示出来  
                    transaction.show(stopwatchFragment);  
                }  
                if(isDiff) {
                	stopwatchFragment.restoreAnimation();
                	stopwatchFragment.initAnimation();
                }
                break;  
            case 3:  
            default:  
                // 当点击了设置tab时，改变控件的图片和文字颜色  
            	addImageViewContainer.setVisibility(View.GONE);
            	timerLayout.setImageResource(R.drawable.tab_timer_click);
                if (timerFragment == null) {  
                    // 如果timerFragment为空，则创建一个并添加到界面上  
                	timerFragment = new TimerFragment();  
                    transaction.add(R.id.content, timerFragment, "" + index);  
                } else {  
                    // 如果SettingFragment不为空，则直接将它显示出来  
                    transaction.show(timerFragment);  
                }  
                if(isDiff) {
                	timerFragment.timePickerEnterAnimation();
                }
                break;  
            }  
            transaction.commit();  
        }  
        
        
        private void setTabSelection3() {
            Log.e("--setTabSelection3 = ----");
            clearSelection();
            lastTabnum = 3;
        	tabnum = 3;
            // 开启一个Fragment事务  
            FragmentTransaction transaction = fragmentManager.beginTransaction();  
            // 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况  
            hideFragments(transaction);          
            // 当点击了设置tab时，改变控件的图片和文字颜色  
        	addImageViewContainer.setVisibility(View.GONE);
        	timerLayout.setImageResource(R.drawable.tab_timer_click);
            if (timerFragment == null) {  
                // 如果timerFragment为空，则创建一个并添加到界面上  
            	timerFragment = new TimerFragment();  
                transaction.add(R.id.content, timerFragment, "" + 3);  
            } else {  
                // 如果SettingFragment不为空，则直接将它显示出来  
                transaction.show(timerFragment);  
            }                
            transaction.commit();  
        }  
 
    /** 
     * 清除掉所有的选中状态。 
     */  
    private void clearSelection() {  
    	alarmclockLayout.setImageResource(R.drawable.tab_alarm_normal);
        stopwatchLayout.setImageResource(R.drawable.tab_stopwatch_normal);  
        timerLayout.setImageResource(R.drawable.tab_timer_normal);  
        worldtimeLayout.setImageResource(R.drawable.tab_worldtime_normal);  
    }  
  
    /** 
     * 将所有的Fragment都置为隐藏状态。 
     *  
     * @param transaction 
     *            用于对Fragment执行操作的事务 
     */  
    private void hideFragments(FragmentTransaction transaction) {  
        if (alarmclockFragment != null) {  
            transaction.hide(alarmclockFragment);  
        }  
        if (worldtimeFragment != null) {  
            transaction.hide(worldtimeFragment);  
        }  
        if (stopwatchFragment != null) {  
            transaction.hide(stopwatchFragment);  
        }  
        if (timerFragment != null) {  
            transaction.hide(timerFragment);  
        }  
    }  
    
    //暂时弃用AuroraClockFragment，启用ClockFragment，所以这个先不用了 mod by tangjun 2014.1.17
    /*
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
    	Log.e("---AlarmClock onActivityResult---");
    	if ( requestCode == AuroraAlarmClockFragment.REQUEST_CODE ) {
    		if ( resultCode == RESULT_OK) {
    			int position = data.getIntExtra("position", -1);
    			Log.e("---AlarmClock onActivityResult--position = --" + position);
    			if ( position > -1 ) {
    				alarmclockFragment.auroraDeleteAlarm(position);
    			}
    		}
    	}
		super.onActivityResult(requestCode, resultCode, data);
	}
	*/

	@Override
	public void onBackPressed() {
		// super.onBackPressed();
		if ( tabnum == 0 && alarmclockFragment.getAlarmClockListView().auroraIsRubbishOut() ) {
			alarmclockFragment.getAlarmClockListView().auroraSetRubbishBack();
		} else if ( tabnum == 1 && worldtimeFragment.getWorldTimeListView().auroraIsRubbishOut() ){
			worldtimeFragment.getWorldTimeListView().auroraSetRubbishBack();
		} else {
			this.moveTaskToBack(true);
		}
	}
	
	@Override
	protected void onDestroy() {
		Log.e("-AlarmClock---onDestroy()------");	
		
		unregisterReceiverForFontChange();
		
		// TODO Auto-generated method stub
		NotificationOperate.cancelNotification(this, TimerFragment.TABNUM);
		NotificationOperate.cancelNotification(this, StopWatchFragment.TABNUM);
    	//aurora add liguangyu 20140410 for #4019 start
    	mRestoreHandler.removeMessages(RESTORE_MESSAGE);
      	//aurora add liguangyu 20140410 for #4019 end
		super.onDestroy();
		
		/*
		FragmentTransaction transaction = fragmentManager.beginTransaction();  
    	if(alarmclockFragment != null) {
    		Log.e("AlarmClock---onDestroy() remove alarmclockFragment");
    		transaction.remove(alarmclockFragment);
    		alarmclockFragment = null;
        }
    	if(worldtimeFragment != null) {
    		transaction.remove(worldtimeFragment);
    		worldtimeFragment = null;
        }
    	if(stopwatchFragment != null) {
    		transaction.remove(stopwatchFragment);
    		stopwatchFragment = null;
        }
    	if(timerFragment != null) {
    		transaction.remove(timerFragment);
    		timerFragment = null;
        }
        transaction.commit(); 
        */
	}
	
	
    public void onTimerFragmentAnimationComplete(){
    	//aurora add liguangyu 20140410 for #4019 start
    	mRestoreHandler.removeMessages(RESTORE_MESSAGE);
      	//aurora add liguangyu 20140410 for #4019 end
    	isEndAnimRun = false;
		setTabSelectionDelay(lastTabnum); 
    }
    
    public void onStopWatchFragmentAnimationComplete(){
    	//aurora add liguangyu 20140410 for #4019 start
    	mRestoreHandler.removeMessages(RESTORE_MESSAGE);
      	//aurora add liguangyu 20140410 for #4019 end
    	isEndAnimRun = false;
		setTabSelectionDelay(lastTabnum); 
    }
    
    public void updateKeyguardPolicy(boolean dismissKeyguard) {
        if (dismissKeyguard) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }
    }


	@Override
	public void onWorldTimeFragmentAnimationComplete() {
		// TODO Auto-generated method stub
    	//aurora add liguangyu 20140410 for #4019 start
    	mRestoreHandler.removeMessages(RESTORE_MESSAGE);
      	//aurora add liguangyu 20140410 for #4019 end
		isEndAnimRun = false;
		
		if ( isOnPause ) {
			return;
		}
		
		setTabSelectionDelay(lastTabnum); 
	}

	@Override
	public void onAlarmClockFragmentAnimationComplete() {
		// TODO Auto-generated method stub
    	//aurora add liguangyu 20140410 for #4019 start
    	mRestoreHandler.removeMessages(RESTORE_MESSAGE);
      	//aurora add liguangyu 20140410 for #4019 end
		isEndAnimRun = false;
		
		if ( isOnPause ) {
			return;
		}
		
		setTabSelectionDelay(lastTabnum); 
	}

	@Override
	protected void onResume() {		 
		super.onResume();
		//refreshBgIfNeeded();	
		
		isOnPause = false;
	}
	 
	 @Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.e("---AlarmClock--onPause---");
		
		isOnPause = true;
		setTabSelectionDelay(lastTabnum); 
	}
	 
	 @Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		Log.e("---AlarmClock--onSaveInstanceState---");
	}
	 
	//aurora add liguangyu 20140410 for #4019 start
	 private static final int RESTORE_MESSAGE = 0;
	 private static final int RESTORE_MESSAGE_DELAY = 2000;
	 private Handler mRestoreHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				isEndAnimRun = false;
				lastTabnum = tabnum;
	            switch (tabnum) {  
		            case 0:  
		            	alarmclockLayout.setImageResource(R.drawable.tab_alarm_click);
		                break;  
		            case 1:  
		               	worldtimeLayout.setImageResource(R.drawable.tab_worldtime_click);
		                break;  
		            case 2:  
		            	stopwatchLayout.setImageResource(R.drawable.tab_stopwatch_click);  
		                break;  
		            case 3:  
		            default:  
		               	timerLayout.setImageResource(R.drawable.tab_timer_click);
	            }  
			}
	 };
	//aurora add liguangyu 20140410 for #4019 end
	 
		/*
		 * register receiver
		 */
		private void registerReceiverForFontChange() {
			IntentFilter filter = new IntentFilter();
			filter.addAction("com.android.settings.ACTION_UPDATE_FONT_BOLD");

			this.registerReceiver(mIntentFontReceiver, filter);
		}

		private void unregisterReceiverForFontChange() {
			this.unregisterReceiver(mIntentFontReceiver);
		}
		
		/*
		 * receiver broadcast , refresh UI 监听设置里字体变粗的广播
		 */
		private final BroadcastReceiver mIntentFontReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				finish();
			}
		};
}