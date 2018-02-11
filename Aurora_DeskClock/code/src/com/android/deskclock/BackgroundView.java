package com.android.deskclock;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class BackgroundView extends View{
    Bitmap bg,bg_reject;
    int width,height;
    int draw_index;
    private int MAX=300;
//    Paint paint;
    boolean is_anim_start;
    private int res_id;
	public BackgroundView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		bg=BitmapFactory.decodeResource(context.getResources(), R.drawable.alertback);
		bg_reject=BitmapFactory.decodeResource(context.getResources(), R.drawable.alertclose_bg);
		width=bg.getWidth();
		height=bg.getHeight();
		draw_index=0;
		
		is_anim_start=false;
		res_id=R.drawable.alertback;
	}
	public BackgroundView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public void setBackgroundId(int resId)
	{
		res_id=resId;
		this.invalidate();
	}
	
	@Override
	public void setBackgroundResource(int resid) {
		// TODO Auto-generated method stub
		res_id=resid;
		this.invalidate();
		//super.setBackgroundResource(resid);
	}
	public void startDisplayAnim()
	{
		is_anim_start=true;
		this.invalidate();
	}
	
	public boolean isAnimStart()
	{
		return is_anim_start;
	}
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		if(is_anim_start)
		{
			Paint paint=new Paint();
			paint.setAntiAlias(true);
			canvas.drawBitmap(bg, 0, 0, null);
			int index=draw_index%20;
			for(int i=0;i<MAX;i++)
			{
				int alpha=255*(i+index*MAX/20)/MAX;
				if(alpha>255)
					alpha=255;
				paint.setAlpha(alpha);
				canvas.save();
//				float angle=360f/MAX;
//				Path p=new Path();
//				p.addArc(new RectF(-(height-width)/2,0,(height+width)/2,height) , angle, 2*angle);
//				canvas.clipPath(p);
				canvas.clipRect(new Rect(0,height*i/MAX,width,height*(i+1)/MAX));
                
				
				canvas.drawBitmap(bg_reject, 0, 0, paint);
				canvas.restore();
			}
			draw_index++;
			if(draw_index>=20)
			{
				draw_index=0;
				is_anim_start=false;
				res_id=R.drawable.alertclose_bg;
			}
			this.invalidate();
		}
		else
		{
			
			if(res_id==R.drawable.alertback)
			{
				canvas.drawBitmap(bg, 0, 0, null);
			}
			else
			{
				canvas.drawBitmap(bg_reject, 0, 0, null);
			}
		}
		super.onDraw(canvas);
	}
    public void release()
    {
      if(bg!=null&&(!bg.isRecycled()))
      {
    	bg.recycle();
    	bg=null;
      }
      if(bg_reject!=null&&(!bg_reject.isRecycled()))
      {
    	  bg_reject.recycle();
    	  bg_reject=null;
      }
    	
    }
	
}
