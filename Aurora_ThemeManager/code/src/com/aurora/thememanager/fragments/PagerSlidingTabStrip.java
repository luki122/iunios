package com.aurora.thememanager.fragments;

import java.util.Locale;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aurora.thememanager.R;

public class PagerSlidingTabStrip extends LinearLayout {
	
	private static final int MAX_TAB_COUNT = 5;

	public interface IconTabProvider {
		public int getPageIconResId(int position);
	}

	// @formatter:off
	private static final int[] ATTRS = new int[] { android.R.attr.textSize, android.R.attr.textColor };
	// @formatter:on

	private LinearLayout.LayoutParams defaultTabLayoutParams;
	private LinearLayout.LayoutParams expandedTabLayoutParams;

	private final PageListener pageListener = new PageListener();
	public OnPageChangeListener delegatePageListener;

	private ViewPager mPager;

	private int mTabCount;

	private int mCurrentPosition = 0;
	private int mSelectedPosition = 0;
	private float mCurrentPositionOffset = 0f;

	private Paint mRectPaint;
	private Paint mDividerPaint;

	private int mIndicatorColor = 0xFFff0000;
	private int mUnderlineColor = 0x1A000000;
	private int mDividerColor = 0x1A000000;

	private boolean mShouldExpand = false;
	private boolean mTextAllCaps = true;
	private boolean mShowDivider = true;

	private int mScrollOffset = 52;
	private int mIndicatorHeight = 4;
	private int mUnderlineHeight = 2;
	private int mDividerPadding = 12;
	private int mTabPadding = 24;
	private int mDividerWidth = 1;

	private int mTabTextSize = 12;
	private int mTabTextColor = 0xFF666666;
	private int mSelectedTabTextColor = 0xFF666666;
	private Typeface mTabTypeface = null;
	private int mTabTypefaceStyle = Typeface.NORMAL;

	private int mLastScrollX = 0;

	private int mTabBackgroundResId = R.drawable.background_tab;

	private Locale mLocale;

	public PagerSlidingTabStrip(Context context) {
		this(context, null);
	}

