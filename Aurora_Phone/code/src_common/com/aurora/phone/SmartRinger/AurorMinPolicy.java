package com.android.phone;

import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

//    用户设置的铃声低于30%时，铃声1秒钟从用户设置的值增强到50%，然后以10%每秒的幅度增加至100%；
public class AurorMinPolicy extends AuroraRingerPolicy {

	private Runnable r = new Runnable() {
		public void run() {
			audioManager.setStreamVolume(AudioManager.STREAM_RING,
					(int) (mMaxVolume * 0.5), 0);
			mVolumeHandler.sendEmptyMessageDelayed(UP_VOLUME, VOLUME_LENGTH);
		}
	};

	public AurorMinPolicy(Context context) {
		super(context);
		mUpRate = 0.1;
	}

	public void start() {
		mVolumeHandler.postDelayed(r, VOLUME_LENGTH);
		super.start();
	}

	public void clearVolumeParameters() {
		mVolumeHandler.removeCallbacks(r);
		super.clearVolumeParameters();
	}
}