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

import java.util.HashMap;

import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.R;
import com.android.contacts.ResConstant;
import com.android.contacts.list.AuroraSimContactListAdapter.PhoneQuery;
import com.android.contacts.list.ContactListItemView.PhotoPosition;
import com.android.contacts.util.DensityUtil;
import com.mediatek.contacts.dialpad.IDialerSearchController.GnDialerSearchResultColumns;

import android.util.Log;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import gionee.provider.GnContactsContract;
import gionee.provider.GnContactsContract.ContactCounts;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.Directory;
import gionee.provider.GnContactsContract.SearchSnippetColumns;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraTextView;

/**
 * A cursor adapter for the {@link GnContactsContract.Contacts#CONTENT_TYPE} content type.
 * Also includes support for including the {@link GnContactsContract.Profile} record in the
 * list.
 */
public abstract class ContactListAdapter extends ContactEntryListAdapter {

	protected static class ContactQuery {
		public static final String RAW_CONTACT_ID = "name_raw_contact_id"; 

		private static final String[] CONTACT_PROJECTION_PRIMARY = new String[] {
			Contacts._ID,                           // 0
			Contacts.DISPLAY_NAME_PRIMARY,          // 1
			Contacts.CONTACT_PRESENCE,              // 2
			Contacts.CONTACT_STATUS,                // 3
			Contacts.PHOTO_ID,                      // 4
			Contacts.PHOTO_THUMBNAIL_URI,           // 5
			Contacts.LOOKUP_KEY,                    // 6
			Contacts.IS_USER_PROFILE,               // 7
			//The following lines are provided and maintained by Mediatek inc.
			Contacts.INDICATE_PHONE_SIM,            // 8 
			Contacts.INDEX_IN_SIM,                  // 9
			//The following lines are provided and maintained by Mediatek inc.

			RAW_CONTACT_ID,//10
			Contacts.PHOTO_URI,//11
		};

		private static final String[] CONTACT_PROJECTION_ALTERNATIVE = new String[] {
			Contacts._ID,                           // 0
			Contacts.DISPLAY_NAME_ALTERNATIVE,      // 1
			Contacts.CONTACT_PRESENCE,              // 2
			Contacts.CONTACT_STATUS,                // 3
			Contacts.PHOTO_ID,                      // 4
			Contacts.PHOTO_THUMBNAIL_URI,           // 5
			Contacts.LOOKUP_KEY,                    // 6
			Contacts.IS_USER_PROFILE,               // 7
			//The following lines are provided and maintained by Mediatek inc.
			Contacts.INDICATE_PHONE_SIM,            // 8 
			Contacts.INDEX_IN_SIM,                  // 9
			//The following lines are provided and maintained by Mediatek inc.

			RAW_CONTACT_ID,//10
			Contacts.PHOTO_URI,//11
		};

		private static final String[] FILTER_PROJECTION_PRIMARY = new String[] {
			Contacts._ID,                           // 0
			Contacts.DISPLAY_NAME_PRIMARY,          // 1
			Contacts.CONTACT_PRESENCE,              // 2
			Contacts.CONTACT_STATUS,                // 3
			Contacts.PHOTO_ID,                      // 4
			Contacts.PHOTO_THUMBNAIL_URI,           // 5
			Contacts.LOOKUP_KEY,                    // 6
			Contacts.IS_USER_PROFILE,               // 7
			//The following lines are provided and maintained by Mediatek inc.
			Contacts.INDICATE_PHONE_SIM,            // 8 
			Contacts.INDEX_IN_SIM,                  // 9
			//The following lines are provided and maintained by Mediatek inc.
			SearchSnippetColumns.SNIPPET,           // 10

			RAW_CONTACT_ID,
			Contacts.PHOTO_URI,//11
		};

