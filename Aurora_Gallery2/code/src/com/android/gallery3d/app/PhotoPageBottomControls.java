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

import com.android.gallery3d.common.ApiHelper;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.RelativeLayout;

import com.android.gallery3d.R;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.util.GalleryUtils;

import java.util.HashMap;
import java.util.Map;
// Aurora <paul> <2013-12-26> added for gallery begin
import android.view.animation.TranslateAnimation;
// Aurora <paul> <2013-12-26> added for gallery end
public class PhotoPageBottomControls implements OnClickListener {
    public interface Delegate {
        public boolean canDisplayBottomControls();
        public boolean canDisplayBottomControl(int control);
        public void onBottomControlClicked(int control);
        public void refreshBottomControlsWhenReady();
        public boolean showEdit();
    }

    private Delegate mDelegate;
    private ViewGroup mParentLayout;
    private ViewGroup mContainer;
	// Aurora <zhanggp> <2013-12-21> added for gallery begin
	private ViewGroup mImages;
	private final int mHeight;
	// Aurora <zhanggp> <2013-12-21> added for gallery end
    private boolean mContainerVisible = false;
    private Map<View, Boolean> mControlsVisible = new HashMap<View, Boolean>();
	
	// Aurora <paul> <2013-12-06> modified for gallery begin
    private Animation mContainerAnimIn;//new AlphaAnimation(0f, 1f);
    private Animation mContainerAnimOut;//new AlphaAnimation(1f, 0f);
    private static final int CONTAINER_ANIM_DURATION_MS = 200;

    private static final int CONTROL_ANIM_DURATION_MS = 150;
    //Aurora <SQF> <2014-07-28>  for NEW_UI begin
    private Gallery mAcitivity;
    private int mNavigationBarHeight;
    //Aurora <SQF> <2014-07-28>  for NEW_UI end
    private static Animation getControlAnimForVisibility(boolean visible,int h) {
        //Aurora <SQF> <2014-07-28>  for NEW_UI begin
        //ORIGINALLY:
    	//Animation anim = visible ? new TranslateAnimation(0,0,h,0)
        //: new TranslateAnimation(0,0,0,h);
        //SQF MODIFIED TO:
    	 Animation anim = visible ? new AlphaAnimation(0.0f, 1.0f)
         : new AlphaAnimation(1.0f, 0.0f);
        //Aurora <SQF> <2014-07-28>  for NEW_UI end
        anim.setDuration(CONTROL_ANIM_DURATION_MS);
        return anim;
    }
	// Aurora <paul> <2013-12-06> modified for gallery end

    @TargetApi(ApiHelper.VERSION_CODES.KITKAT)
    private void adjustTopPosition(Context context) {
    	boolean isPortrait = GalleryUtils.isPortrait(context);
    	adjustTopPosition(context, isPortrait);
    }
    
