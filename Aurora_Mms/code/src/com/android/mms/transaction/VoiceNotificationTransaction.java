/**
    File Description:
        A class for doing with voice reminds from normal message and wap push message,
    while there is support from voice helper.
    Author: fengjy@gionee.com
    Create Date: 2012/09/12
    Change List:
*/


package com.android.mms.transaction;

import com.android.mms.data.Contact;
import com.android.mms.ui.MessageUtils;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.android.mms.MmsApp;
import com.android.mms.R;
//Gionee <zhouyj> <2013-06-17> add for CR00826910 begin
import android.provider.Settings;
//Gionee <zhouyj> <2013-06-17> add for CR00826910 end

public class VoiceNotificationTransaction {
    
    private static String TAG = "VoiceNotificationTransaction";
    private static boolean mIsSpeaking = false;
    private static boolean mServiceReady = false;
    private static String mSpeakBody = null;
    private static String mVoiceConfig = null;
    private static ContentObserver mVoiceConfigObserver = null;
    private static BroadcastReceiver mVoiceStateReceiver = null;
    private static long mVoiceTimeStamp = 0;
    private static MmsApp mApplication = MmsApp.getApplication();
    private static ContentResolver mContentResolver = mApplication.getContentResolver();
    //Ginee <zhouyj> <2013-04-28> add for CR00802651 begin
    private static String mMsgCard       = null;//msgcard
    private static String mMsgName       = null;//msgname
    private static String mMsgNumber     = null;//msgnumber
    private static String mMsgAttribtion = null;//msgattribution
    private static String mMsgContents   = null;//msgcontents
    private static String mMsgType       = null;//msgtype
    private static int    mMsgId         = 0;   //_id
    //Ginee <zhouyj> <2013-04-28> add for CR00802651 end
    
    // Gionee fengjianyi 2012-09-27 add for CR00704516 start
    private static BroadcastReceiver mVoiceConfigCleanReceiver = null;
    
