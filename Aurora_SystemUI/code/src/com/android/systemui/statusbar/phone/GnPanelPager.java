/**
	File Description:
		PagerView contains notificaitonPanel and QuickSettings.
	Author: fengjy@gionee.com
	Create Date: 2013/04/24
	Change List:
*/



package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.support.v4.view.ViewPager;

import com.android.systemui.R;

public class GnPanelPager extends ViewPager {

    private int mSelectedPanelIndex = -1;

    public GnPanelPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setChildrenDrawingOrderEnabled(true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setChildrenDrawingOrderEnabled(true);
    }

    public int getPanelIndex(PanelView pv) {
        final int N = getChildCount();
        for (int i=0; i<N; i++) {
            final PanelView v = (PanelView) getChildAt(i);
            if (pv == v) return i;
        }
        return -1;
    }

    public void setSelectedPanel(PanelView pv) {
        mSelectedPanelIndex = getPanelIndex(pv);
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        if (mSelectedPanelIndex == -1) {
            return i;
        } else {
            if (i == childCount - 1) {
                return mSelectedPanelIndex;
            } else if (i >= mSelectedPanelIndex) {
                return i + 1;
            } else {
                return i;
            }
        }
    }

	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {
		if (getCurrentItem() == 0) {
			View child = findViewById(R.id.scroll);
			int contentHeight = (int) (child.getHeight() +
					child.getY() + child.getPaddingTop() + child.getPaddingBottom());
			if (arg0.getY() <= contentHeight) {
				return false;
			}
		}
		return super.onInterceptTouchEvent(arg0);
	}
}
