/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.contacts.list;

import com.google.android.collect.Lists;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Directory;
import java.util.List;

/**
 * A loader for use in the default contact list, which will also query for the stars
 * if configured to do so.
 * AURORA::add for starred contacts to contacts list::add::wangth::20130902
 */
public class StarredAndContactsLoader extends CursorLoader {

    private boolean mLoadStarred;
    private String[] mProjection;
    private boolean mPhoneMode = false;
    private int mCount = 0;
    private boolean mAutoRecordMode = false;
    
    public StarredAndContactsLoader(Context context) {
        super(context);
    }
    
    public StarredAndContactsLoader(Context context, boolean phoneMode) {
        super(context);
        mPhoneMode = phoneMode;
    }
    
    public StarredAndContactsLoader(Context context, boolean phoneMode, boolean autoRecordMode) {
        super(context);
        mPhoneMode = phoneMode;
        mAutoRecordMode = autoRecordMode;
    }

    public void setLoadStars(boolean flag) {
        mLoadStarred = flag;
    }

    public void setProjection(String[] projection) {
        super.setProjection(projection);
        mProjection = projection;
    }

    @Override
    public Cursor loadInBackground() {
        // First load the profile, if enabled.
        List<Cursor> cursors = Lists.newArrayList();
        if (mLoadStarred) {
            cursors.add(loadStarred());
        }
        final Cursor contactsCursor = super.loadInBackground();
        cursors.add(contactsCursor);
        return new MergeCursor(cursors.toArray(new Cursor[cursors.size()])) {
            @Override
            public Bundle getExtras() {
                // Need to get the extras from the contacts cursor.
                // Gionee:wangth 20120808 modify for CR00672041 begin
                //return contactsCursor.getExtras();
                if (contactsCursor != null) {
                    return contactsCursor.getExtras();
                } else {
                    return null;
                }
                // Gionee:wangth 20120808 modify for CR00672041 end
            }
        };
    }

    /**
     * Loads the stars into a MatrixCursor.
     */
    private MatrixCursor loadStarred() {
        Cursor cursor = null;
        if (mPhoneMode) {
            Uri uri = Phone.CONTENT_URI.buildUpon().appendQueryParameter(
                    ContactsContract.DIRECTORY_PARAM_KEY, String.valueOf(Directory.DEFAULT))
                    .build();
            String selection = Contacts.STARRED + "=1 AND "
                    + Contacts.IN_VISIBLE_GROUP + "=1" 
                    + " AND " + Contacts.HAS_PHONE_NUMBER + "=1";
            
            if (mAutoRecordMode) {
                selection += " AND auto_record=0";
            }
            
            uri.buildUpon()
                    .appendQueryParameter(Contacts.EXTRA_ADDRESS_BOOK_INDEX, "true").build();
            uri = uri.buildUpon()
                    .appendQueryParameter(ContactsContract.REMOVE_DUPLICATE_ENTRIES, "true")
                    .build();
            
            cursor = getContext().getContentResolver().query(uri, mProjection, selection, null, Phone.SORT_KEY_PRIMARY);
        } else {
            cursor = getContext().getContentResolver().query(Contacts.CONTENT_URI, mProjection,
                    Contacts.STARRED + "!=0 AND " + Contacts.IN_VISIBLE_GROUP + "=1", null, null);
        }
        
        // Gionee:wangth 20120627 add for CR00628726 begin
        if (cursor == null) {
            return null;
        }
        
        mCount = cursor.getCount();
        // Gionee:wangth 20120627 add for CR00628726 end
        try {
            MatrixCursor matrix = new MatrixCursor(mProjection);
            Object[] row = new Object[mProjection.length];
            while (cursor.moveToNext()) {
                for (int i = 0; i < row.length; i++) {
                    row[i] = cursor.getString(i);
                }
                matrix.addRow(row);
            }
            return matrix;
        } finally {
            cursor.close();
        }
    }
    
    public int getCursorCount() {
        return mCount;
    }
}
