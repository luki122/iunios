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

import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.layout.LayoutManager;

import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.ClipboardManager;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.util.AttributeSet;
import android.util.Config;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AbsoluteLayout;
import aurora.widget.AuroraButton;
import aurora.widget.AuroraEditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.ImageView; // import com.gionee.widget.GnImageView;
import java.io.IOException;
import java.util.Comparator;
import android.graphics.Color;
import android.text.style.URLSpan;
import android.text.util.Linkify;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import com.aurora.featureoption.FeatureOption;
import android.os.SystemProperties;
import android.database.sqlite.SqliteWrapper;
import android.provider.Browser;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.text.ClipboardManager;
import com.android.mms.ui.ComposeMessageActivity;

/**
 * A basic view to show the contents of a slide.
 */
public class SlideView extends AbsoluteLayout implements
        AdaptableSlideViewInterface {
    private static final String TAG = "SlideView";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;
    // FIXME: Need getHeight from mAudioInfoView instead of constant AUDIO_INFO_HEIGHT.
    private static final int AUDIO_INFO_HEIGHT = 82;

    private View mAudioInfoView;
    private ImageView mImageView;
    private VideoView mVideoView;
    private ScrollView mScrollText;
    private TextView mTextView;
    private OnSizeChangedListener mSizeChangedListener;
    private MediaPlayer mAudioPlayer;
    private boolean mIsPrepared;
    private boolean mStartWhenPrepared;
    private int     mSeekWhenPrepared;
    private boolean mStopWhenPrepared;
    private ScrollView mScrollViewPort;
    private LinearLayout mViewPort;
    // Indicates whether the view is in MMS conformance mode.
    private boolean mConformanceMode;
    private MediaController mMediaController;
    
    //MTK_OP01_PROTECT_START
    private View mTextSizeController;
    private ImageButton mZoomIn;
    private ImageButton mZoomOut;
    //MTK_OP01_PROTECT_END
     
    private ArrayList<String> mURLs = new ArrayList<String>();
    
    private AuroraActivity mActivity;
    private final int MENU_COPY_MESSAGE_TEXT         = 1;
    private final int MENU_ADD_TO_BOOKMARK           = 2;
    private final int MENU_ADD_TO_CONTACTS           = 3;
    private final int MENU_ADD_ADDRESS_TO_CONTACTS   = 4;
    private final int MENU_SEND_EMAIL                = 5;
    private final int MENU_CALL_BACK                 = 6;
    private final int MENU_SEND_SMS                  = 7;
//MTK_OP01_PROTECT_START
    private final int MENU_SELECT_TEXT               = 8;
//MTK_OP01_PROTECT_END

    public static final int REQUEST_CODE_ADD_CONTACT      = 1;
    private final int DEFAULT_TEXT_SIZE = 20;
    private final int TEXT_SIZE_CHANGE = 2;
    private final int MIN_TEXT_SIZE = 10;
    private final int MAX_TEXT_SIZE = 30;
    private int currentSize = DEFAULT_TEXT_SIZE;

    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            mIsPrepared = true;
            if (mSeekWhenPrepared > 0) {
                mAudioPlayer.seekTo(mSeekWhenPrepared);
                mSeekWhenPrepared = 0;
            }
            if (mStartWhenPrepared) {
                mAudioPlayer.start();
                mStartWhenPrepared = false;
                displayAudioInfo();
            }
            if (mStopWhenPrepared) {
                mAudioPlayer.stop();
                mAudioPlayer.release();
                mAudioPlayer = null;
                mStopWhenPrepared = false;
                hideAudioInfo();
            }
        }
    };

    public SlideView(Context context) {
        super(context);
    }

    public SlideView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setImage(String name, Bitmap bitmap) {
        if (mImageView == null) {
            mImageView = new ImageView(mContext);
            mImageView.setPadding(0, 5, 0, 5);
            addView(mImageView, new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0, 0));
            if (DEBUG) {
                mImageView.setBackgroundColor(0xFFFF0000);
            }
        }
        try {
           /* if (null == bitmap) {
                bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_missing_thumbnail_picture);
            }*/
            mImageView.setVisibility(View.VISIBLE);
            mImageView.setImageBitmap(bitmap);
        } catch (java.lang.OutOfMemoryError e) {
            Log.e(TAG, "setImage: out of memory: ", e);
        }
    }
  
    public void setImage(Uri imageUri) {
        if (mImageView == null) {
            mImageView = new ImageView(mContext);
            mImageView.setPadding(0, 1, 0, 0);
            mImageView.setBackgroundColor(0xFFFFFFFF);
            addView(mImageView, new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0, 0));
            if (DEBUG) {
                mImageView.setBackgroundColor(0xFFFF0000);
            }
        }
        mImageView.setVisibility(View.VISIBLE);
        try {
            if (imageUri == null) {
               Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                        -1/*R.drawable.ic_missing_thumbnail_picture*/);
               mImageView.setImageBitmap(bitmap);
               return;
            }
            
            mImageView.setImageURI(imageUri);
        } catch (java.lang.OutOfMemoryError e) {
            Log.e(TAG, "setImage: out of memory: ", e);
        }
    }
    
    public void setImageRegion(int left, int top, int width, int height) {
        // Ignore any requirement of layout change once we are in MMS conformance mode.
        if (mImageView != null && !mConformanceMode) {
            mImageView.setLayoutParams(new LayoutParams(width, height, left, top));
        }
    }

    public void setImageRegionFit(String fit) {
        // TODO Auto-generated method stub
    }

    public void setVideo(String name, Uri video) {
        // the name == null , the uri is a animation gif
        if (name == null) {
            setImage(video);
            return;
        }
        if (mVideoView == null) {
            mVideoView = new VideoView(mContext);
            mVideoView.setWillNotDraw(false);
            addView(mVideoView, new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0, 0));
            if (DEBUG) {
                mVideoView.setBackgroundColor(0xFFFF0000);
            }
        }

        if (LOCAL_LOGV) {
            Log.v(TAG, "Changing video source to " + video);
        }
        mVideoView.setVisibility(View.VISIBLE);
        mVideoView.setVideoURI(video);
        //MTK_OP01_PROTECT_START
        if (MmsApp.isTelecomOperator()) {
            // seek to first ms to got the thumbnail.
            mVideoView.seekTo(1);
        }
        //MTK_OP01_PROTECT_END
        
    }

    public void setMediaController(MediaController mediaController) {
        mMediaController = mediaController;
    }
    
    //MTK_OP01_PROTECT_START
    public void setTextSizeController(View textSizeController, ImageButton zoomIn, ImageButton zoomOut) {
        mTextSizeController = textSizeController;
        mZoomIn = zoomIn;
        mZoomOut = zoomOut;
    }
    //MTK_OP01_PROTECT_END
    
    private void initAudioInfoView(String name) {
        if (null == mAudioInfoView) {
            LayoutInflater factory = LayoutInflater.from(getContext());
            mAudioInfoView = factory.inflate(R.layout.playing_audio_info, null);
            int height = mAudioInfoView.getHeight();
            if (mConformanceMode) {
                mViewPort.addView(mAudioInfoView, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                        AUDIO_INFO_HEIGHT));
            } else {
                addView(mAudioInfoView, new LayoutParams(
                        LayoutParams.MATCH_PARENT, AUDIO_INFO_HEIGHT,
                        0, getHeight() - AUDIO_INFO_HEIGHT));
                if (DEBUG) {
                    mAudioInfoView.setBackgroundColor(0xFFFF0000);
                }
            }
        }
        TextView audioName = (TextView) mAudioInfoView.findViewById(R.id.name);
        audioName.setText(name);
        mAudioInfoView.setVisibility(View.GONE);
    }

    private void displayAudioInfo() {
        if (null != mAudioInfoView) {
            mAudioInfoView.setVisibility(View.VISIBLE);
        }
    }
    
    // gionee zhouyj 2013-03-20 add for CR00786065 start
    public void showAudioInfo() {
        displayAudioInfo();
    }
    // gionee zhouyj 2013-03-20 add for CR00786065 end

    private void hideAudioInfo() {
        if (null != mAudioInfoView) {
            mAudioInfoView.setVisibility(View.GONE);
        }
    }

    public void setAudio(Uri audio, String name, Map<String, ?> extras) {
        if (audio == null) {
            throw new IllegalArgumentException("Audio URI may not be null.");
        }

        if (LOCAL_LOGV) {
            Log.v(TAG, "Changing audio source to " + audio);
        }

        if (mAudioPlayer != null) {
            mAudioPlayer.reset();
            mAudioPlayer.release();
            mAudioPlayer = null;
        }

        // Reset state variables
        mIsPrepared = false;
        mStartWhenPrepared = false;
        mSeekWhenPrepared = 0;
        mStopWhenPrepared = false;

        try {
            mAudioPlayer = new MediaPlayer();
            mAudioPlayer.setOnPreparedListener(mPreparedListener);
            mAudioPlayer.setDataSource(mContext, audio);
            mAudioPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e(TAG, "Unexpected IOException.", e);
            mAudioPlayer.release();
            mAudioPlayer = null;
        }
        initAudioInfoView(name);
    }
    
    public void setActivity(AuroraActivity activity) {
        mActivity = activity;
    }
    
    private final OnCreateContextMenuListener mTextMenuCreateListener =
        new OnCreateContextMenuListener() {
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            TextMenuClickListener l = new TextMenuClickListener();
            menu.clear();
            menu.setHeaderTitle(R.string.message_options);
            menu.add(0, MENU_COPY_MESSAGE_TEXT, 0, R.string.copy_message_text)
                .setOnMenuItemClickListener(l);
//MTK_OP01_PROTECT_START
            // add for select text copy
            if (MmsApp.isTelecomOperator()) {
                menu.add(0, MENU_SELECT_TEXT, 0, R.string.select_text)
                        .setOnMenuItemClickListener(l);
            }
//MTK_OP01_PROTECT_END
            addCallAndContactMenuItems(menu, l, mTextView.getText());
        }
    };
    
    private final class TextMenuClickListener implements MenuItem.OnMenuItemClickListener {
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case MENU_COPY_MESSAGE_TEXT:
                    ClipboardManager clip =
                        (ClipboardManager)getContext().getSystemService(mContext.CLIPBOARD_SERVICE);
                    clip.setText(mTextView.getText()); 
                    return true;
                
                case MENU_ADD_TO_BOOKMARK:
                    if (mURLs.size() == 1) {
                        Browser.saveBookmark(mContext, null, mURLs.get(0));
                    } else if(mURLs.size() > 1) {
                        CharSequence[] items = new CharSequence[mURLs.size()];
                        for (int i = 0; i < mURLs.size(); i++) {
                            items[i] = mURLs.get(i);
                        }
                        new AuroraAlertDialog.Builder(mContext)
                            .setTitle(R.string.menu_add_to_bookmark)
                            //.setIcon(R.drawable.ic_dialog_menu_generic)
                            .setItems(items, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Browser.saveBookmark(mContext, null, mURLs.get(which));
                                    }
                                })
                            .show();
                    }
                    
                    return true;
                case MENU_ADD_ADDRESS_TO_CONTACTS: 
                    Intent mAddContactIntent = item.getIntent();
                    MessageUtils.addNumberOrEmailtoContact(mAddContactIntent.getStringExtra(ContactsContract.Intents.Insert.PHONE), 
                            REQUEST_CODE_ADD_CONTACT, mActivity);
                    return true;
