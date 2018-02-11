package com.android.mms.ui;

import static com.android.mms.ui.MessageListAdapter.COLUMN_ID;
import static com.android.mms.ui.MessageListAdapter.PROJECTION;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.android.internal.telephony.ITelephony;
import com.android.mms.LogTag;
import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
// Aurora xuyong 2013-12-11 added for aurora's new feature start
import com.android.mms.model.GroupItemInfoModel;
// Aurora xuyong 2013-12-11 added for aurora's new feature end
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.ui.ConversationList.BaseProgressQueryHandler;
import com.android.mms.util.ThreadCountManager;

import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraListActivity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ServiceManager;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms;
import android.telephony.SmsManager;
import android.util.Log;
// Aurora liugj 2013-09-13 modified for aurora's new feature start
import android.app.ActionBar;
// Aurora liugj 2013-09-13 modified for aurora's new feature end
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import aurora.widget.AuroraListView;
import android.widget.TextView;

import com.gionee.internal.telephony.GnITelephony;

import com.gionee.internal.telephony.GnPhone;
import gionee.telephony.GnSmsManager;


public class MultiDeleteActivity extends AuroraListActivity {
    
    public static final String TAG = "Mms/MultiDeleteActivity";
    
    private static final int MESSAGE_LIST_QUERY_TOKEN   = 9527;
    private static final int DELETE_MESSAGE_TOKEN       = 9700;
    
    private static final String FOR_MULTIDELETE         = "ForMultiDelete";
    
    private AuroraListView mMsgListView;        // AuroraListView for messages in this conversation
    public MessageListAdapter mMsgListAdapter;  // and its corresponding ListAdapter
    
    private boolean mPossiblePendingNotification;   // If the message list has changed, we may have
                                                    // a pending notification to deal with.
    private long threadId;     // Thread we are working in
    private Conversation mConversation;    // Conversation we are working in
    private BackgroundQueryHandler mBackgroundQueryHandler;
    private ThreadCountManager mThreadCountManager = ThreadCountManager.getInstance();
    
    private MenuItem mSelectAll;
    private MenuItem mCancelSelect;
    private MenuItem mDelete;
    private TextView mActionBarText;
    
