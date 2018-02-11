package com.aurora.mms.ui;
// Aurora xuyong 2014-07-18 created for bug #6626
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
// Aurora xuyong 2014-07-21 added for reject feature start
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
// Aurora xuyong 2014-07-21 added for reject feature end
    // Aurora yudingmin 2014-09-04 added for optimize start
import android.provider.Telephony.Sms.Conversations;
    // Aurora yudingmin 2014-09-04 added for optimize start
import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
    // Aurora yudingmin 2014-09-04 added for optimize start
import android.content.Context;
    // Aurora yudingmin 2014-09-04 added for optimize start
// Aurora xuyong 2014-07-31 added for bug #7045 start
import android.database.ContentObserver;
// Aurora xuyong 2014-07-31 added for bug #7045 end
import android.database.Cursor;
// Aurora xuyong 2014-07-31 added for bug #7074 start
import android.database.CursorIndexOutOfBoundsException;
// Aurora xuyong 2014-07-31 added for bug #7074 end
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
    // Aurora yudingmin 2014-09-04 added for optimize start
import android.widget.AbsListView;
    // Aurora yudingmin 2014-09-04 added for optimize end
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
// Aurora xuyong 2014-07-22 added for bug #6626 end
import android.widget.Toast;
// Aurora xuyong 2014-07-22 added for bug #6626 end

import gionee.provider.GnTelephony.Threads;
// Aurora xuyong 2014-07-31 added for bug #7045 start
import android.provider.Telephony.MmsSms;
// Aurora xuyong 2014-07-31 added for bug #7045 end

import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.ui.ConversationListAdapter;
import com.android.mms.util.GnSelectionManager;
import com.aurora.mms.countmanage.TotalCount;
import com.aurora.mms.ui.ConvFragment.BaseProgressQueryHandler;
import com.aurora.mms.util.Utils;

import aurora.app.AuroraActivity;
import aurora.app.AuroraProgressDialog;

import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;

import android.util.Log;

public class AuroraRejConvOperActivity extends AuroraActivity {
    
    private static final int THREAD_LIST_QUERY_TOKEN  = 1701;
    // Aurora yudingmin 2014-09-04 added for optimize start
    private final int INIT_NONE = 0;
    private final int INIT_INIT_ITEM_COUNT_DONE = 1;
    private final int INIT_ALL_DONE = 2;
    private int mInitStatu = INIT_NONE;
    // Aurora yudingmin 2014-09-04 added for optimize end

    private static final int INIT_ITEM_COUNT = 10;
    // Aurora yudingmin 2014-09-04 added for optimize start
    private static final int POST_ITEM_COUNT = 100;
    private  int mInitedCount = 0;
    private boolean mScrollToBottom = false;
    // Aurora yudingmin 2014-09-04 added for optimize end
    
    private static ThreadListQueryHandler mQueryHandler = null;
    
    private static Map<Integer, Conversation> mAllSelectedConvsCache = new HashMap<Integer, Conversation> ();
    
    private AuroraActionBar mAuroraActionBar = null;
    
    private static AuroraListView mListView;
    private LinearLayout mEmptyView;
    private TextView mEmptyTextView;
    
    private AuroraMenu mAuroraMenu;
    private AuroraMenu mBottomAuroraMenu;
    
    private ConversationListAdapter mListAdapter;
    
    AuroraProgressDialog mProgressDialog = null;

    // Aurora yudingmin 2014-09-04 deleted for optimize start
//    private Cursor mCursor = null;
     // Aurora yudingmin 2014-09-04 deleted for optimize end
    
    private static AuroraActionBatchHandler<Long> mActionBatchHandler = null;
    private Map<Integer, Long> mThreadsMap = new HashMap<Integer, Long>();
    
    protected void onCreate(Bundle bundle) {
         super.onCreate(bundle);
         mQueryHandler = new ThreadListQueryHandler(this.getContentResolver());
         setAuroraContentView(R.layout.aurora_conversation_list_reject_screen, AuroraActionBar.Type.Dashboard);
         initProgressAddingDialog();
         initAuroraActionBar();
         init();
         startMsgListQuery();
        // Aurora xuyong 2014-07-31 added for bug #7045 start
         //this.getContentResolver().registerContentObserver(Conversation.sAllThreadsUri, true, mConvaObserver);
         this.getContentResolver().registerContentObserver(MmsSms.CONTENT_URI, true, mConvaObserver);
        // Aurora xuyong 2014-07-31 added for bug #7045 end
    }
    // Aurora xuyong 2014-07-31 added for bug #7045 start
    private ContentObserver mConvaObserver = new ContentObserver(new Handler()) { 

        @Override
        public void onChange(boolean selfChange) { 
             super.onChange(selfChange);
             // Aurora xuyong 2014-09-22 modified for uptimize start
             if (!mIsAddingToBlack) {
                    startMsgListQuery();
             }
             // Aurora xuyong 2014-09-22 modified for uptimize end
        }   

    };
    
    @Override
    public void onRestart() {
        super.onRestart();
        //startMsgListQuery();
    }
    // Aurora xuyong 2014-07-31 added for bug bad token start
    @Override
    public void onStop() {
        super.onStop();
    }
    // Aurora xuyong 2014-07-31 added for bug bad token end
    // Aurora xuyong 2014-07-31 added for bug #7045 end
    private void initProgressAddingDialog() {
        mProgressDialog = new AuroraProgressDialog(this);
        mProgressDialog.setTitle(R.string.aurora_black_adding);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
    }
    
