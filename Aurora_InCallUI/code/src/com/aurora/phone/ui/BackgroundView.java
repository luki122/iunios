package com.android.incallui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import java.io.File;

public class BackgroundView extends View{
    Bitmap bg,bg_reject;
    int width,height;
    int draw_index;
    private int MAX=300;
    private Paint mBgPaint;
    boolean is_anim_start;
    boolean is_anim_start2;
    private int res_id;
    private boolean mIsTop = false;
    static int MAX_INDEX = 20;
	Paint mPaint = new Paint();
    
    
    Rect src = null;
    RectF dst;
    
    public EdgeEffect mEdgeGlowBottom;
	public BackgroundView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	    float density = context.getResources().getDisplayMetrics().density;
	    if(density > 3) {
//	    if(Build.VERSION.SDK_INT >= 19) {
			bg=BitmapFactory.decodeResource(context.getResources(), R.drawable.full_bg);
			bg_reject=BitmapFactory.decodeResource(context.getResources(), R.drawable.full_reject);
	    } else {
			bg=BitmapFactory.decodeResource(context.getResources(), R.drawable.bg);
			bg_reject=BitmapFactory.decodeResource(context.getResources(), R.drawable.reject_bg);	
	    }
		width=bg.getWidth();
		height=bg.getHeight();
		draw_index=0;
		
		is_anim_start=false;
		res_id=R.drawable.bg;
        mEdgeGlowBottom = new EdgeEffect(context);
        
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels; 
        if(!DeviceUtils.hasKey() && !DeviceUtils.is8910()) {
        	screenHeight += 145;
        }
        dst = new RectF(0, 0, screenWidth, screenHeight);
        
        if(Build.VERSION.SDK_INT >= 19 && !DeviceUtils.is8910()) {
        	height = screenHeight;
        }
        
    	mBgColor = InCallApp.getInstance().getResources().getColor(R.color.aurora_phonebg_color);
    	mBgPaint = new Paint();
    	mBgPaint.setColor(mBgColor);
    	
    	File file = new File("system/theme/lady/Phone.apk");    
    	mIsBlueLady = file.exists();
    	
    	mPaint.setAntiAlias(true);
	}

	public BackgroundView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public void setBackgroundId(int resId) {
		res_id = resId;
		this.invalidate();
	}

	@Override
	public void setBackgroundResource(int resid) {
		// TODO Auto-generated method stub
		res_id = resid;
		this.invalidate();
		// super.setBackgroundResource(resid);
	}

	public void startDisplayAnim() {
		is_anim_start = true;
		this.invalidate();
	}

	public boolean isAnimStart() {
		return is_anim_start;
	}
	
	public void startDisplayAnim2(int index) {
		draw_index = index;
		is_anim_start2 = true;
		this.invalidate();
	}
	
	public void stopDisplayAnim2() {
		draw_index = 0;
		is_anim_start2 = false;
		this.invalidate();
	}
		

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		if(is_anim_start2) {
			if(draw_index == 20) {
				auroraDrawBitmap(canvas, bg_reject);
			} else {
				auroraDrawBitmap(canvas, bg);
				int index = draw_index % MAX_INDEX;
				for (int i = 0; i < MAX; i++) {
//					int alpha = 255 * (i + index * MAX / MAX_INDEX) / MAX;
					int alpha =  255 * index / MAX_INDEX;
					if (alpha > 255)
						alpha = 255;
					mPaint.setAlpha(alpha);
					canvas.save();
					canvas.clipRect(new Rect(0, height * i / MAX, width, height
							* (i + 1) / MAX));

					auroraDrawBitmap(canvas, bg_reject, mPaint);
					canvas.restore();
				}
			}
		} else if (is_anim_start) {

			auroraDrawBitmap(canvas, bg);
			int index = draw_index % MAX_INDEX;
			for (int i = 0; i < MAX; i++) {
				int alpha = 255 * (i + index * MAX / MAX_INDEX) / MAX;
				if (alpha > 255)
					alpha = 255;
				mPaint.setAlpha(alpha);
				canvas.save();
				canvas.clipRect(new Rect(0, height * i / MAX, width, height
						* (i + 1) / MAX));

				auroraDrawBitmap(canvas, bg_reject, mPaint);
				canvas.restore();
			}
			draw_index++;
			if (draw_index >= MAX_INDEX) {
				draw_index = 0;
				is_anim_start = false;
				res_id = R.drawable.reject_bg;
			}
			this.invalidate();
		} else {
			auroraDrawBitmap(canvas, res_id == R.drawable.bg ? bg : bg_reject);
		}
		super.onDraw(canvas);
	}
	
	public void release() {
		if (bg != null && (!bg.isRecycled())) {
			bg.recycle();
			bg = null;
		}
		if (bg_reject != null && (!bg_reject.isRecycled())) {
			bg_reject.recycle();
			bg_reject = null;
		}

	}
    
    public void invalidateEdgeEffect(){
    	if(mIsTop) {
    		invalidate(new Rect(0, 0 , width, mEdgeGlowBottom.getMaxHeight()));	
    	} else {
        	invalidate(new Rect(0, height - mEdgeGlowBottom.getMaxHeight() , width, height));
    	} 	
    }
    
    private int mScrollY = 0;
    public void setScrollY(float f) {
    	mScrollY = (int)f;
    }
    
	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		if (mEdgeGlowBottom != null) {
			// final int scrollY = mScrollY;
			if (!mEdgeGlowBottom.isFinished()) {
				Log.i("BackgroundView", "draw");
				final int restoreCount = canvas.save();
				if (!mIsTop) {
					canvas.translate(-width, height);
					canvas.rotate(180, width, 0);
				}
				mEdgeGlowBottom.setSize(width, height);
				if (mEdgeGlowBottom.draw(canvas)) {
					// Account for the rotation
					// mEdgeGlowBottom.setPosition(0, height - 800);
					invalidateEdgeEffect();
				}
				canvas.restoreToCount(restoreCount);
			}
		}
	}
    
    private void auroraDrawBitmap(Canvas canvas, Bitmap bg) {
    	if(res_id == R.drawable.bg && mIsBlueLady) {
    		auroraDrawRect(canvas);
    	} else {
        	auroraDrawBitmap(canvas, bg, null);    		
    	}
    }
    
    private void auroraDrawBitmap(Canvas canvas, Bitmap bg, Paint paint) {
        if(DeviceUtils.is8910()) {
    		canvas.drawBitmap(bg, 0, 0, paint);
        } else if(Build.VERSION.SDK_INT >= 19) {
    		canvas.drawBitmap(bg, src, dst, paint);
    	} else {
    		canvas.drawBitmap(bg, 0, 0, paint);
    	}
    }
    
    private int mBgColor; 
    private void auroraDrawRect(Canvas canvas) {
		canvas.drawRect(dst, mBgPaint);
    }
    
    private boolean mIsBlueLady;
    
	
}
