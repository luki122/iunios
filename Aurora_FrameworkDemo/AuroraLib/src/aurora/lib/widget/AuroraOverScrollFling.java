package aurora.lib.widget;

import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.aurora.lib.R;

public class AuroraOverScrollFling
{
	protected final static int INVALIDATE = -1;
	
	protected ListView view = null;
	
	protected int MotionX = INVALIDATE;
	
	protected int MotionY = INVALIDATE;
	
	protected int DownY = INVALIDATE;
	
	protected int ActivePointerId = INVALIDATE;
	
	protected int LastY = INVALIDATE;
	
	protected int LastX = INVALIDATE;
	
	protected int auroraOverScrollY;
	
	protected final static int AURORA_DIRECTION_DOWN = 0;
	
	protected final static int AURORA_DIRECTION_UP = 1;
	
	AuroraDecelerateInterpolator auroraInterpolator;
	
	protected static final int AURORA_FLING_ANIM_TIME = 500;
	
	protected static final int AURORA_TOUCH_SLOP = 100;
	
	protected ObjectAnimator auroraOverFingAnim;
	
	protected boolean auroraStartMove = false;
	
	protected MotionEvent auroraMotionEvent;
	
	public void onTouchEvent(MotionEvent event)
	{
		int action = event.getAction() & MotionEvent.ACTION_MASK;
		
		int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
		
		int pointerCount = event.getPointerCount();
		
		auroraMotionEvent = event;
		
		auroraStopOverFingAnim();
		
		switch(action)
		{
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				
				ActivePointerId = event.getPointerId(pointerIndex);
				
				MotionX = (int) event.getX(pointerIndex);
				
				MotionY = (int) event.getY(pointerIndex);
				
				LastY = MotionY;
				
				LastX = MotionX;
				
				DownY = MotionY;
				
				if(action == MotionEvent.ACTION_DOWN)
				{
					auroraStartMove = false;
					
					auroraHandleActionDown(MotionX,MotionY);
				}
				
				if(action == MotionEvent.ACTION_POINTER_DOWN)
				{
					auroraHandleActionPointerDown(MotionX,MotionY);
				}
				break;
		
			case MotionEvent.ACTION_MOVE:
				
				int index = event.findPointerIndex(ActivePointerId);
				
				if(index == -1)
				{
					index = 0;
					
					ActivePointerId = event.getPointerId(index);
				}
				
				MotionX = (int) event.getX(index);
				
				MotionY = (int) event.getY(index);
				
				if(DownY == INVALIDATE)DownY = MotionY;
				
				auroraHandleActionMove(MotionX,MotionY);
				
				LastY = MotionY;
				
				LastX = MotionX;
				
				break;
				
			case MotionEvent.ACTION_UP:
				
				MotionX = (int) event.getX();
				
				MotionY = (int) event.getY();
				
				auroraHandleActionUp(MotionX,MotionY);
				
				DownY = INVALIDATE;
	
				LastY = INVALIDATE;
				
				LastX = INVALIDATE;
				
				ActivePointerId = INVALIDATE;
				
				auroraStartMove = false;
				
				break;
			case MotionEvent.ACTION_POINTER_UP:		
				
				if(DownY == INVALIDATE)break;
				
				auroraOnSecondaryPointerUp(event);
				
				auroraHandleActionPointerUp(MotionX,MotionY);
				
				LastY = MotionY;
				
				LastX = MotionX;
				break;
			case MotionEvent.ACTION_CANCEL:
				auroraHandleActionCancel(MotionX, MotionY);
				break;	
		}
		
	}
	
	protected void auroraHandleActionCancel(int motionX2, int motionY2) {
		// TODO Auto-generated method stub
		
	}
	
	protected void auroraHandleActionPointerUp(int motionX2, int motionY2) {
		// TODO Auto-generated method stub
		
	}

	protected void auroraHandleActionUp(int motionX2, int motionY2) {
		// TODO Auto-generated method stub
		
	}

	protected void auroraHandleActionMove(int motionX2, int motionY2) {
		// TODO Auto-generated method stub
		
	}

	protected void auroraHandleActionPointerDown(int motionX2, int motionY2) {
		// TODO Auto-generated method stub
		
	}

	protected void auroraHandleActionDown(int motionX2, int motionY2) {
		// TODO Auto-generated method stub
		
	}

