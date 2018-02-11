/*
 * Copyright (C) 2012 The Android Open Source Project
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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Slog;
import android.view.MotionEvent;
import android.view.View;

import com.android.systemui.R;
import com.android.systemui.statusbar.GestureRecorder;

// Gionee <fengjianyi><2013-05-10> add for CR00800567 start
import android.util.Log;
import com.android.systemui.statusbar.util.ToolbarIconUtils;
// Gionee <fengjianyi><2013-05-10> add for CR00800567 end

// Aurora <zhanggp> <2013-11-08> added for systemui begin
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Bitmap;
import android.view.Surface;
import android.graphics.Matrix;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.graphics.Rect;
import android.widget.ImageView;
// Aurora <zhanggp> <2013-11-08> added for systemui end
import android.view.GnSurface;
//Aurora <tongyh> <2014-10-20> NullPointerException begin
import android.graphics.BitmapFactory;
//Aurora <tongyh> <2014-10-20> NullPointerException end

public class NotificationPanelView extends PanelView {

    Drawable mHandleBar;
    float mHandleBarHeight;
	// Aurora <zhanggp> <2013-10-08> added for systemui begin
	float mHandleBarWidth;
	// Aurora <zhanggp> <2013-11-08> added for systemui begin
	private Bitmap mPanelBg = null;
	DisplayMetrics mDisplayMetrics = new DisplayMetrics();
	Context mContext;
	Display mDisplay;
	ImageView mBackgroundImg;
	// Aurora <zhanggp> <2013-11-08> added for systemui end
	// Aurora <zhanggp> <2013-10-08> added for systemui end
    View mHandleView;
    int mFingers;
    PhoneStatusBar mStatusBar;
    boolean mOkToFlip;
    
    // Gionee <fengjianyi><2013-05-10> add for CR00800567 start
    private int mTouchAction = -1;
    private int mTouchEventCount = 0;
    private static final int MAX_TOUCH_EVENT_COUNT = 3;
    // Gionee <fengjianyi><2013-05-10> add for CR00800567 end

    public NotificationPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
		// Aurora <zhanggp> <2013-11-08> added for systemui begin
		mContext = context;
		// Aurora <zhanggp> <2013-11-08> added for systemui end
    }

    public void setStatusBar(PhoneStatusBar bar) {
        mStatusBar = bar;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        Resources resources = getContext().getResources();
        // Gionee <fengjianyi><2013-05-10> modify for CR00800567 start
        /*
        mHandleBar = resources.getDrawable(R.drawable.status_bar_close);
        mHandleBarHeight = resources.getDimension(R.dimen.close_handle_height);
        */
     // Aurora <zhanggp> <2013-11-08> added for systemui begin
        mDisplay = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();
        //Aurora <tongyh> <2013-11-18> delete background blur begin
        if (ToolbarIconUtils.GN_QUICK_SETTINGS_SUPPORT) {
            mHandleBar = resources.getDrawable(R.drawable.zzzzz_gn_status_bar_close);
            mHandleBarHeight = resources.getDimension(R.dimen.zzzzz_gn_close_handle_height);
        } else {
		// Aurora <zhanggp> <2013-10-08> modified for systemui begin
			mHandleBar = resources.getDrawable(R.drawable.aurora_gn_status_bar_close);
			mHandleBarHeight = resources.getDimension(R.dimen.aurora_close_handle_height);
//			mHandleBarWidth = resources.getDimension(R.dimen.aurora_close_handle_width);
			mHandleBarWidth = mDisplay.getWidth();
//			mHandleBarWidth = getWidth();
		/*
            mHandleBar = resources.getDrawable(R.drawable.status_bar_close);
            mHandleBarHeight = resources.getDimension(R.dimen.close_handle_height);
		*/
		// Aurora <zhanggp> <2013-10-08> modified for systemui end
        }
        // Gionee <fengjianyi><2013-05-10> modify for CR00800567 end
        mHandleView = findViewById(R.id.handle);

        setContentDescription(resources.getString(R.string.accessibility_desc_notification_shade));
		
		mBackgroundImg = (ImageView)findViewById(R.id.panel_bg);
        //Aurora <tongyh> <2013-11-18> delete background blur end
		// Aurora <zhanggp> <2013-11-08> added for systemui end
    }

    @Override
    public void fling(float vel, boolean always) {
        GestureRecorder gr = ((PhoneStatusBarView) mBar).mBar.getGestureRecorder();
        if (gr != null) {
            gr.tag(
                "fling " + ((vel > 0) ? "open" : "closed"),
                "notifications,v=" + vel);
        }
        super.fling(vel, always);
    }

    // We draw the handle ourselves so that it's always glued to the bottom of the window.
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            final int pl = getPaddingLeft();
            final int pr = getPaddingRight();
            // Gionee <fengjianyi><2013-05-27> modify for CR00800567 start
            //mHandleBar.setBounds(pl, 0, getWidth() - pr, (int) mHandleBarHeight);
            if (ToolbarIconUtils.GN_QUICK_SETTINGS_SUPPORT) {
                mHandleBar.setBounds(pl * 2, 0, getWidth() - pr * 2, (int) mHandleBarHeight);
            } else {
				// Aurora <zhanggp> <2013-10-08> modified for systemui begin
				int leftStart = ((getWidth()- (int)mHandleBarWidth)>>1);
                mHandleBar.setBounds(leftStart, 0, leftStart + (int)mHandleBarWidth, (int) mHandleBarHeight);
				//mHandleBar.setBounds(pl, 0, getWidth() - pr, (int) mHandleBarHeight);
				// Aurora <zhanggp> <2013-10-08> modified for systemui end
			}
            // Gionee <fengjianyi><2013-05-27> modify for CR00800567 end
        }
    }
	// Aurora <zhanggp> <2013-11-08> added for systemui begin
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
		//Aurora <tongyh> <2014-10-20> NullPointerException begin
		if(null != mBackgroundImg && null != mPanelBg && !mPanelBg.isRecycled()){
		//Aurora <tongyh> <2014-10-20> NullPointerException end
            //Aurora <tongyh> <2014-12-03> Horizontal screen notification bar background error begin
//			mBackgroundImg.setImageBitmap(mPanelBg);
			BitmapDrawable bd = new BitmapDrawable(mPanelBg);
			mBackgroundImg.setBackground(bd);
            //Aurora <tongyh> <2014-12-03> Horizontal screen notification bar background error end
		}
	}
	// Aurora <zhanggp> <2013-11-08> added for systemui end
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
		// Aurora <zhanggp> <2013-10-08> modified for systemui begin
        final int off = (int) (getHeight() - mHandleBarHeight - getPaddingBottom()-
        getResources().getDimension(R.dimen.aurora_close_handle_margin_bottom));
        // Gionee <fengjianyi><2013-05-27> modify for CR00800567 start
        //final int off = (int) (getHeight() - mHandleBarHeight - getPaddingBottom());
        /*
        int gnOffset = (int) (ToolbarIconUtils.GN_QUICK_SETTINGS_SUPPORT ?
				getResources().getDimension(R.dimen.zzzzz_gn_close_handle_margin_bottom) : 0);
        final int off = (int) (getHeight() - mHandleBarHeight - getPaddingBottom() - gnOffset);
		*/
        // Gionee <fengjianyi><2013-05-27> modify for CR00800567 end
		// Aurora <zhanggp> <2013-10-08> modified for systemui end

		
        canvas.translate(0, off);
        mHandleBar.setState(mHandleView.getDrawableState());
        mHandleBar.draw(canvas);
        canvas.translate(0, -off);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
	
		// Aurora <zhanggp> <2013-11-08> added for systemui begin
		if(MotionEvent.ACTION_DOWN == event.getActionMasked()) {
			//Aurora <tongyh> <2013-11-18> delete background blur begin
//			loadRecentBg();
			//Aurora <tongyh> <2013-11-18> delete background blur end
			mStatusBar.hideClearButton();
		}
		// Aurora <zhanggp> <2013-11-08> added for systemui end
		
        if (PhoneStatusBar.SETTINGS_DRAG_SHORTCUT && mStatusBar.mHasFlipSettings) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    mOkToFlip = getExpandedHeight() == 0;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    if (mOkToFlip) {
                        float miny = event.getY(0);
                        float maxy = miny;
                        for (int i=1; i<event.getPointerCount(); i++) {
                            final float y = event.getY(i);
                            if (y < miny) miny = y;
                            if (y > maxy) maxy = y;
                        }
                        if (maxy - miny < mHandleBarHeight) {
                            // Gionee <fengjianyi><2013-05-10> modify for CR00800567 start
                            /*
                            if (getMeasuredHeight() < mHandleBarHeight) {
                                mStatusBar.switchToSettings();
                            } else {
                                mStatusBar.flipToSettings();
                            }
                            */
                            if (ToolbarIconUtils.GN_QUICK_SETTINGS_SUPPORT) {
                            	mStatusBar.switchToSettings();
                            } else {
                                if (getMeasuredHeight() < mHandleBarHeight) {
                                    mStatusBar.switchToSettings();
                                } else {
                                    mStatusBar.flipToSettings();
                                }
                            }
                            // Gionee <fengjianyi><2013-05-10> modify for CR00800567 end
                            mOkToFlip = false;
                        }
                    }
                    break;
            }

            // Gionee <fengjianyi><2013-05-10> add for CR00800567 start
            if (ToolbarIconUtils.GN_QUICK_SETTINGS_SUPPORT) {
        	    int action = event.getActionMasked();
                if (mTouchAction != -1) {
            	    mTouchEventCount++;
            	    if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
                	    mTouchAction = action;
            	    }
                } else if (mTouchAction == -1 && action == MotionEvent.ACTION_DOWN) {
            	    mTouchEventCount++;
            	    mTouchAction = action;
                }
                
                if (mTouchAction == MotionEvent.ACTION_POINTER_DOWN) {
            	    mTouchAction = -1;
            	    mTouchEventCount = 0;
                } else if (mTouchEventCount == MAX_TOUCH_EVENT_COUNT) {
            	    if (mTouchAction == MotionEvent.ACTION_DOWN && mOkToFlip) {
            		    if (mStatusBar.hasNotificationData()) {
                		    mStatusBar.flipToNotifications();
            		    } else {
                            mStatusBar.switchToSettings();
            		    }
            	    }
            	    mTouchAction = -1;
            	    mTouchEventCount = 0;
                }
            }
            // Gionee <fengjianyi><2013-05-10> add for CR00800567 end
        }
        return mHandleView.dispatchTouchEvent(event);
    }
// Aurora <zhanggp> <2013-10-17> added for systemui begin
	@Override
	public void onTrackingStart() {
		super.onTrackingStart();
		
		mStatusBar.hideClearButton();
		

	}
	@Override
	public void onTrackingEnd() {
		super.onTrackingEnd();

	}
// Aurora <zhanggp> <2013-10-17> added for systemui end
}
