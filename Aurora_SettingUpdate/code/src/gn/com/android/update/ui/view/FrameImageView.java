package gn.com.android.update.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import gn.com.android.update.ui.anim.AnimationUtils;
import gn.com.android.update.ui.anim.OTAFrameAnimation;
import gn.com.android.update.ui.anim.OTAFrameAnimation.AnimationImageListener;

import java.util.ArrayList;

public class FrameImageView extends ImageView {

    
    private OTAFrameAnimation mFrameAnimation;
    private Context mContext;
    private ArrayList<Integer> mImageId;
    private ArrayList<Integer> mDurations;
    private AnimationUtils utils;
    private int animationXml;
    public FrameImageView(Context context) {
        super(context);
        init(context);
    }

    public FrameImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public FrameImageView(Context context, AttributeSet attrs) {
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
        Log.e("luofu", "startAnim");
        mFrameAnimation.initRes(mImageId, mDurations);
        mFrameAnimation.start();
    }

    public void stopAnim(){
        mFrameAnimation.stop();
    }
    
}
