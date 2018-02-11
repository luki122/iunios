package com.aurora.mms.ui;
//Aurora xuyong 2013-09-20 added for aurora's new feature start
//Aurora xuyong 2013-10-11 added for aurora's new feature start
// Aurora xuyong 2014-04-04 added for aurora's new feature start
import java.util.ArrayList;

import android.content.ActivityNotFoundException;
// Aurora xuyong 2014-06-12 added for multisim feature start
import android.content.BroadcastReceiver;
// Aurora xuyong 2014-06-12 added for multisim feature end
import android.content.ComponentName;
import android.content.Context;
// Aurora xuyong 2014-06-12 added for multisim feature start
import android.content.IntentFilter;
// Aurora xuyong 2014-06-12 added for multisim feature end
// Aurora xuyong 2014-04-04 added for aurora's new feature end
import android.content.Intent;
// Aurora xuyong 2014-06-12 added for multisim feature start
import android.database.ContentObserver;
import android.database.Cursor;
// Aurora xuyong 2014-06-12 added for multisim feature end
// Aurora xuyong 2014-04-04 added for aurora's new feature start
import android.graphics.drawable.BitmapDrawable;
// Aurora xuyong 2014-04-04 added for aurora's new feature end
//Aurora xuyong 2013-10-11 added for aurora's new feature end
// Aurora xuyong 2014-04-04 added for aurora's new feature start
import android.net.Uri;
// Aurora xuyong 2014-04-04 added for aurora's new feature end
import android.os.Bundle;
// Aurora xuyong 2014-04-04 added for aurora's new feature start
import android.os.Handler;
import android.os.Message;
import android.provider.Browser;
import android.provider.ContactsContract;
import android.provider.Contacts.People;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Intents.Insert;
// Aurora xuyong 2014-06-12 added for multisim feature start
import android.telephony.TelephonyManager;
// Aurora xuyong 2014-06-12 added for multisim feature end
import android.text.ClipboardManager;
// Aurora xuyong 2014-09-16 added for bug #8331 start
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
// Aurora xuyong 2014-09-16 added for bug #8331 end
import android.text.style.URLSpan;
// Aurora xuyong 2014-09-16 added for bug #8331 start
import android.text.util.Linkify;
// Aurora xuyong 2014-09-16 added for bug #8331 end
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
// Aurora xuyong 2014-04-04 added for aurora's new feature end
import android.widget.ListView;
// Aurora xuyong 2014-04-04 added for aurora's new feature start
import android.widget.PopupWindow;
import android.widget.Toast;
// Aurora xuyong 2014-04-04 added for aurora's new feature end
//Aurora xuyong 2013-10-11 added for aurora's new feature start
import android.widget.TextView;
// Aurora xuyong 2014-04-04 added for aurora's new feature start
import android.widget.LinearLayout.LayoutParams;
// Aurora xuyong 2014-04-04 added for aurora's new feature end
//Aurora xuyong 2013-10-11 added for aurora's new feature end
// Aurora xuyong 2014-06-12 added for multisim feature start
import gionee.provider.GnTelephony.SIMInfo;
import gionee.provider.GnTelephony.SimInfo;
import gionee.telephony.GnTelephonyManager;
import com.gionee.internal.telephony.GnPhone;
import com.android.internal.telephony.TelephonyIntents;
import gionee.telephony.AuroraTelephoneManager;
import gionee.provider.GnCallLog.Calls;
import android.util.Log;
import com.android.mms.MmsApp;
// Aurora xuyong 2014-06-12 added for multisim feature end
import com.android.mms.R;
// Aurora xuyong 2014-04-04 added for aurora's new feature start
// Aurora xuyong 2014-04-21 added for bug #4438 start
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
// Aurora xuyong 2014-04-21 added for bug #4438 end
import com.android.mms.ui.MessageUtils;
import com.aurora.view.AuroraMultiLinkAdapter;
// Aurora xuyong 2014-09-16 added for bug #8331 start
import com.aurora.view.AuroraURLSpan;
// Aurora xuyong 2014-09-16 added for bug #8331 end
import com.aurora.mms.ui.AuroraMsgDetailActivity.GroupPop;
import com.aurora.mms.util.AuroraLinkMovementMethod;
import com.gionee.mms.ui.SlidesBrowserActivity;
import com.gionee.mms.ui.SlidesBrowserItemView;
// Aurora xuyong 2014-04-04 added for aurora's new feature end
//Aurora xuyong 2013-09-20 added for aurora's new feature end
import aurora.app.AuroraActivity;
//Aurora xuyong 2013-09-20 added for aurora's new feature start
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
//Aurora xuyong 2013-09-20 added for aurora's new feature end

