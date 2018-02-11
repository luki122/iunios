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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
// Aurora xuyong 2013-03-05 added for aurora's new feature start
import java.lang.ref.WeakReference;
// Aurora xuyong 2013-03-05 added for aurora's new feature end
import java.util.Map;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//Aurora xuyong 2013-10-11 added for aurora's new feature start
import android.text.format.DateFormat;
//Aurora xuyong 2013-10-11 added for aurora's new feature end

import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
// Aurora xuyong 2013-12-27 added for aurora's new feature start
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
// Aurora xuyong 2013-12-27 added for aurora's new feature end
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
// Aurora xuyong 2013-09-23 added for aurora's new feature start
import android.content.SharedPreferences;
// Aurora xuyong 2013-09-23 added for aurora's new feature end
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.drawable.Drawable;
import android.net.MailTo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Browser;
import android.provider.ContactsContract.Profile;
import android.provider.Telephony.Sms;
import com.android.mms.util.PhoneNumberUtils;
import android.text.Html;
import android.text.SpannableStringBuilder;
// Aurora xuyong 2014-09-15 added for aurora's new feature start
import android.text.Spanned;
// Aurora xuyong 2014-09-15 added for aurora's new feature end
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
// Aurora xuyong 2014-09-15 added for aurora's new feature start
import android.text.method.LinkMovementMethod;
// Aurora xuyong 2014-09-15 added for aurora's new feature end
import android.text.style.ForegroundColorSpan;
// Aurora xuyong 2014-05-26 added for multisim feature start
import android.text.style.ImageSpan;
// Aurora xuyong 2014-05-26 added for multisim feature end
import android.text.style.LineHeightSpan;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import aurora.widget.AuroraButton;
// Aurora xuyong 2013-09-23 added for aurora's new feature start
import aurora.preference.AuroraPreferenceManager;
// Aurora xuyong 2013-09-23 added for aurora's new feature end
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
// Aurora xuyong 2014-02-13 added for bug #1928 start
import android.widget.ListView;
// Aurora xuyong 2014-02-13 added for bug #1928 end
// Aurora xuyong 2013-11-11 added for aurora's new feature start
import android.widget.ProgressBar;
// Aurora xuyong 2013-11-11 added for aurora's new feature end
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;
// Aurora xuyong 2014-04-25 added for bug #4301 start
import com.android.mms.ContentRestrictionException;
import com.android.mms.LogTag;
// Aurora xuyong 2014-04-25 added for bug #4301 end
import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.data.WorkingMessage;
import com.android.mms.model.FileAttachmentModel;
import com.android.mms.transaction.Transaction;
import com.android.mms.transaction.TransactionBundle;
import com.android.mms.transaction.TransactionService;
// Aurora xuyong 2013-12-11 added for aurora's new feature start
import com.android.mms.ui.MessageItem.DeliveryStatus;
// Aurora xuyong 2013-12-11 added for aurora's new feature end
import com.android.mms.util.DownloadManager;
import com.android.mms.util.SmileyParser;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.ContentType;
import com.aurora.android.mms.pdu.PduHeaders;
//Aurora xuyong 2013-11-15 modified for google adapt end

//a0
import android.os.SystemProperties;
import gionee.provider.GnTelephony.SIMInfo;
import android.text.style.AlignmentSpan;
import android.text.style.LeadingMarginSpan;
// Aurora xuyong 2014-09-15 added for aurora's new feature start
import android.text.util.Linkify;
// Aurora xuyong 2014-09-15 added for aurora's new feature end
import android.text.Spannable;
import android.view.Gravity;
import android.widget.CheckBox;
// Aurora xuyong 2013-12-17 added for aurora's new feature start
import aurora.widget.AuroraCheckBox;
// Aurora xuyong 2013-12-17 added for aurora's new feature end
import android.widget.RelativeLayout;
import com.android.mms.MmsConfig;
//add for gemini
import android.database.Cursor;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.util.SqliteWrapper;
//Aurora xuyong 2013-11-15 modified for google adapt end
import android.content.ContentValues;
import com.aurora.featureoption.FeatureOption;
import android.util.Log;
import android.provider.Telephony.TextBasedSmsColumns;
import com.android.internal.telephony.TelephonyProperties;
//import android.drm.DrmManagerClient;
import gionee.drm.GnDrmManagerClient;
//a1

//gionee gaoj 2012-4-10 added for CR00555790 start
import android.view.View.OnLongClickListener;
import android.telephony.SmsManager;
import android.text.ClipboardManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.os.SystemProperties;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;
import android.content.ContentUris;
// Aurora xuyong 2014-09-27 added for aurora's new feature start
import android.content.res.ColorStateList;
// Aurora xuyong 2014-09-27 added for aurora's new feature end
// Aurora xuyong 2013-03-05 added for aurora's new feature start
// Aurora xuyong 2014-04-25 added for bug #4301 start
import com.android.mms.model.ImageModel;
// Aurora xuyong 2014-04-25 added for bug #4301 end
import com.android.mms.model.AudioModel;
// Aurora xuyong 2013-03-05 added for aurora's new feature end
import com.android.mms.model.MediaModel;
// Aurora xuyong 2013-03-05 added for aurora's new feature start
import com.android.mms.model.SlideModel;
// Aurora xuyong 2013-03-05 added for aurora's new feature end
import com.android.mms.model.SlideshowModel;
// Aurora xuyong 2013-03-05 added for aurora's new feature start
import com.android.mms.model.VideoModel;
// Aurora xuyong 2013-03-05 added for aurora's new feature end
//Aurora xuyong 2013-09-20 added for aurora's new feature start
// Aurora xuyong 2014-04-29 added for aurora's new feature start
import com.aurora.mms.ui.AuroraExpandableTextView;
import com.aurora.mms.ui.AuroraRoundImageView;
// Aurora xuyong 2014-04-29 added for aurora's new feature end
import com.aurora.mms.ui.ClickContent;
import com.aurora.mms.util.AuroraLinkMovementMethod;
import com.aurora.mms.util.Utils;
// Aurora xuyong 2014-11-07 added for bug #9526 start
import com.aurora.mms.util.AuroraMessageBodyView;
// Aurora xuyong 2014-11-07 added for bug #9526 end
// Aurora xuyong 2014-09-15 added for aurora's new feature start
import com.aurora.view.AuroraURLSpan;
import com.aurora.weather.data.WeatherInfo;
// Aurora xuyong 2015-04-23 added for aurora's new feature start
import com.aurora.weather.util.AuroraMsgWeatherUtils;
// Aurora xuyong 2015-04-23 added for aurora's new feature end
// Aurora xuyong 2014-09-15 added for aurora's new feature end
//Aurora xuyong 2013-09-20 added for aurora's new feature end
import com.gionee.mms.ui.SlidesBrowserItemView;
import android.view.ViewStub;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.view.WindowManagerPolicy.WindowState;
import android.webkit.MimeTypeMap;
import android.text.TextUtils.TruncateAt;
//gionee gaoj 2012-4-10 added for CR00555790 end
// gionee zhouyj 2012-06-27 add for CR00628333 start 
import android.view.MotionEvent;
import android.graphics.Color;
//gionee zhouyj 2012-06-27 add for CR00628333 end 

//gionee gaoj 2012-12-19 added for CR00751983 start
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.Animation;
//gionee gaoj 2012-12-19 added for CR00751983 end
import com.gionee.internal.telephony.GnPhone;
import gionee.provider.GnTelephony.Mms;
//Aurora xuyong 2013-10-11 added for aurora's new feature start
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.pdu.PduHeaders;
import com.aurora.android.mms.pdu.PduPersister;
import com.aurora.android.mms.pdu.NotificationInd;
//Aurora xuyong 2013-11-15 modified for google adapt end
//Aurora xuyong 2013-10-11 added for aurora's new feature end
// Aurora xuyong 2016-01-28 added for xy-smartsms start
import com.xy.smartsms.iface.IXYSmartSmsHolder;
import com.xy.smartsms.iface.IXYSmartSmsListItemHolder;
import com.xy.smartsms.manager.XYBubbleListItem;
// Aurora xuyong 2016-01-28 added for xy-smartsms end

/**
 * This class provides view of a message in the messages list.
 */
public class MessageListItem extends LinearLayout implements
// Aurora xuyong 2016-01-28 modified for xy-smartsms start
        SlideViewInterface, OnClickListener, IXYSmartSmsListItemHolder {
// Aurora xuyong 2016-01-28 modified for xy-smartsms end
    public static final String EXTRA_URLS = "com.android.mms.ExtraUrls";
    // Aurora xuyong 2014-05-26 added for multisim feature start
    // Aurora xuyong 2014-07-14 modified for aurora's new feature start
    private static final String AURORA_THUMBNAIL = "     ";
    // Aurora xuyong 2014-07-14 modified for aurora's new feature end
    // Aurora xuyong 2014-05-26 added for multisim feature end
    private static final String TAG = "MessageListItem";
    private static final String M_TAG = "Mms/MessageListItem";
    private static final StyleSpan STYLE_BOLD = new StyleSpan(Typeface.BOLD);

    static final int MSG_LIST_EDIT_MMS   = 1;
    static final int MSG_LIST_EDIT_SMS   = 2;
    // Aurora xuyong 2013-09-23 added for aurora's new feature start
    static final int MSG_LIST_EDIT_MMS_DIALOG   = 14;
    static final int MSG_LIST_EDIT_SMS_DIALOG   = 18;
    // Aurora xuyong 2013-12-11 added for aurora's new feature start
    static final int MSG_LIST_EDIT_SMS_LIST_DIALOG = 22;
    // Aurora xuyong 2013-12-11 added for aurora's new feature end
    // Aurora xuyong 2013-09-23 added for aurora's new feature end
    private static final int PADDING_LEFT_THR = 3;
    private static final int PADDING_LEFT_TWE = 13;
    // Aurora xuyong 2014-02-11 added for bug #1923 start
    private int mPositionInParent;
    // Aurora xuyong 2014-02-11 added for bug #1923 end
    
    private View mMmsView;
    // add for vcard
    private View mFileAttachmentView;
    private ImageView mImageView;
    // Aurora xuyong 2014-05-26 modified for multisim feature start
    private ImageView mSimIndicator;
    // Aurora xuyong 2014-05-26 modified for multisim feature end
    //private ImageView mLockedIndicator;
    private ImageView mDeliveredIndicator;
    private ImageView mDetailsIndicator;
    private ImageButton mSlideShowButton;
    // Aurora xuyong 2014-11-07 modified for bug #9526 start
    private AuroraExpandableTextView mBodyTextView;
    // Aurora xuyong 2014-11-07 modified for bug #9526 end
    private LinearLayout mExternalViews;
    // Aurora xuyong 2014-07-14 added for aurora's new feature start
    private ImageView mSimFlag;
    // Aurora xuyong 2014-07-14 added for aurora's new feature end
    //Aurora xuyong 2013-10-11 added for aurora's new feature start
    //private TextView mIdNumCopyTv;
    //Aurora xuyong 2013-10-11 added for aurora's new feature end
    private AuroraButton mDownloadButton;
    private TextView mDownloadingLabel;
    //Aurora xuyong 2013-10-11 added for aurora's new feature start
    private TextView mAuroraMmsSizeLabel;
    private TextView mAuroraMmsDateLabel;
    private ImageButton mAuroraDownloadMms;
    // Aurora xuyong 2013-11-11 added for aurora's new feature start
    private ProgressBar mAuroraDownloading;
    // Aurora xuyong 2013-11-11 added for aurora's new feature end
    //Aurora xuyong 2013-10-11 added for aurora's new feature end
    private Handler mHandler;
    private MessageItem mMessageItem;
    private String mDefaultCountryIso;
    private TextView mDateView;
    public View mMessageBlock;
    private Path mPath = new Path();
    private Paint mPaint = new Paint();
    // Aurora xuyong 2014-05-05 deleted for aurora's new feature start
    //private QuickContactDivot mAvatar;
    // Aurora xuyong 2014-05-05 deleted for aurora's new feature end
    private boolean mIsLastItemInList;
    //static private Drawable sDefaultContactImage;
    //gionee gaoj 2012-4-10 added for CR00555790 start
//    private View mMsgListItem;
    private LinearLayout mMsgListItemLayout;
    private TextView mMsgSimCard;
    // Aurora xuyong 2013-09-23 modified for aurora's new feature start
    // Aurora xuyong 2013-11-11 added for aurora's new feature start
    private ProgressBar mSendingIndi;
    // Aurora xuyong 2013-11-11 added for aurora's new feature end
    private ImageButton mMsgState;
    // Aurora xuyong 2013-09-23 modified for aurora's new feature end
//    private RelativeLayout mGnOutMsgPanel;
//    private RelativeLayout mGnInMsgPanel;
//    private FrameLayout mGnMmsInView;
//    private FrameLayout mGnMmsOutView;
    // Aurora xuyong 2014-02-11 added for bug #1923 start
    // Aurora xuyong 2015-05-08 modified for bug #13338 start
    private RelativeLayout mItemLayout;
    // Aurora xuyong 2015-05-08 modified for bug #13338 end
    // Aurora xuyong 2014-02-11 added for bug #1923 end
    private RelativeLayout mMsgListItemLayoutParent;
    //Aurora xuyong 2013-10-11 added for aurora's new feature start
    //private ImageView mGnMmsClip;
    // Aurora xuyong 2014-01-03 added for aurora;s new feature start
    private TextView mAttachDownInfo;
    // Aurora xuyong 2014-01-03 added for aurora;s new feature end
    //Aurora xuyong 2013-10-11 added for aurora's new feature end
    private ImageView mGnFavorite;
    // Aurora xuyong 2013-09-13 added for aurora's new feature start
    private View mSendStatus;
    private ImageButton mSendFailIndi;
    // Aurora xuyong 2013-09-13 added for aurora's new feature end
    // Aurora xuyong 2013-09-13 deleted for aurora's new feature start
    //private TextView mRepeatBtn;
    // Aurora xuyong 2013-09-13 deleted for aurora's new feature end
    private TextView mTimeFormat;
    private RelativeLayout mTimeLayout;
    // Aurora xuyong 2015-04-23 added for aurora's new feature start
    //private TextView mBatchTimeFormat;
    // Aurora xuyong 2015-04-23 added for aurora's new feature end
//    private LinearLayout mMsgItemOutPanel;
//    private ViewStub mViewStub;
    private static int mItemWidth = -1;
//    private TextView mMsgAddress;
    private static AuroraAlertDialog mAlertDialog = null;
    private TextView mDownloadGnButton;
    //Aurora xuyong 2013-10-11 added for aurora's new feature start
    private View mAuroraMmsDownloadView;
    //Aurora xuyong 2013-10-11 added for aurora's new feature end

//    private QuickContactDivot mGnAvatarOut;
//    private QuickContactDivot mGnAvatarIn;
    //static private Drawable sContactunknowImage;
    //static private Drawable mAvataroutDrawable;
    //gionee gaoj 2012-4-10 added for CR00555790 end

    //gionee gaoj 2012-8-14 added for CR00623375 start
    private TextView mRegularlyBtn;
    static final int REGULAR_RESET_TIME   = 7;
    //gionee gaoj 2012-8-14 added for CR00623375 end

    //gionee gaoj 2012-9-20 added for CR00699291 start
    //private QuickContactDivot mGnAvatar;
    private FrameLayout mGnMmsView;
    private boolean isItemIn;
    //gionee gaoj 2012-9-20 added for CR00699291 end
    // gionee zhouyj 2013-01-28 add for CR00767258 start 
    private boolean mIsDeleteMode = false;
    // gionee zhouyj 2013-01-28 add for CR00767258 end 

    //Gionee <gaoj> <2013-4-11> added for CR00796538 start
    // Aurora xuyong 2015-05-08 deleted for bug #13338 start
    /*private TextView mSendTimeView;*/
    // Aurora xuyong 2015-05-08 deleted for bug #13338 end
    //Gionee <gaoj> <2013-4-11> added for CR00796538 end

    //Gionee <gaoj> <2013-05-13> modified for CR00811367 begin
    static private int sLasThemeTag = 0;
    static private int mRecvBodyColor = 0;
    static private int mFailBodyColor = 0;
    static private int mRepeatBtColor = 0;
    static private int mSendBodyColor = 0;
    //Gionee <gaoj> <2013-05-13> modified for CR00811367 end
    //Gionee <zhouyj> <2013-05-15> add for CR00810588 begin
    private boolean mIsCurrPlaying = false;
    //private ImageView mStopBtnView;
    //Gionee <zhouyj> <2013-05-15> add for CR00810588 end
    
    //Gionee <guoyx> <2013-05-22> add for CR00818517 begin
    private Uri mContactUri = null;
    private String mContact = null;
    private boolean mExistContactDB = false;
    //Gionee <guoyx> <2013-05-30> add for CR00820739 begin
    /**
     * -1:phone contact; 
     * 0:sim1 contact;
     * 1:sim2 contact
     */
    private int mIndicatePhoneOrSim = -1;
    //Gionee <guoyx> <2013-05-30> add for CR00820739 end
    
    /**
     * value:-1
     */
    private final int URI_INVALID = -1;
    /**
     * value:1 uri for the phone
     */
    private final int URI_PHONE = 1;
    /**
     * value:2 uri for the website 
     */
    private final int URI_HTTP = 2;
    /**
     * value:3 uri for the mail
     */
    private final int URI_MAIL = 3;
    /**
     * value:4 uri for the rtsp
     */
    private final int URI_RTSP = 4;
    /**
     * value:0 action to dial
     */
    private final int ACTION_DIAL = 0;
    /**
     * value:1 action to send message
     */
    private final int ACTION_FORWARD = 1;
    /**
     * value:2 copy the number
     */
    private final int ACTION_COPY = 2;
    /**
     * value:3 create the new contact
     */
    private final int ACTION_NEW_CONTACT = 3;
    /**
     * value:4 insert to a contact
     */
    private final int ACTION_INSERT_CONTACT = 4;
    /**
     * value:5 insert to the contact
     */
    private final int ACTION_INSERT_CONTACT_DIRECTLY = 5;
    /**
     * value:0 item for access the website
     */
    private final int ACTION_ACCESS = 0;
    /**
     * value:1 item for add to bookmark
     */
    private final int ACTION_BOOKMARK = 1;
    /**
     * value:0 item for send mail
     */
    private final int ACTION_MAIL = 0;
    /**
     * value:1 item for new mail contact
     */
    private final int ACTION_MAIL_NEW_CONTACT = 1;
    /**
     * value:2 item for insert mail contact
     */
    private final int ACTION_MAIL_INSERT_CONTACT = 2;
    //Gionee <guoyx> <2013-05-22> add for CR00818517 end
    // Aurora xuyong 2014-09-26 added for india requirement start
    private static boolean DEFAULT_DELIVERY_REPORT_MODE  = MmsApp.mHasIndiaFeature ? true : false;
    // Aurora xuyong 2014-09-26 added for india requirement end

    // Aurora yudingmin 2014-11-21 added for optimize start
    private boolean SMS_DELIVERY_REPORT_MODE;
    private boolean MMS_DELIVERY_REPORT_MODE;
    // Aurora yudingmin 2014-11-21 added for optimize end
    private final Handler myHandler;
    private static class MyHandler extends Handler{
        private MessageListItem mMessageListItem;
        public static final int Gn_Message_TextLine_Rebuild = 0;
        public MyHandler(MessageListItem item){
            mMessageListItem = item;
        }
        public void handleMessage(Message msg){
            switch(msg.what){
            case Gn_Message_TextLine_Rebuild:
                mMessageListItem.gnRebuildTextLine((MessageItem)msg.obj);
                break;
            }
        }
    }
    
    public void gnRebuildTextLine(final MessageItem mMessageItem){
        /*// Aurora xuyong 2014-01-10 modified for aurora's new feature start
        int count = mBodyTextView.getLineCount();
        // Aurora xuyong 2014-01-10 modified for aurora's new feature start
        // Aurora xuyong 2014-02-18 modified for aurora's new feature start
        // Aurora xuyong 2015-08-07 modified for bug #15949 start
        if (count >= 12 && (mMessageItem != null && mMessageItem.mMessageType != PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND)) {
        // Aurora xuyong 2015-08-07 modified for bug #15949 end
        // Aurora xuyong 2014-02-18 modified for aurora's new feature end
        // Aurora xuyong 2014-01-10 modified for aurora's new feature end
            // Aurora xuyong 2013-01-10 added for aurora's new feature start
            if (mMessageItem != null && (mMessageItem.mAttachmentType <= WorkingMessage.ATTACHMENT
                    && mMessageItem.mAttachmentType >= WorkingMessage.IMAGE)) {
                // Aurora xuyong 2014-02-18 modified for aurora's new feature start
                mBodyTextView.setMaxLines(12);
                // Aurora xuyong 2014-02-18 modified for aurora's new feature end
            // Aurora xuyong 2014-01-16 modified for aurora's new feature start
                mIdNumCopyTv.setText(mContext.getString(R.string.aurora_show_detail_unfold));
                mIdNumCopyTv.setVisibility(View.VISIBLE);
                // Aurora xuyong 2014-02-13 added for bug #1928 start
                if (MessageListItem.this != null) {
                    MessageListItem.this.needInvalidateParent();
                }
                // Aurora xuyong 2014-02-13 added for bug #1928 end
            } else {
                // Aurora xuyong 2014-01-10 modified for aurora's new feature start
                if (count >= 18) {
                    mBodyTextView.setMaxLines(18);
                    mIdNumCopyTv.setText(mContext.getString(R.string.aurora_show_detail_unfold));
                    mIdNumCopyTv.setVisibility(View.VISIBLE);
                } else if (mIdNumCopyTv != null && mMessageItem.getIdentifyNumber() != null) {
                    mIdNumCopyTv.setText(mContext.getString(R.string.aurora_cp_id_num));
                    mIdNumCopyTv.setVisibility(View.VISIBLE);
                // Aurora xuyong 2015-08-06 added for aurora's new feature start
                } else {
                    mIdNumCopyTv.setVisibility(View.GONE);
                // Aurora xuyong 2015-08-06 added for aurora's new feature end
                }
                // Aurora xuyong 2014-02-13 added for bug #1928 start
                if (MessageListItem.this != null) {
                    MessageListItem.this.needInvalidateParent();
                }
                // Aurora xuyong 2014-02-13 added for bug #1928 end
                // Aurora xuyong 2014-01-10 modified for aurora's new feature end
        // Aurora xuyong 2014-01-10 modified for aurora's new feature end
            }
            // Aurora xuyong 2013-01-10 added for aurora's new feature end
            // Aurora xuyong 2014-01-16 deleted for aurora's new feature start
            //mShowDetail.setVisibility(View.VISIBLE);
            // Aurora xuyong 2014-01-16 deleted for aurora's new feature end
        } else*/ if (/*mIdNumCopyTv != null &&*/ mMessageItem.getIdentifyNumber() != null) {
            // Aurroa xuyong 2016-01-25 deleted for bug #18263 start
            //mBodyTextView.setTipText(mContext.getString(R.string.aurora_cp_id_num));
            //mBodyTextView.setIdCode(mMessageItem.getIdentifyNumber());
            // Aurroa xuyong 2016-01-25 deleted for bug #18263 end
            mBodyTextView.setHandler(mHandler);
            //mIdNumCopyTv.setVisibility(View.VISIBLE);
/*        	if (MessageListItem.this != null) {
                MessageListItem.this.needInvalidateParent();
            }*/
        // Aurora xuyong 2015-08-06 added for aurora's new feature start
        } /*else {
            mIdNumCopyTv.setVisibility(View.GONE);
        // Aurora xuyong 2015-08-06 added for aurora's new feature end
        }*/
    }
    
    public MessageListItem(Context context) {
        super(context);
        // Aurora yudingmin 2014-11-21 added for optimize start
        myHandler = new MyHandler(this);
        SharedPreferences prefs = AuroraPreferenceManager.getDefaultSharedPreferences(context);
        SMS_DELIVERY_REPORT_MODE = prefs.getBoolean(
                MessagingPreferenceActivity.SMS_DELIVERY_REPORT_MODE,
               // Aurora xuyong 2014-09-26 modified for india requirement start
                DEFAULT_DELIVERY_REPORT_MODE);
        MMS_DELIVERY_REPORT_MODE = prefs.getBoolean(
                        // Aurora xuyong 2014-09-26 modified for india requirement end
                        MessagingPreferenceActivity.MMS_DELIVERY_REPORT_MODE,
                        false);
        // Aurora yudingmin 2014-11-21 added for optimize end
        mDefaultCountryIso = MmsApp.getApplication().getCurrentCountryIso();

        //Gionee <gaoj> <2013-05-13> modified for CR00811367 begin
        /*if (MmsApp.mGnPerfList) {
            if (sDefaultContactImage == null || sLasThemeTag != MmsApp.sThemeChangTag) {
                if (MmsApp.mLightTheme) {
                    //image
                    sDefaultContactImage = context.getResources().getDrawable(R.drawable.gn_ic_contact_picture);
                    sContactunknowImage = context.getResources().getDrawable(R.drawable.gn_contact_default_unknow);
                    mAvataroutDrawable = context.getResources().getDrawable(R.drawable.gn_out_self_image);
                    
                    //text
                    mRecvBodyColor = context.getResources().getColor(R.color.gn_msg_body_recv_color_dark);
                    mFailBodyColor = context.getResources().getColor(R.color.gn_msg_body_faile_color_dark);
                    mRepeatBtColor = context.getResources().getColor(R.color.gn_msg_repeat_color_dark);
                    mSendBodyColor = context.getResources().getColor(R.color.gn_msg_body_send_color_dark);
                } else {
                    sDefaultContactImage = context.getResources().getDrawable(R.drawable.gn_ic_contact_picture_dark);
                    sContactunknowImage = context.getResources().getDrawable(R.drawable.gn_contact_default_unknow_dark);
                    mAvataroutDrawable = context.getResources().getDrawable(R.drawable.gn_out_self_image_dark);
                    
                    mRecvBodyColor = context.getResources().getColor(R.color.gn_msg_body_recv_color_white);
                    mFailBodyColor = context.getResources().getColor(R.color.gn_msg_body_faile_color_white);
                    mRepeatBtColor = context.getResources().getColor(R.color.gn_msg_repeat_color_white);
                    mSendBodyColor = context.getResources().getColor(R.color.gn_msg_body_send_color_white);
                }
                sLasThemeTag = MmsApp.sThemeChangTag;
            }
            return;
        }*/
        //Gionee <gaoj> <2013-05-13> modified for CR00811367 end
       /* 
        if (sDefaultContactImage == null) {
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
        } else {

            if (MmsApp.mLightTheme) {
                sDefaultContactImage = context.getResources().getDrawable(R.drawable.gn_ic_contact_picture);
            } else {
                sDefaultContactImage = context.getResources().getDrawable(R.drawable.gn_ic_contact_picture_dark);
            }
        }*/

        //gionee gaoj 2012-4-25 added for CR00555790 CR00601143 start
       /* if (MmsApp.mGnMessageSupport) {
            if (sContactunknowImage == null) {
                if (MmsApp.mLightTheme) {
                    sContactunknowImage = context.getResources().getDrawable(R.drawable.gn_contact_default_unknow);
                } else {
                    sContactunknowImage = context.getResources().getDrawable(R.drawable.gn_contact_default_unknow_dark);
                }
            } else {

                if (MmsApp.mLightTheme) {
                    sContactunknowImage = context.getResources().getDrawable(R.drawable.gn_contact_default_unknow);
                } else {
                    sContactunknowImage = context.getResources().getDrawable(R.drawable.gn_contact_default_unknow_dark);
                }
            }
            if (mAvataroutDrawable == null) {
                if (MmsApp.mLightTheme) {
                    mAvataroutDrawable = context.getResources().getDrawable(R.drawable.gn_out_self_image);
                } else {
                    mAvataroutDrawable = context.getResources().getDrawable(R.drawable.gn_out_self_image_dark);
                }
            } else {

                if (MmsApp.mLightTheme) {
                    mAvataroutDrawable = context.getResources().getDrawable(R.drawable.gn_out_self_image);
                } else {
                    mAvataroutDrawable = context.getResources().getDrawable(R.drawable.gn_out_self_image_dark);
                }
            }
        }
        //gionee gaoj 2012-4-25 added for CR00555790 CR00601143 end
*/    }

    public MessageListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Aurora yudingmin 2014-11-21 added for optimize start
        myHandler = new MyHandler(this);
        SharedPreferences prefs = AuroraPreferenceManager.getDefaultSharedPreferences(context);
        SMS_DELIVERY_REPORT_MODE = prefs.getBoolean(
                MessagingPreferenceActivity.SMS_DELIVERY_REPORT_MODE,
               // Aurora xuyong 2014-09-26 modified for india requirement start
                DEFAULT_DELIVERY_REPORT_MODE);
        MMS_DELIVERY_REPORT_MODE = prefs.getBoolean(
                // Aurora xuyong 2014-09-26 modified for india requirement end
                MessagingPreferenceActivity.MMS_DELIVERY_REPORT_MODE,
                false);
        // Aurora yudingmin 2014-11-21 added for optimize end

        int color = mContext.getResources().getColor(R.color.timestamp_color);
        mColorSpan = new ForegroundColorSpan(color);
        mDefaultCountryIso = MmsApp.getApplication().getCurrentCountryIso();

        //Gionee <gaoj> <2013-05-13> modified for CR00811367 begin
        /*if (MmsApp.mGnPerfList) {
            if (sDefaultContactImage == null || mRecvBodyColor == 0 || sLasThemeTag != MmsApp.sThemeChangTag) {
                if (MmsApp.mLightTheme) {
                    //image
                    sDefaultContactImage = context.getResources().getDrawable(R.drawable.gn_ic_contact_picture);
                    sContactunknowImage = context.getResources().getDrawable(R.drawable.gn_contact_default_unknow);
                    mAvataroutDrawable = context.getResources().getDrawable(R.drawable.gn_out_self_image);
                    
                    //text
                    mRecvBodyColor = context.getResources().getColor(R.color.gn_msg_body_recv_color_dark);
                    mFailBodyColor = context.getResources().getColor(R.color.gn_msg_body_faile_color_dark);
                    mRepeatBtColor = context.getResources().getColor(R.color.gn_msg_repeat_color_dark);
                    mSendBodyColor = context.getResources().getColor(R.color.gn_msg_body_send_color_dark);
                } else {
                    sDefaultContactImage = context.getResources().getDrawable(R.drawable.gn_ic_contact_picture_dark);
                    sContactunknowImage = context.getResources().getDrawable(R.drawable.gn_contact_default_unknow_dark);
                    mAvataroutDrawable = context.getResources().getDrawable(R.drawable.gn_out_self_image_dark);
                    
                    mRecvBodyColor = context.getResources().getColor(R.color.gn_msg_body_recv_color_white);
                    mFailBodyColor = context.getResources().getColor(R.color.gn_msg_body_faile_color_white);
                    mRepeatBtColor = context.getResources().getColor(R.color.gn_msg_repeat_color_white);
                    mSendBodyColor = context.getResources().getColor(R.color.gn_msg_body_send_color_white);
                }
                sLasThemeTag = MmsApp.sThemeChangTag;
            }
            return;
        }
        //Gionee <gaoj> <2013-05-13> modified for CR00811367 end
        
        if (sDefaultContactImage == null) {
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
        } else {

            if (MmsApp.mLightTheme) {
                sDefaultContactImage = context.getResources().getDrawable(R.drawable.gn_ic_contact_picture);
            } else {
                sDefaultContactImage = context.getResources().getDrawable(R.drawable.gn_ic_contact_picture_dark);
            }
        }

        //gionee gaoj 2012-4-25 added for CR00555790 CR00601143 start
        if (MmsApp.mGnMessageSupport) {
            if (sContactunknowImage == null) {
                if (MmsApp.mLightTheme) {
                    sContactunknowImage = context.getResources().getDrawable(R.drawable.gn_contact_default_unknow);
                } else {
                    sContactunknowImage = context.getResources().getDrawable(R.drawable.gn_contact_default_unknow_dark);
                }
            } else {

                if (MmsApp.mLightTheme) {
                    sContactunknowImage = context.getResources().getDrawable(R.drawable.gn_contact_default_unknow);
                } else {
                    sContactunknowImage = context.getResources().getDrawable(R.drawable.gn_contact_default_unknow_dark);
                }
            }
            if (mAvataroutDrawable == null) {
                if (MmsApp.mLightTheme) {
                    mAvataroutDrawable = context.getResources().getDrawable(R.drawable.gn_out_self_image);
                } else {
                    mAvataroutDrawable = context.getResources().getDrawable(R.drawable.gn_out_self_image_dark);
                }
            } else {

                if (MmsApp.mLightTheme) {
                    mAvataroutDrawable = context.getResources().getDrawable(R.drawable.gn_out_self_image);
                } else {
                    mAvataroutDrawable = context.getResources().getDrawable(R.drawable.gn_out_self_image_dark);
                }
            }
        }*/
        //gionee gaoj 2012-4-25 added for CR00555790 CR00601143 end
    }
    
    private boolean mNeedShowWeatherInfo;
    public void setNeedShowWeatherInfo(boolean need) {
        mNeedShowWeatherInfo = need;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //gionee gaoj 2012-9-20 added for CR00699291 start
        if (MmsApp.mGnMessageSupport) {
            gnonFinishInflate();
            return;
        }
        /*//gionee gaoj 2012-9-20 added for CR00699291 end
        // Aurora xuyong 2014-11-07 modified for bug #9526 start
        mBodyTextView = (AuroraExpandableTextView) findViewById(R.id.text_view);
        //mBodyTextView.setMovementMethod(AuroraLinkMovementMethod.getInstance());
        // Aurora xuyong 2014-11-07 modified for bug #9526 end
        mDateView = (TextView) findViewById(R.id.date_view);
        //a0
        mSimStatus = (TextView) findViewById(R.id.sim_status);
        //a1
        //mLockedIndicator = (ImageView) findViewById(R.id.locked_indicator);
        mDeliveredIndicator = (ImageView) findViewById(R.id.delivered_indicator);
        mDetailsIndicator = (ImageView) findViewById(R.id.details_indicator);
        // Aurora xuyong 2014-05-05 deleted for aurora's new feature start
        //mAvatar = (QuickContactDivot) findViewById(R.id.avatar);
        // Aurora xuyong 2014-05-05 deleted for aurora's new feature end
        mMessageBlock = findViewById(R.id.message_block);
        //a0
        //add for multi-delete
        // Aurora xuyong 2013-12-17 modified for aurora's new feature start
        mSelectedBox = (AuroraCheckBox)findViewById(R.id.select_check_box);
        // Aurora xuyong 2013-12-17 modified for aurora's new feature end
        //a1*/
    }
    
    // Aurora xuyong 2013-12-27 added for aurora's new feature start
    // Aurora xuyong 2014-01-10 modified for bug #1666 start
    // Aurora xuyong 2015-05-08 modified for bug #13338 start
   /* private void auroraStartCheckBoxAppearingAnim(RelativeLayout  front, CheckBox cb)
    // Aurora xuyong 2015-05-08 modified for bug #13338 end
    {
        final CheckBox box = cb;
        // Aurora xuyong 2014-03-28 added for bug #3676 start 
        box.setVisibility(View.VISIBLE);
        // Aurora xuyong 2014-03-28 added for bug #3676 end
        // Aurora xuyong 2015-05-08 modified for bug #13338 start
        final RelativeLayout mFront = front;
        // Aurora xuyong 2015-05-08 modified for bug #13338 end
        // Aurora xuyong 2013-01-13 modified for aurora's new feature strat
        if(box == null) {
            return;
        }
        // Aurora xuyong 2013-01-13 modified for aurora's new feature end
        ValueAnimator animIn1 = ValueAnimator.ofFloat( 0f,1f);
        animIn1.setDuration(300);
        animIn1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                float alpha = (Float) animation.getAnimatedValue();
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mFront.getLayoutParams();
                // Aurora xuyong 2015-05-08 modified for bug #13338 start
                lp.leftMargin = (int) (alpha * 33);
                // Aurora xuyong 2015-05-08 modified for bug #13338 end
                mFront.setLayoutParams(lp);
                // Aurora xuyong 2014-03-28 deletetd for bug #3676 start 
                //// Aurora xuyong 2013-01-13 modified for aurora's new feature strat
                //if(box.getVisibility() != View.VISIBLE) {
                //    box.setVisibility(View.VISIBLE);
                //}
                //// Aurora xuyong 2013-01-13 modified for aurora's new feature end
                //box.setAlpha(alpha);
                //box.invalidate();
                // Aurora xuyong 2014-03-28 deletetd for bug #3676 end
            }
        });
        animIn1.start();
    }*/
    // Aurora xuyong 2014-01-10 modified for bug #1666 end
    // Aurora xuyong 2013-12-27 added for aurora's new feature end
    // Aurora xuyong 2014-02-11 added for bug #1923 start
    public void setPositionInParent(int position) {
        mPositionInParent = position;
    }
    
    public int getPositionInParent() {
        return mPositionInParent;
    }
    // Aurora xuyong 2014-02-11 added for bug #1923 end
    public void bind(MessageItem msgItem, boolean isLastItem, boolean isDeleteMode) {
        //gionee gaoj 2012-9-20 added for CR00699291 start
        if (MmsApp.mGnMessageSupport) {
            gnbind(msgItem, isLastItem, isDeleteMode);
            return;
        }
        //gionee gaoj 2012-9-20 added for CR00699291 end
        //a0
        Log.i(TAG, "MessageListItem.bind() : msgItem.mSimId = " + msgItem.mSimId);
        //a1
        mMessageItem = msgItem;
        mIsLastItemInList = isLastItem;
        
        //a0
        setSelectedBackGroud(false);
        if (isDeleteMode) {
            mSelectedBox.setVisibility(View.VISIBLE);
            // Aurora xuyong 2015-04-23 added for aurora's new feature start
            //mBatchTimeFormat.setVisibility(View.VISIBLE);
            // Aurora xuyong 2015-04-23 added for aurora's new feature end
            if (msgItem.isSelected()) {
                setSelectedBackGroud(true);
            }
        } else {
            mSelectedBox.setVisibility(View.GONE);
            // Aurora xuyong 2015-04-23 added for aurora's new feature start
            //mBatchTimeFormat.setVisibility(View.GONE);
            // Aurora xuyong 2015-04-23 added for aurora's new feature end
        }
        //a0

        setLongClickable(false);
        //set item these two false can make listview always get click event.
        setFocusable(false);
        setClickable(false);
        switch (msgItem.mMessageType) {
            case PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND:
                bindNotifInd(msgItem);
                break;
            default:
                bindCommonMessage(msgItem);
                break;
        }
    }

    public void unbind() {
        // Clear all references to the message item, which can contain attachments and other
        // memory-intensive objects
        mMessageItem = null;
        if (mImageView != null) {
            // Because #setOnClickListener may have set the listener to an object that has the
            // message item in its closure.
            mImageView.setOnClickListener(null);
        }
        if (mSlideShowButton != null) {
            // Because #drawPlaybackButton sets the tag to mMessageItem
            mSlideShowButton.setTag(null);
        }
    }

    public MessageItem getMessageItem() {
        return mMessageItem;
    }

    public void setMsgListItemHandler(Handler handler) {
        mHandler = handler;
        // Aurora xuyong 2014-09-15 added for aurora's new feature start
        // Aurora xuyong 2015-02-04 deleted for bug #11531 start
        //AuroraURLSpan.setHandler(handler);
        // Aurora xuyong 2015-02-04 deleted for bug #11531 end
        // Aurroa xuyong 2016-01-25 modified for aurora 2.0 new feature start
        AuroraURLSpan.setNormalLinkedColor(this.getResources().getColor(R.color.aurora_widget_tip_text_color));
        // Aurroa xuyong 2016-01-25 modified for aurora 2.0 new feature end
        AuroraURLSpan.setPressedLinkedColor(this.getResources().getColor(R.color.aurora_link_pressed_color));
        // Aurora xuyong 2014-09-15 added for aurora's new feature end
    }
    // Aurora xuyong 2014-02-13 added for bug #1928 start
    // Aurora xuyong 2014-02-13 modified for bug #1928 start
    private static boolean mNeedChangeInvaState;
    
    public static void setNeedChangeInva(boolean status) {
        mNeedChangeInvaState = status;
    }
    
    private void needInvalidateParent() {
        if (mNeedChangeInvaState) {
            mNeedChangeInvaState = false;
            ListView view = (ListView) MessageListItem.this.getParent();
            if (view != null) {
                view.invalidateViews();
            }
        }
    }
    // Aurora xuyong 2014-02-13 modified for bug #1928 end
    // Aurora xuyong 2014-02-13 added for bug #1928 end
    private void bindNotifInd(final MessageItem msgItem) {
        hideMmsViewIfNeeded();
        // add for vcard
        hideFileAttachmentViewIfNeeded();

        String msgSizeText = mContext.getString(R.string.message_size_label)
                                + String.valueOf((msgItem.mMessageSize + 1023) / 1024)
                                + mContext.getString(R.string.kilobyte);
        mBodyTextView.setVisibility(View.VISIBLE);
        // Aurroa xuyong 2016-01-25 added for bug #18263 start
        mBodyTextView.setMsgUri(msgItem.getMessageUri());
        // Aurroa xuyong 2016-01-25 added for bug #18263 end
        mBodyTextView.setText(formatMessage(msgItem, msgItem.mContact, null, msgItem.mSubject,
                                            msgItem.mHighlight, msgItem.mTextContentType), this.getPositionInParent(), msgItem.mFolded == 1 ? true : false);
        //Aurora xuyong 2013-10-11 added for aurora's new feature start
        // Aurora xuyong 2013-10-12 modified for aurora's new feature start
        // Aurora xuyong 2013-10-18 modified for aurora's new feature start 
        // Aurora xuyong 2013-10-24 modified for aurora's new feature start
        // Aurora liugj 2013-10-31 modified for fix bug-331/332 start 
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                // Aurora xuyong 2014-01-10 modified for aurora's new feature start
                /*int count = mBodyTextView.getLineCount();
                // Aurora xuyong 2014-01-10 modified for aurora's new feature start
                // Aurora xuyong 2014-02-18 modified for aurora's new feature start
                // Aurora xuyong 2015-08-07 modified for bug #15949 start
                if (count >= 12 && (mMessageItem != null && mMessageItem.mMessageType != PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND)) {
                // Aurora xuyong 2015-08-07 modified for bug #15949 end
                // Aurora xuyong 2014-02-18 modified for aurora's new feature end
                // Aurora xuyong 2014-01-10 modified for aurora's new feature end
                    // Aurora xuyong 2013-01-10 added for aurora's new feature start
                    if (mMessageItem != null && (mMessageItem.mAttachmentType <= WorkingMessage.ATTACHMENT
                            && mMessageItem.mAttachmentType >= WorkingMessage.IMAGE)) {
                        // Aurora xuyong 2014-02-18 modified for aurora's new feature start
                        mBodyTextView.setMaxLines(12);
                        // Aurora xuyong 2014-02-18 modified for aurora's new feature end
                    // Aurora xuyong 2014-01-16 modified for aurora's new feature start
                        mIdNumCopyTv.setText(mContext.getString(R.string.aurora_show_detail_unfold));
                        mIdNumCopyTv.setVisibility(View.VISIBLE);
                        // Aurora xuyong 2014-02-13 added for bug #1928 start
                        if (MessageListItem.this != null) {
                            MessageListItem.this.needInvalidateParent();
                        }
                        // Aurora xuyong 2014-02-13 added for bug #1928 end
                    } else {
                        // Aurora xuyong 2014-01-10 modified for aurora's new feature start
                        if (count >= 18) {
                            mBodyTextView.setMaxLines(18);
                            mIdNumCopyTv.setText(mContext.getString(R.string.aurora_show_detail_unfold));
                            mIdNumCopyTv.setVisibility(View.VISIBLE);
                        } else if (mIdNumCopyTv != null && mMessageItem.getIdentifyNumber() != null) {
                            mIdNumCopyTv.setText(mContext.getString(R.string.aurora_cp_id_num));
                            mIdNumCopyTv.setVisibility(View.VISIBLE);
                        // Aurora xuyong 2015-08-06 added for aurora's new feature start
                        } else {
                            mIdNumCopyTv.setVisibility(View.GONE);
                        // Aurora xuyong 2015-08-06 added for aurora's new feature end
                        }
                        if (MessageListItem.this != null) {
                            MessageListItem.this.needInvalidateParent();
                        }
                        // Aurora xuyong 2014-01-10 modified for aurora's new feature end
                // Aurora xuyong 2014-01-10 modified for aurora's new feature end
                    }*/
                    // Aurora xuyong 2013-01-10 added for aurora's new feature end
                    // Aurora xuyong 2014-01-16 deleted for aurora's new feature start
                    //mShowDetail.setVisibility(View.VISIBLE);
                    /*// Aurora xuyong 2014-01-16 deleted for aurora's new feature end
                    mShowDetail.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                        	if (mShowDetail.getText().equals(mContext.getString(R.string.aurora_show_detail))) {
        	                    // Aurora xuyong 2013-12-30 modified for aurora's new feature start
        	                    if (mMessageItem.mType.equals("sms")) {
        	                        Message msg = Message.obtain(mHandler, ITEM_SHOW_DETAIL);
        	                 // Aurora xuyong 2014-05-26 modified for multisim feature start
        	                        String text = null;
        	                        String rexg = AURORA_THUMBNAIL + "  ";
        	                        if (MmsApp.mGnMultiSimMessage) {
        	                            text = mBodyTextView.getText().toString();
        	                            if (text.startsWith(rexg)) {
        	                                text = text.replaceFirst(rexg, "");
        	                            } else {
        	                                text = mBodyTextView.getText().toString();
        	                            }
        	                        } else {
        	                            text = mBodyTextView.getText().toString();
        	                        }
        	                        msg.obj = text;
        	                 // Aurora xuyong 2014-05-26 modified for multisim feature end
        	                        msg.sendToTarget();
        	                    } else {
        	                 // mMessageItem might be null, casuse mms to crash!
        	                 // Aurora xuyong 2014-06-12 modified for upper reason start
        	                        if (mMessageItem != null) {
        	                            MessageUtils.viewMmsMessageAttachment(mContext, ContentUris.withAppendedId(Mms.CONTENT_URI, mMessageItem.mMsgId), null,
        	                                    mMessageItem.mSubject, mMessageItem.mTimestamp);
        	                        }
        	                 // Aurora xuyong 2014-06-12 modified for upper reason end
        	                    }
        	                    // Aurora xuyong 2013-12-30 modified for aurora's new feature end
                        	} else if (mShowDetail.getText().equals(mContext.getString(R.string.aurora_cp_id_num))) {
                                Message msg = mHandler.obtainMessage(ITEM_COPY_IDENTIFY_NUM);
                                msg.obj = mMessageItem.getIdentifyNumber();
                                msg.sendToTarget();
                        	}
                        }
                    });*/
                /*} else*/ if (/*mIdNumCopyTv != null && */mMessageItem.getIdentifyNumber() != null) {
                    // Aurroa xuyong 2016-01-25 deleted for bug #18263 start
                    //mBodyTextView.setText(mContext.getString(R.string.aurora_cp_id_num));
                    //mBodyTextView.setIdCode(mMessageItem.getIdentifyNumber());
                    // Aurroa xuyong 2016-01-25 deleted for bug #18263 end
                    mBodyTextView.setHandler(mHandler);
                    //mIdNumCopyTv.setVisibility(View.VISIBLE);
                	/*if (MessageListItem.this != null) {
                        MessageListItem.this.needInvalidateParent();
                    }*/
                // Aurora xuyong 2015-08-06 added for aurora's new feature start
                }/* else {
                    mIdNumCopyTv.setVisibility(View.GONE);
                // Aurora xuyong 2015-08-06 added for aurora's new feature end
                }*/
            }
        });
        /*if (mMsgListItemLayoutParent.getHeight() > 800) {
        // Aurora xuyong 2013-10-24 modified for aurora's new feature end
        // Aurora xuyong 2013-10-18 modified for aurora's new feature end
            // Aurora xuyong 2013-10-24 modified for aurora's new feature start
            mBodyTextView.setMaxLines(18);
            // Aurora xuyong 2013-10-24 modified for aurora's new feature end
            mShowDetail.setVisibility(View.VISIBLE);
        // Aurora xuyong 2013-10-12 modified for aurora's new feature end
        } else {
            mShowDetail.setVisibility(View.GONE);
        }*/
        // Aurora liugj 2013-10-31 modified for fix bug-331/332 end
        //Aurora xuyong 2013-10-11 added for aurora's new feature end
