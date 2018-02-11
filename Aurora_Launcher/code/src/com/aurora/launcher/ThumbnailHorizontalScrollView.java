package com.aurora.launcher;

import com.aurora.stickylistheaders.ContextWrapperEdgeEffect;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;
import android.widget.Scroller;

public class ThumbnailHorizontalScrollView extends HorizontalScrollView {
	
	private int leftpage = 0;
	private Scroller mScroller;

	public ThumbnailHorizontalScrollView(Context context) {
		super(new ContextWrapperEdgeEffect(context));
	}

	public ThumbnailHorizontalScrollView(Context context, AttributeSet attrs) {
		super(new ContextWrapperEdgeEffect(context), attrs);
		mScroller = new Scroller(getContext());
	}
	
	public Scroller getScroller() {
		return mScroller;
	}
	
//	public void scrollToDestination(int startX, int startY, int dx, int dy, int duration) {
//		mScroller.startScroll(startX, startY, dx, dy, duration);
//	}

	public int getLeftpage() {
		return leftpage;
	}

	public void setLeftpage(int leftpage) {
		this.leftpage = leftpage;
	}
	
	@Override
    public void computeScroll() {
        computeScrollHelper();
    }
	
    protected boolean computeScrollHelper() {
        if (mScroller.computeScrollOffset()) {
            if (getScrollX() != mScroller.getCurrX()) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            }
            invalidate();
            return true;
        }
        return false;
    }

}
