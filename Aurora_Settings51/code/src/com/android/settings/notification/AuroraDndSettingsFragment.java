/**********************************************
 * 文件名：AuroraDndSettingsFragment.java
 * 说明：勿扰模式设置相关
 * 创建日期：2016-01-06
 * 修改日期：
 * [1] 2016-01-06 create by hujianwei
 * 
 * *********************************************/

package com.android.settings.notification;

import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;

import java.util.Objects;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.android.settings.R;
import android.service.notification.ZenModeConfig;
import android.app.INotificationManager;
import android.app.NotificationManager;
import android.os.ServiceManager;
import android.provider.Settings;
import android.provider.Settings.Global;

import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreference.OnPreferenceClickListener;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import aurora.preference.AuroraPreferenceGroup;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraSwitchPreference;
import com.android.settings.AuroraSettingsPreferenceFragment;
import com.android.settings.accounts.ChooseAccountActivity;
import com.android.settings.notification.AuroraDndTimingPickerPreference;

// ZEN_MODE_OFF = 0;
// ZEN_MODE_IMPORTANT_INTERRUPTIONS = 1;
// ZEN_MODE_NO_INTERRUPTIONS = 2;

public class AuroraDndSettingsFragment extends AuroraSettingsPreferenceFragment {

	private static final String TAG = "AuroraDndSettingsFragment";

	private static final String KEY_DND_ENABLE = "dnd_enable";
	private static final String KEY_TIMING_ENABLE = "timing_enable_disturb";
	private static final String KEY_REPEATED_CALLS_REMIND = "repeated_calls_remind";
	private static final String KEY_TIMING_PIACK = "timing_picker";
	private static final String KEY_ALLOW_CALLS_FROM = "allows_calls_from";
	private static final String KEY_ALLOW_CALLS = "ringer_enable";
	private final int ANYONE_NO_ACCEPT = 0;