    private boolean mIsSelectedAll;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multi_delete_list_screen);
        setProgressBarVisibility(false);
        
        threadId = getIntent().getLongExtra("thread_id", 0);
        if (threadId == 0) {
            Log.e("TAG", "threadId can't be zero");
            finish();
        }
        mConversation = Conversation.get(this, threadId, false);
        mMsgListView = getListView();
        setUpActionBar();
        initMessageList();
        initActivityState(savedInstanceState);
        
        mBackgroundQueryHandler = new BackgroundQueryHandler(getContentResolver());
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        mConversation.blockMarkAsRead(true);
        startMsgListQuery();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged " + newConfig);
        super.onConfigurationChanged(newConfig);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);      
        if (mMsgListAdapter != null) {
            if (mMsgListAdapter.getSelectedNumber() == mMsgListAdapter.getCount()) {
                outState.putBoolean("is_all_selected", true);
            } else if (mMsgListAdapter.getSelectedNumber() == 0) {
                return;
            } else {
                long [] checkedArray = new long[mMsgListAdapter.getSelectedNumber()];
                Iterator iter = mMsgListAdapter.getItemList().entrySet().iterator();
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
        mActionBarText.setText(getResources().getQuantityString(
            R.plurals.message_view_selected_message_count, selectNum, selectNum));
//        mSelectAll.setVisible(!mIsSelectedAll);
//        mCancelSelect.setVisible(hasSelected);
//        mDelete.setVisible(hasSelected);
        super.onPrepareOptionsMenu(menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.select_all:
                if (!mIsSelectedAll) {
                    mIsSelectedAll = true;
                    markCheckedState(mIsSelectedAll);
                    invalidateOptionsMenu();
                }
                break;
            case R.id.cancel_select:
                if (mMsgListAdapter.getSelectedNumber() > 0) {
                    mIsSelectedAll = false;
                    markCheckedState(mIsSelectedAll);
                    invalidateOptionsMenu();
                }
                break;
            case R.id.delete:
                if (mMsgListAdapter.getSelectedNumber() >= mMsgListAdapter.getCount()) {
                    ArrayList<Long> threadIds = new ArrayList<Long> (1);
                    threadIds.add(mConversation.getThreadId());
                    ConversationList.DeleteThreadListener dt = new ConversationList.DeleteThreadListener(threadIds,
                            mBackgroundQueryHandler, MultiDeleteActivity.this);
                    dt.setDeleteLockedMessage(true);
                    ConversationList.confirmDeleteThreadDialog(dt, threadIds, false, MultiDeleteActivity.this);
                } else if (mMsgListAdapter.getSelectedNumber() > 0) {
                    confirmMultiDelete();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onListItemClick(AuroraListView parent, View view, int position, long id) {
        if (view != null) {
            ((MessageListItem) view).onMessageListItemClick();
        }
    }
    
    private void initActivityState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            boolean selectedAll = savedInstanceState.getBoolean("is_all_selected");
            if (selectedAll) {
                mMsgListAdapter.setItemsValue(true, null);
                return;
            } 
            
            long [] selectedItems = savedInstanceState.getLongArray("select_list");
            if (selectedItems != null) {
                mMsgListAdapter.setItemsValue(true, selectedItems);
            }
        }
    }
    
    private void setUpActionBar() {
        // Aurora liugj 2013-09-13 modified for aurora's new feature start
        ActionBar actionBar = getActionBar();
        // Aurora liugj 2013-09-13 modified for aurora's new feature end

        ViewGroup v = (ViewGroup)LayoutInflater.from(this)
            .inflate(R.layout.multi_delete_list_actionbar, null);
        // Aurora liugj 2013-09-13 modified for aurora's new feature start
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_SHOW_TITLE);
        // Aurora liugj 2013-09-13 modified for aurora's new feature end
        ImageButton mQuit = (ImageButton) v.findViewById(R.id.cancel_button);
        mQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                MultiDeleteActivity.this.finish();
            }
        });
        
        mActionBarText = (TextView) v.findViewById(R.id.select_items);
        actionBar.setCustomView(v);
    }
    
    private void initMessageList() {
        if (mMsgListAdapter != null) {
            return;
        }

        String highlightString = getIntent().getStringExtra("highlight");
        Pattern highlight = highlightString == null
            ? null
            : Pattern.compile("\\b" + Pattern.quote(highlightString), Pattern.CASE_INSENSITIVE);

        // Initialize the list adapter with a null cursor.
        mMsgListAdapter = new MessageListAdapter(this, null, mMsgListView, true, highlight);
        mMsgListAdapter.mIsDeleteMode = true;
        mMsgListAdapter.setMsgListItemHandler(mMessageListItemHandler);
        mMsgListAdapter.setOnDataSetChangedListener(mDataSetChangedListener);
        mMsgListView.setAdapter(mMsgListAdapter);
        mMsgListView.setItemsCanFocus(false);
        mMsgListView.setVisibility(View.VISIBLE);
    }
    
    private void startMsgListQuery() {
        // Cancel any pending queries
        mBackgroundQueryHandler.cancelOperation(MESSAGE_LIST_QUERY_TOKEN);
        try {
            mBackgroundQueryHandler.postDelayed(new Runnable() {
                public void run() {
                    mBackgroundQueryHandler.startQuery(
                            MESSAGE_LIST_QUERY_TOKEN, threadId, mConversation.getUri(),
                            PROJECTION, null, null, null);
                }
            }, 50);
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(this, e);
        }
    }
    
    private void markCheckedState(boolean checkedState) {
        mMsgListAdapter.setItemsValue(checkedState, null);
        int count = mMsgListView.getChildCount();
        MessageListItem item = null;
        for (int i = 0; i < count; i++) {
            item = (MessageListItem) mMsgListView.getChildAt(i);
            item.setSelectedBackGroud(checkedState);
        }
    }
    
    /**
     * @return the number of messages that are currently selected.
     */
    private int getSelectedCount() {
        return mMsgListAdapter.getSelectedNumber();
    }
    
    @Override
    public void onUserInteraction() {
        checkPendingNotification();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            checkPendingNotification();
        }
    }
    
    private void checkPendingNotification() {
        if (mPossiblePendingNotification && hasWindowFocus()) {
            mConversation.markAsRead();
            mPossiblePendingNotification = false;
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
                mBackgroundQueryHandler.setProgressDialog(DeleteProgressDialogUtil.getProgressDialog(MultiDeleteActivity.this));
                mBackgroundQueryHandler.showProgressDialog();
                new Thread(new Runnable() {
                    public void run() {
                        Iterator iter = mMsgListAdapter.getItemList().entrySet().iterator();
                        Uri deleteSmsUri = null;
                        Uri deleteMmsUri = null;
                        String[] argsSms = new String[mMsgListAdapter.getSelectedNumber()];
                        String[] argsMms = new String[mMsgListAdapter.getSelectedNumber()];
                        int i = 0;
                        int j = 0;
                        while (iter.hasNext()) {
                            @SuppressWarnings("unchecked")
                            Map.Entry<Long, Boolean> entry = (Entry<Long, Boolean>) iter.next();
                            if (entry.getValue()) {
                                if (entry.getKey() > 0){
                                    Log.i(TAG, "sms");
                                    argsSms[i] = Long.toString(entry.getKey());
                                    Log.i(TAG, "argsSms[i]" + argsSms[i]);
                                    //deleteSmsUri = ContentUris.withAppendedId(Sms.CONTENT_URI, entry.getKey());
                                    deleteSmsUri = Sms.CONTENT_URI;
                                    i++;
                                } else {
                                    Log.i(TAG, "mms");
                                    argsMms[j] = Long.toString(-entry.getKey());
                                    Log.i(TAG, "argsMms[j]" + argsMms[j]);
                                    //deleteMmsUri = ContentUris.withAppendedId(Mms.CONTENT_URI, -entry.getKey());
                                    deleteMmsUri = Mms.CONTENT_URI;
                                    j++;
                                }
                            }
                        }
                        mBackgroundQueryHandler.setMax(
                                (deleteSmsUri != null ? 1 : 0) +
                                (deleteMmsUri != null ? 1 : 0));
                        if (deleteSmsUri != null) {
                            mBackgroundQueryHandler.startDelete(DELETE_MESSAGE_TOKEN,
                                    null, deleteSmsUri, FOR_MULTIDELETE, argsSms);
                        }
                        if (deleteMmsUri != null) {
                            mBackgroundQueryHandler.startDelete(DELETE_MESSAGE_TOKEN,
                                    null, deleteMmsUri, FOR_MULTIDELETE, argsMms);
                        }
                    }
                }).start();
            }
        });
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }
    
    private void updateSendFailedNotification() {
        final long threadId = mConversation.getThreadId();
        if (threadId <= 0)
            return;

        // updateSendFailedNotificationForThread makes a database call, so do the work off
        // of the ui thread.
        new Thread(new Runnable() {
            public void run() {
                MessagingNotification.updateSendFailedNotificationForThread(
                        MultiDeleteActivity.this, threadId);
            }
        }, "updateSendFailedNotification").start();
    }
    
    private final Handler mMessageListItemHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String type;
            switch (msg.what) {
                case MessageListItem.ITEM_CLICK: {
                    //add for multi-delete
                    // Aurora xuyong 2013-12-11 added for aurora's new feature start
                     GroupItemInfoModel sgif = ((MessageItem) msg.obj).mGIIF;
                     ArrayList<Long> sglist = new ArrayList<Long>();
                     // Aurora xuyong 2013-12-13 modified for aurora's new feature start
                     if (sgif != null) {
                         ArrayList<Long> sgifi = sgif.getIds();
                        sglist = sgifi;
                     // Aurora xuyong 2013-12-13 modified for aurora's new feature end     
                     } else {
                         sglist.add(new Long(msg.arg1));
                     }
                    mMsgListAdapter.changeSelectedState(sglist);
                    // Aurora xuyong 2013-12-11 added for aurora's new feature end
                    if (mMsgListAdapter.getSelectedNumber() > 0) {
                        //mDeleteButton.setEnabled(true);
                        if (mMsgListAdapter.getSelectedNumber() == mMsgListAdapter.getCount()) {
                            mIsSelectedAll = true;
                            invalidateOptionsMenu();
                            return;
                        }
                    } else {
                        //mDeleteButton.setEnabled(false);
                    }
                    mIsSelectedAll = false;
                    invalidateOptionsMenu();
                    return;
                }
                default:
                    Log.w(TAG, "Unknown message: " + msg.what);
                    return;
            }
        }
    };

    private final class BackgroundQueryHandler extends BaseProgressQueryHandler {
        public BackgroundQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            switch (token) {
                case MESSAGE_LIST_QUERY_TOKEN:
                    if (cursor == null) {
                        Log.w(TAG, "onQueryComplete, cursor is null.");
                        return;
                    }
                    // check consistency between the query result and
                    // 'mConversation'
                    long tid = (Long) cookie;

                    if (tid != mConversation.getThreadId()) {
                        Log.d(TAG, "onQueryComplete: msg history query result is for threadId " + tid
                            + ", but mConversation has threadId " + mConversation.getThreadId()
                            + " starting a new query");
                        startMsgListQuery();
                        return;
                    }

                    if (mMsgListAdapter.mIsDeleteMode) {
                        mMsgListAdapter.initListMap(cursor);
                    }

                    mMsgListAdapter.changeCursor(cursor);
                    mConversation.blockMarkAsRead(false);
                    return;
            }
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            Intent mIntent = new Intent();
            switch (token) {
                case ConversationList.DELETE_CONVERSATION_TOKEN:
                    try {
                        if(GnPhone.phone != null) {
                            if(GnPhone.isTestIccCard()) {
                                Log.d(TAG, "All messages has been deleted, send notification...");
                                GnSmsManager.getDefault().setSmsMemoryStatus(true);
                            }
                        } else {
                            Log.d(TAG, "Telephony service is not available!");
                        }
                    } catch(Exception ex) {
                        Log.e(TAG, "" + ex.getMessage());
                    }
                    // Update the notification for new messages since they
                    // may be deleted.
                    MessagingNotification.nonBlockingUpdateNewMessageIndicator(
                            MultiDeleteActivity.this, false, false);
                    // Update the notification for failed messages since they
                    // may be deleted.
                    updateSendFailedNotification();
                    MessagingNotification.updateDownloadFailedNotification(MultiDeleteActivity.this);
                    if (progress()) {
                        dismissProgressDialog();
                    }
                    mIntent.putExtra("delete_all", true);
                    break;
                case DELETE_MESSAGE_TOKEN:
                    Log.d(TAG, "onDeleteComplete(): before update mConversation, ThreadId = " + mConversation.getThreadId());
                    mConversation = Conversation.upDateThread(MultiDeleteActivity.this, mConversation.getThreadId(), false);
                    mThreadCountManager.isFull(threadId, MultiDeleteActivity.this, 
                            ThreadCountManager.OP_FLAG_DECREASE);
                    // Update the notification for new messages since they
                    // may be deleted.
                    MessagingNotification.nonBlockingUpdateNewMessageIndicator(
                            MultiDeleteActivity.this, false, false);
                    // Update the notification for failed messages since they
                    // may be deleted.
                    updateSendFailedNotification();
                    MessagingNotification.updateDownloadFailedNotification(MultiDeleteActivity.this);
                    Log.d(TAG, "onDeleteComplete(): MessageCount = " + mConversation.getMessageCount() + 
                            ", ThreadId = " + mConversation.getThreadId());
                    if (progress()) {
                        dismissProgressDialog();
                    }
                    mIntent.putExtra("delete_all", false);
                    break;
            }
            setResult(RESULT_OK, mIntent);
            finish();
        }
    }
    
    private final MessageListAdapter.OnDataSetChangedListener mDataSetChangedListener = new MessageListAdapter.OnDataSetChangedListener() {
        public void onDataSetChanged(MessageListAdapter adapter) {
            mPossiblePendingNotification = true;
        }

        public void onContentChanged(MessageListAdapter adapter) {
            Log.d(TAG, "MessageListAdapter.OnDataSetChangedListener.onContentChanged");
            startMsgListQuery();
        }
    };
}
