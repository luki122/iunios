package com.aurora.account.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.TextView;

public class ExpandableTextView extends TextView {

	private Context m_context;
	private int linecount = 3;
	private boolean isFold = false;
	private static final int DEFAULT_COLLAPSE_LINES = 3;
	private Paint paint = getPaint();
	private static final int INVALID_HEIGHT = -1;
	private int mOriginalHeight = INVALID_HEIGHT;
	// private int mCollapseLines = DEFAULT_COLLAPSE_LINES;
	private int mCollapseHeight = INVALID_HEIGHT;
	private boolean mRequestChangeCollapseLines;

	public ExpandableTextView(Context context) {
		super(context);

		m_context = context;
	}

	public ExpandableTextView(Context context, AttributeSet attrs) {
		super(context, attrs);

		m_context = context;
	}

	public ExpandableTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		m_context = context;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// setLines(linecount);
		/*
		 * if (mOriginalHeight == INVALID_HEIGHT) { mOriginalHeight =
		 * getMeasuredHeight(); }
		 * 
		 * setMeasuredDimension(originalWidth, mOriginalHeight);
		 */
		/*
		 * final int originalHeightMode =
		 * MeasureSpec.getMode(heightMeasureSpec); final int originalWidth =
		 * MeasureSpec.getSize(widthMeasureSpec);
		 * 
		 * //if (mCollapseHeight == INVALID_HEIGHT ||
		 * mRequestChangeCollapseLines) { setLines(linecount);
		 * super.onMeasure(widthMeasureSpec, heightMeasureSpec); mCollapseHeight
		 * = getMeasuredHeight(); // setLines(Integer.MAX_VALUE);
		 * mRequestChangeCollapseLines = false; // }
		 * 
		 * super.onMeasure(widthMeasureSpec,
		 * MeasureSpec.makeMeasureSpec(mCollapseHeight, originalHeightMode));
		 * setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
		 * mCollapseHeight);
		 */
		
		
		/*int height =getMeasuredHeight();*/
		if(isFold)
		{
			Paint.FontMetrics localFontMetrics = getPaint().getFontMetrics();
			float m = (int) Math.ceil(localFontMetrics.descent
					- localFontMetrics.ascent)+getLineSpacingExtra();
			int height1 = (int)(linecount*m) +getCompoundPaddingTop()+getCompoundPaddingBottom();
			setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),height1);
		}

	}

	public void setCollapseLines(int collapseLines,boolean isFold) {
		linecount = collapseLines;
		this.isFold = isFold;
		mRequestChangeCollapseLines = true;
		requestLayout();
	}

	public static int getoffsetleft(Context context, float paramFloat) {
		return (int) (0.5F + paramFloat
				* context.getResources().getDisplayMetrics().density);
	}

	@Override
	protected void onDraw(Canvas arg0) {
		// TODO Auto-generated method stub
		// super.onDraw(arg0);
		if(!isFold)
		{
			super.onDraw(arg0);
			return;
		}
		
		paint.setColor(getCurrentTextColor());
		paint.setTextSize(getTextSize());
		String str1 = getText().toString();
		
		String str2 = str1;

		int count = linecount;// getLineCount()
		

		arg0.translate(getPaddingLeft(),
				getPaddingTop() - paint.getFontMetrics().descent);

		int i = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
		int j = getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - (int)(getMeasuredWidth()*0.3);
		Paint.FontMetrics localFontMetrics = paint.getFontMetrics();
		int m = 0;
		
		for (int ii = 0; ii < count; ii++) {
			int k = (int) Math.ceil(localFontMetrics.descent
					- localFontMetrics.ascent);
			int i2 = 0;
			if (ii == count - 1) {
				i2 = paint.breakText(str2, true, j, null);
			} else {
				i2 = paint.breakText(str2, true, i, null);
			}
			String str5 = str2.substring(0, i2);

			int i3 = str5.indexOf("\n");

			if (i3 == -1) {
				if ((ii == count - 1) && (ii <= 2)&&isFold) {
					str5 += "...";
				}
				str2 = str2.substring(i2, str2.length());
			} else {
				str5 = str5.substring(0, i3);
				str2 = str2.substring(i3 + 1, str2.length());
			}
			m += k+getLineSpacingExtra();
			arg0.drawText(str5, 0.0F, m, paint);

		}
	}

}
