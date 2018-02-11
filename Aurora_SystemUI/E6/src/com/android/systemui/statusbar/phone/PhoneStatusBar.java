/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.storage.StorageVolume;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.Telephony;
import gionee.provider.GnTelephony.SIMInfo;
import android.service.dreams.DreamService;
import android.service.dreams.IDreamManager;
import android.text.AndroidCharacter;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Slog;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewPropertyAnimator;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import aurora.widget.AuroraButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.internal.statusbar.StatusBarIcon;
import com.android.internal.statusbar.StatusBarNotification;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyIntents;

import com.android.systemui.R;
import android.telephony.PhoneStateListener;
import com.android.systemui.statusbar.BaseStatusBar;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.GestureRecorder;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.NotificationData.Entry;
import com.android.systemui.statusbar.SignalClusterView;
import com.android.systemui.statusbar.SignalClusterViewGemini;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.DateView;
import com.android.systemui.statusbar.policy.IntruderAlertView;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.NetworkControllerGemini;
import com.android.systemui.statusbar.policy.NotificationRowLayout;
import com.android.systemui.statusbar.policy.OnSizeChangedListener;
import com.android.systemui.statusbar.policy.Prefs;
import com.android.systemui.statusbar.policy.TelephonyIcons;
import com.android.systemui.statusbar.policy.TelephonyIconsGemini;
//import com.android.systemui.statusbar.toolbar.ToolBarIndicator;
//import com.android.systemui.statusbar.toolbar.ToolBarView;
import com.android.systemui.statusbar.util.SIMHelper;

import com.gionee.featureoption.FeatureOption;
import com.android.systemui.Xlog;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import android.os.SystemProperties;

// Gionee <fengjianyi><2013-05-10> add for CR00800567 start
import android.widget.RadioButton;
import com.android.systemui.statusbar.util.ToolbarIconUtils;
// Gionee <fengjianyi><2013-05-10> add for CR00800567 end

// Aurora <zhanggp> <2013-10-08> added for systemui begin
import android.graphics.Color;
import android.telephony.TelephonyManager;
// Aurora <zhanggp> <2013-10-08> added for systemui end
// Aurora <tongyh> <2013-12-06> add quick lock screen function begin
import android.view.KeyEvent;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.hardware.input.InputManager;
// Aurora <tongyh> <2013-12-06> add quick lock screen function end
import android.os.Build;

// Aurora <Steve.Tang> 2014-12-18. For Dual-Sim. start
import com.android.systemui.statusbar.MSimSignalClusterView;
import com.android.systemui.statusbar.policy.MSimNetworkController;
import gionee.telephony.GnTelephonyManager;
//Aurora <tongyh> <2015-01-13> add account begin
import java.io.File;
import android.net.Uri;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import android.widget.RelativeLayout;
//Aurora <tongyh> <2015-01-13> add account end
//Aurora <tongyh> <2015-01-13> account's clipCircleBitmap begin 
import android.graphics.RectF;
import android.graphics.PorterDuffXfermode;
import android.graphics.Paint;
//Aurora <tongyh> <2015-01-13> account's clipCircleBitmap end

public class PhoneStatusBar extends BaseStatusBar {
    static final String TAG = "PhoneStatusBar";
    public static final boolean DEBUG = BaseStatusBar.DEBUG;
    public static final boolean SPEW = DEBUG;
    public static final boolean DUMPTRUCK = true; // extra dumpsys info
    public static final boolean DEBUG_GESTURES = false;

    public static final boolean DEBUG_CLINGS = false;

    public static final boolean ENABLE_NOTIFICATION_PANEL_CLING = false;

    public static final boolean SETTINGS_DRAG_SHORTCUT = true;

    // additional instrumentation for testing purposes; intended to be left on during development
    public static final boolean CHATTY = DEBUG;

    public static final String ACTION_STATUSBAR_START
            = "com.android.internal.policy.statusbar.START";
    
    /// M: Support AirplaneMode for Statusbar SimIndicator.
    private static final String ACTION_BOOT_IPO
            = "android.intent.action.ACTION_PREBOOT_IPO";
    /// M: [SystemUI] Dismiss new event icon when click clear button for keyguard.
    private static final String CLEAR_NEW_EVENT_VIEW_INTENT = "android.intent.action.KEYGUARD_CLEAR_UREAD_TIPS";

 // Aurora <tongyh> <2014-09-16> incall view can drag statusbar begin    
    public static boolean isShowInCallView = false;
 // Aurora <tongyh> <2014-09-16> incall view can drag statusbar end
// Aurora <tongyh> <2014-09-16> alarm clock statusbar's view can drag statusbar begin
    public static boolean isShowAlarmClockView = false;
// Aurora <tongyh> <2014-09-16> alarm clock statusbar's view can drag statusbar end
    private static final int MSG_OPEN_NOTIFICATION_PANEL = 1000;
    private static final int MSG_CLOSE_PANELS = 1001;
    private static final int MSG_OPEN_SETTINGS_PANEL = 1002;
    // 1020-1030 reserved for BaseStatusBar
    /// M: [SystemUI] Support "SIM indicator". @{
    private static final int MSG_SHOW_INTRUDER = 1003;
    private static final int MSG_HIDE_INTRUDER = 1004;
    /// @}

    // will likely move to a resource or other tunable param at some point
    private static final int INTRUDER_ALERT_DECAY_MS = 0; // disabled, was 10000;

    private static final boolean CLOSE_PANEL_WHEN_EMPTIED = true;

    private static final int NOTIFICATION_PRIORITY_MULTIPLIER = 10; // see NotificationManagerService
    private static final int HIDE_ICONS_BELOW_SCORE = Notification.PRIORITY_LOW * NOTIFICATION_PRIORITY_MULTIPLIER;

    // fling gesture tuning parameters, scaled to display density
    private float mSelfExpandVelocityPx; // classic value: 2000px/s
    private float mSelfCollapseVelocityPx; // classic value: 2000px/s (will be negated to collapse "up")
    private float mFlingExpandMinVelocityPx; // classic value: 200px/s
    private float mFlingCollapseMinVelocityPx; // classic value: 200px/s
    private float mCollapseMinDisplayFraction; // classic value: 0.08 (25px/min(320px,480px) on G1)
    private float mExpandMinDisplayFraction; // classic value: 0.5 (drag open halfway to expand)
    private float mFlingGestureMaxXVelocityPx; // classic value: 150px/s

    private float mExpandAccelPx; // classic value: 2000px/s/s
    private float mCollapseAccelPx; // classic value: 2000px/s/s (will be negated to collapse "up")
    private float mFlingGestureMaxOutputVelocityPx; // how fast can it really go? (should be a little 
                                                    // faster than mSelfCollapseVelocityPx)

    PhoneStatusBarPolicy mIconPolicy;

    // These are no longer handled by the policy, because we need custom strategies for them
    BluetoothController mBluetoothController;
    BatteryController mBatteryController;
    LocationController mLocationController;
    NetworkController mNetworkController;
	// Aurora <Steve.Tang> 2014-12-18. For Dual-Sim. start
    MSimNetworkController mMSimNetworkController;
	// Aurora <Steve.Tang> 2014-12-18. For Dual-Sim. end
    
    int mNaturalBarHeight = -1;
    int mIconSize = -1;
    int mIconHPadding = -1;
    Display mDisplay;
    Point mCurrentDisplaySize = new Point();

    IDreamManager mDreamManager;

    StatusBarWindowView mStatusBarWindow;
    PhoneStatusBarView mStatusBarView;
	

    // Aurora <zhanggp> <2013-10-08> added for systemui begin
 // Aurora <tongyh> <2014-09-16> incall view can drag statusbar begin    
    AuroraGreenStatusBarView mInCallView;
 // Aurora <tongyh> <2014-09-16> incall view can drag statusbar end    
 // Aurora <tongyh> <2014-04-11> Add alarm retreated to the background of green tips begin
    // Aurora <tongyh> <2014-09-16> alarm clock statusbar's view can drag statusbar begin    
    AuroraGreenStatusBarView mAlarmClockView;
    // Aurora <tongyh> <2014-09-16> alarm clock statusbar's view can drag statusbar begin
 // Aurora <tongyh> <2014-04-11> Add alarm retreated to the background of green tips end
	PhoneStatusBarPolicy.Callback mPolicyCallback;
	// Aurora <zhanggp> <2013-10-08> added for systemui end

    int mPixelFormat;
    Object mQueueLock = new Object();

    // viewgroup containing the normal contents of the statusbar
    LinearLayout mStatusBarContents;
 // Aurora <tongyh> <2013-12-06> add quick lock screen function begin
//    ImageView mQuickLockScreen;
//    ImageView mQuickLockScreenSecond;
 // Aurora <tongyh> <2013-12-06> add quick lock screen function end
    // right-hand icons
    LinearLayout mSystemIconArea;
    
    // left-hand icons 
    LinearLayout mStatusIcons;
    // the icons themselves
    /// M: Support "SIM Indicator".
    private ImageView mSimIndicatorIcon;
    IconMerger mNotificationIcons;
    // [+>
    View mMoreIcon;

    // expanded notifications
    NotificationPanelView mNotificationPanel; // the sliding/resizing panel within the notification window
    public ScrollView mScrollView;
    View mExpandedContents;
    int mNotificationPanelGravity;
    int mNotificationPanelMarginBottomPx, mNotificationPanelMarginPx;
    float mNotificationPanelMinHeightFrac;
    boolean mNotificationPanelIsFullScreenWidth;
    TextView mNotificationPanelDebugText;

    // settings
    //QuickSettings mQS;
    public boolean mHasSettingsPanel, mHasFlipSettings;
    //SettingsPanelView mSettingsPanel;
    public View mFlipSettingsView;
    //QuickSettingsContainerView mSettingsContainer;
    int mSettingsPanelGravity;

    // top bar
    View mNotificationPanelHeader;
    View mDateTimeView; 
    View mClearButton;
    ImageView mSettingsButton, mNotificationButton;
    /// M: [SystemUI] Remove settings button to notification header.
    private View mHeaderSettingsButton;

    // carrier/wifi label
    private TextView mCarrierLabel;
	// Aurora <Steve.Tang> 2014-12-18. For Dual-Sim. start
    private TextView mSubsLabel;
	// Aurora <Steve.Tang> 2014-12-18. For Dual-Sim. end
    private boolean mCarrierLabelVisible = false;
    private int mCarrierLabelHeight;
    /// M: Calculate ToolBar height when sim indicator is showing.
    private int mToolBarViewHeight;
    private TextView mEmergencyCallLabel;
    private int mNotificationHeaderHeight;

    private boolean mShowCarrierInPanel = false;

    // position
    int[] mPositionTmp = new int[2];
    boolean mExpandedVisible;

    // the date view
    DateView mDateView;

    // for immersive activities
    private IntruderAlertView mIntruderAlertView;

    // on-screen navigation buttons
    private NavigationBarView mNavigationBarView = null;

    // the tracker view
    int mTrackingPosition; // the position of the top of the tracking view.

    // ticker
    private Ticker mTicker;
    private View mTickerView;
    private boolean mTicking;

    // Tracking finger for opening/closing.
    int mEdgeBorder; // corresponds to R.dimen.status_bar_edge_ignore
    boolean mTracking;
    VelocityTracker mVelocityTracker;

    // help screen
    private boolean mClingShown;
    private ViewGroup mCling;
    private boolean mSuppressStatusBarDrags; // while a cling is up, briefly deaden the bar to give things time to settle

    boolean mAnimating;
    boolean mClosing; // only valid when mAnimating; indicates the initial acceleration
    float mAnimY;
    float mAnimVel;
    float mAnimAccel;
    long mAnimLastTimeNanos;
    boolean mAnimatingReveal = false;
    int mViewDelta;
    float mFlingVelocity;
    int mFlingY;
    int[] mAbsPos = new int[2];
    Runnable mPostCollapseCleanup = null;

    private Animator mLightsOutAnimation;
    private Animator mLightsOnAnimation;

    /// M: Support "Change font size of phone".    
    private float mPreviousConfigFontScale;

    // for disabling the status bar
    int mDisabled = 0;

    // tracking calls to View.setSystemUiVisibility()
    int mSystemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE;

    DisplayMetrics mDisplayMetrics = new DisplayMetrics();

    /// M: [ALPS00336833] When orientation changed, request layout to avoid status bar layout error. @{
    boolean mNeedRelayout = false;
    private int mPrevioutConfigOrientation;
    /// M: [ALPS00336833] When orientation changed, request layout to avoid status bar layout error. @}

    // Gionee <fengjianyi><2013-05-10> add for CR00800567 start
    private GnPanelPager mPanelPager;
    private List<View> mPagerViewsList = new ArrayList<View>();
    private RadioButton mNotificationRadioButton, mSwitchRadioButton;
    
    final int AURORA_NOTIFICATION_ID = 16000;
    final int PHONE_NOTIFICATION_ID = 100;
    final int CALL_FORWARDING_NOTIFICATION_ID = 1;
    boolean mSetStatusbarTransparent = false;

    //click count
    private int touchTount; 
    //first click time
    private long firstClick; 
    //last click time
    private long lastClick;
    
  //click count
    private int touchTountSecond; 
    //first click time
    private long firstClickSecond; 
    //last click time
    private long lastClickSecond;


//Aurora <tongyh> <2015-01-13> add account begin
    private String oldSycDistanceTime="";
    ImageView accountIcon;
    TextView accountName;
    TextView accountDate;
//Aurora <tongyh> <2015-01-13> add account end
    public View getPagerView(int index) {
    	return mPagerViewsList.get(index);
    }
    
    public boolean hasNotificationData() {
    	return mNotificationData.size() > 0;
    }
    // Gionee <fengjianyi><2013-05-10> add for CR00800567 end

    // XXX: gesture research
    private final GestureRecorder mGestureRec = DEBUG_GESTURES
        ? new GestureRecorder("/sdcard/statusbar_gestures.dat") 
        : null;

    private int mNavigationIconHints = 0;
    //Aurora <tongyh> <2014-07-09> add full-screen pull-down status bar begin
    private WindowManager.LayoutParams mShowLP;
	private WindowManager.LayoutParams mHideLP;
    private View mView;
    private static final long AUTOHIDE_TIMEOUT_MS = 2500;
    private boolean mAutohideSuspended;
    //Aurora <tongyh> <2014-07-09> add full-screen pull-down status bar end
 // Aurora <tongyh> <2014-03-08> add phone call state idle  can quick lock screen begin
    TelephonyManager telManager;
    private boolean isPhoneIdle = true;
    
    public boolean isPhoneIdle() {
		return isPhoneIdle;
	}

	public void setPhoneIdle(boolean isPhoneIdle) {
		this.isPhoneIdle = isPhoneIdle;
	}
 // Aurora <tongyh> <2014-03-08> add phone call state idle  can quick lock screen end
    
    private final Animator.AnimatorListener mMakeIconsInvisible = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            // double-check to avoid races
            if (mStatusBarContents.getAlpha() == 0) {
                if (DEBUG) Slog.d(TAG, "makeIconsInvisible");
                mStatusBarContents.setVisibility(View.INVISIBLE);
            }
        }
    };
    
 // Aurora <tongyh> <2014-03-08> add phone call state idle  can quick lock screen begin
    private class HandlerBarPhoneStateListener extends PhoneStateListener{
    	public void onCallStateChanged(int state, String incomingNumber) {
    		 switch (state) {
    		 case TelephonyManager.CALL_STATE_IDLE:
    			 /*if(mQuickLockScreen != null){
    				 mQuickLockScreen.setVisibility(View.VISIBLE);
    			 }*/
    			//Aurora <tongyh> <2015-02-26> fix bug BUG #10904 begin
    			 setInCallViewGone();
    			 //Aurora <tongyh> <2015-02-26> fix bug BUG #10904 end
    			 isPhoneIdle = true;
    			 break;
    		 case TelephonyManager.CALL_STATE_OFFHOOK:
    			 /*if(mQuickLockScreen != null){
    			     mQuickLockScreen.setVisibility(View.GONE);
    			 }*/
    			 isPhoneIdle = false;
    			 break;
    		 case TelephonyManager.CALL_STATE_RINGING:
    			/* if(mQuickLockScreen != null){
    			     mQuickLockScreen.setVisibility(View.GONE);
    			 }*/
    			 isPhoneIdle = false;
    			 break;
    		 }
    		 super.onCallStateChanged(state, incomingNumber);
    	}
    }
    // Aurora <tongyh> <2014-03-08> add phone call state idle  can quick lock screen  end

    // ensure quick settings is disabled until the current user makes it through the setup wizard
    private boolean mUserSetup = false;
    private ContentObserver mUserSetupObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            final boolean userSetup = 0 != Settings.Secure.getIntForUser(
                    mContext.getContentResolver(),
                    Settings.Secure.USER_SETUP_COMPLETE,
                    0 /*default */,
                    mCurrentUserId);
            if (MULTIUSER_DEBUG) Slog.d(TAG, String.format("User setup changed: " +
                    "selfChange=%s userSetup=%s mUserSetup=%s",
                    selfChange, userSetup, mUserSetup));
            if (mSettingsButton != null && !mHasSettingsPanel) {
                mSettingsButton.setVisibility(userSetup ? View.VISIBLE : View.INVISIBLE);
            }
            //if (mSettingsPanel != null) {
            //    mSettingsPanel.setEnabled(userSetup);
            //}
            if (userSetup != mUserSetup) {
                mUserSetup = userSetup;
                if (!mUserSetup && mStatusBarView != null)
                    animateCollapseQuickSettings();
            }
        }
    };
    
    // Aurora <tongyh> <2015-02-27> bug #11761 begin
    public static boolean STATUSBAR_IS_INVERT = false;
    // Aurora <tongyh> <2015-02-27> bug #11761 end
    
    @Override
    public void start() {
        mDisplay = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();

        mDreamManager = IDreamManager.Stub.asInterface(
                ServiceManager.checkService(DreamService.DREAM_SERVICE));

        super.start(); // calls createAndAddWindows()

        addNavigationBar();

        if (ENABLE_INTRUDERS) addIntruderView();

// Aurora <zhanggp> <2013-10-08> added for systemui begin
		mPolicyCallback = new PhoneStatusBarPolicy.Callback(){
            @Override
            public void setStausbarTransparentFlag(boolean isTransparent) {
                mSetStatusbarTransparent = isTransparent;
            }

			@Override
			public void updateStatusBarBgColor(boolean isTransparent){
				if(isTransparent){
					mStatusBarView.setBackgroundColor(Color.TRANSPARENT);
				}else{
					mStatusBarView.setBackgroundColor(Color.BLACK);
				}
			}
			// Aurora <zhanggp> <2013-10-18> added for systemui begin
			@Override
			public void setIcon(String slot, int iconId, int iconLevel,String contentDescription){
				try {
					synchronized (mExIcons) {
						int index = mExIcons.getSlotIndex(slot);
						if (index < 0) {
							throw new SecurityException("invalid status bar icon slot: " + slot);
						}
						String iconPackage = mContext.getPackageName();
						StatusBarIcon icon = new StatusBarIcon(iconPackage, UserHandle.OWNER, iconId,
								iconLevel, 0,
								contentDescription);
						//Slog.d(TAG, "setIcon slot=" + slot + " index=" + index + " icon=" + icon);
						mExIcons.setIcon(index, icon);
						if (mCommandQueue != null) {
							mCommandQueue.setExIcon(index, icon);

						}
					}
				} catch (Exception ex) {
				}

			}
			@Override
			public void setIconVisibility(String slot, boolean visible){
				try {
					synchronized (mExIcons) {
						int index = mExIcons.getSlotIndex(slot);
						if (index < 0) {
							throw new SecurityException("invalid status bar icon slot: " + slot);
						}
					
						StatusBarIcon icon = mExIcons.getIcon(index);
						if (icon == null) {
							return;
						}
					
						if (icon.visible != visible) {
							icon.visible = visible;
					
							if (mCommandQueue != null) {
								mCommandQueue.setExIcon(index, icon);
							}
						}
					}
				} catch (Exception ex) {
				}
			}
			// Aurora <zhanggp> <2013-10-18> added for systemui end
		};
		// Aurora <tongyh> <2013-12-06> set the statusbar background to black begin
		mHandlerBar.setPhoneStatusbarPolicyCallback(mPolicyCallback);
		// Aurora <tongyh> <2013-12-06> set the statusbar background to black end
// Aurora <zhanggp> <2013-10-08> added for systemui end
        // Lastly, call to the icon policy to install/update all the icons.
        mIconPolicy = new PhoneStatusBarPolicy(mContext);

		
// Aurora <zhanggp> <2013-10-08> added for systemui begin
		mIconPolicy.setCallback(mPolicyCallback);
// Aurora <zhanggp> <2013-10-08> added for systemui end

    }

    // ================================================================================
    // Constructing the view
    // ================================================================================
    protected PhoneStatusBarView makeStatusBarView() {
        final Context context = mContext;
        /// M: Support "Change font size of phone".
        Resources res = context.getResources();
        Configuration config = res.getConfiguration();
        mPreviousConfigFontScale = config.fontScale;
        mPrevioutConfigOrientation = config.orientation;
        updateDisplaySize(); // populates mDisplayMetrics
        loadDimens();

        /// M: Support AirplaneMode for Statusbar SimIndicator.
        updateAirplaneMode();

        mIconSize = res.getDimensionPixelSize(R.dimen.aurora_systemui_statusbar_icon_size);
        /// M: [SystemUI] Support "Dual SIM". {
        /*if (FeatureOption.MTK_GEMINI_SUPPORT) {
            // Gionee <fengjianyi><2013-05-10> modify for CR00800567 start
            //mStatusBarWindow = (StatusBarWindowView)View.inflate(context, R.layout.gemini_super_status_bar, null);
            if (ToolbarIconUtils.GN_QUICK_SETTINGS_SUPPORT) {
                mStatusBarWindow = (StatusBarWindowView)View.inflate(context, R.layout.zzzzz_gn_gemini_super_status_bar, null);
            } else {
                mStatusBarWindow = (StatusBarWindowView)View.inflate(context, R.layout.gemini_super_status_bar, null);
            }
            // Gionee <fengjianyi><2013-05-10> modify for CR00800567 end
        } else {*/
		// Aurora <Steve.Tang> 2014-12-18. For Dual-Sim. start
        	if (GnTelephonyManager.isMultiSimEnabled()) {
        		   if(!SystemProperties.getBoolean("phone.type.oversea", false)){
        			   mStatusBarWindow = (StatusBarWindowView) View.inflate(context,
                               R.layout.msim_super_status_bar_account, null);
        		   }else{
        			   mStatusBarWindow = (StatusBarWindowView) View.inflate(context,
                               R.layout.msim_super_status_bar, null);
        		   }
            } else {
            	 if(!SystemProperties.getBoolean("phone.type.oversea", false)){
            		 mStatusBarWindow = (StatusBarWindowView) View.inflate(context, R.layout.super_status_bar_account, null);
            	 }else{
            		 mStatusBarWindow = (StatusBarWindowView) View.inflate(context, R.layout.super_status_bar, null); 
            	 }
            }
//        }
        /// M: [SystemUI] Support "Dual SIM". }
        if (GnTelephonyManager.isMultiSimEnabled()) {
            mStatusBarView = (PhoneStatusBarView) mStatusBarWindow.findViewById(
                    R.id.msim_status_bar);
        } else {
            mStatusBarView = (PhoneStatusBarView) mStatusBarWindow.findViewById(R.id.status_bar);
        }
		// Aurora <Steve.Tang> 2014-12-18. For Dual-Sim. end
		// Aurora <zhanggp> <2013-10-08> added for systemui begin
		//mStatusBarView.setBackgroundColor(Color.TRANSPARENT);
        // Aurora <tongyh> <2014-09-16> incall view can drag statusbar begin    
//      mInCallView = (FrameLayout) mStatusBarWindow.findViewById(R.id.incall_status_bar);
		mInCallView = (AuroraGreenStatusBarView) mStatusBarWindow.findViewById(R.id.incall_status_bar);
        // Aurora <tongyh> <2014-09-16> incall view can drag statusbar end		
		mInCallView.setVisibility(View.GONE);
        // Aurora <tongyh> <2014-09-16> incall view can drag statusbar begin
		mInCallView.setPhoneStatusBarView(mStatusBarView);
		isShowInCallView = false;
        // Aurora <tongyh> <2014-09-16> incall view can drag statusbar end    
		// Aurora <zhanggp> <2013-10-08> added for systemui end
		// Aurora <tongyh> <2014-04-11> Add alarm retreated to the background of green tips begin
		mAlarmClockView = (AuroraGreenStatusBarView) mStatusBarWindow.findViewById(R.id.alarm_clock_status_bar);
		mAlarmClockView.setVisibility(View.GONE);
		// Aurora <tongyh> <2014-09-16> alarm clock statusbar's view can drag statusbar begin
		mAlarmClockView.setPhoneStatusBarView(mStatusBarView);
		isShowAlarmClockView = false;
		// Aurora <tongyh> <2014-09-16> alarm clock statusbar's view can drag statusbar end
		// Aurora <tongyh> <2014-04-11> Add alarm retreated to the background of green tips end
        if (DEBUG) {
            mStatusBarWindow.setBackgroundColor(0x6000FF80);
        }
        mStatusBarWindow.mService = this;
        mStatusBarWindow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mExpandedVisible && !mAnimating) {
                        animateCollapsePanels();
                    }
                }
                return mStatusBarWindow.onTouchEvent(event);
            }});

        mStatusBarView.setBar(this);
        
		// Aurora <Steve.Tang> 2014-12-18. For Dual-Sim. start
        PanelHolder holder;
        if (GnTelephonyManager.isMultiSimEnabled()) {
            holder = (PanelHolder) mStatusBarWindow.findViewById(R.id.msim_panel_holder);
        } else {
            holder = (PanelHolder) mStatusBarWindow.findViewById(R.id.panel_holder);
        }
		// Aurora <Steve.Tang> 2014-12-18. For Dual-Sim. start
        mStatusBarView.setPanelHolder(holder);

        mNotificationPanel = (NotificationPanelView) mStatusBarWindow.findViewById(R.id.notification_panel);
        mNotificationPanel.setStatusBar(this);
		// Aurora <zhanggp> <2013-10-17> modified for systemui begin
        mNotificationPanelIsFullScreenWidth = true;
            //(mNotificationPanel.getLayoutParams().width == ViewGroup.LayoutParams.MATCH_PARENT);
		// Aurora <zhanggp> <2013-10-17> modified for systemui end
        // make the header non-responsive to clicks
		if(!SystemProperties.getBoolean("phone.type.oversea", false)){
			mNotificationPanel.findViewById(R.id.header).setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	            	try {
	            	    Uri uri = Uri.parse("openaccount://com.aurora.account.login");
	        		    Intent intent = new Intent();
	        		    intent.setAction(Intent.ACTION_VIEW);
	        		    intent.addCategory(Intent.CATEGORY_DEFAULT);
	        		    intent.setData(uri);
	        		    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        		    startActivityDismissingKeyguard(intent, true, true);
						mStatusBarView.setBackgroundColor(Color.BLACK);
	            	} catch (Exception e) {
	                }
	            }
	        });
		}else{
			mNotificationPanel.findViewById(R.id.header).setOnTouchListener(
	                new View.OnTouchListener() {
	                    @Override
	                    public boolean onTouch(View v, MotionEvent event) {
	                        return true; // e eats everything
	                    }
	                });
		}
        /// M: [ALPS00352181] When ActivityManager.isHighEndGfx(mDisplay) return true, the dialog
        /// will show error, it will has StatusBar windowBackground.
        mStatusBarWindow.setBackground(null);
        if (!ActivityManager.isHighEndGfx()) {
            mNotificationPanel.setBackground(new FastColorDrawable(context.getResources().getColor(
                    R.color.notification_panel_solid_background)));
        }
        if (ENABLE_INTRUDERS) {
            mIntruderAlertView = (IntruderAlertView) View.inflate(context, R.layout.intruder_alert, null);
            mIntruderAlertView.setVisibility(View.GONE);
            mIntruderAlertView.setBar(this);
        }
        if (MULTIUSER_DEBUG) {
            mNotificationPanelDebugText = (TextView) mNotificationPanel.findViewById(R.id.header_debug_info);
            mNotificationPanelDebugText.setVisibility(View.VISIBLE);
        }

        updateShowSearchHoldoff();

        try {
            boolean showNav = mWindowManagerService.hasNavigationBar();
            if (DEBUG) Slog.v(TAG, "hasNavigationBar=" + showNav);
            if (showNav) {
                mNavigationBarView =
                    (NavigationBarView) View.inflate(context, R.layout.navigation_bar, null);

                mNavigationBarView.setDisabledFlags(mDisabled);
                mNavigationBarView.setBar(this);
            }
        } catch (RemoteException ex) {
            // no window manager? good luck with that
        }

        // figure out which pixel-format to use for the status bar.
        mPixelFormat = PixelFormat.OPAQUE;

        mSystemIconArea = (LinearLayout) mStatusBarView.findViewById(R.id.system_icon_area);
        mStatusIcons = (LinearLayout)mStatusBarView.findViewById(R.id.statusIcons);
        mNotificationIcons = (IconMerger)mStatusBarView.findViewById(R.id.notificationIcons);
        mNotificationIcons.setOverflowIndicator(mMoreIcon);
        mStatusBarContents = (LinearLayout)mStatusBarView.findViewById(R.id.status_bar_contents);
     // Aurora <tongyh> <2013-12-06> add quick lock screen function begin
