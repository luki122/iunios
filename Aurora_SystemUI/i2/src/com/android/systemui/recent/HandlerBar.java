package com.android.systemui.recent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.Environment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.GnSurface;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.android.systemui.R;
import com.android.systemui.recent.RecentsActivity.TouchOutsideListener;
import com.android.systemui.recent.RecentsPanelView.RecentsScrollView;
import com.android.systemui.recent.NineGridToolBarView.ActiviyCallback;
import com.android.systemui.recent.utils.Utils;
import com.android.systemui.statusbar.BaseStatusBar;
import com.android.systemui.statusbar.phone.Blur;
import com.android.systemui.statusbar.phone.PhoneStatusBarPolicy;
import com.android.systemui.statusbar.phone.NavigationBarView;

import android.content.Intent;
import android.os.UserHandle;
import android.content.res.Resources;
import android.provider.Settings;
import android.database.ContentObserver;
import android.content.ContentResolver;
import android.os.SystemProperties;

import android.content.res.Configuration;
import android.graphics.BitmapFactory;

import com.android.systemui.totalCount.CountUtil;

public class HandlerBar extends LinearLayout {

	Context						mContext;
	WindowManager				mWM;		
	WindowManager.LayoutParams	mHandlerBarMParams;	
	WindowManager.LayoutParams  mQuickRecentPanelParams;
	View						mHandlerBarView;
	private int mTouchSlop;
	int handlerHeight = 0;
	private int phoneHeight;
	private int phoneWidth;
	boolean isHandlerBarViewVisiabe = true; 
	boolean isHandlerBarMoveBottom = false;
//	QuickRecentPanel mQuickRecentPanelView;
	public RecentsPanelView mRecentsPanelView;
	int tag = 0;
	int oldOffsetX;
	int oldOffsetY;
    private float mDownMotionX;
    protected float mLastMotionX;
    protected float mLastMotionXRemainder;
    protected float mLastMotionY;
    protected float mTotalMotionX;
    private float mDownMotionZ;
    // Aurora <Felix.Duan> <2014-10-15> <BEGIN> Fix BUG #8293. Missed pull up when orientation changed from landscape
    private float mDownTime;
    // Aurora <Felix.Duan> <2014-10-15> <END> Fix BUG #8293. Missed pull up when orientation changed from landscape
    // Aurora <Felix.Duan> <2014-5-27> <BEGIN> Implement pull up recent panel view from navigationbar
    private static int sHeight = 0;
    // Aurora <Felix.Duan> <2014-5-27> <END> Implement pull up recent panel view from navigationbar
    // Aurora <Felix.Duan> <2014-5-20> <BEGIN> Optimize pull up / down logic
    private static final int QUIT_THRESHOLD = -10; // deltaZ
    // Aurora <Felix.Duan> <2014-5-20> <END> Optimize pull up / down logic
    private static final float CLEAR_LOCK_OFFSET_Y = 190f;
//    ToolBarView mToolBarView;
    NineGridToolBarView mNineGridToolBarView;
	ToolBarIndicator mIndicator;
	private VelocityTracker mVelocityTracker;
//	private int mMaximumVelocity;
	private static final int SNAP_VELOCITY = 200;
	protected static int mRecentsScrimHeight = 0;
	protected static int mQuickSettingHeight = 0;
	// Aurora <tongyh> <2013-12-06> set the statusbar background to black begin
	PhoneStatusBarPolicy.Callback mPolicyCallback;
	// Aurora <tongyh> <2013-12-06> set the statusbar background to black end
	// Aurora <tongyh> <2013-12-29> pull up to the phone ban begin
	TelephonyManager telManager;
	// Aurora <tongyh> <2013-12-29> pull up to the phone ban end

	// Aurora <Felix.Duan> <2015-1-29> <BEGIN> Support landscape recents panel pull out
    private boolean mLandscape = false;
	// Aurora <Felix.Duan> <2015-1-29> <END> Support landscape recents panel pull out

	//
	DisplayMetrics mDisplayMetrics = new DisplayMetrics();
	Display mDisplay;
	private Bitmap mPanelBg = null;
//	Drawable mRecentsPanelViewBackground;
	//
	private int totalMoveDistant = 0;
	private boolean flg = false;
	private boolean isAutoUp = false;
	// Aurora <Felix.Duan> <2015-1-29> <BEGIN> Support landscape recents panel pull out
	private int mLastZ;
	// Aurora <Felix.Duan> <2015-1-29> <END> Support landscape recents panel pull out
	private View mRecentsLayout;
    // Aurora <Felix.Duan> <2014-4-10> <BEGIN> Disable AuroraPagedView scroll before full page loaded.
    AuroraPagedView mAuroraPagedView;
    private Handler mHandler;
    // Aurora <Felix.Duan> <2014-4-10> <END> Disable AuroraPagedView scroll before full page loaded.

    // Aurora <Felix.Duan> <2014-4-9> <BEGIN> Optimize pull up animation laggy
    public static final int ANIMATION_CONTINUE_ENTER_DURATION = 450; // 600ms
    // Aurora <Felix.Duan> <2014-4-9> <END> Optimize pull up animation laggy
	
    // Aurora <Felix.Duan> <2014-5-29> <BEGIN> Unfinished enter animation caused quick no animation
    public static final int AUTO_ENTER_ANIMATION_DURATION = 500;
    // Aurora <Felix.Duan> <2014-5-29> <END> Unfinished enter animation caused quick no animation

    // Aurora <Felix.Duan> <2014-5-5> <BEGIN> Fix BUG #3245. Capbility to hide handler bar.
    // **IMPORTANT** It is responsibility of app uses this feature to re-enable handle at proper time.
    private static Boolean mEnable = true;

    // Aurora <Felix.Duan> <2014-5-20> <BEGIN> Implement AuroraRecentPanelInvoker
    public static final String DISABLE_HANDLER = "com.android.systemui.recent.AURORA_DISABLE_HANDLER";
    public static final String ENABLE_HANDLER = "com.android.systemui.recent.AURORA_ENABLE_HANDLER";
    public static final String AURORA_BIND_INVOKER_SERVICE = "com.android.systemui.recent.AURORA_BIND_INVOKER_SERVICE";
    // Aurora <Felix.Duan> <2014-11-13> <BEGIN> Fix BUG #9458
    public static final String AURORA_BIND_INVOKER_VIEW = "com.android.systemui.recent.AURORA_BIND_INVOKER_VIEW";
    // Aurora <Felix.Duan> <2014-11-13> <END> Fix BUG #9458
    // Aurora <Felix.Duan> <2014-5-20> <END> Implement AuroraRecentPanelInvoker

    // Aurora <Felix.Duan> <2014-5-5> <END> Fix BUG #3245. Capbility to hide handler bar.

    // Aurora <Felix.Duan> <2014-8-12> <BEGIN> Fix BUG #7254.  Pull up freeze.
    // Don`t disable HandlerBar while touching
    public boolean mTouching = false;
    // Aurora <Felix.Duan> <2014-8-12> <END> Fix BUG #7254.  Pull up freeze.

    // Aurora <Felix.Duan> <2014-9-25> <BEGIN> Support pull up recent panel on kitkat
    public boolean mHasNaviBar;
    // Aurora <Felix.Duan> <2014-9-25> <END> Support pull up recent panel on kitkat

