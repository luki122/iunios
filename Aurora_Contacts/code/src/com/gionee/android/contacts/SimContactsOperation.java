/*
 * Copyright (C) 2011-2012, The Linux Foundation. All rights reserved.

 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above
       copyright notice, this list of conditions and the following
       disclaimer in the documentation and/or other materials provided
       with the distribution.
     * Neither the name of The Linux Foundation nor the names of its
       contributors may be used to endorse or promote products derived
       from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gionee.android.contacts;

import com.android.contacts.GNContactsUtils;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import gionee.provider.GnContactsContract;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.CommonDataKinds;
import gionee.provider.GnContactsContract.CommonDataKinds.Phone;
import gionee.provider.GnContactsContract.CommonDataKinds.Email;
import gionee.provider.GnContactsContract.CommonDataKinds.StructuredName;
import gionee.provider.GnContactsContract.RawContacts;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

public class SimContactsOperation {

    private static final String  TAG = "SimContactsOperation";
    private static final boolean DBG = true;
    private static final int SUB1 = 0;
    private static final int SUB2 = 1;

    static final String[] ACCOUNT_PROJECTION = new String[] {
        RawContacts._ID,
        RawContacts.CONTACT_ID,
        RawContacts.ACCOUNT_NAME,
        RawContacts.ACCOUNT_TYPE,
        RawContacts.INDICATE_PHONE_SIM,
        RawContacts.INDEX_IN_SIM,
    };


    private static final int ACCOUNT_COLUMN_RAW_ID = 0;
    private static final int ACCOUNT_COLUMN_CONTACT_ID = 1;
    private static final int ACCOUNT_COLUMN_NAME = 2;
    private static final int ACCOUNT_COLUMN_TYPE = 3;
    private static final int ACCOUNT_COLUMN_INDICATE_PHONE_SIM = 4;
    private static final int ACCOUNT_COLUMN_INDEX_IN_SIM = 5;


    private static Context mContext;
    private ContentResolver mResolver;
    private ContentValues mValues = new ContentValues();


    public SimContactsOperation(Context context) {
        this.mContext = context;
        this.mResolver = context.getContentResolver();
    }

    private static Cursor setupAccountCursor(long contactId) {
        ContentResolver resolver = mContext.getContentResolver();
        Cursor cursor = resolver.query(RawContacts.CONTENT_URI,
                ACCOUNT_PROJECTION,
                RawContacts.CONTACT_ID + "="
                + Long.toString(contactId), null, null);

        if (cursor.moveToFirst()) {
            // TODO: make sure this is the correct one we are query
            return cursor;
        } else {
            cursor.close();
            return null;
        }
    }

    public static ContentValues getSimAccountValues(long contactId) {
        ContentValues mValues = new ContentValues();
        Cursor cursor = setupAccountCursor(contactId);
        if (cursor == null || cursor.getCount() == 0) {
        	//aurora add by liguangyu for cursor leak
        	if(cursor != null) {
        		cursor.close();
        	}
            mValues.clear();
            return mValues;
        }
        
        long rawContactId = cursor.getLong(cursor.getColumnIndex(RawContacts._ID));
        String index = cursor.getString(ACCOUNT_COLUMN_INDEX_IN_SIM);
        cursor.close();
        mValues.clear();
        String name = getContactItems(rawContactId,
                StructuredName.CONTENT_ITEM_TYPE,
                GnContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME);
        mValues.put(GNContactsUtils.STR_TAG, name);

        String number = getContactPhoneNumber(rawContactId,
                Phone.CONTENT_ITEM_TYPE, String.valueOf(Phone.TYPE_MOBILE),
                GnContactsContract.CommonDataKinds.Phone.DATA);
        mValues.put(GNContactsUtils.STR_NUMBER, number);

//        int sub = getSimSubscription(contactId);
//        if (GNContactsUtils.cardIsUsim(sub)) {
//            String anrs = getContactPhoneNumber(rawContactId,
//                    Phone.CONTENT_ITEM_TYPE, String.valueOf(Phone.TYPE_OTHER),
//                    GnContactsContract.CommonDataKinds.Phone.DATA);
//            mValues.put(GNContactsUtils.STR_ANRS, anrs);
//
//            String emails = getContactItems(rawContactId,
//                    Email.CONTENT_ITEM_TYPE,
//                    GnContactsContract.CommonDataKinds.Email.DATA);
//            mValues.put(GNContactsUtils.STR_EMAILS, emails);
//        }
        
        mValues.put(GNContactsUtils.STR_INDEX, index);

        return mValues;
    }

    public static int getSimSubscription(long contactId) {
        int subscription = -1;
        Cursor cursor = setupAccountCursor(contactId);
        if (cursor == null || cursor.getCount() == 0) {
         	//aurora add by liguangyu for cursor leak
        	if(cursor != null) {
        		cursor.close();
        	}
            subscription = -1;
            return subscription;
        }
        
        int index = cursor.getInt(ACCOUNT_COLUMN_INDICATE_PHONE_SIM);
        subscription = index -1;
        cursor.close();
        
        return subscription;
    }


    private static String getContactItems(long rawContactId, String selectionArg, String columnName) {
        String retval = null;
        Uri baseUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);
        Uri dataUri = Uri.withAppendedPath(baseUri, RawContacts.Data.CONTENT_DIRECTORY);

        Cursor c = mContext.getContentResolver().query(dataUri, null,
                Data.MIMETYPE + "=?", new String[] {selectionArg}, null);
        if (c == null || c.getCount() == 0) {
            if(c != null) {
                c.close();
            }
            return null;
        }
        c.moveToPosition(-1);

        while (c.moveToNext()) {
            retval = c.getString(c.getColumnIndex(columnName));
        }

        c.close();

        return retval;
    }

    private static
    String getContactPhoneNumber(long rawContactId, String selectionArg1, String selectionArg2, String columnName) {
        String retval = null;
        Uri baseUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);
        Uri dataUri = Uri.withAppendedPath(baseUri, RawContacts.Data.CONTENT_DIRECTORY);

        Cursor c = mContext.getContentResolver().query(dataUri, null,
                Data.MIMETYPE + "=? AND " + Phone.TYPE + "=?",
                new String[] {selectionArg1,selectionArg2}, null);
        if (c == null || c.getCount() == 0) {
            if(c != null) {
                c.close();
            }
            return null;
        }
        c.moveToPosition(-1);

        while (c.moveToNext()) {
            retval = c.getString(c.getColumnIndex(columnName));
        }

        c.close();

        return retval;
    }


    private void log(String msg) {
        if (DBG) Log.d(TAG,  msg);
    }

}
