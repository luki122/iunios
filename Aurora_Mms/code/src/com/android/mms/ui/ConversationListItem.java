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
// Aurora liugj 2013-09-13 added for aurora's new feature start
import org.xmlpull.v1.XmlPullParser;
// Aurora liugj 2013-09-13 added for aurora's new feature end
import com.android.mms.R;
import com.android.mms.data.CBMessage;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.LogTag;
import com.android.mms.util.SmileyParser;

import android.content.ContentResolver;
import android.content.Context;
// Aurora liugj 2013-09-13 added for aurora's new feature start
import android.content.res.ColorStateList;
// Aurora liugj 2013-09-13 added for aurora's new feature end
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract.Contacts;
import android.provider.Telephony.Mms;
import gionee.provider.GnTelephony.Threads;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
// Aurora liugj 2013-09-13 added for aurora's new feature start
import android.widget.FrameLayout;
// Aurora liugj 2013-09-13 added for aurora's new feature end
import android.widget.ImageView;
// Aurora liugj 2013-09-13 added for aurora's new feature start
import android.widget.LinearLayout;
// Aurora liugj 2013-09-13 added for aurora's new feature end
import android.widget.QuickContactBadge;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aurora.featureoption.FeatureOption;

//gionee gaoj 2012-3-22 added for CR00555790 start
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.SystemProperties;
import com.android.mms.MmsApp;
import android.graphics.Rect;
import android.widget.RelativeLayout.LayoutParams;
import com.android.mms.R.color;
//gionee gaoj 2012-3-22 added for CR00555790 end
//gionee zhouyj 2012-04-25 added for CR00581732 start
import android.widget.CheckBox;
// Aurora liugj 2013-09-13 deleted for aurora's new feature start
//import com.gionee.mms.ui.ConvFragment;
//import com.gionee.mms.ui.DraftFragment;
//import com.gionee.mms.ui.TabActivity;
// Aurora liugj 2013-09-13 deleted for aurora's new feature start
//gionee zhouyj 2012-04-25 added for CR00581732 end
// gionee zhouyj 2012-07-31 add for CR00662942 start 
import com.android.mms.util.GnSelectionManager;
// Aurora liugj 2013-09-13 deleted for aurora's new feature start
import com.aurora.mms.ui.ConvFragment;
// Aurora liugj 2013-09-13 deleted for aurora's new feature end
// gionee zhouyj 2012-07-31 add for CR00662942 end 
//Gionee <zhouyj> <2013-08-02> add for CR00845643 begin
import android.graphics.Paint.FontMetrics;
//Gionee <zhouyj> <2013-08-02> add for CR00845643 end
/**
 * This class manages the view for given conversation.
 */
// Aurora liugj 2013-09-13 modified for aurora's new feature start
// Aurora liugj 2013-10-23 modified for fix bug-134 
// Aurora liugj 2013-12-03 modified for checkbox animation 
public class ConversationListItem extends RelativeLayout implements Contact.UpdateListener {
// Aurora liugj 2013-09-13 modified for aurora's new feature end
    private static final String TAG = "ConversationListItem";
    private static final boolean DEBUG = false;
    // Aurora liugj 2013-09-13 added for aurora's new feature start 
    // Aurora liugj 2013-10-11 deleted for aurora's new feature start
//    private LinearLayout mDelView;
    // Aurora liugj 2013-10-11 deleted for aurora's new feature end
    // Aurora liugj 2013-09-13 added for aurora's new feature end
    private TextView mSubjectView;
    private TextView mFromView;
    public TextView mDateView;
    // Aurora liugj 2013-09-13 added for aurora's new feature start
    private TextView mDraftView;
    // Aurora liugj 2013-09-13 added for aurora's new feature end
    private View mAttachmentView;
    private View mErrorIndicator;
    // Aurora xuyong 2014-10-23 added for priacy feature start
    private View mPrivacyLock;
    // Aurora xuyong 2014-10-23 added for priacy feature end
    // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//    private ImageView mPresenceView;
//    private QuickContactBadge mAvatarView;
    // Aurora liugj 2013-09-13 deleted for aurora's new feature end
    //Gionee <gaoj> <2013-05-13> added for CR00811367 begin
    // gionee zhouyj 2012-04-25 added for CR00581732 start
    //public CheckBox mCheckBox;
    // gionee zhouyj 2012-04-25 added for CR00581732 end
    //Gionee <gaoj> <2013-05-13> added for CR00811367 end

    static private Drawable sDefaultContactImage;

    // For posting UI update Runnables from other threads:
    private Handler mHandler = new Handler();

    private Conversation mConversation;
    private Context mContext;
    // Aurora xuyong 2014-10-23 added for priacy feature start
    private boolean mShowPrivacy;
    // Aurora xuyong 2014-10-23 added for priacy feature end
    //gionee gaoj 2013-3-11 modified for CR00782858 start
    public static final StyleSpan STYLE_BOLD = new StyleSpan(Typeface.BOLD);
    //gionee gaoj 2013-3-11 modified for CR00782858 end
    //gionee gaoj 2012-3-22 added for CR00555790 start

    //private ImageView mGnErrorIndicator;
    static private Drawable sContactunknowImage;
    static final float UNREAD_MESSAGE_COUNT_SIZE = 18f;
    static final float UNREAD_MESSAGE_COUNT_SMALL_SIZE = 12f;
    static final int UNREAD_MESSAGE_COUNT_X = 18;
    static final int UNREAD_MESSAGE_COUNT_Y = 17;
    
    // gionee lwzh add for CR00774362 20130227 begin
    static private Bitmap mUnreadBitmap;
    // gionee lwzh add for CR00774362 20130227 end
    
     // Aurora liugj 2013-11-26 modified for aurora's new feature start
    //private ImageView mEncryptionImage;
     // Aurora liugj 2013-11-26 modified for aurora's new feature end

    // gionee zhouyj 2012-10-12 modify for CR00711214 start 
    private boolean mIsCheckBoxVisibility = false;
    // gionee zhouyj 2012-10-12 modify for CR00711214 start 

    private ConversationListItemData mConversationHeader;
    //gionee gaoj 2012-3-22 added for CR00555790 end
    
     // Aurora liugj 2013-11-14 added for aurora's new feature start
    private ColorStateList mFromColor;
    private ColorStateList mDateColor;
    private ColorStateList mSubjectColor1;
    private ColorStateList mSubjectColor2;
    private ColorStateList mDraftColor;
     // Aurora liugj 2013-11-14 added for aurora's new feature end
    
    //gionee gaoj 2013-2-19 adde for CR00771935 start
    private int mPosition;
    //gionee gaoj 2013-2-19 adde for CR00771935 end
    
    // gionee lwzh modify for CR00774362 20130227 begin
    static private int sLasThemeTag = 0;
    // gionee lwzh modify for CR00774362 20130227 end
    
    //Gionee <zhouyj> <2013-08-02> add for CR00845643 begin
    /*private ImageView mUnreadView = null;
    private int mUnreadCountColor = 0;
    private static Drawable sUnreadBgDrawable = null;
    private static Bitmap mUnreadBmp = null;;
    private int mUnreadTextSizeLarge = 0;
    private int mUnreadTextSizeMedium = 0;
    private int mUnreadTextSizeSmall = 0;*/
     //Gionee <zhouyj> <2013-08-02> add for CR00845643 end
    
    public ConversationListItem(Context context) {
        super(context);
        mContext = context;
        // Aurora liugj 2013-11-14 added for aurora's new feature start
        mFromColor = (ColorStateList) mContext.getResources().getColorStateList(R.color.aurora_list_text_from_color);  
        mDateColor = (ColorStateList) mContext.getResources().getColorStateList(R.color.aurora_list_text_date_color); 
        mSubjectColor1 = (ColorStateList) mContext.getResources().getColorStateList(R.color.aurora_list_text_unread_color);  
        mSubjectColor2 = (ColorStateList) mContext.getResources().getColorStateList(R.color.aurora_list_text_subject_color);  
        mDraftColor = (ColorStateList) mContext.getResources().getColorStateList(R.color.aurora_list_text_draft_color);  
        // Aurora liugj 2013-11-14 added for aurora's new feature end
        //Gionee <zhouyj> <2013-08-02> add for CR00845643 begin
        /*if (MmsApp.mGnUnreadIconChange) {
            BitmapDrawable unReadDrawable = (BitmapDrawable) getResources().getDrawable(
                    R.drawable.gn_unread_bg);
            mUnreadBmp = unReadDrawable.getBitmap();
            sUnreadBgDrawable = context.getResources().getDrawable(R.drawable.gn_unread_bg);
            // Aurora liugj 2013-09-13 modified for aurora's new feature start
                // Aurora liugj 2013-10-10 modified for aurora's new feature start
            mUnreadCountColor = mContext.getResources().getColor(com.aurora.R.color.aurora_warning_color);
                // Aurora liugj 2013-10-10 modified for aurora's new feature end
            // Aurora liugj 2013-09-13 modified for aurora's new feature end
            mUnreadTextSizeLarge = mContext.getResources().getDimensionPixelSize(R.dimen.gn_unread_textsize_lager);
            mUnreadTextSizeMedium = mContext.getResources().getDimensionPixelSize(R.dimen.gn_unread_textsize_medium);
            mUnreadTextSizeSmall = mContext.getResources().getDimensionPixelSize(R.dimen.gn_unread_textsize_small);
        }*/
        //Gionee <zhouyj> <2013-08-02> add for CR00845643 end
    }

