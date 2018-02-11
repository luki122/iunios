package com.aurora.note.activity.record;

import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.aurora.note.R;
import com.aurora.note.bean.MarkInfo;
import com.aurora.note.util.ConvertUtils;

import java.util.ArrayList;

/**
 * @author JimXia
 * 2014-7-26 上午10:08:15
 */
public class PlayWaveformHelper extends WaveformHelper {
//    private static final String TAG = "PlayWaveformHelper";
    
    private short[] mWholeWaveData;
    
    @Override
    protected void init(Resources res) {
        mWaveFormRange = res.getDimension(R.dimen.wave_form_view_width);

        DisplayMetrics dm = res.getDisplayMetrics();
        if (dm.widthPixels / dm.density != 360) {
            mWaveFormRange = dm.widthPixels;
        }

        mTimeLineOffsetBase = mWaveFormRange / 2;
        mTimeBase = (int)(mWaveFormRange / mWaveFormFrameWidth) * 10;
        mExpectedDataNum = (int)mWaveFormRange / DIVIDER;
        mTimeStringNeedUpdatedWidth = res.getDimension(R.dimen.time_string_need_updated_width_play);
    }
        
    public void setWholeWaveData(short[] wholeWaveData) {
        mWholeWaveData = wholeWaveData;
//        if (wholeWaveData != null && wholeWaveData.length > 0) {
//            debugWaveData(wholeWaveData, "whole wave data.");
//        }
    }
    
    @Override
    public short[] prepareWaveformData(short[] waveFormDatas) {
        final ArrayList<Short> waveDataBuffer = mWaveFormDatas;
        if (waveFormDatas != null) {
            waveDataBuffer.clear();
            for (int i = 0; i < waveFormDatas.length; i ++) {
                waveDataBuffer.add(waveFormDatas[i]);
            }
        }
        return convertList2Array(waveDataBuffer);
    }
    
    @Override
    protected void calculateTimeLineTimeData(long timeElapsed, WaveParams params) {
        if (timeElapsed >= (mTimeBase + 1000)) {
            params.isTimelineUpdated = true;
            if (mTimeLineOffset > mTimeStringNeedUpdatedWidth) {
                for (int i = 0; i < TIMELINE_DATA_ARRAY_LENGTH; i ++) {
                    params.timelineData[i] = ConvertUtils.generateTimeString((i - 2) * 1000 + timeElapsed, 0);
                }
            } else {
                for (int i = 0; i < TIMELINE_DATA_ARRAY_LENGTH; i ++) {
                    params.timelineData[i] = ConvertUtils.generateTimeString((i - 3) * 1000 + timeElapsed, 0);
                }
            }
        }
//        super.calculateTimeLineTimeData(timeElapsed, params);
    }
    
    @Override
    public WaveParams calculateTimeLineOffsetAndTimeData(long timeElapsed) {
        WaveParams params = new WaveParams();
        calculateTimeLineOffset(timeElapsed, params);
        calculateTimeLineTimeData(timeElapsed, params);
        prepareDisplayMarkData(timeElapsed, params.displayMarkData);
        prepareDisplayWaveData(timeElapsed, params);
        
        return params;
    }
    
    private void prepareDisplayWaveData(long timeElapsed, WaveParams params) {
        if (mWholeWaveData != null && mWholeWaveData.length > 0) {
            int waveDataSize = 0;
            long startElapsedTime = 0;
            if (timeElapsed <= mTimeBase) {
                // waveDataSize = (int)(timeElapsed * GAP / 1000);
                waveDataSize = (int)((mTimeBase + timeElapsed) * GAP / 1000);
            } else {
                startElapsedTime = timeElapsed - mTimeBase;
                waveDataSize = mTimeBase / 10;
            }
            params.waveData = new short[waveDataSize];
            int startIndex = (int)(startElapsedTime * GAP / 1000);
//            Log.d(TAG, "Jim, startIndex: " + startIndex + ", startElapsedTime: " + startElapsedTime +
//                    ", timeElapsed: " + timeElapsed + ", mWholeWaveData.length: " + mWholeWaveData.length);
            for (int i = 0; i < waveDataSize; i ++) {
                int index = startIndex + i;
                if (index >= mWholeWaveData.length) {
                    params.waveData[i] = 0;
                } else {
                    params.waveData[i] = mWholeWaveData[index];
                }
            }
        }
    }
    
    @Override
    protected void prepareDisplayMarkData(long elapsedTime, ArrayList<MarkInfo> displayMarkData) {
        if (mWholeMarkData != null && mWholeMarkData.size() > 0 && displayMarkData != null) {
            displayMarkData.clear();
            long startElapsedTime = 0;
            long endElapsedTime = elapsedTime + mTimeBase;
            if (elapsedTime > mTimeBase) {
                startElapsedTime = elapsedTime - mTimeBase;
            }
            
            long elapsed = endElapsedTime - startElapsedTime;
            int markInfoSize = mWholeMarkData.size();
            if (markInfoSize > 0) {
                long firstElpasedTime = mWholeMarkData.get(0).getMarkElpasedTime();
                long lastElapsedTime = mWholeMarkData.get(markInfoSize - 1).getMarkElpasedTime();
                
                if (firstElpasedTime <= endElapsedTime && (lastElapsedTime >= startElapsedTime || startElapsedTime - lastElapsedTime < 300)) {
                    int startIndex = 0;
                    for (int i = 0; i < markInfoSize; i ++) {
                        long childElapsedTime = mWholeMarkData.get(i).getMarkElpasedTime();
                        if (childElapsedTime >= startElapsedTime) {
                            startIndex = i;
                            break;
                        } else {
                            if (childElapsedTime < startElapsedTime && (startElapsedTime - childElapsedTime) < 300) {
                                startIndex = i;
                                break;
                            }
                        }
                    }         
                    
                    int endIndex = markInfoSize;
                    for (int i = startIndex; i < markInfoSize; i ++) {
                        if (mWholeMarkData.get(i).getMarkElpasedTime() != endElapsedTime) {
                            if (mWholeMarkData.get(i).getMarkElpasedTime() > endElapsedTime) {
                                endIndex = i;
                                break;
                            }
                        } else {
                            endIndex = i + 1;
                            break;
                        }
                    }
       
                    if (startIndex >= 0 && startIndex < markInfoSize && endIndex > startIndex && endIndex <= markInfoSize) {
                        for (int i = startIndex; i < endIndex; i ++) {
                            MarkInfo info = mWholeMarkData.get(i);
                            long markElapsedTime = info.getMarkElpasedTime();
                            float displayPositionX = 0.0f;
                            if (startElapsedTime != 0) {
                                displayPositionX = (markElapsedTime - startElapsedTime) * 1.0f / elapsed * mWaveFormRange;
                            } else {
                                displayPositionX = markElapsedTime * 1.0f / elapsed * (mWaveFormRange - mTimeLineOffset) + mTimeLineOffset;
                            }
//                            Log.d(TAG, "Jim, play, markElapsedTime: " + markElapsedTime + ", elapsedTime: " + elapsedTime +
//                                    ", startElapsedTime: " + startElapsedTime + ", elapsed: " + elapsed +
//                                    ", mWaveFormRange: " + mWaveFormRange + ", mTimeLineOffset: " + mTimeLineOffset +
//                                    ", displayPositionX: " + displayPositionX);
                            info.setDisplayPositionX(displayPositionX);
                            displayMarkData.add(info);
                        }
                    }
                }
            }
        }
    }
}
