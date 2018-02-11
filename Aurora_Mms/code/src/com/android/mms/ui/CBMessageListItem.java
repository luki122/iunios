/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
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

package com.android.mms.ui;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import aurora.app.AuroraAlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Browser;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.Sms;
import com.android.mms.util.PhoneNumberUtils;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.Layout.Alignment;
import android.text.method.HideReturnsTransformationMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.LineHeightSpan;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ArrayAdapter;
import aurora.widget.AuroraButton;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.data.WorkingMessage;
import com.android.mms.transaction.Transaction;
import com.android.mms.transaction.TransactionBundle;
import com.android.mms.transaction.TransactionService;
import com.android.mms.util.DownloadManager;
import com.android.mms.util.SmileyParser;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.pdu.PduHeaders;
//Aurora xuyong 2013-11-15 modified for google adapt end
import com.android.internal.telephony.Phone;
import com.aurora.featureoption.FeatureOption;


/**
 * This class provides view of a message in the messages list.
 */
public class CBMessageListItem extends LinearLayout {
    public static final String EXTRA_URLS = "com.android.mms.ExtraUrls";
    public static final int  UPDATE_CHANNEL   = 15;
    private static final String TAG = "CBMessageListItem";
    private static final StyleSpan STYLE_BOLD = new StyleSpan(Typeface.BOLD);

    public View mMessageBlock;
    private boolean mIsLastItemInList;
    private CBMessageItem mMessageItem;
    private Handler mHandler;
    private View mItemContainer;
    private View mMsgListItem;
    private TextView mBodyTextView;
    private TextView mSimStatus;
    private TextView mDateView;
    private Path mPath = new Path();
    private Paint mPaint = new Paint();
    private QuickContactDivot mAvatar;
    private static Drawable sDefaultContactImage;
    //add for multi-delete
    private CheckBox mSelectedBox;
    private static final int DEFAULT_ICON_INDENT = 5;
    private ImageView mLockedIndicator;
        
    public CBMessageListItem(Context context) {
        super(context);
        /*if (sDefaultContactImage == null) {
            sDefaultContactImage = context.getResources().getDrawable(R.drawable.ic_contact_picture);
        }*/
    }

