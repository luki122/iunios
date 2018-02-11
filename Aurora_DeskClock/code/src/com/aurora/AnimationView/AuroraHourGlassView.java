package com.aurora.AnimationView;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.deskclock.R;
import com.aurora.utils.DensityUtil;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import 	android.graphics.Region.Op;

public class AuroraHourGlassView extends ImageView {
    public final static int COUNT = 104;
    
   
    public float ptsDraw[] = new float[2*COUNT];
    public float RADIUS, BigRadius = 578/2;
    public  RectF oval;  
    public  int mMainColor = Color.GREEN;
    public int mStep = 10 ;
	 Matrix panRotate;

    
    public float rotateCenterX, rotateCenterY;
     public final Paint mLightPaint = new Paint();
     public final Paint mDarkPaint = new Paint();
     public final Paint mRoundPaint = new Paint();
     public final Paint mRoundLinePaint = new Paint();
     public final Paint mRoundClipPaint = new Paint();
	
	 int mLightRunningPoints =0;
	 public  long timeLeft;
	 public long time;
	 public double mVolumeRatio ,h,v1,v2;
	 public void setTotalTime(long value){
		time = value;
	}
	 public void setTimeleft(long value){
		timeLeft = value;
		this.invalidate();
	}
	 public long getTimeleft(){
		return timeLeft;
	}
	public void setLightRunningPoints(int number) {
		this.mLightRunningPoints = number;
		 this.invalidate();
	}
	 public void setPtsDraw(float[] ptsValues) {
	     System.arraycopy(ptsValues, 0, ptsDraw, 0, 2*COUNT);
	 }
	
	 
		AnimationState mAnimState;
		public void setAnimState(AnimationState state) {
//			Exception e =new Exception ("lgy");
//			e.printStackTrace();
			if(mAnimState!=null && !mAnimState.equals(state)) {
				mAnimState.cancelAnimation();
			}
			mAnimState = state;
			
		}
		public AnimationState getAnimState() {
			return mAnimState;
			
		}
	
		boolean showAnim = true;
		public void setShowAnim(boolean value) {
			showAnim = value;
		}
	
	public AuroraHourGlassView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.hourglass_view); 
		BigRadius = a.getInt(R.styleable.hourglass_view_big_radius, 0);
		
		
		BigRadius=	DensityUtil.dip2px(getContext(),93 );
		a.recycle(); 
		
		
		mMainColor=context.getResources().getColor(R.color.aurora_chronometer_color);
		panRotate = new Matrix();
		RADIUS = 210;
		rotateCenterX = BigRadius;
		rotateCenterY = BigRadius;
		panRotate.setTranslate(rotateCenterX, rotateCenterY);
        mLightPaint.setStyle(Style.FILL);
        mLightPaint.setColor(mMainColor);
        mLightPaint.setAntiAlias(true);
        mDarkPaint.setColor(getResources().getColor(R.color.aurora_chronometer_dark_color));
        mDarkPaint.setAntiAlias(true);
        mDarkPaint.setStyle(Style.FILL);
        mRoundPaint.setStyle(Style.FILL);
        mRoundPaint.setColor(mMainColor);
        mRoundLinePaint.setStyle(Style.STROKE);
        mRoundLinePaint.setColor(mMainColor);
        mRoundClipPaint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT)); 	
    	double angle =  Math.PI / 2;
     	double r = RADIUS/Math.sqrt(2);
    	angle += Math.PI / 2 / 26;
    	ptsDraw[0] = 0; 
    	ptsDraw[1] = BigRadius;
    	ptsDraw[52] = (-1) * BigRadius; 
    	ptsDraw[53] = 0;
    	ptsDraw[104] = 0; 
    	ptsDraw[105] = (-1) * BigRadius;
    	ptsDraw[156] = BigRadius; 
    	ptsDraw[157] = 0;
    	
        for (int i = 2 ; i <= 2 * 25;i+=2) {
        	ptsDraw[i] = (float)(BigRadius * Math.cos(angle)); 
        	ptsDraw[i+1] = (float)(BigRadius * Math.sin(angle));
        	ptsDraw[104-i] = ptsDraw[i]; 
        	ptsDraw[105-i] = (-1) * ptsDraw[i+1];
        	ptsDraw[104+i] = (-1) * ptsDraw[i]; 
        	ptsDraw[105+i] = (-1) * ptsDraw[i+1];
        	ptsDraw[208-i] = (-1) * ptsDraw[i]; 
        	ptsDraw[209-i] = ptsDraw[i+1];
	    	angle += Math.PI / 2 / 26;
        }
    	oval = new RectF(0,0,0,0);
    	
        h = RADIUS -r;    	
        v2 = 2*Math.PI*Math.pow(r, 3)/3;
        v1 = Math.PI*(3*RADIUS - h)*Math.pow(h, 2)/3;
        mVolumeRatio = v2/(v1+v2);
	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(mAnimState!=null && showAnim) {
			mAnimState.onDraw(canvas);
		}
    }
	
}
