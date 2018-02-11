package com.android.server.telecom;

import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

//    用户设置的铃声大于30%，小于100%时，1秒钟从30%增加到用户设置的铃声值，然后以10%每秒的幅度增加至100%，
public class AurorNormalPolicy extends AuroraRingerPolicy {

	private Runnable r = new Runnable() {
		public void run() {
			audioManager.setStreamVolume(AudioManager.STREAM_RING,
					mCurrentVolume, 0);
			mVolumeHandler.sendEmptyMessageDelayed(UP_VOLUME, VOLUME_LENGTH);
		}
	};

	public AurorNormalPolicy(Context context) {
		super(context);
		mUpRate = 0.1;
	}

	public void start() {
		audioManager.setStreamVolume(AudioManager.STREAM_RING,
				(int) (mMaxVolume * 0.3), 0);
		mVolumeHandler.postDelayed(r, VOLUME_LENGTH);
		super.start();
	}

	public void clearVolumeParameters() {
		mVolumeHandler.removeCallbacks(r);
		super.clearVolumeParameters();
	}
}