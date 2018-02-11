package com.android.mms.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.transaction.CBMessagingNotification;
import com.android.mms.transaction.MessageSender;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.transaction.SmsSingleRecipientSender;
import com.android.mms.transaction.WapPushMessagingNotification;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.MessageUtils;
//import com.android.mms.ui.MultiRecipientsActivity;
import com.android.mms.util.Recycler;
import com.android.mms.util.SmileyParser;
import com.android.mms.util.SendingProgressTokenManager;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.MmsException;
//Aurora xuyong 2013-11-15 modified for google adapt end

import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
// Aurora xuyong 2013-09-13 modified for aurora's new feature start
import android.app.ActionBar;
// Aurora xuyong 2013-09-13 modified for aurora's new feature end
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.BaseColumns;
import android.provider.Browser;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import com.android.mms.util.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.text.ClipboardManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import aurora.widget.AuroraListView;
import android.widget.TextView;
import android.widget.Toast;
import aurora.widget.AuroraButton;
import aurora.widget.AuroraEditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.database.sqlite.SqliteWrapper;
import android.graphics.drawable.Drawable;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.Threads;
import android.provider.Telephony;
import com.gionee.internal.telephony.GnPhone;
import gionee.telephony.GnSmsManager;
import gionee.provider.GnTelephony;
import gionee.provider.GnTelephony.SIMInfo;
import com.gionee.internal.telephony.GnTelephonyManagerEx;
import gionee.telephony.gemini.GnGeminiSmsManager;
import com.aurora.featureoption.FeatureOption;
import com.android.internal.telephony.ITelephony;
import aurora.preference.AuroraPreferenceManager;

//gionee wangym 2012-11-22 add for CR00735223 start
import com.android.mms.ui.ScaleDetector.OnScaleListener;
import android.view.MotionEvent;
//gionee wangym 2012-11-22 add for CR00735223 end

public class FolderModeSmsViewer extends AuroraActivity implements Contact.UpdateListener {
    private static final String TAG = "Mms/FolderModeSmsViewer";
    private Uri searchUri;
    //private TextView mTitle;
    private TextView date;
    private TextView recipent;
    private TextView textContent;
    private TextView mByCard;
    private int mSimId;
    private String reciBody;// message text content
    private String reciNumber;// message number, this may be the name
    private String mNumber;//use this save the number
    private String reciDate;// message reciDate
    private Long reciDateLong;
    private ContactList mContactList;
    private int threadId;
    private int mMsgBox;//which box the msg in
    private int status;
    private int msgType;//sms wappush+
    private int locked;
    private long msgid;
    private String mServiceCenter;
    
    // This must match the column IDs below.
    private static final String[] SMS_PROJECTION = new String[] {
        "address",     //0
        "date",        //1
        "body",        //2
        "type",        //3
        "thread_id",   //4
        "status",      //5
        "locked",      //6
        "_id",          //7
        "service_center", //8
        GnTelephony.GN_SIM_ID//"sim_id"    //9
    };
    private static final String[] WAPPUSH_PROJECTION = new String[] {
        "address",     //0
        "date",        //1
        "text",        //2
        "type",        //3
        "thread_id",   //4
        "error",       //5
        "url",         //6
        "_id",          //7
        "service_center", //8
        GnTelephony.GN_SIM_ID//"sim_id"        //9
    };
    private static final String[] CB_PROJECTION = new String[] {
        "channel_id",        //0
        "date",         //1
        "body",        //2
        "seen",        //3
        "thread_id",   //4
        "locked",       //5
        "read",       //6
        "_id",       //7
        GnTelephony.GN_SIM_ID//"sim_id"     //8
    };
    private static final Uri SMS_URI = Uri.parse("content://sms/");
    private static final Uri WAPPUSH_URI = Uri.parse("content://wappush/");
    private static final Uri CB_URI = Uri.parse("content://cb/messages/");
    //menu
    private static final int MENU_REPLY            = Menu.FIRST + 0;
    private static final int MENU_FORWORD          = Menu.FIRST + 1;
    private static final int MENU_RESEND           = Menu.FIRST + 2;
    private static final int MENU_DELETE           = Menu.FIRST + 3;
    private static final int MENU_ADD_CONTACT      = Menu.FIRST + 4;
    private static final int MENU_VIEW_REPORT      = Menu.FIRST + 5;
    private static final int MENU_CALL_RECIPIENT   = Menu.FIRST + 6;
    private static final int MENU_CALL_RECIPIENT_BY_VT  = Menu.FIRST + 7;
    // Context menu ID
    private static final int MENU_VIEW_MESSAGE_DETAILS      = 17;
    private static final int MENU_DELETE_MESSAGE            = 18;
    private static final int MENU_CALL_BACK                 = 22;
    private static final int MENU_SEND_EMAIL                = 23;
    private static final int MENU_COPY_MESSAGE_TEXT         = 24;
    private static final int MENU_ADD_ADDRESS_TO_CONTACTS   = 27;
    private static final int MENU_LOCK_MESSAGE              = 28;
    private static final int MENU_UNLOCK_MESSAGE            = 29;
    private static final int MENU_SAVE_MESSAGE_TO_SIM       = 31;
    private static final int MENU_SEND_SMS                  = 33;
    private static final int MENU_SELECT_TEXT               = 34;
    
    //extract telephony number ...
    private ArrayList<String> mURLs = new ArrayList<String>();
    private static final int MENU_ADD_TO_BOOKMARK       = 35;
    //extract telephony number end 
    
    // for save message to sim card
    private Handler mSaveMsgHandler = null;
    private Thread mSaveMsgThread = null;
    private static final int SIM_SELECT_FOR_SEND_MSG                    = 1;
    private static final int SIM_SELECT_FOR_SAVE_MSG_TO_SIM             = 2;
    private static final int MSG_QUIT_SAVE_MESSAGE_THREAD               = 100;
    private static final int MSG_SAVE_MESSAGE_TO_SIM                    = 102;
    private static final int MSG_SAVE_MESSAGE_TO_SIM_AFTER_SELECT_SIM   = 104;
    private static final int MSG_SAVE_MESSAGE_TO_SIM_SUCCEED            = 106;
    private static final int MSG_SAVE_MESSAGE_TO_SIM_FAILED_GENERIC     = 108;
    private static final int MSG_SAVE_MESSAGE_TO_SIM_FAILED_SIM_FULL    = 110;
    private List<SIMInfo> mSimInfoList;
    private int mSimCount = 0;
    //add for gemini
    private int mSelectedSimId;
    private static final String SELECT_TYPE                             = "Select_type";
    private AuroraAlertDialog mSIMSelectDialog;

    private ImageButton mZoomIn;
    private ImageButton mZoomOut;
    //private View mZoomControls;
    private final int DEFAULT_TEXT_SIZE = 18;
    private final int TEXT_SIZE_CHANGE = 2;
    private final int MIN_TEXT_SIZE = 10;
    private final int MAX_TEXT_SIZE = 30;
    private int currentSize = DEFAULT_TEXT_SIZE;
    private int mHomeBox = 0;

