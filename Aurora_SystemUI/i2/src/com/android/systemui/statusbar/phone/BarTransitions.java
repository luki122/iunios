/*
 * Copyright (C) 2013 The Android Open Source Project
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

import android.animation.TimeInterpolator;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.android.systemui.R;

import android.graphics.Color;
import java.lang.Exception;

public class BarTransitions {
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_COLORS = false;

    public static final int MODE_OPAQUE = 0;
    public static final int MODE_SEMI_TRANSPARENT = 1;
    public static final int MODE_TRANSLUCENT = 2;
    public static final int MODE_LIGHTS_OUT = 3;
    // Aurora <Felix.Duan> <2014-6-10> <BEGIN> Support full transparent status bar
    /*
     * Notice: this mode is not manipulated from PhoneStatusBar.setSystemUiVisibility()
     * So mStatusBarMode/mNavigationBarMode does not mark this status,
     * app may need to send Intent to set this mode more often than they expected.
     * TODO merge IUNI feature into android standard
     *
     * @author Felix Duan
     * @date 2014-6-10
     */
    public static final int MODE_TRANSPARENT = 4;
    // Aurora <Felix.Duan> <2014-6-10> <END> Support full transparent status bar

    // Aurora <Felix.Duan> <2014-6-18> <BEGIN> Support NaviBar full mode change from intent
    // Turn opaque immediatly, used for pull up RecentsPanelView
    public static final int MODE_INSTANT_OPAQUE = 5;
    // Aurora <Felix.Duan> <2014-6-18> <END> Support NaviBar full mode change from intent
    // Aurora <Felix.Duan> <2014-7-30> <BEGIN> Feature : custom navi bar color
    // Set custom navi bg color
    public static final int MODE_CUSTOM_COLOR = 6;
    public static final int MODE_RESET = -1;
    // Aurora <Felix.Duan> <2014-7-30> <END> Feature : custom navi bar color

    public static final int LIGHTS_IN_DURATION = 250;
    public static final int LIGHTS_OUT_DURATION = 750;
    public static final int BACKGROUND_DURATION = 200;

    private final String mTag;
    private final View mView;
    private final boolean mSupportsTransitions = ActivityManager.isHighEndGfx();
    private final BarBackgroundDrawable mBarBackground;

    private int mMode;
    // Aurora <Felix.Duan> <2014-7-30> <BEGIN> Feature : custom navi bar color
    // hold target custom color
    private static int auroraTargetColor = 0;
    // Aurora <Felix.Duan> <2014-7-30> <END> Feature : custom navi bar color

    public BarTransitions(View view, int gradientResourceId) {
        mTag = "BarTransitions." + view.getClass().getSimpleName();
        mView = view;
        mBarBackground = new BarBackgroundDrawable(mView.getContext(), gradientResourceId);
        if (mSupportsTransitions) {
            mView.setBackground(mBarBackground);
        }
    }

    public int getMode() {
        return mMode;
    }

    public String getTag() {
        return mTag;
    }

    // Aurora <Felix.Duan> <2014-7-30> <BEGIN> Feature : custom navi bar color
    // Parse color string to color value, then apply
    public void transitionTo(int mode, boolean animate, String hexColor) {
        int cloneMode = mode;
        try {
            auroraTargetColor = Color.parseColor(hexColor);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("felix", "transitionTo() Exception hexColor = " + hexColor
                + "  " + e.toString());
            auroraTargetColor = 0;
            cloneMode = -1;
        }
        Log.d("felix", "color transitionTo() hexColor = " + hexColor
            + " auroraTargetColor = " + auroraTargetColor);
        transitionTo(cloneMode, animate);
    }
    // Aurora <Felix.Duan> <2014-7-30> <END> Feature : custom navi bar color

    public void transitionTo(int mode, boolean animate) {
        if (mMode == mode) return;
        int oldMode = mMode;
        mMode = mode;
        if (DEBUG) Log.d(mTag, String.format("%s -> %s animate=%s",
                modeToString(oldMode), modeToString(mode),  animate));
        if (mSupportsTransitions) {
            onTransition(oldMode, mMode, animate);
        }
    }

    protected void onTransition(int oldMode, int newMode, boolean animate) {
        applyModeBackground(oldMode, newMode, animate);
    }

    protected void applyModeBackground(int oldMode, int newMode, boolean animate) {
        if (DEBUG) Log.d(mTag, String.format("applyModeBackground oldMode=%s newMode=%s animate=%s",
                modeToString(oldMode), modeToString(newMode), animate));
        mBarBackground.applyModeBackground(oldMode, newMode, animate);
    }

    public static String modeToString(int mode) {
        if (mode == MODE_OPAQUE) return "MODE_OPAQUE";
        if (mode == MODE_SEMI_TRANSPARENT) return "MODE_SEMI_TRANSPARENT";
        if (mode == MODE_TRANSLUCENT) return "MODE_TRANSLUCENT";
        if (mode == MODE_LIGHTS_OUT) return "MODE_LIGHTS_OUT";
        // Aurora <Felix.Duan> <2014-6-10> <BEGIN> Support full transparent status bar
        if (mode == MODE_TRANSPARENT) return "MODE_TRANSPARENT";
        // Aurora <Felix.Duan> <2014-6-18> <BEGIN> Support NaviBar full mode change from intent
        if (mode == MODE_INSTANT_OPAQUE) return "MODE_INSTANT_OPAQUE";
        // Aurora <Felix.Duan> <2014-6-18> <END> Support NaviBar full mode change from intent
        // Aurora <Felix.Duan> <2014-6-10> <END> Support full transparent status bar
        if (mode == MODE_CUSTOM_COLOR) return "MODE_CUSTOM_COLOR";
        if (mode == MODE_RESET) return "MODE_RESET";
        throw new IllegalArgumentException("Unknown mode " + mode);
    }

    public void finishAnimations() {
        mBarBackground.finishAnimation();
    }

    public void setContentVisible(boolean visible) {
        // for subclasses
    }

    private static class BarBackgroundDrawable extends Drawable {
        private final int mOpaque;
        private final int mSemiTransparent;
        // Aurora <Felix.Duan> <2014-6-10> <BEGIN> Support full transparent status bar
        private final int mTransparent;
        // Aurora <Felix.Duan> <2014-6-10> <END> Support full transparent status bar

        // Aurora <Felix.Duan> <2014-6-18> <BEGIN> Support NaviBar full mode change from intent
        // Currently black, maybe other colors later.
        private final int mAuroraOpaque;
        // Aurora <Felix.Duan> <2014-6-18> <END> Support NaviBar full mode change from intent
        private final Drawable mGradient;
        private final TimeInterpolator mInterpolator;

        private int mMode = -1;
        private boolean mAnimating;
        private long mStartTime;
        private long mEndTime;

        private int mGradientAlpha;
        private int mColor;

        private int mGradientAlphaStart;
        private int mColorStart;

        public BarBackgroundDrawable(Context context, int gradientResourceId) {
            final Resources res = context.getResources();
            if (DEBUG_COLORS) {
                mOpaque = 0xff0000ff;
                mSemiTransparent = 0x7f0000ff;
                // Aurora <Felix.Duan> <2014-6-10> <BEGIN> Support full transparent status bar
                mTransparent = 0xaaaa0055;
                // Aurora <Felix.Duan> <2014-6-10> <END> Support full transparent status bar
                // Aurora <Felix.Duan> <2014-6-18> <BEGIN> Support NaviBar full mode change from intent
                mAuroraOpaque = mOpaque;
                // Aurora <Felix.Duan> <2014-6-18> <END> Support NaviBar full mode change from intent
            } else {
                // felix touched
                //mOpaque = res.getColor(R.color.system_bar_background_opaque);
                mOpaque = 0xff000000;
                //mSemiTransparent = res.getColor(R.color.system_bar_background_semi_transparent);
                mSemiTransparent = 0x66000000;
                // Aurora <Felix.Duan> <2014-6-10> <BEGIN> Support full transparent status bar
                mTransparent = 0x00000000;
                // Aurora <Felix.Duan> <2014-6-10> <END> Support full transparent status bar
                // Aurora <Felix.Duan> <2014-6-18> <BEGIN> Support NaviBar full mode change from intent
                mAuroraOpaque = mOpaque;
                // Aurora <Felix.Duan> <2014-6-18> <END> Support NaviBar full mode change from intent
            }
            mGradient = res.getDrawable(gradientResourceId);
            mInterpolator = new LinearInterpolator();
        }

        @Override
        public void setAlpha(int alpha) {
            // noop
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            // noop
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);
            mGradient.setBounds(bounds);
        }

        public void applyModeBackground(int oldMode, int newMode, boolean animate) {
            if (mMode == newMode) return;
            mMode = newMode;
            mAnimating = animate;
            // Aurora <Felix.Duan> <2014-6-18> <BEGIN> Support NaviBar full mode change from intent
            if (mMode == MODE_INSTANT_OPAQUE)
                mAnimating = false;
            if (mAnimating) {
                long now = SystemClock.elapsedRealtime();
                mStartTime = now;
                mEndTime = now + BACKGROUND_DURATION;
                mGradientAlphaStart = mGradientAlpha;
                mColorStart = mColor;
            }
            // Aurora <Felix.Duan> <2014-6-18> <END> Support NaviBar full mode change from intent
            invalidateSelf();
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        public void finishAnimation() {
            if (mAnimating) {
                mAnimating = false;
                invalidateSelf();
            }
        }

        @Override
        public void draw(Canvas canvas) {
            int targetGradientAlpha = 0, targetColor = 0;
            // Aurora <Felix.Duan> <2014-6-10> <START> Support full transparent status bar
            if (mMode == MODE_TRANSLUCENT) {
                targetGradientAlpha = 0xff;
            } else if (mMode == MODE_SEMI_TRANSPARENT) {
                targetColor = mSemiTransparent;
            // Aurora <Felix.Duan> <2014-6-18> <BEGIN> Support NaviBar full mode change from intent
            } else if (mMode == MODE_INSTANT_OPAQUE) {
                targetColor = mAuroraOpaque;
            // Aurora <Felix.Duan> <2014-6-18> <END> Support NaviBar full mode change from intent
            } else if (mMode == MODE_TRANSPARENT) {
                targetColor = mTransparent;
            } else if (mMode == MODE_CUSTOM_COLOR) {
                // Aurora <Felix.Duan> <2014-7-30> <BEGIN> Feature : custom navi bar color
                // apply custom color
                targetGradientAlpha = 0;
                targetColor = auroraTargetColor;
                // Aurora <Felix.Duan> <2014-7-30> <END> Feature : custom navi bar color
            } else {
                targetColor = mOpaque;
            }
            // Aurora <Felix.Duan> <2014-6-10> <END> Support full transparent status bar
            if (!mAnimating) {
                mColor = targetColor;
                mGradientAlpha = targetGradientAlpha;
            } else {
                final long now = SystemClock.elapsedRealtime();
                if (now >= mEndTime) {
                    mAnimating = false;
                    mColor = targetColor;
                    mGradientAlpha = targetGradientAlpha;
                } else {
                    final float t = (now - mStartTime) / (float)(mEndTime - mStartTime);
                    final float v = Math.max(0, Math.min(mInterpolator.getInterpolation(t), 1));
                    mGradientAlpha = (int)(v * targetGradientAlpha + mGradientAlphaStart * (1 - v));
                    mColor = Color.argb(
                          (int)(v * Color.alpha(targetColor) + Color.alpha(mColorStart) * (1 - v)),
                          (int)(v * Color.red(targetColor) + Color.red(mColorStart) * (1 - v)),
                          (int)(v * Color.green(targetColor) + Color.green(mColorStart) * (1 - v)),
                          (int)(v * Color.blue(targetColor) + Color.blue(mColorStart) * (1 - v)));
                }
            }
            if (mGradientAlpha > 0) {
                mGradient.setAlpha(mGradientAlpha);
                mGradient.draw(canvas);
            }
            if (Color.alpha(mColor) > 0) {
                canvas.drawColor(mColor);
            }
            if (mAnimating) {
                invalidateSelf();  // keep going
            }
        }
    }
}
