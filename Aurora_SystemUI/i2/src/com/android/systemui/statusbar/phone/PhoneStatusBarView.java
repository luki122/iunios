/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.app.ActivityManager;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.os.UserHandle;
import android.util.AttributeSet;
import android.util.Slog;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import com.android.systemui.R;
import android.view.VelocityTracker;

import android.view.KeyEvent;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.VelocityTracker;

import com.android.systemui.recent.HandlerBar;
import android.os.Build;

import com.android.systemui.totalCount.CountUtil;
import android.os.SystemProperties;

public class PhoneStatusBarView extends PanelBar {
    private static final String TAG = "PhoneStatusBarView";
    private static final boolean DEBUG = PhoneStatusBar.DEBUG;

    PhoneStatusBar mBar;
    int mScrimColor;
    float mSettingsPanelDragzoneFrac;
    float mSettingsPanelDragzoneMin;

    boolean mFullWidthNotifications;
    PanelView mFadingPanel = null;
    PanelView mLastFullyOpenedPanel = null;
    PanelView mNotificationPanel, mSettingsPanel;
    private boolean mShouldFade;
    private boolean isKeycodePower;
    private float mLastX;
    private float mLastY;
	private VelocityTracker mVelocityTracker;
    private float mDistantDownMotionY;
    private boolean isCanKeycodePower = true;
    private final PhoneStatusBarTransitions mBarTransitions;
    private boolean isCanSetPanelBg = true;
	
