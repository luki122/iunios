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

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.BaseColumns;
import gionee.provider.GnTelephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.MmsSms.PendingMessages;
import gionee.provider.GnTelephony.Sms;
import android.provider.Telephony.Sms.Conversations;
import android.util.Log;
import android.util.LruCache;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CursorAdapter;
import aurora.widget.AuroraListView;
import com.android.mms.R;
// Aurora xuyong 2013-12-11 added for aurora's new feature start
import com.android.mms.model.GroupItemInfoModel;
// Aurora xuyong 2013-12-11 added for aurora's new feature end
import com.android.mms.ui.MessageCursorAdapter;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.MmsException;
//Aurora xuyong 2013-11-15 modified for google adapt end

import java.util.regex.Pattern;

//a0
import android.content.ContentUris;
import android.net.Uri;
import android.telephony.SmsManager;
import android.view.View.OnCreateContextMenuListener;
// Aurora xuyong 2013-12-11 added for aurora's new feature start
import java.util.ArrayList;
// Aurora xuyong 2013-12-11 added for aurora's new feature end
//aurora yudingmin 2014-08-22 added for bug #7671 start
import java.util.Comparator;
//aurora yudingmin 2014-08-22 added for bug #7671 end
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;
//aurora yudingmin 2014-08-22 added for bug #7671 start
import java.util.TreeMap;
//aurora yudingmin 2014-08-22 added for bug #7671 end
// add for gemini
import com.aurora.featureoption.FeatureOption;
// Aurora xuyong 2014-04-29 added for aurora's new feature start
// Aurora xuyong 2015-12-29 added for aurora 2.0 new feature start
import com.aurora.mms.ui.AuroraExpandableTextView;
// Aurora xuyong 2015-12-29 added for aurora 2.0 new feature end
import com.aurora.mms.ui.ThumbnailWorker;
// Aurora xuyong 2014-04-29 added for aurora's new feature end
//a1

//gionee gaoj 2012-4-10 added for CR00555790 start
import java.util.HashSet;
import com.android.mms.MmsApp;
//gionee gaoj 2012-4-10 added for CR00555790 end

//gionee gaoj 2013-2-19 adde for CR00771935 start
import com.android.mms.data.Contact;
import android.widget.ImageView;
// Aurora xuyong 2016-01-19 added for aurora 2.0 new feature start
import android.widget.TextView;
// Aurora xuyong 2016-01-19 added for aurora 2.0 new feature end
//gionee gaoj 2013-2-19 adde for CR00771935 end
// Aurora xuyogn 2016-01-04 added for aurora's new feature start
import aurora.widget.AuroraActionBar;
// Aurora xuyogn 2016-01-04 added for aurora's new feature start
/**
 * The back-end data adapter of a message list.
 */
public class MessageListAdapter extends MessageCursorAdapter {
    private static final String TAG = "MessageListAdapter";
    public static final String CACHE_TAG = "Mms/MessageItemCache";
    private static final boolean LOCAL_LOGV = false;
    static final String[] PROJECTION = new String[] {
        // TODO: should move this symbol into com.android.mms.telephony.Telephony.
        MmsSms.TYPE_DISCRIMINATOR_COLUMN,
        BaseColumns._ID,
        Conversations.THREAD_ID,
        // For SMS
        Sms.ADDRESS,
        Sms.BODY,
        Sms.DATE,
        Sms.DATE_SENT,
        Sms.READ,
        Sms.TYPE,
        Sms.STATUS,
        Sms.LOCKED,
        Sms.ERROR_CODE,
        // For MMS
        Mms.SUBJECT,
        Mms.SUBJECT_CHARSET,
        Mms.DATE,
        Mms.DATE_SENT,
        Mms.READ,
        Mms.MESSAGE_TYPE,
        Mms.MESSAGE_BOX,
        Mms.DELIVERY_REPORT,
        Mms.READ_REPORT,
        PendingMessages.ERROR_TYPE,
        Mms.LOCKED,
        //a0
        Sms.SIM_ID,        
        Mms.SIM_ID,
        Sms.SERVICE_CENTER,
        Mms.SERVICE_CENTER,
        //gionee gaoj 2012-4-10 added for CR00555790 start
        "star",
        // Aurora xuyong 2014-10-23 modified for privacy feature start
        "star",
        //gionee gaoj 2012-4-10 added for CR00555790 end
        //a1
        "is_privacy",
        // Aurora xuyong 2015-12-29 modified for aurora 2.0 new feature start
        "weather_info",
        // Aurora xuyong 2016-03-05 added for bug #20677 start
        "fold",
        // Aurora xuyong 2016-03-05 added for bug #20677 end
        "fold"
        // Aurora xuyong 2015-12-29 modified for aurora 2.0 new feature end
        // Aurora xuyong 2014-10-23 modified for privacy feature end
    };

    // The indexes of the default columns which must be consistent
    // with above PROJECTION.
    static final int COLUMN_MSG_TYPE            = 0;
    static final int COLUMN_ID                  = 1;
    static final int COLUMN_THREAD_ID           = 2;
    static final int COLUMN_SMS_ADDRESS         = 3;
    static final int COLUMN_SMS_BODY            = 4;
    static final int COLUMN_SMS_DATE            = 5;
    static final int COLUMN_SMS_DATE_SENT       = 6;
    static final int COLUMN_SMS_READ            = 7;
    static final int COLUMN_SMS_TYPE            = 8;
    static final int COLUMN_SMS_STATUS          = 9;
    static final int COLUMN_SMS_LOCKED          = 10;
    static final int COLUMN_SMS_ERROR_CODE      = 11;
    static final int COLUMN_MMS_SUBJECT         = 12;
    static final int COLUMN_MMS_SUBJECT_CHARSET = 13;
    static final int COLUMN_MMS_DATE            = 14;
    static final int COLUMN_MMS_DATE_SENT       = 15;
    static final int COLUMN_MMS_READ            = 16;
    static final int COLUMN_MMS_MESSAGE_TYPE    = 17;
    static final int COLUMN_MMS_MESSAGE_BOX     = 18;
    static final int COLUMN_MMS_DELIVERY_REPORT = 19;
    static final int COLUMN_MMS_READ_REPORT     = 20;
    static final int COLUMN_MMS_ERROR_TYPE      = 21;
    static final int COLUMN_MMS_LOCKED          = 22;
    //a0
    static final int COLUMN_SMS_SIMID           = 23;
    static final int COLUMN_MMS_SIMID           = 24;
    static final int COLUMN_SMS_SERVICE_CENTER  = 25;
    static final int COLUMN_MMS_SERVICE_CENTER  = 26;
    //a1

    //gionee gaoj 2012-4-10 added for CR00555790 start
    static final int COLUMN_SMS_STAR       = 27;
    static final int COLUMN_MMS_STAR       = 28;
    // Aurora xuyong 2014-10-23 added for privacy feature start
    static final int COLUMN_IS_PRIVACY     = 29;
    static final int COLUMN_WEATHER_INFO   = 30;
    // Aurora xuyong 2015-12-29 added for aurora 2.0 new feature start
    // Aurora xuyong 2016-03-05 modified for bug #20677 start
    static final int COLUMN_SMS_FOLDED         = 31;
    static final int COLUMN_MMS_FOLDED         = 32;
    // Aurora xuyong 2016-03-05 modified for bug #20677 end
    // Aurora xuyong 2015-12-29 added for aurora 2.0 new feature end
    // Aurora xuyong 2014-10-23 added for privacy feature end
    private Map<Long, Boolean> mMsgStared;
    //gionee gaoj 2012-4-10 added for CR00555790 end
    private static final int CACHE_SIZE         = 500;

