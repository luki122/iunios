/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.settings; 

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraSwitchPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceScreen;
//M: ADD @{
import aurora.preference.AuroraPreferenceActivity;
//M: }
import android.provider.Settings;

//Aurora <likai> <2013-10-19> modify begin
//import android.provider.Telephony.SIMInfo;
import gionee.provider.GnTelephony.SIMInfo;
import com.gionee.internal.telephony.GnTelephonyIntents;
//Aurora <likai> <2013-10-19> modify end

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager; 

import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;
import com.android.settings.nfc.NfcEnabler;
//M: ADD @{
import com.android.settings.nfc.AndroidBeam;
/*import com.mediatek.nfc.MtkNfcEnabler;
import com.mediatek.nfc.NfcPreference;
import com.mediatek.nfc.NfcSettings;*/
//M: }
import com.aurora.featureoption.FeatureOption;
import com.mediatek.wireless.UsbSharingInfo;

// Aurora <likai> <2013-10-29> modify begin
//import com.mediatek.xlog.Log;
import android.util.Log;
// Aurora <likai> <2013-10-29> modify end


//Aurora <steveTang> <2014-05-26> modify begin
import gionee.telephony.GnTelephonyManager;
//Aurora <steveTang> <2014-05-26> modify end

import java.util.List;
// Gionee <wangyaohui><2013-05-28> add for CR00820266 begin
import com.mediatek.wireless.UsbSharingChoose;
// Gionee <wangyaohui><2013-05-28> add for CR00820266 end
// Gionee <wangyaohui><2013-05-28> modify for CR00820266 begin
// public class WirelessSettings extends SettingsPreferenceFragment {
public class WirelessSettings extends SettingsPreferenceFragment 
        implements AuroraPreference.OnPreferenceChangeListener {
// Gionee <wangyaohui><2013-05-28> modify for CR00820266 end

    private static final String TAG = "WirelessSettings";
    private static final String KEY_TOGGLE_AIRPLANE = "toggle_airplane";
    private static final String KEY_TOGGLE_NFC = "toggle_nfc";
    //M: ADD @{
    private static final String KEY_MTK_TOGGLE_NFC = "toggle_mtk_nfc";
    //M: }
    private static final String KEY_WIMAX_SETTINGS = "wimax_settings";
    private static final String KEY_ANDROID_BEAM_SETTINGS = "android_beam_settings";
    private static final String KEY_VPN_SETTINGS = "vpn_settings";
    private static final String KEY_TETHER_SETTINGS = "tether_settings";
    private static final String KEY_PROXY_SETTINGS = "proxy_settings";
    private static final String KEY_MOBILE_NETWORK_SETTINGS = "mobile_network_settings";
    private static final String KEY_TOGGLE_NSD = "toggle_nsd"; //network service discovery
    private static final String KEY_CELL_BROADCAST_SETTINGS = "cell_broadcast_settings";

    //Gionee:zhang_xin 2013-02-01 add for CR00767874 start
     private static final String KEY_DATA_USAGE_SETTINGS = "data_usage_settings";
    //Gionee:zhang_xin 2013-02-01 add for CR00767874 end
    //Gionee <wangguojing> <2013-08-19> add for CR00857643 begin
    private boolean GnGeminiSupport = SystemProperties.get("ro.gn.gemini.support", "no").equals("yes");
    //Gionee <wangguojing> <2013-08-19> add for CR00857643 end

    /// M: @{
    private static final String RCSE_SETTINGS_INTENT = "com.mediatek.rcse.RCSE_SETTINGS";
    private static final String KEY_RCSE_SETTINGS = "rcse_settings";

    private static final String KEY_USB_SHARING = "usb_sharing";
    private static final String USB_DATA_STATE = "mediatek.intent.action.USB_DATA_STATE";
    /// @}

    public static final String EXIT_ECM_RESULT = "exit_ecm_result";
    public static final int REQUEST_CODE_EXIT_ECM = 1;

    private AirplaneModeEnabler mAirplaneModeEnabler;
    private AuroraCheckBoxPreference mAirplaneModePreference;
    private NfcEnabler mNfcEnabler;
    //M: ADD @{
    /*private MtkNfcEnabler mMTKNfcEnabler;
    private NfcPreference mNfcPreference;*/
    //M: }
    private NfcAdapter mNfcAdapter;
    private NsdEnabler mNsdEnabler;

    //Gionee:zhang_xin 2013-02-01 add for CR00767874 start
     private AuroraPreferenceScreen mDataUsageSettingsPreference;
    //Gionee:zhang_xin 2013-02-01 add for CR00767874 end

    /// M: @{
    private AuroraPreferenceScreen mNetworkSettingsPreference;
    //Gemini phone instance
    //In order to do not run with phone process
    private ITelephony mTelephony;
    // Gionee liuyanbo 2012-11-19 add for CR00734108 start
     private static final String KEY_TOGGLE_WIFI_P2P = "toggle_wifi_p2p";
     private static final String KEY_WIFI_P2P_SETTINGS = "wifi_p2p_settings";
//    private WifiP2pEnabler mWifiP2pEnabler;
  // Gionee liuyanbo 2012-11-19 add for CR00734108 end
    private AuroraCheckBoxPreference mUsbSharing;
    private ConnectivityManager mConnectivityManager;
    private IntentFilter mIntentFilter;
    private AuroraPreference mTetherSettings;
    /// @}

    private AuroraSwitchPreference mNfcSwitch;
    /**
     * M: USB internet sharing connect state receiver
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String dataApnKey = intent.getStringExtra(PhoneConstants.DATA_APN_KEY);
            if (USB_DATA_STATE.equals(action) 
                    && "internet".equals(dataApnKey)) {
                PhoneConstants.DataState state = Enum.valueOf(PhoneConstants.DataState.class,
                        intent.getStringExtra(PhoneConstants.STATE_KEY));
                Log.d(TAG, "receive USB_DATA_STATE");
                Log.d(TAG, "dataApnKey = " + dataApnKey + ", state = " + state);
                switch (state) {
                    case CONNECTING:
                        mUsbSharing.setEnabled(false);
                        mUsbSharing.setChecked(false);
                        mUsbSharing.setSummary(R.string.radioInfo_data_connecting);
                        break;
                    case CONNECTED:
                        mUsbSharing.setEnabled(true);
                        mUsbSharing.setChecked(true);
                        mUsbSharing.setSummary(R.string.radioInfo_data_connected);
                        break;
                    case SUSPENDED:
                        break;
                    case DISCONNECTED:
                        mUsbSharing.setEnabled(true);
                        mUsbSharing.setChecked(false);
                        mUsbSharing.setSummary(R.string.usb_sharing_summary);
                        break;
                    default:
                        break;
                }
            } else if (action.equals(UsbManager.ACTION_USB_STATE)) {
                boolean usbConnected = intent.getBooleanExtra(UsbManager.USB_CONNECTED, false);
                boolean usbTethered = false;
                String[] tethered = mConnectivityManager.getTetheredIfaces();
                String[] usbRegexs = mConnectivityManager.getTetherableUsbRegexs();
                for (String s : tethered) {
                    for (String regex : usbRegexs) {
                        if (s.matches(regex)) { 
                            usbTethered = true;
                        }
                    }
                }
                Log.d(TAG, "onReceive: ACTION_USB_STATE usbConnected:" +
                    usbConnected + " usbTethered:" + usbTethered);
                if (!mUsbSharing.isChecked()) {
                    mUsbSharing.setEnabled(usbConnected && !usbTethered);
                }
			//Aurora <steveTang> <2014-05-26> modify begin
            //}  else if (action.equals(GnTelephonyIntents.ACTION_SIM_INFO_UPDATE)) {
            }  else if (action.equals(GnTelephonyIntents.ACTION_SIM_INFO_UPDATE) && GnTelephonyManager.isMultiSimEnabled()) {
			//Aurora <steveTang> <2014-05-26> modify end
                ///M: add for hot swap {
                Log.d(TAG, "ACTION_SIM_INFO_UPDATE received");
                List<SIMInfo> simList = SIMInfo.getInsertedSIMList(getActivity());
                if (simList != null) {
                    Log.d(TAG, "sim card number is: " + simList.size());
                    mNetworkSettingsPreference.setEnabled(simList.size() > 0);
                }
                ///@}
            }
        }
    };

    /**
     * Invoked on each preference click in this hierarchy, overrides
     * AuroraPreferenceActivity's implementation.  Used to make sure we track the
     * preference click events.
     */
    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
        if (preference == mAirplaneModePreference && Boolean.parseBoolean(
                SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE))) {
            // In ECM mode launch ECM app dialog
            startActivityForResult(
                new Intent(TelephonyIntents.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS, null),
                REQUEST_CODE_EXIT_ECM);
            return true;
        } else if (preference == mUsbSharing) {
            if (mUsbSharing.isChecked()) {
//                mConnectivityManager.setUsbInternet(true);
                startFragment(this, UsbSharingInfo.class.getName(), 0, null, R.string.usb_sharing_title);
            } else {
//                mConnectivityManager.setUsbInternet(false);
            }
            return true;    
        //M: add @{
        } /*else if (preference == mNfcPreference) {
            ((AuroraPreferenceActivity)getActivity()).startPreferencePanel(
                    NfcSettings.class.getName(), null, 0, null, null, 0);
        }*/
        //M: @}
        // Let the intents be launched by the AuroraPreference manager
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    // Gionee <wangyaohui><2013-05-28> add for CR00820266 begin
    public boolean onPreferenceChange(AuroraPreference preference, Object value) {
        String key = preference.getKey();
        if (KEY_USB_SHARING.equals(key)) {
            if (mUsbSharing.isChecked()) {
//                mConnectivityManager.setUsbInternet(false);
            } else {
                startFragment(this, UsbSharingChoose.class.getName(), 0, null, R.string.usb_sharing_title);
                getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
            }
            return false;
        }
        return true;
    }
    // Gionee <wangyaohui><2013-05-28> add for CR00820266 end
    public static boolean isRadioAllowed(Context context, String type) {
        if (!AirplaneModeEnabler.isAirplaneModeOn(context)) {
            return true;
        }
        // Here we use the same logic in onCreate().
        String toggleable = Settings.Global.getString(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_TOGGLEABLE_RADIOS);
        return toggleable != null && toggleable.contains(type);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.wireless_settings);

        final boolean isSecondaryUser = UserHandle.myUserId() != UserHandle.USER_OWNER;

        final Activity activity = getActivity();
        mAirplaneModePreference = (AuroraCheckBoxPreference) findPreference(KEY_TOGGLE_AIRPLANE);
        mNfcSwitch = (AuroraSwitchPreference) findPreference(KEY_TOGGLE_NFC);
        AuroraPreferenceScreen androidBeam = (AuroraPreferenceScreen) findPreference(KEY_ANDROID_BEAM_SETTINGS);
        if (mNfcSwitch != null) {
//            getPreferenceScreen().removePreference(nfc);
        }
        if (androidBeam != null) {
            getPreferenceScreen().removePreference(androidBeam);
        }
        //M: add @{
        //mNfcPreference = (NfcPreference) findPreference(KEY_MTK_TOGGLE_NFC);
		if(Build.MODEL.equals("IUNI U810")) {
			mNfcAdapter = null;
		} else {
			mNfcAdapter = NfcAdapter.getDefaultAdapter(activity);
		}
    /// M: @{
        mNetworkSettingsPreference = (AuroraPreferenceScreen) findPreference(KEY_MOBILE_NETWORK_SETTINGS);
        mUsbSharing = (AuroraCheckBoxPreference) findPreference(KEY_USB_SHARING);

        // Aurora <likai> add begin
        if (mUsbSharing != null) {
        	getPreferenceScreen().removePreference(mUsbSharing);
        }
        // Aurora <likai> add end

        /// @}
        AuroraCheckBoxPreference nsd = (AuroraCheckBoxPreference) findPreference(KEY_TOGGLE_NSD);

        mAirplaneModeEnabler = new AirplaneModeEnabler(activity, mAirplaneModePreference);
        //Gionee:zhang_xin 2013-02-01 modify for CR00767874 start
        // Gionee <wangyaohui><2013-05-14> modify for CR00804172 begin
        /*
        mDataUsageSettingsPreference = (AuroraPreferenceScreen) findPreference(KEY_DATA_USAGE_SETTINGS);
        if (GnSettingsThemeUtils.sGnSettingSupport) {
            getPreferenceScreen().removePreference(mAirplaneModePreference);
            getPreferenceScreen().removePreference(mNetworkSettingsPreference);
        }*/
        Intent i = getActivity().getIntent();
        String action = i.getAction();
        boolean  removeAirPlane = true;
        Log.e("wangyaohui","action:" + action);
        if(Settings.ACTION_AIRPLANE_MODE_SETTINGS.equals(action)){
            removeAirPlane = false;
        }
        mDataUsageSettingsPreference = (AuroraPreferenceScreen) findPreference(KEY_DATA_USAGE_SETTINGS);
        /*if (GnSettingsUtils.sGnSettingSupport)*/ {
           if(removeAirPlane) {
                getPreferenceScreen().removePreference(mAirplaneModePreference);
            }
            getPreferenceScreen().removePreference(mNetworkSettingsPreference);
        }
        // Gionee <wangyaohui><2013-05-14> modify for CR00804172 end

        // Aurora <likai> modify begin
        /*
        if (!GnSettingsUtils.sGnSettingSupport) {
             getPreferenceScreen().removePreference(mDataUsageSettingsPreference);
        }
        */
        if (mDataUsageSettingsPreference != null) {
        	getPreferenceScreen().removePreference(mDataUsageSettingsPreference);
        }
        // Aurora <likai> modify end

        //Gionee:zhang_xin 2013-02-01 modify for CR00767874 end
        /*if (FeatureOption.MTK_NFC_ADDON_SUPPORT) {
            AuroraPreferenceScreen screen = getPreferenceScreen();
            screen.removePreference(nfc);
            screen.removePreference(androidBeam);
            mMTKNfcEnabler = new MtkNfcEnabler(activity, mNfcPreference, null, mNfcAdapter);
        } else {
            getPreferenceScreen().removePreference(mNfcPreference);*/
            mNfcEnabler = new NfcEnabler(activity, mNfcSwitch, androidBeam);
        //}
        //M: @}
        // Remove NSD checkbox by default
        getPreferenceScreen().removePreference(nsd);
        //mNsdEnabler = new NsdEnabler(activity, nsd);

        String toggleable = Settings.Global.getString(activity.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_TOGGLEABLE_RADIOS);

        //enable/disable wimax depending on the value in config.xml
        boolean isWimaxEnabled = !isSecondaryUser && this.getResources().getBoolean(
                R.bool.config_wimaxEnabled);
        if (!isWimaxEnabled) {
            AuroraPreferenceScreen root = getPreferenceScreen();
            AuroraPreference ps = (AuroraPreference) findPreference(KEY_WIMAX_SETTINGS);
            if (ps != null) {
                root.removePreference(ps);
            }
        } else {
            if (toggleable == null || !toggleable.contains(Settings.Global.RADIO_WIMAX)
                    && isWimaxEnabled) {
                AuroraPreference ps = (AuroraPreference) findPreference(KEY_WIMAX_SETTINGS);
				// Steve.tang 2014-06-20 remove all setDependency(KEY_TOGGLE_AIRPLANE), because air mode preference has removed, can not find preferece dependencies. start
                //ps.setDependency(KEY_TOGGLE_AIRPLANE);
            	// Steve.tang 2014-06-20 remove all setDependency(KEY_TOGGLE_AIRPLANE), because air mode preference has removed, can not find preferece dependencies. end
        	}
		}
        // Manually set dependencies for Wifi when not toggleable.
        if (toggleable == null || !toggleable.contains(Settings.Global.RADIO_WIFI)) {
			// Steve.tang 2014-06-20 remove all setDependency(KEY_TOGGLE_AIRPLANE), because air mode preference has removed, can not find preferece dependencies. start
            //findPreference(KEY_VPN_SETTINGS).setDependency(KEY_TOGGLE_AIRPLANE);
			// Steve.tang 2014-06-20 remove all setDependency(KEY_TOGGLE_AIRPLANE), because air mode preference has removed, can not find preferece dependencies. end
        }

        //if (isSecondaryUser) { // Disable VPN