    public ConversationListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
         // Aurora liugj 2013-11-14 added for aurora's new feature start
        mFromColor = (ColorStateList) mContext.getResources().getColorStateList(R.color.aurora_list_text_from_color);  
        mDateColor = (ColorStateList) mContext.getResources().getColorStateList(R.color.aurora_list_text_date_color); 
        mSubjectColor1 = (ColorStateList) mContext.getResources().getColorStateList(R.color.aurora_list_text_unread_color);  
        mSubjectColor2 = (ColorStateList) mContext.getResources().getColorStateList(R.color.aurora_list_text_subject_color);  
        mDraftColor = (ColorStateList) mContext.getResources().getColorStateList(R.color.aurora_list_text_draft_color);  
         // Aurora liugj 2013-11-14 added for aurora's new feature end
        // gionee lwzh modify for CR00774362 20130227 begin
        /*if (sDefaultContactImage == null || sLasThemeTag != MmsApp.sThemeChangTag) {
            //gionee gaoj 2012-6-27 added for CR00628364 start
            if (MmsApp.mGnMessageSupport) {
                if (MmsApp.mLightTheme) {
                    sContactunknowImage = context.getResources().getDrawable(R.drawable.gn_contact_default_unknow);                    
                    sDefaultContactImage = context.getResources().getDrawable(R.drawable.gn_ic_contact_picture);
                } else {
                    sDefaultContactImage = context.getResources().getDrawable(R.drawable.gn_ic_contact_picture_dark);
                   sContactunknowImage = context.getResources().getDrawable(R.drawable.gn_contact_default_unknow_dark);
                }
                
                sLasThemeTag = MmsApp.sThemeChangTag;
            } else {
                sDefaultContactImage = context.getResources().getDrawable(R.drawable.ic_contact_picture);
            }
            //gionee gaoj 2012-6-27 added for CR00628364 end
        } */
        // gionee lwzh modify for CR00774362 20130227 end
        //Gionee <zhouyj> <2013-08-02> add for CR00845643 begin
        /*if (MmsApp.mGnUnreadIconChange) {
            BitmapDrawable unReadDrawable = (BitmapDrawable) getResources().getDrawable(
                    R.drawable.gn_unread_bg);
            mUnreadBmp = unReadDrawable.getBitmap();
            sUnreadBgDrawable = context.getResources().getDrawable(R.drawable.gn_unread_bg);
            // Aurora liugj 2013-09-13 deleted for aurora's new feature start
                // Aurora liugj 2013-10-10 modified for aurora's new feature start
            mUnreadCountColor = mContext.getResources().getColor(com.aurora.R.color.aurora_warning_color);
                // Aurora liugj 2013-10-10 modified for aurora's new feature end
            // Aurora liugj 2013-09-13 deleted for aurora's new feature end
            mUnreadTextSizeLarge = mContext.getResources().getDimensionPixelSize(R.dimen.gn_unread_textsize_lager);
            mUnreadTextSizeMedium = mContext.getResources().getDimensionPixelSize(R.dimen.gn_unread_textsize_medium);
            mUnreadTextSizeSmall = mContext.getResources().getDimensionPixelSize(R.dimen.gn_unread_textsize_small);
        }*/
        //Gionee <zhouyj> <2013-08-02> add for CR00845643 end
    }
    // Aurora xuyong 2014-10-23 added for priacy feature start
    public void setPrivacy(boolean show) {
        mShowPrivacy = show && MmsApp.sHasPrivacyFeature;
    }
    // Aurora xuyong 2014-10-23 added for priacy feature end
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        
        mFromView = (TextView) findViewById(R.id.aurora_from);
        mSubjectView = (TextView) findViewById(R.id.aurora_subject);
        // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//        mPresenceView = (ImageView) findViewById(R.id.presence);
        // Aurora liugj 2013-09-13 deleted for aurora's new feature end
        mDateView = (TextView) findViewById(R.id.aurora_date);
        // Aurora liugj 2013-09-13 added for aurora's new feature start
        mDraftView = (TextView) findViewById(R.id.aurora_draft);
        // Aurora liugj 2013-09-13 added for aurora's new feature end
        mAttachmentView = findViewById(R.id.aurora_attachment);
        // Aurora xuyong 2014-10-23 added for priacy feature start
        mPrivacyLock = findViewById(R.id.aurora_lock_privacy);
        // Aurora xuyong 2014-10-23 added for priacy feature end
        mErrorIndicator = findViewById(R.id.aurora_error);
        // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//        mAvatarView = (QuickContactBadge) findViewById(R.id.avatar);
        // Aurora liugj 2013-09-13 deleted for aurora's new feature end
        //mCheckBox = (CheckBox) findViewById(R.id.aurora_conv_select_check_box);
        
        //gionee gaoj 2012-3-22 added for CR00555790 start
        //if (MmsApp.mGnMessageSupport) {
            // gionee zhouyj 2012-04-25 added for CR00581732 start
            // gionee zhouyj 2012-04-25 added for CR00581732 end
            //mGnErrorIndicator = (ImageView)findViewById(R.id.gn_error);
              // Aurora liugj 2013-11-26 modified for aurora's new feature start
           /* mEncryptionImage = (ImageView) findViewById(R.id.encryptionimage);
            if (MmsApp.mDarkStyle) {
                mEncryptionImage.setImageResource(R.drawable.gn_ic_encryptionimage_dark);
            }*/
              // Aurora liugj 2013-11-26 modified for aurora's new feature end
            //Gionee <zhouyj> <2013-08-02> add for CR00845643 begin
            // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//            mUnreadView = (ImageView) findViewById(R.id.gn_unread_view);
            // Aurora liugj 2013-09-13 deleted for aurora's new feature end
            //Gionee <zhouyj> <2013-08-02> add for CR00845643 end
        //}
        //gionee gaoj 2012-3-22 added for CR00555790 end
        // Aurora liugj 2013-09-13 added for aurora's new feature start
        // Aurora liugj 2013-10-11 deleted for aurora's new feature start
