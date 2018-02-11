package com.aurora.change.view;

import com.aurora.thememanager.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

/**
 * 放大底图裁剪
 * 
 * @author zhangxin
 * 
 */

public class CropImageView extends ImageView {

    private float mOldX = 0;
    private float mOldY = 0;

    private float mOldX_0 = 0;
    private float mOldY_0 = 0;

    private float mOldX_1 = 0;
    private float mOldY_1 = 0;

    private static final int STATUS_TOUCH_SINGLE = 1;
    private static final int STATUS_TOUCH_MULTI_START = 2;
    private static final int STATUS_TOUCH_MULTI_DRAGING = 3;

    private int mStatus = STATUS_TOUCH_SINGLE;

    // 默认裁剪宽高
    private Context mContext;
    //private int mDefaultCropWidth = 480;
    //private int mDefaultCropHeight = 800;
    private int mCropWidth = 480;
    private int mCropHeight = 800;

    private float mOriRationHW = 0f;
    private float mScreenRatioWH = 0f;
    private final float mMaxZoomOut = 5.0f;// 最大放大倍数
//    private final float mMinZoomIn = 0.333333f;// 最小放大倍数
    private final float mMinZoomIn = 1f;// 最小放大倍数

    private Drawable mDrawable;// 原图
//    private FloatDrawable mFloatDrawable;// 浮层
    private Drawable mFloatDrawable;// 浮层
    private Rect mDrawableSrc = new Rect();// 原图裁剪矩形框
    private Rect mDrawableDst = new Rect();// 裁剪后图片矩形框
    private Rect mDrawableFloat = new Rect();// 裁剪浮层矩形框
    private boolean isFrist = true;
    private float mRotation = 0;

    private TranslateAnimation trans;

    // 显示/隐藏状态栏
//    private int mBaseSystemUiVisibility = SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | SYSTEM_UI_FLAG_LAYOUT_STABLE;
    private int mLastSystemUiVis = 0;
    private boolean isShowSystemUi = false;
    //private ActionBarCallBack mActionBarCallBack;

    private final static boolean DEBUG = true;
    private final static String TAG = "CropImageView";

    Bitmap mBitmap = null; 

    public boolean mIsSaveEnable = false;

    /*Runnable mNavHider = new Runnable() {

        @Override
        public void run() {
            setNavVisibility(false);
        }
    };*/
   
    
    public CropImageView(Context context) {
        this(context, null);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mIsSaveEnable = false;
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        logD("cropImage:setDrawable", "drawable");
     
       super.setImageDrawable(drawable);
       if (drawable != null)
       {
       	setDrawable(drawable);
       }
      
    }

    public void setImageDrawable(Drawable drawable, float rotation) {
       logD("cropImage:setDrawable=", "rotation=" + rotation);
        if (drawable != null) {
            mRotation = rotation;
            setImageDrawable(drawable);
        }
    }

    public void setDrawable(Drawable drawable) {
        
        logD("cropImage:setDrawable",
                "height=" + drawable.getIntrinsicHeight() + ",width=" + drawable.getIntrinsicWidth()
                        + ",cropHeight=" + getHeight() + ",cropWidth=" + getWidth());
        mDrawable = drawable;
        mCropWidth = getWidth();
        mCropHeight = getHeight();
        isFrist = true;
        if (mDrawable.getIntrinsicWidth() > 0 || mDrawable.getIntrinsicHeight() > 0) {
            mIsSaveEnable = true;
        }
        if (mRotation > 0 && drawable.getIntrinsicHeight() > 0 && drawable.getIntrinsicWidth() > 0) {
            mDrawable = rotationDrawable(drawable, mRotation);
        }
   
    }

