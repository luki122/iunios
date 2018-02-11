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
 * limitations under the License
 */

package com.android.contacts.activities;

import aurora.widget.AuroraActionBar;
import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactsActivity;
import com.android.contacts.ContactsApplication;
import com.android.contacts.R;
import com.android.contacts.editor.AuroraContactEditorFragment;
import com.android.contacts.editor.GnRingtoneEditorView;
import com.android.contacts.editor.AuroraContactEditorFragment.SaveMode;
import com.android.contacts.model.AccountType;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.AccountWithDataSet;
import com.android.contacts.util.DialogManager;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;

import java.util.ArrayList;


//The following lines are provided and maintained by Mediatek Inc.
import com.mediatek.contacts.list.service.MultiChoiceService;

import android.widget.TextView;
import android.widget.Toast;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.ContactsFeatureConstants;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
//The previous lines are provided and maintained by Mediatek Inc.


public class ContactEditorActivity extends ContactsActivity
        implements DialogManager.DialogShowingViewActivity {
    private static final String TAG = "ContactEditorActivity";

    public static final String ACTION_JOIN_COMPLETED = "joinCompleted";
    public static final String ACTION_SAVE_COMPLETED = "saveCompleted";

    /**
     * Boolean intent key that specifies that this activity should finish itself
     * (instead of launching a new view intent) after the editor changes have been
     * saved.
     */
    public static final String INTENT_KEY_FINISH_ACTIVITY_ON_SAVE_COMPLETED =
            "finishActivityOnSaveCompleted";
    
    //Gionee:huangzy 20120705 add for CR00614787 start
    public static final String INTENT_KEY_AUTO_SAVE_ON_BACK_PRESS =
            "autoSaveOnBackPress";
    private boolean mAutoSaveOnBackPress;
    //Gionee:huangzy 20120705 add for CR00614787 end

    private AuroraContactEditorFragment mFragment;
    private boolean mFinishActivityOnSaveCompleted;

    private DialogManager mDialogManager = new DialogManager(this);
    
    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        /*
         * Bug Fix by Mediatek Begin.
         *   Original Android's code:
         *     xxx
         *   CR ID: ALPS00251666
         *   Descriptions: can not add contact when in delete processing
         */
        if (MultiChoiceService.isProcessing(MultiChoiceService.TYPE_DELETE)||MultiChoiceService.isProcessing(MultiChoiceService.TYPE_COPY)) {
            Log.i(TAG,"delete or copy is processing ");
            Toast.makeText(this, R.string.phone_book_busy,
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        /*
         * Bug Fix by Mediatek End.
         */
        
        final Intent intent = getIntent();
        final String action = intent.getAction();

        // Determine whether or not this activity should be finished after the user is done
        // editing the contact or if this activity should launch another activity to view the
        // contact's details.
        mFinishActivityOnSaveCompleted = intent.getBooleanExtra(
                INTENT_KEY_FINISH_ACTIVITY_ON_SAVE_COMPLETED, false);
        
        mAutoSaveOnBackPress = intent.getBooleanExtra(
                INTENT_KEY_AUTO_SAVE_ON_BACK_PRESS, true);

        // The only situation where action could be ACTION_JOIN_COMPLETED is if the
        // user joined the contact with another and closed the activity before
        // the save operation was completed.  The activity should remain closed then.
        if (ACTION_JOIN_COMPLETED.equals(action)) {
            finish();
            return;
        }

        if (ACTION_SAVE_COMPLETED.equals(action)) {
            finish();
            return;
        }
		// aurora <ukiliu> <2013-9-17> modify for auroro ui begin
        setAuroraContentView(R.layout.contact_editor_activity,
                AuroraActionBar.Type.Dashboard);
        
        AuroraActionBar actionBar = getAuroraActionBar();
        actionBar.getCancelButton().setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                    	mFragment.revert();
                    }
                });

        actionBar.getOkButton().setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                    	mFragment.doSaveAction();
                    }
                });
	  if(Intent.ACTION_INSERT.equals(action)&&getIntent().getExtras()==null)	
         actionBar.getOkButton().setEnabled(false);//aurora change zhouxiaobing 20140716  


        mFragment = (AuroraContactEditorFragment) getFragmentManager().findFragmentById(
                R.id.contact_editor_fragment);
        mFragment.setListener(mFragmentListener);
        Uri uri = Intent.ACTION_EDIT.equals(action) ? getIntent().getData() : null;
        mFragment.load(action, uri, getIntent().getExtras());
        
        // The following lines are provided and maintained by Mediatek Inc.
        if (FeatureOption.MTK_GEMINI_3G_SWITCH) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ContactsFeatureConstants.EVENT_PRE_3G_SWITCH);
            registerReceiver(mModemSwitchListener, intentFilter);
        }
        // The previous lines are provided and maintained by Mediatek Inc.
        
        
        
        if (ContactsApplication.sIsAuroraPrivacySupport ) {
        	boolean isPrivacyMode = getIntent().getBooleanExtra("is_privacy_contact", false);
        	if (isPrivacyMode) {
        		ContactsApplication.mPrivacyActivityList.add(this);
        	}
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (mFragment == null) {
            return;
        }

        String action = intent.getAction();
        if (Intent.ACTION_EDIT.equals(action)) {
            mFragment.setIntentExtras(intent.getExtras());
        } else if (ACTION_SAVE_COMPLETED.equals(action)) {
            mFragment.onSaveCompleted(true,
                    intent.getIntExtra(AuroraContactEditorFragment.SAVE_MODE_EXTRA_KEY, SaveMode.CLOSE),
                    intent.getBooleanExtra(ContactSaveService.EXTRA_SAVE_SUCCEEDED, false),
                    intent.getData());
        } else if (ACTION_JOIN_COMPLETED.equals(action)) {
            mFragment.onJoinCompleted(intent.getData());
        }
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        if (DialogManager.isManagedId(id)) return mDialogManager.onCreateDialog(id, args);

        // Nobody knows about the Dialog
        Log.w(TAG, "Unknown dialog requested, id: " + id + ", args: " + args);
        return null;
    }

    @Override
    public void onBackPressed() {
        //Gionee:huangzy 20120705 add for CR00614787 start
        if (!mAutoSaveOnBackPress) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        //Gionee:huangzy 20120705 add for CR00614787 end
        
        /*
         * Change Feature by Mediatek Inc Begin.
         * orignal code
         * mFragment.save(SaveMode.CLOSE); 
         * Description: change save mode when backpressed
         */
        // aurora ukiliu 2013-10-15 modify for aurara ui begin
        mFragment.revert();
//            mFragment.doSaveAction(); 
        // aurora ukiliu 2013-10-15 modify for aurara ui end
        /*
         * Change Feature by Mediatek Inc End.
         */
    }
    
    private final AuroraContactEditorFragment.Listener mFragmentListener =
            new AuroraContactEditorFragment.Listener() {
        @Override
        public void onReverted() {
            finish();
        }

        @Override
        public void onSaveFinished(Intent resultIntent) {
            if (mFinishActivityOnSaveCompleted) {
                setResult(resultIntent == null ? RESULT_CANCELED : RESULT_OK, resultIntent);
                ContactsApplication.getInstance().sendSyncBroad();
            } else if (resultIntent != null) {
                startActivity(resultIntent);
            }
            finish();
        }

        @Override
        public void onContactSplit(Uri newLookupUri) {
            finish();
        }

        @Override
        public void onContactNotFound() {
            finish();
        }

        @Override
        public void onEditOtherContactRequested(
                Uri contactLookupUri, ArrayList<ContentValues> values) {
            Intent intent = new Intent(Intent.ACTION_EDIT, contactLookupUri);
            intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                    | Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            intent.putExtra(AuroraContactEditorFragment.INTENT_EXTRA_ADD_TO_DEFAULT_DIRECTORY, "");

            // Pass on all the data that has been entered so far
            if (values != null && values.size() != 0) {
                intent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, values);
            }

            startActivity(intent);
            finish();
        }

        @Override
        public void onCustomCreateContactActivityRequested(AccountWithDataSet account,
                Bundle intentExtras) {
            final AccountTypeManager accountTypes =
                    AccountTypeManager.getInstance(ContactEditorActivity.this);
            final AccountType accountType = accountTypes.getAccountType(
                    account.type, account.dataSet);

            Intent intent = new Intent();
            intent.setClassName(accountType.resPackageName,
                    accountType.getCreateContactActivityClassName());
            intent.setAction(Intent.ACTION_INSERT);
            intent.setType(Contacts.CONTENT_ITEM_TYPE);
            if (intentExtras != null) {
                intent.putExtras(intentExtras);
            }
            intent.putExtra(RawContacts.ACCOUNT_NAME, account.name);
            intent.putExtra(RawContacts.ACCOUNT_TYPE, account.type);
            intent.putExtra(RawContacts.DATA_SET, account.dataSet);
            intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                    | Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            startActivity(intent);
            finish();
        }

        @Override
        public void onCustomEditContactActivityRequested(AccountWithDataSet account,
                Uri rawContactUri, Bundle intentExtras, boolean redirect) {
            final AccountTypeManager accountTypes =
                    AccountTypeManager.getInstance(ContactEditorActivity.this);
            final AccountType accountType = accountTypes.getAccountType(
                    account.type, account.dataSet);

            Intent intent = new Intent();
            intent.setClassName(accountType.resPackageName,
                    accountType.getEditContactActivityClassName());
            intent.setAction(Intent.ACTION_EDIT);
            intent.setData(rawContactUri);
            if (intentExtras != null) {
                intent.putExtras(intentExtras);
            }

            if (redirect) {
                intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                        | Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                startActivity(intent);
                finish();
            } else {
                startActivity(intent);
            }
        }
    };

    @Override
    public DialogManager getDialogManager() {
        return mDialogManager;
    }
    
    // The following lines are provided and maintained by Mediatek Inc.
    private BroadcastReceiver mModemSwitchListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ContactsFeatureConstants.EVENT_PRE_3G_SWITCH)) {
                Log.i(TAG, "Before modem switch .....");
                finish();
            }
        }
    };
    
    @Override
    protected void onDestroy() {
        if (FeatureOption.MTK_GEMINI_3G_SWITCH) {
            unregisterReceiver(mModemSwitchListener);
        }
        super.onDestroy();
        if (ContactsApplication.sIsAuroraPrivacySupport ) {
        	boolean isPrivacyMode = getIntent().getBooleanExtra("is_privacy_contact", false);
        	if (isPrivacyMode) {
        		ContactsApplication.mPrivacyActivityList.remove(this);
        	}
        }
    }
    // The previous lines are provided and maintained by Mediatek Inc.
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_contact, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	if (mFragment != null) {
    		mFragment.onPrepareOptionsMenu(menu);
    	}
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if (mFragment != null) {
    		mFragment.onOptionsItemSelected(item);
    	}
        return super.onOptionsItemSelected(item);
    }
}
