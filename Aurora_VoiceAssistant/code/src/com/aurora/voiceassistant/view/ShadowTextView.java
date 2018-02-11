package com.aurora.voiceassistant.view;

import android.R.integer;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.TextPaint;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.TextView;

import android.graphics.Matrix;
import com.aurora.voiceassistant.model.DeviceProperties;
import com.aurora.voiceassistant.*;

/**
 * 
 * @author xiejun
 * Application Icon 设计
 * Add
 */
@SuppressLint("NewApi")
public class ShadowTextView extends TextView {
	static final float CORNER_RADIUS = 4.0f;
	static final float SHADOW_LARGE_RADIUS = 25.0f;
	static final float SHADOW_SMALL_RADIUS = 1.75f;
	static final float SHADOW_Y_OFFSET = 1.0f;
	static final int SHADOW_LARGE_COLOUR = 0x50000000;
	static final int SHADOW_SMALL_COLOUR = 0x46000000;
	static final float PADDING_H = 8.0f;
	static final float PADDING_V = 3.0f;

	private int mPrevAlpha = -1;

	private final HolographicOutlineHelper mOutlineHelper = new HolographicOutlineHelper();
	private final Canvas mTempCanvas = new Canvas();
	private final Rect mTempRect = new Rect();
	private boolean mDidInvalidateForPressedState;
	private Bitmap mPressedOrFocusedBackground;
	private int mFocusedOutlineColor;
	private int mFocusedGlowColor;
	private int mPressedOutlineColor;
	private int mPressedGlowColor;

	private boolean mBackgroundSizeChanged;
	private Drawable mBackground;

	private boolean mStayPressed;
//	private CheckLongPressHelper mLongPressHelper;

	// Aurora <jialf> <2013-11-04> modify for fix bug #275 begin
    private float mLastMotionY;
    private float mLastMotionX;
    protected int mTouchSlop;
	// Aurora <jialf> <2013-11-04> modify for fix bug #275 end
    
    private Context mContext;
    
    private boolean isNeedScale = false;
    private float factor = 1.0f;

	public ShadowTextView(Context context) {
		super(context);
		
		mContext = context;
		
		init();
	}

	public ShadowTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ShadowTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	protected void init() {
//		mLongPressHelper = new CheckLongPressHelper(this);
		//mBackground = getBackground();
//		mBackground = getResources().getDrawable(R.drawable.textview_shadow);
//		mBackground = mContext.getResources().getDrawable(R.drawable.vs_barcode_getphotos_pressed);

		// Aurora <jialf> <2013-11-04> modify for fix bug #275 begin
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
		// Aurora <jialf> <2013-11-04> modify for fix bug #275 begin

		final Resources res = getContext().getResources();
		mFocusedOutlineColor = mFocusedGlowColor = mPressedOutlineColor = mPressedGlowColor = res
				.getColor(android.R.color.holo_blue_light);

		/*setShadowLayer(SHADOW_LARGE_RADIUS, 0.0f, SHADOW_Y_OFFSET,
				SHADOW_LARGE_COLOUR);*/
		
		isNeedScale = DeviceProperties.isNeedScale();
		if(isNeedScale) {
			factor = 0.93f;
		}
	}
	
	public void applyFromShortcutInfo(Drawable drawable, String string) {
		Log.d("DEBUG", "isNeedScale = "+isNeedScale+" the factor = "+factor);
		if (isNeedScale) {
			drawable = zoomDrawable(drawable);
		}
		
		setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
		setGravity(Gravity.CENTER_HORIZONTAL);
		setSingleLine(true);
		setEllipsize(TruncateAt.END);
		setText(string);
	}

	/*public void applyFromShortcutInfo(ShortcutInfo info, IconCache iconCache) {
		Bitmap b = info.getIcon(iconCache);
		setCompoundDrawablesWithIntrinsicBounds(null,
				new FastBitmapDrawable(b), null, null);
		setText(info.title);
		setTag(info);
	}*/

