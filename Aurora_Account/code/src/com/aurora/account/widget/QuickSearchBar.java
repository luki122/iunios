package com.aurora.account.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.aurora.account.R;

public class QuickSearchBar extends View {

	// 26个字母
	public static final String[] CHARTS = { "#", "A", "B", "C", "D", "E", "F",
			"G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S",
			"T", "U", "V", "W", "X", "Y", "Z" };

	private OnTouchingLetterChangedListener onTouchingLetterChangedListener;
	private int choose = -1;
	private String needHighlightStr = null;
	private Paint paint = null;
	private boolean showBkg = false;
	private float singleHeight;
	private Typeface mFace;
	private boolean isDuringTouch = false;

	public QuickSearchBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	public QuickSearchBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public QuickSearchBar(Context context) {
		super(context);
		initView(context);
	}

	private void initView(Context context) {
		singleHeight = 0;
		mFace = Typeface.createFromFile("system/fonts/Roboto-Light.ttf");
		paint = new Paint();
		paint.setTypeface(mFace);
		paint.setAntiAlias(true);
		paint.setTextSize(context.getResources().getDimension(
				R.dimen.quick_search_bar_text_size));

		isDuringTouch = false;
	}

	private void calculateSingleHeight() {
		singleHeight = getHeight() / (CHARTS.length);
		// singleHeight = 19.3f*scale;
	}

	/**
	 * 重写这个方法
	 */
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (showBkg) {
			canvas.drawColor(Color.parseColor("#40000000"));
		}
		if (singleHeight <= 0) {
			calculateSingleHeight();
		}
		for (int i = 0; i < CHARTS.length; i++) {
			if (!isDuringTouch && CHARTS[i].equals(needHighlightStr)) {
				paint.setColor(Color.parseColor("#ff9102"));
			} else {
				paint.setColor(Color.BLACK);
			}
			float xPos = getWidth() / 2 - paint.measureText(CHARTS[i]) / 2;
			float yPos = singleHeight * i + singleHeight;
			canvas.drawText(CHARTS[i], xPos, yPos, paint);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		final float y = event.getY();
		final int oldChoose = choose;
		final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
		if (singleHeight <= 0) {
			calculateSingleHeight();
		}
		final int c = (int) (y / singleHeight);

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			isDuringTouch = true;
			if (oldChoose != c && listener != null) {
				if (c >= 0 && c < CHARTS.length) {
					listener.onTouchingLetterChanged(CHARTS[c], y);
					choose = c;
					invalidate();
				}
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (oldChoose != c && listener != null) {
				if (c >= 0 && c < CHARTS.length) {
					listener.onTouchingLetterChanged(CHARTS[c], y);
					choose = c;
					invalidate();
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			choose = -1;
			isDuringTouch = false;
			invalidate();
			break;
		}
		return true;
	}

	/**
	 * 当前显示是以哪个字母开头的
	 * 
	 * @param title
	 */
	public void setCurChooseTitle(String title) {
		if (title == null) {
			return;
		}

		if (!canFindThisChart(title)) {
			title = CHARTS[0];
		}

		if (!title.equals(needHighlightStr)) {
			needHighlightStr = title;
			postInvalidate();
		}
	}

	private boolean canFindThisChart(String title) {
		for (int i = 0; i < CHARTS.length; i++) {
			if (CHARTS[i].equals(title)) {
				return true;
			}
		}

		return false;
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

	public OnTouchingLetterChangedListener getOnTouchingLetterChangedListener() {
		return onTouchingLetterChangedListener;
	}

	/**
	 * 接口
	 * 
	 * @author coder
	 */
	public interface OnTouchingLetterChangedListener {
		public void onTouchingLetterChanged(String s, float positionOfY);
		public int getToPosition(String s);
	}

}
