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

package com.android.settings;

import com.android.settings.wifi.AuroraWifiApEnabler;
import com.android.settings.wifi.WifiApDialog;
import com.android.settings.wifi.WifiApFragment;
import com.android.settings.wifi.WifiSettings;

import android.app.Activity;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothPan;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemProperties;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraPreferenceActivity;
//Aurora <likai> <2013-10-28> add begin
import aurora.preference.AuroraSwitchPreference;
//Aurora <likai> <2013-10-28> add end
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Locale;

/*
 * Displays preferences for Tethering.
 */
public class TetherSettings extends SettingsPreferenceFragment
        implements DialogInterface.OnClickListener, AuroraPreference.OnPreferenceChangeListener {
    private static final String TAG = "TetherSettings";
    public  static boolean IsHotAccess=true;
    private static final String USB_TETHER_SETTINGS = "usb_tether_settings";
    private static final String ENABLE_WIFI_AP = "enable_wifi_ap";
    private static final String ENABLE_BLUETOOTH_TETHERING = "enable_bluetooth_tethering";

    private static final int DIALOG_AP_SETTINGS = 1;

    @Override
	public void onResume() {
		super.onResume();
		WifiSettings.PrintLog(TAG, "onResume");
		IsHotAccess=true;
	}

    private WebView mView;
    // Aurora <likai> <2013-10-28> modify begin
    //private AuroraCheckBoxPreference mUsbTether;
    private AuroraSwitchPreference mUsbTether;
    // Aurora <likai> <2013-10-28> modify end

    private AuroraWifiApEnabler mWifiApEnabler;
    // Aurora <likai> <2013-10-28> modify begin
    //private AuroraCheckBoxPreference mEnableWifiAp;
    private AuroraSwitchPreference mEnableWifiAp;

    //private AuroraCheckBoxPreference mBluetoothTether;
    private AuroraSwitchPreference mBluetoothTether;
    // Aurora <likai> <2013-10-28> modify end

    private BroadcastReceiver mTetherChangeReceiver;

    private String[] mUsbRegexs;

    private String[] mWifiRegexs;

    private String[] mBluetoothRegexs;
    private AtomicReference<BluetoothPan> mBluetoothPan = new AtomicReference<BluetoothPan>();

    private static final String WIFI_AP_SSID_AND_SECURITY = "wifi_ap_ssid_and_security";
    private static final int CONFIG_SUBTEXT = R.string.wifi_tether_configure_subtext;

    private String[] mSecurityType;
    private AuroraPreference mCreateNetwork;

    private WifiApDialog mDialog;
    private WifiApFragment mFragment;
    private WifiManager mWifiManager;
    private AuroraPreference mWifiApSettings;

    private boolean mUsbConnected;
    private boolean mMassStorageActive;

    private boolean mBluetoothEnableForTether;

    private static final int INVALID             = -1;
    private static final int WIFI_TETHERING      = 0;
    private static final int USB_TETHERING       = 1;
    private static final int BLUETOOTH_TETHERING = 2;

    /* One of INVALID, WIFI_TETHERING, USB_TETHERING or BLUETOOTH_TETHERING */
    private int mTetherChoice = INVALID;

    /* Stores the package name and the class name of the provisioning app */
    private String[] mProvisionApp;
    private static final int PROVISION_REQUEST = 0;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // Aurora <likai> <2013-10-28> add begin
        //addPreferencesFromResource(R.xml.tether_prefs);
        addPreferencesFromResource(R.xml.aurora_tether_prefs);
        // Aurora <likai> <2013-10-28> add end

        // Aurora <likai> <2013-10-28> add begin
        ((AuroraActivity) getActivity()).getAuroraActionBar().setTitle(R.string.aurora_tether_settings_title);
        // Aurora <likai> <2013-10-28> add end

        final Activity activity = getActivity();
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && adapter.getState() == BluetoothAdapter.STATE_ON) {
            adapter.getProfileProxy(activity.getApplicationContext(), mProfileServiceListener,
                    BluetoothProfile.PAN);
        }

        // Aurora <likai> <2013-10-28> modify begin
        /*
        mEnableWifiAp =
                (AuroraCheckBoxPreference) findPreference(ENABLE_WIFI_AP);
        AuroraPreference wifiApSettings = findPreference(WIFI_AP_SSID_AND_SECURITY);
        mUsbTether = (AuroraCheckBoxPreference) findPreference(USB_TETHER_SETTINGS);
        mBluetoothTether = (AuroraCheckBoxPreference) findPreference(ENABLE_BLUETOOTH_TETHERING);
        */
        mEnableWifiAp =
                (AuroraSwitchPreference) findPreference(ENABLE_WIFI_AP);
        mWifiApSettings = findPreference(WIFI_AP_SSID_AND_SECURITY);
        mUsbTether = (AuroraSwitchPreference) findPreference(USB_TETHER_SETTINGS);
        mBluetoothTether = (AuroraSwitchPreference) findPreference(ENABLE_BLUETOOTH_TETHERING);
        // Aurora <likai> <2013-10-28> modify end

        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        mUsbRegexs = cm.getTetherableUsbRegexs();
        mWifiRegexs = cm.getTetherableWifiRegexs();
        mBluetoothRegexs = cm.getTetherableBluetoothRegexs();

        final boolean usbAvailable = mUsbRegexs.length != 0;
        final boolean wifiAvailable = mWifiRegexs.length != 0;
        final boolean bluetoothAvailable = true;//mBluetoothRegexs.length != 0; //wolfu mark bluetooth tethering temporarily

        if (!usbAvailable || Utils.isMonkeyRunning()) {
            getPreferenceScreen().removePreference(mUsbTether);
        }

        if (wifiAvailable && !Utils.isMonkeyRunning()) {
            mWifiApEnabler = new AuroraWifiApEnabler(activity, mEnableWifiAp ,  TAG);
            initWifiTethering();
        } else {
            getPreferenceScreen().removePreference(mEnableWifiAp);
            getPreferenceScreen().removePreference(mWifiApSettings);
        }

        if (!bluetoothAvailable) {
            getPreferenceScreen().removePreference(mBluetoothTether);
        } else {
            BluetoothPan pan = mBluetoothPan.get();
            if (pan != null && pan.isTetheringOn()) {
                mBluetoothTether.setChecked(true);
            } else {
                mBluetoothTether.setChecked(false);
            }
        }

        mProvisionApp = getResources().getStringArray(
                R.array.config_mobile_hotspot_provision_app);

        mView = new WebView(activity);
    }

    private void initWifiTethering() {
        final Activity activity = getActivity();
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiApFragment.wifiConfig = mWifiManager.getWifiApConfiguration();
        mSecurityType = getResources().getStringArray(R.array.wifi_ap_security);

        mCreateNetwork = findPreference(WIFI_AP_SSID_AND_SECURITY);

        // Aurora <likai> <2013-10-28> delete mCreateNetwork.setSummary() begin
        /*
        if (mWifiConfig == null) {
            final String s = activity.getString(
                    R.string.aurora_wifi_tether_configure_ssid_default);
            mCreateNetwork.setSummary(String.format(activity.getString(CONFIG_SUBTEXT),
                    s, mSecurityType[WifiApDialog.OPEN_INDEX]));
        } else {
            int index = WifiApDialog.getSecurityTypeIndex(mWifiConfig);
            mCreateNetwork.setSummary(String.format(activity.getString(CONFIG_SUBTEXT),
                    mWifiConfig.SSID,
                    mSecurityType[index]));
        }
        */
        // Aurora <likai> <2013-10-28> delete mCreateNetwork.setSummary() end
    }

    private BluetoothProfile.ServiceListener mProfileServiceListener =
        new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            mBluetoothPan.set((BluetoothPan) proxy);
        }
        public void onServiceDisconnected(int profile) {
            mBluetoothPan.set(null);
        }
    };

    @Override
    public Dialog onCreateDialog(int id) {
        if (id == DIALOG_AP_SETTINGS) {
            final Activity activity = getActivity();
            mDialog = new WifiApDialog(activity, this, WifiApFragment.wifiConfig);
            return mDialog;
        }

        return null;
    }

    private class TetherChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context content, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.ACTION_TETHER_STATE_CHANGED)) {
                // TODO - this should understand the interface types
                ArrayList<String> available = intent.getStringArrayListExtra(
                        ConnectivityManager.EXTRA_AVAILABLE_TETHER);
                ArrayList<String> active = intent.getStringArrayListExtra(
                        ConnectivityManager.EXTRA_ACTIVE_TETHER);
                ArrayList<String> errored = intent.getStringArrayListExtra(
                        ConnectivityManager.EXTRA_ERRORED_TETHER);
                updateState(available.toArray(new String[available.size()]),
                        active.toArray(new String[active.size()]),
                        errored.toArray(new String[errored.size()]));
            } else if (action.equals(Intent.ACTION_MEDIA_SHARED)) {
                mMassStorageActive = true;
                updateState();
            } else if (action.equals(Intent.ACTION_MEDIA_UNSHARED)) {
                mMassStorageActive = false;
                updateState();
            } else if (action.equals(UsbManager.ACTION_USB_STATE)) {
                mUsbConnected = intent.getBooleanExtra(UsbManager.USB_CONNECTED, false);
                updateState();
            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                if (mBluetoothEnableForTether) {
                    switch (intent
                            .getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                        case BluetoothAdapter.STATE_ON:
                            BluetoothPan bluetoothPan = mBluetoothPan.get();
                            if (bluetoothPan != null) {
                                bluetoothPan.setBluetoothTethering(true);
                                mBluetoothEnableForTether = false;
                            }
                            break;

                        case BluetoothAdapter.STATE_OFF:
                        case BluetoothAdapter.ERROR:
                            mBluetoothEnableForTether = false;
                            break;

                        default:
                            // ignore transition states
                    }
                }
                updateState();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        final Activity activity = getActivity();

        mMassStorageActive = Environment.MEDIA_SHARED.equals(Environment.getExternalStorageState());
        mTetherChangeReceiver = new TetherChangeReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.ACTION_TETHER_STATE_CHANGED);
        Intent intent = activity.registerReceiver(mTetherChangeReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_STATE);
        activity.registerReceiver(mTetherChangeReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_SHARED);
        filter.addAction(Intent.ACTION_MEDIA_UNSHARED);
        filter.addDataScheme("file");
        activity.registerReceiver(mTetherChangeReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        activity.registerReceiver(mTetherChangeReceiver, filter);

        if (intent != null) mTetherChangeReceiver.onReceive(activity, intent);
        if (mWifiApEnabler != null) {
            mEnableWifiAp.setOnPreferenceChangeListener(this);
            mWifiApEnabler.resume();
			if(mWifiApEnabler.getSwitchChecked()) {
				mWifiApSettings.setEnabled(false);
			} else {
				mWifiApSettings.setEnabled(true);
			}
        }

        // Aurora <likai> <2013-11-02> add begin
        if (mUsbTether != null) {
            mUsbTether.setOnPreferenceChangeListener(this);
        }
        if (mBluetoothTether != null) {
        	mBluetoothTether.setOnPreferenceChangeListener(this);
        }
        // Aurora <likai> <2013-11-02> add end

        updateState();
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mTetherChangeReceiver);
        mTetherChangeReceiver = null;
        if (mWifiApEnabler != null) {
            mEnableWifiAp.setOnPreferenceChangeListener(null);
            mWifiApEnabler.pause();
        }
    }

    private boolean doUsbTetherCheckedChange = false;

    private void setUsbTetherChecked(boolean checked) {
        doUsbTetherCheckedChange = false;
        if (checked != mUsbTether.isChecked()) {
            mUsbTether.setChecked(checked);
        }
    }

    private void updateState() {
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        String[] available = cm.getTetherableIfaces();
        String[] tethered = cm.getTetheredIfaces();
        String[] errored = cm.getTetheringErroredIfaces();
        updateState(available, tethered, errored);
    }

    private void updateState(String[] available, String[] tethered,
            String[] errored) {
        updateUsbState(available, tethered, errored);
        updateBluetoothState(available, tethered, errored);
    }


    private void updateUsbState(String[] available, String[] tethered,
            String[] errored) {
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean usbAvailable = mUsbConnected && !mMassStorageActive;
        int usbError = ConnectivityManager.TETHER_ERROR_NO_ERROR;
        for (String s : available) {
            for (String regex : mUsbRegexs) {
                if (s.matches(regex)) {
                    if (usbError == ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                        usbError = cm.getLastTetherError(s);
                    }
                }
            }
        }
        boolean usbTethered = false;
        for (String s : tethered) {
            for (String regex : mUsbRegexs) {
                if (s.matches(regex)) usbTethered = true;
            }
        }
        boolean usbErrored = false;
        for (String s: errored) {
            for (String regex : mUsbRegexs) {
                if (s.matches(regex)) usbErrored = true;
            }
        }

        // Aurora <likai> <2013-10-28> delete mUsbTether.setSummary() begin
        if (usbTethered) {
            //mUsbTether.setSummary(R.string.usb_tethering_active_subtext);
            mUsbTether.setEnabled(true);
            //mUsbTether.setChecked(true);
            setUsbTetherChecked(true);
        } else if (usbAvailable) {
            if (usbError == ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                //mUsbTether.setSummary(R.string.usb_tethering_available_subtext);
            } else {
                //mUsbTether.setSummary(R.string.usb_tethering_errored_subtext);
            }
            mUsbTether.setEnabled(true);
            //mUsbTether.setChecked(false);
            if (doUsbTetherCheckedChange) return;
            setUsbTetherChecked(false);
        } else if (usbErrored) {
            //mUsbTether.setSummary(R.string.usb_tethering_errored_subtext);
            mUsbTether.setEnabled(false);
            //mUsbTether.setChecked(false);
            setUsbTetherChecked(false);
        } else if (mMassStorageActive) {
            //mUsbTether.setSummary(R.string.usb_tethering_storage_active_subtext);
            mUsbTether.setEnabled(false);
            //mUsbTether.setChecked(false);
            setUsbTetherChecked(false);
        } else {
            //mUsbTether.setSummary(R.string.usb_tethering_unavailable_subtext);
            mUsbTether.setEnabled(false);
            //mUsbTether.setChecked(false);
            setUsbTetherChecked(false);
        }
        // Aurora <likai> <2013-10-28> delete mUsbTether.setSummary() end
    }

    private void updateBluetoothState(String[] available, String[] tethered,
            String[] errored) {
        boolean bluetoothErrored = false;
        for (String s: errored) {
            for (String regex : mBluetoothRegexs) {
                if (s.matches(regex)) bluetoothErrored = true;
            }
        }

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        int btState = adapter.getState();
        // Aurora <likai> <2013-10-28> delete mBluetoothTether.setSummary() begin
        if (btState == BluetoothAdapter.STATE_TURNING_OFF) {
            mBluetoothTether.setEnabled(false);
            mBluetoothTether.setSummary(R.string.wifi_stopping);
        } else if (btState == BluetoothAdapter.STATE_TURNING_ON) {
            mBluetoothTether.setEnabled(false);
            mBluetoothTether.setSummary(R.string.bluetooth_turning_on);
        } else {
            BluetoothPan bluetoothPan = mBluetoothPan.get();
            if (btState == BluetoothAdapter.STATE_ON && bluetoothPan != null &&
                    bluetoothPan.isTetheringOn()) {
                mBluetoothTether.setChecked(true);
                mBluetoothTether.setEnabled(true);
                int bluetoothTethered = bluetoothPan.getConnectedDevices().size();
                if (bluetoothTethered > 1) {
                    String summary = getString(
                            R.string.bluetooth_tethering_devices_connected_subtext,
                            bluetoothTethered);
                    mBluetoothTether.setSummary(summary);
                } else if (bluetoothTethered == 1) {
                    mBluetoothTether.setSummary(
                            R.string.bluetooth_tethering_device_connected_subtext);
                } else if (bluetoothErrored) {
                    mBluetoothTether.setSummary(R.string.bluetooth_tethering_errored_subtext);
                } else {
                    mBluetoothTether.setSummary(R.string.bluetooth_tethering_available_subtext);
                }
            } else {
                mBluetoothTether.setEnabled(true);
                mBluetoothTether.setChecked(false);
                mBluetoothTether.setSummary(R.string.bluetooth_tethering_off_subtext);
            }
        }
        // Aurora <likai> <2013-10-28> delete mBluetoothTether.setSummary() end
    }

    public boolean onPreferenceChange(AuroraPreference preference, Object value) {
    	WifiSettings.PrintLog(TAG, "  preference:"+preference.toString());
        // Aurora <likai> <2013-11-02> modify begin
        /*boolean enable = (Boolean) value;

        if (enable) {
            startProvisioningIfNecessary(WIFI_TETHERING);
        } else {
            mWifiApEnabler.setSoftapEnabled(false);
        }
        return false;*/
    	ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        if (preference == mUsbTether) {
            boolean newState = (Boolean) value;

            if (newState) {
                startProvisioningIfNecessary(USB_TETHERING);
            } else {
                setUsbTethering(newState);
            }
        } else if (preference == mBluetoothTether) {
            boolean bluetoothTetherState = (Boolean) value;

            if (bluetoothTetherState) {
                startProvisioningIfNecessary(BLUETOOTH_TETHERING);
            } else {
                boolean errored = false;

                String [] tethered = cm.getTetheredIfaces();
                String bluetoothIface = findIface(tethered, mBluetoothRegexs);
                if (bluetoothIface != null &&
                        cm.untether(bluetoothIface) != ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                    errored = true;
                }

                BluetoothPan bluetoothPan = mBluetoothPan.get();
                if (bluetoothPan != null) bluetoothPan.setBluetoothTethering(false);
                // Aurora <likai> <2013-10-28> delete mBluetoothTether.setSummary() begin
                
                if (errored) {
                    mBluetoothTether.setSummary(R.string.bluetooth_tethering_errored_subtext);
                } else {
                    mBluetoothTether.setSummary(R.string.bluetooth_tethering_off_subtext);
                }
                
                // Aurora <likai> <2013-10-28> delete mBluetoothTether.setSummary() end
            }
        } else if (preference == mEnableWifiAp) {
        	boolean enable = (Boolean) value;
            WifiSettings.PrintLog(TAG, "     enable:"+enable);
        	if (enable) {
                mWifiApSettings.setEnabled(false);
                startProvisioningIfNecessary(WIFI_TETHERING);
            } else {
                mWifiApSettings.setEnabled(true);
                mWifiApEnabler.setSoftapEnabled(false);  
            }
        }

        return true;
        // Aurora <likai> <2013-11-02> modify end
    }

    boolean isProvisioningNeeded() {
        if (SystemProperties.getBoolean("net.tethering.noprovisioning", false)) {
            return false;
        }
        return mProvisionApp.length == 2;
    }

    private void startProvisioningIfNecessary(int choice) {
        mTetherChoice = choice;
        if (isProvisioningNeeded()) {
        	Log.i("likai", "isProvisioningNeeded()");
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClassName(mProvisionApp[0], mProvisionApp[1]);
            startActivityForResult(intent, PROVISION_REQUEST);
        } else {
            startTethering();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == PROVISION_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                startTethering();
            } else {
                //BT and USB need checkbox turned off on failure
                //Wifi tethering is never turned on until afterwards
                switch (mTetherChoice) {
                    case BLUETOOTH_TETHERING:
                        mBluetoothTether.setChecked(false);
                        break;
                    case USB_TETHERING:
                        //mUsbTether.setChecked(false);
                    	setUsbTetherChecked(false);
                        break;
                }
                mTetherChoice = INVALID;
            }
        }
    }

    private void startTethering() {
        switch (mTetherChoice) {
            case WIFI_TETHERING:
                mWifiApEnabler.setSoftapEnabled(true);
                break;
            case BLUETOOTH_TETHERING:
                // turn on Bluetooth first
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (adapter != null) {
                    if (adapter.getState() == BluetoothAdapter.STATE_OFF) {
                        //Get the Pan Profile proxy object while turning on BT
                        adapter.getProfileProxy(getActivity().getApplicationContext(),
                                mProfileServiceListener, BluetoothProfile.PAN);
                        mBluetoothEnableForTether = true;
                        adapter.enable();
                        mBluetoothTether.setSummary(R.string.bluetooth_turning_on);
                        mBluetoothTether.setEnabled(false);
                    } else {
                        BluetoothPan bluetoothPan = mBluetoothPan.get();
                        if (bluetoothPan != null) bluetoothPan.setBluetoothTethering(true);
                        mBluetoothTether.setSummary(R.string.bluetooth_tethering_available_subtext);
                    }
                }
                break;
            case USB_TETHERING:
                setUsbTethering(true);
                break;
            default:
                //should not happen
                break;
        }
    }

    private void setUsbTethering(boolean enabled) {
        ConnectivityManager cm =
            (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        //mUsbTether.setChecked(false);
        if (enabled) doUsbTetherCheckedChange = true;
        // Aurora <likai> <2013-10-28> delete mUsbTether.setSummary() begin
        if (cm.setUsbTethering(enabled) != ConnectivityManager.TETHER_ERROR_NO_ERROR) {
            //mUsbTether.setSummary(R.string.usb_tethering_errored_subtext);
            return;
        }
        //mUsbTether.setSummary("");
        // Aurora <likai> <2013-10-28> delete mUsbTether.setSummary() end
    }

    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen screen, AuroraPreference preference) {
    	// Aurora <likai> <2013-11-02> modify begin
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        if (preference == mUsbTether) {
            boolean newState = mUsbTether.isChecked();

            if (newState) {
                startProvisioningIfNecessary(USB_TETHERING);
            } else {
                setUsbTethering(newState);
            }
        } else if (preference == mBluetoothTether) {
            boolean bluetoothTetherState = mBluetoothTether.isChecked();

            if (bluetoothTetherState) {
                startProvisioningIfNecessary(BLUETOOTH_TETHERING);
            } else {
                boolean errored = false;

                String [] tethered = cm.getTetheredIfaces();
                String bluetoothIface = findIface(tethered, mBluetoothRegexs);
                if (bluetoothIface != null &&
                        cm.untether(bluetoothIface) != ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                    errored = true;
                }

                BluetoothPan bluetoothPan = mBluetoothPan.get();
                if (bluetoothPan != null) bluetoothPan.setBluetoothTethering(false);
                if (errored) {
                    mBluetoothTether.setSummary(R.string.bluetooth_tethering_errored_subtext);
                } else {
                    mBluetoothTether.setSummary(R.string.bluetooth_tethering_off_subtext);
                }
            }
        } else if (preference == mCreateNetwork) {
	((AuroraPreferenceActivity) getActivity()).startPreferencePanel(
					WifiApFragment.class.getCanonicalName(), null,
					R.string.wifi_tether_configure_ap_text, null, null, 0);
//	mFragment = new WifiApFragment(mWifiConfig);
        //showDialog(DIALOG_AP_SETTINGS);
        }
    	// Aurora <likai> <2013-11-02> modify end

        return super.onPreferenceTreeClick(screen, preference);
    }

    private static String findIface(String[] ifaces, String[] regexes) {
        for (String iface : ifaces) {
            for (String regex : regexes) {
                if (iface.matches(regex)) {
                    return iface;
                }
            }
        }
        return null;
    }

    public void onClick(DialogInterface dialogInterface, int button) {
        if (button == DialogInterface.BUTTON_POSITIVE) {
/*            mWifiConfig = mDialog.getConfig();
            if (mWifiConfig != null) {
                *//**
                 * if soft AP is stopped, bring up
                 * else restart with new config
                 * TODO: update config on a running access point when framework support is added
                 *//*
                if (mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLED) {
                    mWifiManager.setWifiApEnabled(null, false);
                    mWifiManager.setWifiApEnabled(mWifiConfig, true);
                } else {
                    mWifiManager.setWifiApConfiguration(mWifiConfig);
                }
                // Aurora <likai> <2013-10-28> delete mCreateNetwork.setSummary() begin
                
                int index = WifiApDialog.getSecurityTypeIndex(mWifiConfig);
                mCreateNetwork.setSummary(String.format(getActivity().getString(CONFIG_SUBTEXT),
                        mWifiConfig.SSID,
                        mSecurityType[index]));
                
                // Aurora <likai> <2013-10-28> delete mCreateNetwork.setSummary() end
            }*/
        }
    }

    @Override
    public int getHelpResource() {
        return R.string.help_url_tether;
    }
}
