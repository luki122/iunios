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
 * limitations under the License
 */

package com.android.contacts.group;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.android.contacts.ContactPhotoManager;
import com.android.contacts.util.Constants;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.GroupMemberLoader;
import com.android.contacts.GroupMetaDataLoader;
import com.android.contacts.R;
import com.android.contacts.activities.GroupDetailActivity;
import com.android.contacts.interactions.GroupDeletionDialogFragment;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.list.ContactTileAdapter;
import com.android.contacts.list.ContactTileAdapter.ContactEntry;
import com.android.contacts.list.ContactsRequest;
import com.android.contacts.list.ContactTileAdapter.DisplayType;
import com.android.contacts.model.AccountType;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.util.WeakAsyncTask;
import com.mediatek.contacts.list.ContactListMultiChoiceActivity;
import com.mediatek.contacts.list.ContactsIntentResolverEx;
import com.mediatek.contacts.list.MultiContactsPickerBaseFragment;
import com.mediatek.contacts.util.ContactsGroupUtils;
import com.mediatek.contacts.util.ContactsIntent;

import android.accounts.Account;
import android.app.Activity;
import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Rect;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import gionee.provider.GnContactsContract.CommonDataKinds;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.Groups;
import gionee.provider.GnContactsContract.CommonDataKinds.Email;
import gionee.provider.GnContactsContract.CommonDataKinds.GroupMembership;
import gionee.provider.GnContactsContract.CommonDataKinds.Phone;
import gionee.provider.GnContactsContract.CommonDataKinds.StructuredName;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import aurora.widget.AuroraListView;
import android.widget.TextView;
import android.widget.Toast;

// The following lines are provided and maintained by Mediatek Inc.
import android.widget.ProgressBar;
import android.view.animation.AnimationUtils;
import android.os.Handler;
import android.os.Message;
// The previous lines are provided and maintained by Mediatek Inc.
// Gionee zhangxx 2012-05-19 add for CR00601314 begin
import android.os.RemoteException;
import com.mediatek.contacts.util.ContactsGroupUtils.USIMGroupException;
// Gionee zhangxx 2012-05-19 add for CR00601314 end

import aurora.app.AuroraProgressDialog;

/**
 * Displays the details of a group and shows a list of actions possible for the group.
 */
public class GroupDetailFragment extends Fragment implements OnScrollListener {

    public static interface Listener {
        /**
         * The group title has been loaded
         */
        public void onGroupTitleUpdated(String title);

        /**
         * The number of group members has been determined
         */
        public void onGroupSizeUpdated(String size);

        /**
         * The account type and dataset have been determined.
         */
        public void onAccountTypeUpdated(String accountTypeString, String dataSet);

        /**
         * User decided to go to Edit-Mode
         */
        public void onEditRequested(Uri groupUri);

        /**
         * Contact is selected and should launch details page
         */
        public void onContactSelected(Uri contactUri);
    }

    private static final String TAG = "GroupDetailFragment";

    private static final int LOADER_METADATA = 0;
    private static final int LOADER_MEMBERS = 1;

    private Context mContext;

    private View mRootView;
    private ViewGroup mGroupSourceViewContainer;
    private View mGroupSourceView;
    private TextView mGroupTitle;
    private TextView mGroupSize;
    private AuroraListView mMemberListView;
    private View mEmptyView;

    private Listener mListener;

    private ContactTileAdapter mAdapter;
    private ContactPhotoManager mPhotoManager;
    private AccountTypeManager mAccountTypeManager;

    private Uri mGroupUri;
    private long mGroupId;
    private String mGroupName;
    private String mAccountTypeString;
    private String mDataSet;
    private boolean mIsReadOnly;

    private boolean mShowGroupActionInActionBar;
    private boolean mOptionsMenuGroupDeletable;
    private boolean mOptionsMenuGroupPresent;
    private boolean mCloseActivityAfterDelete;
    
    private final int REQUEST_PICK_RINGTONE = 0;

    public GroupDetailFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        mAccountTypeManager = AccountTypeManager.getInstance(mContext);

        Resources res = getResources();
        /*int columnCount = res.getInteger(R.integer.contact_tile_column_count);*/
        int columnCount = 1;
        mAdapter = new ContactTileAdapter(activity, mContactTileListener, columnCount,
                DisplayType.GROUP_MEMBERS);

        configurePhotoLoader();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        setHasOptionsMenu(true);
        // gionee xuhz 20120515 modify start
        if (ContactsApplication.sIsGnContactsSupport) {
            mRootView = inflater.inflate(R.layout.gn_group_detail_fragment, container, false);
        } else {
            mRootView = inflater.inflate(R.layout.group_detail_fragment, container, false);
        }
        // gionee xuhz 20120515 modify end

        mGroupTitle = (TextView) mRootView.findViewById(R.id.group_title);
        mGroupSize = (TextView) mRootView.findViewById(R.id.group_size);
        mGroupSourceViewContainer = (ViewGroup) mRootView.findViewById(
                R.id.group_source_view_container);
        mEmptyView = mRootView.findViewById(android.R.id.empty);
        if (ContactsApplication.sIsGnContactsSupport) {
        	mEmptyView.findViewById(R.id.empty_tip_add_member).setVisibility(View.GONE);
        }
        mMemberListView = (AuroraListView) mRootView.findViewById(android.R.id.list);
        mMemberListView.setAdapter(mAdapter);
        
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
        

