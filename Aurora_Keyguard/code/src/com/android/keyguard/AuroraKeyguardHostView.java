package com.android.keyguard;

import java.io.File;
import java.util.List;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardSecurityModel.SecurityMode;
import com.android.keyguard.utils.AnimUtils;
import com.android.keyguard.utils.AuroraLog;
import com.android.keyguard.utils.LockScreenBgUtils;
import com.android.keyguard.utils.LockScreenUtils;
import com.android.keyguard.utils.Trace;
import com.android.keyguard.view.AlphaBackground;
import com.android.keyguard.view.AuroraSelectorView;
import com.android.keyguard.view.StatusView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.RemoteControlClient;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.UserManager;
import android.os.SystemProperties;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.BaseSavedState;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import aurora.app.AuroraAlertDialog;
import android.provider.Settings;
import android.database.ContentObserver;
import android.content.ContentResolver;

public class AuroraKeyguardHostView extends KeyguardViewBase {

    public static final boolean DEBUG = true;

    public static final String BLACKSTAYLE_LOCKPAPER_GROUP_1 = "Fascinating";
    public static final String BLACKSTAYLE_LOCKPAPER_GROUP_2 = "MissPuff";
    public static final String DEFAULT_LOCKPAPER_GROUP = "Dream";
    private static final String TAG = "AuroraKeyguardHostView";
    private static final String AURORA_BLACK_IMMERSE = "aurorablackBG8345";
    private static final String AURORA_WHITE_IMMERSE = "aurorawhiteBG653";

    public static final int STATUS_BAR_DISABLE_BACK = 0x00400000;
    public static final int STATUS_BAR_DISABLE_CLOCK = 0x00800000;

	 // Aurora liugj 2014-07-18 added for voice unlock screen start
    private static final String INTENT_ACTION_VOICEWAKEUP = "com.qualcomm.listen.voicewakeup.unlock";
	 private static final String INTENT_ACTION_VOICESIMLOCK = "com.qualcomm.listen.voicewakeup.sim_locked";
    // Aurora liugj 2014-07-18 added for voice unlock screen end
	 private static final String ACTION_RESET_ALARM = "com.aurora.change.RESET_ALARM";

	private static final String AURORA_SERVICECMD = "com.android.music.musicservicecommand";
	private static final String AURORA_MUSIC_ACTION = "android.com.auroramusic.startplay";
	private static final String AURORA_CMDNAME = "command";
	private static final String AURORA_CMDSTOP = "stop";
	private static final String AURORA_CMDPAUSE = "pause";
	private static final String AURORA_CMDPLAY = "play";
	private static final String AURORA_CMDPREV = "previous";
	private static final String AURORA_CMDNEXT = "next";
	private static final String AURORA_CMDTOGGLEPAUSE = "togglepause";

    private PowerManager mPowerManager;
    private Context mContext;

    private boolean mIsVerifyUnlockOnly;
    private boolean mEnableFallback; // TODO: This should get the value from KeyguardPatternView
    private SecurityMode mCurrentSecuritySelection = SecurityMode.Invalid;

    protected OnDismissAction mDismissAction;

    protected int mFailedAttempts;
    private LockPatternUtils mLockPatternUtils;

    private KeyguardSecurityModel mSecurityModel;
//    private KeyguardViewStateManager mViewStateManager;
    private KeyguardSecurityViewFlipper mSecurityViewContainer;

    private boolean mSafeModeEnabled;
	 // Aurora liugj 2014-07-18 added for voice unlock screen start
    private boolean mVoiceLocked = false;
    // Aurora liugj 2014-07-18 added for voice unlock screen end
	 // Aurora liugj 2014-12-08 added for bug-10381 start
	 private boolean isBootCompleted = false;
	 // Aurora liugj 2014-12-08 added for bug-10381 end
	
	// Aurora liugj 2015-01-06 added for bug-10669 start
	private boolean isRunningAnim = false;
	// Aurora liugj 2015-01-06 added for bug-10669 end

    //private ImageView mWallPaper;
    //private LinearLayout mWallPaperBg;
    private ImageView mWallPaperColor;

    protected int mClientGeneration;

    private AlphaBackground mAlphaBackground;

	private static final String NAVI_KEY_HIDE = "navigation_key_hide";
	private int mNaviState = 0;
	private boolean mIsWindowFocus = true;

//    Animator customAppearingAnim, customDisappearingAnim;

    public interface OnDismissAction {
        /* returns true if the dismiss should be deferred */
        boolean onDismiss();
    }

    public AuroraKeyguardHostView(Context context) {
        this(context, null);
    }

    //@SuppressLint("InlinedApi")
	public AuroraKeyguardHostView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mSecurityModel = new KeyguardSecurityModel(mContext);
        mLockPatternUtils = new LockPatternUtils(mContext);
        
        //mLockPatternUtils.auroraSetLong(LockPatternUtils.PASSWORD_TYPE_KEY, DevicePolicyManager.PASSWORD_QUALITY_COMPLEX);