    public PhoneStatusBarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        Resources res = getContext().getResources();
        mScrimColor = res.getColor(R.color.notification_panel_scrim_color);
        mSettingsPanelDragzoneMin = res.getDimension(R.dimen.settings_panel_dragzone_min);
        try {
            mSettingsPanelDragzoneFrac = res.getFraction(R.dimen.settings_panel_dragzone_fraction, 1, 1);
        } catch (NotFoundException ex) {
            mSettingsPanelDragzoneFrac = 0f;
        }
        mFullWidthNotifications = mSettingsPanelDragzoneFrac <= 0f;
        mBarTransitions = new PhoneStatusBarTransitions(this);
    }

    public BarTransitions getBarTransitions() {
        return mBarTransitions;
    }

    public void setBar(PhoneStatusBar bar) {
        mBar = bar;
    }

    public boolean hasFullWidthNotifications() {
        return mFullWidthNotifications;
    }

    @Override
    public void onAttachedToWindow() {
        for (PanelView pv : mPanels) {
            pv.setRubberbandingEnabled(!mFullWidthNotifications);
        }
        mBarTransitions.init();
    }

    @Override
    public void addPanel(PanelView pv) {
        super.addPanel(pv);
        if (pv.getId() == R.id.notification_panel) {
            mNotificationPanel = pv;
        } else if (pv.getId() == R.id.settings_panel){
            mSettingsPanel = pv;
        }
        pv.setRubberbandingEnabled(!mFullWidthNotifications);
    }

    @Override
    public boolean panelsEnabled() {
    	// Aurora <tongyh> <2015-02-27> bug #12002 begin
//      return ((mBar.mDisabled & StatusBarManager.DISABLE_EXPAND) == 0);
    	if(Build.MODEL.contains("U810") || Build.MODEL.contains("IUNI i1") || Build.MODEL.contains("U3")|| Build.MODEL.contains("N1")) {//tymy_20150504_bug13143
    		return ((mBar.mDisabled & StatusBarManager.DISABLE_EXPAND) == 0);
    	}else{
    	    return true;
    	}
    	// Aurora <tongyh> <2015-02-27> bug #12002 end
    }

    @Override
    public boolean onRequestSendAccessibilityEvent(View child, AccessibilityEvent event) {
        if (super.onRequestSendAccessibilityEvent(child, event)) {
            // The status bar is very small so augment the view that the user is touching
            // with the content of the status bar a whole. This way an accessibility service
            // may announce the current item as well as the entire content if appropriate.
            AccessibilityEvent record = AccessibilityEvent.obtain();
            onInitializeAccessibilityEvent(record);
            dispatchPopulateAccessibilityEvent(record);
            event.appendRecord(record);
            return true;
        }
        return false;
    }

    @Override
    public PanelView selectPanelForTouch(MotionEvent touch) {
        final float x = touch.getX();

        if (mFullWidthNotifications) {
            // No double swiping. If either panel is open, nothing else can be pulled down.
            return ((mSettingsPanel == null ? 0 : mSettingsPanel.getExpandedHeight()) 
                        + mNotificationPanel.getExpandedHeight() > 0) 
                    ? null 
                    : mNotificationPanel;
        }

        // We split the status bar into thirds: the left 2/3 are for notifications, and the
        // right 1/3 for quick settings. If you pull the status bar down a second time you'll
        // toggle panels no matter where you pull it down.

        final float w = getMeasuredWidth();
        float region = (w * mSettingsPanelDragzoneFrac);

        if (DEBUG) {
            Slog.v(TAG, String.format(
                "w=%.1f frac=%.3f region=%.1f min=%.1f x=%.1f w-x=%.1f",
                w, mSettingsPanelDragzoneFrac, region, mSettingsPanelDragzoneMin, x, (w-x)));
        }

        if (region < mSettingsPanelDragzoneMin) region = mSettingsPanelDragzoneMin;

        return (w - x < region) ? mSettingsPanel : mNotificationPanel;
    }

    @Override
    public void onPanelPeeked() {
        super.onPanelPeeked();
        mBar.makeExpandedVisible(true);
		// Aurora <zhanggp> <2013-11-08> added for systemui begin
      //Aurora <tongyh> <2013-11-18> delete background blur begin
//		((NotificationPanelView)mNotificationPanel).setPanelBg();
      //Aurora <tongyh> <2013-11-05> RecentsActivity  enter and exit animation end
      //Aurora <tongyh> <2013-11-18> delete background blur end
    }

    @Override
    public void startOpeningPanel(PanelView panel) {
        super.startOpeningPanel(panel);
        // Aurora <tongyh> <2014-09-16> incall view can drag statusbar begin
        if(mBar.isInCallViewVisible()){
        	mBar.setInCallViewVisible(View.GONE);
        }
     // Aurora <tongyh> <2014-09-16> alarm clock statusbar's view can drag statusbar begin
        if(mBar.isAlarmClockViewVisible()){
        	mBar.setAlarmClockViewVisible(View.GONE);
        }
     // Aurora <tongyh> <2014-09-16> alarm clock statusbar's view can drag statusbar end
        // Aurora <tongyh> <2014-09-16> incall view can drag statusbar end
        // we only want to start fading if this is the "first" or "last" panel,
        // which is kind of tricky to determine
        if(isCanSetPanelBg){
//Aurora <tongyh> <2015-01-13> add account begin
        	 if(!SystemProperties.getBoolean("phone.type.oversea", false)){
        		 mBar.accountSynDistanceTime();
        	 }
//Aurora <tongyh> <2015-01-13> add account end
        	((NotificationPanelView)mNotificationPanel).setPanelBg();
        	isCanSetPanelBg = false;
        }
        mShouldFade = (mFadingPanel == null || mFadingPanel.isFullyExpanded());
        if (DEBUG) {
            Slog.v(TAG, "start opening: " + panel + " shouldfade=" + mShouldFade);
        }
        mFadingPanel = panel;
    }

    @Override
    public void onAllPanelsCollapsed() {
    	isCanSetPanelBg = true;
    	// Aurora <tongyh> <2014-07-28> set navifation bar low profile begin
    	mBar.setNavigationBarViewLowProfile(false);
    	// Aurora <tongyh> <2014-07-28> set navifation bar low profile end
    	
		// Aurora <zhanggp> <2013-11-01> added for systemui begin
		mBar.onAllPanelsCollapsed();
		// Aurora <zhanggp> <2013-11-01> added for systemui end

        super.onAllPanelsCollapsed();
        // give animations time to settle
        mBar.makeExpandedInvisibleSoon();
		// Aurora <zhanggp> <2013-10-28> added for systemui begin
		mBar.mStatusBarWindow.setBackgroundColor(0);
		// Aurora <zhanggp> <2013-10-28> added for systemui begin
        mFadingPanel = null;
        mLastFullyOpenedPanel = null;

        // Aurora <Felix.Duan> <2014-5-20> <BEGIN> Disable pull up feature in certain cases.
        // Enable pull up feature
        getContext().sendBroadcastAsUser(new Intent(HandlerBar.ENABLE_HANDLER),
            new UserHandle(UserHandle.USER_CURRENT));
        // Aurora <Felix.Duan> <2014-5-20> <END> Disable pull up feature in certain cases.

        // Aurora <Felix.Duan> <2014-7-14> <BEGIN> Black navi bar when panel pulled down
        Intent intent = new Intent (PhoneStatusBarPolicy.ACTION_SET_NAVIBAR_COLOR);
        intent.putExtra("mode", -1);
        mContext.sendBroadcastAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
        // Aurora <Felix.Duan> <2014-7-14> <END> Black navi bar when panel pulled down
    }

    @Override
    public void onPanelFullyOpened(PanelView openPanel) {
        super.onPanelFullyOpened(openPanel);
     // Aurora <tongyh> <2014-07-28> set navifation bar low profile begin
        mBar.setNavigationBarViewLowProfile(true);
     // Aurora <tongyh> <2014-07-28> set navifation bar low profile end
        if (openPanel != mLastFullyOpenedPanel) {
            openPanel.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
        }
        mFadingPanel = openPanel;
        mLastFullyOpenedPanel = openPanel;
        mShouldFade = true; // now you own the fade, mister
// Aurora <zhanggp> <2013-10-18> added for systemui begin
		mBar.showClearButton();
// Aurora <zhanggp> <2013-10-18> added for systemui end

        // Aurora <Felix.Duan> <2014-5-20> <BEGIN> Disable pull up feature in certain cases.
        getContext().sendBroadcastAsUser(new Intent(HandlerBar.DISABLE_HANDLER),
            new UserHandle(UserHandle.USER_CURRENT));
        // Aurora <Felix.Duan> <2014-5-20> <END> Disable pull up feature in certain cases.

        // Aurora <Felix.Duan> <2014-7-14> <BEGIN> Black navi bar when panel pulled down
        Intent intent = new Intent (PhoneStatusBarPolicy.ACTION_SET_NAVIBAR_COLOR);
        intent.putExtra("mode", BarTransitions.MODE_INSTANT_OPAQUE);
        mContext.sendBroadcastAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
        // Aurora <Felix.Duan> <2014-7-14> <END> Black navi bar when panel pulled down
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	final int action = event.getAction();
    	float x = event.getX();
    	float y = event.getY();
    	float distantY = event.getRawY();
    	if (action == MotionEvent.ACTION_DOWN) {
    		mLastX = x;
    		mLastY = y;
    		isKeycodePower = false;
    		mDistantDownMotionY = event.getRawY();
    		isCanKeycodePower = true;
    	}else if (action == MotionEvent.ACTION_MOVE) {
    		float oldTotalDy = 0;
    		float mTotalDy = y - mLastY;
    		float dy = Math.abs(distantY - mDistantDownMotionY);
    		if(dy > 90){
    			isCanKeycodePower = false;
    		}
    	}else if (action ==  MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL){
    		float mTotalDx = x - mLastX;
    		float mTotalDy = y - mLastY;
    		final int diffX = ( int ) Math.abs(mTotalDx);
            final int diffY = ( int ) Math.abs(mTotalDy);
            double angle = 0;
            if (diffX > 0) {
                angle = (double)diffY / diffX;
            }
            if (((diffX > 60  && angle < Math.tan(30 * Math.PI / 180)) || (diffY == 0 && diffX > 90)) && isCanKeycodePower){
            	isKeycodePower = true;
            }
    		if(isKeycodePower && mBar.isPhoneIdle()){
    			new Thread(){
    				@Override
    				public void run() {
						// Aurora <Steve.Tang> 2015-03-02, Count sliding lock screen counts. start
						CountUtil.getInstance(mContext).update(CountUtil.COUNT_ITEM_022, 1);
						// Aurora <Steve.Tang> 2015-03-02, Count sliding lock screen counts. end
    					sendKeyEvent(KeyEvent.KEYCODE_POWER);
    				}
    			}.start();
    		}
    		isKeycodePower = false;
    		mLastX = 0;
    		mLastY = 0;
    		isCanKeycodePower = true;
    	}
    	if(!panelsEnabled()){
    		return true;
    	}
        return mBar.interceptTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mBar.interceptTouchEvent(event) || super.onInterceptTouchEvent(event);
    }

    @Override
    public void panelExpansionChanged(PanelView panel, float frac) {
        super.panelExpansionChanged(panel, frac);

        if (DEBUG) {
            Slog.v(TAG, "panelExpansionChanged: f=" + frac);
        }

        if (panel == mFadingPanel && mScrimColor != 0 && ActivityManager.isHighEndGfx()) {
            if (mShouldFade) {
                frac = mPanelExpandedFractionSum; // don't judge me
                // let's start this 20% of the way down the screen
                frac = frac * 1.2f - 0.2f;
                if (frac <= 0) {
                    mBar.mStatusBarWindow.setBackgroundColor(0);
                } else {
                    // woo, special effects
                    final float k = (float)(1f-0.5f*(1f-Math.cos(3.14159f * Math.pow(1f-frac, 2f))));
                    // attenuate background color alpha by k
                    final int color = (int) ((mScrimColor >>> 24) * k) << 24 | (mScrimColor & 0xFFFFFF);
                    mBar.mStatusBarWindow.setBackgroundColor(color);
                }
            }
        }

        // fade out the panel as it gets buried into the status bar to avoid overdrawing the
        // status bar on the last frame of a close animation
        final int H = mBar.getStatusBarHeight();
        final float ph = panel.getExpandedHeight() + panel.getPaddingBottom();
        float alpha = 1f;
        if (ph < 2*H) {
            if (ph < H) alpha = 0f;
            else alpha = (ph - H) / H;
            alpha = alpha * alpha; // get there faster
        }
        if (panel.getAlpha() != alpha) {
            panel.setAlpha(alpha);
        }
		
        mBar.updateCarrierLabelVisibility(false);
    }
    
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
}
