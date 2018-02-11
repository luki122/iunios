package com.android.settings.bootanimation;

import com.android.settings.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

/**
 * 头像图片选择框的浮层
 * @author hanpingjiang 2014.04.11
 *
 */
public class IUNIDrawable extends Drawable{

	private Context mContext;
	private int screenWidth;
	private int screenHeight;
	private Paint mLinePaint=new Paint();
	private Path path = new Path();
	{
	    mLinePaint.setARGB(200, 50, 50, 50);
	    mLinePaint.setStrokeWidth(1F);
	    mLinePaint.setStyle(Paint.Style.STROKE);
	    mLinePaint.setAntiAlias(true);
	    mLinePaint.setColor(Color.WHITE);
	}
	public IUNIDrawable(Context context) {
		super();
		this.mContext=context;
		
		DisplayMetrics dm = new DisplayMetrics();
		dm = mContext.getResources().getDisplayMetrics();
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;
	}
	
	public Path drawPath() {
        
        path.reset();
        path.moveTo(0, 0);
        path.lineTo(screenWidth, 0);
        path.lineTo(screenWidth, screenHeight);
        path.lineTo(0, screenHeight);
        path.lineTo(0, 0);
//		
//        //第一个I
//        path.moveTo(0, 0);
//        path.lineTo(63, 0);
//        path.lineTo(63, 315);
//        path.lineTo(0, 315);
//        path.lineTo(0, 0);
//        
//        //第一个U
//        path.moveTo(105, 0);
//        path.lineTo(105, 226);
//
//        RectF rectB = new RectF(105, 131, 294, 321);
//        path.arcTo(rectB, -180, -180);
//        
//        path.lineTo(294, 0);
//        path.lineTo(231, 0);
//        path.lineTo(231, 220);
//        
//        RectF rectS = new RectF(168, 201, 231, 264);
//        path.arcTo(rectS, 0, 180);
//        
//        path.lineTo(168, 0);
//        path.lineTo(105, 0);
//        
//        //第二个N
//        path.moveTo(336, 315);
//        path.lineTo(336, 89);
//
//        RectF rectBT = new RectF(336, -6, 525, 153);
//        path.arcTo(rectBT, -180, 180);
//        
//        path.lineTo(525, 315);
//        path.lineTo(462, 315);
//        path.lineTo(462, 89);
//        
//        RectF rectST = new RectF(399, 52, 462, 105);
//        path.arcTo(rectST, 0, -180);
//        
//        path.lineTo(399, 315);
//        path.lineTo(336, 315);
//        
//        //第二个I
//        path.moveTo(567, 0);
//        path.lineTo(630, 0);
//        path.lineTo(630, 315);
//        path.lineTo(567, 315);
//        path.lineTo(567, 0);
		return path;
	}
	
	@Override
	public void setBounds(Rect bounds) {
		super.setBounds(new Rect(
				bounds.left, 
				bounds.top, 
				bounds.right, 
				bounds.bottom));
	}
	
	@Override
	public void draw(Canvas canvas) {
		// TODO Auto-generated method stub
		Paint paint = mLinePaint;
//		int xoffset = 219;
//        int yoffset = 459;

        canvas.save();
//        canvas.translate(xoffset, yoffset);
        
        path.reset();
        
        path.moveTo(0, 0);
        path.lineTo(screenWidth, 0);
        path.lineTo(screenWidth, screenHeight);
        path.lineTo(0, screenHeight);
        path.lineTo(0, 0);
//        //第一个I
//        path.moveTo(0, 0);
//        path.lineTo(63, 0);
//        path.lineTo(63, 315);
//        path.lineTo(0, 315);
//        path.lineTo(0, 0);
//        
//        //第一个U
//        path.moveTo(105, 0);
//        path.lineTo(105, 226);
//
//        RectF rectB = new RectF(105, 131, 294, 321);
//        path.arcTo(rectB, -180, -180);
//        
//        path.lineTo(294, 0);
//        path.lineTo(231, 0);
//        path.lineTo(231, 220);
//        
//        RectF rectS = new RectF(168, 201, 231, 264);
//        path.arcTo(rectS, 0, 180);
//        
//        path.lineTo(168, 0);
//        path.lineTo(105, 0);
//        
//        //第二个N
//        path.moveTo(336, 315);
//        path.lineTo(336, 89);
//
//        RectF rectBT = new RectF(336, -6, 525, 153);
//        path.arcTo(rectBT, -180, 180);
//        
//        path.lineTo(525, 315);
//        path.lineTo(462, 315);
//        path.lineTo(462, 89);
//        
//        RectF rectST = new RectF(399, 52, 462, 105);
//        path.arcTo(rectST, 0, -180);
//        
//        path.lineTo(399, 315);
//        path.lineTo(336, 315);
//        
//        //第二个I
//        path.moveTo(567, 0);
//        path.lineTo(630, 0);
//        path.lineTo(630, 315);
//        path.lineTo(567, 315);
//        path.lineTo(567, 0);
        
        canvas.drawPath(path, paint);
	}

	@Override
	public int getOpacity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAlpha(int alpha) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		// TODO Auto-generated method stub
		
	}

}
