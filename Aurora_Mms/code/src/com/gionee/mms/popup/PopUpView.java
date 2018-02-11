/*
 *
 * Copyright (C) 2012 gionee Inc
 *
 * Author: fangbin
 *
 * Description:
 *
 * history
 * name                              date                                      description
 *
 */
package com.gionee.mms.popup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.R.integer;
import aurora.app.AuroraAlertDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.BaseColumns;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Data;
import gionee.provider.GnSettings;
import android.provider.Settings;
import gionee.provider.GnTelephony;
import gionee.provider.GnTelephony.Mms;
import gionee.provider.GnTelephony.MmsSms;
import gionee.provider.GnTelephony.MmsSms.PendingMessages;
import gionee.provider.GnTelephony.SIMInfo;
import gionee.provider.GnTelephony.Sms;
import com.gionee.internal.telephony.GnPhone;
import android.provider.Telephony.Sms.Conversations;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import aurora.widget.AuroraButton;
import aurora.widget.AuroraEditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;
import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.data.WorkingMessage;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.transaction.WapPushMessagingNotification;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.WPMessageActivity;
import com.android.mms.util.SmileyParser;
import com.aurora.featureoption.FeatureOption;
import com.gionee.internal.telephony.GnTelephonyManagerEx;
import aurora.app.AuroraActivity;
// Gionee fangbin 20120517 added for CR00596563 start
import android.widget.RelativeLayout;
// Gionee fangbin 20120517 added for CR00596563 end
//gionee zhouyj 2012-05-24 add for CR00607973 start
import com.android.mms.MmsApp;
import com.gionee.mms.ui.SimSelectView;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.text.Selection;
//gionee zhouyj 2012-05-24 add for CR00607973 end
// Gionee fangbin 20120623 added for CR00627817 start
import android.graphics.Color;
// Gionee fangbin 20120623 added for CR00627817 end
// gionee zhouyj 2012-12-25 add for CR00753821 start 
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
// gionee zhouyj 2012-12-25 add for CR00753821 end 

//Gionee <zhouyj> <2013-06-21> add for CR00760761 begin
import android.view.VelocityTracker;
//Gionee <zhouyj> <2013-06-21> add for CR00760761 end

public class PopUpView extends LinearLayout implements OnTouchListener, OnClickListener{
    private static final String TAG = "PopUpView";

    private LayoutInflater mInflater = null;

    private Context mContext = null;
    // Aurora xuyong 2014-05-05 deleted for aurora's new feature start
    //private ImageView mPhotoImageView = null;
    // Aurora xuyong 2014-05-05 deleted for aurora's new feature end
    private ImageButton mCallView = null;

    private ImageView mSimCardView = null;

    private TextView mCountView = null;

    private TextView mNameView = null;

    private TextView mAddressView = null;

    private TextView mNumberView = null;

    private TextView mSmsContentView = null;

    private ScrollView mScrollView = null;

    private AuroraEditText mResponseEditText = null;

    private AuroraButton mSendBtn = null;

    private AuroraButton mDelBtn = null;

    private AuroraButton mViewBtn = null;

    private AuroraButton mReadBtn = null;

    private PopUpInfoLinkedList mInfoList = null;

    private int mCurrentWindowWidth = -1;

    private Animation mLeftOutAnimation = null;

    private Animation mLeftInAnimation = null;

    private Animation mRightOutAnimation = null;

    private Animation mRightInAnimation = null;

    private List<SIMInfo> mSimInfoList = null;

    private int mSimCount = 0;

    private int mSelectedSimId = 0;

    private int mAssociatedSimId = 0;

    private GnTelephonyManagerEx mTelephonyManager = null;

    private AuroraAlertDialog mSIMSelectDialog = null;

    private OnFinishCallBackListener mCallBackListener = null;

    private LinearLayout mContentLayout = null;

    private TextView mDateView = null;

    private ImageView mMmsContentView = null;

    private Drawable mDefaultDrawable = null;

    private TextView mTextCounterView = null;

    public static final int MAX_RESPONSE_COUNT = 100;

    private List mThreadIdList = null;

    private ImageView mLeftImageView = null;

    private ImageView mRightImageView = null;
    private SIMInfo mSimInfo = null;
    // Gionee fangbin 20120517 added for CR00596563 start
    private AuroraButton mCloseBtn = null;
    private AuroraButton mResponseBtn = null;
    private ImageButton mDeleteBtn = null;
    private AuroraAlertDialog mDeleteConfirmDialog = null;
    private RelativeLayout mResponseLayout = null;
    private ImageView mResponseDownView = null;
    private AuroraEditText mResponseContentView = null;
    private Animation mResponseOutterAnim = null;
    private Animation mResponseHiddenAnim = null;
    private static final boolean GN_GIUI_FOUR = true;
    private TextView mTextCountView = null;
    // Gionee fangbin 20120517 added for CR00596563 end
    // gionee zhouyj 2012-05-24 add for CR00607973 start
    private boolean mSignatureEnable = false;
    private String  mSignatureContent = null;
    // gionee zhouyj 2012-05-24 add for CR00607973 end
    
    // Gionee fangbin 20120623 added for CR00627817 start
    private static boolean mUnicomCustom = SystemProperties.get("ro.operator.optr").equals("OP02");
    private static boolean mShowDigitalSlot = SystemProperties.get("ro.gn.operator.showdigitalslot").equals("yes");
    private static boolean mShowSlot = SystemProperties.get("ro.gn.operator.showslot").equals("yes");
    // Gionee fangbin 20120623 added for CR00627817 end

    private static final String SIM_ID = GnTelephony.GN_SIM_ID;//"sim_id";
    
    // gionee zhouyj 2012-11-14 add for CR00729426 start 
    private static boolean sMmsDbChanged = false;
    // gionee zhouyj 2012-11-14 add for CR00729426 end 
    
    //Gionee <zhouyj> <2013-06-21> add for CR00760761 begin
    private VelocityTracker mVelocityTracker = null;
    private static final int MAXIMUM_FLING_VELOCITY = 8000;
    private static final int MAX_SINGLE_TAP_REGION = 10;
    //Gionee <zhouyj> <2013-06-21> add for CR00760761 end
    
    private TextWatcher mTextWatcher = new TextWatcher() {
        private String text = "";

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
            //Gionee <zhouyj> <2013-07-04> modify for CR00828157 begin
            if (mResponseLayout.getVisibility() == View.VISIBLE) {// AuroraEditText is visiable
                if (mSignatureEnable) {
                    mResponseBtn.setEnabled(!TextUtils.isEmpty(arg0) && !mSignatureContent.equals(arg0.toString()));
                } else {
                    mResponseBtn.setEnabled(arg0.length() > 0);
                }
            }
            //Gionee <zhouyj> <2013-07-04> modify for CR00828157 end
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
            text = arg0.toString();
        }

