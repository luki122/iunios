/*
 * Copyright (C) 2012 gionee Inc.
 *
 * Author:gaoj
 *
 * Description:class for holding the data of recent contact data from database
 *
 * history
 * name                              date                                      description
 * lwzh                                                                              modify for CR00774362 20130227 begin
 */

package com.gionee.mms.ui;

import java.util.ArrayList;
import java.util.Collection;
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
import com.android.mms.ui.MyScrollListener;
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
import com.gionee.mms.ui.TabActivity.ViewPagerVisibilityListener;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.pdu.PduHeaders;
//Aurora xuyong 2013-11-15 modified for google adapt end
import com.aurora.featureoption.FeatureOption;
import com.mediatek.wappush.SiExpiredCheck;

import aurora.app.AuroraAlertDialog;
import android.app.ListFragment;
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
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SqliteWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ServiceManager;
import aurora.preference.AuroraPreferenceManager;
import android.provider.Settings;
import gionee.provider.GnSettings.System;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import gionee.provider.GnTelephony.Threads;
import android.provider.Telephony.Sms.Conversations;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.View.OnKeyListener;
import aurora.widget.AuroraButton;
import android.widget.CheckBox;
import android.widget.ListView;
import aurora.widget.AuroraListView;
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
import android.widget.ArrayAdapter;
import android.widget.ListPopupWindow;
import android.widget.SearchView.OnCloseListener;
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
//gionee gaoj 2012-10-15 modified for CR00705539 end
//Gionee <zhouyj> <2013-04-26> add for CR00802651 start
import android.provider.Contacts.People;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Intents.Insert;
//Gionee <zhouyj> <2013-04-26> add for CR00802651 end

//Gionee <gaoj> <2013-05-28> add for CR00817770 begin
import aurora.widget.AuroraSmartPopupLayout;
import aurora.widget.AuroraSmartLayout;
import android.widget.FrameLayout;
import android.content.SharedPreferences.Editor;
import android.widget.LinearLayout.LayoutParams;
import android.view.Gravity;
//Gionee <gaoj> <2013-05-28> add for CR00817770 end
import aurora.app.AuroraActivity;

public class ConvFragment extends ListFragment implements ViewPagerVisibilityListener,
     DraftCache.OnDraftChangedListener{//, OnItemLongClickListener{

    private ThreadListQueryHandler mQueryHandler;
    private ConversationListAdapter mListAdapter;
    private static final int THREAD_LIST_QUERY_TOKEN       = 1701;
    public static final int DELETE_CONVERSATION_TOKEN      = 1801;
    public static final int HAVE_LOCKED_MESSAGES_TOKEN     = 1802;
    private static final int DELETE_OBSOLETE_THREADS_TOKEN = 1803;
    public static final int HAVE_STAR_MESSAGES_TOKEN       = 1804;

    public static final int THREAD_LIST_LOCKED_QUERY_TOKEN  = 1902;
    public static final int THREAD_LIST_FAVORITE_QUERY_TOKEN = 1903;
    private static ArrayList<Long> mAllSelectedThreadIds = new ArrayList<Long>();
    public static final String[] ALL_THREADS_PROJECTION = {
        Threads._ID, Threads.DATE, Threads.MESSAGE_COUNT, Threads.RECIPIENT_IDS,
        Threads.SNIPPET, Threads.SNIPPET_CHARSET, Threads.READ, Threads.ERROR,
        Threads.HAS_ATTACHMENT, Threads.TYPE, Threads.SIM_ID
    };

    private static int CHANGE_SCROLL_LISTENER_MIN_CURSOR_COUNT = 100;
    private Handler mHandler;
    private SharedPreferences mPrefs;
    static private final String CHECKED_MESSAGE_LIMITS = "checked_message_limits";
//    private PostDrawListener mPostDrawListener = new PostDrawListener();
    private MyScrollListener mScrollListener = new MyScrollListener(CHANGE_SCROLL_LISTENER_MIN_CURSOR_COUNT, "ConvFragment_Scroll_Tread");

    //wappush: indicates the type of thread, this exits already, but has not been used before
    private int mType;
    //wappush: SiExpired Check
    private SiExpiredCheck siExpiredCheck;
    //wappush: wappush TAG
    private static final String WP_TAG = "Mms/WapPush";

    private boolean mNeedToMarkAsSeen;
    private boolean mDataValid;
    private static AuroraAlertDialog mDelAllDialog;

    public static final int CONFIRM_PASSWORD_REQUEST = 39;
    public static final int UPDATE_PASSWORD_REQUEST =40;
    public static final int MULTI_PASSWORD_REQUEST = 41;
    public static final int CONFIRM_DECRYPTION_PASSWORD_REQUEST = 42;
    private AuroraListView mListView;
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
    private Map<Integer, Long> mThreadsMap = new HashMap<Integer, Long>();
    // gionee zhouyj 2012-07-31 add for CR00662942 end
    // gionee zhouyj 2012-08-08 add for CR00664390 start 
    private static boolean mListChange = false;
    // gionee zhouyj 2012-08-08 add for CR00664390 end 

    //Gionee tianxiaolong 2012.8.29 add for CR00682320 begin
    private static boolean gnFlyFlag = SystemProperties.get("ro.gn.oversea.custom").equals("RUSSIA_FLY");
    //Gionee tianxiaolong 2012.8.29 add for CR00682320 end

    //gionee gaoj 2013-3-21 modified for CR00786905 start
    private boolean mIsonResume = true;
    //gionee gaoj 2013-3-21 modified for CR00786905 end
    // gionee zhouyj 2012-10-26 add for CR00718476 start 
    private static boolean sInMultiMode = false;
    // gionee zhouyj 2012-10-26 add for CR00718476 end 

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
    //gionee gaoj added for CR00725602 20121201 end
    // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
   /* private class PostDrawListener implements android.view.ViewTreeObserver.OnPostDrawListener {
        @Override
        public boolean onPostDraw() {
            Log.i("AppLaunch", "[AppLaunch] MMS onPostDraw");
            return true;
        }
    }*/
    // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end

    @Override
    public void onVisibilityChanged(boolean visible) {
        // TODO Auto-generated method stub
//        mShowOptionsMenu = true;
        //gionee gaoj 2013-3-21 modified for CR00786905 start
        mIsonResume = visible;
        //gionee gaoj 2013-3-21 modified for CR00786905 end
        // gionee zhouyj 2013-04-02 add for CR00792152 start
        if (!visible && mActionModeHandler != null && mActionModeHandler.inSelectionMode()) {
            mActionModeHandler.leaveSelectionMode();
        }
        // gionee zhouyj 2013-04-02 add for CR00792152 end
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mQueryHandler = new ThreadListQueryHandler(getActivity().getContentResolver());
        mHandler = new Handler();
        mPrefs = AuroraPreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean checkedMessageLimits = mPrefs.getBoolean(CHECKED_MESSAGE_LIMITS, false);
        if (!checkedMessageLimits ) {
            runOneTimeStorageLimitCheckForLegacyMessages();
        }
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            siExpiredCheck = new SiExpiredCheck(getActivity());
            siExpiredCheck.startSiExpiredCheckThread();
        }
        //Gionee <gaoj> <2013-05-28> add for CR00817770 begin
        if (MmsApp.mGnHideEncryption) {
            mHideEncryp = ReadPopTag(TabActivity.sContext, HIDEENCRYPTION);
            Log.d("ConvFragment", "onCreate mHideEncryp = "+mHideEncryp);
        }
        //Gionee <gaoj> <2013-05-28> add for CR00817770 end
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.gn_conversation_list_screen, container, false);

        // Tell the list view which view to display when the list is empty
        mEmptyView = (LinearLayout)fragmentView.findViewById(R.id.gn_conversation_empty);

        //gionee gaoj 2012-6-14 added for CR00623396 start
        mEmptyTextView = (TextView) fragmentView.findViewById(R.id.empty);
        if (MmsApp.mDarkStyle) {
            mEmptyTextView.setTextColor(getResources().getColor(R.color.gn_dark_color_bg));
        }
        mEncryptionTitle = (TextView) fragmentView.findViewById(R.id.gn_encryption_title);
        //gionee gaoj 2012-6-14 added for CR00623396 end
        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);

        mListView = (AuroraListView)getListView();
        mListView.setOnKeyListener(mThreadListKeyListener);
        //mListView.setChoiceMode(AuroraListView.CHOICE_MODE_MULTIPLE_MODAL);
        //mListView.setMultiChoiceModeListener(new ModeCallback());
        //gionee gaoj added for CR00725602 20121201 start
