package com.mediatek.contacts.dialpad;

import android.content.Context;
import android.os.ServiceManager;
import gionee.provider.GnContactsContract.CommonDataKinds.Phone;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;

import com.android.contacts.ContactsApplication;
// Gionee lihuafang 20120422 add for CR00573564 begin
import com.android.contacts.ContactsUtils;
import com.android.contacts.GNContactsUtils;
// Gionee lihuafang 20120422 add for CR00573564 end
import com.android.internal.telephony.ITelephony;

import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import com.mediatek.contacts.util.OperatorUtils;

import java.util.ArrayList;

import com.gionee.internal.telephony.GnITelephony;
import com.android.contacts.AuroraCardTypeUtils;

public class DialerSearchUtils {

    public static String tripHyphen(String number) {
        if (TextUtils.isEmpty(number)) 
            return number;

        String result = number.replaceAll(" ", "");
        result = result.replaceAll("-", "");
        
        return result;
    }

    public static String tripNonDigit(String number) {
        if (TextUtils.isEmpty(number)) 
            return number;

        StringBuilder sb = new StringBuilder();
        int len = number.length();
        
        for (int i = 0; i < len; i++) {
            char c = number.charAt(i);
            if (PhoneNumberUtils.isNonSeparator(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static long getSimType(int indicate) {
        long photoId = 0;
        SIMInfoWrapper simInfo = SIMInfoWrapper.getDefault();
        ITelephony telephony = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
        final int slot = simInfo.getSimSlotById(indicate);

        if (OperatorUtils.getOptrProperties().equals("OP02")) {
            if (slot == 0) {
                return -3;
            } else {
                return -4;
            }
        }
        
        // qc begin
        if (ContactsApplication.isMultiSimEnabled && GNContactsUtils.isOnlyQcContactsSupport()) {
            int nSlot = indicate -1;
            
            if (nSlot == 0) {
                return -3;
            } else {
                return -4;
            }
        }
        // qc end
        
        // Gionee lihuafang 20120422 add for CR00573564 begin
        else if (ContactsUtils.mIsGnContactsSupport && (ContactsUtils.mIsGnShowSlotSupport || ContactsUtils.mIsGnShowDigitalSlotSupport)) {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                if (slot == 0) {
                    return -3;
                } else if (slot == 1) {
                    return -4;
                } else {
                    return 0;
                }
            } else {
                if (slot == 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
        // Gionee lihuafang 20120422 add for CR00573564 end
        else {		
	        try {
	            boolean bUSIM = false;
	            if (FeatureOption.MTK_GEMINI_SUPPORT) {
	                //qc--mtk
	                //if (telephony != null && telephony.getIccCardTypeGemini(slot).equals("USIM")) {
	                if (telephony != null && AuroraCardTypeUtils.getIccCardTypeGemini(telephony, slot).equals("USIM")) {
	                    bUSIM = true;
	                }
	            } else {
	                //qc--mtk
	                //if (telephony != null && telephony.getIccCardType().equals("USIM")) {
	                if (telephony != null && AuroraCardTypeUtils.getIccCardType(telephony).equals("USIM")) {
	                    bUSIM = true;
	                }
	            }

	            if (bUSIM) {
	                photoId = -2;
	            } else {
	                photoId = -1;
	            }

	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	        return photoId;
        }
    }

    public static ArrayList<Integer> adjustHighlitePositionForHyphen(String number,
            String numberMatchedOffsets) {
        ArrayList<Integer> res = new ArrayList<Integer>();
        try {
            int highliteBegin = (int) numberMatchedOffsets.charAt(0);
            int highliteEnd = (int) numberMatchedOffsets.charAt(1);
            if (highliteBegin > highliteEnd || highliteEnd > number.length())
                return res;

            for (int i = 0; i <= highliteBegin; i++) {
                char c = number.charAt(i);
                if (c == '-' || c == ' ') {
                    highliteBegin++;
                    highliteEnd++;
                }
            }

            for (int i = highliteBegin + 1; (i <= highliteEnd && i < number.length()); i++) {
                char c = number.charAt(i);
                if (c == '-' || c == ' ') {
                    highliteEnd++;
                }
            }
			
            if(highliteEnd >= number.length()) 
                highliteEnd = number.length() - 1;	
			
            res.add(highliteBegin);
            res.add(highliteEnd);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return res;
    }
}
