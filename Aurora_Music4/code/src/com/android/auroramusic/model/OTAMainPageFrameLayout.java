package com.android.auroramusic.model;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.ArrayList;

import com.android.auroramusic.model.OTAFrameAnimation.AnimationImageListener;



public class OTAMainPageFrameLayout extends ImageButton {

    private OTAFrameAnimation mFrameAnimation;
    private Context mContext;
    private ArrayList<Integer> mImageId;
    private ArrayList<Integer> mDurations;
    private AnimationUtils utils;
    private int animationXml;
    
    public OTAMainPageFrameLayout(Context context) {
        super(context);
        init(context);
    }

    public OTAMainPageFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public OTAMainPageFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mFrameAnimation = new OTAFrameAnimation(context,this);
        utils = new AnimationUtils();
    }
    
    public void setAnimationListener(AnimationImageListener mAnimationListener){
        mFrameAnimation.setAnimationImageListener(mAnimationListener);
    }
    
    public void setFrameAnimationList(int resId){
        this.animationXml = resId;
        utils.parseFrame(mContext, animationXml);
        mImageId =utils.getImages();
        mDurations = utils.getDuration();
    }
    
    public void startAnim() {
        mFrameAnimation.initRes(mImageId, mDurations);
        mFrameAnimation.start();
    }

    public void stopAnim(){
        mFrameAnimation.stop();
    }
}
