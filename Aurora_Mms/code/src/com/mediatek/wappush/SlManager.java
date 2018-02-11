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
import com.mediatek.wappush.pushparser.SlMessage;
import gionee.provider.GnTelephony.WapPush;
import com.mediatek.wappush.pushparser.SiMessage;
import com.android.mms.transaction.WapPushMessagingNotification;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import android.content.ActivityNotFoundException;
import aurora.preference.AuroraPreferenceManager;
import android.content.SharedPreferences;
import android.os.SystemProperties;
import android.provider.Browser;
import android.net.Uri;
import android.util.Log;

import com.android.mms.MmsConfig;
import com.android.mms.R;
import com.android.mms.ui.MessageUtils;

//gionee gaoj 2012-4-10 added for CR00555790 start
import gionee.provider.GnTelephony.Sms;
import com.gionee.mms.popup.PopUpUtils;
import android.os.Bundle;
import android.database.Cursor;
import com.android.mms.MmsApp;
import com.android.mms.data.Conversation;
//gionee gaoj 2012-4-10 added for CR00555790 end

public class SlManager extends WapPushManager {

    protected SlManager(Context context) {
        super(context);
    }

    @Override
    public void handleIncoming(ParsedMessage message) {
        
        if(message == null){
            Log.i(TAG,"SlManager handleIncoming: null message");
            return;
        }
        
        SlMessage slMsg = null;
        try{
             slMsg = (SlMessage)message;
        }catch (Exception e) {
            Log.e(TAG,"SlManager SiMessage error");
        }
        
        //store in db
        ContentValues values = new ContentValues();
        values.put(WapPush.ADDR,slMsg.getSenderAddr());
        values.put(WapPush.SERVICE_ADDR, slMsg.getServiceCenterAddr());
        values.put(WapPush.SIM_ID,slMsg.getSimId());
        values.put(WapPush.URL, slMsg.url);
        values.put(WapPush.ACTION, slMsg.action);
        
        boolean isAutoLanuching = false;
        if(MmsConfig.getSlAutoLanuchEnabled()&&autoLanuching(slMsg.url)){
            values.put(WapPush.SEEN,WapPush.STATUS_SEEN);
            values.put(WapPush.READ,WapPush.STATUS_READ);
            isAutoLanuching = true;
        }
  
        Uri uri = m_context.getContentResolver().insert(WapPush.CONTENT_URI_SL, values);
        
        //gionee gaoj 2012-10-12 added for CR00711168 start
        if (MmsApp.mIsSafeModeSupport) {
            return;
        }
        //gionee gaoj 2012-10-12 added for CR00711168 end

        //notification
        if(uri != null){
            Log.i(TAG,"SlManager:Store msg! " + slMsg.url );
            if(!isAutoLanuching){
                WapPushMessagingNotification.blockingUpdateNewMessageIndicator(m_context, true);
                //gionee gaoj 2012-4-10 added for CR00555790 start
                if (MmsApp.mGnPopupMsgSupport) {
                    showPopUpView(uri);
                }
                //gionee gaoj 2012-4-10 added for CR00555790 end
            }
        }
    }
    
    //if sl autoLanuching is set, we will open the url.
    private boolean autoLanuching(String url) {

        if (null == url) {
            return false;
        }

        SharedPreferences prefs = AuroraPreferenceManager
                .getDefaultSharedPreferences(m_context);
        boolean isAutoLoading = prefs.getBoolean(
                "pref_key_wappush_sl_autoloading", false);

        if (isAutoLoading) {

            WapPushMessagingNotification.notifySlAutoLanuchMessage(m_context, url);
            
            Uri uri = Uri.parse(MessageUtils.CheckAndModifyUrl(url));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.putExtra(Browser.EXTRA_APPLICATION_ID, m_context
                    .getPackageName());
            //MTK_OP01_PROTECT_START
            /*
            String optr = SystemProperties.get("ro.operator.optr");
            if (null != optr && optr.equals("OP01")) {
                intent.putExtra(Browser.APN_SELECTION, Browser.APN_MOBILE);
            } */
            //MTK_OP01_PROTECT_END
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                m_context.startActivity(intent);
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(m_context, R.string.error_unsupported_scheme,
                        Toast.LENGTH_LONG).show();
                Log.e(TAG, "Scheme " + uri.getScheme() + "is not supported!");
            }
            return true;
        }

        return false;
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
            bundle.putString(PopUpUtils.POPUP_INFO_ADDRESS, cursor.getString(cursor.getColumnIndexOrThrow(Sms.ADDRESS)));
            bundle.putLong(PopUpUtils.POPUP_INFO_DATE, cursor.getLong(cursor.getColumnIndexOrThrow(Sms.DATE)));
            bundle.putString(PopUpUtils.POPUP_INFO_BODY, cursor.getString(cursor.getColumnIndexOrThrow(WapPush.TEXT)));
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
