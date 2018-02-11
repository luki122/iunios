/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.aurora.callsetting;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.IBluetoothHeadsetPhone;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.media.AudioManager;
import android.net.Uri;
import android.net.sip.SipManager;
import android.os.AsyncResult;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.StatFs;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.telephony.ServiceState;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.internal.telephony.*;
import com.android.internal.telephony.cdma.CdmaConnection;
import com.android.internal.telephony.sip.SipPhone;
import com.google.android.collect.Maps;
import com.google.common.base.Objects;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import aurora.widget.*;
import aurora.app.*;
import gionee.os.storage.GnStorageManager;
import gionee.telephony.GnTelephonyManager;
import android.provider.CallLog;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.telecom.VideoProfile;
import com.android.internal.util.Preconditions;

import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
/**
 * Misc utilities for the Phone app.
 */
public class PhoneUtils {
    private static final String LOG_TAG = "PhoneUtils";
    private static final boolean DBG = (PhoneGlobals.DBG_LEVEL >= 2);

    // Do not check in with VDBG = true, since that may write PII to the system log.
    private static final boolean VDBG = true;
    
    static boolean isVoipSupported() {
        return false;
    }
    
    public static void setPreferredDataSubscription(int subscription) {
    }
    
    public static boolean isMtk() {
    	return false;
    }

    
   	public static String getNetworkTitle(OperatorInfo ni) {
   		if (!TextUtils.isEmpty(ni.getOperatorAlphaLong())) {
   			return ni.getOperatorAlphaLong();
   		} else if (!TextUtils.isEmpty(ni.getOperatorAlphaShort())) {
   			return ni.getOperatorAlphaShort();
   		} else {
   			return ni.getOperatorNumeric();
   		}
   	}
}
