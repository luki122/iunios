package com.android.contacts.activities;

import java.util.ArrayList;
import java.util.Set;

import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts.ContactsActivity;
import com.android.contacts.list.AuroraCallRecordContactListFragment;
import com.android.contacts.model.AccountType;
import com.android.contacts.util.IntentFactory;
import com.mediatek.contacts.list.MultiContactsPickerBaseFragment;

import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraMenuItem;
import aurora.app.AuroraActivity.OnSearchViewQuitListener;

/**
 * aurora:wangth 20130913 add
 */

public class AuroraCallRecordContactListActivity extends ContactsActivity 
        implements OnSearchViewQuitListener {
    private static final String TAG = "AuroraCallRecordContactListActivity";
    
    private static AuroraCallRecordContactListFragment mFragment;

    private static AuroraActionBar mActionBar;
    private Context mContext;

    private static String mSelectAllStr;
    private static String mUnSelectAllStr;
    private AuroraAlertDialog mDeleteConDialog;
    public static boolean mIsAddAutoRecording = false;
    private static final int START_DELETE = 0;
    private static final int END_DELETE = 1;
    private static final int ADD_CONTACT_TO_CALL_RECORD = 1;
    private static int removeCount = 0;
    
    private static boolean mIsOnResume = false;
    public static boolean mIsRemoving = false;
    
    public final static String[] DATA_ALLCOLUMNS = new String[] {
        Data._ID,
        Data.MIMETYPE,
        Data.IS_PRIMARY,
        Data.IS_SUPER_PRIMARY,
        Data.DATA1,
        Data.DATA2,
        Data.DATA3,
        Data.DATA4,
        Data.DATA5,
        Data.DATA6,
        Data.DATA7,
        Data.DATA8,
        Data.DATA9,
        Data.DATA10,
        Data.DATA11,
        Data.DATA12,
        Data.DATA13,
        Data.DATA14,
        Data.DATA15,
        Data.SYNC1,
        Data.SYNC2,
        Data.SYNC3,
        Data.SYNC4,
        Data.IS_ADDITIONAL_NUMBER
    };
    
    private ContactsUtils.AuroraContactsProgressDialog mSaveProgressDialog = null;
    private static final int START = 0;
    private static final int END = 1;
    private final Handler mHandler = new Handler() {
        
        @Override
        public void handleMessage(Message msg) {
            
            switch(msg.what) {
            case START: {
                mIsRemoving = true;
                if (!isFinishing()) {
                    if (null == mSaveProgressDialog) {
                        mSaveProgressDialog = new ContactsUtils.AuroraContactsProgressDialog(mContext, AuroraProgressDialog.THEME_AMIGO_FULLSCREEN);
                    }
                    mSaveProgressDialog.setTitle(R.string.aurora_removeing);
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
                mIsRemoving = false;
                if (!isFinishing() && null != mSaveProgressDialog && mSaveProgressDialog.isShowing()) {
                    try {
                        Toast.makeText(mContext, 
                                mContext.getResources().getString(
                                        R.string.aurora_remove_auto_record_toast_multi, removeCount), 
                                        Toast.LENGTH_SHORT).show();
                        
                        mSaveProgressDialog.dismiss();
                        mSaveProgressDialog = null;
                    } catch (Exception e) {
                        
                    }
                }
                
                if (null != mFragment) {
                    if (!mIsOnResume) {
                        try {
                            finish();
                            return;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    mFragment.changeToNormalMode(false);
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
                
        if (ContactsApplication.sIsAuroraPrivacySupport) {
        	ContactsApplication.mPrivacyActivityList.add(this);
        }

        mContext = AuroraCallRecordContactListActivity.this;
        mSelectAllStr = mContext.getResources().getString(R.string.select_all);
        mUnSelectAllStr = mContext.getResources().getString(
                R.string.unselect_all);

        setAuroraContentView(R.layout.aurora_call_record_contact_list_activity,
                AuroraActionBar.Type.Normal);
        addSearchviewInwindowLayout();
        
        mActionBar = getAuroraActionBar();
        setAuroraMenuCallBack(auroraMenuCallBack);
        mActionBar.setTitle(R.string.aurora_record_select_contact);
        addAuroraActionBarItem(AuroraActionBarItem.Type.Add, ADD_CONTACT_TO_CALL_RECORD);
        mActionBar.setOnAuroraActionBarListener(auroraActionBarItemClickListener);
        mActionBar.initActionBottomBarMenu(R.menu.aurora_remove, 1);

        mFragment = (AuroraCallRecordContactListFragment) getFragmentManager()
                .findFragmentById(R.id.call_record_contact_list_fragment);

        setOnSearchViewQuitListener(this);
        showLeftRight();
    }
    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        
        mIsOnResume = true;
        super.onResume();
    }
    
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        
        mIsOnResume = false;
        super.onPause();
    }
    
    private void showLeftRight() {
        if (mActionBar == null) {
            return;
        }
        
        if (mActionBar.getSelectLeftButton() != null ) {
            mActionBar.getSelectLeftButton().setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    mActionBar.setShowBottomBarMenu(false);
                    mActionBar.showActionBarDashBoard();
                    
                    if (mFragment != null && mFragment.getRemoveMemberMode()) {
                        mFragment.changeToNormalMode(true);
                    }
                }
            });
        }
        
        if (mActionBar.getSelectRightButton() != null ) {
            mActionBar.getSelectRightButton().setOnClickListener(new OnClickListener() {

                @Override
                        public void onClick(View v) {
                            // TODO Auto-generated method stub

                            String selectStr = ((TextView) (mActionBar.getSelectRightButton())).getText().toString();
                            if (mFragment != null && selectStr.equals(mSelectAllStr)) {
                                ((TextView) (mActionBar.getSelectRightButton())).setText(mUnSelectAllStr);
                                mFragment.onSelectAll(true);
                            } else if (mFragment != null && selectStr.equals(mUnSelectAllStr)) {
                                ((TextView) (mActionBar.getSelectRightButton())).setText(mSelectAllStr);
                                mFragment.onSelectAll(false);
                            }
                        }
            });
        }
    }
    
    // exit from search mode
    public boolean quit() {
        if (mFragment.getRemoveMemberMode()) {
            mActionBar.setShowBottomBarMenu(true);
            mActionBar.showActionBottomeBarMenu();
        }
        
        mFragment.getListView().auroraSetNeedSlideDelete(true);
        return true;
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (ContactsApplication.sIsAuroraPrivacySupport) {
        	ContactsApplication.mPrivacyActivityList.remove(this);
        }
    }
    
    private OnAuroraActionBarItemClickListener auroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
        public void onAuroraActionBarItemClicked(int itemId) {
            switch (itemId) {
            case ADD_CONTACT_TO_CALL_RECORD:
                if (null == mFragment || null == mFragment.mAdapter) {
                    break;
                }
                
                if (mIsRemoving) {
                    ContactsUtils.toastManager(mContext, R.string.aurora_auto_recording_remove);
                    break;
                }
                
                if (mIsAddAutoRecording) {
                    ContactsUtils.toastManager(mContext, R.string.aurora_auto_recording);
                    break;
                }
                
                Intent intent = new Intent(AuroraCallRecordContactListActivity.this, AuroraSimContactListActivity.class);
                intent.putExtra("aurora_call_record_select", true);
                intent.putExtra(MultiContactsPickerBaseFragment.EXTRA_ACCOUNT_FILTER_EXINFO, "add_to_auto_record/");
                ArrayList<String> values = new ArrayList<String>();
                for (int i = 0; i < mFragment.mItemCount; i++) {
                    values.add(String.valueOf(mFragment.mAdapter.getDataId(i)));
                }
                intent.putExtra("auto_record_data_ids", values);
                startActivity(intent);
                break;
                
            default:
                break;
            }
        }
    };
    
    private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {
        @Override
        public void auroraMenuItemClick(int itemId) {
            switch (itemId) {
            case R.id.menu_delete: {
                removeMember();
                break;
            }
            }
        }
    };
    
    private void removeMember() {
        String message = ContactsApplication.getInstance().
                getResources().getString(R.string.aurora_remove_auto_record_message_multi);
        if (null == mDeleteConDialog) {
            mDeleteConDialog = new AuroraAlertDialog.Builder(this,
                    AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
                    .setTitle(R.string.gn_remove)
                    .setMessage(message)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int whichButton) {
                                    realRemoveMember();
                                }
                            }).create();
        }

        mDeleteConDialog.show();
    }
    
    private void realRemoveMember() {
        removeCount = 0;
        if (mDeleteConDialog != null) {
            mDeleteConDialog.dismiss();
            mDeleteConDialog = null;
        }
        
        int selectedCount = mFragment.getAdapter().getCheckedItem().size();
        Set<Long> dataIds = mFragment.mAdapter.getCheckedItem().keySet();
        Log.e(TAG, "selectedCount = " + selectedCount + "   dataIds = " + dataIds.size());
        final ArrayList<Long> dataIdForAutoRecord = new ArrayList<Long>();
        for (long dataId : dataIds) {
            dataIdForAutoRecord.add(dataId);
        }
        Log.e(TAG, "dataIdForAutoRecord = " + dataIdForAutoRecord.size());
        new RemoveMemberThread(dataIdForAutoRecord).start();
    }
    
    private class RemoveMemberThread extends Thread {
        ArrayList<Long> dataIdForAutoRecord = new ArrayList<Long>();
        
        public RemoveMemberThread(ArrayList<Long> d) {
            dataIdForAutoRecord = d;
        }
        
        @Override
        public void run() {
            if (dataIdForAutoRecord == null || dataIdForAutoRecord.size() < 1) {
                return;
            }
            
            mHandler.sendEmptyMessage(START);
            int successfulItems = 0;
            ContentValues values = new ContentValues();
            
            for (long dataId : dataIdForAutoRecord) {
                values.put("auto_record", 0);
                removeCount += mContext.getContentResolver().update(Data.CONTENT_URI, values, 
                        Data._ID + "=" + dataId, null);
                values.clear();
            }
            
            mHandler.sendEmptyMessage(END);
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK: {
            try {
                boolean deleteIsShow = mFragment.getListView().auroraIsRubbishOut();
                if (deleteIsShow) {
                    mFragment.getListView().auroraSetRubbishBack();
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            if (mActionBar != null && 
                    (mActionBar.auroraIsExitEditModeAnimRunning() || mActionBar.auroraIsEntryEditModeAnimRunning())) {
                return true;
            }
            
            if (mFragment.getRemoveMemberMode()) {
                try {
                    Thread.sleep(300);
                    
                    if (isSearchviewLayoutShow()) {
                        hideSearchviewLayout();
                        mActionBar.setShowBottomBarMenu(true);
                        mActionBar.showActionBottomeBarMenu();
                    } else {
                        mActionBar.setShowBottomBarMenu(false);
                        mActionBar.showActionBarDashBoard();
                        mFragment.changeToNormalMode(true);
                    }
                    
                    mFragment.getListView().auroraSetNeedSlideDelete(true);
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
    
    public void updateSelectedItemsView(int allCount) {
        if (mFragment == null || mFragment.mAdapter == null) {
            return;
        }
        
        int checkedItemCount = mFragment.mAdapter.getCheckedItem().size();
        
        try {
            if (checkedItemCount >= allCount) {
                ((TextView) (mActionBar.getSelectRightButton())).setText(mUnSelectAllStr);
            } else {
                ((TextView) (mActionBar.getSelectRightButton())).setText(mSelectAllStr);
            }
            
            if (checkedItemCount > 0) {
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
        Button conBut = getSearchViewRightButton();
        if (conBut != null) {
            String str = mContext.getResources().getString(R.string.aurora_search_continue);
            if (!mFragment.getRemoveMemberMode()) {
                str = mContext.getResources().getString(R.string.cancel);
            }
            conBut.setText(str);
        }
    }

}