    public CBMessageListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        int color = mContext.getResources().getColor(R.color.timestamp_color);
        mColorSpan = new ForegroundColorSpan(color);
        /*if (sDefaultContactImage == null) {
            sDefaultContactImage = context.getResources().getDrawable(R.drawable.ic_contact_picture);
        }*/
    }

    @Override
    protected void onFinishInflate() {
        Log.d("MMSLog", "CBMessageListItem.onFinishInflate()");
        super.onFinishInflate();
        mMsgListItem = findViewById(R.id.cbmsg_list_item_recv);
        mBodyTextView = (TextView) findViewById(R.id.text_view);
        mDateView = (TextView) findViewById(R.id.date_view);
        mItemContainer =  findViewById(R.id.mms_layout_view_parent);
        //mItemContainer.setLongClickable(true);
        mMessageBlock = findViewById(R.id.message_block);
        mLockedIndicator = (ImageView) findViewById(R.id.locked_indicator);
        mAvatar = (QuickContactDivot) findViewById(R.id.avatar);
        mSimStatus = (TextView) findViewById(R.id.sim_status);
        //add for multi-delete
        mSelectedBox = (CheckBox)findViewById(R.id.select_check_box);
        
    }

    /**
     * Override dispatchDraw so that we can put our own background and border in.
     * This is all complexity to support a shared border from one item to the next.
     */
    @Override
    public void dispatchDraw(Canvas c) {
        View v = mMessageBlock;
        int selectBoxWidth = 0;
        if (mSelectedBox != null && mSelectedBox.getVisibility() == View.VISIBLE) {
            selectBoxWidth = mSelectedBox.getWidth();
        }
        if (v != null) {
            float l = v.getX() + selectBoxWidth;
            float t = v.getY();
            float r = v.getX() + v.getWidth() + selectBoxWidth;
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
                if (selectBoxWidth > 0) {
                    path.lineTo(l - mAvatar.getWidth() - selectBoxWidth, t);
                }
                path.lineTo(r, t);
                path.lineTo(r, b);
                path.lineTo(l, b);
                path.lineTo(l, t + mAvatar.getFarOffset());
            } else if (mAvatar.getPosition() == Divot.LEFT_UPPER) {
                path.moveTo(r, t + mAvatar.getCloseOffset());
                path.lineTo(r, t);
                path.lineTo(l - selectBoxWidth, t);
                path.lineTo(l - selectBoxWidth, b);
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

    public void bind(CBMessageItem msgItem, boolean isLastItem, boolean isDeleteMode) {
        mMessageItem = msgItem;
        mIsLastItemInList = isLastItem;
        //add for multi-delete
        /*if (isDeleteMode) {
            mSelectedBox.setVisibility(View.VISIBLE);
            mSelectedBox.setChecked(msgItem.isSelected());
        } else {
            mSelectedBox.setVisibility(View.GONE);
            
        }*/
        setSelectedBackGroud(false);
        if (isDeleteMode) {
            mSelectedBox.setVisibility(View.VISIBLE);
            if (msgItem.isSelected()) {
                setSelectedBackGroud(true);
            }
        } else {
            mSelectedBox.setVisibility(View.GONE);
        }
        setLongClickable(false);
        mItemContainer.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {                
                onMessageListItemClick();
            }
        });
        bindCommonMessage(msgItem);
    }
    
    public void onMessageListItemClick() {
        if (mSelectedBox != null && mSelectedBox.getVisibility() == View.VISIBLE) {
            if (!mSelectedBox.isChecked()) {
                setSelectedBackGroud(true);
            } else {
                setSelectedBackGroud(false);
            }
            if (null != mHandler) {
                Message msg = Message.obtain(mHandler, MessageListItem.ITEM_CLICK);
                msg.arg1 = (int) mMessageItem.getMessageId();
                msg.sendToTarget();
            }
            return;
        }
    }
    
    public void unbind() {
        // do nothing 
    }

    public CBMessageItem getMessageItem() {
        return mMessageItem;
    }

    public void setMsgListItemHandler(Handler handler) {
        mHandler = handler;
    }

    private void bindCommonMessage(final CBMessageItem msgItem) {
        // Since the message text should be concatenated with the sender's
        // address(or name), I have to display it here instead of
        // displaying it by the Presenter.
        mBodyTextView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

        // Get and/or lazily set the formatted message from/on the
        // MessageItem.  Because the MessageItem instances come from a
        // cache (currently of size ~50), the hit rate on avoiding the
        // expensive formatMessage() call is very high.
        CharSequence formattedMessage = msgItem.getCachedFormattedMessage();
        if (formattedMessage == null) {
            formattedMessage = formatMessage(msgItem.getSubject(), msgItem.getDate(), null);
              msgItem.setCachedFormattedMessage(formattedMessage);
        }
        mBodyTextView.setText(formattedMessage);
        
        CharSequence formattedTimestamp = formatTimestamp(msgItem, msgItem.getDate());
        mDateView.setText(formattedTimestamp);

        CharSequence formattedSimStatus = formatSimStatus(msgItem);
        if (!TextUtils.isEmpty(formattedSimStatus)) {
            mSimStatus.setVisibility(View.VISIBLE);
            mSimStatus.setText(formattedSimStatus);
        } else {
            mSimStatus.setVisibility(View.GONE);
        }
        
        // update avatar
        mAvatar.setImageDrawable(sDefaultContactImage);
                
        //set padding to create space to draw statusIcon
        mDateView.setPadding(DEFAULT_ICON_INDENT, 0, 0, 0);
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

    AlignmentSpan.Standard mAlignRight = new AlignmentSpan.Standard(Alignment.ALIGN_OPPOSITE);
     
    ForegroundColorSpan mColorSpan = null;  // set in ctor

    
    private ClickableSpan mLinkSpan = new ClickableSpan(){
        public void onClick(View widget){
        }
    };
       
    private CharSequence formatMessage(String body, String timestamp, Pattern highlight) {
        SpannableStringBuilder buf = new SpannableStringBuilder();

        if (!TextUtils.isEmpty(body)) {
            // Converts html to spannable if ContentType is "text/html".
            // buf.append(Html.fromHtml(body));
            SmileyParser parser = SmileyParser.getInstance();
            buf.append(parser.addSmileySpans(body));
        }
    
        if (highlight != null) {
            Matcher m = highlight.matcher(buf.toString());
            while (m.find()) {
                buf.setSpan(new StyleSpan(Typeface.BOLD), m.start(), m.end(), 0);
            }
        }

        return buf;
    }

    private CharSequence formatTimestamp(CBMessageItem msgItem, String timestamp) {
        SpannableStringBuilder buf = new SpannableStringBuilder();
         buf.append(TextUtils.isEmpty(timestamp) ? " " : timestamp);        
         buf.setSpan(mSpan, 1, buf.length(), 0);
           
        //buf.setSpan(mTextSmallSpan, 0, buf.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Make the timestamp text not as dark
        buf.setSpan(mColorSpan, 0, buf.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return buf;
    }

    private CharSequence formatSimStatus(CBMessageItem msgItem ) {
        SpannableStringBuilder buffer = new SpannableStringBuilder();
        // If we're in the process of sending a message (i.e. pending), then we show a "Sending..."
        // string in place of the timestamp.
        //Add sim info
        int simInfoStart = buffer.length();
        CharSequence simInfo = MessageUtils.getSimInfo(mContext, msgItem.mSimId);
        if(simInfo.length() > 0){
            buffer.append(" ");
            buffer.append(mContext.getString(R.string.via_without_time_for_recieve));
            simInfoStart = buffer.length();
            buffer.append(" ");
            buffer.append(simInfo);
            buffer.append(" ");
        }

        //buffer.setSpan(mTextSmallSpan, 0, buffer.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Make the timestamp text not as dark
        buffer.setSpan(mColorSpan, 0, simInfoStart, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        return buffer;
    }

    public void setSelectedBackGroud(boolean selected) {
        if (selected) {
            mSelectedBox.setChecked(true);
            mSelectedBox.setBackgroundDrawable(null);
            mMessageBlock.setBackgroundDrawable(null);
            mDateView.setBackgroundDrawable(null);
            //setBackgroundResource(R.drawable.list_selected_holo_light);
        } else {
            setBackgroundDrawable(null);
            mSelectedBox.setChecked(false);
            mSelectedBox.setBackgroundResource(R.drawable.listitem_background);
            mMessageBlock.setBackgroundResource(R.drawable.listitem_background);
            mDateView.setBackgroundResource(R.drawable.listitem_background);
        }
    }
}
