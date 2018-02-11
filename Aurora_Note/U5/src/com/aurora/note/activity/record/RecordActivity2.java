
package com.aurora.note.activity.record;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.aurora.note.R;
import com.aurora.note.activity.BaseActivity;
import com.aurora.note.activity.record.FrameAnimationUtil.FrameAnimationListener;
import com.aurora.note.activity.record.WaveformHelper.WaveParams;
import com.aurora.note.bean.MarkInfo;
import com.aurora.note.bean.RecorderInfo;
import com.aurora.note.db.RecorderAdapter;
import com.aurora.note.util.Globals;
import com.aurora.note.util.SDcardManager;
import com.aurora.note.util.TimeUtils;
import com.aurora.note.util.ToastUtil;
import com.aurora.note.widget.ClockView;
import com.aurora.note.widget.WaveformView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 录音界面
 * @author JimXia
 * 2014年7月4日 上午11:47:07
 */
public class RecordActivity2 extends BaseActivity implements OnClickListener {
    private static final String TAG = "RecordActivity2";
    
    private static final int MSG_WHAT_RECORD_ERROR = 1;
    private static final int MSG_WHAT_PREPARE_RECORD_ERROR = 2;
    private static final int MSG_WHAT_REFRESH_WAVEFORM = 3;
    private static final int MSG_WHAT_RECORD_ENDED = 4;
    
    private WaveformView mWaveformView;
    private File mRecordFile;
    private File mRecordWaveFile;
    private volatile boolean mIsRecord = false;
    private volatile boolean mIsPause = false;
    
//    private TextView mDurationTv;
    private ClockView mDurationCv;
    private ImageView mMarkIv;
    private ImageView mStartIv;
    private ImageView mPauseIv;
    private ImageView mAnimationIv;
    private ImageView mStopIv;
    
//    private long mDuration = 0;
    private AtomicLong mDuration = new AtomicLong(0);
    
    private TelephonyManager mTelephonyManager;
    private boolean mPausedByCall = false; // 因为接通电话而暂停
    
    private Intent mProtectService;    
    
    private static final int[] RECORD_2_PAUSE_ANIM_RES_IDS = {
        R.drawable.record_2_pause_00000, R.drawable.record_2_pause_00001,
        R.drawable.record_2_pause_00002, R.drawable.record_2_pause_00003,
        R.drawable.record_2_pause_00004, R.drawable.record_2_pause_00005,
        R.drawable.record_2_pause_00006, R.drawable.record_2_pause_00007,
        R.drawable.record_2_pause_00008, R.drawable.record_2_pause_00009,
        R.drawable.record_2_pause_00010, R.drawable.record_2_pause_00011,
        R.drawable.record_2_pause_00012, R.drawable.record_2_pause_00013,
        R.drawable.record_2_pause_00014
    };
    
    private static final int[] PAUSE_2_RECORD_ANIM_RES_IDS = {
        R.drawable.pause_2_record_00000, R.drawable.pause_2_record_00001,
        R.drawable.pause_2_record_00002, R.drawable.pause_2_record_00003,
        R.drawable.pause_2_record_00004, R.drawable.pause_2_record_00005,
        R.drawable.pause_2_record_00006, R.drawable.pause_2_record_00007,
        R.drawable.pause_2_record_00008, R.drawable.pause_2_record_00009,
        R.drawable.pause_2_record_00010, R.drawable.pause_2_record_00011,
        R.drawable.pause_2_record_00012, R.drawable.pause_2_record_00013,
        R.drawable.pause_2_record_00014
    };
    private FrameAnimationUtil mFrameAnimationPause2Record;
    private FrameAnimationUtil mFrameAnimationRecord2Pause;
    private int mMarkIndex = 1;
    private WaveformHelper mWaveformHelper;
    private long mTotalWaveDataLength = 0; // 记录保存的波形数据一共有多少个short，便于调试
    
    public static boolean sIsRecording = false;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.aurora.utils.SystemUtils.setStatusBarBackgroundTransparent(this);

