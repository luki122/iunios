package com.aurora.mms.ui;

import java.util.ArrayList;
import java.util.Collection;
// Aurora xuyong 2014-08-23 added for bug #7789 start
import java.util.Collections;
import java.util.Comparator;
// Aurora xuyong 2014-08-23 added for bug #7789 end
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
// Aurora xuyong 2014-04-17 deleted for bug #4318 start
//import com.android.mms.ui.MyScrollListener;
// Aurora xuyong 2014-04-17 deleted for bug #4318 end
//import com.android.mms.ui.SearchActivity;
import com.android.mms.ui.WPMessageActivity;
import com.android.mms.ui.WarnOfStorageLimitsActivity;
import com.android.mms.util.DraftCache;
import com.android.mms.util.Recycler;
import com.gionee.internal.telephony.GnPhone;
import gionee.provider.GnSettings;
import gionee.telephony.GnSmsManager;
import gionee.app.GnStatusBarManager;
import gionee.provider.GnTelephony;
import com.gionee.mms.data.RecentContact;
import com.gionee.mms.popup.PopUpView;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.pdu.PduHeaders;
//Aurora xuyong 2013-11-15 modified for google adapt end
import com.aurora.featureoption.FeatureOption;
import com.mediatek.wappush.SiExpiredCheck;

// Aurora liugj 2013-10-10 modified for aurora's new feature start
// Aurora liugj 2013-09-24 added for aurora's new feature start
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraMenu;
// Aurora liugj 2013-10-09 modified for aurora's new feature start
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
// Aurora liugj 2013-10-09 modified for aurora's new feature end
// Aurora liugj 2013-09-24 added for aurora's new feature end
import aurora.app.AuroraAlertDialog;
// Aurora xuyong 2014-07-02 added for reject feature start
import android.app.Activity;
// Aurora xuyong 2014-07-02 added for reject feature end
import android.app.ListFragment;
import android.app.SearchManager;
import android.app.SearchableInfo;
import aurora.app.AuroraProgressDialog;
import android.app.StatusBarManager;
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
// Aurora xuyong 2014-07-02 added for bug #6183 start
import android.database.ContentObserver;
// Aurora xuyong 2014-07-30 added for bug #6953 start
import android.database.CursorIndexOutOfBoundsException;
// Aurora xuyong 2014-07-30 added for bug #6953 end
// Aurora xuyong 2014-07-02 added for bug #6183 end
import android.database.Cursor;
// Aurora xuyong 2014-06-07 added for bug #5377 start
import android.database.MatrixCursor;
// Aurora xuyong 2014-06-07 added for bug #5377 end
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SqliteWrapper;
// Aurora liugj 2013-10-10 added for aurora's new feature start
import android.graphics.Bitmap;
import android.graphics.Canvas;
// Aurora liugj 2013-10-10 added for aurora's new feature end
import android.graphics.Color;
import android.net.Uri;
import android.net.Uri.Builder;
// Aurora xuyong 2014-03-17 added for bug #3064 start
import android.os.AsyncTask;
// Aurora xuyong 2014-03-17 added for bug #3064 end
import android.os.Bundle;
import android.os.Handler;
// Aurora liugj 2013-10-10 added for aurora's new feature start
import android.os.Message;
// Aurora liugj 2013-10-10 added for aurora's new feature end
import android.os.ServiceManager;
import aurora.preference.AuroraPreferenceManager;
// Aurora liugj 2013-09-20 added for aurora's new feature start
import android.provider.SearchRecentSuggestions;
// Aurora liugj 2013-09-20 added for aurora's new feature end
import android.provider.Settings;
//import gionee.provider.GnSettings.System;
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
// gionee zhouyj 2012-05-08 added for CR00594316 start
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.os.SystemProperties;
// gionee zhouyj 2012-05-08 added for CR00594316 end
// gionee zhouyj 2012-05-29 add for CR00601178 start
import com.gionee.mms.ui.CustomMenu.DropDownMenu;
import android.widget.PopupMenu;
// gionee zhouyj 2012-05-29 add for CR00601178 end
// gionee zhouyj 2012-06-11 add for CR00622467 start 
import android.content.pm.ActivityInfo;
// gionee zhouyj 2012-06-11 add for CR00622467 end

//gionee gaoj 2012-6-14 added for CR00623396 start
import com.android.internal.widget.LockPatternUtils;
import android.R.anim;
import android.app.admin.DevicePolicyManager;
// Aurora xuyong 2014-04-23 added for bug #4218 start
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
// Aurora xuyong 2014-04-23 added for bug #4218 end
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
// Aurora liugj 2013-09-20 added for aurora's new feature start
import android.widget.SearchView.OnQueryTextListener;
// Aurora liugj 2013-09-20 added for aurora's new feature end
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import aurora.widget.AuroraSpinner;
import android.widget.AdapterView.OnItemSelectedListener;
//gionee gaoj 2012-6-14 added for CR00623396 end
// gionee zhouyj 2012-07-31 add for CR00662942 start
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
// Aurora xuyong 2014-08-01 modified for bug #7038 start
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
// Aurora xuyong 2014-08-01 modified for bug #7038 END
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import com.android.mms.util.GnActionModeHandler;
import com.android.mms.util.GnSelectionManager;
import android.widget.AdapterView.OnItemLongClickListener;
//gionee zhouyj 2012-07-31 add for CR00662942 end 
//Gionee tianxiaolong 2012.8.29 add for CR00682320 begin
import android.os.SystemProperties;
//Gionee tianxiaolong 2012.8.29 add for CR00682320 end

//gionee gaoj 2012-10-15 modified for CR00705539 start
import android.provider.Telephony.Sms;
import com.android.mms.widget.MmsWidgetProvider;
// Aurora liugj 2013-09-20 added for aurora's new feature start
import com.aurora.mms.countmanage.TotalCount;
import com.aurora.mms.search.MessageSearchListAdapter;
import com.aurora.mms.util.Utils;
// Aurora liugj 2013-09-20 added for aurora's new feature end
//gionee gaoj 2012-10-15 modified for CR00705539 end
//Gionee <zhouyj> <2013-04-26> add for CR00802651 start
import android.provider.Contacts.People;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Intents.Insert;
//Gionee <zhouyj> <2013-04-26> add for CR00802651 end

//Gionee <gaoj> <2013-05-28> add for CR00817770 begin
import aurora.widget.AuroraSmartPopupLayout;
import aurora.widget.AuroraSmartLayout;
import aurora.widget.AuroraCheckBox;
import android.widget.FrameLayout;
import android.content.SharedPreferences.Editor;
import android.widget.LinearLayout.LayoutParams;
import android.view.Gravity;
//Gionee <gaoj> <2013-05-28> add for CR00817770 end
import aurora.app.AuroraActivity;
import com.gionee.mms.ui.DraftFragment;
import com.gionee.mms.ui.MsgChooseLockPassword;

// Aurora liugj 2013-09-24 added for aurora's new feature start
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
// Aurora liugj 2013-09-24 added for aurora's new feature end
// Aurora liugj 2013-10-10 modified for aurora's new feature end

// Aurora liugj 2013-10-10 modified for aurora's new feature start
// Aurora liugj 2013-12-03 modified for checkbox and search animation
public class ConvFragment extends ListFragment implements 
     DraftCache.OnDraftChangedListener, OnItemLongClickListener,
     AuroraSearchView.OnQueryTextListener, AuroraSearchView.OnCloseListener,
    // Aurora xuyong 2014-08-25 modified for bug #7919 start
     AuroraActivity.OnSearchViewQuitListener, View.OnTouchListener{
    // Aurora xuyong 2014-08-25 modified for bug #7919 end
// Aurora liugj 2013-10-10 modified for aurora's new feature end
    
    // Aurora liugj 2013-10-08 modified for aurora's new feature start
    private static final String DEFAULT_SORT_ORDER = "sms.date DESC";
    private static final String SMS_ID = "_id";
    private static final String SMS_THREAD_ID = "thread_id";
    private static final String SMS_ADDRESS = "address";
    private static final String SMS_DATE = "date";
    private static final String SMS_BODY = "body";
    // Aurora xuyong 2014-02-13 added for bug #11290 start
    private static final String MMS_SUB = "sub";
    private static final String MMS_SUB_CS = "sub_cs";
    private static final String MSG_TYPE = "auroramsgtype";
    private static final String WORDS_INDEX_TEXT = "index_text";
    private static final String WORDS_ID = "_id";
    // Aurora xuyong 2014-02-13 added for bug #11290 end
    
    protected static final String[] PROJECTION_SMS = new String[] {
        SMS_ID,
        SMS_THREAD_ID,
        SMS_ADDRESS,
        // Aurora xuyong 2014-02-13 modified for bug #11290 start
        SMS_BODY,
        SMS_DATE,
        MMS_SUB,
        MMS_SUB_CS,
        MSG_TYPE,
        WORDS_INDEX_TEXT,
        WORDS_ID
        // Aurora xuyong 2014-02-13 modified for bug #11290 end
    };
    // Aurora liugj 2013-10-08 modified for aurora's new feature end

    // Aurora liugj 2013-09-20 modified for aurora's new feature end
    private static ThreadListQueryHandler mQueryHandler;
    private ConversationListAdapter mListAdapter;
    // Aurora liugj 2013-09-29 added for aurora's new feature start
    private MessageSearchListAdapter mAdapter;
    // Aurora liugj 2013-09-29 added for aurora's new feature end
    private static final int THREAD_LIST_QUERY_TOKEN       = 1701;
    public static final int DELETE_CONVERSATION_TOKEN      = 1801;
    public static final int HAVE_LOCKED_MESSAGES_TOKEN     = 1802;
    private static final int DELETE_OBSOLETE_THREADS_TOKEN = 1803;
    public static final int HAVE_STAR_MESSAGES_TOKEN       = 1804;
     // Aurora liugj 2014-01-06 modified for bath-delete optimize start
    public static final int MUL_DELETE_CONVERSATIONS_TOKEN = 1805;
    
    public static final int DELETE_CONVERSATION_NOT_LAST_TOKEN = 1901;
     // Aurora liugj 2014-01-06 modified for bath-delete optimize end
    public static final int THREAD_LIST_LOCKED_QUERY_TOKEN  = 1902;
    public static final int THREAD_LIST_FAVORITE_QUERY_TOKEN = 1903;
    private static ArrayList<Long> mAllSelectedThreadIds = new ArrayList<Long>();
    // Aurora xuyong 2014-07-02 added for reject feature start
    private static Map<Integer, Conversation> mAllSelectedConvsCache = new HashMap<Integer, Conversation> ();
    // Aurora xuyong 2014-07-02 added for reject feature end
    public static final String[] ALL_THREADS_PROJECTION = {
        Threads._ID, Threads.DATE, Threads.MESSAGE_COUNT, Threads.RECIPIENT_IDS,
        Threads.SNIPPET, Threads.SNIPPET_CHARSET, Threads.READ, Threads.ERROR,
        Threads.HAS_ATTACHMENT, Threads.TYPE, Threads.SIM_ID
    };
     // Aurora liugj 2013-11-02 modified for aurora's new feature start
    private static int CHANGE_SCROLL_LISTENER_MIN_CURSOR_COUNT = 500;
     // Aurora liugj 2013-11-02 modified for aurora's new feature end
    // Aurora xuyong 2014-05-05 modified for aurora's new feature start
    // Aurora xuyong 2014-06-07 modidfied for bug #5377 start
    // Aurora yudingmin 2014-09-01 modidfied for optimize start
//    private boolean mHasFinishAll = false;
    private final int INIT_NONE = 0;
    private final int INIT_INIT_ITEM_COUNT_DONE = 1;
    private final int INIT_ALL_DONE = 2;
    private int mInitStatu = INIT_NONE;
    // Aurora yudingmin 2014-09-01 modidfied for optimize end
    // Aurora xuyong 2014-08-27 modified for aurora's new feature start
 // Aurora yudingmin 2014-08-30 deleted for optimize start
//    private int mQueryId = 0;
 // Aurora yudingmin 2014-08-30 deleted for optimize end
    // Aurora xuyong 2014-08-27 modified for aurora's new feature end
    // Aurora xuyong 2014-06-17 added for aurora's new feature start
    private static final int CHANGE_MC1_CURSOR = 11;
    private static final int INIT_ITEM_COUNT = 10;
 // Aurora yudingmin 2014-08-30 added for optimize start
    private static final int POST_ITEM_COUNT = 100;
    // Aurora yudingmin 2014-09-02 added for optimize start
    private  int mInitedCount = 0;
    private boolean mScrollToBottom = true;
 // Aurora yudingmin 2014-09-02 added for optimize end
 // Aurora yudingmin 2014-08-30 added for optimize end
    // Aurora xuyong 2014-06-17 added for aurora's new feature end
    //Aurora xuyong 2014-07-04 added for reject feature start
    public static final int DIS_SELECT_ALL = 12;
    public static final int SELECT_ALL = 13;
    //Aurora xuyong 2014-07-04 added for reject feature end
    // Aurora xuyong 2014-07-16 added for reject feature start
    private static final int FINISH_ACTIVITY = 14;
    // Aurora xuyong 2014-08-27 modified for aurora's new feature start
    private static final int CHANGE_MC2_CURSOR = 15;
    // Aurora xuyong 2014-08-27 modified for aurora's new feature end
    // Aurora yudingmin 2014-12-11 added for optimize start
    private static final int LOAD_ALL_CONTACT_CHANGE = 16;
    private static final int LOAD_ALL_CONTACT_DONE = 17;
    // Aurora yudingmin 2014-12-11 added for optimize end
    AuroraProgressDialog mProgressDialog = null;
    // Aurora xuyong 2014-07-16 added for reject feature end
    // Aurora xuyong 2014-08-01 added for bug #7038 start
    ExecutorService mSingleThreadExecutor = Executors.newSingleThreadExecutor();
    // Aurora xuyong 2014-08-27 modified for aurora's new feature start
    ExecutorService mSingleThreadExecutor2 = Executors.newSingleThreadExecutor();
    // Aurora xuyong 2014-08-27 modified for aurora's new feature end
    // Aurora xuyong 2014-08-01 added for bug #7038 end
    private Handler mHandler = new Handler() {
      // Aurora xuyong 2014-06-17 added for aurora's new feature start
       // Aurora xuyong 2014-07-07 modified for reject feature start
      // Aurora xuyong 2014-08-27 modified for aurora's new feature start
        QueryResult initResult;
      // Aurora xuyong 2014-08-27 modified for aurora's new feature end
       // Aurora xuyong 2014-07-07 modified for reject feature end
        // Aurora xuyong 2014-06-17 added for aurora's new feature end
        @Override
        public void handleMessage(Message msg) {
          // Aurora xuyong 2014-06-17 modified for aurora's new feature start
            switch(msg.what) {
            // Aurora yudingmin 2014-12-11 added for optimize start
            case LOAD_ALL_CONTACT_CHANGE:
                int changeCount = msg.arg1;
                mListAdapter.notifyCountSetChanged(changeCount);
                break;
            case LOAD_ALL_CONTACT_DONE:
                if(mListAdapter != null){
                    Cursor listCursor = mListAdapter.getCursor();
                    // Aurora xuyong 2015-09-06 modified for bug #16158 start
                    if (listCursor == null || listCursor.isClosed()) {
                    	mListAdapter.notifyCountSetChanged(0);
                    } else {
                    	mListAdapter.notifyCountSetChanged(listCursor.getCount());
                    }
                    // Aurora xuyong 2015-09-06 modified for bug #16158 end
                }
                break;
                // Aurora yudingmin 2014-12-11 added for optimize end
             // Aurora xuyong 2014-07-16 added for reject feature start
                case FINISH_ACTIVITY:
                    leaveBatchMode();
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                    // Aurora xuyong 2014-11-12 modified for bug #9759 start
                    Activity activity = AuroraConvListActivity.sAuroraConvListActivity;
                    // Aurora xuyong 2014-11-12 modified for bug #9759 end
                    if (activity != null) {
                        activity.finish();
                    }
                    break;
              // Aurora xuyong 2014-07-16 added for reject feature end
                case CHANGE_MC1_CURSOR:
                // Aurora xuyong 2014-08-27 added for aurora's new feature start
                    if (msg.obj == null) {
                        operateAfterChangeCursor(null);
                        return;
                    }
                // Aurora xuyong 2014-08-27 added for aurora's new feature end
                // if mCursor is null or has been closed, we don't need to 
                // do any more operations on it, so we return here.
                // Aurora xuyong 2014-08-27 modified for aurora's new feature start
 // Aurora yudingmin 2014-08-30 deleted for optimize start
//                    initResult = (QueryResult)(msg.obj);
//                    final int tempQueryId = msg.arg1;
//                    Cursor minResult = initResult.getMinCursor();
//                    if(tempQueryId == mQueryId){
//                        mListAdapter.changeCursor(minResult);
//                        operateAfterChangeCursor(minResult);
//                    }
 // Aurora yudingmin 2014-08-30 deleted for optimize end
                // Aurora xuyong 2014-08-27 modified for aurora's new feature end
                // Aurora xuyong 2014-07-17 modified for bug #6619 start
                // Aurora xuyong 2014-07-28 modified for bug #6931 start
                // Aurora xuyong 2014-08-27 modified for aurora's new feature start
 // Aurora yudingmin 2014-08-30 modified for optimize start
                    final Cursor allCursor = (Cursor)msg.obj;
                // Aurora xuyong 2014-09-02 modified for aurora's new feature start
                    if (allCursor == null || allCursor.getCount() <= 0) {
                // Aurora xuyong 2014-09-02 modified for aurora's new feature end
                // Aurora xuyong 2014-08-27 modified for aurora's new feature end
                // Aurora xuyong 2014-07-28 modified for bug #6931 end
                // Aurora xuyong 2014-07-17 modified for bug #6619 end
                   // Aurora xuyong 2014-07-29 added for aurora's new feature start
                   // Aurora xuyong 2014-08-27 modified for aurora's new feature start
                   // Aurora xuyong 2014-09-02 added for aurora's new feature start
                        mListAdapter.changeCursor(allCursor);
                   // Aurora xuyong 2014-09-02 added for aurora's new feature end
                        operateAfterChangeCursor(allCursor);
                   // Aurora xuyong 2014-08-27 modified for aurora's new feature end
                   // Aurora xuyong 2014-07-29 added for aurora's new feature end
                        return;
                    }
                // Aurora xuyong 2014-08-26 modified for bug modify start
                // Aurora xuyong 2014-08-27 modified for aurora's new feature start
                    // Aurora yudingmin 2014-09-01 modidfied for optimize start
                    if(mInitStatu == INIT_NONE){
                        mInitStatu = INIT_INIT_ITEM_COUNT_DONE;
                        mListAdapter.changeCountBeforeDataChanged(INIT_ITEM_COUNT);
                        // Aurora yudingmin 2014-09-01 modidfied for optimize end
//                        if(allCursor.getCount() > INIT_ITEM_COUNT){
//                            loadAllContact();
//                        } else {
//                            mInitStatu = INIT_ALL_DONE;
//                        }
                   // Aurora xuyong 2014-09-02 modified for aurora's new feature end
                    } else if (mInitStatu == INIT_ALL_DONE){
                        mListAdapter.changeCountBeforeDataChanged(allCursor.getCount());
                    }
                    // Aurora yudingmin 2014-09-01 added for optimize start
                    mListView.auroraSetRubbishBackNoAnim();
                    // Aurora yudingmin 2014-09-01 added for optimize end
                    mListAdapter.changeCursor(allCursor);
                    operateAfterChangeCursor(allCursor);
 // Aurora yudingmin 2014-08-30 modified for optimize end
                // Aurora xuyong 2014-06-24 modified for aurora's new feture start
                // Aurora xuyong 2014-08-27 deleted for aurora's new feature start
                    //operateAfterChangeCursor(minReuslt);
                // Aurora xuyong 2014-08-27 deleted for aurora's new feature end
                // Aurora xuyong 2014-06-24 modified for aurora's new feture end
                    break;
              // Aurora xuyong 2014-08-27 modified for aurora's new feature start
                case CHANGE_MC2_CURSOR:
                    {
                        Cursor allResult = (Cursor)msg.obj;
                        if (mListAdapter != null) {
                            if (allResult != null && !allResult.isClosed()) {
                                mListAdapter.changeCursor(allResult);
                            }
                       // Aurora xuyong 2014-06-24 added for aurora's new feture start
                            operateAfterChangeCursor(allResult);
              // Aurora xuyong 2014-08-27 modified for aurora's new feature end
                       // Aurora xuyong 2014-06-24 added for aurora's new feture end
                        }
                    }
                    break;
             // Aurora xuyong 2014-08-27 added for aurora's new feature start
                default :
                        // more to be do here
                        break;
             // Aurora xuyong 2014-08-27 added for aurora's new feature end
            }
          // Aurora xuyong 2014-06-17 modified for aurora's new feature end
        }
        
    };
    // Aurora xuyong 2014-08-27 added for aurora's new feature start
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

 // Aurora yudingmin 2014-08-30 added for optimize start    
    private void loadAllContact(){
        mSingleThreadExecutor2.execute(new Runnable() {
            public void run() {
                Cursor prepareCursor = null;
                // Aurora xuyong 2014-11-12 modified for bug #9759 start
                Context context = AuroraConvListActivity.sContext;
                // Aurora xuyong 2014-11-12 modified for bug #9759 end
                String selection = null;
                prepareCursor = context.getContentResolver().query(Conversation.sAllThreadsUri, Conversation.ALL_THREADS_PROJECTION, selection, null, Conversations.DEFAULT_SORT_ORDER);
                mInitedCount = INIT_ITEM_COUNT;
                if(context != null){
                    if(prepareCursor != null){
                        if (prepareCursor.moveToPosition(INIT_ITEM_COUNT)) {
                            do{
                                Conversation.from(context, prepareCursor, true);
                                // Aurora yudingmin 2014-09-01 modified for optimize start
                                mInitedCount ++;
                                // Aurora xuyong 2014-10-30 modified for cursor excepion start
                                if (mListAdapter != null) {
                                    // Aurora yudingmin 2014-12-11 modified for optimize start
                                    if(mScrollToBottom && mInitStatu == INIT_INIT_ITEM_COUNT_DONE && mInitedCount - mListAdapter.getCount() > POST_ITEM_COUNT){
                                        mScrollToBottom = false;
                                        Message msg = mHandler.obtainMessage(LOAD_ALL_CONTACT_CHANGE);
                                        msg.arg1 =  mInitedCount;
                                        mHandler.sendMessage(msg);
                                    }
                                    // Aurora yudingmin 2014-12-11 modified for optimize end
                                // Aurora xuyong 2014-10-30 modified for cursor excepion end
                                }
                                // Aurora yudingmin 2014-09-01 modified for optimize end
                            }while(prepareCursor.moveToNext() && !prepareCursor.isBeforeFirst() && !prepareCursor.isAfterLast());
                        }
                        prepareCursor.close();
                    }
                }
                mInitStatu = INIT_ALL_DONE;
                // Aurora yudingmin 2014-12-11 modified for optimize start
                mHandler.sendEmptyMessage(LOAD_ALL_CONTACT_DONE);
                // Aurora yudingmin 2014-12-11 modified for optimize end
            }
        });
    }
 // Aurora yudingmin 2014-08-30 added for optimize end    
    
   // Aurora xuyong 2014-08-27 added for aurora's new feature end
    // Aurora xuyong 2014-08-26 added for bug modify start
    // Aurora xuyong 2014-08-27 modified for aurora's new feature start
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
        // Aurora xuyong 2014-11-26 modified for bug #10049 start
        Context context = AuroraConvListActivity.sContext;
        if (context == null) {
            return;
        }
        // Aurora xuyong 2014-11-26 modified for bug #10049 end
        if (mc2.moveToFirst() && !mc2.isBeforeFirst() && !mc2.isAfterLast()) {
            Conversation.from(context, mc2);
            while(mc2.moveToNext() && !mc2.isBeforeFirst() && !mc2.isAfterLast()) {
                Conversation.from(context, mc2);
            }
 // Aurora yudingmin 2014-08-30 deleted for optimize start    
//            if (queryId == mQueryId) {
//                Message msg = mHandler.obtainMessage(CHANGE_MC2_CURSOR);
//                msg.obj = mc2;
//                msg.arg1 = queryId;
//                msg.sendToTarget();
//            }
 // Aurora yudingmin 2014-08-30 deleted for optimize end    
        }
//        mHasFinishAll = true;
        if (!allResult.isClosed()) {
            allResult.close();
        }
    }
    // Aurora xuyong 2014-08-27 modified for aurora's new feature end
    // Aurora xuyong 2014-08-26 added for bug modify end
    //private Cursor mCursor = null;
    // Aurora xuyong 2014-06-07 modified for bug #5377 end
    // Aurora xuyong 2014-05-05 modified for aurora's new feature end
    private SharedPreferences mPrefs;
    static private final String CHECKED_MESSAGE_LIMITS = "checked_message_limits";
