/*
 * Copyright (C) 2012 gionee Inc.
 *
 * Author:gaoj
 *
 * Description:class for holding the data of recent contact data from database
 *
 * history
 * name                              date                                      description
 *
 */

package com.gionee.mms.ui;
// Aurora xuyong 2014-03-04 added for aurora's new feature start
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
// Aurora xuyong 2014-03-04 added for aurora's new feature end
import aurora.app.AuroraActivity;
// Aurora xuyong 2014-03-04 added for aurora's new feature start
import aurora.app.AuroraProgressDialog;
import android.content.ActivityNotFoundException;
// Aurora xuyong 2014-06-12 added for multisim feature start
import android.content.BroadcastReceiver;
// Aurora xuyong 2014-06-12 added for multisim feature end
import android.content.ComponentName;
import android.content.ContentResolver;
// Aurora xuyong 2014-06-12 added for multisim feature start
// Aurora xuyong 2016-03-21 modified for bug #21602 start
import android.content.DialogInterface;
// Aurora xuyong 2016-03-21 modified for bug #21602 end
import android.content.IntentFilter;
// Aurora xuyong 2014-06-12 added for multisim feature end
// Aurora xuyong 2014-03-04 added for aurora's new feature end
import android.content.Context;
import android.content.Intent;
// Aurora xuyong 2014-06-12 added for multisim feature start
import android.database.ContentObserver;
import android.database.Cursor;
// Aurora xuyong 2014-06-12 added for multisim feature end
import android.graphics.PixelFormat;
// Aurora xuyong 2013-12-30 added for aurora's ne feature start
import android.graphics.Typeface;
// Aurora xuyong 2014-03-04 added for aurora's new feature start
import android.graphics.drawable.BitmapDrawable;
// Aurora xuyong 2014-03-04 added for aurora's new feature end
// Aurora xuyong 2013-12-30 added for aurora's ne feature end
import android.net.Uri;
// Aurora xuyong 2014-03-04 added for aurora's new feature start
import android.os.AsyncTask;
// Aurora xuyong 2014-03-04 added for aurora's new feature end
import android.os.Bundle;
// Aurora xuyong 2014-03-04 modified for aurora's new feature start
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
// Aurora xuyong 2014-06-12 added for multisim feature start
import android.telephony.TelephonyManager;
// Aurora xuyong 2014-06-12 added for multisim feature end
import android.text.ClipboardManager;
import android.util.DisplayMetrics;
// Aurora xuyong 2014-03-04 modified for aurora's new feature end
import android.util.Log;
// Aurora xuyong 2014-03-04 modified for aurora's new feature start
import android.view.Gravity;
import android.view.KeyEvent;
// Aurora xuyong 2014-03-04 modified for aurora's new feature end
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
// Aurora xuyong 2014-03-04 modified for aurora's new feature start
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
// Aurora xuyong 2014-03-04 modified for aurora's new feature end
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
// Aurora xuyong 2014-03-04 modified for aurora's new feature start
import android.widget.ListView;
import android.widget.PopupWindow;
// Aurora xuyong 2014-03-04 modified for aurora's new feature end
// Aurora xuyong 2013-12-30 added for aurora's ne feature start
import android.widget.TextView;
// Aurora xuyong 2013-12-30 added for aurora's ne feature end
import aurora.widget.AuroraListView;
// Aurora xuyong 2013-12-30 added for aurora's ne feature start
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
// Aurora xuyong 2013-12-30 added for aurora's ne feature end
// Aurora xuyong 2014-06-12 added for multisim feature start
import gionee.provider.GnTelephony.SIMInfo;
import gionee.provider.GnTelephony.SimInfo;
import gionee.telephony.GnTelephonyManager;
import com.gionee.internal.telephony.GnPhone;
import com.android.internal.telephony.TelephonyIntents;
import gionee.telephony.AuroraTelephoneManager;
import gionee.provider.GnCallLog.Calls;
// Aurora xuyong 2014-06-12 added for multisim feature end
import com.android.mms.MmsApp;
import com.android.mms.R;
// Aurora xuyong 2014-04-21 added for bug #4438 start
import com.android.mms.data.Contact;
// Aurora xuyong 2014-04-21 added for bug #4438 end
import com.android.mms.model.LayoutModel;
import com.android.mms.model.RegionModel;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.Presenter;
import com.android.mms.ui.PresenterFactory;
import com.android.mms.ui.SlideshowPresenter;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.MmsException;
//Aurora xuyong 2013-11-15 modified for google adapt end
//gionee zhouyj 2012-05-14 add for CR00585826 start
// Aurora liugj 2013-09-13 modified for aurora's new feature start
import android.app.ActionBar;
// Aurora liugj 2013-09-13 modified for aurora's new feature end
import com.android.mms.ui.SlideshowActivity;
//gionee zhouyj 2012-05-14 add for CR00585826 end
//gionee zhouyj 2012-05-24 add for CR00608101 start
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.ContentType;
import com.aurora.android.mms.pdu.GenericPdu;
import com.aurora.android.mms.pdu.MultimediaMessagePdu;
import com.aurora.android.mms.pdu.PduBody;
import com.aurora.android.mms.pdu.PduHeaders;
import com.aurora.android.mms.pdu.PduPart;
import com.aurora.android.mms.pdu.PduPersister;
// Aurora xuyong 2014-03-04 added for aurora's new feature start
import com.aurora.mms.ui.ClickContent;
// Aurora xuyong 2014-05-05 added for aurora's new feature start
import com.aurora.mms.ui.ThumbnailWorker;
// Aurora xuyong 2014-05-05 added for aurora's new feature end
import com.aurora.view.AuroraMultiLinkAdapter;
// Aurora xuyong 2014-09-16 added for bug #8331 start
import com.aurora.view.AuroraURLSpan;
// Aurora xuyong 2014-09-16 added for bug #8331 end
// Aurora xuyong 2014-03-04 added for aurora's new feature end
//Aurora xuyong 2013-11-15 modified for google adapt end
import android.widget.Toast;
// Aurora xuyong 2014-03-04 added for aurora's new feature start
import android.widget.LinearLayout.LayoutParams;
// Aurora xuyong 2014-03-04 added for aurora's new feature end
//gionee zhouyj 2012-05-24 add for CR00608101 end

