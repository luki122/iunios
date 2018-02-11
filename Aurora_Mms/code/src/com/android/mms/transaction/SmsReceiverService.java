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

package com.android.mms.transaction;

import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static android.provider.Telephony.Sms.Intents.SMS_RECEIVED_ACTION;

import java.util.Calendar;
import java.util.GregorianCalendar;
// Aurora xuyong 2015-01-30 added for aurora's new feature start
import java.util.List;
import java.util.Stack;
// Aurora xuyong 2015-01-30 added for aurora's new feature end
import java.util.TimeZone;
// Aurora xuyong 2015-01-30 added for aurora's new feature start
import java.util.Timer;
import java.util.TimerTask;
// Aurora xuyong 2015-01-30 added for aurora's new feature end

import com.android.mms.data.Contact;
import com.android.mms.ui.ClassZeroActivity;
// Aurora xuyong 2015-01-30 added for aurora's new feature start
import com.android.mms.ui.ComposeMessageActivity;
// Aurora xuyong 2015-01-30 added for aurora's new feature end
import com.android.mms.ui.MessagingPreferenceActivity;
import com.android.mms.util.Recycler;
import com.android.mms.util.SendingProgressTokenManager;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.MmsException;
//Aurora xuyong 2013-11-15 modified for google adapt end
import android.database.sqlite.SqliteWrapper;

import aurora.app.AuroraActivity;
// Aurora xuyong 2015-01-30 added for aurora's new feature start
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
// Aurora xuyong 2015-01-30 added for aurora's new feature end
import android.app.Service;
// Aurora xuyong 2015-01-30 added for aurora's new feature start
import android.content.ClipboardManager;
// Aurora xuyong 2015-01-30 added for aurora's new feature end
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
// Aurora xuyong 2015-01-30 added for aurora's new feature start
import android.content.res.Configuration;
// Aurora xuyong 2015-01-30 added for aurora's new feature end
import android.database.Cursor;
import android.database.sqlite.SQLiteDiskIOException;
// Aurora xuyong 2015-01-30 added for aurora's new feature start
import android.graphics.Color;
import android.graphics.PixelFormat;
// Aurora xuyong 2015-01-30 added for aurora's new feature end
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import aurora.preference.AuroraPreferenceManager;
import gionee.provider.GnTelephony.Sms;
import android.provider.Settings;
import android.provider.Telephony.Threads;
import gionee.provider.GnTelephony.SIMInfo;
import android.provider.Telephony.Sms.Inbox;
import android.provider.Telephony.Sms.Intents;
import android.provider.Telephony.Sms.Outbox;
import gionee.telephony.gemini.GnGeminiSmsManager;
import android.telephony.ServiceState;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import gionee.telephony.GnSmsMessage;
import gionee.telephony.GnSmsManager;
// Aurora xuyong 2015-01-30 added for aurora's new feature start
import android.text.Html;
import android.text.Spannable;
// Aurora xuyong 2015-01-30 added for aurora's new feature end
import android.text.TextUtils;
// Aurora xuyong 2015-01-30 added for aurora's new feature start
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
// Aurora xuyong 2015-01-30 added for aurora's new feature end
import android.util.Log;
// Aurora xuyong 2015-01-30 added for aurora's new feature start
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
// Aurora xuyong 2015-01-31 added for aurora's new feature start
import android.view.ViewGroup;
// Aurora xuyong 2015-01-31 added for aurora's new feature end
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
// Aurora xuyong 2015-01-31 added for aurora's new feature start
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
// Aurora xuyong 2015-01-31 added for aurora's new feature end
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
// Aurora xuyong 2015-01-30 added for aurora's new feature end
import android.widget.Toast;
// Aurora xuyong 2014-07-02 added for reject feature start
import java.util.ArrayList;
// Aurora xuyong 2015-01-30 added for aurora's new feature start
import java.util.regex.Matcher;
import java.util.regex.Pattern;
// Aurora xuyong 2015-01-30 added for aurora's new feature end
import com.aurora.mms.util.Utils;
import com.aurora.weather.util.AuroraMsgWeatherUtils;
// Aurora xuyong 2014-07-02 added for reject feature end
import com.android.internal.telephony.TelephonyIntents;
import com.android.mms.R;
import com.android.mms.LogTag;
import com.android.mms.util.ThreadCountManager;
import com.aurora.featureoption.FeatureOption;
import gionee.provider.GnTelephony.Mms;
import com.gionee.internal.telephony.GnPhone;
import com.android.mms.MmsApp;
import android.os.SystemProperties;
//gionee gaoj 2012-3-22 added for CR00555790 start
import android.os.Bundle;
import com.gionee.mms.popup.PopUpUtils;
import com.gionee.mms.regularlysend.RegularlyMainActivity;
// Aurora xuyong 2015-01-30 added for aurora's new feature start
import com.privacymanage.service.AuroraPrivacyUtils;
// Aurora xuyong 2015-01-30 added for aurora's new feature end
import com.android.mms.data.Conversation;
//gionee gaoj 2012-3-22 added for CR00555790 end

import com.android.mms.widget.MmsWidgetProvider;
import com.aurora.weather.data.WeatherInfo;
// Aurora xuyong 2015-04-23 added for aurora's new feature start
import com.aurora.weather.data.WeatherResult;
// Aurora xuyong 2015-04-23 added for aurora's new feature end

/**
 * This service essentially plays the role of a "worker thread", allowing us to store
 * incoming messages to the database, update notifications, etc. without blocking the
 * main thread that SmsReceiver runs on.
 */
public class SmsReceiverService extends Service {
    private static final String TAG = "SmsReceiverService";
    
    // Aurora yudingmin 2014-12-09 added for thread's app result start
    public static final String MESSAGE_STATUS_CHANGED = "com.aurora.mms.transaction.MESSAGE_STATUS_CHANGED";
    // Aurora yudingmin 2014-12-09 added for thread's app result end

    private ServiceHandler mServiceHandler;
    private Looper mServiceLooper;
    private boolean mSending;

    public static final String MESSAGE_SENT_ACTION =
        "com.android.mms.transaction.MESSAGE_SENT";
    public static boolean sSmsSent = true;

    // Indicates next message can be picked up and sent out.
    public static final String EXTRA_MESSAGE_SENT_SEND_NEXT ="SendNextMsg";

    // Indicates this is a concatenation sms
    public static final String EXTRA_MESSAGE_CONCATENATION = "ConcatenationMsg";

    public static final String ACTION_SEND_MESSAGE =
        "com.android.mms.transaction.SEND_MESSAGE";