//        mDelView = (LinearLayout) findViewById(R.id.back);
//        mDelView.setOnClickListener(new OnClickListener() {
//            
//            @Override
//            public void onClick(View v) {
//                Log.e("liugj", "========onClick======="+mConversation.getThreadId());
//                ConvFragment.startQueryStaredMsg(mConversation.getThreadId());
//                
//            }
//        });
        // Aurora liugj 2013-10-11 deleted for aurora's new feature end
        // Aurora liugj 2013-09-13 added for aurora's new feature end
    }

    public Conversation getConversation() {
        return mConversation;
    }

    /**
     * Only used for header binding.
     */
    public void bind(String title, String explain) {
        mFromView.setText(title);
        mSubjectView.setText(explain);
    }

    private CharSequence formatMessage() {
        final int color = android.R.styleable.Theme_textColorSecondary;
        String from = null;
        if (mConversation.getType() == Threads.CELL_BROADCAST_THREAD) {
              // Aurora liugj 2014-01-10 modified for listItem optimize start
            String name = CBMessage.getCBChannelName(Integer.parseInt(mConversation.getRecipients().fromFormatNames(", ")));
            if (!TextUtils.isEmpty(name)) {
                from = name + "(" + Integer.parseInt(mConversation.getRecipients().fromFormatNames(", ")) + ")";
            } else {
                from = name;
            }
        } else {
            from = mConversation.getRecipients().fromFormatNames(", ");
              // Aurora liugj 2014-01-10 modified for listItem optimize end
        }

        if (TextUtils.isEmpty(from)){
            from = mContext.getString(android.R.string.unknownName);
        }

        SpannableStringBuilder buf = new SpannableStringBuilder(from);
        /*// Aurora liugj 2013-09-13 added for aurora's new feature start
        int before = buf.length();
        // Aurora liugj 2013-09-13 added for aurora's new feature end
        if (mConversation.getMessageCount() > 1) {
            // Aurora liugj 2013-09-13 deleted for aurora's new feature start
            //int before = buf.length();
            // Aurora liugj 2013-09-13 deleted for aurora's new feature end
            buf.append("  "+mConversation.getMessageCount());//mContext.getResources().getString(R.string.message_count_format,
            buf.setSpan(new ForegroundColorSpan(
                    mContext.getResources().getColor(R.color.message_count_color)),
                    before, buf.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        if (mConversation.hasDraft()) {
           // buf.append(mContext.getResources().getString(R.string.draft_separator));
            // Aurora liugj 2013-09-13 deleted for aurora's new feature start
            //int before = buf.length();
            // Aurora liugj 2013-09-13 deleted for aurora's new feature end
            int size;
            buf.append(",  "+mContext.getResources().getString(R.string.has_draft));
            size = android.R.style.TextAppearance_Small;
            buf.setSpan(new TextAppearanceSpan(mContext, size, color), before+1,
                    buf.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            buf.setSpan(new ForegroundColorSpan(
                    // Aurora liugj 2013-09-13 modified for aurora's new feature start
                    mContext.getResources().getColor(R.color.aurora_text_color_date)),
                    before+1, buf.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    // Aurora liugj 2013-09-13 modified for aurora's new feature end
        }

        // Unread messages are shown in bold
        if (mConversation.hasUnreadMessages()) {
            //Gionee <zhouyj> <2013-08-02> modify for CR00845643 begin
            if (MmsApp.mGnUnreadIconChange) {
                buf.setSpan(new ForegroundColorSpan(mUnreadCountColor), 0, buf.length(),
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            } else {
                buf.setSpan(STYLE_BOLD, 0, buf.length(),
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            //Gionee <zhouyj> <2013-08-02> modify for CR00845643 end
        }*/
        return buf;
    }


     //gionee gaoj 2012-3-22 added for CR00555790 start
    private CharSequence formatMessage(ConversationListItemData ch) {
        final int size = android.R.style.TextAppearance_Small;
        final int color = android.R.styleable.Theme_textColorSecondary;
        String from = ch.getFrom();

        //gionee gaoj 2013-4-1 modified for CR00788343 start
        if (ch.getMessageCount() == 0 && MmsApp.mIsDraftOpen) {
            //gionee gaoj 2013-4-1 modified for CR00788343 end
            if (TextUtils.isEmpty(from)) {
                from = mContext.getString(R.string.gn_draft_unknow_name);
            }
            SpannableStringBuilder buf = new SpannableStringBuilder(from);
            return buf;
        }
        
        if (TextUtils.isEmpty(from)){
            from = mContext.getString(android.R.string.unknownName);
        }
        SpannableStringBuilder buf = new SpannableStringBuilder(from);
        
        /*//gionee gaoj 2013-4-3 added for CR00788343 start
        if (!ch.hasDraft() && ch.getMessageCount() == 0) {
            return buf;
        }
        //gionee gaoj 2013-4-3 added for CR00788343 end
        // Aurora liugj 2013-09-13 added for aurora's new feature start
        int before = buf.length();
        // Aurora liugj 2013-09-13 added for aurora's new feature end
        if (ch.getType() == Threads.WAPPUSH_THREAD || (ch.hasDraft() && ch.getMessageCount() == 0)) {
            // only draft or WapPush
        } else {
            // gionee zhouyj 2012-05-29 modify for CR00601094 start
            if(MmsApp.mGnMessageSupport && ch.getMessageCount() == 1) {
                
            } else if (MmsApp.mGnMessageSupport){
                buf.append(" (");
                buf.append(ch.getMessageCount() + ") ");
            } else {
                if (ch.getUnreadMessageCount() > 0) {
                    buf.append(" (" + ch.getUnreadMessageCount() + "/");
                } else {
                    buf.append(" (" + "0/");
                }
                buf.append(ch.getMessageCount() + ") ");
            }
            // gionee zhouyj 2012-05-29 modify for CR00601094 end
        }

        // Aurora liugj 2013-09-13 deleted for aurora's new feature start
        //int before = buf.length();
        // Aurora liugj 2013-09-13 deleted for aurora's new feature end
        if (ch.hasDraft()) {
            buf.append(" ");
            buf.append(mContext.getResources().getString(R.string.has_draft));
            buf.setSpan(new TextAppearanceSpan(mContext, size, color), before,
                    buf.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            //gionee gaoj 2013-3-21 modified for CR00787217 start
            buf.setSpan(new ForegroundColorSpan(
                    // Aurora liugj 2013-09-13 modified for aurora's new feature start
                    mContext.getResources().getColor(R.color.aurora_text_color_date)),
                    before, buf.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    // Aurora liugj 2013-09-13 modified for aurora's new feature end
            //gionee gaoj 2013-3-21 modified for CR00787217 end
        }

        // Unread messages are shown in bold
        if (!ch.isRead()) {
            //Gionee <zhouyj> <2013-08-02> modify for CR00845643 begin
            if (MmsApp.mGnUnreadIconChange) {
                // Aurora liugj 2013-09-13 modified for aurora's new feature start
                buf.setSpan(new ForegroundColorSpan(mUnreadCountColor), before, buf.length(),
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                // Aurora liugj 2013-09-13 modified for aurora's new feature end
            } else {
                buf.setSpan(STYLE_BOLD, 0, buf.length(),
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            //Gionee <zhouyj> <2013-08-02> modify for CR00845643 end
        }*/
        return buf;
    }
    //gionee gaoj 2012-3-22 added for CR00555790 end
// Aurora liugj 2013-09-13 deleted for aurora's new feature start
    /*private void updateAvatarView() {    
        //gionee gaoj 2013-2-19 adde for CR00771935 start
        if (Contact.sFouceHideContactListPhoto) {
            mAvatarView.setVisibility(View.GONE);
            return;
        } 
        //gionee gaoj 2013-2-19 adde for CR00771935 end
        
        //gionee gaoj 2012-3-22 added for CR00555790 start
        ConversationListItemData ch = mConversationHeader;

        //gionee gaoj 2013-2-19 adde for CR00771935 start
        Contact.setContactPhotoViewTag(mAvatarView, 
                ch.getFrom(), mPosition, false);
        //gionee gaoj 2013-2-19 adde for CR00771935 end
        int unReadMessageCount = ch.getUnReadMessageCount();
        Drawable avatarDrawable = null;
        if (MmsApp.mGnMessageSupport) {
            if (ch.getContacts().size() == 1) {
                Contact contact = ch.getContacts().get(0);
                if (contact.existsInDatabase()) {
                    mAvatarView.assignContactUri(contact.getUri());
                    avatarDrawable = contact.getAvatar(mContext,
                            sDefaultContactImage, mAvatarView, false);
                } else {
                    //gionee gaoj 2012-12-11 added for CR00742048 start
                    avatarDrawable = contact.getDrawable(mContext,sContactunknowImage);
                    //gionee gaoj 2012-12-11 added for CR00742048 end
                    if (Mms.isEmailAddress(contact.getNumber())) {
                        mAvatarView.assignContactFromEmail(contact.getNumber(),
                                true);
                    } else {
                        if (Mms.isPhoneNumber(contact.getNumber()) || contact.getHotLine()) {
                            mAvatarView.assignContactFromPhone(
                                    contact.getNumber(), true);
                        } else {
                            mAvatarView.assignContactUri(null);
                        }
                    }
                }
                //Gionee <zhouyj> <2013-08-02> modify for CR00845643 begin
                if (unReadMessageCount != 0 && !MmsApp.mGnUnreadIconChange) {
                    avatarDrawable = generatorUnReadMessageCountIcon(
                            avatarDrawable, unReadMessageCount);
                }
                //Gionee <zhouyj> <2013-08-02> modify for CR00845643 end
            } else {
                if (ch.getContacts().size() > 1) {
                    if (MmsApp.mLightTheme) {
                        avatarDrawable = getResources().getDrawable(
                                R.drawable.gn_mms_group_icon);
                    } else {
                        avatarDrawable = getResources().getDrawable(
                                R.drawable.gn_mms_group_icon_dark);
                    }
                } else {
                    avatarDrawable = sContactunknowImage;
                }
                mAvatarView.assignContactUri(null);
            }
        } else {
        //gionee gaoj 2012-3-22 added for CR00555790 end
        if (mConversation.getRecipients().size() == 1) {
            Contact contact = mConversation.getRecipients().get(0);
            avatarDrawable = contact.getAvatar(mContext, sDefaultContactImage);

            String number = contact.getNumber();
            if (Mms.isEmailAddress(number)) {
                mAvatarView.assignContactFromEmail(number, true);
            } else {       
                if (contact.existsInDatabase()) {
                    mAvatarView.assignContactUri(contact.getUri());
                } else {
                    mAvatarView.assignContactFromPhone(number, true);
                }
            }
        } else {
            // TODO get a multiple recipients asset (or do something else)
            avatarDrawable = sDefaultContactImage;
            mAvatarView.assignContactUri(null);
        }
        //gionee gaoj 2012-3-22 added for CR00555790 start
        }
        //gionee gaoj 2012-3-22 added for CR00555790 end
        
        if (mAvatarView.getDrawable() != avatarDrawable || unReadMessageCount != 0) {
            mAvatarView.setImageDrawable(avatarDrawable);
        }
        
        mAvatarView.setVisibility(View.VISIBLE);
    }*/