        mSafeModeEnabled = LockPatternUtils.isSafeModeEnabled();
        if (mSafeModeEnabled) {
            Log.v(TAG, "Keyguard widgets disabled by safe mode");
        }
    }

    private KeyguardUpdateMonitorCallback mUpdateMonitorCallbacks = new KeyguardUpdateMonitorCallback() {
        @Override
        public void onBootCompleted() {

        }

        @Override
        public void onUserSwitchComplete(int userId) {

        }

        @Override
        public void onMusicClientIdChanged(int clientGeneration, boolean clearing,
                android.app.PendingIntent intent) {

        }

        @Override
        public void onMusicPlaybackStateChanged(int playbackState, long eventTime) {

        }

		// Aurora liugj 2014-06-18 added for MusicLockScreen start
		 @Override
        public void onMusicPlaybackStateChanged(boolean isPlaying) {
        	Log.d("liugj", "=====onMusicPlaybackStateChanged====="+isPlaying);
        	if (isPlaying) {
        		KeyguardSecurityView view = getSecurityLockView(mCurrentSecuritySelection);
				if (view instanceof AuroraSelectorView) {
					if (!StatusView.isMusicShow) {
						((AuroraSelectorView) view)
								.onResume(KeyguardSecurityView.VIEW_MUSIC);
					}
				}
			}
        }
		// Aurora liugj 2014-06-18 added for MusicLockScreen end
		  
    };

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mViewMediatorCallback != null) {
            mViewMediatorCallback.keyguardDoneDrawing();
        }
    }

    @Override
    protected void onFinishInflate() {

    	Log.d("vulcan-direct",String.format("onFinishInflate: stack = %s", Trace.StackToString(new Throwable())));
        mSecurityViewContainer = ( KeyguardSecurityViewFlipper ) findViewById(R.id.view_flipper);
        //mWallPaperBg = ( LinearLayout ) findViewById(R.id.lock_screen_bg_view);
        //mWallPaper = ( ImageView ) findViewById(R.id.lock_screen_bg_wallpaper);
//        mWallPaperColor = ( ImageView ) findViewById(R.id.lock_screen_bg_color_view);
        mAlphaBackground = ( AlphaBackground ) findViewById(R.id.lock_screen_alpha_bg);
        
		// Aurora liugj 2014-12-08 added for bug-10381 start
		isBootCompleted = SystemProperties.get("sys.boot_completed", "0").equals("1") ? true : false;
		// Aurora liugj 2014-12-08 added for bug-10381 end

        /*if (isSecure()) {
            mWallPaperBg.setVisibility(View.VISIBLE);
//            mWallPaperBg.setBackgroundColor(Color.RED);
//            mWallPaper.setVisibility(View.VISIBLE);
            LockScreenBgUtils.getInstance().setViewBg(mWallPaper);
//            mWallPaper.setBackground(mContext.getWallpaper());
//            Bitmap bitmap = LockScreenBgUtils.getInstance().getLockScreenBg(mWallPaper);
//            mWallPaper.setBackground(new BitmapDrawable(bitmap));
        }*/
//        if (isSecure()) {
//            findViewById(R.id.aurora_host_view).setBackgroundColor(Color.DKGRAY);
//        }

        setBackButtonEnabled(false);
        setSystemUiClockEnabled(false);
        setSystemUiVisibility(getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

		 // Aurora liugj 2014-06-27 add for MusicLockScreen start
        if (StatusView.isPlayingMusic) {
        	boolean isMusicServiceRun = isMusicServiceRun();
			Log.d("liugj", "=====isMusicServiceRun====="+isMusicServiceRun);
			if (!isMusicServiceRun) {
				StatusView.isPlayingMusic = false;
			}
		}
		 // Aurora liugj 2014-06-27 add for MusicLockScreen end
        
        //Vulcan changed it to  showPrimarySecurityLockScreen2, for new function(direct unlock).
        showPrimarySecurityLockScreen2(false);
        updateSecurityViews();
        
//        final LayoutTransition transition = new LayoutTransition();
//        createAnimations(transition);
////        transition.setAnimator(LayoutTransition.APPEARING, customAppearingAnim);
//        transition.setAnimator(LayoutTransition.DISAPPEARING, customDisappearingAnim);
//        mSecurityViewContainer.setLayoutTransition(transition);
//        mSecurityViewContainer.setInAnimation(null);
//        mSecurityViewContainer.setOutAnimation(null);
    }
    
	 // Aurora liugj 2014-06-27 add for MusicLockScreen start
    private boolean isMusicServiceRun() {
		return isServiceRunning(mContext, "com.android.music.MediaPlaybackService");
	}
	 // Aurora liugj 2014-06-27 add for MusicLockScreen end
    
	// Aurora liugj 2014-06-27 add for MusicLockScreen start
	/**
     * 用来判断服务是否运行.
     * @param context
     * @param className 判断的服务名字
     * @return true 在运行 false 不在运行
     */
	private boolean isServiceRunning(Context context, String className) {
		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager
				.getRunningServices(80);
		if (!(serviceList.size() > 0)) {
			return false;
		}
		for (ActivityManager.RunningServiceInfo runServiceInfo : serviceList) {
			if (runServiceInfo.service.getClassName().equals(className) == true) {
				isRunning = true;
				break;
			}
		}
		return isRunning;
	}
	// Aurora liugj 2014-06-27 add for MusicLockScreen end
	
    private void setBackButtonEnabled(boolean enabled) {
        if (mContext instanceof Activity)
            return; // always enabled in activity mode
        setSystemUiVisibility(enabled ?
        getSystemUiVisibility() & ~STATUS_BAR_DISABLE_BACK
                : getSystemUiVisibility() | STATUS_BAR_DISABLE_BACK);
    }

    private void setSystemUiClockEnabled(boolean enabled) {
        setSystemUiVisibility(enabled ? getSystemUiVisibility() & ~STATUS_BAR_DISABLE_CLOCK
                : getSystemUiVisibility() | STATUS_BAR_DISABLE_CLOCK);
    }

    private void updateSecurityViews() {
        int children = mSecurityViewContainer.getChildCount();
        for (int i = 0; i < children; i++) {
            updateSecurityView(mSecurityViewContainer.getChildAt(i));
        }
        // TODO updateSecurityViews (Callbacks.)
    }

    private void updateSecurityView(View view) {
        if (view instanceof KeyguardSecurityView) {
            KeyguardSecurityView ksv = ( KeyguardSecurityView ) view;
            ksv.setKeyguardCallback(mCallback);
            ksv.setLockPatternUtils(mLockPatternUtils);
            if (view instanceof AuroraSelectorView) {
                if (mAlphaBackground != null) {
                    (( AuroraSelectorView ) view).setAlphaBackgroundView(mAlphaBackground);
                }
            }
        } else {
            Log.w(TAG, "View " + view + " is not a KeyguardSecurityView");
        }
    }

    public void goToWidget(int appWidgetId) {
    }

    public void setLockPatternUtils(LockPatternUtils utils) {
        mSecurityModel.setLockPatternUtils(utils);
//      mLockPatternUtils = utils;
      updateSecurityViews();
    }

    public void setAuroraLockPatternUtils(LockPatternUtils utils) {
        mSecurityModel.setLockPatternUtils(utils);
//        mLockPatternUtils = utils;
        updateSecurityViews();
    }

	 // Aurora liugj 2014-07-21 added for voice unlock screen start
    private BroadcastReceiver mVoiceLockReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d("liugj", mCurrentSecuritySelection+"=====mVoiceLockReceiver====="+action);
			if (INTENT_ACTION_VOICEWAKEUP.equals(action)) {
				mCurrentSecuritySelection = mSecurityModel.getSecurityMode();
				if (mCurrentSecuritySelection != SecurityMode.SimPin || (mCurrentSecuritySelection != SecurityMode.SimPuk)) {
					mVoiceLocked = true;
					crossSecurityLock();
				}
			} else if (INTENT_ACTION_VOICESIMLOCK.equals(action)) {
				wakeupScreen();
			}
		}
    	
    };
	 // Aurora liugj 2014-07-21 added for voice unlock screen end

	// Aurora liugj 2014-09-24 added for voice unlock sim-Lockscreen start
	private void wakeupScreen() {
    	PowerManager.WakeLock wakeLock = null;
    	try {
    		PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "voice_wakeup");
            if (!wakeLock.isHeld()) {
            	wakeLock.acquire(3000);
			}
            Toast.makeText(mContext, R.string.toast_simlock_voice, Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (wakeLock != null && wakeLock.isHeld()) {
				wakeLock.release();
                wakeLock = null;
            }
		}    	
	}
	// Aurora liugj 2014-09-24 added for voice unlock sim-Lockscreen end
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // TODO 此方法后续使用onResume代替
        KeyguardUpdateMonitor.getInstance(mContext).registerCallback(mUpdateMonitorCallbacks);
		  // Aurora liugj 2014-07-21 added for voice unlock screen start
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_ACTION_VOICEWAKEUP);
        filter.addAction(INTENT_ACTION_VOICESIMLOCK);
        mContext.registerReceiver(mVoiceLockReceiver, filter);
		  // Aurora liugj 2014-07-21 added for voice unlock screen end

		//if (mIsWindowFocus) {
        //    mNaviState = Settings.System.getInt(mContext.getContentResolver(), NAVI_KEY_HIDE, 0);
        //    Settings.System.putInt(mContext.getContentResolver(), NAVI_KEY_HIDE, 1);
        //    Log.v("xiaoyong", "mNaviState = " + mNaviState + " onAttachedToWindow");
		//}
    }

    @Override
    protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		// TODO 此方法后续使用onPause代替
		KeyguardUpdateMonitor.getInstance(mContext).removeCallback(
				mUpdateMonitorCallbacks);
		// Aurora liugj 2014-07-21 added for voice unlock screen start
		mContext.unregisterReceiver(mVoiceLockReceiver);
		// Aurora liugj 2014-07-21 added for voice unlock screen end

		// Vulcan changed it to pauseCurLockView, for new function(direct
		// unlock).
		// getSecurityLockView(mCurrentSecuritySelection).onPause();
		pauseCurLockView();

		AnimUtils.onDestroyActivity();

    }
    
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
		mIsWindowFocus = hasWindowFocus;
		Log.v("xiaoyong1", "onWindowFocusChanged mIsWindowFocus = " + mIsWindowFocus + 
			" isShowLockScreen = " + isShowLockScreen(mContext) + " mNaviState = " + mNaviState);
        Log.d(TAG, "hasWindowFocus=" + hasWindowFocus);
        /*if (hasWindowFocus) {
            Log.d("aaa", "hasWindowFocus=" + hasWindowFocus);
            setStatusBarTransparent(mContext, true);
       } else {
            if (!isTopAppToLauncher(mContext) || isShowLockScreen(mContext)) {
				Log.d("aaa", "lockScreen=" + hasWindowFocus);
                setStatusBarTransparent(mContext, false);
            }
        }*/
    }

    public void userActivity() {
        if (mViewMediatorCallback != null) {
            mViewMediatorCallback.userActivity();
        }
    }

    public void onUserActivityTimeoutChanged() {
        if (mViewMediatorCallback != null) {
            mViewMediatorCallback.onUserActivityTimeoutChanged();
        }
    }

    @Override
    public long getUserActivityTimeout() {
        // TODO 返回具体某个锁屏样式的时间
        // Currently only considering user activity timeouts needed by widgets.
        // Could also take into account longer timeouts for certain security views.
//        AuroraLog.d(TAG,"getUserActivityTimeout");
//        if (mGnKeyguardScreen != null) {
//            return mGnKeyguardScreen.getUserActivityTimeout();
//        }
//        if (mAppWidgetContainer != null) {
//            return mAppWidgetContainer.getUserActivityTimeout();
//        }
        return -1;
    }

    private KeyguardSecurityCallback mCallback = new KeyguardSecurityCallback() {

        public void userActivity(long timeout) {
            if (mViewMediatorCallback != null) {
                mViewMediatorCallback.userActivity(timeout);
            }
        }

        public void dismiss(boolean authenticated) {
        	//Vulcan changed it to showNextSecurityScreenOrFinish2 for new function(direct unlock).
            showNextSecurityScreenOrFinish2(authenticated);
        }

        public boolean isVerifyUnlockOnly() {
            return mIsVerifyUnlockOnly;
        }

        public void reportSuccessfulUnlockAttempt() {
            KeyguardUpdateMonitor.getInstance(mContext).clearFailedUnlockAttempts();
            mLockPatternUtils.reportSuccessfulPasswordAttempt();
        }

        public void reportFailedUnlockAttempt() {
            if (mCurrentSecuritySelection == SecurityMode.Biometric) {
                KeyguardUpdateMonitor.getInstance(mContext).reportFailedBiometricUnlockAttempt();
            } else {
                AuroraKeyguardHostView.this.reportFailedUnlockAttempt();
            }
        }

        public int getFailedAttempts() {
            return KeyguardUpdateMonitor.getInstance(mContext).getFailedUnlockAttempts();
        }

        @Override
        public void showBackupSecurity() {
            AuroraKeyguardHostView.this.showBackupSecurityScreen();
        }

        public void setOnDismissAction(OnDismissAction action) {
            AuroraKeyguardHostView.this.setOnDismissAction(action);
        }

        @Override
        public void setOnDismissAction(com.android.keyguard.KeyguardHostView.OnDismissAction action) {
            // TODO Auto-generated method stub
            
        }

		@Override
		public void setRunningAnim(boolean isRun) {
			isRunningAnim = isRun;
		}
        
    };

    private void showDialog(String title, String message) {
//        final AlertDialog dialog = new AlertDialog.Builder(mContext).setTitle(title).setMessage(message)
//                .setNeutralButton(com.android.internal.R.string.ok, null).create();
        final AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(mContext).setTitle(title).setMessage(message)
                .setNeutralButton(com.android.internal.R.string.ok, null).create();
        if (!(mContext instanceof Activity)) {
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        }
        dialog.show();
    }

    private void showTimeoutDialog() {
        int timeoutInSeconds = ( int ) LockPatternUtils.FAILED_ATTEMPT_TIMEOUT_MS / 1000;
        int messageId = 0;

        switch (mSecurityModel.getSecurityMode()) {
            case Pattern:
                messageId = R.string.kg_too_many_failed_pattern_attempts_dialog_message;
                break;
            case PIN:
//                messageId = R.string.kg_too_many_failed_pin_attempts_dialog_message;
                messageId = R.string.kg_too_many_failed_password_attempts_dialog_message;
                break;
            case Password:
                messageId = R.string.kg_too_many_failed_password_attempts_dialog_message;
                break;
        }

        if (messageId != 0) {
            final String message = mContext.getString(messageId, KeyguardUpdateMonitor.getInstance(mContext)
                    .getFailedUnlockAttempts(), timeoutInSeconds);
            showDialog(null, message);
        }
    }

    private void showAlmostAtWipeDialog(int attempts, int remaining) {
//        int timeoutInSeconds = (int) LockPatternUtils.FAILED_ATTEMPT_TIMEOUT_MS / 1000;
        String message = mContext.getString(R.string.kg_failed_attempts_almost_at_wipe, attempts, remaining);
        showDialog(null, message);
    }

    private void showWipeDialog(int attempts) {
        String message = mContext.getString(R.string.kg_failed_attempts_now_wiping, attempts);
        showDialog(null, message);
    }

    private void showAlmostAtAccountLoginDialog() {
        final int timeoutInSeconds = ( int ) LockPatternUtils.FAILED_ATTEMPT_TIMEOUT_MS / 1000;
        final int count = LockPatternUtils.FAILED_ATTEMPTS_BEFORE_RESET
                - LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT;
        String message = mContext.getString(R.string.kg_failed_attempts_almost_at_login, count,
                LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT, timeoutInSeconds);
        showDialog(null, message);
    }

    private void reportFailedUnlockAttempt() {
        final KeyguardUpdateMonitor monitor = KeyguardUpdateMonitor.getInstance(mContext);
        final int failedAttempts = monitor.getFailedUnlockAttempts() + 1; // +1 for this time

        AuroraLog.d(TAG, "reportFailedPatternAttempt: #" + failedAttempts);

        SecurityMode mode = mSecurityModel.getSecurityMode();
        final boolean usingPattern = mode == KeyguardSecurityModel.SecurityMode.Pattern;

        final int failedAttemptsBeforeWipe = mLockPatternUtils.getDevicePolicyManager()
                .getMaximumFailedPasswordsForWipe(null);

        final int failedAttemptWarning = LockPatternUtils.FAILED_ATTEMPTS_BEFORE_RESET
                - LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT;

        final int remainingBeforeWipe = failedAttemptsBeforeWipe > 0 ? (failedAttemptsBeforeWipe - failedAttempts)
                : Integer.MAX_VALUE; // because DPM returns 0 if no restriction

        boolean showTimeout = false;
        if (remainingBeforeWipe < LockPatternUtils.FAILED_ATTEMPTS_BEFORE_WIPE_GRACE) {
            // If we reach this code, it means the user has installed a DevicePolicyManager
            // that requests device wipe after N attempts. Once we get below the grace
            // period, we'll post this dialog every time as a clear warning until the
            // bombshell hits and the device is wiped.
            if (remainingBeforeWipe > 0) {
                showAlmostAtWipeDialog(failedAttempts, remainingBeforeWipe);
            } else {
                // Too many attempts. The device will be wiped shortly.
                AuroraLog.d(TAG, "Too many unlock attempts; device will be wiped!");
                showWipeDialog(failedAttempts);
            }
        } else {
            showTimeout = (failedAttempts > 0 && (failedAttempts % LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT) == 0);
            if (usingPattern && mEnableFallback) {
                if (failedAttempts == failedAttemptWarning) {
                    showAlmostAtAccountLoginDialog();
                    showTimeout = false; // don't show both dialogs
                } else if (failedAttempts >= LockPatternUtils.FAILED_ATTEMPTS_BEFORE_RESET) {
                    mLockPatternUtils.setPermanentlyLocked(true);
                    showSecurityScreen(SecurityMode.Account);
                    // don't show timeout dialog because we show account unlock screen next
                    showTimeout = false;
                }
            }
        }
        monitor.reportFailedUnlockAttempt();
        mLockPatternUtils.reportFailedPasswordAttempt();
        if (showTimeout) {
            showTimeoutDialog();
        }
    }
    
    /**
     * Vulcan created this method in 2014-9-11
     * @param turningOff
     */
    void showPrimarySecurityLockScreen2(boolean turningOff) {
    	Log.d("vulcan-direct","showPrimarySecurityLockScreen2: SecurityModeIsDirect = " + SecurityModeIsDirect());
    	if(SecurityModeIsDirect()) {
			//showPrimarySecurityLockScreen(turningOff);
			showNextSecurityScreenOrFinish2(true);
		} else {
			showPrimarySecurityLockScreen(turningOff);
		}
    }

    /**
     * Shows the primary security screen for the user. This will be either the multi-selector or the user's
     * security method.
     * 
     * @param turningOff
     *            true if the device is being turned off
     */
    void showPrimarySecurityLockScreen(boolean turningOff) {
        SecurityMode securityMode = mSecurityModel.getSecurityMode();
        Log.d("vulcan-direct","showPrimarySecurityLockScreen: securityMode = " + securityMode);
        AuroraLog.d(TAG, "showPrimarySecurityScreen(turningOff=" + turningOff + ")");
        if (!turningOff && KeyguardUpdateMonitor.getInstance(mContext).isAlternateUnlockEnabled()) {
            // If we're not turning off, then allow biometric alternate.
            // We'll reload it when the device comes back on.
            securityMode = mSecurityModel.getAlternateFor(securityMode);
        }
        showSecurityScreen(securityMode);
    }

    /**
     * Shows the backup security screen for the current security mode. This could be used for password
     * recovery screens but is currently only used for pattern unlock to show the account unlock screen and
     * biometric unlock to show the user's normal unlock.
     */
    private void showBackupSecurityScreen() {
        AuroraLog.d(TAG, "showBackupSecurity()");
        SecurityMode backup = mSecurityModel.getBackupSecurityMode(mCurrentSecuritySelection);
        showSecurityScreen(backup);
    }

    public boolean showNextSecurityScreenIfPresent() {
        SecurityMode securityMode = mSecurityModel.getSecurityMode();
        // Allow an alternate, such as biometric unlock
        securityMode = mSecurityModel.getAlternateFor(securityMode);
        if (SecurityMode.None == securityMode) {
            return false;
        } else {
            showSecurityScreen(securityMode); // switch to the alternate security view
            return true;
        }
    }

	 // Aurora liugj 2014-07-21 added for voice unlock screen start
    private void crossSecurityLock() {
    	//mCurrentSecuritySelection = mSecurityModel.getSecurityMode();
    	
    	//Vulcan changed it to showNextSecurityScreenOrFinish2 for new function(direct unlock).
    	showNextSecurityScreenOrFinish2(true);
    }
    // Aurora liugj 2014-07-21 added for voice unlock screen end
    
    
    /**
     * Vulcan created this method in 2014-9-11
     */
    private void finishLockScreen() {
            // If the alternate unlock was suppressed, it can now be safely
            // enabled because the user has left keyguard.
            KeyguardUpdateMonitor.getInstance(mContext).setAlternateUnlockEnabled(true);

            // If there's a pending runnable because the user interacted with a widget
            // and we're leaving keyguard, then run it.
            boolean deferKeyguardDone = false;
            if (mDismissAction != null) {
                deferKeyguardDone = mDismissAction.onDismiss();
                mDismissAction = null;
            }
            if (mViewMediatorCallback != null) {
                if (deferKeyguardDone) {
                    mViewMediatorCallback.keyguardDonePending();
                } else {
                    mViewMediatorCallback.keyguardDone(true);
                }
            }

			  if (!mVoiceLocked) {
            		Intent intent = new Intent();
            		intent.setAction("com.aurora.lancher.start.anim");
            		mContext.sendBroadcast(intent);
			  }
    	return;
    }
    
    /**
     * Vulcan created this method in 2014-9-11
     * @param authenticated
     */
    private void showNextSecurityScreenOrFinish2(boolean authenticated) {
    	if(SecurityModeIsDirect()) {
    		finishLockScreen();
    		//showNextSecurityScreenOrFinish(authenticated);
    	}
    	else {
    		showNextSecurityScreenOrFinish(authenticated);
    	}
    }
    private void showNextSecurityScreenOrFinish(boolean authenticated) {
        Log.d("vulcan-direct", "showNextSecurityScreenOrFinish(" + authenticated + ")" + "mIsUnlock=" + mIsUnlock + ",mCurrentSecuritySelection=" + mCurrentSecuritySelection);
        //Log.d("vulcan-direct", "stack = " + Trace.StackToString(new Throwable()));
        boolean finish = false;
        if (SecurityMode.None == mCurrentSecuritySelection) {
            SecurityMode securityMode = mSecurityModel.getSecurityMode();
            // Allow an alternate, such as biometric unlock
            securityMode = mSecurityModel.getAlternateFor(securityMode);
            if (SecurityMode.None == securityMode) {
                finish = true; // no security required
            } else {
                mIsUnlock = true;
                //Aurora <zhang_xin> <2013-9-26> modify for 双重锁屏 begin
//                showSecurityScreen(securityMode); // switch to the alternate security view
                showSecurityLock(securityMode); // switch to the alternate security view
                //Aurora <zhang_xin> <2013-9-26> modify for 双重锁屏 end
                setSystemUiClockEnabled(true);
            }
        } else if (authenticated) {
            switch (mCurrentSecuritySelection) {
                case Pattern:
                case Password:
                case PIN:
                case Account:
                case Biometric:
                    finish = true;
                    break;

                case SimPin:
                case SimPuk:
                    // Shortcut for SIM PIN/PUK to go to directly to user's security screen or home
                    SecurityMode securityMode = mSecurityModel.getSecurityMode();
                    if (securityMode != SecurityMode.None) {
                        mIsUnlock = true;
                        showSecurityLock(securityMode);
                    } else {
                        finish = true;
                    }
                    break;

                default:
                    Log.v(TAG, "Bad security screen " + mCurrentSecuritySelection + ", fail safe");
                    showPrimarySecurityLockScreen(false);
                    break;
            }
        } else {
            showPrimarySecurityLockScreen(false);
        }
        if (finish) {
            // If the alternate unlock was suppressed, it can now be safely
            // enabled because the user has left keyguard.
            KeyguardUpdateMonitor.getInstance(mContext).setAlternateUnlockEnabled(true);

            // If there's a pending runnable because the user interacted with a widget
            // and we're leaving keyguard, then run it.
            boolean deferKeyguardDone = false;
            if (mDismissAction != null) {
                deferKeyguardDone = mDismissAction.onDismiss();
                mDismissAction = null;
            }
            if (mViewMediatorCallback != null) {
                if (deferKeyguardDone) {
                    mViewMediatorCallback.keyguardDonePending();
                } else {
//                    if (!isTopAppToLauncher(mContext)) {
//                        setStatusBarTransparent(mContext, false);
//                    }
                    mViewMediatorCallback.keyguardDone(true);
                }
            }
			  // Aurora liugj 2014-07-21 modified for voice unlock screen start
			  // Aurora liugj 2014-12-08 added for bug-10381 start
			  if (!mVoiceLocked && isBootCompleted) {
			  // Aurora liugj 2014-12-08 added for bug-10381 end
            		Intent intent = new Intent();
            		intent.setAction("com.aurora.lancher.start.anim");
            		mContext.sendBroadcast(intent);
			  }
			  // Aurora liugj 2014-07-21 modified for voice unlock screen end
        }
    }

    // Used to ignore callbacks from methods that are no longer current (e.g. face unlock).
    // This avoids unwanted asynchronous events from messing with the state.
    private KeyguardSecurityCallback mNullCallback = new KeyguardSecurityCallback() {

        @Override
        public void userActivity(long timeout) {
        }

        @Override
        public void showBackupSecurity() {
        }

        public void setOnDismissAction(OnDismissAction action) {

        }

        @Override
        public void reportSuccessfulUnlockAttempt() {
        }

        @Override
        public void reportFailedUnlockAttempt() {
        }

        @Override
        public boolean isVerifyUnlockOnly() {
            return false;
        }

        @Override
        public int getFailedAttempts() {
            return 0;
        }

        @Override
        public void dismiss(boolean securityVerified) {
        }

        @Override
        public void setOnDismissAction(com.android.keyguard.KeyguardHostView.OnDismissAction action) {
            // TODO Auto-generated method stub
            
        }

		@Override
		public void setRunningAnim(boolean isRun) {
			// TODO Auto-generated method stub
			
		}
    };

    protected boolean mShowSecurityWhenReturn;

