/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

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

package com.android.mms.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.android.internal.telephony.ITelephony;
import com.android.mms.LogTag;
import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.data.FolderView;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.ui.ConversationList.BaseProgressQueryHandler;
import com.android.mms.util.DraftCache;
import com.android.mms.util.Recycler;
import android.database.sqlite.SqliteWrapper;

import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraListActivity;
import aurora.app.AuroraProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.os.Bundle;
import android.os.Handler;
import android.os.ServiceManager;
import aurora.preference.AuroraPreferenceManager;
import android.provider.Browser;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.telephony.SmsManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import aurora.widget.AuroraButton;
import android.widget.CheckBox;
import android.widget.ImageView;
import aurora.widget.AuroraListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.net.Uri;
import gionee.provider.GnTelephony.Threads;
import com.aurora.featureoption.FeatureOption;
import gionee.provider.GnTelephony.Mms;
import gionee.provider.GnTelephony.MmsSms;
import gionee.provider.GnTelephony.Sms;
// Aurora xuyong 2013-09-13 modified for aurora's new feature start
import android.app.ActionBar;
// Aurora xuyong 2013-09-13 modified for aurora's new feature end
import android.widget.ImageButton;
import android.view.ViewGroup;
//import android.widget.WeakList;

/**
 * This activity provides a list view of existing conversations.
 */
