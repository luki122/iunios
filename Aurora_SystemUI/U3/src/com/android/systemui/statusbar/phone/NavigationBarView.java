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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.os.Message;
import android.os.ServiceManager;
import android.util.AttributeSet;
import android.util.Slog;
import android.view.animation.AccelerateInterpolator;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Surface;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.content.Intent;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import android.os.SystemProperties;
import com.android.internal.statusbar.IStatusBarService;
import com.android.systemui.R;
import com.android.systemui.statusbar.BaseStatusBar;
import com.android.systemui.statusbar.DelegateViewHelper;
import com.android.systemui.statusbar.policy.KeyButtonView;
import com.android.systemui.statusbar.policy.DeadZone;
import com.android.systemui.Xlog;

public class NavigationBarView extends LinearLayout {
    final static boolean DEBUG = false;
    final static String TAG = "PhoneStatusBar/NavigationBarView";
    
    /// M: Support "ThemeManager".
    public Context mContext = null;
    
    final static boolean DEBUG_DEADZONE = false;

    final static boolean NAVBAR_ALWAYS_AT_RIGHT = true;

    // slippery nav bar when everything is disabled, e.g. during setup
    final static boolean SLIPPERY_WHEN_DISABLED= true;

    final static boolean ANIMATE_HIDE_TRANSITION = false; // turned off because it introduces unsightly delay when videos goes to full screen

    public static final int NAVIGATION_HINT_BACK_NOP      = 1 << 0;
    public static final int NAVIGATION_HINT_HOME_NOP      = 1 << 1;
    public static final int NAVIGATION_HINT_RECENT_NOP    = 1 << 2;
    
    protected IStatusBarService mBarService;
    final Display mDisplay;
    View mCurrentView = null;
    View[] mRotatedViews = new View[4];

    int mBarSize;
    boolean mVertical;
    boolean mScreenOn;

    boolean mHidden, mLowProfile, mShowMenu;
    int mDisabledFlags = 0;
    // Aurora <Felix.Duan> <2014-7-26> <BEGIN> Feature: navigation bar swipe down/right
    public int mNavigationIconHints = 0;
    // Aurora <Felix.Duan> <2014-7-26> <END> Feature: navigation bar swipe down/right

    // Aurora <Felix.Duan> <2014-5-21> <BEGIN> Implements SystemUI aurora design on Kitkat navigation bar
    private StateListDrawable mBackIcon, mBackLandIcon, mBackAltIcon, mBackAltLandIcon;
    // Aurora <Felix.Duan> <2014-5-21> <END> Implements SystemUI aurora design on Kitkat navigation bar
    
    private DelegateViewHelper mDelegateHelper;
    private DeadZone mDeadZone;
    // Aurora <Felix.Duan> <2014-6-6> <BEGIN> Support kitkat translucent bar
    private final NavigationBarTransitions mBarTransitions;
    // Aurora <Felix.Duan> <2014-6-6> <END> Support kitkat translucent bar

    // workaround for LayoutTransitions leaving the nav buttons in a weird state (bug 5549288)
    final static boolean WORKAROUND_INVALID_LAYOUT = true;
    final static int MSG_CHECK_INVALID_LAYOUT = 8686;
    // Aurora <Hazel.Lam> <2014-7-14> <BEGIN> send broadcast in order to listen navigation bar back button status 
	private String ACTION_NAVIGATION_HINT_BACK = "com.android.systemui.ACTION_NAVIGATION_HINT_BACK";
    // Aurora <Hazel.Lam> <2014-7-14> <END> send broadcast in order to listen navigation bar back button status 

