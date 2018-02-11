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
import java.util.Collection;
import java.util.HashSet;
import com.android.internal.telephony.ITelephony;
import com.android.mms.MmsConfig;
import com.android.mms.LogTag;
import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.MmsApp;
import com.android.mms.transaction.CBMessagingNotification;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.transaction.SmsRejectedReceiver;
import com.android.mms.transaction.WapPushMessagingNotification;
import com.android.mms.util.DraftCache;
import com.android.mms.util.Recycler;
// Aurora liugj 2013-09-13 added for aurora's new feature start
import com.aurora.mms.ui.AuroraConvListActivity;
// Aurora liugj 2013-09-13 added for aurora's new feature end
//gionee gaoj 2012-5-30 added for CR00555790 start
// Aurora liugj 2013-09-13 deleted for aurora's new feature start
//import com.gionee.mms.ui.TabActivity;
// Aurora liugj 2013-09-13 deleted for aurora's new feature end
//gionee gaoj 2012-5-30 added for CR00555790 end
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.pdu.PduHeaders;
//Aurora xuyong 2013-11-15 modified for google adapt end
import com.aurora.featureoption.FeatureOption;
import com.gionee.internal.telephony.GnPhone;
import gionee.telephony.GnSmsManager;
import gionee.app.GnStatusBarManager;

import android.database.sqlite.SqliteWrapper;
import gionee.provider.GnSettings;
import android.app.StatusBarManager;
// Aurora liugj 2013-09-13 modified for aurora's new feature start
import android.app.ActionBar;
// Aurora liugj 2013-09-13 modified for aurora's new feature end
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraListActivity;
import aurora.app.AuroraProgressDialog;
import android.app.SearchManager;
import android.app.SearchManager.OnDismissListener;
import android.app.SearchableInfo;
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
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.os.Bundle;
import android.os.Handler;
import android.os.ServiceManager;
import aurora.preference.AuroraPreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.Telephony.Mms;
import gionee.provider.GnTelephony.SIMInfo;
import android.util.AttributeSet;
import gionee.provider.GnTelephony.Threads;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnKeyListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import aurora.widget.AuroraButton;
import android.widget.CheckBox;
import aurora.widget.AuroraListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.net.Uri;

import com.mediatek.wappush.SiExpiredCheck;
import com.aurora.featureoption.FeatureOption;
import java.util.List;
import android.text.TextUtils;
import android.os.SystemProperties; 
//gionee gaoj 2012-4-10 added for CR00555790 start
import android.graphics.Color;
//gionee gaoj 2012-4-10 added for CR00555790 end
/**
 * This activity provides a list view of existing conversations.
 */
public class ConversationList extends AuroraListActivity implements DraftCache.OnDraftChangedListener {
    private static final String TAG = "ConversationList";
    private static final String CONV_TAG = "Mms/convList";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = DEBUG;

    private static final int THREAD_LIST_QUERY_TOKEN       = 1701;
    private static final int UNREAD_THREADS_QUERY_TOKEN    = 1702;
    public static final int DELETE_CONVERSATION_TOKEN      = 1801;
    public static final int HAVE_LOCKED_MESSAGES_TOKEN     = 1802;
    private static final int DELETE_OBSOLETE_THREADS_TOKEN = 1803;

    // IDs of the context menu items for the list of conversations.
    public static final int MENU_DELETE               = 0;
    public static final int MENU_VIEW                 = 1;
    public static final int MENU_VIEW_CONTACT         = 2;
    public static final int MENU_ADD_TO_CONTACTS      = 3;
    public static final int MENU_SIM_SMS              = 4;
    public static final int MENU_CHANGEVIEW           = 6;
    private ThreadListQueryHandler mQueryHandler;
    private ConversationListAdapter mListAdapter;
    private CharSequence mTitle;
    private SharedPreferences mPrefs;
    private Handler mHandler;
    private boolean mNeedToMarkAsSeen;
    private TextView mUnreadConvCount;

    private MenuItem mSearchItem;
    private SearchView mSearchView;
    private StatusBarManager mStatusBarManager;
    //wappush: indicates the type of thread, this exits already, but has not been used before
    private int mType;
    //wappush: SiExpired Check
    private SiExpiredCheck siExpiredCheck;
    //wappush: wappush TAG
    private static final String WP_TAG = "Mms/WapPush";
    static private final String CHECKED_MESSAGE_LIMITS = "checked_message_limits";
    private static int CHANGE_SCROLL_LISTENER_MIN_CURSOR_COUNT = 100;
    // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
    //private PostDrawListener mPostDrawListener = new PostDrawListener();
    // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
     // Aurora liugj 2014-02-07 modified for xuqiu:searchview scroll with listview start
    private MyScrollListener mScrollListener = new MyScrollListener(CHANGE_SCROLL_LISTENER_MIN_CURSOR_COUNT, "MyConversationList_Scroll_Tread");
     // Aurora liugj 2014-02-07 modified for xuqiu:searchview scroll with listview end

