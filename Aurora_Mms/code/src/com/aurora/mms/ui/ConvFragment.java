package com.aurora.mms.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
// Aurora xuyong 2014-08-23 added for bug #7789 start
import java.util.Collections;
import java.util.Comparator;
// Aurora xuyong 2014-08-23 added for bug #7789 end
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

// Aurora xuyong 2016-02-23 added for bug #19581 start
import com.aurora.mms.util.AnimUtils;
// Aurora xuyong 2016-02-23 added for bug #19581 start
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
import android.app.Fragment;
import android.app.LoaderManager;
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
// Aurora xuyong 2016-03-03 added for bug #20197 start
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
// Aurora xuyong 2016-03-03 added for bug #20197 end
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
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.SearchRecentSuggestions;
// Aurora liugj 2013-09-20 added for aurora's new feature end
import android.provider.Settings;
//import gionee.provider.GnSettings.System;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import gionee.provider.GnTelephony.Threads;
import android.provider.Telephony.Sms.Conversations;
import aurora.view.PagerAdapter;
import aurora.view.ViewPager;
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
import android.widget.AdapterView.OnItemClickListener;
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
import com.privacymanage.service.AuroraPrivacyUtils;

// Aurora liugj 2013-09-24 added for aurora's new feature start
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
// Aurora liugj 2013-09-24 added for aurora's new feature end
// Aurora liugj 2013-10-10 modified for aurora's new feature end
import aurora.widget.floatactionbutton.FloatingActionButton;
import aurora.widget.floatactionbutton.FloatingActionButton.OnFloatActionButtonClickListener;
import aurora.widget.AuroraTabWidget;
import aurora.widget.AuroraViewPager;

import gionee.provider.GnCallLog;