//MTK_OP01_PROTECT_START
                // add for select text copy
                case MENU_SELECT_TEXT:
                    if (MmsApp.isTelecomOperator()) {
                        LayoutInflater factory = LayoutInflater.from(mContext);
                        final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);
                        AuroraEditText contentSelector = (AuroraEditText)textEntryView.findViewById(R.id.content_selector);
                        contentSelector.setText(mTextView.getText());
                        new AuroraAlertDialog.Builder(mContext)
                            .setTitle(R.string.select_text)
                            .setView(textEntryView)
                            .setPositiveButton(R.string.yes, null)
                            .show();
                    }
                    return true;
//MTK_OP01_PROTECT_END
            }
            return false;
        }
    };

    public void setText(String name, String text) {
        if (!mConformanceMode) {
            if (null == mScrollText) {
                mScrollText = new ScrollView(mContext);
                mScrollText.setScrollBarStyle(SCROLLBARS_OUTSIDE_OVERLAY);
                addView(mScrollText, new LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0, 0));
                if (DEBUG) {
                    mScrollText.setBackgroundColor(0xFF00FF00);
                }
            }
            if (null == mTextView) {
                mTextView = new TextView(mContext);
                mTextView.setTextSize(DEFAULT_TEXT_SIZE);
                //gionee gaoj 2012-6-27 added for CR00628364 start
//                mTextView.setTextColor(Color.BLACK);
                //gionee gaoj 2012-6-27 added for CR00628364 end
                mTextView.setAutoLinkMask(Linkify.ALL);
                mTextView.setLinksClickable(false);
                //MTK_OP01_PROTECT_START
                /*if (MmsApp.isTelecomOperator()) {
                    mTextView.setEnableShowUrlDialog(true);
                }*/
                //MTK_OP01_PROTECT_END 
                mTextView.setGravity(0x01);
                mTextView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                mScrollText.addView(mTextView);
            }
            mScrollText.requestFocus();
        }
        mTextView.setVisibility(View.VISIBLE);
        mTextView.setOnCreateContextMenuListener(mTextMenuCreateListener);
        mTextView.setAutoLinkMask(Linkify.ALL);
        mTextView.setLinksClickable(false);
        mTextView.setText(text);
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
                //Intent intent = ConversationList.createAddContactIntent(uriString);
                Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
                
                //gionee gaoj 2013-4-2 added for CR00792780 start
                intent.setComponent(new ComponentName("com.android.contacts",
                "com.android.contacts.activities.ContactEditorActivity"));
                //gionee gaoj 2013-4-2 added for CR00792780 end
                
                intent.putExtra(ContactsContract.Intents.Insert.PHONE, uriString);
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
    
    public void setTextRegion(int left, int top, int width, int height) {
        // Ignore any requirement of layout change once we are in MMS conformance mode.
        if (mScrollText != null && !mConformanceMode) {
            mScrollText.setLayoutParams(new LayoutParams(width, height, left, top));
        }
    }

    public void setVideoRegion(int left, int top, int width, int height) {
        if (mVideoView != null && !mConformanceMode) {
            mVideoView.setLayoutParams(new LayoutParams(width, height, left, top));
        }
    }

    public void setImageVisibility(boolean visible) {
        if (mImageView != null) {
            if (mConformanceMode) {
                mImageView.setVisibility(visible ? View.VISIBLE : View.GONE);
            } else {
                mImageView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            }
        }
    }

    public void setTextVisibility(boolean visible) {
        if (mConformanceMode) {
            if (mTextView != null) {
                mTextView.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        } else if (mScrollText != null) {
            mScrollText.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public void setVideoVisibility(boolean visible) {
        if (mVideoView != null) {
            if (mConformanceMode) {
                mVideoView.setVisibility(visible ? View.VISIBLE : View.GONE);
            } else {
                mVideoView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            }
        }
    }

    public void startAudio() {
        if ((mAudioPlayer != null) && mIsPrepared) {
            mAudioPlayer.start();
            mStartWhenPrepared = false;
            displayAudioInfo();
        } else {
            mStartWhenPrepared = true;
        }
    }

    public void stopAudio() {
        if ((mAudioPlayer != null) && mIsPrepared) {
            mAudioPlayer.stop();
            mAudioPlayer.release();
            mAudioPlayer = null;
            hideAudioInfo();
        } else {
            mStopWhenPrepared = true;
        }
    }

    public void pauseAudio() {
        if ((mAudioPlayer != null) && mIsPrepared) {
            if (mAudioPlayer.isPlaying()) {
                mAudioPlayer.pause();
            }
        }
        if(mAudioInfoView != null)
        {
            displayAudioInfo();
        }
        mStartWhenPrepared = false;
    }

    public void seekAudio(int seekTo) {
        if ((mAudioPlayer != null) && mIsPrepared) {
            mAudioPlayer.seekTo(seekTo);
        } else {
            mSeekWhenPrepared = seekTo;
        }
    }

    public void startVideo() {
        if (mVideoView != null) {
            if (LOCAL_LOGV) {
                Log.v(TAG, "Starting video playback.");
            }
            mVideoView.start();
        }
    }

    public void stopVideo() {
        if ((mVideoView != null)) {
            if (LOCAL_LOGV) {
                Log.v(TAG, "Stopping video playback.");
            }
            mVideoView.stopPlayback();
        }
    }

    public void pauseVideo() {
        if (mVideoView != null) {
            if (LOCAL_LOGV) {
                Log.v(TAG, "Pausing video playback.");
            }
            mVideoView.pause();
        }
    }

    public void seekVideo(int seekTo) {
        if (mVideoView != null) {
            if (seekTo > 0) {
                if (LOCAL_LOGV) {
                    Log.v(TAG, "Seeking video playback to " + seekTo);
                }
                mVideoView.seekTo(seekTo);
            }
        }
    }

    public void reset() {
        if (null != mScrollText) {
            mScrollText.setVisibility(View.GONE);
            mScrollText.scrollTo(0, 0);
        }

        if (null != mImageView) {
            mImageView.setVisibility(View.GONE);
        }

        if (null != mAudioPlayer) {
            stopAudio();
        }

        if (null != mVideoView) {
            stopVideo();
            mVideoView.setVisibility(View.GONE);
        }

        if (null != mTextView) {
            mTextView.setVisibility(View.GONE);
        }

        if (mScrollViewPort != null) {
            mScrollViewPort.scrollTo(0, 0);
            mScrollViewPort.setLayoutParams(
                    new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0, 0));
        }

    }

    public void setVisibility(boolean visible) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (mSizeChangedListener != null) {
            if (LOCAL_LOGV) {
                Log.v(TAG, "new size=" + w + "x" + h);
            }
            mSizeChangedListener.onSizeChanged(w, h - AUDIO_INFO_HEIGHT);
        }
    }

    public void setOnSizeChangedListener(OnSizeChangedListener l) {
        mSizeChangedListener = l;
    }

    private class Position {
        public Position(int left, int top) {
            mTop = top;
            mLeft = left;
        }
        public int mTop;
        public int mLeft;
    }

    /**
     * Makes the SlideView working on  MMSConformance Mode. The view will be
     * re-layout to the linear view.
     * <p>
     * This is Chinese requirement about mms conformance.
     * The most popular Mms service in China is newspaper which is MMS conformance,
     * normally it mixes the image and text and has a number of slides. The
     * AbsoluteLayout doesn't have good user experience for this kind of message,
     * for example,
     *
     * 1. AbsoluteLayout exactly follows the smil's layout which is not optimized,
     * and actually, no other MMS applications follow the smil's layout, they adjust
     * the layout according their screen size. MMS conformance doc also allows the
     * implementation to adjust the layout.
     *
     * 2. The TextView is fixed in the small area of screen, and other part of screen
     * is empty once there is no image in the current slide.
     *
     * 3. The TextView is scrollable in a small area of screen and the font size is
     * small which make the user experience bad.
     *
     * The better UI for the MMS conformance message could be putting the image/video
     * and text in a linear layout view and making them scrollable together.
     *
     * Another reason for only applying the LinearLayout to the MMS conformance message
     * is that the AbsoluteLayout has ability to play image and video in a same screen.
     * which shouldn't be broken.
     */
    public void enableMMSConformanceMode(int textLeft, int textTop,
            int imageLeft, int imageTop) {
        mConformanceMode = true;
        if (mScrollViewPort == null) {
            mScrollViewPort = new ScrollView(mContext) {
                private int mBottomY;
                @Override
                protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                    super.onLayout(changed, left, top, right, bottom);
                    if (getChildCount() > 0) {
                        int childHeight = getChildAt(0).getHeight();
                        int height = getHeight();
                        mBottomY = height < childHeight ? childHeight - height : 0;
                    }
                }
                @Override
                protected void onScrollChanged(int l, int t, int oldl, int oldt) {
                    // Shows MediaController when the view is scrolled to the top/bottom of itself.
                    if (t == 0 || t >= mBottomY){
                        if (mMediaController != null) {
                            mMediaController.show();
                        }
                    }
                }
            };
            mScrollViewPort.setScrollBarStyle(SCROLLBARS_OUTSIDE_OVERLAY);
            mViewPort = new LinearLayout(mContext);
            mViewPort.setOrientation(LinearLayout.VERTICAL);
            mViewPort.setGravity(Gravity.CENTER);
            mViewPort.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (mMediaController != null) {
                        mMediaController.show();
                    }
                }
            });
            mScrollViewPort.addView(mViewPort, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
            addView(mScrollViewPort);
        }
        // Layout views to fit the LinearLayout from left to right, then top to
        // bottom.
        TreeMap<Position, View> viewsByPosition = new TreeMap<Position, View>(new Comparator<Position>() {
            public int compare(Position p1, Position p2) {
                int l1 = p1.mLeft;
                int t1 = p1.mTop;
                int l2 = p2.mLeft;
                int t2 = p2.mTop;
                int res = t1 - t2;
                if (res == 0) {
                    res = l1 - l2;
                }
                if (res == 0) {
                    // A view will be lost if return 0.
                    return -1;
                }
                return res;
            }
        });
        if (textLeft >=0 && textTop >=0) {
            mTextView = new TextView(mContext);
            mTextView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            mTextView.setTextSize(DEFAULT_TEXT_SIZE);
            //gionee gaoj 2012-6-27 added for CR00628364 start
//            mTextView.setTextColor(Color.BLACK);
            //gionee gaoj 2012-6-27 added for CR00628364 end
            mTextView.setAutoLinkMask(Linkify.ALL);
            mTextView.setLinksClickable(false);
            /*if (MmsApp.isTelecomOperator()) {
                mTextView.setEnableShowUrlDialog(true);
            }*/
            //MTK_OP01_PROTECT_END 
            mTextView.setGravity(0x01);
            mTextView.setPadding(5, 5, 5, 5);
            viewsByPosition.put(new Position(textLeft, textTop), mTextView);
        }

        if (imageLeft >=0 && imageTop >=0) {
            mImageView = new ImageView(mContext);
            mImageView.setPadding(0, 1, 0, 0);
            mImageView.setBackgroundColor(0xFFFFFFFF);
            viewsByPosition.put(new Position(imageLeft, imageTop), mImageView);
            // According MMS Conformance Document, the image and video should use the same
            // region. So, put the VideoView below the ImageView.
            mVideoView = new VideoView(mContext);
            mVideoView.setWillNotDraw(false);
            viewsByPosition.put(new Position(imageLeft + 1, imageTop), mVideoView);
        }
        // the height of video view minus the status bar 
        final int  statusBarHeight = (int) (mContext.getResources().getDisplayMetrics().density * 25 + 4);
        for (View view : viewsByPosition.values()) {
            if (view instanceof VideoView) {
                mViewPort.addView(view, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutManager.getInstance().getLayoutParameters().getHeight() - statusBarHeight));
            } else {
                mViewPort.addView(view, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT));
            }
            view.setVisibility(View.GONE);
        }
    }

    public boolean hasText() {
        
        return (mTextView != null && mTextView.getText().length()>0) ? true:false;
    }
    
    //MTK_OP01_PROTECT_START
    public void changeTextZoom(Boolean zoomIn) {
        if (mTextView == null) {
            return;
        }
        if (zoomIn) {
            if (currentSize + TEXT_SIZE_CHANGE <= MAX_TEXT_SIZE) {
                currentSize = currentSize + TEXT_SIZE_CHANGE;
                Log.i(TAG, "zoom in, got currentSize is: " + currentSize);
                mTextView.setTextSize(currentSize);
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
                mTextView.setTextSize(currentSize);
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
    //MTK_OP01_PROTECT_END

    //gionee wangym 2012-11-22 add for CR00735223 start
    public void changeTextSize(float size) {
        if (mTextView != null) {
            mTextView.setTextSize(size);
        }
    }
    //gionee wangym 2012-11-22 add for CR00735223 start
   // Aurora xuyong 2014-04-25 added for bug #4301 start
    @Override
    public void setImage(String name, Uri uri) {
        // TODO Auto-generated method stub
        
    }
   // Aurora xuyong 2014-04-25 added for bug #4301 end
}
