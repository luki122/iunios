/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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

package com.aurora.mms.ui;

import java.util.ArrayList;
// Aurora liugj 2013-09-30 added for aurora's new feature start
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
// Aurora liugj 2013-09-30 added for aurora's new feature end

import com.android.mms.R;
import android.database.sqlite.SqliteWrapper;
import com.android.mms.transaction.MessagingNotification;

import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
//Aurora liugj 2013-09-17 added for aurora's new feature start
import aurora.widget.AuroraActionBar;
//Aurora liugj 2013-09-17 added for aurora's new feature end
// Aurora liugj 2013-09-30 modified for aurora's new feature start
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraCheckBox;
// Aurora liugj 2013-10-09 modified for aurora's new feature start
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
// Aurora liugj 2013-10-09 modified for aurora's new feature end
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
// Aurora liugj 2013-09-30 modified for aurora's new feature end
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;

import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Browser;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.Telephony.Sms;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.PopupWindow;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import aurora.widget.AuroraListView;
import android.widget.TextView;

import gionee.telephony.gemini.GnGeminiSmsManager;
import com.gionee.internal.telephony.GnITelephony;
import gionee.telephony.GnSmsManager;
import gionee.telephony.GnSmsMemoryStatus;
import gionee.provider.GnTelephony;
import gionee.telephony.GnTelephonyManager;
import com.gionee.internal.telephony.GnTelephonyManagerEx;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.app.Dialog;
import aurora.app.AuroraProgressDialog;
import android.os.ServiceManager;
import android.os.RemoteException;
import android.widget.Toast;
import gionee.provider.GnTelephony.SIMInfo;
import android.content.ContentUris;
import android.os.SystemProperties;
import com.android.internal.telephony.ITelephony;
import com.gionee.internal.telephony.GnPhone;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.MmsApp;
import com.android.mms.util.GnSelectionManager;
import com.android.mms.util.Recycler;
import com.aurora.featureoption.FeatureOption;
//gionee gaoj 2012-6-26 added for CR00627832 start
import android.provider.Telephony.Mms;
//gionee gaoj 2012-6-26 added for CR00627832 end

import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.MyScrollListener;
//gionee wangym 2012-11-22 add for CR00735223 start
import com.android.mms.ui.ScaleDetector.OnScaleListener;

import android.view.MotionEvent;
//gionee wangym 2012-11-22 add for CR00735223 end
import android.provider.Telephony.Threads;
// Aurora xuyong 2013-09-13 modified for aurora's new feature start
//import android.app.ActionBar;
// Aurora xuyong 2013-09-13 modified for aurora's new feature end

/**
 * Displays a list of the SMS messages stored on the ICC.
 */
// Aurora liugj 2013-09-25 created for aurora's new feature
// Aurora liugj 2013-09-30 modified for aurora's new feature start
public class AuroraManageSimMessages extends AuroraActivity
        implements /*View.OnCreateContextMenuListener,*/
        OnItemClickListener, OnItemLongClickListener,
        OnKeyListener {
    
    private static final boolean DEBUG = false;
    // Aurora liugj 2013-11-12 added for aurora's new feature start
    private static final int INDEX_COLUMN_ADDRESS = 1;
    private static final int INDEX_COLUMN_DATE = 4;
    private static final int INDEX_COLUMN_BODY = 3;
    private static final int INDEX_COLUMN_SCA = 0;
    private static final int INDEX_COLUMN_STATUS = 5;
    private static final int INDEX_COLUMN_ICC = 6;
    // Aurora liugj 2013-11-12 added for aurora's new feature end
    
    private static final int BATCH_COPY = 0;
    private static final int ACTION_BATCH = 0;
    private static final int DIALOG_REFRESH = 1; 
// Aurora liugj 2013-09-30 modified for aurora's new feature end
    private static Uri ICC_URI;
    private static final String TAG = "ManageSimMessages";
    private static final int MENU_COPY_TO_PHONE_MEMORY = 0;
    private static final int MENU_DELETE_FROM_SIM = 1;
    private static final int MENU_FORWARD = 2;
    private static final int MENU_REPLY =3;
    private static final int MENU_ADD_TO_BOOKMARK      = 4;
    private static final int MENU_CALL_BACK            = 5;
    private static final int MENU_SEND_EMAIL           = 6;
    private static final int MENU_ADD_ADDRESS_TO_CONTACTS = 7;
    private static final int MENU_SEND_SMS              = 9;
    private static final int MENU_ADD_CONTACT           = 10;
    
    private static final int OPTION_MENU_DELETE_ALL = 0;
    //MTK_OP01_PROTECT_START
    private static final int OPTION_MENU_SIM_CAPACITY = 1;
    //MTK_OP01_PROTECT_END

     // Aurora liugj 2014-01-21 modified for aurora's new feature start
    private static final int SHOW_BUSY = 0;
    private static final int SHOW_LIST = 1;
    private static final int SHOW_EMPTY = 2;
     // Aurora liugj 2013-11-12 modified for aurora's new feature end
    private int mState;
    AuroraProgressDialog dialog;
    private static final String ALL_SMS = "999999"; 
    private static final String FOR_MULTIDELETE = "ForMultiDelete";
    private int currentSlotId = 0;

    private ContentResolver mContentResolver;
    private Cursor mCursor = null;
    // Aurora liugj 2013-10-11 modified for aurora's new feature start
    private AuroraListView mSimList;
    // Aurora liugj 2013-10-11 modified for aurora's new feature end
    private TextView mMessage;
    private SimMessageListAdapter mListAdapter = null;
    private AsyncQueryHandler mQueryHandler = null;

    public static final int SIM_FULL_NOTIFICATION_ID = 234;
    public boolean isQuerying = false;
    public boolean isDeleting = false;    
    private boolean isInit = false;
     // Aurora liugj 2013-12-10 deleted for checkbox animation start
    //private boolean mFirstInMultiMode = false;
     // Aurora liugj 2013-12-10 deleted for checkbox animation end
    public static int observerCount = 0; 
    //extract telephony number ...
    private ArrayList<String> mURLs = new ArrayList<String>();
    private ContactList mContactList;
    public static AuroraManageSimMessages mSimMessages = null;
    
    // Aurora liugj 2013-09-30 added for aurora's new feature start
    private AuroraActionBatchHandler<Integer> mActionBatchHandler = null;
    private AuroraActionBar mActionBar;
    private AuroraMenu mAuroraMenu;
    private AuroraMenu mBottomAuroraMenu;
    private Map<Integer, Integer> mThreadsMap = new HashMap<Integer, Integer>();
    private int mSelectCount = 0;
    // Aurora liugj 2013-09-30 added for aurora's new feature end
    
    private final ContentObserver simChangeObserver =
            new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfUpdate) {
            if(!isQuerying){
                refreshMessageList();
            }else{
                if(isDeleting == false){
                    observerCount ++;
                }
                Log.e(TAG, "observerCount = " + observerCount);                
            }
        }
    };
    
    // Aurora liugj 2013-09-30 added for aurora's new feature start
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BATCH_COPY:
                    // Aurora liugj 2013-11-08 added for aurora's new feature start
                    if(null != mActionBatchHandler) {
                        mActionBatchHandler.leaveSelectionMode();
                  }
                    // Aurora liugj 2013-11-08 added for aurora's new feature end
                    AuroraManageSimMessages.this.removeDialog(DIALOG_REFRESH);
                    Toast.makeText(AuroraManageSimMessages.this, getString(R.string.copyto_phone_done), Toast.LENGTH_SHORT)
                            .show();
                    break;
                default:
                    break;
            }
        }
        
    };
    // Aurora liugj 2013-09-30 added for aurora's new feature end
    // Aurora xuyong 2014-03-21 added for aurora's new feature start
    
    private void onBottomMenuDismiss() {
        if (mListAdapter != null) {
             mListAdapter.notifyDataSetChanged();
         }
         if (mSimList != null) {
             mSimList.auroraSetNeedSlideDelete(true);
             mSimList.auroraEnableSelector(true);
         }
         if (mListAdapter != null) {
             mListAdapter.showCheckBox(false);
             mListAdapter.setCheckBoxAnim(false);
             mListAdapter.updateAllCheckBox(0);
         }
         if (mActionBatchHandler != null) {
             mActionBatchHandler.destroyAction();
         }
         if (mListAdapter != null) {
             mListAdapter.notifyDataSetChanged();
         }
         AuroraManageSimMessages.this.setMenuEnable(true);
         if (mActionBatchHandler != null) {
             mActionBatchHandler = null;
         }
         if (mThreadsMap != null) {
             mThreadsMap.clear();
         }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                 // Aurora xuyong 2014-03-22 modified for aurora's new feature start
                 if (mActionBar != null && (mActionBar.auroraIsExitEditModeAnimRunning() || mActionBar.auroraIsEntryEditModeAnimRunning())) {
                 // Aurora xuyong 2014-03-22 modified for aurora's new feature end
                        return true;
                    }
                 if (mBottomAuroraMenu != null && mBottomAuroraMenu.isShowing() && mActionBatchHandler != null) {
                     mActionBatchHandler.leaveSelectionMode();
                     return true;
                 }
                 break;
        }
        return super.onKeyDown(keyCode, event);
    }
    // Aurora xuyong 2014-03-21 added for aurora's new feature end
    @Override
    protected void onCreate(Bundle icicle) {
        // Aurora xuyong 2015-06-30 added for bug #13935 start
    	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        // Aurora xuyong 2015-06-30 added for bug #13935 end
        super.onCreate(icicle);
        // Aurora xuyong 2015-06-30 deleted for bug #13935 start
        //requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        // Aurora xuyong 2015-06-30 deleted for bug #13935 end
        Intent it = getIntent();
        currentSlotId = it.getIntExtra("SlotId", 0);
        Log.i(TAG, "Got slot id is : " + currentSlotId);
        if (currentSlotId == GnPhone.GEMINI_SIM_1) {
            ICC_URI = Uri.parse("content://sms/icc");
        } else if (currentSlotId == GnPhone.GEMINI_SIM_2) {
            ICC_URI = Uri.parse("content://sms/icc2");
        }
        setAuroraContentView(R.layout.aurora_sim_list, AuroraActionBar.Type.Normal);
        
        mSimMessages = this;
        mContentResolver = getContentResolver();
        mQueryHandler = new QueryHandler(mContentResolver, this);

         // Aurora liugj 2013-10-11 modified for aurora's new feature start
        mSimList = (AuroraListView) findViewById(R.id.sim_list);
         // Aurora liugj 2013-10-11 modified for aurora's new feature end
        mMessage = (TextView) findViewById(R.id.empty_message);
        
        // Aurora liugj 2013-09-30 modified for aurora's new feature start
        mActionBar = getAuroraActionBar();
        mActionBar.setTitle(R.string.sim_manage_messages_title);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        // Aurora liugj 2013-10-22 modified for aurora's new feature start
        //addAuroraActionBarItem(AuroraActionBarItem.Type.Edit, ACTION_BATCH);
        // Aurora liugj 2013-10-22 modified for aurora's new feature end
        //mActionBar.setOnAuroraActionBarListener(mActionBarItemClickListener);
        // Aurora liugj 2013-09-30 modified for aurora's new feature end
        /*if (MmsApp.mDarkStyle) {
            mMessage.setTextColor(getResources().getColor(R.color.gn_dark_color_bg));
        }*/
        ITelephony iTelephony = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
        if(MmsApp.mGnMultiSimMessage &&  //Gionee guoyx 20130111 CR00754375
           !GnTelephonyManager.hasIccCardGemini(currentSlotId)){
            mSimList.setVisibility(View.GONE);
            if (currentSlotId == GnPhone.GEMINI_SIM_1) {
                mMessage.setText(R.string.no_sim_1);
            } else if (currentSlotId == GnPhone.GEMINI_SIM_2) {
                mMessage.setText(R.string.no_sim_2);
            }
            mMessage.setVisibility(View.VISIBLE);
            //setTitle(getString(R.string.sim_manage_messages_title));
            //setProgressBarIndeterminateVisibility(false);
        }else if(MmsApp.mGnMultiSimMessage){//Gionee guoyx 20130111 CR00754375
            try{
                boolean mIsSim1Ready = false;
                if (null != iTelephony) {
                    mIsSim1Ready = GnITelephony.isRadioOnGemini(iTelephony, currentSlotId);
                } else {
                    Log.e(TAG, "Can not get phone service !");
                }
                //Gionee qiuxd 2012-5-5 modify for start
                //if(mIsSim1Ready){
                boolean simPINReq = (TelephonyManager.SIM_STATE_PIN_REQUIRED 
                        == GnTelephonyManager.getSimStateGemini(currentSlotId));
                if(simPINReq){
                    mSimList.setVisibility(View.GONE);
                    //mMessage.setText(com.mediatek.R.string.sim_close);
                    mMessage.setText(getString(R.string.gn_input_pin_to_unLock));
                //Gioene qiuxd 2012-5-5 modify for end
                    mMessage.setVisibility(View.VISIBLE);
                    //setTitle(getString(R.string.sim_manage_messages_title));
                    //setProgressBarIndeterminateVisibility(false);
                }else{
                    isInit = true;
                }
            }catch(Exception e){
                Log.e(TAG, "RemoteException happens......");
            }
        }else{
            isInit = true;
        }
        
        //gionee wangym 2012-11-22 add for CR00735223 start
        /*if(MmsApp.mIsTouchModeSupport ){
            mIsCmcc = true;
            float size = MessageUtils.getTextSize(this);
            mTextSize = size;
            if(mListAdapter != null){
                int size_int = (int)size;
                mListAdapter.setTextSize(size_int);
            }
            mScaleDetector = new ScaleDetector(this, new ScaleListener());
        }*/
        //gionee wangym 2012-11-22 add for CR00735223 end
          // Aurora liugj 2013-12-17 added for SimChangeObserver bug start 
        registerSimChangeObserver();
          // Aurora liugj 2013-12-17 added for SimChangeObserver bug end
    }
    
    
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);