	@Override
	protected boolean setFrame(int left, int top, int right, int bottom) {
		if (getLeft() != left || getRight() != right || getTop() != top
				|| getBottom() != bottom) {
			mBackgroundSizeChanged = true;
		}
		return super.setFrame(left, top, right, bottom);
	}

	@Override
	protected boolean verifyDrawable(Drawable who) {
		return who == mBackground || super.verifyDrawable(who);
	}

	@Override
	public void setTag(Object tag) {
		if (tag != null) {
//			LauncherModel.checkItemInfo((ItemInfo) tag);
		}
		super.setTag(tag);
	}

	@Override
	protected void drawableStateChanged() {
		/*if (isPressed()) {
			// In this case, we have already created the pressed outline on
			// ACTION_DOWN,
			// so we just need to do an invalidate to trigger draw
			if (!mDidInvalidateForPressedState) {
				setCellLayoutPressedOrFocusedIcon();
			}
			
		} else {
			// Otherwise, either clear the pressed/focused background, or create
			// a background
			// for the focused state
			final boolean backgroundEmptyBefore = mPressedOrFocusedBackground == null;
			if (!mStayPressed) {
				mPressedOrFocusedBackground = null;
			}
			if (isFocused()) {
				if (getLayout() == null) {
					// In some cases, we get focus before we have been layed
					// out. Set the
					// background to null so that it will get created when the
					// view is drawn.
					mPressedOrFocusedBackground = null;
				} else {
					mPressedOrFocusedBackground = createGlowingOutline(
							mTempCanvas, mFocusedGlowColor,
							mFocusedOutlineColor);
				}
				mStayPressed = false;
				setCellLayoutPressedOrFocusedIcon();
			}
			final boolean backgroundEmptyNow = mPressedOrFocusedBackground == null;
			if (!backgroundEmptyBefore && backgroundEmptyNow) {
				setCellLayoutPressedOrFocusedIcon();
			}
		}*/

		/*Drawable d = mBackground;
		if (d != null && d.isStateful()) {
			d.setState(getDrawableState());
		}*/
		super.drawableStateChanged();
	}

	/**
	 * Draw this BubbleTextView into the given Canvas.
	 * 
	 * @param destCanvas
	 *            the canvas to draw on
	 * @param padding
	 *            the horizontal and vertical padding to use when drawing
	 */
	private void drawWithPadding(Canvas destCanvas, int padding) {
		final Rect clipRect = mTempRect;
		getDrawingRect(clipRect);

		// adjust the clip rect so that we don't include the text label
		clipRect.bottom = getExtendedPaddingTop()
				- (int) ShadowTextView.PADDING_V + getLayout().getLineTop(0);

		// Draw the View into the bitmap.
		// The translate of scrollX and scrollY is necessary when drawing
		// TextViews, because
		// they set scrollX and scrollY to large values to achieve centered text
		destCanvas.save();
		destCanvas.scale(getScaleX(), getScaleY(), (getWidth() + padding) / 2,
				(getHeight() + padding) / 2);
		destCanvas.translate(-getScrollX() + padding / 2, -getScrollY()
				+ padding / 2);
		destCanvas.clipRect(clipRect, Op.REPLACE);
		draw(destCanvas);
		destCanvas.restore();
	}

