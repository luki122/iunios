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

import aurora.app.AuroraAlertDialog;
import android.widget.EditText;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import aurora.preference.AuroraSwitchPreference;
import aurora.preference.AuroraEditTextPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceGroup;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraPreferenceActivity;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import aurora.widget.AuroraEditText;
import android.text.TextWatcher;
import android.app.Dialog;
import android.widget.Button;
import aurora.widget.AuroraButton;
import android.text.Editable;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import android.widget.Toast;

import java.util.HashMap;
import android.bluetooth.BluetoothAdapter;

/**
 * This preference fragment presents the user with all of the profiles
 * for a particular device, and allows them to be individually connected
 * (or disconnected).
 */
// Aurora liugj 2013-10-22 modified for aurora's new feature 
public final class DeviceProfilesSettings extends SettingsPreferenceFragment
        implements CachedBluetoothDevice.Callback, AuroraPreference.OnPreferenceChangeListener {
    private static final String TAG = "DeviceProfilesSettings";
    
    private static final int BLUETOOTH_RENAME_REQUEST_RESULT = 0;
    /// M: the max length that bt hardware layer can support bt name
    private static final int BLUETOOTH_NAME_MAX_LENGTH_BYTES = 59;
    
    private static final String KEY_RENAME_DEVICE = "rename_device";
    private static final String KEY_PROFILE_CONTAINER = "profile_container";
    private static final String KEY_UNPAIR = "unpair";

    public static final String EXTRA_DEVICE = "device";
//    private RenameEditTextPreference mRenameDeviceNamePref;
    private LocalBluetoothManager mManager;
    private CachedBluetoothDevice mCachedDevice;
    private CachedBluetoothDeviceManager mDeviceManager;
    private LocalBluetoothProfileManager mProfileManager;

    private static final int OK_BUTTON = -1;
	
    private AuroraPreferenceGroup mProfileContainer;
    private AuroraPreferenceScreen mDeviceNamePref;

    private final HashMap<LocalBluetoothProfile, AuroraSwitchPreference> mAutoConnectPrefs
            = new HashMap<LocalBluetoothProfile, AuroraSwitchPreference>();

    private AuroraAlertDialog mDisconnectDialog;
    private boolean mProfileGroupIsRemoved;
    private BluetoothDevice device;
    private boolean mIsChecked = true;

    /*private class RenameEditTextPreference implements TextWatcher{
        public void afterTextChanged(Editable s) {
            Dialog d = mDeviceNamePref.getDialog();
            if (d instanceof AuroraAlertDialog) {
                ((AuroraAlertDialog) d).getButton(AuroraAlertDialog.BUTTON_POSITIVE).setEnabled(s.length() > 0);
            }
        }

        // TextWatcher interface
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // not used
        }

        // TextWatcher interface
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // not used
        }
    }*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            device = savedInstanceState.getParcelable(EXTRA_DEVICE);
        } else {
            Bundle args = getArguments();
            device = args.getParcelable(EXTRA_DEVICE);
        }

        addPreferencesFromResource(R.xml.bluetooth_device_advanced);
        getPreferenceScreen().setOrderingAsAdded(false);
        mProfileContainer = (AuroraPreferenceGroup) findPreference(KEY_PROFILE_CONTAINER);
        mDeviceNamePref = (AuroraPreferenceScreen) findPreference(KEY_RENAME_DEVICE);

        if (device == null) {
            Log.w(TAG, "AuroraActivity started without a remote Bluetooth device");
            finish();
            return;  // TODO: test this failure path
        }
//        mRenameDeviceNamePref = new RenameEditTextPreference();
        mManager = LocalBluetoothManager.getInstance(getActivity());
        mDeviceManager = mManager.getCachedDeviceManager();
        mProfileManager = mManager.getProfileManager();
        mCachedDevice = mDeviceManager.findDevice(device);
        if (mCachedDevice == null) {
            Log.w(TAG, "Device not found, cannot connect to it");
            finish();
            return;  // TODO: test this failure path
        }

        String deviceName = mCachedDevice.getName();
//        mDeviceNamePref.setSummary(deviceName);
//        mDeviceNamePref.setText(deviceName);
        mDeviceNamePref.setOnPreferenceChangeListener(this);

        // Add a preference for each profile
        addPreferencesForProfiles();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDisconnectDialog != null) {
            mDisconnectDialog.dismiss();
            mDisconnectDialog = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_DEVICE, mCachedDevice.getDevice());
    }

    @Override
    public void onResume() {
        super.onResume();

        mManager.setForegroundActivity(this);
        mCachedDevice.registerCallback(this);
        if(mCachedDevice.getBondState() == BluetoothDevice.BOND_NONE)
            finish();
        refresh();
        /*EditText et = mDeviceNamePref.getEditText();
        if (et != null) {
            /// M: set the paierd device name max length is 59
            et.setFilters(new InputFilter[] {
                    new Utf8ByteLengthFilter(BLUETOOTH_NAME_MAX_LENGTH_BYTES)
            });
            et.addTextChangedListener(mRenameDeviceNamePref);
            Dialog d = mDeviceNamePref.getDialog();
            if (d instanceof AuroraAlertDialog) {
                AuroraButton b = ((AuroraAlertDialog) d).getButton(AuroraAlertDialog.BUTTON_POSITIVE);
                b.setEnabled(et.getText().length() > 0);
            }
        }*/
    }

    @Override
    public void onPause() {
        super.onPause();

        mCachedDevice.unregisterCallback(this);
        mManager.setForegroundActivity(null);
    }

    private void addPreferencesForProfiles() {
        for (LocalBluetoothProfile profile : mCachedDevice.getConnectableProfiles()) {
            AuroraPreference pref = createProfilePreference(profile);
            mProfileContainer.addPreference(pref);
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
        pref.setKey(profile.toString());
        pref.setTitle(profile.getNameResource(mCachedDevice.getDevice()));
        pref.setPersistent(false);
        pref.setOrder(getProfilePreferenceIndex(profile.getOrdinal()));
        pref.setOnPreferenceChangeListener(this);

        int iconResource = profile.getDrawableResource(mCachedDevice.getBtClass());
        if (iconResource != 0) {
			  // Aurora liugj 2013-10-24 deleted for aurora's new feature start
            //pref.setIcon(getResources().getDrawable(iconResource));
			  // Aurora liugj 2013-10-24 deleted for aurora's new feature end
        }

        /**
         * Gray out profile while connecting and disconnecting
         */
        pref.setEnabled(!mCachedDevice.isBusy());

        refreshProfilePreference(pref, profile);

        return pref;
    }

    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen screen, AuroraPreference preference) {
        String key = preference.getKey();
        if (key.equals(KEY_UNPAIR)) {
            String message;
            String name = mCachedDevice.getName();
            if (TextUtils.isEmpty(name)) {
               name = getString(R.string.bluetooth_device);
            }

            if (unpairDevice()) {
				//wolfu solve BUG #4483
                Log.d(TAG, "onPreferenceTreeClick unpairDevice ");
            	mDeviceManager.onBluetoothStateChanged(BluetoothAdapter.STATE_TURNING_OFF);   
                finish();
                message = getString(R.string.bluetooth_unpair_toast_ok, name);
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            } else {
                message = getString(R.string.bluetooth_unpair_toast_fail, name);
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
            return true;
        }else if (key.equals(KEY_RENAME_DEVICE)) {
        	Bundle bundle = new Bundle();
        	bundle.putString("device_name", mCachedDevice.getName());
        	bundle.putParcelable(DeviceProfilesSettings.EXTRA_DEVICE, device);
        	((AuroraPreferenceActivity) getActivity()).startPreferencePanel(
					BluetoothPairedDeviceNameFragment.class.getCanonicalName(), bundle,
					R.string.bluetooth_device_advanced_rename_device, null, null, BLUETOOTH_RENAME_REQUEST_RESULT);
		}

        return super.onPreferenceTreeClick(screen, preference);
    }
    
    public boolean onPreferenceChange(AuroraPreference preference, Object newValue) {
        if (preference == mDeviceNamePref) {
            mCachedDevice.setName((String) newValue);
        } else if (preference instanceof AuroraSwitchPreference) {
        	// qy modify
        	mIsChecked = (Boolean)newValue;
        	
                Log.d(TAG, "onPreferenceChange newValue " +mIsChecked);
            LocalBluetoothProfile prof = getProfileOf(preference);
            onProfileClicked(prof, (AuroraSwitchPreference) preference);
            return true;   // qy false checkbox will update from onDeviceAttributesChanged() callback
        } else {
            return false;
        }

        return true;
    }

    private void onProfileClicked(LocalBluetoothProfile profile, AuroraSwitchPreference profilePref) {
        BluetoothDevice device = mCachedDevice.getDevice();

        int status = profile.getConnectionStatus(device);
        boolean isConnected = status == BluetoothProfile.STATE_CONNECTED;

                Log.d(TAG, "onProfileClicked isConnected " +isConnected);
        if (isConnected){
            if(!mIsChecked)
            askDisconnect(getActivity(), profile);
        } else if(mIsChecked)
        {
            if (profile.isPreferred(device)) {
                // profile is preferred but not connected: disable auto-connect
                if (profile instanceof PanProfile) {
                    mCachedDevice.connectProfile(profile);
                } else 
                {
                    profile.setPreferred(device, false);
                    refreshProfilePreference(profilePref, profile);
                }
            } else {
                profile.setPreferred(device, true);
                mCachedDevice.connectProfile(profile);
            }
        }
    }

    private AuroraAlertDialog showDisconnectDialog(Context context,
            AuroraAlertDialog dialog,
            DialogInterface.OnClickListener disconnectListener,
            CharSequence title, CharSequence message) {
        if (dialog == null) {
            dialog = new AuroraAlertDialog.Builder(context)
                    .setPositiveButton(android.R.string.ok, disconnectListener)
                    .setNegativeButton(android.R.string.cancel, disconnectListener)
                    .create();
        } else {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            // use disconnectListener for the correct profile(s)
            CharSequence okText = context.getText(android.R.string.ok);
            dialog.setButton(DialogInterface.BUTTON_POSITIVE,
                   okText, disconnectListener);
            CharSequence cancelText = context.getText(android.R.string.cancel);
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                    cancelText, disconnectListener);
        }
        dialog.setCanceledOnTouchOutside(false);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.show();
        return dialog;
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
                // wolfu add Disconnect only when user has selected OK
                if (which == OK_BUTTON) {
                    device.disconnect(profile);
                    profile.setPreferred(device.getDevice(), false);
                }
                refresh();
            }
        };

        mDisconnectDialog = showDisconnectDialog(context,
                mDisconnectDialog, disconnectListener, title, Html.fromHtml(message));
    }

    public void onDeviceAttributesChanged() {
        refresh();
    }

    private void refresh() {
        String deviceName = mCachedDevice.getName();
		  // Aurora liugj 2013-10-24 deleted for aurora's new feature start
        //mDeviceNamePref.setSummary(deviceName);
		  // Aurora liugj 2013-10-24 deleted for aurora's new feature end
        mDeviceNamePref.auroraSetArrowText(deviceName);
//        mDeviceNamePref.setText(deviceName);

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

        /*
         * Gray out checkbox while connecting and disconnecting
         */
        profilePref.setEnabled(!mCachedDevice.isBusy());
        if(profile.getConnectionStatus(device) == BluetoothProfile.STATE_CONNECTED)
           profilePref.setChecked(true);
        else if(profile.getConnectionStatus(device) == BluetoothProfile.STATE_DISCONNECTED )
           profilePref.setChecked(false);

//        profilePref.setChecked(profile.getConnectionStatus(device) == BluetoothProfile.STATE_CONNECTED);
 //           profilePref.setChecked(profile.isPreferred(device));
        profilePref.setSummary(profile.getSummaryResourceForDevice(device));
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

    private boolean unpairDevice() {
        return mCachedDevice.unpair();
    }
}
