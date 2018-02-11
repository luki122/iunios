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

package com.aurora.launcher;
import javax.security.auth.Subject;

import android.R.integer;
import android.annotation.SuppressLint;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aurora.launcher.R;

/**
 * The linear layout used strictly for the widget/wallpaper tab of the customization tray
 */
@SuppressLint("NewApi")
public class PagedViewWidget extends LinearLayout {
    static final String TAG = "PagedViewWidgetLayout";

    private static boolean sDeletePreviewsWhenDetachedFromWindow = true;

    private String mDimensionsFormatString;
    CheckForShortPress mPendingCheckForShortPress = null;
    ShortPressListener mShortPressListener = null;
    boolean mShortPressTriggered = false;
    static PagedViewWidget sShortpressTarget = null;
    boolean mIsAppWidget;
    private final Rect mOriginalImagePadding = new Rect();

    public PagedViewWidget(Context context) {
        this(context, null);
    }

    public PagedViewWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagedViewWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final Resources r = context.getResources();
        mDimensionsFormatString = r.getString(R.string.widget_dims_format);

        setWillNotDraw(false);
        setClipToPadding(false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
//AURORA-START::Add customized widgetpage::Shi guiqiang::20130916
        //final ImageView image = (ImageView) findViewById(R.id.widget_preview);
        final ImageView image = (ImageView) findViewById(R.id.widget_icon);
//AURORA-END::Add customized widgetpage::Shi guiqiang::20130916
        mOriginalImagePadding.left = image.getPaddingLeft();
        mOriginalImagePadding.top = image.getPaddingTop();
        mOriginalImagePadding.right = image.getPaddingRight();
        mOriginalImagePadding.bottom = image.getPaddingBottom();
    }

    public static void setDeletePreviewsWhenDetachedFromWindow(boolean value) {
        sDeletePreviewsWhenDetachedFromWindow = value;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
//AURORA-START::Add customized widgetpage::Shi guiqiang::20130916 
/*
        if (sDeletePreviewsWhenDetachedFromWindow) {
            //final ImageView image = (ImageView) findViewById(R.id.widget_preview);
            if (image != null) {
                FastBitmapDrawable preview = (FastBitmapDrawable) image.getDrawable();
                if (preview != null && preview.getBitmap() != null) {
                    preview.getBitmap().recycle();
                }
                image.setImageDrawable(null);
            }
        }
*/    
//AURORA-END::Add customized widgetpage::Shi guiqiang::20130916 
    }

    public void applyFromAppWidgetProviderInfo(AppWidgetProviderInfo info,
            int maxWidth, int[] cellSpan) {
        mIsAppWidget = true;
//AURORA-START::Add customized widgetpage::Shi guiqiang::20130916
        //final ImageView image = (ImageView) findViewById(R.id.widget_preview);
//        if (maxWidth > -1) {
//            image.setMaxWidth(maxWidth);
//        }
//        image.setContentDescription(info.label);
//AURORA-END::Add customized widgetpage::Shi guiqiang::20130916
        final TextView name = (TextView) findViewById(R.id.widget_name);
        
//AURORA-START::Customized widgetpage::Shi guiqiang::20131022
//        name.setText(info.label);
        String mString = info.label.toString();
        mString = trimStringForWidgetName(mString);
        name.setText(mString);
//AURORA-END::Customized widgetpage::Shi guiqiang::20131022
        
        final TextView dims = (TextView) findViewById(R.id.widget_dims);
        if (dims != null) {
            int hSpan = Math.min(cellSpan[0], LauncherModel.getCellCountX());
            int vSpan = Math.min(cellSpan[1], LauncherModel.getCellCountY());
            dims.setText(String.format(mDimensionsFormatString, hSpan, vSpan));
        }
    }

    public void applyFromResolveInfo(PackageManager pm, ResolveInfo info) {
        mIsAppWidget = false;
        CharSequence label = info.loadLabel(pm);
//AURORA-START::Add customized widgetpage::Shi guiqiang::20130916
        //final ImageView image = (ImageView) findViewById(R.id.widget_preview);
        final ImageView image = (ImageView) findViewById(R.id.widget_icon);
//AURORA-END::Add customized widgetpage::Shi guiqiang::20130916
        image.setContentDescription(label);
        final TextView name = (TextView) findViewById(R.id.widget_name);
        name.setText(label);
        final TextView dims = (TextView) findViewById(R.id.widget_dims);
        if (dims != null) {
            dims.setText(String.format(mDimensionsFormatString, 1, 1));
        }
    }

    public int[] getPreviewSize() {
//AURORA-START::Add customized widgetpage::Shi guiqiang::20130916
        //final ImageView i = (ImageView) findViewById(R.id.widget_preview);
    	final ImageView i = (ImageView) findViewById(R.id.widget_icon);
//AURORA-END::Add customized widgetpage::Shi guiqiang::20130916
        int[] maxSize = new int[2];
        maxSize[0] = i.getWidth() - mOriginalImagePadding.left - mOriginalImagePadding.right;
        maxSize[1] = i.getHeight() - mOriginalImagePadding.top;
        return maxSize;
    }

