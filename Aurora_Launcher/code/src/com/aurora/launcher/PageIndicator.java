package com.aurora.launcher;

import android.R.integer;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.widget.TextView;

/**
 * 
 * @author xiejun
 *
 */
@SuppressLint("NewApi")
public class PageIndicator extends ViewGroup implements PagedView.PageCountChangeListener {
	private LayoutInflater mInflater;
	private int mCellWidth = 0;
	private int mCellHeight=0;
	private int initPageCount = 0;
	private int mCurrentPage = 0;
	private PagedView mPagedView;
	//AURORA-START::Add customized page indicator::Shi guiqiang::20130926
	private TextView mTextView;
	//AURORA-END::Add customized page indicator::Shi guiqiang::20130926
	private Animator mTextViewAnimator;
	private boolean isPageNavigationState = false;
	
	public PageIndicator(Context context) {
		this(context, null);
	}

	public PageIndicator(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public PageIndicator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Log.i("xiejun", "PageIndicator");
		mInflater = LayoutInflater.from(context);
		initPageCount = getResources().getInteger(
				R.integer.page_indicator_max_dot);
		mCellWidth = getResources().getDimensionPixelSize(
				R.dimen.Page_indicator_width);
		mCellHeight=getResources().getDimensionPixelSize(
				R.dimen.Page_indicator_height);
		// initViews(initPageCount);
	}

	public void initPageIndicator(int nums) {
		for (int i = 0; i < nums; i++) {
			addPage();
		}
	}

	@Override
	protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
		final int childCount = getChildCount();
		int childLeft = getPaddingLeft();
		for (int i = 0; i < childCount; i++) {
			final View child = this.getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				
				final int childWidth = child.getMeasuredWidth();;
				final int childHeight = child.getMeasuredHeight();
				int childTop = getPaddingTop();
				child.layout(childLeft, childTop, childLeft + childWidth,
						childTop + childHeight);
				childLeft += childWidth;
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
//AURORA-START::customized page indicator::Shi guiqiang::20131018
		if (getChildCount() < 24) {
			//don't change the value of mCellWidth
		} else {
			mCellWidth = getMeasuredWidth() / getChildCount();
			mCellHeight = (getMeasuredWidth() * getResources().getDimensionPixelSize(R.dimen.Page_indicator_height)) / 
						  (getResources().getDimensionPixelSize(R.dimen.Page_indicator_width) * getChildCount());
			Log.d("DEBUG", "the width of mCellwidth is "+getMeasuredWidth()/getChildCount());
		}
//AURORA-END::customized page indicator::Shi guiqiang::20131018

		setMeasuredDimension(MeasureSpec.makeMeasureSpec(mCellWidth
				* getChildCount(), MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(mCellHeight, MeasureSpec.EXACTLY));
		for (int i = 0; i < getChildCount(); i++) {
			final View child = getChildAt(i);
			final LayoutParams lp = (LayoutParams) child.getLayoutParams();
			int childWidthMode;
			if (lp.width == LayoutParams.WRAP_CONTENT) {
				childWidthMode = MeasureSpec.AT_MOST;
			} else {
				childWidthMode = MeasureSpec.EXACTLY;
			}

			int childHeightMode;
			if (lp.height == LayoutParams.WRAP_CONTENT) {
				childHeightMode = MeasureSpec.AT_MOST;
			} else {
				childHeightMode = MeasureSpec.EXACTLY;
			}
			child.measure(
					MeasureSpec.makeMeasureSpec(mCellWidth, childWidthMode),
					MeasureSpec.makeMeasureSpec(mCellHeight, childHeightMode));
		}
	}

	public void addPage() {
		View view = mInflater.inflate(R.layout.item_indicator, null);
		this.addView(view);
	}

	public void removePage() {
		removePage(getChildCount() - 1);
	}

	public void removePage(int index) {
		Log.i("xiejun","removePage="+index);
		if (index > -1 && index < this.getChildCount()) {
//AURORA-START::Delete the last pageindicator::Shi guiqiang::20131112
			if (this.getChildAt(index) != null) {
				this.removeViewAt(index);
				if (index < mCurrentPage)
					mCurrentPage--;
			}
//AURORA-END::Delete the last pageindicator::Shi guiqiang::20131112
		}
	}

	public void moveIndicatorToPage(int targetPage) {
		
//AURORA-START::Fix bug #544::Shi guiqiang::20131108
//		changeState(mCurrentPage, false);
		mCurrentPage = targetPage;
//		changeState(targetPage, true);
		for (int i = 0; i < getChildCount(); i++) {
			if (i == mCurrentPage) {
				changeState(i, true);
			} else {
				changeState(i, false);
			}
		}
		
//AURORA-END::Fix bug #544::Shi guiqiang::20131108
	}

	private void changeState(int index, boolean active) {
		if (index < 0 || index >= getChildCount()) {
			return;
		}
		this.getChildAt(index).setActivated(active);
	}

	public void setPagedView(PagedView pagedView) {
		mPagedView = pagedView;
	}

	public void setCount(int count) {
		//Aurora-start:xiejun:BUG #120
		if(count-getChildCount()>0){
			initPageIndicator(count-getChildCount());
		}
		//Aurora-end:xiejun:BUG #120
	}

	public void scrollToPage(int index) {

	}
	
//AURORA-START::Add customized page indicator::Shi guiqiang::20130926
	public void setPageIndicatortextView(TextView textView) {
		mTextView = textView;
	}
	
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
    	if (ev.getAction() == MotionEvent.ACTION_DOWN) {
    		if (mTextViewAnimator != null) {
    			mTextViewAnimator.cancel();
    			mTextViewAnimator = null;
    		}
            return true;
        }
//    	return super.onInterceptTouchEvent(ev);
    	return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
		int childCount = this.getChildCount();
		int index = 0;
		int action = ev.getAction();
		if (action == MotionEvent.ACTION_MOVE) {
			final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT; 
			float pointer_x = ev.getX();
			for (int i = 0; i < childCount; i++) {
				View v = this.getChildAt(i);
				float cell_x = v.getX();
				if (pointer_x - cell_x >= 0) index = i;
			}
			//AURORA-START::Customized widgetpage::Shi guiqiang::20131022
			if (mPagedView != null) {
				mPagedView.snapToPage(index, 50);
				mTextView.setAlpha(1);
				String indexString = Integer.toString(index+1);		//display the page number from 1
				mTextView.setText(indexString);
			}
			//AURORA-END::Customized widgetpage::Shi guiqiang::20131022
			return true;
		}
		if (action == MotionEvent.ACTION_UP) {
			//AURORA-START::change animator for page navigation::Shi guiqiang::20140114
			dismissPageNavigation();
			//AURORA-END::change animator for page navigation::Shi guiqiang::20140114
		}
		return true;
    }
//AURORA-END::Add customized page indicator::Shi guiqiang::20130926

