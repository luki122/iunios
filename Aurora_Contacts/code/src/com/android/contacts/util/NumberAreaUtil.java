package com.android.contacts.util;

import java.util.concurrent.ConcurrentHashMap;

import com.android.contacts.ContactsApplication;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.android.contacts.R;

public class NumberAreaUtil {
	
	private static NumberAreaUtil mNumberAreaUtil = null;
	private static Object mWait = new Object();

	private ConcurrentHashMap<String, String> mNumberAreas = new ConcurrentHashMap<String, String>();
	
	public static NumberAreaUtil getInstance(Context context) {
		if (mNumberAreaUtil == null) {
			synchronized (mWait) {
				if(mNumberAreaUtil == null){
					mNumberAreaUtil = new NumberAreaUtil();
				}
			}
		}
		return mNumberAreaUtil;
	}
    public String getNumAreaFromAora(Context context, String number, boolean isRemember){
    	if (!ContactsApplication.sIsGnAreoNumAreaSupport) {
    		return null;
    	}
    	if(mNumberAreas.containsKey(number)){
    		return mNumberAreas.get(number);
    	} else {
    		String area = YuloreUtils.getInstance(context).getArea(number);
    		if(isRemember && !TextUtils.isEmpty(area)){
    			mNumberAreas.put(number, area);
    		}
    		return area;
    	}
    }
    

    public String getNumAreaFromAoraLock(Context context, String number, boolean isRemember){
    	if (!ContactsApplication.sIsGnAreoNumAreaSupport) {
    		return null;
    	}
    	String result = YuloreUtils.getInstance(context).getAreaLock(number);
      	if(TextUtils.isEmpty(result)) { 
			if (!TextUtils.isEmpty(number)
					&& (number.replace(" ", "").length() == 7 || number
							.replace(" ", "").length() == 8)
					&& !number.startsWith("0") && !number.startsWith("400")
					&& !number.startsWith("800")) {
	      		result = ContactsApplication.getInstance().getString(R.string.aurora_local_number);
			} else if (!TextUtils.isEmpty(number)
					&& (number.startsWith("400") || number
							.startsWith("800"))
					&& number.replace(" ", "").length() == 10) {
	      		result = ContactsApplication.getInstance().getString(R.string.aurora_service_number);
	        } else {
	        	result = "";
	      	}
	      	if(result.equalsIgnoreCase("")) {
	  			String[] hotlineInfo = GnHotLinesUtil.getInfo(ContactsApplication.getInstance(), number);
	  			if (null != hotlineInfo) {
	  				result = hotlineInfo[0];
	  			}
	      	}
      	}
      	if(isRemember && result != null){
			mNumberAreas.put(number, result);
      	}
		return result;
    }
}
