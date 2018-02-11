/**
	File Description:
		Special radio button which rotate the text on the button.
	Author: fengjy@gionee.com
	Create Date: 2013/04/24
	Change List:
*/



package com.android.systemui.statusbar.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RadioButton;

import com.android.systemui.R;

public class GnRotateRadioButton extends RadioButton {

	private int mRotate;

	public GnRotateRadioButton(Context context) {
		super(context);
		mRotate = -90;
	}
	
	public GnRotateRadioButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		mRotate = -90;
	}
	
	public GnRotateRadioButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mRotate = -90;
	}

	@Override
	protected void onDraw(Canvas canvas) {
        canvas.rotate(mRotate);
        super.onDraw(canvas);
		Paint paint = getPaint();
		paint.setTextAlign(Paint.Align.CENTER);
		float textPositionX = 0 - (getHeight() - getPaddingTop() - getPaddingBottom()) * 0.5f;
		float textPositionY = (getTextSize()  + getWidth() - getPaddingLeft() - getPaddingRight()) * 0.5f;
		canvas.drawText(getText().toString(), textPositionX, textPositionY, paint);
	}
	
}

