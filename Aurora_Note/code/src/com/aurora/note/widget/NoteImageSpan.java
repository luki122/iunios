
package com.aurora.note.widget;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;
import android.util.Log;

import com.aurora.note.NoteApp;
import com.aurora.note.R;
import com.aurora.note.util.BitmapUtil;

public class NoteImageSpan extends ImageSpan {
    private static final String TAG = "NoteImageSpan";

    private static final int DEFAULT_OFFSET = 5;
    private int mOffset;

    private int mLeft, mTop;

    protected boolean mSelected = false;

    private Type mType;

    private Rect mOriginalDrawableBounds;
    
    protected int mExpectedWidth;
    protected int mExpectedHeight;
    protected int mRightExtraSpace; // 一个空格的宽度
    protected int mLeftPadding;
    protected int mTopPadding;
    
    protected Drawable mSelectedDrawable;

    public static enum Type {
        Type_Preset_Image,
        Type_Sound,
        Type_Picture,
        Type_Video,
        Type_Sign
    }

    protected Drawable mDrawable;

    public NoteImageSpan(Drawable d, String source, Type type, int rightExtraSpace,
            int leftPadding, int topPadding) {
        this(d, -1, source, type, -1, -1, rightExtraSpace, leftPadding, topPadding);
    }
    
    public NoteImageSpan(Drawable d, int selectedDrawableResId, String source, Type type, int rightExtraSpace,
            int leftPadding, int topPadding) {
        this(d, selectedDrawableResId, source, type, -1, -1, rightExtraSpace, leftPadding, topPadding);
    }
    
    public NoteImageSpan(Drawable d, Drawable selectedDrawable, String source, Type type, int rightExtraSpace,
            int leftPadding, int topPadding) {
        this(d, selectedDrawable, source, type, -1, -1, rightExtraSpace, leftPadding, topPadding);
    }
    
    public NoteImageSpan(Drawable d, String source, Type type, int expectedWidth, int expectedHeight,
            int rightExtraSpace, int leftPadding, int topPadding) {
        this(d, -1, source, type, expectedWidth, expectedHeight, rightExtraSpace, leftPadding, topPadding);
    }
    
    public NoteImageSpan(Drawable d, int selectedDrawableResId, String source, Type type, int expectedWidth, int expectedHeight,
            int rightExtraSpace, int leftPadding, int topPadding) {
        this(d, getDrawable(selectedDrawableResId), source,
                type, expectedWidth, expectedHeight, rightExtraSpace, leftPadding, topPadding);
    }

    public NoteImageSpan(Drawable d, Drawable selectedDrawable, String source, Type type, int expectedWidth, int expectedHeight,
            int rightExtraSpace, int leftPadding, int topPadding) {
        super(d, source);
        mDrawable = d;
        mType = type;
        mExpectedWidth = expectedWidth;
        mExpectedHeight = expectedHeight;
        mRightExtraSpace = rightExtraSpace;
        mLeftPadding = leftPadding;
        mTopPadding = topPadding;
        
        if (d != null) {
            mOriginalDrawableBounds = d.copyBounds();
            
            if (mExpectedWidth == -1) {
                mExpectedWidth = d.getBounds().width();
            }
            
            if (mExpectedHeight == -1) {
                mExpectedHeight = d.getBounds().height();
            }
        }

        final Resources res = NoteApp.ysApp.getResources();
        mOffset = res.getDimensionPixelSize(
                R.dimen.new_note_image_span_offset);
        if (mOffset <= 0) {
            mOffset = DEFAULT_OFFSET;
        }
        
        mSelectedDrawable = selectedDrawable;
    }
    
    private static Drawable getDrawable(int drawableResId) {
        final Resources res = NoteApp.ysApp.getResources();
        if (drawableResId > -1) {
            return res.getDrawable(drawableResId);
        }
        
        return null;
    }

    protected void setLeft(int left) {
        mLeft = left;
    }

    protected void setTop(int top) {
        mTop = top;
    }

    public int getLeft() {
        return mLeft;
    }

    public int getTop() {
        return mTop;
    }

    public int getRight() {
        return getLeft() + getWidth();
    }

    public int getBottom() {
        return getTop() + getHeight();
    }

    public int getWidth() {
        return getDrawable().getBounds().width();
    }

    public int getHeight() {
        return getDrawable().getBounds().height();
    }

    public int getOffset() {
        return mOffset;
    }

    public Type getType() {
        return mType;
    }

    public void setSelected(boolean selected) {
        mSelected = selected;
    }

    public boolean isSelected() {
        return mSelected;
    }

    public Rect getOriginalBounds() {
        return mOriginalDrawableBounds;
    }

    public void restoreOriginalBounds() {
        getDrawable().setBounds(mOriginalDrawableBounds);
    }

    public boolean contains(int x, int y) {
        return ((x >= getLeft() && x <= getRight()) && (y >= getTop() && y <= getBottom()));
    }

    public String getSource(int x, int y) {
        if (contains(x, y)) {
            return getSource();
        }
        
        return null;
    }

    public Type getType(int x, int y) {
        if (contains(x, y)) {
            return getType();
        }
        
        return null;
    }

