package com.android.incallui;  

import android.content.Context;
import android.util.Log;
import android.telephony.SubscriptionManager;

import java.util.HashMap; 
  
public class AuroraSubUtils {  
    private static final String TAG = "AuroraSubUtils";
    
    private static HashMap<Integer,Integer> mSubIdSlotIdPairs = new HashMap<Integer,Integer>();


    public static int getSubIdbySlot(Context ctx, int slot) {
    	SubscriptionManager mSubscriptionManager = SubscriptionManager.from(ctx);    
    	return mSubscriptionManager.getSubIdUsingPhoneId(slot);    	
    }
    
    public static int getSlotBySubId(Context ctx, int subId) {
     	SubscriptionManager mSubscriptionManager = SubscriptionManager.from(ctx); 
     	int slot = mSubscriptionManager.getSlotId(subId);
     	if(slot > -1) {
     		mSubIdSlotIdPairs.put(subId , slot);
     	} else if(mSubIdSlotIdPairs.get(subId) != null){
     		slot = mSubIdSlotIdPairs.get(subId);
     	}     	
		return slot;
    }

    

}  