//        mQuickLockScreen = (ImageView)mStatusBarView.findViewById(R.id.quick_lockscreen);
//        mQuickLockScreenSecond = (ImageView)mStatusBarView.findViewById(R.id.quick_lockscreen_second);
//        mQuickLockScreen.setOnClickListener(mQuickLockScreenListener);
//        mQuickLockScreen.setOnTouchListener(mQuickLockScreenTouchListener);
//        mQuickLockScreenSecond.setOnTouchListener(mQuickLockScreenTouchListenerSecond);
     // Aurora <tongyh> <2013-12-06> add quick lock screen function end
        mTickerView = mStatusBarView.findViewById(R.id.ticker);

        /// M: [SystemUI] Support "Notification toolbar". {
        // Gionee <fengjianyi><2013-05-10> modify for CR00800567 start
        View notificationRowPanel = View.inflate(mContext, R.layout.zzzzz_gn_latest_items, null);
        /*
        mToolBarSwitchPanel = mStatusBarWindow.findViewById(R.id.toolBarSwitchPanel);
        mToolBarView = (ToolBarView) mStatusBarWindow.findViewById(R.id.tool_bar_view);
        ToolBarIndicator indicator = (ToolBarIndicator) mStatusBarWindow.findViewById(R.id.indicator);
        mToolBarView.setStatusBarService(this);
        mToolBarView.setToolBarSwitchPanel(mToolBarSwitchPanel);
        mToolBarView.setScrollToScreenCallback(indicator);
        mToolBarView.setToolBarIndicator(indicator);
        mToolBarView.hideSimSwithPanel();
        mToolBarView.moveToDefaultScreen(false);
        
        if (ToolbarIconUtils.GN_QUICK_SETTINGS_SUPPORT) {
            mToolBarSwitchPanel = notificationRowPanel.findViewById(R.id.toolBarSwitchPanel);
            mToolBarView = (ToolBarView) notificationRowPanel.findViewById(R.id.tool_bar_view);
            ToolBarIndicator indicator = (ToolBarIndicator) notificationRowPanel.findViewById(R.id.indicator);
            mToolBarView.setStatusBarService(this);
            mToolBarView.setToolBarSwitchPanel(mToolBarSwitchPanel);
            mToolBarView.setScrollToScreenCallback(indicator);
            mToolBarView.setToolBarIndicator(indicator);
            mToolBarView.hideSimSwithPanel();
            mToolBarView.moveToDefaultScreen(false);
        } else {
            mToolBarSwitchPanel = mStatusBarWindow.findViewById(R.id.toolBarSwitchPanel);
            mToolBarView = (ToolBarView) mStatusBarWindow.findViewById(R.id.tool_bar_view);
            ToolBarIndicator indicator = (ToolBarIndicator) mStatusBarWindow.findViewById(R.id.indicator);
            mToolBarView.setStatusBarService(this);
            mToolBarView.setToolBarSwitchPanel(mToolBarSwitchPanel);
            mToolBarView.setScrollToScreenCallback(indicator);
            mToolBarView.setToolBarIndicator(indicator);
            mToolBarView.hideSimSwithPanel();
            mToolBarView.moveToDefaultScreen(false);
        }
        */
        // Gionee <fengjianyi><2013-05-10> modify for CR00800567 end
        /// M: [SystemUI] Support "Notification toolbar". }

        /// M: [SystemUI] Support "SIM indicator". {
        mSimIndicatorIcon = (ImageView) mStatusBarView.findViewById(R.id.sim_indicator_internet_or_alwaysask);
        /// M: [SystemUI] Support "SIM indicator". }

        // Gionee <fengjianyi><2013-05-10> modify for CR00800567 start
        //mPile = (NotificationRowLayout)mStatusBarWindow.findViewById(R.id.latestItems);
        if (ToolbarIconUtils.GN_QUICK_SETTINGS_SUPPORT) {
            mScrollView = (ScrollView) notificationRowPanel.findViewById(R.id.scroll);
            mScrollView.setVerticalScrollBarEnabled(false); // less drawing during pulldowns
            if (!mNotificationPanelIsFullScreenWidth) {
                mScrollView.setSystemUiVisibility(
                        View.STATUS_BAR_DISABLE_NOTIFICATION_TICKER |
                        View.STATUS_BAR_DISABLE_NOTIFICATION_ICONS |
                        View.STATUS_BAR_DISABLE_CLOCK);
            }
            mPile = (NotificationRowLayout) mScrollView.findViewById(R.id.latestItems);
        } else {
            mPile = (NotificationRowLayout)mStatusBarWindow.findViewById(R.id.latestItems);
        }
        // Gionee <fengjianyi><2013-05-10> modify for CR00800567 end
        mPile.setLayoutTransitionsEnabled(false);
		// Aurora <zhanggp> <2013-11-01> modified for systemui begin
        //mPile.setLongPressListener(getNotificationLongClicker());
		// Aurora <zhanggp> <2013-11-01> modified for systemui end
        mExpandedContents = mPile; // was: expanded.findViewById(R.id.notificationLinearLayout);

        mNotificationPanelHeader = mStatusBarWindow.findViewById(R.id.header);

        mClearButton = mStatusBarWindow.findViewById(R.id.clear_all_button);
        mClearButton.setOnClickListener(mClearButtonListener);
        mClearButton.setAlpha(0f);
        mClearButton.setVisibility(View.GONE);
        mClearButton.setEnabled(false);
        mDateView = (DateView)mStatusBarWindow.findViewById(R.id.date);

        mHasSettingsPanel = res.getBoolean(R.bool.config_hasSettingsPanel);
        mHasFlipSettings = res.getBoolean(R.bool.config_hasFlipSettingsPanel);

        mDateTimeView = mNotificationPanelHeader.findViewById(R.id.datetime);
		// Aurora <zhanggp> <2013-10-08> modified for systemui begin
        //if (mHasFlipSettings) {
//Aurora <rocktong> <2015-01-19> account bug begin
//            mDateTimeView.setOnClickListener(mClockClickListener);
//Aurora <rocktong> <2015-01-19> account bug end
            mDateTimeView.setEnabled(true);
        //}
		// Aurora <zhanggp> <2013-10-08> modified for systemui end
         // Aurora <tongyh> <2015-01-14> delete notify settings drawable begin
   		   if(SystemProperties.getBoolean("phone.type.oversea", false)){
   			mSettingsButton = (ImageView) mStatusBarWindow.findViewById(R.id.settings_button);
   		   }
            
        
        if (mSettingsButton != null) {
            mSettingsButton.setOnClickListener(mSettingsButtonListener);
            if (mHasSettingsPanel) {
                /// M: [SystemUI] Remove settings button to notification header @{.
                mHeaderSettingsButton = mStatusBarWindow.findViewById(R.id.header_settings_button);
                mHeaderSettingsButton.setOnClickListener(mHeaderSettingsButtonListener);
                /// M: [SystemUI] Remove settings button to notification header @}.
                if (mStatusBarView.hasFullWidthNotifications()) {
                    // the settings panel is hiding behind this button
                    mSettingsButton.setImageResource(R.drawable.ic_notify_quicksettings);
                    mSettingsButton.setVisibility(View.VISIBLE);
                } else {
                    // there is a settings panel, but it's on the other side of the (large) screen
                    final View buttonHolder = mStatusBarWindow.findViewById(
                            R.id.settings_button_holder);
                    if (buttonHolder != null) {
                        buttonHolder.setVisibility(View.GONE);
                    }
                }
                // Gionee <fengjianyi><2013-05-10> add for CR00800567 start
//            	if (ToolbarIconUtils.GN_QUICK_SETTINGS_SUPPORT) {
//                    View buttonHolder = mStatusBarWindow.findViewById(R.id.settings_button_holder);
//                    if (buttonHolder != null) {
//                        buttonHolder.setVisibility(View.GONE);
//                    }
//            	}
                // Gionee <fengjianyi><2013-05-10> add for CR00800567 end
            } else {
                // no settings panel, go straight to settings
                mSettingsButton.setVisibility(View.VISIBLE);
                mSettingsButton.setImageResource(R.drawable.ic_notify_settings);
            }
        }
        if (mHasFlipSettings) {
            mNotificationButton = (ImageView) mStatusBarWindow.findViewById(R.id.notification_button);
            if (mNotificationButton != null) {
                mNotificationButton.setOnClickListener(mNotificationButtonListener);
            }
        }

        // Gionee <fengjianyi><2013-05-10> modify for CR00800567 start
        /*
        mScrollView = (ScrollView)mStatusBarWindow.findViewById(R.id.scroll);
        mScrollView.setVerticalScrollBarEnabled(false); // less drawing during pulldowns
        if (!mNotificationPanelIsFullScreenWidth) {
            mScrollView.setSystemUiVisibility(
                    View.STATUS_BAR_DISABLE_NOTIFICATION_TICKER |
                    View.STATUS_BAR_DISABLE_NOTIFICATION_ICONS |
                    View.STATUS_BAR_DISABLE_CLOCK);
        }
        */
     // Aurora <tongyh> <2015-01-14> delete notify settings drawable end
        if (!ToolbarIconUtils.GN_QUICK_SETTINGS_SUPPORT) {
            mScrollView = (ScrollView)mStatusBarWindow.findViewById(R.id.scroll);
            mScrollView.setVerticalScrollBarEnabled(false); // less drawing during pulldowns
            if (!mNotificationPanelIsFullScreenWidth) {
                mScrollView.setSystemUiVisibility(
                        View.STATUS_BAR_DISABLE_NOTIFICATION_TICKER |
                        View.STATUS_BAR_DISABLE_NOTIFICATION_ICONS |
                        View.STATUS_BAR_DISABLE_CLOCK);
            }
        }
        // Gionee <fengjianyi><2013-05-10> modify for CR00800567 end

        mTicker = new MyTicker(context, mStatusBarView);

        TickerView tickerView = (TickerView)mStatusBarView.findViewById(R.id.tickerText);
        tickerView.mTicker = mTicker;

        mEdgeBorder = res.getDimensionPixelSize(R.dimen.status_bar_edge_ignore);

        // set the inital view visibility
        setAreThereNotifications();

        // Other icons
        mLocationController = new LocationController(mContext); // will post a notification
        mBatteryController = new BatteryController(mContext);
        mBatteryController.addIconView((ImageView)mStatusBarView.findViewById(R.id.battery));
		// Aurora <zhanggp> <2013-11-01> added for systemui begin
//		mBatteryController.addBgIconView((ImageView)mStatusBarView.findViewById(R.id.battery_bg));
		// Aurora <zhanggp> <2013-11-01> added for systemui end
        mBatteryController.addLabelView((TextView) mStatusBarWindow.findViewById(R.id.percentage));
        mBluetoothController = new BluetoothController(mContext);

        /// M: [SystemUI] Support "Dual SIM". {
		// Aurora <Steve.Tang> 2014-12-18. For Dual-Sim. start
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            // Gionee <fengjianyi><2013-05-10> modify for CR00800567 start
        	/*
            mCarrier1 = (CarrierLabelGemini) mStatusBarWindow.findViewById(R.id.carrier1);
            mCarrier2 = (CarrierLabelGemini) mStatusBarWindow.findViewById(R.id.carrier2);
            mCarrierDivider = mStatusBarWindow.findViewById(R.id.carrier_divider);
            mCarrierLabelGemini = (LinearLayout) mStatusBarWindow.findViewById(R.id.carrier_label_gemini);
            */
            if (ToolbarIconUtils.GN_QUICK_SETTINGS_SUPPORT) {
                mCarrierLabelGemini = (LinearLayout) notificationRowPanel.findViewById(R.id.carrier_label_gemini);
                mCarrier1 = (CarrierLabelGemini) notificationRowPanel.findViewById(R.id.carrier1);
                mCarrier2 = (CarrierLabelGemini) notificationRowPanel.findViewById(R.id.carrier2);
                mCarrierDivider = notificationRowPanel.findViewById(R.id.carrier_divider);
            } else {
                mCarrier1 = (CarrierLabelGemini) mStatusBarWindow.findViewById(R.id.carrier1);
                mCarrier2 = (CarrierLabelGemini) mStatusBarWindow.findViewById(R.id.carrier2);
                mCarrierDivider = mStatusBarWindow.findViewById(R.id.carrier_divider);
                mCarrierLabelGemini = (LinearLayout) mStatusBarWindow.findViewById(R.id.carrier_label_gemini);
            }
            // Gionee <fengjianyi><2013-05-10> modify for CR00800567 end
            mShowCarrierInPanel = (mCarrierLabelGemini != null);
            if (mShowCarrierInPanel) {
                mCarrier1.setSlotId(FeatureOption.GEMINI_SIM_1);
                mCarrier2.setSlotId(FeatureOption.GEMINI_SIM_2);
            }
        } else {
            mCarrierLabel = (TextView)mStatusBarWindow.findViewById(R.id.carrier_label);
            mShowCarrierInPanel = (mCarrierLabel != null);
        }
// Aurora <Steve.Tang> 2014-12-18. For Dual-Sim. end
//		// Aurora <zhanggp> <2013-10-08> added for systemui begin
     // Aurora <tongyh> <2014-04-09> add CarrierLabel begin
//		mShowCarrierInPanel = false;
     // Aurora <tongyh> <2014-04-09> add CarrierLabel end
//		// Aurora <zhanggp> <2013-10-08> added for systemui end
// Aurora <Steve.Tang> 2014-12-18. For Dual-Sim. start
        if(GnTelephonyManager.isMultiSimEnabled()){
        	mMSimNetworkController = new MSimNetworkController(mContext);
            MSimSignalClusterView mSimSignalCluster = (MSimSignalClusterView)
              mStatusBarView.findViewById(R.id.msim_signal_cluster);
            for (int i=0; i < GnTelephonyManager.getPhoneCount(); i++) {
                mMSimNetworkController.addSignalCluster(mSimSignalCluster, i);
            }
            mSimSignalCluster.setNetworkController(mMSimNetworkController);
            mSubsLabel = (TextView)mStatusBarWindow.findViewById(R.id.subs_label);
            mShowCarrierInPanel = (mCarrierLabel != null);

            if (DEBUG) Log.v(TAG, "carrierlabel=" + mCarrierLabel + " show=" +
                                    mShowCarrierInPanel + "operator label=" + mSubsLabel);
            if (mShowCarrierInPanel) {
                mCarrierLabel.setVisibility(mCarrierLabelVisible ? View.VISIBLE : View.INVISIBLE);

                // for mobile devices, we always show mobile connection info here (SPN/PLMN)
                // for other devices, we show whatever network is connected
                if (mMSimNetworkController.hasMobileDataFeature()) {
//                    mMSimNetworkController.addMobileLabelView(mCarrierLabel);
                } else {
//                    mMSimNetworkController.addCombinedLabelView(mCarrierLabel);
                }
                mSubsLabel.setVisibility(View.VISIBLE);
                mMSimNetworkController.addSubsLabelView(mSubsLabel);
                // set up the dynamic hide/show of the label
                mPile.setOnSizeChangedListener(new OnSizeChangedListener() {
                    @Override
                    public void onSizeChanged(View view, int w, int h, int oldw, int oldh) {
                        updateCarrierLabelVisibility(false);
                    }
                });
            }
        }else{
		// Aurora <Steve.Tang> 2014-12-18. For Dual-Sim. end
        //rocktong
    /*    if (FeatureOption.MTK_GEMINI_SUPPORT) {
            mNetworkControllerGemini = new NetworkControllerGemini(mContext);
            final SignalClusterViewGemini signalCluster =
                (SignalClusterViewGemini) mStatusBarView.findViewById(R.id.signal_cluster);
            mNetworkControllerGemini.addSignalCluster(signalCluster);
            signalCluster.setNetworkControllerGemini(mNetworkControllerGemini);
            mNetworkControllerGemini.setCarrierGemini(mCarrier1, mCarrier2, mCarrierDivider);
        } else {*/
            mNetworkController = new NetworkController(mContext);
            final SignalClusterView signalCluster =
                (SignalClusterView)mStatusBarView.findViewById(R.id.signal_cluster);
            mNetworkController.addSignalCluster(signalCluster);
            signalCluster.setNetworkController(mNetworkController);
        }
        /// M: [SystemUI] Support "Dual SIM". }

		// Aurora <zhanggp> <2013-10-08> modified for systemui begin
		//if (!FeatureOption.MTK_GEMINI_SUPPORT) {
        if (false) {
		// Aurora <zhanggp> <2013-10-08> modified for systemui end
            mEmergencyCallLabel = (TextView)mStatusBarWindow.findViewById(R.id.emergency_calls_only);
            if (mEmergencyCallLabel != null) {
                mNetworkController.addEmergencyLabelView(mEmergencyCallLabel);
                mEmergencyCallLabel.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) { }});
                mEmergencyCallLabel.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom,
                            int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        updateCarrierLabelVisibility(false);
                    }});
            }
        }
        if (DEBUG) {
            /*if (FeatureOption.MTK_GEMINI_SUPPORT) {
                Slog.v(TAG, "carrierlabelGemini=" + mCarrierLabelGemini + " show=" + mShowCarrierInPanel);
            } else {*/
                Slog.v(TAG, "carrierlabel=" + mCarrierLabel + " show=" + mShowCarrierInPanel);
//            }
        }
        if (mShowCarrierInPanel) {
            /// M: [SystemUI] Support "Dual SIM". {
            /*if (FeatureOption.MTK_GEMINI_SUPPORT) {
                mCarrierLabelGemini.setVisibility(mCarrierLabelVisible ? View.VISIBLE : View.INVISIBLE);
                mCarrier2.setVisibility(View.GONE);
                mCarrierDivider.setVisibility(View.GONE);
            } else {*/
                mCarrierLabel.setVisibility(mCarrierLabelVisible ? View.VISIBLE : View.INVISIBLE);
//            }
            /// M: [SystemUI] Support "Dual SIM". }

            // for mobile devices, we always show mobile connection info here (SPN/PLMN)
            // for other devices, we show whatever network is connected
            /*if (!FeatureOption.MTK_GEMINI_SUPPORT) {
                if (!mNetworkController.hasMobileDataFeature()) {
                    mNetworkController.addCombinedLabelView(mCarrierLabel);
                }
            }*/

            // set up the dynamic hide/show of the label
            mPile.setOnSizeChangedListener(new OnSizeChangedListener() {
                @Override
                public void onSizeChanged(View view, int w, int h, int oldw, int oldh) {
                    updateCarrierLabelVisibility(false);
                }
            });
        }

        // Quick Settings (where available, some restrictions apply)
        if (mHasSettingsPanel) {
            // first, figure out where quick settings should be inflated
            final View settings_stub;
            if (mHasFlipSettings) {
                // a version of quick settings that flips around behind the notifications
                // Gionee <fengjianyi><2013-05-10> modify for CR00800567 start
                /*
                settings_stub = mStatusBarWindow.findViewById(R.id.flip_settings_stub);
                if (settings_stub != null) {
                    mFlipSettingsView = ((ViewStub)settings_stub).inflate();
                    mFlipSettingsView.setVisibility(View.GONE);
                    mFlipSettingsView.setVerticalScrollBarEnabled(false);
                }
                */
            	if (ToolbarIconUtils.GN_QUICK_SETTINGS_SUPPORT) {
                	mFlipSettingsView = View.inflate(mContext, R.layout.zzzzz_gn_flip_settings, null);
            	} else {
                    settings_stub = mStatusBarWindow.findViewById(R.id.flip_settings_stub);
                    if (settings_stub != null) {
                        mFlipSettingsView = ((ViewStub)settings_stub).inflate();
                        mFlipSettingsView.setVisibility(View.GONE);
                        mFlipSettingsView.setVerticalScrollBarEnabled(false);
                    }
                }
                // Gionee <fengjianyi><2013-05-10> modify for CR00800567 end
            } else {
                // full quick settings panel
                /*
                settings_stub = mStatusBarWindow.findViewById(R.id.quick_settings_stub);
                if (settings_stub != null) {
                    mSettingsPanel = (SettingsPanelView) ((ViewStub)settings_stub).inflate();
                } else {
                    mSettingsPanel = (SettingsPanelView) mStatusBarWindow.findViewById(R.id.settings_panel);
                }

                if (mSettingsPanel != null) {
                    if (!ActivityManager.isHighEndGfx()) {
                        mSettingsPanel.setBackground(new FastColorDrawable(context.getResources().getColor(
                                R.color.notification_panel_solid_background)));
                    }
                }
                */
            }

            // wherever you find it, Quick Settings needs a container to survive
            // Gionee <fengjianyi><2013-05-10> modify for CR00800567 start
            /*
            mSettingsContainer = (QuickSettingsContainerView)
                    mStatusBarWindow.findViewById(R.id.quick_settings_container);
            */
            /*
            if (ToolbarIconUtils.GN_QUICK_SETTINGS_SUPPORT) {
                mSettingsContainer = (QuickSettingsContainerView)
            		    mFlipSettingsView.findViewById(R.id.quick_settings_container);
            } else {
                mSettingsContainer = (QuickSettingsContainerView)
                        mStatusBarWindow.findViewById(R.id.quick_settings_container);
            }
            */
            // Gionee <fengjianyi><2013-05-10> modify for CR00800567 end
            /*
            if (mSettingsContainer != null) {
                mQS = new QuickSettings(mContext, mSettingsContainer);
                if (!mNotificationPanelIsFullScreenWidth) {
                    mSettingsContainer.setSystemUiVisibility(
                            View.STATUS_BAR_DISABLE_NOTIFICATION_TICKER
                            | View.STATUS_BAR_DISABLE_SYSTEM_INFO);
                }
                if (mSettingsPanel != null) {
                    mSettingsPanel.setQuickSettings(mQS);
                }
                mQS.setService(this);
                mQS.setBar(mStatusBarView);
                mQS.setup(mBatteryController);
            } else {
                mQS = null; // fly away, be free
            }
            */
        }
	
        // Gionee <fengjianyi><2013-05-10> add for CR00800567 start
        if (ToolbarIconUtils.GN_QUICK_SETTINGS_SUPPORT) {
            mPanelPager = (GnPanelPager) mStatusBarWindow.findViewById(R.id.panel_pager);
            mPagerViewsList.add(notificationRowPanel);
            mPagerViewsList.add(mFlipSettingsView);
            mPanelPager.setAdapter(new GnPagerAdapter(mPagerViewsList));
            
            mNotificationRadioButton = (RadioButton) mStatusBarWindow.findViewById(R.id.rb_notification);
            mNotificationRadioButton.setOnClickListener(new OnClickListener() {
    			
    			@Override
    			public void onClick(View v) {
    				mPanelPager.setCurrentItem(0);
    			}
    		});
            mSwitchRadioButton = (RadioButton) mStatusBarWindow.findViewById(R.id.rb_switch);
            mSwitchRadioButton.setOnClickListener(new OnClickListener() {
    			
    			@Override
    			public void onClick(View v) {
    				mPanelPager.setCurrentItem(1);
    			}
    		});
            mPanelPager.setOnPageChangeListener(new GnPanelPager.OnPageChangeListener() {
    			
    			@Override
    			public void onPageSelected(int arg0) {
    				if (arg0 == 0) {
    					mNotificationRadioButton.setChecked(true);
    		            mClearButton.setVisibility(View.VISIBLE);
    		            mClearButton.setAlpha(0f);
    		            setAreThereNotifications();
    		            if (mHeaderSettingsButton != null) {
    		                mHeaderSettingsButton.setVisibility(View.GONE);
    		            }
    				} else if (arg0 == 1) {
    					mSwitchRadioButton.setChecked(true);
    		            mClearButton.setVisibility(View.GONE);
    		            if (mHeaderSettingsButton != null) {
    		                mHeaderSettingsButton.setVisibility(View.VISIBLE);
    		            }
    				}
    			}
    			
    			@Override
    			public void onPageScrolled(int arg0, float arg1, int arg2) {
    				// TODO Auto-generated method stub
    				
    			}
    			
    			@Override
    			public void onPageScrollStateChanged(int arg0) {
    				// TODO Auto-generated method stub
    				
    			}
    		});
        }
        // Gionee <fengjianyi><2013-05-10> add for CR00800567 end

        mClingShown = ! (DEBUG_CLINGS 
            || !Prefs.read(mContext).getBoolean(Prefs.SHOWN_QUICK_SETTINGS_HELP, false));

        if (!ENABLE_NOTIFICATION_PANEL_CLING || ActivityManager.isRunningInTestHarness()) {
            mClingShown = true;
        }

