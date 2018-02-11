package com.aurora.worldtime;

import com.android.deskclock.R;
import com.aurora.worldtime.UsefulUtils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

public class LetterSideBar extends View {
	// 26个字母
		public static String[] b = { "#", "A", "B", "C", "D", "E", "F", "G", "H",
				"I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
				"V", "W", "X", "Y", "Z" };

	private OnTouchingLetterChangedListener onTouchingLetterChangedListener;	
	private int choose = -1;
	private Paint paint = new Paint();
	private boolean showBkg = false;
	private float scale = 1.0f;
	private final int DEF_TEXT_SIZE = 12;

	public LetterSideBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	public LetterSideBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public LetterSideBar(Context context) {
		super(context);
		initView(context);
	}
	
	private void initView(Context context){
//		this.context = context;
		DisplayMetrics displayMetrics = UsefulUtils.getDisplayMetrics(context);
		if(displayMetrics != null){
			scale = displayMetrics.densityDpi/160.0f;
		}
	}

	/**
	 * 重写这个方法
	 */
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (showBkg) {
			//canvas.drawColor(Color.parseColor("#40000000"));
			setBackgroundResource(R.drawable.letterslidebarback);
		} else {
			setBackground(null);
		}

		int height = getHeight();
		int width = getWidth();
		int singleHeight = height / b.length-getResources().getInteger(R.integer.worldtime_sidebar_text_height);
		for (int i = 0; i < b.length; i++) {
			paint.setColor(getResources().getColor(R.color.worldtimesearchlistnamecolor));
//			paint.setTypeface(Typeface.DEFAULT_BOLD);
//			paint.setAntiAlias(true);
			paint.setTextSize(DEF_TEXT_SIZE*scale);//20
			if (i == choose) {
				paint.setColor(Color.parseColor("#3399ff"));
				paint.setFakeBoldText(true);
			}
			float xPos = width / 2 - paint.measureText(b[i]) / 2;
			float yPos = singleHeight * i + singleHeight;
			canvas.drawText(b[i], xPos, yPos, paint);
			paint.reset();
		}
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		final float y = event.getY();
		final int oldChoose = choose;
		final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
		final int c = (int) (y / getHeight() * b.length);

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			showBkg = true;
			if (oldChoose != c && listener != null) {
				if (c >=0 && c < b.length) {
					listener.onTouchingLetterChanged(b[c],y);
					choose = c;
					invalidate();
				}
			}

			break;
		case MotionEvent.ACTION_MOVE:
			if (oldChoose != c && listener != null) {
				if (c >= 0 && c < b.length) {
					listener.onTouchingLetterChanged(b[c],y);
					choose = c;
					invalidate();
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			showBkg = false;
			choose = -1;
			invalidate();
			break;
		}
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}

	/**
	 * 向外公开的方法
	 * 
	 * @param onTouchingLetterChangedListener
	 */
	public void setOnTouchingLetterChangedListener(
			OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
		this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
	}

	/**
	 * 接口
	 * @author coder
	 */
	public interface OnTouchingLetterChangedListener {
		public void onTouchingLetterChanged(String s,float positionOfY);
	}

}
