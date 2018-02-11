package com.aurora.mms.ui;
// Aurora xuyong 2014-10-23 created for privacy feature
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.gionee.internal.telephony.GnITelephony;
import com.android.internal.telephony.ITelephony;
import com.android.mms.LogTag;
import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.transaction.CBMessagingNotification;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.transaction.SmsRejectedReceiver;
import com.android.mms.transaction.WapPushMessagingNotification;
import com.android.mms.ui.CBMessageListActivity;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.ConversationListAdapter;
import com.android.mms.ui.ConversationListItem;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.WPMessageActivity;
import com.android.mms.ui.WarnOfStorageLimitsActivity;
import com.android.mms.util.DraftCache;
import com.android.mms.util.Recycler;
import com.gionee.internal.telephony.GnPhone;
import gionee.provider.GnSettings;
import gionee.telephony.GnSmsManager;
import gionee.provider.GnTelephony;
import com.gionee.mms.data.RecentContact;
import com.gionee.mms.popup.PopUpView;
import com.aurora.android.mms.pdu.PduHeaders;
import com.aurora.featureoption.FeatureOption;
import com.mediatek.wappush.SiExpiredCheck;
import com.privacymanage.service.AuroraPrivacyUtils;

import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.app.AuroraAlertDialog;
import android.app.Activity;
import android.app.ListFragment;
import android.app.SearchManager;
import android.app.SearchableInfo;
import aurora.app.AuroraProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.ColorStateList;
import android.database.ContentObserver;
import android.database.CursorIndexOutOfBoundsException;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SqliteWrapper;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ServiceManager;
import aurora.preference.AuroraPreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.provider.Settings;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import gionee.provider.GnTelephony.Threads;
import android.provider.Telephony.Sms.Conversations;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.View.OnKeyListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.WindowManager;
import android.graphics.Rect;
import aurora.widget.AuroraButton;
import aurora.widget.AuroraSearchView;
import aurora.widget.AuroraListView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.os.SystemProperties;
import com.gionee.mms.ui.CustomMenu.DropDownMenu;
import android.widget.PopupMenu;
import android.content.pm.ActivityInfo;

import com.android.internal.widget.LockPatternUtils;
import android.R.anim;
import android.app.admin.DevicePolicyManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import aurora.widget.AuroraSpinner;
import android.widget.AdapterView.OnItemSelectedListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import com.android.mms.util.GnActionModeHandler;
import com.android.mms.util.GnSelectionManager;
import android.widget.AdapterView.OnItemLongClickListener;
import android.os.SystemProperties;

import android.provider.Telephony.Sms;
import com.android.mms.widget.MmsWidgetProvider;
import com.aurora.mms.search.MessageSearchListAdapter;
import com.aurora.mms.util.Utils;

import aurora.widget.AuroraSmartPopupLayout;
import aurora.widget.AuroraSmartLayout;
import aurora.widget.AuroraCheckBox;
import android.widget.FrameLayout;
import android.content.SharedPreferences.Editor;
import android.widget.LinearLayout.LayoutParams;
import android.view.Gravity;
import aurora.app.AuroraActivity;
import com.gionee.mms.ui.DraftFragment;
import com.gionee.mms.ui.MsgChooseLockPassword;

import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;


import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;

