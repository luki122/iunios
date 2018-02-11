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

package com.mediatek.wappush;

import com.mediatek.wappush.pushparser.ParsedMessage;
import gionee.provider.GnTelephony.WapPush;
import com.mediatek.wappush.pushparser.SiMessage;
import com.android.mms.transaction.WapPushMessagingNotification;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

//gionee gaoj 2012-4-10 added for CR00555790 start
import gionee.provider.GnTelephony.Sms;
import com.gionee.mms.popup.PopUpUtils;
import android.os.Bundle;
import android.content.Intent;
import com.android.mms.MmsApp;
import com.android.mms.data.Conversation;
//gionee gaoj 2012-4-10 added for CR00555790 end
public class SiManager extends WapPushManager {

    public final static String TAG = "Mms/WapPush";
    
    public SiManager(Context context) {
        super(context);
    }

    @Override
    public void handleIncoming(ParsedMessage message) {

        if(message == null){
            Log.e(TAG,"SiManager handleIncoming: null message");
            return;
        }
        
        
        SiMessage siMsg = null;
        try{
             siMsg = (SiMessage)message;
        }catch (Exception e) {
            Log.e(TAG,"SiManager SiMessage error");
        }
        
        //Prepare for query
        ContentResolver resolver = m_context.getContentResolver();
        Cursor cursor = null;
        

        int currentTime = (int)(System.currentTimeMillis()/1000);
        
        //If there is no Created time, use the local device time instead (convert to UTC).
        if(siMsg.create == 0){
            siMsg.create = (int) (System.currentTimeMillis()/1000);
        }
        /*
         * Handle SI Message
         * Reference:WAP-167-ServiceInd-20010731-a
         */
        
        //if si-id is null , si-id will be set the url
        if(siMsg.siid == null){
            siMsg.siid = siMsg.url;
        }
        
        //1,Dicard Expired Message
        
        if(siMsg.expiration > 0 && siMsg.expiration < currentTime){
            Log.i(TAG,"SiManager:Expired Message! "+ siMsg.url );
            return;
        }
        
        /*
         * Query to find Message with the same si-id
         * 
         */
        String []projection = {WapPush._ID,WapPush.SIID,WapPush.URL,WapPush.CREATE, WapPush.ADDR, WapPush.TEXT};
        String selection = WapPush.SIID + "=?";
        String []selectionArgs ={siMsg.siid};
        
        //url && siid will be the same; if siid is null, no need to query database 
        if(siMsg.siid != null){
            cursor = resolver.query(WapPush.CONTENT_URI_SI, projection, selection, selectionArgs, null);
        }

        if(cursor != null){
            try {
                if (cursor.moveToFirst()) {
                    do{
                        long messageId = cursor.getLong(0);
                        String siid = cursor.getString(1);
                        int createdTime = cursor.getInt(3);
                        String address = cursor.getString(4);
                        String text = cursor.getString(5);
                        
                        if(siid.equals(siMsg.siid)){
                          //2,Received SI older than other SI with identical si-id, discard the older one
                            if(siMsg.create > 0 && siMsg.create < createdTime ){
                                Log.i(TAG,"SiManager:Out of order Message! " + siMsg.url );
                                return;
                            }else if(siMsg.create >= createdTime){
                                if (siMsg.getSenderAddr().equals(address) && siMsg.text.equals(text)) {
                                    Log.d(TAG, "Discard duplicate message!");
                                    return;
                                    /*//3,Received SI newer (or of the same age)than other SI with identical si-id, delete the older one
                                    m_context.getContentResolver().delete(ContentUris.withAppendedId(WapPush.CONTENT_URI,messageId), null, null);
                                    Log.i(TAG,"SiManager:Delete older Message! " + messageId);
                                    //we may need to cancel the notification and called off of the UI thread so ok to block
                                    WapPushMessagingNotification.blockingUpdateNewMessageIndicator(m_context, false);*/
                                } else {                                    
                                    // Insert new message!
                                }
                            }
                        }           
                        
                    }while(cursor.moveToNext());

                }
            } finally {
                cursor.close();
            }
        } 
        
        //4,SI has action=delete, discard it
        if(siMsg.action == SiMessage.ACTION_DELETE){
            Log.i(TAG,"SiManager:Discard delete Message! " + siMsg.url );
            return;
        }
        
        //5,Discard SI has action = none
        if(siMsg.action == SiMessage.ACTION_NONE){
            Log.i(TAG,"SiManager:Discard None Message! " + siMsg.url );
            return;
        }
        
        //store in db
        ContentValues values = new ContentValues();
        values.put(WapPush.ADDR,siMsg.getSenderAddr());
        values.put(WapPush.SERVICE_ADDR, siMsg.getServiceCenterAddr());
        values.put(WapPush.SIM_ID,siMsg.getSimId());
        values.put(WapPush.URL, siMsg.url);
        values.put(WapPush.SIID, siMsg.siid);
        values.put(WapPush.ACTION,siMsg.action);
        values.put(WapPush.CREATE,siMsg.create);
        values.put(WapPush.EXPIRATION,siMsg.expiration);
        values.put(WapPush.TEXT, siMsg.text);
        Uri uri = m_context.getContentResolver().insert(WapPush.CONTENT_URI_SI, values);
          
        //gionee gaoj 2012-10-12 added for CR00711168 start
        if (MmsApp.mIsSafeModeSupport) {
            return;
        }
        //gionee gaoj 2012-10-12 added for CR00711168 end

        //notification
        if(uri != null){
            Log.i(TAG,"SiManager:Store msg! " + siMsg.url );
            //called off of the UI thread so ok to block
            WapPushMessagingNotification.blockingUpdateNewMessageIndicator(m_context, true);
            //gionee gaoj 2012-4-10 added for CR00555790 start
        if (MmsApp.mGnPopupMsgSupport) {
            showPopUpView(uri);
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
        }
    }

    //gionee gaoj 2012-4-10 added for CR00555790 start
    private void showPopUpView(Uri messageUri) {
        if (PopUpUtils.mPopUpShowing && PopUpUtils.getPopNotfiSetting(m_context)) {
            Intent intent = new Intent(PopUpUtils.MSG_INFO_RECEIVER_ACTION);

            if (MmsApp.mEncryption) {
                if (getPopUpInfoBundle(messageUri) != null) {
                    intent.putExtras(getPopUpInfoBundle(messageUri));
                    m_context.sendBroadcast(intent);
                }
            } else {
                intent.putExtras(getPopUpInfoBundle(messageUri));
                m_context.sendBroadcast(intent);
            }

        } else if ((PopUpUtils.isLauncherView(m_context) || (PopUpUtils.isLockScreen(m_context) && !PopUpUtils.isMmsView(m_context))) && PopUpUtils.getPopNotfiSetting(m_context)) {
            Intent intents = new Intent(PopUpUtils.POPUP_ACTION);
            intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intents.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            if (MmsApp.mEncryption) {
                if (getPopUpInfoBundle(messageUri) != null) {
                    intents.putExtras(getPopUpInfoBundle(messageUri));
                    m_context.startActivity(intents);
                }
            } else {
                intents.putExtras(getPopUpInfoBundle(messageUri));
                m_context.startActivity(intents);
            }
        }
    }

    private Bundle getPopUpInfoBundle(Uri messageUri) {
        Cursor cursor = m_context.getContentResolver().query(messageUri, null, null, null, null);
        Bundle bundle = new Bundle();
        try {
            if (null == cursor || cursor.getCount() < 1) {
                return null;
            }
            cursor.moveToFirst();
            String text = cursor.getString(cursor.getColumnIndexOrThrow(WapPush.TEXT));
            String url = cursor.getString(cursor.getColumnIndexOrThrow(WapPush.URL));
            bundle.putString(PopUpUtils.POPUP_INFO_ADDRESS, cursor.getString(cursor.getColumnIndexOrThrow(Sms.ADDRESS)));
            bundle.putLong(PopUpUtils.POPUP_INFO_DATE, cursor.getLong(cursor.getColumnIndexOrThrow(Sms.DATE)));
            bundle.putString(PopUpUtils.POPUP_INFO_BODY, text+"\n"+url);
            bundle.putInt(PopUpUtils.POPUP_INFO_SIM_ID, cursor.getInt(cursor.getColumnIndexOrThrow(Sms.SIM_ID)));
            bundle.putInt(PopUpUtils.POPUP_INFO_MSG_TYPE, PopUpUtils.POPUP_TYPE_PUSH);
            bundle.putInt(PopUpUtils.POPUP_INFO_THREAD_ID, cursor.getInt(cursor.getColumnIndexOrThrow(Sms.THREAD_ID)));
            bundle.putString(PopUpUtils.POPUP_INFO_MSG_URI, messageUri.toString());

            if (MmsApp.mEncryption) {
                long threadId = cursor.getLong(cursor
                        .getColumnIndexOrThrow(Sms.THREAD_ID));
                Conversation conversation = Conversation.get(m_context,
                        threadId, false);
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
    //gionee gaoj 2012-4-10 added for CR00555790 end
}
