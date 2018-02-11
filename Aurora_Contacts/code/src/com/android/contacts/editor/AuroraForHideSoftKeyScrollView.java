package com.android.contacts.editor;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;
import android.view.View;

public class AuroraForHideSoftKeyScrollView extends ScrollView implements View.OnTouchListener{



	public AuroraForHideSoftKeyScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.setOnTouchListener(this);
	}
	public AuroraForHideSoftKeyScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		// TODO Auto-generated method stub
		
		super.onScrollChanged(l, t, oldl, oldt);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub

		return super.onTouchEvent(ev);
	}
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		if(v==this)
		{
		InputMethodManager inputMethodManager = (InputMethodManager) this.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
		if(inputMethodManager.isActive())
		{
			
         inputMethodManager.hideSoftInputFromWindow(
                 getWindowToken(), 0);
        // return;
		}
		}
		return false;
	}	
	
}
