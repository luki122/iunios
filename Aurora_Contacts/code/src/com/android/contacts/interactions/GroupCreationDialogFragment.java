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
package com.android.contacts.interactions;

import com.android.contacts.ContactSaveService;
import com.android.contacts.R;
import com.android.contacts.editor.AuroraGroupMembershipView;
import com.android.contacts.model.AccountWithDataSet;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import gionee.provider.GnContactsContract.Groups;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import aurora.widget.AuroraEditText;

/**
 * A dialog for creating a new group.
 */
public class GroupCreationDialogFragment extends GroupNameDialogFragment {
    private static final String ARG_ACCOUNT_TYPE = "accountType";
    private static final String ARG_ACCOUNT_NAME = "accountName";
    private static final String ARG_DATA_SET = "dataSet";
    
    public static void show(
            FragmentManager fragmentManager, String accountType, String accountName,
            String dataSet) {
        GroupCreationDialogFragment dialog = new GroupCreationDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ACCOUNT_TYPE, accountType);
        args.putString(ARG_ACCOUNT_NAME, accountName);
        args.putString(ARG_DATA_SET, dataSet);
        dialog.setArguments(args);
        dialog.show(fragmentManager, "createGroup");       
    }
    
//aurora add zhouxiaobing 20131218 start  
    
    private  static AuroraGroupMembershipView pp;
    @Override
	public void onCancel(DialogInterface dialog) {
		// TODO Auto-generated method stub
		super.onCancel(dialog);
		pp.showDialogForBack(null);
	}

	private Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			pp.showDialogForBack((String)msg.obj);
			super.handleMessage(msg);
		}
    	
    };
    public static void setAuroraGroupMembershipView(AuroraGroupMembershipView a)
    {
    	pp=a;
    }
  //aurora add zhouxiaobing 20131218 end       
    @Override
    protected void initializeGroupLabelEditText(AuroraEditText editText) {
    }

    @Override
    protected int getTitleResourceId() {
        return R.string.aurora_group_create;
    }

    @Override
    protected void onCompleted(String groupLabel) {
        Bundle arguments = getArguments();
        String accountType = arguments.getString(ARG_ACCOUNT_TYPE);
        String accountName = arguments.getString(ARG_ACCOUNT_NAME);
        String dataSet = arguments.getString(ARG_DATA_SET);
        /*
         * Change feature by Mediatek Begin
         * Original Android code:
         *
         * CR ID :ALPS000118978
         * Descriptions: 
         */
        if(!checkName(groupLabel, accountType, accountName)){
            return; 
        }
        
        /*
         * Change feature by Mediatek End
         */
  //aurora add zhouxiaobing 20131218 start       
//        Activity activity = getActivity();
//        activity.startService(ContactSaveService.createNewGroupIntent(activity,
//                new AccountWithDataSet(accountName, accountType, dataSet), groupLabel,
//                null /* no new members to add */,
//                activity.getClass(), Intent.ACTION_EDIT));
        Activity activity = getActivity();
        activity.startService(ContactSaveService.auroraCreateNewGroupIntent2(activity,
                new AccountWithDataSet(accountName, accountType, dataSet), groupLabel,
                null /* no new members to add */,
                activity.getClass(), Intent.ACTION_EDIT));
//        handler.sendMessageDelayed(handler.obtainMessage(0, 0, 0, groupLabel), 2000);
        pp.setIsneedDialog(true, groupLabel);
 //aurora add zhouxiaobing 20131218 end        
    }
    
    // The following lines are provided and maintained by Mediatek Inc.
    private static String TAG = "GroupNameDialogFragment";
    private Context mContext; 
    public boolean checkName(CharSequence name, String accountType, String accountName) {
        mContext = this.getActivity();
        Log.i(TAG, "checkName begiin"+name);
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(mContext, R.string.name_needed, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(name.toString().contains("/") || name.toString().contains("%"))
        {
             Toast.makeText(mContext, R.string.save_group_fail, Toast.LENGTH_SHORT).show();
             return false;
        }
        boolean nameExists = false;
        //check group name in DB
        Log.i(TAG, accountName+"--accountName");
        Log.i(TAG, accountType+"--accountType");
        if (!nameExists) {
            Cursor cursor = mContext.getContentResolver().query(
                    Groups.CONTENT_SUMMARY_URI,
                    new String[] { Groups._ID },
                    Groups.TITLE + "=? AND " + Groups.ACCOUNT_NAME + " =? AND " +
                    Groups.ACCOUNT_TYPE + "=? AND " + Groups.DELETED + "=0",
                    new String[] { name.toString(), accountName, accountType}, null);     
            Log.i(TAG, cursor.getCount()+"--cursor.getCount()");
            if (cursor == null || cursor.getCount() == 0) {
                if (cursor != null) {
                    cursor.close();
                }
            } else {
                cursor.close();
                nameExists = true;
            }
        }
        //If group name exists, make a toast and return false.
        if (nameExists) {
            Toast.makeText(mContext,
                    R.string.group_name_exists, Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }
    
    // The previous  lines are provided and maintained by Mediatek Inc.
}
