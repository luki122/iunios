package com.android.phone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

public class SlideTextView extends View{



        String text="滑动接听";
        int distance;
        Bitmap bitmap_canvas1,bitmap_canvas2;
        Canvas canvas1,canvas2;
        Bitmap wenzi1,wenzi2;
        Paint paint;
        public boolean anim_start;
		@SuppressLint("DrawAllocation")
		
		
		Path p, p21,p212,p2, p22, p222, p2222, p31, p32, p3, p33, p333, p3333;
		int distanceBarrier;
			
		public SlideTextView(Context context, AttributeSet attrs) {
			super(context, attrs);
			// TODO Auto-generated constructor stub

			distance=-200;
			anim_start=false;
			bitmap_canvas1=Bitmap.createBitmap(500, 120, Bitmap.Config.ARGB_8888);
			canvas1=new Canvas(bitmap_canvas1);
			bitmap_canvas2=Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888);
			canvas2=new Canvas(bitmap_canvas2);
			wenzi1=BitmapFactory.decodeResource(context.getResources(), R.drawable.wenzi1);
			wenzi2=BitmapFactory.decodeResource(context.getResources(), R.drawable.wenzi2);
			paint=new Paint();
			BitmapShader bs=new BitmapShader(wenzi2,Shader.TileMode.CLAMP,Shader.TileMode.CLAMP);
			paint.setShader(bs);			
			p=new Path(); 						
 			p21=new Path(); 			
 			p212=new Path(); 			
 			p2=new Path(); 			
 			p22=new Path(); 			
 			p222=new Path(); 			
 			p2222=new Path();
 			p31=new Path(); 			
 			p32=new Path(); 			
 			p3=new Path();
 			p33=new Path(); 			
 			p333=new Path();
 			p3333=new Path(); 	
 			Resources res = PhoneGlobals.getInstance().getResources(); 
 			distanceBarrier = res.getInteger(R.integer.slide_textview_distance);
		}
		
		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			// TODO Auto-generated method stub
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}

		public void startAnim()
		{
			anim_start=true;
			this.invalidate();
		}
		
		public void stopAnim()
		{
			anim_start=false;
			this.invalidate();
		}
		@Override
		protected void onDraw(Canvas canvas) {
			// TODO Auto-generated method stub
			Paint paint1=new Paint();
			paint1.setAlpha(0xbb);
			canvas.drawBitmap(wenzi1, 0, 0, paint1);
         if(anim_start) {
             int left_top=50;
             int right_top=120;
             int left_bottom=-10;
             int right_bottom=60;
 			canvas.save();
 			p.rewind();
 			int height=this.getHeight();//old is 66
 			p.moveTo(left_top+distance, 0);
 			p.lineTo(right_top+distance, 0);
 			p.lineTo(right_bottom+distance, height);
 			p.lineTo(left_bottom+distance, height);
 			p.close();
 			
 			//p.addCircle(0+distance, 60, 60, Path.Direction.CW);
 			//canvas.clipPath(p);
 			//canvas.drawBitmap(wenzi2, 200, 200, null);
 			paint.setAlpha(0x7f);
 			canvas.drawPath(p, paint);
 						
 			p21.rewind();
 			p21.moveTo(left_top-10+distance, 0);
 			p21.lineTo(left_top+distance, 0);
 			p21.lineTo(left_bottom+distance, height);
 			p21.lineTo(left_bottom-10+distance, height);
 			p21.close();
 			paint.setAlpha(0x7f);
 			canvas.drawPath(p21, paint);
 			
 			p212.rewind();
 			p212.moveTo(left_top-20+distance, 0);
 			p212.lineTo(left_top-10+distance, 0);
 			p212.lineTo(left_bottom-10+distance, height);
 			p212.lineTo(left_bottom-20+distance, height);
 			p212.close();
 			paint.setAlpha(0x7f);
 			canvas.drawPath(p212, paint);
 			
 			p2.rewind();
 			p2.moveTo(left_top-30+distance, 0);
 			p2.lineTo(left_top-20+distance, 0);
 			p2.lineTo(left_bottom-20+distance, height);
 			p2.lineTo(left_bottom-30+distance, height);
 			p2.close();
 			paint.setAlpha(0x77);
 			canvas.drawPath(p2, paint);
 			
 			p22.rewind();
 			p22.moveTo(left_top-40+distance, 0);
 			p22.lineTo(left_top-30+distance, 0);
 			p22.lineTo(left_bottom-30+distance, height);
 			p22.lineTo(left_bottom-40+distance, height);
 			p22.close();
 			paint.setAlpha(0x55);
 			canvas.drawPath(p22, paint);
 			
 			p222.rewind();
 			p222.moveTo(left_top-50+distance, 0);
 			p222.lineTo(left_top-40+distance, 0);
 			p222.lineTo(left_bottom-40+distance, height);
 			p222.lineTo(left_bottom-50+distance, height);
 			p222.close();
 			paint.setAlpha(0x33);
 			canvas.drawPath(p222, paint);
 			
 			p2222.rewind();
 			p2222.moveTo(left_top-60+distance, 0);
 			p2222.lineTo(left_top-50+distance, 0);
 			p2222.lineTo(left_bottom-50+distance, height);
 			p2222.lineTo(left_bottom-60+distance, height);
 			p2222.close();
 			paint.setAlpha(0x11);
 			canvas.drawPath(p2222, paint);
 			
 			
 			p31.rewind();
 			p31.moveTo(right_top+distance, 0);
 			p31.lineTo(right_top+10+distance, 0);
 			p31.lineTo(right_bottom+10+distance, height);
 			p31.lineTo(right_bottom+distance, height);
 			p31.close();
 			paint.setAlpha(0x7f);
 			canvas.drawPath(p31, paint);
 			
 			p32.rewind();
 			p32.moveTo(right_top+10+distance, 0);
 			p32.lineTo(right_top+20+distance, 0);
 			p32.lineTo(right_bottom+20+distance, height);
 			p32.lineTo(right_bottom+10+distance, height);
 			p32.close();
 			paint.setAlpha(0x7f);
 			canvas.drawPath(p32, paint);
 			
 			p3.rewind();
 			p3.moveTo(right_top+20+distance, 0);
 			p3.lineTo(right_top+30+distance, 0);
 			p3.lineTo(right_bottom+30+distance, height);
 			p3.lineTo(right_bottom+20+distance, height);
 			p3.close();
 			paint.setAlpha(0x77);
 			canvas.drawPath(p3, paint);
 			
 			
 			
 			p33.rewind();
 			p33.moveTo(right_top+30+distance, 0);
 			p33.lineTo(right_top+40+distance, 0);
 			p33.lineTo(right_bottom+40+distance, height);
 			p33.lineTo(right_bottom+30+distance, height);
 			p33.close();
 			paint.setAlpha(0x55);
 			canvas.drawPath(p33, paint);
 			
 			p333.rewind();
 			p333.moveTo(right_top+40+distance, 0);
 			p333.lineTo(right_top+50+distance, 0);
 			p333.lineTo(right_bottom+50+distance, height);
 			p333.lineTo(right_bottom+40+distance, height);
 			p333.close();
 			paint.setAlpha(0x33);
 			canvas.drawPath(p333, paint);
 			
 			p3333.rewind();
 			p3333.moveTo(right_top+50+distance, 0);
 			p3333.lineTo(right_top+60+distance, 0);
 			p3333.lineTo(right_bottom+60+distance, height);
 			p3333.lineTo(right_bottom+50+distance, height);
 			p3333.close();
 			paint.setAlpha(0x11);
 			canvas.drawPath(p3333, paint);
 			
 			canvas.restore();

			distance+=5;		
			if(distance>distanceBarrier)
				distance=-200;
			this.invalidate();
			//this.postInvalidateDelayed(20);
         }
			super.onDraw(canvas);
			
		}
		
	    public void release()
	    {
	      if(wenzi1!=null&&(!wenzi1.isRecycled()))
	      {
	    	  wenzi1.recycle();
	    	  wenzi1=null;
	      }
	      if(wenzi2!=null&&(!wenzi2.isRecycled()))
	      {
	    	  wenzi2.recycle();
	    	  wenzi2=null;
	      }
	      if(bitmap_canvas1!=null&&(!bitmap_canvas1.isRecycled()))
	      {
	    	  bitmap_canvas1.recycle();
	    	  bitmap_canvas1=null;
	      }
	      if(bitmap_canvas2!=null&&(!bitmap_canvas2.isRecycled()))
	      {
	    	  bitmap_canvas2.recycle();
	    	  bitmap_canvas2=null;
	      }
	    	
	    }		
		
		
		

}
