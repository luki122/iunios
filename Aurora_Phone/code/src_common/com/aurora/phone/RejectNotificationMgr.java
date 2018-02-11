/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.android.phone;

import java.util.Map;
import java.util.Set;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.StatusBarManager;
import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.telephony.ServiceState;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneBase;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyCapabilities;

import java.util.HashSet;


public class RejectNotificationMgr {
    private static final String LOG_TAG = "RejectNotificationMgr";
    protected static final boolean DBG = (PhoneGlobals.DBG_LEVEL >= 1);
    protected static final boolean VDBG = true;



    /** The singleton PrivateNotificationMgr instance. */
    protected static RejectNotificationMgr sInstance;

    protected Context mContext;
    protected NotificationManager mNotificationManager;


    /**
     * Private constructor (this is a singleton).
     * @see init()
     */
    protected RejectNotificationMgr(PhoneGlobals app) {
        mContext = app;
        mNotificationManager = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
        sp = app.getSharedPreferences("AddBlackTimeList", Context.MODE_PRIVATE); 
    }

    static RejectNotificationMgr init(PhoneGlobals app) {
        synchronized (RejectNotificationMgr.class) {
            if (sInstance == null) {
                sInstance = new RejectNotificationMgr(app);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            return sInstance;
        }
    }
    

    protected int mBlackNotificationCount = 0;
    protected Set<String> mBlackNumbers=new HashSet<String>();
    protected SharedPreferences sp;
    protected static final int BLACK_NOTIFICATION_BASE = 17000;
    protected static final int BLACK_LIST_NOTIFICATION_BASE = 3;

    
    public void notifyAddBlackCall(String number, String name) {     	
    	 if (DBG) log("notifyAddBlackCall(): number = " + number + " name = " + name);
    	 updateAddBlackTimeList();
    	 long lastAddTime = sp.getLong(number, 0);    	 
    	 long now = System.currentTimeMillis();
    	 if(mBlackNumbers.contains(number) || !RejectUtils.isSupportBlack() || now - lastAddTime < 24 * 3600 * 1000) {    		 
           	 if (DBG) log("notifyAddBlackCall(): return now - lastAddTime < 24 * 3600 * 1000 = " + (now - lastAddTime < 24 * 3600 * 1000));
           	 if (DBG) log("now = "+ now + " lastAddTime = "+ lastAddTime);
    		 return;
    	 }
    	 final Notification.Builder builder = new Notification.Builder(mContext);    	 
//         builder.setSmallIcon(R.drawable.aurora_black_icon);
         builder.setUsesChronometer(false);
         builder.setWhen(0);
         builder.setAutoCancel(true);
         builder.setDeleteIntent(createClearAddBlackIntent(BLACK_NOTIFICATION_BASE + BLACK_LIST_NOTIFICATION_BASE + mBlackNotificationCount, number));
//         PendingIntent inCallPendingIntent =  PendingIntent.getActivity(mContext, 0, PhoneGlobals.createAddBlackIntent(number, name), 0);
//         builder.setContentIntent(inCallPendingIntent);
         builder.setContentIntent(ManageReject.createAddBlackIntent(mContext, BLACK_NOTIFICATION_BASE + BLACK_LIST_NOTIFICATION_BASE + mBlackNotificationCount, number, name));
         Drawable largeIcon = mContext.getResources().getDrawable(R.drawable.reject_launcher);
         builder.setLargeIcon(((BitmapDrawable) largeIcon).getBitmap());
         String expandedText = mContext.getString(R.string.aurora_add_black_content, TextUtils.isEmpty(name) ? number : name);
         builder.setContentTitle(mContext.getString(R.string.aurora_add_black_title));
         builder.setContentText(expandedText);
         builder.setTicker(expandedText);
         //需要双指才能打开，默认还是显示省略号
         Notification notification = new Notification.BigTextStyle(builder).bigText(expandedText).build();
         notification.icon = R.drawable.reject_launcher;
//         Notification notification = builder.getNotification();
         mNotificationManager.notify(BLACK_NOTIFICATION_BASE + BLACK_LIST_NOTIFICATION_BASE + mBlackNotificationCount, notification);
         mBlackNotificationCount++;
         mBlackNotificationCount = mBlackNotificationCount % 100;
         mBlackNumbers.add(number);
    	 SharedPreferences.Editor editor = sp.edit();
         editor.putLong(number, now);
         editor.commit();
    }    
    
    protected void updateAddBlackTimeList() {
    	try {
    		Map<String, ?> map = sp.getAll();
    		Set<String> keys=map.keySet();   
    		SharedPreferences.Editor editor = sp.edit();
	    	long now = System.currentTimeMillis(); 
    		for(String key:keys){   
    			 long lastAddTime = sp.getLong(key, 0);    	   	    	
    	    	 if(now - lastAddTime >= 24 * 3600 * 1000) {
    	             editor.remove(key);
    	    	 }
    		}  
	        editor.commit();
    	} catch(Exception e) {
    		e.printStackTrace();
    	}    	
    }
    
    protected PendingIntent createClearAddBlackIntent(int id, String number) {
        Intent intent = new Intent(mContext, ClearBlackCallsService.class);
        intent.setAction(ClearBlackCallsService.ACTION_CLEAR_ADD_BLACK);
        intent.putExtra("id", id);
        intent.putExtra("number", number);
        return PendingIntent.getService(mContext, 0, intent, 0);
    }
    
    public void cancelAddBlackNotification(int id, String number) {
   	    if (DBG) log("cancelAddBlackNotification() id =" + id + " number = " + number);
   	    mBlackNumbers.remove(number);
        mNotificationManager.cancel(id);
    }
    
    protected int mHangupBlackCallCount = 0;
    protected int mHangupBlackSmsCount = 0;
    protected static final int HANGUP_BLACK_CALL_NOTIFICATION = BLACK_NOTIFICATION_BASE + BLACK_LIST_NOTIFICATION_BASE + 100;
    
    public void notifyHangupBlackCall(String number) {     	
   	    if (DBG) log("notifyHangupBlackCall(): number = " + number);
   	    if(!ManageReject.isBlackNoticationEnable) {
   	    	return;
   	    }
   	    mHangupBlackCallCount++;
   	    final Notification.Builder builder = new Notification.Builder(mContext);    	 
//        builder.setSmallIcon(R.drawable.aurora_black_icon);
        Drawable largeIcon = mContext.getResources().getDrawable(R.drawable.reject_launcher);
        builder.setLargeIcon(((BitmapDrawable) largeIcon).getBitmap());
        builder.setUsesChronometer(false);
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(true);
        updateBlackNotificationMode();
        builder.setDeleteIntent(createClearHangupBlackCallsIntent());
        builder.setContentIntent(ManageReject.createGotoRejectIntent(mContext));
        String expandedText;
        //mode不是1就是3
//        if(mHangupBlackMode == 1) {
//            if (mHangupBlackCallCount == 1) {
//            	String name = RejectUtils.getLastBlackName();
//                expandedText = mContext.getString(R.string.aurora_hangup_black_single_content, TextUtils.isEmpty(name) ? number : name);
//            } else {
//                expandedText = mContext.getString(R.string.aurora_hangup_black_many_call_content, mHangupBlackCallCount + mHangupBlackSmsCount);
//            }	
//        } else {
//        	expandedText = mContext.getString(R.string.aurora_hangup_black_many_content, mHangupBlackCallCount + mHangupBlackSmsCount);
//        }               
        if (DBG) log("notifyHangupBlackCall() mHangupBlackCallCount =" + mHangupBlackCallCount + " mHangupBlackSmsCount =" + mHangupBlackSmsCount);
        expandedText = mContext.getString(R.string.aurora_hangup_black_many_content, mHangupBlackCallCount + mHangupBlackSmsCount);
        builder.setContentTitle(mContext.getString(R.string.aurora_add_black_title));
        builder.setContentText(expandedText);
//        builder.setTicker(expandedText);
        //需要双指才能打开，默认还是显示省略号
        Notification notification = new Notification.BigTextStyle(builder).bigText(expandedText).build();
        notification.icon = R.drawable.reject_launcher;
        mNotificationManager.notify(HANGUP_BLACK_CALL_NOTIFICATION, notification);
        Intent intent = new Intent("AURORA_HANGUP_CALL");
        intent.putExtra("isCall", true);
        mContext.sendBroadcast(intent);
   }
    
    
    public void notifyHangupBlackSms() {     	
   	    if (DBG) log("notifyHangupBlackSms()");
   	    if(!ManageReject.isBlackNoticationEnable) {
   	    	return;
   	    }
   	    mHangupBlackSmsCount++;
   	    final Notification.Builder builder = new Notification.Builder(mContext);    	 
//        builder.setSmallIcon(R.drawable.aurora_black_icon);
        Drawable largeIcon = mContext.getResources().getDrawable(R.drawable.reject_launcher);
        builder.setLargeIcon(((BitmapDrawable) largeIcon).getBitmap());
        builder.setUsesChronometer(false);
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(true);
        updateBlackNotificationMode(); 
        builder.setDeleteIntent(createClearHangupBlackCallsIntent());
        builder.setContentIntent(ManageReject.createGotoRejectIntent(mContext));
        String expandedText;   
        //mode不是2就是3
//        if(mHangupBlackMode == 2) {
//            if (mHangupBlackSmsCount == 1) {
//                expandedText = mContext.getString(R.string.aurora_hangup_black_single_content, mContext.getString(R.string.notification_missedCall_message));
//            } else {
//                expandedText = mContext.getString(R.string.aurora_hangup_black_many_sms_content, mHangupBlackCallCount + mHangupBlackSmsCount);
//            }
//        } else {
//        	expandedText = mContext.getString(R.string.aurora_hangup_black_many_content, mHangupBlackCallCount + mHangupBlackSmsCount);
//        }               
 	    if (DBG) log("notifyHangupBlackSms() mHangupBlackCallCount =" + mHangupBlackCallCount + " mHangupBlackSmsCount=" + mHangupBlackSmsCount);
        expandedText = mContext.getString(R.string.aurora_hangup_black_many_content, mHangupBlackCallCount + mHangupBlackSmsCount);
        builder.setContentTitle(mContext.getString(R.string.aurora_add_black_title));
        builder.setContentText(expandedText);
//        builder.setTicker(expandedText);
        //需要双指才能打开，默认还是显示省略号
        Notification notification = new Notification.BigTextStyle(builder).bigText(expandedText).build();
        notification.icon = R.drawable.reject_launcher;
        mNotificationManager.notify(HANGUP_BLACK_CALL_NOTIFICATION, notification);
        Intent intent = new Intent("AURORA_HANGUP_CALL");
        intent.putExtra("isCall", false);
        mContext.sendBroadcast(intent);
   }    
    
    public void cancelHangupBlackCallNotification() {
   	    if (DBG) log("cancelHangupBlackCallNotification()");
    	mHangupBlackCallCount = 0;
    	mHangupBlackSmsCount = 0; 
    	mHangupBlackMode = 0;
        mNotificationManager.cancel(HANGUP_BLACK_CALL_NOTIFICATION);
    }
    
    protected PendingIntent createClearHangupBlackCallsIntent() {
        Intent intent = new Intent(mContext, ClearBlackCallsService.class);
        intent.setAction(ClearBlackCallsService.ACTION_CLEAR_HANGUP_BLACK_CALLS);
        return PendingIntent.getService(mContext, 0, intent, 0);
    }
    
    //0为初始值，1为只有来电，2为只有短信，3为混合拦截
    protected static int mHangupBlackMode = 0;
    protected void updateBlackNotificationMode() {
   	    if(mHangupBlackCallCount > 0 && mHangupBlackSmsCount == 0 ) {
   	    	mHangupBlackMode = 1;
   	    } else if(mHangupBlackCallCount == 0 && mHangupBlackSmsCount > 0) {
   	    	mHangupBlackMode = 2;
   	    } else if(mHangupBlackCallCount > 0 && mHangupBlackSmsCount > 0) {
   	    	mHangupBlackMode = 3;
   	    }
    }
    
    public static int getBlackNotificationMode() {
    	return mHangupBlackMode;
    }

    protected void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
    
    
}