        @Override
        public void afterTextChanged(Editable arg0) {
            // TODO Auto-generated method stub
            // Gionee fangbin 20120517 modified for CR00596563 start
            if (GN_GIUI_FOUR) {
                if (mResponseContentView.getLineCount() > MAX_RESPONSE_COUNT) {
                    mResponseContentView.setText(text);
                }
                String msg = mResponseContentView.getText().toString();
                setTextCount(msg);
            } else {
                if (mResponseEditText.getLineCount() > MAX_RESPONSE_COUNT) {
                    mResponseEditText.setText(text);
                }
                String msg = mResponseEditText.getText().toString();
                if (msg.equals("") || msg.trim().equals("")) {
                    mSendBtn.setEnabled(false);
                    mSendBtn.setTextColor(R.color.gn_color_gray);
                } else {
                    mSendBtn.setEnabled(true);
                    mSendBtn.setTextColor(0xFFE7E7E7);
                }
                if (mResponseEditText.getLineCount() > 1) {
                    mTextCounterView.setVisibility(View.VISIBLE);
                    setTextCount(msg);
                } else {
                    mTextCounterView.setVisibility(View.GONE);
                }
            }
            // Gionee fangbin 20120517 modified for CR00596563 end
        }
    };

    private void setTextCount(String text) {
        int[] params = SmsMessage.calculateLength(text, false);
        int msgCount = params[0];
        int unitesUsed = params[1];
        int remainingInCurrentMessage = params[2];
        // Gionee fangbin 20120517 modified for CR00596563 start
        if (text.equals("")) {
            if (GN_GIUI_FOUR) {
                mTextCountView.setText("");
            } else {
                mTextCounterView.setText("");
            }
        } else {
            if (GN_GIUI_FOUR) {
                mTextCountView.setText(remainingInCurrentMessage + "(" + msgCount + ")");
            } else {
                mTextCounterView.setText(remainingInCurrentMessage + "(" + msgCount + ")");
            }
        }
        // Gionee fangbin 20120517 modified for CR00596563 end
    }

    public PopUpView(Context context) {
        this(context, null);
    }

    public PopUpView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PopUpView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, R.style.GnPopContentBgStyle);
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      //gionee <gaoj> <2013-07-17> add for CR00832502 begin
        /*mInflater.inflate(R.layout.gn_popup_view, this);*/
        Log.d("Gaoj", "MmsApp.mGnNewPopSupport = "+MmsApp.mGnNewPopSupport);
        if (MmsApp.mGnNewPopSupport) {
            mInflater.inflate(R.layout.gn_popup_new_view, this);
        } else {
            mInflater.inflate(R.layout.gn_popup_view, this);
        }
        //gionee <gaoj> <2013-07-17> add for CR00832502 end
        mInfoList = new PopUpInfoLinkedList();
        mThreadIdList = new ArrayList<integer>();
        getSimInfoList();
        initViews();
        setListeners();
    }

    public int getmCurrentWindowWidth() {
        return mCurrentWindowWidth;
    }

    public void setmCurrentWindowWidth(int mCurrentWindowWidth) {
        this.mCurrentWindowWidth = mCurrentWindowWidth;
    }

    private void initViews() {
        // Aurora xuyong 2014-05-05 deleted for aurora's new feature start
        //mPhotoImageView = (ImageView) findViewById(R.id.qcb);
        // Aurora xuyong 2014-05-05 deleted for aurora's new feature end
        mCallView = (ImageButton) findViewById(R.id.call);
        mSimCardView = (ImageView) findViewById(R.id.simCard);
        mCountView = (TextView) findViewById(R.id.count);
        mNameView = (TextView) findViewById(R.id.name);
        mAddressView = (TextView) findViewById(R.id.address);
        mNumberView = (TextView) findViewById(R.id.number);
        mSmsContentView = (TextView) findViewById(R.id.smsContent);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        mResponseEditText = (AuroraEditText) findViewById(R.id.responseEditText);
        mSendBtn = (AuroraButton) findViewById(R.id.sendBtn);
        mDelBtn = (AuroraButton) findViewById(R.id.delBtn);
        mViewBtn = (AuroraButton) findViewById(R.id.viewBtn);
        mReadBtn = (AuroraButton) findViewById(R.id.readBtn);
        mContentLayout = (LinearLayout) findViewById(R.id.msgContent);
        mDateView = (TextView) findViewById(R.id.msgDate);
        mMmsContentView = (ImageView) findViewById(R.id.mmsContent);
        //mDefaultDrawable = mContext.getResources().getDrawable(R.drawable.gn_pop_default_photo);
        mTextCounterView = (TextView) findViewById(R.id.text_counter);
        mLeftImageView = (ImageView) findViewById(R.id.left);
        mRightImageView = (ImageView) findViewById(R.id.right);

        mLeftInAnimation = AnimationUtils.loadAnimation(mContext, R.anim.popup_left_in_anim);
        mLeftOutAnimation = AnimationUtils.loadAnimation(mContext, R.anim.popup_left_out_anim);
        mRightInAnimation = AnimationUtils.loadAnimation(mContext, R.anim.popup_right_in_anim);
        mRightOutAnimation = AnimationUtils.loadAnimation(mContext, R.anim.popup_right_out_anim);
        
        // Gionee fangbin 20120517 added for CR00596563 start
        mCloseBtn = (AuroraButton) findViewById(R.id.closeBtn);
        mCloseBtn.setOnClickListener(this);
        mResponseLayout = (RelativeLayout) findViewById(R.id.responseLayout);
        mResponseDownView = (ImageView) findViewById(R.id.responseDownView);
        mResponseDownView.setOnClickListener(this);
        mResponseContentView = (AuroraEditText) findViewById(R.id.responseContentView);
        mResponseContentView.addTextChangedListener(mTextWatcher);
        mResponseBtn = (AuroraButton) findViewById(R.id.responseBtn);
        mResponseBtn.setOnClickListener(this);
        mResponseHiddenAnim = AnimationUtils.loadAnimation(mContext, R.anim.popup_resp_hidden_anim);
        mResponseOutterAnim = AnimationUtils.loadAnimation(mContext, R.anim.popup_resp_outter_anim);
        mResponseHiddenAnim.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                mResponseContentView.setEnabled(false);
                mResponseLayout.setVisibility(View.GONE);
                mScrollView.setVisibility(View.VISIBLE);
                //gionee <gaoj> <2013-07-02> add for CR00832502 begin
                if (MmsApp.mGnNewPopSupport) {
                    mLeftImageView.setVisibility(View.GONE);
                    mRightImageView.setVisibility(View.GONE);
                } else {
                    //gionee <gaoj> <2013-07-02> add for CR00832502 end
                if (mInfoList.getSize() > 1) {
                    mLeftImageView.setVisibility(View.VISIBLE);
                    mRightImageView.setVisibility(View.VISIBLE);
                } else {
                    mLeftImageView.setVisibility(View.GONE);
                    mRightImageView.setVisibility(View.GONE);
                }
                //gionee <gaoj> <2013-07-02> add for CR00832502 begin
                }
                //gionee <gaoj> <2013-07-02> add for CR00832502 end
                mResponseBtn.setText(mContext.getString(R.string.gn_mms_pop_response));
            }
        });
        mResponseOutterAnim.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
                mResponseLayout.setVisibility(View.VISIBLE);
                mResponseContentView.setEnabled(true);
                mScrollView.setVisibility(View.GONE);
                mLeftImageView.setVisibility(View.GONE);
                mRightImageView.setVisibility(View.GONE);
                mResponseBtn.setText(mContext.getString(R.string.gn_mms_pop_send));
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                
            }
        });
        mDeleteBtn = (ImageButton) findViewById(R.id.deleteBtn);
        mDeleteBtn.setOnClickListener(this);
        mTextCountView = (TextView) findViewById(R.id.text_counters);
        // Gionee fangbin 20120517 added for CR00596563 end
        // gionee zhouyj 2012-05-24 add for CR00607973 start
        SharedPreferences sp = mContext.getSharedPreferences("com.android.mms_preferences", AuroraActivity.MODE_PRIVATE);
        mSignatureEnable = sp.getBoolean("pref_key_accessories_signature", false);
        sp = mContext.getSharedPreferences("com.gionee.mms.signature_prefences", AuroraActivity.MODE_PRIVATE);
        mSignatureContent = sp.getString("signature", null);
        if(mSignatureContent == null ) {
            mSignatureEnable = false;
        }
        // gionee zhouyj 2012-05-24 add for CR00607973 end
    }

    private void setListeners() {
        //Gionee <zhouyj> <2013-07-12> modify for CR00828150 begin
        mScrollView.setOnTouchListener(new SlipTouchListener(mScrollView, null, new SlipTouchListener.OnEventCallback() {

            @Override
            public void onSliping(View view, boolean right) {
                // TODO Auto-generated method stub
                viewNextMsg(right);
            }

            @Override
            public void onSingleTapUp(View view) {
                // TODO Auto-generated method stub
                viewMsg(mInfoList.getCurrentNode().getType(), mInfoList.getCurrentNode()
                        .getThreadId());
            }
            
        
        }));
        //Gionee <zhouyj> <2013-07-12> modify for CR00828150 end
        
        mDelBtn.setOnClickListener(this);
        mViewBtn.setOnClickListener(this);
        mReadBtn.setOnClickListener(this);
        mCallView.setOnClickListener(this);
        mSendBtn.setOnClickListener(this);
        mResponseEditText.addTextChangedListener(mTextWatcher);
        mLeftOutAnimation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                // TODO Auto-generated method stub
                if (mInfoList.getSize() > 1) {
                    updateViews();
                }
                mScrollView.startAnimation(mLeftInAnimation);
            }
        });
        mRightOutAnimation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                // TODO Auto-generated method stub
                if (mInfoList.getSize() > 1) {
                    updateViews();
                }
                mScrollView.startAnimation(mRightInAnimation);
            }
        });
    }

    public void addInfoNode(Bundle bundle) {
        if (null != bundle) {
            Contact contact = null;
            PopUpInfoNode node = null;

            int type = bundle.getInt(PopUpUtils.POPUP_INFO_MSG_TYPE);
            String number = bundle.getString(PopUpUtils.POPUP_INFO_ADDRESS);
            int simId = bundle.getInt(PopUpUtils.POPUP_INFO_SIM_ID);
            String msgUri = bundle.getString(PopUpUtils.POPUP_INFO_MSG_URI);
            int threadId = bundle.getInt(PopUpUtils.POPUP_INFO_THREAD_ID);
            contact = Contact.get(number, false);
            String name = contact.getName();
            // Aurora xuyong 2014-07-19 modified for sougou start
            String area = MessageUtils.getNumAreaFromAora(mContext, number);
            // Aurora xuyong 2014-07-19 modified for sougou end
            node = new PopUpInfoNode();
            node.setType(type);
            node.setNumber(number);
            node.setSimId(simId);
            node.setMsgUri(msgUri);
            node.setThreadId(threadId);
            node.setName(name);
            node.setArea(area);
            if (!mThreadIdList.contains(threadId)) {
                mThreadIdList.add(threadId);
            }

            switch (type) {
                case PopUpUtils.POPUP_TYPE_SMS:
                    long date = bundle.getLong(PopUpUtils.POPUP_INFO_DATE);
                    String body = bundle.getString(PopUpUtils.POPUP_INFO_BODY);
                    //Gionee <gaoj> <2013-05-13> added for CR00811367 begin
                    if (MmsApp.mGnPerfList) {
                        node.setDate(MessageUtils.formatTimeStampString(this.getContext(), date));
                    } else {
                    // Gionee fangbin 20120619 modified for CR00625563 start
                    node.setDate(MessageUtils.newformatGNTime(this.getContext(), date));
                    // Gionee fangbin 20120619 modified for CR00625563 end
                    }
                    //Gionee <gaoj> <2013-05-13> added for CR00811367 end
                    node.setBody(body);
                    mInfoList.addNode(node);
                    break;

                case PopUpUtils.POPUP_TYPE_MMS:
                    // Gionee fangbin 20120619 modified for CR00625563 start
                    long mmsDate = bundle.getLong(PopUpUtils.POPUP_INFO_DATE);
                    //Gionee <gaoj> <2013-05-13> added for CR00811367 begin
                    if (MmsApp.mGnPerfList) {
                        node.setDate(MessageUtils.formatTimeStampString(this.getContext(), mmsDate));
                    } else {
                    node.setDate(MessageUtils.newformatGNTime(this.getContext(), mmsDate));
                    // Gionee fangbin 20120619 modified for CR00625563 end
                    }
                    //Gionee <gaoj> <2013-05-13> added for CR00811367 end
                    mInfoList.addNode(node);
                    break;

                case PopUpUtils.POPUP_TYPE_PUSH:
                    long pushDate = bundle.getLong(PopUpUtils.POPUP_INFO_DATE);
                    String pushBody = bundle.getString(PopUpUtils.POPUP_INFO_BODY);
                    //Gionee <gaoj> <2013-05-13> added for CR00811367 begin
                    if (MmsApp.mGnPerfList) {
                        node.setDate(MessageUtils.formatTimeStampString(this.getContext(), pushDate));
                    } else {
                    // Gionee fangbin 20120619 modified for CR00625563 start
                    node.setDate(MessageUtils.newformatGNTime(this.getContext(), pushDate));
                    // Gionee fangbin 20120619 modified for CR00625563 end
                    }
                    //Gionee <gaoj> <2013-05-13> added for CR00811367 end
                    node.setBody(pushBody);
                    mInfoList.addNode(node);
                    break;

                default:
                    break;
            }
        }

        if (null != mInfoList && mInfoList.getSize() == 1) {
            updateViews();
        } else {
            //gionee <gaoj> <2013-07-02> add for CR00832502 begin
            if (MmsApp.mGnNewPopSupport) {
                mLeftImageView.setVisibility(View.GONE);
                mRightImageView.setVisibility(View.GONE);
            } else {
                //gionee <gaoj> <2013-07-02> add for CR00832502 end
            // Gionee fangbin 20120517 modified for CR00596563 start
            if (mResponseLayout.getVisibility() == View.VISIBLE) {
                mLeftImageView.setVisibility(View.GONE);
                mRightImageView.setVisibility(View.GONE);
            } else {
                mLeftImageView.setVisibility(View.VISIBLE);
                mRightImageView.setVisibility(View.VISIBLE);
            }
            // Gionee fangbin 20120517 modified for CR00596563 end
            //gionee <gaoj> <2013-07-02> add for CR00832502 begin
            }
            //gionee <gaoj> <2013-07-02> add for CR00832502 end
            //Gionee <zhouyj> <2013-05-02> modify for CR00803952 begin
            if (null != mInfoList && null != mCountView) {
                mCountView.setText(mInfoList.getCurrentNode().getIndex() + "/" + mInfoList.getSize());
            }
            //Gionee <zhouyj> <2013-05-02> modify for CR00803952 end
        }
    }

    static final String[] GN_PROJECTION = new String[] {
            // TODO: should move this symbol into
            // com.android.mms.telephony.Telephony.
            MmsSms.TYPE_DISCRIMINATOR_COLUMN,
            BaseColumns._ID,
            Conversations.THREAD_ID,
            // For SMS
            Sms.ADDRESS, Sms.BODY, Sms.DATE, Sms.DATE_SENT, Sms.READ, Sms.TYPE,
            Sms.STATUS,
            Sms.LOCKED,
            Sms.ERROR_CODE,
            // For MMS
            Mms.SUBJECT, Mms.SUBJECT_CHARSET, Mms.DATE, Mms.DATE_SENT, Mms.READ, Mms.MESSAGE_TYPE,
            Mms.MESSAGE_BOX, Mms.DELIVERY_REPORT, Mms.READ_REPORT, PendingMessages.ERROR_TYPE,
            Mms.LOCKED, Sms.SIM_ID, Mms.SIM_ID, Sms.SERVICE_CENTER
    };

    private void updateViews() {
        //gionee <gaoj> <2013-07-02> add for CR00832502 begin
        if (MmsApp.mGnNewPopSupport) {
            mLeftImageView.setVisibility(View.GONE);
            mRightImageView.setVisibility(View.GONE);
        } else {
            //gionee <gaoj> <2013-07-02> add for CR00832502 end
        if (mInfoList.getSize() <= 1) {
            mLeftImageView.setVisibility(View.INVISIBLE);
            mRightImageView.setVisibility(View.INVISIBLE);
        } else {
            mLeftImageView.setVisibility(View.VISIBLE);
            mRightImageView.setVisibility(View.VISIBLE);
        }
        //gionee <gaoj> <2013-07-02> add for CR00832502 begin
        }
        //gionee <gaoj> <2013-07-02> add for CR00832502 end
        PopUpInfoNode node = mInfoList.getCurrentNode();
        mNameView.setText(node.getName());
        Contact contact = Contact.get(node.getNumber(), false);
        if (contact.existsInDatabase()) {
            mNumberView.setText(node.getNumber());
            mAddressView.setText(node.getArea());
        } else {
            if (null != node.getArea() && !node.getArea().equals("")) {
                mNumberView.setText(node.getArea());
            } else {
                mNumberView.setText(node.getNumber());
            }
            mAddressView.setText(" ");
        }
        // Aurora xuyong 2014-05-05 deleted for aurora's new feature start
        /*Drawable drawable = contact.getAvatar(mContext, null);
        if (null == drawable) {
            if (contact.existsInDatabase()) {
                drawable = getResources().getDrawable(R.drawable.gn_pop_default_photo);
            } else {
                drawable = getResources().getDrawable(R.drawable.gn_pop_default_unkown_photo);
            }
        } else {
            // drawable 缩放
            drawable = PopUpUtils.zoomDrawable(drawable, mDefaultDrawable.getIntrinsicWidth(),
                    mDefaultDrawable.getIntrinsicHeight());
        }*/
        //mPhotoImageView.setImageDrawable(drawable);
        // Aurora xuyong 2014-05-05 deleted for aurora's new feature end
        mCountView.setText(node.getIndex() + "/" + mInfoList.getSize());
        mSimInfo = SIMInfo.getSIMInfoById(mContext, node.getSimId());
        //Gionee <zhouyj> <2013-06-24> modify for CR00829101 begin
        if (null != mSimInfo && mSimInfo.mSlot != -1 && MmsApp.mGnMultiSimMessage) {
            if (mSimInfo.mSlot == 0) {
              //gionee <gaoj> <2013-07-17> add for CR00832502 begin
                /*mSimCardView.setImageDrawable(mContext.getResources().getDrawable(
                        R.drawable.gn_pop_sim1));*/
                /*if (MmsApp.mGnNewPopSupport) {
                    mSimCardView.setImageDrawable(mContext.getResources().getDrawable(
                            R.drawable.gn_pop_sim1_new));
                } else {
                    mSimCardView.setImageDrawable(mContext.getResources().getDrawable(
                            R.drawable.gn_pop_sim1));
                }*/
                //gionee <gaoj> <2013-07-17> add for CR00832502 end
            } else {
              //gionee <gaoj> <2013-07-17> add for CR00832502 begin
                /*mSimCardView.setImageDrawable(mContext.getResources().getDrawable(
                        R.drawable.gn_pop_sim2));*/
                /*if (MmsApp.mGnNewPopSupport) {
                    mSimCardView.setImageDrawable(mContext.getResources().getDrawable(
                            R.drawable.gn_pop_sim2_new));
                } else {
                    mSimCardView.setImageDrawable(mContext.getResources().getDrawable(
                            R.drawable.gn_pop_sim2));
                }*/
                //gionee <gaoj> <2013-07-17> add for CR00832502 end
            }
            mSimCardView.setVisibility(View.VISIBLE);
        } else {
            mSimCardView.setVisibility(View.INVISIBLE);
        }
        //Gionee <zhouyj> <2013-06-24> modify for CR00829101 end
        mDateView.setText(node.getDate());
        // Gionee fangbin 20120517 modified for CR00596563 start
        if (GN_GIUI_FOUR) {
            mResponseContentView.setText(node.getResponseStr());
            mResponseContentView.setEnabled(true);
            mResponseBtn.setEnabled(true);
        } else {
            mResponseEditText.setText(node.getResponseStr());
            mResponseEditText.setEnabled(true);
        }
        // Gionee fangbin 20120517 modified for CR00596563 end
        switch (node.getType()) {
            case PopUpUtils.POPUP_TYPE_SMS:
                mSmsContentView.setText(PopUpUtils.formatMessage(node.getBody()));
                mSmsContentView.setVisibility(View.VISIBLE);
                mMmsContentView.setVisibility(View.GONE);
                break;
            case PopUpUtils.POPUP_TYPE_MMS:
                mSmsContentView.setVisibility(View.GONE);
                mMmsContentView.setVisibility(View.VISIBLE);
                break;
            case PopUpUtils.POPUP_TYPE_PUSH:
                // Gionee fangbin 20120517 modified for CR00596563 start
                if (GN_GIUI_FOUR) {
                    mResponseBtn.setEnabled(false);
                } else {
                    mResponseEditText.setEnabled(false);
                    mSendBtn.setEnabled(false);
                    mSendBtn.setTextColor(R.color.gn_color_gray);
                }
                // Gionee fangbin 20120517 modified for CR00596563 end
                mSmsContentView.setText(PopUpUtils.formatMessage(node.getBody()));
                mSmsContentView.setVisibility(View.VISIBLE);
                mMmsContentView.setVisibility(View.GONE);
                break;
            default:
                break;
        }
        // Gionee fangbin 20120517 added for CR00596563 start
        // Gionee fangbin 20120607 removed for CR00616120 start
        /*
        if (!mInfoList.getCurrentNode().ismIsRead()) {
            setCurrentStateRead(Uri.parse(mInfoList.getCurrentNode().getMsgUri()));
        }
        */
        // Gionee fangbin 20120607 removed for CR00616120 end
        // Gionee fangbin 20120517 added for CR00596563 end
    }

    private void updateInfoNode() {
        mInfoList.removePopUpInfoNode(mInfoList.getCurrentNode());
        if (mInfoList.getSize() > 0) {
            if (mInfoList.getCurrentNode().getIndex() != 1) {
                mInfoList.moveToNext();
            }
            updateViews();
        } else {
            if (null != mCallBackListener) {
                mCallBackListener.onFinishCallBack();
            }
        }
        MessagingNotification.blockingUpdateNewMessageIndicator(mContext, false, false);
        WapPushMessagingNotification.nonBlockingUpdateNewMessageIndicator(mContext, false);
    }

    private void updateInfoNode(int threadId) {
        mInfoList.removedSameThreadIdInfoNode(threadId);
        if (mInfoList.getSize() > 0) {
            mInfoList.moveToNext();
            updateViews();
        } else {
            if (null != mCallBackListener) {
                mCallBackListener.onFinishCallBack();
            }
        }
    }

    public void destroy() {
        if (null != mInfoList) {
            mInfoList.clear();
            mInfoList = null;
        }
        //Gionee <zhouyj> <2013-05-23> 2013-05-28 add for CR00818740 begin
        if (mDeleteConfirmDialog != null && mDeleteConfirmDialog.isShowing()) {
            mDeleteConfirmDialog.dismiss();
        }
        //Gionee <zhouyj> <2013-05-23> 2013-05-28 add for CR00818740 end
        //Gionee <zhouyj> <2013-06-21> add for CR00760761 begin
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
        //Gionee <zhouyj> <2013-06-21> add for CR00760761 end
    }

    private float mStartX = 0;
    // Gionee fangbin 20120517 added for CR00596563 start
    private float mStartY = 0;
    // Gionee fangbin 20120517 added for CR00596563 end

    private long mStartTime = 0;

    private boolean mFlag = false;

    /*// Gionee fangbin 20120517 modified for CR00596563 start
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        // TODO Auto-generated method stub
        if (view.equals(mContentLayout) || view.equals(mSmsContentView)
                || view.equals(mMmsContentView) || view.equals(mDateView)) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mFlag = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    
                    Log.e("TEST", "CLICK------------------REVIEW!!! : event.getY() : " +  event.getY() + " ---startY: " + mStartY);
                    if (Math.abs(event.getX() - mStartX) <= mCurrentWindowWidth / 15 && Math.abs(event.getY() - mStartY) < 1) {
                        viewMsg(mInfoList.getCurrentNode().getType(), mInfoList.getCurrentNode()
                                .getThreadId());
                        return false;
                    } else {
                        Log.e("TEST",
                                "x cha: "
                                        + Math.abs(event.getX() - mStartX)
                                        + " ....x set: "
                                        + mCurrentWindowWidth
                                        / 6
                                        + " ---Y cha: "
                                        + Math.abs(event.getY() - mStartY)
                                        + " .....speed : "
                                        + (int) (Math.abs(event.getX() - mStartX)
                                                / (System.currentTimeMillis() - mStartTime) * 1000)
                                        + " ....speed set: " + mCurrentWindowWidth / 2);
                        if (Math.abs(event.getX() - mStartX) > mCurrentWindowWidth / 6
                                && Math.abs(event.getX() - mStartX)
                                        / (System.currentTimeMillis() - mStartTime) * 1000 >= mCurrentWindowWidth / 2) {
                            mInfoList.getCurrentNode().setResponseStr(
                                    mResponseEditText.getText().toString());
                            if (event.getX() - mStartX > 0) {
                                if (mInfoList.getSize() > 1) {
                                    mInfoList.moveToPrevious();
                                    mScrollView.startAnimation(mRightOutAnimation);
                                }
                            } else {
                                if (mInfoList.getSize() > 1) {
                                    mInfoList.moveToNext();
                                    mScrollView.startAnimation(mLeftOutAnimation);
                                }
                            }
                        }
                        return true;
                    }
                    
                default:
                    break;
            }
        }
        return true;
    }
    // Gionee fangbin 20120517 modified for CR00596563 end
    
    // Gionee fangbin 20120517 added for CR00596563 start
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mStartTime = System.currentTimeMillis();
            mStartX = ev.getX();
            mStartY = ev.getY();
            break;
            
        case MotionEvent.ACTION_MOVE:
            break;
            
        case MotionEvent.ACTION_UP:
            if (mFlag) {
                if (Math.abs(ev.getX() - mStartX) <= mCurrentWindowWidth / 15 && Math.abs(ev.getY() - mStartY) < 1) {
                    viewMsg(mInfoList.getCurrentNode().getType(), mInfoList.getCurrentNode()
                            .getThreadId());
                } else {
                    Log.e("TEST",
                            "x cha: "
                                    + Math.abs(ev.getX() - mStartX)
                                    + " ....x set: "
                                    + mCurrentWindowWidth
                                    / 6
                                    + " ---Y cha: "
                                    + Math.abs(ev.getY() - mStartY)
                                    + " .....speed : "
                                    + (int) (Math.abs(ev.getX() - mStartX)
                                            / (System.currentTimeMillis() - mStartTime) * 1000)
                                    + " ....speed set: " + mCurrentWindowWidth / 2);
                    if (Math.abs(ev.getX() - mStartX) > mCurrentWindowWidth / 6
                            && Math.abs(ev.getX() - mStartX)
                                    / (System.currentTimeMillis() - mStartTime) * 1000 >= mCurrentWindowWidth / 2
                                    && Math.abs(ev.getY() - mStartY) < mCurrentWindowWidth / 8) {
                        if (GN_GIUI_FOUR) {
                            mInfoList.getCurrentNode().setResponseStr(
                                    mResponseContentView.getText().toString());
                        } else {
                            mInfoList.getCurrentNode().setResponseStr(
                                    mResponseEditText.getText().toString());
                        }
                        if (ev.getX() - mStartX > 0) {
                            if (mInfoList.getSize() > 1) {
                                // Gionee fangbin 20120607 added for CR00616120 start
                                if (!mInfoList.getCurrentNode().ismIsRead()) {
                                    setCurrentStateRead(Uri.parse(mInfoList.getCurrentNode().getMsgUri()));
                                }
                                // Gionee fangbin 20120607 added for CR00616120 end
                                mInfoList.moveToPrevious();
                                mScrollView.startAnimation(mRightOutAnimation);
                            }
                        } else {
                            if (mInfoList.getSize() > 1) {
                                // Gionee fangbin 20120607 added for CR00616120 start
                                if (!mInfoList.getCurrentNode().ismIsRead()) {
                                    setCurrentStateRead(Uri.parse(mInfoList.getCurrentNode().getMsgUri()));
                                }
                                // Gionee fangbin 20120607 added for CR00616120 end
                                mInfoList.moveToNext();
                                mScrollView.startAnimation(mLeftOutAnimation);
                            }
                        }
                    }
                }
            }
            mFlag = false;
            mStartTime = 0;
            mStartX = 0;
            mStartY = 0;
            break;

        default:
            break;
        }
        return super.onInterceptTouchEvent(ev);
    }
    // Gionee fangbin 20120517 added for CR00596563 end
*/
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.delBtn:
                // Gionee fangbin 20120517 added for CR00596563 start
                confirmDeleteDialog();
                // Gionee fangbin 20120517 added for CR00596563 end
                // Gionee fangbin 20120517 removed for CR00596563 start
                //delMsg(Uri.parse(mInfoList.getCurrentNode().getMsgUri()));
                // Gionee fangbin 20120517 removed for CR00596563 end
                break;

            case R.id.viewBtn:
            case R.id.mmsContent:
                viewMsg(mInfoList.getCurrentNode().getType(), mInfoList.getCurrentNode()
                        .getThreadId());
                break;

            case R.id.readBtn:
                readedMsg(Uri.parse(mInfoList.getCurrentNode().getMsgUri()));
                break;

            case R.id.call:
                Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
                        + mInfoList.getCurrentNode().getNumber()));
                
                //gionee gaoj 2013-4-2 added for CR00792780 start
                dialIntent.setComponent(new ComponentName("com.android.contacts",
                "com.android.contacts.activities.DialtactsActivity"));
                //gionee gaoj 2013-4-2 added for CR00792780 end
                
                mContext.startActivity(dialIntent);
                break;

            case R.id.sendBtn:
                // Gionee fangbin 20120517 modified for CR00596563 start
                String msg = mResponseContentView.getText().toString();
                if (msg.equals("") || msg.trim().equals("")) {
                    Toast.makeText(mContext, mContext.getString(R.string.gn_mms_pop_no_response), Toast.LENGTH_SHORT).show();
                } else {
                    String recipientStr = mInfoList.getCurrentNode().getNumber();
                    getSimId(recipientStr);
                }
                // Gionee fangbin 20120517 modified for CR00596563 end
                break;
                
            // Gionee fangbin 20120517 added for CR00596563 start
            case R.id.closeBtn:
              //gionee <gaoj> <2013-07-17> add for CR00832502 begin
                if (MmsApp.mGnNewPopSupport) {
                    confirmDeleteDialog();
                    break;
                }
                //gionee <gaoj> <2013-07-17> add for CR00832502 end
                // gionee zhouyj 2012-12-17 add for CR00741581 start 
                mFlag = false;
                // gionee zhouyj 2012-12-17 add for CR00741581 end 
                // Gionee fangbin 20120607 added for CR00616120 start
                if (!mInfoList.getCurrentNode().ismIsRead()) {
                  setCurrentStateRead(Uri.parse(mInfoList.getCurrentNode().getMsgUri()));
                }
                // Gionee fangbin 20120607 added for CR00616120 end
                if (null != mCallBackListener) {
                    mCallBackListener.onFinishCallBack();
                }
                break;
                
            case R.id.responseBtn:
                if (mResponseBtn.getText().equals(mContext.getString(R.string.gn_mms_pop_response))) {
                    showOrHiddenResponseLayout(true);
                    // gionee zhouyj 2013-03-01 modify for CR00772873 start
                    if (mSignatureEnable) {
                        if(!mResponseContentView.getText().toString().endsWith(mSignatureContent)){
                            mResponseContentView.setText(mResponseContentView.getText() + mSignatureContent);
                        }
                        int index = mResponseContentView.getText().toString().length() - mSignatureContent.length();
                        Selection.setSelection(mResponseContentView.getText(), index);
                        mResponseBtn.setEnabled(!mResponseContentView.getText().toString().equals(mSignatureContent));
                    } else {
                        mResponseBtn.setEnabled(!TextUtils.isEmpty(mResponseContentView.getText().toString()));
                    }
                    // gionee zhouyj 2013-03-01 modify for CR00772873 end
                } else {
                    onClick(mSendBtn);
                }
                break;
                
            case R.id.responseDownView:
                // gionee zhouyj 2012-10-24 add for CR00717346 start 
                mResponseBtn.setEnabled(true);
                // gionee zhouyj 2012-10-24 add for CR00717346 end 
                showOrHiddenResponseLayout(false);
                break;
                
            case R.id.deleteBtn:
              //gionee <gaoj> <2013-07-17> add for CR00832502 begin
                if (MmsApp.mGnNewPopSupport) {
                   // gionee zhouyj 2012-12-17 add for CR00741581 start 
                    mFlag = false;
                    // gionee zhouyj 2012-12-17 add for CR00741581 end 
                    // Gionee fangbin 20120607 added for CR00616120 start
                    if (!mInfoList.getCurrentNode().ismIsRead()) {
                      setCurrentStateRead(Uri.parse(mInfoList.getCurrentNode().getMsgUri()));
                    }
                    // Gionee fangbin 20120607 added for CR00616120 end
                    if (null != mCallBackListener) {
                        mCallBackListener.onFinishCallBack();
                    }
                    break;
                }
                 //gionee <gaoj> <2013-07-17> add for CR00832502 end
                confirmDeleteDialog();
                break;
            // Gionee fangbin 20120517 added for CR00596563 end

            default:
                break;
        }
    }

    private int getSimId(String number) {
        if (mSimCount == 0) {
            // sendButton can't click in this case
            mSendBtn.setEnabled(false);
            mSendBtn.setTextColor(R.color.gn_color_gray);
        } else if (mSimCount == 1) {
            if (MmsApp.mGnMultiSimMessage == true) {
                mSelectedSimId = (int) mSimInfoList.get(0).mSimId;
            }
            prepareSendSms(mSelectedSimId);
        } else if (mSimCount > 1) {
            // getContactSIM, Only one recipient
            mAssociatedSimId = getContactSIM(number); // 152188888888 is a
                                                      // contact number

            // getDefaultSIM()
            long mMessageSimId = Settings.System.getLong(mContext.getContentResolver(),
                    GnSettings.System.SMS_SIM_SETTING, GnSettings.System.DEFAULT_SIM_NOT_SET);
            //Gionee <guoyx> <2013-04-15> modified for CR00797011 begin
            //Gionee guoyx 20130320 added for CR00786476 begin
            if (MmsApp.mQcMultiSimEnabled) {
                int sub = (int)mMessageSimId;
                Log.d(TAG, "getSimId, send sms by sub:" + sub);
                if (sub == GnPhone.GEMINI_SIM_1 || sub == GnPhone.GEMINI_SIM_2) { 
                    //slot 0 or slot 1
                    mSelectedSimId = (int)SIMInfo.getSIMInfoBySlot(mContext,sub).mSimId;
                    prepareSendSms(mSelectedSimId);
                    return mSelectedSimId;
                } else {
                    //always ask
                    mMessageSimId = GnSettings.System.DEFAULT_SIM_SETTING_ALWAYS_ASK;
                }
            }
            //Gionee guoyx 20130320 added for CR00786476 end
            //Gionee <guoyx><2013-04-15> modified for CR00797011 end

            if (mMessageSimId == GnSettings.System.DEFAULT_SIM_SETTING_ALWAYS_ASK) {
                // always ask, show SIM selection dialog
                showSimSelectedDialog();
            } else if (mMessageSimId == GnSettings.System.DEFAULT_SIM_NOT_SET) {
                /*
                 * not set default SIM: if recipients are morn than 2,or there
                 * is no associated SIM, show SIM selection dialog else send
                 * message via associated SIM
                 */
                if (mAssociatedSimId == -1) {
                    showSimSelectedDialog();
                } else {
                    mSelectedSimId = mAssociatedSimId;
                    prepareSendSms(mSelectedSimId);
                }
            } else {
                /*
                 * default SIM: if recipients are morn than 2,or there is no
                 * associated SIM, send message via default SIM else show SIM
                 * selection dialog
                 */
                if (mAssociatedSimId == -1 || (mMessageSimId == mAssociatedSimId)) {
                    mSelectedSimId = (int) mMessageSimId;
                    prepareSendSms(mSelectedSimId);
                } else {
                    showSimSelectedDialog();
                }
            }
        }
        Log.d(TAG, "selected sim id : " + mSelectedSimId);
        return mSelectedSimId;
    }

    private void sendSms(int simId, AuroraActivity activity, String msg, String recipientStr) {
        WorkingMessage message = WorkingMessage.createEmpty(activity);
        ContactList list = ContactList.getByNumbers(activity, recipientStr, true, true);
        Conversation conv = Conversation.get(activity, list, false);
        message.setText(msg);
        message.setConversation(conv);
        if (MmsApp.mGnMultiSimMessage) {
            message.sendGemini(simId);
        } else {
            message.send(recipientStr);
        }
        // Gionee fangbin 20120517 modified for CR00596563 start
        if (GN_GIUI_FOUR) {
            showOrHiddenResponseLayout(false);
            mResponseContentView.setText("");
            mResponseContentView.setHint(mContext.getString(R.string.gn_mms_pop_input));
        } else {
            mResponseEditText.setText("");
            mResponseEditText.setHint(mContext.getString(R.string.gn_mms_pop_input));
        }
        // Gionee fangbin 20120517 modified for CR00596563 end
        readedMsg(Uri.parse(mInfoList.getCurrentNode().getMsgUri()));
    }

    private void prepareSendSms(int simId) {
        // Gionee fangbin 20120517 modified for CR00596563 start
        String msg = "";
        if (GN_GIUI_FOUR) {
            msg = mResponseContentView.getText().toString();
        } else {
            msg = mResponseEditText.getText().toString();
        }
        String recipientStr = mInfoList.getCurrentNode().getNumber();
        if (msg.equals("") || msg.trim().equals("")) {
            Toast.makeText(mContext, mContext.getString(R.string.gn_mms_pop_no_response), Toast.LENGTH_SHORT).show();
        } else {
            sendSms(simId, (AuroraActivity) mContext, msg, recipientStr);
        }
        // Gionee fangbin 20120517 modified for CR00596563 end
    }

    private void delMsg(Uri messageUri) {
        SqliteWrapper.delete(mContext, mContext.getContentResolver(), messageUri, null, null);
        updateInfoNode();
    }

    private void readedMsg(Uri messageUri) {
        ContentValues values = new ContentValues();
        values.put(Sms.SEEN, 1);
        values.put(Sms.READ, 1);
        SqliteWrapper.update(mContext, mContext.getContentResolver(), messageUri, values, null,
                null);
        updateInfoNode();
    }
    //Gionee <guoyx> <2013-05-23> modify for CR00812900 begin
    private void viewMsg(int msgType, final int threadId) {
        switch (msgType) {
            case PopUpUtils.POPUP_TYPE_SMS:
            case PopUpUtils.POPUP_TYPE_MMS:
                mContext.startActivity(ComposeMessageActivity.createIntent(mContext, threadId).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
            case PopUpUtils.POPUP_TYPE_PUSH:
                mContext.startActivity(WPMessageActivity.createIntent(mContext, threadId));
                break;
            default:
                break;
        }
        //Gionee <zhouyj> <2013-06-04> modify for CR00821653 begin
        updateInfoNode(threadId);
        //Gionee <zhouyj> <2013-06-04> modify for CR00821653 end
    }
    //Gionee <guoyx> <2013-05-23> modify for CR00812900 end

    private int getContactSIM(String number) {
        int simId = -1;
        Cursor associateSIMCursor = mContext.getContentResolver().query(
                Data.CONTENT_URI,
                new String[] {
                    SIM_ID//Data.SIM_ID
                },
                Data.MIMETYPE + "='" + CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "' AND ("
                        + Data.DATA1 + "='" + number + "') AND (" + SIM_ID/*Data.SIM_ID*/ + "!= -1)", null,
                null);

        if (null == associateSIMCursor) {
            Log.i(TAG, TAG + " queryContactInfo : associateSIMCursor is null");
        } else {
            Log.i(TAG, TAG + " queryContactInfo : associateSIMCursor is not null. Count["
                    + associateSIMCursor.getCount() + "]");
        }

        if ((null != associateSIMCursor) && (associateSIMCursor.getCount() > 0)) {
            associateSIMCursor.moveToFirst();
            // Get only one record is OK
            simId = (Integer) associateSIMCursor.getInt(0);
        } else {
            simId = -1;
        }
        associateSIMCursor.close();
        return simId;
    }

    private boolean isOpen = true;
    private showSimSelectViewListener mShowSimSlelectView = null;

    public interface showSimSelectViewListener {
        public void showSimSelectView();
    }

    public void setshowSimSelectViewListener(showSimSelectViewListener showSimSlelectView) {
        mShowSimSlelectView = showSimSlelectView;
    }

    public void sendMsg(int simid) {
        prepareSendSms(simid);
    }

    // Gionee fangbin 20120623 modified for CR00627817 start
    private void showSimSelectedDialog() {
        if (isOpen) {
            if (null != mShowSimSlelectView) {
                mShowSimSlelectView.showSimSelectView();
            }
            return;
        }
        // TODO get default SIM and get contact SIM
        List<Map<String, ?>> entries = new ArrayList<Map<String, ?>>();
        for (int i = 0; i < mSimCount; i++) {
            SIMInfo simInfo = mSimInfoList.get(i);
            HashMap<String, Object> entry = new HashMap<String, Object>();

            entry.put("simIcon", simInfo.mSimBackgroundRes);
            int state = getSimStatus(i);
            entry.put("simStatus", getStatusResource(state));
            String simNumber = "";
            if (!TextUtils.isEmpty(simInfo.mNumber)) {
                switch (simInfo.mDispalyNumberFormat) {
                    // case
                    // android.provider.Telephony.SimInfo.DISPLAY_NUMBER_DEFAULT:
                    case GnTelephony.SimInfo.DISPLAY_NUMBER_FIRST:
                        if (simInfo.mNumber.length() <= 4)
                            simNumber = simInfo.mNumber;
                        else
                            simNumber = simInfo.mNumber.substring(0, 4);
                        break;
                    case GnTelephony.SimInfo.DISPLAY_NUMBER_LAST:
                        if (simInfo.mNumber.length() <= 4)
                            simNumber = simInfo.mNumber;
                        else
                            simNumber = simInfo.mNumber.substring(simInfo.mNumber.length() - 4);
                        break;
                    case 0:// android.provider.Telephony.SimInfo.DISPLAY_NUMBER_NONE:
                        simNumber = "";
                        break;
                }
            }
            if (!TextUtils.isEmpty(simNumber)) {
                entry.put("simNumberShort", simNumber);
            } else {
                entry.put("simNumberShort", "");
            }

            entry.put("simName", simInfo.mDisplayName);
            if (!TextUtils.isEmpty(simInfo.mNumber)) {
                entry.put("simNumber", simInfo.mNumber);
            } else {
                entry.put("simNumber", "");
            }
            if (mAssociatedSimId == (int) simInfo.mSimId) {
                // if this SIM is contact SIM, set "Suggested"
                entry.put("suggested", mContext.getString(R.string.suggested));
            } else {
                entry.put("suggested", "");// not suggested
            }
            if (mUnicomCustom || mShowDigitalSlot) {
                if((int) simInfo.mSlot == 0) {
                    entry.put("sim3g", mContext.getString(R.string.gn_sim_slot_1));
                } else if((int) simInfo.mSlot == 1) {
                    entry.put("sim3g", mContext.getString(R.string.gn_sim_slot_2));
                } else {
                    entry.put("sim3g", "");
                }
            } else if (mShowSlot) {
                if((int) simInfo.mSlot == 0) {
                    entry.put("sim3g", mContext.getString(R.string.gn_sim_slot_a));
                } else if((int) simInfo.mSlot == 1) {
                    entry.put("sim3g", mContext.getString(R.string.gn_sim_slot_b));
                } else {
                    entry.put("sim3g", "");
                }
            } else {
                entry.put("sim3g", "");
            }
            entries.add(entry);
        }

        final SimpleAdapter a = new SimpleAdapter(mContext, entries, R.layout.gn_sim_selector,
                new String[] {
                        "simIcon", "simStatus", "simNumberShort", "simName", "simNumber",
                        "suggested", "sim3g"
                }, new int[] {
                        R.id.sim_icon, R.id.sim_status, R.id.sim_number_short, R.id.sim_name,
                        R.id.sim_number, R.id.sim_suggested, R.id.sim3g
                });
        SimpleAdapter.ViewBinder viewBinder = new SimpleAdapter.ViewBinder() {
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (MmsApp.mTransparent) {
                    TextView nameTextView = (TextView) view.findViewById(R.id.sim_name);
                    if (nameTextView != null) {
                        nameTextView.setTextColor(Color.BLACK);
                    }
                    TextView numTextView = (TextView) view.findViewById(R.id.sim_number);
                    if (numTextView != null) {
                        numTextView.setTextColor(R.color.gn_color_gray);
                    }
                }
                if (view instanceof ImageView) {
                    if (view.getId() == R.id.sim_icon) {
                        ImageView simicon = (ImageView) view.findViewById(R.id.sim_icon);
                        simicon.setBackgroundResource((Integer) data);
                    } else if (view.getId() == R.id.sim_status) {
                        ImageView simstatus = (ImageView) view.findViewById(R.id.sim_status);
                        if ((Integer)data != GnPhone.SIM_INDICATOR_UNKNOWN
                                && (Integer)data != GnPhone.SIM_INDICATOR_NORMAL) {
                            simstatus.setVisibility(View.VISIBLE);
                            simstatus.setImageResource((Integer)data);
                        } else {
                            simstatus.setVisibility(View.GONE);
                        }
                    }
                    return true;
                }
                if(view instanceof TextView) {
                    if (view.getId() == R.id.sim_number) {
                        TextView simNumber = (TextView)view.findViewById(R.id.sim_number);
                        if(!TextUtils.isEmpty((String)data)) {
                            simNumber.setText((String)data);
                            simNumber.setVisibility(View.VISIBLE);
                        } else {
                            simNumber.setText("");
                            simNumber.setVisibility(View.GONE);
                        }
                        return true;
                    }
                }
                return false;
            }
        };
        a.setViewBinder(viewBinder);
        AuroraAlertDialog.Builder b = new AuroraAlertDialog.Builder(mContext);
        b.setTitle(mContext.getString(R.string.sim_selected_dialog_title));
        b.setCancelable(true);
        b.setAdapter(a, new DialogInterface.OnClickListener() {
            public final void onClick(DialogInterface dialog, int which) {
                mSelectedSimId = (int) mSimInfoList.get(which).mSimId;
                prepareSendSms(mSelectedSimId);
                dialog.dismiss();
            }
        });
        mSIMSelectDialog = b.create();
        mSIMSelectDialog.show();
    }
    // Gionee fangbin 20120623 modified for CR00627817 end

    private int getSimStatus(int id) {
        mTelephonyManager = GnTelephonyManagerEx.getDefault();
        // int slotId = SIMInfo.getSlotById(this,listSimInfo.get(id).mSimId);
        int slotId = mSimInfoList.get(id).mSlot;
        if (slotId != -1) {
            return mTelephonyManager.getSimIndicatorStateGemini(slotId);
        }
        return -1;
    }

    static int getStatusResource(int state) {

        Log.i("Utils gemini", "!!!!!!!!!!!!!state is " + state);
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

    private boolean is3G(int id) {
        mTelephonyManager = GnTelephonyManagerEx.getDefault();
        // int slotId = SIMInfo.getSlotById(this, listSimInfo.get(id).mSimId);
        int slotId = mSimInfoList.get(id).mSlot;
        Log.i(TAG, "SIMInfo.getSlotById id: " + id + " slotId: " + slotId);
        if (slotId == 0) {
            return true;
        }
        return false;
    }

    private void getSimInfoList() {
        if (MmsApp.mGnMultiSimMessage) {
            mSimInfoList = SIMInfo.getInsertedSIMList(mContext);
            mSimCount = mSimInfoList.isEmpty() ? 0 : mSimInfoList.size();
            Log.d(TAG, "mSimCount = " + mSimCount);
        } else { // single SIM
            if (GnPhone.phone != null) {
                try {
                    mSimCount = GnPhone.isSimInsert() ? 1 : 0;
                } catch (Exception e) {
                    Log.e(TAG, "check sim insert status failed");
                    mSimCount = 0;
                }
            }
        }
    }

    public interface OnFinishCallBackListener {
        public void onFinishCallBack();
    }

    public void setOnFinishCallBackListener(OnFinishCallBackListener mCallBackListener) {
        this.mCallBackListener = mCallBackListener;
    }

    public void resetView() {
        for (int i = 0; i < mThreadIdList.size(); i++) {
            Cursor cursor = SqliteWrapper.query(mContext, mContext.getContentResolver(),
                    PopUpUtils.TABLE_THREADS_URI, new String[] {
                        Sms.READ
                    }, Sms._ID + " = " + mThreadIdList.get(i), null, null);
            try {
                if (null == cursor) {
                    updateInfoNode((Integer) mThreadIdList.remove(i));
                    i--;
                } else if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    int seen = cursor.getInt(cursor.getColumnIndexOrThrow(Sms.READ));
                    if (seen == 1) {
                        updateInfoNode((Integer) mThreadIdList.remove(i));
                        i--;
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            } finally {
                if (null != cursor && !cursor.isClosed()) {
                    cursor.close();
                }
            }
        }
    }
    
    // Gionee fangbin 20120517 added for CR00596563 start
    private void confirmDeleteDialog() {
        if (null == mDeleteConfirmDialog) {
            mDeleteConfirmDialog = new AuroraAlertDialog.Builder(mContext/*, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN*/)
                                        .setTitle(mContext.getString(R.string.confirm_dialog_title))
                                        .setCancelable(true)
                                        .setMessage(R.string.confirm_delete_message)
                                        .setPositiveButton(R.string.lockpassword_ok_label, new DialogInterface.OnClickListener() {
                                            
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // TODO Auto-generated method stub
                                                //Gionee <zhouyj> <2013-05-23> 2013-05-23 modify for CR00818740 begin
                                                if (mInfoList != null) {
                                                    if (!mInfoList.getCurrentNode().ismIsRead()) {
                                                        setCurrentStateRead(Uri.parse(mInfoList.getCurrentNode().getMsgUri()));
                                                    }
                                                    delMsg(Uri.parse(mInfoList.getCurrentNode().getMsgUri()));
                                                    sMmsDbChanged = true;
                                                }
                                                //Gionee <zhouyj> <2013-05-23> 2013-05-23 modify for CR00818740 end
                                                showOrHiddenResponseLayout(false);
                                            }
                                        })
                                        .setNegativeButton(R.string.no, null)
                                        .show();
        } else {
            mDeleteConfirmDialog.show();
        }
    }
    
    private void setCurrentStateRead(Uri messageUri) {
        mInfoList.getCurrentNode().setmIsRead(true);
        ContentValues values = new ContentValues();
        values.put(Sms.SEEN, 1);
        values.put(Sms.READ, 1);
        SqliteWrapper.update(mContext, mContext.getContentResolver(), messageUri, values, null,
                null);
        MessagingNotification.blockingUpdateNewMessageIndicator(mContext, false, false);
        WapPushMessagingNotification.nonBlockingUpdateNewMessageIndicator(mContext, false);
    }
    
    private void showOrHiddenResponseLayout(boolean flag) {
        if (flag) {
            if (mResponseLayout.getVisibility() != View.VISIBLE) {
                mResponseLayout.startAnimation(mResponseOutterAnim);
            }
        } else {
            if (mResponseLayout.getVisibility() == View.VISIBLE) {
                mResponseLayout.startAnimation(mResponseHiddenAnim);
            }
        }
    }
    // Gionee fangbin 20120517 added for CR00596563 end
    
    // gionee zhouyj 2012-11-14 add for CR00729426 start 
    public static boolean getMmsFlag() {
        return sMmsDbChanged;
    }
    
    public static void resetMmsFlag(boolean state) {
        sMmsDbChanged = state;
    }
    // gionee zhouyj 2012-11-14 add for CR00729426 end 

    //Gionee <zhouyj> <2013-06-21> modify for CR00760761 begin
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            break;
        case MotionEvent.ACTION_UP:
            mVelocityTracker.computeCurrentVelocity(1000, MAXIMUM_FLING_VELOCITY);
            final int pointerId = event.getPointerId(0);
            final float velocityX = mVelocityTracker.getXVelocity(pointerId);
            final float velocityY = mVelocityTracker.getYVelocity(pointerId);
            Log.i(TAG,"velocityX = " + velocityX);
            if (Math.abs(velocityX) < MAX_SINGLE_TAP_REGION && Math.abs(velocityY) < MAX_SINGLE_TAP_REGION) {
                viewMsg(mInfoList.getCurrentNode().getType(), mInfoList.getCurrentNode()
                        .getThreadId());
            } else if (Math.abs(velocityX) >= MAX_SINGLE_TAP_REGION && Math.abs(velocityX) > Math.abs(velocityY)) {
                if (GN_GIUI_FOUR) {
                    mInfoList.getCurrentNode().setResponseStr(
                            mResponseContentView.getText().toString());
                } else {
                    mInfoList.getCurrentNode().setResponseStr(
                            mResponseEditText.getText().toString());
                }
                viewNextMsg(velocityX < 0);
            }
            break;
        }
        return true;
    }
    
    private void viewNextMsg(boolean next) {
        if (mInfoList.getSize() > 1) {
            if (!mInfoList.getCurrentNode().ismIsRead()) {
                setCurrentStateRead(Uri.parse(mInfoList.getCurrentNode().getMsgUri()));
            }
            if (next) {
                mInfoList.moveToNext();
                mScrollView.startAnimation(mLeftOutAnimation);
            } else {
                mInfoList.moveToPrevious();
                mScrollView.startAnimation(mRightOutAnimation);
            }
        }
    }
    //Gionee <zhouyj> <2013-06-21> modify for CR00760761 end
}