//gionee wangym 2012-11-22 add for CR00735223 start
import com.android.mms.ui.ScaleDetector.OnScaleListener;
import android.view.MotionEvent;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.ScaleDetector;
import android.os.SystemProperties;
//gionee wangym 2012-11-22 add for CR00735223 end
// Aurora xuyong 2014-03-04 modified for aurora's new feature start
import android.provider.Browser;
import android.provider.ContactsContract;
import android.provider.Contacts.People;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Intents.Insert;
// Aurora xuyong 2014-03-04 modified for aurora's new feature end
// Aurora xuyong 2016-03-21 modified for bug #21602 start
import aurora.app.AuroraAlertDialog;
// Aurora xuyong 2016-03-21 modified for bug #21602 end

public class SlidesBrowserActivity extends AuroraActivity{
    private static final String TAG = "SlidesBrowserActivity";

    LinearLayout mSlideBrowserView;
    private AuroraListView mList;
    private SlideBrowserViewListAdapter mSlideListAdapter;
    // Aurora xuyong 2014-05-07 modified for bug 4693 start
    public static ThumbnailWorker mThumbnailWorker;
    // Aurora xuyong 2014-05-07 modified for bug 4693 end
    // Aurora xuyong 2013-12-30 added for aurora's ne feature start
    private LinearLayout mTitleLayout;
    private TextView mSubjectTextView;
    private TextView mTimeTextView;
    // Aurora xuyong 2013-12-30 added for aurora's ne feature end
    //gionee zhouyj 2012-05-14 added for CR00585826 start
    private final int MENU_PICKER = 1;
    private final int MENU_PLAY_MMS = MENU_PICKER + 1;
    public static final int REQUEST_CODE_CHOOSE_SDCARD    = 1;
    //gionee zhouyj 2012-05-14 added for CR00585826 end
    // Aurora xuyong 2014-03-04 added for aurora's new feature start
    private AuroraActionBar mAuroraActionBar;
    private AuroraMenu mAuroraMenu;
    private ContentResolver mContentResolver;
    private Uri mMsgUri;
    private Uri mCurrenPickUri;
    
    Window window;
    View decorView;
    WindowManager.LayoutParams wl;
    // Aurora xuyong 2014-03-04 added for aurora's new feature start
    // Aurora xuyong 2014-06-12 added for multisim feature start
    private int mInsertedSimCount = 0;
    private GnTelephonyManager mTelephonyManager;
    private int mDaultCallSlot = -1;
   // Aurora xuyong 2014-06-12 added for multisim feature end
    public SlidesBrowserActivity(){

    }

