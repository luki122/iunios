/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import aurora.app.AuroraAlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Browser;
import android.provider.ContactsContract.Profile;
import com.android.mms.util.PhoneNumberUtils;
import android.text.Html;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.LineHeightSpan;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.URLSpan;
import android.text.style.AlignmentSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ArrayAdapter;
import aurora.widget.AuroraButton;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import gionee.provider.GnTelephony.SIMInfo;

import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.data.WorkingMessage;
import com.android.mms.transaction.Transaction;
import com.android.mms.transaction.TransactionBundle;
import com.android.mms.transaction.TransactionService;
import com.android.mms.util.DownloadManager;
import com.android.mms.util.SmileyParser;
import com.android.internal.telephony.Phone;
import android.text.style.ClickableSpan;
import com.aurora.featureoption.FeatureOption;

//gionee gaoj 2012-4-26 added for CR00555790 start
import android.os.SystemProperties;
import android.util.DisplayMetrics;
//gionee gaoj 2012-4-26 added for CR00555790 end
public class WPMessageListItem extends LinearLayout {
    public static final String EXTRA_URLS = "com.android.mms.ExtraUrls";

    private static final StyleSpan STYLE_BOLD = new StyleSpan(Typeface.BOLD);

    private View mItemContainer;
    private ImageView mLockedIndicator;
    private ImageView mExpirationIndicator;
    private ImageView mDetailsIndicator;
    private TextView mBodyTextView;
    private TextView mTimestamp;
    private TextView mSimStatus;
    private Handler mHandler;
    private WPMessageItem mMessageItem;
    private String mDefaultCountryIso;
    private TextView mDateView;
    public View mMessageBlock;
    private Path mPath = new Path();
    private Paint mPaint = new Paint();
    private QuickContactDivot mAvatar;
    private boolean mIsLastItemInList;
    static private Drawable sDefaultContactImage;
    //add for multi-delete
    // private CheckBox mSelectedBox;
    
    //gionee gaoj 2012-4-26 added for CR00555790 start
    private TextView mGnWpTime;
    private ImageView mGnSim;
    private static AuroraAlertDialog mAlertDialog = null;
    static private Drawable sContactunknowImage;
    //gionee gaoj 2012-4-26 added for CR00555790 end
    private int statusIconIndent = 0;
    private static final int DEFAULT_ICON_INDENT = 5;
    
    private static final String WP_TAG = "Mms/WapPush";

    public WPMessageListItem(Context context) {        
        super(context);
        mDefaultCountryIso = MmsApp.getApplication().getCurrentCountryIso();

       /* if (sDefaultContactImage == null) {
            //gionee gaoj 2012-6-27 added for CR00628364 start
            if (MmsApp.mGnMessageSupport) {
                if (MmsApp.mLightTheme) {
                    sDefaultContactImage = context.getResources().getDrawable(R.drawable.gn_ic_contact_picture);
                } else {
                    sDefaultContactImage = context.getResources().getDrawable(R.drawable.gn_ic_contact_picture_dark);
                }
            } else {
                sDefaultContactImage = context.getResources().getDrawable(R.drawable.ic_contact_picture);
            }
            //gionee gaoj 2012-6-27 added for CR00628364 end
        }*/

        //gionee gaoj 2012-4-25 added for CR00555790 start
//        if (MmsApp.mGnMessageSupport) {
//            if (sContactunknowImage == null) {
//                if (MmsApp.mLightTheme) {
//                    sContactunknowImage = context.getResources().getDrawable(R.drawable.gn_contact_default_unknow);
//                } else {
//                    sContactunknowImage = context.getResources().getDrawable(R.drawable.gn_contact_default_unknow_dark);
//                }
//            }
//        }
        //gionee gaoj 2012-4-25 added for CR00555790 end
    }