    void applyPreview(FastBitmapDrawable preview, int index) {
//AURORA-START::Add customized widgetpage::Shi guiqiang::20130916 
    /*	
        final PagedViewWidgetImageView image =
            (PagedViewWidgetImageView) findViewById(R.id.widget_preview);
        
        if (preview != null) {
            image.mAllowRequestLayout = false;
            image.setImageDrawable(preview);
            if (mIsAppWidget) {
                // center horizontally
                int[] imageSize = getPreviewSize();
                int centerAmount = (imageSize[0] - preview.getIntrinsicWidth()) / 2;
                image.setPadding(mOriginalImagePadding.left + centerAmount,
                        mOriginalImagePadding.top,
                        mOriginalImagePadding.right,
                        mOriginalImagePadding.bottom);
            }
            image.setAlpha(1f);
            image.mAllowRequestLayout = true;
        }
        */
//AURORA-END::Add customized widgetpage::Shi guiqiang::20130916 
    }
    
//AURORA-START::Add customized widgetpage::Shi guiqiang::20130916 
    void applyPreviewImage(Drawable preview, int index) {
    	final ImageView image = (ImageView) findViewById(R.id.widget_icon);
        if (preview != null) {
            image.setImageDrawable(preview);
            image.setAlpha(1f);
        }
    }
//AURORA-END::Add customized widgetpage::Shi guiqiang::20130916
    
    void setShortPressListener(ShortPressListener listener) {
        mShortPressListener = listener;
    }

    interface ShortPressListener {
        void onShortPress(View v);
        void cleanUpShortPress(View v);
    }

    class CheckForShortPress implements Runnable {
        public void run() {
            if (sShortpressTarget != null) return;
            if (mShortPressListener != null) {
                mShortPressListener.onShortPress(PagedViewWidget.this);
                sShortpressTarget = PagedViewWidget.this;
            }
            mShortPressTriggered = true;
        }
    }

    private void checkForShortPress() {
        if (sShortpressTarget != null) return;
        if (mPendingCheckForShortPress == null) {
            mPendingCheckForShortPress = new CheckForShortPress();
        }
        postDelayed(mPendingCheckForShortPress, 120);
    }

    /**
     * Remove the longpress detection timer.
     */
    private void removeShortPressCallback() {
        if (mPendingCheckForShortPress != null) {
          removeCallbacks(mPendingCheckForShortPress);
        }
    }

    private void cleanUpShortPress() {
        removeShortPressCallback();
        if (mShortPressTriggered) {
            if (mShortPressListener != null) {
                mShortPressListener.cleanUpShortPress(PagedViewWidget.this);
            }
            mShortPressTriggered = false;
        }
    }

    static void resetShortPressTarget() {
        sShortpressTarget = null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                cleanUpShortPress();
                break;
            case MotionEvent.ACTION_DOWN:
                checkForShortPress();
                break;
            case MotionEvent.ACTION_CANCEL:
                cleanUpShortPress();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
        }

        // We eat up the touch events here, since the PagedView (which uses the same swiping
        // touch code as Workspace previously) uses onInterceptTouchEvent() to determine when
        // the user is scrolling between pages.  This means that if the pages themselves don't
        // handle touch events, it gets forwarded up to PagedView itself, and it's own
        // onTouchEvent() handling will prevent further intercept touch events from being called
        // (it's the same view in that case).  This is not ideal, but to prevent more changes,
        // we just always mark the touch event as handled.
        return true;
    }
    
    //AURORA-START::Customized widgetpage::Shi guiqiang::20131022
    public String trimStringForWidgetName (String str) {
    	String finalString;
    	int index = str.indexOf(")");
        if (index != -1) {
        	if (index != str.length() -1) {
	        	str = str.substring(index+1);
        	}
        }
        index = str.indexOf("(");
        if (index != -1) {
        	if (index != 0) {
	        	str = str.substring(0, index);
        	}
        }
        //AURORA-START::Fix bug #3486::Shi guiqiang::20140324
    	index = str.indexOf("）");
        if (index != -1) {
        	if (index != str.length() -1) {
	        	str = str.substring(index+1);
        	}
        }
        index = str.indexOf("（");
        if (index != -1) {
        	if (index != 0) {
	        	str = str.substring(0, index);
        	}
        }
        //AURORA-END::Fix bug #3486::Shi guiqiang::20140324
        index = str.indexOf("x");
        if (index == -1) index = str.indexOf("X");
        if (index >0  && index < (str.length() -1)) {
        	if (Character.isDigit(str.charAt(index - 1)) && Character.isDigit(str.charAt(index + 1))) {
	        	if (index == str.length() - 2) {
	        		str = str.substring(0, index-1);
	        	} else {
	        		if (index + 2 < str.length()) {
	        			str = str.substring(index+2);
	        		}
	        	}
        	}
        }
        //AURORA-START::Customized widgetpage::Shi guiqiang::20131025
        index = str.indexOf("*");
        if (index >0  && index < (str.length() -1)) {
        	if (Character.isDigit(str.charAt(index - 1)) && Character.isDigit(str.charAt(index + 1))) {
	        	if (index == str.length() - 2) {
	        		str = str.substring(0, index-1);
	        	} else {
	        		if (index + 2 < str.length()) {
	        			str = str.substring(index+2);
	        		}
	        	}
        	}
        }
        index = str.indexOf("_");
        if (index >0  && index < (str.length() -1)) {
        	if (Character.isDigit(str.charAt(index - 1)) && Character.isDigit(str.charAt(index + 1))) {
	        	if (index == str.length() - 2) {
	        		str = str.substring(0, index-1);
	        	} else {
	        		if (index + 2 < str.length()) {
	        			str = str.substring(index+2);
	        		}
	        	}
	        }
        }
        //AURORA-END::Customized widgetpage::Shi guiqiang::20131025
        finalString = str;
        return finalString;
    }
    //AURORA-END::Customized widgetpage::Shi guiqiang::20131022
}
