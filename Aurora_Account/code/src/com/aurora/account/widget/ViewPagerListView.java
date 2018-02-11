package com.aurora.account.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import aurora.widget.AuroraListView;

public class ViewPagerListView extends AuroraListView {
	
	private float xDistance, yDistance, xLast, yLast;

	public ViewPagerListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ViewPagerListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ViewPagerListView(Context context) {
		super(context);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			xDistance = yDistance = 0f;
			xLast = ev.getX();
			yLast = ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			final float curX = ev.getX();
			final float curY = ev.getY();

			xDistance += Math.abs(curX - xLast);
			yDistance += Math.abs(curY - yLast);
			xLast = curX;
			yLast = curY;

			if (xDistance > yDistance) {
				return false;
			}
		}

		return super.onInterceptTouchEvent(ev);
	}
}
