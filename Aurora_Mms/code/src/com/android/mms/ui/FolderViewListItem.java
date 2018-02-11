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

import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.data.FolderView;
import com.android.mms.ui.FolderViewList;
import com.android.mms.util.RateController;
import com.android.mms.util.SmileyParser;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aurora.featureoption.FeatureOption;
import android.provider.Telephony.Threads;

/**
 * This class manages the view for given conversation.
 */
public class FolderViewListItem extends RelativeLayout implements Contact.UpdateListener{
    private static final String TAG = "FolderViewListItem";
    private static final boolean DEBUG = false;

    private TextView mSubjectView;
    private TextView mFromView;
    private TextView mDateView;
    private View mAttachmentView;
    private View mErrorIndicator;
    private ImageView mAvatarView;
    private ImageView mPresenceView;
    private Context mContext;

    // For posting UI update Runnables from other threads:
    private Handler mHandler = new Handler();

    private FolderView mfview;

    private static final StyleSpan STYLE_BOLD = new StyleSpan(Typeface.BOLD);

    public FolderViewListItem(Context context) {
        super(context);
        mContext = context;
    }

    public FolderViewListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mFromView = (TextView) findViewById(R.id.from);
        mSubjectView = (TextView) findViewById(R.id.subject);
        mPresenceView = (ImageView) findViewById(R.id.presence);
        mDateView = (TextView) findViewById(R.id.date);
        mAttachmentView = findViewById(R.id.attachment);
        mErrorIndicator = findViewById(R.id.error);
        mAvatarView = (ImageView) findViewById(R.id.avatar);
    }

//    public Conversation getConversation() {
//        return mConversation;
//    }

//    /**
//     * Only used for header binding.
//     */
//    public void bind(String title, String explain) {
//        mFromView.setText(title);
//        mSubjectView.setText(explain);
//    }

    private CharSequence formatMessage() {
        //ContactList recipients = mfview.getmRecipientString();
        //String from = "";
        //if (recipients != null && !recipients.isEmpty()) {
        //   for (Contact contact : recipients) {
        //        contact.reload(true);
        //    }
        //    from = recipients.formatNames(", ");
        //} else {
        //    from = mContext.getString(android.R.string.unknownName);
        //}
        String from = mfview.getmRecipientString().formatNames(", ");
        if (TextUtils.isEmpty(from)){
            from = mContext.getString(android.R.string.unknownName);
        }
        SpannableStringBuilder buf = new SpannableStringBuilder(from);
        // Unread messages are shown in bold
        if (mfview.getmRead()) {
            buf.setSpan(STYLE_BOLD, 0, buf.length(),
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return buf;
    }

    public final void bind(Context context, final FolderView fview) {
        //if (DEBUG) Log.v(TAG, "bind()");

        mfview = fview;

        //int backgroundId;
        if (mfview.getmRead()) {
            //backgroundId = R.drawable.conversation_item_background_unread;
            mPresenceView.setVisibility(View.VISIBLE);
        } else {
            //backgroundId = R.drawable.conversation_item_background_read;
            mPresenceView.setVisibility(View.INVISIBLE);
        }
        Drawable background = mContext.getResources().getDrawable(-1/*backgroundId*/);

        setBackgroundDrawable(background);
        
        /*if(mfview.getmType() == 1){
            mAvatarView.setImageResource(R.drawable.ic_sms);
        }
        else if(mfview.getmType() == 2){
            mAvatarView.setImageResource(R.drawable.ic_mms);
        }
        else if(mfview.getmType() == 3){
            mAvatarView.setImageResource(R.drawable.ic_wappush);
        }
        else if(mfview.getmType() == 4){
            mAvatarView.setImageResource(R.drawable.ic_cellbroadcast);
        }*/
            
        LayoutParams attachmentLayout = (LayoutParams)mAttachmentView.getLayoutParams();
        boolean hasError = mfview.hasError();
        // When there's an error icon, the attachment icon is left of the error icon.
        // When there is not an error icon, the attachment icon is left of the date text.
        // As far as I know, there's no way to specify that relationship in xml.
        if (hasError) {
            attachmentLayout.addRule(RelativeLayout.LEFT_OF, R.id.error);
        } else {
            attachmentLayout.addRule(RelativeLayout.LEFT_OF, R.id.date);
        }

        boolean hasAttachment = mfview.getmHasAttachment();
        mAttachmentView.setVisibility(hasAttachment ? VISIBLE : GONE);
        
        if(FolderViewList.mgViewID == FolderViewList.OPTION_OUTBOX && !hasError){
            mDateView.setText(R.string.sending_message);
        }else{
            // Date
            mDateView.setText(MessageUtils.formatTimeStampString(context, mfview.getmDate()));
        }
        // From.
        mFromView.setText(formatMessage());

        mSubjectView.setText(mfview.getmSubject());
        LayoutParams subjectLayout = (LayoutParams)mSubjectView.getLayoutParams();
        // We have to make the subject left of whatever optional items are shown on the right.
        subjectLayout.addRule(RelativeLayout.LEFT_OF, hasAttachment ? R.id.attachment :
            (hasError ? R.id.error : R.id.date));

        // Transmission error indicator.
        mErrorIndicator.setVisibility(hasError ? VISIBLE : GONE);

    }

    public final void unbind() {
        if (DEBUG) Log.v(TAG, "unbind: contacts.removeListeners " + this);
        // Unregister contact update callbacks.
        //Contact.removeListener(this);
    }

    @Override
    public void onUpdate(Contact updated) {
        mHandler.post(new Runnable() {
            public void run() {
                updateFromView();
            }
        });     
    }
    private void updateFromView() {
        mfview.getmRecipientString();
        mFromView.setText(formatMessage());
    }
}