//        final ImageView wimaxRSSI =
//                (ImageView)sb.findViewById(R.id.wimax_signal);
//        if (wimaxRSSI != null) {
//            mNetworkController.addWimaxIconView(wimaxRSSI);
//        }
     // Aurora <tongyh> <2014-03-08> add phone call state idle  can quick lock screen begin
        if(telManager == null){
			telManager = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
			telManager.listen(new HandlerBarPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
		}
     // Aurora <tongyh> <2014-03-08> add phone call state idle  can quick lock screen end
        // receive broadcasts
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(FeatureOption.ACTION_SKIN_CHANGED);
        /// M: ALPS00349274 to hide navigation bar when ipo shut down to avoid it flash when in boot ipo mode.{
        filter.addAction("android.intent.action.ACTION_SHUTDOWN_IPO");
        filter.addAction("android.intent.action.ACTION_BOOT_IPO");
        /// M: ALPS00349274 to hide navigation bar when ipo shut down to avoid it flash when in boot ipo mode.}
        /// M: Support "Dual SIM PLMN".
        filter.addAction(FeatureOption.SPN_STRINGS_UPDATED_ACTION);
        //gionee fengxb 2013-4-27 add for CR00802140 start
		// Aurora <Steve.Tang> 2014-12-18. For Dual-Sim. start
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            filter.addAction(FeatureOption.SPN_STRINGS_UPDATED_ACTION_SIM1);
            filter.addAction(FeatureOption.SPN_STRINGS_UPDATED_ACTION_SIM2);
        }
		// Aurora <Steve.Tang> 2014-12-18. For Dual-Sim. end
        //gionee fengxb 2013-4-27 add for CR00802140 end
        //Aurora <tongyh> <2014-06-05> add ACTION_SIM_STATE_CHANGED broadcast begin
        filter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        //Aurora <tongyh> <2014-06-05> add ACTION_SIM_STATE_CHANGED broadcast end
//Aurora <tongyh> <2014-07-09> add full-screen pull-down status bar begin
        filter.addAction("com.aurora.systemui.visiable.statusbar");
//Aurora <tongyh> <2014-07-09> add full-screen pull-down status bar end
		// Aurora <Steve.Tang> 2014-08-13 air mode change broadcast. start
		filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		// Aurora <Steve.Tang> 2014-08-13 air mode change broadcast. end
        context.registerReceiver(mBroadcastReceiver, filter);

        // listen for USER_SETUP_COMPLETE setting (per-user)
        resetUserSetupObserver();
        /// M: [SystemUI] Support "Dual SIM". {
        IntentFilter simInfoIntentFilter = new IntentFilter();
        simInfoIntentFilter.addAction(FeatureOption.SIM_SETTINGS_INFO_CHANGED);
        simInfoIntentFilter.addAction(FeatureOption.ACTION_SIM_INSERTED_STATUS);
        simInfoIntentFilter.addAction(FeatureOption.ACTION_SIM_INFO_UPDATE);
        simInfoIntentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        simInfoIntentFilter.addAction(ACTION_BOOT_IPO);
        context.registerReceiver(mSIMInfoReceiver, simInfoIntentFilter);
        /// M: [SystemUI] Support "Dual SIM". }

        /// M: [ALPS00438070] Handle SD Swap Condition.
        mNeedRemoveKeys = new ArrayList<IBinder>();
        /*if (FeatureOption.MTK_2SDCARD_SWAP) {
            IntentFilter mediaEjectFilter = new IntentFilter();
            mediaEjectFilter.addAction(Intent.ACTION_MEDIA_EJECT);
            mediaEjectFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            mediaEjectFilter.addDataScheme("file");
            context.registerReceiver(mMediaEjectBroadcastReceiver, mediaEjectFilter);
        }*/
        //Aurora <tongyh> <2014-07-09> add full-screen pull-down status bar begin
    	mHideLP = new WindowManager.LayoutParams();
		mHideLP.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
		mHideLP.gravity = Gravity.TOP;
		mHideLP.format = PixelFormat.TRANSLUCENT;
		mHideLP.height = 0;
		mHideLP.width = 0;
		mHideLP.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		
    	mShowLP = new WindowManager.LayoutParams();
		mShowLP.flags = WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
		mShowLP.gravity = Gravity.TOP;
		mShowLP.format = PixelFormat.TRANSLUCENT;
		mShowLP.height = 0;
		mShowLP.width = 0;
		mShowLP.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        //Aurora <tongyh> <2014-07-09> add full-screen pull-down status bar end
//Aurora <tongyh> <2015-01-13> add account begin
        if(!SystemProperties.getBoolean("phone.type.oversea", false)){
            accountIcon = (ImageView)mStatusBarWindow.findViewById(R.id.account_icon);
            accountName = (TextView)mStatusBarWindow.findViewById(R.id.account_name);
//        accountIcon.setOnClickListener(mAccountIconListener);
//        accountName.setOnClickListener(mAccountNameListener);
            accountDate = (TextView)mStatusBarWindow.findViewById(R.id.account_date);
            initAccountProviderObserver();
        }
//Aurora <tongyh> <2015-01-13> add account end
        return mStatusBarView;
    }

    // Gionee <fengjianyi><2013-05-13> add for CR00800567 start
    private void updateStatusBarView(int orientation) {
        PanelHolder holder = (PanelHolder) mStatusBarWindow.findViewById(R.id.panel_holder);
        holder.removeViewAt(0);
        View contentView = View.inflate(mContext, orientation ==  Configuration.ORIENTATION_PORTRAIT ?
        		R.layout.zzzzz_gn_gemini_status_bar_expanded : R.layout.zzzzz_gn_gemini_status_bar_expanded_land, null);
        holder.addView(contentView, 0);
        mStatusBarView.setPanelHolder(holder);

        mNotificationPanel = (NotificationPanelView) mStatusBarWindow.findViewById(R.id.notification_panel);
        mNotificationPanel.setStatusBar(this);
		// Aurora <zhanggp> <2013-10-08> modified for systemui begin
        mNotificationPanelIsFullScreenWidth = true;
            //(mNotificationPanel.getLayoutParams().width == ViewGroup.LayoutParams.MATCH_PARENT);
		// Aurora <zhanggp> <2013-10-08> modified for systemui end
        mNotificationPanel.findViewById(R.id.header).setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true; // e eats everything
                    }
                });
        mStatusBarWindow.setBackground(null);
        if (!ActivityManager.isHighEndGfx()) {
            mNotificationPanel.setBackground(new FastColorDrawable(mContext.getResources().getColor(
                    R.color.notification_panel_solid_background)));
        }
        if (MULTIUSER_DEBUG) {
            mNotificationPanelDebugText = (TextView) mNotificationPanel.findViewById(R.id.header_debug_info);
            mNotificationPanelDebugText.setVisibility(View.VISIBLE);
        }

        mNotificationPanelHeader = mStatusBarWindow.findViewById(R.id.header);

        mClearButton = mStatusBarWindow.findViewById(R.id.clear_all_button);
        mClearButton.setOnClickListener(mClearButtonListener);
        mClearButton.setAlpha(0f);
        mClearButton.setVisibility(View.GONE);
        mClearButton.setEnabled(false);
        mDateView = (DateView)mStatusBarWindow.findViewById(R.id.date);

        mDateTimeView = mNotificationPanelHeader.findViewById(R.id.datetime);
		// Aurora <zhanggp> <2013-11-12> modified for systemui begin
        //if (mHasFlipSettings) {
//Aurora <rocktong> <2015-01-19> account bug begin
//            mDateTimeView.setOnClickListener(mClockClickListener);
//Aurora <rocktong> <2015-01-19> account bug end
            mDateTimeView.setEnabled(true);
        //}
		// Aurora <zhanggp> <2013-11-12> modified for systemui end
         // Aurora <tongyh> <2015-01-14> delete notify settings drawable begin
        /*mSettingsButton = (ImageView) mStatusBarWindow.findViewById(R.id.settings_button);
        if (mSettingsButton != null) {
            mSettingsButton.setOnClickListener(mSettingsButtonListener);
            if (mHasSettingsPanel) {
                mHeaderSettingsButton = mStatusBarWindow.findViewById(R.id.header_settings_button);
                mHeaderSettingsButton.setOnClickListener(mHeaderSettingsButtonListener);
                
                View buttonHolder = mStatusBarWindow.findViewById(R.id.settings_button_holder);
                if (buttonHolder != null) {
                    buttonHolder.setVisibility(View.GONE);
                }
            } else {
                // no settings panel, go straight to settings
                mSettingsButton.setVisibility(View.VISIBLE);
                mSettingsButton.setImageResource(R.drawable.ic_notify_settings);
            }
        }
        if (mHasFlipSettings) {
            mNotificationButton = (ImageView) mStatusBarWindow.findViewById(R.id.notification_button);
            if (mNotificationButton != null) {
                mNotificationButton.setOnClickListener(mNotificationButtonListener);
            }
        }*/
        
        // Aurora <tongyh> <2015-01-14> delete notify settings drawable end
    	mPanelPager.removeAllViews();
        mPanelPager = (GnPanelPager) mStatusBarWindow.findViewById(R.id.panel_pager);
        mPanelPager.setAdapter(new GnPagerAdapter(mPagerViewsList));
        mPanelPager.setCurrentItem(0);
        setAreThereNotifications();
        
        mNotificationRadioButton = (RadioButton) mStatusBarWindow.findViewById(R.id.rb_notification);
        mNotificationRadioButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mPanelPager.setCurrentItem(0);
			}
		});
        mSwitchRadioButton = (RadioButton) mStatusBarWindow.findViewById(R.id.rb_switch);
        mSwitchRadioButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mPanelPager.setCurrentItem(1);
			}
		});
        mPanelPager.setOnPageChangeListener(new GnPanelPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				if (arg0 == 0) {
					mNotificationRadioButton.setChecked(true);
		            mClearButton.setVisibility(View.VISIBLE);
		            mClearButton.setAlpha(0f);
		            setAreThereNotifications();
		            if (mHeaderSettingsButton != null) {
		                mHeaderSettingsButton.setVisibility(View.GONE);
		            }
				} else if (arg0 == 1) {
					mSwitchRadioButton.setChecked(true);
		            mClearButton.setVisibility(View.GONE);
		            if (mHeaderSettingsButton != null) {
		                mHeaderSettingsButton.setVisibility(View.VISIBLE);
		            }
				}
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
    }
    // Gionee <fengjianyi><2013-05-13> add for CR00800567 end

    @Override
    protected View getStatusBarView() {
        return mStatusBarView;
    }

    @Override
    protected WindowManager.LayoutParams getRecentsLayoutParams(LayoutParams layoutParams) {
        boolean opaque = false;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                layoutParams.width,
                layoutParams.height,
                WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
                (opaque ? PixelFormat.OPAQUE : PixelFormat.TRANSLUCENT));
        if (ActivityManager.isHighEndGfx()) {
            lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        } else {
            lp.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            lp.dimAmount = 0.75f;
        }
        lp.gravity = Gravity.BOTTOM | Gravity.LEFT;
        lp.setTitle("RecentsPanel");
        lp.windowAnimations = com.aurora.R.style.Animation_RecentApplications;
        lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED
        | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING;
        return lp;
    }

    @Override
    protected WindowManager.LayoutParams getSearchLayoutParams(LayoutParams layoutParams) {
        boolean opaque = false;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_NAVIGATION_BAR_PANEL,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
                (opaque ? PixelFormat.OPAQUE : PixelFormat.TRANSLUCENT));
        if (ActivityManager.isHighEndGfx()) {
            lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        }
        lp.gravity = Gravity.BOTTOM | Gravity.LEFT;
        lp.setTitle("SearchPanel");
        // TODO: Define custom animation for Search panel
        lp.windowAnimations = com.aurora.R.style.Animation_RecentApplications;
        lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED
        | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING;
        return lp;
    }

    @Override
    protected void updateSearchPanel() {
    	//Aurora <tongyh> <2013-12-05> delete the searchPanelView  for htc begin
//        super.updateSearchPanel();
//        mSearchPanelView.setStatusBarView(mNavigationBarView);
//        mNavigationBarView.setDelegateView(mSearchPanelView);
    	//Aurora <tongyh> <2013-12-05> delete the searchPanelView  for htc end
    }

    @Override
    public void showSearchPanel() {
    	//Aurora <tongyh> <2013-12-05> delete the searchPanelView  for htc begin
//        super.showSearchPanel();
//        mHandler.removeCallbacks(mShowSearchPanel);
//
//        // we want to freeze the sysui state wherever it is
//        mSearchPanelView.setSystemUiVisibility(mSystemUiVisibility);
//
//        WindowManager.LayoutParams lp =
//            (android.view.WindowManager.LayoutParams) mNavigationBarView.getLayoutParams();
//        lp.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
//        mWindowManager.updateViewLayout(mNavigationBarView, lp);
    	//Aurora <tongyh> <2013-12-05> delete the searchPanelView  for htc end
    }

    @Override
    public void hideSearchPanel() {
    	//Aurora <tongyh> <2013-12-05> delete the searchPanelView  for htc begin
//        super.hideSearchPanel();
//        WindowManager.LayoutParams lp =
//            (android.view.WindowManager.LayoutParams) mNavigationBarView.getLayoutParams();
//        lp.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
//        mWindowManager.updateViewLayout(mNavigationBarView, lp);
    	//Aurora <tongyh> <2013-12-05> delete the searchPanelView  for htc end
    }

    protected int getStatusBarGravity() {
        return Gravity.TOP | Gravity.FILL_HORIZONTAL;
    }

    public int getStatusBarHeight() {
        if (mNaturalBarHeight < 0) {
            final Resources res = mContext.getResources();
            mNaturalBarHeight =
                    res.getDimensionPixelSize(R.dimen.aurora_systemui_status_bar_height);
        }
        return mNaturalBarHeight;
    }

    private View.OnClickListener mRecentsClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            toggleRecentApps();
        }
    };

    private int mShowSearchHoldoff = 0;
    private Runnable mShowSearchPanel = new Runnable() {
        public void run() {
            showSearchPanel();
            awakenDreams();
        }
    };

    View.OnTouchListener mHomeSearchActionListener = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!shouldDisableNavbarGestures()) {
                    mHandler.removeCallbacks(mShowSearchPanel);
                    mHandler.postDelayed(mShowSearchPanel, mShowSearchHoldoff);
                }
            break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mHandler.removeCallbacks(mShowSearchPanel);
                awakenDreams();
            break;
        }
        return false;
        }
    };

    private void awakenDreams() {
        if (mDreamManager != null) {
            try {
                mDreamManager.awaken();
            } catch (RemoteException e) {
                // fine, stay asleep then
            }
        }
    }

    private void prepareNavigationBarView() {
        mNavigationBarView.reorient();

        mNavigationBarView.getRecentsButton().setOnClickListener(mRecentsClickListener);
        mNavigationBarView.getRecentsButton().setOnTouchListener(mRecentsPreloadOnTouchListener);
      //Aurora <tongyh> <2013-12-05> delete the searchPanelView  for htc begin
//        mNavigationBarView.getHomeButton().setOnTouchListener(mHomeSearchActionListener);
//        mNavigationBarView.getSearchLight().setOnTouchListener(mHomeSearchActionListener);
//        updateSearchPanel();
      //Aurora <tongyh> <2013-12-05> delete the searchPanelView  for htc end
    }

    // For small-screen devices (read: phones) that lack hardware navigation buttons
    private void addNavigationBar() {
        if (DEBUG) Slog.v(TAG, "addNavigationBar: about to add " + mNavigationBarView);
        if (mNavigationBarView == null) return;

        prepareNavigationBarView();

        mWindowManager.addView(mNavigationBarView, getNavigationBarLayoutParams());
    }

    private void repositionNavigationBar() {
        if (mNavigationBarView == null) return;

        prepareNavigationBarView();

        mWindowManager.updateViewLayout(mNavigationBarView, getNavigationBarLayoutParams());
    }

    private void notifyNavigationBarScreenOn(boolean screenOn) {
        if (mNavigationBarView == null) return;
        mNavigationBarView.notifyScreenOn(screenOn);
    }

    private WindowManager.LayoutParams getNavigationBarLayoutParams() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_NAVIGATION_BAR,
                    0
                    | WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
                PixelFormat.OPAQUE);
        // this will allow the navbar to run in an overlay on devices that support this
        if (ActivityManager.isHighEndGfx()) {
            lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        }

        lp.setTitle("NavigationBar");
        lp.windowAnimations = 0;
        return lp;
    }

    private void addIntruderView() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL, // above the status bar!
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                    | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.FILL_HORIZONTAL;
        //lp.y += height * 1.5; // FIXME
        lp.setTitle("IntruderAlert");
        lp.packageName = mContext.getPackageName();
        lp.windowAnimations = R.style.Animation_StatusBar_IntruderAlert;

        mWindowManager.addView(mIntruderAlertView, lp);
    }

    public void addIcon(String slot, int index, int viewIndex, StatusBarIcon icon) {
        if (SPEW) Slog.d(TAG, "addIcon slot=" + slot + " index=" + index + " viewIndex=" + viewIndex
                + " icon=" + icon);

        StatusBarIconView view = new StatusBarIconView(mContext, slot, null);
        view.set(icon);
		// Aurora <zhanggp> <2013-11-14> added for systemui begin
		if(isNotShowIcon(slot)){
			view.setVisibility(View.GONE);
		}
		// Aurora <zhanggp> <2013-11-14> added for systemui end
        mStatusIcons.addView(view, viewIndex, new LinearLayout.LayoutParams(mIconSize, mIconSize));
    }

    public void updateIcon(String slot, int index, int viewIndex,
            StatusBarIcon old, StatusBarIcon icon) {
        if (SPEW) Slog.d(TAG, "updateIcon slot=" + slot + " index=" + index + " viewIndex=" + viewIndex
                + " old=" + old + " icon=" + icon);
        StatusBarIconView view = (StatusBarIconView)mStatusIcons.getChildAt(viewIndex);
        view.set(icon);
		// Aurora <zhanggp> <2013-11-14> added for systemui begin
		if(isNotShowIcon(slot)){
			view.setVisibility(View.GONE);
		}
		// Aurora <zhanggp> <2013-11-14> added for systemui end
    }

    public void removeIcon(String slot, int index, int viewIndex) {
        if (SPEW) Slog.d(TAG, "removeIcon slot=" + slot + " index=" + index + " viewIndex=" + viewIndex);
        mStatusIcons.removeViewAt(viewIndex);
    }

    public void addNotification(IBinder key, StatusBarNotification notification) {
    	// Aurora <tongyh> <2013-10-19> delete the sansung msic notification begin
        if("com.sec.android.pagebuddynotisvc".equals(notification.pkg) || "com.qualcomm.logkit".equals(notification.pkg)){
        	return;
        }
        
        if(notification.notification.icon  == mContext.getResources().getSystem().getIdentifier("stat_notify_rssi_in_range", "drawable", "android")){
        	return;
        }
        
        // Aurora <tongyh> <2014-12-18> Use caution traffic begin
        if(notification.notification.icon  == mContext.getResources().getSystem().getIdentifier("stat_notify_error", "drawable", "android") 
        		&& "android".equals(notification.pkg) && notification.notification.tickerText.equals(mContext.getString(R.string.data_usage_warning_title))){
        	return;
        }
        // Aurora <tongyh> <2014-12-18> Use caution traffic end
        if(Build.MODEL.contains("I9500") || Build.MODEL.contains("SM-N90")){
        	int notification_wifi_id =  mContext.getResources().getSystem().getIdentifier("stat_notify_wifi_in_range", "drawable", "android");
            if(notification.id == notification_wifi_id){
            	return;
            }
        }
        

//        int notification_wifi_id = mContext.getResources().getSystem().getIdentifier("stat_notify_wifi_in_range", "drawable", "android");
//        if (notification.notification.icon == notification_wifi_id) {
//            return;
//        }

        // Aurora <tongyh> <2013-10-19> delete the sansung msic notification end
		// Aurora <zhanggp> <2013-10-19> added for systemui begin
		if(handleSpecialNotification(key,notification)){
			return;
		}
		// Aurora <zhanggp> <2013-10-19> added for systemui end		
        /// M: [ALPS00438070] Handle SD Swap Condition.
        /*if (FeatureOption.MTK_2SDCARD_SWAP) {
            try {
                ApplicationInfo applicationInfo = mContext.getPackageManager().getApplicationInfo(notification.pkg, 0);
                if ((applicationInfo.flags & applicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
                    if (mAvoidSDAppAddNotification) {
                        return;
                    }
                    mNeedRemoveKeys.add(key);
                    if (DEBUG) Slog.d(TAG, "applicationInfo pkg = " + notification.pkg + " to remove notification key = " + key);
                }
            } catch (NameNotFoundException e1) {
                e1.printStackTrace();
            }
        }*/

        if (DEBUG) Slog.d(TAG, "addNotification score=" + notification.score);
        StatusBarIconView iconView = addNotificationViews(key, notification);
        if (iconView == null) return;

        boolean immersive = false;
        try {
            immersive = ActivityManagerNative.getDefault().isTopActivityImmersive();
            if (DEBUG) {
                Slog.d(TAG, "Top activity is " + (immersive?"immersive":"not immersive"));
            }
        } catch (RemoteException ex) {
        }

        /*
         * DISABLED due to missing API
        if (ENABLE_INTRUDERS && (
                   // TODO(dsandler): Only if the screen is on
                notification.notification.intruderView != null)) {
            Slog.d(TAG, "Presenting high-priority notification");
            // special new transient ticker mode
            // 1. Populate mIntruderAlertView

            if (notification.notification.intruderView == null) {
                Slog.e(TAG, notification.notification.toString() + " wanted to intrude but intruderView was null");
                return;
            }

            // bind the click event to the content area
            PendingIntent contentIntent = notification.notification.contentIntent;
            final View.OnClickListener listener = (contentIntent != null)
                    ? new NotificationClicker(contentIntent,
                            notification.pkg, notification.tag, notification.id)
                    : null;

            mIntruderAlertView.applyIntruderContent(notification.notification.intruderView, listener);

            mCurrentlyIntrudingNotification = notification;

            // 2. Animate mIntruderAlertView in
            mHandler.sendEmptyMessage(MSG_SHOW_INTRUDER);

            // 3. Set alarm to age the notification off (TODO)
            mHandler.removeMessages(MSG_HIDE_INTRUDER);
            if (INTRUDER_ALERT_DECAY_MS > 0) {
                mHandler.sendEmptyMessageDelayed(MSG_HIDE_INTRUDER, INTRUDER_ALERT_DECAY_MS);
            }
        } else
         */

        if (notification.notification.fullScreenIntent != null) {
            // Stop screensaver if the notification has a full-screen intent.
            // (like an incoming phone call)
            awakenDreams();

            // not immersive & a full-screen alert should be shown
            if (DEBUG) Slog.d(TAG, "Notification has fullScreenIntent; sending fullScreenIntent");
            try {
                notification.notification.fullScreenIntent.send();
            } catch (PendingIntent.CanceledException e) {
            }
        } else {
            // usual case: status bar visible & not immersive

            // show the ticker if there isn't an intruder too
            if (mCurrentlyIntrudingNotification == null) {
                tick(null, notification, true);
            }
        }

        /// M: [SystemUI] Support "Dual SIM". {
//        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            // process SIM info of notification.
            /*
            SIMInfo simInfo = null;
            int simInfoType = notification.notification.simInfoType;
            long simId = notification.notification.simId;
            if ((simInfoType >= 1 || simInfoType <= 3) && simId > 0) {
                Xlog.d(TAG, "addNotificationViews, simInfoType=" + simInfoType + ", simId=" + simId + ".");
                simInfo = SIMHelper.getSIMInfo(mContext, simId);
            }
            if (simInfo != null) {
                NotificationData.Entry entry = mNotificationData.findByKey(key);
                updateNotificationSimInfo(simInfo, notification.notification, iconView, entry.expanded);
            }
            */
//        }
        /// M: [SystemUI] Support "Dual SIM". }
		
// Aurora <zhanggp> <2013-10-17> added for systemui begin
		refreshInCallState(notification);		
// Aurora <zhanggp> <2013-10-17> added for systemui end
		// Aurora <tongyh> <2014-04-11> Add alarm retreated to the background of green tips begin
		refreshAlarmClockState(notification);
		// Aurora <tongyh> <2014-04-11> Add alarm retreated to the background of green tips end
        // Recalculate the position of the sliding windows and the titles.
        setAreThereNotifications();
        updateExpandedViewPos(EXPANDED_LEAVE_ALONE);
    }

    public void removeNotification(IBinder key) {

		
        StatusBarNotification old = removeNotificationViews(key);
        if (SPEW) Slog.d(TAG, "removeNotification key=" + key + " old=" + old);

        /// M: [ALPS00438070] Handle SD Swap Condition.
        /*if (FeatureOption.MTK_2SDCARD_SWAP) {
            if (mNeedRemoveKeys.contains(key)) {
                mNeedRemoveKeys.remove(key);
            }
        }*/

        if (old != null) {
            // Cancel the ticker if it's still running
            mTicker.removeEntry(old);

            // Recalculate the position of the sliding windows and the titles.
            updateExpandedViewPos(EXPANDED_LEAVE_ALONE);

            if (ENABLE_INTRUDERS && old == mCurrentlyIntrudingNotification) {
                mHandler.sendEmptyMessage(MSG_HIDE_INTRUDER);
            }

            if (CLOSE_PANEL_WHEN_EMPTIED && mNotificationData.size() == 0 && !mAnimating) {
                animateCollapsePanels();
            }
			
			// Aurora <zhanggp> <2013-10-08> added for systemui begin
			if("com.android.phone".equals(old.pkg) && 2 == old.id){
				//mStatusBarView.setVisibility(View.VISIBLE);
				mInCallView.setVisibility(View.GONE);
                // Aurora <tongyh> <2014-09-16> incall view can drag statusbar begin
				isShowInCallView = false;
                // Aurora <tongyh> <2014-09-16> incall view can drag statusbar end
			}
			// Aurora <zhanggp> <2013-10-08> added for systemui end
			// Aurora <tongyh> <2014-04-11> Add alarm retreated to the background of green tips begin
			if("com.android.deskclock".equals(old.pkg) && 16220 == old.id){
				mAlarmClockView.setVisibility(View.GONE);
				// Aurora <tongyh> <2014-09-16> alarm clock statusbar's view can drag statusbar begin
				isShowAlarmClockView = false;
				// Aurora <tongyh> <2014-09-16> alarm clock statusbar's view can drag statusbar end
			}
			// Aurora <tongyh> <2014-04-11> Add alarm retreated to the background of green tips end
        }

		// Aurora <zhanggp> <2013-10-08> added for systemui begin
		if(View.VISIBLE == mInCallView.getVisibility()){
			TelephonyManager telManager = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
			if(TelephonyManager.CALL_STATE_IDLE == telManager.getDefault().getCallState()){
				//mStatusBarView.setVisibility(View.VISIBLE);
				mInCallView.setVisibility(View.GONE);
                // Aurora <tongyh> <2014-09-16> incall view can drag statusbar begin
				isShowInCallView = false;
                // Aurora <tongyh> <2014-09-16> incall view can drag statusbar end
			}
		}
		// Aurora <zhanggp> <2013-10-08> added for systemui end
        setAreThereNotifications();
    }

    private void updateShowSearchHoldoff() {
        mShowSearchHoldoff = mContext.getResources().getInteger(
            R.integer.config_show_search_delay);
    }

    private void loadNotificationShade() {
        if (mPile == null) return;

        int N = mNotificationData.size();

        ArrayList<View> toShow = new ArrayList<View>();
		// Aurora <zhanggp> <2013-10-08> added for systemui begin
		ArrayList<View> toShowUnClickable = new ArrayList<View>();;
		// Aurora <zhanggp> <2013-10-08> added for systemui end
        final boolean provisioned = isDeviceProvisioned();
        // If the device hasn't been through Setup, we only show system notifications
        for (int i=0; i<N; i++) {
            Entry ent = mNotificationData.get(N-i-1);
            if (!(provisioned || showNotificationEvenIfUnprovisioned(ent.notification))) continue;
            //Aurora <tongyh> <2014-11-05> add aurora privacymanage icon begin 
            if("com.aurora.privacymanage".equals(ent.notification.pkg) && ent.notification.id == 10000){
            	continue;
            }
            //Aurora <tongyh> <2014-11-05> add aurora privacymanage icon end
            if("com.android.email".equals(ent.notification.pkg) && ent.notification.id == 1000){
            	continue;
            }
            if (!notificationIsForCurrentUser(ent.notification)) continue;
            // Aurora <tongyh> <2014-06-10> the iuni music does not support the darg notice begin
            if("com.android.music".equals(ent.notification.pkg)){
            	toShow.add(0, ent.row);
            	continue;
            }
            // Aurora <tongyh> <2014-06-10> the iuni music does not support the darg notice end
			// Aurora <zhanggp> <2013-10-08> modified for systemui begin
			if(ent.notification.isClearable())
            	toShow.add(ent.row);
			else
				toShowUnClickable.add(ent.row);
            //toShow.add(ent.row);
			// Aurora <zhanggp> <2013-10-08> modified for systemui end
        }

        ArrayList<View> toRemove = new ArrayList<View>();
        for (int i=0; i<mPile.getChildCount(); i++) {
            View child = mPile.getChildAt(i);
			// Aurora <zhanggp> <2013-10-08> modified for systemui begin
            if (!toShow.contains(child)&&!toShowUnClickable.contains(child)) {
                toRemove.add(child);
            }
			/*
            if (!toShow.contains(child)) {
                toRemove.add(child);
            }
			*/
			// Aurora <zhanggp> <2013-10-08> modified for systemui end
        }

        for (View remove : toRemove) {
            mPile.removeView(remove);
        }

		
		// Aurora <zhanggp> <2013-10-08> added for systemui begin
		int toShowCount = toShow.size();	
		// Aurora <zhanggp> <2013-10-08> added for systemui end
        for (int i=0; i<toShow.size(); i++) {
            View v = toShow.get(i);
            if (v.getParent() == null) {
                mPile.addView(v, i);
            }
        }
		
		// Aurora <zhanggp> <2013-10-08> added for systemui begin
		for (int i=0; i<toShowUnClickable.size(); i++) {
			View v = toShowUnClickable.get(i);
			if (v.getParent() == null) {
				mPile.addView(v, toShowCount + i);
			}
		}
		// Aurora <zhanggp> <2013-10-08> added for systemui end
		
        if (mSettingsButton != null) {
            mSettingsButton.setEnabled(isDeviceProvisioned());
        }
    }

    @Override
    protected void updateNotificationIcons() {
        if (mNotificationIcons == null) return;

        loadNotificationShade();
        
        final LinearLayout.LayoutParams params
            = new LinearLayout.LayoutParams(mIconSize + 2*mIconHPadding, mNaturalBarHeight);
        int N = mNotificationData.size();

        if (DEBUG) {
            Slog.d(TAG, "refreshing icons: " + N + " notifications, mNotificationIcons=" + mNotificationIcons);
        }

        ArrayList<View> toShow = new ArrayList<View>();
        //Aurora <tongyh> <2014-11-05> add aurora privacymanage icon begin 
        ArrayList<View> mPrivacyView = new ArrayList<View>();
        //Aurora <tongyh> <2014-11-05> add aurora privacymanage icon end
		// Aurora <zhanggp> <2013-10-08> added for systemui begin
		ArrayList<Boolean> toMerge = new ArrayList<Boolean>();
		// Aurora <zhanggp> <2013-10-08> added for systemui end
		
        // M: StatusBar IconMerger feature, hash{pkg+icon}=iconlevel
        HashMap<String, Integer> uniqueIcon = new HashMap<String, Integer>();

        final boolean provisioned = isDeviceProvisioned();
        // If the device hasn't been through Setup, we only show system notifications
        for (int i=0; i<N; i++) {
            Entry ent = mNotificationData.get(N-i-1);
            if (!((provisioned && ent.notification.score >= HIDE_ICONS_BELOW_SCORE)
                    || showNotificationEvenIfUnprovisioned(ent.notification))) continue;
            if (!notificationIsForCurrentUser(ent.notification)) continue;

            // M: StatusBar IconMerger feature
            //Aurora <tongyh> <2014-10-09> qq mm can show one icon begin
//            String key = ent.notification.pkg + String.valueOf(ent.notification.notification.icon);
            String key = null;
            if("com.tencent.mobileqq".equals(ent.notification.pkg) 
					|| "com.tencent.qqlite".equals(ent.notification.pkg)
					|| "com.tencent.mm".equals(ent.notification.pkg)){
            	key = ent.notification.pkg;
			}else{
                key = ent.notification.pkg + String.valueOf(ent.notification.notification.icon);
			}
            //Aurora <tongyh> <2014-10-09> qq mm can show one icon end
            if (uniqueIcon.containsKey(key) && uniqueIcon.get(key) == ent.notification.notification.iconLevel
			// Aurora <zhanggp> <2013-10-29> added for systemui begin
				||!canShowIcon(ent.notification)) 
			// Aurora <zhanggp> <2013-10-29> added for systemui end
			{
            	// Aurora <tongyh> <2014-09-01> But the notification icon does not show the number plus one begin
            	if(canMerge(ent.notification)){
            		toMerge.add(new Boolean(ent.ToMerge));
            	}
            	// Aurora <tongyh> <2014-01-09> But the notification icon does not show the number plus one end
                Xlog.d(TAG, "updateNotificationIcons(), IconMerger feature, skip pkg / icon / iconlevel ="
                    + ent.notification.pkg + "/" + ent.notification.notification.icon + "/" + ent.notification.notification.iconLevel);
                continue;
            }
            if("com.tencent.mobileqq".equals(ent.notification.pkg) || "com.tencent.qqlite".equals(ent.notification.pkg)){
        		final StatusBarIconView iconView = new StatusBarIconView(mContext,
        				ent.notification.pkg + "/0x" + Integer.toHexString(ent.notification.id),
        				ent.notification.notification);
                iconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                int drawableId = R.drawable.aurora_qq;
                if(isSystemNotification(ent.notification)){
                	TextView tv = (TextView)ent.row.findViewById(getSystemTemplatesLineThreeId());
                	if(tv != null){
                		if(tv.getText().toString().contains(mContext.getString(R.string.qq_running_back))){
                			drawableId = R.drawable.aurora_qq_ongoing;
                		}
                	}
                }
                final StatusBarIcon ic = new StatusBarIcon(null,
                		ent.notification.user,
                		drawableId,
                            ent.notification.notification.iconLevel,
                            ent.notification.notification.number,
                            ent.notification.notification.tickerText);
                if (iconView.set(ic)) {
                	toShow.add(iconView);
                }
        		
        	}else if("com.tencent.mm".equals(ent.notification.pkg)){
        		final StatusBarIconView iconView = new StatusBarIconView(mContext,
        				ent.notification.pkg + "/0x" + Integer.toHexString(ent.notification.id),
        				ent.notification.notification);
                iconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

                final StatusBarIcon ic = new StatusBarIcon(null,
                		ent.notification.user,
                		R.drawable.aurora_weixin,
                            ent.notification.notification.iconLevel,
                            ent.notification.notification.number,
                            ent.notification.notification.tickerText);
                if (iconView.set(ic)) {
                	toShow.add(iconView);
                }
        	}else if("com.android.mms".equals(ent.notification.pkg)){
        		final StatusBarIconView iconView = new StatusBarIconView(mContext,
        				ent.notification.pkg + "/0x" + Integer.toHexString(ent.notification.id),
        				ent.notification.notification);
                iconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

                final StatusBarIcon ic = new StatusBarIcon(null,
                		ent.notification.user,
                		R.drawable.aurora_message,
                            ent.notification.notification.iconLevel,
                            ent.notification.notification.number,
                            ent.notification.notification.tickerText);
                if (iconView.set(ic)) {
                	toShow.add(iconView);
                }
        	}else{
        		//Aurora <tongyh> <2014-11-05> add aurora privacymanage icon begin 
        		if("com.aurora.privacymanage".equals(ent.notification.pkg) && ent.notification.id == 10000){
        			mPrivacyView.add(ent.icon);
        		}else{
        			toShow.add(ent.icon);
        		}
        		//Aurora <tongyh> <2014-10-21> add aurora privacymanage icon end
        	}
        	
			// Aurora <zhanggp> <2013-10-08> added for systemui begin
			toMerge.add(new Boolean(ent.ToMerge));
			// Aurora <zhanggp> <2013-10-08> added for systemui end
			//Aurora <tongyh> <2014-10-09> qq mm can show one icon begin
//			uniqueIcon.put(key, ent.notification.notification.iconLevel);
			if("com.tencent.mobileqq".equals(ent.notification.pkg) 
					|| "com.tencent.qqlite".equals(ent.notification.pkg)
					|| "com.tencent.mm".equals(ent.notification.pkg)){
				uniqueIcon.put(ent.notification.pkg, ent.notification.notification.iconLevel);
			}else{
                uniqueIcon.put(key, ent.notification.notification.iconLevel);
			}
			//Aurora <tongyh> <2014-10-09> qq mm can show one icon end
        }
        uniqueIcon = null;

        ArrayList<View> toRemove = new ArrayList<View>();
        for (int i=0; i<mNotificationIcons.getChildCount(); i++) {
            View child = mNotificationIcons.getChildAt(i);
          //Aurora <tongyh> <2014-11-19> add aurora privacymanage icon begin 
            if (!toShow.contains(child)) {
            	if(!mPrivacyView.contains(child)){
            		toRemove.add(child);
            	}
            }
          //Aurora <tongyh> <2014-11-19> add aurora privacymanage icon end
        }

        for (View remove : toRemove) {
            mNotificationIcons.removeView(remove);
        }
		// Aurora <zhanggp> <2013-10-08> added for systemui begin
		int MergeIconNum = 0;
		int index = mNotificationIcons.getChildCount();
		// Aurora <zhanggp> <2013-10-08> added for systemui end
        for (int i=0; i<toShow.size(); i++) {
            View v = toShow.get(i);
            if (v.getParent() == null) {
				// Aurora <zhanggp> <2013-10-08> modified for systemui begin
//				if(toMerge.get(i).booleanValue()){
////					++MergeIconNum;
//					continue;
//				}
				
                mNotificationIcons.addView(v, index++, params);
				//mNotificationIcons.addView(v, i, params);
				// Aurora <zhanggp> <2013-10-08> modified for systemui end
            }
        }
     // Aurora <tongyh> <2014-09-19> the status bar icon code logic changes begin
        for(int i=0; i< toMerge.size(); i++){
        	if(toMerge.get(i).booleanValue()){
			    ++MergeIconNum;
		    }
        }
      // Aurora <tongyh> <2014-09-19> the status bar icon code logic changes end 
      //Aurora <tongyh> <2014-11-19> add aurora privacymanage icon begin 
        for (int i=0; i<mPrivacyView.size(); i++) {
        	View v = mPrivacyView.get(i);
        	if (v.getParent() == null) {
        		mNotificationIcons.addView(v, 0, params);
        	}
        }
        //Aurora <tongyh> <2014-11-19> add aurora privacymanage icon end
		// Aurora <zhanggp> <2013-10-08> added for systemui begin
		if(MergeIconNum > 0){
			View sv = getSpecialIconView(MergeIconNum);
			//Aurora <tongyh> <2014-11-19> add aurora privacymanage icon begin 
			int i = 0;
			if(mPrivacyView.size() > 0){
				i = 1;
			}
			mNotificationIcons.addView(sv,i,params);
			//Aurora <tongyh> <2014-11-19> add aurora privacymanage icon end 
		}
		// Aurora <zhanggp> <2013-10-08> added for systemui end
    }

    protected void updateCarrierLabelVisibility(boolean force) {
        if (!mShowCarrierInPanel) return;
        // The idea here is to only show the carrier label when there is enough room to see it, 
        // i.e. when there aren't enough notifications to fill the panel.
        if (DEBUG) {
            Slog.d(TAG, String.format("pileh=%d scrollh=%d carrierh=%d",
                    mPile.getHeight(), mScrollView.getHeight(), mCarrierLabelHeight));
        }

        final boolean emergencyCallsShownElsewhere = mEmergencyCallLabel != null;
        boolean makeVisible = false;
        /// M: Calculate ToolBar height when sim indicator is showing.
        /// M: Fix [ALPS00455548] Use getExpandedHeight instead of getHeight to avoid race condition.
        // Aurora <tongyh> <2014-04-09> add CarrierLabel begin
//        int height = mToolBarSwitchPanel.getVisibility() == View.VISIBLE ? 
        int height = false ?
        // Aurora <tongyh> <2014-04-09> add CarrierLabel end
                ((int)mNotificationPanel.getExpandedHeight() - mCarrierLabelHeight - mNotificationHeaderHeight - mToolBarViewHeight)
                : ((int)mNotificationPanel.getExpandedHeight() - mCarrierLabelHeight - mNotificationHeaderHeight);
        // Gionee <fengjianyi><2013-05-13> add for CR00800567 start
        if (ToolbarIconUtils.GN_QUICK_SETTINGS_SUPPORT) {
            int orientation = mContext.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            	height -= mNotificationRadioButton.getHeight();
            }
        }
        // Gionee <fengjianyi><2013-05-13> add for CR00800567 end
        /// M: Support "Dual Sim" @{
        /*if (FeatureOption.MTK_GEMINI_SUPPORT) {
            makeVisible =
                mPile.getHeight() < height && mScrollView.getVisibility() == View.VISIBLE;
        } else {*/
            makeVisible =
            // Aurora <tongyh> <2014-04-09> add CarrierLabel begin
