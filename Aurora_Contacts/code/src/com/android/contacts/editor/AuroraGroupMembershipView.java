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

import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactsApplication;
import com.android.contacts.GroupMetaDataLoader;
import com.android.contacts.R;
import com.android.contacts.ResConstant;
import com.android.contacts.interactions.GroupCreationDialogFragment;
import com.android.contacts.model.AccountType;
import com.android.contacts.model.AccountWithDataSet;
import com.android.contacts.model.DataKind;
import com.android.contacts.model.EntityDelta;
import com.android.contacts.model.EntityDelta.ValuesDelta;
import com.android.contacts.model.EntityModifier;
import com.mediatek.contacts.util.Objects;

import android.app.Activity;
import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import aurora.app.AuroraMultipleChoiceListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import gionee.provider.GnContactsContract.CommonDataKinds.GroupMembership;
import gionee.provider.GnContactsContract.Groups;
import gionee.provider.GnContactsContract.RawContacts;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.Toast;
import aurora.widget.AuroraListView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * An editor for group membership.  Displays the current group membership list and
 * brings up a dialog to change it.
 */
public class AuroraGroupMembershipView extends LinearLayout
        implements OnClickListener, OnItemClickListener {
    private int whichs;
    private static final int CREATE_NEW_GROUP_GROUP_ID = 133;
    private ArrayList<String> groupNames,groupNameInit;
    private HashMap<Integer, Boolean> positions;
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

    private class MyArrayAdapter<T> extends ArrayAdapter<T>  {

		public MyArrayAdapter(Context context, int resource) {
			super(context, resource);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			if (position == getCount() - 1) {
				if (convertView == null) {
					convertView  = mInflater.inflate(R.layout.aurora_group_membership_custom_item, parent, false);
				}
			}
			return super.getView(position, convertView, parent);
		}
    	
    }

    private EntityDelta mState;
    private Cursor mGroupMetaData;
    private String mAccountName;
    private String mAccountType;
    private String mDataSet;
    private TextView mGroupList;
    private MyArrayAdapter<GroupSelectionItem> mAdapter;
    private long mDefaultGroupId;
    private long mFavoritesGroupId;
    private DataKind mKind;
    private boolean mDefaultGroupVisibilityKnown;
    private boolean mDefaultGroupVisible;
    private LayoutInflater mInflater;
    
    private AuroraAlertDialog mDialog;

    public AuroraGroupMembershipView(Context context) {
        super(context);
    }

    public AuroraGroupMembershipView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Resources resources = mContext.getResources();
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        kindTitle.setVisibility(View.GONE);
    }

    public void setGroupMetaData(Cursor groupMetaData) {
    	System.out.println("setGroupMetaData");
        this.mGroupMetaData = groupMetaData;

        if(is_need_dialog)
        {
//        	showDialogForBack(group_label);


//            mAdapter = new MyArrayAdapter<GroupSelectionItem>(
//                    getContext(), R.layout.group_membership_list_item);
// 
        	mAdapter.clear();
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
    	                    Log.e("wangth", "checked = " + checked + "  title = " + title);
    	                    if (groupNames.get(groupNames.size() - 1).equals(title)) {
    	                    	checked = true;
    	                    }
    	                    mAdapter.add(new GroupSelectionItem(groupId, title, checked));
    	                }
    	            }
    	        }
            }

            if (!mAccountType.equals(mUsimAccountType)) {
                mAdapter.add(new GroupSelectionItem(CREATE_NEW_GROUP_GROUP_ID, getContext().getString(
                        R.string.aurora_group_create), false));
            }
            
            ListView listView = null;
        	listView = mDialog.getListView();
            listView.setChoiceMode(AuroraListView.CHOICE_MODE_MULTIPLE);
            listView.setOverScrollMode(OVER_SCROLL_ALWAYS);
            int count = mAdapter.getCount();
            for (int i = 0; i < count; i++) {
                listView.setItemChecked(i, mAdapter.getItem(i).isChecked());
                if (i == count - 1) {
                	listView.setItemChecked(i, true);
                	Log.e("wangth", "checked = ==");
                }
            }
            
