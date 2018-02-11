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

package com.gionee.mms.ui;

import java.util.ArrayList;
import android.R.integer;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
//import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog.Calls;
import gionee.provider.GnCallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
//import android.os.Handler;
// Aurora xuyong 2014-10-22 added for bug #9213 start
import android.os.Looper;
// Aurora xuyong 2014-10-22 added for bug #9213 end
import android.os.SystemProperties;
import com.gionee.mms.data.RecentContact;

import com.android.mms.util.PhoneNumberUtils;
import com.android.mms.data.Contact;

import android.util.Log;
import java.util.HashMap;
import java.util.Iterator;

public class ContactsCacheSingleton {
    private static final String TAG = "ContactsCacheSingleton";
    final public static class ContactListItemCache {
        public ArrayList<String> mNames = new ArrayList<String>();
        public String mNumber = null;
        public String mLabel = null;
        public String mCity = null;
        public int mPhoneIndicateOrSim = 0;
        public int mPhotoId = -1;
        public ArrayList<Long> mContactIds = new ArrayList<Long>();
        public boolean mIsChecked = false;
        public int mSortIndex = 0;

        final static int TAG_RECENT_CONTACT = 0x1;
        final static int TAG_PHONEBOOK_CONTACT = 0x2;
        final static int TAG_CALLLOG_CONTACT = 0x4;

        public String getNameString() {
            if (mNames.isEmpty()) {
                return "";
            }

            String retString = "";
            int i = 0;
            for (i = 0; i < mNames.size(); i++) {
                String temp = mNames.get(i);

                if (i == 1) {
                    retString = retString.concat("<");
                }
                retString = retString.concat(temp);

                if (i < mNames.size() - 1) {
                    retString = retString.concat(" ");
                }
            }

            if (i >= 2) {
                retString = retString.concat(">");
            }
            return retString;
        }

        public String toString() {
            String retString = "";

            if (!mNames.isEmpty()) {
                retString += mNames.get(0);
            }

            if (mNumber != null) {
                retString += " " + mNumber;
            }

            return retString;
        }
    }

    private int mSortCount = 0;

    private static final String sDB_FIELD_SIM_ID = "indicate_phone_or_sim_contact";

    private static final String INDICATE_PHONE_SIM = "indicate_phone_or_sim_contact";//Contacts.INDICATE_PHONE_SIM,
    static final String[] PHONE_EMAIL_PROJECTION = new String[] {
        Phone._ID, // 0
        Phone.TYPE, // 1
        Phone.LABEL, // 2
        Phone.NUMBER, // 3
        Phone.DISPLAY_NAME_PRIMARY, // 4
        Phone.DISPLAY_NAME_ALTERNATIVE, // 5
        Phone.CONTACT_ID, // 6
        Phone.PHOTO_ID, // 7
        Phone.PHONETIC_NAME, // 8
        Phone.MIMETYPE, // 9
        INDICATE_PHONE_SIM, // 10
        Phone.STARRED,
    };

    Context mContext = null;

    protected static final int PHONE_ID_COLUMN_INDEX = 0;
    protected static final int PHONE_TYPE_COLUMN_INDEX = 1;
    protected static final int PHONE_LABEL_COLUMN_INDEX = 2;
    protected static final int PHONE_NUMBER_COLUMN_INDEX = 3;
    protected static final int PHONE_PRIMARY_DISPLAY_NAME_COLUMN_INDEX = 4;
    protected static final int PHONE_ALTERNATIVE_DISPLAY_NAME_COLUMN_INDEX = 5;
    protected static final int PHONE_CONTACT_ID_COLUMN_INDEX = 6;
    protected static final int PHONE_PHOTO_ID_COLUMN_INDEX = 7;
    protected static final int PHONE_PHONETIC_NAME_COLUMN_INDEX = 8;
    protected static final int PHONE_MIMETYPE_INDEX = 9;
    protected static final int PHONE_INDICATE_PHONE_SIM_INDEX = 10;

    public static final String CALL_NUMBER_TYPE = "calllognumbertype";
    public static final String CALL_NUMBER_TYPE_ID = "calllognumbertypeid";

