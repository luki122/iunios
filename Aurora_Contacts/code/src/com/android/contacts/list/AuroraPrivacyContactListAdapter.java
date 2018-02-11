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

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraTextView;
import aurora.widget.AuroraListView;
import aurora.app.AuroraAlertDialog;
import gionee.provider.GnCallLog.Calls;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.RawContacts;

import com.android.contacts.ContactsUtils;
import com.android.contacts.R;
import com.android.contacts.list.ContactListAdapter.ContactQuery;
import com.android.contacts.widget.IndexerListAdapter.Placement;

public class AuroraPrivacyContactListAdapter extends CursorAdapter {
	
	private Context mContext;
	
	private boolean mCheckBoxEnable = false;
	private boolean mNeedAnim = false;
	private HashMap<Long, String> mCheckedItem = new HashMap<Long, String>();
	
	public static final String RAW_CONTACT_ID = "name_raw_contact_id"; 
	public static final String[] CONTACT_PROJECTION_PRIMARY = new String[] {
        Contacts._ID,                           // 0
        Contacts.DISPLAY_NAME_PRIMARY,          // 1
        Contacts.CONTACT_PRESENCE,              // 2
        Contacts.CONTACT_STATUS,                // 3
        Contacts.PHOTO_ID,                      // 4
        Contacts.PHOTO_THUMBNAIL_URI,           // 5
        Contacts.LOOKUP_KEY,                    // 6
        Contacts.IS_USER_PROFILE,               // 7
        
        RAW_CONTACT_ID,
        "call_notification_type"
    };
	
	private static final int CONTACT_ID_INDEX = 0;
	private static final int NAME_INDEX = 1;
	private static final int LOOKUP_KEY_INDEX = 6;
	private static final int RAW_CONTACT_ID_INDEX = 8;
	
	public AuroraPrivacyContactListAdapter(Context context) {
		super(context, null, false);
		
		mContext = context;
	}
	
	public void setCheckBoxEnable(boolean flag) {
        mCheckBoxEnable = flag;
    }
    
    public boolean getCheckBoxEnable() {
        return mCheckBoxEnable;
    }
    
    public void setNeedAnim(boolean flag) {
        mNeedAnim = flag;
    }
    
    public boolean getNeedAnim() {
        return mNeedAnim;
    }
    
    public boolean mAuroraListDelet = false;
    
    public void setAuroraListDelet(boolean flag) {
        mAuroraListDelet = flag;
    }
    
    public void setCheckedItem(long contactId, String str) {
        if (mCheckedItem == null) {
            mCheckedItem = new HashMap<Long, String>();
        }
        
        mCheckedItem.put(contactId, str);
    }
    
    public HashMap<Long, String> getCheckedItem() {
    	return mCheckedItem;
    }
    
    public Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub

            setNeedAnim(false);
            super.handleMessage(msg);
        }

    };

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		ContactListItemView view = new ContactListItemView(context, null);
		
		View v = (View) LayoutInflater.from(context).inflate(
                com.aurora.R.layout.aurora_slid_listview, null);
        RelativeLayout mainUi = (RelativeLayout) v
                .findViewById(com.aurora.R.id.aurora_listview_front);
        mainUi.addView(view, 0, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        
        ImageView iv = (ImageView) v.findViewById(com.aurora.R.id.aurora_listview_divider);
        iv.setVisibility(View.VISIBLE);
        
        LinearLayout deleteUi = (LinearLayout) v
                .findViewById(com.aurora.R.id.aurora_listview_back);
        ViewGroup.LayoutParams param = deleteUi.getLayoutParams();
        param.width = mContext.getResources().getDimensionPixelSize(
                R.dimen.aurora_list_item_delete_back_width);
        deleteUi.setLayoutParams(param);
		
		return v;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		return super.getView(position, convertView, parent);
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final RelativeLayout mainUi = (RelativeLayout) view
				.findViewById(com.aurora.R.id.aurora_listview_front);
		final AuroraCheckBox checkBox = (AuroraCheckBox) view
				.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
		final ContactListItemView nameView = (ContactListItemView) (mainUi
				.getChildAt(0));
		nameView.auroraSetCheckable(false);

		int position = cursor.getPosition();

		LinearLayout contentUi = (LinearLayout) view
				.findViewById(com.aurora.R.id.content);
		AuroraListView.auroraGetAuroraStateListDrawableFromIndex(contentUi, position);

		if (getCheckBoxEnable()) {
			int contactId = cursor.getInt(CONTACT_ID_INDEX);
			boolean checked = false;
			if (getCheckedItem() != null
					&& getCheckedItem().containsKey(Long.valueOf(contactId))) {
				checked = true;
			}

			if (getNeedAnim()) {
				AuroraListView.auroraStartCheckBoxAppearingAnim(
						nameView.getNameTextView(), checkBox);
			} else {
				AuroraListView.auroraSetCheckBoxVisible(
						nameView.getNameTextView(), checkBox, true);
			}
			checkBox.setChecked(checked);
		} else {
			if (checkBox != null) {
				if (getNeedAnim()) {
					AuroraListView.auroraStartCheckBoxDisappearingAnim(
							nameView.getNameTextView(), checkBox);
				} else {
					AuroraListView.auroraSetCheckBoxVisible(
							nameView.getNameTextView(), checkBox, false);
				}
			}
			nameView.setCheckable(false);
		}

		nameView.gnShowDisplayName(cursor, NAME_INDEX);

		if (getNeedAnim()) {
			mHandler.sendMessage(mHandler.obtainMessage());
		}
		
		ViewGroup.LayoutParams lp = view.getLayoutParams();
        int high = mContext.getResources().getDimensionPixelOffset(com.aurora.R.dimen.aurora_list_singleline_height);
        if(null != lp) {
            lp.height = high + 1;
            view.setLayoutParams(lp);
            contentUi.setAlpha(255);
        }
	}
	
	public int getContactID(int position) {
		Cursor cursor = mCursor;
		if (position <= cursor.getCount()) {
			if (cursor.moveToPosition(position)) {
				return cursor.getInt(CONTACT_ID_INDEX);
			}
		}
		
		return 0;
	}
	
	public int getRawContactID(int position) {
		Cursor cursor = mCursor;
		if (position <= cursor.getCount()) {
			if (cursor.moveToPosition(position)) {
				return cursor.getInt(RAW_CONTACT_ID_INDEX);
			}
		}
		
		return 0;
	}
	
	public String getName(int position) {
		Cursor cursor = mCursor;
		if (position <= cursor.getCount()) {
			if (cursor.moveToPosition(position)) {
				return cursor.getString(NAME_INDEX);
			}
		}
		
		return null;
	}
	
	public Uri getContactUri(int position) {
		Cursor cursor = mCursor;
		if (position <= cursor.getCount()) {
			if (cursor.moveToPosition(position)) {
				long contactId = cursor.getLong(CONTACT_ID_INDEX);
		        String lookupKey = cursor.getString(LOOKUP_KEY_INDEX);
		        Uri uri = Contacts.getLookupUri(contactId, lookupKey);
		        return uri;
			}
		}
		
		return null;
	}
}
