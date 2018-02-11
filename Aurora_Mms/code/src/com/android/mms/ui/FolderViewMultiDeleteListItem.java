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
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract.Intents;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.LineHeightSpan;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.QuickContactBadge;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.provider.Telephony.Threads;

/**
 * This class manages the view for given conversation.
 */
public class FolderViewMultiDeleteListItem extends RelativeLayout implements Contact.UpdateListener{
    private static final String TAG = "FolderViewMultiDeleteListItem";
    private TextView mSubjectView;
    private TextView mFromView;
    private TextView mDateView;
    private ImageView mAvatarView;
    private CheckBox mCheckbox;
    private Context mContext;
    // For posting UI update Runnables from other threads:
    private Handler mHandler = new Handler();
    private FolderViewMultiDeleteListItemData mMultiDeleteThreadData;

    public FolderViewMultiDeleteListItem(Context context) {
        super(context);
        mContext = context;

    }

    public FolderViewMultiDeleteListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mFromView    = (TextView) findViewById(R.id.from);
        mSubjectView = (TextView) findViewById(R.id.subject);
        mDateView    = (TextView) findViewById(R.id.date);  
        mAvatarView  = (ImageView) findViewById(R.id.avatar);
        mCheckbox    = (CheckBox) findViewById(R.id.CheckBox01);
        
    }

    public FolderViewMultiDeleteListItemData getMultiDeleteHeader() {
        return mMultiDeleteThreadData;
    }   
    
    private void setMultiDeleteHeader(FolderViewMultiDeleteListItemData header) {
        mMultiDeleteThreadData = header;
    }
    
    private String formatMessage(FolderViewMultiDeleteListItemData ch) {
        String from = ch.getFrom();
        if (TextUtils.isEmpty(from)){
            from = mContext.getString(android.R.string.unknownName);
        }
        return from;
    }


    
    public void clickListItem() {
        if (mCheckbox.isChecked()) {
            //mCheckbox.setChecked(false);
            setSelectedBackGroud(false);
        } else {
            //mCheckbox.setChecked(true);
            setSelectedBackGroud(true);
        }
        
    }
    
    
    public final void bind(Context context, final FolderViewMultiDeleteListItemData ch) {
        setMultiDeleteHeader(ch);      
        boolean hasError = ch.hasError();
        // Date
        if(FolderViewMultiDeleteActivity.viewid == FolderViewList.OPTION_OUTBOX && !hasError){
            mDateView.setText(R.string.sending_message);
        }else{
            mDateView.setText(ch.getDate());
        }
        // From.
        mFromView.setText(formatMessage(ch));


        // Subject
        mSubjectView.setText(ch.getSubject());
        mSubjectView.setSingleLine(true);
        mSubjectView.setEllipsize(TextUtils.TruncateAt.END);
       /* if(ch.getType() == 1){
            mAvatarView.setImageResource(R.drawable.ic_sms);
        }else if(ch.getType() == 2){
            mAvatarView.setImageResource(R.drawable.ic_mms);
        }else if(ch.getType() == 3){
            mAvatarView.setImageResource(R.drawable.ic_wappush);
        }else if(ch.getType() == 4){
            mAvatarView.setImageResource(R.drawable.ic_cellbroadcast);
        }*/
        //mCheckbox.setChecked(ch.isSelected());
        setSelectedBackGroud(ch.isSelected());
    }  

    public void setSelectedBackGroud(boolean selected) {
        if (selected) {
            mCheckbox.setChecked(true);
            //setBackgroundResource(R.drawable.list_selected_holo_light);
        } else {
            mCheckbox.setChecked(false);
            setBackgroundDrawable(null);
        }
    }

    public final void unbind() {
     //   Contact.removeListener(this);
    }

    @Override
    public void onUpdate(Contact updated) {
        updateFromView();
    }
    
    private void updateFromView() {
       // mFromView.setText(formatMessage());
    }
    
}