    static final String[] CALLLOG_PROJECTION = new String[] {
        Calls._ID,                          // 0
        Calls.NUMBER,                       // 1
        Calls.DATE,                         // 2
        Calls.DURATION,                     // 3
        Calls.TYPE,                         // 4
        Calls.VOICEMAIL_URI,                // 5
        Calls.COUNTRY_ISO,                  // 6
        Calls.GEOCODED_LOCATION,            // 7
        Calls.IS_READ,                      // 8
        // Aurora xuyong 2013-11-15 modified for S4 adapt start
        "simid"/*Calls.SIM_ID*/,                       // 9
        // Aurora xuyong 2013-11-15 modified for S4 adapt end
        GnCallLog.Calls.VTCALL,                       // 10
        GnCallLog.Calls.RAW_CONTACT_ID,               // 11
        GnCallLog.Calls.DATA_ID,                      // 12

        Contacts.DISPLAY_NAME,              // 13
        CALL_NUMBER_TYPE,                   // 14
        CALL_NUMBER_TYPE_ID,                // 15
        Data.PHOTO_ID,                      // 16
        INDICATE_PHONE_SIM,                 //RawContacts.INDICATE_PHONE_SIM,     // 17
        RawContacts.CONTACT_ID,             // 18
        Contacts.LOOKUP_KEY,                // 19
        Data.PHOTO_URI                      // 20
    };

    static final int NUMBER_COLUMN_INDEX = 1;
    static final int DATE_COLUMN_INDEX = 2;
    static final int DURATION_COLUMN_INDEX = 3;
    static final int CALL_TYPE_COLUMN_INDEX = 4;
    static final int CALLER_NAME_COLUMN_INDEX = 5;
    static final int CALLER_NUMBERTYPE_COLUMN_INDEX = 6;
    static final int CALLER_NUMBERLABEL_COLUMN_INDEX = 7;
    static final int CALLER_SIMID_INDEX = 8;
    static final int CALLER_VT_INDEX = 9;
    static final int CALLER_PHOTO_ID_COLUMN_INDEX = 10;
    static final int CALLER_INDICATE_PHONE_SIM_COLUMN_INDEX = 11;

    private ContactsCacheSingleton() {
    }

    ArrayList<String> mCheckedNumbers = null;
    private boolean mIsInited = false;

    public boolean isInited() {
        Log.i(TAG, "isInted" + mIsInited);
        return mIsInited;
    }

    public void init(ArrayList<String> checkedNumbers, Context context) {
        resetCaches();
        
        if (mCheckedNumbers == null) {
            mCheckedNumbers = checkedNumbers;
        }

        if (mContext == null) {
            mContext = context;
        }

        if (mQueryHandler == null) {
            mQueryHandler = new QueryHandler(context);
        }


        mIsInited = true;
        //gionee gaoj 2012-12-4 added for CR00738791 start
        //mCallLogResolver = new DbChangeResolver(new Handler());
        //context.getContentResolver().registerContentObserver(CALL_LOG_URI, true, mCallLogResolver);
        //gionee gaoj 2012-12-4 added for CR00738791 end
    }

    public interface onQueryCompleteListener {
        void onQueryComplete(ArrayList<ContactListItemCache> result);
    }

    private onQueryCompleteListener mQueryComleteListener;
    private boolean mIsQueryComplete = false;

    public void setOnQueryCompleteListener(onQueryCompleteListener onQueryCompleteListener) {
        mQueryComleteListener = onQueryCompleteListener;

        Log.i(TAG, "mQueryComleteListener + mIsQueryComplete " + mQueryComleteListener + " "
                + mIsQueryComplete + " " + mContactAndNumberList.size());
        if (mQueryComleteListener != null && mIsQueryComplete == true) {
            mQueryComleteListener.onQueryComplete(mContactAndNumberList);
        }
    }

    public void starQueryNumbers() {
        mQueryHandler.cancelOperation(PHONEBOOK_QUERY_TOKEN);
        mQueryHandler.cancelOperation(CALLLOG_QUERY_TOKEN);
        //gionee gaoj 2012-6-15 added for CR00623383 start
        mQueryHandler.cancelOperation(STARCONTACTS_QUERY_TOKEN);
        //gionee gaoj 2012-6-15 added for CR00623383 end
        Log.i(TAG, "starQueryNumbers ");
        mIsQueryComplete = false;
        startQueryPhoneBook();
    }

    public final ArrayList<ContactListItemCache> getContactList() {
        return mContactAndNumberList;
    }

