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

import static com.android.mms.ui.MessageListAdapter.COLUMN_ID;
import static com.android.mms.ui.MessageListAdapter.COLUMN_MMS_LOCKED;
import static com.android.mms.ui.MessageListAdapter.COLUMN_MSG_TYPE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
import com.android.mms.transaction.Transaction;
import com.android.mms.transaction.TransactionBundle;
import com.android.mms.transaction.TransactionService;
import com.android.mms.ui.FolderModeSmsViewer;
import com.android.mms.util.DraftCache;
import com.android.mms.util.Recycler;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.pdu.PduHeaders;
import com.aurora.mms.ui.AuroraConvListActivity;
//Aurora xuyong 2013-11-15 modified for google adapt end
import android.database.sqlite.SqliteWrapper;
import android.provider.Settings;

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
import android.content.res.Resources;
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
import android.provider.Telephony.Sms;
import gionee.provider.GnTelephony.WapPush;
import com.gionee.internal.telephony.GnPhone;
import android.telephony.SmsManager;
import android.util.AttributeSet;
import android.provider.Telephony.Threads;
import android.util.Log;
import android.util.SparseBooleanArray;
//import android.view.ActionMode;
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
import android.widget.AdapterView;
import aurora.widget.AuroraButton;
import android.widget.CheckBox;
import aurora.widget.AuroraListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.SearchView;
import android.widget.Toast;
//import android.widget.SearchView;
import android.widget.TextView;
import android.net.Uri;
import android.provider.Telephony.Threads;
import com.mediatek.wappush.SiExpiredCheck;
import com.aurora.featureoption.FeatureOption;
import java.util.List;
import android.text.TextUtils;
import android.os.SystemProperties; 
import com.android.mms.data.FolderView;
import com.android.mms.util.DownloadManager;

import android.widget.AdapterView.OnItemClickListener;
// Aurora xuyong 2013-09-13 modified for aurora's new feature start
import android.app.ActionBar;
// Aurora xuyong 2013-09-13 modified for aurora's new feature end
import android.widget.ListPopupWindow;
import android.view.LayoutInflater;
//gionee gaoj 2012-4-10 added for CR00555790 start
import com.gionee.mms.ui.TabActivity;
//gionee gaoj 2012-4-10 added for CR00555790 end
/**
 * This activity provides a list view of existing conversations.
 */
public class FolderViewList extends AuroraListActivity implements DraftCache.OnDraftChangedListener {
    private static final String TAG = "FolderViewList";
    private static final String CONV_TAG = "Mms/FolderViewList";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = DEBUG;
    
    public static final int OPTION_INBOX    = 0;
    public static final int OPTION_OUTBOX   = 1;
    public static final int OPTION_DRAFTBOX = 2;
    public static final int OPTION_SENTBOX  = 3;

    public static final int DRAFTFOLDER_LIST_QUERY_TOKEN      = 1009;
    public static final int INBOXFOLDER_LIST_QUERY_TOKEN      = 1111;
    public static final int OUTBOXFOLDER_LIST_QUERY_TOKEN     = 1121;
    public static final int SENTFOLDER_LIST_QUERY_TOKEN       = 1131;
    public static final int FOLDERVIEW_DELETE_TOKEN           = 1001;
    public static final int FOLDERVIEW_HAVE_LOCKED_MESSAGES_TOKEN     = 1002;
    private static final int FOLDERVIEW_DELETE_OBSOLETE_THREADS_TOKEN = 1003;
    
    
    private static final Uri SMS_URI = Uri.parse("content://sms/");
    private static final Uri MMS_URI = Uri.parse("content://mms/");
    private static final Uri WAPPUSH_URI = Uri.parse("content://wappush/");
    private static final Uri CB_URI = Uri.parse("content://cb/messages/");
    // IDs of the context menu items for the list of conversations.
    public static final int MENU_DELETE               = 0;
    public static final int MENU_VIEW                 = 1;
    public static final int MENU_VIEW_CONTACT         = 2;
    public static final int MENU_ADD_TO_CONTACTS      = 3;
    public static final int MENU_SIM_SMS              = 4;
    public static final int MENU_FORWORD              = 5;
    public static final int MENU_REPLY                = 6;
    
    // IDs of the option menu items for the list of conversations.
    public static final int MENU_MULTIDELETE          = 0;
    public static final int MENU_CHANGEVIEW           = 1;
    