    private class H extends Handler {
        public void handleMessage(Message m) {
            switch (m.what) {
                case MSG_CHECK_INVALID_LAYOUT:
                    final String how = "" + m.obj;
                    final int w = getWidth();
                    final int h = getHeight();
                    final int vw = mCurrentView.getWidth();
                    final int vh = mCurrentView.getHeight();

                    if (h != vh || w != vw) {
                        Slog.w(TAG, String.format(
                            "*** Invalid layout in navigation bar (%s this=%dx%d cur=%dx%d)",
                            how, w, h, vw, vh));
                        if (WORKAROUND_INVALID_LAYOUT) {
                            requestLayout();
                        }
                    }
                    break;
            }
        }
    }

    // Aurora <Felix.Duan> <2014-6-6> <BEGIN> Support kitkat translucent bar
    public BarTransitions getBarTransitions() {
        return mBarTransitions;
    // Aurora <Felix.Duan> <2014-6-6> <END> Support kitkat translucent bar
    }

    public void setDelegateView(View view) {
        mDelegateHelper.setDelegateView(view);
    }

    public void setBar(BaseStatusBar phoneStatusBar) {
        mDelegateHelper.setBar(phoneStatusBar);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Aurora <Felix.Duan> <2014-7-26> <BEGIN> Feature: navigation bar swipe down/right
        // now handle mVertical status
        //// Aurora <Felix.Duan> <2014-6-23> <BEGIN> Fix BUG #5378 #5222 #5393. Don`t trigger recents panel view in landscape mode.
        //if (mVertical) 
        //    return super.onTouchEvent(event);
        //// Aurora <Felix.Duan> <2014-6-23> <END> Fix BUG #5378 #5222 #5393. Don`t trigger recents panel view in landscape mode.
        // Aurora <Felix.Duan> <2014-7-26> <END> Feature: navigation bar swipe down/right
        if (mDeadZone != null && event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            mDeadZone.poke(event);
        }
        if (mDelegateHelper != null) {
            boolean ret = mDelegateHelper.onInterceptTouchEvent(event);
            if (ret) return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        // Aurora <Felix.Duan> <2014-7-26> <BEGIN> Feature: navigation bar swipe down/right
        // now handle mVertical status
        //// Aurora <Felix.Duan> <2014-6-23> <BEGIN> Fix BUG #5378 #5222 #5393. Don`t trigger recents panel view in landscape mode.
        //if (mVertical) 
        //    return super.onInterceptTouchEvent(event);
        //// Aurora <Felix.Duan> <2014-6-23> <END> Fix BUG #5378 #5222 #5393. Don`t trigger recents panel view in landscape mode.
        // Aurora <Felix.Duan> <2014-7-26> <END> Feature: navigation bar swipe down/right
        return mDelegateHelper.onInterceptTouchEvent(event);
    }

    private H mHandler = new H();

    // Aurora <Felix.Duan> <2014-6-6> <BEGIN> Support kitkat translucent bar
    public View getCurrentView() {
        return mCurrentView;
    }
    // Aurora <Felix.Duan> <2014-6-6> <END> Support kitkat translucent bar

    // Aurora <Felix.Duan> <2014-5-21> <BEGIN> Implements SystemUI aurora design on Kitkat navigation bar
    public View getAuroraMenuButton() {
        return mCurrentView.findViewById(R.id.aurora_menu);
    }
    // Aurora <Felix.Duan> <2014-5-21> <END> Implements SystemUI aurora design on Kitkat navigation bar

    public View getMenuButton() {
        return mCurrentView.findViewById(R.id.menu);
    }

    public View getBackButton() {
        return mCurrentView.findViewById(R.id.back);
    }

    public View getHomeButton() {
        return mCurrentView.findViewById(R.id.home);
    }

    // for when home is disabled, but search isn't
    public View getSearchLight() {
        return mCurrentView.findViewById(R.id.search_light);
    }

    public NavigationBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        /// M: Support "ThemeManager".
        mContext = context;
        
        mHidden = false;

        mDisplay = ((WindowManager)context.getSystemService(
                Context.WINDOW_SERVICE)).getDefaultDisplay();
        mBarService = IStatusBarService.Stub.asInterface(
                ServiceManager.getService(Context.STATUS_BAR_SERVICE));

        final Resources res = mContext.getResources();
        mBarSize = res.getDimensionPixelSize(R.dimen.navigation_bar_size);
        mVertical = false;
        mShowMenu = false;
        mDelegateHelper = new DelegateViewHelper(this);

        // Aurora <Felix.Duan> <2014-5-21> <BEGIN> Implements SystemUI aurora design on Kitkat navigation bar
        // felix tweak button icon state

		// Aurora <SteveTang> 2014-12-11. if navigationbar height is 40dp(e.g U3),use normal icon res. Others(e.g Honor-6, height is 48dp) use honor icon res. start
        mBackIcon = new StateListDrawable();
        mBackLandIcon = new StateListDrawable();
        mBackAltIcon = new StateListDrawable();
		if(isNavgationBarHeight40dp()){
		    mBackIcon.addState(new int[] {android.R.attr.state_pressed}, res.getDrawable(R.drawable.ic_sysbar_back_pressed));
		    mBackIcon.addState(new int[] {}, res.getDrawable(R.drawable.ic_sysbar_back));

		    mBackLandIcon.addState(new int[] {android.R.attr.state_pressed}, res.getDrawable(R.drawable.ic_sysbar_back_land_pressed));
		    mBackLandIcon.addState(new int[] {}, res.getDrawable(R.drawable.ic_sysbar_back_land));

		    mBackAltIcon.addState(new int[] {android.R.attr.state_pressed}, res.getDrawable(R.drawable.ic_sysbar_back_ime_pressed));
		    mBackAltIcon.addState(new int[] {}, res.getDrawable(R.drawable.ic_sysbar_back_ime));

		    mBackAltLandIcon = new StateListDrawable();
		    mBackAltLandIcon.addState(new int[] {android.R.attr.state_pressed}, res.getDrawable(R.drawable.ic_sysbar_back_ime_pressed));
		    mBackAltLandIcon.addState(new int[] {}, res.getDrawable(R.drawable.ic_sysbar_back_ime));
		} else {
			mBackIcon.addState(new int[] {android.R.attr.state_pressed}, res.getDrawable(R.drawable.ic_sysbar_back_pressed_honor));
		    mBackIcon.addState(new int[] {}, res.getDrawable(R.drawable.ic_sysbar_back_honor));

		    mBackLandIcon.addState(new int[] {android.R.attr.state_pressed}, res.getDrawable(R.drawable.ic_sysbar_back_land_pressed_honor));
		    mBackLandIcon.addState(new int[] {}, res.getDrawable(R.drawable.ic_sysbar_back_land_honor));

		    mBackAltIcon.addState(new int[] {android.R.attr.state_pressed}, res.getDrawable(R.drawable.ic_sysbar_back_ime_pressed_honor));
		    mBackAltIcon.addState(new int[] {}, res.getDrawable(R.drawable.ic_sysbar_back_ime_honor));

		    mBackAltLandIcon = new StateListDrawable();
		    mBackAltLandIcon.addState(new int[] {android.R.attr.state_pressed}, res.getDrawable(R.drawable.ic_sysbar_back_ime_pressed_honor));
		    mBackAltLandIcon.addState(new int[] {}, res.getDrawable(R.drawable.ic_sysbar_back_ime_honor));

		}
		// Aurora <SteveTang> 2014-12-11. if navigationbar height is 40dp(e.g U3),use normal icon res. Others(e.g Honor-6, height is 48dp) use honor icon res. end

        // original no state
        // mBackIcon = res.getDrawable(R.drawable.ic_sysbar_back);
        // mBackLandIcon = res.getDrawable(R.drawable.ic_sysbar_back_land);
        // mBackAltIcon = res.getDrawable(R.drawable.ic_sysbar_back_ime);
        // mBackAltLandIcon = res.getDrawable(R.drawable.ic_sysbar_back_ime);
        // Aurora <Felix.Duan> <2014-5-21> <END> Implements SystemUI aurora design on Kitkat navigation bar
        // Aurora <Felix.Duan> <2014-6-6> <BEGIN> Support kitkat translucent bar
        mBarTransitions = new NavigationBarTransitions(this);
        // Aurora <Felix.Duan> <2014-6-6> <END> Support kitkat translucent bar
    }

