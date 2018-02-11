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
 * limitations under the License
 */

package com.android.providers.contacts;

import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.android.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder;
import com.android.providers.contacts.util.PhoneNumberUtils;

import com.google.android.collect.Sets;

import android.content.ContentValues;
import android.content.Context;
import gionee.provider.GnCallLog.Calls;
import android.provider.CallLog;
import android.text.TextUtils;
import android.util.Log;

import java.util.Locale;
import java.util.Set;

/**
 * Default implementation of {@link CallLogInsertionHelper}.
 * <p>
 * It added the country ISO abbreviation and the geocoded location.
 * <p>
 * It uses {@link PhoneNumberOfflineGeocoder} to compute the geocoded location of a phone number.
 */
/*package*/ class DefaultCallLogInsertionHelper implements CallLogInsertionHelper {
    private static DefaultCallLogInsertionHelper sInstance;
    private static final Set<String> LEGACY_UNKNOWN_NUMBERS = Sets.newHashSet("-1", "-2", "-3");

    private final CountryMonitor mCountryMonitor;
    private PhoneNumberUtil mPhoneNumberUtil;
    private PhoneNumberOfflineGeocoder mPhoneNumberOfflineGeocoder;
    private Context mContext;

    private final static String TAG = "DefaultCallLogInsertionHelper";
    public static synchronized DefaultCallLogInsertionHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DefaultCallLogInsertionHelper(context);
        }
        return sInstance;
    }

    private DefaultCallLogInsertionHelper(Context context) {
        mCountryMonitor = new CountryMonitor(context);
        mContext = context;
    }

    @Override
    public void addComputedValues(ContentValues values) {
        // Insert the current country code, so we know the country the number belongs to.
        String countryIso = getCurrentCountryIso();
        Log.d(TAG, "addComputedValues() countryIso == [" + countryIso +"]");
        Log.d(TAG, "addComputedValues() geocoded == [" + getGeocodedLocationFor(values.getAsString(CallLog.Calls.NUMBER), countryIso) +"]");
        values.put(CallLog.Calls.COUNTRY_ISO, countryIso);
        // Insert the geocoded location, so that we do not need to compute it on the fly.
        values.put(CallLog.Calls.GEOCODED_LOCATION,
                getGeocodedLocationFor(values.getAsString(CallLog.Calls.NUMBER), countryIso));

        final String number = values.getAsString(CallLog.Calls.NUMBER);
        if (LEGACY_UNKNOWN_NUMBERS.contains(number)) {
            values.put(CallLog.Calls.NUMBER_PRESENTATION, CallLog.Calls.PRESENTATION_UNKNOWN);
            //delete by ligy
//            values.put(CallLog.Calls.NUMBER, "");
        }

        // Check for a normalized number; if not present attempt to determine one now.
        if (!values.containsKey(CallLog.Calls.CACHED_NORMALIZED_NUMBER) &&
                !TextUtils.isEmpty(number)) {
            String normalizedNumber = PhoneNumberUtils.formatNumberToE164(number, countryIso);
            if (!TextUtils.isEmpty(normalizedNumber)) {
                values.put(CallLog.Calls.CACHED_NORMALIZED_NUMBER, normalizedNumber);
            }
        }
    }

    private String getCurrentCountryIso() {
        return mCountryMonitor.getCountryIso();
    }

    private synchronized PhoneNumberUtil getPhoneNumberUtil() {
        if (mPhoneNumberUtil == null) {
            mPhoneNumberUtil = PhoneNumberUtil.getInstance();
        }
        return mPhoneNumberUtil;
    }

    private PhoneNumber parsePhoneNumber(String number, String countryIso) {
        try {
            return getPhoneNumberUtil().parse(number, countryIso);
        } catch (NumberParseException e) {
            return null;
        }
    }

    private synchronized PhoneNumberOfflineGeocoder getPhoneNumberOfflineGeocoder() {
        if (mPhoneNumberOfflineGeocoder == null) {
            mPhoneNumberOfflineGeocoder = PhoneNumberOfflineGeocoder.getInstance();
        }
        return mPhoneNumberOfflineGeocoder;
    }

    @Override
    public String getGeocodedLocationFor(String number, String countryIso) {
        PhoneNumber structuredPhoneNumber = parsePhoneNumber(number, countryIso);
        if (structuredPhoneNumber != null) {
            return getPhoneNumberOfflineGeocoder().getDescriptionForNumber(
                    structuredPhoneNumber, mContext.getResources().getConfiguration().locale);
        } else {
            return null;
        }
    }
}
