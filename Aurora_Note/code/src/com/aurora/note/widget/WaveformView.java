
package com.aurora.note.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.aurora.note.NoteApp;
import com.aurora.note.R;
import com.aurora.note.bean.MarkInfo;
import com.aurora.utils.DensityUtil;

import java.lang.ref.SoftReference;
import java.util.ArrayList;

public class WaveformView extends View {
    private static final String TAG = "WaveformView";
    
    /*private static final int[] MARK_FLAG_NUM_DRAWABLES = {
            R.drawable.flag_num_01, R.drawable.flag_num_02, R.drawable.flag_num_03,
            R.drawable.flag_num_04, R.drawable.flag_num_05, R.drawable.flag_num_06,
            R.drawable.flag_num_07, R.drawable.flag_num_08, R.drawable.flag_num_09,
            R.drawable.flag_num_10, R.drawable.flag_num_11, R.drawable.flag_num_12,
            R.drawable.flag_num_13, R.drawable.flag_num_14, R.drawable.flag_num_15,
            R.drawable.flag_num_16, R.drawable.flag_num_17, R.drawable.flag_num_18,
            R.drawable.flag_num_19, R.drawable.flag_num_20, R.drawable.flag_num_21,
            R.drawable.flag_num_22, R.drawable.flag_num_23, R.drawable.flag_num_24,
            R.drawable.flag_num_25, R.drawable.flag_num_26, R.drawable.flag_num_27,
            R.drawable.flag_num_28, R.drawable.flag_num_29, R.drawable.flag_num_30,
            R.drawable.flag_num_31, R.drawable.flag_num_32, R.drawable.flag_num_33,
            R.drawable.flag_num_34, R.drawable.flag_num_35, R.drawable.flag_num_36,
            R.drawable.flag_num_37, R.drawable.flag_num_38, R.drawable.flag_num_39,
            R.drawable.flag_num_40, R.drawable.flag_num_41, R.drawable.flag_num_42,
            R.drawable.flag_num_43, R.drawable.flag_num_44, R.drawable.flag_num_45,
            R.drawable.flag_num_46, R.drawable.flag_num_47, R.drawable.flag_num_48,
            R.drawable.flag_num_49, R.drawable.flag_num_50, R.drawable.flag_num_51,
            R.drawable.flag_num_52, R.drawable.flag_num_53, R.drawable.flag_num_54,
            R.drawable.flag_num_55, R.drawable.flag_num_56, R.drawable.flag_num_57,
            R.drawable.flag_num_58, R.drawable.flag_num_59, R.drawable.flag_num_60,
            R.drawable.flag_num_61, R.drawable.flag_num_62, R.drawable.flag_num_63,
            R.drawable.flag_num_64, R.drawable.flag_num_65, R.drawable.flag_num_66,
            R.drawable.flag_num_67, R.drawable.flag_num_68, R.drawable.flag_num_69,
            R.drawable.flag_num_70, R.drawable.flag_num_71, R.drawable.flag_num_72,
            R.drawable.flag_num_73, R.drawable.flag_num_74, R.drawable.flag_num_75,
            R.drawable.flag_num_76, R.drawable.flag_num_77, R.drawable.flag_num_78,
            R.drawable.flag_num_79, R.drawable.flag_num_80, R.drawable.flag_num_81,
            R.drawable.flag_num_82, R.drawable.flag_num_83, R.drawable.flag_num_84,
            R.drawable.flag_num_85, R.drawable.flag_num_86, R.drawable.flag_num_87,
            R.drawable.flag_num_88, R.drawable.flag_num_89, R.drawable.flag_num_90,
            R.drawable.flag_num_91, R.drawable.flag_num_92, R.drawable.flag_num_93,
            R.drawable.flag_num_94, R.drawable.flag_num_95, R.drawable.flag_num_96,
            R.drawable.flag_num_97, R.drawable.flag_num_98, R.drawable.flag_num_99
    };*/
    private Bitmap mMarkFlagBitmap;
    private ArrayList<MarkInfo> mMarkFlagInfos;
    private SparseArray<SoftReference<Bitmap>> mMarkBitmaps;
    private boolean mShowTimeLine = true;
    private String[] mTimeDatas = { "00:00:01", "00:00:02", "00:00:03", "00:00:04", "00:00:05", "00:00:06", "00:00:07", "00:00:08" };
    private Paint mTimeDrawPaint;
//    private Bitmap mTimeElapsedIndicator;
    private Drawable mTimeElapsedIndicator;
//    private float mTimeElapsedIndicatorOffset;
//    private Bitmap mTimeLineBitmap;
    private Drawable mTimelineDrawable;
    private int mTimeLineCellWidth;
    private float mTimeLineTranslate;
    private int mTimePaddingLeft;
    private float mTimeTextSize;
    private short[] mWaveFormDatas;
    protected Paint mWaveFormDrawPaint;
    protected int mWaveFormFrameWidth;
//    private int mWaveFormMarginTop;
    private boolean mWaveFormNormalMode;
    protected float mWaveFormRange;
    private int mWaveViewAreaRangeFlag;
    
