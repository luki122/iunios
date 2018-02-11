/**
 * Vulcan created this file in 2015年3月17日 下午2:26:54 .
 */
package com.android.phase1.cinema;

import android.view.MotionEvent;
import android.view.VelocityTracker;

/**
 * Vulcan created FingerTracker in 2015年3月17日 .
 * 
 */
public class FingerTracker {
	
	public static final float DISTANCE_FAREST = 250f;
	public static final float VELOCITY_LIMIT = 10000;
	
	private boolean mTouched = false;
	private float mStartY = 0f;
	private VelocityTracker mVelocityTracker = null;
	private int mPointerId;

	/**
	 * 
	 */
	public FingerTracker() {
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月17日 下午2:31:45 .
	 * @param y
	 */
	public void startTracking(MotionEvent e) {
		mTouched = true;
		
		mStartY = e.getRawY();

		if(mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		
		mPointerId = e.getPointerId(0);
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月23日 下午5:49:50 .
	 * @param y
	 */
	public void updateY(float y) {
		mStartY = y;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月17日 下午2:34:27 .
	 * @param y
	 * @return
	 */
	public float getDistance(float y) {
		float distance = y - mStartY;
		distance = (float)((int)(distance * 30f));
		distance = distance / 30f;
		return distance;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月23日 下午2:11:15 .
	 * @return
	 */
	public float getStartY() {
		return mStartY;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月17日 下午4:59:46 .
	 * @param e
	 */
	public void addMovement(MotionEvent e) {
		if(mVelocityTracker != null) {
			mVelocityTracker.addMovement(e);
		}
		
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月17日 下午4:58:51 .
	 * @return
	 */
	public float computeCurrentYVelocity() {
		if(mVelocityTracker == null) {
			return 0;
		}
		mVelocityTracker.computeCurrentVelocity(1000);
		return mVelocityTracker.getYVelocity(mPointerId);
	}
	
	/*
	 * 
	 */
	public void stopTracking() {
		mTouched = false;
		
		if(mVelocityTracker != null) {
			mVelocityTracker.clear();
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月18日 下午2:45:11 .
	 * @return
	 */
	public boolean isTouched() {
		return mTouched;
	}

}
