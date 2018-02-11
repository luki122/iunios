package com.aurora.puremanager.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;

public class CircleView extends View {

	Paint paint, bg_paint, paint_arc;
	RectF area, area_bg;
	int value = 100;
	LinearGradient shader, bg_shader;
	private float mRotate, mRotate_down;

	public CircleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
		// TODO Auto-generated constructor stub
	}

	public CircleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
		// TODO Auto-generated constructor stub
	}

	public CircleView(Context context) {
		super(context);
		init();
		// TODO Auto-generated constructor stub
	}

	public void setProgress(int value) {
		this.value = value;
		if(value > 0){
			mRotate = 0;
		}
		invalidate();
	}

	public void init() {
		WindowManager wm = (WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE);
		int width = wm.getDefaultDisplay().getWidth();
		
		PathEffect effect = null;
		// 圆弧进度画笔
		paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setStyle(Style.STROKE);
		if (width < 1080) {
			effect = new DashPathEffect(new float[] { 3, 5, 3, 5 }, 1); // 用来控制绘制轮廓(线条)的方式
		} else {
			effect = new DashPathEffect(new float[] { 4, 7, 4, 7 }, 1); // 用来控制绘制轮廓(线条)的方式
		}
		paint.setPathEffect(effect);
		paint.setAntiAlias(true);
		if (width < 1080) {
			paint.setStrokeWidth(30);
		} else {
			paint.setStrokeWidth(45);
		}
		// 背景圆弧画笔
		bg_paint = new Paint();
		bg_paint.setColor(Color.WHITE);
		bg_paint.setStyle(Style.STROKE);
		bg_paint.setPathEffect(effect);
		bg_paint.setAntiAlias(true);
		if (width < 1080) {
			bg_paint.setStrokeWidth(30);
		} else {
			bg_paint.setStrokeWidth(45);
		}
		
		// 外圆环画笔
		paint_arc = new Paint();
		paint_arc.setColor(0xFF97c6dd);
		paint_arc.setStyle(Style.STROKE);
		paint_arc.setAntiAlias(true);
		if (width < 1080) {
			paint_arc.setStrokeWidth(2);
		} else {
			paint_arc.setStrokeWidth(3);
		}

		if (width < 1080) {
			// 圆弧进度位置
			area = new RectF(30, 30, 450, 450);
			// 外圆环位置
			area_bg = new RectF(5, 5, 475, 475);
		} else {
			// 圆弧进度位置
			area = new RectF(45, 45, 675, 675);
			// 外圆环位置
			area_bg = new RectF(7, 7, 712, 712);
		}
		// 圆弧渲染
		shader = new LinearGradient(0, 0, 0, 0, new int[] { Color.WHITE,
				Color.WHITE }, null, Shader.TileMode.CLAMP);
		paint.setShader(shader);

		bg_shader = new LinearGradient(0, 0, 0, 0, new int[] { 0xFF97c6dd,
				0xFF8eb6f8 }, null, Shader.TileMode.CLAMP);
		bg_paint.setShader(bg_shader);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		invalidate();
		canvas.drawArc(area, 135, 270, false, bg_paint);
		canvas.drawArc(area_bg, 135, 270, false, paint_arc);
		mRotate += 1;
		if (mRotate >= value && value > 0) {
			mRotate = value;
			mRotate_down = mRotate;
		}
		// 内存清理时动画
		if (value == 0) {
			mRotate_down -= 0.6;
			if (mRotate_down > 0) {
				canvas.drawArc(area, 135, 270 * mRotate_down / 100, false,
						paint);
			}
		}
		if (mRotate > 0 && value > 0) {
			canvas.drawArc(area, 135, 270 * mRotate / 100, false, paint);
		}
	}
}