package com.android.phone;  

import android.content.Context;
import android.util.Log;
import com.android.phone.AuroraTelephony.SIMInfo;
  
public class AuroraSubUtils {  
    private static final String TAG = "AuroraSubUtils";


    public static long getSubIdbySlot(Context ctx, int slot) {
        long simId = 0;
        SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(ctx, slot);
        if(simInfo != null) {
        	simId = simInfo.mSimId;
        } 	
        return simId;
    }
    
    public static int getSlotBySubId(Context ctx, int subId) {
    	int slot = -1 ;
    	SIMInfo simInfo = SIMInfo.getSIMInfoById(PhoneGlobals.getInstance(), subId);
		if (simInfo != null) {
			slot = simInfo.mSlot;
		}
		return slot;
    }

    

}  