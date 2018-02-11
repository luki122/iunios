/*
 * Copyright (C) 2008 The Android Open Source Project
 * Copyright (c) 2011-2013 The Linux Foundation. All rights reserved.
 *
 * Not a Contribution.
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

package com.android.phone;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.net.sip.SipManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.AsyncResult;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;

import android.os.Build;

/**
 * Top level "Call settings" UI; see res/xml/call_feature_setting.xml
 *
 * This preference screen is the root of the "MSim Call settings" hierarchy
 * available from the Phone app; the settings here let you control various
 * features related to phone calls (including voicemail settings, SIP
 * settings, the "Respond via SMS" feature, and others.)  It's used only
 * on voice-capable phone devices.
 *
 * Note that this activity is part of the package com.android.phone, even
 * though you reach it from the "Phone" app (i.e. DialtactsActivity) which
 * is from the package com.android.contacts.
 *
 * For the "MSim Mobile network settings" screen under the main Settings app,
 * See {@link MSimMobileNetworkSettings}.
 *
 * @see com.android.phone.MSimMobileNetworkSettings
 */
public class MSimCallFeaturesSetting extends Activity{   
	
    private static final String PHONE_PACKAGE = "com.aurora.callsetting";
    private static final String CALL_SETTINGS_CLASS_NAME =
            "com.aurora.callsetting.CallFeaturesSetting";
    private static final String MSIM_CALL_SETTINGS_CLASS_NAME =
            "com.aurora.callsetting.MSimCallFeaturesSetting";
	@Override
    protected void onCreate(Bundle icicle) {
            super.onCreate(icicle);
            final Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClassName(PHONE_PACKAGE, MSIM_CALL_SETTINGS_CLASS_NAME);
 
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
}
