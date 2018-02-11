package com.mediatek.contacts.util;

import com.android.contacts.ContactsApplication;
import com.mediatek.contacts.ContactsFeatureConstants;

import android.content.Context;
import android.provider.Settings;

public class ContactsSettingsUtils {

    private final static String TAG = "ContactsSettingsUtils";

    private static final long DEFAULT_SIM_SETTING_ALWAYS_ASK = ContactsFeatureConstants.DEFAULT_SIM_SETTING_ALWAYS_ASK;
    public static final long VOICE_CALL_SIM_SETTING_INTERNET = ContactsFeatureConstants.VOICE_CALL_SIM_SETTING_INTERNET;
    public static final long DEFAULT_SIM_NOT_SET = ContactsFeatureConstants.DEFAULT_SIM_NOT_SET;
    

    private static ContactsSettingsUtils sMe;

    protected Context mContext;

    private ContactsSettingsUtils(Context context) {
        mContext = context;
    }

    public static ContactsSettingsUtils getInstance() {
        if(sMe == null)
            sMe = new ContactsSettingsUtils(ContactsApplication.getInstance());
        return sMe;
    }

    public static long getDefaultSIMForVoiceCall() {
        return DEFAULT_SIM_SETTING_ALWAYS_ASK;
    }

    public static long getDefaultSIMForVideoCall() {
        return 0;
    }

    protected void registerSettingsObserver() {
        //
    }
}