//            removePreference(KEY_VPN_SETTINGS);
        //}

        // Manually set dependencies for Bluetooth when not toggleable.
        //if (toggleable == null || !toggleable.contains(Settings.Global.RADIO_BLUETOOTH)) {
            // No bluetooth-dependent items in the list. Code kept in case one is added later.
        //}

        // Manually set dependencies for NFC when not toggleable.
        if (toggleable == null || !toggleable.contains(Settings.Global.RADIO_NFC)) {
            //M: add @{
            /*if (FeatureOption.MTK_NFC_ADDON_SUPPORT) {
                mNfcPreference.setDependency(KEY_TOGGLE_AIRPLANE);
            } else {*/
				// Steve.tang 2014-06-20 remove all setDependency(KEY_TOGGLE_AIRPLANE), because air mode preference has removed, can not find preferece dependencies. start
                //mNfcSwitch.setDependency(KEY_TOGGLE_AIRPLANE);
				// Steve.tang 2014-06-20 remove all setDependency(KEY_TOGGLE_AIRPLANE), because air mode preference has removed, can not find preferece dependencies. send
            //}
            //M: @}
			// Steve.tang 2014-06-20 remove all setDependency(KEY_TOGGLE_AIRPLANE), because air mode preference has removed, can not find preferece dependencies. start
            //findPreference(KEY_ANDROID_BEAM_SETTINGS).setDependency(KEY_TOGGLE_AIRPLANE);
			// Steve.tang 2014-06-20 remove all setDependency(KEY_TOGGLE_AIRPLANE), because air mode preference has removed, can not find preferece dependencies. end
        }

        //M: add @{
        // Remove NFC if its not available
        if (mNfcAdapter == null) {
           /* if (FeatureOption.MTK_NFC_ADDON_SUPPORT) {
                getPreferenceScreen().removePreference(mNfcPreference);
                mMTKNfcEnabler = null;
            } else {*/
                getPreferenceScreen().removePreference(mNfcSwitch);
                mNfcEnabler = null;
            //}
            getPreferenceScreen().removePreference(androidBeam);
        }
        //M: @}
        
        // Remove Mobile Network Settings if it's a wifi-only device.
        if (isSecondaryUser || Utils.isWifiOnly(getActivity())) {
            /// M: remove preference
            getPreferenceScreen().removePreference(mNetworkSettingsPreference);
        }

        // Gionee liuyanbo 2012-11-19 add for CR00734108 start
        AuroraCheckBoxPreference wifiP2p = (AuroraCheckBoxPreference) findPreference(KEY_TOGGLE_WIFI_P2P);
