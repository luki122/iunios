package com.aurora.apihook.phonewindowmanger;


import java.lang.reflect.Method;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator.AnimatorUpdateListener;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.GnSurface;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManagerPolicy.WindowManagerFuncs;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.PowerManager;
import android.os.UserHandle;

import com.android.internal.policy.impl.EnableAccessibilityController;
import com.android.internal.policy.impl.PhoneWindowManager;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;

import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.view.WindowManagerPolicy.WindowManagerFuncs;

public class AuroraShutDownDialog extends Dialog implements DialogInterface, AnimatorUpdateListener,
AnimatorListener,android.view.View.OnClickListener,AnimationListener,OnLongClickListener{

	private static final int[] SHUTDOWN_ICON_IN_RES = { com.aurora.R.drawable.shutdown0,
			com.aurora.R.drawable.shutdown1, com.aurora.R.drawable.shutdown2, com.aurora.R.drawable.shutdown3,
			com.aurora.R.drawable.shutdown4, com.aurora.R.drawable.shutdown5, com.aurora.R.drawable.shutdown6,
			com.aurora.R.drawable.shutdown7, com.aurora.R.drawable.shutdown8, com.aurora.R.drawable.shutdown9,
			com.aurora.R.drawable.shutdown10, com.aurora.R.drawable.shutdown11,
			com.aurora.R.drawable.shutdown12, com.aurora.R.drawable.shutdown13, };

	private static final int[] REBOOT_ICON_IN_RES = { com.aurora.R.drawable.reboot0,
			com.aurora.R.drawable.reboot1, com.aurora.R.drawable.reboot2, com.aurora.R.drawable.reboot3,
			com.aurora.R.drawable.reboot4, com.aurora.R.drawable.reboot5, com.aurora.R.drawable.reboot6,
			com.aurora.R.drawable.reboot7, com.aurora.R.drawable.reboot8, com.aurora.R.drawable.reboot9,
			com.aurora.R.drawable.reboot10, com.aurora.R.drawable.reboot11, };

	/**
	 * 取消关机窗口时关机按钮的动画
	 */
	private static final int[] SHUTDOWN_ICON_OUT_RES = {
			com.aurora.R.drawable.shutdown_out1, com.aurora.R.drawable.shutdown_out2,
			com.aurora.R.drawable.shutdown_out3, com.aurora.R.drawable.shutdown_out4,
			com.aurora.R.drawable.shutdown_out5, com.aurora.R.drawable.shutdown_out6,
			com.aurora.R.drawable.shutdown_out7, com.aurora.R.drawable.shutdown_out8,
			com.aurora.R.drawable.shutdown_out9, com.aurora.R.drawable.shutdown_out10,
			com.aurora.R.drawable.shutdown_out11, com.aurora.R.drawable.shutdown_out12,
			com.aurora.R.drawable.shutdown_out13, com.aurora.R.drawable.shutdown_out14,

	};

	private static final int[] REBOOT_ICON_OUT_RES = { com.aurora.R.drawable.reboot_out1,
			com.aurora.R.drawable.reboot_out2, com.aurora.R.drawable.reboot_out3,
			com.aurora.R.drawable.reboot_out4, com.aurora.R.drawable.reboot_out5,
			com.aurora.R.drawable.reboot_out6, com.aurora.R.drawable.reboot_out7,
			com.aurora.R.drawable.reboot_out8, com.aurora.R.drawable.reboot_out9,
			com.aurora.R.drawable.reboot_out10, com.aurora.R.drawable.reboot_out11,
			com.aurora.R.drawable.reboot_out12, };

	/**
	 * 点击关机按钮时该按钮的动画
	 */
	private static final int[] SHUTDOW_ICON_GO_TO_OPTION_RES = { com.aurora.R.drawable.gtc0,
			com.aurora.R.drawable.gtc1, com.aurora.R.drawable.gtc2, com.aurora.R.drawable.gtc3, com.aurora.R.drawable.gtc4,
			com.aurora.R.drawable.gtc5, com.aurora.R.drawable.gtc6, com.aurora.R.drawable.gtc7, com.aurora.R.drawable.gtc8,

			com.aurora.R.drawable.gtc9, com.aurora.R.drawable.gtc10, com.aurora.R.drawable.gtc11,
			com.aurora.R.drawable.gtc12, com.aurora.R.drawable.gtc13, com.aurora.R.drawable.gtc14,
			com.aurora.R.drawable.gtc15, com.aurora.R.drawable.gtc16, com.aurora.R.drawable.gtc17,
			com.aurora.R.drawable.gtc18 };

	/**
	 * 点击重启按钮时该按钮的动画
	 */
	private static final int[] REBOOT_ICON_GO_TO_OPTION_RES = { com.aurora.R.drawable.cqt0,
			com.aurora.R.drawable.cqt1, com.aurora.R.drawable.cqt3, com.aurora.R.drawable.cqt4, com.aurora.R.drawable.cqt5,
			com.aurora.R.drawable.cqt6, com.aurora.R.drawable.cqt7, com.aurora.R.drawable.cqt8, com.aurora.R.drawable.cqt9,
			com.aurora.R.drawable.cqt10,

			com.aurora.R.drawable.cqt11, com.aurora.R.drawable.cqt12, com.aurora.R.drawable.cqt13,
			com.aurora.R.drawable.cqt14, com.aurora.R.drawable.cqt15, com.aurora.R.drawable.cqt16,
			com.aurora.R.drawable.cqt17, com.aurora.R.drawable.cqt18

	};

	private static final int[] REBOOT_LOADING_RES = { com.aurora.R.drawable.cql01,
			com.aurora.R.drawable.cql02, com.aurora.R.drawable.cql03, com.aurora.R.drawable.cql04,
			com.aurora.R.drawable.cql05, com.aurora.R.drawable.cql06, com.aurora.R.drawable.cql07,
			com.aurora.R.drawable.cql08, com.aurora.R.drawable.cql09, com.aurora.R.drawable.cql10,

			com.aurora.R.drawable.cql11, com.aurora.R.drawable.cql12, com.aurora.R.drawable.cql13,
			com.aurora.R.drawable.cql14, com.aurora.R.drawable.cql15, com.aurora.R.drawable.cql16,
			com.aurora.R.drawable.cql17, com.aurora.R.drawable.cql18, com.aurora.R.drawable.cql19,
			com.aurora.R.drawable.cql20, com.aurora.R.drawable.cql21, com.aurora.R.drawable.cql22,
			com.aurora.R.drawable.cql23, com.aurora.R.drawable.cql24, com.aurora.R.drawable.cql25,
			com.aurora.R.drawable.cql26, com.aurora.R.drawable.cql27, com.aurora.R.drawable.cql28,
			com.aurora.R.drawable.cql29, com.aurora.R.drawable.cql30, com.aurora.R.drawable.cql31,
			com.aurora.R.drawable.cql32 };

	private static final int[] SHUTDOWN_LOADING_RES = { com.aurora.R.drawable.gjl01,
			com.aurora.R.drawable.gjl02, com.aurora.R.drawable.gjl03, com.aurora.R.drawable.gjl04,
			com.aurora.R.drawable.gjl05, com.aurora.R.drawable.gjl06, com.aurora.R.drawable.gjl07,
			com.aurora.R.drawable.gjl08, com.aurora.R.drawable.gjl09, com.aurora.R.drawable.gjl10,

			com.aurora.R.drawable.gjl11, com.aurora.R.drawable.gjl12, com.aurora.R.drawable.gjl13,
			com.aurora.R.drawable.gjl14, com.aurora.R.drawable.gjl15, com.aurora.R.drawable.gjl16,
			com.aurora.R.drawable.gjl17, com.aurora.R.drawable.gjl18, com.aurora.R.drawable.gjl19,
			com.aurora.R.drawable.gjl20,

			com.aurora.R.drawable.gjl21, com.aurora.R.drawable.gjl22, com.aurora.R.drawable.gjl23,
			com.aurora.R.drawable.gjl24, com.aurora.R.drawable.gjl25, com.aurora.R.drawable.gjl26,
			com.aurora.R.drawable.gjl27, com.aurora.R.drawable.gjl28, com.aurora.R.drawable.gjl29,
			com.aurora.R.drawable.gjl30, com.aurora.R.drawable.gjl31, com.aurora.R.drawable.gjl32, };

	private static final String TAG = "ShutdownDialog";
	
	/**
	 * 退出窗口的信息
	 */
	private static final int MSG_DISSMISS_DIALOG = 0;
	
	/**
	 * 执行关机操作的信息
	 */
	private static final int MSG_SHUTDOWN = 1;
	
	/**
	 * 执行重启操作的信息
	 */
	private static final int MSG_REBOOT = 2;
	
	private static final int MSG_DISMISS_WITH_ANIMATION = 3;
	
	/**
	 * 执行重启进入安全模式的信息
	 */
	private static final int MSG_REBOOT_SAFE_MODE = 4;
	/**
	 * 播放关机文本出现的信息
	 */
	private static final int MSG_PLAY_SHUTDOWN_TEXT_SHOW_ANIM = 5;
	
	
	private static final int REBOOT_ICON_SHOW_DURATION = 400;
	
	private static final int SHUTDOWN_ICON_SHOW_DURATION = 466;
	
   private static final int REBOOT_ICON_HIDE_DURATION = 400;
	
	private static final int SHUTDOWN_ICON_HIDE_DURATION = 466;
	
	private static final int REBOOT_ICON_GO_TO_OPTION_DURATION = 633;

	private static final int SHUTDOWN_ICON_GO_TO_OPTION_DURATION = 633;
	
	private static final int SHUTDOWN_LOADING_DURATION = 1066;
	
	private static final int REBOOT_LOADING_DURATION = 1066;
	
	private static final int ICON_TRANSLATE_X_DURATION = 300;
	
	private static final int ICON_TRANSLATE_X_START_OFFSET = 300;
	
	
	/**
	 * 重启图标动画延迟
	 */
	private static final int ICON_REBOOT_SHOW_OFFSET = 100;
	
	/**
	 * 关机图标动画延迟
	 */
	private static final int ICON_SHUTDOWN_SHOW_OFFSET = 200;
	
	private static final int HANDLE_SHUTDOWN_DURATION = 1500;
	
	private final Context mContext;
	private int mWindowTouchSlop;
	private EnableAccessibilityController mEnableAccessibilityController;

	private boolean mIntercepted;
	private boolean mCancelOnUp;
    private WindowManagerFuncs mWindowManagerFuncs;
    
	
	private View mContentView;
	private View mBlurParentView;
	private View mLeftWidget,mRightWidget;
	private View mBlackView;
	
	private ImageView mShutdownBg;
	private ImageView mShutdownIcon;
	private ImageView mRebootBg;
	private ImageView mRebootIcon;
	
	private TextView mShutdowText;
	private TextView mRebootText;
	
	
	private Animation mIconBgShowAnim;
	private Animation mIconBgHideAnim;
	private Animation mShutdownTextShowAnim;
	private Animation mRebootTextShowAnim;
	private Animation mTextHideAnim;
	private Animation mIconPressedExitAnim;
	
	private Animation mIconHideAnimation;
	/**
	 * 重启按钮动画 30帧每秒
	 */
	private ValueAnimator mRebootIconShowAnimator;
	
	/**
	 * 关机按钮动画 30帧每秒
	 */
	private ValueAnimator mShutdownIconShowAnimator;
	
	/**
	 * 退出关机界面时关机按钮的动画
	 */
	private ValueAnimator mShutdowIconHideAnimator;
	
	private ValueAnimator mRebootIconHideAnimator;
	
	/**
	 * 点击关机按钮时该按钮的动画
	 */
	private ValueAnimator mShutdownGotoOptionAnimator;
	
	private ValueAnimator mRebootGotoOptionAnimator;
	
	private ObjectAnimator mLeftWidgetTranslationXAnim;
	
	private ObjectAnimator mRightWidgetTranslationXAnim;
	
	private ValueAnimator mShutdownLoadingAnimator;
	
	private ValueAnimator mRebootLodingAnimator;
	
	private Interpolator mTranslateXInterpolator;
	
	private int mCenterX;
	private int mScreenWidth ,mScreenHeight;
	private int mKeyBack = 0;
	private final int NAV_MODE_CANCLE = -1;
	private final int NAV_MODE_SET_COLOR = 6;
	private float mTranslateRightAnimX;
	
	private boolean mReboot = false;
	 private boolean mHasTelephony;
	 private boolean mAirplaneState;
	 private boolean mIsWaitingForEcmExit = false;
	 private boolean mRebootSafeMode = false;
	 private boolean mDismissable = true;
	private String mSoundPath = "/system/media/audio/ui/PowerMenu.ogg";
	private final String NAV_COLOR = "#D9000000";

	private final String NAV_ACTION = "aurora.action.SET_NAVIBAR_COLOR";
	private static final String NAVI_KEY_HIDE = "navigation_key_hide";
	
	private AlertDialog mRebootSafeModeAlert;
	
	private Runnable mBlurRunnalbe = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Bitmap blurBit = SurfaceControl.screenshot(mScreenWidth, mScreenHeight);
			if(blurBit != null){
				Drawable dr = new BitmapDrawable(mContext.getResources(),Blur.doBlur(mContext,blurBit,10));//生产模糊图片  
				if(dr != null){
					mBlurParentView.setBackground(dr);
				}
			}
		}
	};
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			
			if(msg.what == MSG_DISSMISS_DIALOG){
				superDismiss();
			}else if(msg.what ==MSG_REBOOT){
				option(mContext,null,true,"reboot");
			}else if(msg.what == MSG_SHUTDOWN){
				if(mWindowManagerFuncs != null){
					mWindowManagerFuncs.shutdown(false);
				}else{
					option(mContext,false,"shutdown");
				}
			}else if(msg.what == MSG_DISMISS_WITH_ANIMATION){
				if(isShowing()){
					dismiss();
				}
			}else if(msg.what == MSG_REBOOT_SAFE_MODE){
				if(mWindowManagerFuncs != null){
					mWindowManagerFuncs.rebootSafeMode(false);
				}else{
					option(mContext,false,"rebootSafeMode");
				}
			}else if(msg.what == MSG_PLAY_SHUTDOWN_TEXT_SHOW_ANIM){
				mShutdowText.startAnimation(mShutdownTextShowAnim);
			}
			
		};
	};
	
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
//	                    dismiss();
	                	mHandler.sendEmptyMessage(MSG_DISMISS_WITH_ANIMATION);
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
	    
	public AuroraShutDownDialog(Context context,WindowManagerFuncs wmf) {
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
		
		mScreenWidth = context.getResources().getDisplayMetrics().widthPixels;
		mScreenHeight = context.getResources().getDisplayMetrics().heightPixels;
		
		mRebootSafeModeAlert = new AlertDialog.Builder(mContext)
		.setTitle( com.android.internal.R.string.reboot_safemode_title)
		.setMessage(com.android.internal.R.string.reboot_safemode_confirm)
		     .setPositiveButton(com.android.internal.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    	playClickAnimation();
                    	mReboot = false;
        				mLeftWidget.startAnimation(mIconPressedExitAnim);
                    }
                })
                .setNegativeButton(com.android.internal.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    	
                    	dialog.dismiss();
                       
                    }
                })
                .create();
		mRebootSafeModeAlert.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
		
		// TODO Auto-generated constructor stub
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
	
	private void superDismiss(){
		super.dismiss();
	}
	
	private void option(Context context,String reson,boolean confirm,String name){
		final PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		pm.reboot("");
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
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(com.aurora.R.layout.shutdown_layout);
		mContentView = getWindow().getDecorView();
		mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
		
		initWidget();
		 initAnimations();
		 
	}
	
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		if (EnableAccessibilityController
				.canEnableAccessibilityViaGesture(mContext)) {
			mEnableAccessibilityController = new EnableAccessibilityController(
					mContext,null);
			super.setCanceledOnTouchOutside(false);
		} else {
			mEnableAccessibilityController = null;
			super.setCanceledOnTouchOutside(true);
		}
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
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
	
	private boolean cancelTouchOutSide(int eventX,int eventY){
		
		return (eventX < -mWindowTouchSlop
				|| eventY < -mWindowTouchSlop
				|| eventX >= mLeftWidget.getWidth() + mWindowTouchSlop
				|| eventY >= mLeftWidget.getHeight() + mWindowTouchSlop)||(eventX < -mWindowTouchSlop
						|| eventY < -mWindowTouchSlop
						|| eventX >= mRightWidget.getWidth() + mWindowTouchSlop
						|| eventY >= mRightWidget.getHeight() + mWindowTouchSlop);
	}

	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		final int action = event.getActionMasked();
		if (action == MotionEvent.ACTION_DOWN) {
			View decor = getWindow().getDecorView();
			final int eventX = (int) event.getX();
			final int eventY = (int) event.getY();
			if (cancelTouchOutSide(eventX, eventY)) {
				mCancelOnUp = true;
			}
		}else	if (action == MotionEvent.ACTION_UP) {
			Log.d(TAG, "touch up");
//			if (mCancelOnUp && mDismissable) {
				cancel();
//			}
			mCancelOnUp = false;
			mIntercepted = false;
		}
		
		return super.onTouchEvent(event);
	}
	

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mKeyBack == 0) {
				dismiss();
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

	public void playSounds(int sound, int number) {/*
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

	*/}
	
	
	
	private void initWidget(){
		mShutdownBg = (ImageView)findViewById(android.R.id.button2);
		mShutdownIcon = (ImageView)findViewById(com.aurora.R.id.aurora_shutdown_btn);
		mShutdownBg.setOnClickListener(this);
		mShutdownBg.setOnLongClickListener(this);
		mRebootBg = (ImageView)findViewById(android.R.id.button1);
		mRebootIcon = (ImageView)findViewById(com.aurora.R.id.aurora_reboot_btn);
		mRebootBg.setOnClickListener(this);
		mShutdowText = (TextView)findViewById(com.aurora.R.id.aurora_shutdown_text);
		mRebootText = (TextView)findViewById(com.aurora.R.id.aurora_reboot_text);
		mLeftWidget = findViewById(com.aurora.R.id.reboot_layout);
		mRightWidget = findViewById(com.aurora.R.id.shutdown_layout);
		mBlackView = findViewById(com.aurora.R.id.aurora_customPanel);
		mBlurParentView = findViewById(com.aurora.R.id.aurora_topPanel);
		
		
	
		mHandler.post(mBlurRunnalbe);
		
	}
	
	
	private void initAnimations(){
		mIconBgShowAnim = AnimationUtils.loadAnimation(getContext(), com.aurora.R.anim.shutdown_btn_show_anim);
		mIconBgHideAnim = AnimationUtils.loadAnimation(getContext(), com.aurora.R.anim.shutdown_btn_hide_anim);
		mShutdownTextShowAnim = AnimationUtils.loadAnimation(getContext(), com.aurora.R.anim.shutdown_text_show_anim);
		mRebootTextShowAnim = AnimationUtils.loadAnimation(getContext(), com.aurora.R.anim.reboot_text_show_anim);
		mTextHideAnim = AnimationUtils.loadAnimation(getContext(), com.aurora.R.anim.shutdown_dialog_text_hide_anim);
		mIconPressedExitAnim = AnimationUtils.loadAnimation(getContext(), com.aurora.R.anim.shutdown_widget_exit_anim);
		mIconHideAnimation = AnimationUtils.loadAnimation(getContext(), com.aurora.R.anim.shutdown_dialog_text_hide_anim);
		mIconPressedExitAnim.setAnimationListener(this);
		mTextHideAnim.setAnimationListener(this);
		mIconHideAnimation.setAnimationListener(this);
		//30/1000  14/x
		/*
		 * shutdown icon animator
		 */
		mShutdownIconShowAnimator = ValueAnimator.ofInt(0,SHUTDOWN_ICON_IN_RES.length -1);
		mShutdownIconShowAnimator.setDuration(SHUTDOWN_ICON_SHOW_DURATION);
		mShutdownIconShowAnimator.addUpdateListener(this);
		mShutdownIconShowAnimator.addListener(this);
		mShutdownIconShowAnimator.setStartDelay(ICON_SHUTDOWN_SHOW_OFFSET);
		/*
		 * reboot icon animator
		 */
		mRebootIconShowAnimator = ValueAnimator.ofInt(0,REBOOT_ICON_IN_RES.length -1);
		mRebootIconShowAnimator.setDuration(REBOOT_ICON_SHOW_DURATION);
		mRebootIconShowAnimator.addUpdateListener(this);
		mRebootIconShowAnimator.addListener(this);
		mRebootIconShowAnimator.setStartDelay(ICON_REBOOT_SHOW_OFFSET);
		
		/*
		 * shutdown icon hide animator
		 */
		mShutdowIconHideAnimator = ValueAnimator.ofInt(0,SHUTDOWN_ICON_OUT_RES.length -1);
		mShutdowIconHideAnimator.setDuration(SHUTDOWN_ICON_HIDE_DURATION);
		mShutdowIconHideAnimator.addUpdateListener(this);
		mShutdowIconHideAnimator.addListener(this);
		
		/*
		 * reboot icon hide animator
		 */
		mRebootIconHideAnimator = ValueAnimator.ofInt(0,REBOOT_ICON_OUT_RES.length -1);
		mRebootIconHideAnimator.setDuration(REBOOT_ICON_HIDE_DURATION);
		mRebootIconHideAnimator.addUpdateListener(this);
		mRebootIconHideAnimator.addListener(this);
		
		
		mShutdownGotoOptionAnimator = ValueAnimator.ofInt(0,SHUTDOW_ICON_GO_TO_OPTION_RES.length -1);
		mShutdownGotoOptionAnimator.setDuration(SHUTDOWN_ICON_GO_TO_OPTION_DURATION);
		mShutdownGotoOptionAnimator.addUpdateListener(this);
		mShutdownGotoOptionAnimator.addListener(this);
		
		
		mRebootGotoOptionAnimator = ValueAnimator.ofInt(0,REBOOT_ICON_GO_TO_OPTION_RES.length -1);
		mRebootGotoOptionAnimator.setDuration(REBOOT_ICON_GO_TO_OPTION_DURATION);
		mRebootGotoOptionAnimator.addUpdateListener(this);
		mRebootGotoOptionAnimator.addListener(this);
		
		
		
		
		mShutdownLoadingAnimator = ValueAnimator.ofInt(0,SHUTDOWN_LOADING_RES.length -1);
		mShutdownLoadingAnimator.setDuration(SHUTDOWN_LOADING_DURATION);
		mShutdownLoadingAnimator.addUpdateListener(this);
		mShutdownLoadingAnimator.addListener(this);
		mShutdownLoadingAnimator.setRepeatCount(Integer.MAX_VALUE);
		
		mRebootLodingAnimator = ValueAnimator.ofInt(0,REBOOT_LOADING_RES.length -1);
		mRebootLodingAnimator.setDuration(REBOOT_LOADING_DURATION);
		mRebootLodingAnimator.addUpdateListener(this);
		mRebootLodingAnimator.addListener(this);
		mRebootLodingAnimator.setRepeatCount(Integer.MAX_VALUE);
	}
	
	
	@Override
	public void show() {
		// TODO Auto-generated method stub
		super.show();
		mShutdownBg.startAnimation(mIconBgShowAnim);
		mRebootBg.startAnimation(mIconBgShowAnim);
		
		mRebootText.startAnimation(mRebootTextShowAnim);
		
		//关机文本比重启文本推迟100毫秒播放动画
		mHandler.sendEmptyMessageDelayed(MSG_PLAY_SHUTDOWN_TEXT_SHOW_ANIM, 100);
		
		mRebootIconShowAnimator.start();
		mShutdownIconShowAnimator.start();
		
		mContext.sendBroadcast(new Intent(
				"com.aurora.action.SHUTDOWN_DIALOG_SHOW"));
		
	}
	
	@Override
	public void dismiss() {
		// TODO Auto-generated method stub
		if(!mDismissable){
			return;
		}
		mShutdownBg.startAnimation(mIconBgHideAnim);
		mRebootBg.startAnimation(mIconBgHideAnim);
		mRebootIconHideAnimator.start();
		mShutdowIconHideAnimator.start();
		mRebootText.startAnimation(mTextHideAnim);
		mShutdowText.startAnimation(mTextHideAnim);
		mContext.sendBroadcast(new Intent(
				"com.aurora.action.SHUTDOWN_DIALOG_DISMISS"));
	}
	
	

	@Override
	public void onAnimationCancel(Animator animator) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationEnd(Animator animator) {
		// TODO Auto-generated method stub
		if(animator == mShutdowIconHideAnimator || animator == mShutdowIconHideAnimator){
			mLeftWidget.setVisibility(View.INVISIBLE);
			mRightWidget.setVisibility(View.INVISIBLE);
			Log.d(TAG, "hide animation end");
			mHandler.sendEmptyMessageDelayed(MSG_DISSMISS_DIALOG, 100);
//			super.dismiss();
		}else if(animator == mRightWidgetTranslationXAnim || animator == mLeftWidgetTranslationXAnim){
			if(mReboot){
				mRebootBg.startAnimation(mIconHideAnimation);
				mRebootGotoOptionAnimator.start();
			}else{
				mShutdownBg.startAnimation(mIconHideAnimation);
				mShutdownGotoOptionAnimator.start();
			}
		
		}else if(animator == mRebootGotoOptionAnimator || animator == mShutdownGotoOptionAnimator){
			mDismissable = false;
			mBlackView.setAlpha(1.0f);
			if(mRebootSafeMode){
				mShutdownLoadingAnimator.start();
				mHandler.sendEmptyMessageDelayed(MSG_REBOOT_SAFE_MODE,HANDLE_SHUTDOWN_DURATION);
				return;
			}
			if(mReboot){
				mRebootLodingAnimator.start();
				mHandler.sendEmptyMessageDelayed(MSG_REBOOT, HANDLE_SHUTDOWN_DURATION);
			}else{
				mShutdownLoadingAnimator.start();
				mHandler.sendEmptyMessageDelayed(MSG_SHUTDOWN,HANDLE_SHUTDOWN_DURATION);
				
			}
		}
	}

	@Override
	public void onAnimationRepeat(Animator animator) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationStart(Animator animator) {
		// TODO Auto-generated method stub
		if(animator == mRebootIconShowAnimator){
			mRebootIcon.setImageDrawable(null);
		}else if(animator == mShutdownIconShowAnimator){
			 mShutdownIcon.setImageDrawable(null);
		}
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animator) {
		// TODO Auto-generated method stub
		if(animator == mRebootIconShowAnimator){
			int index = (int) animator.getAnimatedValue();
			mRebootIcon.setImageResource(REBOOT_ICON_IN_RES[index]);
		}else if(animator == mShutdownIconShowAnimator){
			
			int index = (int) animator.getAnimatedValue();
			 mShutdownIcon.setImageResource(SHUTDOWN_ICON_IN_RES[index]);
		}else if(animator == mRebootIconHideAnimator){
			int index = (int) animator.getAnimatedValue();
			mRebootIcon.setImageResource(REBOOT_ICON_OUT_RES[index]);
		}else if(animator == mShutdowIconHideAnimator){
			int index = (int) animator.getAnimatedValue();
			mShutdownIcon.setImageResource(SHUTDOWN_ICON_OUT_RES[index]);
		}else if(animator == mShutdownGotoOptionAnimator){
			int index = (int) animator.getAnimatedValue();
			mShutdownIcon.setImageResource(SHUTDOW_ICON_GO_TO_OPTION_RES[index]);
		}else if(animator == mRebootGotoOptionAnimator){
			int index = (int) animator.getAnimatedValue();
			mRebootIcon.setImageResource(REBOOT_ICON_GO_TO_OPTION_RES[index]);
		}else if(animator == mShutdownLoadingAnimator){
			int index = (int) animator.getAnimatedValue();
			mShutdownIcon.setImageResource(SHUTDOWN_LOADING_RES[index]);
		}else if(animator == mRebootLodingAnimator){
			int index = (int) animator.getAnimatedValue();
			mRebootIcon.setImageResource(REBOOT_LOADING_RES[index]);
		}
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		if(view == mRebootBg){
			mReboot = true;
			mRightWidget.startAnimation(mIconPressedExitAnim);
		}else if(view == mShutdownBg){
			mReboot = false;
			mLeftWidget.startAnimation(mIconPressedExitAnim);
		
		}
		playClickAnimation();
		
	}
	
	
	private Interpolator getTranslateXInterpolator(){
		if(mTranslateXInterpolator == null){
			mTranslateXInterpolator = AnimationUtils.loadInterpolator(mContext, com.aurora.R.interpolator.shutdown_icon_translate_ease_in_out);
		}
		return mTranslateXInterpolator;
	}
	
	
	
	private void playClickAnimation(){
		mShutdowText.startAnimation(mTextHideAnim);
		mRebootText.startAnimation(mTextHideAnim);
		
		getCenterX();
		mLeftWidgetTranslationXAnim = ObjectAnimator.ofFloat(mLeftWidget, "translationX", 0f,mTranslateRightAnimX);
		mLeftWidgetTranslationXAnim.setDuration(ICON_TRANSLATE_X_DURATION);
		mLeftWidgetTranslationXAnim.addListener(this);
		mLeftWidgetTranslationXAnim.setInterpolator(getTranslateXInterpolator());
		
		mRightWidgetTranslationXAnim = ObjectAnimator.ofFloat(mRightWidget, "translationX", 0f,-mTranslateRightAnimX);
		mRightWidgetTranslationXAnim.setDuration(ICON_TRANSLATE_X_DURATION);
		mRightWidgetTranslationXAnim.addListener(this);
		mRightWidgetTranslationXAnim.setInterpolator(getTranslateXInterpolator());
		
		ValueAnimator gotoOptionAlpha = ValueAnimator.ofFloat(1.0f,0.1f);
		gotoOptionAlpha.setDuration(300);
		gotoOptionAlpha.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animator) {
				// TODO Auto-generated method stub
				float alpha = (float) animator.getAnimatedValue();
				if(mReboot){
					mRebootIcon.setAlpha(alpha);
				}else{
					mShutdownIcon.setAlpha(alpha);
				}
			}
		});