    private void initAuroraActionBar() {
        mAuroraActionBar = this.getAuroraActionBar();
        if (mAuroraActionBar != null) {
            ((TextView)mAuroraActionBar.getOkButton()).setText(R.string.select_all);
            // Aurora xuyong 2016-03-05 added for bug #20741 start
            TextView middleTextView = mAuroraActionBar.getMiddleTextView();
            middleTextView.setText(this.getResources().getString(R.string.noItemSelected));
            // Aurora xuyong 2016-03-05 added for bug #20741 end
        }
        // Aurora xuyong 2016-01-14 modified for aurora 2.0 new feature start
        this.setAuroraBottomBarMenuCallBack(mAuroraMenuCallBack);
        // Aurora xuyong 2016-01-14 modified for aurora 2.0 new feature end
        mAuroraActionBar.initActionBottomBarMenu(R.menu.aurora_reject_list_menu_delete, 1);
        mAuroraMenu = mAuroraActionBar.getActionBarMenu();
        mBottomAuroraMenu = mAuroraActionBar.getAuroraActionBottomBarMenu();
    }
    
    private void init() {
        mListView = (AuroraListView)findViewById(R.id.reject_list);
        mListView.auroraSetUseNewSelectorLogical(false);
        mListView.setDividerHeight(0);
        mListView.auroraSetFrameNumbers(6);
        mListView.auroraSetNeedSlideDelete(false);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                // TODO Auto-generated method stub
                // Aurora xuyong 2016-01-29 modified for aurora 2.0 new feature start
                // Aurora xuyong 2016-03-03 modified for bug #20197 start
                AuroraCheckBox mCheckBox = (AuroraCheckBox) arg1.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
                // Aurora xuyong 2016-03-03 modified for bug #20197 end
                // Aurora xuyong 2016-01-29 modified for aurora 2.0 new feature start
                if (mCheckBox == null) {
                    return;
                }
                boolean isChecked = mCheckBox.isChecked();
                mCheckBox.auroraSetChecked(!isChecked, true);
             // Aurora xuyong 2014-07-31 modified for bug #7045 start
                if (mActionBatchHandler != null) {
                    mActionBatchHandler.getSelectionManger().toggle(arg3);
                }
             // Aurora xuyong 2014-07-31 modified for bug #7045 end
                final int pos = arg2;
    // Aurora yudingmin 2014-09-04 added for optimize start
                final Cursor cursor = mListAdapter.getCursor();
                final Context context = AuroraRejConvOperActivity.this.getApplicationContext();
    // Aurora yudingmin 2014-09-04 added for optimize end
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
    // Aurora yudingmin 2014-09-04 modified for optimize start
                        if (cursor == null || cursor.isClosed()) {
                            return;
                        }
                        if (cursor.moveToPosition(pos)) {
                       // Aurora xuyong 2014-08-01 modified for bug #7040 start
                            Conversation conv = null;
                            try {
                                conv = Conversation.from(context, cursor);
                            } catch (CursorIndexOutOfBoundsException e) {
                                
                            }
    // Aurora yudingmin 2014-09-04 modified for optimize end
                       // Aurora xuyong 2014-08-01 modified for bug #7040 end
                            operateCons(pos, conv);
                        }
                    }
                    
                }).start();
            }
        });

    // Aurora yudingmin 2014-09-04 added for optimize start
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
            
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {
                // TODO Auto-generated method stub
                if (firstVisibleItem + visibleItemCount > totalItemCount - 2 && totalItemCount > 0) {
                    if(mInitedCount > INIT_ITEM_COUNT && mInitStatu == INIT_INIT_ITEM_COUNT_DONE && mInitedCount > mListAdapter.getCount() + POST_ITEM_COUNT){
                        mListAdapter.notifyCountSetChanged(mInitedCount);
                    } else {
                        mScrollToBottom = true;
                    }
                }
            }
        });
    // Aurora yudingmin 2014-09-04 added for optimize end
        // Aurora xuyong 2016-03-03 modified for bug #20197 start
        mListAdapter = new ConversationListAdapter(this, null, true);
        // Aurora xuyong 2016-03-03 modified for bug #20197 end
        mListAdapter.showCheckBox(true);
        
        mEmptyView = (LinearLayout)findViewById(R.id.aurora_conversation_reject_empty);
        
        mEmptyTextView = (TextView)findViewById(R.id.aurora_reject_empty);
    }
    
    private void operateCons(int pos, Conversation conv) {
        if (mAllSelectedConvsCache != null) {
            Integer index = new Integer(pos);
            if (mAllSelectedConvsCache.containsKey(index)) {
                mAllSelectedConvsCache.remove(index);
            } else {
                mAllSelectedConvsCache.put(index, conv);
            }
        }
    }
    
    private void operateAllConvs(Cursor cursor) {
    // Aurora yudingmin 2014-09-04 added for optimize start
        Context context = AuroraRejConvOperActivity.this.getApplicationContext();
    // Aurora yudingmin 2014-09-04 added for optimize end
        if (mAllSelectedConvsCache != null && cursor != null && !cursor.isClosed()) {
            cursor.moveToPosition(-1);
            int position = 0;
          // Aurora xuyong 2014-07-31 added for bug #7074 start
            while (cursor.moveToNext() && !cursor.isBeforeFirst() && !cursor.isAfterLast()) {
          // Aurora xuyong 2014-07-31 added for bug #7074 end
                Integer index = new Integer(position);
             // Aurora xuyong 2014-08-01 modified for bug #7040 start
                Conversation conv = null;
                try {
    // Aurora yudingmin 2014-09-04 modified for optimize start
                    conv = Conversation.from(context, cursor);
    // Aurora yudingmin 2014-09-04 modified for optimize start
                } catch (CursorIndexOutOfBoundsException e) {
                    
                }
             // Aurora xuyong 2014-08-01 modified for bug #7040 end
                if (mAllSelectedConvsCache.containsKey(index)) {
                    // do nothing!
                } else {
                    mAllSelectedConvsCache.put(index, conv);
                }
                position++;
            }
        }
    }
    
    private void startMsgListQuery() {
        try {
           // Aurora xuyong 2014-10-23 modified for privacy feature start
            Conversation.startQueryForAll(mQueryHandler, THREAD_LIST_QUERY_TOKEN, "fromReject");
           // Aurora xuyong 2014-10-23 modified for privacy feature end
        } catch (SQLiteException e) {
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        mListView.auroraOnResume();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        mListView.auroraOnPause();
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);
    }
    
    @Override
    public void onDestroy() {
        if (null != mActionBatchHandler && mActionBatchHandler.isInSelectionMode()) {
            mActionBatchHandler.leaveSelectionMode();
        }
        if (mListAdapter != null && mListAdapter.getCursor() != null) {
            mListAdapter.getCursor().close();
        }
        if (mConvaObserver != null) {
            this.getContentResolver().unregisterContentObserver(mConvaObserver);
            mConvaObserver = null;
        }
        super.onDestroy();
        Utils.removeInstance(this);
    }
    
    private final class ThreadListQueryHandler extends BaseProgressQueryHandler {
        private final String CONV_TAG = "AuroraRejConvOperActivity";
        private boolean lockFlag = false;
        private boolean starFlag = false;

        public ThreadListQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            switch (token) {
            case THREAD_LIST_QUERY_TOKEN:
             // Aurora xuyong 2014-07-22 modified for bug #6735 start
    // Aurora yudingmin 2014-09-04 modified for optimize start
//                mCursor = needShowGroupConv(cursor);
             // Aurora xuyong 2014-07-22 modified for bug #6735 end
             // Aurora xuyong 2014-08-07 modified for bug #7303 start
                final Cursor transCursor = needShowGroupConv(cursor);
             // Aurora xuyong 2014-08-07 modified for bug #7303 end
                if (mInitStatu != INIT_ALL_DONE) {
                    if (mSingleThreadExecutor == null) {
                        Message msg = mHandler.obtainMessage(CHANGE_MC1_CURSOR);
                        msg.sendToTarget();
                        return;
                    }
                    final Context context = AuroraRejConvOperActivity.this.getApplicationContext();
                    mSingleThreadExecutor.execute(new Runnable() {
                            
                            @Override
                            public void run() {
                                int initCount = INIT_ITEM_COUNT;
                                if (transCursor == null || transCursor.isClosed()) {
                              // Aurora xuyong 2014-07-21 added for reject feature start
                                    Message msg = mHandler.obtainMessage(CHANGE_MC1_CURSOR);
                              // Aurora xuyong 2014-07-22 added for bug #6735 start
                                    msg.obj = transCursor;
                              // Aurora xuyong 2014-07-22 added for bug #6735 end
                                    msg.sendToTarget();
                              // Aurora xuyong 2014-07-21 added for reject feature end
                                    return;
                                }
                                int count = transCursor.getCount();
                                int columnCount = transCursor.getColumnCount();
                                MatrixCursor mc1 = new MatrixCursor(Conversation.ALL_THREADS_PROJECTION, initCount);
                           // Aurora xuyong 2014-07-31 added for bug #7074 start
                                if (transCursor.moveToFirst() && !transCursor.isBeforeFirst() && !transCursor.isAfterLast()) {
                           // Aurora xuyong 2014-07-31 added for bug #7074 end
                                    Object[] firstItemDetail = new Object[columnCount];
                                    for (int i = 0; i < columnCount; i++) {
                                 // Aurora xuyong 2014-07-31 modified for bug #7074 start
                                        try {
                                            firstItemDetail[i] = transCursor.getString(i);
                                        } catch (CursorIndexOutOfBoundsException e) {
                                            Log.e("CIOEC", "current position = " + i + ", transCursor.getColumnCount() = " + transCursor.getColumnCount());
                                        }
                                 // Aurora xuyong 2014-07-31 modified for bug #7074 end
                                    }
                                    mc1.addRow(firstItemDetail);
                                    initCount--;
                              // Aurora xuyong 2014-07-31 modified for bug #7074 start
                                    while (transCursor.moveToNext() && initCount > 0 && !transCursor.isBeforeFirst() && !transCursor.isAfterLast()) {
                              // Aurora xuyong 2014-07-31 modified for bug #7074 end
                                        Object[] itemDetail = new Object[columnCount];
                                        for (int i = 0; i < columnCount; i++) {
                                     // Aurora xuyong 2014-07-31 modified for bug #7074 start
                                            try {
                                                itemDetail[i] = transCursor.getString(i);
                                            } catch (CursorIndexOutOfBoundsException e) {
                                                Log.e("CIOEC", "current position = " + i + ", transCursor.getColumnCount() = " + transCursor.getColumnCount());
                                            }
                                     // Aurora xuyong 2014-07-31 modified for bug #7074 end
                                        }
                                        mc1.addRow(itemDetail);
                                        initCount--;
                                    }
                              // Aurora xuyong 2014-07-31 modified for bug #7074 start
                                    if (mc1.moveToFirst() && !mc1.isBeforeFirst() && !mc1.isAfterLast()) {
                              // Aurora xuyong 2014-07-31 modified for bug #7074 end
                                 // Aurora xuyong 2014-08-01 modified for bug #7040 start
                                        try {
                                            Conversation.from(context, mc1);
                                        } catch (CursorIndexOutOfBoundsException e) {
                                            
                                        }
                                 // Aurora xuyong 2014-08-01 modified for bug #7040 end
                                 // Aurora xuyong 2014-07-31 modified for bug #7074 start
                                        while(mc1.moveToNext() && !mc1.isBeforeFirst() && !mc1.isAfterLast()) {
                                 // Aurora xuyong 2014-07-31 modified for bug #7074 end
                                     // Aurora xuyong 2014-08-01 modified for bug #7040 start
                                            try {
                                                Conversation.from(context, mc1);
                                            } catch (CursorIndexOutOfBoundsException e) {
                                                
                                            }
                                     // Aurora xuyong 2014-08-01 modified for bug #7040 end
                                        }
                                    }
                                }
                                transCursor.moveToFirst();
                                Message msg = mHandler.obtainMessage(CHANGE_MC1_CURSOR);
                                msg.obj = transCursor;
                                msg.sendToTarget();
                                if(mc1 != null){
                                    mc1.close();
                                }
                            }
                            
                        });
                } else {
                // Aurora xuyong 2014-09-22 modified for bug #8401 start
                    if (mListAdapter != null) {
                        mListAdapter.changeCountBeforeDataChanged(transCursor == null?0:transCursor.getCount());
                        mListAdapter.changeCursor(transCursor);
                        operateAfterChangeCursor(transCursor);
                    }
                // Aurora xuyong 2014-09-22 modified for bug #8401 end
                }
    // Aurora yudingmin 2014-09-04 modified for optimize end
                break;
            default:
            }
        }
    }
    // Aurora xuyong 2014-07-22 added for bug #6626 start
    private boolean mFirstClick = true;
   // Aurora xuyong 2014-07-22 added for bug #6626 end
    private OnAuroraMenuItemClickListener mAuroraMenuCallBack = new OnAuroraMenuItemClickListener() {

        @Override
        public void auroraMenuItemClick(int itemId) {
            switch (itemId) {
            case R.id.aurora_list_menu_reject:
             // Aurora xuyong 2014-07-22 modified for bug #6626 start
                int selectedCount = mAllSelectedConvsCache.size();
                if (mAllSelectedConvsCache != null && selectedCount > 100) {
                    Toast.makeText(AuroraRejConvOperActivity.this, R.string.aurora_add_blackname_more_toast, Toast.LENGTH_SHORT).show();
                    return;
                }
                // Aurora xuyong 2014-03-05 modified for aurora's new feature start
                new TotalCount(AuroraRejConvOperActivity.this, TotalCount.REJECT_MODULE_kEY, TotalCount.ACTION_REJECT, selectedCount).countData();
                // Aurora xuyong 2014-03-05 modified for aurora's new feature end
                if (mFirstClick) {
                    mFirstClick = false;
                    mProgressDialog.show();
                // Aurora xuyong 2014-09-22 modified for uptimize start
                    addContactToBlackList();
                // Aurora xuyong 2014-09-22 modified for uptimize end
                }
             // Aurora xuyong 2014-07-22 modified for bug #6626 end
                break;
            default:
                break;
            }
        }
    };
    
    // Aurora xuyong 2014-07-21 added for reject feature start
    private static final HashMap<Uri, ContentValues> addRejectInfoCache = new HashMap<Uri, ContentValues>();
    // Aurora xuyong 2014-07-21 added for reject feature end
    // Aurora xuyong 2014-09-22 added for uptimize start
    boolean mIsAddingToBlack = false;
    // Aurora xuyong 2014-09-22 added for uptimize end
    private void addContactToBlackList() {
       // Aurora xuyong 2014-09-10 deleted for uptimize start
        //final Iterator iter = mAllSelectedConvsCache.entrySet().iterator();
       // Aurora xuyong 2014-09-10 deleted for uptimize end
      // Aurora xuyong 2014-09-22 modified for uptimize start
        mIsAddingToBlack = true;
        mSingleThreadExecutor2.execute(new Runnable() {
      // Aurora xuyong 2014-09-22 modified for uptimize end
            
            @Override
            public void run() {
             // Aurora xuyong 2014-09-10 modified for uptimize start
                synchronized (mAllSelectedConvsCache) {
                    Iterator iter = mAllSelectedConvsCache.entrySet().iterator();
                    while (iter.hasNext()) {
                        ContentResolver resolver = AuroraRejConvOperActivity.this.getContentResolver();
                        Conversation conv = ((Map.Entry<Integer, Conversation>)(iter.next())).getValue();
                        ContactList contacts = conv.getRecipients();
                        ContentValues values = new ContentValues();
                        for (Contact contact : contacts) {
                            values.put("isBlack", 1);
                            String number = contact.getNumber();
                            values.put("number", number);
                            String lable = Utils.getLable(AuroraRejConvOperActivity.this, number, values);
                            values.put("lable", lable);
                            values.put("black_name", contact.getNameOnly());
                            values.put("reject", 3);
                            resolver.insert(Utils.BLACK_URI, values);
                        }
                        long threadId = conv.getThreadId();
                        Uri threadUri = Uri.parse(
                                "content://mms-sms/conversations_reject/" + threadId);
                        ContentValues threadValues = new ContentValues();
                        threadValues.put("reject", 1);
                        threadValues.put("concurrent_resume", 1);
                    // Aurora xuyong 2014-07-21 modified for reject feature start
                        addRejectInfoCache.put(threadUri, threadValues);
                    // Aurora xuyong 2014-07-21 modified for reject feature end
                    }
                // Aurora xuyong 2014-09-22 modified for uptimize start
                    mIsAddingToBlack = false;
                // Aurora xuyong 2014-09-22 modified for uptimize end
                    Message msg = mHandler.obtainMessage(FINISH_ACTIVITY);
                    msg.sendToTarget();
                }
             // Aurora xuyong 2014-09-10 modified for uptimize end
            }
            
        });
    }
    // Aurora xuyong 2014-07-21 added for reject feature start
   // Aurora xuyong 2014-09-22 modified for uptimize start
    private final ExecutorService mSingleThreadExecutor2 = Executors.newSingleThreadExecutor();
    private void updateRejectCache() {
        if (addRejectInfoCache == null || addRejectInfoCache.size() <= 0) {
   // Aurora xuyong 2014-09-22 modified for uptimize end
            return;
        }
       // Aurora xuyong 2014-07-31 modified for bug #7074 start
        final ContentResolver resolver = this.getContentResolver();
       // Aurora xuyong 2014-07-31 modified for bug #7074 end
       // Aurora xuyong 2014-09-22 modified for uptimize start
        mSingleThreadExecutor2.execute(new Runnable() {
       // Aurora xuyong 2014-09-22 modified for uptimize end

            @Override
            public void run() {
                // TODO Auto-generated method stub
             // Aurora xuyong 2014-09-22 modified for uptimize start
                synchronized (addRejectInfoCache) {
                    Iterator iter = addRejectInfoCache.entrySet().iterator();
                    Map.Entry<Uri, ContentValues> entry = null;
                    while (iter.hasNext()) {
                        entry = (Map.Entry<Uri, ContentValues>)(iter.next());
                        resolver.update(entry.getKey(), entry.getValue(), null, null);
                    }
                    addRejectInfoCache.clear();
                }
             // Aurora xuyong 2014-09-22 modified for uptimize end
            }
            
        });
       // Aurora xuyong 2014-09-22 deleted for uptimize start
        //map.clear();
       // Aurora xuyong 2014-09-22 deleted for uptimize start
    }
    // Aurora xuyong 2014-07-21 added for reject feature end
    
    private static final int FINISH_ACTIVITY = 0x1;
    private static final int CHANGE_MC1_CURSOR = 0x2;
    // Aurora xuyong 2014-08-01 added for bug #7038 start
    ExecutorService mSingleThreadExecutor = Executors.newSingleThreadExecutor();
   // Aurora xuyong 2014-08-01 added for bug #7038 end
    private Handler mHandler = new Handler() {
        
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case ConvFragment.SELECT_ALL:
                    clearSelectedConvCache();
                // Aurora xuyong 2014-08-01 modified for bug #7038 start
                    if (mSingleThreadExecutor != null) {
                        mSingleThreadExecutor.execute(new Runnable() {
                            
                            @Override
                            public void run() {
                                operateAllConvs(mListAdapter.getCursor());
                            }
    
                        });
                    }
                // Aurora xuyong 2014-08-01 modified for bug #7038 end
                    break;
                case ConvFragment.DIS_SELECT_ALL:
                    clearSelectedConvCache();
                    break;
                case FINISH_ACTIVITY:
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                    clearSelectedConvCache();
                    AuroraRejConvOperActivity.this.finish();
                // Aurora xuyong 2014-07-21 added for reject feature start
                // Aurora xuyong 2014-09-22 modified for uptimize start
                    updateRejectCache();
                // Aurora xuyong 2014-09-22 modified for uptimize end
                // Aurora xuyong 2014-07-21 added for reject feature end
                    break;
                 case CHANGE_MC1_CURSOR:
    // Aurora yudingmin 2014-09-04 modified for optimize start
                     Cursor allCursor = (Cursor)(msg.obj);
                // Aurora xuyong 2014-09-02 modified for reject start
//                    if (mListAdapter != null && initResult != null) {
//                        mListAdapter.changeCountBeforeDataChanged(initResult.getCount());
                // Aurora xuyong 2014-09-02 modified for reject end
//                        mListAdapter.changeCursor(initResult);
//                    }
                    if (allCursor == null || allCursor.isClosed()) {
                   // Aurora xuyong 2014-07-22 added for bug #6735 start
                        mEmptyTextView.setText(R.string.no_conversations);
                        mEmptyView.setVisibility(View.VISIBLE);
                        mListView.setVisibility(View.GONE);
                   // Aurora xuyong 2014-07-22 added for bug #6735 end
                        return;
                    }
                    if(mInitStatu == INIT_NONE){
                        mInitStatu = INIT_INIT_ITEM_COUNT_DONE;
                        mListAdapter.changeCountBeforeDataChanged(INIT_ITEM_COUNT);
                        if(allCursor.getCount() > INIT_ITEM_COUNT){
                            new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            loadAllContact();
                                        }
                                        
                                    }).start();
                        } else {
                            mInitStatu = INIT_ALL_DONE;
                        }
                    }
                    mListView.auroraSetRubbishBackNoAnim();
                    mListAdapter.changeCursor(allCursor);
                    operateAfterChangeCursor(allCursor);
    // Aurora yudingmin 2014-09-04 modified for optimize start
                    break;
                default:
                    break;
            }
        }
        
    };
    
    // Aurora yudingmin 2014-09-04 added for optimize start
    private void loadAllContact(){
        mInitedCount = 0;
        if(!isFinishing()){
            Context context = getApplicationContext();
            String selection = null;
            Cursor mc2 = context.getContentResolver().query(Conversation.sAllThreadsUri, Conversation.ALL_THREADS_PROJECTION, selection, null, Conversations.DEFAULT_SORT_ORDER);
            if(mc2 != null){
                if (mc2.moveToFirst() && !mc2.isBeforeFirst() && !mc2.isAfterLast()) {
                    do{
                        Conversation.from(context, mc2, true);
                        // Aurora yudingmin 2014-09-01 modified for optimize start
                        mInitedCount ++;
                        if(mListAdapter != null)
                        {
                            if(mScrollToBottom && mInitedCount - mListAdapter.getCount() > POST_ITEM_COUNT){
                                mScrollToBottom = false;
                                notifyCountSetChanged(mInitedCount);
                            }
                        } else {
                            return;
                        }
                        // Aurora yudingmin 2014-09-01 modified for optimize end
                    }while(mc2.moveToNext() && !mc2.isBeforeFirst() && !mc2.isAfterLast());
                }
                mc2.close();
            }
        }
        // Aurora yudingmin 2014-09-01 modified for fix nullPoint start
        if(mListAdapter != null)
        {
            Cursor listCursor = mListAdapter.getCursor();
            mInitedCount = listCursor== null? 0: listCursor.getCount();
            notifyCountSetChanged(mInitedCount);
        }
        // Aurora yudingmin 2014-09-01 modified for optimize end
        // Aurora yudingmin 2014-09-01 added for fix nullPoint start
        mInitStatu = INIT_ALL_DONE;
        // Aurora yudingmin 2014-09-01 added for optimize start
    }
    
    private void notifyCountSetChanged(final int count){
        mHandler.post(new Runnable() {
            public void run() {
             // Aurora xuyong 2014-09-28 modified for bug #8937 start
                if (mListAdapter != null) {
                    mListAdapter.notifyCountSetChanged(count);
                }
             // Aurora xuyong 2014-09-28 modified for bug #8937 end
            }
        });
    }
    // Aurora yudingmin 2014-09-04 added for optimize end
    
    private int mSelectCount = 0;
    
    protected void initActionBatchHandler() {
        mActionBatchHandler = new AuroraActionBatchHandler<Long>(AuroraRejConvOperActivity.this, mAuroraActionBar) {
            
            @Override
            public void enterSelectionMode(boolean autoLeave, Long itemPressing) {
                if (mListView != null) {
                    mListView.auroraSetNeedSlideDelete(false);
             // Aurora xuyong 2014-07-31 modified for bug #7062 start
                    mListView.auroraEnableSelector(false);
                }
                if (mListAdapter != null) {
                    mListAdapter.showCheckBox(true);
                    mListAdapter.setCheckBoxAnim(false);
                }
             // Aurora xuyong 2014-07-31 modified for bug #7062 end
                AuroraRejConvOperActivity.this.setMenuEnable(false);
                super.enterSelectionMode(autoLeave, itemPressing);
             // Aurora xuyong 2014-07-31 modified for bug #7062 start
                if (mListAdapter != null) {
                    mListAdapter.notifyDataSetChanged();
                }
             // Aurora xuyong 2014-07-31 modified for bug #7062 end
            }

            @Override
            public Set getDataSet() {
                // TODO Auto-generated method stub
                Set<Long> dataSet = new HashSet<Long>(mThreadsMap.size());
                for(int i = 0; i < mThreadsMap.size(); i++)
                    dataSet.add(mThreadsMap.get(i));
                return dataSet;
            }

            public void leaveSelectionModeWithActivityFinish(Activity activity) {
                super.leaveSelectionModeWithActivityFinish(activity);
                clearSelectedConvCache();
                if (activity != null) {
                    activity.finish();
                }
            }
            
            @Override
            public void leaveSelectionMode() {
                if (mAuroraMenu != null) {
                    // Aurora yudingmin 2014-10-30 modified for bug #9502 start
                    if(mAuroraActionBar.isShowBottomBarMenu()){
                        mAuroraActionBar.setShowBottomBarMenu(false);
                        // Aurora xuyong 2014-07-31 modified for bug #7074 start
                        mAuroraActionBar.showActionBottomeBarMenu();
                        // Aurora xuyong 2014-07-31 modified for bug #7074 end
                    }
                    // Aurora yudingmin 2014-10-30 modified for bug #9502 end
                    AuroraRejConvOperActivity.this.finish();
                }
            }

            @Override
            public void updateUi() {
                // TODO Auto-generated method stub
                mSelectCount = null != getSelected() ? getSelected().size() : 0;
                mBottomAuroraMenu.setBottomMenuItemEnable(1, mSelectCount == 0 ? false : true);
            }

            @Override
            public void updateListView(int allShow) {
             // Aurora xuyong 2014-07-31 modified for bug #7062 start
                if (mListAdapter != null) {
                    mListAdapter.updateAllCheckBox(allShow);
                    mListAdapter.notifyDataSetChanged();
                }
             // Aurora xuyong 2014-07-31 modified for bug #7062 end
            }
            
            public void bindToAdapter(GnSelectionManager<Long> selectionManager) {
                // TODO Auto-generated method stub
             // Aurora xuyong 2014-07-31 modified for bug #7062 start
                if (mListAdapter != null) {
                    mListAdapter.setSelectionManager(selectionManager);
                    if (selectionManager != null) {
                        selectionManager.setAdapter(mListAdapter);
                    }
                }
             // Aurora xuyong 2014-07-31 modified for bug #7062 end
            }
        };
       // Aurora xuyong 2014-07-31 modified for bug #7045 start
        if (mActionBatchHandler != null) {
            mActionBatchHandler.setHandler(mHandler);
            mActionBatchHandler.setIsFromReject(true);
            mActionBatchHandler.setIsFromRejectActivity(true);
        }
       // Aurora xuyong 2014-07-31 modified for bug #7045 end
    }
    
    // Aurora yudingmin 2014-09-04 modified for optimize start
    private void operateAfterChangeCursor(Cursor cursor) {
        if (mListAdapter.isEmpty()) {
            mEmptyTextView.setText(R.string.no_conversations);
            mEmptyView.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
            if(!mActionBatchHandler.isInSelectionMode() && mAuroraActionBar != null && mAuroraActionBar.isShowBottomBarMenu()){
                mAuroraActionBar.setShowBottomBarMenu(false);
                mAuroraActionBar.showActionBottomeBarMenu();
            }
        } else {
            mListView.setAdapter(mListAdapter);
            mListView.setRecyclerListener(mListAdapter);
            mEmptyView.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            if (!Conversation.isInitialized()) {
                Conversation.init(AuroraConvListActivity.sContext);
            }else{
                if (cursor != null && !cursor.isClosed()) {
                    Conversation.removeInvalidCache(cursor);
                }
            }
        }
        this.invalidateOptionsMenu();
        if(null != mActionBatchHandler) {
            // Aurora xuyong 2014-09-11 modified for NullPointerException start
            if (cursor != null && !cursor.isClosed() && cursor.getCount() > mThreadsMap.size()) {
            // Aurora xuyong 2014-09-11 modified for NullPointerException end
                mThreadsMap.clear();
                initThreadsMap();
            }
            mActionBatchHandler.refreshDataSet();
            mActionBatchHandler.updateUi();
        }
    }
    // Aurora yudingmin 2014-09-04 modified for optimize start
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (mActionBatchHandler != null) {
                    mActionBatchHandler.leaveSelectionModeWithActivityFinish(AuroraRejConvOperActivity.this);
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    private void onBottomMenuDismiss() {
        if (mListView != null) {
            mListView.auroraEnableSelector(true);
        }
        if (mListAdapter != null) {
            mListAdapter.showCheckBox(false);
            mListAdapter.setCheckBoxAnim(false);
            mListAdapter.updateAllCheckBox(0);
        }
        if (mActionBatchHandler != null) {
            mActionBatchHandler.destroyAction();
        }
        if (mListView != null) {
            mListView.auroraSetNeedSlideDelete(true);
        }

        if (mListAdapter != null) {
            mListAdapter.notifyDataSetChanged();
        }
        this.setMenuEnable(true);
        mActionBatchHandler = null;
        if (mThreadsMap != null) {
            mThreadsMap.clear();
        }
    }

    private void initThreadsMap() {
        // TODO Auto-generated method stub
        Cursor cursor = mListAdapter.getCursor();
        if (cursor != null && !cursor.isClosed()) {
            cursor.moveToPosition(-1);
            int i = 0;
            while(cursor.moveToNext()) {
                if (mThreadsMap.get(i) == null) {
                    mThreadsMap.put(i++, cursor.getLong(0));
                }
            }
        }
        // Aurora liugj 2013-10-25 modified for aurora's new feature end
    }
    
    private void clearSelectedConvCache() {
        if (mAllSelectedConvsCache != null) {
            mAllSelectedConvsCache.clear();
        }
    }
    
     private Cursor needShowGroupConv(Cursor cursor) {
                int simpleRecipientConvcount = 0;
                if (cursor != null && !cursor.isClosed() && cursor.moveToFirst()) {
                    if (getRecipientsNumber(cursor) == 1) {
                        simpleRecipientConvcount++;
                    }
                    while (cursor.moveToNext()) {
                        if (getRecipientsNumber(cursor) == 1) {
                            simpleRecipientConvcount++;
                        }
                    }
                }
             // Aurora xuyong 2014-07-22 added for bug #6735 start
                if (simpleRecipientConvcount <= 0) {
                    TextView selectAllBtn = (TextView) mAuroraActionBar.getOkButton();
                    selectAllBtn.setVisibility(View.GONE);
                    initActionBatchHandler();
                // Aurora xuyong 2014-07-31 modified for bug #7045 start
                    if (mActionBatchHandler != null) {
                        mActionBatchHandler.initActionBarOnlyCancelListener();
                    }
                // Aurora xuyong 2014-07-31 modified for bug #7045 end
                    return null;
                }
             // Aurora xuyong 2014-07-22 added for bug #6735 end
                MatrixCursor copyOne = new MatrixCursor(Conversation.ALL_THREADS_PROJECTION, simpleRecipientConvcount);
             // Aurora xuyong 2014-07-31 modified for bug #7074 start
                if (cursor != null && !cursor.isClosed() && cursor.moveToFirst() && !cursor.isBeforeFirst() && !cursor.isAfterLast()) {
             // Aurora xuyong 2014-07-31 modified for bug #7074 end
                    int columnCount = cursor.getColumnCount();
                    if (getRecipientsNumber(cursor) == 1) {
                        Object[] firstItemDetail = new Object[columnCount];
                        for (int i = 0; i < columnCount; i++) {
                       // Aurora xuyong 2014-07-31 modified for bug #7074 start
                            try {
                                firstItemDetail[i] = cursor.getString(i);
                            } catch (CursorIndexOutOfBoundsException e) {
                                Log.e("CIOEC", "current position = " + i + ", cursor.getColumnCount() = " + cursor.getColumnCount());
                            }
                       // Aurora xuyong 2014-07-31 modified for bug #7074 end
                        }
                        copyOne.addRow(firstItemDetail);
                    }
                // Aurora xuyong 2014-07-31 modified for bug #7074 start
                    while (cursor.moveToNext() && !cursor.isBeforeFirst() && !cursor.isAfterLast()) {
                // Aurora xuyong 2014-07-31 modified for bug #7074 end
                        if (getRecipientsNumber(cursor) == 1) {
                            if (getRecipientsNumber(cursor) == 1) {
                                Object[] firstItemDetail = new Object[columnCount];
                                for (int i = 0; i < columnCount; i++) {
                             // Aurora xuyong 2014-07-31 modified for bug #7074 start
                                    try {
                                        firstItemDetail[i] = cursor.getString(i);
                                    } catch (CursorIndexOutOfBoundsException e) {
                                        Log.e("CIOEC", "current position = " + i + ", cursor.getColumnCount() = " + cursor.getColumnCount());
                                    }
                             // Aurora xuyong 2014-07-31 modified for bug #7074 end
                                }
                                copyOne.addRow(firstItemDetail);
                            }
                        }
                    }
                }
                if (copyOne == null || (copyOne != null && !copyOne.isClosed() && copyOne.getCount() <= 0)) {
                    AuroraActionBatchHandler.setHiddenAllBtn(true);
                // Aurora xuyong 2014-07-22 added for bug #6735 start
                    initActionBatchHandler();
                 // Aurora xuyong 2014-07-31 modified for bug #7045 start
                    if (mActionBatchHandler != null) {
                        mActionBatchHandler.initActionBarOnlyCancelListener();
                    }
                 // Aurora xuyong 2014-07-31 modified for bug #7045 end
                // Aurora xuyong 2014-07-22 added for bug #6735 end
                } else {
                    AuroraActionBatchHandler.setHiddenAllBtn(false);
                // Aurora xuyong 2014-07-22 added for bug #6735 start
                    initActionBatchHandler();
                // Aurora xuyong 2014-07-31 modified for bug #7045 start
                    if (mActionBatchHandler != null) {
                        mActionBatchHandler.enterSelectionMode(false, null);
                        // Aurora yudingmin 2014-10-30 modified for bug #9502 start
                        if(!isFinishing()){
                            // Aurora xuyong 2014-07-31 modified for bug #7062 start
                            mAuroraActionBar.setShowBottomBarMenu(true);
                       // Aurora xuyong 2014-07-31 modified for bug #7074 start
                            mAuroraActionBar.showActionBottomeBarMenu();
                       // Aurora xuyong 2014-07-31 modified for bug #7074 end
                       // Aurora xuyong 2014-07-31 modified for bug #7062 end
                        }
                        // Aurora yudingmin 2014-10-30 modified for bug #9502 end
                    }
                }
                // Aurora yudingmin 2014-10-30 modified for bug #9486 start
                if(cursor != null && !cursor.isClosed()){
                    cursor.close();
                }
                // Aurora yudingmin 2014-10-30 modified for bug #9486 end
                return copyOne;
    }
     
    private int getRecipientsNumber(Cursor cursor) {
        String recipientsIds = cursor.getString(cursor.getColumnIndex(Threads.RECIPIENT_IDS));
        String[] ids = recipientsIds.split(" ");
        return ids.length;
    }

}