	public PagerSlidingTabStrip(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PagerSlidingTabStrip(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		setWillNotDraw(false);

		setOrientation(LinearLayout.HORIZONTAL);
		setGravity(Gravity.CENTER);
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		DisplayMetrics dm = getResources().getDisplayMetrics();

		mScrollOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mScrollOffset, dm);
		mIndicatorHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mIndicatorHeight, dm);
		mUnderlineHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mUnderlineHeight, dm);
		mDividerPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mDividerPadding, dm);
		mTabPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mTabPadding, dm);
		mDividerWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mDividerWidth, dm);
		mTabTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTabTextSize, dm);

		// get system attrs (android:textSize and android:textColor)

		TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);

		mTabTextSize = a.getDimensionPixelSize(0, mTabTextSize);
		mTabTextColor = a.getColor(1, mTabTextColor);

		a.recycle();

		// get custom attrs

		a = context.obtainStyledAttributes(attrs, R.styleable.PagerSlidingTab);

		mIndicatorColor = a.getColor(R.styleable.PagerSlidingTab_indicatorColor, mIndicatorColor);
		mUnderlineColor = a.getColor(R.styleable.PagerSlidingTab_underlineColor, mUnderlineColor);
		mDividerColor = a.getColor(R.styleable.PagerSlidingTab_dividerColor, mDividerColor);
		mIndicatorHeight = a.getDimensionPixelSize(R.styleable.PagerSlidingTab_indicatorHeight,
				mIndicatorHeight);
		mUnderlineHeight = a.getDimensionPixelSize(R.styleable.PagerSlidingTab_underlineHeight,
				mUnderlineHeight);
		mDividerPadding = a.getDimensionPixelSize(R.styleable.PagerSlidingTab_dividerPadding,
				mDividerPadding);
		mTabPadding = a.getDimensionPixelSize(R.styleable.PagerSlidingTab_tabPaddingLeftRight,
				mTabPadding);
		mTabBackgroundResId = a.getResourceId(R.styleable.PagerSlidingTab_tabBackground,
				mTabBackgroundResId);
		mShouldExpand = a.getBoolean(R.styleable.PagerSlidingTab_shouldExpand, mShouldExpand);
		mScrollOffset = a.getDimensionPixelSize(R.styleable.PagerSlidingTab_scrollOffset,
				mScrollOffset);
		mTextAllCaps = a.getBoolean(R.styleable.PagerSlidingTab_textAllCaps, mTextAllCaps);
		mShowDivider = a.getBoolean(R.styleable.PagerSlidingTab_showDivider, mTextAllCaps);
		a.recycle();

		mRectPaint = new Paint();
		mRectPaint.setAntiAlias(true);
		mRectPaint.setStyle(Style.FILL);

		mDividerPaint = new Paint();
		mDividerPaint.setAntiAlias(true);
		mDividerPaint.setStrokeWidth(mDividerWidth);

		defaultTabLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.MATCH_PARENT);
		expandedTabLayoutParams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);

		if (mLocale == null) {
			mLocale = getResources().getConfiguration().locale;
		}
	}

	public void setViewPager(ViewPager pager) {
		this.mPager = pager;

		if (pager.getAdapter() == null) {
			throw new IllegalStateException("ViewPager does not have adapter instance.");
		}

		pager.setOnPageChangeListener(pageListener);

		notifyDataSetChanged();
	}

	public void setOnPageChangeListener(OnPageChangeListener listener) {
		this.delegatePageListener = listener;
	}

	public void notifyDataSetChanged() {

		removeAllViews();

		mTabCount = mPager.getAdapter().getCount();

		for (int i = 0; i < mTabCount; i++) {

			if (mPager.getAdapter() instanceof IconTabProvider) {
				addIconTab(i, ((IconTabProvider) mPager.getAdapter()).getPageIconResId(i));
			} else {
				addTextTab(i, mPager.getAdapter().getPageTitle(i).toString());
			}

		}

		updateTabStyles();

		getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				getViewTreeObserver().removeGlobalOnLayoutListener(this);
				mCurrentPosition = mPager.getCurrentItem();
				
			}
		});

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//		int childCount = getChildCount();
//		int width = getResources().getDisplayMetrics().widthPixels;
//		if(childCount <=0){
//			return;
//		}
//		for(int i = 0;i<childCount;i++){
//			View child = getChildAt(i);
//			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width/childCount, ViewGroup.LayoutParams.MATCH_PARENT);
//			params.weight = 1;
//			child.setLayoutParams(params);
//		}
	}
	
	
	private void addTextTab(final int position, String title) {

		TextView tab = new TextView(getContext());
		tab.setText(title);
		tab.setGravity(Gravity.CENTER);
		tab.setSingleLine();
		addTab(position, tab);
	}

	private void addIconTab(final int position, int resId) {

		ImageButton tab = new ImageButton(getContext());
		tab.setImageResource(resId);

		addTab(position, tab);

	}

	private void addTab(final int position, View tab) {
		if(getChildCount() > MAX_TAB_COUNT){
			try {
				throw new IllegalAccessException("tab count must less than 5");
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		tab.setFocusable(true);
		tab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mPager.setCurrentItem(position);
			}
		});

		tab.setPadding(mTabPadding, 0, mTabPadding, 0);
		addView(tab, position, mShouldExpand ? expandedTabLayoutParams : defaultTabLayoutParams);
	}

	private void updateTabStyles() {

		for (int i = 0; i < mTabCount; i++) {

			View v = getChildAt(i);

			v.setBackgroundResource(mTabBackgroundResId);

			if (v instanceof TextView) {

				TextView tab = (TextView) v;
				tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTabTextSize);
				tab.setTypeface(mTabTypeface, mTabTypefaceStyle);
				tab.setTextColor(mTabTextColor);

				// setAllCaps() is only available from API 14, so the upper case
				// is made manually if we are on a
				// pre-ICS-build
				if (mTextAllCaps) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
						tab.setAllCaps(true);
					} else {
						tab.setText(tab.getText().toString().toUpperCase(mLocale));
					}
				}
				if (i == mSelectedPosition) {
					tab.setTextColor(mSelectedTabTextColor);
				}
			}
		}

	}


	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (isInEditMode() || mTabCount == 0) {
			return;
		}

		final int height = getHeight();

		// draw underline
		mRectPaint.setColor(mUnderlineColor);
		canvas.drawRect(0, height - mUnderlineHeight, getWidth(), height, mRectPaint);

		// draw indicator line
		mRectPaint.setColor(mIndicatorColor);

		// default: line below current tab
		View currentTab = getChildAt(mCurrentPosition);
		float lineLeft = currentTab.getLeft();
		float lineRight = currentTab.getRight();

		// if there is an offset, start interpolating left and right coordinates
		// between current and next tab
		if (mCurrentPositionOffset > 0f && mCurrentPosition < mTabCount - 1) {

			View nextTab = getChildAt(mCurrentPosition + 1);
			final float nextTabLeft = nextTab.getLeft();
			final float nextTabRight = nextTab.getRight();

			lineLeft = (mCurrentPositionOffset * nextTabLeft + (1f - mCurrentPositionOffset) * lineLeft);
			lineRight = (mCurrentPositionOffset * nextTabRight + (1f - mCurrentPositionOffset) * lineRight);
		}

		canvas.drawRect(lineLeft, height - mIndicatorHeight, lineRight, height, mRectPaint);

		// draw divider
		if (mShowDivider) {
			mDividerPaint.setColor(mDividerColor);
			for (int i = 0; i < mTabCount - 1; i++) {
				View tab = getChildAt(i);
				canvas.drawLine(tab.getRight(), mDividerPadding,
						tab.getRight(), height - mDividerPadding, mDividerPaint);
			}
		}
	}

	private class PageListener implements OnPageChangeListener {

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			mCurrentPosition = position;
			mCurrentPositionOffset = positionOffset;


			invalidate();

			if (delegatePageListener != null) {
				delegatePageListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			if (state == ViewPager.SCROLL_STATE_IDLE) {
				
			}

			if (delegatePageListener != null) {
				delegatePageListener.onPageScrollStateChanged(state);
			}
		}

		@Override
		public void onPageSelected(int position) {
			mSelectedPosition = position;
			updateTabStyles();
			if (delegatePageListener != null) {
				delegatePageListener.onPageSelected(position);
			}
		}

	}

	public void setIndicatorColor(int indicatorColor) {
		this.mIndicatorColor = indicatorColor;
		invalidate();
	}

	public void setIndicatorColorResource(int resId) {
		this.mIndicatorColor = getResources().getColor(resId);
		invalidate();
	}

	public int getIndicatorColor() {
		return this.mIndicatorColor;
	}

	public void setIndicatorHeight(int indicatorLineHeightPx) {
		this.mIndicatorHeight = indicatorLineHeightPx;
		invalidate();
	}

	public int getIndicatorHeight() {
		return mIndicatorHeight;
	}

	public void setUnderlineColor(int underlineColor) {
		this.mUnderlineColor = underlineColor;
		invalidate();
	}

	public void setUnderlineColorResource(int resId) {
		this.mUnderlineColor = getResources().getColor(resId);
		invalidate();
	}

	public int getUnderlineColor() {
		return mUnderlineColor;
	}

	public void setDividerColor(int dividerColor) {
		this.mDividerColor = dividerColor;
		invalidate();
	}

	public void setDividerColorResource(int resId) {
		this.mDividerColor = getResources().getColor(resId);
		invalidate();
	}

	public int getDividerColor() {
		return mDividerColor;
	}

	public void setUnderlineHeight(int underlineHeightPx) {
		this.mUnderlineHeight = underlineHeightPx;
		invalidate();
	}

	public int getUnderlineHeight() {
		return mUnderlineHeight;
	}

	public void setDividerPadding(int dividerPaddingPx) {
		this.mDividerPadding = dividerPaddingPx;
		invalidate();
	}

	public int getDividerPadding() {
		return mDividerPadding;
	}

	public void setScrollOffset(int scrollOffsetPx) {
		this.mScrollOffset = scrollOffsetPx;
		invalidate();
	}

	public int getScrollOffset() {
		return mScrollOffset;
	}

	public void setShouldExpand(boolean shouldExpand) {
		this.mShouldExpand = shouldExpand;
		notifyDataSetChanged();
	}

	public boolean getShouldExpand() {
		return mShouldExpand;
	}

	public boolean isTextAllCaps() {
		return mTextAllCaps;
	}

	public void setAllCaps(boolean textAllCaps) {
		this.mTextAllCaps = textAllCaps;
	}

	public void setTextSize(int textSizePx) {
		this.mTabTextSize = textSizePx;
		updateTabStyles();
	}

	public int getTextSize() {
		return mTabTextSize;
	}

	public void setTextColor(int textColor) {
		this.mTabTextColor = textColor;
		updateTabStyles();
	}

	public void setTextColorResource(int resId) {
		this.mTabTextColor = getResources().getColor(resId);
		updateTabStyles();
	}

	public int getTextColor() {
		return mTabTextColor;
	}

	public void setSelectedTextColor(int textColor) {
		this.mSelectedTabTextColor = textColor;
		updateTabStyles();
	}

	public void setSelectedTextColorResource(int resId) {
		this.mSelectedTabTextColor = getResources().getColor(resId);
		updateTabStyles();
	}

	public int getSelectedTextColor() {
		return mSelectedTabTextColor;
	}

	public void setTypeface(Typeface typeface, int style) {
		this.mTabTypeface = typeface;
		this.mTabTypefaceStyle = style;
		updateTabStyles();
	}

	public void setTabBackground(int resId) {
		this.mTabBackgroundResId = resId;
		updateTabStyles();
	}

	public int getTabBackground() {
		return mTabBackgroundResId;
	}

	public void setTabPaddingLeftRight(int paddingPx) {
		this.mTabPadding = paddingPx;
		updateTabStyles();
	}

	public int getTabPaddingLeftRight() {
		return mTabPadding;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());
		mCurrentPosition = savedState.currentPosition;
		requestLayout();
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		SavedState savedState = new SavedState(superState);
		savedState.currentPosition = mCurrentPosition;
		return savedState;
	}

	static class SavedState extends BaseSavedState {
		int currentPosition;

		public SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			currentPosition = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(currentPosition);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

}
