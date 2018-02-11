package com.gionee.calendar.view;

import com.android.calendar.AllInOneActivity;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;
//Gionee <jiating> <2013-04-24> modify for CR00000000  begin 
public class MyFramelayout extends FrameLayout{

	
	private Context context;
	private float currentX;
	private float currentY;
	private float startX;
	private float startY;
	public MyFramelayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context=context;
		// TODO Auto-generated constructor stub
	}

	public MyFramelayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context=context;
		// TODO Auto-generated constructor stub
	}

	public MyFramelayout(Context context) {
		super(context);
		this.context=context;
		// TODO Auto-generated constructor stub
	}
    
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		AllInOneActivity allInOneActivity=(AllInOneActivity)context;
		Log.i("jiating","onInterceptTouchEvent...");
		currentX=ev.getX();
		currentY=ev.getY();
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
		    startX=currentX;
	        startY=currentY;
			Log.i("jiating","onInterceptTouchEvent...ACTION_DOWN");
			/*if(allInOneActivity.isMenuOut() && !allInOneActivity.isMenuing()  && allInOneActivity.getSlidingView().isShown()){
				if((ev.getX()<allInOneActivity.getmAllInOneImageView().getWidth()+AllInOneActivity.ANIMATION_NEED_MORE_DISTANCE) ){
				allInOneActivity.setMenuIn();
				return true;
				}
			}else if(allInOneActivity.isMenuing()){
				return true;
			}*/
			
			break;
		case MotionEvent.ACTION_MOVE:
			Log.i("jiating","onInterceptTouchEvent...ACTION_MOVE");
//			if(allInOneActivity.isMenuing()){
//				return true;
//			}
			
			/*if(allInOneActivity.isMenuing() || allInOneActivity.isMenuOut()){
				Log.i("jiating","GNSlidingView...ACTION_MOVE1111...");
				if(allInOneActivity.getSlidingView().isShown() &&  Math.abs((currentX-startX))>Math.abs((currentY-startY))  && (currentX-startX)>100  &&  ev.getY()>allInOneActivity.getSlidingContentSystemView().getBottom() ){
					allInOneActivity.setMenuIn();
					Log.i("jiating","GNSlidingView...ACTION_MOVE222222...");
					return true;
				}
				
			}*/
		default:
			break;
		}
		return false;
	}
	
	
	
}
//Gionee <jiating> <2013-04-24> modify for CR00000000  end