//      mListView.setOnItemLongClickListener(this);
        mListView.setOnCreateContextMenuListener(mConvListOnCreateContextMenuListener);
        //gionee gaoj added for CR00725602 20121201 end
        mListView.setOnScrollListener(mScrollListener);

        mEmptyView.setVisibility(View.GONE);
        mListAdapter = new ConversationListAdapter(getActivity(), null);
        mListAdapter.setOnContentChangedListener(mContentChangedListener);
        setListAdapter(mListAdapter);
        mListView.setRecyclerListener(mListAdapter);
        // gionee zhouyj 2012-01-21 add for CR00765451 start 
        if (mActionModeHandler != null && mActionModeHandler.isInSelectionMode()) {
            mListAdapter.showCheckBox(true);
            mActionModeHandler.bindToAdapter(mActionModeHandler.getSelectionManger());
        }
        // gionee zhouyj 2012-01-21 add for CR00765451 end 
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

    public void startEncryptionAsyncQuery() {
        try {

            Conversation.startQueryForAll(mQueryHandler, THREAD_LIST_QUERY_TOKEN, true);
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(getActivity(), e);
        }
    }

    private void startAsyncQuery() {
        try {

            Conversation.startQueryForAll(mQueryHandler, THREAD_LIST_QUERY_TOKEN);

        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(getActivity(), e);
        }
    }

    /**
     * Checks to see if the number of MMS and SMS messages are under the limits for the
     * recycler. If so, it will automatically turn on the recycler setting. If not, it
     * will prompt the user with a message and point them to the setting to manually
     * turn on the recycler.
     */
    public synchronized void runOneTimeStorageLimitCheckForLegacyMessages() {
        if (Recycler.isAutoDeleteEnabled(getActivity())) {
            markCheckedMessageLimit();
            return;
        }
        new Thread(new Runnable() {
            public void run() {
                //gionee gaoj 2012-8-11 added for CR00673061 start
                if (TabActivity.sTabActivity != null) {
                    //gionee gaoj 2012-8-11 added for CR00673061 end
                // gionee zhouyj 2012-07-02 modify for CR00632512 start 
                if (Recycler.checkForThreadsOverLimit(TabActivity.sTabActivity)) {
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            Intent intent = new Intent(TabActivity.sTabActivity,
                                    WarnOfStorageLimitsActivity.class);
                            // gionee zhouyj 2013-04-09 modify for CR00795009 start
                            TabActivity.sTabActivity.startActivity(intent);
                            // gionee zhouyj 2013-04-09 modify for CR00795009 end
                        }
                    }, 2000);
                }
                TabActivity.sTabActivity.runOnUiThread(new Runnable() {
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
//        
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
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        //gionee gaoj 2013-3-26 added for CR00787090 start
        mIsonResume = false;
        //gionee gaoj 2013-3-26 added for CR00787090 end
        // gionee zhouyj 2013-04-01 add for CR00792152 start
        if (mActionModeHandler != null && mActionModeHandler.inSelectionMode()) {
            mActionModeHandler.leaveSelectionMode();
        }
        // gionee zhouyj 2013-04-01 add for CR00792152 end
    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
//        getActivity().getWindow().getDecorView().getViewTreeObserver().addOnPostDrawListener(mPostDrawListener);

        MessagingNotification.cancelNotification(getActivity(),
                SmsRejectedReceiver.SMS_REJECTED_NOTIFICATION_ID);

        DraftCache.getInstance().addOnDraftChangedListener(this);
        mNeedToMarkAsSeen = true;

        //gionee gaoj 2012-8-7 modified for CR00664245 start
        startQuery();
        //gionee gaoj 2012-8-7 modified for CR00664245 end
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            siExpiredCheck.startExpiredCheck();
        }
        
        DraftCache.getInstance().refresh();
        if (!Conversation.loadingThreads()) {
            Contact.invalidateCache();
        }
    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
//        getActivity().getWindow().getDecorView().getViewTreeObserver().removeOnPostDrawListener(mPostDrawListener);
        DraftCache.getInstance().removeOnDraftChangedListener(this);
        //getListView().setChoiceMode(AuroraListView.CHOICE_MODE_MULTIPLE_MODAL);

        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            siExpiredCheck.stopExpiredCheck();
        }

        //gionee gaoj 2013-3-11 added for CR00782858 start
          // Aurora liugj 2013-11-07 deleted for hide widget start
        //MmsWidgetProvider.notifyDatasetChanged(TabActivity.sContext);
          // Aurora liugj 2013-11-07 deleted for hide widget end
        //gionee gaoj 2013-3-11 added for CR00782858 end
        
        //Gionee <gaoj> <2013-05-28> add for CR00817770 begin
        if (MmsApp.mGnHideEncryption && mHideEncryp != ReadPopTag(TabActivity.sContext, HIDEENCRYPTION)) {
            WritePopTag(TabActivity.sContext, HIDEENCRYPTION, mHideEncryp);
        }
        //Gionee <gaoj> <2013-05-28> add for CR00817770 end
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mScrollListener.destroyThread();
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            siExpiredCheck.stopSiExpiredCheckThread();
        }
        isEncryptionList = false;
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

    private final class ThreadListQueryHandler extends BaseProgressQueryHandler {
        private final String CONV_TAG = "ConvFragment";
        private boolean lockFlag = false;
        private boolean starFlag = false;

        public ThreadListQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            //gionee gaoj 2012-10-12 added for CR00711168 start
            if (MmsApp.mIsSafeModeSupport) {
                mEmptyTextView.setText(R.string.no_conversations);
                mEncryptionTitle.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.GONE);
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
                // gionee zhouyj 2012-11-07 add for CR00723358 start 
                if(mActionModeHandler != null) {
                    mActionModeHandler.leaveSelectionMode();
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
                mListAdapter.changeCursor(cursor);
                if (mListAdapter.isEmpty()) {
                    //gionee gaoj 2012-6-14 added for CR00623396 start
                    if (isEncryptionList) {
                        mEmptyTextView.setText(R.string.no_encryption_conversations);
                    } else {
                        mEmptyTextView.setText(R.string.no_conversations);
                    }
                    //gionee gaoj 2012-6-14 added for CR00623396 end
                    mEmptyView.setVisibility(View.VISIBLE);
                    mListView.setVisibility(View.GONE);
                    mEncryptionTitle.setVisibility(View.GONE);
                } else {
                    if (isEncryptionList) {
                        mEncryptionTitle.setVisibility(View.VISIBLE);
                    } else {
                        mEncryptionTitle.setVisibility(View.GONE);
                    }
                    mEmptyView.setVisibility(View.GONE);
                    mListView.setVisibility(View.VISIBLE);
                }
                if (!Conversation.isInitialized()) {
                    Conversation.init(TabActivity.sContext);
                }else{
                    Conversation.removeInvalidCache(cursor);
                }
                
                if (mNeedToMarkAsSeen) {
                    mNeedToMarkAsSeen = false;
                    Conversation.markAllConversationsAsSeen(TabActivity.sContext);

                    // Delete any obsolete threads. Obsolete threads are threads that aren't
                    // referenced by at least one message in the pdu or sms tables. We only call
                    // this on the first query (because of mNeedToMarkAsSeen).
                    mHandler.post(mDeleteObsoleteThreadsRunnable);
                }
                //gionee gaoj 2012-5-25 added for CR00421454 start
                TabActivity.sTabActivity.invalidateOptionsMenu();
                //gionee gaoj 2012-5-25 added for CR00421454 end

                // gionee zhouyj 2012-08-13 add for CR00664390 start 
                if(mContentChanged) {
                    if(mListViewWatcher != null) {
                        mListViewWatcher.listViewChanged(true);
                    }
                    if(!mListChange && null != mActionModeHandler) {
                        //gionee gaoj 2012-11-22 added for CR00718172 start
                        if (cursor.getCount() > mThreadsMap.size()) {
                            mSetEncryption.clear();
                            mThreadsMap.clear();
                            initThreadsMap();
                        }
                        //gionee gaoj 2012-11-22 added for CR00718172 end
                        mActionModeHandler.refreshDataSet();
                        mActionModeHandler.updateUi();
                        if(cursor.getCount() != mActionModeHandler.getSelected().size()) {
                            mListChange = true;
                        }
                    }
                    mContentChanged = false;
                }
                // gionee zhouyj 2012-08-13 add for CR00664390 end 

                //Gionee <gaoj> <2013-05-28> add for CR00817770 begin
                Log.d("ConvFragment", "ReadPopTag(TabActivity.sContext, FIRSTENCRYPTION) = "+ReadPopTag(TabActivity.sContext, FIRSTENCRYPTION));
                Log.d("ConvFragment", "ReadPopTag(TabActivity.sContext, ISOPEN_STRING = "+ReadPopTag(TabActivity.sContext, ISOPEN_STRING));
                if (!isEncryptionList && MmsApp.mGnHideEncryption && MmsApp.mGnSmartGuide && ReadPopTag(TabActivity.sContext, FIRSTENCRYPTION)
                        && !ReadPopTag(TabActivity.sContext, ISOPEN_STRING)) {
                    if (queryHasEncryption()) {
                        OpenPopLayout();
                        WritePopTag(TabActivity.sContext, ISOPEN_STRING);
                    }
                }
                //Gionee <gaoj> <2013-05-28> add for CR00817770 end
                break;

            case THREAD_LIST_LOCKED_QUERY_TOKEN:
                //gionee gaoj 2012-5-16 remove locked for CRCR00600687 start
                lockFlag = false;
//                lockFlag = (cursor != null && cursor.getCount() > 0);
                //gionee gaoj 2012-5-16 remove locked for CRCR00600687 end
                cursor.close();
                break;
            case THREAD_LIST_FAVORITE_QUERY_TOKEN:
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
                    confirmDeleteGnThreadDialog(
                            new GnDeleteThreadListener(-1, mQueryHandler,
                                    getActivity()), isAllSelected, starFlag,
                            getActivity());

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
                    confirmDeleteGnThreadDialog(
                            new GnDeleteThreadListener(tempThreadId, mQueryHandler,
                                    getActivity()), isAllSelected, starFlag,
                            getActivity());
                }
                cursor.close();
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
                starFlag = (cursor != null && cursor.getCount() > 0);
                confirmDeleteGnThreadDialog(
                        new GnDeleteThreadListener(onethread, mQueryHandler,
                                getActivity()), false, starFlag,
                        getActivity()); 
                cursor.close();
                break;
            case THREAD_LIST_FAVORITE_QUERY_ALL_TOKEN:
                starFlag = (cursor != null && cursor.getCount() > 0);
                confirmDeleteGnThreadDialog(
                        new GnDeleteThreadListener(-1, mQueryHandler,
                                getActivity()), true, starFlag,
                        getActivity());
                cursor.close();
                break;
            //gionee gaoj 2012-10-15 added for CR00705539 end
            default:
            }
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            // When this callback is called after deleting, token is 1803(DELETE_OBSOLETE_THREADS_TOKEN)
            // not 1801(DELETE_CONVERSATION_TOKEN)
            // gionee zhouyj 2012-04-28 modified for CR00585947 start
            CBMessagingNotification.updateNewMessageIndicator(TabActivity.sContext);
            // gionee zhouyj 2012-04-28 modified for CR00585947 end
            switch (token) {
            //gionee gaoj 2012-10-15 added for CR00705539 start
            case DELETE_CONVERSATION_NOT_LAST_TOKEN:
                //Not the Last , Nothing to do here.
                break;
            //gionee gaoj 2012-10-15 added for CR00705539 end
            case DELETE_CONVERSATION_TOKEN:
                // Rebuild the contacts cache now that a thread and its associated unique
                // recipients have been deleted.
                Contact.init(TabActivity.sContext);

                // Make sure the conversation cache reflects the threads in the DB.
                Conversation.init(TabActivity.sContext);

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
                // Update the notification for new messages since they
                // may be deleted.
                MessagingNotification.nonBlockingUpdateNewMessageIndicator(TabActivity.sContext,
                        false, false);
                // Update the notification for failed messages since they
                // may be deleted.
                MessagingNotification.updateSendFailedNotification(TabActivity.sContext);
                MessagingNotification.updateDownloadFailedNotification(TabActivity.sContext);

                //Update the notification for new WAP Push messages
                if(FeatureOption.MTK_WAPPUSH_SUPPORT){
                    WapPushMessagingNotification.nonBlockingUpdateNewMessageIndicator(TabActivity.sContext,false);
                }

                // Make sure the list reflects the delete
                //Gionee <zhouyj> <2013-05-04> modify for CR00799297 begin
                ConvFragment.this.startQuery();
                //Gionee <zhouyj> <2013-05-04> modify for CR00799297 end
                if (progress()) {
                    dismissProgressDialog();
                }
                break;

            case DELETE_OBSOLETE_THREADS_TOKEN:
                // Nothing to do here.
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
                        long id = getListView().getSelectedItemId();
                        if (id > 0) {
                            if (MmsApp.mEncryption) {
                                if (checkoutEncryption()) {
                                    final Intent intent = new Intent();
                                    intent.setClass(getActivity(), MsgChooseLockPassword.class);
                                    intent.putExtra("isdecryption", true);
                                    startActivityForResult(intent,
                                            ConvFragment.UPDATE_PASSWORD_REQUEST);
                                    break;
                                }
                            }

                            gnDeleteConversations();

                        }
                        return true;
                    }
                }
            }
            return false;
        }
    };

    @Override
    public void onDraftChanged(long threadId, boolean hasDraft) {
        // TODO Auto-generated method stub
        mQueryHandler.post(new Runnable() {
            public void run() {
                mListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);
        if(null != mActionModeHandler && mActionModeHandler.inSelectionMode()) {
            mActionModeHandler.getSelectionManger().toggle(id);
        } else {
            Cursor cursor  = (Cursor) getListView().getItemAtPosition(position);
            if (cursor == null) {
                return;
            }   
            
             // gionee lwzh add for CR00774362 20130227 begin
            if (MmsApp.mGnMessageSupport) {
                // 0 : threadid, 9 type
                openThread(cursor.getInt(0), cursor.getInt(9));
                return;
            }
            // gionee lwzh add for CR00774362 20130227 begin
            
            Conversation conv = Conversation.from(getActivity(), cursor);
            long tid = conv.getThreadId();
    
            openThread(tid, conv.getType());
        }
    }
    
    //gionee gaoj added for CR00725602 20121201 start
    /*@Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
            int position, long id) {
        // TODO Auto-generated method stub
        if(null == mActionModeHandler) {
            initThreadsMap();
            initActionModeHandler();
            mActionModeHandler.enterSelectionMode(true, id);
            return true;
        }
        return false;
    }*/
    private final OnCreateContextMenuListener mConvListOnCreateContextMenuListener = 
        new OnCreateContextMenuListener() {

            public void onCreateContextMenu(ContextMenu menu, View v,
                    ContextMenuInfo menuInfo) {
                // TODO Auto-generated method stub

                if (null != mActionModeHandler) {
                    return;
                }
                Cursor cursor = mListAdapter.getCursor();
                if (cursor == null || cursor.getPosition() < 0) {
                    return;
                }

                Conversation conv = Conversation.from(TabActivity.sContext, cursor);
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
        if (cursor != null && cursor.getPosition() >= 0) {
            switch (item.getItemId()) {
            case MENU_DELETE: {
                if (!isEncryptionList && mHasEncryption) {
                    final Intent intent = new Intent();
                    intent.setClass(TabActivity.sContext, MsgChooseLockPassword.class);
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
                    if (MmsApp.mGnHideEncryption && MmsApp.mGnSmartGuide && !ReadPopTag(TabActivity.sContext, FIRSTENCRYPTION)) {
                        WritePopTag(TabActivity.sContext, FIRSTENCRYPTION);
                    }
                    //Gionee <gaoj> <2013-05-28> add for CR00817770 end
                    Uri uri = Uri.parse("content://mms-sms/encryption/" + mThreadID);
                    Conversation conv = Conversation.from(TabActivity.sContext, cursor);
                    int update = conv.updatethreads(TabActivity.sContext, uri, false);
                    if (update > 0) {
                        encryptiontoast(mHasEncryption);
                        conv.updateNotification();
                    }
                }
                break;
            case MENU_DECRYPTION:
                if (isEncryptionList) {
                    Uri uri = Uri.parse("content://mms-sms/encryption/" + mThreadID);
                    Conversation conv = Conversation.get(TabActivity.sContext,
                            mThreadID, false);
                    int update = conv.updatethreads(TabActivity.sContext, uri,
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
                    Conversation conv = Conversation.from(TabActivity.sContext, cursor);
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
                    Conversation conv = Conversation.from(TabActivity.sContext, cursor);
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
                    Conversation conv = Conversation.from(TabActivity.sContext, cursor);
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

    private void openThread(long threadId, int type) {
        if(FeatureOption.MTK_WAPPUSH_SUPPORT == true){
            //wappush: add opptunities for starting wappush activity if it is a wappush thread 
            //type: Threads.COMMON_THREAD, Threads.BROADCAST_THREAD and Threads.WAP_PUSH
            if(type == Threads.WAPPUSH_THREAD){
                startActivity(WPMessageActivity.createIntent(getActivity(), threadId));
            } else if (type == Threads.CELL_BROADCAST_THREAD) {
                startActivity(CBMessageListActivity.createIntent(getActivity(), threadId));
            } else {
                startActivity(ComposeMessageActivity.createIntent(getActivity(), threadId));
            }
        }else{
            if (type == Threads.CELL_BROADCAST_THREAD) {
                startActivity(CBMessageListActivity.createIntent(getActivity(), threadId));
            } else {
                startActivity(ComposeMessageActivity.createIntent(getActivity(), threadId));
            }
        }
    }

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
                Conversation.asyncDeleteObsoleteThreads(mQueryHandler,
                        DELETE_OBSOLETE_THREADS_TOKEN);
            }
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
            dialog.setProgressStyle(AuroraProgressDialog.STYLE_SPINNER);
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

    //gionee gaoj 2012-10-15 modified for CR00705539 start
    //gionee gaoj 2012-9-20 added for CR00699291 start
    private void startQueryStaredMsg(long threadId) {
        int tag;
        if (threadId == -1) {
            tag = THREAD_LIST_FAVORITE_QUERY_ALL_TOKEN;
        } else {
            tag = THREAD_LIST_FAVORITE_QUERY_ONE_TOKEN;
        }
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

        public GnDeleteThreadListener(long threadId, AsyncQueryHandler handler, Context context) {
            mThreadId = threadId;
            mHandler = handler;
            mContext = context;
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
            if(whichButton == -1 && mActionModeHandler != null) {
                mActionModeHandler.leaveSelectionMode();
            }
            MessageUtils.handleReadReport(mContext, mThreadId,
                    PduHeaders.READ_STATUS__DELETED_WITHOUT_BEING_READ, new Runnable() {
                public void run() {
                    showProgressDialog();
                    int token = DELETE_CONVERSATION_TOKEN;
                    // gionee zhouyj 2012-11-08 modify for CR00725666 start 
                    if(mThreadId == -1 && mListChange) {
                        mThreadId = 9999;
                    }
                    mListChange = false;
                    // gionee zhouyj 2012-08-08 add for CR00664390 end 
                    // gionee zhouyj 2012-07-10 modify for CR00640610 start 
                    if (mThreadId == -1) {
                        //Gionee <zhouyj> <2013-06-13> modify for CR00789924 begin
                        if (MmsApp.mIsDraftOpen) {
                            // has not draft
                            Uri sAllThreadsUri = Threads.CONTENT_URI.buildUpon().appendQueryParameter("simple", "true").build();
                            Cursor c = SqliteWrapper.query(mContext, mContext.getContentResolver(), sAllThreadsUri,
                                  ALL_THREADS_PROJECTION, "sim_id=-1 AND message_count=0", null, null);
    
                            if (c != null && c.getCount() == 0 && mDeleteStaredMessages == true) {
                                // has not draft,delete all. clear table more fast
                                Log.i("GnDeleteThreadListener", "delete all: no draft, no stared message");
                                Conversation.startDeleteAll(mHandler, token, true);
                                c.close();
                                DraftCache.getInstance().refresh();
                                return ;
                            }
                        } else {
                            if (mDeleteStaredMessages && !isEncryptionList && !mHideEncryp) {
                                Conversation.startDeleteAll(mHandler, token, true);
                                DraftCache.getInstance().refresh();
                                return ;
                            }
                        }
                        //Gionee <zhouyj> <2013-06-13> modify for CR00789924 end
                    }
                    // multi delete
                    if (mThreadId == 9999 || mThreadId == -1) {
                        //gionee gaoj 2012-10-15 modified for CR00705539 start
                        String threadidString = null;
                        if (mThreadsList.size() == 1) {
                            Conversation.startDelete(mHandler, token,
                                    mDeleteStaredMessages, mThreadsList.get(0), "OneLast");
                        } else {
                            for (int i = 0; i < mThreadsList.size(); i++) {
                                threadidString = mThreadsList.get(i);
                                if (i > 0) {
                                    threadidString = threadidString.substring(16, threadidString.length());
                                }
                                if (i == mThreadsList.size() - 1) {
                                    Conversation.startDelete(mHandler, token,
                                            mDeleteStaredMessages, threadidString, "MultLast");
                                } else {
                                    Conversation.startDelete(mHandler, DELETE_CONVERSATION_NOT_LAST_TOKEN,
                                            mDeleteStaredMessages, threadidString, null);
                                }
                            }
                        }
                        //gionee gaoj 2012-10-15 modified for CR00705539 end
                        DraftCache.getInstance().refresh();
                    } else {
                        Conversation.startDeletestar(mHandler, token,
                                mDeleteStaredMessages, mThreadId);
                        DraftCache.getInstance().setDraftState(mThreadId, false);
                    }
                    // gionee zhouyj 2012-11-08 modify for CR00725666 end 
                }

                private void showProgressDialog() {
                    if (mHandler instanceof BaseProgressQueryHandler) {
                        ((BaseProgressQueryHandler) mHandler).setProgressDialog(
                                DeleteProgressDialogUtil.getProgressDialog(mContext));
                        ((BaseProgressQueryHandler) mHandler).showProgressDialog();
                    }
                }
            });
            dialog.dismiss();
            //Gionee <zhouyj> <2013-05-02> add for CR00802651 begin
            if (MmsApp.mGnDeleteRecordSupport) {
                mHandler.post(mDeleteRecordRunnable);
            }
            //Gionee <zhouyj> <2013-05-02> add for CR00802651 end
            //gionee gaoj 2012-12-3 removed for CR00738791 start
            //gionee gaoj 2012-12-3 removed for CR00738791 end  
        }
    }
    
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
        View contents = View.inflate(context, R.layout.gn_delete_thread_dialog_view, null);
        TextView msg = (TextView) contents.findViewById(R.id.gn_message);
        msg.setText(R.string.confirm_delete_conversation);

        final CheckBox starCheckbox = (CheckBox) contents.findViewById(R.id.gn_delete_favorite);

        //gionee gaoj added for CR00725602 20121201 start
        // Aurora xuyong 2013-09-20 deleted for aurora's new feature start
        /*if (deleteAll) {
            msg.setTextColor(Color.WHITE);
            starCheckbox.setTextColor(Color.WHITE);
        }*/
        // Aurora xuyong 2013-09-20 deleted for aurora's new feature end
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
        }

        //gionee gaoj added for CR00725602 20121201 start
        AuroraAlertDialog.Builder builder = null;
        if (deleteAll) {
            builder = new AuroraAlertDialog.Builder(context/*, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN*/);
        } else {
            builder = new AuroraAlertDialog.Builder(context);//, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN);
        }
        //gionee gaoj added for CR00725602 20121201 end
        mDelAllDialog = builder.setTitle(R.string.confirm_dialog_title)
                .setCancelable(true).setPositiveButton(R.string.OK, listener)
                .setNegativeButton(R.string.no, null).setView(contents).show();
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

                Conversation conv = Conversation.get(TabActivity.sContext,
                        mThreadID, false);

                //Gionee <gaoj> <2013-05-28> add for CR00817770 begin
                if (MmsApp.mGnHideEncryption && MmsApp.mGnSmartGuide && data != null && data.getAction().equals("succeed")
                        && !ReadPopTag(TabActivity.sContext, FIRSTENCRYPTION)) {
                    WritePopTag(TabActivity.sContext, FIRSTENCRYPTION);
                }
                //Gionee <gaoj> <2013-05-28> add for CR00817770 end
                Conversation.setFirstEncryption(false);
                int update = conv.updatethreads(TabActivity.sContext, uri,
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
                if(null != mActionModeHandler) {
                    mActionModeHandler.leaveSelectionMode();
                }
                //Gionee <gaoj> <2013-05-28> add for CR00817770 begin
                if (MmsApp.mGnHideEncryption && MmsApp.mGnSmartGuide && !ReadPopTag(TabActivity.sContext, FIRSTENCRYPTION)) {
                    WritePopTag(TabActivity.sContext, FIRSTENCRYPTION);
                }
                //Gionee <gaoj> <2013-05-28> add for CR00817770 end
                Conversation.setFirstEncryption(false);
                Toast.makeText(getActivity(), R.string.pref_summary_mms_password_setting_succeed, Toast.LENGTH_SHORT).show();
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
                if (MmsApp.mGnHideEncryption && MmsApp.mGnSmartGuide && !ReadPopTag(TabActivity.sContext, FIRSTENCRYPTION)) {
                    WritePopTag(TabActivity.sContext, FIRSTENCRYPTION);
                }
                //Gionee <gaoj> <2013-05-28> add for CR00817770 end
                Conversation.setFirstEncryption(false);
                encryptSelectsConv(true);
                Toast.makeText(TabActivity.sContext, TabActivity.sContext.getString(R.string.gn_multi_encryption), Toast.LENGTH_SHORT).show();
                if(null != mActionModeHandler) {
                    mActionModeHandler.leaveSelectionMode();
                }
            }
            break;
        case CONFIRM_DECRYPTION_PASSWORD_REQUEST:
            if (data != null && data.getAction().equals("confirm")) {
                encryptSelectsConv(false);
                Toast.makeText(TabActivity.sContext, TabActivity.sContext.getString(R.string.gn_multi_dncryption), Toast.LENGTH_SHORT).show();
                if(null != mActionModeHandler) {
                    mActionModeHandler.leaveSelectionMode();
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
        if(!TabActivity.checkMsgImportExportSms()) {
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
                    final Intent intent = new Intent();
                    intent.setClass(getActivity(), MsgChooseLockPassword.class);
                    intent.putExtra("isdecryption", true);
                    startActivityForResult(intent,
                            ConvFragment.CONFIRM_PASSWORD_REQUEST);
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

            Conversation.startQueryForEncryption(mQueryHandler, THREAD_LIST_QUERY_TOKEN, true);
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(getActivity(), e);
        }
    }

    //gionee gaoj added for CR00725602 20121201 start
    private void encryptiontoast(boolean hasencryption) {
        final int resId = hasencryption ? R.string.gn_confirm_dncryption
                : R.string.gn_confirm_encryption;
        Toast.makeText(TabActivity.sContext, resId, Toast.LENGTH_SHORT).show();
    }

    private void inputdecryption(int tag){
        final Intent intent = new Intent();
        intent.setClass(TabActivity.sContext, MsgChooseLockPassword.class);
        intent.putExtra("isdecryption", true);
        this.startActivityForResult(intent, tag);
    }
    //gionee gaoj added for CR00725602 20121201 end

    private void inputencryption(int tag) {
        DevicePolicyManager DPM = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
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
            Intent intent = new Intent(getActivity(),
                    MsgChooseLockPassword.class);
            intent.putExtra(LockPatternUtils.PASSWORD_TYPE_KEY, quality);
            intent.putExtra(MsgChooseLockPassword.PASSWORD_MIN_KEY, minLength);
            intent.putExtra(MsgChooseLockPassword.PASSWORD_MAX_KEY, maxLength);
            startActivityForResult(intent, tag);
        }
    }
    //gionee gaoj 2012-6-14 added for CR00623396 end
    
    // gionee zhouyj 2012-06-15 add for CR00623385 start 
    private void encryptionConv(Uri uri, boolean encryption) {
        ContentResolver resolver = TabActivity.sContext.getContentResolver();
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
        mActionModeHandler = new GnActionModeHandler<Long>(getActivity(), title, R.menu.gn_conversation_multi_select_menu) {
            private MenuItem mEncryptItem = null;
            private MenuItem mDeleteItem = null;
            public void enterSelectionMode(boolean autoLeave, Long itemPressing) {
                // gionee zhouyj 2012-09-04 modify for CR00686905 start
                int orientation = getActivity().getResources().getConfiguration().orientation;
                if(orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                }
                // gionee zhouyj 2012-09-04 modify for CR00686905 end
                // gionee zhouyj 2012-10-12 modify for CR00711214 start 
                mListAdapter.showCheckBox(true);
                // gionee zhouyj 2012-10-12 modify for CR00711214 end 
                // gionee zhouyj 2012-10-26 add for CR00718476 start 
                sInMultiMode = true;
                // gionee zhouyj 2012-10-26 add for CR00718476 end

                mListAdapter.notifyDataSetChanged();
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
                mEncryptItem = menu.findItem(R.id.encryption).setVisible(true);
                if (mActionModeHandler.getSelected().isEmpty()) {
                    mEncryptItem.setEnabled(false);
                    mDeleteItem.setEnabled(false);
                    /*if (MmsApp.mDarkStyle) {
                        mEncryptItem.setIcon(R.drawable.gn_conv_dncryption_unuse);
                        //mDeleteItem.setIcon(R.drawable.gn_com_delete_unuse_dark_bg);
                    }*/
                } else {
                    mEncryptItem.setEnabled(true);
                    mDeleteItem.setEnabled(true);
                    /*if (MmsApp.mDarkStyle) {
                        mEncryptItem.setIcon(R.drawable.gn_conv_encryption_light_bg);
                        //mDeleteItem.setIcon(R.drawable.gn_com_delete_dark_bg);
                    }*/
                }
                //Gionee tianxiaolong 2012.8.29 add for CR00682320 begin
                if(gnFlyFlag){
                    mEncryptItem.setVisible(false);
                }
                //Gionee tianxiaolong 2012.8.29 add for CR00682320 end
                
                if(isEncryptionList) {
                    mBeingEncrypt = false;
                    mEncryptItem.setTitle(R.string.menu_decryption);
                   /* if (MmsApp.mDarkStyle) {
                        mEncryptItem.setIcon(R.drawable.gn_conv_dncryption_unuse);
                    } else {
                        mEncryptItem.setIcon(R.drawable.gn_conv_dncryption_bg);
                    }*/
                }
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
                mListAdapter.notifyDataSetChanged();
                if(!isEncryptionList) {
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
                        /*if (MmsApp.mDarkStyle) {
                            mEncryptItem.setIcon (mBeingEncrypt ? R.drawable.gn_conv_encryption_light_bg : R.drawable.gn_conv_dncryption_light_bg);
                        } else {
                            mEncryptItem.setIcon (mBeingEncrypt ? R.drawable.gn_conv_encryption_bg : R.drawable.gn_conv_dncryption_bg);
                        }*/
                    }
                } else {
                    /*if (getSelected().size() > 0) {
                        if (MmsApp.mDarkStyle) {
                            mEncryptItem.setIcon(R.drawable.gn_conv_dncryption_light_bg);
                        } else {
                            mEncryptItem.setIcon(R.drawable.gn_conv_dncryption);
                        }
                    }*/
                }
                mSelectCount = null != getSelected() ? getSelected().size() : 0;
                if (null != mEncryptItem) {
                    mEncryptItem.setEnabled(mSelectCount == 0 ? false : true);
                    /*if (mSelectCount == 0) {
                        if (MmsApp.mDarkStyle) {
                            mEncryptItem.setIcon(R.drawable.gn_conv_dncryption_unuse);
                        } else {
                            mEncryptItem.setIcon(R.drawable.gn_conv_encryption_unuse);
                        }
                    }*/
                }
                if (null != mDeleteItem) {
                    if (MmsApp.mDarkStyle) {
                        /*if (mSelectCount == 0) {
                            mDeleteItem.setIcon(R.drawable.gn_com_delete_unuse_dark_bg);
                        } else {
                            mDeleteItem.setIcon(R.drawable.gn_com_delete_dark_bg);
                        }*/
                    }
                    mDeleteItem.setEnabled(mSelectCount == 0 ? false : true);
                }
                // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//                ((AuroraActivity)getActivity()).getActionBar().updateActionMode();
                // Aurora liugj 2013-09-13 deleted for aurora's new feature end
            }
            
            public void bindToAdapter(GnSelectionManager<Long> selectionManager) {
                // TODO Auto-generated method stub
                mListAdapter.setSelectionManager(selectionManager);
            }
            
            public void onDestroyActionMode(ActionMode mode) {
                super.onDestroyActionMode(mode);
                // gionee zhouyj 2012-10-12 modify for CR00711214 start 
                mListAdapter.showCheckBox(false);
                // gionee zhouyj 2012-10-12 modify for CR00711214 end 
                // gionee zhouyj 2012-10-26 modify for CR00718476 start 
                if (!DraftFragment.isMultiMode()) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }
                sInMultiMode = false;
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
    
    private void deleteConversations() {
        if(mAllSelectedThreadIds.size() > 0) {
            if (MmsApp.mEncryption && !isEncryptionList) {
                if (checkoutEncryption()) {
                    final Intent intent = new Intent();
                    intent.setClass(getActivity(), MsgChooseLockPassword.class);
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
                    if (MmsApp.mGnHideEncryption && MmsApp.mGnSmartGuide && !ReadPopTag(TabActivity.sContext, FIRSTENCRYPTION)) {
                        WritePopTag(TabActivity.sContext, FIRSTENCRYPTION);
                    }
                    //Gionee <gaoj> <2013-05-28> add for CR00817770 end
                    encryptSelectsConv(true);
                    Toast.makeText(TabActivity.sContext, TabActivity.sContext.getString(R.string.gn_multi_encryption), Toast.LENGTH_SHORT).show();
                } else {
                    final Intent intent = new Intent();
                    intent.setClass(getActivity(), MsgChooseLockPassword.class);
                    intent.putExtra("isdecryption", true);
                    startActivityForResult(intent,
                            ConvFragment.CONFIRM_DECRYPTION_PASSWORD_REQUEST);
                    return false;
                }
            }
        } else {
            encryptSelectsConv(false);
            Toast.makeText(TabActivity.sContext, TabActivity.sContext.getString(R.string.gn_multi_dncryption), Toast.LENGTH_SHORT).show();
        }
        return true;
    }
    
    public void leaveForChanged() {
        if (null != mActionModeHandler && mActionModeHandler.inSelectionMode()) {
            mActionModeHandler.leaveSelectionMode();
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
        int len = mAllSelectedThreadIds.size();
        if (len == 1) {
            startQueryStaredMsg(mAllSelectedThreadIds.get(0));
        } else {
            deleteThreads();
            boolean hasStar = false;
            queryFavoriteList();
            for (int i = 0; i < mFavoriThreadsList.size(); i++) {
                if (mAllSelectedThreadIds.contains(mFavoriThreadsList.get(i))) {
                    hasStar = true;
                    break;
                }
            }
            boolean isCheckAll = false;
            if (mAllSelectedThreadIds.size() == mListView.getCount() && !isEncryptionList && !mHideEncryp) {
                isCheckAll = true;
            }
            //Gionee <zhouyj> <2013-04-11> modify for CR00796238 start
            confirmDeleteGnThreadDialog(new GnDeleteThreadListener((isCheckAll) ? -1 : 9999,
                    mQueryHandler, getActivity()), isCheckAll, hasStar,
                    getActivity());
            //Gionee <zhouyj> <2013-04-11> modify for CR00796238 end
        }
    }
    // gionee zhouyj 2012-11-08 modify for CR00725666 end 

    private void initThreadsMap() {
        // TODO Auto-generated method stub
        Cursor cursor = mListAdapter.getCursor();
        cursor.moveToPosition(-1);
        int i = 0;
        while(cursor.moveToNext()) {
            if(cursor.getInt(13) == 1 && !mSetEncryption.contains(cursor.getLong(0))) {
                mSetEncryption.add(cursor.getLong(0));
            }
            if (mThreadsMap.get(i) == null) {
                mThreadsMap.put(i++, cursor.getLong(0));
            }
        }
    }
    //gionee gaoj 2012-9-20 added for CR00699291 end

    //gionee gaoj 2012-10-15 added for CR00705539 start
    private static final int BatchDeleteNum = 900;
    private static ArrayList<String> mThreadsList = new ArrayList<String>();
    public static final int THREAD_LIST_FAVORITE_QUERY_ONE_TOKEN = 1013;
    public static final int THREAD_LIST_FAVORITE_QUERY_ALL_TOKEN = 1014;

    public static final int DELETE_CONVERSATION_NOT_LAST_TOKEN      = 1901;

    private static void deleteThreads() {
        mThreadsList.clear();
        long threadId;
        StringBuilder buf = new StringBuilder();
        int i = 0;
        int j = 1;
        Iterator<Long> it = mAllSelectedThreadIds.iterator();
        while(it.hasNext()) {
            threadId = it.next();
            if(i++ > 0) {
                buf.append(" OR thread_id = ");
            }
            buf.append(Long.toString(threadId));
            if (i == BatchDeleteNum * j) {
                mThreadsList.add(buf.toString());
                buf = new StringBuilder();
                j++;
            }
        }
        mThreadsList.add(buf.toString());
    }

    private static ArrayList<Long> mFavoriThreadsList = new ArrayList<Long>();

    private final static String[] FAVORITE_THREADS_PROJECTION = new String[] {
        Conversations.THREAD_ID
    };

    private void queryFavoriteList() {
        Cursor cursor = getActivity().getContentResolver().query(Sms.CONTENT_URI, FAVORITE_THREADS_PROJECTION, "star=1",
                null, null);
        mFavoriThreadsList.clear();
        if (cursor != null) {
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                Long threadid = cursor.getLong(0);
                mFavoriThreadsList.add(threadid);
            }
            cursor.close();
        }
    }
    //gionee gaoj 2012-10-15 added for CR00705539 end
    
    // gionee zhouyj 2012-10-26 add for CR00718476 start 
    public static boolean isMultiMode() {
        return sInMultiMode;
    }
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
        if (isEncryptionList || (null != mActionModeHandler && mActionModeHandler.inSelectionMode())) {
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
        ContentResolver resolver = TabActivity.sContext.getContentResolver();
        String[] THREADS_QUERY_COLUMNS = { "encryption" };
        Cursor cursor = null;
        try {
            cursor = resolver.query(Conversation.sAllThreadsUri, null,
                    "encryption = 1", null, null);
            Log.d("ConvFragment", ""+cursor.getCount());
            if (cursor != null && cursor.getCount() > 0) {
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
            SqliteWrapper.checkSQLiteException(getActivity(), e);
        }
    }
    //Gionee <gaoj> <2013-05-21> added for CR00817770 end

    //Gionee <gaoj> <2013-05-28> add for CR00817770 begin
    

    private void OpenPopLayout() {
        FrameLayout frameLayout = (FrameLayout) getView().findViewById(R.id.gn_conv_frame_layout);
        View AuroraSmartPopupLayout = initPopupLayout(TabActivity.sContext);
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
        //linearLayout.setBackgroundResource(R.drawable.gn_conv_bg_popup);
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
        Toast.makeText(TabActivity.sContext, resId, Toast.LENGTH_SHORT).show();
    }
    //Gionee <gaoj> <2013-05-28> add for CR00817770 end
}