//    @Override
    public void reset() {
        mIsVerifyUnlockOnly = false;
        // TODO 恢复默认widget页面
//        mAppWidgetContainer.setCurrentPage(getWidgetPosition(R.id.keyguard_status_view));
    }

    /**
     * Sets an action to perform when keyguard is dismissed.
     * 
     * @param action
     */
    protected void setOnDismissAction(OnDismissAction action) {
        mDismissAction = action;
    }

    /**
     * @deprecated Use {@link #getSecurityLockView} instead.
     */
    @Deprecated
    private KeyguardSecurityView getSecurityView(SecurityMode securityMode) {
        final int securityViewIdForMode = getSecurityViewIdForMode(securityMode);
        KeyguardSecurityView view = null;
        // TODO 返回有几层锁屏
        final int children = mSecurityViewContainer.getChildCount();
        for (int child = 0; child < children; child++) {
            if (mSecurityViewContainer.getChildAt(child).getId() == securityViewIdForMode) {
                view = (( KeyguardSecurityView ) mSecurityViewContainer.getChildAt(child));
                break;
            }
        }
        int layoutId = getLayoutIdFor(securityMode);
        if (view == null && layoutId != 0) {
            final LayoutInflater inflater = LayoutInflater.from(mContext);
            AuroraLog.d(TAG, "inflating id = " + layoutId);
            View v = inflater.inflate(layoutId, mSecurityViewContainer, false);
            mSecurityViewContainer.addView(v);
            updateSecurityView(v);
            view = ( KeyguardSecurityView ) v;
        }

        final int selectorChildren = mSecurityViewContainer.getChildCount();
        // AURORA-START::ALPHA::modify::zhang_xin::2013-9-9
        // R.layout.keyguard_selector_view
        boolean isSelector = false;
        for (int i = 0; i < selectorChildren; i++) {
            if (mSecurityViewContainer.getChildAt(i).getId() == R.id.selector_view) {
                isSelector = true;
                break;
            }
        }

        if (selectorChildren > 0 && !isSelector) {
            int selectorId = getLayoutIdFor(SecurityMode.None);
            final LayoutInflater inflater = LayoutInflater.from(mContext);
            if (DEBUG)
                Log.v(TAG, "zhang_xin inflating id = " + selectorId);
            View v = inflater.inflate(selectorId, mSecurityViewContainer, false);
            mSecurityViewContainer.addView(v);
            updateSecurityView(v);
            view = ( KeyguardSecurityView ) v;
        }
        // AURORA-END::ALPHA::modify::zhang_xin::2013-9-9

        /*if (view instanceof KeyguardSelectorView) {
            KeyguardSelectorView selectorView = (KeyguardSelectorView) view;
            View carrierText = selectorView.findViewById(R.id.keyguard_selector_fade_container);
            selectorView.setCarrierArea(carrierText);
        }*/

        return view;
    }

    /**
     * Switches to the given security view unless it's already being shown, in which case this is a no-op.
     * @deprecated Use {@link #showSecurityLock} instead.
     * @param securityMode
     */
    @Deprecated
    private void showSecurityScreen(SecurityMode securityMode) {
        AuroraLog.d(TAG, "showSecurityScreen(" + securityMode + ")");

        /*if (securityMode == mCurrentSecuritySelection) return;

        KeyguardSecurityView oldView = getSecurityView(mCurrentSecuritySelection);
        KeyguardSecurityView newView = getSecurityView(securityMode);

        // Enter full screen mode if we're in SIM or Account screen
        boolean fullScreenEnabled = getResources().getBoolean(
                R.bool.kg_sim_puk_account_full_screen);
        boolean isSimOrAccount = securityMode == SecurityMode.SimPin
                || securityMode == SecurityMode.SimPuk
                || securityMode == SecurityMode.Account;
        //        mAppWidgetContainer.setVisibility(
        //                isSimOrAccount && fullScreenEnabled ? View.GONE : View.VISIBLE);

        //        if (mSlidingChallengeLayout != null) {
        //            mSlidingChallengeLayout.setChallengeInteractive(!fullScreenEnabled);
        //        }

        // Emulate Activity life cycle
        if (oldView != null) {
            oldView.onPause();
            oldView.setKeyguardCallback(mNullCallback); // ignore requests from old view
        }
        newView.onResume(KeyguardSecurityView.VIEW_REVEALED);
        newView.setKeyguardCallback(mCallback);

        final boolean needsInput = newView.needsInput();
        if (mViewMediatorCallback != null) {
            mViewMediatorCallback.setNeedsInput(needsInput);
        }

        // Find and show this child.
        final int childCount = mSecurityViewContainer.getChildCount();

        //        mSecurityViewContainer.setInAnimation(
        //                AnimationUtils.loadAnimation(mContext, R.anim.keyguard_security_fade_in));
        //        mSecurityViewContainer.setOutAnimation(
        //                AnimationUtils.loadAnimation(mContext, R.anim.keyguard_security_fade_out));
        final int securityViewIdForMode = getSecurityViewIdForMode(securityMode);
        for (int i = 0; i < childCount; i++) {
            if (mSecurityViewContainer.getChildAt(i).getId() == securityViewIdForMode) {
                mSecurityViewContainer.setDisplayedChild(i);
                break;
            }
        }

        if (securityMode == SecurityMode.None) {
            // Discard current runnable if we're switching back to the selector view
        //            setOnDismissAction(null);
        }
        if (securityMode == SecurityMode.Account && !mLockPatternUtils.isPermanentlyLocked()) {
            // we're showing account as a backup, provide a way to get back to primary
            setBackButtonEnabled(true);
        }
        mCurrentSecuritySelection = securityMode;*/
        showSecurityLock(securityMode);
    }

    private boolean mIsUnlock = false;

    private void showSecurityLock(SecurityMode securityMode) {
    	
    	Log.d("vulcan-direct",String.format("showSecurityLock: mIsUnlock = %b",mIsUnlock));
    	Log.d("vulcan-direct",String.format("showSecurityLock: stack = %s",Trace.StackToString(new Throwable())));
    	Log.d("vulcan-direct2", "showSecurityScreen:  securityMode= " + securityMode);
    	//Log.d("vulcan-direct2", "showSecurityScreen:  stack= " + Trace.StackToString(new Throwable()));

        if (securityMode == mCurrentSecuritySelection) {
            return;
        }
        AuroraLog.d(TAG, "mIsUnlock=" + mIsUnlock);

        KeyguardSecurityView oldView = null;
        KeyguardSecurityView newView = null;

        if (mIsUnlock) {
            oldView = getSecurityLockView(mCurrentSecuritySelection);
            newView = getSecurityLockView(securityMode);

            // Emulate Activity life cycle
            if (oldView != null) {
                oldView.onPause();
                oldView.setKeyguardCallback(mNullCallback); // ignore requests from old view
                oldView.playDisAppearAnim();
            }
            newView.onResume(KeyguardSecurityView.VIEW_REVEALED);
            newView.setKeyguardCallback(mCallback);
            newView.playAppearAnim();

            final boolean needsInput = newView.needsInput();
            if (mViewMediatorCallback != null) {
                mViewMediatorCallback.setNeedsInput(needsInput);
            }

            // Find and show this child.
            final int childCount = mSecurityViewContainer.getChildCount();

            final int securityViewIdForMode = getSecurityViewIdForMode(securityMode);
            for (int i = 0; i < childCount; i++) {
                if (mSecurityViewContainer.getChildAt(i).getId() == securityViewIdForMode) {
                    mSecurityViewContainer.setDisplayedChild(i);
                    break;
                }
            }
            if (securityMode == SecurityMode.Account && !mLockPatternUtils.isPermanentlyLocked()) {
                // we're showing account as a backup, provide a way to get back to primary
                setBackButtonEnabled(true);
            }
            mCurrentSecuritySelection = securityMode;
//            Bitmap bitmap = LockScreenBgUtils.getInstance().getLockScreenBlurBg(mWallPaper);
//            mWallPaper.setBackground(new BitmapDrawable(bitmap));
        } else {
            oldView = getSecurityLockView(mCurrentSecuritySelection);
            newView = getSecurityLockView(securityMode);

            Log.d("vulcan-direct",String.format("showSecurityLock: oldView = %s,newView = %s",oldView,newView));
            AuroraLog.d(TAG, "oldView = " + oldView + ",newView = " + newView);

            if (oldView != null) {
                if (oldView instanceof AuroraSelectorView) {
                    oldView.onResume(KeyguardSecurityView.VIEW_REVEALED);
                    oldView.setKeyguardCallback(mCallback);
                } else {
                    oldView.onPause();
                    oldView.setKeyguardCallback(mNullCallback);
                }
            }

            if (newView instanceof AuroraSelectorView) {
                newView.onResume(KeyguardSecurityView.VIEW_REVEALED);
                newView.setKeyguardCallback(mCallback);
            } else {
                newView.onPause();
                newView.setKeyguardCallback(mNullCallback);
            }

//            for (int i = 0; i < mSecurityViewContainer.getChildCount(); i++) {
//                mSecurityViewContainer.getChildAt(i).setVisibility(View.VISIBLE);
//            }
//            View view = mSecurityViewContainer.findViewById(R.id.lock_screen_alpha_bg);
//            if (view != null) {
//                mSecurityViewContainer.bringChildToFront(view);
//            }
            View selectorView = mSecurityViewContainer.findViewById(R.id.selector_view);
//            if (selectorView != null) {
//                mSecurityViewContainer.bringChildToFront(selectorView);
//            }
            mSecurityViewContainer.setDisplayedChild(mSecurityViewContainer.indexOfChild(selectorView));

            AuroraLog.d(TAG, "mSecurityViewContainer=" + mSecurityViewContainer.getDisplayedChild());
            mCurrentSecuritySelection = SecurityMode.None;
//            Bitmap bitmap = LockScreenBgUtils.getInstance().getLockScreenBg(mWallPaper);
//            mWallPaper.setBackground(new BitmapDrawable(bitmap));
        }
    }

    private KeyguardSecurityView getSecurityLockView(SecurityMode securityMode) {
        final int securityViewIdForMode = getSecurityViewIdForMode(securityMode);
        KeyguardSecurityView view = null;
        final int children = mSecurityViewContainer.getChildCount();
        for (int child = 0; child < children; child++) {
            if (mSecurityViewContainer.getChildAt(child).getId() == securityViewIdForMode) {
                view = (( KeyguardSecurityView ) mSecurityViewContainer.getChildAt(child));
                break;
            }
        }
        if (DEBUG)
            Log.v(TAG, "mSecurityViewContainer children = " + children + ",securityMode=" +securityMode);
        int layoutId = getLayoutIdFor(securityMode);
        if (view == null && layoutId != 0) {
            final LayoutInflater inflater = LayoutInflater.from(mContext);
            if (DEBUG)
                Log.v(TAG, "inflating id = " + layoutId);
            View v = inflater.inflate(layoutId, mSecurityViewContainer, false);
            mSecurityViewContainer.addView(v);
            updateSecurityView(v);
            view = ( KeyguardSecurityView ) v;

            if (securityMode == SecurityMode.Password || securityMode == SecurityMode.PIN
                    || securityMode == SecurityMode.Pattern || securityMode == securityMode.SimPin
                    || securityMode == securityMode.SimPuk) {
                int selectorLayoutId = getLayoutIdFor(SecurityMode.None);
                View selectorView = mSecurityViewContainer.findViewById(R.id.selector_view);
                if (DEBUG)
                    Log.v(TAG, "selectorLayoutId = " + selectorLayoutId);
                if (selectorView == null) {
                    selectorView = inflater.inflate(selectorLayoutId, mSecurityViewContainer, false);
                    mSecurityViewContainer.addView(selectorView);
                }
                updateSecurityView(selectorView);
                view = ( KeyguardSecurityView ) selectorView;
            }
        }

//        if (view instanceof AuroraSelectorView) {
//            AuroraSelectorView selectorView = (AuroraSelectorView) view;
//            View carrierText = selectorView.findViewById(R.id.keyguard_selector_fade_container);
//            selectorView.setCarrierArea(carrierText);
//        }

        return view;
    }

	 // Aurora liugj 2014-09-16 added for reset lockpaper alarm start
    private void sendResetAlarmBroadcast(Context context) {
    	Intent intent = new Intent();
    	intent.putExtra("pkg", "com.aurora.change");
		intent.setAction(ACTION_RESET_ALARM);
		intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		context.sendBroadcast(intent);
		Log.d(LockScreenBgUtils.LOCK_TAG, "----sendResetAlarmBroadcast----");
	}
	// Aurora liugj 2014-09-16 added for reset lockpaper alarm end
    
    /**
     * vulcan created this method in 2014-9-12
     * @param reason
     */
    private void resumeCurLockView(int reason) {
    	KeyguardSecurityView ksv = getSecurityLockView(mCurrentSecuritySelection);
    	if(ksv != null) {
			// Aurora liugj 2014-09-16 added for reset lockpaper alarm start
			// Aurora liugj 2014-12-08 added for bug-10381 start
			if(isBootCompleted) {
			// Aurora liugj 2014-12-08 added for bug-10381 end
				sendResetAlarmBroadcast(mContext);
			}
			// Aurora liugj 2014-09-16 added for reset lockpaper alarm end
    		ksv.onResume(reason);
    	}
    }
    
    /**
     * vulcan created this method in 2014-9-12
     * @param reason
     */
    private void pauseCurLockView() {
    	KeyguardSecurityView ksv = getSecurityLockView(mCurrentSecuritySelection);
    	if(ksv != null) {
    		ksv.onPause();
    	}
    }

    @Override
    public void onScreenTurnedOn() {
		AuroraLog.d(TAG,
				"screen on, instance " + Integer.toHexString(hashCode()));
		// Aurora liugj 2014-12-08 added for bug-10381 start
		isBootCompleted = SystemProperties.get("sys.boot_completed", "0").equals("1") ? true : false;
       Log.d(TAG, "===onScreenTurnedOn===isBootCompleted="+isBootCompleted);
		// Aurora liugj 2014-12-08 added for bug-10381 end

		// Vulcan changed it to showPrimarySecurityLockScreen2 for new
		// function(direct unlock).
		showPrimarySecurityLockScreen2(false);

		// Vulcan edited it in 2014-9-12
		// Aurora <zhang_xin> <2013-9-26> modify for 双重锁屏 begin
		// getSecurityView(mCurrentSecuritySelection).onResume(KeyguardSecurityView.SCREEN_ON);
		// getSecurityLockView(mCurrentSecuritySelection).onResume(KeyguardSecurityView.SCREEN_ON);
		// Aurora <zhang_xin> <2013-9-26> modify for 双重锁屏 end
		resumeCurLockView(KeyguardSecurityView.SCREEN_ON);

		/*if (isSecure()) {
			mWallPaperBg.setVisibility(View.VISIBLE);
			LockScreenBgUtils.getInstance().setViewBg(mWallPaper);
		}*/

		// This is a an attempt to fix bug 7137389 where the device comes back
		// on but the entire
		// layout is blank but forcing a layout causes it to reappear (e.g. with
		// with
		// hierarchyviewer).
		requestLayout();
		setStatusBarTransparent(mContext, true);
		// if (mViewStateManager != null) {
		// mViewStateManager.showUsabilityHints();
		// }
		requestFocus();
    }

    @Override
    public void onScreenTurnedOff() {
        AuroraLog.d(
                TAG,
                String.format("screen off, instance %s at %s", Integer.toHexString(hashCode()),
                        SystemClock.uptimeMillis()));
        // Once the screen turns off, we no longer consider this to be first boot and we want the
        // biometric unlock to start next time keyguard is shown.
        KeyguardUpdateMonitor.getInstance(mContext).setAlternateUnlockEnabled(true);
        //Aurora <zhang_xin> <2013-9-26> modify for 双重锁屏,恢复默认 begin
        if (isSecure()) {
            mIsUnlock = false;
            mCurrentSecuritySelection = SecurityMode.None;
            setSystemUiClockEnabled(false);
        }
  
		// Vulcan changed it to showPrimarySecurityLockScreen2 for new
		// function(direct unlock).
        Log.d("vulcan-direct","onScreenTurnedOff:");
        showPrimarySecurityLockScreen2(true);
        
//        getSecurityView(mCurrentSecuritySelection).onPause();
        //getSecurityLockView(mCurrentSecuritySelection).onPause();
        //Aurora <zhang_xin> <2013-9-26> modify for 双重锁屏,恢复默认 end
        clearFocus();
    }

    @Override
    public void show() {
        AuroraLog.d(TAG, "show()");
        
		// Vulcan changed it to showPrimarySecurityLockScreen2 for new
		// function(direct unlock).
        showPrimarySecurityLockScreen2(false);
    }
    
    
    /**
     * Vulcan created this method in 2014-9-11
     * @return
     */
    private boolean SecurityModeIsDirect() {
    	 SecurityMode mode = mSecurityModel.getSecurityMode();
    	 return mode == SecurityMode.Direct;
    }

    private boolean isSecure() {
        SecurityMode mode = mSecurityModel.getSecurityMode();
        switch (mode) {
            case Pattern:
                return mLockPatternUtils.isLockPatternEnabled();
            case Password:
            case PIN:
                return mLockPatternUtils.isLockPasswordEnabled();
            case SimPin:
            case SimPuk:
            case Account:
                return true;
            case None:
            case Direct:
                return false;
            default:
                throw new IllegalStateException("Unknown security mode " + mode);
        }
    }

	// Aurora <liugj> <2014-06-27> added for screenoff gesture begin
    @Override
    public void wakeWhenReadyTq(int keyCode) {
        AuroraLog.d(TAG, "onWakeKey = " + keyCode);
        if (keyCode == KeyEvent.KEYCODE_MENU && isSecure()) {
            AuroraLog.d(TAG, "switching screens to unlock screen because wake key was MENU");
            showSecurityScreen(SecurityMode.None);
        } else {
            AuroraLog.d(TAG, "poking wake lock immediately, keyCode=" + keyCode);
        }
//        if (keyCode == KeyEvent.KEYCODE_GESTURE_U_RIGHT) {
		  // Aurora <liugj> <2014-11-05> modified for U2kk KEYCODE begin
        if (keyCode == 255 || (SystemProperties.get("ro.product.model", "IUNI U3").equals("IUNI U810") && keyCode == 229)) {
		  // Aurora <liugj> <2014-11-05> modified for U2kk KEYCODE end
            try {
                Intent intent = new Intent();
                if (isSecure()) {
                    intent.setAction(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE);
//                    intent.setClassName("com.android.camera", "com.android.camera.SecureCameraActivity");
                } else {
                    mCallback.dismiss(false);
//                    intent.setAction(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
//                    intent.setClassName("com.android.camera", "com.android.camera.CameraActivity");
                    intent.setClassName("com.android.camera", "com.android.camera.ColdCaptureCameraActivity");
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("cold_screen_capture", true);
                mContext.startActivity(intent);
            } catch (Exception e) {
                try {
                    Intent intent = new Intent();
                    if (isSecure()) {
                        intent.setAction(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE);
                    } else {
                        mCallback.dismiss(false);
                        intent.setAction(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("cold_screen_capture", true);
                    mContext.startActivity(intent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }else if (keyCode == 257) {
			if (StatusView.isPlayingMusic) {
				Intent intent = new Intent();
				intent.setAction(AURORA_SERVICECMD);
				intent.putExtra(AURORA_CMDNAME, AURORA_CMDNEXT);
				mContext.sendBroadcast(intent);
			}
		}else if (keyCode == 258) {
			if (StatusView.isPlayingMusic) {
				Intent intent = new Intent();
				intent.setAction(AURORA_SERVICECMD);
				intent.putExtra(AURORA_CMDNAME, AURORA_CMDPREV);
				mContext.sendBroadcast(intent);
			}
		// Aurora liugj 2014-12-08 added for bug-10381 start
		}else if (keyCode == 259 && isBootCompleted) {
		// Aurora liugj 2014-12-08 added for bug-10381 end
			Intent intent = new Intent();
			// Aurora liugj 2014-11-25 modified for music play action change start
			//intent.setAction(AURORA_SERVICECMD);
			//intent.putExtra(AURORA_CMDNAME, AURORA_CMDTOGGLEPAUSE);
			intent.setAction(AURORA_MUSIC_ACTION);
			// Aurora liugj 2014-11-25 modified for music play action change end
			mContext.sendBroadcast(intent);
		}
//        if (mViewMediatorCallback != null) {
//            mViewMediatorCallback.wakeUp();
//        }
    }
	// Aurora <liugj> <2014-06-27> added for screenoff gesture end

    @Override
    public void verifyUnlock() {
        SecurityMode securityMode = mSecurityModel.getSecurityMode();
        if (securityMode == KeyguardSecurityModel.SecurityMode.None) {
            if (mViewMediatorCallback != null) {
                mViewMediatorCallback.keyguardDone(true);
            }
        } else if (securityMode != KeyguardSecurityModel.SecurityMode.Pattern
                && securityMode != KeyguardSecurityModel.SecurityMode.PIN
                && securityMode != KeyguardSecurityModel.SecurityMode.Password) {
            // can only verify unlock when in pattern/password mode
            if (mViewMediatorCallback != null) {
                mViewMediatorCallback.keyguardDone(false);
            }
        } else {
            // otherwise, go to the unlock screen, see if they can verify it
            mIsVerifyUnlockOnly = true;
            showSecurityScreen(securityMode);
        }
    }

    private int getSecurityViewIdForMode(SecurityMode securityMode) {
        switch (securityMode) {
            case None:
                return R.id.selector_view;
            case Pattern:
                return R.id.keyguard_pattern_view;
            case PIN:
//                return R.id.keyguard_pin_view;
                return R.id.keyguard_digit_view;
            case Password:
                return R.id.keyguard_password_view;
//            case Biometric: return R.id.keyguard_face_unlock_view;
//            case Account: return R.id.keyguard_account_view;
            case SimPin:
                if (KeyguardUpdateMonitor.sIsMultiSimEnabled) {
                    return R.id.msim_keyguard_sim_pin_view;
                }
                return R.id.keyguard_sim_pin_view;
            case SimPuk:
                if (KeyguardUpdateMonitor.sIsMultiSimEnabled) {
                    return R.id.msim_keyguard_sim_puk_view;
                }
                return R.id.keyguard_sim_puk_view;
        }
        return 0;
    }

    private int getLayoutIdFor(SecurityMode securityMode) {
        switch (securityMode) {
            case None:
                return R.layout.selector_view;
            case Pattern:
                return R.layout.keyguard_pattern_view;
            case PIN:
//                return R.layout.security_pin_view;
                return R.layout.security_digit_view;
            case Password:
                return R.layout.keyguard_password_view;
//            case Biometric: return R.layout.keyguard_face_unlock_view;
//            case Account: return R.layout.keyguard_account_view;
            case SimPin:
                if (KeyguardUpdateMonitor.sIsMultiSimEnabled) {
                    return R.layout.msim_keyguard_sim_pin_view;
                }
                return R.layout.keyguard_sim_pin_view;
            case SimPuk:
                if (KeyguardUpdateMonitor.sIsMultiSimEnabled) {
                    return R.layout.msim_keyguard_sim_puk_view;
                }
                return R.layout.keyguard_sim_puk_view;
            default:
                return 0;
        }
    }

    @Override
    public void cleanUp() {
        // Make sure we let go of all widgets and their package contexts promptly. If we don't do
        // this, and the associated application is uninstalled, it can cause a soft reboot.
        /*int count = mAppWidgetContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            KeyguardWidgetFrame frame = mAppWidgetContainer.getWidgetPageAt(i);
            frame.removeAllViews();
        }*/
    }

    /**
     * In general, we enable unlocking the insecure keyguard with the menu key. However, there are some cases
     * where we wish to disable it, notably when the menu button placement or technology is prone to false
     * positives.
     * 
     * @return true if the menu key should be enabled
     */
    private static final String ENABLE_MENU_KEY_FILE = "/data/local/enable_menu_key";

    private boolean shouldEnableMenuKey() {
        final Resources res = getResources();
        final boolean configDisabled = res
                .getBoolean(com.android.internal.R.bool.config_disableMenuKeyInLockScreen);
        final boolean isTestHarness = ActivityManager.isRunningInTestHarness();
        final boolean fileOverride = (new File(ENABLE_MENU_KEY_FILE)).exists();
//        return !configDisabled || isTestHarness || fileOverride;
        return false;
    }

    public boolean handleMenuKey() {
        // The following enables the MENU key to work for testing automation
        if (shouldEnableMenuKey()) {
        	
    		// Vulcan changed it to showNextSecurityScreenOrFinish2 for new
    		// function(direct unlock).
            showNextSecurityScreenOrFinish2(false);
            return true;
        }
        return false;
    }

    public boolean handleBackKey() {
        if (mCurrentSecuritySelection == SecurityMode.Account) {
            // go back to primary screen and re-disable back
            setBackButtonEnabled(false);
            
    		// Vulcan changed it to showPrimarySecurityLockScreen2 for new
    		// function(direct unlock).
            showPrimarySecurityLockScreen2(false /*turningOff*/);
            return true;
		  // Aurora liugj 2015-01-06 added for bug-10669 start
        } else if (!isRunningAnim && (mCurrentSecuritySelection == SecurityMode.PIN || mCurrentSecuritySelection == SecurityMode.Pattern)) {
			showSecurityLock(SecurityMode.None);
			return true;
		  // Aurora liugj 2015-01-06 added for bug-10669 end
		}
        if (mCurrentSecuritySelection != SecurityMode.None) {
            mCallback.dismiss(false);
            return true;
        }
        return false;
    }

    /**
     * Dismisses the keyguard by going to the next screen or making it gone.
     */
    public void dismiss() {
    	
    	
		// Vulcan changed it to showNextSecurityScreenOrFinish2 for new
		// function(direct unlock).
        showNextSecurityScreenOrFinish2(false);
    }

    public void showAssistant() {

    }

    /*@Override
    public boolean isAlarmUnlockScreen() {
        // TODO Auto-generated method stub
        return false;
    }*/

    private void setStatusBarTransparent(Context context, boolean enable) {
    	if (Build.VERSION.SDK_INT > 18) {
    		NotificationManager notificationManager = ( NotificationManager ) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification.Builder builder = new Notification.Builder(context);
            builder.setSmallIcon(com.aurora.R.drawable.aurora_switch_on);
            String tag = AURORA_WHITE_IMMERSE;
            try {
            	Context changeContext = mContext.createPackageContext("com.aurora.change", Context.CONTEXT_IGNORE_SECURITY);
                SharedPreferences sp = changeContext.getSharedPreferences("aurora_change", Context.MODE_MULTI_PROCESS | Context.MODE_WORLD_READABLE);
                String defaultGroup = Build.MODEL.equals("IUNI i1") ? BLACKSTAYLE_LOCKPAPER_GROUP_2 : DEFAULT_LOCKPAPER_GROUP;
                String currentGroup = sp.getString("current_lockpaper_group", defaultGroup);
                
                //shigq add start
				String currentGroupStatusBlack = sp.getString("current_lockpaper_group_status_black", null);
                Log.d("Wallpaper_DEBUG", "current_lockpaper_group_time_black2222222222----------= "+currentGroupStatusBlack);
                if (currentGroupStatusBlack != null) {
					if ("true".equals(currentGroupStatusBlack)) {
						tag = AURORA_BLACK_IMMERSE;
					} else {
						tag = AURORA_WHITE_IMMERSE;
					}
                	
				} else {
					if (currentGroup.equals(BLACKSTAYLE_LOCKPAPER_GROUP_1) || currentGroup.equals(BLACKSTAYLE_LOCKPAPER_GROUP_2) && enable) {
	                	tag = AURORA_BLACK_IMMERSE;
	                }else {
	                	tag = AURORA_WHITE_IMMERSE;
					}
				}
                
                Log.d("liugj3", TAG+"-----onFinishInflate---currentGroup="+currentGroup);
                /*if (currentGroup.equals(BLACKSTAYLE_LOCKPAPER_GROUP_1) || currentGroup.equals(BLACKSTAYLE_LOCKPAPER_GROUP_2) && enable) {
                	tag = AURORA_BLACK_IMMERSE;
                }else {
                	tag = AURORA_WHITE_IMMERSE;
				}*/
                //shigq add end
                
            }catch(NameNotFoundException e) {
            	
            }
            notificationManager.notify(tag, 0, builder.build());
		}

    }

    private boolean isTopAppToLauncher(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> list = activityManager.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            RunningTaskInfo info = list.get(0);
            String pkclN = info.topActivity.getClassName();
            if (pkclN.equals("com.aurora.launcher.Launcher")) {
                Log.d(TAG, "isTopAppToLauncher true");
                return true;
            }
        }
        Log.d(TAG, "isTopAppToLauncher false");
        return false;
    }
    
    private boolean isShowLockScreen(Context context){
        KeyguardManager manager = ( KeyguardManager ) context.getSystemService(Context.KEYGUARD_SERVICE);
        return manager.isKeyguardLocked();
    }

    /*private void createAnimations(LayoutTransition transition) {
     // Adding
        customAppearingAnim = ObjectAnimator.ofFloat(null, "translationY", 500f).
                setDuration(transition.getDuration(LayoutTransition.APPEARING));
        customAppearingAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator anim) {
                View view = (View) ((ObjectAnimator) anim).getTarget();
                view.setTranslationY(0);
            }
        });

        // Removing
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("translationY", -300f);
        PropertyValuesHolder pvhAlpha = PropertyValuesHolder.ofFloat("alpha", 0f);
        customDisappearingAnim = ObjectAnimator.ofPropertyValuesHolder(this, pvhY, pvhAlpha).
                setDuration(transition.getDuration(LayoutTransition.DISAPPEARING));
        customDisappearingAnim.setInterpolator(new DecelerateInterpolator());
//        customDisappearingAnim = ObjectAnimator.ofFloat(null, "translationY", -300f).
//                setDuration(transition.getDuration(LayoutTransition.DISAPPEARING));
        customDisappearingAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator anim) {
                View view = (View) ((ObjectAnimator) anim).getTarget();
                view.setTranslationY(0);
            }
        });
    }*/
    public void dispatch(MotionEvent event) {
//        mAppWidgetContainer.handleExternalCameraEvent(event);
    }

    public void launchCamera() {
//        mActivityLauncher.launchCamera(getHandler(), null);
    }
}