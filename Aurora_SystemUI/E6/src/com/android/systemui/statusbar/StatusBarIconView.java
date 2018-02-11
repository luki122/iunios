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

package com.android.systemui.statusbar;

import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Slog;
import android.util.Log;
import android.view.ViewDebug;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;

import java.text.NumberFormat;

import com.android.internal.statusbar.StatusBarIcon;

import com.android.systemui.R;

public class StatusBarIconView extends AnimatedImageView {
    private static final String TAG = "StatusBarIconView";

    private StatusBarIcon mIcon;
    @ViewDebug.ExportedProperty private String mSlot;
    private Drawable mNumberBackground;
    private Paint mNumberPain;
    private int mNumberX;
    private int mNumberY;
    private String mNumberText;
    private Notification mNotification;
	// Aurora <zhanggp> <2013-10-08> added for systemui begin
	private boolean mIsNumIcon;
	public Paint mAuroraNumberPain;
	// Aurora <zhanggp> <2013-10-08> added for systemui end
    public StatusBarIconView(Context context, String slot, Notification notification) {
        super(context);
        final Resources res = context.getResources();
        /// M: [SystemUI] Support "Dual SIM". @{
		// Aurora <zhanggp> <2013-11-11> modified for systemui begin
        //mPadding = res.getDimensionPixelSize(R.dimen.status_bar_signal_bg_padding);
        //setPadding(mPadding, mPadding, mPadding, mPadding);
		// Aurora <zhanggp> <2013-11-11> modified for systemui end
        /// @}
        mSlot = slot;
        mNumberPain = new Paint();
		// Aurora <zhanggp> <2013-10-08> added for systemui begin
		mIsNumIcon = false;
		mAuroraNumberPain = new Paint();
        mAuroraNumberPain.setTextAlign(Paint.Align.CENTER);
        mAuroraNumberPain.setColor(res.getColor(R.drawable.notification_number_text_color));
        mAuroraNumberPain.setAntiAlias(true);
		mAuroraNumberPain.setTextSize(res.getDimensionPixelSize(R.dimen.aurora_status_bar_num_font_size));
		// Aurora <zhanggp> <2013-10-08> added for systemui end
        mNumberPain.setTextAlign(Paint.Align.CENTER);
        mNumberPain.setColor(res.getColor(R.drawable.notification_number_text_color));
        mNumberPain.setAntiAlias(true);
        mNotification = notification;
        setContentDescription(notification);

        // We do not resize and scale system icons (on the right), only notification icons (on the
        // left).
        // Aurora <zhanggp> <2013-11-05> modified for systemui begin
       
        if (notification != null) {
            //final int outerBounds = res.getDimensionPixelSize(R.dimen.status_bar_icon_size);
            //final int imageBounds = res.getDimensionPixelSize(R.dimen.status_bar_icon_drawing_size);
            final float scale = 1.0f;//(float)imageBounds / (float)outerBounds;
            setScaleX(scale);
            setScaleY(scale);
            //Aurora <tongyh> <2014-01-16> set StatusBarIconView alphe to 1.0f begin
//            final float alpha = res.getFraction(R.dimen.status_bar_icon_drawing_alpha, 1, 1);
            final float alpha =1.0f;
          //Aurora <tongyh> <2013-11-05> set StatusBarIconView alphe to 1.0f end
            setAlpha(alpha);
            
        }
        
		// Aurora <zhanggp> <2013-11-05> modified for systemui end
        setScaleType(ImageView.ScaleType.CENTER);
    }