		private static final String[] FILTER_PROJECTION_ALTERNATIVE = new String[] {
			Contacts._ID,                           // 0
			Contacts.DISPLAY_NAME_ALTERNATIVE,      // 1
			Contacts.CONTACT_PRESENCE,              // 2
			Contacts.CONTACT_STATUS,                // 3
			Contacts.PHOTO_ID,                      // 4
			Contacts.PHOTO_THUMBNAIL_URI,           // 5
			Contacts.LOOKUP_KEY,                    // 6
			Contacts.IS_USER_PROFILE,               // 7
			//The following lines are provided and maintained by Mediatek inc.
			Contacts.INDICATE_PHONE_SIM,            // 8 
			Contacts.INDEX_IN_SIM,                  // 9
			//The following lines are provided and maintained by Mediatek inc.
			SearchSnippetColumns.SNIPPET,           // 10

			RAW_CONTACT_ID,
			Contacts.PHOTO_URI,//11
		};

		public static final int CONTACT_ID               = 0;
		public static final int CONTACT_DISPLAY_NAME     = 1;
		public static final int CONTACT_PRESENCE_STATUS  = 2;
		public static final int CONTACT_CONTACT_STATUS   = 3;
		public static final int CONTACT_PHOTO_ID         = 4;
		public static final int CONTACT_PHOTO_URI        = 5;
		public static final int CONTACT_LOOKUP_KEY       = 6;
		public static final int CONTACT_IS_USER_PROFILE  = 7;
		//The following lines are provided and maintained by Mediatek inc.
		protected static final int CONTACT_INDICATE_PHONE_SIM = 8;
		protected static final int CONTACT_INDEX_IN_SIM = 9;
		//The following lines are provided and maintained by Mediatek inc.
		public static final int CONTACT_SNIPPET          = 10;
		public static final int CONTACT_REAL_PHOTO_URI          = 11;
	}

	private static final String TAG = "liyang-ContactListAdapter";

	private CharSequence mUnknownNameText;

	private long mSelectedContactDirectoryId;
	private String mSelectedContactLookupKey;
	private long mSelectedContactId;

	public ContactListAdapter(Context context) {
		super(context);
		deviderLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
		deviderLayoutParams.setMargins(mContext.getResources().getDimensionPixelOffset(R.dimen.single_list_item_photo_layout_size),0,
				mContext.getResources().getDimensionPixelOffset(R.dimen.single_list_item_divider_margin_right),0);
		mUnknownNameText = context.getText(R.string.missing_name);
	}

	public CharSequence getUnknownNameText() {
		return mUnknownNameText;
	}

	public long getSelectedContactDirectoryId() {
		return mSelectedContactDirectoryId;
	}

	public String getSelectedContactLookupKey() {
		return mSelectedContactLookupKey;
	}

	public long getSelectedContactId() {
		return mSelectedContactId;
	}

	public void setSelectedContact(long selectedDirectoryId, String lookupKey, long contactId) {
		mSelectedContactDirectoryId = selectedDirectoryId;
		mSelectedContactLookupKey = lookupKey;
		mSelectedContactId = contactId;
	}

	protected static Uri buildSectionIndexerUri(Uri uri) {
		return uri.buildUpon()
				.appendQueryParameter(ContactCounts.ADDRESS_BOOK_INDEX_EXTRAS, "true").build();
	}

	@Override
	public String getContactDisplayName(int position) {
		// aurora <wangth> <2013-10-19> modify for aurora ui begin 
		if (getIsQueryForDialer()) {
			return ((Cursor) getItem(position)).getString(6);
		}
		// aurora <wangth> <2013-10-19> modify for aurora ui end
		return ((Cursor) getItem(position)).getString(ContactQuery.CONTACT_DISPLAY_NAME);
	}

	/**
	 * Builds the {@link Contacts#CONTENT_LOOKUP_URI} for the given
	 * {@link ListView} position.
	 */
	public Uri getContactUri(int position) {
		int partitionIndex = getPartitionForPosition(position);
		Cursor item = (Cursor)getItem(position);
		return item != null ? getContactUri(partitionIndex, item) : null;
	}