    public static final String FOLDERVIEW_KEY         = "floderview_key";    
    private View mFolderSpinner;
    private MenuItem mSearchItem;
    private SearchView mSearchView;
    private ThreadListQueryHandler mQueryHandler;
    private FolderViewListAdapter mListAdapter;
    private Handler mHandler;
    private boolean mNeedToMarkAsSeen;
    private Contact mContact = null;
    //private SearchView mSearchView;
   // private StatusBarManager mStatusBarManager;
    //wappush: indicates the type of thread, this exits already, but has not been used before
    private int mType;
    public static int mgViewID;
    private Context context=null;
    private AccountDropdownPopup mAccountDropdown;
    private TextView SpinnerTextView;
    private TextView mCountTextView;
    
    private SimpleAdapter mAdapter;
    private static final String VIEW_ITEM_KEY_BOXNAME   = "spinner_line_2";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.folderview_list_screen);
        mQueryHandler = new ThreadListQueryHandler(getContentResolver());
        
        AuroraListView listView = getListView();
        //listView.setOnCreateContextMenuListener(mConvListOnCreateContextMenuListener);
        listView.setOnKeyListener(mThreadListKeyListener);
       
        View emptyView = findViewById(R.id.empty);
        listView.setEmptyView(emptyView);
        
        context = FolderViewList.this;
        initListAdapter();
        mHandler = new Handler();

        initSpinnerListAdapter();
        setTitle("");
        mgViewID = getIntent().getIntExtra(FOLDERVIEW_KEY, 0);
        Log.d(TAG, "onCreate, mgViewID:" + mgViewID);
        setBoxTitle(mgViewID);
    }
    
    
    private void initSpinnerListAdapter() {
        
        mAdapter = new SimpleAdapter(this, getData(),
              R.layout.folder_mode_item,
              new String[] {"spinner_line_2"},
              new int[] {R.id.spinner_line_2});     
        setupActionBar();
        
        mAccountDropdown = new AccountDropdownPopup(context);
        mAccountDropdown.setAdapter(mAdapter);
  
   }
    
    private void setupActionBar() {
        // Aurora xuyong 2013-09-13 modified for aurora's new feature start
        ActionBar actionBar = getActionBar();
        // Aurora xuyong 2013-09-13 modified for aurora's new feature end
        ViewGroup v = (ViewGroup)LayoutInflater.from(this).inflate(R.layout.folder_mode_actionbar, null);
        // Aurora xuyong 2013-09-13 modified for aurora's new feature start
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(v,
                new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                        ActionBar.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER_VERTICAL | Gravity.LEFT));
        // Aurora xuyong 2013-09-13 modified for aurora's new feature end
        mCountTextView = (TextView)v.findViewById(R.id.message_count);

        mFolderSpinner = (View)v.findViewById(R.id.account_spinner);
        SpinnerTextView = (TextView)v.findViewById(R.id.boxname);;
        SpinnerTextView.setText(R.string.inbox);
        
        mFolderSpinner.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (mAdapter.getCount() > 0) {
                    mAccountDropdown.show();
                    Log.d("this", "mAdapter.getCount()="+mAdapter.getCount());
                }
            }
        });
    }
    
    // Based on Spinner.DropdownPopup
    private class AccountDropdownPopup extends ListPopupWindow {
        public AccountDropdownPopup(Context context) {
            super(context);
            setAnchorView(mFolderSpinner);
            setModal(true);
            setPromptPosition(POSITION_PROMPT_ABOVE);
            setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    onAccountSpinnerItemClicked(position);
                    dismiss();
                }
            });
        }

        @Override
        public void show() {
            setWidth(200);
            setInputMethodMode(ListPopupWindow.INPUT_METHOD_NOT_NEEDED);
            super.show();
            // List view is instantiated in super.show(), so we need to do this after...
            getListView().setChoiceMode(AuroraListView.CHOICE_MODE_SINGLE);
        }
    }
    
    private void onAccountSpinnerItemClicked(int position) {

         switch (position) {
            case OPTION_INBOX:
                mgViewID = OPTION_INBOX;
                SpinnerTextView.setText(R.string.inbox);
                startAsyncQuery();
                break;
            case OPTION_OUTBOX:
                mgViewID =OPTION_OUTBOX;
                SpinnerTextView.setText(R.string.outbox);
                startAsyncQuery();
                break;
            case OPTION_DRAFTBOX:
                mgViewID = OPTION_DRAFTBOX;
                SpinnerTextView.setText(R.string.draftbox);
                startAsyncQuery();
                break;
            case OPTION_SENTBOX:
                mgViewID = OPTION_SENTBOX;
                SpinnerTextView.setText(R.string.sentbox);
                startAsyncQuery();
                break;
            default:
                break;
        }
    }
    
    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        Resources res = getResources();
        map.put(VIEW_ITEM_KEY_BOXNAME, res.getText(R.string.inbox));
        list.add(map);
        
        map = new HashMap<String, Object>();
        map.put(VIEW_ITEM_KEY_BOXNAME, res.getText(R.string.outbox));
        list.add(map);
        
        map = new HashMap<String, Object>();
        map.put(VIEW_ITEM_KEY_BOXNAME, res.getText(R.string.draftbox));
        list.add(map);
        
        map = new HashMap<String, Object>();
        map.put(VIEW_ITEM_KEY_BOXNAME, res.getText(R.string.sentbox));
        list.add(map);

