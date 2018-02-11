/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
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

package com.android.mms.transaction;

import android.database.sqlite.SqliteWrapper;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.gionee.internal.telephony.GnPhone;
import com.android.mms.LogTag;

import android.net.NetworkUtils;
import gionee.provider.GnTelephony;

import android.text.TextUtils;
import android.util.Log;
import com.android.mms.MmsApp;


/**
 * Container of transaction settings. Instances of this class are contained
 * within Transaction instances to allow overriding of the default APN
 * settings or of the MMS Client.
 */
public class TransactionSettings {
    private static final String TAG = "TransactionSettings";
    private static final boolean DEBUG = true;
    private static final boolean LOCAL_LOGV = false;

    private String mServiceCenter;
    private String mProxyAddress;
    private int mProxyPort = -1;

    private static final String[] APN_PROJECTION = {
        GnTelephony.Carriers.TYPE,            // 0
        GnTelephony.Carriers.MMSC,            // 1
        GnTelephony.Carriers.MMSPROXY,        // 2
        GnTelephony.Carriers.MMSPORT          // 3
    };
    private static final int COLUMN_TYPE         = 0;
    private static final int COLUMN_MMSC         = 1;
    private static final int COLUMN_MMSPROXY     = 2;
    private static final int COLUMN_MMSPORT      = 3;

    /**
     * Constructor that uses the default settings of the MMS Client.
     *
     * @param context The context of the MMS Client
     */
    public TransactionSettings(Context context, String apnName) {
        String selection = TextUtils.isEmpty(apnName) ? null :
                GnTelephony.Carriers.APN + "='" + apnName.trim() + "'";

        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(),
                            Uri.withAppendedPath(GnTelephony.Carriers.CONTENT_URI, "current"),
                            APN_PROJECTION, selection, null, null);

        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "TransactionSettings looking for apn: " + selection + " returned: " +
                    (cursor ==null ? "null cursor" : (cursor.getCount() + " hits")));
        }

        if (cursor == null) {
            Log.e(TAG, "Apn is not found in Database!");
            return;
        }

        boolean sawValidApn = false;
        try {
            while (cursor.moveToNext() && TextUtils.isEmpty(mServiceCenter)) {
                // Read values from APN settings
                if (isValidApnType(cursor.getString(COLUMN_TYPE), GnPhone.APN_TYPE_MMS)) {
                    sawValidApn = true;
                    mServiceCenter = cursor.getString(COLUMN_MMSC) != null ? NetworkUtils.trimV4AddrZeros(cursor.getString(COLUMN_MMSC).trim()):null;
                    mProxyAddress = NetworkUtils.trimV4AddrZeros(cursor.getString(COLUMN_MMSPROXY));
                    if (isProxySet()) {
                        String portString = cursor.getString(COLUMN_MMSPORT);
                        try {
                            mProxyPort = Integer.parseInt(portString);
                        } catch (NumberFormatException e) {
                            if (TextUtils.isEmpty(portString)) {
                                Log.w(TAG, "mms port not set!");
                            } else {
                                Log.e(TAG, "Bad port number format: " + portString, e);
                            }
                        }
                    }
                }
            }
        } finally {
            cursor.close();
        }

        if (sawValidApn && TextUtils.isEmpty(mServiceCenter)) {
            Log.e(TAG, "Invalid APN setting: MMSC is empty");
        }
    }

    // add for gemini
    /**
     * Constructor that uses the default settings of the MMS Client.
     *
     * @param context The context of the MMS Client
     */
    public TransactionSettings(Context context, String apnName, int slotId) {
        String selection = (apnName != null)?
                GnTelephony.Carriers.APN + "='" + apnName.trim() + "'": null;
        Cursor cursor = null;
        if (GnPhone.GEMINI_SIM_1 == slotId) {
            cursor = SqliteWrapper.query(context, context.getContentResolver(),
                                Uri.withAppendedPath(GnTelephony.Carriers.SIM1Carriers.CONTENT_URI, "current"),
                                APN_PROJECTION, selection, null, null);
        } else if (GnPhone.GEMINI_SIM_2 == slotId){
            cursor = SqliteWrapper.query(context, context.getContentResolver(),
                                Uri.withAppendedPath(GnTelephony.Carriers.SIM2Carriers.CONTENT_URI, "current"),
                                APN_PROJECTION, selection, null, null);
        } else {
            Log.e(MmsApp.TXN_TAG, "Invalide slot id:" + slotId);
        }

        if (cursor == null) {
            Log.e(MmsApp.TXN_TAG, "Apn is not found in Database!");
            return;
        }

        boolean sawValidApn = false;
        try {
            while (cursor.moveToNext() && TextUtils.isEmpty(mServiceCenter)) {
                // Read values from APN settings
                if (isValidApnType(cursor.getString(COLUMN_TYPE), GnPhone.APN_TYPE_MMS)) {
                    sawValidApn = true;
                    mServiceCenter = cursor.getString(COLUMN_MMSC) != null ? cursor.getString(COLUMN_MMSC).trim():null;
                    Log.d(MmsApp.TXN_TAG, "Service Center=" + mServiceCenter);
                    mProxyAddress = cursor.getString(COLUMN_MMSPROXY);
                    Log.d(MmsApp.TXN_TAG, "Proxy=" + mProxyAddress);
                    if (isProxySet()) {
                        String portString = cursor.getString(COLUMN_MMSPORT);
                        Log.d(MmsApp.TXN_TAG, "Port=" + portString);
                        try {
                            mProxyPort = Integer.parseInt(portString);
                        } catch (NumberFormatException e) {
                            if (TextUtils.isEmpty(portString)) {
                                Log.w(TAG, "mms port not set!");
                            } else {
                                Log.e(TAG, "Bad port number format: " + portString, e);
                            }
                        }
                    }
                }
            }
        } finally {
            cursor.close();
        }

        Log.v(TAG, "APN setting: MMSC: " + mServiceCenter + " looked for: " + selection);

        if (sawValidApn && TextUtils.isEmpty(mServiceCenter)) {
            Log.e(TAG, "Invalid APN setting: MMSC is empty");
        }
    }

    /**
     * Constructor that overrides the default settings of the MMS Client.
     *
     * @param mmscUrl The MMSC URL
     * @param proxyAddr The proxy address
     * @param proxyPort The port used by the proxy address
     * immediately start a SendTransaction upon completion of a NotificationTransaction,
     * false otherwise.
     */
    public TransactionSettings(String mmscUrl, String proxyAddr, int proxyPort) {
        mServiceCenter = mmscUrl != null ? mmscUrl.trim() : null;
        mProxyAddress = proxyAddr;
        mProxyPort = proxyPort;

        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "TransactionSettings: " + mServiceCenter +
                    " proxyAddress: " + mProxyAddress +
                    " proxyPort: " + mProxyPort);
        }
   }

    public String getMmscUrl() {
        return mServiceCenter;
    }

    public String getProxyAddress() {
        return mProxyAddress;
    }

    public int getProxyPort() {
        return mProxyPort;
    }

    public boolean isProxySet() {
        return (mProxyAddress != null) && (mProxyAddress.trim().length() != 0);
    }

    static private boolean isValidApnType(String types, String requestType) {
        // If APN type is unspecified, assume APN_TYPE_ALL.
        if (TextUtils.isEmpty(types)) {
            return true;
        }

        for (String t : types.split(",")) {
            if (t.equals(requestType) || t.equals(GnPhone.APN_TYPE_ALL)) {
                return true;
            }
        }
        return false;
    }
}