    // If adapter data is valid
    private boolean mDataValid;
    private boolean mDisableSearchFalg = false;
    // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
    /*private class PostDrawListener implements android.view.ViewTreeObserver.OnPostDrawListener {
        @Override
        public boolean onPostDraw() {
            Log.i("AppLaunch", "[AppLaunch] MMS onPostDraw");
            return true;
        }
    }*/
    // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        //gionee gaoj 2012-5-30 added for CR00555790 start
        if (MmsApp.mGnMessageSupport){
            // Aurora liugj 2013-09-13 modified for aurora's new feature start
            Intent intent = new Intent(this, AuroraConvListActivity.class);
            // Aurora liugj 2013-09-13 modified for aurora's new feature end
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return;
        }
        //gionee gaoj 2012-5-30 added for CR00555790 end
        setContentView(R.layout.conversation_list_screen);

        mStatusBarManager = (StatusBarManager)getSystemService(Context.STATUS_BAR_SERVICE);
        mQueryHandler = new ThreadListQueryHandler(getContentResolver());

        AuroraListView listView = getListView();
        listView.setOnCreateContextMenuListener(mConvListOnCreateContextMenuListener);
        listView.setOnKeyListener(mThreadListKeyListener);
        listView.setChoiceMode(AuroraListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new ModeCallback());
        listView.setOnScrollListener(mScrollListener);

        // Tell the list view which view to display when the list is empty
        View emptyView = findViewById(R.id.empty);
        listView.setEmptyView(emptyView);

        initListAdapter();

        setupActionBar();

        mTitle = getString(R.string.app_label);

