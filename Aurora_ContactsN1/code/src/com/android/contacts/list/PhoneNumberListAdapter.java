/*
 * Copyright (C) 2010 The Android Open Source Project
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

import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Directory;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.list.AuroraSimContactListAdapter.PhoneQuery;
import com.android.contacts.list.ContactListAdapter.ContactQuery;
import com.android.contacts.util.NumberAreaUtil;
import com.mediatek.contacts.dialpad.IDialerSearchController.AuroraDialerSearchResultColumns;

/**
 * A cursor adapter for the {@link Phone#CONTENT_TYPE} content type.
 */
public class PhoneNumberListAdapter extends ContactEntryListAdapter {
    private static final String TAG = PhoneNumberListAdapter.class.getSimpleName();

    protected static class PhoneQuery {
        private static final String[] PROJECTION_PRIMARY = new String[] {
            Phone._ID,                          // 0
            Phone.TYPE,                         // 1
            Phone.LABEL,                        // 2
            Phone.NUMBER,                       // 3
            Phone.CONTACT_ID,                   // 4
            Phone.LOOKUP_KEY,                   // 5
            Phone.PHOTO_ID,                     // 6
            Phone.DISPLAY_NAME_PRIMARY,         // 7
            //The following lines are provided and maintained by Mediatek inc.
            RawContacts.INDICATE_PHONE_SIM,     // 8
            //The following lines are provided and maintained by Mediatek inc.
        };

        private static final String[] PROJECTION_ALTERNATIVE = new String[] {
            Phone._ID,                          // 0
            Phone.TYPE,                         // 1
            Phone.LABEL,                        // 2
            Phone.NUMBER,                       // 3
            Phone.CONTACT_ID,                   // 4
            Phone.LOOKUP_KEY,                   // 5
            Phone.PHOTO_ID,                     // 6
            Phone.DISPLAY_NAME_ALTERNATIVE,     // 7
            //The following lines are provided and maintained by Mediatek inc.
            RawContacts.INDICATE_PHONE_SIM,     // 8
            //The following lines are provided and maintained by Mediatek inc.
        };

         
        private static final int PHONE_ID           = 0;
        
        
         
        private static final int PHONE_NUMBER       = 3;
         
        private static final int PHONE_CONTACT_ID   = 4;
        
         
        
        private static final int PHONE_DISPLAY_NAME = 7;
        
    }

    private final CharSequence mUnknownNameText;

    private ContactListItemView.PhotoPosition mPhotoPosition;

    public PhoneNumberListAdapter(Context context) {
        super(context);

        mUnknownNameText = context.getText(android.R.string.unknownName);
    }

    protected CharSequence getUnknownNameText() {
        return mUnknownNameText;
    }

    @Override
    public void configureLoader(CursorLoader loader, long directoryId) {
        Uri uri;

        if (directoryId != Directory.DEFAULT) {
            Log.w(TAG, "PhoneNumberListAdapter is not ready for non-default directory ID ("
                    + "directoryId: " + directoryId + ")");
        }

        if (isSearchMode()) {
            String query = getQueryString();
            Builder builder = Phone.CONTENT_FILTER_URI.buildUpon();
            if (TextUtils.isEmpty(query)) {
                builder.appendPath("");
            } else {
                builder.appendPath(query);      // Builder will encode the query
            }

            builder.appendQueryParameter(ContactsContract.DIRECTORY_PARAM_KEY,
                    String.valueOf(directoryId));
            uri = builder.build();
        } else {
            uri = Phone.CONTENT_URI.buildUpon().appendQueryParameter(
                    ContactsContract.DIRECTORY_PARAM_KEY, String.valueOf(Directory.DEFAULT))
                    .build();
            if (isSectionHeaderDisplayEnabled()) {
                uri = buildSectionIndexerUri(uri);
            }
            configureSelection(loader, directoryId, getFilter());
        }

        // Remove duplicates when it is possible.
        uri = uri.buildUpon()
                .appendQueryParameter(ContactsContract.REMOVE_DUPLICATE_ENTRIES, "true")
                .build();
        loader.setUri(uri);

        // TODO a projection that includes the search snippet
        if (getContactNameDisplayOrder() == ContactsContract.Preferences.DISPLAY_ORDER_PRIMARY) {
            loader.setProjection(PhoneQuery.PROJECTION_PRIMARY);
        } else {
            loader.setProjection(PhoneQuery.PROJECTION_ALTERNATIVE);
        }

        if (getSortOrder() == ContactsContract.Preferences.SORT_ORDER_PRIMARY) {
            loader.setSortOrder(Phone.SORT_KEY_PRIMARY);
        } else {
            loader.setSortOrder(Phone.SORT_KEY_ALTERNATIVE);
        }
    }