    public NoteImageSpan getSpan(int x, int y) {
        if (contains(x, y)) {
            return this;
        }
        
        return null;
    }

    @Override
    public Drawable getDrawable() {
        return mDrawable;
    }

    public void setDrawable(Drawable d) {
        mDrawable = d;
    }

    protected static boolean resize(NoteImageSpan span, int targetWidth, int targetHeight) {
        Drawable d = span.getDrawable();
        if (d != null) {
            if (d instanceof BitmapDrawable) {
                // 图片还未经过处理
                BitmapDrawable bd = (BitmapDrawable) d;
                Bitmap bitmap = bd.getBitmap();
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                if (width != targetWidth || height != targetHeight) {
                    Log.d(TAG, "Jim, reize, before scale: width: " + width + ", height: " + height);
                    bitmap = BitmapUtil.scaleBitmap(bitmap, targetWidth, targetHeight);
                    width = bitmap.getWidth();
                    height = bitmap.getHeight();
                    Log.d(TAG, "Jim, reize, after scale: width: " + width + ", height: " + height);
                    if (width > targetWidth || height > targetHeight) {
                        bitmap = BitmapUtil.cropBitmap(bitmap, targetWidth, targetHeight);
                        Log.d(TAG, "Jim, reize, after crop: width: " + bitmap.getWidth() + ", height: " + bitmap.getHeight());
                    }
                    
                    bd = new BitmapDrawable(NoteApp.ysApp.getResources(), bitmap);
                    bd.setBounds(0, 0, bd.getIntrinsicWidth(), bd.getIntrinsicHeight());
                    span.setDrawable(bd);

                    return true;
                }
            } else {
                Rect bounds = d.getBounds();
                if (bounds.width() != targetWidth || bounds.height() != targetHeight) {
                    d.setBounds(0, 0, targetWidth, targetHeight);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, FontMetricsInt fm) {
        int width;
        if (mType == Type.Type_Picture || mType == Type.Type_Preset_Image) {
            Drawable d = getDrawable();
            Rect rect = d.getBounds();
            if ((d instanceof BitmapDrawable) &&
                    (d.getBounds().bottom > mExpectedHeight || d.getBounds().right > mExpectedWidth) &&
                    resize(this, mExpectedWidth, mExpectedHeight)) {
                d = getDrawable();
                rect = d.getBounds();
            }
            
            if (paint != null && fm != null) {
                paint.getFontMetricsInt(fm);
            }
            if (fm != null) {
                fm.ascent = -rect.bottom;
//                fm.descent = 0;

                fm.top = fm.ascent;
//                fm.bottom = 0;
            }
            
            width = rect.right;
            if (mRightExtraSpace > 0) {
                width += mRightExtraSpace;
            }
            
            return width;
        } else if (mType == Type.Type_Sign) {
            Drawable d = getDrawable();
            Rect rect = d.getBounds();
            if (fm != null && rect.height() > (fm.descent - fm.ascent)) {
                fm.ascent = -rect.bottom;
                fm.descent = 0;

                fm.top = fm.ascent;
                fm.bottom = 0;
            } else {
                if (paint != null && fm != null) {
                    paint.getFontMetricsInt(fm);
                }
            }
            
            width = rect.right;
            
            return width;
        } else {
            Drawable d = getDrawable();
            Rect rect = d.getBounds();
            if (paint != null && fm != null) {
                paint.getFontMetricsInt(fm);
            }
            if (fm != null) {
                fm.ascent = -rect.bottom;
//                fm.descent = 0;

                fm.top = fm.ascent;
//                fm.bottom = 0;
            }
            
            width = rect.right;
            if (mRightExtraSpace > 0) {
                width += mRightExtraSpace;
            }
            
            return width;
        }
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top,
            int y, int bottom, Paint paint) {
        Drawable b = getDrawable();
        int transY;
        canvas.save();
        if (mType == Type.Type_Sign) {
            transY = top + (y - top - b.getBounds().height()) / 2 + mOffset;
        } else {
            transY = top + mOffset;
        }
        canvas.translate(x, transY);
        setLeft(((int) x) + mLeftPadding);
        setTop(transY + mTopPadding);
        b.draw(canvas);
        if (Type.Type_Picture == mType && mSelected && mSelectedDrawable != null) {
            mSelectedDrawable.setBounds(b.getBounds());
            mSelectedDrawable.draw(canvas);
        }
//        drawDrawableBounds(canvas, b, this, paint);
        canvas.restore();        
    }

    protected static void drawDrawableBounds(Canvas canvas, Drawable drawable, NoteImageSpan span,
            Paint paint) {
        int color = paint.getColor();
        Style style = paint.getStyle();
        float textSize = paint.getTextSize();
        paint.setColor(Color.BLUE);
        paint.setStyle(Style.STROKE);
        canvas.drawRect(drawable.getBounds(), paint);
        paint.setTextSize(30);
        paint.setColor(Color.RED);
        String text = "[" + span.getLeft() + ", " + span.getTop() + " - " + span.getRight() + ", "
                + span.getBottom() + "]";
        canvas.drawText(text, 30, 30, paint);
        paint.setColor(color);
        paint.setStyle(style);
        paint.setTextSize(textSize);
    }    
}
