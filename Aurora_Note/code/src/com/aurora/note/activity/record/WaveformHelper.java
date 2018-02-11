package com.aurora.note.activity.record;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;

import com.aurora.note.NoteApp;
import com.aurora.note.R;
import com.aurora.note.bean.MarkInfo;
import com.aurora.note.util.ConvertUtils;

import java.util.ArrayList;

/**
 * @author JimXia
 * 2014-7-26 上午9:38:20
 */
public class WaveformHelper {
    private static final String TAG = "WaveformHelper";
    
    public static final int GAP = 50;
    public static final int TIMELINE_DATA_ARRAY_LENGTH = 8;
    public static final int DIVIDER = 1;
    
    public int mRateX = (RecordConstants.SAMPLING_RATE *
            RecordUtil.getChannelCount(RecordConstants.AUDIO_RECORD_CHANNEL_CONFIG)) / GAP;
    protected ArrayList<MarkInfo> mWholeMarkData = new ArrayList<MarkInfo>();
    protected float mTimeLineOffset;
    protected float mTimeLineOffsetBase;
    protected float mTimeLineOffsetScale;
    protected float mTimeStringNeedUpdatedWidth;
    private boolean mFirstTimeStringNeedUpdated = true;
    private boolean mSecondTimeStringNeedUpdated;
    protected float mWaveFormRange;
    protected int mWaveFormFrameWidth;
    protected int mTimeBase;
    private OnWaveformDataConvertedListener mOnWaveformDataConvertedListener;
    protected ArrayList<Short> mWaveFormDatas = new ArrayList<Short>();
    protected int mExpectedDataNum;
    
    public WaveformHelper() {
        final Resources res = NoteApp.ysApp.getResources();
        mTimeLineOffsetScale = res.getDimension(R.dimen.time_line_offset_scale);
        mTimeStringNeedUpdatedWidth = res.getDimension(R.dimen.time_string_need_updated_width_main);
        mWaveFormFrameWidth = res.getDimensionPixelSize(R.dimen.wave_form_size);
        
        init(res);
    }
    
    protected void init(Resources res) {
        /*final */float waveFormRange = res.getDimension(R.dimen.wave_form_view_width);

        DisplayMetrics dm = res.getDisplayMetrics();
        if (dm.widthPixels / dm.density != 360) {
            mWaveFormRange = dm.widthPixels;
        }

        mWaveFormRange = waveFormRange / 2.0f;
        mTimeLineOffsetBase = mWaveFormRange;
        mTimeBase = (int)(waveFormRange / mWaveFormFrameWidth) * 10;
        mExpectedDataNum = (int)waveFormRange / DIVIDER;
    }
    
    public short[] prepareWaveformData(short[] waveFormDatas) {
        final ArrayList<Short> waveDataBuffer = mWaveFormDatas;
        
        short[] data = convertData(waveFormDatas);
        for (int i = 0; i < data.length; i ++) {
            waveDataBuffer.add(data[i]);
        }
        if (mOnWaveformDataConvertedListener != null) {
            mOnWaveformDataConvertedListener.onWaveformDataConverted(data);
        }
        final int unwanted = waveDataBuffer.size() - mExpectedDataNum;
        for (int i = 0; i < unwanted; i ++) {
            waveDataBuffer.remove(0);
        }
        return convertList2Array(waveDataBuffer);
    }
    
    protected static short[] convertList2Array(ArrayList<Short> waveData) {
        short[] arr = null;
        if (waveData == null) {
            return arr;
        }
        arr = new short[waveData.size()];
        for (int i = 0, size = waveData.size(); i < size; i ++) {
            arr[i] = waveData.get(i);
        }
        
        return arr;
    }
    
    public void addMarkInfo(MarkInfo markInfo) {
        synchronized (mWholeMarkData) {
            mWholeMarkData.add(markInfo);
        }
    }
    
