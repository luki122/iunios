package com.aurora.iunivoice.widget.app;

import java.io.CharConversionException;

import com.aurora.iunivoice.widget.AuroraButton;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class AuroraDialogButton extends AuroraButton {

	
//
	private CharSequence text;
	public AuroraDialogButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public AuroraDialogButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public AuroraDialogButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	@Override
		public boolean onTouchEvent(MotionEvent event) {
			// TODO Auto-generated method stub
//			switch (event.getAction()) {
//			case MotionEvent.ACTION_DOWN:
//				this.setTextColor(Color.WHITE);
//				break;
//            case MotionEvent.ACTION_UP:
//            	this.setTextColor(Color.BLACK);
//				break;
//
//			default:
//				break;
//			}
			return super.onTouchEvent(event);
		}
	
	
	

}
