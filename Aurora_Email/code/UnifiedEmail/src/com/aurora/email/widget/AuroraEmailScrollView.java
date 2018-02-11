package com.aurora.email.widget;



import com.android.mail.utils.MyLog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class AuroraEmailScrollView extends ScrollView {

	private static final String TAG = "AuroraEmailScrollView";
	private boolean issmool = false; // 上滑模式
	private int oldHeight = 0;
	private int changeHeigth = 0;
	private OnInputMethodChangeListener mOnInputMethodChangeListener;
	private onScrollChangedListener monScrollChangedListener;

	private boolean isShowInput = false;
	private boolean isRellyLength = false;

	public AuroraEmailScrollView(Context context) {
		super(context);
	}

	public AuroraEmailScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AuroraEmailScrollView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		if (!isRellyLength && !isShowInput && getScrollY() == 0) {
			return false;
		}
		return super.onTouchEvent(ev);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
	//	MyLog.d(TAG, "onLayout:" + getHeight());
		super.onLayout(changed, l, t, r, b);
		if (mOnInputMethodChangeListener == null) {
			return;
		}
		if (changeHeigth == 0) {
			// 第一次进入layout
			changeHeigth = getMeasuredHeight();
		} else if (getMeasuredHeight() > changeHeigth) {
			// 关闭弹出输入法
			isShowInput = false;
			mOnInputMethodChangeListener
					.OnInputMethodChange(OnInputMethodChangeListener.STATE_PUT_INT);
		} else if (getMeasuredHeight() < changeHeigth) {
			// 弹出输入法
			isShowInput = true;
			mOnInputMethodChangeListener
					.OnInputMethodChange(OnInputMethodChangeListener.STATE_PUT_OUT);
		}
		changeHeigth = getMeasuredHeight();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	/*	MyLog.d(TAG, "onMeasure:" + getHeight() + " mesureh:"
				+ getMeasuredHeight());*/
		if (oldHeight == 0) {
			oldHeight = getMeasuredHeight();
		}
		View view = getChildAt(0);
		if (view instanceof LinearLayout) {
			LinearLayout chiLayout = (LinearLayout) view;
			int count = chiLayout.getChildCount();
			int chilayoutHeight = chiLayout.getMeasuredHeight();
			// MyLog.d(TAG, "count:" + count + " height:" + chilayoutHeight);
			int total = chiLayout.getChildAt(0).getMeasuredHeight()
					+ chiLayout.getChildAt(1).getMeasuredHeight()
					+ chiLayout.getChildAt(2).getMeasuredHeight(); // 总共偏移高度
			MyLog.d(TAG, "total:" + total);
			if (chilayoutHeight - total < oldHeight) {
				// 不够上滑
				if (chilayoutHeight < oldHeight) {
					isRellyLength = false;
				} else {
					isRellyLength = true;
				}

				chiLayout.measure(MeasureSpec.makeMeasureSpec(
						chiLayout.getMeasuredWidth(), MeasureSpec.EXACTLY),
						MeasureSpec.makeMeasureSpec(oldHeight + total,
								MeasureSpec.EXACTLY));
				View child = chiLayout.getChildAt(count - 1);
				if (child != null) {
					child.measure(MeasureSpec.makeMeasureSpec(
							child.getMeasuredWidth(), MeasureSpec.EXACTLY),
							MeasureSpec.makeMeasureSpec(
									oldHeight + total - chilayoutHeight
											+ child.getMeasuredHeight(),
									MeasureSpec.EXACTLY));
				}

			} else {
				isRellyLength = true;
			}
		}
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		// MyLog.d(TAG, "onScrollChanged:"+t);
		super.onScrollChanged(l, t, oldl, oldt);
		if (monScrollChangedListener != null) {
			monScrollChangedListener.onScrollChanged(t);
		}
	}

	public boolean isShowInputMethod() {

		return isShowInput;
	}

	public void setOnInputMethodChangeListener(OnInputMethodChangeListener l) {
		mOnInputMethodChangeListener = l;
	}

	public interface OnInputMethodChangeListener {

		public static final int STATE_PUT_OUT = 0;
		public static final int STATE_PUT_INT = 1;

		public void OnInputMethodChange(int state);
	}

	public void setonScrollChangedListener(onScrollChangedListener l) {
		monScrollChangedListener = l;
	}

	public interface onScrollChangedListener {
		public void onScrollChanged(int scrollY);
	}
}
