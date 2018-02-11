package com.aurora.note.widget;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.aurora.note.R;
import com.aurora.note.NoteApp;
import com.aurora.note.util.Globals;

import java.util.ArrayList;

/**
 * 用于图片组的ImageSpan
 * @author JimXia
 * 2014-5-20 上午11:22:55
 */
public class NoteImageGroupSpan extends NoteImageSpan {
//    private static final String TAG = "NoteImageGroupSpan";
    
    private ArrayList<NoteImageSpan> mSubSpans;
    private static final int DEFAULT_OFFSET = 20;
    
    private int mHOffset; // 水平间距
    private int mVOffset; // 垂直间距
     
    private static int sTwoImageLeftWidth; // 两张图片左边图片的宽度
    private static int sTwoImageRightWidth; // 两张图片右边图片的宽度
    private static int sTwoImageLeftHeight = -1; // 两张图片左边图片的高度
    private static int sTwoImageRightHeight = -1; // 两张图片右边图片的高度
    
    private static int sThreeImageLeftWidth; // 三张图片左边图片的宽度
    private static int sThreeImageRightWidth; // 三张图片右边图片的宽度
    private static int sThreeImageLeftHeight = -1; // 三张图片左边图片的高度
    private static int sThreeImageRightHeight = -1; // 三张图片右边图片的高度
    
    private static int sFourImageTopWidth = -1; // 四张图片上边图片的宽度
    private static int sFourImageBottomWidth = -1; // 四张图片下边图片的宽度
    private static int sFourImageTopHeight; // 四张图片上边图片的高度
    private static int sFourImageBottomHeight; // 四张图片下边图片的高度
    
    private int mWidth;
    private int mHeight;
    
    private SpanInfo[] mInfos;    
    
    static {
        Resources res = NoteApp.ysApp.getResources();
        sTwoImageLeftWidth = res.getDimensionPixelSize(R.dimen.new_note_two_image_left_width);
        sTwoImageRightWidth = res.getDimensionPixelSize(R.dimen.new_note_two_image_right_width);
        
        sThreeImageLeftWidth = res.getDimensionPixelSize(R.dimen.new_note_three_image_left_width);
        sThreeImageRightWidth = res.getDimensionPixelSize(R.dimen.new_note_three_image_right_width);
        
        sFourImageTopHeight = res.getDimensionPixelSize(R.dimen.new_note_four_image_top_height);
        sFourImageBottomHeight = res.getDimensionPixelSize(R.dimen.new_note_four_image_bottom_height);
    }
    
    public NoteImageGroupSpan(Drawable d, String source, Type type, int expectedWidth, int expectedHeight,
            int rightExtraSpace, int leftPadding, int topPadding) {
        super(d, source, type, expectedWidth, expectedHeight, rightExtraSpace, leftPadding, topPadding);
        mSubSpans = new ArrayList<NoteImageSpan>(Globals.ATTACHMENT_IMAGE_GROUP_MAX_IMAGES);
        
        if (sTwoImageLeftHeight == -1) {
            sTwoImageLeftHeight = expectedHeight;
        }
        if (sTwoImageRightHeight == -1) {
            sTwoImageRightHeight = expectedHeight;
        }
        if (sThreeImageLeftHeight == -1) {
            sThreeImageLeftHeight = expectedHeight;
        }
        if (sFourImageTopWidth == -1) {
            sFourImageTopWidth = expectedWidth;
        }
        
        Resources res = NoteApp.ysApp.getResources();
        mHOffset = res.getDimensionPixelSize(R.dimen.new_note_image_group_hoffset);
        if (mHOffset <= 0) {
            mHOffset = DEFAULT_OFFSET;
        }
        
        mVOffset = res.getDimensionPixelSize(R.dimen.new_note_image_group_voffset);
        if (mVOffset <= 0) {
            mVOffset = DEFAULT_OFFSET;
        }
    }
    
    public void addSubSpan(NoteImageSpan subSpan) {
        if (!mSubSpans.contains(subSpan)) {
            mSubSpans.add(subSpan);
        }
    }
    
    public void removeSubSpan(NoteImageSpan subSpan) {
        mSubSpans.remove(subSpan);
    }
    
    public void removeAllSubSpans() {
        mSubSpans.clear();
    }
    
    public int getSubSpanCount() {
        return mSubSpans.size();
    }
    
    public ArrayList<NoteImageSpan> getSubSpans() {
        // 拷贝一份新的，避免client直接修改子span
        return new ArrayList<NoteImageSpan>(mSubSpans);
    }
    
    @Override
    public int getWidth() {
        return mWidth;
    }
    
    @Override
    public int getHeight() {
        return mHeight;
    }
    
    @Override
    public String getSource(int x, int y) {
        for (NoteImageSpan span: mSubSpans) {
            if (span.contains(x, y)) {
                return span.getSource();
            }
        }
        
        return null;
    }
    
    @Override
    public Type getType(int x, int y) {
        for (NoteImageSpan span: mSubSpans) {
            if (span.contains(x, y)) {
                return span.getType();
            }
        }
        
        return null;
    }
    