    public final ArrayList<ContactListItemCache> getCallLogList() {
        return mCallLogList;
    }

    public final ArrayList<ContactListItemCache> getRecentList() {
        return mRecentList;
    }
    
    //gionee gaoj 2012-6-15 added for CR00623383 start
    public final ArrayList<ContactListItemCache> getStarList() {
        return mStarList;
    }
    //gionee gaoj 2012-6-15 added for CR00623383 start

    //gionee gaoj 2012-5-18 modified for CR00601632 start
    public final ArrayList<ContactListItemCache> getRecentCallLogList() {
        for (int i=0; i<mCallLogList.size(); i++) {
            if (mRecentList.contains(mCallLogList.get(i))) {
                //nothing
            } else {
                mRecentList.add(mCallLogList.get(i));
            }
        }
        return mRecentList;
    }
    //gionee gaoj 2012-5-18 modified for CR00601632 end

    private int mPhoneBookContactCount = 0;
    private int mRecentContactCount = 0;
    private int mCalllogContactCount = 0;

    public int getPhoneBookContactCount() {
        return mContactAndNumberList.size();
    }

    public int getRecentContactCount() {
        return mRecentList.size();
    }

    public int getCalllogContactCount() {
        return mCallLogList.size();
    }

    //gionee gaoj 2012-6-15 added for CR00623383 start
    ArrayList<ContactListItemCache> mStarList = new ArrayList<ContactListItemCache>();
    public int getStarContactCount() {
        return mStarList.size();
    }
    //gionee gaoj 2012-6-15 added for CR00623383 end
    ArrayList<ContactListItemCache> mContactAndNumberList = new ArrayList<ContactListItemCache>();
    ArrayList<ContactListItemCache> mCallLogList = new ArrayList<ContactListItemCache>();
    ArrayList<ContactListItemCache> mRecentList = new ArrayList<ContactListItemCache>();

    HashMap<String, ContactListItemCache> mNumberHashMap = new HashMap<String, ContactListItemCache>();
    HashMap<Long, ArrayList<ContactListItemCache>> mContactIdHashMap = new HashMap<Long, ArrayList<ContactListItemCache>>();

    private static ContactsCacheSingleton mInstance;

    public static ContactsCacheSingleton getInstance() {
        if (mInstance == null) {
            mInstance = new ContactsCacheSingleton();
        }

        return mInstance;
    }

    private void resetCaches() {
        Log.i(TAG, "resetCaches");
        if (mContactAndNumberList != null) {
            mContactAndNumberList.clear();
        }

        if (mCheckedNumbers != null) {
            mCheckedNumbers.clear();
            mCheckedNumbers = null;
        }

        if (mContactIdHashMap != null) {
            mContactIdHashMap.clear();
        }

        if (mNumberHashMap != null) {
            mNumberHashMap.clear();
        }

        if (mCallLogList != null) {
            mCallLogList.clear();
        }

        if (mRecentList != null) {
            mRecentList.clear();
        }

        //gionee gaoj 2012-6-15 added for CR00623383 start
        if (mStarList != null) {
            mStarList.clear();
        }
        //gionee gaoj 2012-6-15 added for CR00623383 end
        mQueryComleteListener = null;

        mIsInited = false;

        mIsQueryComplete = false;
    }

    public static void destoryInstance() {
        Log.i(TAG, "destoryInstance");
        if (mInstance != null) {
            mInstance.resetCaches();
            mInstance = null;
        }
    }

    private QueryHandler mQueryHandler;

