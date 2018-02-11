package com.netmanage.view;

import com.aurora.netmanage.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.ImageView;

public class CircleView extends ImageView {
	private static final Xfermode MASK_XFERMODE;
	private Bitmap mask;
	private Paint paint;
	private int circleWidth;

	static {
		PorterDuff.Mode localMode = PorterDuff.Mode.DST_OUT;
		MASK_XFERMODE = new PorterDuffXfermode(localMode);
	}

	public CircleView(Context paramContext) {
		super(paramContext);
		initView();
	}

	public CircleView(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
		initView();
	}

	public CircleView(Context paramContext, AttributeSet paramAttributeSet,
			int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
		initView();
	}
	
	private void initView(){	
		Resources resources = getContext().getResources();
		circleWidth = (int)(resources.getDimension(R.dimen.main_act_progress_view_size));
	}

	public Bitmap createMask() {
		int i = circleWidth;
		int j = circleWidth;
		Bitmap.Config localConfig = Bitmap.Config.ARGB_8888;
		Bitmap localBitmap = Bitmap.createBitmap(i, j, localConfig);
		Canvas localCanvas = new Canvas(localBitmap);
		Paint localPaint = new Paint(Paint.ANTI_ALIAS_FLAG);			
		float f1 = circleWidth;
		float f2 = circleWidth;
		RectF localRectF = new RectF(0.0F, 0.0F, f1, f2);
		localCanvas.drawOval(localRectF, localPaint);
		return localBitmap;		
	}	
	
	protected void onDraw(Canvas paramCanvas) {
		Drawable localDrawable = getDrawable();
		if (localDrawable == null)
			return;
		try {
			if (this.paint == null) {			
				this.paint = new Paint();
				
				this.paint.setFilterBitmap(false);
				this.paint.setXfermode(MASK_XFERMODE);
			}
			
			float f1 = getWidth();
			float f2 = getHeight();
			
			int i = paramCanvas.saveLayer(0.0F, 0.0F, f1, f2, null, Canvas.ALL_SAVE_FLAG);
			
			int j = getWidth();
			int k = getHeight();
			localDrawable.setBounds(0, 0, j, k);
			localDrawable.draw(paramCanvas);
			
			if ((this.mask == null) || (this.mask.isRecycled())) {
				Bitmap localBitmap1 = createMask();
				this.mask = localBitmap1;
			}
			
//			paramCanvas.drawBitmap(mask, 0.0F, 0.0F, paint);
			paramCanvas.drawBitmap(mask, (getWidth()-circleWidth)/2, (getWidth()-circleWidth)/2, paint);
			paramCanvas.restoreToCount(i);
			return;
		} catch (Exception localException) {
			StringBuilder localStringBuilder = new StringBuilder()
					.append("Attempting to draw with recycled bitmap. View ID = ");
			System.out.println("localStringBuilder==" + localStringBuilder);
		}
	}


}