//    private PostDrawListener mPostDrawListener = new PostDrawListener();
    // Aurora xuyong 2014-04-17 deleted for bug #4318 start
    //private MyScrollListener mScrollListener;
    // Aurora xuyong 2014-04-17 deleted for bug #4318 end

    //wappush: indicates the type of thread, this exits already, but has not been used before
    private int mType;
    //wappush: SiExpired Check
    private SiExpiredCheck siExpiredCheck;
    //wappush: wappush TAG
    private static final String WP_TAG = "Mms/WapPush";

    private boolean mNeedToMarkAsSeen;
    private boolean mDataValid;
    private static AuroraAlertDialog mDelAllDialog;
    // Aurora liugj 2013-09-24 added for aurora's new feature start
    private AuroraMenu mAuroraMenu;
    private AuroraMenu mBottomAuroraMenu;
    // Aurora liugj 2013-09-24 added for aurora's new feature end

    public static final int CONFIRM_PASSWORD_REQUEST = 39;
    public static final int UPDATE_PASSWORD_REQUEST =40;
    public static final int MULTI_PASSWORD_REQUEST = 41;
    public static final int CONFIRM_DECRYPTION_PASSWORD_REQUEST = 42;
    
    // Aurora liugj 2013-10-10 modified for aurora's new feature start
    /** 激活框动画-准备 */
    //private static final int MESSAGE_ANI_PREPARE_GOTO_SEARCH = 200;
    /** 激活框动画-开始 */
    //private static final int MESSAGE_ANI_START_GOTO_SEARCH = 201;
    /** 激活框动画-进入搜索 */
    //private static final int MESSAGE_ANI_GOTO_SEARCH = 202;
    /** 框返回动画-准备 */
    //private static final int MESSAGE_ANI_PREPARE_BACK_FROM_SEARCH = 300;
    /** 框返回动画-开始 */
    //private static final int MESSAGE_ANI_START_BACK_FROM_SEARCH = 301;
    /** 框返回动画-进入首页 */
    //private static final int MESSAGE_ANI_BACK_FROM_SEARCH = 302;
    /** intent EXTRA 启动返回动画 */
    //public static final String EXTRA_START_BACK_ANIMATION = "EXTRA_START_BACK_ANIMATION";
    /** 动画handler */
    //private Handler mAnimationHandler;
    /** 激活框动画是否正在播 */
    //private boolean mHasGotoSearchAnimationPlaying;
    /** 框返回动画是否正在播 */
    //private boolean mHasBackFromSearchAnimationPlaying;
    /** 动画相关的holder */
    //private AnimationHolder mAnimationHolder;
    
    //private int mWidth, mHeight;
    
    /**
     * 截图缓存。
     */
    //private Bitmap mSnapshot;
    private View fragmentView;
    
    // Aurora liugj 2013-10-10 modified for aurora's new feature start
    private TextView mSearchView;    
    // Aurora liugj 2013-10-10 modified for aurora's new feature end
    //private Button mCancelSearchBtn;
    // Aurora liugj 2013-10-10 modified for aurora's new feature end
    // Aurora liugj 2013-10-11 modified for aurora's new feature start
   // Aurora xuyong 2014-04-21 modified for bug #4460 start
    private static AuroraListView mListView;
   // Aurora xuyong 2014-04-21 modified for bug #4460 end
    // Aurora liugj 2013-10-11 modified for aurora's new feature end
     // Aurora liugj 2014-02-07 modified for xuqiu:searchview scroll with listview start
    private boolean isInit = false;
    // we won't use this view anymore
    private TextView mSearchLayout;
    private View mListLayout;
     // Aurora liugj 2014-02-07 modified for xuqiu:searchview scroll with listview end
    private LinearLayout mEmptyView;
    private final static int MENU_EXCHANGE = 6;
    
    //gionee gaoj 2012-6-14 added for CR00623396 start
    private TextView mEmptyTextView;
    private TextView mEncryptionTitle;
    public static boolean isEncryptionList = false;
    //gionee gaoj 2012-6-14 added for CR00623396 end
    // gionee zhouyj 2012-05-17 add for CR00601094 start
    private int mSelectCount = 0;
    // gionee zhouyj 2012-05-17 add for CR00601094 end 
    // gionee zhouyj 2012-06-21 add for CR00625679 start 
    private boolean mBeingEncrypt = false;
    public static HashSet<Long> mSetEncryption = new HashSet<Long>();
    // gionee zhouyj 2012-06-21 add for CR00625679 end 
    //gionee gaoj 2012-6-30 added for CR00632246 start
    private boolean mContentChanged;
    //gionee gaoj 2012-6-30 added for CR00632246 end
    // gionee zhouyj 2012-07-31 add for CR00662942 start
    private  static GnActionModeHandler<Long> mActionModeHandler = null;
    // Aurora liugj 2013-09-24 added for aurora's new feature start
    private static AuroraActionBatchHandler<Long> mActionBatchHandler = null;
    // Aurora liugj 2013-09-24 added for aurora's new feature start
    private Map<Integer, Long> mThreadsMap = new HashMap<Integer, Long>();
    // gionee zhouyj 2012-07-31 add for CR00662942 end
    // gionee zhouyj 2012-08-08 add for CR00664390 start 
    private static boolean mListChange = false;
    // gionee zhouyj 2012-08-08 add for CR00664390 end 

    //Gionee tianxiaolong 2012.8.29 add for CR00682320 begin
    private static boolean gnFlyFlag = SystemProperties.get("ro.gn.oversea.custom").equals("RUSSIA_FLY");
    //Gionee tianxiaolong 2012.8.29 add for CR00682320 end
    
    private boolean mFirstStart = true;
    //gionee gaoj 2013-3-21 modified for CR00786905 start
    private boolean mIsonResume = true;
    //gionee gaoj 2013-3-21 modified for CR00786905 end
    // gionee zhouyj 2012-10-26 add for CR00718476 start 
    //private static boolean sInMultiMode = false;
    // gionee zhouyj 2012-10-26 add for CR00718476 end 
    //private boolean mFirstInMultiMode = false;
    //gionee gaoj added for CR00725602 20121201 start
    private Contact mContact = null;
    private boolean mHasEncryption = false;
    //Gionee <zhouyj> <2013-05-03> modify for CR00807509 begin
    //ConvFragment   ContextMenu id from 10 to 29
    private Menu mMenu ;
    private static final int MENU_DELETE       = 10;
    private static final int MENU_ENCRYPTION   = 11;
    private static final int MENU_DECRYPTION   = 12;
    private static final int MENU_VIEW_CONTACT = 13;
    private static final int MENU_NEW_CONTACT  = 14;
    private static final int MENU_ADD_CONTACT  = 15;
    //Gionee <zhouyj> <2013-05-03> modify for CR00807509 begin
    public static final int ONEDEL_CONFIRM_PASSWORD_REQUEST = 11;
    public static final int ONE_CONTEXT_PASSWORD_REQUEST = 12;
    private long mThreadID = -1;
    public boolean mSearchMode = false;
    // Aurora liugj 2013-09-20 added for aurora's new feature start
    // Aurora xuyong 2014-03-17 deleted for bug #3064 start
    //private AsyncQueryHandler mSearchHandler;
    // Aurora xuyong 2014-03-17 deleted for bug #3064 end
    private String mQueryString;
    // Aurora liugj 2013-09-20 added for aurora's new feature end
    
    //gionee gaoj added for CR00725602 20121201 end
    // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
    /*private class PostDrawListener implements android.view.ViewTreeObserver.OnPostDrawListener {
        @Override
        public boolean onPostDraw() {
            Log.i("AppLaunch", "[AppLaunch] MMS onPostDraw");
            return true;
        }
    }*/
    // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
    
    /*@Override
    public void onVisibilityChanged(boolean visible) {
        // TODO Auto-generated method stub
//        mShowOptionsMenu = true;
        //gionee gaoj 2013-3-21 modified for CR00786905 start
        mIsonResume = visible;
        //gionee gaoj 2013-3-21 modified for CR00786905 end
        // gionee zhouyj 2013-04-02 add for CR00792152 start
        if (!     visible && mActionModeHandler != null && mActionModeHandler.inSelectionMode()) {
            mActionModeHandler.leaveSelectionMode();
        }
        // gionee zhouyj 2013-04-02 add for CR00792152 end
    }*/
    // Aurora xuyong 2014-07-02 added for bug #6183 start
    private ContentObserver mConvaObserver = new ContentObserver(new Handler()) { 

        @Override
        public void onChange(boolean selfChange) { 
             super.onChange(selfChange);
             if (!mIsDeleting) {
             // Aurora xuyong 2014-08-26 modified for bug modify start
                mQueryHandler.removeCallbacks(mQueryRunnable);
                mQueryHandler.postDelayed(mQueryRunnable, 500);
             // Aurora xuyong 2014-08-26 modified for bug modify end
             }
        }   

    };
    // Aurora xuyong 2014-07-02 added for bug #6183 end
    // Aurora xuyong 2014-07-16 added for reject feature start
    private void initProgressAddingDialog(Context context) {
        mProgressDialog = new AuroraProgressDialog(context);
        // Aurora xuyong 2014-11-26 modified for bug #10049 start
        mProgressDialog.setMessage(context.getString(R.string.aurora_black_adding));
        // Aurora xuyong 2014-11-26 modified for bug #10049 end
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
    }
    // Aurora xuyong 2014-07-16 added for reject feature end
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        // Aurora xuyong 2014-11-26 modified for bug #10049 start
        Context context = AuroraConvListActivity.sContext;
        if (context == null) {
            return;
        }
        mQueryHandler = new ThreadListQueryHandler(AuroraConvListActivity.sContext.getContentResolver());
        // Aurora xuyong 2014-11-26 modified for bug #10049 end
        // Aurora xuyong 2014-05-05 deleted for aurora's new feature start
        //mHandler = new Handler();
        // Aurora xuyong 2014-05-05 deleted for aurora's new feature end
        // Aurora xuyong 2014-11-26 modified for bug #10049 start
        mPrefs = AuroraPreferenceManager.getDefaultSharedPreferences(AuroraConvListActivity.sContext);
        // Aurora xuyong 2014-11-26 modified for bug #10049 end
        /*boolean checkedMessageLimits = mPrefs.getBoolean(CHECKED_MESSAGE_LIMITS, false);
        if (!checkedMessageLimits ) {
            runOneTimeStorageLimitCheckForLegacyMessages();
        }*/
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            // Aurora xuyong 2014-11-26 modified for bug #10049 start
            siExpiredCheck = new SiExpiredCheck(AuroraConvListActivity.sContext);
            // Aurora xuyong 2014-11-26 modified for bug #10049 end
            siExpiredCheck.startSiExpiredCheckThread();
        }
        //Gionee <gaoj> <2013-05-28> add for CR00817770 begin
        if (MmsApp.mGnHideEncryption) {
            mHideEncryp = ReadPopTag(AuroraConvListActivity.sContext, HIDEENCRYPTION);
            Log.d("ConvFragment", "onCreate mHideEncryp = "+mHideEncryp);
        }
        // Aurora xuyong 2014-07-02 added for bug #6183 start
        // Aurora xuyong 2014-07-16 added for reject feature start
        //initProgressAddingDialog(activity);
        // Aurora xuyong 2014-07-16 added for reject feature end
        // Aurora xuyong 2014-08-23 deleted for bug #7789 start
        initProgressAddingDialog(context);
        // Aurora xuyong 2014-08-26 deleted for bug modify start
        //activity.getApplicationContext().getContentResolver()
        //         .registerContentObserver(Conversation.sAllThreadsUri, true, mConvaObserver);
        // Aurora xuyong 2014-08-26 deleted for bug modify end
        // Aurora xuyong 2014-07-03 added for aurora's new feature start
        // Aurora xuyong modified for aurora's new feature start
        context.getContentResolver().registerContentObserver(MmsSms.CONTENT_URI, true, mConvaObserver);
        // Aurora xuyong modified for aurora's new feature end
        // Aurora xuyong 2014-07-03 added for aurora's new feature end
        // Aurora xuyong 2014-08-23 deleted for bug #7789 end
        // Aurora xuyong 2014-07-02 added for bug #6183 end
        //Gionee <gaoj> <2013-05-28> add for CR00817770 end
        // Aurora liugj 2013-10-10 added for aurora's new feature start
        /*WindowManager winManager = (WindowManager)getActivity().getSystemService(Context.WINDOW_SERVICE);
        mWidth = winManager.getDefaultDisplay().getWidth();
        mHeight = winManager.getDefaultDisplay().getHeight() - Utils.getStatusHeight(getActivity());*/
        // Aurora liugj 2013-10-10 added for aurora's new feature end
        // Aurora xuyong 2014-03-07 added for bug #11582 start
        DraftCache.getInstance().refresh();
        // Aurora xuyong 2014-03-07 added for bug #11582 end
        loadAllContact();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        //Log.d("ConvFragment", "======onCreateView=======");
        fragmentView = inflater.inflate(R.layout.aurora_conversation_list_screen, container, false);
        
          // Aurora liugj 2014-02-07 deleted for xuqiu:searchview scroll with listview start
        //mListLayout = (RelativeLayout) fragmentView.findViewById(R.id.convlist_layout);
          // Aurora liugj 2014-02-07 deleted for xuqiu:searchview scroll with listview end
        // Tell the list view which view to display when the list is empty
        mEmptyView = (LinearLayout)fragmentView.findViewById(R.id.gn_conversation_empty);

        //gionee gaoj 2012-6-14 added for CR00623396 start
        mEmptyTextView = (TextView) fragmentView.findViewById(R.id.empty);
          // Aurora liugj 2013-10-24 modified for aurora's new feature start
        /*if (MmsApp.mDarkStyle) {
            mEmptyTextView.setTextColor(getResources().getColor(R.color.gn_dark_color_bg));
        }*/
          // Aurora liugj 2013-10-24 modified for aurora's new feature end
        mEncryptionTitle = (TextView) fragmentView.findViewById(R.id.gn_encryption_title);
        // Aurora xuyong 2014-08-25 added for bug #7919 start
        mEncryptionTitle.setOnTouchListener(this);
        // Aurora xuyong 2014-08-25 added for bug #7919 end
        //gionee gaoj 2012-6-14 added for CR00623396 end
        // Aurora liugj 2013-10-10 modified for aurora's new feature start
        // Aurora liugj 2013-10-10 modified for aurora's new feature start
          // Aurora liugj 2014-02-07 modified for xuqiu:searchview scroll with listview start
        //mSearchView    = (TextView) fragmentView.findViewById(R.id.conversation_header_searchbox);
          // Aurora liugj 2014-02-07 modified for xuqiu:searchview scroll with listview end
        // Aurora liugj 2013-10-10 modified for aurora's new feature end
        //mCancelSearchBtn = (Button) fragmentView.findViewById(R.id.search_cancel_btn);
        // Aurora liugj 2013-10-10 modified for aurora's new feature end
        return fragmentView;
    }

     // Aurora liugj 2014-02-07 modified for xuqiu:searchview scroll with listview start
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        //Log.d("ConvFragment", "======onViewCreated=======");
         // Aurora liugj 2013-10-11 modified for aurora's new feature start
        mListView = (AuroraListView) getListView();
        // Aurora xuyong 2014-08-25 added for bug #7919 start
        mListView.setOnTouchListener(this);
        // Aurora xuyong 2014-08-25 added for bug #7919 end
        // Aurora xuyong 2014-04-23 added for bug #4218 start
        // Aurora xuyong 2014-06-07 deleted for bug #5377 start
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub
                /* // Aurora xuyong 2014-04-23 modified aurora's new feature start
                    if (mListAdapter != null) {
                    // Aurora xuyong 2014-04-25 modified for aurora's new feature start
                        if (mNeedSetDataChanged) {
                            mListAdapter.notifyDataSetChanged();
                        }
                        mNeedSetDataChanged = true;
                    // Aurora xuyong 2014-04-25 modified for aurora's new feature end
                        if (mListAdapter.mNeedAnim) {
                            mListAdapter.setCheckBoxAnim(false);
                        }
                        if (mListAdapter.mAllShowCheckBox != 0) {
                            mListAdapter.updateAllCheckBox(0);
                        }
                    }
                 // Aurora xuyong 2014-04-23 modified aurora's new feature end
                    if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && view != null) {
                    // Aurora xuyong 2014-05-05 modified for aurora's new feature start
                        Message msg = mHandler.obtainMessage();
                        msg.arg1 = OFFSET;
                        msg.sendToTarget();
                    // Aurora xuyong 2014-05-05 modified for aurora's new feature end
                    }*/
            }
            
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {
                // TODO Auto-generated method stub
                // Aurora yudingmin 2014-09-02 added for optimize start
                if (firstVisibleItem + visibleItemCount > totalItemCount - 2 && totalItemCount > 0) {
                    if(mInitedCount > INIT_ITEM_COUNT && mInitStatu == INIT_INIT_ITEM_COUNT_DONE && mInitedCount > mListAdapter.getCount() + POST_ITEM_COUNT){
                        mListAdapter.notifyCountSetChanged(mInitedCount);
                    } else {
                        mScrollToBottom = true;
                    }
                }
                // Aurora yudingmin 2014-09-02 added for optimize end
            }
        });
       // Aurora xuyong 2014-06-07 deleted for bug #5377 start
       // Aurora xuyong 2014-04-23 added for bug #4218 end
        // Aurora xuyong 2014-02-19 added for aurora's new feature start
        mListView.auroraSetUseNewSelectorLogical(false);
        // Aurora xuyong 2014-03-08 added for aurora's new feature start
        mListView.setDividerHeight(0);
        // Aurora xuyong 2014-03-08 added for aurora's new feature end
        // Aurora xuyong 2013-03-05 modified for aurora's new feature start
        mListView.auroraSetFrameNumbers(6);
        // Aurora xuyong 2013-03-05 modified for aurora's new feature end
        // Aurora xuyong 2014-02-19 added for aurora's new feature end
          // Aurora liugj 2014-02-07 modified for xuqiu:searchview scroll with listview start
        //mListView.setOnKeyListener(mThreadListKeyListener);

        //mEmptyView.setVisibility(View.GONE);
          // Aurora liugj 2014-02-07 modified for xuqiu:searchview scroll with listview end
        // Aurora xuyong 2014-11-26 modified for bug #10049 start
        mListAdapter = new ConversationListAdapter(AuroraConvListActivity.sContext, null);
        // Aurora xuyong 2014-11-26 modified for bug #10049 end
        // Aurora xuyong 2014-08-26 modified for bug modify start
        //mListAdapter.setOnContentChangedListener(mContentChangedListener);
        // Aurora xuyong 2014-08-26 modified for bug modify end

        // gionee zhouyj 2012-01-21 add for CR00765451 start
          // Aurora liugj 2013-09-24 modified for aurora's new feature start
        if (mActionBatchHandler != null && mActionBatchHandler.isInSelectionMode()) {
            mListAdapter.showCheckBox(true);
            mActionBatchHandler.bindToAdapter(mActionBatchHandler.getSelectionManger());
        }
          // Aurora liugj 2013-09-24 modified for aurora's new feature end
        // gionee zhouyj 2012-01-21 add for CR00765451 end 
        
        // Aurora liugj 2013-10-10 modified for aurora's new feature start       
        /*mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.e("liugj", "-=====onFocusChange=====-"+hasFocus);
                if (hasFocus && !mSearchMode) {
                    enterSearchMode();
                }
            }
        });
        mCancelSearchBtn.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                exitSearchMode();
            }
        });*/
        // Aurora liugj 2013-10-10 modified for aurora's new feature end
    }
     // Aurora liugj 2014-02-07 modified for xuqiu:searchview scroll with listview end

     // Aurora liugj 2014-01-07 added for bug:when delete visible onBackPressed activity finished start    
    public boolean hideDeleteBack() {
        if (mListView != null) {
            boolean deleteIsShow = mListView.auroraIsRubbishOut();
            if (deleteIsShow) {
                // Aurora xuyong 2014-02-19 modified for aurora's new feature start
                mListView.auroraSetRubbishBack();
                // Aurora xuyong 2014-02-19 modified for aurora's new feature end
                return true;
            }
        }
        return false;
    }
    // Aurora liugj 2014-01-07 added for bug:when delete visible onBackPressed activity finished end

    // Aurora liugj 2013-10-10 added for aurora's new feature start
    /**
     * 启动去搜索输入界面的动画
     */
    /*private void startGotoSearchAnimation() {
        // 避免重复启动
        if (mHasGotoSearchAnimationPlaying) {
            return;
        }
        
        mHasGotoSearchAnimationPlaying = true;
        // Aurora liugj 2013-09-30 deleted for fix bug-179 start
        mListView.auroraOnPause();
        // Aurora liugj 2013-09-30 deleted for fix bug-179 end
        // Aurora liugj 2013-10-11 modified for aurora's new feature start
        mListView.auroraSetNeedSlideDelete(false);
        // Aurora liugj 2013-10-11 modified for aurora's new feature end
        ((AuroraActivity)getActivity()).setMenuEnable(false);

        // 先截图
        Bitmap screenshot = captureSnapshot(mWidth, mHeight, false);

        // 动画相关view的holder
        AnimationHolder holder = getAnimationHolder();

        holder.root.setVisibility(View.VISIBLE);
        holder.root.setClickable(true);
        holder.root.invalidate();

        holder.imageView.setImageBitmap(screenshot);
        holder.imageView.setVisibility(View.VISIBLE);
         // Aurora liugj 2013-10-11 added for aurora's new feature start
        holder.bottomBar.setVisibility(View.GONE);
         // Aurora liugj 2013-10-11 added for aurora's new feature end
        
        holder.searchbox.setVisibility(View.VISIBLE);

        holder.button.setVisibility(View.VISIBLE);
//        holder.button.setText(R.string.search_cancel);
          // Aurora liugj 2013-10-24 modified for aurora's new feature start
        //holder.button.setEnabled(false);
          // Aurora liugj 2013-10-24 modified for aurora's new feature end

        holder.voice.setVisibility(View.VISIBLE);
        holder.voice.setEnabled(false);
        ((AuroraActivity)getActivity()).getAuroraActionBar().setVisibility(View.GONE);
        Handler handler = mAnimationHandler;
        
        handler.obtainMessage(MESSAGE_ANI_PREPARE_GOTO_SEARCH).sendToTarget();
    }*/
    
    /**
     * 获取AnimationHolder
     * @return AnimationHolder
     */
    /*private AnimationHolder getAnimationHolder() {
        if (mAnimationHolder == null) {
            AnimationHolder holder = new AnimationHolder();

            ViewStub stub = (ViewStub) fragmentView.findViewById(R.id.home_inputbox);
            holder.root = stub.inflate();

            holder.imageView = (ImageView) holder.root.findViewById(R.id.home_aim_screenshot);
              // Aurora liugj 2013-10-11 added for aurora's new feature start
            holder.bottomBar = (ImageView) holder.root.findViewById(R.id.search_bottombar);
              // Aurora liugj 2013-10-11 added for aurora's new feature end
            holder.searchbox = (AuroraSearchView) holder.root.findViewById(R.id.mms_searchview);
            holder.searchbox.setQueryHint(getActivity().getString(R.string.gn_search_hint));
            holder.button = (Button) holder.root.findViewById(R.id.search_cancel_btn);
//            holder.voice = holder.searchbox.findViewById(R.id.float_voice_search);
            holder.durationFade = 150; // SUPPRESS CHECKSTYLE
            holder.durationMove = 150; // SUPPRESS CHECKSTYLE
            
            mAnimationHolder = holder;
            // 同时初始化handler
            mAnimationHandler = new Handler(this);
        }

        return mAnimationHolder;
    }*/
    
    /**
     * 动画holder
     * 
     * @author qiaopu
     * @since 2013-1-16
     */