        setContentView(R.layout.record_activity_2);
        initView();
        
        startProtectService();
        startRecord(false);
        sIsRecording = true;
    }
    
    private void startProtectService() {
        if (mProtectService == null) {
            mProtectService = new Intent(this, ProtectService.class);
        }
        startService(mProtectService);
    }
    
    private void stopProtectService() {
        if (mProtectService != null) {
            stopService(mProtectService);
        }
    }

    private void initView() {
        mWaveformView = (WaveformView) findViewById(R.id.waveform_view);
        mWaveformView.setWaveDrawConfig(0);
        mWaveformView.reset();

        DisplayMetrics dm = getResources().getDisplayMetrics();
	    if (dm.widthPixels / dm.density != 360) {
	        LayoutParams params = mWaveformView.getLayoutParams();
	        params.width = dm.widthPixels;
	    }

        mDurationCv = (ClockView) findViewById(R.id.duration_cv);
        
        mMarkIv = (ImageView) findViewById(R.id.mark_iv);
        mStartIv = (ImageView) findViewById(R.id.start_iv);
        mPauseIv = (ImageView) findViewById(R.id.pause_iv);
        
        mAnimationIv = (ImageView) findViewById(R.id.animation_iv);
        mFrameAnimationPause2Record = new FrameAnimationUtil(RECORD_2_PAUSE_ANIM_RES_IDS, true, false, mAnimationIv);
        mFrameAnimationPause2Record.setAnimationListener(new FrameAnimationListener() {
            @Override
            public void onAnimationEnd() {
                mAnimationIv.setVisibility(View.GONE);
                mStartIv.setVisibility(View.VISIBLE);
                mFrameAnimationPause2Record.reset();
            }
        });
        mFrameAnimationRecord2Pause = new FrameAnimationUtil(PAUSE_2_RECORD_ANIM_RES_IDS, true, false, mAnimationIv);
        mFrameAnimationRecord2Pause.setAnimationListener(new FrameAnimationListener() {
            @Override
            public void onAnimationEnd() {
                mAnimationIv.setVisibility(View.GONE);
                mPauseIv.setVisibility(View.VISIBLE);
                mFrameAnimationRecord2Pause.reset();
            }
        });
        
        mStopIv = (ImageView) findViewById(R.id.stop_iv);
        
        mMarkIv.setOnClickListener(this);
        mStartIv.setOnClickListener(this);
        mPauseIv.setOnClickListener(this);
        mStopIv.setOnClickListener(this);
        
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }
    
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        private boolean mRing = false;
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING: // 来电
                    Log.d(TAG, "Jim, CALL_STATE_RINGING");
                    mRing = true;
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.d(TAG, "Jim, CALL_STATE_OFFHOOK");
                    if (mRing) {
                        // 电话接通了
                        pauseRecord(false);
                        mPausedByCall = true;
//                        mRing = false;
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE: // 挂断电话
                    Log.d(TAG, "Jim, CALL_STATE_IDLE");
                    mRing = false;
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
        
    };
    
    @Override
    protected void onResume() {
        super.onResume();

        com.aurora.utils.SystemUtils.switchStatusBarColorMode(
                com.aurora.utils.SystemUtils.STATUS_BAR_MODE_BLACK, this);

        if (mPausedByCall) {
            mPausedByCall = false;
            startRecord(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mark_iv:
                mark();
                break;
            case R.id.start_iv:
                startRecord(true);
                break;
            case R.id.pause_iv:
                pauseRecord(true);
                break;
            case R.id.stop_iv:
                stopRecord();
                break;
        }
    }
    
    private void mark() {
        if (mMarkIndex > RecordConstants.MAX_MARK_NUM) {
            Log.e(TAG, "Jim, reached max mark num.");
            return;
        }
        mWaveformHelper.addMarkInfo(new MarkInfo(mMarkIndex, mDuration.get()));
        mMarkIndex ++;
//        Log.d(TAG, "Jim, mark, duration: " + mDuration.get());
    }
    
    @Override
    public void onBackPressed() {
//        stopRecord();
        handleBackPressed();
    }
    
    private void handleBackPressed() {
        new AlertDialog.Builder(this)
        .setTitle(R.string.cancel_record_title)
        .setMessage(R.string.cancel_record_msg)
        .setPositiveButton(R.string.delete_confirm_ok_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                close();
                setResult(RESULT_CANCELED);
                finish();
                sIsRecording = false;
            }
        })
        .setNegativeButton(R.string.delete_confirm_cancel_btn, null)
        .create()
        .show();
    }
    
    private boolean checkSDCard() {
        if (!SDcardManager.checkSDCardMount()) {
            ToastUtil.longToast(R.string.sdcard_not_mounted);
            return false;
        }
        if (!SDcardManager.checkSDCardAvailableSize()) {
            ToastUtil.longToast(R.string.sd_space_not_enough);
            return false;
        }
        return true;
    }
    
    private void startRecord(boolean needAnimation) {
        if (!checkSDCard()) {
            return;
        }
        if (!Globals.SOUND_DIR.exists()) {
            Globals.SOUND_DIR.mkdirs();
        }

        mMarkIv.setEnabled(true);
        if (needAnimation) {
            mAnimationIv.setVisibility(View.VISIBLE);
            mStartIv.setVisibility(View.GONE);
            mPauseIv.setVisibility(View.GONE);
            mFrameAnimationRecord2Pause.startAnimation();
        } else {
            mStartIv.setVisibility(View.GONE);
            mAnimationIv.setVisibility(View.GONE);
            mPauseIv.setVisibility(View.VISIBLE);
        }
        
        if (mRecordFile == null) {
            String recordBaseName = "YY" + DateFormat.format("yyyyMMdd_HHmmss", Calendar.getInstance(Locale.CHINA));
            mRecordFile = new File(Globals.SOUND_DIR, recordBaseName + RecordConstants.EXTENSION_NAME);
            mRecordWaveFile = new File(Globals.SOUND_DIR, recordBaseName + RecordConstants.EXTENSION_NAME +
                    RecordConstants.WAVE_EXTENSION_NAME);
            mWaveformHelper = new WaveformHelper();
        }
        
        RecordingThread recordingThread = new RecordingThread();
        mWaveformHelper.setOnWaveformDataConvertedListener(recordingThread);
        recordingThread.start();
    }

    private void stopRecord() {
        mMarkIv.setEnabled(false);
        close();
//        saveRecordInfo2Database();
        
        Intent intent = new Intent();
        intent.putExtra("recordFileName", mRecordFile.getAbsolutePath());
        intent.putExtra("recordTime", TimeUtils.getStringByDate());
        intent.putExtra("recordDuration", TimeUtils.getDateStr(mDuration.get()));
        setResult(RESULT_OK, intent);
        finish();
        sIsRecording = false;
    }
    
    private void saveRecordInfo2Database() {
        RecorderAdapter recorderDb = new RecorderAdapter(this);
        recorderDb.open();
        RecorderInfo info = new RecorderInfo();
        info.setPath(mRecordFile.getAbsolutePath());
        info.setName(mRecordFile.getName());
        info.setMarks(mWaveformHelper.getAllMarkInfo());
        info.setDuration(mDuration.get());
        recorderDb.insertOrUpdate(info);
        recorderDb.close();
    }
    
    private void pauseRecord(boolean needAnimation) {
        mMarkIv.setEnabled(false);
        if (needAnimation) {
            mAnimationIv.setVisibility(View.VISIBLE);
            mStartIv.setVisibility(View.GONE);
            mPauseIv.setVisibility(View.GONE);
            mFrameAnimationPause2Record.startAnimation();
        } else {
            mPauseIv.setVisibility(View.GONE);
            mAnimationIv.setVisibility(View.GONE);
            mStartIv.setVisibility(View.VISIBLE);
        }
        
        mIsRecord = false;
        mIsPause = true;
    }

    private void close() {
        mIsPause = false;
        mIsRecord = false;
    }
    
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_WHAT_REFRESH_WAVEFORM:
                WaveParams params = (WaveParams) msg.obj;