	// add by mediatek
	public String getContactLookUpKey(int position) {
		Cursor item = (Cursor)getItem(position);
		return item != null ? getContactLookUpKey(item) : null;
	}

	private String getContactLookUpKey(Cursor cursor) {
		String lookupKey = cursor.getString(ContactQuery.CONTACT_LOOKUP_KEY);
		return lookupKey;
	}

	public Uri getContactUri(int partitionIndex, Cursor cursor) {
		long contactId = cursor.getLong(ContactQuery.CONTACT_ID);
		String lookupKey = cursor.getString(ContactQuery.CONTACT_LOOKUP_KEY);
		if (getIsQueryForDialer()) {
			contactId = cursor.getLong(GnDialerSearchResultColumns.CONTACT_ID_INDEX);
			lookupKey = cursor.getString(GnDialerSearchResultColumns.LOOKUP_KEY_INDEX);
		}
		Uri uri = Contacts.getLookupUri(contactId, lookupKey);
		long directoryId = ((DirectoryPartition)getPartition(partitionIndex)).getDirectoryId();
		if (directoryId != Directory.DEFAULT) {
			uri = uri.buildUpon().appendQueryParameter(
					GnContactsContract.DIRECTORY_PARAM_KEY, String.valueOf(directoryId)).build();
		}
		return uri;
	}

	/**
	 * Returns true if the specified contact is selected in the list. For a
	 * contact to be shown as selected, we need both the directory and and the
	 * lookup key to be the same. We are paying no attention to the contactId,
	 * because it is volatile, especially in the case of directories.
	 */
	public boolean isSelectedContact(int partitionIndex, Cursor cursor) {
		long directoryId = ((DirectoryPartition)getPartition(partitionIndex)).getDirectoryId();
		if (getSelectedContactDirectoryId() != directoryId) {
			return false;
		}
		String lookupKey = getSelectedContactLookupKey();
		if (lookupKey != null && TextUtils.equals(lookupKey,
				cursor.getString(ContactQuery.CONTACT_LOOKUP_KEY))) {
			return true;
		}

		return directoryId != Directory.DEFAULT && directoryId != Directory.LOCAL_INVISIBLE
				&& getSelectedContactId() == cursor.getLong(ContactQuery.CONTACT_ID);
	}

	@Override
	protected View newView(Context context, int partition, Cursor cursor, int position,
			ViewGroup parent) {
		if (ContactsApplication.sIsGnContactsSupport) {
			return gnNewView(context, partition, cursor, position, parent);
		}
		ContactListItemView view = new ContactListItemView(context, null);

		view.setUnknownNameText(mUnknownNameText);
		view.setQuickContactEnabled(isQuickContactEnabled());
		view.setActivatedStateSupported(isSelectionVisible());
		return view;
	}

	// aurora <wangth> <2013-11-2> add for aurora begin
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
	// aurora <wangth> <2013-11-2> add for aurora end

	protected void bindSectionHeaderAndDivider(ContactListItemView view, int position,
			Cursor cursor) {
		if (isSectionHeaderDisplayEnabled()) {
			Placement placement = getItemPlacementInSection(position);

			// First position, set the contacts number string
			if (position == 0 && cursor.getInt(ContactQuery.CONTACT_IS_USER_PROFILE) == 1) {
				view.setCountView(getContactsCount());
			} else {
				view.setCountView(null);
			}
			view.setSectionHeader(placement.sectionHeader);
			view.setDividerVisible(ContactsApplication.sIsGnGGKJ_V2_0Support || 
					!placement.lastInSection);
		} else {
			view.setSectionHeader(null);
			view.setDividerVisible(true);
			view.setCountView(null);
		}
	}


