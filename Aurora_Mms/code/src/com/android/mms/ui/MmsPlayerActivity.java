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

import com.android.mms.LogTag;
import com.android.mms.R;
import com.android.mms.TempFileProvider;
import com.android.mms.data.Contact;
import com.android.mms.dom.AttrImpl;
import com.android.mms.dom.smil.SmilDocumentImpl;
import com.android.mms.dom.smil.SmilPlayer;
import com.android.mms.dom.smil.parser.SmilXmlSerializer;
import com.android.mms.model.AudioModel;
import com.android.mms.model.FileAttachmentModel;
import com.android.mms.model.LayoutModel;
import com.android.mms.model.MediaModel;
import com.android.mms.model.RegionMediaModel;
import com.android.mms.model.RegionModel;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
import com.android.mms.model.SmilHelper;
import com.android.mms.model.TextModel;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.MmsException;
//Aurora xuyong 2013-11-15 modified for google adapt end

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.smil.SMILDocument;
import org.w3c.dom.smil.SMILElement;

import aurora.app.AuroraActivity;
// Aurora liugj 2013-09-13 modified for aurora's new feature start
import android.app.ActionBar;
// Aurora liugj 2013-09-13 modified for aurora's new feature end
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraListActivity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.text.ClipboardManager;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.util.Config;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import aurora.widget.AuroraButton;
import aurora.widget.AuroraEditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import aurora.widget.AuroraListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.AbsoluteLayout.LayoutParams;
import android.widget.MediaController.MediaPlayerControl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import android.widget.Toast;
import android.content.ActivityNotFoundException;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SqliteWrapper;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.ContentType;
import com.aurora.android.mms.pdu.PduBody;
import com.aurora.android.mms.pdu.PduPart;
//Aurora xuyong 2013-11-15 modified for google adapt end
import com.aurora.featureoption.FeatureOption;
import android.os.SystemProperties;
import android.provider.Browser;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
//add for cmcc dir ui begin
import com.android.mms.transaction.MessagingNotification;
import android.content.ContentUris;
import android.content.ContentValues;
import gionee.provider.GnTelephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.MmsSms.PendingMessages;
import android.text.TextUtils;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.pdu.PduPersister;
import com.aurora.android.mms.pdu.MultimediaMessagePdu;
//Aurora xuyong 2013-11-15 modified for google adapt end
import com.android.mms.transaction.MessageSender;
import com.android.mms.transaction.MmsMessageSender;
import android.content.DialogInterface;
import com.android.mms.util.Recycler;
import com.android.mms.util.SmileyParser;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.pdu.EncodedStringValue;
import com.aurora.android.mms.pdu.SendReq;
//Aurora xuyong 2013-11-15 modified for google adapt end
import aurora.app.AuroraAlertDialog;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.MmsApp;
//add for cmcc dir ui end
import com.android.mms.data.Conversation;
import com.android.mms.data.WorkingMessage;
//gionee wangym 2012-11-22 add for CR00735223 start
import com.android.mms.ui.ScaleDetector.OnScaleListener;
import com.aurora.mms.ui.AuroraConvListActivity;

import android.view.MotionEvent;
//gionee wangym 2012-11-22 add for CR00735223 end


/**
 * Plays the given slideshow in full-screen mode with a common controller.
 */
public class MmsPlayerActivity extends AuroraActivity implements Contact.UpdateListener {
    private static final String TAG = "MmsPlayerActivity";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;
    private MmsPlayerActivityAdapter mListAdapter;
    private final int MENU_PLAYASSLIDE = 1;
    private Uri msgUri;
    private AuroraListView listView;
    private ContentResolver mContentResolver;
    private Cursor mCursor = null;
    private AsyncQueryHandler mQueryHandler = null;
    private SmileyParser parser = SmileyParser.getInstance();

    // context menu item
    private final int MENU_COPY_MESSAGE_TEXT         = 1;
    private final int MENU_ADD_TO_BOOKMARK           = 2;
    private final int MENU_ADD_TO_CONTACTS           = 3;
    private final int MENU_ADD_ADDRESS_TO_CONTACTS   = 4;
    private final int MENU_SEND_EMAIL                = 5;
    private final int MENU_CALL_BACK                 = 6;
    private final int MENU_SEND_SMS                  = 7;
    private final int MENU_SELECT_TEXT               = 8;
    
    public static final int REQUEST_CODE_ADD_CONTACT      = 1;
    private final int DEFAULT_TEXT_SIZE = 20;
    private final int TEXT_SIZE_CHANGE = 2;
    private final int MIN_TEXT_SIZE = 10;
    private final int MAX_TEXT_SIZE = 30;
    private int currentSize = DEFAULT_TEXT_SIZE;
    private Context mContext;
    private ArrayList<String> mURLs = new ArrayList<String>();
    private String mCopyText;
    //MTK_OP01_PROTECT_START 
    private ImageButton mZoomIn;
    private ImageButton mZoomOut;
    private View mZoomControls;
    //MTK_OP01_PROTECT_END
    