//                mDuration = params.duration;
                updateDurationText();
                
                mWaveformView.waveChanged(params.waveData, params.displayMarkData,
                        params.isTimelineUpdated? params.timelineData: null, params.timeLineTranslate);
                break;
            case MSG_WHAT_PREPARE_RECORD_ERROR:
                ToastUtil.shortToast(msg.arg1);
                break;
            case MSG_WHAT_RECORD_ENDED:
                break;
        }
    }
    
    private void updateDurationText() {
//        mDurationTv.setText(TimeUtils.getDateStr(mDuration.get()));
        mDurationCv.timeChanged(mDuration.get());
    }
    
    private class RecordingThread extends Thread implements WaveformHelper.OnWaveformDataConvertedListener {
        private ArrayList<short[]> mWaveDataList = new ArrayList<short[]>();
        private int mWaveDataLength = 0;
        private RandomAccessFile mRecordWavFile;
        
        public RecordingThread() {
            super("RecordThread");
        }
        
        @Override
        public void onWaveformDataConverted(short[] waveformData) {
//            long _beginTime = System.currentTimeMillis();
            if (waveformData != null && waveformData.length > 0) {
                mWaveDataList.add(waveformData);
                mTotalWaveDataLength += waveformData.length;
                mWaveDataLength += waveformData.length;
//                Log.d(TAG, "Jim, mTotalWaveDataLength: " + mTotalWaveDataLength + ", mWaveDataLength: " + mWaveDataLength);
                if (mWaveDataLength >= 512) {
                    saveWaveData2File();
                }
            } else {
                Log.e(TAG, "Received wave data array is null or empty");
            }
//            Log.d(TAG, "Jim, time, onWaveformDataConverted use time: " + (System.currentTimeMillis() - _beginTime));
        }
        
        private void closeRecordWavFile() {
            if (mRecordWavFile != null) {
                try {
                    mRecordWavFile.close();
                } catch (IOException e) {
                    Log.e(TAG, "close wave file error.", e);
                }
                mRecordWavFile = null;
            }
        }
        
        private void saveWaveData2File() {
            RandomAccessFile raf = mRecordWavFile;
            if (raf == null) {
                try {
                    raf = new RandomAccessFile(mRecordWaveFile, "rw");
                    if (raf.length() > 0) {
                        raf.seek(raf.length());
                    }
                    mRecordWavFile = raf;
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "open wave file error.", e);
                } catch (IOException e) {
                    Log.e(TAG, "seek wave file error.", e);
                }
            }
            if (raf != null) {
                byte[] buf = new byte[mWaveDataLength * 2];
                ShortBuffer shortBuffer = ByteBuffer.wrap(buf).order(ByteOrder.nativeOrder()).asShortBuffer();
                int count = 0;
                for (int i = 0, size = mWaveDataList.size(); i < size; i ++) {
                    short[] data = mWaveDataList.get(i);
                    shortBuffer.put(data);
                    count += data.length;
                }
                Log.d(TAG, "Jim, mWaveDataLength: " + mWaveDataLength + ", actual length: " + count);
                try {
                    raf.write(buf);
                    Log.d(TAG, "Jim, write wave data to file successfully, byte count: " + buf.length);
                    mWaveDataLength = 0;
                    mWaveDataList.clear();
                } catch (IOException e) {
                    Log.e(TAG, "write wave data error.", e);
                }
            }
        }
        
        private int getBestMinBufferSize(int minBufferSize) {
            int shortNum = minBufferSize / 2;
            boolean isAdjust = false;
            if (shortNum % mWaveformHelper.mRateX != 0) {
                shortNum = (shortNum / mWaveformHelper.mRateX + 1) * mWaveformHelper.mRateX;
                isAdjust = true;
            }
            if (shortNum / mWaveformHelper.mRateX < 4) {
                shortNum = mWaveformHelper.mRateX * 4;
                isAdjust = true;
            }
            if (isAdjust) {
                minBufferSize = shortNum * 2;
                Log.d(TAG, "Jim, minBufferSize is adjusted to: " + minBufferSize);
            }
            
            return minBufferSize;
        }
        
        @Override
        public void run() {
//            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
            AudioRecord audioRecord = null;
            boolean hasError = false;
            int minBufferSize = -1;
            try {
                minBufferSize = AudioRecord.getMinBufferSize(RecordConstants.SAMPLING_RATE,
                        RecordConstants.AUDIO_RECORD_CHANNEL_CONFIG, RecordConstants.AUDIO_RECORD_AUDIO_FORMAT);
                if (minBufferSize == AudioRecord.ERROR_BAD_VALUE || minBufferSize == AudioRecord.ERROR) {
                    Log.d(TAG, "Get min buffer size error, return " + minBufferSize);
                    reportError(R.string.prepare_record_error);
                    return;
                }
                Log.d(TAG, "Jim, min buffer size: " + minBufferSize);                
                minBufferSize = getBestMinBufferSize(minBufferSize);
                
                audioRecord = new AudioRecord(RecordConstants.AUDIO_SOURCE,
                        RecordConstants.SAMPLING_RATE, RecordConstants.AUDIO_RECORD_CHANNEL_CONFIG,
                        RecordConstants.AUDIO_RECORD_AUDIO_FORMAT, minBufferSize);
                audioRecord.startRecording();
                mIsRecord = true;
            } catch (IllegalStateException e) {
                hasError = true;
                Log.e(TAG, "create AudioRecord error", e);
            } catch (Exception e) {
                Log.e(TAG, "create AudioRecord error", e);
                hasError = true;
            }
            if (hasError) {
                reportError(R.string.prepare_record_error);
                return;
            }
            
            RandomAccessFile dos = null;
            try {
                File file = mRecordFile;
                if (file.exists() && !mIsPause) {
                    file.delete();
                } else {
                    mIsPause = false;
                }
                dos = new RandomAccessFile(file, "rw");
                final long fileLength = file.length();
                int payloadSize = 0;
                final short nChannels = RecordUtil.getChannelCount(RecordConstants.AUDIO_RECORD_CHANNEL_CONFIG);
                final short bitCount = RecordUtil.getAudioFormatBitCount(RecordConstants.AUDIO_RECORD_AUDIO_FORMAT);
                if (fileLength <= 20) {
                    dos.writeBytes(RecordConstants.WAV_FILE_HEADER_RIFF);
                    dos.writeInt(0);
                    dos.writeBytes(RecordConstants.WAV_FILE_HEADER_WAVE);
                    dos.writeBytes("fmt ");
                    dos.writeInt(Integer.reverseBytes(0x10));
                    dos.writeShort(Short.reverseBytes(RecordConstants.WAVE_FORMAT_PCM));
                    dos.writeShort(Short.reverseBytes(nChannels)); // 声道数，1-单声道, 2-双声道
                    dos.writeInt(Integer.reverseBytes(RecordConstants.SAMPLING_RATE));
                    dos.writeInt(Integer.reverseBytes((nChannels * RecordConstants.SAMPLING_RATE * bitCount) / 8));
                    dos.writeShort(Short.reverseBytes((short)2));
                    dos.writeShort(Short.reverseBytes(bitCount));
                    dos.writeBytes("data");
                    dos.writeInt(0);
                } else {
                    dos.seek(fileLength);
                    payloadSize = (int)fileLength - RecordConstants.WAV_FILE_HEADER_CHUNK_SIZE;
                }

                byte[] audiodata = new byte[minBufferSize];
                int readsize = 0;
//                long _beginTime = 0;
                while (mIsRecord) {
//                    _beginTime = System.currentTimeMillis();
                    readsize = audioRecord.read(audiodata, 0, audiodata.length);
//                    Log.d(TAG, "Jim, time, audio record read use time: " + (System.currentTimeMillis() - _beginTime));
                    if (readsize > 0) {
//                            _beginTime = System.currentTimeMillis();
                            dos.write(audiodata, 0, readsize);
//                            Log.d(TAG, "Jim, time, write file use time: " + (System.currentTimeMillis() - _beginTime));
                            
                            payloadSize += readsize;
                            
                            short[] tmpBuf = new short[readsize / 2];
                            ByteBuffer.wrap(audiodata).order(ByteOrder.nativeOrder()).asShortBuffer().get(tmpBuf);
//                            denoise(tmpBuf, 0, tmpBuf.length);
//                            RecordUtil.debugWaveformData(tmpBuf, TAG);
                            
                            int duration = (int)RecordUtil.getDuration(payloadSize,
                                    RecordConstants.SAMPLING_RATE, nChannels, bitCount / 8);
                            mDuration.set(duration);
//                            Log.d(TAG, "Jim, total bytes: " + payloadSize + ", duration: " + duration + ", minBufferSize: " +
//                                    minBufferSize + ", readsize: " + readsize);
                            
                            WaveParams params = mWaveformHelper.calculateTimeLineOffsetAndTimeData(duration);
//                            Log.d(TAG, "Jim, mark, duration: " + duration + ", marks: " + params.displayMarkData);
                            params.waveData = mWaveformHelper.prepareWaveformData(tmpBuf);
//                            RecordUtil.debugWaveformData(params.waveData, "Jim, after waveform data: ");
                            params.duration = duration;
                            refreshWaveform(params);
                    } else {
                        Log.e(TAG, "Jim, audioRecord.read return invalid value: " + readsize);
                    }
                }
                
                dos.seek(4);
                dos.writeInt(Integer.reverseBytes(payloadSize));
                dos.seek(40);
                dos.writeInt(Integer.reverseBytes(payloadSize));
                dos.close();
                saveWaveData2File();
                saveRecordInfo2Database();
                Log.d(TAG, "Jim, Total wave data length: " + mTotalWaveDataLength);
                mHandler.obtainMessage(MSG_WHAT_RECORD_ENDED).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Jim, record error.", e);
                reportError(e);
            } finally {
                audioRecord.stop();
                audioRecord.release();
                closeRecordWavFile();
            }
        }
        
        // 噪音消除算法
        /*private void denoise(short[] originalData, int off, int len) {
            for (int i = 0; i < len; i++) {
                int j = originalData[i + off];
                originalData[i + off] = (short) (j >> 2);
            }
        }*/
        
        void refreshWaveform(WaveParams params) {
            mHandler.removeMessages(MSG_WHAT_REFRESH_WAVEFORM);
            mHandler.obtainMessage(MSG_WHAT_REFRESH_WAVEFORM, params).sendToTarget();
        }
        
        private void reportError(Throwable t) {
            mHandler.obtainMessage(MSG_WHAT_RECORD_ERROR, t).sendToTarget();
        }
        
        private void reportError(int msgResId) {
            mHandler.obtainMessage(MSG_WHAT_PREPARE_RECORD_ERROR, msgResId, -1).sendToTarget();
        }
    }
    
    /**
     * Computes the decibel level of the current sound buffer and updates the appropriate text
     * view.
     */
    double updateDecibelLevel(short[] data) {
        // Compute the root-mean-squared of the sound buffer and then apply the formula for
        // computing the decibel level, 20 * log_10(rms). This is an uncalibrated calculation
        // that assumes no noise in the samples; with 16-bit recording, it can range from
        // -90 dB to 0 dB.
        double sum = 0;

        for (short rawSample : data) {
            double sample = rawSample / 32768.0;
            sum += sample * sample;
        }

        double rms = Math.sqrt(sum / data.length);
        return 20 * Math.log10(rms);
    }

    @Override
    protected void onDestroy() {
        close();
        if (mTelephonyManager != null) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
            mPhoneStateListener = null;
            mTelephonyManager = null;
        }
        stopProtectService();
        super.onDestroy();
    }
}
