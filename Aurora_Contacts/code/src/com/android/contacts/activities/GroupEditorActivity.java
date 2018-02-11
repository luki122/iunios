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
import com.android.contacts.editor.AuroraContactEditorFragment.SaveMode;
import com.android.contacts.group.GroupEditorFragment;
import com.android.contacts.util.DialogManager;
import com.android.contacts.util.PhoneCapabilityTester;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

public class GroupEditorActivity extends ContactsActivity
        implements DialogManager.DialogShowingViewActivity {

    private static final String TAG = "GroupEditorActivity";

    public static final String ACTION_SAVE_COMPLETED = "saveCompleted";
    public static final String ACTION_ADD_MEMBER_COMPLETED = "addMemberCompleted";
    public static final String ACTION_REMOVE_MEMBER_COMPLETED = "removeMemberCompleted";

    private GroupEditorFragment mFragment;

    private DialogManager mDialogManager = new DialogManager(this);
    private int mSlotId;

    @Override
    public void onCreate(Bundle savedState) {
        // gionee xuhz add for support gn theme start
    	if (Intent.ACTION_INSERT.equals(getIntent().getAction())) {    	
    	} else if (ContactsApplication.sIsGnTransparentTheme) {
            setTheme(R.style.GN_GroupEditorActivityTheme);
        } else if (ContactsApplication.sIsGnDarkTheme) {
            setTheme(R.style.GN_GroupEditorActivityTheme_dark);
        } else if (ContactsApplication.sIsGnLightTheme) {
            setTheme(R.style.GN_GroupEditorActivityTheme_light);
        } 
        // gionee xuhz add for support gn theme start
    	
        super.onCreate(savedState);
       
        /*
         * New feature by Mediatek Begin
         * Original Android code:
         *  String action = getIntent().getAction();
         */
        Intent intent = getIntent();
        String action = intent.getAction();
        mSlotId = intent.getIntExtra("SLOT_ID", -1);
        int simId  = intent.getIntExtra("SIM_ID",-1);
        Log.i(TAG, mSlotId+"-------mSlotId[oncreate]");
        /*
         * New feature by Mediatek End
         */
 
        if (ACTION_SAVE_COMPLETED.equals(action)) {
            finish();
            return;
        }

        setContentView(R.layout.group_editor_activity);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // Inflate a custom action bar that contains the "done" button for saving changes
            // to the group
            View customActionBarView = LayoutInflater.from(this).inflate(
            		ContactsApplication.sIsGnGGKJ_V2_0Support ? R.layout.gn_group_editor_custom_action_bar :
            			R.layout.editor_custom_action_bar, null);
            customActionBarView.setLayoutParams(new ActionBar.LayoutParams(
            		ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
            View saveMenuItem = customActionBarView.findViewById(R.id.save_menu_item);
            saveMenuItem.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFragment.onDoneClicked();
                }
            });
            
            if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
            	View discardMenuItem = customActionBarView.findViewById(R.id.discard_menu_item);
            	discardMenuItem.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mFragment.revert();
                    }
                });
            }
            // Show the custom action bar but hide the home icon and title
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME |
                    ActionBar.DISPLAY_SHOW_TITLE);
            actionBar.setCustomView(customActionBarView);
        }

        mFragment = (GroupEditorFragment) getFragmentManager().findFragmentById(
                R.id.group_editor_fragment);
        mFragment.setListener(mFragmentListener);
        mFragment.setContentResolver(getContentResolver());

        // NOTE The fragment will restore its state by itself after orientation changes, so
        // we need to do this only for a new instance.
        if (savedState == null) {
            Uri uri = Intent.ACTION_EDIT.equals(action) ? getIntent().getData() : null;
            /*
             * New feature by Mediatek Begin
             * Original Android code:
             *  mFragment.load(action, uri, getIntent().getExtras());
             */
                mFragment.load(action, uri, getIntent().getExtras(), mSlotId, simId);
            /*
             * New feature by Mediatek End
             */

        }
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        if (DialogManager.isManagedId(id)) {
            return mDialogManager.onCreateDialog(id, args);
        } else {
            // Nobody knows about the Dialog
            Log.w(TAG, "Unknown dialog requested, id: " + id + ", args: " + args);
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        // If the change could not be saved, then revert to the default "back" button behavior.

        /*
         * New feature by Mediatek Begin
         * Original Android code:
         * if (!mFragment.save(SaveMode.CLOSE)) {
            super.onBackPressed();
           }
         * CR ID :ALPS00228918
         * Descriptions: 
         */
        mFragment.checkGroupName(SaveMode.CLOSE);
        /*
         * New feature by Mediatek End
         */
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (mFragment == null) {
            return;
        }
        // The following lines are provided and maintained by Mediatek Inc.
        mSlotId = intent.getIntExtra("mSlotId", -1);
        boolean dismissDialog = intent.getBooleanExtra("dismissDialog", false);
        Log.i(TAG, mSlotId+"----mSlotId"); 
        Log.i(TAG, dismissDialog+"----dismissDialog"); 
        // The previous  lines are provided and maintained by Mediatek Inc.
        String action = intent.getAction();
        Log.i(TAG, action+"----action"); 
        if (ACTION_SAVE_COMPLETED.equals(action)) {
            mFragment.onSaveCompleted(true,
                    intent.getIntExtra(GroupEditorFragment.SAVE_MODE_EXTRA_KEY, SaveMode.CLOSE),
                    intent.getData());
        }
        if(dismissDialog){
        	mFragment.dismissDialog();
        }
    }

    private final GroupEditorFragment.Listener mFragmentListener =
            new GroupEditorFragment.Listener() {
        @Override
        public void onGroupNotFound() {
            finish();
        }

        @Override
        public void onReverted() {
            finish();
        }

        @Override
        public void onAccountsNotFound() {
            finish();
        }

        @Override
        public void onSaveFinished(int resultCode, Intent resultIntent) {
            // TODO: Collapse these 2 cases into 1 that will just launch an intent with the VIEW
            // action to see the group URI (when group URIs are supported)
            // For a 2-pane screen, set the activity result, so the original activity (that launched
            // the editor) can display the group detail page
            if (PhoneCapabilityTester.isUsingTwoPanes(GroupEditorActivity.this)) {
                setResult(resultCode, resultIntent);
            } else if (resultIntent != null) {
                // For a 1-pane screen, launch the group detail page
                /*
                 * Bug Fix by Mediatek Begin
                 * Original Android's code:
                 * 
                Intent intent = new Intent(GroupEditorActivity.this, GroupDetailActivity.class);
                intent.setData(resultIntent.getData());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                 * CR ID :ALPS000113859
                 * Descriptions:add the arg slotId 
                 */
                Intent intent = new Intent(GroupEditorActivity.this, GroupDetailActivity.class);
                intent.setData(resultIntent.getData()); 
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("mSlotId", mSlotId);
                intent.putExtra("callBackIntent","callBackIntent");
                startActivity(intent);
                /*
                 * Bug Fix by Mediatek End
                 */
            }
            finish();
        }
    };

    @Override
    public DialogManager getDialogManager() {
        return mDialogManager;
    }
}