public class ConvFragment extends Fragment implements
     DraftCache.OnDraftChangedListener, OnItemLongClickListener, OnItemClickListener,
     AuroraSearchView.OnQueryTextListener, AuroraSearchView.OnCloseListener,
     AuroraActivity.OnSearchViewQuitListener, View.OnTouchListener{

    private static final String DEFAULT_SORT_ORDER = "sms.date DESC";
    private static final String SMS_ID = "_id";
    private static final String SMS_THREAD_ID = "thread_id";
    private static final String SMS_ADDRESS = "address";
    private static final String SMS_DATE = "date";
    private static final String SMS_BODY = "body";
    private static final String MMS_SUB = "sub";
    private static final String MMS_SUB_CS = "sub_cs";
    private static final String MSG_TYPE = "auroramsgtype";
    private static final String WORDS_INDEX_TEXT = "index_text";
    private static final String WORDS_ID = "_id";
    
    protected static final String[] PROJECTION_SMS = new String[] {
        SMS_ID,
        SMS_THREAD_ID,
        SMS_ADDRESS,
        SMS_BODY,
        SMS_DATE,
        MMS_SUB,
        MMS_SUB_CS,
        MSG_TYPE,
        WORDS_INDEX_TEXT,
        WORDS_ID
    };

    private static ThreadListQueryHandler mQueryHandler;

    private ConversationListAdapter mListAdapter;
    private ConversationListAdapter mNotifyListAdapter;

    private MessageSearchListAdapter mAdapter;

    private static final int THREAD_LIST_QUERY_TOKEN              = 1701;
    private static final int THREAD_LIST_NOTIFY_QUERY_TOKEN       = 1702;

    public static final int DELETE_CONVERSATION_TOKEN      = 1801;

    public static final int HAVE_LOCKED_MESSAGES_TOKEN     = 1802;
    private static final int DELETE_OBSOLETE_THREADS_TOKEN = 1803;
    public static final int HAVE_STAR_MESSAGES_TOKEN       = 1804;

    public static final int MUL_DELETE_CONVERSATIONS_TOKEN = 1805;
    
    public static final int DELETE_CONVERSATION_NOT_LAST_TOKEN = 1901;

    public static final int THREAD_LIST_LOCKED_QUERY_TOKEN  = 1902;
    public static final int THREAD_LIST_FAVORITE_QUERY_TOKEN = 1903;

    private static ArrayList<Long> mAllSelectedThreadIds = new ArrayList<Long>();

    public static final String[] ALL_THREADS_PROJECTION = {
        Threads._ID, Threads.DATE, Threads.MESSAGE_COUNT, Threads.RECIPIENT_IDS,
        Threads.SNIPPET, Threads.SNIPPET_CHARSET, Threads.READ, Threads.ERROR,
        Threads.HAS_ATTACHMENT, Threads.TYPE, Threads.SIM_ID
    };

    private final int INIT_NONE = 0;
    private final int INIT_INIT_ITEM_COUNT_DONE = 1;
    private final int INIT_ALL_DONE = 2;

    private int mInitStatu = INIT_NONE;
    private int mNotifyInitStatu = INIT_NONE;

    private static final int CHANGE_MC1_CURSOR = 11;
    private static final int CHANGE_NOTIFY_MC1_CURSOR = 111;

    private static final int INIT_ITEM_COUNT = 10;
    private static final int POST_ITEM_COUNT = 100;

    private int mInitedCount = 0;
    private int mNotifyInitedCount = 0;

    private boolean mScrollToBottom = true;
    private boolean mNotifyScrollToBottom = true;

    public static final int DIS_SELECT_ALL = 12;
    public static final int SELECT_ALL = 13;
    private static final int FINISH_ACTIVITY = 14;

    private static final int LOAD_ALL_CONTACT_CHANGE = 16;
    private static final int LOAD_NOTIFY_ALL_CONTACT_CHANGE = 18;
    private static final int LOAD_ALL_CONTACT_DONE = 17;
    private static final int LOAD_NOTIFY_ALL_CONTACT_DONE = 19;
    // Aurora xuyong 2016-01-26 added for bug #18470 start
    private static final int RE_RESEACH = 20;
    // Aurora xuyong 2016-01-26 added for bug #18470 end
    AuroraProgressDialog mProgressDialog = null;
    AuroraProgressDialog mNotifyProgressDialog = null;

    ExecutorService mSingleThreadExecutor = Executors.newSingleThreadExecutor();
    ExecutorService mNotifySingleThreadExecutor = Executors.newSingleThreadExecutor();

    ExecutorService mSingleThreadExecutor2 = Executors.newSingleThreadExecutor();
    ExecutorService mNotifySingleThreadExecutor2 = Executors.newSingleThreadExecutor();
    // Aurora xuyong 2016-03-10 added for bug #20942 start
    public ConvFragment() {
        super();
    }

    public ConvFragment(int tabIndex) {
        super();
        mFromConvTabIndex = tabIndex;
    }
    // Aurora xuyong 2016-03-10 added for bug #20942 end
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                // Aurora xuyong 2016-01-26 added for bug #18470 start
                case RE_RESEACH:
                    if (mQueryAsyncTask != null) {
                        mQueryAsyncTask.cancel(true);
                        String queryString = (String)msg.obj;
                        mQueryAsyncTask = new QueryAsyncTask(AuroraConvListActivity.sContext, queryString);
                        mQueryAsyncTask.execute(queryString);
                    }
                    break;
                // Aurora xuyong 2016-01-26 added for bug #18470 end
                case LOAD_ALL_CONTACT_CHANGE:
                    mListAdapter.notifyCountSetChanged(msg.arg1);
                    break;
                case LOAD_ALL_CONTACT_DONE:
                    if(mListAdapter != null){
                        Cursor listCursor = mListAdapter.getCursor();
                        if (listCursor == null || listCursor.isClosed()) {
                            mListAdapter.notifyCountSetChanged(0);
                        } else {
                            mListAdapter.notifyCountSetChanged(listCursor.getCount());
                        }
                    }
                    break;
                case LOAD_NOTIFY_ALL_CONTACT_CHANGE:
                    mNotifyListAdapter.notifyCountSetChanged(msg.arg1);
                    break;
                case LOAD_NOTIFY_ALL_CONTACT_DONE:
                    if(mNotifyListAdapter != null){
                        Cursor listNotifyCursor = mNotifyListAdapter.getCursor();
                        if (listNotifyCursor == null || listNotifyCursor.isClosed()) {
                            mNotifyListAdapter.notifyCountSetChanged(0);
                        } else {
                            mNotifyListAdapter.notifyCountSetChanged(listNotifyCursor.getCount());
                        }
                    }
                    break;
                case FINISH_ACTIVITY:
                    leaveBatchMode();
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                    Activity activity = AuroraConvListActivity.sAuroraConvListActivity;
                    if (activity != null) {
                        activity.finish();
                    }
                    break;
                case CHANGE_MC1_CURSOR:
                    if (msg.obj == null) {
                        operateAfterChangeCursor(null, PERSONAL_TAB);
                        return;
                    }
                    final Cursor allCursor = (Cursor)msg.obj;
                    if (allCursor == null || allCursor.getCount() <= 0) {
                        mListAdapter.changeCursor(allCursor);
                        operateAfterChangeCursor(allCursor, PERSONAL_TAB);
                        return;
                    }
                    if(mInitStatu == INIT_NONE){
                        mInitStatu = INIT_INIT_ITEM_COUNT_DONE;

                        mListAdapter.changeCountBeforeDataChanged(INIT_ITEM_COUNT);
                    } else if (mInitStatu == INIT_ALL_DONE){
                        mListAdapter.changeCountBeforeDataChanged(allCursor.getCount());
                    }
                    mListAdapter.changeCursor(allCursor);
                    operateAfterChangeCursor(allCursor, PERSONAL_TAB);
                    break;
                case CHANGE_NOTIFY_MC1_CURSOR:
                    if (msg.obj == null) {
                        operateAfterChangeCursor(null, NOTIFICATION_TAB);
                        return;
                    }
                    final Cursor allNotifyCursor = (Cursor)msg.obj;
                    if (allNotifyCursor == null || allNotifyCursor.getCount() <= 0) {
                        mNotifyListAdapter.changeCursor(allNotifyCursor);
                        operateAfterChangeCursor(allNotifyCursor, NOTIFICATION_TAB);
                        return;
                    }
                    if(mNotifyInitStatu == INIT_NONE){
                        mNotifyInitStatu = INIT_INIT_ITEM_COUNT_DONE;
                        mNotifyListAdapter.changeCountBeforeDataChanged(INIT_ITEM_COUNT);
                    } else if (mNotifyInitStatu == INIT_ALL_DONE){
                        mNotifyListAdapter.changeCountBeforeDataChanged(allNotifyCursor.getCount());
                    }
                    mNotifyListAdapter.changeCursor(allNotifyCursor);
                    operateAfterChangeCursor(allNotifyCursor, NOTIFICATION_TAB);
                    break;
                default :
                        // more to be do here
                        break;
            }
        }
        
    };

    private void loadAllContact(){
        mSingleThreadExecutor2.execute(new Runnable() {
            public void run() {
                Cursor prepareCursor = null;
                Context context = AuroraConvListActivity.sContext;
                String selection = "notification_index = 0";
                prepareCursor = context.getContentResolver().query(Conversation.sAllThreadsUri, Conversation.ALL_THREADS_PROJECTION, selection, null, Conversations.DEFAULT_SORT_ORDER);
                mInitedCount = INIT_ITEM_COUNT;
                if(context != null){
                    if(prepareCursor != null){
                        if (prepareCursor.moveToPosition(INIT_ITEM_COUNT)) {
                            do{
                                Conversation.from(context, prepareCursor, true);
                                mInitedCount ++;
                                if (mListAdapter != null) {
                                    if(mScrollToBottom && mInitStatu == INIT_INIT_ITEM_COUNT_DONE && mInitedCount - mListAdapter.getCount() > POST_ITEM_COUNT){
                                        mScrollToBottom = false;
                                        Message msg = mHandler.obtainMessage(LOAD_ALL_CONTACT_CHANGE);
                                        msg.arg1 =  mInitedCount;
                                        mHandler.sendMessage(msg);
                                    }
                                }
                            } while (prepareCursor.moveToNext() && !prepareCursor.isBeforeFirst() && !prepareCursor.isAfterLast());
                        }
                        prepareCursor.close();
                    }
                }
                mInitStatu = INIT_ALL_DONE;
                mHandler.sendEmptyMessage(LOAD_ALL_CONTACT_DONE);
            }
        });
    }

    private void loadAllNotifyContact(){
        mNotifySingleThreadExecutor2.execute(new Runnable() {
            public void run() {
                Cursor prepareCursor = null;
                Context context = AuroraConvListActivity.sContext;
                String selection = "notification_index = 1";
                prepareCursor = context.getContentResolver().query(Conversation.sAllThreadsUri, Conversation.ALL_THREADS_PROJECTION, selection, null, Conversations.DEFAULT_SORT_ORDER);
                mNotifyInitedCount = INIT_ITEM_COUNT;
                if(context != null){
                    if(prepareCursor != null){
                        if (prepareCursor.moveToPosition(INIT_ITEM_COUNT)) {
                            do{
                                Conversation.from(context, prepareCursor, true);
                                mNotifyInitedCount ++;
                                if (mNotifyListAdapter != null) {
                                    if(mNotifyScrollToBottom && mNotifyInitStatu == INIT_INIT_ITEM_COUNT_DONE && mNotifyInitedCount - mNotifyListAdapter.getCount() > POST_ITEM_COUNT){
                                        mNotifyScrollToBottom = false;
                                        Message msg = mHandler.obtainMessage(LOAD_NOTIFY_ALL_CONTACT_CHANGE);
                                        msg.arg1 =  mNotifyInitedCount;
                                        mHandler.sendMessage(msg);
                                    }
                                }
                            } while (prepareCursor.moveToNext() && !prepareCursor.isBeforeFirst() && !prepareCursor.isAfterLast());
                        }
                        prepareCursor.close();
                    }
                }
                mNotifyInitStatu = INIT_ALL_DONE;
                mHandler.sendEmptyMessage(LOAD_NOTIFY_ALL_CONTACT_DONE);
            }
        });
    }

    private SharedPreferences mPrefs;
    static private final String CHECKED_MESSAGE_LIMITS = "checked_message_limits";

    //wappush: indicates the type of thread, this exits already, but has not been used before
    private int mType;
    //wappush: SiExpired Check
    private SiExpiredCheck siExpiredCheck;
    //wappush: wappush TAG
    private static final String WP_TAG = "Mms/WapPush";

    private boolean mNeedToMarkAsSeen;
    private static AuroraAlertDialog mDelAllDialog;
    private static AuroraAlertDialog mNotifyDelAllDialog;
    private AuroraMenu mAuroraMenu;
    private AuroraMenu mBottomAuroraMenu;

    public static final int CONFIRM_PASSWORD_REQUEST = 39;
    public static final int UPDATE_PASSWORD_REQUEST =40;
    public static final int MULTI_PASSWORD_REQUEST = 41;
    public static final int CONFIRM_DECRYPTION_PASSWORD_REQUEST = 42;

    private FloatingActionButton mFloatingButton;
    private View mTabConview;
    private AuroraTabWidget mAuroraTabWidget;
    private AuroraViewPager mAuroraViewPager;
    private ConvTabPageAdapter mConvTabPageAdapter;
    private View[] pageViews = new View[2];

    private View mFragmentView;
    private View mNotifyFragmentView;

    private AuroraListView mListView;
    private AuroraListView mNotifyListView;

    private boolean isInit = false;
    private boolean isNotifyInit = false;
    // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature start
    private LinearLayout mEncryptionTitle;
    private LinearLayout mNotifyEncryptionTitle;
    // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature end
    public static boolean isEncryptionList = false;
    public static boolean isNotifyEncryptionList = false;

    private int mSelectCount = 0;
    private int mNotifySelectCount = 0;

    private boolean mBeingEncrypt = false;
    private boolean mNotifyBeingEncrypt = false;

    public static HashSet<Long> mSetEncryption = new HashSet<Long>();
    public static HashSet<Long> mNotifySetEncryption = new HashSet<Long>();

    private boolean mContentChanged;
    private boolean mNotifyContentChanged;

    private  static GnActionModeHandler<Long> mActionModeHandler = null;

    private static AuroraActionBatchHandler<Long> mActionBatchHandler = null;

    private Map<Integer, Long> mThreadsMap = new HashMap<Integer, Long>();
    private Map<Integer, Long> mNotifyThreadsMap = new HashMap<Integer, Long>();

    private static boolean mListChange = false;
    private static boolean mNotifyListChange = false;

    private static boolean gnFlyFlag = SystemProperties.get("ro.gn.oversea.custom").equals("RUSSIA_FLY");
    
    private boolean mFirstStart = true;

    private Contact mContact = null;
    private boolean mHasEncryption = false;

    private static final int MENU_DELETE       = 10;
    private static final int MENU_ENCRYPTION   = 11;
    private static final int MENU_DECRYPTION   = 12;
    private static final int MENU_VIEW_CONTACT = 13;
    private static final int MENU_NEW_CONTACT  = 14;
    private static final int MENU_ADD_CONTACT  = 15;

    public static final int ONEDEL_CONFIRM_PASSWORD_REQUEST = 11;
    public static final int ONE_CONTEXT_PASSWORD_REQUEST = 12;

    private long mThreadID = -1;
    public boolean mSearchMode = false;
    private String mQueryString;

    private ContentObserver mConvObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange) { 
             super.onChange(selfChange);
             if (!mIsDeleting) {
                mQueryHandler.removeCallbacks(mQueryRunnable);
                mQueryHandler.postDelayed(mQueryRunnable, 500);
             }
        }   

    };

    private void initProgressAddingDialog(Context context) {
        mProgressDialog = new AuroraProgressDialog(context);
        mProgressDialog.setMessage(context.getString(R.string.aurora_black_adding));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
    }

    private void initNotifyProgressAddingDialog(Context context) {
        mNotifyProgressDialog = new AuroraProgressDialog(context);
        mNotifyProgressDialog.setMessage(context.getString(R.string.aurora_black_adding));
        mNotifyProgressDialog.setCanceledOnTouchOutside(false);
        mNotifyProgressDialog.setCancelable(false);
    }

    private final int RECENT_CONTACT_COUNT = 5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Context context = AuroraConvListActivity.sContext;
        if (context == null) {
            return;
        }
        mQueryHandler = new ThreadListQueryHandler(AuroraConvListActivity.sContext.getContentResolver());
        mPrefs = AuroraPreferenceManager.getDefaultSharedPreferences(AuroraConvListActivity.sContext);
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            siExpiredCheck = new SiExpiredCheck(AuroraConvListActivity.sContext);
            siExpiredCheck.startSiExpiredCheckThread();
        }
        if (MmsApp.mGnHideEncryption) {
            mHideEncryp = ReadPopTag(AuroraConvListActivity.sContext, HIDEENCRYPTION);
            Log.d("ConvFragment", "onCreate mHideEncryp = "+mHideEncryp);
        }
        initProgressAddingDialog(context);
        context.getContentResolver().registerContentObserver(MmsSms.CONTENT_URI, true, mConvObserver);
        // Aurora xuyong 2016-01-19 added for bug #18457 start
        context.getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, mConvObserver);
        // Aurora xuyong 2016-01-19 added for bug #18457 end
        DraftCache.getInstance().refresh();
        loadAllContact();
        loadAllNotifyContact();
    }

    @Override
    public LoaderManager getLoaderManager() {
        return super.getLoaderManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mTabConview = inflater.inflate(R.layout.aurora_conversation_tab_screen, container, false);
        mAuroraTabWidget = (AuroraTabWidget)mTabConview.findViewById(R.id.aurora_conv_tab);
        mAuroraTabWidget.setOnPageChangeListener(new ConvTabPagerChangeListener());
        mFloatingButton = (FloatingActionButton)mTabConview.findViewById(R.id.aurora_edit_msg_bt);
        mFloatingButton.setOnFloatingActionButtonClickListener(new OnFloatActionButtonClickListener() {

            @Override
            public void onClick() {
                Context context = AuroraConvListActivity.sContext;
                if (context != null) {
                    Intent intent = ComposeMessageActivity.createIntent(context, 0);
                    startActivity(intent);
                }
            }

        });
        mFragmentView = inflater.inflate(R.layout.aurora_conversation_list_screen, null, false);
        mNotifyFragmentView = inflater.inflate(R.layout.aurora_conversation_list_screen, null, false);
        pageViews[0] = mFragmentView;
        pageViews[1] = mNotifyFragmentView;
        mAuroraViewPager = mAuroraTabWidget.getViewPager();
        mConvTabPageAdapter = new ConvTabPageAdapter();
        mAuroraViewPager.setAdapter(mConvTabPageAdapter);
        // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature start
        mEncryptionTitle = (LinearLayout) mFragmentView.findViewById(R.id.gn_encryption_title);
        mEncryptionTitle.setOnTouchListener(this);
        mNotifyEncryptionTitle = (LinearLayout) mNotifyFragmentView.findViewById(R.id.gn_encryption_title);
        // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature end
        mNotifyEncryptionTitle.setOnTouchListener(this);
        return mTabConview;
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        mListView = getListView();
        mNotifyListView  = getNotifyListView();
        mListView.setOnTouchListener(this);
        mNotifyListView.setOnTouchListener(this);
        AbsListView.OnScrollListener listScrollListener = new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub
                // Aurora xuyong 2016-01-28 added for xy-smartsms start
                switch (scrollState) {
                    case OnScrollListener.SCROLL_STATE_FLING:
                        getCurrentAdapter().SCROLL_STATE_FLING = true;
                        break;
                    default:
                        // Aurora xuyong 2016-01-29 modified for xy-smartsms start
                        getCurrentAdapter().SCROLL_STATE_FLING = false;
                        // Aurora xuyong 2016-01-29 modified for xy-smartsms end
                        break;
                }
                // Aurora xuyong 2016-01-28 added for xy-smartsms end
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                // TODO Auto-generated method stub
                // Aurora xuyong 2016-01-28 added for xy-smartsms start
                if(firstVisibleItem == 0){
                    if(getCurrentAdapter() !=null){
                        getCurrentAdapter().SCROLL_STATE_FLING=false;

                    }
                }
                else if(firstVisibleItem+visibleItemCount == totalItemCount){
                    if(getCurrentAdapter() !=null){
                        getCurrentAdapter().SCROLL_STATE_FLING=false;
                    }
                }
                // Aurora xuyong 2016-01-28 added for xy-smartsms end
                if (firstVisibleItem + visibleItemCount > totalItemCount - 2 && totalItemCount > 0) {
                    if (mCurrentTabIndex == PERSONAL_TAB) {
                        if (mInitedCount > INIT_ITEM_COUNT && mInitStatu == INIT_INIT_ITEM_COUNT_DONE && mInitedCount > mListAdapter.getCount() + POST_ITEM_COUNT) {
                            mListAdapter.notifyCountSetChanged(mInitedCount);
                        } else {
                            mScrollToBottom = true;
                        }
                    } else if (mCurrentTabIndex == NOTIFICATION_TAB) {
                        if (mNotifyInitedCount > INIT_ITEM_COUNT && mNotifyInitStatu == INIT_INIT_ITEM_COUNT_DONE && mNotifyInitedCount > mNotifyListAdapter.getCount() + POST_ITEM_COUNT) {
                            mNotifyListAdapter.notifyCountSetChanged(mNotifyInitedCount);
                        } else {
                            mNotifyScrollToBottom = true;
                        }
                    }
                }
            }
        };
        mListView.setOnItemClickListener(ConvFragment.this);
        mNotifyListView.setOnItemClickListener(ConvFragment.this);
        mListView.setOnScrollListener(listScrollListener);
        mNotifyListView.setOnScrollListener(listScrollListener);
        mListView.setDividerHeight(0);
        // Aurora xuyong 2016-03-03 deleted for bug #20197 start
        //mNotifyListView.setDividerHeight(0);
        // Aurora xuyong 2016-03-03 deleted for bug #20197 end
        mListAdapter = new ConversationListAdapter(AuroraConvListActivity.sContext, null, PERSONAL_TAB);
        mNotifyListAdapter = new ConversationListAdapter(AuroraConvListActivity.sContext, null, NOTIFICATION_TAB);

        if (mActionBatchHandler != null && mActionBatchHandler.isInSelectionMode()) {
            if (mCurrentTabIndex == PERSONAL_TAB) {
                mListAdapter.showCheckBox(true);
            } else if (mCurrentTabIndex == NOTIFICATION_TAB) {
                mNotifyListAdapter.showCheckBox(true);
            }
            mActionBatchHandler.bindToAdapter(mActionBatchHandler.getSelectionManger());
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mEncryptionTitle == v || mListView == v || mNotifyEncryptionTitle == v || mNotifyListView == v) {
                InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);     
                if (imm.isActive()) {     
                    imm.hideSoftInputFromWindow(v.getApplicationWindowToken() , 0);   
                }
            }
        }
        return false;
    }

    @Override
    public boolean onClose() {
        AuroraActivity activity = AuroraConvListActivity.sAuroraConvListActivity;
        // Aurora xuyong 2016-01-06 modified for bug #18285 start
        if (mSearchBox != null) {
            mSearchBox.onActionViewExpanded();
            mSearchBox.setQuery(null, false);
        // Aurora xuyong 2016-01-06 modified for bug #18285 end
        }
        return false;
    }

    HashMap<String, Cursor> searchResultsCache = new HashMap<String, Cursor>();
    QueryAsyncTask mQueryAsyncTask;
    
    private class QueryAsyncTask extends AsyncTask<String, Void, Cursor> {
        
        private String mTag;
        private Context mQContext;
        private MatrixCursor mQueryResult;
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
            Uri uri = Uri.parse("content://mms-sms/search");
            // Aurora xuyong 2016-01-20 modified for bug #18501 start
            if (params == null || params.length <= 0) {
                return null;
            } else {
                // Aurora xuyong 2016-01-26 modified for bug #18470 start
                Cursor result = null;
                try {
                    result = mQContext.getContentResolver().query(uri.buildUpon().appendQueryParameter("pattern", params[0]).build(), null, null, null, null);
                } catch(SQLiteException e) {
                    e.printStackTrace();
                    if (mHandler != null) {
                        Message msg = mHandler.obtainMessage(RE_RESEACH);
                        msg.obj = params[0];
                        msg.sendToTarget();
                    }
                } finally {
                    return result;
                }
                // Aurora xuyong 2016-01-26 modified for bug #18470 end
            }
            // Aurora xuyong 2016-01-20 modified for bug #18501 end
        }

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
            // Aurora xuyong 2016-01-19 added for bug #18470 start
            if (!result.isClosed()) {
                result.close();
            }
            // Aurora xuyong 2016-01-19 added for bug #18470 end
        }

        @Override
        protected void onPostExecute(Cursor result) {
            if (mSearchMode) {
                // Aurora xuyong 2015-12-15 added for aurora 2.0 new feature start
                AuroraListView listView = getCurrentListView();
                ConversationListAdapter listAdapter = getCurrentAdapter();
                // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature start
                LinearLayout encryptionTitle = getCurrentEncrptionTitle();
                // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature end
                // Aurora xuyong 2015-12-15 added for aurora 2.0 new feature end
                if (result == null) {
                    // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature start
                    // Aurora xuyong 2016-01-12 deleted for aurora 2.0 new feature start
                    //listView.setVisibility(View.GONE);
                    // Aurora xuyong 2016-01-12 deleted for aurora 2.0 new feature end
                    // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature start
                    ((TextView)(encryptionTitle.findViewById(R.id.aurora_conv_empty))).setText(R.string.aurora_msgsearch_no_result_tip);
                    encryptionTitle.setVisibility(View.VISIBLE);
                    // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature end
                    // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature end
                    return;
                }
                getCopyOfResult(result);
                if (searchResultsCache != null && (!searchResultsCache.containsKey(mQueryString))) {
                    searchResultsCache.put(mQueryString, mQueryResult);
                }
                // Aurora xuyong 2016-01-19 modified for bug #18470 start
                if (mQueryResult == null) {
                    ((TextView)(encryptionTitle.findViewById(R.id.aurora_conv_empty))).setText(R.string.aurora_msgsearch_no_result_tip);
                    encryptionTitle.setVisibility(View.VISIBLE);
                    return;
                }
                int cursorCount = mQueryResult.getCount();
                // Aurora xuyong 2016-01-19 modified for bug #18470 end
                if (cursorCount == 0) {
                    // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature start
                    // Aurora xuyong 2016-01-12 deleted for aurora 2.0 new feature start
                    //listView.setVisibility(View.GONE);
                    // Aurora xuyong 2016-01-12 deleted for aurora 2.0 new feature end
                    // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature start
                    ((TextView)(encryptionTitle.findViewById(R.id.aurora_conv_empty))).setText(R.string.aurora_msgsearch_no_result_tip);
                    encryptionTitle.setVisibility(View.VISIBLE);
                    // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature end
                    // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature end
                    if (!result.isClosed()) {
                        result.close();
                    }
                // Aurora xuyong 2015-12-15 added for aurora 2.0 new feature start
                } else {
                    // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature start
                    encryptionTitle.setVisibility(View.GONE);
                    // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature end
                // Aurora xuyong 2015-12-15 added for aurora 2.0 new feature end
                    if (mAdapter == null) {
                        // Aurora xuyong 2016-01-19 added for bug #18470 start
                        mAdapter = new MessageSearchListAdapter(AuroraConvListActivity.sContext, mQueryResult);
                        // Aurora xuyong 2016-01-19 added for bug #18470 end
                    } else {
                        // Aurora xuyong 2016-01-19 added for bug #18470 start
                        mAdapter.changeCursor(mQueryResult);
                        // Aurora xuyong 2016-01-19 added for bug #18470 end
                    }
                    mAdapter.setQueryString(mQueryString);
                    mAdapter.setSearchMode(true);
                    // Aurora xuyong 2015-12-15 added for aurora 2.0 new feature start
                    listView.setAdapter(mAdapter);
                    listView.setRecyclerListener(mAdapter);
                    // Aurora xuyong 2016-01-12 deleted for aurora 2.0 new feature start
                    //listView.setVisibility(View.VISIBLE);
                    // Aurora xuyong 2016-01-12 deleted for aurora 2.0 new feature end
                    // Aurora xuyong 2015-12-15 added for aurora 2.0 new feature end
                    SearchRecentSuggestions recent = ((MmsApp) AuroraConvListActivity.sApp).getRecentSuggestions();
                    if (recent != null) {
                        recent.saveRecentQuery(
                                mQueryString,
                                getString(R.string.search_history,
                                        cursorCount, mQueryString));
                    }
                    if (mQueryString != null) {
                        if (mQueryString.contains("/%")) {
                            mQueryString = mQueryString.replaceAll("/%", "%");
                        }
                        if (mQueryString.contains("//")) {
                            mQueryString = mQueryString.replaceAll("//", "/");
                        }
                        mActivityQueryString = mQueryString;
                    }
                }
            }
        }
        
    }

    private String mActivityQueryString;

    @Override
    public boolean onQueryTextChange(final String queryString) {
        String changedQueryString = queryString;
        // Aurora xuyong 2015-12-15 added for aurora 2.0 new feature start
        AuroraListView listView = getCurrentListView();
        ConversationListAdapter listAdapter = getCurrentAdapter();
        // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature start
        LinearLayout encryptionTitle = getCurrentEncrptionTitle();
        // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature end
        // Aurora xuyong 2015-12-15 added for aurora 2.0 new feature end
        if (queryString != null) {
            if (queryString.contains("/")) {
                changedQueryString = changedQueryString.replaceAll("/", "//"); 
            }
            if (queryString.contains("%")) {
                changedQueryString = changedQueryString.replaceAll("%", "/%");
            }
        }
        if (TextUtils.isEmpty(changedQueryString)) {
            AuroraActivity activityE = AuroraConvListActivity.sAuroraConvListActivity;
            if (activityE == null) {
                return false;
            }
            activityE.getSearchViewGreyBackground().setVisibility(View.VISIBLE);
            // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature start
            // Aurora xuyong 2016-01-11 deleted for aurora 2.0 new feature start
            //encryptionTitle.setVisibility(View.GONE);
            // Aurora xuyong 2016-01-11 deleted for aurora 2.0 new feature end
            // Aurora xuyong 2016-01-12 deleted for aurora 2.0 new feature start
            //listView.setVisibility(View.VISIBLE);
            // Aurora xuyong 2016-01-12 deleted for aurora 2.0 new feature end
            if (listView.getHeaderViewsCount() == 0) {
                listView.setAdapter(null);
                listView.setAdapter(listAdapter);
                listView.setRecyclerListener(listAdapter);
            // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature end
            }
            mQueryString = null;
        // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature start
            // Aurora xuyong 2016-01-11 added for aurora 2.0 new feature start
            if (listAdapter != null && listAdapter.getCount() <= 0) {
                encryptionTitle.setVisibility(View.VISIBLE);
                // Aurora xuyong 2016-01-12 deleted for aurora 2.0 new feature start
                //listView.setVisibility(View.GONE);
                // Aurora xuyong 2016-01-12 deleted for aurora 2.0 new feature end
            // Aurora xuyong 2016-01-12 added for aurora 2.0 new feature start
            } else {
                encryptionTitle.setVisibility(View.GONE);
                //listView.setVisibility(View.VISIBLE);
            // Aurora xuyong 2016-01-12 added for aurora 2.0 new feature end
            }
            ((TextView)(encryptionTitle.findViewById(R.id.aurora_conv_empty))).setText(R.string.aurora_no_msg_tip);
            // Aurora xuyong 2016-01-11 added for aurora 2.0 new feature end
        } else {
        // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature end
            AuroraActivity activityNE = AuroraConvListActivity.sAuroraConvListActivity;
            if (activityNE == null) {
                return false;
            }
            activityNE.getSearchViewGreyBackground().setVisibility(View.GONE);
            mQueryString = changedQueryString;
            Cursor cacheCursor = searchResultsCache.get(mQueryString);
            if (searchResultsCache != null && cacheCursor != null && !cacheCursor.isClosed()) {
                if (cacheCursor.getCount() > 0) {
                    // Aurora xuyong 2015-12-15 added for aurora 2.0 new feature start
                    // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature start
                    encryptionTitle.setVisibility(View.GONE);
                    // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature end
                    // Aurora xuyong 2015-12-15 added for aurora 2.0 new feature end
                    if (mAdapter == null) {
                        mAdapter = new MessageSearchListAdapter(AuroraConvListActivity.sContext, cacheCursor);
                    } else {
                        mAdapter.changeCursor(cacheCursor);
                        mAdapter.setQueryString(mQueryString);
                        mAdapter.setSearchMode(true);
                    }
                    // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature start
                    listView.setAdapter(mAdapter);
                    listView.setRecyclerListener(mAdapter);
                    // Aurora xuyong 2016-01-12 deleted for aurora 2.0 new feature start
                    //listView.setVisibility(View.VISIBLE);
                    // Aurora xuyong 2016-01-12 deleted for aurora 2.0 new feature end
                    // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature end
                } else {
                    // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature start
                    // Aurora xuyong 2016-01-12 deleted for aurora 2.0 new feature start
                    //listView.setVisibility(View.GONE);
                    // Aurora xuyong 2016-01-12 deleted for aurora 2.0 new feature end
                    // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature start
                    ((TextView)(encryptionTitle.findViewById(R.id.aurora_conv_empty))).setText(R.string.aurora_msgsearch_no_result_tip);
                    // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature end
                    // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature end
                }
                return true;
            }
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
        }
        
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }
    
    @Override
    public boolean quit(){
        onClose();
        mSearchMode = false;
        // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature start
        AuroraListView listView = getCurrentListView();
        ConversationListAdapter listAdapter = getCurrentAdapter();
        // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature start
        LinearLayout encryptionTitle = getCurrentEncrptionTitle();
        ((TextView)(encryptionTitle.findViewById(R.id.aurora_conv_empty))).setText(R.string.aurora_no_msg_tip);
        // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature end
        if (mAuroraTabWidget != null) {
            mAuroraTabWidget.getmScrollIconLinearLayout().setVisibility(View.VISIBLE);
        }
        // Aurora xuyong 2016-01-06 added for bug #18286 start
        if (mSearchBox != null) {
            mSearchBox.clearText();
        }
        // Aurora xuyong 2016-01-06 added for bug #18286 end
        if (mAuroraViewPager != null) {
            mAuroraViewPager.setCanScroll(true);
        }
        if (mFloatingButton != null) {
            // Aurora xuyong 2016-02-23 modified for bug #19581 start
            AnimUtils.scaleIn(mFloatingButton, 250, 0);
            //mFloatingButton.setVisibility(View.VISIBLE);
            // Aurora xuyong 2016-02-23 modified for bug #19581 end
        }
        // Aurora xuyong 2016-01-11 deleted for aurora 2.0 new feature start
        //encrptionTitle.setVisibility(View.GONE);
        // Aurora xuyong 2016-01-11 deleted for aurora 2.0 new feature end
        // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature end
        if (mQueryAsyncTask != null && mQueryAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
            mQueryAsyncTask.cancel(true);
        }
        if (searchResultsCache != null) {
            Iterator it = searchResultsCache.entrySet().iterator();;
            while (it.hasNext()) {
                Map.Entry<String, Cursor> entry = (Map.Entry<String, Cursor>)it.next();
                Cursor cursor = entry.getValue();
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
            searchResultsCache.clear();
        }
        if (mAdapter != null) {
            mAdapter.destroy();
            mAdapter = null;
        }
        mQueryString = null;
        setListViewWatcher(null);
        // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature start
        if (listView != null) {
            if (listView.getHeaderViewsCount() == 0) {
                listView.setAdapter(null);
                listView.setAdapter(listAdapter);
                listView.setRecyclerListener(listAdapter);
        // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature end
            }
            // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature start
            // Aurora xuyong 2016-01-12 deleted for aurora 2.0 new feature start
            //listView.setVisibility(View.VISIBLE);
            // Aurora xuyong 2016-01-12 deleted for aurora 2.0 new feature end
            // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature end
            // Aurora xuyong 2015-01-11 added for aurora 2.0 new feature start
            if (listAdapter != null && listAdapter.getCount() <= 0) {
                encryptionTitle.setVisibility(View.VISIBLE);
                // Aurora xuyong 2016-01-12 deleted for aurora 2.0 new feature start
                //listView.setVisibility(View.GONE);
                // Aurora xuyong 2016-01-12 deleted for aurora 2.0 new feature end
            // Aurora xuyong 2016-01-12 added for aurora 2.0 new feature start
            } else {
                encryptionTitle.setVisibility(View.GONE);
                //listView.setVisibility(View.VISIBLE);
            // Aurora xuyong 2016-01-12 deleted for aurora 2.0 new feature end
            }
            // Aurora xuyong 2015-01-11 added for aurora 2.0 new feature end
        }
        AuroraActivity activity = AuroraConvListActivity.sAuroraConvListActivity;
        if (activity != null) {
            activity.setMenuEnable(true);
        }
        return true;
    }
    // Aurora xuyong 2016-01-06 added for bug #18285 start
    private AuroraSearchView mSearchBox;
    // Aurora xuyong 2016-01-06 added for bug #18285 end
    public void gotoSearchMode(AuroraActionBar actionBar) {
        // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature start
        if (!mSearchMode) {
            AuroraActivity activity = (AuroraActivity)AuroraConvListActivity.sAuroraConvListActivity;
            if (activity == null) {
                return;
            }
            // Aurora xuyong 2016-01-06 modified for bug #18285 start
            mSearchBox = (AuroraSearchView) actionBar.getAuroraActionbarSearchView();
            if (mSearchBox == null) {
            // Aurora xuyong 2016-01-06 modified for bug #18285 end
                return;
            }
            if (mAuroraTabWidget != null) {
                mAuroraTabWidget.getmScrollIconLinearLayout().setVisibility(View.GONE);
            }
            if (mAuroraViewPager != null) {
                mAuroraViewPager.setCanScroll(false);
            }
            if (mFloatingButton != null) {
                // Aurora xuyong 2016-02-23 modified for bug #19581 start
                AnimUtils.scaleOut(mFloatingButton, 250);
                //mFloatingButton.setVisibility(View.GONE);
                // Aurora xuyong 2016-02-23 modified for bug #19581 end
            }
            activity.showSearchviewLayout();
            mSearchMode = true;
            Contact.removeAllConvListeItemListener();
            activity.setMenuEnable(false);
            // Aurora xuyong 2016-01-06 modified for bug #18285 start
            mSearchBox.setInputType(EditorInfo.TYPE_CLASS_TEXT);
            mSearchBox.onActionViewExpanded();
            mSearchBox.setOnQueryTextListener(this);
            mSearchBox.setOnCloseListener(this);
            // Aurora xuyong 2016-01-06 modified for bug #18285 end
            activity.setOnSearchViewQuitListener(this);

            setListViewWatcher(new ConvFragment.ListViewWatcher() {

                @Override
                public void listViewChanged(boolean isChange) {
                    // TODO Auto-generated method stub
                    if (isChange && mSearchMode) {
                        AuroraActivity activity = AuroraConvListActivity.sAuroraConvListActivity;
                        if (activity != null) {
                            activity.hideSearchviewLayout();
                        }
                    }
                }
            });
        }
        // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature end
    }

    private Runnable mQueryRunnable = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            mContentChanged = true;
            startQuery(PERSONAL_TAB);
            // Aurora xuyong 2016-01-19 added for bug #18457 start
            mNotifyContentChanged = true;
            startQuery(NOTIFICATION_TAB);
            // Aurora xuyong 2016-01-19 added for bug #18457 end
        }
    };

    // Aurora xuyong 2016-01-19 deleted for bug #18457 start
    /*private Runnable mNotifyQueryRunnable = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            mNotifyContentChanged = true;
            startQuery(NOTIFICATION_TAB);
        }
    };*/
    // Aurora xuyong 2016-01-19 deleted for bug #18457 end

    public void startEncryptionAsyncQuery() {
        try {
            Conversation.startQueryForAll(mQueryHandler, THREAD_LIST_QUERY_TOKEN, true);
        } catch (SQLiteException e) {
            Context context = AuroraConvListActivity.sContext;
            if (context != null) {
                SqliteWrapper.checkSQLiteException(context, e);
            }
        }
    }

    private void startAsyncQuery() {
        try {
            Conversation.startQueryForAll(mQueryHandler, THREAD_LIST_QUERY_TOKEN, null);
        } catch (SQLiteException e) {
            Context context = AuroraConvListActivity.sContext;
            if (context != null) {
                SqliteWrapper.checkSQLiteException(context, e);
            }
        }
    }

    private void startAsyncQuery(int tabIndex) {
        try {
            String selection = null;
            int token = 0;
            if (tabIndex == PERSONAL_TAB) {
                selection = "notification_index = 0";
                token = THREAD_LIST_QUERY_TOKEN;
            } else if (tabIndex == NOTIFICATION_TAB) {
                selection = "notification_index = 1";
                token = THREAD_LIST_NOTIFY_QUERY_TOKEN;
            }
            Conversation.startQueryForAll(mQueryHandler, token, selection);
        } catch (SQLiteException e) {
            Context context = AuroraConvListActivity.sContext;
            if (context != null) {
                SqliteWrapper.checkSQLiteException(context, e);
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
                if (activity != null) {
                if (Recycler.checkForThreadsOverLimit(AuroraConvListActivity.sContext)) {
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            Intent intent = new Intent(AuroraConvListActivity.sContext,
                                    WarnOfStorageLimitsActivity.class);
                            AuroraConvListActivity.sContext.startActivity(intent);
                        }
                    }, 2000);
                }
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        markCheckedMessageLimit();
                    }
                });
                }
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
    // Aurora xuyong 2016-01-27 added for aurora 2.0 new feature start
    public static final String CONV_NOTIFICATION = "conv_notification";
    public static final String CONV_NOTIFICATION_INDEX = "conv_notification_index";
    private int mNotificaitonIndex = 0;
    // Aurora xuyong 2016-03-10 added for bug #20942 start
    private int mFromConvTabIndex = -1;
    // Aurora xuyong 2016-03-10 added for bug #20942 end
    private boolean getNeedScrollViewPager(Context context) {
        // Aurora xuyong 2016-03-10 modified for bug #20942 start
        if (mFromConvTabIndex != -1) {
            mNotificaitonIndex = mFromConvTabIndex;
            return true;
        } else {
            mNotificaitonIndex = getNotificationIndex(context);
        }
        // Aurora xuyong 2016-03-10 modified for bug #20942 end
        context.getSharedPreferences(CONV_NOTIFICATION, AuroraActivity.MODE_PRIVATE).edit().putInt(ConvFragment.CONV_NOTIFICATION_INDEX,
                mCurrentTabIndex).commit();
        return mNotificaitonIndex != mCurrentTabIndex;
    }

    private int getNotificationIndex(Context context) {
        return context.getSharedPreferences(CONV_NOTIFICATION, AuroraActivity.MODE_PRIVATE).getInt(CONV_NOTIFICATION_INDEX, 0);
    }
    // Aurora xuyong 2016-01-27 added for aurora 2.0 new feature end
    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            siExpiredCheck.startExpiredCheck();
        }
        // Aurora xuyong 2016-01-19 added for bug #18457 start
        Context context = AuroraConvListActivity.sContext;
        // Aurora xuyong 2016-01-27 added for aurora 2.0 new feature start
        if (getNeedScrollViewPager(context)) {
            mAuroraViewPager.setCurrentItem(mNotificaitonIndex);
        }
        // Aurora xuyong 2016-01-27 added for aurora 2.0 new feature end
        if (context != null) {
            MessagingNotification.cancelNotification(context,
                    SmsRejectedReceiver.SMS_REJECTED_NOTIFICATION_ID);
        }
        DraftCache.getInstance().addOnDraftChangedListener(this);
        mNeedToMarkAsSeen = true;

        startQuery(PERSONAL_TAB);
        startQuery(NOTIFICATION_TAB);
        if (!Conversation.loadingThreads()) {
            Contact.invalidateCache();
        }
        if (mSearchMode) {
            AuroraActivity activity = AuroraConvListActivity.sAuroraConvListActivity;
            if (activity == null) {
                return;
            }
            activity.setMenuEnable(false);
            // Aurora xuyong 2016-01-06 modified for bug #18285 start
            if (mSearchBox != null) {
                String searchText = mSearchBox.getQuery().toString();
                this.onQueryTextChange(searchText);
            }
            // Aurora xuyong 2016-01-06 modified for bug #18285 end
        }
        // Aurora xuyong 2016-01-19 added for bug #18457 end
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        // Aurora xuyong 2016-01-19 added for bug #18457 start
        /*Context context = AuroraConvListActivity.sContext;
        if (context != null) {
            MessagingNotification.cancelNotification(context,
                    SmsRejectedReceiver.SMS_REJECTED_NOTIFICATION_ID);
        }
        DraftCache.getInstance().addOnDraftChangedListener(this);
        mNeedToMarkAsSeen = true;

        startQuery(PERSONAL_TAB);
        startQuery(NOTIFICATION_TAB);
        if (!Conversation.loadingThreads()) {
            Contact.invalidateCache();
        }
        if (mSearchMode) {
            AuroraActivity activity = AuroraConvListActivity.sAuroraConvListActivity;
            if (activity == null) {
                return;
            }
            activity.setMenuEnable(false);
            // Aurora xuyong 2016-01-06 modified for bug #18285 start
            if (mSearchBox != null) {
                String searchText = mSearchBox.getQuery().toString();
                this.onQueryTextChange(searchText);
            }
            // Aurora xuyong 2016-01-06 modified for bug #18285 end
        }*/
        // Aurora xuyong 2016-01-19 added for bug #18457 end
    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        DraftCache.getInstance().removeOnDraftChangedListener(this);
        // Aurora xuyong 2016-01-27 added for aurora 2.0 new feature start
        AuroraConvListActivity.sContext.getSharedPreferences(ConvFragment.CONV_NOTIFICATION, AuroraActivity.MODE_PRIVATE).edit().putInt(ConvFragment.CONV_NOTIFICATION_INDEX,
                mCurrentTabIndex).commit();
        // Aurora xuyong 2016-01-27 added for aurora 2.0 new feature end
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            siExpiredCheck.stopExpiredCheck();
        }

        if (MmsApp.mGnHideEncryption && mHideEncryp != ReadPopTag(AuroraConvListActivity.sContext, HIDEENCRYPTION)) {
            WritePopTag(AuroraConvListActivity.sContext, HIDEENCRYPTION, mHideEncryp);
        }
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
        if (mNotifyListAdapter != null && mNotifyListAdapter.getCursor() != null) {
            mNotifyListAdapter.getCursor().close();
        }
        if (mAdapter != null) {
            mAdapter.destroy();
            mAdapter = null;
        }
        if (mConvTabPageAdapter != null) {
            mConvTabPageAdapter = null;
            mAuroraViewPager.setAdapter(null);
        }
        super.onDestroy();
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            siExpiredCheck.stopSiExpiredCheckThread();
        }
        isEncryptionList = false;
        isNotifyEncryptionList = false;
        if (MmsApp.mGnMultiSimMessage && mConvObserver != null) {
            AuroraConvListActivity.sContext.getContentResolver().unregisterContentObserver(mConvObserver);
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
                        Context context = AuroraConvListActivity.sContext;
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
                    if (!TextUtils.isEmpty(conversationsIds)) {
                        Conversation.startDelete(mQueryHandler, DELETE_CONVERSATION_TOKEN,
                                true, conversationsIds.toString(), "OneLast");
                    }
                }
            }).start();
        }
    }

    private boolean getInit(int tabIndex) {
        if (tabIndex == PERSONAL_TAB) {
            return isInit;
        } else if (tabIndex == NOTIFICATION_TAB) {
            return isNotifyInit;
        }
        return false;
    }

    private void setInit(int tabIndex, boolean status) {
        if (tabIndex == PERSONAL_TAB) {
            isInit = status;
        } else if (tabIndex == NOTIFICATION_TAB) {
            isNotifyInit = status;
        }
    }

    private boolean getContentChanged(int tabIndex) {
        if (tabIndex == PERSONAL_TAB) {
            return mContentChanged;
        } else if (tabIndex == NOTIFICATION_TAB) {
            return mNotifyContentChanged;
        }
        return false;
    }

    private void setContentChanged(int tabIndex, boolean status) {
        if (tabIndex == PERSONAL_TAB) {
            mContentChanged = status;
        } else if (tabIndex == NOTIFICATION_TAB) {
            mNotifyContentChanged = status;
        }
    }

    private boolean getListChange(int tabIndex) {
        if (tabIndex == PERSONAL_TAB) {
            return mListChange;
        } else if (tabIndex == NOTIFICATION_TAB) {
            return mNotifyListChange;
        }
        return false;
    }

    private void setListChange(int tabIndex, boolean status) {
        if (tabIndex == PERSONAL_TAB) {
            mListChange = status;
        } else if (tabIndex == NOTIFICATION_TAB) {
            mNotifyListChange = status;
        }
    }

    private ConversationListAdapter getAdapter(int tabIndex) {
        if (tabIndex == PERSONAL_TAB) {
            return mListAdapter;
        } else if (tabIndex == NOTIFICATION_TAB) {
            return mNotifyListAdapter;
        }
        return null;
    }
    // Aurora xuyong 2015-12-15 added for aurora 2.0 new feature start
    private ConversationListAdapter getCurrentAdapter() {
        if (mCurrentTabIndex == PERSONAL_TAB) {
            return mListAdapter;
        } else if (mCurrentTabIndex == NOTIFICATION_TAB) {
            return mNotifyListAdapter;
        }
        return null;
    }
    // Aurora xuyong 2015-12-15 added for aurora 2.0 new feature end
    private Map<Integer, Long> getThreadsMap(int tabIndex) {
        if (tabIndex == PERSONAL_TAB) {
            return mThreadsMap;
        } else if (tabIndex == NOTIFICATION_TAB) {
            return mNotifyThreadsMap;
        }
        return null;
    }

    private HashSet<Long> getSetEncryption(int tabIndex) {
        if (tabIndex == PERSONAL_TAB) {
            return mSetEncryption;
        } else if (tabIndex == NOTIFICATION_TAB) {
            return mNotifySetEncryption;
        }
        return null;
    }

    private boolean getEncrptionList(int tabIndex) {
        if (tabIndex == PERSONAL_TAB) {
            return isEncryptionList;
        } else if (tabIndex == NOTIFICATION_TAB) {
            return isNotifyEncryptionList;
        }
        return false;
    }
    // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature start
    private LinearLayout getEncrptionTitle(int tabIndex) {
    // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature end
        if (tabIndex == PERSONAL_TAB) {
            return mEncryptionTitle;
        } else if (tabIndex == NOTIFICATION_TAB) {
            return mNotifyEncryptionTitle;
        }
        return null;
    }
    // Aurora xuyong 2015-12-15 added for aurora 2.0 new feature start
    // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature start
    private LinearLayout getCurrentEncrptionTitle() {
    // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature end
        if (mCurrentTabIndex == PERSONAL_TAB) {
            return mEncryptionTitle;
        } else if (mCurrentTabIndex == NOTIFICATION_TAB) {
            return mNotifyEncryptionTitle;
        }
        return null;
    }
    // Aurora xuyong 2015-12-15 added for aurora 2.0 new feature end

    private void operateAfterChangeCursor(Cursor result, int tabIndex) {
        ConversationListAdapter adapter = getAdapter(tabIndex);
        // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature start
        ListView listView = getCurrentListViewByIndex(tabIndex);
        // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature end
        // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature start
        boolean encryptionList = getEncrptionList(tabIndex);
        LinearLayout encryptionTitle = getEncrptionTitle(tabIndex);
        // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature end
        if (!mSearchMode) {
            if (adapter.isEmpty()) {
                // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature start
                if (encryptionList) {
                    ((TextView)(encryptionTitle.findViewById(R.id.aurora_conv_empty))).setText(R.string.no_encryption_conversations);
                } else {
                    ((TextView)(encryptionTitle.findViewById(R.id.aurora_conv_empty))).setText(R.string.aurora_no_msg_tip);
                }
                encryptionTitle.setVisibility(View.VISIBLE);
                // Aurora xuyong 2016-01-12 deleted for aurora 2.0 new feature start
                //listView.setVisibility(View.GONE);
                // Aurora xuyong 2016-01-12 deleted for aurora 2.0 new feature end
                //encryptionTitle.setVisibility(View.GONE);
                // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature end
            } else {
                if (!getInit(tabIndex)) {
                    setInit(tabIndex, true);
                    listView.setOnKeyListener(mThreadListKeyListener);
                    Context context = AuroraConvListActivity.sContext;
                    if (context == null) {
                        return;    
                    }
                    adapter.notifyDataSetChanged();
                    listView.setAdapter(adapter);
                    listView.setRecyclerListener(adapter);
                    listView.setOnItemLongClickListener(ConvFragment.this);
                }
                // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature start
                if (encryptionList) {
                    ((TextView)(encryptionTitle.findViewById(R.id.aurora_conv_empty))).setText(R.string.gn_encryption);
                // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature end
                    encryptionTitle.setVisibility(View.VISIBLE);
                } else {
                    encryptionTitle.setVisibility(View.GONE);
                }
                // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature start
                //emptyView.setVisibility(View.GONE);
                // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature end
                // Aurora xuyong 2016-01-12 deleted for aurora 2.0 new feature start
                //listView.setVisibility(View.VISIBLE);
                // Aurora xuyong 2016-01-12 deleted for aurora 2.0 new feature end
            }
        }
        if (!Conversation.isInitialized()) {
            Conversation.init(AuroraConvListActivity.sContext);
        }else{
            if (result != null && !result.isClosed()) {
                if (result.moveToFirst()) {
                    Conversation.removeInvalidCache(result, adapter.getCount());
                }
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
        if(getContentChanged(tabIndex)) {
            if(!getListChange(tabIndex) && null != mActionBatchHandler) {
                if (result.getCount() > getThreadsMap(tabIndex).size()) {
                    getSetEncryption(tabIndex).clear();
                    getThreadsMap(tabIndex).clear();
                    initThreadsMap(tabIndex);
                }
                mActionBatchHandler.refreshDataSet();
                mActionBatchHandler.updateUi();
                if(result.getCount() != mActionBatchHandler.getSelected().size()) {
                    setListChange(tabIndex, true);
                }
            }
            setContentChanged(tabIndex, false);
        }
        // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature start
        if (!encryptionList && MmsApp.mGnHideEncryption && MmsApp.mGnSmartGuide && ReadPopTag(AuroraConvListActivity.sContext, FIRSTENCRYPTION)
        // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature end
                && !ReadPopTag(AuroraConvListActivity.sContext, ISOPEN_STRING)) {
            if (queryHasEncryption()) {
                OpenPopLayout();
                WritePopTag(AuroraConvListActivity.sContext, ISOPEN_STRING);
            }
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
            if (cursor == null) {
                return;
            }
            switch (token) {
                case THREAD_LIST_QUERY_TOKEN:
                    // Aurora xuyong 2016-01-19 added for bug #18254 start
                    // Aurora xuyong 2016-01-26 modified for bug #18254 start
                    Utils.updateWidget(AuroraConvListActivity.sContext);
                    // Aurora xuyong 2016-01-26 modified for bug #18254 end
                    // Aurora xuyong 2016-01-19 added for bug #18254 end
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
                                        Context context = AuroraConvListActivity.sContext;
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
                        mListAdapter.changeCountBeforeDataChanged(transCursor == null ? 0 : transCursor.getCount());
                        mListAdapter.changeCursor(transCursor);
                        operateAfterChangeCursor(transCursor, PERSONAL_TAB);
                    }
                    break;
                case THREAD_LIST_NOTIFY_QUERY_TOKEN:
                    final Cursor transNotifyCursor = cursor;
                    if (mNotifyInitStatu != INIT_ALL_DONE) {
                        mNotifySingleThreadExecutor.execute(new Runnable() {

                            @Override
                            public void run() {
                                int initCount = INIT_ITEM_COUNT;
                                if (transNotifyCursor == null || transNotifyCursor.isClosed() || transNotifyCursor.getCount() <= 0) {
                                    Message msg = mHandler.obtainMessage(CHANGE_NOTIFY_MC1_CURSOR);
                                    msg.sendToTarget();
                                    return;
                                }
                                int count = transNotifyCursor.getCount();
                                int columnCount = transNotifyCursor.getColumnCount();
                                MatrixCursor mc1Notify = new MatrixCursor(Conversation.ALL_THREADS_PROJECTION, initCount);
                                transNotifyCursor.moveToPosition(-1);
                                if (transNotifyCursor.moveToFirst() && !transNotifyCursor.isAfterLast() && !transNotifyCursor.isBeforeFirst()) {
                                    Object[] firstItemDetail = new Object[columnCount];
                                    for (int i = 0; i < columnCount; i++) {
                                        try {
                                            firstItemDetail[i] = transNotifyCursor.getString(i);
                                        } catch (CursorIndexOutOfBoundsException e) {
                                            Log.e("CIOEC", "current position = " + i + ", transCursor.getColumnCount() = " + transNotifyCursor.getColumnCount());
                                        }
                                    }
                                    mc1Notify.addRow(firstItemDetail);
                                    initCount--;
                                    while (transNotifyCursor.moveToNext() && initCount > 0 && !transNotifyCursor.isBeforeFirst() && !transNotifyCursor.isAfterLast()) {
                                        Object[] itemDetail = new Object[columnCount];
                                        for (int i = 0; i < columnCount; i++) {
                                            try {
                                                itemDetail[i] = transNotifyCursor.getString(i);
                                            } catch (CursorIndexOutOfBoundsException e) {
                                                Log.e("CIOEC", "current position = " + i + ", transCursor.getColumnCount() = " + transNotifyCursor.getColumnCount());
                                            }
                                        }
                                        mc1Notify.addRow(itemDetail);
                                        initCount--;
                                    }
                                    if (mc1Notify.moveToFirst() && !mc1Notify.isAfterLast() && !mc1Notify.isBeforeFirst()) {
                                        Context context = AuroraConvListActivity.sContext;
                                        if (context != null) {
                                            try {
                                                Conversation.from(context, mc1Notify, true);
                                            } catch (CursorIndexOutOfBoundsException e) {

                                            }
                                            while(mc1Notify.moveToNext() && !mc1Notify.isBeforeFirst() && !mc1Notify.isAfterLast()) {
                                                try {
                                                    Conversation.from(context, mc1Notify, true);
                                                } catch (CursorIndexOutOfBoundsException e) {

                                                }
                                            }
                                        }
                                    }
                                }
                                transNotifyCursor.moveToFirst();
                                Message msg = mHandler.obtainMessage(CHANGE_NOTIFY_MC1_CURSOR);
                                if(mc1Notify != null){
                                    mc1Notify.close();
                                }
                                msg.obj = transNotifyCursor;
                                msg.sendToTarget();
                            }
                        });
                    } else {
                        mNotifyListAdapter.changeCountBeforeDataChanged(transNotifyCursor == null ? 0 : transNotifyCursor.getCount());
                        mNotifyListAdapter.changeCursor(transNotifyCursor);
                        operateAfterChangeCursor(transNotifyCursor, NOTIFICATION_TAB);
                    }
                    break;
                case THREAD_LIST_LOCKED_QUERY_TOKEN:
                    lockFlag = false;
                    cursor.close();
                    break;
                case THREAD_LIST_FAVORITE_QUERY_TOKEN:
                    starFlag = (cursor != null && cursor.getCount() > 0);
                    boolean isAll = false;
                    if (!isEncryptionList && mAllSelectedThreadIds.size() == mListView.getCount()) {
                        isAll = true;
                    }
                    final boolean isAllSelected = isAll;
                    long threadId = (Long)cookie;
                    if (isAllSelected){
                        Activity activity = AuroraConvListActivity.sAuroraConvListActivity;
                        if (activity != null) {
                            confirmDeleteGnThreadDialog(
                                    new GnDeleteThreadListener(mListView, -1, mQueryHandler,
                                            activity), isAllSelected, starFlag,
                                            activity);
                        }
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
                        Activity activity = AuroraConvListActivity.sAuroraConvListActivity;
                        if (activity != null) {
                            confirmDeleteGnThreadDialog(
                                    new GnDeleteThreadListener(mListView, tempThreadId, mQueryHandler,
                                            activity), isAllSelected, starFlag,
                                            activity);
                        }
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
                    auroraDeleteConversations(mListView, onethread, mQueryHandler, AuroraConvListActivity.sContext, true);
                    break;
                case THREAD_LIST_FAVORITE_QUERY_ALL_TOKEN:
                    starFlag = (cursor != null && cursor.getCount() > 0);
                    Activity activity = AuroraConvListActivity.sAuroraConvListActivity;
                    if (activity != null) {
                    confirmDeleteGnThreadDialog(
                            new GnDeleteThreadListener(mListView, -1, mQueryHandler,
                                    activity), true, starFlag,
                                    activity);
                    }
                    cursor.close();
                    break;
                default:
            }
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            mIsDeleting = false;
            CBMessagingNotification.updateNewMessageIndicator(AuroraConvListActivity.sContext);
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
                    ConvFragment.this.startQuery();
                            Toast.makeText(AuroraConvListActivity.sContext, R.string.conversation_has_deleted, Toast.LENGTH_SHORT).show();
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
                    ConvFragment.this.startQuery();
                    if (progress()) {
                        Toast.makeText(AuroraConvListActivity.sContext, R.string.conversation_has_deleted, Toast.LENGTH_SHORT).show();
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
                if (dialog != null) {
                    dialog.dismiss();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                dialog = null;
            }
        }
    }

    public static AuroraAlertDialog getDelAllDialog(){
        if (mCurrentTabIndex == PERSONAL_TAB) {
            return mDelAllDialog;
        } else if (mCurrentTabIndex == NOTIFICATION_TAB) {
            return mNotifyDelAllDialog;
        }
        return null;
    }

    public static AuroraAlertDialog getDelAllDialog(int tabIndex){
        if (tabIndex == PERSONAL_TAB) {
            return mDelAllDialog;
        } else if (tabIndex == NOTIFICATION_TAB) {
            return mNotifyDelAllDialog;
        }
        return null;
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
                                    Context context = AuroraConvListActivity.sContext;
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
                    mNotifyListAdapter.notifyDataSetChanged();
                }
            });
        }
        mNeedSetDataChanged = true;
    }

    public AuroraListView getListView() {
        if (mListView == null) {
            mListView = (AuroraListView)mFragmentView.findViewById(R.id.aurora_conv_list);
        }
        return mListView;
    }

    public AuroraListView getNotifyListView() {
        if (mNotifyListView == null) {
            mNotifyListView = (AuroraListView)mNotifyFragmentView.findViewById(R.id.aurora_conv_list);
        // Aurora xuyong 2016-01-11 modified for aurora 2.0 new feature start
        }
        return mNotifyListView;
    }
    // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature start
    public AuroraListView getCurrentListViewByIndex(int tabIndex) {
    // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature end
        if (tabIndex == PERSONAL_TAB) {
            return getListView();
        } else if (tabIndex == NOTIFICATION_TAB) {
            return getNotifyListView();
        }
        return null;
    }
    // Aurora xuyong 2015-12-15 added for aurora 2.0 new feature start
    public AuroraListView getCurrentListView() {
        if (mCurrentTabIndex == PERSONAL_TAB) {
            return getListView();
        } else if (mCurrentTabIndex == NOTIFICATION_TAB) {
            return getNotifyListView();
        }
        return null;
    }
    // Aurora xuyong 2015-12-15 added for aurora 2.0 new feature end
    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        // TODO Auto-generated method stub
        if(null != mActionBatchHandler && mActionBatchHandler.isInSelectionMode()) {
            // Aurora xuyong 2016-03-03 modified for bug #20197 start
            AuroraCheckBox mCheckBox = (AuroraCheckBox) v.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
            // Aurora xuyong 2016-03-03 modified for bug #20197 end
            if (mCheckBox == null) {
                return;
            }
            boolean isChecked = mCheckBox.isChecked();
            mCheckBox.setChecked(!isChecked);
            mActionBatchHandler.getSelectionManger().toggle(id);
        } else if (mSearchMode && mQueryString != null) {
            // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature start
            Cursor cursor  = (Cursor) getCurrentListViewByIndex(mCurrentTabIndex).getItemAtPosition(position);
            // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature end
            if (cursor == null || cursor.isClosed()) {
                return;
            } 
            try {
                long threadId = cursor.getLong(cursor.getColumnIndex("thread_id"));
                long sourceId = cursor.getLong(cursor.getColumnIndex("_id"));
                Intent onClickIntent = new Intent(AuroraConvListActivity.sContext, ComposeMessageActivity.class);
                if (searchResultsCache != null && searchResultsCache.containsKey(mQueryString)) {
                    searchResultsCache.remove(mQueryString);
                }
                onClickIntent.putExtra("highlight", mActivityQueryString);
                onClickIntent.putExtra("select_id", sourceId);
                onClickIntent.putExtra("thread_id", threadId);
                startActivity(onClickIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature start
            Cursor cursor  = (Cursor) getCurrentListViewByIndex(mCurrentTabIndex).getItemAtPosition(position);
            // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature end
            if (cursor == null || cursor.isClosed()) {
                return;
            }
            if (MmsApp.mGnMessageSupport) {
                try {
                    // 0 : threadid, 9 type
                    long threadId = cursor.getLong(cursor.getColumnIndex("_id"));
                    int type = cursor.getInt(cursor.getColumnIndex("type"));
                    if (MmsApp.sHasPrivacyFeature) {
                        openThread(threadId, type, cursor.getLong(cursor.getColumnIndex("is_privacy")));
                    } else {
                        openThread(threadId, type);
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
            Context context = AuroraConvListActivity.sContext;
            if (null != context) {
                Conversation conv = null;
                try {
                    conv = Conversation.from(context, cursor);
                } catch (CursorIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
                if (conv != null) {
                    long tid = conv.getThreadId();
                    if (MmsApp.sHasPrivacyFeature) {
                        openThread(tid, conv.getType(), conv.getPrivacy());
                    } else {
                        openThread(tid, conv.getType());
                    }
                }
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
            int position, long id) {
        // TODO Auto-generated method stub
        if (mSearchMode) {
            return false;
        }
        if(null == mActionBatchHandler) {
            initThreadsMap(mCurrentTabIndex);
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
    
    public boolean isBottomMenuShowing() {
        if (mBottomAuroraMenu != null && mBottomAuroraMenu.isShowing()) {
            return true;
        }
        return false;
    }

    private void onBottomMenuDismiss() {
        int tabIndex = mCurrentTabIndex;
        // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature start
        AuroraListView listView = getCurrentListViewByIndex(tabIndex);
        // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature end
        ConversationListAdapter adapter = getAdapter(tabIndex);
        AuroraAlertDialog delDialog = getDelAllDialog(tabIndex);
        if (adapter != null) {
            adapter.showCheckBox(false);
            //adapter.setCheckBoxAnim(false);
            adapter.updateAllCheckBox(0);
        }
        if (mActionBatchHandler != null) {
            mActionBatchHandler.destroyAction();
        }
        if (adapter != null) {
            if (mNeedSetDataChanged) {
                adapter.notifyDataSetChanged();
            }
            mNeedSetDataChanged = true;
        }
        Activity activity = AuroraConvListActivity.sAuroraConvListActivity;
        if (activity != null) {
            ((AuroraActivity)activity).setMenuEnable(true);
        }
        mActionBatchHandler = null;
        if(null != delDialog && !delDialog.isShowing()) {
            setListChange(tabIndex, false);
        }
        if (getThreadsMap(tabIndex) != null) {
            getThreadsMap(tabIndex).clear();
        }
        if (getSetEncryption(tabIndex) != null) {
            getSetEncryption(tabIndex).clear();
        }
    }

    protected void initActionBatchHandler() {
        final int tabIndex = mCurrentTabIndex;
        Activity activity = AuroraConvListActivity.sAuroraConvListActivity;
        if (null != activity) {
            mActionBatchHandler = new AuroraActionBatchHandler<Long>(AuroraConvListActivity.sAuroraConvListActivity, mAuroraActionBar) {
                
                @Override
                public void enterSelectionMode(boolean autoLeave, Long itemPressing) {
                    // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature start
                    AuroraListView listView = getCurrentListViewByIndex(tabIndex);
                    // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature end
                    ConversationListAdapter adapter = getAdapter(tabIndex);
                    mAuroraViewPager.setCanScroll(false);
                    if (mFloatingButton != null) {
                        mFloatingButton.setVisibility(View.GONE);
                    }
                    // Aurora xuyong 2016-02-04 modified for aurora 2.0 new feature start
                    if (mAuroraTabWidget != null) {
                        mAuroraTabWidget.getmScrollIconLinearLayout().setVisibility(View.GONE);
                    }
                    // Aurora xuyong 2016-02-04 modified for aurora 2.0 new feature end
                    adapter.showCheckBox(true);
                    //adapter.setCheckBoxAnim(true);
                    AuroraConvListActivity.sAuroraConvListActivity.setMenuEnable(false);
                    super.enterSelectionMode(autoLeave, itemPressing);
                    if (mNeedSetDataChanged) {
                        adapter.notifyDataSetChanged();
                    }
                    mNeedSetDataChanged = true;
                }
    
                @Override
                public Set getDataSet() {
                    // TODO Auto-generated method stub
                    Set<Long> dataSet = new HashSet<Long>(getThreadsMap(tabIndex).size());
                    for(int i = 0; i < getThreadsMap(tabIndex).size(); i++)
                        dataSet.add(getThreadsMap(tabIndex).get(i));
                    return dataSet;
                }

                @Override
                public void leaveSelectionMode() {
                    mAuroraViewPager.setCanScroll(true);
                    if (mFloatingButton != null) {
                        // Aurora xuyong 2016-02-23 modified for bug #19581 start
                        AnimUtils.scaleIn(mFloatingButton, 250, 0);
                        //mFloatingButton.setVisibility(View.VISIBLE);
                        // Aurora xuyong 2016-02-23 modified for bug #19581 end
                    }
                    // Aurora xuyong 2016-02-04 modified for aurora 2.0 new feature start
                    if (mAuroraTabWidget != null) {
                        mAuroraTabWidget.getmScrollIconLinearLayout().setVisibility(View.VISIBLE);
                    }
                    // Aurora xuyong 2016-02-04 modified for aurora 2.0 new feature end
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
                    // Aurora xuyong 2016-01-18 added for aurora 2.0 new feature start
                    mBottomAuroraMenu.setBottomMenuItemEnable(0, mSelectCount == 0 ? false : true);
                    // Aurora xuyong 2016-01-18 added for aurora 2.0 new feature end
                    mBottomAuroraMenu.setBottomMenuItemEnable(1, mSelectCount == 0 ? false : true);
                }

                @Override
                public void updateListView(int allShow) {
                    ConversationListAdapter adapter = getAdapter(tabIndex);
                    adapter.updateAllCheckBox(allShow);
                    if (mNeedSetDataChanged) {
                        adapter.notifyDataSetChanged();
                    }
                    mNeedSetDataChanged = true;
                }
                
                public void bindToAdapter(GnSelectionManager<Long> selectionManager) {
                    // TODO Auto-generated method stub
                    ConversationListAdapter adapter = getAdapter(tabIndex);
                    adapter.setSelectionManager(selectionManager);
                    if (selectionManager != null) {
                        selectionManager.setAdapter(adapter);
                    }
                }
            };
            mActionBatchHandler.setHandler(mHandler);
        }
    }

    AuroraActionBar mAuroraActionBar;
    
    private void initActionBatchMode() {
         AuroraActivity activity = AuroraConvListActivity.sAuroraConvListActivity;
         // Aurora xuyong 2016-03-11 modified for bug #20134 start
         if (activity == null || activity.isFinishing()) {
         // Aurora xuyong 2016-03-11 modified for bug #20134 end
             return;
         }
         mAuroraActionBar = activity.getAuroraActionBar();
         // Aurora xuyong 2016-01-14 modified for aurora 2.0 new feature start
         activity.setAuroraBottomBarMenuCallBack(mAuroraMenuCallBack); //必须写在布局前
         // Aurora xuyong 2016-01-14 modified for aurora 2.0 new feature end
         int menuId = R.menu.aurora_list_menu_delete;
         // Aurora xuyong 2016-01-07 modified for bug #18232 start
         mAuroraActionBar.initActionBottomBarMenu(menuId, 2);
         // Aurora xuyong 2016-01-07 modified for bug #18232 end
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
    // Aurora xuyong 2016-01-07 added for bug #18232 start
    private void markConvHaveReaded(Context context, ArrayList<Long> list) {
        int markCount = list.size();
        for (int i = 0; i < markCount; i++) {
            // Aurora xuyong 2016-01-21 modified for bug #18509 start
            long threadId = list.get(i);
            Conversation conv = Conversation.get(context, list.get(i), true);
            if (conv.hasUnreadMessages()) {
                markAsRead(context, threadId);
            }
            // Aurora xuyong 2016-01-21 modified for bug #18509 end
        }
        // Aurora xuyong 2016-03-28 added for bug #21801 start
        MessagingNotification.nonBlockingUpdateNewMessageIndicator(context, false, false);
        // Aurora xuyong 2016-03-28 added for bug #21801 end
    }
    // Aurora xuyong 2016-01-21 added for bug #18509 start
    private void markAsRead(Context context, final long threadId){
        Uri threadUri = ContentUris.withAppendedId(Threads.CONTENT_URI, threadId);

        ContentValues readContentValues = new ContentValues(1);
        readContentValues.put("read", 1);
        // Aurora xuyong 2016-01-26 modified for bug #18626 start
        try {
            context.getContentResolver().update(threadUri, readContentValues,
                    "read=0", null);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        // Aurora xuyong 2016-01-26 modified for bug #18626 end
    }
    // Aurora xuyong 2016-01-21 added for bug #18509 end
    // Aurora xuyong 2016-01-07 added for bug #18232 end
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
                mAllSelectedThreadIds =  (ArrayList<Long>)mActionBatchHandler.getSelected().clone();
                deleteConversations();
                break;
            // Aurora xuyong 2016-01-07 added for bug #18232 start
            case R.id.aurora_list_menu_read_mark:
                mAllSelectedThreadIds = (ArrayList<Long>)mActionBatchHandler.getSelected().clone();
                // Aurora xuyong 2016-01-21 modified for bug #18509 start
                new Thread(mMarkConvAsReadRunnable).start();
                // Aurora xuyong 2016-01-21 modified for bug #18509 end
                leaveBatchMode();
                break;
            // Aurora xuyong 2016-01-07 added for bug #18232 end
            default:
                break;
            }
        }
    };
    // Aurora xuyong 2016-01-21 added for bug #18509 start
    private Runnable mMarkConvAsReadRunnable = new Runnable() {

        @Override
        public void run() {
            // Aurora xuyong 2016-01-26 modified for bug #18626 start
            markConvHaveReaded(AuroraConvListActivity.sContext, (ArrayList<Long>)mAllSelectedThreadIds.clone());
            // Aurora xuyong 2016-01-26 modified for bug #18626 end
        }

    };
    // Aurora xuyong 2016-01-21 added for bug #18509 end
    private void openThread(long threadId, int type) {
        // Aurora xuyong 2016-03-15 modified for bug #21337 start
        Context context = AuroraConvListActivity.sContext;
        Intent intent = ComposeMessageActivity.createIntent(context, threadId).putExtra("quick_query", true);
        if (FeatureOption.MTK_WAPPUSH_SUPPORT == true) {
            if (context != null) {
                startActivity(intent);
            }
        } else {
            startActivity(intent);
        }
        // Aurora xuyong 2016-03-15 modified for bug #21337 end
    }

    private void openThread(long threadId, int type, long privacy) {
        // Aurora xuyong 2016-03-15 modified for bug #21337 start
        Context context = AuroraConvListActivity.sContext;
        Intent intent = ComposeMessageActivity.createIntent(context, threadId).putExtra("quick_query", true);
        if (context != null) {
            intent = ComposeMessageActivity.createIntent(context, threadId).putExtra("quick_query", true);
            if (intent != null) {
                intent.putExtra("is_privacy", privacy);
                startActivity(intent);
            }
        }
        // Aurora xuyong 2016-03-15 modified for bug #21337 end
    }

    private final Runnable mDeleteObsoleteThreadsRunnable = new Runnable() {
        public void run() {
            if (DraftCache.getInstance().getSavingDraft()) {
                // We're still saving a draft. Try again in a second. We don't want to delete
                // any threads out from under the draft.
                mHandler.postDelayed(mDeleteObsoleteThreadsRunnable, 1000);
            } else {
                MessageUtils.asyncDeleteOldMms();
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
            //dialog.setProgressStyle(AuroraProgressDialog.STYLE_SPINNER);
            dialog.setMessage(context.getString(R.string.deleting));
            dialog.setMax(1); /* default is one complete */
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            return dialog;
        }
    }

    private void startQueryLockedAndStaredMsg(long threadId) {
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
            new TotalCount(mContext, TotalCount.MMS_MODULE_kEY, TotalCount.MSG_DEL_RP, 1).countData();
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

                        if (c != null && c.getCount() == 0 && deleteStaredMessages /*== true*/) {
                            // has not draft,delete all. clear table more fast
                            Conversation.startDeleteAll(handler, token, true, mCurrentTabIndex);
                            c.close();
                            DraftCache.getInstance().refresh();
                            return ;
                        }
                    } else {
                        if (deleteStaredMessages && !isEncryptionList && !mHideEncryp) {
                            Conversation.startDeleteAll(handler, token, true, mCurrentTabIndex);
                            DraftCache.getInstance().refresh();
                            return ;
                        }
                    }
                }else if (threadId == 9999) {
                    // multi delete
                    showProgressDialog(context, handler);
                    String threadidString = null;
                    if (mThreadsList.size() == 1) {
                        Conversation.startDelete(handler, MUL_DELETE_CONVERSATIONS_TOKEN,
                                deleteStaredMessages, mThreadsList.get(0), "OneLast", true);
                    } else {
                        int size = mThreadsList.size();
                        for (int i = 0; i < size; i++) {
                            threadidString = mThreadsList.get(i);
                            if (i == size - 1) {
                                Conversation.startDelete(handler, MUL_DELETE_CONVERSATIONS_TOKEN,
                                        deleteStaredMessages, threadidString, "MultLast", true);
                            } else {
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
                    Conversation.startDeletestar(handler, token,
                            deleteStaredMessages, threadId);
                    DraftCache.getInstance().setDraftState(threadId, false);
                }
            }

        });
        if (MmsApp.mGnDeleteRecordSupport) {
            handler.post(mDeleteRecordRunnable);
        }
    }

    static boolean mNeedSetDataChanged = true;
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

    public static AuroraAlertDialog confirmDeleteGnThreadDialog(final GnDeleteThreadListener listener,
            boolean deleteAll,
            boolean hasFavoriteMessages,
            Context context) {
        listener.setDeleteStaredMessage(true);
        String message = context.getString(R.string.confirm_delete_one_conversation);
        if (mAllSelectedThreadIds.size() != 1) {
            message = deleteAll ? context.getString(R.string.confirm_delete_all_conversations) : context.getString(R.string.confirm_delete_selected_conversations, mAllSelectedThreadIds.size());
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
                .setNegativeButton(R.string.no, null).show();
        return mDelAllDialog;
    }

    /*public static void startQueryHaveGnLockedMessages(AsyncQueryHandler handler, long threadId,
            int token, String selection) {
        handler.cancelOperation(token);
        Uri uri = MmsSms.CONTENT_LOCKED_URI;
        if (threadId != 9999) {
            selection = "";
        }
        handler.startQuery(token, new Long(threadId), uri,
                ALL_THREADS_PROJECTION, selection, null, Conversations.DEFAULT_SORT_ORDER);
    }*/

    /*public static void startQueryHaveGnStaredMessages(AsyncQueryHandler handler, long threadId,
            int token, String selection) {
        handler.cancelOperation(token);
        Uri uri = Uri.parse("content://mms-sms/stared");
        if (threadId != 9999) {
            selection = null;
        }
        handler.startQuery(token, new Long(threadId), uri,
                ALL_THREADS_PROJECTION, selection, null, Conversations.DEFAULT_SORT_ORDER);
    }*/
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        switch (requestCode) {
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
                    if (MmsApp.mGnHideEncryption && MmsApp.mGnSmartGuide && data != null && data.getAction().equals("succeed")
                            && !ReadPopTag(AuroraConvListActivity.sContext, FIRSTENCRYPTION)) {
                        WritePopTag(AuroraConvListActivity.sContext, FIRSTENCRYPTION);
                    }
                    Conversation.setFirstEncryption(false);
                    int update = conv.updatethreads(AuroraConvListActivity.sContext, uri,
                            mHasEncryption);
                    if (update > 0) {
                        encryptiontoast(mHasEncryption);
                        conv.updateNotification();
                    }
                }
                break;
            case UPDATE_PASSWORD_REQUEST:
                if (data != null && data.getAction().equals("confirm")) {
                    gnDeleteConversations();
                } else if (data != null && data.getAction().equals("succeed")) {
                    isEncryptionList = true;
                    startEncryptionQuery();
                    if(null != mActionBatchHandler) {
                        mActionBatchHandler.leaveSelectionMode();
                    }
                    if (MmsApp.mGnHideEncryption && MmsApp.mGnSmartGuide && !ReadPopTag(AuroraConvListActivity.sContext, FIRSTENCRYPTION)) {
                        WritePopTag(AuroraConvListActivity.sContext, FIRSTENCRYPTION);
                    }
                    Conversation.setFirstEncryption(false);
                    Toast.makeText(AuroraConvListActivity.sContext, R.string.pref_summary_mms_password_setting_succeed, Toast.LENGTH_SHORT).show();
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
                    if (MmsApp.mGnHideEncryption && MmsApp.mGnSmartGuide && !ReadPopTag(AuroraConvListActivity.sContext, FIRSTENCRYPTION)) {
                        WritePopTag(AuroraConvListActivity.sContext, FIRSTENCRYPTION);
                    }
                    Conversation.setFirstEncryption(false);
                    encryptSelectsConv(true);
                    Toast.makeText(AuroraConvListActivity.sContext, AuroraConvListActivity.sContext.getString(R.string.gn_multi_encryption), Toast.LENGTH_SHORT).show();
                    if(null != mActionBatchHandler) {
                        mActionBatchHandler.leaveSelectionMode();
                    }
                }
                break;
            case CONFIRM_DECRYPTION_PASSWORD_REQUEST:
                if (data != null && data.getAction().equals("confirm")) {
                    encryptSelectsConv(false);
                    Toast.makeText(AuroraConvListActivity.sContext, AuroraConvListActivity.sContext.getString(R.string.gn_multi_dncryption), Toast.LENGTH_SHORT).show();
                    if(null != mActionBatchHandler) {
                        mActionBatchHandler.leaveSelectionMode();
                    }
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch(item.getItemId()) {
        case R.id.gn_action_delete_all:
            startQueryLockedAndStaredMsg(-1);
            break;
        case R.id.gn_action_batch_operation:
            if(null == mActionModeHandler) {
                initThreadsMap(mCurrentTabIndex);
                initActionModeHandler();
                mActionModeHandler.enterSelectionMode(false, null);
                return true;
            }
            break;
        case R.id.gn_action_encryption:
            if (isEncryptionList) {
                isEncryptionList = false;
                if (MmsApp.mGnHideEncryption) {
                    startQuery();
                } else {
                startAsyncQuery();
                }
            } else {
                if (Conversation.getFirstEncryption() == true) {
                    inputencryption(UPDATE_PASSWORD_REQUEST);
                } else {
                    Context context = AuroraConvListActivity.sContext;
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
        }
        return super.onOptionsItemSelected(item);
    }

    private static final int MIN_PASSWORD_LENGTH = 4;
    public void startEncryptionQuery() {
        try {
            Conversation.startQueryForEncryption(mQueryHandler, THREAD_LIST_QUERY_TOKEN, true);
        } catch (SQLiteException e) {
            Context context = AuroraConvListActivity.sContext;
            if (context != null) {
                SqliteWrapper.checkSQLiteException(context, e);
            }
        }
    }

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

    private void inputencryption(int tag) {
        Context context = AuroraConvListActivity.sContext;
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

    private void encryptionConv(Uri uri, boolean encryption) {
        ContentResolver resolver = AuroraConvListActivity.sContext.getContentResolver();
        ContentValues values = new ContentValues(1);
        values.put("encryption", encryption? 1 : 0);
        String selection = "";
        selection = encryption? "encryption = 0":"encryption = 1";
        resolver.update(uri, values, selection, null);
    }

    private void encryptSelectsConv(boolean encrypt) {
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
    }

    protected void initActionModeHandler() {
        String title = null;
        Activity activity = AuroraConvListActivity.sAuroraConvListActivity;
        if (activity != null) {
            mActionModeHandler = new GnActionModeHandler<Long>(activity, title, R.menu.aurora_conversation_multi_select_menu) {
                private MenuItem mDeleteItem = null;
                public void enterSelectionMode(boolean autoLeave, Long itemPressing) {
                    mListAdapter.showCheckBox(true);
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
                    if (mActionModeHandler.getSelected().isEmpty()) {
                        mDeleteItem.setEnabled(false);
                    } else {
                        mDeleteItem.setEnabled(true);
                    }
                    return true;
                }
                
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    mAllSelectedThreadIds.clear();
                    mAllSelectedThreadIds = (ArrayList<Long>)mActionModeHandler.getSelected().clone();
                    switch (item.getItemId()) {
                    case R.id.delete:
                        deleteConversations();
                        break;
                    case R.id.encryption:
                        encryptConversations();
                        leaveSelectionMode();
                        return true;
                    default:
                        break;
                    }
                    return true;
                }
                
                public void updateUi() {
                    // TODO Auto-generated method stub
                    mSelectCount = null != getSelected() ? getSelected().size() : 0;
                    if (null != mDeleteItem) {
                        mDeleteItem.setEnabled(mSelectCount == 0 ? false : true);
                    }
                }
                
                public void bindToAdapter(GnSelectionManager<Long> selectionManager) {
                    // TODO Auto-generated method stub
                    mListAdapter.setSelectionManager(selectionManager);
                }
                
                public void onDestroyActionMode(ActionMode mode) {
                    super.onDestroyActionMode(mode);
                    mListAdapter.showCheckBox(false);
                    mActionModeHandler = null;
                    if(null != mDelAllDialog && !mDelAllDialog.isShowing()) {
                        mListChange = false;
                    }
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
                    intent.setClass(AuroraConvListActivity.sContext, MsgChooseLockPassword.class);
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
            if (mAllSelectedThreadIds.size() == getAdapter(mCurrentTabIndex).getCount() && !isEncryptionList && !mHideEncryp) {
                isCheckAll = true;
            }
            long threadId = -1;
            if (len == 1) {
                threadId = mAllSelectedThreadIds.get(0);
            }else {
                threadId = (isCheckAll) ? -1 : 9999;
            }
            GnDeleteThreadListener listener = new GnDeleteThreadListener(mListView, threadId ,
                    mQueryHandler, AuroraConvListActivity.sAuroraConvListActivity);
            confirmDeleteGnThreadDialog(listener, isCheckAll, hasStar,
                    AuroraConvListActivity.sAuroraConvListActivity);
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

    private void initThreadsMap(int tabIndex) {
        // TODO Auto-generated method stub
        Cursor cursor = getAdapter(tabIndex).getCursor();
        if (cursor != null && !cursor.isClosed()) {
            cursor.moveToPosition(-1);
            int i = 0;
            while(cursor.moveToNext() && !cursor.isBeforeFirst() && !cursor.isAfterLast()) {
                if(cursor.getInt(13) == 1 && !mSetEncryption.contains(cursor.getLong(0))) {
                    getSetEncryption(tabIndex).add(cursor.getLong(0));
                }
                if (getThreadsMap(tabIndex).get(i) == null) {
                    getThreadsMap(tabIndex).put(i++, cursor.getLong(0));
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
        Cursor cursor = AuroraConvListActivity.sContext.getContentResolver().query(Sms.CONTENT_URI, FAVORITE_THREADS_PROJECTION, "star=1",
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

    private void startQuery(int tabIndex) {
        if (isEncryptionList) {
            startEncryptionQuery();
        } else {
            if (MmsApp.mGnHideEncryption) {
                setEncryptionHide(mHideEncryp);
            } else {
                startAsyncQuery(tabIndex);
            }
        }
    }

    private void startQuery() {
        if (isEncryptionList) {
            startEncryptionQuery();
        } else {
            if (MmsApp.mGnHideEncryption) {
                setEncryptionHide(mHideEncryp);
            } else {
                startAsyncQuery();
            }
        }
    }

    static boolean mHideEncryp = false;
    
    public void setEncryptionHide (boolean hide) {
        if (isEncryptionList) {
            return;
        }
        if (hide) {
            startNoEncryptionQuery();
        } else {
            startAsyncQuery(PERSONAL_TAB);
            startAsyncQuery(NOTIFICATION_TAB);
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
            if (cursor != null && !cursor.isClosed() && cursor.getCount() > 0) {
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
            Conversation.startQueryForNoEncryption(mQueryHandler, THREAD_LIST_QUERY_TOKEN, true);
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(AuroraConvListActivity.sContext, e);
        }
    }

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

    private static final String POPUPOPEN = "PopupIsOpen";
    public static final String ISOPEN_STRING = "isOpen";
    public static final String FIRSTENCRYPTION = "firstEncryption";
    public static final String HIDEENCRYPTION = "HideEncryption";

    public static boolean ReadPopTag(Context context, String tag) {
        SharedPreferences user = context.getSharedPreferences(POPUPOPEN, context.MODE_PRIVATE);
        boolean isSmartPopSupport = true;
        try {
            Class <?> clazz = Class.forName("aurora.widget.AuroraSmartPopupLayout");
        } catch (ClassNotFoundException e) {
            // TODO: handle exception
            Log.i("ConvFragment", "e = " + e);
            isSmartPopSupport = false;
        }
        if (user.getBoolean(tag, false) && isSmartPopSupport) {
            return true;
        }
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

    private final int TAB_COUNT = 2;
    private class ConvTabPageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return TAB_COUNT;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getItemPosition(Object object) {
            // TODO Auto-generated method stub
            return super.getItemPosition(object);
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            // TODO Auto-generated method stub
            ((ViewPager) arg0).removeView(pageViews[arg1]);
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            // TODO Auto-generated method stub
            ((ViewPager) arg0).addView(pageViews[arg1]);
            return pageViews[arg1];
        }
    }


    public final static int PERSONAL_TAB = 0;
    public final static int NOTIFICATION_TAB = 1;
    private static int mCurrentTabIndex = PERSONAL_TAB;

    private class ConvTabPagerChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float v, int i1) {
            mCurrentTabIndex = position;
            AuroraConvListActivity.sContext.getSharedPreferences(CONV_NOTIFICATION, AuroraActivity.MODE_PRIVATE).edit().putInt(ConvFragment.CONV_NOTIFICATION_INDEX,
                    mCurrentTabIndex).commit();
        }

        @Override
        public void onPageSelected(int i) {
        }

        @Override
        public void onPageScrollStateChanged(int i) {
        }
    }
}