	protected void bindPhoto(final ContactListItemView view, int partitionIndex, Cursor cursor) {
		if (!isPhotoSupported(partitionIndex)) {
			view.removePhotoView();
			return;
		}

		Uri photoUri=null;
		long photoId = 0;
		int contactId=0;
		int contactIdIndex0=cursor.getColumnIndex(Contacts._ID);
		if(contactIdIndex0>=0){
			contactId=  cursor.getInt(contactIdIndex0);
		}
		//		for(int i=0;i<cursor.getColumnCount();i++){
		//			Log.d(TAG,"columnname:"+cursor.getColumnName(i)+" value;"+cursor.getString(i));
		//		}

		if(isSearchMode()){
			if (!cursor.isNull(5)) {
				photoId = cursor.getLong(cursor.getColumnIndex("photo_id"));		
			}
			int contactIdIndex=cursor.getColumnIndex("contact_id");
			if(contactIdIndex>=0){
				contactId=  cursor.getInt(contactIdIndex);
			}

		}else{
			if (!cursor.isNull(ContactQuery.CONTACT_REAL_PHOTO_URI)) {
				photoUri=Uri.parse(cursor.getString(ContactQuery.CONTACT_REAL_PHOTO_URI));
			}
			//			if (!cursor.isNull(ContactQuery.CONTACT_PHOTO_ID)) {
			//				photoId = cursor.getLong(ContactQuery.CONTACT_PHOTO_ID);
			//				Log.d(TAG,"photoId:"+photoId);
			//			}
		}

		Log.d(TAG,"contactId:"+contactId);

		if(isSearchMode()){
			if(photoId>0){
				ContactPhotoManager.getInstance(mContext).loadPhoto(view.getPhotoView(), photoId, false, true);
			}else{
				int index=(int) (contactId%(ResConstant.randomContactPhotoId.length));
				Log.d(TAG,"contactId:"+contactId+" index:"+index);

				if(index<ResConstant.randomContactPhotoId.length&&contactId!=0){
					view.getPhotoView().setImageDrawable(mContext.getResources().getDrawable(ResConstant.randomContactPhotoId[index]));
				}else{
					view.getPhotoView().setImageDrawable(mContext.getResources().getDrawable(R.drawable.svg_dial_default_photo1));
				}

				//			else if(photoId<-99){
				//				int index=(int) (0-photoId-100);
				//				Log.d(TAG,"index:"+index);
				//				if(index>=0){
				//					view.getPhotoView().setImageDrawable(mContext.getResources().getDrawable(ResConstant.randomContactPhotoId[index]));
				//				}
				//			}else{
				//				view.getPhotoView().setImageDrawable(mContext.getResources().getDrawable(R.drawable.svg_dial_default_photo1));
				//			}

			}


		}else{
			if(photoUri!=null){
				if(getPhotoLoader()==null){
					ContactPhotoManager.getInstance(mContext).loadPhoto((ImageView)(view.getPhotoView()), photoUri, true,
							false, ContactPhotoManager.DEFAULT_BLANK);
				}else{
					getPhotoLoader().loadPhoto((ImageView)(view.getPhotoView()), photoUri, true,
							false, ContactPhotoManager.DEFAULT_BLANK);
				}
			}else{
				int index=(int) (contactId%(ResConstant.randomContactPhotoId.length));
				Log.d(TAG,"contactId:"+contactId+" index:"+index);

				if(index<ResConstant.randomContactPhotoId.length){
					view.getPhotoView().setImageDrawable(mContext.getResources().getDrawable(ResConstant.randomContactPhotoId[index]));
				}else{
					view.getPhotoView().setImageDrawable(mContext.getResources().getDrawable(R.drawable.svg_dial_default_photo1));
				}
			}	

		}
	}