    public static final int INCOMING_ITEM_TYPE = 0;
    public static final int OUTGOING_ITEM_TYPE = 1;
    // Aurora xuyong 2013-12-11 added for aurora's new feature start
    ArrayList<GroupItemInfoModel> mList;
    // Aurora xuyong 2013-12-11 added for aurora's new feature end

    protected LayoutInflater mInflater;
    private final LruCache<Long, MessageItem> mMessageItemCache;
    private final ColumnsMap mColumnsMap;
    private OnDataSetChangedListener mOnDataSetChangedListener;
    private Handler mMsgListItemHandler;
    private Pattern mHighlight;
    private Context mContext;

    public MessageListAdapter(
            Context context, Cursor c, AuroraListView listView,
            boolean useDefaultColumnsMap, Pattern highlight) {
        super(context, c, FLAG_REGISTER_CONTENT_OBSERVER);
        mContext = context;
        mHighlight = highlight;

        mInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mMessageItemCache = new LruCache<Long, MessageItem>(CACHE_SIZE);
        
        //a0
        mListItem = new HashMap<Long, Boolean>();
        //a1

        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (MmsApp.mGnMessageSupport) {

            mMsgStared = new HashMap<Long, Boolean>();
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
        if (useDefaultColumnsMap) {
            mColumnsMap = new ColumnsMap();
        } else {
            mColumnsMap = new ColumnsMap(c);
        }

        listView.setRecyclerListener(new AbsListView.RecyclerListener() {
            @Override
            public void onMovedToScrapHeap(View view) {
                if (view instanceof MessageListItem) {
                    MessageListItem mli = (MessageListItem) view;
                    // Clear references to resources
                    mli.unbind();
                }
            }
        });
    }
    // Aurora xuyong 2013-12-11 added for aurora's new feature start
    //aurora yudingmin 2014-08-22 modified for bug #7671 start
    /**
     * this method must run in main thread
     */
    // Aurora xuyong 2015-04-25 modified for bug #13301 start
    public void  changeData(ArrayList<GroupItemInfoModel> list, Cursor cursor) {
    // Aurora xuyong 2015-04-25 modified for bug #13301 end
        if(mList != null)
        {
            mList.clear();
        }
        mList = list;
        // Aurora xuyong 2015-04-25 modified for bug #13301 start
        initWeatherInfoList(cursor);
        changeCursor(cursor);
        // Aurora xuyong 2015-12-29 added for aurora 2.0 new feature start
        AuroraExpandableTextView.clearCollapsedStatus();
        // Aurora xuyong 2015-12-29 added for aurora 2.0 new feature end
        // Aurora xuyong 2015-04-25 modified for bug #13301 end
    }
    //aurora yudingmin 2014-08-22 modified for bug #7671 end
    // Aurora xuyong 2015-04-25 added for bug #13301 start
    private ArrayList<String> mWeatherInfoList = new ArrayList<String>();
    
    public void initWeatherInfoList(Cursor cursor) {
        mWeatherInfoList.clear();
        if (cursor != null) {
            String weatherInfo = null;
            cursor.moveToPosition(-1);
            int i = 0;
            while (cursor.moveToNext()) {
                weatherInfo = cursor.getString(mColumnsMap.mWeatherInfo);
                if (weatherInfo != null || i == 0) {
                	mWeatherInfoList.add(weatherInfo);
                } else {
                	mWeatherInfoList.add(mWeatherInfoList.get(i - 1 < 0 ? 0 : i - 1));
                }
                i++;
            }
        }
    }
    // Aurora xuyong 2015-04-25 added for bug #13301 end
    
    private boolean mNeedShowWeatherInfo;
    public void setNeedShowWeatherInfo(boolean need) {
        mNeedShowWeatherInfo = need;
    }
    
    // Aurora xuyong 2014-01-07 added for aurora's new feature start
    public int getInCursorPosition(int position) {
        //aurora yudingmin 2014-08-21 added for bug #7738 start
        if(mList == null) {
            return position;
        }
        //aurora yudingmin 2014-08-21 added for bug #7738 end
       // Aurora xuyong 2014-08-19 modified for bug #7739 start
        if (position < 0) {
            return -1;
        }
        int pos = 0;
       // Aurora xuyong 2014-08-19 modified for bug #7739 end
        for (int i = 0; i < position; i++) {
          // Aurora xuyong 2014-08-16 modified for aurora's new feature start
            if (mList != null && i < mList.size() && mList.get(i) != null ) {
                pos += mList.get(i).getIds().size();
            }
          // Aurora xuyong 2014-08-16 modified for aurora's new feature end
        }
        return pos;
    }
    // Aurora xuyong 2014-01-07 added for aurora's new feature end
    
    private int getCurrentCursorPositons(int position) {
        //aurora yudingmin 2014-08-21 added for bug #7738 start
        if(mList == null) {
            return position;
        }
        //aurora yudingmin 2014-08-21 added for bug #7738 end
        int inListPosition = position;
        // Aurora xuyong 2014-05-27 modified for bug #4840 start
        synchronized(mList) {
            //aurora yudingmin 2014-08-21 modified for bug #7738 start
            int max = mList.size();
            int min = 0;
            int half = max/2;
            while(max - min >= 4) {
                    int indexCur = mList.get(half).getPositions();
                    int indexNext = mList.get(half + 1).getPositions();
                    if(inListPosition > indexNext) {
                        min = half;
                        half = min + (max - min)/2;
                    } else if(inListPosition < indexCur) {
                        max = half;
                        half = min + (max - min)/2;
                    } else {
                        if (indexCur == indexNext || inListPosition >= indexCur && inListPosition < indexNext) {
                            return half;
                        } else {
                            return half+1;
                        }
                    }
            }
          // Aurora xuyong 2014-08-22 modified for bug #7739 & #7853 start
            for (int i = min; i <= max; i++) {
                int indexCur = mList.get(i).getPositions();
                int indexNext = mList.get(i+1 >=  mList.size() ? ( mList.size() -1) : (i + 1)).getPositions();
                if (inListPosition >= indexCur && inListPosition <= indexNext) {
                    if (indexCur == indexNext || (inListPosition >= indexCur && inListPosition < indexNext)) {
          // Aurora xuyong 2014-08-22 modified for bug #7739 & #7853 end
                        return i;
                    } else {
                        return i+1;
                    }
                }
            } 
        }
        //aurora yudingmin 2014-08-21 modified for bug #7738 end
        // Aurora xuyong 2014-05-27 modified for bug #4840 end
        return -1;
    }
    // Aurora xuyong 2013-12-11 added for aurora's new feature end

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Aurora xuyong 2014-01-03 added for bug #1713 start 
        if (cursor == null) {
            return;
        }
        // Aurora xuyong 2014-02-11 added for bug #1923 start
        int positionInParent = getCurrentCursorPositons(cursor.getPosition());
        // Aurora xuyong 2014-02-11 added for bug #1923 end
        // Aurora xuyong 2014-01-03 added for bug #1713 end
        Log.d(CACHE_TAG, "bindView() start.");
        if (view instanceof MessageListItem) {
            if (!mIsScrolling || mIsDeleteMode) {
                String type = cursor.getString(mColumnsMap.mColumnMsgType);
                long msgId = cursor.getLong(mColumnsMap.mColumnMsgId);
                Log.d(CACHE_TAG, "bindView(): type=" + type + ", msgId=" + msgId);
                MessageItem msgItem = getCachedMessageItem(type, msgId, cursor);
                if (msgItem != null) {
                    MessageListItem mli = (MessageListItem) view;
                    mli.setNeedShowWeatherInfo(mNeedShowWeatherInfo);
                    // Aurora xuyong 2014-02-11 added for bug #1923 start
                    mli.setPositionInParent(positionInParent);
                    // Aurora xuyong 2014-02-11 added for bug #1923 end
                    // Aurora xuyong 2014-01-03 added for aurora;s new feature start
                    mli.initAttachChildViewVisibility();
                    // Aurora xuyong 2014-01-03 added for aurora;s new feature end
                    //a0
                    //for multi-delete
                    if (mIsDeleteMode) {
                         msgId = getKey(type, msgId);
                         if (mListItem.get(msgId) == null) {
                            mListItem.put(msgId, false);
                         } else {
                            msgItem.setSelectedState(mListItem.get(msgId));
                         }
                         // Aurora xuyong 2014-08-22 added for bug #7739 & #7853 start
                         if (mSelectedMap.get(getInCursorPosition(positionInParent)) == null) {
                             mSelectedMap.put(getInCursorPosition(positionInParent), false);
                         }
                    } else {
                        msgItem.setSelectedState(false);
                       // Aurora xuyong 2014-08-22 added for bug #7739 & #7853 end
                    }
//MTK_OP01_PROTECT_START
                    // add for text zoom
//                    String optr = SystemProperties.get("ro.operator.optr");
//                    if (optr != null && optr.equals("OP01")) {
//                        mli.setTextSize(mTextSize);
//                    }
//MTK_OP01_PROTECT_END
                    //a1
                    //gionee gaoj 2012-8-14 added for CR00623375 start
                        //Gionee <gaoj> <2013-4-11> added for CR00796538 start
                        if (MmsApp.mGnRegularlyMsgSend || MmsApp.mDisplaySendTime) {
                            //Gionee <gaoj> <2013-4-11> added for CR00796538 end
                        long status = msgItem.getMsgStatus();
                        if (MmsApp.mGnMessageSupport && status != SmsManager.STATUS_ON_ICC_READ
                                && status != SmsManager.STATUS_ON_ICC_UNREAD
                                && status != SmsManager.STATUS_ON_ICC_SENT
                                && status != SmsManager.STATUS_ON_ICC_UNSENT ) {
                            if (cursor.getPosition() >= 1) {
                                Cursor lastMsgCursor = (Cursor) getItem(getCurrentCursorPositons(cursor.getPosition()) - 1);
                                if (lastMsgCursor != null && isCursorValid(lastMsgCursor)) {
                                    long nextMsgId = lastMsgCursor.getLong(mColumnsMap.mColumnMsgId);
                                    String lastType = lastMsgCursor.getString(mColumnsMap.mColumnMsgType);
                                    MessageItem nextMsgItem = getCachedMessageItem(lastType, nextMsgId, cursor);
                                    if (nextMsgItem != null) {
                                        // Aurora xuyong 2013-10-18 modified for bug #47 start
                                        mli.setLastTime(nextMsgItem.mAuroraDate);
                                        // Aurora xuyong 2015-04-23 added for aurora's new feature start
                                        // Aurora xuyong 2015-04-25 modified for bug #13301 start
                                        // Aurora xuyong 2015-04-27 modified for bug #13301 start
                                        int lastPos = getCurrentCursorPositons(lastMsgCursor.getPosition());
                                        // Aurora xuyong 2015-04-27 modified for bug #13301 end
                                        mli.setLastWeatherInfo(mWeatherInfoList.get(lastPos < 0 ? 0 : lastPos));
                                        //mli.setLastWeatherInfo(nextMsgItem.mWeatherInfo);
                                        // Aurora xuyong 2015-04-25 modified for bug #13301 end
                                        // Aurora xuyong 2015-04-23 added for aurora's new feature end
                                        // Aurora xuyong 2013-10-18 modified for bug #47 end
                                        //gionee gaoj 2012-8-22 added for CR00679009 start
                                        //Gionee <gaoj> <2013-4-11> added for CR00796538 start
                                        boolean failed = nextMsgItem.isOutgoingMessage() && nextMsgItem.isFailedMessage();
                                        /*|| null != nextMsgItem.mSendTimestamp;*/
                                        //Gionee <gaoj> <2013-4-11> added for CR00796538 end
                                        mli.setLastMsgFailed(failed);
                                        //gionee gaoj 2012-8-22 added for CR00679009 end
                                    }
                                }
                                //gionee gaoj 2012-8-20 added for CR00678311 start
                                cursor.moveToNext();
                                //gionee gaoj 2012-8-20 added for CR00678311 end
                            } else {
                                // Aurora xuyong 2013-10-18 modified for bug #47 start
                                mli.setLastTime(-1);
                                // Aurora xuyong 2015-04-23 added for aurora's new feature start
                                mli.setLastWeatherInfo(null);
                                // Aurora xuyong 2015-04-23 added for aurora's new feature end
                                // Aurora xuyong 2013-10-18 modified for bug #47 end
                            }
                        }
                        }
                    //gionee gaoj 2012-8-14 added for CR00623375 end

                     // gionee lwzh modify for CR00774362 20130227 begin
                    if (cursor.getPosition() == cursor.getCount() - 1) {
                        //gionee gaoj 2013-2-19 adde for CR00771935 start
                        ImageView photoView = mli.getQuickContact();
                        // Aurora xuyong 2014-10-23 modified for privacy feature start
                        String name = null;
                        if (MmsApp.sHasPrivacyFeature) {
                            name = Contact.get(msgItem.mAddress, false, msgItem.mPrivacy).getName();
                        } else {
                            name = Contact.get(msgItem.mAddress, false).getName();
                        }
                        // Aurora xuyong 2014-10-23 modified for privacy feature end
                        Contact.setContactPhotoViewTag(photoView, 
                                name, cursor.getPosition(), false);
                        //gionee gaoj 2013-2-19 adde for CR00771935 end
                    }
                    // gionee lwzh modify for CR00774362 20130227 end
                    mli.setMsgListItemHandler(mMsgListItemHandler);
                    mli.bind(msgItem, cursor.getPosition() == cursor.getCount() - 1, mIsDeleteMode);
                // Aurora xuyong 2014-04-28 modified for aurora's new feature start
                } /*else {
                    MessageListItem mli = (MessageListItem) view;
                    // Aurora xuyong 2014-02-11 added for bug #1923 start
                    mli.setPositionInParent(positionInParent);
                    // Aurora xuyong 2014-02-11 added for bug #1923 end
                    // Aurora xuyong 2014-01-03 added for aurora;s new feature start
                    mli.initAttachChildViewVisibility();
                    // Aurora xuyong 2014-01-03 added for aurora;s new feature end
                    // MTK_OP01_PROTECT_START
                    // add for text zoom
                    String optr = SystemProperties.get("ro.operator.optr");
                    if (optr != null && optr.equals("OP01")) {
                        mli.setTextSize(mTextSize);
                    }
                    // MTK_OP01_PROTECT_END
                    mli.bindDefault(cursor.getPosition() == cursor.getCount() - 1);
                }*/
            } /*else {
                MessageListItem mli = (MessageListItem) view;
                // Aurora xuyong 2014-02-11 added for bug #1923 start
                mli.setPositionInParent(positionInParent);
                // Aurora xuyong 2014-02-11 added for bug #1923 end
                // Aurora xuyong 2014-01-03 added for aurora;s new feature start
                mli.initAttachChildViewVisibility();
                // Aurora xuyong 2014-01-03 added for aurora;s new feature end
                //Gionee <gaoj> <2013-05-13> modified for CR00811367 begin
//MTK_OP01_PROTECT_START
                // add for text zoom
                /*String optr = SystemProperties.get("ro.operator.optr");
                if (optr != null && optr.equals("OP01")) {
                    mli.setTextSize(mTextSize);
                }*/
//MTK_OP01_PROTECT_END
                //Gionee <gaoj> <2013-05-13> modified for CR00811367 end
               /* mli.bindDefault(cursor.getPosition() == cursor.getCount() - 1);
            }*/
            // Aurora xuyong 2014-04-28 modified for aurora's new feature end
        }
    }

