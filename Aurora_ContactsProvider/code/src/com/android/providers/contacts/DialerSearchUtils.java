package com.android.providers.contacts;

import com.android.providers.contacts.ContactsDatabaseHelper.PhoneColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.PhoneLookupColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.Tables;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import com.android.providers.contacts.util.PhoneNumberUtils;
import gionee.provider.GnCallLog.Calls;


public class DialerSearchUtils {
	
    public static String computeNormalizedNumber(String number) {
        String normalizedNumber = null;
        if (number != null) {
            normalizedNumber = PhoneNumberUtils.getStrippedReversed(number);
        }
        return normalizedNumber;
    }
    
	public static String stripSpecialCharInNumberForDialerSearch(String number) {
		if (number == null)
	    	return null;
	    int len = number.length();		
		StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < len; i++) {
	    	char c = number.charAt(i);
			if (PhoneNumberUtils.isNonSeparator(c)) {
				sb.append(c);
			//GIONEE: lujian 2012.08.15 modify for "fly CR00678093" begin	
			//} else if (c == ' ' || c == '-') {
			} else if (c=='-'|| c==' ' || c=='('||c==')') {
			//GIONEE: lujian 2012.08.15 modify for "fly CR00678093"   end
				// strip blank and hyphen
			} else {
				break;
			}
	    }
	    return sb.toString();
	}
}