// Aurora liugj 2013-09-13 deleted for aurora's new feature end
     //gionee gaoj 2012-3-22 added for CR00555790 start
    public ConversationListItemData getConversationHeader() {
        return mConversationHeader;
    }
    /**
     * @param icon given pic
     * @return pic with unRead message number
     */
    /*@SuppressWarnings("deprecation")
    private Drawable generatorUnReadMessageCountIcon(Drawable contactDrawble,int unReadMessageCount){
        //init canvas
        //gionee gaoj 2012-7-9 modified for CR00640407 start
        int iconSize = (int) getResources().getDimension(R.dimen.gn_app_size);
        //gionee gaoj 2012-7-9 modified for CR00640407 end
        Log.d(TAG, "the icon size is " + iconSize);
        Bitmap unReadMessage = Bitmap.createBitmap(iconSize, iconSize, Config.ARGB_8888);
        Canvas canvas = new Canvas(unReadMessage);
        Bitmap icon = ((BitmapDrawable) contactDrawble).getBitmap();

        // copy pic
        Paint iconPaint = new Paint();
        iconPaint.setDither(true);
        iconPaint.setFilterBitmap(true);
        //gionee gaoj 2012-7-9 modified for CR00640407 start
        int tob = (icon.getHeight() - icon.getWidth()) / 2;
        Rect src = new Rect(0, tob, icon.getWidth(), icon.getHeight());
        //gionee gaoj 2012-7-9 modified for CR00640407 end
        Rect dst = new Rect(0, 0, iconSize, iconSize);
        canvas.drawBitmap(icon, src, dst, iconPaint);

        // gionee lwzh modify for CR00774362 20130227 begin
        if (mUnreadBitmap == null) {
            BitmapDrawable unReadDrawable = (BitmapDrawable) getResources().getDrawable(
                  R.drawable.gn_ic_msg_unread_count);

            mUnreadBitmap = unReadDrawable.getBitmap();
        }
        // gionee lwzh modify for CR00774362 20130227 end
        
        //Gionee <zhouyj> <2013-06-13> modify for CR00825976 begin
        canvas.drawBitmap(mUnreadBitmap, iconSize - mUnreadBitmap.getWidth(), 0, iconPaint);
        //canvas.drawBitmap(mUnreadBitmap, 0, 0, iconPaint);
        //Gionee <zhouyj> <2013-06-13> modify for CR00825976 end

        Paint countPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
        //gionee gaoj 2013-3-21 modified for CR00787217 start
        countPaint.setColor(getResources().getColor(R.color.gn_unread_color_white));
        //gionee gaoj 2013-3-21 modified for CR00787217 end
        countPaint.setTextSize(UNREAD_MESSAGE_COUNT_SIZE);
        Boolean hdpiFlag = iconSize > 50 ? true : false;
        if (hdpiFlag) {
            countPaint.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            countPaint.setTypeface(Typeface.DEFAULT);
        }

        String strUnread;
        float textX = 0;
        float textY = mUnreadBitmap.getHeight()/2 + 5;
        if (unReadMessageCount < 10) {
            strUnread = String.valueOf(unReadMessageCount);
            //Gionee <zhouyj> <2013-06-13> modify for CR00825976 begin
            textX = iconSize - mUnreadBitmap.getWidth() / 2 - 5;
            //textX = mUnreadBitmap.getWidth() / 2 - 3;
            //Gionee <zhouyj> <2013-06-13> modify for CR00825976 end
        } else {
            // strUnread = String.valueOf(9) + "+";
            // textX = iconSize - mUnreadBitmap.getWidth() / 2 - 8;
            if (unReadMessageCount < 100) {
                strUnread = String.valueOf(unReadMessageCount);
            } else {
                strUnread = String.valueOf(99) + "+";
                countPaint.setTextSize(UNREAD_MESSAGE_COUNT_SMALL_SIZE);
            }
            //Gionee <zhouyj> <2013-06-13> modify for CR00825976 begin
            textX = iconSize - mUnreadBitmap.getWidth() / 2 - 10;
            //textX = mUnreadBitmap.getWidth() / 2 - 8;
            //Gionee <zhouyj> <2013-06-13> modify for CR00825976 end
        }
        canvas.drawText(strUnread, textX, textY, countPaint);

        return new BitmapDrawable(unReadMessage);
    }*/
    
    //Gionee <zhouyj> <2013-08-02> add for CR00845643 begin
    /*private Drawable drawUnreadCountIcon(Drawable unreadBg,int unReadMessageCount) {
        Bitmap unReadMessage = Bitmap.createBitmap(unreadBg.getIntrinsicWidth(),
                unreadBg.getIntrinsicHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(unReadMessage);

        Paint iconPaint = new Paint();
        iconPaint.setDither(true);
        iconPaint.setFilterBitmap(true);
        if (mUnreadBmp == null) {
            BitmapDrawable unReadDrawable = (BitmapDrawable) unreadBg;
            mUnreadBmp = unReadDrawable.getBitmap();
        }
        canvas.drawBitmap(mUnreadBmp, 0, 0, iconPaint);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG
                | Paint.DEV_KERN_TEXT_FLAG);
        paint.setColor(mUnreadCountColor);
        String strUnread = String.valueOf(unReadMessageCount);
        float textX = 0;
        float textY = 0;
        int height = unReadMessage.getHeight();
        float fontHeight = 0;
        if (unReadMessageCount < 10) {
            paint.setTextSize(mUnreadTextSizeLarge);
        } else {
            if (unReadMessageCount >= 100) {
                paint.setTextSize(mUnreadTextSizeSmall);
                strUnread = String.valueOf(99) + "+";
            } else {
                paint.setTextSize(mUnreadTextSizeMedium);
            }
        }
        FontMetrics fontMetrics = paint.getFontMetrics();
        fontHeight = paint.getFontMetrics().descent - paint.getFontMetrics().ascent;
        textX = (unReadMessage.getWidth() - paint.measureText(strUnread)) / 2;
        textY = height - (height - fontHeight) / 2 - fontMetrics.bottom;
        canvas.drawText(strUnread, textX, textY, paint);
        return new BitmapDrawable(getResources(), unReadMessage);
    }*/
    //Gionee <zhouyj> <2013-08-02> add for CR00845643 end
    
    private void setConversationHeader(ConversationListItemData header) {
        mConversationHeader = header;
    }
    // Aurora liugj 2013-09-13 deleted for aurora's new feature start
    /*public void setPresenceIcon(int iconId) {
        if (iconId == 0) {
            mPresenceView.setVisibility(View.GONE);
        } else {
            mPresenceView.setImageResource(iconId);
            mPresenceView.setVisibility(View.VISIBLE);
        }
    }*/
    // Aurora liugj 2013-09-13 deleted for aurora's new feature end
     //gionee gaoj 2012-3-22 added for CR00555790 end

    private void updateFromView() {    //may have bug
        //Gionee <gaoj> <2013-05-13> added for CR00811367 begin
        if (MmsApp.mGnPerfList) {
            mFromView.setText(formatMessage(mConversation));
            // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//            updateAvatarView(mConversation);
            // Aurora liugj 2013-09-13 deleted for aurora's new feature end
            return;
        }
        //Gionee <gaoj> <2013-05-13> added for CR00811367 end
        
        //gionee gaoj 2012-3-22 added for CR00555790 start
        if (MmsApp.mGnMessageSupport) {
            ConversationListItemData ch = mConversationHeader;
            ch.updateRecipients();
            mFromView.setText(formatMessage(ch));
        } else {
        //gionee gaoj 2012-3-22 added for CR00555790 end
        mFromView.setText(formatMessage());
        //gionee gaoj 2012-3-22 added for CR00555790 start
        }
         //gionee gaoj 2012-3-22 added for CR00555790 end
         // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//        updateAvatarView();
         // Aurora liugj 2013-09-13 deleted for aurora's new feature end
    }

    public void onUpdate(final Contact updated) {
        // Aurora yudingmin 2014-08-30 modified for optimize start
        // Aurora yudingmin 2014-09-29 modified for bug #8895 start
        mHandler.post(new Runnable() {
            public void run() {
                if(mConversation.getRecipients().contains(updated)){
                    updateFromView();
                }
            }
        });
        // Aurora yudingmin 2014-09-29 modified for bug #8895 end
        // Aurora yudingmin 2014-08-30 modified for optimize end
    }
    //gionee gaoj 2012-3-22 added for CR00555790 start
    public final void bind(Context context, final ConversationListItemData ch, final Conversation conversation,
            GnSelectionManager<Long> manager, int position) {
        //if (DEBUG) Log.v(TAG, "bind()");

        setConversationHeader(ch);

        //gionee gaoj 2012-9-20 added for CR00699291 start
        //mCheckBox.setVisibility(mIsCheckBoxVisibility ? VISIBLE : GONE);
        //gionee gaoj 2012-9-20 added for CR00699291 end
        
        // gionee lwzh modify for CR00774362 20130227 begin
        // gionee zhouyj 2012-08-22 modify for CR00678634 start
        /*if(manager != null && mIsCheckBoxVisibility) {
            setChecked(manager.isSelected(conversation.getThreadId()));
        }*/
        // gionee zhouyj 2012-08-22 modify for CR00678634 end

        // LayoutParams attachmentLayout = (LayoutParams)mAttachmentView.getLayoutParams();
        
        // When there's an error icon, the attachment icon is left of the error icon.
        // When there is not an error icon, the attachment icon is left of the date text.
        // As far as I know, there's no way to specify that relationship in xml.
        //if (hasError) {
        //    attachmentLayout.addRule(RelativeLayout.LEFT_OF, R.id.gn_error);
        //} else {
        //    attachmentLayout.addRule(RelativeLayout.LEFT_OF,R.id.right_panel);
        //}

        /*// Date
        mDateView.setVisibility(VISIBLE);
        mDateView.setText(ch.getDate());*/

        // From.
        /*mFromView.setVisibility(VISIBLE);
        mFromView.setText(formatMessage(ch));*/
        
          // Aurora liugj 2013-11-18 modified for aurora's new feature start
        updateUi(context, conversation, mIsCheckBoxVisibility);
          // Aurora liugj 2013-11-18 modified for aurora's new feature end
        // Register for updates in changes of any of the contacts in this conversation.
        //ContactList contacts = ch.getContacts();

        /*if (DEBUG) Log.v(TAG, "bind: contacts.addListeners " + this);
        Contact.addListener(this);*/
        //setPresenceIcon(contacts.getPresenceResId());

        // Subject
        /*mSubjectView.setText(ch.getSubject());
        if (MmsApp.mEncryption) {
            if (ch.hasEncryption() && !ConvFragment.isEncryptionList) {
                mSubjectView.setVisibility(GONE);
                mEncryptionImage.setVisibility(VISIBLE);
            } else {
                mSubjectView.setVisibility(VISIBLE);
                mEncryptionImage.setVisibility(GONE);
            }
        }*/
        // LayoutParams subjectLayout = (LayoutParams)mSubjectView.getLayoutParams();

        // Transmission error indicator.
        //mGnErrorIndicator.setVisibility((hasError && !mIsCheckBoxVisibility) ? VISIBLE : GONE);

        //wappush: modify the picture
        // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//        if (mGnErrorIndicator.getVisibility() == View.VISIBLE) {
//            if(true == FeatureOption.MTK_WAPPUSH_SUPPORT){
//                if(mConversationHeader.getType() == Threads.WAPPUSH_THREAD){
//                    mGnErrorIndicator.setImageResource(R.drawable.alert_wappush_si_expired);
//                }else{
//                    mGnErrorIndicator.setImageResource(R.drawable.gn_ic_list_alert_sms_failed);
//                }
//            }else{
//                mGnErrorIndicator.setImageResource(R.drawable.gn_ic_list_alert_sms_failed);
//            }
//        }
        // Aurora liugj 2013-09-13 deleted for aurora's new feature end

        //gionee gaoj 2013-2-19 adde for CR00771935 start
        mPosition = position;
        //gionee gaoj 2013-2-19 adde for CR00771935 end
        // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//        updateAvatarView();
        // Aurora liugj 2013-09-13 deleted for aurora's new feature end
        //Gionee <zhouyj> <2013-08-02> add for CR00845643 begin
        /*if (MmsApp.mGnUnreadIconChange) {
            if  (conversation.hasUnreadMessages()) {
                  // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//                mUnreadView.setVisibility(View.VISIBLE);
//                mUnreadView.setImageDrawable(drawUnreadCountIcon(sUnreadBgDrawable, conversation.getUnreadMessageCount()));
                  // Aurora liugj 2013-09-13 deleted for aurora's new feature end
                  // Aurora liugj 2013-09-13 added for aurora's new feature start
                // Aurora liugj 2013-10-10 modified for aurora's new feature start
                mSubjectView.setTextColor(mContext.getResources().getColor(com.aurora.R.color.aurora_title_color));
                // Aurora liugj 2013-10-10 modified for aurora's new feature end
                  // Aurora liugj 2013-09-13 added for aurora's new feature end
            } else {
                // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//                mUnreadView.setVisibility(View.INVISIBLE);
                // Aurora liugj 2013-09-13 deleted for aurora's new feature end
                // Aurora liugj 2013-09-13 added for aurora's new feature start
                XmlPullParser xrp = mContext.getResources().getXml(
                        R.color.aurora_list_text_subject_color);
                try {
                    ColorStateList csl = ColorStateList.createFromXml(
                            mContext.getResources(), xrp);
                    mSubjectView.setTextColor(csl);
                } catch (Exception e) {
                }
                // Aurora liugj 2013-09-13 added for aurora's new feature end
            }
        }*/
        //Gionee <zhouyj> <2013-08-02> add for CR00845643 end

        //LayoutParams fromLayout = (LayoutParams) mFromView.getLayoutParams();
        
        // gionee lwzh add for CR00706055 20121003 begin
        //if (hasAttachment && (ch.isGnDraft())) {
        // gionee lwzh add for CR00706055 20121003 end
        //    fromLayout.addRule(RelativeLayout.LEFT_OF, R.id.attachment);
        //} else if (hasError) {
        //    fromLayout.addRule(RelativeLayout.LEFT_OF, R.id.gn_error);
        //} else {
        //    fromLayout.addRule(RelativeLayout.LEFT_OF, R.id.right_panel);
        //}
        
        // gionee lwzh modify for CR00774362 20130227 begin
    }
    //gionee gaoj 2012-3-22 added for CR00555790 end
    
     // Aurora liugj 2013-11-18 modified for aurora's new feature start
    public final void bind(Context context, final Conversation conversation, boolean isChecked) {
     // Aurora liugj 2013-11-18 modified for aurora's new feature end
        //if (DEBUG) Log.v(TAG, "bind()");

        mConversation = conversation;
        
        /*int backgroundId;
        if (conversation.isChecked()) {
            backgroundId = R.drawable.list_selected_holo_light;
        } else if (conversation.hasUnreadMessages()) {
            backgroundId = R.drawable.conversation_item_background_unread;
            // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//            mPresenceView.setVisibility(View.VISIBLE);
            // Aurora liugj 2013-09-13 deleted for aurora's new feature end
        } else {
            backgroundId = R.drawable.conversation_item_background_read;
            // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//            mPresenceView.setVisibility(View.INVISIBLE);
            // Aurora liugj 2013-09-13 deleted for aurora's new feature end
        }
        Drawable background = mContext.getResources().getDrawable(backgroundId);

        setBackgroundDrawable(background);*/

//        LayoutParams attachmentLayout = (LayoutParams)mAttachmentView.getLayoutParams();
        
        // When there's an error icon, the attachment icon is left of the error icon.
        // When there is not an error icon, the attachment icon is left of the date text.
        // As far as I know, there's no way to specify that relationship in xml.
//        if (hasError) {
//            attachmentLayout.addRule(RelativeLayout.LEFT_OF, R.id.error);
//        } else {
//            attachmentLayout.addRule(RelativeLayout.LEFT_OF, R.id.date);
//        }
        
          // Aurora liugj 2013-11-18 modified for aurora's new feature start
        updateUi(context, conversation, isChecked);
            // Aurora liugj 2013-11-18 modified for aurora's new feature end
        
        /*boolean hasAttachment = conversation.hasAttachment();
        mAttachmentView.setVisibility(hasAttachment ? VISIBLE : GONE);

        // Date
        mDateView.setVisibility(VISIBLE);
          // Aurora liugj 2013-09-24 modified for aurora's new feature start
        mDateView.setText(MessageUtils.formatAuroraTimeStampString(context, conversation.getDate(), true));
          // Aurora liugj 2013-09-24 modified for aurora's new feature end
        // From.
        mFromView.setVisibility(VISIBLE);
        mFromView.setText(formatMessage());*/

        // Register for updates in changes of any of the contacts in this conversation.
        //ContactList contacts = conversation.getRecipients();

        /*if (DEBUG) Log.v(TAG, "bind: contacts.addListeners " + this);
        Contact.addListener(this);*/

        // Subject
//        SmileyParser parser = SmileyParser.getInstance();
//        mSubjectView.setText(parser.addSmileySpans(conversation.getSnippet()));

        /*mSubjectView.setVisibility(VISIBLE);
        mSubjectView.setText(conversation.getSnippet());*/
 //       LayoutParams subjectLayout = (LayoutParams)mSubjectView.getLayoutParams();
 //       // We have to make the subject left of whatever optional items are shown on the right.
 //       subjectLayout.addRule(RelativeLayout.LEFT_OF, hasAttachment ? R.id.attachment :
  //          (hasError ? R.id.error : R.id.date));

        // Transmission error indicator.
        //mErrorIndicator.setVisibility(hasError ? VISIBLE : GONE);
        // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//        updateAvatarView();
        // Aurora liugj 2013-09-13 deleted for aurora's new feature end
        //Gionee <zhouyj> <2013-08-02> add for CR00845643 begin
        /*if (MmsApp.mGnUnreadIconChange) {
            if  (conversation.hasUnreadMessages()) {
                // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//                mUnreadView.setVisibility(View.VISIBLE);
//                mUnreadView.setImageDrawable(drawUnreadCountIcon(sUnreadBgDrawable, conversation.getUnreadMessageCount()));
                // Aurora liugj 2013-09-13 deleted for aurora's new feature end
                // Aurora liugj 2013-09-13 added for aurora's new feature start
                // Aurora liugj 2013-10-10 modified for aurora's new feature start
                mSubjectView.setTextColor(mContext.getResources().getColor(com.aurora.R.color.aurora_title_color));
                // Aurora liugj 2013-10-10 modified for aurora's new feature end
                // Aurora liugj 2013-09-13 added for aurora's new feature end
            } else {
                // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//                mUnreadView.setVisibility(View.INVISIBLE);
                // Aurora liugj 2013-09-13 deleted for aurora's new feature end
                // Aurora liugj 2013-09-13 added for aurora's new feature start
                XmlPullParser xrp = mContext.getResources().getXml(
                        R.color.aurora_list_text_subject_color);
                try {
                    ColorStateList csl = ColorStateList.createFromXml(
                            mContext.getResources(), xrp);
                    mSubjectView.setTextColor(csl);
                } catch (Exception e) {
                }
                // Aurora liugj 2013-09-13 added for aurora's new feature end
            }
        }*/
        //Gionee <zhouyj> <2013-08-02> add for CR00845643 end
    }

    public final void unbind() {
        if (DEBUG) Log.v(TAG, "unbind: contacts.removeListeners " + this);
        // Unregister contact update callbacks.
        Contact.removeListener(this);
    }

     // Aurora liugj 2013-12-20 modified for list scroll optimize start
    public void bindDefault() {
        Log.d(TAG, "bindDefault().");
        // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//        mPresenceView.setVisibility(GONE);
        // Aurora liugj 2013-09-13 deleted for aurora's new feature end
        mFromView.setText(R.string.refreshing);
        mDateView.setVisibility(View.GONE);
        
        // Aurora liugj 2013-09-29 added for aurora's new feature start
        mDraftView.setVisibility(View.GONE);
        // Aurora liugj 2013-09-29 added for aurora's new feature start
        mSubjectView.setVisibility(GONE);
        // gionee lwzh modify for CR00774362 20130227 begin
        // gionee gaoj 2012-5-28 added for CR00608202 start
        //if (!MmsApp.mGnMessageSupport) {
            mErrorIndicator.setVisibility(GONE);
        //} else {
            //mGnErrorIndicator.setVisibility(GONE);
        //}
        // gionee gaoj 2012-5-28 added for CR00608202 end
        mAttachmentView.setVisibility(GONE);
        // Aurora xuyong 2014-10-23 added for priacy feature start
        mPrivacyLock.setVisibility(GONE);
        // Aurora xuyong 2014-10-23 added for priacy feature end
        // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//        if (mAvatarView.getDrawable() != sDefaultContactImage) {
//            mAvatarView.setImageDrawable(sDefaultContactImage);
//        }
        // Aurora liugj 2013-09-13 deleted for aurora's new feature end
        // gionee lwzh modify for CR00774362 20130227 end
        // gionee zhouyj 2012-04-25 added for CR00581732 start
        //if (MmsApp.mGnMessageSupport) {
            //mCheckBox.setVisibility(View.GONE);
        //}
        // gionee zhouyj 2012-04-25 added for CR00581732 end
    }
     // Aurora liugj 2013-12-20 modified for list scroll optimize end
    
    // gionee zhouyj 2012-04-25 added for CR00581732 start
    
    /*public void setChecked(boolean checked){
        if(mCheckBox.getVisibility() == View.VISIBLE) {
            mCheckBox.setChecked(checked);
        }
    }*/
    // gionee zhouyj 2012-04-25 added for CR00581732 end

    // gionee zhouyj 2012-10-12 modify for CR00711214 start 
    public void setCheckBoxVisibility(boolean value){
        mIsCheckBoxVisibility = value;
    }
    // gionee zhouyj 2012-10-12 modify for CR00711214 start 
    
    //gionee gaoj 2013-2-19 adde for CR00771935 start
    // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//    public QuickContactBadge getQuickContact() {