        mHandler = new Handler();
        mPrefs = AuroraPreferenceManager.getDefaultSharedPreferences(this);
        boolean checkedMessageLimits = mPrefs.getBoolean(CHECKED_MESSAGE_LIMITS, false);
        if (DEBUG) Log.v(TAG, "checkedMessageLimits: " + checkedMessageLimits);
        if (!checkedMessageLimits || DEBUG) {
            runOneTimeStorageLimitCheckForLegacyMessages();
        }
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            siExpiredCheck = new SiExpiredCheck(this);
            siExpiredCheck.startSiExpiredCheckThread();
        }
    }

    private void setupActionBar() {
        // Aurora liugj 2013-09-13 modified for aurora's new feature start
        ActionBar actionBar = getActionBar();
        // Aurora liugj 2013-09-13 modified for aurora's new feature end

        ViewGroup v = (ViewGroup)LayoutInflater.from(this)
            .inflate(R.layout.conversation_list_actionbar, null);
        // Aurora liugj 2013-09-13 modified for aurora's new feature start
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(v,
                new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                        ActionBar.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER_VERTICAL | Gravity.RIGHT));
        // Aurora liugj 2013-09-13 modified for aurora's new feature end

        mUnreadConvCount = (TextView)v.findViewById(R.id.unread_conv_count);
    }

    private final ConversationListAdapter.OnContentChangedListener mContentChangedListener =
        new ConversationListAdapter.OnContentChangedListener() {
        public void onContentChanged(ConversationListAdapter adapter) {
            startAsyncQuery();
        }
    };

    private void initListAdapter() {
        mListAdapter = new ConversationListAdapter(this, null);
        mListAdapter.setOnContentChangedListener(mContentChangedListener);
        setListAdapter(mListAdapter);
        getListView().setRecyclerListener(mListAdapter);
    }

    @Override
    public void onBackPressed() {
        if (isTaskRoot()) {
            // Instead of stopping, simply push this to the back of the stack.
            // This is only done when running at the top of the stack;
            // otherwise, we have been launched by someone else so need to
            // allow the user to go back to the caller.
            moveTaskToBack(false);
        } else {
            super.onBackPressed();
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
            if (DEBUG) Log.v(TAG, "recycler is already turned on");
            // The recycler is already turned on. We don't need to check anything or warn
            // the user, just remember that we've made the check.
            markCheckedMessageLimit();
            return;
        }
        new Thread(new Runnable() {
            public void run() {
                if (Recycler.checkForThreadsOverLimit(ConversationList.this)) {
                    if (DEBUG) Log.v(TAG, "checkForThreadsOverLimit TRUE");
                    // Dang, one or more of the threads are over the limit. Show an activity
                    // that'll encourage the user to manually turn on the setting. Delay showing
                    // this activity until a couple of seconds after the conversation list appears.
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            Intent intent = new Intent(ConversationList.this,
                                    WarnOfStorageLimitsActivity.class);
                            startActivity(intent);
                        }
                    }, 2000);
                }// else {
                 //   if (DEBUG) Log.v(TAG, "checkForThreadsOverLimit silently turning on recycler");
                 //     No threads were over the limit. Turn on the recycler by default.
                 //   runOnUiThread(new Runnable() {
                 //       public void run() {
                 //           SharedPreferences.Editor editor = mPrefs.edit();
                 //           editor.putBoolean(MessagingPreferenceActivity.AUTO_DELETE, true);
                 //           editor.apply();
                 //       }
                 //   });
                 // }
                // Remember that we don't have to do the check anymore when starting MMS.
                runOnUiThread(new Runnable() {
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
        if (DEBUG) Log.v(TAG, "markCheckedMessageLimit");
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(CHECKED_MESSAGE_LIMITS, true);
        editor.apply();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // Handle intents that occur after the activity has already been created.
        //Gionee <zhouyj> <2013-06-25> add for CR00824278 begin
        if (MmsApp.mGnMessageSupport){
            // Aurora liugj 2013-09-13 modified for aurora's new feature start
            Intent i = new Intent(this, AuroraConvListActivity.class);
            // Aurora liugj 2013-09-13 modified for aurora's new feature end
            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
            return;
        }
        //Gionee <zhouyj> <2013-06-25> add for CR00824278 end
        startAsyncQuery();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            siExpiredCheck.startExpiredCheck();
        }
        ComposeMessageActivity.mDestroy = true;

        Handler mShowIndicatorHandler = new Handler();
        // Gionee lixiaohu 2012-08-28 added for CR00681687 start
        if (!MmsApp.mGnMultiSimMessage) {
        final ComponentName name = getComponentName();
        GnStatusBarManager.hideSIMIndicator(mStatusBarManager, name);
        GnStatusBarManager.showSIMIndicator(mStatusBarManager, name, GnSettings.System.SMS_SIM_SETTING);
        }
        // Gionee lixiaohu 2012-08-28 added for CR00681687 end

    }

    @Override
    protected void onPause() {
        // Gionee lixiaohu 2012-08-28 added for CR00681687 start
        if (!MmsApp.mGnMultiSimMessage) {
        GnStatusBarManager.hideSIMIndicator(mStatusBarManager, getComponentName());
        }
        // Gionee lixiaohu 2012-08-28 added for CR00681687 end
        super.onPause();
    }
    @Override
    protected void onStart() {
        super.onStart();
        MmsConfig.setMmsDirMode(false);
        Log.i(TAG,"[Performance test][Mms] loading data start time ["
            + System.currentTimeMillis() + "]" );
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
        //getWindow().getDecorView().getViewTreeObserver().addOnPostDrawListener(mPostDrawListener);
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
        MessagingNotification.cancelNotification(getApplicationContext(),
                SmsRejectedReceiver.SMS_REJECTED_NOTIFICATION_ID);

        DraftCache.getInstance().addOnDraftChangedListener(this);
        mNeedToMarkAsSeen = true;
        startAsyncQuery();
        // We used to refresh the DraftCache here, but
        // refreshing the DraftCache each time we go to the ConversationList seems overly
        // aggressive. We already update the DraftCache when leaving CMA in onStop() and
        // onNewIntent(), and when we delete threads or delete all in CMA or this activity.
        // I hope we don't have to do such a heavy operation each time we enter here.
        // new: third party may add/delete draft, and we must refresh to check this.
        DraftCache.getInstance().refresh();
        // we invalidate the contact cache here because we want to get updated presence
        // and any contact changes. We don't invalidate the cache by observing presence and contact
        // changes (since that's too untargeted), so as a tradeoff we do it here.
        // If we're in the middle of the app initialization where we're loading the conversation
        // threads, don't invalidate the cache because we're in the process of building it.
        // TODO: think of a better way to invalidate cache more surgically or based on actual
        // TODO: changes we care about
        if (!Conversation.loadingThreads()) {
            Contact.invalidateCache();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
        //getWindow().getDecorView().getViewTreeObserver().removeOnPostDrawListener(mPostDrawListener);
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
        DraftCache.getInstance().removeOnDraftChangedListener(this);

        // Simply setting the choice mode causes the previous choice mode to finish and we exit
        // multi-select mode (if we're in it) and remove all the selections.
        getListView().setChoiceMode(AuroraListView.CHOICE_MODE_MULTIPLE_MODAL);

        //mListAdapter.changeCursor(null);
        //wappush: stop expiration check when exit
        Log.i(WP_TAG, "ConversationList: " + "stopExpiredCheck");
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            siExpiredCheck.stopExpiredCheck();
        }
    }

    @Override
    protected void onDestroy() {
        //gionee gaoj 2012-5-30 added for CR00555790 start
        if (MmsApp.mGnMessageSupport) {
            super.onDestroy();
            return;
        }
        //gionee gaoj 2012-5-30 added for CR00555790 end
        //stop the si expired check thread
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            siExpiredCheck.stopSiExpiredCheckThread();
        }
        mScrollListener.destroyThread();
        super.onDestroy();
    }

    public void onDraftChanged(final long threadId, final boolean hasDraft) {
        // Run notifyDataSetChanged() on the main thread.
        mQueryHandler.post(new Runnable() {
            public void run() {
                if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                    log("onDraftChanged: threadId=" + threadId + ", hasDraft=" + hasDraft);
                }
                mListAdapter.notifyDataSetChanged();
            }
        });
    }

    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(mDisableSearchFalg){
            switch (keyCode) {
                case KeyEvent.KEYCODE_SEARCH:
                    // do nothing since we don't want search box which may cause UI crash
                    // TODO: mapping to other useful feature
                    return true;
                default:
                    break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }   
    
    private void startAsyncQuery() {
        try {
            setTitle(getString(R.string.refreshing));
            setProgressBarIndeterminateVisibility(true);

            Conversation.startQueryForAll(mQueryHandler, THREAD_LIST_QUERY_TOKEN);
            // Aurora xuyong 2014-10-23 modified for privacy feature start
            Conversation.startQuery(mQueryHandler, UNREAD_THREADS_QUERY_TOKEN, Threads.READ + "=0", false);
            // Aurora xuyong 2014-10-23 modified for privacy feature end
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(this, e);
        }
    }

    SearchView.OnQueryTextListener mQueryTextListener = new SearchView.OnQueryTextListener() {
           // Aurora liugj 2013-10-25 modified for fix bug-241 start 
        public boolean onQueryTextSubmit(String query) {
            /*Intent intent = new Intent();
            intent.setClass(ConversationList.this, SearchActivity.class);
            intent.putExtra(SearchManager.QUERY, query);
            startActivity(intent);
            mSearchItem.collapseActionView();*/
            return true;
        }
          // Aurora liugj 2013-10-25 modified for fix bug-241 end

        public boolean onQueryTextChange(String newText) {
            return false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.conversation_list_menu, menu);
        
        mSearchItem = menu.findItem(R.id.search);
        mSearchView = (SearchView) mSearchItem.getActionView();

        mSearchView.setOnQueryTextListener(mQueryTextListener);
        mSearchView.setQueryHint(getString(R.string.search_hint));
        mSearchView.setIconifiedByDefault(true);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        if (searchManager != null) {
            SearchableInfo info = searchManager.getSearchableInfo(this.getComponentName());
            mSearchView.setSearchableInfo(info);
        }
        //MTK_OP01_PROTECT_START
        if (MmsApp.isTelecomOperator()) {
            menu.add(0, MENU_CHANGEVIEW, 0, R.string.changeview);
        }
        //MTK_OP01_PROTECT_END
        
        //MTK_OP02_PROTECT_START
        if (MmsApp.isUnicomOperator()) {
            /*menu.add(0, MENU_SIM_SMS, 0, R.string.menu_sim_sms).setIcon(
                    R.drawable.ic_menu_sim_sms);*/
            MenuItem item = menu.findItem(MENU_SIM_SMS);
            List<SIMInfo> listSimInfo = SIMInfo.getInsertedSIMList(this);
            if(listSimInfo == null || listSimInfo.isEmpty()){
                item.setEnabled(false);
                Log.d(TAG, "onCreateOptionsMenu MenuItem setEnabled(false) optr = " 
                         + MmsApp.getApplication().getOperator());
            }
        }
        //MTK_OP02_PROTECT_END
        //MTK_OP01_PROTECT_START
        else if (MmsApp.isTelecomOperator()) {
           /* menu.add(0, MENU_SIM_SMS, 0, R.string.menu_sim_sms).setIcon(
                    R.drawable.ic_menu_sim_sms);*/
            MenuItem item = menu.findItem(MENU_SIM_SMS);
            List<SIMInfo> listSimInfo = SIMInfo.getInsertedSIMList(this);
            if(listSimInfo == null || listSimInfo.isEmpty()){
                item.setEnabled(false);
                Log.d(TAG, "onCreateOptionsMenu MenuItem setEnabled(false) optr = "
                         + MmsApp.getApplication().getOperator());
            }
        }
        //MTK_OP01_PROTECT_END

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_delete_all);
        if (item != null) {
            mDataValid = mListAdapter.isDataValid();
            item.setVisible(mListAdapter.getCount() > 0);
        }
        if (!LogTag.DEBUG_DUMP) {
            item = menu.findItem(R.id.action_debug_dump);
            if (item != null) {
                item.setVisible(false);
            }
        }
        item = menu.findItem(R.id.action_omacp);
        item.setVisible(false);
        Context otherAppContext = null;
        try{
            otherAppContext = this.createPackageContext("com.mediatek.omacp", 
                    Context.CONTEXT_IGNORE_SECURITY);
        } catch(Exception e) {
            Log.e(CONV_TAG, "ConversationList NotFoundContext");
        }
        if (null != otherAppContext) {
            SharedPreferences sp = otherAppContext.getSharedPreferences("omacp", 
                    MODE_WORLD_READABLE | MODE_MULTI_PROCESS);
            boolean omaCpShow = sp.getBoolean("configuration_msg_exist", false);
            if(omaCpShow) {  
                item.setVisible(true);
            }
        }
        return true;
    }

    @Override
    public boolean onSearchRequested() {
        mSearchItem.expandActionView();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_compose_new:
                createNewMessage();
                break;
            case R.id.action_delete_all:
                // The invalid threadId of -1 means all threads here.
                confirmDeleteThread(-1L, mQueryHandler);
                break;
            case R.id.action_settings:
                Intent intent = new Intent(this, MessagingPreferenceActivity.class);
                startActivityIfNeeded(intent, -1);
                break;
            //MTK_OP02_PROTECT_START
            case MENU_SIM_SMS:
                if(FeatureOption.MTK_GEMINI_SUPPORT == true){
                    List<SIMInfo> listSimInfo = SIMInfo.getInsertedSIMList(this);
                    if (listSimInfo.size() > 1) { 
                        Intent simSmsIntent = new Intent();
                        simSmsIntent.setClass(this, SelectCardPreferenceActivity.class);
                        simSmsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        simSmsIntent.putExtra("preference", MessagingPreferenceActivity.SMS_MANAGE_SIM_MESSAGES);
                        startActivity(simSmsIntent);
                    } else {  
                        Intent simSmsIntent = new Intent();
                        simSmsIntent.setClass(this, ManageSimMessages.class);
                        simSmsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        simSmsIntent.putExtra("SlotId", listSimInfo.get(0).mSlot); 
                        startActivity(simSmsIntent);
                    }
                } else { 
                    startActivity(new Intent(this, ManageSimMessages.class));
                }
                break;
            //MTK_OP02_PROTECT_END
            case R.id.action_omacp:
                Intent omacpintent = new Intent();
                omacpintent.setClassName("com.mediatek.omacp", "com.mediatek.omacp.message.OmacpMessageList");
                omacpintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityIfNeeded(omacpintent, -1);
                break;
            case R.id.action_debug_dump:
                LogTag.dumpInternalTables(this);
                break;
            //MTK_OP01_PROTECT_START
            case MENU_CHANGEVIEW:
                MmsConfig.setMmsDirMode(true);
                MessageUtils.updateNotification(this);
                Intent it = new Intent(this, FolderViewList.class);
                it.putExtra("floderview_key", FolderViewList.OPTION_INBOX);// show inbox by default
                startActivity(it);
                finish();
                break;
            //MTK_OP01_PROTECT_END
            default:
                return true;
        }
        return false;
    }

    @Override
    protected void onListItemClick(AuroraListView l, View v, int position, long id) {
        // Note: don't read the thread id data from the ConversationListItem view passed in.
        // It's unreliable to read the cached data stored in the view because the ListItem
        // can be recycled, and the same view could be assigned to a different position
        // if you click the list item fast enough. Instead, get the cursor at the position
        // clicked and load the data from the cursor.
        // (ConversationListAdapter extends CursorAdapter, so getItemAtPosition() should
        // return the cursor object, which is moved to the position passed in)
        Cursor cursor  = (Cursor) getListView().getItemAtPosition(position);
            //klocwork issue pid:18182
            if (cursor == null) {
                return;
            }
        Conversation conv = Conversation.from(this, cursor);
        long tid = conv.getThreadId();

        //wappush: modify the calling of openThread, add one parameter
        if (LogTag.VERBOSE) {
            Log.d(TAG, "onListItemClick: pos=" + position + ", view=" + v + ", tid=" + tid);
        }

        Log.i(WP_TAG, "ConversationList: " + "conv.getType() is : " + conv.getType());
        openThread(tid, conv.getType());
    }

    private void createNewMessage() {
        startActivity(ComposeMessageActivity.createIntent(this, 0));
    }

    private void openThread(long threadId, int type) {
        if(FeatureOption.MTK_WAPPUSH_SUPPORT == true){
            //wappush: add opptunities for starting wappush activity if it is a wappush thread 
            //type: Threads.COMMON_THREAD, Threads.BROADCAST_THREAD and Threads.WAP_PUSH
            if(type == Threads.WAPPUSH_THREAD){
                startActivity(WPMessageActivity.createIntent(this, threadId));            
            } else if (type == Threads.CELL_BROADCAST_THREAD) {
                startActivity(CBMessageListActivity.createIntent(this, threadId));                
            } else {
                startActivity(ComposeMessageActivity.createIntent(this, threadId));
            }
        }else{
            if (type == Threads.CELL_BROADCAST_THREAD) {
                startActivity(CBMessageListActivity.createIntent(this, threadId));                
            } else {
                startActivity(ComposeMessageActivity.createIntent(this, threadId));
            }
        }
    }

    public static Intent createAddContactIntent(String address) {
        // address must be a single recipient
        Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        
        //gionee gaoj 2013-4-2 added for CR00792780 start
        intent.setComponent(new ComponentName("com.android.contacts",
        "com.android.contacts.activities.ContactSelectionActivity"));
        //gionee gaoj 2013-4-2 added for CR00792780 end
        
        intent.setType(Contacts.CONTENT_ITEM_TYPE);
        if (Mms.isEmailAddress(address)) {
            intent.putExtra(ContactsContract.Intents.Insert.EMAIL, address);
        } else {
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, address);
            intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        return intent;
    }

    private final OnCreateContextMenuListener mConvListOnCreateContextMenuListener =
        new OnCreateContextMenuListener() {
        public void onCreateContextMenu(ContextMenu menu, View v,
                ContextMenuInfo menuInfo) {
            Cursor cursor = mListAdapter.getCursor();
            if (cursor == null || cursor.getPosition() < 0) {
                return;
            }
            Conversation conv = Conversation.from(ConversationList.this, cursor);
            //wappush: get the added mType value
            mType = conv.getType();
            Log.i(WP_TAG, "ConversationList: " + "mType is : " + mType);   

            ContactList recipients = conv.getRecipients();
            menu.setHeaderTitle(recipients.formatNames(","));

            AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.add(0, MENU_VIEW, 0, R.string.menu_view);

            // Only show if there's a single recipient
            if (recipients.size() == 1) {
                // do we have this recipient in contacts?
                if (recipients.get(0).existsInDatabase()) {
                    menu.add(0, MENU_VIEW_CONTACT, 0, R.string.menu_view_contact);
                } else {
                    menu.add(0, MENU_ADD_TO_CONTACTS, 0, R.string.menu_add_to_contacts);
                }
            }
            menu.add(0, MENU_DELETE, 0, R.string.menu_delete);
        }
    };

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Cursor cursor = mListAdapter.getCursor();
        if (cursor != null && cursor.getPosition() >= 0) {
            Conversation conv = Conversation.from(ConversationList.this, cursor);
            long threadId = conv.getThreadId();
            switch (item.getItemId()) {
            case MENU_DELETE: {
                confirmDeleteThread(threadId, mQueryHandler);
                break;
            }
            case MENU_VIEW: {
                openThread(threadId, mType);
                break;
            }
            case MENU_VIEW_CONTACT: {
                Contact contact = conv.getRecipients().get(0);
                Intent intent = new Intent(Intent.ACTION_VIEW, contact.getUri());
                
                //gionee gaoj 2013-4-2 added for CR00792780 start
                intent.setComponent(new ComponentName("com.android.contacts",
                "com.android.contacts.activities.ContactDetailActivity"));
                //gionee gaoj 2013-4-2 added for CR00792780 end
                
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                startActivity(intent);
                break;
            }
            case MENU_ADD_TO_CONTACTS: {
                String address = conv.getRecipients().get(0).getNumber();
                startActivity(createAddContactIntent(address));
                break;
            }
            default:
                break;
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // We override this method to avoid restarting the entire
        // activity when the keyboard is opened (declared in
        // AndroidManifest.xml).  Because the only translatable text
        // in this activity is "New Message", which has the full width
        // of phone to work with, localization shouldn't be a problem:
        // no abbreviated alternate words should be needed even in
        // 'wide' languages like German or Russian.

        super.onConfigurationChanged(newConfig);
        if (DEBUG) Log.v(TAG, "onConfigurationChanged: " + newConfig);
    }

    /**
     * Start the process of putting up a dialog to confirm deleting a thread,
     * but first start a background query to see if any of the threads or thread
     * contain locked messages so we'll know how detailed of a UI to display.
     * @param threadId id of the thread to delete or -1 for all threads
     * @param handler query handler to do the background locked query
     */
    public static void confirmDeleteThread(long threadId, AsyncQueryHandler handler) {
        ArrayList<Long> threadIds = null;
        if (threadId != -1) {
            threadIds = new ArrayList<Long>();
            threadIds.add(threadId);
        }
        confirmDeleteThreads(threadIds, handler);
    }

    /**
     * Start the process of putting up a dialog to confirm deleting threads,
     * but first start a background query to see if any of the threads
     * contain locked messages so we'll know how detailed of a UI to display.
     * @param threadIds list of threadIds to delete or null for all threads
     * @param handler query handler to do the background locked query
     */
    public static void confirmDeleteThreads(Collection<Long> threadIds, AsyncQueryHandler handler) {
        Conversation.startQueryHaveLockedMessages(handler, threadIds,
                HAVE_LOCKED_MESSAGES_TOKEN);
    }

    /**
     * Build and show the proper delete thread dialog. The UI is slightly different
     * depending on whether there are locked messages in the thread(s) and whether we're
     * deleting single/multiple threads or all threads.
     * @param listener gets called when the delete button is pressed
     * @param deleteAll whether to show a single thread or all threads UI
     * @param hasLockedMessages whether the thread(s) contain locked messages
     * @param context used to load the various UI elements
     */
    public static void confirmDeleteThreadDialog(final DeleteThreadListener listener,
            Collection<Long> threadIds,
            boolean hasLockedMessages,
            Context context) {
        View contents = View.inflate(context, R.layout.delete_thread_dialog_view, null);
        TextView msg = (TextView)contents.findViewById(R.id.message);

        //gionee gaoj 2012-5-5 added for CR00555790 start
        /*if (MmsApp.mLightTheme) {
            msg.setTextColor(Color.WHITE);
        }*/
        //gionee gaoj 2012-5-5 added for CR00555790 end
        if (threadIds == null) {
            msg.setText(R.string.confirm_delete_all_conversations);
        } else {
            // Show the number of threads getting deleted in the confirmation dialog.
            int cnt = threadIds.size();
            msg.setText(context.getResources().getQuantityString(
                R.plurals.confirm_delete_conversation, cnt, cnt));
        }

        final CheckBox checkbox = (CheckBox)contents.findViewById(R.id.delete_locked);
        //gionee gaoj 2012-5-5 added for CR00555790 start
        /*if (MmsApp.mLightTheme) {
            checkbox.setTextColor(Color.WHITE);
        }*/
        //gionee gaoj 2012-5-5 added for CR00555790 end
        //gionee gaoj 2012-5-16 remove locked for CRCR00600687 start
        if (MmsApp.mGnMessageSupport) {
            checkbox.setVisibility(View.GONE);
        } else {
        if (!hasLockedMessages) {
            checkbox.setVisibility(View.GONE);
        } else {
            listener.setDeleteLockedMessage(checkbox.isChecked());
            checkbox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    listener.setDeleteLockedMessage(checkbox.isChecked());
                }
            });
        }
        }
        //gionee gaoj 2012-5-16 remove locked for CRCR00600687 end

        AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(context/*, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN*/);
        builder.setTitle(R.string.confirm_dialog_title)
        //Gionee <Gaoj> <2013-05-22> delete CR00818422 begin
            /*.setIconAttribute(android.R.attr.alertDialogIcon)*/
            //Gionee <Gaoj> <2013-05-22> delete CR00818422 end
            .setCancelable(true)
            .setPositiveButton(R.string.delete, listener)
            .setNegativeButton(R.string.no, null)
            .setView(contents)
            .show();
    }

    private final OnKeyListener mThreadListKeyListener = new OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DEL: {
                        long id = getListView().getSelectedItemId();
                        if (id > 0) {
                            confirmDeleteThread(id, mQueryHandler);
                        }
                        return true;
                    }
                }
            }
            return false;
        }
    };

    public static class DeleteThreadListener implements OnClickListener {
        private final Collection<Long> mThreadIds;
        private final AsyncQueryHandler mHandler;
        private final Context mContext;
        private boolean mDeleteLockedMessages;

        public DeleteThreadListener(Collection<Long> threadIds, AsyncQueryHandler handler,
                Context context) {
            mThreadIds = threadIds;
            mHandler = handler;
            mContext = context;
        }

        public void setDeleteLockedMessage(boolean deleteLockedMessages) {
            mDeleteLockedMessages = deleteLockedMessages;
        }

        private void markAsRead(final long threadId){
            Uri threadUri = ContentUris.withAppendedId(Threads.CONTENT_URI, threadId);

            ContentValues readContentValues = new ContentValues(1);
            readContentValues.put("read", 1);
            mContext.getContentResolver().update(threadUri, readContentValues,
                    "read=0", null);
        }

        public void onClick(DialogInterface dialog, final int whichButton) {
            MessageUtils.handleReadReport(mContext, mThreadIds,
                    PduHeaders.READ_STATUS__DELETED_WITHOUT_BEING_READ, new Runnable() {
                public void run() {
                    showProgressDialog();
                    int token = DELETE_CONVERSATION_TOKEN;
                    if (mThreadIds == null) {
                        //wappush: do not need modify the code here, but delete function in provider has been modified.
                        Conversation.startDeleteAll(mHandler, token, mDeleteLockedMessages);
                        DraftCache.getInstance().refresh();
                    } else {
                        //wappush: do not need modify the code here, but delete function in provider has been modified.
                        for (long threadId : mThreadIds) {
                            markAsRead(threadId);
                            Conversation.startDelete(mHandler, token, mDeleteLockedMessages,
                                    threadId);
                            DraftCache.getInstance().setDraftState(threadId, false);
                        }
                    }
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

    private final class ThreadListQueryHandler extends BaseProgressQueryHandler {
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
                    /* After deleting a conversation, The AuroraListView may refresh again.
                     * Because the cursor is not changed before query again, it may
                     * cause that the deleted threads's data is added in the cache again
                     * by ConversationListAdapter::bindView().
                     * We need to remove the not existed conversation in cache*/
                //Conversation.removeInvalidCache(cursor);
                mListAdapter.changeCursor(cursor);
                if (!mDataValid) {
                    invalidateOptionsMenu();
                }
                setTitle(mTitle);
                setProgressBarIndeterminateVisibility(false);  
                if (!Conversation.isInitialized()) {
                    Conversation.init(getApplicationContext());
                }else{
                    Conversation.removeInvalidCache(cursor);
                }
                
                if (mNeedToMarkAsSeen) {
                    mNeedToMarkAsSeen = false;
                    Conversation.markAllConversationsAsSeen(getApplicationContext());

                    // Delete any obsolete threads. Obsolete threads are threads that aren't
                    // referenced by at least one message in the pdu or sms tables. We only call
                    // this on the first query (because of mNeedToMarkAsSeen).
                    mHandler.post(mDeleteObsoleteThreadsRunnable);
                }
                break;

            case UNREAD_THREADS_QUERY_TOKEN:
                int count = cursor.getCount();
                mUnreadConvCount.setText(count > 0 ? Integer.toString(count) : null);
                cursor.close();
                break;

            case HAVE_LOCKED_MESSAGES_TOKEN:
                Collection<Long> threadIds = (Collection<Long>)cookie;
                confirmDeleteThreadDialog(new DeleteThreadListener(threadIds, mQueryHandler,
                        ConversationList.this), threadIds,
                        cursor != null && cursor.getCount() > 0,
                        ConversationList.this);
                cursor.close();
                break;

            default:
                Log.e(TAG, "onQueryComplete called with unknown token " + token);
            }
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            // When this callback is called after deleting, token is 1803(DELETE_OBSOLETE_THREADS_TOKEN)
            // not 1801(DELETE_CONVERSATION_TOKEN)
            CBMessagingNotification.updateNewMessageIndicator(ConversationList.this);
            switch (token) {
            case DELETE_CONVERSATION_TOKEN:
                // Rebuild the contacts cache now that a thread and its associated unique
                // recipients have been deleted.
                Contact.init(ConversationList.this);

                // Make sure the conversation cache reflects the threads in the DB.
                Conversation.init(ConversationList.this);

                try {
                    if(GnPhone.phone != null) {
                        if(GnPhone.isTestIccCard()) {
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
                MessagingNotification.nonBlockingUpdateNewMessageIndicator(ConversationList.this,
                        false, false);
                // Update the notification for failed messages since they
                // may be deleted.
                MessagingNotification.updateSendFailedNotification(ConversationList.this);
                MessagingNotification.updateDownloadFailedNotification(ConversationList.this);

                //Update the notification for new WAP Push messages
                if(FeatureOption.MTK_WAPPUSH_SUPPORT){
                    WapPushMessagingNotification.nonBlockingUpdateNewMessageIndicator(ConversationList.this,false);
                }

                // Make sure the list reflects the delete
                //startAsyncQuery();
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

    private class ModeCallback implements AuroraListView.MultiChoiceModeListener {
        private View mMultiSelectActionBarView;
        private TextView mSelectedConvCount;
        private HashSet<Long> mSelectedThreadIds;
        private HashSet<Integer> mCheckedPosition;
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = getMenuInflater();
            mSelectedThreadIds = new HashSet<Long>();
            mCheckedPosition = new HashSet<Integer>();
            inflater.inflate(R.menu.conversation_multi_select_menu, menu);

            if (mMultiSelectActionBarView == null) {
                mMultiSelectActionBarView = (ViewGroup)LayoutInflater.from(ConversationList.this)
                    .inflate(R.layout.conversation_list_multi_select_actionbar, null);

                mSelectedConvCount =
                    (TextView)mMultiSelectActionBarView.findViewById(R.id.selected_conv_count);
            }
            mode.setCustomView(mMultiSelectActionBarView);
            ((TextView)mMultiSelectActionBarView.findViewById(R.id.title))
                .setText(R.string.select_conversations);
            mDisableSearchFalg = true;
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            if (mMultiSelectActionBarView == null) {
                ViewGroup v = (ViewGroup)LayoutInflater.from(ConversationList.this)
                    .inflate(R.layout.conversation_list_multi_select_actionbar, null);
                mode.setCustomView(v);

                mSelectedConvCount = (TextView)v.findViewById(R.id.selected_conv_count);
            }
            return true;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete:
                    if (mSelectedThreadIds.size() > 0) {
                        confirmDeleteThreads(mSelectedThreadIds, mQueryHandler);
                    }
                    mode.finish();
                    break;

                default:
                    if (mCheckedPosition != null && mCheckedPosition.size() > 0){
                        mCheckedPosition.clear();
                    }
                    break;
            }
            return true;
        }

        public void onDestroyActionMode(ActionMode mode) {
            ConversationListAdapter adapter = (ConversationListAdapter)getListView().getAdapter();
            //adapter.uncheckAll();
            adapter.uncheckSelect(mCheckedPosition);
            mSelectedThreadIds = null;
            mCheckedPosition = null;
            mDisableSearchFalg = false;
        }

        public void onItemCheckedStateChanged(ActionMode mode,
                int position, long id, boolean checked) {
            AuroraListView listView = getListView();
            final int checkedCount = listView.getCheckedItemCount();
            mSelectedConvCount.setText(Integer.toString(checkedCount));

            Cursor cursor  = (Cursor)listView.getItemAtPosition(position);
            Conversation conv = Conversation.from(ConversationList.this, cursor);
            conv.setIsChecked(checked);
            long threadId = conv.getThreadId();

            if (checked) {
                mSelectedThreadIds.add(threadId);
                mCheckedPosition.add(position);
            } else {
                mSelectedThreadIds.remove(threadId);
                mCheckedPosition.remove(position);
            }
        }

    }

    private void log(String format, Object... args) {
        String s = String.format(format, args);
        Log.d(TAG, "[" + Thread.currentThread().getId() + "] " + s);
    }
}