	public HandlerBar(Context context) {
		super(context);
		mContext = context;
		mHandlerBarMParams = new WindowManager.LayoutParams();
        // Aurora <Felix.Duan> <2014-7-28> <BEGIN> Pull-up refactor: preload tasks & views
        mHandlerBarMParams.setTitle("HandlerBar");
        // Aurora <Felix.Duan> <2014-7-28> <END> Pull-up refactor: preload tasks & views
		mQuickRecentPanelParams = new WindowManager.LayoutParams();
        // Aurora <Felix.Duan> <2014-7-28> <BEGIN> Pull-up refactor: preload tasks & views
    	mQuickRecentPanelParams.setTitle("Recents");
        // Aurora <Felix.Duan> <2014-7-28> <END> Pull-up refactor: preload tasks & views
		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();//
//        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();//
        Resources r=context.getResources();
        mRecentsScrimHeight = (int)(r.getDimension(R.dimen.recents_view_width_or_height));
    	mQuickSettingHeight = (int)(r.getDimension(R.dimen.quick_setting_view_height));
    	mDisplay = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();
        // Aurora <Felix.Duan> <2015-1-29> <BEGIN> Support landscape recents panel pull out
        mLandscape = isOrientationLand();
        // Aurora <Felix.Duan> <2015-1-29> <END> Support landscape recents panel pull out
	}
	
	public interface HandlerBarCallback{
    	public void removeRecentPanelView();
    	public void removeRecentPanelView(boolean isShowHandlerBar);
    	public void setRecentsPanelViewBackgroundAlpha(int alpha);
    	public void showRecentwsPanelView();
    	public void showHandlerBarView(boolean isShow);
//    	public void quickSettingViewInit(View v);
//    	public void recentsViewInit(View v);
    	public RecentsPanelView getRecentsPanelView();
    	public void removeBar();
    	public void addBar();
        // Aurora <Felix.Duan> <2015-1-29> <BEGIN> Support landscape recents panel pull out
    	public void updateOrientation(int newOri);
        // Aurora <Felix.Duan> <2015-1-29> <END> Support landscape recents panel pull out
    	
    	//iht 2015-03-23 更新图标问题
    	public void refreshViewIcons();
    }
    
    private HandlerBarCallback hcb = new HandlerBarCallback(){
    	@Override
    	public void refreshViewIcons(){
    		Log.v("iht-ssui","++++++++++++++++++++++++++++++++++++++++++++++-----------U3--------update");
    		mRecentsPanelView.refreshViewIcons();
    	}
    	
		@Override
		public void removeRecentPanelView() {
			removeRecentPanelViewAchieve();
		}
		
		@Override
		public void removeRecentPanelView(boolean isShowHandlerBar) {
			removeRecentPanelViewAchieve(isShowHandlerBar);
		}
		
		/*@Override
		public void quickSettingViewInit(View v) {
			try{
				mQuickSettingHeight = v.getHeight();
				Log.d("1130", "mQuickSettingHeight = " + mQuickSettingHeight);
//				((WindowManager)mContext.getSystemService("window")).updateViewLayout(v);
			}catch (Exception localException)
		      {
		        localException.printStackTrace();
		    }
			
		}
		
		@Override
		public void recentsViewInit(View v) {
			try{
				if(v instanceof RecentsScrollView){
					mRecentsContainerHeight = v.getHeight();
					Log.d("1130", "mRecentsContainerHeight = " + mRecentsContainerHeight);
				}else{
					mRecentsNoAppsHeight = v.getHeight();
					Log.d("1130", "mRecentsNoAppsHeight = " + mRecentsNoAppsHeight);
				}
//				((WindowManager)mContext.getSystemService("window")).updateViewLayout(v);
			}catch (Exception localException)
		      {
		        localException.printStackTrace();
		    }
		}*/

		@Override
		public void setRecentsPanelViewBackgroundAlpha(int alpha) {
			// TODO Auto-generated method stub
			setBackgroundAlpha(alpha);
		}
		@Override
		public void showRecentwsPanelView() {
			// TODO Auto-generated method stub
			showRecentsPanelViewAchieve();
		}
		@Override
		public void showHandlerBarView(boolean isShow) {
			// TODO Auto-generated method stub
			showView(isShow);
		}
		
		public RecentsPanelView getRecentsPanelView(){
			return mRecentsPanelView;
		}
		
		public void removeBar(){
			removeHandlerBar();
		}
		
    	public void addBar(){
    		addHandlerBar();
    	}
    	
        // Aurora <Felix.Duan> <2015-1-29> <BEGIN> Support landscape recents panel pull out
    	public void updateOrientation(int newOri) {
            mLandscape = (newOri == Configuration.ORIENTATION_LANDSCAPE);
            Log.d("felix","HandlerBar.DEBUG updateOrientation() mLandscape = " + mLandscape);
        }
        // Aurora <Felix.Duan> <2015-1-29> <END> Support landscape recents panel pull out
    };
		