	/*protected void bindPhoto(final ContactListItemView view, int partitionIndex, Cursor cursor) {
        if (!isPhotoSupported(partitionIndex)) {
            view.removePhotoView();
            return;
        }

        Log.d(TAG,"bindPhoto1");
        // Set the photo, if available
        long photoId = 0;
        if (!cursor.isNull(ContactQuery.CONTACT_PHOTO_ID)) {
            photoId = cursor.getLong(ContactQuery.CONTACT_PHOTO_ID);
            Log.d(TAG,"photoId:"+photoId);

        }



	 * Bug Fix by Mediatek Begin.
	 *   Original Android隆's code:
	 *     xxx
	 *   CR ID: ALPS00110185
	 *   Descriptions: 颅

        int indicatePhoneSim = cursor.getInt(ContactQuery.CONTACT_INDICATE_PHONE_SIM);
		if (indicatePhoneSim > 0) {
			photoId = getSimType(indicatePhoneSim);
		}

	 * Bug Fix by Mediatek End.


        if (photoId != 0) {
        	// gionee xuhz 20121208 modify for GIUI2.0 start
        	if (ContactsApplication.sIsGnGGKJ_V2_0Support && ContactsApplication.sIsGnDarkStyle) {
                getPhotoLoader().loadPhoto(view.getPhotoView(), photoId, false, true);
        	} else {
        		Log.d(TAG,"photoLoader:"+getPhotoLoader());
        		Log.d(TAG, "view.getPhotoview:"+view.getPhotoView());
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
        	view.getPhotoView().setVisibility(View.VISIBLE);

        }
    }*/




	protected void bindName(final ContactListItemView view, Cursor cursor) {
		if (ContactsApplication.sIsGnContactsSupport) {
			//mGnLastBindName = view.gnShowDisplayName(cursor, ContactQuery.CONTACT_DISPLAY_NAME);
			if (getIsQueryForDialer()) {
				mGnLastBindName = view.auroraShowDisplayName(cursor, GnDialerSearchResultColumns.NAME_INDEX, mQueryString);
			} else {
				mGnLastBindName = view.gnShowDisplayName(cursor, ContactQuery.CONTACT_DISPLAY_NAME);
			}
			if (null != mGnLastBindName) {
				mGnLastBindName = mGnLastBindName.trim();
			}
		} else {
			view.showDisplayName(
					cursor, ContactQuery.CONTACT_DISPLAY_NAME, getContactNameDisplayOrder());
		}
		// Note: we don't show phonetic any more (See issue 5265330)
	}

	protected void bindPresenceAndStatusMessage(final ContactListItemView view, Cursor cursor) {
		view.showPresenceAndStatusMessage(cursor, ContactQuery.CONTACT_PRESENCE_STATUS,
				ContactQuery.CONTACT_CONTACT_STATUS);
	}

	protected void bindSearchSnippet(final ContactListItemView view, Cursor cursor) {
		view.showSnippet(cursor, ContactQuery.CONTACT_SNIPPET);
	}

	public int getSelectedContactPosition() {
		if (mSelectedContactLookupKey == null && mSelectedContactId == 0) {
			return -1;
		}

		Cursor cursor = null;
		int partitionIndex = -1;
		int partitionCount = getPartitionCount();
		for (int i = 0; i < partitionCount; i++) {
			DirectoryPartition partition = (DirectoryPartition) getPartition(i);
			if (partition.getDirectoryId() == mSelectedContactDirectoryId) {
				partitionIndex = i;
				break;
			}
		}
		if (partitionIndex == -1) {
			return -1;
		}

		cursor = getCursor(partitionIndex);
		if (cursor == null) {
			return -1;
		}

		cursor.moveToPosition(-1);      // Reset cursor
		int offset = -1;
		while (cursor.moveToNext()) {
			if (mSelectedContactLookupKey != null) {
				String lookupKey = cursor.getString(ContactQuery.CONTACT_LOOKUP_KEY);
				if (mSelectedContactLookupKey.equals(lookupKey)) {
					offset = cursor.getPosition();
					break;
				}
			}
			if (mSelectedContactId != 0 && (mSelectedContactDirectoryId == Directory.DEFAULT
					|| mSelectedContactDirectoryId == Directory.LOCAL_INVISIBLE)) {
				long contactId = cursor.getLong(ContactQuery.CONTACT_ID);
				if (contactId == mSelectedContactId) {
					offset = cursor.getPosition();
					break;
				}
			}
		}
		cursor.close();
		if (offset == -1) {
			return -1;
		}

		int position = getPositionForPartition(partitionIndex) + offset;
		if (hasHeader(partitionIndex)) {
			position++;
		}
		return position;
	}