public class AuroraPrivConvActivity extends AuroraActivity implements 
     DraftCache.OnDraftChangedListener, OnItemLongClickListener, View.OnTouchListener{

    private static final String DEFAULT_SORT_ORDER = "sms.date DESC";
    private static final String SMS_ID = "_id";
    private static final String SMS_THREAD_ID = "thread_id";
    private static final String SMS_ADDRESS = "address";
    private static final String SMS_DATE = "date";
    private static final String SMS_BODY = "body";
    
    protected static final String[] PROJECTION_SMS = new String[] {
        SMS_ID,
        SMS_THREAD_ID,
        SMS_ADDRESS,
        SMS_DATE,
        SMS_BODY
    };

    private static ThreadListQueryHandler mQueryHandler;
    private ConversationListAdapter mListAdapter;
    private static final int THREAD_LIST_QUERY_TOKEN       = 1701;
    public static final int DELETE_CONVERSATION_TOKEN      = 1801;
    public static final int HAVE_LOCKED_MESSAGES_TOKEN     = 1802;
    private static final int DELETE_OBSOLETE_THREADS_TOKEN = 1803;
    public static final int HAVE_STAR_MESSAGES_TOKEN       = 1804;
    public static final int MUL_DELETE_CONVERSATIONS_TOKEN = 1805;
    
    public static final int DELETE_CONVERSATION_NOT_LAST_TOKEN = 1901;
    public static final int THREAD_LIST_LOCKED_QUERY_TOKEN  = 1902;
    public static final int THREAD_LIST_FAVORITE_QUERY_TOKEN = 1903;
    private static ArrayList<Long> mAllSelectedThreadIds = new ArrayList<Long>();
    private static Map<Integer, Conversation> mAllSelectedConvsCache = new HashMap<Integer, Conversation> ();
    public static final String[] ALL_THREADS_PROJECTION = {
        Threads._ID, Threads.DATE, Threads.MESSAGE_COUNT, Threads.RECIPIENT_IDS,
        Threads.SNIPPET, Threads.SNIPPET_CHARSET, Threads.READ, Threads.ERROR,
        Threads.HAS_ATTACHMENT, Threads.TYPE, Threads.SIM_ID
    };
    private static int CHANGE_SCROLL_LISTENER_MIN_CURSOR_COUNT = 500;
    private final int INIT_NONE = 0;
    private final int INIT_INIT_ITEM_COUNT_DONE = 1;
    private final int INIT_ALL_DONE = 2;
    private int mInitStatu = INIT_NONE;
    private static final int CHANGE_MC1_CURSOR = 11;
    private static final int INIT_ITEM_COUNT = 10;
    private static final int POST_ITEM_COUNT = 100;
    private  int mInitedCount = 0;
    private boolean mScrollToBottom = false;
    public static final int DIS_SELECT_ALL = 12;
    public static final int SELECT_ALL = 13;
    private static final int FINISH_ACTIVITY = 14;
    private static final int CHANGE_MC2_CURSOR = 15;
    AuroraProgressDialog mProgressDialog = null;
    ExecutorService mSingleThreadExecutor = Executors.newSingleThreadExecutor();
    RunnableExecutor mSingleThreadExecutor2 = new RunnableExecutor();
    private Handler mHandler = new Handler() {
        
        QueryResult initResult;
        
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case FINISH_ACTIVITY:
                    leaveBatchMode();
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                    AuroraPrivConvActivity.this.finish();
                    break;
                case CHANGE_MC1_CURSOR:
                    if (msg.obj == null) {
                        operateAfterChangeCursor(null);
                        return;
                    }
                    final Cursor allCursor = (Cursor)msg.obj;
                    if (allCursor == null || allCursor.getCount() <= 0) {
                        mListAdapter.changeCursor(allCursor);
                        operateAfterChangeCursor(allCursor);
                        return;
                    }
                    if(mInitStatu == INIT_NONE){
                        mInitStatu = INIT_INIT_ITEM_COUNT_DONE;
                        mListAdapter.changeCountBeforeDataChanged(INIT_ITEM_COUNT);
                        if(allCursor.getCount() > INIT_ITEM_COUNT){
                            mSingleThreadExecutor2.execute(new Runnable() {
                                        
                                        @Override
                                        public void run() {    
                                            loadAllContact();
                                        }
                                        
                                    });
                        } else {
                            mInitStatu = INIT_ALL_DONE;
                        }
                    }
                    mListView.auroraSetRubbishBackNoAnim();
                    mListAdapter.changeCursor(allCursor);
                    operateAfterChangeCursor(allCursor);
                    break;
                case CHANGE_MC2_CURSOR:
                    {
                        Cursor allResult = (Cursor)msg.obj;
                        if (mListAdapter != null) {
                            if (allResult != null && !allResult.isClosed()) {
                                mListAdapter.changeCursor(allResult);
                            }
                            operateAfterChangeCursor(allResult);
                        }
                    }
                    break;
                default :
                        // more to be do here
                        break;
            }
        }
        
    };
    
    class RunnableExecutor implements Runnable {

        Runnable mWaitingRunnable = null;
        Thread mThread = null;

        public void execute(Runnable runnable) {
            synchronized (this) {
                mWaitingRunnable = runnable;
                if (mThread == null) {
                    mThread = new Thread(this);
                    mThread.start();
                }
            }
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            do {
                Runnable temp = null;
                synchronized (this) {
                    temp = mWaitingRunnable;
                    mWaitingRunnable = null;
                }
                if (temp != null) {
                    temp.run();
                }
            } while (mWaitingRunnable != null);
            synchronized (this) {
                if(mWaitingRunnable == null){
                    mThread = null;
                } else {
                    mThread = new Thread(this);
                    mThread.start();
                }
            }
        }
    }
  
    private void loadAllContact(){
        mInitedCount = 0;
        Context context = this.getApplicationContext();
        String selection = null;
        Cursor mc2 = context.getContentResolver().query(Conversation.sAllThreadsUri, Conversation.ALL_THREADS_PROJECTION, selection, null, Conversations.DEFAULT_SORT_ORDER);
        if(mc2 != null){
            if (mc2.moveToFirst() && !mc2.isBeforeFirst() && !mc2.isAfterLast()) {
                do{
                    Conversation.from(context, mc2, true);
                    mInitedCount ++;
                    // Aurora xuyong 2014-10-30 modified for cursor excepion start
                    if(mListAdapter != null)
                    {
                        if(mScrollToBottom && mInitedCount - mListAdapter.getCount() > POST_ITEM_COUNT){
                            mScrollToBottom = false;
                            notifyCountSetChanged(mInitedCount);
                        }
                    // Aurora xuyong 2014-10-30 modified for cursor excepion end
                    }
                }while(mc2.moveToNext() && !mc2.isBeforeFirst() && !mc2.isAfterLast());
            }
            mc2.close();
        }
        Cursor listCursor = mListAdapter.getCursor();
        mInitedCount = listCursor== null? 0: listCursor.getCount();
        notifyCountSetChanged(mInitedCount);
        mInitStatu = INIT_ALL_DONE;
    }
    
    private void notifyCountSetChanged(final int count){
        mHandler.post(new Runnable() {
            public void run() {
                mListAdapter.notifyCountSetChanged(count);
            }
        });
    }
    
    private void rebuildCursor(Cursor allResult, final int queryId) {
        if (allResult == null) {
            return;
        }
        final int count = allResult.getCount();
        final int columnCount = allResult.getColumnCount();
        final MatrixCursor mc2 = new MatrixCursor(Conversation.ALL_THREADS_PROJECTION, count);
        allResult.moveToPosition(-1);
        if (allResult.moveToFirst() && !allResult.isBeforeFirst() && !allResult.isAfterLast()) {
            Object[] firstItemDetail = new Object[columnCount];
            for (int i = 0; i < columnCount; i++) {
                try {
                    firstItemDetail[i] = allResult.getString(i);
                } catch (CursorIndexOutOfBoundsException e) {
                    Log.e("CIOEC", "current position = " + i + ", mCursor.getColumnCount() = " + allResult.getColumnCount());
                }
            }
            mc2.addRow(firstItemDetail);
            while (allResult.moveToNext() && !allResult.isBeforeFirst() && !allResult.isAfterLast()) {
                Object[] itemDetail = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    try {
                        itemDetail[i] = allResult.getString(i);
                    } catch (CursorIndexOutOfBoundsException e) {
                        Log.e("CIOEC", "current position = " + i + ", mCursor.getColumnCount() = " + allResult.getColumnCount());
                    }
                }
                mc2.addRow(itemDetail);
            }
            allResult.moveToFirst();
        }
        Context context = this.getApplicationContext();
        if (mc2.moveToFirst() && !mc2.isBeforeFirst() && !mc2.isAfterLast()) {
            Conversation.from(context, mc2);
            while(mc2.moveToNext() && !mc2.isBeforeFirst() && !mc2.isAfterLast()) {
                Conversation.from(context, mc2);
            }    
        }
        if (!allResult.isClosed()) {
            allResult.close();
        }
    }
    
    private SharedPreferences mPrefs;
    static private final String CHECKED_MESSAGE_LIMITS = "checked_message_limits";
    private int mType;
    private static final String WP_TAG = "Mms/WapPush";

    private boolean mNeedToMarkAsSeen;
    private boolean mDataValid;
    private static AuroraAlertDialog mDelAllDialog;
    private AuroraMenu mAuroraMenu;
    private AuroraMenu mBottomAuroraMenu;

    public static final int CONFIRM_PASSWORD_REQUEST = 39;
    public static final int UPDATE_PASSWORD_REQUEST =40;
    public static final int MULTI_PASSWORD_REQUEST = 41;
    public static final int CONFIRM_DECRYPTION_PASSWORD_REQUEST = 42;
    
    private static AuroraListView mListView;
    private boolean isInit = false;
    private LinearLayout mEmptyView;
    private final static int MENU_EXCHANGE = 6;
    
    private TextView mEmptyTextView;
    private int mSelectCount = 0; 
    private boolean mBeingEncrypt = false;
    public static HashSet<Long> mSetEncryption = new HashSet<Long>();
    private boolean mContentChanged;
    private  static GnActionModeHandler<Long> mActionModeHandler = null;
    private static AuroraActionBatchHandler<Long> mActionBatchHandler = null;
    private Map<Integer, Long> mThreadsMap = new HashMap<Integer, Long>();
    private static boolean mListChange = false;
    
    private static boolean gnFlyFlag = SystemProperties.get("ro.gn.oversea.custom").equals("RUSSIA_FLY");
    
    private boolean mFirstStart = true;
    private boolean mIsonResume = true;
    private Contact mContact = null;
    private boolean mHasEncryption = false;
    private Menu mMenu ;
    private static final int MENU_DELETE       = 10;
    private static final int MENU_ENCRYPTION   = 11;
    private static final int MENU_DECRYPTION   = 12;
    private static final int MENU_VIEW_CONTACT = 13;
    private static final int MENU_NEW_CONTACT  = 14;
    private static final int MENU_ADD_CONTACT  = 15;
    public static final int ONEDEL_CONFIRM_PASSWORD_REQUEST = 11;
    public static final int ONE_CONTEXT_PASSWORD_REQUEST = 12;
    private long mThreadID = -1;
    private String mQueryString;
    
    private ContentObserver mConvaObserver = new ContentObserver(new Handler()) { 

        @Override
        public void onChange(boolean selfChange) { 
             super.onChange(selfChange);
             if (!mIsDeleting) {
                mQueryHandler.removeCallbacks(mQueryRunnable);
                mQueryHandler.postDelayed(mQueryRunnable, 500);
             }
        }   

    };
    
    private void initProgressAddingDialog(Activity activity) {
        mProgressDialog = new AuroraProgressDialog(activity);
        mProgressDialog.setMessage(this.getString(R.string.aurora_black_adding));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        // Aurora xuyong 2014-10-24 added for privacy feature start
        Utils.addInstance(this);
        // Aurora xuyong 2014-10-24 added for privacy feature end
        mQueryHandler = new ThreadListQueryHandler(this.getContentResolver());
        mPrefs = AuroraPreferenceManager.getDefaultSharedPreferences(this);
        boolean checkedMessageLimits = mPrefs.getBoolean(CHECKED_MESSAGE_LIMITS, false);
        /*if (!checkedMessageLimits ) {
            runOneTimeStorageLimitCheckForLegacyMessages();
        }*/
        initProgressAddingDialog(this);
            this.getApplicationContext().getContentResolver()
                     .registerContentObserver(MmsSms.CONTENT_URI, true, mConvaObserver);
        setAuroraContentView(R.layout.aurora_conversation_list_privacy_screen,
                 AuroraActionBar.Type.Normal);
        this.getAuroraActionBar().setTitle(R.string.aurora_privacy_msg_title);
        this.getAuroraActionBar().setmOnActionBarBackItemListener(auroraActionBarBackItemClickListener);
        mEmptyView = (LinearLayout)findViewById(R.id.aurora_conversation_privacy_empty);

        mEmptyTextView = (TextView)findViewById(R.id.aurora_privacy_empty);
        
        mListView = (AuroraListView) findViewById(R.id.privacy_list);
        mListView.setOnTouchListener(this);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                // TODO Auto-generated method stub
                 if(null != mActionBatchHandler && mActionBatchHandler.isInSelectionMode()) {
                        AuroraCheckBox mCheckBox = (AuroraCheckBox) arg1.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
                        if (mCheckBox == null) {
                            return;
                        }
                        boolean isChecked = mCheckBox.isChecked();
                        mCheckBox.auroraSetChecked(!isChecked, true);
                        mActionBatchHandler.getSelectionManger().toggle(arg3);
                    } else {
                        Cursor cursor  = (Cursor) arg0.getItemAtPosition(arg2);
                        if (cursor == null || cursor.isClosed()) {
                            return;
                        }   
                        
                        if (MmsApp.mGnMessageSupport) {
                            try {
                                // 0 : threadid, 9 type
                                long threadId = cursor.getLong(cursor.getColumnIndex("_id"));
                                int type = cursor.getInt(cursor.getColumnIndex("type"));
                                // Aurora xuyong 2014-10-04 modified for bug #9597 start
                                if (MmsApp.sHasPrivacyFeature) {
                                    openThread(threadId, type, cursor.getLong(cursor.getColumnIndex("is_privacy")));
                                } else {
                                    openThread(threadId, type);
                                }
                                // Aurora xuyong 2014-10-04 modified for bug #9597 end
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                            return;
                        }
                        Context context = AuroraPrivConvActivity.this.getApplicationContext();
                        if (null != context) {
                            Conversation conv = null;
                            try {
                                conv = Conversation.from(context, cursor);
                            } catch (CursorIndexOutOfBoundsException e) {
                                
                            }
                            if (conv != null) {
                                long tid = conv.getThreadId();
                                // Aurora xuyong 2014-10-04 modified for bug #9597 start
                                if (MmsApp.sHasPrivacyFeature) {
                                    openThread(tid, conv.getType(), conv.getPrivacy());
                                } else {
                                    openThread(tid, conv.getType());
                                }
                                // Aurora xuyong 2014-10-04 modified for bug #9597 end
                            }
                        }
            }
            }
        });
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub
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
        mListView.auroraSetUseNewSelectorLogical(false);
        mListView.setDividerHeight(0);
        mListView.auroraSetFrameNumbers(6);
        mListAdapter = new ConversationListAdapter(this, null);
        mListAdapter.setPrivacy(false);
        if (mActionBatchHandler != null && mActionBatchHandler.isInSelectionMode()) {
            mListAdapter.showCheckBox(true);
            mActionBatchHandler.bindToAdapter(mActionBatchHandler.getSelectionManger());
        }
    }

    public boolean hideDeleteBack() {
        if (mListView != null) {
            boolean deleteIsShow = mListView.auroraIsRubbishOut();
            if (deleteIsShow) {
                mListView.auroraSetRubbishBack();
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mListView == v) {
                InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);     
                if (imm.isActive()) {     
                    imm.hideSoftInputFromWindow(v.getApplicationWindowToken() , 0);   
                }
            }
        }
        return false;
    }
    
    private void hideInputMethod() {
        InputMethodManager inputMethodManager =
            (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(this.getWindow()!=null && this.getWindow().getCurrentFocus()!=null){
            inputMethodManager.hideSoftInputFromWindow(this.getWindow().getCurrentFocus().getWindowToken(), 0);
        }
    }
    
    private Runnable mQueryRunnable = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            mContentChanged = true;
            startQuery();
        }
    };

    private final ConversationListAdapter.OnContentChangedListener mContentChangedListener =
        new ConversationListAdapter.OnContentChangedListener() {
        public void onContentChanged(ConversationListAdapter adapter) {
             if (mIsonResume) {
                mNeedToMarkAsSeen = true;
                mQueryHandler.removeCallbacks(mQueryRunnable);
                mQueryHandler.postDelayed(mQueryRunnable, 0);
            } 
            if (mListViewWatcher != null) {
                mListViewWatcher.listViewChanged(true);
            }
        }
    };

    public void startEncryptionAsyncQuery() {
        try {
            if (mListView != null) {
                mListView.auroraSetNeedSlideDelete(false);
            }
            Conversation.startQueryForAll(mQueryHandler, THREAD_LIST_QUERY_TOKEN, true);
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(this, e);
        }
    }

    private void startAsyncQuery() {
        try {
            if (mListView != null) {
                mListView.auroraSetNeedSlideDelete(false);
            }
            Conversation.startQueryForPrivacyAll(mQueryHandler, THREAD_LIST_QUERY_TOKEN, null);

        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(this, e);
        }
    }

    /**
     * Checks to see if the number of MMS and SMS messages are under the limits for the
     * recycler. If so, it will automatically turn on the recycler setting. If not, it
     * will prompt the user with a message and point them to the setting to manually
     * turn on the recycler.
     */
    public synchronized void runOneTimeStorageLimitCheckForLegacyMessages() {
        if (Recycler.isAutoDeleteEnabled(this)) {
            markCheckedMessageLimit();
            return;
        }
        new Thread(new Runnable() {
            public void run() {
                if (Recycler.checkForThreadsOverLimit(AuroraPrivConvActivity.this)) {
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            Intent intent = new Intent(AuroraPrivConvActivity.this,
                                    WarnOfStorageLimitsActivity.class);
                            AuroraPrivConvActivity.this.startActivity(intent);
                        }
                    }, 2000);
                }
                AuroraPrivConvActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        markCheckedMessageLimit();
                    }
                });
            }
        }).start();
    }

    /**
     * Mark in preferences that we've checked the user's message limits. Once checked, we'll
     * never check them again, unless the user wipe-data or resets the device.
     */
    private void markCheckedMessageLimit() {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(CHECKED_MESSAGE_LIMITS, true);
        editor.apply();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mListView.auroraOnResume();
        mIsonResume = true;
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mListView.auroraOnPause();
        hideDeleteBack();
        mIsonResume = false;
    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        MessagingNotification.cancelNotification(this,
               SmsRejectedReceiver.SMS_REJECTED_NOTIFICATION_ID);
        DraftCache.getInstance().addOnDraftChangedListener(this);
        mNeedToMarkAsSeen = true;

        startQuery();
        
        DraftCache.getInstance().refresh();
        if (!Conversation.loadingThreads()) {
            Contact.invalidateCache();
        }
    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        DraftCache.getInstance().removeOnDraftChangedListener(this);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        if (null != mActionBatchHandler && mActionBatchHandler.isInSelectionMode()) {
            mActionBatchHandler.leaveSelectionMode();
        }
        if (mListAdapter != null && mListAdapter.getCursor() != null) {
            mListAdapter.getCursor().close();
        }
        super.onDestroy();
      // Aurora xuyong 2014-10-24 added for privacy feature start
        Utils.removeInstance(this);
      // Aurora xuyong 2014-10-24 added for privacy feature end
        if (MmsApp.mGnMultiSimMessage && mConvaObserver != null) {
            getContentResolver().unregisterContentObserver(mConvaObserver);
        }
    }

    public interface ListViewWatcher {
        void listViewChanged(boolean isChange);
    }

    private ListViewWatcher mListViewWatcher = null;

    public void setListViewWatcher (ListViewWatcher l) {
        mListViewWatcher = l;
    }

    public void backDeleteThead() {
        if (mFirstStart) {
            mFirstStart = false;
            new Thread(new Runnable() {
                
                @Override
                public void run() {
                    Cursor cursor = null;
                    StringBuffer conversationsIds = new StringBuffer();
                    try {
                        Uri allThreadsUri = Threads.CONTENT_URI.buildUpon().appendQueryParameter("simple", "true").build();
                            cursor = AuroraPrivConvActivity.this.getContentResolver().query(allThreadsUri, new String[] { "_id" }, "deleted = 1",
                                    null, null);
                        if (cursor != null && cursor.getCount() > 0) {
                            while (cursor.moveToNext() && !cursor.isAfterLast() && !cursor.isBeforeFirst()) {
                                conversationsIds.append(cursor.getLong(0));
                                if (!cursor.isLast()) {
                                    conversationsIds.append(",");
                                }
                            }
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                    if (!TextUtils.isEmpty(conversationsIds)) {
                        if (mListView != null) {
                            mListView.auroraSetNeedSlideDelete(false);
                        }
                        Conversation.startDelete(mQueryHandler, DELETE_CONVERSATION_TOKEN,
                                true, conversationsIds.toString(), "OneLast");
                    }
                }
            }).start();
        }
    }
    
    private void operateAfterChangeCursor(Cursor result) {
        if (mListAdapter.isEmpty()) {
            mEmptyTextView.setText(R.string.no_privacy_conversations);
            mEmptyView.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        } else {
            if (!isInit) {
                isInit = true;
                mListView.setOnKeyListener(mThreadListKeyListener);
                mListView.setAdapter(mListAdapter);
                mListView.setRecyclerListener(mListAdapter);
                mListView.auroraSetAuroraBackOnClickListener(new AuroraListView.AuroraBackOnClickListener() {
                    
                    @Override
                    public void auroraOnClick(int position) {
                        mNeedDeleteHereFlag = false;
                        mNeedSetDataChanged = false;
                        Cursor cursor = (Cursor)mListView.getItemAtPosition(position);
                        if (cursor == null || cursor.getPosition() < 0) {
                            return;
                        }
                        Conversation conv = Conversation.from(AuroraPrivConvActivity.this, cursor);
                        if (MmsApp.mGnDeleteRecordSupport && mAllSelectedThreadIds != null) {
                            mAllSelectedThreadIds.clear();
                            mAllSelectedThreadIds.add(conv.getThreadId());
                        }
                        deleteConversations();
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
                mListView.auroraSetDeleteItemListener(new AuroraListView.AuroraDeleteItemListener () {
                    
                    @Override
                    public void auroraDeleteItem(View v,int position) {
                        if (mListView != null) {
                            mListView.auroraSetNeedSlideDelete(false);
                        }
                        Conversation.startDeletestar(mDeletedAQHandler, mDeletedToken,
                                mDeletedStaredMessages, mDeletedThreadId);
                        DraftCache.getInstance().setDraftState(mDeletedThreadId, false);
                    }

                });
                mListView.setOnItemLongClickListener(AuroraPrivConvActivity.this);
            }
            mEmptyView.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        }
        if (!Conversation.isInitialized()) {
            Conversation.init(this);
        }else{
            if (result != null && !result.isClosed()) {
                if (result.moveToFirst()) {   
                    Conversation.removeInvalidCache(result, mListAdapter.getCount());    
                }
            }
        }
        
        if (mNeedToMarkAsSeen) {
            mNeedToMarkAsSeen = false;
            Conversation.markAllConversationsAsSeen(this);

            // Delete any obsolete threads. Obsolete threads are threads that aren't
            // referenced by at least one message in the pdu or sms tables. We only call
            // this on the first query (because of mNeedToMarkAsSeen).
            mHandler.post(mDeleteObsoleteThreadsRunnable);
        }
        this.invalidateOptionsMenu();
        if(mContentChanged) {
            if(!mListChange && null != mActionBatchHandler) {
                if (result.getCount() > mThreadsMap.size()) {
                    mSetEncryption.clear();
                    mThreadsMap.clear();
                    initThreadsMap();
                }
                mActionBatchHandler.refreshDataSet();
                mActionBatchHandler.updateUi();
                if(result.getCount() != mActionBatchHandler.getSelected().size()) {
                    mListChange = true;
                }
            }
            mContentChanged = false;
        }
    }
    
    class QueryResult {
        Cursor mAllResult;
        Cursor mMinorityResult;
        
        public QueryResult(Cursor allCursor, Cursor minCursor) {
            mAllResult = allCursor;
            mMinorityResult = minCursor;
        }
        
        public Cursor getAllCursor() {
            return mAllResult;
        }
        
        public Cursor getMinCursor() {
            return mMinorityResult;
        }
    }
    
    private final class ThreadListQueryHandler extends BaseProgressQueryHandler {
        private final String CONV_TAG = "ConvFragment";
        private boolean lockFlag = false;
        private boolean starFlag = false;

        public ThreadListQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (mListView != null) {
                if (!(isBottomMenuShowing())) {
                    mListView.auroraSetNeedSlideDelete(true);
                }
            }
            if (MmsApp.mIsSafeModeSupport) {
                mEmptyTextView.setText(R.string.no_conversations);
                mEmptyView.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.GONE);
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
                if(mActionBatchHandler != null) {
                    mActionBatchHandler.leaveSelectionMode();
                }
                if (mDelAllDialog != null && mDelAllDialog.isShowing()) {
                    mDelAllDialog.dismiss();
                }
                return;
            }
              if (cursor == null) {
                   return;
              }
            switch (token) {
            case THREAD_LIST_QUERY_TOKEN:
                final Cursor transCursor = cursor;
                if (mInitStatu != INIT_ALL_DONE) {
                    mSingleThreadExecutor.execute(new Runnable() {
                        
                        @Override
                        public void run() {
                            int initCount = INIT_ITEM_COUNT;
                            if (transCursor == null || transCursor.isClosed() || transCursor.getCount() <= 0) {
                                Message msg = mHandler.obtainMessage(CHANGE_MC1_CURSOR);
                                msg.sendToTarget();
                                return;
                            }
                            int count = transCursor.getCount();
                            int columnCount = transCursor.getColumnCount();
                            MatrixCursor mc1 = new MatrixCursor(Conversation.ALL_THREADS_PROJECTION, initCount);
                            transCursor.moveToPosition(-1);
                            if (transCursor.moveToFirst() && !transCursor.isAfterLast() && !transCursor.isBeforeFirst()) {
                                Object[] firstItemDetail = new Object[columnCount];
                                for (int i = 0; i < columnCount; i++) {
                                    try {
                                        firstItemDetail[i] = transCursor.getString(i);
                                    } catch (CursorIndexOutOfBoundsException e) {
                                        Log.e("CIOEC", "current position = " + i + ", transCursor.getColumnCount() = " + transCursor.getColumnCount());
                                    }
                                }
                                mc1.addRow(firstItemDetail);
                                initCount--;
                                while (transCursor.moveToNext() && initCount > 0 && !transCursor.isBeforeFirst() && !transCursor.isAfterLast()) {
                                    Object[] itemDetail = new Object[columnCount];
                                    for (int i = 0; i < columnCount; i++) {
                                        try {
                                            itemDetail[i] = transCursor.getString(i);
                                        } catch (CursorIndexOutOfBoundsException e) {
                                            Log.e("CIOEC", "current position = " + i + ", transCursor.getColumnCount() = " + transCursor.getColumnCount());
                                        }
                                    }
                                    mc1.addRow(itemDetail);
                                    initCount--;
                                }
                                if (mc1.moveToFirst() && !mc1.isAfterLast() && !mc1.isBeforeFirst()) {
                                    Context context = AuroraPrivConvActivity.this.getApplicationContext();
                                    if (context != null) {
                                        try {  
                                            Conversation.from(context, mc1, true);
                                        } catch (CursorIndexOutOfBoundsException e) {
                                            
                                        }
                                        while(mc1.moveToNext() && !mc1.isBeforeFirst() && !mc1.isAfterLast()) {
                                            try {  
                                                Conversation.from(context, mc1, true);
                                            } catch (CursorIndexOutOfBoundsException e) {
                                                
                                            }
                                        }
                                    }
                                }
                            }
                            transCursor.moveToFirst();
                            Message msg = mHandler.obtainMessage(CHANGE_MC1_CURSOR);
                            if(mc1 != null){
                                mc1.close();
                            }
                            msg.obj = transCursor;
                            msg.sendToTarget();
                        }
                    });
                } else {
                    mListAdapter.changeCountBeforeDataChanged(transCursor == null?0:transCursor.getCount());
                    mListView.auroraSetRubbishBackNoAnim();
                    mListAdapter.changeCursor(transCursor);
                    operateAfterChangeCursor(transCursor);
                }
                break;

            case THREAD_LIST_LOCKED_QUERY_TOKEN:
                lockFlag = false;
                cursor.close();
                break;
            case THREAD_LIST_FAVORITE_QUERY_TOKEN:
                starFlag = (cursor != null && cursor.getCount() > 0);
                boolean isAll = false;
                if (mAllSelectedThreadIds.size() == mListView.getCount()) {
                    isAll = true;
                }
                final boolean isAllSelected = isAll;
                long threadId = (Long)cookie;
                if (isAllSelected){
                        confirmDeleteGnThreadDialog(
                                new GnDeleteThreadListener(mListView, -1, mQueryHandler,
                                        AuroraPrivConvActivity.this), isAllSelected, starFlag,
                                        AuroraPrivConvActivity.this);
                } else {
                    long tempThreadId = threadId;
                    if (mAllSelectedThreadIds.size() != 1){
                        tempThreadId = 9999;
                    } else {
                        Iterator it = mAllSelectedThreadIds.iterator();
                        while(it.hasNext()) {
                            tempThreadId = (Long)it.next();
                            break;
                        }
                    }
                    confirmDeleteGnThreadDialog(
                            new GnDeleteThreadListener(mListView, tempThreadId, mQueryHandler,
                                    AuroraPrivConvActivity.this), isAllSelected, starFlag,
                                    AuroraPrivConvActivity.this);
                }
                cursor.close();
                break;
            case THREAD_LIST_FAVORITE_QUERY_ONE_TOKEN:
                long onethread = (Long)cookie;
                if (MmsApp.mGnDeleteRecordSupport && mAllSelectedThreadIds != null) {
                    mAllSelectedThreadIds.clear();
                    mAllSelectedThreadIds.add(onethread);
                }
                if(mActionBatchHandler != null) {
                    mActionBatchHandler.leaveSelectionMode();
                }
                auroraDeleteConversations(mListView, onethread, mQueryHandler, AuroraPrivConvActivity.this, true);
                break;
            case THREAD_LIST_FAVORITE_QUERY_ALL_TOKEN:
                starFlag = (cursor != null && cursor.getCount() > 0);
                confirmDeleteGnThreadDialog(
                        new GnDeleteThreadListener(mListView, -1, mQueryHandler,
                                AuroraPrivConvActivity.this), true, starFlag,
                                AuroraPrivConvActivity.this);
                cursor.close();
                break;
            default:
            }
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            mIsDeleting = false;
            if (mListView != null) {
                if (!(isBottomMenuShowing())) {
                    mListView.auroraSetNeedSlideDelete(true);
                }
            }
            CBMessagingNotification.updateNewMessageIndicator(AuroraPrivConvActivity.this);
            switch (token) {
                case DELETE_CONVERSATION_NOT_LAST_TOKEN:
                    //Not the Last , Nothing to do here.
                    break;
                case DELETE_CONVERSATION_TOKEN:
                    try {
                        if(GnPhone.phone != null) {
                            if(GnITelephony.isTestIccCard(GnPhone.phone)) {
                                GnSmsManager.getDefault().setSmsMemoryStatus(true);
                            }
                        } else {
                            Log.d(CONV_TAG, "Telephony service is not available!");
                        }
                    } catch(Exception ex) {
                        Log.e(CONV_TAG, " " + ex.getMessage());
                    }
                    // Update the notification for new messages since they
                    // may be deleted.
                    // avoid ANR sometimes
                    new Thread(new Runnable() {
    
                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            // Update the notification for failed messages since they
                            // may be deleted.
                            MessagingNotification.nonBlockingUpdateNewMessageIndicator(AuroraPrivConvActivity.this, false, false);
                            MessagingNotification.updateSendFailedNotification(AuroraPrivConvActivity.this);
                            MessagingNotification.updateDownloadFailedNotification(AuroraPrivConvActivity.this);
                            //Update the notification for new WAP Push messages
                            if(FeatureOption.MTK_WAPPUSH_SUPPORT){
                                WapPushMessagingNotification.nonBlockingUpdateNewMessageIndicator(AuroraPrivConvActivity.this,false);
                            }
                        }
                        
                    }).start();
                    AuroraPrivConvActivity.this.startQuery();
                    Toast.makeText(AuroraPrivConvActivity.this, R.string.conversation_has_deleted, Toast.LENGTH_SHORT).show();
                    if(mActionBatchHandler != null) {
                        mActionBatchHandler.leaveSelectionMode();
                    }
                    dismissProgressDialog();
                    break;
    
                case DELETE_OBSOLETE_THREADS_TOKEN:
                    // Nothing to do here.
                    break;
                    
                case MUL_DELETE_CONVERSATIONS_TOKEN:
                    try {
                        if(GnPhone.phone != null) {
                            if(GnITelephony.isTestIccCard(GnPhone.phone)) {
                                Log.d(CONV_TAG, "All threads has been deleted, send notification..");
                                GnSmsManager.getDefault().setSmsMemoryStatus(true);
                            }
                        } else {
                            Log.d(CONV_TAG, "Telephony service is not available!");
                        }
                    } catch(Exception ex) {
                        Log.e(CONV_TAG, " " + ex.getMessage());
                    }
                    // avoid ANR sometimes
                    new Thread(new Runnable() {
    
                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            // Update the notification for failed messages since they
                            // may be deleted.
                            MessagingNotification.nonBlockingUpdateNewMessageIndicator(AuroraPrivConvActivity.this, false, false);
                            MessagingNotification.updateSendFailedNotification(AuroraPrivConvActivity.this);
                            MessagingNotification.updateDownloadFailedNotification(AuroraPrivConvActivity.this);
                            //Update the notification for new WAP Push messages
                            if(FeatureOption.MTK_WAPPUSH_SUPPORT){
                                WapPushMessagingNotification.nonBlockingUpdateNewMessageIndicator(AuroraPrivConvActivity.this,false);
                            }
                        }
                        
                    }).start();
                    // Make sure the list reflects the delete
                    AuroraPrivConvActivity.this.startQuery();
                    if (progress()) {
                        Toast.makeText(AuroraPrivConvActivity.this, R.string.conversation_has_deleted, Toast.LENGTH_SHORT).show();
                         if(mActionBatchHandler != null) {
                            mActionBatchHandler.leaveSelectionMode();
                        }
                        dismissProgressDialog();
                    }
                    break;    
            }
        }
    }

    /**
     * The base class about the handler with progress dialog function.
     */
    public static abstract class BaseProgressQueryHandler extends AsyncQueryHandler {
        private AuroraProgressDialog dialog;
        private int progress;
        
        public BaseProgressQueryHandler(ContentResolver resolver) {
            super(resolver);
        }
        
        /**
         * Sets the progress dialog.
         * @param dialog the progress dialog.
         */
        public void setProgressDialog(AuroraProgressDialog dialog) {
            this.dialog = dialog;
        }
        
        /**
         * Sets the max progress.
         * @param max the max progress.
         */
        public void setMax(int max) {
            if (dialog != null) {
                dialog.setMax(max);
            }
        }
        
        /**
         * Shows the progress dialog. Must be in UI thread.
         */
        public void showProgressDialog() {
            if (dialog != null) {
                dialog.show();
            }
        }
        
        /**
         * Rolls the progress as + 1.
         * @return if progress >= max.
         */
        protected boolean progress() {
            if (dialog != null) {
                return ++progress >= dialog.getMax();
            } else {
                return false;
            }
        }
        
        /**
         * Dismisses the progress dialog.
         */
        protected void dismissProgressDialog() {
            try {
                dialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                dialog = null;
            }
        }
    }

    public static AuroraAlertDialog getDelAllDialog(){
        return mDelAllDialog;
    }

    private final OnKeyListener mThreadListKeyListener = new OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DEL: {
                        long id = mListView.getSelectedItemId();
                        if (id > 0) {
                            gnDeleteConversations();
                        }
                        return true;
                    }
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
    };

    @Override
    public void onDraftChanged(long threadId, boolean hasDraft) {
        // TODO Auto-generated method stub
        if (mNeedSetDataChanged) {
            mQueryHandler.post(new Runnable() {
                public void run() {
                    mListAdapter.notifyDataSetChanged();
                }
            });
        }
        mNeedSetDataChanged = true;
    }
    
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
            int position, long id) {
        // TODO Auto-generated method stub
        if(null == mActionBatchHandler) {
            initThreadsMap();
            initActionBatchMode();
            mActionBatchHandler.enterSelectionMode(false, id);
            return true;
        }
        return false;
    }
    public void leaveBatchMode() {
        if(null != mActionBatchHandler) {
            mActionBatchHandler.leaveSelectionMode();
      }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
         switch (keyCode) {
             case KeyEvent.KEYCODE_BACK:
                 if (null != mActionBatchHandler && mActionBatchHandler.isInSelectionMode()) {
                     mActionBatchHandler.leaveSelectionMode();
                     return true;
                  }
         }
         return super.onKeyDown(keyCode, event);
    }
    
    private OnAuroraActionBarBackItemClickListener auroraActionBarBackItemClickListener = new OnAuroraActionBarBackItemClickListener() {
        
        @Override
        public void onAuroraActionBarBackItemClicked(int itemId) {
            switch (itemId) {
            case OnAuroraActionBarBackItemClickListener.HOME_ITEM:
                if (null != mActionBatchHandler && mActionBatchHandler.isInSelectionMode()) {
                    mActionBatchHandler.leaveSelectionMode();
                    return;
                }
                AuroraPrivConvActivity.this.finish();
                break;

            default:
                break;
            }
            
        }
    };
    
    public boolean isBottomMenuShowing() {
        if (mBottomAuroraMenu != null && mBottomAuroraMenu.isShowing()) {
            return true;
        }
        return false;
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
            if (mNeedSetDataChanged) {
                mListAdapter.notifyDataSetChanged();
            }
            mNeedSetDataChanged = true;
        }
        this.setMenuEnable(true);
        mActionBatchHandler = null;
        if(null != mDelAllDialog && !mDelAllDialog.isShowing()) {
            mListChange = false;
        }
        if (mThreadsMap != null) {
            mThreadsMap.clear();
        }
        if (mSetEncryption != null) {
            mSetEncryption.clear();
        }
    }
    
    protected void initActionBatchHandler() {
        mActionBatchHandler = new AuroraActionBatchHandler<Long>(this, mAuroraActionBar) {
            
            @Override
            public void enterSelectionMode(boolean autoLeave, Long itemPressing) {
                if (mListView != null) {
                    mListView.auroraSetNeedSlideDelete(false);
                }
                mListView.auroraEnableSelector(false);
                mListAdapter.showCheckBox(true);
                mListAdapter.setCheckBoxAnim(true);
                AuroraPrivConvActivity.this.setMenuEnable(false);
                super.enterSelectionMode(autoLeave, itemPressing);
                if (mNeedSetDataChanged) {
                    mListAdapter.notifyDataSetChanged();
                }
                mNeedSetDataChanged = true;
            }

            @Override
            public Set getDataSet() {
                // TODO Auto-generated method stub
                Set<Long> dataSet = new HashSet<Long>(mThreadsMap.size());
                for(int i = 0; i < mThreadsMap.size(); i++)
                    dataSet.add(mThreadsMap.get(i));
                return dataSet;
            }
            
            @Override
            public void leaveSelectionMode() {
                if (mAuroraMenu != null) {
                    mAuroraActionBar.setShowBottomBarMenu(false);
                    mAuroraActionBar.showActionBarDashBoard();
                    onBottomMenuDismiss();
                }
            }

            @Override
            public void updateUi() {
                // TODO Auto-generated method stub
                mSelectCount = null != getSelected() ? getSelected().size() : 0;
                // Aurora xuyong 2016-04-08 added for aurora 2.0 new feature start
                mBottomAuroraMenu.setBottomMenuItemEnable(0, mSelectCount == 0 ? false : true);
                // Aurora xuyong 2016-04-08 added for aurora 2.0 new feature end
                mBottomAuroraMenu.setBottomMenuItemEnable(1, mSelectCount == 0 ? false : true);
            }

            @Override
            public void updateListView(int allShow) {
                mListAdapter.updateAllCheckBox(allShow);
                if (mNeedSetDataChanged) {
                    mListAdapter.notifyDataSetChanged();
                }
                mNeedSetDataChanged = true;
            }
            
            public void bindToAdapter(GnSelectionManager<Long> selectionManager) {
                // TODO Auto-generated method stub
                mListAdapter.setSelectionManager(selectionManager);
                if (selectionManager != null) {
                    selectionManager.setAdapter(mListAdapter);
                }
            }
        };
        mActionBatchHandler.setHandler(mHandler);
    }
    
    AuroraActionBar mAuroraActionBar;
    
    private void initActionBatchMode() {
         mAuroraActionBar = this.getAuroraActionBar();
         // Aurora xuyong 2016-01-14 modified for aurora 2.0 new feature start
         this.setAuroraBottomBarMenuCallBack(mAuroraMenuCallBack); //必须写在布局前
         // Aurora xuyong 2016-01-14 modified for aurora 2.0 new feature end
         int menuId = R.menu.aurora_list_menu_delete;
         // Aurora xuyong 2016-0-08 modified for bug #22013 start
         mAuroraActionBar.initActionBottomBarMenu(menuId, 2);
         // Aurora xuyong 2016-0-08 modified for bug #22013 end
         mAuroraActionBar.showActionBarDashBoard();
         mAuroraMenu = mAuroraActionBar.getActionBarMenu();
         mBottomAuroraMenu = mAuroraActionBar.getAuroraActionBottomBarMenu();
         initActionBatchHandler();
         mAuroraMenu.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                onBottomMenuDismiss();
            }
         });
    }
    
    private static boolean mNeedShowDialog = true;
    // Aurora xuyong 2016-04-08 added for aurora 2.0 new feature start
    private Runnable mMarkConvAsReadRunnable = new Runnable() {

        @Override
        public void run() {
            markConvHaveReaded(AuroraPrivConvActivity.this, (ArrayList<Long>)mAllSelectedThreadIds.clone());
        }

    };

    private void markConvHaveReaded(Context context, ArrayList<Long> list) {
        int markCount = list.size();
        for (int i = 0; i < markCount; i++) {
            long threadId = list.get(i);
            Conversation conv = Conversation.get(context, list.get(i), true);
            if (conv.hasUnreadMessages()) {
                markAsRead(context, threadId);
            }
        }
        MessagingNotification.nonBlockingUpdateNewMessageIndicator(context, false, false);
    }

    private void markAsRead(Context context, final long threadId){
        Uri threadUri = ContentUris.withAppendedId(Threads.CONTENT_URI, threadId);

        ContentValues readContentValues = new ContentValues(1);
        readContentValues.put("read", 1);
        try {
            context.getContentResolver().update(threadUri, readContentValues,
                    "read=0", null);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }
    // Aurora xuyong 2016-04-08 added for aurora 2.0 new feature end
    private OnAuroraMenuItemClickListener mAuroraMenuCallBack = new OnAuroraMenuItemClickListener() {

        @Override
        public void auroraMenuItemClick(int itemId) {
            switch (itemId) {
            case R.id.aurora_list_menu_del:
                if (!mNeedShowDialog) {
                    return;
                }
                mNeedShowDialog = false;
                mAllSelectedThreadIds.clear();
                mAllSelectedThreadIds = (ArrayList<Long>)mActionBatchHandler.getSelected().clone();
                deleteConversations();
                break;
            // Aurora xuyong 2016-04-08 added for aurora 2.0 new feature start
            case R.id.aurora_list_menu_read_mark:
                mAllSelectedThreadIds = (ArrayList<Long>)mActionBatchHandler.getSelected().clone();
                new Thread(mMarkConvAsReadRunnable).start();
                leaveBatchMode();
                break;
            // Aurora xuyong 2016-04-08 added for aurora 2.0 new feature end
            default:
                break;
            }
        }
    };

    private void openThread(long threadId, int type) {
        if(FeatureOption.MTK_WAPPUSH_SUPPORT == true){
             startActivity(ComposeMessageActivity.createIntent(this, threadId).putExtra("quick_query", true));
        }else{
             startActivity(ComposeMessageActivity.createIntent(this, threadId).putExtra("quick_query", true));
        }
    }
    // Aurora xuyong 2014-10-04 added for bug #9597 start
    private void openThread(long threadId, int type, long privacy) {
        Intent threadIntent = ComposeMessageActivity.createIntent(this, threadId).putExtra("quick_query", true);
        threadIntent.putExtra("is_privacy", privacy);
        startActivity(threadIntent);
    }
    // Aurora xuyong 2014-10-04 added for bug #9597 end
    private final Runnable mDeleteObsoleteThreadsRunnable = new Runnable() {
        public void run() {
            if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                LogTag.debug("mDeleteObsoleteThreadsRunnable getSavingDraft(): " +
                        DraftCache.getInstance().getSavingDraft());
            }
            if (DraftCache.getInstance().getSavingDraft()) {
                // We're still saving a draft. Try again in a second. We don't want to delete
                // any threads out from under the draft.
                mHandler.postDelayed(mDeleteObsoleteThreadsRunnable, 1000);
            } else {
                MessageUtils.asyncDeleteOldMms();
                if (mListView != null) {
                        mListView.auroraSetNeedSlideDelete(false);
                }
                Conversation.asyncDeleteObsoleteThreads(mQueryHandler,
                        DELETE_OBSOLETE_THREADS_TOKEN);
            }
            backDeleteThead();
        }
    };

    static class DeleteProgressDialogUtil {
        /**
         * Gets a delete progress dialog.
         * @param context the activity context.
         * @return the delete progress dialog.
         */
        public static AuroraProgressDialog getProgressDialog(Context context) {
            AuroraProgressDialog dialog = new AuroraProgressDialog(context);
            dialog.setMessage(context.getString(R.string.deleting));
            dialog.setMax(1); /* default is one complete */
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            return dialog;
        }
    }

    private void startQueryLockedAndStaredMsg(long threadId) {
        if (mListView != null) {
            mListView.auroraSetNeedSlideDelete(false);
        }
        Conversation.startQueryHaveLockedMessages(mQueryHandler, threadId,
                THREAD_LIST_LOCKED_QUERY_TOKEN);
        Conversation.startQueryHaveStarMessages(mQueryHandler, threadId,
                THREAD_LIST_FAVORITE_QUERY_TOKEN);
    }

    public static void startQueryStaredMsg(long threadId) {
        int tag;
        if (threadId == -1) {
            tag = THREAD_LIST_FAVORITE_QUERY_ALL_TOKEN;
        } else {
            tag = THREAD_LIST_FAVORITE_QUERY_ONE_TOKEN;
        }
        if (mListView != null) {
            mListView.auroraSetNeedSlideDelete(false);
        }
        Conversation.startQueryHaveStarMessages(mQueryHandler, threadId,
                tag);
    }

    public static class GnDeleteThreadListener implements OnClickListener {
        private long mThreadId;
        private final AsyncQueryHandler mHandler;
        private final Context mContext;
        private boolean mDeleteStaredMessages;
        private AuroraListView listView;

        public GnDeleteThreadListener(AuroraListView listView, long threadId, AsyncQueryHandler handler, Context context) {
            mThreadId = threadId;
            mHandler = handler;
            mContext = context;
            this.listView = listView;
        }

        public void setDeleteStaredMessage(boolean deleteStaredMessages) {
            mDeleteStaredMessages = deleteStaredMessages;
        }

        private void markAsRead(final long threadId){
            Uri threadUri = ContentUris.withAppendedId(Threads.CONTENT_URI, mThreadId);

            ContentValues readContentValues = new ContentValues(1);
            readContentValues.put("read", 1);
            mContext.getContentResolver().update(threadUri, readContentValues,
                    "read=0", null);
        }
        public void onClick(DialogInterface dialog, final int whichButton) {
            dialog.dismiss();
            auroraDeleteConversations(listView, mThreadId, mHandler, mContext, mDeleteStaredMessages);
        }
        
    }

    private static void showProgressDialog(Context context, AsyncQueryHandler handler) {
        if (handler instanceof BaseProgressQueryHandler) {
            ((BaseProgressQueryHandler) handler).setProgressDialog(
                    DeleteProgressDialogUtil.getProgressDialog(context));
            ((BaseProgressQueryHandler) handler).showProgressDialog();
        }
    }
    
    private static boolean mIsDeleting = false;
    private static void auroraDeleteConversations(final AuroraListView listView, long thread_Id, final AsyncQueryHandler handler, final Context context, final boolean deleteStaredMessages) {
        if(thread_Id == -1 && mListChange) {
            thread_Id = 9999;
        }
        final long threadId = thread_Id;
        MessageUtils.handleReadReport(context, threadId,
                PduHeaders.READ_STATUS__DELETED_WITHOUT_BEING_READ, new Runnable() {
            public void run() {
                mIsDeleting = true;
                int token = DELETE_CONVERSATION_TOKEN;
                
                mListChange = false;
                if (threadId == -1) {
                         showProgressDialog(context, handler);
                    if (MmsApp.mIsDraftOpen) {
                        // has not draft
                        Uri sAllThreadsUri = Threads.CONTENT_URI.buildUpon().appendQueryParameter("simple", "true").build();
                        Cursor c = SqliteWrapper.query(context, context.getContentResolver(), sAllThreadsUri,
                              ALL_THREADS_PROJECTION, "sim_id=-1 AND message_count=0", null, null);

                        if (c != null && c.getCount() == 0 && deleteStaredMessages) {
                            // has not draft,delete all. clear table more fast
                            Log.i("GnDeleteThreadListener", "delete all: no draft, no stared message");
                            if (mListView != null) {
                                mListView.auroraSetNeedSlideDelete(false);
                            }
                            Conversation.startDeleteAll(handler, token, true, AuroraPrivacyUtils.getCurrentAccountId());
                            c.close();
                            DraftCache.getInstance().refresh();
                            return ;
                        }
                    } else {
                        if (deleteStaredMessages) {
                            if (mListView != null) {
                                mListView.auroraSetNeedSlideDelete(false);
                            }
                            Conversation.startDeleteAll(handler, token, true, AuroraPrivacyUtils.getCurrentAccountId());
                            DraftCache.getInstance().refresh();
                            return ;
                        }
                    }
                }else if (threadId == 9999) { // multi delete
                        showProgressDialog(context, handler);
                    String threadidString = null;
                    if (mThreadsList.size() == 1) {
                        if (mListView != null) {
                            mListView.auroraSetNeedSlideDelete(false);
                        }
                        Conversation.startDelete(handler, MUL_DELETE_CONVERSATIONS_TOKEN,
                                deleteStaredMessages, mThreadsList.get(0), "OneLast", true);
                    } else {
                        int size = mThreadsList.size();
                        for (int i = 0; i < size; i++) {
                            threadidString = mThreadsList.get(i);
                            if (i == size - 1) {
                                if (mListView != null) {
                                    mListView.auroraSetNeedSlideDelete(false);
                                }
                                Conversation.startDelete(handler, MUL_DELETE_CONVERSATIONS_TOKEN,
                                        deleteStaredMessages, threadidString, "MultLast", true);
                            } else {
                                if (mListView != null) {
                                    mListView.auroraSetNeedSlideDelete(false);
                                }
                                Conversation.startDelete(handler, DELETE_CONVERSATION_NOT_LAST_TOKEN,
                                        deleteStaredMessages, threadidString, null, true);
                            }
                        }
                    }
                    DraftCache.getInstance().refresh();
                } else {
                    mDeletedAQHandler = handler;
                    mDeletedToken = token;
                    mDeletedStaredMessages = deleteStaredMessages;
                    mDeletedThreadId = threadId;
                    if (listView != null) {
                        listView.auroraDeleteSelectedItemAnim();
                    }
                    if (mNeedDeleteHereFlag) {
                        if (mListView != null) {
                            mListView.auroraSetNeedSlideDelete(false);
                        }
                        Conversation.startDeletestar(handler, token,
                                deleteStaredMessages, threadId);
                        DraftCache.getInstance().setDraftState(threadId, false);
                    }
                    mNeedDeleteHereFlag = true;
                }
            }

        });
        if (MmsApp.mGnDeleteRecordSupport) {
            handler.post(mDeleteRecordRunnable);
        }
    }

    static boolean mNeedSetDataChanged = true;
    static boolean mNeedDeleteHereFlag = true;
    static AsyncQueryHandler mDeletedAQHandler;
    static int mDeletedToken;
    static boolean mDeletedStaredMessages;
    static long mDeletedThreadId;
    
    private static void deleteRecord() {
        Conversation conv;
        for(long id : mAllSelectedThreadIds) {
            conv = Conversation.get(id);
            if(conv != null) {
                for (String number : conv.getRecipients().getNumbers()) {
                    RecentContact.deleteRecordByNumber(number);
                }
            }
        }
    }
    
    private static Runnable mDeleteRecordRunnable = new Runnable() {
        
        @Override
        public void run() {
            // TODO Auto-generated method stub
            deleteRecord();
        }
    };

    @SuppressWarnings("null")
    private static long[] getSelectThreadIds() {
        int i = 0;
        long[] threadIds = new long[mAllSelectedThreadIds.size()];
        for (long id:mAllSelectedThreadIds) {
            threadIds[i] = id;
            i++;
        }
        return threadIds;
    }

    public static AuroraAlertDialog confirmDeleteGnThreadDialog(final GnDeleteThreadListener listener,
            boolean deleteAll,
            boolean hasFavoriteMessages,
            Context context) {
        listener.setDeleteStaredMessage(true);
        String message = context.getString(R.string.confirm_delete_one_conversation);;
        if (mAllSelectedThreadIds.size() != 1) {
            // Aurora xuyong 2016-04-08 modified for aurora 2.0 new feature start
            message = deleteAll ? context.getString(R.string.confirm_delete_all_conversations) : context.getString(R.string.confirm_delete_selected_conversations, mAllSelectedThreadIds.size());
            // Aurora xuyong 2016-04-08 modified for aurora 2.0 new feature end
        }
        AuroraAlertDialog.Builder builder = null;
        builder = new AuroraAlertDialog.Builder(context);
        mDelAllDialog = builder/*.setTitle(R.string.confirm_dialog_title)*/
                .setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        // TODO Auto-generated method stub
                        mNeedShowDialog = true;
                    }
                })
                .setTitle(message)
                .setCancelable(true).setPositiveButton(R.string.confirm_dialog_title, listener)
                .setNegativeButton(R.string.no, null)/*.setView(contents)*/.show();
        return mDelAllDialog;
    }

    public static void startQueryHaveGnLockedMessages(AsyncQueryHandler handler, long threadId,
            int token, String selection) {
        handler.cancelOperation(token);
        Uri uri = MmsSms.CONTENT_LOCKED_URI;
        if (threadId != 9999) {
            selection = "";
        }
        handler.startQuery(token, new Long(threadId), uri,
                ALL_THREADS_PROJECTION, selection, null, Conversations.DEFAULT_SORT_ORDER);
    }

    public static void startQueryHaveGnStaredMessages(AsyncQueryHandler handler, long threadId,
            int token, String selection) {
        handler.cancelOperation(token);
        Uri uri = Uri.parse("content://mms-sms/stared");
        if (threadId != 9999) {
            selection = null;
        }
        handler.startQuery(token, new Long(threadId), uri,
                ALL_THREADS_PROJECTION, selection, null, Conversations.DEFAULT_SORT_ORDER);
    }
    
    private void deleteConversations() {
        if(mAllSelectedThreadIds.size() > 0) {
            gnDeleteConversations();
        }
    }
    
    public void leaveForChanged() {
        if (null != mActionBatchHandler && mActionBatchHandler.isInSelectionMode()) {
            mActionBatchHandler.leaveSelectionMode();
        }
    }

    private boolean checkoutEncryption() {
        // TODO Auto-generated method stub
        for (long threadsid : mAllSelectedThreadIds) {
            if (mSetEncryption.contains(threadsid)) {
                return true;
            }
        }
        return false;
    }

    private void gnDeleteConversations() {
        int len = mAllSelectedThreadIds.size();
        if (len > 0) {
            deleteThreads();
            boolean hasStar = false;
            boolean isCheckAll = false;
            if (mAllSelectedThreadIds.size() == mListAdapter.getCount()) {
                isCheckAll = true;
            }
            long threadId = -1;
            if (len == 1) {
                threadId = mAllSelectedThreadIds.get(0);
            }else {
                threadId = (isCheckAll) ? -1 : 9999;
            }
            GnDeleteThreadListener listener = new GnDeleteThreadListener(mListView, threadId ,
                    mQueryHandler, this);
            confirmDeleteGnThreadDialog(listener, isCheckAll, hasStar,
                    this);
        }
    } 

    private void initThreadsMap() {
        // TODO Auto-generated method stub
        Cursor cursor = mListAdapter.getCursor();
        if (cursor != null && !cursor.isClosed()) {
            cursor.moveToPosition(-1);
            int i = 0;
            while(cursor.moveToNext() && !cursor.isBeforeFirst() && !cursor.isAfterLast()) {
                if(cursor.getInt(13) == 1 && !mSetEncryption.contains(cursor.getLong(0))) {
                    mSetEncryption.add(cursor.getLong(0));
                }
                if (mThreadsMap.get(i) == null) {
                    mThreadsMap.put(i++, cursor.getLong(0));
                }
            }
        }
    }
    
    private static final int BatchDeleteNum = 500;
    private static ArrayList<String> mThreadsList = new ArrayList<String>();
    public static final int THREAD_LIST_FAVORITE_QUERY_ONE_TOKEN = 1013;
    public static final int THREAD_LIST_FAVORITE_QUERY_ALL_TOKEN = 1014;

    private static void deleteThreads() {
        mThreadsList.clear();
        long threadId;
        StringBuilder buf = new StringBuilder();
        int i = 0;
        Collections.sort(mAllSelectedThreadIds, new Comparator<Long>() {
            
            @Override
            public int compare(Long l1, Long l2) {
              return l1.compareTo(l2);
            }
            
        });
        Iterator<Long> it = mAllSelectedThreadIds.iterator();
        while(it.hasNext()) {
              i++;
            threadId = it.next();
            buf.append(Long.toString(threadId));
            if (i % BatchDeleteNum == 0) {
                 if (buf.length() > 0) {
                     mThreadsList.add(buf.toString());
                 }
                buf = new StringBuilder();
            } else if (it.hasNext()) {                
                   buf.append(",");
            }
        }
        if (buf.length() > 0) {
            mThreadsList.add(buf.toString());
        }
    }

    private static ArrayList<Long> mFavoriThreadsList = new ArrayList<Long>();

    private final static String[] FAVORITE_THREADS_PROJECTION = new String[] {
        Conversations.THREAD_ID
    };

    private void queryFavoriteList() {
        Cursor cursor = this.getContentResolver().query(Sms.CONTENT_URI, FAVORITE_THREADS_PROJECTION, "star=1",
                null, null);
        mFavoriThreadsList.clear();
        if (cursor != null && !cursor.isClosed()) {
            cursor.moveToPosition(-1);
            while (cursor.moveToNext() && !cursor.isBeforeFirst() && !cursor.isAfterLast()) {
                Long threadid = cursor.getLong(0);
                mFavoriThreadsList.add(threadid);
            }
            cursor.close();
        }
    }
    
    private void startQuery() {
        startAsyncQuery();
    }

    public void startNoEncryptionQuery() {
        try {
            if (mListView != null) {
                mListView.auroraSetNeedSlideDelete(false);
            }
            Conversation.startQueryForNoEncryption(mQueryHandler, THREAD_LIST_QUERY_TOKEN, true);
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(this, e);
        }
    }
}
