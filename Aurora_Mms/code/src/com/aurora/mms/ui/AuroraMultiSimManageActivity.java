package com.aurora.mms.ui;
// Aurora xuyong 2014-06-05 created for aurora's multisim feature 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.database.sqlite.SqliteWrapper;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;
import android.app.Dialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony.Threads;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.telephony.SmsManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;

import aurora.widget.AuroraMenu;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraCheckBox;

import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.MessageUtils;

import gionee.provider.GnTelephony.SIMInfo;
import gionee.provider.GnTelephony;

import com.gionee.internal.telephony.GnPhone;

import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraListView;

import com.android.mms.ui.MessageUtils;
import com.android.mms.util.GnSelectionManager;
import com.android.mms.util.Recycler;

public class AuroraMultiSimManageActivity extends AuroraActivity implements 
         OnItemClickListener, OnItemLongClickListener,OnKeyListener{
    
    private static final boolean DEBUG = false;
    private static final String TAG = "AuroraMultiSimManageActivity";
    
    private static Uri ICC_URI1, ICC_URI2;
    
    private AuroraProgressDialog dialog;
    private AuroraActionBar mActionBar;
    private AuroraMenu mAuroraMenu;
    private AuroraMenu mBottomAuroraMenu;
    
    private Map<Integer, Integer> mThreadsMap = new HashMap<Integer, Integer>();
    
    private AuroraListView mSimList;
    private TextView mEmptyTextView;
    
    private static final int BATCH_COPY = 0;
    private static final int ACTION_BATCH = 0;
    private static final int DIALOG_REFRESH = 1;
    
    private static final int SHOW_BUSY = 0;
    private static final int SHOW_LIST = 1;
    private static final int SHOW_EMPTY = 2;
    
    private static final String ALL_SMS = "999999";
    private static final String FOR_MULTIDELETE = "ForMultiDelete";
    private int mState;
    
    private int showSlotId = -1;
    
    private SimMessageListAdapter mAdapter;
    
    private AsyncQueryHandler mQueryHandler = null;
    public static AuroraMultiSimManageActivity mSimMessages = null;
    
    private AuroraActionBatchHandler<Integer> mActionBatchHandler = null;
    
    private ContentResolver mContentResolver;
    private Cursor mCursor = null;
    
    private boolean isInit = false;
    public boolean isQuerying = false;
    public boolean isDeleting = false; 
    
    public static int observerCount = 0; 
    private int mSelectCount = 0;
    
    @Override
    protected void onCreate(Bundle bundle) {
        Log.d(TAG, "onCreate"); 
        // Aurora xuyong 2015-06-30 added for bug #13935 start
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        // Aurora xuyong 2015-06-30 added for bug #13935 end
        super.onCreate(bundle);
        // Aurora xuyong 2015-06-30 deleted for bug #13935 start
        //requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        // Aurora xuyong 2015-06-30 deleted for bug #13935 end
        Intent it = getIntent();
        showSlotId = it.getIntExtra("SlotId", 2);
        if (showSlotId == 0) {
            ICC_URI1 = Uri.parse("content://sms/icc");
            ICC_URI2 = null;
        } else if (showSlotId == 1) {
            ICC_URI2 = Uri.parse("content://sms/icc2");
            ICC_URI1 = null;
        } else if (showSlotId == 2) {
            ICC_URI1 = Uri.parse("content://sms/icc");
            ICC_URI2 = Uri.parse("content://sms/icc2");
        }
        
        setAuroraContentView(R.layout.aurora_multisim_manage_layout, AuroraActionBar.Type.Normal);
        mSimMessages = this;
        
        mContentResolver = getContentResolver();
        mQueryHandler = new QueryHandler(mContentResolver, this);
        
        initView();
        
        mActionBar = getAuroraActionBar();
        mActionBar.setTitle(R.string.sim_manage_messages_title);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        
        isInit = true;
        registerSim1ChangeObserver();
        registerSim2ChangeObserver();
    }
    
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
                    AuroraMultiSimManageActivity.this.removeDialog(DIALOG_REFRESH);
                    Toast.makeText(AuroraMultiSimManageActivity.this, getString(R.string.copyto_phone_done), Toast.LENGTH_SHORT)
                            .show();
                    break;
                default:
                    break;
            }
        }
        
    };
    
    private void registerSim1ChangeObserver() {
        if (ICC_URI1 != null) {
            mContentResolver.registerContentObserver(
                    ICC_URI1, true, simChangeObserver);
        }
    }
    
    private void registerSim2ChangeObserver() {
        if (ICC_URI2 != null) {
            mContentResolver.registerContentObserver(
                    ICC_URI2, true, simChangeObserver);
        }
    }
    
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
    
    private void refreshMessageList() {
           if (!isDeleting) {
               showDialog(DIALOG_REFRESH);
        }
       if (mCursor != null) {
           stopManagingCursor(mCursor);
       }
       startQuery1();
   }
    
    @Override
    public void onResume() {
        Log.d(TAG, "onResume"); 
        super.onResume();
        if (isInit) {
            init();
        }
        if (isDeleting) {
            // This means app is deleting SIM SMS when left activity last time
            refreshMessageList();
        }
    }
    
    @Override
    public void onPause() {
        Log.d(TAG, "onPause");                                            
        super.onPause();
        mSimList.auroraOnPause();
        Contact.invalidateCache();
    }

    @Override
    protected void onDestroy() {
        if (mAdapter != null) {
            mAdapter.destroy();
            mAdapter = null;
        }
        super.onDestroy();
        mContentResolver.unregisterContentObserver(simChangeObserver);
    }
    
    public static final int SIM_FULL_NOTIFICATION_ID = 234;
    private void init() {
        isInit = false;
        MessagingNotification.cancelNotification(getApplicationContext(),
                SIM_FULL_NOTIFICATION_ID);
        showDialog(DIALOG_REFRESH);
        startQuery1();
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
    
    private class QueryHandler extends AsyncQueryHandler {
        private final AuroraMultiSimManageActivity mParent;
        
        public QueryHandler(ContentResolver cr, AuroraMultiSimManageActivity activity) {
            super(cr);
            mParent = activity;
        }
        
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if(null != mActionBatchHandler) {
                mActionBatchHandler.leaveSelectionMode();
            }
            if (mCursor != null && !mCursor.isClosed()) {
                stopManagingCursor(mCursor);
            }
            if (showSlotId == 2) {
                if (resultList.size() == 0) {
                    resultList.add(convertIccToSms(cursor, mSlot1Id));
                    startQuery2();
                    return;
                } else {
                    if(observerCount > 0){
                        AuroraMultiSimManageActivity.this.startQuery1();
                        observerCount = 0;
                        return;
                    }else{
                        isQuerying = false;
                        removeDialog(DIALOG_REFRESH);
                        if(isDeleting){
                            Toast.makeText(AuroraMultiSimManageActivity.this, getString(R.string.sms_has_deleted), Toast.LENGTH_SHORT).show();
                            isDeleting = false;
                        }
                    }
                    resultList.add(convertIccToSms(cursor, mSlot2Id));
                }
            } else if (showSlotId == 0) {
                if(observerCount > 0){
                    AuroraMultiSimManageActivity.this.startQuery1();
                    observerCount = 0;
                    return;
                }else{
                    isQuerying = false;
                    removeDialog(DIALOG_REFRESH);
                    if(isDeleting){
                        Toast.makeText(AuroraMultiSimManageActivity.this, getString(R.string.sms_has_deleted), Toast.LENGTH_SHORT).show();
                        isDeleting = false;
                    }
                }
                resultList.add(convertIccToSms(cursor, mSlot1Id));
            } else if (showSlotId == 1) {
                if(observerCount > 0){
                    AuroraMultiSimManageActivity.this.startQuery1();
                    observerCount = 0;
                    return;
                }else{
                    isQuerying = false;
                    removeDialog(DIALOG_REFRESH);
                    if(isDeleting){
                        Toast.makeText(AuroraMultiSimManageActivity.this, getString(R.string.sms_has_deleted), Toast.LENGTH_SHORT).show();
                        isDeleting = false;
                    }
                }
                resultList.add(convertIccToSms(cursor, mSlot2Id));
            }
            if (resultList.size() == 2) {
                mCursor = convertToCursor(resultList.get(0), resultList.get(1));
            } else {
                mCursor = convertToCursor(resultList.get(0));
            }
            if (mCursor != null) {
                if (!mCursor.moveToFirst()) {
                     updateState(SHOW_EMPTY);
                } else if (mAdapter == null) {
                    updateState(SHOW_LIST);
                    mAdapter = new SimMessageListAdapter(mParent, mCursor, true);
                    mSimList.setAdapter(mAdapter);
                    mSimList.auroraSetNeedSlideDelete(true);
                    mSimList.auroraSetAuroraBackOnClickListener(new AuroraListView.AuroraBackOnClickListener() {
                        
                        @Override
                        public void auroraOnClick(int position) {
                            Cursor pcursor  = (Cursor) mSimList.getItemAtPosition(position);
                            if (pcursor == null) {
                                return;
                            }
                            final Uri uri = getUriByPosition(position);
                            mSimList.auroraDeleteSelectedItemAnim();
                            final String msgIndex = getMsgIndexByCursor(pcursor);
                            new Thread(new Runnable() {
                                public void run() {
                                    ArrayList<IndexAndUri> list = new ArrayList<IndexAndUri>();
                                    list.add(new IndexAndUri(msgIndex, uri));
                                    deleteFromSim(list);
                                }
                            }, "AuroraMultiSimManageActivity").start();
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
                    mSimList.setOnItemClickListener(mParent);
                    mSimList.setOnItemLongClickListener(mParent);
                    mSimList.setOnKeyListener(mParent);
                    mSimList.setRecyclerListener(mAdapter);
                } else {
                    updateState(SHOW_LIST);
                    mAdapter.changeCursor(mCursor);
                }
                startManagingCursor(mCursor);
                if (MmsApp.mIsSafeModeSupport) {
                    // Let user know the SIM is empty
                    updateState(SHOW_EMPTY);
                } else {
                    invalidateOptionsMenu(); 
                }
            } else {
                 if (mAdapter != null) {
                     mAdapter.changeCursor(null);
                 }
                 updateState(SHOW_EMPTY);
            }    
        }
    }
    
    private static final int INDEX_COLUMN_ADDRESS = 1;
    private static final int INDEX_COLUMN_DATE = 4;
    private static final int INDEX_COLUMN_BODY = 3;
    private static final int INDEX_COLUMN_SCA = 0;
    private static final int INDEX_COLUMN_STATUS = 5;
    private static final int INDEX_COLUMN_ICC = 6;
    
    private String getMsgIndexByCursor(Cursor cursor) {
        if (cursor != null) {
            return cursor.getString(INDEX_COLUMN_ICC);
        } 
        return null;
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(null != mActionBatchHandler && mActionBatchHandler.isInSelectionMode()) {
            AuroraCheckBox mCheckBox = (AuroraCheckBox) view.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
            if (mCheckBox == null) {
                return;
            }
            boolean isChecked = mCheckBox.isChecked();
            mCheckBox.auroraSetChecked(!isChecked, true);
            mActionBatchHandler.getSelectionManger().toggle(position);
        } else {
            final Cursor cursor  = (Cursor) mSimList.getItemAtPosition(position);
            if (cursor == null) {
                return;
            }
            
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
                            final Uri uri = getUriByPosition(cursor.getPosition());
                                showDialog(DIALOG_REFRESH);
                                    new Thread(new Runnable() {
                                        public void run() {
                                            ArrayList<IndexAndUri> list = new ArrayList<IndexAndUri>();
                                            list.add(new IndexAndUri(msgIndex, uri));
                                            deleteFromSim(list);
                                        }
                                    }, "AuroraMultiSimManageActivity").start();
                                    dialog.dismiss();
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
    }
    
    private boolean isIncomingMessage(Cursor cursor) {
        int messageStatus = cursor.getInt(INDEX_COLUMN_STATUS);
        Log.d(MmsApp.TXN_TAG, "message status:" + messageStatus);
        return (messageStatus == SmsManager.STATUS_ON_ICC_READ) ||
             (messageStatus == SmsManager.STATUS_ON_ICC_UNREAD);
    }
    
    private class IndexAndUri {
        String mIndex;
        Uri mUri;
        
        public IndexAndUri(String index, Uri uri) {
            mIndex = index;
            mUri = uri;
        }
        
        public String getIndex() {
            return mIndex;
        }
        
        public Uri getUri() {
            return mUri;
        }
        
    }
    
    private void deleteFromSim(ArrayList<IndexAndUri> indexAndUriList) {
        /* 1. Non-Concatenated SMS's message index string is like "1"
         * 2. Concatenated SMS's message index string is like "1;2;3;".
         * 3. If a concatenated SMS only has one segment stored in SIM Card, its message 
         *    index string is like "1;".
         */
        if (indexAndUriList == null) {
            return;
        }
       // Aurora xuyong 2014-06-17 modify for bug #5808 start
        boolean neeShowSIMFullNoti = false;
        for (IndexAndUri item : indexAndUriList) {
            Uri uri = item.getUri();
            String index = item.getIndex();
            if (uri != null && uri.equals(ICC_URI1)) {
                if (index != null) {
                    neeShowSIMFullNoti |= (1 == SqliteWrapper.delete(this, mContentResolver, ICC_URI1, FOR_MULTIDELETE, index.split(";")));
                }
            } else if (uri != null && uri.equals(ICC_URI2)) {
                if (index != null) {
                    neeShowSIMFullNoti |= (1 == SqliteWrapper.delete(this, mContentResolver, ICC_URI2, FOR_MULTIDELETE, index.split(";")));
                }
            }
            //Aurora yudingmin 2014-09-02 added for bug #8053 start
            try{
                Thread.sleep(300);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
            //Aurora yudingmin 2014-09-02 added for bug #8053 end
        }
        if (neeShowSIMFullNoti) {
        // Aurora xuyong 2014-06-17 modify for bug #5808 end
            MessagingNotification.cancelNotification(getApplicationContext(),
                    SIM_FULL_NOTIFICATION_ID);
        }
        isDeleting = true;
    }
    
    public Uri getUriByPosition(int position) {
        Uri uri = null;
        if (resultList == null || resultList.size() <= 0) {
            return null;
        }
       // Aurora xuyong 2014-06-17 modify for bug #5808 start
        if (resultList.size() == 1) {
            if (ICC_URI1 != null) {
                uri = ICC_URI1;
            } else if (ICC_URI2 != null) {
                uri = ICC_URI2;
            }
        } else if (resultList.size() == 2) {
            if (resultList.get(0) != null) {
                if (position > (resultList.get(0).length - 1)) {
                    uri = ICC_URI2;
                } else {
                    uri = ICC_URI1;
                }
            } else {
                uri = ICC_URI2;
            }
        }
       // Aurora xuyong 2014-06-17 modify for bug #5808 end
        return uri;
    }

    public void deleteFromSim(Cursor cursor) {
        if (cursor == null) {
            return;
        }
        String msgIndex = getMsgIndexByCursor(cursor);
        int position  = cursor.getPosition();
        Uri uri = getUriByPosition(position);
        ArrayList<IndexAndUri> list = new ArrayList<IndexAndUri>();
        list.add(new IndexAndUri(msgIndex, uri));
        if (msgIndex != null) {
            deleteFromSim(list);
        }
    }

    private void deleteAllFromSim() {
        // For Delete all,MTK FW support delete all using messageIndex = -1, here use 999999 instead of -1;
        String messageIndexString = ALL_SMS;
        Uri simUri1 = ICC_URI1.buildUpon().appendPath(messageIndexString).build();
        Uri simUri2 = ICC_URI2.buildUpon().appendPath(messageIndexString).build();
        Log.i(TAG, "delete simUri1: " + simUri1);
        Log.i(TAG, "delete simUri2: " + simUri2);
        if (SqliteWrapper.delete(this, mContentResolver, simUri1, null, null) == 1 
                | SqliteWrapper.delete(this, mContentResolver, simUri2, null, null) == 1) {
            MessagingNotification.cancelNotification(getApplicationContext(),
                    SIM_FULL_NOTIFICATION_ID);
        }
        isDeleting = true;
    }
    
    private void copyToPhoneMemory(Cursor cursor, boolean single) {
            if (cursor == null) {
                return;
            }
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
            if (DEBUG) {
                Log.d(MmsApp.TXN_TAG, "\t address \t=" + address);
                Log.d(MmsApp.TXN_TAG, "\t body \t=" + body);
                Log.d(MmsApp.TXN_TAG, "\t date \t=" + date);
                Log.d(MmsApp.TXN_TAG, "\t sc \t=" + serviceCenter);
            }
            Uri MsgUri = null;
            
            try {
                if (isIncomingMessage(cursor)) {
                    Log.d(MmsApp.TXN_TAG, "Copy incoming sms to phone");
                    if (MmsApp.mGnMultiSimMessage) {
                        SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(this,
                                cursor.getInt(cursor.getColumnIndex("_id")));
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
                                cursor.getInt(cursor.getColumnIndex("_id")));
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
                        MsgUri = GnTelephony.Sms.Sent.addMessage(mContentResolver,
                                address, body, null, date);
                    }
                }
                if(MmsApp.mGnMessageSupport) {
                    Long threadId = Threads.getOrCreateThreadId(getApplicationContext(), address);
                    Recycler.getSmsRecycler().deleteOldMessagesByThreadId(getApplicationContext(), 
                    threadId);
                } else {
                    Recycler.getSmsRecycler().deleteOldMessages(getApplicationContext());
                }
                if (single) {
                    if (MsgUri != null) {
                        Log.d(MmsApp.TXN_TAG, "Copy to phone success. message uri:" + MsgUri.toString());
                        String msg = getString(R.string.copyto_phone_done);
                        Toast.makeText(AuroraMultiSimManageActivity.this, msg, Toast.LENGTH_SHORT)
                                .show();
                    }else {
                        String msg = getString(R.string.copyto_phone_fail);
                        Toast.makeText(AuroraMultiSimManageActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                    
                }
                
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
        }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                 if (mActionBar != null && (mActionBar.auroraIsExitEditModeAnimRunning() || mActionBar.auroraIsEntryEditModeAnimRunning())) {
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
        if (mAdapter == null || mAdapter.getCount() == 0) {
            return;
        }
        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(-1);
        int i = 0;
        while(cursor.moveToNext()) {
            if (mThreadsMap.get(i) == null) {
                mThreadsMap.put(i++, i - 1);
            }
        }
    }
    
    private void confirmDeleteDialog(OnClickListener listener, int messageId) {
        AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this/*, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN*/);
        builder.setTitle(R.string.confirm_dialog_title);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.yes, listener);
        builder.setNegativeButton(R.string.no, null);
        builder.setMessage(messageId);

        builder.show();
    }
    
    private String formatName(String address) {
        StringBuffer contantName;
        if(!TextUtils.isEmpty(address)) {
            contantName = new StringBuffer(Contact.get(address, true).getName());
        } else {
            contantName = new StringBuffer(getString(android.R.string.unknownName));
        }
        return contantName.toString();
    }
    
    private OnAuroraMenuItemClickListener mAuroraMenuCallBack = new OnAuroraMenuItemClickListener() {
        
        @Override
        public void auroraMenuItemClick(int itemId) {
            final ArrayList<Integer> mSelectIds = (ArrayList<Integer>) mActionBatchHandler.getSelected().clone();
            final boolean isSingle = mSelectIds.size() > 1 ? false : true;
            switch (itemId) {
            case R.id.aurora_sim_list_menu_copy:

                if (isSingle) {
                    Cursor cursor = (Cursor) mSimList.getItemAtPosition(mSelectIds.get(0));
                    copyToPhoneMemory(cursor, true);
                    if(null != mActionBatchHandler) {
                        mActionBatchHandler.leaveSelectionMode();
                    }
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
                break;
            case R.id.aurora_sim_list_menu_del:
                    final ArrayList<IndexAndUri> list = new ArrayList<IndexAndUri>();
                    final StringBuffer msgIndexs = new StringBuffer();
                    for (Integer integer : mSelectIds) {
                        Cursor cursor = (Cursor) mSimList.getItemAtPosition(integer);
                        int position  = cursor.getPosition();
                        Uri uri = getUriByPosition(position);
                        String msgIndex = getMsgIndexByCursor(cursor);
                        list.add(new IndexAndUri(msgIndex, uri));
                        /*if(!TextUtils.isEmpty(msgIndex)){
                            msgIndexs.append(msgIndex);
                            int len = msgIndexs.length() - 1;
                            if(mSelectIds.indexOf(integer) != mSelectIds.size() - 1 && msgIndexs.charAt(len) != ';' ) {
                                msgIndexs.append(";");
                            }else if(mSelectIds.indexOf(integer) == mSelectIds.size() - 1 && msgIndexs.charAt(len) == ';' ){
                                msgIndexs.deleteCharAt(len);
                            }
                        }*/
                    }
                    confirmDeleteDialog(new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                                showDialog(DIALOG_REFRESH);
                            new Thread(new Runnable() {
                                public void run() {
                                    deleteFromSim(list);
                                }
                            }, "AuroraMultiSimManageActivity").start();
                            dialog.dismiss();
                        }
                    }, mSelectIds.size() == mAdapter.getCount() ? R.string.confirm_delete_all_SIM_messages : R.string.confirm_delete_selected_SIM_message);

                break;
            default:
                break;
            }
        }
    };    
    
    private void initActionBatchMode() {
        // Aurora xuyong 2016-01-14 modified for aurora 2.0 new feature start
        setAuroraBottomBarMenuCallBack(mAuroraMenuCallBack); // 必须写在布局前
        // Aurora xuyong 2016-01-14 modified for aurora 2.0 new feature end
        mActionBar.initActionBottomBarMenu(R.menu.aurora_sim_list_menu, 2);
        mActionBar.showActionBarDashBoard();
        mAuroraMenu = mActionBar.getActionBarMenu();
        mBottomAuroraMenu = mActionBar.getAuroraActionBottomBarMenu();
        initActionBatchHandler();
        mAuroraMenu.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                onBottomMenuDismiss();
            }
        });
    }
    
    private void onBottomMenuDismiss() {
        if (mAdapter != null) {
             mAdapter.notifyDataSetChanged();
         }
         if (mSimList != null) {
             mSimList.auroraSetNeedSlideDelete(true);
             mSimList.auroraEnableSelector(true);
         }
         if (mAdapter != null) {
             mAdapter.showCheckBox(false);
             mAdapter.setCheckBoxAnim(false);
             mAdapter.updateAllCheckBox(0);
         }
         if (mActionBatchHandler != null) {
             mActionBatchHandler.destroyAction();
         }
         if (mAdapter != null) {
             mAdapter.notifyDataSetChanged();
         }
         AuroraMultiSimManageActivity.this.setMenuEnable(true);
         if (mActionBatchHandler != null) {
             mActionBatchHandler = null;
         }
         if (mThreadsMap != null) {
             mThreadsMap.clear();
         }
    }
    
    protected void initActionBatchHandler() {
        mActionBatchHandler = new AuroraActionBatchHandler<Integer>(this, mActionBar) {
            
            @Override
            public void enterSelectionMode(boolean autoLeave, Integer itemPressing) {
                mSimList.auroraSetNeedSlideDelete(false);
                AuroraMultiSimManageActivity.this.setMenuEnable(false);
                mSimList.auroraEnableSelector(false);
                mAdapter.showCheckBox(true);
                mAdapter.setCheckBoxAnim(true);
                super.enterSelectionMode(autoLeave, itemPressing);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public Set getDataSet() {
                // TODO Auto-generated method stub
                Set<Integer> dataSet = new HashSet<Integer>(mThreadsMap.size());
                for(int i = 0; i < mThreadsMap.size(); i++)
                    dataSet.add(mThreadsMap.get(i));
                return dataSet;
            }
            
            @Override
            public void leaveSelectionMode() {
                if (mAuroraMenu != null) {
                    mActionBar.setShowBottomBarMenu(false);
                    mActionBar.showActionBarDashBoard();
                    onBottomMenuDismiss();
                }
            }

            @Override
            public void updateUi() {
                mSelectCount = null != getSelected() ? getSelected().size() : 0;
                mBottomAuroraMenu.setBottomMenuItemEnable(1, mSelectCount == 0 ? false : true);
                mBottomAuroraMenu.setBottomMenuItemEnable(2, mSelectCount == 0 ? false : true);
            }

            @Override
            public void updateListView(int allShow) {
                mAdapter.updateAllCheckBox(allShow);
                mAdapter.notifyDataSetChanged();
            }
            
            public void bindToAdapter(GnSelectionManager<Integer> selectionManager) {
                // TODO Auto-generated method stub
                mAdapter.setSelectionManager(selectionManager);
                // Aurora xuyong 2014-06-11 added for bug #5592 start
                if (selectionManager != null) {
                    selectionManager.setAdapter(mAdapter);
                }
                // Aurora xuyong 2014-06-11 added for bug #5592 end
            }
        };
    }
    
    private void updateState(int state) {
        Log.d(TAG, "updateState, state = "+ state);            
        if (mState == state) {
            return;
        }

        mState = state;
        switch (state) {
            case SHOW_LIST:
                mEmptyTextView.setVisibility(View.GONE);
                mSimList.setVisibility(View.VISIBLE);
                if(mActionBar.getItem(ACTION_BATCH) == null) {
                    addAuroraActionBarItem(AuroraActionBarItem.Type.Edit, ACTION_BATCH);
                    mActionBar.setOnAuroraActionBarListener(mActionBarItemClickListener);
                }
                break;
            case SHOW_EMPTY:
                mSimList.setVisibility(View.GONE);
                if(mActionBar.getItem(ACTION_BATCH) != null) {
                    mActionBar.removeItem(ACTION_BATCH);
                    mActionBar.setOnAuroraActionBarListener(null);
                }
                mEmptyTextView.setVisibility(View.VISIBLE);
                break;
            case SHOW_BUSY:
                mSimList.setVisibility(View.GONE);
                mEmptyTextView.setVisibility(View.GONE);
                showDialog(DIALOG_REFRESH);
                break;
            default:
                Log.e(TAG, "Invalid State");
        }
    }
    
    private OnAuroraActionBarItemClickListener mActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
        public void onAuroraActionBarItemClicked(int itemId) {
            switch (itemId) {
            case ACTION_BATCH:
                try {
                    boolean deleteIsShow = mSimList.auroraIsRubbishOut();
                    if (deleteIsShow) {
                        mSimList.auroraOnPause();
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
                break;
            default:
                break;
            }
        }
    };

    private void initView() {
        mEmptyTextView = (TextView)findViewById(R.id.empty_message);
        mSimList = (AuroraListView)findViewById(R.id.sim_list_sim);
    }
    
    private void startQuery1() {
        if (resultList != null && resultList.size() > 0) {
            resultList.clear();
        }
        if (isFinishing()) {
            return;
        }
        if (ICC_URI1 != null) {
            try {
                isQuerying = true;
                mQueryHandler.startQuery(0, null, ICC_URI1, null, null, null, null);
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
        } else {
            startQuery2();
        }
    }
    
    private void startQuery2() {
        if (isFinishing()) {
            return;
        }
        if (ICC_URI2 != null) {
            try {
                isQuerying = true;
                mQueryHandler.startQuery(0, null, ICC_URI2, null, null, null, null);
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
        }
    }
    
    private final int mSlot1Id = 0;
    private final int mSlot2Id = 1;
    
    ArrayList<Object[][]> resultList = new ArrayList<Object[][]>();
    private Cursor convertToCursor(Object[][] result) {
       // Aurora xuyong 2014-06-10 added for aurora's multisim feature start
        if (result == null) {
            return null;
        }
       // Aurora xuyong 2014-06-10 added for aurora's multisim feature end
        MatrixCursor matrixCursor = new MatrixCursor(ICC_COLUMNS, result.length);
        for (Object[] result1Item : result) {
            matrixCursor.addRow(result1Item);
        }
        return matrixCursor;
    }
    private Cursor convertToCursor(Object[][] result1, Object[][] result2) {
      // Aurora xuyong 2014-06-10 modified for aurora's multisim feature start
        MatrixCursor matrixCursor = null;
        if (result1 != null && result2 != null) {
            matrixCursor = new MatrixCursor(ICC_COLUMNS, result1.length + result2.length);
            for (Object[] result1Item : result1) {
                matrixCursor.addRow(result1Item);
            }
            for (Object[] result2Item : result2) {
                matrixCursor.addRow(result2Item);
            }
        } else if (result1 != null) {
            matrixCursor = matrixCursor = new MatrixCursor(ICC_COLUMNS, result1.length);
            for (Object[] result1Item : result1) {
                matrixCursor.addRow(result1Item);
            }
        } else if (result2 != null) {
            matrixCursor = matrixCursor = new MatrixCursor(ICC_COLUMNS, result2.length);
            for (Object[] result2Item : result2) {
                matrixCursor.addRow(result2Item);
            }
        }
       // Aurora xuyong 2014-06-10 modified for aurora's multisim feature end
        return matrixCursor;
    }
    
    private final static String[] ICC_COLUMNS = new String[] {
        // N.B.: These columns must appear in the same order as the
        // calls to add appear in convertIccToSms.
        "service_center_address",       // getServiceCenterAddress
        "address",                      // getDisplayOriginatingAddress
        "message_class",                // getMessageClass
        "body",                         // getDisplayMessageBody
        "date",                         // getTimestampMillis
        "status",                       // getStatusOnIcc
        "index_on_icc",                 // getIndexOnIcc
        "is_status_report",             // isStatusReportMessage
        "transport_type",               // Always "sms".
        "type",                         // Always MESSAGE_TYPE_ALL.
        "locked",                       // Always 0 (false).
        "error_code",                   // Always 0
        "_id"
    };
    
    private Object[][] convertIccToSms(Cursor cursor, int simid) {
        if (cursor != null && cursor.getCount() > 0) {
            int objectNum = cursor.getCount();
            int ColumnNum = ICC_COLUMNS.length;
            Object[][] row = new Object[objectNum][ColumnNum];
            int index = 0;
            while (cursor.moveToNext()) {
                row[index][cursor.getColumnIndex(ICC_COLUMNS[0])] = cursor.getString(cursor.getColumnIndex(ICC_COLUMNS[0]));
                row[index][cursor.getColumnIndex(ICC_COLUMNS[1])] = cursor.getString(cursor.getColumnIndex(ICC_COLUMNS[1]));
                row[index][cursor.getColumnIndex(ICC_COLUMNS[2])] = cursor.getString(cursor.getColumnIndex(ICC_COLUMNS[2]));
                row[index][cursor.getColumnIndex(ICC_COLUMNS[3])] = cursor.getString(cursor.getColumnIndex(ICC_COLUMNS[3]));
                row[index][cursor.getColumnIndex(ICC_COLUMNS[4])] = cursor.getString(cursor.getColumnIndex(ICC_COLUMNS[4]));
                row[index][cursor.getColumnIndex(ICC_COLUMNS[5])] = cursor.getString(cursor.getColumnIndex(ICC_COLUMNS[5]));
                row[index][cursor.getColumnIndex(ICC_COLUMNS[6])] = cursor.getString(cursor.getColumnIndex(ICC_COLUMNS[6]));
                row[index][cursor.getColumnIndex(ICC_COLUMNS[7])] = cursor.getString(cursor.getColumnIndex(ICC_COLUMNS[7]));
                row[index][cursor.getColumnIndex(ICC_COLUMNS[8])] = cursor.getString(cursor.getColumnIndex(ICC_COLUMNS[8]));
                row[index][cursor.getColumnIndex(ICC_COLUMNS[9])] = cursor.getString(cursor.getColumnIndex(ICC_COLUMNS[9]));
                row[index][cursor.getColumnIndex(ICC_COLUMNS[10])] = cursor.getString(cursor.getColumnIndex(ICC_COLUMNS[10]));
                row[index][cursor.getColumnIndex(ICC_COLUMNS[11])] = cursor.getString(cursor.getColumnIndex(ICC_COLUMNS[11]));
                row[index][12] = simid;
                index++;
            }
            return row;
        }
        return null;
    }
}