	public boolean hasValidSelection() {
		return getSelectedContactPosition() != -1;
	}

	public Uri getFirstContactUri() {
		int partitionCount = getPartitionCount();
		for (int i = 0; i < partitionCount; i++) {
			DirectoryPartition partition = (DirectoryPartition) getPartition(i);
			if (partition.isLoading()) {
				continue;
			}

			Cursor cursor = getCursor(i);
			if (cursor == null) {
				continue;
			}

			if (!cursor.moveToFirst()) {
				continue;
			}

			return getContactUri(i, cursor);
		}

		return null;
	}

	@Override
	public void changeCursor(int partitionIndex, Cursor cursor) {
		super.changeCursor(partitionIndex, cursor);

		// Check if a profile exists
		if (cursor != null && cursor.getCount() > 0) {
			try{
				if(cursor.moveToFirst()){
					setProfileExists(cursor.getInt(ContactQuery.CONTACT_IS_USER_PROFILE) == 1);
				}
			}catch(Exception e){
				Log.d(TAG,"e:"+e.toString());
			}
		}
	}

	/**
	 * @return Projection useful for children.
	 */
	protected final String[] getProjection(boolean forSearch) {
		final int sortOrder = getContactNameDisplayOrder();
		if (forSearch) {
			if (sortOrder == GnContactsContract.Preferences.DISPLAY_ORDER_PRIMARY) {
				return ContactQuery.FILTER_PROJECTION_PRIMARY;
			} else {
				return ContactQuery.FILTER_PROJECTION_ALTERNATIVE;
			}
		} else {
			if (sortOrder == GnContactsContract.Preferences.DISPLAY_ORDER_PRIMARY) {
				return ContactQuery.CONTACT_PROJECTION_PRIMARY;
			} else {
				return ContactQuery.CONTACT_PROJECTION_ALTERNATIVE;
			}
		}
	}

	//**************************follow lines are Gionee****************************

	//Gionee:huangzy 20120607 add for CR00614801 start
	private String mGnLastBindName;

	public String gnGetLastBindName() {
		return mGnLastBindName;
	}

	public String gnGetLastBindNameFristChar() {
		if (null == mGnLastBindName || mGnLastBindName.length() <= 1) {
			return mGnLastBindName;
		}

		return mGnLastBindName.substring(0, 1);
	}
	//Gionee:huangzy 20120607 add for CR00614801 end

	private LinearLayout.LayoutParams deviderLayoutParams;
	protected View gnNewView(Context context, int partition, Cursor cursor, int position,
			ViewGroup parent) {
		ContactListItemView view = new ContactListItemView(context, null);
		view.setUnknownNameText(mUnknownNameText);
		view.setQuickContactEnabled(isQuickContactEnabled());
		view.setActivatedStateSupported(isSelectionVisible());

		//		Log.d(TAG,"getbackground:"+view.getBackground());
		//		
		//		view.setBackground(null);

		View v = (View) LayoutInflater.from(context).inflate(
				com.aurora.R.layout.aurora_slid_listview, null);
		RelativeLayout mainUi = (RelativeLayout) v
				.findViewById(com.aurora.R.id.aurora_listview_front);
		mainUi.addView(view, 0, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));

		ImageView iv = (ImageView) v.findViewById(com.aurora.R.id.aurora_listview_divider);
		((LinearLayout)iv.getParent()).setBackgroundColor(mContext.getResources().getColor(R.color.listview_item_color));
		iv.setLayoutParams(deviderLayoutParams);
		iv.setVisibility(View.VISIBLE);
		

		View paddingView=v.findViewById(com.aurora.R.id.control_padding);
		paddingView.setPadding(0, 0, 0, 0);

		CheckBox cb=(CheckBox) v.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
		AuroraListView.setCheckBoxMaringRightIfHasIndex(cb);


		return v;
	}
}
