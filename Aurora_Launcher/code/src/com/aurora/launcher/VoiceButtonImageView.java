package com.aurora.launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.Scroller;

public class VoiceButtonImageView extends ImageView {
	
	private Scroller mScroller;
	
	public VoiceButtonImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public VoiceButtonImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mScroller = new Scroller(getContext(),new ScrollInterpolator());
	}
	
	public Scroller getScroller() {
		return mScroller;
	}
	
	@Override
    public void scrollTo(int x, int y) {
		setTranslationX(x);
    }
	
	@Override
    public void computeScroll() {
        computeScrollHelper();
    }
	
    protected boolean computeScrollHelper() {
        if (mScroller.computeScrollOffset()) {
//        	Log.d("DEBUG", "the getScrollX is "+getScrollX()+" the getCurrX is "+mScroller.getCurrX());
            if (getScrollX() != mScroller.getCurrX() || getTranslationX() != 0) {	//make sure TranslationX = 0
            	scrollTo(mScroller.getCurrX(), 0);
            }
            invalidate();
            return true;
        }
//        Log.d("DEBUG", "the TranslationX = "+getTranslationX());
        return false;
    }

    private static class ScrollInterpolator implements Interpolator {
        public ScrollInterpolator() {
        }

        public float getInterpolation(float t) {
            t -= 1.0f;
            return t*t*t*t*t + 1 ;
        }
    }
    
}
