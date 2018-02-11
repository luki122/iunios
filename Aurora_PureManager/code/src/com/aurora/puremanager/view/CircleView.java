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

public class CircleView extends View {

	Paint paint, bg_paint, paint_arc;
	RectF area, area_bg;
	int value = 100;
	LinearGradient shader, bg_shader;

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
		invalidate();
	}

	public void init() {
		//圆弧进度画笔
		paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setStyle(Style.STROKE);
		PathEffect effect = new DashPathEffect(new float[] { 3, 5, 3, 5 }, 1); // 用来控制绘制轮廓(线条)的方式
		paint.setPathEffect(effect);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(30);
		//背景圆弧画笔
		bg_paint = new Paint();
		bg_paint.setColor(Color.WHITE);
		bg_paint.setStyle(Style.STROKE);
		bg_paint.setPathEffect(effect);
		bg_paint.setAntiAlias(true);
		bg_paint.setStrokeWidth(30);
		//外圆环画笔
		paint_arc = new Paint();
		paint_arc.setColor(0xFF8eb6f8);
		paint_arc.setStyle(Style.STROKE);
		paint_arc.setAntiAlias(true);
		paint_arc.setStrokeWidth(2);
		//圆弧进度位置
		area = new RectF(30, 30, 450, 450);
		//外圆环位置
		area_bg = new RectF(5, 5, 475, 475);
		//圆弧渲染
		shader = new LinearGradient(0, 0, 0, 0, new int[] { Color.WHITE,
				Color.WHITE }, null, Shader.TileMode.CLAMP);
		paint.setShader(shader);

		bg_shader = new LinearGradient(0, 0, 0, 0, new int[] { 0xFF8eb6f8,
				0xFF8eb6f8 }, null, Shader.TileMode.CLAMP);
		bg_paint.setShader(bg_shader);
	}

	private float mRotate, mRotate_down;

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
		//内存清理时动画
		if(value == 0){
			mRotate_down -= 0.3;
			if (mRotate_down > 0) {
				canvas.drawArc(area, 135, 270 * mRotate_down / 100, false, paint);
			}
		}
		if (mRotate > 0 && value > 0) {
			canvas.drawArc(area, 135, 270 * mRotate / 100, false, paint);
		}
	}
}