//        return mAvatarView;
//    }
    // Aurora liugj 2013-09-13 deleted for aurora's new feature end
    //gionee gaoj 2013-2-19 adde for CR00771935 end
    
    //Gionee <gaoj> <2013-05-13> added for CR00811367 begin
     // Aurora liugj 2013-11-18 modified for aurora's new feature start
    public final void bind(Context context, final Conversation conv, int position, boolean ischecked) {
     // Aurora liugj 2013-11-18 modified for aurora's new feature end

        mConversation = conv;
        /*boolean hasError = conv.hasError();
        boolean hasAttachment = conv.hasAttachment();*/

        // Date
        //mDateView.setVisibility(VISIBLE);
        //mDateView.setText(MessageUtils.newformatGNTime(context, conv.getDate()));
          // Aurora liugj 2013-09-24 modified for aurora's new feature start
        //mDateView.setText(MessageUtils.formatAuroraTimeStampString(context, conv.getDate(), true));
          // Aurora liugj 2013-09-24 modified for aurora's new feature end

        // From.
        /*mFromView.setVisibility(VISIBLE);
        mFromView.setText(formatMessage(conv));*/

        // Register for updates in changes of any of the contacts in this conversation.
        //ContactList contacts = ch.getContacts();

        /*if (DEBUG) Log.v(TAG, "bind: contacts.addListeners " + this);
        Contact.addListener(this);*/
        //setPresenceIcon(contacts.getPresenceResId());

        // Subject
        /*mSubjectView.setText(conv.getSnippet());
        if (MmsApp.mEncryption) {
            if (conv.getEncryption() && !ConvFragment.isEncryptionList) {
                mSubjectView.setVisibility(GONE);
                mEncryptionImage.setVisibility(VISIBLE);
            } else {
                mSubjectView.setVisibility(VISIBLE);
                mEncryptionImage.setVisibility(GONE);
            }
        }*/
        
          // Aurora liugj 2013-11-18 modified for aurora's new feature start
        updateUi(context, conv, ischecked);
          // Aurora liugj 2013-11-18 modified for aurora's new feature end
        // Transmission error indicator.
        //mGnErrorIndicator.setVisibility((hasError) ? VISIBLE : GONE);
         // Aurora liugj 2013-10-11 added for aurora's new feature start
         // mAttachmentView.setVisibility(hasAttachment ? VISIBLE : GONE);
        // Aurora liugj 2013-10-11 added for aurora's new feature end

        //wappush: modify the picture
        // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//        if (mGnErrorIndicator.getVisibility() == View.VISIBLE) {
//            if(true == FeatureOption.MTK_WAPPUSH_SUPPORT){
//                if(conv.getType() == Threads.WAPPUSH_THREAD){
//                    mGnErrorIndicator.setImageResource(R.drawable.alert_wappush_si_expired);
//                }else{
//                    mGnErrorIndicator.setImageResource(R.drawable.gn_ic_list_alert_sms_failed);
//                }
//            }else{
//                mGnErrorIndicator.setImageResource(R.drawable.gn_ic_list_alert_sms_failed);
//            }
//        }
        // Aurora liugj 2013-09-13 deleted for aurora's new feature end

        mPosition = position;
        // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//        updateAvatarView(conv);
        // Aurora liugj 2013-09-13 deleted for aurora's new feature end
        //Gionee <zhouyj> <2013-08-02> add for CR00845643 begin
        /*if (MmsApp.mGnUnreadIconChange) {
            if  (conv.hasUnreadMessages()) {
                // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//                mUnreadView.setVisibility(View.VISIBLE);
//                mUnreadView.setImageDrawable(drawUnreadCountIcon(sUnreadBgDrawable, conv.getUnreadMessageCount()));
                // Aurora liugj 2013-09-13 deleted for aurora's new feature end
                // Aurora liugj 2013-09-13 added for aurora's new feature start
                // Aurora liugj 2013-10-10 modified for aurora's new feature start
                mSubjectView.setTextColor(mContext.getResources().getColor(com.aurora.R.color.aurora_title_color));
                // Aurora liugj 2013-10-10 modified for aurora's new feature end
                // Aurora liugj 2013-09-13 added for aurora's new feature end
            } else {
                // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//                mUnreadView.setVisibility(View.INVISIBLE);
                // Aurora liugj 2013-09-13 deleted for aurora's new feature end
                // Aurora liugj 2013-09-13 added for aurora's new feature start
                XmlPullParser xrp = mContext.getResources().getXml(
                        R.color.aurora_list_text_subject_color);
                try {
                    ColorStateList csl = ColorStateList.createFromXml(
                            mContext.getResources(), xrp);
                    mSubjectView.setTextColor(csl);
                } catch (Exception e) {
                }
                // Aurora liugj 2013-09-13 added for aurora's new feature end
            }
        }*/
        //Gionee <zhouyj> <2013-08-02> add for CR00845643 end
    }
    //Gionee <gaoj> <2013-05-13> added for CR00811367 end
    
    //Gionee <gaoj> <2013-05-13> added for CR00811367 begin
    private String mFrom = null;
    // Aurora liugj 2013-09-13 deleted for aurora's new feature start
    /*private CharSequence formatMessage(Conversation conv) {
        System.out.println("======formatMessage=2======");
        final int size = android.R.style.TextAppearance_Small;
        final int color = android.R.styleable.Theme_textColorSecondary;
        mFrom = conv.getRecipients().formatNames(", ");

        if (TextUtils.isEmpty(mFrom)){
            mFrom = mContext.getString(android.R.string.unknownName);
        }
        SpannableStringBuilder buf = new SpannableStringBuilder(mFrom);
        
        if (!conv.hasDraft() && conv.getMessageCount() == 0) {
            return buf;
        }
        
        int before = buf.length();
        if ((conv.hasDraft() && conv.getMessageCount() == 0)) {
            // only draft or WapPush
        } else {
            if(conv.getMessageCount() == 1) {
                
            } else {
                buf.append(" (");
                buf.append(conv.getMessageCount() + ") ");
            }
        }

        if (conv.hasDraft()) {
            buf.append(" ");
            buf.append(mContext.getResources().getString(R.string.has_draft));
            buf.setSpan(new TextAppearanceSpan(mContext, size, color), before,
                    buf.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            buf.setSpan(new ForegroundColorSpan(
                        // Aurora liugj 2013-10-10 modified for aurora's new feature start
                    mContext.getResources().getColor(com.aurora.R.color.aurora_warning_color)),
                        // Aurora liugj 2013-10-10 modified for aurora's new feature end
                    before, buf.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        //Gionee <zhouyj> <2013-08-02> add for CR00845643 begin
        if (conv.hasUnreadMessages()) {
            if (MmsApp.mGnUnreadIconChange) {
                buf.setSpan(new ForegroundColorSpan(mUnreadCountColor), before, buf.length(),
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            } else {
                buf.setSpan(STYLE_BOLD, 0, buf.length(),
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            
        }
        //Gionee <zhouyj> <2013-08-02> add for CR00845643 end
        return buf;
    }*/
    //Gionee <gaoj> <2013-05-13> added for CR00811367 end
    // Aurora liugj 2013-09-13 deleted for aurora's new feature end
    
    private CharSequence formatMessage(Conversation conv) {
          // Aurora liugj 2014-01-10 modified for listItem optimize start
        mFrom = conv.getRecipients().fromFormatNames(", ");
          // Aurora liugj 2014-01-10 modified for listItem optimize end

        if (TextUtils.isEmpty(mFrom)){
            mFrom = mContext.getString(android.R.string.unknownName);
        }
        SpannableStringBuilder buf = new SpannableStringBuilder(mFrom);
        
        return buf;
    }
    // Aurora liugj 2013-09-13 deleted for aurora's new feature end
    //Gionee <gaoj> <2013-05-13> added for CR00811367 begin
    private Drawable sContactGroupLightImage = null;
    private Drawable sContactGroupDarkImage = null;
    // Aurora liugj 2013-09-13 deleted for aurora's new feature start
    /*private void updateAvatarView(Conversation conv) {
        if (Contact.sFouceHideContactListPhoto) {
            mAvatarView.setVisibility(View.GONE);
            return;
        } 

        Contact.setContactPhotoViewTag(mAvatarView, 
                mFrom, mPosition, false);
        int unReadMessageCount = conv.getUnreadMessageCount();
        Drawable avatarDrawable = null;

        if (conv.getRecipients().size() == 1) {
            Contact contact = conv.getRecipients().get(0);

            if (contact.existsInDatabase()) {
                mAvatarView.assignContactUri(contact.getUri());
                avatarDrawable = contact.getAvatar(mContext,
                        sDefaultContactImage, mAvatarView, false);
            } else {
                avatarDrawable = contact.getDrawable(mContext,sContactunknowImage);
                if (Mms.isEmailAddress(contact.getNumber())) {
                    mAvatarView.assignContactFromEmail(contact.getNumber(), true);
                } else {
                    if (Mms.isPhoneNumber(contact.getNumber()) || contact.getHotLine()) {
                        mAvatarView.assignContactFromPhone(
                                contact.getNumber(), true);
                    } else {
                        mAvatarView.assignContactUri(null);
                    }
                }
            }
            //Gionee <zhouyj> <2013-08-02> modify for CR00845643 begin
            if (unReadMessageCount != 0 && !MmsApp.mGnUnreadIconChange) {
                avatarDrawable = generatorUnReadMessageCountIcon(
                        avatarDrawable, unReadMessageCount);
            }
            //Gionee <zhouyj> <2013-08-02> modify for CR00845643 end
        } else {
            if (conv.getRecipients().size() > 1) {
                if (MmsApp.mLightTheme) {
                    if (sContactGroupLightImage == null) {
                        sContactGroupLightImage = getResources().getDrawable(
                                R.drawable.gn_mms_group_icon);
                    }
                    avatarDrawable = sContactGroupLightImage;
                } else {
                    if (sContactGroupDarkImage == null) {
                        sContactGroupDarkImage = getResources().getDrawable(
                                R.drawable.gn_mms_group_icon);
                    }
                    avatarDrawable = sContactGroupDarkImage;
                }
            } else {
                avatarDrawable = sContactunknowImage;
            }
            mAvatarView.assignContactUri(null);
        }

        if (mAvatarView.getDrawable() != avatarDrawable || unReadMessageCount != 0) {
            mAvatarView.setImageDrawable(avatarDrawable);
        }

        mAvatarView.setVisibility(View.VISIBLE);
    }*/
    // Aurora liugj 2013-09-13 deleted for aurora's new feature end
    //Gionee <gaoj> <2013-05-13> added for CR00811367 end

    //Gionee <gaoj> <2013-05-13> added for CR00811367 begin
     // Aurora liugj 2013-12-20 modified for list scroll optimize start
    public void GnbindDefault() {
        if (DEBUG) {
            Log.d(TAG, "GnbindDefault().");
        }
        mFromView.setText(R.string.refreshing);
        mDateView.setVisibility(View.GONE);
        mSubjectView.setVisibility(GONE);
        // Aurora liugj 2013-09-29 modified for aurora's new feature start
        mDraftView.setVisibility(View.GONE);
       
         // Aurora liugj 2013-11-02 modified for aurora's new feature start
        /*if (MmsApp.mGnMessageSupport) {*/
            mErrorIndicator.setVisibility(GONE);
        /*} else {
            mGnErrorIndicator.setVisibility(GONE);
        }*/
         // Aurora liugj 2013-11-02 modified for aurora's new feature end
        mAttachmentView.setVisibility(GONE);
        // Aurora xuyong 2014-10-23 added for priacy feature start
        mPrivacyLock.setVisibility(GONE);
        // Aurora xuyong 2014-10-23 added for priacy feature end
        //mCheckBox.setChecked(false);
        // Aurora liugj 2013-09-29 modified for aurora's new feature end
        // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//        if (mAvatarView.getDrawable() == null && !Contact.sFouceHideContactListPhoto) {
//            mAvatarView.setImageDrawable(sContactunknowImage);
//        }
        // Aurora liugj 2013-09-13 deleted for aurora's new feature end
    }
     // Aurora liugj 2013-12-20 modified for list scroll optimize end
    //Gionee <gaoj> <2013-05-13> added for CR00811367 emd
       
    // Aurora liugj 2013-10-23 added for fix bug-134 start 
    // Aurora liugj 2013-11-18 modified for aurora's new feature start
    // Aurora liugj 2013-12-20 modified for list scroll optimize start
    private void updateUi(Context context, Conversation conv, boolean isChecked) {
        
        mFromView.setVisibility(VISIBLE);
        mFromView.setText(formatMessage(conv));
        // Aurora xuyong 2014-04-01 added for bug #3827 start
        mDraftView.setVisibility(View.GONE);
        mSubjectView.setTextColor(mSubjectColor2);
        // Aurora xuyong 2014-04-01 added for bug #3827 end
        
        // Date
        mDateView.setVisibility(VISIBLE);
        mDateView.setText(MessageUtils.formatAuroraTimeStampString(context,
                conv.getDate(), true));
        
        if (isChecked) {
            mFromView.setTextColor(mContext.getResources().getColor(R.color.aurora_text_color_title)); 
            mDateView.setTextColor(mContext.getResources().getColor(R.color.aurora_text_color_date));
        }else {
            if (mFromColor != null) {
                mFromView.setTextColor(mFromColor);  
            }
            if (mDateColor != null) {
                mDateView.setTextColor(mDateColor);
            }
        }
        
        int count = conv.getUnreadMessageCount();

        boolean hasError = conv.hasError();

        if (conv.hasUnreadMessages() && count > 0) {
            // Aurora liugj 2013-10-29 added for aurora's new feature start
            /*XmlPullParser xrp = mContext.getResources().getXml(
                    R.color.aurora_list_text_unread_color);
            try {
                ColorStateList csl = ColorStateList.createFromXml(
                        mContext.getResources(), xrp);
                mSubjectView.setTextColor(csl);
            } catch (Exception e) {
            }*/
            // Aurora liugj 2013-11-14 modified for aurora's new feature start
            //ColorStateList csl = (ColorStateList) mContext.getResources().getColorStateList(R.color.aurora_list_text_unread_color);  
            if (mSubjectColor1 != null && !isChecked) {
                mSubjectView.setTextColor(mSubjectColor1);  
            } else {
                mSubjectView.setTextColor(mContext.getResources().getColor(com.aurora.R.color.aurora_title_color));
            }
            // Aurora liugj 2013-11-14 modified for aurora's new feature end
            // Aurora liugj 2013-10-29 added for aurora's new feature end
            if (count > 1) {
                // Aurora liugj 2013-09-24 modified for aurora's new feature end
                // Aurora liugj 2013-09-29 added for aurora's new feature start
                if (count > 99) {
                    count = 99;
                }
                // Aurora liugj 2013-09-29 added for aurora's new feature end
                StringBuffer sb = new StringBuffer();
                sb.append(" (");
                sb.append(count + ") ");
                mDraftView.setText(sb.toString());
                // Aurora liugj 2013-10-10 modified for aurora's new feature
                // start
                mDraftView.setTextColor(mContext.getResources().getColor(
                        com.aurora.R.color.aurora_warning_color));
                // Aurora liugj 2013-10-10 modified for aurora's new feature end
                mDraftView.setVisibility(View.VISIBLE);
            }
            mErrorIndicator.setVisibility(GONE);
        } else {
            // Aurora liugj 2013-10-29 added for aurora's new feature start
            /*XmlPullParser xrp = mContext.getResources().getXml(
                    R.color.aurora_list_text_subject_color);
            try {
                ColorStateList csl = ColorStateList.createFromXml(
                        mContext.getResources(), xrp);
                mSubjectView.setTextColor(csl);
            } catch (Exception e) {
            }*/
            // Aurora liugj 2013-11-14 modified for aurora's new feature start
            //ColorStateList csl = (ColorStateList) mContext.getResources().getColorStateList(R.color.aurora_list_text_subject_color);  
            if (mSubjectColor2 != null && !isChecked) {
                mSubjectView.setTextColor(mSubjectColor2);  
            }else {
                mSubjectView.setTextColor(mContext.getResources().getColor(R.color.aurora_text_color_subject));
            }
            // Aurora liugj 2013-11-14 modified for aurora's new feature end
            // Aurora liugj 2013-10-29 added for aurora's new feature end
            if (conv.hasDraft()) {
                // Aurora liugj 2013-10-29 added for aurora's new feature start
                /*xrp = mContext.getResources().getXml(
                        R.color.aurora_list_text_draft_color);
                try {
                    ColorStateList csl = ColorStateList.createFromXml(
                            mContext.getResources(), xrp);
                    mDraftView.setTextColor(csl);
                } catch (Exception e) {
                }*/
                // Aurora liugj 2013-11-14 modified for aurora's new feature start
                //csl = (ColorStateList) mContext.getResources().getColorStateList(R.color.aurora_list_text_draft_color);  
                if (mDraftColor != null && !isChecked) {
                    mDraftView.setTextColor(mDraftColor);
                } else {
                    mDraftView.setTextColor(mContext.getResources().getColor(R.color.aurora_text_color_draft));
                }
                // Aurora liugj 2013-11-14 modified for aurora's new feature end
                // Aurora liugj 2013-10-29 added for aurora's new feature end
                mDraftView.setText(R.string.has_draft);
                mDraftView.setVisibility(View.VISIBLE);
                mErrorIndicator.setVisibility(GONE);
            } else {
                mDraftView.setVisibility(View.GONE);
                // Aurora liugj 2013-11-02 modified for aurora's new feature start
                mErrorIndicator.setVisibility(hasError ? VISIBLE : GONE);
                // Aurora liugj 2013-11-02 modified for aurora's new feature end
            }
        }
        
        boolean hasAttachment = conv.hasAttachment();
        mAttachmentView.setVisibility(hasAttachment ? VISIBLE : GONE);
      // Aurora xuyong 2014-10-23 added for privacy feature start
        mPrivacyLock.setVisibility(conv.getPrivacy() > 0 && mShowPrivacy? VISIBLE : GONE);
        // Aurora xuyong 2014-10-23 added for privacy faeture end
        // Subject
        mSubjectView.setText(conv.getSnippet());
        if (!mSubjectView.isShown()) {
            mSubjectView.setVisibility(View.VISIBLE);
        }
        // Aurora liugj 2013-11-26 modified for aurora's new feature start
        /*if (MmsApp.mEncryption) {
            if (conv.getEncryption() && !ConvFragment.isEncryptionList) {
                mSubjectView.setVisibility(GONE);
                mEncryptionImage.setVisibility(VISIBLE);
            } else {
                mSubjectView.setVisibility(VISIBLE);
                mEncryptionImage.setVisibility(GONE);
            }
        }*/
        // Aurora liugj 2013-11-26 modified for aurora's new feature end

        if (DEBUG)
            Log.v(TAG, "bind: contacts.addListeners " + this);
        Contact.addListener(this);

        // wappush: modify the picture
        // Aurora liugj 2013-09-13 deleted for aurora's new feature start
        // if (mGnErrorIndicator.getVisibility() == View.VISIBLE) {
        // if(true == FeatureOption.MTK_WAPPUSH_SUPPORT){
        // if(conv.getType() == Threads.WAPPUSH_THREAD){
        // mGnErrorIndicator.setImageResource(R.drawable.alert_wappush_si_expired);
        // }else{
        // mGnErrorIndicator.setImageResource(R.drawable.gn_ic_list_alert_sms_failed);
        // }
        // }else{
        // mGnErrorIndicator.setImageResource(R.drawable.gn_ic_list_alert_sms_failed);
        // }
        // }
        // Aurora liugj 2013-09-13 deleted for aurora's new feature end

        // Aurora liugj 2013-09-13 deleted for aurora's new feature start
        // updateAvatarView(conv);
        // Aurora liugj 2013-09-13 deleted for aurora's new feature end
        // Gionee <zhouyj> <2013-08-02> add for CR00845643 begin
        /*if (MmsApp.mGnUnreadIconChange) {
        if (conv.hasUnreadMessages()) {
            // Aurora liugj 2013-09-13 deleted for aurora's new feature start
            // mUnreadView.setVisibility(View.VISIBLE);
            // mUnreadView.setImageDrawable(drawUnreadCountIcon(sUnreadBgDrawable,
            // conv.getUnreadMessageCount()));
            // Aurora liugj 2013-09-13 deleted for aurora's new feature end
            // Aurora liugj 2013-10-10 modified for aurora's new feature start
            mSubjectView.setTextColor(mContext.getResources().getColor(
                    com.aurora.R.color.aurora_title_color));
            // Aurora liugj 2013-10-10 modified for aurora's new feature end
        } else {
            // Aurora liugj 2013-09-13 deleted for aurora's new feature start
            // mUnreadView.setVisibility(View.INVISIBLE);
            // Aurora liugj 2013-09-13 deleted for aurora's new feature end
            // Aurora liugj 2013-09-13 added for aurora's new feature start
            XmlPullParser xrp = mContext.getResources().getXml(
                    R.color.aurora_list_text_subject_color);
            try {
                ColorStateList csl = ColorStateList.createFromXml(
                        mContext.getResources(), xrp);
                mSubjectView.setTextColor(csl);
            } catch (Exception e) {
            }
            // Aurora liugj 2013-09-13 added for aurora's new feature end
        }
        }*/
    }
    // Aurora liugj 2013-12-20 modified for list scroll optimize end
    // Aurora liugj 2013-11-18 modified for aurora's new feature end
    // Aurora liugj 2013-10-23 added for fix bug-134 end
}
