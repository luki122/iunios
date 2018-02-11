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
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothFtp.Client;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothUuid;
//import android.bluetooth.BluetoothProfileManager;
//import android.bluetooth.BluetoothProfileManager.Profile;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import com.android.settings.R;

import com.aurora.featureoption.FeatureOption;

/**
 * FtpProfile handles Bluetooth FTP.
 * TODO: add null checks around calls to mService object.
 */
final class FtpProfile implements LocalBluetoothProfile {
    private static final String TAG = "FtpProfile";

//    static final ParcelUuid[] UUIDS = new ParcelUuid[] {
//        BluetoothUuid.ObexFileTransfer
//    };
    static final String NAME = "FTP";

    // Order of this profile in device profiles list
    private static final int ORDINAL = 5;
    
//    private static BluetoothProfileManager mService;

    public enum Profile{
        Bluetooth_HEADSET(0),
        Bluetooth_A2DP(1),
        Bluetooth_HID(2),
        Bluetooth_FTP_Client(3),
        Bluetooth_FTP_Server(4),
        Bluetooth_BIP_Initiator(5),
        Bluetooth_BIP_Responder(6),
        Bluetooth_BPP_Sender(7),
        Bluetooth_SIMAP(8),
        Bluetooth_PBAP(9),
        Bluetooth_OPP_Server(10),
		Bluetooth_OPP_Client(11),
		Bluetooth_DUN(12),
		Bluetooth_AVRCP(13),
		Bluetooth_PRXM(14),
		Bluetooth_PRXR(15),
		Bluetooth_PAN_NAP(16),
		Bluetooth_PAN_GN(17),
		Bluetooth_MAP_Server(18);
		
		 public final int localizedString;
		 private Profile(int localizedString) {
            this.localizedString = localizedString;
        }	
	}

//    private static Client mFtpClient;
    
    private static Profile profile = Profile.Bluetooth_FTP_Client;

    public static final int STATE_ACTIVE = 0;
    public static final int STATE_CONNECTED = 1;
    public static final int STATE_DISCONNECTED = 2;
    public static final int STATE_CONNECTING = 3;
    public static final int STATE_DISCONNECTING = 4;
    public static final int STATE_UNKNOWN = 5;
    public static final int STATE_ENABLING = 10;
    public static final int STATE_ENABLED = 11;
    public static final int STATE_DISABLING = 12;
    public static final int STATE_DISABLED = 13;
    public static final int STATE_ABNORMAL = 14;

    FtpProfile(Context context) {
        Log.d(TAG, "[BT][FTP] Constructor of FtpProfile in Settings.");
//        if (mFtpClient == null) {
//            mFtpClient = new Client(context);
//        }
    }

    public boolean isConnectable() {
		return FeatureOption.MTK_BT_PROFILE_FTP;
    }

    public boolean isAutoConnectable() {
        return false;
    }

    public boolean connect(BluetoothDevice device) {
        return false;
//        return mFtpClient.connect(device);
    }

    public boolean disconnect(BluetoothDevice device) {
        return false;
//        return mFtpClient.disconnect(device);
    }

    public int getConnectionStatus(BluetoothDevice device) {
        int state = STATE_UNKNOWN;
//        int state = mFtpClient.getState(device);
        if (state < STATE_ACTIVE || state > STATE_UNKNOWN) {
            state = STATE_UNKNOWN;
        } 
        //convert BluetoothProfileManager state to BluetoothProfile State
        switch(state) {
            case STATE_ACTIVE :
            case STATE_CONNECTED : state = BluetoothProfile.STATE_CONNECTED; break;
            case STATE_DISCONNECTING : state = BluetoothProfile.STATE_DISCONNECTING; break;
            case STATE_CONNECTING : state = BluetoothProfile.STATE_CONNECTING; break;
            case STATE_DISCONNECTED : state = BluetoothProfile.STATE_DISCONNECTED; break;
        };
        // Log.d(TAG, "[BT][FTP] getConnectionStatus(), Device: " + device + " state: " + state);
        return state;
    }

    public boolean isPreferred(BluetoothDevice device) {
        int state = getConnectionStatus(device);
        return (state == BluetoothProfile.STATE_CONNECTING || state == BluetoothProfile.STATE_CONNECTED);
    }

    public int getPreferred(BluetoothDevice device) {
        return -1;
    }

    public void setPreferred(BluetoothDevice device, boolean preferred) {
    }

    public boolean isProfileReady() {
//        return mService != null;
        return false;
    }

    public String toString() {
        return NAME;
    }

    public int getOrdinal() {
        return ORDINAL;
    }

    public int getNameResource(BluetoothDevice device) {
        return R.string.bluetooth_profile_ftp;
    }

    public int getSummaryResourceForDevice(BluetoothDevice device) {
        int state = getConnectionStatus(device);
        if (state == BluetoothProfile.STATE_CONNECTED) {
            return R.string.bluetooth_ftp_profile_summary_connected;
        } else {
            return R.string.bluetooth_ftp_profile_summary_use_for;
        }
    }

    public int getDrawableResource(BluetoothClass btClass) {
        return R.drawable.ic_bt_transmit_ftp;
    }
}