    protected float mRateY;
    
    protected int mBaseLine = 0;
    
    protected static final int BG_COLOR = 0xfff8f8f8;
    
    private Drawable mHeaderBgDrawable;
    private int mHeaderBgHeight;
    private int mWaveBodyHeight;
    private Drawable mBottomLineDrawable;
    private static final int RECORD_TIME_INDICATOR_CIRCLE_HEIGHT = DensityUtil.dip2px(NoteApp.ysApp, 6);
    
    public WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    private void drawHeader(Canvas canvas) {
        mHeaderBgDrawable.draw(canvas);
    }
    
    private void drawBackgroundColor(Canvas canvas, int width) {
        canvas.save();
        canvas.translate(0, mHeaderBgHeight);
        canvas.clipRect(0, 0, width, mWaveBodyHeight);
        canvas.drawColor(BG_COLOR);
        canvas.restore();
    }
    
    private void drawTimeline(Canvas canvas, int height) {
        String[] timeDatas = mTimeDatas;
        if ((timeDatas != null) && (timeDatas.length > 0) && (this.mShowTimeLine)) {
            int timeLineCellWidth = mTimeLineCellWidth;
            int timePaddingLeft = mTimePaddingLeft;
            float timeTextSize = mTimeTextSize;
            final int timelineYOffset = DensityUtil.dip2px(getContext(), 14);
            canvas.save();
            canvas.translate(mTimeLineTranslate, 0f);
//            canvas.drawBitmap(mTimeLineBitmap, 0.0f, mHeaderBgHeight - mTimeLineBitmap.getHeight(), null);
//            canvas.drawBitmap(mTimeLineBitmap, 0.0f, 0f, null);
//            mTimelineDrawable.setBounds(0, 0, mTimelineDrawable.getIntrinsicWidth(), mHeaderBgHeight + mWaveBodyHeight);
            mTimelineDrawable.draw(canvas);
            for (int i = 0; i < timeDatas.length; i++) {
                canvas.drawText(timeDatas[i], timePaddingLeft + timeLineCellWidth * i,
                        timeTextSize + timelineYOffset, mTimeDrawPaint);
            }
            canvas.restore();
            
//            RecordUtil.debugTimelineData(timeDatas, "Jim, onDraw, timeline data: " + mTimeLineTranslate);
        }
    }
    
