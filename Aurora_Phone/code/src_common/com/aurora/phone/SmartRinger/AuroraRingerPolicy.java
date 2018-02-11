package com.android.phone;

import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

public class AuroraRingerPolicy implements AuroraAccelerometerListener.Listener {
	private static final String LOG_TAG = "AuroraRingerPolicy";
	protected int mCurrentVolume;
	protected int mMaxVolume;
	protected int mMinVolume;
	protected int mLowerDuration;
	protected AudioManager audioManager;
	protected double mUpRate;
	protected final static int VOLUME_LENGTH = 1000;
	protected static final int UP_VOLUME = 0;
	protected static final int DOWN_VOLUME = 1;
	protected int mCurrentUpLevel = 0;
	protected AuroraAccelerometerListener mAccelerometerListener;
	protected Context mContext;
	
    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }

	public AuroraRingerPolicy(Context context) {
		mContext = context;
		audioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		mMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
		mCurrentVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
		// 当距离传感器未被遮挡，同时处于运动中时（如上两个条件均满足），铃声从当前音量在1.5秒之内减弱到30%。
		// 不满足其中任一条件的，铃声不减弱。
		mLowerDuration = (int) (1500 / (mMaxVolume - mMaxVolume * 0.3));
		mAccelerometerListener = new AuroraAccelerometerListener(context, this);
	}

	public void start() {
		mAccelerometerListener.enable(true);
	}

	protected Handler mVolumeHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			AudioManager audioManager = (AudioManager) mContext
					.getSystemService(Context.AUDIO_SERVICE);
			int currentVolume = audioManager
					.getStreamVolume(AudioManager.STREAM_RING);
			log(" currentVolume =" + currentVolume);
			switch (msg.what) {
			case UP_VOLUME:
				log("- UP_VOLUME enter");
				if (currentVolume < mMaxVolume) {
					++mCurrentUpLevel;
					int nextVolume = (int) (mCurrentUpLevel * mUpRate * mMaxVolume);
					log("nextVolume = " + nextVolume);
					if (nextVolume > currentVolume) {
						audioManager.adjustStreamVolume(
								AudioManager.STREAM_RING,
								AudioManager.ADJUST_RAISE, 0);
					}
					this.sendEmptyMessageDelayed(UP_VOLUME, VOLUME_LENGTH);
				}
				break;
			case DOWN_VOLUME:
				log("- DOWN_VOLUME enter");
				if (currentVolume > mMaxVolume * 0.3) {
					audioManager.adjustStreamVolume(AudioManager.STREAM_RING,
							AudioManager.ADJUST_LOWER, 0);
					this.sendEmptyMessageDelayed(DOWN_VOLUME, mLowerDuration);
				}
				break;
			default:
				break;
			}
		}
	};

	private void startLowerRingerVolume() {
		mVolumeHandler.removeMessages(UP_VOLUME);
		mVolumeHandler.sendEmptyMessageDelayed(DOWN_VOLUME, mLowerDuration);
	}

	public void clearVolumeParameters() {
		mCurrentUpLevel = 0;
		mVolumeHandler.removeMessages(UP_VOLUME);
		mVolumeHandler.removeMessages(DOWN_VOLUME);
		audioManager.setStreamVolume(AudioManager.STREAM_RING, mCurrentVolume,
				0);
		mAccelerometerListener.enable(false);
	}

	// 铃声减弱：
	// 1. 距离传感器未被遮挡；
	// 2. 加速度传感器判断手机当前是否处于运动状态。
	public void onPickThePhone() {
		int currentVolume = audioManager
				.getStreamVolume(AudioManager.STREAM_RING);
		if (currentVolume > mMaxVolume * 0.3) {
			mAccelerometerListener.enable(false);
			startLowerRingerVolume();
		}
	}
}