    // This must match the column IDs below.
    private static final String[] SEND_PROJECTION = new String[] {
        Sms._ID,        //0
        Sms.THREAD_ID,  //1
        Sms.ADDRESS,    //2
        Sms.BODY,       //3
        Sms.STATUS,     //4

    };
    private static final  Uri UPDATE_THREADS_URI = Uri.parse("content://mms-sms/conversations/status");
    // Aurora xuyong 2015-01-30 added for aurora's new feature start
    private static final int CHANGE_VIEW = 4;
    private static final int SHOW_IPOPS = 3;
    // Aurora xuyong 2015-01-30 added for aurora's new feature end
    private static final int FDN_CHECK_FAIL = 2;
    private static final int RADIO_NOT_AVILABLE = 1;
    public Handler mToastHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // Aurora xuyong 2015-01-30 added for aurora's new feature start
                case CHANGE_VIEW:
                    // Aurora xuyong 2015-03-17 modified for aurora's new feature start
                    if (mDoneButton != null) {
                        mDoneButton.setText(SmsReceiverService.this.getApplicationContext().getString(R.string.aurora_ipop_suffix, msg.arg1));
                    }
                    // Aurora xuyong 2015-03-17 modified for aurora's new feature end
                    break;
                // Aurora xuyong 2015-01-30 added for aurora's new feature end
                case RADIO_NOT_AVILABLE:
                    Toast.makeText(SmsReceiverService.this,
                        getString(R.string.message_queued), Toast.LENGTH_SHORT)
                       .show();
                    break;
                case FDN_CHECK_FAIL:
                    Toast.makeText(SmsReceiverService.this, R.string.fdn_enabled,
                         Toast.LENGTH_LONG).show();
                    break;
                // Aurora xuyong 2015-01-30 added for aurora's new feature start
                case SHOW_IPOPS:
                    mIdentifyNum = Utils.getUsefulCode(SmsReceiverService.this.getApplicationContext(), mInstance.getBody());
                    // Aurora xuyong 2015-03-17 modified for aurora's new feature start
                    if (!smsIsTopTask()) {
                    // Aurora xuyong 2015-03-17 modified for aurora's new feature end
                        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
                        if (!keyguardManager.inKeyguardRestrictedInputMode()
                                && getApplication().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                            if (SMS_RECEIVED_ACTION.equals(mIntent.getAction()) || 
                                    "android.provider.Telephony.SMS_DELIVER".equals(mIntent.getAction())) {
                                    long msgPrivacy = Utils.getFristPrivacyId(getApplicationContext(), mInstance.getNumber());
                                    long currentPriId = AuroraPrivacyUtils.getCurrentAccountId();
                                    if (msgPrivacy == 0) {
                                        mIsPriOrRejMsg += 0;
                                    } else {
                                        if (currentPriId > 0) {
                                            mIsPriOrRejMsg += msgPrivacy == currentPriId ? 0 : 1;
                                        } else {
                                            mIsPriOrRejMsg += 1;
                                        }
                                    }
                                    if ((MmsApp.sHasPrivacyFeature || MmsApp.sHasRejectFeature) && mIsPriOrRejMsg > 0) {
                                        return;
                                    }
                                    // Aurora xuyong 2014-03-17 added for aurora's new feature start
                                    if (mIdentifyNum == null && (mInstance == null || mInstance.getBody() == null 
                                            || (!Utils.isNotificationMsg(SmsReceiverService.this, mInstance.getNumber(), mInstance.getBody()) && mInstance.contactNotExistsInDatabase()))) {
                                        return;
                                    }
                                    // Aurora xuyong 2014-03-17 added for aurora's new feature end
                                    showIdentifyPopWindow();
                            }
                        }
                    }
                    break;
                // Aurora xuyong 2015-01-30 added for aurora's new feature end
                default :
                    break;
            }
        }
    };

    // This must match SEND_PROJECTION.
    private static final int SEND_COLUMN_ID         = 0;
    private static final int SEND_COLUMN_THREAD_ID  = 1;
    private static final int SEND_COLUMN_ADDRESS    = 2;
    private static final int SEND_COLUMN_BODY       = 3;
    private static final int SEND_COLUMN_STATUS     = 4;

    private int mResultCode;

    //Gionee zengxuanhui 20120809 add for CR00672106 begin
    private static final boolean gnGeminiRingtoneSupport = SystemProperties.get("ro.gn.gemini.ringtone.support").equals("yes");
    //Gionee zengxuanhui 20120809 add for CR00672106 end
    
    private final String SIM_CARD = "Sim";
    private final String UIM_CARD = "Uim";

    @Override
    public void onCreate() {
        // Temporarily removed for this duplicate message track down.
//        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE) || LogTag.DEBUG_SEND) {
//            Log.v(TAG, "onCreate");
//        }

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
        HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Temporarily removed for this duplicate message track down.
        // Aurora xuyong 2014-05-30 modified for bug #5101 start
        if (MESSAGE_SENT_ACTION.equals(intent.getAction()) || 
                    "android.provider.Telephony.SMS_DELIVER".equals(intent.getAction())) {
        // Aurora xuyong 2014-05-30 modified for bug #5101 end
            mResultCode = intent != null ? intent.getIntExtra("result", 0) : 0;
            Log.d(MmsApp.TXN_TAG, "Message Sent Result Code = " + mResultCode);
        }

        if (mResultCode != 0) {
            Log.v(TAG, "onStart: #" + startId + " mResultCode: " + mResultCode +
                    " = " + translateResultCode(mResultCode));
        }
        // Aurora xuyong 2015-01-30 added for aurora's new feature start
        mIntent = intent;
        // Aurora xuyong 2015-01-30 added for aurora's new feature end
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
        return Service.START_NOT_STICKY;
    }
    // Aurora xuyong 2015-01-30 added for aurora's new feature start
    WindowManager.LayoutParams wmParams;
    WindowManager mWindowManager;
    // Aurora xuyong 2015-03-17 modified for aurora's new feature start
    static RelativeLayout mRelativeLayout;
    // Aurora xuyong 2015-03-17 modified for aurora's new feature end
    TextView mIdentifyContentView;
    // Aurora xuyong 2015-03-17 modified for aurora's new feature start
    Button mDoneButton;
    // Aurora xuyong 2015-03-17 modified for aurora's new feature end
    Intent mIntent;
    // Aurora xuyong 2015-02-04 modified for bug #11555 start
    long mThreadId = -1;
    // Aurora xuyong 2015-02-04 modified for bug #11555 end
    NameAndBody mInstance = new NameAndBody();
    int mIsPriOrRejMsg = 0;
    String mIdentifyNum;
    
    public class NameAndBody {
        
        private String mName = null;
        private String mNumber = null;
        private String mBody = null;
        
        public NameAndBody() {
            mName = null;
            mBody = null;
        }
        
        public NameAndBody(String name, String body) {
            mName = name;
            mBody = body;
        }
        
        public String getName() {
            return mName;
        }
        
        public String getBody() {
            return mBody;
        }
        // Aurora xuyong 2014-03-17 added for aurora's new feature start
        public boolean contactNotExistsInDatabase() {
            return !Contact.get(mNumber, true).existsInDatabase();
        }
        // Aurora xuyong 2014-03-17 added for aurora's new feature end
        public String getNumber() {
            return mNumber;
        }
        
        public void setName(String name) {
            mName = name;
        }
        
        public void setBody(String body) {
            mBody = body;
        }
        
        public void setNumber(String number) {
            mNumber = number;
        }
    }
    
    private long getThreadId(Uri uri) {
        // Aurora xuyong 2015-02-04 added for bug #11555 start
        if (uri == null) {
            return -1;
        }
        // Aurora xuyong 2015-02-04 added for bug #11555 end
        Cursor result = null;
        try {
            result = getApplication().getContentResolver().query(uri, new String[] { "thread_id" }, null, null, null);
            if (result.moveToFirst()) {
                mThreadId = result.getLong(0);
            }
        } finally {
            if (result != null && !result.isClosed()) {
                result.close();
            }
        }
        return -1l;
    }
    
    private void rebuildShowContent() {
        LayoutInflater inflater = LayoutInflater.from(getApplication());
        // init the whole relativeLayout
        // Aurora xuyong 2015-03-17 added for aurora's new feature start
        if (mRelativeLayout != null) {
            mWindowManager.removeView(mRelativeLayout);
        }
        // Aurora xuyong 2015-03-17 added for aurora's new feature end
        mRelativeLayout = (RelativeLayout) inflater.inflate(R.layout.aurora_identify_pop_layout, null);
        mRelativeLayout.setBackgroundColor(Color.argb(218, 0, 0, 0));
        mRelativeLayout.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mRelativeLayout != null) {
                    if (mWindowManager != null) {
                        mWindowManager.removeView(mRelativeLayout);
                    }
                    mRelativeLayout = null;
                }
                // Aurora xuyong 2015-02-04 modified for bug #11555 start
                if (mThreadId > 0) {
                    Intent clickIntent = ComposeMessageActivity.createIntent(SmsReceiverService.this.getApplication().getApplicationContext(), mThreadId);
                    clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    SmsReceiverService.this.getApplicationContext().startActivity(clickIntent);
                }
                // Aurora xuyong 2015-02-04 modified for bug #11555 end
            }
            
        });
        // when we up-scroll this relativeLayout, we should remove this layout.
        mRelativeLayout.setOnTouchListener(new View.OnTouchListener() {
            
            float curY = -1;
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        curY = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        boolean isUpScroll = event.getY() < curY;
                        if (isUpScroll) {
                            if (mRelativeLayout != null) {
                                if (mWindowManager != null) {
                                    mWindowManager.removeView(mRelativeLayout);
                                }
                                mRelativeLayout = null;
                            }
                        }
                        break;
                }
                return false;
            }
            
        });
        // init textview
        mIdentifyContentView = (TextView)mRelativeLayout.findViewById(R.id.aurora_pop_content);
        // Aurora xuyong 2015-03-17 modified for aurora's new feature start
        String content = null;
        if (mIdentifyNum != null) {
            content = mInstance.getName() + this.getApplicationContext().getString(R.string.aurora_ipop_colon) + "\n"+ mIdentifyNum;
        } else {
            content = mInstance.getName() + this.getApplicationContext().getString(R.string.aurora_ipop_colon) + "\n"+ mInstance.getBody();
        }
        // Aurora xuyong 2015-03-17 modified for aurora's new feature end
        Spannable.Factory sf = Spannable.Factory.getInstance();
        Spannable sp = sf.newSpannable(content);
        // Aurora xuyong 2015-02-06 modified for forher feature start
        // Aurora xuyong 2015-03-17 modified for aurora's new feature start
        CharacterStyle colorStyle = null;
        if (mIdentifyNum != null) {
            colorStyle = new ForegroundColorSpan(this.getApplicationContext().getResources().getColor(R.color.aurora_ipop_tc));
        } else {
            colorStyle = new ForegroundColorSpan(Color.argb(255, 255, 255, 255));
        }
        // Aurora xuyong 2015-03-17 modified for aurora's new feature end
        // Aurora xuyong 2015-02-06 modified for forher feature end
        CharacterStyle underlineStyle = new UnderlineSpan();
        // Aurora xuyong 2015-03-17 modified for aurora's new feature start
        int start = -1;
        int end = -1;
        if (mIdentifyNum != null) {
            start = content.indexOf(mIdentifyNum);
            end = start + mIdentifyNum.length();
        } else {
            start = content.indexOf(mInstance.getBody());
            end = start + mInstance.getBody().length();
        }
        // Aurora xuyong 2015-03-17 modified for aurora's new feature end
        // change macth color
        sp.setSpan(colorStyle, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // reset underline
        // Aurora xuyong 2015-03-17 modified for aurora's new feature start
        if (mIdentifyNum != null) {
            sp.setSpan(underlineStyle, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        // Aurora xuyong 2015-03-17 modified for aurora's new feature end
        mIdentifyContentView.setText(sp);
        // init button
        // Aurora xuyong 2015-03-17 modified for aurora's new feature start
        mDoneButton = (Button)mRelativeLayout.findViewById(R.id.aurora_pop_done);
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                    ClipboardManager cmb = (ClipboardManager)SmsReceiverService.this.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    cmb.setText(mIdentifyNum);
                    mDoneButton.setText(SmsReceiverService.this.getApplicationContext().getString(R.string.aurora_ipop_copy_done));
                    // Aurora xuyong 2015-01-31 added for aurora's new feature start
                    int width = (int)SmsReceiverService.this.getApplicationContext().getResources().getDimension(R.dimen.aurora_ipop_bt_width);
                    ViewGroup.LayoutParams wl = mDoneButton.getLayoutParams();
                    wl.width = width;
                    mDoneButton.setLayoutParams(wl);
                    // Aurora xuyong 2015-01-31 added for aurora's new feature end
                    mDoneButton.setEnabled(false);
            }
        });
        // Aurora xuyong 2015-03-17 modified for aurora's new feature end
        // Aurora xuyong 2015-03-17 added for aurora's new feature start
        if (mIdentifyNum == null) {
            mDoneButton.setVisibility(View.GONE);
        }
        // Aurora xuyong 2015-03-17 added for aurora's new feature end
    }
    
    private void showIdentifyPopWindow() {
        wmParams = new WindowManager.LayoutParams();
        mWindowManager = (WindowManager)getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        
        wmParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
        wmParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                | LayoutParams.FLAG_NOT_FOCUSABLE
                | LayoutParams.FLAG_FULLSCREEN
                | LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        wmParams.windowAnimations = R.style.AuroraIdentifyPopAnim;
        wmParams.format = PixelFormat.TRANSLUCENT;
        
        wmParams.gravity = Gravity.TOP;
        
        wmParams.x = 0;
        wmParams.y = 0;
        
        wmParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        
        rebuildShowContent();
        
        mWindowManager.addView(mRelativeLayout, wmParams);
        
        // If the user did nothing during 5 seconds after this added view appear,
        // then we should remove it.
        final Timer disappearTimer = new Timer();  
        TimerTask disappearTask = new TimerTask(){  
      
            public void run() {
                if (mRelativeLayout != null) {
                    if (mWindowManager != null) {
                        mWindowManager.removeView(mRelativeLayout);
                    }
                    mRelativeLayout = null;
                }
                disappearTimer.cancel();
            }
              
        }; 
        disappearTimer.schedule(disappearTask, 5000);
    }
    // Aurora xuyong 2015-01-30 added for aurora's new feature end
    private static String translateResultCode(int resultCode) {
        switch (resultCode) {
            case AuroraActivity.RESULT_OK:
                return "AuroraActivity.RESULT_OK";
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                return "SmsManager.RESULT_ERROR_GENERIC_FAILURE";
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                return "SmsManager.RESULT_ERROR_RADIO_OFF";
            case SmsManager.RESULT_ERROR_NULL_PDU:
                return "SmsManager.RESULT_ERROR_NULL_PDU";
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                return "SmsManager.RESULT_ERROR_NO_SERVICE";
            case SmsManager.RESULT_ERROR_LIMIT_EXCEEDED:
                return "SmsManager.RESULT_ERROR_LIMIT_EXCEEDED";
            case SmsManager.RESULT_ERROR_FDN_CHECK_FAILURE:
                return "SmsManager.RESULT_ERROR_FDN_CHECK_FAILURE";
            default:
                return "Unknown error code";
        }
    }

    @Override
    public void onDestroy() {
        // Temporarily removed for this duplicate message track down.
//        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE) || LogTag.DEBUG_SEND) {
//            Log.v(TAG, "onDestroy");
//        }
        mServiceLooper.quit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        /**
         * Handle incoming transaction requests.
         * The incoming requests are initiated by the MMSC Server or by the MMS Client itself.
         */
        @Override
        public void handleMessage(Message msg) {
            Log.d(MmsApp.TXN_TAG, "Sms handleMessage :" + msg);
            int serviceId = msg.arg1;
            Intent intent = (Intent)msg.obj;
            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                Log.v(TAG, "handleMessage serviceId: " + serviceId + " intent: " + intent);
            }
            if (intent != null) {
                String action = intent.getAction();

                int error = intent.getIntExtra("errorCode", 0);

                if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                    Log.v(TAG, "handleMessage action: " + action + " error: " + error);
                }

                if (MESSAGE_SENT_ACTION.equals(intent.getAction())) {
                    handleSmsSent(intent, error);
                // Aurora xuyong 2014-09-09 modified for 4.4 feature start
                } else if ( "android.provider.Telephony.SMS_DELIVER".equals(action) || SMS_RECEIVED_ACTION.equals(action)) {
                // Aurora xuyong 2014-09-09 modified for 4.4 feature end
                    handleSmsReceived(intent, error);
                } else if (ACTION_BOOT_COMPLETED.equals(action)) {
                    handleBootCompleted();
                } else if (TelephonyIntents.ACTION_SERVICE_STATE_CHANGED.equals(action)) {
                    handleServiceStateChanged(intent);
                } else if (ACTION_SEND_MESSAGE.endsWith(action)) {
                    handleSendMessage(intent);
                }
            }
            
            sSmsSent = true;
            // NOTE: We MUST not call stopSelf() directly, since we need to
            // make sure the wake lock acquired by AlertReceiver is released.
            SmsReceiver.finishStartingService(SmsReceiverService.this, serviceId);
        }
    }

    private void handleServiceStateChanged(Intent intent) {
        Log.v(MmsApp.TXN_TAG, "Sms handleServiceStateChanged");
        // If service just returned, start sending out the queued messages
        ServiceState serviceState = ServiceState.newFromBundle(intent.getExtras());
        if (serviceState.getState() == ServiceState.STATE_IN_SERVICE) {
            if (MmsApp.mGnMultiSimMessage) {
                // convert slot id to sim id
                int slotId = intent.getIntExtra(GnPhone.GEMINI_SIM_ID_KEY, -1);
                SIMInfo si = SIMInfo.getSIMInfoBySlot(this, slotId);
                if (null == si) {
                    Log.e(MmsApp.TXN_TAG, "handleServiceStateChanged:SIMInfo is null for slot " + slotId);
                    return;
                }
                sendFirstQueuedMessageGemini((int)si.mSimId);
            } else {
                sendFirstQueuedMessage();
            }
        }
    }

    private void handleSendMessage(Intent intent) {
        // Aurora yudingmin 2014-12-09 modified for thread's app result start
        if (!mSending) {
            String thirdResponse = intent.getStringExtra(MessageSender.Third_Response);
            Log.d(MmsApp.TXN_TAG, "handleSendMessage() simId=" + intent.getIntExtra(GnPhone.GEMINI_SIM_ID_KEY, -1) + ";Third_Response="+thirdResponse);
            if (MmsApp.mGnMultiSimMessage) {
                if(TextUtils.isEmpty(thirdResponse)){
                    sendFirstQueuedMessageGemini(intent.getIntExtra(GnPhone.GEMINI_SIM_ID_KEY, -1));
                } else {
                    Log.v(TAG, "handleSendMessage 1 Third_Response from intent is " + thirdResponse);
                    sendFirstQueuedMessageGemini(intent.getIntExtra(GnPhone.GEMINI_SIM_ID_KEY, -1), thirdResponse);
                }
            } else {
                if(TextUtils.isEmpty(thirdResponse)){
                    sendFirstQueuedMessage();
                } else {
                    Log.v(TAG, "handleSendMessage 2 Third_Response from intent is " + thirdResponse);
                    sendFirstQueuedMessage(thirdResponse);
                }
            }
        }
        // Aurora yudingmin 2014-12-09 modified for thread's app result end
    }