//    class AnimationHolder {
//        /** 动画根view */
//        View root;
//        /** 取消按钮 */
//        Button button;
//        /** 截图view */
//        ImageView imageView;
//          // Aurora liugj 2013-10-11 added for aurora's new feature start
//        /** 渐变条 **/ 
//        ImageView bottomBar;
//          // Aurora liugj 2013-10-11 added for aurora's new feature end
//        /** 话筒 */
//        /*View voice;*/
//        /** 框view */
//        AuroraSearchView searchbox;
//        /** 移动时间 */
//        int durationMove;
//        /** 淡出时间 */
//        int durationFade;
//        /** 框偏移Y值 */
//        int boxOffsetY;
//        /** 按钮偏移X值 */
//        int buttonOffsetWidth;
//    }
    
    /**
     * 窗口截图
     * 
     * @param width
     *            截图宽度
     * @param height
     *            截图高度
     * @param useHomeCache
     *            是否在截图时缓存，仅在多窗口时为true
     * @return 窗口截图
     */
    /*public Bitmap captureSnapshot(int width, int height, boolean useHomeCache) {
        // return mHomeScrollView.captureSnapshot(width, height, useHomeCache);
        if (useHomeCache && mSnapshot != null && !mSnapshot.isRecycled()) {
            return mSnapshot;
        }
        if (width > 0 && height > 0) {
            try {
                long start = 0;
                Bitmap capture = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                capture.eraseColor(Color.WHITE);
                Canvas c = new Canvas(capture);
                final int left = mListLayout.getScrollX();
                final int top = mListLayout.getScrollY();
                int state = c.save();
                float scale = capture.getWidth() / (float) width;
                c.scale(scale, scale);
                c.clipRect(0, 0, width, (int) (height / scale)); // 弹出输入法键盘时，高度会变小，故使用宽度反推
                c.translate(-left, -top);
                mListLayout.draw(c);
                c.restoreToCount(state);

                if (useHomeCache) {
                    mSnapshot = capture;
                }
                return capture;
            } catch (Exception e) {
                // 因为在绘图过程中，可能产生空指针，也可能出现内存不足，故在此统一处理。
                return null;
            }
        } else {
            return null;
        }
    }*/
    
    /*@Override
    public boolean handleMessage(Message msg) {
        final AnimationHolder holder = mAnimationHolder;
        int homeOffsetY = 0;
        switch (msg.what) {
        case MESSAGE_ANI_PREPARE_GOTO_SEARCH:

            holder.buttonOffsetWidth = holder.button.getMeasuredWidth();
            holder.boxOffsetY = -homeOffsetY + fragmentView.findViewById(R.id.conversation_header_searchbox).getTop()
                    - holder.root.findViewById(R.id.mms_searchview).getTop();

            mAnimationHandler.obtainMessage(MESSAGE_ANI_START_GOTO_SEARCH).sendToTarget();

            // holder.durationFade = 300;
            // holder.durationMove = 200;
            // holder.durationShowButton = 200;
            return true;

        case MESSAGE_ANI_PREPARE_BACK_FROM_SEARCH:
            // 先截图
            holder.root.setVisibility(View.INVISIBLE);
            Bitmap screenshot = captureSnapshot(mWidth, mHeight, false);

            holder.root.setVisibility(View.VISIBLE);
            //holder.root.setClickable(true);
            holder.root.invalidate();

            holder.imageView.setImageBitmap(screenshot);
            holder.imageView.setVisibility(View.INVISIBLE);
              // Aurora liugj 2013-10-11 added for aurora's new feature start
            holder.bottomBar.setVisibility(View.GONE);
              // Aurora liugj 2013-10-11 added for aurora's new feature end

            // holder.durationFade = 1000;
            // holder.durationMove = 1000;
            // holder.durationShowButton = 1000;

            holder.buttonOffsetWidth = holder.button.getMeasuredWidth();
            holder.boxOffsetY = -homeOffsetY + fragmentView.findViewById(R.id.conversation_header_searchbox).getTop()
                    - holder.root.findViewById(R.id.mms_searchview).getTop();

            mAnimationHandler.obtainMessage(MESSAGE_ANI_START_BACK_FROM_SEARCH).sendToTarget();

            return true;

        case MESSAGE_ANI_START_GOTO_SEARCH:
            // 淡出的动画
            AlphaAnimation fadeOutAni = new AlphaAnimation(1.0f, 0.0f);
            fadeOutAni.setDuration(holder.durationFade);
            fadeOutAni.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                    holder.imageView.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

            });

            // 上移动画
            TranslateAnimation moveUpAni = new TranslateAnimation(0, 0, holder.boxOffsetY, 0);
            moveUpAni.setStartOffset(holder.durationFade);
            moveUpAni.setDuration(holder.durationMove);
            moveUpAni.setInterpolator(new DecelerateInterpolator());

            // 先不动再上移动画
            AnimationSet stayThanMoveUpAni = new AnimationSet(false);

            stayThanMoveUpAni.addAnimation(moveUpAni);
            stayThanMoveUpAni.setFillAfter(true);

            // 左移动画
            TranslateAnimation moveLeftAni = new TranslateAnimation(holder.buttonOffsetWidth, 0, 0, 0);
            moveLeftAni.setInterpolator(new DecelerateInterpolator());
            moveLeftAni.setStartOffset(holder.durationFade);
            moveLeftAni.setDuration(holder.durationMove);
            moveLeftAni.setFillAfter(true);

            // 同时启动所有动画
            holder.imageView.startAnimation(fadeOutAni);
            holder.searchbox.startAnimation(stayThanMoveUpAni);
//            holder.voice.startAnimation(moveLeftAni);
            holder.button.startAnimation(moveLeftAni);

            Message msgGotoSearch = mAnimationHandler.obtainMessage(MESSAGE_ANI_GOTO_SEARCH);
            mAnimationHandler.sendMessageDelayed(msgGotoSearch, holder.durationFade + holder.durationMove);

            return true;

        case MESSAGE_ANI_START_BACK_FROM_SEARCH:
            // 淡入的动画
            AlphaAnimation fadeInAni = new AlphaAnimation(0.0f, 1.0f);
            fadeInAni.setStartOffset(holder.durationMove);
            fadeInAni.setDuration(holder.durationFade);
            fadeInAni.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    holder.imageView.setVisibility(View.VISIBLE);
                }

            });

            // 下移动画
            TranslateAnimation moveDownAni = new TranslateAnimation(0, 0, 0, holder.boxOffsetY);
            moveDownAni.setDuration(holder.durationMove);
            moveDownAni.setInterpolator(new AccelerateInterpolator());

            // 先下移再不动动画
            AnimationSet moveDownThanStayAni = new AnimationSet(false);

            moveDownThanStayAni.addAnimation(moveDownAni);
            moveDownThanStayAni.setFillAfter(true);
            // 右移动画
            TranslateAnimation moveRightAni = new TranslateAnimation(0, holder.buttonOffsetWidth, 0, 0);
            moveRightAni.setInterpolator(new AccelerateInterpolator());
            moveRightAni.setDuration(holder.durationMove);
            moveRightAni.setFillAfter(true);

            // 同时启动所有动画
            holder.imageView.startAnimation(fadeInAni);
            holder.searchbox.startAnimation(moveDownThanStayAni);
//            holder.voice.startAnimation(moveRightAni);
            holder.button.startAnimation(moveRightAni);

            Message msgBackFromSearch = mAnimationHandler.obtainMessage(MESSAGE_ANI_BACK_FROM_SEARCH);
            mAnimationHandler.sendMessageDelayed(msgBackFromSearch, holder.durationFade + holder.durationMove);

            return true;

        case MESSAGE_ANI_GOTO_SEARCH:
            mHasGotoSearchAnimationPlaying = false;

            holder.imageView.clearAnimation();
            holder.searchbox.clearAnimation();
//            holder.voice.clearAnimation();
            holder.button.clearAnimation();
            // 动画结束后立刻清空截图
            holder.imageView.setImageDrawable(null);
            holder.imageView.getLayoutParams().height = mHeight;

            enterSearchMode();
            
            return true;

        case MESSAGE_ANI_BACK_FROM_SEARCH:
            mHasBackFromSearchAnimationPlaying = false;
            holder.imageView.clearAnimation();
            holder.searchbox.clearAnimation();
//            holder.voice.clearAnimation();
            holder.button.clearAnimation();

            holder.root.setVisibility(View.GONE);

            // 动画结束后立刻清空截图
            holder.imageView.setImageDrawable(null);
            holder.imageView.getLayoutParams().height = mHeight;
            exitSearchMode();
            return true;

        default:
            return false;
        }
    }*/
    
    /**
     * 启动从搜索输入界面返回动画
     */
    /*public void startBackFromSearchAnimation() {
        // 避免重复启动
        if (mHasBackFromSearchAnimationPlaying) {
            return;
        }
        
        // 动画相关view的holder
        AnimationHolder holder = getAnimationHolder();

        mHasBackFromSearchAnimationPlaying = true;

        holder.root.setVisibility(View.VISIBLE);
        holder.root.setClickable(false);

        Handler handler = mAnimationHandler;

        // 因为有键盘收起动画和Activity切换动画，延后启动
        int delay = 50; // SUPPRESS CHECKSTYLE
        handler.sendMessageDelayed(handler.obtainMessage(MESSAGE_ANI_PREPARE_BACK_FROM_SEARCH), delay);
    }*/
    // Aurora liugj 2013-10-10 added for aurora's new feature end
   // Aurora xuyong 2014-08-25 added for bug #7919 start
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mEncryptionTitle == v || mListView == v) {
                InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);     
                if (imm.isActive()) {     
                    imm.hideSoftInputFromWindow(v.getApplicationWindowToken() , 0);   
                }
            }
        }
        return false;
    }
    // Aurora xuyong 2014-08-25 added for bug #7919 end
    @Override
    public boolean onClose() {
        Log.e("liugj", "-=====onClose=====-");
        // Aurora liugj 2013-10-10 modified for aurora's new feature start
        //final AnimationHolder holder = mAnimationHolder;
        // Aurora xuyong 2014-11-26 modified for bug #10049 start
        AuroraActivity activity = AuroraConvListActivity.sAuroraConvListActivity;
        if (activity != null) {
            AuroraSearchView searchbox = (AuroraSearchView) activity.getSearchView();
            // Aurora xuyong 2014-11-26 modified for bug #10049 end
            searchbox.onActionViewExpanded();
            searchbox.setQuery(null, false);
            // Aurora liugj 2013-10-10 modified for aurora's new feature end
        }
        return false;
    }
    // Aurora xuyong 2014-03-12 added for bug #3064 start
    HashMap<String, Cursor> searchResultsCache = new HashMap<String, Cursor>();
    // Aurora xuyong 2014-03-12 added for bug #3064 end
    // Aurora liugj 2013-09-20 modified for aurora's new feature start
     // Aurora liugj 2014-02-07 modified for xuqiu:searchview scroll with listview start
    // Aurora xuyong 2014-03-17 added for bug #3064 start
    QueryAsyncTask mQueryAsyncTask;
    
    private class QueryAsyncTask extends AsyncTask<String, Void, Cursor> {
        
        private String mTag;
        private Context mQContext;
       // Aurora xuyong 2014-06-09 added for aurora's new feature start
        private MatrixCursor mQueryResult;
        // Aurora xuyong 2014-06-09 added for aurora's new feature end
        public QueryAsyncTask(Context context, String tag) {
            mQContext = context;
            mTag = tag;
        }
        
        public String getTag() {
            return mTag;
        }
        
        @Override
        protected Cursor doInBackground(String... params) {
            // TODO Auto-generated method stub
            // Aurora xuyong 2014-02-13 modified for bug #11290 start
            Uri uri = Uri.parse("content://mms-sms/search");
            //String sqlQuery = "%" + params[0] + "%";
            // Aurora xuyong 2014-04-10 modified for bug #4061 start
            // Aurora xuyong 2014-10-29 modified for reject & privacy feature start
            //String selection = "(sms.body LIKE ? escape '/' OR sms.address LIKE ? escape '/' OR sms.address " + searchContacts(params[0]) + ") AND is_privacy = 0 AND reject = 0";
            // Aurora xuyong 2014-10-29 modified for reject & privacy feature end
            // Aurora xuyong 2014-04-10 modified for bug #4061 end
            //String[] selectionArgs = new String[] { sqlQuery, sqlQuery };
            return mQContext.getContentResolver().query(uri.buildUpon().appendQueryParameter("pattern", params[0]).build(), null, null, null, null);
            // Aurora xuyong 2014-02-13 modified for bug #11290 end
        }
        // Aurora xuyong 2014-06-09 added for aurora's new feature start
        private void getCopyOfResult(Cursor result) {
            if (result == null || result.getCount() <= 0) {
                return;
            }
            mQueryResult = new MatrixCursor(PROJECTION_SMS, result.getCount());
            int columnCount = result.getColumnCount();
            Object[] item = new Object[columnCount];
            while (result.moveToNext() && !result.isBeforeFirst() && !result.isAfterLast()) {
                for (int i = 0; i < columnCount; i++) {
                    item[i] = result.getString(i);
                }
                mQueryResult.addRow(item);
            }
        }
        // Aurora xuyong 2014-06-09 added for aurora's new feature end
        @Override
        protected void onPostExecute(Cursor result) {
            if (mSearchMode) {
                // Aurora xuyong 2014-10-31 added for bug #9396 start
                if (result == null) {
                    mSearchLayout.setVisibility(View.GONE);
                    mListView.setVisibility(View.GONE);
                    mEncryptionTitle.setText(R.string.search_no_result);
                    mEncryptionTitle.setVisibility(View.VISIBLE);
                    return;
                }
                // Aurora xuyong 2014-10-31 added for bug #9396 end
             // Aurora xuyong 2014-06-09 modified for aurora's new feature start
                getCopyOfResult(result);
                if (searchResultsCache != null && (!searchResultsCache.containsKey(mQueryString))) {
                    searchResultsCache.put(mQueryString, mQueryResult);
             // Aurora xuyong 2014-06-09 modified for aurora's new feature end
                }
                // Aurora xuyong 2014-10-31 deleted for bug #9396 start
                //if (result == null) {
                //    return;
                //}
                // Aurora xuyong 2014-10-31 deleted for bug #9396 end
                int cursorCount = result.getCount();
                if (cursorCount == 0) {
                    mSearchLayout.setVisibility(View.GONE);
                    mListView.setVisibility(View.GONE);
                    mEncryptionTitle.setText(R.string.search_no_result);
                    mEncryptionTitle.setVisibility(View.VISIBLE);
                    // Aurora xuyong 2014-10-31 added for cursor leak start
                    if (!result.isClosed()) {
                        result.close();
                    }
                    // Aurora xuyong 2014-10-31 added for cursor leak end
                }else {
                    // Aurora xuyong 2015-06-30 modified for bug #13931 start
                	if (mListLayout != null) {
                		mListLayout.setVisibility(View.GONE);
                		mListView.removeHeaderView(mListLayout);
                	}
                    // Aurora xuyong 2015-06-30 modified for bug #13931 end
                    mSearchLayout.setVisibility(View.VISIBLE);
                    mEncryptionTitle.setVisibility(View.GONE);
                    if (mAdapter == null) {
                        mAdapter = new MessageSearchListAdapter(AuroraConvListActivity.sContext, result);
                    } else {
                        mAdapter.changeCursor(result);
                    }
                    mAdapter.setQueryString(mQueryString);
                    mAdapter.setSearchMode(true);
                // Aurora xuyong 2014-08-26 modified for bug modify start
                    //mAdapter.setOnContentChangedListener(mSearchContentChangedListener);
                // Aurora xuyong 2014-08-26 modified for bug modify end
                    mListView.setAdapter(mAdapter);
                    mListView.setRecyclerListener(mAdapter);
                 // Aurora xuyong 2014-04-17 deleted for bug #4318 start
                    //mScrollListener = new MyScrollListener(getActivity(), CHANGE_SCROLL_LISTENER_MIN_CURSOR_COUNT, "Search_Scroll_Tread");
                   //mListView.setOnScrollListener(mScrollListener);
                 // Aurora xuyong 2014-04-17 deleted for bug #4318 end
                    mListView.setVisibility(View.VISIBLE);
                    SearchRecentSuggestions recent = ((MmsApp) AuroraConvListActivity.sApp).getRecentSuggestions();
                    if (recent != null) {
                        recent.saveRecentQuery(
                                mQueryString,
                                getString(R.string.search_history,
                                        cursorCount, mQueryString));
                    }
                   // Aurora xuyong 2014-04-10 added for bug #4061 start
                    if (mQueryString != null) {
                        if (mQueryString.contains("/%")) {
                            mQueryString = mQueryString.replaceAll("/%", "%");
                        }
                        if (mQueryString.contains("//")) {
                            mQueryString = mQueryString.replaceAll("//", "/");
                        }
                        mActivityQueryString = mQueryString;
                    }
                   // Aurora xuyong 2014-04-10 modified for bug #4061 end
                }
            }
        }
        
    }
    // Aurora xuyong 2014-04-10 added for bug #4061 start
    private String mActivityQueryString;
    // Aurora xuyong 2014-04-10 added for bug #4061 end
    // Aurora xuyong 2014-03-17 added for bug #3064 end
    @Override
    public boolean onQueryTextChange(final String queryString) {
        // Aurora liugj 2013-10-10 modified for aurora's new feature start
        //final AnimationHolder holder = mAnimationHolder;
        // Aurora liugj 2013-11-15 modified for bug-760 start
      // Aurora xuyong 2014-04-10 modified for bug #4061 start
        String changedQueryString = queryString;
        if (queryString != null) {
            if (queryString.contains("/")) {
                changedQueryString = changedQueryString.replaceAll("/", "//"); 
            }
            if (queryString.contains("%")) {
                changedQueryString = changedQueryString.replaceAll("%", "/%");
            }
        }
        if (TextUtils.isEmpty(changedQueryString)) {
      // Aurora xuyong 2014-04-10 modified for bug #4061 end
        // Aurora liugj 2013-11-15 modified for bug-760 end
            //holder.imageView.setVisibility(View.VISIBLE);
            // Aurora liugj 2013-10-11 added for aurora's new feature start
            //holder.bottomBar.setVisibility(View.GONE);
            // Aurora xuyong 2014-11-26 modified for bug #10049 start
            AuroraActivity activityE = AuroraConvListActivity.sAuroraConvListActivity;
            if (activityE == null) {
                return false;
            }
            activityE.getSearchViewGreyBackground().setVisibility(View.VISIBLE);
            // Aurora xuyong 2014-11-26 modified for bug #10049 end
            // Aurora liugj 2013-10-11 added for aurora's new feature end
            // Aurora liugj 2013-10-08 modified for aurora's new feature start
            mEncryptionTitle.setVisibility(View.GONE);
            mSearchLayout.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            // Aurora liugj 2013-10-08 modified for aurora's new feature end
            if (mListView.getHeaderViewsCount() == 0) {                
                mListView.setAdapter(null);
                // Aurora xuyong 2015-06-30 added for bug #13931 start
                if (mListLayout != null) {
                    mListLayout.setVisibility(View.VISIBLE);
                }
                // Aurora xuyong 2015-06-30 added for bug #13931 end
                mListView.addHeaderView(mListLayout);
                mListView.setAdapter(mListAdapter);
                mListView.setRecyclerListener(mListAdapter);
                // Aurora liugj 2013-11-04 added for aurora's new feature start
             // Aurora xuyong 2014-04-17 deleted for bug #4318 start
                //mScrollListener = new MyScrollListener(CHANGE_SCROLL_LISTENER_MIN_CURSOR_COUNT, "ConversationList_Scroll_Tread");
               //mListView.setOnScrollListener(mScrollListener);
             // Aurora xuyong 2014-04-17 deleted for bug #4318 end
                // Aurora liugj 2013-11-04 added for aurora's new feature end
            }
            // Aurora liugj 2013-10-15 added for aurora's new feature start
            mQueryString = null;
            // Aurora liugj 2013-10-15 added for aurora's new feature end
        }else {
            //holder.imageView.setVisibility(View.GONE);
            // Aurora liugj 2013-10-11 added for aurora's new feature start
            //holder.bottomBar.setVisibility(View.VISIBLE);
            // Aurora xuyong 2014-11-26 modified for bug #10049 start Activity activity = AuroraConvListActivity.sAuroraConvListActivity;
            AuroraActivity activityNE = AuroraConvListActivity.sAuroraConvListActivity;
            if (activityNE == null) {
                return false;
            }
            activityNE.getSearchViewGreyBackground().setVisibility(View.GONE);
            // Aurora xuyong 2014-11-26 modified for bug #10049 end
            // Aurora liugj 2013-10-11 added for aurora's new feature end
            // Aurora liugj 2013-10-10 modified for aurora's new feature end
            // Aurora liugj 2013-11-15 modified for bug-760 start
          // Aurora xuyong 2014-04-10 modified for bug #4061 start
            mQueryString = changedQueryString;
          // Aurora xuyong 2014-04-10 modified for bug #4061 end
            // Aurora liugj 2013-11-15 modified for bug-760 end
            // Aurora xuyong 2014-03-12 added for bug #3064 start
            Cursor cacheCursor = searchResultsCache.get(mQueryString);
            if (searchResultsCache != null && cacheCursor != null && !cacheCursor.isClosed()) {
                if (cacheCursor.getCount() > 0) {
                    // Aurora xuyong 2015-06-30 modified for bug #13931 start
                	if (mListLayout != null) {
                		mListLayout.setVisibility(View.GONE);
                		mListView.removeHeaderView(mListLayout);
                	}
                    // Aurora xuyong 2015-06-30 modified for bug #13931 end
                    mSearchLayout.setVisibility(View.VISIBLE);
                    mEncryptionTitle.setVisibility(View.GONE);
                    if (mAdapter == null) {
                        mAdapter = new MessageSearchListAdapter(AuroraConvListActivity.sContext, cacheCursor);
                    } else {
                        mAdapter.changeCursor(cacheCursor);
                        mAdapter.setQueryString(mQueryString);
                        mAdapter.setSearchMode(true);
                   // Aurora xuyong 2014-08-26 modified for bug modify start
                        //mAdapter.setOnContentChangedListener(mSearchContentChangedListener);
                   // Aurora xuyong 2014-08-26 modified for bug modify end
                    }
                    mListView.setAdapter(mAdapter);
                    mListView.setRecyclerListener(mAdapter);
                 // Aurora xuyong 2014-04-17 deleted for bug #4318 start
                    //mScrollListener = new MyScrollListener(getActivity(), CHANGE_SCROLL_LISTENER_MIN_CURSOR_COUNT, "Search_Scroll_Tread");
                    //mListView.setOnScrollListener(mScrollListener);
                 // Aurora xuyong 2014-04-17 deleted for bug #4318 end
                    mListView.setVisibility(View.VISIBLE);
                } else {
                    mSearchLayout.setVisibility(View.GONE);
                    mListView.setVisibility(View.GONE);
                    mEncryptionTitle.setText(R.string.search_no_result);
                    mEncryptionTitle.setVisibility(View.VISIBLE);
                }
                return true;
            }
            // Aurora xuyong 2014-03-12 added for bug #3064 end
            // Aurora xuyong 2014-03-17 deleted for bug #3064 start
            /*if (mSearchHandler == null) {
                ContentResolver cr = AuroraConvListActivity.sContext.getContentResolver();
                mSearchHandler = new AsyncQueryHandler(cr) {

                    @Override
                    protected void onQueryComplete(int token, Object cookie,
                            Cursor cursor) {
                        // Aurora xuyong 2014-03-12 added for bug #3064 start
                        final Cursor cursorProxy = cursor;
                        if (searchResultsCache != null && !searchResultsCache.containsKey(mQueryString)) {
                            searchResultsCache.put(mQueryString, cursorProxy);
                        }
                        // Aurora xuyong 2014-03-12 added for bug #3064 end
                        if (cursor == null) {
                            return;
                        }
                        int cursorCount = cursor.getCount();
                        // Aurora liugj 2013-10-08 modified for aurora's new feature start
                        Log.e("liugj", cursorCount+"---------onQueryComplete--------"+mQueryString);
                        if (cursorCount == 0) {
                            mSearchLayout.setVisibility(View.GONE);
                            mListView.setVisibility(View.GONE);
                            mEncryptionTitle.setText(R.string.search_no_result);    //android.content.res.Resources$NotFoundException: String resource ID
                            mEncryptionTitle.setVisibility(View.VISIBLE);
                        }else {
                            mListView.removeHeaderView(mListLayout);
                            mSearchLayout.setVisibility(View.VISIBLE);
                            mEncryptionTitle.setVisibility(View.GONE);
                            // Aurora liugj 2013-09-29 modified for aurora's new feature start
                            mAdapter = new MessageSearchListAdapter(AuroraConvListActivity.sContext, cursor);
                            // Aurora liugj 2013-09-29 modified for aurora's new feature end
                            mAdapter.setQueryString(mQueryString);
                            mAdapter.setSearchMode(true);
                            mAdapter.setOnContentChangedListener(mSearchContentChangedListener);
                            mListView.setAdapter(mAdapter);
                            mListView.setRecyclerListener(mAdapter);
                            // Aurora liugj 2013-11-04 added for aurora's new feature start
                            mScrollListener = new MyScrollListener(getActivity(), CHANGE_SCROLL_LISTENER_MIN_CURSOR_COUNT, "Search_Scroll_Tread");
                            mListView.setOnScrollListener(mScrollListener);
                            // Aurora liugj 2013-11-04 added for aurora's new feature end
                            mListView.setVisibility(View.VISIBLE);
                            
                            // Remember the query if there are actual results
                            SearchRecentSuggestions recent = ((MmsApp) AuroraConvListActivity.sApp).getRecentSuggestions();
                            if (recent != null) {
                                recent.saveRecentQuery(
                                        mQueryString,
                                        getString(R.string.search_history,
                                                cursorCount, mQueryString));
                            }
                        }
                        // Aurora liugj 2013-10-08 modified for aurora's new feature end
                    }
                    
                };
            //}
            // Aurora liugj 2013-10-08 modified for aurora's new feature start
            // don't pass a projection since the search uri ignores it by MmsSmsProvider.URI_SEARCH_SUGGEST
            /*Uri uri = Uri.parse("content://mms-sms/search").buildUpon()
                        .appendQueryParameter("pattern", queryString).build();
            
            // kick off a query for the threads which match the search string
            mSearchHandler.startQuery(0, null, uri, null, null, null, null);*/
            //Uri uri = Uri.parse("content://sms");
            //String sqlQuery = "%"+mQueryString+"%";
            // Aurora liugj 2013-11-29 modified for sms search for name start
            //mSearchHandler.startQuery(0, null, uri, PROJECTION_SMS, /*"charindex(rtrim(?),sms.body) > 0"*/ "sms.body LIKE ? OR sms.address LIKE ? OR sms.address " + searchContacts(queryString), new String[] { sqlQuery, sqlQuery }, DEFAULT_SORT_ORDER);
            // Aurora liugj 2013-11-29 modified for sms search for name end
            // Aurora liugj 2013-10-08 modified for aurora's new feature end
            // Aurora xuyong 2014-03-17 deleted for bug #3064 end
            // Aurora xuyong 2014-03-17 added for bug #3064 start
            if (mQueryAsyncTask == null) {
                mQueryAsyncTask = new QueryAsyncTask(AuroraConvListActivity.sContext, mQueryString);
                mQueryAsyncTask.execute(mQueryString);
            } else {
                if (mQueryAsyncTask.getTag().equals(mQueryString) && mQueryAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
                    return true;
                } else {
                    mQueryAsyncTask.cancel(true);
                    mQueryAsyncTask = new QueryAsyncTask(AuroraConvListActivity.sContext, mQueryString);
                    mQueryAsyncTask.execute(mQueryString);
                }
                
            }
            // Aurora xuyong 2014-03-17 added for bug #3064 end
        }
        
        return true;
    }
     // Aurora liugj 2014-02-07 modified for xuqiu:searchview scroll with listview end
    // Aurora liugj 2013-09-20 modified for aurora's new feature end
    
    // Aurora liugj 2013-09-20 modified for aurora's new feature start
    @Override
    public boolean onQueryTextSubmit(String query) {
        
        return true;
    }
    // Aurora liugj 2013-09-20 modified for aurora's new feature end
    
    @Override
    public boolean quit(){
        // Aurora xuyong 2014-03-17 added for bug #3064 start
        if (mListView != null) {
            // Aurora xuyong 2015-06-30 modified for bug #13931 start
        	if (mListLayout != null) {
        		mListLayout.setVisibility(View.GONE);
        		mListView.removeHeaderView(mListLayout);
        	}
            // Aurora xuyong 2015-06-30 modified for bug #13931 end
        }
        if (mSearchLayout != null) {
            mSearchLayout.setVisibility(View.GONE);
        }
        // Aurora xuyong 2014-03-17 added for bug #3064 end
        onClose();
        // Aurora xuyong 2014-03-17 added for bug #3064 start
        mSearchMode = false;
        // Aurora xuyong 2014-03-17 added for bug #3064 end
        mEncryptionTitle.setVisibility(View.GONE);
        // Aurora xuyong 2014-03-17 added for bug #3064 start
        if (mQueryAsyncTask != null && mQueryAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
            mQueryAsyncTask.cancel(true);
        }
        // Aurora xuyong 2014-03-17 added for bug #3064 end
        // Aurora xuyong 2014-03-12 added for bug #3064 start
        if (searchResultsCache != null) {
            Iterator it = searchResultsCache.entrySet().iterator();;
            while (it.hasNext()) {
                Map.Entry<String, Cursor> entry = (Map.Entry<String, Cursor>)it.next();
                Cursor cursor = entry.getValue();
             // Aurora xuyong 2014-06-09 modified for aurora's new feature start
                if (cursor != null && !cursor.isClosed()) {
             // Aurora xuyong 2014-06-09 modified for aurora's new feature end
                    cursor.close();
                }
            }
            searchResultsCache.clear();
        }
        // Aurora xuyong 2014-03-12 added for bug #3064 end
        //hideInputMethod();
        if (mAdapter != null) {
            mAdapter.destroy();
             mAdapter = null;
            // Aurora xuyong 2014-03-17 deleted for bug #3064 start
             //mSearchHandler = null;
            // Aurora xuyong 2014-03-17 deleted for bug #3064 end
        }
        mQueryString = null;
        setListViewWatcher(null);
        // Aurora xuyong 2014-03-17 deleted for bug #3064 start
        //mSearchMode = false;
        // Aurora xuyong 2014-03-17 deleted for bug #3064 end
        // Aurora xuyong 2014-03-17 modified for bug #3064 start
        if (mListView != null) {
            if (mListView.getHeaderViewsCount() == 0) {                
                mListView.setAdapter(null);
                // Aurora xuyong 2015-06-30 modified for bug #13931 start
                if (mListLayout != null) {
                    mListLayout.setVisibility(View.VISIBLE);
                }
                // Aurora xuyong 2015-06-30 modified for bug #13931 end
                mListView.addHeaderView(mListLayout);
                mListView.setAdapter(mListAdapter);
                mListView.setRecyclerListener(mListAdapter);
             // Aurora xuyong 2014-04-17 deleted for bug #4318 start
                //mScrollListener = new MyScrollListener(CHANGE_SCROLL_LISTENER_MIN_CURSOR_COUNT, "ConversationList_Scroll_Tread");
                //mListView.setOnScrollListener(mScrollListener);
             // Aurora xuyong 2014-04-17 deleted for bug #4318 end
            }
            mListView.setVisibility(View.VISIBLE);
            mListView.auroraSetNeedSlideDelete(true);
        }
        // Aurora xuyong 2014-03-17 modified for bug #3064 end
        // Aurora xuyong 2014-11-26 modified for bug #10049 start
        AuroraActivity activity = AuroraConvListActivity.sAuroraConvListActivity;
        if (activity != null) {
            activity.setMenuEnable(true);
        }
        // Aurora xuyong 2014-11-26 modified for bug #10049 end
        return true;
    }
    
     // Aurora liugj 2013-11-29 added for sms search for name start
    private String searchContacts(String pattern) {
        StringBuffer in = new StringBuffer(" IN (");
        Builder builder = Phone.CONTENT_FILTER_URI.buildUpon();
        builder.appendPath(pattern);      // Builder will encode the query
        Cursor cursor = AuroraConvListActivity.sContext.getContentResolver().query(builder.build(), 
                new String[] {Phone.NUMBER}, null, null, null);
          // Aurora liugj 2014-02-08 modified for bug-2219 start
        if (cursor != null ) {
            try {
                while (cursor.moveToNext() && !cursor.isBeforeFirst() && !cursor.isAfterLast()) {
                    String number = Contact.getValidNumber(cursor.getString(0));
                    if (number.startsWith("+86")) {
                        in.append(" '").append(number).append("' ,");
                    // if any contact's number contains some uncommon symbol such as #,
                    // sometimes this condition would cause crash.
                    // Aurora xuyong 2014-06-04 modified for upper condition start
                        in.append(" '").append(number.substring(3));
                        if (!cursor.isLast()) {
                            in.append("' ,");
                        } else {
                            in.append("' ");
                        }
                    }else {
                        in.append(" '+86").append(number).append("' ,");
                        in.append(" '").append(number);
                        if (!cursor.isLast()) {
                            in.append("' ,");
                        } else {
                            in.append("' ");
                        }
                    // Aurora xuyong 2014-06-04 modified for upper condition end
                    }
                }
            } finally {
                cursor.close();
            }
        }
        // Aurora liugj 2014-02-08 modified for bug-2219 end
        in.append(" )");
        //Log.d("liugj", "searchContacts in = " + in.toString());
        return in.toString();
    }
    // Aurora liugj 2013-11-29 added for sms search for name end
    
    
    // Aurora liugj 2013-09-20 added for aurora's new feature start
    private final MessageSearchListAdapter.OnContentChangedListener mSearchContentChangedListener =
            new MessageSearchListAdapter.OnContentChangedListener() {
            public void onContentChanged(MessageSearchListAdapter adapter) {
                //gionee gaoj 2012-9-20 added for CR00699291 start
                 if (mIsonResume) {
                    mNeedToMarkAsSeen = true;
                    mQueryHandler.removeCallbacks(mQueryRunnable);
                    mQueryHandler.postDelayed(mQueryRunnable, 500);
                }
                // gionee zhouyj 2013-02-05 add for CR00771239 start 
                if (mListViewWatcher != null) {
                    mListViewWatcher.listViewChanged(true);
                }
                // gionee zhouyj 2013-02-05 add for CR00771239 end 
                //gionee gaoj 2012-9-20 added for CR00699291 end
            }
     };
     // Aurora liugj 2013-09-20 added for aurora's new feature start
    
    private void gotoSearchMode() {
        mSearchMode = true;
       // Aurora xuyong 2014-05-05 added for aurora's new feature start
        Contact.removeAllConvListeItemListener();
       // Aurora xuyong 2014-05-05 added for aurora's new feature end
        hideDeleteBack();
       // Aurora xuyong 2014-04-21 modified for bug #4460 start
        if (mListView != null) {
            mListView.auroraSetNeedSlideDelete(false);
        }
       // Aurora xuyong 2014-04-21 modified for bug #4460 end
        // Aurora xuyong 2014-11-26 modified for bug #10049 start
        AuroraActivity activity = AuroraConvListActivity.sAuroraConvListActivity;
        if (activity == null) {
            return;
        }
        activity.setMenuEnable(false);
        AuroraSearchView searchbox = (AuroraSearchView) activity.getSearchView();
        // Aurora xuyong 2014-11-26 modified for bug #10049 end
        searchbox.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        // Aurora liugj 2014-01-06 modified for hide queryHint start
        //searchbox.setQueryHint(getResources().getString(R.string.gn_search_hint));
        // Aurora liugj 2014-01-06 modified for hide queryHint end
        searchbox.onActionViewExpanded();
        searchbox.setOnQueryTextListener(this);
        searchbox.setOnCloseListener(this);
        // Aurora xuyong 2014-11-26 modified for bug #10049 start
        activity.setOnSearchViewQuitListener(this);
        // Aurora xuyong 2014-11-26 modified for bug #10049 end
        /*((AuroraActivity)getActivity()).setOnSearchViewQuitListener(new AuroraActivity.OnSearchViewQuitListener() {
            
            @Override
            public boolean quit(){
                onClose();
                mEncryptionTitle.setVisibility(View.GONE);
                //hideInputMethod();
                if (mAdapter != null) {
                    mAdapter.destroy();
                     mAdapter = null;
                     mSearchHandler = null;
                }
                mQueryString = null;
                setListViewWatcher(null);
                mSearchMode = false;
                mListView.setVisibility(View.VISIBLE);
                mListView.auroraSetNeedSlideDelete(true);
                ((AuroraActivity)getActivity()).setMenuEnable(true);
                return true;
            }
        });*/
        
        setListViewWatcher(new ConvFragment.ListViewWatcher() {

            @Override
            public void listViewChanged(boolean isChange) {
                // TODO Auto-generated method stub
                if (isChange && mSearchMode) {
                    Log.e("liugj", "-----------listViewChanged----------");
                    // Aurora xuyong 2014-11-26 modified for bug #10049 start
                    AuroraActivity activity = AuroraConvListActivity.sAuroraConvListActivity;
                    if (activity != null) {
                        activity.hideSearchviewLayout();
                    }
                    // Aurora xuyong 2014-11-26 modified for bug #10049 end
                }
            }
        });
    }
     
    // Aurora liugj 2013-09-20 modified for aurora's new feature start    
    /*private void enterSearchMode() {
        // Aurora liugj 2013-10-10 modified for aurora's new feature start
        final AnimationHolder holder = mAnimationHolder;
        mSearchMode = true;
        // Aurora liugj 2013-10-10 modified for aurora's new feature start
        holder.searchbox.getFocus();
        // Aurora liugj 2013-10-10 modified for aurora's new feature end
        holder.searchbox.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        holder.searchbox.onActionViewExpanded();
        holder.searchbox.setOnQueryTextListener(this);
        //holder.searchbox.setOnCloseListener(this);
        holder.button.setEnabled(true);
        holder.button.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                startBackFromSearchAnimation();
            }
        });
        // Aurora liugj 2013-10-15 added for aurora's new feature start
        holder.root.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                startBackFromSearchAnimation();
            }
        });
        // Aurora liugj 2013-10-15 added for aurora's new feature end
        // Aurora liugj 2013-10-10 modified for aurora's new feature end
        if (!MmsApp.mIsSafeModeSupport) {
            SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
            if (searchManager != null) {
                SearchableInfo info = searchManager.getSearchableInfo(getActivity().getComponentName());
                mSearchView.setSearchableInfo(info);
            }
        }
        setListViewWatcher(new ConvFragment.ListViewWatcher() {

                @Override
                public void listViewChanged(boolean isChange) {
                    // TODO Auto-generated method stub
                    if (isChange && mSearchMode) {
                        //Gionee <zhouyj> <2013-06-17> modify for CR00826647 start
                        if (mSearchView != null && mSearchView.getSuggestionsAdapter() != null) {
                            mSearchView.getSuggestionsAdapter().notifyDataSetChanged();
                        }
                        //Gionee <zhouyj> <2013-04-25> modify for CR00826647 end
                        exitSearchMode();
                        Log.e("liugj", "-----------listViewChanged----------");
                        // Aurora liugj 2013-10-10 modified for aurora's new feature start
                        startBackFromSearchAnimation();
                        // Aurora liugj 2013-10-10 modified for aurora's new feature end
                    }
                }
            });
      }*/
    // Aurora liugj 2013-09-20 modified for aurora's new feature end
    
     // Aurora liugj 2013-10-10 modified for aurora's new feature start
    /*private void exitSearchMode() {
        Log.e("liugj", "-----------exitSearchMode----------");
        ((AuroraActivity)getActivity()).getAuroraActionBar().setVisibility(View.VISIBLE);
        final AnimationHolder holder = mAnimationHolder;        
        //onClose();
         mEncryptionTitle.setVisibility(View.GONE);
        //mCancelSearchBtn.setVisibility(View.GONE);
        holder.searchbox.clearFocus();
        holder.searchbox.clearEditFocus();
//        hideInputMethod();
        // Aurora liugj 2013-09-29 added for aurora's new feature start
        if (mAdapter != null) {
            mAdapter.destroy();
             mAdapter = null;
             mSearchHandler = null;
        }
        mQueryString = null;
        // Aurora liugj 2013-09-29 added for aurora's new feature end
        setListViewWatcher(null);
        mSearchMode = false;
        // Aurora liugj 2013-11-15 modified for aurora's new feature start        
        mListView.setVisibility(View.VISIBLE);
        // Aurora liugj 2013-11-15 modified for aurora's new feature end
        // Aurora liugj 2013-10-11 modified for aurora's new feature start
        mListView.auroraSetNeedSlideDelete(true);
        // Aurora liugj 2013-10-11 modified for aurora's new feature end
        // Aurora liugj 2013-09-24 modified for aurora's new feature start
        ((AuroraActivity)getActivity()).setMenuEnable(true);
       // Aurora liugj 2013-09-24 modified for aurora's new feature end
    }*/
    // Aurora liugj 2013-10-10 modified for aurora's new feature end

    private void hideInputMethod() {
        Activity activity = AuroraConvListActivity.sAuroraConvListActivity;
        if (activity == null) {
            return;
        }
        InputMethodManager inputMethodManager =
        // Aurora xuyong 2014-11-26 modified for bug #10049 start
            (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(activity.getWindow()!=null && activity.getWindow().getCurrentFocus()!=null){
            inputMethodManager.hideSoftInputFromWindow(activity.getWindow().getCurrentFocus().getWindowToken(), 0);
        // Aurora xuyong 2014-11-26 modified for bug #10049 end
        }
    }
    
    //gionee gaoj 2012-9-20 added for CR00699291 start
    private Runnable mQueryRunnable = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            mContentChanged = true;
            //Gionee <zhouyj> <2013-05-06> modify for CR00799297 begin
            startQuery();
            //Gionee <zhouyj> <2013-05-06> modify for CR00799297 end
        }
    };
    //gionee gaoj 2012-9-20 added for CR00699291 end

    private final ConversationListAdapter.OnContentChangedListener mContentChangedListener =
        new ConversationListAdapter.OnContentChangedListener() {
        public void onContentChanged(ConversationListAdapter adapter) {
            //gionee gaoj 2012-9-20 added for CR00699291 start
             if (mIsonResume) {
                mNeedToMarkAsSeen = true;
                mQueryHandler.removeCallbacks(mQueryRunnable);
                // Aurora xuyong 2014-04-01 modify for aurora's new feature start
                mQueryHandler.postDelayed(mQueryRunnable, 0);
                // Aurora xuyong 2014-04-01 modify for aurora's new feature end
            }
            // gionee zhouyj 2013-02-05 add for CR00771239 start 
            if (mListViewWatcher != null) {
                mListViewWatcher.listViewChanged(true);
            }
            // gionee zhouyj 2013-02-05 add for CR00771239 end 
            //gionee gaoj 2012-9-20 added for CR00699291 end
        }
    };

    public void startEncryptionAsyncQuery() {
        try {
          // Aurora xuyong 2014-04-21 modified for bug #4460 start
            if (mListView != null) {
                mListView.auroraSetNeedSlideDelete(false);
            }
          // Aurora xuyong 2014-04-21 modified for bug #4460 end
            Conversation.startQueryForAll(mQueryHandler, THREAD_LIST_QUERY_TOKEN, true);
        } catch (SQLiteException e) {
            // Aurora xuyong 2014-11-12 modified for bug #9759 start
            Context context = AuroraConvListActivity.sContext;
            // Aurora xuyong 2014-11-12 modified for bug #9759 end
            if (context != null) {
                SqliteWrapper.checkSQLiteException(context, e);
            }
        }
    }

    private void startAsyncQuery() {
        try {
          // Aurora xuyong 2014-04-21 modified for bug #4460 start
            if (mListView != null) {
                mListView.auroraSetNeedSlideDelete(false);
            }
          // Aurora xuyong 2014-04-21 modified for bug #4460 end
            Conversation.startQueryForAll(mQueryHandler, THREAD_LIST_QUERY_TOKEN, null);

        } catch (SQLiteException e) {
            // Aurora xuyong 2014-11-12 modified for bug #9759 start
            Context context = AuroraConvListActivity.sContext;
            // Aurora xuyong 2014-11-12 modified for bug #9759 end
            if (context != null) {
                // Aurora xuyong 2014-11-26 modified for bug #10049 start
                SqliteWrapper.checkSQLiteException(context, e);
                // Aurora xuyong 2014-11-26 modified for bug #10049 end
            }
        }
    }

    /**
     * Checks to see if the number of MMS and SMS messages are under the limits for the
     * recycler. If so, it will automatically turn on the recycler setting. If not, it
     * will prompt the user with a message and point them to the setting to manually
     * turn on the recycler.
     */
    public synchronized void runOneTimeStorageLimitCheckForLegacyMessages() {
        // Aurora xuyong 2014-11-12 modified for bug #9759 start
        final Activity activity = AuroraConvListActivity.sAuroraConvListActivity;
        // Aurora xuyong 2014-11-12 modified for bug #9759 end
        if (activity == null) {
            return;
        }
        if (activity != null && Recycler.isAutoDeleteEnabled(activity)) {
            markCheckedMessageLimit();
            return;
        }
        new Thread(new Runnable() {
            public void run() {
                //gionee gaoj 2012-8-11 added for CR00673061 start
                if (activity != null) {
                    //gionee gaoj 2012-8-11 added for CR00673061 end
                // gionee zhouyj 2012-07-02 modify for CR00632512 start 
                if (Recycler.checkForThreadsOverLimit(AuroraConvListActivity.sContext)) {
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            Intent intent = new Intent(AuroraConvListActivity.sContext,
                                    WarnOfStorageLimitsActivity.class);
                            // gionee zhouyj 2013-04-09 modify for CR00795009 start
                            AuroraConvListActivity.sContext.startActivity(intent);
                            // gionee zhouyj 2013-04-09 modify for CR00795009 end
                        }
                    }, 2000);
                }
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        markCheckedMessageLimit();
                    }
                });
                // gionee zhouyj 2012-07-02 modify for CR00632512 end 
                //gionee gaoj 2012-8-11 added for CR00673061 start
                }
                //gionee gaoj 2012-8-11 added for CR00673061 end
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
        // Aurora xuyong 2014-02-18 added for aurora's new feature start
        mListView.auroraOnResume();
        // Aurora xuyong 2014-02-18 added for aurora's new feature end
         // Aurora liugj 2013-10-10 added for aurora's new feature start
        // 上次未完成动画，直接打开Search
        /*if (mHasGotoSearchAnimationPlaying) {
            mHasGotoSearchAnimationPlaying = false;

            mAnimationHolder.root.setVisibility(View.VISIBLE);

            enterSearchMode();
            
        }*/
        // Aurora liugj 2013-10-10 added for aurora's new feature end
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            siExpiredCheck.startExpiredCheck();
        }
