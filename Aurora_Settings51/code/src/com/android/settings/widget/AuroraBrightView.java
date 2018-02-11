package com.android.settings.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.android.settings.R;

public class AuroraBrightView extends View{
	
	private Bitmap [] brightBitmaps;
	private Bitmap mDrawBit;
	private int [] brightSources = new int []{
			R.drawable.aurora_brightness00,
			R.drawable.aurora_brightness01,
			R.drawable.aurora_brightness02,
			R.drawable.aurora_brightness03,
			R.drawable.aurora_brightness04,
			R.drawable.aurora_brightness05,
			R.drawable.aurora_brightness06,
			R.drawable.aurora_brightness07,
			R.drawable.aurora_brightness08,
			R.drawable.aurora_brightness09,
			R.drawable.aurora_brightness10,
			R.drawable.aurora_brightness11,
			R.drawable.aurora_brightness12,
			R.drawable.aurora_brightness13,
			R.drawable.aurora_brightness14,
			R.drawable.aurora_brightness15,
			R.drawable.aurora_brightness16,
			R.drawable.aurora_brightness17,
			R.drawable.aurora_brightness18,
			R.drawable.aurora_brightness19,
			R.drawable.aurora_brightness20,
			R.drawable.aurora_brightness21,
			R.drawable.aurora_brightness22,
			R.drawable.aurora_brightness23,
			};

	public AuroraBrightView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public AuroraBrightView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		brightBitmaps = new Bitmap [24];
		for(int i =0;i< brightSources.length;i++){
			brightBitmaps[i]= BitmapFactory.decodeResource(getResources(),brightSources[i]);
		}
		mDrawBit = brightBitmaps[0];
	}

	public AuroraBrightView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public void rotate(int progress){
		mDrawBit = brightBitmaps[progress%24];
		invalidate();
	}
	public void rest(){
		mDrawBit = brightBitmaps[0];
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		if(mDrawBit!=null){
			canvas.drawBitmap(mDrawBit, 0, 0, null);
		}
		
	}
	
	
	

}