    @TargetApi(ApiHelper.VERSION_CODES.KITKAT)
    public void adjustTopPosition(Context context, boolean isPortrait) {
    	//Build.VERSION.SDK_INT U2-> 18 , U3 -> 19
    	if(! GalleryUtils.isU3()) {
    		//Log.i("SQF_LOG", "will not adjustTopPosition : ..................");
    		return;
    	}
    	//Log.i("SQF_LOG", "adjustTopPosition : ..................");
    	// U3 needs to adjust PhotoPageBottomControls top position
		RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams)mContainer.getLayoutParams();
		if(isPortrait) {
			p.bottomMargin = mNavigationBarHeight;
		} else {
			p.bottomMargin = 0;
		}
		mContainer.setLayoutParams(p);
    }
    
    public PhotoPageBottomControls(Delegate delegate, Activity context, RelativeLayout layout) {
        mDelegate = delegate;
        mParentLayout = layout;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContainer = (ViewGroup) inflater
                .inflate(R.layout.photopage_bottom_controls, mParentLayout, false);
		// Aurora <zhanggp> <2013-12-21> added for gallery begin
		mImages = (ViewGroup)mContainer.findViewById(R.id.photopage_bottom_images);
		mHeight = context.getResources().getDimensionPixelSize(R.dimen.aurora_bottom_controls_height);
	    //Aurora <SQF> <2014-07-28>  for NEW_UI begin
		mAcitivity = (Gallery)context;
		mNavigationBarHeight = mAcitivity.getNavigationBarHeight();
		
		//set mContainer position
		if(GalleryUtils.needNavigationBarControl()) {
			adjustTopPosition(context);
		}
	    //ORIGINALLY:
		//mContainerAnimIn = new TranslateAnimation(0,0,mHeight,0);
		//mContainerAnimOut = new TranslateAnimation(0,0,0,mHeight);
	    //SQF MODIFIED TO:
		mContainerAnimIn = new AlphaAnimation(0.0f, 1.0f);
		mContainerAnimOut = new AlphaAnimation(1.0f, 0.0f);
	    //Aurora <SQF> <2014-07-28>  for NEW_UI end
		// Aurora <zhanggp> <2013-12-21> added for gallery end
        mParentLayout.addView(mContainer);

        for (int i = mContainer.getChildCount() - 1; i >= 0; i--) {
            View child = mContainer.getChildAt(i);
            child.setOnClickListener(this);
            mControlsVisible.put(child, false);
        }
		// Aurora <zhanggp> <2013-12-21> added for gallery begin
        for (int i = mImages.getChildCount() - 1; i >= 0; i--) {
            View child = mImages.getChildAt(i);
            child.setOnClickListener(this);
            mControlsVisible.put(child, false);
        }
		// Aurora <zhanggp> <2013-12-21> added for gallery end

        mContainerAnimIn.setDuration(CONTAINER_ANIM_DURATION_MS);
        mContainerAnimOut.setDuration(CONTAINER_ANIM_DURATION_MS);

        mDelegate.refreshBottomControlsWhenReady();
    }
    
    public void hideDirectly() {
    	mContainer.clearAnimation();
    	mContainerAnimOut.reset();
    	mContainer.setVisibility(View.INVISIBLE);
        mIsShowing = false;
        mContainerVisible = false;
    }

    private void hide() {
        mContainer.clearAnimation();
        mContainerAnimOut.reset();
        mContainer.startAnimation(mContainerAnimOut);
        mContainer.setVisibility(View.INVISIBLE);
        mIsShowing = false;
    }

    private void show() {
        mContainer.clearAnimation();
        mContainerAnimIn.reset();
        mContainer.startAnimation(mContainerAnimIn);
        mContainer.setVisibility(View.VISIBLE);
        mIsShowing = true;
    }
    
    private boolean mIsShowing = false;
    public boolean isShowing() {
    	return mIsShowing;
    }
	// Aurora <paul> <2013-12-26> added for gallery begin
	private boolean isAnimView(int id){
		return (R.id.photopage_bottom_images == id || R.id.photopage_bottom_control_bgView == id);
	}
	// Aurora <paul> <2013-12-26> added for gallery end
    public void refresh() {
    	final boolean showEdit = mDelegate.showEdit();
        boolean visible = showEdit && mDelegate.canDisplayBottomControls();
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
        
        for (final View control : mControlsVisible.keySet()) {
            final Boolean prevVisibility = mControlsVisible.get(control);
            final boolean curVisibility = showEdit && mDelegate.canDisplayBottomControl(control.getId());    
            if(showEdit) {
            	control.setVisibility(curVisibility ? View.VISIBLE : View.GONE);
            } 
            if (prevVisibility.booleanValue() != curVisibility) {
				// Aurora <zhanggp> <2013-12-06> modified for gallery begin
            	
                //Aurora <SQF> <2014-07-23>  for NEW_UI begin
                //ORIGINALLY:
                //if (!containerVisibilityChanged && isAnimView(control.getId())) {
                //    control.clearAnimation();
                //    control.startAnimation(getControlAnimForVisibility(curVisibility,mHeight));
                //}
                //control.setVisibility(curVisibility ? View.VISIBLE : View.GONE);
                //SQF MODIFIED TO:
            	if (!containerVisibilityChanged && isAnimView(control.getId())) {
                    control.clearAnimation();
                    //control.startAnimation(getControlAnimForVisibility(curVisibility,mHeight));
                    Animation animation = getControlAnimForVisibility(curVisibility,mHeight);
                    animation.setAnimationListener(new AnimationListener() {

						@Override
						public void onAnimationEnd(Animation arg0) {
							// TODO Auto-generated method stub
								for(final View v : mControlsVisible.keySet()) {
						            final boolean curVisibility = showEdit && mDelegate.canDisplayBottomControl(v.getId());
						            if(curVisibility == false) {
						            	v.setVisibility(View.GONE);
						            }
								}
						}

						@Override
						public void onAnimationRepeat(Animation arg0) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void onAnimationStart(Animation arg0) {
							// TODO Auto-generated method stub
							
						}
                    	
                    });
                    control.startAnimation(animation);
                }

                
                
                //Aurora <SQF> <2014-07-23>  for NEW_UI end
				
//                if (!containerVisibilityChanged) {
//                    control.clearAnimation();
//                    control.startAnimation(getControlAnimForVisibility(curVisibility));
//                }
//				control.setVisibility(curVisibility ? View.VISIBLE : View.INVISIBLE);
				
				// Aurora <zhanggp> <2013-12-06> modified for gallery end
                //mControlsVisible.put(control, curVisibility); //SQF annotated on 2014.7.23
            }
            
            mControlsVisible.put(control, curVisibility);
        }

        // Force a layout change
        mContainer.requestLayout(); // Kick framework to draw the control.
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
            mDelegate.onBottomControlClicked(view.getId());
        }
    }
}
