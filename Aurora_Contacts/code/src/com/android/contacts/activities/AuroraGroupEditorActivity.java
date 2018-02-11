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

import com.android.contacts.ContactLoader;
import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactsActivity;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.GroupMetaDataLoader;
import com.android.contacts.R;
import com.android.contacts.SimpleAsynTask;
import com.android.contacts.editor.AuroraContactEditorFragment.SaveMode;
import com.android.contacts.group.GroupEditorFragment;
import com.android.contacts.group.GroupEditorFragment.Status;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.list.ContactsRequest;
import com.android.contacts.model.AccountType;
import com.android.contacts.model.AccountWithDataSet;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.util.PhoneCapabilityTester;
import com.mediatek.contacts.list.MultiContactsPickerBaseFragment;

import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import android.app.Activity;
import android.app.Dialog;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import gionee.provider.GnContactsContract.CommonDataKinds;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.Groups;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import aurora.widget.AuroraEditText;
import aurora.app.AuroraProgressDialog;

public class AuroraGroupEditorActivity extends ContactsActivity 
        implements TextWatcher{

    private static final String TAG = "AuroraGroupEditorActivity";

    public static final String ACTION_SAVE_COMPLETED = "saveCompleted";
    public static final String ACTION_ADD_MEMBER_COMPLETED = "addMemberCompleted";
    public static final String ACTION_REMOVE_MEMBER_COMPLETED = "removeMemberCompleted";
    
    public static final String EXTRA_GROUP_NAME = "group_name";
    public static final String EXTRA_RINGTONE = "group_ringtone";

    private int mSlotId;
    
    private Context mContext;
    
    private AuroraEditText mGroupTitleTV;
    private String mGroupTitleName = "";
    private RelativeLayout mRingEditLayout;
    private TextView mRingtoneTV;
    private ImageView mDeleteGroupNameTv;
    private String mGroupRingtone;
    private Uri mPickedGroupRingtoneUri;
    private final static String KEY_CUSTOM_RINGTONE_CACHE = "customRingtone";
    private final int REQUEST_PICK_RINGTONE = 0;
    
    private long mRawContactIds[];
    
    private int mEditGroupType = 0;
    private final int EDIT_GROUP_TYPE_INSERT = 1;
    private final int EDIT_GROUP_TYPE_EDIT = 2;
    
    private String mOriginalGroupName = "";
    private String mAccountName;
    private String mAccountType;
    private String mDataSet;
    private boolean mGroupNameIsReadOnly;
    
    private String mGroupUriStr;
    private Uri mGroupUri;
    
    private ContactsUtils.AuroraContactsProgressDialog mProgressDialog;
    
    private static boolean mIsDeleteContactFinish = false;
    private static final int START = 0;
    private static final int END = 1;
    private final Handler mHandler = new Handler() {
        
        @Override
        public void handleMessage(Message msg) {
            
            switch(msg.what) {
            case START: {
                if (null != AuroraGroupEditorActivity.this && !isFinishing()) {
                    if (null == mProgressDialog) {
                        mProgressDialog = new ContactsUtils.AuroraContactsProgressDialog(mContext, AuroraProgressDialog.THEME_AMIGO_FULLSCREEN);
                    }
                    mProgressDialog.setTitle(R.string.aurora_save_group_dialog_title);
                    mProgressDialog.setIndeterminate(false);
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    try {
                        mProgressDialog.show();
                    } catch (Exception e) {
                        
                    }
                }
                break;
            }
            
            case END: {
                if (null != AuroraGroupEditorActivity.this && !isFinishing() 
                        && null != mProgressDialog && mProgressDialog.isShowing()) {
                    try {
                        mProgressDialog.dismiss();
                    } catch (Exception e) {
                        
                    }
                }
                break;
            }
            }
            
            super.handleMessage(msg);
        }
    
    };
    
    @Override
    public void onCreate(Bundle savedState) {
    	this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	setTheme(R.style.GN_PeopleTheme_light);
        super.onCreate(savedState);

        mContext = AuroraGroupEditorActivity.this;

        Intent intent = getIntent();
        String action = intent.getAction();
        mSlotId = intent.getIntExtra("SLOT_ID", -1);
        int simId = intent.getIntExtra("SIM_ID", -1);
        Log.i(TAG, mSlotId + "-------mSlotId[oncreate]");

        if (ACTION_SAVE_COMPLETED.equals(action)) {
            finish();
            return;
        }
        
        setAuroraContentView(R.layout.aurora_group_editor_activity,
                AuroraActionBar.Type.Dashboard); 
        AuroraActionBar actionBar = getAuroraActionBar();
        actionBar.getCancelButton().setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });

        actionBar.getOkButton().setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        onDoneClicked();
                    }
                });
        
        mGroupTitleTV = (AuroraEditText)findViewById(R.id.aurora_edit_group_name);
        mGroupTitleTV.addTextChangedListener(this);
        mGroupTitleTV.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mGroupTitleTV.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    showInputMethod();
                }
            }
        });
        
        // ring setting
        mRingEditLayout = (RelativeLayout)findViewById(R.id.aurora_edit_ring);
        mRingEditLayout.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                
                Intent pickIntent = IntentFactory.newPickRingtoneIntent(mContext, mGroupRingtone);
                try {
                    startActivityForResult(pickIntent, REQUEST_PICK_RINGTONE);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        mRingtoneTV = (TextView)findViewById(R.id.aurora_group_ring_name);
        
        mDeleteGroupNameTv = (ImageView)findViewById(R.id.aurora_group_title_delete_name);
        mDeleteGroupNameTv.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mGroupTitleTV != null) {
                    mGroupTitleTV.getText().clear();
                }
            }
        });
        
        if (Intent.ACTION_INSERT.equals(action)) {
            mEditGroupType = EDIT_GROUP_TYPE_INSERT;
        } else if (Intent.ACTION_EDIT.equals(action)) {
            mEditGroupType = EDIT_GROUP_TYPE_EDIT;
            mGroupUri = intent.getData();
            mOriginalGroupName = intent.getStringExtra(EXTRA_GROUP_NAME);
            mGroupRingtone = intent.getStringExtra(EXTRA_RINGTONE);
            mRawContactIds = intent.getLongArrayExtra("update_rawcontact_id");
            mGroupTitleTV.setText(mOriginalGroupName);
            if (!mOriginalGroupName.isEmpty()) {
            	try {
            		mGroupTitleTV.setSelection(mOriginalGroupName.length());
            	} catch (Exception e) {
            		e.printStackTrace();
            	}
            }
            setGroupRingtoneTV(mGroupRingtone);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        
        mSlotId = intent.getIntExtra("mSlotId", -1);
        boolean dismissDialog = intent.getBooleanExtra("dismissDialog", false);
        Log.i(TAG, "mSlotId = " + mSlotId);
        Log.i(TAG, "dismissDialog = " + dismissDialog);
        String action = intent.getAction();
        Log.i(TAG, "action = " + action);
        if (ACTION_SAVE_COMPLETED.equals(action)) {
            onSaveCompleted(true,
                    intent.getIntExtra(GroupEditorFragment.SAVE_MODE_EXTRA_KEY, SaveMode.CLOSE),
                    intent.getData());
        }
        
        if(dismissDialog){
            mHandler.sendEmptyMessage(END);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (Activity.RESULT_OK == resultCode) {
            switch (requestCode) {
            case REQUEST_PICK_RINGTONE:
                mPickedGroupRingtoneUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                mGroupRingtone = (null != mPickedGroupRingtoneUri) ? mPickedGroupRingtoneUri.toString() : null;
                setGroupRingtoneTV(mGroupRingtone);
                
                break;
            }
        }
    }
    
    private void onDoneClicked() {
        if (null != mGroupTitleTV) {
            ContactsUtils.hide(mGroupTitleTV.getWindowToken());
        }

        mGroupTitleName = mGroupTitleTV.getText().toString();
        
        if (mGroupTitleName.isEmpty()) {
            Toast.makeText(mContext, R.string.name_needed, Toast.LENGTH_SHORT).show();
            return;
        }
        
        String str = mGroupTitleName.replaceAll(" ", "");
        if (str.isEmpty()) {
            Toast.makeText(mContext, R.string.name_needed, Toast.LENGTH_SHORT).show();
            return;
        }
        
        boolean isNoExist = ContactsUtils.checkName(mContext, mGroupTitleName);
        if (!isNoExist && mOriginalGroupName != null && 
                mOriginalGroupName.equals(mGroupTitleName)) {
            isNoExist = true;
        }
        
        if (!isNoExist) {
            Toast.makeText(mContext, mContext.getString(
                    R.string.aurora_rename_group_toast), Toast.LENGTH_SHORT).show();
        } else {
            mHandler.sendEmptyMessage(START);
            if (mEditGroupType == EDIT_GROUP_TYPE_INSERT) {
                Intent saveIntent = ContactSaveService
                        .auroraCreateNewGroupIntent(mContext,
                                new AccountWithDataSet(
                                        AccountType.ACCOUNT_NAME_LOCAL_PHONE,
                                        AccountType.ACCOUNT_TYPE_LOCAL_PHONE,
                                        null), mGroupTitleName, null, this
                                        .getClass(), ACTION_SAVE_COMPLETED,
                                null, -1);
                saveIntent.putExtra(ContactSaveService.EXTRA_GROUP_RINGTONE,
                        mGroupRingtone);
                mContext.startService(saveIntent);
            } else if (mEditGroupType == EDIT_GROUP_TYPE_EDIT) {
                Intent saveIntent = ContactSaveService.auroraUpdateGroupIntent(
                        mContext,
                        Long.parseLong(mGroupUri.getLastPathSegment()),
                        mGroupTitleName,
                        null, 
                        null,
                        mRawContactIds,
                        this.getClass(),
                        ACTION_SAVE_COMPLETED,
                        mOriginalGroupName,
                        -1,
                        null,
                        null);
                
                saveIntent.putExtra(ContactSaveService.EXTRA_GROUP_RINGTONE, mGroupRingtone);
                mContext.startService(saveIntent);
            }
        }
    }
    
    private void onSaveCompleted(boolean hadChanges, int saveMode, Uri groupUri) {
        boolean success = groupUri != null;
        Log.d(TAG, "onSaveCompleted(" + saveMode + ", " + groupUri + ")");
        if (hadChanges) {
            Toast.makeText(mContext, success ? R.string.groupSavedToast :
                    R.string.groupSavedErrorToast, Toast.LENGTH_SHORT).show();
        }
        switch (saveMode) {
            case SaveMode.CLOSE:
            case SaveMode.HOME:
                final Intent resultIntent;
                final int resultCode;
                if (success && groupUri != null) {
                    final String requestAuthority =
                            groupUri == null ? null : groupUri.getAuthority();

                    resultIntent = new Intent();
                    if ("contacts".equals(requestAuthority)) {
                        // Build legacy Uri when requested by caller
                        final long groupId = ContentUris.parseId(groupUri);
                        final Uri legacyContentUri = Uri.parse("content://contacts/groups");
                        final Uri legacyUri = ContentUris.withAppendedId(
                                legacyContentUri, groupId);
                        resultIntent.setData(legacyUri);
                    } else {
                        // Otherwise pass back the given Uri
                        resultIntent.setData(groupUri);
                    }
                    
                    resultCode = Activity.RESULT_OK;
                } else {
                    resultCode = Activity.RESULT_CANCELED;
                    resultIntent = null;
                }
                // It is already saved, so prevent that it is saved again
                Intent intent = new Intent(AuroraGroupEditorActivity.this, AuroraGroupDetailActivity.class);
                intent.setData(resultIntent.getData());
                if (groupUri != null) {
                    intent.putExtra(MultiContactsPickerBaseFragment.EXTRA_ACCOUNT_FILTER_EXINFO, 
                            "withGroupId/" + groupUri.getLastPathSegment());
                    intent.putExtra(MultiContactsPickerBaseFragment.EXTRA_ACCOUNT_FILTER, 
                            ContactListFilter.createAccountFilter(AccountType.ACCOUNT_TYPE_LOCAL_PHONE, 
                                    AccountType.ACCOUNT_NAME_LOCAL_PHONE, null, null));
                }
                
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("mSlotId", mSlotId);
                intent.putExtra("callBackIntent","callBackIntent");
                intent.putExtra(EXTRA_GROUP_NAME, mGroupTitleName);
                intent.putExtra(EXTRA_RINGTONE, mGroupRingtone);
                
                if (mEditGroupType == EDIT_GROUP_TYPE_INSERT) {
                    startActivity(intent);
                } else if (mEditGroupType == EDIT_GROUP_TYPE_EDIT) {
                    setResult(resultCode, intent);
                }
                    
                finish();
                break;
            case SaveMode.RELOAD:
                // TODO: Handle reloading the group list
            default:
                throw new IllegalStateException("Unsupported save mode " + saveMode);
        }
    }
    
    private void setGroupRingtoneTV (final String uriStr) {
        if (null != uriStr) {
            new SimpleAsynTask() {
                String title = null;
                @Override
                protected Integer doInBackground(Integer... params) {
                    title = ContactLoader.gnGetRingtoneTile(AuroraGroupEditorActivity.this, uriStr);
                    return null;
                }
                
                @Override
                protected void onPostExecute(Integer result) {
                    if (title != null && mRingtoneTV != null) {
                        mRingtoneTV.setText(title);
                    }
                }
            }.execute();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        // TODO Auto-generated method stub
        
        if (mGroupTitleTV != null) {
            String name = mGroupTitleTV.getText().toString();
            if (!name.isEmpty()) {
                if (mDeleteGroupNameTv != null) {
                    mDeleteGroupNameTv.setVisibility(View.VISIBLE);
                }
            } else {
                if (mDeleteGroupNameTv != null) {
                    mDeleteGroupNameTv.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // TODO Auto-generated method stub
        
    }
    
    private void showInputMethod() {
        final InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && mGroupTitleTV != null) {
            imm.showSoftInputFromInputMethod(mGroupTitleTV.getWindowToken(), 0);
        }
    }
    
    private void hideSoftKeyboard() {
        // Hide soft keyboard, if visible
        final InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && mGroupTitleTV != null) {
            imm.hideSoftInputFromWindow(mGroupTitleTV.getWindowToken(), 0);
        }
    }
}
