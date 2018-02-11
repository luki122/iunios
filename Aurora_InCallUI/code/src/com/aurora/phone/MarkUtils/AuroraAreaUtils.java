package com.android.incallui;

import com.yulore.framework.*;
import com.yulore.superyellowpage.modelbean.*;

import java.util.List;
import android.content.Context;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.sogou.hmt.sdk.manager.HmtSdkManager;
public class AuroraAreaUtils {
    private static final String TAG = "AuroraAreaUtils";
	

    public static String getNumArea(String number) {
    	Log.i(TAG, "number = " + number);
		String result = "";
    	if(SogouUtils.isInit()) {
    		try {
    			result = HmtSdkManager.getInstance().getRegionName(number);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}       
    	} else if(YuLoreUtils.isInit()) {
			RecognitionTelephone r = InCallApp.getInstance().mYuloreUtils.queryNumberInfo(number, 1, true, 1);
			if(r != null) {
				result = r.getName();
				if(TextUtils.isEmpty(result)) {
					result = r.getLocation();
				}
        	}
    	}
    	if(TextUtils.isEmpty(result)) {
    		result = processEmptyArea(number);
    	}
		return result;        
    }
    
    private static String processEmptyArea(String number) {
    	String result = "";
      	if(!TextUtils.isEmpty(number) 
      			&& (number.replace(" ", "").length() == 7 || number.replace(" ", "").length() == 8)
      			&& !number.startsWith("0")
        			&& !number.startsWith("400")
        			&& !number.startsWith("800")
      			) {
      		result = InCallApp.getInstance().getString(R.string.aurora_local_number);
      	}  else if (!TextUtils.isEmpty(number) 	              			
        			&& (number.startsWith("400") || number.startsWith("800"))
        			&& number.replace(" ", "").length() == 10
        			){
      		result = InCallApp.getInstance().getString(R.string.aurora_service_number);
        } else {
        	result = "";
      	}   	
    	return result;
    }

}