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

package com.aurora.mms.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import com.android.mms.R;
import com.android.mms.LogTag;
import com.android.mms.data.Conversation;
import com.android.mms.ui.MessageCursorAdapter;
import com.android.mms.ui.MessageItem;
import com.android.mms.ui.MessageListAdapter.ColumnsMap;
import android.provider.Telephony;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CursorAdapter;

import android.net.Uri;
import android.net.Uri.Builder;
//gionee gaoj 2012-3-22 added for CR00555790 start
import android.os.SystemProperties;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Directory;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;

import com.android.mms.MmsApp;
import java.util.ArrayList;
// Aurora liugj 2013-09-29 added for aurora's new feature start
import java.util.regex.Matcher;
// Aurora liugj 2013-09-29 added for aurora's new feature end
import java.util.regex.Pattern;
//gionee gaoj 2012-3-22 added for CR00555790 end
// gionee zhouyj 2012-07-31 add for CR00662942 start 
import com.android.mms.util.GnSelectionManager;
// gionee zhouyj 2012-07-31 add for CR00662942 end 

//gionee gaoj 2013-2-19 adde for CR00771935 start
import android.widget.ImageView;
import com.android.mms.data.Contact;
//gionee gaoj 2013-2-19 adde for CR00771935 end

// Aurora liugj 2013-09-20 created for aurora's new feature
/**
 * The back-end data adapter for MessageSearchList.
 */
// Aurora liugj 2013-09-29 modified for aurora's new feature start
public class MessageSearchListAdapter extends MessageCursorAdapter implements AbsListView.RecyclerListener, Contact.UpdateListener {
// Aurora liugj 2013-09-29 modified for aurora's new feature end
    private static final String TAG = "MessageSearchListAdapter";
    private static final int INDEX_COLUMN_ADDRESS = 2;
    private static final boolean LOCAL_LOGV = false;
    
    private int mResultLimit = Integer.MAX_VALUE;
    private final LayoutInflater mFactory;
    private OnContentChangedListener mOnContentChangedListener;
    // gionee zhouyj 2012-07-31 add for CR00662942 start 
    private GnSelectionManager<Long> mSelectionManager;
    // gionee zhouyj 2012-07-31 add for CR00662942 end 
    