    //gionee wangym 2012-11-22 add for CR00735223 start
    private final float MIN_ADJUST_TEXT_SIZE = 0.2f;
    private ScaleDetector mScaleDetector;
    private float mTextSize;
    //gionee wangym 2012-11-22 add for CR00735223 end

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.d(TAG,"onCreate");
        //define the activity title
        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.foldermode_sms_viewer);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
       // getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.foldermode_smsviewer_title);
        recipent = (TextView) findViewById(R.id.msg_recipent);
        //mTitle = (TextView) findViewById(R.id.sms_viewer_title);
        date = (TextView) findViewById(R.id.msg_date);
        textContent = (TextView) findViewById(R.id.msg_text);
        textContent.setOnCreateContextMenuListener(mContextMenuCreateListener);
        textContent.setOnClickListener(mClickListener);
        mByCard = (TextView)findViewById(R.id.by_card);
        Intent intent = getIntent();
        searchUri = intent.getData();
        msgType = intent.getIntExtra("msg_type", 1);
        mHomeBox = intent.getIntExtra("folderbox", 0);
        Log.d(TAG, "the sms intent uri is " + searchUri.getPath());
        //add for zoom
        mZoomIn = (ImageButton) findViewById(R.id.zoomIn);
        mZoomOut = (ImageButton) findViewById(R.id.zoomOut);
        //mZoomControls = findViewById(R.id.zoomControls);
        mZoomIn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0) {
                changeTextZoom(true);
            }
        });
        mZoomOut.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0) {
                changeTextZoom(false);
            }
        });
        //gionee wangym 2012-11-22 add for CR00735223 start
        if(MmsApp.mIsTouchModeSupport )
        {
            mScaleDetector = new ScaleDetector(this, new ScaleListener());
            mTextSize = MessageUtils.getTextSize(this);
            changeTextSize(mTextSize);
            mZoomIn.setVisibility(View.GONE);
            mZoomOut.setVisibility(View.GONE);
        }
        //gionee wangym 2012-11-22 add for CR00735223 end
    }

    private void changeTextZoom(Boolean zoomIn) {
        if (textContent == null) {
            return;
        }
        if (zoomIn) {
            if (currentSize + TEXT_SIZE_CHANGE <= MAX_TEXT_SIZE) {
                currentSize = currentSize + TEXT_SIZE_CHANGE;
                Log.i(TAG, "zoom in, got currentSize is: " + currentSize);
                textContent.setTextSize(currentSize);
                if (mZoomOut.isEnabled() == false && mZoomOut.isClickable() == false) {
                    mZoomOut.setEnabled(true);
                    mZoomOut.setClickable(true);
                }
            }
            if (currentSize == MAX_TEXT_SIZE) {
                mZoomIn.setPressed(false);
                mZoomIn.setEnabled(false);
                mZoomIn.setClickable(false);
            }
        } else {
            if (currentSize - TEXT_SIZE_CHANGE >= MIN_TEXT_SIZE) {
                currentSize = currentSize - TEXT_SIZE_CHANGE;
                Log.i(TAG, "zoom out, got currentSize is: " + currentSize);
                textContent.setTextSize(currentSize);
                if (mZoomIn.isEnabled() == false && mZoomIn.isClickable() == false) {
                    mZoomIn.setEnabled(true);
                    mZoomIn.setClickable(true);
                }
            }
            if (currentSize == MIN_TEXT_SIZE) {
                mZoomOut.setPressed(false);
                mZoomOut.setEnabled(false);
                mZoomOut.setClickable(false);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Aurora xuyong 2013-09-13 modified for aurora's new feature start
        ActionBar actionBar = getActionBar();
        // Aurora xuyong 2013-09-13 modified for aurora's new feature end
        actionBar.setDisplayHomeAsUpEnabled(true);
        Contact.addListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume,msgType:" + msgType);
        if (searchUri == null){
            Log.e(TAG,"smsId is wrong");
            return;
        }
        // get all SIM info
        mGetSimInfoRunnable.run();
        String[] projection = SMS_PROJECTION;
        Cursor cursor = null;
        if (msgType == 1){
            projection = SMS_PROJECTION;
            cursor = getContentResolver().query(
                    searchUri, // URI
                    projection, // projection
                    null, // selection
                    null, // selection args
                    null); // sortOrder
        } else if (msgType == 3){
            //mTitle.setText(R.string.viewer_title_wappush);
            // Aurora xuyong 2013-09-13 modified for aurora's new feature start
            ActionBar actionBar = getActionBar();
            // Aurora xuyong 2013-09-13 modified for aurora's new feature end
            actionBar.setTitle(R.string.viewer_title_wappush);
            projection = WAPPUSH_PROJECTION;
            cursor = getContentResolver().query(
                searchUri, // URI
                projection, // projection
                null, // selection
                null, // selection args
                null); // sortOrder
        } else if (msgType == 4){
            //mTitle.setText(R.string.viewer_title_cb);
            // Aurora xuyong 2013-09-13 modified for aurora's new feature start
            ActionBar actionBar = getActionBar();
            // Aurora xuyong 2013-09-13 modified for aurora's new feature end
            actionBar.setTitle(R.string.viewer_title_cb);
            projection = CB_PROJECTION;
            String selection = "_id=" + searchUri.getPathSegments().get(1);
            Log.d(TAG, "query cb selection = " + selection);
            cursor = getContentResolver().query(
                    CB_URI, // URI
                    projection, // projection
                    selection, // selection
                    null, // selection args
                    null); // sortOrder
        } 
        
        try {
            if (cursor == null || cursor.getCount() == 0){
                Log.w(TAG,"cursor is null");
                return;
            }
            Log.d(TAG, "cursor count = " + cursor.getCount());
            cursor.moveToFirst();
            reciBody = cursor.getString(2);
            mMsgBox = cursor.getInt(3);
            //record sim card
            if (msgType == 4) {
                mSimId = cursor.getInt(8);
            } else {
                mSimId = cursor.getInt(9);
            }
            if (mMsgBox == 3 || msgType == 4){//draft
                //come here should be impossible.
                String recipientIds = getRecipientIds(cursor.getInt(4));
                reciNumber = getContactNumber(recipientIds);
            } else { 
                reciNumber = getContactNumberByNumber(cursor.getString(0));
            }
            Log.d(TAG, "reciNumber = " + reciNumber);
            String showNumber = "";
            String reDate = "";
            reciDateLong = cursor.getLong(1);
            reciDate = MessageUtils.formatTimeStampString(this, cursor.getLong(1));
            if (mMsgBox == 1 || msgType == 3 || msgType == 4) {
                showNumber = getString(R.string.via_without_time_for_send) + ": " + reciNumber;
                reDate = String.format(getString(R.string.received_on),reciDate);
            } else {
                showNumber = getString(R.string.via_without_time_for_recieve) + ": " + reciNumber;
                reDate = String.format(getString(R.string.sent_on), reciDate);
            }
            threadId = cursor.getInt(4);
            status = cursor.getInt(5);
            Log.d(TAG, "reciNumber = " + showNumber + "\n reciDate = " + reciDate + "\n reciBody = " + reciBody);
            if (msgType == 1) {
                locked = cursor.getInt(6);
                mServiceCenter = cursor.getString(8);
            } else if (msgType == 3) {//wappush
                String url = cursor.getString(6);
                reciBody = reciBody + "\n" + url;
                mServiceCenter = cursor.getString(8);
            }
            msgid = cursor.getLong(7);
            recipent.setText(showNumber);
            date.setText(reDate);
            SmileyParser parser = SmileyParser.getInstance();
            textContent.setText(parser.addSmileySpans(reciBody));
            //show card indicator
            if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
                formatSimStatus();
            } else {
                mByCard.setVisibility(View.GONE);
            }
            //update it to has read status
            markSmsRead(msgType);
        } finally {
             if (cursor != null){
                 cursor.close();
             }
        }
    }
    
    private String getRecipientIds(int threadId){
        Uri uri = Uri.parse("content://mms-sms/thread_id");
        final Uri reUri = ContentUris.withAppendedId(uri, threadId);
        Log.d(TAG, "getRecipientIds uri = " + reUri.getPath());
        Cursor c = null;
        String res = "";
        try {
            c = getContentResolver().query(reUri, null, null, null, null);
            Log.e(TAG, "count is " + c.getCount());
            if (c == null) {
                Log.e(TAG, "getRecipientIds cursor is null");
                return null;
            }
            c.moveToFirst();
            res = c.getString(0);
            Log.d(TAG, "getRecipientIds = " + res);
            return res;
        } finally {
            c.close();
        }
    }
    
    private String getContactNumberByNumber(String recNum){
         if (recNum == null){
            Log.d(TAG, "getContactNumber recNum is null");
            return getString(android.R.string.unknownName);
         }
         mNumber = recNum;
         ContactList recipients = ContactList.getByNumbers(recNum, true, false); 
         String res = "";
         if (recipients != null && !recipients.isEmpty()) {
             //for (Contact contact:recipients) {
             //    contact.reload(true);  
             //}
             res = recipients.formatNames(", ");
         } else {
             res = getString(android.R.string.unknownName);
         }
         Log.d(TAG, "getContactNumber recNum res IS " + res);
         return res;
    }
    
    private String getContactNumber(String recipientIds){
         if (recipientIds == null){
            Log.d(TAG, "getContactNumber recipientIds is null");
            return getString(android.R.string.unknownName);
         }
         ContactList recipients = ContactList.getByIds(recipientIds, true); 
         String res = "";
         if (recipients != null && !recipients.isEmpty()) {
             for (Contact contact:recipients) {
                 contact.reload(true);  
             }
             res = recipients.formatNames(", ");
         } else {
             res = getString(android.R.string.unknownName);
         }
         Log.d(TAG, "getContactNumber recipientIds res IS " + res);
         return res;
    }
    
    private void markSmsRead(int type) {
        Uri readUri = null;
        final ContentValues values = new ContentValues(1);
        values.put("read", 1);
        values.put("seen", 1);
        if (type == 1){
            readUri = ContentUris.withAppendedId(SMS_URI, msgid);
            SqliteWrapper.update(getApplicationContext(), getContentResolver(), readUri, values,null, null);
        } else if (type == 3){
            readUri = ContentUris.withAppendedId(WAPPUSH_URI, msgid);
            SqliteWrapper.update(getApplicationContext(), getContentResolver(), readUri, values, null, null);
        } else if (type == 4){
            String selection = "_id=" + searchUri.getPathSegments().get(1);
            SqliteWrapper.update(getApplicationContext(), getContentResolver(), CB_URI, values, selection, null);
        } 
  
        //cancel the notification
        updateNotification(this, type);
    }
    
    public static void updateNotification(final Context context, final int type) {
        new Thread(new Runnable() {
            public void run() {
                 if (type == 1){
                     //update sms notification
                     MessagingNotification.blockingUpdateNewMessageIndicator(context, false, false);
                 } else if (type == 3){
                     WapPushMessagingNotification.blockingUpdateNewMessageIndicator(context, false);
                 } else if (type == 4){
                     CBMessagingNotification.updateNewMessageIndicator(context);
                 }
            }
        }).start();
    }
    
    private final OnCreateContextMenuListener mContextMenuCreateListener = new OnCreateContextMenuListener() {

        @Override
        public void onCreateContextMenu(ContextMenu arg0, View arg1, ContextMenuInfo arg2) {
            // TODO Auto-generated method stub
            //addPositionBasedMenuItems(arg0, arg1, arg2);
            arg0.setHeaderTitle(R.string.message_options);
            MsgListMenuClickListener l = new MsgListMenuClickListener();
            addCallAndContactMenuItems(arg0, l);
            if (msgType == 1){
                arg0.add(0, MENU_COPY_MESSAGE_TEXT, 0,
                        R.string.copy_message_text).setOnMenuItemClickListener(l);
                arg0.add(0, MENU_SAVE_MESSAGE_TO_SIM, 0,
                        R.string.save_message_to_sim).setOnMenuItemClickListener(l);
            }
            arg0.add(0, MENU_SELECT_TEXT, 0, R.string.select_text)
            .setOnMenuItemClickListener(l);
            arg0.add(0, MENU_VIEW_MESSAGE_DETAILS, 0, R.string.view_message_details)
            .setOnMenuItemClickListener(l);
            arg0.add(0, MENU_DELETE_MESSAGE, 0, R.string.delete_message)
            .setOnMenuItemClickListener(l);
        }
    };
 
    /**
     * Context menu handlers for the message list view.
     */
    private final class MsgListMenuClickListener implements MenuItem.OnMenuItemClickListener {

        @Override
        public boolean onMenuItemClick(MenuItem arg0) {
            // TODO Auto-generated method stub
            switch(arg0.getItemId()){
             case MENU_LOCK_MESSAGE: {
                  lockMessage(msgid, true);
                  return true;
              }
              case MENU_UNLOCK_MESSAGE: {
                  lockMessage(msgid, false);
                  return true;
              }
              case MENU_SAVE_MESSAGE_TO_SIM: {
                  mSaveMsgThread = new SaveMsgThread(msgid);
                  mSaveMsgThread.start();
                  return true;
              }
              case MENU_REPLY:{
                 MessageUtils.replyMessage(getBaseContext(), mNumber);
                 return true;
              }
              case MENU_RESEND:{
                  resendMsg();
                  return true;
              }
              case MENU_DELETE_MESSAGE:{
                  deleteMsg(msgid);
                  return true;
              }
              case MENU_COPY_MESSAGE_TEXT:{
                 copyToClipboard(reciBody);
                  return true;
              }
              case MENU_VIEW_MESSAGE_DETAILS: {
                  String messageDetails = getMessageDetails();
                  new AuroraAlertDialog.Builder(FolderModeSmsViewer.this)
                          .setTitle(R.string.message_details_title)
                          .setMessage(messageDetails)
                          .setPositiveButton(android.R.string.ok, null)
                          .setCancelable(true)
                          .show();
                  return true;
              }
              case MENU_ADD_TO_BOOKMARK:{
                  if (mURLs.size() == 1) {
                      Browser.saveBookmark(FolderModeSmsViewer.this, null, mURLs.get(0));
                  } else if(mURLs.size() > 1) {
                      CharSequence[] items = new CharSequence[mURLs.size()];
                      for (int i = 0; i < mURLs.size(); i++) {
                          items[i] = mURLs.get(i);
                      }
                      /*new AuroraAlertDialog.Builder(FolderModeSmsViewer.this)
                          .setTitle(R.string.menu_add_to_bookmark)
                          .setIcon(R.drawable.ic_dialog_menu_generic)
                          .setItems(items, new DialogInterface.OnClickListener() {
                              public void onClick(DialogInterface dialog, int which) {
                                  Browser.saveBookmark(FolderModeSmsViewer.this, null, mURLs.get(which));
                                  }
                              })
                          .show();*/
                  }
                  return true;
               }
               case MENU_SELECT_TEXT:
                   AuroraAlertDialog.Builder dialog = new AuroraAlertDialog.Builder(FolderModeSmsViewer.this)
                                                        .setPositiveButton(R.string.yes, null);
                   LayoutInflater factory = LayoutInflater.from(dialog.getContext());
                   final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);
                   AuroraEditText contentSelector = (AuroraEditText)textEntryView.findViewById(R.id.content_selector);
                   contentSelector.setText(reciBody);
                   dialog.setView(textEntryView).show();
                   return true;
              default:
                  return false;
            }
        }
        
    }
    
    private String getMessageDetails(){
        StringBuilder details = new StringBuilder();
        Resources res = getResources();

        // Message Type: Text message.
        details.append(res.getString(R.string.message_type_label));
        if (msgType == 1){
            details.append(res.getString(R.string.text_message));
        } else if (msgType == 3){
            details.append(res.getString(R.string.wp_msg_type));
        } else if (msgType == 4){
            details.append(res.getString(R.string.cb_message));
        }
       
        // Address: ***
        details.append('\n');
        if (mMsgBox == Sms.MESSAGE_TYPE_INBOX) {
            details.append(res.getString(R.string.from_label));
        } else {
            details.append(res.getString(R.string.to_address_label));
        }
        details.append(reciNumber);
        // Date: ***
        details.append('\n');
        if (mMsgBox == Sms.MESSAGE_TYPE_INBOX) {
            details.append(res.getString(R.string.received_label));
        } else {
            details.append(res.getString(R.string.sent_label));
        }
        details.append(reciDate);
        
        // Message Center: ***
        if (mMsgBox == 1 || msgType == 3) {
            //gionee gaoj 2012-6-18 added for CR00625734 start
            if (MmsApp.mGnMessageSupport) {
                if (!TextUtils.isEmpty(mServiceCenter)) {
                    details.append('\n');
                    details.append(res.getString(R.string.service_center_label));
                    details.append(mServiceCenter);
                }
            } else {
            //gionee gaoj 2012-6-18 added for CR00625734 end
            details.append('\n');
            details.append(res.getString(R.string.service_center_label));
            details.append(mServiceCenter);
            //gionee gaoj 2012-6-18 added for CR00625734 start
            }
            //gionee gaoj 2012-6-18 added for CR00625734 end
        }
        return details.toString();
     }
    private void copyToClipboard(String str) {
        ClipboardManager clip = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        clip.setText(str);
    }
    
    private void deleteMsg(long id){
        final Uri deleteUri = ContentUris.withAppendedId(SMS_URI, id);
        MessageUtils.confirmDeleteMessage(this, deleteUri); 
    } 
    private void lockMessage(long id, boolean lock){
        final Uri lockUri = ContentUris.withAppendedId(SMS_URI, id);

        final ContentValues values = new ContentValues(1);
        values.put("locked", lock ? 1 : 0);

        new Thread(new Runnable() {
            public void run() {
                getContentResolver().update(lockUri, values, null, null);
            }
        }, "lockMessage").start();
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.slideshow_menu, menu);
        // add extra menu option by condition
        if (mMsgBox == 1) {
            menu.add(0, MENU_REPLY, 1, R.string.menu_reply);
        } else if (mMsgBox == 5) {
            menu.add(0, MENU_RESEND, 1, R.string.menu_retry_sending);
        }
        //show report
        if (((mMsgBox == Sms.MESSAGE_TYPE_SENT)
           ||(mMsgBox == Sms.MESSAGE_TYPE_OUTBOX)
           ||(mMsgBox == Sms.MESSAGE_TYPE_QUEUED))
           && (isSms())
           && (status != Sms.STATUS_NONE)) {
            menu.add(0, MENU_VIEW_REPORT, 0, R.string.view_delivery_report);
        }

        super.onCreateOptionsMenu(menu);
        return true;
    }
 
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isSms()
            &&((mMsgBox == Sms.MESSAGE_TYPE_OUTBOX) || (mMsgBox == Sms.MESSAGE_TYPE_QUEUED))
            &&(menu.findItem(MENU_RESEND) == null)) {
            // update sms msgbox, it may be send fail and moved to fail box, need show resend item.
            Cursor cursor = null;
            try {
                cursor = getContentResolver().query(searchUri, SMS_PROJECTION, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    mMsgBox = cursor.getInt(3);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            if (mMsgBox == Sms.MESSAGE_TYPE_FAILED) {
                menu.add(0, MENU_RESEND, 1, R.string.menu_retry_sending);
            }
        }
        boolean showAddContact = false;
        // if there is at least one number not exist in contact db, should show add.
        mContactList = ContactList.getByNumbers(mNumber, false, true);
        for (Contact contact : mContactList) {
            if (!contact.existsInDatabase()) {
                showAddContact = true;
                Log.d(TAG, "not in contact[number:" + contact.getNumber() + ",name:" + contact.getName());
                break;
            }
        }
        boolean menuAddExist = (menu.findItem(MENU_ADD_CONTACT)!= null);
        if (showAddContact) {
            if (!menuAddExist) {
                menu.add(0, MENU_ADD_CONTACT, 1, R.string.menu_add_to_contacts);
            }
        } else {
            menu.removeItem(MENU_ADD_CONTACT);
        }
       /* if (isSms() && menu.findItem(MENU_CALL_RECIPIENT)== null && isRecipientCallable()) {
            MenuItem item = menu.add(0, MENU_CALL_RECIPIENT, 0, R.string.menu_call)
                .setIcon(R.drawable.ic_menu_call)
                .setTitle(R.string.menu_call);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add(0, MENU_CALL_RECIPIENT_BY_VT, 0, R.string.call_video_call)
                .setIcon(R.drawable.ic_video_call)
                .setTitle(R.string.call_video_call);
        }
*/
        super.onPrepareOptionsMenu(menu);
        return true;
    }
     
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
             case R.id.message_forward:
                // this is a little slow if mms is big.
                forwardMessage(reciBody);
                break;
            case R.id.message_delete:
                MessageUtils.confirmDeleteMessage(this, searchUri);
                break;
            case MENU_ADD_CONTACT:
                addToContact();
                break;
            case MENU_REPLY:
                MessageUtils.replyMessage(this, mNumber);
                break;
            case MENU_RESEND:
                resendMsg();
                break;
            case MENU_VIEW_REPORT:
                showDeliveryReport();
                break;
            case android.R.id.home:
                Intent it = new Intent(this, FolderViewList.class);
                it.putExtra("floderview_key", mHomeBox);
                finish();
                startActivity(it);
                break;
            case MENU_CALL_RECIPIENT:
                dialRecipient(false);
                break;
            case MENU_CALL_RECIPIENT_BY_VT:
                dialRecipient(true);
                break;
            default:
                return false;
        }
        return true;
    }
    
    private void forwardMessage(String body) {
        Intent intent = new Intent();
        intent.setClassName(this, "com.android.mms.ui.ForwardMessageActivity");
        intent.putExtra("forwarded_message", true);
        if (body != null) {
            // add for SMS forward with sender
            String smsBody = body;
            if (MmsApp.isTelecomOperator()) {
                SharedPreferences prefs = AuroraPreferenceManager.getDefaultSharedPreferences(this);
                boolean smsForwardWithSender = prefs.getBoolean(MessagingPreferenceActivity.SMS_FORWARD_WITH_SENDER, false);
                Log.d(TAG, "forwardMessage(): SMS Forward With Sender ?= " + smsForwardWithSender);
                if (smsForwardWithSender) {
                    if (mMsgBox == Sms.MESSAGE_TYPE_INBOX) {
                        smsBody += "\n" + getString(R.string.forward_from);
                        Contact contact = Contact.get(mNumber, false);
                        Log.d(TAG, "forwardMessage(): Contact's name and number="
                                   + Contact.formatNameAndNumber(contact.getName(), contact.getNumber(), ""));
                        smsBody += Contact.formatNameAndNumber(contact.getName(), contact.getNumber(), "");
                    }
                }
            }
            intent.putExtra("sms_body", smsBody);
        }
        startActivity(intent);
    } 
    
    private void addToContact(){
        int count = mContactList.size();
        switch(count) {
        case 0:
            Log.e(TAG, "add contact, mCount == 0!");
            break;
        case 1:
            Intent intent = ConversationList.createAddContactIntent(mNumber);
            startActivity(intent);
            //MessageUtils.addNumberOrEmailtoContact(reciNumber, 0, this);
            break;
        default:
            //MultiRecipientsActivity.setContactList(mContactList);
            //final Intent i = new Intent(getApplicationContext(), MultiRecipientsActivity.class);
            //startActivity(i);
            break;
        }
    }
    private void resendMsg(){
      try {
          final MessageSender sender = new SmsSingleRecipientSender(this,
                  mNumber, reciBody, threadId, status == Sms.STATUS_PENDING,
                  searchUri);
          final Context ct = this;
          final ContentResolver cr = this.getContentResolver();
          final Uri mUri = this.searchUri;
          new Thread(new Runnable(){
              public void run() {
                    try {
                        if (status == Sms.STATUS_FAILED) {
                            ContentValues cv = new ContentValues();
                            cv.put(Sms.STATUS, Sms.STATUS_NONE);
                            SqliteWrapper.update(ct, cr, mUri, cv, null, null);
                        }
                        sender.sendMessage(SendingProgressTokenManager.NO_TOKEN);
                    } catch (MmsException e) {
                        Log.e(TAG, "Can't resend mms.");
                    }
                  // Make sure this thread isn't over the limits in message count
                  Recycler.getSmsRecycler().deleteOldMessagesByThreadId(getApplicationContext(), threadId);
              }
          }).start();
      } catch (Exception e) {
          Log.e(TAG, "Failed to send message: " + searchUri + ", threadId=" + threadId, e);
      }
      finish();
    }
    
    private boolean isNumberInContacts(String phoneNumber) {
        return Contact.get(phoneNumber, false).existsInDatabase();
    }
    
    private final void addCallAndContactMenuItems(ContextMenu menu, MsgListMenuClickListener l) {
        // Add all possible links in the address & message
        StringBuilder textToSpannify = new StringBuilder();
        if (isInbox()) {
            textToSpannify.append(reciNumber + ": ");
        }
        textToSpannify.append(reciBody);

        SpannableString msg = new SpannableString(textToSpannify.toString());
        Linkify.addLinks(msg, Linkify.ALL);
        ArrayList<String> uris =
            MessageUtils.extractUris(msg.getSpans(0, msg.length(), URLSpan.class));
        mURLs.clear();
        while (uris.size() > 0) {
            String uriString = uris.remove(0);
            // Remove any dupes so they don't get added to the menu multiple times
            while (uris.contains(uriString)) {
                uris.remove(uriString);
            }

            int sep = uriString.indexOf(":");
            String prefix = null;
            if (sep >= 0) {
                prefix = uriString.substring(0, sep);
                if ("mailto".equalsIgnoreCase(prefix) || "tel".equalsIgnoreCase(prefix)){
                    uriString = uriString.substring(sep + 1);
                }
            }
            boolean addToContacts = false;
            if ("mailto".equalsIgnoreCase(prefix)) {
                String sendEmailString = getString(R.string.menu_send_email).replace("%s", uriString);
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("mailto:" + uriString));
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                menu.add(0, MENU_SEND_EMAIL, 0, sendEmailString).setOnMenuItemClickListener(l).setIntent(intent);
                addToContacts = !haveEmailContact(uriString);
            } else if ("tel".equalsIgnoreCase(prefix)) {
                String callBackString = getString(R.string.menu_call_back).replace("%s", uriString);
                Intent intent = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:" + uriString));
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                menu.add(0, MENU_CALL_BACK, 0, callBackString).setOnMenuItemClickListener(l).setIntent(intent);
                
                if (reciBody != null && reciBody.replaceAll("\\-", "").contains(uriString)) {
                    String sendSmsString = getString(
                        R.string.menu_send_sms).replace("%s", uriString);
                    Intent intentSms = new Intent(Intent.ACTION_SENDTO,
                        Uri.parse("smsto:" + uriString));
                    intentSms.setClassName(FolderModeSmsViewer.this, "com.android.mms.ui.SendMessageToActivity");
                    intentSms.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    menu.add(0, MENU_SEND_SMS, 0, sendSmsString)
                        .setOnMenuItemClickListener(l)
                        .setIntent(intentSms);
                }
                addToContacts = !isNumberInContacts(uriString);
            } else {
                //add URL to book mark
                if (mURLs.size() <= 0) {
                    menu.add(0, MENU_ADD_TO_BOOKMARK, 0, R.string.menu_add_to_bookmark)
                    .setOnMenuItemClickListener(l);
                }
                mURLs.add(uriString);
            }
            if (addToContacts) {
                //Intent intent = ConversationList.createAddContactIntent(uriString);
                Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
                intent.putExtra(ContactsContract.Intents.Insert.PHONE, uriString);
                String addContactString = getString(
                        R.string.menu_add_address_to_contacts).replace("%s", uriString);
                menu.add(0, MENU_ADD_ADDRESS_TO_CONTACTS, 0, addContactString)
                    .setOnMenuItemClickListener(l)
                    .setIntent(intent);
            }
        }
    }
    
    private boolean haveEmailContact(String emailAddress) {
        Cursor cursor = SqliteWrapper.query(this, getContentResolver(),
                Uri.withAppendedPath(Email.CONTENT_LOOKUP_URI, Uri.encode(emailAddress)),
                new String[] { Contacts.DISPLAY_NAME }, null, null, null);

        if (cursor != null) {
            try {
                String name;
                while (cursor.moveToNext()) {
                    name = cursor.getString(0);
                    if (!TextUtils.isEmpty(name)) {
                        return true;
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return false;
    }

    private void addPositionBasedMenuItems(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info;

        try {
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo");
            return;
        }
        final int position = info.position;

        addUriSpecificMenuItems(menu, v, position);
    }

    private void addUriSpecificMenuItems(ContextMenu menu, View v, int position) {
        Uri uri = getSelectedUriFromMessageList((AuroraListView) v, position);

        if (uri != null) {
            Intent intent = new Intent(null, uri);
            intent.addCategory(Intent.CATEGORY_SELECTED_ALTERNATIVE);
            menu.addIntentOptions(0, 0, 0,
                    new android.content.ComponentName(this, ComposeMessageActivity.class),
                    null, intent, 0, null);
        }
    }

    private Uri getSelectedUriFromMessageList(AuroraListView listView, int position) {
        // If the context menu was opened over a uri, get that uri.
        MessageListItem msglistItem = (MessageListItem) listView.getChildAt(position);
        if (msglistItem == null) {
            // FIXME: Should get the correct view. No such interface in AuroraListView currently
            // to get the view by position. The AuroraListView.getChildAt(position) cannot
            // get correct view since the list doesn't create one child for each item.
            // And if setSelection(position) then getSelectedView(),
            // cannot get corrent view when in touch mode.
            return null;
        }

        TextView textView;
        CharSequence text = null;
        int selStart = -1;
        int selEnd = -1;

        //check if message sender is selected
        textView = (TextView) msglistItem.findViewById(R.id.text_view);
        if (textView != null) {
            text = textView.getText();
            selStart = textView.getSelectionStart();
            selEnd = textView.getSelectionEnd();
        }

        if (selStart == -1) {
            //sender is not being selected, it may be within the message body
            textView = (TextView) msglistItem.findViewById(R.id.body_text_view);
            if (textView != null) {
                text = textView.getText();
                selStart = textView.getSelectionStart();
                selEnd = textView.getSelectionEnd();
            }
        }

        // Check that some text is actually selected, rather than the cursor
        // just being placed within the TextView.
        if (selStart != selEnd) {
            int min = Math.min(selStart, selEnd);
            int max = Math.max(selStart, selEnd);

            URLSpan[] urls = ((Spanned) text).getSpans(min, max,
                                                        URLSpan.class);

            if (urls.length == 1) {
                return Uri.parse(urls[0].getURL());
            }
        }

        //no uri was selected
        return null;
    }
    
    private OnClickListener mClickListener = new OnClickListener(){
        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
             onMessageItemClick();
        }
    };

    private void onMessageItemClick(){
         boolean mIsTel = false;
         URLSpan[] spans = textContent.getUrls();
         final java.util.ArrayList<String> urls = MessageUtils.extractUris(spans);
         final String telPrefix = "tel:";
         String url = ""; 
         for(int i=0;i<spans.length;i++) {
             url = urls.get(i);
             if(url.startsWith(telPrefix)) {
                 mIsTel = true;
                 urls.add("smsto:"+url.substring(telPrefix.length()));
             }
         }

         if (spans.length == 0) {
             // Do nothing.
         } else if (spans.length == 1 && !mIsTel) {
             /*Uri uri = Uri.parse(spans[0].getURL());
             Intent intent = new Intent(Intent.ACTION_VIEW, uri);
             intent.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());
             intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
             startActivity(intent);*/
             final String mUriTemp = spans[0].getURL();
             if (!mUriTemp.startsWith("mailto:")) {//a url
                 AuroraAlertDialog.Builder b = new AuroraAlertDialog.Builder(FolderModeSmsViewer.this);
                 b.setTitle(R.string.url_dialog_choice_title);
                 b.setMessage(R.string.url_dialog_choice_message);
                 b.setCancelable(true);
                 b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                     public final void onClick(DialogInterface dialog, int which) {
                         dialog.dismiss();
                     }
                 });
                 b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                     
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         Uri uri = Uri.parse(mUriTemp);
                         Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                         intent.putExtra(Browser.EXTRA_APPLICATION_ID, FolderModeSmsViewer.this.getPackageName());
                         intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                         FolderModeSmsViewer.this.startActivity(intent);
                     }
                 });
                 b.show();
            } else {//open mail directly
                Uri uri = Uri.parse(mUriTemp);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.putExtra(Browser.EXTRA_APPLICATION_ID, FolderModeSmsViewer.this.getPackageName());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                FolderModeSmsViewer.this.startActivity(intent);
            }
         } else {
             ArrayAdapter<String> adapter =
                 new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, urls) {
                 public View getView(int position, View convertView, ViewGroup parent) {
                     View v = super.getView(position, convertView, parent);
                     try {
                         String url = getItem(position).toString();
                         TextView tv = (TextView) v;
                         Drawable d = getPackageManager().getActivityIcon(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                         if (d != null) {
                             d.setBounds(0, 0, d.getIntrinsicHeight(), d.getIntrinsicHeight());
                             tv.setCompoundDrawablePadding(10);
                             tv.setCompoundDrawables(d, null, null, null);
                         }
                         final String telPrefix = "tel:";
                         if (url.startsWith(telPrefix)) {
                             url = PhoneNumberUtils.formatNumber(url.substring(telPrefix.length()));
                         }
                         final String smsPrefix = "smsto:";
                         if (url.startsWith(smsPrefix)) {
                             url = PhoneNumberUtils.formatNumber(url.substring(smsPrefix.length()));
                         }
                         tv.setText(url);
                     } catch (android.content.pm.PackageManager.NameNotFoundException ex) {
                         ;
                     }
                     return v;
                 }
             };

             AuroraAlertDialog.Builder b = new AuroraAlertDialog.Builder(this);

             DialogInterface.OnClickListener click = new DialogInterface.OnClickListener() {
                 public final void onClick(DialogInterface dialog, int which) {
                     if (which >= 0) {
                         Uri uri = Uri.parse(urls.get(which));
                         Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                         intent.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());
                         if (urls.get(which).startsWith("smsto:")) {
                             intent.setClassName(FolderModeSmsViewer.this, "com.android.mms.ui.SendMessageToActivity");
                         }
                         intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                         startActivity(intent);
                     }
                     dialog.dismiss();
                 }
             };

             b.setTitle(R.string.select_link_title);
             b.setCancelable(true);
             b.setAdapter(adapter, click);

             b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                 public final void onClick(DialogInterface dialog, int which) {
                     dialog.dismiss();
                 }
             });

             b.show();
         }
    }

