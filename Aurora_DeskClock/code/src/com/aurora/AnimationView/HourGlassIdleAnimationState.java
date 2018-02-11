package com.aurora.AnimationView;

import java.util.HashMap;
import java.util.Map;

import com.android.deskclock.R;
import com.aurora.AnimationView.AnimationState.OnHourGlassAnimationCompleteListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.os.Handler;
import android.util.Log;
public  class HourGlassIdleAnimationState extends AnimationStateBase {	

	
	public  HourGlassIdleAnimationState(Context context, Handler h,AuroraHourGlassView view) {
		super(context,h, view);
	}
	
	public  void onDraw(Canvas canvas) {
	}

	
	public void startAnimation(OnHourGlassAnimationCompleteListener listener) {
		mView.invalidate();
	}
	
	public void cancelAnimation() {
	}
	
    

}

