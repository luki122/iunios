package com.android.phone;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.OnGestureListener;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.internal.telephony.PhoneConstants;

import com.android.phone.GlowPadView.OnTriggerListener;

public class SlideView2 extends LinearLayout implements OnGestureListener{
	
    private static final String TAG = "SlideView2";
	
    private static SharedPreferences mSP = null;
	private OnTriggerListener onlistener;
	private GestureDetector gd; 
	private View incomingcall;
	private ImageView text_fuhao;
	private Animation animation;
	private boolean is_scroll;
	private boolean is_flying;
	private boolean is_not_need_flying;
	private float downx;
	private SlideTextView text;
	
	
	private int  distanceBarrier = 450;
	private int  distanceBarrier2 = 400;
	
	public SlideView2(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	public SlideView2(Context context, AttributeSet attrs) {
		super(context, attrs);
		gd=new GestureDetector(this);
		is_scroll=false;
		is_flying=false;
		is_not_need_flying=false;
        mSP = PhoneGlobals.getInstance().getApplicationContext()
                .getSharedPreferences("com.android.phone_preferences", Context.MODE_PRIVATE); 
        
        distanceBarrier = context.getResources().getDimensionPixelOffset(R.dimen.slideview_distance);  
        distanceBarrier2 = context.getResources().getDimensionPixelOffset(R.dimen.slideview_distance2);
		// TODO Auto-generated constructor stub
	}
	public SlideView2(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		incomingcall=findViewById(R.id.incomingcall);
		text_fuhao=(ImageView)findViewById(R.id.text_fuhao);
		text=(SlideTextView)findViewById(R.id.huadongjieting);
		animation=android.view.animation.AnimationUtils.loadAnimation(this.getContext(), R.anim.huadong);
	}

    private BackgroundView groundview;
    private InCallScreen mInCallScreen;

	public void setInCallScreen(InCallScreen c) {
		mInCallScreen = c;
		if (c != null) {
			groundview = (BackgroundView) c.findViewById(R.id.background);
		}
	}
	
	public void setOnTriggerListener(OnTriggerListener o)
	{
		onlistener=o;
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
	    boolean defIncomingTouchSwitch = this.getResources().getBoolean(R.bool.aurora_def_incoming_touch_switch);
	    boolean touchSwitch = mSP != null && mSP.getBoolean("aurora_incoming_touch_switch", defIncomingTouchSwitch);
		if(touchSwitch && !PreventTouchManager.mIsTouchEnable) {
			Log.v(TAG, "onTouchEvent return PreventTouchManager");
			return true;
		}
//		if(!PhoneGlobals.getInstance().notifier.isRingingTouchOn) {
//			Log.v(TAG, "onTouchEvent return isRingingTouchOn");
//			return true;
//		}				
		Log.v(TAG, "onTouchEvent");
		if(mInCallScreen.mCallCardAnimController.getIncoming_to_incall_anim_ex())
		{
			Log.v(TAG, "onTouchEvent return 111");
			return true;
		}
		if((event.getAction()==MotionEvent.ACTION_UP||event.getAction()==MotionEvent.ACTION_DOWN)&&is_not_need_flying)
		{
			Log.v(TAG, "onTouchEvent return 1");
			is_not_need_flying=false;
			return false;
		}
		if(is_not_need_flying) {
			Log.v(TAG, "onTouchEvent return 2");
			return true;
		}
		boolean touch=gd.onTouchEvent(event);
		if(event.getAction()==MotionEvent.ACTION_UP)
		{
			if(is_scroll)
			{
				mInCallScreen.mCallCardAnimController.ResetIncallCardViewAnimationManDongZuo();
				mInCallScreen.mInCallTouchUiAnimController.ResetAnimationRight();
//				createAndStartAnimationManDongZuo(0);
				text_fuhao.startAnimation(animation);
				text.startAnim();
				is_scroll=false;
				Log.v(TAG, "onTouchEvent return 3");
			    return false;
			}
					     if (groundview.mEdgeGlowBottom != null) {
		    	 groundview.mEdgeGlowBottom.onRelease();
		     }
		}
		return touch;//super.onTouchEvent(event);
	}
	public void Release()
	{
		is_scroll=false;
		is_flying=false;
		is_not_need_flying=false;		
		
	}
	
	public void setCantouch(boolean is_can)
	{
		
		is_not_need_flying=!is_can;
		Log.v("SlideView", "setCantouch is_not_need_flying="+is_not_need_flying);
	}
	
	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		Log.v("SlideView", "onDown");
		is_flying=false;
		is_scroll=false;
		is_not_need_flying=false;
		downx=e.getX();
		return true;
	}
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		Log.v("SlideView", "onFling");
		
