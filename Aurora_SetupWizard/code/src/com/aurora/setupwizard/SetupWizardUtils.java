package com.aurora.setupwizard;

import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

public class SetupWizardUtils {
	
	private static final String TAG = "SetupWizardUtils";
	
	public static boolean hasTelephony(Context context) {
        PackageManager packageManager = context.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }
	
    // We only care that one sim is inserted
    public static boolean isSimInserted(Context context) {
    	TelephonyManager telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE); 
    	String result = telMgr.getSimSerialNumber();
    	if (TextUtils.isEmpty(result)) {
    		return false;
    	}
    	return true;
    }

}
