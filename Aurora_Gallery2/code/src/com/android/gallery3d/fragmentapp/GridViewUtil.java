package com.android.gallery3d.fragmentapp;

import com.android.gallery3d.fragmentutil.RecyclingImageView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

public class GridViewUtil {

	private final int LINE_THICKNESS = 15;
	
	/** 
	 * * 根据手机的分辨率从 dp 的单位 转成为 px(像素) 
	 *  */ 
	public static int dip2px(Context context, float dpValue) {  
		final float scale = context.getResources().getDisplayMetrics().density;  
		return (int) (dpValue * scale + 0.5f);  
	}  
	
	/** 
	 *  * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 
	 *  */ 
	public static int px2dip(Context context, float pxValue) {  
		final float scale = context.getResources().getDisplayMetrics().density;  
		return (int) (pxValue / scale + 0.5f);  
	}
	
	/** 
	* 将px值转换为sp值，保证文字大小不变 
	*  
	* @param pxValue 
	* @param fontScale 
	*            （DisplayMetrics类中属性scaledDensity） 
	* @return 
	*/  
		public static int px2sp(Context context, float pxValue) {  
			final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;  
			return (int) (pxValue / fontScale + 0.5f);  
		}  
		
		/** 
		 * 将sp值转换为px值，保证文字大小不变 
		 *  
		 * @param spValue 
		 * @param fontScale 
		 *            （DisplayMetrics类中属性scaledDensity） 
		 * @return 
		 */  
		public static int sp2px(Context context, float spValue) {  
			final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;  
			return (int) (spValue * fontScale + 0.5f);  
		}  
	
	public Bitmap getBitmapFromView(View v) {
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas (bitmap);
        v.draw(canvas);
        return bitmap;
    }
	
	private Bitmap getBitmapWithBorder(View v) {
        Bitmap bitmap = getBitmapFromView(v);
        Canvas can = new Canvas(bitmap);

        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(LINE_THICKNESS);
        paint.setColor(Color.BLACK);

        can.drawBitmap(bitmap, 0, 0, null);
        can.drawRect(rect, paint);

        return bitmap;
    }
}
