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

package com.android.contacts.activities;

import com.android.contacts.ContactsActivity;
import com.android.contacts.ContactsApplication;
import com.android.contacts.R;
import com.android.contacts.group.GroupDetailDisplayUtils;
import com.android.contacts.group.GroupDetailFragment;
import com.android.contacts.activities.PeopleActivity.AccountCategoryInfo;
import com.android.contacts.list.ContactsRequest;
import com.android.contacts.model.AccountType;
import com.android.contacts.model.AccountTypeManager;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import gionee.provider.GnContactsContract.Groups;
import gionee.provider.GnContactsContract.Intents;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
// Gionee:wangth 20120601 add for CR00561993 begin
import com.android.contacts.ContactsUtils;
// Gionee:wangth 20120601 add for CR00561993 end

public class GroupDetailActivity extends ContactsActivity {

    private static final String TAG = "GroupDetailActivity";

    private boolean mShowGroupSourceInActionBar;

    private String mAccountTypeString;
    private String mDataSet;

    private GroupDetailFragment mFragment;

    @Override
    public void onCreate(Bundle savedState) {
        // gionee xuhz add for support gn theme start
        if (ContactsApplication.sIsGnTransparentTheme) {
            setTheme(R.style.GN_DetailActivityTheme);
        } else if (ContactsApplication.sIsGnDarkTheme) {
            setTheme(R.style.GN_DetailActivityTheme_dark);
        } else if (ContactsApplication.sIsGnLightTheme) {
            setTheme(R.style.GN_DetailActivityTheme_light);
        } 
        // gionee xuhz add for support gn theme start
    	
        super.onCreate(savedState);

        // TODO: Create Intent Resolver to handle the different ways users can get to this list.
        // TODO: Handle search or key down

        setContentView(R.layout.group_detail_activity);

        mShowGroupSourceInActionBar = getResources().getBoolean(
                R.bool.config_show_group_action_in_action_bar);

        mFragment = (GroupDetailFragment) getFragmentManager().findFragmentById(
                R.id.group_detail_fragment);
        mFragment.setListener(mFragmentListener);
        mFragment.setShowGroupSourceInActionBar(mShowGroupSourceInActionBar);
        
        /*
		 * New feature by Mediatek Begin
		 * Original Android code:
		 * 
		 */
        mIntentExtras = this.getIntent().getExtras();
    	final AccountCategoryInfo accountCategoryInfo = mIntentExtras == null ? null :
    		(AccountCategoryInfo)mIntentExtras.getParcelable("AccountCategory");
    	if (accountCategoryInfo != null) {
    	   	mCategory = accountCategoryInfo.mAccountCategory;
        	mSlotId = accountCategoryInfo.mSlotId;
        	mSimId = accountCategoryInfo.mSimId;
        	mSimName = accountCategoryInfo.mSimName;
    	}
    	Log.i(TAG, mSlotId+"----mSlotId+++++[groupDetialActivity]");
    	Log.i(TAG, mSimId+"----mSimId+++++[groupDetialActivity]");
    	Log.i(TAG, mSimName+"----mSimName+++++[groupDetialActivity]");
        mFragment.loadExtras(mCategory, mSlotId, mSimId, mSimName);
    
        //Log.i(TAG, slotId+"----slotId");
        String callBackIntent = getIntent().getStringExtra("callBackIntent");
        Log.i(TAG, callBackIntent+"----callBackIntent");
        if (null != callBackIntent) {
            int slotId = getIntent().getIntExtra("mSlotId", -1);
            mFragment.loadExtras(slotId);  
            Log.i(TAG, slotId+"----slotId");
        }
        /*
         * New feature by Mediatek End
         */
        mFragment.loadGroup(getIntent().getData());
        mFragment.closeActivityAfterDelete(true);

        // We want the UP affordance but no app icon.
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE,
                    ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE
                    | ActionBar.DISPLAY_SHOW_HOME);
        }
    }
    
    // Gionee:wangth 20120601 add for CR00561993 begin
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        
        if (ContactsUtils.mIsGnContactsSupport) {
            GroupDetailActivity.this.closeOptionsMenu();
        }
    }
    // Gionee:wangth 20120601 add for CR00561993 end

    private final GroupDetailFragment.Listener mFragmentListener =
            new GroupDetailFragment.Listener() {

        @Override
        public void onGroupSizeUpdated(String size) {
            getActionBar().setSubtitle(size);
        }

        @Override
        public void onGroupTitleUpdated(String title) {
            getActionBar().setTitle(title);
        }

        @Override
        public void onAccountTypeUpdated(String accountTypeString, String dataSet) {
            mAccountTypeString = accountTypeString;
            mDataSet = dataSet;
            invalidateOptionsMenu();
        }

        @Override
        public void onEditRequested(Uri groupUri) {
            final Intent intent = new Intent(GroupDetailActivity.this, GroupEditorActivity.class);
            /*
             * Bug Fix by Mediatek Begin
             * Original Android's code:
             *   intent.setData(groupUri);
                intent.setAction(Intent.ACTION_EDIT);
             * CR ID :ALPS000116203
             * Descriptions:
             */
            mSlotId = Integer.parseInt(groupUri.getLastPathSegment().toString());
            String grpId = groupUri.getPathSegments().get(1).toString(); 
            Log.i(TAG, grpId+"--------grpId");
            Uri uri = Uri.parse("content://com.android.contacts/groups").buildUpon().appendPath(grpId).build();  
            Log.i(TAG, uri.toString()+"--------groupUri.getPath();");
            intent.setData(uri);
            intent.setAction(Intent.ACTION_EDIT);
            intent.putExtra("SLOT_ID", mSlotId);
            intent.putExtra("SIM_ID", mSimId);
            /*
             * Bug Fix by Mediatek End
             */
            startActivity(intent);
        }

        @Override
        public void onContactSelected(Uri contactUri) {
            Intent intent = new Intent(Intent.ACTION_VIEW, contactUri);
            intent.putExtra(ContactDetailActivity.INTENT_KEY_FINISH_ACTIVITY_ON_UP_SELECTED, true);
            startActivity(intent);
        }

    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
        if (mShowGroupSourceInActionBar) {
            inflater.inflate(R.menu.group_source, menu);
        } else {
        	if (ContactsApplication.sIsGnContactsSupport) {
        		inflater.inflate(R.menu.gn_view_group, menu);
        	} else {
        		inflater.inflate(R.menu.view_group, menu);
        	}
        }

        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!mShowGroupSourceInActionBar) {
            if (mFragment != null) {
            	mFragment.onPrepareOptionsMenu(menu);
            }
            return super.onPrepareOptionsMenu(menu);
        }
        MenuItem groupSourceMenuItem = menu.findItem(R.id.menu_group_source);
        if (groupSourceMenuItem == null) {
            if (mFragment != null) {
            	mFragment.onPrepareOptionsMenu(menu);
            }
            return super.onPrepareOptionsMenu(menu);
        }
        final AccountTypeManager manager = AccountTypeManager.getInstance(this);
        final AccountType accountType =
                manager.getAccountType(mAccountTypeString, mDataSet);
        if (TextUtils.isEmpty(mAccountTypeString)
                || TextUtils.isEmpty(accountType.getViewGroupActivity())) {
            groupSourceMenuItem.setVisible(false);
            if (mFragment != null) {
            	mFragment.onPrepareOptionsMenu(menu);
            }
            return super.onPrepareOptionsMenu(menu);
        }
        View groupSourceView = GroupDetailDisplayUtils.getNewGroupSourceView(this);
        GroupDetailDisplayUtils.bindGroupSourceView(this, groupSourceView,
                mAccountTypeString, mDataSet);
        groupSourceView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Uri uri = ContentUris.withAppendedId(Groups.CONTENT_URI,
                        mFragment.getGroupId());
                final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setClassName(accountType.resPackageName, accountType.getViewGroupActivity());
                startActivity(intent);
            }
        });
        groupSourceMenuItem.setActionView(groupSourceView);
        groupSourceMenuItem.setVisible(true);

        if (mFragment != null) {
        	mFragment.onPrepareOptionsMenu(menu);
        }
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, PeopleActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            default:
                break;
        }
        
        if (mFragment != null) {
        	mFragment.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }
    // The following lines are provided and maintained by Mediatek Inc.
    private Bundle mIntentExtras;
    private String mCategory = null;
   	private int mSlotId   = -1;
   	private int mSimId;
   	private String mSimName ;
	// The previous  lines are provided and maintained by Mediatek Inc.
}