    @Override
    protected void onDraw(Canvas canvas) {
    	if (mDrawable == null) {
            return;
        }

        // nothing to draw (empty bounds)
        if (mDrawable.getIntrinsicWidth() <=0 || mDrawable.getIntrinsicHeight() <= 0) {
            return;
        }
        configureBounds();

        logD("mTempDst:", mDrawable.getBounds().width()+"---------" + mDrawable.getBounds().height());
        logD("mTempDst:", mDrawableDst+"-------ration:" + (float) mDrawable.getBounds().width()
                / (float) mDrawable.getBounds().height());

        mDrawable.draw(canvas);

//        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
//        Canvas mycanvas = new Canvas(mBitmap);
//        mDrawable.draw(mycanvas);
        
        canvas.save();
        canvas.clipRect(mDrawableFloat, Region.Op.DIFFERENCE);
        canvas.drawColor(Color.parseColor("#a0000000"));
        canvas.restore();
      
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        getParent().requestDisallowInterceptTouchEvent(true);
        if (event.getPointerCount() > 1) {
            if (mStatus == STATUS_TOUCH_SINGLE) {
                mStatus = STATUS_TOUCH_MULTI_START;

                mOldX_0 = event.getX(0);
                mOldY_0 = event.getY(0);

                mOldX_1 = event.getX(1);
                mOldY_1 = event.getY(1);
            } else if (mStatus == STATUS_TOUCH_MULTI_START) {
                mStatus = STATUS_TOUCH_MULTI_DRAGING;
            }
        } else {
            if (mStatus == STATUS_TOUCH_MULTI_START || mStatus == STATUS_TOUCH_MULTI_DRAGING) {
                mOldX_0 = 0;
                mOldY_0 = 0;

                mOldX_1 = 0;
                mOldY_1 = 0;

                mOldX = event.getX();
                mOldY = event.getY();
            }

            mStatus = STATUS_TOUCH_SINGLE;
        }

        logD("count currentTouch" + event.getPointerCount(), "-------");

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                logD("count ACTION_DOWN", "-------");
                mOldX = event.getX();
                mOldY = event.getY();
                //isShowSystemUi = true;
                mIsSaveEnable = false;
                break;

            case MotionEvent.ACTION_UP:
                logD("count ACTION_UP", "-------" + event.getPointerCount());
                checkBounds();
                /*if (isShowSystemUi) {
                    int dx_action = Math.abs((int) (event.getX() - mOldX));
                    int dy_action = Math.abs((int) (event.getY() - mOldY));
                    if (dx_action <= 5 && dy_action <= 5) {
                        if (mActionBarCallBack != null) {
                            mActionBarCallBack.toggleActionBarVisibility();
                        }
//                    toggleActionBarVisibility();
                    }
                }*/
                mIsSaveEnable = true;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                logD("count ACTION_POINTER_DOWN", "-------");
                break;

            case MotionEvent.ACTION_POINTER_UP:
                logD("count ACTION_POINTER_UP", "-------");
                //isShowSystemUi = false;
                break;

            case MotionEvent.ACTION_MOVE:
                logD("count ACTION_MOVE", "-------");
                if (mStatus == STATUS_TOUCH_MULTI_DRAGING) {
                    float newx_0 = event.getX(0);
                    float newy_0 = event.getY(0);

                    float newx_1 = event.getX(1);
                    float newy_1 = event.getY(1);

                    float oldWidth = Math.abs(mOldX_1 - mOldX_0);
                    float oldHeight = Math.abs(mOldY_1 - mOldY_0);

                    float newWidth = Math.abs(newx_1 - newx_0);
                    float newHeight = Math.abs(newy_1 - newy_0);

                    boolean isDependHeight = Math.abs(newHeight - oldHeight) > Math.abs(newWidth - oldWidth);

                    float ration = isDependHeight ? ((float) newHeight / (float) oldHeight)
                            : ((float) newWidth / (float) oldWidth);
                    int centerX = mDrawableDst.centerX();
                    int centerY = mDrawableDst.centerY();
                    int _newHeight = (int) (mDrawableDst.height() * ration);
                    int _newWidth = (int) ((float) _newHeight / mOriRationHW);

                    float tmpZoomRation = (float) _newWidth / (float) mDrawableSrc.width();
                    if (tmpZoomRation >= mMaxZoomOut) {
                        _newHeight = (int) (mMaxZoomOut * mDrawableSrc.height());
                        _newWidth = (int) ((float) _newHeight / mOriRationHW);
                    } else if (tmpZoomRation <= mMinZoomIn) {
                        _newHeight = (int) (mMinZoomIn * mDrawableSrc.height());
                        _newWidth = (int) ((float) _newHeight / mOriRationHW);
                    }

                    mDrawableDst.set(centerX - _newWidth / 2, centerY - _newHeight / 2, centerX + _newWidth
                            / 2, centerY + _newHeight / 2);
                    invalidate();

                    logD("width():" + (mDrawableSrc.width()) + "height():" + (mDrawableSrc.height()),
                            "new width():" + (mDrawableDst.width()) + "new height():"
                                    + (mDrawableDst.height()));
                    logD("" + (float) mDrawableSrc.height() / (float) mDrawableSrc.width(), "mDrawableDst:"
                            + (float) mDrawableDst.height() / (float) mDrawableDst.width());

                    mOldX_0 = newx_0;
                    mOldY_0 = newy_0;

                    mOldX_1 = newx_1;
                    mOldY_1 = newy_1;
                } else if (mStatus == STATUS_TOUCH_SINGLE) {
                    int dx = (int) (event.getX() - mOldX);
                    int dy = (int) (event.getY() - mOldY);

                    mOldX = event.getX();
                    mOldY = event.getY();

                    if (!(dx == 0 && dy == 0)) {
                        mDrawableDst.offset((int) dx, (int) dy);
                        invalidate();
                        //isShowSystemUi = false;
                    }
                }
                mIsSaveEnable = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                mIsSaveEnable = false;
                break;
        }

