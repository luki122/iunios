package com.aurora.puremanager.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.aurora.puremanager.R;
//import com.nineoldandroids.animation.ValueAnimator;

import java.text.DecimalFormat;

public class RiseNumberTextView extends TextView implements IRiseNumber {

	private static final int STOPPED = 0;

	private static final int RUNNING = 1;

	private int mPlayingState = STOPPED;

	private float number;

	private float fromNumber;
	
	private long duration = 1500;
	private int numberType = 2;

	private DecimalFormat fnum;

	private EndListener mEndListener = null;

	final static int[] sizeTable = { 9, 99, 999, 9999, 99999, 999999, 9999999,
			99999999, 999999999, Integer.MAX_VALUE };

	public RiseNumberTextView(Context context) {
		super(context);
	}

	public RiseNumberTextView(Context context, AttributeSet attr) {
		super(context, attr);
//		setTextColor(context.getResources().getColor(R.color.white));
//		setTextSize(50);
	}

	public RiseNumberTextView(Context context, AttributeSet attr, int defStyle) {
		super(context, attr, defStyle);
	}
	
	public boolean isRunning() {
		return (mPlayingState == RUNNING);
	}
	
	private void runFloat() {
		ValueAnimator valueAnimator = ValueAnimator.ofFloat(fromNumber, number);
		valueAnimator.setDuration(duration);

		valueAnimator
				.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator valueAnimator) {

						setText(fnum.format(Float.parseFloat(valueAnimator
								.getAnimatedValue().toString())));
						if (valueAnimator.getAnimatedFraction() >= 1) {
							mPlayingState = STOPPED;
							if (mEndListener != null)
								mEndListener.onEndFinish();
						}
					}
				});
		valueAnimator.start();
	}

	private void runInt(int type) {
		ValueAnimator valueAnimator = null;
		if(type == 0){
			valueAnimator = ValueAnimator.ofInt((int) fromNumber,
					(int) number);
		}else{
			valueAnimator = ValueAnimator.ofInt((int) number, (int) 0);
		}
		valueAnimator.setDuration(duration);

		valueAnimator
				.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator valueAnimator) {
						setText(valueAnimator.getAnimatedValue().toString());
						if (valueAnimator.getAnimatedFraction() >= 1) {
							mPlayingState = STOPPED;
							if (mEndListener != null)
								mEndListener.onEndFinish();
						}
					}
				});
		valueAnimator.start();
	}

	static int sizeOfInt(int x) {
		for (int i = 0;; i++){
			if (x <= sizeTable[i])
				return i + 1;
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		fnum = new DecimalFormat("##0.00");
	}

	@Override
	public void start() {
		if (!isRunning()) {
			mPlayingState = RUNNING;
			if (numberType == 1){
				runInt(0);
			}else if(numberType == 2){
				runFloat();
			}else{
				runInt(1);
			}
		}
	}

	@Override
	public void withNumber(float number) {
		this.number = number;
		numberType = 2;
		fromNumber = 0;
	}

	@Override
	public void withNumber(int number) {
		this.number = number;
		numberType = 1;
		fromNumber = 0;
	}
	
	@Override
	public void clearNumber() {
		numberType = 3;
		fromNumber = 0;
	}
	
	@Override
	public void setDuration(long duration) {
		this.duration = duration;
	}

	@Override
	public void setOnEndListener(EndListener callback) {
		mEndListener = callback;
	}

	public interface EndListener {
		public void onEndFinish();
	}
}
