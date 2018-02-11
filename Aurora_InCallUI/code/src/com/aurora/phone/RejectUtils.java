package com.android.incallui;  
  
import android.os.SystemProperties;
  
public class RejectUtils {  
    
    public static boolean isSupportBlack() {
//        return InCallApp.getInstance().getResources().getBoolan(R.bool.aurora_black_list);
        String prop = SystemProperties.get("ro.aurora.reject.support");
    	return prop.contains("yes") && !DeviceUtils.isIndia();
    }   
}  