// Aurora yudingmin 2014-12-09 modified for thread's app result start
    public void sendFirstQueuedMessage() {
        sendFirstQueuedMessage(null);
    }
// Aurora yudingmin 2014-12-09 modified for thread's app result end

// Aurora yudingmin 2014-12-09 added for thread's app result start
    public synchronized void sendFirstQueuedMessage(String thirdResponse) {
// Aurora yudingmin 2014-12-09 added for thread's app result end
        Log.d(MmsApp.TXN_TAG, "sendFirstQueuedMessage()");
        boolean success = true;
        // get all the queued messages from the database
        final Uri uri = Uri.parse("content://sms/queued");
        ContentResolver resolver = getContentResolver();
        //Gionee <guoyx> <2013-06-25> modify for CR00829568 begin
        String selectionString = null;
        if (MmsApp.mGnRegularlyMsgSend) {
            selectionString = "date <= " + System.currentTimeMillis();
        } else {
            selectionString = null;
        }
        // Aurora xuyong 2014-11-05 modified for privacy feature start
        if (MmsApp.sHasPrivacyFeature) {
            if (selectionString == null || selectionString.length() == 0) {
                selectionString = " is_privacy >= 0 ";
            } else if (!selectionString.contains("is_privacy")) {
                selectionString = "is_privacy >= 0 AND " + selectionString;
            }
        }
        // Aurora xuyong 2014-11-05 modified for privacy feature end
        Cursor c = SqliteWrapper.query(this, resolver, uri,
                        SEND_PROJECTION, selectionString, null, "date ASC");   // date ASC so we send out in
                                                                    // same order the user tried
                                                                    // to send messages.
        //Gionee <guoyx> <2013-06-25> modify for CR00829568 end
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    String msgText = c.getString(SEND_COLUMN_BODY);
                    String address = c.getString(SEND_COLUMN_ADDRESS);
                    int threadId = c.getInt(SEND_COLUMN_THREAD_ID);
                    int status = c.getInt(SEND_COLUMN_STATUS);

                    int msgId = c.getInt(SEND_COLUMN_ID);
                    Uri msgUri = ContentUris.withAppendedId(Sms.CONTENT_URI, msgId);

                    SmsMessageSender sender = new SmsSingleRecipientSender(this,
                            address, msgText, threadId, status == Sms.STATUS_PENDING,
                            msgUri);
                    
                    // Aurora yudingmin 2014-12-09 added for thread's app result start
                    sender.setThirdResponse(thirdResponse);
                    // Aurora yudingmin 2014-12-09 added for thread's app result end

                    if (LogTag.DEBUG_SEND ||
                            LogTag.VERBOSE ||
                            Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.v(TAG, "sendFirstQueuedMessage " + msgUri +
                                ", address: " + address +
                                ", threadId: " + threadId +
                                ",Third_Response: " + thirdResponse +
                                ", body: " + msgText);
                    }

                    try {
                        sender.sendMessage(SendingProgressTokenManager.NO_TOKEN);;
                        mSending = true;
                    } catch (MmsException e) {
                        Log.e(TAG, "sendFirstQueuedMessage: failed to send message " + msgUri
                                + ", caught ", e);
                        success = false;
                    }
                }
            } finally {
                if (!success) {
                    int msgId = c.getInt(SEND_COLUMN_ID);
                    Uri msgUri = ContentUris.withAppendedId(Sms.CONTENT_URI, msgId);
                    messageFailedToSend(msgUri, SmsManager.RESULT_ERROR_GENERIC_FAILURE);
                    // Aurora yudingmin 2014-12-09 modified for thread's app result start
                    sendFirstQueuedMessage(thirdResponse);
                    // Aurora yudingmin 2014-12-09 modified for thread's app result end
                }
                c.close();
            }
        }
        if (success) {
            // We successfully sent all the messages in the queue. We don't need to
            // be notified of any service changes any longer.
            unRegisterForServiceStateChanges();
        }
    }

    private void updateSizeForSentMessage(Intent intent){
        Uri uri = intent.getData();
        int messageSize = intent.getIntExtra("pdu_size", -1);
        Log.d(MmsApp.TXN_TAG, "update size for sent sms, size=" + messageSize);
        Cursor cursor = SqliteWrapper.query(this, getContentResolver(),
                                            // Aurora xuyong 2014-11-05 modified for privacy feature start
                                            uri, null, "is_privacy >= 0", null, null);
                                            // Aurora xuyong 2014-11-05 modified for privacy feature end
        if (cursor != null) {
            try {
                if ((cursor.getCount() == 1) && cursor.moveToFirst()) {
                    ContentValues sizeValue = new ContentValues();
                    sizeValue.put(Mms.MESSAGE_SIZE, messageSize);
                    SqliteWrapper.update(this, getContentResolver(),
                                        uri, sizeValue, null, null);
                }
            } finally {
                cursor.close();
            }
        }
    }
    
    private void handleSmsSent(Intent intent, int error) {
        Log.d(MmsApp.TXN_TAG, "handleSmsSent(), errorcode=" + error);
        Uri uri = intent.getData();
        mSending = false;
        boolean sendNextMsg = intent.getBooleanExtra(EXTRA_MESSAGE_SENT_SEND_NEXT, false);

        // GIONEE:wangfei 2012-09-03 add for CR00686851 begin
        if (gnGeminiRingtoneSupport) {
            MessagingNotification.setOutgoingSmsSimId(intent.getIntExtra(GnPhone.GEMINI_SIM_ID_KEY, -1));
        }
        // GIONEE:wangfei 2012-09-03 add for CR00686851 begin


        if (LogTag.DEBUG_SEND) {
            Log.v(TAG, "handleSmsSent uri: " + uri + " sendNextMsg: " + sendNextMsg +
                    " mResultCode: " + mResultCode +
                    " = " + translateResultCode(mResultCode) + " error: " + error);
        }
        
        // Aurora yudingmin 2014-12-09 added for thread's app result start
        Log.v(TAG, "before Third_Response handleSmsSent uri: " + uri.toString());
        Bundle bundle = intent.getExtras();
        if(bundle.containsKey(MessageSender.Third_Response)){
            Intent messageStatusChanged = new Intent(MESSAGE_STATUS_CHANGED);
            bundle.putString("uri", uri.toString());
            messageStatusChanged.putExtras(bundle);
            sendBroadcast(messageStatusChanged);
            Log.v(TAG, "we send the broadcast to third, MessageSender.Third_Response: " + bundle.getString(MessageSender.Third_Response));
        }
        // Aurora yudingmin 2014-12-09 added for thread's app result end

        boolean beConcatenationMsg = intent.getBooleanExtra(EXTRA_MESSAGE_CONCATENATION, false);

        // set message size
        updateSizeForSentMessage(intent);

        if (mResultCode == AuroraActivity.RESULT_OK) {
            Log.d(MmsApp.TXN_TAG, "handleSmsSent(), result is RESULT_OK");
            if (LogTag.DEBUG_SEND || Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                Log.v(TAG, "handleSmsSent move message to sent folder uri: " + uri);
            }
            if (sendNextMsg) {//this is the last part of a sms.a long sms's part is sent ordered.
                Cursor cursor = SqliteWrapper.query(this, getContentResolver(),
                                                    // Aurora xuyong 2014-11-05 modified for privacy feature start
                                                    uri, new String[] {Sms.TYPE}, "is_privacy >= 0", null, null);
                                                    // Aurora xuyong 2014-11-05 modified for privacy feature end
                if (cursor != null) {
                    try {
                        if ((cursor.getCount() == 1) && cursor.moveToFirst()) {
                            int smsType = 0;
                            smsType = cursor.getInt(0);
                            //if smsType is failed, that means at least one part of this long sms is sent failed.
                            // then this long sms is sent failed.
                            //so we shouldn't move it to other boxes.just keep it in failed box.
                            if (smsType != Sms.MESSAGE_TYPE_FAILED) {
                                //move sms from out box to sent box
                                if (!Sms.moveMessageToFolder(this, uri, Sms.MESSAGE_TYPE_SENT, error)) {
                                    Log.e(TAG, "handleSmsSent: failed to move message " + uri + " to sent folder");
                                }
                            }
                        }
                    } finally {
                        cursor.close();
                    }
                }
            }

            //if (!Sms.moveMessageToFolder(this, uri, Sms.MESSAGE_TYPE_SENT, error)) {
            //    Log.e(TAG, "handleSmsSent: failed to move message " + uri + " to sent folder");
            //}
            if (sendNextMsg) {
                if (MmsApp.mGnMultiSimMessage) {
                    // convert slot id to sim id
                    int slotId = intent.getIntExtra(GnPhone.GEMINI_SIM_ID_KEY, -1);
                    SIMInfo si = SIMInfo.getSIMInfoBySlot(this, slotId);
                    if (null == si) {
                        Log.e(MmsApp.TXN_TAG, "SmsReceiver:SIMInfo is null for slot " + slotId);
                        return;
                    }
                    sendFirstQueuedMessageGemini((int)si.mSimId);
                }else{
                    sendFirstQueuedMessage();
                }
            }

            // Update the notification for failed messages since they may be deleted.
            MessagingNotification.updateSendFailedNotification(this);
        } else if ((mResultCode == SmsManager.RESULT_ERROR_RADIO_OFF) ||
                (mResultCode == SmsManager.RESULT_ERROR_NO_SERVICE)) {
            if (mResultCode == SmsManager.RESULT_ERROR_RADIO_OFF) {
                Log.d(MmsApp.TXN_TAG, "handleSmsSent(), result is RESULT_ERROR_RADIO_OFF");
            } else {
                Log.d(MmsApp.TXN_TAG, "handleSmsSent(), result is RESULT_ERROR_NO_SERVICE");
            }
            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                Log.v(TAG, "handleSmsSent: no service, queuing message w/ uri: " + uri);
            }
            // We got an error with no service or no radio. Register for state changes so
            // when the status of the connection/radio changes, we can try to send the
            // queued up messages.
            registerForServiceStateChanges();
            // We couldn't send the message, put in the queue to retry later.
            Sms.moveMessageToFolder(this, uri, Sms.MESSAGE_TYPE_QUEUED, error);
            mToastHandler.post(new Runnable() {
                public void run() {
                    Toast.makeText(SmsReceiverService.this, getString(R.string.message_queued),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            if (mResultCode == SmsManager.RESULT_ERROR_FDN_CHECK_FAILURE) {
                mToastHandler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(SmsReceiverService.this, getString(R.string.fdn_check_failure),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
            messageFailedToSend(uri, error);
            if (sendNextMsg) {
                if (MmsApp.mGnMultiSimMessage) {
                    // convert slot id to sim id
                    int slotId = intent.getIntExtra(GnPhone.GEMINI_SIM_ID_KEY, -1);
                    SIMInfo si = SIMInfo.getSIMInfoBySlot(this, slotId);
                    if (null == si) {
                        Log.e(MmsApp.TXN_TAG, "SmsReceiver:SIMInfo is null for slot " + slotId);
                        return;
                    }
                    sendFirstQueuedMessageGemini((int)si.mSimId);
                } else {
                    sendFirstQueuedMessage();
                }
            }
        }
    }

    private void messageFailedToSend(Uri uri, int error) {
        Log.d(MmsApp.TXN_TAG, "messageFailedToSend(),uri=" + uri + "\terror=" + error);
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE) || LogTag.DEBUG_SEND) {
            Log.v(TAG, "messageFailedToSend msg failed uri: " + uri + " error: " + error);
        }
        Sms.moveMessageToFolder(this, uri, Sms.MESSAGE_TYPE_FAILED, error);
        
        // update sms status when failed. this Sms.STATUS is used for delivery report.
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(Sms.STATUS, Sms.STATUS_FAILED);
        // Aurora xuyong 2013-10-23 added for bug #135 start
        contentValues.put(Sms.READ, 1);
        // Aurora xuyong 2013-10-23 added for bug #135 end
        SqliteWrapper.update(this, this.getContentResolver(), uri, contentValues, null, null);
        
        MessagingNotification.notifySendFailed(getApplicationContext(), true);

        //gionee gaoj 2013-3-11 added for CR00782858 start
        MmsWidgetProvider.notifyDatasetChanged(getApplicationContext());
        //gionee gaoj 2013-3-11 added for CR00782858 end
    }

    private void handleSmsReceived(Intent intent, int error) {
        SmsMessage[] msgs = Intents.getMessagesFromIntent(intent);
        String format = intent.getStringExtra("format");
        //Gionee zengxuanhui 20120809 add for CR00672106/CR00682985 begin
        if (gnGeminiRingtoneSupport) {
            int slotId = intent.getIntExtra(GnPhone.GEMINI_SIM_ID_KEY, -1);
            int simId = GnPhone.GEMINI_SIM_1;
            if(1 == slotId){
                simId = GnPhone.GEMINI_SIM_2;
            }
            MessagingNotification.setIncomingSmsSimId(simId);
        }
        //Gionee zengxuanhui 20120809 add for CR00672106/CR00682985 end
        Uri messageUri = insertMessage(this, intent, error, format);
        // Aurora xuyong 2015-01-30 added for aurora's new feature start
        getThreadId(messageUri);
        // Aurora xuyong 2015-01-30 added for aurora's new feature end
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE) || LogTag.DEBUG_SEND) {
            SmsMessage sms = msgs[0];
            Log.v(TAG, "handleSmsReceived" + (sms.isReplace() ? "(replace)" : "") +
                    " messageUri: " + messageUri +
                    ", address: " + sms.getOriginatingAddress() +
                    ", body: " + sms.getMessageBody());
        }
        SmsMessage tmpsms = msgs[0];
        Log.d(MmsApp.TXN_TAG, "handleSmsReceived" + (tmpsms.isReplace() ? "(replace)" : "") 
            + " messageUri: " + messageUri 
            + ", address: " + tmpsms.getOriginatingAddress() 
            + ", body: " + tmpsms.getMessageBody());

        //gionee gaoj 2012-10-12 added for CR00711168 start
        if (MmsApp.mIsSafeModeSupport) {
            return;
        }
        //gionee gaoj 2012-10-12 added for CR00711168 end

        if (messageUri != null) {
            // Called off of the UI thread so ok to block.
            MessagingNotification.blockingUpdateNewMessageIndicator(this, true, false);
            
            //gionee gaoj 2012-3-22 added for CR00555790 start
            if (MmsApp.mGnPopupMsgSupport) {
                showPopUpView(messageUri);
            }
            //gionee gaoj 2012-3-22 added for CR00555790 end
        } else {
            SmsMessage sms = msgs[0];
            SmsMessage msg = SmsMessage.createFromPdu(sms.getPdu());
            CharSequence messageChars = msg.getMessageBody();
            String message = messageChars.toString();
            if (!TextUtils.isEmpty(message)) {
                MessagingNotification.notifyClassZeroMessage(this, msgs[0]
                        .getOriginatingAddress());
            }
        }
    }

    //gionee gaoj 2012-3-22 added for CR00555790 start
    private void showPopUpView(Uri messageUri) {
        if (PopUpUtils.mPopUpShowing && PopUpUtils.getPopNotfiSetting(this)) {
            Intent intent = new Intent(PopUpUtils.MSG_INFO_RECEIVER_ACTION);
            if (MmsApp.mEncryption) {
                if (getPopUpInfoBundle(messageUri) != null) {
                    intent.putExtras(getPopUpInfoBundle(messageUri));
                    sendBroadcast(intent);
                }
            } else {
                intent.putExtras(getPopUpInfoBundle(messageUri));
                sendBroadcast(intent);
            }

        } else if ((PopUpUtils.isLauncherView(this) || (PopUpUtils.isLockScreen(this) && !PopUpUtils.isMmsView(this))) && PopUpUtils.getPopNotfiSetting(this)) {
            Intent intents = new Intent(PopUpUtils.POPUP_ACTION);
            intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intents.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            if (MmsApp.mEncryption) {
                if (getPopUpInfoBundle(messageUri) != null) {
                    intents.putExtras(getPopUpInfoBundle(messageUri));
                    startActivity(intents);
                }
            } else {
                intents.putExtras(getPopUpInfoBundle(messageUri));
                startActivity(intents);
            }
        }
    }
    
    private boolean smsIsTopTask() {
        ActivityManager activityManager = (ActivityManager) this.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = this.getPackageName();
        List<RunningTaskInfo> appTask = activityManager.getRunningTasks(1);
        if (appTask != null && appTask.size() > 0) {
            if(appTask.get(0).topActivity.toString().contains(packageName)) {
                return true;
            }
        }
        return false;
    }

    private Bundle getPopUpInfoBundle(Uri messageUri) {
        Cursor cursor = SqliteWrapper.query(this, getContentResolver(), messageUri, null, null, null, null);
        Bundle bundle = new Bundle();
        try {
            if (null == cursor || cursor.getCount() < 1) {
                return null;
            }
            cursor.moveToFirst();
            bundle.putString(PopUpUtils.POPUP_INFO_ADDRESS, cursor.getString(cursor.getColumnIndexOrThrow(Sms.ADDRESS)));
            bundle.putLong(PopUpUtils.POPUP_INFO_DATE, cursor.getLong(cursor.getColumnIndexOrThrow(Sms.DATE)));
            bundle.putString(PopUpUtils.POPUP_INFO_BODY, cursor.getString(cursor.getColumnIndexOrThrow(Sms.BODY)));
            bundle.putInt(PopUpUtils.POPUP_INFO_SIM_ID, cursor.getInt(cursor.getColumnIndexOrThrow(Sms.SIM_ID)));
            bundle.putInt(PopUpUtils.POPUP_INFO_MSG_TYPE, PopUpUtils.POPUP_TYPE_SMS);
            bundle.putInt(PopUpUtils.POPUP_INFO_THREAD_ID, cursor.getInt(cursor.getColumnIndexOrThrow(Sms.THREAD_ID)));
            bundle.putString(PopUpUtils.POPUP_INFO_MSG_URI, messageUri.toString());

            if (MmsApp.mEncryption) {
                long threadId = cursor.getLong(cursor
                        .getColumnIndexOrThrow(Sms.THREAD_ID));
                Conversation conversation = Conversation.get(this, threadId,
                        false);
                if (conversation.getEncryption()) {
                    return null;
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
        return bundle;
    }
    //gionee gaoj 2012-3-22 added for CR00555790 end

    private void handleBootCompleted() {
        // Some messages may get stuck in the outbox. At this point, they're probably irrelevant
        // to the user, so mark them as failed and notify the user, who can then decide whether to
        // resend them manually.
        int numMoved = moveOutboxMessagesToFailedBox();
        if (numMoved > 0) {
            MessagingNotification.notifySendFailed(getApplicationContext(), true);
        }

        if (MmsApp.mGnMultiSimMessage) {
            Log.d(TAG, "handleBootCompleted not need to send the message now!");
        } else {
            // Send any queued messages that were waiting from before the reboot.
            sendFirstQueuedMessage();
        }

        // Called off of the UI thread so ok to block.
        MessagingNotification.blockingUpdateNewMessageIndicator(this, true, false);
        //gionee gaoj 2012-8-21 added for CR00678365 start
        if (MmsApp.mGnRegularlyMsgSend) {
            Cursor cursor = null;
            long date = -1;
            String[] SMS_QUERY_COLUMNS = { "date" };
            try {
                cursor = getContentResolver().query(Sms.CONTENT_URI, SMS_QUERY_COLUMNS, "date > " + System.currentTimeMillis(), null, null);
                if (cursor != null && cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        date = cursor.getLong(0);
                        RegularlyMainActivity.reSetAlarmManager(this, date);
                    }
                }
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
        }
        //gionee gaoj 2012-8-21 added for CR00678365 end
    }

    /**
     * Move all messages that are in the outbox to the failed state and set them to unread.
     * @return The number of messages that were actually moved
     */
    private int moveOutboxMessagesToFailedBox() {
        ContentValues values = new ContentValues(3);

        values.put(Sms.TYPE, Sms.MESSAGE_TYPE_FAILED);
        values.put(Sms.ERROR_CODE, SmsManager.RESULT_ERROR_GENERIC_FAILURE);
        values.put(Sms.READ, Integer.valueOf(0));
        int messageCount = 0;
        try {
            messageCount = SqliteWrapper.update(
                    getApplicationContext(), getContentResolver(), Outbox.CONTENT_URI,
                    values, "type = " + Sms.MESSAGE_TYPE_OUTBOX, null);
        } catch (SQLiteDiskIOException e) {
            // Ignore
            Log.e(MmsApp.TXN_TAG, "SQLiteDiskIOException caught while move outbox message to queue box", e);
        }
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE) || LogTag.DEBUG_SEND) {
            Log.v(TAG, "moveOutboxMessagesToFailedBox messageCount: " + messageCount);
        }
        return messageCount;
    }

    public static final String CLASS_ZERO_BODY_KEY = "CLASS_ZERO_BODY";

    // This must match the column IDs below.
    private final static String[] REPLACE_PROJECTION = new String[] {
        Sms._ID,
        Sms.ADDRESS,
        Sms.PROTOCOL
    };

    // add for gemini
    private final static String[] REPLACE_PROJECTION_GEMINI = new String[] {
        Sms._ID,
        Sms.ADDRESS,
        Sms.PROTOCOL,
        Sms.SIM_ID
    };

    // This must match REPLACE_PROJECTION.
    private static final int REPLACE_COLUMN_ID = 0;

    /**
     * If the message is a class-zero message, display it immediately
     * and return null.  Otherwise, store it using the
     * <code>ContentResolver</code> and return the
     * <code>Uri</code> of the thread containing this message
     * so that we can use it for notification.
     */
     private Uri insertMessage(Context context, Intent intent, int error, String format) {
        // Build the helper classes to parse the messages.
        SmsMessage[] msgs = Intents.getMessagesFromIntent(intent);
        SmsMessage sms = msgs[0];

        // convert slot id to sim id
        // Aurora xuyong 2013-11-13 modified for S4 adapt: can't receive SMS/MMS start
        int slotId;
        if (MmsApp.mGnMultiSimMessage) {
            slotId = intent.getIntExtra(GnPhone.GEMINI_SIM_ID_KEY, -1);
        } else {
            slotId = 0;
        }
        // Aurora xuyong 2013-11-13 modified for S4 adapt: can't receive SMS/MMS end
        
        SIMInfo si = SIMInfo.getSIMInfoBySlot(context, slotId);
        if (null == si) {
            Log.e(MmsApp.TXN_TAG, "insertMessage:SIMInfo is null for slot " + slotId);
            return null;
        }
        int simId = (int)si.mSimId;
        intent.putExtra(GnPhone.GEMINI_SIM_ID_KEY, simId);
        Log.d(MmsApp.TXN_TAG, "Sms insert message,\tslotId = " + slotId + "\tsimId = " + simId);

        if (sms.getMessageClass() == SmsMessage.MessageClass.CLASS_0) {
            SmsMessage msg = SmsMessage.createFromPdu(sms.getPdu());
            CharSequence messageChars = msg.getMessageBody();
            String message = messageChars.toString();
            
            //if (! TextUtils.isEmpty(message)) {
                displayClassZeroMessage(context, intent, format);
            //}
            return null;
        } else if (sms.isReplace()) {
            return replaceMessage(context, msgs, error);
        } else {
            return storeMessage(context, msgs, error);
        }
    }
/*    
    private Uri insertMessage(Context context, SmsMessage[] msgs, int error) {
        // Build the helper classes to parse the messages.
        Log.d(MmsApp.TXN_TAG, "Sms insertMessage");
        SmsMessage sms = msgs[0];

        if (sms.getMessageClass() == SmsMessage.MessageClass.CLASS_0) {
            SmsMessage msg = SmsMessage.createFromPdu(sms.getPdu());
            CharSequence messageChars = msg.getMessageBody();
            String message = messageChars.toString();
            
            if (! TextUtils.isEmpty(message)) {
                displayClassZeroMessage(context, sms);
            }
            return null;
        } else if (sms.isReplace()) {
            return replaceMessage(context, msgs, error);
        } else {
            return storeMessage(context, msgs, error);
        }
    }
*/
    /**
     * This method is used if this is a "replace short message" SMS.
     * We find any existing message that matches the incoming
     * message's originating address and protocol identifier.  If
     * there is one, we replace its fields with those of the new
     * message.  Otherwise, we store the new message as usual.
     *
     * See TS 23.040 9.2.3.9.
     */
    private Uri replaceMessage(Context context, SmsMessage[] msgs, int error) {
        Log.v(MmsApp.TXN_TAG, "Sms replaceMessage");
        SmsMessage sms = msgs[0];
        ContentValues values = extractContentValues(sms);
        values.put(Sms.ERROR_CODE, error);
        int pduCount = msgs.length;

        if (pduCount == 1) {
            // There is only one part, so grab the body directly.
            values.put(Inbox.BODY, replaceFormFeeds(sms.getDisplayMessageBody()));
        } else {
            // Build up the body from the parts.
            StringBuilder body = new StringBuilder();
            for (int i = 0; i < pduCount; i++) {
                sms = msgs[i];
                if (sms.mWrappedSmsMessage != null) {
                    body.append(sms.getDisplayMessageBody());
                }
            }
            values.put(Inbox.BODY, replaceFormFeeds(body.toString()));
        }

        ContentResolver resolver = context.getContentResolver();
        String originatingAddress = sms.getOriginatingAddress();
        int protocolIdentifier = sms.getProtocolIdentifier();
        String selection =
                Sms.ADDRESS + " = ? AND " +
                // Aurora xuyong 2014-11-05 modified for privacy feature start
                Sms.PROTOCOL + " = ?" + " AND is_privacy >= 0";
                // Aurora xuyong 2014-11-05 modified for privacy feature end
        String[] selectionArgs = null;

        // for gemini we should care the sim id
        if (MmsApp.mGnMultiSimMessage) {
            selection = selection + " AND " + Sms.SIM_ID + " = ?";
            GnSmsMessage gnSms = new GnSmsMessage();
            // conver slot id to sim id
            SIMInfo si = SIMInfo.getSIMInfoBySlot(context, gnSms.getMessageSimId(sms));
            if (null == si) {
                Log.e(MmsApp.TXN_TAG, "SmsReceiverService:SIMInfo is null for slot " + gnSms.getMessageSimId(sms));
                return null;
            }
            int simId = (int)si.mSimId;
            selectionArgs = new String[] {originatingAddress, 
                                          Integer.toString(protocolIdentifier), 
                                          Integer.toString(simId/*sms.getMessageSimId()*/)};
        } else {
            selectionArgs = new String[] {originatingAddress, 
                                          Integer.toString(protocolIdentifier)};
        }

        // add for gemini
        Cursor cursor = SqliteWrapper.query(context, resolver, Inbox.CONTENT_URI,
                            MmsApp.mGnMultiSimMessage ? REPLACE_PROJECTION_GEMINI : REPLACE_PROJECTION, //guoyx 20130116
                            selection, 
                            selectionArgs, null);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    long messageId = cursor.getLong(REPLACE_COLUMN_ID);
                    Uri messageUri = ContentUris.withAppendedId(
                            Sms.CONTENT_URI, messageId);

                    SqliteWrapper.update(context, resolver, messageUri,
                                        values, null, null);
                    return messageUri;
                }
            } finally {
                cursor.close();
            }
        }
        return storeMessage(context, msgs, error);
    }

    public static String replaceFormFeeds(String s) {
        // Some providers send formfeeds in their messages. Convert those formfeeds to newlines.
        return s.replace('\f', '\n');
    }