//                !(emergencyCallsShownElsewhere && mNetworkController.isEmergencyOnly())
//                && mPile.getHeight() < height && mScrollView.getVisibility() == View.VISIBLE;
            		true;
           // Aurora <tongyh> <2014-04-09> add CarrierLabel end
//        }
        /// M: Support "Dual Sim" @}
        if (force || mCarrierLabelVisible != makeVisible) {
            mCarrierLabelVisible = makeVisible;
            if (DEBUG) {
                Slog.d(TAG, "making carrier label " + (makeVisible?"visible":"invisible"));
            }
            /// M: Support "Dual Sim" @{
            /*if (FeatureOption.MTK_GEMINI_SUPPORT) {
                mCarrierLabelGemini.animate().cancel();
                if (makeVisible) {
                    mCarrierLabelGemini.setVisibility(View.VISIBLE);
                }
                mCarrierLabelGemini.animate()
                    .alpha(makeVisible ? 1f : 0f)
                    .setDuration(150)
                    .setListener(makeVisible ? null : new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (!mCarrierLabelVisible) { // race
                                mCarrierLabelGemini.setVisibility(View.INVISIBLE);
                                mCarrierLabelGemini.setAlpha(0f);
                            }
                        }
                    })
                    .start();
            /// M: Support "Dual Sim" @{
            } else {*/
                mCarrierLabel.animate().cancel();
                if (makeVisible) {
                    mCarrierLabel.setVisibility(View.VISIBLE);
                }
                mCarrierLabel.animate()
                    .alpha(makeVisible ? 1f : 0f)
                    //.setStartDelay(makeVisible ? 500 : 0)
                    //.setDuration(makeVisible ? 750 : 100)
                    .setDuration(150)
                    .setListener(makeVisible ? null : new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (!mCarrierLabelVisible) { // race
                                mCarrierLabel.setVisibility(View.INVISIBLE);
                                mCarrierLabel.setAlpha(0f);
                            }
                        }
                    })
                    .start();
