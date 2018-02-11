package views;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.animation.TranslateAnimation;

public class WeatherViewPager extends ViewPager {

	private Rect mRect = new Rect();//用来记录初始位置
	private int pagerCount = 3;
	private int currentItem = 0;
	private boolean handleDefault = true;
	private float preX = 0f;
	private static final float RATIO = 0.8f;//摩擦系数
	private static final float SCROLL_WIDTH = 15f;
	private float mOffset = 0f;

	
	public WeatherViewPager(Context context) {
		super(context);
	}

	public WeatherViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setCurrentOffset(float offset) {
		this.mOffset = offset;
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			preX = event.getX();//记录起点
		}
		return super.onInterceptTouchEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if(getAdapter()!=null)
			{
				pagerCount = getAdapter().getCount();
				currentItem = getCurrentItem();
			}
			break;
		case MotionEvent.ACTION_UP:
			onTouchActionUp();
			break;
		case MotionEvent.ACTION_MOVE:
			
			//Log.e("111111", "---WeatherViewPager ACTION_MOVE event.getPointerCount() = ---" + event.getPointerCount());
			
			//当时滑到第一项或者是最后一项的时候。
			if ((currentItem == 0 || currentItem == pagerCount - 1)) {
				float nowX = event.getX();
				float offset = nowX - preX;
				preX = nowX;
				if (currentItem == 0 && mOffset == 0f) {
					if (offset > SCROLL_WIDTH) {//手指滑动的距离大于设定值
						whetherConditionIsRight(offset);
					} else if (!handleDefault) {//这种情况是已经出现缓冲区域了，手指慢慢恢复的情况
						if (getLeft() + (int) (offset * RATIO) >= mRect.left) {
							layout(getLeft() + (int) (offset * RATIO), getTop(), getRight() + (int) (offset * RATIO), getBottom());
						}
					}
				}
				if (currentItem == pagerCount - 1  && mOffset == 0f){
					if (offset < -SCROLL_WIDTH) {
						whetherConditionIsRight(offset);
					} else if (!handleDefault) {
						if (getRight() + (int) (offset * RATIO) <= mRect.right) {
							layout(getLeft() + (int) (offset * RATIO), getTop(), getRight() + (int) (offset * RATIO), getBottom());
						}
					}
				}
			} else {
				handleDefault = true;
			}

			if (!handleDefault) {
				return true;
			}
			break;

		default:
			break;
		}
		return super.onTouchEvent(event);
	}

	private boolean isInTwoSides(){
		if(currentItem == 0 || currentItem == pagerCount-1)
		{
			return true;
		}
		
		return false;
	}
	
	
	private void whetherConditionIsRight(float offset) {
		if (mRect.isEmpty()) {
			mRect.set(getLeft(), getTop(), getRight(), getBottom());
		}
		handleDefault = false;
		layout(getLeft() + (int) (offset * RATIO), getTop(), getRight() + (int) (offset * RATIO), getBottom());
	}

	private void onTouchActionUp() {
		if (!mRect.isEmpty()) {
			recoveryPosition();
		}
	}

	private void recoveryPosition() {
		TranslateAnimation ta = null;
		ta = new TranslateAnimation(getLeft(), mRect.left, 0, 0);
		ta.setDuration(300);
		startAnimation(ta);
		layout(mRect.left, mRect.top, mRect.right, mRect.bottom);
		mRect.setEmpty();
		handleDefault = true;
	}

}

