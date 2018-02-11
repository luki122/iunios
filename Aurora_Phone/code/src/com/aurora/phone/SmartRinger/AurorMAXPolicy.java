package com.android.phone;

import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

//    用户设置铃声为100%时，铃声从40%开始增强，每秒增加25%至100%；
public class AurorMAXPolicy extends AuroraRingerPolicy {
	public AurorMAXPolicy(Context context) {
		super(context);
		mUpRate = 0.25;

	}

	public void start() {
		audioManager.setStreamVolume(AudioManager.STREAM_RING,
				(int) (mMaxVolume * 0.4), 0);
		mVolumeHandler.sendEmptyMessageDelayed(UP_VOLUME, VOLUME_LENGTH);
		super.start();
	}
}