	public void fun() {
        Log.d("felix","HandlerBar.DEBUG fun()");
		mWM = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		phoneWidth = mWM.getDefaultDisplay().getWidth();
		phoneHeight = mWM.getDefaultDisplay().getHeight();
		mHandlerBarView = LayoutInflater.from(mContext).inflate(R.layout.handlerbar, null);
//		mQuickRecentPanelView = (QuickRecentPanel)LayoutInflater.from(mContext).inflate(R.layout.quick_recent_panel, null);
		mRecentsPanelView = (RecentsPanelView)LayoutInflater.from(mContext).inflate(R.layout.quick_recent_panel, null);
		
		mRecentsPanelView.init(hcb,phoneWidth, phoneHeight);
//		mRecentsPanelViewBackground = mRecentsPanelView.getBackground();
		setBackgroundAlpha(0);
		mRecentsPanelView.setRecentsMaskViewAlpha();
		mRecentsPanelView.setRecentsQuickSettingViewAlpha();
		mRecentsLayout = mRecentsPanelView.getMoveView();
        // Aurora <Felix.Duan> <2014-4-10> <BEGIN> Disable AuroraPagedView scroll before full page loaded.
        mAuroraPagedView = (AuroraPagedView)mRecentsLayout.findViewById(R.id.recents_container);
        mHandler = new Handler();
        // Aurora <Felix.Duan> <2014-4-10> <END> Disable AuroraPagedView scroll before full page loaded.
		// Aurora <zhanggp> <2013-10-18> added for quicksetting begin
		/*mToolBarView = ( ToolBarView ) mRecentsPanelView.findViewById(R.id.tool_bar_view);
		mIndicator = ( ToolBarIndicator ) mRecentsPanelView.findViewById(R.id.indicator);
		mToolBarView.setScrollToScreenCallback(mIndicator);
		mToolBarView.setToolBarIndicator(mIndicator);
		mIndicator.setVisibility(View.GONE);
		mToolBarView.moveToDefaultScreen(false);
		mToolBarView.setActivityCB(new ActiviyCallback(){
				public void finishActivity(){
					// Aurora <tongyh> <2013-12-04> Long press the shortcut key to exit the RecentPanelview begin
					removeRecentPanelViewAchieve();
					// Aurora <tongyh> <2013-12-04> Long press the shortcut key to exit the RecentPanelview end
				}
			});*/
		mNineGridToolBarView = (NineGridToolBarView) mRecentsPanelView.findViewById(R.id.tool_bar_view);
		mNineGridToolBarView.setActivityCB(new ActiviyCallback(){
			public void finishActivity(){
				// Aurora <tongyh> <2013-12-04> Long press the shortcut key to exit the RecentPanelview begin
//				removeRecentPanelViewAchieve();
				mRecentsPanelView.removeRecentsPanelView();
				// Aurora <tongyh> <2013-12-04> Long press the shortcut key to exit the RecentPanelview end
			}
		});
		final RecentTasksLoader recentTasksLoader = RecentTasksLoader.getInstance(mContext);
		recentTasksLoader.preloadFirstTask();
		// Aurora <zhanggp> <2013-10-18> added for quicksetting end
	    recentTasksLoader.setRecentsPanel(mRecentsPanelView, mRecentsPanelView);
	    
//		mQuickRecentPanelView.initView(mWM);
        // Aurora <Felix.Duan> <2015-1-29> <BEGIN> Support landscape recents panel pull out
        // All var with Y is replaced by Z. For Z can be Y or X, depening screen orientation.
		mHandlerBarView.setOnTouchListener(new OnTouchListener() {
			
            // Aurora <Felix.Duan> <2014-5-20> <BEGIN> Optimize pull up / down logic
            /*
             * New MOTION_UP logic when finger moving slow, A.K.A. flg = false
             * if 1. panel && finger is up to less than 1/3
             * or 2. finger moving down
             * then close panels
            */
		    int lastMoveDeltaZ = 0;
            private boolean shouldClosePanel(int z, int deltaZ) {
                Log.d("felix","HandlerBar.DEBUG shouldClosePanel() z = " + z);
				return ((mLandscape ? mRecentsLayout.getScrollX() : mRecentsLayout.getScrollY()) < mRecentsScrimHeight / 3)
                            && (-z < mRecentsScrimHeight / 3)
                            || (deltaZ < QUIT_THRESHOLD || lastMoveDeltaZ < QUIT_THRESHOLD); 
            }
            // Aurora <Felix.Duan> <2014-5-20> <END> Optimize pull up / down logic

			public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_OUTSIDE == event.getAction())
                    return false;
                // Aurora <Felix.Duan> <2014-7-7> <BEGIN> Support disabling pull up on navigation bar
                if (mEnable == false) {
                    Log.d("felix","HandlerBar.DEBUG onTouch mEnable = false");
                    return false;
                }
                // Aurora <Felix.Duan> <2014-7-7> <END> Support disabling pull up on navigation bar
				
                // Aurora <Felix.Duan> <2014-7-30> <BEGIN> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804
                // anmating, skip
                if (mRecentsPanelView.mAnimating == true)
                    return false;
                // Aurora <Felix.Duan> <2014-7-30> <END> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804

				if (mVelocityTracker == null) {
		            mVelocityTracker = VelocityTracker.obtain();
		        }
				
	             
				final int action = event.getAction();
				int z = (int)(mLandscape ? event.getX() : event.getY());


                // Aurora <Felix.Duan> <2014-8-12> <BEGIN> Fix BUG #7254.  Pull up freeze.
                mTouching = true;
                // Aurora <Felix.Duan> <2014-8-12> <END> Fix BUG #7254.  Pull up freeze.

                Log.d("felix","HandlerBar.DEBUG onTouch() action = " + action + " z = " + z);
				float distantZ = (mLandscape ? event.getRawX() : event.getRawY());
				int deltaZ = 0;
				int velocityZ = 0;
				if (action == MotionEvent.ACTION_DOWN) {
                    // Aurora <Felix.Duan> <2014-10-15> <BEGIN> Fix BUG #8293. Missed pull up when orientation changed from landscape
                    mDownTime = event.getDownTime();
                    // Aurora <Felix.Duan> <2014-10-15> <END> Fix BUG #8293. Missed pull up when orientation changed from landscape
					mDownMotionZ = (mLandscape ? event.getX() : event.getY());
					isHandlerBarViewVisiabe = true;
					isHandlerBarMoveBottom = false;
					switchingHandlerPanel();
					((RecentsLayout)mRecentsLayout).closeScroll();
                    // Aurora <Felix.Duan> <2014-5-27> <BEGIN> Implement pull up recent panel view from navigationbar
                    // Trigger from navigationBar, down position is not good for mLastX/Y
					mLastZ = 0;
                    // Aurora <Felix.Duan> <2014-5-27> <END> Implement pull up recent panel view from navigationbar
				}else if (action == MotionEvent.ACTION_MOVE) {
                    // Aurora <Felix.Duan> <2014-10-15> <BEGIN> Fix BUG #8293. Missed pull up when orientation changed from landscape
                    if (mDownTime != event.getDownTime()) {
                        // If down time dosen`t match, this event comes with no down event.
                        // Better not handle it
                        Log.d("felix", "stale event");
                        return false;
                    }
                    // Aurora <Felix.Duan> <2014-10-15> <END> Fix BUG #8293. Missed pull up when orientation changed from landscape
					float dz = Math.abs(distantZ);
                    // Aurora <Felix.Duan> <2014-5-27> <BEGIN> Implement pull up recent panel view from navigationbar
                    // Only update coordinates until pointer went out of navi bar.
                    if (z <= 0){
					    isHandlerBarViewVisiabe = false;
					    mVelocityTracker.addMovement(event);
					    final VelocityTracker velocityTracker = mVelocityTracker;
		                velocityTracker.computeCurrentVelocity(50);
		                velocityZ = (int) (mLandscape
                            ? velocityTracker.getXVelocity()
                            : velocityTracker.getYVelocity());
		                if(-velocityZ > 50){
		                	isAutoUp = true;
		                }
	            	    final float directionZ = z - mDownMotionZ;
                        // Aurora <Felix.Duan> <2014-5-20> <BEGIN> Optimize pull up / down logic
		                lastMoveDeltaZ = deltaZ = mLastZ - z;
                        // Aurora <Felix.Duan> <2014-5-20> <END> Optimize pull up / down logic
		                Log.d("0310", "directionZ = " + directionZ);
		                totalMoveDistant += deltaZ;
	            	    if((int)dz != 0){
					    	isHandlerBarMoveBottom = true;
					    }
                        Log.d("felix","HandlerBar ACTION_MOVE velocityZ = " + velocityZ + "  deltaZ = " + deltaZ); 
                         // Aurora <Felix.Duan> <2014-4-9> <BEGIN> Optimize pull up animation laggy   	 
	            	     if(!mRecentsPanelView.isShown() && totalMoveDistant > 10){
                         // Aurora <Felix.Duan> <2014-4-9> <END> Optimize pull up animation laggy
	            	    	// Aurora <tongyh> <2013-12-06> set the statusbar background to black begin
	            	    	// Aurora <tongyh> <2013-12-13> set PhoneStatusBarView background transparent begin
	            	    	 BaseStatusBar.isCanSetStatusBarViewBg = true;
	            	    	// Aurora <tongyh> <2013-12-13> set PhoneStatusBarView background transparent end
	            	    	// Aurora <tongyh> <2013-12-06> set the statusbar background to black end
	            	         showRecentsPanelViewAchieve();
	            	         setPanelBg();
//	            	         mRecentsPanelView.openHardAccelerate();
					     }
	            	     
	            	     if (-velocityZ < SNAP_VELOCITY) {
	     			    	flg = false;
	     			    	if (totalMoveDistant >= mRecentsScrimHeight) {
	     			    		if(deltaZ > mRecentsScrimHeight/2){
	     			    			mRecentsPanelView.autoQuickSettingEnterAnimationForZ((mLandscape)
                                        ? mRecentsLayout.getScrollX()
                                        : mRecentsLayout.getScrollY());
                                    if (mLandscape)
                                        ((RecentsLayout)mRecentsLayout).startScroll(mRecentsLayout.getScrollX(), 0,
                                            mRecentsScrimHeight - mRecentsLayout.getScrollX(), 0, 500);
                                    else
                                        ((RecentsLayout)mRecentsLayout).startScroll(0, mRecentsLayout.getScrollY(),
                                            0, mRecentsScrimHeight - mRecentsLayout.getScrollY(), 500);
                                    // Aurora <Felix.Duan> <2014-4-10> <BEGIN> Disable AuroraPagedView scroll before full page loaded.
	     			    			pauseAuroraPagedViewScroll();
                                    // Aurora <Felix.Duan> <2014-4-10> <END> Disable AuroraPagedView scroll before full page loaded.
	     			    		}else{
                                    // Aurora <Felix.Duan> <2014-7-28> <BEGIN> Pull-up refactor: preload tasks & views
                                    ((RecentsLayout)mRecentsLayout).scroll((mLandscape
                                            ? mRecentsLayout.getScrollX()
                                            : mRecentsLayout.getScrollY()),
                                            -mRecentsScrimHeight, 200);
                                    // Aurora <Felix.Duan> <2014-7-28> <END> Pull-up refactor: preload tasks & views

	     			    		}
	     			    	} else {
	     			    		
	     			    		if((mLandscape ? mRecentsLayout.getScrollX() : mRecentsLayout.getScrollY()) + deltaZ 
                                        > mRecentsScrimHeight){
	     			    			
	     			    		}else{
	     			    			if(deltaZ > mRecentsScrimHeight/2){
	     			    				
                                        if (mLandscape)
                                            ((RecentsLayout)mRecentsLayout).startScroll(mRecentsLayout.getScrollX(), 0, deltaZ, 0, 500);
                                        else
                                            ((RecentsLayout)mRecentsLayout).startScroll(0, mRecentsLayout.getScrollY(), 0, deltaZ, 500);
                                        // Aurora <Felix.Duan> <2014-4-10> <BEGIN> Disable AuroraPagedView scroll before full page loaded.
	                                    pauseAuroraPagedViewScroll();
                                        // Aurora <Felix.Duan> <2014-4-10> <END> Disable AuroraPagedView scroll before full page loaded.
	     			    			}else{
	     			    				
                                        // Aurora <Felix.Duan> <2014-7-28> <BEGIN> Pull-up refactor: preload tasks & views
	     							    ((RecentsLayout)mRecentsLayout).scroll((mLandscape?mRecentsLayout.getScrollX():mRecentsLayout.getScrollY()), z, 200);
                                        // Aurora <Felix.Duan> <2014-7-28> <END> Pull-up refactor: preload tasks & views

	     			    			}
	     			    		}
//	     			    		}
	     			    		
	     			    	}
	     			     }else{
	     			    	
	     			    	flg = true;
	     			     }
	            	     notifyQuickRecentPanelZMove((mLandscape
                                ? mRecentsLayout.getScrollX()
                                : mRecentsLayout.getScrollY()));
                     }
                     // Aurora <Felix.Duan> <2014-5-27> <END> Implement pull up recent panel view from navigationbar
	     			 mLastZ = z;
				}else if (action ==  MotionEvent.ACTION_UP

                    // Aurora <Felix.Duan> <2014-4-21> <BEGIN> Fix BUG #2989. Handle pull up  ACTION_CANCEL case.
                    || action == MotionEvent.ACTION_CANCEL){
                    // Aurora <Felix.Duan> <2014-10-15> <BEGIN> Fix BUG #8293. Missed pull up when orientation changed from landscape
                    if (mDownTime != event.getDownTime()) {
                        // If down time dosen`t match, this event comes with no down event.
                        // Better not handle it
                        Log.d("felix", "stale event");
                        return false;
                    }
                    // Aurora <Felix.Duan> <2014-10-15> <END> Fix BUG #8293. Missed pull up when orientation changed from landscape
                    // Aurora <Felix.Duan> <2014-4-21> <END>  Fix BUG #2989. Handle pull up  ACTION_CANCEL case.

                    // Aurora <Felix.Duan> <2014-8-12> <BEGIN> Fix BUG #7254.  Pull up freeze.
                    mTouching = false;
                    // Aurora <Felix.Duan> <2014-8-12> <END> Fix BUG #7254.  Pull up freeze.

                    // Aurora <Felix.Duan> <2014-5-20> <BEGIN> Optimize pull up / down logic
		            deltaZ = mLastZ - z;
                    Log.d("felix", "HandlerBar ACTION_UP flg = " + flg
                            + "  deltaZ = " + deltaZ
                            + "  lastMoveDeltaZ = " + lastMoveDeltaZ
                            + "  isAutoUp = " + isAutoUp);
                    // Aurora <Felix.Duan> <2014-7-28> <BEGIN> Pull-up refactor: preload tasks & views
                    // skip if RecentsPanelView not showed up
                    if (!isHandlerBarViewVisiabe) {
					if(!flg){
						flg = false;
                        if (!shouldClosePanel(z, deltaZ)) { 
							isAutoUp = false;
							setBackgroundAlpha(255);
							// Aurora <Steve.Tang> 2015-03-03 count show recent panel counts by sliding from screen edge. start
							CountUtil.getInstance(mContext).update(CountUtil.COUNT_ITEM_005, 1);
							// Aurora <Steve.Tang> 2015-03-03 count show recent panel counts by sliding from screen edge. end
							mRecentsPanelView.continueAutoQuickSettingEnterAnimation();
                            // Aurora <Felix.Duan> <2014-4-9> <BEGIN> Optimize pull up animation laggy
							// Aurora <Steve.Tang> support orientation land recent panel. start
							if(!mLandscape){
								((RecentsLayout)mRecentsLayout).startScroll(0, mRecentsLayout.getScrollY(),
                                        0, mRecentsScrimHeight - mRecentsLayout.getScrollY(),
                                        ANIMATION_CONTINUE_ENTER_DURATION);

							} else {
								((RecentsLayout)mRecentsLayout).startScroll(mRecentsLayout.getScrollX(), 0,
                                        mRecentsScrimHeight - mRecentsLayout.getScrollX(),0,
                                        ANIMATION_CONTINUE_ENTER_DURATION);
							}
							// Aurora <Steve.Tang> support orientation land recent panel. end
                            // Aurora <Felix.Duan> <2014-4-10> <BEGIN> Disable AuroraPagedView scroll before full page loaded.
                            pauseAuroraPagedViewScroll();
                            // Aurora <Felix.Duan> <2014-4-10> <END> Disable AuroraPagedView scroll before full page loaded.
                            // Aurora <Felix.Duan> <2014-4-9> <END> Optimize pull up animation laggy
						} else {
							if(mLandscape)
                                ((RecentsLayout)mRecentsLayout).startScroll(mRecentsLayout.getScrollX(), 0,
                                        -mRecentsLayout.getScrollX(), 0, 500);
                            else
                                ((RecentsLayout)mRecentsLayout).startScroll(0, mRecentsLayout.getScrollY(),
                                        0, -mRecentsLayout.getScrollY(), 500);
                            // Aurora <Felix.Duan> <2014-4-10> <BEGIN> Disable AuroraPagedView scroll before full page loaded.
                            pauseAuroraPagedViewScroll();
                            // Aurora <Felix.Duan> <2014-4-10> <END> Disable AuroraPagedView scroll before full page loaded.
							mRecentsPanelView.removeRecentsPanelView();
						}
						
					}else{
						setBackgroundAlpha(255);
						// Aurora <Steve.Tang> 2015-03-03 count show recent panel counts by sliding from screen edge. start
						CountUtil.getInstance(mContext).update(CountUtil.COUNT_ITEM_005, 1);
						// Aurora <Steve.Tang> 2015-03-03 count show recent panel counts by sliding from screen edge. end
						mRecentsPanelView.continueAutoQuickSettingEnterAnimation();
                        // Aurora <Felix.Duan> <2014-4-9> <BEGIN> Optimize pull up animation laggy
                        if(mLandscape)
                            ((RecentsLayout)mRecentsLayout).startScroll(0, 0,
                                    mRecentsScrimHeight, 0,
                                    ANIMATION_CONTINUE_ENTER_DURATION);
                        else
                            ((RecentsLayout)mRecentsLayout).startScroll(0, 0,
                                    0, mRecentsScrimHeight,
                                    ANIMATION_CONTINUE_ENTER_DURATION);
                        // Aurora <Felix.Duan> <2014-4-10> <BEGIN> Disable AuroraPagedView scroll before full page loaded.
                        pauseAuroraPagedViewScroll();
                        // Aurora <Felix.Duan> <2014-4-10> <END> Disable AuroraPagedView scroll before full page loaded.
                        // Aurora <Felix.Duan> <2014-4-9> <END> Optimize pull up animation laggy
					}
                    }
					isHandlerBarViewVisiabe = true;
                    // Aurora <Felix.Duan> <2014-7-28> <END> Pull-up refactor: preload tasks & views
					totalMoveDistant = 0;
					
					
				    lastMoveDeltaZ = 0;
                    // Aurora <Felix.Duan> <2014-5-20> <END> Optimize pull up / down logic
				}
				
				velocityZ = 0;
				releaseVelocityTracker();
				return true;
			}
		});
        // Aurora <Felix.Duan> <2015-1-29> <END> Support landscape recents panel pull out
		
		
		
		// Aurora <tongyh> <2013-12-29> pull up to the phone ban end
    	
    	
    	WindowManager wm = mWM;
		mHandlerBarMParams.type = 2003; 
		mHandlerBarMParams.flags = 40;
		mHandlerBarMParams.gravity = Gravity.BOTTOM; //

		mHandlerBarMParams.width = LayoutParams.MATCH_PARENT;
		mHandlerBarMParams.height = sHeight;//45;//0;
		mHandlerBarMParams.format = -3; 

		wm.addView(mHandlerBarView, mHandlerBarMParams);
        // Aurora <Felix.Duan> <2014-7-28> <BEGIN> Pull-up refactor: preload tasks & views
        mRecentsPanelView.setVisibility(View.GONE);
		// Aurora <Steve.Tang> 2014-07-30 control NineGridToolBarView state manly. start
		mNineGridToolBarView.updateViewState(false);
		// Aurora <Steve.Tang> 2014-07-30 control NineGridToolBarView state manly. end

    	mQuickRecentPanelParams.type = WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL;
    	mQuickRecentPanelParams.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
    	mQuickRecentPanelParams.width = LayoutParams.MATCH_PARENT;
    	mQuickRecentPanelParams.height = LayoutParams.MATCH_PARENT;
    	mQuickRecentPanelParams.format = -3;
        wm.addView(mRecentsPanelView, mQuickRecentPanelParams);
        // Aurora <Felix.Duan> <2014-7-28> <END> Pull-up refactor: preload tasks & views

		handlerHeight = mHandlerBarView.getHeight();
		// Aurora <tongyh> <2013-12-29> pull up to the phone ban begin
		if(telManager == null){
			telManager = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
			telManager.listen(new HandlerBarPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
		}

        // Aurora <Felix.Duan> <2014-12-5> <BEGIN> Add Huawei Honor 6 to navigation bar device list
        // Aurora <Felix.Duan> <2014-9-25> <BEGIN> Support pull up recent panel on kitkat
        //mHasNaviBar = mContext.getResources().getBoolean(com.android.internal.R.bool.config_showNavigationBar);
        mHasNaviBar = Utils.hasNavBar();
        // Aurora <Felix.Duan> <2014-9-25> <END> Support pull up recent panel on kitkat
        // Aurora <Felix.Duan> <2014-12-5> <END> Add Huawei Honor 6 to navigation bar device list

// Aurora <Felix.Duan> <2014-5-20> <BEGIN> Implement AuroraRecentPanelInvoker
        // Aurora <Felix.Duan> <2014-5-5> <BEGIN> Fix BUG #3245. Capbility to hide handler bar.
        IntentFilter filter = new IntentFilter();
        filter.addAction(DISABLE_HANDLER);
        filter.addAction(ENABLE_HANDLER);
        filter.addAction("felix.duan.setHeight");
        // Aurora <Felix.Duan> <2014-9-25> <BEGIN> Support pull up recent panel on kitkat
        // Aurora <Felix.Duan> <2014-11-13> <BEGIN> Fix BUG #9458
        filter.addAction(AURORA_BIND_INVOKER_VIEW);
        // Aurora <Felix.Duan> <2014-11-13> <END> Fix BUG #9458
        // Aurora <Felix.Duan> <2014-9-25> <END> Support pull up recent panel on kitkat
        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (DISABLE_HANDLER.equals(intent.getAction())) {
                    // Aurora <Felix.Duan> <2014-8-12> <BEGIN> Fix BUG #7254.  Pull up freeze.
                    Log.d("felix", "HandlerBar.DEBUG onReceive() DISABLE_HANDLER mTouching = " + mTouching);
                    if (!mTouching) {
                        mEnable = false;
                        showView(false);
                        // Aurora <Felix.Duan> <2014-10-10> <BEGIN> Fix BUG #8992.
                        if (!mHasNaviBar){}
                        	//update to 5.0 begin
//                            InvokerService.getInstance().updatePanelStatus(mEnable);
                      //update to 5.0 end
                        // Aurora <Felix.Duan> <2014-10-10> <END> Fix BUG #8992.
                    }
                    // Aurora <Felix.Duan> <2014-8-12> <END> Fix BUG #7254.  Pull up freeze.
                } else if (ENABLE_HANDLER.equals(intent.getAction())){
                    Log.d("felix", "HandlerBar.DEBUG onReceive() ENABLE_HANDLER");
                    mEnable = true;
                    showView(true);
                    // Aurora <Felix.Duan> <2014-10-10> <BEGIN> Fix BUG #8992.
                    if (!mHasNaviBar){}
                  	  //update to 5.0 begin
//                        InvokerService.getInstance().updatePanelStatus(mEnable);
                  	  //update to 5.0 end
                    // Aurora <Felix.Duan> <2014-10-10> <END> Fix BUG #8992.
                } else if ("felix.duan.setHeight".equals(intent.getAction())){
                    int height = intent.getIntExtra("height",45);
                    Log.d("felix", "onReceive() height = " + height);
                    sHeight = height;
                // Aurora <Felix.Duan> <2014-11-13> <BEGIN> Fix BUG #9458
                } else if(intent.getAction().equals(AURORA_BIND_INVOKER_VIEW)) {
                // Aurora <Felix.Duan> <2014-11-13> <END> Fix BUG #9458
                    // Aurora <Felix.Duan> <2014-9-25> <BEGIN> Support pull up recent panel on kitkat
                    bindServiceView();
                    // Aurora <Felix.Duan> <2014-9-25> <END> Support pull up recent panel on kitkat
                }
            }
        }, filter);
        // Aurora <Felix.Duan> <2014-5-5> <END> Fix BUG #3245. Capbility to hide handler bar.
        // Aurora <Felix.Duan> <2014-9-25> <BEGIN> Support pull up recent panel on kitkat
        bindServiceView();
        // Aurora <Felix.Duan> <2014-9-25> <END> Support pull up recent panel on kitkat

        // Aurora <Felix.Duan> <2014-7-26> <BEGIN> Feature: navigation bar swipe down/right
        mSettingsObserver = new SettingsObserver(mHandler);
        mSettingsObserver.observe();
        // Aurora <Felix.Duan> <2014-7-26> <END> Feature: navigation bar swipe down/right

        // Aurora <Felix.Duan> <2014-11-13> <BEGIN> Fix BUG #9458
        mContext.sendBroadcast(new Intent(HandlerBar.AURORA_BIND_INVOKER_SERVICE));
        // Aurora <Felix.Duan> <2014-11-13> <END> Fix BUG #9458
	}
