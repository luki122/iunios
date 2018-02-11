package com.aurora.note.activity.record;

import android.media.AudioFormat;
import android.media.MediaRecorder;

/**
 * @author JimXia
 * 2014年7月9日 上午11:19:21
 */
public interface RecordConstants {
    public static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    public static final int SAMPLING_RATE = 8000;
//    public static final int SAMPLING_RATE = 44100;
    public static final int AUDIO_RECORD_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    public static final int AUDIO_RECORD_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    
    public static final short WAVE_FORMAT_PCM = 1; // 1表示为PCM形式的声音数据
    
    public static final int AUDIO_TRACK_CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO;
    public static final int AUDIO_TRACK_AUDIO_FORMAT = AUDIO_RECORD_AUDIO_FORMAT;
    
    public static final int WAV_FILE_HEADER_CHUNK_SIZE = 44;
    
    public static final String WAV_FILE_HEADER_RIFF = "RIFF";
    public static final String WAV_FILE_HEADER_WAVE = "WAVE";
    
    public static final String EXTENSION_NAME = ".wav";
    public static final String WAVE_EXTENSION_NAME = ".dat";
    
    public static final int MAX_MARK_NUM = 99;
}