    private void drawWaveform(Canvas canvas) {
        final short[] waveData = mWaveFormDatas;
        if (waveData != null && waveData.length > 0) {
            float waveFormDrawStartPosition = mWaveFormRange;
            if (mWaveViewAreaRangeFlag == 0) {
                waveFormDrawStartPosition = waveFormDrawStartPosition / 2.0F;
            }

            int waveFormVerticalCenter = mBaseLine;
            int length = waveData.length;
            float waveFormPositionTranslate = mWaveFormFrameWidth;
            if (mWaveFormNormalMode && ((length * 2) < waveFormDrawStartPosition)) {
                waveFormPositionTranslate = waveFormDrawStartPosition * 1.0f / length;
            }

//            canvas.drawLine(0, waveFormVerticalCenter, getWidth(), waveFormVerticalCenter, mWaveFormDrawPaint);
            for (int i = length - 1; i >= 0; i--) {
//                byte data = (byte) Math.abs(waveData.get(i));
//                float waveDataHalfHeight = mWaveFormHeightScale * data / 2.0f;
//                canvas.drawLine(waveFormDrawStartPosition, waveFormVerticalCenter
//                        - waveDataHalfHeight,
//                        waveFormDrawStartPosition, waveFormVerticalCenter + waveDataHalfHeight,
//                        mWaveFormDrawPaint);
                short y = (short)(waveData[i] * mRateY);
                canvas.drawLine(waveFormDrawStartPosition, waveFormVerticalCenter
                        - y, waveFormDrawStartPosition, waveFormVerticalCenter + y, mWaveFormDrawPaint);
                waveFormDrawStartPosition -= waveFormPositionTranslate;
            }
        }
    }
    
    private void drawCenterLine(Canvas canvas, int width, int waveFormVerticalCenter) {
        canvas.drawLine(0, waveFormVerticalCenter, width, waveFormVerticalCenter, mWaveFormDrawPaint);
    }
    
    private void drawMark(Canvas canvas, int height) {
        ArrayList<MarkInfo> markInfo = mMarkFlagInfos;
//      Log.d(TAG, "Jim, onDraw, markInfo: " + markInfo);
      if (markInfo != null && markInfo.size() > 0) {
          int markInfoSize = markInfo.size();
//          int markFlagMarginTop = mHeaderBgHeight;
          int markFlagMarginTop = 0;
          float waveFormDrawStartPosition = mWaveFormRange;
          if (mWaveViewAreaRangeFlag == 0) {
              waveFormDrawStartPosition = waveFormDrawStartPosition / 2;
          }
          canvas.save();
          canvas.clipRect(2.0f, 0f, waveFormDrawStartPosition - 2.0f, height);
//          Log.d(TAG, "\n\nJim, markInfoSize: " + markInfoSize);
          for (int i = 0; i < markInfoSize; i++) {
              MarkInfo info = markInfo.get(i);
              if (info != null) {
                  canvas.drawBitmap(mMarkFlagBitmap, info.getDisplayPositionX(),
                          markFlagMarginTop, null);
                  /*int index = info.getIndex() - 1;
                  if (index >= 0 && index < MARK_FLAG_NUM_DRAWABLES.length) {
                      SoftReference<Bitmap> srBitmap = mMarkBitmaps.get(index);
                      Bitmap bitmap;
                      if (srBitmap == null || srBitmap.get() == null) {
                          bitmap = decodeBitmap(index);
                      } else {
                          bitmap = srBitmap.get();
                      }
                      canvas.drawBitmap(bitmap, info.getDisplayPositionX(),
                              markFlagMarginTop, null);
                  }*/
//                  Log.d(TAG, "Jim, getMarkElpasedTime: " + info.getMarkElpasedTime() +
//                          ", display position x: " + info.getDisplayPositionX());
//                  canvas.drawText("" + info.getMarkElpasedTime(), info.getDisplayPositionX(),
//                          mWaveFormMarginTop + 90, mTimeDrawPaint);
              }
          }
          canvas.restore();
      }
    }
    
