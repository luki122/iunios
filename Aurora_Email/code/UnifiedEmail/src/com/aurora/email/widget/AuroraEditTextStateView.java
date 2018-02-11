package com.aurora.email.widget;

import java.util.ArrayList;
import java.util.Hashtable;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import com.android.mail.R;

public class AuroraEditTextStateView extends RelativeLayout {
	int mLeft, mRight, mTop, mBottom, currentBottom;
	Hashtable<View, Position> map = new Hashtable<View, AuroraEditTextStateView.Position>();
	AuroraListener mListener;
	int mRows;
	int mHeight;
	int mOldChildCount,mOldChildCount2;
	int mAddressWidth;

	public interface AuroraListener {
		void changeHeight(int height,boolean flag);
	}

	public void setAuroraListener(AuroraListener listener) {
		mListener = listener;
	}

	public AuroraEditTextStateView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);

	}

	public AuroraEditTextStateView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);

	}

	public AuroraEditTextStateView(Context context) {
		super(context);

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		int count = getChildCount();
//		if(count>mOldChildCount2){
//			mListener.changeHeight(mRows,true);	
//		}
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			Position pos = map.get(child);
			if (pos != null) {
				child.layout(pos.left, pos.top, pos.right, pos.bottom);
			} else {
				Log.i("MyLayout", "error");
			}
		}
		mOldChildCount2 = count;

		//paul add
		if(null != mListener){
			mListener.changeHeight(mRows,true);	
		}
	}

	public int getPosition(int IndexInRow, int childIndex) {
		if (IndexInRow > 0) {
			return getPosition(IndexInRow - 1, childIndex - 1)
					+ getChildAt(childIndex - 1).getMeasuredWidth();
		}
		return 0;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec);
		mLeft = 0;
		mRight = 0;
		mTop = 0;
		mBottom = 0;
		int j = 0;
		int index = 0;
		int count = getChildCount();
		mRows = 0;
		for (int i = 0; i < count; i++) {
			Position position = new Position();
			View view = getChildAt(i);
			mLeft = getPosition(i - j, i);
			mRight = mLeft + view.getMeasuredWidth();
			if (mRight > width) {
				j = i;
				if (view.getId() != R.id.autocomplete_textview) {
					mLeft = getPosition(i - j, i);
					mTop += getChildAt(i).getMeasuredHeight();
					mRows++;
				}
			}
			if (mLeft >= width - mAddressWidth) {
				j = i;
				mLeft = getPosition(i - j, i);
				mTop += mHeight;
				mRows++;
			}
			if (view.getId() == R.id.autocomplete_textview) {
				mRight = view.getMeasuredWidth();
			} else
				mRight = mLeft + view.getMeasuredWidth();
			mBottom = mTop + view.getMeasuredHeight();
			position.left = mLeft;
			position.top = mTop;
			position.right = mRight;
			position.bottom = mBottom;
			map.put(view, position);
		}
//		if (mOldChildCount > count) {
//				mListener.changeHeight(mRows,false);
//		}
		mOldChildCount = count;
		setMeasuredDimension(width, mBottom);
	}

	private class Position {
		int left, top, right, bottom;
	}

	private void init(Context context) {
		mHeight = context.getResources().getDimensionPixelSize(
				R.dimen.aurora_address_itemheight);
		mAddressWidth = context.getResources().getDimensionPixelSize(
				R.dimen.aurora_address_width);
	}

}