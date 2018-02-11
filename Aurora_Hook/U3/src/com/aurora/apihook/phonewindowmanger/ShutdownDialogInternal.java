package com.aurora.apihook.phonewindowmanger;

import java.lang.reflect.Method;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Notification.Builder;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.View.OnLongClickListener;
import android.view.WindowManagerPolicy.WindowManagerFuncs;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.internal.policy.impl.EnableAccessibilityController;
import com.android.internal.policy.impl.PhoneWindowManager;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;

import android.view.WindowManagerPolicy.WindowManagerFuncs;
import com.aurora.apihook.phonewindowmanger.FrameAnimation;
import com.aurora.apihook.phonewindowmanger.FrameAnimation.AnimationImageListener;
import com.aurora.apihook.phonewindowmanger.FrameAnimation.FrameCallback;
import com.android.server.power.ShutdownThread;
public  class ShutdownDialogInternal extends Dialog implements DialogInterface,
android.view.View.OnClickListener,OnLongClickListener, AnimationListener,AnimationImageListener{

	/**
	 * flag for show airmode item 
	 */
	private static final boolean SHOW_AIRMODE_PANLE = false;
	
	private final Context mContext;
	private int mWindowTouchSlop;
	// private GridView grid;

	private EnableAccessibilityController mEnableAccessibilityController;

	private boolean mIntercepted;
	private boolean mCancelOnUp;
    private WindowManagerFuncs mWindowManagerFuncs;
	private ImageButton shutdown;
	private ImageButton reboot;
	private ImageButton airmode;

	private View layoutAirMode;
	private View layoutReboot;
	private View layoutShutdown;

	private View mNavView;

	private TextView textAirMode;
	private TextView textReboot;
	private TextView textShutdown;

	private AlertDialog mRebootConfirmAlert;

	/**
	 * 监听虚拟键状态的改变 通过 ContentObserver#onChange()获得 NAVI_KEY_HIDE 改变的情况
	 */
	private SettingsObserver mSettingsObserver;

	private int airmodeOpen;
	private int airmodeClose;

	private int airmodeOpenImg;
	private int airmodeCloseImg;

	private int mKeyBack = 0;

	private Animation airModeAnim;
	private Animation rebootAnim;
	private Animation shutdownAnim;

	private Animation mDismissAirMode;
	private Animation mDismissReboot;
	private Animation mDismissAnim;
	private Animation mAirmodeTextAnim;
	private Animation mShutdownTextAnim;
	private Animation mRebootTextAnim;
	
	private Animation mAirmodeTextDismissAnim;
	private Animation mShutdownTextDismissAnim;
	private Animation mRebootTextDismissAnim;

	private FrameAnimation airModeIconAnim;
	private FrameAnimation rebootIconAnim;
	private FrameAnimation shutdownIconAnim;
	private final int MSG_PLAY_REBOOT_ANIM = 100;
	private final int MSG_PLAY_SHUTDOWN_ANIM = 101;
	private final int MSG_PLAY_REBOOT_DISMISS = 102;
	private final int MSG_PLAY_SHUTDOWN_DISMISS = 103;
	private final int MSG_PLAY_ICON_ANIM = 104;
	private final int MSG_PLAY_TEXT_ANIM = 105;

	private final int NAV_MODE_CANCLE = -1;
	private final int NAV_MODE_SET_COLOR = 6;

	private final int mSlideToShowNavArea = 100;
	private final int mSlideDistance = 10;
	private final String NAV_COLOR = "#D9000000";

	private final String NAV_ACTION = "aurora.action.SET_NAVIBAR_COLOR";
	private static final String NAVI_KEY_HIDE = "navigation_key_hide";
	private int mScreenHeight;
	private int mTouchedY;
	private Handler mNavigationKeyHandler = new Handler();

	private Handler mAnimHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_PLAY_REBOOT_ANIM:
				reboot.startAnimation(rebootAnim);
				layoutReboot.setVisibility(View.VISIBLE);
				
				break;
			case MSG_PLAY_SHUTDOWN_ANIM:
				shutdown.startAnimation(shutdownAnim);
				layoutShutdown.setVisibility(View.VISIBLE);
				break;
			case MSG_PLAY_REBOOT_DISMISS:
				reboot.setAnimation(mDismissReboot);
				textReboot.startAnimation(mRebootTextDismissAnim);
                break;
            case MSG_PLAY_SHUTDOWN_DISMISS:
                shutdown.startAnimation(mDismissAnim);
                textShutdown.startAnimation(mShutdownTextDismissAnim);
                break;
			case MSG_PLAY_ICON_ANIM:
//				airModeIconAnim.setAnimationImageListener(this);
//				rebootIconAnim.setAnimationImageListener(this);
//				shutdownIconAnim.setAnimationImageListener(this);
//				
//				airModeIconAnim.setView(airmode);
//				rebootIconAnim.setView(reboot);
//				shutdownIconAnim.setView(shutdown);
				
				//airModeIconAnim.start();
				rebootIconAnim.start();
				shutdownIconAnim.start();
				break;

			case MSG_PLAY_TEXT_ANIM:
				//textAirMode.startAnimation(mAirmodeTextAnim);
				textReboot.startAnimation(mRebootTextAnim);
				textShutdown.startAnimation(mShutdownTextAnim);
			break;
			default:
				break;
			}

		};

	};
	private boolean isAirMode = false;

	private LayoutInflater mInflater;
	private View mContentView;
	private String mSoundPath = "/system/media/audio/ui/PowerMenu.ogg";

	long startTime;
	long endTime;
	 private boolean mHasTelephony;
	 private boolean mAirplaneState;
	 private boolean mIsWaitingForEcmExit = false;
	 PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
	        @Override
	        public void onServiceStateChanged(ServiceState serviceState) {
	            if (!mHasTelephony)
	                return;
	            final boolean inAirplaneMode = serviceState.getState() == ServiceState.STATE_POWER_OFF;
	            mAirplaneState  = inAirplaneMode;
	        }
	    };
	    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
	        public void onReceive(Context context, Intent intent) {
	            String action = intent.getAction();
	            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)
	                    || Intent.ACTION_SCREEN_OFF.equals(action)) {
	                String reason = intent.getStringExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY);
	                if (!PhoneWindowManager.SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS.equals(reason)) {
	                    dismiss();
	                }
	            } else if (TelephonyIntents.ACTION_EMERGENCY_CALLBACK_MODE_CHANGED.equals(action)) {
	                // Airplane mode can be changed after ECM exits if airplane
	                // toggle button
	                // is pressed during ECM mode
	                if (!(intent.getBooleanExtra("PHONE_IN_ECM_STATE", false)) &&
	                        mIsWaitingForEcmExit) {
	                    mIsWaitingForEcmExit = false;
	                    changeAirplaneModeSystemSetting(true);
	                }
	            }
	        }
	    };
	
	public ShutdownDialogInternal(Context context,WindowManagerFuncs wmf) {
		super(context, com.aurora.R.style.Aurora_Dialog_Fullscreen);
		// TODO Auto-generated constructor stub
		mWindowManagerFuncs = wmf;
		TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE);
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // receive broadcasts
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(TelephonyIntents.ACTION_EMERGENCY_CALLBACK_MODE_CHANGED);
        context.registerReceiver(mBroadcastReceiver, filter);
        
        mHasTelephony = cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE);
		mContext = context;
		mInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		 startTime = System.currentTimeMillis();
		mContentView = mInflater.inflate(
				com.aurora.R.layout.aurora_power_layout, null);
		mContentView
		.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
		mWindowTouchSlop = ViewConfiguration.get(mContext)
				.getScaledWindowTouchSlop();
		shutdown = (ImageButton) mContentView
				.findViewById(com.aurora.R.id.aurora_shutdown_btn);
		reboot = (ImageButton) mContentView
				.findViewById(com.aurora.R.id.aurora_reboot_btn);
		if(SHOW_AIRMODE_PANLE){
		airmode = (ImageButton) mContentView
				.findViewById(com.aurora.R.id.aurora_airmode_btn);

		airmodeClose = com.aurora.R.string.aurora_airmode;
		airmodeOpen = com.aurora.R.string.aurora_airmode_open;
		airmodeOpenImg = com.aurora.R.drawable.aurora_airmode_normal;
		airmodeCloseImg = com.aurora.R.drawable.aurora_airmode_pressed;

		layoutAirMode = mContentView
				.findViewById(com.aurora.R.id.air_mode_layout);
		textAirMode = (TextView) mContentView
				.findViewById(com.aurora.R.id.aurora_airmode_text);
		}
		
		
		layoutReboot = mContentView
				.findViewById(com.aurora.R.id.reboot_layout);
		layoutShutdown = mContentView
				.findViewById(com.aurora.R.id.shutdown_layout);

		
		textReboot = (TextView) mContentView
				.findViewById(com.aurora.R.id.aurora_reboot_text);
		textShutdown = (TextView) mContentView
				.findViewById(com.aurora.R.id.aurora_shutdown_text);