//        init();
    }

    private void init() {
        isInit = false;
        MessagingNotification.cancelNotification(getApplicationContext(),
                SIM_FULL_NOTIFICATION_ID);
        // Aurora liugj 2013-10-12 modified for aurora's new feature start
        showDialog(DIALOG_REFRESH);
        // Aurora liugj 2013-10-12 modified for aurora's new feature end
        startQuery();
    }

    private class QueryHandler extends AsyncQueryHandler {
        private final AuroraManageSimMessages mParent;

        public QueryHandler(
                ContentResolver contentResolver, AuroraManageSimMessages parent) {
            super(contentResolver);
            mParent = parent;
        }

        @Override
        protected void onQueryComplete(
                int token, Object cookie, Cursor cursor) {
            Log.d(TAG, "onQueryComplete");
              // Aurora liugj 2013-11-08 added for aurora's new feature start
            if(null != mActionBatchHandler) {
                mActionBatchHandler.leaveSelectionMode();
            }
              // Aurora liugj 2013-11-08 added for aurora's new feature end
            //Gionee <zhouyj> <2013-06-29> modify for CR00831538 begin
            //Gionee <zhouyj> <2013-06-29> modify for CR00831538 end
            if(observerCount > 0){
                AuroraManageSimMessages.this.startQuery();
                observerCount = 0;
                return;
            }else{
                isQuerying = false;
                    // Aurora liugj 2013-12-10 modified for bug-992 start
                removeDialog(DIALOG_REFRESH);
                if(isDeleting){
                    // Aurora liugj 2013-11-12 modified for bug-666 start 
                    Toast.makeText(AuroraManageSimMessages.this, getString(R.string.sms_has_deleted), Toast.LENGTH_SHORT).show();
                    // Aurora liugj 2013-11-12 modified for bug-666 end
                    isDeleting = false;
                }
                // Aurora liugj 2013-12-10 modified for bug-992 end
            }
            if (mCursor != null && !mCursor.isClosed()) {
                stopManagingCursor(mCursor);
            }
            mCursor = cursor;
            if (mCursor != null) {
                if (!mCursor.moveToFirst()) {
                    // Let user know the SIM is empty
                    updateState(SHOW_EMPTY);
                } else if (mListAdapter == null) {
                    // Note that the MessageListAdapter doesn't support auto-requeries. If we
                    // want to respond to changes we'd need to add a line like:
                    //   mListAdapter.setOnDataSetChangedListener(mDataSetChangedListener);
                    // See ComposeMessageActivity for an example.
                        // Aurora liugj 2013-12-10 modified for checkbox animation start
                    mListAdapter = new SimMessageListAdapter(mParent, mCursor);
                        // Aurora liugj 2013-12-10 modified for checkbox animation end
                    mSimList.setAdapter(mListAdapter);
                    // Aurora liugj 2013-10-11 modified for aurora's new feature start
                    mSimList.auroraSetNeedSlideDelete(true);
                    // Aurora liugj 2014-01-09 modified for allcheck animation start
                // Aurora xuyong 2014-04-17 deleted for bug #4318 start
                    //mSimList.setOnScrollListener(new MyScrollListener(220, "SimList_Scroll_Tread"));
                // Aurora xuyong 2014-04-17 deleted for bug #4318 end
                    // Aurora liugj 2014-01-09 modified for allcheck animation end
                    mSimList.auroraSetAuroraBackOnClickListener(new AuroraListView.AuroraBackOnClickListener() {
                        
                        @Override
                        public void auroraOnClick(int position) {
                            Cursor pcursor  = (Cursor) mSimList.getItemAtPosition(position);
                            if (pcursor == null) {
                                return;
                            } 
                             // Aurora liugj 2014-01-22 added for listview delete animation start
                            mSimList.auroraDeleteSelectedItemAnim();
                             // Aurora liugj 2014-01-22 added for listview delete animation end
                            final String msgIndex = getMsgIndexByCursor(pcursor);
                            new Thread(new Runnable() {
                                public void run() {
                                    deleteFromSim(msgIndex);
                                }
                            }, "AuroraManageSimMessages").start();
                        }
                        
                        @Override
                        public void auroraPrepareDraged(int position) {
                            
                        }

                        @Override
                        public void auroraDragedSuccess(int position) {
                            
                        }
                    
                        @Override
                        public void auroraDragedUnSuccess(int position) {
                            
                        }
                    });
                    // Aurora liugj 2013-10-11 modified for aurora's new feature end
                    mSimList.setOnItemClickListener(mParent);
                        // Aurora liugj 2013-09-30 modified for aurora's new feature start
                    mSimList.setOnItemLongClickListener(mParent);
                    mSimList.setOnKeyListener(mParent);
//                    mSimList.setOnCreateContextMenuListener(mParent);
                    mSimList.setRecyclerListener(mListAdapter);
                    // Aurora liugj 2013-09-30 modified for aurora's new feature end
                    updateState(SHOW_LIST);
                } else {
                    mListAdapter.changeCursor(mCursor);
                    updateState(SHOW_LIST);
                }
                startManagingCursor(mCursor);
                    // Aurora liugj 2013-12-17 deleted for SimChangeObserver bug start 
                //registerSimChangeObserver();
                    // Aurora liugj 2013-12-17 deleted for SimChangeObserver bug end
                //gionee gaoj 2012-10-12 added for CR00711168 start
                if (MmsApp.mIsSafeModeSupport) {
                    // Let user know the SIM is empty
                    updateState(SHOW_EMPTY);
                // gionee zhouyj 2012-11-28 add for CR00736900 start 
                } else {
                    invalidateOptionsMenu();
                // gionee zhouyj 2012-11-28 add for CR00736900 end 
                }
                //gionee gaoj 2012-10-12 added for CR00711168 end
            } else {
                // Let user know the SIM is empty
                //Gionee guoyx 20130325 added for CR00788903 begin
                if (mListAdapter != null) {
                    mListAdapter.changeCursor(null);
                }
                //Gionee guoyx 20130325 added for CR00788903 end
                updateState(SHOW_EMPTY);
            }
        }
    }


    @Override 
    protected Dialog onCreateDialog(int id){
        switch(id){
            case DIALOG_REFRESH: {
                if (dialog != null && dialog.getContext()!= this){
                    removeDialog(DIALOG_REFRESH);
                    Log.d(TAG, "onCreateDialog dialog is not null");
                }
                dialog = new AuroraProgressDialog(this);
                dialog.setIndeterminate(true);
                dialog.setCancelable(false);
                dialog.setMessage(getString(R.string.refreshing));
                return dialog;
            }
        }
        return null;
    }

    private void startQuery() {
        Log.d(TAG, "startQuery");                            
        // gionee zhouyj 2012-09-12 add for CR00679093 start
        if(MmsApp.mGnMessageSupport && isFinishing())
            return ;
        // gionee zhouyj 2012-09-12 add for CR00679093 end
        //Gionee <zhouyj> <2013-06-29> modify for CR00831538 begin
          // Aurora liugj 2013-12-10 modified for bug-992 start
        //showDialog(DIALOG_REFRESH);
          // Aurora liugj 2013-12-10 modified for bug-992 end
        //Gionee <zhouyj> <2013-06-29> modify for CR00831538 end

        try {
            isQuerying = true;
            mQueryHandler.startQuery(0, null, ICC_URI, null, null, null, null);
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(this, e);
        }
    }

    private void refreshMessageList() {
         // Aurora liugj 2013-10-12 modified for aurora's new feature start
        // Aurora liugj 2013-12-10 modified for bug-992 start
        if (!isDeleting) {
            showDialog(DIALOG_REFRESH);
        }
        // Aurora liugj 2013-12-10 modified for bug-992 end
        // Aurora liugj 2013-10-12 modified for aurora's new feature end
        if (mCursor != null) {
            stopManagingCursor(mCursor);
            // mCursor.close();
        }
        startQuery();
    }
    
    // Aurora liugj 2013-09-30 deleted for aurora's new feature start
    /*@Override
    public void onCreateContextMenu(
        ContextMenu menu, View v,
        ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle(R.string.message_options);
        //MTK_OP02_PROTECT_START
        if (MmsApp.isUnicomOperator()) {
            AdapterView.AdapterContextMenuInfo info = null;
            try {
                 info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            } catch (ClassCastException exception) {
                Log.e(TAG, "Bad menuInfo.", exception);
            }
            final Cursor cursor = (Cursor) mListAdapter.getItem(info.position);
//            addCallAndContactMenuItems(menu, cursor);
            addRecipientToContact(menu, cursor);
            menu.add(0, MENU_FORWARD, 0, R.string.menu_forward);
            //Gionee <zhouyj> <2013-05-27> modify for CR00813804 begin
            if (!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndexOrThrow("address")))) {
                menu.add(0, MENU_REPLY, 0, R.string.menu_reply);
            }
            //Gionee <zhouyj> <2013-05-27> modify for CR00813804 end
        }
        //MTK_OP02_PROTECT_END
        menu.add(0, MENU_COPY_TO_PHONE_MEMORY, 0,
                R.string.sim_copy_to_phone_memory);
        menu.add(0, MENU_DELETE_FROM_SIM, 0, R.string.sim_delete);
    }*/
    // Aurora liugj 2013-09-30 deleted for aurora's new feature end
    
    // Aurora liugj 2013-09-30 deleted for aurora's new feature start
    /*@Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException exception) {
            Log.e(TAG, "Bad menuInfo.", exception);
            return false;
        }

        final Cursor cursor = (Cursor) mListAdapter.getItem(info.position);
        if(cursor == null){
            Log.e(TAG, "Bad menuInfo, cursor is null");
            return false;
        }
        switch (item.getItemId()) {
            case MENU_COPY_TO_PHONE_MEMORY:
                copyToPhoneMemory(cursor);
                return true;
            case MENU_DELETE_FROM_SIM:
                final String msgIndex = getMsgIndexByCursor(cursor);
                confirmDeleteDialog(new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Aurora liugj 2013-10-12 modified for aurora's new feature start
                             showDialog(DIALOG_REFRESH);
                            // Aurora liugj 2013-10-12 modified for aurora's new feature end
                        new Thread(new Runnable() {
                            public void run() {
                                deleteFromSim(msgIndex);
                            }
                        }, "AuroraManageSimMessages").start();
                        dialog.dismiss();
                    }
                }, R.string.confirm_delete_SIM_message);
                return true;
            //MTK_OP02_PROTECT_START
            case MENU_FORWARD:
                forwardMessage(cursor);
                return true;
            case MENU_REPLY:
                replyMessage(cursor);
                return true;
            case MENU_ADD_TO_BOOKMARK:{
                if (mURLs.size() == 1) {
                    Browser.saveBookmark(AuroraManageSimMessages.this, null, mURLs.get(0));
                } else if(mURLs.size() > 1) {
                    CharSequence[] items = new CharSequence[mURLs.size()];
                    for (int i = 0; i < mURLs.size(); i++) {
                        items[i] = mURLs.get(i);
                    }
                    new AuroraAlertDialog.Builder(AuroraManageSimMessages.this)
                        .setTitle(R.string.menu_add_to_bookmark)
                        .setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Browser.saveBookmark(AuroraManageSimMessages.this, null, mURLs.get(which));
                                }
                            })
                        .show();
                }
                return true;
             }
            case MENU_ADD_CONTACT:
                String number = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                addToContact(number);
                return true;
            //MTK_OP02_PROTECT_END
        }
        return super.onContextItemSelected(item);
    }*/
    // Aurora liugj 2013-09-30 deleted for aurora's new feature end

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");                                        
        super.onResume();

        /* If recreate this activity, the dialog and cursor will be restore in method
         * onRestoreInstanceState() which will be invoked between onStart and onResume.
         * Note: The dialog showed before onPause() will be recreated 
         *      (Refer to AuroraActivity.onSaveInstanceState() and onRestoreInstanceState()).
         * So, we should initialize in onResume method when it is first time enter this
         * activity, if need.*/
        if (isInit) {
            init();
        }
          // Aurora liugj 2013-12-17 deleted for SimChangeObserver bug start 
        //registerSimChangeObserver();
          // Aurora liugj 2013-12-17 deleted for SimChangeObserver bug end
        if (isDeleting) {
            // This means app is deleting SIM SMS when left activity last time
            refreshMessageList();
        }
        // gionee zhouyj 2012-11-14 add for CR00729273 start 
            // Aurora liugj 2013-09-30 deleted for aurora's new feature start
        /*if (MmsApp.mGnMessageSupport) {
            updateState(MmsApp.mIsSafeModeSupport ? SHOW_EMPTY : SHOW_LIST);
        }*/
            // Aurora liugj 2013-09-30 deleted for aurora's new feature end
        // gionee zhouyj 2012-11-14 add for CR00729273 end 
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");                                            
        super.onPause();
        // Aurora liugj 2013-09-30 deleted for fix bug-179 start
        mSimList.auroraOnPause();
        // Aurora liugj 2013-09-30 deleted for fix bug-179 end
        //invalidate cache to refresh contact data
        Contact.invalidateCache();        
        //mContentResolver.unregisterContentObserver(simChangeObserver);
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");                                            
        super.onStop();
        if (dialog != null) {
            removeDialog(DIALOG_REFRESH);
        }
        //Gionee guoyx 20130325 added for CR00788903 begin
        /*if (!isDeleting) {
            isInit = true;
        }*/
        //Gionee guoyx 20130325 added for CR00788903 end
    }

    private void registerSimChangeObserver() {
        mContentResolver.registerContentObserver(
                ICC_URI, true, simChangeObserver);
    }
    
    // Aurora liugj 2013-09-30 modified for aurora's new feature start
    private void copyToPhoneMemory(Cursor cursor, boolean single) {
    // Aurora liugj 2013-09-30 modified for aurora's new feature end
         // Aurora liugj 2013-11-12 modified for aurora's new feature start
        /*String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
        String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
        Long date = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
        String serviceCenter = cursor.getString(cursor.getColumnIndexOrThrow("service_center_address"));*/
        // Aurora xuyong 2014-03-11 added for bug #3079 start
        if (cursor == null) {
            return;
        }
        // Aurora xuyong 2014-03-11 added for bug #3079 end
        String address = cursor.getString(INDEX_COLUMN_ADDRESS);
       // Aurora xuyong 2014-09-11 added for bug #8251 start
       // we need assign a new value to these result which has no address, here we assign 2 to them;
        if (address == null) {
            address = "2";
        }
       // Aurora xuyong 2014-09-11 added for bug #8251 end
        String body = cursor.getString(INDEX_COLUMN_BODY);
        Long date = cursor.getLong(INDEX_COLUMN_DATE);
        String serviceCenter = cursor.getString(INDEX_COLUMN_SCA);
         // Aurora liugj 2013-11-12 modified for aurora's new feature end
        if (DEBUG) {
            Log.d(MmsApp.TXN_TAG, "\t address \t=" + address);
            Log.d(MmsApp.TXN_TAG, "\t body \t=" + body);
            Log.d(MmsApp.TXN_TAG, "\t date \t=" + date);
            Log.d(MmsApp.TXN_TAG, "\t sc \t=" + serviceCenter);
        }
        //Gionee guoyx 20130225 add for CR00772795 begin
        Uri MsgUri = null;
        //Gionee guoyx 20130225 add for CR00772795 end

        // Gionee guoyx 20130225 modified for CR00772795 begin
        try {
            if (isIncomingMessage(cursor)) {
                Log.d(MmsApp.TXN_TAG, "Copy incoming sms to phone");
                if (MmsApp.mGnMultiSimMessage) {
                    SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(this,
                            currentSlotId);
                    if (simInfo != null) {
                        MsgUri = GnTelephony.Sms.Inbox
                                .addMessage(mContentResolver, address, body,
                                        null, serviceCenter, date, true,
                                        (int) simInfo.mSimId);
                    } else {
                        MsgUri = GnTelephony.Sms.Inbox.addMessage(
                                mContentResolver, address, body, null,
                                serviceCenter, date, true, -1);
                    }
                } else {
                    MsgUri = GnTelephony.Sms.Inbox.addMessage(mContentResolver,
                            address, body, null, serviceCenter, date, true);    //inui-Phone go
                }
            } else {
                // outgoing sms has not date info
                date = System.currentTimeMillis();
                Log.d(MmsApp.TXN_TAG, "Copy outgoing sms to phone");

                if (MmsApp.mGnMultiSimMessage) {
                    SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(this,
                            currentSlotId);
                    if (simInfo != null) {
                        MsgUri = GnTelephony.Sms.Sent.addMessage(
                                mContentResolver, address, body, null,
                                serviceCenter, date, (int) simInfo.mSimId);
                    } else {
                        MsgUri = GnTelephony.Sms.Sent.addMessage(
                                mContentResolver, address, body, null,
                                serviceCenter, date, -1);
                    }
                } else {
                    // Aurora xuyong 2014-03-14 modified for bug #3077 start
                    MsgUri = GnTelephony.Sms.Sent.addMessage(mContentResolver,
                            address, body, null, date);
                    // Aurora xuyong 2014-03-14 modified for bug #3077 end
                }
            }
            // Gionee guoyx 20130225 modified for CR00772795 end

            // gionee lwzh mofify for CR00705726 20121002 begin
            if(MmsApp.mGnMessageSupport) {
                // Aurora xuyong 2014-06-26 modified for bug #6172 start
                if (address != null) {
                    Long threadId = Threads.getOrCreateThreadId(getApplicationContext(), address);
                    Recycler.getSmsRecycler().deleteOldMessagesByThreadId(getApplicationContext(), threadId);
                }
                // Aurora xuyong 2014-06-26 modified for bug #6172 end
            } else {
            // gionee lwzh mofify for CR00705726 20121002 end
                Recycler.getSmsRecycler().deleteOldMessages(getApplicationContext());
            // gionee lwzh mofify for CR00705726 20121002 begin
            }
            // gionee lwzh mofify for CR00705726 20121002 end
            //Gionee guoyx 20130225 modified for CR00772795 begin
          // Aurora liugj 2013-10-29 added for aurora's new feature start
            // Aurora liugj 2013-09-30 modified for aurora's new feature start
            if (single) {
            // Aurora liugj 2013-09-30 modified for aurora's new feature end
                if (MsgUri != null) {
                    Log.d(MmsApp.TXN_TAG, "Copy to phone success. message uri:" + MsgUri.toString());
                    String msg = getString(R.string.copyto_phone_done);
                    Toast.makeText(AuroraManageSimMessages.this, msg, Toast.LENGTH_SHORT)
                            .show();
                }else {
                    String msg = getString(R.string.copyto_phone_fail);
                    Toast.makeText(AuroraManageSimMessages.this, msg, Toast.LENGTH_SHORT).show();
                }
                
            }
            // Aurora liugj 2013-10-29 added for aurora's new feature end
            //Gionee guoyx 20130225 modified for CR00772795 end
            
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(this, e);
        }
    }

    private boolean isIncomingMessage(Cursor cursor) {
          // Aurora liugj 2013-11-12 modified for aurora's new feature start
        /*int messageStatus = cursor.getInt(
                cursor.getColumnIndexOrThrow("status"));*/
         int messageStatus = cursor.getInt(INDEX_COLUMN_STATUS);
         // Aurora liugj 2013-11-12 modified for aurora's new feature end
        Log.d(MmsApp.TXN_TAG, "message status:" + messageStatus);
        return (messageStatus == SmsManager.STATUS_ON_ICC_READ) ||
               (messageStatus == SmsManager.STATUS_ON_ICC_UNREAD);
    }

    private String getMsgIndexByCursor(Cursor cursor) {
          // Aurora liugj 2013-11-12 modified for aurora's new feature start
        //return cursor.getString(cursor.getColumnIndexOrThrow("index_on_icc"));
          // Aurora xuyong 2014-03-11 modified for bug #3079 start
          if (cursor != null) {
              return cursor.getString(INDEX_COLUMN_ICC);
          } 
          return null;
          // Aurora xuyong 2014-03-11 modified for bug #3079 end
          // Aurora liugj 2013-11-12 modified for aurora's new feature end
    }

    private void deleteFromSim(String msgIndex) {
        /* 1. Non-Concatenated SMS's message index string is like "1"
         * 2. Concatenated SMS's message index string is like "1;2;3;".
         * 3. If a concatenated SMS only has one segment stored in SIM Card, its message 
         *    index string is like "1;".
         */
        // Aurora xuyong 2014-03-11 added for bug #3079 start
        if (msgIndex == null) {
            return;
        }
        // Aurora xuyong 2014-03-11 added for bug #3079 end
        String[] index = msgIndex.split(";");
        Uri simUri = ICC_URI.buildUpon().build();
        if (SqliteWrapper.delete(this, mContentResolver, simUri, FOR_MULTIDELETE, index) == 1) {
            MessagingNotification.cancelNotification(getApplicationContext(),
                    SIM_FULL_NOTIFICATION_ID);
        }
        isDeleting = true;
    }

    public void deleteFromSim(Cursor cursor) {
        String msgIndex = getMsgIndexByCursor(cursor);
        // Aurora xuyong 2014-03-11 modified for bug #3079 start
        if (msgIndex != null) {
            deleteFromSim(msgIndex);
        }
        // Aurora xuyong 2014-03-11 modified for bug #3079 end
    }

    private void deleteAllFromSim() {
        // For Delete all,MTK FW support delete all using messageIndex = -1, here use 999999 instead of -1;
        String messageIndexString = ALL_SMS;
        //cursor.getString(cursor.getColumnIndexOrThrow("index_on_icc"));
        Uri simUri = ICC_URI.buildUpon().appendPath(messageIndexString).build();
        Log.i(TAG, "delete simUri: " + simUri);
        if (SqliteWrapper.delete(this, mContentResolver, simUri, null, null) == 1) {
            MessagingNotification.cancelNotification(getApplicationContext(),
                    SIM_FULL_NOTIFICATION_ID);
        }
        isDeleting = true;
    }
    
    // Aurora liugj 2013-09-17 modified for aurora's new feature start
    /*@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        MenuItem miDeleteAll = menu.add(0, OPTION_MENU_DELETE_ALL, 0, R.string.menu_delete_messages);

        miDeleteAll.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        if ((null != mCursor) && (mCursor.getCount() > 0) && mState == SHOW_LIST) {
            miDeleteAll.setEnabled(true);
            miDeleteAll.setIcon(R.drawable.gn_com_delete_bg);
        } else {
            miDeleteAll.setEnabled(false);
            miDeleteAll.setIcon(R.drawable.gn_com_delete_unuse_bg);
        }
        
        //MTK_OP01_PROTECT_START
        if (MmsApp.isTelecomOperator()) {
            MenuItem miSimCapacity = menu.add(0, OPTION_MENU_SIM_CAPACITY, 0, R.string.menu_show_icc_sms_capacity).setIcon(
                        R.drawable.ic_menu_sim_capacity);
            miDeleteAll.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            
            if (mState == SHOW_LIST || mState == SHOW_EMPTY) {
                miSimCapacity.setEnabled(true);
            } else {
                miSimCapacity.setEnabled(false);
            }
        }
        //MTK_OP01_PROTECT_END

        super.onPrepareOptionsMenu(menu);

        return true;
    }*/

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        //gionee gaoj added for CR00725602 20121201 start
        case android.R.id.home:
            finish();
            break;
        //gionee gaoj added for CR00725602 20121201 end
            case OPTION_MENU_DELETE_ALL:
                confirmDeleteDialog(new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Aurora liugj 2013-10-12 modified for aurora's new feature start
                             showDialog(DIALOG_REFRESH);
                             // Aurora liugj 2013-10-12 modified for aurora's new feature end
                        //deleteAllFromSim();
                        new Thread(new Runnable() {
                            public void run() {
                                deleteAllFromSim();
                            }
                        }, "ManageSimMessages").start();
                        dialog.dismiss();
                    }
                }, R.string.confirm_delete_all_SIM_messages);
                break;
            //MTK_OP01_PROTECT_START
            case OPTION_MENU_SIM_CAPACITY:
//                GnSmsMemoryStatus SimMemStatus = null;
//                if (FeatureOption.MTK_GEMINI_SUPPORT) {
//                    SimMemStatus = GnGeminiSmsManager.getSmsSimMemoryStatusGemini(currentSlotId);
//                } else {
//                    SimMemStatus = GnSmsManager.getSmsSimMemoryStatus();
//                }
                int simUsed = GnSmsMemoryStatus.getUsed(currentSlotId);
                int simTotal = GnSmsMemoryStatus.getTotal(currentSlotId);

                String message = null;
                if (simUsed != -1 null != SimMemStatus) {
                    message = getString(R.string.icc_sms_used) + Integer.toString(simUsed)
                                + "\n" + getString(R.string.icc_sms_total) + Integer.toString(simTotal);
                } else {
                    message = getString(R.string.get_icc_sms_capacity_failed);
                }
                new AuroraAlertDialog.Builder(ManageSimMessages.this)
                            .setIconAttribute(android.R.attr.alertDialogIcon)
                            .setTitle(R.string.show_icc_sms_capacity_title)
                            .setMessage(message)
                            .setPositiveButton(android.R.string.ok, null)
                            .setCancelable(true)
                            .show();
                break;
            //MTK_OP01_PROTECT_END
        }

        return super.onOptionsItemSelected(item);
    }*/
    // Aurora liugj 2013-09-17 modified for aurora's new feature end
    
    private void confirmDeleteDialog(OnClickListener listener, int messageId) {
        AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this/*, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN*/);
        builder.setTitle(R.string.confirm_dialog_title);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.yes, listener);
        builder.setNegativeButton(R.string.no, null);
        builder.setMessage(messageId);

        builder.show();
    }

    private void updateState(int state) {
        Log.d(TAG, "updateState, state = "+ state);            
        if (mState == state) {
            return;
        }

        mState = state;
        switch (state) {
            case SHOW_LIST:
                   mMessage.setVisibility(View.GONE);
                mSimList.setVisibility(View.VISIBLE);
                    // Aurora liugj 2013-10-09 modified for aurora's new feature start
                //mSimList.requestFocus();
                //mSimList.setSelection(mSimList.getCount()-1);
                    // Aurora liugj 2013-10-09 modified for aurora's new feature end
                    // Aurora liugj 2013-12-10 modified for bug-992 start
                if(mActionBar.getItem(ACTION_BATCH) == null) {
                    addAuroraActionBarItem(AuroraActionBarItem.Type.Edit, ACTION_BATCH);
                     // Aurora liugj 2014-01-21 modified for aurora's new feature start
                    mActionBar.setOnAuroraActionBarListener(mActionBarItemClickListener);
                     // Aurora liugj 2014-01-21 modified for aurora's new feature end
                }
                //setTitle(getString(R.string.sim_manage_messages_title));
                //setProgressBarIndeterminateVisibility(false);
                    // Aurora liugj 2013-12-10 modified for bug-992 end
                break;
            case SHOW_EMPTY:
                mSimList.setVisibility(View.GONE);
                    // Aurora liugj 2013-12-10 modified for bug-992 start
                //mActionBar.setDisplayOptions(ACTION_BATCH, false);
                if(mActionBar.getItem(ACTION_BATCH) != null) {
                    mActionBar.removeItem(ACTION_BATCH);
                    // Aurora liugj 2014-01-21 modified for aurora's new feature start
                    mActionBar.setOnAuroraActionBarListener(null);
                    // Aurora liugj 2014-01-21 modified for aurora's new feature end
                }
                mMessage.setVisibility(View.VISIBLE);
                //setTitle(getString(R.string.sim_manage_messages_title));
                //setProgressBarIndeterminateVisibility(false);
                    // Aurora liugj 2013-12-10 modified for bug-992 end
                break;
            case SHOW_BUSY:
                mSimList.setVisibility(View.GONE);
                mMessage.setVisibility(View.GONE);
                // Aurora liugj 2013-12-10 modified for bug-992 start
                showDialog(DIALOG_REFRESH);
                /*setTitle(getString(R.string.refreshing));
                setProgressBarIndeterminateVisibility(true);*/
                // Aurora liugj 2013-12-10 modified for bug-992 end
                break;
            default:
                Log.e(TAG, "Invalid State");
        }
    }

    private void viewMessage(Cursor cursor) {
        // TODO: Add this.
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode)
    {
        if (null != intent && null != intent.getData()
                && intent.getData().getScheme().equals("mailto")) {
            try {
                super.startActivityForResult(intent, requestCode);
            } catch (ActivityNotFoundException e) {
                Log.w(TAG, "Failed to startActivityForResult: " + intent);
                Intent i = new Intent().setClassName("com.android.email", "com.android.email.activity.setup.AccountSetupBasics");
                this.startActivity(i);
                finish();
            } catch (Exception e) {
                Log.e(TAG, "Failed to startActivityForResult: " + intent);
                Toast.makeText(this,getString(R.string.message_open_email_fail),
                      Toast.LENGTH_SHORT).show();
          }
        } else {
            super.startActivityForResult(intent, requestCode);
        }
    }
    
    //MTK_OP02_PROTECT_START
    private void forwardMessage(Cursor cursor) {
        //String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
          String body = cursor.getString(INDEX_COLUMN_BODY);
        Intent intent = new Intent();
        intent.setClassName(this, "com.android.mms.ui.ForwardMessageActivity");
        intent.putExtra(ComposeMessageActivity.FORWARD_MESSAGE, true);
        if (body != null) {
            intent.putExtra(ComposeMessageActivity.SMS_BODY, body);
        }
        
        startActivity(intent);
    }
    
    private void replyMessage(Cursor cursor) {
        //String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
          String address = cursor.getString(INDEX_COLUMN_ADDRESS);
        //Gionee <zhouyj> <2013-05-18> add for CR00813804 begin
        if (TextUtils.isEmpty(address)) {
            Toast.makeText(this, getString(R.string.gn_unknown_sender), Toast.LENGTH_SHORT).show();
            return ;
        }
        //Gionee <zhouyj> <2013-05-18> add for CR00813804 end
        Intent intent = new Intent(Intent.ACTION_SENDTO,
                Uri.fromParts("sms", address, null));
        startActivity(intent);
    }

    private final void addCallAndContactMenuItems(ContextMenu menu, Cursor cursor) {
        // Add all possible links in the address & message
        StringBuilder textToSpannify = new StringBuilder();  
        //String reciBody = cursor.getString(cursor.getColumnIndexOrThrow("body"));
        //String reciNumber = cursor.getString(cursor.getColumnIndexOrThrow("address"));
          String reciBody = cursor.getString(INDEX_COLUMN_BODY);
        String reciNumber = cursor.getString(INDEX_COLUMN_ADDRESS);
        textToSpannify.append(reciNumber + ": ");
        textToSpannify.append(reciBody);
        SpannableString msg = new SpannableString(textToSpannify.toString());
        Linkify.addLinks(msg, Linkify.ALL);
        ArrayList<String> uris =
            MessageUtils.extractUris(msg.getSpans(0, msg.length(), URLSpan.class));
        mURLs.clear();
        Log.d(TAG, "addCallAndContactMenuItems uris.size() = " + uris.size());
        while (uris.size() > 0) {
            String uriString = uris.remove(0);
            // Remove any dupes so they don't get added to the menu multiple times
            while (uris.contains(uriString)) {
                uris.remove(uriString);
            }

            int sep = uriString.indexOf(":");
            String prefix = null;
            if (sep >= 0) {
                prefix = uriString.substring(0, sep);
                if ("mailto".equalsIgnoreCase(prefix) || "tel".equalsIgnoreCase(prefix)){
                    uriString = uriString.substring(sep + 1);
                }
            }
            boolean addToContacts = false;
            if ("mailto".equalsIgnoreCase(prefix)) {
                String sendEmailString = getString(R.string.menu_send_email).replace("%s", uriString);
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("mailto:" + uriString));
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
//                menu.add(0, MENU_SEND_EMAIL, 0, sendEmailString).setIntent(intent);
                addToContacts = !haveEmailContact(uriString);
            } else if ("tel".equalsIgnoreCase(prefix)) {
                
                if (reciBody != null && reciBody.replaceAll("\\-", "").contains(uriString)) {
                    String sendSmsString = getString(
                        R.string.menu_send_sms).replace("%s", uriString);
                    Intent intentSms = new Intent(Intent.ACTION_SENDTO,
                        Uri.parse("smsto:" + uriString));
                    intentSms.setClassName(AuroraManageSimMessages.this, "com.android.mms.ui.SendMessageToActivity");
                    intentSms.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    menu.add(0, MENU_SEND_SMS, 0, sendSmsString).setIntent(intentSms);
                }
                addToContacts = !isNumberInContacts(uriString);
            } else {
                //add URL to book mark
                if (mURLs.size() <= 0) {
                    menu.add(0, MENU_ADD_TO_BOOKMARK, 0, R.string.menu_add_to_bookmark);
                }
                mURLs.add(uriString);
            }
            if (addToContacts) {
                //Intent intent = ConversationList.createAddContactIntent(uriString);
                Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
                
                //gionee gaoj 2013-4-2 added for CR00792780 start
                intent.setComponent(new ComponentName("com.android.contacts",
                "com.android.contacts.activities.ContactEditorActivity"));
                //gionee gaoj 2013-4-2 added for CR00792780 end
                
                //gionee gaoj 2012-6-26 added for CR00627832 start
                if (Mms.isEmailAddress(uriString)) {
                    intent.putExtra(ContactsContract.Intents.Insert.EMAIL, uriString);
                } else {
                    intent.putExtra(ContactsContract.Intents.Insert.PHONE, uriString);
                }
                //gionee gaoj 2012-6-26 added for CR00627832 end
                String addContactString = getString(
                        R.string.menu_add_address_to_contacts).replace("%s", uriString);
                menu.add(0, MENU_ADD_ADDRESS_TO_CONTACTS, 0, addContactString)
                    .setIntent(intent);
            }
        }
    }
    
    private boolean addRecipientToContact(ContextMenu menu, Cursor cursor){
         boolean showAddContact = false;
         //String reciNumber = cursor.getString(cursor.getColumnIndexOrThrow("address"));
         String reciNumber = cursor.getString(INDEX_COLUMN_ADDRESS);
         Log.d(TAG, "addRecipientToContact reciNumber = " + reciNumber);
         // if there is at least one number not exist in contact db, should show add.
         mContactList = ContactList.getByNumbers(reciNumber, false, true);
         for (Contact contact : mContactList) {
             if (!contact.existsInDatabase()) {
                 showAddContact = true;
                 Log.d(TAG, "not in contact[number:" + contact.getNumber() + ",name:" + contact.getName());
                 break;
             }
         }
         boolean menuAddExist = (menu.findItem(MENU_ADD_CONTACT)!= null);
        /* if (showAddContact) {
             //Gionee <zhouyj> <2013-05-27> modify for CR00813804 begin
             if (!menuAddExist && !TextUtils.isEmpty(reciNumber)) {
             //Gionee <zhouyj> <2013-05-27> modify for CR00813804 end
                 menu.add(0, MENU_ADD_CONTACT, 1, R.string.menu_add_to_contacts).setIcon(R.drawable.ic_menu_contact);
             }
         } else {
             menu.removeItem(MENU_ADD_CONTACT);
         }*/
         return true;
    }
    private boolean isNumberInContacts(String phoneNumber) {
        return Contact.get(phoneNumber, false).existsInDatabase();
    }
    
    private boolean haveEmailContact(String emailAddress) {
        Cursor cursor = SqliteWrapper.query(this, getContentResolver(),
                Uri.withAppendedPath(Email.CONTENT_LOOKUP_URI, Uri.encode(emailAddress)),
                new String[] { Contacts.DISPLAY_NAME }, null, null, null);

        if (cursor != null) {
            try {
                String name;
                while (cursor.moveToNext()) {
                    name = cursor.getString(0);
                    if (!TextUtils.isEmpty(name)) {
                        return true;
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return false;
    }
    
    private void addToContact(String reciNumber){
        int count = mContactList.size();
        switch(count) {
        case 0:
            Log.e(TAG, "add contact, mCount == 0!");
            break;
        case 1:
            MessageUtils.addNumberOrEmailtoContact(reciNumber, 0, this);
            break;
        default:
            break;
        }
    }
    //MTK_OP02_PROTECT_END

    //gionee wangym 2012-11-22 add for CR00735223 start
    @Override
    public boolean  dispatchTouchEvent(MotionEvent event){
        
        boolean ret = false;

//MTK_OP01_PROTECT_START
       /* if(mIsCmcc && mScaleDetector != null){
                ret = mScaleDetector.onTouchEvent(event);
        }*/
//MTK_OP01_PROTECT_END
        
        if(!ret){
            ret = super.dispatchTouchEvent(event); 
        }
        return ret;
    }
    
//MTK_OP01_PROTECT_START
    
    private final int DEFAULT_TEXT_SIZE = 20;
    private final int MIN_TEXT_SIZE = 10;
    private final int MAX_TEXT_SIZE = 32;
//    private ScaleDetector mScaleDetector;
    private float mTextSize = DEFAULT_TEXT_SIZE;
    private float MIN_ADJUST_TEXT_SIZE = 0.2f;
    private boolean mIsCmcc = false;    
    
    // add for cmcc changTextSize by multiTouch
    /*private void changeTextSize(float size){
        if(mListAdapter != null){
            mListAdapter.setTextSize(size);
        }
        
        if(mSimList != null && mSimList.getVisibility() == View.VISIBLE){
            int count = mSimList.getChildCount();
            for(int i = 0; i < count; i++){
                MessageListItem item =  (MessageListItem)mSimList.getChildAt(i);
                if(item != null){
                    item.setBodyTextSize(size);
                }
            }
        }
    }*/    
    
    /*public class ScaleListener implements OnScaleListener{
        
        public boolean onScaleStart(ScaleDetector detector) {
            Log.i(TAG, "onScaleStart -> mTextSize = " + mTextSize);
            return true;
        }
        
        public void onScaleEnd(ScaleDetector detector) {
            Log.i(TAG, "onScaleEnd -> mTextSize = " + mTextSize);
            
            //save current value to preference
            MessageUtils.setTextSize(AuroraManageSimMessages.this, mTextSize);
        }
        
        public boolean onScale(ScaleDetector detector) {

            float size = mTextSize * detector.getScaleFactor();
            
            if(Math.abs(size - mTextSize) < MIN_ADJUST_TEXT_SIZE){
                return false;
            }            
            if(size < MIN_TEXT_SIZE){
                size = MIN_TEXT_SIZE;
            }            
            if(size > MAX_TEXT_SIZE){
                size = MAX_TEXT_SIZE;
            }            
            if(size != mTextSize){
                changeTextSize(size);
                mTextSize = size;
            }
            return true;
        }
    }*/


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
            long id) {
        if(null == mActionBatchHandler) {
            initThreadsMap();
            initActionBatchMode();
            mActionBatchHandler.enterSelectionMode(false, position);
            return true;
        }
        return false;
    }
    
    private void initThreadsMap() {
        // TODO Auto-generated method stub
        // Aurora liugj 2013-11-21 modified for NULL start
        if (mListAdapter == null || mListAdapter.getCount() == 0) {
            return;
        }
        // Aurora liugj 2013-11-21 modified for NULL end
        Cursor cursor = mListAdapter.getCursor();
        cursor.moveToPosition(-1);
        int i = 0;
        while(cursor.moveToNext()) {
            if (mThreadsMap.get(i) == null) {
                mThreadsMap.put(i++, i - 1);
            }
        }
    }
    
    private void initActionBatchMode() {
        // Aurora xuyong 2016-01-14 modified for aurora 2.0 new feature start
        setAuroraBottomBarMenuCallBack(mAuroraMenuCallBack); // 必须写在布局前
        // Aurora xuyong 2016-01-14 modified for aurora 2.0 new feature end
        mActionBar.initActionBottomBarMenu(R.menu.aurora_sim_list_menu, 2);
        // Aurora xuyong 2014-03-21 modified for aurora's new feature start
        mActionBar.showActionBarDashBoard();
        // Aurora xuyong 2014-03-21 modified for aurora's new feature end
        mAuroraMenu = mActionBar.getActionBarMenu();
        mBottomAuroraMenu = mActionBar.getAuroraActionBottomBarMenu();
        initActionBatchHandler();
        mAuroraMenu.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                //Log.e("liugj", "------onDismiss-----");
                // Aurora liugj 2013-10-11 modified for aurora's new feature start
                /*mSimList.auroraSetNeedSlideDelete(true);
                // Aurora liugj 2013-10-11 modified for aurora's new feature end
                // Aurora liugj 2013-11-18 added for aurora's new feature start
                mSimList.auroraEnableSelector(true);    
                // Aurora liugj 2013-11-18 added for aurora's new feature end
                mListAdapter.showCheckBox(false);
                mListAdapter.setCheckBoxAnim(false);
                // Aurora liugj 2014-01-07 modified for allcheck animation start
                mListAdapter.updateAllCheckBox(0);
                // Aurora liugj 2014-01-07 modified for allcheck animation end
                mActionBatchHandler.destroyAction();
                mListAdapter.notifyDataSetChanged();
                AuroraManageSimMessages.this.setMenuEnable(true);

                mActionBatchHandler = null;
//                if (null != mDelAllDialog && !mDelAllDialog.isShowing()) {
//                    mListChange = false;
//                }
                mThreadsMap.clear();*/
                onBottomMenuDismiss();
            }
        });
    }
    
    protected void initActionBatchHandler() {
        // Aurora xuyong 2014-03-21 modified for aurora's new feature start
        mActionBatchHandler = new AuroraActionBatchHandler<Integer>(this, mActionBar) {
        // Aurora xuyong 2014-03-21 modified for aurora's new feature end
            
            @Override
            public void enterSelectionMode(boolean autoLeave, Integer itemPressing) {
                // Aurora liugj 2013-10-11 modified for aurora's new feature start
                mSimList.auroraSetNeedSlideDelete(false);
                // Aurora liugj 2013-10-11 modified for aurora's new feature end
                AuroraManageSimMessages.this.setMenuEnable(false);
                // Aurora liugj 2013-11-18 added for aurora's new feature start
                mSimList.auroraEnableSelector(false);
                // Aurora liugj 2013-11-18 added for aurora's new feature end
                mListAdapter.showCheckBox(true);
                mListAdapter.setCheckBoxAnim(true);
                // Aurora liugj 2013-12-10 deleted for checkbox animation start
                //mFirstInMultiMode = true;
                // Aurora liugj 2013-12-10 deleted for checkbox animation end
                super.enterSelectionMode(autoLeave, itemPressing);
                // Aurora liugj 2013-12-11 added for checkbox animation start
                mListAdapter.notifyDataSetChanged();
                // Aurora liugj 2013-12-11 added for checkbox animation end
            }

            @Override
            public Set getDataSet() {
                // TODO Auto-generated method stub
                Set<Integer> dataSet = new HashSet<Integer>(mThreadsMap.size());
                for(int i = 0; i < mThreadsMap.size(); i++)
                    dataSet.add(mThreadsMap.get(i));
                return dataSet;
            }
            
            // Aurora liugj 2013-10-17 modified for aurora's new feature start
            @Override
            public void leaveSelectionMode() {
                if (mAuroraMenu != null) {
                    //mAuroraMenu.dismiss();
                    mActionBar.setShowBottomBarMenu(false);
                    // Aurora xuyong 2014-03-21 modified for aurora's new feature start
                    mActionBar.showActionBarDashBoard();
                    // Aurora xuyong 2014-03-21 modified for aurora's new feature end
                    onBottomMenuDismiss();
                }
                /*if (mBottomAuroraMenu != null) {
                    mBottomAuroraMenu.dismiss();
                    // Aurora liugj 2013-10-09 modified for aurora's new feature start
                    mActionBar.contentViewFloatDown();
                    // Aurora liugj 2013-10-09 modified for aurora's new feature end
                }*/
            }
            // Aurora liugj 2013-10-17 modified for aurora's new feature end

            @Override
            public void updateUi() {
                // TODO Auto-generated method stub
                // Aurora liugj 2013-12-10 modified for checkbox animation start
                /*if (mFirstInMultiMode) {
                    mFirstInMultiMode = false;
                }else {*/
                    //mListAdapter.notifyDataSetChanged();
                //}
                // Aurora liugj 2013-12-10 modified for checkbox animation end
                mSelectCount = null != getSelected() ? getSelected().size() : 0;
                mBottomAuroraMenu.setBottomMenuItemEnable(1, mSelectCount == 0 ? false : true);
                mBottomAuroraMenu.setBottomMenuItemEnable(2, mSelectCount == 0 ? false : true);
            }

            // Aurora liugj 2014-01-07 modified for allcheck animation start
            @Override
            public void updateListView(int allShow) {
                mListAdapter.updateAllCheckBox(allShow);
                mListAdapter.notifyDataSetChanged();
            }
            // Aurora liugj 2014-01-07 modified for allcheck animation end
            
            public void bindToAdapter(GnSelectionManager<Integer> selectionManager) {
                // TODO Auto-generated method stub
                mListAdapter.setSelectionManager(selectionManager);
                // Aurora xuyong 2014-06-11 added for bug #5592 start
                if (selectionManager != null) {
                    selectionManager.setAdapter(mListAdapter);
                }
                // Aurora xuyong 2014-06-11 added for bug #5592 end
            }
        };
    }
    
    private OnAuroraActionBarItemClickListener mActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
        public void onAuroraActionBarItemClicked(int itemId) {
            switch (itemId) {
            case ACTION_BATCH:
                // Aurora liugj 2013-12-03 modified for checkbox animation bug start
                try {
                    boolean deleteIsShow = mSimList.auroraIsRubbishOut();
                    if (deleteIsShow) {
                        // Aurora liugj 2013-09-30 deleted for fix bug-179 start
                        mSimList.auroraOnPause();
                        // Aurora liugj 2013-09-30 deleted for fix bug-179 end
                    }
                    handler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            if (null == mActionBatchHandler) {
                                initThreadsMap();
                                initActionBatchMode();
                                mActionBatchHandler.enterSelectionMode(false, null);
                            }
                        }
                    }, deleteIsShow ? 450 : 0);
                }catch (Exception e) {
                    e.printStackTrace();
                }
                // Aurora liugj 2013-12-03 modified for checkbox animation bug end
                break;
            default:
                break;
            }
        }
    };
    
    private OnAuroraMenuItemClickListener mAuroraMenuCallBack = new OnAuroraMenuItemClickListener() {
        
        @Override
        public void auroraMenuItemClick(int itemId) {
            final ArrayList<Integer> mSelectIds = (ArrayList<Integer>) mActionBatchHandler.getSelected().clone();
            // Aurora xuyong 2014-03-13 modified for bug #3077 start
            final boolean isSingle = mSelectIds.size() > 1 ? false : true;
            switch (itemId) {
            case R.id.aurora_sim_list_menu_copy:

                if (isSingle) {
                    Cursor cursor = (Cursor) mSimList.getItemAtPosition(mSelectIds.get(0));
                    copyToPhoneMemory(cursor, true);
                    // Aurora xuyong 2014-03-21 added for bug #3407 start
                    if(null != mActionBatchHandler) {
                        mActionBatchHandler.leaveSelectionMode();
                    }
                    // Aurora xuyong 2014-03-21 added for bug #3407 end
                } else {
                    showDialog(DIALOG_REFRESH);
                    new Thread(new Runnable() {
                        public void run() {
                            for (Integer integer : mSelectIds) {
                                Cursor cursor = (Cursor) mSimList.getItemAtPosition(integer);
                                copyToPhoneMemory(cursor, false);
                            }
                            handler.sendEmptyMessage(BATCH_COPY);
                        }
                    }).start();
                }
                // Aurora xuyong 2014-03-13 modified for bug #3077 end
                break;
            case R.id.aurora_sim_list_menu_del:
                // Aurora liugj 2013-11-18 modified for deleteAllSim not valid in S4 start
                /*if (mSelectIds.size() == mListAdapter.getCount()) {
                    confirmDeleteDialog(new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Aurora liugj 2013-10-12 modified for aurora's new feature start
                                showDialog(DIALOG_REFRESH);
                                 // Aurora liugj 2013-10-12 modified for aurora's new feature end
                            //deleteAllFromSim();
                            new Thread(new Runnable() {
                                public void run() {
                                    deleteAllFromSim();
                                }
                            }, "AuroraManageSimMessages").start();
                            dialog.dismiss();
                        }
                    }, R.string.confirm_delete_all_SIM_messages);
                }else {*/
                    final StringBuffer msgIndexs = new StringBuffer();
                    for (Integer integer : mSelectIds) {
                        Cursor cursor = (Cursor) mSimList.getItemAtPosition(integer);
                        // Aurora liugj 2013-11-18 modified for bug-929 start
                        String msgIndex = getMsgIndexByCursor(cursor);
                        if(!TextUtils.isEmpty(msgIndex)){
                            msgIndexs.append(msgIndex);
                            int len = msgIndexs.length() - 1;
                            if(mSelectIds.indexOf(integer) != mSelectIds.size() - 1 && msgIndexs.charAt(len) != ';' ) {
                                msgIndexs.append(";");
                            }else if(mSelectIds.indexOf(integer) == mSelectIds.size() - 1 && msgIndexs.charAt(len) == ';' ){
                                msgIndexs.deleteCharAt(len);
                            }
                        // Aurora liugj 2013-11-18 modified for bug-929 end
                        }
                    }
                    confirmDeleteDialog(new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Aurora liugj 2013-10-12 modified for aurora's new feature start
                                showDialog(DIALOG_REFRESH);
                                 // Aurora liugj 2013-10-12 modified for aurora's new feature end
                            new Thread(new Runnable() {
                                public void run() {
                                    deleteFromSim(msgIndexs.toString());
                                }
                            }, "AuroraManageSimMessages").start();
                            dialog.dismiss();
                        }
                    }, mSelectIds.size() == mListAdapter.getCount() ? R.string.confirm_delete_all_SIM_messages : R.string.confirm_delete_selected_SIM_message);
                //}
                // Aurora liugj 2013-11-18 modified for deleteAllSim not valid in S4 end
                break;
            default:
                break;
            }
        }
    };    
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(null != mActionBatchHandler && mActionBatchHandler.isInSelectionMode()) {
            // Aurora liugj 2013-12-11 added for checkbox animation start
            AuroraCheckBox mCheckBox = (AuroraCheckBox) view.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
            // Aurora xuyong 2014-03-11 added for bug #3079 start
            if (mCheckBox == null) {
                return;
            }
            // Aurora xuyong 2014-03-11 added for bug #3079 end
            boolean isChecked = mCheckBox.isChecked();
            mCheckBox.auroraSetChecked(!isChecked, true);
            // Aurora liugj 2013-12-11 added for checkbox animation end
            mActionBatchHandler.getSelectionManger().toggle(position);
        }else {
            final Cursor cursor  = (Cursor) mSimList.getItemAtPosition(position);
            if (cursor == null) {
                return;
            } 
            /*String address = cursor.getString(cursor
                    .getColumnIndexOrThrow("address"));
            String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
            Long date = cursor.getLong(cursor.getColumnIndexOrThrow("date"));*/
            String address = cursor.getString(INDEX_COLUMN_ADDRESS);
            String body = cursor.getString(INDEX_COLUMN_BODY);
            Long date = cursor.getLong(INDEX_COLUMN_DATE);
            
            View contents = View.inflate(this, R.layout.aurora_sim_info_dialog, null);
            TextView bodyView = (TextView) contents.findViewById(R.id.dialog_sim_body);
            TextView dateView = (TextView) contents.findViewById(R.id.dialog_sim_date);
            bodyView.setText(body);
            dateView.setText(MessageUtils.formatAuroraTimeStampString(this, date, false));
            new AuroraAlertDialog.Builder(this).setTitle(formatName(address))
                    .setCancelable(true).setPositiveButton(R.string.sim_delete, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            final String msgIndex = getMsgIndexByCursor(cursor);
                               // Aurora liugj 2013-10-29 added for aurora's new feature start
                            /*confirmDeleteDialog(new OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {*/
                            // Aurora liugj 2013-10-12 modified for aurora's new feature start
                                showDialog(DIALOG_REFRESH);
                                 // Aurora liugj 2013-10-12 modified for aurora's new feature end
                                    new Thread(new Runnable() {
                                        public void run() {
                                            deleteFromSim(msgIndex);
                                        }
                                    }, "AuroraManageSimMessages").start();
                                    dialog.dismiss();
                               /* }
                            }, R.string.confirm_delete_SIM_message);*/
                              // Aurora liugj 2013-10-29 added for aurora's new feature end
                        }
                        
                    })
                    .setNegativeButton(R.string.sim_copy_to_phone_memory, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            copyToPhoneMemory(cursor, true);
                        }
                        
                    }).setView(contents).show();
        }
    };
    
    private String formatName(String address) {
        StringBuffer contantName;
        if(!TextUtils.isEmpty(address)) {
            contantName = new StringBuffer(Contact.get(address, true).getName());
        } else {
            contantName = new StringBuffer(getString(android.R.string.unknownName));
        }
        return contantName.toString();
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_MENU: {
                    if (mActionBatchHandler != null && mActionBatchHandler.isInSelectionMode()) {
                        return true;
                    }
                    break;
                }
            }
        }
        return false;
    }
    
    @Override
    protected void onDestroy() {
        if (mListAdapter != null) {
            mListAdapter.destroy();
            mListAdapter = null;
        }
        super.onDestroy();
        // Aurora liugj 2013-12-17 added for SimChangeObserver bug start 
        mContentResolver.unregisterContentObserver(simChangeObserver);
        // Aurora liugj 2013-12-17 added for SimChangeObserver bug end
    }

    // Aurora liugj 2014-01-07 added for bug:when delete visible onBackPressed activity finished start
    @Override
    public void onBackPressed() {
        if (mSimList != null) {
            boolean deleteIsShow = mSimList.auroraIsRubbishOut();
            if (deleteIsShow) {
                mSimList.auroraOnPause();
                return;
            }
        }
        super.onBackPressed();
    }
    // Aurora liugj 2014-01-07 added for bug:when delete visible onBackPressed activity finished end
}