	private void auroraOnSecondaryPointerUp(MotionEvent event)
	{
		 final int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
		 
		 final int pointerId = event.getPointerId(pointerIndex);
		 
		 if (pointerId == ActivePointerId) 
		 {
			 // This was our active pointer going up. Choose a new
			 // active pointer and adjust accordingly.
	 
			 final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
			 
			 MotionX = (int) event.getX(newPointerIndex);
			 
			 MotionY = (int) event.getY(newPointerIndex);
			 
			 ActivePointerId = event.getPointerId(newPointerIndex);
		 }
	}
	
	public void auroraPlayOverScrollAnim(View v)
	{
		if(v == null)return;
		
		ObjectAnimator anim = ObjectAnimator.ofInt(v, "ScrollY", auroraOverScrollY , 0);
		
		anim.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// TODO Auto-generated method stub
				auroraOverScrollY = (Integer) animation.getAnimatedValue();
			}
		});
		
		if(auroraInterpolator == null)
			auroraInterpolator = new AuroraDecelerateInterpolator();
		
		auroraInterpolator.auroraReset();
		
		anim.setInterpolator(auroraInterpolator);
		
		anim.setDuration(AURORA_FLING_ANIM_TIME);
		
		anim.addListener(auroraFlingAnimListener);
		
		auroraOverFingAnim = anim;
		
		anim.start();
	}
	
	public void auroraStopOverFingAnim()
	{
		if(auroraOverFingAnim != null && auroraOverFingAnim.isRunning())
		{
			auroraOverFingAnim.cancel();
		}		
	}
	
	protected int auroraGetDirection(int y)
	{
		return (y - LastY) > 0 ? AURORA_DIRECTION_DOWN : AURORA_DIRECTION_UP;
	}
	
	protected boolean auroraIsFirstItemTopVisible()
	{
		return false;
	}
	
	protected boolean auroraIsLastItemBottomVisible()
	{
		return false;
	}
	
	protected void auroraSetOverScrollY(int y)
	{
		if(!auroraStartMove)
		{
			if(Math.abs(y-DownY) > AURORA_TOUCH_SLOP)
			{
				LastY = DownY = y;
				
				auroraStartMove = true;
			}
			else
			{
				return;
			}
			
		}
		
		int direction = auroraGetDirection(y);
		
		switch(direction)
		{
		case AURORA_DIRECTION_DOWN:
			
			if(auroraIsFirstItemTopVisible())
			{
				auroraOverScrollY -= (y - LastY) * (float)Math.pow((double)0.9985,   250-(double)auroraOverScrollY);
			}
			else if(auroraOverScrollY > 0)
			{
				auroraOverScrollY -= (y - LastY);
				
				if(auroraOverScrollY <= 0)auroraOverScrollY = 0;
			}
			break;
		case AURORA_DIRECTION_UP:
			
			if(auroraOverScrollY < 0 )
			{
				auroraOverScrollY -= (y - LastY);
				
				if(auroraOverScrollY >= 0)auroraOverScrollY = 0;
			}
			else if(auroraIsLastItemBottomVisible())
			{
				auroraOverScrollY -= (y - LastY) * (float)Math.pow((double)0.9985,   250 + (double)auroraOverScrollY);
			}
			
			break;
		}
	}
	
	AnimatorListener auroraFlingAnimListener = new AnimatorListener(){

			@Override
			public void onAnimationCancel(android.animation.Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			//when anim is end,we should reset content bg.
			@Override
			public void onAnimationEnd(android.animation.Animator animation) {
				// TODO Auto-generated method stub
				auroraOverFlingEndListener();
			}

			@Override
			public void onAnimationRepeat(android.animation.Animator animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(android.animation.Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
	}; 
	
	protected void auroraOverFlingEndListener()
	{
		
	}
	
	public static class AuroraDecelerateInterpolator implements Interpolator
	{
		float result = 0;
		float lastResut = 0;
		float Index = 1f;
		float ratio = 4.5f;
		float times = 1;
		
		float mFactor = 1.0f;
		
		public AuroraDecelerateInterpolator()
		{
			mFactor = 1.0f;
		}
		
		public AuroraDecelerateInterpolator(float factor)
		{
			mFactor = factor;
		}
		
		@Override
		public float getInterpolation(float input) 
		{
			result = lastResut + Index/ratio;
			
			ratio += times * mFactor;
			
			times++;
		
			if(result >= 1.0f || input >= 1.0f) result =1.0f;
			
			lastResut = result;
			
			return result;
		}
		
		public void auroraReset()
		{
			result = 0;
			lastResut = 0;
			Index = 1f;
			ratio = 4.5f;
			times = 1;
		}
	}
	
	
	public void auroraSetScrollY(int scrollY)
	{
		auroraOverScrollY = scrollY;
	}
}