    public void setWholeMarkInfo(ArrayList<MarkInfo> wholeMarkInfo) {
        if (wholeMarkInfo != null) {
            synchronized (mWholeMarkData) {
                mWholeMarkData.clear();
                mWholeMarkData.addAll(wholeMarkInfo);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public ArrayList<MarkInfo> getAllMarkInfo() {
        ArrayList<MarkInfo> wholeMarkInfo = null;
        synchronized (mWholeMarkData) {
            wholeMarkInfo = (ArrayList<MarkInfo>) mWholeMarkData.clone();
        }
        return wholeMarkInfo;
    }
    
    public static class WaveParams {
        public String[] timelineData;
        public ArrayList<MarkInfo> displayMarkData;
        public boolean isTimelineUpdated;
        public float timeLineTranslate;
        public int duration;
        public short[] waveData;
        
        public WaveParams() {
            timelineData = new String[TIMELINE_DATA_ARRAY_LENGTH];
            displayMarkData = new ArrayList<MarkInfo>();
        }
    }
    
    protected void calculateTimeLineOffset(long timeElapsed, WaveParams params) {
//        Log.d(TAG, "Jim, mTimeLineOffset: " + mTimeLineOffset + ", timeElapsed: " + timeElapsed +
//                ", mTimeBase: " + mTimeBase + ", mTimeLineOffsetScale: " + mTimeLineOffsetScale +
//                ", mTimeLineOffsetBase: " + mTimeLineOffsetBase);
        if (timeElapsed == 0) {
            mTimeLineOffset = mTimeLineOffsetBase - (timeElapsed * 1.0f / 10.0f * mTimeLineOffsetScale);
        } else if (mTimeLineOffset < 0 || timeElapsed >= mTimeBase) {
            mTimeLineOffset = -(((timeElapsed - mTimeBase) % 1000) * 1.0f / 10.0f) * mTimeLineOffsetScale;
        } else {
            mTimeLineOffset = mTimeLineOffsetBase - (timeElapsed * 1.0f / 10.0f * mTimeLineOffsetScale);
        }
        params.timeLineTranslate = mTimeLineOffset;
    }
    
    protected void calculateTimeLineTimeData(long timeElapsed, WaveParams params) {
        if (timeElapsed >= (mTimeBase + 1000)) {
            if (mTimeLineOffset > mTimeStringNeedUpdatedWidth) {
               if (mFirstTimeStringNeedUpdated) {
                   params.isTimelineUpdated = true;
                    for (int i = 0; i < TIMELINE_DATA_ARRAY_LENGTH; i ++) {
                        params.timelineData[i] = ConvertUtils.generateTimeString((i - 2) * 1000 + timeElapsed, 0);
                    }
                    mFirstTimeStringNeedUpdated = false;
                    mSecondTimeStringNeedUpdated = true;
               } 
            } else {
                if (mSecondTimeStringNeedUpdated) {
                    params.isTimelineUpdated = true;
                    for (int i = 0; i < TIMELINE_DATA_ARRAY_LENGTH; i ++) {
                        params.timelineData[i] = ConvertUtils.generateTimeString((i - 3) * 1000 + timeElapsed, 0);
                    }
                    mFirstTimeStringNeedUpdated = true;
                    mSecondTimeStringNeedUpdated = false;
                }
            }
            
//            Log.d(TAG, "Jim, timeElapsed: " + timeElapsed + ", mTimeBase: " + mTimeBase +
//                    ", mTimeLineOffset: " + mTimeLineOffset + ", mTimeStringNeedUpdatedWidth: " +
//                    mTimeStringNeedUpdatedWidth + ", mFirstTimeStringNeedUpdated: " + mFirstTimeStringNeedUpdated +
//                    ", mSecondTimeStringNeedUpdated: " + mSecondTimeStringNeedUpdated);
//            if (params.isTimelineUpdated) {
//                RecordUtil.debugTimelineData(params.timelineData, TAG + ", Jim timeline data, timeElapsed: " + timeElapsed +
//                        ", mTimeLineOffset: " + mTimeLineOffset);
//            }
        }
    }
    
    public WaveParams calculateTimeLineOffsetAndTimeData(long timeElapsed) {
        WaveParams params = new WaveParams();
        calculateTimeLineOffset(timeElapsed, params);
        calculateTimeLineTimeData(timeElapsed, params);
        prepareDisplayMarkData(timeElapsed, params.displayMarkData);
        
        return params;
    }
    
    protected void prepareDisplayMarkData(long elapsedTime, ArrayList<MarkInfo> displayMarkData) {
        if (mWholeMarkData != null && mWholeMarkData.size() > 0 && displayMarkData != null) {
            displayMarkData.clear();
            long startElapsedTime = 0;
            long endElapsedTime = elapsedTime;
            
            if (elapsedTime > mTimeBase) {
                startElapsedTime = elapsedTime - mTimeBase;
            }
            
            long elapsed = endElapsedTime - startElapsedTime;
            int markInfoSize = mWholeMarkData.size();
            if (markInfoSize > 0) {
                long firstElpasedTime = mWholeMarkData.get(0).getMarkElpasedTime();
                long lastElapsedTime = mWholeMarkData.get(markInfoSize - 1).getMarkElpasedTime();
                
                if (firstElpasedTime <= endElapsedTime &&
                    (lastElapsedTime >= startElapsedTime ||
                    (startElapsedTime - lastElapsedTime) < 300)) {
                    
                    int startIndex = 0;
                    for (int i = 0; i < markInfoSize; i ++) {
                        long childElapsedTime = mWholeMarkData.get(i).getMarkElpasedTime();
                        if (childElapsedTime < startElapsedTime) {
                            if (startElapsedTime - childElapsedTime < 300) {
                                startIndex = i;
                                break;
                            }
                        } else {
                            startIndex = i;
                            break;
                        }
                    }

                    int endIndex = markInfoSize;
                    for (int i = startIndex; i < markInfoSize; i ++) {
                        long childElapsedTime = mWholeMarkData.get(i).getMarkElpasedTime();
                        if (childElapsedTime != endElapsedTime) {
                            if (childElapsedTime > endElapsedTime) {
                                endIndex = i;
                                break;
                            }
                        } else {
                            endIndex = i + 1;
                            break;
                        }
                    }

                    if (startIndex >= 0 && startIndex < markInfoSize &&
                            startIndex < endIndex && endIndex <= markInfoSize) {
                        for (int i = startIndex; i < endIndex; i ++) {
                            MarkInfo info = mWholeMarkData.get(i);
                            long markElapsedTime = info.getMarkElpasedTime();
                            float displayPositionX = 0;
                            if (startElapsedTime != 0) {
                                displayPositionX = (markElapsedTime - startElapsedTime) * 1.0f / elapsed * mWaveFormRange;
                            } else {
                                displayPositionX = markElapsedTime * 1.0f / elapsed * (mWaveFormRange - mTimeLineOffset) + mTimeLineOffset;
                            }
                            info.setDisplayPositionX(displayPositionX);
//                            info.setLockScreenDisplayPositionX(mLockScreenWaveFormRange * displayPositionX / mWaveFormRange);
                            displayMarkData.add(info);
                        }
                    }
                }
            }
        }
    }
    
    public short[] convertData(short[] waveFormDatas) {
        return convertData(waveFormDatas, mRateX);
    }
    
    public byte[] convertData(byte[] waveFormDatas) {
        return convertData(waveFormDatas, mRateX);
    }
    
    void debugWaveData(short[] waveFormDatas, String tag) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int i = 0; i < waveFormDatas.length; i ++) {
            if (count > 0) {
                sb.append(" ");
            }
            sb.append(Short.toString(waveFormDatas[i]));
            count ++;
            if (count % 16 == 0) {
                sb.append("\n");
                count = 0;
            }
        }
        Log.d(TAG, tag + "\n" + sb.toString());
    }
    
    public static short[] convertData(short[] waveFormDatas, int rateX) {
        final int length = waveFormDatas.length;
        int newLength = length / rateX;
//        if (length % rateX != 0) {
//            newLength += 1;
//        }
        short[] data = new short[newLength];
        int index = 0;
        for (int i = 0; i < length && index < data.length; i += rateX) {
            short max = 0;
            for (int j = i; j < i + rateX && j < length; j ++) {
                short val = (short)Math.abs(waveFormDatas[j]);
                if (val > max) {
                    max = val;
                }
            }
            data[index ++] = max;
//            Log.d(TAG, "Jim, convertData, data[" + (index - 1) + "] = " + data[index - 1]);
        }
        
        if (data.length > 2) {
            data[0] = (short)(data[0] / 2 + data[1] / 2);
            for (int i = 1; i < data.length - 1; i ++) {
                data[i] = (short)(data[i - 1] / 3 + data[i] / 3 + data[i + 1] / 3);
//                Log.d(TAG, "Jim, convertData, data[" + i + "] = " + data[i]);
            }
            data[data.length - 1] = (short)(data[data.length - 2] / 2 + data[data.length - 1] / 2);
        }
        
        return data;
    }
    
    public static byte[] convertData(byte[] waveFormDatas, int rateX) {
        final int length = waveFormDatas.length;
        int newLength = length / rateX;
//        if (length % rateX != 0) {
//            newLength += 1;
//        }
        byte[] data = new byte[newLength];
        int index = 0;
        for (int i = 0; i < length && index < data.length; i += rateX) {
            byte max = 0;
            for (int j = i; j < i + rateX && j < length; j ++) {
                byte val = (byte)Math.abs(waveFormDatas[j]);
                if (val > max) {
                    max = val;
                }
            }
            data[index ++] = max;
        }
        
        if (data.length > 2) {
            data[0] = (byte)(data[0] / 2 + data[1] / 2);
            for (int i = 1; i < data.length - 1; i ++) {
                data[i] = (byte)(data[i - 1] / 3 + data[i] / 3 + data[i + 1] / 3);
            }
            data[data.length - 1] = (byte)(data[data.length - 2] / 2 + data[data.length - 1] / 2);
        }
        
        return data;
    }
    
    public static interface OnWaveformDataConvertedListener {
        void onWaveformDataConverted(short[] waveformData);
    }
    
    public void setOnWaveformDataConvertedListener(OnWaveformDataConvertedListener listener) {
        mOnWaveformDataConvertedListener = listener;
    }
}