		if(PhoneGlobals.getInstance().mCM.getState() == PhoneConstants.State.IDLE) {
			Log.v(TAG, "onFling state IDLE");
			return false;
		}
		if(is_not_need_flying) {
			Log.v(TAG, "onFling return 1");
			return false;
		}
		//aurora add liguangyu 20140725 for BUG #6828 start
		if(e1 == null) {
			Log.v(TAG, "onFling return 2");
			return false;
		}
		//aurora add liguangyu 20140725 for BUG #6828 end
		if(e1.getActionIndex() != e2.getActionIndex()) {
			Log.v(TAG, "onFling return 3");
			return false;
		}

		float x1=e1.getX();
		float y1=e1.getY();
		float x2=e2.getX();
		float y2=e2.getY();
		Log.v(TAG, "onFling x1 = " + x1 + " y1 = " +y1 + " x2 = " + x2 + " y2 = " +y2);
		is_flying=true;
		
		if((Math.abs(x1-x2)>2*Math.abs(y1-y2))&&(x1-x2)<-distanceBarrier)
		{
			is_scroll=false;
			onlistener.onTrigger(null,0);			
		}
		else if(2*Math.abs(x1-x2)<Math.abs(y1-y2))
		{
			if(y2-y1>distanceBarrier)
			{
				is_scroll=false;
				onlistener.onTrigger(null,1);
				mInCallScreen.mCallCardAnimController.ResetIncallCardViewAnimationManDongZuo2();
				mInCallScreen.mInCallTouchUiAnimController.ResetAnimationRight();
			}
			else if(y1-y2>distanceBarrier2)
			{
				is_scroll=false;
				onlistener.onTrigger(null,2);
				mInCallScreen.mCallCardAnimController.ResetIncallCardViewAnimationManDongZuo2();
				mInCallScreen.mInCallTouchUiAnimController.ResetAnimationRight();
			}
		}
/*		if((x1-x2)<-200)
		{
			is_scroll=false;
			onlistener.onTrigger(null,0);
			
		}
		else if(y2-y1>200)
		{
			is_scroll=false;
			onlistener.onTrigger(null,1);
		}
		else if(y1-y2>200)
		{
			is_scroll=false;
			onlistener.onTrigger(null,2);
		}*/
		return false;
	}
	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		Log.v("SlideView", "onLongPress");
	}
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		Log.v("SlideView", "onScroll");
		//aurora add liguangyu 20140725 for BUG #6828 start
		if(e1 == null) {
			return false;
		}
		//aurora add liguangyu 20140725 for BUG #6828 end
		float x1=e1.getX();
		float y1=e1.getY();
		float x2=e2.getX();
		float y2=e2.getY();
		if((Math.abs(x1-x2)>2*Math.abs(y1-y2))&&!is_flying)
		{
		  if((downx-x2)>-distanceBarrier)	
		  {
			int per=(int)(Math.abs(downx-x2)*200/distanceBarrier);
			if((downx-x2)>=0)
				         per=0;
			mInCallScreen.mCallCardAnimController.setIncallCardViewAnimationManDongZuo(per);
			//createAndStartAnimationManDongZuo(per);
			mInCallScreen.mInCallTouchUiAnimController.ManDongzuoAnimationRight(per);
			is_scroll=true;
		  }
		  else
		  {
			  is_not_need_flying=true;
			  onlistener.onTrigger(null,0);
		  }
		}

		if(y1 - y2 > distanceBarrier2) {
//            if (!groundview.mEdgeGlowBottom.isFinished()) {
//            	groundview.mEdgeGlowBottom.onRelease();
//            }
//			groundview.invalidateEdgeEffect();
		} else if(y1 - y2 > 30 && !is_scroll) {
			groundview.mEdgeGlowBottom.onPull(Math.abs(y1 - y2)/distanceBarrier2);
			groundview.invalidateEdgeEffect();
		}
		return false;
	}
	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		Log.v("SlideView", "onShowPress");
	}
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		Log.v("SlideView", "onSingleTapUp");
		return false;
	}	
	
	public boolean dispatchTouchEvent (MotionEvent event) {
		Log.v(TAG, "event.x=" + event.getX() + " event.y=" + event.getY());
		return super.dispatchTouchEvent(event);
	}
}