//        // gionee zhouyj 2012-11-14 add for CR00729426 start 
//        if (PopUpView.getMmsFlag()) {
//            mQueryHandler.post(mQueryRunnable);;
//            PopUpView.resetMmsFlag(false);
//        }
//        // gionee zhouyj 2012-11-14 add for CR00729426 end 
//        ComposeMessageActivity.mDestroy = true;
        //gionee gaoj 2013-3-26 added for CR00787090 start
        mIsonResume = true;
        //gionee gaoj 2013-3-26 added for CR00787090 end
        Log.e("liugj","------ConvFragment.onResume-------");
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        // Aurora xuyong 2014-02-19 added for aurora's new feature start
        mListView.auroraOnPause();
        // Aurora xuyong 2014-02-19 added for aurora's new feature end
        Log.e("liugj","------ConvFragment.onPause-------");
          // Aurora liugj 2013-10-10 added for aurora's new feature start
        // 暂停的时候移除所有msg
        /*if (mHasGotoSearchAnimationPlaying || mHasBackFromSearchAnimationPlaying) {

            mAnimationHandler.removeMessages(MESSAGE_ANI_PREPARE_GOTO_SEARCH);
            mAnimationHandler.removeMessages(MESSAGE_ANI_START_GOTO_SEARCH);
            mAnimationHandler.removeMessages(MESSAGE_ANI_GOTO_SEARCH);
            mAnimationHandler.removeMessages(MESSAGE_ANI_PREPARE_BACK_FROM_SEARCH);
            mAnimationHandler.removeMessages(MESSAGE_ANI_START_BACK_FROM_SEARCH);
            mAnimationHandler.removeMessages(MESSAGE_ANI_BACK_FROM_SEARCH);
            
            if (mHasBackFromSearchAnimationPlaying) {
                mHasBackFromSearchAnimationPlaying = false;
                mAnimationHolder.root.setVisibility(View.GONE);
                // 释放截图
                mAnimationHolder.imageView.setImageDrawable(null);
                exitSearchMode();
            }

        }*/
        // Aurora liugj 2013-10-10 added for aurora's new feature END
//        mListView.closeOpenedItems();
        // Aurora liugj 2013-09-30 deleted for fix bug-179 start
        hideDeleteBack();
        // Aurora liugj 2013-09-30 deleted for fix bug-179 end
        //gionee gaoj 2013-3-26 added for CR00787090 start
        mIsonResume = false;
        //gionee gaoj 2013-3-26 added for CR00787090 end
        // gionee zhouyj 2013-04-01 add for CR00792152 start
        // Aurora liugj 2013-09-24 modified for aurora's new feature start
          // Aurora liugj 2013-10-25 modified for aurora's new feature start
        /*if (mActionBatchHandler != null && mActionBatchHandler.isInSelectionMode()) {
            mActionBatchHandler.leaveSelectionMode();
        }*/
          // Aurora liugj 2013-10-25 modified for aurora's new feature end
        // Aurora liugj 2013-09-24 modified for aurora's new feature end
        // gionee zhouyj 2013-04-01 add for CR00792152 end
    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
//        getActivity().getWindow().getDecorView().getViewTreeObserver().addOnPostDrawListener(mPostDrawListener);
        // Aurora xuyong 2014-11-12 modified for bug #9759 start
        Context context = AuroraConvListActivity.sContext;
        // Aurora xuyong 2014-11-12 modified for bug #9759 end
        if (context != null) {
            MessagingNotification.cancelNotification(context,
                    SmsRejectedReceiver.SMS_REJECTED_NOTIFICATION_ID);
        }
        DraftCache.getInstance().addOnDraftChangedListener(this);
        mNeedToMarkAsSeen = true;

        //gionee gaoj 2012-8-7 modified for CR00664245 start
        startQuery();
        //gionee gaoj 2012-8-7 modified for CR00664245 end
          // Aurora liugj 2013-10-25 modified for aurora's new feature start
        /*if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            siExpiredCheck.startExpiredCheck();
        }*/
         // Aurora liugj 2013-10-25 modified for aurora's new feature end
        // Aurora xuyong 2014-03-07 deleted for bug #11582 start
        //DraftCache.getInstance().refresh();
        // Aurora xuyong 2014-03-07 deleted for bug #11582 end
        if (!Conversation.loadingThreads()) {
            Contact.invalidateCache();
        }
        // Aurora xuyong 2015-03-11 added for bug #12139 start
        if (mSearchMode) {
            AuroraActivity activity = AuroraConvListActivity.sAuroraConvListActivity;
            if (activity == null) {
                return;
            }
            activity.setMenuEnable(false);
            AuroraSearchView searchbox = (AuroraSearchView) activity.getSearchView();
            String searchText = searchbox.getQuery().toString();
            this.onQueryTextChange(searchText);
        }
        // Aurora xuyong 2015-03-11 added for bug #12139 end
    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
