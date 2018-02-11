package com.aurora.widget;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.aurora.filemanager.R;

/**
 */
public class PieChart extends View {

	public interface OnSelectedLisenter {
		public abstract void onSelected(int iSelectedIndex);
	}

	private static final String TAG = PieChart.class.getName();
	public static final String ERROR_NOT_EQUAL_TO_100 = "NOT_EQUAL_TO_100";
	private static final int DEGREE_360 = 360;
	private static String[] PIE_COLORS = null;
	private static int iColorListSize = 0;

	private Paint paintPieFill;
	private Paint paintPieBorder;
	private ArrayList<Float> alPercentage = new ArrayList<Float>();

	private int iDisplayWidth, iDisplayHeight;
	private int iSelectedIndex = -1;
	private int iCenterWidth = 0;
	private int iShift = 0;
	private int iMargin = 0; // margin to left and right, used for get Radius
	private int iDataSize = 0;

	private RectF r = null;

	private float fDensity = 0.0f;
	private float fStartAngle = 0.0f;
	private float fEndAngle = 0.0f;

	private int spaceCount = 0;//隔断层数量，角度计算

	public PieChart(Context context, AttributeSet attrs) {
		super(context, attrs);
		PIE_COLORS = getResources().getStringArray(R.array.colors);
		iColorListSize = PIE_COLORS.length;

		fnGetDisplayMetrics(context);

		// used for paint circle
		paintPieFill = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintPieFill.setStyle(Paint.Style.STROKE);
		paintPieFill.setStrokeWidth(fnGetRealPxFromDp(33));

	}

	// set listener
	public void setOnSelectedListener(OnSelectedLisenter listener) {
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		for (int i = 0; i < iDataSize; i++) {
			if (alPercentage.get(i) == 0)
				continue;
			// check whether the data size larger than color list size
			if (i >= iColorListSize) {
				paintPieFill.setColor(Color.parseColor(PIE_COLORS[i
						% iColorListSize]));
			} else {
				paintPieFill.setColor(Color.parseColor(PIE_COLORS[i]));
			}

			fEndAngle = alPercentage.get(i);

			// convert percentage to angle
			fEndAngle = fEndAngle / 100 * (DEGREE_360 );// 中间隔断层计算
			canvas.drawArc(r, fStartAngle, fEndAngle+0.5F, false, paintPieFill);
			fStartAngle = fStartAngle + fEndAngle;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// get screen size
		iDisplayWidth = MeasureSpec.getSize(widthMeasureSpec);
		iDisplayHeight = MeasureSpec.getSize(heightMeasureSpec);
		/*
		 * determine the rectangle size
		 */
		iCenterWidth = iDisplayWidth / 4;
		int iR=(int) fnGetRealPxFromDp(62);
		if (r == null) {
			r = new RectF(iCenterWidth - iR, // top
					iCenterWidth - iR, // left
					iCenterWidth + iR, // rights
					iCenterWidth + iR); // bottom
		}
		setMeasuredDimension(iCenterWidth*2, iCenterWidth*2);// 设置画布大小
	}


	private void fnGetDisplayMetrics(Context cxt) {
		final DisplayMetrics dm = cxt.getResources().getDisplayMetrics();
		fDensity = dm.density;
	}

	private float fnGetRealPxFromDp(float fDp) {
		return (fDensity != 1.0f) ? fDensity * fDp : fDp;
	}

	public void setAdapter(ArrayList<Float> alPercentage) throws Exception {
		spaceCount = 0;
		this.alPercentage = alPercentage;
		iDataSize = alPercentage.size();
		for (int i = 0; i < iDataSize; i++) {
			alPercentage.get(i);
			if (alPercentage.get(i) != 0)
				spaceCount++;
		}
		Log.i(TAG, "spaceCount=" + spaceCount);

	}

}
