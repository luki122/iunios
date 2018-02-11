package com.android.server.telecom;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.Data;
import android.util.Log;
import android.telephony.SubscriptionManager;
import com.android.server.telecom.AuroraTelephony.SIMInfo;
import com.android.server.telecom.AuroraTelephony.SimInfo;


public class SimIconUtils {
    private static final String LOG_TAG = "SimIconUtils";
    
    public static int getSimIconNotification(int simId) {
		int result = -1;
		int slot = 0;

//	     slot = SubscriptionManager.getSlotId(simId);
		SIMInfo simInfo = SIMInfo.getSIMInfoById(AuroraGlobals.getInstance(), simId);
		if (simInfo != null) {
			slot = simInfo.mSlot;
		}
		if(slot == 0) {
			result = R.drawable.sim_noti_11;
		} else {
			result = R.drawable.sim_noti_22;
		}	
    	
		
		return result;
	}
	
	
}