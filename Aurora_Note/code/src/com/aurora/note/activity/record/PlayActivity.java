package com.aurora.note.activity.record;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Bundle;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.aurora.note.R;
import com.aurora.note.activity.BaseActivity;
import com.aurora.note.util.FileLog;
import com.aurora.note.util.TimeUtils;
import com.aurora.note.util.ToastUtil;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class PlayActivity extends BaseActivity implements OnClickListener {
	private static final String TAG = "PlayActivity";
	private static final int UPDATA_PROGRESS = 1;
	
	private static final long TIMER_PERIOD = 100;
	
	private ImageView mStartOrPauseIv;
	private SeekBar mProgress;
	private TextView mPlayTimeTv;
	private TextView mTotalTimeTv;
	private View mComplete;
	
	// 0 录音 1 停止
	private int status = 0;
	private MediaPlayer mediaPlayer;
	private boolean isChanging = false;// 互斥变量，防止定时器与SeekBar拖动时进度冲突
	private Timer mTimer;
	private TimerTask mTimerTask;
	private String path = "";
	
	private TelephonyManager mTelephonyManager;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.play);
		initViews();
		path =  getIntent().getStringExtra("url");
		setListeners();
		startNow();
	}
	
	private void initViews() {
	    mStartOrPauseIv = (ImageView) findViewById(R.id.pause_iv);
	    mProgress = (SeekBar) findViewById(R.id.playing_progress);
	    mPlayTimeTv = (TextView) findViewById(R.id.play_time_tv);
	    mTotalTimeTv = (TextView) findViewById(R.id.total_time_tv);
	    mComplete = findViewById(R.id.complete_tv);
	    if (isPauseState()) {
	        mProgress.setEnabled(false);
	    }
	}
	
	private void setListeners() {
	    mProgress.setOnSeekBarChangeListener(new VoiceSeekBar());

        mStartOrPauseIv.setOnClickListener(this);
        mComplete.setOnClickListener(this);
        
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
                    pauseNow();
                    break;
                case TelephonyManager.CALL_STATE_IDLE: // 挂断电话
                    Log.d(TAG, "Jim, CALL_STATE_IDLE");
//                    startNow();
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
        
    }; 
	
	@Override
    public void onClick(View v) {
	    switch (v.getId()) {
	        case R.id.pause_iv:
	            if (status == 0) {
	                startNow();
                } else {
                    pauseNow();
                }
	            break;
	        case R.id.complete_tv:
	            complete();
	            break;
	    }
    }
	
	private void complete() {
	    stop();
        finish();
	}
	
	private void startNow() {
	    changeToPlayState();
        play();
	}
	
	private void pauseNow() {
	    changeToPauseState();
        pause();
	}
	
	private boolean isPauseState() {
	    return status == 0;
	}
	
	private void changeToPlayState() {
	    mStartOrPauseIv.setImageResource(R.drawable.play_pause_selector);
	    status = 1;
//	    mProgress.setEnabled(true);
	}
	
	private void changeToPauseState() {
	    mStartOrPauseIv.setImageResource(R.drawable.play_start_selector);
        status = 0;
//        mProgress.setEnabled(false);
	}

	// 进度条处理
	class VoiceSeekBar implements OnSeekBarChangeListener {
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
		    if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
		        mPlayTimeTv.setText(TimeUtils.getDateStr(progress));
		    }
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
			isChanging = true;
		}

		public void onStopTrackingTouch(SeekBar seekBar) {
		    if (mediaPlayer != null) {
		        mediaPlayer.seekTo(seekBar.getProgress());
		    }
			isChanging = false;
		}

	}

	@Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            // 开始更新界面
            case UPDATA_PROGRESS:
                // 当用户调整界面并保存后，要重新获取一次版块数据库
                if (mediaPlayer != null/* && mediaPlayer.isPlaying() */) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    mProgress.setProgress(currentPosition);
                    mPlayTimeTv.setText(TimeUtils.getDateStr(currentPosition));
                }
                break;
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
                if (!isChanging) {
                    mHandler.sendEmptyMessage(UPDATA_PROGRESS);
                }
            }
        };
        mTimer.schedule(mTimerTask, 0, TIMER_PERIOD);
	}

	/**
	 * 播放音乐
	 */
	protected void play() {
	    if (mediaPlayer == null) {
	        File file = new File(path);
	        if (file.exists() && file.length() > 0) {

	            try {
	                mediaPlayer = new MediaPlayer();
	                // 设置指定的流媒体地址
	                mediaPlayer.setDataSource(path);
	                // 设置音频流的类型
	                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

	                // 通过异步的方式装载媒体资源
	                mediaPlayer.prepareAsync();
	                mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
	                    @Override
	                    public void onPrepared(MediaPlayer mp) {
	                        Log.d(TAG, "Jim, onPrepared, thread: " + Thread.currentThread().getName() + ": " + Thread.currentThread().getId());
	                        // 装载完毕 开始播放流媒体
	                        mProgress.setMax(mediaPlayer.getDuration());
	                        mProgress.setEnabled(true);
	                        mTotalTimeTv.setText(TimeUtils.getDateStr(mediaPlayer.getDuration()));
	                        mediaPlayer.start();
	                        // ----------定时器记录播放进度---------//
	                        startTimer();
	                    }
	                });
	                // 设置循环播放
	                // mediaPlayer.setLooping(true);
	                mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

	                    @Override
	                    public void onCompletion(MediaPlayer mp) {
	                        // 在播放完毕被回调
	                        handleComplete();
	                    }
	                });
	                mediaPlayer
	                        .setOnSeekCompleteListener(new OnSeekCompleteListener() {
	                            @Override
	                            public void onSeekComplete(MediaPlayer mp) {
	                            }
	                        });
	                mediaPlayer.setOnErrorListener(new OnErrorListener() {

	                    @Override
	                    public boolean onError(MediaPlayer mp, int what, int extra) {
	                        // 如果发生错误，重新播放
	                        Log.d(TAG, "Jim, play error, mp: " + mp + ", what: " + what + ", extra: " + extra);
	                        startNow();
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
	        if (!mediaPlayer.isPlaying()) {
	            Log.d(TAG, "Jim, cur progress: " + mProgress.getProgress() + ", max progress: " + mProgress.getMax());
	            if (mProgress.getProgress() == mProgress.getMax()) {
	                mProgress.setProgress(0);
	            }
	            mediaPlayer.seekTo(mProgress.getProgress());
	            mediaPlayer.start();
	            startTimer();
	        }
	    }
	}
	
	private void handleComplete() {
//	    mProgress.setProgress(0);
	    mProgress.setProgress(mProgress.getMax());
	    Log.d(TAG, "Jim, play complete, current position: " + mediaPlayer.getCurrentPosition());
        mTimerTask.cancel();                
        changeToPauseState();
//        mPlayTimeTv.setText(TimeUtils.getDateStr(0));
	}

	/**
	 * 停止播放
	 */
	protected void stop() {
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
		}
		
		if (mTimer != null) {
		    mTimer.cancel();
		}
	}
	
	private void pause() {
	    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
	        mediaPlayer.pause();
        }
        
        if (mTimer != null) {
            mTimer.cancel();
        }
	}

	@Override
	protected void onDestroy() {
		// 在activity结束的时候回收资源
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
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
        setResult(RESULT_OK);
        super.finish();
    }
}