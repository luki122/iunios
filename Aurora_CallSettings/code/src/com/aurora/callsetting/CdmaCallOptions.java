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

package com.aurora.callsetting;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;

import android.content.DialogInterface;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import aurora.app.*;
import aurora.preference.*;

import static com.aurora.callsetting.AuroraMSimConstants.SUBSCRIPTION_KEY;

public class CdmaCallOptions extends AuroraPreferenceActivity {
    private static final String LOG_TAG = "CdmaCallOptions";
    private final boolean DBG = (PhoneGlobals.DBG_LEVEL >= 2);

    private static final String BUTTON_VP_KEY = "button_voice_privacy_key";
    private AuroraCheckBoxPreference mButtonVoicePrivacy;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.cdma_call_privacy);
        // getting selected subscription
        int subscription = AuroraPhoneUtils.getSlot(getIntent());

        Log.d(LOG_TAG, "Getting CDMACallOptions subscription =" + subscription);
        Phone phone = PhoneGlobals.getInstance().getPhone(subscription);

        mButtonVoicePrivacy = (AuroraCheckBoxPreference) findPreference(BUTTON_VP_KEY);
        if (phone.getPhoneType() != PhoneConstants.PHONE_TYPE_CDMA
                || getResources().getBoolean(R.bool.config_voice_privacy_disable)) {
            //disable the entire screen
            getPreferenceScreen().setEnabled(false);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
        if (preference.getKey().equals(BUTTON_VP_KEY)) {
            return true;
        }
        return false;
    }

}