//    private static int count = 0;

    private Uri storeMessage(Context context, SmsMessage[] msgs, int error) {
        Log.v(MmsApp.TXN_TAG, "Sms storeMessage");
        SmsMessage sms = msgs[0];

        // Store the message in the content provider.
        ContentValues values = extractContentValues(sms);
        values.put(Sms.ERROR_CODE, error);
        int pduCount = msgs.length;

        if (pduCount == 1) {
            // There is only one part, so grab the body directly.
            values.put(Inbox.BODY, replaceFormFeeds(sms.getDisplayMessageBody()));
        } else {
            // Build up the body from the parts.
            StringBuilder body = new StringBuilder();
            for (int i = 0; i < pduCount; i++) {
                sms = msgs[i];
                if (sms.mWrappedSmsMessage != null) {
                    body.append(sms.getDisplayMessageBody());
                }
            }
            values.put(Inbox.BODY, replaceFormFeeds(body.toString()));
        }
        // Aurora xuyong 2014-07-02 added for reject feature start
        String messageBody = values.getAsString(Inbox.BODY);
        // Aurora xuyong 2015-01-30 added for aurora's new feature start
        mInstance.setBody(messageBody);
        // Aurora xuyong 2015-01-30 added for aurora's new feature end
        
        // check whether we need reject this message if it's a rubbish message
        // Aurora xuyong 2014-09-02 added for whitelist feature start
        boolean rejectValue = false;
        // Aurora xuyong 2014-09-02 added for whitelist feature end
        if (MmsApp.sHasRejectFeature) {
            if (!Utils.isInit()) {
                Utils.init(getApplicationContext());    
            }
           // Aurora xuyong 2014-07-07 modified for reject feature start
           // Aurora xuyong 2014-09-15 modified for sogou jar replace start
            if (Utils.needRejectRubbishMsg(getApplicationContext()) && Utils.isSpam(getApplicationContext(), sms.getDisplayOriginatingAddress(), messageBody)) {
           // Aurora xuyong 2014-09-15 modified for sogou jar replace end
           // Aurora xuyong 2014-07-07 modified for reject feature end
             // Aurora xuyong 2014-08-06 modified for bug #6895 start
                values.put("reject", 1);
             // Aurora xuyong 2014-08-23 added for bug #7909 start
             // Aurora xuyong 2014-09-02 modified for whitelist feature start
                if (Utils.isInWhiteList(context, values.getAsString(Sms.ADDRESS))) {
                    values.put("reject", 0);
                }
                rejectValue = values.getAsInteger("reject").intValue() == 1 ? true : false;
             // Aurora xuyong 2014-09-02 modified for whitelist feature end
             // Aurora xuyong 2014-08-23 added for bug #7909 end
             // Aurora xuyong 2014-08-06 modified for bug #6895 end
            }
        }
        // Aurora xuyong 2014-07-02 added for reject feature end
        // Make sure we've got a thread id so after the insert we'll be able to delete
        // excess messages.
        String address = values.getAsString(Sms.ADDRESS);

        // Code for debugging and easy injection of short codes, non email addresses, etc.
        // See Contact.isAlphaNumber() for further comments and results.
//        switch (count++ % 8) {
//            case 0: address = "AB12"; break;
//            case 1: address = "12"; break;
//            case 2: address = "Jello123"; break;
//            case 3: address = "T-Mobile"; break;
//            case 4: address = "Mobile1"; break;
//            case 5: address = "Dogs77"; break;
//            case 6: address = "****1"; break;
//            case 7: address = "#4#5#6#"; break;
//        }

        if (!TextUtils.isEmpty(address)) {
            Contact cacheContact = Contact.get(address,true);
            if (cacheContact != null) {
                address = cacheContact.getNumber();
            }
            // Aurora xuyong 2015-01-30 added for aurora's new feature start
            mInstance.setName(cacheContact.getName());
            mInstance.setNumber(cacheContact.getNumber());
            if (AuroraMsgWeatherUtils.needShowWeatherInfo(this, mInstance)) {
                    String cityName = AuroraMsgWeatherUtils.getAreaName(context, address);
                    if (cityName != null) {
                        String timeSection = AuroraMsgWeatherUtils.getTimeSection();
                        WeatherInfo weatherInfo = AuroraMsgWeatherUtils.extractUsefulInfo(AuroraMsgWeatherUtils.getWeatherInfoFromCache(context, cityName, timeSection));
                        String reformatWeatherInfo = null;
                        // Aurora xuyong 2015-04-23 added for aurora's new feature start
                        WeatherResult result = null;
                        // Aurora xuyong 2015-04-23 added for aurora's new feature end
                        if (weatherInfo == null) {
                           String id = AuroraMsgWeatherUtils.getCityIdByCityName(context, cityName);
                           String info = null;
                           try {
                               // Mms channel
                               info = AuroraMsgWeatherUtils.getWeatherInfoFromNet(context, id);
                               weatherInfo = AuroraMsgWeatherUtils.extractUsefulInfo(info);
                               reformatWeatherInfo = cityName + WeatherInfo.RECORD_DIVIDER + AuroraMsgWeatherUtils.reformat(weatherInfo);
                           } catch (Exception e) {
                               e.printStackTrace();
                           }
                           AuroraMsgWeatherUtils.insertNewInfo(context, cityName, timeSection, reformatWeatherInfo);
                           // Aurora xuyong 2015-04-23 added for aurora's new feature start
                           result = AuroraMsgWeatherUtils.getWeatherResult(context, weatherInfo);
                           if (result != null) {
                               reformatWeatherInfo += WeatherInfo.RECORD_DIVIDER + result.getIndex() + WeatherInfo.RECORD_DIVIDER + result.getName();
                           }
                           // Aurora xuyong 2015-04-23 added for aurora's new feature end
                        }
                        // Aurora xuyong 2015-04-25 modified for aurora's new feature start
                        Log.e("Mms/Weather", "reformatWeatherInfo = " + reformatWeatherInfo + WeatherInfo.RECORD_DIVIDER + timeSection);
                        values.put("weather_info", reformatWeatherInfo + WeatherInfo.RECORD_DIVIDER + timeSection);
                        // Aurora xuyong 2015-04-25 modified for aurora's new feature end
                    }
            }
            // Aurora xuyong 2015-01-30 added for aurora's new feature end
        } else {
            address = getString(R.string.unknown_sender);
            values.put(Sms.ADDRESS, address);
            // Aurora xuyong 2015-01-30 added for aurora's new feature start
            mInstance.setName(address);
            // Aurora xuyong 2015-01-30 added for aurora's new feature end
        }
        // Aurora xuyong 2014-07-02 added for reject feature start
        // check whether we need reject this message if it comes from a contact
        // who is in the black name list
        if (MmsApp.sHasRejectFeature) {
            if (Utils.needRejectBlackMsg(getApplicationContext()) && Utils.isInBlackList(getApplicationContext(), address)) {
             // Aurora xuyong 2014-08-06 modified for bug #6895 start
                values.put("reject", 1);
             // Aurora xuyong 2014-08-23 added for bug #7909 start
             // Aurora xuyong 2014-09-02 modified for whitelist feature start
                rejectValue = true;
                //MessagingNotification.setIsRejectMsg(true);
             // Aurora xuyong 2014-09-02 modified for whitelist feature end
             // Aurora xuyong 2014-08-23 added for bug #7909 end
             // Aurora xuyong 2014-08-06 modified for bug #6895 end
            }
        }
        // Aurora xuyong 2014-07-02 added for reject feature end
        GnSmsMessage gnSms = new GnSmsMessage();
        // Aurora xuyong 2014-09-02 added for whitelist feature start
        MessagingNotification.setIsRejectMsg(rejectValue);
        // Aurora xuyong 2015-01-30 added for aurora's new feature start
        mIsPriOrRejMsg = rejectValue ? 1 : 0;
        // Aurora xuyong 2015-01-30 added for aurora's new feature end
        // Aurora xuyong 2014-09-02 added for whitelist feature end
        // for gemini, we should add sim id information
        if (MmsApp.mGnMultiSimMessage) {//guoyx 20130116
            // conver slot id to sim id        
            SIMInfo si = SIMInfo.getSIMInfoBySlot(context, gnSms.getMessageSimId(sms));
            if (null == si) {
                Log.e(MmsApp.TXN_TAG, "SmsReceiverService:SIMInfo is null for slot " + gnSms.getMessageSimId(sms));
                return null;
            }
            values.put(Sms.SIM_ID, (int)si.mSimId/*sms.getMessageSimId()*/);
            
            /* MTK note: for FTA test in the GEMINI phone
            * We need to tell SmsManager where the last incoming SMS comes from.
            * This is because the mms APP and Phone APP runs in two different process
            * and mms will use setSmsMemoryStatus to tell modem that the ME storage is full or not.
            * Since We need to dispatch the infomation about ME storage to currect SIM
            * so we should use setLastIncomingSmsSimId here 
            * to tell SmsManager this to let it dispatch the info.
            */
            GnSmsManager.getDefault().setLastIncomingSmsSimId(gnSms.getMessageSimId(sms));
        }
        ContentResolver resolver = context.getContentResolver();
        Uri insertedUri = SqliteWrapper.insert(context, resolver, Inbox.CONTENT_URI, values);
        
        // store on SIM if needed
        SharedPreferences prefs = AuroraPreferenceManager.getDefaultSharedPreferences(context);
        String optr = SystemProperties.get("ro.operator.optr");
        String storeLocation = null;
        //MTK_OP02_PROTECT_START
        if (MmsApp.OPERATOR_UNICOM.equals(optr)) {
            if (MmsApp.mGnMultiSimMessage) {
                final String saveLocationKey = Integer.toString(gnSms.getMessageSimId(sms)) 
                        + "_" + MessagingPreferenceActivity.SMS_SAVE_LOCATION;
                storeLocation = prefs.getString(saveLocationKey, "Phone");
            } else {
                storeLocation = prefs.getString(MessagingPreferenceActivity.SMS_SAVE_LOCATION, "Phone");
            }
        } else {
        //MTK_OP02_PROTECT_END
            storeLocation = prefs.getString(MessagingPreferenceActivity.SMS_SAVE_LOCATION, "Phone");
        //MTK_OP02_PROTECT_START
        }
        //MTK_OP02_PROTECT_END
        if (SIM_CARD.equals(storeLocation)) {
            String sc = (null == sms.getServiceCenterAddress()) ? "" : sms.getServiceCenterAddress();
            boolean bSucceed = true;
            for (int i = 0; i < pduCount; i++) {
                sms = msgs[i];
                //Gionee <guoyx> <2013-06-05> modify for CR00823075 begin
                if (MmsApp.mGnMultiSimMessage) {
                    bSucceed = GnGeminiSmsManager.copyMessageToIccGemini(sms);
                    Log.d(MmsApp.TXN_TAG, "save sms on SIM. part:" + i 
                            + "; result:" + bSucceed + "; sc:" + sc + "; slotId:" + gnSms.getMessageSimId(sms));
                    //Gionee <guoyx> <2013-05-31> add for CR00820781 begin
                } else {
                    // Aurora xuyong 2013-11-15 modified for S4 adapt start
                    bSucceed = SmsManager.getDefault().copyMessageToIcc(gionee.telephony.GnSmsMessage.getInstance().getSmsc(sms),gionee.telephony.GnSmsMessage.getInstance().getSmsc(sms), 
                            SmsManager.STATUS_ON_ICC_READ);
                    // Aurora xuyong 2013-11-15 modified for S4 adapt end
                    Log.d(MmsApp.TXN_TAG, "save sms on SIM. part:" + i + "; result:" + bSucceed + "; sc:" + sc);
                    //Gionee <guoyx> <2013-05-31> add for CR00820781 end
                } 
//                else {
//                    long time = getGnTimestampMillis(sms.getTimestampMillis());
//                    bSucceed = GnSmsManager.getDefault().copyTextMessageToIccCard(sc, address,
//                            sms.getDisplayMessageBody(), SmsManager.STATUS_ON_ICC_READ, time);
//                    Log.d(MmsApp.TXN_TAG, "save sms on SIM. part:" + i + "; result:" + bSucceed + "; sc:" + sc);
//                }
                //Gionee <guoyx> <2013-06-05> modify for CR00823075 end
            }
        }

        // set sms size
        if (null != insertedUri) {
            int messageSize = 0;
            if (pduCount == 1) {
                messageSize = sms.getPdu().length;
            } else {
                for (int i = 0; i < pduCount; i++) {
                    sms = msgs[i];
                    messageSize += sms.getPdu().length;
                }
            }
            ContentValues sizeValue = new ContentValues();
            sizeValue.put(Mms.MESSAGE_SIZE, messageSize);
            SqliteWrapper.update(this, getContentResolver(), insertedUri, sizeValue, null, null);
        }

        // Now make sure we're not over the limit in stored messages
        // Aurora xuyong 2014-08-06 modified for bug #6931 start
        Long threadId = -1l;
        try {
            threadId = Threads.getOrCreateThreadId(context, address);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
        // Aurora xuyong 2014-08-06 modified for bug #6931 end
        try {
            ThreadCountManager.getInstance().isFull(threadId, context, ThreadCountManager.OP_FLAG_INCREASE);
        } catch (Exception oe) {
            Log.e(TAG, oe.getMessage());
        }
        Recycler.getSmsRecycler().deleteOldMessagesByThreadId(getApplicationContext(), threadId);

        //gionee gaoj 2013-3-11 added for CR00782858 start
        // Aurora liugj 2013-11-07 deleted for hide widget start
        //MmsWidgetProvider.notifyDatasetChanged(context);
          // Aurora liugj 2013-11-07 deleted for hide widget end
        //gionee gaoj 2013-3-11 added for CR00782858 end
        // Aurora xuyong 2015-01-30 added for aurora's new feature start
        Message msg = Message.obtain(mToastHandler, SHOW_IPOPS);
        msg.sendToTarget();
        // Aurora xuyong 2015-01-30 added for aurora's new feature end
        return insertedUri;
    }

    /**
     * Extract all the content values except the body from an SMS
     * message.
     */
    private ContentValues extractContentValues(SmsMessage sms) {
        // Store the message in the content provider.
        ContentValues values = new ContentValues();

        values.put(Inbox.ADDRESS, sms.getDisplayOriginatingAddress());
/*
        // Use now for the timestamp to avoid confusion with clock
        // drift between the handset and the SMSC.
        // Check to make sure the system is giving us a non-bogus time.
        Calendar buildDate = new GregorianCalendar(2011, 8, 18);    // 18 Sep 2011
        Calendar nowDate = new GregorianCalendar();
        long now = System.currentTimeMillis();
        nowDate.setTimeInMillis(now);

        if (nowDate.before(buildDate)) {
            // It looks like our system clock isn't set yet because the current time right now
            // is before an arbitrary time we made this build. Instead of inserting a bogus
            // receive time in this case, use the timestamp of when the message was sent.
            now = sms.getTimestampMillis();
        }

        values.put(Inbox.DATE, new Long(now));
*/
        //use local time
        values.put(Inbox.DATE, new Long(System.currentTimeMillis()));
        values.put(Inbox.DATE_SENT, Long.valueOf(sms.getTimestampMillis()));
        values.put(Inbox.PROTOCOL, sms.getProtocolIdentifier());
        values.put(Inbox.READ, 0);
        values.put(Inbox.SEEN, 0);
        if (sms.getPseudoSubject().length() > 0) {
            values.put(Inbox.SUBJECT, sms.getPseudoSubject());
        }
        values.put(Inbox.REPLY_PATH_PRESENT, sms.isReplyPathPresent() ? 1 : 0);
        values.put(Inbox.SERVICE_CENTER, sms.getServiceCenterAddress());
        return values;
    }

    /**
     * Displays a class-zero message immediately in a pop-up window
     * with the number from where it received the Notification with
     * the body of the message
     *
     */
     private void displayClassZeroMessage(Context context, Intent intent, String format) {
        // Using NEW_TASK here is necessary because we're calling
        // startActivity from outside an activity.
        Log.v(MmsApp.TXN_TAG, "Sms displayClassZeroMessage");

        intent.setComponent(new ComponentName(context, ClassZeroActivity.class))
            .putExtra("format", format)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        // add for gemini, add sim id info
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            intent.putExtra(GnPhone.GEMINI_SIM_ID_KEY, intent.getIntExtra(GnPhone.GEMINI_SIM_ID_KEY, -1));
        }

        context.startActivity(intent);
    }
