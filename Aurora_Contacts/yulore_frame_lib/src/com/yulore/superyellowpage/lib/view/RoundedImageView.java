package com.yulore.superyellowpage.lib.view;


import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.yulore.superyellowpage.lib.R;


/**
 * An ImageView that allows a pixel corner radius
 * to be specified. The image's corners will be rounded
 * to this radius, with transparent behind the image.
 *
 * <b>WARNING:</b> This is only supported for bitmap
 * image sources. If other types of drawable are set
 * as the image source, the corners <b>will not be
 * rounded.</b>
 * @author alex
 *
 */
public class RoundedImageView extends ImageView {

	private float mCornerRadius;

	public RoundedImageView(Context context) {
		super(context);
	}

	public RoundedImageView(Context context, AttributeSet attributes) {
		super(context, attributes);
		Resources rsc = context.getResources();
		int cornerRadius = (int) rsc.getDisplayMetrics().density * rsc.getDimensionPixelSize(R.dimen.yulore_superyellowpage_list_logo_size) / 14;
		TypedArray array = context.obtainStyledAttributes(attributes, R.styleable.RoundedImageView);
		if (array != null) {
			mCornerRadius = array.getDimension(R.styleable.RoundedImageView_corner_radius, cornerRadius);
//			Log.e("RoundedImageView", "mCornerRadius = "+mCornerRadius);
			array.recycle();
		}
	}

	/**
	 * Sets the corner radius for rounded image corners in
	 * absolutely display pixels.
	 * @param cornerRadius
	 */
	public void setCornerRadius(float cornerRadius) {
		mCornerRadius = cornerRadius;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// Round some corners betch!
		Drawable maiDrawable = getDrawable();
		if (maiDrawable instanceof BitmapDrawable && mCornerRadius > 0) {
			Paint paint = ((BitmapDrawable) maiDrawable).getPaint();
	        final int color = 0xff000000;
	        Rect bitmapBounds = maiDrawable.getBounds();
	        final RectF rectF = new RectF(bitmapBounds);
	        // Create an off-screen bitmap to the PorterDuff alpha blending to work right
			int saveCount = canvas.saveLayer(rectF, null,
                    Canvas.MATRIX_SAVE_FLAG |
                    Canvas.CLIP_SAVE_FLAG |
                    Canvas.HAS_ALPHA_LAYER_SAVE_FLAG |
                    Canvas.FULL_COLOR_LAYER_SAVE_FLAG |
                    Canvas.CLIP_TO_LAYER_SAVE_FLAG);
			// Resize the rounded rect we'll clip by this view's current bounds
			// (super.onDraw() will do something similar with the drawable to draw)
			getImageMatrix().mapRect(rectF);

	        paint.setAntiAlias(true);
	        canvas.drawARGB(0, 0, 0, 0);
	        paint.setColor(color);
	        canvas.drawRoundRect(rectF, mCornerRadius, mCornerRadius, paint);

			Xfermode oldMode = paint.getXfermode();
			// This is the paint already associated with the BitmapDrawable that super draws
	        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
	        super.onDraw(canvas);
	        paint.setXfermode(oldMode);
	        canvas.restoreToCount(saveCount);
		} else {
			super.onDraw(canvas);
		}
	}
}