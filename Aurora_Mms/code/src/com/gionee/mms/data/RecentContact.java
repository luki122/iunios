/*
 * Copyright (C) 2012 gionee Inc.
 *
 * Author:gaoj
 *
 * Description:class for holding the data of recent contact data from database
 *
 * history
 * name                              date                                      description
 *
 */

package com.gionee.mms.data;

import java.util.ArrayList;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
//Gionee <zhouyj> <2013-04-20> add for CR00796964 start
import com.gionee.mms.ui.ContactsCacheSingleton;
//Gionee <zhouyj> <2013-04-20> add for CR00796964 end

public class RecentContact {

    private static String TAG = "RecentContact";

    // members used to query data
    private static RecentContactsQueryHandler sRecentContactsQueryHandler;
    public static ArrayList<RecentContactData> sRecentContactCache;

    private final static int RECENT_CONTACTS_MAX_NUM = 20;
    private final static int RECENT_CONTACT_QUERY_TOKEN = 40;
    private final static int RECENT_CONTACT_DEL_TOKEN = 41;
    private static final Uri sRecentContactUri = Uri.parse("content://mms-sms//recent_contact");
    private static final String[] RECENT_CONTACTS_PROJECTION = {
            "_id", "number"
    };

    private final static int CONTACT_ID_COLUMN = 0;
    private final static int CONTACT_NUMBER_COLUMN = 1;

    public static void init(Context context) {
        sRecentContactsQueryHandler = new RecentContactsQueryHandler(context.getContentResolver());
        sRecentContactsQueryHandler.startQuery(RECENT_CONTACT_QUERY_TOKEN, null, sRecentContactUri,
                RECENT_CONTACTS_PROJECTION, null, null, "date DESC");
    }

    public static void insertRecentContact(String[] numbers) {
        if (sRecentContactsQueryHandler == null || sRecentContactCache == null || numbers == null) {
            throw new IllegalStateException(
                    "sRecentContactsQueryHandler, sRecentContactCache or param numbers is null");
        }

        int numberMaxLen = 0;
        if (numbers.length >= RECENT_CONTACTS_MAX_NUM) {
            numberMaxLen = RECENT_CONTACTS_MAX_NUM;
        } else {
            numberMaxLen = numbers.length;
        }

        // first check that the numbers to be inserted whether already exist in
        // database
        for (int index = 0; index < numberMaxLen; index++) {
            String number = numbers[index];
            // number = delCharsFromStr(number, '-');
            for (int count = 0; count < sRecentContactCache.size(); count++) {
                RecentContactData contactData = sRecentContactCache
                        .get(count);
                if (number.equals(contactData.mNumber)) {
                    String selection = "_id=" + contactData.mContactId;
                    sRecentContactsQueryHandler.startDelete(0, null, sRecentContactUri, selection,
                            null);
                    sRecentContactCache.remove(count);
                }
            }
        }

        // then delete the oldest contact data if necessary
        int rowsToDel = numberMaxLen - (RECENT_CONTACTS_MAX_NUM - sRecentContactCache.size());

        if (rowsToDel > 0) {
            for (int index = 0; index < rowsToDel; index++) {
                RecentContactData contactData = sRecentContactCache
                        .get(sRecentContactCache.size() - index - 1);
                String selection = "_id=" + contactData.mContactId;
                sRecentContactsQueryHandler
                        .startDelete(0, null, sRecentContactUri, selection, null);
            }
        }

        // insert numbers to recent_contact database
        for (int index = 0; index < numberMaxLen; index++) {
            ContentValues values = new ContentValues(1);
            values.put("number", numbers[index]);
            sRecentContactsQueryHandler.startInsert(0, null, sRecentContactUri, values);
        }

        // update the cached data:sRecentContactCache
        sRecentContactsQueryHandler.startQuery(RECENT_CONTACT_QUERY_TOKEN, null, sRecentContactUri,
                RECENT_CONTACTS_PROJECTION, null, null, "date DESC");
    }

    // remove chars '-' from the string src
    public static String delCharsFromStr(String src, char c) {
        StringBuilder strBuilder = new StringBuilder(src);
        int len = strBuilder.length();
        for (int step = len - 1; step >= 0; step--) {
            if (strBuilder.charAt(step) == c) {
                strBuilder.deleteCharAt(step);
                step--;
            }
        }
        return strBuilder.toString();
    }

    public static void deleteRecordByNumber(String number) {
        if (number == null && number.length() == 0)
            return;
        //Gionee <zhouyj> <2013-04-20> add for CR00796964 start
        ContactsCacheSingleton.ContactListItemCache contactCache = ContactsCacheSingleton
            .getInstance().getContactItem(number);
        if (contactCache != null) {
            ContactsCacheSingleton.getInstance().getRecentCallLogList().remove(contactCache);
            ContactsCacheSingleton.getInstance().getCallLogList().remove(contactCache);
        }
        //Gionee <zhouyj> <2013-04-20> add for CR00796964 end
        for (int count = 0; count < sRecentContactCache.size(); count++) {
            RecentContactData contactData = sRecentContactCache
                    .get(count);
            if (number.equals(contactData.mNumber)) {
                String selection = "_id=" + contactData.mContactId;
                sRecentContactsQueryHandler
                        .startDelete(0, null, sRecentContactUri, selection, null);
                sRecentContactCache.remove(count);
                break;
            }
        }
    }

    private final static class RecentContactsQueryHandler extends AsyncQueryHandler {
        public RecentContactsQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            switch (token) {
                case RECENT_CONTACT_QUERY_TOKEN:
                    if (sRecentContactCache != null && sRecentContactCache.size() != 0)
                        sRecentContactCache.clear();
                    sRecentContactCache = new ArrayList<RecentContactData>();
                    // Get the latest RECENT_CONTACTS_MAX_NUM conversations and
                    // make
                    // new cursor
                    if (cursor != null) {
                        cursor.moveToPosition(-1);
                        while (cursor.moveToNext()) {
                            RecentContactData itemData = new RecentContactData();
                            itemData.mContactId = cursor.getLong(CONTACT_ID_COLUMN);
                            itemData.mNumber = cursor.getString(CONTACT_NUMBER_COLUMN);

                            sRecentContactCache.add(itemData);
                        }
                        cursor.close();
                    }
                    break;
                default:
                    Log.e(TAG, "onQueryComplete called with unknown token " + token);
            }
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            switch (token) {
                case RECENT_CONTACT_DEL_TOKEN:
                    break;
                default:
                    Log.e(TAG, "onQueryComplete called with unknown token " + token);
            }
        }

    }

    public static class RecentContactData {
        public long mContactId = -1L;
        public String mName = null;
        public String mNumber = null;
    }
}
