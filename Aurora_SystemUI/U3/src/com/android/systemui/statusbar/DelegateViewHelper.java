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

package com.android.systemui.statusbar;

import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import com.android.systemui.R;

import com.android.systemui.statusbar.phone.NavigationBarView;
import com.android.systemui.statusbar.policy.KeyButtonView;
import android.app.StatusBarManager;
import android.util.Log;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.provider.Settings;
import android.os.SystemProperties;
import com.android.systemui.statusbar.util.NavBarHiddenHelper;

public class DelegateViewHelper {
    private static final String TAG = "DelegateViewHelper";
    private static final boolean DBG = (SystemProperties.getInt("ro.debuggable", 0) == 1);

    private View mDelegateView;
    private View mSourceView;
    private BaseStatusBar mBar;
    private int[] mTempPoint = new int[2];
    private float[] mDownPoint = new float[2];
    private float mTriggerThreshhold;
    private boolean mPanelShowing;

    RectF mInitialTouch = new RectF();
    private boolean mStarted;
    private boolean mSwapXY = false;

    // Aurora <Felix.Duan> <2014-8-19> <BEGIN> Add guidance on hiding navigation bar
    NavBarHiddenHelper nbHelper;
    // Aurora <Felix.Duan> <2014-8-19> <END> Add guidance on hiding navigation bar

    ContentResolver mContentResolver;

    public DelegateViewHelper(View sourceView) {
        setSourceView(sourceView);
        mContentResolver = mSourceView.getContext().getContentResolver();
    }

    public void setDelegateView(View view) {
        mDelegateView = view;
    }

    public void setBar(BaseStatusBar phoneStatusBar) {
        mBar = phoneStatusBar;
        // Aurora <Felix.Duan> <2014-8-19> <BEGIN> Add guidance on hiding navigation bar
        nbHelper = new NavBarHiddenHelper(mBar.mContext);
        // Aurora <Felix.Duan> <2014-8-19> <END> Add guidance on hiding navigation bar
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mSourceView == null || mDelegateView == null
                || mBar.shouldDisableNavbarGestures()) {
            return false;
        }