    private void configureSelection(
            CursorLoader loader, long directoryId, ContactListFilter filter) {
        if (filter == null || directoryId != Directory.DEFAULT) {
            return;
        }

        final StringBuilder selection = new StringBuilder();
        final List<String> selectionArgs = new ArrayList<String>();

        switch (filter.filterType) {
            case ContactListFilter.FILTER_TYPE_CUSTOM: {
                selection.append(Contacts.IN_VISIBLE_GROUP + "=1");
                selection.append(" AND " + Contacts.HAS_PHONE_NUMBER + "=1");
                break;
            }
            case ContactListFilter.FILTER_TYPE_ACCOUNT: {
                selection.append("(");

                selection.append(RawContacts.ACCOUNT_TYPE + "=?"
                        + " AND " + RawContacts.ACCOUNT_NAME + "=?");
                selectionArgs.add(filter.accountType);
                selectionArgs.add(filter.accountName);
                if (filter.dataSet != null) {
                    selection.append(" AND " + RawContacts.DATA_SET + "=?");
                    selectionArgs.add(filter.dataSet);
                } else {
                    selection.append(" AND " + RawContacts.DATA_SET + " IS NULL");
                }
                selection.append(")");
                break;
            }
            case ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS:
            case ContactListFilter.FILTER_TYPE_DEFAULT:
                break; // No selection needed.
            case ContactListFilter.FILTER_TYPE_WITH_PHONE_NUMBERS_ONLY:
                break; // This adapter is always "phone only", so no selection needed either.
            default:
                Log.w(TAG, "Unsupported filter type came " +
                        "(type: " + filter.filterType + ", toString: " + filter + ")" +
                        " showing all contacts.");
                // No selection.
                break;
        }
        loader.setSelection(selection.toString());
        loader.setSelectionArgs(selectionArgs.toArray(new String[0]));
    }

    protected static Uri buildSectionIndexerUri(Uri uri) {
        return uri.buildUpon()
                .appendQueryParameter(Contacts.EXTRA_ADDRESS_BOOK_INDEX, "true").build();
    }

    @Override
    public String getContactDisplayName(int position) {
        return ((Cursor) getItem(position)).getString(PhoneQuery.PHONE_DISPLAY_NAME);
    }

    /**
     * Builds a {@link Data#CONTENT_URI} for the given cursor position.
     *
     * @return Uri for the data. may be null if the cursor is not ready.
     */
    public Uri getDataUri(int position) {
        Cursor cursor = ((Cursor)getItem(position));
        if (cursor != null) {
            // aurora <wangth> <2013-9-23> modify for aurora ui begin 
            //long id = cursor.getLong(PhoneQuery.PHONE_ID);
            long id = 0;
            if (getIsQueryForDialer()) {
                long contactId = cursor.getLong(AuroraDialerSearchResultColumns.CONTACT_ID_INDEX);
                long rawContactId = 0;
                String number = cursor.getString(AuroraDialerSearchResultColumns.SEARCH_PHONE_NUMBER_INDEX);
                Cursor rawC = ContactsApplication.getInstance().getApplicationContext().getContentResolver().query(
                        RawContacts.CONTENT_URI, 
                        new String[] {"_id"}, 
                        "contact_id = " + contactId, 
                        null, 
                        null);
                if (rawC != null) {
                    if (rawC.moveToFirst()) {
                        rawContactId = rawC.getLong(0);
                        
                        String selection = Data.MIMETYPE + " = '" + Phone.CONTENT_ITEM_TYPE + "' AND " + 
                                Data.RAW_CONTACT_ID + " = " + rawContactId + " AND data1 = " + number;
                        Cursor c = ContactsApplication.getInstance().getApplicationContext().getContentResolver().query(
                                Data.CONTENT_URI, 
                                new String[] {Data._ID}, 
                                selection, 
                                null, 
                                null);
                        if (c != null) {
                            if (c.moveToFirst()) {
                                id = c.getLong(0);
                            }
                            
                            c.close();
                        }
                    }
                    rawC.close();
                }
            } else {
                id = cursor.getLong(PhoneQuery.PHONE_ID);
            }
            //  aurora <wangth> <2013-9-23> modify for aurora ui end
            return ContentUris.withAppendedId(Data.CONTENT_URI, id);
        } else {
            Log.w(TAG, "Cursor was null in getDataUri() call. Returning null instead.");
            return null;
        }
    }

    @Override
    protected View newView(Context context, int partition, Cursor cursor, int position,
            ViewGroup parent) {
        final ContactListItemView view = new ContactListItemView(context, null);
        view.setUnknownNameText(mUnknownNameText);
        view.setQuickContactEnabled(isQuickContactEnabled());
        view.setPhotoPosition(mPhotoPosition);
        // aurora <wangth> <2013-9-23> add for aurora ui begin 
        view.setDoubleRow(true);
        // aurora <wangth> <2013-9-23> add for aurora ui end
        return view;
    }

