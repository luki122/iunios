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

package com.android.phone;

/**
 * App-wide constants and enums for the phone app.
 *
 * Any constants that need to be shared between two or more classes within
 * the com.android.phone package should be defined here.  (Constants that
 * are private to only one class can go in that class's .java file.)
 */
public class Constants {

    /**
     * Complete list of error / diagnostic indications we might possibly
     * need to present to the user.
     *
     * This enum is basically a high-level list of the kinds of failures
     * or "exceptional conditions" that can occur when making a phone
     * call.  When an error occurs, the CallController stashes away one of
     * these codes in the InCallUiState.pendingCallStatusCode flag and
     * launches the InCallScreen; the InCallScreen will then display some
     * kind of message to the user (usually an error dialog) explaining
     * what happened.
     *
     * The enum values here cover all possible result status / error
     * conditions that can happen when attempting to place an outgoing
     * call (see CallController.placeCall() and placeCallInternal()), as
     * well as some other conditions (like CDMA_CALL_LOST and EXITED_ECM)
     * that don't technically result from the placeCall() sequence but
     * still need to be communicated to the user.
     */
    public enum CallStatusCode {
        /**
         * No error or exceptional condition occurred.
         * The InCallScreen does not need to display any kind of alert to the user.
         */
        SUCCESS,

        /**
         * Radio is explictly powered off, presumably because the
         * device is in airplane mode.
         */
        POWER_OFF,

        /**
         * Only emergency numbers are allowed, but we tried to dial
         * a non-emergency number.
         */
        EMERGENCY_ONLY,

        /**
         * No network connection.
         */
        OUT_OF_SERVICE,

        /**
         * The supplied CALL Intent didn't contain a valid phone number.
         */
        NO_PHONE_NUMBER_SUPPLIED,

        /**
         * Our initial phone number was actually an MMI sequence.
         */
        DIALED_MMI,

        /**
         * We couldn't successfully place the call due to an
         * unknown failure in the telephony layer.
         */
        CALL_FAILED,

        /**
         * We tried to call a voicemail: URI but the device has no
         * voicemail number configured.
         *
         * When InCallUiState.pendingCallStatusCode is set to this
         * value, the InCallScreen will bring up a UI explaining what
         * happened, and allowing the user to go into Settings to fix the
         * problem.
         */
        VOICEMAIL_NUMBER_MISSING,

        /**
         * This status indicates that InCallScreen should display the
         * CDMA-specific "call lost" dialog.  (If an outgoing call fails,
         * and the CDMA "auto-retry" feature is enabled, *and* the retried
         * call fails too, we display this specific dialog.)
         *
         * TODO: this is currently unused, since the "call lost" dialog
         * needs to be triggered by a *disconnect* event, rather than when
         * the InCallScreen first comes to the foreground.  For now we use
         * the needToShowCallLostDialog field for this (see below.)
         */
        CDMA_CALL_LOST,

        /**
         * This status indicates that the call was placed successfully,
         * but additionally, the InCallScreen needs to display the
         * "Exiting ECM" dialog.
         *
         * (Details: "Emergency callback mode" is a CDMA-specific concept
         * where the phone disallows data connections over the cell
         * network for some period of time after you make an emergency
         * call.  If the phone is in ECM and you dial a non-emergency
         * number, that automatically *cancels* ECM, but we additionally
         * need to warn the user that ECM has been canceled (see bug
         * 4207607.))
         */
        EXITED_ECM,

        /**
         * out of 3G service for video call
         */
        OUT_OF_3G_FAILED,

        /**
         * add by mediatek .inc
         * description : blocked by FDN
         */
        FDN_BLOCKED,
        /**
         * out of 3G service for drop voice call
         */
        DROP_VOICECALL,
        
        //aurora add liguangyu 20140928 for #8739 start
        OTHER_ACTIVE
        //aurora add liguangyu 20140928 for #8739 end
    }

    //
    // URI schemes
    //

    public static final String SCHEME_SIP = "sip";
    public static final String SCHEME_SMS = "sms";
    public static final String SCHEME_SMSTO = "smsto";
    public static final String SCHEME_TEL = "tel";
    public static final String SCHEME_VOICEMAIL = "voicemail";

