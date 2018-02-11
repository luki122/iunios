package com.android.phone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import com.android.internal.widget.multiwaveview.GlowPadView.OnTriggerListener;

public class SlideView extends View implements OnGestureListener{
    private GestureDetector gd; 
    private OnTriggerListener onlistener;
    Paint paint=new Paint();
    private int textsize,textsize2;
    private int textgap,textgap2;
    private int bottom_margin;
    private int hang_gap;
    private int left_margin;
    private int alpha1,alpha2;
	public SlideView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	public SlideView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		gd=new GestureDetector(this);
		textsize=context.getResources().getDimensionPixelSize(R.dimen.incoming_call_text_size);
		textsize2=context.getResources().getDimensionPixelSize(R.dimen.incoming_call_text_size2);
		textgap=context.getResources().getDimensionPixelSize(R.dimen.incoming_call_text_gap);
		textgap2=context.getResources().getDimensionPixelSize(R.dimen.incoming_call_text_gap2);
		bottom_margin=context.getResources().getDimensionPixelSize(R.dimen.incoming_call_jieting_bottom_margin);
		left_margin=context.getResources().getDimensionPixelSize(R.dimen.incoming_call_text_left_magin);
		hang_gap=context.getResources().getDimensionPixelSize(R.dimen.incoming_call_text_hang_gap);
		alpha1=255*80/100;
		alpha2=255*25/100;
	}
	public SlideView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		int width=this.getWidth();
		int height=this.getHeight();
		paint.setTextSize(textsize2);
		paint.setColor(Color.WHITE);
		int x1=left_margin;
		int y1=height-bottom_margin;
		paint.setAlpha(alpha2);
		canvas.drawText("向上滑动可拒接", x1, y1, paint);
		paint.setAlpha(alpha1);
		paint.setTextSize(textsize);
		int y2=y1-textsize2-hang_gap;
		int textwidth=(int)paint.measureText("滑动接听");
		int textwidth2=(int)paint.measureText(">");
		canvas.drawText("滑动接听", x1, y2, paint);
		canvas.drawText(">", x1+textwidth+textgap, y2, paint);
		paint.setAlpha(alpha2);
		canvas.drawText(">", x1+textwidth+textgap+textgap+textwidth2, y2, paint);		
		super.onDraw(canvas);
	}
	public void setOnTriggerListener(OnTriggerListener o)
	{
		onlistener=o;
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return gd.onTouchEvent(event);//super.onTouchEvent(event);
	}
	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		Log.v("SlideView", "onDown");
		return true;
	}
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		Log.v("SlideView", "onFling");
		float x1=e1.getX();
		float y1=e1.getY();
		float x2=e2.getX();
		float y2=e2.getY();
		if((x1-x2)<-200)
		{
			onlistener.onTrigger(null,0);
		}
		else if(y2-y1>200)
		{
			onlistener.onTrigger(null,1);
		}
		else if(y1-y2>200)
		{
			onlistener.onTrigger(null,2);
		}
		return false;
	}
	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		Log.v("SlideView", "onLongPress");
	}
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		Log.v("SlideView", "onScroll");
		return false;
	}
	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		Log.v("SlideView", "onShowPress");
	}
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		Log.v("SlideView", "onSingleTapUp");
		return false;
	}
	
}
