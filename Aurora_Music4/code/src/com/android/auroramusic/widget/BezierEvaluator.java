package com.android.auroramusic.widget;

import android.animation.TypeEvaluator;
import android.graphics.PointF;

public class BezierEvaluator implements TypeEvaluator<Object> {
	int startX;
	int startY;
	int endX;
	int endY;
	int controlX;
	int controlY;

	static class Result{
		PointF pointF;
		float scalex;
	}
	public BezierEvaluator(int sx, int sy, int ex, int ey) {
		startX = sx;
		startY = sy;
		endX = ex;
		endY = ey;
	}


	@Override
	public PointF evaluate(float fraction, Object startValue, Object endValue) {
		// TODO Auto-generated method stub
		final float t = fraction;
		float oneMinusT = 1.0f - t;
//		PointF point = new PointF();
		PointF point0 = (PointF) startValue;
//
//		PointF point1 = new PointF();
//		PointF point2 = new PointF();
//		point1.set(startX+(endX-startX)/2, startY);
//		point2.set(startX+(endX-startX)/2, endY);
		PointF point3 = (PointF) endValue;
//
//		point.x = oneMinusT * oneMinusT * oneMinusT * (point0.x) + 3
//				* oneMinusT * oneMinusT * t * (point1.x) + 3 * oneMinusT * t
//				* t * (point2.x) + t * t * t * (point3.x);
//
//		point.y = oneMinusT * oneMinusT * oneMinusT * (point0.y) + 3
//				* oneMinusT * oneMinusT * t * (point1.y) + 3 * oneMinusT * t
//				* t * (point2.y) + t * t * t * (point3.y);
		PointF point = new PointF();
		point.x = oneMinusT*point0.x+t*point3.x;
		point.y = oneMinusT*point0.y+t*point3.y;
		return point;
	}
}
