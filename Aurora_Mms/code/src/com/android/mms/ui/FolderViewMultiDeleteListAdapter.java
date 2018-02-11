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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import com.android.mms.R;
import com.android.mms.data.Conversation;
import com.android.mms.data.FolderView;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CursorAdapter;

public class FolderViewMultiDeleteListAdapter extends CursorAdapter implements AbsListView.RecyclerListener {
    private static final String TAG = "FolderViewMultiDeleteListAdapter";
    private final LayoutInflater mFactory;
    private OnContentChangedListener mOnContentChangedListener;
    private Map<Long, Boolean> mListItem;
    private Context mContext;
 
    private static final int CACHE_SIZE = 50;
    public FolderViewMultiDeleteListAdapter(Context context, Cursor cursor) {
        super(context, cursor, false /* auto-requery */);
        mFactory = LayoutInflater.from(context);
        mListItem = new HashMap<Long, Boolean>();
        mContext = context;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (!(view instanceof FolderViewMultiDeleteListItem)) {
            Log.e(TAG, "Unexpected bound view: " + view);       
            return;
        }       
        bindTreadView(view, context, cursor);
            
    }
    
    private void bindTreadView(View view, Context context, Cursor cursor) {
        Log.e("$ time stamp $", "before bindTreadView()");
        FolderViewMultiDeleteListItem headerView = (FolderViewMultiDeleteListItem) view;
        FolderView mfview = FolderView.from(context, cursor);
        FolderViewMultiDeleteListItemData ch = new FolderViewMultiDeleteListItemData(context, mfview);
        int  type = cursor.getInt(6);
        long msgId = cursor.getInt(0);  
        msgId = getKey(type, msgId);
        if (mListItem.get(msgId) == null) {
            mListItem.put(msgId, false);
        } else {
            ch.setSelectedState(mListItem.get(msgId));
        }
        
        headerView.bind(context, ch);      
    }
   
    public void changeSelectedState(long listId) {
        mListItem.put(listId, !mListItem.get(listId));
        
    }
    
    public  Map<Long, Boolean> getItemList() {
        return mListItem;
        
    }
    
    public void initListMap(Cursor cursor) {
        if (cursor != null) {
            long itemId = 0;
            int type;
            long msgId = 0;
            while (cursor.moveToNext()) {
                type = cursor.getInt(6);
                msgId = cursor.getInt(0);           
                itemId = getKey(type, msgId);             
                if (mListItem.get(itemId) == null) {
                    mListItem.put(itemId, false);
                }
            }
        }  
    }
    
     public static long getKey(int type, long id) {
        if (type == 2) {//mms
            return -id;
        } else if(type == 1){
            return id;
        }else if(type == 3){
            return 100000+id;
        }else{//cb
            return -(100000+id);
        }
    }
    
    public void setItemsValue(boolean value, long[] keyArray) {       
        Iterator iter = mListItem.entrySet().iterator();
        //keyArray = null means set the all item
        if (keyArray == null) {
            while (iter.hasNext()) {
                @SuppressWarnings("unchecked")
                Map.Entry<Long, Boolean> entry = (Entry<Long, Boolean>) iter.next();
                entry.setValue(value);
            }
        } else {
            for (int i = 0; i < keyArray.length; i++) {
                mListItem.put(keyArray[i], value);
            }
        }               
    }
    
    public int getSelectedNumber() {
        Iterator iter = mListItem.entrySet().iterator();
        int number = 0;
        while (iter.hasNext()) {
            @SuppressWarnings("unchecked")
            Map.Entry<Long, Boolean> entry = (Entry<Long, Boolean>) iter.next();
            if (entry.getValue()) {
                number++;
            }
        }
        return number;
    }
    
    public boolean isAllSelected() {
        return getSelectedNumber() == mListItem.size();
    }
    
    
    public void onMovedToScrapHeap(View view) {
        FolderViewMultiDeleteListItem headerView = (FolderViewMultiDeleteListItem)view;
        headerView.unbind();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {       
        return mFactory.inflate(R.layout.folderview_multi_delete_item, parent, false);
    }

    public interface OnContentChangedListener {
        void onContentChanged(FolderViewMultiDeleteListAdapter adapter);
    }

    public void setOnContentChangedListener(OnContentChangedListener l) {
        mOnContentChangedListener = l;
    }
    
    @Override
    protected void onContentChanged() {
        if (mCursor != null && !mCursor.isClosed()) {
            if (mOnContentChangedListener != null) {
                mOnContentChangedListener.onContentChanged(this);
            }
        }
    }
}
