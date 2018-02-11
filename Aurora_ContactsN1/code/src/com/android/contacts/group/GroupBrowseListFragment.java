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

import com.android.contacts.AuroraGroupListLoader;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.GroupListLoader;
import com.android.contacts.R;
import com.android.contacts.activities.AuroraGroupDetailActivity;
import com.android.contacts.activities.AuroraGroupEditorActivity;
import com.android.contacts.group.GroupBrowseListAdapter.GroupListItemViewCache;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.model.AccountType;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.AccountWithDataSet;
import com.android.contacts.widget.AutoScrollListView;
import com.mediatek.contacts.list.MultiContactsPickerBaseFragment;
import com.mediatek.contacts.model.AccountWithDataSetEx;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import aurora.preference.AuroraPreferenceManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import aurora.widget.AuroraListView;
import android.widget.TextView; 
// The following lines are provided and maintained by Mediatek Inc.
import android.widget.ProgressBar;
import android.view.animation.AnimationUtils;
import android.os.Handler;
import android.os.Message;

// The previous lines are provided and maintained by Mediatek Inc.
/**
 * Fragment to display the list of groups.
 */
public class GroupBrowseListFragment extends Fragment
        implements OnFocusChangeListener, OnTouchListener {

    /**
     * Action callbacks that can be sent by a group list.
     */
    public interface OnGroupBrowserActionListener  {

        /**
         * Opens the specified group for viewing.
         *
         * @param groupUri for the group that the user wishes to view.
         */
        void onViewGroupAction(Uri groupUri);

    }

    private static final String TAG = "GroupBrowseListFragment";

    private static final int LOADER_GROUPS = 1;

    private Context mContext;
    private Cursor mGroupListCursor;

    private boolean mSelectionToScreenRequested;

    private static final String EXTRA_KEY_GROUP_URI = "groups.groupUri";

    private View mRootView;
    private AutoScrollListView mListView;
    // gionee xuhz 20120514 modify start
    private View mGnEmptyView;
    // gionee xuhz 20120514 modify end
    private TextView mEmptyView;
    private View mAddAccountsView;
    private View mAddAccountButton;

    private GroupBrowseListAdapter mAdapter;
    private boolean mSelectionVisible;
    private Uri mSelectedGroupUri;

    private int mVerticalScrollbarPosition = View.SCROLLBAR_POSITION_RIGHT;

    private OnGroupBrowserActionListener mListener;

    public GroupBrowseListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mSelectedGroupUri = savedInstanceState.getParcelable(EXTRA_KEY_GROUP_URI);
            if (mSelectedGroupUri != null) {
                // The selection may be out of screen, if rotated from portrait to landscape,
                // so ensure it's visible.
                mSelectionToScreenRequested = true;
            }
        }

        // gionee xuhz 20120514 modify start
    
            // aurora <wangth> <2013-10-21> modify for aurora ui begin 
            //mRootView = inflater.inflate(R.layout.gn_group_browse_list_fragment, null);
            mRootView = inflater.inflate(R.layout.aurora_group_browse_list_fragment, null);
//            View noGroupView = (View) mRootView.findViewById(R.id.no_group);
//            if (null != noGroupView) {
//                initHeaderView(noGroupView);
//            }
            // aurora <wangth> <2013-10-21> modify for aurora ui end
            mGnEmptyView = (View) mRootView.findViewById(R.id.empty);
      
        // gionee xuhz 20120514 modify end

        /*
         * Bug Fix by Mediatek Begin. Original Android's code: CR ID:
         * ALPS00115673 Descriptions: add wait cursor
         */

        mLoadingContainer = mRootView.findViewById(R.id.loading_container);
        mLoadingContact = (TextView) mRootView.findViewById(R.id.loading_contact);
        mLoadingContact.setVisibility(View.GONE);
        mProgress = (ProgressBar) mRootView.findViewById(R.id.progress_loading_contact);
        mProgress.setVisibility(View.GONE);

        /*
         * Bug Fix by Mediatek End.
         */

        mAdapter = new GroupBrowseListAdapter(mContext);
        mAdapter.setSelectionVisible(mSelectionVisible);
        mAdapter.setSelectedGroup(mSelectedGroupUri);

        mListView = (AutoScrollListView) mRootView.findViewById(R.id.list);
        mListView.setOnFocusChangeListener(this);
        mListView.setOnTouchListener(this);
        
        //aurora <wangth> <2013-9-4> add for auroro ui begin