	@Override
	public void onPageCountChange(int screen,boolean delete) {
		if(delete){
			if(screen==-1){
				removePage();
			}else{
				removePage(screen);
			}

			LauncherApplication.logVulcan.print("delete screen on onPageCountChange, screen =" + screen);
		}else{
			addPage();
		}
		
	}
	
	//AURORA-START::change animator for page navigation::Shi guiqiang::20140114
	Animator getTextViewAlphaAnimator(boolean animated, boolean direction, int duration, Runnable callback) {
		AnimatorSet anim = animated ? LauncherAnimUtils.createAnimatorSet() : null;
		
		float alpha = direction ? 0.0f : 1.0f;
		if (animated) {
			LauncherViewPropertyAnimator a = new LauncherViewPropertyAnimator(mTextView);
			a.setDuration(duration);
			a.alpha(alpha);    
			anim.play(a);
		} else {
			mTextView.setAlpha(alpha);
		}
		return anim;
	}
	
	public void dismissPageNavigation() {
		if (mTextView != null) {
			mTextViewAnimator = getTextViewAlphaAnimator(true, true, 1000, null);
			if (mTextViewAnimator != null) {
				mTextViewAnimator.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						// TODO Auto-generated method stub
						super.onAnimationEnd(animation);
					}
	
					@Override
					public void onAnimationCancel(Animator animation) {
						// TODO Auto-generated method stub
						mTextView.setAlpha(0f);
						super.onAnimationCancel(animation);
					}
				});
				mTextViewAnimator.start();
			}
		}
	}
	//AURORA-END::change animator for page navigation::Shi guiqiang::20140114
	
}