	/**
	 * Returns a new bitmap to be used as the object outline, e.g. to visualize
	 * the drop location. Responsibility for the bitmap is transferred to the
	 * caller.
	 */
	private Bitmap createGlowingOutline(Canvas canvas, int outlineColor,
			int glowColor) {
		final int padding = HolographicOutlineHelper.MAX_OUTER_BLUR_RADIUS;
		final Bitmap b = Bitmap.createBitmap(getWidth() + padding, getHeight()
				+ padding, Bitmap.Config.ARGB_8888);

		canvas.setBitmap(b);
		drawWithPadding(canvas, padding);
		mOutlineHelper.applyExtraThickExpensiveOutlineWithBlur(b, canvas,
				glowColor, outlineColor);
		canvas.setBitmap(null);

		return b;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Call the superclass onTouchEvent first, because sometimes it changes
		// the state to
		// isPressed() on an ACTION_UP
		boolean result = super.onTouchEvent(event);

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// So that the pressed outline is visible immediately when
			// isPressed() is true,
			// we pre-create it on ACTION_DOWN (it takes a small but perceptible
			// amount of time
			// to create it)
			/*
			if (mPressedOrFocusedBackground == null) {
				mPressedOrFocusedBackground = createGlowingOutline(mTempCanvas,
						mPressedGlowColor, mPressedOutlineColor);
			}
			// Invalidate so the pressed state is visible, or set a flag so we
			// know that we
			// have to call invalidate as soon as the state is "pressed"
			if (isPressed()) {
				mDidInvalidateForPressedState = true;
				setCellLayoutPressedOrFocusedIcon();
			} else {
				mDidInvalidateForPressedState = false;
			}
			*/

			iconScale(0.97f);
//            mLongPressHelper.postCheckForLongPress();
			mLastMotionY = event.getY();
            mLastMotionX = event.getX();
            postDelayed(new Runnable() {
				
				@Override
				public void run() {
					iconScale(1.0f);
				}
			}, 300);
            break;
        case MotionEvent.ACTION_MOVE:
            final float y = event.getY();
            final float x = event.getX();
            final float yDiff = Math.abs(y - mLastMotionY);
            final float xDiff = Math.abs(x - mLastMotionX);
            if (xDiff > mTouchSlop || yDiff > mTouchSlop) {
                cancelLongPress();
            }
            break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			// If we've touched down and up on an item, and it's still not
			// "pressed", then
			// destroy the pressed outline
			/*if (!isPressed()) {
				mPressedOrFocusedBackground = null;
			}*/
			// Aurora <jialf> <2013-11-04> modify for fix bug #275 end
			iconScale(1.0f);
//			mLongPressHelper.cancelLongPress();
			break;
		}
		return result;
	}

	void setStayPressed(boolean stayPressed) {
		mStayPressed = stayPressed;
		if (!stayPressed) {
			mPressedOrFocusedBackground = null;
		}
		setCellLayoutPressedOrFocusedIcon();
	}

	void setCellLayoutPressedOrFocusedIcon() {
		/*if (getParent() instanceof ShortcutAndWidgetContainer) {
			ShortcutAndWidgetContainer parent = (ShortcutAndWidgetContainer) getParent();
			if (parent != null) {
				CellLayout layout = (CellLayout) parent.getParent();
				layout.setPressedOrFocusedIcon((mPressedOrFocusedBackground != null) ? this
						: null);
			}
		}*/
	}

	void clearPressedOrFocusedBackground() {
		mPressedOrFocusedBackground = null;
		setCellLayoutPressedOrFocusedIcon();
	}

	Bitmap getPressedOrFocusedBackground() {
		return mPressedOrFocusedBackground;
	}

	int getPressedOrFocusedBackgroundPadding() {
		return HolographicOutlineHelper.MAX_OUTER_BLUR_RADIUS / 2;
	}

	@Override
	public void draw(Canvas canvas) {
		/*if(getCurrentTextColor() != getResources().getColor(
				android.R.color.transparent)){
			canvas.save();
			TextPaint tPaint = getPaint();
			CharSequence charSequence1 = getText();
			int textLength=0;
			String string = null;
			if(charSequence1!=null){
				string = charSequence1.toString().trim();
				textLength=charSequence1.length();
			}
			final Drawable background = mBackground;//new ColorDrawable(Color.RED);
			final int scrollX = getScrollX();
			final int scrollY = getScrollY();
	        final int compoundPaddingLeft = getCompoundPaddingLeft();
	        final int compoundPaddingTop = getCompoundPaddingTop();
	        final int compoundPaddingRight = getCompoundPaddingRight();
	        final int compoundPaddingBottom = getCompoundPaddingBottom(); 
	        float desiredWidth = tPaint.measureText(this.getText(),0,textLength);
	        float exactWidth = getWidth() - compoundPaddingLeft-compoundPaddingRight;
	        if(desiredWidth > exactWidth){
	        	desiredWidth = exactWidth-10;
	        }
			float desiredHeight =this.getLineHeight();
			int drawablePanding = getCompoundDrawablePadding();
	        float clipLeft = compoundPaddingLeft;
	        float clipTop = (scrollY == 0) ? 0 : compoundPaddingTop + scrollY+drawablePanding;
	        float clipRight = getRight() - getLeft() - compoundPaddingRight + scrollX;
	        float clipBottom = getBottom() - getTop() + scrollY - compoundPaddingBottom;
			Drawable drawableTop =getCompoundDrawables()[1];
			if(drawableTop!=null){
				clipTop+=drawableTop.getIntrinsicHeight();
			}		
	        canvas.clipRect(new RectF(clipLeft, clipTop, clipRight, clipBottom));
			canvas.translate(compoundPaddingLeft, compoundPaddingTop);
			float dl = (getWidth()-desiredWidth-compoundPaddingLeft-compoundPaddingRight)/2-10;
			float dr = dl +desiredWidth+20;
			float dt = -10;
			float db = desiredHeight+15;
			if (background != null&&string!=null&&string.length()>0) {
				if (mBackgroundSizeChanged) {
					background.setBounds((int)dl, (int)dt, (int)dr,(int)db);
					mBackgroundSizeChanged = false;
				}
				if ((scrollX | scrollY) == 0) {
					background.draw(canvas);
				} else {
					canvas.translate(scrollX, scrollY);
					background.draw(canvas);
					canvas.translate(-scrollX, -scrollY);
				}
			}
			canvas.restore();
		}*/

		// If text is transparent, don't draw any shadow
		/*if (getCurrentTextColor() == getResources().getColor(
				android.R.color.transparent)) {
			getPaint().clearShadowLayer();
			super.draw(canvas);
			return;
		}*/
		super.draw(canvas);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (mBackground != null)
			mBackground.setCallback(this);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mBackground != null)
			mBackground.setCallback(null);
	}

	/*@Override
	protected boolean onSetAlpha(int alpha) {
		if (mPrevAlpha != alpha) {
			mPrevAlpha = alpha;
			super.onSetAlpha(alpha);
		}
		return true;
	}*/

	@Override
	public void cancelLongPress() {
		super.cancelLongPress();
//		mLongPressHelper.cancelLongPress();
	}
	
	public float getTextDediredWiddth(){
		TextPaint tPaint = getPaint();
		CharSequence charSequence1 = getText();
		int textLength=0;
		String string = null;
		float desiredWidth = 0;
		if(charSequence1!=null){
			string = charSequence1.toString().trim();
			string = string != null ? string.toString().replace(' ', ' ').trim() : null;
			if(string!=null){
				textLength=string.length();
				desiredWidth = tPaint.measureText(string,0,textLength);
				float exactWidth = getWidth() - getCompoundPaddingLeft()-getCompoundPaddingRight();
		        if(desiredWidth > exactWidth){
		        	desiredWidth = exactWidth-10;
		        }
			}else{
				textLength=0;
			}
		}
        return desiredWidth;
	}
	
	private void iconScale(float f){
		setScaleX(f);
		setScaleY(f);
		setPivotX(getWidth()/2);
		setPivotY(getHeight()/2);
		if(f<1.0f){
			this.setAlpha(0.6f);
		}else{
			this.setAlpha(1.0f);
		}
	}
	
	private Drawable zoomDrawable(Drawable drawable) {
		BitmapDrawable targetBitmapDrawable = (BitmapDrawable) drawable;
		Bitmap targetBitmap = targetBitmapDrawable.getBitmap();
		Bitmap resultBitmap = zoomBitmap(targetBitmap, mContext);
		BitmapDrawable resultDrawable = new BitmapDrawable(mContext.getResources(), resultBitmap);
		return resultDrawable;
	}
	
	public Bitmap zoomBitmap(Bitmap oldbmp ,Context context){
    	Matrix matrix = new Matrix();
		int width = oldbmp.getWidth();
		int height = oldbmp.getHeight();
//		float scaleWidth = 0.93f;
//		float scaleHeight = 0.93f;
		float scaleWidth = factor;
		float scaleHeight = factor;
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height, matrix, true);
		
		return newbmp;
    }
}