        mSourceView.getLocationOnScreen(mTempPoint);
        final float sourceX = mTempPoint[0];
        final float sourceY = mTempPoint[1];

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Aurora <Felix.Duan> <2014-5-27> <BEGIN> Implement pull up recent panel view from navigationbar
                // mDelegateView, HandlerBar is always visible
                // TODO get visibile state of RecentsPanelView
                mPanelShowing = false;//mDelegateView.getVisibility() == View.VISIBLE;
                // Aurora <Felix.Duan> <2014-5-27> <END> Implement pull up recent panel view from navigationbar
                mDownPoint[0] = event.getX();
                mDownPoint[1] = event.getY();
                mStarted = mInitialTouch.contains(mDownPoint[0] + sourceX, mDownPoint[1] + sourceY);
                break;
        }

        if (!mStarted) {
            return false;
        }

        if (!mPanelShowing && event.getAction() == MotionEvent.ACTION_MOVE) {
            final int historySize = event.getHistorySize();
            for (int k = 0; k < historySize + 1; k++) {
                float x = k < historySize ? event.getHistoricalX(k) : event.getX();
                float y = k < historySize ? event.getHistoricalY(k) : event.getY();
                // Aurora <Felix.Duan> <2014-7-26> <BEGIN> Feature: navigation bar swipe down/right
                final float distance = mLandscape ? (mDownPoint[0] - x) : (mDownPoint[1] - y);
                // break pull up process
                // Aurora <Felix.Duan> <2015-1-29> <BEGIN> Support landscape recents panel pull out
                if (shouldHideNaviBar(distance)) {
                    break;
                }
                // Aurora <Felix.Duan> <2015-1-29> <END> Support landscape recents panel pull out
                // Aurora <Felix.Duan> <2014-7-26> <END> Feature: navigation bar swipe down/right
                if (distance > mTriggerThreshhold) {
                    mBar.showSearchPanel();
                    mPanelShowing = true;
                    break;
                }
            }
        } else if (!mPanelShowing && event.getAction() == MotionEvent.ACTION_UP) {
                // Aurora <Felix.Duan> <2014-7-26> <BEGIN> Feature: navigation bar swipe down/right
                final float distance = mLandscape ? (mDownPoint[0] - event.getX()) : (mDownPoint[1] - event.getY());
                if (shouldHideNaviBar(distance)) {
                    hideNaviBar();
                    return true;
                }
                // Aurora <Felix.Duan> <2014-7-26> <END> Feature: navigation bar swipe down/right
        }


        // Aurora <Felix.Duan> <2015-1-29> <BEGIN> Support landscape recents panel pull out
        // pull up
        mDelegateView.dispatchTouchEvent(event);
        // Aurora <Felix.Duan> <2015-1-29> <END> Support landscape recents panel pull out
        return mPanelShowing;
    }

    public void setSourceView(View view) {
        mSourceView = view;
        if (mSourceView != null) {
            mTriggerThreshhold = mSourceView.getContext().getResources()
                    .getDimension(R.dimen.navbar_search_up_threshhold);
        }
    }

    /**
     * Selects the initial touch region based on a list of views.  This is meant to be called by
     * a container widget on children over which the initial touch should be detected.  Note this
     * will compute a minimum bound that contains all specified views.
     *
     * @param views
     */
    public void setInitialTouchRegion(View ... views) {
        RectF bounds = new RectF();
        int p[] = new int[2];
        for (int i = 0; i < views.length; i++) {
            View view = views[i];
            if (view == null) continue;
            view.getLocationOnScreen(p);
            if (i == 0) {
                bounds.set(p[0], p[1], p[0] + view.getWidth(), p[1] + view.getHeight());
            } else {
                bounds.union(p[0], p[1], p[0] + view.getWidth(), p[1] + view.getHeight());
            }
        }
        mInitialTouch.set(bounds);
    }

    /**
     * When rotation is set to NO_SENSOR, then this allows swapping x/y for gesture detection
     * @param swap
     */
    public void setSwapXY(boolean swap) {
        mSwapXY = swap;
    }

    // Aurora <Felix.Duan> <2014-7-26> <BEGIN> Feature: navigation bar swipe down/right
    private boolean mLandscape = false;
    // Trigger distance
    private static final int DRAG_DISTANCE = 36;//13;//32;//10;//7;//15;
    // Key in Settings.System
    private static final String NAVI_KEY_HIDE = "navigation_key_hide";

    private boolean shouldHideNaviBar(float distance) {
        log("shouldHideNaviBar() distance = " + distance);
        if (-distance < DRAG_DISTANCE)
            return false;
        // Aurora <Felix.Duan> <2014-9-24> <BEGIN> Add switcher to hide navigation bar feature.turn false;
        if (!allowHideNaviBar())
            return false;
        // Aurora <Felix.Duan> <2014-9-24> <END> Add switcher to hide navigation bar feature.
        return true;
    }

    // Aurora <Felix.Duan> <2014-9-24> <BEGIN> Add switcher to hide navigation bar feature.
    /**
     * Hide navigation bar feature switcher.
     * Persisted at Settings.System with int value:
     *  1       allow to hide
     *  others  not allow
     *
     *  default returns true 
     */
    private static final String NAVI_KEY_HIDE_ALLOW = "navigation_key_hide_allow";

    private boolean allowHideNaviBar() {
        return Settings.System.getInt(mContentResolver, NAVI_KEY_HIDE_ALLOW, 1) == 1;
    }
    // Aurora <Felix.Duan> <2014-9-24> <END> Add switcher to hide navigation bar feature.

    private void hideNaviBar() {
        int hints = ((NavigationBarView)mSourceView).mNavigationIconHints;
        log("hideNaviBar() hints = " + hints);
        // if sot input method is showing, skip hiding
        if (0 == (hints & StatusBarManager.NAVIGATION_HINT_BACK_ALT)) {
            ContentValues values = new ContentValues();
            values.put("name", NAVI_KEY_HIDE);
            values.put("value", 1);
            mContentResolver.insert(Settings.System.CONTENT_URI, values);
        }

        // Aurora <Felix.Duan> <2014-8-19> <BEGIN> Add guidance on hiding navigation bar
        nbHelper.nbHidden();
        // Aurora <Felix.Duan> <2014-8-19> <END> Add guidance on hiding navigation bar
    }

    // swipe down or right according to landscape
    public void setLandscape(boolean landscape) {
        mLandscape = landscape;
    }

    private void log(String msg) {
        if (DBG) Log.d(TAG, msg);
    }
    // Aurora <Felix.Duan> <2014-7-26> <END> Feature: navigation bar swipe down/right
}