        logD("event.getAction()：" + event.getAction() + "count：" + event.getPointerCount(), "-------getX:"
                + event.getX() + "--------getY:" + event.getY());
        return true;
    }

    private void configureBounds() {
        if (isFrist) {
            logD("configureBounds start:",
                    "height=" + mDrawable.getIntrinsicHeight() + ",width=" + mDrawable.getIntrinsicWidth());
            mOriRationHW = ((float) mDrawable.getIntrinsicHeight()) / ((float) mDrawable.getIntrinsicWidth());

            final float scale = mContext.getResources().getDisplayMetrics().density;

            int h = Math.min(getHeight(), (int) (mDrawable.getIntrinsicHeight() * scale + 0.5f));
            //int h = mDrawable.getIntrinsicHeight();
            int w = (int) (h / mOriRationHW);
            //int w = mDrawable.getIntrinsicWidth();

            int left = (getWidth() - w) / 2;
            int top = (getHeight() - h) / 2;
            int right = left + w;
            int bottom = top + h;

            mDrawableSrc.set(left, top, right, bottom);
            mDrawableDst.set(mDrawableSrc);

            // 设置浮层的大小
//            int floatWidth = dipTopx(mContext, mCropWidth);
//            int floatHeight = dipTopx(mContext, mCropHeight);
//            int floatLeft = (getWidth() - floatWidth) / 2;
//            int floatTop = (getHeight() - floatHeight) / 2;
//            mDrawableFloat.set(floatLeft, floatTop, floatLeft + floatWidth, floatTop + floatHeight);
            mDrawableFloat.set(0, 0, mCropWidth, mCropHeight);

            isFrist = false;
            logD("configureBounds end:", "mOriRationWH=" + mOriRationHW + ",getWidth=" + getWidth()
                    + ",getHeight=" + getHeight() + ",w=" + w + ",h=" + h);
        }
       mDrawable.setBounds(mDrawableDst);
        Log.e("linp","@@mDrawable.getBounds().left="+mDrawable.getBounds().left+";"+"mDrawable.getBounds().top="+mDrawable.getBounds().top+";"+"mDrawable.getBounds().right="+mDrawable.getBounds().right
        		+";"+"mDrawable.getBounds().bottom="+mDrawable.getBounds().bottom
        		
        		);
        Log.e("linp"," configureBounds mDrawableDst.left="+ mDrawableDst.left+";"+" mDrawableDst.top="+ mDrawableDst.top+";"+"mDrawableDst.right="+ mDrawableDst.right+";"+"mDrawableDst.bottom="+ mDrawableDst.bottom);
       
      //  invalidate();
    }

    private void checkBounds() {
        int newLeft = mDrawableDst.left;
        int newTop = mDrawableDst.top;

        boolean isChange = false;
    
        onRebound();

        logD("checkBound", "getLeft=" + getLeft() + ",getRight=" + getRight());
        logD("checkBound", "left=" + mDrawableDst.left + ",right=" + mDrawableDst.right + ",top="
                + mDrawableDst.top + ",bottom=" + mDrawableDst.bottom);
        logD("checkBound", "width=" + mDrawableDst.width() + ",height=" + mDrawableDst.height());

    }

    private void onRebound() {
        int disX = 0, disY = 0;
        if (mDrawableDst.height() < mDrawableFloat.height()) {
            disY = (mDrawableFloat.height() - mDrawableDst.height()) / 2 - mDrawableDst.top;
        } else {
            if (mDrawableDst.top > 0) {
                disY = -mDrawableDst.top;
            }
            if (mDrawableDst.bottom < mDrawableFloat.height()) {
                disY = mDrawableFloat.height() - mDrawableDst.bottom;
            }
        }

        if (mDrawableDst.width() < mDrawableFloat.width()) {
            disX = (mDrawableFloat.width() - mDrawableDst.width()) / 2 - mDrawableDst.left;
        } else {
            if (mDrawableDst.left > 0) {
                disX = -mDrawableDst.left;
            }
            if (mDrawableDst.right < mDrawableFloat.width()) {
                disX = mDrawableFloat.width() - mDrawableDst.right;
            }
        }

        mDrawableDst.offset(disX, disY);
        invalidate();
    }

    private boolean reScale() {
        float scaleX = 1.0f;
        float scaleY = 1.0f;

        return false;
    }

    private void setRect() {

    }

    public Bitmap getCropImage() {
       	/*Log.d("Wallpaper_DEBUG", "---------getCropImage33333--getHeight = "+getHeight()+" getWidth = "+getWidth());
		Log.d("Wallpaper_DEBUG", "---------getCropImage33333--getIntrinsicHeight = "+mDrawable.getIntrinsicHeight()+" getIntrinsicWidth = "+mDrawable.getIntrinsicWidth());
    	*/Log.d("Wallpaper_DEBUG", "---------getCropImage33333--getBounds().height = "+mDrawable.getBounds().height()+" getBounds().width = "+mDrawable.getBounds().width()+
    		   								 "getBounds().left = "+mDrawable.getBounds().left+" getBounds.right = "+mDrawable.getBounds().right);
    	
    	Log.d("Wallpaper_DEBUG", "---------getCropImage33333--mDrawableDst = "+mDrawableDst);
    	Log.d("Wallpaper_DEBUG", "---------getCropImage33333--mDrawableSrc = "+mDrawableSrc);
    	Log.d("Wallpaper_DEBUG", "---------getCropImage33333--mDrawableFloat = "+mDrawableFloat);
       	final float scale = mContext.getResources().getDisplayMetrics().density;

       	int h = Math.min(getHeight(), (int) (mDrawable.getIntrinsicHeight() * scale + 0.5f));
       	//int h = mDrawable.getIntrinsicHeight();
       	int w = (int) (h / mOriRationHW);
       	//int w = mDrawable.getIntrinsicWidth();

       	int left = (getWidth() - w) / 2;
       	int top = (getHeight() - h) / 2;
       	int right = left + w;
       	int bottom = top + h;
       	
       	       
       
    	//postInvalidate();
        Bitmap tempBitmap = null;
        try {  
            tempBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);	//OutOfMemoryError
        } catch (OutOfMemoryError e) {  
            while(tempBitmap == null) {  
                System.gc();  
                System.runFinalization();  
            }  
        }  
        Log.e("linp", "@%@getCropImage="+"mDrawable.getBounds().left="+mDrawable.getBounds().left+";"+"mDrawable.getBounds().top="+mDrawable.getBounds().top+";"+
        "mDrawable.getBounds().right="+mDrawable.getBounds().right+";" +
        		"+mDrawable.getBounds().bottom="+mDrawable.getBounds().bottom);
        Canvas canvas = new Canvas(tempBitmap);
       // mDrawable.setBounds(mDrawableDst);
        if (mDrawable.getBounds().height() != getHeight() || mDrawable.getBounds().width() != getWidth()) {
			mDrawable.setBounds(left, top, right, bottom);
			/*Log.d("Wallpaper_DEBUG", "---------getCropImage22222--getHeight = "+getHeight()+" getWidth = "+getWidth());
			Log.d("Wallpaper_DEBUG", "---------getCropImage22222--getIntrinsicHeight = "+mDrawable.getIntrinsicHeight()+" getIntrinsicWidth = "+mDrawable.getIntrinsicWidth());
	    	*/Log.d("Wallpaper_DEBUG", "---------getCropImage22222--getBounds().height = "+mDrawable.getBounds().height()+" getBounds().width = "+mDrawable.getBounds().width()+
	    		   								 "getBounds().left = "+mDrawable.getBounds().left+" getBounds.right = "+mDrawable.getBounds().right);
	    	
	    	Log.d("Wallpaper_DEBUG", "---------getCropImage22222--mDrawableDst = "+mDrawableDst);
	    	Log.d("Wallpaper_DEBUG", "---------getCropImage22222--mDrawableSrc = "+mDrawableSrc);
	    	Log.d("Wallpaper_DEBUG", "---------getCropImage22222--mDrawableFloat = "+mDrawableFloat);
	    	Log.d("Wallpaper_DEBUG", "---------getCropImage22222--left = "+left+" top = "+top+" right = "+right+" bottom = "+bottom);
	    	
		} else {
			/*Log.d("Wallpaper_DEBUG", "---------getCropImage11111--getHeight = "+getHeight()+" getWidth = "+getWidth());
			Log.d("Wallpaper_DEBUG", "---------getCropImage11111--getIntrinsicHeight = "+mDrawable.getIntrinsicHeight()+" getIntrinsicWidth = "+mDrawable.getIntrinsicWidth());
	    	*/Log.d("Wallpaper_DEBUG", "---------getCropImage11111--getBounds().height = "+mDrawable.getBounds().height()+" getBounds().width = "+mDrawable.getBounds().width()+
	    		   								 "getBounds().left = "+mDrawable.getBounds().left+" getBounds.right = "+mDrawable.getBounds().right);
	    	
	    	Log.d("Wallpaper_DEBUG", "---------getCropImage11111--mDrawableDst = "+mDrawableDst);
	    	Log.d("Wallpaper_DEBUG", "---------getCropImage11111--mDrawableSrc = "+mDrawableSrc);
	    	Log.d("Wallpaper_DEBUG", "---------getCropImage11111--mDrawableFloat = "+mDrawableFloat);
	    	Log.d("Wallpaper_DEBUG", "---------getCropImage11111--left = "+left+" top = "+top+" right = "+right+" bottom = "+bottom);
		}
        
		//M:shigq Fix bug #14276, #14656 for Android5.0 start
        mDrawable.setBounds(mDrawableDst);
		//M:shigq Fix bug #14276, #14656 for Android5.0 end
        mDrawable.draw(canvas);

