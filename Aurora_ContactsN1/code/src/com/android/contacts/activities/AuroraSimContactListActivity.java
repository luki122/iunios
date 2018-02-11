package com.android.contacts.activities;

import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.android.contacts.ContactsActivity;
import com.android.contacts.list.AuroraSimContactListFragment;
import com.android.contacts.model.AccountType;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.util.RejectStatisticsUtil;
import com.android.contacts.util.YuloreUtils;

import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraMenuItem;
import aurora.app.AuroraActivity.OnSearchViewQuitListener;

/**
 * aurora:wangth 20130913 add
 */

public class AuroraSimContactListActivity extends ContactsActivity 
        implements OnSearchViewQuitListener {
    private static final String TAG = "AuroraSimContactListActivity";
    
    private static AuroraSimContactListFragment mFragment;

    private static AuroraActionBar mActionBar;
    private Context mContext;

    private static String mSelectAllStr;
    private static String mUnSelectAllStr;
    public static final String ACTION_FOR_MMS_SELECT = "com.aurora.action.mms.select.contact";
    private boolean mIsMmsSelectContact = false;
    private boolean mIsAttachment = false;
    private boolean mIsCallRecord = false;
    private boolean mIsBlackName = false;
    private ArrayList<String> mNumberList = new ArrayList<String>();
    private boolean mIsAddBlacking = false;
    public static final String ACTION_FOR_EMAIL_SELECT= "com.aurora.action.email.select.contact";
    private boolean mIsEmailSelect = false;
    private ArrayList<String> mEmailList = new ArrayList<String>();
    public static final char POLYPHONIC_SEPARATOR = 'þ';
    
    private ContactsUtils.AuroraContactsProgressDialog mSaveProgressDialog = null;
    private static final int START = 0;
    private static final int END = 1;
    private final Handler mHandler = new Handler() {
        
        @Override
        public void handleMessage(Message msg) {
            
            switch(msg.what) {
            case START: {
                if (!isFinishing()) {
                    if (null == mSaveProgressDialog) {
                        mSaveProgressDialog = new ContactsUtils.AuroraContactsProgressDialog(mContext, AuroraProgressDialog.THEME_AMIGO_FULLSCREEN);
                    }
                    mSaveProgressDialog.setTitle(R.string.aurora_save_group_dialog_title);
                    mSaveProgressDialog.setIndeterminate(false);
                    mSaveProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    try {
                        mSaveProgressDialog.show();
                        AuroraCallRecordContactListActivity.mIsAddAutoRecording = true;
                    } catch (Exception e) {
                        
                    }
                }
                break;
            }
            
            case END: {
            	mIsAddBlacking = false;
                AuroraCallRecordContactListActivity.mIsAddAutoRecording = false;
                if (!isFinishing() 
                        && null != mSaveProgressDialog && mSaveProgressDialog.isShowing()) {
                    try {
                        mSaveProgressDialog.dismiss();
                        mSaveProgressDialog = null;
                        finish();
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
    protected void onCreate(Bundle savedState) {
    	this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedState);
        
        mContext = AuroraSimContactListActivity.this;
        mSelectAllStr = mContext.getResources().getString(R.string.select_all);
        mUnSelectAllStr = mContext.getResources().getString(
                R.string.unselect_all);

        String action = getIntent().getAction();
        if (null != action) {
            if (ACTION_FOR_MMS_SELECT.equals(action)) {
            	mIsMmsSelectContact = true;
            } else if (ACTION_FOR_EMAIL_SELECT.equals(action)) {
            	mIsEmailSelect = true;
            }
        }
        
        Bundle extras = getIntent().getExtras();
        if (null != extras) {
            mIsAttachment = extras.getBoolean("isAttachment");
            mIsCallRecord = extras.getBoolean("aurora_call_record_select");
            mIsBlackName = extras.getBoolean("blackname_select");
            
            if (mIsCallRecord || mIsMmsSelectContact || mIsBlackName || mIsEmailSelect) {
                setAuroraContentView(R.layout.aurora_sim_contact_list_activity,
                        AuroraActionBar.Type.Dashboard);
            } else {
                setAuroraContentView(R.layout.aurora_sim_contact_list_activity,
                        AuroraActionBar.Type.Normal);
            }
            addSearchviewInwindowLayout();
            
            if (mIsMmsSelectContact) {
                mNumberList = extras.getStringArrayList("ContactNumbers");
                Log.i(TAG, "From mms ContactNumbers : " + mNumberList);
            } else if (mIsEmailSelect) {
            	mEmailList = extras.getStringArrayList("emails");
            	Log.i(TAG, "From email addresses : " + mEmailList);
            }
        }
        
        if (ContactsApplication.sIsAuroraPrivacySupport && !mIsEmailSelect) {
        	ContactsApplication.mPrivacyActivityList.add(this);
        }
        
        mActionBar = getAuroraActionBar();
        mFragment = (AuroraSimContactListFragment) getFragmentManager()
                .findFragmentById(R.id.sim_contact_fragment);
        
//        mHandler.postDelayed(new Runnable() {
//
//			@Override
//			public void run() {
				setAuroraMenuCallBack(auroraMenuCallBack);
				if (mIsCallRecord || mIsMmsSelectContact || mIsBlackName || mIsEmailSelect) {
		            mActionBar.initActionBottomBarMenu(R.menu.aurora_add, 1);

		            mActionBar.getOkButton().setText(mSelectAllStr);
		            mActionBar.getCancelButton().setOnClickListener(
		                    new View.OnClickListener() {

		                        @Override
		                        public void onClick(View v) {
		                            finish();
		                        }
		                    });

		            mActionBar.getOkButton().setOnClickListener(
		                    new View.OnClickListener() {

		                        @Override
		                        public void onClick(View v) {
		                            String okButtonStr = mActionBar.getOkButton()
		                                    .getText().toString();
		                            if (okButtonStr.equals(mSelectAllStr)) {
		                                mActionBar.getOkButton().setText(mUnSelectAllStr);
		                                mFragment.onSelectAll(true);
		                            } else if (okButtonStr.equals(mUnSelectAllStr)) {
		                                mActionBar.getOkButton().setText(mSelectAllStr);
		                                mFragment.onSelectAll(false);
		                            }
		                        }
		                    });
		        }
//			}
//		}, 50);

        setOnSearchViewQuitListener(this);
    }
    
    public boolean quit() {
        if (mIsCallRecord || mIsMmsSelectContact || mIsBlackName || mIsEmailSelect) {
            mActionBar.setShowBottomBarMenu(true);
            mActionBar.showActionBottomeBarMenu();
            // mActionBar.showActionBarDashBoard();
        }
        
        return true;
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (ContactsApplication.sIsAuroraPrivacySupport && !mIsEmailSelect) {
        	ContactsApplication.mPrivacyActivityList.remove(this);
        }
    }
    
    private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {
        @Override
        public void auroraMenuItemClick(int itemId) {
            switch (itemId) {
            case R.id.menu_add: {
                addContacts();
                break;
            }
            }
        }
    };
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK: {
            if (mIsCallRecord || mIsMmsSelectContact || mIsBlackName || mIsEmailSelect) {
                try {
                    if (isSearchviewLayoutShow()) {
                        mActionBar.setShowBottomBarMenu(true);
//                        mActionBar.showActionBarDashBoard();
                        mActionBar.showActionBottomeBarMenu();
                        hideSearchviewLayout();
                    } else {
                        finish();
                    }
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            break;
        }

        default: {
        }
        }

        return super.onKeyDown(keyCode, event);
    }
    
    private void addContacts() {
        if (mFragment == null || mFragment.mAdapter == null) {
            return;
        }
        
        int selectedCount = mFragment.mAdapter.getCheckedItem().size();
        if (0 >= selectedCount) {
            return;
        }
        
        if (mIsMmsSelectContact && !mIsAttachment && selectedCount > 100) {
            ContactsUtils.toastManager(mContext, R.string.aurora_add_mms_more_toast);
            return;
        }
        
        if (mIsMmsSelectContact && mIsAttachment && selectedCount > 1000) {
            ContactsUtils.toastManager(mContext, R.string.aurora_add_mms_attachment_more_toast);
            return;
        }
        
        if (mIsBlackName && selectedCount > 100) {
        	ContactsUtils.toastManager(mContext, R.string.aurora_add_blackname_more_toast);
        	return;
        }
        RejectStatisticsUtil.addBlackCount(mContext, selectedCount);
        Set<Long> dataIds = mFragment.mAdapter.getCheckedItem().keySet();
        ArrayList<String> numbersForMms = new ArrayList<String>();
        ArrayList<String> namesForBlack = new ArrayList<String>();
        ArrayList<String> numbersForBlack = new ArrayList<String>();
        ArrayList<String> dataIdForAutoRecord = new ArrayList<String>();
        long dataId = -1;
        String number;
        String nameBlack;
        String numberBlack;
        
        for (int position = 0; position < mFragment.mItemCount; position++) {
            dataId = mFragment.mAdapter.getDataId(position);
            if (dataIds.contains(dataId)) {
            	if (mIsBlackName) {
            		nameBlack =  mFragment.mAdapter.getContactDisplayName(position);
            		numberBlack = mFragment.mAdapter.getNumber(position);
            		Log.d(TAG, "nameBlack = " + nameBlack + "  numberBlack = " + numberBlack + "  dataId = " + dataId);
            		if (numbersForBlack.contains(numberBlack)) {
            			continue;
            		}
            		namesForBlack.add(nameBlack);
            		numbersForBlack.add(numberBlack);
            	}
            	
                if (mIsCallRecord || mIsBlackName) {
                    dataIdForAutoRecord.add(String.valueOf(dataId));
                } else {
                	number = mFragment.mAdapter.getCheckedItem().get(dataId);
                	if (!numbersForMms.contains(number)) {
                		numbersForMms.add(number);
                		Log.e(TAG, "NUMBER = " + number);
                	}
                }
            }
        }
        
        if (mIsCallRecord) {
            new AddAutoRecordThread(dataIdForAutoRecord).start();
            return;
        } else if (mIsBlackName) {
        	if (dataIdForAutoRecord.size() > 100) {
        	    ContactsUtils.toastManager(mContext, R.string.aurora_add_blackname_more_toast);
        	} else {
        		if (mIsAddBlacking) {
        			return;
        		}
        		
        		mIsAddBlacking = true;
        		new AddBlackNameThread(dataIdForAutoRecord, namesForBlack, numbersForBlack).start();
        	}
        	
            return;
        }
        
        mNumberList = mFragment.mAdapter.getMmsSelectList();
        if (mIsEmailSelect) {
        	mNumberList = mFragment.mAdapter.getEmailSelectList();
        }
        int index = 0;
        if (mNumberList != null && mNumberList.size() > 0) {
            String strChecked, strComeIn;
            boolean flag = false;
            for (int i = 0; i < mNumberList.size(); i++) {
                strComeIn = mNumberList.get(i);
                for (int j = 0; j < numbersForMms.size(); j++) {
                    strChecked = numbersForMms.get(j);
                    if (strChecked.contains(strComeIn)) {
                        flag = true;
                    }
                }
                
                if (!flag) {
                    numbersForMms.add(index, mNumberList.get(i));
                    index++;
                }
                flag = false;
            }
        }
        
        if (!mIsEmailSelect && !mIsAttachment && numbersForMms.size() > 100) {
            ContactsUtils.toastManager(mContext, R.string.aurora_add_mms_more_toast);
            return;
        } else if (mIsEmailSelect && numbersForMms.size() > 100) {
        	ContactsUtils.toastManager(mContext, R.string.aurora_add_blackname_more_toast);
        	return;
        }
        
        if (mIsMmsSelectContact) {
            Intent intent = new Intent();
            intent.putStringArrayListExtra("ContactNumbers", numbersForMms);
            Log.i(TAG, "To Mms ContactNumbers : " + numbersForMms);
            setResult(Activity.RESULT_OK, intent);
            finish();
            return;
        } else if (mIsEmailSelect) {
        	Intent intent = new Intent();
            intent.putStringArrayListExtra("emails", numbersForMms);
            Log.i(TAG, "To Email : " + numbersForMms);
            setResult(Activity.RESULT_OK, intent);
            finish();
            return;
        }
    }
    
    private class AddAutoRecordThread extends Thread {
        ArrayList<String> dataIdForAutoRecord = new ArrayList<String>();
        
        public AddAutoRecordThread(ArrayList<String> list) {
            this.dataIdForAutoRecord = list;
        }
        
        @Override
        public void run() {
            if (dataIdForAutoRecord == null || dataIdForAutoRecord.size() < 1) {
                return;
            }
            
            mHandler.sendEmptyMessage(START);
            ContentValues values = new ContentValues();
            
            for (String dataId : dataIdForAutoRecord) {
                values.put("auto_record", 1);
                mContext.getContentResolver().update(Data.CONTENT_URI, values, 
                        Data._ID + "=" + dataId, null);
                values.clear();
            }
            
            mHandler.sendEmptyMessage(END);
        }
    }
    
    private class AddBlackNameThread extends Thread {
        ArrayList<String> dataIdForBlackName = new ArrayList<String>();
        ArrayList<String> nameForBlackName = new ArrayList<String>();
        ArrayList<String> numberForBlackName = new ArrayList<String>();
        ArrayList<String> numberAdded = new ArrayList<String>();
        
        public AddBlackNameThread(ArrayList<String> list, ArrayList<String> nameList, ArrayList<String> numberList) {
            this.dataIdForBlackName = list;
            this.nameForBlackName = nameList;
            this.numberForBlackName = numberList;
        }
        
        @Override
        public void run() {
            if (dataIdForBlackName == null || dataIdForBlackName.size() < 1) {
            	mIsAddBlacking = false;
                return;
            }
            
            mHandler.sendEmptyMessage(START);
            ContentValues values = new ContentValues();
            
            for (int i = 0; i < dataIdForBlackName.size(); i++) {
            	String number = numberForBlackName.get(i);
            	if (number == null) {
            		continue;
            	}
            	
            	if (numberAdded.contains(number)) {
            		continue;
            	}
            	
            	values.put("isblack", 1);
            	values.put("black_name", nameForBlackName.get(i));
            	values.put("number", number);
            	values.put("reject", 3);
            	String mark = YuloreUtils.getInstance(mContext).getUserMark(mContext, number);
            	int userMark = -1;
            	
            	if (mark == null) {
            		mark = YuloreUtils.getInstance(mContext).getMarkContent(number,mContext);
            		userMark = YuloreUtils.getInstance(mContext).getMarkNumber(mContext, number);
            	}
            	if (null != mark) {
            		values.put("lable", mark);
            		values.put("user_mark", userMark);
            	}
            	Log.d(TAG, "number = " + number + "  mark = " + mark + " markCount = " + userMark);
            	
            	mContext.getContentResolver().insert(Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "black"), values);
                values.clear();
                numberAdded.add(number);
                
                try {
                	sleep(200);
                } catch (Exception e) {
                	e.printStackTrace();
                }
            }
            
            mHandler.sendEmptyMessage(END);
        }
    }
    
    public void updateSelectedItemsView(int allCount) {
        if (mFragment == null || mFragment.mAdapter == null) {
            return;
        }
        
        int count = allCount - mFragment.mAdapter.getStarredCount();
        int checkedItemCount = mFragment.mAdapter.getCheckedItem().size();
        
        try {
            if (count == 0) {
                mActionBar.getOkButton().setVisibility(View.GONE);
                initButtomBar(false);
                return;
            } else {
                mActionBar.getOkButton().setVisibility(View.VISIBLE);
                if (!mFragment.mSearchViewHasFocus) {
                    initButtomBar(true);
                }
            }
            
            if (checkedItemCount >= count) {
                mActionBar.getOkButton().setText(mUnSelectAllStr);
            } else {
                mActionBar.getOkButton().setText(mSelectAllStr);
            }
            
//            mActionBar.showActionBottomeBarMenu();
            
            if (checkedItemCount > 0 || (mFragment.mAdapter.getMmsSelectList() != null && mFragment.mAdapter.getMmsSelectList().size() > 0)) {
                setBottomMenuEnable(true);
            } else {
                setBottomMenuEnable(false);
            }
            
            setRightBtnTv(checkedItemCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void setBottomMenuEnable(boolean flag) {
        AuroraMenu auroraMenu = mActionBar.getAuroraActionBottomBarMenu();
        auroraMenu.setBottomMenuItemEnable(1, flag);
    }
    
    private void initButtomBar(boolean flag) {
        try {
            mActionBar.setShowBottomBarMenu(flag);
            boolean isSearchShowing = isSearchviewLayoutShow();
            if (!isSearchShowing) {
                mActionBar.showActionBottomeBarMenu();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void setRightBtnTv (int checkCount) {
        if (mIsMmsSelectContact || mIsCallRecord || mIsBlackName || mIsEmailSelect) {
            Button conBut = getSearchViewRightButton();
            if (conBut != null) {
                String str = mContext.getResources().getString(R.string.aurora_search_continue);
//                if (checkCount > 0) {
//                    str = mContext.getResources().getString(R.string.aurora_search_continue_mutil, checkCount);
//                }
                conBut.setText(str);
            }
        }
    }

}
