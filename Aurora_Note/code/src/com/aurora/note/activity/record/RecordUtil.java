package com.aurora.note.activity.record;

import android.media.AudioFormat;
import android.util.Log;

/**
 * @author JimXia
 * 2014年7月9日 上午11:19:21
 */
public class RecordUtil {
    
    public static short getChannelCount(int channelConfig) {
        switch (channelConfig) {
            case AudioFormat.CHANNEL_IN_MONO:
            case AudioFormat.CHANNEL_OUT_MONO:
                return 1;
            case AudioFormat.CHANNEL_IN_STEREO:
//            case AudioFormat.CHANNEL_OUT_STEREO:
                return 2;
            default:
                return 1;
        }
    }
    
    public static short getAudioFormatBitCount(int audioFormat) {
        switch (audioFormat) {
            case AudioFormat.ENCODING_PCM_16BIT:
                return 16;
            case AudioFormat.ENCODING_PCM_8BIT:
                return 8;
            default:
                return 8;
        }
    }
    
    public static long getDuration(long bytes, int sampleRate, int channelCount, int sampleByte) {
        return bytes * 1000 / (sampleRate * channelCount * sampleByte);
//        return (bytes / (sampleRate * channelCount * sampleByte)) * 1000;
    }
    
    public static int byte2Int(byte[] b) {
        return (b[0] & 0xff) | ((b[1] & 0xff) << 8) | ((b[2] & 0xff) << 16) | ((b[3] & 0xff) << 24);
    }
    
    public static int byte2Short(byte[] b) {
        return (b[0] & 0xff) | ((b[1] & 0xff) << 8);
    }
    
    public static void debugTimelineData(String[] timelineData, String tag) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < timelineData.length; i ++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(timelineData[i]);
        }
        Log.d(tag, "Jim, timeline data: " + sb.toString());
    }
    
    public static void debugWaveformData(short[] waveformData, String tag) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < waveformData.length; i ++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(Short.toString(waveformData[i]));
            if ((i + 1) % 16 == 0) {
                sb.append("\n");
            }
        }
        Log.d(tag, "Jim, waveform data: \n" + sb.toString());
    }
}