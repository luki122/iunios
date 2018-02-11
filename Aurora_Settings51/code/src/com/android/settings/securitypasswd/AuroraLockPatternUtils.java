/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.settings.securitypasswd;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;

import com.android.internal.widget.ILockSettings;
import com.android.internal.widget.LockPatternUtils;
import com.mediatek.settings.sim.Log;
//import com.android.internal.widget.LockPatternUtilsCache;

/**
 * Utilities for the lock pattern and its settings.
 */
public class AuroraLockPatternUtils extends LockPatternUtils{

    private static final String TAG = "AuroraLockPatternUtils";


    private ILockSettings mGnLockSettingsService;


    private static volatile int sCurrentUserId = UserHandle.USER_NULL;

    public AuroraLockPatternUtils(Context context) {
         super(context);
    }

    private ILockSettings getGnLockSettings() {
        if (mGnLockSettingsService == null) {
            mGnLockSettingsService = ILockSettings.Stub.asInterface(ServiceManager.getService("lock_settings")); 
/*            LockPatternUtilsCache.getInstance(
                    ILockSettings.Stub.asInterface(ServiceManager.getService("lock_settings")));*/
        }
        return mGnLockSettingsService;
    }
    private long gnGetLong(String secureSettingKey, long defaultValue, int userHandle) {
        try {
            return getGnLockSettings().getLong(secureSettingKey, defaultValue, userHandle);
        } catch (RemoteException re) {
            return defaultValue;
        }
    }

    public long gnGetLong(String secureSettingKey, long defaultValue) {
        try {
            return getGnLockSettings().getLong(secureSettingKey, defaultValue,
                    getCurrentOrCallingUserIdForSPwd());
        } catch (RemoteException re) {
            return defaultValue;
        }
    }

    public void gnSetLong(String secureSettingKey, long value) {
        gnSetLong(secureSettingKey, value, getCurrentOrCallingUserIdForSPwd());
    }

    private void gnSetLong(String secureSettingKey, long value, int userHandle) {
        try {
            getGnLockSettings().setLong(secureSettingKey, value, userHandle);
        } catch (RemoteException re) {
            // What can we do?
            Log.e(TAG, "Couldn't write long " + secureSettingKey + re);
        }
    }
	
    public String gnGetString(String secureSettingKey) {
        return gnGetString(secureSettingKey, getCurrentOrCallingUserIdForSPwd());
    }

    private String gnGetString(String secureSettingKey, int userHandle) {
        try {
            return getGnLockSettings().getString(secureSettingKey, null, userHandle);
        } catch (RemoteException re) {
            return null;
        }
    }
    public void gnSetString(String secureSettingKey, String value) {
        gnSetString(secureSettingKey, value, getCurrentOrCallingUserIdForSPwd());
    }

    private void gnSetString(String secureSettingKey, String value, int userHandle) {
        try {
            getGnLockSettings().setString(secureSettingKey, value, userHandle);
        } catch (RemoteException re) {
            // What can we do?
            Log.e(TAG, "Couldn't write string " + secureSettingKey + re);
        }
    }
    public void gnSetLockPassword(String password) {
        try {
            getGnLockSettings().setLockPassword(password, getCurrentOrCallingUserIdForSPwd());
        } catch (RemoteException re) {
            Log.e(TAG, "gnSetLockPassword re=" + re);
        }
    }
    public void gnSetLockPattern(String pattern) {
        try {
            getGnLockSettings().setLockPattern(pattern, getCurrentOrCallingUserIdForSPwd());
        } catch (RemoteException re) {
            Log.e(TAG, "gnSetLockPattern re=" + re);
        }
    }
	
    private int getCurrentOrCallingUserIdForSPwd() {
        return UserHandle.getCallingUserId();
    }

}