/*    
    private void displayClassZeroMessage(Context context, SmsMessage sms) {
        // Using NEW_TASK here is necessary because we're calling
        // startActivity from outside an activity.
        Log.v(MmsApp.TXN_TAG, "Sms displayClassZeroMessage");
        Intent smsDialogIntent = new Intent(context, ClassZeroActivity.class)
                .putExtra("pdu", sms.getPdu())
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                          | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        // add for gemini, add sim id info
        if(FeatureOption.MTK_GEMINI_SUPPORT == true){
            smsDialogIntent.putExtra(Phone.GEMINI_SIM_ID_KEY, sms.getMessageSimId());
        }

        context.startActivity(smsDialogIntent);
    }
*/
    private void registerForServiceStateChanges() {
        Context context = getApplicationContext();
        unRegisterForServiceStateChanges();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TelephonyIntents.ACTION_SERVICE_STATE_CHANGED);
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE) || LogTag.DEBUG_SEND) {
            Log.v(TAG, "registerForServiceStateChanges");
        }

        context.registerReceiver(SmsReceiver.getInstance(), intentFilter);
    }

    private void unRegisterForServiceStateChanges() {
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE) || LogTag.DEBUG_SEND) {
            Log.v(TAG, "unRegisterForServiceStateChanges");
        }
        try {
            Context context = getApplicationContext();
            context.unregisterReceiver(SmsReceiver.getInstance());
        } catch (IllegalArgumentException e) {
            // Allow un-matched register-unregister calls
        }
    }


    /**
     * send first queue message, and the messages should have the same SIMID as the last send one.
     * * @param lastSIMID The SIM ID that last message used
     *
     */
    // 2.2