    public WPMessageListItem(Context context, AttributeSet attrs) {
        super(context, attrs);

        int color = mContext.getResources().getColor(R.color.timestamp_color);
        mColorSpan = new ForegroundColorSpan(color);
        mDefaultCountryIso = MmsApp.getApplication().getCurrentCountryIso();

        /*if (sDefaultContactImage == null) {
            //gionee gaoj 2012-6-27 added for CR00628364 start
            if (MmsApp.mGnMessageSupport) {
                if (MmsApp.mLightTheme) {
                    sDefaultContactImage = context.getResources().getDrawable(R.drawable.gn_ic_contact_picture);
                } else {
                    sDefaultContactImage = context.getResources().getDrawable(R.drawable.gn_ic_contact_picture_dark);
                }
            } else {
                sDefaultContactImage = context.getResources().getDrawable(R.drawable.ic_contact_picture);
            }
            //gionee gaoj 2012-6-27 added for CR00628364 end
        }*/

        //gionee gaoj 2012-4-25 added for CR00555790 start
/*        if (MmsApp.mGnMessageSupport) {
            if (sContactunknowImage == null) {
                if (MmsApp.mLightTheme) {
                    sContactunknowImage = context.getResources().getDrawable(R.drawable.gn_contact_default_unknow);
                } else {
                    sContactunknowImage = context.getResources().getDrawable(R.drawable.gn_contact_default_unknow_dark);
                }
            }
        }*/
           //gionee gaoj 2012-4-25 added for CR00555790 end
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mItemContainer =  findViewById(R.id.wpms_layout_view_parent);
//        mMessageBlock = findViewById(R.id.message_block);
        mAvatar = (QuickContactDivot) findViewById(R.id.avatar);
        mBodyTextView = (TextView) findViewById(R.id.text_view);
//        mTimestamp = (TextView) findViewById(R.id.date_view);
        mLockedIndicator = (ImageView) findViewById(R.id.locked_indicator);
        mExpirationIndicator = (ImageView) findViewById(R.id.expiration_indicator);
        mDetailsIndicator = (ImageView) findViewById(R.id.details_indicator);
//        mSimStatus = (TextView) findViewById(R.id.sim_status);
        //gionee gaoj 2012-4-26 added for CR00555790 start
        if(MmsApp.mGnMessageSupport) {
            mGnWpTime = (TextView) findViewById(R.id.gn_wp_msg_time);
            mGnSim = (ImageView) findViewById(R.id.gn_wp_msg_sim);
        } else {
            mMessageBlock = findViewById(R.id.message_block);
            mTimestamp = (TextView) findViewById(R.id.date_view);
            mSimStatus = (TextView) findViewById(R.id.sim_status);
        }
        //gionee gaoj 2012-4-26 added for CR00555790 end
        //add for multi-delete
        // mSelectedBox = (CheckBox)findViewById(R.id.select_check_box);

    }