//        Matrix matrix = new Matrix();
//        float scale = (float) (mDrawableSrc.height()) / (float) (mDrawableDst.height());
//        matrix.postScale(scale, scale);
//
//        Bitmap ret = Bitmap.createBitmap(tempBitmap, mDrawableFloat.left, mDrawableFloat.top,
//                mDrawableFloat.width(), mDrawableFloat.height(), matrix, true);
//
//        tempBitmap.recycle();
//        tempBitmap = null;
//
//        Bitmap newRet = Bitmap.createScaledBitmap(ret, mCropWidth, mCropHeight, false);
//        ret.recycle();
//        ret = newRet;
//
//        return ret;
        return tempBitmap;
//    	return mBitmap;
    }

    public int dipTopx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 处理移动回弹
     * 
     * @param disX
     * @param disY
     */
    public void rebound(int disX, int disY) {
        trans = new TranslateAnimation(-disX, 0, -disY, 0);
        trans.setInterpolator(new AccelerateInterpolator());
        trans.setDuration(300);
        this.startAnimation(trans);
    }

    /* @Override
     public void onSystemUiVisibilityChange(int visibility) {
         // Detect when we go out of low-profile mode, to also go out
         // of full screen. We only do this when the low profile mode
         // is changing from its last state, and turning off.
         int diff = mLastSystemUiVis ^ visibility;
         mLastSystemUiVis = visibility;
         if ((diff & SYSTEM_UI_FLAG_LOW_PROFILE) != 0 && (visibility & SYSTEM_UI_FLAG_LOW_PROFILE) == 0) {
             setNavVisibility(true);
         }
     }

     @Override
     protected void onWindowVisibilityChanged(int visibility) {
         super.onWindowVisibilityChanged(visibility);

         // When we become visible, we show our navigation elements briefly
         // before hiding them.
         setNavVisibility(true);
         getHandler().postDelayed(mNavHider, 2000);
     }*/

    /**
     * 处理状态栏显示/隐藏
     * 
     * @param visible
     */
    /*private void setNavVisibility(boolean visible) {
        int newVis = mBaseSystemUiVisibility;
        if (!visible) {
            newVis |= SYSTEM_UI_FLAG_LOW_PROFILE | SYSTEM_UI_FLAG_FULLSCREEN;
        }
        final boolean changed = newVis == getSystemUiVisibility();

        // Unschedule any pending event to hide navigation if we are
        // changing the visibility, or making the UI visible.
        if (changed || visible) {
            Handler h = getHandler();
            if (h != null) {
                h.removeCallbacks(mNavHider);
            }
        }

        // Set the new desired visibility.
        setSystemUiVisibility(newVis);
    }*/
    /*private void toggleActionBarVisibility() {
        Log.d("count", "toggleActionBarVisibility");
        final int vis = getSystemUiVisibility();
        if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
            setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            Log.d("count", "toggleActionBarVisibility false");
        } else {
            Log.d("count", "toggleActionBarVisibility true");
            setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
    }*/

    /*public interface ActionBarCallBack {
        void toggleActionBarVisibility();
    }*/

    /*public void setActionBarCallBack(ActionBarCallBack callBack) {
        mActionBarCallBack = callBack;
    }*/

    private void logD(String title, String body) {
        if (DEBUG) {
            Log.d(TAG, title + ":" + body);
        }
    }

    private Drawable rotationDrawable(Drawable drawable, float rotation) {
        try {
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();
            Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                    : Bitmap.Config.RGB_565;
            Bitmap bitmap = Bitmap.createBitmap(width, height, config);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, width, height);
            drawable.draw(canvas);
            Bitmap newBitmap = adjustPhotoRotation(bitmap, rotation);
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
            return new BitmapDrawable(newBitmap);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return drawable;
        } catch (Exception e) {
            e.printStackTrace();
            return drawable;
        }
    }

    private Bitmap adjustPhotoRotation(Bitmap bm, final float orientationDegree) {
        try {
            /*Matrix m = new Matrix();
            m.setRotate(orientationDegree, ( float ) bm.getWidth() / 2, ( float ) bm.getHeight() / 2);
            float targetX, targetY;
            if (orientationDegree == 90) {
                targetX = bm.getHeight();
                targetY = 0;
            }else {
                targetX = bm.getHeight();
                targetY = bm.getWidth();
            }
            
            final float[] values = new float[9];
            m.getValues(values);
            float x1 = values[Matrix.MTRANS_X];
            float y1 = values[Matrix.MTRANS_Y];
            Bitmap bm1 = Bitmap.createBitmap(bm.getHeight(), bm.getWidth(), Bitmap.Config.ARGB_8888);
            m.postTranslate(targetX - x1, targetY - y1);*/
            Matrix m = new Matrix();
            int height = bm.getHeight();
            int width = bm.getWidth();
            m.setRotate(orientationDegree, ( float ) width / 2, ( float ) height / 2);
            float targetX = 0;
            float targetY = 0;
            if (orientationDegree == 90 || orientationDegree == 270) {
                if (width > height) {
                    targetX = ( float ) height / 2 - ( float ) width / 2;
                    targetY = 0 - targetX;
                } else {
                    targetY = ( float ) width / 2 - ( float ) height / 2;
                    targetX = 0 - targetY;
                }
            }
            m.postTranslate(targetX, targetY);
            Bitmap bm1 = Bitmap.createBitmap(bm.getHeight(), bm.getWidth(), Bitmap.Config.ARGB_8888);
            Paint paint = new Paint();
            Canvas canvas = new Canvas(bm1);
            canvas.drawBitmap(bm, m, paint);
            return bm1;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return bm;
        } catch (Exception e) {
            return bm;
        }
    }
}