    private void getAllPhoneBookNumbers(ContentResolver resolver, Cursor cursor) {
        if (cursor == null || (cursor != null && cursor.getCount() == 0)) {
            return;
        }

        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            String number = cursor.getString(PHONE_NUMBER_COLUMN_INDEX);
            // gionee zhouyj 2013-01-25 add for CR00768084 start 
            if (number == null) number = "";
            // gionee zhouyj 2013-01-25 add for CR00768084 end 
            String name = cursor.getString(PHONE_PRIMARY_DISPLAY_NAME_COLUMN_INDEX);
            long contactId = cursor.getLong(PHONE_CONTACT_ID_COLUMN_INDEX);
            //gionee gaoj 2012-7-27 modified for CR00657995 start
            ContactListItemCache contactItem = null;
            if (number != null) {
                number = number.replaceAll(" ", "");
                number = number.replaceAll("-", "");
                contactItem = getContactItem(number);
            }
            //gionee gaoj 2012-7-27 modified for CR00657995 end
            if (contactItem != null) {
                contactItem.mContactIds.add(contactId);

                if (TextUtils.isEmpty(name)) {
                    break;
                }

                boolean isDuplicateName = false;
                for (String tempName : contactItem.mNames) {
                    if (tempName.equals(name)) {
                        isDuplicateName = true;
                    }
                }

                if (isDuplicateName == false) {
                    contactItem.mNames.add(name);
                }
                ArrayList<ContactListItemCache> cacheTemp = mContactIdHashMap.get(contactItem.mContactIds.get(0));
                mContactIdHashMap.put(contactId, cacheTemp);
            } else {
                ContactListItemCache contact = new ContactListItemCache();
                contact.mNumber = number;
                if (!TextUtils.isEmpty(name)) {
                    contact.mNames.add(name);
                }
                contact.mContactIds.add(contactId);

                addContactAndNumber(contact);
            }
        }
    }

    private void getAllRecentMsgNumbers() {
        for (RecentContact.RecentContactData recentContact : RecentContact.sRecentContactCache) {
            ContactsCacheSingleton.ContactListItemCache contactCache = ContactsCacheSingleton
                    .getInstance().getContactItem(recentContact.mNumber);

            if (contactCache == null) {

                contactCache = new ContactsCacheSingleton.ContactListItemCache();


                contactCache.mNumber = recentContact.mNumber;
                mRecentList.add(contactCache);
                mNumberHashMap.put(contactCache.mNumber, contactCache);
            } else {
                mRecentList.add(contactCache);
            }
        }
    }


    private void getAllCalllogNumbers(ContentResolver resolver, Cursor cursor) {
        if (cursor == null || (cursor != null && cursor.getCount() == 0)) {
            return;
        }

        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            String number = cursor.getString(NUMBER_COLUMN_INDEX);
            // gionee zhouyj 2013-01-10 add for CR00757434 start 
            if (number != null && ("-1".equals(number) ) || "*".equals(number)) {
                continue ;
            }
            // gionee zhouyj 2013-01-10 add for CR00757434 end 

            ContactsCacheSingleton.ContactListItemCache contactCache = ContactsCacheSingleton
                    .getInstance().getContactItem(number);

            if (contactCache == null) {
                contactCache = new ContactsCacheSingleton.ContactListItemCache();
                contactCache.mNumber = number;

                mCallLogList.add(contactCache);
                mNumberHashMap.put(number, contactCache);
            } else {
                mCallLogList.add(contactCache);
            }
        }
    }

    //gionee gaoj 2012-6-15 added for CR00623383 start
    private void getAllStarNumbers(ContentResolver resolver, Cursor cursor) {
        if (cursor == null || (cursor != null && cursor.getCount() == 0)) {
            return;
        }

        cursor.moveToPosition(-1);
        ArrayList<ContactsCacheSingleton.ContactListItemCache> children = new ArrayList<ContactsCacheSingleton.ContactListItemCache>();
        while (cursor.moveToNext()) {
            String number = cursor.getString(PHONE_NUMBER_COLUMN_INDEX);

            ContactsCacheSingleton.ContactListItemCache contactCache = ContactsCacheSingleton
                    .getInstance().getContactItem(number);

            if (contactCache == null) {
                contactCache = new ContactsCacheSingleton.ContactListItemCache();
                contactCache.mNumber = number;

                mStarList.add(contactCache);
                mNumberHashMap.put(number, contactCache);
            } else {
                mStarList.add(contactCache);
            }
        }
    }
    //gionee gaoj 2012-6-15 added for CR00623383 end

    private class QueryHandler extends AsyncQueryHandler {
        Context mContext;

        public QueryHandler(Context context) {
            super(context.getContentResolver());
            mContext = context;
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {

            if (mInstance == null) {
                Log.i(TAG, "onQueryComplete mInstance == null");
                if (cursor != null) {
                    cursor.close();
                }
                return;
            }

            if (token == PHONEBOOK_QUERY_TOKEN) {
                if (cursor != null) {
                    if (cursor.getCount() != 0) {

                        // find all numbers
                        getAllPhoneBookNumbers(mContext.getContentResolver(), cursor);
                    }
                    cursor.close();
                }

                startQueryCalllog();
            } else if (token == CALLLOG_QUERY_TOKEN) {
                if (cursor != null) {
                    if (cursor.getCount() != 0) {

                        // find all numbers
                        getAllCalllogNumbers(mContext.getContentResolver(), cursor);
                    }
                    cursor.close();
                }

                //gionee gaoj 2012-6-15 added for CR00623383 start
                startQueryFavorite();
                //gionee gaoj 2012-6-15 added for CR00623383 end
            } else if (token == STARCONTACTS_QUERY_TOKEN) {
                if (cursor != null) {
                    if (cursor.getCount() != 0) {
                        getAllStarNumbers(mContext.getContentResolver(), cursor);
                    }
                    cursor.close();
                }
                getAllRecentMsgNumbers();

                mIsQueryComplete = true;
                setCheckedNumber(mCheckedNumbers);
                Log.i(TAG, "starQueryNumbers end");
                if (mQueryComleteListener != null) {
                    mQueryComleteListener.onQueryComplete(mContactAndNumberList);
                }
            }
        }
    }

    private static final int CALLLOG_QUERY_TOKEN = 43;

    private static final int PHONEBOOK_QUERY_TOKEN = 42;

    public static final Uri PICK_PHONE_EMAIL_URI = Uri
            .parse("content://com.android.contacts/data/phone_email");
    private static final String CLAUSE_ONLY_VISIBLE = Contacts.IN_VISIBLE_GROUP + "=1";

    void startQueryPhoneBook() {

        // Cancel any pending queries
        mQueryHandler.cancelOperation(PHONEBOOK_QUERY_TOKEN);

        // Kick off the new query
        mQueryHandler.startQuery(PHONEBOOK_QUERY_TOKEN, null, PICK_PHONE_EMAIL_URI,
                PHONE_EMAIL_PROJECTION, null, null, Contacts.SORT_KEY_PRIMARY);
    }

    void startQueryCalllog() {
        mQueryHandler.startQuery(CALLLOG_QUERY_TOKEN, null,
                Uri.parse("content://call_log/gncallsjoindataview"), CALLLOG_PROJECTION, null, null,
                Calls.DEFAULT_SORT_ORDER);
    }

    //gionee gaoj 2012-6-15 added for CR00623383 start
    private static final int STARCONTACTS_QUERY_TOKEN = 45;

    void startQueryFavorite() {
        mQueryHandler.startQuery(STARCONTACTS_QUERY_TOKEN, null, PICK_PHONE_EMAIL_URI,
                PHONE_EMAIL_PROJECTION, "starred = 1", null, Contacts.SORT_KEY_PRIMARY);
    }
    //gionee gaoj 2012-6-15 added for CR00623383 end

    public ArrayList<String> getCheckedNumbers() {
        ArrayList<String> retList = new ArrayList<String>();

        Iterator<ContactListItemCache> iterator = mNumberHashMap.values().iterator();
        ContactListItemCache cache = null;
        while (iterator != null && iterator.hasNext() && ((cache = iterator.next()) != null)) {
            String str = "";
            if (cache.mIsChecked == true) {
                String name = null;
                if (!cache.mContactIds.isEmpty()) {
                    if (!cache.mNames.isEmpty()) {
                        name = cache.mNames.get(0);
                        str = name + ":" + cache.mNumber;
                    } else {
                        str = cache.mNumber;
                    }
                }
                if (!TextUtils.isEmpty(name) && !name.equals(cache.mNumber)) {
                    str = name + ":" + cache.mNumber;
                } else {
                    str = cache.mNumber;
                }

                retList.add(str);
            }

        }
        
        if (mCheckedNumbers != null && mCheckedNumbers.size() != 0) {
            for (String temp : mCheckedNumbers) {
                if (getContactItem(temp) == null) {
                    retList.add(temp);
                }
            }
        }
        return retList;
    }

    public int getCheckedCount() {
        int retValue = 0;
        Iterator<ContactListItemCache> iterator = mNumberHashMap.values().iterator();
        ContactListItemCache temp = null;
        while (iterator != null && iterator.hasNext() && ((temp = iterator.next()) != null)) {
            if (temp.mIsChecked) {
                retValue++;
            }
        }

        return retValue;
    }

    //gionee gaoj 2012-12-5 added for CR00738917 start
    public void setCheckFalse(String number) {
        ContactListItemCache cache = mNumberHashMap.get(number.replace(" ", ""));
        if (cache != null) {
            cache.mIsChecked = false;
        }
    }
    //gionee gaoj 2012-12-5 added for CR00738917 end

    public ContactListItemCache getContactItem(String number) {
        return mNumberHashMap.get(number.replace(" ", ""));
    }

    // the item must contain the number and name
    public ContactListItemCache addContactAndNumber(ContactListItemCache item) {

        Long contactId = item.mContactIds.get(item.mContactIds.size() - 1);

        ArrayList<ContactListItemCache> cacheTemp = mContactIdHashMap.get(contactId);

        if (cacheTemp == null) {
            cacheTemp = new ArrayList<ContactListItemCache>();
            cacheTemp.add(item);
            mContactIdHashMap.put(contactId, cacheTemp);
        } else {
            cacheTemp.add(item);
        }
        mNumberHashMap.put(item.mNumber.replace(" ", ""), item);
        mContactAndNumberList.add(item);
        
        item.mSortIndex = mSortCount;
        mSortCount++;
        return item;
    }

    public void setCheckedNumber(ArrayList<String> checkedNumbers) {
        // gionee zhouyj 2012-04-20 add for CR00573920 start
        clearItemsChecked();
        // gionee zhouyj 2012-04-20 add for CR00573920 end
        if (checkedNumbers == null || (checkedNumbers != null && checkedNumbers.isEmpty())) {
            
            if (mCheckedNumbers != null) {
                mCheckedNumbers.clear();
                mCheckedNumbers = null;
            }
            return;
        }
        
        if (mCheckedNumbers == null) {
            mCheckedNumbers = checkedNumbers;
        }
        
        for (String temp : checkedNumbers) {
            ContactListItemCache cache = mNumberHashMap.get(temp.replace(" ", ""));
            if (cache != null) {
                cache.mIsChecked = true;
            }
        }
    }
    
    // gionee zhouyj 2012-04-20 add for CR00573920 start
    private void clearItemsChecked(){
        Iterator<ContactListItemCache> iterator = mNumberHashMap.values().iterator();
        ContactListItemCache cache = null;
        while (iterator != null && iterator.hasNext() && ((cache = iterator.next()) != null)) {
            cache.mIsChecked = false;
        }
        //Gionee <zhouyj> <2013-05-10> add for CR00810699 begin
        if (mCheckedNumbers != null) {
            mCheckedNumbers.clear();
            mCheckedNumbers = null;
        }
        //Gionee <zhouyj> <2013-05-10> add for CR00810699 end
    }
    // gionee zhouyj 2012-04-20 add for CR00573920 end

    public void removeContactAndNumber(ContactListItemCache item) {

        if (mContactAndNumberList.contains(item)) {
            mContactAndNumberList.remove(item);

            return;
        }

        ContactListItemCache contactItem = getContactItem(item.mNumber);

        if (contactItem != null) {
            mContactAndNumberList.remove(contactItem);
        }
    }

    public void getContactsAndNumber(long contactId, ArrayList<ContactListItemCache> retList) {
        ArrayList<ContactListItemCache> temp = mContactIdHashMap.get(contactId);

        if (temp != null) {
            for (ContactListItemCache tempCache : temp) {
                retList.add(tempCache);
            }
        }
    }

    //gionee gaoj 2012-12-4 added for CR00738791 start
    /*private DbChangeResolver mCallLogResolver = null;
    private boolean isCallLogChang = false;
    private static final Uri CALL_LOG_URI = Uri.parse("content://call_log/calls");
    class DbChangeResolver extends ContentObserver {

        public DbChangeResolver(Handler handler) {
            super(handler);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void onChange(boolean selfChange) {
            // TODO Auto-generated method stub
            super.onChange(selfChange);
            if (mQueryHandler != null) {
                mQueryHandler.removeCallbacks(mQueryRunnable);
                mQueryHandler.postDelayed(mQueryRunnable, 300);
            }
        }
    }

    private Runnable mQueryRunnable = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            isCallLogChang = true;
            if (mCallLogList != null) {
                mCallLogList.clear();
            }
            mQueryHandler.cancelOperation(CALLLOG_QUERY_TOKEN);
            startQueryCalllog();
        }
    };*/
    //gionee gaoj 2012-12-4 added for CR00738791 end
}