//        map = new HashMap<String, Object>();
//        map.put(VIEW_ITEM_KEY_BOXNAME, res.getText(R.string.simbox));
//        list.add(map);
        
        return list;
    }
    

    private final FolderViewListAdapter.OnContentChangedListener mContentChangedListener =
        new FolderViewListAdapter.OnContentChangedListener() {
        public void onContentChanged(FolderViewListAdapter adapter) {
            startAsyncQuery(200);
        }
    };

    private void initListAdapter() {
        mListAdapter = new FolderViewListAdapter(this, null);
        mListAdapter.setOnContentChangedListener(mContentChangedListener);
        setListAdapter(mListAdapter);
        getListView().setRecyclerListener(mListAdapter);
    }

    private void startmDeleteActivity(int viewid){
        Intent intent = new Intent(this,FolderViewMultiDeleteActivity.class);
        intent.putExtra(FolderViewList.FOLDERVIEW_KEY, viewid);
        startActivity(intent);    
    }
   
    @Override
    protected void onNewIntent(Intent intent) {
        // Handle intents that occur after the activity has already been created.
        setIntent(intent);
        mgViewID = intent.getIntExtra(FOLDERVIEW_KEY, 0);
        Log.d(TAG, "onNewIntent, mgViewID:" + mgViewID);
        setBoxTitle(mgViewID);
        if (mgViewID == OPTION_OUTBOX) {
            FolderView.markFailedSmsMmsSeen(this);//mark as seen
        }
        startAsyncQuery();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //ComposeMessageActivity.mDestroy = true;
        MessagingNotification.nonBlockingUpdateNewMessageIndicator(FolderViewList.this, false, false);
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            WapPushMessagingNotification.nonBlockingUpdateNewMessageIndicator(FolderViewList.this,false);
        }
        CBMessagingNotification.updateAllNotifications(FolderViewList.this);
    }

    private void setBoxTitle(int id) {
        switch (id) {
            case OPTION_INBOX:
                SpinnerTextView.setText(R.string.inbox);
                break;
            case OPTION_OUTBOX:
                SpinnerTextView.setText(R.string.outbox);
                break;
            case OPTION_DRAFTBOX:
                SpinnerTextView.setText(R.string.draftbox);
                break;
            case OPTION_SENTBOX:
                SpinnerTextView.setText(R.string.sentbox);
                break;
            default:
                Log.d(TAG, "mgViewID = " + id);
                break;
        }
    }
    
    @Override
    protected void onPause() {
        //mStatusBarManager.hideSIMIndicator(getComponentName());
        super.onPause();
    }
    @Override
    protected void onStart() {
        super.onStart();
        MmsConfig.setMmsDirMode(true);
        DraftCache.getInstance().addOnDraftChangedListener(this);
        mNeedToMarkAsSeen = true;
        startAsyncQuery();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Contact.invalidateCache();
        DraftCache.getInstance().removeOnDraftChangedListener(this);
        //mListAdapter.changeCursor(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                   finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void startAsyncQuery() {
        try {
//            setTitle(getString(R.string.refreshing));
//            setProgressBarIndeterminateVisibility(true);

            switch (mgViewID) {
                case OPTION_INBOX:
                    FolderView.startQueryForInboxView(mQueryHandler, INBOXFOLDER_LIST_QUERY_TOKEN);
                    MessagingNotification.cancelNotification(this, MessagingNotification.DOWNLOAD_FAILED_NOTIFICATION_ID);
                    break;
                case OPTION_OUTBOX:
                    FolderView.startQueryForOutBoxView(mQueryHandler, OUTBOXFOLDER_LIST_QUERY_TOKEN);
                    MessagingNotification.cancelNotification(this, MessagingNotification.MESSAGE_FAILED_NOTIFICATION_ID);
                    break;
                case OPTION_DRAFTBOX:
                    FolderView.startQueryForDraftboxView(mQueryHandler, DRAFTFOLDER_LIST_QUERY_TOKEN);
                    break;
                case OPTION_SENTBOX:
                    FolderView.startQueryForSentboxView(mQueryHandler, SENTFOLDER_LIST_QUERY_TOKEN);
                    break;
                default:
                    break;
            }
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(this, e);
        }
    }

    private void startAsyncQuery(int PostTime) {
        try {
//            setTitle(getString(R.string.refreshing));
//            setProgressBarIndeterminateVisibility(true);
            switch (mgViewID) {
                case OPTION_INBOX:
                    FolderView.startQueryForInboxView(mQueryHandler, INBOXFOLDER_LIST_QUERY_TOKEN,PostTime);
                    MessagingNotification.cancelNotification(this, MessagingNotification.DOWNLOAD_FAILED_NOTIFICATION_ID);
                    break;
                case OPTION_OUTBOX:
                    FolderView.startQueryForOutBoxView(mQueryHandler, OUTBOXFOLDER_LIST_QUERY_TOKEN);
                    MessagingNotification.cancelNotification(this, MessagingNotification.MESSAGE_FAILED_NOTIFICATION_ID);
                    break;
                case OPTION_DRAFTBOX:
                    FolderView.startQueryForDraftboxView(mQueryHandler, DRAFTFOLDER_LIST_QUERY_TOKEN);
                    break;
                case OPTION_SENTBOX:
                    FolderView.startQueryForSentboxView(mQueryHandler, SENTFOLDER_LIST_QUERY_TOKEN);
                    break;
                default:
                    break;
            }
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(this, e);
        }
    }

    @Override
    protected void onListItemClick(AuroraListView l, View v, int position, long id) {
        Cursor cursor  = (Cursor) getListView().getItemAtPosition(position);
        if (cursor == null) {
            Log.d(TAG, "cursor == null");
             return;
        }
        int type = cursor.getInt(6);
        int messageid   = cursor.getInt(0);
        String recipients = cursor.getString(2);
        Log.d(TAG, "messageid =" + messageid);
        if(mgViewID == OPTION_DRAFTBOX){//in draftbox
            long threadId = cursor.getLong(1);
            Intent it = ComposeMessageActivity.createIntent(this, threadId);
            it.putExtra("folderbox", mgViewID);
            it.putExtra("hiderecipient", true);
            it.putExtra("showinput", true);
            startActivity(it);
        } else if(type == 1){//sms
            Intent intent = new Intent();
            intent.setClass(context, FolderModeSmsViewer.class);
            intent.setData(ContentUris.withAppendedId(SMS_URI, messageid));
            intent.putExtra("msg_type", 1);
            intent.putExtra("folderbox", mgViewID);
            startActivity(intent);
        } else if (type == 3){//wappush
           //messageid = cursor.getInt(1);
            Intent intent = new Intent();
            intent.setClass(context, FolderModeSmsViewer.class);
            intent.setData(ContentUris.withAppendedId(WAPPUSH_URI, messageid));
            intent.putExtra("msg_type", 3);
            intent.putExtra("folderbox", mgViewID);
            startActivity(intent);
        } else if (type == 4){//cb
          //  messageid = cursor.getInt(1);
            Intent intent = new Intent();
            intent.setClass(context, FolderModeSmsViewer.class);
            intent.setData(ContentUris.withAppendedId(CB_URI, messageid));
            intent.putExtra("msg_type", 4);
            intent.putExtra("folderbox", mgViewID);
            startActivity(intent);
        }else if(type == 2){//mms
            Log.d(TAG,"TYPE1 = "+cursor.getInt(9));      
            if(mgViewID == OPTION_INBOX && 
                    cursor.getInt(9)== PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND){
                DownloadManager Dmanager = DownloadManager.getInstance();
                int loadstate = Dmanager.getState(ContentUris.withAppendedId(MMS_URI, messageid));
                if(loadstate != DownloadManager.STATE_DOWNLOADING){         
                    confirmDownloadDialog(new DownloadMessageListener(
                        ContentUris.withAppendedId(MMS_URI, messageid),cursor.getInt(10),messageid));
                }
                else{
                    Toast.makeText(context, R.string.folder_download, Toast.LENGTH_SHORT).show();
                }
            }else{
                Intent intent = new Intent();
                intent.setClass(context, MmsPlayerActivity.class);
                intent.setData(ContentUris.withAppendedId(MMS_URI, messageid));
                intent.putExtra("dirmode", true);
                intent.putExtra("folderbox", mgViewID);
                startActivity(intent);   
            }
            
        }
        
    }

    private class DownloadMessageListener implements OnClickListener {
        private final Uri mDownloadUri;
        private final int sim_id;
        private final int messageid;
        public DownloadMessageListener(Uri DownloadUri,int simid,int msgid) {
            mDownloadUri = DownloadUri;
            Log.d(TAG,"mDownloadUri ="+mDownloadUri);
            sim_id       = simid;
            Log.d(TAG,"sim_id ="+sim_id);
            messageid    = msgid;
            Log.d(TAG,"messageid ="+messageid);
        }

        public void onClick(DialogInterface dialog, int whichButton) {
            markMmsIndReaded(ContentUris.withAppendedId(MMS_URI, messageid));
            MessagingNotification.nonBlockingUpdateNewMessageIndicator(FolderViewList.this, false, false);
            DownloadManager Dmanager = DownloadManager.getInstance();
            Dmanager.setState(ContentUris.withAppendedId(MMS_URI, messageid),DownloadManager.STATE_DOWNLOADING);
            Intent intent = new Intent(context, TransactionService.class);
            intent.putExtra(TransactionBundle.URI, mDownloadUri.toString());
            intent.putExtra(TransactionBundle.TRANSACTION_TYPE,
                    Transaction.RETRIEVE_TRANSACTION);
            // add for gemini
            intent.putExtra(GnPhone.GEMINI_SIM_ID_KEY, sim_id);
            context.startService(intent);
        }    
    }


    private final OnCreateContextMenuListener mConvListOnCreateContextMenuListener =
        new OnCreateContextMenuListener() {
        public void onCreateContextMenu(ContextMenu menu, View v,
                ContextMenuInfo menuInfo) {
            Cursor cursor = mListAdapter.getCursor();
            if (cursor == null || cursor.getPosition() < 0) {
                return;
            }

//            if(mgViewID == OPTION_INBOX){
//                menu.add(0, MENU_REPLY, 0, R.string.menu_reply);
//            }
//
            int type = cursor.getInt(6);
            int boxtype = cursor.getInt(11);
            String recipientIds = cursor.getString(2);
            ContactList recipients;
            if(type == 2 || (type == 1 && boxtype ==3) || type == 4){
                recipients = ContactList.getByIds(recipientIds, false);
            }else{
                recipients = ContactList.getByNumbers(recipientIds, false, true);
            }          
            menu.setHeaderTitle(recipients.formatNames(","));
//
//            if (recipients.size() == 1) {
//                // do we have this recipient in contacts?
//                if (recipients.get(0).existsInDatabase()) {
//                    menu.add(0, MENU_VIEW_CONTACT, 0, R.string.menu_view_contact);
//                } else {
//                    menu.add(0, MENU_ADD_TO_CONTACTS, 0, R.string.menu_add_to_contacts);
//                }
//            }
            
            menu.add(0, MENU_DELETE, 0, R.string.menu_delete_messages);
            
            //menu.add(0, MENU_FORWORD, 0, R.string.menu_forward);
            
        }
    };

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Cursor cursor = mListAdapter.getCursor();
        if (cursor != null && cursor.getPosition() >= 0) {
////            Conversation conv = Conversation.from(FolderViewList.this, cursor);
////            long threadId = conv.getThreadId();
            switch (item.getItemId()) {
              case MENU_DELETE: {
                  int type = cursor.getInt(6);
                  Log.d(TAG, "type ="+type);
                  int messageid   = cursor.getInt(0);
                  long mThreadId   = cursor.getLong(1);
                  confirmDeleteDialog(new DeleteMessageListener(
                      messageid,type,mQueryHandler,mThreadId));
                  break;
              }
              case MENU_VIEW_CONTACT: {
                  if (mContact == null){
                      break;
                  }
                  Contact contact = mContact;
                  Intent intent = new Intent(Intent.ACTION_VIEW, contact.getUri());
                  intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                  startActivity(intent);
                  break;
              }
              case MENU_FORWORD: {
                  String body = cursor.getString(3);
                  forwardMessage(body);
                  break;
              }
              case MENU_ADD_TO_CONTACTS: {
                  if (mContact == null){
                      break;
                  }
                  String address = mContact.getNumber();
                  onAddContactButtonClickInt(address);
                  break;
              }
              case MENU_REPLY: {
                  String address = mContact.getNumber();
                  MessageUtils.replyMessage(this, address);
                  break;
              }
              default:
                  break;
            }
        }
        return super.onContextItemSelected(item);
    }

    public void onAddContactButtonClickInt(final String number) {
        if(!TextUtils.isEmpty(number)) {
            String message = this.getResources().getString(R.string.add_contact_dialog_message, number);
            AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this)
                                                         .setTitle(number)
                                                         .setMessage(message);
            
            AuroraAlertDialog dialog = builder.create();
            
            dialog.setButton(AuroraAlertDialog.BUTTON_POSITIVE, this.getResources().getString(R.string.add_contact_dialog_existing), new DialogInterface.OnClickListener() {
                
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                    intent.setType(Contacts.CONTENT_ITEM_TYPE);
                    intent.putExtra(ContactsContract.Intents.Insert.PHONE, number);
                        startActivity(intent);
                }
            });

            dialog.setButton(AuroraAlertDialog.BUTTON_NEGATIVE, this.getResources().getString(R.string.add_contact_dialog_new), new DialogInterface.OnClickListener() {
                
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    final Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
                    intent.putExtra(ContactsContract.Intents.Insert.PHONE, number);
                        startActivity(intent);
                }
                
            });
            dialog.show();
        }
    }
    private void forwardMessage(String body) {
        Intent intent = new Intent();
        intent.setClassName(this, "com.android.mms.ui.ForwardMessageActivity");
        intent.putExtra("forwarded_message", true);
        if (body != null) {
            intent.putExtra("sms_body", body);
        }
        startActivity(intent);
    } 
    
    private class DeleteMessageListener implements OnClickListener {
        private  Uri mDeleteUri = null;
        private final AsyncQueryHandler mHandler;
        private  long threadid = 0l;
        public DeleteMessageListener(long msgId, int type,AsyncQueryHandler handler,long mthreadid) {
            mHandler = handler;
            threadid = mthreadid;
            if (type == 2) {//mms
                mDeleteUri = ContentUris.withAppendedId(Mms.CONTENT_URI, msgId);
            } 
            else if(type == 1){//sms
                mDeleteUri = ContentUris.withAppendedId(Sms.CONTENT_URI, msgId);
            }
            else if(type == 3){//wp
                mDeleteUri = ContentUris.withAppendedId(WAPPUSH_URI, msgId);
            }
            else if(type == 4){//cb
                mDeleteUri = ContentUris.withAppendedId(CB_URI, msgId);
            }
        }

        public void onClick(DialogInterface dialog, int whichButton) {
            mHandler.startDelete(FOLDERVIEW_DELETE_TOKEN,
                    null, mDeleteUri, null, null);
            DraftCache.getInstance().updateDraftStateInCache(threadid);
            dialog.dismiss();
        }
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

    private void confirmDeleteDialog(OnClickListener listener) {
        AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_dialog_title);
        builder.setIconAttribute(android.R.attr.alertDialogIcon);
        builder.setCancelable(true);
        builder.setMessage(R.string.confirm_delete_message);
        builder.setPositiveButton(R.string.delete, listener);
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }

    private void confirmDownloadDialog(OnClickListener listener) {
        AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this);
        builder.setTitle(R.string.download);
        builder.setIconAttribute(android.R.attr.alertDialogIcon);
        builder.setCancelable(true);
        builder.setMessage(R.string.confirm_download_message);
        builder.setPositiveButton(R.string.download, listener);
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }


    private final OnKeyListener mThreadListKeyListener = new OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DEL: {
                        long id = getListView().getSelectedItemId();
                        if (id > 0) {
                            //confirmDeleteThread(id, mQueryHandler);
                        }
                        return true;
                    }
                }
            }
            return false;
        }
    };


    
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
                LogTag.debug("mDeleteObsoleteThreadsRunnable getSavingDraft(): "
                        + DraftCache.getInstance().getSavingDraft());
            }
            if (DraftCache.getInstance().getSavingDraft()) {
                // We're still saving a draft. Try again in a second. We don't
                // want to delete
                // any threads out from under the draft.
                mHandler.postDelayed(mDeleteObsoleteThreadsRunnable, 1000);
            } else {
                MessageUtils.asyncDeleteOldMms();
                Conversation.asyncDeleteObsoleteThreads(mQueryHandler,
                        FOLDERVIEW_DELETE_OBSOLETE_THREADS_TOKEN);
            }
        }
    };

    private final class ThreadListQueryHandler extends BaseProgressQueryHandler {
        public ThreadListQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if(cursor == null || cursor.getCount() == 0){
                Log.d(TAG,"cursor == null||count==0.");
                mCountTextView.setVisibility(View.INVISIBLE);
                if (cursor != null) {
                    mListAdapter.changeCursor(cursor);
                }
                return;
            }
            //in this case the adpter should be notifychanged.
            if (mSearchView != null){
                String searchString = mSearchView.getQuery().toString();
                if (searchString != null && searchString.length() > 0){
                    Log.d(TAG, "onQueryComplete mSearchView != null");
                    mSearchView.getSuggestionsAdapter().notifyDataSetChanged();
                }
            }
            mCountTextView.setVisibility(View.VISIBLE);
            switch (token) {
            case DRAFTFOLDER_LIST_QUERY_TOKEN:
                mCountTextView.setText(""+cursor.getCount());
                Log.d(TAG,"onQueryComplete DRAFTFOLDER_LIST_QUERY_TOKEN");
                mListAdapter.changeCursor(cursor);

                if (mNeedToMarkAsSeen) {
                    mNeedToMarkAsSeen = false;
                    // Delete any obsolete threads. Obsolete threads are threads that aren't
                    // referenced by at least one message in the pdu or sms tables. We only call
                    // this on the first query (because of mNeedToMarkAsSeen).
                    mHandler.post(mDeleteObsoleteThreadsRunnable);
                }
                break;
            case INBOXFOLDER_LIST_QUERY_TOKEN:
                int count = 0;
                while (cursor.moveToNext()) {
                    if (cursor.getInt(5) == 0) {
                        count++;
                    }
                }
                mCountTextView.setText(""+count+"/"+cursor.getCount());
                Log.d(TAG,"onQueryComplete INBOXFOLDER_LIST_QUERY_TOKEN");
                mListAdapter.changeCursor(cursor);
                break;
            case OUTBOXFOLDER_LIST_QUERY_TOKEN:
                mCountTextView.setText(""+cursor.getCount());
                Log.d(TAG,"onQueryComplete OUTBOXFOLDER_LIST_QUERY_TOKEN");
                mListAdapter.changeCursor(cursor);
                break;
            case SENTFOLDER_LIST_QUERY_TOKEN:
                mCountTextView.setText(""+cursor.getCount());
                Log.d(TAG,"onQueryComplete SENTFOLDER_LIST_QUERY_TOKEN");
                mListAdapter.changeCursor(cursor);
                break;
            case FOLDERVIEW_HAVE_LOCKED_MESSAGES_TOKEN:
                Collection<Long> threadIds = (Collection<Long>)cookie;
//                confirmDeleteThreadDialog(new DeleteThreadListener(threadIds, mQueryHandler,
//                    FolderViewList.this), threadIds,
//                        cursor != null && cursor.getCount() > 0,
//                        FolderViewList.this);
                break;

            default:
                Log.e(TAG, "onQueryComplete called with unknown token " + token);
            }
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            // When this callback is called after deleting, token is 1803(DELETE_OBSOLETE_THREADS_TOKEN)
            // not 1801(DELETE_CONVERSATION_TOKEN)
            switch (token) {
            case FOLDERVIEW_DELETE_TOKEN:

                // Update the notification for new messages since they
                // may be deleted.
                MessagingNotification.nonBlockingUpdateNewMessageIndicator(FolderViewList.this,
                        false, false);
                // Update the notification for failed messages since they
                // may be deleted.
                //MessagingNotification.updateSendFailedNotification(FolderViewList.this);
                //MessagingNotification.updateDownloadFailedNotification(FolderViewList.this);

                //Update the notification for new WAP Push messages
                if(FeatureOption.MTK_WAPPUSH_SUPPORT){
                    WapPushMessagingNotification.nonBlockingUpdateNewMessageIndicator(FolderViewList.this,false);
                }
                CBMessagingNotification.updateAllNotifications(FolderViewList.this);
                // Make sure the list reflects the delete
                //startAsyncQuery();
                if (progress()) {
                    dismissProgressDialog();
                }
                break;

            case FOLDERVIEW_DELETE_OBSOLETE_THREADS_TOKEN:
                // Nothing to do here.
                break;
            }
        }
    }

    private void markMmsIndReaded(final Uri uri) {
        new Thread(new Runnable() {
            public void run() {
                final ContentValues values = new ContentValues(2);
                values.put("read", 1);
                values.put("seen", 1);
                SqliteWrapper.update(getApplicationContext(), getContentResolver(), uri, values, null, null);
            }
        }).start();
        MessagingNotification.nonBlockingUpdateNewMessageIndicator(this, false, false);
    }
    
    @Override
    public void onDraftChanged(long threadId, boolean hasDraft) {
        // TODO Auto-generated method stub
        Log.d(TAG,"Override onDraftChanged");
        if(mgViewID == OPTION_DRAFTBOX){
            startAsyncQuery();       
        }       
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (mListAdapter.getCount() > 0) {
            menu.add(0, MENU_MULTIDELETE, 0, R.string.menu_delete_messages);
        }
        getMenuInflater().inflate(R.menu.conversation_list_menu, menu);
        menu.removeItem(R.id.action_delete_all);
        menu.removeItem(R.id.action_debug_dump);
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
        menu.add(0, MENU_CHANGEVIEW, 0, R.string.changeview);
        
        /*menu.add(0, MENU_SIM_SMS, 0, R.string.menu_sim_sms).setIcon(
            R.drawable.ic_menu_sim_sms);*/
        MenuItem item = menu.findItem(MENU_SIM_SMS);
        List<SIMInfo> listSimInfo = SIMInfo.getInsertedSIMList(this);
        if(listSimInfo == null || listSimInfo.isEmpty()){
            item.setEnabled(false);
            Log.d(TAG, "onPrepareOptionsMenu MenuItem setEnabled(false)");
        }

        // omacp menu
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
        super.onPrepareOptionsMenu(menu);
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case MENU_CHANGEVIEW:
                MmsConfig.setMmsDirMode(false);
                MessageUtils.updateNotification(this);
                //gionee gaoj 2012-3-22 added for CR00555790 start
                if (MmsApp.mGnMessageSupport) {
                    startActivity(new Intent(this, TabActivity.class));
                } else {
                //gionee gaoj 2012-3-22 added for CR00555790 end
                    // Aurora liugj 2013-12-13 modified for startActivity start
                startActivity(new Intent(this, AuroraConvListActivity.class));
                    // Aurora liugj 2013-12-13 modified for startActivity end
                //gionee gaoj 2012-3-22 added for CR00555790 start
                }
                //gionee gaoj 2012-3-22 added for CR00555790 end
                finish();
                break;
            case R.id.action_compose_new:
                {
                    Intent intent = new Intent(context, ComposeMessageActivity.class);
                    intent.putExtra("folderbox", mgViewID);
                    startActivity(intent);
                }
                break;
            case R.id.action_settings:
                {
                    Intent intent = new Intent(this, MessagingPreferenceActivity.class);
                    startActivityIfNeeded(intent, -1);
                }
                break;
            case R.id.action_omacp:
                Intent omacpintent = new Intent();
                omacpintent.setClassName("com.mediatek.omacp", "com.mediatek.omacp.message.OmacpMessageList");
                omacpintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityIfNeeded(omacpintent, -1);
                break;
            case MENU_MULTIDELETE: 
                switch (mgViewID) {
                    case OPTION_INBOX:
                        startmDeleteActivity(OPTION_INBOX);
                        break;
                    case OPTION_OUTBOX:
                        startmDeleteActivity(OPTION_OUTBOX);
                        break;
                    case OPTION_DRAFTBOX:
                        startmDeleteActivity(OPTION_DRAFTBOX);
                        break;
                    case OPTION_SENTBOX:
                        startmDeleteActivity(OPTION_SENTBOX);
                        break;
                    default:
                        break;
                }
                break;
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
            default:
                return true;
        }
        return true;
    }

    SearchView.OnQueryTextListener mQueryTextListener = new SearchView.OnQueryTextListener() {
          // Aurora liugj 2013-10-25 modified for fix bug-241 start 
        public boolean onQueryTextSubmit(String query) {
            /*Intent intent = new Intent();
            intent.setClass(FolderViewList.this, SearchActivity.class);
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
    public boolean onSearchRequested() {
        mSearchItem.expandActionView();
        return true;
    }
}
