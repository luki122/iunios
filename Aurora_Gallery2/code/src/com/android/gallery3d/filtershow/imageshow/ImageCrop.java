/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.filtershow.imageshow;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.android.gallery3d.R;
import com.android.gallery3d.filtershow.crop.CropDrawingUtils;
import com.android.gallery3d.filtershow.crop.CropMath;
import com.android.gallery3d.filtershow.crop.CropObject;
import com.android.gallery3d.filtershow.editors.EditorCrop;
import com.android.gallery3d.filtershow.filters.FilterCropRepresentation;
import com.android.gallery3d.filtershow.imageshow.GeometryMathUtils.GeometryHolder;
import android.view.ScaleGestureDetector;
import android.graphics.Point;
import android.graphics.Rect;

import android.animation.ValueAnimator;
import android.animation.Animator;

public class ImageCrop extends ImageShow {
    private static final String TAG = ImageCrop.class.getSimpleName();
    private RectF mImageBounds = new RectF();
    private RectF mScreenCropBounds = new RectF();
    private Paint mPaint = new Paint();
    private CropObject mCropObj = null;
    private GeometryHolder mGeometry = new GeometryHolder();
    private GeometryHolder mUpdateHolder = new GeometryHolder();
    private Drawable mCropIndicator;
    private int mIndicatorSize;
    private boolean mMovingBlock = false;
    private Matrix mDisplayCropMatrix = null;
    //private Matrix mDisplayMatrixInverse = null;
    private float mPrevX = 0;
    private float mPrevY = 0;
    private float mMinSideSize = 90;
    private float mTouchTolerance = 40;
	//paul add start
	private float mCornerW = 12;
	private float mCornerH = 24;
	private Rect mCanvasRect;
	private RectF mInnerRect; 
	private int mMovingCorner = 0;
	private CropObject mCropObjSave = null;
	private boolean mLeaving = false;
	private Bitmap mPreview = null;
	private InteractionMode mMode = InteractionMode.NONE;
	private int mPaintAlpha = 255;
	//paul add end
	private boolean mNeedSetBg =false;
	
    private enum Mode {
        NONE, MOVE
    }

    private enum TOUCHHANDLE {
		BOX_HANDLE,
		BG_HANDLE,
		NOT_HANDLE

    }
	private TOUCHHANDLE mHandleState = TOUCHHANDLE.BG_HANDLE;
	
    //private Mode mState = Mode.NONE;
    
    private boolean mValidDraw = false;
    FilterCropRepresentation mLocalRep = new FilterCropRepresentation();
    EditorCrop mEditorCrop;

    public ImageCrop(Context context) {
        super(context);
        setup(context);
    }

