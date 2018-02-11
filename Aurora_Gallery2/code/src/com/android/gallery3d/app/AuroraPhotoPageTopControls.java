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

package com.android.gallery3d.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.android.gallery3d.R;
import com.android.gallery3d.util.MyLog;

import java.util.HashMap;
import java.util.Map;
import android.widget.TextView;
import android.graphics.Typeface;

import android.view.animation.TranslateAnimation;


public class AuroraPhotoPageTopControls implements OnClickListener {

	//Aurora <SQF> <2015-03-16>  for NEW_UI begin
	//private static String ACTION_BAR_TITLE_FONT = "/system/fonts/title.ttf";
	//private static String ACTION_BAR_TITLE_FONT = "/system/fonts/title.ttf";
	//private static final String ACTION_BAR_TITLE_FONT = "system/fonts/DroidSansFallback.ttf";
	//Aurora <SQF> <2015-03-16>  for NEW_UI end
	
    public interface Delegate {
        public boolean canDisplayTopControls();
        public boolean canDisplayTopControl(int control);
        public void onTopControlClicked(int control);
        //Aurora <SQF> <2015-03-31>  for NEW_UI begin
        //public boolean isShortVideo();
        //Aurora <SQF> <2015-03-31>  for NEW_UI begin
        //public void refreshTopControlsWhenReady();
    }

    private Delegate mDelegate;
    private ViewGroup mParentLayout;
    private ViewGroup mContainer;
	private TextView mTitleView;
	private final int mHeight;
    private boolean mContainerVisible = false;
    private Map<View, Boolean> mControlsVisible = new HashMap<View, Boolean>();

    private Animation mContainerAnimIn;//new AlphaAnimation(0f, 1f);
    private Animation mContainerAnimOut;//new AlphaAnimation(1f, 0f);
    private static final int CONTAINER_ANIM_DURATION_MS = 200;

    private static final int CONTROL_ANIM_DURATION_MS = 150;
    private Animation getControlAnimForVisibility(boolean visible,int h) {
        //Aurora <SQF> <2014-07-30>  for NEW_UI begin
        //ORIGINALLY:
    	//Animation anim = visible ? new TranslateAnimation(0,0,-h,0)
        //: new TranslateAnimation(0,0,0,-h);
        //SQF MODIFIED TO:
    	Animation anim = visible ? new AlphaAnimation(0.0f, 1.0f)
        : new AlphaAnimation(1.0f, 0.0f);
        //Aurora <SQF> <2014-07-30>  for NEW_UI end
        
        anim.setDuration(CONTROL_ANIM_DURATION_MS);
        return anim;
    }

    public AuroraPhotoPageTopControls(Delegate delegate, Context context, RelativeLayout layout) {
		
        mDelegate = delegate;
        mParentLayout = layout;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContainer = (ViewGroup) inflater
                .inflate(R.layout.aurora_photopage_top_controls, mParentLayout, false);
		mHeight = context.getResources().getDimensionPixelSize(R.dimen.aurora_bottom_controls_height);
		
	    //Aurora <SQF> <2014-07-30>  for NEW_UI begin
	    //ORIGINALLY:
		//mContainerAnimIn = new TranslateAnimation(0,0,-mHeight,0);
		//mContainerAnimOut = new TranslateAnimation(0,0,0,-mHeight);
	    //SQF MODIFIED TO:
		mContainerAnimIn = new AlphaAnimation(0.0f, 1.0f);
		mContainerAnimOut = new AlphaAnimation(1.0f, 0.0f);
	    //Aurora <SQF> <2014-07-30>  for NEW_UI end

        mParentLayout.addView(mContainer);
		
		mTitleView = (TextView)mContainer.findViewById(R.id.leftTextView);
		/*
		//Typeface titleFace = Typeface.createFromFile(ACTION_BAR_TITLE_FONT);
		//mTitleView.setTypeface(titleFace);
		*/
        for (int i = mContainer.getChildCount() - 1; i >= 0; i--) {
            View child = mContainer.getChildAt(i);
            child.setOnClickListener(this);
            mControlsVisible.put(child, false);
        }

        mContainerAnimIn.setDuration(CONTAINER_ANIM_DURATION_MS);
        mContainerAnimOut.setDuration(CONTAINER_ANIM_DURATION_MS);

        //mDelegate.refreshTopControlsWhenReady();
    }
    
    public void hideDirectly() {
    	mContainer.clearAnimation();
    	mContainerAnimOut.reset();
    	mContainer.setVisibility(View.INVISIBLE);
        mContainerVisible = false;
    }

    private void hide() {
        mContainer.clearAnimation();
        mContainerAnimOut.reset();
        mContainer.startAnimation(mContainerAnimOut);
        mContainer.setVisibility(View.INVISIBLE);
    }

    private void show() {
        mContainer.clearAnimation();
        mContainerAnimIn.reset();
        mContainer.startAnimation(mContainerAnimIn);
        mContainer.setVisibility(View.VISIBLE);
    }

    public void refresh() {
        boolean visible = mDelegate.canDisplayTopControls();
        boolean containerVisibilityChanged = (visible != mContainerVisible);
        if (containerVisibilityChanged) {
            if (visible) {
                show();
            } else {
                hide();
            }
            mContainerVisible = visible;
        }
        if (!mContainerVisible) {
            return;
        }
        for (View control : mControlsVisible.keySet()) {
            Boolean prevVisibility = mControlsVisible.get(control);
            boolean curVisibility = mDelegate.canDisplayTopControl(control.getId());
            if (prevVisibility.booleanValue() != curVisibility) {
                if (!containerVisibilityChanged) {
                    control.clearAnimation();
                    control.startAnimation(getControlAnimForVisibility(curVisibility,mHeight));
                }
                control.setVisibility(curVisibility ? View.VISIBLE : View.GONE);
                mControlsVisible.put(control, curVisibility);
            }
        }
        // Force a layout change
        mContainer.requestLayout(); // Kick framework to draw the control.
        
		/*
        //Aurora <SQF> <2015-03-31>  for NEW_UI begin
        ImageButton button = (ImageButton)mContainer.findViewById(R.id.rightButton);
        if(mDelegate.isShortVideo()) {
        	button.setImageResource(android.R.drawable.alert_dark_frame);//TODO:TO BE REPLACED
        	//Log.i("SQF_LOG", "isShortVideo=======================111");
        } else {
        	button.setImageResource(R.drawable.aurora_top_icon_slideshow);
        	//Log.i("SQF_LOG", "isShortVideo=======================222");
        }
        //Aurora <SQF> <2015-03-31>  for NEW_UI end
		*/
    }

    public void cleanup() {
        mParentLayout.removeView(mContainer);
        mControlsVisible.clear();
    }

    @Override
    public void onClick(View view) {
        //paul added for BUG #3444
        Boolean isVisible = mControlsVisible.get(view);
    	if(null == isVisible){
			return;
		}

        if (mContainerVisible && isVisible.booleanValue()) {
            mDelegate.onTopControlClicked(view.getId());
        }
    }

	public int getHeight(){
		return mContainer.getHeight();
	}

	public void setTitle(CharSequence title) {
		mTitleView.setText(title);
	}

}