//            }
        }
    }

    @Override
    protected void setAreThereNotifications() {
        // Gionee <fengjianyi><2013-05-10> add for CR00800567 start
    	if (ToolbarIconUtils.GN_QUICK_SETTINGS_SUPPORT) {
    		gnSetAreThereNotifications();
    		return;
    	}
        // Gionee <fengjianyi><2013-05-10> add for CR00800567 end
        final boolean any = mNotificationData.size() > 0;

        final boolean clearable = any && mNotificationData.hasClearableItems();

        if (DEBUG) {
            Slog.d(TAG, "setAreThereNotifications: N=" + mNotificationData.size()
                    + " any=" + any + " clearable=" + clearable);
        }

        if (mHasFlipSettings 
                && mFlipSettingsView != null 
                && mFlipSettingsView.getVisibility() == View.VISIBLE
                && mScrollView.getVisibility() != View.VISIBLE) {
            // the flip settings panel is unequivocally showing; we should not be shown
            mClearButton.setVisibility(View.GONE);
        } else if (mClearButton.isShown()) {
            if (clearable != (mClearButton.getAlpha() == 1.0f)) {
                ObjectAnimator clearAnimation = ObjectAnimator.ofFloat(
                        mClearButton, "alpha", clearable ? 1.0f : 0.0f).setDuration(250);
                clearAnimation.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (mClearButton.getAlpha() <= 0.0f) {
                            mClearButton.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (mClearButton.getAlpha() <= 0.0f) {
                            mClearButton.setVisibility(View.VISIBLE);
                        }
                    }
                });
                clearAnimation.start();
            }
        } else {
			// Aurora <zhanggp> <2013-11-14> modified for systemui begin
        	if(clearable && !mNotificationPanel.isFullyExpanded()){
				
			}else{
            	mClearButton.setAlpha(clearable ? 1.0f : 0.0f);
            	mClearButton.setVisibility(clearable ? View.VISIBLE : View.GONE);
        	}
			/*
			mClearButton.setAlpha(clearable ? 1.0f : 0.0f);
			mClearButton.setVisibility(clearable ? View.VISIBLE : View.GONE);
			*/
			// Aurora <zhanggp> <2013-11-14> modified for systemui end
        }
        mClearButton.setEnabled(clearable);

        final View nlo = mStatusBarView.findViewById(R.id.notification_lights_out);
        final boolean showDot = (any&&!areLightsOn());
        if (showDot != (nlo.getAlpha() == 1.0f)) {
            if (showDot) {
                nlo.setAlpha(0f);
                nlo.setVisibility(View.VISIBLE);
            }
            nlo.animate()
                .alpha(showDot?1:0)
                .setDuration(showDot?750:250)
                .setInterpolator(new AccelerateInterpolator(2.0f))
                .setListener(showDot ? null : new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator _a) {
                        nlo.setVisibility(View.GONE);
                    }
                })
                .start();
        }

        updateCarrierLabelVisibility(false);
    }
    
    // Gionee <fengjianyi><2013-05-10> add for CR00800567 start
    private void gnSetAreThereNotifications() {
        final boolean any = mNotificationData.size() > 0;

        final boolean clearable = any && mNotificationData.hasClearableItems();

        if (DEBUG) {
            Slog.d(TAG, "gnSetAreThereNotifications: N=" + mNotificationData.size()
                    + " any=" + any + " clearable=" + clearable);
        }

        if (mPanelPager != null && mPanelPager.getCurrentItem() == 1) {
            // the flip settings panel is unequivocally showing; we should not be shown
            mClearButton.setVisibility(View.GONE);
        } else if (mClearButton.isShown()) {
            if (clearable != (mClearButton.getAlpha() == 1.0f)) {
                ObjectAnimator clearAnimation = ObjectAnimator.ofFloat(
                        mClearButton, "alpha", clearable ? 1.0f : 0.0f).setDuration(250);
                clearAnimation.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (mClearButton.getAlpha() <= 0.0f) {
                            mClearButton.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (mClearButton.getAlpha() <= 0.0f) {
                            mClearButton.setVisibility(View.VISIBLE);
                        }
                    }
                });
                clearAnimation.start();
            }
        } else {
            mClearButton.setAlpha(clearable ? 1.0f : 0.0f);
            mClearButton.setVisibility(clearable ? View.VISIBLE : View.GONE);
        }
        mClearButton.setEnabled(clearable);

        final View nlo = mStatusBarView.findViewById(R.id.notification_lights_out);
        final boolean showDot = (any&&!areLightsOn());
        if (showDot != (nlo.getAlpha() == 1.0f)) {
            if (showDot) {
                nlo.setAlpha(0f);
                nlo.setVisibility(View.VISIBLE);
            }
            nlo.animate()
                .alpha(showDot?1:0)
                .setDuration(showDot?750:250)
                .setInterpolator(new AccelerateInterpolator(2.0f))
                .setListener(showDot ? null : new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator _a) {
                        nlo.setVisibility(View.GONE);
                    }
                })
                .start();
        }

        updateCarrierLabelVisibility(false);
    }
    // Gionee <fengjianyi><2013-05-10> add for CR00800567 end

    public void showClock(boolean show) {
        if (mStatusBarView == null) return;
        View clock = mStatusBarView.findViewById(R.id.clock);
        if (clock != null) {
            clock.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * State is one or more of the DISABLE constants from StatusBarManager.
     */
    public void disable(int state) {
        final int old = mDisabled;
        final int diff = state ^ old;
        mDisabled = state;

        if (DEBUG) {
            Slog.d(TAG, String.format("disable: 0x%08x -> 0x%08x (diff: 0x%08x)",
                old, state, diff));
        }

        StringBuilder flagdbg = new StringBuilder();
        flagdbg.append("disable: < ");
        flagdbg.append(((state & StatusBarManager.DISABLE_EXPAND) != 0) ? "EXPAND" : "expand");
        flagdbg.append(((diff  & StatusBarManager.DISABLE_EXPAND) != 0) ? "* " : " ");
        flagdbg.append(((state & StatusBarManager.DISABLE_NOTIFICATION_ICONS) != 0) ? "ICONS" : "icons");
        flagdbg.append(((diff  & StatusBarManager.DISABLE_NOTIFICATION_ICONS) != 0) ? "* " : " ");
        flagdbg.append(((state & StatusBarManager.DISABLE_NOTIFICATION_ALERTS) != 0) ? "ALERTS" : "alerts");
        flagdbg.append(((diff  & StatusBarManager.DISABLE_NOTIFICATION_ALERTS) != 0) ? "* " : " ");
        flagdbg.append(((state & StatusBarManager.DISABLE_NOTIFICATION_TICKER) != 0) ? "TICKER" : "ticker");
        flagdbg.append(((diff  & StatusBarManager.DISABLE_NOTIFICATION_TICKER) != 0) ? "* " : " ");
        flagdbg.append(((state & StatusBarManager.DISABLE_SYSTEM_INFO) != 0) ? "SYSTEM_INFO" : "system_info");
        flagdbg.append(((diff  & StatusBarManager.DISABLE_SYSTEM_INFO) != 0) ? "* " : " ");
        flagdbg.append(((state & StatusBarManager.DISABLE_BACK) != 0) ? "BACK" : "back");
        flagdbg.append(((diff  & StatusBarManager.DISABLE_BACK) != 0) ? "* " : " ");
        flagdbg.append(((state & StatusBarManager.DISABLE_HOME) != 0) ? "HOME" : "home");
        flagdbg.append(((diff  & StatusBarManager.DISABLE_HOME) != 0) ? "* " : " ");
        flagdbg.append(((state & StatusBarManager.DISABLE_RECENT) != 0) ? "RECENT" : "recent");
        flagdbg.append(((diff  & StatusBarManager.DISABLE_RECENT) != 0) ? "* " : " ");
        flagdbg.append(((state & StatusBarManager.DISABLE_CLOCK) != 0) ? "CLOCK" : "clock");
        flagdbg.append(((diff  & StatusBarManager.DISABLE_CLOCK) != 0) ? "* " : " ");
        flagdbg.append(((state & StatusBarManager.DISABLE_SEARCH) != 0) ? "SEARCH" : "search");
        flagdbg.append(((diff  & StatusBarManager.DISABLE_SEARCH) != 0) ? "* " : " ");
        flagdbg.append(">");
        Slog.d(TAG, flagdbg.toString());

        if ((diff & StatusBarManager.DISABLE_SYSTEM_INFO) != 0) {
            mSystemIconArea.animate().cancel();
            if ((state & StatusBarManager.DISABLE_SYSTEM_INFO) != 0) {
                mSystemIconArea.animate()
                    .alpha(0f)
                    .translationY(mNaturalBarHeight*0.5f)
                    .setDuration(175)
                    .setInterpolator(new DecelerateInterpolator(1.5f))
                    .setListener(mMakeIconsInvisible)
                    .start();
            } else {
                mSystemIconArea.setVisibility(View.VISIBLE);
                mSystemIconArea.animate()
                    .alpha(1f)
                    .translationY(0)
                    .setStartDelay(0)
                    .setInterpolator(new DecelerateInterpolator(1.5f))
                    .setDuration(175)
                    .start();
            }
        }

        if ((diff & StatusBarManager.DISABLE_CLOCK) != 0) {
            boolean show = (state & StatusBarManager.DISABLE_CLOCK) == 0;
            showClock(show);
        }
        if ((diff & StatusBarManager.DISABLE_EXPAND) != 0) {
            if ((state & StatusBarManager.DISABLE_EXPAND) != 0) {
                animateCollapsePanels();
            }
        }

        if ((diff & (StatusBarManager.DISABLE_HOME
                        | StatusBarManager.DISABLE_RECENT
                        | StatusBarManager.DISABLE_BACK
                        | StatusBarManager.DISABLE_SEARCH)) != 0) {
            // the nav bar will take care of these
            if (mNavigationBarView != null) mNavigationBarView.setDisabledFlags(state);

            if ((state & StatusBarManager.DISABLE_RECENT) != 0) {
                // close recents if it's visible
                mHandler.removeMessages(MSG_CLOSE_RECENTS_PANEL);
                mHandler.sendEmptyMessage(MSG_CLOSE_RECENTS_PANEL);
            }
        }

        if ((diff & StatusBarManager.DISABLE_NOTIFICATION_ICONS) != 0) {
            if ((state & StatusBarManager.DISABLE_NOTIFICATION_ICONS) != 0) {
                if (mTicking) {
                    haltTicker();
                }

                mNotificationIcons.animate()
                    .alpha(0f)
                    .translationY(mNaturalBarHeight*0.5f)
                    .setDuration(175)
                    .setInterpolator(new DecelerateInterpolator(1.5f))
                    .setListener(mMakeIconsInvisible)
                    .start();
            } else {
                mNotificationIcons.setVisibility(View.VISIBLE);
                mNotificationIcons.animate()
                    .alpha(1f)
                    .translationY(0)
                    .setStartDelay(0)
                    .setInterpolator(new DecelerateInterpolator(1.5f))
                    .setDuration(175)
                    .start();
            }
        } else if ((diff & StatusBarManager.DISABLE_NOTIFICATION_TICKER) != 0) {
            if (mTicking && (state & StatusBarManager.DISABLE_NOTIFICATION_TICKER) != 0) {
                haltTicker();
            }
        }
    }

    @Override
    protected BaseStatusBar.H createHandler() {
        return new PhoneStatusBar.H();
    }

    /**
     * All changes to the status bar and notifications funnel through here and are batched.
     */
    private class H extends BaseStatusBar.H {
        public void handleMessage(Message m) {
            super.handleMessage(m);
            switch (m.what) {
                case MSG_OPEN_NOTIFICATION_PANEL:
                    animateExpandNotificationsPanel();
                    break;
                case MSG_OPEN_SETTINGS_PANEL:
                    animateExpandSettingsPanel();
                    break;
                case MSG_CLOSE_PANELS:
                    animateCollapsePanels();
                    break;
                case MSG_SHOW_INTRUDER:
                    setIntruderAlertVisibility(true);
                    break;
                case MSG_HIDE_INTRUDER:
                    setIntruderAlertVisibility(false);
                    mCurrentlyIntrudingNotification = null;
                    break;
            }
        }
    }

    public Handler getHandler() {
        return mHandler;
    }

    View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            // Because 'v' is a ViewGroup, all its children will be (un)selected
            // too, which allows marqueeing to work.
            v.setSelected(hasFocus);
        }
    };

    void makeExpandedVisible(boolean revealAfterDraw) {
        if (SPEW) Slog.d(TAG, "Make expanded visible: expanded visible=" + mExpandedVisible);
        if (mExpandedVisible) {
            return;
        }

        mExpandedVisible = true;
        mPile.setLayoutTransitionsEnabled(true);
        if (mNavigationBarView != null)
            mNavigationBarView.setSlippery(true);

        updateCarrierLabelVisibility(true);

        updateExpandedViewPos(EXPANDED_LEAVE_ALONE);

        // Expand the window to encompass the full screen in anticipation of the drag.
        // This is only possible to do atomically because the status bar is at the top of the screen!
        WindowManager.LayoutParams lp = (WindowManager.LayoutParams) mStatusBarWindow.getLayoutParams();
        lp.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        mWindowManager.updateViewLayout(mStatusBarWindow, lp);

        // Updating the window layout will force an expensive traversal/redraw.
        // Kick off the reveal animation after this is complete to avoid animation latency.
        if (revealAfterDraw) {
//            mHandler.post(mStartRevealAnimation);
        }

        /// M: Show always update clock of DateView.
        if (mDateView != null) {
            mDateView.updateClock();
        }
        visibilityChanged(true);
    }

    public void animateCollapsePanels() {
        animateCollapsePanels(CommandQueue.FLAG_EXCLUDE_NONE);
    }

    public void animateCollapsePanels(int flags) {
        if (SPEW) {
            Slog.d(TAG, "animateCollapse():"
                    + " mExpandedVisible=" + mExpandedVisible
                    + " mAnimating=" + mAnimating
                    + " mAnimatingReveal=" + mAnimatingReveal
                    + " mAnimY=" + mAnimY
                    + " mAnimVel=" + mAnimVel
                    + " flags=" + flags);
        }

        if ((flags & CommandQueue.FLAG_EXCLUDE_RECENTS_PANEL) == 0) {
            mHandler.removeMessages(MSG_CLOSE_RECENTS_PANEL);
            mHandler.sendEmptyMessage(MSG_CLOSE_RECENTS_PANEL);
        }

        if ((flags & CommandQueue.FLAG_EXCLUDE_SEARCH_PANEL) == 0) {
            mHandler.removeMessages(MSG_CLOSE_SEARCH_PANEL);
            mHandler.sendEmptyMessage(MSG_CLOSE_SEARCH_PANEL);
        }

        mStatusBarWindow.cancelExpandHelper();
		// Aurora <zhanggp> <2013-11-15> added for systemui start
		hideClearButton();
		// Aurora <zhanggp> <2013-11-15> added for systemui end
        mStatusBarView.collapseAllPanels(true);
    }

    public ViewPropertyAnimator setVisibilityWhenDone(
            final ViewPropertyAnimator a, final View v, final int vis) {
        a.setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                v.setVisibility(vis);
                a.setListener(null); // oneshot
            }
        });
        return a;
    }

    public Animator setVisibilityWhenDone(
            final Animator a, final View v, final int vis) {
        a.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                v.setVisibility(vis);
            }
        });
        return a;
    }

    public Animator interpolator(TimeInterpolator ti, Animator a) {
        a.setInterpolator(ti);
        return a;
    }

    public Animator startDelay(int d, Animator a) {
        a.setStartDelay(d);
        return a;
    }
    
    public Animator start(Animator a) {
        a.start();
        return a;
    }

    final TimeInterpolator mAccelerateInterpolator = new AccelerateInterpolator();
    final TimeInterpolator mDecelerateInterpolator = new DecelerateInterpolator();
    final int FLIP_DURATION_OUT = 125;
    final int FLIP_DURATION_IN = 225;
    final int FLIP_DURATION = (FLIP_DURATION_IN + FLIP_DURATION_OUT);

    Animator mScrollViewAnim, mFlipSettingsViewAnim, mNotificationButtonAnim,
        mSettingsButtonAnim, mClearButtonAnim;
    /// M: [SystemUI] Remove settings button to notification header.
    private Animator mHeaderSettingsButtonAnim;

    @Override
    public void animateExpandNotificationsPanel() {
        if (SPEW) Slog.d(TAG, "animateExpand: mExpandedVisible=" + mExpandedVisible);
        if ((mDisabled & StatusBarManager.DISABLE_EXPAND) != 0) {
            return ;
        }
        mNotificationPanel.expand();
        if (mHasFlipSettings && mScrollView.getVisibility() != View.VISIBLE) {
            flipToNotifications();
        }

        if (false) postStartTracing();
    }
    
    // M: To expand slowly than usual.
    private void animateExpandNotificationsPanelSlow() {
        Slog.d(TAG, "animateExpandSlow: mExpandedVisible=" + mExpandedVisible);
        if ((mDisabled & StatusBarManager.DISABLE_EXPAND) != 0) {
            return ;
        }

        mNotificationPanel.expandSlow();
        if (mHasFlipSettings && mScrollView.getVisibility() != View.VISIBLE) {
            flipToNotifications();
        }

        if (false) postStartTracing();
    }

    public void flipToNotifications() {
        // Gionee <fengjianyi><2013-05-10> add for CR00800567 start
    	if (ToolbarIconUtils.GN_QUICK_SETTINGS_SUPPORT) {
    		mPanelPager.setCurrentItem(0);
    		return;
    	}
        // Gionee <fengjianyi><2013-05-10> add for CR00800567 end
        if (mFlipSettingsViewAnim != null) mFlipSettingsViewAnim.cancel();
        if (mScrollViewAnim != null) mScrollViewAnim.cancel();
        if (mSettingsButtonAnim != null) mSettingsButtonAnim.cancel();
        if (mNotificationButtonAnim != null) mNotificationButtonAnim.cancel();
        if (mClearButtonAnim != null) mClearButtonAnim.cancel();
        /// M: [SystemUI] Remove settings button to notification header @{.
        if (mHeaderSettingsButtonAnim != null) {
            mHeaderSettingsButtonAnim.cancel();
        }
        /// M: [SystemUI] Remove settings button to notification header @}.
        mScrollView.setVisibility(View.VISIBLE);
        mScrollViewAnim = start(
            startDelay(FLIP_DURATION_OUT,
                interpolator(mDecelerateInterpolator,
                    ObjectAnimator.ofFloat(mScrollView, View.SCALE_X, 0f, 1f)
                        .setDuration(FLIP_DURATION_IN)
                    )));
        mFlipSettingsViewAnim = start(
            setVisibilityWhenDone(
                interpolator(mAccelerateInterpolator,
                        ObjectAnimator.ofFloat(mFlipSettingsView, View.SCALE_X, 1f, 0f)
                        )
                    .setDuration(FLIP_DURATION_OUT),
                mFlipSettingsView, View.INVISIBLE));
        mNotificationButtonAnim = start(
            setVisibilityWhenDone(
                ObjectAnimator.ofFloat(mNotificationButton, View.ALPHA, 0f)
                    .setDuration(FLIP_DURATION),
                mNotificationButton, View.INVISIBLE));
        mSettingsButton.setVisibility(View.VISIBLE);
        mSettingsButtonAnim = start(
            ObjectAnimator.ofFloat(mSettingsButton, View.ALPHA, 1f)
                .setDuration(FLIP_DURATION));
        mClearButton.setVisibility(View.VISIBLE);
        mClearButton.setAlpha(0f);
        setAreThereNotifications(); // this will show/hide the button as necessary
        mNotificationPanel.postDelayed(new Runnable() {
            public void run() {
                updateCarrierLabelVisibility(false);
            }
        }, FLIP_DURATION - 150);
        /// M: [SystemUI] Remove settings button to notification header @{.
        if (mHeaderSettingsButton != null) {
            mHeaderSettingsButton.setVisibility(View.GONE);
        }
        /// M: [SystemUI] Remove settings button to notification header @}.
        /// M: [SystemUI] Support SimIndicator, show SimIndicator when notification panel is visible. @{
        //if (mToolBarView.mSimSwitchPanelView.isPanelShowing()) {
        //    mToolBarSwitchPanel.setVisibility(View.VISIBLE);
        //}
        /// M: [SystemUI] Support SimIndicator, show SimIndicator when notification panel is visible. @{
    }

    @Override
    public void animateExpandSettingsPanel() {
        if (SPEW) Slog.d(TAG, "animateExpand: mExpandedVisible=" + mExpandedVisible);
        if ((mDisabled & StatusBarManager.DISABLE_EXPAND) != 0) {
            return;
        }
        if (mHasFlipSettings) {
            mNotificationPanel.expand();
            if (mFlipSettingsView.getVisibility() != View.VISIBLE) {
                flipToSettings();
            }
        } //else if (mSettingsPanel != null) {
          //  mSettingsPanel.expand();
        //}

        if (false) postStartTracing();
    }

    public void switchToSettings() {
        // Gionee <fengjianyi><2013-05-10> add for CR00800567 start
    	if (ToolbarIconUtils.GN_QUICK_SETTINGS_SUPPORT) {
    		mPanelPager.setCurrentItem(1);
    		return;
    	}
        // Gionee <fengjianyi><2013-05-10> add for CR00800567 end
        mFlipSettingsView.setScaleX(1f);
        mFlipSettingsView.setVisibility(View.VISIBLE);
        mSettingsButton.setVisibility(View.GONE);
        mScrollView.setVisibility(View.GONE);
        mScrollView.setScaleX(0f);
        mNotificationButton.setVisibility(View.VISIBLE);
        mNotificationButton.setAlpha(1f);
        mClearButton.setVisibility(View.GONE);
        /// M: [SystemUI] Remove settings button to notification header @{.
        if (mHeaderSettingsButton != null) {
            mHeaderSettingsButton.setVisibility(View.VISIBLE);
        }
        /// M: [SystemUI] Remove settings button to notification header @}.
        /// M: [SystemUI] Support SimIndicator, hide SimIndicator when settings panel is visible.
        mToolBarSwitchPanel.setVisibility(View.GONE);
    }

    public void flipToSettings() {
        // Gionee <fengjianyi><2013-05-10> add for CR00800567 start
    	if (ToolbarIconUtils.GN_QUICK_SETTINGS_SUPPORT) {
            mClearButtonAnim = start(
                    setVisibilityWhenDone(
                        ObjectAnimator.ofFloat(mClearButton, View.ALPHA, 0f)
                        .setDuration(FLIP_DURATION),
                        mClearButton, View.GONE));
            
            if (mHeaderSettingsButton != null) {
                mHeaderSettingsButtonAnim = start(
                        setVisibilityWhenDone(
                                ObjectAnimator.ofFloat(mHeaderSettingsButton, View.ALPHA, 1f)
                                .setDuration(FLIP_DURATION),
                                mHeaderSettingsButton, View.VISIBLE));
            }
    		mPanelPager.setCurrentItem(1);
    		return;
    	}
        // Gionee <fengjianyi><2013-05-10> add for CR00800567 end
        if (mFlipSettingsViewAnim != null) mFlipSettingsViewAnim.cancel();
        if (mScrollViewAnim != null) mScrollViewAnim.cancel();
        if (mSettingsButtonAnim != null) mSettingsButtonAnim.cancel();
        if (mNotificationButtonAnim != null) mNotificationButtonAnim.cancel();
        if (mClearButtonAnim != null) mClearButtonAnim.cancel();
        /// M: [SystemUI] Remove settings button to notification header @{.
        if (mHeaderSettingsButtonAnim != null) {
            mHeaderSettingsButtonAnim.cancel();
        }
        /// M: [SystemUI] Remove settings button to notification header @}.
        mFlipSettingsView.setVisibility(View.VISIBLE);
        mFlipSettingsView.setScaleX(0f);
        mFlipSettingsViewAnim = start(
            startDelay(FLIP_DURATION_OUT,
                interpolator(mDecelerateInterpolator,
                    ObjectAnimator.ofFloat(mFlipSettingsView, View.SCALE_X, 0f, 1f)
                        .setDuration(FLIP_DURATION_IN)
                    )));
        mScrollViewAnim = start(
            setVisibilityWhenDone(
                interpolator(mAccelerateInterpolator,
                        ObjectAnimator.ofFloat(mScrollView, View.SCALE_X, 1f, 0f)
                        )
                    .setDuration(FLIP_DURATION_OUT), 
                mScrollView, View.INVISIBLE));
        mSettingsButtonAnim = start(
            setVisibilityWhenDone(
                ObjectAnimator.ofFloat(mSettingsButton, View.ALPHA, 0f)
                    .setDuration(FLIP_DURATION),
                    mScrollView, View.INVISIBLE));
        mNotificationButton.setVisibility(View.VISIBLE);
        mNotificationButtonAnim = start(
            ObjectAnimator.ofFloat(mNotificationButton, View.ALPHA, 1f)
                .setDuration(FLIP_DURATION));
        mClearButtonAnim = start(
            setVisibilityWhenDone(
                ObjectAnimator.ofFloat(mClearButton, View.ALPHA, 0f)
                .setDuration(FLIP_DURATION),
                mClearButton, View.GONE));
        /// M: [SystemUI] Remove settings button to notification header @{.
        if (mHeaderSettingsButton != null) {
            mHeaderSettingsButtonAnim = start(
                    setVisibilityWhenDone(
                            ObjectAnimator.ofFloat(mHeaderSettingsButton, View.ALPHA, 1f)
                            .setDuration(FLIP_DURATION),
                            mHeaderSettingsButton, View.VISIBLE));
        }
        /// M: [SystemUI] Remove settings button to notification header @}.
        mNotificationPanel.postDelayed(new Runnable() {
            public void run() {
                updateCarrierLabelVisibility(false);
            }
        }, FLIP_DURATION - 150);
        /// M: [SystemUI] Support SimIndicator, hide SimIndicator when settings panel is visible.
        mToolBarSwitchPanel.setVisibility(View.GONE);
    }

    public void flipPanels() {
        if (mHasFlipSettings) {
            if (mFlipSettingsView.getVisibility() != View.VISIBLE) {
                flipToSettings();
            } else {
                flipToNotifications();
            }
        }
    }

    public void animateCollapseQuickSettings() {
		// Aurora <zhanggp> <2013-11-15> added for systemui start
		hideClearButton();
		// Aurora <zhanggp> <2013-11-15> added for systemui end
        mStatusBarView.collapseAllPanels(true);
    }

    void makeExpandedInvisibleSoon() {
        mHandler.postDelayed(new Runnable() { public void run() { makeExpandedInvisible(); }}, 50);
    }

    void makeExpandedInvisible() {
        if (SPEW) Slog.d(TAG, "makeExpandedInvisible: mExpandedVisible=" + mExpandedVisible
                + " mExpandedVisible=" + mExpandedVisible);

        if (!mExpandedVisible) {
            return;
        }

        // Ensure the panel is fully collapsed (just in case; bug 6765842, 7260868)
		// Aurora <zhanggp> <2013-11-15> added for systemui start
        hideClearButton();
		// Aurora <zhanggp> <2013-11-15> added for systemui end
        mStatusBarView.collapseAllPanels(/*animate=*/ false);

        // Gionee <fengjianyi><2013-05-10> modify for CR00800567 start
        //if (mHasFlipSettings) {
        if (mHasFlipSettings && !ToolbarIconUtils.GN_QUICK_SETTINGS_SUPPORT) {
            // reset things to their proper state
            if (mFlipSettingsViewAnim != null) mFlipSettingsViewAnim.cancel();
            if (mScrollViewAnim != null) mScrollViewAnim.cancel();
            if (mSettingsButtonAnim != null) mSettingsButtonAnim.cancel();
            if (mNotificationButtonAnim != null) mNotificationButtonAnim.cancel();
            if (mClearButtonAnim != null) mClearButtonAnim.cancel();

            mScrollView.setScaleX(1f);
            mScrollView.setVisibility(View.VISIBLE);
            mSettingsButton.setAlpha(1f);
            mSettingsButton.setVisibility(View.VISIBLE);
            mNotificationPanel.setVisibility(View.GONE);
            mFlipSettingsView.setVisibility(View.GONE);
            mNotificationButton.setVisibility(View.GONE);
            /// M: [SystemUI] Support SimIndicator, show SimIndicator when notification panel is visible. @{
            //if (mToolBarView.mSimSwitchPanelView.isPanelShowing()) {
            //    mToolBarSwitchPanel.setVisibility(View.VISIBLE);
            //}
            /// M: [SystemUI] Support SimIndicator, show SimIndicator when notification panel is visible. @}
            /// M: [SystemUI] Remove settings button to notification header @{.
            if (mHeaderSettingsButton != null) {
                mHeaderSettingsButton.setVisibility(View.GONE);
            }
            /// M: [SystemUI] Remove settings button to notification header @}.
            setAreThereNotifications(); // show the clear button
        }
        // Gionee <fengjianyi><2013-05-10> modify for CR00800567 end

        mExpandedVisible = false;
        mPile.setLayoutTransitionsEnabled(false);
        if (mNavigationBarView != null)
            mNavigationBarView.setSlippery(false);
        visibilityChanged(false);

        // Shrink the window to the size of the status bar only
        WindowManager.LayoutParams lp = (WindowManager.LayoutParams) mStatusBarWindow.getLayoutParams();
        lp.height = getStatusBarHeight();
        lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.flags &= ~WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        mWindowManager.updateViewLayout(mStatusBarWindow, lp);

        if ((mDisabled & StatusBarManager.DISABLE_NOTIFICATION_ICONS) == 0) {
            setNotificationIconVisibility(true, android.R.anim.fade_in);
        }

        /// M: [SystemUI] Support "Notification toolbar". {
        //mToolBarView.dismissDialogs();
        //if (mQS != null) {
        //    mQS.dismissDialogs();
        //}
        /// M: [SystemUI] Support "Notification toolbar". }

        /// M: [SystemUI] Dismiss application guide dialog.@{
        if (mAppGuideDialog != null && mAppGuideDialog.isShowing()) {
            mAppGuideDialog.dismiss();
            Xlog.d(TAG, "performCollapse dismiss mAppGuideDialog");
        }
        /// M: [SystemUI] Dismiss application guide dialog.@}

        // Close any "App info" popups that might have snuck on-screen
        dismissPopups();

        if (mPostCollapseCleanup != null) {
            mPostCollapseCleanup.run();
            mPostCollapseCleanup = null;
        }
        //Aurora <tongyh> <2014-07-09> add full-screen pull-down status bar begin
        if(mView != null && mView.getParent() != null){
        	setInteracting(false);
        }
        
        //Aurora <tongyh> <2014-07-09> add full-screen pull-down status bar end
    }

    /**
     * Enables or disables layers on the children of the notifications pile.
     * 
     * When layers are enabled, this method attempts to enable layers for the minimal
     * number of children. Only children visible when the notification area is fully
     * expanded will receive a layer. The technique used in this method might cause
     * more children than necessary to get a layer (at most one extra child with the
     * current UI.)
     * 
     * @param layerType {@link View#LAYER_TYPE_NONE} or {@link View#LAYER_TYPE_HARDWARE}
     */
    private void setPileLayers(int layerType) {
        final int count = mPile.getChildCount();

        switch (layerType) {
            case View.LAYER_TYPE_NONE:
                for (int i = 0; i < count; i++) {
                    mPile.getChildAt(i).setLayerType(layerType, null);
                }
                break;
            case View.LAYER_TYPE_HARDWARE:
                final int[] location = new int[2]; 
                mNotificationPanel.getLocationInWindow(location);

                final int left = location[0];
                final int top = location[1];
                final int right = left + mNotificationPanel.getWidth();
                final int bottom = top + getExpandedViewMaxHeight();

                final Rect childBounds = new Rect();

                for (int i = 0; i < count; i++) {
                    final View view = mPile.getChildAt(i);
                    view.getLocationInWindow(location);

                    childBounds.set(location[0], location[1],
                            location[0] + view.getWidth(), location[1] + view.getHeight());

                    if (childBounds.intersects(left, top, right, bottom)) {
                        view.setLayerType(layerType, null);
                    }
                }

                break;
        }
    }

    public boolean isClinging() {
        return mCling != null && mCling.getVisibility() == View.VISIBLE;
    }

    public void hideCling() {
        if (isClinging()) {
            mCling.animate().alpha(0f).setDuration(250).start();
            mCling.setVisibility(View.GONE);
            mSuppressStatusBarDrags = false;
        }
    }

    public void showCling() {
        // lazily inflate this to accommodate orientation change
        final ViewStub stub = (ViewStub) mStatusBarWindow.findViewById(R.id.status_bar_cling_stub);
        if (stub == null) {
            mClingShown = true;
            return; // no clings on this device
        }

        mSuppressStatusBarDrags = true;

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCling = (ViewGroup) stub.inflate();

                mCling.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true; // e eats everything
                    }});
                mCling.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hideCling();
                    }});

                mCling.setAlpha(0f);
                mCling.setVisibility(View.VISIBLE);
                mCling.animate().alpha(1f);

                mClingShown = true;
                SharedPreferences.Editor editor = Prefs.edit(mContext);
                editor.putBoolean(Prefs.SHOWN_QUICK_SETTINGS_HELP, true);
                editor.apply();

                makeExpandedVisible(true); // enforce visibility in case the shade is still animating closed
                animateExpandNotificationsPanel();

                mSuppressStatusBarDrags = false;
            }
        }, 500);

        animateExpandNotificationsPanel();
    }

    public boolean interceptTouchEvent(MotionEvent event) {
        if (SPEW) {
            Slog.d(TAG, "Touch: rawY=" + event.getRawY() + " event=" + event + " mDisabled="
                + mDisabled + " mTracking=" + mTracking);
        } else if (CHATTY) {
            if (event.getAction() != MotionEvent.ACTION_MOVE) {
                Slog.d(TAG, String.format(
                            "panel: %s at (%f, %f) mDisabled=0x%08x",
                            MotionEvent.actionToString(event.getAction()),
                            event.getRawX(), event.getRawY(), mDisabled));
            }
        }

        if (DEBUG_GESTURES) {
            mGestureRec.add(event);
        }

        // Cling (first-run help) handling.
        // The cling is supposed to show the first time you drag, or even tap, the status bar.
        // It should show the notification panel, then fade in after half a second, giving you 
        // an explanation of what just happened, as well as teach you how to access quick
        // settings (another drag). The user can dismiss the cling by clicking OK or by 
        // dragging quick settings into view.
        final int act = event.getActionMasked();
        if (mSuppressStatusBarDrags) {
            return true;
        } else if (act == MotionEvent.ACTION_UP && !mClingShown) {
            showCling();
        } else {
            hideCling();
        }
//Aurora <tongyh> <2014-07-09> add full-screen pull-down status bar begin
        if(mView != null && mView.getParent() != null){
        	final boolean upOrCancel =
                    event.getAction() == MotionEvent.ACTION_UP ||
                    event.getAction() == MotionEvent.ACTION_CANCEL;
            if (upOrCancel && !mExpandedVisible) {
                setInteracting(false);
            } else {
                setInteracting(true);
            }
        }
//Aurora <tongyh> <2014-07-09> add full-screen pull-down status bar end
        return false;
    }

    public GestureRecorder getGestureRecorder() {
        return mGestureRec;
    }

    @Override // CommandQueue
    public void setNavigationIconHints(int hints) {
        if (hints == mNavigationIconHints) return;

        mNavigationIconHints = hints;

        if (mNavigationBarView != null) {
            mNavigationBarView.setNavigationIconHints(hints);
        }
    }

    @Override // CommandQueue
    public void setSystemUiVisibility(int vis, int mask) {
        final int oldVal = mSystemUiVisibility;
        final int newVal = (oldVal&~mask) | (vis&mask);
        final int diff = newVal ^ oldVal;

        if (diff != 0) {
            mSystemUiVisibility = newVal;

            if (0 != (diff & View.SYSTEM_UI_FLAG_LOW_PROFILE)) {
                final boolean lightsOut = (0 != (vis & View.SYSTEM_UI_FLAG_LOW_PROFILE));
                if (lightsOut) {
                    animateCollapsePanels();
                    if (mTicking) {
                        haltTicker();
                    }
                }

                if (mNavigationBarView != null) {
                    mNavigationBarView.setLowProfile(lightsOut);
                }

//                setStatusBarLowProfile(lightsOut);
            }

            notifyUiVisibilityChanged();
        }
    }

    private void setStatusBarLowProfile(boolean lightsOut) {
        if (mLightsOutAnimation == null) {
            final View notifications = mStatusBarView.findViewById(R.id.notification_icon_area);
            final View systemIcons = mStatusBarView.findViewById(R.id.statusIcons);
            final View signal = mStatusBarView.findViewById(R.id.signal_cluster);
            final View battery = mStatusBarView.findViewById(R.id.battery);
            final View clock = mStatusBarView.findViewById(R.id.clock);

            final AnimatorSet lightsOutAnim = new AnimatorSet();
            lightsOutAnim.playTogether(
                    ObjectAnimator.ofFloat(notifications, View.ALPHA, 0),
                    ObjectAnimator.ofFloat(systemIcons, View.ALPHA, 0),
                    ObjectAnimator.ofFloat(signal, View.ALPHA, 0),
                    ObjectAnimator.ofFloat(battery, View.ALPHA, 0.5f),
                    ObjectAnimator.ofFloat(clock, View.ALPHA, 0.5f)
                );
            lightsOutAnim.setDuration(750);

            final AnimatorSet lightsOnAnim = new AnimatorSet();
            lightsOnAnim.playTogether(
                    ObjectAnimator.ofFloat(notifications, View.ALPHA, 1),
                    ObjectAnimator.ofFloat(systemIcons, View.ALPHA, 1),
                    ObjectAnimator.ofFloat(signal, View.ALPHA, 1),
                    ObjectAnimator.ofFloat(battery, View.ALPHA, 1),
                    ObjectAnimator.ofFloat(clock, View.ALPHA, 1)
                );
            lightsOnAnim.setDuration(250);

            mLightsOutAnimation = lightsOutAnim;
            mLightsOnAnimation = lightsOnAnim;
        }

        mLightsOutAnimation.cancel();
        mLightsOnAnimation.cancel();

        final Animator a = lightsOut ? mLightsOutAnimation : mLightsOnAnimation;
        a.start();

        setAreThereNotifications();
    }

    private boolean areLightsOn() {
        return 0 == (mSystemUiVisibility & View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }

    public void setLightsOn(boolean on) {
        Log.v(TAG, "setLightsOn(" + on + ")");
        if (on) {
            setSystemUiVisibility(0, View.SYSTEM_UI_FLAG_LOW_PROFILE);
        } else {
            setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE, View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
    }

    private void notifyUiVisibilityChanged() {
        try {
            mWindowManagerService.statusBarVisibilityChanged(mSystemUiVisibility);
        } catch (RemoteException ex) {
        }
    }

    public void topAppWindowChanged(boolean showMenu) {
        // Aurora <Felix.Duan> <2014-8-8> <BEGIN> Refactor：update IME state &  sechdule task loading
        if (mHandlerBar != null && mHandlerBar.mRecentsPanelView != null)
            mHandlerBar.mRecentsPanelView.scheduleLoadTask();
        // Aurora <Felix.Duan> <2014-8-8> <END> Refactor：update IME state &  sechdule task loading
        if (DEBUG) {
            Slog.d(TAG, (showMenu?"showing":"hiding") + " the MENU button");
        }
        if (mNavigationBarView != null) {
            mNavigationBarView.setMenuVisibility(showMenu);
        }

        // See above re: lights-out policy for legacy apps.
        if (showMenu) setLightsOn(true);
    }

    @Override
    public void setImeWindowStatus(IBinder token, int vis, int backDisposition) {
        // Aurora <Felix.Duan> <2014-8-8> <BEGIN> Refactor：update IME state &  sechdule task loading
        boolean imeShowing = ((vis & InputMethodService.IME_VISIBLE) != 0);
        if (mHandlerBar != null && mHandlerBar.mRecentsPanelView != null)
            mHandlerBar.mRecentsPanelView.updateImeState(imeShowing);
        // Aurora <Felix.Duan> <2014-8-8> <END> Refactor：update IME state &  sechdule task loading
        boolean altBack = (backDisposition == InputMethodService.BACK_DISPOSITION_WILL_DISMISS)
            || ((vis & InputMethodService.IME_VISIBLE) != 0);

        mCommandQueue.setNavigationIconHints(
                altBack ? (mNavigationIconHints | StatusBarManager.NAVIGATION_HINT_BACK_ALT)
                        : (mNavigationIconHints & ~StatusBarManager.NAVIGATION_HINT_BACK_ALT));
        //if (mQS != null) mQS.setImeWindowStatus(vis > 0);
    }

    @Override
    public void setHardKeyboardStatus(boolean available, boolean enabled) {}

    @Override
    protected void tick(IBinder key, StatusBarNotification n, boolean firstTime) {
        // no ticking in lights-out mode
        if (!areLightsOn()) return;

        // no ticking in Setup
        if (!isDeviceProvisioned()) return;

        // not for you
        if (!notificationIsForCurrentUser(n)) return;

        // Show the ticker if one is requested. Also don't do this
        // until status bar window is attached to the window manager,
        // because...  well, what's the point otherwise?  And trying to
        // run a ticker without being attached will crash!
        if (n.notification.tickerText != null && mStatusBarWindow.getWindowToken() != null) {
            if (0 == (mDisabled & (StatusBarManager.DISABLE_NOTIFICATION_ICONS
                            | StatusBarManager.DISABLE_NOTIFICATION_TICKER))) {
                mTicker.addEntry(n);
            }
        }
    }

    private class MyTicker extends Ticker {
        MyTicker(Context context, View sb) {
            super(context, sb);
        }

        @Override
        public void tickerStarting() {
        	isPhoneIdle = false;
            mTicking = true;
            mStatusBarContents.setVisibility(View.GONE);
         // Aurora <tongyh> <2013-12-06> add quick lock screen function begin
//            mQuickLockScreen.setVisibility(View.GONE);
         // Aurora <tongyh> <2013-12-06> add quick lock screen function end
            mTickerView.setVisibility(View.VISIBLE);
            mTickerView.startAnimation(loadAnim(com.aurora.R.anim.push_up_in, null));
            mStatusBarContents.startAnimation(loadAnim(com.aurora.R.anim.push_up_out, null));
        }

        @Override
        public void tickerDone() {
        	isPhoneIdle = true;
            mStatusBarContents.setVisibility(View.VISIBLE);
         // Aurora <tongyh> <2013-12-06> add quick lock screen function begin
//            mQuickLockScreen.setVisibility(View.VISIBLE);
         // Aurora <tongyh> <2013-12-06> add quick lock screen function end
            mTickerView.setVisibility(View.GONE);
            mStatusBarContents.startAnimation(loadAnim(com.aurora.R.anim.push_down_in, null));
            mTickerView.startAnimation(loadAnim(com.aurora.R.anim.push_down_out,
                        mTickingDoneListener));
        }

        public void tickerHalting() {
        	isPhoneIdle = true;
            mStatusBarContents.setVisibility(View.VISIBLE);
         // Aurora <tongyh> <2013-12-06> add quick lock screen function begin
//            mQuickLockScreen.setVisibility(View.VISIBLE);
         // Aurora <tongyh> <2013-12-06> add quick lock screen function end
            mTickerView.setVisibility(View.GONE);
            mStatusBarContents.startAnimation(loadAnim(android.R.anim.fade_in, null));
            // we do not animate the ticker away at this point, just get rid of it (b/6992707)
        }
    }

    Animation.AnimationListener mTickingDoneListener = new Animation.AnimationListener() {;
        public void onAnimationEnd(Animation animation) {
            mTicking = false;
        }
        public void onAnimationRepeat(Animation animation) {
        }
        public void onAnimationStart(Animation animation) {
        }
    };

    private Animation loadAnim(int id, Animation.AnimationListener listener) {
        Animation anim = AnimationUtils.loadAnimation(mContext, id);
        if (listener != null) {
            anim.setAnimationListener(listener);
        }
        return anim;
    }

    public static String viewInfo(View v) {
        return "[(" + v.getLeft() + "," + v.getTop() + ")(" + v.getRight() + "," + v.getBottom()
                + ") " + v.getWidth() + "x" + v.getHeight() + "]";
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        synchronized (mQueueLock) {
            pw.println("Current Status Bar state:");
            pw.println("  mExpandedVisible=" + mExpandedVisible
                    + ", mTrackingPosition=" + mTrackingPosition);
            pw.println("  mTicking=" + mTicking);
            pw.println("  mTracking=" + mTracking);
            pw.println("  mNotificationPanel=" + 
                    ((mNotificationPanel == null) 
                            ? "null" 
                            : (mNotificationPanel + " params=" + mNotificationPanel.getLayoutParams().debug(""))));
            pw.println("  mAnimating=" + mAnimating
                    + ", mAnimY=" + mAnimY + ", mAnimVel=" + mAnimVel
                    + ", mAnimAccel=" + mAnimAccel);
            pw.println("  mAnimLastTimeNanos=" + mAnimLastTimeNanos);
            pw.println("  mAnimatingReveal=" + mAnimatingReveal
                    + " mViewDelta=" + mViewDelta);
            pw.println("  mDisplayMetrics=" + mDisplayMetrics);
            pw.println("  mPile: " + viewInfo(mPile));
            pw.println("  mTickerView: " + viewInfo(mTickerView));
            pw.println("  mScrollView: " + viewInfo(mScrollView)
                    + " scroll " + mScrollView.getScrollX() + "," + mScrollView.getScrollY());
        }

        pw.print("  mNavigationBarView=");
        if (mNavigationBarView == null) {
            pw.println("null");
        } else {
            mNavigationBarView.dump(fd, pw, args);
        }

        if (DUMPTRUCK) {
            synchronized (mNotificationData) {
                int N = mNotificationData.size();
                pw.println("  notification icons: " + N);
                for (int i=0; i<N; i++) {
                    NotificationData.Entry e = mNotificationData.get(i);
                    pw.println("    [" + i + "] key=" + e.key + " icon=" + e.icon);
                    StatusBarNotification n = e.notification;
                    pw.println("         pkg=" + n.pkg + " id=" + n.id + " score=" + n.score);
                    pw.println("         notification=" + n.notification);
                    pw.println("         tickerText=\"" + n.notification.tickerText + "\"");
                }
            }

            int N = mStatusIcons.getChildCount();
            pw.println("  system icons: " + N);
            for (int i=0; i<N; i++) {
                StatusBarIconView ic = (StatusBarIconView) mStatusIcons.getChildAt(i);
                pw.println("    [" + i + "] icon=" + ic);
            }

            if (false) {
                pw.println("see the logcat for a dump of the views we have created.");
                // must happen on ui thread
                mHandler.post(new Runnable() {
                        public void run() {
                            mStatusBarView.getLocationOnScreen(mAbsPos);
                            Slog.d(TAG, "mStatusBarView: ----- (" + mAbsPos[0] + "," + mAbsPos[1]
                                    + ") " + mStatusBarView.getWidth() + "x"
                                    + getStatusBarHeight());
                            mStatusBarView.debug();
                        }
                    });
            }
        }

        if (DEBUG_GESTURES) {
            pw.print("  status bar gestures: ");
            mGestureRec.dump(fd, pw, args);
        }
        // Aurora <tongyh> <2014-07-30> fc begin
		// Aurora <Steve.Tang> 2014-12-18. For Dual-Sim. start
        if(GnTelephonyManager.isMultiSimEnabled()){
        	
        }else{
        // Aurora <tongyh> <2014-07-30> fc end
        /// M: [SystemUI] Support "Dual SIM". {
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            mNetworkControllerGemini.dump(fd, pw, args);
        } else {
            mNetworkController.dump(fd, pw, args);
        }
        /// M: [SystemUI] Support "Dual SIM". }
       // Aurora <tongyh> <2014-07-30> fc begin
       }
       // Aurora <tongyh> <2014-07-30> fc end
	   // Aurora <Steve.Tang> 2014-12-18. For Dual-Sim. end
    }

    @Override
    public void createAndAddWindows() {
        addStatusBarWindow();
    }

    private void addStatusBarWindow() {
        // Put up the view
        final int height = getStatusBarHeight();

        // Now that the status bar window encompasses the sliding panel and its
        // translucent backdrop, the entire thing is made TRANSLUCENT and is
        // hardware-accelerated.
        final WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                height,
                WindowManager.LayoutParams.TYPE_STATUS_BAR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
                    | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
                PixelFormat.TRANSLUCENT);

        lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;

        lp.gravity = getStatusBarGravity();
        lp.setTitle("StatusBar");
        lp.packageName = mContext.getPackageName();

        makeStatusBarView();
        mWindowManager.addView(mStatusBarWindow, lp);
    }

    void setNotificationIconVisibility(boolean visible, int anim) {
        int old = mNotificationIcons.getVisibility();
        int v = visible ? View.VISIBLE : View.INVISIBLE;
        if (old != v) {
            mNotificationIcons.setVisibility(v);
            mNotificationIcons.startAnimation(loadAnim(anim, null));
        }
    }

    void updateExpandedInvisiblePosition() {
        mTrackingPosition = -mDisplayMetrics.heightPixels;
    }

    static final float saturate(float a) {
        return a < 0f ? 0f : (a > 1f ? 1f : a);
    }

    @Override
    protected int getExpandedViewMaxHeight() {
        return mDisplayMetrics.heightPixels - mNotificationPanelMarginBottomPx;
    }

    @Override
    public void updateExpandedViewPos(int thingy) {
        if (DEBUG) Slog.v(TAG, "updateExpandedViewPos");

        // on larger devices, the notification panel is propped open a bit
        mNotificationPanel.setMinimumHeight(
                (int)(mNotificationPanelMinHeightFrac * mCurrentDisplaySize.y));

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mNotificationPanel.getLayoutParams();
        lp.gravity = mNotificationPanelGravity;
        lp.leftMargin = mNotificationPanelMarginPx;
        mNotificationPanel.setLayoutParams(lp);
		/*
        if (mSettingsPanel != null) {
            lp = (FrameLayout.LayoutParams) mSettingsPanel.getLayoutParams();
            lp.gravity = mSettingsPanelGravity;
            lp.rightMargin = mNotificationPanelMarginPx;
            mSettingsPanel.setLayoutParams(lp);
        }
		*/
        updateCarrierLabelVisibility(false);
    }

    // called by makeStatusbar and also by PhoneStatusBarView
    void updateDisplaySize() {
        mDisplay.getMetrics(mDisplayMetrics);
        if (DEBUG_GESTURES) {
            mGestureRec.tag("display", 
                    String.format("%dx%d", mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels));
        }
    }

    private View.OnClickListener mClearButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            synchronized (mNotificationData) {
                // animate-swipe all dismissable notifications, then animate the shade closed
                int numChildren = mPile.getChildCount();

                int scrollTop = mScrollView.getScrollY();
                int scrollBottom = scrollTop + mScrollView.getHeight();
                final ArrayList<View> snapshot = new ArrayList<View>(numChildren);
                for (int i=0; i<numChildren; i++) {
                    final View child = mPile.getChildAt(i);
                    if (mPile.canChildBeDismissed(child) && child.getBottom() > scrollTop &&
                            child.getTop() < scrollBottom) {
                        snapshot.add(child);
                    }
                }
                if (snapshot.isEmpty()) {
                    animateCollapsePanels(CommandQueue.FLAG_EXCLUDE_NONE);
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // Decrease the delay for every row we animate to give the sense of
                        // accelerating the swipes
                        final int ROW_DELAY_DECREMENT = 10;
                        int currentDelay = 140;
                        int totalDelay = 0;

                        // Set the shade-animating state to avoid doing other work during
                        // all of these animations. In particular, avoid layout and
                        // redrawing when collapsing the shade.
                        mPile.setViewRemoval(false);

                        mPostCollapseCleanup = new Runnable() {
                            @Override
                            public void run() {
                                if (DEBUG) {
                                    Slog.v(TAG, "running post-collapse cleanup");
                                }
                                try {
                                    mPile.setViewRemoval(true);
                                    mBarService.onClearAllNotifications();
                                } catch (Exception ex) { }
                            }
                        };

                        View sampleView = snapshot.get(0);
                        int width = sampleView.getWidth();
                        final int velocity = width * 8; // 1000/8 = 125 ms duration
                        for (final View _v : snapshot) {
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mPile.dismissRowAnimated(_v, velocity);
                                }
                            }, totalDelay);
                            currentDelay = Math.max(50, currentDelay - ROW_DELAY_DECREMENT);
                            totalDelay += currentDelay;
                        }
                        // Delay the collapse animation until after all swipe animations have
                        // finished. Provide some buffer because there may be some extra delay
                        // before actually starting each swipe animation. Ideally, we'd
                        // synchronize the end of those animations with the start of the collaps
                        // exactly.
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                animateCollapsePanels(CommandQueue.FLAG_EXCLUDE_NONE);
                            }
                        }, totalDelay + 225);
                    }
                }).start();
                /// M: [SystemUI] Dismiss new event icon when click clear button for keyguard.@{
                Intent intent = new Intent(CLEAR_NEW_EVENT_VIEW_INTENT);
                mContext.sendBroadcast(intent);
                /// M: [SystemUI] Dismiss new event icon when click clear button for keyguard.@}
            }
        }
    };

    public void startActivityDismissingKeyguard(Intent intent, boolean onlyProvisioned) {
        if (onlyProvisioned && !isDeviceProvisioned()) return;
        try {
            // Dismiss the lock screen when Settings starts.
            ActivityManagerNative.getDefault().dismissKeyguardOnNextActivity();
        } catch (RemoteException e) {
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivityAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
        animateCollapsePanels();
    }
    //Aurora <rocktong> <2015-01-19> account bug begin
    public void startActivityDismissingKeyguard(Intent intent, boolean onlyProvisioned,boolean flag) {
        if (onlyProvisioned && !isDeviceProvisioned()) return;
        try {
            // Dismiss the lock screen when Settings starts.
            ActivityManagerNative.getDefault().dismissKeyguardOnNextActivity();
        } catch (RemoteException e) {
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivityAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
        animateCollapsePanels();
    }
    //Aurora <rocktong> <2015-01-19> account bug end
    private View.OnClickListener mSettingsButtonListener = new View.OnClickListener() {
        public void onClick(View v) {

	        startActivityDismissingKeyguard(
	                new Intent("android.settings.NOTIFYPUSH_SETTINGS"), true);
	        mStatusBarView.setBackgroundColor(Color.BLACK);
        }
    };

    /// M: [SystemUI] Remove settings button to notification header @{.
    private View.OnClickListener mHeaderSettingsButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            startActivityDismissingKeyguard(new Intent("android.settings.NOTIFYPUSH_SETTINGS"), true);
            mStatusBarView.setBackgroundColor(Color.BLACK);
        }
    };
    /// M: [SystemUI] Remove settings button to notification header @}.
