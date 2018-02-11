package com.aurora.launcher;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.AvoidXfermode.Mode;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GlsurfaceMask extends SurfaceView implements SurfaceHolder.Callback{
	
	private SurfaceHolder surfaceHolder;
	
	public GlsurfaceMask(Context context, AttributeSet attrs){
		super(context,attrs);
		init();
	}
	
	
	public GlsurfaceMask(Context context) {
		super(context);
		init();
	}
	
	private void init(){
		surfaceHolder=getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.dispatchDraw(canvas);
	}


	@Override
	public void draw(Canvas canvas) {
		// TODO Auto-generated method stub
		canvas.drawARGB(1, 0, 0, 1);
		super.draw(canvas);
	}


	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		//Log.e("linp", "-----------------------------------surfaceCreated");	
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		//Log.e("linp", "-----------------------------------surfaceDestroyed");	
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {}
}