    //
    // TODO: Move all the various EXTRA_* and intent action constants here too.
    // (Currently they're all over the place: InCallScreen,
    // OutgoingCallBroadcaster, OtaUtils, etc.)
    //
    public static final String EXTRA_SLOT_ID = "com.android.phone.extra.slot";
    public static final String EXTRA_ORIGINAL_SIM_ID = "com.android.phone.extra.original";
    public static final String EXTRA_IS_VIDEO_CALL = "com.android.phone.extra.video";
    public static final String EXTRA_IS_IP_DIAL = "com.android.phone.extra.ip";
    public static final String EXTRA_IS_NOTIFICATION = "com.android.phone.extra.notification";
    public static final String EXTRA_IS_FORBIDE_DIALOG = "com.android.phone.extra.forbid_dialog";
    public static final String EXTRA_INTERNATIONAL_DIAL_OPTION = "com.android.phone.extra.international";
    public static final String EXTRA_ACTUAL_NUMBER_TO_DIAL = "android.phone.extra.ACTUAL_NUMBER_TO_DIAL";
    
    public static final int INTERNATIONAL_DIAL_OPTION_NORMAL = 0;
    public static final int INTERNATIONAL_DIAL_OPTION_WITH_COUNTRY_CODE = 1;
    public static final int INTERNATIONAL_DIAL_OPTION_IGNORE = 2;

    // Dtmf tone type setting value for CDMA phone
    public static final int DTMF_TONE_TYPE_NORMAL = 0;
    public static final int DTMF_TONE_TYPE_LONG   = 1;

    public static final String VOICEMAIL_URI = "voicemail:";
    public static final String PHONE_PACKAGE = "com.android.phone";
    public static final String OUTGOING_CALL_BROADCASTER = "com.android.phone.OutgoingCallBroadcaster";
    public static final String OUTGOING_CALL_RECEIVER = "com.mediatek.phone.OutgoingCallReceiver";

    // setting class name
    public static final String CALL_SETTING_CLASS_NAME = "com.mediatek.settings.CallSettings";
    public static final String VOICE_MAIL_SETTINGS_CLASS_NAME = "com.mediatek.settings.VoiceMailSetting";
    public static final String MODEM_3G_CAPABILITY_SWITCH_SETTING_CLASS_NAME =
                                                           "com.mediatek.settings.Modem3GCapabilitySwitch";
    public static final String SIP_CALL_SETTING_CLASS_NAME = "com.mediatek.settings.SipCallSetting";
    public static final String IP_PREFIX_SETTING_CLASS_NAME = "com.mediatek.settings.IpPrefixPreference";
    public static final String SETTING_SUB_TITLE_NAME = "sub_title_name";

    // Return codes from placeCall()
    public static final int CALL_STATUS_DIALED = 0;  // The number was successfully dialed
    public static final int CALL_STATUS_DIALED_MMI = 1;  // The specified number was an MMI code
    public static final int CALL_STATUS_FAILED = 2;  // The call failed
    public static final int CALL_STATUS_DROP_VOICECALL = 3;  // The VT Call drop voice call
    public static final String EXTRA_FOLLOW_SIM_MANAGEMENT = "follow_sim_management";

    // Phone record related
    public static final long PHONE_RECORD_LOW_STORAGE_THRESHOLD = 2L * 1024L * 1024L; // unit is BYTE, totally 2MB
    // The value to seperate voice call recording and VT call recording
    public static final int PHONE_RECORDING_VOICE_CALL_CUSTOM_VALUE = 0;
    public static final int PHONE_RECORDING_VIDEO_CALL_CUSTOM_VALUE = 1;
    public static final int PHONE_RECORDING_TYPE_NOT_RECORDING = 0;
    public static final int PHONE_RECORDING_TYPE_VOICE_AND_PEER_VIDEO = 1;
    public static final int PHONE_RECORDING_TYPE_ONLY_VOICE = 2;
    public static final int PHONE_RECORDING_TYPE_ONLY_PEER_VIDEO = 3;
    
    // Unread missed call count related
    public static final String CONTACTS_PACKAGE = "com.android.contacts";
    public static final String CONTACTS_DIALTACTS_ACTIVITY = "com.android.contacts.activities.DialtactsActivity";
    public static final String CONTACTS_UNREAD_KEY = "com_android_contacts_mtk_unread";

