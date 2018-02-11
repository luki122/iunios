/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aurora.timer;

import aurora.app.AuroraActivity;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.android.deskclock.Alarms;
import com.android.deskclock.R;
//Gionee <baorui><2013-07-16> modify for CR00836845 begin
import com.aurora.utils.GnRingtoneUtil;
//Gionee <baorui><2013-07-16> modify for CR00836845 end

/**
 * Manages music and vibe. Runs as a service so that it can continue to play if
 * another activity overrides the AlarmAlert dialog.
 */
public class ChronometerService extends Service {

	private static final long[] sVibratePattern = new long[] { 500, 500 };

	private boolean mPlaying = false;
	private Vibrator mVibrator;
	private MediaPlayer mMediaPlayer;
	private TelephonyManager mTelephonyManager;
	private int mInitialCallState;

	private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
		@Override
		public void onCallStateChanged(int state, String ignored) {
			// The user might already be in a call when the alarm fires. When
			// we register onCallStateChanged, we get the initial in-call state
			// which kills the alarm. Check against the initial call state so
			// we don't kill the alarm during a call.
			if (state != TelephonyManager.CALL_STATE_IDLE
					&& state != mInitialCallState) {
				stopSelf();
			}
		}
	};

	@Override
	public void onCreate() {
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		// Listen for incoming calls to kill the alarm.
		mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mTelephonyManager.listen(mPhoneStateListener,
				PhoneStateListener.LISTEN_CALL_STATE);
		// AlarmAlertWakeLock.acquireCpuWakeLock(this);
		// ChronometerAlarmAlertWakeLock.acquireCpuWakeLock(this);
	}

	@Override
	public void onDestroy() {
		stop();
		// Stop listening for incoming calls.
		mTelephonyManager.listen(mPhoneStateListener, 0);
		// AlarmAlertWakeLock.releaseCpuLock();
		ChronometerAlarmAlertWakeLock
				.releaseCpuLock(ChronometerAlarmAlertWakeLock.CHRONMENTER);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// No intent, tell the system not to restart us.
		if (intent == null) {
			stopSelf();
			return START_NOT_STICKY;
		}

		play();

		// Record the initial call state here so that the new alarm has the
		// newest state.
		mInitialCallState = mTelephonyManager.getCallState();

		return START_STICKY;
	}

	// Volume suggested by media team for in-call alarms.
	private static final float IN_CALL_VOLUME = 0.125f;

	private void play() {
		// stop() checks to see if we are already playing.
		stop();

		SharedPreferences preferences = this.getSharedPreferences(
				"Chronometer", Activity.MODE_PRIVATE);
//		String uri = preferences.getString("ChronometerMusicUri",
//				Settings.System.DEFAULT_ALARM_ALERT_URI.toString());
		
		String uri = preferences.getString("ChronometerMusicUri",
				TimerFragment.DEFAULT_TIMEALARM);

		Uri alert = Uri.parse(uri);

        // Gionee <baorui><2013-07-11> modify for CR00832906 begin
        if (isAlertSilent(alert)) {
            mVibrator.vibrate(sVibratePattern, 0);
            mPlaying = true;

            return;
        }
        // Gionee <baorui><2013-07-11> modify for CR00832906 end

        // Gionee <baorui><2013-07-16> modify for CR00836845 begin
        alert = UpdateAlert(this, alert, preferences);
        // Gionee <baorui><2013-07-16> modify for CR00836845 end

		// TODO: Reuse mMediaPlayer instead of creating a new one and/or use
		// RingtoneManager.
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setOnErrorListener(new OnErrorListener() {
			public boolean onError(MediaPlayer mp, int what, int extra) {
				mp.stop();
				mp.release();
				mMediaPlayer = null;
				return true;
			}
		});

		try {
			// Check if we are in a call. If we are, use the in-call alarm
			// resource at a low volume to not disrupt the call.
			if (mTelephonyManager.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
				mMediaPlayer.setVolume(IN_CALL_VOLUME, IN_CALL_VOLUME);
				setDataSourceFromResource(getResources(), mMediaPlayer,
						R.raw.in_call_alarm);
			} else {
				mMediaPlayer.setDataSource(this, alert);
			}
			startAlarm(mMediaPlayer);
		} catch (Exception ex) {
			// The alert may be on the sd card which could be busy right
			// now. Use the fallback ringtone.
			try {
				// Must reset the media player to clear the error state.
				try {
					mMediaPlayer.reset();
					alert = RingtoneManager
							.getDefaultUri(RingtoneManager.TYPE_ALARM);
					mMediaPlayer.setDataSource(this, alert);
                    // Gionee <baorui><2013-07-05> modify for CR00827882 begin
                    /*
                    Editor editor = preferences.edit();
                    editor.remove("ChronometerMusicUri");
                    editor.remove("ChronometerMusicName");
                    editor.putString("ChronometerMusicUri", alert.toString());
                    editor.putString("ChronometerMusicName", getResources()
                    		.getString(R.string.default_ringtone));
                    editor.commit();
                    */
                    // Gionee <baorui><2013-07-05> modify for CR00827882 end
					startAlarm(mMediaPlayer);
				} catch (Exception exception) {
					mMediaPlayer.reset();
					setDataSourceFromResource(getResources(), mMediaPlayer,
							R.raw.in_call_alarm);
					startAlarm(mMediaPlayer);
				}
			} catch (Exception ex2) {
				// At this point we just don't play anything.
			}

		}

		/* Start the vibrator after everything is ok with the media player */
		mVibrator.vibrate(sVibratePattern, 0);

		mPlaying = true;
	}

	// Do the common stuff when starting the alarm.
	private void startAlarm(MediaPlayer player) throws java.io.IOException,
			IllegalArgumentException, IllegalStateException {
		final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		// do not play alarms if stream volume is 0
		// (typically because ringer mode is silent).
		if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
			player.setAudioStreamType(AudioManager.STREAM_ALARM);
			player.setLooping(true);
			player.prepare();
			player.start();
		}
	}

	private void setDataSourceFromResource(Resources resources,
			MediaPlayer player, int res) throws java.io.IOException {
		AssetFileDescriptor afd = resources.openRawResourceFd(res);
		if (afd != null) {
			player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
					afd.getLength());
			afd.close();
		}
	}

	/**
	 * Stops alarm audio and disables alarm if it not snoozed and not repeating
	 */
	public void stop() {
		if (mPlaying) {
			mPlaying = false;

			Intent alarmDone = new Intent(Alarms.ALARM_DONE_ACTION);
			sendBroadcast(alarmDone);

			// Stop audio playing
			if (mMediaPlayer != null) {
				mMediaPlayer.stop();
				mMediaPlayer.release();
				mMediaPlayer = null;
			}

			// Stop vibrator
			mVibrator.cancel();
		}
	}

    // Gionee <baorui><2013-07-11> modify for CR00832906 begin
    private boolean isAlertSilent(Uri alert) {
        boolean mIsSilent = false;
        String uri = null;

        if (RingtoneManager.isDefault(alert)) {
            uri = Settings.System.getString(getContentResolver(), Settings.System.ALARM_ALERT);
            if (uri == null) {
                mIsSilent = true;
            }
        }

        return mIsSilent;
    }
    // Gionee <baorui><2013-07-11> modify for CR00832906 end

    // Gionee <baorui><2013-07-16> modify for CR00836845 begin
    private Uri UpdateAlert(Context context, Uri oldUri, SharedPreferences preferences) {
        SharedPreferences prefs = getSharedPreferences("SettingsActivity", Activity.MODE_PRIVATE);

        String mData = preferences.getString("_data", prefs.getString("_data", null));
        int mVolumes = preferences.getInt("volumes", prefs.getInt("volumes", 0));
        Uri newUri = oldUri;

        if (!isRingtoneExist(oldUri, context)) {
            if (Alarms.isUpdateRintoneUri(mData, oldUri, context, mVolumes)) {
                newUri = Alarms.updateRintoneUri(mData, oldUri, context, mVolumes);
            }
        }

        return newUri;
    }

    private boolean isRingtoneExist(Uri uri, Context context) {
        Uri tempUri = uri;

        if (RingtoneManager.isDefault(uri)) {
            String mUriStr = Settings.System.getString(context.getContentResolver(),
                    Settings.System.ALARM_ALERT);
            tempUri = Uri.parse(mUriStr);
        }

        return GnRingtoneUtil.isRingtoneExist(tempUri, context.getContentResolver());
    }
    // Gionee <baorui><2013-07-16> modify for CR00836845 end
}