//Aurora <rocktong> <2015-01-19> account bug begin
    /*private View.OnClickListener mClockClickListener = new View.OnClickListener() {
        public void onClick(View v) {
//Aurora <tongyh> <2015-01-13> add account begin
//            startActivityDismissingKeyguard(
//                    new Intent(Intent.ACTION_QUICK_CLOCK), true); // have fun, everyone
        	try {
        	    Uri uri = Uri.parse("openaccount://com.aurora.account.login");
    		    Intent intent = new Intent();
    		    intent.setAction(Intent.ACTION_VIEW);
    		    intent.addCategory(Intent.CATEGORY_DEFAULT);
    		    intent.setData(uri);
    		    // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    		    startActivityDismissingKeyguard(intent, true);
        	} catch (Exception e) {
            }
        }
    };*/
//Aurora <rocktong> <2015-01-19> account bug end
    private View.OnClickListener mNotificationButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            animateExpandNotificationsPanel();
        }
    };

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Slog.v(TAG, "onReceive: " + intent);
            String action = intent.getAction();
            Xlog.d(TAG, "onReceive, action=" + action);
            /// M: ALPS00349274 to hide navigation bar when ipo shut down to avoid it flash when in boot ipo mode.{
            if ("android.intent.action.ACTION_BOOT_IPO".equals(action)) {
                if (mNavigationBarView != null) {
                    View view = mNavigationBarView.findViewById(R.id.rot0);
                    if (view != null && view.getVisibility() != View.GONE) {
                        Xlog.d(TAG, "receive android.intent.action.ACTION_BOOT_IPO to set mNavigationBarView visible");
                        view.setVisibility(View.VISIBLE);
                    }
                }
            } else if ("android.intent.action.ACTION_SHUTDOWN_IPO".equals(action)) {
                if (mNavigationBarView != null) {
                    Xlog.d(TAG, "receive android.intent.action.ACTION_SHUTDOWN_IPO to set mNavigationBarView invisible");
                    mNavigationBarView.hideForIPOShutdown();
                }
            /// M: ALPS00349274 to hide navigation bar when ipo shut down to avoid it flash when in boot ipo mode.}
            } else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                int flags = CommandQueue.FLAG_EXCLUDE_NONE;
                if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                    String reason = intent.getStringExtra("reason");
                    if (reason != null && reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                        flags |= CommandQueue.FLAG_EXCLUDE_RECENTS_PANEL;
                    }
                }
                animateCollapsePanels(flags);
            /// M: [SystemUI] Support "ThemeManager" @{
            /*
            } else if (Intent.ACTION_SKIN_CHANGED.equals(action)) {
                refreshApplicationGuide();
                refreshExpandedView(context);
                if (mNavigationBarView != null) {
                    mNavigationBarView.upDateResources();
                }
                repositionNavigationBar();
                updateResources();
                */
            /// M: [SystemUI] Support "Theme management". @}
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                // no waiting!
                /// M: [SystemUI]Show application guide for App.
                if (mAppGuideDialog != null && mAppGuideDialog.isShowing()) {
                    mAppGuideDialog.dismiss();
                    Xlog.d(TAG, "mAppGuideDialog.dismiss()");
                }
                /// M: [SystemUI]Show application guide for App. @}
                makeExpandedInvisible();
                notifyNavigationBarScreenOn(false);
            } else if (Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {
                if (DEBUG) {
                    Slog.v(TAG, "configuration changed: " + mContext.getResources().getConfiguration());
                }
                /// M: [SystemUI]Show application guide for App.
                refreshApplicationGuide();
                Configuration currentConfig = context.getResources().getConfiguration();
                /// M: [ALPS00336833] When orientation changed, request layout to avoid status bar layout error. @{
                if (currentConfig.orientation != mPrevioutConfigOrientation) {
                    mNeedRelayout = true;
                    mPrevioutConfigOrientation = currentConfig.orientation;
                    // Gionee <fengjianyi><2013-05-13> add for CR00800567 start
                    if (ToolbarIconUtils.GN_QUICK_SETTINGS_SUPPORT) {
                        updateStatusBarView(currentConfig.orientation);
                    }
                    // Gionee <fengjianyi><2013-05-13> add for CR00800567 end
                }
                /// M: [ALPS00336833] When orientation changed, request layout to avoid status bar layout error. @}
                mDisplay.getSize(mCurrentDisplaySize);
             // Aurora <tongyh> <2014-03-07> Horizontal and vertical screen treatment begin
                refreshExpandedView(mContext);
             // Aurora <tongyh> <2014-03-07> Horizontal and vertical screen treatment end
                updateResources();
                repositionNavigationBar();
                updateExpandedViewPos(EXPANDED_LEAVE_ALONE);
                updateShowSearchHoldoff();
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                // work around problem where mDisplay.getRotation() is not stable while screen is off (bug 7086018)
                repositionNavigationBar();
                notifyNavigationBarScreenOn(true);
            /// M: [SystemUI] Support "Dual SIM PLMN Change". @{
            } else if (FeatureOption.SPN_STRINGS_UPDATED_ACTION.equals(action)) {
			// Aurora <Steve.Tang> 2014-12-18. For Dual-Sim. start
                if (mShowCarrierInPanel && !GnTelephonyManager.isMultiSimEnabled()) {
				// Aurora <Steve.Tang> 2014-12-18. For Dual-Sim. end
                    /*if (FeatureOption.MTK_GEMINI_SUPPORT) {
                        int tempSimId = intent.getIntExtra(FeatureOption.GEMINI_SIM_ID_KEY, FeatureOption.GEMINI_SIM_1);
                        if (tempSimId == mCarrier1.getSlotId()) {
                            mCarrier1.updateNetworkName(intent.getBooleanExtra(FeatureOption.EXTRA_SHOW_SPN, false),
                            intent.getStringExtra(FeatureOption.EXTRA_SPN),
                            intent.getBooleanExtra(FeatureOption.EXTRA_SHOW_PLMN, false),
                            intent.getStringExtra(FeatureOption.EXTRA_PLMN));
                        } else {
                            mCarrier2.updateNetworkName(intent.getBooleanExtra(FeatureOption.EXTRA_SHOW_SPN, false),
                            intent.getStringExtra(FeatureOption.EXTRA_SPN),
                            intent.getBooleanExtra(FeatureOption.EXTRA_SHOW_PLMN, false),
                            intent.getStringExtra(FeatureOption.EXTRA_PLMN));
                        }
                    } else {*/
						((CarrierLabel)mCarrierLabel).updateNetworkName(
                                intent.getBooleanExtra(FeatureOption.EXTRA_SHOW_SPN, false),
                                intent.getStringExtra(FeatureOption.EXTRA_SPN),
                                intent.getBooleanExtra(FeatureOption.EXTRA_SHOW_PLMN, false),
                                intent.getStringExtra(FeatureOption.EXTRA_PLMN), isAirplaneModeOn());
//                    }
                }
            }
            //gionee fengxb 2013-4-27 add for CR00802140 start
			// Aurora <Steve.Tang> 2014-12-18. For Dual-Sim. start
            else if (FeatureOption.MTK_GEMINI_SUPPORT) {
            	if (mShowCarrierInPanel) {
                    if (FeatureOption.SPN_STRINGS_UPDATED_ACTION_SIM1.equals(action) ||
                             FeatureOption.SPN_STRINGS_UPDATED_ACTION_SIM2.equals(action)) {
                    	int tempSimId = intent.getIntExtra(FeatureOption.GEMINI_SIM_ID_KEY, FeatureOption.GEMINI_SIM_1);
                        if (tempSimId == mCarrier1.getSlotId()) {
                            mCarrier1.updateNetworkName(intent.getBooleanExtra(FeatureOption.EXTRA_SHOW_SPN, false),
                            intent.getStringExtra(FeatureOption.EXTRA_SPN),
                            intent.getBooleanExtra(FeatureOption.EXTRA_SHOW_PLMN, false),
                            intent.getStringExtra(FeatureOption.EXTRA_PLMN));
                        } else {
                            mCarrier2.updateNetworkName(intent.getBooleanExtra(FeatureOption.EXTRA_SHOW_SPN, false),
                            intent.getStringExtra(FeatureOption.EXTRA_SPN),
                            intent.getBooleanExtra(FeatureOption.EXTRA_SHOW_PLMN, false),
                            intent.getStringExtra(FeatureOption.EXTRA_PLMN));
                        }
                    }
            	}
            }
			// Aurora <Steve.Tang> 2014-12-18. For Dual-Sim. end
            //gionee fengxb 2013-4-27 add for CR00802140 end
            /// M: [SystemUI] Support "Dual SIM PLMN Change". }@
		  // Aurora <Steve.Tang> 2014-08-12 update as air plane mode change, start
            //else if(action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)){
			// Aurora <Steve.Tang> 2014-12-18. For Dual-Sim. start		  
            else if(action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED) || (Intent.ACTION_AIRPLANE_MODE_CHANGED).equals(action)){
		  // Aurora <Steve.Tang> 2014-08-12 update as air plane mode change, end
            	if(GnTelephonyManager.isMultiSimEnabled()){
            	    ViewGroup mSignalClusterCombo = (ViewGroup)mStatusBarWindow.findViewById(R.id.signal_cluster_combo);
            	    ViewGroup mSignalClusterCombo2 = (ViewGroup)mStatusBarWindow.findViewById(R.id.signal_cluster_combo2);
            	    View mSpacer2 = mStatusBarWindow.findViewById(R.id.spacer2);

					// Steve.Tang 2014-07-18 set state absent as is unknow or absent, start
					int simOneState = GnTelephonyManager.getSimStateGemini(0);
					int simTwoState = GnTelephonyManager.getSimStateGemini(1);

            		boolean mSimOneIsAbsent = false;
                	boolean mSimTwoIsAbsent = false;

					if(simOneState == TelephonyManager.SIM_STATE_ABSENT || simOneState == TelephonyManager.SIM_STATE_UNKNOWN){
							mSimOneIsAbsent = true;
					}

					if(simTwoState == TelephonyManager.SIM_STATE_ABSENT || simTwoState == TelephonyManager.SIM_STATE_UNKNOWN){
							mSimTwoIsAbsent = true;
					}
					//boolean mSimOneIsAbsent = GnTelephonyManager.getSimStateGemini(0) == TelephonyManager.SIM_STATE_ABSENT;
                	//boolean mSimTwoIsAbsent = GnTelephonyManager.getSimStateGemini(1) == TelephonyManager.SIM_STATE_ABSENT;

					// Steve.Tang 2014-07-18 set state absent as is unknow or absent, end

android.util.Log.e("xiuyong","the air mode is: " + isAirplaneModeOn());

                	if(mSimOneIsAbsent && mSimTwoIsAbsent){
                		mSignalClusterCombo.setVisibility(View.VISIBLE);
                		mSpacer2.setVisibility(View.GONE);
                		mSignalClusterCombo2.setVisibility(View.GONE);
                	}else if(mSimOneIsAbsent){
                		mSignalClusterCombo.setVisibility(View.GONE);
                		mSpacer2.setVisibility(View.GONE);
                		mSignalClusterCombo2.setVisibility(View.VISIBLE);
                	}else if(mSimTwoIsAbsent){
                		mSignalClusterCombo.setVisibility(View.VISIBLE);
                		mSpacer2.setVisibility(View.GONE);
                		mSignalClusterCombo2.setVisibility(View.GONE);
                	}else{
                		mSignalClusterCombo.setVisibility(View.VISIBLE);
						// Aurora <Steve.Tang> 2014-08-12 do not show space2 as air mode on. start
						if(isAirplaneModeOn()){
		            		mSpacer2.setVisibility(View.GONE);
						} else {
		            		mSpacer2.setVisibility(View.VISIBLE);				
						}
						// Aurora <Steve.Tang> 2014-08-12 do not show space2 as air mode on. end
                		mSignalClusterCombo2.setVisibility(View.VISIBLE);
                	}
            	}
            }
          //Aurora <tongyh> <2014-06-05> add ACTION_SIM_STATE_CHANGED broadcast end
		  // Aurora <Steve.Tang> 2014-12-18. For Dual-Sim. end