    public ImageCrop(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context);
    }

    public ImageCrop(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup(context);
    }

    private void setup(Context context) {
        Resources rsc = context.getResources();
        mCropIndicator = rsc.getDrawable(R.drawable.camera_crop);
        mIndicatorSize = (int) rsc.getDimension(R.dimen.crop_indicator_size);
        //mMinSideSize = (int) rsc.getDimension(R.dimen.crop_min_side);
        //mTouchTolerance = (int) rsc.getDimension(R.dimen.crop_touch_tolerance);			
		mCornerW = rsc.getDimension(R.dimen.crop_corner_w);
		mCornerH = rsc.getDimension(R.dimen.crop_corner_h);
		mTouchTolerance = (mCornerW + mCornerH) * 2f;
		mMinSideSize = 2.4f * mTouchTolerance;
    }

    public void setFilterCropRepresentation(FilterCropRepresentation crop) {
        mLocalRep = (crop == null) ? new FilterCropRepresentation() : crop;
        GeometryMathUtils.initializeHolder(mUpdateHolder, mLocalRep);
        mValidDraw = true;
    }

    public FilterCropRepresentation getFinalRepresentation() {		
        return mLocalRep;
    }

    private void internallyUpdateLocalRep(RectF crop, RectF image) {
        FilterCropRepresentation
                .findNormalizedCrop(crop, (int) image.width(), (int) image.height());
        mGeometry.crop.set(crop);
        mUpdateHolder.set(mGeometry);
        mLocalRep.setCrop(crop);

		//mLocalRep.setStraighten(mUpdateHolder.straighten);
    }


	private TOUCHHANDLE getHandleState(MotionEvent event){
		if (event.getAction() == MotionEvent.ACTION_DOWN && mCropObj != null && mMode == InteractionMode.NONE) {
			float x = event.getX();
			float y = event.getY();
			
			RectF screenCropRect = new RectF();
			mCropObj.getInnerBounds(screenCropRect);
			mMovingCorner = mCropObj.selectCorner(x, y, screenCropRect);

			if(CropObject.MOVE_BLOCK== mMovingCorner || CropObject.MOVE_NONE == mMovingCorner){
				mHandleState = TOUCHHANDLE.BG_HANDLE;
			}else{
				mHandleState = TOUCHHANDLE.BOX_HANDLE;
			}
		}
		return mHandleState;
	}

	private boolean canScale(float dx, float dy){
		if(null == mCropObj) return false;
		RectF inner = mCropObj.getInnerBounds();
		if ((mMovingCorner & CropObject.MOVE_LEFT) != 0) {
			inner.left += dx;
		}
		if ((mMovingCorner & CropObject.MOVE_TOP) != 0) {
			inner.top += dy;
		}
		if ((mMovingCorner & CropObject.MOVE_RIGHT) != 0) {
			inner.right += dx;
		}
		if ((mMovingCorner & CropObject.MOVE_BOTTOM) != 0) {
			inner.bottom += dy;
		}
		if(inner.top >= inner.bottom || inner.left >= inner.right) return false;
		
		float width = inner.width();
		float height = inner.height();
		int viewWidth = mCanvasRect.width();
		int viewHeight = mCanvasRect.height();

		float scaleX = (viewWidth - 2 * mShadowMargin) / width;
		float scaleY = (viewHeight - 2 * mShadowMargin) / height;
		float scale = scaleX < scaleY ? scaleX : scaleY;
		
		MasterImage img = MasterImage.getImage();
		float scaleFactor = img.getScaleFactor();

		if (scaleFactor * scale > img.getMaxScaleFactor()) {
			return false;
		}
		return true;
	}
	private void innerResize(){
		RectF inner = mCropObj.getInnerBounds();
		RectF oldInner = new RectF(inner);
		float width = inner.width();
		float height = inner.height();
		int viewWidth = mCanvasRect.width();
		int viewHeight = mCanvasRect.height();

		float scaleX = (viewWidth - 2 * mShadowMargin) / width;
		float scaleY = (viewHeight - 2 * mShadowMargin) / height;
		float scale = scaleX < scaleY ? scaleX : scaleY;
		float x = (inner.right + inner.left) / 2.0f;
		float y = (inner.bottom + inner.top) / 2.0f;

		MasterImage img = MasterImage.getImage();
		float scaleFactor = img.getScaleFactor();

		Matrix m = new Matrix();
		float translateX = viewWidth / 2.0f - x;
		float translateY = viewHeight / 2.0f - y;
		m.postScale(scale, scale, x, y);
		m.postTranslate(translateX, translateY);
		m.mapRect(inner);
		translateX /= scaleFactor;
		translateY /= scaleFactor;
		scaleFactor *= (scale);
		startAnimation(oldInner, inner, translateX, translateY, scaleFactor, 300);
	}
	
	private boolean handleTouchEvent(MotionEvent event){
		   float x = event.getX();
		   float y = event.getY();
		   if (mDisplayCropMatrix == null) {
			   return true;
		   }
		   float[] touchPoint = {
				   x, y
		   };
		   //mDisplayMatrixInverse.mapPoints(touchPoint);
		   x = touchPoint[0];
		   y = touchPoint[1];
		   switch (event.getActionMasked()) {
			   case (MotionEvent.ACTION_DOWN):
				   if (mMode == InteractionMode.NONE && null != mCropObj) {
					   //if (!mCropObj.selectEdge(x, y)) {
						//   mMovingBlock = mCropObj.selectEdge(CropObject.MOVE_BLOCK);
					   //}
					   mPrevX = x;
					   mPrevY = y;
					   mMode = InteractionMode.MOVE;
				   }
				   break;
			   case (MotionEvent.ACTION_UP):
			   case (MotionEvent.ACTION_CANCEL):
				   if (mMode == InteractionMode.MOVE) {
					   mCropObj.selectEdge(CropObject.MOVE_NONE);
					   mMovingBlock = false;
					   mPrevX = x;
					   mPrevY = y;
					   mMode = InteractionMode.NONE;
					   innerResize();
				   }
				   break;
			   case (MotionEvent.ACTION_MOVE):
				   if (mMode == InteractionMode.MOVE) {
					   float dx = x - mPrevX;
					   float dy = y - mPrevY;
					   if(canScale(dx,dy)){
					   	mCropObj.moveCurrentSelectionEx(dx, dy);
					   	mPrevX = x;
					   	mPrevY = y;
					   }
				   }
				   break;
			   default:
				   break;
		   }
		   invalidate();
		   return true;

	}

	
	
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	if(mPlayingAnimation) return true;
    	getHandleState(event);
		if(TOUCHHANDLE.BOX_HANDLE == mHandleState) return handleTouchEvent(event);
		return super.onTouchEvent(event);
    }

    private void clearDisplay() {
        //mDisplayMatrixInverse = null;
        invalidate();
    }

    public void applyFreeAspect() {
        mCropObj.unsetAspectRatio();
        invalidate();
    }

    public void applyOriginalAspect() {
        RectF outer = mCropObj.getOuterBounds();
        float w = outer.width();
        float h = outer.height();
        if (w > 0 && h > 0) {
            applyAspect(w, h);
            mCropObj.resetBoundsTo(outer, outer);
            internallyUpdateLocalRep(mCropObj.getInnerBounds(), mCropObj.getOuterBounds());
        } else {
            Log.w(TAG, "failed to set aspect ratio original");
        }
        invalidate();
    }

    public void applyAspect(float x, float y) {
        if (x <= 0 || y <= 0) {
            throw new IllegalArgumentException("Bad arguments to applyAspect");
        }
        // If we are rotated by 90 degrees from horizontal, swap x and y
        if (GeometryMathUtils.needsDimensionSwap(mGeometry.rotation)) {
            float tmp = x;
            x = y;
            y = tmp;
        }
        if (!mCropObj.setInnerAspectRatio(x, y)) {
            Log.w(TAG, "failed to set aspect ratio");
        }
        internallyUpdateLocalRep(mCropObj.getInnerBounds(), mCropObj.getOuterBounds());
        invalidate();
    }

    /**
     * Rotates first d bits in integer x to the left some number of times.
     */
    private int bitCycleLeft(int x, int times, int d) {
        int mask = (1 << d) - 1;
        int mout = x & mask;
        times %= d;
        int hi = mout >> (d - times);
        int low = (mout << times) & mask;
        int ret = x & ~mask;
        ret |= low;
        ret |= hi;
        return ret;
    }

    /**
     * Find the selected edge or corner in screen coordinates.
     */
    private int decode(int movingEdges, float rotation) {
        int rot = CropMath.constrainedRotation(rotation);
        switch (rot) {
            case 90:
                return bitCycleLeft(movingEdges, 1, 4);
            case 180:
                return bitCycleLeft(movingEdges, 2, 4);
            case 270:
                return bitCycleLeft(movingEdges, 3, 4);
            default:
                return movingEdges;
        }
    }


	/*
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        clearDisplay();
    }
    */
	
	private void updateCrop(){
		if(null == mCropObj) return;
		
		RectF inner = mCropObj.getInnerBounds();

		MasterImage master = MasterImage.getImage();
		if(inner.equals(mInnerRect)){
			if(1.0f == master.getScaleFactor()
				&& 0 == master.getTranslation().x 
				&& 0 == master.getTranslation().y){
				return;
			}
		}
		
        Matrix originalToScreen = master.originalImageToScreen();
        if (originalToScreen == null) {
            return;
        }
        Matrix screenToOriginal = new Matrix();
        originalToScreen.invert(screenToOriginal);
		
		screenToOriginal.mapRect(inner);

		RectF fullHighres = new RectF(master.getOriginalBounds());
		
		if (!fullHighres.contains(inner)) {
			inner.intersect(fullHighres);	
		}		

		FilterCropRepresentation
			.findNormalizedCrop(inner, fullHighres.width(),fullHighres.height());

		mGeometry.crop.set(inner);
		mUpdateHolder.set(mGeometry);
		mLocalRep.setCrop(inner);
	}
	
	public void finalApplyCalled(){
		mLeaving = true;
		MasterImage.getImage().setUpdating(true);
		MasterImage.getImage().removeObserver(this);
		updateCrop();
		MasterImage.getImage().save();
		mCropObjSave = mCropObj;
		if (mAnimatorRect != null) {
			mAnimatorRect.cancel();
		}
		mCropObj = null;
	}
	
	public void backToMain() {
		if (mAnimatorRect != null) {
			mAnimatorRect.cancel();
		}
		mCropObj = null;	
	}

	private void forceStateConsistency(Canvas canvas) {
		MasterImage master = MasterImage.getImage();
		Bitmap image = master.getPartialImage();
		if(null == image){
			return;
		}
		int width = image.getWidth();
		int height = image.getHeight();
		if (mCropObj == null || !mUpdateHolder.equals(mGeometry)
				|| mImageBounds.width() != width || mImageBounds.height() != height
				|| !mLocalRep.getCrop().equals(mUpdateHolder.crop)) {
			mImageBounds.set(0, 0, width, height);
			mGeometry.set(mUpdateHolder);
			mLocalRep.setCrop(mUpdateHolder.crop);

			if(null == mCropObj){
				float straighten = mGeometry.straighten;
				mGeometry.straighten = 0;
				mCanvasRect = new Rect(0,0,canvas.getWidth(), canvas.getHeight());
				mDisplayCropMatrix = GeometryMathUtils.getFullGeometryToScreenMatrix(mGeometry,
						width, height, canvas.getWidth(), canvas.getHeight(),mShadowMargin);
				
				mGeometry.straighten = straighten;
				mInnerRect = new RectF(mUpdateHolder.crop);
				FilterCropRepresentation.findScaledCrop(mInnerRect, width, height);
				mDisplayCropMatrix.mapRect(mInnerRect);

				//RectF outter = new RectF(0, 0, canvas.getWidth(), canvas.getHeight());
				mCurrentInnerRect = new RectF(mInnerRect);
				mCropObj = new CropObject(mInnerRect, mInnerRect, (int) mUpdateHolder.straighten);
				
				mCropObj.unsetAspectRatio();
				// Scale min side and tolerance by display matrix scale factor
				//mCropObj.setMinInnerSideSize(mDisplayMatrixInverse.mapRadius(mMinSideSize));
				//mCropObj.setTouchTolerance(mDisplayMatrixInverse.mapRadius(mTouchTolerance));
				mCropObj.setMinInnerSideSize(mMinSideSize);
				mCropObj.setTouchTolerance(mTouchTolerance);
			}else {
				updateOuter();
			}
			clearDisplay();
		}
	}

	private void updateOuter(){
		MasterImage master = MasterImage.getImage();
		RectF ori = new RectF(master.getOriginalBounds());
		Matrix originalToScreen = master.originalImageToScreen();
		if (originalToScreen != null) {
			originalToScreen.mapRect(ori);
			RectF view = new RectF(mCanvasRect);
			if (!view.contains(ori)) {
				ori.intersect(view); 
			}
			
			if(mCropObj.setOuterTo(ori)){
				mCurrentInnerRect = mCropObj.getInnerBounds();
			}
		}

	}
    @Override
    public void onDraw(Canvas canvas) {
    	if(mLeaving) return;
        forceStateConsistency(canvas);
        mPaint.reset();
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
        MasterImage.getImage().setImageShowSize(
                getWidth() - 2 * mShadowMargin,
                getHeight() - 2 * mShadowMargin);

        MasterImage img = MasterImage.getImage();
		Bitmap fullHighres = MasterImage.getImage().getPartialImage();

        canvas.save();

        mShadowDrawn = false;
		if(null == mPreview || !mLeaving){
        	mPreview = MasterImage.getImage().getHighresImage();
        }

        //boolean isDoingNewLookAnimation = MasterImage.getImage().onGoingNewLookAnimation();
		if(!mLeaving){
	        if (mPreview == null) {
	            drawImageAndAnimate(canvas, getFilteredImage());
	        } else {
	            drawImageAndAnimate(canvas, mPreview);
	        }
			if(!mPlayingAnimation){
				drawHighresImageEx(canvas, fullHighres);
			}
		}
       
        //drawCompareImage(canvas, getGeometryOnlyImage());
        if(null!= mCropObj && !mLeaving){
        	if(mNeedSetBg){
        		mNeedSetBg = false;
        		mActivity.changeMainPanelBackgroundColor();//Aurora <SQF> <2014-07-16>  for NEW_UI
				mCropObj.getInnerBounds(mScreenCropBounds);
				startAnimation(new RectF(mCanvasRect), mScreenCropBounds, 100);
        	} else {
		        mCropObj.getInnerBounds(mScreenCropBounds);
	            CropDrawingUtils.drawCropRect(canvas, mScreenCropBounds, mPaintAlpha);
				CropDrawingUtils.drawCorners(canvas, mScreenCropBounds, mCornerW, mCornerH, mPaintAlpha);
	            CropDrawingUtils.drawShade(canvas, mScreenCropBounds);
	            CropDrawingUtils.drawRuleOfThird(canvas, mScreenCropBounds, mPaintAlpha);      
        	}
        }
        canvas.restore();

    }

    public void setEditor(EditorCrop editorCrop) {
        mEditorCrop = editorCrop;
    }

	//paul add animation 
	private ValueAnimator mAnimatorRect = null;
	private RectF mAnimRect;
	private RectF mAnimOffset = new RectF();
	private Point mTranslationFrom;
	private float mTranslationOffsetX;
	private float mTranslationOffsetY;
	private float mScaleFrom;
	private float mScaleOffset;
	private RectF mTempRect = new RectF();
	private Point mTempPoint = new Point();
	float mX;
	float mY;
	private boolean mPlayingAnimation = false;

	public boolean isPlayingAnimation(){
		return mPlayingAnimation;
	}
	public void enter(boolean isReload) {
		if(isReload){
			mCropObj = mCropObjSave;			
		}
		
		MasterImage.getImage().setUpdating(false);
		mLeaving = false;
		mNeedSetBg = true;
		mPlayingAnimation = false;

	}
	
	private void startAnimation(RectF from, RectF to, int delay){
		if (from.equals(to)) {
			return;
		}
		
		if (mAnimatorRect != null) {
			mAnimatorRect.cancel();
		}
		mPlayingAnimation = true;
		
		mAnimRect = from;
		mAnimOffset.left = to.left - from.left;
		mAnimOffset.right = to.right - from.right;
		mAnimOffset.top = to.top - from.top;
		mAnimOffset.bottom = to.bottom - from.bottom;
		mAnimatorRect = ValueAnimator.ofFloat(0f, 1f);
		mAnimatorRect.setDuration(delay);

		mAnimatorRect.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float progress = (Float)animation.getAnimatedValue();
				
				mTempRect.left = mAnimRect.left + mAnimOffset.left * progress;
				mTempRect.right = mAnimRect.right + mAnimOffset.right * progress;
				mTempRect.top = mAnimRect.top + mAnimOffset.top * progress;
				mTempRect.bottom = mAnimRect.bottom + mAnimOffset.bottom * progress;
				mCropObj.setInnerTo(mTempRect);
				mCurrentInnerRect = mTempRect;

				mPaintAlpha = (int)(progress * 255);
				invalidate();
			}
		});
		mAnimatorRect.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
			}
		
			@Override
			public void onAnimationEnd(Animator animation) {
				mPlayingAnimation = false;
				mPaintAlpha = 255;
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				mPlayingAnimation = false;
				mPaintAlpha = 255;
			}
	
			@Override
			public void onAnimationRepeat(Animator animation) {
			}
		});

		mAnimatorRect.start();
	}


	private void startAnimation(RectF from, RectF to,float tx, float ty, float toScale, int delay){
		mScaleFrom = MasterImage.getImage().getScaleFactor();
		mScaleOffset = toScale - mScaleFrom;

		if (from.equals(to) && tx == 0 && ty == 0 && mScaleOffset == 0) {
			return;
		}
		
		if (mAnimatorRect != null) {
			mAnimatorRect.cancel();
		}
		mPlayingAnimation = true;
		
		mAnimRect = from;
		mAnimOffset.left = to.left - from.left;
		mAnimOffset.right = to.right - from.right;
		mAnimOffset.top = to.top - from.top;
		mAnimOffset.bottom = to.bottom - from.bottom;
		mAnimatorRect = ValueAnimator.ofFloat(0f, 1f);
		mAnimatorRect.setDuration(delay);

		mTranslationFrom = new Point(MasterImage.getImage().getTranslation());
		mTranslationOffsetX = tx;
		mTranslationOffsetY = ty;

		mX = mTranslationOffsetX < 0 ? -0.5f : 0.5f;
		mY = mTranslationOffsetY < 0 ? -0.5f : 0.5f;
		mAnimatorRect.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float progress = (Float)animation.getAnimatedValue();
				
				mTempRect.left = mAnimRect.left + mAnimOffset.left * progress;
				mTempRect.right = mAnimRect.right + mAnimOffset.right * progress;
				mTempRect.top = mAnimRect.top + mAnimOffset.top * progress;
				mTempRect.bottom = mAnimRect.bottom + mAnimOffset.bottom * progress;
				mCropObj.setInnerTo(mTempRect);
				mCurrentInnerRect = mTempRect;
				mTempPoint.x = (int)(mTranslationFrom.x + mTranslationOffsetX * progress + mX);
				mTempPoint.y = (int)(mTranslationFrom.y + mTranslationOffsetY * progress + mY);

				MasterImage.getImage().setTranslation(mTempPoint);
				MasterImage.getImage().setScaleFactor(mScaleFrom + mScaleOffset * progress);

				invalidate();
			}


			
		});
		mAnimatorRect.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
			}
		
			@Override
			public void onAnimationEnd(Animator animation) {
				updateOuter();
				MasterImage.getImage().needsUpdatePartialPreview();
				mPlayingAnimation = false;
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				updateOuter();
				MasterImage.getImage().needsUpdatePartialPreview();
				mPlayingAnimation = false;
			}
	
			@Override
			public void onAnimationRepeat(Animator animation) {
			}
		});

		mAnimatorRect.start();
	}

}
