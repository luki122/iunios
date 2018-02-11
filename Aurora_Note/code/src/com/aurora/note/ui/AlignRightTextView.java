package com.aurora.note.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.aurora.utils.DensityUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * 让文本右端对齐的文本控件，用于解决备忘录中的文字生成图片时不好看的问题
 * @author JimXia
 *
 * @date 2015年3月5日 上午10:28:46
 */
public class AlignRightTextView extends TextView {
	private static final String TAG = "AlignRightTextView";

	/**绘制文本时可用的宽度*/
	private int mAvailableWidth;
	
	/**从文本中解析出来的ImageSpan，如果有的话*/
	private ImageSpanData[] mImageSpanData;
	
	/**每一行文本作为一个元素*/
	private ArrayList<LineText> mLineTexts = null;
	
	private final FontMetricsInt mFontMetricsInt = new FontMetricsInt();
	
	/**测量和绘制单个字符时的缓冲*/
	private final char[] mChrBuf = new char[1];
	
	/**如果调用setText的时候，控件的可用宽度还没有测量出来，则将这个变量设置为true*/
	private boolean mPendingGenerateLineText = false;
	
	/**是否绘制调试用的边线*/
	private static final boolean DEBUG = false;
	
	/**调试用的画笔*/
	private final Paint mDebugPaint;
	
	/**字间距，以像素为单位*/
	private int mWordSpace;
	
	public AlignRightTextView(Context context) {
		this(context, null);
	}

