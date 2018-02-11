package com.android.gallery3d.plugin.tuYa.ui;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.android.gallery3d.common.Utils;
import com.android.gallery3d.plugin.tuYa.app.DrawingManager;
import com.android.gallery3d.plugin.tuYa.app.SimplePoint;
import com.android.gallery3d.plugin.tuYa.drawings.Drawing;
import com.android.gallery3d.plugin.tuYa.drawings.DrawingFactory;

public class TuYaView extends View {
    private static final float TOUCH_TOLERANCE = 4;
    private static final String TAG = "TuYaView";
    private static final boolean DEBUG = true;
    private Canvas mTuYaCanvas;
    private Bitmap mTuYaBitmap;

    private Paint mBitmapPaint;
    private Bitmap mOriginalBitmap;
    private RectF mShowRectF;

    private int mCurDrawingId;
    private Drawing mCurDrawing;
    private DrawingManager mDrawingManager;

    private SimplePoint mDownPoint = new SimplePoint();
    private SimplePoint mFirstPoint = new SimplePoint();
    private SimplePoint mCurPoint = new SimplePoint();

    private OnSizeChangeListener mOnSizeChangeListener;

    public interface OnSizeChangeListener {
        public void onSizeChange(int w, int h);
    }

    public TuYaView(Context context) {
        this(context, null);
    }

    public TuYaView(Context context, AttributeSet attr) {
        super(context, attr);
        init();
    }

    private void init() {
        mDrawingManager = new DrawingManager();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        mShowRectF = new RectF();
    }

    public void setOnSizeChangeListener(OnSizeChangeListener listener) {
        mOnSizeChangeListener = listener;
    }

    public void setCurDrawingId(int id) {
        mCurDrawingId = id;
    }

    public int getCurDrawingId() {
        return mCurDrawingId;
    }

    public void undo() {
        if (mDrawingManager.hasModifications()) {
            reset((int) mShowRectF.width(), (int) mShowRectF.height());
            mDrawingManager.undo(mTuYaCanvas);
            invalidate();
        }
    }

    public void redo() {
        reset((int) mShowRectF.width(), (int) mShowRectF.height());
        mDrawingManager.redo(mTuYaCanvas);
        invalidate();
    }

    public void reDraw() {
        if (mDrawingManager.hasModifications()) {
            reset((int) mShowRectF.width(), (int) mShowRectF.height());
            mDrawingManager.clear();
            invalidate();
        }
    }

    public void setTuYaBitmap(Bitmap bitmap) {
        mOriginalBitmap = bitmap;
        calculateShowRectF(bitmap);
        int showWidth = (int) mShowRectF.width();
        int showHeight = (int) mShowRectF.height();
        mDrawingManager.setShowSize(showWidth, showHeight);
        reset(showWidth, showHeight);
        invalidate();

    }

    private void calculateShowRectF(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int viewW = getWidth();
        int viewH = getHeight();
        float scale = Math.min(((float) viewW) / w, ((float) viewH) / h);
        float targetW = w * scale;
        float targetH = h * scale;
        float left = (viewW - targetW) / 2;
        float top = (viewH - targetH) / 2;
        if (DEBUG) {
            Log.d(TAG, "setTuYaBitmap w = " + w + ",h = " + bitmap.getHeight() + ",scale = " + scale
                    + ",viewW = " + viewW + ",viewH = " + viewH + ",targetW = " + targetW + ",targetH = "
                    + targetH + ",left = " + left + ",top = " + top);
        }
        mShowRectF.set(left, top, left + targetW, top + targetH);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (DEBUG) {
            Log.d(TAG, "w = " + w + ",h = " + h + ",oldw = " + oldw + ",oldh = " + oldh);
        }
        if (mOnSizeChangeListener != null) {
            mOnSizeChangeListener.onSizeChange(w, h);
            mOnSizeChangeListener = null;
        }
    }

    private void reset(int w, int h) {
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mTuYaBitmap = bitmap;
        Canvas canvas = new Canvas(bitmap);
        mTuYaCanvas = canvas;
        Rect src = new Rect(0, 0, mOriginalBitmap.getWidth(), mOriginalBitmap.getHeight());
        Rect dst = new Rect(0, 0, w, h);
        canvas.drawBitmap(mOriginalBitmap, src, dst, mBitmapPaint);
    }

    public DrawingManager getDrawingManager() {
        return mDrawingManager;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mTuYaBitmap != null) {
            mTuYaBitmap.recycle();
            mTuYaBitmap = null;
        }

        if (mOriginalBitmap != null) {
            mOriginalBitmap.recycle();
            mOriginalBitmap = null;
        }

        mDrawingManager.clear();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mTuYaCanvas == null) {
            return false;
        }
        event.offsetLocation(-mShowRectF.left, -mShowRectF.top);
        transform(event);
        event.offsetLocation(mShowRectF.left, mShowRectF.top);

        return true;
    }

    private void transform(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handlerDown(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                handlerMove(x, y);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                handlerUp(x, y);
                break;
            default:
                break;
        }
    }

    private void handlerDown(float x, float y) {
        Utils.assertTrue(mCurDrawing == null);
        if (contains(x, y)) {
            mFirstPoint.setXY(x, y);
        }
        mDownPoint.setXY(x, y);
    }

    private void handlerMove(float x, float y) {
        if (mCurDrawing == null) {
            if (!contains(x, y)) {
                mFirstPoint.setEmpty();
                return;
            }
            if (mFirstPoint.isEmpty()) {
                mFirstPoint.setXY(x, y);
                return;
            }
            mCurPoint.setXY(x, y);
            if (!isMove(mFirstPoint, mCurPoint)) {
                return;
            }
            // main logic
            if (mCurDrawing == null) {
                initCurDawing(mDownPoint.getX(), mDownPoint.getY());
            }
        } else {
            mCurDrawing.fingerMove(x, y);
            invalidate();
        }
    }

    private boolean isMove(SimplePoint firstPoint, SimplePoint curPoint) {
        float dx = Math.abs(curPoint.getX() - firstPoint.getX());
        float dy = Math.abs(curPoint.getY() - firstPoint.getY());
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            return true;
        }
        return false;
    }

    private void handlerUp(float x, float y) {
        if (mCurDrawing == null) {
            cleanPointState();
            return;
        }
        mCurDrawing.fingerUp(x, y);
        mCurDrawing.apply(mTuYaCanvas);
        mDrawingManager.push(mCurDrawing);
        mCurDrawing = null;
        cleanPointState();
        invalidate();
    }

    private void cleanPointState() {
        mFirstPoint.setEmpty();
        mDownPoint.setEmpty();
        mCurPoint.setEmpty();
    }

    private boolean contains(float x, float y) {
        return x >= 0 && x < mShowRectF.width() && y >= 0 && y < mShowRectF.height();
    }

    private void initCurDawing(float x, float y) {
        mCurDrawing = DrawingFactory.createDrawing(mCurDrawingId);
        mCurDrawing.fingerDown(x, y);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mTuYaBitmap != null) {
            canvas.drawBitmap(mTuYaBitmap, mShowRectF.left, mShowRectF.top, mBitmapPaint);
        }
        if (mCurDrawing != null) {
            canvas.clipRect(mShowRectF);
            canvas.translate(mShowRectF.left, mShowRectF.top);
            mCurDrawing.apply(canvas);
            canvas.translate(-mShowRectF.left, -mShowRectF.top);
        }
    }

    public Bitmap getOriginalBitmap() {
        return mOriginalBitmap;
    }

    public RectF getShowRectF() {
        return mShowRectF;
    }
}
