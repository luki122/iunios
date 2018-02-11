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
// Aurora xuyong 2015-08-29 added for bug #15926 start
import android.os.Build;
// Aurora xuyong 2015-08-29 added for bug #15926 end
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
// Aurora xuyong 2015-08-29 added for bug #15926 start
import android.support.v4.app.NotificationCompat;
// Aurora xuyong 2015-08-29 added for bug #15926 end
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneBase;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyCapabilities;

import java.util.HashSet;


public class I2RejectNotificationMgr extends  RejectNotificationMgr{
    private static final String LOG_TAG = "I2RejectNotificationMgr";




    /**
     * Private constructor (this is a singleton).
     * @see init()
     */
    public I2RejectNotificationMgr(PhoneGlobals app) {
       super(app);
    }

//    static RejectNotificationMgr init(PhoneGlobals app) {
//        synchronized (I2RejectNotificationMgr.class) {
//            if (sInstance == null) {
//                sInstance = new I2RejectNotificationMgr(app);
//            } else {
////                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
//            }
//            return sInstance;
//        }
//    }
    

    // Aurora xuyong 2015-08-29 added for bug #15926 start
    public void notifyHangupBlackCall(String number) {
    	
    	if(NotificationMgr.useCustomView) {
    		notifyHangupBlackCallCustom(number);
    		return;
    	}

   	    if (DBG) log("notifyHangupBlackCall(): number = " + number);
   	    if(!ManageReject.isBlackNoticationEnable) {
   	    	return;
   	    }
   	    mHangupBlackCallCount++;
   	    
   	    String title = mContext.getString(R.string.aurora_add_black_title);
   	    long time = System.currentTimeMillis();   	    
	   	if (DBG) log("notifyHangupBlackSms() mHangupBlackCallCount =" + mHangupBlackCallCount + " mHangupBlackSmsCount=" + mHangupBlackSmsCount);
	    String expandedText = mContext.getString(R.string.aurora_hangup_black_many_content, mHangupBlackCallCount + mHangupBlackSmsCount);
   	    final NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
		builder.setContentTitle(title).setContentText(expandedText)
				.setSmallIcon(R.drawable.reject_launcher).setTicker(null)
				.setWhen(time)
				.setAutoCancel(true)
		        .setContentIntent(ManageReject.createGotoRejectIntent(mContext))
	            .setDeleteIntent(createClearHangupBlackCallsIntent());
        Notification notification = builder.build();
        
        mNotificationManager.notify(HANGUP_BLACK_CALL_NOTIFICATION, notification);
        Intent intent = new Intent("AURORA_HANGUP_CALL");
        intent.putExtra("isCall", true);
        mContext.sendBroadcast(intent);
    

    }
    
    public void notifyHangupBlackSms() {
    	
    	if(NotificationMgr.useCustomView) {
    		notifyHangupBlackSmsCustom();
    		return;
    	}

    	if (DBG) log("notifyHangupBlackSms()");
   	    if(!ManageReject.isBlackNoticationEnable) {
   	    	return;
   	    }
   	    mHangupBlackSmsCount++;
   	    
   	    String title = mContext.getString(R.string.aurora_add_black_title);
   	    long time = System.currentTimeMillis();
	   	if (DBG) log("notifyHangupBlackSms() mHangupBlackCallCount =" + mHangupBlackCallCount + " mHangupBlackSmsCount=" + mHangupBlackSmsCount);
	    String expandedText = mContext.getString(R.string.aurora_hangup_black_many_content, mHangupBlackCallCount + mHangupBlackSmsCount);
   	    final NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
		builder.setContentTitle(title).setContentText(expandedText)
				.setSmallIcon(R.drawable.reject_launcher).setTicker(null)
				.setWhen(time)
		        .setAutoCancel(true)
		        .setContentIntent(ManageReject.createGotoRejectIntent(mContext))
	            .setDeleteIntent(createClearHangupBlackCallsIntent());
   	    
   	    
   	    
        Notification notification = builder.build();
        mNotificationManager.notify(HANGUP_BLACK_CALL_NOTIFICATION, notification);
        Intent intent = new Intent("AURORA_HANGUP_CALL");
        intent.putExtra("isCall", false);
        mContext.sendBroadcast(intent);
    
    }
    