    public void bind(WPMessageItem msgItem, boolean isLastItem) {
        mMessageItem = msgItem;
        mIsLastItemInList = isLastItem;

       //add for multi-delete
        /*if (isDeleteMode) {
            mSelectedBox.setVisibility(View.VISIBLE);
            mSelectedBox.setChecked(msgItem.isSelected());
        } else {
            mSelectedBox.setVisibility(View.GONE);
            
        }*/
        setLongClickable(false);

        bindCommonMessage(msgItem);
        
        mItemContainer.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {                
               onMessageListItemClick();
            }
        });
        
    }

    public void unbind() {
        // Clear all references to the message item, which can contain attachments and other
        // memory-intensive objects
        mMessageItem = null;
    }

    public WPMessageItem getMessageItem() {
        return mMessageItem;
    }
    
    public View getItemContainer(){
        return mItemContainer;
    }

    public void setMsgListItemHandler(Handler handler) {
        mHandler = handler;
    }
    // Aurora xuyong 2014-05-05 deleted for aurora's new feature start
    /*private void updateAvatarView(String addr, boolean isSelf) {
        Drawable avatarDrawable;
        if (isSelf || !TextUtils.isEmpty(addr)) {
            Contact contact = isSelf ? Contact.getMe(false) : Contact.get(addr, false);
            //gionee gaoj 2013-2-19 adde for CR00771935 start
            if (MmsApp.mGnMessageSupport) {
                if (contact.existsInDatabase()) {
                    avatarDrawable = contact.getAvatar(mContext,
                            sDefaultContactImage, mAvatar, false);
                } else {
                    avatarDrawable = contact.getAvatar(mContext,
                            sContactunknowImage, mAvatar, false);
                }
            } else {
            //gionee gaoj 2013-2-19 adde for CR00771935 end
            avatarDrawable = contact.getAvatar(mContext, sDefaultContactImage);
            //gionee gaoj 2013-2-19 adde for CR00771935 start
            }
            //gionee gaoj 2013-2-19 adde for CR00771935 end

            if (isSelf) {
                mAvatar.assignContactUri(Profile.CONTENT_URI);
            } else {
                if (contact.existsInDatabase()) {
                    mAvatar.assignContactUri(contact.getUri());
                } else {
                    mAvatar.assignContactFromPhone(contact.getNumber(), true);
                }
            }
        } else {
            avatarDrawable = sDefaultContactImage;
        }
        mAvatar.setImageDrawable(avatarDrawable);
    }*/
    // Aurora xuyong 2014-05-05 deleted for aurora's new feature end
    private void bindCommonMessage(final WPMessageItem msgItem) {
        // Since the message text should be concatenated with the sender's
        // address(or name), I have to display it here instead of
        // displaying it by the Presenter.
        mBodyTextView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

        String addr = msgItem.mAddress;
        //gionee gaoj 2012-5-9 added for CR00588933 start
        // Aurora xuyong 2014-05-05 deleted for aurora's new feature start
        /*if (MmsApp.mGnMessageSupport) {
            if (mIsLastItemInList) {
            updateAvatarView(msgItem.mAddress, false);
            } else {
                mAvatar.setVisibility(GONE);
            }
        } else {
            //gionee gaoj 2012-5-9 added for CR00588933 end
        updateAvatarView(msgItem.mAddress, false);
        //gionee gaoj 2012-5-9 added for CR00588933 start
        }*/
        // Aurora xuyong 2014-05-05 deleted for aurora's new feature end
        //gionee gaoj 2012-5-9 added for CR00588933 end

        // Get and/or lazily set the formatted message from/on the
        // MessageItem.  Because the MessageItem instances come from a
        // cache (currently of size ~50), the hit rate on avoiding the
        // expensive formatMessage() call is very high.
//        CharSequence formattedMessage = formatMessage(msgItem, msgItem.mContact, msgItem.mText, msgItem.mURL,
//                msgItem.mCreate, msgItem.mExpiration, msgItem.mTimestamp, msgItem.mHighlight);
        
        CharSequence formattedMessage = formatMessage(msgItem, msgItem.mContact, msgItem.mText, msgItem.mURL, msgItem.mTimestamp, msgItem.mExpiration, msgItem.mHighlight);

        //gionee gaoj 2012-4-26 added for CR00555790 start
        if (MmsApp.mGnMessageSupport) {
            DisplayMetrics dm = new DisplayMetrics();
            int screenWidth = MessageUtils.getScreenWidth(mContext, dm);
            int maxLength = (int)3 * screenWidth / 4;
            mBodyTextView.setMaxWidth(maxLength);
        }
        //gionee gaoj 2012-4-26 added for CR00555790 end
        mBodyTextView.setText(formattedMessage);
        mBodyTextView.setTextSize(mTextSize);
        
        CharSequence timestamp = formatTimestamp(msgItem,msgItem.mTimestamp);
        CharSequence simStatus = formatSimStatus(msgItem);
         
        //gionee gaoj 2012-4-26 added for CR00555790 start
        if(MmsApp.mGnMessageSupport) {
            mGnWpTime.setText(msgItem.mTimestamp);
            MessageUtils.setSimImageBg(mContext, msgItem.mSimId, mGnSim);
            drawRightStatusIndicator(msgItem);
        } else {
        //gionee gaoj 2012-4-26 added for CR00555790 end
        mTimestamp.setText(timestamp);
        mSimStatus.setText(simStatus);
        drawRightStatusIndicator(msgItem);
        
        //set padding to create space to draw statusIcon
        mTimestamp.setPadding(statusIconIndent, 0, 0, 0);
        //gionee gaoj 2012-4-26 added for CR00555790 start
        }
        //gionee gaoj 2012-4-26 added for CR00555790 end

        requestLayout();
    }
    


    private LineHeightSpan mSpan = new LineHeightSpan() {
        public void chooseHeight(CharSequence text, int start,
                int end, int spanstartv, int v, FontMetricsInt fm) {
            fm.ascent -= 10;
        }
    };

    TextAppearanceSpan mTextSmallSpan =
        new TextAppearanceSpan(mContext, android.R.style.TextAppearance_Small);

    ForegroundColorSpan mColorSpan = null;  // set in ctor

    
    private ClickableSpan mLinkSpan = new ClickableSpan(){
        public void onClick(View widget){
        }
    };
    