    //add for cmcc dir ui begin
    private boolean mDirMode = false;
    private TextView mRecipient;
    private TextView mDate;
    private TextView mByCard;
    private TextView mMmsSubject;
    private ImageView mHasFileAttachment;
    private TextView mAttachName;
    private Long mDateLong;
    private int mSimId;
    private String mNumber;
    private String mName;
    private int mIndex = -1; //the first number index in ContactList can be added to contact 
    private ContactList mContactList;
    private boolean mShowAddContact = false;
    private Uri mMsgUri;
    private int mMsgBox;
    private boolean mShowResend;
    private long mThreadId;
    private long mMsgId;
    private int mDeliveryReport;
    private int mReadReport;
    private String mSubject;
    private SlideshowModel mSlideshow;
    private int mHomeBox = 0;
    private static final String[] PDU_PROJECTION = new String[] {
        Mms._ID,
        Mms.MESSAGE_BOX,
        Mms.THREAD_ID,
        Mms.RETRIEVE_TEXT,
        Mms.SUBJECT,
        Mms.SUBJECT_CHARSET,
        Mms.DELIVERY_REPORT,
        Mms.READ_REPORT,
        Mms.DATE,
        Mms.SIM_ID
    };
    private static final int PDU_COLUMN_ID                    = 0;
    private static final int PDU_COLUMN_MESSAGE_BOX           = 1;
    private static final int PDU_COLUMN_THREAD_ID             = 2;
    private static final int PDU_COLUMN_RETRIEVE_TEXT         = 3;
    private static final int PDU_COLUMN_SUBJECT               = 4;
    private static final int PDU_COLUMN_SUBJECT_CHARSET       = 5;
    private static final int COLUMN_MMS_DELIVERY_REPORT       = 6;
    private static final int COLUMN_MMS_READ_REPORT           = 7;    
    private static final int COLUMN_MMS_DATE                  = 8;
    private static final int COLUMN_MMS_SIM_ID                = 9;

    // option menu item
    private static final int MENU_REPLY           = 2;
    private static final int MENU_RESEND          = 3;
    private static final int MENU_ADD_CONTACT     = 4;
    private static final int MENU_VIEW_REPORT     = 5;
    private static final int MENU_FORWARD         = 6;
    private static final int MENU_DELETE          = 7;
    private static final int MNEU_DETAIL          = 8;
    private static final int MENU_CALL_RECIPIENT  = 9;
    private static final int MENU_CALL_RECIPIENT_BY_VT  = 10;
    private static final int MENU_COPY_TO_SDCARD        = 11;
    //add for cmcc dir ui end

    //gionee wangym 2012-11-22 add for CR00735223 start
   private final float MIN_ADJUST_TEXT_SIZE = 0.2f;
   private ScaleDetector mScaleDetector;
   private float mTextSize = DEFAULT_TEXT_SIZE;
   private boolean mIsCmcc = false;
   //gionee wangym 2012-11-22 add for CR00735223 end

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    
    @Override
    public void onCreate(Bundle icicle) {
        //gionee gaoj 2012-6-27 added for CR00628364 start
        if (MmsApp.mLightTheme) {
            setTheme(R.style.GnMmsLightTheme);
        } else if (MmsApp.mDarkStyle) {
            setTheme(R.style.GnMmsDarkTheme);
        }
        //gionee gaoj 2012-6-27 added for CR00628364 end
        super.onCreate(icicle);
        mContext = this;
        Intent intent = getIntent();
        msgUri = intent.getData();
        //add for cmcc dir ui begin
        mDirMode = intent.getBooleanExtra("dirmode", false);
        mHomeBox = intent.getIntExtra("folderbox", 0);
        //add for cmcc dir ui end
        setContentView(R.layout.mms_player_activity);
        listView = (AuroraListView) findViewById(R.id.slide_item_list);
        //add for cmcc dir mode begin
        if (mDirMode) {
            View header = LayoutInflater.from(this).inflate(R.layout.foldermode_mms_header, null);
            mRecipient = (TextView)header.findViewById(R.id.msg_recipent);
            mDate = (TextView)header.findViewById(R.id.msg_date);
            mByCard = (TextView)header.findViewById(R.id.by_card);
            mMmsSubject = (TextView)header.findViewById(R.id.msg_subject);
            mHasFileAttachment = (ImageView)header.findViewById(R.id.has_file_attachment);
            mAttachName = (TextView)header.findViewById(R.id.attach_Name);
            mMmsSubject.setVisibility(View.GONE);
            mHasFileAttachment.setVisibility(View.GONE);
            mAttachName.setVisibility(View.GONE);
            listView.addHeaderView(header);
        }
        //add for cmcc dir mode end
        
        //MTK_OP01_PROTECT_START 
        if (MmsApp.isTelecomOperator()) {
            mZoomIn = (ImageButton) findViewById(R.id.zoomIn);
            mZoomOut = (ImageButton) findViewById(R.id.zoomOut);
            mZoomControls = findViewById(R.id.zoomControls);
            
            mZoomIn.setVisibility(View.VISIBLE);
            mZoomOut.setVisibility(View.VISIBLE);
            mZoomControls.setVisibility(View.VISIBLE);
            
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
        }
        //MTK_OP01_PROTECT_END

        //gionee wangym 2012-11-22 add for CR00735223 start
        if(MmsApp.mIsTouchModeSupport ){
            mScaleDetector = new ScaleDetector(this, new ScaleListener());
            mTextSize = MessageUtils.getTextSize(this);
            mIsCmcc = true;
        }
        //gionee wangym 2012-11-22 add for CR00735223 end

        //add for cmcc dir ui begin
        if (mDirMode) {
            mMsgUri = msgUri;
            parseMsgUri();
        }
        //add for cmcc dir ui end
        initListAdapter(msgUri);
    }

