package com.aurora.apihook.touch;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.content.Context;

import android.os.SystemProperties;

import android.content.res.Configuration;
import android.view.WindowManager;
import android.graphics.Point;

public class AuroraThreeFingerScreenShotPolicy {

	private AuroraITakeScreenShot mScreenShot;

	private static final float PRECENT_OF_SCREEN_FOR_DISTANCE = 0.25f;

	private static final int FINGER_COUNT = 3;

	private float distance = 360f;// need to change for diff screen size

	private Handler mHandler = new Handler();

	private ConsumeState mConsumeState = new ConsumeState();

	private Runnable mTakeScreenShot = new Runnable() {

		@Override
		public void run() {
			android.util.Log.e("xiuyong", "start screenshot");
			mScreenShot.takeScreenShot();
		}
	};

	float[] startPYS = { -1, -1, -1 };
	float[] endPYS = { -1, -1, -1 };
	boolean isFirst = true;
	boolean mShouldTake = false;
	int mPointCount = 0;
	boolean isComeIn = false;
	
	private void resetPointState(){
		startPYS[0] = startPYS[1] = startPYS[2] = -1;
		endPYS[0] = endPYS[1] = endPYS[2] = -1;
		isFirst = true;
		isComeIn = false;
	}

	public AuroraThreeFingerScreenShotPolicy(Context mContext){
		mScreenShot = new AuroraScreenShotHelper(mContext);
		mConfiguration = mContext.getResources().getConfiguration();
		initDistance(mContext);
	}

	public boolean handleEvent(MotionEvent event) {

		int action = event.getActionMasked();

		// android.util.Log.e("haha", "Same Object? " + event);
		//android.util.Log.e("haha", "onTouchEvent:  " + event.getPointerCount());
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			/*
			 * int fingers = event.getPointerCount(); if (fingers !=
			 * FINGER_COUNT) return false; downPYS[0] =
			 * event.getY(0); downPYS[1] = event.getY(1); downPYS[2]
			 * = event.getY(2);
			 */
			android.util.Log.e("xiuyong", "ACTION_DOWN:  " + event.getPointerCount());
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			mPointCount = event.getPointerCount();
			if(mPointCount == FINGER_COUNT){
				android.util.Log.e("xiuyong", " ACTION_POINTER_DOWN:  Three Points");
				isComeIn = true;
			}
			break;

		case MotionEvent.ACTION_MOVE:
			//android.util.Log.e("haha", "ACTION_MOVE:  ");
			if (!mShouldTake && isComeIn) {
				if (event.getPointerCount() == FINGER_COUNT) {
					if (isFirst) {
						startPYS[0] = event.getY(0);
						startPYS[1] = event.getY(1);
						startPYS[2] = event.getY(2);
						mConsumeState.update(true);
						//android.util.Log.e("haha", "start point: " + startPYS[0] + " "+ startPYS[1] + " "+ startPYS[2]);
						isFirst = false;
					} else {
						endPYS[0] = event.getY(0);
						endPYS[1] = event.getY(1);
						endPYS[2] = event.getY(2);
						//android.util.Log.e("haha", "start point: " + endPYS[0] + " "+ endPYS[1] + " "+ endPYS[2]);
					}
				} else {
					if(!isFirst){
						mShouldTake = isTakeScreenShot(startPYS, endPYS);
					}
					resetPointState();
				}
			}

			break;
		case MotionEvent.ACTION_UP:
			android.util.Log.e("xiuyong", " ACTION_up=> isComeIn:  " + isComeIn + "  isFirst:  " + isFirst + "  mShouldTake:  " + mShouldTake);
			if (mShouldTake) {
				takeScreenShot();
			}
			// Aurora <Steve.Tang> 2015-02-26 resolve red-mi note can not snap screen.start
			// as three point up, red-mi three point dispear at same time and do not come in ACTION_MOVE. 
			// usually, point disapear at different time.(e.g IUNI U3)
			else {
				if(!isFirst){
					mShouldTake = isTakeScreenShot(startPYS, endPYS);
				}
				if (mShouldTake) {
					takeScreenShot();
				} 
			}
			// Aurora <Steve.Tang> 2015-02-26 reslove red-mi note can not snap screen.end
			resetPointState();
			mShouldTake = false;
			mConsumeState.update(false);
			break;
		case MotionEvent.ACTION_CANCEL:
			resetPointState();
			mShouldTake = false;
			mConsumeState.update(false);
			android.util.Log.e("xiuyong", " ACTION_Cancel: ");
			break;
		}

		return true;
	}

	private void takeScreenShot() {
		mHandler.removeCallbacks(mTakeScreenShot);
		//mHandler.postDelayed(mTakeScreenShot, 100);
		mHandler.post(mTakeScreenShot);
	}

	private boolean isTakeScreenShot(float[] startPYs, float[] endPYs) {
		int direction = 0;// 0 means down, 1 means up
		distance = getDistance();
android.util.Log.e("xiuyong", "the distance is: " + distance);
		if (startPYs.length != FINGER_COUNT
				|| endPYs.length != FINGER_COUNT
				|| startPYs[0] == -1 || endPYs[0] == -1)
			return false;
		for (int i = 0; i < FINGER_COUNT; i++) {
			if ((i == 0) && (startPYs[i] > endPYs[i])) {
				direction = 1;
			}
			if (direction == 0) {
				if (endPYs[i] - startPYs[i] > distance)
					continue;
				else
					return false;
			} else {
				if (startPYs[i] - endPYs[i] > distance)
					continue;
				else
					return false;
			}
		}
		return true;
	}

    private class ConsumeState {
        private boolean mConsumed;

        public ConsumeState() {
            mConsumed = false;
        }

        public void update(boolean newState){
            mConsumed = newState;
            if (mConsumed)
                SystemProperties.set("sys.aurora.input.intercept", "1");
            else
                SystemProperties.set("sys.aurora.input.intercept", "0");

            android.util.Log.e("xiuyong", "update() " + mConsumed + "  " + SystemProperties.getBoolean("sys.aurora.input.intercept", false));
        }

        public boolean getState() {
            return mConsumed;
        }
    }


	private int distanceLand = 200;
	private int distancePort = 400;
	private Configuration mConfiguration ;

	private void initDistance(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Point p = new Point();
		wm.getDefaultDisplay().getSize(p);
		int ori = mConfiguration.orientation;
		if (ori == Configuration.ORIENTATION_PORTRAIT) {
			distancePort = p.y / 6;
			distanceLand = p.x / 6;
		} else {
			distancePort = p.x / 6;
			distanceLand = p.y / 6;
		}
		android.util.Log.e("xiuyong", "the distancePort is: " + distancePort + "   the distanceLand is: " + distanceLand);
	}
	
	private int getDistance(){
		int ori = mConfiguration.orientation;
		if (ori == Configuration.ORIENTATION_PORTRAIT) {
			return distancePort;
		} else {
			return distanceLand;
		}
	}
}