//            listView.setOnItemClickListener(this);
            if(group_label!=null)
            {
                for (int i = 0; i < count; i++) {
                	if(group_label.equalsIgnoreCase(mAdapter.getItem(i).mTitle))
                	{
                		if (i == count - 1) {
                        	listView.setItemChecked(i, true);
                        	Log.e("wangth", "mTitle      = ");
                        }
                		
                		mAdapter.getItem(i).mChecked=true;
                		listView.setItemChecked(i, mAdapter.getItem(i).isChecked());
                		break;
                	}
                }
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
                
                if (i == count -1) {
                	listView.setItemChecked(i, true);
                	
                }
            }
            
        	is_need_dialog=false;
        }else{
        	  updateView();
        }
        Log.v("AuroraGroupMembershipView", "setGroupMetaData");
    }

    public void setState(EntityDelta state) {
        mState = state;
        ValuesDelta values = state.getValues();
        mAccountType = values.getAsString(RawContacts.ACCOUNT_TYPE);
        mAccountName = values.getAsString(RawContacts.ACCOUNT_NAME);
        mDataSet = values.getAsString(RawContacts.DATA_SET);
        mDefaultGroupVisibilityKnown = false;
        updateView();
        System.out.println("public void setState(EntityDelta state)");
    }

    private void updateView() {
        System.out.println("private void updateView()");
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

    private boolean selected[];
    
    @Override
    public void onClick(View v) {

        mAdapter = new MyArrayAdapter<GroupSelectionItem>(
                getContext(), R.layout.group_membership_list_item);
        
        
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

        if (mAccountType.equals(AccountType.ACCOUNT_TYPE_LOCAL_PHONE)) {
            mAdapter.add(new GroupSelectionItem(CREATE_NEW_GROUP_GROUP_ID, getContext().getString(
                    R.string.aurora_group_create), false));
        }
        
        ListView listView = null;
//aurora add zhouxiaobing 20131230 start
        groupNames=new ArrayList<String>();
        groupNameInit=new ArrayList<String>();
        positions=new HashMap<Integer, Boolean>();
//        String sel[]=new String [mAdapter.getCount()];
        selected = new boolean[mAdapter.getCount()];
        for(int i=0;i<mAdapter.getCount();i++)
        {
        	groupNameInit.add(mAdapter.getItem(i).mTitle);
//        	sel[i]=mAdapter.getItem(i).mTitle;
        	if(i!=mAdapter.getCount()-1){
        		groupNames.add(mAdapter.getItem(i).mTitle);
        	}
        	if(mAdapter.getItem(i).isChecked())
        	{
        		selected[i]=true;
        		if(i!=mAdapter.getCount()-1){
        			positions.put(i, true);
            	}	
        	}
        	else
        	{
        		if(i!=mAdapter.getCount()-1){
        			positions.put(i, false);
            	}	
        		selected[i]=false;
        	}
        	
        }
//aurora add zhouxiaobing 20131230 end   
    	mDialog = new AuroraAlertDialog.Builder(getContext())
    		.setTitle(R.string.aurora_select_group).setShowAddItemViewInMultiChoiceMode(true)
    		.setMultiChoiceItems(groupNameInit.toArray(new String [groupNameInit.size()]), selected,new AuroraMultipleChoiceListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						// TODO Auto-generated method stub
						System.out.println("which="+which);
						System.out.println("isChecked="+isChecked);
						onItemSelectChoice(which);
						whichs=which;
						positions.put(which, isChecked);
						
						
					}
					
					@Override
					public void onInput(final EditText mEdit, final Button arg1) {
						// TODO Auto-generated method stub
						    mEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
					        mEdit.setSingleLine(true);
					        mEdit.setTextSize(18);
					        mEdit.addTextChangedListener(new TextWatcher() {
								
								@Override
								public void onTextChanged(CharSequence s, int start, int before, int count) {
									// TODO Auto-generated method stub
									
								}
								
								@Override
								public void beforeTextChanged(CharSequence s, int start, int count,
										int after) {
									// TODO Auto-generated method stub
									
								}
								
								@Override
								public void afterTextChanged(Editable s) {
									// TODO Auto-generated method stub
									System.out.println("afterTextChanged");
									
									arg1.setEnabled(!TextUtils.isEmpty(mEdit.getText().toString().trim()));
								}
							});
					}
					
					@Override
					public void onClick(DialogInterface arg0, int arg1, boolean equal,
							CharSequence arg3) {
						// TODO Auto-generated method stub
						System.out.println("arg1="+arg1);
						System.out.println("arg2="+equal);
						System.out.println("arg3="+arg3);
						
						
						
						onCompleted(arg3.toString());
						
					}
				})
    		//.setAdapter(mAdapter, null)
//    		.setMultiChoiceItems(sel,selected,new DialogInterface.OnMultiChoiceClickListener() {
//                
//                @Override
//                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
//                    // TODO Auto-generated method stub
//                	onItemSelectChoice(which);
//                }
//            })//aurora change zhouxiaobing 20131230
    		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
