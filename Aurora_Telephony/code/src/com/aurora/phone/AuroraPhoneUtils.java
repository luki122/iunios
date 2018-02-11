package com.android.phone;

import java.util.List;

import com.android.internal.telephony.Phone;

import android.content.SharedPreferences;
import android.content.Context;
import android.text.TextUtils;
import android.telephony.SubscriptionManager;

import android.telecom.TelecomManager;
import android.telecom.PhoneAccountHandle;

public class AuroraPhoneUtils {
	private static final String LOG_TAG = "AuroraPhoneUtils";

	public static boolean isSimulate() {
//		if (DeviceUtils.isIUNI() || DeviceUtils.is7503()) {
			SharedPreferences sP = null;
			sP = PhoneGlobals.getInstance().getSharedPreferences(
					"com.android.phone_preferences", Context.MODE_PRIVATE);
			return sP != null && sP.getBoolean("aurora_simulate_switch", false);
//		}
//		return false;
	}
	
//	  public static Phone[] getPhones() {
//		  if(isSimulate()) {
//			 Phone[] p =  new Phone[1]; 
//	         p[0] = PhoneGlobals.getPhone();
//	         return p;
//		  } else {
//			  return AuroraPhoneUtils.getPhones();
//		  }
//	    }
	
	public static PhoneAccountHandle getPhoneAccountById(Context context,
			String id) {
		if (!TextUtils.isEmpty(id)) {
			final TelecomManager telecomManager = (TelecomManager) context
					.getSystemService(Context.TELECOM_SERVICE);
			final List<PhoneAccountHandle> accounts = telecomManager
					.getCallCapablePhoneAccounts();
			for (PhoneAccountHandle account : accounts) {
				if (id.equals(account.getId())) {
					return account;
				}
			}
		}
		return null;
	}

	public static int getSubIdbySlot(Context ctx, int slot) {
		SubscriptionManager mSubscriptionManager = SubscriptionManager
				.from(ctx);
		return mSubscriptionManager.getSubIdUsingPhoneId(slot);
	}
}