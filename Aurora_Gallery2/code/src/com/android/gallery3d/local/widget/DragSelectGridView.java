package com.android.gallery3d.local.widget;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;

import com.android.gallery3d.xcloudalbum.tools.LogUtil;

public class DragSelectGridView extends GridView {
	private static final String TAG = "DragSelectGridView";

	public DragSelectGridView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public DragSelectGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DragSelectGridView(Context context) {
		super(context);
	}

	public boolean setDragSelectActive(boolean active, int initialSelection) {
		if (active && mDragSelectActive) {
			return false;
		}
		mLastDraggedIndex = -1;
		mMinReached = -1;
		mMaxReached = -1;
		// mAdapter.setSelected(initialSelection - 1);
		mDragSelectActive = active;
		mFromPosition = initialSelection;
		mLastDraggedIndex = initialSelection;
		return true;
	}

	public int getItemPosition(MotionEvent e) {
		final View v = findChildViewUnder(e.getX(), e.getY());
		if (v == null) {
			return -2;
		}
		GalleryItemView itemView = (GalleryItemView) v.getTag();
		int index = Integer.valueOf(itemView.getCheckboxOk().getTag() + "");
		// LogUtil.d(TAG, "---getItemPosition:"+index);
		return index;
	}

	public View findChildViewUnder(float x, float y) {
		final int count = getChildCount();
		// LogUtil.d(TAG,
		// "---getChildCount():"+getChildCount()+" "+mAdapter.getCount());
		for (int i = count - 1; i >= 0; i--) {
			final View child = getChildAt(i);
			final float translationX = ViewCompat.getTranslationX(child);
			final float translationY = ViewCompat.getTranslationY(child);
			if (x >= child.getLeft() + translationX && x <= child.getRight() + translationX && y >= child.getTop() + translationY && y <= child.getBottom() + translationY) {
				return child;
			}
		}
		return null;
	}

	public void setAdapter(GalleryItemAdapter adapter) {
		super.setAdapter(adapter);
		this.mAdapter = adapter;
	}

	private GalleryItemAdapter mAdapter;

	private boolean mDragSelectActive;
	private int mMinReached;
	private int mMaxReached;
	private int mLastDraggedIndex = -1;
	private int mFromPosition;
	private int mDownPosition = -1;
	private boolean mUnDragSelect;
	private float mStartX = 0, mStartY = 0;

	@Override
	public boolean dispatchTouchEvent(MotionEvent e) {
		if (e.getAction() == MotionEvent.ACTION_DOWN) {
			mStartX = e.getX();
			mStartY = e.getY();
			if (mDownPosition == -1) {
				mDownPosition = getItemPosition(e);
			}
			if (mAdapter.isSelect(mDownPosition)) {
				mUnDragSelect = false;
			} else {
				mUnDragSelect = true;
			}
			LogUtil.d(TAG, "----mUnDragSelect:" + mUnDragSelect);
		}
		if (!mAdapter.isOperation()) {
			return super.dispatchTouchEvent(e);
		} else {
			switch (e.getAction()) {
			case MotionEvent.ACTION_MOVE:

				final int itemPosition = getItemPosition(e);
				View view = findChildViewUnder(e.getX(), e.getY());
				if (view == null) {
					LogUtil.d(TAG, "----ACTION_MOVE---view");
					return true;
				}
				float v = Math.abs(view.getRight() - view.getLeft());
				float h = Math.abs(view.getBottom() - view.getTop());
				// LogUtil.d(TAG, "---m:" + v + " M:" + Math.abs(e.getX() -
				// mStartX) + " h:" + h + "  H:" + Math.abs(e.getY() -
				// mStartY)+"---mDownPosition:"+mDownPosition);
//				if (Math.abs(e.getX() - mStartX) > v / 2 && !mDragSelectActive && !mUnDragSelect) {
//					mAdapter.setSelected(mDownPosition, false);
//				}
				if (Math.abs(e.getX() - mStartX) > v && !mDragSelectActive) {
					setDragSelectActive(true, itemPosition);
				}
				if (mDragSelectActive) {
					if (Math.abs(e.getX() - mStartX) > v || Math.abs(e.getY() - mStartY) > h/2) {
						if (itemPosition != -2 && mLastDraggedIndex != itemPosition) {
							mLastDraggedIndex = itemPosition;

							if (mMinReached == -1) {
								mMinReached = mLastDraggedIndex;
							}
							if (mMaxReached == -1) {
								mMaxReached = mLastDraggedIndex;
							}
							mMinReached = Math.min(mLastDraggedIndex, Math.min(mMinReached, mFromPosition));
							mMaxReached = Math.max(mLastDraggedIndex, Math.max(mMaxReached, mFromPosition));

//							LogUtil.d(TAG, "---mFromPosition:" + mFromPosition + " mLastDraggedIndex:" + mLastDraggedIndex + " " + "mMinReached:" + mMinReached + " mMaxReached:" + mMaxReached
//									+ "---mDownPosition:" + mDownPosition);
							if (mAdapter != null) {
								boolean init = (mDownPosition != mFromPosition && mDownPosition != -2);
								if (mFromPosition > mLastDraggedIndex) {
									if (init) {
										LogUtil.d(TAG, "----22222222 itemPosition:"+itemPosition);
										mAdapter.selectRange(mDownPosition, mMaxReached + 1, mMinReached, mMaxReached, mUnDragSelect);
									}
									LogUtil.d(TAG, "----55555555 itemPosition:"+itemPosition);
									mAdapter.selectRange((mMaxReached == mFromPosition) ? mMaxReached : (mFromPosition - 1), mLastDraggedIndex, mMinReached, mMaxReached, mUnDragSelect);
								} else {
									if (init) {
										LogUtil.d(TAG, "----3333333333333 itemPosition:"+itemPosition);
										mAdapter.selectRange(mDownPosition, mMinReached - 1, mMinReached, mMaxReached, mUnDragSelect);
									}
									LogUtil.d(TAG, "----66666666 itemPosition:"+itemPosition);
									mAdapter.selectRange((mDownPosition != -2) ? mMinReached : (mFromPosition + 1), mLastDraggedIndex, mMinReached, mMaxReached,mUnDragSelect);
								}
								mDownPosition = -2;
								mFromPosition = mLastDraggedIndex;

							}

						}
						mStartX = e.getX();
						mStartY = e.getY();
					}

					return true;
				}

				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				if (mDragSelectActive) {
					mDragSelectActive = false;
				}
				mAdapter.setOpposite();
				mDownPosition = -1;
				return super.dispatchTouchEvent(e);
			}
		}
		return super.dispatchTouchEvent(e);
	}

}
