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
import android.view.MotionEvent;
import android.view.View;

import com.aurora.filemanager.R;
import com.aurora.tools.LogUtil;

/**
 */
public class SinglePieChart extends View {

//	public interface OnSelectedLisenter {
//		public abstract void onSelected(int iSelectedIndex);
//	}
//
//	private OnSelectedLisenter onSelectedListener = null;

	private static final String TAG = SinglePieChart.class.getName();
	public static final String ERROR_NOT_EQUAL_TO_100 = "NOT_EQUAL_TO_100";
	private static final float DEGREE_360 = 360.0f;
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

	private int spaceCount = 0;// 隔断层数量，角度计算

	public SinglePieChart(Context context, AttributeSet attrs) {
		super(context, attrs);
		PIE_COLORS = getResources().getStringArray(R.array.colors);
		iColorListSize = PIE_COLORS.length;

		fnGetDisplayMetrics(context);
//		iShift = (int) fnGetRealPxFromDp(30);
//		iMargin = (int) fnGetRealPxFromDp(36);

		// used for paint circle
		paintPieFill = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintPieFill.setStyle(Paint.Style.STROKE);
		paintPieFill.setStrokeWidth(fnGetRealPxFromDp(52));
		// used for paint border
//		paintPieBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
//		paintPieBorder.setStyle(Paint.Style.STROKE);
//		paintPieBorder.setStrokeWidth(fnGetRealPxFromDp(3));
//		paintPieBorder.setColor(Color.WHITE);
//		Log.i(TAG, "PieChart init");

	}

	// set listener
//	public void setOnSelectedListener(OnSelectedLisenter listener) {
//		this.onSelectedListener = listener;
//	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Log.i(TAG, "onDraw");
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
			fEndAngle = fEndAngle / 100F * (DEGREE_360 );// 中间隔断层计算
//			LogUtil.elog(TAG, "fEndAngle=="+fEndAngle);
//			fEndAngle = fEndAngle / 100 * (DEGREE_360 - 2 * spaceCount);// 中间隔断层计算

			// if the part of pie was selected then change the coordinate
//			if (iSelectedIndex == i) {
//				canvas.save(Canvas.MATRIX_SAVE_FLAG);
//				float fAngle = fStartAngle + fEndAngle / 2;
//				double dxRadius = Math.toRadians((fAngle + DEGREE_360)
//						% DEGREE_360);
//				float fY = (float) Math.sin(dxRadius);
//				float fX = (float) Math.cos(dxRadius);
//				canvas.translate(fX * iShift, fY * iShift);
//			}
			canvas.drawArc(r, fStartAngle, fEndAngle+0.5F, false, paintPieFill);//+0.5F解决画圆弧出现空白间隙

			// if the part of pie was selected then draw a border
//			if (iSelectedIndex == i) {
//				canvas.drawArc(r, fStartAngle, fEndAngle, true, paintPieBorder);
//				canvas.restore();
//			}
			fStartAngle = fStartAngle + fEndAngle ;
//		LogUtil.elog(TAG, "1dp==px"+dp2px(getContext(), 1));	
//			invalidate();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// get screen size
		iDisplayWidth = MeasureSpec.getSize(widthMeasureSpec);
		iDisplayHeight = MeasureSpec.getSize(heightMeasureSpec);

//		if (iDisplayWidth > iDisplayHeight) {
//			iDisplayWidth = iDisplayHeight;
//		}

		/*
		 * determine the rectangle size
		 */
		iCenterWidth =iDisplayWidth / 2;
		// int iR = iCenterWidth - iMargin;
		int iR = (int) fnGetRealPxFromDp(98);//u2 98
//		Log.i(TAG, "iCenterWidth=" + iCenterWidth+" iR="+iR+" fDensity="+fDensity);
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
//		return (fDensity != 1.0f) ? fDensity * fDp : fDp;
		return fDp*fDensity+0.5f;
	}

	public void setAdapter(ArrayList<Float> alPercentage) throws Exception {
//		spaceCount = 0;
		this.alPercentage = alPercentage;
		iDataSize = alPercentage.size();
//		float fSum = 0;
//		for (int i = 0; i < iDataSize; i++) {
//			fSum += alPercentage.get(i);
//			if (alPercentage.get(i) != 0)
//				spaceCount++;
//		}
//		Log.i(TAG, "spaceCount=" + spaceCount);

	}
	
	/** 
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素) 
     */  
    public static int dp2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (dpValue * scale + 0.5f);  
    }  
  
    /** 
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 
     */  
    public static int px2dp(Context context, float pxValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (pxValue / scale + 0.5f);  
    }  

}