//        addGroupBrowseListHeaderView(inflater);
        //addGroupBrowseListFooterView(inflater);
        mListView.auroraSetSelectorToContentBg(true);
        //aurora <wangth> <2013-9-4> add for auroro ui end
        
        mListView.setAdapter(mAdapter);        
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GroupListItemViewCache groupListItem = (GroupListItemViewCache) view.getTag();
                if (groupListItem != null) {
                	viewGroup(groupListItem.getUri());
                }
                /*
                 * New feature by Mediatek Begin
                 * Original Android code:
                 * if (groupListItem != null) {
                    viewGroup(groupListItem.getUri());
                    }
                 */
                /*
                GroupListItem item =  (GroupListItem)mListView.getAdapter().getItem(position);               
                String accountName = item.getAccountName();
                String accountType = item.getAccountType();         
                Log.i(TAG, accountName+"-------------accountName");
                Log.i(TAG, accountType+"-------------accountType");  
                
                 AccountWithDataSet account = null;
                 final List<AccountWithDataSet> accounts =
                     AccountTypeManager.getInstance(mContext).getGroupWritableAccounts();
                 int i = 0;
                 int slotId = -1;
                 for(AccountWithDataSet ac :accounts){
                     Log.i(TAG, ac.name+ "-------------ac.type");
                     Log.i(TAG, ac.type+ "-------------ac.type");
                     if (ac.name.equals(accountName) && ac.type.equals(accountType)) {
                         account = accounts.get(i);
                         if (account instanceof AccountWithDataSetEx){
                                slotId = ((AccountWithDataSetEx) account).getSlotId();
                                Log.i(TAG, slotId + "-------------slotId++++++++account");
                         }   
                     }
                     i++;
                 }              
                 Uri uri = groupListItem.getUri().buildUpon().appendPath(String.valueOf(slotId)).appendPath(accountName).appendPath(accountType).build();
                 Log.i(TAG, uri+"-------------uri");
                if (groupListItem != null) {
                    viewGroup(uri);
                }
                */

                /*
                 * New feature by Mediatek End
                 */
            }
        });

      

        mAddAccountsView = mRootView.findViewById(R.id.add_accounts);
        mAddAccountButton = mRootView.findViewById(R.id.add_account_button);
        mAddAccountButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_ADD_ACCOUNT);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                intent.putExtra(Settings.EXTRA_AUTHORITIES,
                        new String[] { ContactsContract.AUTHORITY });
                startActivity(intent);
            }
        });
        setAddAccountsVisibility(!ContactsUtils.areGroupWritableAccountsAvailable(mContext));

        return mRootView;
    }

    public void setVerticalScrollbarPosition(int position) {
        if (mVerticalScrollbarPosition != position) {
            mVerticalScrollbarPosition = position;
            configureVerticalScrollbar();
        }
    }

    private void configureVerticalScrollbar() {
        mListView.setVerticalScrollbarPosition(mVerticalScrollbarPosition);
        mListView.setScrollBarStyle(AuroraListView.SCROLLBARS_OUTSIDE_OVERLAY);
        int leftPadding = 0;
        int rightPadding = 0;
        if (mVerticalScrollbarPosition == View.SCROLLBAR_POSITION_LEFT) {
            leftPadding = mContext.getResources().getDimensionPixelOffset(
                    R.dimen.list_visible_scrollbar_padding);
        } else {
            rightPadding = mContext.getResources().getDimensionPixelOffset(
                    R.dimen.list_visible_scrollbar_padding);
        }
        mListView.setPadding(leftPadding, mListView.getPaddingTop(),
                rightPadding, mListView.getPaddingBottom());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    @Override
    public void onStart() {
        getLoaderManager().initLoader(LOADER_GROUPS, null, mGroupLoaderListener);
        super.onStart();
    }

    /**
     * The listener for the group meta data loader for all groups.
     */
    private final LoaderManager.LoaderCallbacks<Cursor> mGroupLoaderListener =
            new LoaderCallbacks<Cursor>() {

        @Override
        public CursorLoader onCreateLoader(int id, Bundle args) {
            /*
             * Bug Fix by Mediatek Begin. Original Android's code: CR ID:
             * ALPS00115673 Descriptions: add wait cursor
             */
            isFinished = false;
            mHandler.sendMessageDelayed(mHandler.obtainMessage(WAIT_CURSOR_START),
                    WAIT_CURSOR_DELAY_TIME);

            /*
             * Bug Fix by Mediatek End.
             */
 

            SharedPreferences mPrefs = AuroraPreferenceManager.getDefaultSharedPreferences(getActivity());
            int filterInt = mPrefs.getInt("filter.type", -1);
            if (filterInt == -3) {
                return new AuroraGroupListLoader(mContext);
            } else {
                return new GroupListLoader(mContext);
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            /*
             * Bug Fix by Mediatek Begin. Original Android's code: CR ID:
             * ALPS00115673 Descriptions: add wait cursor
             */
            Log.i(TAG, "onLoadFinished222222222222222");

            isFinished = true;
            mLoadingContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                    android.R.anim.fade_out));
            mLoadingContainer.setVisibility(View.GONE);
            mLoadingContact.setVisibility(View.GONE);
            mProgress.setVisibility(View.GONE);
            /*
             * Bug Fix by Mediatek End.
             */
            mGroupListCursor = data;
            bindGroupList();
            // aurora <wangth> <2013-12-24> add for aurora begin
            mListView.setEmptyView(mGnEmptyView);
            // aurora <wangth> <2013-12-24> add for aurora end
        }

        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };

    private void bindGroupList() {


        setAddAccountsVisibility(!ContactsUtils.areGroupWritableAccountsAvailable(mContext));
        if (mGroupListCursor == null) {
            return;
        }
        mAdapter.setCursor(mGroupListCursor);
        
        // aurora <wangth> <2013-9-4> add for auroro ui begin
//        setNoGroupMemberCount(mContext);
        // aurora <wangth> <2013-9-4> add for auroro ui end

        if (mSelectionToScreenRequested) {
            mSelectionToScreenRequested = false;
            requestSelectionToScreen();
        }

        mSelectedGroupUri = mAdapter.getSelectedGroup();
        if (mSelectionVisible && mSelectedGroupUri != null) {
            viewGroup(mSelectedGroupUri);
        }
    }

    public void setListener(OnGroupBrowserActionListener listener) {
        mListener = listener;
    }

    public void setSelectionVisible(boolean flag) {
        mSelectionVisible = flag;
        if (mAdapter != null) {
            mAdapter.setSelectionVisible(mSelectionVisible);
        }
    }

    private void setSelectedGroup(Uri groupUri) {
        mSelectedGroupUri = groupUri;
        mAdapter.setSelectedGroup(groupUri);
        mListView.invalidateViews();
    }

    private void viewGroup(Uri groupUri) {
        setSelectedGroup(groupUri);
        if (mListener != null) mListener.onViewGroupAction(groupUri);
    }

    public void setSelectedUri(Uri groupUri) {
        viewGroup(groupUri);
        mSelectionToScreenRequested = true;
    }

    protected void requestSelectionToScreen() {
        if (!mSelectionVisible) {
            return; // If selection isn't visible we don't care.
        }
        int selectedPosition = mAdapter.getSelectedGroupPosition();
        if (selectedPosition != -1) {
            mListView.requestPositionToScreen(selectedPosition,
                    true /* smooth scroll requested */);
        }
    }

    private void hideSoftKeyboard() {
        if (mContext == null) {
            return;
        }
        // Hide soft keyboard, if visible
        InputMethodManager inputMethodManager = (InputMethodManager)
                mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mListView.getWindowToken(), 0);
    }

    /**
     * Dismisses the soft keyboard when the list takes focus.
     */
    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (view == mListView && hasFocus) {
            hideSoftKeyboard();
        }
    }

    /**
     * Dismisses the soft keyboard when the list is touched.
     */
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == mListView) {
            hideSoftKeyboard();
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_KEY_GROUP_URI, mSelectedGroupUri);
    }

    public void setAddAccountsVisibility(boolean visible) {
        if (mAddAccountsView != null) {
            mAddAccountsView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    /*
     * Bug Fix by Mediatek Begin. Original Android's code: CR ID: ALPS00115673
     * Descriptions: add wait cursor
     */

    private View mLoadingContainer;

    private TextView mLoadingContact;

    private ProgressBar mProgress;

    public static boolean isFinished = false;

    private static final int WAIT_CURSOR_START = 1230;

    private static final long WAIT_CURSOR_DELAY_TIME = 500;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage msg==== " + msg.what);

            switch (msg.what) {

                case WAIT_CURSOR_START:
                    Log.i(TAG, "start WAIT_CURSOR_START !isFinished : " + !isFinished);
                    if (!isFinished) {
                        mLoadingContainer.setVisibility(View.VISIBLE);
                        mLoadingContact.setVisibility(View.VISIBLE);
                        mProgress.setVisibility(View.VISIBLE);
                    }
                    break;

                default:
                    break;
            }
        }
    };
    /*
     * Bug Fix by Mediatek End.
     */
    
    // aurora <wangth> <2013-9-4> add for auroro ui begin
    // add header view
    private TextView mNoGroupMemberCount;
    private void addGroupBrowseListHeaderView(LayoutInflater inflater) {
        View headerView = inflater.inflate(R.layout.aurora_group_item, null);
        if (mListView != null) {
            mListView.addHeaderView(headerView);
        }
        
        initHeaderView(headerView);
    }
    
    private void initHeaderView(View headerView) {
        headerView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(getActivity(),
                        AuroraGroupDetailActivity.class);
                intent.putExtra(
                        AuroraGroupEditorActivity.EXTRA_GROUP_NAME,
                        mContext.getResources().getString(
                                R.string.aurora_group_no_group));
                intent.putExtra(
                        MultiContactsPickerBaseFragment.EXTRA_ACCOUNT_FILTER_EXINFO,
                        "noGroupId/");
                intent.putExtra(
                        MultiContactsPickerBaseFragment.EXTRA_ACCOUNT_FILTER,
                        ContactListFilter.createAccountFilter(
                                AccountType.ACCOUNT_TYPE_LOCAL_PHONE,
                                AccountType.ACCOUNT_NAME_LOCAL_PHONE, null,
                                null));
                startActivity(intent);
            }
        });
        
        TextView groupTitle = (TextView) headerView.findViewById(R.id.label);
        mNoGroupMemberCount = (TextView) headerView.findViewById(R.id.count);
        //aurora <wangth> <2013-9-16> add for aurora ui begin
        try {
            Typeface tf = Typeface.createFromFile("system/fonts/number.ttf");
            mNoGroupMemberCount.setTypeface(tf);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //aurora <wangth> <2013-9-16> add for aurora ui end
        
        String title = mContext.getResources().getString(R.string.aurora_group_no_group);
        groupTitle.setText(title);
        setNoGroupMemberCount(mContext);
    }
    
    // add footer view
    private void addGroupBrowseListFooterView(LayoutInflater inflater) {
        String[] title = new String[] {null, null, null,};
        title[0] = mContext.getResources().getString(R.string.aurora_group_attribution);
        title[1] = mContext.getResources().getString(R.string.aurora_group_company);
        title[2] = mContext.getResources().getString(R.string.aurora_group_recently_added);
        
        for (int i = 0; i < title.length; i++) {
            if (mListView != null) {
                View footerView = inflater.inflate(R.layout.aurora_group_item, null);
                mListView.addFooterView(footerView);
                TextView groupTitle = (TextView) footerView.findViewById(R.id.label);
                TextView leftHuo = (TextView) footerView.findViewById(R.id.count_left);
                TextView rightHuo = (TextView) footerView.findViewById(R.id.count_right);
                leftHuo.setVisibility(View.GONE);
                rightHuo.setVisibility(View.GONE);
                groupTitle.setText(title[i]);
            }
        }
    }
    
    public void setNoGroupMemberCount(Context context) {
        int count = ContactsUtils.getNoGroupsCount(context);
        
        if (mNoGroupMemberCount != null) {
            mNoGroupMemberCount.setText(String.valueOf(count));
        }
    }
    // aurora <wangth> <2013-9-4> add for auroro ui end
}
