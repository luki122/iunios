package com.aurora.note.activity.record;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.aurora.note.R;
import com.aurora.note.util.Globals;
import com.aurora.note.util.SDcardManager;
import com.aurora.note.util.TimeUtils;
import com.aurora.note.util.ToastUtil;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class RecordActivity extends Activity implements OnClickListener {
    private static final String TAG = RecordActivity.class.getSimpleName();
    
    private static final int MSG_UPDATE_DURATION = 1;
    
	private Timer mTimer;
	private TimerTask mTimerTask;
	
	private TextView mDurationTv;
	private View mAnmation;
	private View mComplete;
	private long mDuration = 0;
	private RefreshUIHandler mRefreshUIHandler = new RefreshUIHandler(this);
	
	private MediaRecorder mr;

	private File file;
    
    private TelephonyManager mTelephonyManager;
    
    private static final int[] ANIMATION_RES_IDS = {
        R.drawable.record_animation_00000, R.drawable.record_animation_00001,
        R.drawable.record_animation_00002, R.drawable.record_animation_00003,
        R.drawable.record_animation_00004, R.drawable.record_animation_00005,
        R.drawable.record_animation_00006, R.drawable.record_animation_00007,
        R.drawable.record_animation_00008, R.drawable.record_animation_00009,
        R.drawable.record_animation_00010, R.drawable.record_animation_00011,
        R.drawable.record_animation_00012, R.drawable.record_animation_00013,
        R.drawable.record_animation_00014, R.drawable.record_animation_00015,
        R.drawable.record_animation_00016, R.drawable.record_animation_00017,
        R.drawable.record_animation_00018, R.drawable.record_animation_00019,
        R.drawable.record_animation_00020, R.drawable.record_animation_00021,
        R.drawable.record_animation_00022, R.drawable.record_animation_00023,
        R.drawable.record_animation_00024, R.drawable.record_animation_00025,
        R.drawable.record_animation_00026, R.drawable.record_animation_00027,
        R.drawable.record_animation_00028, R.drawable.record_animation_00029,
        R.drawable.record_animation_00030, R.drawable.record_animation_00031,
        R.drawable.record_animation_00032, R.drawable.record_animation_00033,
        R.drawable.record_animation_00034, R.drawable.record_animation_00035,
        R.drawable.record_animation_00036, R.drawable.record_animation_00037,
        R.drawable.record_animation_00038, R.drawable.record_animation_00039,
        R.drawable.record_animation_00040, R.drawable.record_animation_00041,
        R.drawable.record_animation_00042, R.drawable.record_animation_00043,
        R.drawable.record_animation_00044, R.drawable.record_animation_00045,
        R.drawable.record_animation_00046, R.drawable.record_animation_00047,
        R.drawable.record_animation_00048, R.drawable.record_animation_00049
    };
    private FrameAnimationUtil mFrameAnimationUtil;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record);
		initViews();
		setListeners();
		startRecord();
	}
	
	private void initViews() {
	    mDurationTv = (TextView) findViewById(R.id.duration_tv);
	    mAnmation = findViewById(R.id.animation);
	    mComplete = findViewById(R.id.complete_tv);
	    
	    mFrameAnimationUtil = new FrameAnimationUtil(ANIMATION_RES_IDS, false, false, mAnmation);
	}
	
	private void setListeners() {
	    mComplete.setOnClickListener(this);
	    if (mTelephonyManager == null) {
            mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        }
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
                        stopRecord();
                        mRing = false;
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
    protected void onDestroy() {
        if (mTelephonyManager != null) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        
        super.onDestroy();
    }
	
	private void startRecord() {
			if (!checkSDCard()) {
					return;
				}
				if (!Globals.SOUND_DIR.exists()) {
					Globals.SOUND_DIR.mkdirs();
				}
	    file = new File(Globals.SOUND_DIR,
                "YY" + DateFormat.format("yyyyMMdd_HHmmss",
                        Calendar.getInstance(Locale.CHINA)) + ".amr");

        // 创建录音对象
        boolean hasError = false;
        try {
            mr = new MediaRecorder();
            // 创建文件
            file.createNewFile();
            
            mr.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            mr.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            mr.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mr.setOutputFile(file.getAbsolutePath());
            mr.prepare();
            mr.start();
            
            mTimer = new Timer();
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    if (null != mr) {
                        mDuration++;
                        mRefreshUIHandler.sendEmptyMessage(MSG_UPDATE_DURATION);
                    }
                }
            };
            mTimer.schedule(mTimerTask, 1000, 1000);
        } catch (IllegalStateException e) {
            Log.e(TAG, "startRecord error", e);
            hasError = true;
        } catch (IOException e) {
            Log.e(TAG, "startRecord error", e);
            hasError = true;
        } catch (Exception e) {
            Log.e(TAG, "startRecord error", e);
            hasError = true;
        }
        
        if (hasError) {
            ToastUtil.shortToast(R.string.prepare_record_error);
            if (file.exists()) {
                file.delete();
            }
            finish();
        }
	}
	
	private void stopRecord() {
	    if (mr != null) {
            mr.stop();
            mr.release();
            mr = null;
        }
	    mFrameAnimationUtil.stopAnimation();
	    
	    if (mTimer != null) {
	        mTimer.cancel();
	    }
	    mTimer = null;
	    
        Intent intent = new Intent();
        intent.putExtra("recordFileName", file.getAbsolutePath());
        intent.putExtra("recordTime", TimeUtils.getStringByDate());
        intent.putExtra("recordDuration", TimeUtils.getDateStr(mDuration*1000));
        setResult(RESULT_OK, intent);
        finish();
	}

//	@Override
//	public void finish() {
//		if (mr != null) {
//			mr.stop();
//			mr.release();
//			mr = null;
//		}
//		super.finish();
//	}
	
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.complete_tv:
                stopRecord();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        // 拦截返回键
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mFrameAnimationUtil.startAnimation();
    }

	/**
	 * 
	 * @author jason
	 * @since 2014-4-14
	 * @return
	 */
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
    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mFrameAnimationUtil.stopAnimation();
    }
    
    /**
     * 刷新录音时长
     * 
     * @author JimXia
     * @date 2014-4-24 下午3:19:47
     */
    private static final class RefreshUIHandler extends Handler {
        private WeakReference<RecordActivity> mTarget;
        
        public RefreshUIHandler(RecordActivity target) {
            mTarget = new WeakReference<RecordActivity>(target);
        }
        
        private void updateDurationText() {
            final RecordActivity target = mTarget.get();
            if (target != null) {
                final long duration = target.mDuration;
  
                target.mDurationTv.setText(TimeUtils.getDateStr(duration*1000));
            }
        }
        
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_DURATION:
                    updateDurationText();
                    break;
            }
        }
    }
}