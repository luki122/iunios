package com.android.phone;
import android.telephony.PhoneNumberUtils;
import android.content.Context;

public class AuroraPhoneNumberUtils {
    public static boolean isLocalEmergencyNumber(String number, Context context) {
    		return 	PhoneNumberUtils.isLocalEmergencyNumber(number, context);
    }

    public static boolean isPotentialLocalEmergencyNumber(String number, Context context) {
    		return PhoneNumberUtils.isPotentialLocalEmergencyNumber(number, context);
    }

}