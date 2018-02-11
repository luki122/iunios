package com.mediatek.contacts.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import gionee.provider.GnContactsContract;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.RawContacts;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.android.contacts.ContactsActivity;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.R;
import com.android.contacts.group.AuroraGroupDetailFragment;
import com.android.contacts.list.AuroraGroupDetailAdapter;
import com.android.contacts.list.ContactListFilter;
import com.privacymanage.data.AidlAccountData;
import com.privacymanage.service.AuroraPrivacyUtils;

import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraSearchView;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraMenuItem;
import aurora.app.AuroraActivity;
import aurora.app.AuroraProgressDialog;
import aurora.app.AuroraActivity.OnSearchViewQuitListener;

/**
 * aurora:wangth 20130917 addmerge1
 */

public class AuroraContactListMultiChoiceActivity extends ContactsActivity 
        implements OnSearchViewQuitListener {
    
    private static final String TAG = "liyang-AuroraContactListMultiChoiceActivity";
    
    private static AuroraActionBar mActionBar;
    private Context mContext;
    
    private static String mSelectAllStr;
    private static String mUnSelectAllStr;
    
    private String mFilterExInfo;
    private ContactListFilter mFilter;
    
    private static AuroraContactListMultiChoiceFragment mFragment;
    
    public static final String EXTRA_ID_ARRAY = "idArray";
    public static final String EXTRA_CONTACT_ID_ARRAY = "contactIdArray";
    public static final String EXTRA_SIM_INDEX_ARRAY = "simIndexArray";
    
    private boolean mIsPrivacyMode = false;
    private PowerManager.WakeLock mWakeLock;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	ContactsApplication.sendSimContactBroad();
    	this.requestWindowFeature(Window.FEATURE_NO_TITLE);                
        setTheme(R.style.GN_PeopleTheme_light);
        super.onCreate(savedInstanceState);
        
        mContext = AuroraContactListMultiChoiceActivity.this;
        mSelectAllStr = mContext.getResources().getString(R.string.select_all);
        mUnSelectAllStr = mContext.getResources().getString(R.string.unselect_all);
        
        setAuroraContentView(R.layout.aurora_contact_list_multi_choice_activity,
                AuroraActionBar.Type.Dashboard);  
        mActionBar = getAuroraActionBar();
        Log.d(TAG,"getMiddleTextView:"+mActionBar.getMiddleTextView());
        mActionBar.getMiddleTextView().setText(mContext.getString(R.string.selected_total_num, 0));
        
        addSearchviewInwindowLayout();
        setAuroraBottomBarMenuCallBack(auroraMenuCallBack);
        mFragment = (AuroraContactListMultiChoiceFragment)getFragmentManager().findFragmentById(R.id.group_detail_fragment);
        
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mFilter = extras.getParcelable(MultiContactsPickerBaseFragment.EXTRA_ACCOUNT_FILTER);
            mFilterExInfo = extras.getString(MultiContactsPickerBaseFragment.EXTRA_ACCOUNT_FILTER_EXINFO);
            mIsPrivacyMode = extras.getBoolean("aurora_add_privacy_contacts");
            
            if (mIsPrivacyMode) {
            	ContactsApplication.mPrivacyActivityList.add(this);
            }
            
            if (mIsPrivacyMode || 
            		(mFilterExInfo != null && 
            		(mFilterExInfo.contains("noGroupId/") || 
            				mFilterExInfo.equals("export/") || 
            				mFilterExInfo.equals("exportSim/")))) {
                mActionBar.initActionBottomBarMenu(R.menu.aurora_add_group_member, 1);
                
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
                                String okButtonStr = mActionBar.getOkButton().getText().toString();
                                if (okButtonStr.equals(mSelectAllStr)) {
                                    mActionBar.getOkButton().setText(mUnSelectAllStr);
                                    mFragment.onSelectAll(true);
                                    mActionBar.getMiddleTextView().setText(mContext.getResources().getString(R.string.selected_total_num, count));
                                } else if (okButtonStr.equals(mUnSelectAllStr)) {
                                    mActionBar.getOkButton().setText(mSelectAllStr);
                                    mFragment.onSelectAll(false);
                                    mActionBar.getMiddleTextView().setText(mContext.getResources().getString(R.string.selected_total_num, 0));
                                }
                            }
                        });
            }
        }
        
        setOnSearchViewQuitListener(this);
        

    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	 if (mIsPrivacyMode) {
         	ContactsApplication.mPrivacyActivityList.remove(this);
         }
    }
    
    public boolean quit() {
    	Log.d(TAG,"quit search");
        mActionBar.setShowBottomBarMenu(true);
        mActionBar.showActionBottomeBarMenu();
        // mActionBar.showActionBarDashBoard();

        return true;
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK: {
            	Log.d(TAG,"mIsAuroraSearchMode:"+mFragment.mIsAuroraSearchMode);
                try {
                    if (mFragment.mIsAuroraSearchMode) {
//                        mActionBar.setShowBottomBarMenu(true);
//                        mActionBar.showActionBarDashBoard();
//                        mActionBar.showActionBottomeBarMenu();
                        mFragment.quitSeach();
                        
                    } else {
                        finish();
                    }
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }           	
                
                break;
            }
            
            default: {
            }
        }

        return super.onKeyDown(keyCode, event);
    }
    
    private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {

        @Override
        public void auroraMenuItemClick(int itemId) {
            switch (itemId) {
            case R.id.menu_add: {
            	if (mFragment == null || mFragment.mAdapter == null) {
            		break;
            	}
            	
                int selectedCount = mFragment.mAdapter.getCheckedItem().size();
                Log.d(TAG, "selectedCount:"+selectedCount+" totalCount:"+AuroraGroupDetailFragment.mItemCount);
                if (0 >= selectedCount || mContext == null) {
                    break;    
                }
                

                
                if (selectedCount+AuroraGroupDetailFragment.mItemCount > 100 && (mFilterExInfo != null 
                        && mFilterExInfo.contains("noGroupId/") || mIsPrivacyMode)) {
                    Toast.makeText(
                            mContext,
                            mContext.getResources().getString(
                                    R.string.aurora_add_group_more_toast),
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                
                final Intent retIntent = new Intent();
                long[] checkedIds = new long[selectedCount];
                int index = 0;
                Set<Long> contactsIds = mFragment.mAdapter.getCheckedItem().keySet();
                ArrayList<String> cIds = new ArrayList<String>();
                for (Long id : contactsIds) {
                    checkedIds[index++] = id;
                    cIds.add(String.valueOf(id));
                }
                
                if (mIsPrivacyMode) {
                	final PowerManager powerManager = (PowerManager) mContext
                            .getSystemService("power");
                    mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK
                            | PowerManager.ON_AFTER_RELEASE, TAG);
                	new AddPrivacyThread(cIds).start();
                	return;
                }
                
                int resultLen = 0;
                long[] idArray = new long[selectedCount];
                long[] contactIdArray = new long[selectedCount];
                int[] simIndexIds = new int[selectedCount];
                for (int position = 0, curArray = 0; position < selectedCount && curArray < selectedCount; ++ position) {
                    int contactId = (int)checkedIds[position];
                    int rawContactId = ContactsUtils.getRawContactId(mContext, contactId);
                    idArray[curArray] = rawContactId;
                    contactIdArray[curArray] = contactId;
                    simIndexIds[curArray] = -1;
                    Log.d(TAG, "idArray[" + curArray + "] = " + idArray[curArray]);
                    curArray++;
                    resultLen++;
                }

                Bundle extras = new Bundle();
                extras.putLongArray(EXTRA_ID_ARRAY, idArray);
                extras.putLongArray(EXTRA_CONTACT_ID_ARRAY, contactIdArray);
                extras.putIntArray("simIndexArray", simIndexIds);
                retIntent.putExtras(extras);
                setResult(Activity.RESULT_OK, retIntent);
                finish();

                break;
            }
            
            default:
                break;
            }
        }
    };
    
    int count;
    public void updateSelectedItemsView(int allCount) {
        count = allCount;
        if (mFragment == null || mFragment.mAdapter == null) {
            return;
        }
        
        int curArray = mFragment.mAdapter.getCheckedItem().size();
        Log.d(TAG, "curArray = " + curArray + "  count = " + count);
        mActionBar.getMiddleTextView().setText(mContext.getString(R.string.selected_total_num, curArray));
        if (curArray >= count) {
            mActionBar.getOkButton().setText(mUnSelectAllStr);
        } else {
            mActionBar.getOkButton().setText(mSelectAllStr);
        }
        
        try {
            if (count == 0) {
                mActionBar.getOkButton().setVisibility(View.GONE);
//                initButtomBar(false);
                return;
            } else {
                mActionBar.getOkButton().setVisibility(View.VISIBLE);
                if (!mFragment.mSearchViewHasFocus) {
//                    initButtomBar(true);
                }
            }
            
            if (curArray > 0) {
                setBottomMenuEnable(true);
            } else {
                setBottomMenuEnable(false);
            }
            
            setRightBtnTv(curArray);
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
            if (!isSearchviewLayoutShow()) {
                mActionBar.showActionBottomeBarMenu();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void setRightBtnTv (int checkCount) {
        Button conBut = getSearchViewRightButton();
        if (conBut != null) {
            String str = mContext.getResources().getString(R.string.aurora_search_continue);
//            if (checkCount > 0) {
//                str = mContext.getResources().getString(R.string.aurora_search_continue_mutil, checkCount);
//            }
            conBut.setText(str);
        }
    }
    
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
                    } catch (Exception e) {
                        
                    }
                }
                break;
            }
            
            case END: {
            	if (mWakeLock != null && mWakeLock.isHeld()) {
                    mWakeLock.release();
                }
            	
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
    
    private class AddPrivacyThread extends Thread {
        ArrayList<String> contactIds = new ArrayList<String>();
        
        public AddPrivacyThread(ArrayList<String> list) {
            this.contactIds = list;
        }
        
        @Override
        public void run() {
            if (contactIds == null || contactIds.size() < 1) {
                return;
            }
            
            mWakeLock.acquire();
            
            mHandler.sendEmptyMessage(START);
            ContentValues values = new ContentValues();
            
            for (String contact_id : contactIds) {
                values.put("is_privacy", AuroraPrivacyUtils.mCurrentAccountId);
                int updateRaw = mContext.getContentResolver().update(RawContacts.CONTENT_URI, values, 
                		RawContacts.CONTACT_ID + "=" + contact_id, null);
                int updateData = mContext.getContentResolver().update(Data.CONTENT_URI, values, 
                        Data.CONTACT_ID + "=" + contact_id + " AND is_privacy > -1", null);
                values.clear();
                Log.i(TAG, "AuroraPrivacyUtils.mCurrentAccountId = " + AuroraPrivacyUtils.mCurrentAccountId
                		+ "  updateRaw = " + updateRaw + "  updateData = " + updateData);
                try {
                	sleep(100);
                } catch (Exception e) {
                	e.printStackTrace();
                }
            }
            
            mHandler.sendEmptyMessage(END);
        }
    }
}