//MTK_OP01_PROTECT_START
        // add for text zoom
        // Aurora xuyong 2013-09-13 added for aurora's new feature start
        /*if (MmsApp.isTelecomOperator()) {
            mBodyTextView.setTextSize(mTextSize);
        }*/
        // Aurora xuyong 2013-09-13 added for aurora's new feature end
//MTK_OP01_PROTECT_END

        mDateView.setVisibility(View.VISIBLE);
        mDateView.setText(msgSizeText + " " + msgItem.mTimestamp);
        //a0
        mSimStatus.setVisibility(View.VISIBLE);
        mSimStatus.setText(formatSimStatus(msgItem));
        //a1

        int state = DownloadManager.getInstance().getState(msgItem.mMessageUri);
        switch (state) {
            case DownloadManager.STATE_DOWNLOADING:
                inflateDownloadControls();
                //Aurora xuyong 2013-10-11 modified for aurora's new feature start
                //mDownloadingLabel.setVisibility(View.VISIBLE);
                // Aurora xuyong 2013-11-11 modified for aurora's new feature start
                mAuroraDownloadMms.setVisibility(View.GONE);
                //mAuroraDownloadMms.setImageResource(R.drawable.aurora_msg_sending);
                mAuroraDownloading.setVisibility(View.VISIBLE);
                // Aurora xuyong 2013-11-11 modified for aurora's new feature end
                //mDownloadButton.setVisibility(View.GONE);
                mAuroraMmsSizeLabel.setVisibility(View.VISIBLE);
                mAuroraMmsDateLabel.setVisibility(View.VISIBLE);
                //Aurora xuyong 2013-10-11 modified for aurora's new feature end
                //a0
                findViewById(R.id.text_view).setVisibility(GONE);
                //a1
                break;
            case DownloadManager.STATE_UNSTARTED:
            case DownloadManager.STATE_TRANSIENT_FAILURE:
            case DownloadManager.STATE_PERMANENT_FAILURE:
            default:
                setLongClickable(true);
                inflateDownloadControls();
                //Aurora xuyong 2013-10-11 modified for aurora's new feature start
                //mDownloadingLabel.setVisibility(View.GONE);
                //mDownloadButton.setVisibility(View.VISIBLE);
                mAuroraDownloadMms.setVisibility(View.VISIBLE);
                mAuroraDownloadMms.setImageResource(R.drawable.aurora_mms_download_selector);
                // Aurora xuyong 2013-11-11 added for aurora's new feature start
                mAuroraDownloading.setVisibility(View.GONE);
                // Aurora xuyong 2013-11-11 added for aurora's new feature end
                mAuroraMmsSizeLabel.setVisibility(View.VISIBLE);
                mAuroraMmsDateLabel.setVisibility(View.VISIBLE);
                //Aurora xuyong 2013-10-11 modified for aurora's new feature end
                //a0
                findViewById(R.id.text_view).setVisibility(GONE);
                //a1
                //Aurora xuyong 2013-10-11 modified for aurora's new feature start
                mAuroraDownloadMms.setOnClickListener(new OnClickListener() {
                //Aurora xuyong 2013-10-11 modified for aurora's new feature end
                    @Override
                    public void onClick(View v) {
                        //a0
                        //add for multi-delete
                        if (mSelectedBox != null && mSelectedBox.getVisibility() == View.VISIBLE) {
                            return;
                        }

                        // add for gemini
                        int simId = 0;
                        if (MmsApp.mGnMultiSimMessage) {
                            // get sim id by uri
                            Cursor cursor = SqliteWrapper.query(msgItem.mContext, msgItem.mContext.getContentResolver(),
                                msgItem.mMessageUri, new String[] { Mms.SIM_ID }, null, null, null);
                            if (cursor != null) {
                                try {
                                    if ((cursor.getCount() == 1) && cursor.moveToFirst()) {
                                        simId = cursor.getInt(0);
                                    }
                                } finally {
                                    cursor.close();
                                }
                            }
                        }
                        
                        //MTK_OP01_PROTECT_START
                        if (MmsApp.isTelecomOperator()) {
                            // check device memory status
                            if (MmsConfig.getDeviceStorageFullStatus()) {
                                MmsApp.getToastHandler().sendEmptyMessage(MmsApp.MSG_RETRIEVE_FAILURE_DEVICE_MEMORY_FULL);
                                return;
                            }
                        }
                        //MTK_OP01_PROTECT_END
                        //a1
                        //Aurora xuyong 2013-10-11 modified for aurora's new feature start
                        //mDownloadingLabel.setVisibility(View.VISIBLE);
                        //mDownloadButton.setVisibility(View.GONE);
                        // Aurora xuyong 2013-11-11 modified for aurora's new feature start
                        mAuroraDownloadMms.setVisibility(GONE);
                        //mAuroraDownloadMms.setImageResource(R.drawable.aurora_msg_sending);
                        mAuroraDownloading.setVisibility(View.VISIBLE);
                        // Aurora xuyong 2013-11-11 modified for aurora's new feature end
                        mAuroraMmsSizeLabel.setVisibility(View.VISIBLE);
                        mAuroraMmsDateLabel.setVisibility(View.VISIBLE);
                        //Aurora xuyong 2013-10-11 modified for aurora's new feature end
                        Intent intent = new Intent(mContext, TransactionService.class);
                        intent.putExtra(TransactionBundle.URI, msgItem.mMessageUri.toString());
                        intent.putExtra(TransactionBundle.TRANSACTION_TYPE,
                                Transaction.RETRIEVE_TRANSACTION);
                        // Aurora xuyong 2014-11-08 added for reject new feature start
                        intent.putExtra("avoidReject", true);
                        // Aurora xuyong 2014-11-08 addded for reject new feature end
                        //a0
                        // add for gemini
                        intent.putExtra(GnPhone.GEMINI_SIM_ID_KEY, simId);
                        //a1
                        mContext.startService(intent);
                    }
                });
                //mtk81083 this is a google default bug. it has no this code!
                // When we show the mDownloadButton, this list item's onItemClickListener doesn't
                // get called. (It gets set in ComposeMessageActivity:
                // mMsgListView.setOnItemClickListener) Here we explicitly set the item's
                // onClickListener. It allows the item to respond to embedded html links and at the
                // same time, allows the button to work.
                // Aurora xuyong 2014-02-11 deleted for bug #1923 start
                /*setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onMessageListItemClick();
                    }
                });*/
                // Aurora xuyong 2014-02-11 deleted for bug #1923 end
                break;
        }

        // Hide the indicators.
        //m0
        //mLockedIndicator.setVisibility(View.GONE);
       /* if (msgItem.mLocked) {
            mLockedIndicator.setImageResource(R.drawable.ic_lock_message_sms);          
            mLockedIndicator.setVisibility(View.VISIBLE);

            mSimStatus.setPadding(PADDING_LEFT_THR, 0, 0, 0);
        } else {*/
        //    mLockedIndicator.setVisibility(View.GONE);

        //    mSimStatus.setPadding(PADDING_LEFT_TWE, 0, 0, 0);
        //}
        //m1
        mDeliveredIndicator.setVisibility(View.GONE);
        mDetailsIndicator.setVisibility(View.GONE);
        // Aurora xuyong 2014-05-05 deleted for aurora's new feature start
        //updateAvatarView(msgItem.mAddress, false);
        // Aurora xuyong 2014-05-05 deleted for aurora's new feature end
    }

    private Toast mInvalidContactToast;
    private boolean selfExists() {
        boolean exists = false;
        Cursor c = null;
        try {
            c = mContext.getContentResolver().query(Profile.CONTENT_URI, new String[]{"_id"}, null, null, null);
            exists = c != null && c.moveToFirst();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return exists;
    }
    // Aurora xuyong 2014-05-05 deleted for aurora's new feature start
    /*private void updateAvatarView(String addr, boolean isSelf) {
        Drawable avatarDrawable;
        if (isSelf || !TextUtils.isEmpty(addr)) {
            Contact contact = isSelf ? Contact.getMe(false) : Contact.get(addr, false);
            avatarDrawable = contact.getAvatar(mContext, sDefaultContactImage);

            if (isSelf) {
                if (selfExists()) {
                    mAvatar.assignContactUri(Profile.CONTENT_URI);
                } else {
                    mAvatar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mInvalidContactToast == null) {
                                mInvalidContactToast = Toast.makeText(mContext, R.string.invalid_contact_message, Toast.LENGTH_SHORT);
                            }
                            mInvalidContactToast.show();
                        }
                    });
                }
            } else {
                String number = contact.getNumber();
                if (Mms.isEmailAddress(number)) {
                    mAvatar.assignContactFromEmail(number, true);
                } else {
                    mAvatar.assignContactFromPhone(number, true);
                }
            }
        } else {
            avatarDrawable = sDefaultContactImage;
        }
        mAvatar.setImageDrawable(avatarDrawable);
    }*/
    // Aurora xuyong 2014-05-05 deleted for aurora's new feature end
    private void bindCommonMessage(final MessageItem msgItem) {
        if (mDownloadButton != null) {
            //Aurora xuyong 2013-10-11 modified for aurora's new feature start
            //mDownloadButton.setVisibility(View.GONE);
            //mDownloadingLabel.setVisibility(View.GONE);
            mAuroraMmsSizeLabel.setVisibility(View.GONE);
            mAuroraMmsDateLabel.setVisibility(View.GONE);
            mAuroraDownloadMms.setVisibility(View.GONE);
            // Aurora xuyong 2013-11-11 added for aurora's new feature start
            mAuroraDownloading.setVisibility(View.GONE);
            // Aurora xuyong 2013-11-11 added for aurora's new feature end
            //a0
            //mBodyTextView.setVisibility(View.VISIBLE);
            //Aurora xuyong 2013-10-11 modified for aurora's new feature end
            //a1
        }
        // Since the message text should be concatenated with the sender's
        // address(or name), I have to display it here instead of
        // displaying it by the Presenter.
        mBodyTextView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        //Aurora xuyong 2013-10-11 added for aurora's new feature start
        mBodyTextView.setVisibility(View.VISIBLE);
        //Aurora xuyong 2013-10-11 added for aurora's new feature end

        boolean isSelf = Sms.isOutgoingFolder(msgItem.mBoxId);
        String addr = isSelf ? null : msgItem.mAddress;
        // Aurora xuyong 2014-05-05 added for aurora's new feature start
        //updateAvatarView(addr, isSelf);
        // Aurora xuyong 2014-05-05 added for aurora's new feature end

        // Get and/or lazily set the formatted message from/on the
        // MessageItem.  Because the MessageItem instances come from a
        // cache (currently of size ~50), the hit rate on avoiding the
        // expensive formatMessage() call is very high.
        CharSequence formattedMessage = msgItem.getCachedFormattedMessage();
        //a0
        CharSequence formattedTimestamp = msgItem.getCachedFormattedTimestamp();
        CharSequence formattedSimStatus= msgItem.getCachedFormattedTimestamp();
        //a1
        if (formattedMessage == null) {
            formattedMessage = formatMessage(msgItem, msgItem.mContact, msgItem.mBody,
                                             msgItem.mSubject,
                                             msgItem.mHighlight, msgItem.mTextContentType);
            //a0
            formattedTimestamp = formatTimestamp(msgItem, msgItem.mTimestamp);
            formattedSimStatus = formatSimStatus(msgItem);
            //a1
        }
        // Aurroa xuyong 2016-01-25 added for bug #18263 start
        mBodyTextView.setMsgUri(msgItem.getMessageUri());
        // Aurroa xuyong 2016-01-25 added for bug #18263 end
        mBodyTextView.setText(formattedMessage, this.getPositionInParent(), msgItem.mFolded == 1 ? true : false);
        //Aurora xuyong 2013-10-11 added for aurora's new feature start
        // Aurora xuyong 2013-10-12 modified for aurora's new feature start
        // Aurora xuyong 2013-10-18 modified for aurora's new feature start 
        // Aurora xuyong 2013-10-24 modified for aurora's new feature start
        // Aurora liugj 2013-10-31 modified for fix bug-331/332 start 
       /*if (mBodyTextView.getText().toString().getBytes().length > 580) {
        // Aurora xuyong 2013-10-24 modified for aurora's new feature end
        // Aurora xuyong 2013-10-18 modified for aurora's new feature end
            // Aurora xuyong 2013-10-24 modified for aurora's new feature start
            mBodyTextView.setMaxLines(18);
            // Aurora xuyong 2013-10-24 modified for aurora's new feature end
            mShowDetail.setVisibility(View.VISIBLE);
        // Aurora xuyong 2013-10-12 modified for aurora's new feature end
        } else {
            mShowDetail.setVisibility(View.GONE);
        }*/
        // Aurora liugj 2013-10-31 modified for fix bug-331/332 end
        //Aurora xuyong 2013-10-11 added for aurora's new feature end
//MTK_OP01_PROTECT_START
        // add for text zoom
        // Aurora xuyong 2013-09-13 added for aurora's new feature start
        /*if (MmsApp.isTelecomOperator()) {
            mBodyTextView.setTextSize(mTextSize);
        }*/
        // Aurora xuyong 2013-09-13 added for aurora's new feature end
//MTK_OP01_PROTECT_END

        // If we're in the process of sending a message (i.e. pending), then we show a "SENDING..."
        // string in place of the timestamp.
        //m0
        /*mDateView.setText(msgItem.isSending() ?
                mContext.getResources().getString(R.string.sending_message) :
                    msgItem.mTimestamp);
        */
        if (msgItem.isFailedMessage() || (!msgItem.isSending() && TextUtils.isEmpty(msgItem.mTimestamp))) {
            mDateView.setVisibility(View.GONE);
        } else {
            mDateView.setVisibility(View.VISIBLE);
            mDateView.setText(msgItem.isSending() ?
                mContext.getResources().getString(R.string.sending_message) :
                    msgItem.mTimestamp);
        }
        //m1
        
        //a0
        if (!msgItem.isSimMsg() && !TextUtils.isEmpty(formattedSimStatus)) {
            mSimStatus.setVisibility(View.VISIBLE);
            mSimStatus.setText(formattedSimStatus);
        } else {
            mSimStatus.setVisibility(View.GONE);
        }
        //a1

        if (msgItem.isSms()) {
            hideMmsViewIfNeeded();
            // add for vcard
            hideFileAttachmentViewIfNeeded();
        } else {
            Presenter presenter = PresenterFactory.getPresenter(
                    "MmsThumbnailPresenter", mContext,
                    this, msgItem.mSlideshow);
            presenter.present();

            if (msgItem.mAttachmentType != WorkingMessage.TEXT) {
                if (msgItem.mAttachmentType == WorkingMessage.ATTACHMENT) {
                    // show file attachment view
                    hideMmsViewIfNeeded();
                    showFileAttachmentView(msgItem.mSlideshow.getAttachFiles());
                } else {
                    hideFileAttachmentViewIfNeeded();
                    inflateMmsView();
                    mMmsView.setVisibility(View.VISIBLE);
                    drawPlaybackButton(msgItem);
                    if (mSlideShowButton.getVisibility() == View.GONE) {
                        setMediaOnClickListener(msgItem);
                    }
                }
            } else {
                hideMmsViewIfNeeded();
                // add for vcard
                hideFileAttachmentViewIfNeeded();
            }
        }
        drawRightStatusIndicator(msgItem);

        requestLayout();
    }

    private void hideMmsViewIfNeeded() {
        //gionee gaoj 2012-9-20 added for CR00699291 start
        if (MmsApp.mGnMessageSupport) {
            if (mGnMmsView != null) {
                mGnMmsView.setVisibility(View.GONE);
            }
        } else
        //gionee gaoj 2012-9-20 added for CR00699291 end
        if (mMmsView != null) {
            mMmsView.setVisibility(View.GONE);
        }
        /*// Aurora xuyong 2015-08-07 added for bug #15949 start
        if (mMessageItem != null && mMessageItem.mMessageType == PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND) {
            mIdNumCopyTv.setVisibility(View.GONE);
        }
        // Aurora xuyong 2015-08-07 added for bug #15949 end*/
    }

    @Override
    public void startAudio() {
        // TODO Auto-generated method stub
    }

    @Override
    public void startVideo() {
        // TODO Auto-generated method stub
    }
    // Aurora xuyong 2014-01-03 added for aurora;s new feature start
    public void initAttachChildViewVisibility () {
         if (mGnMmsView == null) {
             mGnMmsView = (FrameLayout)findViewById(R.id.gn_mms_view);
         }
         mGnMmsView.findViewById(R.id.gn_image_view).setVisibility(GONE);
         // Aurora xuyong 2014-05-26 added for multisim feature start
         if (MmsApp.mGnMultiSimMessage) {
             findViewById(R.id.aurora_sim_indi).setVisibility(GONE);
           // Aurora xuyong 2014-07-15 added for multisim feature start
             findViewById(R.id.aurora_sim_flag).setVisibility(GONE);
           // Aurora xuyong 2014-07-15 added for multisim feature end
         }
         // Aurora xuyong 2014-05-26 added for multisim feature end
          //findViewById(R.id.gn_image_clip_batch).setVisibility(GONE);
          //findViewById(R.id.aurora_bg_down_batch).setVisibility(GONE);
          findViewById(R.id.gn_image_view).setVisibility(GONE);
          //findViewById(R.id.gn_image_clip).setVisibility(GONE);
          findViewById(R.id.aurora_bg_down).setVisibility(GONE);
          mGnMmsView.setVisibility(GONE);
          mExternalViews = (LinearLayout)findViewById(R.id.aurora_external_views);
          if (mExternalViews != null) {
              mExternalViews.setVisibility(GONE);
          }
    }
    // Aurora xuyong 2014-01-03 added for aurora;s new feature end
    @Override
    public void setAudio(Uri audio, String name, Map<String, ?> extras) {
        // TODO Auto-generated method stub
        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (MmsApp.mGnMessageSupport) {
            inflateGnMmsView();
            mImageView.setVisibility(VISIBLE);
            //mGnMmsClip.setVisibility(VISIBLE);
            //mAttachDownInfo.setVisibility(VISIBLE);
            // Aurora xuyong 2014-04-29 modified for aurora's new feature start
            if (mImageView instanceof AuroraRoundImageView) {
                ((AuroraRoundImageView)mImageView).bindTextView(mAttachDownInfo);
            }
            // Aurora xuyong 2014-05-07 modified for bug 4693 start
            ComposeMessageActivity.mThumbnailWorker.loadImage(audio, mImageView, isAudio);
            // Aurora xuyong 2014-05-07 modified for bug 4693 end
            // Aurora xuyong 2014-04-29 modified for aurora's new feature end
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
    }

    @Override
    public void setImage(String name, Bitmap bitmap) {
        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (MmsApp.mGnMessageSupport) {
            inflateGnMmsView();
        } else {
            //gionee gaoj 2012-4-10 added for CR00555790 end
        inflateMmsView();

        //gionee gaoj 2012-4-10 added for CR00555790 start
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
        try {
           /* if (null == bitmap) {
                bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_missing_thumbnail_picture);
            }*/
            mImageView.setImageBitmap(bitmap);
            mImageView.setVisibility(VISIBLE);
            //gionee gaoj 2012-4-10 added for CR00555790 start
            //Aurora xuyong 2013-10-11 added for aurora's new feature start
            if (MmsApp.mGnMessageSupport) {
                //mGnMmsClip.setVisibility(GONE);
                // Aurora xuyong 2014-01-03 added for aurora;s new feature start
                mAttachDownInfo.setVisibility(GONE);
                // Aurora xuyong 2014-01-03 added for aurora;s new feature end
            }
            //Aurora xuyong 2013-10-11 added for aurora's new feature end
            //gionee gaoj 2012-4-10 added for CR00555790 end
        } catch (java.lang.OutOfMemoryError e) {
            Log.e(TAG, "setImage: out of memory: ", e);
        }
    }

    private void inflateMmsView() {
        if (mMmsView == null) {
            //inflate the surrounding view_stub
            findViewById(R.id.mms_layout_view_stub).setVisibility(VISIBLE);

            mMmsView = findViewById(R.id.mms_view);
            mImageView = (ImageView) findViewById(R.id.image_view);
            mSlideShowButton = (ImageButton) findViewById(R.id.play_slideshow_button);
        }
    }
    
    // Add for vCard begin
    private void hideFileAttachmentViewIfNeeded() {
        if (mFileAttachmentView != null) {
            mFileAttachmentView.setVisibility(View.GONE);
        }
    }

    private void importVCard(FileAttachmentModel attach) {
        final String[] filenames = mContext.fileList();
        for (String file : filenames) {
            if (file.endsWith(".vcf")) {
                mContext.deleteFile(file);
            }
        }
        try {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = mContext.getContentResolver().openInputStream(attach.getUri());
                out = mContext.openFileOutput(attach.getSrc(), Context.MODE_WORLD_READABLE);
                byte[] buf = new byte[8096];
                int seg = 0;
                while ((seg = in.read(buf)) != -1) {
                    out.write(buf, 0, seg);
                }
            } finally {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "importVCard, file not found " + attach + ", exception ", e);
        } catch (IOException e) {
            Log.e(TAG, "importVCard, ioexception " + attach + ", exception ", e);
        } catch (Exception e) {
            Log.e(TAG, "importVCard, unknown errror ", e);
        }
        final File tempVCard = mContext.getFileStreamPath(attach.getSrc());
        if (!tempVCard.exists() || tempVCard.length() <= 0) {
            Log.e(TAG, "importVCard, file is not exists or empty " + tempVCard);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(tempVCard), attach.getContentType().toLowerCase());
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        mContext.startActivity(intent);
    }

    private void showFileAttachmentView(ArrayList<FileAttachmentModel> files) {
        // There should be one and only one file
        if (files == null || files.size() != 1) {
            Log.e(TAG, "showFileAttachmentView, oops no attachment files found");
            return;
        }
        if (mFileAttachmentView == null) {
            findViewById(R.id.gn_mms_file_attachment_view_stub).setVisibility(VISIBLE);
            mFileAttachmentView = findViewById(R.id.file_attachment_view);
        }
        mFileAttachmentView.setVisibility(View.VISIBLE);
        final FileAttachmentModel attach = files.get(0);
        mFileAttachmentView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mSelectedBox != null && mSelectedBox.getVisibility() == View.VISIBLE) {         
                    return;
                }
                importVCard(attach);
            }
        });
        final ImageView thumb = (ImageView) mFileAttachmentView.findViewById(R.id.file_attachment_thumbnail);
        final TextView name = (TextView) mFileAttachmentView.findViewById(R.id.file_attachment_name_info);
        String nameText = null;
        int thumbResId = -1;
        if (attach.isVCard()) {
            nameText = mContext.getString(R.string.file_attachment_vcard_name, attach.getSrc());
            //thumbResId = R.drawable.ic_vcard_attach;
        }
        name.setText(nameText);
        thumb.setImageResource(thumbResId);
        final TextView size = (TextView) mFileAttachmentView.findViewById(R.id.file_attachment_size_info);
        size.setText(MessageUtils.getHumanReadableSize(attach.getAttachSize()));
    }
    // Add for vCard end

    private void inflateDownloadControls() {
        //gionee gaoj 2012-4-10 added for CR00555790 start
        if(MmsApp.mGnMessageSupport){
            //Aurora xuyong 2013-10-11 modified for aurora's new feature start
//            if (mAuroraMmsDownloadView == null) {        //Aurora junming 2014-4-24 del for bug(coder found) --one line
                // inflate the download controls
                //Aurora junming 2014-4-24 add for bug(coder found) start
                if (mAuroraMmsDownloadView == null){
                    mAuroraMmsDownloadView =  findViewById(R.id.gn_mms_downloading_view_stub);
                    mAuroraMmsDownloadView.setVisibility(VISIBLE);
                }
                //Aurora junming 2014-4-24 add for bug(coder found) end                
//                mAuroraMmsDownloadView =  findViewById(R.id.gn_mms_downloading_view_stub);//Aurora junming 2014-4-24 del for bug(coder found) --one line
//                mAuroraMmsDownloadView.setVisibility(VISIBLE);//Aurora junming 2014-4-24 del for bug(coder found) --one line
                mDownloadGnButton = (TextView) findViewById(R.id.gn_btn_download_msg);
                mDownloadingLabel = (TextView) findViewById(R.id.gn_label_downloading);
                mAuroraMmsSizeLabel = (TextView) findViewById(R.id.aurora_mms_size);
                mAuroraMmsDateLabel = (TextView) findViewById(R.id.aurora_mms_date_unu);
                PduPersister p = PduPersister.getPduPersister(this.getContext());
                Uri mMUri = this.getMessageItem().getMessageUri();
                int mMType = this.getMessageItem().getMessageType();
                if (PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND == mMType) {
                    try {
                        NotificationInd notifInd = (NotificationInd) p.load(mMUri);
                        mAuroraMmsSizeLabel.setText(this.getContext().getResources().getString(R.string.aurora_mms_size,
                                String.valueOf((notifInd.getMessageSize() + 1023) / 1024)));
                        mAuroraMmsDateLabel.setText(this.getContext().getResources().getString(
                                R.string.expire_on, DateFormat.format("yyyy-MM-dd", notifInd.getExpiry() * 1000L).toString()));
                    } catch (Exception e) {
                        
                    }
                }
            //Aurora xuyong 2013-10-11 modified for aurora's new feature end
//            }        //Aurora junming 2014-4-24 del for bug(coder found) --one line
        } else {
            //gionee gaoj 2012-4-10 added for CR00555790 end
        if (mDownloadButton == null) {
            //inflate the download controls
            findViewById(R.id.mms_downloading_view_stub).setVisibility(VISIBLE);
            mDownloadButton = (AuroraButton) findViewById(R.id.btn_download_msg);
            mDownloadingLabel = (TextView) findViewById(R.id.label_downloading);
        }
        //gionee gaoj 2012-4-10 added for CR00555790 start
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
    }
    
    private LeadingMarginSpan mLeadingMarginSpan;

    private LineHeightSpan mSpan = new LineHeightSpan() {
        @Override
        public void chooseHeight(CharSequence text, int start,
                int end, int spanstartv, int v, FontMetricsInt fm) {
            fm.ascent -= 10;
        }
    };

    TextAppearanceSpan mTextSmallSpan =
        new TextAppearanceSpan(mContext, android.R.style.TextAppearance_Small);

    ForegroundColorSpan mColorSpan = null;  // set in ctor
    
    private CharSequence formatMessage(MessageItem msgItem){
        return formatMessage(msgItem, msgItem.mContact, msgItem.mBody,
                msgItem.mSubject,
                msgItem.mHighlight, msgItem.mTextContentType);
    }

    private CharSequence formatMessage(MessageItem msgItem, String contact, String body,
                                       String subject, Pattern highlight,
                                       String contentType) {
        SpannableStringBuilder buf = new SpannableStringBuilder();
        boolean hasSubject = !TextUtils.isEmpty(subject);
        // Aurora xuyong 2014-08-05 added for multi-sim start
        boolean hasAddThumbnail = false;
        // Aurora xuyong 2014-08-05 added for multi-sim end
        SmileyParser parser = SmileyParser.getInstance();
        if (hasSubject) {
            CharSequence smilizedSubject = parser.addSmileySpans(subject);
            // Can't use the normal getString() with extra arguments for string replacement
            // because it doesn't preserve the SpannableText returned by addSmileySpans.
            // We have to manually replace the %s with our text.
            // Aurora xuyong 2014-08-05 added for multi-sim start
            // Aurora xuyong 2014-09025 modified for android 4.4 feature start
            if (MmsApp.mGnMultiSimMessage && mNeedShowSimIndi) {
            // Aurora xuyong 2014-09025 modified for android 4.4 feature end
                buf.append(TextUtils.replace(AURORA_THUMBNAIL + "  " +  mContext.getResources().getString(R.string.inline_subject),
                        new String[] { "%s" }, new CharSequence[] { smilizedSubject }));
                hasAddThumbnail = true;
            } else {
                buf.append(TextUtils.replace(mContext.getResources().getString(R.string.inline_subject),
                        new String[] { "%s" }, new CharSequence[] { smilizedSubject }));
            }
            // Aurora xuyong 2014-08-05 added for multi-sim end
            buf.replace(0, buf.length(), parser.addSmileySpans(buf));
        }

        if (!TextUtils.isEmpty(body)) {
            // Converts html to spannable if ContentType is "text/html".
            if (contentType != null && ContentType.TEXT_HTML.equals(contentType)) {
             // Aurora xuyong 2014-08-05 added for multi-sim start
             // Aurora xuyong 2014-09025 modified for android 4.4 feature start
                if (MmsApp.mGnMultiSimMessage && !hasAddThumbnail && mNeedShowSimIndi) {
             // Aurora xuyong 2014-09025 modified for android 4.4 feature end
                    buf.append("\n");
                buf.append(Html.fromHtml(AURORA_THUMBNAIL + "  " +  body));
                } else {
                    buf.append("\n");
                buf.append(Html.fromHtml(body));
                }
             // Aurora xuyong 2014-08-05 added for multi-sim end
            } else {
                if (hasSubject) {
                    buf.append(" - ");
                }
                // Aurora xuyong 2014-08-05 added for multi-sim start
                // Aurora xuyong 2014-09025 modified for android 4.4 feature start
                if (MmsApp.mGnMultiSimMessage && !hasAddThumbnail && mNeedShowSimIndi) {
                // Aurora xuyong 2014-09025 modified for android 4.4 feature end
                    buf.append(parser.addSmileySpans(AURORA_THUMBNAIL + "  " +  body));
                } else {
                        buf.append(parser.addSmileySpans(body));
                }
                // Aurora xuyong 2014-08-05 added for multi-sim end
            }
        }
        // Aurora xuyong 2014-09-15 deleted for aurora's new feature start
        //if (highlight != null) {
        //    Matcher m = highlight.matcher(buf.toString());
        //    while (m.find()) {
                // Aurora xuyong 2013-09-22 modified for Aurora's new feature start
                    // Aurora liugj 2013-09-23 modified for aurora's new feature start
                    // Aurora liugj 2013-10-10 modified for aurora's new feature start
       //         buf.setSpan(new ForegroundColorSpan(getContext().getResources().getColor(com.aurora.R.color.aurora_highlighted_color)), m.start(), m.end(), 0);
                    // Aurora liugj 2013-10-10 modified for aurora's new feature end
                // Aurora liugj 2013-09-23 modified for aurora's new feature end
                // Aurora xuyong 2013-09-22 modified for Aurora's new feature end
       //     }
       // }
       // Aurora xuyong 2014-09-15 deleted for aurora's new feature end
        //a0
        buf.setSpan(mLeadingMarginSpan, 0, buf.length(), 0);
        //a1
    // Aurora xuyong 2014-09-15 modified for aurora's new feature start
        return rebuildTextBody(buf.toString(), highlight);
    }
    // Aurora xuyong 2014-09-15 modified for aurora's new feature end

    // Aurora xuyong 2014-09-15 added for aurora's new feature start
    private Spannable rebuildTextBody(String body, Pattern highlight) {
        Spannable.Factory sf = Spannable.Factory.getInstance();
        Spannable sp = sf.newSpannable(body);
        if(Linkify.addLinks(sp, Linkify.ALL)) {
            URLSpan[] urlSpans = sp.getSpans(0, sp.length(), URLSpan.class);
            for (URLSpan urlSpan : urlSpans) {
                int start = sp.getSpanStart(urlSpan);
                int end   = sp.getSpanEnd(urlSpan);
                sp.removeSpan(urlSpan);
                AuroraURLSpan aURLSpan = new AuroraURLSpan(urlSpan.getURL());
                // Aurora xuyong 2015-02-04 added for bug #11531 start
                aURLSpan.setHandler(mHandler);
                // Aurora xuyong 2015-02-04 added for bug #11531 end
                sp.setSpan(aURLSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        if (highlight != null) {
            Matcher m = highlight.matcher(body.toString());
            while (m.find()) {
                sp.setSpan(new ForegroundColorSpan(getContext().getResources().getColor(com.aurora.R.color.aurora_highlighted_color)), m.start(), m.end(), 0);
            }
        }
        final String identifyNum = Utils.getUsefulCode(MessageListItem.this.getContext(), body);
        if (identifyNum != null && identifyNum.length() > 0) {
            mMessageItem.setIdentifyNumber(identifyNum);
            Pattern iDenPattern = Pattern.compile(identifyNum);
            Matcher mIN = iDenPattern.matcher(body.toString());
            while (mIN.find()) {
                // Aurroa xuyong 2016-01-25 modified for aurora 2.0 new feature start
                sp.setSpan(new ForegroundColorSpan(getContext().getResources().getColor(R.color.aurora_widget_tip_text_color)), mIN.start(), mIN.end(), 0);
                // Aurroa xuyong 2016-01-25 modified for aurora 2.0 new feature end
            }
        }
        return sp;
    }
    // Aurora xuyong 2014-09-15 added for aurora's new feature end
    private void drawPlaybackButton(MessageItem msgItem) {
        switch (msgItem.mAttachmentType) {
            case WorkingMessage.SLIDESHOW:
            case WorkingMessage.AUDIO:
            case WorkingMessage.VIDEO:
                // Show the 'Play' button and bind message info on it.
                mSlideShowButton.setTag(msgItem);
                //a0
                mSlideShowButton.setVisibility(View.GONE);
                Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), -1/*R.drawable.mms_play_btn*/); 
                if (msgItem.hasDrmContent()) {
                    if (FeatureOption.MTK_DRM_APP) {
                        Log.i(TAG," msgItem hasDrmContent"); 
                        Drawable front = mContext.getResources().getDrawable(-1/*R.drawable.drm_red_lock*/);
                        GnDrmManagerClient drmManager= new GnDrmManagerClient(mContext);
                        Bitmap drmBitmap = drmManager.overlayBitmap(bitmap, front);
                        mSlideShowButton.setImageBitmap(drmBitmap);
                        if (bitmap != null && !bitmap.isRecycled()) {
                            bitmap.recycle();
                            bitmap = null;
                        }
                    } else {
                        Log.i(TAG," msgItem hasn't DrmContent");
                        mSlideShowButton.setImageBitmap(bitmap);
                    }
                } else {
                    Log.i(TAG," msgItem hasn't DrmContent"); 
                    mSlideShowButton.setImageBitmap(bitmap);
                }
                //a1
                // Set call-back for the 'Play' button.
                mSlideShowButton.setOnClickListener(this);
                mSlideShowButton.setVisibility(View.VISIBLE);
                // gionee zhouyj 2012-07-16 remove for CR00640776 start 
                if(!MmsApp.mGnMessageSupport) {
                    setLongClickable(true);
                }
                // gionee zhouyj 2012-07-16 remove for CR00640776 end 

                // When we show the mSlideShowButton, this list item's onItemClickListener doesn't
                // get called. (It gets set in ComposeMessageActivity:
                // mMsgListView.setOnItemClickListener) Here we explicitly set the item's
                // onClickListener. It allows the item to respond to embedded html links and at the
                // same time, allows the slide show play button to work.
                // Aurora xuyong 2014-02-11 deleted for bug #1923 start
                /*setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onMessageListItemClick();
                    }
                });*/
                // Aurora xuyong 2014-02-11 deleted for bug #1923 end
                break;
            case WorkingMessage.IMAGE:
                if (msgItem.mSlideshow.get(0).hasText()) {
                    Log.d(TAG, "msgItem is image and text");
                    mSlideShowButton.setTag(msgItem);
                    mSlideShowButton.setVisibility(View.GONE);
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), -1/*R.drawable.mms_play_btn*/); 
                    if (msgItem.hasDrmContent()) {
                        if (FeatureOption.MTK_DRM_APP) {
                            Log.i(TAG," msgItem hasDrmContent"); 
                            Drawable front = mContext.getResources().getDrawable(-1/*R.drawable.drm_red_lock*/);
                            GnDrmManagerClient drmManager= new GnDrmManagerClient(mContext);
                            Bitmap drmBitmap = drmManager.overlayBitmap(bitmap, front);
                            mSlideShowButton.setImageBitmap(drmBitmap);
                            if (bitmap != null && !bitmap.isRecycled()) {
                                bitmap.recycle();
                                bitmap = null;
                            }
                        } else {
                            Log.i(TAG," msgItem hasn't DrmContent");
                            mSlideShowButton.setImageBitmap(bitmap);
                        }
                    } else {
                        Log.i(TAG," msgItem hasn't DrmContent"); 
                        mSlideShowButton.setImageBitmap(bitmap);
                    }
                    // Set call-back for the 'Play' button.
                    mSlideShowButton.setOnClickListener(this);
                    mSlideShowButton.setVisibility(View.VISIBLE);
                    // gionee zhouyj 2012-07-16 remove for CR00640776 start 
                    if(!MmsApp.mGnMessageSupport) {
                        setLongClickable(true);
                    }
                    // gionee zhouyj 2012-07-16 remove for CR00640776 end 
                    
                    // When we show the mSlideShowButton, this list item's onItemClickListener doesn't
                    // get called. (It gets set in ComposeMessageActivity:
                    // mMsgListView.setOnItemClickListener) Here we explicitly set the item's
                    // onClickListener. It allows the item to respond to embedded html links and at the
                    // same time, allows the slide show play button to work.
                    // Aurora xuyong 2014-02-11 deleted for bug #1923 start
                    /*setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            onMessageListItemClick();
                        }
                    });*/
                    // Aurora xuyong 2014-02-11 deleted for bug #1923 end
                } else {
                    mSlideShowButton.setVisibility(View.GONE);
                }
                break;
            default:
                mSlideShowButton.setVisibility(View.GONE);
                break;
        }
    }

    // OnClick Listener for the playback button
    @Override
    public void onClick(View v) {
        //a0
        //add for multi-delete
        if (mSelectedBox != null && mSelectedBox.getVisibility() == View.VISIBLE) {         
            return;
        }
        //a1
        MessageItem mi = (MessageItem) v.getTag();
        switch (mi.mAttachmentType) {
            case WorkingMessage.VIDEO:
            case WorkingMessage.IMAGE:
                if (mi.mSlideshow.get(0).hasText()) {
                    MessageUtils.viewMmsMessageAttachmentMini(mContext, mi.mMessageUri, mi.mSlideshow);
                } else {
                    MessageUtils.viewMmsMessageAttachment(mContext, mi.mMessageUri, mi.mSlideshow);
                }
                break;
            case WorkingMessage.AUDIO:
            case WorkingMessage.SLIDESHOW:
                MessageUtils.viewMmsMessageAttachment(mContext, mi.mMessageUri, null);
                break;
        }
    }
    // Aurora xuyong 2013-10-14 added for aurora's new feature start 
    private ArrayList<String> initComList(ArrayList<String> list) {
        ArrayList<String> nl = new ArrayList();
        for (String s : list) {
            if (!nl.contains(s)) {
                nl.add(s);
            }
        }
        return nl;
    }
    // Aurora xuyong 2013-10-14 added for aurora's new feature end
    public void onMessageListItemClick() {
        //a0
        //add for multi-delete
        if (mSelectedBox != null && mSelectedBox.getVisibility() == View.VISIBLE) {
            //gionee gaoj 2012-4-10 added for CR00555790 start
            if (MmsApp.mGnMessageSupport) {
                // Aurora xuyong 2013-12-17 modified for aurora's new feature start
                mSelectedBox.auroraSetChecked(!mSelectedBox.isChecked(), true);
                // Aurora xuyong 2013-12-17 modified for aurora's new feature end
            } else {
                //gionee gaoj 2012-4-10 added for CR00555790 end
            if (!mSelectedBox.isChecked()) {
                setSelectedBackGroud(true);
            } else {
                setSelectedBackGroud(false);
            }
            //gionee gaoj 2012-4-10 added for CR00555790 start
            }
            //gionee gaoj 2012-4-10 added for CR00555790 end
            if (null != mHandler) {
                Message msg = Message.obtain(mHandler, ITEM_CLICK);
                // Aurora xuyong 2014-05-08 added for bug #4718 start
                if (mMessageItem == null) {
                    return;
                }
                // Aurora xuyong 2014-05-08 added for bug #4718 end
                msg.arg1 = (int)(mMessageItem.mType.equals("mms")? -mMessageItem.mMsgId : mMessageItem.mMsgId);
                // Aurora xuyong 2014-02-11 added for bug #1923 start
                msg.arg2 = this.getPositionInParent();
                // Aurora xuyong 2014-02-11 added for bug #1923 end
                //gionee gaoj 2012-4-10 added for CR00555790 start
                if (MmsApp.mGnMessageSupport) {
                    msg.obj = mMessageItem;
                }
                //gionee gaoj 2012-4-10 added for CR00555790 end
                msg.sendToTarget();
            }
            return;
        }
        //a1
        
        // If the message is a failed one, clicking it should reload it in the compose view,
        // regardless of whether it has links in it
        //gionee gaoj 2012-5-4 added for CR00555790 start
        if (!MmsApp.mGnMessageSupport) {
        if (mMessageItem != null &&
                mMessageItem.isOutgoingMessage() &&
                mMessageItem.isFailedMessage() ) {
            recomposeFailedMessage();
            return;
        }
        }
        //gionee gaoj 2012-5-4 added for CR00555790 end

        //gionee gaoj 2012-4-28 added for CR00555790 start
        // Aurora xuyong 2014-09-15 deleted for aurora's new feature start
        /*if (mBodyTextView == null) {
            return;
        }
        //gionee gaoj 2012-4-28 added for CR00555790 end
        // Check for links. If none, do nothing; if 1, open it; if >1, ask user to pick one
        URLSpan[] spans = mBodyTextView.getUrls();
        //a0
        // Aurora xuyong 2013-10-14 modified for aurora's new feature start 
        java.util.ArrayList<String> urlsold = MessageUtils.extractUris(spans);
        final java.util.ArrayList<String> urls = initComList(urlsold);
        // Aurora xuyong 2013-10-14 modified for aurora's new feature end
        //gionee gaoj 2012-5-4 added for CR00555790 start
        if (!MmsApp.mGnMessageSupport) {
        final String telPrefix = "tel:";
        String url = ""; 
        for(int i=0;i<spans.length;i++) {
            url = urls.get(i);
            if(url.startsWith(telPrefix)) {
                mIsTel = true;
                urls.add("smsto:"+url.substring(telPrefix.length()));
            }
        }
        //gionee gaoj 2012-5-4 added for CR00555790 end
        }
        //a1
        // Aurora xuyong 2013-10-14 modified for aurora's new feature start 
        if (urls.size() == 0) {
        // Aurora xuyong 2013-10-14 modified for aurora's new feature end
            // Do nothing.
        //m0
        //} else if (spans.length == 1) {
        // Aurora xuyong 2013-10-14 modified for aurora's new feature start 
        } else if (urls.size() == 1 && !mIsTel) {
        // Aurora xuyong 2013-10-14 modified for aurora's new feature end
        //m1
            //gionee gaoj 2012-4-28 added for CR00555790 start
            if (MmsApp.mGnMessageSupport) {
                // Aurora xuyong 2014-06-06 added for bug #5497 start
                if (mMessageItem == null) {
                    return;
                }
                // Aurora xuyong 2014-06-06 added for bug #5497 end
                //Gionee <guoyx> <2013-05-23> add for CR00818517 begin
                Contact contact = Contact.get(mMessageItem.mAddress, true);
                mContact = contact.getName();
                mExistContactDB = contact.existsInDatabase();
                //Gionee <guoyx> <2013-05-30> add for CR00820739 begin
                mIndicatePhoneOrSim = contact.getIndicatePhoneOrSim();
                //Gionee <guoyx> <2013-05-30> add for CR00820739 end
                mContactUri = contact.getUri();
                //Gionee <guoyx> <2013-05-23> add for CR00818517 end
                //Aurora xuyong 2013-09-20 deleted for aurora's new feature start
                //alertDialog(MessageUtils.extractUris(spans).get(0));
                //Aurora xuyong 2013-09-20 deleted for aurora's new feature end
                //Aurora xuyong 2013-09-20 added for aurora's new feature start
                if (null != mHandler) {
                    Message msg = Message.obtain(mHandler, ITEM_BODY_CLICK);
                    ClickContent cc = new ClickContent();
                    // Aurora xuyong 2013-10-14 modified for aurora's new feature start 
                    cc.setValue(urls.get(0));
                    // Aurora xuyong 2013-10-14 modified for aurora's new feature end
                    cc.setContact(mContact);
                    cc.setExistContactDB(mExistContactDB);
                    cc.setIndicatePhoneOrSim(mIndicatePhoneOrSim);
                    cc.setContactUri(mContactUri);
                    msg.obj = cc;
                    msg.sendToTarget();
                }
                //Aurora xuyong 2013-09-20 added for aurora's new feature end
                return;
            }
            //gionee gaoj 2012-4-28 added for CR00555790 end
            /*
            Uri uri = Uri.parse(spans[0].getURL());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.putExtra(Browser.EXTRA_APPLICATION_ID, mContext.getPackageName());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            mContext.startActivity(intent);

            final String mUriTemp = spans[0].getURL();
            
            //MTK_OP01_PROTECT_START
            if (MmsApp.isTelecomOperator() && (!mUriTemp.startsWith("mailto:"))) {
                AuroraAlertDialog.Builder b = new AuroraAlertDialog.Builder(mContext);
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
                        
                        //gionee gaoj 2013-4-2 added for CR00792780 start
                        intent.setComponent(new ComponentName("com.android.contacts",
                        "com.android.contacts.activities.ContactDetailActivity"));
                        //gionee gaoj 2013-4-2 added for CR00792780 end
                        
                        intent.putExtra(Browser.EXTRA_APPLICATION_ID, mContext.getPackageName());
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                        mContext.startActivity(intent);
                    }
                });
                b.show();
            } else {
            //MTK_OP01_PROTECT_END

                Uri uri = Uri.parse(mUriTemp);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.putExtra(Browser.EXTRA_APPLICATION_ID, mContext.getPackageName());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                mContext.startActivity(intent);
                
            //MTK_OP01_PROTECT_START
            }
            //MTK_OP01_PROTECT_END
            
        } else {
            //gionee gaoj 2012-4-28 added for CR00555790 start
            // Aurora xuyong 2013-10-14 modified for aurora's new feature start 
            if (MmsApp.mGnMessageSupport && urls.size() == 1) {
                alertDialog(urls.get(0));
                return;
            }
            
            if (MmsApp.mGnMessageSupport) {
                ArrayList<String> lList = MessageUtils.extractUris(spans);
                // Aurora xuyong 2014-04-14 modified for bug #4159 start
                if (mMessageItem != null) {
                    Contact contact = Contact.get(mMessageItem.mAddress, true);
                    mContact = contact.getName();
                    mExistContactDB = contact.existsInDatabase();
                    mIndicatePhoneOrSim = contact.getIndicatePhoneOrSim();
                    mContactUri = contact.getUri();
                    if (null != mHandler) {
                        Message msg = Message.obtain(mHandler, ITEM_BODY_MULTI_CLICK);
                        ClickContent cc = new ClickContent();
                        cc.setValues(urls);
                        cc.setContact(mContact);
                        cc.setExistContactDB(mExistContactDB);
                        cc.setIndicatePhoneOrSim(mIndicatePhoneOrSim);
                        cc.setContactUri(mContactUri);
                        msg.obj = cc;
                        msg.sendToTarget();
                    }
                }
                // Aurora xuyong 2014-04-14 modified for bug #4159 end
            // Aurora xuyong 2013-10-14 modified for aurora's new feature end
                return;
            }
            //gionee gaoj 2012-4-28 added for CR00555790 end
            //m0
            //final java.util.ArrayList<String> urls = MessageUtils.extractUris(spans);
            //m1
            //gionee gaoj 2012-5-18 added for CR00601735 CR00640592 start
            int layout = -1;
            if (MmsApp.mTransparent) {
                layout = R.layout.gn_select_dialog_item;
            } else {
                layout = R.layout.gn_select_dialog_item_light_dark;
            }
            //gionee gaoj 2012-5-18 added for CR00601735 CR00640592 end
            ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(mContext, layout, urls) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View v = super.getView(position, convertView, parent);
                    try {
                        String url = getItem(position).toString();
                        TextView tv = (TextView) v;
                        //gionee gaoj 2012-4-10 added for CR00555790 start
                        if (MmsApp.mGnMessageSupport) {
                            tv.setSingleLine(true);
                            tv.setEllipsize(TruncateAt.END);
                        }
                        //gionee gaoj 2012-4-10 added for CR00555790 end
                        //gionee gaoj 2012-4-10 modified for CR00555790 start
                        Drawable d = null;
                        if (!MmsApp.mGnMessageSupport) {
                            d = mContext.getPackageManager().getActivityIcon(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        }
                        //gionee gaoj 2012-4-10 modified for CR00555790 end
                        if (d != null) {
                            d.setBounds(0, 0, d.getIntrinsicHeight(), d.getIntrinsicHeight());
                            tv.setCompoundDrawablePadding(10);
                            tv.setCompoundDrawables(d, null, null, null);
                        }
                        final String telPrefix = "tel:";
                        //a0
                        final String smsPrefix = "smsto:";
                        //a1
                        if (url.startsWith(telPrefix)) {
                            url = PhoneNumberUtils.formatNumber(
                                            url.substring(telPrefix.length()), mDefaultCountryIso);
                            if (url == null) {
                                Log.w(TAG,"url turn to null after calling PhoneNumberUtils.formatNumber");
                                url = getItem(position).toString().substring(telPrefix.length());
                            }
                        }
                        //a0
                        else if (url.startsWith(smsPrefix)) {
                            url = PhoneNumberUtils.formatNumber(
                                            url.substring(smsPrefix.length()), mDefaultCountryIso);
                            if (url == null) {
                                Log.w(TAG,"url turn to null after calling PhoneNumberUtils.formatNumber");
                                url = getItem(position).toString().substring(smsPrefix.length());
                            }
                        }
                        final String mailPrefix ="mailto";
                        if(url.startsWith(mailPrefix))
                        {
                            MailTo mt = MailTo.parse(url);
                            url = mt.getTo();
                        }
                        //a1
                        //gionee gaoj 2012-4-10 added for CR00555790 start
                        if (MmsApp.mGnMessageSupport) {
                            if (getFlag(url) == 3) {
                                url = url.substring(7);
                            }
                        }
                        //gionee gaoj 2012-4-10 added for CR00555790 end
                        tv.setText(url);
                    } catch (android.content.pm.PackageManager.NameNotFoundException ex) {
                        // it's ok if we're unable to set the drawable for this view - the user
                        // can still use it
                        //gionee gaoj 2012-4-10 added for CR00555790 start
                        if (MmsApp.mGnMessageSupport) {
                            String url = getItem(position).toString();
                            TextView tv = (TextView) v;
                            if (getFlag(url) == 3) {
                                tv.setText(url.substring(7));
                            }
                        }
                        //gionee gaoj 2012-4-10 added for CR00555790 end
                    }
                    return v;
                }
            };

            AuroraAlertDialog.Builder b = new AuroraAlertDialog.Builder(mContext);

            DialogInterface.OnClickListener click = new DialogInterface.OnClickListener() {
                @Override
                public final void onClick(DialogInterface dialog, int which) {
                    //gionee gaoj 2012-4-10 added for CR00555790 start
                    String url = urls.get(which);
                    if (MmsApp.mGnMessageSupport) {
                        alertDialog(url);
                    } else {
                        //gionee gaoj 2012-4-10 added for CR00555790 end
                    if (which >= 0) {
                        Uri uri = Uri.parse(urls.get(which));
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        intent.putExtra(Browser.EXTRA_APPLICATION_ID, mContext.getPackageName());
                        //a0
                        if (urls.get(which).startsWith("smsto:")) {
                            intent.setClassName(mContext, "com.android.mms.ui.SendMessageToActivity");
                        }
                        //a1
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                        mContext.startActivity(intent);
                    }
                    //gionee gaoj 2012-4-10 added for CR00555790 start
                    }
                    //gionee gaoj 2012-4-10 added for CR00555790 end
                    dialog.dismiss();
                }
            };

            b.setTitle(R.string.select_link_title);
            b.setCancelable(true);
            b.setAdapter(adapter, click);

//            b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//                @Override
//                public final void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                }
//            });

            //gionee gaoj 2012-4-10 added for CR00555790 start
            if (MmsApp.mGnMessageSupport) {
                //Gionee <guoyx> <2013-06-19> add for CR00821366 begin
                Contact contact = Contact.get(mMessageItem.mAddress, true);
                mContact = contact.getName();
                mExistContactDB = contact.existsInDatabase();
                mIndicatePhoneOrSim = contact.getIndicatePhoneOrSim();
                mContactUri = contact.getUri();
                //Gionee <guoyx> <2013-06-19> add for CR00821366 end
                //gionee gaoj added for CR00725602 20121201 start
                b.setCancelIcon(true);
                //gionee gaoj added for CR00725602 20121201 end
                mAlertDialog = b.show();
            } else {
                //gionee gaoj 2012-4-10 added for CR00555790 end
            b.show();
            //gionee gaoj 2012-4-10 added for CR00555790 start
            }
            //gionee gaoj 2012-4-10 added for CR00555790 end
        }*/
        // Aurora xuyong 2014-09-15 deleted for aurora's new feature end
    }
    // Aurora xuyong 2014-02-18 added for aurora's new feature start
    private long mLastClickTime = -1l;
    // Aurora xuyong 2014-02-18 added for aurora's new feature end

    private void setMediaOnClickListener(final MessageItem msgItem) {
        // gionee zhouyj 2012-06-12 add for CR00623647 start 
        // gionee zhouyj 2013-01-28 add for CR00767258 start 
        if (mIsDeleteMode) {
            mImageView.setLongClickable(false);
            mImageView.setClickable(false);
            return ;
        }
        // gionee zhouyj 2013-01-28 add for CR00767258 end 
        // Aurora xuyong 2014-02-25 deleted for bug #2589 start
        /*if(MessageUtils.mUnicomCustom && msgItem.mAttachmentType == WorkingMessage.AUDIO) {
            //Aurora xuyong 2013-10-11 modified for aurora's new feature start
            mGnMmsClip.setOnClickListener(new OnClickListener() {
            //Aurora xuyong 2013-10-11 modified for aurora's new feature end
                @Override
                public void onClick(View v) {
                    // Aurora xuyong 2014-02-18 added for aurora's new feature start
                    long currentClickTime = System.currentTimeMillis();
                    if (mLastClickTime == -1l) {
                        mLastClickTime = currentClickTime;
                    }
                    long duration = currentClickTime - mLastClickTime;
                    mLastClickTime = currentClickTime;
                    if (duration <= 500 && duration != 0) {
                        return;
                    }
                    // Aurora xuyong 2014-02-18 added for aurora's new feature end
                    if (mSelectedBox != null && mSelectedBox.getVisibility() == View.VISIBLE) {
                        // Aurora xuyong 2013-12-17 modified for aurora's new feature start
                        mSelectedBox.auroraSetChecked(!mSelectedBox.isChecked(), true);
                        // Aurora xuyong 2013-12-17 modified for aurora's new feature end 
                        if (mSelectedBox.isChecked()) {
                            setSelectedBackGroud(true);
                        } else {
                            setSelectedBackGroud(false);
                        }
                        if (null != mHandler) {
                            Message msg = Message.obtain(mHandler, ITEM_CLICK);
                            msg.arg1 = (int)(mMessageItem.mType.equals("mms")? -mMessageItem.mMsgId : mMessageItem.mMsgId);
                            if (MmsApp.mGnMessageSupport) {
                                msg.obj = msgItem;
                            }
                            msg.sendToTarget();
                        }
                        return;
                    }
                    if(MmsApp.mGnMessageSupport) {
                        MessageUtils.viewMmsMessageAttachment(mContext, msgItem.mMessageUri, msgItem.mSlideshow);
                    } else {
                    if (msgItem.mAttachmentType == WorkingMessage.IMAGE && msgItem.mSlideshow.get(0).hasText()) {
                        //Aurora xuyong 2013-10-11 modified for aurora's new feature start
                        mGnMmsClip.setOnClickListener(null);
                        //Aurora xuyong 2013-10-11 modified for aurora's new feature send
                    } else {
                        MessageUtils.viewMmsMessageAttachment(mContext, msgItem.mMessageUri, msgItem.mSlideshow);
                    }
                    }
                }
            });
            //Aurora xuyong 2013-09-20 deleted for aurora's new feature start
            // Aurora xuyong 2014-02-11 modified for bug #1923 start
            mImageView.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (null != mHandler) {
                        Message msg = Message.obtain(mHandler, ITEM_BODY_LONG_CLICK);
                        msg.arg1 = MessageListItem.this.getPositionInParent();
                        msg.sendToTarget();
                    }
                    return true;
                }
            });
            // Aurora xuyong 2014-02-11 modified for bug #1923 end
            //Aurora xuyong 2013-09-20 deleted for aurora's new feature end
            return ;
        }*/
        // Aurora xuyong 2014-02-25 deleted for bug #2589 end
        // gionee zhouyj 2012-06-12 add for CR00623647 end
        switch(msgItem.mAttachmentType) {
        case WorkingMessage.IMAGE:
            //Aurora xuyong 2013-10-11 added for aurora's new feature start
            mImageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Aurora xuyong 2014-02-18 added for aurora's new feature start
                    long currentClickTime = System.currentTimeMillis();
                    if (mLastClickTime == -1l) {
                        mLastClickTime = currentClickTime;
                    }
                    long duration = currentClickTime - mLastClickTime;
                    mLastClickTime = currentClickTime;
                    if (duration <= 500 && duration != 0) {
                        return;
                    }
                    // Aurora xuyong 2014-02-18 added for aurora's new feature end
                    if (mSelectedBox != null && mSelectedBox.getVisibility() == View.VISIBLE) {
                        // Aurora xuyong 2013-12-17 modified for aurora's new feature start
                        mSelectedBox.auroraSetChecked(!mSelectedBox.isChecked(), true);
                        // Aurora xuyong 2013-12-17 modified for aurora's new feature end

                        if (mSelectedBox.isChecked()) {
                            setSelectedBackGroud(true);
                        } else {
                            setSelectedBackGroud(false);
                        }

                        if (null != mHandler) {
                            Message msg = Message.obtain(mHandler, ITEM_CLICK);
                            // Aurora xuyong 2014-05-08 added for bug #4718 start
                            if (mMessageItem == null) {
                                return;
                            }
                            // Aurora xuyong 2014-05-08 added for bug #4718 end
                            msg.arg1 = (int)(mMessageItem.mType.equals("mms")? -mMessageItem.mMsgId : mMessageItem.mMsgId);
                            if (MmsApp.mGnMessageSupport) {
                                msg.obj = msgItem;
                            }
                            msg.sendToTarget();
                        }
                        return;
                    }
                    if(MmsApp.mGnMessageSupport) {
                        MessageUtils.viewMmsMessageAttachment(mContext, msgItem.mMessageUri, msgItem.mSlideshow);
                    } else {
                    if (msgItem.mAttachmentType == WorkingMessage.IMAGE && msgItem.mSlideshow.get(0).hasText()) {
                        mImageView.setOnClickListener(null);
                    } else {
                        MessageUtils.viewMmsMessageAttachment(mContext, msgItem.mMessageUri, msgItem.mSlideshow);
                    }
                    }
                }
            });
            // Aurora xuyong 2013-10-13 added for aurora's new feature start
            mImageView.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // Aurora xuyong 2014-02-11 modified for bug #1923 start
                    if (null != mHandler) {
                        Message msg = Message.obtain(mHandler, ITEM_BODY_LONG_CLICK);
                        msg.arg1 = MessageListItem.this.getPositionInParent();
                        msg.sendToTarget();
                    }
                    return true;
                    // Aurora xuyong 2014-02-11 modified for bug #1923 end
                }
            });
            // Aurora xuyong 2013-10-13 added for aurora's new feature end
            break;
            //Aurora xuyong 2013-10-11 added for aurora's new feature end
        // Aurora xuyong 2014-02-25 added for bug #2589 start
        case WorkingMessage.AUDIO:
             mImageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    long currentClickTime = System.currentTimeMillis();
                    if (mLastClickTime == -1l) {
                        mLastClickTime = currentClickTime;
                    }
                    long duration = currentClickTime - mLastClickTime;
                    mLastClickTime = currentClickTime;
                    if (duration <= 500 && duration != 0) {
                        return;
                    }
                    if (mSelectedBox != null && mSelectedBox.getVisibility() == View.VISIBLE) {
                        mSelectedBox.auroraSetChecked(!mSelectedBox.isChecked(), true);
                        if (mSelectedBox.isChecked()) {
                            setSelectedBackGroud(true);
                        } else {
                            setSelectedBackGroud(false);
                        }
                        if (null != mHandler) {
                            Message msg = Message.obtain(mHandler, ITEM_CLICK);
                            // Aurora xuyong 2014-05-08 added for bug #4718 start
                            if (mMessageItem == null) {
                                return;
                            }
                            // Aurora xuyong 2014-05-08 added for bug #4718 end
                            msg.arg1 = (int)(mMessageItem.mType.equals("mms")? -mMessageItem.mMsgId : mMessageItem.mMsgId);
                            if (MmsApp.mGnMessageSupport) {
                                msg.obj = msgItem;
                            }
                            msg.sendToTarget();
                        }
                        return;
                    }
                    if(MmsApp.mGnMessageSupport) {
                        MessageUtils.viewMmsMessageAttachment(mContext, msgItem.mMessageUri, msgItem.mSlideshow);
                    } else {
                    if (msgItem.mAttachmentType == WorkingMessage.IMAGE && msgItem.mSlideshow.get(0).hasText()) {
                        mImageView.setOnClickListener(null);
                    } else {
                        MessageUtils.viewMmsMessageAttachment(mContext, msgItem.mMessageUri, msgItem.mSlideshow);
                    }
                    }
                }
             });
             mImageView.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (null != mHandler) {
                        Message msg = Message.obtain(mHandler, ITEM_BODY_LONG_CLICK);
                        msg.arg1 = MessageListItem.this.getPositionInParent();
                        msg.sendToTarget();
                    }
                    return true;
                }
             });
             // Aurora xuyong 2014-02-28 added for aurora's new feature start
            mImageView.setOnLongClickListener(new OnLongClickListener() {
                 public boolean onLongClick(View v) {
                     // Aurora xuyong 2014-02-11 modified for bug #1923 start
                     if (null != mHandler) {
                         Message msg = Message.obtain(mHandler, ITEM_BODY_LONG_CLICK);
                         msg.arg1 = MessageListItem.this.getPositionInParent();
                         msg.sendToTarget();
                     }
                     return true;
                     // Aurora xuyong 2014-02-11 modified for bug #1923 end
                 }
             });
             // Aurora xuyong 2014-02-28 added for aurora's new feature end
             break;
        // Aurora xuyong 2014-02-25 added for bug #2589 end
        case WorkingMessage.VIDEO:
            //Aurora xuyong 2013-10-11 modified for aurora's new feature start
            mImageView.setOnClickListener(new OnClickListener() {
            //Aurora xuyong 2013-10-11 modified for aurora's new feature end
                @Override
                public void onClick(View v) {
                    // Aurora xuyong 2014-02-18 modified for aurora's new feature start
                    long currentClickTime = System.currentTimeMillis();
                    if (mLastClickTime == -1l) {
                        mLastClickTime = currentClickTime;
                    }
                    long duration = currentClickTime - mLastClickTime;
                    mLastClickTime = currentClickTime;
                    if (duration <= 500 && duration != 0) {
                        return;
                    }
                    // Aurora xuyong 2014-02-18 modified for aurora's new feature end
                    //a0
                    //add for multi-delete
                    if (mSelectedBox != null && mSelectedBox.getVisibility() == View.VISIBLE) {
                        // Aurora xuyong 2013-12-17 modified for aurora's new feature start
                        mSelectedBox.auroraSetChecked(!mSelectedBox.isChecked(), true); 
                        // Aurora xuyong 2013-12-17 modified for aurora's new feature end

                        if (mSelectedBox.isChecked()) {
                            setSelectedBackGroud(true);
                        } else {
                            setSelectedBackGroud(false);
                        }

                        if (null != mHandler) {
                            Message msg = Message.obtain(mHandler, ITEM_CLICK);
                            // Aurora xuyong 2014-05-08 added for bug #4718 start
                            if (mMessageItem == null) {
                                return;
                            }
                            // Aurora xuyong 2014-05-08 added for bug #4718 end
                            msg.arg1 = (int)(mMessageItem.mType.equals("mms")? -mMessageItem.mMsgId : mMessageItem.mMsgId);
                            //gionee gaoj 2012-4-10 added for CR00555790 start
                            if (MmsApp.mGnMessageSupport) {
                                msg.obj = msgItem;
                            }
                            //gionee gaoj 2012-4-10 added for CR00555790 end
                            msg.sendToTarget();
                        }
                        return;
                    }
                    //a1
                    
                    //m0
                    // gionee zhouyj 2012-05-07 added for CR00589134 start
                    if(MmsApp.mGnMessageSupport) {
                        MessageUtils.viewMmsMessageAttachment(mContext, msgItem.mMessageUri, msgItem.mSlideshow);
                    } else {
                    // gionee zhouyj 2012-05-07 added for CR00589134 end
                    //MessageUtils.viewMmsMessageAttachment(mContext, null, msgItem.mSlideshow);
                    if (msgItem.mAttachmentType == WorkingMessage.IMAGE && msgItem.mSlideshow.get(0).hasText()) {
                        //Aurora xuyong 2013-10-11 modified for aurora's new feature start
                        mImageView.setOnClickListener(null);
                        //Aurora xuyong 2013-10-11 modified for aurora's new feature end
                    } else {
                        MessageUtils.viewMmsMessageAttachment(mContext, msgItem.mMessageUri, msgItem.mSlideshow);
                    }
                    // gionee zhouyj 2012-05-07 added for CR00589134 start
                    }
                    // gionee zhouyj 2012-05-07 added for CR00589134 end
                    //m1
                }
            });
            //Aurora xuyong 2013-09-20 deleted for aurora's new feature start
            // Aurora xuyong 2013-10-13 modified for aurora's new feature start
            mImageView.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // Aurora xuyong 2014-02-11 modified for bug #1923 start
                    if (null != mHandler) {
                        Message msg = Message.obtain(mHandler, ITEM_BODY_LONG_CLICK);
                        msg.arg1 = MessageListItem.this.getPositionInParent();
                        msg.sendToTarget();
                    }
                    return true;
                    // Aurora xuyong 2014-02-11 modified for bug #1923 end
                }
            });
            mImageView.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // Aurora xuyong 2014-02-11 modified for bug #1923 start
                    if (null != mHandler) {
                        Message msg = Message.obtain(mHandler, ITEM_BODY_LONG_CLICK);
                        msg.arg1 = MessageListItem.this.getPositionInParent();
                        msg.sendToTarget();
                    }
                    return true;
                    // Aurora xuyong 2014-02-11 modified for bug #1923 end
                }
            });
            // Aurora xuyong 2013-10-13 modified for aurora's new feature end
            //Aurora xuyong 2013-09-20 deleted for aurora's new feature end
            break;

            //gionee gaoj 2012-4-10 added for CR00555790 start
        case WorkingMessage.SLIDESHOW:
            //Aurora xuyong 2013-10-11 modified for aurora's new feature start
            mImageView.setOnClickListener(new OnClickListener() {
            //Aurora xuyong 2013-10-11 modified for aurora's new feature end
                public void onClick(View v) {
                    // Aurora xuyong 2014-02-18 modified for aurora's new feature start
                    long currentClickTime = System.currentTimeMillis();
                    if (mLastClickTime == -1l) {
                        mLastClickTime = currentClickTime;
                    }
                    long duration = currentClickTime - mLastClickTime;
                    mLastClickTime = currentClickTime;
                    if (duration <= 500 && duration != 0) {
                        return;
                    }
                    // Aurora xuyong 2014-02-18 modified for aurora's new feature end
                    //add for multi-delete
                    if (mSelectedBox != null && mSelectedBox.getVisibility() == View.VISIBLE) {
                        // Aurora xuyong 2013-12-17 modified for aurora's new feature start
                        mSelectedBox.auroraSetChecked(!mSelectedBox.isChecked(), true);
                        // Aurora xuyong 2013-12-17 modified for aurora's new feature end
                        if (null != mHandler) {
                            Message msg = Message.obtain(mHandler, ITEM_CLICK);
                            // Aurora xuyong 2014-05-08 added for bug #4718 start
                            if (mMessageItem == null) {
                                return;
                            }
                            // Aurora xuyong 2014-05-08 added for bug #4718 end
                            msg.arg1 = (int)(mMessageItem.mType.equals("mms")? -mMessageItem.mMsgId : mMessageItem.mMsgId);
                            if (MmsApp.mGnMessageSupport) {
                                msg.obj = msgItem;
                            }
                            msg.sendToTarget();
                        }
                        return;
                    }
                    MessageUtils.viewMmsMessageAttachment(mContext,ContentUris.withAppendedId(Mms.CONTENT_URI, msgItem.mMsgId), null,
                            mMessageItem.mSubject, mMessageItem.mTimestamp);
                }
            });
            // Aurora xuyong 2014-01-03 modified for aurora;s new feature start
            mImageView.setOnClickListener(new OnClickListener() {
                //Aurora xuyong 2013-10-11 modified for aurora's new feature end
                    public void onClick(View v) {
                        // Aurora xuyong 2014-02-18 modified for aurora's new feature start
                        long currentClickTime = System.currentTimeMillis();
                        if (mLastClickTime == -1l) {
                            mLastClickTime = currentClickTime;
                        }
                        long duration = currentClickTime - mLastClickTime;
                        mLastClickTime = currentClickTime;
                        if (duration <= 500 && duration != 0) {
                            return;
                        }
                        // Aurora xuyong 2014-02-18 modified for aurora's new feature end
                        //add for multi-delete
                        if (mSelectedBox != null && mSelectedBox.getVisibility() == View.VISIBLE) {
                            // Aurora xuyong 2013-12-17 modified for aurora's new feature start
                            mSelectedBox.auroraSetChecked(!mSelectedBox.isChecked(), true);
                            // Aurora xuyong 2013-12-17 modified for aurora's new feature end
                            if (null != mHandler) {
                                Message msg = Message.obtain(mHandler, ITEM_CLICK);
                                // Aurora xuyong 2014-05-08 added for bug #4718 start
                                if (mMessageItem == null) {
                                    return;
                                }
                                // Aurora xuyong 2014-05-08 added for bug #4718 end
                                msg.arg1 = (int)(mMessageItem.mType.equals("mms")? -mMessageItem.mMsgId : mMessageItem.mMsgId);
                                if (MmsApp.mGnMessageSupport) {
                                    msg.obj = msgItem;
                                }
                                msg.sendToTarget();
                            }
                            return;
                        }
                        MessageUtils.viewMmsMessageAttachment(mContext,ContentUris.withAppendedId(Mms.CONTENT_URI, msgItem.mMsgId), null,
                                mMessageItem.mSubject, mMessageItem.mTimestamp);
                    }
                });
            // Aurora xuyong 2014-01-03 modified for aurora;s new feature end
            //Aurora xuyong 2013-09-20 deleted for aurora's new feature start
            // Aurora xuyong 2013-10-13 modified for aurora's new feature start
            mImageView.setOnLongClickListener(new OnLongClickListener() {
                public boolean onLongClick(View v) {
                    // Aurora xuyong 2014-02-11 modified for bug #1923 start
                    if (null != mHandler) {
                        Message msg = Message.obtain(mHandler, ITEM_BODY_LONG_CLICK);
                        msg.arg1 = MessageListItem.this.getPositionInParent();
                        msg.sendToTarget();
                    }
                    return true;
                    // Aurora xuyong 2014-02-11 modified for bug #1923 end
                }
            });
            // Aurora xuyong 2014-01-03 added for aurora;s new feature start
            mImageView.setOnLongClickListener(new OnLongClickListener() {
                public boolean onLongClick(View v) {
                    // Aurora xuyong 2014-02-11 modified for bug #1923 start
                    if (null != mHandler) {
                        Message msg = Message.obtain(mHandler, ITEM_BODY_LONG_CLICK);
                        msg.arg1 = MessageListItem.this.getPositionInParent();
                        msg.sendToTarget();
                    }
                    return true;
                    // Aurora xuyong 2014-02-11 modified for bug #1923 end
                }
            });
            // Aurora xuyong 2014-01-03 added for aurora;s new feature end
            // Aurora xuyong 2013-10-13 modified for aurora's new feature end
            //Aurora xuyong 2013-09-20 deleted for aurora's new feature end
            break;
            //gionee gaoj 2012-4-10 added for CR00555790 end
        default:
            //gionee gaoj 2012-4-10 added for CR00555790 start
            if (MmsApp.mGnMessageSupport) {
                //Aurora xuyong 2013-10-11 modified for aurora's new feature start
                mImageView.setOnClickListener(new OnClickListener() {
                //Aurora xuyong 2013-10-11 modified for aurora's new feature end
                    public void onClick(View v) {
                        // Aurora xuyong 2014-02-18 modified for aurora's new feature start
                        long currentClickTime = System.currentTimeMillis();
                        if (mLastClickTime == -1l) {
                            mLastClickTime = currentClickTime;
                        }
                        long duration = currentClickTime - mLastClickTime;
                        mLastClickTime = currentClickTime;
                        if (duration <= 500 && duration != 0) {
                            return;
                        }
                        // Aurora xuyong 2014-02-18 modified for aurora's new feature end
                        //add for multi-delete
                        if (mSelectedBox != null && mSelectedBox.getVisibility() == View.VISIBLE) {
                            // Aurora xuyong 2013-12-17 modified for aurora's new feature start
                            mSelectedBox.auroraSetChecked(!mSelectedBox.isChecked(), true);
                            // Aurora xuyong 2013-12-17 modified for aurora's new feature end
                            if (null != mHandler) {
                                Message msg = Message.obtain(mHandler, ITEM_CLICK);
                                // Aurora xuyong 2014-05-08 added for bug #4718 start
                                if (mMessageItem == null) {
                                        return;
                                }
                                // Aurora xuyong 2014-05-08 added for bug #4718 end
                                msg.arg1 = (int)(mMessageItem.mType.equals("mms")? -mMessageItem.mMsgId : mMessageItem.mMsgId);
                                msg.obj = msgItem;
                                msg.sendToTarget();
                            }
                            return;
                        }
                        Uri msg = ContentUris.withAppendedId(Mms.CONTENT_URI, msgItem.mMsgId);
                        final SlideshowModel model;
                        try {
                            model = SlideshowModel.createFromMessageUri(MessageListItem.this.getContext(), msg);
                            MediaModel mediaModel = model.get(0).getAudio();
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            String contentType;
                            if (mediaModel.isDrmProtected()) {
                                contentType = mediaModel.getDrmObject().getContentType();
                            } else {
                                String[] temp = mediaModel.getSrc().split("\\.");
                                MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                                contentType = mimeTypeMap.getMimeTypeFromExtension(temp[temp.length-1]);
                            }
                            // Aurora xuyong 2015-07-30 modified for bug #14494 start
                            File output = MessageUtils.copyPartsToOutputFile(MessageListItem.this.getContext(), mediaModel.getUri(), contentType);
                            intent.setDataAndType(Uri.fromFile(output), contentType);
                            // Aurora xuyong 2015-07-30 modified for bug #14494 end
                            MessageListItem.this.getContext().startActivity(intent);
                        } catch (Exception e) {
                            Log.e(TAG, "Cannot present the slides browser.", e);
                        }
                  }
              });
              //Aurora xuyong 2013-09-20 added for aurora's new feature start
              // Aurora xuyong 2013-10-13 modified for aurora's new feature start
                mImageView.setOnLongClickListener(new OnLongClickListener() {
                    public boolean onLongClick(View v) {
                        // Aurora xuyong 2014-02-11 modified for bug #1923 start
                        if (null != mHandler) {
                            Message msg = Message.obtain(mHandler, ITEM_BODY_LONG_CLICK);
                            msg.arg1 = MessageListItem.this.getPositionInParent();
                            msg.sendToTarget();
                        }
                        return true;
                        // Aurora xuyong 2014-02-11 modified for bug #1923 end
                    }
                });
                mImageView.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        // Aurora xuyong 2014-02-11 modified for bug #1923 start
                        if (null != mHandler) {
                            Message msg = Message.obtain(mHandler, ITEM_BODY_LONG_CLICK);
                            msg.arg1 = MessageListItem.this.getPositionInParent();
                            msg.sendToTarget();
                        }
                        return true;
                        // Aurora xuyong 2014-02-11 modified for bug #1923 end
                    }
                });
                // Aurora xuyong 2013-10-13 modified for aurora's new feature end
                //Aurora xuyong 2013-09-20 added for aurora's new feature end
            } else {
                //gionee gaoj 2012-4-10 added for CR00555790 end
            mImageView.setOnClickListener(null);
            //gionee gaoj 2012-4-10 added for CR00555790 start
            }
            //gionee gaoj 2012-4-10 added for CR00555790 end
            break;
        }
    }

    /**
     * Assuming the current message is a failed one, reload it into the compose view so that the
     * user can resend it.
     */
    private void recomposeFailedMessage() {
        String type = mMessageItem.mType;
        final int what;
        if (type.equals("sms")) {
            what = MSG_LIST_EDIT_SMS;
        } else {
            what = MSG_LIST_EDIT_MMS;
        }
        //a0
        //add for multi-delete
        if (mSelectedBox != null && mSelectedBox.getVisibility() == View.VISIBLE) {         
            return;
        }
        //a1
        if (null != mHandler) {
            Message msg = Message.obtain(mHandler, what);
            msg.obj = new Long(mMessageItem.mMsgId);
            msg.sendToTarget();
        }
    }

    private void drawRightStatusIndicator(MessageItem msgItem) {
        if (msgItem.mStar == true) {
            mGnFavorite.setVisibility(View.VISIBLE);
        } else {
            mGnFavorite.setVisibility(View.GONE);
        }
        if(!isItemIn){
            //gionee gaoj 2012-8-14 added for CR00623375 start
            if (MmsApp.mGnRegularlyMsgSend) {
                if (msgItem.isRegularlyMms()) {
                    //Gionee <guoyx> <2013-05-23> modify for CR00813219 begin
                    // gionee zhouyj 2012-12-25 modify for CR00747272 start 
                    if (!MmsApp.mGnMultiSimMessage || msgItem.getSimId() != -1) {
                        // Aurora xuyong 2013-12-30 modified for bug #1623 start
                        if (mRegularlyBtn != null) {
                            mRegularlyBtn.setVisibility(View.VISIBLE);
                        }
                        // Aurora xuyong 2013-12-30 modified for bug #1623 end
                        setRegularSendButClickListener(msgItem);
                    }
                    // gionee zhouyj 2012-12-25 modify for CR00747272 end 
                    //Gionee <guoyx> <2013-05-23> modify for CR00813219 end
                    return;
                } else {
                    // Aurora xuyong 2013-12-30 modified for bug #1623 start
                    if (mRegularlyBtn != null) {
                        mRegularlyBtn.setVisibility(View.GONE);
                    }
                    // Aurora xuyong 2013-12-30 modified for bug #1623 end
                }
            }
            //gionee gaoj 2012-8-14 added for CR00623375 end
            //gionee gaoj 2012-8-21 modified for CR00678365 start
            if (msgItem.isOutgoingMessage() && msgItem.isFailedMessage() && !msgItem.isRegularlyMms()) {
                //gionee gaoj 2012-8-21 modified for CR00678365 end
                setErrorIndicatorClickListener(msgItem);
                // Aurora xuyong 2013-09-13 deleted for aurora's new feature start
                //mRepeatBtn.setVisibility(View.VISIBLE);
                // Aurora xuyong 2013-09-13 deleted for aurora's new feature end
                // Aurora xuyong 2013-09-13 added for aurora's new feature start
                if (mIsDeleteMode) {
                    // Aurora xuyong 2013-12-30 modified for bug #1623 start
                    if (mSendFailIndi != null) {
                        mSendFailIndi.setVisibility(View.GONE);
                    }
                    // Aurora xuyong 2013-12-30 modified for bug #1623 end
                } else {
                    // Aurora xuyong 2013-12-30 modified for bug #1623 start
                    if (mSendFailIndi != null) {
                        mSendFailIndi.setVisibility(View.VISIBLE);
                    }
                    // Aurora xuyong 2013-09-23 added for aurora's new feature start
                    if (mMsgState != null) {
                        mMsgState.setVisibility(View.GONE);
                    }
                    // Aurora xuyong 2013-11-11 added for aurora's new feature start
                    if (mSendingIndi != null) {
                        mSendingIndi.setVisibility(View.GONE);
                    }
                    // Aurora xuyong 2013-12-30 modified for bug #1623 end
                    // Aurora xuyong 2013-11-11 added for aurora's new feature end
                    // Aurora xuyong 2013-09-23 added for aurora's new feature end
                }
                // Aurora xuyong 2013-09-13 added for aurora's new feature end
            } else if (msgItem.mDeliveryStatus == MessageItem.DeliveryStatus.FAILED) {
                setErrorIndicatorClickListener(msgItem);
                // Aurora xuyong 2013-09-13 deleted for aurora's new feature start
                //mRepeatBtn.setVisibility(View.VISIBLE);
                // Aurora xuyong 2013-09-13 deleted for aurora's new feature end
                // Aurora xuyong 2013-09-13 added for aurora's new feature start
                if (mIsDeleteMode) {
                    // Aurora xuyong 2013-12-30 modified for bug #1623 start
                    if (mSendFailIndi != null) {
                        mSendFailIndi.setVisibility(View.GONE);
                    }
                } else {
                    if (mSendFailIndi != null) {
                        mSendFailIndi.setVisibility(View.VISIBLE);
                    }
                    // Aurora xuyong 2013-09-23 added for aurora's new feature start
                    if (mMsgState != null) {
                        mMsgState.setVisibility(View.GONE);
                    }
                    // Aurora xuyong 2013-11-11 added for aurora's new feature start
                    if (mSendingIndi != null) {
                        mSendingIndi.setVisibility(View.GONE);
                    }
                    // Aurora xuyong 2013-12-30 modified for bug #1623 end
                    // Aurora xuyong 2013-11-11 added for aurora's new feature end
                    // Aurora xuyong 2013-09-23 added for aurora's new feature end
                }
                // Aurora xuyong 2013-09-13 added for aurora's new feature end
            } else {
                // Aurora xuyong 2013-09-13 deleted for aurora's new feature start
                //mRepeatBtn.setVisibility(View.GONE);
                // Aurora xuyong 2013-09-13 deleted for aurora's new feature start
                // Aurora xuyong 2013-09-13 added for aurora's new feature start
                // Aurora xuyong 2013-12-30 modified for bug #1623 start
                if (mSendFailIndi != null) {
                    mSendFailIndi.setVisibility(View.GONE);
                }
                // Aurora xuyong 2013-12-30 modified for bug #1623 end
                // Aurora xuyong 2013-09-13 added for aurora's new feature end
            }
            // Aurora xuyong 2013-09-13 deleted for aurora's new feature start
            /*if(mRepeatBtn.getVisibility() == View.VISIBLE){
                mBodyTextView.setMaxWidth(getItemWidth());
            }*/
            // Aurora xuyong 2013-09-13 deleted for aurora's new feature end
        }
    }

    @Override
    public void setImageRegionFit(String fit) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setImageVisibility(boolean visible) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setText(String name, String text) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setTextVisibility(boolean visible) {
        // TODO Auto-generated method stub
    }
    // Aurora xuyong 2014-03-05 added for aurora's new featrue start
    // Aurora xuyong 2014-03-07 modified for aurora's new feature start
    // Aurora xuyong 2014-04-25 added for bug #4301 start
    // Aurora xuyong 2014-04-29 modified for aurora's new feature start
    public static final int isImage = 0;
    public static final int isAudio = 1;
    public static final int isVideo = 2;
    // Aurora xuyong 2014-04-29 modified for aurora's new feature end
    // Aurora xuyong 2014-04-25 added for bug #4301 end
    // Aurora xuyong 2014-04-29 deleted for aurora's new feature start
    /*private class GetDurationRunnable implements Runnable {
        // Aurora xuyong 2014-04-25 added for bug #4301 start
        int mtype = -1;
       // Aurora xuyong 2014-04-25 added for bug #4301 end
        Context mContext;
        Uri mUri;
        boolean mIsVideo;
        // Aurora xuyong 2014-04-25 modified for bug #4301 start
        public GetDurationRunnable(Context context, Uri uri, int type) {
       // Aurora xuyong 2014-04-25 modified for bug #4301 end
    // Aurora xuyong 2014-03-07 modified for aurora's new feature end
            mContext =     context;
          // Aurora xuyong 2014-04-25 modified for bug #4301 start
            mUri = uri;
            mtype = type;
          // Aurora xuyong 2014-04-25 modified for bug #4301 end
        }
        
        @Override
        public void run() {
            // TODO Auto-generated method stub
            Bitmap bitmap = null;
          // Aurora xuyong 2014-04-25 modified for bug #4301 start
            int duration = -1;
            switch(mtype) {
            case isImage:
                if (null == mUri) {
                    bitmap = BitmapFactory.decodeResource(getResources(),
                            R.drawable.aurora_image_thumbnail);
                }
                bitmap = internalGetBitmap(mUri);
                break;
            case isAudio:
                bitmap = BitmapFactory.decodeResource(mContext.getResources(),
                        R.drawable.aurora_audio_thumbnail);
                duration = VideoAttachmentView.getMediaDuration(mContext, mUri);
                break;
            case isVideo:
          // Aurora xuyong 2014-04-25 modified for bug #4301 end
                bitmap = VideoAttachmentView.createVideoThumbnail(mContext, mUri);
                if (bitmap == null) {
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(),
                            R.drawable.aurora_video_thumbnail);
                }
             // Aurora xuyong 2014-04-25 added for bug #4301 start
                duration = VideoAttachmentView.getMediaDuration(mContext, mUri);
                break;
             // Aurora xuyong 2014-04-25 added for bug #4301 end
            }
          // Aurora xuyong 2014-04-25 deleted for bug #4301 start
            //int duration = VideoAttachmentView.getMediaDuration(mContext, mUri);
          // Aurora xuyong 2014-04-25 deleted for bug #4301 end
            BitmapAndDurationInfo info = new BitmapAndDurationInfo(duration);
            info.setBitmap(bitmap);
          // Aurora xuyong 2014-04-25 added for bug #4301 start
            info.setType(mtype);
          // Aurora xuyong 2014-04-25 added for bug #4301 end
            if (mMessageItem != null) {
                info.setUri(mUri);
            }
            Message msg = Message.obtain(mDhandler);
            if (mMessageItem != null) {
                msg.obj = info;
            }
            msg.sendToTarget();
        }
        
    }*/
    // Aurora xuyong 2014-04-29 deleted for aurora's new feature end
    // Aurora xuyong 2014-04-25 added for bug #4301 start
    private static final int THUMBNAIL_BOUNDS_LIMIT = 480;
    private Bitmap internalGetBitmap(Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = createThumbnailBitmap(THUMBNAIL_BOUNDS_LIMIT, uri);
        } catch (OutOfMemoryError ex) {
            // fall through and return a null bitmap. The callers can handle a null
            // result and show R.drawable.ic_missing_thumbnail_picture
        }
        return bitmap;
    }
    private Bitmap createThumbnailBitmap(int thumbnailBoundsLimit, Uri uri) {
        UriImage uriImage = new UriImage(mContext, uri);
        int outWidth = uriImage.getWidth();
        int outHeight = uriImage.getHeight();

        int s = 1;
        while ((outWidth / s > thumbnailBoundsLimit)
                || (outHeight / s > thumbnailBoundsLimit)) {
            s *= 2;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = s;

        InputStream input = null;
        InputStream inputForRotate = null;
        try {
            input = mContext.getContentResolver().openInputStream(uri);
            Bitmap b = BitmapFactory.decodeStream(input, null, options);
            return b;
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        } catch (OutOfMemoryError ex) {
            throw ex;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
    }
    // Aurora xuyong 2014-04-25 added for bug #4301 end
     // Aurora xuyong 2014-03-05 added for aurora's new featrue end
    // Aurora xuyong 2013-03-05 added for aurora's new feature start
    // Aurora xuyong 2014-03-07 modified for aurora's new feature start
   // Aurora xuyong 2014-04-29 deleted for aurora's new feature start
    /*private class BitmapAndDurationInfo {
    // Aurora xuyong 2014-03-07 modified for aurora's new feature end
        
        private WeakReference<Bitmap> mBitmapReference;
        private int mDuration = -1;
        private Uri mUri;
       // Aurora xuyong 2014-04-25 modified for bug #4301 start
        private int mType;
       // Aurora xuyong 2014-04-25 modified for bug #4301 end
        
        BitmapAndDurationInfo(int duration) {
            mDuration = duration;
        }
        
        public synchronized void setBitmap(Bitmap bitmap) {
            mBitmapReference = new WeakReference<Bitmap>(bitmap);
        }
        
        public void setDuration(int duration) {
            mDuration = duration;
        }
        
        public synchronized void setUri(Uri uri) {
            mUri = uri;
        }
        // Aurora xuyong 2014-04-25 modified for bug #4301 start
        public void setType(int type) {
            mType = type;
        }
        // Aurora xuyong 2014-04-25 modified for bug #4301 end
        public synchronized Bitmap getBitmap() {
            return mBitmapReference.get();
        }
        
        public int getDuration() {
            return mDuration;
        }
        
        public synchronized Uri getUri() {
            return mUri;
        }
        // Aurora xuyong 2014-04-25 modified for bug #4301 start
        public int getType() {
            return mType;
        }
       // Aurora xuyong 2014-04-25 modified for bug #4301 end
    }*/
    // Aurora xuyong 2014-04-29 deleted for aurora's new feature end
    // Aurora xuyong 2013-03-05 added for aurora's new feature end
    @Override
    public void setVideo(String name, Uri video) {
        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (MmsApp.mGnMessageSupport) {
            inflateGnMmsView();
            // Aurora xuyong 2014-04-29 added for aurora's new feature start
            mImageView.setVisibility(VISIBLE);
            //mGnMmsClip.setVisibility(VISIBLE);
            mAttachDownInfo.setVisibility(VISIBLE);
            if (mImageView instanceof AuroraRoundImageView) {
                ((AuroraRoundImageView)mImageView).bindTextView(mAttachDownInfo);
            }
            // Aurora xuyong 2014-05-07 modified for bug 4693 start
            ComposeMessageActivity.mThumbnailWorker.loadImage(video, mImageView, isVideo);
            // Aurora xuyong 2014-05-07 modified for bug 4693 end
            // Aurora xuyong 2014-04-29 added for aurora's new feature end
        } else {
            //gionee gaoj 2012-4-10 added for CR00555790 end
        inflateMmsView();
        //gionee gaoj 2012-4-10 added for CR00555790 start
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
    }

    @Override
    public void setVideoVisibility(boolean visible) {
        // TODO Auto-generated method stub
    }

    @Override
    public void stopAudio() {
        // TODO Auto-generated method stub
    }

    @Override
    public void stopVideo() {
        // TODO Auto-generated method stub
    }

    @Override
    public void reset() {
        if (mImageView != null) {
            mImageView.setVisibility(GONE);
        }
    }

    @Override
    public void setVisibility(boolean visible) {
        // TODO Auto-generated method stub
    }

    @Override
    public void pauseAudio() {
        // TODO Auto-generated method stub

    }

    @Override
    public void pauseVideo() {
        // TODO Auto-generated method stub

    }

    @Override
    public void seekAudio(int seekTo) {
        // TODO Auto-generated method stub

    }

    @Override
    public void seekVideo(int seekTo) {
        // TODO Auto-generated method stub

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
            // Aurora xuyong 2014-05-05 deleted for aurora's new feature start
            /*if (mAvatar.getPosition() == Divot.RIGHT_UPPER) {
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
            }*/
            // Aurora xuyong 2014-05-05 deleted for aurora's new feature end
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
    
    // Aurora xuyong 2014-03-05 added for aurora's new featrue start
    // Aurora xuyong 2014-04-29 deleted for aurora's new feature start
    /*Handler mDhandler = new Handler() {
        
        @Override
        public void handleMessage(Message msg) {
            // Aurora xuyong 2013-03-05 modified for aurora's new feature start
            BitmapAndDurationInfo info = (BitmapAndDurationInfo)(msg.obj);
            if (info == null || mMessageItem == null) {
                return;
            }
          // Aurora xuyong 2014-04-25 modified for bug #4301 start
            synchronized(info) {
                Bitmap bitmap = info.getBitmap();
                Uri uri = info.getUri();
                int type = info.getType();
                int duration = info.getDuration();
                // Aurora xuyong 2014-03-07 added for aurora's new feature start
                if (mImageView != null) {
                    switch(type) {
                    case isImage:
                        mImageView.setImageResource(R.drawable.aurora_image_thumbnail);
                        break;
                    case isAudio:
                        mImageView.setImageResource(R.drawable.aurora_audio_thumbnail);
                        break;
                    case isVideo:
                        mImageView.setImageResource(R.drawable.aurora_video_thumbnail);
                        break;
                    }
                }
                if (mAttachDownInfo != null) {
                    mAttachDownInfo.setText(VideoAttachmentView.initMediaDuration(duration));
                }
                // Aurora xuyong 2014-03-07 added for aurora's new feature end
                boolean needReplaceVideoImage = false;
                boolean needReplaceAudioImage = false;
                boolean needReplacePictureImage = false;
                SlideshowModel model = mMessageItem.mSlideshow;
                switch(type) {
                case isImage:
                    if (model != null) {
                        SlideModel smodel = model.get(0);
                        if (smodel != null) {
                            ImageModel im = smodel.getImage();
                            if (im != null) {
                          // Aurora xuyong 2014-04-28 modified for aurora's new feature start
                                Uri imageUri = im.getUri();
                                if (imageUri != null) {
                                    needReplacePictureImage = imageUri.equals(uri);
                                }
                          // Aurora xuyong 2014-04-28 modified for aurora's new feature end
                            }
                        }
                    }
                    break;
                case isAudio:
                    if (model != null) {
                        SlideModel smodel = model.get(0);
                        if (smodel != null) {
                            AudioModel am = smodel.getAudio();
                            if (am != null) {
                          // Aurora xuyong 2014-04-28 modified for aurora's new feature start
                                Uri audioUri = am.getUri();
                                if (audioUri != null) {
                                    needReplaceAudioImage = audioUri.equals(uri);
                                }
                          // Aurora xuyong 2014-04-28 modified for aurora's new feature end
                            }
                        }
                    }
                    break;
                case isVideo:
                    if (model != null) {
                        SlideModel smodel = model.get(0);
                        if (smodel != null) {
                            VideoModel vm = smodel.getVideo();
                            if (vm != null) {
                          // Aurora xuyong 2014-04-28 modified for aurora's new feature start
                                Uri videoUri = vm.getUri();
                                if (videoUri != null) {
                                    needReplaceVideoImage = videoUri.equals(uri);
                                }
                          // Aurora xuyong 2014-04-28 modified for aurora's new feature end
                            }
                        }
                    }
                    break;
                }
                // Aurora xuyong 2014-03-05 modified for aurora's new feature start
                if (needReplaceVideoImage || needReplaceAudioImage || needReplacePictureImage) {
                    
                    if (mImageView != null) {
                        // Aurora xuyong 2014-03-08 modified for aurora's new feature start
                        if (bitmap != null) {
                            mImageView.setImageBitmap(bitmap);
                        } else {
                            switch(type) {
                            case isImage:
                                mImageView.setImageResource(R.drawable.aurora_image_thumbnail);
                                break;
                            case isAudio:
                                mImageView.setImageResource(R.drawable.aurora_audio_thumbnail);
                                break;
                            case isVideo:
                                mImageView.setImageResource(R.drawable.aurora_video_thumbnail);
                                break;
                            }
                        }
                        // Aurora xuyong 2014-03-08 modified for aurora's new feature end
                    }
                // Aurora xuyong 2014-03-05 modified for aurora's new feature end
                } else {
                    if (mImageView != null) {
                        if (needReplaceVideoImage) {
                            mImageView.setImageResource(R.drawable.aurora_video_thumbnail);
                        } else if (needReplaceAudioImage) {
                            mImageView.setImageResource(R.drawable.aurora_audio_thumbnail);
                        } else if (needReplacePictureImage) {
                            mImageView.setImageResource(R.drawable.aurora_image_thumbnail);
                        }
                    }
                // Aurora xuyong 2013-03-05 modified for aurora's new feature end
                }
          // Aurora xuyong 2014-04-25 modified for bug #4301 end
            }
        }
    };*/
    // Aurora xuyong 2014-04-29 deleted for aurora's new feature end
    // Aurora xuyong 2014-03-05 added for aurora's new featrue end
    //a0
    static final int ITEM_CLICK          = 5;
    static final int ITEM_MARGIN         = 50;
    //Aurora xuyong 2013-09-20 added for aurora's new feature start
    static final int ITEM_BODY_CLICK     = 500;
    // Aurora xuyong 2013-10-14 added for aurora's new feature start 
    static final int ITEM_BODY_MULTI_CLICK     = 501;
    // Aurora xuyong 2014-02-11 added for bug #1923 start
    static final int ITEM_BODY_LONG_CLICK      = 502;
    // Aurora xuyong 2014-02-11 added for bug #1923 end
    // Aurora xuyong 2013-10-14 added for aurora's new feature end
    //Aurora xuyong 2013-09-20 added for aurora's new feature end
    //Aurora xuyong 2013-10-11 added for aurora's new feature start
    // Aurora xuyong 2014-03-05 modified for aurora's new featrue start
    public static final int ITEM_SHOW_DETAIL    = 503;
    public static final int ITEM_COPY_IDENTIFY_NUM          = 504;
    // Aurora xuyong 2014-03-05 modified for aurora's new featrue end
    //Aurora xuyong 2013-10-11 added for aurora's new feature end
    private TextView mSimStatus;
    // Aurora xuyong 2013-12-17 modified for aurora's new feature start
    private AuroraCheckBox mSelectedBox;
    // Aurora xuyong 2013-12-27 added for aurora's new feature start
    // Aurora xuyong 2015-05-08 deleted for bug #13338 start
    //private LinearLayout mParentLayout;
    // Aurora xuyong 2015-05-08 deleted for bug #13338 end
    // Aurora xuyong 2013-12-27 added for aurora's new feature end
    // Aurora xuyong 2013-12-17 modified for aurora's new feature end
    private boolean mIsTel = false;

    private CharSequence formatTimestamp(MessageItem msgItem, String timestamp) {
        SpannableStringBuilder buf = new SpannableStringBuilder();
        if (msgItem.isSending()) {
            timestamp = mContext.getResources().getString(R.string.sending_message);
        }

           buf.append(TextUtils.isEmpty(timestamp) ? " " : timestamp);        
           buf.setSpan(mSpan, 1, buf.length(), 0);
           
        //buf.setSpan(mTextSmallSpan, 0, buf.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Make the timestamp text not as dark
        buf.setSpan(mColorSpan, 0, buf.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        return buf;
    }

    private CharSequence formatSimStatus(MessageItem msgItem ) {
        SpannableStringBuilder buffer = new SpannableStringBuilder();
        // If we're in the process of sending a message (i.e. pending), then we show a "Sending..."
        // string in place of the timestamp.
        //Add sim info
        int simInfoStart = buffer.length();
        CharSequence simInfo = MessageUtils.getSimInfo(mContext, msgItem.mSimId);
        if(simInfo.length() > 0){
            if (msgItem.mBoxId == TextBasedSmsColumns.MESSAGE_TYPE_INBOX) {
                buffer.append(" ");
                buffer.append(mContext.getString(R.string.via_without_time_for_recieve));
            } else {
                buffer.append(" ");
                buffer.append(mContext.getString(R.string.via_without_time_for_send));
            }
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
            // Aurora xuyong 2013-12-17 modified for aurora's new feature start
            // Aurora xuyong 2013-12-27 modified for aurora's new feature start
            // Aurora xuyong 2014-03-28 modified for bug #3676 start
            mSelectedBox.auroraSetChecked(true, false);
            // Aurora xuyong 2014-03-28 modified for bug #3676 end
            // Aurora xuyong 2013-12-27 modified for aurora's new feature end
            // Aurora xuyong 2013-12-17 modified for aurora's new feature end
            mSelectedBox.setBackgroundDrawable(null);
            //gionee gaoj 2012-5-9 added for CR00588933 start
            if (!MmsApp.mGnMessageSupport) {
            mMessageBlock.setBackgroundDrawable(null);
            mDateView.setBackgroundDrawable(null);
            }
            //gionee gaoj 2012-5-9 added for CR00588933 end
            //setBackgroundResource(R.drawable.list_selected_holo_light);
        } else {
            setBackgroundDrawable(null);
            // Aurora xuyong 2013-12-17 modified for aurora's new feature start
            // Aurora xuyong 2013-12-27 modified for aurora's new feature start
            // Aurora xuyong 2014-03-28 modified for bug #3676 start
            mSelectedBox.auroraSetChecked(false, false);
            // Aurora xuyong 2014-03-28 modified for bug #3676 end
            // Aurora xuyong 2013-12-27 modified for aurora's new feature end
            // Aurora xuyong 2013-12-17 modified for aurora's new feature end
            //gionee gaoj 2012-5-9 added for CR00588933 start
            if (!MmsApp.mGnMessageSupport) {
            mSelectedBox.setBackgroundResource(R.drawable.listitem_background);
            mMessageBlock.setBackgroundResource(R.drawable.listitem_background);
            mDateView.setBackgroundResource(R.drawable.listitem_background);
            }
            //gionee gaoj 2012-5-9 added for CR00588933 end
        }
    }

//MTK_OP01_PROTECT_START
    // add for text zoom
    private final float DEFAULT_TEXT_SIZE = 20;
    private float mTextSize = DEFAULT_TEXT_SIZE;
    public void setTextSize(float size) {
        mTextSize = size;
    }
//MTK_OP01_PROTECT_END
    public void bindDefault(boolean isLastItem) {
        Log.d(M_TAG, "bindDefault()");
        //gionee gaoj 2012-9-20 added for CR00699291 start
        if (MmsApp.mGnMessageSupport) {
            gnbindDefault(isLastItem);
            return;
        }
        //gionee gaoj 2012-9-20 added for CR00699291 end
        mIsLastItemInList = isLastItem;
        mSelectedBox.setVisibility(View.GONE);
        // Aurora xuyong 2015-04-23 added for aurora's new feature start
        //mBatchTimeFormat.setVisibility(View.GONE);
        // Aurora xuyong 2015-04-23 added for aurora's new feature end
        setLongClickable(false);
        setFocusable(false);
        setClickable(false);

        if (mMmsView != null) {
            mMmsView.setVisibility(View.GONE);
        }
        if (mFileAttachmentView != null) {
            mFileAttachmentView.setVisibility(View.GONE);
        }
        //mBodyTextView.setText(R.string.refreshing);
        //Aurora xuyong 2013-10-11 added for aurora's new feature start
        // Aurora xuyong 2013-10-12 modified for aurora's new feature start
        // Aurora xuyong 2013-10-18 modified for aurora's new feature start 
        // Aurora xuyong 2013-10-24 modified for aurora's new feature start
        // Aurora liugj 2013-10-31 modified for fix bug-331/332 start 
       /*if (mBodyTextView.getText().toString().getBytes().length > 580) {
        // Aurora xuyong 2013-10-24 modified for aurora's new feature end
        // Aurora xuyong 2013-10-18 modified for aurora's new feature end
            // Aurora xuyong 2013-10-24 modified for aurora's new feature start
            mBodyTextView.setMaxLines(18);
            // Aurora xuyong 2013-10-24 modified for aurora's new feature end
            mShowDetail.setVisibility(View.VISIBLE);
        // Aurora xuyong 2013-10-12 modified for aurora's new feature end
        } else {
            mShowDetail.setVisibility(View.GONE);
        }*/
        // Aurora liugj 2013-10-31 modified for fix bug-331/332 end
        //Aurora xuyong 2013-10-11 added for aurora's new feature end
//MTK_OP01_PROTECT_START
        // add for text zoom
        // Aurora xuyong 2013-09-13 deleted for aurora's new feature start
        /*if (MmsApp.isTelecomOperator()) {
            mBodyTextView.setTextSize(mTextSize);
        }*/
        // Aurora xuyong 2013-09-13 deleted for aurora's new feature end
//MTK_OP01_PROTECT_END

        mDateView.setVisibility(View.GONE);
        mSimStatus.setVisibility(View.GONE);
        if (mDownloadButton != null) {
            //Aurora xuyong 2013-10-11 modified for aurora's new feature start
            //mDownloadingLabel.setVisibility(View.GONE);
            //mDownloadButton.setVisibility(View.GONE);
            mAuroraDownloadMms.setVisibility(View.GONE);
            mAuroraMmsSizeLabel.setVisibility(View.GONE);
            mAuroraMmsDateLabel.setVisibility(View.GONE);
            // Aurora xuyong 2013-11-11 added for aurora's new feature start
            mAuroraDownloading.setVisibility(View.GONE);
            // Aurora xuyong 2013-11-11 added for aurora's new feature end
            //Aurora xuyong 2013-10-11 modified for aurora's new feature end
        }
        //mLockedIndicator.setVisibility(View.GONE);
        mSimStatus.setPadding(PADDING_LEFT_TWE, 0, 0, 0);
        mDeliveredIndicator.setVisibility(View.GONE);
        mDetailsIndicator.setVisibility(View.GONE);
        // Aurora xuyong 2014-05-05 deleted for aurora's new feature start
        //mAvatar.setImageDrawable(sDefaultContactImage);
        // Aurora xuyong 2014-05-05 deleted for aurora's new feature end
        requestLayout();
    }
    //a1
    //gionee gaoj 2012-4-10 added for CR00555790 start
    // Aurora xuyong 2013-10-18 modified for bug #47 start
    // Aurora xuyong 2016-03-28 modified for bug #21852 start
    private long mLastMsgTime = -1;
    // Aurora xuyong 2016-03-28 modified for bug #21852 end

    public void setLastTime(long strTime) {
    // Aurora xuyong 2013-10-18 modified for bug #47 end
        mLastMsgTime = strTime;
    }
    // Aurora xuyong 2015-04-23 added for aurora's new feature start
    private String mLastWeatherInfo;
    public void setLastWeatherInfo(String weatherInfo) {
        mLastWeatherInfo = weatherInfo;
    }
    // Aurora xuyong 2015-04-23 added for aurora's new feature end

    //gionee gaoj 2012-8-22 added for CR00679009 start
    private boolean mLastMsgFailed = false;

    public void setLastMsgFailed(boolean failed) {
        mLastMsgFailed = failed;
    }
    //gionee gaoj 2012-8-22 added for CR00679009 end

    public static void resetDialog() {
        if ((null != mAlertDialog) && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }
    /*private void displayGnPanel(int msgBoxId) {
        mMsgListItemLayoutParent = (LinearLayout) findViewById(R.id.gn_msg_item_content);
        switch (msgBoxId) {
            case Mms.MESSAGE_BOX_INBOX:
                inflateGnMsgIn();
                break;
            case Mms.MESSAGE_BOX_DRAFTS:
            case Sms.MESSAGE_TYPE_FAILED:
            case Sms.MESSAGE_TYPE_QUEUED:
            case Mms.MESSAGE_BOX_OUTBOX:
            default:
                inflateGnMsgOut();
                break;
        }
        mTimeFormat = (TextView) findViewById(R.id.gn_msg_time_text);
        //add for multi-delete
        mSelectedBox = (CheckBox)findViewById(R.id.gn_select_check_box);
    }*/

    /*private void inflateGnMsgIn() {
        if (mGnInMsgPanel != null) {
            mGnInMsgPanel = null;
        }
        if (mGnOutMsgPanel != null) {
            mGnOutMsgPanel.setVisibility(View.GONE);
            mGnOutMsgPanel = null;
        }
        mGnInMsgPanel = (RelativeLayout) findViewById(R.id.gn_mms_layout_view_parent_in);
        mGnInMsgPanel.setVisibility(View.VISIBLE);
        mMsgSimCard = (TextView) findViewById(R.id.message_incoming_sim_card);
        mMsgListItemLayout = (LinearLayout) findViewById(R.id.mms_layout_view_parent_in);
        // gionee zhouyj 2012-07-16 remove for CR00640776 start 
        //mMsgListItemLayout.setLongClickable(true);
        // gionee zhouyj 2012-07-16 remove for CR00640776 end 
        mBodyTextView = (TextView) findViewById(R.id.gn_text_view_in);
        mLockedIndicator = (ImageView) findViewById(R.id.locked_indicator);
        mGnFavorite = (ImageView) findViewById(R.id.favorite_indicator);
        mGnAvatarIn = (QuickContactDivot) findViewById(R.id.gn_avatar_in);
    }*/

    /*private void inflateGnMsgOut() {
        if (mGnOutMsgPanel != null) {
            mGnOutMsgPanel = null;
        }
        if (mGnInMsgPanel != null) {
            mGnInMsgPanel.setVisibility(View.GONE);
            mGnInMsgPanel = null;
        }
        mGnOutMsgPanel = (RelativeLayout) findViewById(R.id.gn_mms_layout_view_parent_out);
        mGnOutMsgPanel.setVisibility(View.VISIBLE);
        mMsgItemOutPanel = (LinearLayout) findViewById(R.id.gn_msg_out);
        mMsgSimCard = (TextView) findViewById(R.id.message_outing_sim_card);
        mMsgState = (TextView) findViewById(R.id.deliver_status_text);
        mMsgListItemLayout = (LinearLayout) findViewById(R.id.mms_layout_view_parent_out);
        // gionee zhouyj 2012-07-16 remove for CR00640776 start 
        //mMsgListItemLayout.setLongClickable(true);
        // gionee zhouyj 2012-07-16 remove for CR00640776 end 
        mBodyTextView = (TextView) findViewById(R.id.gn_text_view_out);
        mLockedIndicator = (ImageView) findViewById(R.id.gn_locked_indicator_out);
        mGnFavorite = (ImageView) findViewById(R.id.gn_favorite_indicator_out);
        mRepeatBtn = (TextView) findViewById(R.id.gn_repeat_btn);
        mGnAvatarOut = (QuickContactDivot) findViewById(R.id.gn_avatar_out);
        //gionee gaoj 2012-8-14 added for CR00623375 start
        if (MmsApp.mGnRegularlyMsgSend) {
            mRegularlyBtn = (TextView) findViewById(R.id.gn_regularly_btn);
        }
        //gionee gaoj 2012-8-14 added for CR00623375 end
    }*/
    // Aurora xuyong 2014-05-26 added for multisim feature start
    // Aurora xuyong 2014-07-14 modified for aurora's new feature start
    private void setProperSImIcon(ImageView flagView, TextView view, int simId) {
       // Aurora xuyong 2014-07-15 added for multisim feature start
        if (view.getText() == null || view.getText().length() <= 0) {
            return;
        }
       // Aurora xuyong 2014-07-15 added for multisim feature end
        int drawableId = MessageUtils.getSimBigIcon(this.getContext(), simId);
        // Aurora yudingmin 2014-10-27 modified for sim start
        if(drawableId > 0){
            flagView.setVisibility(View.VISIBLE);
            flagView.setImageResource(drawableId);
        } else {
            // Aurora xuyong 2015-03-25 modified for aurora's new feature start
            flagView.setVisibility(View.VISIBLE);
            flagView.setImageResource(R.drawable.aurora_sim_not_found);
            // Aurora xuyong 2015-03-25 modified for aurora's new feature end
        }
        // Aurora yudingmin 2014-10-27 modified for sim end
       // Aurora xuyong 2014-07-15 modified for multisim feature start
      // Aurora xuyong 2014-08-05 modified for multi-sim start
//        view.setText(formatMessage(mMessageItem, mMessageItem.mContact, mMessageItem.mBody,
//                mMessageItem.mSubject, mMessageItem.mHighlight, mMessageItem.mTextContentType));
        // Aurora xuyong 2014-09-27 added for aurora's new feature start
       // Aurora xuyong 2014-09-27 added for aurora's new feature end
      // Aurora xuyong 2014-08-05 modified for multi-sim end
       // Aurora xuyong 2014-07-15 modified for multisim feature end
    }
    // Aurora xuyong 2014-07-14 modified for aurora's new feature end
    // Aurora xuyong 2014-09025 modified for android 4.4 feature start
    private boolean mNeedShowSimIndi = true;
    private int mSimIndiDrawableId = -1;
    private int getIndiDrawableId(int simId) {
        return MessageUtils.getSimBigIcon(this.getContext(), simId);
    }
    private void setProperImageIndi(ImageView view) {
        if (mNeedShowSimIndi) {
            view.setImageResource(mSimIndiDrawableId);
        // Aurora xuyong 2014-01-21 added for bug #11259 start
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        // Aurora xuyong 2014-01-21 added for bug #11259 end
        }
    }
    // Aurora xuyong 2014-09025 modified for android 4.4 feature end
    // Aurora xuyong 2014-05-26 added for multisim feature end
    private void inflateGnMmsView() {
        if (mGnMmsView == null) {
            mGnMmsView = (FrameLayout)findViewById(R.id.gn_mms_view);
        // Aurora xuyong 2013-10-21 modified for aurora's new feature start
        }
        //mImageView = (ImageView) findViewById(R.id.gn_image_view);
        // Aurora xuyong 2013-11-14 modified for aurora's new feature start
        // Aurora xuyong 2013-11-16 modified for aurora's new feature start
        /*if ((this.getMessageItem().mBody != null && this.getMessageItem().mBody.length() > 0)
                || (mMessageItem.mSubject != null && mMessageItem.mSubject.length() > 0)) {
        // Aurora xuyong 2013-11-16 modified for aurora's new feature end
        // Aurora xuyong 2013-11-14 modified for aurora's new feature s end
            mImageView = (ImageView) mGnMmsView.findViewById(R.id.gn_image_view_batch);
          // Aurora xuyong 2014-05-26 added for multisim feature start
            mSimIndicator = (ImageView) mGnMmsView.findViewById(R.id.aurora_sim_indi);
            mSimIndicator.setVisibility(GONE);
          // Aurora xuyong 2014-05-26 added for multisim feature end
            // Aurora xuyong 2013-10-24 modified for aurora's new feature start
            mGnMmsClip = (ImageView) findViewById(R.id.gn_image_clip_batch);
            // Aurora xuyong 2013-10-24 modified for aurora's new feature end
            // Aurora xuyong 2014-01-03 added for aurora;s new feature start
            mAttachDownInfo = (TextView) findViewById(R.id.aurora_bg_down_batch);
            // Aurora xuyong 2014-01-03 added for aurora;s new feature end
        } else {*/
            mImageView = (ImageView) mGnMmsView.findViewById(R.id.gn_image_view);
          // Aurora xuyong 2014-05-26 added for multisim feature start
            mSimIndicator = (ImageView) mGnMmsView.findViewById(R.id.aurora_sim_indi);
            if (MmsApp.mGnMultiSimMessage) {
             // Aurora xuyong 2014-09025 modified for android 4.4 feature start
                setProperImageIndi(mSimIndicator);
             // Aurora xuyong 2014-09025 modified for android 4.4 feature end
                // Aurora xuyong 2014-01-21 deleted for bug #11259 start
                //mSimIndicator.setVisibility(VISIBLE);
                // Aurora xuyong 2014-01-21 deleted for bug #11259 end
            } else {
                mSimIndicator.setVisibility(GONE);
            }
          // Aurora xuyong 2014-05-26 added for multisim feature end
            // Aurora xuyong 2013-10-24 modified for aurora's new feature start
            //mGnMmsClip = (ImageView) findViewById(R.id.gn_image_clip);
            // Aurora xuyong 2013-10-24 modified for aurora's new feature end
            // Aurora xuyong 2014-01-03 added for aurora;s new feature start
            mAttachDownInfo = (TextView) findViewById(R.id.aurora_bg_down);
            // Aurora xuyong 2014-01-03 added for aurora;s new feature end
        //}
        // Aurora xuyong 2014-01-03 addede for aurora;s new feature start
        mGnMmsView.setVisibility(VISIBLE);
        // Aurora xuyong 2014-01-03 added for aurora;s new feature end
        // Aurora xuyong 2013-10-21 modified for aurora's new feature end
        //Aurora xuyong 2013-10-11 added for aurora's new feature start
        // Aurora xuyong 2013-10-24 deleted for aurora's new feature start
        //mGnMmsClip = (ImageView) findViewById(R.id.gn_image_clip);
        // Aurora xuyong 2013-10-24 deleted for aurora's new feature end
        //Aurora xuyong 2013-10-11 added for aurora's new feature end
    }
    private void drawGnLeftStatusIndicator(int msgBoxId,int simId) {
        if (isItemIn) {
            //gionee gaoj 2012-5-25 modified for CR00588933 start
            mMsgSimCard.setVisibility(View.VISIBLE);
            //gionee gaoj 2012-5-25 modified for CR00588933 end
            //Gionee <zhouyj> <2013-05-16> modify for CR00810588 begin
            // Aurora xuyong 2013-09-13 modified for aurora's new feature start
            mBodyTextView.setBackgroundResource(mIsCurrPlaying ? R.drawable.aurora_msg_receive_bg:
                R.drawable.aurora_msg_receive_bg);
            // Aurora xuyong 2013-09-13 modified for aurora's new feature end
            //Gionee <zhouyj> <2013-05-16> modify for CR00810588 end

            //Gionee <gaoj> <2013-05-13> modified for CR00811367 begin
            if (MmsApp.mGnPerfList) {
                // Aurora xuyong 2013-09-13 deleted for aurora's new feature start
                //mBodyTextView.setTextColor(mRecvBodyColor);
                // Aurora xuyong 2013-09-13 deleted for aurora's new feature end
            } else {
                //Gionee <gaoj> <2013-05-13> modified for CR00811367 end
            //gionee gaoj 2013-3-21 modified for CR00787217 start
            if (MmsApp.mLightTheme) {
                // Aurora xuyong 2013-09-13 deleted for aurora's new feature start
                //mBodyTextView.setTextColor(mContext.getResources().getColor(R.color.gn_msg_body_recv_color_dark));
                // Aurora xuyong 2013-09-13 deleted for aurora's new feature end
            } else {
                // Aurora xuyong 2013-09-13 deleted for aurora's new feature start
                //mBodyTextView.setTextColor(mContext.getResources().getColor(R.color.gn_msg_body_recv_color_white));
                // Aurora xuyong 2013-09-13 deleted for aurora's new feature end
            }
            //gionee gaoj 2013-3-21 modified for CR00787217 end
            //Gionee <gaoj> <2013-05-13> modified for CR00811367 begin
            }
            //Gionee <gaoj> <2013-05-13> modified for CR00811367 end
        } else {
            //gionee gaoj 2012-5-25 modified for CR00588933 start
//            mMsgItemOutPanel.setVisibility(View.VISIBLE);
            //gionee gaoj 2012-5-25 modified for CR00588933 end
            // Aurora xuyong 2013-09-13 modified for aurora's new feature start
            if (mSendFailIndi != null) {
                //mRepeatBtn.setVisibility(View.GONE);
                mSendFailIndi.setVisibility(View.GONE);
            }
            // Aurora xuyong 2013-09-13 modified for aurora's new feature end
            if (mMessageItem.isFailedMessage()) {
                //Gionee <zhouyj> <2013-05-16> modify for CR00810588 begin
                // Aurora xuyong 2013-09-13 modified for aurora's new feature start
                mBodyTextView.setBackgroundResource(mIsCurrPlaying ? R.drawable.aurora_msg_send_bg :
                    R.drawable.aurora_msg_send_bg);
                // Aurora xuyong 2013-09-13 modified for aurora's new feature end
                //Gionee <zhouyj> <2013-05-16> modify for CR00810588 end

                //Gionee <gaoj> <2013-05-13> modified for CR00811367 begin
                // Aurora xuyong 2013-09-13 deleted for aurora's new feature start
                /*if (MmsApp.mGnPerfList) {
                    //mBodyTextView.setTextColor(mFailBodyColor);
                    mRepeatBtn.setTextColor(mRepeatBtColor);
                } else {
                      //Gionee <gaoj> <2013-05-13> modified for CR00811367 end
                //gionee gaoj 2013-3-21 modified for CR00787217 start
                if (MmsApp.mLightTheme) {
                    //mBodyTextView.setTextColor(mContext.getResources().getColor(R.color.gn_msg_body_faile_color_dark));
                    mRepeatBtn.setTextColor(mContext.getResources().getColor(R.color.gn_msg_repeat_color_dark));
                } else {
                    //mBodyTextView.setTextColor(mContext.getResources().getColor(R.color.gn_msg_body_faile_color_white));
                    mRepeatBtn.setTextColor(mContext.getResources().getColor(R.color.gn_msg_repeat_color_white));
                }
                //gionee gaoj 2013-3-21 modified for CR00787217 end
                //Gionee <gaoj> <2013-05-13> modified for CR00811367 begin
                }*/
                // Aurora xuyong 2013-09-13 deleted for aurora's new feature end
                //Gionee <gaoj> <2013-05-13> modified for CR00811367 end
            } else {
                //Gionee <gaoj> <2013-05-13> modified for CR00811367 begin
                if (MmsApp.mGnPerfList) {
                    // Aurora xuyong 2013-09-13 deleted for aurora's new feature start
                    //mBodyTextView.setTextColor(mSendBodyColor);
                    // Aurora xuyong 2013-09-13 deleted for aurora's new feature end
                } else {
                    //Gionee <gaoj> <2013-05-13> modified for CR00811367 end
                //gionee gaoj 2013-3-21 modified for CR00787217 start
                if (MmsApp.mLightTheme) {
                    // Aurora xuyong 2013-09-13 deleted for aurora's new feature start
                    //mBodyTextView.setTextColor(mContext.getResources().getColor(R.color.gn_msg_body_send_color_dark));
                    // Aurora xuyong 2013-09-13 deleted for aurora's new feature end
                } else {
                    // Aurora xuyong 2013-09-13 deleted for aurora's new feature start
                    //mBodyTextView.setTextColor(mContext.getResources().getColor(R.color.gn_msg_body_send_color_white));
                    // Aurora xuyong 2013-09-13 deleted for aurora's new feature end
                }
                //gionee gaoj 2013-3-21 modified for CR00787217 end
                //Gionee <gaoj> <2013-05-13> modified for CR00811367 begin
                }
                //Gionee <gaoj> <2013-05-13> modified for CR00811367 end
                //Gionee <zhouyj> <2013-05-16> modify for CR00810588 begin
                // Aurora xuyong 2013-09-13 modified for aurora's new feature start
                mBodyTextView.setBackgroundResource(mIsCurrPlaying ? R.drawable.aurora_msg_send_bg:
                    R.drawable.aurora_msg_send_bg);
                // Aurora xuyong 2013-09-13 modified for aurora's new feature end
                //Gionee <zhouyj> <2013-05-16> modify for CR00810588 end
                //Gionee <gaoj> <2013-05-13> modified for CR00811367 begin
                /*if (!MmsApp.mGnPerfList) {
                //gionee gaoj 2012-12-19 added for CR00751983 start
                if (mIsLastItemInList && ComposeMessageActivity.isSendingMsg) {
                    ComposeMessageActivity.isSendingMsg = false;
                    AnimationSet set = new AnimationSet(true);
                    ScaleAnimation scaleAnimation = new ScaleAnimation(0.3f, 1.0f, 0.3f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
                    set.addAnimation(scaleAnimation);
                    set.addAnimation(alphaAnimation);
                    set.setDuration(500);
                    mMsgListItemLayout.startAnimation(set);
                }
                //gionee gaoj 2012-12-19 added for CR00751983 end
                }*/
                //Gionee <gaoj> <2013-05-13> modified for CR00811367 end
            }
            //gionee gaoj 2012-8-14 added for CR00623375 start
            if (MmsApp.mGnRegularlyMsgSend) {
                if (mMessageItem.mIsRegularlyMms) {
                    //Gionee <zhouyj> <2013-05-16> modify for CR00810588 begin
                    // Aurora xuyong 2013-10-12 modified for aurora's new feature start 
                    mBodyTextView.setBackgroundResource(mIsCurrPlaying ? R.drawable.aurora_msg_send_bg:
                        R.drawable.aurora_msg_send_bg);
                    // Aurora xuyong 2013-10-12 modified for aurora's new feature end
                    //Gionee <zhouyj> <2013-05-16> modify for CR00810588 end
                }
            }
            //gionee gaoj 2012-8-14 added for CR00623375 end

            //Gionee <gaoj> <2013-05-13> modified for CR00811367 begin
            /*if (MmsApp.mGnPerfList && mIsLastItemInList && ComposeMessageActivity.isSendingMsg) {
                ComposeMessageActivity.isSendingMsg = false;
                AnimationSet set = new AnimationSet(true);
                ScaleAnimation scaleAnimation = new ScaleAnimation(0.3f, 1.0f, 0.3f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
                set.addAnimation(scaleAnimation);
                set.addAnimation(alphaAnimation);
                set.setDuration(500);
                mMsgListItemLayoutParent.startAnimation(set);
            }*/
            //Gionee <gaoj> <2013-05-13> modified for CR00811367 end
        }
    }
    //Gionee <guoyx> <2013-05-22> modify for CR00818517 begin
    public ArrayAdapter<String> getAdapter(String url) {
        //gionee gaoj 2012-7-10 added for CR00640592 start
        int layout = -1;
        if (MmsApp.mTransparent) {
            layout = R.layout.gn_select_dialog_item;
        } else {
            layout = R.layout.gn_select_dialog_item_light_dark;
        }
        //gionee gaoj 2012-7-10 added for CR00640592 end
        ArrayAdapter<String> adapter = null;
        String[] telArray = null;
        Log.i(TAG, "contact Indicate Phone Or Sim:" + mIndicatePhoneOrSim);
        if (mExistContactDB && MmsApp.mAddContactDriectly 
        //Gionee <guoyx> <2013-05-30> modify for CR00820739 begin                
                && mIndicatePhoneOrSim == -1) {
        //Gionee <guoyx> <2013-05-30> modify for CR00820739 end
            telArray = new String[]{
                    /*item for ACTION_DIAL*/
                    mContext.getString(R.string.gn_dail),
                    /*item for ACTION_FORWARD*/
                    mContext.getString(R.string.gn_send_message), 
                    /*item for ACTION_COPY*/
                    mContext.getString(R.string.gn_copy_text),
                    /*item for ACTION_NEW_CONTACT*/
                    mContext.getString(R.string.gn_new_contact),
                    /*item for ACTION_INSERT_CONTACT*/
                    mContext.getString(R.string.add_to_contact),
                    /*item for ACTION_INSERT_CONTACT_DIRECTLY*/
                    mContext.getString(R.string.add_to_the_contact) + mContact};
        } else {
            telArray = new String[]{
                    /*item for ACTION_DIAL*/
                    mContext.getString(R.string.gn_dail),
                    /*item for ACTION_FORWARD*/
                    mContext.getString(R.string.gn_send_message), 
                    /*item for ACTION_COPY*/
                    mContext.getString(R.string.gn_copy_text),
                    /*item for ACTION_NEW_CONTACT*/
                    mContext.getString(R.string.gn_new_contact),
                    /*item for ACTION_INSERT_CONTACT*/
                    mContext.getString(R.string.add_to_contact)};
        }
        
        String[] httpArray = new String[]{
                /*item for ACTION_ACCESS*/
                mContext.getString(R.string.gn_visit),
                /*item for ACTION_BOOKMARK*/
                mContext.getString(R.string.gn_add_to_bookmarks)};
        String[] mailArray = new String[]{
                /*item for ACTION_MAIL*/
                mContext.getString(R.string.gn_send_email),
                /*item for ACTION_MAIL_NEW_CONTACT*/
                mContext.getString(R.string.gn_new_contact),
                /*item for ACTION_MAIL_INSERT_CONTACT*/
                mContext.getString(R.string.add_to_contact)};
        String[] rtspArray = new String[]{
                /*item for ACTION_ACCESS*/
                mContext.getString(R.string.gn_visit)};
        switch (getFlag(url)) {
        case URI_PHONE:
            adapter = new ArrayAdapter<String>(mContext, layout, telArray);
            break;
        case URI_HTTP:
            adapter = new ArrayAdapter<String>(mContext, layout, httpArray);
            break;
        case URI_MAIL:
            adapter = new ArrayAdapter<String>(mContext, layout, mailArray);
            break;
        case URI_RTSP:
            adapter = new ArrayAdapter<String>(mContext, layout, rtspArray);
                break;
            default:
                break;
        }
        return adapter;
    }
  //Gionee <guoyx> <2013-05-22> modify for CR00818517 end
    
  //Gionee <guoyx> <2013-05-22> modify for CR00818517 begin
    public int getFlag(String url) {
        int flag = URI_INVALID;
        if (url.startsWith("tel:")) {
            flag = URI_PHONE;
        } else if (url.startsWith("http:") || url.startsWith("https:")) {
            flag = URI_HTTP;
        } else if (url.startsWith("mailto:")) {
            flag = URI_MAIL;
        } else if (url.startsWith("rtsp:")) {
            flag = URI_RTSP;
        }
        return flag;
    }
  //Gionee <guoyx> <2013-05-22> modify for CR00818517 end
  //Gionee <guoyx> <2013-05-22> modify for CR00818517 begin
    public void alertDialog(final String url) {
        AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(mContext);
        DialogInterface.OnClickListener listener = null;
        switch (getFlag(url)) {
            case URI_PHONE:
                listener = new DialogInterface.OnClickListener() {
                    public final void onClick(DialogInterface dialog, int which) {
                        Intent intent = null;
                        Uri uri = null;
                        switch (which) {
                            case ACTION_DIAL:
                                uri = Uri.parse(url);
                                intent = new Intent(Intent.ACTION_DIAL, uri);
                                
                                //gionee gaoj 2013-4-2 added for CR00792780 start
                                intent.setComponent(new ComponentName("com.android.contacts",
                                "com.android.contacts.activities.DialtactsActivity"));
                                //gionee gaoj 2013-4-2 added for CR00792780 end
                                
                                intent.putExtra(Browser.EXTRA_APPLICATION_ID, mContext.getPackageName());
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                                mContext.startActivity(intent);
                                break;
                            case ACTION_FORWARD:
                                Intent smsIntent = new Intent();
                                smsIntent.setClassName(mContext, "com.android.mms.ui.ForwardMessageActivity");
                                smsIntent.putExtra("SENDMSGNUMBER", url.substring(4));
                                smsIntent.putExtra("ISSENDMSG", true);
                                //gionee gaoj 2012-6-26 added for CR00628104 start
                                smsIntent.putExtra("forwarded_message", true);
                                //gionee gaoj 2012-6-26 added for CR00628104 end
                                mContext.startActivity(smsIntent);
                                break;
                            case ACTION_COPY:
                                ClipboardManager clip = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                clip.setText(url.substring(4));
                                break;
                            case ACTION_NEW_CONTACT:
                                intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
                                
                                //gionee gaoj 2013-4-2 added for CR00792780 start
                                intent.setComponent(new ComponentName("com.android.contacts",
                                "com.android.contacts.activities.ContactEditorActivity"));
                                //gionee gaoj 2013-4-2 added for CR00792780 end
                                
                                intent.putExtra(ContactsContract.Intents.Insert.PHONE, url.substring(4));
                                mContext.startActivity(intent);
                                break;
                            case ACTION_INSERT_CONTACT:
                                intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                                
                                //gionee gaoj 2013-4-2 added for CR00792780 start
                                intent.setComponent(new ComponentName("com.android.contacts",
                                "com.android.contacts.activities.ContactSelectionActivity"));
                                //gionee gaoj 2013-4-2 added for CR00792780 end
                                
                                intent.setType(Contacts.CONTENT_ITEM_TYPE);
                                intent.putExtra(ContactsContract.Intents.Insert.PHONE, url.substring(4));
                                mContext.startActivity(intent);
                                break;
                            //Gionee <guoyx> <2013-05-22> add for CR00818517 begin
                            case ACTION_INSERT_CONTACT_DIRECTLY:
                                intent = new Intent(Intent.ACTION_EDIT, mContactUri);
                                intent.putExtra(ContactsContract.Intents.Insert.PHONE, url.substring(4));
                                mContext.startActivity(intent);
                                break;
                            //Gionee <guoyx> <2013-05-22> add for CR00818517 end
                            default:
                                break;
                        }
                        dialog.dismiss();
                    }
                };
                break;
            case URI_HTTP:
                listener = new DialogInterface.OnClickListener() {
                    public final void onClick(DialogInterface dialog, int which) {
                        Intent intent = null;
                        switch (which) {
                            case ACTION_ACCESS:
                                Uri uri = Uri.parse(url);
                                intent = new Intent(Intent.ACTION_VIEW, uri);
                                intent.putExtra(Browser.EXTRA_APPLICATION_ID, mContext.getPackageName());
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                                mContext.startActivity(intent);
                                break;
                            case ACTION_BOOKMARK:
                                intent = new Intent();
                                intent.setAction("android.intent.action.INSERT");
                                intent.setType("vnd.android.cursor.dir/bookmark");
                                intent.putExtra("url", MessageUtils.CheckAndModifyUrl(url));
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                                mContext.startActivity(intent);
                                break;
                            default:
                                break;
                        }
                        dialog.dismiss();
                    }
                };
                break;
            case URI_MAIL:
                listener = new DialogInterface.OnClickListener() {
                    public final void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case ACTION_MAIL:
                                Uri uri = Uri.parse(url);
                                //gionee gaoj 2012 5-25 modified for CR00607834 start
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                //gionee gaoj 2012 5-25 modified for CR00607834 start
                                
                                intent.putExtra(Browser.EXTRA_APPLICATION_ID, mContext.getPackageName());
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                                mContext.startActivity(intent);
                                break;
                            case ACTION_MAIL_NEW_CONTACT:
                                intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
                                
                                //gionee gaoj 2013-4-2 added for CR00792780 start
                                intent.setComponent(new ComponentName("com.android.contacts",
                                "com.android.contacts.activities.ContactEditorActivity"));
                                //gionee gaoj 2013-4-2 added for CR00792780 end
                                
                                /*
                                intent.putExtra(ContactsContract.Intents.Insert.PHONE, url.substring(7));
                                */
                                intent.putExtra(ContactsContract.Intents.Insert.EMAIL, url.substring(7));
                                mContext.startActivity(intent);
                                break;
                            case ACTION_MAIL_INSERT_CONTACT:
                                intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                                
                                //gionee gaoj 2013-4-2 added for CR00792780 start
                                intent.setComponent(new ComponentName("com.android.contacts",
                                "com.android.contacts.activities.ContactSelectionActivity"));
                                //gionee gaoj 2013-4-2 added for CR00792780 end
                                
                                intent.setType(Contacts.CONTENT_ITEM_TYPE);
                                intent.putExtra(ContactsContract.Intents.Insert.EMAIL, url.substring(7));
                                mContext.startActivity(intent);
                                break;
                            default:
                                break;
                        }
                        dialog.dismiss();
                    }
                };
                break;
            case URI_RTSP:
                listener = new DialogInterface.OnClickListener() {
                    public final void onClick(DialogInterface dialog, int which) {
                        Intent intent = null;
                        switch (which) {
                            case ACTION_ACCESS:
                                Uri uri = Uri.parse(url);
                                intent = new Intent(Intent.ACTION_VIEW, uri);
                                
                                intent.putExtra(Browser.EXTRA_APPLICATION_ID, mContext.getPackageName());
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                                mContext.startActivity(intent);
                                break;
                            default:
                                break;
                        }
                        dialog.dismiss();
                    }
                };
                break;
            default:
                break;
        }
        if (getFlag(url) == URI_PHONE) {
            builder.setTitle(url.substring(4));
        } else if (getFlag(url) == URI_MAIL){
            builder.setTitle(url.substring(7));
        } else if (getFlag(url) == URI_HTTP || getFlag(url) == URI_RTSP) {
            builder.setTitle(url);
        }
        builder.setCancelable(true);
        builder.setAdapter(getAdapter(url), listener);

//        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//            public final void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
        if (MmsApp.mGnMessageSupport) {
            //gionee gaoj added for CR00725602 20121201 start
            builder.setCancelIcon(true);
            //gionee gaoj added for CR00725602 20121201 end
            mAlertDialog = builder.show();
        } else {
            builder.show();
        }
    }
  //Gionee <guoyx> <2013-05-22> modify for CR00818517 end
    /**
     * if message delivery status is failed and message is outgoing && failed
     * ,return failed. else(include pending,received,none) return received.
     */
    private String getGnMsgDeliveredIndicator(final MessageItem msgItem) {
        String msg = "";
        MessageItem.DeliveryStatus msgDeliveredIndicator = msgItem.mDeliveryStatus;
        if (msgDeliveredIndicator == MessageItem.DeliveryStatus.FAILED) {
            msg = this.getResources().getString(R.string.gn_send_msg_failed);
        } else if (msgItem.isOutgoingMessage() && msgItem.isFailedMessage()) {
            msg = this.getResources().getString(R.string.gn_send_msg_failed);
        } else {
            //gionee gaoj 2012-5-14 modified for CR00596444 start
//            msg = this.getResources().getString(R.string.gn_send_msg_received);
            //gionee gaoj 2012-5-14 modified for CR00596444 end
        }
        return msg;
    }

    private void setErrorIndicatorClickListener(final MessageItem msgItem) {
        String type = msgItem.mType;
        final int what;
        if (type.equals("sms")) {
            // Aurora xuyong 2013-09-23 modified for aurora's new feature start
            //what = MSG_LIST_EDIT_SMS_DIALOG;
            // Aurora xuyong 2013-09-23 modified for aurora's new feature end
            // Aurora xuyong 2013-12-11 modified for aurora's new feature start
            if (mMessageItem.mGIIF != null && ComposeMessageActivity.sIsGroupMsg) {
                what = MSG_LIST_EDIT_SMS_LIST_DIALOG;
            } else {
                what = MSG_LIST_EDIT_SMS_DIALOG;
            }
            // Aurora xuyong 2013-12-11 modified for aurora's new feature end
        } else {
            // Aurora xuyong 2013-09-23 modified for aurora's new feature start
            what = MSG_LIST_EDIT_MMS_DIALOG;
            // Aurora xuyong 2013-09-23 modified for aurora's new feature end
        }

        // Aurora xuyong 2013-09-13 added for aurora's new feature start
        mSendFailIndi.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (null != mHandler) {
                    Message msg = Message.obtain(mHandler, what);
                    // Aurora xuyong 2013-12-11 modified for aurora's new feature start
                    if (what == MSG_LIST_EDIT_SMS_LIST_DIALOG) {
                        msg.obj = msgItem.mGIIF;
                    } else {
                        msg.obj = new Long(msgItem.mMsgId);
                    }
                    // Aurora xuyong 2013-12-11 modified for aurora's new feature end
                    msg.sendToTarget();
                }
            }
            
        });
        // Aurora xuyong 2013-09-13 added for aurora's new feature end
        // Aurora xuyong 2013-09-13 deleted for aurora's new feature start
        /*mRepeatBtn.setClickable(true);
        mRepeatBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // add for multi-delete
                if (mSelectedBox != null && mSelectedBox.getVisibility() == View.VISIBLE) {
                    return;
                }
                if (null != mHandler) {
                    if (msgItem.mSimId > 0) {
                        mRepeatBtn.setClickable(false);
                    }
                    Message msg = Message.obtain(mHandler, what);
                    msg.obj = new Long(msgItem.mMsgId);
                    msg.sendToTarget();
                }
            }
        });*/
        // Aurora xuyong 2013-09-13 deleted for aurora's new feature end
    }

    //gionee gaoj 2012-8-14 added for CR00623375 CR00678407 start
    private void setRegularSendButClickListener(final MessageItem msgItem) {
        String type = msgItem.mType;
        final int what;
        what = REGULAR_RESET_TIME;
        mRegularlyBtn.setClickable(true);
        mRegularlyBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // add for multi-delete
                if (mSelectedBox != null && mSelectedBox.getVisibility() == View.VISIBLE) {
                    return;
                }
                if (null != mHandler) {
                    Message msg = Message.obtain(mHandler, what);
                    msg.obj = msgItem;
                    msg.sendToTarget();
                }
            }
        });
    }
    //gionee gaoj 2012-8-14 added for CR00623375 CR00678407 end
    // Aurora xuyong 2014-05-05 deleted for aurora's new feature start
    /*private void updateGnAvatarView(String addr, boolean isSelf, QuickContactDivot avatar) {
        Drawable avatarDrawable;
        Contact contact = null;
        if (isSelf || !TextUtils.isEmpty(addr)) {
            if (!isItemIn) {
                contact = isSelf ? Contact.getMe(false) : Contact.get(addr, false);
                //gionee gaoj 2013-2-19 adde for CR00771935 start
                if (isSelf) {
                    avatarDrawable = contact.getAvatar(mContext, mAvataroutDrawable, avatar, true);
                } else {
                    avatarDrawable = contact.getAvatar(mContext, mAvataroutDrawable, avatar, false);
                }
                //gionee gaoj 2013-2-19 adde for CR00771935 end
            } else{
                contact = Contact.get(addr, false);
                if (contact.existsInDatabase()) {
                    //gionee gaoj 2013-2-19 adde for CR00771935 start
                    avatarDrawable = contact.getAvatar(mContext,
                            sDefaultContactImage, avatar, false);
                    //gionee gaoj 2013-2-19 adde for CR00771935 end
                } else {
                    //gionee gaoj 2012-12-11 added for CR00742048 start
                    avatarDrawable = contact.getDrawable(mContext, sContactunknowImage);
                    //gionee gaoj 2012-12-11 added for CR00742048 end
                }
            }

            if (isSelf) {
                // gionee zhouyj 2012-10-15 modify for CR00711635 start 
                if (selfExists()) {
                    avatar.assignContactUri(Profile.CONTENT_URI);
                } else {
                    avatar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mInvalidContactToast == null) {
                                mInvalidContactToast = Toast.makeText(mContext, R.string.invalid_contact_message, Toast.LENGTH_SHORT);
                            }
                            mInvalidContactToast.show();
                        }
                    });
                }
                // gionee zhouyj 2012-10-15 modify for CR00711635 end 
            } else {
                String number = contact.getNumber();
                if (Mms.isEmailAddress(number)) {
                    avatar.assignContactFromEmail(number, true);
                } else {
                    avatar.assignContactFromPhone(number, true);
                }
            }
        } else {
            avatarDrawable = sDefaultContactImage;
        }
        avatar.setImageDrawable(avatarDrawable);
        //gionee gaoj 2013-2-19 adde for CR00771935 start
        if (Contact.sFouceHideContactListPhoto) {
            avatar.setVisibility(View.GONE);
        } else {
            //Gionee <zhouyj> <2013-05-16> modify for CR00810588 begin
            avatar.setVisibility(mIsCurrPlaying ? View.GONE : View.VISIBLE);
            //Gionee <zhouyj> <2013-05-16> modify for CR00810588 end
        }
        //gionee gaoj 2013-2-19 adde for CR00771935 end
    }*/
    // Aurora xuyong 2014-05-05 deleted for aurora's new feature end
    
    private RelativeLayout getWeatherModule(final MessageItem mMessageItem) {
        WeatherInfo info = WeatherInfo.initFromRecord(mMessageItem.mWeatherInfo);
        RelativeLayout weatherModule  = (RelativeLayout)LayoutInflater.from(this.getContext()).inflate(R.layout.aurora_msgitem_weather_layout, null);
        TextView cityText = (TextView)weatherModule.findViewById(R.id.aurora_city);
        cityText.setText(info.getCity());
        // Aurora xuyong 2015-04-23 modified for aurora's new feature start
        TextView tempText = (TextView)weatherModule.findViewById(R.id.aurora_a_tmp);
        tempText.setText(info.getCurTemp());
        TextView typeText = (TextView)weatherModule.findViewById(R.id.aurora_status);
        typeText.setText(info.getWeatherName());
        ImageView thumb = (ImageView)weatherModule.findViewById(R.id.aurora_weather_thumbnail);
        thumb.setImageResource(AuroraMsgWeatherUtils.getResourceIdByIndex(info.getWeatherIndex()));
        // Aurora xuyong 2015-04-23 modified for aurora's new feature end
        return weatherModule;
    }
    
    private void bindGnCommonMessage(final MessageItem msgItem) {
        // Aurora xuyong 2016-02-04 added for xy-smartsms start
        mMsgListItemLayout.setVisibility(View.VISIBLE);
        // Aurora xuyong 2016-02-04 added for xy-smartsms end
        // Aurora xuyong 2015-05-05 modified for bug #13383 start
        if (msgItem != null && msgItem.mWeatherInfo != null && WeatherInfo.hasValidWeatherInfo(msgItem.mWeatherInfo) && msgItem.isReceivedMessage()) {
        // Aurora xuyong 2015-05-05 modified for bug #13383 end
            // Aurora xuyong 2015-04-23 modified for aurora's new feature start
            if (mNeedShowWeatherInfo &&  (mLastWeatherInfo == null || !msgItem.mWeatherInfo.equals(mLastWeatherInfo))) {
                mExternalViews.addView(getWeatherModule(mMessageItem), new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            // Aurora xuyong 2015-04-23 modified for aurora's new feature end
                mExternalViews.setVisibility(View.VISIBLE);
                /*mExternalViews.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        // Aurora xuyong 2014-02-11 modified for bug #1923 start
                        if (null != mHandler) {
                            Message msg = Message.obtain(mHandler, ITEM_BODY_LONG_CLICK);
                            msg.arg1 = MessageListItem.this.getPositionInParent();
                            msg.sendToTarget();
                        }
                        return true;
                        // Aurora xuyong 2014-02-11 modified for bug #1923 end
                    }
                });*/
            }
        }
        // Aurora xuyong 2015-04-23 added for aurora's new feature start
        //mBatchTimeFormat.setText(msgItem.mBatchTimestamp);
        // Aurora xuyong 2015-04-23 added for aurora's new feature end
       // Aurora xuyong 2014-09025 added for android 4.4 feature start
        // Aurora xuyong 2015-03-25 modified for aurora's new feature start
        if (MmsApp.mGnMultiSimMessage) {
            if (this.getMessageItem() != null) {
                mSimIndiDrawableId = getIndiDrawableId(this.getMessageItem().mSimId);
            }
            // Aurora xuyong 2015-03-27 added for aurora's new feature start
            if (mSimIndiDrawableId < 0) {
                mSimIndiDrawableId = R.drawable.aurora_sim_not_found;
            }
            // Aurora xuyong 2015-03-27 added for aurora's new feature end
        // Aurora xuyong 2015-03-25 modified for aurora's new feature end
            mNeedShowSimIndi = true;
        } else {
            mNeedShowSimIndi = false;
        }
       // Aurora xuyong 2014-09025 added for android 4.4 feature end
        //Aurora xuyong 2013-10-11 modified for aurora's new feature start
        if (mAuroraMmsDownloadView != null) {
            //mDownloadGnButton.setVisibility(View.GONE);
            //mDownloadingLabel.setVisibility(View.GONE);
            mAuroraMmsDownloadView.setVisibility(View.GONE);
            mAuroraDownloadMms.setVisibility(View.GONE);
            mAuroraMmsSizeLabel.setVisibility(View.GONE);
            mAuroraMmsDateLabel.setVisibility(View.GONE);
            // Aurora xuyong 2013-11-11 added for aurora's new feature start
            mAuroraDownloading.setVisibility(View.GONE);
            // Aurora xuyong 2013-11-11 added for aurora's new feature end
            //mBodyTextView.setVisibility(View.VISIBLE);
        //Aurora xuyong 2013-10-11 modified for aurora's new feature end
        }
        // Aurora xuyong 2013-10-24 modified for aurora's new feature start
         // Aurora liugj 2013-10-31 modified for fix bug-331/332 start 
        /*if (mBodyTextView != null) {
            mBodyTextView.setVisibility(GONE);
        }

        if (msgItem.mBody != null && msgItem.mBody.getBytes().length > 580) {
            mBodyTextView = (TextView) mMsgListItemLayoutParent.findViewById(R.id.gn_text_view_batch);
        } else {
            mBodyTextView = (TextView) mMsgListItemLayoutParent.findViewById(R.id.gn_text_view);
        }*/
         // Aurora liugj 2013-10-31 modified for fix bug-331/332 end
        // Aurora xuyong 2013-10-24 modified for aurora's new feature end
        mBodyTextView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

        boolean isSelf = !isItemIn;

        //Gionee <gaoj> <2013-05-13> modified for CR00811367 begin
        //gionee gaoj 2012-5-9 added for CR00588933 start
        // Aurora xuyong 2014-05-05 deleted for aurora's new feature start
        /*if (mIsLastItemInList && !Contact.sFouceHideContactListPhoto) {
            //Gionee <gaoj> <2013-05-13> modified for CR00811367 end
            updateGnAvatarView(msgItem.mAddress, isSelf, mGnAvatar);
            // Aurora xuyong 2013-09-13 added for aurora's new feature start
            mGnAvatar.setVisibility(GONE);
            // Aurora xuyong 2013-09-13 added for aurora's new feature end
        } else {
            mGnAvatar.setVisibility(GONE);
        }*/
        // Aurora xuyong 2014-05-05 deleted for aurora's new feature end
        //gionee gaoj 2012-5-9 added for CR00588933 end
        // Get and/or lazily set the formatted message from/on the
        // MessageItem.  Because the MessageItem instances come from a
        // cache (currently of size ~50), the hit rate on avoiding the
        // expensive formatMessage() call is very high.
        CharSequence formattedMessage = msgItem.getCachedFormattedMessage();
        if (formattedMessage == null) {
            formattedMessage = formatMessage(msgItem);
            //Gionee <gaoj> <2013-05-13> added for CR00811367 begin
            msgItem.setCachedFormattedMessage(formattedMessage);
            //Gionee <gaoj> <2013-05-13> added for CR00811367 end
        }
        //gionee gaoj 2013-1-4 added for CR00756848 start
        // Aurora xuyong 2013-09-22 deleted for Aurora's new feature start
        /*if (ComposeMessageActivity.sIsGroupMsg && msgItem.isFailedMessage() && msgItem.isSms()) {
            String contact = Contact.get(msgItem.mAddress, true).getName();
            formattedMessage = getResources().getString(R.string.gn_body_title, contact) + formattedMessage;
        }*/
        // Aurora xuyong 2013-09-22 deleted for Aurora's new feature end
        //gionee gaoj 2013-1-4 added for CR00756848 end
        // Aurora liugj 2013-10-31 modified for fix bug-331/332 start 
        //mBodyTextView.setText(formattedMessage);
        //Aurora xuyong 2013-10-11 added for aurora's new feature start
        if (formattedMessage == null || formattedMessage.length() <=0) {
            mBodyTextView.setVisibility(View.GONE);
            //mIdNumCopyTv.setVisibility(View.GONE);
        } else {
            mBodyTextView.setVisibility(View.VISIBLE);
            //mBodyTextView.setMaxLines(18);
            // Aurroa xuyong 2016-01-25 added for bug #18263 start
            mBodyTextView.setMsgUri(msgItem.getMessageUri());
            // Aurroa xuyong 2016-01-25 added for bug #18263 end
            mBodyTextView.setText(formattedMessage, this.getPositionInParent(), msgItem.mFolded == 1 ? true : false);
          // Aurora xuyong 2014-05-26 added for multisim feature start
            if (MmsApp.mGnMultiSimMessage) {
             // Aurora xuyong 2014-07-14 modified for aurora's new feature start
                setProperSImIcon(mSimFlag, mBodyTextView.getTextView(), mMessageItem.mSimId);
             // Aurora xuyong 2014-07-14 modified for aurora's new feature end
            // Aurora xuyong 2014-11-07 added for bug #9526 start
            }
        
            // Aurora xuyong 2014-11-07 added for bug #9526 end
          // Aurora xuyong 2014-05-26 added for multisim feature end
//            myHandler.post(new Runnable() {
//                @Override
//                public void run() {}
//            });
            Message msg = Message.obtain(myHandler, MyHandler.Gn_Message_TextLine_Rebuild);
            msg.obj = msgItem;
            myHandler.sendMessage(msg);
            // Aurora xuyong 2013-10-12 modified for aurora's new feature start
            // Aurora xuyong 2013-10-18 modified for aurora's new feature start 
            // Aurora xuyong 2013-10-24 modified for aurora's new feature start
            //if (mBodyTextView.getText().toString().getBytes().length > 580) {
            // Aurora xuyong 2013-10-24 modified for aurora's new feature end
            // Aurora xuyong 2013-10-18 modified for aurora's new feature end
                // Aurora xuyong 2013-10-24 modified for aurora's new feature start
                //mBodyTextView.setMaxLines(18);
                // Aurora xuyong 2013-10-24 modified for aurora's new feature end
                //mShowDetail.setVisibility(View.VISIBLE);
            // Aurora xuyong 2013-10-12 modified for aurora's new feature end
                /*mShowDetail.setOnClickListener(new View.OnClickListener() {
                    
                    @Override
                    public void onClick(View arg0) {
                        Message msg = Message.obtain(mHandler, ITEM_SHOW_DETAIL);
                        msg.obj = mBodyTextView.getText();
                        msg.sendToTarget();
                    }
                });
            } else {
                mShowDetail.setVisibility(View.GONE);
            }*/
            // Aurora liugj 2013-10-31 modified for fix bug-331/332 end
        }
        //Aurora xuyong 2013-10-11 added for aurora's new feature end
//MTK_OP01_PROTECT_START
        // add for text zoom
//        String optr = SystemProperties.get("ro.operator.optr");
//        if (optr != null && optr.equals("OP01")) {
            // Aurora xuyong 2013-09-13 deleted for aurora's new feature start
            //mBodyTextView.setTextSize(mTextSize);
            // Aurora xuyong 2013-09-13 deleted for aurora's new feature end
//        }
//MTK_OP01_PROTECT_END
        //gionee gaoj 2012-5-14 modified for CR00596444 start
        MessageItem.DeliveryStatus msgDeliveredIndicator = msgItem.mDeliveryStatus;
        if (msgDeliveredIndicator == MessageItem.DeliveryStatus.FAILED) {
            mTimeLayout.setVisibility(View.GONE);
            //gionee gaoj 2012-8-22 modified for CR00678305 start
        } else if (msgItem.isOutgoingMessage() && msgItem.isFailedMessage() && !msgItem.mIsRegularlyMms) {
            //gionee gaoj 2012-8-22 modified for CR00678305 end
            mTimeLayout.setVisibility(View.GONE);
        } else {
            //gionee gaoj 2012-8-14 added for CR00623375 start
            // Aurora xuyong 2013-10-18 modified for bug #47 start
            // Aurora xuyong 2016-03-28 modified for bug #21852 start
            if (mLastMsgTime != -1 && Math.abs(msgItem.mAuroraDate - mLastMsgTime) < 300000 && !mLastMsgFailed) { //300000 = 5 * 60 * 1000
            // Aurora xuyong 2016-03-28 modified for bug #21852 end
            // Aurora xuyong 2013-10-18 modified for bug #47 end
            //gionee gaoj 2012-8-14 added for CR00623375 end
                mTimeLayout.setVisibility(View.GONE);
            } else {
                //Gionee <guoyx> <2013-07-10> modify for CR00832162 begin
                //Gionee <gaoj> <2013-4-11> added for CR00796538 start
                // Aurora xuyong 2015-05-08 deleted for bug #13338 start
                /*if (MmsApp.mDisplaySendTime && null != msgItem.mSendTimestamp 
                        && null != mSendTimeView) {
                    //Aurora xuyong 2013-10-11 modified for aurora's new feature start
                    mSendTimeView.setVisibility(View.GONE);
                    //Aurora xuyong 2013-10-11 modified for aurora's new feature end
                    mSendTimeView.setText(msgItem.mSendTimestamp);
                } else if (null != mSendTimeView && mSendTimeView.getVisibility() == View.VISIBLE) {
                    mSendTimeView.setVisibility(View.GONE);
                }*/
                // Aurora xuyong 2015-05-08 deleted for bug #13338 end
                //Gionee <gaoj> <2013-4-11> added for CR00796538 end
                //Gionee <guoyx> <2013-07-10> modify for CR00832162 end
                // Aurora xuyong 2013-09-13 deleted for aurora's new feature start
                /*if (mIsDeleteMode) {
                    mTimeFormat.setVisibility(View.GONE);
                } else {*/
                // Aurora xuyong 2013-09-13 deleted for aurora's new feature end
                // Aurora xuyong 2013-12-11 added for aurora's new feature start
                if (mMessageItem.mDeliveryStatus != DeliveryStatus.PENDING) {
                    mTimeLayout.setVisibility(View.VISIBLE);
                    // Aurora xuyong 2016-02-25 modified for bug #18967 start
                    if (msgItem.mTimestamp != null) {
                        mTimeFormat.setText(msgItem.mTimestamp);
                    } else {
                        mTimeFormat.setText(MessageUtils.formatAuroraTimeStampString(this.getContext(), System.currentTimeMillis(), false));
                    }
                    // Aurora xuyong 2016-02-25 modified for bug #18967 end
                }
                // Aurora xuyong 2013-12-11 added for aurora's new feature end
                // Aurora xuyong 2013-09-13 deleted for aurora's new feature start
                //}
                // Aurora xuyong 2013-09-13 deleted for aurora's new feature end
            }
        }
        //gionee gaoj 2012-5-14 modified for CR00596444 end

        mBodyTextView.setMaxWidth(getItemWidth());
        //gionee gaoj 2012-5-24 added for CR00588933 end
        if (msgItem.isSms()) {
            hideMmsViewIfNeeded();
            // add for vcard
            hideFileAttachmentViewIfNeeded();
        } else {
            Presenter presenter = PresenterFactory.getPresenter(
                    "MmsThumbnailPresenter", mContext,
                    this, msgItem.mSlideshow);
            presenter.setmGnFlag(true);
            presenter.present();

            if (msgItem.mAttachmentType != WorkingMessage.TEXT) {
                // gionee zhouyj 2012-06-01 add for CR00611491 start
                if( msgItem.mAttachmentType == WorkingMessage.ATTACHMENT ) {
                    hideMmsViewIfNeeded();
                    showFileAttachmentView(msgItem.mSlideshow.getAttachFiles());
                } else {
                // gionee zhouyj 2012-06-01 add for CR00611491 end
                    //gionee gaoj 2012-10-14 added for CR00711318 start
                    hideFileAttachmentViewIfNeeded();
                    //gionee gaoj 2012-10-14 added for CR00711318 end
                inflateGnMmsView();
                if (mGnMmsView != null) {
                    mGnMmsView.setVisibility(View.VISIBLE);
                }
                setMediaOnClickListener(msgItem);
                // gionee zhouyj 2012-06-01 add for CR00611491 start
                }
                // gionee zhouyj 2012-06-01 add for CR00611491 end
            } else {
                hideMmsViewIfNeeded();
                hideFileAttachmentViewIfNeeded();
            }
        }
        MessageUtils.setSimTextBg(mContext, msgItem.mSimId, mMsgSimCard, !isItemIn);

        drawGnLeftStatusIndicator(msgItem.mBoxId,msgItem.mSimId);
        if (mMsgState != null) {
            // Aurora xuyong 2013-09-24 modified for aurora;s new feature start
            if (!mIsDeleteMode) {
                if (msgItem.isSending() && !msgItem.isRegularlyMms()) {
                    // Aurora xuyong 2013-09-23 modified for aurora's new feature start
                    // Aurora xuyong 2013-11-11 modified for aurora's new feature start
                    mMsgState.setVisibility(View.INVISIBLE);
                    mSendingIndi.setVisibility(View.VISIBLE);
                    //mMsgState.setImageDrawable(this.getResources().getDrawable(R.drawable.aurora_msg_sending));
                    // Aurora xuyong 2013-11-11 modified for aurora's new feature end
                    // Aurora xuyong 2013-09-23 modified for aurora's new feature end
                } else {
                    //gionee gaoj 2012-5-24 added for CR00588933 start
                    // Aurora xuyong 2013-09-23 modified for aurora's new feature start
                    mMsgState.setVisibility(View.VISIBLE);
                    if (msgItem.mDeliveryStatus == MessageItem.DeliveryStatus.RECEIVED) {
                        // Aurora yudingmin 2014-11-21 modified for optimize start
                        if ((SMS_DELIVERY_REPORT_MODE && msgItem.mType.equals("sms")) || (MMS_DELIVERY_REPORT_MODE && msgItem.mType.equals("mms"))) {
                            // Aurora yudingmin 2014-11-21 modified for optimize end
                            // Aurora xuyong 2013-11-11 added for aurora's new feature start
                            //mMsgState.setImageDrawable(this.getResources().getDrawable(R.drawable.aurora_msg_send_suc));
                            mSendingIndi.setVisibility(View.GONE);
                            // Aurora xuyong 2013-11-11 added for aurora's new feature end
                        } else {
                            mMsgState.setVisibility(View.GONE);
                        }
                    } else {
                        mMsgState.setVisibility(View.GONE);
                        // Aurora xuyong 2016-01-22 added for aurora 2.0 new feature start
                        mSendingIndi.setVisibility(View.GONE);
                        // Aurora xuyong 2016-01-22 added for aurora 2.0 new feature end
                    }
                    // Aurora xuyong 2013-09-23 modified for aurora's new feature end
                    //gionee gaoj 2012-5-24 added for CR00588933 end
                }
            } else {
                mMsgState.setVisibility(View.GONE);
            // Aurora xuyong 2013-09-24 added for aurora;s new feature end
            }
        }
        drawRightStatusIndicator(msgItem);

        long msgStatus = msgItem.getMsgStatus();
        if (msgStatus == SmsManager.STATUS_ON_ICC_READ
                || msgStatus == SmsManager.STATUS_ON_ICC_UNREAD
                || msgStatus == SmsManager.STATUS_ON_ICC_SENT
                || msgStatus == SmsManager.STATUS_ON_ICC_UNSENT) {
            if (mDeliveredIndicator != null) {
                mDeliveredIndicator.setVisibility(View.GONE);
            }

            if (mDeliveredIndicator != null) {
                mDetailsIndicator.setVisibility(View.GONE);
            }

            if (mMsgSimCard != null) {
                mMsgSimCard.setVisibility(View.GONE);
            }
            // Aurora xuyong 2013-09-23 deleted for aurora's new feature start
            /*if (mMsgState != null) {
                mMsgState.setVisibility(View.GONE);
            }*/
            // Aurora xuyong 2013-09-23 deleted for aurora's new feature end
            //gionee gaoj 2012-6-16 added for CR00601106 start
            /*if (mGnAvatar != null) {
                mGnAvatar.setVisibility(View.GONE);
            }*/
            //gionee gaoj 2012-6-16 added for CR00601106 end

            //gionee gaoj 2012-12-10 added for CR00741542 start
            drawSimBackGroud();
            //gionee gaoj 2012-12-10 added for CR00741542 end
        // Aurora xuyong 2013-09-23 deleted for aurora's new feature start
        /*} else {
            if (mMsgState != null) {
                mMsgState.setVisibility(View.VISIBLE);
            }*/
        // Aurora xuyong 2013-09-23 deleted for aurora's new feature end
        }

        if (msgStatus == SmsManager.STATUS_ON_ICC_SENT 
                || msgStatus == SmsManager.STATUS_ON_ICC_UNSENT) {
            mTimeLayout.setVisibility(View.GONE);
        }

        requestLayout();
    }
    //gionee gaoj 2012-4-10 added for CR00555790 end

    //gionee gaoj 2012-12-10 added for CR00741542 start
    private void drawSimBackGroud() {
        if (isItemIn) {
        // Aurora xuyong 2013-10-12 modified for aurora's new feature start 
            mBodyTextView.setBackgroundResource(R.drawable.aurora_msg_receive_bg);
        } else {
            if (mMessageItem.isFailedMessage()) {
                mBodyTextView.setBackgroundResource(R.drawable.aurora_msg_send_bg);
            } else {
                mBodyTextView.setBackgroundResource(R.drawable.aurora_msg_send_bg);
            }
            if (MmsApp.mGnRegularlyMsgSend) {
                if (mMessageItem.mIsRegularlyMms) {
                    mBodyTextView.setBackgroundResource(R.drawable.aurora_msg_send_bg);
         // Aurora xuyong 2013-10-12 modified for aurora's new feature end
                }
            }
        }
    }
    //gionee gaoj 2012-12-10 added for CR00741542 end
    // Aurora xuyong 2014-01-10 added for bug #1666 start
    private boolean mIsFirstBind = true;
    // Aurora xuyong 2014-01-10 added for bug #1666 end
    //gionee gaoj 2012-9-20 added for CR00699291 start
    private void gnbind(MessageItem msgItem, boolean isLastItem,
            boolean isDeleteMode) {
        // Aurora xuyong 2014-03-28 added for bug #3676 start 
        if (msgItem == null) {
            return;
        }
        // Aurora xuyong 2014-03-28 added for bug #3676 end
        // TODO Auto-generated method stub
        // gionee zhouyj 2013-01-28 add for CR00767258 start 
        mIsDeleteMode = isDeleteMode;
        // gionee zhouyj 2013-01-28 add for CR00767258 end 
        mMessageItem = msgItem;
        mIsLastItemInList = isLastItem;
        checkItem(msgItem.mBoxId);

        if (isDeleteMode) {
            // Aurora xuyong 2013-12-27 modified for aurora's new feature start
            //mSelectedBox.setVisibility(View.VISIBLE);
            // Aurora xuyong 2013-12-17 modified for aurora's new feature start
            // Aurora xuyong 2014-03-28 modified for bug #3676 start 
            boolean isSelected = msgItem.isSelected();
            mSelectedBox.auroraSetChecked(isSelected, false);
            // Aurora xuyong 2014-03-28 modified for bug #3676 end
            // Aurora xuyong 2013-12-17 modified for aurora's new feature end
            /*if (mMessageItem.mBoxId == Mms.MESSAGE_BOX_INBOX) {
            // Aurora xuyong 2014-01-10 modified for bug #1666 start
                if (mIsFirstBind) {
                    mIsFirstBind = false;
                    // Aurora xuyong 2015-05-08 modified for bug #13338 start
                    auroraStartCheckBoxAppearingAnim(mMsgListItemLayoutParent, mSelectedBox);
                    // Aurora xuyong 2015-05-08 modified for bug #13338 end
                }
            // Aurora xuyong 2014-01-10 modified for bug #1666 end
            }*/
            mSelectedBox.setVisibility(View.VISIBLE);
            // Aurora xuyong 2015-04-23 added for aurora's new feature start
            //mBatchTimeFormat.setVisibility(View.VISIBLE);
            // Aurora xuyong 2015-04-23 added for aurora's new feature end
            // Aurora xuyong 2013-12-27 modified for aurora's new feature end
        } else {
            // Aurora xuyong 2015-05-08 added for bug #13338 start
        	RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mMsgListItemLayoutParent.getLayoutParams();
            lp.leftMargin = (int)this.getResources().getDimension(R.dimen.aurora_msg_left_margin);
            mMsgListItemLayoutParent.setLayoutParams(lp);
            // Aurora xuyong 2015-05-08 added for bug #13338 end
            // Aurora xuyong 2014-01-10 added for bug #1666 start
            mIsFirstBind = true;
            // Aurora xuyong 2014-01-10 added for bug #1666 end
            // Aurora xuyong 2013-01-13 modified for aurora's new feature strat
            // Aurora xuyong 2014-03-28 modified for bug #3676 start
            mSelectedBox.auroraSetChecked(false, false);
            // Aurora xuyong 2014-03-28 modified for bug #3676 end
            // Aurora xuyong 2013-01-13 modified for aurora's new feature end
            mSelectedBox.setVisibility(View.GONE);
            // Aurora xuyong 2015-04-23 added for aurora's new feature start
            //mBatchTimeFormat.setVisibility(View.GONE);
            // Aurora xuyong 2015-04-23 added for aurora's new feature end
        }
        switch (msgItem.mMessageType) {
            case PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND:
                bindGNNotifInd(msgItem);
                break;
            default:
                // Aurora xuyong 2016-01-28 modified for xy-smartsms start
                int showBubbleMode = getShowBubbleMode();
                if (showBubbleMode == XYBubbleListItem.DUOQU_SMARTSMS_SHOW_DEFAULT_PRIMITIVE) {
                    bindGnCommonMessage(msgItem);
                } else {
                    bindRichBubbleView(showBubbleMode);
                }
                // Aurora xuyong 2016-01-28 modified for xy-smartsms end
                break;
        }
    }

    private void bindGNNotifInd(final MessageItem msgItem) {
        hideMmsViewIfNeeded();
        // add for vcard
        hideFileAttachmentViewIfNeeded();
        // Aurora xuyong 2013-10-24 modified for aurora's new feature start
        if (mBodyTextView != null) {
            mBodyTextView.setVisibility(GONE);
        }
        // Aurora xuyong 2015-04-23 added for aurora's new feature start
        //mBatchTimeFormat.setText(msgItem.mBatchTimestamp);
        // Aurora xuyong 2015-04-23 added for aurora's new feature end
         // Aurora liugj 2013-10-31 modified for fix bug-331/332 start 
        /*if (msgItem.mBody != null && msgItem.mBody.getBytes().length > 580) {
            mBodyTextView = (TextView) mMsgListItemLayoutParent.findViewById(R.id.gn_text_view_batch);
        } else {
            mBodyTextView = (TextView) mMsgListItemLayoutParent.findViewById(R.id.gn_text_view);
        }*/
         // Aurora liugj 2013-10-31 modified for fix bug-331/332 end
        // Aurora xuyong 2013-10-24 modified for aurora's new feature end
        mBodyTextView.setVisibility(View.VISIBLE);
        // Aurroa xuyong 2016-01-25 added for bug #18263 start
        mBodyTextView.setMsgUri(msgItem.getMessageUri());
        // Aurroa xuyong 2016-01-25 added for bug #18263 end
        mBodyTextView.setText(formatMessage(msgItem, msgItem.mContact, null, msgItem.mSubject,
                                            msgItem.mHighlight, msgItem.mTextContentType), this.getPositionInParent(), msgItem.mFolded == 1 ? true : false);
        //Aurora xuyong 2013-10-11 added for aurora's new feature start
        // Aurora xuyong 2013-10-12 modified for aurora's new feature start
        // Aurora xuyong 2013-10-18 modified for aurora's new feature start 
        // Aurora xuyong 2013-10-24 modified for aurora's new feature start
        // Aurora liugj 2013-10-31 modified for fix bug-331/332 start 
        //if (mBodyTextView.getText().toString().getBytes().length > 580) {
        // Aurora xuyong 2013-10-24 modified for aurora's new feature end
        // Aurora xuyong 2013-10-18 modified for aurora's new feature end
            // Aurora xuyong 2013-10-24 modified for aurora's new feature start
            //mBodyTextView.setMaxLines(18);
            // Aurora xuyong 2013-10-24 modified for aurora's new feature end
            //mShowDetail.setVisibility(View.VISIBLE);
        // Aurora xuyong 2013-10-12 modified for aurora's new feature end
        /*} else {
            mShowDetail.setVisibility(View.GONE);
        }*/
        // Aurora liugj 2013-10-31 modified for fix bug-331/332 end
        //Aurora xuyong 2013-10-11 added for aurora's new feature end
//MTK_OP01_PROTECT_START
        // add for text zoom
//        String optr = SystemProperties.get("ro.operator.optr");
//        if (optr != null && optr.equals("OP01")) {
            // Aurora xuyong 2013-09-13 deleted for aurora's new feature start
            //mBodyTextView.setTextSize(mTextSize);
            // Aurora xuyong 2013-09-13 deleted for aurora's new feature end
//        }
//MTK_OP01_PROTECT_END
        // Aurora xuyong 2013-10-18 modified for bug #47 start
        // Aurora xuyong 2016-03-28 modified for bug #21852 start
        if (mLastMsgTime != -1 && Math.abs(msgItem.mAuroraDate - mLastMsgTime) < 5 * 60 * 1000 && !mLastMsgFailed) {
        // Aurora xuyong 2016-03-28 modified for bug #21852 end
        // Aurora xuyong 2013-10-18 modified for bug #47 end
            mTimeLayout.setVisibility(View.GONE);
        } else {
            //Gionee <guoyx> <2013-07-11> modify for CR00832162 begin
            //Gionee <gaoj> <2013-4-11> added for CR00796538 start
            // Aurora xuyong 2015-05-08 deleted for bug #13338 start
            /*if (MmsApp.mDisplaySendTime && null != msgItem.mSendTimestamp 
                    && null != mSendTimeView) {
                //Aurora xuyong 2013-10-11 modified for aurora's new feature start
                mSendTimeView.setVisibility(View.GONE);
                //Aurora xuyong 2013-10-11 modified for aurora's new feature end
                mSendTimeView.setText(msgItem.mSendTimestamp);
            } else if (null != mSendTimeView && mSendTimeView.getVisibility() == View.VISIBLE) {
                mSendTimeView.setVisibility(View.GONE);
            }*/
            // Aurora xuyong 2015-05-08 deleted for bug #13338 end
            //Gionee <gaoj> <2013-4-11> added for CR00796538 end
            //Gionee <guoyx> <2013-07-11> modify for CR00832162 end
            // Aurora xuyong 2013-10-21 modified for aurora's new feature start
            mTimeLayout.setVisibility(View.GONE);
            // Aurora xuyong 2013-10-21 modified for aurora's new feature end
            mTimeFormat.setText(msgItem.mTimestamp);
        }
        int state = DownloadManager.getInstance().getState(msgItem.mMessageUri);
        switch (state) {
            case DownloadManager.STATE_DOWNLOADING:
                inflateDownloadControls();
                //Aurora xuyong 2013-10-11 modified for aurora's new feature start
                //mDownloadingLabel.setVisibility(View.VISIBLE);
                //mDownloadGnButton.setVisibility(View.GONE);
                mAuroraMmsDownloadView.setVisibility(VISIBLE);
                // Aurora xuyong 2013-11-11 modified for aurora's new feature start
                mAuroraDownloadMms.setVisibility(View.GONE);
                //mAuroraDownloadMms.setImageResource(R.drawable.aurora_msg_sending);
                mAuroraDownloading.setVisibility(View.VISIBLE);
                // Aurora xuyong 2013-11-11 modified for aurora's new feature end
                mAuroraMmsSizeLabel.setVisibility(View.VISIBLE);
                mAuroraMmsDateLabel.setVisibility(View.VISIBLE);
                mBodyTextView.setVisibility(View.GONE);
                //Aurora xuyong 2013-10-11 modified for aurora's new feature end
                break;
            case DownloadManager.STATE_UNSTARTED:
            case DownloadManager.STATE_TRANSIENT_FAILURE:
            case DownloadManager.STATE_PERMANENT_FAILURE:
            default:
                inflateDownloadControls();
                //Aurora xuyong 2013-10-11 modified for aurora's new feature start
                //mDownloadingLabel.setVisibility(View.GONE);
                //mDownloadGnButton.setVisibility(View.VISIBLE);
                mAuroraMmsDownloadView.setVisibility(VISIBLE);
                mAuroraDownloadMms.setVisibility(View.VISIBLE);
                mAuroraDownloadMms.setImageResource(R.drawable.aurora_mms_download_selector);
                // Aurora xuyong 2013-11-11 added for aurora's new feature start
                mAuroraDownloading.setVisibility(View.GONE);
                // Aurora xuyong 2013-11-11 added for aurora's new feature end
                mAuroraMmsSizeLabel.setVisibility(View.VISIBLE);
                mAuroraMmsDateLabel.setVisibility(View.VISIBLE);
                mBodyTextView.setVisibility(View.GONE);
                mAuroraDownloadMms.setOnClickListener(new OnClickListener() {
                //Aurora xuyong 2013-10-11 modified for aurora's new feature end
                    public void onClick(View v) {
                        //add for multi-delete
                        if (mSelectedBox != null && mSelectedBox.getVisibility() == View.VISIBLE) {
                            return;
                        }
                        //Aurora xuyong 2013-10-11 modified for aurora's new feature start
                        //mDownloadingLabel.setVisibility(View.VISIBLE);
                        //mDownloadGnButton.setVisibility(View.GONE);
                        mAuroraMmsDownloadView.setVisibility(VISIBLE);
                        // Aurora xuyong 2013-11-11 modified for aurora's new feature start
                        mAuroraDownloadMms.setVisibility(View.GONE);
                        //mAuroraDownloadMms.setImageResource(R.drawable.aurora_msg_sending);
                        mAuroraDownloading.setVisibility(View.VISIBLE);
                        // Aurora xuyong 2013-11-11 modified for aurora's new feature end
                        mAuroraMmsSizeLabel.setVisibility(View.VISIBLE);
                        mAuroraMmsDateLabel.setVisibility(View.VISIBLE);
                        //Aurora xuyong 2013-10-11 modified for aurora's new feature end
                        Intent intent = new Intent(mContext, TransactionService.class);
                        intent.putExtra(TransactionBundle.URI, msgItem.mMessageUri.toString());
                        intent.putExtra(TransactionBundle.TRANSACTION_TYPE,
                                Transaction.RETRIEVE_TRANSACTION);
                        // add for gemini
                        int simId = 0;
                        // Aurora xuyong 2014-06-18 moified for bug #5891 start
                        if (MmsApp.mGnMultiSimMessage) {
                        // Aurora xuyong 2014-06-18 moified for bug #5891 end
                            // get sim id by uri
                            Cursor cursor = SqliteWrapper.query(msgItem.mContext, msgItem.mContext.getContentResolver(),
                                msgItem.mMessageUri, new String[] { Mms.SIM_ID }, null, null, null);
                            if (cursor != null) {
                                try {
                                    if ((cursor.getCount() == 1) && cursor.moveToFirst()) {
                                        simId = cursor.getInt(0);
                                    }
                                } finally {
                                    cursor.close();
                                }
                            }
                        }
                        Log.v("comp", "Before Retrieve, simId=" + simId);
                        intent.putExtra(GnPhone.GEMINI_SIM_ID_KEY, simId);
                        intent.putExtra(Mms.DATE_SENT, msgItem.mSmsDate);
                        // Aurora xuyong 2014-11-08 added for reject new feature start
                        intent.putExtra("avoidReject", true);
                        // Aurora xuyong 2014-11-08 added for reject new feature end
                        mContext.startService(intent);
                    }
                });
                break;
        }

        MessageUtils.setSimTextBg(mContext, msgItem.mSimId, mMsgSimCard, !isItemIn);
        drawGnLeftStatusIndicator(msgItem.mBoxId,msgItem.mSimId);
        if (msgItem.mStar == true) {
            mGnFavorite.setVisibility(View.VISIBLE);
        } else {
            mGnFavorite.setVisibility(View.GONE);
        }
        //Gionee <gaoj> <2013-05-13> modified for CR00811367 begin
        //gionee gaoj 2012-5-9 added for CR00588933 start
        // Aurora xuyong 2014-05-05 deleted for aurora's new feature start
        /*if (mIsLastItemInList && !Contact.sFouceHideContactListPhoto) {
            //Gionee <gaoj> <2013-05-13> added for CR00811367 end
            updateGnAvatarView(msgItem.mAddress, false, mGnAvatar);
            //Aurora xuyong 2013-10-11 added for aurora's new feature start
            mGnAvatar.setVisibility(GONE);
            //Aurora xuyong 2013-10-11 added for aurora's new feature end
        } else {
            mGnAvatar.setVisibility(GONE);
        }*/
        // Aurora xuyong 2014-05-05 deleted for aurora's new feature end
    }

    private void checkItem(int msgBoxId) {
        switch (msgBoxId) {
            case Mms.MESSAGE_BOX_INBOX:
                isItemIn = true;
                break;
            case Mms.MESSAGE_BOX_DRAFTS:
            case Sms.MESSAGE_TYPE_FAILED:
            case Sms.MESSAGE_TYPE_QUEUED:
            case Mms.MESSAGE_BOX_OUTBOX:
            default:
                isItemIn = false;
                break;
        }
    }

    private void gnbindDefault(boolean isLastItem) {
        mIsLastItemInList = isLastItem;
        mSelectedBox.setVisibility(View.GONE);
        // Aurora xuyong 2015-04-23 added for aurora's new feature start
        //mBatchTimeFormat.setVisibility(View.GONE);
        // Aurora xuyong 2015-04-23 added for aurora's new feature end
        setFocusable(false);
        setClickable(false);

        if (mGnMmsView != null) {
            mGnMmsView.setVisibility(View.GONE);
        }
        if (mFileAttachmentView != null) {
            mFileAttachmentView.setVisibility(View.GONE);
        }
        //mBodyTextView.setText(R.string.refreshing);
        //Aurora xuyong 2013-10-11 added for aurora's new feature start
        // Aurora xuyong 2013-10-12 modified for aurora's new feature start
        // Aurora xuyong 2013-10-18 modified for aurora's new feature start 
        // Aurora xuyong 2013-10-24 modified for aurora's new feature start
        // Aurora liugj 2013-10-31 modified for fix bug-331/332 start 
        //if (mBodyTextView.getText().toString().getBytes().length > 580) {
        // Aurora xuyong 2013-10-24 modified for aurora's new feature end
        // Aurora xuyong 2013-10-18 modified for aurora's new feature end
            // Aurora xuyong 2013-10-24 modified for aurora's new feature start
            //mBodyTextView.setMaxLines(18);
            // Aurora xuyong 2013-10-24 modified for aurora's new feature end
           //mShowDetail.setVisibility(View.VISIBLE);
        // Aurora xuyong 2013-10-12 modified for aurora's new feature end
        /*} else {
            mShowDetail.setVisibility(View.GONE);
        }*/
        // Aurora liugj 2013-10-31 modified for fix bug-331/332 end
        //Aurora xuyong 2013-10-11 added for aurora's new feature end
        // Aurora xuyong 2013-09-13 deleted for aurora's new feature start
        //mBodyTextView.setTextSize(mTextSize);
        // Aurora xuyong 2013-09-13 deleted for aurora's new feature end
        // Aurora xuyong 2013-09-13 modified for aurora's new feature start
        if (mSendFailIndi != null) {
            //mRepeatBtn.setVisibility(View.GONE);
            mSendFailIndi.setVisibility(View.GONE);
        }
        // Aurora xuyong 2013-09-13 modified for aurora's new feature end
        if (mRegularlyBtn != null) {
            mRegularlyBtn.setVisibility(View.GONE);
        }
        mTimeLayout.setVisibility(View.GONE);
        mMsgSimCard.setVisibility(View.GONE);
        //Aurora xuyong 2013-10-11 modified for aurora's new feature start
        if (mAuroraMmsDownloadView != null) {
            //mDownloadingLabel.setVisibility(View.GONE);
            //mDownloadGnButton.setVisibility(View.GONE);
            mAuroraMmsDownloadView.setVisibility(View.GONE);
            mAuroraDownloadMms.setVisibility(View.GONE);
            mAuroraMmsSizeLabel.setVisibility(View.GONE);
            mAuroraMmsDateLabel.setVisibility(View.GONE);
            // Aurora xuyong 2013-11-11 added for aurora's new feature start
            mAuroraDownloading.setVisibility(View.GONE);
            // Aurora xuyong 2013-11-11 added for aurora's new feature end
        }
        //Aurora xuyong 2013-10-11 modified for aurora's new feature end
        mMsgSimCard.setPadding(PADDING_LEFT_TWE, 0, 0, 0);
        //mGnAvatar.setVisibility(View.GONE);

        requestLayout();
    }

    private void gnonFinishInflate() {
        // Aurora xuyong 2014-02-11 added for bug #1923 start
        // Aurora xuyong 2015-05-08 modified for bug #13338 start
        mItemLayout = (RelativeLayout)findViewById(R.id.aurora_under_time);
        // Aurora xuyong 2015-05-08 modified for bug #13338 end
        // Aurora xuyong 2014-02-11 added for bug #1923 end
        mMsgListItemLayoutParent = (RelativeLayout) findViewById(R.id.gn_msg_item_content);
        mMsgSimCard = (TextView) findViewById(R.id.message_sim_card);
        mMsgListItemLayout = (LinearLayout) findViewById(R.id.mms_layout_view_parent);
        // Aurora xuyong 2013-10-24 deleted for aurora's new feature start
        // Aurora xuyong 2014-11-07 modified for bug #9526 start
        mBodyTextView = (AuroraExpandableTextView) findViewById(R.id.gn_text_view);
        mExternalViews = (LinearLayout)findViewById(R.id.aurora_external_views);
        mBodyTextView.setMovementMethod(AuroraLinkMovementMethod.getInstance());
        // Aurora xuyong 2014-11-07 modified for bug #9526 end
        // Aurora xuyong 2014-09-15 added for aurora's new feature start
        mBodyTextView.getTextView().setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                // TODO Auto-generated method stub
                if (null != mHandler) {
                    Message msg = Message.obtain(mHandler, ITEM_BODY_LONG_CLICK);
                    msg.arg1 = MessageListItem.this.getPositionInParent();
                    msg.sendToTarget();
                }
                return true;
            }
        });
        mBodyTextView.getTextView().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                onMessageListItemClick();
            }
        });
       // Aurora xuyong 2014-09-15 added for aurora's new feature end
        // Aurora xuyong 2014-07-14 added for aurora's new feature start
        mSimFlag = (ImageView) findViewById(R.id.aurora_sim_flag);
        // Aurora xuyong 2014-07-14 added for aurora's new feature end
        // Aurora xuyong 2013-10-24 deleted for aurora's new feature end
        //Aurora xuyong 2013-10-11 added for aurora's new feature start
        //mIdNumCopyTv = (TextView) findViewById(R.id.aurora_id_copy);
        /*mIdNumCopyTv.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
            	*//*if (mIdNumCopyTv.getText().equals(mContext.getString(R.string.aurora_show_detail_unfold))) {
                    // Aurora xuyong 2013-12-30 modified for aurora's new feature start
                    if (mMessageItem.mType.equals("sms")) {
                        Message msg = Message.obtain(mHandler, ITEM_SHOW_DETAIL);
                 // Aurora xuyong 2014-05-26 modified for multisim feature start
                        String text = null;
                        String rexg = AURORA_THUMBNAIL + "  ";
                        if (MmsApp.mGnMultiSimMessage) {
                            text = mBodyTextView.getText().toString();
                            if (text.startsWith(rexg)) {
                                text = text.replaceFirst(rexg, "");
                            } else {
                                text = mBodyTextView.getText().toString();
                            }
                        } else {
                            text = mBodyTextView.getText().toString();
                        }
                        msg.obj = text;
                 // Aurora xuyong 2014-05-26 modified for multisim feature end
                        msg.sendToTarget();
                    } else {
                 // mMessageItem might be null, casuse mms to crash!
                 // Aurora xuyong 2014-06-12 modified for upper reason start
                        if (mMessageItem != null) {
                            MessageUtils.viewMmsMessageAttachment(mContext, ContentUris.withAppendedId(Mms.CONTENT_URI, mMessageItem.mMsgId), null,
                                    mMessageItem.mSubject, mMessageItem.mTimestamp);
                        }
                 // Aurora xuyong 2014-06-12 modified for upper reason end
                    }
                    // Aurora xuyong 2013-12-30 modified for aurora's new feature end
            	} else if (mIdNumCopyTv.getText().equals(mContext.getString(R.string.aurora_cp_id_num))) {*//*
                    Message msg = mHandler.obtainMessage(ITEM_COPY_IDENTIFY_NUM);
                    msg.obj = mMessageItem.getIdentifyNumber();
                    msg.sendToTarget();
            	//}
            }
        });*/
        mGnFavorite = (ImageView) findViewById(R.id.favorite_indicator);
        //Aurora xuyong 2013-10-11 added for aurora's new feature start
        mAuroraDownloadMms = (ImageButton) findViewById(R.id.aurora_mms_download);
        // Aurora xuyong 2013-11-11 added for aurora's new feature start
        mAuroraDownloading = (ProgressBar) findViewById(R.id.aurora_mms_downloading);
        // Aurora xuyong 2013-11-11 added for aurora's new feature end
        //Aurora xuyong 2013-10-11 added for aurora's new feature end
        //mGnAvatar = (QuickContactDivot) findViewById(R.id.gn_avatar);
        mTimeLayout = (RelativeLayout)findViewById(R.id.aurora_msg_time_layout);
        mTimeFormat = (TextView) findViewById(R.id.gn_msg_time_text);
        // Aurora xuyong 2015-04-23 added for aurora's new feature start
        //mBatchTimeFormat = (TextView) findViewById(R.id.aurora_batch_time_tag);
        // Aurora xuyong 2015-04-23 added for aurora's new feature end
        // Aurora xuyong 2013-09-17 added for aurora's new feature start
        // Aurora xuyong 2015-03-04 deletetd for bug #11930 start
        /*Typeface ttf = Typeface.createFromFile("system/fonts/number.ttf");
        mTimeFormat.setTypeface(ttf);*/
        // Aurora xuyong 2015-03-04 deletetd for bug #11930 end
        // Aurora xuyong 2013-09-17 added for aurora's new feature end
        // Aurora xuyong 2013-12-17 modified for aurora's new feature start
        mSelectedBox = (AuroraCheckBox)findViewById(R.id.gn_select_check_box);
        // Aurora xuyong 2013-12-27 added for aurora's new feature start
        // Aurora xuyong 2015-05-08 deleted for bug #13338 start
        //mParentLayout = (LinearLayout)findViewById(R.id.gn_mms_parent_before);
        // Aurora xuyong 2015-05-08 deleted for bug #13338 end
        // Aurora xuyong 2014-02-11 added for bug #1923 start
        mItemLayout.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mIsDeleteMode) {
                    onMessageListItemClick();
                }
            }
        });
        mMsgListItemLayout.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                onMessageListItemClick();
            }
        });
        mMsgListItemLayout.setOnLongClickListener(new View.OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
                // TODO Auto-generated method stub
                if (null != mHandler) {
                    Message msg = Message.obtain(mHandler, ITEM_BODY_LONG_CLICK);
                    msg.arg1 = MessageListItem.this.getPositionInParent();
                    msg.sendToTarget();
                }
                return true;
            }
        });
        // Aurora xuyong 2014-02-11 added for bug #1923 end
        // Aurora xuyong 2013-12-27 added for aurora's new feature end
        // Aurora xuyong 2013-12-17 modified for aurora's new feature end
        // Aurora xuyong 2013-09-23 modified for aurora's new feature start
        mMsgState = (ImageButton) findViewById(R.id.deliver_status_text);
        // Aurora xuyong 2013-11-11 added for aurora's new feature start
        mSendingIndi = (ProgressBar) findViewById(R.id.aurora_sending_indi);
        // Aurora xuyong 2013-11-11 added for aurora's new feature end
        // Aurora xuyong 2013-09-23 modified for aurora's new feature end
        // Aurora xuyong 2013-09-13 deleted for aurora's new feature start
        //mRepeatBtn = (TextView) findViewById(R.id.gn_repeat_btn);
        // Aurora xuyong 2013-09-13 deleted for aurora's new feature end
        // Aurora xuyong 2013-09-13 added for aurora's new feature start
        mSendFailIndi = (ImageButton)findViewById(R.id.aurora_send_fail);
        // Aurora xuyong 2013-09-13 added for aurora's new feature end
        //gionee gaoj 2012-8-14 added for CR00623375 start
        if (MmsApp.mGnRegularlyMsgSend) {
            mRegularlyBtn = (TextView) findViewById(R.id.gn_regularly_btn);
        }
        //gionee gaoj 2012-8-14 added for CR00623375 end
        
        //Gionee <gaoj> <2013-4-11> added for CR00796538 start
        // Aurora xuyong 2015-05-08 deleted for bug #13338 start
       /* if (MmsApp.mDisplaySendTime) {
            mSendTimeView = (TextView) findViewById(R.id.gn_msg_send_time);
        }*/
        // Aurora xuyong 2015-05-08 deleted for bug #13338 end
        //Gionee <gaoj> <2013-4-11> added for CR00796538 end
