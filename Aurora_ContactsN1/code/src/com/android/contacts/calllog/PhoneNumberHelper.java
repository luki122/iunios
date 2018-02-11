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

package com.android.contacts.calllog;

import com.android.contacts.ContactsApplication;
import com.android.contacts.R;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.mediatek.contacts.ContactsFeatureConstants;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.aurora.android.contacts.AuroraTelephonyManager;
import com.android.contacts.ContactsUtils;

/**
 * Helper for formatting and managing phone numbers.
 */
public class PhoneNumberHelper {
    private final Resources mResources;

    public PhoneNumberHelper(Resources resources) {
        mResources = resources;
    }

    /** Returns true if it is possible to place a call to the given number. */
    public static boolean canPlaceCallsTo(CharSequence number) {
        return !(TextUtils.isEmpty(number)
                || number.equals("-1")
                || number.equals("-2")
                || number.equals("-3"));
    }

    /** Returns true if it is possible to send an SMS to the given number. */
    public boolean canSendSmsTo(CharSequence number) {
        return canPlaceCallsTo(number) && !isSipNumber(number);
    }

    /**
     * Returns the string to display for the given phone number.
     *
     * @param number the number to display
     * @param formattedNumber the formatted number if available, may be null
     */
    public CharSequence getDisplayNumber(CharSequence number, CharSequence formattedNumber) {
        if (TextUtils.isEmpty(number)) {
            return "";
        }
        if (number.equals("-1")) {
            return mResources.getString(R.string.unknown);
        }
        if (number.equals("-2")) {
            return mResources.getString(R.string.private_num);
        }
        if (number.equals("-3")) {
            return mResources.getString(R.string.payphone);
        }
        /*if (isVoicemailNumber(number)) {
            return mResources.getString(R.string.voicemail);
        } */
        if (TextUtils.isEmpty(formattedNumber)) {
            return number;
        } else {
            return formattedNumber;
        }
    }

    /** Returns a URI that can be used to place a call to this number. */
    public static Uri getCallUri(String number) {
        /**
        * Change Feature by Mediatek Begin.
        * Original Android's Code:
          if (isSipNumber(number)) {
              return Uri.fromParts("sip", number, null);
          }
          return Uri.fromParts("tel", number, null);
        * Descriptions:
        */
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            return Uri.fromParts("tel", number, null);
        } else {
            if (isSipNumber(number)) {
                return Uri.fromParts("sip", number, null);
            }
            return Uri.fromParts("tel", number, null);
        }
        
        /**
        * Change Feature by Mediatek End.
        */
     }

    /** Returns true if the given number is the number of the configured voicemail. */
    public boolean isVoicemailNumber(CharSequence number) {
        return PhoneNumberUtils.isVoiceMailNumber(number.toString());
    }

    /** Returns true if the given number is a SIP address. */
    public static boolean isSipNumber(CharSequence number) {
        return PhoneNumberUtils.isUriNumber(number.toString());
    }
    
    //The following lines are provided and maintained by Mediatek Inc.
    /** Returns true if the given number is a emergency number. */
    public boolean isEmergencyNumber(CharSequence number) {
        return PhoneNumberUtils.isEmergencyNumber(number.toString());
    }
    
    /** Returns true if the given number is a isVoiceMailNumberForMtk . */
    public boolean isVoiceMailNumberForMtk(CharSequence number, int simId) {
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            final int slot = ContactsUtils.getSlotBySubId(simId);
            if (slot == ContactsFeatureConstants.GEMINI_SIM_1) {
                if (mVoiceMailNumber != null
                        && PhoneNumberUtils.compare(mVoiceMailNumber.toString(), number.toString())) {
                    return true;
                }

            } else if (slot == ContactsFeatureConstants.GEMINI_SIM_2) {
                if (mVoiceMailNumber2 != null
                        && PhoneNumberUtils
                                .compare(mVoiceMailNumber2.toString(), number.toString())) {
                    return true;
                }
            }
        } else {
            if (mVoiceMailNumber != null
                    && PhoneNumberUtils.compare(mVoiceMailNumber.toString(), number.toString())) {
                return true;
            }
        }
        return false;
    }

    private static String mVoiceMailNumber;
    private static String mVoiceMailNumber2;

    public static void getVoiceMailNumber() {
        TelephonyManager telephonyManager = TelephonyManager.getDefault();
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            mVoiceMailNumber = AuroraTelephonyManager.getVoiceMailNumberGemini(ContactsFeatureConstants.GEMINI_SIM_1);
            mVoiceMailNumber2 = AuroraTelephonyManager.getVoiceMailNumberGemini(ContactsFeatureConstants.GEMINI_SIM_2);
        } else {
            mVoiceMailNumber = telephonyManager.getVoiceMailNumber();
        }
    }
   
   public static boolean isVoicemailUri(Uri uri) {
       if (uri == null) {
           return false;
       }
       
       String scheme = uri.getScheme();
       return "voicemail".equals(scheme);
   }
   
   public Uri getCallUri(String number, int simId) {
       if (isVoiceMailNumberForMtk(number, simId)) {
           return Uri.parse("voicemail:x");
       }
       if (FeatureOption.MTK_GEMINI_SUPPORT) {
           return Uri.fromParts("tel", number, null);
       } else {
           if (isSipNumber(number)) {
               return Uri.fromParts("sip", number, null);
           }
           return Uri.fromParts("tel", number, null);
       }
       
    }
    //The previous lines are provided and maintained by Mediatek Inc.

}
