package com.android.browser;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class MainContentFrameLayout extends FrameLayout {
	GestureDetector ges;

	public MainContentFrameLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public MainContentFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public MainContentFrameLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	public void setGesture(GestureDetector gesture) {
		ges=gesture;
		
		
	}
  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
	// TODO Auto-generated method stub
	  ges.onTouchEvent(ev);

	return super.dispatchTouchEvent(ev);
  }
	
}
