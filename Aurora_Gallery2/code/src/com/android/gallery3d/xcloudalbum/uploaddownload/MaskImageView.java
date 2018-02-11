package com.android.gallery3d.xcloudalbum.uploaddownload;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class MaskImageView extends ImageView {
	
	public MaskImageView(Context context) {
		super(context);
	}
	
	public MaskImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public MaskImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	private boolean mHasMask;

	public void setHasMask(boolean hasMask) {
		setWillNotDraw (false);
		mHasMask = hasMask;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		
		if(mHasMask) {
			//Drawable drawable = getDrawable();
			//Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
			
			//Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
			//Canvas canvas = new Canvas(result);
	        //Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	        //设置两张图片相交时的模式 
	        //paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
			//canvas.drawBitmap(bitmap, 0, 0, null);
			canvas.drawARGB(204, 255, 255, 255);
	        //paint.setXfermode(null);
	        //setImageBitmap(result);
	        //setScaleType(ScaleType.CENTER); 
		}
	}
	
	
}
