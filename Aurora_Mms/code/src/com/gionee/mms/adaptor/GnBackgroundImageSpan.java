package com.gionee.mms.adaptor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ReplacementSpan;

/**
 * combine ImageSpan and BackgroundColorSpan, 
 * make this a ReplacementSpan as to make do not break the text and NOT effect by the bidi algorithmss
 *
 */
public class GnBackgroundImageSpan extends ReplacementSpan implements ParcelableSpan{
    private Drawable mDrawable;
    private int mImageId;
    private int mWidth = -1;
    private final int BACKGROUND_IMAGE_SPAN = 19; //TextUtils.BACKGROUND_IMAGE_SPAN = 19;
    
    /**
     * new BackgroundImageSpan use resource id and Drawable     
         * @hide
     * @param id the drawable resource id
     * @param drawable Drawable related to the id
     */
    public GnBackgroundImageSpan(int id, Drawable drawable) {
        mImageId = id;
        mDrawable = drawable;
    }
    /**
     * @hide
     */
    public GnBackgroundImageSpan(Parcel src) {
        mImageId = src.readInt();
    }
    /**
     * @hide
     */
    public void draw(Canvas canvas, int width,float x,int top, int y, int bottom, Paint paint) {
        if (mDrawable == null) {//if no backgroundImage just don't do any draw
            throw new IllegalStateException("should call convertToDrawable() first");
        }
        Drawable drawable = mDrawable;
        canvas.save();

        canvas.translate(x, top); // translate to the left top point
        mDrawable.setBounds(0, 0, width, (bottom - top));
        drawable.draw(canvas);
        
        canvas.restore();
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        //tp.imageBackgroud = true;
    }

    @Override
    public int getSpanTypeId() {
        return BACKGROUND_IMAGE_SPAN;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mImageId);
    }
    /**
     * @hide
     */
    public void convertToDrawable(Context context) {
        if (mDrawable == null) {
            mDrawable = context.getResources().getDrawable(mImageId);    
        }

    }
    
    /**
     * convert a style text that contain BackgroundImageSpan, Parcek only pass resource id, 
     * after Parcel, we need to convert resource id to Drawable.
     * @hide
     * 
     */
    public static void convert(CharSequence text , Context context) {
        if (!(text instanceof SpannableStringBuilder)) {
            return;
        }
        
        SpannableStringBuilder builder = (SpannableStringBuilder)text;
                
        GnBackgroundImageSpan[] spans = builder.getSpans(0, text.length(), GnBackgroundImageSpan.class);
        if (spans == null || spans.length == 0) {
            return;
        }
        
        for (int i = 0; i < spans.length; i++) {
            spans[i].convertToDrawable(context);
        }
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end,
            float x, int top, int y, int bottom, Paint paint) {
        // draw image 
        draw(canvas, mWidth,x,top, y, bottom, paint);
        
        // draw text
        // the paint is already updated 
        canvas.drawText(text,start,end, x,y, paint);
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end,
            FontMetricsInt fm) {        
        float size = paint.measureText(text, start, end);
        if (fm != null && paint != null) {
            paint.getFontMetricsInt(fm);
        }
        mWidth = (int)size;
        return mWidth;
    }
    
}
