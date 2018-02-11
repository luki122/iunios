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

import com.android.contacts.R;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import gionee.provider.GnContactsContract;
import gionee.provider.GnContactsContract.CommonDataKinds.Phone;
import gionee.provider.GnContactsContract.ContactCounts;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.Directory;
import gionee.provider.GnContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraTextView;

import java.util.ArrayList;
import java.util.List;

import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.list.AuroraSimContactListAdapter.EmailQuery;
import com.android.contacts.list.AuroraSimContactListAdapter.PhoneQuery;
import com.android.contacts.list.ContactListAdapter.ContactQuery;
import com.android.contacts.util.DensityUtil;
import com.android.contacts.util.NumberAreaUtil;
import com.android.contacts.widget.IndexerListAdapter.Placement;
import com.mediatek.contacts.dialpad.IDialerSearchController.GnDialerSearchResultColumns;

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

        public static final int PHONE_ID           = 0;
        public static final int PHONE_TYPE         = 1;
        public static final int PHONE_LABEL        = 2;
        public static final int PHONE_NUMBER       = 3;
        public static final int PHONE_CONTACT_ID   = 4;
        public static final int PHONE_LOOKUP_KEY   = 5;
        public static final int PHONE_PHOTO_ID     = 6;
        public static final int PHONE_DISPLAY_NAME = 7;
        //The following lines are provided and maintained by Mediatek inc.
        public static final int PHONE_INDICATE_PHONE_SIM_COLUMN_INDEX = 8;
        //The following lines are provided and maintained by Mediatek inc.
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

            builder.appendQueryParameter(GnContactsContract.DIRECTORY_PARAM_KEY,
                    String.valueOf(directoryId));
            uri = builder.build();
        } else {
            uri = Phone.CONTENT_URI.buildUpon().appendQueryParameter(
                    GnContactsContract.DIRECTORY_PARAM_KEY, String.valueOf(Directory.DEFAULT))
                    .build();
            if (isSectionHeaderDisplayEnabled()) {
                uri = buildSectionIndexerUri(uri);
            }
            configureSelection(loader, directoryId, getFilter());
        }

        // Remove duplicates when it is possible.
        uri = uri.buildUpon()
                .appendQueryParameter(GnContactsContract.REMOVE_DUPLICATE_ENTRIES, "true")
                .build();
        loader.setUri(uri);

        // TODO a projection that includes the search snippet
        if (getContactNameDisplayOrder() == GnContactsContract.Preferences.DISPLAY_ORDER_PRIMARY) {
            loader.setProjection(PhoneQuery.PROJECTION_PRIMARY);
        } else {
            loader.setProjection(PhoneQuery.PROJECTION_ALTERNATIVE);
        }

        if (getSortOrder() == GnContactsContract.Preferences.SORT_ORDER_PRIMARY) {
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
                .appendQueryParameter(ContactCounts.ADDRESS_BOOK_INDEX_EXTRAS, "true").build();
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
                long contactId = cursor.getLong(GnDialerSearchResultColumns.CONTACT_ID_INDEX);
                long rawContactId = 0;
                String number = cursor.getString(GnDialerSearchResultColumns.PHONE_NUMBER_INDEX);
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
        /*final ContactListItemView view = new ContactListItemView(context, null);
        view.setUnknownNameText(mUnknownNameText);
        view.setQuickContactEnabled(isQuickContactEnabled());
        view.setPhotoPosition(mPhotoPosition);
        // aurora <wangth> <2013-9-23> add for aurora ui begin 
        view.setDoubleRow(true);
        // aurora <wangth> <2013-9-23> add for aurora ui end
        return view;*/
    	
    	ContactListItemView view = new ContactListItemView(context, null);
        view.setUnknownNameText(mUnknownNameText);
        view.setQuickContactEnabled(isQuickContactEnabled());
        view.setActivatedStateSupported(isSelectionVisible());
        
        View v = (View) LayoutInflater.from(context).inflate(
                com.aurora.R.layout.aurora_slid_listview, null);
        RelativeLayout mainUi = (RelativeLayout) v
                .findViewById(com.aurora.R.id.aurora_listview_front);
        mainUi.addView(view, 0, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        
        ImageView iv = (ImageView) v.findViewById(com.aurora.R.id.aurora_listview_divider);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DensityUtil.dip2px(context,0.3F));
        layoutParams.setMargins(DensityUtil.dip2px(context,72),0,DensityUtil.dip2px(context,24),0);
        iv.setLayoutParams(layoutParams);
        iv.setVisibility(View.VISIBLE);
        
        View paddingView=v.findViewById(com.aurora.R.id.control_padding);
		paddingView.setPadding(0, 0, 0, 0);
        
//        CheckBox cb=(CheckBox) v.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
//        AuroraListView.setCheckBoxMaringRightIfHasIndex(cb);

        
        return v;
    }
    
    protected void auroraBindSectionHeaderAndDivider(View view, int position,
            Cursor cursor) {
        LinearLayout headerUi = (LinearLayout) view
                .findViewById(com.aurora.R.id.aurora_list_header);
        if (isSectionHeaderDisplayEnabled()) {
            Placement placement = getItemPlacementInSection(position);
            ViewGroup.LayoutParams params = headerUi.getLayoutParams();
            if (placement.sectionHeader != null) {
                params.height = mContext.getResources().getDimensionPixelSize(
                        R.dimen.aurora_edit_group_margin_top);
                AuroraTextView tv = new AuroraTextView(mContext);
                tv.setText(placement.sectionHeader);
                int paddingLeft = mContext.getResources()
                        .getDimensionPixelSize(
                                R.dimen.aurora_group_entrance_left_margin);
//                if (placement.sectionHeader.equals("#")) {
                    tv.setTextAppearance(mContext, R.style.aurora_list_header_style);
                    tv.setPadding(paddingLeft, 0, 0, ContactsUtils.CONTACT_LIST_HEADER_PADDING_BOTTOM);
//                } else {
//                    tv.setTextSize(15);
//                    tv.setPadding(paddingLeft, 0, 0, ContactsUtils.CONTACT_LIST_HEADER_PADDING_BOTTOM);
//                    if (placement.sectionHeader.equals("Q")) {
//                        tv.setPadding(paddingLeft, 0, 0, 0);
//                    }
//                }
                
                tv.setHeight(params.height);
                tv.setGravity(Gravity.CENTER_VERTICAL);
                tv.setBackgroundColor(mContext.getResources().getColor(R.color.contact_list_header_background_color));
                if (headerUi != null) {
                    headerUi.removeAllViews();
                }
                headerUi.setEnabled(false);
                headerUi.setClickable(false);
                headerUi.addView(tv);
                headerUi.setLayoutParams(params);
            }
        }
        
        if (headerUi != null) {
            if (!isSearchMode()) {
                headerUi.setVisibility(View.VISIBLE);
            } else {
                headerUi.setVisibility(View.GONE);
            }
        }
        
        LinearLayout deleteUi = (LinearLayout) view
                .findViewById(com.aurora.R.id.aurora_listview_back);
        ViewGroup.LayoutParams param = deleteUi.getLayoutParams();
        param.width = mContext.getResources().getDimensionPixelSize(
                R.dimen.aurora_list_item_delete_back_width);
        deleteUi.setLayoutParams(param);
    }
    
    public String getNumber(int position) {
        if (getIsQueryForDialer()) {
            return ((Cursor) getItem(position)).getString(GnDialerSearchResultColumns.PHONE_NUMBER_INDEX);
        }
        return ((Cursor) getItem(position)).getString(
        		false ? EmailQuery.EMAIL_ADDRESS : PhoneQuery.PHONE_NUMBER);
    } 
    
    public long getDataId(int position) {
        Cursor cursor = (Cursor) getItem(position);
        if (cursor == null) {
            return 0;
        }
        
        if (getIsQueryForDialer()) {
            int contactId = cursor.getInt(1);
            String number = getNumber(position);
            int rawContactId = ContactsUtils.queryForRawContactId(mContext.getContentResolver(), contactId);
            
            Cursor c = mContext.getContentResolver().query(
                    Data.CONTENT_URI, 
                    new String[] {"_id"}, 
                    "raw_contact_id=" + rawContactId + " and mimetype_id=5 and data1='" + number + "'", 
                    null, 
                    null);
            
            long dataId = 0;
            if (c != null) {
                if (c.moveToFirst()) {
                    dataId = c.getLong(0);
                }
                
                c.close();
            }
            
            return dataId;
        }
        
        return cursor.getLong(false ? EmailQuery.EMAIL_ID : PhoneQuery.PHONE_ID);
    }

    @Override
    protected void bindView(View itemView, int partition, Cursor cursor, int position) {
    	Log.d("liyang","bindview22");
    	
    	 final RelativeLayout mainUi = (RelativeLayout)itemView.findViewById(com.aurora.R.id.aurora_listview_front);
         final AuroraCheckBox checkBox = (AuroraCheckBox) itemView.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
         final ContactListItemView view = (ContactListItemView)(mainUi.getChildAt(0));
         //ContactListItemView view = (ContactListItemView)itemView;
         
         auroraBindSectionHeaderAndDivider(itemView, position, cursor);
         LinearLayout contentUi = (LinearLayout) itemView.findViewById(com.aurora.R.id.content);
//         AuroraListView.auroraGetAuroraStateListDrawableFromIndex(contentUi, position);
         
//         view.setHighlightedPrefix(isSearchMode() ? getUpperCaseQueryString()
//                 : null);
         
         mainUi.removeViewAt(0);
         mainUi.addView(view, 0);
         
         view.auroraSetCheckable(false);
         
         View moveUi = view.getNameTextView();
         
         
//         int privacyId = 0;
//         if (!mIsEmailSelectMode) {
//         	privacyId = getPrivacyId(position);
//         }
         
         if (true) {
         	Log.d(TAG, "bindview1");
             view.setNameTextViewStyle(true, true);
             
             String name = getContactDisplayName(position);
             String number = getNumber(position);
//             Log.d(TAG, "name:"+name+" number:"+number);
             if (null != number) {
                 number = number.replaceAll(" ", "");
             }
             
             long dataId = getDataId(position);
             boolean checked = false;
             if (getCheckedItem() != null
                     && getCheckedItem().get(dataId) != null) {
                 checked = true;
             }
             
             

             checkBox.setChecked(checked);
         } else {
//         	Log.d(TAG, "bindview2");
             view.setNameTextViewStyle(false, true);
             view.setCheckable(false);
             if (getNeedAnim()) {
                 AuroraListView.auroraStartCheckBoxDisappearingAnim(moveUi, checkBox);
             } else {
                 AuroraListView.auroraSetCheckBoxVisible(moveUi, checkBox, false);
             }
         }

         {
 	        ImageView photoView = isQuickContactEnabled() ? view.getQuickContact() : view.getPhotoView();
 	        ContactPhotoManager.setContactPhotoViewTag(photoView, 
 	        		cursor.getString(ContactQuery.CONTACT_DISPLAY_NAME), position, false);
         }
         //Gionee:huangzy 20130131 add for CR00770449 end
         if (isQuickContactEnabled()) {
             bindQuickContact(view, partition, cursor, ContactQuery.CONTACT_PHOTO_ID,
                     ContactQuery.CONTACT_ID, ContactQuery.CONTACT_LOOKUP_KEY);
         } else {
             bindPhoto(view, partition, cursor);
         }
         
         bindName(view, cursor);
         bindPhoneNumber(view, cursor);
         
         
         if (getNeedAnim()) {
             mHandler.sendMessage(mHandler.obtainMessage());
         }
         
         
         
        /*ContactListItemView view = (ContactListItemView)itemView;

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
            currentContactId = cursor.getLong(GnDialerSearchResultColumns.CONTACT_ID_INDEX);
        } else {
            currentContactId = cursor.getLong(PhoneQuery.PHONE_CONTACT_ID);
        }
        //  aurora <wangth> <2013-11-6> modify for aurora end
//        if (cursor.moveToPrevious() && !cursor.isBeforeFirst()) {
//            // aurora <wangth> <2013-11-6> modify for aurora begin
//            //final long previousContactId = cursor.getLong(PhoneQuery.PHONE_CONTACT_ID);
//            long previousContactId = 0;
//            if (getIsQueryForDialer()) {
//                previousContactId = cursor.getLong(GnDialerSearchResultColumns.CONTACT_ID_INDEX);
//            } else {
//                previousContactId = cursor.getLong(PhoneQuery.PHONE_CONTACT_ID);
//            }
//            //  aurora <wangth> <2013-11-6> modify for aurora end
//            if (currentContactId == previousContactId) {
//                isFirstEntry = false;
//            }
//        }
        cursor.moveToPosition(position);
//        if (cursor.moveToNext() && !cursor.isAfterLast()) {
//            final long nextContactId = cursor.getLong(PhoneQuery.PHONE_CONTACT_ID);
//            if (currentContactId == nextContactId) {
//                // The following entry should be in the same group, which means we don't want a
//                // divider between them.
//                // TODO: we want a different divider than the divider between groups. Just hiding
//                // this divider won't be enough.
//                showBottomDivider = false;
//            }
//        }
//        cursor.moveToPosition(position);

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
        view.setDividerVisible(showBottomDivider);*/
    }
    
    protected void bindPhoto(final ContactListItemView view, int partitionIndex, Cursor cursor) {
        if (!isPhotoSupported(partitionIndex)) {
            view.removePhotoView();
            return;
        }

//        Log.d(TAG,"bindPhoto1");
        // Set the photo, if available
        long photoId = 0;
        if (!cursor.isNull(PhoneQuery.PHONE_PHOTO_ID)) {
            photoId = cursor.getLong(PhoneQuery.PHONE_PHOTO_ID);
//            Log.d(TAG,"photoId:"+photoId);
            
        }

        
        /*
         * Bug Fix by Mediatek Begin.
         *   Original Android隆's code:
         *     xxx
         *   CR ID: ALPS00110185
         *   Descriptions: 颅
         */
//        int indicatePhoneSim = cursor.getInt(ContactQuery.CONTACT_INDICATE_PHONE_SIM);
//		if (indicatePhoneSim > 0) {
//			photoId = getSimType(indicatePhoneSim);
//		}
		/*
	     * Bug Fix by Mediatek End.
	     */

		
        if (photoId != 0) {
        	// gionee xuhz 20121208 modify for GIUI2.0 start
        	if (ContactsApplication.sIsGnGGKJ_V2_0Support && ContactsApplication.sIsGnDarkStyle) {
        		getPhotoLoader().loadPhoto(view.getPhotoView(), photoId, false, true);
        	} else {
//        		Log.d(TAG,"photoLoader:"+getPhotoLoader());
        		getPhotoLoader().loadPhoto(view.getPhotoView(), photoId, false, false);
        	}
        	// gionee xuhz 20121208 modify for GIUI2.0 end
        } else {
//            final String photoUriString = cursor.getString(ContactQuery.CONTACT_PHOTO_URI);
//            final Uri photoUri = photoUriString == null ? null : Uri.parse(photoUriString);
//        	// gionee xuhz 20121208 modify for GIUI2.0 start
//        	if (ContactsApplication.sIsGnGGKJ_V2_0Support && ContactsApplication.sIsGnDarkStyle) {
//                getPhotoLoader().loadPhoto(view.getPhotoView(), photoUri, false, true);
//        	} else {
//                getPhotoLoader().loadPhoto(view.getPhotoView(), photoUri, false, false);
//        	}
//        	// gionee xuhz 20121208 modify for GIUI2.0 end
        	view.getPhotoView().setImageDrawable(mContext.getResources().getDrawable(R.drawable.svg_dial_default_photo1));
        	view.getPhotoView().setVisibility(View.INVISIBLE);
        	
        }
    }

    protected void bindPhoneNumber(ContactListItemView view, Cursor cursor) {
    	if (ContactsApplication.sIsGnContactsSupport) {
    		gnBindPhoneNumber(view, cursor);
    		return;
    	}
    	
        CharSequence label = null;
        if (!cursor.isNull(PhoneQuery.PHONE_TYPE)) {
            final int type = cursor.getInt(PhoneQuery.PHONE_TYPE);
            final String customLabel = cursor.getString(PhoneQuery.PHONE_LABEL);

            // TODO cache
            label = Phone.getTypeLabel(getContext().getResources(), type, customLabel);
        }
        view.setLabel(label);
        view.showData(cursor, PhoneQuery.PHONE_NUMBER);
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
            view.auroraShowDisplayName(cursor, GnDialerSearchResultColumns.NAME_INDEX, mQueryString);
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
        // aurora <wangth> <2013-11-7> modify for aurora begin 
        if (ContactsUtils.mIsGnContactsSupport) {
            return;
        }
        // aurora <wangth> <2013-11-7> modify for aurora end
        long photoId = 0;
        if (!cursor.isNull(PhoneQuery.PHONE_PHOTO_ID)) {
            photoId = cursor.getLong(PhoneQuery.PHONE_PHOTO_ID);
        }
        /*
         * Bug Fix by Mediatek Begin.
         *   Original Android's code:
         *     
         *   CR ID: ALPS00112776
         *   Descriptions: sim card icon display not right
         */
        int indicatePhoneSim = cursor.getInt(PhoneQuery.PHONE_INDICATE_PHONE_SIM_COLUMN_INDEX);
        if (indicatePhoneSim > 0) {
            photoId = getSimType(indicatePhoneSim);
        }
        /*
         * Bug Fix by Mediatek End.
         */

        getPhotoLoader().loadPhoto(view.getPhotoView(), photoId, false, false);
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
            number = cursor.getString(GnDialerSearchResultColumns.PHONE_NUMBER_INDEX);
        } else {
            number = cursor.getString(PhoneQuery.PHONE_NUMBER);
        }
        // aurora <wangth> <2013-11-6> modify for aurora end
//        CharSequence area = NumberAreaUtil.getNumAreaFromAora(number);
//        view.setLabel(area);
        // aurora <wangth> <2013-11-6> modify for aurora begin
        //view.showData(cursor, PhoneQuery.PHONE_NUMBER);
        if (getIsQueryForDialer()) {
            view.showData(cursor, GnDialerSearchResultColumns.PHONE_NUMBER_INDEX);
        } else {
            view.showData(cursor, PhoneQuery.PHONE_NUMBER);
        }
        // aurora <wangth> <2013-11-6> modify for aurora end
    }
}