//		mNavView = mContentView
//				.findViewById(com.aurora.R.id.aurora_nav_buttons);
		initAnim();

	}

	
	private void option(Context context,boolean confirm,String name){
		try{
		Class<?> ShutdownThreadClz = Class.forName("com.android.server.power.ShutdownThread");
		if(ShutdownThreadClz != null){
			Method shutdownMethod = ShutdownThreadClz.getDeclaredMethod(name, 
					Context.class,boolean.class);
			if(shutdownMethod != null){
				shutdownMethod.setAccessible(true);
				shutdownMethod.invoke(null, context,confirm);
			}
		}
		}catch(Exception e){
			
		}
	}
	private void option(Context context,String reson,boolean confirm,String name){
		try{
		Class<?> ShutdownThreadClz = Class.forName("com.android.server.power.ShutdownThread");
		if(ShutdownThreadClz != null){
			Method shutdownMethod = ShutdownThreadClz.getDeclaredMethod(name, 
					Context.class,String.class,boolean.class);
			if(shutdownMethod != null){
				shutdownMethod.setAccessible(true);
				shutdownMethod.invoke(null, context,reson,confirm);
			}
		}
		}catch(Exception e){
			
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// getWindow().getAttributes()
		setContentView(mContentView);
		
		initRebootConfirmAlert();
		if(SHOW_AIRMODE_PANLE){
		int isAirplaneMode = Settings.System.getInt(
				mContext.getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, 0);
		boolean find = (isAirplaneMode == 1) ? true : false;
		isAirMode = find;

		String airModeText = mContext.getResources().getString(isAirMode ? airmodeClose : airmodeOpen);
		textAirMode.setText(airModeText);
		airmode.setOnClickListener(this);
		}
		String rebootText = mContext.getResources().getString(com.aurora.R.string.aurora_reboot);
		textReboot.setText(rebootText);
		String shutdownText = mContext.getResources().getString(com.aurora.R.string.aurora_shutdown);
		textShutdown.setText(shutdownText);
		shutdown.setOnClickListener(this);
		reboot.setOnClickListener(this);
		
		shutdown.setOnLongClickListener(this);

	}

	/**
	 * 通过改写 NAVI_KEY_HIDE 的值，可以控制虚拟键的显示/隐藏。 hide = true, 写入1，代表隐藏虚拟键 hide =
	 * false, 写入0，代表显示虚拟键
	 */
	private void hideNaviBar(boolean hide) {
		ContentValues values = new ContentValues();
		values.put("name", NAVI_KEY_HIDE);
		values.put("value", (hide ? 1 : 0));
		ContentResolver cr = mContext.getContentResolver();
		cr.insert(Settings.System.CONTENT_URI, values);
	}

	private void setNavBarColor(int mode, boolean putColor) {
		Intent intent = new Intent(NAV_ACTION);
		intent.putExtra("mode", mode);
		if (putColor) {
			intent.putExtra("color", NAV_COLOR);
		}
		mContext.sendBroadcast(intent);
	}


	public void setFrameAnimation(FrameAnimation mAirmodeAnim,FrameAnimation mRebootAnim,FrameAnimation mShutDownAnim){
				if(SHOW_AIRMODE_PANLE){
		airModeIconAnim = mAirmodeAnim;
}
		rebootIconAnim = mRebootAnim;
		shutdownIconAnim = mShutDownAnim;
		if(SHOW_AIRMODE_PANLE){
		if(airModeIconAnim == null){
			airModeIconAnim = new FrameAnimation(mContext,
					FrameAnimation.ICON_AIRMODE_DOWN);
}
		}
		if(rebootIconAnim == null){
			rebootIconAnim = new FrameAnimation(mContext,
					FrameAnimation.ICON_REBOOT_DOWN);
		}
		if(shutdownIconAnim == null){
			shutdownIconAnim = new FrameAnimation(mContext,
					FrameAnimation.ICON_SHUT_DOWN);
		}
		if(SHOW_AIRMODE_PANLE){
		airModeIconAnim.setAnimationImageListener(this);
		}
		rebootIconAnim.setAnimationImageListener(this);
		shutdownIconAnim.setAnimationImageListener(this);
		if(SHOW_AIRMODE_PANLE){
		airModeIconAnim.setView(airmode);
		}
		rebootIconAnim.setView(reboot);
		shutdownIconAnim.setView(shutdown);
	}
	
	private void initAnim() {
		
//		airModeIconAnim = new FrameAnimation(mContext,
//				FrameAnimation.ICON_AIRMODE_DOWN);
//		rebootIconAnim = new FrameAnimation(mContext,
//				FrameAnimation.ICON_REBOOT_DOWN);
//		shutdownIconAnim = new FrameAnimation(mContext,
//				FrameAnimation.ICON_SHUT_DOWN);
		
//		airModeIconAnim.setView(airmode);
//		rebootIconAnim.setView(reboot);
//		shutdownIconAnim.setView(shutdown);
		
		rebootAnim = AnimationUtils.loadAnimation(mContext,
				com.aurora.R.anim.aurora_airmode_anim);
		if(SHOW_AIRMODE_PANLE){
		airModeAnim = AnimationUtils.loadAnimation(mContext,
				com.aurora.R.anim.aurora_airmode_anim);
		}
		shutdownAnim = AnimationUtils.loadAnimation(mContext,
				com.aurora.R.anim.aurora_airmode_anim);
		mDismissAirMode = AnimationUtils.loadAnimation(mContext,
				com.aurora.R.anim.aurora_shutdown_dialog_push_up_out);
		mDismissAnim = AnimationUtils.loadAnimation(mContext,
				com.aurora.R.anim.aurora_shutdown_dialog_push_up_out);
		mDismissReboot = AnimationUtils.loadAnimation(mContext,
				com.aurora.R.anim.aurora_shutdown_dialog_push_up_out);

		if(SHOW_AIRMODE_PANLE){
		mAirmodeTextDismissAnim = AnimationUtils.loadAnimation(mContext,
				com.aurora.R.anim.aurora_shutdown_dialog_push_up_out);
		}
		mShutdownTextDismissAnim = AnimationUtils.loadAnimation(mContext,
				com.aurora.R.anim.aurora_shutdown_dialog_push_up_out);
		mRebootTextDismissAnim = AnimationUtils.loadAnimation(mContext,
				com.aurora.R.anim.aurora_shutdown_dialog_push_up_out);
		
		if(SHOW_AIRMODE_PANLE){
		mAirmodeTextAnim = AnimationUtils.loadAnimation(mContext,
				com.aurora.R.anim.aurora_shutdown_text_anim);
		}

		mShutdownTextAnim=AnimationUtils.loadAnimation(mContext,
				com.aurora.R.anim.aurora_shutdown_text_anim);
		mRebootTextAnim=AnimationUtils.loadAnimation(mContext,
				com.aurora.R.anim.aurora_shutdown_text_anim);
		if(SHOW_AIRMODE_PANLE){
		mAirmodeTextDismissAnim.setAnimationListener(this);
		}
		mShutdownTextDismissAnim.setAnimationListener(this);
		mRebootTextDismissAnim.setAnimationListener(this);
		if(SHOW_AIRMODE_PANLE){
		mAirmodeTextAnim.setAnimationListener(this);
		}
		mShutdownTextAnim.setAnimationListener(this);
		mRebootTextAnim.setAnimationListener(this);
		if(SHOW_AIRMODE_PANLE){
		mDismissAirMode.setAnimationListener(this);
		}
		mDismissAnim.setAnimationListener(this);
//		airModeIconAnim.setAnimationImageListener(this);
//		rebootIconAnim.setAnimationImageListener(this);
//		shutdownIconAnim.setAnimationImageListener(this);
		rebootAnim.setAnimationListener(this);
		if(SHOW_AIRMODE_PANLE){
		airModeAnim.setAnimationListener(this);
		}
		mDismissReboot.setAnimationListener(this);
	}

	private void playAnimation() {
		if(SHOW_AIRMODE_PANLE){
			airmode.startAnimation(airModeAnim);
			layoutAirMode.setVisibility(View.VISIBLE);
		}else{
			reboot.startAnimation(rebootAnim);
			layoutReboot.setVisibility(View.VISIBLE);
		}
		mAnimHandler.sendEmptyMessageDelayed(MSG_PLAY_TEXT_ANIM, 300);
	}

	private void initRebootConfirmAlert() {
		AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
		alert.setTitle(com.aurora.R.string.reboot_alert_title);
		alert.setMessage(com.aurora.R.string.reboot_alert_message);
		alert.setPositiveButton(com.aurora.R.string.reboot_ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Intent i = new Intent(Intent.ACTION_REBOOT);
						i.putExtra("nowait", 1);
						i.putExtra("interval", 1);
						i.putExtra("window", 0);
						mContext.sendBroadcast(i);
//						Log.e("policy", "reboot");
//						option(mContext,null,true,"reboot");
//						dialog.dismiss();
//						dismissDialog();
					}
				});
		alert.setNegativeButton(com.aurora.R.string.reboot_cancel,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();

					}
				});

		mRebootConfirmAlert = alert.create();

		mRebootConfirmAlert.getWindow().setType(
				WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
	}

	private void dismissDialog() {
		playDismissAnim();
		
	}

	@Override
	protected void onStart() {
		// If global accessibility gesture can be performed, we will take
		// care
		// of dismissing the dialog on touch outside. This is because the
		// dialog
		// is dismissed on the first down while the global gesture is a long
		// press
		// with two fingers anywhere on the screen.
		if (EnableAccessibilityController
				.canEnableAccessibilityViaGesture(mContext)) {
			mEnableAccessibilityController = new EnableAccessibilityController(
					mContext);
			super.setCanceledOnTouchOutside(false);
		} else {
			mEnableAccessibilityController = null;
			super.setCanceledOnTouchOutside(true);
		}
		super.onStart();
	}

	@Override
	protected void onStop() {
		if (mEnableAccessibilityController != null) {
			mEnableAccessibilityController.onDestroy();
		}
		setNavBarColor(NAV_MODE_CANCLE, false);
		super.onStop();
	}

	private boolean isKeyGuardShowing() {
		KeyguardManager mKeyguardManager = (KeyguardManager) mContext
				.getSystemService(Context.KEYGUARD_SERVICE);
		return mKeyguardManager.inKeyguardRestrictedInputMode();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (mEnableAccessibilityController != null) {
			final int action = event.getActionMasked();
			if (action == MotionEvent.ACTION_DOWN) {
				View decor = getWindow().getDecorView();
				final int eventX = (int) event.getX();
				final int eventY = (int) event.getY();
				if (eventX < -mWindowTouchSlop
						|| eventY < -mWindowTouchSlop
						|| eventX >= decor.getWidth() + mWindowTouchSlop
						|| eventY >= decor.getHeight() + mWindowTouchSlop) {
					mCancelOnUp = true;
				}
			}
			try {
				if (!mIntercepted) {
					mIntercepted = mEnableAccessibilityController
							.onInterceptTouchEvent(event);
					if (mIntercepted) {
						final long now = SystemClock.uptimeMillis();
						event = MotionEvent.obtain(now, now,
								MotionEvent.ACTION_CANCEL, 0.0f, 0.0f, 0);
						event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
						mCancelOnUp = true;
					}
				} else {
					return mEnableAccessibilityController
							.onTouchEvent(event);
				}
			} finally {
				if (action == MotionEvent.ACTION_UP) {
					if (mCancelOnUp) {
						cancel();
					}
					mCancelOnUp = false;
					mIntercepted = false;
				}
			}
		}
		return super.dispatchTouchEvent(event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mKeyBack == 0) {
				dismissDialog();
				mKeyBack = 1;
			}

			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		if (hasFocus) {
			setStatusBarBG(true);
		}
		super.onWindowFocusChanged(hasFocus);

	}

	public void setStatusBarBG(boolean isTransparent) {
		Intent StatusBarBGIntent = new Intent();
		StatusBarBGIntent
				.setAction("aurora.action.SET_STATUSBAR_TRANSPARENT");
		StatusBarBGIntent.putExtra("transparent", isTransparent);
		mContext.sendBroadcast(StatusBarBGIntent);
	}

	private void setStatusBarTransparent(boolean enable) {
		Log.i("aaa", "setStatusBarTransparent:" + enable);
		NotificationManager notificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder builder = new Builder(mContext);
		builder.setSmallIcon(com.aurora.R.drawable.stat_sys_secure);
		String tag = "auroraSBNT653";
		if (enable) {
			tag = "auroraSBT8345";
		} else {
			tag = "auroraSBNT653";
		}
		notificationManager.notify(tag, 0, builder.build());
	}

	public void playSounds(int sound, int number) {
		MediaPlayer mediaPlayer = new MediaPlayer();
//		mediaPlayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				if (mp != null) {
					mp.stop();
					mp.release();
				}
			}
		});
		try {
			mediaPlayer.setDataSource(mSoundPath);
			mediaPlayer.prepare();
			mediaPlayer.setLooping(false);
			mediaPlayer.start();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	private void playDismissAnim() {
		if(SHOW_AIRMODE_PANLE){
		 airmode.startAnimation(mDismissAirMode);
		
		 textAirMode.startAnimation(mAirmodeTextDismissAnim);
		}else{
			 reboot.startAnimation(mDismissReboot);
				
			 textReboot.startAnimation(mRebootTextDismissAnim);
		}
//         layoutAirMode.setVisibility(View.INVISIBLE);
         mAnimHandler.sendEmptyMessageDelayed(MSG_PLAY_SHUTDOWN_DISMISS,20);
		mIsShowing = false;
	}
	private boolean mIsShowing;
	
	public boolean getIsShow(){
		
		return mIsShowing;
	}

	@Override
	public void show() {
		super.show();
		mIsShowing = true;
		mKeyBack = 0;
		endTime =System.currentTimeMillis();
		playSounds(1, 1);
		playAnimation();
		/*
		 * if(mNavView != null){ if(isKeyGuardShowing()){
		 * mNavView.setVisibility(View.VISIBLE); } }else{
		 * mNavView.setVisibility(View.GONE); }
		 */
		mContext.sendBroadcast(new Intent(
				"com.aurora.action.SHUTDOWN_DIALOG_SHOW"));
		setNavBarColor(NAV_MODE_SET_COLOR, true);
	}

	
	
	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case com.aurora.R.id.aurora_shutdown_btn:
			if(mWindowManagerFuncs != null){
				mWindowManagerFuncs.shutdown(true);
			}else{
				Log.e("policy", "shutdown");
				option(mContext,true,"shutdown");
//				ShutdownThread.shutdown(mContext,true);
			}
			dismissDialog();
			break;
		case com.aurora.R.id.aurora_reboot_btn:
			mRebootConfirmAlert.show();
//			option(mContext,null,true,"reboot");
			dismissDialog();
			break;
		case com.aurora.R.id.aurora_airmode_btn:
			if(!SHOW_AIRMODE_PANLE){
				break;
			}

			if(mIsShowing == false){
				return;
			}
			isAirMode = !isAirMode;
			if (mHasTelephony
					&& Boolean.parseBoolean(SystemProperties
							.get(TelephonyProperties.PROPERTY_INECM_MODE))) {
				mIsWaitingForEcmExit = true;
				// Launch ECM exit dialog
				Intent ecmDialogIntent = new Intent(
						TelephonyIntents.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS,
						null);
				ecmDialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(ecmDialogIntent);
			} else {
				changeAirplaneModeSystemSetting(isAirMode);
			}

			// In ECM mode airplane state cannot be changed
			mAirplaneState = isAirMode;
			String airmodeText = mContext.getResources().getString(isAirMode ? airmodeClose : airmodeOpen);
			textAirMode.setText(airmodeText);
			dismissDialog();
			break;

		default:
			break;
		}

	}

	private void ActivityInitFunction() {
		mSettingsObserver = new SettingsObserver(mNavigationKeyHandler);
		mSettingsObserver.observe();
	}

	@Override
	public boolean onLongClick(View v) {
//		mWindowManagerFuncs.rebootSafeMode(true);
		if(mWindowManagerFuncs != null){
			mWindowManagerFuncs.rebootSafeMode(true);
		}else{
			option(mContext,true,"rebootSafeMode");
//			ShutdownThread.rebootSafeMode(mContext,  true);
		}
		dismiss();
		return true;
	}

	class SettingsObserver extends ContentObserver {
		SettingsObserver(Handler handler) {
			super(handler);
		}

		void observe() {
			ContentResolver resolver = mContext.getContentResolver();
			resolver.registerContentObserver(
					Settings.System.getUriFor(NAVI_KEY_HIDE), false, this);
		}

		@Override
		public void onChange(boolean selfChange) {
			if (selfChange) {

			}
			update();
		}

		void update() {
			// your logic code
		}
	}

	   private void changeAirplaneModeSystemSetting(boolean on) {
	        Settings.Global.putInt(
	                mContext.getContentResolver(),
	                Settings.Global.AIRPLANE_MODE_ON,
	                on ? 1 : 0);
	        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
	        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
	        intent.putExtra("state", on);
	        mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
	        if (!mHasTelephony) {
	            mAirplaneState = on ;
	        }
	    }
	
	@Override
	public void onAnimationEnd(Animation animation) {
      //			 TODO Auto-generated method stub
	if(animation == mDismissAnim){
			dismiss();
		}
	if(animation == mRebootTextAnim){
		if(SHOW_AIRMODE_PANLE){
			textAirMode.setVisibility(View.VISIBLE);
		}
		textReboot.setVisibility(View.VISIBLE);
		textShutdown.setVisibility(View.VISIBLE);
	}
	if(SHOW_AIRMODE_PANLE){
		if(animation == mAirmodeTextDismissAnim){
			layoutAirMode.setVisibility(View.INVISIBLE);
		}
	}
	if(animation == mRebootTextDismissAnim){
		  layoutReboot.setVisibility(View.INVISIBLE);
	}
	if(animation == mShutdownTextDismissAnim){
		  layoutShutdown.setVisibility(View.INVISIBLE);
	}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub
		if (animation == rebootAnim) {
			mAnimHandler.sendEmptyMessageDelayed(MSG_PLAY_SHUTDOWN_ANIM,60);
			mAnimHandler.sendEmptyMessageDelayed(MSG_PLAY_ICON_ANIM, 340);
		}/*else if(animation == rebootAnim){
			mAnimHandler.sendEmptyMessage(MSG_PLAY_SHUTDOWN_ANIM);
		}*/else if(animation == mDismissReboot){
			mAnimHandler.sendEmptyMessageDelayed(MSG_PLAY_SHUTDOWN_DISMISS,20);
			
		}
	}

	@Override
	public void onAnimationStart(FrameAnimation animation) {
	}

	@Override
	public void onAnimationEnd(FrameAnimation animation) {
		// TODO Auto-generated method stub
		/*if(animation == airModeIconAnim){
			airmode.setBackgroundResource(com.aurora.R.drawable.aurora_airmode_selector);
		}else*/ if(animation == rebootIconAnim){
			reboot.setBackgroundResource(com.aurora.R.drawable.aurora_reboot_selector);
		}else if(animation == shutdownIconAnim){
			shutdown.setBackgroundResource(com.aurora.R.drawable.aurora_shutdown_selector);
		}
	}

	@Override
	public void onRepeat(int repeatIndex) {
	}

	@Override
	public void onFrameChange(int repeatIndex, int frameIndex,
			int currentTime) {
	}

}
//
//class FrameAnimation {
//
//	public static final int ICON_SHUT_DOWN = 1;
//	public static final int ICON_AIRMODE_DOWN = 2;
//	public static final int ICON_REBOOT_DOWN = 3;
//
//	public int mIconType;
//
//
//	private Handler handler;
//	private View view;
//	private AnimationImageListener animationImageListener;
//
//	private FrameCallback[] callbacks;
//	private Drawable[] frames;
//	private ArrayList<Integer> mDrawables;
//	private int[] durations;
//	private int frameCount;
//
//	private boolean isRun;
//	private boolean fillAfter;
//	private boolean isOneShot;
//	private boolean isLimitless;
//	private int repeatTime;
//
//	private int currentRepeat;
//	private int currentFrame;
//	private int currentTime;
//	private int mLoadDivider;
//	private AnimationDrawable mAnimDrawable;
//	private Context mContext;
//
//	private int[] mAirmodePics = new int[]{
//			com.aurora.R.drawable.aurora_airmode_normal,
//			com.aurora.R.drawable.aurora_airmode004015,
//			com.aurora.R.drawable.aurora_airmode004016,
//			com.aurora.R.drawable.aurora_airmode004017,
//			com.aurora.R.drawable.aurora_airmode004018,
//			com.aurora.R.drawable.aurora_airmode004019,
//			com.aurora.R.drawable.aurora_airmode004020,
//			com.aurora.R.drawable.aurora_airmode004021,
//			com.aurora.R.drawable.aurora_airmode004022,
//			com.aurora.R.drawable.aurora_airmode004023,
//			com.aurora.R.drawable.aurora_airmode004024,
//			com.aurora.R.drawable.aurora_airmode004025,
//			com.aurora.R.drawable.aurora_airmode004026,
//			com.aurora.R.drawable.aurora_airmode004027,
//			com.aurora.R.drawable.aurora_airmode004028,
//			com.aurora.R.drawable.aurora_airmode004029,
//			com.aurora.R.drawable.aurora_airmode004030,
//			com.aurora.R.drawable.aurora_airmode004031,
//			com.aurora.R.drawable.aurora_airmode004032,
//			com.aurora.R.drawable.aurora_airmode004033,
//			com.aurora.R.drawable.aurora_airmode004034,
//			com.aurora.R.drawable.aurora_airmode004035,
//			com.aurora.R.drawable.aurora_airmode004036,
//			com.aurora.R.drawable.aurora_airmode004037,
//			com.aurora.R.drawable.aurora_airmode004038,
//			com.aurora.R.drawable.aurora_airmode004039,
//			com.aurora.R.drawable.aurora_airmode004040
//	};
//	private int[] mShutdownPics = new int[]{
//			com.aurora.R.drawable.aurora_shutdown_normal,
//			com.aurora.R.drawable.aurora_shutdown0016,
//			com.aurora.R.drawable.aurora_shutdown0017,
//			com.aurora.R.drawable.aurora_shutdown0018,
//			com.aurora.R.drawable.aurora_shutdown0019,
//			com.aurora.R.drawable.aurora_shutdown0020,
//			com.aurora.R.drawable.aurora_shutdown0021,
//			com.aurora.R.drawable.aurora_shutdown0022,
//			com.aurora.R.drawable.aurora_shutdown0023,
//			com.aurora.R.drawable.aurora_shutdown0024,
//			com.aurora.R.drawable.aurora_shutdown0025,
//			com.aurora.R.drawable.aurora_shutdown0026,
//			com.aurora.R.drawable.aurora_shutdown0027,
//			com.aurora.R.drawable.aurora_shutdown0028,
//			com.aurora.R.drawable.aurora_shutdown0029,
//			com.aurora.R.drawable.aurora_shutdown0030,
//			com.aurora.R.drawable.aurora_shutdown0031,
//			com.aurora.R.drawable.aurora_shutdown0032,
//			com.aurora.R.drawable.aurora_shutdown0033,
//			com.aurora.R.drawable.aurora_shutdown0034,
//			com.aurora.R.drawable.aurora_shutdown0035,
//			com.aurora.R.drawable.aurora_shutdown0036,
//			com.aurora.R.drawable.aurora_shutdown0037,
//			com.aurora.R.drawable.aurora_shutdown0038,
//			com.aurora.R.drawable.aurora_shutdown0039,
//			com.aurora.R.drawable.aurora_shutdown0040
//	};
//	private int[] mRebootPics = new int[]{
//			com.aurora.R.drawable.aurora_reboot_normal,
//			com.aurora.R.drawable.aurora_reboot0016,
//			com.aurora.R.drawable.aurora_reboot0017,
//			com.aurora.R.drawable.aurora_reboot0018,
//			com.aurora.R.drawable.aurora_reboot0019,
//			com.aurora.R.drawable.aurora_reboot0020,
//			com.aurora.R.drawable.aurora_reboot0021,
//			com.aurora.R.drawable.aurora_reboot0022,
//			com.aurora.R.drawable.aurora_reboot0023,
//			com.aurora.R.drawable.aurora_reboot0024,
//			com.aurora.R.drawable.aurora_reboot0025,
//			com.aurora.R.drawable.aurora_reboot0026,
//			com.aurora.R.drawable.aurora_reboot0027,
//			com.aurora.R.drawable.aurora_reboot0028,
//			com.aurora.R.drawable.aurora_reboot0029,
//			com.aurora.R.drawable.aurora_reboot0030,
//			com.aurora.R.drawable.aurora_reboot0031,
//			com.aurora.R.drawable.aurora_reboot0032,
//			com.aurora.R.drawable.aurora_reboot0033,
//			com.aurora.R.drawable.aurora_reboot0034,
//			com.aurora.R.drawable.aurora_reboot0035,
//			com.aurora.R.drawable.aurora_reboot0036,
//			com.aurora.R.drawable.aurora_reboot0037,
//			com.aurora.R.drawable.aurora_reboot0038,
//			com.aurora.R.drawable.aurora_reboot0039,
//			com.aurora.R.drawable.aurora_reboot0040
//	};
//	
//	private Runnable nextFrameRun = new Runnable() {
//		public void run() {
//			if (!isRun) {
//				end();
//				return;
//			}
//			currentTime += durations[currentFrame];
//			if (callbacks[currentFrame] != null) {
//				callbacks[currentFrame].onFrameEnd(currentFrame);
//			}
//			nextFrame();
//		}
//	};
//	
//
//
//	public FrameAnimation(Context context,  int iconType) {
//		mContext = context;
//		int animRes = 0;
//		switch (iconType) {
//		case ICON_SHUT_DOWN:
//			getDrawables(mShutdownPics);
//			break;
//		case ICON_REBOOT_DOWN:
//			getDrawables(mRebootPics);
//			break;
//		case ICON_AIRMODE_DOWN:
//			getDrawables(mAirmodePics);
//			break;
//
//		default:
//			break;
//		}
////		mAnimDrawable =(AnimationDrawable) mContext.getResources()
////				.getDrawable(animRes);
//		this.handler = new Handler();
//			init();
//	}
//	
//	private void getDrawables(int[] resArray){
//		frames = new Drawable[resArray.length];
//		long startTime = System.currentTimeMillis();
//		for(int i = 0;i<resArray.length;i++){
//			frames[i] = mContext.getResources().getDrawable(resArray[i]);
//		}
//		long endTime = System.currentTimeMillis();
//		Log.e("power", "totalTime:"+(endTime - startTime));
//	}
//	
//	public void setView(View view){
//		this.view = view;
//	}
//
//	private void init() {
//		this.frameCount = frames.length;
//		this.callbacks = new FrameCallback[frameCount];
//		this.isRun = false;
//		this.fillAfter = false;
//		this.isOneShot = true;
//		this.isLimitless = false;
//		this.repeatTime = 2;
//		durations = new int[frameCount];
//		long startTime = System.currentTimeMillis();
//		for (int i = 0; i < frameCount; i++) {
//			durations[i] = 10;
//		}
//	}
//
//	public void start() {
//		if (isRun) {
//			return;
//		}
//		this.isRun = true;
//		this.currentRepeat = -1;
//		this.currentFrame = -1;
//		this.currentTime = 0;
//		if (animationImageListener != null) {
//			animationImageListener.onAnimationStart(this);
//		}
//		startProcess();
//	}
//
//	public void stop() {
//		this.isRun = false;
//	}
//
//	private void startProcess() {
//		this.currentFrame = -1;
//		this.currentTime = 0;
//		this.currentRepeat++;
//		if (animationImageListener != null) {
//			animationImageListener.onRepeat(currentRepeat);
//		}
//		nextFrame();
//	}
//
//	private void endProcess() {
//		if (isOneShot || (!isLimitless && currentRepeat >= repeatTime - 1)
//				|| !isRun) {
//			end();
//		} else {
//			startProcess();
//		}
//	}
//
//	private void end() {
//		if (!fillAfter && frameCount > 0) {
//			view.setBackgroundDrawable(frames[0]);
//		}
//		if (animationImageListener != null) {
//			animationImageListener.onAnimationEnd(this);
//		}
//		this.isRun = false;
//	}
//
//	private void nextFrame() {
//		if (currentFrame == frameCount - 1) {
//			endProcess();
//			return;
//		}
//
//		currentFrame++;
//
//		changeFrame(currentFrame);
//
//		handler.postDelayed(nextFrameRun, durations[currentFrame]);
//	}
//
//	private void changeFrame(int frameIndex) {
//		synchronized (frames) {
//			view.setBackgroundDrawable(frames[frameIndex]);
//			frames[frameIndex] = null;
//			if (animationImageListener != null) {
//				animationImageListener.onFrameChange(currentRepeat, frameIndex,
//						currentTime);
//			}
//			if (callbacks[currentFrame] != null) {
//				callbacks[currentFrame].onFrameStart(frameIndex);
//			}
//
//		}
//	}
//
//	public int getSumDuration() {
//		int sumDuration = 0;
//		for (int duration : durations) {
//			sumDuration += duration;
//		}
//		return sumDuration;
//	}
//
//	public boolean isOneShot() {
//		return isOneShot;
//	}
//
//	public void setOneShot(boolean isOneShot) {
//		this.isOneShot = isOneShot;
//	}
//
//	public boolean isFillAfter() {
//		return fillAfter;
//	}
//
//	public void setFillAfter(boolean fillAfter) {
//		this.fillAfter = fillAfter;
//	}
//
//	public boolean isLimitless() {
//		return isLimitless;
//	}
//
//	public void setLimitless(boolean isLimitless) {
//		if (isLimitless) {
//			setOneShot(false);
//		}
//		this.isLimitless = isLimitless;
//	}
//
//	public void addFrameCallback(int index, FrameCallback callback) {
//		this.callbacks[index] = callback;
//	}
//
//	public void setAnimationImageListener(
//			AnimationImageListener animationImageListener) {
//		this.animationImageListener = animationImageListener;
//	}
//
//	public int getRepeatTime() {
//		return repeatTime;
//	}
//
//	public void setRepeatTime(int repeatTime) {
//		this.repeatTime = repeatTime;
//	}
//
//
//
//}
//
// interface AnimationImageListener {
//	public void onAnimationStart(FrameAnimation animation);
//
//	public void onAnimationEnd(FrameAnimation animation);
//
//	public void onRepeat(int repeatIndex);
//
//	public void onFrameChange(int repeatIndex, int frameIndex,
//			int currentTime);
//}
//
// interface FrameCallback {
//	public void onFrameStart(int startTime);
//
//	public void onFrameEnd(int endTime);
//}
//
