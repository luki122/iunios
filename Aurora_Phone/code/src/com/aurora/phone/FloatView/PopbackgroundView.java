package com.android.phone;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


public class PopbackgroundView extends View{
 
	private float mDensity = 3.0f;
	private float r;
	
	public PopbackgroundView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	    float density = context.getResources().getDisplayMetrics().density;
	  
    	mColor = Color.WHITE;
    	mPaint = new Paint();
    	mPaint.setAntiAlias(true);
 
    	mPaint.setColor(mColor);
    	
		mDensity = context.getResources().getDisplayMetrics().density;
		r =  24 * mDensity;
	}

	public PopbackgroundView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
    protected float mTranslationX = 0.0f;
	public void setCenterX(float x) {
		mTranslationX = x;
	}
    protected float mTranslationY = 0.0f;
	public void setCenterY(float y) {
		mTranslationY = y;
	}
	

    	
    	private float ww = 0;
    	public void setWw(float w) {
    		ww = w;
    	}
    	
    	public float getWw() {
    		return ww;
    	}
   

    
	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		  canvas.save(Canvas.MATRIX_SAVE_FLAG);
	        canvas.translate(mTranslationX, mTranslationY);	
	        canvas.drawCircle( -ww /2, 0,r, mPaint);
	        canvas.drawCircle( ww /2, 0, r, mPaint);
	        canvas.drawRect(-ww/2, -r, ww/2, r, mPaint);
	        canvas.restore();
	}
    
    private int mColor; 
    
    private Paint mPaint;
    
    
	
}