    private void drawTimeIndicator(Canvas canvas, int width, int height) {
        if (mShowTimeLine) {
            int top = mHeaderBgHeight;
            if (mWaveViewAreaRangeFlag == 0) {
                top = mHeaderBgHeight - RECORD_TIME_INDICATOR_CIRCLE_HEIGHT;
            }
            int left = (width - mTimeElapsedIndicator.getIntrinsicWidth()) / 2;
            mTimeElapsedIndicator.setBounds(left, top, left + mTimeElapsedIndicator.getIntrinsicWidth(), height);
            mTimeElapsedIndicator.draw(canvas);
//            canvas.drawBitmap(mTimeElapsedIndicator, (width - mTimeElapsedIndicator.getIntrinsicWidth()) / 2,
//                    top, null);
        }/* else {
//            canvas.drawBitmap(mTimeElapsedIndicator, mTimeElapsedIndicatorOffset, mWaveFormMarginTop, null);
            mSrcRect.set(0, 0, mTimeElapsedIndicator.getWidth(), mTimeElapsedIndicator.getHeight());
            float left = mTimeElapsedIndicatorOffset;
            mDstRect.set(left, mWaveFormMarginTop, left + mTimeElapsedIndicator.getWidth(), getHeight());
            canvas.drawBitmap(mTimeElapsedIndicator, mSrcRect, mDstRect, null);
        }*/
    }
    
    private void drawBottomSepLine(Canvas canvas, int width) {
        canvas.save();
        canvas.translate(0, mHeaderBgHeight + mWaveBodyHeight);
        mBottomLineDrawable.setBounds(0, 0, width, mBottomLineDrawable.getIntrinsicHeight());
        mBottomLineDrawable.draw(canvas);
        canvas.restore();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        final int width = getWidth();
        final int height = getHeight();
        
        // 画header背景
        drawHeader(canvas);
        drawBackgroundColor(canvas, width);
//        drawTimeline(canvas);
        drawWaveform(canvas);
        drawTimeline(canvas, height);
        drawMark(canvas, height);
        drawCenterLine(canvas, width, mBaseLine);
        drawTimeIndicator(canvas, width, height);
        drawBottomSepLine(canvas, width);
    }
    
    /*private Bitmap decodeBitmap(int index) {
        return BitmapFactory.decodeResource(getResources(), MARK_FLAG_NUM_DRAWABLES[index]);
    }*/

    public void reset() {
        mTimeLineTranslate = mWaveFormRange / 2.0f;
        mWaveFormDatas = null;
        mMarkFlagInfos = null;
        if (mMarkBitmaps != null) {
            mMarkBitmaps.clear();
        }
        mMarkBitmaps = null;
        mTimeDatas = new String[] {
                "00:00:01", "00:00:02", "00:00:03", "00:00:04", "00:00:05", "00:00:06", "00:00:07", "00:00:08"
        };
        invalidate();
    }

    public void setShowTimeLine(boolean showTimeLine) {
        this.mShowTimeLine = showTimeLine;
        invalidate();
    }

    /*public void setTimeElapsedIndicatorOffset(float timeElapsedIndicatorOffset) {
        this.mTimeElapsedIndicatorOffset = timeElapsedIndicatorOffset;
        invalidate();
    }*/