    public static final String STORAGE_SETTING_INTENT_NAME = "android.settings.INTERNAL_STORAGE_SETTINGS";
    public static final int STORAGE_TYPE_PHONE_STORAGE = 0;
    public static final int STORAGE_TYPE_SD_CARD = 1;

    /**
     * VT screen mode for updating VT UI
     */
    public static enum VTScreenMode {
        VT_SCREEN_CLOSE,
        VT_SCREEN_OPEN,
        VT_SCREEN_WAITING
    }

    public static final String PHONE_PREFERENCE_NAME = "com.android.phone_preferences";
    
    /* NETWORK_MODE_* See ril.h RIL_REQUEST_SET_PREFERRED_NETWORK_TYPE */
    public static final int NETWORK_MODE_WCDMA_PREF     = 0; /* GSM/WCDMA (WCDMA preferred) */
    public static final int NETWORK_MODE_GSM_ONLY       = 1; /* GSM only */
    public static final int NETWORK_MODE_WCDMA_ONLY     = 2; /* WCDMA only */
    public static final int NETWORK_MODE_GSM_UMTS       = 3; /* GSM/WCDMA (auto mode, according to PRL)
                                            AVAILABLE Application Settings menu*/
    public static final int NETWORK_MODE_CDMA           = 4; /* CDMA and EvDo (auto mode, according to PRL)
                                            AVAILABLE Application Settings menu*/
    public static final int NETWORK_MODE_CDMA_NO_EVDO   = 5; /* CDMA only */
    public static final int NETWORK_MODE_EVDO_NO_CDMA   = 6; /* EvDo only */
    public static final int NETWORK_MODE_GLOBAL         = 7; /* GSM/WCDMA, CDMA, and EvDo (auto mode, according to PRL)
                                            AVAILABLE Application Settings menu*/
    public static final int NETWORK_MODE_LTE_CDMA_EVDO  = 8; /* LTE, CDMA and EvDo */
    public static final int NETWORK_MODE_LTE_GSM_WCDMA  = 9; /* LTE, GSM/WCDMA */
    public static final int NETWORK_MODE_LTE_CMDA_EVDO_GSM_WCDMA = 10; /* LTE, CDMA, EvDo, GSM/WCDMA */
    public static final int NETWORK_MODE_LTE_ONLY       = 11; /* LTE Only mode. */
    public static final int NETWORK_MODE_LTE_WCDMA      = 12; /* LTE/WCDMA */
    
    public static final int NETWORK_MODE_TD_SCDMA_ONLY            = 13; /* TD-SCDMA only */
    public static final int NETWORK_MODE_TD_SCDMA_WCDMA           = 14; /* TD-SCDMA and WCDMA */
    public static final int NETWORK_MODE_TD_SCDMA_LTE             = 15; /* TD-SCDMA and LTE */
    public static final int NETWORK_MODE_TD_SCDMA_GSM             = 16; /* TD-SCDMA and GSM */
    public static final int NETWORK_MODE_TD_SCDMA_GSM_LTE         = 17; /* TD-SCDMA,GSM and LTE */
    public static final int NETWORK_MODE_TD_SCDMA_GSM_WCDMA       = 18; /* TD-SCDMA, GSM/WCDMA */
    public static final int NETWORK_MODE_TD_SCDMA_WCDMA_LTE       = 19; /* TD-SCDMA, WCDMA and LTE */
    public static final int NETWORK_MODE_TD_SCDMA_GSM_WCDMA_LTE   = 20; /* TD-SCDMA, GSM/WCDMA and LTE */
    public static final int NETWORK_MODE_TD_SCDMA_CDMA_EVDO_GSM_WCDMA  = 21; /*TD-SCDMA,EvDo,CDMA,GSM/WCDMA*/
    public static final int NETWORK_MODE_TD_SCDMA_LTE_CDMA_EVDO_GSM_WCDMA = 22; /* TD-SCDMA/LTE/GSM/WCDMA, CDMA, and
                                                               EvDo */
    public static int PREFERRED_NETWORK_MODE      = NETWORK_MODE_WCDMA_PREF;
     
}
