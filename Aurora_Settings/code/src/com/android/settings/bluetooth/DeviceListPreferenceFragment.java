/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.settings.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraPreferenceGroup;
import aurora.preference.AuroraPreferenceScreen;
import android.util.Log;
// Aurora liugj 2013-10-24 added for aurora's new feature start
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import aurora.widget.CustomAuroraActionBarItem;
import aurora.app.AuroraActivity;
// Aurora liugj 2013-10-24 added for aurora's new feature end

import com.android.settings.ProgressCategory;
import com.android.settings.SettingsPreferenceFragment;

import java.util.Collection;
import java.util.WeakHashMap;

import com.android.settings.R;

/**
 * Parent class for settings fragments that contain a list of Bluetooth
 * devices.
 *
 * @see BluetoothSettings
 * @see DevicePickerFragment
 */
public abstract class DeviceListPreferenceFragment extends
        SettingsPreferenceFragment implements BluetoothCallback {

    private static final String TAG = "DeviceListPreferenceFragment";

    // / M: Bluetooth performance log TAG
    private static final String PERFORMANCE_TAG = "BtPerformanceTest";

    private static final String KEY_BT_DEVICE_LIST = "bt_device_list";
    private static final String KEY_BT_SCAN = "bt_scan";

    private BluetoothDeviceFilter.Filter mFilter;

    BluetoothDevice mSelectedDevice;

    LocalBluetoothAdapter mLocalAdapter;
    LocalBluetoothManager mLocalManager;

    public AuroraPreferenceGroup mDeviceListGroup;
    protected AuroraProgressPreference mProgressPreference;
	 // Aurora liugj 2013-10-24 added for aurora's new feature start
//    Button scanButton;
//    private ProgressBar scanBar;
	 // Aurora liugj 2013-10-24 added for aurora's new feature end

    final WeakHashMap<CachedBluetoothDevice, BluetoothDevicePreference> mDevicePreferenceMap =
            new WeakHashMap<CachedBluetoothDevice, BluetoothDevicePreference>();

    DeviceListPreferenceFragment() {
        mFilter = BluetoothDeviceFilter.ALL_FILTER;
    }

    final void setFilter(BluetoothDeviceFilter.Filter filter) {
        mFilter = filter;
    }

    final void setFilter(int filterType) {
        mFilter = BluetoothDeviceFilter.getFilter(filterType);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocalManager = LocalBluetoothManager.getInstance(getActivity());
        if (mLocalManager == null) {
            Log.e(TAG, "Bluetooth is not supported on this device");
            return;
        }
        mLocalAdapter = mLocalManager.getBluetoothAdapter();

		  // Aurora liugj 2013-10-24 added for aurora's new feature start        
//        ((AuroraActivity)getActivity()).getAuroraActionBar().addItem(R.layout.aurora_actionbar_scan, 0);
//        CustomAuroraActionBarItem item = (CustomAuroraActionBarItem) ((AuroraActivity) getActivity()).getAuroraActionBar().getItem(0);
//        View view = item.getItemView();
//        scanButton = (Button) view.findViewById(R.id.scan_btn);
//        scanBar = (ProgressBar) view.findViewById(R.id.scan_progressbar);
		  // Aurora liugj 2013-10-24 added for aurora's new feature end
        
        addPreferencesForActivity();

        mDeviceListGroup = (AuroraPreferenceCategory) findPreference(KEY_BT_DEVICE_LIST);
        mProgressPreference = (AuroraProgressPreference) findPreference(KEY_BT_SCAN);
        
    }

    void setDeviceListGroup(AuroraPreferenceGroup preferenceGroup) {
        mDeviceListGroup = preferenceGroup;
    }

    /** Add preferences from the subclass. */
    abstract void addPreferencesForActivity();

    @Override
    public void onResume() {
        super.onResume();
        if (mLocalManager == null) return;

        mLocalManager.setForegroundActivity(this);
        mLocalManager.getEventManager().registerCallback(this);

        updateProgressUi(mLocalAdapter.isDiscovering());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mLocalManager == null)
            return;
        // / M: Move clear screen form onPause() to onResume()
        mLocalAdapter.stopScanning();
        mLocalManager.setForegroundActivity(null);
        mLocalManager.getEventManager().unregisterCallback(this);
    }

    void removeAllDevices() {
        mDevicePreferenceMap.clear();
		// Aurora liugj 2013-10-22 modified for aurora's new feature start
//        mDeviceListGroup.removeAll();
		// Aurora liugj 2013-10-22 modified for aurora's new feature end
    }

    void addCachedDevices() {
        Collection<CachedBluetoothDevice> cachedDevices =
                mLocalManager.getCachedDeviceManager().getCachedDevicesCopy();
        for (CachedBluetoothDevice cachedDevice : cachedDevices) {
            onDeviceAdded(cachedDevice);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen,
            AuroraPreference preference) {
        if (KEY_BT_SCAN.equals(preference.getKey())) {
            // / M: call startScanning() with the param type 0, so we can search
            // BR/EDR device
        	Log.i("qy", "KEY_BT_SCAN.equals(preference.getKey())");
            int type = mLocalAdapter.getScanType(mFilter);            
            mLocalAdapter.startScanning(true, type);
            
            mProgressPreference.setProgress(true);
            return true;
        }

        if (preference instanceof BluetoothDevicePreference) {
            BluetoothDevicePreference btPreference = (BluetoothDevicePreference) preference;
            CachedBluetoothDevice device = btPreference.getCachedDevice();
            mSelectedDevice = device.getDevice();
            onDevicePreferenceClick(btPreference);
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    void onDevicePreferenceClick(BluetoothDevicePreference btPreference) {
        btPreference.onClicked();
    }

    public void onDeviceAdded(CachedBluetoothDevice cachedDevice) {
        Log.d(TAG, "onDeviceAdded, Device name is " + cachedDevice.getName());
        if (mDevicePreferenceMap.get(cachedDevice) != null) {
            Log.d(TAG, "Device name " + cachedDevice.getName() + "already have preference");
            return;
        }

        // Prevent updates while the list shows one of the state messages
        if (mLocalAdapter.getBluetoothState() != BluetoothAdapter.STATE_ON)
            return;

        if (mFilter.matches(cachedDevice.getDevice())) {
            Log.d(TAG, "Device name " + cachedDevice.getName() + "create new preference");
            createDevicePreference(cachedDevice);
        }
     }

    void createDevicePreference(CachedBluetoothDevice cachedDevice) {
        BluetoothDevicePreference preference = new BluetoothDevicePreference(
                getActivity(), cachedDevice);

        initDevicePreference(preference);
        mDeviceListGroup.addPreference(preference);
        mDevicePreferenceMap.put(cachedDevice, preference);
    }

    /**
     * Overridden in {@link BluetoothSettings} to add a listener.
     * 
     * @param preference
     *            the newly added preference
     */
    void initDevicePreference(BluetoothDevicePreference preference) {
        // Does nothing by default
    }

    public void onDeviceDeleted(CachedBluetoothDevice cachedDevice) {
        BluetoothDevicePreference preference = mDevicePreferenceMap
                .remove(cachedDevice);
        if (preference != null) {
            mDeviceListGroup.removePreference(preference);
        }
    }

    public void onScanningStateChanged(boolean started) {
        Log.d(TAG, "onScanningStateChanged " + started);
        updateProgressUi(started);
    }

    private void updateProgressUi(boolean start) {
        Log.d(TAG, "updateProgressUi " + start);
		// Aurora liugj 2013-10-24 modified for aurora's new feature start
        if (mDeviceListGroup instanceof AuroraPreferenceCategory) {
            //((AuroraPreferenceCategory) mDeviceListGroup).setProgress(start);
//        	scanBar.setVisibility(start ? View.VISIBLE : View.GONE);
//        	scanButton.setVisibility(start ? View.GONE : View.VISIBLE);
             Log.d(TAG, "setProgress " + start);
        }
		// Aurora liugj 2013-10-24 modified for aurora's new feature end
    }

    public void onBluetoothStateChanged(int bluetoothState) {
        // / M: when bluetooth state turn to STATE_TURNING_OFF, set progress
        // category disable @{
        if (bluetoothState == BluetoothAdapter.STATE_TURNING_OFF) {
            Log.d(TAG, "Turn off bt");
            updateProgressUi(false);
            // / @}
            // / M: Add for Bluetooth log @{
        } else if (bluetoothState == BluetoothAdapter.STATE_OFF) {
            long disableEndTime = System.currentTimeMillis();
            Log.d(PERFORMANCE_TAG,
                    "[Performance test][Settings][Bt] Bluetooth disable end ["
                            + disableEndTime + "]");
        } else if (bluetoothState == BluetoothAdapter.STATE_ON) {
            long enableEndTime = System.currentTimeMillis();
            Log.d(PERFORMANCE_TAG,
                    "[Performance test][Settings][Bt] Bluetooth enable end ["
                            + enableEndTime + "]");
        }
    }
}
