package com.aurora.AnimationView;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.android.deskclock.R;
import com.aurora.AnimationView.AnimationState.OnHourGlassAnimationCompleteListener;
import com.aurora.AnimationView.AuroraAnimationDrawable.OnFrameAnimationCompleteListener;
import com.aurora.timer.TimerFragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
public  class HourGlassRunningAnimationState extends HourGlassRunningPauseAnimationState {	       
	
	public static final String TAG = "HourGlassRunningAnimationState";
	
	private ImageView water;
	TranslateAnimation waterAnimation;
	AnimationListener waterListener;
	AuroraAnimationDrawable mWaterAnimationDrawable;
	boolean mIsWaterAnim = false;
	int waterHeight,waterWidth;
	float scale;
	
	public  HourGlassRunningAnimationState(Context context, Handler h,AuroraHourGlassView view, ImageView view2) {
		super(context,h, view);
		water = view2;
		int resID;
		Bitmap waterBmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.water);	     
        waterHeight = waterBmp.getHeight();
        waterWidth = waterBmp.getWidth();
        if(waterBmp != null) {
        	waterBmp.recycle();
        	waterBmp = null;
        }
//		int resID = mContext.getResources().getIdentifier("hourglass_running", "drawable", "com.android.deskclock"); 
//	    b = BitmapFactory.decodeResource(mContext.getResources(), resID);	
		mWaterAnimationDrawable = new AuroraAnimationDrawable();
		String index;
		InputStream is;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inPreferredConfig = Bitmap.Config.RGB_565;
		opts.inPurgeable = true;
		opts.inInputShareable = true;
		opts.inSampleSize = 2;
		for (int i = 21; i <= 50; i += 3) {
			index = String.valueOf(i);			
			resID = context.getResources().getIdentifier("water_drop00" + index,
					"drawable", "com.android.deskclock");
			try {
				is = context.getResources().openRawResource(resID);
				mWaterAnimationDrawable.addFrame(Drawable.createFromResourceStream(context.getResources(), null, is, "src", opts), 12);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		resID = context.getResources().getIdentifier("water_drop_end","drawable", "com.android.deskclock");
		try {
			opts.outHeight = waterHeight;
			opts.outWidth = waterWidth;
			is = context.getResources().openRawResource(resID);
			mWaterAnimationDrawable.addFrame(Drawable.createFromResourceStream(context.getResources(), null, is, "src", opts), 12);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		waterListener = new Animation.AnimationListener() {
			public void onAnimationStart(Animation animation) {
				Log.v(TAG, " onAnimationStart"); 
				mIsWaterAnim = true;
				int delta2 = (int)(1.75*delta);
		       	water.setTranslationY(height/2+delta2 +(int)(RADIUS-H) - waterHeight/2 + 12);
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {	
				Log.v(TAG, " onAnimationEnd"); 
				int delta2 = (int)(1.75*delta);
		       	water.setTranslationY(height/2+delta2 +(int)(RADIUS-H) - waterHeight/2 + mContext.getResources().getInteger(R.integer.time_water_translation));
		        //aurora add liguangyu 20140331 for bug #3453 start
				mWaterAnimationDrawable.stop();
				//aurora add liguangyu 20140331 for bug #3453 end
				water.setBackgroundDrawable(mWaterAnimationDrawable);
				mWaterAnimationDrawable.start(new OnFrameAnimationCompleteListener() {
					public void onFrameAnimationComplete() {
						mIsWaterAnim = false;
						if(mListener!= null) {
							mListener.onHourGlassAnimationComplete();
						}
					}
				});	
			}
		};
	}
	
	public  void onDraw(Canvas canvas) {
		super.onDraw(canvas);
//		com.android.deskclock.Log.i("HourGlassRunningAnimationState ondraw"); 
// 		mView.oval.set(width/2-delta/4, (int)(height/2-1.75*delta), width/2+delta/4, (int)(height-1.5*delta-6));
//       	canvas.drawRect(mView.oval, mView.mRoundPaint);
	}
		
	public void startAnimation(OnHourGlassAnimationCompleteListener listener) {
		Log.v(TAG, " startAnimation");
		if(mView.time < 1000 * 30) {
			scale = 4;
		} else if(mView.time <= 1000 * 60) {
			scale = 2;
		} else if(mView.time > 60 * 1000 * 60) {
			scale = 0.5f;
		} else {
			scale = 1;
		}
		mListener = listener;
		mIsWaterAnim = false;
		mHandler.removeCallbacks(mRunnable);
		mHandler.post(mRunnable);
	}
	
	public void cancelAnimation() {
		Log.v(TAG, " cancelAnimation");	
		try {
			waterAnimation.setAnimationListener(null);	
			waterAnimation.cancel();
			mWaterAnimationDrawable.stop();
			water.setBackgroundDrawable(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mHandler.removeCallbacks(mRunnable);
		mIsWaterAnim = false;
	}
	
    Runnable mRunnable = new Runnable() {
		public void run() {			
			if(!mIsWaterAnim) {				
				waterAnimation=new TranslateAnimation(0,0, -(int)(4.5*delta+RADIUS-H), -(int)1.75*delta);
				double duration = mContext.getResources().getInteger(R.integer.common_anim_duration)/scale;
				duration = Math.sqrt((RADIUS-H)/RADIUS) * duration;
				waterAnimation.setDuration((int)duration);
				waterAnimation.setInterpolator(new AccelerateInterpolator());
				waterAnimation.setAnimationListener(waterListener);	
				water.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.water));
				water.startAnimation(waterAnimation);
			}
			mHandler.postDelayed(this, 125);
		}
    };
}

