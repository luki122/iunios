/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
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

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import com.android.settings.AuroraSettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.SettingsActivity;

import java.util.HashMap;

import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceGroup;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraSwitchPreference;


/**
 * This preference fragment presents the user with all of the profiles
 * for a particular device, and allows them to be individually connected
 * (or disconnected).
 */
public final class AuroraDeviceProfilesSettings extends AuroraSettingsPreferenceFragment
        implements CachedBluetoothDevice.Callback, AuroraPreference.OnPreferenceChangeListener {
    private static final String TAG = "AuroraDeviceProfilesSettings";

    private static final String KEY_PROFILE_CONTAINER = "profile_container";
    private static final String KEY_UNPAIR = "unpair";
    private static final String KEY_PBAP_SERVER = "PBAP Server";

    private CachedBluetoothDevice mCachedDevice;
    private LocalBluetoothManager mManager;
    private LocalBluetoothProfileManager mProfileManager;

    private static final int BLUETOOTH_RENAME_REQUEST_RESULT = 0;
    private static final int OK_BUTTON = -1;

    private AuroraPreferenceGroup mProfileContainer;
    private AuroraPreference mDeviceNamePref;

    private final HashMap<LocalBluetoothProfile, AuroraSwitchPreference> mAutoConnectPrefs
            = new HashMap<LocalBluetoothProfile, AuroraSwitchPreference>();

    private AlertDialog mDisconnectDialog;
    private boolean mProfileGroupIsRemoved;

    private static final String KEY_RENAME_DEVICE = "rename_device";
    public static final String EXTRA_DEVICE = "device";
    private BluetoothDevice device;

    public AuroraDeviceProfilesSettings(BluetoothDevice devicename){
        device = devicename;
    }

    public AuroraDeviceProfilesSettings(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         Log.d(TAG, "onCreate");

        if (savedInstanceState != null) {
            device = savedInstanceState.getParcelable(EXTRA_DEVICE);
        } else {
            Bundle args = getArguments();
            device = args.getParcelable(EXTRA_DEVICE);
        }

        if (device == null) {
            Log.w(TAG, "AuroraActivity started without a remote Bluetooth device");
            finish();
            return;  // TODO: test this failure path
        }

        addPreferencesFromResource(R.xml.aurora_bluetooth_device_advanced);
//        addPreferencesFromResource(R.xml.gn_bluetooth_device_advanced);
//        addPreferencesFromResource(R.xml.bluetooth_device_advanced);
        getPreferenceScreen().setOrderingAsAdded(false);
        mProfileContainer = (AuroraPreferenceGroup) findPreference(KEY_PROFILE_CONTAINER);
        //mProfileContainer.setLayoutResource(R.layout.bluetooth_preference_category);

        mManager = LocalBluetoothManager.getInstance(getActivity());
        CachedBluetoothDeviceManager deviceManager =
                mManager.getCachedDeviceManager();
        mProfileManager = mManager.getProfileManager();
        mDeviceNamePref = (AuroraPreference) findPreference(KEY_RENAME_DEVICE);
        mCachedDevice = deviceManager.findDevice(device);
        if (mCachedDevice == null) {
            Log.w(TAG, "Device not found, cannot connect to it");
            finish();
            return;  // TODO: test this failure path
        }

        String deviceName = mCachedDevice.getName();
        mDeviceNamePref.auroraSetArrowText(deviceName,true);
        mDeviceNamePref.setOnPreferenceClickListener(new AuroraPreference.OnPreferenceClickListener() {
                                                         @Override
                                                         public boolean onPreferenceClick(AuroraPreference preference) {
                                                             Bundle bundle = new Bundle();
                                                             bundle.putString("device_name", mCachedDevice.getName());
                                                             bundle.putParcelable(AuroraDeviceProfilesSettings.EXTRA_DEVICE, device);
                                                             ((SettingsActivity) getActivity()).startPreferencePanel(
                                                                     AuroraBluetoothPairedDeviceNameFragment.class.getCanonicalName(), bundle,
                                                                     R.string.bluetooth_device_advanced_rename_device, null, null, BLUETOOTH_RENAME_REQUEST_RESULT);
                                                             return true;
                                                         }
                                                     }
        );

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        if (mDisconnectDialog != null) {
            mDisconnectDialog.dismiss();
            mDisconnectDialog = null;
        }
        //if (mCachedDevice != null) {
        //   mCachedDevice.unregisterCallback(this);
        //}
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        mManager.setForegroundActivity(getActivity());
        if (mCachedDevice != null) {
            if(mDeviceNamePref != null) {
                String deviceName = mCachedDevice.getName();
                mDeviceNamePref.auroraSetArrowText(deviceName);
            }
            mCachedDevice.registerCallback(this);
            Log.d(TAG, "onResume, registerCallback");
            if (mCachedDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                finish();
                return;
            }
            refresh();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");

        if (mCachedDevice != null) {
            mCachedDevice.unregisterCallback(this);
            Log.d(TAG, "onPause, unregisterCallback");
        }

        mManager.setForegroundActivity(null);
    }

    public void setDevice(CachedBluetoothDevice cachedDevice) {
        mCachedDevice = cachedDevice;
        Log.d(TAG, "setDevice : " + cachedDevice);

        if (isResumed()) {
            Log.d(TAG, "setDevice, registerCallback & refresh");
            mCachedDevice.registerCallback(this);
            addPreferencesForProfiles();
            refresh();
        }
    }

    private void addPreferencesForProfiles() {
        mProfileContainer.removeAll();
        for (LocalBluetoothProfile profile : mCachedDevice.getConnectableProfiles()) {
            AuroraPreference pref = createProfilePreference(profile);
            mProfileContainer.addPreference(pref);
        }

        final int pbapPermission = mCachedDevice.getPhonebookPermissionChoice();
        // Only provide PBAP cabability if the client device has requested PBAP.
        if (pbapPermission != CachedBluetoothDevice.ACCESS_UNKNOWN) {
            final PbapServerProfile psp = mManager.getProfileManager().getPbapProfile();
            AuroraSwitchPreference pbapPref = createProfilePreference(psp);
            mProfileContainer.addPreference(pbapPref);
        }

        final MapProfile mapProfile = mManager.getProfileManager().getMapProfile();
        final int mapPermission = mCachedDevice.getMessagePermissionChoice();
        if (mapPermission != CachedBluetoothDevice.ACCESS_UNKNOWN) {
            AuroraSwitchPreference mapPreference = createProfilePreference(mapProfile);
            mProfileContainer.addPreference(mapPreference);
        }

        showOrHideProfileGroup();
    }

    private void showOrHideProfileGroup() {
        int numProfiles = mProfileContainer.getPreferenceCount();
        if (!mProfileGroupIsRemoved && numProfiles == 0) {
            getPreferenceScreen().removePreference(mProfileContainer);
            mProfileGroupIsRemoved = true;
        } else if (mProfileGroupIsRemoved && numProfiles != 0) {
            getPreferenceScreen().addPreference(mProfileContainer);
            mProfileGroupIsRemoved = false;
        }
    }

    /**
     * Creates a checkbox preference for the particular profile. The key will be
     * the profile's name.
     *
     * @param profile The profile for which the preference controls.
     * @return A preference that allows the user to choose whether this profile
     *         will be connected to.
     */
    private AuroraSwitchPreference createProfilePreference(LocalBluetoothProfile profile) {
        AuroraSwitchPreference pref = new AuroraSwitchPreference(getActivity());
        //pref.setLayoutResource(R.layout.aurora_preference_start_widget);
        pref.setKey(profile.toString());
        pref.setTitle(profile.getNameResource(mCachedDevice.getDevice()));
        pref.setPersistent(false);
        pref.setOrder(getProfilePreferenceIndex(profile.getOrdinal()));
        pref.setOnPreferenceChangeListener(this);
/*
        int iconResource = profile.getDrawableResource(mCachedDevice.getBtClass());
        if (iconResource != 0) {
            pref.setIcon(getResources().getDrawable(iconResource));
        }
*/
        refreshProfilePreference(pref, profile);

        return pref;
    }

    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen screen, AuroraPreference preference) {
        String key = preference.getKey();
        if (key.equals(KEY_UNPAIR)) {
            mCachedDevice.unpair();
            Log.d(TAG, "onPreferenceTreeClick unpairDevice ");
            finish();
            return true;
        }

        return super.onPreferenceTreeClick(screen, preference);
    }
    public boolean onPreferenceChange(AuroraPreference preference, Object newValue) {
        if (preference instanceof AuroraSwitchPreference) {
            LocalBluetoothProfile prof = getProfileOf(preference);
            onProfileClicked(prof, (AuroraSwitchPreference) preference);
            return false;   // checkbox will update from onDeviceAttributesChanged() callback
        } else {
            return false;
        }
    }

    private void onProfileClicked(LocalBluetoothProfile profile, AuroraSwitchPreference profilePref) {
        BluetoothDevice device = mCachedDevice.getDevice();

        if (profilePref.getKey().equals(KEY_PBAP_SERVER)) {
            final int newPermission = mCachedDevice.getPhonebookPermissionChoice()
                == CachedBluetoothDevice.ACCESS_ALLOWED ? CachedBluetoothDevice.ACCESS_REJECTED
                : CachedBluetoothDevice.ACCESS_ALLOWED;
            mCachedDevice.setPhonebookPermissionChoice(newPermission);
            profilePref.setChecked(newPermission == CachedBluetoothDevice.ACCESS_ALLOWED);
            return;
        }

        int status = profile.getConnectionStatus(device);
        boolean isConnected =
                status == BluetoothProfile.STATE_CONNECTED;
        Log.d(TAG, "isConnected : " + isConnected);

        ///M: Profile may not connected, do not ask disconnect.
        if (isConnected || (profile instanceof MapProfile && profilePref.isChecked())) {
            askDisconnect(mManager.getForegroundActivity(), profile);
        } else {
            if (profile instanceof MapProfile) {
                mCachedDevice.setMessagePermissionChoice(BluetoothDevice.ACCESS_ALLOWED);
                refreshProfilePreference(profilePref, profile);
            }
            Log.d(TAG, mCachedDevice.getName() + " " + profile.toString() + " isPreferred() : " + profile.isPreferred(device));
            if (profile.isPreferred(device)) {
                // profile is preferred but not connected: disable auto-connect
                if (profile instanceof PanProfile) {
                    mCachedDevice.connectProfile(profile);
                } else {
                profile.setPreferred(device, false);
                Log.d(TAG, profile.toString() + " setPreferred false");
                refreshProfilePreference(profilePref, profile);
                }
            } else {
                profile.setPreferred(device, true);
                Log.d(TAG, profile.toString() + " setPreferred true and connect profile");
                mCachedDevice.connectProfile(profile);
            }
        }
    }

    private void askDisconnect(Context context,
            final LocalBluetoothProfile profile) {
        // local reference for callback
        final CachedBluetoothDevice device = mCachedDevice;
        String name = device.getName();
        if (TextUtils.isEmpty(name)) {
            name = context.getString(R.string.bluetooth_device);
        }

        String profileName = context.getString(profile.getNameResource(device.getDevice()));

        String title = context.getString(R.string.bluetooth_disable_profile_title);
        String message = context.getString(R.string.bluetooth_disable_profile_message,
                profileName, name);

        DialogInterface.OnClickListener disconnectListener =
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Disconnect only when user has selected OK
                if (which == OK_BUTTON) {
                    device.disconnect(profile);
                    profile.setPreferred(device.getDevice(), false);
                    if (profile instanceof MapProfile) {
                        device.setMessagePermissionChoice(BluetoothDevice.ACCESS_REJECTED);
                    }
                    refreshProfilePreference((AuroraSwitchPreference)findPreference(profile.toString()), profile);
                }
            }
        };

        mDisconnectDialog = Utils.showDisconnectDialog(context,
                mDisconnectDialog, disconnectListener, title, Html.fromHtml(message));
    }

    @Override
    public void onDeviceAttributesChanged() {
        refresh();
    }

    private void refresh() {
        ///M:
        if (getView() == null) {
            Log.e(TAG, "getView() is null, just skip");
            return;
        }

        refreshProfiles();
    }

    private void refreshProfiles() {
        for (LocalBluetoothProfile profile : mCachedDevice.getConnectableProfiles()) {
            AuroraSwitchPreference profilePref = (AuroraSwitchPreference)findPreference(profile.toString());
            if (profilePref == null) {
                profilePref = createProfilePreference(profile);
                mProfileContainer.addPreference(profilePref);
            } else {
                refreshProfilePreference(profilePref, profile);
            }
        }
        for (LocalBluetoothProfile profile : mCachedDevice.getRemovedProfiles()) {
            AuroraPreference profilePref = findPreference(profile.toString());
            if (profilePref != null) {
                Log.d(TAG, "Removing " + profile.toString() + " from profile list");
                mProfileContainer.removePreference(profilePref);
            }
        }

        showOrHideProfileGroup();
    }

    private void refreshProfilePreference(AuroraSwitchPreference profilePref,
            LocalBluetoothProfile profile) {
        BluetoothDevice device = mCachedDevice.getDevice();

        // Gray out checkbox while connecting and disconnecting.
        Log.d(TAG, "isBusy : " + mCachedDevice.isBusy());
        profilePref.setEnabled(!mCachedDevice.isBusy());

        if (profile instanceof MapProfile) {
            profilePref.setChecked(mCachedDevice.getMessagePermissionChoice()
                    == CachedBluetoothDevice.ACCESS_ALLOWED);
        } else if (profile instanceof PbapServerProfile) {
            // Handle PBAP specially.
            profilePref.setChecked(mCachedDevice.getPhonebookPermissionChoice()
                    == CachedBluetoothDevice.ACCESS_ALLOWED);
        }
        if (profile instanceof PanProfile) {
            profilePref.setChecked(profile.getConnectionStatus(device) ==
                    BluetoothProfile.STATE_CONNECTED);
        }
        else {
            Log.d(TAG, profile.toString() + " isPreferred : " + profile.isPreferred(device));
            profilePref.setChecked(profile.isPreferred(device));
        }
    }

    private LocalBluetoothProfile getProfileOf(AuroraPreference pref) {
        if (!(pref instanceof AuroraSwitchPreference)) {
            return null;
        }
        String key = pref.getKey();
        if (TextUtils.isEmpty(key)) return null;

        try {
            return mProfileManager.getProfileByName(pref.getKey());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private int getProfilePreferenceIndex(int profIndex) {
        return mProfileContainer.getOrder() + profIndex * 10;
    }
}