    // Aurora xuyong 2015-08-29 added for bug #15926 start
    public void notifyHangupBlackCallCustom(String number) {

   	    if (DBG) log("notifyHangupBlackCallCustom(): number = " + number);
   	    if(!ManageReject.isBlackNoticationEnable) {
   	    	return;
   	    }
   	    mHangupBlackCallCount++;
   	    
   	    String title = mContext.getString(R.string.aurora_add_black_title);
   	    long time = System.currentTimeMillis();
	   	if (DBG) log("notifyHangupBlackCallCustom() mHangupBlackCallCount =" + mHangupBlackCallCount + " mHangupBlackSmsCount=" + mHangupBlackSmsCount);
	    String expandedText = mContext.getString(R.string.aurora_hangup_black_many_content, mHangupBlackCallCount + mHangupBlackSmsCount);
   	    final NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
   	    builder.setContentTitle(title).setContentText(expandedText).setSmallIcon(R.drawable.reject_launcher).setTicker(null)
                .setWhen(time);
        Notification notification = builder.build();
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.custom_reject_notification);
        remoteViews.setImageViewResource(R.id.aurora_notification, R.drawable.reject_launcher);
        remoteViews.setTextViewText(R.id.aurora_address, title); 
        remoteViews.setTextViewText(R.id.aurora_body, expandedText);
        remoteViews.setTextViewText(R.id.aurora_date, RejectUtils.formatAuroraTimeStampString(mContext, time));
        notification.contentView = remoteViews;
        notification.contentIntent = ManageReject.createGotoRejectIntent(mContext);
        notification.deleteIntent = createClearHangupBlackCallsIntent();
        mNotificationManager.notify(HANGUP_BLACK_CALL_NOTIFICATION, notification);
        Intent intent = new Intent("AURORA_HANGUP_CALL");
        intent.putExtra("isCall", true);
        mContext.sendBroadcast(intent);
    

    }
    
    public void notifyHangupBlackSmsCustom() {

    	if (DBG) log("notifyHangupBlackSmsCustom()");
   	    if(!ManageReject.isBlackNoticationEnable) {
   	    	return;
   	    }
   	    mHangupBlackSmsCount++;
   	    
   	    String title = mContext.getString(R.string.aurora_add_black_title);
   	    long time = System.currentTimeMillis();
	   	if (DBG) log("notifyHangupBlackSmsCustom() mHangupBlackCallCount =" + mHangupBlackCallCount + " mHangupBlackSmsCount=" + mHangupBlackSmsCount);
	    String expandedText = mContext.getString(R.string.aurora_hangup_black_many_content, mHangupBlackCallCount + mHangupBlackSmsCount);
   	    final NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
   	    builder.setContentTitle(title).setContentText(expandedText).setSmallIcon(R.drawable.reject_launcher).setTicker(null)
                .setWhen(time);
        Notification notification = builder.build();
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.custom_reject_notification);
        remoteViews.setImageViewResource(R.id.aurora_notification, R.drawable.reject_launcher);
        remoteViews.setTextViewText(R.id.aurora_address, title); 
        remoteViews.setTextViewText(R.id.aurora_body, expandedText);
        remoteViews.setTextViewText(R.id.aurora_date, RejectUtils.formatAuroraTimeStampString(mContext, time));
        notification.contentView = remoteViews;
        notification.contentIntent = ManageReject.createGotoRejectIntent(mContext);
        notification.deleteIntent = createClearHangupBlackCallsIntent();
        mNotificationManager.notify(HANGUP_BLACK_CALL_NOTIFICATION, notification);
        Intent intent = new Intent("AURORA_HANGUP_CALL");
        intent.putExtra("isCall", false);
        mContext.sendBroadcast(intent);
    
    }
    
}
