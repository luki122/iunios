package com.aurora.AnimationView;

import java.util.HashMap;
import java.util.Map;

import com.android.deskclock.R;
import com.aurora.AnimationView.AnimationState.OnHourGlassAnimationCompleteListener;
import com.aurora.timer.TimerFragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import 	android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
public  class HourGlassRunningPauseAnimationState extends AnimationStateBase {	       
	
  	double H ;
	Bitmap backgound,b;
	private Path path =  new Path();
   int width,height,delta;
	 public double hh;
	    public double RADIUS ,r;
	    Rect rect;
  	
	public  HourGlassRunningPauseAnimationState(Context context, Handler h,AuroraHourGlassView view) {
		super(context,h, view);
	    int resID = mContext.getResources().getIdentifier("hourglass_backgroud", "drawable", "com.android.deskclock"); 
	    backgound = BitmapFactory.decodeResource(mContext.getResources(), resID); 
		resID = mContext.getResources().getIdentifier("hourglass_running_pause", "drawable", "com.android.deskclock"); 
	    b = BitmapFactory.decodeResource(mContext.getResources(), resID);	
        width = b.getWidth(); 
        height = b.getHeight();
        delta = width/20;      
		RADIUS = (height - delta * 6.5)/2;
        r = RADIUS/Math.sqrt(2);
        hh = RADIUS -r;    
        rect = new Rect();
	}
	
	public  void onDraw(Canvas canvas) {
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
				| Paint.FILTER_BITMAP_FLAG));
    	   

        double p = (double)mView.timeLeft/mView.time;
//		com.android.deskclock.Log.i("HourGlassRunningPauseAnimationState ondraw mView.timeLeft = " + mView.timeLeft + " mView.time =" + mView.time + " p=" + p); 
        if(mView.timeLeft<=0) {
        	H =  RADIUS;
        } else {
         if(p>mView.mVolumeRatio) { 
         	H = hh*Math.pow((1-p)*(mView.v1+mView.v2)/mView.v1, 1/3.0);
         	if(H>hh) {
         		H=hh;
         	}
         } else  {
         	H = RADIUS - r*Math.pow(p*(mView.v1+mView.v2)/mView.v2, 1/3.0);   
         }
        }
             
         canvas.drawBitmap(backgound, 0, 0, null); 
        int delta2 = (int)(1.75*delta);
        int delta3 = (int)(1.5*delta);
        if(H != RADIUS) {
	        canvas.save();
	    	rect.set(0, delta3 + (int)H,width, height/2-delta2);
	    	canvas.clipRect(rect);
	        canvas.drawBitmap(b, 0, 0, null);   
	        canvas.restore();
        }
        canvas.save();
    	rect.set(0, height/2+delta2 +(int)(RADIUS-H),width,height-delta3 + 3);
    	canvas.clipRect(rect);
        canvas.drawBitmap(b, 0, 0, null);   
        canvas.restore();
	}
	

    

}

