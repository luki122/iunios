/*
 * Copyright (C) 2015 IUNI
 */
package com.android.systemui.statusbar.phone;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.Log;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.HashSet;
import static com.android.systemui.statusbar.phone.BarTransitions.MODE_OPAQUE;
import static com.android.systemui.statusbar.phone.BarTransitions.MODE_SEMI_TRANSPARENT;
import static com.android.systemui.statusbar.phone.BarTransitions.MODE_TRANSLUCENT;
import static com.android.systemui.statusbar.phone.BarTransitions.MODE_LIGHTS_OUT;
import static com.android.systemui.statusbar.phone.BarTransitions.MODE_TRANSPARENT;
import static com.android.systemui.statusbar.phone.BarTransitions.MODE_CUSTOM_COLOR;
import android.animation.Animator;
//Aurora <tongyh> <2015-02-26> battery Percentage Fonts color begin
import com.android.systemui.R;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.content.Context;
//Aurora <tongyh> <2015-02-26> battery Percentage Fonts color end
import android.graphics.drawable.AnimationDrawable;//tymt.tao_20150430_bug13111&&13078
/**
 * Invert StatusBar based on Android ColorFilter mechanism
 * First collect target views.
 * Then, invert background, ImageView, TextView
 *
 * mInvert: TRUE,           white bg & black content
 * mInvert: FALSE(default), black bg & white content
 *
 * @author: Felix.Duan
 * @date: 2015-2-6
 */
public class ColorInverter {
    private static final String TAG = "StatusBar.ColorInverter";
    private ViewGroup mTarget;
    private PhoneStatusBar mBar;
    private Boolean mInvert = false;
    public Boolean getmInvert() {
		return mInvert;
	}

	private HashSet<ImageView> mImageSet = new HashSet<ImageView>();
    private HashSet<TextView> mTextSet = new HashSet<TextView>();
 // Aurora <tongyh> <2015-02-26> battery Percentage Fonts color begin
    private boolean mIsCharge;
 // Aurora <tongyh> <2015-02-26> battery Percentage Fonts color end

    public ColorInverter(ViewGroup target, PhoneStatusBar bar, boolean invert) {
        mTarget = target;
        mBar = bar;
        mInvert = invert;
     // Aurora <tongyh> <2015-02-26> battery Percentage Fonts color begin
        mBar.mContext.registerReceiver(mbatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
     // Aurora <tongyh> <2015-02-26> battery Percentage Fonts color end
    }

    public void invert(int systemUiVis) {
        Log.d(TAG, "invertColor");
        invert(!mInvert, systemUiVis);
    }

    public void invert(boolean invert, int systemUiVis) {
    	Log.d(TAG, "invert------------------------invert = " + invert);
        Log.d(TAG, "invertColor");
        mInvert = invert;
        clearCollect();
        collectView(mTarget);
        invertBackground(systemUiVis);
        invertText();
        invertImage();
    }

    // Collect target views
    private void collectView(ViewGroup parent) {
        int count = parent.getChildCount();
        if (count == 0) return;
        View child;
        for (int i =0; i<count; i++) {
            child = parent.getChildAt(i);
            if (child instanceof TextView) {
                //Log.d(TAG ,"collectView TextView");
                mTextSet.add((TextView)child);
            } else if (child instanceof ImageView) {
                //Log.d(TAG ,"collectView ImageView");
// Aurora <tongyh> <2015-03-02> ticker icon invert begin
//              mImageSet.add((ImageView)child);
            	View mParent = (View)child.getParent();
            	String mTag = (String)mParent.getTag();
            	if(mParent.getId() == R.id.tickerIcon){
            		child.setTag(mTag);
            	}
            	mImageSet.add((ImageView)child);
// Aurora <tongyh> <2015-03-02> ticker icon invert end
            }else if (child instanceof ViewGroup) {
                //Log.d(TAG ,"collectView ViewGroup");
            	collectView((ViewGroup)child);
            } else {
                //Log.d(TAG ,"collectView else");
            }
        }
    }

    private void clearCollect() {
        mImageSet.clear();
        mTextSet.clear();
    }

    private void invertBackground(int vis) {
        Log.d(TAG, "invert bg vis = " + vis);
        if (mInvert) mBar.setTransparentSBar();
        else mBar.unSetTransparentSBar();
    }

    private void invertText() {
        Log.d(TAG, "invert text");
        int color = (mInvert) ? Color.BLACK : Color.WHITE;
        for (TextView view : mTextSet) {
        	// Aurora <tongyh> <2015-02-26> battery Percentage Fonts color begin
        	if((view.getId() == R.id.percentage) && mIsCharge){
        	}else{
        	// Aurora <tongyh> <2015-02-26> battery Percentage Fonts color end
        	view.setTextColor(color);
        	}
        }
    }

    private void invertImage() {
    	Log.d(TAG, "invertImage------------------------");
        if (mInvert)
            mBar.mColorFilter = new PorterDuffColorFilter(0xff000000, PorterDuff.Mode.MULTIPLY);
        else
            mBar.mColorFilter = null;
        Log.d(TAG, "invert image " + mBar.mColorFilter);
        for (ImageView v : mImageSet) {
// Aurora <tongyh> <2015-03-02> ticker icon invert begin
//          invert(v);
        	String mTag = (String)v.getTag();
        	Log.d(TAG, "mTag = " + mTag);
        	if(v.getId() == R.id.tickericonone || v.getId() == R.id.tickericontwo){
            	if(mTag != null && ("android".equals(mTag) || "com.android.providers.downloads".equals(mTag) || "com.android.systemui".equals(mTag) || "com.android.mms".equals(mTag))){
            		invert(v);
            	}
        	}else{
                invert(v);
        	}
// Aurora <tongyh> <2015-03-02> ticker icon invert end
        }
    }

    private void invert(ImageView imageView) {
        Log.d(TAG, "invert imageview " + mInvert);
        if((imageView.getDrawable()) instanceof AnimationDrawable){//tymt.tao_20150430_bug13111&&13078
        	int size=((AnimationDrawable)imageView.getDrawable()).getNumberOfFrames();
        	for(int i=0;i<size;i++){
        		((AnimationDrawable)imageView.getDrawable()).getFrame(i).setColorFilter(mBar.mColorFilter);
			}        	       	        	
        }else{
            Drawable background = imageView.getBackground();
            imageView.setColorFilter(mBar.mColorFilter);
            if (background != null) background.setColorFilter(mBar.mColorFilter);
        }        
        
    }

 // Aurora <tongyh> <2015-02-26> battery Percentage Fonts color begin
    private BroadcastReceiver mbatteryReceiver = new BroadcastReceiver(){
        public void onReceive(Context context, Intent intent){
        	String action =intent.getAction();
        	if(Intent.ACTION_BATTERY_CHANGED.equals(action)){
        		int status=intent.getIntExtra("status",BatteryManager.BATTERY_STATUS_UNKNOWN);
        		if(status==BatteryManager.BATTERY_STATUS_CHARGING){
        			mIsCharge = true;
        		}else{
        			mIsCharge = false;
        		}
        	}
        }
    };
 // Aurora <tongyh> <2015-02-26> battery Percentage Fonts color end
}