	public AlignRightTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	private int getLineSpacing() {
	    int lineSpacingExtra = DensityUtil.dip2px(getContext(), 11);
	    try {
            Method getLineSpacingExtra = TextView.class.
                    getDeclaredMethod("getLineSpacingExtra");
            Object obj = getLineSpacingExtra.invoke(this);
            if (obj != null && obj instanceof Float) {
                lineSpacingExtra = ((Float) obj).intValue();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
	    
	    return lineSpacingExtra;
	}

	public AlignRightTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		if (DEBUG) {
		    mDebugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		    mDebugPaint.setStyle(Style.STROKE);
		    mDebugPaint.setColor(Color.RED);
		} else {
		    mDebugPaint = null;
		}
	}
	
	public void setWordSpace(int wordSpace) {
	    mWordSpace = wordSpace;
	    if (mLineTexts != null) {
	        mLineTexts = null;
	        generateLineTexts();
	    }
	}
	
	public int getWordSpace() {
	    return mWordSpace;
	}
	
    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        getPaint().setColor(color);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        Spanned spanned = null;
        ImageSpanData[] imageSpanDatas = null;
        
        if (text instanceof Spanned) {
            spanned = (Spanned) text;
            ImageSpan[] imageSpans = spanned.getSpans(0, text.length(), ImageSpan.class);
            if (imageSpans != null && imageSpans.length > 0) {
                imageSpanDatas = new ImageSpanData[imageSpans.length];
                for (int i = 0; i < imageSpans.length; i ++) {
                    imageSpanDatas[i] = new ImageSpanData();
                    imageSpanDatas[i].span = imageSpans[i];
                    imageSpanDatas[i].start = spanned.getSpanStart(imageSpans[i]);
                    imageSpanDatas[i].end = spanned.getSpanEnd(imageSpans[i]);
                }
            }
        }
        mImageSpanData = imageSpanDatas;
        mLineTexts = null;
        
        super.setText(text, type);
        
        if (mAvailableWidth > 0) {
            generateLineTexts();
        } else {
            mPendingGenerateLineText = true;
        }
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        mAvailableWidth = getMeasuredWidth() - getCompoundPaddingLeft() - getCompoundPaddingRight();
        if (mPendingGenerateLineText) {
            mPendingGenerateLineText = false;
            int totalHeight = generateLineTexts() + getPaddingTop() + getPaddingBottom();
            
            if (totalHeight > getMeasuredHeight()) {
                setMeasuredDimension(getMeasuredWidth(), totalHeight);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mLineTexts == null) {
            generateLineTexts();
        }
        
        if (mLineTexts == null || mLineTexts.isEmpty()) {
            super.onDraw(canvas);
        } else {
            final TextPaint paint = getPaint();
            CharSequence text = getText();
            ImageSpanData[] imageSpanDatas = mImageSpanData;
            final int fontHeight = getFontHeight(paint);
            final int lineSpacingExtra = getLineSpacing();
            float x = 0;
            int y = fontHeight - (int)paint.descent();
            int top = 0;
            int bottom = mLineTexts.get(0).lineHeight + lineSpacingExtra;
            canvas.save();
            canvas.translate(getCompoundPaddingLeft(), getCompoundPaddingTop());
            
            if (DEBUG) {
                // 画两边的边线
                canvas.drawLine(0, 0, 0, getHeight(), mDebugPaint);
                canvas.drawLine(mAvailableWidth, 0, mAvailableWidth, getHeight(), mDebugPaint);
            }
            
            final int wordSpace = mWordSpace;
            for (int i = 0, size = mLineTexts.size(); i < size; i ++) {            
                LineText lt = mLineTexts.get(i);
                x = 0;
                int yDelta = lt.lineHeight;
                if (imageSpanDatas != null) {
                    for (int j = 0; j < imageSpanDatas.length; j ++) {
                        if (imageSpanDatas[j].start == lt.start) {
                            imageSpanDatas[j].span.getSize(paint, text,
                                    imageSpanDatas[j].start, imageSpanDatas[j].end, mFontMetricsInt);
                            imageSpanDatas[j].span.draw(canvas, text,
                                    imageSpanDatas[j].start, imageSpanDatas[j].end, x, top, y, bottom, paint);
                            
                            if (DEBUG) {
                                canvas.drawRect(x, top, x + imageSpanDatas[j].spanSize,
                                        top + mFontMetricsInt.bottom - mFontMetricsInt.top, mDebugPaint);
                            }
                            
                            x += imageSpanDatas[j].spanSize + lt.wordSpaceOffset + wordSpace;
//                            yDelta = (mFontMetricsInt.bottom - mFontMetricsInt.top) > fontHeight ?
//                                    mFontMetricsInt.bottom - mFontMetricsInt.top: fontHeight;
                            lt.start += (imageSpanDatas[j].end - imageSpanDatas[j].start/* + 1*/);
                            break;
                        }
                    }
                }
                
                for (int j = lt.start, length = text.length(); j <= lt.end && j < length; j ++) {
                    mChrBuf[0] = text.charAt(j);
                    if (mChrBuf[0] == '\n') {
                        if (DEBUG) {
                            int color = mDebugPaint.getColor();
                            mDebugPaint.setColor(Color.BLUE);
                            canvas.drawCircle(x, y, 20, mDebugPaint);
                            mDebugPaint.setColor(color);
                        }
                        // 换行符不用画
                        continue;
                    }
                    canvas.drawText(mChrBuf, 0, 1, x, y, paint);
                    
                    if (DEBUG) {
                        canvas.drawRect(x, top, x + paint.measureText(mChrBuf, 0, 1),
                                top + fontHeight, mDebugPaint);
                    }
                    
                    x += paint.measureText(mChrBuf, 0, 1);
                    if (!isSpaceChar(mChrBuf[0])) {
                        x += lt.wordSpaceOffset + wordSpace;
                    }
                }
                
                y += yDelta + lineSpacingExtra;
                top += yDelta + lineSpacingExtra;
            }
            
            canvas.restore();
        }
    }
    
    private int getFontHeight(Paint paint) {
        paint.getFontMetricsInt(mFontMetricsInt);
//        return mFontMetricsInt.descent - mFontMetricsInt.ascent;
        return mFontMetricsInt.bottom - mFontMetricsInt.top;
    }
    
    private int generateLineTexts() {
        if (mLineTexts == null) {
            mLineTexts = new ArrayList<LineText>();

            final TextPaint paint = getPaint();
            
            final int availableWidth = mAvailableWidth;
            CharSequence text = getText();
            ImageSpanData[] imageSpanDatas = mImageSpanData;

            ImageSpanData span = null;
            float totalLineWidth = 0f;
            int lineChrCount = 0;
            int lineCount = 0;
            int lineStart = 0;
            float chrWidth = 0;
            boolean isCurChrImageSpan = false;
            final int wordSpace = mWordSpace;
            /**总高度*/
            int totalHeight = 0;
            int maxLineHeight = 0;
            final int fontHeight = getFontHeight(getPaint());
            final int lineSpacingExtra = getLineSpacing();
            
            for (int i = 0, length = text.length(); i < length; i ++) {
                mChrBuf[0] = text.charAt(i);
                span = null;
                isCurChrImageSpan = false;
                
                if (imageSpanDatas != null) {
                    for (int j = 0; j < imageSpanDatas.length; j ++) {
                        if (imageSpanDatas[j].start == i) {
                            span = imageSpanDatas[j];
                            // 不考虑span重叠的情况，以第一个找到的span为准
                            break;
                        }
                    }
                }
                
                if (span != null) {
                    span.spanSize = span.span.getSize(paint, text, span.start, span.end, mFontMetricsInt);
                    chrWidth = span.spanSize;
                    maxLineHeight = Math.max(maxLineHeight,
                            mFontMetricsInt.bottom - mFontMetricsInt.top);
                    i += (span.end - span.start - 1);
                    isCurChrImageSpan = true;
                } else {
                    chrWidth = paint.measureText(mChrBuf, 0, 1);
                    maxLineHeight = Math.max(maxLineHeight, fontHeight);
                    if (!isSpaceChar(mChrBuf[0])) {
                        // 空格字符不统计
                        lineChrCount ++;
                    }
                }
                
                if (mChrBuf[0] == '\n'/* && lineStart != i*/) {
                    lineCount ++;
                    mLineTexts.add(new LineText(lineStart, i, lineCount, 0, maxLineHeight));
                    totalHeight += maxLineHeight + lineSpacingExtra;
                    lineStart = i + 1;
                    totalLineWidth = 0;
                    lineChrCount = 0;
                    maxLineHeight = 0;
                } else {
                    totalLineWidth += chrWidth;
                    if (!isSpaceChar(mChrBuf[0])) {
                        // 空格字符后边就不要加字间距了
                        totalLineWidth += wordSpace;
                    }
                    if (totalLineWidth >= availableWidth) {
                        if (!isSpaceChar(mChrBuf[0])) {
                            totalLineWidth -= wordSpace; // 一行的最后一个字符不需要间距
                        }
                        lineCount ++;
                        if (!isCurChrImageSpan) {
                            if (isLeftPunctuation(mChrBuf[0])) {
                                i--;
                                mChrBuf[0] = text.charAt(i); // 取上一个字符
                                float tmpWidth = availableWidth - totalLineWidth + chrWidth;
                                if (!isSpaceChar(mChrBuf[0])) {
                                    tmpWidth += wordSpace;
                                    lineChrCount --;
                                }
                                float tempWordSpaceOffset = tmpWidth / lineChrCount;
                                mLineTexts.add(new LineText(lineStart, i, lineCount,
                                        tempWordSpaceOffset, maxLineHeight));
                            } else if (isRightPunctuation(mChrBuf[0])) {
                                if (i == (length - 1)) {
                                    mLineTexts.add(new LineText(lineStart, i,
                                            lineCount, 0, maxLineHeight));
                                    totalHeight += maxLineHeight + lineSpacingExtra;
                                    break;
                                } else {
                                    char nextChar = text.charAt(i + 1);
                                    if ((isHalfPunctuation(nextChar) || isPunctuation(nextChar)) &&
                                            !isLeftPunctuation(nextChar)) {
                                        i++;
                                        mChrBuf[0] = nextChar;
                                        float tmpWidth = availableWidth - totalLineWidth -
                                                paint.measureText(mChrBuf, 0, 1);
                                        if (!isSpaceChar(nextChar)) {
                                            lineChrCount ++;
                                            tmpWidth -= wordSpace;
                                        }
                                        float tempWordSpaceOffset = tmpWidth / lineChrCount;
                                        mLineTexts.add(new LineText(lineStart, i, lineCount,
                                                tempWordSpaceOffset, maxLineHeight));
                                    } else {
                                        float tempWordSpaceOffset = (availableWidth - totalLineWidth) / lineChrCount;
                                        mLineTexts.add(new LineText(lineStart, i, lineCount,
                                                tempWordSpaceOffset, maxLineHeight));
                                    }
                                }
                            } else {
                                if (isHalfPunctuation(mChrBuf[0]) || isPunctuation(mChrBuf[0])) {
                                    float tempWordSpaceOffset = (availableWidth - totalLineWidth) / lineChrCount;
                                    mLineTexts.add(new LineText(lineStart, i, lineCount,
                                            tempWordSpaceOffset, maxLineHeight));
                                } else {
                                    if (i >= 2) {
                                        char preChar = text.charAt(i - 1);
                                        if (isLeftPunctuation(preChar)) {
                                            i = i - 2;
                                            char curChar = mChrBuf[0];
                                            mChrBuf[0] = preChar;
                                            float tmpWidth = availableWidth - totalLineWidth +
                                                    chrWidth + paint.measureText(mChrBuf, 0, 1);
                                            if (!isSpaceChar(curChar)) {
                                                tmpWidth += wordSpace;
                                                lineChrCount --;
                                            }
                                            if (!isSpaceChar(preChar)) {
                                                tmpWidth += wordSpace;
                                                lineChrCount --;
                                            }
                                            float tempWordSpaceOffset = tmpWidth / lineChrCount;
                                            mLineTexts.add(new LineText(lineStart, i, lineCount,
                                                    tempWordSpaceOffset, maxLineHeight));
                                        } else {
                                            i--;
                                            float tmpWidth = availableWidth - totalLineWidth + chrWidth;
                                            if (!isSpaceChar(mChrBuf[0])) {
                                                tmpWidth += wordSpace;
                                                lineChrCount --;
                                            }
                                            float tempWordSpaceOffset = tmpWidth / lineChrCount;
                                            mLineTexts.add(new LineText(lineStart, i, lineCount,
                                                    tempWordSpaceOffset, maxLineHeight));
                                        }

                                    }
                                }
                            }
                        } else {
                            if (i < length - 1) {
                                char nextChr = text.charAt(i + 1);
                                if (nextChr == '\n') {
                                    // ImageSpan后的下一个字符是换行符
                                    i ++;
                                }
                            }
                            mLineTexts.add(new LineText(lineStart, i, lineCount, 0, maxLineHeight));
                        }
                        
                        lineStart = i + 1;
                        totalLineWidth = 0;
                        lineChrCount = 0;
                        totalHeight += maxLineHeight + lineSpacingExtra;
                        maxLineHeight = 0;
                    } else {
                        if (i == length - 1) {
                            lineCount ++;
                            mLineTexts.add(new LineText(lineStart, i, lineCount, 0, maxLineHeight));
                            totalHeight += maxLineHeight + lineSpacingExtra;
                        }
                    }
                }
            }
            
            return totalHeight;
        }
        
        return 0;
    }
    
    private boolean isSpaceChar(char chr) {
        return chr == ' ';
    }
    
    private static boolean isLeftPunctuation(char c) {
        int count = c;

        if (count == 8220 || count == 12298 || count == 65288 || count == 12304
                || count == 40 || count == 60 || count == 91 || count == 123) {
            return true;
        }

        return false;
    }
    
    private static boolean isRightPunctuation(char c) {
        int count = c;
        if (count == 8221 || count == 12299 || count == 65289 || count == 12305
                || count == 41 || count == 62 || count == 93 || count == 125) {

            return true;

        }

        return false;
    }
    
    private static boolean isHalfPunctuation(char c) {
        int count = c;

        if (count >= 33 && count <= 47) {

            // !~/

            return true;

        } else if (count >= 58 && count <= 64) {

            // :~@

            return true;

        } else if (count >= 91 && count <= 96) {

            // [~

            return true;

        } else if (count >= 123 && count <= 126) {

            // {~~

            return true;

        }

        return false;
    }
    
    private static boolean isPunctuation(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);

        if (ub == Character.UnicodeBlock.GENERAL_PUNCTUATION

                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION

                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {

            return true;

        }

        return false;
    }
    
    private static class ImageSpanData {
        int start;
        int end;
        ImageSpan span;
        int spanSize;
        
        @Override
        public String toString() {
            return "ImageSpanData [start=" + start + ", end=" + end + ", span=" + span
                    + ", spanSize=" + spanSize + "]";
        }
    }
    
    private static class LineText {
        int start;
        int end;
        int lineCount;
        float wordSpaceOffset;
        int lineHeight;
        
        LineText(int start, int end, int lineCount, float wordSpaceOffset, int lineHeight) {
            this.start = start;
            this.end = end;
            this.lineCount = lineCount;
            this.wordSpaceOffset = wordSpaceOffset;
            this.lineHeight = lineHeight;
        }

        @Override
        public String toString() {
            return "LineText [start=" + start + ", end=" + end + ", lineCount=" + lineCount
                    + ", wordSpaceOffset=" + wordSpaceOffset +
                    ", lineHeight=" + lineHeight + "]";
        }
    }
}