	private AuroraSwitchPreference mDndEnableSwitchPref = null;
	private AuroraSwitchPreference mTimingEnableSwitchPref = null;
	private AuroraSwitchPreference mRepeatedCallsSwitchPref = null;
	private AuroraDndTimingPickerPreference mDndTimingPicker = null;
	private AuroraSwitchPreference mAllowCallsSwitchPref = null;
	private AuroraDropDownPreference mAllowCallsFrom = null;

	
	private Context mContext;
	private ZenModeConfig gZenModeConfig = null;
	public int mZenMode = 0;
    private final Handler mHandler = new Handler();
    private final SettingsObserver mSettingsObserver = new SettingsObserver();
    
    
	@Override
	public void onCreate(Bundle icicle) {

		super.onCreate(icicle);
		addPreferencesFromResource(R.xml.aurora_do_not_disturb_settings);

		gZenModeConfig = getZenModeConfig();
		mZenMode = getZenMode();
		mContext = getActivity();
		Log.d(TAG, "ZenModeConfig:" + gZenModeConfig);
		Log.d(TAG, "ZenMode:" + mZenMode);
		
		
		//勿扰模式开关
		mDndEnableSwitchPref = (AuroraSwitchPreference) findPreference(KEY_DND_ENABLE);
		mDndEnableSwitchPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(AuroraPreference preference, Object object) {
				Log.d(TAG, "*** mDndEnableSwitchPref onPreferenceChange 111");
				final boolean value = (Boolean) object;
				int mTmpMode = value ? Global.ZEN_MODE_IMPORTANT_INTERRUPTIONS : Global.ZEN_MODE_OFF;
				Log.d(TAG, "*** mDndEnableSwitchPref onPreferenceChange 222");
				if (mZenMode == mTmpMode) return false;
				setZenMode(mTmpMode);
				return true;
			}
			
		});
		
		//定时勿扰选择时间
		mDndTimingPicker = (AuroraDndTimingPickerPreference) findPreference(KEY_TIMING_PIACK);

		if (mDndTimingPicker != null) {
			mDndTimingPicker.AuroraDndTimingScreenInit(gZenModeConfig);
			mDndTimingPicker.setCallback(new AuroraDndTimingPickerPreference.Callback() {
						@Override
						public boolean onSetStartTime(int hour, int minute) {
							Log.d(TAG, "onSetStartTime");

							if (!ZenModeConfig.isValidHour(hour)) return false;
							if (!ZenModeConfig.isValidMinute(minute)) return false;

							if (hour == gZenModeConfig.sleepStartHour && minute == gZenModeConfig.sleepStartMinute)  return true;

							final ZenModeConfig newConfig = gZenModeConfig.copy();
							newConfig.sleepStartHour = hour;
							newConfig.sleepStartMinute = minute;
							return setZenModeConfig(newConfig);
						}

						@Override
						public boolean onSetEndTime(int hour, int minute) {
							Log.d(TAG, "onSetEndTime");
							if (!ZenModeConfig.isValidHour(hour)) return false;
							if (!ZenModeConfig.isValidMinute(minute))  return false;
							if (hour == gZenModeConfig.sleepEndHour && minute == gZenModeConfig.sleepEndMinute) return true;
							
							final ZenModeConfig newConfig = gZenModeConfig.copy();
							newConfig.sleepEndHour = hour;
							newConfig.sleepEndMinute = minute;
							return setZenModeConfig(newConfig);
						}
					});
		}
		
		//定时勿扰
		mTimingEnableSwitchPref = (AuroraSwitchPreference) findPreference(KEY_TIMING_ENABLE);
		mTimingEnableSwitchPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(AuroraPreference preference, Object object) {

				final boolean mTimingMode = object instanceof Boolean ? ((Boolean) object) : false;

				final ZenModeConfig newConfig = gZenModeConfig.copy();
				Log.d(TAG, "mTimingMode:" + mTimingMode);
				if(  mTimingMode ==  true){
					
					// 默认全周生效
					newConfig.sleepMode ="days:1,2,3,4,5,6,7";
					//newConfig.allowCalls = true;
					getPreferenceScreen().addPreference(mDndTimingPicker);
				}else{
					newConfig.sleepMode = null;
					//newConfig.allowCalls = false;
					getPreferenceScreen().removePreference(mDndTimingPicker);
				}
				newConfig.sleepNone = false;
				setZenModeConfig(newConfig);
				return true;
			}
		});
		
		mAllowCallsSwitchPref = (AuroraSwitchPreference) findPreference(KEY_ALLOW_CALLS);
		mAllowCallsSwitchPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(AuroraPreference preference, Object object) {
				Log.d(TAG, "*** mAllowCallsSwitchPref onPreferenceChange111 ");
				final boolean allowCalls = object instanceof Boolean ? ((Boolean) object) : false;

				if (gZenModeConfig == null || gZenModeConfig.allowCalls == allowCalls) return false;
				Log.d(TAG, "*** mAllowCallsSwitchPref onPreferenceChange222 ");
				final ZenModeConfig newConfig = gZenModeConfig.copy();
				newConfig.allowCalls = allowCalls;
				setZenModeConfig(newConfig);
				return true;
			}
		});		
		
		// 允许来电下拉框
		mAllowCallsFrom = (AuroraDropDownPreference) findPreference(KEY_ALLOW_CALLS_FROM);
		mAllowCallsFrom.addItem(R.string.alow_calls_from_starred, ZenModeConfig.SOURCE_STAR);
		mAllowCallsFrom.addItem(R.string.alow_calls_from_contacts, ZenModeConfig.SOURCE_CONTACT);
		mAllowCallsFrom.setCallback(new AuroraDropDownPreference.Callback() {
			@Override
			public boolean onItemSelected(int pos, Object newValue) {

                final int val = (Integer) newValue;
                if (val == gZenModeConfig.allowFrom) return true;

                final ZenModeConfig newConfig = gZenModeConfig.copy();
                newConfig.allowFrom = val;
                return setZenModeConfig(newConfig);
			}
		});

		mRepeatedCallsSwitchPref = (AuroraSwitchPreference) findPreference(KEY_REPEATED_CALLS_REMIND);
		mRepeatedCallsSwitchPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(AuroraPreference preference, Object object) {

				final boolean allowRepeat = object instanceof Boolean ? ((Boolean) object) : false;
				if (gZenModeConfig == null || gZenModeConfig.allowRepeatCallers == allowRepeat) return false;
				
				final ZenModeConfig newConfig = gZenModeConfig.copy();
				newConfig.allowRepeatCallers = allowRepeat;
				setZenModeConfig(newConfig);
				return true;
			}
		});
		
		updateControls();
	}

    @Override
    public void onResume() {
        super.onResume();
        updateControls();
        mSettingsObserver.register();
    }

    @Override
    public void onPause() {
        super.onPause();
        mSettingsObserver.unregister();
    }

	@Override
	public void onStop() {
		super.onStop();
	}

	private ZenModeConfig getZenModeConfig() {
		final INotificationManager nm = INotificationManager.Stub
				.asInterface(ServiceManager
						.getService(Context.NOTIFICATION_SERVICE));
		try {
			return nm.getZenModeConfig();
		} catch (Exception e) {
			Log.w(TAG, "Error calling NoMan", e);
			return new ZenModeConfig();
		}
	}

	private boolean setZenModeConfig(ZenModeConfig config) {
		final INotificationManager nm = INotificationManager.Stub
				.asInterface(ServiceManager
						.getService(Context.NOTIFICATION_SERVICE));
		try {
			final boolean success = nm.setZenModeConfig(config);
			
			if (success) {
				Log.d(TAG, "setconfig success!");
				gZenModeConfig = config;
			}
			return success;
		} catch (Exception e) {
			Log.w(TAG, "Error calling NoMan", e);
			return false;
		}
	}

	private int getZenMode() {
		return Global.getInt(this.getContentResolver(), Global.ZEN_MODE,
				Global.ZEN_MODE_OFF);
	}

	private void setZenMode(final int mode) {
		if (mZenMode == mode)
			return;
		mZenMode = mode;
		Global.putInt(this.getContentResolver(), Global.ZEN_MODE, mZenMode);
	}

	//定时勿扰是否关闭
	private boolean getDndTimingMode(){
		if(  gZenModeConfig.sleepMode == null){
			return false;
		}
		
		return gZenModeConfig.sleepMode.equals("days:1,2,3,4,5,6,7");
	}
	
	private void updateControls() {
		
		Log.d(TAG, "updateControls!");
		
		if (mDndEnableSwitchPref != null) {
			mDndEnableSwitchPref.setChecked(mZenMode == 1 ? true : false);
		}

		if (mTimingEnableSwitchPref != null) {
			mTimingEnableSwitchPref.setChecked(getDndTimingMode() );
			
			//定时勿扰选择时间
			if ( getDndTimingMode()) {
				getPreferenceScreen().addPreference(mDndTimingPicker);
			} else {
				getPreferenceScreen().removePreference(mDndTimingPicker);
			}
		}

		//通话是否启用
		if(  mAllowCallsSwitchPref != null ){
			mAllowCallsSwitchPref.setChecked(gZenModeConfig.allowCalls);
			//mAllowCallsSwitchPref.setEnabled( (mZenMode == Global.ZEN_MODE_IMPORTANT_INTERRUPTIONS ? true : false) || ( getDndTimingMode() ));
		}
		
		if (mAllowCallsFrom != null) {
			mAllowCallsFrom.setSelectedValue(gZenModeConfig.allowFrom);
		   //mAllowCallsFrom.setEnabled( gZenModeConfig.allowCalls  && mAllowCallsSwitchPref.isEnabled());
	}
		
		if (mRepeatedCallsSwitchPref != null) {
			mRepeatedCallsSwitchPref.setChecked(gZenModeConfig.allowRepeatCallers);
			//mRepeatedCallsSwitchPref.setEnabled( (mZenMode == Global.ZEN_MODE_IMPORTANT_INTERRUPTIONS ? true : false) || ( getDndTimingMode() ));
		}
		
		
	}
	
	private final class SettingsObserver extends ContentObserver {
        private final Uri ZEN_MODE_URI = Global.getUriFor(Global.ZEN_MODE);
        private final Uri ZEN_MODE_CONFIG_ETAG_URI = Global.getUriFor(Global.ZEN_MODE_CONFIG_ETAG);

        public SettingsObserver() {
            super(mHandler);
        }

        public void register() {
            getContentResolver().registerContentObserver(ZEN_MODE_URI, false, this);
            getContentResolver().registerContentObserver(ZEN_MODE_CONFIG_ETAG_URI, false, this);
        }

        public void unregister() {
            getContentResolver().unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);

            Log.d(TAG, "onChange uri:" + uri.toString());
            if (ZEN_MODE_URI.equals(uri)) {
            	
            	//更新立即启用
            	mZenMode = getZenMode();
            	if( mDndEnableSwitchPref != null){
            		mDndEnableSwitchPref.setChecked( mZenMode == Global.ZEN_MODE_IMPORTANT_INTERRUPTIONS ? true:false);
            	}
                
            }
            if (ZEN_MODE_CONFIG_ETAG_URI.equals(uri)) {
                updateZenModeConfig();
            }
            updateControls();
        }
    }
	
    private void updateZenModeConfig() {
    	
        final ZenModeConfig config = getZenModeConfig();
       
        if (Objects.equals(config, gZenModeConfig)) return;
        
        gZenModeConfig = config;
        
    }
}