// Aurora yudingmin 2014-12-09 modified for thread's app result start
    public void sendFirstQueuedMessageGemini(int simId) {
        sendFirstQueuedMessageGemini(simId, null);
    }
// Aurora yudingmin 2014-12-09 modified for thread's app result end
    
// Aurora yudingmin 2014-12-09 added for thread's app result start
    public synchronized void sendFirstQueuedMessageGemini(int simId, String thirdResponse) {
// Aurora yudingmin 2014-12-09 added for thread's app result end
        Log.d(MmsApp.TXN_TAG, "sendFirstQueuedMessageGemini() simId=" + simId);
        boolean success = true;
        // get all the queued messages from the database
        final Uri uri = Uri.parse("content://sms/queued");
        ContentResolver resolver = getContentResolver();
        //gionee gaoj 2012-8-21 modified for CR00679004 start
        String selectionString = null;
        if (MmsApp.mGnRegularlyMsgSend) {
            selectionString = Mms.SIM_ID + "=" + simId + " and date <= " + System.currentTimeMillis();
        } else {
            selectionString = Mms.SIM_ID + "=" + simId;
        }
        // Aurora xuyong 2014-11-05 added for privacy feature start
        if (MmsApp.sHasPrivacyFeature) {
            if (selectionString == null || selectionString.length() == 0) {
                selectionString = " is_privacy >= 0 ";
            } else if (!selectionString.contains("is_privacy")) {
                selectionString = "is_privacy >= 0 AND " + selectionString;
            }
        }
        // Aurora xuyong 2014-11-05 added for privacy feature end
        Cursor c  = SqliteWrapper.query(this, resolver, uri,
                        SEND_PROJECTION, selectionString, null, "date ASC");   // date ASC so we send out in
                                                                    // same order the user tried
                                                                    // to send messages.
        //gionee gaoj 2012-8-21 modified for CR00679004 end
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    String msgText = c.getString(SEND_COLUMN_BODY);
                    String address = c.getString(SEND_COLUMN_ADDRESS);
                    int threadId = c.getInt(SEND_COLUMN_THREAD_ID);
                    int status = c.getInt(SEND_COLUMN_STATUS);

                    int msgId = c.getInt(SEND_COLUMN_ID);
                    Uri msgUri = ContentUris.withAppendedId(Sms.CONTENT_URI, msgId);

                    SmsMessageSender sender = new SmsSingleRecipientSender(this,
                            address, msgText, threadId, status == Sms.STATUS_PENDING,
                            msgUri, simId);
                    
                    // Aurora yudingmin 2014-12-09 added for thread's app result start
                    sender.setThirdResponse(thirdResponse);
                    // Aurora yudingmin 2014-12-09 added for thread's app result end

                    if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.v(TAG, "sendFirstQueuedMessage " + msgUri +
                                ", address: " + address +
                                ", threadId: " + threadId +
                                ", body: " + msgText);
                    }
                    Log.d(MmsApp.TXN_TAG, "sendFirstQueuedMessage " + msgUri 
                        + ", address: " + address 
                        + ", threadId: " + threadId 
                        + ", body: " + msgText 
                        + ",Third_Response: " + thirdResponse
                        + ", simId: " + simId);
                    try {
                        sender.sendMessageGemini(SendingProgressTokenManager.NO_TOKEN, simId);
                        mSending = true;
                    } catch (MmsException e) {
                        Log.e(TAG, "sendFirstQueuedMessage: failed to send message " + msgUri
                                + ", caught ", e);
                        success = false;
                    }
                }
            } finally {
                if (!success) {
                    int msgId = c.getInt(SEND_COLUMN_ID);
                    Uri msgUri = ContentUris.withAppendedId(Sms.CONTENT_URI, msgId);
                    messageFailedToSend(msgUri, SmsManager.RESULT_ERROR_GENERIC_FAILURE);
                    // Aurora yudingmin 2014-12-09 modified for thread's app result start
                    sendFirstQueuedMessageGemini(simId, thirdResponse);
                    // Aurora yudingmin 2014-12-09 modified for thread's app result end
                }
                c.close();
            }
        }
        //check wether there are more pending messages need to be sent.
        c = null;
        // Aurora xuyong 2014-11-05 modified for privacy feature start
        c = SqliteWrapper.query(this, resolver, uri, SEND_PROJECTION, "is_privacy >= 0", null, null);
        // Aurora xuyong 2014-11-05 modified for privacy feature end
        Log.d(MmsApp.TXN_TAG, "there is pending sms:" + (c == null?false:true));
        if (success && c == null) {
            //only when no pending msgs,we can unregister safely.
            // We successfully sent all the messages in the queue. We don't need to
            // be notified of any service changes any longer.
            unRegisterForServiceStateChanges();
        }
        if (c != null) {
            c.close();
        }
    }

    private long getGnTimestampMillis(long timestampMillis) {
        int offSetTime = TimeZone.getDefault().getOffset(timestampMillis);
        return timestampMillis - offSetTime;
    }
}


