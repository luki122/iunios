package com.aurora.mms.ui;
// Aurora xuyong 2014-07-29 created for aurora's new feature
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import com.android.mms.R;

import com.aurora.utils.DensityUtil;

import android.util.Log;

public class AuroraSendMsgImageView extends ImageView {
    
    public static final String FLAG = "AuroraSendMsgImageView";
    
    public static final int LEFT_POSITION = 0;
    public static final int RIGHT_POSITION = 1;
    
    private static final int STATE_ENABLED = android.R.attr.state_enabled;
    private static final int STATE_PRESSED = android.R.attr.state_pressed;
    
    private boolean mIsHugeMode = false;
    private boolean mEnabled = false;
    private boolean mPressed = false;
    
    private int mPosition = -1;
    private int mDrawBoundLeft  = 0;
    private int mDrawBoundRight = 0;
    private int mDrawBoundTop = 0;
    private int mDrawBoundBottom = 0;
    
    private Drawable mBgDrawble;
    
    private int mAnimationWidth, mAnimationHeight;
    
    private Context mContext;
    
    public AuroraSendMsgImageView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        mContext = context;
        initBgDrawableAndPaddings(context);
    }
    
    public AuroraSendMsgImageView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        mContext = context;
        initBgDrawableAndPaddings(context);
    }
    
    public void setPosition(int position) {
        mPosition = position;
    }
    
    public void setHugeMode(boolean mode) {
        mIsHugeMode = mode;
        invalidate();
    }
    // Aurora xuyong 2014-09-16 added for aurora's new feature start
    public boolean isHugeMode() {
        return mIsHugeMode;
    }
    // Aurora xuyong 2014-09-16 added for aurora's new feature end
    @Override
    public void setEnabled(boolean enabled) {
        // TODO Auto-generated method stub
        mEnabled = enabled;
        super.setEnabled(enabled);
    }

    
    private void initBgDrawableAndPaddings(Context context) {
        if (mEnabled) {
            if (mPressed) {
                if (mIsHugeMode) {
                    mBgDrawble = context.getResources().getDrawable(R.drawable.aurora_mode_huge_bg_pressed);
                } else {
                    mBgDrawble = context.getResources().getDrawable(R.drawable.aurora_mode_small_bg_pressed);
                }
            } else {
                if (mIsHugeMode) {
                    mBgDrawble = context.getResources().getDrawable(R.drawable.aurora_mode_huge_bg_normal);
                } else {
                    mBgDrawble = context.getResources().getDrawable(R.drawable.aurora_mode_small_bg_normal);
                }
            }
        } else {
            mBgDrawble = context.getResources().getDrawable(R.drawable.aurora_mode_small_bg_disabled);
        }
        if (mBgDrawble != null) {
            mAnimationWidth  = mBgDrawble.getIntrinsicWidth();
            mAnimationHeight = mBgDrawble.getIntrinsicHeight();
        }
        if (mIsHugeMode) {
            if (mPosition == LEFT_POSITION) {
                mDrawBoundLeft  = (int)(context.getResources().getDimension(R.dimen.aurora_send_huge_leftpadding));
                mDrawBoundRight = (int)(context.getResources().getDimension(R.dimen.aurora_send_huge_rightpadding));    
            } else {
                mDrawBoundLeft  = (int)(context.getResources().getDimension(R.dimen.aurora_send_huge_rightpadding));
                mDrawBoundRight = (int)(context.getResources().getDimension(R.dimen.aurora_send_huge_leftpadding));
            }
        } else {
            if (mPosition == LEFT_POSITION) {
                mDrawBoundLeft  = (int)(context.getResources().getDimension(R.dimen.aurora_send_small_leftpadding));
                mDrawBoundRight = (int)(context.getResources().getDimension(R.dimen.aurora_send_small_rightpadding));    
            } else {
                mDrawBoundLeft  = (int)(context.getResources().getDimension(R.dimen.aurora_send_small_rightpadding));
                mDrawBoundRight = (int)(context.getResources().getDimension(R.dimen.aurora_send_small_leftpadding));
            }
        }
    }
    
    @Override 
    protected void onDraw(Canvas canvas) {
        canvas.save();
        initBgDrawableAndPaddings(mContext);
        final int w = getWidth();
        final int h = getHeight();
        int left = mDrawBoundLeft;
        int right = w - mDrawBoundRight;
        int top = (h - mAnimationWidth) / 2;
        int bottom = top + mAnimationHeight;
        if (mBgDrawble != null) {
            mBgDrawble.setBounds(left, top, right, bottom);
            mBgDrawble.draw(canvas);
        }
        super.onDraw(canvas);
    }
    
    private void setModescale() {
        
    }
    
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                 mPressed = true;
                 initBgDrawableAndPaddings(mContext);
                 invalidate();
                 break;
            case MotionEvent.ACTION_UP:
                 mPressed = false;
                 initBgDrawableAndPaddings(mContext);
                 invalidate();
                 break;
        }
    
        return super.onTouchEvent(ev);
    }


}
