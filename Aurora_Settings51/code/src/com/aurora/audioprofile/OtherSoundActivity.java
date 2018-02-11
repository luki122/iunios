package com.aurora.audioprofile;

import com.android.settings.R;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.R.string;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.*;
import aurora.widget.AuroraActionBar;
import android.content.ContentResolver;
import android.os.Parcelable;

import com.aurora.featureoption.FeatureOption;
import com.mediatek.audioprofile.AudioProfileManager;

import aurora.preference.*;
import android.media.RingtoneManager;
import android.os.Vibrator;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.media.AudioSystem;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.SystemProperties;
import android.os.SystemVibrator;

public class OtherSoundActivity extends AuroraPreferenceActivity implements
		AuroraPreference.OnPreferenceChangeListener {
	static{
		MediaPlayer.stopMusic = true;
	}
	private AudioProfileManager mProfileManager;
	private AudioManager mAudioManager = null;
	public static final String KEY = "mtk_audioprofile_general";
	private static final String TAG = "AudioProfileActivity";

	public static final String KEY_DTMF_TONE = "dtmf_tone";
	public static final String KEY_SOUND_EFFECTS = "sound_effects";
	public static final String KEY_LOCK_SOUND = "lock_sounds";
	public static final String KEY_DTS = "enable_dts";
	private AuroraSwitchPreference mDtmfTone;
	private AuroraSwitchPreference mSoundEffects;
	private AuroraSwitchPreference mLockSounds;
	//private AuroraSwitchPreference mEnableDTS;
	public static final String ACTION_UPDATE_MUTE_VIBRATE = "gn.com.android.audioprofile.action.UPDATE_MUTE_VIBRATE";
	private static final String ACTION_RECENTS_PANEL_HIDDEN = "com.android.systemui.recent.aurora.RECENTS_PANEL_HIDDEN";
	public static final int RINGER_MODE_SILENT = 0;

	private AuroraSwitchPreference mMuteSwitchPreference;
	private AuroraSwitchPreference mVibrateSwitchPreference;
	private AuroraActionBar auroraActionBar;

	private OnAuroraActionBarBackItemClickListener auroActionBarItemBackListener = new OnAuroraActionBarBackItemClickListener() {
		public void onAuroraActionBarBackItemClicked(int itemId) {
			switch (itemId) {
			case -1:
				finish();
				overridePendingTransition(
						com.aurora.R.anim.aurora_activity_close_enter,
						com.aurora.R.anim.aurora_activity_close_exit);
				break;
			default:
				break;
			}
		}
	};

	private final BroadcastReceiver mUpdateUIReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ACTION_UPDATE_MUTE_VIBRATE)) {
				int ringMode = intent.getIntExtra("RING_MODE", -1);
				Log.v(TAG, "----ringMode=== " + ringMode);
			} else if (action.equals(ACTION_RECENTS_PANEL_HIDDEN)) {
				Log.v(TAG, "----updateUI===  ");
				updateUI();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.GnSettingsLightTheme);
		super.onCreate(savedInstanceState);

		mProfileManager = (AudioProfileManager) getSystemService(Context.AUDIO_PROFILE_SERVICE);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		addPreferencesFromResource(R.xml.other_sound_activity);
		// set title
		auroraActionBar = getAuroraActionBar();
		auroraActionBar.setTitle(R.string.other_sound_settings); // test title
		auroraActionBar
				.setmOnActionBarBackItemListener(auroActionBarItemBackListener);

		mDtmfTone = (AuroraSwitchPreference) findPreference(KEY_DTMF_TONE);
		mDtmfTone.setOnPreferenceChangeListener(this);
		mSoundEffects = (AuroraSwitchPreference) findPreference(KEY_SOUND_EFFECTS);
		mSoundEffects.setOnPreferenceChangeListener(this);
		mLockSounds = (AuroraSwitchPreference) findPreference(KEY_LOCK_SOUND);
		mLockSounds.setOnPreferenceChangeListener(this);
		// dts
		//mEnableDTS = (AuroraSwitchPreference) findPreference(KEY_DTS);
		setDtmfSoundLockScreenState(true);
	}

	private void setDtmfSoundLockScreenState(boolean state) {
		if (state) {
			boolean mDtmfToneState = mProfileManager.isDtmfToneEnabled(KEY);
			boolean mSoundState = mProfileManager.isSoundEffectEnabled(KEY);
			boolean mLockScreenState = mProfileManager.isLockScreenEnabled(KEY);
			Log.v(TAG, "---setDtmfSoundLockScreenState----mDtmfToneState = "
					+ mDtmfToneState + "  mSoundState = " + mSoundState
					+ "  mLockScreenState = " + mLockScreenState);
			mDtmfTone.setChecked(mDtmfToneState);
			mSoundEffects.setChecked(mSoundState);
			mLockSounds.setChecked(mLockScreenState);
			mDtmfTone.setEnabled(true);
			mSoundEffects.setEnabled(true);
			mLockSounds.setEnabled(true);
		} else {
			mDtmfTone.setChecked(false);
			mSoundEffects.setChecked(false);
			mLockSounds.setChecked(false);
			mDtmfTone.setEnabled(false);
			mSoundEffects.setEnabled(false);
			mLockSounds.setEnabled(false);
		}
	}

	@Override
	public boolean onPreferenceChange(AuroraPreference preference,
			Object newValue) {
		boolean isChecked = (Boolean) newValue;
		/*if (preference == mEnableDTS) {
			if ((Boolean) newValue) {
				mAudioManager
						.setParameters("srs_cfg:trumedia_enable=1;srs_cfg:trumedia_preset=0");
			} else {
				mAudioManager.setParameters("srs_cfg:trumedia_enable=0");
			}

			// 获取到sharepreference 对象， 参数一为xml文件名，参数为文件的可操作模式
			SharedPreferences iuniSP = getSharedPreferences("iuni",
					Context.MODE_PRIVATE);
			// 获取到编辑对象
			SharedPreferences.Editor edit = iuniSP.edit();
			edit.putBoolean(KEY_DTS, (Boolean) newValue);
			// 提交.
			edit.commit();
		}

		else*/ if (mDtmfTone == preference || mSoundEffects == preference
				|| mLockSounds == preference) {
			int ringMode = mAudioManager.getRingerModeInternal();
			Log.v(TAG,
					"----onPreferenceChange--DtmfSoundEffectLockSounds-----ringMode==="
							+ ringMode);
			if (ringMode == AudioManager.RINGER_MODE_SILENT
					|| ringMode == AudioManager.RINGER_MODE_VIBRATE) {
				return true;
			}

			if (mDtmfTone == preference) {

				mProfileManager.setDtmfToneEnabled(KEY, isChecked);
			} else if (mSoundEffects == preference) {

				mProfileManager.setSoundEffectEnabled(KEY, isChecked);
			} else if (mLockSounds == preference) {

				mProfileManager.setLockScreenEnabled(KEY, isChecked);
			}
		}

		return true;
	}

	private void updateUI() {
		int ringMode = mAudioManager.getRingerModeInternal();
		if (ringMode == AudioManager.RINGER_MODE_SILENT
				|| ringMode == AudioManager.RINGER_MODE_VIBRATE) {
			if (ringMode == AudioManager.RINGER_MODE_SILENT) {
				mMuteSwitchPreference.setChecked(true);
				mVibrateSwitchPreference.setChecked(false);
			} else {
				mMuteSwitchPreference.setChecked(true);
				mVibrateSwitchPreference.setChecked(true);
			}
			setDtmfSoundLockScreenState(false);
		} else {
			mMuteSwitchPreference.setChecked(false);
			if (Settings.System.getInt(getContentResolver(),
					Settings.System.VIBRATE_WHEN_RINGING, 0) != 0) {
				mVibrateSwitchPreference.setChecked(true);
			} else {
				mVibrateSwitchPreference.setChecked(false);
			}
			setDtmfSoundLockScreenState(true);
		}
	}
}