//        if (Utils.gnWifiDirectSupport) {
//            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)
//                    || FeatureOption.MTK_WLAN_SUPPORT == false || (FeatureOption.MTK_CTA_SUPPORT == true)
//                    || (SystemProperties.getInt("ro.mediatek.wlan.p2p", 0) == 0)) {
//                getPreferenceScreen().removePreference(wifiP2p);
//                Log.i(TAG, "removePreference(wifiP2p);");
//            } else {
//                mWifiP2pEnabler = new WifiP2pEnabler(activity, wifiP2p);
//                Log.i(TAG, "new WifiP2pEnabler(activity, wifiP2p)");
//            }
//        } else {
//            getPreferenceScreen().removePreference(wifiP2p);
//        }        
        //Gionee <zhang_xin><2013-05-13> add for CR00810504 begin
        getPreferenceScreen().removePreference(wifiP2p);
        //Gionee <zhang_xin><2013-05-13> add for CR00810504 end
        if(findPreference(KEY_WIFI_P2P_SETTINGS)!=null){
            getPreferenceScreen().removePreference(findPreference(KEY_WIFI_P2P_SETTINGS));
        }
        // Gionee liuyanbo 2012-11-19 add for CR00734108 end
        
        // Enable Proxy selector settings if allowed.
        AuroraPreference mGlobalProxy = findPreference(KEY_PROXY_SETTINGS);
        DevicePolicyManager mDPM = (DevicePolicyManager)
                activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        // proxy UI disabled until we have better app support
        getPreferenceScreen().removePreference(mGlobalProxy);
        mGlobalProxy.setEnabled(mDPM.getGlobalProxyAdmin() == null);

        /// M: @{
        // Disable Tethering if it's not allowed or if it's a wifi-only device
        mConnectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        mTetherSettings = findPreference(KEY_TETHER_SETTINGS);
        if (mTetherSettings != null) {
//            getPreferenceScreen().removePreference(mTetherSettings);
        }

        if (mConnectivityManager != null) {
            if (isSecondaryUser || !mConnectivityManager.isTetheringSupported() || Utils.isWifiOnly(getActivity())) {
                getPreferenceScreen().removePreference(mTetherSettings);
            } else {
                mTetherSettings.setTitle(Utils.getTetheringLabel(mConnectivityManager));
            }
        }
        /// @}

        // Enable link to CMAS app settings depending on the value in config.xml.
        boolean isCellBroadcastAppLinkEnabled = this.getResources().getBoolean(
                R.bool.config_cellBroadcastAppLinks);
        try {
            if (isCellBroadcastAppLinkEnabled) {
                PackageManager pm = getPackageManager();
                if (pm.getApplicationEnabledSetting("com.android.cellbroadcastreceiver")
                        == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                    isCellBroadcastAppLinkEnabled = false;  // CMAS app disabled
                }
            }
        } catch (IllegalArgumentException ignored) {
            isCellBroadcastAppLinkEnabled = false;  // CMAS app not installed
        }
        if (isSecondaryUser || !isCellBroadcastAppLinkEnabled) {
            AuroraPreferenceScreen root = getPreferenceScreen();
            AuroraPreference ps = findPreference(KEY_CELL_BROADCAST_SETTINGS);
            if (ps != null) {
                root.removePreference(ps);
            }
        }

        /// M: @{
        //If rcse apk was not installed, then should hide rcse settings ui
        Intent intent = new Intent(RCSE_SETTINGS_INTENT);
        List<ResolveInfo> rcseApps = getPackageManager().queryIntentActivities(intent, 0);
        if (rcseApps == null || rcseApps.size() == 0) {
            Log.w(TAG, RCSE_SETTINGS_INTENT + " is not installed");
            getPreferenceScreen().removePreference(findPreference(KEY_RCSE_SETTINGS));
        } else {
            Log.w(TAG, RCSE_SETTINGS_INTENT + " is installed");
            findPreference(KEY_RCSE_SETTINGS).setIntent(intent);
        }

        mIntentFilter = new IntentFilter(USB_DATA_STATE);
        mIntentFilter.addAction(UsbManager.ACTION_USB_STATE);
        mIntentFilter.addAction(GnTelephonyIntents.ACTION_SIM_INFO_UPDATE);
        /// @}
    }
    /**
     * M: update mobile network enabled
     */
    private void updateMobileNetworkEnabled() {
        if (null == mTelephony) {
            Log.e(TAG, "Could not get mTelephony object");
            return;
        }
        boolean sim1Exist = true;
        boolean sim2Exist = true;
        if (FeatureOption.MTK_GEMINI_SUPPORT && GnGeminiSupport) {
            boolean dualHasSim = true;

            // Aurora <likai> <2013-11-08> modify begin
            /*try {
                sim1Exist = mTelephony.isSimInsert(PhoneConstants.GEMINI_SIM_1);
                sim2Exist = mTelephony.isSimInsert(PhoneConstants.GEMINI_SIM_2);
                dualHasSim = sim1Exist || sim2Exist;
            } catch (RemoteException e) {
               Log.i(TAG, "RemoteException happens......");
            }*/
            List<SIMInfo> simList = SIMInfo.getInsertedSIMList(getActivity());
            dualHasSim = (simList != null && simList.size() > 0);
            // Aurora <likai> <2013-11-08> modify end

            Log.i(TAG, "dualHasSim state: sim1Exist?" + sim1Exist + ", sim2Exist?" + sim2Exist);
            mNetworkSettingsPreference.setEnabled(dualHasSim);

		// Aurora <steveTang> <2014-05-26> modify for single sim. begin.
        } else if(!GnTelephonyManager.isMultiSimEnabled()) {
            boolean hasSim = true;

            TelephonyManager manager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
			int absent = manager.getSimState();  
			if (1 == absent) {  
				hasSim = false;
			}  
            Log.i(TAG, "single SIM version, hasSim?" + absent);
            mNetworkSettingsPreference.setEnabled(hasSim);
		// Aurora <steveTang> <2014-05-26>  modify for single sim. end.
        } else {
            boolean hasSim = true;

            // Aurora <likai> <2013-11-08> modify begin
            /*try {
                hasSim = mTelephony.isSimInsert(PhoneConstants.GEMINI_SIM_1);
            } catch (RemoteException e) {
                Log.i(TAG, "RemoteException happens......");
            }*/
            List<SIMInfo> simList = SIMInfo.getInsertedSIMList(getActivity());
            hasSim = (simList != null && simList.size() > 0);
            // Aurora <likai> <2013-11-08> modify end

            Log.i(TAG, "single SIM version, hasSim?" + hasSim);
            mNetworkSettingsPreference.setEnabled(hasSim);
        }        
    }
    /// M:  @{
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            Log.d(TAG, "PhoneStateListener, new state=" + state);
            if (state == TelephonyManager.CALL_STATE_IDLE && getActivity() != null) {
                TelephonyManager telephonyManager = 
                    (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                int currPhoneCallState = telephonyManager.getCallState();
                
                Log.d(TAG, "Total PhoneState =" + currPhoneCallState);
                if (currPhoneCallState == TelephonyManager.CALL_STATE_IDLE) {
                    //only if both SIM are in call state, we will enable mobile network settings
                    mTelephony = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
                    updateMobileNetworkEnabled();
                }
            } 
        }
    };
    /// @}
    @Override
    public void onResume() {
        super.onResume();

        mAirplaneModeEnabler.resume();
       /* if (FeatureOption.MTK_NFC_ADDON_SUPPORT) {
            if (mMTKNfcEnabler != null) {
                mMTKNfcEnabler.resume();
            }
        } else {*/
            if (mNfcEnabler != null) {
                mNfcEnabler.resume();
            }
        //}
        if (mNsdEnabler != null) {
            mNsdEnabler.resume();
        }
        
        /// M:  @{
        TelephonyManager telephonyManager = 
            (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        Log.d(TAG, "onResume(), call state=" + telephonyManager.getCallState());
        if (telephonyManager.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
            mNetworkSettingsPreference.setEnabled(false);
        } else {
            mNetworkSettingsPreference.setEnabled(true);
        }

        getActivity().registerReceiver(mReceiver, mIntentFilter);
        /// @}
    }

    @Override
    public void onPause() {
        super.onPause();
        /// M:  @{
        TelephonyManager telephonyManager = 
            (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);

        getActivity().unregisterReceiver(mReceiver);
        /// @}

        mAirplaneModeEnabler.pause();
       /* if (FeatureOption.MTK_NFC_ADDON_SUPPORT) {
            if (mMTKNfcEnabler != null) {
                mMTKNfcEnabler.pause();
            }
        } else {*/
            if (mNfcEnabler != null) {
                mNfcEnabler.pause();
            }
        //}
        if (mNsdEnabler != null) {
            mNsdEnabler.pause();
        }
        
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_EXIT_ECM) {
            Boolean isChoiceYes = data.getBooleanExtra(EXIT_ECM_RESULT, false);
            // Set Airplane mode based on the return value and checkbox state
            mAirplaneModeEnabler.setAirplaneModeInECM(isChoiceYes,
                    mAirplaneModePreference.isChecked());
        }
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_url_more_networks;
    }
}