    @Override
    public boolean contains(int x, int y) {
        for (NoteImageSpan span: mSubSpans) {
            if (span.contains(x, y)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public NoteImageSpan getSpan(int x, int y) {
        for (NoteImageSpan span: mSubSpans) {
            if (span.contains(x, y)) {
                return span;
            }
        }
        
        return null;
    }
    
    public int indexOf(NoteImageSpan subSpan) {
        if (subSpan == null) {
            return -1;
        }
        
        return mSubSpans.indexOf(subSpan);
    }
    
    private static class SpanInfo {
        int transX;
        int transY;
        int width;
        int height;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y,
            int bottom, Paint paint) {
        final ArrayList<NoteImageSpan> spans = mSubSpans;
        if (spans.size() == 0) {
            throw new RuntimeException("NoteImageGroupSpan must contain at least one sub span");
        }
        
        NoteImageSpan span;
        final int spanCount = spans.size();
        if (spanCount == 1) {
            span = spans.get(0);
            span.draw(canvas, text, start, end, x, top, y, bottom, paint);
            setLeft(span.getLeft());
            setTop(span.getTop());
        } else {
            final SpanInfo[] infos = mInfos;
            SpanInfo info = null;
            int myLeft = Integer.MAX_VALUE, myTop = Integer.MAX_VALUE;
            switch (spanCount) {
                case 2:
                    info = infos[0];
                    info.transX = (int)x;
                    info.transY = top + getOffset();
                    
                    info = infos[1];
                    info.transX = infos[0].transX + infos[0].width + mHOffset;
                    info.transY = infos[0].transY;
                    break;
                case 3:
                    info = infos[0];
                    info.transX = (int)x;
                    info.transY = top + getOffset();
                    
                    info = infos[1];
                    info.transX = infos[0].transX + infos[0].width + mHOffset;
                    info.transY = infos[0].transY;
                    
                    info = infos[2];
                    info.transX = infos[0].transX + infos[0].width + mHOffset;
                    info.transY = infos[0].transY + infos[1].height + mVOffset;
                    break;
                case 4:
                    info = infos[0];
                    info.transX = (int)x;
                    info.transY = top + getOffset();
                    
                    info = infos[1];
                    info.transX = infos[0].transX;
                    info.transY = infos[0].transY + infos[0].height + mVOffset;
                    
                    info = infos[2];
                    info.transX = infos[1].transX + infos[1].width + mHOffset;
                    info.transY = infos[1].transY;
                    
                    info = infos[3];
                    info.transX = infos[2].transX + infos[2].width + mHOffset;
                    info.transY = infos[2].transY;
                    break;
            }
            
            Drawable d;
            for (int i = 0; i < spanCount; i ++) {
                info = infos[i];
                span = spans.get(i);
                d = span.getDrawable();
                d.setBounds(0, 0, info.width, info.height);
                canvas.save();
                canvas.translate(info.transX, info.transY);
                span.setLeft(info.transX + mLeftPadding);
                span.setTop(info.transY + mTopPadding);
                if (myLeft > span.getLeft()) {
                    myLeft = span.getLeft();
                }
                if (myTop > span.getTop()) {
                    myTop = span.getTop();
                }
//                Log.d(TAG, "Jim, left: " + info.transX + ", top: " + info.transY);
                d.draw(canvas);
                if (span.isSelected() && span.mSelectedDrawable != null) {
                    span.mSelectedDrawable.setBounds(d.getBounds());
                    span.mSelectedDrawable.draw(canvas);
                }
//                drawDrawableBounds(canvas, d, span, paint);
                canvas.restore();
            }
            setLeft(myLeft);
            setTop(myTop);
        }
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, FontMetricsInt fm) {
        if (mSubSpans.size() == 0) {
            throw new RuntimeException("NoteImageGroupSpan must contain at least one sub span");
        }
        
        NoteImageSpan span;
        Drawable d;
        Rect bounds;
        int width = mExpectedWidth, height = mExpectedHeight;
        final int size = mSubSpans.size();
        if (size == 1) {
            span = mSubSpans.get(0);
            width = span.getSize(paint, text, start, end, fm);
            mWidth = width;
            mHeight = span.getHeight();
            return width;
        } else if (size == 2) {
            mInfos = new SpanInfo[size];
            
            span = mSubSpans.get(0);
            d = span.getDrawable();
            bounds = d.getBounds();
            if (bounds.width() != sTwoImageLeftWidth || bounds.height() != sTwoImageLeftHeight) {
                resize(span, sTwoImageLeftWidth, sTwoImageLeftHeight);
            }
            SpanInfo info = new SpanInfo();
            info.width = sTwoImageLeftWidth;
            info.height = sTwoImageLeftHeight;
            mInfos[0] = info;
            
            span = mSubSpans.get(1);
            d = span.getDrawable();
            bounds = d.getBounds();
            sTwoImageRightWidth = width - mHOffset - sTwoImageLeftWidth; // TODO: 宽度应该按比例分配
            if (bounds.width() != sTwoImageRightWidth || bounds.height() != sTwoImageRightHeight) {
                resize(span, sTwoImageRightWidth, sTwoImageRightHeight);
            }
            info = new SpanInfo();
            info.width = sTwoImageRightWidth;
            info.height = sTwoImageRightHeight;
            mInfos[1] = info;
            
            height = mExpectedHeight;
            width = mExpectedWidth;
        } else if (size == 3) {
            mInfos = new SpanInfo[size];
            
            if (sThreeImageRightHeight == -1) {
                sThreeImageRightHeight = (mExpectedHeight - mVOffset) / 2;
            }
            span = mSubSpans.get(0);
            d = span.getDrawable();
            bounds = d.getBounds();
            if (bounds.width() != sThreeImageLeftWidth || bounds.height() != sThreeImageLeftHeight) {
                resize(span, sThreeImageLeftWidth, sThreeImageLeftHeight);
            }
            SpanInfo info = new SpanInfo();
            info.width = sThreeImageLeftWidth;
            info.height = sThreeImageLeftHeight;
            mInfos[0] = info;
            
            span = mSubSpans.get(1);
            d = span.getDrawable();
            bounds = d.getBounds();
            sThreeImageRightWidth = width - mHOffset - sThreeImageLeftWidth; // TODO: 宽度应该按比例分配，不应该写死
            if (bounds.width() != sThreeImageRightWidth || bounds.height() != sThreeImageRightHeight) {
                resize(span, sThreeImageRightWidth, sThreeImageRightHeight);
            }
            info = new SpanInfo();
            info.width = sThreeImageRightWidth;
            info.height = sThreeImageRightHeight;
            mInfos[1] = info;
            
            span = mSubSpans.get(2);
            d = span.getDrawable();
            bounds = d.getBounds();
            int rightBottomHeight = mExpectedHeight - mVOffset - sThreeImageRightHeight;
            if (bounds.width() != sThreeImageRightWidth || bounds.height() != rightBottomHeight) {
                resize(span, sThreeImageRightWidth, rightBottomHeight);
            }
            info = new SpanInfo();
            info.width = sThreeImageRightWidth;
            info.height = rightBottomHeight;
            mInfos[2] = info;
            
            height = mExpectedHeight;
            width = mExpectedWidth;
        } else if (size == 4) {
            mInfos = new SpanInfo[size];
            
            if (sFourImageTopWidth == -1) {
                sFourImageTopWidth = mExpectedWidth;
            }
            if (sFourImageBottomWidth == -1) {
                sFourImageBottomWidth = (mExpectedWidth - mHOffset * 2) / 3;
            }
            
            span = mSubSpans.get(0);
            d = span.getDrawable();
            bounds = d.getBounds();
            if (bounds.width() != sFourImageTopWidth || bounds.height() != sFourImageTopHeight) {
                resize(span, sFourImageTopWidth, sFourImageTopHeight);
            }
            SpanInfo info = new SpanInfo();
            info.width = sFourImageTopWidth;
            info.height = sFourImageTopHeight;
            mInfos[0] = info;
            
            span = mSubSpans.get(1);
            d = span.getDrawable();
            bounds = d.getBounds();
            if (bounds.width() != sFourImageBottomWidth || bounds.height() != sFourImageBottomHeight) {
                resize(span, sFourImageBottomWidth, sFourImageBottomHeight);
            }
            info = new SpanInfo();
            info.width = sFourImageBottomWidth;
            info.height = sFourImageBottomHeight;
            mInfos[1] = info;
            
            int bottomMiddleWidth = mExpectedWidth - mHOffset * 2 - sFourImageBottomWidth * 2;
            span = mSubSpans.get(2);
            d = span.getDrawable();
            bounds = d.getBounds();
            if (bounds.width() != bottomMiddleWidth || bounds.height() != sFourImageBottomHeight) {
                resize(span, bottomMiddleWidth, sFourImageBottomHeight);
            }
            info = new SpanInfo();
            info.width = bottomMiddleWidth;
            info.height = sFourImageBottomHeight;
            mInfos[2] = info;
            
            span = mSubSpans.get(3);
            d = span.getDrawable();
            bounds = d.getBounds();
            if (bounds.width() != sFourImageBottomWidth || bounds.height() != sFourImageBottomHeight) {
                resize(span, sFourImageBottomWidth, sFourImageBottomHeight);
            }
            info = new SpanInfo();
            info.width = sFourImageBottomWidth;
            info.height = sFourImageBottomHeight;
            mInfos[3] = info;
            
            height = sFourImageTopHeight + mVOffset + sFourImageBottomHeight;
            width = mExpectedWidth;
        }
        
        if (paint != null && fm != null) {
            paint.getFontMetricsInt(fm);
        }
        
        if (fm != null) {
            fm.ascent = -height; 
//            fm.descent = 0; 

            fm.top = fm.ascent;
//            fm.bottom = 0;
        }
        
        mWidth = width;
        mHeight = height;
        
        if (mRightExtraSpace > 0) {
            width += mRightExtraSpace;
        }

        return width;
    }    
}