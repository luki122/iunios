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
import com.android.mms.data.CBMessage;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.LogTag;
import com.android.mms.util.SmileyParser;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.provider.Telephony.Threads;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.ImageView; // import com.gionee.widget.GnImageView;
import com.aurora.featureoption.FeatureOption;

/**
 * This class manages the view for given conversation.
 */
public class MmsPlayerActivityItem extends RelativeLayout {
    private static final String TAG = "ConversationListItem";
    private static final boolean DEBUG = false;
    private ImageView mImage;
    private ImageView mVideo;
    private View mAudio;
    private TextView mAudioName;
    private TextView mText; 

    // For posting UI update Runnables from other threads:
    private Handler mHandler = new Handler();

    private Conversation mConversation;

    private static final StyleSpan STYLE_BOLD = new StyleSpan(Typeface.BOLD);

    public MmsPlayerActivityItem(Context context) {
        super(context);
    }

    public MmsPlayerActivityItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        
        mImage    = (ImageView) findViewById(R.id.image);
        mVideo    = (ImageView) findViewById(R.id.video);
        
        mAudio = (View) findViewById(R.id.audio);
        mAudioName = (TextView) findViewById(R.id.audio_name);
        
        mText  = (TextView) findViewById(R.id.text);
        
        mImage.setVisibility(View.GONE);
        mVideo.setVisibility(View.GONE);
        
        mAudio.setVisibility(View.GONE);
        mAudioName.setVisibility(View.GONE);
        
        mText.setVisibility(View.GONE);
    }
}