    // Aurora liugj 2013-09-29 modified for aurora's new feature start
    private Context mContext;
    private String mQueryString;
    //private char[] mUpperCaseQueryString;
    private boolean mSearchMode = false;
    private HashMap<String, String> mNameMap = new HashMap<String, String>();
    // Aurora liugj 2013-09-29 modified for aurora's new feature end
    
    
    public MessageSearchListAdapter(Context context, Cursor cursor) {
        super(context, cursor, false /* auto-requery */);
        // Aurora liugj 2013-09-29 added for aurora's new feature start
        mContext = context;
        Contact.addListener(this);
        // Aurora liugj 2013-09-29 added for aurora's new feature end
        mFactory = LayoutInflater.from(context);
    }
    // Aurora xuyong 2014-04-17 modified for aurora's new feature start
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        if (mDataValid && mCursor != null && !mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position "
                    + position);
        }
        View v;
        if (convertView == null) {
            v = newView(mContext, mCursor, parent);
        } else {
            v = convertView;
        }
        bindView(v, mContext, mCursor);
        return v;
    }
    // Aurora xuyong 2014-04-17 modified for aurora's new feature end
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (!(view instanceof MessageSearchListItem)) {
            Log.e(TAG, "Unexpected bound view: " + view);
            return;
        }

        MessageSearchListItem headerView = (MessageSearchListItem) view;

        if (!mIsScrolling) {
            // Aurora liugj 2013-09-29 modified for aurora's new feature start
            //String address = cursor.getString(cursor.getColumnIndex("address"));
            String address = cursor.getString(INDEX_COLUMN_ADDRESS);
            String name = formatName(address, isNumeric(mQueryString));
            headerView.bind(context, cursor, getQueryString(), name);
            // Aurora liugj 2013-09-29 modified for aurora's new feature end
        } else {
            headerView.bindDefault();
        }
        
    }
    
    // Aurora liugj 2013-09-29 added for aurora's new feature start
    private String formatName(String address, boolean isFullNum) {
        StringBuffer contantName;
        // Aurora liugj 2013-10-18 added for aurora's new feature start
        if (mNameMap == null) {
            mNameMap =  new HashMap<String, String>();
        }
        // Aurora liugj 2013-10-18 added for aurora's new feature end
        if (mNameMap.containsKey(address)) {
            contantName = new StringBuffer(mNameMap.get(address));
        }else {
            if(!TextUtils.isEmpty(address)) {
                contantName = new StringBuffer(Contact.get(address, true).getName());
                if (isFullNum && !isNumeric(contantName.toString())) {
                    contantName.append(" "+address);
                }
            } else {
                contantName = new StringBuffer(mContext.getString(android.R.string.unknownName));
            }
            mNameMap.put(address, contantName.toString());
        }
        return contantName.toString();
    }
    
    // Aurora liugj 2013-10-08 modified for aurora's new feature start
    private boolean isNumeric(String str) {
        // Aurora liugj 2013-10-15 added for aurora's new feature start
        if (str == null) {
            return false;
        }
        if (str.startsWith("+")) {
            str = str.substring(1);
        }
        // Aurora liugj 2013-10-15 added for aurora's new feature end
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }
    // Aurora liugj 2013-10-08 modified for aurora's new feature end
   // Aurora liugj 2013-09-29 added for aurora's new feature end

    public void onMovedToScrapHeap(View view) {
        // Aurora liugj 2013-09-25 modified for aurora's new feature start
        if (view instanceof MessageSearchListItem) {
            MessageSearchListItem headerView = (MessageSearchListItem)view;
            headerView.unbind();
        }
        // Aurora liugj 2013-09-25 modified for aurora's new feature end
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        if (LOCAL_LOGV) Log.v(TAG, "inflating new view");
        // Aurora liugj 2014-01-17 modified for textview cut start
        return mFactory.inflate(R.layout.aurora_message_search_list_item, null, false);
        // Aurora liugj 2014-01-17 modified for textview cut end
    }

    public interface OnContentChangedListener {
        void onContentChanged(MessageSearchListAdapter adapter);
    }

    public void setOnContentChangedListener(OnContentChangedListener l) {
        mOnContentChangedListener = l;
    }
        
    // Aurora liugj 2013-10-08 modified for aurora's new feature start
    @Override
    protected void onContentChanged() {
        if (getCursor() != null && !getCursor().isClosed()) {
            if (mOnContentChangedListener != null) {
                mOnContentChangedListener.onContentChanged(this);
            }
        }
    }
    // Aurora liugj 2013-10-08 modified for aurora's new feature end
    
    public boolean isSearchMode() {
        return mSearchMode;
    }
    
    public void setSearchMode(boolean searchMode) {
        this.mSearchMode = searchMode;
    }
    
    public String getQueryString() {
        return mQueryString;
    }

    public void setQueryString(String queryString) {
        // Aurora liugj 2013-11-06 modified for fix bug-417 start 
        if (queryString == null) {
            mQueryString = "";
        }else {
            mQueryString = queryString;
            /*if (TextUtils.isEmpty(queryString)) {
                mUpperCaseQueryString = null;
            } else {
                mUpperCaseQueryString = queryString.toUpperCase().toCharArray();
            }*/
        }
        // Aurora liugj 2013-11-06 modified for fix bug-417 end
    }
    
    @Override
    public void notifyDataSetChanged() {
        // TODO Auto-generated method stub
        super.notifyDataSetChanged();
        Log.i(TAG, "[Performance test][Mms] loading data end time ["
            + System.currentTimeMillis() + "]" );
    }
       
    private int getResultLimit() {
        return mResultLimit;
    }
    
    public void setResultLimit(int limit) {
        this.mResultLimit = limit;
    }
    
    // Aurora liugj 2013-09-29 added for aurora's new feature start
    public void destroy() {
        Contact.removeListener(this);
        mNameMap.clear();
        mNameMap = null;
    }
    // Aurora liugj 2013-09-29 added for aurora's new feature end

    // Aurora liugj 2013-09-29 added for aurora's new feature start
    @Override
    public void onUpdate(Contact updated) {
        if (mNameMap.containsKey(updated.getNumber())) {
            mNameMap.remove(updated.getNumber());
        }
    }
    // Aurora liugj 2013-09-29 added for aurora's new feature end
}