        return mRootView;
    }

    public void loadGroup(Uri groupUri) {
        mGroupUri= groupUri;
        startGroupMetadataLoader();
    }

    public void setQuickContact(boolean enableQuickContact) {
        mAdapter.enableQuickContact(enableQuickContact);
    }

    private void configurePhotoLoader() {
        if (mContext != null) {
            if (mPhotoManager == null) {
                mPhotoManager = ContactPhotoManager.getInstance(mContext);
            }
            if (mMemberListView != null) {
                mMemberListView.setOnScrollListener(this);
            }
            if (mAdapter != null) {
                mAdapter.setPhotoLoader(mPhotoManager);
            }
        }
    }

    public void setListener(Listener value) {
        mListener = value;
    }

    public void setShowGroupSourceInActionBar(boolean show) {
        mShowGroupActionInActionBar = show;
    }

    /**
     * Start the loader to retrieve the metadata for this group.
     */
    private void startGroupMetadataLoader() {
        getLoaderManager().restartLoader(LOADER_METADATA, null, mGroupMetadataLoaderListener);
    }

    /**
     * Start the loader to retrieve the list of group members.
     */
    private void startGroupMembersLoader() {
        getLoaderManager().restartLoader(LOADER_MEMBERS, null, mGroupMemberListLoaderListener);
    }

    private final ContactTileAdapter.Listener mContactTileListener =
            new ContactTileAdapter.Listener() {

        @Override
        public void onContactSelected(Uri contactUri, Rect targetRect) {
            mListener.onContactSelected(contactUri);
        }

        // gionee xuhz 20130117 add for CR00765218 start
		@Override
		public void onCreateContextMenu(final Uri contactUri, final String displayName, final int simIndex) {
	    	CharSequence[] items = new CharSequence[1];
	    	items[0] = getString(R.string.gn_remove_from_group);
	    	
			AuroraAlertDialog removeDialog = new AuroraAlertDialog.Builder(getActivity())
					.setTitle(displayName).setTitleDividerVisible(true)
					.setItems(items, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
							case 0:
		                        if (contactUri != null) {
		                        	removeMemberFromGroup(contactUri, displayName, simIndex);
		                        }
								break;
							default:
								break;
							}
						}
					})
					.setCancelIcon(true).create();

			removeDialog.show();
		}
		// gionee xuhz 20130117 add for CR00765218 end
    };

    /**
     * The listener for the group metadata loader.
     */
    private final LoaderManager.LoaderCallbacks<Cursor> mGroupMetadataLoaderListener =
            new LoaderCallbacks<Cursor>() {

        @Override
        public CursorLoader onCreateLoader(int id, Bundle args) {
            /*
             * Bug Fix by Mediatek Begin. Original Android's code: CR ID:
             * ALPS00115673 Descriptions: add wait cursor
             */
            Log.i(TAG, "onCreateLoader");

            isFinished = false;
            mHandler.sendMessageDelayed(mHandler.obtainMessage(WAIT_CURSOR_START),
                    WAIT_CURSOR_DELAY_TIME);

            /*
             * Bug Fix by Mediatek End.
             */
            OCL = System.currentTimeMillis();
            Log.i(TAG,"GroupDetailFragment mGroupMetadataLoaderListener onCreateLoader OCL : "+OCL);
            return new GroupMetaDataLoader(mContext, mGroupUri);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            OLF = System.currentTimeMillis();
            Log.i(TAG,"GroupDetailFragment mGroupMetadataLoaderListener onLoadFinished OLF : "+OLF+" | OLF-OCL = "+(OLF-OCL));
            data.moveToPosition(-1);
            if (data.moveToNext()) {
                boolean deleted = data.getInt(GroupMetaDataLoader.DELETED) == 1;
                if (!deleted) {
                    bindGroupMetaData(data);

                    // Retrieve the list of members
                    startGroupMembersLoader();
                    return;
                }
            }
            updateSize(-1);
            updateTitle(null);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {}
    };

    /**
     * The listener for the group members list loader
     */
    private final LoaderManager.LoaderCallbacks<Cursor> mGroupMemberListLoaderListener =
            new LoaderCallbacks<Cursor>() {

        @Override
        public CursorLoader onCreateLoader(int id, Bundle args) {
            OCL1 = System.currentTimeMillis();
            Log.i(TAG,"GroupDetailFragment mGroupMemberListLoaderListener onCreateLoader OCL1 : "+OCL1);
            return GroupMemberLoader.constructLoaderForGroupDetailQuery(mContext, mGroupId);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            updateSize(data.getCount());
            // The following lines are provided and maintained by Mediatek Inc.
            
            OLF1 = System.currentTimeMillis();
            Log.i(TAG,"GroupDetailFragment mGroupMemberListLoaderListener onLoadFinished OLF1 : "+OLF1+" | OLF1-OCL1 = "+(OLF1-OCL1));
            /*
             * Bug Fix by Mediatek Begin. Original Android's code: CR ID:
             * ALPS00115673 Descriptions: add wait cursor
             */

            isFinished = true;

            mLoadingContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                    android.R.anim.fade_out));

            mLoadingContainer.setVisibility(View.GONE);
            mLoadingContact.setVisibility(View.GONE);
            mProgress.setVisibility(View.GONE);

            Log.i(TAG, "ohonefavoriterfragmetn onloadfinished");

            /*
             * Bug Fix by Mediatek End.
             */
            
            groupMemberSize = data.getCount();
            if(DEBUG)Log.i(TAG, groupMemberSize+"------groupMemberSize mGroupMemberListLoaderListener");
            
            final Cursor cursor =  mContext.getContentResolver().query(
                    Groups.CONTENT_URI,
                    new String[] { Groups._ID, Groups.TITLE },
                            Groups.DELETED + "=0 "
                            +"AND " + Groups.ACCOUNT_NAME+ "= '" + mAccountName 
                            + "'", null, null);
            Log.i(TAG, cursor.getCount()+"-----curosr");
            if (cursor.getCount() <= 1) {
                DISABLE_MOVE_MENU = true;
            }
            else
            {
                // Add for tablet, sine the fragment will not always be recreated as phone
                // Then the variable will not be reset
                DISABLE_MOVE_MENU = false;
            }
            cursor.close();
            getActivity().invalidateOptionsMenu();
            // The previous  lines are provided and maintained by Mediatek Inc.
            mAdapter.setContactCursor(data);
            mMemberListView.setEmptyView(mEmptyView);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {}
    };

    private void bindGroupMetaData(Cursor cursor) {
        cursor.moveToPosition(-1);
        if (cursor.moveToNext()) {
            mAccountTypeString = cursor.getString(GroupMetaDataLoader.ACCOUNT_TYPE);
            mAccountName = cursor.getString(GroupMetaDataLoader.ACCOUNT_NAME);
            mDataSet = cursor.getString(GroupMetaDataLoader.DATA_SET);
            mGroupId = cursor.getLong(GroupMetaDataLoader.GROUP_ID);
            mGroupName = cursor.getString(GroupMetaDataLoader.TITLE);
            mIsReadOnly = cursor.getInt(GroupMetaDataLoader.IS_READ_ONLY) == 1;
            updateTitle(mGroupName);
            // Must call invalidate so that the option menu will get updated
            getActivity().invalidateOptionsMenu ();

            final String accountTypeString = cursor.getString(GroupMetaDataLoader.ACCOUNT_TYPE);
            final String dataSet = cursor.getString(GroupMetaDataLoader.DATA_SET);
            updateAccountType(accountTypeString, dataSet);
        }
    }

    // gionee xuhz 20130117 add for CR00765218 start
    protected void removeMemberFromGroup(Uri contactUri, String displayName, int simIndex) {
        long contactId =  ContentUris.parseId(contactUri);
        long rawContactId = ContactsUtils.queryForRawContactId(mContext.getContentResolver(), contactId);
        long [] rawContactsIds = {rawContactId};
        int [] simIndexs = {simIndex};

        Intent saveIntent = ContactSaveService.createGroupUpdateIntent(mContext, mGroupId, null, 
        		null, rawContactsIds, getClass(), null, mGroupName, mSlotId, null, simIndexs);
        getActivity().startService(saveIntent);
	}
    // gionee xuhz 20130117 add for CR00765218 end

	private void updateTitle(String title) {
        if (mGroupTitle != null) {
            mGroupTitle.setText(title);
        } else {
            mListener.onGroupTitleUpdated(title);
        }
    }

    /**
     * Display the count of the number of group members.
     * @param size of the group (can be -1 if no size could be determined)
     */
    private void updateSize(int size) {
    	if (ContactsApplication.sIsGnContactsSupport) {
    		gnUpdateSize(size);
    		return;
    	}
    	
        String groupSizeString;
        if (size == -1) {
            groupSizeString = null;
        } else {
            String groupSizeTemplateString = getResources().getQuantityString(
                    R.plurals.num_contacts_in_group, size);
            AccountType accountType = mAccountTypeManager.getAccountType(mAccountTypeString,
                    mDataSet);
            groupSizeString = String.format(groupSizeTemplateString, size,
                    accountType.getDisplayLabel(mContext));
        }

        if (mGroupSize != null) {
            mGroupSize.setText(groupSizeString);
        } else {
            mListener.onGroupSizeUpdated(groupSizeString);
        }
    }

    /**
     * Once the account type, group source action, and group source URI have been determined
     * (based on the result from the {@link Loader}), then we can display this to the user in 1 of
     * 2 ways depending on screen size and orientation: either as a button in the action bar or as
     * a button in a static header on the page.
     */
    private void updateAccountType(final String accountTypeString, final String dataSet) {

        // If the group action should be shown in the action bar, then pass the data to the
        // listener who will take care of setting up the view and click listener. There is nothing
        // else to be done by this {@link Fragment}.
        if (mShowGroupActionInActionBar) {
            mListener.onAccountTypeUpdated(accountTypeString, dataSet);
            return;
        }

        final AccountTypeManager manager = AccountTypeManager.getInstance(getActivity());
        final AccountType accountType =
                manager.getAccountType(accountTypeString, dataSet);

        // Otherwise, if the {@link Fragment} needs to create and setup the button, then first
        // verify that there is a valid action.
        if (!TextUtils.isEmpty(accountType.getViewGroupActivity())) {
            if (mGroupSourceView == null) {
                mGroupSourceView = GroupDetailDisplayUtils.getNewGroupSourceView(mContext);
                // Figure out how to add the view to the fragment.
                // If there is a static header with a container for the group source view, insert
                // the view there.
                if (mGroupSourceViewContainer != null) {
                    mGroupSourceViewContainer.addView(mGroupSourceView);
                }
            }

            // Rebind the data since this action can change if the loader returns updated data
            mGroupSourceView.setVisibility(View.VISIBLE);
            GroupDetailDisplayUtils.bindGroupSourceView(mContext, mGroupSourceView,
                    accountTypeString, dataSet);
            mGroupSourceView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Uri uri = ContentUris.withAppendedId(Groups.CONTENT_URI, mGroupId);
                    final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setClassName(accountType.resPackageName,
                            accountType.getViewGroupActivity());
                    startActivity(intent);
                }
            });
        } else if (mGroupSourceView != null) {
            mGroupSourceView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
            mPhotoManager.pause();
        } else {
            mPhotoManager.resume();
            if (null != mAdapter) {
            	mAdapter.notifyDataSetChanged();	
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, final MenuInflater inflater) {
    	/*if (ContactsApplication.sIsGnContactsSupport) {
    		inflater.inflate(R.menu.gn_view_group, menu);
    	} else {
    		inflater.inflate(R.menu.view_group, menu);
    	}*/
    }

    public boolean isOptionsMenuChanged() {
        return mOptionsMenuGroupDeletable != isGroupDeletable() &&
                mOptionsMenuGroupPresent != isGroupPresent();
    }

    public boolean isGroupDeletable() {
        return mGroupUri != null && !mIsReadOnly;
    }

    public boolean isGroupPresent() {
        return mGroupUri != null;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
    	/*
		 * Bug Fix by Mediatek Begin
		 * Original Android's code:
		 * mOptionsMenuGroupDeletable = isGroupDeletable() && isVisible() 
		 * mOptionsMenuGroupPresent = isGroupPresent() && isVisible()
		 * CR ID :ALPS000252546
		 * Descriptions: when loading data ,move the menu
		 */
         mOptionsMenuGroupDeletable = isGroupDeletable() && isVisible() && isFinished;
         mOptionsMenuGroupPresent = isGroupPresent() && isVisible() && isFinished;
    	/*
		 * Bug Fix by Mediatek End
		 */
         // gionee xuhz 20120814 add for CR00673920 start
         final MenuItem addMemberMenu = menu.findItem(R.id.menu_add_member);
         addMemberMenu.setVisible(mOptionsMenuGroupPresent);
         // gionee xuhz 20120814 add for CR00673920 end
         
        final MenuItem editMenu = menu.findItem(R.id.menu_edit_group);
        editMenu.setVisible(mOptionsMenuGroupPresent);

        final MenuItem deleteMenu = menu.findItem(R.id.menu_delete_group);
        deleteMenu.setVisible(mOptionsMenuGroupDeletable);
        
        // The following lines are provided and maintained by Mediatek Inc.
        if(DEBUG)Log.i(TAG, groupMemberSize+"------groupMemberSize onPrepareOptionsMenu [fragment]");
        final MenuItem moveMenu = menu.findItem(R.id.menu_move_group);
        final MenuItem sendMsgMenu = menu.findItem(R.id.menu_message_group);
        final MenuItem sendEmailMenu = menu.findItem(R.id.menu_email_group);
        final MenuItem batchSetRingtoneMenu = menu.findItem(R.id.gn_menu_ringtone_set_batch);
        if (groupMemberSize <= 0) {    
            moveMenu.setVisible(false);
            sendMsgMenu.setVisible(false);     
            sendEmailMenu.setVisible(false);
            if (null != batchSetRingtoneMenu) {
            	batchSetRingtoneMenu.setVisible(false);
            }
        }else{
            if (DISABLE_MOVE_MENU == true) {
                moveMenu.setVisible(false);
            }
            else
            {
                moveMenu.setVisible(true); 
            }
            sendMsgMenu.setVisible(true);     
            sendEmailMenu.setVisible(true);
            
            if (null != batchSetRingtoneMenu) {
            	// gionee xuhz 20120911 modify for CR00687409 start
            	if (mSlotId >= 0) {
                	batchSetRingtoneMenu.setVisible(false);
            	} else {
                	batchSetRingtoneMenu.setVisible(true);
            	}
            	// gionee xuhz 20120911 modify for CR00687409 end
            }
        }

        
        // The previous  lines are provided and maintained by Mediatek Inc.
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_edit_group: {
                // The following lines are provided and maintained by Mediatek Inc.
                mGroupUri = mGroupUri.buildUpon().appendPath(String.valueOf(mSlotId)).build();
                // The previous  lines are provided and maintained by Mediatek Inc.
                if (mListener != null) mListener.onEditRequested(mGroupUri);
                break;
            }
            case R.id.menu_delete_group: {
                GroupDeletionDialogFragment.show(getFragmentManager(), mGroupId, mGroupName,
                        mCloseActivityAfterDelete, mSimId, mSlotId);
                return true;
            }
            case R.id.menu_move_group: {
                
                Intent moveIntent = new Intent().setClassName(this.getActivity(), "com.mediatek.contacts.list.ContactListMultiChoiceActivity");
                moveIntent.setAction(com.mediatek.contacts.util.ContactsIntent.LIST.ACTION_PICK_GROUP_MULTICONTACTS);
                moveIntent.putExtra("mGroupName", mGroupName);
                moveIntent.putExtra("mSlotId", mSlotId);
                moveIntent.putExtra("mGroupId", mGroupId);
                moveIntent.putExtra("mAccountName", mAccountName);
                if (!TextUtils.isEmpty(mAccountName)
                    && !TextUtils.isEmpty(mAccountTypeString)) {
                    Account tmpAccount = new Account(mAccountName, mAccountTypeString);
                    moveIntent.putExtra("account", tmpAccount);
                }
                
                this.startActivity(moveIntent);
                if (!PhoneCapabilityTester.isUsingTwoPanes(this.getActivity())) {
                    getActivity().finish();
                }
                break;
            }
            case R.id.menu_message_group: {
            	 new SendGroupSmsTask(this.getActivity()).execute(mGroupName);
                 break;
            }
            case R.id.menu_email_group:{
            	  new SendGroupEmailTask(this.getActivity()).execute(mGroupName);
                  break;
            }
            case R.id.menu_add_member:{
            	addGroupMembersRequest();
            	break;
            }
            case R.id.gn_menu_ringtone_set_batch: {
            	Intent pickIntent = IntentFactory.newPickRingtoneIntent(mContext, mCustomRingtone);
            	startActivityForResult(pickIntent, REQUEST_PICK_RINGTONE);
            	
            	break;
            }
        }
        return false;
    }

    public void closeActivityAfterDelete(boolean closeActivity) {
        mCloseActivityAfterDelete = closeActivity;
    }

    public long getGroupId() {
        return mGroupId;
    }
    
    // The following lines are provided and maintained by Mediatek Inc.
    private static final boolean DEBUG = true;
    private String mCategoryId = null;
   	private int mSlotId   = -1;
   	private int mSimId = -1;
   	private String mSimName ;
    private String mAccountName;
    private int groupMemberSize = -1;
    private boolean DISABLE_MOVE_MENU = false;
    public void loadExtras(String CategoryId, int slotId, int simIndicator, String simName) {
    	mCategoryId = CategoryId;
    	mSlotId = slotId;
    	mSimId = simIndicator;
    	mSimName = simName;
    	registerAirPlanModeReceiver();
    }
    public void loadExtras(int slotId) {
        mSlotId = slotId;
        registerAirPlanModeReceiver();
    }
    private void registerAirPlanModeReceiver(){
        if (mSlotId >= 0) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            this.getActivity().registerReceiver(airPlaneModeReceiver,filter);
            Log.i(TAG, "registerReceiver----load()");
        }
    }

	private class SendGroupSmsTask extends
			WeakAsyncTask<String, Void, String, Activity> {
		private WeakReference<AuroraProgressDialog> mProgress;

		public SendGroupSmsTask(Activity target) {
			super(target);
		}

		@Override
		protected void onPreExecute(Activity target) {
            mProgress = new WeakReference<AuroraProgressDialog>(AuroraProgressDialog.show(
                    target, null, 
                    target.getText(R.string.please_wait), true));
		}

		@Override
		protected String doInBackground(final Activity target,
				String... group) {
			return getSmsAddressFromGroup(
					target.getBaseContext(), getGroupId());
		}

		@Override
		protected void onPostExecute(final Activity target,
				String address) {
			AuroraProgressDialog progress = mProgress.get();
			if (progress != null && progress.isShowing()) {
				progress.dismiss();
			}
			if (address == null || address.length() == 0) {
				Toast.makeText(target, R.string.no_valid_number_in_group,
						Toast.LENGTH_SHORT).show();
			} else {
				String[] list = address.split(";");
				if (list.length > 1) {
					Toast.makeText(target, list[1], Toast.LENGTH_SHORT).show();
				}
				address = list[0];
				if (address == null || address.length() == 0) {
					return;
				}
				Intent intent = new Intent(Intent.ACTION_SENDTO);
				intent.setData(Uri.fromParts(Constants.SCHEME_SMSTO, address,
						null));
				startActivity(intent);
			}
		}
		
		public String getSmsAddressFromGroup(Context context, long groupId) {
		    Log.d(TAG, "groupId:" + groupId);
	        StringBuilder builder = new StringBuilder();
	        ContentResolver resolver = context.getContentResolver();
	        Cursor contactCursor = resolver.query(Data.CONTENT_URI,
	                new String[]{Data.CONTACT_ID}, 
	                Data.MIMETYPE + "=? AND " + GroupMembership.GROUP_ROW_ID + "=?", 
	                new String[]{GroupMembership.CONTENT_ITEM_TYPE, String.valueOf(groupId)}, null);
	        Log.d(TAG, "contactCusor count:" + contactCursor.getCount());
	        StringBuilder ids = new StringBuilder();
	        HashSet<Long> allContacts = new HashSet<Long>();
	        if (contactCursor != null) {
	            while (contactCursor.moveToNext()) {
	                Long contactId = contactCursor.getLong(0);
	                if (!allContacts.contains(contactId)) {
	                    ids.append(contactId).append(",");
	                    allContacts.add(contactId);
	                }
	            }
	            contactCursor.close();
	        }
	        StringBuilder where = new StringBuilder();
	        if (ids.length() > 0) {
	            ids.deleteCharAt(ids.length() - 1);
	            where.append(Data.CONTACT_ID + " IN (");
	            where.append(ids.toString());
	            where.append(")");
	        } else {
	            return "";
	        }
	        where.append(" AND ");
	        where.append(Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'");
	        Log.i(TAG, "getSmsAddressFromGroup where " + where);
	        
            Cursor cursor = resolver.query(Data.CONTENT_URI, 
                    new String[] {Data.DATA1, Phone.TYPE, Data.CONTACT_ID },
                    where.toString(), null, Data.CONTACT_ID + " ASC ");
	        if (cursor != null) {
	            long candidateContactId = -1;
	            int candidateType = -1;
	            String candidateAddress = "";
	            while (cursor.moveToNext()) {
	                Long id = cursor.getLong(2);
	                if (allContacts.contains(id))
	                    allContacts.remove(id);
	                int type = cursor.getInt(1);
	                String number = cursor.getString(0);
	                int numIndex = number.indexOf(",");
	                int tempIndex = -1;
	                if ((tempIndex  = number.indexOf(";")) >= 0) {
	                    if (numIndex < 0) {
	                        numIndex = tempIndex;
	                    } else {
	                        numIndex = numIndex < tempIndex ? numIndex:tempIndex;
	                    }
	                }
	                if (numIndex == 0) {
	                    continue;
	                } else if (numIndex > 0) {
	                    number = number.substring(0, numIndex);
	                }
	                
	                if (candidateContactId == -1) {
	                    candidateContactId = id;
	                    candidateType = type;
	                    candidateAddress = number;
	                } else {
	                    if (candidateContactId != id) {
	                        if (candidateAddress != null && candidateAddress.length() > 0) {
	                            if (builder.length() > 0) {
	                                builder.append(",");
	                            }
	                            builder.append(candidateAddress);
	                        }
	                        candidateContactId = id;
	                        candidateType = type;
	                        candidateAddress = number;
	                    } else {
	                        if (candidateType != Phone.TYPE_MOBILE && type == Phone.TYPE_MOBILE) {
	                            candidateContactId = id;
	                            candidateType = type;
	                            candidateAddress = number;
	                        }
	                    }
	                }
	                if (cursor.isLast()) {
	                    if (candidateAddress != null && candidateAddress.length() > 0) {
	                        if (builder.length() > 0) {
	                            builder.append(",");
	                        }
	                        builder.append(candidateAddress);
	                    }
	                }
	                
	            }
	            cursor.close();
	        }
	        Log.i(TAG, "[getSmsAddressFromGroup]address:" + builder);
	        
	        ids = new StringBuilder();
	        where = new StringBuilder();
	        List<String> noNumberContactList = new ArrayList<String>();
	        if (allContacts.size() > 0) {
    	        Long [] allContactsArray = allContacts.toArray(new Long[0]);
    	        for(Long id:allContactsArray){
    	            if (ids.length() > 0) {
    	                ids.append(",");
    	            }
    	            ids.append(id.toString());
    	        }
	        }
	        if (ids.length() > 0) {
	            where.append(Data.CONTACT_ID + " IN(");
	            where.append(ids.toString());
	            where.append(")");
	        } else {
	            return builder.toString();
	        }
	        where.append(" AND ");
	        where.append(Data.MIMETYPE + "='" + StructuredName.CONTENT_ITEM_TYPE + "'");
	        Log.i(TAG, "[getSmsAddressFromGroup]query no name cursor selection:" + where.toString());
	        
            Cursor cursor2 = resolver.query(Data.CONTENT_URI,
                    new String[] { Data.DATA1 }, where.toString(), null,
                    Data.CONTACT_ID + " ASC ");
            
	        if(cursor2 != null){
	            while(cursor2.moveToNext()){
	                noNumberContactList.add(cursor2.getString(0));
	            }
	            cursor2.close();
	        }
	        String str = "";
	        if(noNumberContactList.size() == 1){
	            str = context.getString(R.string.send_groupsms_no_number_1, noNumberContactList.get(0));
	        }else if(noNumberContactList.size() == 2){
	            str = context.getString(R.string.send_groupsms_no_number_2, noNumberContactList.get(0),noNumberContactList.get(1));
	        }else if(noNumberContactList.size() > 2){
	            str = context.getString(R.string.send_groupsms_no_number_more, noNumberContactList.get(0),String.valueOf(noNumberContactList.size() - 1));
	        }
	        String result = builder.toString();
	        Log.i(TAG, "[getSmsAddressFromGroup]result:" + result);
	        if(str != null && str.length()>0){
	            return result + ";" + str;
	        }else 
	            return result;
	    }
	}
    
	private class SendGroupEmailTask extends
			WeakAsyncTask<String, Void, String, Activity> {
		private WeakReference<AuroraProgressDialog> mProgress;

		public SendGroupEmailTask(Activity target) {
			super(target);
		}

		@Override
		protected void onPreExecute(Activity target) {
			mProgress = new WeakReference<AuroraProgressDialog>(AuroraProgressDialog.show(
					target, null, target.getText(R.string.please_wait)));
		}

		@Override
		protected String doInBackground(final Activity target,
				String... group) {
			return getEmailAddressFromGroup(target, getGroupId());
		}

		@Override
		protected void onPostExecute(final Activity target,
				String address) {
			AuroraProgressDialog progress = mProgress.get();
			if (progress != null && progress.isShowing()) {
				progress.dismiss();
			}
			try {
				// Intent intent = new Intent(Intent.ACTION_SENDTO,
				// Uri.fromParts(Constants.SCHEME_MAILTO, address, null));
				// String[] addrList = address.split(",");
				//        
				// Intent intent = new Intent(Intent.ACTION_SEND);
				// intent.setType("*/*");
				// intent.putExtra(Intent.EXTRA_EMAIL, addrList);
				Uri dataUri = null;

				if (address != null && address.length() > 0) {
					// address = address.replace(",", ";");
					dataUri = Uri.parse("mailto:" + address);
				}
				if (address == null || address.length() == 0) {
					Toast.makeText(target,
							R.string.no_valid_email_in_group,
							Toast.LENGTH_SHORT).show();
				} else {
					Intent intent = new Intent(Intent.ACTION_SENDTO, dataUri);
					startActivity(intent);
				}
			} catch (ActivityNotFoundException e) {
				Log.e(TAG, "No activity found for Eamil");
				Toast.makeText(target, R.string.email_error,
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Log.e(TAG, "SendGroupEmail error", e);
			}
		}
		
		
		public String getEmailAddressFromGroup(Context context, long groupId) {
		    StringBuilder builder = new StringBuilder();
            ContentResolver resolver = context.getContentResolver();
            Cursor contactCursor = resolver.query(Data.CONTENT_URI,
                    new String[]{Data.CONTACT_ID}, 
                    Data.MIMETYPE + "=? AND " + GroupMembership.GROUP_ROW_ID + "=?", 
                    new String[]{GroupMembership.CONTENT_ITEM_TYPE, String.valueOf(groupId)}, null);
            StringBuilder ids = new StringBuilder();
            HashSet<Long> allContacts = new HashSet<Long>();
            if (contactCursor != null) {
                while (contactCursor.moveToNext()) {
                    Long contactId = contactCursor.getLong(0);
                    if (!allContacts.contains(contactId)) {
                        ids.append(contactId).append(",");
                        allContacts.add(contactId);
                    }
                }
                contactCursor.close();
            }
            StringBuilder where = new StringBuilder();
            if (ids.length() > 0) {
                ids.deleteCharAt(ids.length() - 1);
                where.append(Data.CONTACT_ID + " IN (");
                where.append(ids.toString());
                where.append(")");
            } else {
                return "";
            }
            where.append(" AND ");
            where.append(Data.MIMETYPE + "='" + Email.CONTENT_ITEM_TYPE + "'");
            Log.i(TAG, "[getEmailAddressFromGroup]where " + where);
            Cursor cursor = resolver.query(Data.CONTENT_URI, 
                    new String[] {Data.DATA1, Phone.TYPE, Data.CONTACT_ID },
                    where.toString(), null, Data.CONTACT_ID + " ASC ");
            if (cursor != null) {
                long candidateContactId = -1;
                String candidateAddress = "";
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(2);
                    int type = cursor.getInt(1);
                    String email = cursor.getString(0);
                    if (candidateContactId == -1) {
                        candidateContactId = id;
                        candidateAddress = email;
                    } else {
                        if (candidateContactId != id) {
                            if (candidateAddress != null && candidateAddress.length() > 0) {
                                if (builder.length() > 0) {
                                    builder.append(",");
                                }
                                builder.append(candidateAddress);
                            }
                            candidateContactId = id;
                            candidateAddress = email;
                        }
                    }
                    if (cursor.isLast()) {
                        if (candidateAddress != null && candidateAddress.length() > 0) {
                            if (builder.length() > 0) {
                                builder.append(",");
                            }
                            builder.append(candidateAddress);
                        }
                    }
                }
                cursor.close();
            }
            Log.i(TAG, "[getEmailAddressFromGroup]builder String:" + builder.toString());
            return builder.toString();
        }
	}
	
	  private final BroadcastReceiver airPlaneModeReceiver = new BroadcastReceiver(){

	        @Override
	        public void onReceive(Context context, Intent intent) {
	            Log.i(TAG, "In onReceive ");
	            final String action = intent.getAction();
	            Log.i(TAG, "action is " + action);
	            if(Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)){
	                processAirplaneModeChanged(intent);
	            }
	            
	        }
	        
	    };
	    void processAirplaneModeChanged(Intent intent) {
	        Log.i(TAG, "processAirplaneModeChanged");
	        boolean isAirplaneModeOn = intent.getBooleanExtra("state", false);
	        Log.i(TAG, "[processAirplaneModeChanged]isAirplaneModeOn:" + isAirplaneModeOn);
	        if (isAirplaneModeOn) {
	            this.getActivity().finish();
	        } else {
	           
	        }
	    }

	    @Override
	    public void onDestroy() {
	        if (mSlotId >= 0){
	            this.getActivity().unregisterReceiver(airPlaneModeReceiver);
	        }
	        super.onDestroy();
	    }
	// The previous  lines are provided and maintained by Mediatek Inc.
        /*
         * Bug Fix by Mediatek Begin. Original Android's code: CR ID: ALPS00115673
         * Descriptions: add wait cursor
         */
        private long OCL;
	    private long OLF;
	    private long OCL1;
        private long OLF1;
        private TextView mLoadingContact;

        private ProgressBar mProgress;

        private View mLoadingContainer;

        private static boolean isFinished = false;

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
        private final static String KEY_CUSTOM_RINGTONE_CACHE = "customRingtone";
        // gionee xuhz 20120917 add for CR00693008 start
        private final static String KEY_GROUP_ID = "groupId";
        // gionee xuhz 20120917 add for CR00693008 end
        private String mCustomRingtone;
        
        protected void addGroupMembersRequest() {
        	Intent intent = null;
        	
        	intent = new Intent(ContactsIntent.LIST.ACTION_PICK_MULTICONTACTS);
            intent.setType(Phone.CONTENT_TYPE);
            intent.putExtra(MultiContactsPickerBaseFragment.EXTRA_ACCOUNT_FILTER_CLICKABLE, false);
            ContactListFilter filter = ContactListFilter.createAccountFilter(mAccountTypeString, mAccountName, mDataSet, null);            
            intent.putExtra(MultiContactsPickerBaseFragment.EXTRA_ACCOUNT_FILTER, filter);
            intent.putExtra(MultiContactsPickerBaseFragment.EXTRA_ACCOUNT_FILTER_EXINFO, "withoutGroupId/" + mGroupId);
            //Gionee:huangzy 20120604 add for CR00616160 start
            intent.putExtra(ContactsIntentResolverEx.EXTRA_KEY_REQUEST_TYPE, ContactsIntentResolverEx.GN_REQ_TYPE_ADD2GROUP_PICKER);
            //Gionee:huangzy 20120604 add for CR00616160 end
        	startActivityForResult(intent, ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS);
		}
        
        protected void addGroupMembers(long[] ids, int[] simIndexIds) {
            //Gionee:huangzy 20120621 add for CR00624998 start
            if (null == ids) {
                return;
            }
            int len = ids.length;
            if (len > MAX_BATCH_HANDLE_NUM) {
                ids = subIdsIfNeed(ids);
                showSubMsg(String.format(getString(R.string.gn_max_add_num), MAX_BATCH_HANDLE_NUM));
            }
            //Gionee:huangzy 20120621 add for CR00624998 end
        	Intent saveIntent = ContactSaveService.createGroupUpdateIntent(mContext, mGroupId, null, 
        			ids, null, getClass(), null, null, mSlotId, simIndexIds, null);
        	getActivity().startService(saveIntent);
    	}
        
        //Gionee:huangzy 20120621 add for CR00624998 start
        private final int MAX_BATCH_HANDLE_NUM = ContactsApplication.MAX_BATCH_HANDLE_NUM;
        protected long[] subIdsIfNeed(long[] ids) {
            if (null == ids || ids.length < MAX_BATCH_HANDLE_NUM) {
                return ids;
            }
            
            return Arrays.copyOfRange(ids, 0, MAX_BATCH_HANDLE_NUM);
        }
        
        protected void showSubMsg(String msg) {
            Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
        }
        //Gionee:huangzy 20120621 add for CR00624998 end
        
        // Gionee zhangxx 2012-05-19 add for CR00601314 begin
        protected void addUSIMGroupMembers(long[] ids, final int[] simIndexIds) {
            // Gionee:wangth 20121213 modify for CR00742817 begin
            int[] nSimIndexIds = simIndexIds;
            //Gionee:huangzy 20120621 add for CR00624998 start
            if (null == ids) {
                return;
            }
            int len = ids.length;
            if (len > MAX_BATCH_HANDLE_NUM) {
                ids = subIdsIfNeed(ids);
                nSimIndexIds = Arrays.copyOfRange(nSimIndexIds, 0, MAX_BATCH_HANDLE_NUM);
                showSubMsg(String.format(getString(R.string.gn_max_add_num), MAX_BATCH_HANDLE_NUM));
            }
            //Gionee:huangzy 20120621 add for CR00624998 end
            
            Intent saveIntent = ContactSaveService.createGroupUpdateIntent(mContext, mGroupId, null, 
                    ids, null, getClass(), null, null, -1, nSimIndexIds, null);
            getActivity().startService(saveIntent);
            
            final int[] nSimNeedAddIndexIds = nSimIndexIds;
            
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    int ugrpId = -1;
                    try {
                        ugrpId = ContactsGroupUtils.USIMGroup.syncUSIMGroupUpdate(mSlotId, mGroupName, null);
                        Log.i(TAG, ugrpId + "---------ugrpId[updateGroup]");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    } catch (USIMGroupException e) {
                        Log.d(TAG, "[SyncUSIMGroup] catched USIMGroupException." 
                                + " ErrorType: " + e.getErrorType());
                        return;
                    }

                    for (int i = 0; i < nSimNeedAddIndexIds.length; i++) {
                        ContactsGroupUtils.USIMGroup.addUSIMGroupMember(mSlotId, nSimNeedAddIndexIds[i], ugrpId);
                    }
                }
            }).start();
            // Gionee:wangth 20121213 modify for CR00742817 end
        }
        // Gionee zhangxx 2012-05-19 add for CR00601314 end
        
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
        	super.onActivityResult(requestCode, resultCode, data);
        	        	        	
        	if (Activity.RESULT_OK == resultCode) {
        		switch (requestCode) {
	        		case ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS:
	        			Bundle extra = data.getExtras();
	        			
	        			long[] contactsIds = extra.getLongArray(MultiContactsPickerBaseFragment.EXTRA_ID_ARRAY);
	    				int[] simIndexIds = extra.getIntArray(MultiContactsPickerBaseFragment.EXTRA_SIM_INDEX_ARRAY);
	    				// Gionee zhangxx 2012-05-19 add for CR00601314 begin
	    				if (mSlotId >= 0) {
	    				    if (null != contactsIds) {	    				        
	    				        addUSIMGroupMembers(contactsIds, simIndexIds);
	    				    }
	    				} else {
	    				// Gionee zhangxx 2012-05-19 add for CR00601314 end
	    				    if (null != contactsIds) {
	    				        addGroupMembers(contactsIds, simIndexIds);
	    				    }
	    				// Gionee zhangxx 2012-05-19 add for CR00601314 begin
	    				}
	    				// Gionee zhangxx 2012-05-19 add for CR00601314 end
	        			break;
	        			
	        		case REQUEST_PICK_RINGTONE:
	                    Uri pickedUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    	mCustomRingtone = (null != pickedUri) ? pickedUri.toString() : null; 
	                    handleRingtonePicked(pickedUri);
	        			break;
        		}
        	}
        }
        
        private void gnUpdateSize(int size) {
            String groupSizeString;
            if (size == -1) {
                groupSizeString = null;
            } else {
                groupSizeString = String.format(getResources().getString(R.string.gn_people_count), size);
            }

            if (mGroupSize != null) {
                mGroupSize.setText(groupSizeString);
            } else {
                mListener.onGroupSizeUpdated(groupSizeString);
            }
        }
        
        private void handleRingtonePicked(Uri pickedUri) {
        	String customRingtone = pickedUri.toString();
        	//Gionee:huangzy 20120905 modify for CR00687176 start
        	String selection = "( name_raw_contact_id IN ( "
                + "SELECT raw_contact_id FROM view_data"
                + " WHERE ("
                + Data.MIMETYPE + " = '" + CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
                + "' AND data1=" + mGroupId
                + ")))";
        	//Gionee:huangzy 20120905 modify for CR00687176 end
            Intent intent = ContactSaveService.createBatchSetRingtone(
                    mContext, selection, customRingtone);
            
            mContext.startService(intent);
        }
        
        @Override
        public void onSaveInstanceState(Bundle outState) {
        	super.onSaveInstanceState(outState);        
        	outState.putString(KEY_CUSTOM_RINGTONE_CACHE, mCustomRingtone);
        	// gionee xuhz 20120917 add for CR00693008 start
        	outState.putLong(KEY_GROUP_ID, mGroupId);
        	// gionee xuhz 20120917 add for CR00693008 end
        }
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
        	super.onCreate(savedInstanceState);
        	
        	if (null != savedInstanceState) {
        		mCustomRingtone = savedInstanceState.getString(KEY_CUSTOM_RINGTONE_CACHE);
        		// gionee xuhz 20120917 add for CR00693008 start
        		mGroupId = savedInstanceState.getLong(KEY_GROUP_ID);
        		// gionee xuhz 20120917 add for CR00693008 end
        	}
        }
}
