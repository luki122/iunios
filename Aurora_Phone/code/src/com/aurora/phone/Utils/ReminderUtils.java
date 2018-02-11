package com.android.phone;


import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.CallLog;
import android.text.TextUtils;
import android.util.Log;
import android.content.Intent;

public class ReminderUtils {
    private static final String LOG_TAG = "ReminderUtils";
    private static final boolean DBG = (PhoneGlobals.DBG_LEVEL >= 2);
    
	public static final Uri CONTENT_URI = Uri.parse("content://com.aurora.reminder");
	public static final String TABLE_NAME = "reminder_data";

	public static final String ID = "_id";
	public static final String TITLE = "title";
	public static final String ACTION = "action";
	public static final String PACKAGE = "package";
	public static final String LEVEL = "level";
	public static final String VISIBLE = "visible";

	public static final String PACKAGE_OWN = "com.aurora.reminder";
	public static final String PACKAGE_PHONE = "com.android.phone";
	
	public static void insertPhoneReminder(String message) {
		if(AuroraPrivacyUtils.getCurrentAccountId() > 0) {
			return;
		}
		ContentValues values = new ContentValues();
		values.put(TITLE, message);
		values.put(ACTION, Intent.ACTION_VIEW);
		values.put(PACKAGE, PACKAGE_PHONE);
		try {
			PhoneGlobals.getInstance().getContentResolver().insert(CONTENT_URI, values);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void updatePhoneReminder() {
		ContentValues values = new ContentValues();
		values.put(VISIBLE, 0);
		try {
			PhoneGlobals.getInstance().getContentResolver().update(CONTENT_URI, values, 
					PACKAGE + "=?", new String[] {PACKAGE_PHONE});
		} catch(Exception e) {
			e.printStackTrace();
		}

	}
	
}
