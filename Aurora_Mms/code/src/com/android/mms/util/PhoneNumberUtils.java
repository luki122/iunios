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

package com.android.mms.util;

import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.android.i18n.phonenumbers.ShortNumberUtil;
import com.android.mms.MmsApp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.CountryDetector;
import android.net.Uri;
import android.os.SystemProperties;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
//import android.telephony.Rlog;
import android.util.SparseIntArray;
import android.telephony.TelephonyManager;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Various utilities for dealing with phone number strings.
 */
public class PhoneNumberUtils extends android.telephony.PhoneNumberUtils
{

    // Three and four digit phone numbers for either special services,
    // or 3-6 digit addresses from the network (eg carrier-originated SMS messages) should
    // not match.
    //
    // This constant used to be 5, but SMS short codes has increased in length and
    // can be easily 6 digits now days. Most countries have SMS short code length between
    // 3 to 6 digits. The exceptions are
    //
    // Australia: Short codes are six or eight digits in length, starting with the prefix "19"
    //            followed by an additional four or six digits and two.
    // Czech Republic: Codes are seven digits in length for MO and five (not billed) or
    //            eight (billed) for MT direction
    //
    // see http://en.wikipedia.org/wiki/Short_code#Regional_differences for reference
    //
    // However, in order to loose match 650-555-1212 and 555-1212, we need to set the min match
    // to 7.
    static final int MIN_MATCH = (MmsApp.mHasIndiaFeature ? 10 : 7); // modify by wangth
    
    static final int MOBILE_MIN_MATCH = (MmsApp.mHasIndiaFeature ? 10 : 11); // add by wangth

    /**
     * Returns the rightmost MIN_MATCH (5) characters in the network portion
     * in *reversed* order
     *
     * This can be used to do a database lookup against the column
     * that stores getStrippedReversed()
     *
     * Returns null if phoneNumber == null
     */
    public static String
    toCallerIDMinMatch(String phoneNumber) {
        String np = extractNetworkPortionAlt(phoneNumber);
        // add by wangth begin
        if (np != null && (np.startsWith("1") || np.startsWith("+861"))) {
            return internalGetStrippedReversed(np, MOBILE_MIN_MATCH);
        }
        // add by wangth end
        return internalGetStrippedReversed(np, MIN_MATCH);
    }
    
    /**
     * Returns the last numDigits of the reversed phone number
     * Returns null if np == null
     */
    private static String
    internalGetStrippedReversed(String np, int numDigits) {
        if (np == null) return null;

        StringBuilder ret = new StringBuilder(numDigits);
        int length = np.length();

        for (int i = length - 1, s = length
            ; i >= 0 && (s - i) <= numDigits ; i--
        ) {
            char c = np.charAt(i);

            ret.append(c);
        }

        return ret.toString();
    }

    // Aurora xuyong 2015-05-25 added for aurora's new feature start
    public static String getCallerIDMinMacth(String phoneNumber) {
    	String np = internalGetStrippedReversed(extractNetworkPortionAlt(phoneNumber), MOBILE_MIN_MATCH);
    	if (np == null) {
    		return null;
    	}
    	int length = np.length();
    	StringBuilder ret = new StringBuilder(length);
    	for (int i = length - 1; i >= 0; i--) {
    		char c = np.charAt(i);
            ret.append(c);
    	}
    	
    	return ret.toString();
    }
    // Aurora xuyong 2015-05-25 added for aurora's new feature end
}