public class FolderViewMultiDeleteActivity extends AuroraActivity
          implements DraftCache.OnDraftChangedListener {
   // public static final WeakList<FolderViewMultiDeleteActivity> INSTANCES = new WeakList<FolderViewMultiDeleteActivity>(2);
    private static final String TAG = "FolderViewMultiDeleteActivity";
    private ThreadListQueryHandler mQueryHandler;
    private FolderViewMultiDeleteListAdapter mListAdapter;
    private AuroraListView mMultiDeleteList;
    private ContentResolver mContentResolver;
    private boolean needQuit = false;
    public static final int FOLDERVIEW_DELETE_TOKEN      = 1022;
    private static final String FOR_MULTIDELETE         = "ForMultiDelete";
    private static final String FOR_FOLDERMODE_MULTIDELETE = "ForFolderMultiDelete";
    private static final Uri CB_DELETE_URI = Uri.parse("content://cb/messages/");
    private static final Uri WAPPUSH_DELETE_URI = Uri.parse("content://wappush/");
    
    private MenuItem mSelectAll;
    private MenuItem mCancelSelect;
    private MenuItem mDelete;
    private TextView mActionBarText;
    private boolean mIsSelectedAll;
    private int mBoxType = 0;
    public static int viewid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContentResolver = getContentResolver();
        setContentView(R.layout.folderview_multi_delete);
        mQueryHandler = new ThreadListQueryHandler(mContentResolver);
        
        mMultiDeleteList = (AuroraListView) findViewById(R.id.item_list);
        mMultiDeleteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (view != null) {
                    ((FolderViewMultiDeleteListItem) view).clickListItem();
                    long itemdId = -1;    
                    Cursor cursor  = (Cursor)mMultiDeleteList.getItemAtPosition(position);
                    if (cursor == null) {
                        Log.d(TAG, "cursor == null");
                         return;
                    }
                    int type = cursor.getInt(6);
                    Log.d(TAG, "type ="+type);
                    int messageid   = cursor.getInt(0);
                    Log.d(TAG, "messageid ="+messageid);
                    itemdId = FolderViewMultiDeleteListAdapter.getKey(type, messageid);
                    //itemdId = (long)(type == 2 ? -messageid : messageid);
                    mListAdapter.changeSelectedState(itemdId);
                    invalidateOptionsMenu();
                }
            }
        });
        setUpActionBar();
        initListAdapter(); 
        initActivityState(savedInstanceState);
        
    }

    private void initActivityState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            boolean selectedAll = savedInstanceState.getBoolean("is_all_selected");
            if (selectedAll) {
                mListAdapter.setItemsValue(true, null);
                return;
            } 
            
            long [] selectedItems = savedInstanceState.getLongArray("select_list");
            if (selectedItems != null) {
                mListAdapter.setItemsValue(true, selectedItems);
            }
        }
        
    }
    
    private final FolderViewMultiDeleteListAdapter.OnContentChangedListener mContentChangedListener =
        new FolderViewMultiDeleteListAdapter.OnContentChangedListener() {
        public void onContentChanged(FolderViewMultiDeleteListAdapter adapter) {
            startAsyncQuery();
            
        }          
    };

    private void initListAdapter() {
        mListAdapter = new FolderViewMultiDeleteListAdapter(this, null);
        mListAdapter.setOnContentChangedListener(mContentChangedListener);
        mMultiDeleteList.setAdapter(mListAdapter);
        mMultiDeleteList.setRecyclerListener(mListAdapter);
    }
   
    @Override
    protected void onNewIntent(Intent intent) {
        // Handle intents that occur after the activity has already been created.
        startAsyncQuery();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);      
        if (mListAdapter != null) {
            if (mListAdapter.isAllSelected()) {
                outState.putBoolean("is_all_selected", true);
            } else if (mListAdapter.getSelectedNumber() == 0) {
                return;
            }
            else {
                long [] checkedArray = new long[mListAdapter.getSelectedNumber()];
                Iterator iter = mListAdapter.getItemList().entrySet().iterator();
                int i = 0;
                while (iter.hasNext()) {
                    @SuppressWarnings("unchecked")
                    Map.Entry<Long, Boolean> entry = (Entry<Long, Boolean>) iter.next();
                    if (entry.getValue()) {                        
                        checkedArray[i] = entry.getKey();
                        i++;
                    }
                }    
                outState.putLongArray("select_list", checkedArray);
            }
            
        }     
    }
    
    @Override
    protected void onResume() {   
        startAsyncQuery();   
        super.onResume();
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        
        DraftCache.getInstance().addOnDraftChangedListener(this);
        if (!Conversation.loadingThreads()) {
            Contact.invalidateCache();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        DraftCache.getInstance().removeOnDraftChangedListener(this);
        mListAdapter.changeCursor(null);
              
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
//        synchronized (INSTANCES) {
//            INSTANCES.remove(this);
//        }
    }

    public void onDraftChanged(final long threadId, final boolean hasDraft) {
        // Run notifyDataSetChanged() on the main thread.
        mQueryHandler.post(new Runnable() {
            public void run() {
                mListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setUpActionBar() {
        // Aurora xuyong 2013-09-13 modified for aurora's new feature start
        ActionBar actionBar = getActionBar();
        // Aurora xuyong 2013-09-13 modified for aurora's new feature end

        ViewGroup v = (ViewGroup)LayoutInflater.from(this)
            .inflate(R.layout.multi_delete_list_actionbar, null);
        // Aurora xuyong 2013-09-13 modified for aurora's new feature start
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_SHOW_TITLE);
        // Aurora xuyong 2013-09-13 modified for aurora's new feature end
        ImageButton mQuit = (ImageButton) v.findViewById(R.id.cancel_button);
        mQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                FolderViewMultiDeleteActivity.this.finish();
            }
        });
        
        mActionBarText = (TextView) v.findViewById(R.id.select_items);
        actionBar.setCustomView(v);
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.compose_multi_select_menu, menu);
        mSelectAll = menu.findItem(R.id.select_all);
        mCancelSelect = menu.findItem(R.id.cancel_select);
        mDelete = menu.findItem(R.id.delete);

        super.onCreateOptionsMenu(menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        int selectNum = getSelectedCount();
        Log.d("this","selectNum = "+selectNum);
        mActionBarText.setText(getResources().getQuantityString(
            R.plurals.message_view_selected_message_count, selectNum, selectNum));

        super.onPrepareOptionsMenu(menu);
        return true;
    }
    
    
    private int getSelectedCount() {
        return mListAdapter.getSelectedNumber();
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.select_all:
                    mIsSelectedAll = true;
                    markCheckedState(mIsSelectedAll);
                    mListAdapter.setItemsValue(true, null);
                    invalidateOptionsMenu();
                break;
            case R.id.cancel_select:
                if (mListAdapter.getSelectedNumber() > 0) {
                    mIsSelectedAll = false;
                    markCheckedState(mIsSelectedAll);
                    mListAdapter.setItemsValue(false, null);
                    invalidateOptionsMenu();
                }
                break;
            case R.id.delete:
                if (mListAdapter.getSelectedNumber() > 0) {
                    needQuit = false;
                    confirmMultiDelete();
                }
                break;
        }
        return true;
    }
    
    
    private void startAsyncQuery() {
        //setProgressBarIndeterminateVisibility(true);
       try {
//          setTitle(getString(R.string.refreshing));
//          setProgressBarIndeterminateVisibility(true);
       Bundle extras = getIntent().getExtras();
       viewid = extras.getInt(FolderViewList.FOLDERVIEW_KEY);
       switch (viewid) {
              case FolderViewList.OPTION_INBOX:
                  mBoxType = 1;
                  //TitleView.setText(R.string.inbox);
                  FolderView.startQueryForInboxView(mQueryHandler, FolderViewList.INBOXFOLDER_LIST_QUERY_TOKEN);
                  break;
              case FolderViewList.OPTION_OUTBOX:
                  mBoxType = 4;
                  //TitleView.setText(R.string.outbox);
                  FolderView.startQueryForOutBoxView(mQueryHandler, FolderViewList.OUTBOXFOLDER_LIST_QUERY_TOKEN);
                  break;
              case FolderViewList.OPTION_DRAFTBOX:
                  mBoxType = 3;
                 // TitleView.setText(R.string.draftbox);
                  FolderView.startQueryForDraftboxView(mQueryHandler, FolderViewList.DRAFTFOLDER_LIST_QUERY_TOKEN);
                  break;
              case FolderViewList.OPTION_SENTBOX:
                  mBoxType = 2;
                 // TitleView.setText(R.string.sentbox);
                  FolderView.startQueryForSentboxView(mQueryHandler, FolderViewList.SENTFOLDER_LIST_QUERY_TOKEN);
                  break;
              default:
                  break;
          }
        } catch (SQLiteException e) {
              SqliteWrapper.checkSQLiteException(this, e);
      }
    }
      
    @Override
    public boolean onSearchRequested() {
        startSearch(null, false, null /*appData*/, false);
        return true;
    }  

    private final class ThreadListQueryHandler extends BaseProgressQueryHandler {
        public ThreadListQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            //setProgressBarIndeterminateVisibility(false);
//            switch (token) {
//            case FolderViewList.INBOXFOLDER_LIST_QUERY_TOKEN:
                if(cursor == null || cursor.getCount() == 0){
                    finish();                
                }
                mListAdapter.initListMap(cursor);
                mListAdapter.changeCursor(cursor);
     
        }    
        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            switch (token) {
            case FOLDERVIEW_DELETE_TOKEN:              
                //Log.i(TAG, "thread delete complete,thread id = " + mThreadID);
                if (mListAdapter.isAllSelected() || needQuit) {
                    finish();
                }
                if (progress()) {
                    dismissProgressDialog();
                }
                DraftCache.getInstance().refresh();
                break;                          
            default:
                Log.e(TAG, "invaild token");            
            }
        }
    }

    private void markCheckedState(boolean checkedState) {
        int count = mMultiDeleteList.getChildCount();  
        FolderViewMultiDeleteListItem layout = null;
        int childCount = 0;
        View view = null;
        for (int i = 0; i < count; i++) {
            layout = (FolderViewMultiDeleteListItem)mMultiDeleteList.getChildAt(i);
            //mark background color
            layout.setSelectedBackGroud(checkedState);
            /*
            childCount = layout.getChildCount();
            
            for (int j = 0; j < childCount; j++) {
                view = layout.getChildAt(j);
                if (view instanceof CheckBox) {
                    ((CheckBox)view).setChecked(checkedState);
                    break;
                }
            }
            */
        }
    }

    
    private void confirmMultiDelete() {
        AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_dialog_title);
        builder.setIconAttribute(android.R.attr.alertDialogIcon);
        builder.setCancelable(true);
        builder.setMessage(R.string.confirm_delete_selected_messages);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mQueryHandler.setProgressDialog(DeleteProgressDialogUtil.getProgressDialog(
                    FolderViewMultiDeleteActivity.this));
                mQueryHandler.showProgressDialog();
                new Thread(new Runnable() {
                    public void run() {
                        Iterator iter = mListAdapter.getItemList().entrySet().iterator();
                        Uri deleteSmsUri = Sms.CONTENT_URI;;
                        Uri deleteMmsUri = null;
                        Uri deleteCbUri  = null;
                        Uri deleteWpUri = null;
                        ArrayList<String> argsSms = new ArrayList<String>();
                        String[] argsMms = new String[mListAdapter.getSelectedNumber()];
                        String[] argsCb = new String[mListAdapter.getSelectedNumber()];
                        String[] argsWp = new String[mListAdapter.getSelectedNumber()];
                        int i = 0;
                        int j = 0;
                        int k = 0;
                        int m = 0;
                        while (iter.hasNext()) {
                            @SuppressWarnings("unchecked")
                            Map.Entry<Long, Boolean> entry = (Entry<Long, Boolean>) iter.next();
                            if (!entry.getValue()) {
                                if(entry.getKey() > 0){
                                    Log.i(TAG, "sms");      
                                    argsSms.add(Long.toString(entry.getKey()));
                                    Log.i(TAG, "sms :entry.getKey()= "+entry.getKey());
                                   // Log.i(TAG, "argsSms[i]" + argsSms[i]);
                                    //deleteSmsUri = ContentUris.withAppendedId(Sms.CONTENT_URI, entry.getKey());
                                    deleteSmsUri = Sms.CONTENT_URI;
                                }
                            } else {
                                if (entry.getKey() > 100000){
                                    deleteWpUri = ContentUris.withAppendedId(WAPPUSH_DELETE_URI, entry.getKey()-100000);
                                    Log.i(TAG, "wappush :entry.getKey()-100000 = "+(entry.getKey()-100000));
                                    Log.i(TAG, "wappush");      
                                    mQueryHandler.startDelete(FOLDERVIEW_DELETE_TOKEN,
                                        null, deleteWpUri, null, null);                              
                                    m++;
                                }else if(entry.getKey() < -100000){
                                    Log.i(TAG, "CB");      
                                    argsCb[k] = Long.toString(-(entry.getKey()+100000));
                                    Log.i(TAG, "CB :-entry.getKey() +100000= "+(-(entry.getKey()+100000)));
                                    Log.i(TAG, "argsSms[i]" + argsCb[k]);
                                    //deleteSmsUri = ContentUris.withAppendedId(Sms.CONTENT_URI, entry.getKey());
                                    deleteCbUri = CB_DELETE_URI;
                                    k++;
                                }else if(entry.getKey() < 0){
                                    Log.i(TAG, "mms");                  
                                    argsMms[j] = Long.toString(-entry.getKey());
                                    Log.i(TAG, "mms :-entry.getKey() = "+(-entry.getKey()));
                                    Log.i(TAG, "argsMms[j]" + argsMms[j]);
                                    //deleteMmsUri = ContentUris.withAppendedId(Mms.CONTENT_URI, -entry.getKey());
                                    deleteMmsUri = Mms.CONTENT_URI;
                                    j++;
                                }
                            }
                             
                        }
                        mQueryHandler.setMax(
                             (deleteSmsUri != null ? 1 : 0) +
                             (deleteMmsUri != null ? 1 : 0)+(deleteCbUri != null ? 1 : 0));
                        argsSms.add(String.valueOf(mBoxType));
                        String[] deleteArgs = argsSms.toArray((new String[0]));
                        mQueryHandler.startDelete(FOLDERVIEW_DELETE_TOKEN,
                                    null, deleteSmsUri, FOR_FOLDERMODE_MULTIDELETE, deleteArgs);
                        if (deleteMmsUri != null) {
                            mQueryHandler.startDelete(FOLDERVIEW_DELETE_TOKEN,
                                    null, deleteMmsUri, FOR_MULTIDELETE, argsMms);
                        }
                        if (deleteCbUri != null) {
                            mQueryHandler.startDelete(FOLDERVIEW_DELETE_TOKEN,
                                    null, deleteCbUri, FOR_MULTIDELETE, argsCb);
                        }
                        needQuit = true;
                    }
                }).start();
            }
        });
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }

    private void markThreadRead(long threadId){
        //mark the thread as read
        Uri threadUri = ContentUris.withAppendedId(Threads.CONTENT_URI, threadId);
        ContentValues readContentValues = new ContentValues(1);
        readContentValues.put("read", 1);
        mContentResolver.update(threadUri, readContentValues,
                "read=0", null);
    }
}


