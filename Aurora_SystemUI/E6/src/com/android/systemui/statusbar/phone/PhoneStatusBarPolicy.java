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

package com.android.systemui.statusbar.phone;

import android.app.StatusBarManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.RemoteException;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Slog;

import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.cdma.EriInfo;
import com.android.internal.telephony.cdma.TtyIntent;
import com.android.server.am.BatteryStatsService;
import com.android.systemui.R;

import com.android.systemui.Xlog;

// Gionee: <fengjianyi><2013-4-1> add for CR00792007 begin
import android.database.ContentObserver;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
// Gionee: <fengjianyi><2013-4-1> add for CR00792007 end
import android.os.Build;

/**
 * This class contains all of the policy about which icons are installed in the status
 * bar at boot time.  It goes through the normal API for icons, even though it probably
 * strictly doesn't need to.
 */
public class PhoneStatusBarPolicy {
    private static final String TAG = "PhoneStatusBarPolicy";

    // message codes for the handler
    private static final int EVENT_BATTERY_CLOSE = 4;

    private static final int AM_PM_STYLE_NORMAL  = 0;
    private static final int AM_PM_STYLE_SMALL   = 1;
    private static final int AM_PM_STYLE_GONE    = 2;

    private static final int AM_PM_STYLE = AM_PM_STYLE_GONE;

    private static final int INET_CONDITION_THRESHOLD = 50;

    private static final boolean SHOW_SYNC_ICON = false;

	// Aurora <zhanggp> <2013-10-08> added for systemui begin
	private static final String ACTION_CHANGE_STATUSBAR_BG = "aurora.action.CHANGE_STATUSBAR_BG";
	private Callback mCallback;
	// Aurora <zhanggp> <2013-10-08> added for systemui end
    private static final String ACTION_SET_STATUSBAR_TRANSPARENT = "aurora.action.SET_STATUSBAR_TRANSPARENT";

    // Aurora <Felix.Duan> <2014-6-18> <BEGIN> Support NaviBar full mode change from intent
    // Fix compile bug
    public static final String ACTION_SET_NAVIBAR_COLOR = "aurora.action.SET_NAVIBAR_COLOR";
    // Aurora <Felix.Duan> <2014-6-18> <END> Support NaviBar full mode change from intent

    private final Context mContext;
    private final StatusBarManager mService;
    private final Handler mHandler = new Handler();

    // storage
    private StorageManager mStorageManager;


    // Assume it's all good unless we hear otherwise.  We don't always seem
    // to get broadcasts that it *is* there.
    IccCardConstants.State mSimState = IccCardConstants.State.READY;

    // ringer volume
    private boolean mVolumeVisible;

    // bluetooth device status
    private boolean mBluetoothEnabled = false;

    // wifi
    private static final int[][] sWifiSignalImages = {
            { R.drawable.stat_sys_wifi_signal_1,
              R.drawable.stat_sys_wifi_signal_2,
              R.drawable.stat_sys_wifi_signal_3,
              R.drawable.stat_sys_wifi_signal_4 },
            { R.drawable.stat_sys_wifi_signal_1_fully,
              R.drawable.stat_sys_wifi_signal_2_fully,
              R.drawable.stat_sys_wifi_signal_3_fully,
              R.drawable.stat_sys_wifi_signal_4_fully }
        };
    private static final int sWifiTemporarilyNotConnectedImage =
            R.drawable.stat_sys_wifi_signal_0;

    private int mLastWifiSignalLevel = -1;
    private boolean mIsWifiConnected = false;

    // state of inet connection - 0 not connected, 100 connected
    private int mInetCondition = 0;

    // Gionee: <fengjianyi><2013-4-1> add for CR00792007 begin
    //private static final boolean GN_GUEST_MODE_SUPPORT = false;//SystemProperties.get("ro.gn.guestmode.support").equals("yes");
    /*
    private ContentObserver mGuestModeChangeObserver = new ContentObserver(new Handler()){

        @Override
        public void onChange(boolean selfChange) {
            Log.d(TAG, "Guest mode changed!");
            mService.setIconVisibility("guest_mode", isGuestModeEnabled());
        }
        
    };
    */
    // Gionee: <fengjianyi><2013-4-1> add for CR00792007 end

