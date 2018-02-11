/*
 * Copyright (C) 2011 The Android Open Source Project
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
package com.android.browser;

import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.webkit.WebView;

import com.android.browser.BrowserWebView.OnScrollChangedListener;
import com.android.phase1.cinema.Cinema;
import com.android.phase1.cinema.CinemaMan;
import com.android.phase1.cinema.CinemaMan.CinemaListener;
import com.android.phase1.cinema.WebviewStatusMachine;

/**
 * Helper class to manage when to show the URL bar based off of touch
 * input, and when to begin the hide timer.
 */
public class UrlBarAutoShowManager implements OnTouchListener,
        OnScrollChangedListener {

    private static float V_TRIGGER_ANGLE = .9f;
    private static long SCROLL_TIMEOUT_DURATION = 150;
    private static long IGNORE_INTERVAL = 250;

    private BrowserWebView mTarget;
    private BaseUi mUi;

    private int mSlop;

    private float mStartTouchX;
    private float mStartTouchY;
    private boolean mIsTracking;
    private boolean mHasTriggered;
    private long mLastScrollTime;
    private long mTriggeredTime;
    private boolean mIsScrolling;
    
    private final WebviewStatusMachine mWebviewStatusMachine;
    
    /**
     * 
     * Vulcan created this method in 2015年3月31日 上午11:02:24 .
     */
    public void onAskTitleBar() {
    	mWebviewStatusMachine.onAskTitleBar();
    	return;
    }

    public UrlBarAutoShowManager(BaseUi ui) {
        mUi = ui;
        ViewConfiguration config = ViewConfiguration.get(mUi.getActivity());
        mSlop = config.getScaledTouchSlop() * 2;
        
        View webview,titlebar,toolbar;
        webview = mUi.mMainContentView;
        titlebar = mUi.mFixedTitlebarContainer;
        toolbar = mUi.toolBarContainer;
        mWebviewStatusMachine = new WebviewStatusMachine(mUi, webview,titlebar,toolbar);
    }

    public void setTarget(BrowserWebView v) {
        if (mTarget == v) return;

        if (mTarget != null) {
            mTarget.setOnTouchListener(null);
            mTarget.setOnScrollChangedListener(null);
        }
        mTarget = v;
        if (mTarget != null) {
            mTarget.setOnTouchListener(this);
            mTarget.setOnScrollChangedListener(this);
        }
    }

    @Override
    public void onScrollChanged(int l, int t, int oldl, int oldt) {
    	
    	mWebviewStatusMachine.onScrollChanged(l, t, oldl, oldt);
    	
        mLastScrollTime = SystemClock.uptimeMillis();
        mIsScrolling = true;
        if (t != 0) {
            // If it is showing, extend it
            if (mUi.isTitleBarShowing()) {
                long remaining = mLastScrollTime - mTriggeredTime;
                remaining = Math.max(BaseUi.HIDE_TITLEBAR_DELAY - remaining,
                        SCROLL_TIMEOUT_DURATION);
                mUi.showTitleBarForDuration(remaining);
            }
        } else {
            mUi.suggestHideTitleBar();
//            if(!mIsTracking) {
//            	mWebviewStatusMachine.mSlideTimesFromBottom = 0;
//            	mWebviewStatusMachine.startCinema(CinemaMan.CINEMA_TYPE_REPLAY_INIT,new CinemaListener() {
//					@Override
//					public void onCinemaEnd(Cinema cinema) {
//						mWebviewStatusMachine.setCinemaProgress(CinemaMan.CINEMA_TYPE_WEBVIEW, 0f);
//						mWebviewStatusMachine.setWebviewEventSwitch(true);
//						mWebviewStatusMachine.changeStatus(null, WebviewStatusMachine.STATUS_INIT);
//						mWebviewStatusMachine.changeTargetStatus(null,WebviewStatusMachine.STATUS_NULL);
//					}
//				});
//            }
        }
    }

    void stopTracking() {
        if (mIsTracking) {
            mIsTracking = false;
            mIsScrolling = false;
            if (mUi.isTitleBarShowing()) {
                mUi.showTitleBarForDuration();
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
    	
    	//Anyhow, all events will be transfered to mWebviewStatusMachine
    	mWebviewStatusMachine.onTouchEvent(v,event);
    	
        if (event.getPointerCount() > 1) {
            stopTracking();
        }
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
        	if(mUi.mNavigationBar.mUrlInput.mState != UrlInputView.StateListener.STATE_NORMAL) {
        		//长按地址栏后弹出键盘，点击键盘外的区域，收起键盘
        		BaseUi.onBackPressed();
        	}
            if (!mIsTracking && event.getPointerCount() == 1) {
                long sinceLastScroll =
                        SystemClock.uptimeMillis() - mLastScrollTime;
                if (sinceLastScroll < IGNORE_INTERVAL) {
                    break;
                }
                mStartTouchY = event.getY();
                mStartTouchX = event.getX();
                mIsTracking = true;
                mHasTriggered = false;
            }
            break;
        case MotionEvent.ACTION_MOVE:
            if (mIsTracking && !mHasTriggered) {
                WebView web = (WebView) v;
                float dy = event.getY() - mStartTouchY;
                float ady = Math.abs(dy);
                float adx = Math.abs(event.getX() - mStartTouchX);
                if (ady > mSlop) {
                    mHasTriggered = true;
                    float angle = (float) Math.atan2(ady, adx);
                    if (dy > mSlop && angle > V_TRIGGER_ANGLE
                            && !mUi.isTitleBarShowing()
                            && (web.getVisibleTitleHeight() == 0
                            || (!mIsScrolling && web.getScrollY() > 0))) {
                        mTriggeredTime = SystemClock.uptimeMillis();
                        mUi.showTitleBar();
                    }
                }
            }
            break;
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            stopTracking();
            break;
        }
        
        
        
        return !mWebviewStatusMachine.getWebviewEventSwitch(event);
    }

}
