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

package com.android.providers.contacts.util;

import com.android.providers.contacts.ContactsProvidersApplication;

import android.os.SystemProperties;

/**
 * Various utilities for dealing with phone number strings.
 */
public class PhoneNumberUtils extends android.telephony.PhoneNumberUtils
{

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
//        android.util.Log.e("wangth", "phoneNumber =  " + phoneNumber + "  np = " + np);
        // add by wangth begin
        // Aurora xuyong 2015-08-24 modified for bug #15845 start
        // The cts-verifier test case: 
        //     Notification Attention Management Test
        //         Charlie's phone number has the same start "+1" with Alice & Bob's phone number
        //     and also has the same end "5551212", so when we look up contact by Charlie's number,
        //     we can always find a match result, which isn't the test case's want.
        if (np != null && (np.startsWith("1") || np.startsWith("+861") || np.startsWith("+1"))) {
        // Aurora xuyong 2015-08-24 modified for bug #15845 end
            return internalGetStrippedReversed(np, Mobile_MIN_MATCH);
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

//        android.util.Log.e("wangth", "ret.toString() = " + ret.toString());
        return ret.toString();
    }
    
    // However, in order to loose match 650-555-1212 and 555-1212, we need to set the min match
    // to 7.
    static final int MIN_MATCH = (ContactsProvidersApplication.sIsIndiaProduct? 10 : 7);  // modify for wangth
    
    static final int Mobile_MIN_MATCH = (ContactsProvidersApplication.sIsIndiaProduct? 10 : 11); // add by wangth
    
}