//        getActivity().getWindow().getDecorView().getViewTreeObserver().removeOnPostDrawListener(mPostDrawListener);
        DraftCache.getInstance().removeOnDraftChangedListener(this);
        //getListView().setChoiceMode(AuroraFlingListView.CHOICE_MODE_MULTIPLE_MODAL);

        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            siExpiredCheck.stopExpiredCheck();
        }

        //gionee gaoj 2013-3-11 added for CR00782858 start
          // Aurora liugj 2013-11-07 deleted for hide widget start
        //MmsWidgetProvider.notifyDatasetChanged(AuroraConvListActivity.sContext);
          // Aurora liugj 2013-11-07 deleted for hide widget end
        //gionee gaoj 2013-3-11 added for CR00782858 end
        
        //Gionee <gaoj> <2013-05-28> add for CR00817770 begin
        if (MmsApp.mGnHideEncryption && mHideEncryp != ReadPopTag(AuroraConvListActivity.sContext, HIDEENCRYPTION)) {
            WritePopTag(AuroraConvListActivity.sContext, HIDEENCRYPTION, mHideEncryp);
        }
        //Gionee <gaoj> <2013-05-28> add for CR00817770 end
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        if (null != mActionBatchHandler && mActionBatchHandler.isInSelectionMode()) {
            mActionBatchHandler.leaveSelectionMode();
        }
        // Aurora liugj 2013-09-29 modified for aurora's new feature start
        if (mListAdapter != null && mListAdapter.getCursor() != null) {
            mListAdapter.getCursor().close();
        }
        if (mAdapter != null) {
            mAdapter.destroy();
             mAdapter = null;
            // Aurora xuyong 2014-03-17 deleted for bug #3064 start
             //mSearchHandler = null;
            // Aurora xuyong 2014-03-17 deleted for bug #3064 end
        }
        // Aurora liugj 2013-09-29 modified for aurora's new feature end
        super.onDestroy();
        // Aurora xuyong 2013-02-17 modified for bug #2356 start
        // Aurora xuyong 2014-04-17 deleted for bug #4318 start
        //if (mScrollListener != null) {
        //    mScrollListener.destroyThread();
        //}
        // Aurora xuyong 2014-04-17 deleted for bug #4318 end
        // Aurora xuyong 2013-02-17 modified for bug #2356 end
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            siExpiredCheck.stopSiExpiredCheckThread();
        }
        isEncryptionList = false;
        // Aurora xuyong 2014-09-10 added for uptimize start
        if (MmsApp.mGnMultiSimMessage && mConvaObserver != null) {
            AuroraConvListActivity.sContext.getContentResolver().unregisterContentObserver(mConvaObserver);
        }
        // Aurora xuyong 2014-09-10 added for uptimize end
    }

    //gionee gaoj 2012-6-30 added for CR00632246 start
    public interface ListViewWatcher {
        void listViewChanged(boolean isChange);
    }

    private ListViewWatcher mListViewWatcher = null;

    public void setListViewWatcher (ListViewWatcher l) {
        mListViewWatcher = l;
    }
    //gionee gaoj 2012-6-30 added for CR00632246 end

     // Aurora liugj 2014-01-06 added for bath-delete optimize start
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
                        // Aurora xuyong 2014-11-12 modified for bug #9759 start
                        Context context = AuroraConvListActivity.sContext;
                        // Aurora xuyong 2014-11-12 modified for bug #9759 end
                        if (context != null) {
                            cursor = context.getContentResolver().query(allThreadsUri, new String[] { "_id" }, "deleted = 1",
                                    null, null);
                            if (cursor != null && cursor.getCount() > 0) {
                                while (cursor.moveToNext() && !cursor.isAfterLast() && !cursor.isBeforeFirst()) {
                                    conversationsIds.append(cursor.getLong(0));
                                    if (!cursor.isLast()) {
                                        conversationsIds.append(",");
                                    }
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
                    //Log.d("liugj", "-------conversationsIds------"+conversationsIds.toString());
                    if (!TextUtils.isEmpty(conversationsIds)) {
                    // Aurora xuyong 2014-04-21 added for bug #4460 start
                        if (mListView != null) {
                            mListView.auroraSetNeedSlideDelete(false);
                        }
                    // Aurora xuyong 2014-04-21 added for bug #4460 end
                        Conversation.startDelete(mQueryHandler, DELETE_CONVERSATION_TOKEN,
                                true, conversationsIds.toString(), "OneLast");
                    }
                }
            }).start();
        }
    }
    // Aurora liugj 2014-01-06 added for bath-delete optimize end
    // Aurora xuyong 2014-06-07 deleted for bug #5377 start
    // Aurora xuyong 2014-04-23 added for bug #4218 start
    // Aurora xuyong 2014-04-25 modified for aurora's new feature start
    // Aurora xuyong 2014-05-05 modified for aurora's new feature start
    //private static final int INIT_NUM = 10;
    // Aurora xuyong 2014-05-05 modified for aurora's new feature end
    // Aurora xuyong 2014-04-25 modified for aurora's new feature end
    // Aurora xuyong 2014-04-24 modified for aurora's new feature start
    // Aurora xuyong 2014-05-05 modified for aurora's new feature start
    //private static final int OFFSET = 100;
    // Aurora xuyong 2014-05-05 modified for aurora's new feature end
    // Aurora xuyong 2014-04-24 modified for aurora's new feature end
    // Aurora xuyong 2014-04-25 deleted for aurora's new feature start
    //private static final int ONCE_NUMBER = 250;
    // Aurora xuyong 2014-04-25 deleted for aurora's new feature end
    //private int hasInitialiedCount = 0;
    //private Cursor mCursor;
    // Aurora xuyong 2014-04-25 modified for aurora's new feature start
    //private void initPriorityRecipients(final Cursor cursor) {
    // Aurora xuyong 2014-04-25 modified for aurora's new feature end
    /*    if (cursor != null && !cursor.isClosed()) {
            int count = 0;
            while (cursor.moveToNext() && count < INIT_NUM) {
                Conversation.from(ConvFragment.this.getActivity(), cursor);
                count++;
            }
            hasInitialiedCount += count;
            cursor.moveToFirst();
        }
    }*/
    // Aurora xuyong 2014-04-23 added for bug #4218 end
    // Aurora xuyong 2014-06-24 added for aurora's new feture start
    // Aurora xuyong 2014-08-27 modified for aurora's new feature start
    private void operateAfterChangeCursor(Cursor result) {
    // Aurora xuyong 2014-08-27 modified for aurora's new feature end
        if (!mSearchMode) {
            if (mListAdapter.isEmpty()) {
                if (isEncryptionList) {
                    mEmptyTextView.setText(R.string.no_encryption_conversations);
                } else {
                    mEmptyTextView.setText(R.string.no_conversations);
                }
                mEmptyView.setVisibility(View.VISIBLE);
                if (mListLayout != null) {
                    mListLayout.setVisibility(View.GONE);
                }
                mListView.setVisibility(View.GONE);
                mEncryptionTitle.setVisibility(View.GONE);
            } else {
                if (!isInit) {
                    isInit = true;
                    mSearchLayout = (TextView) fragmentView.findViewById(R.id.search_layout);
                    mListView.setOnKeyListener(mThreadListKeyListener);
                    // Aurora xuyong 2014-11-12 modified for bug #9759 start
                    Context context = AuroraConvListActivity.sContext;
                    // Aurora xuyong 2014-11-12 modified for bug #9759 end
                    if (context == null) {
                        return;    
                    }
                    mListLayout = LayoutInflater.from(context).inflate(R.layout.aurora_searchbox_layout, null);
                    if (mListLayout != null) {
                        mListLayout.setOnClickListener(null);
                    }
                    mSearchView    = (TextView) mListLayout.findViewById(R.id.conversation_header_searchbox);
                    // Aurora xuyong 2015-06-30 modified for bug #13931 start
                    if (mListLayout != null) {
                        mListLayout.setVisibility(View.VISIBLE);
                    }
                    // Aurora xuyong 2015-06-30 modified for bug #13931 end
                    mListView.addHeaderView(mListLayout);
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
                            Conversation conv = Conversation.from(AuroraConvListActivity.sContext, cursor);
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
                    mListView.setOnItemLongClickListener(ConvFragment.this);
                    if (mSearchView != null) {
                        mSearchView.setOnClickListener(new View.OnClickListener() {
                            
                            @Override
                            public void onClick(View v) {
                                if (!mSearchMode) {
                                    // Aurora xuyong 2014-11-12 modified for bug #9759 start
                                    Activity activity = AuroraConvListActivity.sAuroraConvListActivity;
                                    // Aurora xuyong 2014-11-12 modified for bug #9759 end
                                    if (activity == null) {
                                        return;
                                    }
                                    AuroraSearchView searchbox = (AuroraSearchView) ((AuroraActivity)activity).getSearchView();
                                    if(searchbox == null){
                                        return;
                                    }
                                    ((AuroraActivity)activity).showSearchviewLayout();
                                    gotoSearchMode();
                                }
                            }
                        });
                    }
                }

                if (isEncryptionList) {
                    mEncryptionTitle.setText(R.string.gn_encryption);
                    mEncryptionTitle.setVisibility(View.VISIBLE);
                } else {
                    mEncryptionTitle.setVisibility(View.GONE);
                }
                mEmptyView.setVisibility(View.GONE);
                if (mListLayout != null) {
                    mListLayout.setVisibility(View.VISIBLE);
                }
                mListView.setVisibility(View.VISIBLE);
            }
        }
        if (!Conversation.isInitialized()) {
            Conversation.init(AuroraConvListActivity.sContext);
        }else{
          // Aurora xuyong 2014-08-27 modified for aurora's new feature start
            if (result != null && !result.isClosed()) {
                if (result.moveToFirst()) {
 // Aurora yudingmin 2014-08-30 modified for optimize start    
                    Conversation.removeInvalidCache(result, mListAdapter.getCount());
 // Aurora yudingmin 2014-08-30 modified for optimize end    
                }
          // Aurora xuyong 2014-08-27 modified for aurora's new feature end
            }
        }
        
        if (mNeedToMarkAsSeen) {
            mNeedToMarkAsSeen = false;
            Conversation.markAllConversationsAsSeen(AuroraConvListActivity.sContext);

            // Delete any obsolete threads. Obsolete threads are threads that aren't
            // referenced by at least one message in the pdu or sms tables. We only call
            // this on the first query (because of mNeedToMarkAsSeen).
            mHandler.post(mDeleteObsoleteThreadsRunnable);
        }
        Activity activity = AuroraConvListActivity.sAuroraConvListActivity;
        if (activity == null) {
            return;
        }
        activity.invalidateOptionsMenu();
        if(mContentChanged) {
            if(!mListChange && null != mActionBatchHandler) {
                // Aurora xuyong 2014-08-27 modified for aurora's new feature start
                if (result.getCount() > mThreadsMap.size()) {
                // Aurora xuyong 2014-08-27 modified for aurora's new feature end
                    mSetEncryption.clear();
                    mThreadsMap.clear();
                    initThreadsMap();
                }
                mActionBatchHandler.refreshDataSet();
                mActionBatchHandler.updateUi();
                // Aurora xuyong 2014-08-27 modified for aurora's new feature start
                if(result.getCount() != mActionBatchHandler.getSelected().size()) {
                // Aurora xuyong 2014-08-27 modified for aurora's new feature end
                    mListChange = true;
                }
            }
            mContentChanged = false;
        }
        if (!isEncryptionList && MmsApp.mGnHideEncryption && MmsApp.mGnSmartGuide && ReadPopTag(AuroraConvListActivity.sContext, FIRSTENCRYPTION)
                && !ReadPopTag(AuroraConvListActivity.sContext, ISOPEN_STRING)) {
            if (queryHasEncryption()) {
                OpenPopLayout();
                WritePopTag(AuroraConvListActivity.sContext, ISOPEN_STRING);
            }
        }
    }
    // Aurora xuyong 2014-06-24 added for aurora's new feture end
    // Aurora xuyong 2014-08-27 added for aurora's new feature start
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
    // Aurora xuyong 2014-08-27 added for aurora's new feature end
    // Aurora xuyong 2014-06-07 deleted for bug #5377 start
    private final class ThreadListQueryHandler extends BaseProgressQueryHandler {
        private final String CONV_TAG = "ConvFragment";
        private boolean lockFlag = false;
        private boolean starFlag = false;

        public ThreadListQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
          // Aurora xuyong 2014-04-21 added for bug #4460 start
            if (mListView != null) {
             // Aurora xuyong 2014-04-24 modify for aurora's new feature start
             // Aurora xuyong 2014-07-23 modified for bug #5384 start
                if (!(isBottomMenuShowing() || mSearchMode)) {
             // Aurora xuyong 2014-07-23 modified for bug #5384 end
                    mListView.auroraSetNeedSlideDelete(true);
                }
             // Aurora xuyong 2014-04-24 modify for aurora's new feature end
            }
          // Aurora xuyong 2014-04-21 added for bug #4460 end
            //gionee gaoj 2012-10-12 added for CR00711168 start
            if (MmsApp.mIsSafeModeSupport) {
                mEmptyTextView.setText(R.string.no_conversations);
                mEncryptionTitle.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
                mListLayout.setVisibility(View.GONE);
                mListView.setVisibility(View.GONE);
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
                // gionee zhouyj 2012-11-07 add for CR00723358 start 
                    // Aurora liugj 2013-09-24 modified for aurora's new feature start
                if(mActionBatchHandler != null) {
                    mActionBatchHandler.leaveSelectionMode();
                    // Aurora liugj 2013-09-24 modified for aurora's new feature end
                }
                if (mDelAllDialog != null && mDelAllDialog.isShowing()) {
                    mDelAllDialog.dismiss();
                }
                // gionee zhouyj 2012-11-07 add for CR00723358 end 
                return;
            }
            //gionee gaoj 2012-10-12 added for CR00711168 end
              if (cursor == null) {
                   return;
              }
            switch (token) {
            case THREAD_LIST_QUERY_TOKEN:
                // Aurora liugj 2013-10-08 modified for aurora's new feature start
             // Aurora xuyong 2014-06-07 modified for bug #5377 start
                //mListAdapter.changeCursor(cursor);
             // Aurora xuyong 2014-08-27 deleted for aurora's new feature start
                //mCursor = cursor;
             // Aurora xuyong 2014-08-27 deleted for aurora's new feature end
             // Aurora xuyong 2014-06-17 added for aurora's new feature start
                final Cursor transCursor = cursor;
             // Aurora xuyong 2014-06-17 added for aurora's new feature end
             // Aurora xuyong 2014-08-27 added for aurora's new feature start
 // Aurora yudingmin 2014-08-30 deleted for optimize start    
//                mQueryId++;
//                final int tempQueryId = mQueryId;
 // Aurora yudingmin 2014-08-30 deleted for optimize end    
             // Aurora xuyong 2014-08-27 added for aurora's new feature end
                // Aurora yudingmin 2014-09-01 modidfied for optimize start
                if (mInitStatu != INIT_ALL_DONE) {
                    // Aurora yudingmin 2014-09-01 modidfied for optimize end
                // Aurora xuyong 2014-06-17 modified for aurora's new feature start
                // Aurora xuyong 2014-08-26 modified for bug modify start
                    mSingleThreadExecutor.execute(new Runnable() {
                // Aurora xuyong 2014-08-26 modified for bug modify end
                        
                        @Override
                        public void run() {
                            int initCount = INIT_ITEM_COUNT;
                       // Aurora xuyong 2014-07-17 added for bug #6619 start
                       // Aurora xuyong 2014-07-28 modified for bug #6931 start
                            if (transCursor == null || transCursor.isClosed() || transCursor.getCount() <= 0) {
                       // Aurora xuyong 2014-07-28 modified for bug #6931 end
                          // Aurora xuyong 2014-07-21 added for reject feature start
                                Message msg = mHandler.obtainMessage(CHANGE_MC1_CURSOR);
                                msg.sendToTarget();
                          // Aurora xuyong 2014-07-21 added for reject feature end
                                return;
                            }
                       // Aurora xuyong 2014-07-17 added for bug #6619 end
                            int count = transCursor.getCount();
                            int columnCount = transCursor.getColumnCount();
                            MatrixCursor mc1 = new MatrixCursor(Conversation.ALL_THREADS_PROJECTION, initCount);
                       // Aurora xuyong modified for aurora's new feature start
                            transCursor.moveToPosition(-1);
                       // Aurora xuyong 2014-07-24 modified for bug #6812 start
                       // Aurora xuyong 2014-07-30 modified for bug #6953 start
                            if (transCursor.moveToFirst() && !transCursor.isAfterLast() && !transCursor.isBeforeFirst()) {
                       // Aurora xuyong 2014-07-30 modified for bug #6953 end
                       // Aurora xuyong 2014-07-24 modified for bug #6812 end
                       // Aurora xuyong modified for aurora's new feature end
                                Object[] firstItemDetail = new Object[columnCount];
                                for (int i = 0; i < columnCount; i++) {
                             // Aurora xuyong 2014-07-30 modified for bug #6953 start
                                    try {
                                        firstItemDetail[i] = transCursor.getString(i);
                                    } catch (CursorIndexOutOfBoundsException e) {
                                        Log.e("CIOEC", "current position = " + i + ", transCursor.getColumnCount() = " + transCursor.getColumnCount());
                                    }
                             // Aurora xuyong 2014-07-30 modified for bug #6953 end
                                }
                                mc1.addRow(firstItemDetail);
                                initCount--;
                                while (transCursor.moveToNext() && initCount > 0 && !transCursor.isBeforeFirst() && !transCursor.isAfterLast()) {
                                    Object[] itemDetail = new Object[columnCount];
                                    for (int i = 0; i < columnCount; i++) {
                                 // Aurora xuyong 2014-07-30 modified for bug #6953 start
                                        try {
                                            itemDetail[i] = transCursor.getString(i);
                                        } catch (CursorIndexOutOfBoundsException e) {
                                            Log.e("CIOEC", "current position = " + i + ", transCursor.getColumnCount() = " + transCursor.getColumnCount());
                                        }
                                 // Aurora xuyong 2014-07-30 modified for bug #6953 end
                                    }
                                    mc1.addRow(itemDetail);
                                    initCount--;
                                }
                          // Aurora xuyong 2014-07-30 modified for bug #6953 start
                                if (mc1.moveToFirst() && !mc1.isAfterLast() && !mc1.isBeforeFirst()) {
                          // Aurora xuyong 2014-07-30 modified for bug #6953 end
                             // Aurora xuyong 2014-08-27 modified for aurora's new feature start
                                    // Aurora xuyong 2014-11-26 modified for bug #10049 start
                                    Context context = AuroraConvListActivity.sContext;
                                    // Aurora xuyong 2014-11-26 modified for bug #10049 end
                                    if (context != null) {
                             // Aurora xuyong 2014-08-27 modified for aurora's new feature end
                                // Aurora xuyong 2014-08-01 modified for bug #7040 start
                                        try {
                                    // Aurora xuyong 2014-08-27 modified for aurora's new feature start
 // Aurora yudingmin 2014-08-30 modified for optimize start    
                                            Conversation.from(context, mc1, true);
 // Aurora yudingmin 2014-08-30 modified for optimize end    
                                    // Aurora xuyong 2014-08-27 modified for aurora's new feature end
                                        } catch (CursorIndexOutOfBoundsException e) {
                                            
                                        }
                                // Aurora xuyong 2014-08-01 modified for bug #7040 end
                                        while(mc1.moveToNext() && !mc1.isBeforeFirst() && !mc1.isAfterLast()) {
                                    // Aurora xuyong 2014-08-01 modified for bug #7040 start
                                            try {
                                        // Aurora xuyong 2014-08-27 modified for aurora's new feature start
// Aurora yudingmin 2014-08-30 modified for optimize start    
                                                Conversation.from(context, mc1, true);
// Aurora yudingmin 2014-08-30 modified for optimize end    
                                        // Aurora xuyong 2014-08-27 modified for aurora's new feature end
                                            } catch (CursorIndexOutOfBoundsException e) {
                                                
                                            }
                                    // Aurora xuyong 2014-08-01 modified for bug #7040 end
                                        }
                                    }
                                }
                            }
                            transCursor.moveToFirst();
                            Message msg = mHandler.obtainMessage(CHANGE_MC1_CURSOR);
                       // Aurora xuyong 2014-08-27 modified for aurora's new feature start
// Aurora yudingmin 2014-08-30 modified for optimize start    
                            if(mc1 != null){
                                mc1.close();
                            }
                            msg.obj = transCursor;//new QueryResult(transCursor, mc1);
//                            msg.arg1 = tempQueryId;
// Aurora yudingmin 2014-08-30 modified for optimize end    
                       // Aurora xuyong 2014-08-27 modified for aurora's new feature end
                            msg.sendToTarget();
                        }
                    // Aurora xuyong 2014-08-26 modified for bug modify start
                    });
                // Aurora xuyong 2014-08-26 modified for bug modify end
                    // Aurora xuyong 2014-06-17 modified for aurora's new feature start
                } else {
                // Aurora xuyong 2014-08-27 modified for aurora's new feature start
                    // Aurora yudingmin 2014-09-01 added for optimize start
                    mListAdapter.changeCountBeforeDataChanged(transCursor == null?0:transCursor.getCount());
                    // Aurora yudingmin 2014-09-01 added for optimize end
                    // Aurora yudingmin 2014-09-01 added for optimize start
                    mListView.auroraSetRubbishBackNoAnim();
                    // Aurora yudingmin 2014-09-01 added for optimize end
                    mListAdapter.changeCursor(transCursor);
                    operateAfterChangeCursor(transCursor);
                // Aurora xuyong 2014-08-27 modified for aurora's new feature end
                }
                break;

            case THREAD_LIST_LOCKED_QUERY_TOKEN:
                //gionee gaoj 2012-5-16 remove locked for CRCR00600687 start
                lockFlag = false;
//                lockFlag = (cursor != null && cursor.getCount() > 0);
                //gionee gaoj 2012-5-16 remove locked for CRCR00600687 end
                cursor.close();
                break;
            case THREAD_LIST_FAVORITE_QUERY_TOKEN:
            // Aurora liugj 2013-09-24 modified for aurora's new feature start
                starFlag = (cursor != null && cursor.getCount() > 0);
                boolean isAll = false;
                // gionee zhouyj 2012-05-23 modify for CR00607141 start
                if (!isEncryptionList && mAllSelectedThreadIds.size() == mListView.getCount()) {
                    isAll = true;
                }
                // gionee zhouyj 2012-05-23 modify for CR00607141 end
                final boolean isAllSelected = isAll;
                long threadId = (Long)cookie;
                if (isAllSelected){
                        // Aurora liugj 2014-01-22 modified for listview delete animation start
                    // Aurora xuyong 2014-11-12 modified for bug #9759 start
                    Activity activity = AuroraConvListActivity.sAuroraConvListActivity;
                    // Aurora xuyong 2014-11-12 modified for bug #9759 end
                    if (activity != null) {
                        confirmDeleteGnThreadDialog(
                                new GnDeleteThreadListener(mListView, -1, mQueryHandler,
                                        activity), isAllSelected, starFlag,
                                        activity);
                    }
                        // Aurora liugj 2014-01-22 modified for listview delete animation end

                } else {
                    long tempThreadId = threadId;
                    if (mAllSelectedThreadIds.size() != 1){
                        tempThreadId = 9999;
                    }
                    // gionee zhouyj 2012-08-11 add for CR00664268 start 
                    else { //mAllSelectedThreadIds.size() = 1, but threadId = -1
                        Iterator it = mAllSelectedThreadIds.iterator();
                        while(it.hasNext()) {
                            tempThreadId = (Long)it.next();
                            break;
                        }
                    }
                    // gionee zhouyj 2012-08-11 add for CR00664268 end 
                        // Aurora liugj 2014-01-22 modified for listview delete animation start
                    // Aurora xuyong 2014-11-12 modified for bug #9759 start
                    Activity activity = AuroraConvListActivity.sAuroraConvListActivity;
                    // Aurora xuyong 2014-11-12 modified for bug #9759 end
                    if (activity != null) {
                        confirmDeleteGnThreadDialog(
                                new GnDeleteThreadListener(mListView, tempThreadId, mQueryHandler,
                                // Aurora xuyong 2014-11-26 modified for bug #10049 start
                                        activity), isAllSelected, starFlag,
                                        activity);
                                // Aurora xuyong 2014-11-26 modified for bug #10049 end
                    }
                        // Aurora liugj 2014-01-22 modified for listview delete animation end
                }
                cursor.close();
            // Aurora liugj 2013-09-24 modified for aurora's new feature end
                break;
            //gionee gaoj 2012-10-15 added for CR00705539 start
            case THREAD_LIST_FAVORITE_QUERY_ONE_TOKEN:
                long onethread = (Long)cookie;
                //Gionee <zhouyj> <2013-05-02> add for CR00802651 begin
                if (MmsApp.mGnDeleteRecordSupport && mAllSelectedThreadIds != null) {
                    mAllSelectedThreadIds.clear();
                    mAllSelectedThreadIds.add(onethread);
                }
                //Gionee <zhouyj> <2013-05-02> add for CR00802651 end
//                starFlag = (cursor != null && cursor.getCount() > 0);
//                confirmDeleteGnThreadDialog(
//                        new GnDeleteThreadListener(onethread, mQueryHandler,
//                                getActivity()), false, starFlag,
//                        getActivity()); 
                // Aurora liugj 2013-09-24 modified for aurora's new feature start
                if(mActionBatchHandler != null) {
                    mActionBatchHandler.leaveSelectionMode();
                // Aurora liugj 2013-09-24 modified for aurora's new feature end
                }
                    // Aurora liugj 2014-01-22 modified for listview delete animation start
                // Aurora xuyong 2014-11-26 modified for bug #10049 start
                auroraDeleteConversations(mListView, onethread, mQueryHandler, AuroraConvListActivity.sContext, true);
                // Aurora xuyong 2014-11-26 modified for bug #10049 end
                    // Aurora liugj 2014-01-22 modified for listview delete animation end
//                cursor.close();
                break;
            case THREAD_LIST_FAVORITE_QUERY_ALL_TOKEN:
            // Aurora liugj 2013-09-24 modified for aurora's new feature start
                starFlag = (cursor != null && cursor.getCount() > 0);
                    // Aurora liugj 2014-01-22 modified for listview delete animation start
                // Aurora xuyong 2014-11-12 modified for bug #9759 start
                Activity activity = AuroraConvListActivity.sAuroraConvListActivity;
                // Aurora xuyong 2014-11-12 modified for bug #9759 end
                if (activity != null) {
                confirmDeleteGnThreadDialog(
                        new GnDeleteThreadListener(mListView, -1, mQueryHandler,
                                activity), true, starFlag,
                                activity);
                }
                    // Aurora liugj 2014-01-22 modified for listview delete animation end
                cursor.close();
            // Aurora liugj 2013-09-24 modified for aurora's new feature end
                break;
            //gionee gaoj 2012-10-15 added for CR00705539 end
            default:
            }
        }

         // Aurora liugj 2014-01-06 added for bath-delete optimize start
        /*@Override
        protected void onUpdateComplete(int token, Object cookie, int result) {            
            
            switch (token) {
            case MUL_DELETE_CONVERSATIONS_TOKEN:
                // Update the notification for new messages since they
                // may be deleted.
                MessagingNotification.nonBlockingUpdateNewMessageIndicator(AuroraConvListActivity.sContext,
                        false, false);
                // Update the notification for failed messages since they
                // may be deleted.
                MessagingNotification.updateSendFailedNotification(AuroraConvListActivity.sContext);
                MessagingNotification.updateDownloadFailedNotification(AuroraConvListActivity.sContext);

                //Update the notification for new WAP Push messages
                if(FeatureOption.MTK_WAPPUSH_SUPPORT){
                    WapPushMessagingNotification.nonBlockingUpdateNewMessageIndicator(AuroraConvListActivity.sContext,false);
                }

                // Make sure the list reflects the delete
                //Gionee <zhouyj> <2013-05-04> modify for CR00799297 begin
                ConvFragment.this.startQuery();
                //Gionee <zhouyj> <2013-05-04> modify for CR00799297 end
                if (progress()) {
                        // Aurora liugj 2013-10-15 added for aurora's new feature start
                        Toast.makeText(AuroraConvListActivity.sContext, R.string.conversation_has_deleted, Toast.LENGTH_SHORT).show();
                        // Aurora liugj 2013-10-15 added for aurora's new feature end
                    // Aurora liugj 2013-11-08 modified for aurora's new feature start
                    if(mActionBatchHandler != null) {
                        mActionBatchHandler.leaveSelectionMode();
                    }
                    // Aurora liugj 2013-11-08 modified for aurora's new feature end
                    //dismissProgressDialog();
                }
                break;
            }
        }
        // Aurora liugj 2014-01-06 added for bath-delete optimize end*/

        // Aurora liugj 2014-01-06 modified for bath-delete optimize start
        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            mIsDeleting = false;
          // Aurora xuyong 2014-04-21 added for bug #4460 start
            if (mListView != null) {
             // Aurora xuyong 2014-04-24 modify for aurora's new feature start
             // Aurora xuyong 2014-07-23 modified for bug #5384 start
                if (!(isBottomMenuShowing() || mSearchMode)) {
             // Aurora xuyong 2014-07-23 modified for bug #5384 end
                    mListView.auroraSetNeedSlideDelete(true);
                }
             // Aurora xuyong 2014-04-24 modify for aurora's new feature end
            }
          // Aurora xuyong 2014-04-21 added for bug #4460 end
              // When this callback is called after deleting, token is 1803(DELETE_OBSOLETE_THREADS_TOKEN)
            // not 1801(DELETE_CONVERSATION_TOKEN)
            // gionee zhouyj 2012-04-28 modified for CR00585947 start
            CBMessagingNotification.updateNewMessageIndicator(AuroraConvListActivity.sContext);
            // gionee zhouyj 2012-04-28 modified for CR00585947 end
            switch (token) {
            //gionee gaoj 2012-10-15 added for CR00705539 start
            case DELETE_CONVERSATION_NOT_LAST_TOKEN:
                //Not the Last , Nothing to do here.
                break;
            //gionee gaoj 2012-10-15 added for CR00705539 end
            case DELETE_CONVERSATION_TOKEN:
                    // Aurora liugj 2014-01-06 modified for bug-1496 start
                // Rebuild the contacts cache now that a thread and its associated unique
                // recipients have been deleted.
                //Contact.init(AuroraConvListActivity.sContext);

                // Make sure the conversation cache reflects the threads in the DB.
                //Conversation.init(AuroraConvListActivity.sContext);
                    // Aurora liugj 2014-01-06 modified for bug-1496 end

                try {
                    if(GnPhone.phone != null) {
                        if(GnITelephony.isTestIccCard(GnPhone.phone)) {
                            //Log.d(CONV_TAG, "All threads has been deleted, send notification..");
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
                // Aurora xuyong 2014-04-16 modified for aurora's new feature start
                // avoid ANR sometimes
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        // Update the notification for failed messages since they
                        // may be deleted.
                        MessagingNotification.nonBlockingUpdateNewMessageIndicator(AuroraConvListActivity.sContext,
                                false, false);
                        MessagingNotification.updateSendFailedNotification(AuroraConvListActivity.sContext);
                        MessagingNotification.updateDownloadFailedNotification(AuroraConvListActivity.sContext);
                        //Update the notification for new WAP Push messages
                        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
                            WapPushMessagingNotification.nonBlockingUpdateNewMessageIndicator(AuroraConvListActivity.sContext,false);
                        }
                    }
                    
                }).start();
                //MessagingNotification.nonBlockingUpdateNewMessageIndicator(AuroraConvListActivity.sContext,
                //        false, false);
                // Update the notification for failed messages since they
                // may be deleted.
                //MessagingNotification.updateSendFailedNotification(AuroraConvListActivity.sContext);
                //MessagingNotification.updateDownloadFailedNotification(AuroraConvListActivity.sContext);

                //Update the notification for new WAP Push messages
                //if(FeatureOption.MTK_WAPPUSH_SUPPORT){
                //    WapPushMessagingNotification.nonBlockingUpdateNewMessageIndicator(AuroraConvListActivity.sContext,false);
                //}
                // Aurora xuyong 2014-04-16 modified for aurora's new feature end
                // Make sure the list reflects the delete
                //Gionee <zhouyj> <2013-05-04> modify for CR00799297 begin
                ConvFragment.this.startQuery();
                //Gionee <zhouyj> <2013-05-04> modify for CR00799297 end
                //if (progress()) {
                        // Aurora liugj 2013-10-15 added for aurora's new feature start
                        Toast.makeText(AuroraConvListActivity.sContext, R.string.conversation_has_deleted, Toast.LENGTH_SHORT).show();
                        // Aurora liugj 2013-10-15 added for aurora's new feature end
                    // Aurora liugj 2013-11-08 modified for aurora's new feature start
                    if(mActionBatchHandler != null) {
                        mActionBatchHandler.leaveSelectionMode();
                    }
                    // Aurora liugj 2013-11-08 modified for aurora's new feature end
                    dismissProgressDialog();
                //}
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
                // Aurora xuyong 2041-03-10 added for bug #2854 start
             // Aurora xuyong 2014-04-16 modified for aurora's new feature start
                // avoid ANR sometimes
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        // Update the notification for failed messages since they
                        // may be deleted.
                        MessagingNotification.nonBlockingUpdateNewMessageIndicator(AuroraConvListActivity.sContext,
                                false, false);
                        MessagingNotification.updateSendFailedNotification(AuroraConvListActivity.sContext);
                        MessagingNotification.updateDownloadFailedNotification(AuroraConvListActivity.sContext);
                        //Update the notification for new WAP Push messages
                        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
                            WapPushMessagingNotification.nonBlockingUpdateNewMessageIndicator(AuroraConvListActivity.sContext,false);
                        }
                    }
                    
                }).start();
                //MessagingNotification.nonBlockingUpdateNewMessageIndicator(AuroraConvListActivity.sContext,
                //false, false);
                // Update the notification for failed messages since they
                // may be deleted.
                //MessagingNotification.updateSendFailedNotification(AuroraConvListActivity.sContext);
                //MessagingNotification.updateDownloadFailedNotification(AuroraConvListActivity.sContext);

                //Update the notification for new WAP Push messages
                //if(FeatureOption.MTK_WAPPUSH_SUPPORT){
                //    WapPushMessagingNotification.nonBlockingUpdateNewMessageIndicator(AuroraConvListActivity.sContext,false);
                //}
                // Aurora xuyong 2014-04-16 modified for aurora's new feature end
                // Make sure the list reflects the delete
                ConvFragment.this.startQuery();
                if (progress()) {
                    Toast.makeText(AuroraConvListActivity.sContext, R.string.conversation_has_deleted, Toast.LENGTH_SHORT).show();
                     if(mActionBatchHandler != null) {
                        mActionBatchHandler.leaveSelectionMode();
                    }
                    dismissProgressDialog();
                }
                // Aurora xuyong 2041-03-10 added for bug #2854 end
                break;    
            }
        }
          // Aurora liugj 2014-01-06 modified for bath-delete optimize end
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
                // Aurora xuyong 2015-03-03 modified for NullpointerException start
                if (dialog != null) {
                    dialog.dismiss();
                }
                // Aurora xuyong 2015-03-03 modified for NullpointerException end
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
                        long id = getListView().getSelectedItemId();
                        if (id > 0) {
                            if (MmsApp.mEncryption) {
                                if (checkoutEncryption()) {
                                    // Aurora xuyong 2014-11-12 modified for bug #9759 start
                                    Context context = AuroraConvListActivity.sContext;
                                    // Aurora xuyong 2014-11-12 modified for bug #9759 end
                                    if (context != null) {
                                        final Intent intent = new Intent();
                                        intent.setClass(context, MsgChooseLockPassword.class);
                                        intent.putExtra("isdecryption", true);
                                        startActivityForResult(intent,
                                                ConvFragment.UPDATE_PASSWORD_REQUEST);
                                    }
                                    break;
                                }
                            }

                            gnDeleteConversations();

                        }
                        return true;
                    }
                    // Aurora liugj 2013-09-24 added for aurora's new feature start
                    case KeyEvent.KEYCODE_MENU: {
                        if (mActionBatchHandler != null && mActionBatchHandler.isInSelectionMode()) {
                            return true;
                        }
                        break;
                    }
                    // Aurora liugj 2013-09-24 added for aurora's new feature end
                }
            }
            return false;
        }
    };

    @Override
    public void onDraftChanged(long threadId, boolean hasDraft) {
        // TODO Auto-generated method stub
       // Aurora xuyong 2014-04-09 modified for aurora's new feature start
        if (mNeedSetDataChanged) {
            mQueryHandler.post(new Runnable() {
                public void run() {
                    mListAdapter.notifyDataSetChanged();
                }
            });
        }
        mNeedSetDataChanged = true;
      // Aurora xuyong 2014-04-09 modified for aurora's new feature end
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);
         // Aurora liugj 2013-09-24 modified for aurora's new feature start
        if(null != mActionBatchHandler && mActionBatchHandler.isInSelectionMode()) {
            // Aurora liugj 2013-12-11 added for checkbox animation start
            AuroraCheckBox mCheckBox = (AuroraCheckBox) v.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
            // Aurora xuyong 2014-02-14 added for aurora's new feature start
            if (mCheckBox == null) {
                return;
            }
            // Aurora xuyong 2014-02-14 added for aurora's new feature end
            boolean isChecked = mCheckBox.isChecked();
            mCheckBox.auroraSetChecked(!isChecked, true);
            // Aurora liugj 2013-12-11 added for checkbox animation end
            mActionBatchHandler.getSelectionManger().toggle(id);
         // Aurora liugj 2013-10-19 modified for aurora's new feature start
          // Aurora liugj 2013-09-23 added for aurora's new feature start
        } else if (mSearchMode && mQueryString != null) {
        // Aurora liugj 2013-09-24 modified for aurora's new feature end
            Cursor cursor  = (Cursor) getListView().getItemAtPosition(position);
            // Aurora xuyong 2014-07-17 modified for bug #6619 start
            if (cursor == null || cursor.isClosed()) {
            // Aurora xuyong 2014-07-17 modified for bug #6619 end
                return;
            } 
            try {
                long threadId = cursor.getLong(cursor.getColumnIndex("thread_id"));
                long sourceId = cursor.getLong(cursor.getColumnIndex("_id"));
                // Aurora xuyong 2014-11-26 modified for bug #10049 start
                Intent onClickIntent = new Intent(AuroraConvListActivity.sContext, ComposeMessageActivity.class);
                // Aurora xuyong 2014-11-26 modified for bug #10049 end
                // Aurora xuyong 2014-04-10 modified for bug #4061 start
                // Aurora xuyong 2015-03-11 added for bug #12139 start
                if (searchResultsCache != null && searchResultsCache.containsKey(mQueryString)) {
                    searchResultsCache.remove(mQueryString);
                }
                // Aurora xuyong 2015-03-11 added for bug #12139 end
                onClickIntent.putExtra("highlight", mActivityQueryString);
                // Aurora xuyong 2014-04-10 modified for bug #4061 end
                onClickIntent.putExtra("select_id", sourceId);
                onClickIntent.putExtra("thread_id", threadId);
                startActivity(onClickIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        // Aurora liugj 2013-10-19 modified for aurora's new feature end
       // Aurora liugj 2013-09-23 added for aurora's new feature end      
        } else {
            Cursor cursor  = (Cursor) getListView().getItemAtPosition(position);
            // Aurora xuyong 2014-07-17 modified for bug #6619 start
            if (cursor == null || cursor.isClosed()) {
            // Aurora xuyong 2014-07-17 modified for bug #6619 end
                return;
            }   
            
             // gionee lwzh add for CR00774362 20130227 begin
            if (MmsApp.mGnMessageSupport) {
                // Aurora liugj 2013-11-14 modified for Monkey-Test bug start
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
                // Aurora liugj 2013-11-14 modified for Monkey-Test bug end
                return;
            }
            // gionee lwzh add for CR00774362 20130227 begin
            // Aurora xuyong 2014-08-27 modified for aurora's new feature start
            // Aurora xuyong 2014-11-26 modified for bug #10049 start
            Context context = AuroraConvListActivity.sContext;
            // Aurora xuyong 2014-11-26 modified for bug #10049 end
            if (null != context) {
            // Aurora xuyong 2014-08-27 modified for aurora's new feature end
             // Aurora xuyong 2014-08-01 modified for bug #7040 start
                Conversation conv = null;
                try {
                // Aurora xuyong 2014-08-27 modified for aurora's new feature start
                    conv = Conversation.from(context, cursor);
                // Aurora xuyong 2014-08-27 modified for aurora's new feature end
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
             // Aurora xuyong 2014-08-01 modified for bug #7040 enbd
            }
        }
    }
    
    //gionee gaoj added for CR00725602 20121201 start
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
            int position, long id) {
        // TODO Auto-generated method stub
        if (mSearchMode) {
            return false;
        }
        // Aurora liugj 2013-09-24 modified for aurora's new feature start
        if(null == mActionBatchHandler) {
            initThreadsMap();
            initActionBatchMode();
            mActionBatchHandler.enterSelectionMode(false, id);
        // Aurora liugj 2013-09-24 modified for aurora's new feature end
            return true;
        }
        return false;
    }
    // Aurora xuyong 2014-03-21 added for aurora's new feature start
    public void leaveBatchMode() {
        if(null != mActionBatchHandler) {
            mActionBatchHandler.leaveSelectionMode();
      }
    }
    
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
        if (mSearchView != null) {
            mSearchView.setClickable(true);
        }
        if (mListAdapter != null) {
          // Aurora xuyong 2014-04-25 modified for aurora's new feature start
            if (mNeedSetDataChanged) {
                mListAdapter.notifyDataSetChanged();
            }
            mNeedSetDataChanged = true;
          // Aurora xuyong 2014-04-25 modified for aurora's new feature end
        }
        // Aurora xuyong 2014-11-12 modified for bug #9759 start
        Activity activity = AuroraConvListActivity.sAuroraConvListActivity;
        // Aurora xuyong 2014-11-12 modified for bug #9759 end
        if (activity != null) {
            ((AuroraActivity)activity).setMenuEnable(true);
        }
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
    // Aurora xuyong 2014-03-21 added for aurora's new feature end
    // Aurora liugj 2013-09-24 added for aurora's new feature start
    protected void initActionBatchHandler() {
        // Aurora xuyong 2014-03-21 modified for aurora's new feature start
        // Aurora xuyong 2014-11-12 modified for bug #9759 start
        Activity activity = AuroraConvListActivity.sAuroraConvListActivity;
        // Aurora xuyong 2014-11-12 modified for bug #9759 end
        if (null != activity) {
            // Aurora xuyong 2014-11-26 modified for bug #10049 start
            mActionBatchHandler = new AuroraActionBatchHandler<Long>(AuroraConvListActivity.sAuroraConvListActivity, mAuroraActionBar) {
            // Aurora xuyong 2014-11-26 modified for bug #10049 end
            // Aurora xuyong 2014-03-21 modified for aurora's new feature end
                
                @Override
                public void enterSelectionMode(boolean autoLeave, Long itemPressing) {
                    Log.e("liugj", "------enterSelectionMode-----");
                    // Aurora liugj 2014-01-14 modified for aurora's new feature start
                    if (mSearchView != null) {
                        mSearchView.setClickable(false);
                    }
                    // Aurora liugj 2013-01-14 modified for aurora's new feature end
                    // Aurora liugj 2013-10-11 modified for aurora's new feature start
                 // Aurora xuyong 2014-04-21 modified for bug #4460 start
                    if (mListView != null) {
                        mListView.auroraSetNeedSlideDelete(false);
                    }
                 // Aurora xuyong 2014-04-21 modified for bug #4460 end
                    // Aurora liugj 2013-11-18 modified for aurora's new feature start
                    mListView.auroraEnableSelector(false);
                    mListAdapter.showCheckBox(true);
                    // Aurora liugj 2014-01-16 modified for check animation start
                    mListAdapter.setCheckBoxAnim(true);
                    // Aurora liugj 2014-01-16 modified for check animation end
                    // Aurora liugj 2013-11-18 modified for aurora's new feature end
                    // Aurora liugj 2013-10-11 modified for aurora's new feature end
                    // Aurora xuyong 2014-11-26 modified for bug #10049 start
                    AuroraConvListActivity.sAuroraConvListActivity.setMenuEnable(false);
                    // Aurora xuyong 2014-11-26 modified for bug #10049 end
                    //mFirstInMultiMode = true;
                    super.enterSelectionMode(autoLeave, itemPressing);
                    // Aurora liugj 2013-12-11 added for checkbox animation start
                 // Aurora xuyong 2014-04-25 modified for aurora's new feature start
                    if (mNeedSetDataChanged) {
                        mListAdapter.notifyDataSetChanged();
                    }
                    mNeedSetDataChanged = true;
                 // Aurora xuyong 2014-04-25 modified for aurora's new feature end
                    // Aurora liugj 2013-12-11 added for checkbox animation end
                }
    
                @Override
                public Set getDataSet() {
                    // TODO Auto-generated method stub
                    Set<Long> dataSet = new HashSet<Long>(mThreadsMap.size());
                    for(int i = 0; i < mThreadsMap.size(); i++)
                        dataSet.add(mThreadsMap.get(i));
                    return dataSet;
                }
                
                // Aurora liugj 2013-10-17 modified for aurora's new feature start
                @Override
                public void leaveSelectionMode() {
                    Log.e("liugj", "------leaveSelectionMode-----");
                    if (mAuroraMenu != null) {
                        // Aurora xuyong 2014-03-21 modified for aurora's new feature start
                        mAuroraActionBar.setShowBottomBarMenu(false);
                        mAuroraActionBar.showActionBarDashBoard();
                        onBottomMenuDismiss();
                        // Aurora xuyong 2014-03-21 modified for aurora's new feature end
                        //mAuroraMenu.dismiss();
                    }
                    /*if (mBottomAuroraMenu != null) {
                        mBottomAuroraMenu.dismiss();
                        // Aurora liugj 2013-10-09 modified for aurora's new feature start
                        ((AuroraActivity)getActivity()).getAuroraActionBar().contentViewFloatDown();
                        // Aurora liugj 2013-10-09 modified for aurora's new feature END
                    }*/
                }
                // Aurora liugj 2013-10-17 modified for aurora's new feature end
    
                @Override
                public void updateUi() {
                    // TODO Auto-generated method stub
                    /*if (mFirstInMultiMode) {
                        mFirstInMultiMode = false;
                    }else {*/
                        //mListAdapter.notifyDataSetChanged();
                    //}
                    mSelectCount = null != getSelected() ? getSelected().size() : 0;
                    // Aurora liugj 2013-09-25 modified for aurora's new feature start
                    mBottomAuroraMenu.setBottomMenuItemEnable(1, mSelectCount == 0 ? false : true);
                    // Aurora liugj 2013-09-25 modified for aurora's new feature end
                    //Log.e("liugj", "------updateUi-----" + (mSelectCount == 0 ? false : true));
                }
    
                // Aurora liugj 2014-01-07 modified for allcheck animation start
                @Override
                public void updateListView(int allShow) {
                    mListAdapter.updateAllCheckBox(allShow);
                 // Aurora xuyong 2014-04-25 modified for aurora's new feature start
                    if (mNeedSetDataChanged) {
                        mListAdapter.notifyDataSetChanged();
                    }
                    mNeedSetDataChanged = true;
                 // Aurora xuyong 2014-04-25 modified for aurora's new feature end
                }
                // Aurora liugj 2014-01-07 modified for allcheck animation end
                
                public void bindToAdapter(GnSelectionManager<Long> selectionManager) {
                    // TODO Auto-generated method stub
                    mListAdapter.setSelectionManager(selectionManager);
                    // Aurora xuyong 2014-06-11 added for bug #5592 start
                    if (selectionManager != null) {
                        selectionManager.setAdapter(mListAdapter);
                    }
                    // Aurora xuyong 2014-06-11 added for bug #5592 end
                }
            };
          //Aurora xuyong 2014-07-04 added for reject feature start
            mActionBatchHandler.setHandler(mHandler);
          //Aurora xuyong 2014-07-04 added for reject feature end
        }
    }
    // Aurora xuyong 2014-03-21 modified for aurora's new feature start
    AuroraActionBar mAuroraActionBar;
    
    private void initActionBatchMode() {
         // Aurora xuyong 2014-11-12 modified for bug #9759 start
         AuroraActivity activity = AuroraConvListActivity.sAuroraConvListActivity;
         // Aurora xuyong 2014-11-12 modified for bug #9759 end
         if (activity == null) {
             return;
         }
         mAuroraActionBar = activity.getAuroraActionBar();
         activity.setAuroraMenuCallBack(mAuroraMenuCallBack); //必须写在布局前
         int menuId = R.menu.aurora_list_menu_delete;
         mAuroraActionBar.initActionBottomBarMenu(menuId, 1);
         mAuroraActionBar.showActionBarDashBoard();
//         actionBar.showActionBottomeBarMenu();
         mAuroraMenu = mAuroraActionBar.getActionBarMenu();
         mBottomAuroraMenu = mAuroraActionBar.getAuroraActionBottomBarMenu();
    // Aurora xuyong 2014-03-21 modified for aurora's new feature end
         initActionBatchHandler();
         mAuroraMenu.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                //Log.e("liugj", "------onDismiss-----");
                // Aurora liugj 2013-10-11 modified for aurora's new feature start
                // Aurora liugj 2013-11-18 modified for aurora's new feature start
                // Aurora xuyong 2014-02-24 modified for bug #2580 start
                onBottomMenuDismiss();
                // Aurora xuyong 2014-02-24 modified for bug #2580 end
            }
         });
    }
    // Aurora xuyong 2014-02-24 added for bug #2580 start
    private static boolean mNeedShowDialog = true;
    // Aurora xuyong 2014-02-24 added for bug #2580 end
    private OnAuroraMenuItemClickListener mAuroraMenuCallBack = new OnAuroraMenuItemClickListener() {

        @Override
        public void auroraMenuItemClick(int itemId) {
            switch (itemId) {
            case R.id.aurora_list_menu_del:
                // Aurora xuyong 2014-02-24 added for bug #2580 start
                if (!mNeedShowDialog) {
                    return;
                }
                mNeedShowDialog = false;
                // Aurora xuyong 2014-02-24 added for bug #2580 end
                mAllSelectedThreadIds.clear();
                mAllSelectedThreadIds = (ArrayList<Long>)mActionBatchHandler.getSelected().clone();
                deleteConversations();
                break;
            default:
                break;
            }
        }
    };    
    // Aurora liugj 2013-09-24 added for aurora's new feature end

    private final OnCreateContextMenuListener mConvListOnCreateContextMenuListener = 
        new OnCreateContextMenuListener() {

            public void onCreateContextMenu(ContextMenu menu, View v,
                    ContextMenuInfo menuInfo) {
                // TODO Auto-generated method stub
                    // Aurora liugj 2013-09-24 modified for aurora's new feature start
                if (null != mActionBatchHandler) {
                    // Aurora liugj 2013-09-24 modified for aurora's new feature end
                    return;
                }
                Cursor cursor = mListAdapter.getCursor();
                // Aurora xuyong 2014-07-17 modified for bug #6619 start
                if (cursor == null || cursor.isClosed() || cursor.getPosition() < 0) {
                // Aurora xuyong 2014-07-17 modified for bug #6619 end
                    return;
                }

                Conversation conv = Conversation.from(AuroraConvListActivity.sContext, cursor);
                mThreadID = conv.getThreadId();
                mType = conv.getType();
                mHasEncryption = conv.getEncryption();
                if (conv != null && conv.getRecipients() != null
                        && conv.getRecipients().size() > 0) {
                    mContact = conv.getRecipients().get(0);
                }

                ContactList recipients = conv.getRecipients();
                menu.setHeaderTitle(recipients.formatNames(","));

                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

                menu.add(0, MENU_DELETE, 0, R.string.menu_delete);
                if (mHasEncryption) {
                    menu.add(0, MENU_DECRYPTION, 0, R.string.menu_decryption);
                } else {
                    menu.add(0, MENU_ENCRYPTION, 0, R.string.menu_encryption);
                }
                //Gionee <zhouyj> <2013-04-26> add for CR00802651 start
                if (MmsApp.mGnAddContactSupport) {
                    if (recipients.size() == 1) {
                        Contact contact = recipients.get(0);
                        if (contact.existsInDatabase()) {
                            menu.add(0, MENU_VIEW_CONTACT, 0, R.string.gn_contact_details);
                        } else {
                            menu.add(0, MENU_NEW_CONTACT, 0, R.string.gn_new_contact);
                            menu.add(0, MENU_ADD_CONTACT, 0, R.string.gn_existing_contact);
                        }
                    }
                }
                //Gionee <zhouyj> <2013-04-26> add for CR00802651 end
                return;
            }
        };

    public boolean onContextItemSelected(MenuItem item) {
        Cursor cursor = mListAdapter.getCursor();
        // Aurora xuyong 2014-07-17 modified for bug #6619 start
        if (cursor != null && !cursor.isClosed() && cursor.getPosition() >= 0) {
        // Aurora xuyong 2014-07-17 modified for bug #6619 end
            switch (item.getItemId()) {
            case MENU_DELETE: {
                if (!isEncryptionList && mHasEncryption) {
                    final Intent intent = new Intent();
                    intent.setClass(AuroraConvListActivity.sContext, MsgChooseLockPassword.class);
                    intent.putExtra("isdecryption", true);
                    this.startActivityForResult(intent,
                            ONEDEL_CONFIRM_PASSWORD_REQUEST);
                } else {
                    startQueryStaredMsg(mThreadID);
                }
                break;
            }
            case MENU_ENCRYPTION:
                if (Conversation.getFirstEncryption() == true) {
                    inputencryption(ONE_CONTEXT_PASSWORD_REQUEST);
                } else {
                    //Gionee <gaoj> <2013-05-28> add for CR00817770 begin
                    if (MmsApp.mGnHideEncryption && MmsApp.mGnSmartGuide && !ReadPopTag(AuroraConvListActivity.sContext, FIRSTENCRYPTION)) {
                        WritePopTag(AuroraConvListActivity.sContext, FIRSTENCRYPTION);
                    }
                    //Gionee <gaoj> <2013-05-28> add for CR00817770 end
                    Uri uri = Uri.parse("content://mms-sms/encryption/" + mThreadID);
                    Conversation conv = Conversation.from(AuroraConvListActivity.sContext, cursor);
                    int update = conv.updatethreads(AuroraConvListActivity.sContext, uri, false);
                    if (update > 0) {
                        encryptiontoast(mHasEncryption);
                        conv.updateNotification();
                    }
                }
                break;
            case MENU_DECRYPTION:
                if (isEncryptionList) {
                    Uri uri = Uri.parse("content://mms-sms/encryption/" + mThreadID);
                    Conversation conv = Conversation.get(AuroraConvListActivity.sContext,
                            mThreadID, false);
                    int update = conv.updatethreads(AuroraConvListActivity.sContext, uri,
                            true);
                    if (update > 0) {
                        encryptiontoast(true);
                        conv.updateNotification();
                    }
                } else {
                    inputdecryption(ONE_CONTEXT_PASSWORD_REQUEST);
                }
                break;
                //Gionee <zhouyj> <2013-04-26> add for CR00802651 start
            case MENU_VIEW_CONTACT:
                {
                    Conversation conv = Conversation.from(AuroraConvListActivity.sContext, cursor);
                    ContactList recipients = conv.getRecipients();
                    if (recipients.size() == 1) {
                        Contact contact = recipients.get(0);
                        Intent viewIntent = new Intent(Intent.ACTION_VIEW, contact.getUri());
                        viewIntent.setComponent(new ComponentName("com.android.contacts",
                        "com.android.contacts.activities.ContactDetailActivity"));
                        viewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(viewIntent);
                    }
                }
                break;
            case MENU_NEW_CONTACT:
                {
                    Conversation conv = Conversation.from(AuroraConvListActivity.sContext, cursor);
                    ContactList recipients = conv.getRecipients();
                    if (recipients.size() == 1) {
                        Contact contact = recipients.get(0);
                        Intent intent = new Intent(Intent.ACTION_INSERT,Contacts.CONTENT_URI);
                        intent.setComponent(new ComponentName("com.android.contacts",
                            "com.android.contacts.activities.ContactEditorActivity"));
                        if (Mms.isEmailAddress(contact.getNumber())) {
                            intent.putExtra(Insert.EMAIL, contact.getNumber());
                        } else {
                            intent.putExtra(Insert.PHONE, contact.getNumber());
                        }
                        startActivity(intent);
                    }
                }
                break;
            case MENU_ADD_CONTACT:
                {
                    Conversation conv = Conversation.from(AuroraConvListActivity.sContext, cursor);
                    ContactList recipients = conv.getRecipients();
                    if (recipients.size() == 1) {
                        Contact contact = recipients.get(0);
                        Intent newintent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                        newintent.setComponent(new ComponentName("com.android.contacts",
                            "com.android.contacts.activities.ContactSelectionActivity"));
                        if (Mms.isEmailAddress(contact.getNumber())) {
                            newintent.putExtra(Insert.EMAIL, contact.getNumber());
                        } else {
                            newintent.putExtra(Insert.PHONE, contact.getNumber());
                        }
                        newintent.setType(People.CONTENT_ITEM_TYPE);
                        startActivity(newintent);
                    }
                }
                break;
                //Gionee <zhouyj> <2013-04-26> add for CR00802651 end
            default:
                break;
            }
        }
        return super.onContextItemSelected(item);
    };
    //gionee gaoj added for CR00725602 20121201 end
    
    /*public void deleteSingleConvasation(long threadId) {
        if (!isEncryptionList && mHasEncryption) {
            final Intent intent = new Intent();
            intent.setClass(AuroraConvListActivity.sContext, MsgChooseLockPassword.class);
            intent.putExtra("isdecryption", true);
            this.startActivityForResult(intent,
                    ONEDEL_CONFIRM_PASSWORD_REQUEST);
        } else {
            startQueryStaredMsg(threadId);
        }
    }*/
    
    private void openThread(long threadId, int type) {
        if(FeatureOption.MTK_WAPPUSH_SUPPORT == true){
            //wappush: add opptunities for starting wappush activity if it is a wappush thread 
            //type: Threads.COMMON_THREAD, Threads.BROADCAST_THREAD and Threads.WAP_PUSH
            // Aurora xuyong 2013-11-15 deleted for bug #752 start
            /*if(type == Threads.WAPPUSH_THREAD){
                startActivity(WPMessageActivity.createIntent(getActivity(), threadId));
            } else if (type == Threads.CELL_BROADCAST_THREAD) {
                startActivity(CBMessageListActivity.createIntent(getActivity(), threadId));
            } else {*/
            // Aurora xuyong 2013-11-15 deleted for bug #752 end
                // Aurora xuyong 2014-11-12 modified for bug #9759 start
                Context context = AuroraConvListActivity.sContext;
                // Aurora xuyong 2014-11-12 modified for bug #9759 end
                if (context != null) {
                    // Aurora xuyong 2014-08-14 modified for aurora's new feature start
                    startActivity(ComposeMessageActivity.createIntent(context, threadId).putExtra("quick_query", true));
                    // Aurora xuyong 2014-08-14 modified for aurora's new feature end
                }
            // Aurora xuyong 2013-11-15 deleted for bug #752 start
            //}
            // Aurora xuyong 2013-11-15 deleted for bug #752 end
        }else{
            // Aurora xuyong 2013-11-15 deleted for bug #752 start
            /*if (type == Threads.CELL_BROADCAST_THREAD) {
                startActivity(CBMessageListActivity.createIntent(getActivity(), threadId));
            } else {*/
            // Aurora xuyong 2013-11-15 deleted for bug #752 end
                // Aurora xuyong 2014-11-12 modified for bug #9759 start
                Context context = AuroraConvListActivity.sContext;
                // Aurora xuyong 2014-11-12 modified for bug #9759 end
                if (context != null) {
                    // Aurora xuyong 2014-08-14 modified for aurora's new feature start
                    startActivity(ComposeMessageActivity.createIntent(context, threadId).putExtra("quick_query", true));
                    // Aurora xuyong 2014-08-14 modified for aurora's new feature end
                }
            // Aurora xuyong 2013-11-15 deleted for bug #752 start
            //}
            // Aurora xuyong 2013-11-15 deleted for bug #752 end
        }
    }
    // Aurora xuyong 2014-10-04 added for bug #9597 start
    private void openThread(long threadId, int type, long privacy) {
        Intent threadIntent = null;
        // Aurora xuyong 2014-11-12 modified for bug #9759 start
        Context context = AuroraConvListActivity.sContext;
        // Aurora xuyong 2014-11-12 modified for bug #9759 end
        if (context != null) {
            threadIntent = ComposeMessageActivity.createIntent(context, threadId).putExtra("quick_query", true);
            if (threadIntent != null) {
                threadIntent.putExtra("is_privacy", privacy);
                startActivity(threadIntent);
            }
        }
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
                // Aurora xuyong 2014-04-21 added for bug #4460 start
                if (mListView != null) {
                        mListView.auroraSetNeedSlideDelete(false);
                }
                // Aurora xuyong 2014-04-21 added for bug #4460 end
                Conversation.asyncDeleteObsoleteThreads(mQueryHandler,
                        DELETE_OBSOLETE_THREADS_TOKEN);
            }
              // Aurora liugj 2014-01-06 added for bath-delete optimize start
            backDeleteThead();
              // Aurora liugj 2014-01-06 added for bath-delete optimize end
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
            //dialog.setProgressStyle(AuroraProgressDialog.STYLE_SPINNER);
            dialog.setMessage(context.getString(R.string.deleting));
            dialog.setMax(1); /* default is one complete */
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            return dialog;
        }
    }

    private void startQueryLockedAndStaredMsg(long threadId) {
       // Aurora xuyong 2014-04-21 added for bug #4460 start
        if (mListView != null) {
            mListView.auroraSetNeedSlideDelete(false);
        }
       // Aurora xuyong 2014-04-21 added for bug #4460 end
        Conversation.startQueryHaveLockedMessages(mQueryHandler, threadId,
                THREAD_LIST_LOCKED_QUERY_TOKEN);
        Conversation.startQueryHaveStarMessages(mQueryHandler, threadId,
                THREAD_LIST_FAVORITE_QUERY_TOKEN);
    }

    //gionee gaoj 2012-10-15 modified for CR00705539 start
    //gionee gaoj 2012-9-20 added for CR00699291 start
    public static void startQueryStaredMsg(long threadId) {
        int tag;
        if (threadId == -1) {
            tag = THREAD_LIST_FAVORITE_QUERY_ALL_TOKEN;
        } else {
            tag = THREAD_LIST_FAVORITE_QUERY_ONE_TOKEN;
        }
        // Aurora xuyong 2014-04-21 added for bug #4460 start
        if (mListView != null) {
            mListView.auroraSetNeedSlideDelete(false);
          }
        // Aurora xuyong 2014-04-21 added for bug #4460 end
        Log.e("liugj", (mQueryHandler == null)+"=======startQueryStaredMsg_tag======="+threadId);
        Conversation.startQueryHaveStarMessages(mQueryHandler, threadId,
                tag);
    }
    //gionee gaoj 2012-9-20 added for CR00699291 end
    //gionee gaoj 2012-10-15 modified for CR00705539 end

    public static class GnDeleteThreadListener implements OnClickListener {
        private long mThreadId;
        private final AsyncQueryHandler mHandler;
        private final Context mContext;
        private boolean mDeleteStaredMessages;
          // Aurora liugj 2014-01-22 modified for listview delete animation start
        private AuroraListView listView;

        public GnDeleteThreadListener(AuroraListView listView, long threadId, AsyncQueryHandler handler, Context context) {
            mThreadId = threadId;
            mHandler = handler;
            mContext = context;
            this.listView = listView;
        }
          // Aurora liugj 2014-01-22 modified for listview delete animation end

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
            // Aurora xuyong 2015-02-12 added for repoter feature start
            new TotalCount(mContext, TotalCount.MMS_MODULE_kEY, TotalCount.MSG_DEL_RP, 1).countData();
            // Aurora xuyong 2015-02-12 added for repoter feature end
            // Aurora liugj 2013-10-25 modified for aurora's new feature start
            /*// Aurora liugj 2013-09-24 modified for aurora's new feature start
            if(whichButton == -1 && mActionBatchHandler != null) {
                mActionBatchHandler.leaveSelectionMode();
            // Aurora liugj 2013-09-24 modified for aurora's new feature end
            }*/
            // Aurora liugj 2013-10-25 modified for aurora's new feature end
            dialog.dismiss();
            // Aurora liugj 2014-01-22 modified for listview delete animation start
            auroraDeleteConversations(listView, mThreadId, mHandler, mContext, mDeleteStaredMessages);
            // Aurora liugj 2014-01-22 modified for listview delete animation end
        }
        
    }

    // Aurora liugj 2014-01-06 modified for bath-delete optimize start
    private static void showProgressDialog(Context context, AsyncQueryHandler handler) {
        if (handler instanceof BaseProgressQueryHandler) {
            ((BaseProgressQueryHandler) handler).setProgressDialog(
                    DeleteProgressDialogUtil.getProgressDialog(context));
            ((BaseProgressQueryHandler) handler).showProgressDialog();
        }
    }
     // Aurora liugj 2014-01-06 modified for bath-delete optimize end

     // Aurora liugj 2014-01-06 modified for bath-delete optimize start
     // Aurora liugj 2014-01-22 modified for listview delete animation start
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
                //showProgressDialog();
                int token = DELETE_CONVERSATION_TOKEN;
                // gionee zhouyj 2012-11-08 modify for CR00725666 start 
                
                mListChange = false;
                // gionee zhouyj 2012-08-08 add for CR00664390 end 
                // gionee zhouyj 2012-07-10 modify for CR00640610 start
                if (threadId == -1) {
                         showProgressDialog(context, handler);
                    //Gionee <zhouyj> <2013-06-13> modify for CR00789924 begin
                    if (MmsApp.mIsDraftOpen) {
                        // has not draft
                        Uri sAllThreadsUri = Threads.CONTENT_URI.buildUpon().appendQueryParameter("simple", "true").build();
                        Cursor c = SqliteWrapper.query(context, context.getContentResolver(), sAllThreadsUri,
                              ALL_THREADS_PROJECTION, "sim_id=-1 AND message_count=0", null, null);

                        if (c != null && c.getCount() == 0 && deleteStaredMessages /*== true*/) {
                            // has not draft,delete all. clear table more fast
                            Log.i("GnDeleteThreadListener", "delete all: no draft, no stared message");
                            // Aurora xuyong 2014-04-21 added for bug #4460 start
                            if (mListView != null) {
                                mListView.auroraSetNeedSlideDelete(false);
                              }
                            // Aurora xuyong 2014-04-21 added for bug #4460 end
                            Conversation.startDeleteAll(handler, token, true);
                            c.close();
                            DraftCache.getInstance().refresh();
                            return ;
                        }
                    } else {
                        if (deleteStaredMessages && !isEncryptionList && !mHideEncryp) {
                          // Aurora xuyong 2014-04-21 modified for bug #4460 start
                            if (mListView != null) {
                                mListView.auroraSetNeedSlideDelete(false);
                            }
                          // Aurora xuyong 2014-04-21 modified for bug #4460 end
                            Conversation.startDeleteAll(handler, token, true);
                            DraftCache.getInstance().refresh();
                            return ;
                        }
                    }
                    //Gionee <zhouyj> <2013-06-13> modify for CR00789924 end
                }else if (threadId == 9999 /*|| threadId == -1*/) { // multi delete
                        showProgressDialog(context, handler);
                    //gionee gaoj 2012-10-15 modified for CR00705539 start
                    String threadidString = null;
                    if (mThreadsList.size() == 1) {
                       // Aurora xuyong 2014-04-21 modified for bug #4460 start
                        if (mListView != null) {
                            mListView.auroraSetNeedSlideDelete(false);
                        }
                       // Aurora xuyong 2014-04-21 modified for bug #4460 end
                        Conversation.startDelete(handler, MUL_DELETE_CONVERSATIONS_TOKEN,
                                deleteStaredMessages, mThreadsList.get(0), "OneLast", true);
                    } else {
                        int size = mThreadsList.size();
                        for (int i = 0; i < size; i++) {
                            threadidString = mThreadsList.get(i);
                            // Aurora xuyong 2014-08-23 deleted for bug #7789 start
                            /*if (i > 0) {
                                threadidString = threadidString.substring(16, threadidString.length());
                            }*/
                            // Aurora xuyong 2014-08-23 deleted for bug #7789 end
                            if (i == size - 1) {
                             // Aurora xuyong 2014-04-21 added for bug #4460 start
                                if (mListView != null) {
                                    mListView.auroraSetNeedSlideDelete(false);
                                }
                             // Aurora xuyong 2014-04-21 added for bug #4460 end
                                Conversation.startDelete(handler, MUL_DELETE_CONVERSATIONS_TOKEN,
                                        deleteStaredMessages, threadidString, "MultLast", true);
                            } else {
                             // Aurora xuyong 2014-04-21 modified for bug #4460 start
                                if (mListView != null) {
                                    mListView.auroraSetNeedSlideDelete(false);
                                }
                             // Aurora xuyong 2014-04-21 modified for bug #4460 end
                                Conversation.startDelete(handler, DELETE_CONVERSATION_NOT_LAST_TOKEN,
                                        deleteStaredMessages, threadidString, null, true);
                            }
                        }
                    }
                    //gionee gaoj 2012-10-15 modified for CR00705539 end
                    DraftCache.getInstance().refresh();
                } else {
                // Aurora xuyong 2014-04-09 added for aurora's new feature start
                    mDeletedAQHandler = handler;
                    mDeletedToken = token;
                    mDeletedStaredMessages = deleteStaredMessages;
                    mDeletedThreadId = threadId;
                // Aurora xuyong 2014-04-09 added for aurora's new feature end
                    if (listView != null) {
                        listView.auroraDeleteSelectedItemAnim();
                    }
                // Aurora xuyong 2014-04-09 modified for aurora's new feature start
                    if (mNeedDeleteHereFlag) {
                       // Aurora xuyong 2014-04-21 added for bug #4460 start
                           if (mListView != null) {
                            mListView.auroraSetNeedSlideDelete(false);
                        }
                       // Aurora xuyong 2014-04-21 added for bug #4460 end
                        Conversation.startDeletestar(handler, token,
                                deleteStaredMessages, threadId);
                        DraftCache.getInstance().setDraftState(threadId, false);
                    }
                    mNeedDeleteHereFlag = true;
                // Aurora xuyong 2014-04-09 modified for aurora's new feature end
                }
                // gionee zhouyj 2012-11-08 modify for CR00725666 end 
            }

        });
      //Gionee <zhouyj> <2013-05-02> add for CR00802651 begin
        if (MmsApp.mGnDeleteRecordSupport) {
            handler.post(mDeleteRecordRunnable);
        }
        //Gionee <zhouyj> <2013-05-02> add for CR00802651 end
    }
    // Aurora xuyong 2014-04-09 added for aurora's new feature start
    static boolean mNeedSetDataChanged = true;
    static boolean mNeedDeleteHereFlag = true;
    static AsyncQueryHandler mDeletedAQHandler;
    static int mDeletedToken;
    static boolean mDeletedStaredMessages;
    static long mDeletedThreadId;
    // Aurora xuyong 2014-04-09 added for aurora's new feature end
     // Aurora liugj 2014-01-22 modified for listview delete animation end
     // Aurora liugj 2014-01-06 modified for bath-delete optimize end
    
    //gionee gaoj 2012-12-3 removed for CR00738791 start
    //gionee gaoj 2012-12-3 removed for CR00738791 end 
    
    //Gionee <zhouyj> <2013-05-02> add for CR00802651 begin
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
    //Gionee <zhouyj> <2013-05-02> add for CR00802651 end

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

    //Gionee <guoyx> <2013-06-09> modify for CR00824219 begin
    public static AuroraAlertDialog confirmDeleteGnThreadDialog(final GnDeleteThreadListener listener,
            boolean deleteAll,
            boolean hasFavoriteMessages,
            Context context) {
         // Aurora liugj 2014-01-23 modified for bug-1875 start
        /*View contents = View.inflate(context, R.layout.gn_delete_thread_dialog_view, null);
        TextView msg = (TextView) contents.findViewById(R.id.gn_message);
        // Aurora liugj 2014-01-06 modified for aurora's new feature start
        if (mAllSelectedThreadIds.size() == 1) {
            msg.setText(context.getString(R.string.confirm_delete_one_conversation));
        }else {
            msg.setText(deleteAll ? context.getString(R.string.confirm_delete_all_conversations) : context.getString(R.string.confirm_delete_conversations, mAllSelectedThreadIds.size()));
        }
        // Aurora liugj 2014-01-06 modified for aurora's new feature end

        final CheckBox starCheckbox = (CheckBox) contents.findViewById(R.id.gn_delete_favorite);

        //gionee gaoj added for CR00725602 20121201 start
            // Aurora liugj 2013-09-24 modified for aurora's new feature start
        if (deleteAll) {
            msg.setTextColor(Color.WHITE);
            starCheckbox.setTextColor(Color.WHITE);
        }
            // Aurora liugj 2013-09-24 modified for aurora's new feature end
        //gionee gaoj added for CR00725602 20121201 end

        if (!hasFavoriteMessages) {
            starCheckbox.setVisibility(View.GONE);
            listener.setDeleteStaredMessage(true);
        } else {
            listener.setDeleteStaredMessage(starCheckbox.isChecked());
            starCheckbox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    listener.setDeleteStaredMessage(starCheckbox.isChecked());
                }
            });
        }*/
        listener.setDeleteStaredMessage(true);
        String message = context.getString(R.string.confirm_delete_one_conversation);;
        if (mAllSelectedThreadIds.size() != 1) {
            message = deleteAll ? context.getString(R.string.confirm_delete_all_conversations) : context.getString(R.string.confirm_delete_selected_conversations);
        }
        //gionee gaoj added for CR00725602 20121201 start
        AuroraAlertDialog.Builder builder = null;
        /*if (deleteAll) {
            builder = new AuroraAlertDialog.Builder(context, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN);
        } else {*/
            builder = new AuroraAlertDialog.Builder(context);//, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN);
        //}
        //gionee gaoj added for CR00725602 20121201 end
        mDelAllDialog = builder.setTitle(R.string.confirm_dialog_title)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        // TODO Auto-generated method stub
                        mNeedShowDialog = true;
                    }
                })
                .setMessage(message)
                .setCancelable(true).setPositiveButton(R.string.OK, listener)
                .setNegativeButton(R.string.no, null)/*.setView(contents)*/.show();
        // Aurora liugj 2014-01-23 modified for bug-1875 end

        return mDelAllDialog;
    }
    //Gionee <guoyx> <2013-06-09> modify for CR00824219 end

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
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        switch (requestCode) {
        //gionee gaoj added for CR00725602 20121201 start
        case ONEDEL_CONFIRM_PASSWORD_REQUEST:
            if (data != null && data.getAction().equals("confirm")) {
                startQueryStaredMsg(mThreadID);
            }
            break;
        case ONE_CONTEXT_PASSWORD_REQUEST:
            if (data != null
                    && (data.getAction().equals("succeed") || data.getAction()
                            .equals("confirm"))) {
                Uri uri = Uri.parse("content://mms-sms/encryption/" + mThreadID);

                Conversation conv = Conversation.get(AuroraConvListActivity.sContext,
                        mThreadID, false);

                //Gionee <gaoj> <2013-05-28> add for CR00817770 begin
                if (MmsApp.mGnHideEncryption && MmsApp.mGnSmartGuide && data != null && data.getAction().equals("succeed")
                        && !ReadPopTag(AuroraConvListActivity.sContext, FIRSTENCRYPTION)) {
                    WritePopTag(AuroraConvListActivity.sContext, FIRSTENCRYPTION);
                }
                //Gionee <gaoj> <2013-05-28> add for CR00817770 end
                Conversation.setFirstEncryption(false);
                int update = conv.updatethreads(AuroraConvListActivity.sContext, uri,
                        mHasEncryption);
                if (update > 0) {
                    encryptiontoast(mHasEncryption);
                    conv.updateNotification();
                }
            }
            break;
        //gionee gaoj added for CR00725602 20121201 end
        case UPDATE_PASSWORD_REQUEST:
            if (data != null && data.getAction().equals("confirm")) {
                gnDeleteConversations();
            } else if (data != null && data.getAction().equals("succeed")) {
                isEncryptionList = true;
                startEncryptionQuery();
                    // Aurora liugj 2013-09-24 modified for aurora's new feature start
                if(null != mActionBatchHandler) {
                    mActionBatchHandler.leaveSelectionMode();
                    // Aurora liugj 2013-09-24 modified for aurora's new feature end
                }
                //Gionee <gaoj> <2013-05-28> add for CR00817770 begin
                if (MmsApp.mGnHideEncryption && MmsApp.mGnSmartGuide && !ReadPopTag(AuroraConvListActivity.sContext, FIRSTENCRYPTION)) {
                    WritePopTag(AuroraConvListActivity.sContext, FIRSTENCRYPTION);
                }
                //Gionee <gaoj> <2013-05-28> add for CR00817770 end
                Conversation.setFirstEncryption(false);
                // Aurora xuyong 2014-11-26 modified for bug #10049 start
                Toast.makeText(AuroraConvListActivity.sContext, R.string.pref_summary_mms_password_setting_succeed, Toast.LENGTH_SHORT).show();
                // Aurora xuyong 2014-11-26 modified for bug #10049 end
            }
            break;
        case CONFIRM_PASSWORD_REQUEST:
            if (data != null && data.getAction().equals("confirm")) {
                isEncryptionList = true;
                startEncryptionQuery();
            }
            break;
        case MULTI_PASSWORD_REQUEST:
            if (data != null && data.getAction().equals("succeed")) {
                //Gionee <gaoj> <2013-05-28> add for CR00817770 begin
                if (MmsApp.mGnHideEncryption && MmsApp.mGnSmartGuide && !ReadPopTag(AuroraConvListActivity.sContext, FIRSTENCRYPTION)) {
                    WritePopTag(AuroraConvListActivity.sContext, FIRSTENCRYPTION);
                }
                //Gionee <gaoj> <2013-05-28> add for CR00817770 end
                Conversation.setFirstEncryption(false);
                encryptSelectsConv(true);
                Toast.makeText(AuroraConvListActivity.sContext, AuroraConvListActivity.sContext.getString(R.string.gn_multi_encryption), Toast.LENGTH_SHORT).show();
                    // Aurora liugj 2013-09-24 modified for aurora's new feature start
                if(null != mActionBatchHandler) {
                    mActionBatchHandler.leaveSelectionMode();
                    // Aurora liugj 2013-09-24 modified for aurora's new feature end
                }
            }
            break;
        case CONFIRM_DECRYPTION_PASSWORD_REQUEST:
            if (data != null && data.getAction().equals("confirm")) {
                encryptSelectsConv(false);
                Toast.makeText(AuroraConvListActivity.sContext, AuroraConvListActivity.sContext.getString(R.string.gn_multi_dncryption), Toast.LENGTH_SHORT).show();
                    // Aurora liugj 2013-09-24 modified for aurora's new feature start
                if(null != mActionBatchHandler) {
                    mActionBatchHandler.leaveSelectionMode();
                    // Aurora liugj 2013-09-24 modified for aurora's new feature end
                }
            }
            break;
        default:
            break;
    }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private MenuItem mMenuBatchOperationItem;
    private MenuItem mMenuChangeEncryptionItem;
    private MenuItem mMenuCancelAllFavoriteItem;
    // gionee zhouyj 2012-05-16 modify for CR00601094 start
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub

        //gionee gaoj 2012-8-7 added for CR00671408 start
        if(!MmsApp.sIsExchangeExist) {
            menu.removeItem(R.id.gn_action_exchange);
        }
        if (!MmsApp.isOpenApi()) {
            menu.removeItem(R.id.gn_action_doctoran);
        }
        //gionee gaoj 2012-8-7 added for CR00671408 end

        // check mms import & export
        if(!AuroraConvListActivity.checkMsgImportExportSms()) {
            menu.removeItem(R.id.gn_action_in_out);
        }
        mMenuCancelAllFavoriteItem = menu.findItem(R.id.gn_action_cancel_all_favorite);
        if (mMenuCancelAllFavoriteItem != null) {
            mMenuCancelAllFavoriteItem.setVisible(false);
        }
        // gionee zhouyj 2013-03-25 modify for CR00789172 start
        //gionee gaoj added for CR00725602 20121201 start
        mMenuChangeEncryptionItem = menu.findItem(R.id.gn_action_encryption);
        if (mMenuChangeEncryptionItem != null) {
            mMenuChangeEncryptionItem.setVisible(true);
            mMenuChangeEncryptionItem.setTitle(isEncryptionList ? R.string.gn_action_all : R.string.gn_action_encryption);
            if ((!isEncryptionList && mListAdapter.isEmpty()) || MmsApp.mIsSafeModeSupport) {
                mMenuChangeEncryptionItem.setEnabled(false);
            } else {
                mMenuChangeEncryptionItem.setEnabled(true);
            }
        }
        //gionee gaoj added for CR00725602 20121201 end
        mMenuBatchOperationItem = menu.findItem(R.id.gn_action_batch_operation);
        if (mMenuBatchOperationItem != null) {
            mMenuBatchOperationItem.setVisible(true);
            if (MmsApp.mIsSafeModeSupport || mListAdapter.isEmpty()) {
                mMenuBatchOperationItem.setEnabled(false);
            } else {
                mMenuBatchOperationItem.setEnabled(true);
            }
        }
        // gionee zhouyj 2013-03-25 modify for CR00789172 end
        super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch(item.getItemId()) {
        case R.id.gn_action_delete_all:
            initDeleteThreadIds();
            startQueryLockedAndStaredMsg(-1);
            break;
            //gionee gaoj added for CR00725602 20121201 start
        case R.id.gn_action_batch_operation:
            if(null == mActionModeHandler) {
                initThreadsMap();
                initActionModeHandler();
                mActionModeHandler.enterSelectionMode(false, null);
                return true;
            }
            break;
        case R.id.gn_action_encryption:
            if (isEncryptionList) {
                isEncryptionList = false;
                //Gionee <gaoj> <2013-05-28> add for CR00817770 begin
                if (MmsApp.mGnHideEncryption) {
                    startQuery();
                } else {
                    //Gionee <gaoj> <2013-05-28> add for CR00817770 end
                startAsyncQuery();
                //Gionee <gaoj> <2013-05-28> add for CR00817770 begin
                }
                //Gionee <gaoj> <2013-05-28> add for CR00817770 end
            } else {
                if (Conversation.getFirstEncryption() == true) {
                    inputencryption(UPDATE_PASSWORD_REQUEST);
                } else {
                    // Aurora xuyong 2014-11-12 modified for bug #9759 start
                    Context context = AuroraConvListActivity.sContext;
                    // Aurora xuyong 2014-11-12 modified for bug #9759 end
                    if (context != null) {
                        final Intent intent = new Intent();
                        intent.setClass(context, MsgChooseLockPassword.class);
                        intent.putExtra("isdecryption", true);
                        startActivityForResult(intent,
                                ConvFragment.CONFIRM_PASSWORD_REQUEST);
                    }
                }
            }
            break;
            //gionee gaoj added for CR00725602 20121201 end
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void initDeleteThreadIds() {
        /*if(mAllSelectedThreadIds == null) {
            mAllSelectedThreadIds =new HashSet<Long>();
        } else {
            mAllSelectedThreadIds.clear();
        }
        for(int i = 0; i < mListView.getCount(); i++) {
            mAllSelectedThreadIds.add(mThreadsMap.get(i));
        }*/
    }
    // gionee zhouyj 2012-05-16 modify for CR00601094 end

    //gionee gaoj 2012-6-14 added for CR00623396 start
    private static final int MIN_PASSWORD_LENGTH = 4;
    public void startEncryptionQuery() {
        try {
          // Aurora xuyong 2014-04-21 added for bug #4460 start
            if (mListView != null) {
                mListView.auroraSetNeedSlideDelete(false);
            }
          // Aurora xuyong 2014-04-21 added for bug #4460 end
            Conversation.startQueryForEncryption(mQueryHandler, THREAD_LIST_QUERY_TOKEN, true);
        } catch (SQLiteException e) {
            // Aurora xuyong 2014-11-12 modified for bug #9759 start
            Context context = AuroraConvListActivity.sContext;
            // Aurora xuyong 2014-11-12 modified for bug #9759 end
            if (context != null) {
                SqliteWrapper.checkSQLiteException(context, e);
            }
        }
    }

    //gionee gaoj added for CR00725602 20121201 start
    private void encryptiontoast(boolean hasencryption) {
        final int resId = hasencryption ? R.string.gn_confirm_dncryption
                : R.string.gn_confirm_encryption;
        Toast.makeText(AuroraConvListActivity.sContext, resId, Toast.LENGTH_SHORT).show();
    }

    private void inputdecryption(int tag){
        final Intent intent = new Intent();
        intent.setClass(AuroraConvListActivity.sContext, MsgChooseLockPassword.class);
        intent.putExtra("isdecryption", true);
        this.startActivityForResult(intent, tag);
    }
    //gionee gaoj added for CR00725602 20121201 end

    private void inputencryption(int tag) {
        // Aurora xuyong 2014-11-12 modified for bug #9759 start
        Context context = AuroraConvListActivity.sContext;
        // Aurora xuyong 2014-11-12 modified for bug #9759 end
        if (context != null) {
            DevicePolicyManager DPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            int quality = DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC;
            int minQuality = DPM.getPasswordQuality(null);
            if (quality < minQuality) {
                quality = minQuality;
            }
            if (quality >= DevicePolicyManager.PASSWORD_QUALITY_NUMERIC) {
                int minLength = DPM.getPasswordMinimumLength(null);
                if (minLength < MIN_PASSWORD_LENGTH) {
                    minLength = MIN_PASSWORD_LENGTH;
                }
                final int maxLength = DPM.getPasswordMaximumLength(quality);
                Intent intent = new Intent(context,
                        MsgChooseLockPassword.class);
                intent.putExtra(LockPatternUtils.PASSWORD_TYPE_KEY, quality);
                intent.putExtra(MsgChooseLockPassword.PASSWORD_MIN_KEY, minLength);
                intent.putExtra(MsgChooseLockPassword.PASSWORD_MAX_KEY, maxLength);
                startActivityForResult(intent, tag);
            }
        }
    }
    //gionee gaoj 2012-6-14 added for CR00623396 end
    
    // gionee zhouyj 2012-06-15 add for CR00623385 start 
    private void encryptionConv(Uri uri, boolean encryption) {
        ContentResolver resolver = AuroraConvListActivity.sContext.getContentResolver();
        ContentValues values = new ContentValues(1);
        values.put("encryption", encryption? 1 : 0);
        String selection = "";
        selection = encryption? "encryption = 0":"encryption = 1";
        resolver.update(uri, values, selection, null);
    }
    // gionee zhouyj 2012-06-15 add for CR00623385 end 
    
    // gionee zhouyj 2012-06-21 add for CR00625679 start 
    private void encryptSelectsConv(boolean encrypt) {
        // gionee zhouyj 2013-03-14 modidfy for CR00783435 start
        final boolean b = encrypt;
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                long threadId;
                String ids = "";
                StringBuilder buf = new StringBuilder();
                int i = 0;
                Iterator<Long> it = mAllSelectedThreadIds.iterator();
                while(it.hasNext()) {
                    threadId = it.next();
                    if(i++ > 0) {
                        buf.append(" OR _id = ");
                    }
                    buf.append(Long.toString(threadId));
                    if (i > BatchDeleteNum) {
                        ids = buf.toString();
                        Uri uri = Uri.parse("content://mms-sms/encryption/" + ids);
                        encryptionConv(uri, b);
                        i = 0;
                        buf.delete(0, buf.length());
                    }
                }
                ids = buf.toString();
                Uri uri = Uri.parse("content://mms-sms/encryption/" + ids);
                encryptionConv(uri, b);
            }
        }).start();
        // gionee zhouyj 2013-03-14 add for CR00783435 end
    }
    // gionee zhouyj 2012-06-21 add for CR00625679 end 

    protected void initActionModeHandler() {
        String title = null;
        // Aurora xuyong 2014-11-12 modified for bug #9759 start
        Activity activity = AuroraConvListActivity.sAuroraConvListActivity;
        // Aurora xuyong 2014-11-12 modified for bug #9759 end
        if (activity != null) {
            mActionModeHandler = new GnActionModeHandler<Long>(activity, title, R.menu.aurora_conversation_multi_select_menu) {
    //            private MenuItem mEncryptItem = null;
                private MenuItem mDeleteItem = null;
                public void enterSelectionMode(boolean autoLeave, Long itemPressing) {
                    // Aurora liugj 2013-10-11 modified for aurora's new feature start
                 // Aurora xuyong 2014-04-21 modified for bug #4460 start
                    if (mListView != null) {
                        mListView.auroraSetNeedSlideDelete(false);
                    }
                 // Aurora xuyong 2014-04-21 modified for bug #4460 end
                    // Aurora liugj 2013-10-11 modified for aurora's new feature end
                    // gionee zhouyj 2012-09-04 modify for CR00686905 start
    //                int orientation = getActivity().getResources().getConfiguration().orientation;
                    /*if(orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    } else {
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                    }*/
                    // gionee zhouyj 2012-09-04 modify for CR00686905 end
                    // gionee zhouyj 2012-10-12 modify for CR00711214 start 
                    mListAdapter.showCheckBox(true);
                    // gionee zhouyj 2012-10-12 modify for CR00711214 end 
                    // gionee zhouyj 2012-10-26 add for CR00718476 start 
                    //sInMultiMode = true;
                    // gionee zhouyj 2012-10-26 add for CR00718476 end
    
                    //mListAdapter.notifyDataSetChanged();
                    super.enterSelectionMode(autoLeave, itemPressing);
                };
                
                public Set getDataSet() {
                    // TODO Auto-generated method stub
                    Set<Long> dataSet = new HashSet<Long>(mThreadsMap.size());
                    for(int i = 0; i < mThreadsMap.size(); i++)
                        dataSet.add(mThreadsMap.get(i));
                    return dataSet;
                }
                
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    super.onPrepareActionMode(mode, menu);
                    mDeleteItem = menu.findItem(R.id.delete);
    //                mEncryptItem = menu.findItem(R.id.encryption).setVisible(true);
                    if (mActionModeHandler.getSelected().isEmpty()) {
    //                    mEncryptItem.setEnabled(false);
                        mDeleteItem.setEnabled(false);
                        /*if (MmsApp.mDarkStyle) {
    //                        mEncryptItem.setIcon(R.drawable.gn_conv_dncryption_unuse);
                            mDeleteItem.setIcon(R.drawable.gn_com_delete_unuse_dark_bg);
                        }*/
                    } else {
    //                    mEncryptItem.setEnabled(true);
                        mDeleteItem.setEnabled(true);
                        /*if (MmsApp.mDarkStyle) {
    //                        mEncryptItem.setIcon(R.drawable.gn_conv_encryption_light_bg);
                            mDeleteItem.setIcon(R.drawable.gn_com_delete_dark_bg);
                        }*/
                    }
                    //Gionee tianxiaolong 2012.8.29 add for CR00682320 begin
                    /*if(gnFlyFlag){
                        mEncryptItem.setVisible(false);
                    }*/
                    //Gionee tianxiaolong 2012.8.29 add for CR00682320 end
                    
                    /*if(isEncryptionList) {
                        mBeingEncrypt = false;
                        mEncryptItem.setTitle(R.string.menu_decryption);
                        if (MmsApp.mDarkStyle) {
                            mEncryptItem.setIcon(R.drawable.gn_conv_dncryption_unuse);
                        } else {
                            mEncryptItem.setIcon(R.drawable.gn_conv_dncryption_bg);
                        }
                    }*/
                    return true;
                };
                
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    mAllSelectedThreadIds.clear();
                    mAllSelectedThreadIds = (ArrayList<Long>)mActionModeHandler.getSelected().clone();
                    switch (item.getItemId()) {
                    case R.id.delete:
                        deleteConversations();
                        break;
                    case R.id.encryption:
                        // gionee zhouyj 2012-10-22 modify for CR00692847 start 
                        encryptConversations();
                        leaveSelectionMode();
                        // gionee zhouyj 2012-10-22 modify for CR00692847 end 
                        return true;
                    default:
                        break;
                    }
                    return true;
                }
                
                public void updateUi() {
                    // TODO Auto-generated method stub
                    //mListAdapter.notifyDataSetChanged();
                    /*if(!isEncryptionList) {
                        mBeingEncrypt = false;
                        ArrayList<Long> arrayList = getSelected();
                        if(null != arrayList) {
                            Iterator it = arrayList.iterator();
                            while(it.hasNext()) {
                                if(!mSetEncryption.contains(it.next())) {
                                    mBeingEncrypt = true;
                                    break;
                                }
                            }
                        }
                        if(null != mEncryptItem) {
                            mEncryptItem.setTitle(mBeingEncrypt ? R.string.menu_encryption : R.string.menu_decryption);
                            if (MmsApp.mDarkStyle) {
                                mEncryptItem.setIcon (mBeingEncrypt ? R.drawable.gn_conv_encryption_light_bg : R.drawable.gn_conv_dncryption_light_bg);
                            } else {
                                mEncryptItem.setIcon (mBeingEncrypt ? R.drawable.gn_conv_encryption_bg : R.drawable.gn_conv_dncryption_bg);
                            }
                        }
                    } else {
                        if (getSelected().size() > 0) {
                            if (MmsApp.mDarkStyle) {
                                mEncryptItem.setIcon(R.drawable.gn_conv_dncryption_light_bg);
                            } else {
                                mEncryptItem.setIcon(R.drawable.gn_conv_dncryption);
                            }
                        }
                    }*/
                    mSelectCount = null != getSelected() ? getSelected().size() : 0;
                    /*if (null != mEncryptItem) {
                        mEncryptItem.setEnabled(mSelectCount == 0 ? false : true);
                        if (mSelectCount == 0) {
                            if (MmsApp.mDarkStyle) {
                                mEncryptItem.setIcon(R.drawable.gn_conv_dncryption_unuse);
                            } else {
                                mEncryptItem.setIcon(R.drawable.gn_conv_encryption_unuse);
                            }
                        }
                    }*/
                    if (null != mDeleteItem) {
                        /*if (MmsApp.mDarkStyle) {
                            if (mSelectCount == 0) {
                                mDeleteItem.setIcon(R.drawable.gn_com_delete_unuse_dark_bg);
                            } else {
                                mDeleteItem.setIcon(R.drawable.gn_com_delete_dark_bg);
                            }
                        }*/
                        mDeleteItem.setEnabled(mSelectCount == 0 ? false : true);
                    }
    //                ((AuroraActivity)getActivity()).getActionBar().updateActionMode();
                }
                
                public void bindToAdapter(GnSelectionManager<Long> selectionManager) {
                    // TODO Auto-generated method stub
                    mListAdapter.setSelectionManager(selectionManager);
                }
                
                public void onDestroyActionMode(ActionMode mode) {
                    super.onDestroyActionMode(mode);
                    Log.d("liugj","========onDestroyActionMode=========");
                        // Aurora liugj 2013-10-11 modified for aurora's new feature start
                    mListView.auroraSetNeedSlideDelete(true);
                        // Aurora liugj 2013-10-11 modified for aurora's new feature end
                    // gionee zhouyj 2012-10-12 modify for CR00711214 start 
                    mListAdapter.showCheckBox(false);
                    // gionee zhouyj 2012-10-12 modify for CR00711214 end 
                    // gionee zhouyj 2012-10-26 modify for CR00718476 start 
                    /*if (!DraftFragment.isMultiMode()) {
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    }*/
                    //sInMultiMode = false;
                    // gionee zhouyj 2012-10-26 modify for CR00718476 start 
                    mActionModeHandler = null;
                    // gionee zhouyj 2012-08-11 add for CR00664390 start 
                    if(null != mDelAllDialog && !mDelAllDialog.isShowing()) {
                        mListChange = false;
                    }
                    // gionee zhouyj 2012-08-11 add for CR00664390 end 
                    mThreadsMap.clear();
                    mSetEncryption.clear();
                }
            };
        }
    }
    
    private void deleteConversations() {
        if(mAllSelectedThreadIds.size() > 0) {
            if (MmsApp.mEncryption && !isEncryptionList) {
                if (checkoutEncryption()) {
                    final Intent intent = new Intent();
                    // Aurora xuyong 2014-11-26 modified for bug #10049 start
                    intent.setClass(AuroraConvListActivity.sContext, MsgChooseLockPassword.class);
                    // Aurora xuyong 2014-11-26 modified for bug #10049 end
                    intent.putExtra("isdecryption", true);
                    startActivityForResult(intent,
                            ConvFragment.UPDATE_PASSWORD_REQUEST);
                    return ;
                }
            }
            gnDeleteConversations();
        }
    }
    
    private boolean encryptConversations(){
        if(!isEncryptionList) {
            if (Conversation.getFirstEncryption() == true) {
                inputencryption(MULTI_PASSWORD_REQUEST);
                return false;
            } else {
                if(mBeingEncrypt) {
                    //Gionee <gaoj> <2013-05-28> add for CR00817770 begin
                    if (MmsApp.mGnHideEncryption && MmsApp.mGnSmartGuide && !ReadPopTag(AuroraConvListActivity.sContext, FIRSTENCRYPTION)) {
                        WritePopTag(AuroraConvListActivity.sContext, FIRSTENCRYPTION);
                    }
                    //Gionee <gaoj> <2013-05-28> add for CR00817770 end
                    encryptSelectsConv(true);
                    Toast.makeText(AuroraConvListActivity.sContext, AuroraConvListActivity.sContext.getString(R.string.gn_multi_encryption), Toast.LENGTH_SHORT).show();
                } else {
                    final Intent intent = new Intent();
                    // Aurora xuyong 2014-11-26 modified for bug #10049 start
                    intent.setClass(AuroraConvListActivity.sContext, MsgChooseLockPassword.class);
                    // Aurora xuyong 2014-11-26 modified for bug #10049 end
                    intent.putExtra("isdecryption", true);
                    startActivityForResult(intent,
                            ConvFragment.CONFIRM_DECRYPTION_PASSWORD_REQUEST);
                    return false;
                }
            }
        } else {
            encryptSelectsConv(false);
            Toast.makeText(AuroraConvListActivity.sContext, AuroraConvListActivity.sContext.getString(R.string.gn_multi_dncryption), Toast.LENGTH_SHORT).show();
        }
        return true;
    }
    
    public void leaveForChanged() {
        // Aurora liugj 2013-09-24 modified for aurora's new feature start
        if (null != mActionBatchHandler && mActionBatchHandler.isInSelectionMode()) {
            mActionBatchHandler.leaveSelectionMode();
        // Aurora liugj 2013-09-24 modified for aurora's new feature end
        }
    }

    //gionee gaoj 2012-9-20 added for CR00699291 start
    private boolean checkoutEncryption() {
        // TODO Auto-generated method stub
        for (long threadsid : mAllSelectedThreadIds) {
            if (mSetEncryption.contains(threadsid)) {
                return true;
            }
        }
        return false;
    }

    // gionee zhouyj 2012-11-08 modify for CR00725666 start 
    private void gnDeleteConversations() {
        // Aurora liugj 2013-11-08 added for aurora's new feature start
        /*if(mActionBatchHandler != null) {
            mActionBatchHandler.leaveSelectionMode();
        }*/
        // Aurora liugj 2013-11-08 added for aurora's new feature end
        int len = mAllSelectedThreadIds.size();
        // Aurora liugj 2013-09-24 modified for aurora's new feature start
        if (len > 0) {
           /* startQueryStaredMsg(mAllSelectedThreadIds.get(0));
        } else {*/
        // Aurora liugj 2013-09-24 modified for aurora's new feature end
            deleteThreads();
            boolean hasStar = false;
                // Aurora liugj 2013-10-24 modified for aurora's new feature start
            /*queryFavoriteList();
            for (int i = 0; i < mFavoriThreadsList.size(); i++) {
                if (mAllSelectedThreadIds.contains(mFavoriThreadsList.get(i))) {
                    hasStar = true;
                    break;
                }
            }*/
                // Aurora liugj 2013-10-24 modified for aurora's new feature end
            boolean isCheckAll = false;
            // Aurora xuyong 2014-08-23 modified for bug #7789 start
            if (mAllSelectedThreadIds.size() == mListAdapter.getCount() && !isEncryptionList && !mHideEncryp) {
            // Aurora xuyong 2014-08-23 modified for bug #7789 end
                isCheckAll = true;
            }
            // Aurora liugj 2013-09-24 modified for aurora's new feature start
            //Gionee <zhouyj> <2013-04-11> modify for CR00796238 start
              // Aurora liugj 2013-10-24 modified for aurora's new feature start
            long threadId = -1;
            if (len == 1) {
                threadId = mAllSelectedThreadIds.get(0);
            }else {
                threadId = (isCheckAll) ? -1 : 9999;
            }
             // Aurora liugj 2014-01-22 modified for listview delete animation start
            GnDeleteThreadListener listener = new GnDeleteThreadListener(mListView, threadId ,
            // Aurora xuyong 2014-11-26 modified for bug #10049 start
                    mQueryHandler, AuroraConvListActivity.sAuroraConvListActivity);
            confirmDeleteGnThreadDialog(listener, isCheckAll, hasStar,
                    AuroraConvListActivity.sAuroraConvListActivity);
            // Aurora xuyong 2014-11-26 modified for bug #10049 end
             // Aurora liugj 2014-01-22 modified for listview delete animation end
             // Aurora liugj 2013-10-24 modified for aurora's new feature end
            //Gionee <zhouyj> <2013-04-11> modify for CR00796238 end
            // Aurora liugj 2013-09-24 modified for aurora's new feature end
        }
    }
    // gionee zhouyj 2012-11-08 modify for CR00725666 end 

    private void initThreadsMap() {
        // TODO Auto-generated method stub
        Cursor cursor = mListAdapter.getCursor();
        // Aurora liugj 2013-10-25 modified for aurora's new feature start
        // Aurora xuyong 2014-07-17 modified for bug #6619 start
        if (cursor != null && !cursor.isClosed()) {
        // Aurora xuyong 2014-07-17 modified for bug #6619 end
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
        // Aurora liugj 2013-10-25 modified for aurora's new feature end
    }
    //gionee gaoj 2012-9-20 added for CR00699291 end

    //gionee gaoj 2012-10-15 added for CR00705539 start
    // Aurora xuyong 2014-08-23 modified for bug #7789 start
    private static final int BatchDeleteNum = 500;
    // Aurora xuyong 2014-08-23 modified for bug #7789 end
    private static ArrayList<String> mThreadsList = new ArrayList<String>();
    public static final int THREAD_LIST_FAVORITE_QUERY_ONE_TOKEN = 1013;
    public static final int THREAD_LIST_FAVORITE_QUERY_ALL_TOKEN = 1014;

    private static void deleteThreads() {
        mThreadsList.clear();
        long threadId;
        StringBuilder buf = new StringBuilder();
        int i = 0;
        // Aurora xuyong 2014-08-23 added for bug #7789 start
        Collections.sort(mAllSelectedThreadIds, new Comparator<Long>() {
            
            @Override
            public int compare(Long l1, Long l2) {
              return l1.compareTo(l2);
            }
            
        });
        // Aurora xuyong 2014-08-23 added for bug #7789 end
        Iterator<Long> it = mAllSelectedThreadIds.iterator();
        while(it.hasNext()) {
            // Aurora xuyong 2014-08-01 modified for bug #7040 start
              i++;
            threadId = it.next();
            buf.append(Long.toString(threadId));
            // Aurora xuyong 2014-08-23 modified for bug #7789 start
            if (i % BatchDeleteNum == 0) {
                 if (buf.length() > 0) {
                     mThreadsList.add(buf.toString());
                 }
                buf = new StringBuilder();
            } else if (it.hasNext()) {                
                   buf.append(",");
            }
            // Aurora xuyong 2014-08-01 modified for bug #7040 end
        }
        if (buf.length() > 0) {
            mThreadsList.add(buf.toString());
        }
             // Aurora xuyong 2014-08-23 modified for bug #7789 end
    }

    private static ArrayList<Long> mFavoriThreadsList = new ArrayList<Long>();

    private final static String[] FAVORITE_THREADS_PROJECTION = new String[] {
        Conversations.THREAD_ID
    };

    private void queryFavoriteList() {
        // Aurora xuyong 2014-11-26 modified for bug #10049 start
        Cursor cursor = AuroraConvListActivity.sContext.getContentResolver().query(Sms.CONTENT_URI, FAVORITE_THREADS_PROJECTION, "star=1",
        // Aurora xuyong 2014-11-26 modified for bug #10049 end
                null, null);
        mFavoriThreadsList.clear();
        // Aurora xuyong 2014-07-17 modified for bug #6619 start
        if (cursor != null && !cursor.isClosed()) {
        // Aurora xuyong 2014-07-17 modified for bug #6619 end
            cursor.moveToPosition(-1);
            while (cursor.moveToNext() && !cursor.isBeforeFirst() && !cursor.isAfterLast()) {
                Long threadid = cursor.getLong(0);
                mFavoriThreadsList.add(threadid);
            }
            cursor.close();
        }
    }
    //gionee gaoj 2012-10-15 added for CR00705539 end
    
    // gionee zhouyj 2012-10-26 add for CR00718476 start 
    /*public static boolean isMultiMode() {
        return sInMultiMode;
    }*/
    // gionee zhouyj 2012-10-26 add for CR00718476 end 
    
    //Gionee <zhouyj> <2013-05-06> modify for CR00799297 begin
    private void startQuery() {
        if (isEncryptionList) {
            startEncryptionQuery();
        } else {
            //Gionee <gaoj> <2013-05-28> add for CR00817770 begin
            if (MmsApp.mGnHideEncryption) {
                setEncryptionHide(mHideEncryp);
            } else {
                //Gionee <gaoj> <2013-05-28> add for CR00817770 end
                startAsyncQuery();
                //Gionee <gaoj> <2013-05-28> add for CR00817770 begin
            }
            //Gionee <gaoj> <2013-05-28> add for CR00817770 end
        }
    }
    //Gionee <zhouyj> <2013-05-04> modify for CR00799297 end
    
    //Gionee <gaoj> <2013-05-21> added for CR00817770 begin
    static boolean mHideEncryp = false;
    
    public void setEncryptionHide (boolean hide) {
        // Aurora liugj 2013-09-24 modified for aurora's new feature start
        // Aurora liugj 2013-11-16 modified for bug-813 start
        if (isEncryptionList /*|| (null != mActionBatchHandler && mActionBatchHandler.isInSelectionMode())*/) {
        // Aurora liugj 2013-11-16 modified for bug-813 end
        // Aurora liugj 2013-09-24 modified for aurora's new feature end
            return;
        }
        if (hide) {
            startNoEncryptionQuery();
        } else {
            startAsyncQuery();
        }

        if (mHideEncryp != hide && queryHasEncryption()) {
            mHideEncryp = hide;
            HideEncryptiontoast(mHideEncryp);
        }
    }

    private boolean queryHasEncryption() {
        ContentResolver resolver = AuroraConvListActivity.sContext.getContentResolver();
        String[] THREADS_QUERY_COLUMNS = { "encryption" };
        Cursor cursor = null;
        try {
            cursor = resolver.query(Conversation.sAllThreadsUri, null,
                    "encryption = 1", null, null);
            Log.d("ConvFragment", ""+cursor.getCount());
            // Aurora xuyong 2014-07-17 modified for bug #6619 start
            if (cursor != null && !cursor.isClosed() && cursor.getCount() > 0) {
            // Aurora xuyong 2014-07-17 modified for bug #6619 end
                return true;
            }
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    public void startNoEncryptionQuery() {
        try {
          // Aurora xuyong 2014-04-21 added for bug #4460 start
            if (mListView != null) {
                mListView.auroraSetNeedSlideDelete(false);
            }
          // Aurora xuyong 2014-04-21 added for bug #4460 end
            Conversation.startQueryForNoEncryption(mQueryHandler, THREAD_LIST_QUERY_TOKEN, true);
        } catch (SQLiteException e) {
            // Aurora xuyong 2014-11-26 modified for bug #10049 start
            SqliteWrapper.checkSQLiteException(AuroraConvListActivity.sContext, e);
            // Aurora xuyong 2014-11-26 modified for bug #10049 end
        }
    }
    //Gionee <gaoj> <2013-05-21> added for CR00817770 end

    //Gionee <gaoj> <2013-05-28> add for CR00817770 begin
    

    private void OpenPopLayout() {
        FrameLayout frameLayout = (FrameLayout) getView().findViewById(R.id.gn_conv_frame_layout);
        View AuroraSmartPopupLayout = initPopupLayout(AuroraConvListActivity.sContext);
        FrameLayout.LayoutParams poParams = new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, 
                LayoutParams.FILL_PARENT, Gravity.CENTER);
        frameLayout.addView(AuroraSmartPopupLayout , poParams);
    }

    public View initPopupLayout(Context context){
        AuroraSmartPopupLayout smartPopup = new AuroraSmartPopupLayout(context);
        smartPopup.setSmartDegree(AuroraSmartLayout.HIGH_DEGREE);
        Settings.System.putInt(context.getContentResolver(), 
                AuroraSmartPopupLayout.USER_DEGREE, AuroraSmartLayout.LOW_DEGREE);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setVerticalGravity(Gravity.BOTTOM);
        /*linearLayout.setBackgroundResource(R.drawable.gn_conv_bg_popup);*/
        int color = context.getResources().getColor(R.color.gn_hide_encry_pop_text_color);

        TextView textView1 = new TextView(context);
        textView1.setText(context.getResources().getString(R.string.gn_conv_popup_text1));
        textView1.setTextSize(18);
        textView1.setTextColor(color);
        LinearLayout.LayoutParams lp1=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        lp1.setMargins(30, 0, 0, 0);
        linearLayout.addView(textView1, lp1);

        TextView textView2 = new TextView(context);
        textView2.setText(context.getResources().getString(R.string.gn_conv_popup_text2));
        textView2.setTextSize(18);
        textView2.setTextColor(color);
        LinearLayout.LayoutParams lp2=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        lp2.setMargins(0, 0, 30, 20);
        lp2.gravity = Gravity.RIGHT;
        linearLayout.addView(textView2, lp2);

        LinearLayout.LayoutParams lp3=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        smartPopup.setVerticalGravity(Gravity.CENTER);
        smartPopup.setHorizontalGravity(Gravity.CENTER);
        smartPopup.addView(linearLayout, lp3);
        return smartPopup;
    }
    //Gionee <gaoj> <2013-05-28> add for CR00817770 end

    //Gionee <gaoj> <2013-05-28> add for CR00817770 begin
    private static final String POPUPOPEN = "PopupIsOpen";
    public static final String ISOPEN_STRING = "isOpen";
    public static final String FIRSTENCRYPTION = "firstEncryption";
    public static final String HIDEENCRYPTION = "HideEncryption";

    public static boolean ReadPopTag(Context context, String tag) {
        SharedPreferences user = context.getSharedPreferences(POPUPOPEN, context.MODE_PRIVATE);
      //Gionee <zhouyj> <2013-08-01> modify for CR00836913 begin
        boolean isSmartPopSupport = true;
        try {
            Class <?> clazz = Class.forName("aurora.widget.AuroraSmartPopupLayout");//"com.gnwidget.SmartPopupLayout");
        } catch (ClassNotFoundException e) {
            // TODO: handle exception
            Log.i("ConvFragment", "e = " + e);
            isSmartPopSupport = false;
        }
        if (user.getBoolean(tag, false) && isSmartPopSupport) {
            return true;
        }
        //Gionee <zhouyj> <2013-08-01> modify for CR00836913 end
        return false;
    }

    public static void WritePopTag(Context context, String tag) {
        WritePopTag(context, tag, true);
    }

    public static void WritePopTag(Context context, String tag, Boolean value) {
        SharedPreferences user = context.getSharedPreferences(POPUPOPEN, context.MODE_PRIVATE);
        Editor editor = user.edit();
        editor.putBoolean(tag, value);
        editor.commit();
    }

    private void HideEncryptiontoast(boolean hide) {
        final int resId = hide ? R.string.gn_conv_hide_encry_toast
                : R.string.gn_conv_view_encry_toast;
        Toast.makeText(AuroraConvListActivity.sContext, resId, Toast.LENGTH_SHORT).show();
    }
    //Gionee <gaoj> <2013-05-28> add for CR00817770 end
}
