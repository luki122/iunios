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

package com.android.mms.ui;

import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
import com.android.mms.model.TextModel;
import com.android.mms.ui.WPMessageListAdapter.WPColumnsMap;
import com.android.mms.util.AddressUtils;
import com.aurora.featureoption.FeatureOption;
import com.android.internal.telephony.Phone;

//gionee gaoj 2012-4-26 added for CR00555790 start
import android.os.SystemProperties;
import android.text.format.DateFormat;
import com.android.mms.MmsApp;
//gionee gaoj 2012-4-26 added for CR00555790 end
public class WPMessageItem {
    
    private static String WP_TAG = "Mms/WapPush";

    final Context mContext;
    final int mType;
    final long mMsgId;
    boolean mLocked;            // locked to prevent auto-deletion

    String mTimestamp;
    String mAddress;
    String mContact;
    String mText;
    String mURL;
    long mCreate;
    String mExpiration;
    Pattern mHighlight; // portion of message to highlight (from search)
    int isExpired;   //if already be expired, the value is based on ERROR column
    int mAction;    //Priority
    //add for gemini
    int mSimId;
    //add for multi-delete
    // boolean mItemSelected = false;
    
    String mBody;

    WPMessageItem(Context context, int type, Cursor cursor,
            WPColumnsMap WPcolumnsMap, Pattern highlight) {
        mContext = context;
        mMsgId = cursor.getLong(WPcolumnsMap.mColumnMsgId);
        mHighlight = highlight;
        mType = type;

        long receiveDate = cursor.getLong(WPcolumnsMap.mColumnWpmsDate);        
        //gionee gaoj 2012-4-26 added for CR00555790 start
        if (MmsApp.mGnMessageSupport) {
            mTimestamp = MessageUtils.detailFormatGNTime(context, receiveDate);
        } else {
        //gionee gaoj 2012-4-26 added for CR00555790 end
        mTimestamp = String.format(context.getString(R.string.received_on), MessageUtils.formatTimeStampString(context, receiveDate));
        //gionee gaoj 2012-4-26 added for CR00555790 start
        }
        //gionee gaoj 2012-4-26 added for CR00555790 end
        
        mAddress = cursor.getString(WPcolumnsMap.mColumnWpmsAddr);
        if(!TextUtils.isEmpty(mAddress)) {
            mContact = Contact.get(mAddress, false).getName();
        }else{
            mContact = "";
        }
        
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.toString();
        
        mText = cursor.getString(WPcolumnsMap.mColumnWpmsText);
        if(null != mText && !"".equals(mText)){
            Log.i(WP_TAG, "WPMessageItem: " + "mText is : " + mText + "test");
            sBuilder.append(mText);
        }
        
        mURL = cursor.getString(WPcolumnsMap.mColumnWpmsURL);
        if(null != mURL && !"".equals(mURL)){
//            sBuilder.append("<br>");
            sBuilder.append("\n");
            sBuilder.append(mURL);
        }
        mBody = sBuilder.toString();
        
        mCreate = cursor.getLong(WPcolumnsMap.mColumnWpmsCreate) * 1000;
        
        long expirationDate = cursor.getLong(WPcolumnsMap.mColumnWpmsExpiration) * 1000;        
        if(0 != expirationDate){
            mExpiration = String.format(context.getString(R.string.wp_msg_expiration_label), MessageUtils.formatTimeStampString(context, expirationDate));
        }
        
        isExpired = cursor.getInt(WPcolumnsMap.mColumnWpmsError);
        mAction = cursor.getInt(WPcolumnsMap.mColumnWpmsAction);
        
        //add for gemini
        if (MmsApp.mGnMultiSimMessage == true){
            mSimId = cursor.getInt(WPcolumnsMap.mColumnWpmsSimId);
        }
        //gionee gaoj 2012-5-16 remove locked for CRCR00600687 start
        if (!MmsApp.mGnMessageSupport) {
        mLocked = cursor.getInt(WPcolumnsMap.mColumnWpmsLocked) != 0;
        }
        //gionee gaoj 2012-5-16 remove locked for CRCR00600687 end
//        mBody = mText + mURL + mCreate + mExpiration;
        
        Log.i(WP_TAG, toString());
    }

    //add for gemini
    public int getSimId() {
        return mSimId;
    }
    //add for multi-delete
/*    public boolean isSelected() {
        return mItemSelected;
    }
    public void setSelectedState(boolean isSelected) {
        mItemSelected = isSelected;
    }*/
    @Override
    public String toString() {
        //add for gemini
        if (FeatureOption.MTK_GEMINI_SUPPORT == true){
        return "type: " + mType +
            " sim: " + mSimId +               
            " text: " + mText +
            " url: " + mURL +
            " time: " + mTimestamp +         
            " address: " + mAddress +
            " contact: " + mContact +
            " create: " + mCreate +
            " expiration: " + mExpiration +
            " action: " + mAction;
        }else{
            return "type: " + mType +
            " text: " + mText +
            " url: " + mURL +
            " time: " + mTimestamp +         
            " address: " + mAddress +
            " contact: " + mContact +
            " create: " + mCreate +
            " expiration: " + mExpiration +
            " action: " + mAction;
        }
        
    }

}