//    private CharSequence formatMessage(WPMessageItem msgItem, String contact, String text, String URL, String create,
//                                       String expiration, String timestamp, Pattern highlight) {
    private CharSequence formatMessage(WPMessageItem msgItem, String contact, String mText, String mURL,
                                         String timestamp, String expiration, Pattern highlight) {
        SpannableStringBuilder buf = new SpannableStringBuilder();

        if (!TextUtils.isEmpty(mText)) {
            // Converts html to spannable if ContentType is "text/html".
            // buf.append(Html.fromHtml(body));
            SmileyParser parser = SmileyParser.getInstance();
            buf.append(parser.addSmileySpans(mText));
            buf.append("\n");
        }
        /*
         * Fix the bug that *.inc will be not be treated as URL  
         */
        if(!TextUtils.isEmpty(mURL)){
            int urlStart = buf.length();
            buf.append(mURL);
            //new URLSpan(mURL);//it doesn't work
            buf.setSpan(mLinkSpan, urlStart, buf.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (highlight != null) {
            Matcher m = highlight.matcher(buf.toString());
            while (m.find()) {
                buf.setSpan(new StyleSpan(Typeface.BOLD), m.start(), m.end(), 0);
            }
        }
        
        return buf;
    }

    private CharSequence formatTimestamp(WPMessageItem msgItem,String timestamp){
 
        SpannableStringBuilder buf = new SpannableStringBuilder();       
        buf.append(TextUtils.isEmpty(timestamp) ? " " : timestamp);
        buf.setSpan(mSpan, 1, buf.length(), 0);
        
        //Add sim info
       /* int simInfoStart = buf.length();
        CharSequence simInfo = MessageUtils.getSimInfo(mContext, mMessageItem.mSimId);
        if(simInfo.length() > 0){
            buf.append(" ");
            buf.append(mContext.getString(R.string.via_without_time_for_recieve));
            simInfoStart = buf.length();
            buf.append(" ");
            buf.append(simInfo);
        }
        
        buf.setSpan(mTextSmallSpan, 0, buf.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);*/
        // Make the timestamp text not as dark
        buf.setSpan(mColorSpan, 0, buf.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        return buf;
    }

    private CharSequence formatSimStatus(WPMessageItem msgItem){
 
        SpannableStringBuilder buf = new SpannableStringBuilder();       
       /* buf.append(TextUtils.isEmpty(timestamp) ? " " : timestamp);
        buf.setSpan(mSpan, 1, buf.length(), 0);*/
        
        //Add sim info
        int simInfoStart = buf.length();
        CharSequence simInfo = MessageUtils.getSimInfo(mContext, mMessageItem.mSimId);
        if(simInfo.length() > 0){
            buf.append(" ");
            buf.append(mContext.getString(R.string.via_without_time_for_recieve));
            simInfoStart = buf.length();
            buf.append(" ");
            buf.append(simInfo);
        }
        
       // buf.setSpan(mTextSmallSpan, 0, buf.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Make the timestamp text not as dark
        buf.setSpan(mColorSpan, 0, simInfoStart, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        return buf;
    }

    private void drawRightStatusIndicator(WPMessageItem msgItem) {
        
        statusIconIndent = DEFAULT_ICON_INDENT;
        
        //Locked icon
        /*if (msgItem.mLocked) {
            mLockedIndicator.setImageResource(R.drawable.ic_lock_message_sms);
            mLockedIndicator.setVisibility(View.VISIBLE);
            statusIconIndent += mLockedIndicator.getDrawable().getIntrinsicWidth();
        } else {
            mLockedIndicator.setVisibility(View.GONE);
        }*/
        
        //Expiration icon
        if (1 == msgItem.isExpired) {
            //mExpirationIndicator.setImageResource(R.drawable.alert_wappush_si_expired);
            mExpirationIndicator.setVisibility(View.VISIBLE);
            statusIconIndent += mExpirationIndicator.getDrawable().getIntrinsicWidth();
        } else {
            mExpirationIndicator.setVisibility(View.GONE);
        }

    }  
    
    public void onMessageListItemClick() {
        //add for multi-delete
        /*if (mSelectedBox != null && mSelectedBox.getVisibility() == View.VISIBLE) {
            mSelectedBox.setChecked(!mSelectedBox.isChecked()); 
            if (null != mHandler) {
                Message msg = Message.obtain(mHandler, MessageListItem.ITEM_CLICK);
                msg.arg1 = (int) mMessageItem.mMsgId;
                msg.sendToTarget();
            }
            return;
        }*/
        // TODO Auto-generated method stub
        if(null == mMessageItem.mURL){
            return;
        }else{
            Log.i(WP_TAG, "WPMessageListItem: " + "mMessageItem.mURL is : " + mMessageItem.mURL);
            
            final java.util.ArrayList<String> urls = new ArrayList<String>();
            //add http manually if the URL does not contain this            
            urls.add(MessageUtils.CheckAndModifyUrl(mMessageItem.mURL));            

            //gionee gaoj 2012-4-26 added for CR00555790 start
            ArrayAdapter<String> adapter = null;
            if (MmsApp.mGnMessageSupport) {
                String[] httpArray = new String[]{mContext.getString(R.string.gn_visit),mContext.getString(R.string.gn_add_to_bookmarks)};
                //gionee gaoj 2012-5-14 modified for CR00596350 CR00640592 start
                if (MmsApp.mTransparent) {
                    adapter = new ArrayAdapter<String>(mContext, R.layout.gn_select_dialog_item, httpArray);
                } else {
                    adapter = new ArrayAdapter<String>(mContext, R.layout.gn_select_dialog_item_light_dark, httpArray);
                }
                //gionee gaoj 2012-5-14 modified for CR00596350 CR00640592 end
            } else {
            adapter =
                new ArrayAdapter<String>(mContext, android.R.layout.select_dialog_item, urls) {
                public View getView(int position, View convertView, ViewGroup parent) {
                    View v = super.getView(position, convertView, parent);
                    try {
                        String url = getItem(position).toString();
                        TextView tv = (TextView) v;
                        Drawable d = mContext.getPackageManager().getActivityIcon(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        if (d != null) {
                            d.setBounds(0, 0, d.getIntrinsicHeight(), d.getIntrinsicHeight());
                            tv.setCompoundDrawablePadding(10);
                            tv.setCompoundDrawables(d, null, null, null);
                        }
                        tv.setText(url);
                    } catch (android.content.pm.PackageManager.NameNotFoundException ex) {
                        // it's ok if we're unable to set the drawable for this view - the user
                        // can still use it
                    }
                    return v;
                }
            };
            }
            //gionee gaoj 2012-4-26 added for CR00555790 end

            AuroraAlertDialog.Builder b = new AuroraAlertDialog.Builder(mContext);

            DialogInterface.OnClickListener click = new DialogInterface.OnClickListener() {
                public final void onClick(DialogInterface dialog, int which) {
                    //gionee gaoj 2012-4-26 added for CR00555790 start
                    if (MmsApp.mGnMessageSupport) {
                        Intent intent = null;
                        switch (which) {
                            case 0:
                                Uri uri = Uri.parse(MessageUtils.CheckAndModifyUrl(mMessageItem.mURL));
                                intent = new Intent(Intent.ACTION_VIEW, uri);
                                intent.putExtra(Browser.EXTRA_APPLICATION_ID, mContext.getPackageName());
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                                mContext.startActivity(intent);
                                break;
                            case 1:
                                intent = new Intent();
                                intent.setAction("android.intent.action.INSERT");
                                intent.setType("vnd.android.cursor.dir/bookmark");
                                intent.putExtra("url", MessageUtils.CheckAndModifyUrl(mMessageItem.mURL));
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                                mContext.startActivity(intent);
                                break;
                            default:
                                break;
                        }
                        dialog.dismiss();
                    } else {
                    //gionee gaoj 2012-4-26 added for CR00555790 end
                    if (which >= 0) {
                        Uri uri = Uri.parse(urls.get(which));
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        intent.putExtra(Browser.EXTRA_APPLICATION_ID, mContext.getPackageName());
                        //MTK_OP01_PROTECT_START
                        /*
                        if (MmsApp.getApplication().isCdmaOperator()) {
                            intent.putExtra(Browser.APN_SELECTION, Browser.APN_MOBILE);
                        } */
                        //MTK_OP01_PROTECT_END
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                        try{
                            mContext.startActivity(intent);
                        }catch(ActivityNotFoundException ex){
                            //mContext.startActivity(new Intent(com.android.fallback.Fallback.class));
                            Toast.makeText(mContext, R.string.error_unsupported_scheme, Toast.LENGTH_LONG).show();
                            Log.e(WP_TAG,"Scheme " + uri.getScheme() + "is not supported!" );
                        }
                    }
                    //gionee gaoj 2012-4-26 added for CR00555790 start
                    }
                    //gionee gaoj 2012-4-26 added for CR00555790 end
                }
            };

            //gionee gaoj 2012-4-26 added for CR00555790 start
            if (MmsApp.mGnMessageSupport) {
                b.setTitle(mMessageItem.mURL);
            } else {
            //gionee gaoj 2012-4-26 added for CR00555790 end
            b.setTitle(R.string.select_link_title);
            //gionee gaoj 2012-4-26 added for CR00555790 start
            }
            //gionee gaoj 2012-4-26 added for CR00555790 end
            b.setCancelable(true);
            b.setAdapter(adapter, click);

//            b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//                public final void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                }
//            });

            //gionee gaoj 2012-4-26 added for CR00555790 start
            if (MmsApp.mGnMessageSupport) {
                mAlertDialog = b.show();
            } else {
            //gionee gaoj 2012-4-26 added for CR00555790 end
            b.show();
            //gionee gaoj 2012-4-26 added for CR00555790 start
            }
            //gionee gaoj 2012-4-26 added for CR00555790 end
        }
    }

    /**
     * Override dispatchDraw so that we can put our own background and border in.
     * This is all complexity to support a shared border from one item to the next.
     */
    @Override
    public void dispatchDraw(Canvas c) {
        View v = mMessageBlock;
        if (v != null) {
            float l = v.getX();
            float t = v.getY();
            float r = v.getX() + v.getWidth();
            float b = v.getY() + v.getHeight();

            Path path = mPath;
            path.reset();

            super.dispatchDraw(c);

            path.reset();

            r -= 1;

            // This block of code draws the border around the "message block" section
            // of the layout.  This would normally be a simple rectangle but we omit
            // the border at the point of the avatar's divot.  Also, the bottom is drawn
            // 1 pixel below our own bounds to get it to line up with the border of
            // the next item.
            //
            // But for the last item we draw the bottom in our own bounds -- so it will
            // show up.
            if (mIsLastItemInList) {
                b -= 1;
            }
            if (mAvatar.getPosition() == Divot.RIGHT_UPPER) {
                path.moveTo(l, t + mAvatar.getCloseOffset());
                path.lineTo(l, t);
                path.lineTo(r, t);
                path.lineTo(r, b);
                path.lineTo(l, b);
                path.lineTo(l, t + mAvatar.getFarOffset());
            } else if (mAvatar.getPosition() == Divot.LEFT_UPPER) {
                path.moveTo(r, t + mAvatar.getCloseOffset());
                path.lineTo(r, t);
                path.lineTo(l, t);
                path.lineTo(l, b);
                path.lineTo(r, b);
                path.lineTo(r, t + mAvatar.getFarOffset());
            }

            Paint paint = mPaint;
//            paint.setColor(0xff00ff00);
            paint.setColor(0xffcccccc);
            paint.setStrokeWidth(1F);
            paint.setStyle(Paint.Style.STROKE);
            c.drawPath(path, paint);
        } else {
            super.dispatchDraw(c);
        }
    }
    //gionee gaoj 2012-4-26 added for CR00555790 start
    public static void resetDialog() {
        if ((null != mAlertDialog) && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }
    //gionee gaoj 2012-4-26 added for CR00555790 end

    private final float DEFAULT_TEXT_SIZE = 20;
    private float mTextSize = DEFAULT_TEXT_SIZE;
    public void setTextSize(float size) {
        mTextSize = size;
    }
    //gionee wangym 2012-11-22 add for CR00735223 start
     public void setBodyTextSize(float size){
         mTextSize = size;
        if(mBodyTextView != null && mBodyTextView.getVisibility() == View.VISIBLE){
            mBodyTextView.setTextSize(size);
        }
    }
    //gionee wangym 2012-11-22 add for CR00735223 end

     //gionee gaoj 2013-2-19 adde for CR00771935 start
     public QuickContactBadge getQuickContact() {
         return mAvatar;
     }
     //gionee gaoj 2013-2-19 adde for CR00771935 end
}
