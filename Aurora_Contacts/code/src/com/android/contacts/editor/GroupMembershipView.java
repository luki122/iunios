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

package com.android.contacts.editor;

import com.android.contacts.ContactsApplication;
import com.android.contacts.GroupMetaDataLoader;
import com.android.contacts.R;
import com.android.contacts.ResConstant;
import com.android.contacts.interactions.GroupCreationDialogFragment;
import com.android.contacts.model.AccountType;
import com.android.contacts.model.DataKind;
import com.android.contacts.model.EntityDelta;
import com.android.contacts.model.EntityDelta.ValuesDelta;
import com.android.contacts.model.EntityModifier;
import com.mediatek.contacts.util.Objects;

import android.app.Activity;
import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import gionee.provider.GnContactsContract.CommonDataKinds.GroupMembership;
import gionee.provider.GnContactsContract.RawContacts;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import aurora.widget.AuroraListView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * An editor for group membership.  Displays the current group membership list and
 * brings up a dialog to change it.
 */
public class GroupMembershipView extends LinearLayout
        implements OnClickListener, OnItemClickListener {

    private static final int CREATE_NEW_GROUP_GROUP_ID = 133;

    public static final class GroupSelectionItem {
        private final long mGroupId;
        private final String mTitle;
        private boolean mChecked;

        public GroupSelectionItem(long groupId, String title, boolean checked) {
            this.mGroupId = groupId;
            this.mTitle = title;
            mChecked = checked;
        }

        public long getGroupId() {
            return mGroupId;
        }

        public boolean isChecked() {
            return mChecked;
        }

        public void setChecked(boolean checked) {
            mChecked = checked;
        }

        @Override
        public String toString() {
            return mTitle;
        }
    }

    private EntityDelta mState;
    private Cursor mGroupMetaData;
    private String mAccountName;
    private String mAccountType;
    private String mDataSet;
    private TextView mGroupList;
    private ArrayAdapter<GroupSelectionItem> mAdapter;
    private long mDefaultGroupId;
    private long mFavoritesGroupId;
    private ListPopupWindow mPopup;
    private DataKind mKind;
    private boolean mDefaultGroupVisibilityKnown;
    private boolean mDefaultGroupVisible;

    private int mPrimaryTextColor;
    
    private AuroraAlertDialog mDialog;

    public GroupMembershipView(Context context) {
        super(context);
    }

    public GroupMembershipView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Resources resources = mContext.getResources();
        mPrimaryTextColor = resources.getColor(R.color.primary_text_color);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mGroupList != null) {
            mGroupList.setEnabled(enabled);
        }
    }
    
    public void setKind(DataKind kind) {
        mKind = kind;
        TextView kindTitle = (TextView) findViewById(R.id.kind_title);
        kindTitle.setText(getResources().getString(kind.titleRes).toUpperCase());
		// aurora <ukiliu> <2013-9-17> add for auroro ui begin
        kindTitle.setVisibility(View.GONE);
		// aurora <ukiliu> <2013-9-17> add for auroro ui end
        /*if (ContactsApplication.sIsGnContactsSupport) {
            kindTitle.setTextColor(ResConstant.sHeaderTextColor);
        }*/
    }

    public void setGroupMetaData(Cursor groupMetaData) {
        this.mGroupMetaData = groupMetaData;
        updateView();
    }

    public void setState(EntityDelta state) {
        mState = state;
        ValuesDelta values = state.getValues();
        mAccountType = values.getAsString(RawContacts.ACCOUNT_TYPE);
        mAccountName = values.getAsString(RawContacts.ACCOUNT_NAME);
        mDataSet = values.getAsString(RawContacts.DATA_SET);
        mDefaultGroupVisibilityKnown = false;
        updateView();
    }

    private void updateView() {
//        if (mGroupMetaData == null || mGroupMetaData.isClosed() || mAccountType == null
//                || mAccountName == null) {
//            setVisibility(GONE);
//            return;
//        }
        if (mAccountType == null
                || mAccountName == null) {
        	Log.e("liumxxx","no account info, can't select group!");
            setVisibility(GONE);
            return;
        }

        boolean accountHasGroups = false;
        mFavoritesGroupId = 0;
        mDefaultGroupId = 0;

        StringBuilder sb = new StringBuilder();
        if (mGroupMetaData != null && !mGroupMetaData.isClosed()) {
	        mGroupMetaData.moveToPosition(-1);
	        while (mGroupMetaData.moveToNext()) {
	            String accountName = mGroupMetaData.getString(GroupMetaDataLoader.ACCOUNT_NAME);
	            String accountType = mGroupMetaData.getString(GroupMetaDataLoader.ACCOUNT_TYPE);
	            String dataSet = mGroupMetaData.getString(GroupMetaDataLoader.DATA_SET);
	            if (accountName.equals(mAccountName) && accountType.equals(mAccountType)
	                    && Objects.equal(dataSet, mDataSet)) {
	                long groupId = mGroupMetaData.getLong(GroupMetaDataLoader.GROUP_ID);
	                if (!mGroupMetaData.isNull(GroupMetaDataLoader.FAVORITES)
	                        && mGroupMetaData.getInt(GroupMetaDataLoader.FAVORITES) != 0) {
	                    mFavoritesGroupId = groupId;
	                } else if (!mGroupMetaData.isNull(GroupMetaDataLoader.AUTO_ADD)
	                            && mGroupMetaData.getInt(GroupMetaDataLoader.AUTO_ADD) != 0) {
	                    mDefaultGroupId = groupId;
	                } else {
	                    accountHasGroups = true;
	                }
	
	                // Exclude favorites from the list - they are handled with special UI (star)
	                // Also exclude the default group.
	                if (groupId != mFavoritesGroupId && groupId != mDefaultGroupId
	                        && hasMembership(groupId)) {
	                    String title = mGroupMetaData.getString(GroupMetaDataLoader.TITLE);
	                    if (sb.length() != 0) {
	                        sb.append(", ");
	                    }
	                    sb.append(title);
	                }
	            }
	        }

        }
//        if (!accountHasGroups) {
//            setVisibility(GONE);
//            return;
//        }

        if (mGroupList == null) {
            mGroupList = (TextView) findViewById(R.id.group_list);
            mGroupList.setClickable(false);
            ((View)(mGroupList.getParent())).setOnClickListener(this);
        }

        mGroupList.setEnabled(isEnabled());
        mGroupList.setText(sb.toString());
        setVisibility(VISIBLE);

        if (!mDefaultGroupVisibilityKnown) {
            // Only show the default group (My Contacts) if the contact is NOT in it
            mDefaultGroupVisible = mDefaultGroupId != 0 && !hasMembership(mDefaultGroupId);
            mDefaultGroupVisibilityKnown = true;
        }
    }

    @Override
    public void onClick(View v) {
        if (mPopup != null && mPopup.isShowing()) {
            mPopup.dismiss();
            mPopup = null;
            return;
        }

        // gionee xuhz 20120515 modify start
        if (ContactsApplication.sIsGnTransparentTheme) {
            mAdapter = new ArrayAdapter<GroupSelectionItem>(
                    getContext(), R.layout.gn_group_membership_list_item);
        } else {
            mAdapter = new ArrayAdapter<GroupSelectionItem>(
                    getContext(), R.layout.group_membership_list_item);
        }
        // gionee xuhz 20120515 modify end

        if (mGroupMetaData != null && !mGroupMetaData.isClosed()) {

        	mGroupMetaData.moveToPosition(-1);
	        while (mGroupMetaData.moveToNext()) {
	            String accountName = mGroupMetaData.getString(GroupMetaDataLoader.ACCOUNT_NAME);
	            String accountType = mGroupMetaData.getString(GroupMetaDataLoader.ACCOUNT_TYPE);
	            String dataSet = mGroupMetaData.getString(GroupMetaDataLoader.DATA_SET);
	            if (accountName.equals(mAccountName) && accountType.equals(mAccountType)
	                    && Objects.equal(dataSet, mDataSet)) {
	                long groupId = mGroupMetaData.getLong(GroupMetaDataLoader.GROUP_ID);
	                if (groupId != mFavoritesGroupId
	                        && (groupId != mDefaultGroupId || mDefaultGroupVisible)) {
	                    String title = mGroupMetaData.getString(GroupMetaDataLoader.TITLE);
	                    boolean checked = hasMembership(groupId);
	                    mAdapter.add(new GroupSelectionItem(groupId, title, checked));
	                }
	            }
	        }
        }
        /* 
         * New Feature by Mediatek Begin.
         * Original Android's code:
         * mAdapter.add(new GroupSelectionItem(CREATE_NEW_GROUP_GROUP_ID,getContext().getString(R.string.create_group_item_label), false))
         * CR ID: ALPS00101852
         * Descriptions: Remove add new group feature in edit USIM contacnt
         */
        if (!mAccountType.equals(mUsimAccountType)) {
            mAdapter.add(new GroupSelectionItem(CREATE_NEW_GROUP_GROUP_ID, getContext().getString(
                    R.string.aurora_group_create), false));
        }

        /*
         * Change Feature by Mediatek End.
         */
        
        ListView listView = null;
        if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
        	mDialog = new AuroraAlertDialog.Builder(getContext())
        		.setTitle(R.string.aurora_group_title)
        		.setAdapter(mAdapter, null)
        		.setTitleDividerVisible(true)
        		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						
					}
        			
        		})
        		.create();
        	mDialog.show();
        	
        	listView = mDialog.getListView();
        } else {
        	mPopup = new ListPopupWindow(getContext(), null);
            mPopup.setAnchorView(mGroupList);
            mPopup.setAdapter(mAdapter);
            mPopup.setModal(true);
            mPopup.show();
            
            listView = mPopup.getListView();
        }

        /*ListView listView = mPopup.getListView();*/
        listView.setChoiceMode(AuroraListView.CHOICE_MODE_MULTIPLE);
        listView.setOverScrollMode(OVER_SCROLL_ALWAYS);
        int count = mAdapter.getCount();
        for (int i = 0; i < count; i++) {
            listView.setItemChecked(i, mAdapter.getItem(i).isChecked());
        }

        listView.setOnItemClickListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        dismissPopup();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    	ListView list = (ListView) parent;
        int count = mAdapter.getCount();
        // The following lines are provided and maintained by Mediatek inc.
        if (!mAccountType.equals(mUsimAccountType)) {
            if (list.isItemChecked(count - 1)) {
                list.setItemChecked(count - 1, false);
                createNewGroup();
                return;
            }
        }
        // The following lines are provided and maintained by Mediatek inc.

        for (int i = 0; i < count; i++) {
            mAdapter.getItem(i).setChecked(list.isItemChecked(i));
        }

        // First remove the memberships that have been unchecked
        ArrayList<ValuesDelta> entries = mState.getMimeEntries(GroupMembership.CONTENT_ITEM_TYPE);
        if (entries != null) {
            for (ValuesDelta entry : entries) {
                if (!entry.isDelete()) {
                    Long groupId = entry.getAsLong(GroupMembership.GROUP_ROW_ID);
                    if (groupId != null && groupId != mFavoritesGroupId
                            && (groupId != mDefaultGroupId || mDefaultGroupVisible)
                            && !isGroupChecked(groupId)) {
                        entry.markDeleted();
                    }
                }
            }
        }

        // Now add the newly selected items
        for (int i = 0; i < count; i++) {
            GroupSelectionItem item = mAdapter.getItem(i);
            long groupId = item.getGroupId();
            if (item.isChecked() && !hasMembership(groupId)) {
                ValuesDelta entry = EntityModifier.insertChild(mState, mKind);
                entry.put(GroupMembership.GROUP_ROW_ID, groupId);
            }
        }

        updateView();
    }

    private boolean isGroupChecked(long groupId) {
        int count = mAdapter.getCount();
        for (int i = 0; i < count; i++) {
            GroupSelectionItem item = mAdapter.getItem(i);
            if (groupId == item.getGroupId()) {
                return item.isChecked();
            }
        }
        return false;
    }

    private boolean hasMembership(long groupId) {
        if (groupId == mDefaultGroupId && mState.isContactInsert()) {
            return true;
        }

        ArrayList<ValuesDelta> entries = mState.getMimeEntries(GroupMembership.CONTENT_ITEM_TYPE);
        if (entries != null) {
            for (ValuesDelta values : entries) {
                if (!values.isDelete()) {
                    Long id = values.getAsLong(GroupMembership.GROUP_ROW_ID);
                    if (id != null && id == groupId) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void createNewGroup() {
    	dismissPopup();

        GroupCreationDialogFragment.show(
                ((Activity) getContext()).getFragmentManager(), mAccountType, mAccountName,
                mDataSet);
    }
    /* 
     * New Feature by Mediatek Begin.
     * Original Android's code:
     * 
     * CR ID: ALPS00101852
     * Descriptions: crete sim/usim contact
     */
    private String mUsimAccountType =  AccountType.ACCOUNT_TYPE_USIM;
    /*
     * Change Feature by Mediatek End.
     */
    
    private void dismissPopup() {
    	if (mPopup != null && mPopup.isShowing()) {
            mPopup.dismiss();
        }
        if (null != mDialog) {
        	mDialog.dismiss();
        }
        
        mPopup = null;
        mDialog = null;
    }
}