//Aurora <tongyh> <2014-07-09> add full-screen pull-down status bar begin
            else if (action.equals("com.aurora.systemui.visiable.statusbar")){
            	Log.d("0707", "com.aurora.systemui.visiable.statusbar");
            	handleShow();
            }
//Aurora <tongyh> <2014-07-09> add full-screen pull-down status bar end
        }
    };
	// Aurora <Steve.Tang> 2014-08-12 get air mode state. start
	private boolean isAirplaneModeOn(){
		return (Settings.System.getInt(mContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1);
	}
	// Aurora <Steve.Tang> 2014-08-12 get air mode state. end
    @Override
    public void userSwitched(int newUserId) {
        if (MULTIUSER_DEBUG) mNotificationPanelDebugText.setText("USER " + newUserId);
        animateCollapsePanels();
        updateNotificationIcons();
        resetUserSetupObserver();
    }

    private void resetUserSetupObserver() {
        mContext.getContentResolver().unregisterContentObserver(mUserSetupObserver);
        mUserSetupObserver.onChange(false);
        mContext.getContentResolver().registerContentObserver(
                Settings.Secure.getUriFor(Settings.Secure.USER_SETUP_COMPLETE), true,
                mUserSetupObserver,
                mCurrentUserId);
    }

    private void setIntruderAlertVisibility(boolean vis) {
        if (!ENABLE_INTRUDERS) return;
        if (DEBUG) {
            Slog.v(TAG, (vis ? "showing" : "hiding") + " intruder alert window");
        }
        mIntruderAlertView.setVisibility(vis ? View.VISIBLE : View.GONE);
    }

    public void dismissIntruder() {
        if (mCurrentlyIntrudingNotification == null) return;

        try {
            mBarService.onNotificationClear(
                    mCurrentlyIntrudingNotification.pkg,
                    mCurrentlyIntrudingNotification.tag,
                    mCurrentlyIntrudingNotification.id);
        } catch (android.os.RemoteException ex) {
            // oh well
        }
    }

    /**
     * Reload some of our resources when the configuration changes.
     *
     * We don't reload everything when the configuration changes -- we probably
     * should, but getting that smooth is tough.  Someday we'll fix that.  In the
     * meantime, just update the things that we know change.
     */
    void updateResources() {
        Xlog.d(TAG, "updateResources");

        final Context context = mContext;
        final Resources res = context.getResources();

        if (mClearButton instanceof TextView) {
            ((TextView)mClearButton).setText(context.getText(R.string.status_bar_clear_all_button));
        }
        /// M: [SystemUI] Support "Notification toolbar". {
        //mToolBarView.updateResources();
        /// M: [SystemUI] Support "Notification toolbar". }

        // Update the QuickSettings container
        //if (mQS != null) mQS.updateResources();

        // Gionee <fengjianyi><2013-05-20> add for CR00800567 start
        if (ToolbarIconUtils.GN_QUICK_SETTINGS_SUPPORT) {
        	mNotificationRadioButton.setText(R.string.zzzzz_gn_radio_notification);
        	mSwitchRadioButton.setText(R.string.zzzzz_gn_radio_switch);
        }
        // Gionee <fengjianyi><2013-05-20> add for CR00800567 end

        loadDimens();
    }

    protected void loadDimens() {
        final Resources res = mContext.getResources();

        mNaturalBarHeight = res.getDimensionPixelSize(
                R.dimen.aurora_systemui_status_bar_height);

        int newIconSize = res.getDimensionPixelSize(
        		R.dimen.aurora_systemui_statusbar_icon_size);
        int newIconHPadding = res.getDimensionPixelSize(
            R.dimen.status_bar_icon_padding);

        if (newIconHPadding != mIconHPadding || newIconSize != mIconSize) {
//            Slog.d(TAG, "size=" + newIconSize + " padding=" + newIconHPadding);
            mIconHPadding = newIconHPadding;
            mIconSize = newIconSize;
            //reloadAllNotificationIcons(); // reload the tray
        }

        mEdgeBorder = res.getDimensionPixelSize(R.dimen.status_bar_edge_ignore);

        mSelfExpandVelocityPx = res.getDimension(R.dimen.self_expand_velocity);
        mSelfCollapseVelocityPx = res.getDimension(R.dimen.self_collapse_velocity);
        mFlingExpandMinVelocityPx = res.getDimension(R.dimen.fling_expand_min_velocity);
        mFlingCollapseMinVelocityPx = res.getDimension(R.dimen.fling_collapse_min_velocity);

        mCollapseMinDisplayFraction = res.getFraction(R.dimen.collapse_min_display_fraction, 1, 1);
        mExpandMinDisplayFraction = res.getFraction(R.dimen.expand_min_display_fraction, 1, 1);

        mExpandAccelPx = res.getDimension(R.dimen.expand_accel);
        mCollapseAccelPx = res.getDimension(R.dimen.collapse_accel);

        mFlingGestureMaxXVelocityPx = res.getDimension(R.dimen.fling_gesture_max_x_velocity);

        mFlingGestureMaxOutputVelocityPx = res.getDimension(R.dimen.fling_gesture_max_output_velocity);

        mNotificationPanelMarginBottomPx
            = (int) res.getDimension(R.dimen.notification_panel_margin_bottom);
        mNotificationPanelMarginPx
            = (int) res.getDimension(R.dimen.notification_panel_margin_left);
        mNotificationPanelGravity = res.getInteger(R.integer.notification_panel_layout_gravity);
        if (mNotificationPanelGravity <= 0) {
            mNotificationPanelGravity = Gravity.LEFT | Gravity.TOP;
        }
        mSettingsPanelGravity = res.getInteger(R.integer.settings_panel_layout_gravity);
        if (mSettingsPanelGravity <= 0) {
            mSettingsPanelGravity = Gravity.RIGHT | Gravity.TOP;
        }

        mCarrierLabelHeight = res.getDimensionPixelSize(R.dimen.carrier_label_height);
        mNotificationHeaderHeight = res.getDimensionPixelSize(R.dimen.notification_panel_header_height);
        /// M: Calculate ToolBar height when sim indicator is showing.
        mToolBarViewHeight = res.getDimensionPixelSize(R.dimen.toolbar_height);

        mNotificationPanelMinHeightFrac = res.getFraction(R.dimen.notification_panel_min_height_frac, 1, 1);
        if (mNotificationPanelMinHeightFrac < 0f || mNotificationPanelMinHeightFrac > 1f) {
            mNotificationPanelMinHeightFrac = 0f;
        }

        if (false) Slog.v(TAG, "updateResources");
    }

    //
    // tracing
    //

    void postStartTracing() {
        mHandler.postDelayed(mStartTracing, 3000);
    }

    void vibrate() {
        android.os.Vibrator vib = (android.os.Vibrator)mContext.getSystemService(
                Context.VIBRATOR_SERVICE);
        vib.vibrate(250);
    }

    Runnable mStartTracing = new Runnable() {
        public void run() {
            vibrate();
            SystemClock.sleep(250);
            Slog.d(TAG, "startTracing");
            android.os.Debug.startMethodTracing("/data/statusbar-traces/trace");
            mHandler.postDelayed(mStopTracing, 10000);
        }
    };

    Runnable mStopTracing = new Runnable() {
        public void run() {
            android.os.Debug.stopMethodTracing();
            Slog.d(TAG, "stopTracing");
            vibrate();
        }
    };

    @Override
    protected void haltTicker() {
        mTicker.halt();
    }

    @Override
    protected boolean shouldDisableNavbarGestures() {
        return !isDeviceProvisioned()
                || mExpandedVisible
                || (mDisabled & StatusBarManager.DISABLE_SEARCH) != 0;
    }

    private static class FastColorDrawable extends Drawable {
        private final int mColor;

        public FastColorDrawable(int color) {
            mColor = 0xff000000 | color;
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawColor(mColor, PorterDuff.Mode.SRC);
        }

        @Override
        public void setAlpha(int alpha) {
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
        }

        @Override
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }

        @Override
        public void setBounds(int left, int top, int right, int bottom) {
        }

        @Override
        public void setBounds(Rect bounds) {
        }
    }
    /// M: [SystemUI] Support "Dual SIM". @{

    private NetworkControllerGemini mNetworkControllerGemini;

    private CarrierLabelGemini mCarrier1 = null;
    private View mCarrierDivider = null;
    private CarrierLabelGemini mCarrier2 = null;
    private LinearLayout mCarrierLabelGemini = null;

    private BroadcastReceiver mSIMInfoReceiver = new BroadcastReceiver() {
        public void onReceive(final Context context, final Intent intent) {
		// Aurora <zhanggp> <2013-10-30> modified for systemui begin
		/*
            String action = intent.getAction();
            Xlog.d(TAG, "onReceive, intent action is " + action + ".");
            if (action.equals(Intent.SIM_SETTINGS_INFO_CHANGED)) {
                mHandler.post(new Runnable() {
                    public void run() {
                        SIMHelper.updateSIMInfos(context);
                        int type = intent.getIntExtra("type", -1);
                        long simId = intent.getLongExtra("simid", -1);
                        if (type == 0 || type == 1) {
                            // name and color changed
                            updateNotificationsSimInfo(simId);
                        }
                        // update ToolBarView's panel views
                        mToolBarView.updateSimInfos(intent);
                        if (mQS != null) {
                            mQS.updateSimInfo(intent);
                        }
                    }
                });
            } else if (action.equals(TelephonyIntents.ACTION_SIM_INSERTED_STATUS)
                    || action.equals(TelephonyIntents.ACTION_SIM_INFO_UPDATE)){
                mHandler.post(new Runnable() {
                    public void run() {
                        SIMHelper.updateSIMInfos(context);
                    }
                });
                updateSimIndicator();
            } else if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                updateAirplaneMode();
            } else if (action.equals(ACTION_BOOT_IPO)) {
                if (mSimIndicatorIcon != null) {
                    mSimIndicatorIcon.setVisibility(View.GONE);
                }
            }
			*/
		// Aurora <zhanggp> <2013-10-30> modified for systemui end
        }
    };
// Aurora <zhanggp> <2013-10-30> modified for systemui begin
    private void updateNotificationsSimInfo(long simId) {
    }
	private void updateNotificationSimInfo(SIMInfo simInfo, Notification n, StatusBarIconView iconView, View itemView) {
	}

	/*
    private void updateNotificationsSimInfo(long simId) {
        Xlog.d(TAG, "updateNotificationsSimInfo, the simId is " + simId + ".");
        if (simId == -1) {
            return;
        }
        SIMInfo simInfo = SIMHelper.getSIMInfo(mContext, simId);
        if (simInfo == null) {
            Xlog.d(TAG, "updateNotificationsSimInfo, the simInfo is null.");
            return;
        }
        for (int i = 0, n = this.mNotificationData.size(); i < n; i++) {
            Entry entry = this.mNotificationData.get(i);
            updateNotificationSimInfo(simInfo, entry.notification.notification, entry.icon, entry.expanded);
        }
    }

    private void updateNotificationSimInfo(SIMInfo simInfo, Notification n, StatusBarIconView iconView, View itemView) {
        if (n.simId != simInfo.mSimId) {
            return;
        }
        int simInfoType = n.simInfoType;
        if (iconView == null) { //for update SimIndicatorView
            for (int i=0; i<mNotificationIcons.getChildCount(); i++) {
                View child = mNotificationIcons.getChildAt(i);
                if (child instanceof StatusBarIconView) {
                    StatusBarIconView iconViewtemp = (StatusBarIconView) child;
                    if(iconViewtemp.getNotificationSimId() == n.simId){
                        iconView = iconViewtemp;
                        break;
                    }
                }
            }
        }        
        // icon part.
//        if ((simInfoType == 2 || simInfoType == 3) && simInfo != null && iconView != null) {
//            Xlog.d(TAG, "updateNotificationSimInfo, add sim info to status bar.");
//            Drawable drawable = iconView.getResources().getDrawable(simInfo.mSimBackgroundRes);
//           if (drawable != null) {
//                iconView.setSimInfoBackground(drawable);
//                iconView.invalidate();
//            }
//        }
        // item part.
        if ((simInfoType == 1 || simInfoType == 3) && simInfo != null && (simInfo.mColor >= 0 && simInfo.mColor < Telephony.SIMBackgroundRes.length)) {
            Xlog.d(TAG, "updateNotificationSimInfo, add sim info to notification item. simInfo.mColor = " + simInfo.mColor);
            View simIndicatorLayout = itemView.findViewById(com.aurora.R.id.notification_sim_indicator);
            simIndicatorLayout.setVisibility(View.VISIBLE);
            ImageView bgView = (ImageView) itemView.findViewById(com.aurora.R.id.notification_sim_indicator_bg);
            bgView.setBackground(mContext.getResources().getDrawable(TelephonyIcons.SIM_INDICATOR_BACKGROUND_NOTIFICATION[simInfo.mColor]));
            bgView.setVisibility(View.VISIBLE);
        } else {
            View simIndicatorLayout = itemView.findViewById(com.aurora.R.id.notification_sim_indicator);
            simIndicatorLayout.setVisibility(View.VISIBLE);
            View bgView = itemView.findViewById(com.aurora.R.id.notification_sim_indicator_bg);
            bgView.setVisibility(View.GONE);
        }
    }

	*/
// Aurora <zhanggp> <2013-10-30> modified for systemui end
    /// M: [SystemUI] Support "Dual SIM". @}

    /// M: [SystemUI] Support "Notification toolbar". @{
    //private ToolBarView mToolBarView;
    private View mToolBarSwitchPanel;
    public boolean isExpanded() {
        return mExpandedVisible;
    }
    /// M: [SystemUI] Support "Notification toolbar". @}

    /// M: [SystemUI] Support "SIM indicator". @{

    private boolean mIsSimIndicatorShowing = false;
    private String mBusinessType = null;
	// Aurora <zhanggp> <2013-10-17> modified for systemui begin
    public void showSIMIndicator(String businessType) {

    }

    public void hideSIMIndicator() {
 
    }
	
   private boolean mAirplaneMode = false;
    private boolean mSimIndicatorIconShow = false;
    
    private void updateAirplaneMode() {

    }

    private void updateSimIndicator() {

    }
	/*
    public void showSIMIndicator(String businessType) {
        if (mIsSimIndicatorShowing) {
            hideSIMIndicator();
        }
        mBusinessType = businessType;
        long simId = SIMHelper.getDefaultSIM(mContext, businessType);
        Xlog.d(TAG, "showSIMIndicator, show SIM indicator which business is " + businessType + "  simId = "+simId+".");
        if (simId == android.provider.Settings.System.DEFAULT_SIM_SETTING_ALWAYS_ASK) {
            List<SIMInfo> simInfos = SIMHelper.getSIMInfoList(mContext);
            if (simInfos != null && simInfos.size() > 0) {
                showAlwaysAskOrInternetCall(simId);
                mToolBarView.showSimSwithPanel(businessType);
            }
        } else if (businessType.equals(android.provider.Settings.System.VOICE_CALL_SIM_SETTING)
                && simId == android.provider.Settings.System.VOICE_CALL_SIM_SETTING_INTERNET) {
            showAlwaysAskOrInternetCall(simId);
            mToolBarView.showSimSwithPanel(businessType);
        } else {
            mSimIndicatorIconShow = false;
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                List<SIMInfo> simInfos = SIMHelper.getSIMInfoList(mContext);
                if (simInfos == null) {
                    return;
                }
                int slot = 0;
                for (int i = 0; i < simInfos.size(); i++) {
                    if (simInfos.get(i).mSimId == simId) {
                        slot = simInfos.get(i).mSlot;
                        break;
                    }
                }
                if (simInfos.size() == 1) {
                    if (businessType.equals(android.provider.Settings.System.VOICE_CALL_SIM_SETTING)
                            && isInternetCallEnabled(mContext)) {
                        mNetworkControllerGemini.showSimIndicator(slot);
                        mToolBarView.showSimSwithPanel(businessType);
                    }
                } else if (simInfos.size() > 1) {
                    mNetworkControllerGemini.showSimIndicator(slot);
                    mToolBarView.showSimSwithPanel(businessType);
                }
            } else {
                List<SIMInfo> simInfos = SIMHelper.getSIMInfoList(mContext);
                if (simInfos == null) {
                    return;
                }
                if (businessType.equals(android.provider.Settings.System.VOICE_CALL_SIM_SETTING)
                        && isInternetCallEnabled(mContext) && simInfos.size() == 1) {
                    mNetworkController.showSimIndicator();
                    mToolBarView.showSimSwithPanel(businessType);
                }
            }
        }
        mIsSimIndicatorShowing = true;
    }

    public void hideSIMIndicator() {
        Xlog.d(TAG, "hideSIMIndicator SIM indicator.mBusinessType = " + mBusinessType);
        if (mBusinessType == null) return;
        long simId = SIMHelper.getDefaultSIM(mContext, mBusinessType);
        Xlog.d(TAG, "hideSIMIndicator, hide SIM indicator simId = "+simId+".");
        mSimIndicatorIcon.setVisibility(View.GONE);
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            mNetworkControllerGemini.hideSimIndicator(FeatureOption.GEMINI_SIM_1);
            mNetworkControllerGemini.hideSimIndicator(FeatureOption.GEMINI_SIM_2);
        } else {
            mNetworkController.hideSimIndicator();
        }
        mToolBarView.hideSimSwithPanel();
        mIsSimIndicatorShowing = false;
        mSimIndicatorIconShow = false;
    }

    private void showAlwaysAskOrInternetCall(long simId) {
        mSimIndicatorIconShow = true;
        if (simId == android.provider.Settings.System.VOICE_CALL_SIM_SETTING_INTERNET) {
            mSimIndicatorIcon.setBackgroundResource(R.drawable.sim_indicator_internet_call);
        } else {
            mSimIndicatorIcon.setBackgroundResource(R.drawable.sim_indicator_always_ask);
        }
        if (!mAirplaneMode) {
            mSimIndicatorIcon.setVisibility(View.VISIBLE);
        } else {
            mSimIndicatorIcon.setVisibility(View.GONE);
            mSimIndicatorIconShow = false;
        }
    }

    private static boolean isInternetCallEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(), Settings.System.ENABLE_INTERNET_CALL, 0) == 1;
    }
	*/
// Aurora <zhanggp> <2013-10-17> modified for systemui end
    /// M: [SystemUI] Support "SIM Indicator". }@

    /// M: [SystemUI]Show application guide for App. @{
    private Dialog mAppGuideDialog;
    private AuroraButton mAppGuideButton;
    private String mAppName;
    private View mAppGuideView;
    private static final String SHOW_APP_GUIDE_SETTING = "settings";
    private static final String MMS = "MMS";
    private static final String PHONE = "PHONE";
    private static final String CONTACTS = "CONTACTS";
    private static final String MMS_SHOW_GUIDE = "mms_show_guide";
    private static final String PHONE_SHOW_GUIDE = "phone_show_guide";
    private static final String CONTACTS_SHOW_GUIDE = "contacts_show_guide";

    public void showApplicationGuide(String appName) {
        SharedPreferences settings = mContext.getSharedPreferences(SHOW_APP_GUIDE_SETTING, 0);
        mAppName = appName;
        Xlog.d(TAG, "showApplicationGuide appName = " + appName);
        if (MMS.equals(appName) && "1".equals(settings.getString(MMS_SHOW_GUIDE, "1"))) {
            createAndShowAppGuideDialog();
        } else if (PHONE.equals(appName) && "1".equals(settings.getString(PHONE_SHOW_GUIDE, "1"))) {
            createAndShowAppGuideDialog();
        } else if (CONTACTS.equals(appName) && "1".equals(settings.getString(CONTACTS_SHOW_GUIDE, "1"))) {
            createAndShowAppGuideDialog();
        }
    }

    public void createAndShowAppGuideDialog() {
        Xlog.d(TAG, "createAndShowAppGuideDialog");
        if ((mDisabled & StatusBarManager.DISABLE_EXPAND) != 0) {
            Xlog.d(TAG, "StatusBar can not expand, so return.");
            return;
        }
        mAppGuideDialog = new ApplicationGuideDialog(mContext, R.style.ApplicationGuideDialog);
        mAppGuideDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL);
        animateExpandNotificationsPanelSlow();
        mAppGuideDialog.show();
        ObjectAnimator oa = ObjectAnimator.ofFloat(mAppGuideView, "alpha", 0.0f, 1.0f);
        oa.setDuration(1500);
        oa.start();
    }

    private class ApplicationGuideDialog extends Dialog {

        public ApplicationGuideDialog(Context context, int theme) {
            super(context, theme);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mAppGuideView = View.inflate(mContext, R.layout.application_guide, null);
            setContentView(mAppGuideView);
			/*
            if (FeatureOption.MTK_THEMEMANAGER_APP) {
                final int themeMainColor = mContext.getResources().getThemeMainColor();
                if (themeMainColor != 0) {
                    TextView applicationGuideTitle = (TextView)mAppGuideView.findViewById(R.id.applicationGuideTitleText);
                    applicationGuideTitle.setTextColor(themeMainColor);
                }
            }*/
            mAppGuideButton = (AuroraButton) mAppGuideView.findViewById(R.id.appGuideBtn);
            mAppGuideButton.setOnClickListener(mAppGuideBtnListener);
        }

        @Override
        public void onBackPressed() {
            mAppGuideDialog.dismiss();
            animateCollapsePanels();
            super.onBackPressed();
        }
        
    }

    private View.OnClickListener mAppGuideBtnListener = new View.OnClickListener() {
        public void onClick(View v) {
            Xlog.d(TAG, "onClick! dimiss application guide dialog.");
            mAppGuideDialog.dismiss();
            animateCollapsePanels();
            SharedPreferences settings = mContext.getSharedPreferences(SHOW_APP_GUIDE_SETTING, 0);
            SharedPreferences.Editor editor = settings.edit();
            if (MMS.equals(mAppName)) {
                editor.putString(MMS_SHOW_GUIDE, "0");
                editor.commit();
            } else if (PHONE.equals(mAppName)) {
                editor.putString(PHONE_SHOW_GUIDE, "0");
                editor.commit();
            } else if (CONTACTS.equals(mAppName)) {
                editor.putString(CONTACTS_SHOW_GUIDE, "0");
                editor.commit();
            }
        }
    };
    
    public void dismissAppGuide() {
        if (mAppGuideDialog != null && mAppGuideDialog.isShowing()) {
            Xlog.d(TAG, "dismiss app guide dialog");
            mAppGuideDialog.dismiss();
            mNotificationPanel.cancelTimeAnimator();
            makeExpandedInvisible();
        }
    }

    private void refreshApplicationGuide() {
        if (mAppGuideDialog != null) {
            mAppGuideView = View.inflate(mContext, R.layout.application_guide, null);
            mAppGuideDialog.setContentView(mAppGuideView);
			/*
            if (FeatureOption.MTK_THEMEMANAGER_APP) {
                final int themeMainColor = mContext.getResources().getThemeMainColor();
                if (themeMainColor != 0) {
                    TextView applicationGuideTitle = (TextView)mAppGuideView.findViewById(R.id.applicationGuideTitleText);
                    applicationGuideTitle.setTextColor(themeMainColor);
                }
            }*/
            mAppGuideButton = (AuroraButton) mAppGuideView.findViewById(R.id.appGuideBtn);
            mAppGuideButton.setOnClickListener(mAppGuideBtnListener);
        }
    }
    /// M: [SystemUI]Show application guide for App. @}

    /// M: [SystemUI]Support ThemeManager. @{
    private void refreshExpandedView(Context context) {
        for (int i = 0, n = this.mNotificationData.size(); i < n; i++) {
            Entry entry = this.mNotificationData.get(i);
            inflateViews(entry, mPile);
         // Aurora <tongyh> <2014-03-07> Horizontal and vertical screen treatment begin
            updateViewStatus(entry);
         // Aurora <tongyh> <2014-02-07> Horizontal and vertical screen treatment end
        }
        loadNotificationShade();
     // Aurora <tongyh> <2014-05-06>  Notification does not automatically expand begin
//        updateExpansionStates();
        updateExpansionStatesNoExpand();
     // Aurora <tongyh> <2014-05-06>  Notification does not automatically expand end
        setAreThereNotifications();
        mNotificationPanel.onFinishInflate();
        //mToolBarView.mSimSwitchPanelView.updateSimInfo();
        // Aurora <tongyh> <2015-01-14> delete notify settings drawable begin
        /*if (mHasFlipSettings) {
            ImageView notificationButton = (ImageView) mStatusBarWindow.findViewById(R.id.notification_button);
            if (notificationButton != null) {
                notificationButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_notifications));
            }
        }
        if (mHasSettingsPanel) {
            if (mStatusBarView.hasFullWidthNotifications()) {
                ImageView settingsButton = (ImageView) mStatusBarWindow.findViewById(R.id.settings_button);
                settingsButton.setImageDrawable(context.getResources()
                        .getDrawable(R.drawable.ic_notify_quicksettings));
            }
        } else {
            ImageView settingsButton = (ImageView) mStatusBarWindow.findViewById(R.id.settings_button);
            settingsButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_notify_settings));
        }*/
     // Aurora <tongyh> <2015-01-14> delete notify settings drawable end
        ImageView clearButton = (ImageView) mStatusBarWindow.findViewById(R.id.clear_all_button);
        clearButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_notify_clear));