//					StringBuffer sb=new StringBuffer();
//					for(int i=0;i<mAdapter.getCount()-1;i++){
//						if(positions.get(i)){
//							System.out.println(groupNames.get(i));
//							sb.append(groupNames.get(i)).append(",");
//							
//						}
//					}
//					if(sb.length()>0){
//						mGroupList.setText(sb.toString().substring(0, sb.toString().length()-1));
//					}
					
					updateView();
					
				}
    			
    		}).setTitleDividerVisible(true)
    		.create();
    	mDialog.show();
    	
    	listView = mDialog.getListView();
        listView.setChoiceMode(AuroraListView.CHOICE_MODE_MULTIPLE);
        listView.setOverScrollMode(OVER_SCROLL_ALWAYS);
        int count = mAdapter.getCount();
        for (int i = 0; i < count; i++) {
            listView.setItemChecked(i, mAdapter.getItem(i).isChecked());
        }

 //       listView.setOnItemClickListener(this);
    }
//aurora add zhouxiaobing 20131218 start
    
    protected void onCompleted(String groupLabel) {
        
        if(!checkName(groupLabel, mAccountType, mAccountName)){
            return; 
        }
        this.getContext().startService(ContactSaveService.auroraCreateNewGroupIntent2(this.getContext(),
                new AccountWithDataSet(mAccountName, mAccountType, mDataSet), groupLabel,
                null,
                this.getContext().getClass(), Intent.ACTION_EDIT));
        groupNames.add(groupLabel);
        for (int i = 0; i < groupNames.size(); i++) {
			System.out.println(groupNames.get(i));
		}
        
        
        setIsneedDialog(true, groupLabel);

        
        
       
    }
    
    
    public boolean checkName(CharSequence name, String accountType, String accountName) {
        
       
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getContext(), R.string.name_needed, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(name.toString().contains("/") || name.toString().contains("%"))
        {
             Toast.makeText(getContext(), R.string.save_group_fail, Toast.LENGTH_SHORT).show();
             return false;
        }
        boolean nameExists = false;
   
        if (!nameExists) {
            Cursor cursor = getContext().getContentResolver().query(
                    Groups.CONTENT_SUMMARY_URI,
                    new String[] { Groups._ID },
                    Groups.TITLE + "=? AND " + Groups.ACCOUNT_NAME + " =? AND " +
                    Groups.ACCOUNT_TYPE + "=? AND " + Groups.DELETED + "=0",
                    new String[] { name.toString(), accountName, accountType}, null);     
          
            if (cursor == null || cursor.getCount() == 0) {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            } else {
                cursor.close();
                cursor = null;
                nameExists = true;
            }
        }
        //If group name exists, make a toast and return false.
        if (nameExists) {
            Toast.makeText(getContext(),
                    R.string.group_name_exists, Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }
    
    
    public void showDialogForBack(String groupname)
    {}
    public boolean is_need_dialog=false;
    public String group_label;
    public void setIsneedDialog(boolean b,String g)
    {
    	System.out.println("setIsneedDialog");
    	is_need_dialog=b;
    	group_label=g;
    }
//aurora add zhouxiaobing 20131218 end    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        dismissPopup();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    	ListView list = (ListView) parent;
        int count = mAdapter.getCount();
        if (mAccountType.equals(AccountType.ACCOUNT_TYPE_LOCAL_PHONE)) {
            if (list.isItemChecked(count - 1)) {
                list.setItemChecked(count - 1, false);
                createNewGroup();
                return;
            }
        }

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

    //aurora add zhoxuiaobing 
    public void onItemSelectChoice(int which)
    {
        int count = mAdapter.getCount();
        System.out.println("count="+count);
//        if (mAccountType.equals(AccountType.ACCOUNT_TYPE_LOCAL_PHONE)) {
//            if (which==count-1) {
//                createNewGroup();
//                return;
//            }
//        }
        mAdapter.getItem(which).setChecked(!mAdapter.getItem(which).isChecked());
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
        for (int i = 0; i < mAdapter.getCount(); i++) {
            GroupSelectionItem item = mAdapter.getItem(i);
            long groupId = item.getGroupId();
            if (item.isChecked()&& !hasMembership(groupId)) {
                ValuesDelta entry = EntityModifier.insertChild(mState, mKind);
                entry.put(GroupMembership.GROUP_ROW_ID, groupId);
            }
        }
//        updateView();    	
    }
    //
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
        GroupCreationDialogFragment.setAuroraGroupMembershipView(this);//aurora add zhouxiaobing 20131218
    }

    private String mUsimAccountType =  AccountType.ACCOUNT_TYPE_USIM;

    private void dismissPopup() {

        if (null != mDialog) {
        	mDialog.dismiss();
        }
        mDialog = null;
    }
}