//		gotoOptionAlpha.setStartDelay(600);
		AnimatorSet set = new AnimatorSet();
		set.setStartDelay(ICON_TRANSLATE_X_START_OFFSET);
		set.playTogether(mLeftWidgetTranslationXAnim,mRightWidgetTranslationXAnim,gotoOptionAlpha);
		set.start();
//		gotoOptionAlpha.start();
	}
	
	private void getCenterX(){
		View parent = findViewById(com.aurora.R.id.aurora_parentPanel);
		mCenterX = parent.getMeasuredWidth() / 2;
		mTranslateRightAnimX = mCenterX - mLeftWidget.getMeasuredWidth() / 2;
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		// TODO Auto-generated method stub
		if(animation == mIconPressedExitAnim){
			if(mReboot){
				mRightWidget.setVisibility(View.INVISIBLE);
			}else{
				mLeftWidget.setVisibility(View.INVISIBLE);
			}
		}else if(animation == mTextHideAnim ){
			mRebootText.setVisibility(View.GONE);
			mShutdowText.setVisibility(View.GONE);
		}else if(animation == mIconHideAnimation){
			mShutdownBg.setVisibility(View.INVISIBLE);
			mRebootBg.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onAnimationRepeat(Animation arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationStart(Animation arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onLongClick(View arg0) {
		mRebootSafeMode = true;
		mRebootSafeModeAlert.show();
		return true;
	}
	
	
	
	
}