// Aurora <Felix.Duan> <2014-5-20> <END> Implement AuroraRecentPanelInvoker

    // Aurora <Felix.Duan> <2014-9-25> <BEGIN> Support pull up recent panel on kitkat
    // Aurora <Felix.Duan> <2014-5-20> <BEGIN> Implement AuroraRecentPanelInvoker
    private static InvokerService sInvokerService = null;
    // TODO choose proper time to bind
    private void bindServiceView(){
        Log.d("felix", "bindServiceView()");
        if (mHasNaviBar) return; 
        if (sInvokerService == null)
            sInvokerService = InvokerService.getInstance();
        if (sInvokerService != null) {
            sInvokerService.setView(mHandlerBarView);
        } else {
            Log.d("felix", "bindServiceView() sInvokerService = null");
        }
    }
    // Aurora <Felix.Duan> <2014-5-20> <END> Implement AuroraRecentPanelInvoker
    // Aurora <Felix.Duan> <2014-9-25> <END> Support pull up recent panel on kitkat

    // Aurora <Felix.Duan> <2014-4-10> <BEGIN> Disable AuroraPagedView scroll before full page loaded.
    /**
     * Pause @AuroraPagedView scroll on X axis.
     *
     * @see @AuroraPagedView#setScrollable
     *
     * @author Felix.Duan.
     * @date 2014-4-10
     * Modified @date 2014-4-28
     */
    private void pauseAuroraPagedViewScroll() {
        if (BaseStatusBar.FELIXDBG) Log.d("felix", "HandlerBar.DEBUG pauseAuroraPagedViewScroll()");
        mAuroraPagedView.setScrollable(false);
    }
    // Aurora <Felix.Duan> <2014-4-10> <END> Disable AuroraPagedView scroll before full page loaded.

    public void show(){
    	WindowManager wm = mWM;
//    	mQuickRecentPanelParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
    	mQuickRecentPanelParams.type = WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL;
    	mQuickRecentPanelParams.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
    	mQuickRecentPanelParams.width = LayoutParams.MATCH_PARENT;
    	mQuickRecentPanelParams.height = LayoutParams.MATCH_PARENT;
    	mQuickRecentPanelParams.format = -3;

//		wm.addView(mQuickRecentPanelView, mQuickRecentPanelParams);mRecentsPanelView
        // Aurora <Felix.Duan> <2014-7-28> <BEGIN> Pull-up refactor: preload tasks & views
        //wm.addView(mRecentsPanelView, mQuickRecentPanelParams);
        mRecentsPanelView.setVisibility(View.VISIBLE);
		// Aurora <Steve.Tang> 2014-07-30 control NineGridToolBarView state manly. start
		mNineGridToolBarView.updateViewState(true);
		// Aurora <Steve.Tang> 2014-07-30 control NineGridToolBarView state manly. end
        // Aurora <Felix.Duan> <2014-7-28> <END> Pull-up refactor: preload tasks & views
    	/*if(mRecentsPanelView.getVisibility() == View.GONE){
    		mRecentsPanelView.setVisibility(View.VISIBLE);
    	}*/
    	
    	

    }
    
    // Not used any more
    //public void notifyQuickRecentPanelYMoveUp(float y, float dy){
    //	mRecentsPanelView.quickRecentPanelYMoveUp(y, dy);
    //}
    //
    //public void notifyQuickRecentPanelYMoveDown(float y, float dy){
    //	mRecentsPanelView.quickRecentPanelYMoveDown(y, dy);
    //}
    
	// Aurora <Felix.Duan> <2015-1-29> <BEGIN> Support landscape recents panel pull out
    public void notifyQuickRecentPanelZMove(float scrollZ){
    	mRecentsPanelView.quickRecentPanelZMove((int)scrollZ);
    }
	// Aurora <Felix.Duan> <2015-1-29> <END> Support landscape recents panel pull out
    
    /*public void snapChild(boolean flag){
    	mRecentsPanelView.snap(flag);
    }*/
    
    public void hideBar()
    {
        Log.d("felix","HandlerBar.DEBUG hideBar()");
/*    	  mHandlerBarMParams.height = 0;
    	  mHandlerBarMParams.flags = 24;
      try
      {
        ((WindowManager)mContext.getSystemService("window")).updateViewLayout(mHandlerBarView, mHandlerBarMParams);*/
    	  mHandlerBarView.setVisibility(View.GONE);
//        return;
 /*     }
      catch (Exception localException)
      {
      }*/
    }
    
    public void resumeBar()
    {
        Log.d("felix","HandlerBar.DEBUG resumeBar()");
//        mHandlerBarMParams.height = 45;
//        mHandlerBarMParams.flags = 40;
//      try
//      {
//          ((WindowManager)this.mContext.getSystemService("window")).updateViewLayout(mHandlerBarView, mHandlerBarMParams);
    	  mHandlerBarView.setVisibility(View.VISIBLE);
//        return;
//      }
//      catch (Exception localException)
//      {
//        localException.printStackTrace();
//      }
    }
    
    public void removeHandlerBar(){
        Log.d("felix","HandlerBar.DEBUG removeHandlerBar()");
    	try{
    	((WindowManager)mContext.getSystemService("window")).removeViewImmediate(mHandlerBarView);
    	}catch (Exception localException){
    		
    	}
    }
    
    public void addHandlerBar(){
        Log.d("felix","HandlerBar.DEBUG addHandlerBar()");
    	try{
    		WindowManager wm = mWM;
    		mHandlerBarMParams.type = 2003; 
    		mHandlerBarMParams.flags = 40;
    		mHandlerBarMParams.gravity = Gravity.BOTTOM; //

    		mHandlerBarMParams.width = LayoutParams.MATCH_PARENT;
    		mHandlerBarMParams.height = sHeight;//45;//0;
    		mHandlerBarMParams.format = -3; 

    		wm.addView(mHandlerBarView, mHandlerBarMParams);
        	}catch (Exception localException){
        		
        	}
    }
    
    public void switchingHandlerPanel(){
    	  //用一个定时器定时
    	if(!isBarHide()){
    		showView(false);
    	}
    	new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if(isBarHide()) {
                	if(isBarHide()){
                		showView(true);//hide
                	}
                }
				
			}
		}, 3000);
         
        /*final Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
            Handler handler = new Handler() {
                public void handleMessage(Message msg) {
                    switch(msg.what) {
                    case 1:
                        if(isBarHide()) {
                        	if(isHandlerBarViewVisiabe){
                        		showView(true);//see
                        	}
                            if(timer != null) {
                                //cancel Timer
                                timer.cancel();
                            }
                        }
                        break;
                    }
                    super.handleMessage(msg);
                }
            };
        };
        //以后每3秒，执行一次task
        timer.schedule(task, 3000);*/
    }
    
    //控制悬浮按钮的显示或消失
    public void showView(Boolean boo) {
        if(boo && mEnable) {
        	resumeBar();
        }else {
        	hideBar();
        }
    }
    
    public boolean isBarHide(){
//    	if(mHandlerBarMParams.height == 0){
    	if(mHandlerBarView.getVisibility() == View.GONE){
    		return true;
    	}else{
    		return false;
    	}
    	
    }
    
    public void callRecentsPanelShow(){
        Log.d("felix", "HandlerBar.DEBUG callRecentsPanelShow()");

        // Aurora <Felix.Duan> <2014-8-7> <BEGIN> Low_Profile on recents
    	setNavigationBarViewLowProfile(true);
        // Aurora <Felix.Duan> <2014-8-7> <END> Low_Profile on recents

    	final RecentTasksLoader recentTasksLoader = RecentTasksLoader.getInstance(mContext);
        boolean waitingForWindowAnimation = false;
        mRecentsPanelView.show(true, recentTasksLoader.getLoadedTasks(), recentTasksLoader.isFirstScreenful(), waitingForWindowAnimation);
    }
    
    
    public void setBackgroundAlpha(int alpha){//0-229
    	mRecentsPanelView.getBackground().setAlpha(Math.min(alpha, 255));
    }
    
    private void removeRecentPanelViewAchieve(){
        // Aurora <Felix.Duan> <2014-8-7> <BEGIN> Low_Profile on recents
    	setNavigationBarViewLowProfile(false);
        // Aurora <Felix.Duan> <2014-8-7> <END> Low_Profile on recents

        // Aurora <Felix.Duan> <2014-7-30> <BEGIN> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804
        // enable pulling
        mEnable = true;
        // Aurora <Felix.Duan> <2014-7-30> <END> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804
    	if(mRecentsPanelView != null){
			try{
				if(mRecentsPanelView.isShown()){
					setBackgroundAlpha(0);
//					((WindowManager)mContext.getSystemService("window")).removeView(mRecentsPanelView);
                    // Aurora <Felix.Duan> <2014-7-28> <BEGIN> Pull-up refactor: preload tasks & views
					//((WindowManager)mContext.getSystemService("window")).removeViewImmediate(mRecentsPanelView);
        Log.d("felix", "HandlerBar.DEBUG removeRecentPanelViewAchieve() 1");
                    mRecentsPanelView.setVisibility(View.GONE);
					// Aurora <Steve.Tang> 2014-07-30 control NineGridToolBarView state manly. start
					mNineGridToolBarView.updateViewState(false);
					// Aurora <Steve.Tang> 2014-07-30 control NineGridToolBarView state manly. end
                    mAuroraPagedView.snapToPage(1);
                    // Aurora <Felix.Duan> <2014-7-28> <END> Pull-up refactor: preload tasks & views
					removeHandlerBar();
					addHandlerBar();
					Intent intent= new Intent(HandlerBar.ENABLE_HANDLER);
					mContext.sendBroadcastAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
//					if(mRecentsPanelView.getVisibility() == View.VISIBLE){
//						mRecentsPanelView.setVisibility(View.GONE);
//			    	}
					showView(true);

                    // Aurora <Felix.Duan> <2014-7-30> <BEGIN> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804
                    mRecentsPanelView.setNaviBarColorBlack(false);
                    // Aurora <Felix.Duan> <2014-7-30> <END> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804
				}
			}catch (Exception localException)
		      {
		        localException.printStackTrace();
		      }
		}
    }
    
    
    private void removeRecentPanelViewAchieve(boolean isShowHandlerBar){

        // Aurora <Felix.Duan> <2014-8-7> <BEGIN> Low_Profile on recents
    	setNavigationBarViewLowProfile(false);
        // Aurora <Felix.Duan> <2014-8-7> <END> Low_Profile on recents

        // Aurora <Felix.Duan> <2014-7-30> <BEGIN> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804
        // enable pulling
        mEnable = true;
        // Aurora <Felix.Duan> <2014-7-30> <END> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804
    	if(mRecentsPanelView != null){
			try{
				if(mRecentsPanelView.isShown()){
					setBackgroundAlpha(0);
//					((WindowManager)mContext.getSystemService("window")).removeView(mRecentsPanelView);
                    // Aurora <Felix.Duan> <2014-7-28> <BEGIN> Pull-up refactor: preload tasks & views
					//((WindowManager)mContext.getSystemService("window")).removeViewImmediate(mRecentsPanelView);
                    mRecentsPanelView.setVisibility(View.GONE);
        Log.d("felix", "HandlerBar.DEBUG removeRecentPanelViewAchieve() 2");
					// Aurora <Steve.Tang> 2014-07-30 control NineGridToolBarView state manly. start
					mNineGridToolBarView.updateViewState(false);
					// Aurora <Steve.Tang> 2014-07-30 control NineGridToolBarView state manly. end
                    mAuroraPagedView.snapToPage(1);
                    // Aurora <Felix.Duan> <2014-7-28> <END> Pull-up refactor: preload tasks & views
					removeHandlerBar();
					addHandlerBar();

					Intent intent= new Intent(HandlerBar.ENABLE_HANDLER);
					mContext.sendBroadcastAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));

//					if(mRecentsPanelView.getVisibility() == View.VISIBLE){
//						mRecentsPanelView.setVisibility(View.GONE);
//			    	}
					if(!isShowHandlerBar){
						showView(false);
					}

                    // Aurora <Felix.Duan> <2014-7-30> <BEGIN> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804
                    mRecentsPanelView.setNaviBarColorBlack(false);
                    // Aurora <Felix.Duan> <2014-7-30> <END> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804
				}
			}catch (Exception localException)
		      {
		        localException.printStackTrace();
		      }
		}
    }
    
    public void showRecentsPanelViewAchieve(){
        Log.d("felix","HandlerBar.DEBUG showRecentsPanelView()");
    	
//    	final RecentTasksLoader recentTasksLoader = RecentTasksLoader.getInstance(mContext);
// 	      recentTasksLoader.setRecentsPanel(mRecentsPanelView, mRecentsPanelView);
 	      callRecentsPanelShow();
 	      show();
// 	      RecentTasksLoader.getInstance(mContext).preloadRecentTasksList();
	      
//	      if (mRecentsPanelView != null) {
//		 mRecentsPanelView.refreshRecentTasks();
//			 mRecentsPanelView.refreshViews();
//	      }
    }
  //Aurora <tongyh> <2013-12-05> long press home key to open recentspanel view begin
    public boolean isRecentsPanelViewShow(){
    	return mRecentsPanelView.isShown();
    }
    
	// Aurora <Steve.Tang> 2015-01-19 is devices Orientation land. start
	private boolean isOrientationLand(){
		int orientation = mContext.getResources().getConfiguration().orientation;
        Log.d("felix","HandlerBar.DEBUG isOrientationLand() " + orientation);
	    if (orientation == Configuration.ORIENTATION_PORTRAIT) return false;
		return true;
	}
	// Aurora <Steve.Tang> 2015-01-19 is devices Orientation land. end

    public void showRecentsPanelViewForHomeKey(){
        Log.d("felix","HandlerBar.DEBUG showRecentsPanelViewForHomeKey()");
        // Aurora <Felix.Duan> <2014-5-5> <BEGIN> Fix BUG #3245. Capbility to hide handler bar.
        if (!mEnable) return;
        // Aurora <Felix.Duan> <2014-5-5> <END> Fix BUG #3245. Capbility to hide handler bar.
//        final RecentTasksLoader recentTasksLoader = RecentTasksLoader.getInstance(mContext);
// 	    recentTasksLoader.setRecentsPanel(mRecentsPanelView, mRecentsPanelView);
    	setBackgroundAlpha(255);
 	    callRecentsPanelShow();
 	    setPanelBg();
 	    show();
// 	    RecentTasksLoader.getInstance(mContext).preloadRecentTasksList();
	      
//	    if (mRecentsPanelView != null) {
//		 mRecentsPanelView.refreshRecentTasks();
//	        mRecentsPanelView.refreshViews();
//	    }
        // Aurora <Felix.Duan> <2014-5-29> <BEGIN> Unfinished enter animation caused quick no animation
		// Aurora <Steve.Tang> 2015-01-19 support orientation land recent panel. start
		if(!mLandscape){
			((RecentsLayout)mRecentsLayout).startScroll(0, 0, 0, HandlerBar.mRecentsScrimHeight, AUTO_ENTER_ANIMATION_DURATION);
		} else {
			((RecentsLayout)mRecentsLayout).startScroll(0, 0, HandlerBar.mRecentsScrimHeight, 0, AUTO_ENTER_ANIMATION_DURATION);
		}
		// Aurora <Steve.Tang> 2015-01-19 support orientation land recent panel. end
        // Aurora <Felix.Duan> <2014-5-29> <END> Unfinished enter animation caused quick no animation
//     	    setBackgroundAlpha(255);

			// Aurora <Steve.Tang> 2015-03-03 count show recent panel counts by long press home key. start
			CountUtil.getInstance(mContext).update(CountUtil.COUNT_ITEM_006, 1);
			// Aurora <Steve.Tang> 2015-03-03 count show recent panel counts by long press home key. end
	        mRecentsPanelView.autoQuickSettingEnterAnimation();
	        // Aurora <Felix.Duan> <2014-4-17> <BEGIN> Disable AuroraPagedView scroll before full page loaded.
            pauseAuroraPagedViewScroll();
            // Aurora <Felix.Duan> <2014-4-17> <END> Disable AuroraPagedView scroll before full page loaded.
//	        snapChild(true);
	        
	        RecentsPanelView.isResponseHomeKey = true;
    }
  //Aurora <tongyh> <2013-12-05> long press home key to open recentspanel view end
    
 // Aurora <tongyh> <2013-12-06> set the statusbar background to black begin
    public void setPhoneStatusbarPolicyCallback(PhoneStatusBarPolicy.Callback mmPolicyCallback){
    	mPolicyCallback = mmPolicyCallback;
    }
 // Aurora <tongyh> <2013-12-06> set the statusbar background to black end
    // Aurora <tongyh> <2013-12-29> pull up to the phone ban begin
    private class HandlerBarPhoneStateListener extends PhoneStateListener{
    	public void onCallStateChanged(int state, String incomingNumber) {
    		 switch (state) {
    		 case TelephonyManager.CALL_STATE_IDLE:
    			 BaseStatusBar.isPhoneRinging = false;
    			 if(isBarHide()){
    			     showView(true);
    			 }
    			 break;
    		 case TelephonyManager.CALL_STATE_OFFHOOK: 
                 // Aurora <Felix.Duan> <2014-9-28> <BEGIN> Fix BUG #8651. Allow long press HOME key pops up recent panel.
    			 BaseStatusBar.isPhoneRinging = false;
                 // Aurora <Felix.Duan> <2014-9-28> <END> Fix BUG #8651. Allow long press HOME key pops up recent panel.
    			 if(mRecentsPanelView.isShown()){
    				 mRecentsPanelView.removeRecentsPanelView(false);
    			 }else{
    			     if(!isBarHide()){
    				     showView(false);
    			     }
    			 }
    			 break;
    		 case TelephonyManager.CALL_STATE_RINGING:
    			 BaseStatusBar.isPhoneRinging = true;
    			 if(mRecentsPanelView.isShown()){
    				 mRecentsPanelView.removeRecentsPanelView(false);
    			 }else{
    			     if(!isBarHide()){
    				     showView(false);
    			     }
    			 }
    			 break;
    		 }
    		 super.onCallStateChanged(state, incomingNumber);
    	}
    }
    // Aurora <tongyh> <2013-12-29> pull up to the phone ban end
    //
    private void loadRecentBg() {
		mDisplay.getRealMetrics(mDisplayMetrics);
		if(null != mPanelBg && !mPanelBg.isRecycled()){
			mPanelBg.recycle();
		}
		try{
			Bitmap ScreenBitmap = GnSurface.screenshot((mDisplayMetrics.widthPixels>>2), (mDisplayMetrics.heightPixels>>2));
            //Aurora <tongyh> <2014-12-03> Horizontal screen notification bar background error begin
			if(mContext.getResources().getConfiguration().orientation == mContext.getResources().getConfiguration().ORIENTATION_LANDSCAPE){
			int angle = 0;
			int kk = mDisplay.getRotation();
			if(Surface.ROTATION_90 == kk){
				angle = -90;
			}else if(Surface.ROTATION_270 == kk){
				angle = 90;
			}
			Matrix matrix = new Matrix();
	        matrix.postRotate(angle);
	        ScreenBitmap =  Bitmap.createBitmap(ScreenBitmap, 0, 0, (mDisplayMetrics.widthPixels>>2), (mDisplayMetrics.heightPixels>>2),matrix,true);
			}
            //Aurora <tongyh> <2014-12-03> Horizontal screen notification bar background error end
			// Aurora <zhanggp> <2013-10-08> modified for systemui begin
			if(Settings.System.getInt(mContext.getContentResolver(),
        			"navigation_key_hide", 0) == 0){
				ScreenBitmap = chopOffNaviBar(ScreenBitmap);
			}
			// Aurora <zhanggp> <2013-10-08> modified for systemui end
			mPanelBg = Blur.fastblur(mContext, ScreenBitmap, 20);
			if (!ScreenBitmap.isRecycled()){
				ScreenBitmap.recycle();
			}
		}catch(Exception e){
			//Aurora <tongyh> <2014-10-20> NullPointerException begin
			mPanelBg = BitmapFactory.decodeResource(getContext().getResources(),R.drawable.status_bar_bg_tile);
			Log.d("NotificationPanelView", e.toString());
			//Aurora <tongyh> <2014-10-20> NullPointerException end
		}
    }

    // Aurora <Felix.Duan> <2014-6-9> <BEGIN> Fix BUG #5391. Chop off navi bar of blurred bg
    /**
     * Chop off navigation bar from screenshot to fix BUG #5391
     *
     * TODO landscape
     * @author Felix Duan
     * @date 2014-6-9
     */
    private Bitmap chopOffNaviBar(Bitmap srcBitmap) {
        // Aurora <Felix.Duan> <2014-9-25> <BEGIN> Support pull up recent panel on kitkat
        if (!mHasNaviBar) {
            // No need to chop
            return srcBitmap;
        }
        // Aurora <Felix.Duan> <2014-9-25> <END> Support pull up recent panel on kitkat
        int navHeightPx = mContext.getResources()
            .getDimensionPixelSize(com.android.internal.R.dimen.navigation_bar_height);
        int width = srcBitmap.getWidth();
        int height = srcBitmap.getHeight() - (navHeightPx >> 2);
        return Bitmap.createBitmap(srcBitmap, 0, 0, width, height);
    }
    // Aurora <Felix.Duan> <2014-6-9> <END> Fix BUG #5391. Chop off navi bar of blurred bg

	private static Bitmap small(Bitmap bitmap) {
		Matrix matrix = new Matrix();
		matrix.postScale(0.2f, 0.2f);
		Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
			bitmap.getHeight(), matrix, true);
		return resizeBmp;
	}
	public void setPanelBg(){
//		if(null == mPanelBg){
			loadRecentBg();
//		}
		if(null != mRecentsPanelView.getBackground()){
			
			mRecentsPanelView.setBackground(new BitmapDrawable(mPanelBg));
		}
	}
	private void releaseVelocityTracker() {  
	    if(null != mVelocityTracker){
		    
    	    mVelocityTracker.clear(); 
    	    mVelocityTracker.recycle();
    	    mVelocityTracker = null;  
        }
	 }
    //
	
    // Aurora <Felix.Duan> <2014-5-27> <BEGIN> Implement pull up recent panel view from navigationbar
    // For navigationBar set as it`s delegate view
    public View getHandlerBarView() {
        if (mHandlerBarView == null)
            return null;
        else
            return mHandlerBarView;
    }
    // Aurora <Felix.Duan> <2014-5-27> <END> Implement pull up recent panel view from navigationbar

    // Aurora <Felix.Duan> <2014-7-26> <BEGIN> Feature: navigation bar swipe down/right
    // Hide recents panel if navi bar is set to hidden
    private static final String NAVI_KEY_HIDE = "navigation_key_hide";
    private SettingsObserver mSettingsObserver;
    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(
                    Settings.System.getUriFor(NAVI_KEY_HIDE),
                    false, this);
        }

        @Override
        public void onChange(boolean selfChange) {
            update();
        }

        void update() {
            int status = Settings.System.getInt(
                    mContext.getContentResolver(),
                    NAVI_KEY_HIDE, 0 /*default */);
            if (status == 1)
                removeRecentPanelViewAchieve();
        }
    }
    // Aurora <Felix.Duan> <2014-7-26> <END> Feature: navigation bar swipe down/right

    // Aurora <Felix.Duan> <2014-8-7> <BEGIN> Low_Profile on recents
    private NavigationBarView mNavBarView;
    public void setNavBarView(NavigationBarView view) {
        mNavBarView = view;
    }

    public void setNavigationBarViewLowProfile(boolean lightsOut){
    	if (mNavBarView != null) {
            mNavBarView.setLowProfile(lightsOut);
        }
    }
    // Aurora <Felix.Duan> <2014-8-7> <END> Low_Profile on recents
}
