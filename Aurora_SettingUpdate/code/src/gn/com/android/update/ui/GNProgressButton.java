/**
 * @name:GNProgressButton.java
 * @author:wangpf
 * @see:This class in order to achieve progress bar button
 * @createdate：2013-04-11
 */
// Gionee <wangpf> <2013-04-11> modify for CR00797077 begin
package gn.com.android.update.ui;

import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import gn.com.android.update.R;
import gn.com.android.update.utils.LogUtils;

public class GNProgressButton extends TextView {
    private final static String TAG = "GNProgressButton";
    private final static int TIMERTASK_DELAY = 0;
    private final static int TIMERTASK_PERIOD = 45;
    private final static int TIMERTASK_ROATE = 10;
    private Paint mProgressPaint;
    private RectF mRoundOval;
    private Paint mBottomPaint;
    private int mPaintWidth;
    private int mPaintColor;
    private int mStartProgress;
    private int mCurProgress;
    private int mEndProgress;
    private boolean mFillPaint;
    private int mSidePaintInterval;
    private boolean mShowBack;
    private int mSaveMax;
    private Resources mResources;
    private boolean mPause = true;
    private TimerTask mTimerTask;
    private Timer mTimer;
    private Bitmap mBitmap;
    private int mRoate = 0;
    private float mCCircleX, mCCircleY, mRadius;

    public GNProgressButton(Context context) {
        super(context);
        init();
    }

    public GNProgressButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ProgressButton);

        mEndProgress = array.getInt(R.styleable.ProgressButton_max, 100);
        mSaveMax = mEndProgress;
        mFillPaint = array.getBoolean(R.styleable.ProgressButton_fill, true);
        if (mFillPaint == false) {
            mProgressPaint.setStyle(Paint.Style.STROKE);
            mBottomPaint.setStyle(Paint.Style.STROKE);
        }

        mSidePaintInterval = array.getInt(R.styleable.ProgressButton_insideinterval, 0);

        mShowBack = array.getBoolean(R.styleable.ProgressButton_showbottom, true);

        mPaintWidth = array.getInt(R.styleable.ProgressButton_paintwidth, 10);
        if (mFillPaint) {
            mPaintWidth = 0;
        }

        mProgressPaint.setStrokeWidth(mPaintWidth);
        mProgressPaint.setMaskFilter(new BlurMaskFilter(2, BlurMaskFilter.Blur.SOLID));
        mBottomPaint.setStrokeWidth(mPaintWidth);
        mPaintColor = array.getColor(R.styleable.ProgressButton_paintcolor, 0xffffcc00);
        mProgressPaint.setColor(mPaintColor);

        array.recycle(); // very important
    }

    private void init() {
        mPaintWidth = 0;
        mPaintColor = getResources().getInteger(R.color.paint_color);

        mProgressPaint = new Paint();
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setStyle(Paint.Style.FILL);

        mProgressPaint.setStrokeWidth(mPaintWidth);
        mProgressPaint.setColor(mPaintColor);

        mBottomPaint = new Paint();
        mBottomPaint.setAntiAlias(true);
        mBottomPaint.setStyle(Paint.Style.FILL);
        mBottomPaint.setStrokeWidth(mPaintWidth);
        mBottomPaint.setColor(Color.GRAY);

        mStartProgress = -90;
        mCurProgress = 0;
        mEndProgress = 100;
        mSaveMax = 100;

        mFillPaint = true;
        mShowBack = true;

        mSidePaintInterval = 0;
        mRoundOval = new RectF(0, 0, 0, 0);

        mResources = getResources();
        setGravity(Gravity.CENTER);
        setTextColor(mResources.getColor(R.color.progress_text_color));

        if (mResources != null) {
            if (mBitmap != null && !mBitmap.isRecycled()) {
                mBitmap.recycle();
            }
            try {
                mBitmap = BitmapFactory.decodeResource(mResources, R.drawable.download_progress_anim);
            } catch (OutOfMemoryError e) {
                LogUtils.logd(TAG, "create bitmap failed!");
            }
        }
    }

    public void setPause(boolean pause) {
        mPause = pause;
        if (mPause) {
            cancelTimer();
        } else {
            startTimer();
        }
    }

    public void setTextString(String text) {
        setTextSize(mResources.getDimension(R.dimen.progress_text_start_size));
        setText(text);
        invalidate();
    }

    public synchronized void setProgress(int progress) {
        mCurProgress = progress;
        if (mCurProgress < 0) {
            mCurProgress = 0;
        }

        if (mCurProgress > mEndProgress) {
            mCurProgress = mEndProgress;
        }
        setTextSize(mResources.getDimension(R.dimen.progress_text_size));
        StringBuffer sb = new StringBuffer(String.valueOf(mCurProgress));
        sb.append("%");
        setText(sb);
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (mSidePaintInterval != 0) {
            mRoundOval.set(mPaintWidth / 2 + mSidePaintInterval, mPaintWidth / 2
                    + mSidePaintInterval,
                    w - mPaintWidth / 2 - mSidePaintInterval, h - mPaintWidth / 2
                            - mSidePaintInterval);
        } else {
            int sl = getPaddingLeft();
            int sr = getPaddingRight();
            int st = getPaddingTop();
            int sb = getPaddingBottom();
            mRoundOval.set(sl + mPaintWidth / 2, st + mPaintWidth / 2, w - sr - mPaintWidth / 2, h
                    - sb - mPaintWidth / 2);
        }
        int width = getWidth();
        mCCircleX = width / 2.0f;
        mCCircleY = getHeight() / 2.0f;
        mRadius = width / 2.0f - mSidePaintInterval;
        setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        float x = Math.abs(mCCircleX - event.getX());
                        float y = Math.abs(mCCircleY - event.getY());
                        float len = (float) Math.sqrt(x * x + y * y);
                        if (len <= mRadius) {
                            return false;
                        } else {
                            return true;
                        }
                    default:
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        int id = enabled ? R.drawable.download_button_bg : R.drawable.download_button_nenable;
        setBackgroundDrawable(mResources.getDrawable(id));
        int color = enabled ? Color.BLACK : Color.GRAY;
        setTextColor(color);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
        getBackground().setCallback(null);
        LogUtils.logd(TAG, "onDetachedFromWindow()  ");
        cancelTimer();
        mResources = null;
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mShowBack) {
            canvas.drawArc(mRoundOval, 0, 360, mFillPaint, mBottomPaint);
        }
        float rate = (float) mCurProgress / mEndProgress;
        float sweep = 360 * rate;
        canvas.drawArc(mRoundOval, mStartProgress, sweep, mFillPaint, mProgressPaint);
        if (!mPause) {
            canvas.save();
            canvas.rotate(mRoate, getWidth() / 2, getHeight() / 2);
            canvas.drawBitmap(mBitmap, 0, 0, new Paint());
            canvas.restore();
        }
    }

    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            mRoate += TIMERTASK_ROATE;
            if (mRoate >= 360) {
                mRoate = 0;
            }
            postInvalidate();
        }
    }

    private void cancelTimer() {
        LogUtils.logd(TAG, "cancelTimer mTimer = " + (mTimer == null));
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void startTimer() {
        LogUtils.logd(TAG, "startTimer() mTimer = " + (mTimer == null));
        if (mTimer == null) {
            mTimer = new Timer();
        }

        if (mTimerTask == null) {
            mTimerTask = new MyTimerTask();
            mTimer.schedule(mTimerTask, TIMERTASK_DELAY, TIMERTASK_PERIOD);
        }

    }
}
