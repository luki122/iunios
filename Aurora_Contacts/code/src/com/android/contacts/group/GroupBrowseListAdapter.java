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

package com.android.contacts.group;

import java.util.List;
import aurora.widget.AuroraTextView;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactsApplication;
import com.android.contacts.GroupListLoader;
import com.android.contacts.R;
import com.android.contacts.ResConstant;
import com.android.contacts.model.AccountType;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.AccountWithDataSet;
import com.android.contacts.util.AccountFilterUtil;
import com.android.contacts.util.PhoneCapabilityTester;
import com.mediatek.contacts.util.Objects;
import com.mediatek.contacts.model.AccountWithDataSetEx;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.Contacts;
import gionee.provider.GnContactsContract.Groups;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Adapter to populate the list of groups.
 */
public class GroupBrowseListAdapter extends BaseAdapter {

    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private final AccountTypeManager mAccountTypeManager;

    private Cursor mCursor;

    private boolean mSelectionVisible;
    private Uri mSelectedGroupUri;

    public GroupBrowseListAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mAccountTypeManager = AccountTypeManager.getInstance(mContext);
    }

    public void setCursor(Cursor cursor) {
        mCursor = cursor;

        // If there's no selected group already and the cursor is valid, then by default, select the
        // first group
        if (mSelectedGroupUri == null && cursor != null && cursor.getCount() > 0) {
            GroupListItem firstItem = getItem(0);
            long groupId = (firstItem == null) ? null : firstItem.getGroupId();
            // mSelectedGroupUri = getGroupUriFromId(groupId);
            mSelectedGroupUri = getGroupUriFromIdAndAccountInfo(groupId,
                    firstItem.getAccountName(), firstItem.getAccountType());
        }

        notifyDataSetChanged();
    }

    public int getSelectedGroupPosition() {
        if (mSelectedGroupUri == null || mCursor == null || mCursor.getCount() == 0) {
            return -1;
        }

        int index = 0;
        mCursor.moveToPosition(-1);
        while (mCursor.moveToNext()) {
            long groupId = mCursor.getLong(GroupListLoader.GROUP_ID);
            //Uri uri = getGroupUriFromId(groupId);

            String accountName = mCursor.getString(GroupListLoader.ACCOUNT_NAME);
            String accountType = mCursor.getString(GroupListLoader.ACCOUNT_TYPE);
            // uri = GroupUriWithAccountInfo(uri, accountName, accountType);
            Uri uri = getGroupUriFromIdAndAccountInfo(groupId, accountName, accountType);
            if (mSelectedGroupUri.equals(uri)) {
                  return index;
            }
            index++;
        }
        return -1;
    }

    public void setSelectionVisible(boolean flag) {
        mSelectionVisible = flag;
    }

    public void setSelectedGroup(Uri groupUri) {
        mSelectedGroupUri = groupUri;
    }

    private boolean isSelectedGroup(Uri groupUri) {
        return mSelectedGroupUri != null && mSelectedGroupUri.equals(groupUri);
    }

    public Uri getSelectedGroup() {
        return mSelectedGroupUri;
    }

    @Override
    public int getCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public GroupListItem getItem(int position) {
        if (mCursor == null || mCursor.isClosed() || !mCursor.moveToPosition(position)) {
            return null;
        }
        String accountName = mCursor.getString(GroupListLoader.ACCOUNT_NAME);
        String accountType = mCursor.getString(GroupListLoader.ACCOUNT_TYPE);
        String dataSet = mCursor.getString(GroupListLoader.DATA_SET);
        long groupId = mCursor.getLong(GroupListLoader.GROUP_ID);
        String title = mCursor.getString(GroupListLoader.TITLE);
        int memberCount = mCursor.getInt(GroupListLoader.MEMBER_COUNT);

        // Figure out if this is the first group for this account name / account type pair by
        // checking the previous entry. This is to determine whether or not we need to display an
        // account header in this item.
        int previousIndex = position - 1;
        boolean isFirstGroupInAccount = true;
        if (previousIndex >= 0 && mCursor.moveToPosition(previousIndex)) {
            String previousGroupAccountName = mCursor.getString(GroupListLoader.ACCOUNT_NAME);
            String previousGroupAccountType = mCursor.getString(GroupListLoader.ACCOUNT_TYPE);
            String previousGroupDataSet = mCursor.getString(GroupListLoader.DATA_SET);

            if (accountName.equals(previousGroupAccountName) &&
                    accountType.equals(previousGroupAccountType) &&
                    Objects.equal(dataSet, previousGroupDataSet)) {
                isFirstGroupInAccount = false;
            }
        }

        return new GroupListItem(accountName, accountType, dataSet, groupId, title,
                isFirstGroupInAccount, memberCount);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GroupListItem entry = getItem(position);
        GroupListItemViewCache viewCache = null;
        if (convertView != null) {
            viewCache = (GroupListItemViewCache) convertView.getTag();
        } else {
        	convertView = mLayoutInflater.inflate(com.aurora.R.layout.aurora_slid_listview, null);
        	RelativeLayout mainUi = (RelativeLayout) convertView
					.findViewById(com.aurora.R.id.aurora_listview_front);
        	mLayoutInflater.inflate(R.layout.aurora_group_item, mainUi);
            viewCache = new GroupListItemViewCache(convertView);
            convertView.setTag(viewCache);
        }
        convertView.findViewById(com.aurora.R.id.aurora_listview_divider).setVisibility(View.GONE);
        // Add a header if this is the first group in an account and hide the divider
        // aurora <wangth> <2013-9-4> modify for auroro ui begin
        /*
        if (entry.isFirstGroupInAccount()) {
            bindHeaderView(entry, viewCache);
            viewCache.accountHeader.setVisibility(View.VISIBLE);
//            viewCache.divider.setVisibility(View.GONE);
            if (position == 0) {
                // Have the list's top padding in the first header.
                //
                // This allows the ListView to show correct fading effect on top.
                // If we have topPadding in the ListView itself, an inappropriate padding is
                // inserted between fading items and the top edge.
                viewCache.accountHeaderExtraTopPadding.setVisibility(View.VISIBLE);
            } else {
                viewCache.accountHeaderExtraTopPadding.setVisibility(View.GONE);
            }
        } else {
            viewCache.accountHeader.setVisibility(View.GONE);
//            viewCache.divider.setVisibility(View.VISIBLE);
            viewCache.accountHeaderExtraTopPadding.setVisibility(View.GONE);
        }
        */
        // aurora <wangth> <2013-9-4> modify for auroro ui begin

        // Bind the group data
        //Uri groupUri = getGroupUriFromId(entry.getGroupId());
        Uri groupUri = getGroupUriFromIdAndAccountInfo(entry.getGroupId(), entry.getAccountName(), entry.getAccountType());
        // aurora <wangth> <2013-9-4> remove for auroro ui begin
        /*
        String memberCountString = mContext.getResources().getQuantityString(
                R.plurals.group_list_num_contacts_in_group, entry.getMemberCount(),
                entry.getMemberCount());
        */
        // aurora <wangth> <2013-9-4> remove for auroro ui end
        viewCache.setUri(groupUri);
        viewCache.groupTitle.setText(entry.getTitle());
        viewCache.groupMemberCount.setText(String.valueOf(entry.getMemberCount())+"位");
        //aurora <wangth> <2013-9-16> add for auroro ui begin
        try {
            Typeface tf = Typeface.createFromFile("system/fonts/number.ttf");
            viewCache.groupMemberCount.setTypeface(tf);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        auroraBindSectionHeader(viewCache.header, entry, position);
//        aurora.widget.AuroraListView.auroraGetAuroraStateListDrawableFromIndex(viewCache.content, position);

        if (mSelectionVisible) {
        	convertView.setActivated(isSelectedGroup(groupUri));
        }
        
        return convertView;
    }
    
    // aurora <wangth> <2013-12-24> add for aurora begin
    private void auroraBindSectionHeader(LinearLayout headerUi, GroupListItem item, int position) {
        if (headerUi == null) {
            return;
        } 
        if (headerUi != null) {
            headerUi.removeAllViews();
        }
        
//        TextView header = new TextView(mContext);
//        header.setSingleLine();
//        header.setTextAppearance(mContext, R.style.aurora_list_header_style);
//        int paddingLeft = mContext.getResources().getDimensionPixelSize(
//                R.dimen.aurora_group_entrance_left_margin);
//        header.setPadding(paddingLeft, 6, 0, 0);
//        header.setMaxWidth(300);
//        header.setTextColor(mContext.getResources().getColor(
//                R.color.aurora_contact_list_header_color));
//        ViewGroup.LayoutParams params = headerUi.getLayoutParams();
//        params.height = mContext.getResources().getDimensionPixelSize(
//                R.dimen.aurora_edit_group_margin_top);
//        header.setHeight(params.height);
//        header.setGravity(Gravity.BOTTOM);
//        headerUi.setEnabled(false);
//        headerUi.setClickable(false);
//        headerUi.addView(header);
//        headerUi.setLayoutParams(params);
//        headerUi.setVisibility(View.GONE);
        
        ViewGroup.LayoutParams params = headerUi.getLayoutParams();
		params.height = mContext.getResources().getDimensionPixelSize(
				R.dimen.aurora_edit_group_margin_top);
		AuroraTextView tv = new AuroraTextView(mContext);
		tv.setSingleLine();
		int paddingLeft = mContext.getResources()
				.getDimensionPixelSize(
						R.dimen.aurora_group_entrance_left_margin);
		tv.setTextAppearance(mContext, R.style.aurora_list_header_style);
		tv.setHeight(params.height);
		tv.setPadding(paddingLeft, 0, 0, 0);
		tv.setGravity(Gravity.CENTER_VERTICAL);
		headerUi.setBackgroundColor(mContext.getResources().getColor(R.color.contact_list_header_background_color));
		headerUi.removeAllViews();
		headerUi.setEnabled(false);
		headerUi.setClickable(false);
		headerUi.addView(tv);
		headerUi.setLayoutParams(params);					
		headerUi.setVisibility(View.VISIBLE);
		
        
        if (position == 0) {
            if (item.getAccountType().equals(AccountType.ACCOUNT_TYPE_LOCAL_PHONE)) {
                tv.setText(R.string.aurora_local_group_show);
            } else {
                tv.setText(item.getAccountName());
            }
            
            headerUi.setVisibility(View.VISIBLE);
        } else {
            GroupListItem preEentry = getItem(position - 1);
            if (item.getAccountName() != null && preEentry.getAccountName() != null &&
                    !item.getAccountName().equals(preEentry.getAccountName())) {
                tv.setText(item.getAccountName());
                headerUi.setVisibility(View.VISIBLE);
            } else {
                headerUi.setVisibility(View.GONE);
            }
        }
    }
    // aurora <wangth> <2013-12-24> add for aurora end

    private void bindHeaderView(GroupListItem entry, GroupListItemViewCache viewCache) {
        AccountType accountType = mAccountTypeManager.getAccountType(
                entry.getAccountType(), entry.getDataSet());
        
        if (ContactsApplication.sIsGnContactsSupport) {
        	viewCache.accountType.setTextColor(ResConstant.sHeaderTextColor);
        	
            // Gionee:wangth 20120718 add for CR00651197 begin
            if (accountType == null || null == accountType.accountType) {
                return;
            }
            // Gionee:wangth 20120718 add for CR00651197 end
        	if (AccountType.ACCOUNT_TYPE_LOCAL_PHONE.endsWith(accountType.accountType)) {
        	    //Gionee:huangzy 20120531 modify for CR00611609 start
        		viewCache.accountType.setText(mContext.getString(R.string.gn_only_save_local_label));
        		viewCache.accountName.setText(mContext.getString(R.string.gn_phone_label));
                //Gionee:huangzy 20120531 modify for CR00611609 end                
        		return;
        	}
        }
        viewCache.accountType.setText(accountType.getDisplayLabel(mContext).toString());

        /*
         * Bug Fix by Mediatek Begin.
         *   Original Android's code:
         *     viewCache.accountName.setText(entry.getAccountName());
         *   CR ID: ALPS00117716
         *   Descriptions: 
         */
        String displayName = null;
        String displayNameForTablet = null;
        displayName = AccountFilterUtil.getAccountDisplayNameByAccount(
                entry.getAccountType(), entry.getAccountName());
        if (null == displayName) {
            if (PhoneCapabilityTester.isUsingTwoPanes(mContext)) {
            	displayNameForTablet = entry.getAccountName();
            	if (displayNameForTablet.equals("Phone")) {
            	  displayNameForTablet = mContext.getResources().getString(R.string.local_device_account_name);
            	}
            	viewCache.accountName.setText(displayNameForTablet);
            }
            else
              viewCache.accountName.setText(entry.getAccountName());
        } else {
            viewCache.accountName.setText(displayName);
        }
        /*
         * Bug Fix by Mediatek End.
         */
    }

    private static Uri getGroupUriFromId(long groupId) {
        return ContentUris.withAppendedId(Groups.CONTENT_URI, groupId);
    }

    private Uri getGroupUriFromIdAndAccountInfo(long groupId, String accountName, String accountType) {
        Uri retUri = ContentUris.withAppendedId(Groups.CONTENT_URI, groupId);
        if (accountName != null && accountType != null) {
            retUri = GroupUriWithAccountInfo(retUri, accountName, accountType);
        }
        return retUri;
    }

    /**
     * Cache of the children views of a contact detail entry represented by a
     * {@link GroupListItem}
     */
    public static class GroupListItemViewCache {
        public final TextView accountType;
        public final TextView accountName;
        public final TextView groupTitle;
        public final TextView groupMemberCount;
        // aurora <wangth> <2013-9-4> remove for auroro ui begin
        /*
        public final View accountHeader;
        public final View accountHeaderExtraTopPadding;
        */
        // aurora <wangth> <2013-9-4> remove for auroro ui end
//        public final View divider;
        private Uri mUri;
        
        public final LinearLayout header;
        public final LinearLayout content;

        public GroupListItemViewCache(View view) {
            accountType = (TextView) view.findViewById(R.id.account_type);
            accountName = (TextView) view.findViewById(R.id.account_name);
            groupTitle = (TextView) view.findViewById(R.id.label);
            groupMemberCount = (TextView) view.findViewById(R.id.count);
            // aurora <wangth> <2013-9-4> modify for auroro ui begin
            /*
            accountHeader = view.findViewById(R.id.group_list_header);
            accountHeaderExtraTopPadding = view.findViewById(R.id.header_extra_top_padding);
            divider = view.findViewById(R.id.divider);
            // gionee xuhz 20121205 add for GIUI2.0 start
            if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
            	accountType.setVisibility(View.GONE);
            	accountName.setTextColor(ResConstant.sHeaderTextColor);
            }
            // gionee xuhz 20121205 add for GIUI2.0 end
            */
//            divider = view.findViewById(com.aurora.R.id.aurora_listview_divider);
            header = (LinearLayout) view.findViewById(com.aurora.R.id.aurora_list_header);
            content = (LinearLayout) view.findViewById(com.aurora.R.id.content);
            // aurora <wangth> <2013-9-4> modify for auroro ui end
        }

        public void setUri(Uri uri) {
            mUri = uri;
        }

        public Uri getUri() {
            return mUri;
        }
    }

    private Uri GroupUriWithAccountInfo(final Uri groupUri, String accountName, String accountType) {
        if (groupUri == null) {
            return groupUri;
        }

        Uri retUri = groupUri;

        AccountWithDataSet account = null;
        final List<AccountWithDataSet> accounts = AccountTypeManager.getInstance(mContext)
                .getGroupWritableAccounts();
        int i = 0;
        int slotId = -1;
        for (AccountWithDataSet ac : accounts) {
            if (ac.name.equals(accountName) && ac.type.equals(accountType)) {
                account = accounts.get(i);
                if (account instanceof AccountWithDataSetEx) {
                    slotId = ((AccountWithDataSetEx) account).getSlotId();
                }
            }
            i++;
        }
        retUri = groupUri.buildUpon().appendPath(String.valueOf(slotId)).appendPath(accountName)
                .appendPath(accountType).build();

        return retUri;
    }
}
