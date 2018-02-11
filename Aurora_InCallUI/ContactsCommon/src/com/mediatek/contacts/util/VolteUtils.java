package com.mediatek.contacts.util;

import java.util.List;

import android.content.Context;
import android.telecom.PhoneAccount;
import android.telecom.TelecomManager;


public class VolteUtils {
    /**
     * Returns whether the VoLTE conference call enabled.
     * @param context the context
     * @return true if the VOLTE is supported and has Volte phone account
     */
    public static boolean isVoLTEConfCallEnable(Context context) {
        final TelecomManager telecomManager = (TelecomManager) context
                .getSystemService(Context.TELECOM_SERVICE);
        List<PhoneAccount> phoneAccouts = telecomManager.getAllPhoneAccounts();
        for (PhoneAccount phoneAccount : phoneAccouts) {
            if (phoneAccount.hasCapabilities(
                    PhoneAccount.CAPABILITY_VOLTE_ENHANCED_CONFERENCE)) {
                return true;
            }
        }
        return false;
    }
}
