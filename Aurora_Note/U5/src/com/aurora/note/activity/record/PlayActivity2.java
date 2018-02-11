package com.aurora.note.activity.record;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Bundle;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.aurora.note.R;
import com.aurora.note.activity.BaseActivity;
import com.aurora.note.activity.record.FrameAnimationUtil.FrameAnimationListener;
import com.aurora.note.activity.record.WaveformHelper.WaveParams;
import com.aurora.note.bean.MarkInfo;
import com.aurora.note.bean.RecorderInfo;
import com.aurora.note.db.RecorderAdapter;
import com.aurora.note.util.FileLog;
import com.aurora.note.util.FileUtils;
import com.aurora.note.util.TimeUtils;
import com.aurora.note.util.ToastUtil;
import com.aurora.note.widget.ClockView;
import com.aurora.note.widget.PlaySeekBar;
import com.aurora.note.widget.WaveformViewForPlay;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayActivity2 extends BaseActivity implements OnClickListener {
	private static final String TAG = "PlayActivity2";
	
	public static final String EXTRA_SHORT_FILE_NAME = "shortFileName";
	
	private static final int MSG_WHAT_UPDATA_PROGRESS_FOR_MEDIA_PLAYER = 1;
	private static final int MSG_WHAT_REFRESH_WAVEFORM = 2;
	private static final int MSG_WHAT_PLAY_ERROR_EXCEPTION = 3;
	private static final int MSG_WHAT_PCM_PLAYER_STOPPED = 4;
	
	private static final long TIMER_PERIOD = 100;
	
	private PlaySeekBar mProgress;
//	private TextView mPlayTimeTv;
	private ClockView mPlayTimeCv;
	private TextView mTotalTimeTv;
	private TextView mFileNameTv;
	private ImageView mMarkIv;
    private ImageView mStartIv;
    private ImageView mPauseIv;
    private ImageView mAnimationIv;
    private ImageView mStopIv;
	
	private MediaPlayer mMediaPlayer;
	private volatile boolean mIsChanging = false;// 互斥变量，防止定时器与SeekBar拖动时进度冲突
	private Timer mTimer;
	private TimerTask mTimerTask;
	private String mRecordFilePath = "";
	
	private TelephonyManager mTelephonyManager;
	
	private boolean mIsPCMWavFormat = false;
	
	private volatile boolean mIsPlayPause = false;
	private int mPlayTotalTime;
	private volatile boolean mKeepRunning;
	private int mSampleRate;
	private PCMAudioTrack mAudioTrackPlayer;
	
	private WaveformViewForPlay mWaveformView;
	
	private final AtomicInteger mExpectedAudioTrackProgress = new AtomicInteger(-1);
	
	private static final int[] PLAY_2_PAUSE_ANIM_RES_IDS = {
        R.drawable.play_2_pause_00000, R.drawable.play_2_pause_00001,
        R.drawable.play_2_pause_00002, R.drawable.play_2_pause_00003,
        R.drawable.play_2_pause_00004, R.drawable.play_2_pause_00005,
        R.drawable.play_2_pause_00006, R.drawable.play_2_pause_00007,
        R.drawable.play_2_pause_00008, R.drawable.play_2_pause_00009,
        R.drawable.play_2_pause_00010, R.drawable.play_2_pause_00011,
        R.drawable.play_2_pause_00012, R.drawable.play_2_pause_00013,
        R.drawable.play_2_pause_00014
    };
    
    private static final int[] PAUSE_2_PLAY_ANIM_RES_IDS = {
        R.drawable.pause_2_play_00000, R.drawable.pause_2_play_00001,
        R.drawable.pause_2_play_00002, R.drawable.pause_2_play_00003,
        R.drawable.pause_2_play_00004, R.drawable.pause_2_play_00005,
        R.drawable.pause_2_play_00006, R.drawable.pause_2_play_00007,
        R.drawable.pause_2_play_00008, R.drawable.pause_2_play_00009,
        R.drawable.pause_2_play_00010, R.drawable.pause_2_play_00011,
        R.drawable.pause_2_play_00012, R.drawable.pause_2_play_00013,
        R.drawable.pause_2_play_00014
    };
	private FrameAnimationUtil mFrameAnimationPause2Play;
    private FrameAnimationUtil mFrameAnimationPlay2Pause;
	    
    private PlayWaveformHelper mPlayWaveformHelper;
    private RecorderAdapter mRecorderDb;
    private RecorderInfo mRecorderInfo;
    
    /*private */final Object mLock = new Object();
    /*private */AtomicBoolean mIsMarkInfoChanged = new AtomicBoolean(false);
    
    public static boolean sIsPlaying = false;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		com.aurora.utils.SystemUtils.setStatusBarBackgroundTransparent(this);

		final Intent intent = getIntent();
		mRecordFilePath = intent.getStringExtra("url");
		String shortFileName = intent.getStringExtra(EXTRA_SHORT_FILE_NAME);
		
		File targetFile = new File(mRecordFilePath);
		final byte[] buffer = new byte[RecordConstants.WAV_FILE_HEADER_CHUNK_SIZE];
        int result = readFromRecordFile(targetFile, 0, buffer);
        if (result == -1) {
            finish();
            ToastUtil.shortToast(R.string.play_error_parse_file_failed);
            return;
        }        
        mIsPCMWavFormat = RecordConstants.WAV_FILE_HEADER_RIFF.equalsIgnoreCase(new String(buffer, 0, 4)) &&
                RecordConstants.WAV_FILE_HEADER_WAVE.equalsIgnoreCase(new String(buffer, 8, 4));
        if (mIsPCMWavFormat) {
            initDB();
        }
        
        setContentView(R.layout.play_activity_2);
		initViews();
		if (TextUtils.isEmpty(shortFileName)) {
		    shortFileName = formatRecordFileName(targetFile);
		}
        mFileNameTv.setText(shortFileName);
        
		setListeners();
		if (mIsPCMWavFormat) {
		    initAll();
		}

        startNow(false);
		
		sIsPlaying = true;
	}

	@Override
    protected void onResume() {
        super.onResume();

        com.aurora.utils.SystemUtils.switchStatusBarColorMode(
                com.aurora.utils.SystemUtils.STATUS_BAR_MODE_BLACK, this);
    }

	private static String formatRecordFileName(File file) {
	    String fileName = file.getName();
	    if (!TextUtils.isEmpty(fileName)) {
	        fileName = fileName.substring(2); // 去掉YY
	        fileName = fileName.substring(0, fileName.length() - 4); // 去掉扩展名
	        
	        return fileName;
	    }
	    
	    return "";
	}
	
	private void initDB() {
        mRecorderDb = new RecorderAdapter(this);
        mRecorderDb.open();
    }
	
	private void initViews() {
	    mProgress = (PlaySeekBar) findViewById(R.id.play_seek_bar);
	    mPlayTimeCv = (ClockView) findViewById(R.id.play_time_cv);
	    
	    mTotalTimeTv = (TextView) findViewById(R.id.total_time_tv);
	    mFileNameTv = (TextView) findViewById(R.id.file_name_tv);
	    mMarkIv = (ImageView) findViewById(R.id.mark_iv);
        mStartIv = (ImageView) findViewById(R.id.start_iv);
        mPauseIv = (ImageView) findViewById(R.id.pause_iv);
        mAnimationIv = (ImageView) findViewById(R.id.animation_iv);
        mFrameAnimationPause2Play = new FrameAnimationUtil(PAUSE_2_PLAY_ANIM_RES_IDS, true, false, mAnimationIv);
        mFrameAnimationPause2Play.setAnimationListener(new FrameAnimationListener() {
            @Override
            public void onAnimationEnd() {
                mAnimationIv.setVisibility(View.GONE);
                mStartIv.setVisibility(View.VISIBLE);
                mFrameAnimationPause2Play.reset();
            }
        });
        mFrameAnimationPlay2Pause = new FrameAnimationUtil(PLAY_2_PAUSE_ANIM_RES_IDS, true, false, mAnimationIv);
        mFrameAnimationPlay2Pause.setAnimationListener(new FrameAnimationListener() {
            @Override
            public void onAnimationEnd() {
                mAnimationIv.setVisibility(View.GONE);
                mPauseIv.setVisibility(View.VISIBLE);
                mFrameAnimationPlay2Pause.reset();
            }
        });
        
        mStopIv = (ImageView) findViewById(R.id.stop_iv);
        
	    if (isPauseState()) {
	        mProgress.setEnabled(false);
	    }
	    
	    mWaveformView = (WaveformViewForPlay) findViewById(R.id.play_voice);
	    mWaveformView.setWaveDrawConfig(1);
	    
	    DisplayMetrics dm = getResources().getDisplayMetrics();
	    if (dm.widthPixels / dm.density != 360) {
	        LayoutParams params = mWaveformView.getLayoutParams();
	        params.width = dm.widthPixels;
	    }

	    if (mIsPCMWavFormat) {
	        mPlayWaveformHelper = new PlayWaveformHelper();
	        mPlayWaveformHelper.setWholeWaveData(getWholeWaveData());
	        initWholeMarkInfo();
	    } else {
	        mMarkIv.setEnabled(false);
	        mWaveformView.setWaveDrawable(R.drawable.waveform_default);
	    }
	}
	
	private void initWholeMarkInfo() {
	    mRecorderInfo = mRecorderDb.queryDataByPath(mRecordFilePath);
	    if (mRecorderInfo != null) {
	        mPlayWaveformHelper.setWholeMarkInfo(mRecorderInfo.getMarks());
	        mProgress.addAllMarkInfo(mRecorderInfo.getMarks());
	    }
	}
	
	private short[] getWholeWaveData() {
	    String waveFilePath = mRecordFilePath + RecordConstants.WAVE_EXTENSION_NAME;
        byte[] wholeWaveData = null;
        try {
            wholeWaveData = FileUtils.readFileToByteArray(new File(waveFilePath));
            if (wholeWaveData != null) {
                short[] dst = new short[wholeWaveData.length / 2];
                ByteBuffer.wrap(wholeWaveData).order(ByteOrder.nativeOrder()).asShortBuffer().get(dst);
                return dst;
            }
        } catch (IOException e) {
            Log.e(TAG, "Jim, get whole wave data error.", e);
            e.printStackTrace();
        }
        
        return null;
	}
	
	private void setListeners() {
	    mProgress.setOnSeekBarChangeListener(new VoiceSeekBar());
	    
	    mMarkIv.setOnClickListener(this);
        mStartIv.setOnClickListener(this);
        mPauseIv.setOnClickListener(this);
        mStopIv.setOnClickListener(this);
        
        if (mTelephonyManager == null) {
            mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        }
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
	}
	
	private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING: // 来电
                    Log.d(TAG, "Jim, CALL_STATE_RINGING");
                    pauseNow(false);
                    break;
                case TelephonyManager.CALL_STATE_IDLE: // 挂断电话
                    Log.d(TAG, "Jim, CALL_STATE_IDLE");
