package com.secure.view;

import com.secure.utils.Utils;
import com.aurora.secure.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

/**
 * 主界面中的圆圈view
 * @author chengrq
 *
 */
public class MainCircleView extends View {
	private RectF circleRect;
	private RectF canvasRect;
	private final int spaceAngles = 90;
	private float StrokeWidth;//64;//画笔空心边框的宽度 
	private float diameter;//656;//直径
	private final float startAngleOfUseApp = 135;
	private final float startAngleOfSysApp = 45;	
	private float userAppTotalAngles;
	private float sysAppTotalAngles;
	private float curSweepAngleOfUseApp;
	private float curSweepAngleOfSysApp;
	
	private Bitmap tmpBmp;
	private Canvas canvas;
	private Paint paint;
	private Paint mPaint;
	
	public MainCircleView(final Context context, AttributeSet attrs) {
		super(context, attrs);	
		mPaint = new Paint();
		StrokeWidth = context.getResources().getDimension(R.dimen.main_circle_view_stroke_width);
		diameter = context.getResources().getDimension(R.dimen.main_circle_view_diameter);
	}
	
	public void init(int userAppNum,int sysAppNum){
		canvasRect = new RectF(0,0,diameter,diameter);
		circleRect = new RectF(StrokeWidth/2,StrokeWidth/2,
				canvasRect.width()-StrokeWidth/2,canvasRect.width()-StrokeWidth/2);
		userAppTotalAngles = (360-spaceAngles)*userAppNum/(userAppNum+sysAppNum);
		sysAppTotalAngles = (360-spaceAngles)-(userAppTotalAngles-1);//减1的目的是为防止出现空隙
	}
	
	public void updateViewWhenAppNumChange(int userAppNum,int sysAppNum){
		Log.i("zhangwei", "start");
		if(userAppNum+sysAppNum == 0){
			return ;
		}
		userAppTotalAngles = (360-spaceAngles)*userAppNum/(userAppNum+sysAppNum);
		sysAppTotalAngles = (360-spaceAngles)-(userAppTotalAngles-1);//减1的目的是为防止出现空隙
		curSweepAngleOfUseApp = userAppTotalAngles;
		curSweepAngleOfSysApp = -sysAppTotalAngles;
		Log.i("zhangwei", "the userAppTotalAngles="+userAppTotalAngles+" the sysAppTotalAngles="+sysAppTotalAngles);
		Log.i("zhangwei", "the curSweepAngleOfUseApp="+curSweepAngleOfUseApp+" the curSweepAngleOfSysApp="+curSweepAngleOfSysApp);
		postInvalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Bitmap drawBitmap = createProessBitmap();
		if(drawBitmap != null){
			canvas.drawBitmap(drawBitmap,0,0,mPaint);
		}		
	}
		
	@Override
	protected void onDetachedFromWindow() {
		if(tmpBmp != null){
			tmpBmp.recycle();
			tmpBmp = null;
		}
		canvas = null;
		paint = null;
		super.onDetachedFromWindow();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		if(!changed){
			return ;
		}
		super.onLayout(changed, left, top, right, bottom);
	}
		
	public void updateAnimView(float animScale){
		curSweepAngleOfUseApp = userAppTotalAngles*animScale;
		curSweepAngleOfSysApp = -sysAppTotalAngles*animScale;
		Log.i("zhangwei", "the userAppTotalAngles="+userAppTotalAngles+" the sysAppTotalAngles="+sysAppTotalAngles);
		Log.i("zhangwei", "the curSweepAngleOfUseApp="+curSweepAngleOfUseApp+" the curSweepAngleOfSysApp="+curSweepAngleOfSysApp);

		postInvalidate();
	}
      	
	 /**
	  * 生成圆弧形bitmap
	  * @return
	  */	
	 private Bitmap createProessBitmap(){ 
		 if(canvasRect == null){
			 return null;
		 }
		 if(tmpBmp == null || tmpBmp.isRecycled()){
			 tmpBmp = Bitmap.createBitmap(
					   (int)(canvasRect.right-canvasRect.left), 
					   (int)(canvasRect.bottom-canvasRect.top),
					   Config.ARGB_8888);		   
			 canvas = new Canvas(tmpBmp); 
			 paint = new Paint();
			 
			 paint.setStyle(Paint.Style.STROKE); // 设置画笔设为空心
		     paint.setStrokeWidth(StrokeWidth); // 设置画笔空心边框的宽度 
		     paint.setAntiAlias(true);// 取消锯齿 
		 }
	          
	     paint.setColor(Color.parseColor("#00ce9b"));	 
	     canvas.drawArc(circleRect, startAngleOfUseApp, curSweepAngleOfUseApp, false, paint);  
	     
	     paint.setColor(Color.parseColor("#59bdef"));	 
	     canvas.drawArc(circleRect, startAngleOfSysApp,curSweepAngleOfSysApp, false, paint); 
	     
	   return tmpBmp;
	}
}