    public StatusBarIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        final Resources res = context.getResources();
		// Aurora <zhanggp> <2013-11-11> modified for systemui begin
        /// M: [SystemUI] Support "Dual SIM". @{
        //mPadding = res.getDimensionPixelSize(R.dimen.status_bar_signal_bg_padding);
        //setPadding(mPadding, mPadding, mPadding, mPadding);
        /// @}
        //final int outerBounds = res.getDimensionPixelSize(R.dimen.status_bar_icon_size);
        //final int imageBounds = res.getDimensionPixelSize(R.dimen.status_bar_icon_drawing_size);
        final float scale = 1.0f;//(float)imageBounds / (float)outerBounds;
		// Aurora <zhanggp> <2013-11-11> modified for systemui end
        setScaleX(scale);
        setScaleY(scale);
        final float alpha = res.getFraction(R.dimen.status_bar_icon_drawing_alpha, 1, 1);
        setAlpha(alpha);
    }

    private static boolean streq(String a, String b) {
        if (a == b) {
            return true;
        }
        if (a == null && b != null) {
            return false;
        }
        if (a != null && b == null) {
            return false;
        }
        return a.equals(b);
    }

    /**
     * Returns whether the set succeeded.
     */
    public boolean set(StatusBarIcon icon) {
        final boolean iconEquals = mIcon != null
                && streq(mIcon.iconPackage, icon.iconPackage)
                && mIcon.iconId == icon.iconId;
        final boolean levelEquals = iconEquals
                && mIcon.iconLevel == icon.iconLevel;
        final boolean visibilityEquals = mIcon != null
                && mIcon.visible == icon.visible;
        final boolean numberEquals = mIcon != null
                && mIcon.number == icon.number;
        mIcon = icon.clone();
        setContentDescription(icon.contentDescription);
        if (!iconEquals) {
            Drawable drawable = getIcon(icon);
            if (drawable == null) {
                Slog.w(TAG, "No icon for slot " + mSlot);
                return false;
            }
            setImageDrawable(drawable);
        }
        if (!levelEquals) {
            setImageLevel(icon.iconLevel);
        }

        if (!numberEquals) {
            if (icon.number > 0 && mContext.getResources().getBoolean(
                        R.bool.config_statusBarShowNumber)) {
                if (mNumberBackground == null) {
                    mNumberBackground = getContext().getResources().getDrawable(
                            R.drawable.ic_notification_overlay);
                }
                placeNumber();
            } else {
                mNumberBackground = null;
                mNumberText = null;
            }
            invalidate();
        }
        if (!visibilityEquals) {
            setVisibility(icon.visible ? VISIBLE : GONE);
        }
        return true;
    }

    private Drawable getIcon(StatusBarIcon icon) {
        return getIcon(getContext(), icon);
    }

    /**
     * Returns the right icon to use for this item, respecting the iconId and
     * iconPackage (if set)
     * 
     * @param context Context to use to get resources if iconPackage is not set
     * @return Drawable for this item, or null if the package or item could not
     *         be found
     */
    public static Drawable getIcon(Context context, StatusBarIcon icon) {
        Resources r = null;

        if (icon.iconPackage != null) {
            try {
                int userId = icon.user.getIdentifier();
                if (userId == UserHandle.USER_ALL) {
                    userId = UserHandle.USER_OWNER;
                }
                r = context.getPackageManager()
                        .getResourcesForApplicationAsUser(icon.iconPackage, userId);
            } catch (PackageManager.NameNotFoundException ex) {
                Slog.e(TAG, "Icon package not found: " + icon.iconPackage);
                return null;
            }
        } else {
            r = context.getResources();
        }

        if (icon.iconId == 0) {
            return null;
        }
        
        try {
            return r.getDrawable(icon.iconId);
        } catch (RuntimeException e) {
            Slog.w(TAG, "Icon not found in "
                  + (icon.iconPackage != null ? icon.iconId : "<system>")
                  + ": " + Integer.toHexString(icon.iconId));
        }

        return null;
    }

    public StatusBarIcon getStatusBarIcon() {
        return mIcon;
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        if (mNotification != null) {
            event.setParcelableData(mNotification);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mNumberBackground != null) {
            placeNumber();
        }
		// Aurora <zhanggp> <2013-10-08> added for systemui begin
		else if(mIsNumIcon){
        	Aurora_placeNumber();
        }
		// Aurora <zhanggp> <2013-10-08> added for systemui end
    }

    @Override
    protected void onDraw(Canvas canvas) {
        /// M: [SystemUI] Support "Dual SIM". @{
        if (mSimInfoBackground != null) {
            mSimInfoBackground.setBounds(0, 0, getWidth(), getHeight());
            mSimInfoBackground.draw(canvas);
        }
        /// @}

        super.onDraw(canvas);

        if (mNumberBackground != null) {
            mNumberBackground.draw(canvas);
            canvas.drawText(mNumberText, mNumberX, mNumberY, mNumberPain);
        }
		// Aurora <zhanggp> <2013-10-08> added for systemui begin
		else if(mIsNumIcon){
			canvas.drawText(mNumberText, mNumberX, mNumberY, mAuroraNumberPain);
		}
		// Aurora <zhanggp> <2013-10-08> added for systemui end
    }

    @Override
    protected void debug(int depth) {
        super.debug(depth);
        Log.d("View", debugIndent(depth) + "slot=" + mSlot);
        Log.d("View", debugIndent(depth) + "icon=" + mIcon);
    }

    void placeNumber() {
        final String str;
        final int tooBig = mContext.getResources().getInteger(
                android.R.integer.status_bar_notification_info_maxnum);
        if (mIcon.number > tooBig) {
            str = mContext.getResources().getString(
                        android.R.string.status_bar_notification_info_overflow);
        } else {
            NumberFormat f = NumberFormat.getIntegerInstance();
            str = f.format(mIcon.number);
        }
        mNumberText = str;

        final int w = getWidth();
        final int h = getHeight();
        final Rect r = new Rect();
        mNumberPain.getTextBounds(str, 0, str.length(), r);
        final int tw = r.right - r.left;
        final int th = r.bottom - r.top;
        mNumberBackground.getPadding(r);
        int dw = r.left + tw + r.right;
        if (dw < mNumberBackground.getMinimumWidth()) {
            dw = mNumberBackground.getMinimumWidth();
        }
        mNumberX = w-r.right-((dw-r.right-r.left)/2);
        int dh = r.top + th + r.bottom;
        if (dh < mNumberBackground.getMinimumWidth()) {
            dh = mNumberBackground.getMinimumWidth();
        }
        mNumberY = h-r.bottom-((dh-r.top-th-r.bottom)/2);
        mNumberBackground.setBounds(w-dw, h-dh, w, h);
    }

    private void setContentDescription(Notification notification) {
        if (notification != null) {
            CharSequence tickerText = notification.tickerText;
            if (!TextUtils.isEmpty(tickerText)) {
                setContentDescription(tickerText);
            }
        }
    }

    public String toString() {
        return "StatusBarIconView(slot=" + mSlot + " icon=" + mIcon 
            + " notification=" + mNotification + ")";
    }

    /// M: [SystemUI] Support "Dual SIM". @{
    
    private Drawable mSimInfoBackground;
	// Aurora <zhanggp> <2013-11-11> modified for systemui begin
    //private int mPadding;
	// Aurora <zhanggp> <2013-11-11> modified for systemui end
    public Drawable getSimInfoBackground() {
        return mSimInfoBackground;
    }

    public void setSimInfoBackground(Drawable d) {
        this.mSimInfoBackground = d;
    }
    /*
    public long getNotificationSimId() {
        return mNotification.simId;
    }
    */
    

    /// @}
	

	// Aurora <zhanggp> <2013-10-08> added for systemui begin
    public boolean Aurora_set(StatusBarIcon icon) {
        final boolean iconEquals = mIcon != null
                && streq(mIcon.iconPackage, icon.iconPackage)
                && mIcon.iconId == icon.iconId;
        final boolean levelEquals = iconEquals
                && mIcon.iconLevel == icon.iconLevel;
        final boolean visibilityEquals = mIcon != null
                && mIcon.visible == icon.visible;
        final boolean numberEquals = mIcon != null
                && mIcon.number == icon.number;
        mIcon = icon.clone();
        setContentDescription(icon.contentDescription);
        if (!iconEquals) {
            Drawable drawable = getIcon(icon);
            if (drawable == null) {
                Slog.w(TAG, "No icon for slot " + mSlot);
                return false;
            }
            setImageDrawable(drawable);
        }
        if (!levelEquals) {
            setImageLevel(icon.iconLevel);
        }

        if (!numberEquals) {
            if (icon.number > 0) {
				mNumberBackground = null;
				mIsNumIcon = true;
                Aurora_placeNumber();
            } else {
                mNumberBackground = null;
                mNumberText = null;
            }
            invalidate();
        }
        if (!visibilityEquals) {
            setVisibility(icon.visible ? VISIBLE : GONE);
        }
        return true;
    }
	

    void Aurora_placeNumber() {
        final String str;
        final int TOOBIG = 99;
		NumberFormat f = NumberFormat.getIntegerInstance();
        if (mIcon.number > TOOBIG) {
			str = f.format(TOOBIG);
        } else {
            str = f.format(mIcon.number);
        }
        mNumberText = str;

        final int w = getWidth();
        final int h = getHeight();
        final Rect r = new Rect();
        mAuroraNumberPain.getTextBounds(str, 0, str.length(), r);
        //final int tw = r.right - r.left;
        final int th = r.bottom - r.top;


        mNumberX = (w>>1);
        mNumberY = (h + th)>>1;
    }
	
	// Aurora <zhanggp> <2013-10-08> added for systemui end
}