    public interface OnDataSetChangedListener {
        void onDataSetChanged(MessageListAdapter adapter);
        void onContentChanged(MessageListAdapter adapter);
    }

    public void setOnDataSetChangedListener(OnDataSetChangedListener l) {
        mOnDataSetChangedListener = l;
    }

    public void setMsgListItemHandler(Handler handler) {
        mMsgListItemHandler = handler;
    }

    private boolean mClearCacheFlag = true;
    public void setClearCacheFlag(boolean clearCacheFlag) {
        mClearCacheFlag = clearCacheFlag;
    }
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (LOCAL_LOGV) {
            Log.v(TAG, "MessageListAdapter.notifyDataSetChanged().");
        }

        if (mClearCacheFlag) {
            mMessageItemCache.evictAll();
        }
        mClearCacheFlag = true;

        if (mOnDataSetChangedListener != null) {
            mOnDataSetChangedListener.onDataSetChanged(this);
        }
    }

    @Override
    protected void onContentChanged() {
        if (getCursor() != null && !getCursor().isClosed()) {
            if (mOnDataSetChangedListener != null) {
                mOnDataSetChangedListener.onContentChanged(this);
            }
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (MmsApp.mGnMessageSupport) {
            // gionee lwzh add for CR00633086 20120702 begin
            return mInflater.inflate(getItemViewType(cursor) == INCOMING_ITEM_TYPE ?
                R.layout.gn_message_list_item_recv : R.layout.gn_message_list_item_send,
                parent, false);
            // return mInflater.inflate(R.layout.gn_message_list_item, parent, false);
            // gionee lwzh add for CR00633086 20120702 end
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
        return mInflater.inflate(getItemViewType(cursor) == INCOMING_ITEM_TYPE ?
                R.layout.message_list_item_recv : R.layout.message_list_item_send,
                parent, false);
    }

    static final int MSG_LIST_NEED_REFRASH   = 3;
    Object object = new Object();
    public MessageItem getCachedMessageItem(String type, long msgId, Cursor c) {
        final long key = getKey(type, msgId);
        MessageItem item = mMessageItemCache.get(key);
        Log.d(CACHE_TAG, "getCachedMessageItem(): key=" + key + ", item is in cache?=" + (item != null));
        if (item == null && c != null && isCursorValid(c)) {
            if (type.equals("mms")) {
                Log.d(CACHE_TAG, "getCachedMessageItem(): no cache, create one MessageItem on background.");
                final int boxId = c.getInt(mColumnsMap.mColumnMmsMessageBox);
                final int messageType = c.getInt(mColumnsMap.mColumnMmsMessageType);
                final int simId = MmsApp.mGnMultiSimMessage ? c.getInt(mColumnsMap.mColumnSmsSimId) : -1; //TODO need to modifiy any more? Gionee 20130107 by guoyx
                final int errorType = c.getInt(mColumnsMap.mColumnMmsErrorType);
                final int locked = c.getInt(mColumnsMap.mColumnMmsLocked);
                final int charset = c.getInt(mColumnsMap.mColumnMmsSubjectCharset);
                final long mMsgId = msgId;
                final String mmsType = type;
                final String subject = c.getString(mColumnsMap.mColumnMmsSubject);
                final String serviceCenter = c.getString(mColumnsMap.mColumnSmsServiceCenter);
                final String deliveryReport = c.getString(mColumnsMap.mColumnMmsDeliveryReport);
                final String readReport = c.getString(mColumnsMap.mColumnMmsReadReport);
                final Pattern highlight = mHighlight;
                // Aurora xuyong 2016-03-05 added for bug #20677 start
                final int folded = c.getInt(mColumnsMap.mColumnMmsFolded);
                // Aurora xuyong 2016-03-05 added for bug #20677 end
                // Aurora xuyong 2013-12-11 added for aurora's new feature start
                //aurora yudingmin 2014-08-21 modified for bug #7738 start
                final GroupItemInfoModel model;
                if(mList == null) {
                    model = new GroupItemInfoModel();
                    int preMessageDate = c.getInt(MessageListAdapter.COLUMN_SMS_DATE);
                    int preMessageId = c.getInt(MessageListAdapter.COLUMN_ID);
                  // Aurora xuyong 2014-06-09 modified for aurora's new feature start
                    int preMessageStatus = c.getInt(MessageListAdapter.COLUMN_SMS_STATUS);
                  // Aurora xuyong 2014-06-09 modified for aurora's new feature end
                    String preAddress = c.getString(MessageListAdapter.COLUMN_SMS_ADDRESS);
                    model.setPositions(c.getPosition());
                    model.addIds(new Long(preMessageId));
                    model.addStatus(preMessageStatus);
                    model.addAddress(preAddress);
                    model.setNums(1);
                } else {
                    model = mList.get(getCurrentCursorPositons(c.getPosition()));
                }
                //aurora yudingmin 2014-08-21 modified for bug #7738 end
                // Aurora xuyong 2013-12-11 added for aurora's new feature end
                final Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        Log.d(CACHE_TAG, "getCachedMessageItem(): call UI thread notify data set change.");
                        final Message msg = Message.obtain(mMsgListItemHandler, MSG_LIST_NEED_REFRASH);
                        msg.sendToTarget();
                    }
                };
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            MessageItem backgroundItem = new MessageItem(mContext, boxId, messageType, simId,
                                    errorType, locked, charset, mMsgId, mmsType, subject,
                            // Aurora xuyong 2016-03-05 modified for bug #20677 start
                                    serviceCenter, deliveryReport, readReport, highlight, folded);
                            // Aurora xuyong 2016-03-05 modified for bug #20677 end
                            Log.d(CACHE_TAG, "getCachedMessageItem(): put new MessageItem into cache, messageId = -" + backgroundItem.mMsgId);
                            mMessageItemCache.put(key, backgroundItem);
                            mMsgListItemHandler.postDelayed(r, 200);
                            // Aurora xuyong 2014-04-28 modified for aurora's new feature start
                            /*synchronized (object) {
                                object.notifyAll();
                            }*/
                            // Aurora xuyong 2014-04-28 modified for aurora's new feature end
                        } catch (MmsException e) {
                            Log.e(TAG, "getCachedMessageItem: ", e);
                        }
                    }
                });
                t.start();
                // Aurora xuyong 2014-04-28 modified for aurora's new feature start
                //synchronized (object) {
                   // try {
                        //Gionee <gaoj> <2013-05-13> modified for CR00811367 begin
                        /*int waitTime = 2000;*/
                     //   int waitTime = 0;
                     //   if (MmsApp.mGnPerfList) {
                     //       waitTime = 500;
                     //   } else {
                     //       waitTime = 2000;
                     //   }
                        //Gionee <gaoj> <2013-05-13> modified for CR00811367 end
                    //    object.wait(waitTime);
                   // } catch (InterruptedException ex) {
                        // do nothing
                   // }
                //}
                // Aurora xuyong 2014-04-28 modified for aurora's new feature end
                item = mMessageItemCache.get(key);
                if (item != null) {
                    Log.d(CACHE_TAG, "getCachedMessageItem(): get item during wait.");
                    Log.d(CACHE_TAG, "getCachedMessageItem(): cancel UI thread notify data set change.");
                    mMsgListItemHandler.removeCallbacks(r);
                }
            } else {
                int count = c.getColumnCount();
                // Aurora xuyong 2013-12-11 modified for aurora's new feature start
                int cPos = c.getPosition();
                //aurora yudingmin 2014-08-21 modified for bug #7738 start
                GroupItemInfoModel model;
                if(mList == null) {
                    model = new GroupItemInfoModel();
                    int preMessageDate = c.getInt(MessageListAdapter.COLUMN_SMS_DATE);
                    int preMessageId = c.getInt(MessageListAdapter.COLUMN_ID);
                  // Aurora xuyong 2014-06-09 modified for aurora's new feature start
                    int preMessageStatus = c.getInt(MessageListAdapter.COLUMN_SMS_STATUS);
                  // Aurora xuyong 2014-06-09 modified for aurora's new feature end
                    String preAddress = c.getString(MessageListAdapter.COLUMN_SMS_ADDRESS);
                    model.setPositions(cPos);
                    model.addIds(Long.valueOf(preMessageId));
                    model.addStatus(preMessageStatus);
                    model.addAddress(preAddress);
                    model.setNums(1);
                    try {
                        item = new MessageItem(mContext, type, c, mColumnsMap, mHighlight, model);
                        mMessageItemCache.put(key, item);
                    } catch (MmsException e) {
                        Log.e(TAG, "getCachedMessageItem: ", e);
                    }
                } else {
                    model = mList.get(getCurrentCursorPositons(cPos));
                    //aurora yudingmin 2014-08-21 modified for bug #7738 end
                    try {
                        item = new MessageItem(mContext, type, c, mColumnsMap, mHighlight, model);
                        mMessageItemCache.put(key, item);
                        int mCount = model.getNums();
                        for (int i = 1; i < mCount; i++) {
                            if (c != null && c.moveToNext()) {
                                item = new MessageItem(mContext, type, c, mColumnsMap, mHighlight, model);
                                mMessageItemCache.put(new Long(model.getIds().get(i)), item);
                            }
                        }
                        // after we create the item cache, we should move the cursor to the right position
                        mCursor.moveToPosition(cPos);
                    // Aurora xuyong 2013-12-11 modified for aurora's new feature end
                    } catch (MmsException e) {
                        Log.e(TAG, "getCachedMessageItem: ", e);
                    }
                }
            }
        }
        return item;
    }

    private boolean isCursorValid(Cursor cursor) {
        // Check whether the cursor is valid or not.
        if (cursor.isClosed() || cursor.isBeforeFirst() || cursor.isAfterLast()) {
            return false;
        }
        return true;
    }

    private static long getKey(String type, long id) {
        if (type.equals("mms")) {
            return -id;
        } else {
            return id;
        }
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    /* MessageListAdapter says that it contains two types of views. Really, it just contains
     * a single type, a MessageListItem. Depending upon whether the message is an incoming or
     * outgoing message, the avatar and text and other items are laid out either left or right
     * justified. That works fine for everything but the message text. When views are recycled,
     * there's a greater than zero chance that the right-justified text on outgoing messages
     * will remain left-justified. The best solution at this point is to tell the adapter we've
     * got two different types of views. That way we won't recycle views between the two types.
     * @see android.widget.BaseAdapter#getViewTypeCount()
     */
    @Override
    public int getViewTypeCount() {
        return 2;   // Incoming and outgoing messages
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        // Aurora xuyong 2013-12-11 modified for aurora's new feature start
        // Aurora xuyong 2014-01-03 modified for bug #1713 start 
        //aurora yudingmin 2014-08-21 modified for bug #7738 start
        if (mList != null && mDataValid && mCursor != null && !mCursor.moveToPosition(mList.get(position).getPositions())) {
        // Aurora xuyong 2014-01-03 modified for bug #1713 end
            throw new IllegalStateException("couldn't move cursor to position "
                    + mList.get(position).getPositions());
        }
        //aurora yudingmin 2014-08-21 modified for bug #7738 end
        // Aurora xuyong 2013-12-11 modified for aurora's new feature end
        View v;
        if (convertView == null) {
            v = newView(mContext, mCursor, parent);
            /*if(v instanceof MessageListItem){
                ((MessageListItem)v).setTextSize(mTextSize);
            }*/
        } else {
            v = convertView;
        }
        bindView(v, mContext, mCursor);
        return v;
    }
    
    @Override
    public Object getItem(int position) {
        // Aurora xuyong 2013-12-11 modified for aurora's new feature start
        //aurora yudingmin 2014-08-21 modified for bug #7738 start
        if (mDataValid && mCursor != null) {
            if(mList == null) {
                mCursor.moveToPosition(position);
            } else if(mList.size() > 0) {
                mCursor.moveToPosition(mList.get(position).getPositions());
            }
            //aurora yudingmin 2014-08-21 modified for bug #7738 end
        // Aurora xuyong 2013-12-11 modified for aurora's new feature end
            return mCursor;
        } else {
            return null;
        }
    }
    // Aurora xuyong 2013-12-11 added for aurora's new feature start
    public int getChildCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }
    // Aurora xuyong 2013-12-11 added for aurora's new feature end
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        // Aurora xuyong 2013-12-11 modified for aurora's new feature start
        //aurora yudingmin 2014-08-21 modified for bug #7738 start
        if(mList == null) {
          // Aurora xuyong 2014-08-22 modified for bug #7739 & #7853 start
            return mCursor == null? 0 : mCursor.getCount();
          // Aurora xuyong 2014-08-22 modified for bug #7739 & #7853 end
        } else {
            return mList.size();
        }// super.getCount();
        //aurora yudingmin 2014-08-21 modified for bug #7738 end
        // Aurora xuyong 2013-12-11 modified for aurora's new feature end
    }

    @Override
    public int getItemViewType(int position) {
        // Aurora xuyong 2014-06-07 modified for bug #5437 start
        Cursor cursor = (Cursor)getItem(getCurrentCursorPositons(position));
        // Aurora xuyong 2014-06-07 modified for bug #5437 end
        return getItemViewType(cursor);
    }

    private int getItemViewType(Cursor cursor) {
        // Aurora xuyong 2014-01-03 modified for bug #1713 start 
       // Aurora xuyong 2014-07-29 modified for aurora's new feature start
        if (cursor != null && !cursor.isAfterLast() && !cursor.isBeforeFirst()) {
       // Aurora xuyong 2014-07-29 modified for aurora's new feature end
            String type = cursor.getString(mColumnsMap.mColumnMsgType);
            int boxId;
            if ("sms".equals(type)) {
                long status = cursor.getLong(mColumnsMap.mColumnSmsStatus);
                // check sim sms and set box id
                if (status == SmsManager.STATUS_ON_ICC_SENT 
                        || status == SmsManager.STATUS_ON_ICC_UNSENT) {
                    boxId = Sms.MESSAGE_TYPE_SENT;
                } else if (status == SmsManager.STATUS_ON_ICC_READ
                        || status == SmsManager.STATUS_ON_ICC_UNREAD) {
                    boxId = Sms.MESSAGE_TYPE_INBOX;
                } else {
                    boxId = cursor.getInt(mColumnsMap.mColumnSmsType);
                }
            } else {
                boxId = cursor.getInt(mColumnsMap.mColumnMmsMessageBox);
            }
            return boxId == Mms.MESSAGE_BOX_INBOX ? INCOMING_ITEM_TYPE : OUTGOING_ITEM_TYPE;
        } else {
            return -1;
        }
        // Aurora xuyong 2014-01-03 modified for bug #1713 end
    }

    public static class ColumnsMap {
        public int mColumnMsgType;
        public int mColumnMsgId;
        public int mColumnSmsAddress;
        public int mColumnSmsBody;
        public int mColumnSmsDate;
        public int mColumnSmsDateSent;
        public int mColumnSmsRead;
        public int mColumnSmsType;
        public int mColumnSmsStatus;
        public int mColumnSmsLocked;
        public int mColumnSmsErrorCode;
        public int mColumnMmsSubject;
        public int mColumnMmsSubjectCharset;
        public int mColumnMmsDate;
        public int mColumnMmsDateSent;
        public int mColumnMmsRead;
        public int mColumnMmsMessageType;
        public int mColumnMmsMessageBox;
        public int mColumnMmsDeliveryReport;
        public int mColumnMmsReadReport;
        public int mColumnMmsErrorType;
        public int mColumnMmsLocked;
        //a0
        public int mColumnSmsSimId;
        public int mColumnMmsSimId;
        public int mColumnSmsServiceCenter;
        public int mColumnMmsServiceCenter;
        //a1
        //gionee gaoj 2012-4-10 added for CR00555790 start
        public int mColumnSmsStar;
        public int mColumnMmsStar;
        // Aurora xuyong 2014-10-23 added for privacy feature start
        public int mPrivacy;
        public int mWeatherInfo;
        // Aurora xuyong 2015-12-29 added for aurora 2.0 new feature start
        // Aurora xuyong 2016-03-05 modified for bug #20677 start
        public int mColumnSmsFolded;
        public int mColumnMmsFolded;
        // Aurora xuyong 2016-03-05 modified for bug #20677 end
        // Aurora xuyong 2015-12-29 added for aurora 2.0 new feature end
        // Aurora xuyong 2014-10-23 added for privacy feature end
        //gionee gaoj 2012-4-10 added for CR00555790 end

        public ColumnsMap() {
            mColumnMsgType            = COLUMN_MSG_TYPE;
            mColumnMsgId              = COLUMN_ID;
            mColumnSmsAddress         = COLUMN_SMS_ADDRESS;
            mColumnSmsBody            = COLUMN_SMS_BODY;
            mColumnSmsDate            = COLUMN_SMS_DATE;
            mColumnSmsDateSent        = COLUMN_SMS_DATE_SENT;
            mColumnSmsType            = COLUMN_SMS_TYPE;
            mColumnSmsStatus          = COLUMN_SMS_STATUS;
            mColumnSmsLocked          = COLUMN_SMS_LOCKED;
            mColumnSmsErrorCode       = COLUMN_SMS_ERROR_CODE;
            mColumnMmsSubject         = COLUMN_MMS_SUBJECT;
            mColumnMmsSubjectCharset  = COLUMN_MMS_SUBJECT_CHARSET;
            mColumnMmsMessageType     = COLUMN_MMS_MESSAGE_TYPE;
            mColumnMmsMessageBox      = COLUMN_MMS_MESSAGE_BOX;
            mColumnMmsDeliveryReport  = COLUMN_MMS_DELIVERY_REPORT;
            mColumnMmsReadReport      = COLUMN_MMS_READ_REPORT;
            mColumnMmsErrorType       = COLUMN_MMS_ERROR_TYPE;
            mColumnMmsLocked          = COLUMN_MMS_LOCKED;
            //a0
            mColumnSmsSimId           = COLUMN_SMS_SIMID;
            mColumnMmsSimId           = COLUMN_MMS_SIMID;
            mColumnSmsServiceCenter   = COLUMN_SMS_SERVICE_CENTER;
            mColumnMmsServiceCenter   = COLUMN_MMS_SERVICE_CENTER;
            //a1

            //gionee gaoj 2012-4-10 added for CR00555790 start
            if (MmsApp.mGnMessageSupport) {
                mColumnSmsStar = COLUMN_SMS_STAR;
                mColumnMmsStar = COLUMN_MMS_STAR;
            }
            //gionee gaoj 2012-4-10 added for CR00555790 end
            // Aurora xuyong 2014-10-23 added for privacy feature start
            mPrivacy = COLUMN_IS_PRIVACY;
            mWeatherInfo = COLUMN_WEATHER_INFO;
            // Aurora xuyong 2015-12-29 added for aurora 2.0 new feature start
            // Aurora xuyong 2016-03-05 modified for bug #20677 start
            mColumnSmsFolded = COLUMN_SMS_FOLDED;
            mColumnMmsFolded = COLUMN_MMS_FOLDED;
            // Aurora xuyong 2016-03-05 modified for bug #20677 end
            // Aurora xuyong 2015-12-29 added for aurora 2.0 new feature end
            // Aurora xuyong 2014-10-23 added for priacy feature end
        }

        public ColumnsMap(Cursor cursor) {
            // Ignore all 'not found' exceptions since the custom columns
            // may be just a subset of the default columns.
            try {
                mColumnMsgType = cursor.getColumnIndexOrThrow(
                        MmsSms.TYPE_DISCRIMINATOR_COLUMN);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMsgId = cursor.getColumnIndexOrThrow(BaseColumns._ID);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsAddress = cursor.getColumnIndexOrThrow(Sms.ADDRESS);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsBody = cursor.getColumnIndexOrThrow(Sms.BODY);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsDate = cursor.getColumnIndexOrThrow(Sms.DATE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsDateSent = cursor.getColumnIndexOrThrow(Sms.DATE_SENT);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsType = cursor.getColumnIndexOrThrow(Sms.TYPE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsStatus = cursor.getColumnIndexOrThrow(Sms.STATUS);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsLocked = cursor.getColumnIndexOrThrow(Sms.LOCKED);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsErrorCode = cursor.getColumnIndexOrThrow(Sms.ERROR_CODE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsSubject = cursor.getColumnIndexOrThrow(Mms.SUBJECT);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsSubjectCharset = cursor.getColumnIndexOrThrow(Mms.SUBJECT_CHARSET);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsMessageType = cursor.getColumnIndexOrThrow(Mms.MESSAGE_TYPE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsMessageBox = cursor.getColumnIndexOrThrow(Mms.MESSAGE_BOX);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsDeliveryReport = cursor.getColumnIndexOrThrow(Mms.DELIVERY_REPORT);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsReadReport = cursor.getColumnIndexOrThrow(Mms.READ_REPORT);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsErrorType = cursor.getColumnIndexOrThrow(PendingMessages.ERROR_TYPE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsLocked = cursor.getColumnIndexOrThrow(Mms.LOCKED);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }
            
            try {
                mColumnSmsSimId = cursor.getColumnIndexOrThrow(Sms.SIM_ID);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, e.getMessage());
            }


            try {
                mColumnMmsSimId = cursor.getColumnIndexOrThrow(Mms.SIM_ID);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, e.getMessage());
            }
            
            try {
                mColumnSmsServiceCenter = cursor.getColumnIndexOrThrow(Sms.SERVICE_CENTER);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, e.getMessage());
            }

            try {
                mColumnMmsServiceCenter = cursor.getColumnIndexOrThrow(Mms.SERVICE_CENTER);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, e.getMessage());
            }
            //gionee gaoj 2012-4-10 added for CR00555790 start
            if (MmsApp.mGnMessageSupport) {
                try {
                    mColumnSmsStar = cursor.getColumnIndexOrThrow("star");
                } catch (IllegalArgumentException e) {
                    Log.w("colsMap", e.getMessage());
                }

                try {
                    mColumnMmsStar = cursor.getColumnIndexOrThrow("star");
                } catch (IllegalArgumentException e) {
                    Log.w("colsMap", e.getMessage());
                }
            }
            //gionee gaoj 2012-4-10 added for CR00555790 end
            try {
                mWeatherInfo = cursor.getColumnIndexOrThrow("weather_info");
            } catch (IllegalArgumentException e) {
                Log.w(TAG, e.getMessage());
            }
            // Aurora xuyong 2016-03-05 added for bug #20677 start
            try {
                mColumnSmsFolded = cursor.getColumnIndexOrThrow("fold");
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsFolded = cursor.getColumnIndexOrThrow("fold");
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }
            // Aurora xuyong 2016-03-05 added for bug #20677 end
        }
    }
    
    //a0
    //add for multi-delete
    public boolean mIsDeleteMode = false;
    private Map<Long, Boolean> mListItem;
    // Aurora xuyong 2014-08-22 added for bug #7739 & #7853 start
    private Map<Long, Boolean> mAllListItem = new HashMap<Long, Boolean>();
    private Map<Long, Boolean> mNoneListItem = new HashMap<Long, Boolean>();
    // Aurora xuyong 2014-08-22 added for bug #7739 & #7853 end
    // Aurora xuyogn 2016-01-04 added for aurora's new feature start
    private int mSelectedItemCount = 0;
    // Aurora xuyogn 2016-01-04 added for aurora's new feature end
    
    //add for multi-delete
    // Aurora xuyong 2013-12-11 modified for aurora's new feature start
    public void changeSelectedState(ArrayList<Long> listId) {
        for (Long it : listId) {
            //gionee gaoj 2012-7-1 added for CR00623538 start
            if (mListItem.get(it) == null) {
               // Aurora xuyong 2014-08-26 modified for bug modify start
                mListItem.put(it, true);
               // Aurora xuyong 2014-08-26 modified for bug modify end
            } else {
            //gionee gaoj 2012-7-1 added for CR00623538 end
            mListItem.put(it, !mListItem.get(it));
            //gionee gaoj 2012-7-1 added for CR00623538 start
            }
            //gionee gaoj 2012-7-1 added for CR00623538 end
        }
    }
    // Aurora xuyong 2013-12-11 modified for aurora's new feature end
    // Aurora xuyong 2014-08-22 added for bug #7739 & #7853 start
    public void changeSelectedPosition(int pos) {
        if(mList != null) {
            pos = getInCursorPosition(pos);
        }
        if(mSelectedMap != null) {
            if (mSelectedMap.get(pos) == null) {
             // Aurora xuyong 2014-08-26 modified for bug modify start
                mSelectedMap.put(pos, true);
                // Aurora xuyogn 2016-01-04 added for aurora's new feature start
                mSelectedItemCount++;
                // Aurora xuyogn 2016-01-04 added for aurora's new feature end
             // Aurora xuyong 2014-08-26 modified for bug modify end
            } else {
                // Aurora xuyogn 2016-01-04 added for aurora's new feature start
                if (mSelectedMap.get(pos)) {
                    mSelectedItemCount--;
                } else {
                    mSelectedItemCount++;
                }
                // Aurora xuyogn 2016-01-04 added for aurora's new feature end
                mSelectedMap.put(pos, !mSelectedMap.get(pos));
            }
            // Aurora xuyogn 2016-01-04 added for aurora's new feature start
            // Aurora xuyong 2016-01-19 modified for aurora 2.0 new feature start
            updateMiddleText(mSelectedItemCount);
            // Aurora xuyong 2016-01-19 modified for aurora 2.0 new feature end
            // Aurora xuyogn 2016-01-04 added for aurora's new feature end
        }
    }
    // Aurora xuyong 2014-08-22 added for bug #7739 & #7853 end
    public  Map<Long, Boolean> getItemList() {
        return mListItem;
        
    }
    
    public Uri getMessageUri(long messageId) {
        Uri messageUri = null;
        if (messageId > 0) {
            messageUri = ContentUris.withAppendedId(Sms.CONTENT_URI, messageId);
        } else {
            messageUri = ContentUris.withAppendedId(Mms.CONTENT_URI, -messageId);
        }        
        return messageUri;
    }
    
    // Aurora xuyong 2013-09-26 added for aurora.s new feature start
    // Aurora xuyong 2013-12-11 modified for aurora's new feature start
    public void initListMap(Cursor cursor, ArrayList<Long> list, String type) {
        // Aurora xuyong 2013-10-11 modified for aurora's new feature start
        for (Long it : list) {
            if (type.equals("mms")) {
                mListItem.put(-it, true);
            } else {
                mListItem.put(it, true);
            }
        }
    // Aurora xuyong 2013-12-11 modified for aurora's new feature end
        // Aurora xuyong 2013-10-11 modified for aurora's new feature end
    }
    // Aurora xuyong 2013-09-26 added for aurora.s new feature end
    // Aurora xuyogn 2016-01-04 added for aurora's new feature start
    private AuroraActionBar mActionBar;
    public void setActionBar(AuroraActionBar actionBar) {
        mActionBar = actionBar;
    }
    // Aurora xuyogn 2016-01-04 added for aurora's new feature end
    // Aurora xuyong 2014-08-22 added for bug #7739 & #7853 start
    public void initSelectMap(int pos) {
        if(mList != null) {
            pos = getInCursorPosition(pos);
        }
        if (mSelectedMap != null) {
            mSelectedMap.put(pos, true);
            // Aurora xuyogn 2016-01-04 added for aurora's new feature start
            mSelectedItemCount++;
            // Aurora xuyogn 2016-01-04 added for aurora's new feature end
        }
        // Aurora xuyogn 2016-01-04 added for aurora's new feature start
        // Aurora xuyong 2016-01-19 modified for aurora 2.0 new feature start
        updateMiddleText(mSelectedItemCount);
        // Aurora xuyong 2016-01-19 modified for aurora 2.0 new feature end
        // Aurora xuyogn 2016-01-04 added for aurora's new feature end
    }    
    // Aurora xuyong 2014-08-22 added for bug #7739 & #7853 end
    public void initListMap(Cursor cursor) {
        if (cursor != null) {
            long itemId = 0;
            String type;
            long msgId = 0L;
            // gionee zhouyj 2012-05-05 added for CR00588554 start
            if(MmsApp.mGnMessageSupport) {
                cursor.moveToPosition(-1);
            }
            // gionee zhouyj 2012-05-05 added for CR00588554 end
            // Aurora xuyong 2014-08-22 added for bug #7739 & #7853 start
            int i = 0;
            // Aurora xuyong 2014-08-22 added for bug #7739 & #7853 end
            while (cursor.moveToNext()) {
                type = cursor.getString(mColumnsMap.mColumnMsgType);
                msgId = cursor.getLong(mColumnsMap.mColumnMsgId);
                //MessageItem item = getCachedMessageItem(type, msgId, cursor);
                itemId = getKey(type, msgId);
    
                if (mListItem.get(itemId) == null) {
                    mListItem.put(itemId, false);
                }
                // Aurora xuyong 2014-08-22 added for bug #7739 & #7853 start
                int pos = -1;
                if(mList != null) {
                    pos = getInCursorPosition(i);
                }
                if (mSelectedMap != null) {
                    mSelectedMap.put(pos, false);
                    // Aurora xuyogn 2016-01-04 added for aurora's new feature start
                    mSelectedItemCount--;
                    // Aurora xuyogn 2016-01-04 added for aurora's new feature end
                }
                // Aurora xuyogn 2016-01-04 added for aurora's new feature start
                // Aurora xuyong 2016-01-19 modified for aurora 2.0 new feature start
                updateMiddleText(mSelectedItemCount);
                // Aurora xuyong 2016-01-19 modified for aurora 2.0 new feature end
                // Aurora xuyogn 2016-01-04 added for aurora's new feature end
                i++;
                // Aurora xuyong 2014-08-22 added for bug #7739 & #7853 end
            }
        }  
    }
    // Aurora xuyong 2016-01-19 added for aurora 2.0 new feature start
    private void updateMiddleText(int selectedNum) {
        TextView middleTextView = mActionBar.getMiddleTextView();
        if (selectedNum == 0) {
            middleTextView.setText(mContext.getResources().getString(R.string.noItemSelected));
        } else {
            middleTextView.setText(mContext.getResources().getQuantityString(R.plurals.numberOfItemSelected, selectedNum, selectedNum));
        }
    }
    // Aurora xuyong 2016-01-19 added for aurora 2.0 new feature end
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
    
    public void clearList() {
        if (mListItem != null) {
            // Aurora xuyong 2014-08-22 modified for bug #7739 & #7853 start
            mListItem = new HashMap<Long, Boolean>();
            // Aurora xuyong 2014-08-22 modified for bug #7739 & #7853 end
        }
        // Aurora xuyong 2014-08-22 added for bug #7739 & #7853 start
        if (mSelectedMap != null) {
            // Aurora xuyogn 2016-01-04 added for aurora's new feature start
            mSelectedItemCount = 0;
            // Aurora xuyong 2016-01-19 modified for aurora 2.0 new feature start
            updateMiddleText(mSelectedItemCount);
            // Aurora xuyong 2016-01-19 modified for aurora 2.0 new feature end
            // Aurora xuyogn 2016-01-04 added for aurora's new feature end
            mSelectedMap = new HashMap<Integer, Boolean>();
        }
        // Aurora xuyong 2014-08-22 added for bug #7739 & #7853 end
        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (MmsApp.mGnMessageSupport) {

            if (mMsgStared != null) {
                mMsgStared.clear();
            }
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
    }

    //gionee gaoj 2012-9-20 added for CR00699291 start
    public long mDeleteMsgid;
    //gionee gaoj 2012-9-20 added for CR00699291 end
    public int getSelectedNumber() {
        // Aurora xuyong 2014-08-22 added for bug #7739 & #7853 start
          mSelectedMmsNumber = 0;
        // Aurora xuyong 2014-08-22 added for bug #7739 & #7853 end
        Iterator iter = mListItem.entrySet().iterator();
        int number = 0;
        while (iter.hasNext()) {
            @SuppressWarnings("unchecked")
            Map.Entry<Long, Boolean> entry = (Entry<Long, Boolean>) iter.next();
            if (entry.getValue()) {
                number++;
                mDeleteMsgid = entry.getKey();
            }
            // Aurora xuyong 2014-08-22 added for bug #7739 & #7853 start
            if (entry.getKey() < 0 && entry.getValue()) {
                mSelectedMmsNumber++;
            }
            // Aurora xuyong 2014-08-22 added for bug #7739 & #7853 end
        }
        return number;
    }
    // Aurora xuyong 2014-08-22 added for bug #7739 & #7853 start
    private int mSelectedMmsNumber;
    public int getSelectedMmsNumber() {
        return mSelectedMmsNumber;
    }
    
    
    public Map<Integer, Boolean> mSelectedMap = new HashMap<Integer, Boolean>();
    public Map<Integer, Boolean> mAllSelectedMap = new HashMap<Integer, Boolean>();
    public Map<Integer, Boolean> mNoneSelectedMap = new HashMap<Integer, Boolean>();
    public Map<Integer, Boolean> getSelectedMap() {
        Map<Integer, Boolean> sortMap = new TreeMap<Integer, Boolean>(new MapKeyComparator());
        sortMap.putAll(mSelectedMap);
        Iterator iter = sortMap.entrySet().iterator();
        while (iter.hasNext()) {
            @SuppressWarnings("unchecked")
            Map.Entry<Integer, Boolean> entry = (Entry<Integer, Boolean>) iter.next();
        }
        return mSelectedMap;
    }
    
    public class MapKeyComparator implements Comparator<Integer>{  
        public int compare(Integer id1, Integer id2) {  
            return id1.compareTo(id2);  
        }  
    }  
    // Aurora xuyong 2014-08-22 added for bug #7739 & #7853 end
//MTK_OP01_PROTECT_START
    // add for text zoom
    private final float DEFAULT_TEXT_SIZE = 20;
    private float mTextSize = DEFAULT_TEXT_SIZE;
    public void setTextSize(float size) {
        mTextSize = size;
    }
//MTK_OP01_PROTECT_END
    //gionee gaoj 2012-9-20 added for CR00699291 start
    public void changeSelectedStaredState(MessageItem msgItem) {
        long msgId = msgItem.mMsgId;
        if ((msgItem.mType).equals("mms")) {
            msgId = -msgItem.mMsgId;
        }
        if (mListItem.get(msgId) != null) {


            if (mListItem.get(msgId)) {
                mMsgStared.put(msgId, msgItem.mStar);
            } else if (!mListItem.get(msgId)) {
                mMsgStared.remove(msgId);
            }
        }
    }
    // Aurora xuyong 2014-08-22 added for bug #7739 & #7853 start
    public void getAllAndNone(final Cursor cursor) {
        initListMaps(cursor);
    }
    
    public void initListMaps(Cursor cursor) {
        mAllListItem.clear();
        mNoneListItem.clear();
        mAllSelectedMap.clear();
        mNoneSelectedMap.clear();
        if (cursor != null) {
            long itemId = 0;
            String type;
            long msgId = 0L;
            boolean stared = false;
            cursor.moveToPosition(-1);
            int i = 0;
            while (cursor.moveToNext()) {
                type = cursor.getString(mColumnsMap.mColumnMsgType);
                msgId = cursor.getLong(mColumnsMap.mColumnMsgId);
                itemId = getKey(type, msgId);
                mAllListItem.put(itemId, true);
                mNoneListItem.put(itemId, false);
                mAllSelectedMap.put(i, true);
                mNoneSelectedMap.put(i, false);
                i++;
            }
        }
    }
    
    public void initListMaps(boolean isAllSelected) {
        if (isAllSelected) {
            mListItem = mAllListItem;
            // Aurora xuyong 2015-03-10 modified for bug #12099 start
            mSelectedMap.putAll(mAllSelectedMap);
            // Aurora xuyong 2015-03-10 modified for bug #12099 end
            // Aurora xuyogn 2016-01-04 added for aurora's new feature start
            mSelectedItemCount = mAllSelectedMap.size();
            // Aurora xuyong 2016-01-19 modified for aurora 2.0 new feature start
            updateMiddleText(mSelectedItemCount);
            // Aurora xuyong 2016-01-19 modified for aurora 2.0 new feature end
            // Aurora xuyogn 2016-01-04 added for aurora's new feature end
        } else {
            mListItem = mNoneListItem;
            // Aurora xuyong 2015-03-10 modified for bug #12099 start
            mSelectedMap.putAll(mNoneSelectedMap);
            // Aurora xuyogn 2016-01-04 added for aurora's new feature start
            mSelectedItemCount = 0;
            // Aurora xuyong 2016-01-19 modified for aurora 2.0 new feature start
            updateMiddleText(mSelectedItemCount);
            // Aurora xuyong 2016-01-19 modified for aurora 2.0 new feature end
            // Aurora xuyogn 2016-01-04 added for aurora's new feature end
            // Aurora xuyong 2015-03-10 modified for bug #12099 end
        }
    }
    // Aurora xuyong 2014-08-22 added for bug #7739 & #7853 end
    public void initStaredMaps(Cursor cursor) {
        if (cursor != null) {
            long itemId = 0;
            String type;
            long msgId = 0L;
            boolean stared = false;
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                type = cursor.getString(mColumnsMap.mColumnMsgType);
                msgId = cursor.getLong(mColumnsMap.mColumnMsgId);
                itemId = getKey(type, msgId);
                if (itemId > 0) {

                    stared = cursor.getInt(mColumnsMap.mColumnSmsStar) != 0;
                } else {

                    stared = false;
                }

                mMsgStared.put(itemId, stared);
                // Aurora xuyong 2014-08-22 added for bug #7739 & #7853 start
                if (mListItem != null) {
                    if (mListItem.get(itemId) == null) {
                        mListItem.put(itemId, false);
                    }
                }
                // Aurora xuyong 2014-08-22 added for bug #7739 & #7853 end
            }
        }
    }
    //gionee gaoj 2012-9-20 added for CR00699291 end

    public Map<Long, Boolean> getmMsgStared() {
        return mMsgStared;
    }
    //gionee gaoj 2012-4-10 added for CR00555790 end
}