    private void initListAdapter(Uri msg) {
        //PduBody body = null;
        SlideshowModel slideshowModel = null;
        try {
            //body = SlideshowModel.getPduBody(MmsPlayerActivity.this, msg);
            slideshowModel = SlideshowModel.createFromMessageUri(MmsPlayerActivity.this, msg);
            //add for cmcc dir ui begin
            mSlideshow = slideshowModel;
            //add for cmmc dir ui end
        } catch (MmsException e) {
            
        }
        
        int slideNum = slideshowModel.size();
        Log.i(TAG, "initListAdapter,getPartsNum:" + slideNum);
        ArrayList<MmsPlayerActivityItemData> attachments = new ArrayList<MmsPlayerActivityItemData>(slideNum);
        for(int i = 0; i < slideNum; i++) {
            SlideModel slideModel = slideshowModel.get(i);
            if (slideModel == null) {
                break;
            }
            String text = null;
            Uri imageUri = null;
            Uri videoUri = null;
            String audioName = null;
            for (MediaModel media : slideModel) {
                if (media.isImage()) {
                    imageUri= media.getUri();
                } else if (media.isVideo()) {
                    videoUri = media.getUri();
                } else if (media.isAudio()) {
                    audioName= media.getSrc();
                    Log.i(TAG, " add audio name: " + audioName);
                } else if (media.isText()) {
                    
                    TextModel sm = slideModel.getText();
                    if (sm != null) {
                        text = sm.getText();
                    }
                }
            }
            Log.i(TAG, "Add slide: " + i + " imageUri is: " + imageUri + 
                    " videoUri is: " + videoUri + " audio name is: " + audioName);
            attachments.add(new MmsPlayerActivityItemData(this, imageUri, videoUri, audioName, text));
        }
        
        if (mDirMode) {
            if (mSubject != null) {
                mMmsSubject.setText(parser.addSmileySpans(getString(R.string.subject_label) + mSubject));
                mMmsSubject.setVisibility(View.VISIBLE);
            }
            int fileAttachmentCount = slideshowModel.sizeOfFilesAttach();
            if (fileAttachmentCount > 0) {
                Log.i(TAG, "has file attachment");
                String src = slideshowModel.getAttachFiles().get(0).getSrc();
                mAttachName.setText(src);
                mHasFileAttachment.setVisibility(View.VISIBLE);
                mAttachName.setVisibility(View.VISIBLE);
            }
        }

        attachments.trimToSize();
        mListAdapter = new MmsPlayerActivityAdapter(this, attachments);
        listView.setAdapter(mListAdapter);
        listView.setOnCreateContextMenuListener(mTextMenuCreateListener);
        
    }
    
