
package com.aurora.community.widget;

import android.content.Context;
import android.util.AttributeSet;

/**
 * 带删除按钮的EditText
 * 
 * @author JimXia 2014-9-22 下午2:34:13
 */
public class ClearableEditText extends AuroraEditText {
    // private static final String TAG = "ClearableEditText";
//    private Drawable mClearDrawable;
//    private final Rect mClickBounds = new Rect(); // 点击区域
//    private boolean mIsClearDrawableVisible = false;
//    
//    private static final int[] STATE_PRESSED = new int[] {android.R.attr.state_pressed};
//    private static final int[] STATE_NORMAL = StateSet.NOTHING;

    public ClearableEditText(Context context) {
        this(context, null);
    }

    public ClearableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
//        final Resources res = context.getResources();
//        mClearDrawable = res.getDrawable(
//                R.drawable.clearable_edittext_clear_selector);
        setIsNeedDeleteAll(true);
//        setCompoundDrawablePadding(res.getDimensionPixelSize(R.dimen.padding_15));
    }
    
    @Override
    protected void onDetachedFromWindow() {
        getText().clearSpans();
        super.onDetachedFromWindow();
    }

    /*
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsClearDrawableVisible) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mClickBounds.contains(x, y) && mClearDrawable.isStateful()) {
                        mClearDrawable.setState(STATE_PRESSED);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!mClickBounds.contains(x, y) && mClearDrawable.isStateful()) {
                        mClearDrawable.setState(STATE_NORMAL);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (mClearDrawable.isStateful()) {
                        mClearDrawable.setState(STATE_NORMAL);
                    }
                    if (mClickBounds.contains(x, y)) {
                        setText("");
                        event.setAction(MotionEvent.ACTION_CANCEL);
                    }
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mClearDrawable != null && isFocused() && !TextUtils.isEmpty(getText().toString())) {
//            final int compoundPaddingTop = getCompoundPaddingTop();
//            final int compoundPaddingBottom = getCompoundPaddingBottom();
//            final int bottom = mBottom;
//            final int top = mTop;
//            
//            final boolean isLayoutRtl = isLayoutRtl();
//            final int offset = getHorizontalOffsetForDrawables();
//            final int rightOffset = isLayoutRtl ? offset : 0 ;
//            int vspace = bottom - top - compoundPaddingBottom - compoundPaddingTop;
//            canvas.save();
//            canvas.translate(mScrollX + mRight - mLeft - mPaddingRight
//                    - mClearDrawable.getBounds().width() - rightOffset,
//                    mScrollY + compoundPaddingTop + (vspace - mClearDrawable.getBounds().height()) / 2);
            mClearDrawable.draw(canvas);
//            canvas.restore();
            
            mIsClearDrawableVisible = true;
            mClearDrawable.setCallback(this);
        } else {
            mIsClearDrawableVisible = false;
            mClearDrawable.setCallback(null);
        }
        
//        debugClickArea(canvas);
    }
    
    protected void debugClickArea(Canvas canvas) {
        canvas.save();
        canvas.clipRect(mClickBounds);
        canvas.drawColor(0x66ff0000);
        canvas.restore();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mClearDrawable != null) {
            int drawableWidth = mClearDrawable.getIntrinsicWidth();
            int drawableHeight = mClearDrawable.getIntrinsicHeight();
            int drawableRight = (right - left) - getCompoundPaddingRight();
            int drawableTop = ((bottom - top) - drawableHeight) / 2;
            mClearDrawable.setBounds(drawableRight - drawableWidth, drawableTop, drawableRight, drawableTop + drawableHeight);
//            mClearDrawable.setBounds(0, 0, mClearDrawable.getIntrinsicWidth(), mClearDrawable.getIntrinsicHeight());
            
            // 放大点击区域
            mClickBounds.set(drawableRight - drawableWidth - getCompoundPaddingRight(), 0,
                    right - left, bottom - top);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mClearDrawable.setCallback(null);
        mClearDrawable = null;
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        if (mClearDrawable == who) {
            return true;
        }
        
        return super.verifyDrawable(who);
    }
    */
}
