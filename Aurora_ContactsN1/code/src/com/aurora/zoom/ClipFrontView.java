package com.aurora.zoom;

import android.content.Context;
import android.graphics.Path; 
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.android.contacts.R;
import com.aurora.zoom.activity.ZoomClipViewActivity;
import android.graphics.RectF;
import android.graphics.Rect;
import android.graphics.Region;

public class ClipFrontView extends View {
    private static final int SHADE_AREA_COLOR = 0xC0FFFFFF;
    // Gionee fangbin 20120627 modified for CR00627113 start
//    private static final int LIGHT_RECT_COLOR = 0xFF0AB6ED;
    private static final int LIGHT_RECT_COLOR = 0xFFFFFFFF;
    // Gionee fangbin 20120627 modified for CR00627113 end
    private final Paint mShadePaint = new Paint();
    private final Paint mLightPaint = new Paint();

    public ClipFrontView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mShadePaint.setColor(SHADE_AREA_COLOR);
        mLightPaint.setStyle(Style.STROKE);
        mLightPaint.setColor(LIGHT_RECT_COLOR);
        // Gionee fangbin 20120627 modified for CR00627113 start
        mLightPaint.setStrokeWidth(19.5f);
        // Gionee fangbin 20120627 modified for CR00627113 end
    }

    public ClipFrontView(Context context, AttributeSet attrs) {
       this(context, attrs, 0);
    }

    public ClipFrontView(Context context) {
        this(context, null);
    }
    
    private void drawShadeArea(Canvas canvas) {
        //aurora change liguangyu 20131102 for clip photo start
//        int gap = (ZoomClipViewActivity.WINDOW_HEIGHT-ZoomClipViewActivity.WINDOW_WIDTH)/2;
//        canvas.drawRect(0, 0, ZoomClipViewActivity.WINDOW_WIDTH, gap, mShadePaint);
//        // Gionee fangbin 20130402 modified for CR00790969 start
//        canvas.drawRect(0, ZoomClipViewActivity.WINDOW_HEIGHT-gap, 
//                ZoomClipViewActivity.WINDOW_WIDTH, ZoomClipViewActivity.WINDOW_HEIGHT, mShadePaint);
//        // Gionee fangbin 20121019 modified for CR00711730 start
//        canvas.drawRect(getResources().getDimensionPixelSize(R.dimen.gn_scale), gap, 
//                ZoomClipViewActivity.WINDOW_WIDTH-getResources().getDimensionPixelSize(R.dimen.gn_scale), 
//                ZoomClipViewActivity.WINDOW_HEIGHT-gap, mLightPaint);
//        // Gionee fangbin 20121019 modified for CR00711730 end
//        // Gionee fangbin 20130402 modified for CR00790969 end\
        final int windowHeight = ZoomClipViewActivity.WINDOW_HEIGHT  ;
        final int windowWidth =	ZoomClipViewActivity.WINDOW_WIDTH;
//        final int radius = windowWidth/2 - ZoomClipViewActivity.WINDOW_WIDTH/16;
        final int radius = ZoomClipViewActivity.mRadius;//501; // wangth 20140522 modify
//        final int ccy = ZoomClipViewActivity.WINDOW_HEIGHT /2 - ZoomClipViewActivity.WINDOW_HEIGHT/9;
        final int ccy = ZoomClipViewActivity.mCcy;//229*3;     // wangth 20140522 modify
        final int radiusWidth = ZoomClipViewActivity.mRadiusWidth;//9;     // yudingmin 20150505 modify
//        canvas.drawRect(0, 0, windowWidth, ccy - radius, mShadePaint);
//        canvas.drawRect(0, ccy + radius, windowWidth, windowHeight, mShadePaint);    	
//        Path p =  new Path();
//        p.moveTo(windowWidth/2, ccy - radius);
//        p.lineTo(0, ccy - radius);
//        p.lineTo(0, ccy + radius);
//        p.lineTo(windowWidth, ccy + radius);
//        p.lineTo(windowWidth, ccy - radius);
//        p.lineTo(windowWidth/2, ccy - radius);
//        p.addCircle(windowWidth/2, ccy, radius, Path.Direction.CW);
        canvas.drawRect(0, 0, windowWidth, ccy - radius, mShadePaint);
      canvas.drawRect(0, ccy + radius, windowWidth, windowHeight, mShadePaint);    	
      Path p =  new Path();
      p.moveTo(windowWidth/2, ccy - radius);
      p.lineTo(0, ccy - radius);
      p.lineTo(0, ccy + radius);
      p.lineTo(windowWidth, ccy + radius);
      p.lineTo(windowWidth, ccy - radius);
      p.lineTo(windowWidth/2, ccy - radius);
      p.addCircle(windowWidth/2, ccy, radius, Path.Direction.CW);
      canvas.drawCircle(windowWidth/2, ccy, radius - radiusWidth, mLightPaint);
        canvas.drawPath(p, mShadePaint);
       //aurora change liguangyu 20131102 for clip photo end

        
  
     }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        drawShadeArea(canvas);
    }
}