    public void setWaveDrawConfig(int waveFormAreaRangeFlag) {
        Resources res = getResources();
        mWaveFormRange = res.getDimension(R.dimen.wave_form_view_width);

        DisplayMetrics dm = res.getDisplayMetrics();
        if (dm.widthPixels / dm.density != 360) {
            mWaveFormRange = dm.widthPixels;
        }

        mWaveFormFrameWidth = res.getDimensionPixelSize(R.dimen.wave_form_size);
//        mWaveFormMarginTop = res.getDimensionPixelSize(R.dimen.wave_form_margin_top);
        mTimeLineCellWidth = res.getDimensionPixelSize(R.dimen.time_line_cell_width);
        mTimePaddingLeft = res.getDimensionPixelSize(R.dimen.time_padding_left);
        mTimeTextSize = res.getDimension(R.dimen.time_text_size);
        
        mTimeLineTranslate = mWaveFormRange / 2;
        
        mWaveFormDrawPaint = new Paint();
        mWaveFormDrawPaint.setColor(res.getColor(R.color.color_wave));
        mWaveFormDrawPaint.setStrokeWidth(mWaveFormFrameWidth);
        
        mTimeDrawPaint = new Paint();
//        mTimeDrawPaint.setTypeface(SystemUtils.getTypeface("/system/fonts/Roboto-Light.ttf"));
        mTimeDrawPaint.setTextSize(mTimeTextSize);
        mTimeDrawPaint.setColor(res.getColor(R.color.wave_time_line_text_color));
        
        mTimelineDrawable = res.getDrawable(R.drawable.scale);
        
//        int markFlagBitmapResId = R.drawable.main_flag;
        int markFlagBitmapResId = R.drawable.trim_flag;
//        if (waveFormAreaRangeFlag != 0) {
//            markFlagBitmapResId = R.drawable.trim_flag;
//        }
        mMarkFlagBitmap = BitmapFactory.decodeResource(res, markFlagBitmapResId);
        
        int timeElapsedIndicatorResId = R.drawable.time_elapse_indicator;
        if (waveFormAreaRangeFlag != 0) {
            timeElapsedIndicatorResId = R.drawable.time_elapse_indicator_for_play;
        }
        mTimeElapsedIndicator = res.getDrawable(timeElapsedIndicatorResId);
        
        mHeaderBgDrawable = res.getDrawable(R.drawable.waveform_header_bg);
        mHeaderBgHeight = res.getDimensionPixelSize(R.dimen.wave_form_header_height);
        mBottomLineDrawable = res.getDrawable(R.drawable.waveform_separate_line);
//        mWaveBodyHeight = res.getDimensionPixelSize(R.dimen.wave_form_view_body_height);
        
        mWaveViewAreaRangeFlag = waveFormAreaRangeFlag;
    }

    public void setWaveFormNormalMode(boolean normalMode) {
        this.mWaveFormNormalMode = normalMode;
    }

    public void waveChanged(short[] waveFormDatas, ArrayList<MarkInfo> markFlagInfos, String[] timeDatas, float timeLineTranslate) {
        mWaveFormDatas = waveFormDatas;
        mMarkFlagInfos = markFlagInfos;
        /*if (markFlagInfos != null && markFlagInfos.size() > 0) {
            mMarkBitmaps = new SparseArray<SoftReference<Bitmap>>(markFlagInfos.size());
            
            final Resources res = getResources();
            for (int i = 0, size = markFlagInfos.size(); i < size; i++) {
                MarkInfo info = markFlagInfos.get(i);
                if (info != null) {
                    int index = info.getIndex() - 1;
                    if (index >= 0 && index < MARK_FLAG_NUM_DRAWABLES.length) {
                        Bitmap bitmap = BitmapFactory.decodeResource(res, MARK_FLAG_NUM_DRAWABLES[index]);
                        mMarkBitmaps.append(index, new SoftReference<Bitmap>(bitmap));
                    }
                }
            }
        }*/
        mTimeLineTranslate = timeLineTranslate;
        if (timeDatas != null) {
            mTimeDatas = timeDatas;
        }
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mWaveBodyHeight = bottom - top - mHeaderBgHeight;
        if (mWaveViewAreaRangeFlag == 0) {
            mWaveBodyHeight -= RECORD_TIME_INDICATOR_CIRCLE_HEIGHT;
        }
        Log.d(TAG, "Jim, mWaveBodyHeight: " + mWaveBodyHeight);
        mBaseLine = mWaveBodyHeight / 2 + mHeaderBgHeight;
        mRateY = (mWaveBodyHeight - DensityUtil.dip2px(getContext(), 10)) * 0.5f / Short.MAX_VALUE;
        mHeaderBgDrawable.setBounds(left, top, right, top + mHeaderBgHeight);
        mTimelineDrawable.setBounds(0, 0, mTimelineDrawable.getIntrinsicWidth(), mHeaderBgHeight + mWaveBodyHeight);
    }
}
