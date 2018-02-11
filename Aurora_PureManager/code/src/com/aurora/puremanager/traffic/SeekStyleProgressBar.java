package com.aurora.puremanager.traffic;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

public class SeekStyleProgressBar extends SeekBar {
	
	public SeekStyleProgressBar(Context context) {
		super(context);
	}
	
	public SeekStyleProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public SeekStyleProgressBar(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public SeekStyleProgressBar(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

}