// save sim message 
 private Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_SAVE_MESSAGE_TO_SIM_SUCCEED:
                Toast.makeText(FolderModeSmsViewer.this, R.string.save_message_to_sim_successful, Toast.LENGTH_SHORT).show();
                break;

            case MSG_SAVE_MESSAGE_TO_SIM_FAILED_GENERIC:
                Toast.makeText(FolderModeSmsViewer.this, R.string.save_message_to_sim_unsuccessful, Toast.LENGTH_SHORT).show();
                break;

            case MSG_SAVE_MESSAGE_TO_SIM_FAILED_SIM_FULL:
                Toast.makeText(FolderModeSmsViewer.this, 
                        getString(R.string.save_message_to_sim_unsuccessful) + ". " + getString(R.string.sim_full_title), 
                        Toast.LENGTH_SHORT).show();
                break;

            case MSG_SAVE_MESSAGE_TO_SIM:
                String type = (String)msg.obj;
                long msgId = msg.arg1;
                saveMessageToSim(type, msgId);
                break;

            default:
                Log.d(TAG, "inUIHandler msg unhandled.");
                break;
            }
        }
    };
    
    private final class SaveMsgThread extends Thread {
        private long msgId = 0;
        public SaveMsgThread(long id) {
            msgId = id;
        }
        public void run() {
            Looper.prepare();
            if (null != Looper.myLooper()) {
                mSaveMsgHandler = new SaveMsgHandler(Looper.myLooper());
            }
            Message msg = mSaveMsgHandler.obtainMessage(MSG_SAVE_MESSAGE_TO_SIM);
            msg.arg1 = (int)msgId;
            if (FeatureOption.MTK_GEMINI_SUPPORT && mSimCount > 1) {
                mUiHandler.sendMessage(msg);
            } else {
                mSaveMsgHandler.sendMessage(msg);
            }
            Looper.loop();
        }
    }

    private final class SaveMsgHandler extends Handler {
        public SaveMsgHandler(Looper looper) {
            super(looper);
        }
        
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_QUIT_SAVE_MESSAGE_THREAD: {
                    Log.d(TAG, "exit save message thread");
                    getLooper().quit();
                    break;
                }

                case MSG_SAVE_MESSAGE_TO_SIM: {
                    String type = (String)msg.obj;
                    long msgId = msg.arg1;
                    //saveMessageToSim(type, msgId);
                    getMessageAndSaveToSim(msgId);
                    break;
                }

                case MSG_SAVE_MESSAGE_TO_SIM_AFTER_SELECT_SIM: {
                    Intent it = (Intent)msg.obj;
                    getMessageAndSaveToSim(it);
                    break;
                }

                default:
                    break;
            }
        }
    }
    
    private void getMessageAndSaveToSim(Intent intent) {
        Log.d(TAG, "get message and save to sim, selected sim id = " + mSelectedSimId);
        String msgType = intent.getStringExtra("message_type");
        long msgId = intent.getLongExtra("message_id", 0);
        getMessageAndSaveToSim(msgId);
    }

    private void getMessageAndSaveToSim(long msgId){
        int result = 0; 
        String scAddress = null;
   
        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> messages = null;
        messages = smsManager.divideMessage(reciBody);

        int smsStatus = 0;
        long timeStamp = 0;
        if (isInbox()) {
            smsStatus = SmsManager.STATUS_ON_ICC_READ;
            timeStamp = reciDateLong;
            scAddress = getServiceCenter();
        } else if (isSentbox()) {
            smsStatus = SmsManager.STATUS_ON_ICC_SENT;
        } else if (isFailedbox()) {
            smsStatus = SmsManager.STATUS_ON_ICC_UNSENT;
        } else {
            Log.e(TAG, "Unknown sms status");
        }

        if (scAddress == null) {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                scAddress = GnTelephonyManagerEx.getDefault().getScAddress(SIMInfo.getSlotById(this, mSelectedSimId));
            } else {
                scAddress = GnTelephonyManagerEx.getDefault().getScAddress(0);
            }
        }

        Log.d(TAG, "\t scAddress\t= " + scAddress);
        Log.d(TAG, "\t Address\t= " + mNumber);
        Log.d(TAG, "\t msgBody\t= " + reciBody);
        Log.d(TAG, "\t smsStatus\t= " + smsStatus);
        Log.d(TAG, "\t timeStamp\t= " + timeStamp);

        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            int slotId = -1;
            if (mSimCount == 1) {
                slotId = mSimInfoList.get(0).mSlot;
            } else {
                slotId = SIMInfo.getSlotById(this, mSelectedSimId);
            }
            Log.d(TAG, "\t slot Id\t= " + slotId);

            result = GnGeminiSmsManager.copyTextMessageToIccCardGemini(scAddress, 
                    mNumber, messages, smsStatus, timeStamp, slotId);
        } else {
            result = GnSmsManager.getDefault().copyTextMessageToIccCard(scAddress, 
                    mNumber, messages, smsStatus, timeStamp);
        }
        Log.d(TAG, "\t result \t= " + result);
        // Aurora xuyong 2013-11-15 modified for S4 adapt start
        if (result == 0/*SmsManager.RESULT_ERROR_SUCCESS*/) {
        // Aurora xuyong 2013-11-15 modified for S4 adapt end
            //mSaveMsgHandler.sendEmptyMessage(MSG_SAVE_MESSAGE_TO_SIM_SUCCEED);
            mUiHandler.sendEmptyMessage(MSG_SAVE_MESSAGE_TO_SIM_SUCCEED);  
        // Aurora xuyong 2013-11-15 modified for S4 adapt start          
        } else if (result == 7 /*SmsManager.RESULT_ERROR_SIM_MEM_FULL*/) {
        // Aurora xuyong 2013-11-15 modified for S4 adapt end
            //mSaveMsgHandler.sendEmptyMessage(MSG_SAVE_MESSAGE_TO_SIM_FAILED_SIM_FULL);
            mUiHandler.sendEmptyMessage(MSG_SAVE_MESSAGE_TO_SIM_FAILED_SIM_FULL);
        } else {
            //mSaveMsgHandler.sendEmptyMessage(MSG_SAVE_MESSAGE_TO_SIM_FAILED_GENERIC);
            mUiHandler.sendEmptyMessage(MSG_SAVE_MESSAGE_TO_SIM_FAILED_GENERIC);
        }
        mSaveMsgHandler.sendEmptyMessageDelayed(MSG_QUIT_SAVE_MESSAGE_THREAD, 5000);
    }
    
 private void showSimSelectedDialog(Intent intent) {
        // TODO get default SIM and get contact SIM
        final Intent it = intent;
        List<Map<String, ?>> entries = new ArrayList<Map<String, ?>>();
        for (int i = 0; i < mSimCount; i++) {
            SIMInfo simInfo = mSimInfoList.get(i);
            HashMap<String, Object> entry = new HashMap<String, Object>();

            entry.put("simIcon", simInfo.mSimBackgroundRes);
            int state = getSimStatus(i);
            entry.put("simStatus", getStatusResource(state));
            String simNumber = "";
            if (!TextUtils.isEmpty(simInfo.mNumber)) {
                switch(simInfo.mDispalyNumberFormat) {
                    //case android.provider.Telephony.SimInfo.DISPLAY_NUMBER_DEFAULT:
                    case GnTelephony.SimInfo.DISPLAY_NUMBER_FIRST:
                        if(simInfo.mNumber.length() <= 4)
                            simNumber = simInfo.mNumber;
                        else
                            simNumber = simInfo.mNumber.substring(0, 4);
                        break;
                    case GnTelephony.SimInfo.DISPLAY_NUMBER_LAST:
                        if(simInfo.mNumber.length() <= 4)
                            simNumber = simInfo.mNumber;
                        else
                            simNumber = simInfo.mNumber.substring(simInfo.mNumber.length() - 4);
                        break;
                    case 0://android.provider.Telephony.SimInfo.DISPLAY_NUMBER_NONE:
                        simNumber = "";
                        break;
                }
            }
            if (!TextUtils.isEmpty(simNumber)) {
                entry.put("simNumberShort",simNumber);
            } else {
                entry.put("simNumberShort", "");
            }

            entry.put("simName", simInfo.mDisplayName);
            if (!TextUtils.isEmpty(simInfo.mNumber)) {
                entry.put("simNumber", simInfo.mNumber);
            } else {
                entry.put("simNumber", "");
            }
//            if (mAssociatedSimId == (int) simInfo.mSimId) {
//                // if this SIM is contact SIM, set "Suggested"
//                entry.put("suggested", getString(R.string.suggested));
//            } else {
//                entry.put("suggested", "");// not suggested
//            }
            if (!is3G(i)) {
                entry.put("sim3g", "");
            } else {
                //MTK_OP02_PROTECT_START
                if (MmsApp.isUnicomOperator()) {
                    entry.put("sim3g", "3G");
                } else 
                //MTK_OP02_PROTECT_END
                {
                    entry.put("sim3g", "");
                }
            }
            entries.add(entry);
        }

        final SimpleAdapter a = new SimpleAdapter(
                this,
                entries,
                R.layout.sim_selector,
                new String[] {"simIcon", "simStatus", "simNumberShort", "simName", "simNumber", "suggested", "sim3g"},
                new int[] {R.id.sim_icon, R.id.sim_status, R.id.sim_number_short, R.id.sim_name, R.id.sim_number, R.id.sim_suggested, R.id.sim3g});
        SimpleAdapter.ViewBinder viewBinder = new SimpleAdapter.ViewBinder() {
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view instanceof ImageView) {
                    if (view.getId() == R.id.sim_icon) {
                        ImageView simicon = (ImageView) view.findViewById(R.id.sim_icon);
                        simicon.setBackgroundResource((Integer) data);
                    } else if (view.getId() == R.id.sim_status) {
                        ImageView simstatus = (ImageView)view.findViewById(R.id.sim_status);
                        simstatus.setImageResource((Integer)data);
                    }
                    return true;
                }
                return false;
            }
        };
        a.setViewBinder(viewBinder);
        AuroraAlertDialog.Builder b = new AuroraAlertDialog.Builder(this);
        b.setTitle(getString(R.string.sim_selected_dialog_title));
        b.setCancelable(true);
        b.setAdapter(a, new DialogInterface.OnClickListener() {
            @SuppressWarnings("unchecked")
            public final void onClick(DialogInterface dialog, int which) {
                mSelectedSimId = (int) mSimInfoList.get(which).mSimId;
                if (it.getIntExtra(SELECT_TYPE, -1) == SIM_SELECT_FOR_SEND_MSG) {
                 //  confirmSendMessageIfNeeded();
                } else if (it.getIntExtra(SELECT_TYPE, -1) == SIM_SELECT_FOR_SAVE_MSG_TO_SIM) {
                    //getMessageAndSaveToSim(it);
                    Message msg = mSaveMsgHandler.obtainMessage(MSG_SAVE_MESSAGE_TO_SIM_AFTER_SELECT_SIM);
                    msg.obj = it;
                    //mSaveMsgHandler.sendMessageDelayed(msg, 60);
                    mSaveMsgHandler.sendMessage(msg);
                }
                dialog.dismiss();
            }
        });
        mSIMSelectDialog = b.create();
        mSIMSelectDialog.show();
    }
    
    /**
     * getSimStatus
     * @param id is the slot
     * @return state
     */
    public int getSimStatus(int id) {
        GnTelephonyManagerEx mTelephonyManager = GnTelephonyManagerEx.getDefault();
        //int slotId = SIMInfo.getSlotById(this,listSimInfo.get(id).mSimId);
        int slotId = mSimInfoList.get(id).mSlot;
        if (slotId != -1) {
            return mTelephonyManager.getSimIndicatorStateGemini(slotId);
        }
        return -1;
    }

    public boolean is3G(int id) {
        GnTelephonyManagerEx mTelephonyManager = GnTelephonyManagerEx.getDefault();
        //int slotId = SIMInfo.getSlotById(this, listSimInfo.get(id).mSimId);
        int slotId = mSimInfoList.get(id).mSlot;
        Log.i(TAG, "is3G SIMInfo.getSlotById id: " + id + " slotId: " + slotId);
        if (slotId == 0) {
            return true;
        }
        return false;
    }

    static int getStatusResource(int state) {
        Log.i("Utils gemini", "!!!!!!!!!!!!!state is "+state);
        switch (state) {
            /*case GnPhone.SIM_INDICATOR_RADIOOFF:
                return R.drawable.sim_radio_off;
            case GnPhone.SIM_INDICATOR_LOCKED:
                return R.drawable.sim_locked;
            case GnPhone.SIM_INDICATOR_INVALID:
                return R.drawable.sim_invalid;
            case GnPhone.SIM_INDICATOR_SEARCHING:
                return R.drawable.sim_searching;
            case GnPhone.SIM_INDICATOR_ROAMING:
                return R.drawable.sim_roaming;
            case GnPhone.SIM_INDICATOR_CONNECTED:
                return R.drawable.sim_connected;
            case GnPhone.SIM_INDICATOR_ROAMINGCONNECTED:
                return R.drawable.sim_roaming_connected;*/
            default:
                return -1;
        }
    }
    
    private void saveMessageToSim(String msgType, long msgId) {
        Intent intent = new Intent();
        intent.putExtra("message_type", msgType);
        intent.putExtra("message_id", msgId);
        intent.putExtra(SELECT_TYPE, SIM_SELECT_FOR_SAVE_MSG_TO_SIM);
        showSimSelectedDialog(intent);
    }
    
    private void getSimInfoList() {
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            mSimInfoList = SIMInfo.getInsertedSIMList(this);
            mSimCount = mSimInfoList.isEmpty()? 0: mSimInfoList.size();
            Log.v(TAG, "getSimInfoList(): mSimCount = " + mSimCount);
        } else { // single SIM
           
            if (GnPhone.phone != null) {
                try {
                    mSimCount = GnPhone.isSimInsert() ? 1: 0;
                } catch (Exception e) {
                    Log.e(TAG, "check sim insert status failed");
                    mSimCount = 0;
                }
            }
        }
    }
    
    Runnable mGetSimInfoRunnable = new Runnable() {
        public void run() {
            getSimInfoList();
        }
    };
    
    private boolean isInbox(){
        return mMsgBox == 1;
    }
    
    private boolean isSms(){
        return msgType == 1;
    }
    
    private boolean isSentbox(){
        return mMsgBox == 2;
    }
    
    private boolean isFailedbox(){
        return mMsgBox == 5;
    }
     
    private String getServiceCenter(){
        return mServiceCenter;
    }

    private void showDeliveryReport() {
        Intent intent = new Intent(this, DeliveryReportActivity.class);
        intent.putExtra("message_id", msgid);
        intent.putExtra("message_type", "sms");
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Contact.removeListener(this);
    }

    // We don't want to show the "call" option unless there is only one
    // recipient and it's a phone number.
    private boolean isRecipientCallable() {
        return (mContactList.size() == 1 && !mContactList.containsEmail());
    }

    private void dialRecipient(boolean isVideoCall) {
        if (isRecipientCallable()) {
            String number = mContactList.get(0).getNumber();
            Intent dialIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
            if (isVideoCall) {
                dialIntent.putExtra("com.android.phone.extra.video", true);
            }
            startActivity(dialIntent);
        }
    }

    private void formatSimStatus() {
        SpannableStringBuilder buffer = new SpannableStringBuilder();
        int simInfoStart = buffer.length();
        CharSequence simInfo = MessageUtils.getSimInfo(this, mSimId);
        if(simInfo.length() > 0){
            buffer.append(getString(R.string.by_card));
            simInfoStart = buffer.length();
            buffer.append(" ");
            buffer.append(simInfo);
        }
        int color = getResources().getColor(R.color.timestamp_color);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
        buffer.setSpan(colorSpan, 0, simInfoStart, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mByCard.setText(buffer);
    }

    public void onUpdate(Contact updated) {
        Log.d(TAG, "onUpdate,update number and name:" + updated.getNumber() + "," + updated.getName());
        if (updated.getNumber().equals(mNumber)) {
            if (!updated.getName().equals(reciNumber)) {
                reciNumber = updated.getName();
                String showNumber = "";
                if (mMsgBox == 1 || msgType == 3 || msgType == 4) {
                    showNumber = getString(R.string.via_without_time_for_send) + ": " + reciNumber;
                } else {
                    showNumber = getString(R.string.via_without_time_for_recieve) + ": " + reciNumber;
                }
                final String showString = showNumber;
                runOnUiThread(new Runnable() {
                    public void run() {
                        recipent.setText(showString);
                    }
                });
            }
        }
    }

    //gionee wangym 2012-11-22 add for CR00735223 start
    @Override
    public boolean  dispatchTouchEvent(MotionEvent event){
        boolean ret = false;

        if(mScaleDetector != null){
            ret = mScaleDetector.onTouchEvent(event);
        }
        if(!ret){
            ret = super.dispatchTouchEvent(event); 
        }
        return ret;
    }

    private void changeTextSize(float size){
        if(textContent != null){
            textContent.setTextSize(size);
        }
    }

    public class ScaleListener implements OnScaleListener{

        public boolean onScaleStart(ScaleDetector detector) {
           
           Log.i(TAG, "onScaleStart -> mTextSize = " + mTextSize);
        return true;
    }

    public void onScaleEnd(ScaleDetector detector) {
       Log.i(TAG, "onScaleEnd -> mTextSize = " + mTextSize);
    //save current value to preference
            MessageUtils.setTextSize(FolderModeSmsViewer.this, mTextSize);
        }

        public boolean onScale(ScaleDetector detector) {

            float size = mTextSize * detector.getScaleFactor();
           
            if(Math.abs(size - mTextSize) < MIN_ADJUST_TEXT_SIZE){
                return false;
            }            
            if(size < MIN_TEXT_SIZE){
                size = MIN_TEXT_SIZE;
            }            
            if(size > MAX_TEXT_SIZE){
                size = MAX_TEXT_SIZE;
            }            
            if(size != mTextSize){
                mTextSize = size;
                changeTextSize(size);
            }
            return true;
        }
    };
    //gionee wangym 2012-11-22 add for CR00735223 end
}