    private int changeTextZoom(boolean zoomIn) {
        if (mListAdapter == null) {
            return currentSize;
        }
        if (zoomIn) {
            if (currentSize + TEXT_SIZE_CHANGE <= MAX_TEXT_SIZE) {
                currentSize = currentSize + TEXT_SIZE_CHANGE;
                Log.i(TAG, "zoom in, got currentSize is: " + currentSize);
                mListAdapter.setTextSize(currentSize);
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
                mListAdapter.setTextSize(currentSize);
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
        mListAdapter.notifyDataSetChanged();
        return currentSize;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }
    
    @Override
    protected void onPause() {
        if (mListAdapter != null) {
            mListAdapter.clearAllCache();
        }
        super.onPause();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        Contact.removeListener(this);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        // Aurora liugj 2013-09-13 modified for aurora's new feature start
        ActionBar actionBar = getActionBar();
        // Aurora liugj 2013-09-13 modified for aurora's new feature end
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.mms);
        Contact.addListener(this);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (mDirMode) {
            updateRecipient();
        }
        if (mListAdapter != null) {
            mListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_PLAYASSLIDE, 0, R.string.play_as_slideshow);
        // add for cmcc dir ui mode begin
        if (mDirMode) {
            if (haveSomethingToCopyToSDCard(mMsgId)) {
                menu.add(0, MENU_COPY_TO_SDCARD, 0, R.string.copy_to_sdcard);
            }
            menu.add(0, MENU_FORWARD, 0, R.string.menu_forward);
            menu.add(0, MENU_DELETE, 0, R.string.delete_message);
            // add extra menu option by condition
            if (mMsgBox == Mms.MESSAGE_BOX_INBOX) {
                menu.add(R.id.slidegroup, MENU_REPLY, 1, R.string.menu_reply);
            } else if ((mMsgBox == Mms.MESSAGE_BOX_OUTBOX) && mShowResend/*send fail*/) {
                menu.add(R.id.slidegroup, MENU_RESEND, 1, R.string.menu_retry_sending);
            }
            //show report
            if (((mMsgBox == Mms.MESSAGE_BOX_OUTBOX) || (mMsgBox == Mms.MESSAGE_BOX_SENT))
                && (mDeliveryReport == 128 || mReadReport == 128/*show only has been requested*/)) {
                menu.add(R.id.slidegroup, MENU_VIEW_REPORT, 0, R.string.view_delivery_report);
            }
            menu.add(0, MNEU_DETAIL, 0, R.string.slideshow_details);
            /*if (isRecipientCallable()) {
                MenuItem item = menu.add(0, MENU_CALL_RECIPIENT, 0, R.string.menu_call)
                    .setIcon(R.drawable.ic_menu_call)
                    .setTitle(R.string.menu_call);
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                menu.add(0, MENU_CALL_RECIPIENT_BY_VT, 0, R.string.call_video_call)
                    .setIcon(R.drawable.ic_video_call)
                    .setTitle(R.string.call_video_call);
            }*/
        }
        // add for cmcc dir ui mode end

        super.onCreateOptionsMenu(menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case MENU_PLAYASSLIDE:
                Intent intent = new Intent(this, SlideshowActivity.class);      
                intent.setData(msgUri);
                this.startActivity(intent);
                break;
             // add for cmcc dir ui mode begin
             case MENU_FORWARD:
                // this is a little slow if mms is big.
                new Thread(new Runnable() {
                    public void run() {
                        forwardMms();
                    }
                }).start();
                break;
            case MENU_DELETE:
                MessageUtils.confirmDeleteMessage(this, mMsgUri);
                break;
            case MNEU_DETAIL:
                onClickDetails(null);
                break;
            case MENU_ADD_CONTACT:
                addToContact();
                break;
            case MENU_REPLY:
                MessageUtils.replyMessage(this, mNumber);
                break;
            case MENU_RESEND:
                Uri uri = null;
                try {
                    PduPersister persister = PduPersister.getPduPersister(this);
                    uri = persister.move(mMsgUri, Mms.Draft.CONTENT_URI);
                    final MessageSender sender = new MmsMessageSender(this, uri, mSlideshow.getCurrentSlideshowSize());
                    final long threadId = mThreadId;
                    new Thread(new Runnable(){
                        public void run() {
                            try {
                                sender.sendMessage(threadId);
                            } catch(MmsException e) {
                                Log.e(TAG, "Can't resend mms.");
                            }
                            // Make sure this thread isn't over the limits in message count
                            Recycler.getMmsRecycler().deleteOldMessagesByThreadId(getApplicationContext(), threadId);
                        }
                    }).start();
                } catch (Exception e) {
                    Log.e(TAG, "Failed to send message: " + uri + ", threadId=" + mThreadId, e);
                }
                finish();
                break;
            case MENU_VIEW_REPORT:
                showDeliveryReport();
                break;
            case android.R.id.home:
                if (mDirMode) {
                        // Aurora liugj 2013-11-06 modified for aurora's new feature start
                    Intent it = new Intent(this, AuroraConvListActivity.class);
                    //it.putExtra("floderview_key", mHomeBox);
                        // Aurora liugj 2013-11-06 modified for aurora's new feature end
                    finish();
                    startActivity(it);
                } else {
                    //open what?
                    finish();
                }
                break;
            case MENU_CALL_RECIPIENT:
                dialRecipient(false);
                break;
            case MENU_CALL_RECIPIENT_BY_VT:
                dialRecipient(true);
                break;
            case MENU_COPY_TO_SDCARD: {
                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    Toast.makeText(MmsPlayerActivity.this, getString(R.string.Insert_sdcard), Toast.LENGTH_LONG).show();
                    break;
                }

                final long iMsgId = mMsgId;
                Intent i = new Intent(MmsPlayerActivity.this, MultiSaveActivity.class);
                i.putExtra("msgid", iMsgId);
                startActivityForResult(i,0);
                break;
            }
            default:
                break;
            // add for cmcc dir ui mode end
        }
        return super.onOptionsItemSelected(item);
    }