    private static void reGetVoiceConfig() {
        mVoiceConfig = "false";
        try {
            Cursor cursor = mContentResolver.query(
                    Uri.parse("content://gn.com.voice/setting/3"), null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    mVoiceConfig = cursor.getString(cursor.getColumnIndex("setting_value"));
                    Log.d(TAG, "voice: config is " + mVoiceConfig + "!");
                }
                cursor.close();
            }
        } catch (Exception e) {
            // TODO: handle exception
            Log.i(VoiceHelperReceiver.TAG, "OldVoiceConfig is not support!!!");
        }
    }
    // Gionee fengjianyi 2012-09-27 add for CR00704516 end
    
    //Gionee <zhouyj> <2013-06-17> modify for CR00826910 begin
    public static boolean getVoiceConfig() {
        // Gionee fengjianyi 2012-09-27 modify for CR00704516 start
        if (!mServiceReady) {
            reGetVoiceConfig();
            initVoiceService();
        }
        //Gionee fengjianyi 2012-09-27 modify for CR00704516 end
        if (MmsApp.mGnSettingSoundSupport) {
            return getNewVoiceConfig();
        } else {
            return mVoiceConfig.equals("true");
        }
    }
    
    public static boolean getNewVoiceConfig() {
        String gn_sound_control_switch = "gn_sound_control_switch";
        String gn_sound_control_message = "gn_sound_control_message";
        int voiceContral = Settings.System.getInt(mApplication.getContentResolver(),
                gn_sound_control_switch/*Settings.System.GN_SOUND_CONTROL_SWITCH*/,0);
        int voiceContralMessage = Settings.System.getInt(mApplication.getContentResolver(),
                gn_sound_control_message/*Settings.System.GN_SOUND_CONTROL_MESSAGE*/,0);
        return voiceContral == 1 && voiceContralMessage == 1;
    }
    //Gionee <zhouyj> <2013-06-17> modify for CR00826910 end
    
    public static boolean isSpeaking() {
        return mIsSpeaking;
    }
    
    public static void stopSpeak() {
        Log.d(TAG, "voice: stop speak!");
        mIsSpeaking = false;
        Intent intent = new Intent("gn.voice.service.TTSService");
        mApplication.stopService(intent);
    }
    
    public static void speakRemindInfo() {
        //Log.d(TAG, "voice: stop last speak!");
        //stopSpeak();
        
        Log.d(TAG, "voice: start speak!");
        mIsSpeaking = true;
        //Gionee <zhouyj> <2013-04-28> modify for CR00802651 begin
        Log.d(TAG, "voice: content is " + mSpeakBody);
        Intent intent = new Intent("gn.voice.service.TTSService");
        intent.putExtra("id", String.valueOf(mMsgId));
        intent.putExtra("type", "mms");
        intent.putExtra("message", mSpeakBody);
        mVoiceTimeStamp = System.currentTimeMillis();
        intent.putExtra("timestamp", mVoiceTimeStamp);
        
        intent.putExtra("msgtype", mMsgType);
        intent.putExtra("msgcard", mMsgCard);
        intent.putExtra("msgname", mMsgName);
        intent.putExtra("msgnumber", mMsgNumber);
        intent.putExtra("msgattribution", mMsgAttribtion);
        intent.putExtra("msgcontents", mMsgContents);
        Log.i(VoiceHelperReceiver.TAG, "speakRemindInfo  start speak!  mMsgId = " + mMsgId + "   mMsgType = " + mMsgType + "\nmMsgCard = " + mMsgCard + "\nmMsgName = " + mMsgName + 
                "\nmMsgNumber = " + mMsgNumber + "\nmMsgAttribtion = " + mMsgAttribtion + "\nmMsgContents = " + mMsgContents +
                "\nMms_Verson = " + MessageUtils.getPackageVerson(mApplication, mApplication.getPackageName())
                + "   VoiceHelper_Verson = " + MessageUtils.getPackageVerson(mApplication, "gn.com.voice"));
        
        mApplication.startService(intent);
        //Gionee guoyangxu 2012-10-11 modified for CR00709756 begin
        mSpeakBody = null;
        mMsgId = 0;
        mMsgType = null;
        mMsgCard = null;
        mMsgName = null;
        mMsgNumber = null;
        mMsgAttribtion = null;
        mMsgContents = null;
        Log.d("fengjianyi", "set the speak content to null.");
        //Gionee guoyangxu 2012-10-11 modified for CR00709756 end
        //Gionee <zhouyj> <2013-04-28> modify for CR00802651 end
    }
    
    private static void initVoiceService() {
        Log.d(TAG, "voice: init service!");

        mVoiceConfigObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
            
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);

                // Gionee fengjianyi 2012-09-27 modify for CR00704516 start
                reGetVoiceConfig();
                // Gionee fengjianyi 2012-09-27 modify for CR00704516 end
            }
        };
        mContentResolver.registerContentObserver(
                Uri.parse("content://gn.com.voice/setting"), true, mVoiceConfigObserver);

        // Gionee fengjianyi 2012-09-27 add for CR00704516 start
        mVoiceConfigCleanReceiver = new BroadcastReceiver() {
            
            @Override
            public void onReceive(Context context, Intent intent) {
                reGetVoiceConfig();
            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_DATA_CLEARED);
        filter.addDataScheme("package");
        mApplication.registerReceiver(mVoiceConfigCleanReceiver, filter);
        // Gionee fengjianyi 2012-09-27 add for CR00704516 end
        
        mServiceReady = true;
    }
    
    private void destoryVoiceService() {
        Log.d(TAG, "voice: destory service!");

        stopSpeak();
        mServiceReady = false;
        mContentResolver.unregisterContentObserver(mVoiceConfigObserver);
        mVoiceConfigObserver = null;
        mVoiceConfig = null;
        // Gionee fengjianyi 2012-09-27 add for CR00704516 start
        mApplication.unregisterReceiver(mVoiceConfigCleanReceiver);
        mVoiceConfigCleanReceiver = null;
        //Gionee fengjianyi 2012-09-27 add for CR00704516 end
    }
    
    //Gionee <zhouyj> <2013-04-28> modify for CR00802651 begin
    public static void setSpeakMessage(int slotId, int id, String address, String type, String content) {
        String simName = "";
        String speakAddress = null;
        //Gionee guoyangxu 2012-10-11 modified for CR00709756 begin
        //slotId 0:sim1 ;slotId 1:sim2
        if (slotId == 1) {
            simName = mApplication.getString(R.string.gn_sim_b);
        } else if(slotId == 0){
            simName = mApplication.getString(R.string.gn_sim_a);
        }
        
        mMsgId = id;
        mMsgCard = simName;
        mMsgNumber = address;
        mMsgType = type;
        mMsgContents = content;
        // Aurora xuyong 2014-07-19 modified for sougou start
        mMsgAttribtion = MessageUtils.getNumAreaFromAora(mApplication.getApplicationContext(), address);
        // Aurora xuyong 2014-07-19 modified for sougou end

        //Gionee guoyangxu 2012-10-11 modified for CR00709756 end
        Contact contact = Contact.get(address, true, 0);
        
        if (!contact.existsInDatabase()) {
            // Aurora xuyong 2014-07-19 modified for sougou start
            String numberArea = MessageUtils.getNumAreaFromAora(mApplication.getApplicationContext(), address);
            // Aurora xuyong 2014-07-19 modified for sougou end
            String number = address.length() < 5 ? address :
                address.substring(address.length() - 4, address.length());
            if (numberArea != null) {
                speakAddress = mApplication.getString(R.string.gn_last_nubmer) + 
                    number + numberArea + mApplication.getString(R.string.gn_unknow_nubmer);
            } else {
                speakAddress = mApplication.getString(R.string.gn_last_nubmer) + 
                    number + mApplication.getString(R.string.gn_unknow_nubmer);
            }
            mMsgName = null;
        } else if (!TextUtils.isEmpty(contact.getName())) {
            String name = contact.getName();
            if (!name.contains(address) && !name.equals(address)) {
                speakAddress = name;
            } else {
                String number = address.length() < 5 ? address :
                    address.substring(address.length() - 4, address.length());
                speakAddress = mApplication.getString(R.string.gn_last_nubmer) + number;
            }
            mMsgName = name;
        } else {
            String number = address.length() < 5 ? address :
                address.substring(address.length() - 4, address.length());
            speakAddress = mApplication.getString(R.string.gn_last_nubmer) + number;
            mMsgName = null;
        }
        //Gionee guoyangxu 2012-10-11 modified for CR00709756 begin
        if (mSpeakBody == null) {
            mSpeakBody = mApplication.getString(R.string.gn_notify_message,
                    simName, speakAddress);
            Log.d("fengjianyi", "set the speak content: " + mSpeakBody);
        }
        //Gionee guoyangxu 2012-10-11 modified for CR00709756 end
    }
    //Gionee <zhouyj> <2013-04-28> modify for CR00802651 end
}
