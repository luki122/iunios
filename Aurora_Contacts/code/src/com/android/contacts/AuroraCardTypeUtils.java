package com.android.contacts;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.gionee.internal.telephony.GnITelephony;
import android.os.ServiceManager;
import android.util.Log;
import com.android.internal.telephony.ITelephony;
/*
 *  QC special.
 */
public class AuroraCardTypeUtils{
    private static final String TAG = "AuroraCardTypeUtils";
    
    private static AuroraCardTypeUtils sInstance;

    private Context mContext;
    
    private BroadcastReceiver mUiccReceiver;
    
    public static String mSimType1 = "unknown", mSimType2 = "unknown";
    
    private static final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
    
    public static AuroraCardTypeUtils init(Context context) {
        synchronized (AuroraCardTypeUtils.class) {
            if (sInstance == null) {
                sInstance = new AuroraCardTypeUtils(context);
            } else {
                Log.wtf(TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            return sInstance;
        }
    }
    
    private AuroraCardTypeUtils(Context context) {
        mContext = context;
        mUiccReceiver = new AuroraCardTypeUtilsReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("AURORA_UICC_CHANGED");
        mContext.registerReceiver(mUiccReceiver, filter);
        getType();
    }  
    
    
	private class AuroraCardTypeUtilsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) { 
            Log.d(TAG, "onReceive");
            getType();
        }
    }
	
	public static String getIccCardType(ITelephony iTel) {
	    if(mSimType1.equals("unknown")) {
	    	mSimType1 = GnITelephony.getIccCardType(iTel);
        }
        Log.d(TAG, "getIccCardType mSimType1 = " + mSimType1);
		return  mSimType1;
	}
	
	public static String getIccCardTypeGemini(ITelephony iTel, int slotId) {
		String result = slotId > 0 ? mSimType2 : mSimType1;
        if(result.equals("unknown")) {
        	result = GnITelephony.getIccCardTypeGemini(iTel, slotId);
        }
        Log.d(TAG, "getIccCardTypeGemini mSimType" + slotId + " = " + result);
		return result;
	}
	
	private static void getType(){
    	if(GNContactsUtils.isMultiSimEnabled()) {
        	mSimType1 = GnITelephony.getIccCardTypeGemini(iTel, 0);
            mSimType2 = GnITelephony.getIccCardTypeGemini(iTel, 1);		
    	} else {
    		mSimType1 = GnITelephony.getIccCardType(iTel);
    	}
	} 
}