    private final OnCreateContextMenuListener mTextMenuCreateListener =
        new OnCreateContextMenuListener() {
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            TextMenuClickListener l = new TextMenuClickListener();
            int itemId = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
            //cmcc dir mode code begin
            if (mDirMode) {
                if (itemId ==0) {
                    //the header did not response
                    return;
                }
                itemId--;// list view has a header now.
            }
            //cmcc dir mode code end
            Log.i(TAG, "getItem Id: " + itemId);
            MmsPlayerActivityItemData data = (MmsPlayerActivityItemData) mListAdapter.getItem(itemId);
            if (data != null) {
                addCallAndContactMenuItems(menu, l, data.getText());
            } else {
                Log.i(TAG, "getItem null");
            }
            
        }
    };
    
    private final class TextMenuClickListener implements MenuItem.OnMenuItemClickListener {
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case MENU_COPY_MESSAGE_TEXT:
                    ClipboardManager clip =
                        (ClipboardManager)MmsPlayerActivity.this.getSystemService(mContext.CLIPBOARD_SERVICE);
                    clip.setText(mCopyText); 
                    return true;
                
                case MENU_ADD_TO_BOOKMARK:
                    if (mURLs.size() == 1) {
                        Browser.saveBookmark(mContext, null, mURLs.get(0));
                    } else if(mURLs.size() > 1) {
                        CharSequence[] items = new CharSequence[mURLs.size()];
                        for (int i = 0; i < mURLs.size(); i++) {
                            items[i] = mURLs.get(i);
                        }/*
                        new AuroraAlertDialog.Builder(mContext)
                            .setTitle(R.string.menu_add_to_bookmark)
                            .setIcon(R.drawable.ic_dialog_menu_generic)
                            .setItems(items, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Browser.saveBookmark(mContext, null, mURLs.get(which));
                                    }
                                })
                            .show();*/
                    }
                    
                    return true;
                case MENU_ADD_ADDRESS_TO_CONTACTS: 
                    Intent mAddContactIntent = item.getIntent();
                    startActivityForResult(mAddContactIntent, REQUEST_CODE_ADD_CONTACT);
                    return true;
                //MTK_OP01_PROTECT_START 
                case MENU_SELECT_TEXT:
                    AuroraAlertDialog.Builder dialog = new AuroraAlertDialog.Builder(mContext)
                                                        .setPositiveButton(R.string.yes, null);
                    LayoutInflater factory = LayoutInflater.from(dialog.getContext());
                    final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);
                    AuroraEditText contentSelector = (AuroraEditText)textEntryView.findViewById(R.id.content_selector);
                    contentSelector.setText(mCopyText);
                    dialog.setView(textEntryView).show();
                    return true;
                //MTK_OP01_PROTECT_END
            }
            return false;
        }
    }
    
    
    private final void addCallAndContactMenuItems(
            ContextMenu menu, TextMenuClickListener l, CharSequence text) {
        // Add all possible links in the address & message
        StringBuilder textToSpannify = new StringBuilder();
        textToSpannify.append(text);

        SpannableString msg = new SpannableString(textToSpannify.toString());
        Linkify.addLinks(msg, Linkify.ALL);
        ArrayList<String> uris =
            MessageUtils.extractUris(msg.getSpans(0, msg.length(), URLSpan.class));
        mURLs.clear();
        menu.setHeaderTitle(R.string.message_options);
        if (text!=null && text.length()>0) {
            menu.add(0, MENU_COPY_MESSAGE_TEXT, 0, R.string.copy_message_text)
                .setOnMenuItemClickListener(l);
            //MTK_OP01_PROTECT_START 
            if (MmsApp.isTelecomOperator()) {
                menu.add(0, MENU_SELECT_TEXT, 0, R.string.select_text)
                    .setOnMenuItemClickListener(l);
            }
            //MTK_OP01_PROTECT_END
            mCopyText = (String) text;
        }
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
                String sendEmailString = mContext.getString(
                        R.string.menu_send_email).replace("%s", uriString);
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("mailto:" + uriString));
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                Log.i(TAG, "send email number: " + sendEmailString);
                menu.add(0, MENU_SEND_EMAIL, 0, sendEmailString)
                    .setOnMenuItemClickListener(l)
                    .setIntent(intent);
                addToContacts = !haveEmailContact(uriString);
            } else if ("tel".equalsIgnoreCase(prefix)) {
                String callBackString = mContext.getString(
                        R.string.menu_call_back).replace("%s", uriString);
                Intent intent = new Intent(Intent.ACTION_DIAL,
                        Uri.parse("tel:" + uriString));
                
                //gionee gaoj 2013-4-2 added for CR00792780 start
                intent.setComponent(new ComponentName("com.android.contacts",
                "com.android.contacts.activities.DialtactsActivity"));
                //gionee gaoj 2013-4-2 added for CR00792780 end
                
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                Log.i(TAG, "call back number: " + callBackString);
                menu.add(0, MENU_CALL_BACK, 0, callBackString)
                    .setOnMenuItemClickListener(l)
                    .setIntent(intent);
                
                if (text != null && text.toString().replaceAll("\\-", "").contains(uriString)) {
                    String sendSmsString = mContext.getString(
                        R.string.menu_send_sms).replace("%s", uriString);
                    Intent intentSms = new Intent(Intent.ACTION_SENDTO,
                        Uri.parse("smsto:" + uriString));
                    intentSms.setClassName(mContext, "com.android.mms.ui.SendMessageToActivity");
                    intentSms.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Log.i(TAG, "send sms number: " + sendSmsString);
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
                Intent intent = ConversationList.createAddContactIntent(uriString);
                String addContactString = mContext.getString(
                        R.string.menu_add_address_to_contacts).replace("%s", uriString);
                menu.add(0, MENU_ADD_ADDRESS_TO_CONTACTS, 0, addContactString)
                    .setOnMenuItemClickListener(l)
                    .setIntent(intent);
            }
        }
    }
    
    private boolean haveEmailContact(String emailAddress) {
        Cursor cursor = SqliteWrapper.query(mContext, mContext.getContentResolver(),
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

    private boolean isNumberInContacts(String phoneNumber) {
        return Contact.get(phoneNumber, false).existsInDatabase();
    }

    public interface OnDataSetChangedListener {
        void onDataSetChanged(MessageListAdapter adapter);
    }

    //add for cmcc dir ui begin
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!mDirMode) {
            return true;
        }
        //mContactList = ContactList.getByNumbers(mNumber, false, true);
        int count  = 0;
        mIndex = -1;
        for (Contact contact : mContactList) {
            if (!contact.existsInDatabase()) {
                mIndex = count;
                Log.d(TAG, "not in contact[number:" + contact.getNumber() + ",name:" + contact.getName());
                break;
            }
            count++;
        }
        boolean menuAddExist = (menu.findItem(MENU_ADD_CONTACT)!= null);
        if (mIndex != -1/*ICS has no add multi recipients ui*/) {
            if (!menuAddExist) {
                menu.add(R.id.slidegroup, MENU_ADD_CONTACT, 1, R.string.menu_add_to_contacts);
            }
        } else {
            menu.removeItem(MENU_ADD_CONTACT);
        }
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    private void parseMsgUri() {
        Log.d(TAG, "uri:" + mMsgUri.toString());
        Cursor c = null;
        try {
            c = SqliteWrapper.query(this, getContentResolver(), mMsgUri,
                            PDU_PROJECTION, null, null, null);
            if (c == null || c.getCount() != 1) {
                Log.e(TAG, "count is not 1!!");
                return;
            }
            c.moveToFirst();
            mMsgBox = c.getInt(PDU_COLUMN_MESSAGE_BOX);
            mThreadId = c.getLong(PDU_COLUMN_THREAD_ID);
            mMsgId = c.getLong(PDU_COLUMN_ID);
            mSubject = c.getString(PDU_COLUMN_SUBJECT);
            mDeliveryReport = c.getInt(COLUMN_MMS_DELIVERY_REPORT);
            mReadReport= c.getInt(COLUMN_MMS_READ_REPORT);
            mDateLong = c.getLong(COLUMN_MMS_DATE)*1000L;
            mSimId = c.getInt(COLUMN_MMS_SIM_ID);
            if (!TextUtils.isEmpty(mSubject)) {
                EncodedStringValue v = new EncodedStringValue(
                        c.getInt(PDU_COLUMN_SUBJECT_CHARSET),
                        PduPersister.getBytes(mSubject));
                mSubject = v.getString();
            }
            Log.d(TAG, "msgbox:"+mMsgBox+",threadId:"+mThreadId+",subject:"+mSubject);
            // get the mms related numbers
            PduPersister persister = PduPersister.getPduPersister(this);
            MultimediaMessagePdu pdu = (MultimediaMessagePdu)persister.load(mMsgUri);
            if (mMsgBox == Mms.MESSAGE_BOX_INBOX) {
                mNumber = pdu.getFrom().getString();
                Log.d(TAG, "this mms is from:" + mNumber);
            } else {
                EncodedStringValue numbers[] = pdu.getTo();
                mNumber = EncodedStringValue.concat(numbers);
                Log.d(TAG, "this mms is to:" + mNumber);
            }
            /*
            mContactList = ContactList.getByNumbers(mNumber, false, true);
            if (mContactList.size() == 1) {
                mName = mContactList.get(0).getName();
            } else {
                mName = mContactList.getFirstName(null);
            }
            Log.d(TAG, "mName:" + mName);
            */
            //check whether this mms was sent fail.
            if (mMsgBox == Mms.MESSAGE_BOX_OUTBOX) {
                checkSendFail();
            }
        } catch(MmsException e) {
            Log.e(TAG, "MmsException:" + e.toString());
        } finally {
            if (c != null) {
                c.close();
            }
        }
        // fill mms header view
        String date;
        date = MessageUtils.formatTimeStampString(this, mDateLong);
        if (mMsgBox == Mms.MESSAGE_BOX_INBOX) {
            //mName = getString(R.string.via_without_time_for_send) + ": " + mName;
            date = String.format(getString(R.string.received_on), date);
        } else {
            //mName = getString(R.string.via_without_time_for_recieve) + ": " + mName;
            date = String.format(getString(R.string.sent_on), date);
        }
        //mRecipient.setText("xx");//do this later.
        mDate.setText(date);
        if (MmsApp.mGnMultiSimMessage == true) {
            formatSimStatus();
        } else {
            mByCard.setVisibility(View.GONE);
        }
        //mark this mms as readed.
        markMmsRead();
        //update notification
        MessagingNotification.nonBlockingUpdateNewMessageIndicator(this, false, false);
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

    private void markMmsRead() {
        new Thread(new Runnable() {
            public void run() {
                final ContentValues values = new ContentValues(2);
                values.put("read", 1);
                values.put("seen", 1);
                SqliteWrapper.update(getApplicationContext(), getContentResolver(), mMsgUri, values, null, null);
            }
        }).start();
    }

    private void checkSendFail() {
        long msgId = ContentUris.parseId(mMsgUri);
        Log.d(TAG, "mms id:" + msgId);
        Uri.Builder uriBuilder = PendingMessages.CONTENT_URI.buildUpon();
        uriBuilder.appendQueryParameter("protocol", "mms");
        uriBuilder.appendQueryParameter("message", String.valueOf(msgId));

        Cursor cursor = SqliteWrapper.query(this, getContentResolver(),
                uriBuilder.build(), null, null, null, null);
        if (cursor != null) {
            try {
                if ((cursor.getCount() == 1) && cursor.moveToFirst()) {
                    int errorType = cursor.getInt(cursor.getColumnIndexOrThrow(PendingMessages.ERROR_TYPE));
                    if (errorType == MmsSms.ERR_TYPE_GENERIC_PERMANENT) {
                        mShowResend = true;
                    } else {
                        mShowResend = false;
                    }
                } else {
                    Log.e(TAG, "query result not 1.!!");
                }
            } finally {
                cursor.close();
            }
        } else {
            Log.d(TAG, "query PendingMessages get null!");
        }
    }

    private void forwardMms() {
        SendReq sendReq = new SendReq();
        String subject = getString(R.string.forward_prefix);
        if (mSubject != null) {
            subject += mSubject;
        }
        sendReq.setSubject(new EncodedStringValue(subject));
        sendReq.setBody(mSlideshow.makeCopy(this));

        Uri uri = null;
        try {
            PduPersister persister = PduPersister.getPduPersister(this);
            // Copy the parts of the message here.
            uri = persister.persist(sendReq, Mms.Draft.CONTENT_URI);
        } catch (MmsException e) {
            Log.e(TAG, "Failed to copy message: " + mMsgUri, e);
            MmsApp.getToastHandler().sendEmptyMessage(MmsApp.MSG_MMS_CAN_NOT_SAVE);
            return;
        }
        
        Intent intent = new Intent();
        intent.putExtra("forwarded_message", true);
        intent.putExtra("msg_uri", uri);
        intent.putExtra("subject", subject);
        intent.setClassName(this, "com.android.mms.ui.ForwardMessageActivity");
        startActivity(intent);
    }

    private void addToContact() {
        //int count = mContactList.size();
        //switch(count) {
        //case 0:
        //    Log.e(TAG, "add contact, mCount == 0!");
        //    break;
        //case 1:
        if (mIndex != -1) {
            Intent intent = ConversationList.createAddContactIntent(mContactList.get(mIndex).getNumber());
            startActivity(intent);
        }
            //MessageUtils.addNumberOrEmailtoContact(mNumber, 0, this);
        //    break;
        //default:
            //MultiRecipientsActivity.setContactList(mContactList);
            //final Intent i = new Intent(getApplicationContext(), MultiRecipientsActivity.class);
            //startActivity(i);
        //    break;
        //}
    }

    public void onClickDetails(View v) {
        //show mms details info.
        String messageDetails = MessageUtils.getMmsDetail(this, mMsgUri, mSlideshow.getCurrentSlideshowSize(), mMsgBox);
        AuroraAlertDialog mDetailDialog = new AuroraAlertDialog.Builder(this)
                .setTitle(R.string.message_details_title)
                .setMessage(messageDetails)
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(true)
                .show();
    }

    private void showDeliveryReport() {
        Intent intent = new Intent(this, DeliveryReportActivity.class);
        intent.putExtra("message_id", mMsgId);
        intent.putExtra("message_type", "mms");
        startActivity(intent);
    }

    // We don't want to show the "call" option unless there is only one
    // recipient and it's a phone number.
    private boolean isRecipientCallable() {
        if (mContactList == null) {
            if (mNumber != null) {
                mContactList = ContactList.getByNumbers(mNumber, true, false);
            } else {
                Log.e(TAG, "isRecipientCallable(): no number with msgUri=" + mMsgUri);
                return false;
            }
        }
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
    //add for cmcc dir ui end
    
    /**
     * Looks to see if there are any valid parts of the attachment that can be copied to a SD card.
     * @param msgId
     */
    private boolean haveSomethingToCopyToSDCard(long msgId) {
        PduBody body = null;
        try {
            body = SlideshowModel.getPduBody(MmsPlayerActivity.this, mMsgUri);
        } catch (MmsException e) {
            
        }
        
        if (body == null) {
            return false;
        }

        boolean result = false;
        int partNum = body.getPartsNum();
        for(int i = 0; i < partNum; i++) {
            PduPart part = body.getPart(i);
            String type = new String(part.getContentType());

            if (ContentType.isImageType(type) || ContentType.isVideoType(type) ||
                    ContentType.isAudioType(type)) {
                result = true;
                break;
            }

            // add for vcard
            if (FileAttachmentModel.isSupportedFile(part)) {
                result = true;
                break;
            }
        }
        return result;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean succeeded = false;
        if (data != null && data.hasExtra("multi_save_result")) {
            succeeded = data.getBooleanExtra("multi_save_result", false);
            final int resId = succeeded ? R.string.copy_to_sdcard_success : R.string.Insert_sdcard;
            Toast.makeText(MmsPlayerActivity.this, resId, Toast.LENGTH_SHORT).show();
        }
        return;
    }

    public void onUpdate(Contact updated) {
        Log.d(TAG, "onUpdate,number and name:"+ updated.getNumber() + "," + updated.getName());
        if ((mContactList != null) && (updated.getNumber().equals(mContactList.get(0).getNumber()))) {
            updateRecipient();
        }
    }

    private void updateRecipient() {
        mContactList = ContactList.getByNumbers(mNumber, true, false);
        //update name
        if (mContactList.size() == 1) {
            mName = mContactList.get(0).getName();
        } else {
            mName = mContactList.getFirstName(null);
        }
        Log.d(TAG, "mName:" + mName);
        if (mMsgBox == Mms.MESSAGE_BOX_INBOX) {
            mName = getString(R.string.via_without_time_for_send) + ": " + mName;
        } else {
            mName = getString(R.string.via_without_time_for_recieve) + ": " + mName;
        }
        final String showString = mName;
        runOnUiThread(new Runnable() {
            public void run() {
                mRecipient.setText(showString);
            }
        });
    }

    //gionee wangym 2012-11-22 add for CR00735223 start
        @Override
    public boolean  dispatchTouchEvent(MotionEvent event){
        boolean ret = false;

//MTK_OP01_PROTECT_START  
        if(mIsCmcc && mScaleDetector != null){
            ret = mScaleDetector.onTouchEvent(event);
        }
//MTK_OP01_PROTECT_END
        
        if(!ret){
            ret = super.dispatchTouchEvent(event); 
        }
        
        return ret; 
    }
    
    
  //MTK_OP01_PROTECT_START
    private void changeTextSize(float size){        
        if(mListAdapter != null){
            mListAdapter.setTextSize(size);
            mListAdapter.notifyDataSetChanged();
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
            MessageUtils.setTextSize(MmsPlayerActivity.this, mTextSize);
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
                changeTextSize(size);
                mTextSize = size;
            }
            return true;
        }
    };
    //gionee wangym 2012-11-22 add for CR00735223 end
}