//                    startNow();
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
        
    };
    
    private void mark() {
        if (mProgress.getMarkNum() > RecordConstants.MAX_MARK_NUM) {
            Log.e(TAG, "Jim, reached max mark num.");
            return;
        }
        mProgress.addMarkInfo(new MarkInfo(mProgress.getProgress()));
        mPlayWaveformHelper.setWholeMarkInfo(mProgress.getAllMarkInfo());
        
        if (!mIsMarkInfoChanged.get()) {
            mIsMarkInfoChanged.set(true);
        }
        Log.d(TAG, "Jim, mark, duration: " + mProgress.getProgress());
    }
	
	@Override
    public void onClick(View v) {
	    switch (v.getId()) {
	        case R.id.mark_iv:
	            mark();
                break;
            case R.id.start_iv:
                startNow(true);
                break;
            case R.id.pause_iv:
                pauseNow(true);
                break;
            case R.id.stop_iv:
                complete();
                break;
	    }
    }
	
	private void complete() {
	    mMarkIv.setEnabled(false);
	    stop();
        finish();
	}
	
	private void startNow(boolean needAnimation) {
	    mMarkIv.setEnabled(true);
	    if (needAnimation) {
	        mAnimationIv.setVisibility(View.VISIBLE);
	        mStartIv.setVisibility(View.GONE);
	        mPauseIv.setVisibility(View.GONE);
	        mFrameAnimationPlay2Pause.startAnimation();
	    } else {
            mStartIv.setVisibility(View.GONE);
            mAnimationIv.setVisibility(View.GONE);
            mPauseIv.setVisibility(View.VISIBLE);
	    }
	    
	    changePauseState(false);
	    if (mIsPCMWavFormat) {
	        play2();
	    } else {
	        play();
	    }
	}
	
	private void pauseNow(boolean needAnimation) {
	    mMarkIv.setEnabled(false);
	    if (needAnimation) {
	        mAnimationIv.setVisibility(View.VISIBLE);
	        mStartIv.setVisibility(View.GONE);
	        mPauseIv.setVisibility(View.GONE);
	        mFrameAnimationPause2Play.startAnimation();
	    } else {
	        mPauseIv.setVisibility(View.GONE);
	        mAnimationIv.setVisibility(View.GONE);
            mStartIv.setVisibility(View.VISIBLE);
	    }
	    
	    changePauseState(true);
        pause();
	}
	
	private boolean isPauseState() {
	    return mIsPlayPause;
	}
	
	private void changePauseState(boolean isPause) {
	    mIsPlayPause = isPause;
	    synchronized (mLock) {
	        mLock.notifyAll();
	    }
	}
	
	private void changeIsChangeState(boolean isChanging) {
	    mIsChanging = isChanging;
	    synchronized (mLock) {
	        mLock.notifyAll();
	    }
	}

	// 进度条处理
	class VoiceSeekBar implements OnSeekBarChangeListener {
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
		    if (!mIsPCMWavFormat) {
		        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
//	                mPlayTimeTv.setText(TimeUtils.getDateStr(progress));
		            mPlayTimeCv.timeChanged(progress);
	            }
		    } else {
		        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
//                    mPlayTimeTv.setText(TimeUtils.getDateStr(progress));
                    mPlayTimeCv.timeChanged(progress);
                }
		    }
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
			changeIsChangeState(true);
		}

		public void onStopTrackingTouch(SeekBar seekBar) {
		    if (mMediaPlayer != null) {
		        mMediaPlayer.seekTo(seekBar.getProgress());
		    } else {
		        mExpectedAudioTrackProgress.set(seekBar.getProgress());
		    }
		    changeIsChangeState(false);
		}

	}

	@Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            // 开始更新界面
            case MSG_WHAT_UPDATA_PROGRESS_FOR_MEDIA_PLAYER:
                if (mMediaPlayer != null/* && mediaPlayer.isPlaying() */) {
                    int currentPosition = mMediaPlayer.getCurrentPosition();
                    mProgress.setProgress(currentPosition);
//                    mPlayTimeTv.setText(TimeUtils.getDateStr(currentPosition));
                    mPlayTimeCv.timeChanged(currentPosition);
                }
                break;
            case MSG_WHAT_REFRESH_WAVEFORM: {
                WaveParams params = (WaveParams) msg.obj;
//                if (mAudioTrackPlayer != null && mKeepRunning) {
                    mProgress.setProgress(params.duration);
//                    mPlayTimeTv.setText(TimeUtils.getDateStr(params.duration));
                    mPlayTimeCv.timeChanged(params.duration);
//                }
                mWaveformView.waveChanged(params.waveData, params.displayMarkData,
                        params.isTimelineUpdated ? params.timelineData: null, params.timeLineTranslate);
                break;
            }
            case MSG_WHAT_PLAY_ERROR_EXCEPTION:
                ToastUtil.shortToast(R.string.play_error);
                break;
            case MSG_WHAT_PCM_PLAYER_STOPPED: {
                if (mAnimationIv.getVisibility() != View.GONE) {
                    mAnimationIv.setVisibility(View.GONE);
                    mFrameAnimationPause2Play.stopAnimation();
                    mFrameAnimationPlay2Pause.stopAnimation();
                }
                mPauseIv.setVisibility(View.GONE);
                mStartIv.setVisibility(View.VISIBLE);
                mProgress.setProgress(0);
                mWaveformView.reset();
                WaveParams params = (WaveParams) msg.obj;
//                Log.d(TAG, "params.timeLineTranslate: " + params.timeLineTranslate);
                mWaveformView.waveChanged(params.waveData, params.displayMarkData,
                        params.isTimelineUpdated ? params.timelineData: null, params.timeLineTranslate);
                break;
            }
            default:
                break;
        }
        super.handleMessage(msg);
    }
	
	private void startTimer() {
	    mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (!mIsChanging) {
                    mHandler.sendEmptyMessage(MSG_WHAT_UPDATA_PROGRESS_FOR_MEDIA_PLAYER);
                }
            }
        };
        mTimer.schedule(mTimerTask, 0, TIMER_PERIOD);
	}
	
	protected void play2() {
	    if (mIsPlayPause) {
	        changePauseState(false);
	    } else {
	        if (!mKeepRunning) {
	            if (mSampleRate >= 0 && mPlayTotalTime >= 0) {
	                mWaveformView.reset();
	                mAudioTrackPlayer = new PCMAudioTrack();
	                mAudioTrackPlayer.init();
	                mAudioTrackPlayer.start();
	            } else {
	                ToastUtil.shortToast(R.string.play_error_invalid_record_file);
	            }
	        }
	    }
	}

	/**
	 * 播放音乐
	 */
	protected void play() {
	    if (mMediaPlayer == null) {
	        File file = new File(mRecordFilePath);
	        if (file.exists() && file.length() > 0) {

	            try {
	                mMediaPlayer = new MediaPlayer();
	                // 设置指定的流媒体地址
	                mMediaPlayer.setDataSource(mRecordFilePath);
	                // 设置音频流的类型
	                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

	                // 通过异步的方式装载媒体资源
	                mMediaPlayer.prepareAsync();
	                mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
	                    @Override
	                    public void onPrepared(MediaPlayer mp) {
	                        Log.d(TAG, "Jim, onPrepared, thread: " + Thread.currentThread().getName() + ": " + Thread.currentThread().getId());
	                        // 装载完毕 开始播放流媒体
	                        mProgress.setMax(mMediaPlayer.getDuration());
	                        mProgress.setEnabled(true);
	                        mTotalTimeTv.setText(TimeUtils.getDateStr(mMediaPlayer.getDuration()));
	                        mMediaPlayer.start();
//	                        mWaveformView.link(mMediaPlayer);
	                        // ----------定时器记录播放进度---------//
	                        startTimer();
	                    }
	                });
	                // 设置循环播放
	                // mediaPlayer.setLooping(true);
	                mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

	                    @Override
	                    public void onCompletion(MediaPlayer mp) {
	                        // 在播放完毕被回调
	                        handleComplete();
	                    }
	                });
	                mMediaPlayer
	                        .setOnSeekCompleteListener(new OnSeekCompleteListener() {
	                            @Override
	                            public void onSeekComplete(MediaPlayer mp) {
	                            }
	                        });
	                mMediaPlayer.setOnErrorListener(new OnErrorListener() {

	                    @Override
	                    public boolean onError(MediaPlayer mp, int what, int extra) {
	                        // 如果发生错误，重新播放
	                        Log.d(TAG, "Jim, play error, mp: " + mp + ", what: " + what + ", extra: " + extra);
	                        startNow(false);
	                        return false;
	                    }
	                });
	            } catch (Exception e) {
	                FileLog.e(TAG, e.getMessage());
	                ToastUtil.shortToast(R.string.play_error);
	            }

	        } else {
	            ToastUtil.shortToast(R.string.play_error_file_not_exist);
	        }
	    } else {
	        if (!mMediaPlayer.isPlaying()) {
	            Log.d(TAG, "Jim, cur progress: " + mProgress.getProgress() + ", max progress: " + mProgress.getMax());
	            if (mProgress.getProgress() == mProgress.getMax()) {
	                mProgress.setProgress(0);
	            }
	            mMediaPlayer.seekTo(mProgress.getProgress());
	            mMediaPlayer.start();
//	            mWaveformView.resume();
	            startTimer();
	        }
	    }
	}
	
	private void handleComplete() {
//	    mProgress.setProgress(0);
//	    mWaveformView.stop();
	    mProgress.setProgress(mProgress.getMax());
	    Log.d(TAG, "Jim, play complete, current position: " + mMediaPlayer.getCurrentPosition());
        mTimerTask.cancel(); 
        mPauseIv.setVisibility(View.GONE);
        mStartIv.setVisibility(View.VISIBLE);
//        mPlayTimeTv.setText(TimeUtils.getDateStr(0));
	}

	/**
	 * 停止播放
	 */
	protected void stop() {
	    mStartIv.setVisibility(View.VISIBLE);
        mAnimationIv.setVisibility(View.GONE);
        mPauseIv.setVisibility(View.GONE);
	    if (!mIsPCMWavFormat) {
	        changePauseState(false);
	        
	        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
//	            mWaveformView.release();
	            mMediaPlayer.stop();
	            mMediaPlayer.release();
	            mMediaPlayer = null;
	        }
	        
	        if (mTimer != null) {
	            mTimer.cancel();
	        }
	    } else {
	        stopPlayAudio();
	    }
	}
	
	/*private boolean isPlaying() {
	    if (!mIsPCMWavFormat) {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                return true;
            }
            
            return false;
        } else {
            if (mAudioTrackPlayer != null) {
                return true;
            }
            return false;
        }
	}*/
	
	private void pause() {
	    if (!mIsPCMWavFormat) {
	        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
//	            mWaveformView.pause();
	            mMediaPlayer.pause();
	        }
	        if (mTimer != null) {
	            mTimer.cancel();
	        }
	    }
	}

	@Override
	protected void onDestroy() {
		// 在activity结束的时候回收资源
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		
		if (mTelephonyManager != null) {
		    mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
		}
		
		super.onDestroy();
	}

    @Override
    public void onBackPressed() {
        complete();
    }

    @Override
    public void finish() {
        Log.d(TAG, "Jim, finish enter");
//        if (isPlaying()) {
//            Log.d(TAG, "Jim, will stop playing.");
//            stop();
//        }
        setResult(RESULT_OK);
        super.finish();
        sIsPlaying = false;
    }
    
    private int getWavRecordSampleRate(File f) {
        final byte[] sampleRate = new byte[4];
        int result = readFromRecordFile(f, 24, sampleRate);
        if (result == -1) {
            return result;
        }
        
        return RecordUtil.byte2Int(sampleRate);
    }
    
    private static int readFromRecordFile(File file, int offset, byte[] buffer) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            in.skip(offset);
            return in.read(buffer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return -1;
    }
    
    private int getAudioTime(File f) {
        final byte[] filesize = new byte[4];
        int result = readFromRecordFile(f, 40, filesize);
        if (result == -1) {
            return result;
        }
        
        int totalBytes = RecordUtil.byte2Int(filesize);
        Log.d(TAG, "Jim, getAudioTime, totalBytes: " + totalBytes);
        return convertByteNum2Millisecond(totalBytes, mSampleRate, RecordConstants.AUDIO_TRACK_CHANNEL_CONFIG,
                RecordConstants.AUDIO_TRACK_AUDIO_FORMAT);
    }
    
    private void stopPlayAudio() {
        if (mAudioTrackPlayer != null) {
            mAudioTrackPlayer.free();
            mAudioTrackPlayer = null;
        }
        
        if (mIsPlayPause) {
            changePauseState(false);
        } else {
            mProgress.setProgress(0);
        }
    }
    
    private void initAll() {
        changePauseState(false);
        mProgress.setProgress(0);
        getWavRecordFileInfo();
    }
    
    private void getWavRecordFileInfo() {
        File targetFile = new File(mRecordFilePath);
        mSampleRate = getWavRecordSampleRate(targetFile);
        if (mSampleRate >= 0) {
            mPlayTotalTime = getAudioTime(targetFile);
            if (mPlayTotalTime < 0) {
                Log.e(TAG, "Jim, audio time error.");
            }
            Log.d(TAG, "Jim, PlayTotalTime: " + mPlayTotalTime);
            String totalStrtime = TimeUtils.getDateStr(mPlayTotalTime);
            mTotalTimeTv.setText(totalStrtime);
            mProgress.setMax(mPlayTotalTime);
            if (mRecorderInfo == null) {
                mRecorderInfo = new RecorderInfo();
                mRecorderInfo.setDuration(mPlayTotalTime);
                mRecorderInfo.setPath(mRecordFilePath);
                mRecorderInfo.setName(mFileNameTv.getText().toString());
            }
        } else {
            Log.e(TAG, "Jim, audio baudrate error");
        }
    }
    
    static int convertByteNum2Millisecond(int byteNum, int sampleRate, int channelConfig, int audioFormat) {
        final int channelCount = RecordUtil.getChannelCount(RecordConstants.AUDIO_TRACK_CHANNEL_CONFIG);
        final int sampleByte = RecordUtil.getAudioFormatBitCount(RecordConstants.AUDIO_TRACK_AUDIO_FORMAT) / 8;
//        return (byteNum / (sampleRate * channelCount * sampleByte)) * 1000;
        return (int)RecordUtil.getDuration(byteNum, sampleRate, channelCount, sampleByte);
    }
    
    private void saveRecordInfo2Database() {
        if (mIsPCMWavFormat) {
            RecorderInfo info = mRecorderInfo;
            if (info != null) {
                if (mIsMarkInfoChanged.get()) {
                    info.setMarks(mProgress.getAllMarkInfo());
                    if (mRecorderDb == null) {
                        // 有可能第一次播完之后就把链接给断开了
                        initDB();
                    }
                    mRecorderDb.insertOrUpdate(info);
                    mIsMarkInfoChanged.set(false);
                }
            }
            if (mRecorderDb != null) {
                mRecorderDb.close();
                mRecorderDb = null;
            }
        }
    }
    
    private class PCMAudioTrack extends Thread {
        private File mFile;
        private RandomAccessFile mRandomAccessFile;
        private int mOutBufferSize;
        private AudioTrack mAudioTrack;
        private int mTotalPayload;
        
        public void free() {
            mKeepRunning = false;
        }
        
        public boolean init() {
            mFile = new File(mRecordFilePath);
            mTotalPayload = (int) mFile.length() - RecordConstants.WAV_FILE_HEADER_CHUNK_SIZE;
            boolean hasError = false;
            try {
                mRandomAccessFile = new RandomAccessFile(mFile, "r");
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Jim, init file inputstream error.", e);
                hasError = true;
            }
            if (hasError) {
                ToastUtil.shortToast(R.string.play_error_file_not_exist);
                return false;
            }
            
            mKeepRunning = true;
            mOutBufferSize = AudioTrack.getMinBufferSize(mSampleRate,
                    RecordConstants.AUDIO_TRACK_CHANNEL_CONFIG, RecordConstants.AUDIO_TRACK_AUDIO_FORMAT);
            Log.d(TAG, "Jim, total payload: " + mTotalPayload + ", mOutBufferSize: " + mOutBufferSize);
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate, RecordConstants.AUDIO_TRACK_CHANNEL_CONFIG,
                    RecordConstants.AUDIO_TRACK_AUDIO_FORMAT, mOutBufferSize, AudioTrack.MODE_STREAM);
            
            return true;
        }
        
        private int getBestMinBufferSize(int minBufferSize) {
            final int sampleRate = mSampleRate;
            // 每次读取20ms的音频数据，为了保证波形图刷新的帧率
            int bufSize = ((sampleRate *
                    RecordUtil.getChannelCount(RecordConstants.AUDIO_TRACK_CHANNEL_CONFIG) *
                    (RecordUtil.getAudioFormatBitCount(RecordConstants.AUDIO_TRACK_AUDIO_FORMAT) / 8)) /
                    1000) * 20;
            return bufSize < minBufferSize ? bufSize: minBufferSize;
//            return minBufferSize;
        }
        
        @Override
        public void run() {
            int totalReadBytes = 0;
//            final byte[] buffer = new byte[mOutBufferSize];
            final int bufferSize = getBestMinBufferSize(mOutBufferSize);
            final byte[] buffer = new byte[bufferSize];
            
            try {
                mAudioTrack.play();
                mRandomAccessFile.seek(RecordConstants.WAV_FILE_HEADER_CHUNK_SIZE);
                while (mKeepRunning) {
                    synchronized (mLock) {
                        while (mIsPlayPause || mIsChanging) {
                            try {
                                mLock.wait();
                            } catch (InterruptedException e) {
                            }
                        }
                    }
//                    if (!mIsPlayPause && !mIsChanging) {
                    final int expectedProgress = mExpectedAudioTrackProgress.get();
                    if (expectedProgress != -1) {
                        mExpectedAudioTrackProgress.set(-1);
                        int skipBytes = (int)(((expectedProgress * 1.0f) / mPlayTotalTime) * mTotalPayload);
                        if (skipBytes % mOutBufferSize != 0) {
                            skipBytes = ((skipBytes / mOutBufferSize) + 1) * mOutBufferSize;
                        }
                        totalReadBytes = skipBytes;
                        skipBytes += RecordConstants.WAV_FILE_HEADER_CHUNK_SIZE;
                        mRandomAccessFile.seek(skipBytes);
                    }
                    
                    int readbytes = mRandomAccessFile.read(buffer);
                    if (readbytes > 0) {
                        mAudioTrack.write(buffer, 0, readbytes);
                        totalReadBytes += readbytes;
                        int currentTime = convertByteNum2Millisecond(totalReadBytes, mSampleRate,
                                RecordConstants.AUDIO_TRACK_CHANNEL_CONFIG, RecordConstants.AUDIO_TRACK_AUDIO_FORMAT);
//                        Log.d(TAG, "Jim, total_len: " + totalReadBytes + ", readbytes: " + readbytes +
//                                ", mOutBufferSize: " + mOutBufferSize + ", currentTime: " + currentTime);
                                                
                        WaveParams params = mPlayWaveformHelper.calculateTimeLineOffsetAndTimeData(currentTime);
                        if (params.waveData == null) {
                            short[] tmpBuf = new short[readbytes / 2];
                            ByteBuffer.wrap(buffer).order(ByteOrder.nativeOrder()).asShortBuffer().get(tmpBuf);
                            params.waveData = mPlayWaveformHelper.prepareWaveformData(tmpBuf);
                        }
                        params.duration = currentTime;
                        refreshWaveform(params);
                        
                        if (totalReadBytes >= mTotalPayload) {
                            mKeepRunning = false;
                        }
                    } else {
                        mKeepRunning = false;
                    }
//                    }
                }
                saveRecordInfo2Database();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Jim, audio track error.", e);
                reportError(e);
            } catch (IOException e) {
                Log.e(TAG, "Jim, audio track error.", e);
                reportError(e);
            } finally {
                mAudioTrack.stop();
                mAudioTrack.release();
                mAudioTrack = null;
                try {
                    mRandomAccessFile.close();
                } catch (IOException e) {
                }
            }
            
            WaveParams params = mPlayWaveformHelper.calculateTimeLineOffsetAndTimeData(0);
            if (params.waveData == null) {
                params.waveData = mPlayWaveformHelper.prepareWaveformData(null);
            }
            mHandler.obtainMessage(MSG_WHAT_PCM_PLAYER_STOPPED, params).sendToTarget();
        }
        
        private void reportError(Throwable t) {
            mHandler.obtainMessage(MSG_WHAT_PLAY_ERROR_EXCEPTION, t).sendToTarget();
        }
        
        private void refreshWaveform(WaveParams params) {
            mHandler.removeMessages(MSG_WHAT_REFRESH_WAVEFORM);
            mHandler.obtainMessage(MSG_WHAT_REFRESH_WAVEFORM, params).sendToTarget();
        }
    }
}