    @Override
    public void onCreate(Bundle icicle) {
        //gionee gaoj 2012-6-27 added for CR00628364 start
        // Aurora xuyong 2013-12-30 deleted for aurora's ne feature start
        /*if (MmsApp.mLightTheme) {
            setTheme(R.style.GnMmsLightTheme);
        } else if (MmsApp.mDarkStyle) {
            setTheme(R.style.GnMmsDarkTheme);
        }*/
        // Aurora xuyong 2013-12-30 deleted for aurora's ne feature end
        //gionee gaoj 2012-6-27 added for CR00628364 end
        super.onCreate(icicle);
        // Aurora xuyong 2014-03-04 added for aurora's new feature start
        window = this.getWindow();
        wl = window.getAttributes();
        decorView = window.getDecorView();
        // Aurora xuyong 2014-05-07 modified for bug 4693 start
        mThumbnailWorker = new ThumbnailWorker(this);
        // Aurora xuyong 2014-05-07 modified for bug 4693 end
        // Aurora xuyong 2014-03-04 added for aurora's new feature end
        // gionee zhouyj 2012-04-28 annoted for CR00585826 start
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFormat(PixelFormat.TRANSLUCENT);
        // gionee zhouyj 2012-04-28 annoted for CR00585826 end
        // Aurora xuyong 2013-12-30 modified for aurora's ne feature start
        //setContentView(R.layout.gn_slidesbrowser);
        setAuroraContentView(R.layout.gn_slidesbrowser,
                AuroraActionBar.Type.Custom);
        // Aurora xuyong 2013-12-30 modified for aurora's ne feature end
        // Aurora xuyong 2014-03-04 added for aurora's new feature start
        mContentResolver = getContentResolver();
        // Aurora xuyong 2014-03-04 added for aurora's new feature end
        mList = (AuroraListView)findViewById(R.id.slidesbrowser_listview);
        // Aurora xuyong 2013-12-30 deleted for aurora's ne feature start
        //mList.setDividerHeight(10);
        //mList.setDivider(getResources().getDrawable(R.drawable.gn_ic_mms_slide_devider));
        // Aurora xuyong 2013-12-30 deleted for aurora's ne feature end
        mList.setItemsCanFocus(false);
        // Aurora xuyong 2031-12-31 added for aurora's new features start
        mList.setFastScrollEnabled(false);
        // Aurora xuyong 2031-12-31 added for aurora's new features end
        // Aurora xuyong 2014-09-16 added for bug #8331 start
        // Aurora xuyong 2015-02-04 deleted for bug #11531 start
        //AuroraURLSpan.setHandler(mHandler);
        // Aurora xuyong 2015-02-04 deleted for bug #11531 end
        // Aurora xuyong 2014-09-16 added for bug #8331 end
        Intent intent = getIntent();
        // Aurora xuyong 2014-03-04 modified for aurora's new feature start
        mMsgUri = intent.getData();
        // Aurora xuyong 2014-03-04 modified for aurora's new feature end
        final SlideshowModel model;

        try {
            // Aurora xuyong 2014-03-04 modified for aurora's new feature start
            model = SlideshowModel.createFromMessageUri(this, mMsgUri);
            // Aurora xuyong 2014-03-04 modified for aurora's new feature end
        } catch (MmsException e) {
            Log.e(TAG, "Cannot present the slides browser.", e);
            finish();
            return;
        }

        mSlideListAdapter = new SlideBrowserViewListAdapter(
                this, R.layout.gn_slidesbrowser_item, model);
        mList.setAdapter(mSlideListAdapter);

        Log.i(TAG, "model size " + model.size());
        //gionee gaoj 2012-5-29 added for CR00555790 start
        // Aurora xuyong 2013-12-30 modified for aurora's ne feature start
        /*if (MmsApp.mGnMessageSupport) {
            // Aurora liugj 2013-09-13 modified for aurora's new feature start
            AuroraActionBar actionBar = getAuroraActionBar();
            // Aurora liugj 2013-09-13 modified for aurora's new feature end
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }*/
        //gionee gaoj 2012-5-29 added for CR00555790 end
        // Aurora xuyong 2014-03-04 modified for aurora's new feature start
        mAuroraActionBar = getAuroraActionBar();
        mAuroraActionBar.setCustomView(R.layout.aurora_message_title_area);
        mAuroraMenu = mAuroraActionBar.getActionBarMenu();
        // Aurora xuyong 2016-01-14 modified for aurora 2.0 new feature start
        setAuroraSystemMenuCallBack(auroraMenuCallBack);
        // Aurora xuyong 2016-01-14 modified for aurora 2.0 new feature end
        mTitleLayout = (LinearLayout)mAuroraActionBar.getCustomView(R.id.aurora_title);
        // Aurora xuyong 2014-03-04 modified for aurora's new feature end
        mTitleLayout.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish();
            }
        });
        mSubjectTextView = (TextView)mTitleLayout.findViewById(R.id.aurora_title_name);
        mTimeTextView = (TextView)mTitleLayout.findViewById(R.id.aurora_title_number_area);
        String subject = intent.getStringExtra("subject");
        if (subject == null) {
            mSubjectTextView.setText(this.getResources().getString(R.string.no_subject));
        } else {
            mSubjectTextView.setText(intent.getStringExtra("subject"));
        }
        // Aurora xuyong 2015-03-04 deletetd for bug #11930 start
        /*Typeface ttf = Typeface.createFromFile("system/fonts/number.ttf");
        mTimeTextView.setTypeface(ttf);*/
        // Aurora xuyong 2015-03-04 deletetd for bug #11930 end
        // Aurora xuyong 2014-02-13 modified for aurora's new feature start
        String timeStamp = intent.getStringExtra("timestamp");
        if (timeStamp == null) {
          // Aurora xuyong 2014-08-28 modified for aurora's new feature start
            mTimeTextView.setText(this.getResources().getString(R.string.aurora_mms_sending));
          // Aurora xuyong 2014-08-28 modified for aurora's new feature end
        } else {
            mTimeTextView.setText(intent.getStringExtra("timestamp"));
        }
        // Aurora xuyong 2014-02-13 modified for aurora's new feature end
        // Aurora xuyong 2013-12-30 modified for aurora's ne feature end

        //gionee wangym 2012-11-22 add for CR00735223 start
        if(MmsApp.mIsTouchModeSupport ){
            mIsCmcc = true;
            float size = MessageUtils.getTextSize(this);
            mTextSize = size;
            // Aurora xuyong 2013-12-30 deleted for aurora's ne feature start
            //mScaleDetector = new ScaleDetector(this, new ScaleListener());
            // Aurora xuyong 2013-12-30 deleted for aurora's ne feature end
        }
        //gionee wangym 2012-11-22 add for CR00735223 end
    }
    
    // gionee zhouyj 2012-04-28 added for CR00585826 start
    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        // Aurora xuyong 2013-12-30 deleted for aurora's ne feature start
        // Aurora liugj 2013-09-13 modified for aurora's new feature start
        //ActionBar actionBar = getActionBar();
        // Aurora liugj 2013-09-13 modified for aurora's new feature end
        //actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setTitle(R.string.mms);
        // Aurora xuyong 2013-12-30 deleted for aurora's ne feature end
    }
    // Aurora xuyong 2014-05-05 added for aurora's new feature start
    // Aurora xuyong 2014-06-12 added for multisim feature start
    private IntentFilter mSimStateChangedFilter = new IntentFilter();
    private BroadcastReceiver mSimStateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(intent.getAction())
                    || intent.getAction().equals("android.intent.action.PHB_STATE_CHANGED")) {
             // Aurora xuyong 2014-06-07 added for bug #5449 start
                if (MmsApp.mGnMultiSimMessage) {
                    mInsertedSimCount = SIMInfo.getInsertedSIMCount(SlidesBrowserActivity.this);
                    if (mInsertedSimCount == 2) {
                        SIMInfo simInfo1 = SIMInfo.getSIMInfoBySlot(SlidesBrowserActivity.this
                                    , GnPhone.GEMINI_SIM_1); 
                         SIMInfo simInfo2 = SIMInfo.getSIMInfoBySlot(SlidesBrowserActivity.this
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
                         SIMInfo simInfo1 = SIMInfo.getSIMInfoBySlot(SlidesBrowserActivity.this
                                    , GnPhone.GEMINI_SIM_1); 
                         SIMInfo simInfo2 = SIMInfo.getSIMInfoBySlot(SlidesBrowserActivity.this
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
                 mInsertedSimCount = SIMInfo.getInsertedSIMCount(SlidesBrowserActivity.this);
                 if (mInsertedSimCount == 2) {
                     SIMInfo simInfo1 = SIMInfo.getSIMInfoBySlot(SlidesBrowserActivity.this
                                 , GnPhone.GEMINI_SIM_1); 
                      SIMInfo simInfo2 = SIMInfo.getSIMInfoBySlot(SlidesBrowserActivity.this
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
                     SIMInfo simInfo1 = SIMInfo.getSIMInfoBySlot(SlidesBrowserActivity.this
                                 , GnPhone.GEMINI_SIM_1); 
                      SIMInfo simInfo2 = SIMInfo.getSIMInfoBySlot(SlidesBrowserActivity.this
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
    // Aurora xuyong 2014-06-12 added for multisim feature end
    @Override
    protected void onResume() {
        super.onResume();
       // Aurora xuyong 2014-06-12 added for multisim feature start
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
                  SIMInfo simInfo1 = SIMInfo.getSIMInfoBySlot(SlidesBrowserActivity.this
                           , GnPhone.GEMINI_SIM_1); 
               SIMInfo simInfo2 = SIMInfo.getSIMInfoBySlot(SlidesBrowserActivity.this
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
        // Aurora xuyong 2014-06-12 added for multisim feature end
        ThumbnailWorker.setNeedCache(false);
    }
    // Aurora xuyong 2014-06-12 added for multisim feature start
    @Override
    protected void onStop() {
       super.onStop();
       if (MmsApp.mGnMultiSimMessage && mSimStateChangedReceiver != null) {
              unregisterReceiver(mSimStateChangedReceiver);
           mSimStateChangedReceiver = null;
       }
     }
    // Aurora xuyong 2014-06-12 added for multisim feature end
    // Aurora xuyong 2014-05-05 added for aurora's new feature end
    // gionee zhouyj 2012-04-28 added for CR00585826 end
    // Aurora xuyong 2014-03-04 deleted for aurora's new feature start
    //@Override
    // gionee zhouyj 2012-05-14 modified for CR00585826 start
    /*public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_PICKER, 0, getString(R.string.attachmentpicker)).setIcon(R.drawable.gn_atttach_save).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, MENU_PLAY_MMS, 0, getString(R.string.play_as_slideshow)).setIcon(R.drawable.ic_menu_movie).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        // gionee zhouyj 2012-05-24 add for CR00608101 start
        try {
            if(getAttachmentCount(getIntent().getData()) <= 0){
                menu.getItem(0).setEnabled(false).setIcon(R.drawable.gn_atttach_save_dis);
            }
        } catch (MmsException e) {

            Log.i(TAG, "AttachmentListAdapter get attach error");
            return false;
        }
        // gionee zhouyj 2012-05-24 add for CR00608101 end
        return super.onCreateOptionsMenu(menu);
    }*/
    // gionee zhouyj 2012-05-14 modified for CR00585826 end

    //@Override
    // gionee zhouyj 2012-05-14 modified for CR00585826 start
    /*public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch(item.getItemId()) {
        case MENU_PICKER:
            // gionee zhouyj 2012-08-01 modify for CR00662594 start 
            if(MmsApp.mStorageMountedCount == 2) {
                Intent i = new Intent("android.intent.action.choosesdcard");
                i.putExtra("tips", getString(R.string.copy_attachment_to));
                i.putExtra("uri", getIntent().getData().toString());
                startActivityForResult(i, REQUEST_CODE_CHOOSE_SDCARD);
            } else if(MmsApp.mStorageMountedCount == 1) {
                Intent i = new Intent(getBaseContext(), AttachmentPickerActivity.class);
                i.setData(getIntent().getData());
                startActivity(i);
            } else {
                Toast.makeText(this, getString(R.string.gn_no_sdcard), Toast.LENGTH_SHORT).show();
            }
            // gionee zhouyj 2012-08-01 modify for CR00662594 end 
            return true;
        case MENU_PLAY_MMS:
            intent = new Intent(getBaseContext(), SlideshowActivity.class);
            intent.setData(getIntent().getData());
            startActivity(intent);
            break;
        case android.R.id.home:
            finish();
            break;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }*/
    // gionee zhouyj 2012-05-14 modified for CR00585826 start
    
    //@Override
    /*protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if(REQUEST_CODE_CHOOSE_SDCARD == requestCode && RESULT_OK == resultCode) {
            if(data != null && data.getAction().equals("ChooseSdcard")) {
                String uriString = data.getStringExtra("uri");
                if(uriString != null) {
                    Intent i = new Intent(getBaseContext(), AttachmentPickerActivity.class);
                    i.putExtra("position", data.getIntExtra("position", 0));
                    i.setData(Uri.parse(uriString));
                    startActivity(i);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }*/
    // Aurora xuyong 2014-03-04 deleted for aurora's new feature end
    // Aurora xuyong 2014-03-04 added for aurora's new feature start
    private final int IMAGE_PICK_INDEX = 7;
    private final int VIDEO_PICK_INDEX = 8;
    private final int OTHER_PICK_INDEX = 9;
    private ClickContent mClickContent;
    
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
                case SlidesBrowserItemView.MODEL_LINK_CLICK:
                case AuroraMultiLinkAdapter.LINK_CLICK:
                    if (puw != null) {
                        dismissGroupMenu(puw);
                    }
                    mClickContent = (ClickContent)(msg.obj);
                // Aurora xuyong 2014-08-28 added for NullpointerException start
                    if (mClickContent == null || mClickContent.getValue() == null) {
                        return;
                    }
                    // Aurora xuyong 2016-03-21 modified for bug #21602 start
                    createLinkClickAction();
                    // Aurora xuyong 2016-03-21 modified for bug #21602 end
                    break;
                case SlidesBrowserItemView.MODEL_MULTI_LINKS_CLICK:
                     if (puw != null) {
                            dismissGroupNewMenu(puw);
                        }
                 // Aurora xuyong 2014-08-28 added for NullpointerException start
                     if (mClickContent == null || mClickContent.getValues() == null) {
                         return;
                     }
                 // Aurora xuyong 2014-08-28 added for NullpointerException end
                     mClickContent = (ClickContent)msg.obj;
                        ArrayList<String> values = mClickContent.getValues();
                        LinearLayout popGroup = (LinearLayout)((LayoutInflater)LayoutInflater.from(SlidesBrowserActivity.this)).inflate(R.layout.aurora_group_list_callback, null);
                        popGroup.setFocusable(true);
                        popGroup.setFocusableInTouchMode(true);
                        ListView linkList = (ListView)popGroup.findViewById(R.id.aurora_group_call_list);
                        AuroraMultiLinkAdapter adapter = new AuroraMultiLinkAdapter(SlidesBrowserActivity.this, values, mHandler);
                        linkList.setAdapter(adapter);
                        DisplayMetrics dm = new DisplayMetrics();
                        SlidesBrowserActivity.this.getWindowManager().getDefaultDisplay().getMetrics(dm);
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
                    break;
                case IMAGE_PICK_INDEX:
                       Toast.makeText(SlidesBrowserActivity.this, R.string.aurora_image_saved, Toast.LENGTH_SHORT).show();
                    break;
                case VIDEO_PICK_INDEX:
                       Toast.makeText(SlidesBrowserActivity.this, R.string.aurora_video_saved, Toast.LENGTH_SHORT).show();
                    break;
                case OTHER_PICK_INDEX:
                       Toast.makeText(SlidesBrowserActivity.this, R.string.aurora_other_saved, Toast.LENGTH_SHORT).show();
                    break;
                case SlidesBrowserItemView.MODEL_ATTACHMENTS_PICK:
                    mCurrenPickUri = (Uri)(msg.obj);
                    setAuroraMenuAdapter(null);
                    setAuroraMenuItems(R.menu.aurora_attach_pick_menu);
                    try {
                        showAuroraMenu();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
        
    };
    
    private AuroraProgressDialog mPickDialog;
    private class PickSelectAttachmentAsyncTask extends AsyncTask<Uri, Void, Boolean> {
        
        private final String sFileSavedPath1 = Environment.getExternalStorageDirectory() + File.separator + "DCIM"
                + File.separator + "mms";
        private final String sFileSavedPath2 = Environment.getExternalStorageDirectory() + File.separator + "Download";
        
        @Override 
        protected void onPreExecute() {
            if (mPickDialog == null) {
                mPickDialog = new AuroraProgressDialog(SlidesBrowserActivity.this);
                mPickDialog.setIndeterminate(true);
                mPickDialog.setProgressStyle(AuroraProgressDialog.STYLE_SPINNER);
                mPickDialog.setCanceledOnTouchOutside(false);
                mPickDialog.setCancelable(false);
                mPickDialog.setMessage(SlidesBrowserActivity.this.
                        getText(R.string.aurora_saving_attachments));
            }
            mPickDialog.show();
        }
        
        @Override
        protected Boolean doInBackground(Uri... params) {
            // TODO Auto-generated method stub
            try {
                return new Boolean(pickAttachments(params[0]));
            } catch(MmsException e) {
                return new Boolean(false);
            }
        }
        
        private boolean pickAttachments(Uri msgUri) throws MmsException {
            PduPersister p = PduPersister.getPduPersister(SlidesBrowserActivity.this);
            GenericPdu pdu = p.load(mMsgUri);
            PduBody pduBody = null;
            boolean hasNoException = true;
            int msgType = pdu.getMessageType();
            if ((msgType == PduHeaders.MESSAGE_TYPE_SEND_REQ)
                    || (msgType == PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF)) {
                pduBody = ((MultimediaMessagePdu) pdu).getBody();
            }
            
             if (pduBody == null) {
                 throw new MmsException();
             }
             
             int partNum = pduBody.getPartsNum();
             int i = 0;
             while (i < partNum) {
                 if (!hasNoException) {
                     break;
                 }
                 PduPart pduPart = pduBody.getPart(i);
                 if (pduPart.getDataUri().toString().equals(msgUri.toString())) {
                     String type = new String(pduPart.getContentType());
                     if (type.contains("image")) {
                         hasNoException = pickContents(pduPart, sFileSavedPath1);
                         Message msg = Message.obtain(mHandler, IMAGE_PICK_INDEX);
                         msg.sendToTarget();
                     } else if (type.contains("video")) {
                         hasNoException = pickContents(pduPart, sFileSavedPath1);
                         Message msg = Message.obtain(mHandler, VIDEO_PICK_INDEX);
                         msg.sendToTarget();
                     } else if (!(type.contains("application/smil") || type.contains("text/plain"))) {
                         hasNoException = pickContents(pduPart, sFileSavedPath2);
                         Message msg = Message.obtain(mHandler, OTHER_PICK_INDEX);
                         msg.sendToTarget();
                     }
                     break;
                 }
                 i++;
             }
             return hasNoException;
        }
        
        private File getAUniqueDestination(String base, String extension) {
            File file = new File(base + "." + extension);

            for (int i = 2; file.exists(); i++) {
                file = new File(base + "_" + i + "." + extension);
            }
            return file;
         }
         
         private boolean pickContents(PduPart part, String destinatePath) {
             InputStream input = null;
             FileOutputStream fout = null;
             String newFileName = null;
             String extension = null;
             try {
                 input = mContentResolver.openInputStream(part.getDataUri());
                 if (input instanceof FileInputStream) {
                     FileInputStream fin = (FileInputStream) input;
                     byte[] location = part.getContentLocation();
                     if (location == null) {
                         location = part.getFilename();
                     }
                     if (location == null) {
                         location = part.getName();
                     }
                     String fileName;
                     if (location == null) {
                         // Use " " as fallback name.
                         fileName = " ";
                     } else {
                         fileName = new String(location);
                     }
                     
                     int index;
    
                     fileName = fileName.replace(' ', '_');
                     fileName = fileName.replace(':', '-');
    
                     if ((index = fileName.lastIndexOf(".")) == -1) {
                         String type = new String(part.getContentType());
                         extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(type);
                     } else {
                         extension = fileName.substring(index + 1, fileName.length());
                         fileName = fileName.substring(0, index);
                     }
                     newFileName = fileName.replace('.', '_');
                     File file = getAUniqueDestination(destinatePath + File.separator + newFileName, extension);
                     File parentFile = file.getParentFile();
                     if (!parentFile.exists() && !parentFile.mkdirs()) {
                         // can't save this attachment, so we return false
                         return false;
                     }
                     
                     fout = new FileOutputStream(file);
    
                     byte[] buffer = new byte[8000];
                     int size = 0;
                     while ((size = fin.read(buffer)) != -1) {
                         fout.write(buffer, 0, size);
                     }
                 }
             } catch (IOException e) {
                 return false;
             } finally {
                 if (null != input) {
                     try {
                         input.close();
                     } catch (IOException e) {
                         // Ignore
                         return false;
                     }
                 }
                 if (null != fout) {
                     try {
                         fout.close();
                     } catch (IOException e) {
                         // Ignore
                         return false;
                     }
                 }
             }
             return true;
        }
        
        @Override 
        protected void onPostExecute(Boolean result) {
            mPickDialog.dismiss();
            if (result) {
                Toast.makeText(SlidesBrowserActivity.this, R.string.aurora_saving_finished, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SlidesBrowserActivity.this, R.string.aurora_saving_error, Toast.LENGTH_SHORT).show();
            }
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
    // Aurora xuyong 2014-06-12 added for multisim feature start
    private void multiSimDialCorresRecipient(int slotId, String number, Boolean isVideoCall) {
        Intent dialIntent = AuroraTelephoneManager.getCallNumberIntent(number, slotId);
        startActivity(dialIntent);
     }
    
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
    private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {
        
        @Override
        public void auroraMenuItemClick(int itemId) {
            switch(itemId) {
            case R.id.aurora_attachment_pick:
                new PickSelectAttachmentAsyncTask().execute(mCurrenPickUri);
                break;
            case R.id.aurora_msg_phone_dail:
             // Aurora xuyong 2014-06-12 modified for multisim feature start
                if (mClickContent != null && mClickContent.getValue() != null) { 
                    if (MmsApp.mGnMultiSimMessage) {
                           Intent dialIntent = AuroraTelephoneManager.getCallNumberIntent(mClickContent.getValue().substring(4), mDaultCallSlot);
                           startActivity(dialIntent); 
                       } else {
                           Intent dialIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mClickContent.getValue().substring(4)));
                        startActivity(dialIntent);
                       }
                }
              // Aurora xuyong 2014-06-12 modified for multisim feature end
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
                smsIntent.setClassName(SlidesBrowserActivity.this, "com.android.mms.ui.ForwardMessageActivity");
                if (mClickContent != null && mClickContent.getValue() != null) {
                    smsIntent.putExtra("SENDMSGNUMBER", mClickContent.getValue().substring(4));
                }
                smsIntent.putExtra("ISSENDMSG", true);
                smsIntent.putExtra("forwarded_message", true);
                SlidesBrowserActivity.this.startActivity(smsIntent);
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
                        Intent intent = new Intent(Intent.ACTION_VIEW, mgvUri);
                       // Aurora xuyong 2014-06-12 modified for multisim feature end
                        intent.putExtra(Browser.EXTRA_APPLICATION_ID, SlidesBrowserActivity.this.getPackageName());
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                        SlidesBrowserActivity.this.startActivity(intent);
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
                     SlidesBrowserActivity.this.startActivity(mmsIntent);
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
                     SlidesBrowserActivity.this.startActivity(mmcIntent);
                 }
                 break;
            /*case R.id.aurora_msg_mail_add:
                 {
                     Intent mmaIntent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                     mmaIntent.setComponent(new ComponentName("com.android.contacts",
                         "com.android.contacts.activities.ContactSelectionActivity")); 
                     mmaIntent.setType(Contacts.CONTENT_ITEM_TYPE);
                     if (mClickContent != null && mClickContent.getValue() != null) {
                         mmaIntent.putExtra(ContactsContract.Intents.Insert.EMAIL, mClickContent.getValue().substring(7));
                     }
                     SlidesBrowserActivity.this.startActivity(mmaIntent);
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
                         msrvIntent.putExtra(Browser.EXTRA_APPLICATION_ID, SlidesBrowserActivity.this.getPackageName());
                         msrvIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                         SlidesBrowserActivity.this.startActivity(msrvIntent);
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
            (ClipboardManager)SlidesBrowserActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
        clip.setText(str);
        if (str != null && str.length() > 0) {
            Toast.makeText(this, R.string.auroa_sms_copy_done, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.auroa_sms_copy_fail, Toast.LENGTH_SHORT).show();
        }
    }
    // Aurora xuyong 2014-03-04 added for aurora's new feature end
    // Aurora xuyong 2014-03-04 modified for aurora's new feature start
    private class SlideBrowserViewListAdapter extends ArrayAdapter<SlideModel> {
    // Aurora xuyong 2014-03-04 modified for aurora's new feature end
        private final Context mContext;
        private final int mResource;
        private final LayoutInflater mInflater;
        private final SlideshowModel mSlideshow;

        public SlideBrowserViewListAdapter(Context context, int resource,
                SlideshowModel slideshow) {
            super(context, resource, slideshow);

            mContext = context;
            mResource = resource;
            mInflater = LayoutInflater.from(context);
            mSlideshow = slideshow;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return createViewFromResource(position, convertView, mResource);
        }

        private View createViewFromResource(int position, View convertView, int resource) {

            SlidesBrowserItemView slideListItemView;

            slideListItemView = (SlidesBrowserItemView) mInflater.inflate(
                    resource, null);
            // Aurora xuyong 2014-03-04 added for aurora's new feature start
            slideListItemView.setHandler(mHandler);
            // Aurora xuyong 2014-03-04 added for aurora's new feature end
            int imageLeft = 0;
            int imageTop = 0;
            int textLeft = 0;
            int textTop = 0;
            LayoutModel layout = mSlideshow.getLayout();
            if (layout != null) {
                // Aurora xuyong 2013-12-30 modified for aurora's ne feature start
                RegionModel textRegion = layout.getTextRegion();
                if (textRegion != null) {
                    textLeft = textRegion.getLeft();
                    textTop = textRegion.getTop();
                }
                RegionModel imageRegion = layout.getImageRegion();
                if (imageRegion != null) {
                    imageLeft = imageRegion.getLeft();
                    imageTop = imageRegion.getTop();
                }
                /*RegionModel textRegion = layout.getTextRegion();
                if (textRegion != null) {
                    textLeft = textRegion.getLeft();
                    textTop = textRegion.getTop();
                }*/
                // Aurora xuyong 2013-12-30 modified for aurora's ne feature end
            }
            // Aurora xuyong 2013-12-30 modified for aurora's ne feature start
            //slideListItemView.setSlideNumber(position);
            //slideListItemView.createModelViews(textLeft, textTop, imageLeft, imageTop);
            slideListItemView.createModelViews(imageLeft, imageTop, textLeft, textTop);
            slideListItemView.setSlideShowModel(mSlideshow.get(position));

            Presenter presenter = PresenterFactory.getPresenter(
                    "SlideshowPresenter", mContext, slideListItemView, mSlideshow);
            ((SlideshowPresenter) presenter).setLocation(position);
            presenter.present();
            slideListItemView.setOnClickListener(null);
            // Aurora xuyong 2013-12-30 modified for aurora's new feature start
            if (!(this.getCount() <= 1)) {
                slideListItemView.setSlideNumber(position);    
            }
            //slideListItemView.setSlideNumber(position);
            // Aurora xuyong 2013-12-30 modified for aurora's new feature end
            // Aurora xuyong 2013-12-30 modified for aurora's ne feature end

            return slideListItemView;
        }
    }
    
    // gionee zhouyj 2012-05-24 add for CR00608101 start
    private int getAttachmentCount(Uri uri) throws MmsException {
        PduPersister p = PduPersister.getPduPersister(this);
        GenericPdu pdu = p.load(uri);
        PduBody pduBody = null;
        int msgType = pdu.getMessageType();
        if ((msgType == PduHeaders.MESSAGE_TYPE_SEND_REQ)
                || (msgType == PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF)) {
            pduBody = ((MultimediaMessagePdu) pdu).getBody();
        }
        if (pduBody == null) {
            throw new MmsException();
        }
        int partNum = pduBody.getPartsNum();
        return partNum - 1;
    }
    // gionee zhouyj 2012-05-24 add for CR00608101 end

    //gionee wangym 2012-11-22 add for CR00735223 start
  @Override
  public boolean  dispatchTouchEvent(MotionEvent event){
      
      boolean ret = false;
      // Aurora xuyong 2013-12-30 deleted for aurora's ne feature start
      /* if(mIsCmcc && mScaleDetector != null){
              ret = mScaleDetector.onTouchEvent(event);
      }*/
      // Aurora xuyong 2013-12-30 deleted for aurora's ne feature end

      
      if(!ret){
          ret = super.dispatchTouchEvent(event); 
      }
      return ret;
  }
  
//MTK_OP01_PROTECT_START
  
  private final int DEFAULT_TEXT_SIZE = 20;
  private final int MIN_TEXT_SIZE = 10;
  private final int MAX_TEXT_SIZE = 32;
  // Aurora xuyong 2013-12-30 deleted for aurora's ne feature start
  //private ScaleDetector mScaleDetector;
  // Aurora xuyong 2013-12-30 deleted for aurora's ne feature end
  private float mTextSize = DEFAULT_TEXT_SIZE;
  private float MIN_ADJUST_TEXT_SIZE = 0.2f;
  private boolean mIsCmcc = false;    
  
  // add for cmcc changTextSize by multiTouch
  private void changeTextSize(float size){
       if(mList != null && mList.getVisibility() == View.VISIBLE){
          int count = mList.getChildCount();
          for(int i = 0; i < count; i++){
              SlidesBrowserItemView item =  (SlidesBrowserItemView)mList.getChildAt(i);
              if(item != null){
                  item.setMmsTextSize(size);
              }
          }
      }
  }    
  // Aurora xuyong 2013-12-30 deleted for aurora's ne feature start
  /*public class ScaleListener implements OnScaleListener{
      
      public boolean onScaleStart(ScaleDetector detector) {
          Log.i(TAG, "onScaleStart -> mTextSize = " + mTextSize);
          return true;
      }
      
      public void onScaleEnd(ScaleDetector detector) {
          Log.i(TAG, "onScaleEnd -> mTextSize = " + mTextSize);
          
          //save current value to preference
          MessageUtils.setTextSize(SlidesBrowserActivity.this, mTextSize);
      }
      
      public boolean onScale(ScaleDetector detector) {

          float size = mTextSize * detector.getScaleFactor();
          
          if(Math.abs(size - mTextSize) < MIN_ADJUST_TEXT_SIZE){
              return false;
          }            
          if(size < MIN_TEXT_SIZE){
              size = MIN_TEXT_SIZE;
          }            
          if(size > MAX_TEXT_SIZE){
              size = MAX_TEXT_SIZE;
          }            
          if(size != mTextSize){
              changeTextSize(size);
              mTextSize = size;
          }
          return true;
      }
  };*/
  // Aurora xuyong 2013-12-30 deleted for aurora's ne feature end
  //gionee wangym 2012-11-22 add for CR00735223 end
  // Aurora xuyong 2014-05-07 added for bug 4693 start
  @Override
  protected void onDestroy() {
       mThumbnailWorker.clearTaskSet();
      // Aurora xuyong 2014-09-10 added for uptimize start
       if (mSimInfoObserver != null) {
           this.getContentResolver().unregisterContentObserver(mSimInfoObserver);
       }
      // Aurora xuyong 2014-09-10 added for uptimize end
       super.onDestroy();
  }
  // Aurora xuyong 2014-05-07 added for bug 4693 end
  // Aurora xuyong 2016-03-21 added for bug #21602 start
    private void createLinkClickAction() {
        AuroraAlertDialog dialog = null;
        String[] items = null;
        switch (getFlag(mClickContent.getValue())) {
            case URI_PHONE:
                final String number = mClickContent.getValue().substring(4);
                mCurrentContact = Contact.get(number, true);
                if (MmsApp.mGnMultiSimMessage && mInsertedSimCount == 2) {
                    if (mCurrentContact.existsInDatabase()) {
                        int slotIdEx = getLastCallSlotId(this, number);
                        switch(slotIdEx) {
                            case -1:
                                items = new String[5];
                                items[0] = this.getString(R.string.aurora_dail_sim1);
                                items[1] = this.getString(R.string.aurora_dail_sim2);
                                items[2] = this.getString(R.string.aurora_send_msg);
                                items[3] = this.getString(R.string.aurora_copy);
                                items[4] = this.getString(R.string.aurora_view_contact);
                                dialog = new AuroraAlertDialog.Builder(this)
                                        .setItems(items,
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog,
                                                                        int which) {
                                                        switch(which) {
                                                            case 0:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_phone_dail_sim1);
                                                                break;
                                                            case 1:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_phone_dail_sim2);
                                                                break;
                                                            case 2:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_phone_forward);
                                                                break;
                                                            case 3:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_num_copy);
                                                                break;
                                                            case 4:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_current_view);
                                                                break;
                                                        }
                                                    }
                                                }).create();
                                break;
                            case 0:
                                items = new String[5];
                                items[0] = this.getString(R.string.aurora_dail_sim1f);
                                items[1] = this.getString(R.string.aurora_dail_sim2);
                                items[2] = this.getString(R.string.aurora_send_msg);
                                items[3] = this.getString(R.string.aurora_copy);
                                items[4] = this.getString(R.string.aurora_view_contact);
                                dialog = new AuroraAlertDialog.Builder(this)
                                        .setItems(items,
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog,
                                                                        int which) {
                                                        switch(which) {
                                                            case 0:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_phone_dail_sim1);
                                                                break;
                                                            case 1:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_phone_dail_sim2);
                                                                break;
                                                            case 2:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_phone_forward);
                                                                break;
                                                            case 3:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_num_copy);
                                                                break;
                                                            case 4:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_current_view);
                                                                break;
                                                        }
                                                    }
                                                }).create();
                                break;
                            case 1:
                                items = new String[5];
                                items[0] = this.getString(R.string.aurora_dail_sim2f);
                                items[1] = this.getString(R.string.aurora_dail_sim1);
                                items[2] = this.getString(R.string.aurora_send_msg);
                                items[3] = this.getString(R.string.aurora_copy);
                                items[4] = this.getString(R.string.aurora_view_contact);
                                dialog = new AuroraAlertDialog.Builder(this)
                                        .setItems(items,
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog,
                                                                        int which) {
                                                        switch(which) {
                                                            case 0:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_phone_dail_sim2);
                                                                break;
                                                            case 1:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_phone_dail_sim1);
                                                                break;
                                                            case 2:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_phone_forward);
                                                                break;
                                                            case 3:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_num_copy);
                                                                break;
                                                            case 4:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_current_view);
                                                                break;
                                                        }
                                                    }
                                                }).create();
                                break;
                        }
                    } else if (!mCurrentContact.existsInDatabase()) {
                        int slotIdNew = getLastCallSlotId(this, number);
                        switch(slotIdNew) {
                            case -1:
                                items = new String[5];
                                items[0] = this.getString(R.string.aurora_dail_sim1);
                                items[1] = this.getString(R.string.aurora_dail_sim2);
                                items[2] = this.getString(R.string.aurora_send_msg);
                                items[3] = this.getString(R.string.aurora_copy);
                                items[4] = this.getString(R.string.aurora_created_contact);
                                dialog = new AuroraAlertDialog.Builder(this)
                                        .setItems(items,
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog,
                                                                        int which) {
                                                        switch(which) {
                                                            case 0:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_phone_dail_sim1);
                                                                break;
                                                            case 1:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_phone_dail_sim2);
                                                                break;
                                                            case 2:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_phone_forward);
                                                                break;
                                                            case 3:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_num_copy);
                                                                break;
                                                            case 4:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_phone_created);
                                                                break;
                                                        }
                                                    }
                                                }).create();
                                break;
                            case 0:
                                items = new String[5];
                                items[0] = this.getString(R.string.aurora_dail_sim1f);
                                items[1] = this.getString(R.string.aurora_dail_sim2);
                                items[2] = this.getString(R.string.aurora_send_msg);
                                items[3] = this.getString(R.string.aurora_copy);
                                items[4] = this.getString(R.string.aurora_created_contact);
                                dialog = new AuroraAlertDialog.Builder(this)
                                        .setItems(items,
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog,
                                                                        int which) {
                                                        switch(which) {
                                                            case 0:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_phone_dail_sim1);
                                                                break;
                                                            case 1:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_phone_dail_sim2);
                                                                break;
                                                            case 2:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_phone_forward);
                                                                break;
                                                            case 3:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_num_copy);
                                                                break;
                                                            case 4:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_phone_created);
                                                                break;
                                                        }
                                                    }
                                                }).create();
                                break;
                            case 1:
                                items = new String[5];
                                items[0] = this.getString(R.string.aurora_dail_sim2f);
                                items[1] = this.getString(R.string.aurora_dail_sim1);
                                items[2] = this.getString(R.string.aurora_send_msg);
                                items[3] = this.getString(R.string.aurora_copy);
                                items[4] = this.getString(R.string.aurora_created_contact);
                                dialog = new AuroraAlertDialog.Builder(this)
                                        .setItems(items,
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog,
                                                                        int which) {
                                                        switch(which) {
                                                            case 0:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_phone_dail_sim2);
                                                                break;
                                                            case 1:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_phone_dail_sim1);
                                                                break;
                                                            case 2:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_phone_forward);
                                                                break;
                                                            case 3:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_num_copy);
                                                                break;
                                                            case 4:
                                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_phone_created);
                                                                break;
                                                        }
                                                    }
                                                }).create();
                                break;
                        }
                    }
                } else {
                    if (mCurrentContact.existsInDatabase()) {
                        items = new String[4];
                        items[0] = this.getString(R.string.aurora_dail);
                        items[1] = this.getString(R.string.aurora_send_msg);
                        items[2] = this.getString(R.string.aurora_copy);
                        items[3] = this.getString(R.string.aurora_view_contact);
                        dialog = new AuroraAlertDialog.Builder(this)
                                .setItems(items,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,
                                                                int which) {
                                                switch(which) {
                                                    case 0:
                                                        auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_phone_dail);
                                                        break;
                                                    case 1:
                                                        auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_phone_forward);
                                                        break;
                                                    case 2:
                                                        auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_num_copy);
                                                        break;
                                                    case 3:
                                                        auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_current_view);
                                                        break;
                                                }
                                            }
                                        }).create();
                    } else {
                        items = new String[4];
                        items[0] = this.getString(R.string.aurora_dail);
                        items[1] = this.getString(R.string.aurora_send_msg);
                        items[2] = this.getString(R.string.aurora_copy);
                        items[3] = this.getString(R.string.aurora_created_contact);
                        dialog = new AuroraAlertDialog.Builder(this)
                                .setItems(items,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,
                                                                int which) {
                                                switch(which) {
                                                    case 0:
                                                        auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_phone_dail);
                                                        break;
                                                    case 1:
                                                        auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_phone_forward);
                                                        break;
                                                    case 2:
                                                        auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_num_copy);
                                                        break;
                                                    case 3:
                                                        auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_phone_created);
                                                        break;
                                                }
                                            }
                                        }).create();
                    }
                }
                break;
            case URI_HTTP:
                items = new String[2];
                items[0] = this.getString(R.string.aurora_msg_web_visit);
                items[1] = this.getString(R.string.aurora_copy);
                dialog = new AuroraAlertDialog.Builder(this)
                        .setItems(items,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        switch(which) {
                                            case 0:
                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_http_visit);
                                                break;
                                            case 1:
                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_http_copy);
                                                break;
                                        }
                                    }
                                }).create();
                break;
            case URI_MAIL:
                items = new String[3];
                items[0] = this.getString(R.string.aurora_send_mail);
                items[1] = this.getString(R.string.aurora_copy);
                items[2] = this.getString(R.string.aurora_created_contact);
                dialog = new AuroraAlertDialog.Builder(this)
                        .setItems(items,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        switch(which) {
                                            case 0:
                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_mail_send);
                                                break;
                                            case 1:
                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_mail_copy);
                                                break;
                                            case 2:
                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_mail_created);
                                                break;
                                        }
                                    }
                                }).create();
                break;
            case URI_RTSP:
                items = new String[2];
                items[0] = this.getString(R.string.aurora_msg_web_visit);
                items[1] = this.getString(R.string.aurora_copy);
                dialog = new AuroraAlertDialog.Builder(this)
                        .setItems(items,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        switch(which) {
                                            case 0:
                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_rtsp_visit);
                                                break;
                                            case 1:
                                                auroraMenuCallBack.auroraMenuItemClick(R.id.aurora_msg_rtsp_copy);
                                                break;
                                        }
                                    }
                                }).create();
                break;
            default:
                break;
        }
        if (dialog != null) {
            dialog.show();
        }
    }
 // Aurora xuyong 2016-03-21 added for bug #21602 end
}