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

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.ui.MessageUtils;
import com.gionee.mms.ui.AddReceiptorTab.ContactsPicker;
import com.gionee.mms.ui.AddReceiptorTab.ViewPagerVisibilityListener;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseExpandableListAdapter;
import aurora.widget.AuroraButton;
import android.widget.CheckBox;
import aurora.widget.AuroraExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import aurora.widget.AuroraExpandableListView.OnChildClickListener;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class GroupFragment extends Fragment implements ContactsPicker, ViewPagerVisibilityListener{
    private Activity mActivity;
    private static final String TAG = "GroupFragment";

    private AuroraExpandableListView mList;
    private LinearLayout mEmptyView;
    private QueryHandler mQueryHandler;
    private static final int MENU_SELECT_CANCEL = 0;
    private static final int MENU_SELECT_DONE = 1;
    private static final int MENU_SELECT_ALL = 2;

    final static class ContactListItemCache {
        public String mName;

        // common contact
        public String mNumber;
        public String mCity;

        // group id
        public int mId;

        // if it is a parent
        public boolean mIsParent;
        public boolean mExpanded;
        public int mChildCount;
        public int mEndChildIndex;
        public int mParentIndex;
        public boolean mIsChecked;
        public CheckBox mCheckBox;
    }

    final static class ListItem {
        public int mIndexInChace;
    }

    private ArrayList<ContactListItemCache> mContactsList = new ArrayList<ContactListItemCache>();
    private ContactGroupExpandableListAdapter mAdapter;
    final static int LIST_TAG_GROUP = 1;
    final static int LIST_TAG_NORMAL_CONTACT = 2;

    private interface DataGroupsQuery {
        //gionee gaoj 2012-5-7 modified for CR00573934 start
        public static final String GROUP_SELECTION = Groups.DELETED + "=0";
        //gionee gaoj 2012-4-10 modified for CR00573934 end
        public static final String GROUP_SORDORDER = Groups.SYSTEM_ID + " DESC, " + Groups.TITLE;
        public static final String[] GROUP_PROJECTION = new String[] {
                Groups._ID, Groups.TITLE
        };
        public static final int GROUPS_ID = 0;
        public static final int GROUPS_TITLE = 1;
    }

    public static interface DataContactsQuery {
        final String GROUP_MEMBER_SHIP = Data.MIMETYPE + "='" + GroupMembership.CONTENT_ITEM_TYPE
                + "'";
        final String CONTACTS_SORDORDER = "sort_key";
        final String[] CONTACTS_PROJECTION = new String[] {
                Data._ID, Data.CONTACT_ID, Data.RAW_CONTACT_ID, Data.DISPLAY_NAME, Data.PHOTO_ID
        };

        final int DATA_ID = 0;
        final int DATA_CONTACT_ID = 1;
        final int DATA_RAW_CONTACT_ID = 2;
        final int DATA_DISPLAY_NAME = 3;
        final int DATA_PHOTO_ID = 4;
    }

    public static Cursor getGroupContacts(ContentResolver contentresolver, int nGroupId) {
        String strSelection = Data.DATA1 + "=?" + " AND " + Data.MIMETYPE + "='"
                + GroupMembership.CONTENT_ITEM_TYPE + "'";

        return contentresolver.query(Data.CONTENT_URI, DataContactsQuery.CONTACTS_PROJECTION,
                strSelection, new String[] {
                    String.valueOf(nGroupId)
                }, Data.DISPLAY_NAME + " ASC");
    }

    public static String getGroupsName(Context context, String name) {
        if (name == null) {
            return null;
        }
        Resources res = context.getResources();
        if ("Co-worker".equals(name)) {
            return res.getString(R.string.groups_coworker);
        }

        if ("Family".equals(name)) {
            return res.getString(R.string.groups_family);
        }
        if ("Friends".equals(name)) {
            return res.getString(R.string.groups_friends);
        }
        if ("Schoolmate".equals(name)) {
            return res.getString(R.string.groups_schoolmate);
        }
        if ("VIP".equals(name)) {
            return res.getString(R.string.groups_vip);
        }

        if ("All Contacts".equals(name)) {
            return res.getString(R.string.showAllGroups);
        }
        if ("SIM".equals(name)) {
            return res.getString(R.string.sim);
        }
        return name;
    }

    private class QueryHandler extends AsyncQueryCallbackHandler {
        protected final WeakReference<AddReceiptorTab> mActivity;
        Context mContext;

        public QueryHandler(Context context) {
            super(context.getContentResolver());
            mActivity = new WeakReference<AddReceiptorTab>((AddReceiptorTab) context);
            mContext = context;
        }

        @Override
        public void execQuery() {
            ContactsCacheSingleton.ContactListItemCache favoriteItem = new ContactsCacheSingleton.ContactListItemCache();
            int allChildSize = 0;
            Cursor contactGroupsCursor = GroupFragment.this.mActivity.getContentResolver().query(Groups.CONTENT_SUMMARY_URI,
                    DataGroupsQuery.GROUP_PROJECTION, DataGroupsQuery.GROUP_SELECTION, null,
                    DataGroupsQuery.GROUP_SORDORDER);

            if (null != contactGroupsCursor) {
                contactGroupsCursor.moveToPosition(-1);
                while (contactGroupsCursor.moveToNext()) {
                    Cursor contactCursor = getGroupContacts(mContext.getContentResolver(),
                            contactGroupsCursor.getInt(DataGroupsQuery.GROUPS_ID));
                    if (contactCursor != null) {
                        contactCursor.moveToPosition(-1);
                        ArrayList<ContactsCacheSingleton.ContactListItemCache> children = new ArrayList<ContactsCacheSingleton.ContactListItemCache>();
                        while (contactCursor.moveToNext()) {
                            long contactId = contactCursor.getLong(contactCursor
                                    .getColumnIndex(Data.CONTACT_ID));
                            ContactsCacheSingleton.getInstance().getContactsAndNumber(contactId,
                                    children);
                        }
                        String groupName = getGroupsName(mContext,
                                contactGroupsCursor.getString(DataGroupsQuery.GROUPS_TITLE));
                        mAdapter.addOneGroup(groupName, children);
                        contactCursor.close();
                        allChildSize += children.size();
                    }
                }
            }
            contactGroupsCursor.close();
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Object result) {
            mList.setAdapter(mAdapter);
            if(mList.getCount() > 0) {
                mList.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(View.GONE);
            } else {
                mList.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
            }
            //Gionee <zhouyj> <2013-04-25> add for CR00801550 start
            if (GroupFragment.this.mActivity != null) {
                GroupFragment.this.mActivity.invalidateOptionsMenu();
            }
            //Gionee <zhouyj> <2013-04-25> add for CR00801550 end
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        mActivity = AddReceiptorTab.mCurrent;
        mQueryHandler = new QueryHandler(mActivity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        View fragmentView = inflater.inflate(R.layout.gn_group_list_activity, container, false);
        mList = (AuroraExpandableListView) fragmentView.findViewById(R.id.group_expandable_list);

        mEmptyView = (LinearLayout) fragmentView.findViewById(R.id.gn_group_empty);
        TextView emptytTextView = (TextView) fragmentView.findViewById(R.id.gn_group_empty_text);
        if (MmsApp.mDarkStyle) {
            emptytTextView.setTextColor(mActivity.getResources().getColor(R.color.gn_dark_color_bg));
        }
        return fragmentView;
    }

    private MenuItem mMenuSelectAllItem;
    private MenuItem mMenuOkItem;
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        // select all
        mMenuSelectAllItem = menu.findItem(R.id.gn_add_select_all);
        mMenuSelectAllItem.setEnabled(true);
        if(mEmptyView.getVisibility() == View.VISIBLE || mAdapter.getAllChildCount() == 0) {
            mMenuSelectAllItem.setTitle(R.string.select_all);
            mMenuSelectAllItem.setEnabled(false);
        } else if(isAllChecked()) {
            mMenuSelectAllItem.setTitle(R.string.unselect_all);
        } else {
            mMenuSelectAllItem.setTitle(R.string.select_all);
        }
        
        // ok
        mMenuOkItem = menu.findItem(R.id.gn_add_select_done);
        int checkedCount = ContactsCacheSingleton.getInstance().getCheckedCount();
        if(checkedCount == 0) {
            mMenuOkItem.setTitle(R.string.gn_confirm);
            mMenuOkItem.setEnabled(false);
        } else {
            mMenuOkItem.setTitle(getString(R.string.gn_confirm) + "(" + checkedCount + ")");
            mMenuOkItem.setEnabled(true);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch(item.getItemId()) {
        case R.id.gn_add_select_all:
            mAdapter.setAllItemCheckState(item.getTitle().toString().equals(getString(R.string.select_all)));
            updateSelectDoneText();
            break;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new ContactGroupExpandableListAdapter(mActivity);
        //mList.setGroupIndicator(getResources().getDrawable(R.drawable.gn_expander_ic_folder));
        // We manually save/restore the listview state
        mList.setSaveEnabled(false);
        mList.setOnChildClickListener(new OnChildClickListener() {
            @Override
            public boolean onChildClick(AuroraExpandableListView arg0, View arg1, int arg2, int arg3,
                    long arg4) {
                mAdapter.childCheckBoxClicked(arg2, arg3, arg1);
                int checkCount = mAdapter.getCheckedCount();
                updateSelectDoneText();
                mAdapter.notifyDataSetChanged();
                return true;
            }
        });

        mAdapter.setGroupCheckBoxClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                CheckBox chk = (CheckBox) arg0;
                int groupPos = (Integer) chk.getTag();
                mAdapter.groupCheckBoxClicked(groupPos, chk.isChecked());
                int checkCount = mAdapter.getCheckedCount();
                updateSelectDoneText();
                if (mAdapter.getAllChildCount() == 0) {
                    return;
                }
                mAdapter.notifyDataSetChanged();
            }
        });
        mQueryHandler.startQuery(0, null, null, null, null, null, null);
    }
    
    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mAdapter.notifyDataSetChanged();
        updateSelectDoneText();
    }

    public static class ContactGroupExpandableListAdapter extends BaseExpandableListAdapter {
        private Context mContext;
        public class ContactGroupData {
            String mName = null;
            ArrayList<ContactsCacheSingleton.ContactListItemCache> mChildren = null;
        }
        private ArrayList<ContactGroupData> mGroups = new ArrayList<ContactGroupData>();
        public void addOneGroup(String groupName,
                ArrayList<ContactsCacheSingleton.ContactListItemCache> children) {
            ContactGroupData oneGroup = new ContactGroupData();
            oneGroup.mName = groupName;
            oneGroup.mChildren = children;
            mAllChildCount += children.size();
            mGroups.add(oneGroup);
        }

        private OnClickListener mCheckBoxClickListener;

        public void setGroupCheckBoxClickListener(View.OnClickListener listener) {
            mCheckBoxClickListener = listener;
        }

        public ContactGroupExpandableListAdapter(Context context) {
            mContext = context;
            // mInflater = LayoutInflater.from(context);
        }

        public Object getChild(int groupPosition, int childPosition) {
            return mGroups.get(groupPosition).mChildren.get(childPosition);
        }

        public long getChildId(int groupPosition, int childPosition) {
            return groupPosition * 1000 + childPosition;
        }

        public int getChildrenCount(int groupPosition) {
            return mGroups.get(groupPosition).mChildren.size();
        }

        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                View convertView, ViewGroup parent) {
            Log.i(TAG, "groupPosition = " + groupPosition);

            ContactListItemView view;
            if (convertView == null) {
                view = new ContactListItemView(mContext, null);
            } else {
                view = (ContactListItemView) convertView;
            }
            ContactsCacheSingleton.ContactListItemCache cache = mGroups.get(groupPosition).mChildren
                    .get(childPosition);
            view.getCheckBox().setChecked(cache.mIsChecked);
            view.getNameTextView().setText(cache.getNameString());
            view.getNameTextView().setPadding(10, 0, 0, 0);
            view.getDataView().setPadding(10, 0, 0, 0);
            // Aurora xuyong 2014-07-19 modified for sougou start
            String area = MessageUtils.getNumAreaFromAora(mContext, cache.mNumber);
            // Aurora xuyong 2014-07-19 modified for sougou end
            if (area == null || "".equals(area)) {
                view.getDataView().setText(cache.mNumber);
            } else {
                view.getDataView().setText(cache.mNumber + "  " + area);
            }
            return view;
        }

        public Object getGroup(int groupPosition) {
            return mGroups.get(groupPosition);
        }

        public int getGroupCount() {
            return mGroups.size();
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                ViewGroup parent) {

            View retView = convertView;
            if (convertView == null) {
                retView = LinearLayout.inflate(mContext, R.layout.gn_group_list_group_item, null);
            }
            TextView title = (TextView) retView.findViewById(R.id.groupName);
            CheckBox checkBox = (CheckBox) retView.findViewById(R.id.group_item_check_box);
            checkBox.setTag(groupPosition);
            checkBox.setChecked(false);
            for (ContactsCacheSingleton.ContactListItemCache child : mGroups.get(groupPosition).mChildren) {
                if (child.mIsChecked == true) {
                    checkBox.setChecked(true);
                }
            }

            // setGroupCheckBoxClick(checkBox);
            if (mCheckBoxClickListener != null) {
                checkBox.setOnClickListener(mCheckBoxClickListener);
            }
            title.setText(mGroups.get(groupPosition).mName + 
                    " [" + mGroups.get(groupPosition).mChildren.size() + "]");
            title.setEllipsize(TruncateAt.MIDDLE);
            Log.i(TAG, "getGroupView" + " " + mGroups.get(groupPosition).mName);
            return retView;
        }

        @Override
        public long getGroupId(int arg0) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isChildSelectable(int arg0, int arg1) {
            return true;
        }

        public void groupCheckBoxClicked(int groupPosition, boolean checked) {
            for (int i = 0; i < mGroups.get(groupPosition).mChildren.size(); i++) {
                ContactsCacheSingleton.ContactListItemCache item = mGroups.get(groupPosition).mChildren
                        .get(i);

                item.mIsChecked = checked;
            }
        }

        public void childCheckBoxClicked(int groupPosition, int childPosition, View convertView) {
            final ContactListItemView view = (ContactListItemView) convertView;
            // final ListItem listitem = getItem(position);
            ContactsCacheSingleton.ContactListItemCache item = mGroups.get(groupPosition).mChildren
                    .get(childPosition);
            view.getCheckBox().setChecked(!view.getCheckBox().isChecked());
            item.mIsChecked = view.getCheckBox().isChecked();
        }

        public int getCheckedCount() {
            int retValue = 0;
            for (int groupIndex = 0; groupIndex < mGroups.size(); groupIndex++) {
                for (int i = 0; i < mGroups.get(groupIndex).mChildren.size(); i++) {
                    ContactsCacheSingleton.ContactListItemCache item = mGroups.get(groupIndex).mChildren
                            .get(i);
                    if (item.mIsChecked == true) {
                        retValue++;
                    }
                }
            }

            return retValue;
        } // getCheckedCount end

        int mAllChildCount = 0;

        public int getAllChildCount() {
            return mAllChildCount;
        }

        public void setAllItemCheckState(boolean isCheck) {
            for (int groupIndex = 0; groupIndex < mGroups.size(); groupIndex++) {
                for (int i = 0; i < mGroups.get(groupIndex).mChildren.size(); i++) {
                    ContactsCacheSingleton.ContactListItemCache item = mGroups.get(groupIndex).mChildren
                            .get(i);
                    item.mIsChecked = isCheck;
                }
            }
            notifyDataSetChanged();
        } // setAllItemCheckState end

    }

    public void updateSelectDoneText() {
        //Gionee <zhouyj> <2013-04-24> modify for CR00801550 start
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        //Gionee <zhouyj> <2013-04-24> modify for CR00801550 end
        // gionee zhouyj 2013-01-23 modify for CR00765961 start 
        if (mActivity != null) {
            mActivity.invalidateOptionsMenu();
        }
        // gionee zhouyj 2013-01-23 modify for CR00765961 end 
    }
    
    private boolean isAllChecked() {
        boolean isAllChecked = true;
        for (int groupIndex = 0; groupIndex < mAdapter.mGroups.size(); groupIndex++) {
            for (int i = 0; i < mAdapter.mGroups.get(groupIndex).mChildren.size(); i++) {
                ContactsCacheSingleton.ContactListItemCache item = mAdapter.mGroups.get(groupIndex).mChildren
                        .get(i);
                if (item.mIsChecked == false) {
                    isAllChecked = false;
                    break;
                }
            }
        }
        return isAllChecked;
    }
    
    @Override
    public void markAll() {
        mAdapter.setAllItemCheckState(true);
        updateSelectDoneText();
    }

    @Override
    public void updateButton() {
        updateSelectDoneText();
    }

    //Gionee <zhouyj> <2013-04-24> add for CR00801550 start
    @Override
    public void onVisibilityChanged(boolean visible) {
        // TODO Auto-generated method stub
    }
    //Gionee <zhouyj> <2013-04-24> add for CR00801550 end
}