public class AuroraMsgDetailActivity extends AuroraActivity{
    //Aurora xuyong 2013-09-20 added for aurora's new feature start
    private AuroraActionBar mAuroraActionBar;
    //Aurora xuyong 2013-10-11 modified for aurora's new feature start
    private TextView mContent;
    //Aurora xuyong 2013-10-11 modified for aurora's new feature end
    // Aurora xuyong 2014-04-04 added for aurora's new feature start
    Window window;
    View decorView;
    WindowManager.LayoutParams wl;
    // Aurora xuyong 2014-04-04 added for aurora's new feature end
    // Aurora xuyong 2014-06-12 added for multisim feature start
    private int mInsertedSimCount = 0;
    private GnTelephonyManager mTelephonyManager;
    private int mDaultCallSlot = -1;
    // Aurora xuyong 2014-06-12 added for multisim feature end
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Aurora xuyong 2014-04-04 added for aurora's new feature start
        window = this.getWindow();
        wl = window.getAttributes();
        decorView = window.getDecorView();
        // Aurora xuyong 2014-04-04 added for aurora's new feature end
        setAuroraContentView(R.layout.aurora_message_detail_layout,
                AuroraActionBar.Type.Normal);
        mAuroraActionBar = getAuroraActionBar();
        mAuroraActionBar.setTitle(R.string.aurora_msg_detail);
        // Aurora xuyong 2014-04-04 added for aurora's new feature start
        // Aurora xuyong 2016-01-14 modified for aurora 2.0 new feature start
        setAuroraSystemMenuCallBack(auroraMenuCallBack);
        // Aurora xuyong 2016-01-14 modified for aurora 2.0 new feature end
        // Aurora xuyong 2014-04-04 added for aurora's new feature end
        //Aurora xuyong 2013-10-11 modified for aurora's new feature start
      // Aurora xuyong 2014-09-16 added for bug #8331 start
        // Aurora xuyong 2015-02-04 deleted for bug #11531 start
        //AuroraURLSpan.setHandler(mHandler);
        // Aurora xuyong 2015-02-04 deleted for bug #11531 end
      // Aurora xuyong 2014-09-16 added for bug #8331 end
        mContent = (TextView)findViewById(R.id.aurora_detail_mes);
      // Aurora xuyong 2014-09-16 modified for bug #8331 start
        mContent.setText(rebuildTextBody(getDetailContent()));
      // Aurora xuyong 2014-09-16 modified for bug #8331 end
        //Aurora xuyong 2013-10-11 modified for aurora's new feature end
        // Aurora xuyong 2014-04-04 added for aurora's new feature start
      // Aurora xuyong 2014-09-16 deleted for bug #8331 start
        /*mContent.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                initAutoLinks((TextView)v);
            }
        });*/
      // Aurora xuyong 2014-09-16 deleted for bug #8331 end
        // Aurora xuyong 2014-04-04 added for aurora's new feature end
    }
    //Aurora xuyong 2013-09-20 added for aurora's new feature end
    // Aurora xuyong 2014-09-16 added for bug #8331 start
    private Spannable rebuildTextBody(String body) {
        Spannable.Factory sf = Spannable.Factory.getInstance();
        Spannable sp = sf.newSpannable(body);
        if(Linkify.addLinks(sp, Linkify.ALL)) {
            mContent.setMovementMethod(AuroraLinkMovementMethod.getInstance());
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
        return sp;
    }
    // Aurora xuyong 2014-09-16 added for bug #8331 end
    //Aurora xuyong 2013-10-11 modified for aurora's new feature start
    private String getDetailContent() {
        Intent intent = getIntent();
        return intent.getCharSequenceExtra("msgdetail").toString();
    }
    // Aurora xuyong 2014-06-12 added for multisim feature start
    private IntentFilter mSimStateChangedFilter = new IntentFilter();
    private BroadcastReceiver mSimStateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(intent.getAction())
                    || intent.getAction().equals("android.intent.action.PHB_STATE_CHANGED")) {
             // Aurora xuyong 2014-06-07 added for bug #5449 start
                if (MmsApp.mGnMultiSimMessage) {
                    mInsertedSimCount = SIMInfo.getInsertedSIMCount(AuroraMsgDetailActivity.this);
                    if (mInsertedSimCount == 2) {
                        SIMInfo simInfo1 = SIMInfo.getSIMInfoBySlot(AuroraMsgDetailActivity.this
                                    , GnPhone.GEMINI_SIM_1); 
                         SIMInfo simInfo2 = SIMInfo.getSIMInfoBySlot(AuroraMsgDetailActivity.this
                                    , GnPhone.GEMINI_SIM_2);
                         if ((simInfo1 == null || mTelephonyManager.getSimStateGemini(GnPhone.GEMINI_SIM_1) != TelephonyManager.SIM_STATE_READY)) {
                             mInsertedSimCount--;
                         };
                         if ((simInfo2 == null || mTelephonyManager.getSimStateGemini(GnPhone.GEMINI_SIM_2) != TelephonyManager.SIM_STATE_READY)) {
                             mInsertedSimCount--;
                         };
                         if (mInsertedSimCount < 2) {
                            if (simInfo1 != null) {
                                 mDaultCallSlot = 0;
                             } else if (simInfo2 != null) {
                                 mDaultCallSlot = 1;
                             }
                         }
                    } else {
                         SIMInfo simInfo1 = SIMInfo.getSIMInfoBySlot(AuroraMsgDetailActivity.this
                                    , GnPhone.GEMINI_SIM_1); 
                         SIMInfo simInfo2 = SIMInfo.getSIMInfoBySlot(AuroraMsgDetailActivity.this
                                    , GnPhone.GEMINI_SIM_2);
                         if (simInfo1 != null) {
                             mDaultCallSlot = 0;
                         };
                         if (simInfo2 != null) {
                             mDaultCallSlot = 1;
                         };
                    }
                }
            }
        }
    };
     // Aurora xuyong 2014-09-10 modified for uptimize start
    private ContentObserver mSimInfoObserver = new ContentObserver(new Handler()) { 
     // Aurora xuyong 2014-09-10 modified for uptimize end
        @Override
        public void onChange(boolean selfChange) { 
             super.onChange(selfChange); 
             if (MmsApp.mGnMultiSimMessage) {
                 mInsertedSimCount = SIMInfo.getInsertedSIMCount(AuroraMsgDetailActivity.this);
                 if (mInsertedSimCount == 2) {
                     SIMInfo simInfo1 = SIMInfo.getSIMInfoBySlot(AuroraMsgDetailActivity.this
                                 , GnPhone.GEMINI_SIM_1); 
                      SIMInfo simInfo2 = SIMInfo.getSIMInfoBySlot(AuroraMsgDetailActivity.this
                                 , GnPhone.GEMINI_SIM_2);
                      if ((simInfo1 == null || mTelephonyManager.getSimStateGemini(GnPhone.GEMINI_SIM_1) != TelephonyManager.SIM_STATE_READY)) {
                          mInsertedSimCount--;
                      };
                      if ((simInfo2 == null || mTelephonyManager.getSimStateGemini(GnPhone.GEMINI_SIM_2) != TelephonyManager.SIM_STATE_READY)) {
                          mInsertedSimCount--;
                      };
                      if (mInsertedSimCount < 2) {
                        if (simInfo1 != null) {
                             mDaultCallSlot = 0;
                         } else if (simInfo2 != null) {
                             mDaultCallSlot = 1;
                         }
                     }
                 } else {
                     SIMInfo simInfo1 = SIMInfo.getSIMInfoBySlot(AuroraMsgDetailActivity.this
                                 , GnPhone.GEMINI_SIM_1); 
                      SIMInfo simInfo2 = SIMInfo.getSIMInfoBySlot(AuroraMsgDetailActivity.this
                                 , GnPhone.GEMINI_SIM_2);
                      if (simInfo1 != null) {
                          mDaultCallSlot = 0;
                      };
                      if (simInfo2 != null ) {
                          mDaultCallSlot = 1;
                      };
                 }
             }
        }   

    };
    
     @Override
     protected void onResume() {
        super.onResume();
        if (MmsApp.mGnMultiSimMessage) {
            mTelephonyManager = GnTelephonyManager.getDefault();
            mInsertedSimCount = SIMInfo.getInsertedSIMCount(this);
            if (mInsertedSimCount == 2) {
                SIMInfo simInfo1 = SIMInfo.getSIMInfoBySlot(this, GnPhone.GEMINI_SIM_1); 
                SIMInfo simInfo2 = SIMInfo.getSIMInfoBySlot(this, GnPhone.GEMINI_SIM_2);
                if ((simInfo1 == null || mTelephonyManager.getSimStateGemini(GnPhone.GEMINI_SIM_1) != TelephonyManager.SIM_STATE_READY)) {
                    mInsertedSimCount--;
                };
                if ((simInfo2 == null || mTelephonyManager.getSimStateGemini(GnPhone.GEMINI_SIM_2) != TelephonyManager.SIM_STATE_READY)) {
                    mInsertedSimCount--;
                };
                if (mInsertedSimCount < 2) {
                    if (simInfo1 != null) {
                         mDaultCallSlot = 0;
                     } else if (simInfo2 != null) {
                         mDaultCallSlot = 1;
                     }
                }
            }  else {
                  SIMInfo simInfo1 = SIMInfo.getSIMInfoBySlot(AuroraMsgDetailActivity.this
                           , GnPhone.GEMINI_SIM_1); 
               SIMInfo simInfo2 = SIMInfo.getSIMInfoBySlot(AuroraMsgDetailActivity.this
                           , GnPhone.GEMINI_SIM_2);
               if (simInfo1 != null) {
                   mDaultCallSlot = 0;
               };
               if (simInfo2 != null) {
                   mDaultCallSlot = 1;
               };
              }
          // Aurora xuyong 2014-09-10 modified for uptimize start
            this.getContentResolver().registerContentObserver(SimInfo.CONTENT_URI, true, mSimInfoObserver);
          // Aurora xuyong 2014-09-10 modified for uptimize end
            mSimStateChangedFilter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
            mSimStateChangedFilter.addAction("android.intent.action.PHB_STATE_CHANGED");
            registerReceiver(mSimStateChangedReceiver, mSimStateChangedFilter);
        }
     }
     
    @Override
    protected void onStop() {
       super.onStop();
       // Aurora xuyong 2014-09-10 deleted for uptimize start
       //if (MmsApp.mGnMultiSimMessage && mSimStateChangedReceiver != null) {
       //       unregisterReceiver(mSimStateChangedReceiver);
       //    mSimStateChangedReceiver = null;
       //}
       // Aurora xuyong 2014-09-10 deleted for uptimize end
    }
    // Aurora xuyong 2014-09-10 added for uptimize start
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (MmsApp.mGnMultiSimMessage && mSimInfoObserver != null) {
            this.getContentResolver().unregisterContentObserver(mSimInfoObserver);
        }
        if (MmsApp.mGnMultiSimMessage && mSimStateChangedReceiver != null) {
           unregisterReceiver(mSimStateChangedReceiver);
           mSimStateChangedReceiver = null;
        }
    }
   // Aurora xuyong 2014-09-10 added for uptimize end
   // Aurora xuyong 2014-06-12 added for multisim feature end
    //Aurora xuyong 2013-10-11 modified for aurora's new feature end
    // Aurora xuyong 2014-04-04 added for aurora's new feature start
    private final int URI_INVALID = -1;
    private final int URI_PHONE = 1;
    private final int URI_HTTP = 2;
    private final int URI_MAIL = 3;
    private final int URI_RTSP = 4;
    
    private int getFlag(String url) {
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
    
    private GroupPop puw = null;
    
    class GroupPop extends PopupWindow {
        
        public GroupPop(View view) {
            super(view);
        }
        
        public void dismiss() {
            startMenuDismissThread();
            super.dismiss();
        }
        
    }
    
    private void startMenuDismissThread() {
        removeCoverView();
    }
    
    private void startMenuShowThread() {
        addCoverView();
    }
    
    public void dismissGroupMenu(PopupWindow puw) {
        puw.dismiss();
    }
    
    public void dismissGroupNewMenu(PopupWindow puw) {
        removeCoverView();
        puw.dismiss();
    }
    // Aurora xuyong 2014-04-21 added for bug #4438 start
    private Contact mCurrentContact;
   // Aurora xuyong 2014-04-21 added for bug #4438 end
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case AuroraMultiLinkAdapter.LINK_CLICK:
                    if (puw != null) {
                        dismissGroupMenu(puw);
                    }
                    mClickContent = (ClickContent)(msg.obj);
                // Aurora xuyong 2014-08-28 added for NullpointerException start
                    if (mClickContent == null && mClickContent.getValue() == null) {
                        return;
                    }
                // Aurora xuyong 2014-08-28 added for NullpointerException end
                    String value = mClickContent.getValue();
                    if (puw != null) {
                        dismissGroupMenu(puw);
                    }
                    setAuroraMenuAdapter(null);
                    switch (getFlag(value)) {
                         case URI_PHONE:
                        // Aurora xuyong 2014-04-21 added for bug #4438 start
                              String number = value.substring(4);
                             mCurrentContact = Contact.get(number, true);
                        // Aurora xuyong 2014-04-21 added for bug #4438 end
                        // Aurora xuyong 2014-06-12 added for multisim feature start
                             if (MmsApp.mGnMultiSimMessage && mInsertedSimCount == 2) {
                                 if (mCurrentContact.existsInDatabase()) {
                                     int slotIdEx = getLastCallSlotId(AuroraMsgDetailActivity.this, number);
                                     switch(slotIdEx) {
                                         case -1:
                                             setAuroraMenuItems(R.menu.aurora_msg_content_phone_exsit_menu_multisim);
                                             break;
                                         case 0:
                                             setAuroraMenuItems(R.menu.aurora_msg_content_phone_exsit_menu_multisim1f);
                                             break;
                                         case 1:
                                             setAuroraMenuItems(R.menu.aurora_msg_content_phone_exsit_menu_multisim2f);
                                             break;
                                     }
                                 } else if (!mCurrentContact.existsInDatabase()) {
                                     int slotIdNew = getLastCallSlotId(AuroraMsgDetailActivity.this, number);
                                     switch(slotIdNew) {
                                         case -1:
                                             setAuroraMenuItems(R.menu.aurora_msg_content_phone_menu_multisim);
                                             break;
                                         case 0:
                                             setAuroraMenuItems(R.menu.aurora_msg_content_phone_menu_multisim1f);
                                             break;
                                         case 1:
                                             setAuroraMenuItems(R.menu.aurora_msg_content_phone_menu_multisim2f);
                                             break;
                                     }
                                 }
                              } else {
                                  if (mCurrentContact.existsInDatabase()) {
                                      setAuroraMenuItems(R.menu.aurora_msg_content_phone_exsit_menu);
                                  } else {
                                      setAuroraMenuItems(R.menu.aurora_msg_content_phone_menu);
                                  }
                              }
                          // Aurora xuyong 2014-06-12 added for multisim feature end
                              break;
                         case URI_HTTP:
                              setAuroraMenuItems(R.menu.aurora_msg_content_http_menu);
                              break;
                         case URI_MAIL:
                              setAuroraMenuItems(R.menu.aurora_msg_content_mail_menu);
                              break;
                         case URI_RTSP:
                              setAuroraMenuItems(R.menu.aurora_msg_content_rtsp_menu);
                              break;
                         default:
                              break;
                     }
                    try {
                        showAuroraMenu();
                     } catch (Exception e) {
                        e.printStackTrace();
                     }
                break;
            }
         }
    };
    // Aurora xuyong 2014-06-12 added for multisim feature start
    public int getLastCallSlotId(Context context, String number) {

        int lastSimId = -1;
        String[] projection = { Calls.SIM_ID };
        Cursor cursor = null;
        if (null != context) {
            cursor = context.getContentResolver().query(Calls.CONTENT_URI,
                    projection, Calls.NUMBER + " = '" + number + "'", null,
                    "_id desc");
        }

        if (null != cursor) {
            if (true == cursor.moveToFirst()) {
                lastSimId = Integer.valueOf(cursor.getInt(0));
            }

            cursor.close();
        }

        SIMInfo simInfo1 = SIMInfo.getSIMInfoBySlot(this, GnPhone.GEMINI_SIM_1);
        SIMInfo simInfo2 = SIMInfo.getSIMInfoBySlot(this, GnPhone.GEMINI_SIM_2);
        long simId1 = -2;
        if (simInfo1 != null) {
            simId1 = simInfo1.mSimId;
        }
        long simId2 = -2;
        if (simInfo2 != null) {
            simId2 = simInfo2.mSimId;
        }
        
        if (lastSimId == simId1) {
            return 0;
        } else if (lastSimId == simId2) {
            return 1;
        } else {
            return -1;
        }
    }
    // Aurora xuyong 2014-06-12 added for multisim feature end
    /*private void initAutoLinks(TextView textView) {
        URLSpan[] spans = textView.getUrls();
        java.util.ArrayList<String> urlsold = MessageUtils.extractUris(spans);
        final java.util.ArrayList<String> urls = initComList(urlsold);
        if (urls.size() == 0) {
            return;
        } else if (urls.size() == 1) {
            mClickContent = new ClickContent();
            mClickContent.setValue(urls.get(0));
            if (puw != null) {
                dismissGroupMenu(puw);
            }
            setAuroraMenuAdapter(null);
            switch (getFlag(urls.get(0))) {
                 case URI_PHONE:
                 // Aurora xuyong 2014-04-21 added for bug #4438 start
                      String number = mClickContent.getValue().substring(4);
                        mCurrentContact = Contact.get(number, true);
                  // Aurora xuyong 2014-06-12 modified for multisim feature start
                     if (MmsApp.mGnMultiSimMessage && mInsertedSimCount == 2) {
                        if (mCurrentContact.existsInDatabase()) {
                            int slotIdEx = getLastCallSlotId(AuroraMsgDetailActivity.this, number);
                            switch(slotIdEx) {
                                case -1:
                                    setAuroraMenuItems(R.menu.aurora_msg_content_phone_exsit_menu_multisim);
                                    break;
                                case 0:
                                    setAuroraMenuItems(R.menu.aurora_msg_content_phone_exsit_menu_multisim1f);
                                    break;
                                case 1:
                                    setAuroraMenuItems(R.menu.aurora_msg_content_phone_exsit_menu_multisim2f);
                                    break;
                            }
                        } else if (!mCurrentContact.existsInDatabase()) {
                            int slotIdNew = getLastCallSlotId(AuroraMsgDetailActivity.this, number);
                            switch(slotIdNew) {
                                case -1:
                                    setAuroraMenuItems(R.menu.aurora_msg_content_phone_menu_multisim);
                                    break;
                                case 0:
                                    setAuroraMenuItems(R.menu.aurora_msg_content_phone_menu_multisim1f);
                                    break;
                                case 1:
                                    setAuroraMenuItems(R.menu.aurora_msg_content_phone_menu_multisim2f);
                                    break;
                            }
                        }
                    } else {
                         if (mCurrentContact.existsInDatabase()) {
                             setAuroraMenuItems(R.menu.aurora_msg_content_phone_exsit_menu);
                         } else {
                             setAuroraMenuItems(R.menu.aurora_msg_content_phone_menu);
                         }
                    }
                 // Aurora xuyong 2014-06-12 modified for multisim feature end
                 // Aurora xuyong 2014-04-21 added for bug #4438 end
                      break;
                 case URI_HTTP:
                      setAuroraMenuItems(R.menu.aurora_msg_content_http_menu);
                      break;
                 case URI_MAIL:
                      setAuroraMenuItems(R.menu.aurora_msg_content_mail_menu);
                      break;
                 case URI_RTSP:
                      setAuroraMenuItems(R.menu.aurora_msg_content_rtsp_menu);
                      break;
                 default:
                      break;
             }
            try {
                showAuroraMenu();
             } catch (Exception e) {
                e.printStackTrace();
             }
        } else {
            if (puw != null) {
                   dismissGroupNewMenu(puw);
              }
            mClickContent = new ClickContent();
            mClickContent.setValues(urls);
            LinearLayout popGroup = (LinearLayout)((LayoutInflater)LayoutInflater.from(AuroraMsgDetailActivity.this)).inflate(R.layout.aurora_group_list_callback, null);
            popGroup.setFocusable(true);
            popGroup.setFocusableInTouchMode(true);
            ListView linkList = (ListView)popGroup.findViewById(R.id.aurora_group_call_list);
            AuroraMultiLinkAdapter adapter = new AuroraMultiLinkAdapter(AuroraMsgDetailActivity.this, urls, mHandler);
            linkList.setAdapter(adapter);
            DisplayMetrics dm = new DisplayMetrics();
            AuroraMsgDetailActivity.this.getWindowManager().getDefaultDisplay().getMetrics(dm);
            int linkCount = adapter.getCount();
            linkList.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int)((linkCount > 6 ? 6.6 : linkCount) * 47 * dm.density)));
            puw = new GroupPop(popGroup);
            popGroup.setOnClickListener(new View.OnClickListener() {
                    
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    dismissGroupMenu(puw);
                }
            });
            popGroup.setOnKeyListener(new View.OnKeyListener() {
                
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    // TODO Auto-generated method stub
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        switch(keyCode) {
                            case KeyEvent.KEYCODE_BACK:
                                dismissGroupMenu(puw);
                                break;
                            default:
                                break;
                         }
                     }
                      return false;
                  }
             });
             puw.setWindowLayoutMode(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
             puw.setAnimationStyle(R.style.ActionBottomBarPopupAnimation);
             puw.setFocusable(true);
             puw.setBackgroundDrawable(new BitmapDrawable());
             puw.setOutsideTouchable(true);

             if (puw != null && !puw.isShowing()) {
                 startMenuShowThread();
                 puw.showAtLocation(decorView,
                        Gravity.BOTTOM, 0, 0);                     
             }
            
        }
    }
    */
    private ArrayList<String> initComList(ArrayList<String> list) {
        ArrayList<String> nl = new ArrayList();
        for (String s : list) {
            if (!nl.contains(s)) {
                nl.add(s);
            }
        }
        return nl;
    }
    
     private ClickContent mClickContent;
     // Aurora xuyong 2014-06-12 added for multisim feature start
     private void multiSimDialCorresRecipient(int slotId, String number, Boolean isVideoCall) {
        Intent dialIntent = AuroraTelephoneManager.getCallNumberIntent(number, slotId);
        startActivity(dialIntent);
     }
     // Aurora xuyong 2014-06-12 added for multisim feature end
    private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {
        
        @Override
        public void auroraMenuItemClick(int itemId) {
            switch(itemId) {
                case R.id.aurora_msg_phone_dail:
                    if (mClickContent != null && mClickContent.getValue() != null) {
                     // Aurora xuyong 2014-06-12 modified for multisim feature start
                        if (MmsApp.mGnMultiSimMessage) {
                               Intent dialIntent = AuroraTelephoneManager.getCallNumberIntent(mClickContent.getValue().substring(4), mDaultCallSlot);
                               startActivity(dialIntent); 
                           } else {
                               Intent dialIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mClickContent.getValue().substring(4)));
                            startActivity(dialIntent);
                           }
                     // Aurora xuyong 2014-06-12 modified for multisim feature end
                    }
                    break;
             // Aurora xuyong 2014-06-12 added for multisim feature start
                case R.id.aurora_msg_phone_dail_sim1:
                // Aurora xuyong 2014-08-28 modified for NullpointerException start
                    if (mClickContent != null && mClickContent.getValue() != null) {
                        multiSimDialCorresRecipient(0, mClickContent.getValue().substring(4), false);
                    }
                // Aurora xuyong 2014-08-28 modified for NullpointerException end
                    break;
                case R.id.aurora_msg_phone_dail_sim2:
                // Aurora xuyong 2014-06-18 modified for bug #5929 start
                // Aurora xuyong 2014-08-28 modified for NullpointerException start
                    if (mClickContent != null && mClickContent.getValue() != null) {
                        multiSimDialCorresRecipient(1, mClickContent.getValue().substring(4), false);
                    }
                // Aurora xuyong 2014-08-28 modified for NullpointerException end
                // Aurora xuyong 2014-06-18 modified for bug #5929 end
                    break;
             // Aurora xuyong 2014-06-12 added for multisim feature end
                case R.id.aurora_msg_phone_forward:
                    Intent smsIntent = new Intent();
                    smsIntent.setClassName(AuroraMsgDetailActivity.this, "com.android.mms.ui.ForwardMessageActivity");
                    if (mClickContent != null && mClickContent.getValue() != null) {
                        smsIntent.putExtra("SENDMSGNUMBER", mClickContent.getValue().substring(4));
                    }
                    smsIntent.putExtra("ISSENDMSG", true);
                    smsIntent.putExtra("forwarded_message", true);
                    AuroraMsgDetailActivity.this.startActivity(smsIntent);
                    break;
                case R.id.aurora_msg_phone_created:
                     {
                          Intent mgcIntent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
                          mgcIntent.setComponent(new ComponentName("com.android.contacts",
                               "com.android.contacts.activities.ContactEditorActivity"));
                          if (mClickContent != null && mClickContent.getValue() != null) {
                              mgcIntent.putExtra(Insert.PHONE, mClickContent.getValue().substring(4));
                          }
                          startActivity(mgcIntent);
                     }
                     break;
                /*case R.id.aurora_msg_phone_add:
                     {
                         Intent newintent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                         newintent.setComponent(new ComponentName("com.android.contacts",
                                   "com.android.contacts.activities.ContactSelectionActivity"));
                         if (mClickContent != null && mClickContent.getValue() != null) {
                             newintent.putExtra(Insert.PHONE, mClickContent.getValue().substring(4));
                         }
                         newintent.setType(People.CONTENT_ITEM_TYPE);
                         startActivity(newintent);
                     }
                     break;*/
                case R.id.aurora_msg_num_copy:
                     {
                         if (mClickContent != null && mClickContent.getValue() != null) {
                             copyToClipboard(mClickContent.getValue().substring(4));
                         }
                     }
                    break;
                case R.id.aurora_msg_http_visit:
                     {
                         if (mClickContent != null && mClickContent.getValue() != null) {
                            Uri mgvUri = Uri.parse(mClickContent.getValue());
                         // Aurora xuyong 2014-06-12 modified for multisim feature start
                           Intent httpIntent = new Intent(Intent.ACTION_VIEW, mgvUri);
                           httpIntent.putExtra(Browser.EXTRA_APPLICATION_ID, AuroraMsgDetailActivity.this.getPackageName());
                           httpIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                           AuroraMsgDetailActivity.this.startActivity(httpIntent);
                          // Aurora xuyong 2014-06-12 modified for multisim feature end
                         }
                     }
                     break;
                case R.id.aurora_msg_http_copy:
                     {
                     // Aurora xuyong 2014-08-28 modified for NullpointerException start
                         if (mClickContent != null && mClickContent.getValue() != null) {
                             String urlString = mClickContent.getValue().toString();
                             if (urlString.startsWith("http:")) {
                                 if (mClickContent != null && mClickContent.getValue() != null) {
                                     copyToClipboard(mClickContent.getValue().substring(7));
                                 }
                             } else if (urlString.startsWith("https:")) {
                                 if (mClickContent != null && mClickContent.getValue() != null) {
                                     copyToClipboard(mClickContent.getValue().substring(8));
                                 }
                             }
                     // Aurora xuyong 2014-08-28 modified for NullpointerException end
                         }
                     }
                     break;
                case R.id.aurora_msg_mail_send:
                     {
                         if (mClickContent != null && mClickContent.getValue() != null) {
                             Uri uri = Uri.parse(mClickContent.getValue());
                             Intent mmsIntent = new Intent(Intent.ACTION_VIEW, uri);
                          mmsIntent.putExtra(Browser.EXTRA_APPLICATION_ID, "com.android.mms");
                          mmsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                          AuroraMsgDetailActivity.this.startActivity(mmsIntent);
                         }
                     }
                     break;
                case R.id.aurora_msg_mail_created:
                     {
                         Intent mmcIntent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
                         mmcIntent.setComponent(new ComponentName("com.android.contacts",
                        "com.android.contacts.activities.ContactEditorActivity"));
                         if (mClickContent != null && mClickContent.getValue() != null) {
                             mmcIntent.putExtra(ContactsContract.Intents.Insert.EMAIL, mClickContent.getValue().substring(7));
                         }
                         AuroraMsgDetailActivity.this.startActivity(mmcIntent);
                     }
                     break;
               /* case R.id.aurora_msg_mail_add:
                     {
                         Intent mmaIntent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                         mmaIntent.setComponent(new ComponentName("com.android.contacts",
                             "com.android.contacts.activities.ContactSelectionActivity")); 
                         mmaIntent.setType(Contacts.CONTENT_ITEM_TYPE);
                         if (mClickContent != null && mClickContent.getValue() != null) {
                             mmaIntent.putExtra(ContactsContract.Intents.Insert.EMAIL, mClickContent.getValue().substring(7));
                         }
                         AuroraMsgDetailActivity.this.startActivity(mmaIntent);
                     }
                     break;*/
                case R.id.aurora_msg_mail_copy:
                     if (mClickContent != null && mClickContent.getValue() != null) {
                         copyToClipboard(mClickContent.getValue().substring(7));
                     }
                     break;
                case R.id.aurora_msg_rtsp_visit:
                     {
                    // Aurora xuyong 2014-08-28 modified for NullpointerException start
                        if (mClickContent != null && mClickContent.getValue() != null) {
                            Uri mrvUri = Uri.parse(mClickContent.getValue());
                            Intent msrvIntent = new Intent(Intent.ACTION_VIEW, mrvUri);
                            msrvIntent.putExtra(Browser.EXTRA_APPLICATION_ID, AuroraMsgDetailActivity.this.getPackageName());
                            msrvIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                            AuroraMsgDetailActivity.this.startActivity(msrvIntent);
                        }
                    // Aurora xuyong 2014-08-28 modified for NullpointerException end
                     }
                     break;
                case R.id.aurora_msg_rtsp_copy:
                     {
                         if (mClickContent != null && mClickContent.getValue() != null) {
                             copyToClipboard(mClickContent.getValue().substring(5));
                         }
                     }
                     break;
             // Aurora xuyong 2014-04-21 added for bug #4438 start
                case R.id.aurora_current_view:
                    if (mCurrentContact != null && mCurrentContact.existsInDatabase()) {
                         Uri contactUri = mCurrentContact.getUri();
                         Intent currentPhoneIntent = new Intent(Intent.ACTION_VIEW, contactUri);
                         currentPhoneIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                         startActivity(currentPhoneIntent);
                    }
                    break;
             // Aurora xuyong 2014-04-21 added for bug #4438 end
            }
        }
        
    };
    
    private void copyToClipboard(String str) {
        ClipboardManager clip =
            (ClipboardManager)AuroraMsgDetailActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
        clip.setText(str);
        if (str != null && str.length() > 0) {
            Toast.makeText(this, R.string.auroa_sms_copy_done, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.auroa_sms_copy_fail, Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        
        if (null != intent && null != intent.getData()
                && intent.getData().getScheme().equals("mailto")) {
            try {
                super.startActivityForResult(intent, requestCode);
            } catch (ActivityNotFoundException e) {
                Intent i = new Intent().setClassName("com.android.email", "com.android.email.activity.setup.AccountSetupBasics");
                this.startActivity(i);
            } catch (Exception e) {
                Toast.makeText(this,getString(R.string.message_open_email_fail),
                      Toast.LENGTH_SHORT).show();
          }
        } else {
            try {
                super.startActivityForResult(intent, requestCode);
            } catch (ActivityNotFoundException e) {
                Intent mchooserIntent = Intent.createChooser(intent, null);
                super.startActivityForResult(mchooserIntent, requestCode);
            }
        }
    }
    // Aurora xuyong 2014-04-04 added for aurora's new feature end
}
