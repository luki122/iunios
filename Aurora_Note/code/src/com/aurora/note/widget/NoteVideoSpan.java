package com.aurora.note.widget;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;

import com.aurora.note.NoteApp;

/**
 * 用来显示视频图片的ImageSpan
 * @author JimXia
 * 2014-6-12 下午4:42:56
 */
public class NoteVideoSpan extends NoteImageSpan {
    private Drawable mPlayIndicatorNormal;
    private boolean mIsVideoImageValid = true;

    public NoteVideoSpan(Drawable d, int normalResId, int pressedResId, String source, Type type, int rightExtraSpace,
            int leftPadding, int topPadding, boolean isVideoImageValid) {
        super(d, pressedResId, source, type, rightExtraSpace, leftPadding, topPadding);

        mIsVideoImageValid = isVideoImageValid;

        final Resources res = NoteApp.ysApp.getResources();
        mPlayIndicatorNormal = res.getDrawable(normalResId);
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top,
            int y, int bottom, Paint paint) {
        super.draw(canvas, text, start, end, x, top, y, bottom, paint);

        Drawable d = mSelected ? mSelectedDrawable: mPlayIndicatorNormal;
        if (/*mIsVideoImageValid && */d != null) {
            final int width = getWidth();
            final int height = getHeight();

            int dLeft = getLeft() - mLeftPadding + (width - d.getIntrinsicWidth()) / 2;
            int dTop = getTop() - mTopPadding + (height - d.getIntrinsicHeight()) / 2;
            d.setBounds(dLeft, dTop, dLeft + d.getIntrinsicWidth(), dTop + d.getIntrinsicHeight());
            d.draw(canvas);
            // drawDrawableBounds(canvas, d, paint);
        }
    }

    protected static void drawDrawableBounds(Canvas canvas, Drawable drawable, Paint paint) {
        int color = paint.getColor();
        Style style = paint.getStyle();
        float textSize = paint.getTextSize();
        paint.setColor(Color.BLUE);
        paint.setStyle(Style.STROKE);
        canvas.drawRect(drawable.getBounds(), paint);
        paint.setColor(color);
        paint.setStyle(style);
        paint.setTextSize(textSize);
    }

}