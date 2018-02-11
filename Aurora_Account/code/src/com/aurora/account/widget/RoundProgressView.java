package com.aurora.account.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

import com.aurora.account.R;
import com.aurora.account.util.CustomAnimCallBack;
import com.aurora.account.util.CustomAnimation;

public class RoundProgressView extends View {

	/**
	 * 画笔对象的引用
	 */
	private Paint paint;
	
	/**
	 * 文字画笔 
	 */
	private Paint textPaint;
	private FontMetrics fontMetrics;

	/**
	 * 圆环的颜色
	 */
	private int roundColor;

	/**
	 * 圆环进度的颜色
	 */
	private int roundProgressColor;

	/**
	 * 圆环的宽度
	 */
	private float roundWidth;
	
	/**
	 * 字体大小
	 */
	private float textSize;
	
	/**
	 * 字体颜色
	 */
	private int textColor;
	
	/**
	 * 当前进度
	 */
	private int progress;
	
	/**
	 * 最大进度
	 */
	private int max;
	
	private RectF oval;
	private int centre;
	private float radius;
	
	private boolean needClear = false;;
	
	private boolean showText = true;
	
	public RoundProgressView(Context context) {
		this(context, null);
	}

	public RoundProgressView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RoundProgressView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		paint = new Paint();

		TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
				R.styleable.RoundProgressView);

		// 获取自定义属性和默认值
		roundColor = mTypedArray.getColor(
				R.styleable.RoundProgressView_roundColor, Color.TRANSPARENT);
		roundProgressColor = mTypedArray.getColor(
				R.styleable.RoundProgressView_roundProgressColor, Color.GREEN);
		roundWidth = mTypedArray.getDimension(
				R.styleable.RoundProgressView_roundWidth, 5);
		textSize = mTypedArray.getDimension(R.styleable.RoundProgressView_textSize, 50);
		textColor = mTypedArray.getColor(R.styleable.RoundProgressView_textColor, Color.GRAY);
		progress = mTypedArray.getInteger(R.styleable.RoundProgressView_progress, 0);
		max = mTypedArray.getInteger(R.styleable.RoundProgressView_max, 100);

		mTypedArray.recycle();
	}
	
	private void init() {
		if (centre == 0) {
			centre = getWidth() / 2; // 获取圆心的x坐标
			radius = centre - roundWidth * 1.0f / 2; // 圆环的半径
			oval = new RectF(); // 用于定义的圆弧的形状和大小的界限
			oval.left = centre - radius;
			oval.top = centre - radius;
			oval.right = centre + radius;
			oval.bottom = centre + radius;
		}
		if (fontMetrics == null) {
			textPaint = new Paint();
			textPaint.setTextSize(textSize);
			textPaint.setColor(textColor);
			textPaint.setAntiAlias(true); // 消除锯齿
			
			fontMetrics = textPaint.getFontMetrics();
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		init();
		clear();
		
		/**
		 * 画最外层的大圆环
		 */
		if (roundColor != Color.TRANSPARENT) {
			paint.setColor(roundColor); // 设置圆环的颜色
			paint.setStyle(Paint.Style.STROKE); // 设置空心
			paint.setStrokeWidth(roundWidth); // 设置圆环的宽度
			paint.setAntiAlias(true); // 消除锯齿
			canvas.drawCircle(centre, centre, radius, paint); // 画出圆环
		}

		/**
		 * 画圆弧 ，画圆环的进度
		 */
		paint.setStrokeWidth(roundWidth); // 设置圆环的宽度
		paint.setColor(roundProgressColor); // 设置进度的颜色
		paint.setStyle(Paint.Style.STROKE); // 设置空心
		paint.setAntiAlias(true); // 消除锯齿
		canvas.drawArc(oval, -90, 360 * progress / max, false, paint); // 根据进度画圆弧
		
		if (showText) {
			float textWidth = textPaint.measureText(progress + "%");
			float baseline = 0f + (getHeight() - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
			canvas.drawText(progress + "%", (getWidth() - textWidth) / 2, baseline, textPaint);
		}
		
	}
	
	private void clear() {
		if (needClear) {
			Canvas canvas = new Canvas();
			Paint paint = new Paint();
			paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
			canvas.drawPaint(paint);
			paint.setXfermode(new PorterDuffXfermode(Mode.SRC));
			invalidate();
		}
	}

	public int getMax() {
		return max;
	}

	/**
	 * 设置进度的最大值
	 * 
	 * @param max
	 */
	public void setMax(int max) {
		if (max < 0) {
			throw new IllegalArgumentException("max not less than 0");
		}
		this.max = max;
	}

	/**
	 * 获取进度.需要同步
	 * 
	 * @return
	 */
	public int getProgress() {
		return progress;
	}

	/**
	 * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步 刷新界面调用postInvalidate()能在非UI线程刷新
	 * 
	 * @param progress
	 */
	public void setProgress(int progress) {
		if (progress < 0) {
			throw new IllegalArgumentException("progress not less than 0");
		}
		if (progress > max) {
			progress = max;
		}
		if (progress < this.progress) {
			needClear = true;
		} else {
			needClear = false;
		}
		if (progress <= max) {
			this.progress = progress;
			postInvalidate();
		}
	}
	
	/**
	 * 设置进度并动画，此为线程安全控件，由于考虑多线的问题，需要同步 刷新界面调用postInvalidate()能在非UI线程刷新
	 * 
	 * @param progress
	 */
	public void setProgressAnim(int afterProgress, int duration) {
		if (afterProgress < 0) {
			throw new IllegalArgumentException("progress not less than 0");
		}
		if (afterProgress > max) {
			afterProgress = max;
		}
		if (afterProgress == progress) {
			return;
		}
		
		final int oldProgress = progress;
		final int newProgress = afterProgress;
		needClear = false;
		
		// 如果是进度回滚，不动画显示
		if (newProgress < oldProgress) {
			setProgress(newProgress);
			return;
		}
		
		CustomAnimation animation = new CustomAnimation(new CustomAnimCallBack() {
			@Override
			public void callBack(float interpolatedTime, Transformation t) {
				
				if (oldProgress < newProgress) {
					progress = oldProgress + (int) ((newProgress - oldProgress) * interpolatedTime);
				} else if (oldProgress > newProgress) {
					progress = oldProgress - (int) ((oldProgress - newProgress) * interpolatedTime);
				}
				postInvalidate();
			}
		});
		animation.setDuration(duration);
		animation.setInterpolator(new LinearInterpolator());
		clearAnimation();
		startAnimation(animation);
	}
	
	/**
	 * 设置进度并动画且隐藏，此为线程安全控件，由于考虑多线的问题，需要同步 刷新界面调用postInvalidate()能在非UI线程刷新
	 * 
	 * @param progress
	 */
	public void setProgressAndHide(int afterProgress, int duration, int endDuration) {
		if (afterProgress < 0) {
			throw new IllegalArgumentException("progress not less than 0");
		}
		if (afterProgress > max) {
			afterProgress = max;
		}
		if (afterProgress == progress) {
			return;
		}
		
		final int oldProgress = progress;
		final int newProgress = afterProgress;
		needClear = false;
		
		// 如果是进度回滚，不动画显示
		if (newProgress < oldProgress) {
			setProgress(newProgress);
			return;
		}
		
		CustomAnimation animation = new CustomAnimation(new CustomAnimCallBack() {
			@Override
			public void callBack(float interpolatedTime, Transformation t) {
				
				if (oldProgress < newProgress) {
					progress = oldProgress + (int) ((newProgress - oldProgress) * interpolatedTime);
				} else if (oldProgress > newProgress) {
					progress = oldProgress - (int) ((oldProgress - newProgress) * interpolatedTime);
				}
				postInvalidate();
			}
		});
		animation.setDuration(duration);
		animation.setInterpolator(new LinearInterpolator());
		
		AlphaAnimation alphaHide = new AlphaAnimation(1.0f, 0f);
		alphaHide.setDuration(endDuration);
		alphaHide.setFillAfter(true);
		
		AnimationSet animSet = new AnimationSet(true);
		
		animSet.addAnimation(animation);
		animSet.addAnimation(alphaHide);
		
		clearAnimation();
		startAnimation(animSet);
	}

	public int getCricleColor() {
		return roundColor;
	}

	public void setCricleColor(int cricleColor) {
		this.roundColor = cricleColor;
	}

	public int getCricleProgressColor() {
		return roundProgressColor;
	}

	public void setCricleProgressColor(int cricleProgressColor) {
		this.roundProgressColor = cricleProgressColor;
	}

	public float getRoundWidth() {
		return roundWidth;
	}

	public void setRoundWidth(float roundWidth) {
		this.roundWidth = roundWidth;
	}

	public boolean isShowText() {
		return showText;
	}

	public void setShowText(boolean showText) {
		this.showText = showText;
		postInvalidate();
	}

}