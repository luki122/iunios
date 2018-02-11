package com.android.systemui.recent;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.util.Log;
import com.android.systemui.R;

import android.content.res.Configuration;

public class RecentsLayout extends RelativeLayout{
	private Scroller mScroller;
	private Context mContext;
	
	public RecentsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
		mContext = context;
        mScroller = new Scroller(context);
	}

	private boolean isOrientationPortrait(){
		int orientation = getScreenState();
		if (orientation == Configuration.ORIENTATION_PORTRAIT) return true;
		return false;
	}

	private int getScreenState(){
		return getResources().getConfiguration().orientation;
	}

	private void rotationChangeAnimation(){
		if(isOrientationPortrait()){
			startScroll(HandlerBar.mRecentsScrimHeight, 0, 0, 0, HandlerBar.AUTO_ENTER_ANIMATION_DURATION);
			startScroll(0, 0, 0, HandlerBar.mRecentsScrimHeight, HandlerBar.AUTO_ENTER_ANIMATION_DURATION);
		} else {
			startScroll(0, HandlerBar.mRecentsScrimHeight, 0, 0, HandlerBar.AUTO_ENTER_ANIMATION_DURATION);
			startScroll(0, 0, HandlerBar.mRecentsScrimHeight, 0, HandlerBar.AUTO_ENTER_ANIMATION_DURATION);
		}
	}	
    
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // Aurora <Felix.Duan> <2014-9-18> <BEGIN> Add animation of swipe up icon on recents panel 
        // Make it two times high of visible part for swipe up.
        View child = findViewById(R.id.recents_bg_protect);
		int left = 0;
		int top = 0;
		int right = 0;
		int bottom = 0;

		if(isOrientationPortrait()){
			left = 0;
			top = RecentsPanelView.phoneHeight - HandlerBar.mRecentsScrimHeight;
			right = RecentsPanelView.phoneWidth;
			bottom = RecentsPanelView.phoneHeight + HandlerBar.mRecentsScrimHeight;
		}else{
			left = 	RecentsPanelView.phoneWidth - HandlerBar.mRecentsScrimHeight;
			top = 0;
			right = RecentsPanelView.phoneWidth + HandlerBar.mRecentsScrimHeight;
			bottom = RecentsPanelView.phoneHeight;
		}
        child.layout(left, top, right, bottom);
        // Aurora <Felix.Duan> <2014-9-18> <END> Add animation of swipe up icon on recents panel 
    }

	// Aurora <Felix.Duan> <2015-1-29> <BEGIN> Support landscape recents panel pull out
    // Aurora <Felix.Duan> <2014-7-28> <BEGIN> Pull-up refactor: preload tasks & views
    /**
     * Start scroll or extend scroll if already scrolling.
     * Start speed is 0
     * 
     * @author Felix.Duan
     * @date 2014-7-28
     */
    public void scroll(int startZ, int z, int duration) {
        Log.d("felix","RecentsLayout scroll()  startZ = " + startZ + "  z = " + z);
		if (mScroller.isFinished())
            if(isOrientationPortrait())
            // Aurora <Felix.Duan> <2014-7-30> <BEGIN> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804
                startScroll(0, startZ, 0, 0, duration);
            // Aurora <Felix.Duan> <2014-7-30> <END> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804
            else
                startScroll(startZ, 0, 0, 0, duration);
        else {
            if(isOrientationPortrait())
                mScroller.setFinalY(-z);
            else
                mScroller.setFinalX(-z);
            mScroller.extendDuration(200);
        }
    }
    // Aurora <Felix.Duan> <2014-7-28> <END> Pull-up refactor: preload tasks & views
	// Aurora <Felix.Duan> <2015-1-29> <END> Support landscape recents panel pull out
    
    
    // Aurora <Felix.Duan> <2014-7-28> <BEGIN> Pull-up refactor: preload tasks & views
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        Log.d("felix","RecentsLayout startScroll()  startY = " + startY + "  dy = " + dy);
		if (!mScroller.isFinished()) mScroller.abortAnimation();
        mScroller.startScroll(startX, startY, dx, dy, duration);
		//invalidate();
        postInvalidateOnAnimation();
	}
    // Aurora <Felix.Duan> <2014-7-28> <END> Pull-up refactor: preload tasks & views

	@Override
	public void computeScroll() {

		// 先判断mScroller滚动是否完成
		if (mScroller.computeScrollOffset()) {

			// 这里调用View的scrollTo()完成实际的滚动
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			// 必须调用该方法，否则不一定能看到滚动效果
            // Aurora <Felix.Duan> <2014-7-28> <BEGIN> Pull-up refactor: preload tasks & views
			//postInvalidate();
			postInvalidateOnAnimation();
            // Aurora <Felix.Duan> <2014-7-28> <END> Pull-up refactor: preload tasks & views
		}
	}
	
	public void closeScroll(){
		if (!mScroller.isFinished()) {
           mScroller.abortAnimation();
       }
	}
}