//        ImageView headerSettingsButton = (ImageView) mStatusBarWindow.findViewById(R.id.header_settings_button);
//        headerSettingsButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_notify_settings));
    }
    /// M: [SystemUI]Support ThemeManager. @}

    /// M: [ALPS00438070] Handle SD Swap Condition. FeatureOption.MTK_2SDCARD_SWAP
    private ArrayList<IBinder> mNeedRemoveKeys;
    private boolean mAvoidSDAppAddNotification;
    private static final String EXTERNAL_STORAGE_PATH = "/storage/sdcard1";

    private BroadcastReceiver mMediaEjectBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            StorageVolume storageVolume = (StorageVolume) intent.getParcelableExtra(StorageVolume.EXTRA_STORAGE_VOLUME);
            //Gionee <guozj><2013-3-6> modify for CR00779171 begin
            if(storageVolume == null){
            	return;
            }
            //Gionee <guozj><2013-3-6> modify for CR00779171 end
            String path = storageVolume.getPath();
            if (!EXTERNAL_STORAGE_PATH.equals(path)) {
                return;
            }
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                if (DEBUG) Xlog.d(TAG, "receive Intent.ACTION_MEDIA_EJECT to remove notification & path = " + path);
                if (mNeedRemoveKeys.isEmpty()) {
                    return;
                }
                mAvoidSDAppAddNotification = true;
                ArrayList<IBinder> copy = (ArrayList) mNeedRemoveKeys.clone();
                for (IBinder key : copy) {
                    removeNotification(key);
                }
                copy.clear();
            } else if(action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                mAvoidSDAppAddNotification = false;
            }
        }
    };

	
	// Aurora <zhanggp> <2013-10-08> added for systemui begin
	public boolean isNotificationExpanded(){
		return !mNotificationPanel.isFullyCollapsed();
	}
	public void refreshInCallState(StatusBarNotification notification){
		
		if("com.android.phone".equals(notification.pkg) && (2 == notification.id || 500 == notification.id)){
			TelephonyManager telManager = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);	
			int state = telManager.getDefault().getCallState();
			final boolean hasActiveCall = (TelephonyManager.CALL_STATE_OFFHOOK == state && 2 == notification.id) 
				||(TelephonyManager.CALL_STATE_RINGING == state && 500 == notification.id);
			if(hasActiveCall){
					final View.OnClickListener l = getNotificationClickListener(notification);
					// Aurora <tongyh> <2014-09-16> incall view can drag statusbar begin
					isShowInCallView = true;
					mInCallView.setStatusBarNotification(notification);
					if(l != null){
						if(!isNotificationExpanded()){
						    //mStatusBarView.setVisibility(View.INVISIBLE);
						    mInCallView.setVisibility(View.VISIBLE);
						    mInCallView.setOnClickListener(l);
						}
					}
					// Aurora <tongyh> <2014-09-16> incall view can drag statusbar end
			}else{
				mInCallView.setVisibility(View.GONE);
				// Aurora <tongyh> <2014-09-16> incall view can drag statusbar begin
				isShowInCallView = false;
				// Aurora <tongyh> <2014-09-16> incall view can drag statusbar end
			}
		}
	}
	// Aurora <tongyh> <2014-04-11> Add alarm retreated to the background of green tips begin
    public void refreshAlarmClockState(StatusBarNotification notification){
		if("com.android.deskclock".equals(notification.pkg)){
			final boolean hasActiveAlram = (16210 == notification.id);
			if(hasActiveAlram){
					final View.OnClickListener l = getNotificationClickListener(notification);
					// Aurora <tongyh> <2014-09-16> alarm clock statusbar's view can drag statusbar begin
					isShowAlarmClockView = true;
					mAlarmClockView.setStatusBarNotification(notification);
					if(l != null){
						if(!isNotificationExpanded()){
						    //mStatusBarView.setVisibility(View.INVISIBLE);
						    mAlarmClockView.setVisibility(View.VISIBLE);
						    mAlarmClockView.setOnClickListener(l);
						}
					}
					// Aurora <tongyh> <2014-09-16> alarm clock statusbar's view can drag statusbar end
			}else{
				mAlarmClockView.setVisibility(View.GONE);
				// Aurora <tongyh> <2014-09-16> alarm clock statusbar's view can drag statusbar begin
				isShowAlarmClockView = false;
				// Aurora <tongyh> <2014-09-16> alarm clock statusbar's view can drag statusbar end
			}
		}
    }
 // Aurora <tongyh> <2014-04-11> Add alarm retreated to the background of green tips end
	
	
	public void setStatusbarBgFlag(int flag){
		// Aurora <tongyh> <2013-12-13> set PhoneStatusBarView background transparent begin
		if(BaseStatusBar.isCanSetStatusBarViewBg){
			BaseStatusBar.isCanSetStatusBarViewBg = false;
			return;
		}
		// Aurora <tongyh> <2013-12-13> set PhoneStatusBarView background transparent end
		if(0 == flag){
			if ((isExpanded() && !isNotificationClick) || mSetStatusbarTransparent) {
                mSetStatusbarTransparent = false;
				return; //Aurora setBackgroundColor is black, bug #2871 2014-03-07
			}
			mStatusBarView.setBackgroundColor(Color.BLACK);
		}else if(1 == flag){
			mStatusBarView.setBackgroundColor(Color.TRANSPARENT);
		}
	}
	public void hideClearButton(){
		if(mClearButton.getVisibility() == View.VISIBLE){
			if (mClearButtonAnim != null) mClearButtonAnim.cancel();
			mClearButton.setAlpha(1f);
			mClearButton.setVisibility(View.GONE);
		}
	}
	public void showClearButton(){
        final boolean any = mNotificationData.size() > 0;
        final boolean clearable = any && mNotificationData.hasClearableItems();
		if(clearable && mClearButton.getVisibility() == View.GONE && mNotificationPanel.isFullyExpanded()){
			if (mClearButtonAnim != null) mClearButtonAnim.cancel();
			mClearButton.setAlpha(1f);
			mClearButton.setVisibility(View.VISIBLE);
		}
	}

	
	private StatusBarIconView getSpecialIconView(int num){
		if(num <= 0){
			return null;
		}
		
        // Construct the icon.
        final StatusBarIconView iconView = new StatusBarIconView(mContext,
                "gn.systemui.merge.slot",
                null);
        iconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        if(num > 9){
			iconView.mAuroraNumberPain.setTextSize(mContext.getResources().getDimensionPixelSize(R.dimen.aurora_status_bar_doublenum_font_size));
		}
		
        final StatusBarIcon ic = new StatusBarIcon(null,
                    UserHandle.CURRENT,
                    R.drawable.aurora_statusbar_merge_bg,//resId,
					0,//not use
                    num,//number
                    null);
		iconView.Aurora_set(ic);
		
		return iconView;
	}
	

	private boolean handleSpecialNotification(IBinder key,StatusBarNotification notification){
		if(null == notification.tag){
			return false;
		}

		boolean handled = false;
		if(AURORA_TOSET.equals(notification.tag)){
			setStatusbarBgFlag(1);
			handled = true;
		}
		else if(AURORA_NOT_TOSET.equals(notification.tag)){
			setStatusbarBgFlag(0);
			handled = true;
		}

		if(handled){
			addData(key,notification);
	        int msg = MSG_REMOVE_NOTIFICATION;
	        mHandler.removeMessages(msg);
	        mHandler.sendEmptyMessage(msg);
		}
		return handled;
	}

	public void onAllPanelsCollapsed(){
		AuroraCloseAskDialog();
		// Aurora <tongyh> <2013-11-04> added for notification row to restore the initial state begin
		cancelSetNotificationsEnabled();
		// Aurora <tongyh> <2013-11-04> added for notification row to restore the initial state end
		// Aurora <tongyh> <2014-09-16> incall view can drag statusbar begin
				if(isShowInCallView && mInCallView != null){
		    		mInCallView.setVisibility(View.VISIBLE);
		    	}
		// Aurora <tongyh> <2014-09-16> incall view can drag statusbar end
		// Aurora <tongyh> <2014-09-16> alarm clock statusbar's view can drag statusbar begin
		if(isShowAlarmClockView && mAlarmClockView != null){
			mAlarmClockView.setVisibility(View.VISIBLE);
		}
		// Aurora <tongyh> <2014-09-16> alarm clock statusbar's view can drag statusbar end
	}
	
	// Aurora <tongyh> <2014-09-01> But the notification icon does not show the number plus one begin
	private boolean canMerge(StatusBarNotification notification){
		// Aurora <tongyh> <2014-09-19> the status bar icon code logic changes begin
//			if("com.tudou.android".equals(notification.pkg)){
		if(notification.isClearable()){
			// Aurora <tongyh> <2014-09-19> the status bar icon code logic changes begin
				return true;
			}else{
				return false;
			}
	}
    // Aurora <tongyh> <2014-09-01> But the notification icon does not show the number plus one end
	
	private boolean canShowIcon(StatusBarNotification notification){
		// Aurora <tongyh> <2013-07-17> BUG #6466，BUG #6760 begin
		if((notification.pkg.equals("com.android.phone") && notification.id == 1)
				|| (notification.pkg.equals("com.android.phone") && notification.id == (AURORA_NOTIFICATION_ID + PHONE_NOTIFICATION_ID + CALL_FORWARDING_NOTIFICATION_ID))){
    		return true;
    	}
		
		if(notification.pkg.equals("com.android.phone")  || notification.pkg.equals("com.tudou.android")  || notification.pkg.equals("com.android.music")){
		    return false;
		}
		// Aurora <tongyh> <2013-07-17> BUG #6466，BUG #6760 end
		// Aurora <tongyh> <2014-09-19> the status bar icon code logic changes begin
//		if(notification.isClearable()) return true;
		// Aurora <tongyh> <2014-09-19> the status bar icon code logic changes end
		if("android".equals(notification.pkg)
				||"com.android.systemui".equals(notification.pkg) 
				|| "com.tencent.mobileqq".equals(notification.pkg)
				|| "com.tencent.qqlite".equals(notification.pkg)
				|| "com.tencent.mm".equals(notification.pkg) 
				|| "com.android.mms".equals(notification.pkg) 
				|| "com.android.providers.downloads".equals(notification.pkg)
		//Aurora <tongyh> <2014-11-05> add aurora privacymanage icon begin  
		|| ("com.aurora.privacymanage".equals(notification.pkg) && notification.id == 10000)
		|| "com.android.email".equals(notification.pkg)
		) {
		//Aurora <tongyh> <2014-11-05> add aurora privacymanage icon end
			return true;
		}
		return false;
	}

	private boolean isNotShowIcon(String slot){
		if("ime".equals(slot)){
			return true;
		}
		return false;
	}
	// Aurora <zhanggp> <2013-10-08> added for systemui end
	 // Aurora <tongyh> <2013-12-06> add quick lock screen function begin
/*	private View.OnClickListener mQuickLockScreenListener = new View.OnClickListener() {
		public void onClick(View v) {
			new Thread(){
				@Override
				public void run() {
					sendKeyEvent(KeyEvent.KEYCODE_POWER);
				}
			}.start();

		}
	};*/
	// Aurora <tongyh> <2014-04-15> add quick lock screen function begin
    /*private View.OnTouchListener mQuickLockScreenTouchListener = new View.OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(event.getAction()==MotionEvent.ACTION_DOWN){
                if(firstClick!=0 && System.currentTimeMillis()-firstClick>500){ 
                    touchTount = 0;
                } 
                touchTount++; 
                if(touchTount==1){ 
                    firstClick = System.currentTimeMillis();
                }else if(touchTount==2){ 
                    lastClick = System.currentTimeMillis();
                    if(lastClick-firstClick<500){
                    		new Thread(){
                				@Override
                				public void run() {
                					sendKeyEvent(KeyEvent.KEYCODE_POWER);
                				}
                			}.start();
                    } 
                     
                    clear(); 
                } 
            } 
            return true;
		}
	};
	
	private void clear(){ 
		touchTount = 0; 
        firstClick = 0; 
        lastClick = 0;
    } */
	
/*private View.OnTouchListener mQuickLockScreenTouchListenerSecond = new View.OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(event.getAction()==MotionEvent.ACTION_DOWN){
                if(firstClickSecond!=0 && System.currentTimeMillis()-firstClickSecond>500){ 
                	touchTountSecond = 0;
                } 
                touchTountSecond++; 
                if(touchTountSecond==1){ 
                	firstClickSecond = System.currentTimeMillis();
                }else if(touchTountSecond==2){ 
                	lastClickSecond = System.currentTimeMillis();
                    if(lastClickSecond-firstClickSecond<500){
                    		new Thread(){
                				@Override
                				public void run() {
                					sendKeyEvent(KeyEvent.KEYCODE_POWER);
                				}
                			}.start();
                    } 
                     
                    clearSecond(); 
                } 
            } 
            return false;
		}
	};
	private void clearSecond(){ 
		touchTountSecond = 0; 
        firstClickSecond = 0; 
        lastClickSecond = 0;
    } */
	// Aurora <tongyh> <2014-04-15> add quick lock screen function begin
	private void sendKeyEvent(int keyCode) {
        long now = SystemClock.uptimeMillis();
        injectKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, 0,
        		KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0, InputDevice.SOURCE_KEYBOARD));
        injectKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0, 0,
                KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0, InputDevice.SOURCE_KEYBOARD));
    }
	
	private void injectKeyEvent(KeyEvent event) {
		InputManager.getInstance().injectInputEvent(event, InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
    }
	 // Aurora <tongyh> <2013-12-06> add quick lock screen function end
    //Aurora <tongyh> <2014-07-09> add full-screen pull-down status bar begin
    
    private void statusbarShow(){
    	mWindowManager.updateViewLayout(mView, mShowLP);
    }
    
    private void handleHide(){
    	if(mView != null){
    		mWindowManager.updateViewLayout(mView, mHideLP);
        	mWindowManager.removeView(mView);
        	mView = null;
    	}
    }

    private void handleShow() {
    	mView = new View(mContext);
        mWindowManager.addView(mView, mHideLP);
        statusbarShow();
        scheduleAutohide();
    }
    
    private void suspendAutohide() {
        mHandler.removeCallbacks(mAutohide);
        mAutohideSuspended = true;
    }
    
    private void cancelAutohide() {
        mAutohideSuspended = false;
        mHandler.removeCallbacks(mAutohide);
    }
    
    private void scheduleAutohide() {
        cancelAutohide();
        mHandler.postDelayed(mAutohide, AUTOHIDE_TIMEOUT_MS);
    }
    
    private final Runnable mAutohide = new Runnable() {
        @Override
        public void run() {
        	handleHide();
        }};
    
    public void setInteracting(boolean interacting) {
        
        if (interacting) {
            suspendAutohide();
        } else {
            resumeSuspendedAutohide();
        }
        //checkBarModes();
    }
    
    private void resumeSuspendedAutohide() {
        if (mAutohideSuspended) {
            scheduleAutohide();
        }
    }
    
    //Aurora <tongyh> <2014-07-09> add full-screen pull-down status bar end
    // Aurora <tongyh> <2014-09-16> incall view can drag statusbar begin
    public boolean isInCallViewVisible(){
    	if(mInCallView!= null){
    		if(mInCallView.getVisibility() == View.VISIBLE){
    			return true;
    		}else
    			return false;
    	}
    	else 
    		return false;
    }
    
    public void setInCallViewVisible(int mVisible){
    	if(mInCallView != null){
    		mInCallView.setVisibility(mVisible);
    	}
    }
 // Aurora <tongyh> <2014-09-16> alarm clock statusbar's view can drag statusbar begin
    public boolean isAlarmClockViewVisible(){
    	if(mAlarmClockView!= null){
    		if(mAlarmClockView.getVisibility() == View.VISIBLE){
    			return true;
    		}else
    			return false;
    	}
    	else 
    		return false;
    }
    
    public void setAlarmClockViewVisible(int mVisible){
    	if(mAlarmClockView != null){
    		mAlarmClockView.setVisibility(mVisible);
    	}
    }
 // Aurora <tongyh> <2014-09-16> alarm clock statusbar's view can drag statusbar end
    // Aurora <tongyh> <2014-09-16> incall view can drag statusbar end
//Aurora <tongyh> <2015-01-13> add account begin
    public void accountSynDistanceTime(){
    	mAccountProviderObserver.update();
    }
    
    private AccountProviderObserver mAccountProviderObserver;
    
    private void initAccountProviderObserver() {
    	mAccountProviderObserver = new AccountProviderObserver(new Handler());
    	mAccountProviderObserver.observe();
    	mAccountProviderObserver.update();
    }
    
    class AccountProviderObserver extends ContentObserver {
    	Uri uri;
    	AccountProviderObserver(Handler handler) {
            super(handler);
            uri = Uri.parse("content://com.aurora.account.accountprovider/account_info");
        }
        
        void observe() {
            mContext.getContentResolver().registerContentObserver(uri,
            false, this);
        }
        
        @Override
        public void onChange(boolean selfChange) {
            update();
        }
        
        public void update() {
//        	String[] projection = {"nick", "iconPath", "lastSyncFinishedTime", "hasLogin"}; 
        	Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
        	if(cursor != null && cursor.moveToFirst()){
        		String nickName = cursor.getString(cursor.getColumnIndex("nick"));
        		String iconPathName = cursor.getString(cursor.getColumnIndex("iconPath"));
        		long lastSyncFinishedTimeName = cursor.getLong(cursor.getColumnIndex("lastSyncFinishedTime"));
        		boolean hasLoginName = cursor.getInt(cursor.getColumnIndex("hasLogin")) == 1 ? true : false;
        		if(hasLoginName){
        			Bitmap bm = null;
        			File file = new File(iconPathName);
                    if (file.exists()) {
                            bm = BitmapFactory.decodeFile(iconPathName);
                            //The picture shows the ImageView 
                          //Aurora <tongyh> <2015-01-13> account's clipCircleBitmap begin 
                            accountIcon.setImageBitmap(loadLocalCacheIcon(bm));
                          //Aurora <tongyh> <2015-01-13> account's clipCircleBitmap end
                    }else{
                    	accountIcon.setImageResource(R.drawable.auroroa_account_login_default);
                    }
                    //Aurora <rocktong> <2015-02-05> fix bug 11544 begin
                    if(nickName.contains("&nbsp;")){
                    	nickName=nickName.replace("&nbsp;", " ");
                    }
                    //Aurora <rocktong> <2015-02-05> fix bug 11544 end
        		    accountName.setText(nickName);
        		    String sycDate = getDistanceTime(getDateTime(lastSyncFinishedTimeName));
        		    if(!"error".equals(sycDate) && sycDate != null && !"".equals(sycDate)){
        		    	if(!oldSycDistanceTime.equals(sycDate)){
        		    		oldSycDistanceTime = sycDate;
        		    		if(lastSyncFinishedTimeName == 0){
        		    			accountDate.setVisibility(View.GONE);
        		    		}else{
        		    		    accountDate.setVisibility(View.VISIBLE);
        		    		}
            		    	accountDate.setText(mContext.getString(
                	                R.string.account_syn_date, sycDate));
        		    	}
        		    }else{
        		    	accountDate.setVisibility(View.GONE);
        		    }
        		}else{
        			accountIcon.setImageResource(R.drawable.auroroa_account_default);
        			accountName.setText(R.string.account_load);
        			accountDate.setVisibility(View.GONE);
        		}
        	}else{
        		accountIcon.setImageResource(R.drawable.auroroa_account_default);
    			accountName.setText(R.string.account_load);
    			accountDate.setVisibility(View.GONE);
        	}
        }
    }
    //Aurora <rocktong> <2015-01-19> account bug begin
    /*private View.OnClickListener mAccountNameListener = new View.OnClickListener() {
        public void onClick(View v) {
        	try {
        	    Uri uri = Uri.parse("openaccount://com.aurora.account.login");
    		    Intent intent = new Intent();
    		    intent.setAction(Intent.ACTION_VIEW);
    		    intent.addCategory(Intent.CATEGORY_DEFAULT);
    		    intent.setData(uri);
    		    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    		    startActivityDismissingKeyguard(intent, true);
        	} catch (Exception e) {
            }
        }
    };*/
    
    /*private View.OnClickListener mAccountIconListener = new View.OnClickListener() {
        public void onClick(View v) {
        	try {
        	    Uri uri = Uri.parse("openaccount://com.aurora.account.login");
    		    Intent intent = new Intent();
    		    intent.setAction(Intent.ACTION_VIEW);
    		    intent.addCategory(Intent.CATEGORY_DEFAULT);
    		    intent.setData(uri);
    		    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    		    startActivityDismissingKeyguard(intent, true);
        	} catch (Exception e) {
            }
        }
    };*/
    //Aurora <rocktong> <2015-01-19> account bug end
    public String getDistanceTime(String dataTime) {
		if (dataTime.equals("0")){
			return getResourceString(0);
		}
		SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date begin = null, end = null;
		try {
			end = dfs.parse(getCurrentDateTime(dfs));
			begin = dfs.parse(dataTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if ((null != begin) && (null != end)) {
			long between = (end.getTime() - begin.getTime()) / 1000;// Divided by 1000 is to be converted into seconds
			long day = between / (24 * 3600);
			long hour = between % (24 * 3600) / 3600;
			long minute = between % 3600 / 60;
			long second = between % 60;
			long month = day / 30;
			long year = month / 12;
			String times = "";
			if (year >= 1) {
				times = dataTime.substring(2, 10);
			} else if (month >= 1) {
				times = month + getResourceString(6);
			} else if (day > 0) {
				times = day + getResourceString(5);
			} else if (hour > 0) {
				times = hour + getResourceString(4);
			} else if (minute > 0) {
				times = minute + getResourceString(3);
			} else if (second > 0) {
				times = getResourceString(1);
			} else if (second == 0) {
				times = getResourceString(1);
			}
			return times;
		} else {
			return "error";
		}
	}
    
    private String getCurrentDateTime(SimpleDateFormat dfs){
    	Date curDate = new Date(System.currentTimeMillis());
    	return dfs.format(curDate);
    }
    
    private String getDateTime(long time){
    	SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	Date sycDate = new Date(time);
    	return dfs.format(sycDate);
    }
    
    private String getResourceString(int i){
    	switch(i){
    	case 0:
    		return mContext.getString(
                    R.string.account_never);
    	case 1:
    		return mContext.getString(
                    R.string.account_just);
    	case 2:
    		return mContext.getString(
                    R.string.account_second);
    	case 3:
    		return mContext.getString(
                    R.string.account_minute);
    	case 4:
    		return mContext.getString(
                    R.string.account_hour);
    	case 5:
    		return mContext.getString(
                    R.string.account_days);
    	case 6:
    		return mContext.getString(
                    R.string.account_months);
    	default:
    		return null;
    	}
    }
//Aurora <tongyh> <2015-01-13> add account end
    //Aurora <tongyh> <2015-01-13> account's clipCircleBitmap begin 
    public static Bitmap clipCircleBitmap(Bitmap bitmap, int diameter) {
        if (bitmap == null) {
            return bitmap;
        }
        Bitmap output = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output); 
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final RectF rect = new RectF(0f, 0f, diameter, diameter);
        paint.setColor(Color.WHITE);
        canvas.drawOval(rect, paint);      
        paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, null, rect, paint);
        return output;
    }
    
    public static Bitmap loadLocalCacheIcon(Bitmap bm) {
        if (bm != null) {
            return clipCircleBitmap(bm, bm.getWidth());
        } else {
        	return null;
        }
    }
  //Aurora <tongyh> <2015-01-13> clipCircleBitmap end
  //Aurora <tongyh> <2015-02-26> fix bug BUG #10904 begin
    public void setInCallViewGone(){
    	if(mInCallView != null && View.VISIBLE == mInCallView.getVisibility()){
			TelephonyManager telManager = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
			if(TelephonyManager.CALL_STATE_IDLE == telManager.getDefault().getCallState()){
				mInCallView.setVisibility(View.GONE);
				isShowInCallView = false;
			}
		}
    }
    //Aurora <tongyh> <2015-02-26> fix bug BUG #10904 end
}