/*        //Gionee <zhouyj> <2013-05-15> add for CR00810588 begin
        if (MmsApp.mGnVoiceReadMsgSupport) {
            mStopBtnView = (ImageView) findViewById(R.id.gn_stop_voice);
        }
        //Gionee <zhouyj> <2013-05-15> add for CR00810588 end
*/    }

    private int getItemWidth() {
        if (mItemWidth == -1) {
            DisplayMetrics dm = new DisplayMetrics();
            int screenwidth = MessageUtils.getScreenWidth(mContext, dm);
            mItemWidth = (int)3 * screenwidth / 4 ;
        }
        return mItemWidth;
    }
    //gionee gaoj 2012-9-20 added for CR00699291 end

    //gionee wangym 2012-11-22 add for CR00735223 start
    public void setBodyTextSize(float size){
         // Aurora xuyong 2013-09-13 deleted for aurora's new feature start
        /*mTextSize = size;
        if(mBodyTextView != null && mBodyTextView.getVisibility() == View.VISIBLE){
            mBodyTextView.setTextSize(size);
        }*/
        // Aurora xuyong 2013-09-13 deleted for aurora's new feature end
    }
    //gionee wangym 2012-11-22 add for CR00735223 end

     //gionee gaoj 2013-2-19 adde for CR00771935 start
     public QuickContactBadge getQuickContact() {
         return null;//mGnAvatar;
     }
     //gionee gaoj 2013-2-19 adde for CR00771935 end
     
     //Gionee <zhouyj> <2013-05-15> add for CR00810588 begin
     static final int MSG_LIST_STOP_SERVICE = 8;
     
     public void setCurrPlaying(boolean play) {
         /*mIsCurrPlaying = play;
         if (mIsCurrPlaying) {
             mStopBtnView.setOnClickListener(new View.OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    Message msg = Message.obtain(mHandler, MSG_LIST_STOP_SERVICE);
                    msg.sendToTarget();
                }
             });
             mStopBtnView.setVisibility(VISIBLE);
         } else {
             mStopBtnView.setOnClickListener(null);
             mStopBtnView.setVisibility(GONE);
         }*/
     }
     //Gionee <zhouyj> <2013-05-15> add for CR00810588 end
   // Aurora xuyong 2014-04-25 added for bug #4301 start
    @Override
    public void setImage(String name, Uri uri) {
        Bitmap bitmap = null;
        // TODO Auto-generated method stub
        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (MmsApp.mGnMessageSupport) {
            inflateGnMmsView();
        } else {
            //gionee gaoj 2012-4-10 added for CR00555790 end
        inflateMmsView();

        //gionee gaoj 2012-4-10 added for CR00555790 start
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
     // TODO Auto-generated method stub
        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (MmsApp.mGnMessageSupport) {
            inflateGnMmsView();
            // Aurora xuyong 2014-04-29 modified for aurora's new feature start
            mImageView.setVisibility(VISIBLE);
            // Aurora xuyong 2014-05-07 modified for bug 4693 start
            ComposeMessageActivity.mThumbnailWorker.loadImage(uri, mImageView, isImage);
            // Aurora xuyong 2014-05-07 modified for bug 4693 end
            // Aurora xuyong 2014-04-29 modified for aurora's new feature end
        }
    }
   // Aurora xuyong 2014-04-25 added for bug #4301 end
    // Aurora xuyong 2016-01-28 added for xy-smartsms start
    //private boolean mSameItem =false;
    private IXYSmartSmsHolder mSmartSmsHolder =null;
    private XYBubbleListItem mXYBubbleListItem =null;
    @Override
    public void showDefaultListItem() {
        // TODO Auto-generated method stub
        //bindCommonMessage(mSameItem);
        bindGnCommonMessage(mMessageItem);
    }

    @Override
    public View getListItemView() {
        // TODO Auto-generated method stub
        return MessageListItem.this;
    }


    @Override
    public IXYSmartSmsHolder getXySmartSmsHolder() {
        // TODO Auto-generated method stub
        if(mSmartSmsHolder == null || mContext != null && mContext instanceof IXYSmartSmsHolder){
            mSmartSmsHolder= (IXYSmartSmsHolder)mContext;
        }
        return mSmartSmsHolder;
    }

    @Override
    public View findViewById(int viewId, Object... obj) {
        return this.findViewById(viewId);
    }

    @Override
    public int getShowBubbleMode() {
        // TODO Auto-generated method stub
        if(mMessageItem.isSms() && getXySmartSmsHolder() != null && getXySmartSmsHolder().isNotifyComposeMessage() && mMessageItem.isReceivedMessage()){
            return XYBubbleListItem.DUOQU_SMARTSMS_SHOW_BUBBLE_RICH;
        }
        return XYBubbleListItem.DUOQU_SMARTSMS_SHOW_DEFAULT_PRIMITIVE;
    }
    // Aurora xuyong 2016-02-01 added for xy-smartsms start
    @Override
    public View getDefaultContent() {
        return mMsgListItemLayout;
    }
    // Aurora xuyong 2016-02-01 added for xy-smartsms end
    private void bindRichBubbleView(int showBubbleModel){
        if(mXYBubbleListItem == null){
            mXYBubbleListItem = new XYBubbleListItem(getXySmartSmsHolder(), this);
        }
        findViewById(R.id.duoqu_rich_item_group).setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (null != mHandler) {
                    Message msg = Message.obtain(mHandler, ITEM_BODY_LONG_CLICK);
                    msg.arg1 = MessageListItem.this.getPositionInParent();
                    msg.sendToTarget();
                }
                return true;
            }
        });
        findViewById(R.id.duoqu_rich_item_group).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                onMessageListItemClick();
            }
        });
        mXYBubbleListItem.bindBubbleView(mMessageItem, showBubbleModel);
    }
    // Aurora xuyong 2016-01-28 added for xy-smartsms end
}