    @Override
    protected void bindView(View itemView, int partition, Cursor cursor, int position) {
        ContactListItemView view = (ContactListItemView)itemView;

        // Look at elements before and after this position, checking if contact IDs are same.
        // If they have one same contact ID, it means they can be grouped.
        //
        // In one group, only the first entry will show its photo and its name, and the other
        // entries in the group show just their data (e.g. phone number, email address).
        cursor.moveToPosition(position);
        boolean isFirstEntry = true;
        boolean showBottomDivider = true;
        //  aurora <wangth> <2013-11-6> modify for aurora begin
        //final long currentContactId = cursor.getLong(PhoneQuery.PHONE_CONTACT_ID);
        long currentContactId = 0;
        if (getIsQueryForDialer()) {
            currentContactId = cursor.getLong(AuroraDialerSearchResultColumns.CONTACT_ID_INDEX);
        } else {
            currentContactId = cursor.getLong(PhoneQuery.PHONE_CONTACT_ID);
        }
        //  aurora <wangth> <2013-11-6> modify for aurora end

        cursor.moveToPosition(position);


        bindSectionHeaderAndDivider(view, position);
        if (isFirstEntry) {
            bindName(view, cursor);
            
            //Gionee:huangzy 20130131 add for CR00770449 start
//            {
//    	        ImageView photoView = isQuickContactEnabled() ? view.getQuickContact() : view.getPhotoView();
//    	        ContactPhotoManager.setContactPhotoViewTag(photoView, 
//    	        		cursor.getString(PhoneQuery.PHONE_DISPLAY_NAME), position, false);
//            }
//            //Gionee:huangzy 20130131 add for CR00770449 end
//            if (isQuickContactEnabled()) {
//                bindQuickContact(view, partition, cursor, PhoneQuery.PHONE_PHOTO_ID,
//                        PhoneQuery.PHONE_CONTACT_ID, PhoneQuery.PHONE_LOOKUP_KEY);
//            } else {
//                bindPhoto(view, cursor);
//            }
        } else {
            unbindName(view);

            view.removePhotoView(true, false);
        }
        bindPhoneNumber(view, cursor);
        view.setDividerVisible(showBottomDivider);
    }

    protected void bindPhoneNumber(ContactListItemView view, Cursor cursor) {
   
    		gnBindPhoneNumber(view, cursor);
    		return;
    
    	
        
    }

    protected void bindSectionHeaderAndDivider(final ContactListItemView view, int position) {
        if (isSectionHeaderDisplayEnabled()) {
            Placement placement = getItemPlacementInSection(position);
            view.setSectionHeader(placement.firstInSection ? placement.sectionHeader : null);
            view.setDividerVisible(!placement.lastInSection);
        } else {
            view.setSectionHeader(null);
            view.setDividerVisible(true);
        }
    }

    protected void bindName(final ContactListItemView view, Cursor cursor) {
        //  aurora <wangth> <2013-11-6> modify for aurora begin
        //view.showDisplayName(cursor, PhoneQuery.PHONE_DISPLAY_NAME, getContactNameDisplayOrder());
        if (getIsQueryForDialer()) {
            view.auroraShowDisplayName(cursor, AuroraDialerSearchResultColumns.NAME_INDEX, mQueryString);
        } else {
            view.showDisplayName(cursor, PhoneQuery.PHONE_DISPLAY_NAME, getContactNameDisplayOrder());
        }
        //  aurora <wangth> <2013-11-6> modify for aurora end
        // Note: we don't show phonetic names any more (see issue 5265330)
    }

    protected void unbindName(final ContactListItemView view) {
        view.hideDisplayName();
    }

    protected void bindPhoto(final ContactListItemView view, Cursor cursor) {
 
    }

    public void setPhotoPosition(ContactListItemView.PhotoPosition photoPosition) {
        mPhotoPosition = photoPosition;
    }

    public ContactListItemView.PhotoPosition getPhotoPosition() {
        return mPhotoPosition;
    }

    protected void gnBindPhoneNumber(ContactListItemView view, Cursor cursor) {
        // aurora <wangth> <2013-11-6> modify for aurora begin
    	//String number = cursor.getString(PhoneQuery.PHONE_NUMBER);
        String number = null;
        if (getIsQueryForDialer()) {
            number = cursor.getString(AuroraDialerSearchResultColumns.SEARCH_PHONE_NUMBER_INDEX);
        } else {
            number = cursor.getString(PhoneQuery.PHONE_NUMBER);
        }
        // aurora <wangth> <2013-11-6> modify for aurora end
//        CharSequence area = NumberAreaUtil.getNumAreaFromAora(number);
//        view.setLabel(area);
        // aurora <wangth> <2013-11-6> modify for aurora begin
        //view.showData(cursor, PhoneQuery.PHONE_NUMBER);
        if (getIsQueryForDialer()) {
            view.showData(cursor, AuroraDialerSearchResultColumns.SEARCH_PHONE_NUMBER_INDEX);
        } else {
            view.showData(cursor, PhoneQuery.PHONE_NUMBER);
        }
        // aurora <wangth> <2013-11-6> modify for aurora end
    }
}
