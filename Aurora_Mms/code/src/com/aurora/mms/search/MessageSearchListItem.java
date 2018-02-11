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

package com.aurora.mms.search;
// Aurora liugj 2013-09-13 added for aurora's new feature start
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
// Aurora liugj 2013-09-13 added for aurora's new feature end
import com.android.mms.R;
import com.android.mms.data.CBMessage;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.LogTag;
import com.android.mms.ui.MessageItem;
import com.android.mms.ui.MessageUtils;
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
import android.widget.CheckBox;

import com.android.mms.util.GnSelectionManager;
import com.aurora.mms.ui.ConvFragment;
import com.aurora.view.AuroraTextViewSnippet;

import android.graphics.Paint.FontMetrics;

/**
 * This class manages the view for given conversation.
 */
// Aurora liugj 2013-09-20 created for aurora's new feature 
// Aurora liugj 2013-09-29 modified for aurora's new feature start
public class MessageSearchListItem extends RelativeLayout {
// Aurora liugj 2013-09-29 modified for aurora's new feature end

    private static final String TAG = "ConversationSearchListItem";
    // Aurora xuyong 2014-01-23 modified for bug #11290 start
    private static final int INDEX_COLUMN_DATE = 4;
    private static final int INDEX_COLUMN_BODY = 3;
    private static final int INDEX_COLUMN_SUB = 5;
    private static final int INDEX_COLUMN_SUB_CS = 6;
    private static final int INDEX_COLUMN_MSG_TYPE = 7;
    // Aurora xuyong 2014-01-23 modified for bug #11290 end
    private static final boolean DEBUG = false;
    
    private AuroraTextViewSnippet mSubjectView;
    private AuroraTextViewSnippet mFromView;
    private TextView mDateView;

    static private Drawable sDefaultContactImage;

    // For posting UI update Runnables from other threads:
    private Handler mHandler = new Handler();

//    private Conversation mConversation;
    private MessageItem mMessageItem;
    private Context mContext;

    //private ImageView mEncryptionImage;
    
    private Cursor mCursor;
    private String mQeryString;
    
    public MessageSearchListItem(Context context) {
        super(context);
        mContext = context;
    }

    public MessageSearchListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }
    
    private String getQueryString() {
        // Aurora liugj 2013-11-06 modified for fix bug-417 start 
        if (mQeryString == null) {
            mQeryString = "";
        }
        // Aurora liugj 2013-11-06 modified for fix bug-417 end
        return mQeryString;
    }
    
    private void setQueryString(String queryString) {
        mQeryString = queryString;
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        
        mFromView = (AuroraTextViewSnippet) findViewById(R.id.search_from);
        mSubjectView = (AuroraTextViewSnippet) findViewById(R.id.search_subject);
        mDateView = (TextView) findViewById(R.id.search_date);

        //gionee gaoj 2012-3-22 added for CR00555790 start
        /*if (MmsApp.mGnMessageSupport) {
            mEncryptionImage = (ImageView) findViewById(R.id.encryptionimage);
            if (MmsApp.mDarkStyle) {
                mEncryptionImage.setImageResource(R.drawable.gn_ic_encryptionimage_dark);
            }
        }*/
        //gionee gaoj 2012-3-22 added for CR00555790 end
    }
   
    public MessageItem getMessageItem() {
        return mMessageItem;
    }
    
    public Cursor getCursor() {
        return mCursor;
    }
        
    public void bindDefault() {
        // Aurora liugj 2013-09-29 modified for aurora's new feature start
        mFromView.setText(mContext.getString(R.string.refreshing), "");
        // Aurora liugj 2013-09-29 modified for aurora's new feature end
        mDateView.setVisibility(View.GONE);
        mSubjectView.setVisibility(GONE);
    }
    
    // Aurora liugj 2013-09-29 modified for aurora's new feature start
     // Aurora liugj 2013-12-20 modified for list scroll optimize start
    public final void bind(Context context, Cursor cursor, String queryString, String name) {
        mCursor = cursor;
        mQeryString = queryString;
        
        // Aurora liugj 2013-09-25 modified for aurora's new feature start
//        final int smsIdPos    = cursor.getColumnIndex("_id");
//        final int threadIdPos = cursor.getColumnIndex("thread_id");
//        final int addressPos  = cursor.getColumnIndex("address");
        //final int bodyPos     = cursor.getColumnIndex("body");
        //final int datePos     = cursor.getColumnIndex("date");
        // Aurora liugj 2013-09-25 modified for aurora's new feature end

        // From.
        mFromView.setVisibility(VISIBLE);
        // Aurora liugj 2013-11-15 modified for bug-764 start
        int length = getQueryString().length();
        // Aurora liugj 2013-12-13 modified for NullPointerException start
        if (name == null) {
            mFromView.setText("", "");
        }else {
            if (name.length() < length) {
                mFromView.setText(name, "");
            }else {
                mFromView.setText(name, getQueryString());
            }
        }
        long date = cursor.getLong(INDEX_COLUMN_DATE);
        if (String.valueOf(date).length() == 10) {
            date *= 1000L;
        }
        mDateView.setVisibility(VISIBLE);
        // Aurora liugj 2013-09-24 modified for aurora's new feature start
        mDateView.setText(MessageUtils.formatAuroraTimeStampString(context, date, true));
        // Aurora liugj 2013-09-24 modified for aurora's new feature end
        mSubjectView.setVisibility(VISIBLE);
        String body = cursor.getString(INDEX_COLUMN_BODY);
        // Aurora xuyong 2014-01-23 modified for bug #11290 start
        int type = cursor.getInt(INDEX_COLUMN_MSG_TYPE);
        if (type == 1) {
            if (body == null || body.length() <= 0) {
                mSubjectView.setText("", "");
            }else {
                if (body.length() < length) {
                 // Aurora xuyong 2014-04-10 modified for bug #4061 start
                    if (queryString.contains("/%") || queryString.contains("//")) {
                        mSubjectView.setText(body, getQueryString());
                    } else {
                        mSubjectView.setText(body, "");
                    }
                 // Aurora xuyong 2014-04-10 modified for bug #4061 end
                }else {
                    mSubjectView.setText(body, getQueryString());
                }
            }
        } else if (type == 2) {
            String snippet = MessageUtils.extractEncStrFromCursor(cursor, INDEX_COLUMN_SUB, INDEX_COLUMN_SUB_CS);
            if (TextUtils.isEmpty(snippet)) {
                snippet = context.getString(R.string.no_subject_view);
            }
            mSubjectView.setText(snippet, "");
        }
        // Aurora xuyong 2014-01-23 modified for bug #11290 end
        // Aurora liugj 2013-12-13 modified for NullPointerException end
        // Aurora liugj 2013-11-15 modified for bug-764 end
    }
     // Aurora liugj 2013-12-20 modified for list scroll optimize end
    // Aurora liugj 2013-09-25 modified for aurora's new feature end   
    
    // Aurora liugj 2013-09-29 modified for aurora's new feature start
    public final void unbind() {
        
    }
    // Aurora liugj 2013-09-29 modified for aurora's new feature end
    
}