	// Aurora <SteveTang> 2014-12-11. wheather the navigation height is 40dp(e.g U3). start
	//whether the nav height is 40dp, use for choose diff navigationbar icon res
    private boolean isNavgationBarHeight40dp() {
		int navheightPix = mContext.getResources().getDimensionPixelSize(com.android.internal.R.dimen.navigation_bar_height);
		int judgepix = mContext.getResources().getDimensionPixelSize(R.dimen.navigationbar_height_40dp);
        android.util.Log.e("stevetang", "judgepix = " + judgepix + "  navheightPix = " + navheightPix);
        return (navheightPix == judgepix);
    }
	// Aurora <SteveTang> 2014-12-11. wheather the navigation height is 40dp(e.g U3). end

    public void notifyScreenOn(boolean screenOn) {
        mScreenOn = screenOn;
        setDisabledFlags(mDisabledFlags, true);
    }

    View.OnTouchListener mLightsOutListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent ev) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                // even though setting the systemUI visibility below will turn these views
                // on, we need them to come up faster so that they can catch this motion
                // event
                setLowProfile(false, false, false);

                try {
                    mBarService.setSystemUiVisibility(0, View.SYSTEM_UI_FLAG_LOW_PROFILE);
                } catch (android.os.RemoteException ex) {
                }
            }
            return false;
        }
    };

    public void setNavigationIconHints(int hints) {
        setNavigationIconHints(hints, false);
    }

    public void setNavigationIconHints(int hints, boolean force) {
        if (!force && hints == mNavigationIconHints) return;

        if (DEBUG) {
            android.widget.Toast.makeText(mContext,
                "Navigation icon hints = " + hints,
                500).show();
        }

        mNavigationIconHints = hints;

        // Aurora <Felix.Duan> <2014-7-14> <BEGIN> Don`t dim BACK_ALT view for IME
        //getBackButton().setAlpha(
        //    (0 != (hints & NAVIGATION_HINT_BACK_NOP)) ? 0.5f : 1.0f);
        getBackButton().setAlpha(1.0f);
        // Aurora <Felix.Duan> <2014-7-14> <END> Don`t dim BACK_ALT view for IME

        getHomeButton().setAlpha(
            (0 != (hints & NAVIGATION_HINT_HOME_NOP)) ? 0.5f : 1.0f);
        // Aurora <Felix.Duan> <2014-5-21> <BEGIN> Implements SystemUI aurora design on Kitkat navigation bar
        getAuroraMenuButton().setAlpha(
            (0 != (hints & NAVIGATION_HINT_RECENT_NOP)) ? 0.5f : 1.0f);
        // Aurora <Felix.Duan> <2014-5-21> <END> Implements SystemUI aurora design on Kitkat navigation bar

        // Aurora <Hazel.Lam> <2014-7-14> <BEGIN> send broadcast in order to listen navigation bar back button status 
        if(0 == (hints & StatusBarManager.NAVIGATION_HINT_BACK_ALT)){
			mContext.sendBroadcast(new Intent(ACTION_NAVIGATION_HINT_BACK));
		}
        // Aurora <Hazel.Lam> <2014-7-14> <END> send broadcast in order to listen navigation bar back button status 
        
        ((ImageView)getBackButton()).setImageDrawable(
            (0 != (hints & StatusBarManager.NAVIGATION_HINT_BACK_ALT))
                ? (mVertical ? mBackAltLandIcon : mBackAltIcon)
                : (mVertical ? mBackLandIcon : mBackIcon));

        setDisabledFlags(mDisabledFlags, true);
    }

    public void setDisabledFlags(int disabledFlags) {
        setDisabledFlags(disabledFlags, false);
    }

    public void setDisabledFlags(int disabledFlags, boolean force) {
        if (!force && mDisabledFlags == disabledFlags) return;

        mDisabledFlags = disabledFlags;

        final boolean disableHome = ((disabledFlags & View.STATUS_BAR_DISABLE_HOME) != 0);
        final boolean disableRecent = ((disabledFlags & View.STATUS_BAR_DISABLE_RECENT) != 0);
        final boolean disableBack = ((disabledFlags & View.STATUS_BAR_DISABLE_BACK) != 0)
                && ((mNavigationIconHints & StatusBarManager.NAVIGATION_HINT_BACK_ALT) == 0);
        final boolean disableSearch = ((disabledFlags & View.STATUS_BAR_DISABLE_SEARCH) != 0);

        if (SLIPPERY_WHEN_DISABLED) {
            // Aurora <Felix.Duan> <2014-8-8> <BEGIN> Fix BUG #5398. Disable pull up in keyguard
            //setSlippery(disableHome && disableRecent && disableBack && disableSearch);
            setSlippery(disableHome && disableRecent && disableBack);
            // Aurora <Felix.Duan> <2014-8-8> <END> Fix BUG #5398. Disable pull up in keyguard
        }

        if (!mScreenOn && mCurrentView != null) {
            ViewGroup navButtons = (ViewGroup) mCurrentView.findViewById(R.id.nav_buttons);
            LayoutTransition lt = navButtons == null ? null : navButtons.getLayoutTransition();
            if (lt != null) {
                lt.disableTransitionType(
                        LayoutTransition.CHANGE_APPEARING | LayoutTransition.CHANGE_DISAPPEARING |
                        LayoutTransition.APPEARING | LayoutTransition.DISAPPEARING);
            }
        }

        getBackButton()   .setVisibility(disableBack       ? View.INVISIBLE : View.VISIBLE);
        getHomeButton()   .setVisibility(disableHome       ? View.INVISIBLE : View.VISIBLE);
        // Aurora <Felix.Duan> <2014-5-21> <BEGIN> Implements SystemUI aurora design on Kitkat navigation bar
        getAuroraMenuButton().setVisibility(disableHome     ? View.INVISIBLE : View.VISIBLE);
        // Aurora <Felix.Duan> <2014-5-21> <END> Implements SystemUI aurora design on Kitkat navigation bar
    
        // Aurora <Felix.Duan> <2014-6-23> <BEGIN> Fix BUG #6001. IUNI style don`t need search light.
        //getSearchLight().setVisibility((disableHome && !disableSearch) ? View.VISIBLE : View.GONE);
        getSearchLight().setVisibility(View.GONE);
        // Aurora <Felix.Duan> <2014-6-23> <END> Fix BUG #6001. IUNI style don`t need search light.

        // Aurora <Felix.Duan> <2014-6-6> <BEGIN> Support kitkat translucent bar
        mBarTransitions.applyBackButtonQuiescentAlpha(mBarTransitions.getMode(), true /*animate*/);
        // Aurora <Felix.Duan> <2014-6-6> <END> Support kitkat translucent bar
    }

    public void setSlippery(boolean newSlippery) {
        WindowManager.LayoutParams lp = (WindowManager.LayoutParams) getLayoutParams();
        if (lp != null) {
            boolean oldSlippery = (lp.flags & WindowManager.LayoutParams.FLAG_SLIPPERY) != 0;
            if (!oldSlippery && newSlippery) {
                lp.flags |= WindowManager.LayoutParams.FLAG_SLIPPERY;
            } else if (oldSlippery && !newSlippery) {
                lp.flags &= ~WindowManager.LayoutParams.FLAG_SLIPPERY;
            } else {
                return;
            }
            WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
            wm.updateViewLayout(this, lp);
        }
    }

    public void setMenuVisibility(final boolean show) {
        setMenuVisibility(show, false);
    }

    public void setMenuVisibility(final boolean show, final boolean force) {
        // Aurora <Felix.Duan> <2014-5-16> <BEGIN> Show MENU key permanently
        // if (!force && mShowMenu == show) return;

        // mShowMenu = show;
        // Aurora <Felix.Duan> <2014-5-21> <BEGIN> Implements SystemUI aurora design on Kitkat navigation bar
        mShowMenu = false; // always hide menu

        getMenuButton().setVisibility(mShowMenu ? View.VISIBLE : View.INVISIBLE);
        // Aurora <Felix.Duan> <2014-5-21> <END> Implements SystemUI aurora design on Kitkat navigation bar
        // Aurora <Felix.Duan> <2014-5-16> <END> Show MENU key permanently
    }

    public void setLowProfile(final boolean lightsOut) {
        setLowProfile(lightsOut, true, false);
    }

    public void setLowProfile(final boolean lightsOut, final boolean animate, final boolean force) {
        if (!force && lightsOut == mLowProfile) return;

        mLowProfile = lightsOut;

        if (DEBUG) Slog.d(TAG, "setting lights " + (lightsOut?"out":"on"));

        final View navButtons = mCurrentView.findViewById(R.id.nav_buttons);
        final View lowLights = mCurrentView.findViewById(R.id.lights_out);

        // ok, everyone, stop it right there
        navButtons.animate().cancel();
        lowLights.animate().cancel();

        if (!animate) {
            navButtons.setAlpha(lightsOut ? 0f : 1f);

            lowLights.setAlpha(lightsOut ? 1f : 0f);
            lowLights.setVisibility(lightsOut ? View.VISIBLE : View.GONE);
        } else {
            navButtons.animate()
                .alpha(lightsOut ? 0f : 1f)
                .setDuration(lightsOut ? 750 : 250)
                .start();

            lowLights.setOnTouchListener(mLightsOutListener);
            if (lowLights.getVisibility() == View.GONE) {
                lowLights.setAlpha(0f);
                lowLights.setVisibility(View.VISIBLE);
            }
            lowLights.animate()
                .alpha(lightsOut ? 1f : 0f)
                .setDuration(lightsOut ? 750 : 250)
                .setInterpolator(new AccelerateInterpolator(2.0f))
                .setListener(lightsOut ? null : new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator _a) {
                        lowLights.setVisibility(View.GONE);
                    }
                })
                .start();
        }
    }

    public void setHidden(final boolean hide) {
        if (hide == mHidden) return;

        mHidden = hide;
        Slog.d(TAG,
            (hide ? "HIDING" : "SHOWING") + " navigation bar");

        // bring up the lights no matter what
        setLowProfile(false);
    }

    @Override
    public void onFinishInflate() {
        mRotatedViews[Surface.ROTATION_0] = 
        mRotatedViews[Surface.ROTATION_180] = findViewById(R.id.rot0);

        mRotatedViews[Surface.ROTATION_90] = findViewById(R.id.rot90);
        
        mRotatedViews[Surface.ROTATION_270] = NAVBAR_ALWAYS_AT_RIGHT
                                                ? findViewById(R.id.rot90)
                                                : findViewById(R.id.rot270);

        mCurrentView = mRotatedViews[Surface.ROTATION_0];
    }

    public void reorient() {
        final int rot = mDisplay.getRotation();
        for (int i=0; i<4; i++) {
            mRotatedViews[i].setVisibility(View.GONE);
        }
        mCurrentView = mRotatedViews[rot];
        mCurrentView.setVisibility(View.VISIBLE);

        mDeadZone = (DeadZone) mCurrentView.findViewById(R.id.deadzone);

        // force the low profile & disabled states into compliance
        // Aurora <Felix.Duan> <2014-6-6> <BEGIN> Support kitkat translucent bar
        mBarTransitions.init(mVertical);
        // Aurora <Felix.Duan> <2014-6-6> <END> Support kitkat translucent bar
        setLowProfile(mLowProfile, false, true /* force */);
        setDisabledFlags(mDisabledFlags, true /* force */);
        setMenuVisibility(mShowMenu, true /* force */);

        if (DEBUG) {
            Slog.d(TAG, "reorient(): rot=" + mDisplay.getRotation());
        }

        setNavigationIconHints(mNavigationIconHints, true);
        // Aurora <Felix.Duan> <2014-7-26> <BEGIN> Feature: navigation bar swipe down/right
        // let it know orientation status, to determine swipe down or right
        mDelegateHelper.setLandscape(mVertical);
        // Aurora <Felix.Duan> <2014-7-26> <END> Feature: navigation bar swipe down/right
    }

    public void hideForIPOShutdown() {
        for (int i=0; i<4; i++) {
            if (mRotatedViews[i] != null && mRotatedViews[i].getVisibility() != View.GONE) {
                mRotatedViews[i].setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // Aurora <Felix.Duan> <2014-5-21> <BEGIN> Implements SystemUI aurora design on Kitkat navigation bar
        mDelegateHelper.setInitialTouchRegion(getHomeButton(), getBackButton(), getAuroraMenuButton());
        // Aurora <Felix.Duan> <2014-5-21> <END> Implements SystemUI aurora design on Kitkat navigation bar
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (DEBUG) Slog.d(TAG, String.format(
                    "onSizeChanged: (%dx%d) old: (%dx%d)", w, h, oldw, oldh));

        final boolean newVertical = w > 0 && h > w;
        if (newVertical != mVertical) {
            mVertical = newVertical;
            //Slog.v(TAG, String.format("onSizeChanged: h=%d, w=%d, vert=%s", h, w, mVertical?"y":"n"));
            reorient();
        }

        postCheckForInvalidLayout("sizeChanged");
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /*
    @Override
    protected void onLayout (boolean changed, int left, int top, int right, int bottom) {
        if (DEBUG) Slog.d(TAG, String.format(
                    "onLayout: %s (%d,%d,%d,%d)", 
                    changed?"changed":"notchanged", left, top, right, bottom));
        super.onLayout(changed, left, top, right, bottom);
    }

    // uncomment this for extra defensiveness in WORKAROUND_INVALID_LAYOUT situations: if all else
    // fails, any touch on the display will fix the layout.
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (DEBUG) Slog.d(TAG, "onInterceptTouchEvent: " + ev.toString());
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            postCheckForInvalidLayout("touch");
        }
        return super.onInterceptTouchEvent(ev);
    }
    */
        

    private String getResourceName(int resId) {
        if (resId != 0) {
            final android.content.res.Resources res = mContext.getResources();
            try {
                return res.getResourceName(resId);
            } catch (android.content.res.Resources.NotFoundException ex) {
                return "(unknown)";
            }
        } else {
            return "(null)";
        }
    }

    private void postCheckForInvalidLayout(final String how) {
        mHandler.obtainMessage(MSG_CHECK_INVALID_LAYOUT, 0, 0, how).sendToTarget();
    }

    private static String visibilityToString(int vis) {
        switch (vis) {
            case View.INVISIBLE:
                return "INVISIBLE";
            case View.GONE:
                return "GONE";
            default:
                return "VISIBLE";
        }
    }

    // Aurora <Felix.Duan> <2014-5-21> <BEGIN> Implements SystemUI aurora design on Kitkat navigation bar
    /// M: Support "ThemeManager".@{
    public void upDateResources() {
        final int rot = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getRotation();
        Xlog.d(TAG, "NavigationBarView upDateResources   rot = " + rot);
        KeyButtonView backButton = (KeyButtonView) getBackButton();
        KeyButtonView homeButton = (KeyButtonView) getHomeButton();
        KeyButtonView auroraMenuButton = (KeyButtonView) getAuroraMenuButton();
        KeyButtonView menuButton = (KeyButtonView) getMenuButton();
        switch (rot) {
        case 0:
        case 2:
            backButton.setGlowBackgroud(mContext.getResources().getDrawable(R.drawable.ic_sysbar_highlight));
            homeButton.setGlowBackgroud(mContext.getResources().getDrawable(R.drawable.ic_sysbar_highlight));
            auroraMenuButton.setGlowBackgroud(mContext.getResources().getDrawable(R.drawable.ic_sysbar_highlight));
            menuButton.setGlowBackgroud(mContext.getResources().getDrawable(R.drawable.ic_sysbar_highlight));
            break;
        case 1:
            backButton.setGlowBackgroud(mContext.getResources().getDrawable(R.drawable.ic_sysbar_highlight_land));
            homeButton.setGlowBackgroud(mContext.getResources().getDrawable(R.drawable.ic_sysbar_highlight_land));
            auroraMenuButton.setGlowBackgroud(mContext.getResources().getDrawable(R.drawable.ic_sysbar_highlight_land));
            menuButton.setGlowBackgroud(mContext.getResources().getDrawable(R.drawable.ic_sysbar_highlight_land));
            break;
        default:
        }
    }
    // Aurora <Felix.Duan> <2014-5-21> <END> Implements SystemUI aurora design on Kitkat navigation bar

    // Aurora <Felix.Duan> <2014-5-21> <BEGIN> Implements SystemUI aurora design on Kitkat navigation bar
    /// M: Support "ThemeManager".@} 
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("NavigationBarView {");
        final Rect r = new Rect();
        final Point size = new Point();
        mDisplay.getRealSize(size);

        pw.println(String.format("      this: " + PhoneStatusBar.viewInfo(this)
                        + " " + visibilityToString(getVisibility())));

        getWindowVisibleDisplayFrame(r);
        final boolean offscreen = r.right > size.x || r.bottom > size.y;
        pw.println("      window: " 
                + r.toShortString()
                + " " + visibilityToString(getWindowVisibility())
                + (offscreen ? " OFFSCREEN!" : ""));

        pw.println(String.format("      mCurrentView: id=%s (%dx%d) %s",
                        getResourceName(mCurrentView.getId()),
                        mCurrentView.getWidth(), mCurrentView.getHeight(),
                        visibilityToString(mCurrentView.getVisibility())));

        pw.println(String.format("      disabled=0x%08x vertical=%s hidden=%s low=%s menu=%s",
                        mDisabledFlags,
                        mVertical ? "true" : "false",
                        mHidden ? "true" : "false",
                        mLowProfile ? "true" : "false",
                        mShowMenu ? "true" : "false"));

        final View back = getBackButton();
        final View home = getHomeButton();
        final View auroraMenu = getAuroraMenuButton();
        final View menu = getMenuButton();

        pw.println("      back: "
                + PhoneStatusBar.viewInfo(back)
                + " " + visibilityToString(back.getVisibility())
                );
        pw.println("      home: "
                + PhoneStatusBar.viewInfo(home)
                + " " + visibilityToString(home.getVisibility())
                );
        pw.println("      aurMenu: "
                + PhoneStatusBar.viewInfo(auroraMenu)
                + " " + visibilityToString(auroraMenu.getVisibility())
                );
        pw.println("      menu: "
                + PhoneStatusBar.viewInfo(menu)
                + " " + visibilityToString(menu.getVisibility())
                );
        pw.println("    }");
    }
    // Aurora <Felix.Duan> <2014-5-21> <END> Implements SystemUI aurora design on Kitkat navigation bar

}
