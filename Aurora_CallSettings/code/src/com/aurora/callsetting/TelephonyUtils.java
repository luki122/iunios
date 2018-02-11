package com.aurora.callsetting;

import java.util.List;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;

import android.util.Log;
import android.telecom.TelecomManager;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;

public class TelephonyUtils {
    private static final String TAG = "TelephonyUtils";

    /**
     * Get whether airplane mode is in on.
     * @param context Context.
     * @return True for on.
     */
    public static boolean isAirplaneModeOn(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    /**
     * Calling API to get subId is in on.
     * @param subId Subscribers ID.
     * @return {@code true} if radio on
     */
    public static boolean isRadioOn(int subId) {
        ITelephony itele = ITelephony.Stub.asInterface(
                ServiceManager.getService(Context.TELEPHONY_SERVICE));
        boolean isOn = false;
        try {
            isOn = subId == SubscriptionManager.INVALID_SUBSCRIPTION_ID ? false :
                                                itele.isRadioOnForSubscriber(subId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "isOn = " + isOn + ", subId: " + subId);
        return isOn;
    }

    /**
     * check all slot radio on.
     * @param context context
     * @return is all slots radio on;
     */
    public static boolean isAllSlotRadioOn(Context context) {
        boolean isAllRadioOn = true;
        final TelephonyManager tm =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final int numSlots = tm.getSimCount();
            for (int i = 0; i < numSlots; ++i) {
                final SubscriptionInfo sir = Utils.findRecordBySlotId(context, i);
                if (sir != null) {
                    isAllRadioOn = isAllRadioOn && isRadioOn(sir.getSubscriptionId());
                }
            }
            Log.d(TAG, "isAllSlotRadioOn()... isAllRadioOn: " + isAllRadioOn);
            return isAllRadioOn;
    }
    
    /**
     * Return whether the project is Gemini or not.
     * @return If Gemini, return true, else return false
     */
    public static boolean isHotSwapHanppened(List<SubscriptionInfo> originaList,
            List<SubscriptionInfo> currentList) {
        boolean result = originaList.size() != currentList.size();
        Log.d(TAG, "isHotSwapHanppened : " + result);
        return result;
    }
    
    public static void setTelecomPreferSlot(int slot) {
		final TelecomManager telecomManager = TelecomManager.from(PhoneGlobals.getInstance());
		PhoneAccountHandle phoneAccountHandle = AuroraPhoneUtils.getPhoneAccountById(PhoneGlobals.getInstance(), String.valueOf(SubscriptionManager.from(PhoneGlobals.getInstance()).getSubIdUsingPhoneId(slot)));
		telecomManager.setUserSelectedOutgoingPhoneAccount(phoneAccountHandle);
    }
    
    public static void setTelecomPreferSub(int subId) {
		final TelecomManager telecomManager = TelecomManager.from(PhoneGlobals.getInstance());
		PhoneAccountHandle phoneAccountHandle = AuroraPhoneUtils.getPhoneAccountById(PhoneGlobals.getInstance(), String.valueOf(subId));
		telecomManager.setUserSelectedOutgoingPhoneAccount(phoneAccountHandle);
    }
}