    // sync state
    // If sync is active the SyncActive icon is displayed. If sync is not active but
    // sync is failing the SyncFailing icon is displayed. Otherwise neither are displayed.

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_ALARM_CHANGED)) {
                updateAlarm(intent);
            }
            else if (action.equals(Intent.ACTION_SYNC_STATE_CHANGED)) {
                updateSyncState(intent);
            }
            else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED) ||
                    action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
                updateBluetooth(intent);
            }
            else if (action.equals(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
                updateVolume();
            }
            else if (action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
                updateSimState(intent);
            }
            else if (action.equals(TtyIntent.TTY_ENABLED_CHANGE_ACTION)) {
                updateTTY(intent);
            }
            /// M: [SystemUI] Support "Headset icon". @{
            else if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
                //gionee fengxb 2013-3-4 modify for CR00778294 start
                updateHeadSet(intent);
                //gionee fengxb 2013-3-4 modify for CR00778294 end
            }
            /// @}
            // Aurora <zhanggp> <2013-10-08> added for systemui begin
            else if (action.equals(ACTION_CHANGE_STATUSBAR_BG)) {
				if(mCallback != null){
					boolean isTransparent = intent.getBooleanExtra("transparent", false);
                	mCallback.updateStatusBarBgColor(isTransparent);
				}
            } else if (action.equals(ACTION_SET_STATUSBAR_TRANSPARENT)) {
                if (mCallback != null  && !Build.MODEL.contains("MI 3")) {
                    boolean isTransparent = intent.getBooleanExtra("transparent", false);
                    mCallback.setStausbarTransparentFlag(isTransparent);
                    //mCallback.updateStatusBarBgColor(isTransparent);
                    Log.v("xiaoyong", "ACTION_SET_STATUSBAR_TRANSPARENT"); 
                }
            }
			// Aurora <zhanggp> <2013-10-08> added for systemui end
        }

    };

	// Aurora <zhanggp> <2013-10-08> added for systemui begin
	public void setCallback(Callback cb){
		mCallback = cb;
	}
	public interface Callback {
        public void setStausbarTransparentFlag(boolean isTransparent);
		public void updateStatusBarBgColor(boolean isTransparent);
		public void setIcon(String slot, int iconId, int iconLevel,String contentDescription);
		public void setIconVisibility(String slot, boolean visible);
	}
	// Aurora <zhanggp> <2013-10-08> added for systemui end
    public PhoneStatusBarPolicy(Context context) {
        mContext = context;
        mService = (StatusBarManager)context.getSystemService(Context.STATUS_BAR_SERVICE);

        // listen for broadcasts
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_ALARM_CHANGED);
        filter.addAction(Intent.ACTION_SYNC_STATE_CHANGED);
        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        filter.addAction(TtyIntent.TTY_ENABLED_CHANGE_ACTION);
        /// M: [SystemUI] Support "Headset icon". @{
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        /// @}
        // Aurora <zhanggp> <2013-10-08> added for systemui begin
        filter.addAction(ACTION_CHANGE_STATUSBAR_BG);
		// Aurora <zhanggp> <2013-10-08> added for systemui end
        filter.addAction(ACTION_SET_STATUSBAR_TRANSPARENT);
        mContext.registerReceiver(mIntentReceiver, filter, null, mHandler);

        // Gionee: <fengjianyi><2013-4-1> add for CR00792007 begin
        /*
        if (GN_GUEST_MODE_SUPPORT) {
            mContext.getContentResolver().registerContentObserver(
                    Settings.Secure.getUriFor(Settings.Secure.GN_GUEST_MODE), false, mGuestModeChangeObserver);
        }
        */
        // Gionee: <fengjianyi><2013-4-1> add for CR00792007 end

        // storage
        mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        mStorageManager.registerListener(
                new com.android.systemui.usb.StorageNotification(context));

        // Gionee: <fengjianyi><2013-4-1> add for CR00792007 begin
        // guest mode
        /*
        if (GN_GUEST_MODE_SUPPORT) {
            mService.setIcon("guest_mode", R.drawable.zzzzz_stat_sys_guest_mode_on, 0, null);
            mService.setIconVisibility("guest_mode", isGuestModeEnabled());
        }
        */
        // Gionee: <fengjianyi><2013-4-1> add for CR00792007 end

        // TTY status
        mService.setIcon("tty",  R.drawable.stat_sys_tty_mode, 0, null);
        mService.setIconVisibility("tty", false);

        // Cdma Roaming Indicator, ERI
        mService.setIcon("cdma_eri", R.drawable.stat_sys_roaming_cdma_0, 0, null);
        mService.setIconVisibility("cdma_eri", false);

        // bluetooth status
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        int bluetoothIcon = R.drawable.stat_sys_data_bluetooth;
        if (adapter != null) {
            mBluetoothEnabled = (adapter.getState() == BluetoothAdapter.STATE_ON);
            if (adapter.getConnectionState() == BluetoothAdapter.STATE_CONNECTED) {
                bluetoothIcon = R.drawable.stat_sys_data_bluetooth_connected;
            }
        }
        mService.setIcon("bluetooth", bluetoothIcon, 0, null);
        mService.setIconVisibility("bluetooth", mBluetoothEnabled);

        // Alarm clock
        mService.setIcon("alarm_clock", R.drawable.stat_sys_alarm, 0, null);
        mService.setIconVisibility("alarm_clock", false);

        // Sync state
        mService.setIcon("sync_active", R.drawable.stat_sys_sync, 0, null);
        mService.setIcon("sync_failing", R.drawable.stat_sys_sync_error, 0, null);
        mService.setIconVisibility("sync_active", false);
        mService.setIconVisibility("sync_failing", false);

        // volume
        mService.setIcon("volume", R.drawable.stat_sys_ringer_silent, 0, null);
        mService.setIconVisibility("volume", false);
        updateVolume();

        /// M: [SystemUI] Support "Headset icon". @{
		// Aurora <zhanggp> <2013-10-17> modified for systemui begin
		if(null != mCallback){
			mCallback.setIcon("headset", R.drawable.stat_sys_headset_with_mic, 0, null);
			mCallback.setIconVisibility("headset", false);
		}
		// Aurora <zhanggp> <2013-10-17> modified for systemui end
        /// @}
    }

    private final void updateAlarm(Intent intent) {
        boolean alarmSet = intent.getBooleanExtra("alarmSet", false);
        mService.setIconVisibility("alarm_clock", alarmSet);
    }

    private final void updateSyncState(Intent intent) {
        if (!SHOW_SYNC_ICON) return;
        boolean isActive = intent.getBooleanExtra("active", false);
        boolean isFailing = intent.getBooleanExtra("failing", false);
        mService.setIconVisibility("sync_active", isActive);
        // Don't display sync failing icon: BUG 1297963 Set sync error timeout to "never"
        //mService.setIconVisibility("sync_failing", isFailing && !isActive);
    }

    private final void updateSimState(Intent intent) {
        String stateExtra = intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE);
        if (IccCardConstants.INTENT_VALUE_ICC_ABSENT.equals(stateExtra)) {
            mSimState = IccCardConstants.State.ABSENT;
        }
        else if (IccCardConstants.INTENT_VALUE_ICC_READY.equals(stateExtra)) {
            mSimState = IccCardConstants.State.READY;
        }
        else if (IccCardConstants.INTENT_VALUE_ICC_LOCKED.equals(stateExtra)) {
            final String lockedReason =
                    intent.getStringExtra(IccCardConstants.INTENT_KEY_LOCKED_REASON);
            if (IccCardConstants.INTENT_VALUE_LOCKED_ON_PIN.equals(lockedReason)) {
                mSimState = IccCardConstants.State.PIN_REQUIRED;
            }
            else if (IccCardConstants.INTENT_VALUE_LOCKED_ON_PUK.equals(lockedReason)) {
                mSimState = IccCardConstants.State.PUK_REQUIRED;
            }
            else {
                mSimState = IccCardConstants.State.NETWORK_LOCKED;
            }
        } else {
            mSimState = IccCardConstants.State.UNKNOWN;
        }
    }

    private final void updateVolume() {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        final int ringerMode = audioManager.getRingerMode();
        final boolean visible = ringerMode == AudioManager.RINGER_MODE_SILENT ||
                ringerMode == AudioManager.RINGER_MODE_VIBRATE;

        final int iconId;
        String contentDescription = null;
        if (ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
            iconId = R.drawable.stat_sys_ringer_vibrate;
            contentDescription = mContext.getString(R.string.accessibility_ringer_vibrate);
        } else {
            iconId =  R.drawable.stat_sys_ringer_silent;
            contentDescription = mContext.getString(R.string.accessibility_ringer_silent);
        }

        if (visible) {
            mService.setIcon("volume", iconId, 0, contentDescription);
        }
        if (visible != mVolumeVisible) {
            mService.setIconVisibility("volume", visible);
            mVolumeVisible = visible;
        }
    }

    private final void updateBluetooth(Intent intent) {
        int iconId = R.drawable.stat_sys_data_bluetooth;
        String contentDescription = null;
        String action = intent.getAction();
        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            mBluetoothEnabled = state == BluetoothAdapter.STATE_ON;
        } else if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE,
                BluetoothAdapter.STATE_DISCONNECTED);
            if (state == BluetoothAdapter.STATE_CONNECTED) {
                iconId = R.drawable.stat_sys_data_bluetooth_connected;
                contentDescription = mContext.getString(R.string.accessibility_bluetooth_connected);
            } else {
                contentDescription = mContext.getString(
                        R.string.accessibility_bluetooth_disconnected);
            }
        } else {
            return;
        }

        mService.setIcon("bluetooth", iconId, 0, contentDescription);
        mService.setIconVisibility("bluetooth", mBluetoothEnabled);
    }

    private final void updateTTY(Intent intent) {
        final String action = intent.getAction();
        final boolean enabled = intent.getBooleanExtra(TtyIntent.TTY_ENABLED, false);

        if (false) Slog.v(TAG, "updateTTY: enabled: " + enabled);

        if (enabled) {
            // TTY is on
            if (false) Slog.v(TAG, "updateTTY: set TTY on");
            mService.setIcon("tty", R.drawable.stat_sys_tty_mode, 0,
                    mContext.getString(R.string.accessibility_tty_enabled));
            mService.setIconVisibility("tty", true);
        } else {
            // TTY is off
            if (false) Slog.v(TAG, "updateTTY: set TTY off");
            mService.setIconVisibility("tty", false);
        }
    }

    /// M: [SystemUI] Support "Headset icon". @{
    private final void updateHeadSet(Intent intent) {
        int state = intent.getIntExtra("state", -1);
        int mic = intent.getIntExtra("microphone", -1);
        Xlog.d(TAG, "updateHeadSet, state=" + state + ", mic=" + mic + ".");
		// Aurora <zhanggp> <2013-10-18> modified for systemui begin
        if (state == -1 || mic == -1 || null == mCallback) {
            return;
        }
        if (state == 1) {
            if (mic == 1) {
                mCallback.setIcon("headset", R.drawable.stat_sys_headset_with_mic, 0, null);
                mCallback.setIconVisibility("headset", true);
            } else {
                mCallback.setIcon("headset", R.drawable.stat_sys_headset_without_mic, 0, null);
                mCallback.setIconVisibility("headset", true);
            }
        } else {
            mCallback.setIconVisibility("headset", false);
        }
		/*
        if (state == -1 || mic == -1) {
            return;
        }
        if (state == 1) {
            if (mic == 1) {
                mService.setIcon("headset", R.drawable.stat_sys_headset_with_mic, 0, null);
                mService.setIconVisibility("headset", true);
            } else {
                mService.setIcon("headset", R.drawable.stat_sys_headset_without_mic, 0, null);
                mService.setIconVisibility("headset", true);
            }
        } else {
            mService.setIconVisibility("headset", false);
        }
		*/
		// Aurora <zhanggp> <2013-10-17> modified for systemui end
    }
    /// @}

    // Gionee: <fengjianyi><2013-4-1> add for CR00792007 begin
    /*
    private boolean isGuestModeEnabled() {
    	int config = Settings.Secure.getInt(mContext.getContentResolver(),
				Settings.Secure.GN_GUEST_MODE, 0);
		Log.d(TAG, "Load guest mode config " + config);
		return config == 1;
    }
    */
    // Gionee: <fengjianyi><2013-4-1> add for CR00792007 end
}
