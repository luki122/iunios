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

import java.util.HashSet;
import java.util.Iterator;
// Aurora xuyong 2014-05-23 added for density-modify start
// Aurora xuyong 2015-12-04 added for aurora2.0 new feature start
import com.aurora.mms.ui.ConvFragment;
// Aurora xuyong 2015-12-04 added for aurora2.0 new feature end
// Aurora xuyong 2016-02-29 added for bug #20197 start
import com.aurora.mms.util.AnimUtils;
import com.aurora.mms.util.Utils;
// Aurora xuyong 2016-02-29 added for bug #20197 end
import com.aurora.utils.DensityUtil;
// Aurora xuyong 2014-05-23 added for density-modify end
import com.android.mms.R;
import com.android.mms.LogTag;
import com.android.mms.data.Conversation;
import com.android.mms.ui.MessageCursorAdapter;

import android.animation.ObjectAnimator;

import android.content.Context;
import android.database.Cursor;
// Aurora xuyong 2016-03-03 added for bug #20197 start
import android.graphics.Color;
import android.graphics.drawable.Drawable;
// Aurora xuyong 2016-03-03 added for bug #20197 end
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.RelativeLayout;

//gionee gaoj 2012-3-22 added for CR00555790 start
import android.os.SystemProperties;
import com.android.mms.MmsApp;
import java.util.ArrayList;
//gionee gaoj 2012-3-22 added for CR00555790 end
// gionee zhouyj 2012-07-31 add for CR00662942 start 
import com.android.mms.util.GnSelectionManager;
// gionee zhouyj 2012-07-31 add for CR00662942 end 

//gionee gaoj 2013-2-19 adde for CR00771935 start
import android.widget.ImageView;
// Aurora xuyong 2016-02-29 added for bug #20197 start
import android.widget.TextView;
// Aurora xuyong 2016-02-29 added for bug #20197 end
import com.android.mms.data.Contact;
//gionee gaoj 2013-2-19 adde for CR00771935 end
import aurora.widget.AuroraListView;
import aurora.widget.AuroraCheckBox;

/**
 * The back-end data adapter for ConversationList.
 */
//TODO: This should be public class ConversationListAdapter extends ArrayAdapter<Conversation>
// Aurora liugj 2013-12-03 modified for checkbox animation 
public class ConversationListAdapter extends MessageCursorAdapter implements AbsListView.RecyclerListener {
    private static final String TAG = "ConversationListAdapter";
    private static final boolean LOCAL_LOGV = false;

    private final LayoutInflater mFactory;
    private OnContentChangedListener mOnContentChangedListener;
    // gionee zhouyj 2012-07-31 add for CR00662942 start 
    private GnSelectionManager<Long> mSelectionManager;
    // gionee zhouyj 2012-07-31 add for CR00662942 end 
    // gionee zhouyj 2012-10-12 add for CR00711214 start 
    private boolean mIsShowCheckBox = false;
    // gionee zhouyj 2012-10-12 add for CR00711214 end 
    private AuroraListView mListView;
    private Context mContext;
    private final int mItemHeight;
    private final int mFrontHeight;
    // Aurora xuyong 2014-10-23 added for privacy feature start
    private boolean mShowPrivacy = true;
    // Aurora xuyong 2014-10-23 added for privacy feature end
    // Aurora xuyong 2015-12-04 added for aurora2.0 new feature start
    private int mTabIndex = ConvFragment.PERSONAL_TAB;

    // Aurora xuyong 2016-01-28 added for xy-smartsms start
    public boolean SCROLL_STATE_FLING = false;
    // Aurora xuyong 2016-01-28 added for xy-smartsms end

    public ConversationListAdapter(Context context, Cursor cursor, int tabIndex) {
        super(context, cursor, false /* auto-requery */);
        mFactory = LayoutInflater.from(context);
        mContext = context;
        mItemHeight = mContext.getResources().getDimensionPixelOffset(com.aurora.R.dimen.aurora_list_doubleline_height);//DensityUtil.dip2px(context, ) / 3;
        mFrontHeight = mItemHeight - 1;//DensityUtil.dip2px(context, 213) / 3;
        mTabIndex = tabIndex;
    }
    // Aurora xuyong 2015-12-04 added for aurora2.0 new feature end
    public ConversationListAdapter(Context context, Cursor cursor) {
        super(context, cursor, false /* auto-requery */);
        mFactory = LayoutInflater.from(context);
        mContext = context;
        mItemHeight = mContext.getResources().getDimensionPixelOffset(com.aurora.R.dimen.aurora_list_doubleline_height);//DensityUtil.dip2px(context, ) / 3;
        mFrontHeight = mItemHeight - 1;//DensityUtil.dip2px(context, 213) / 3;
    }
    // Aurora xuyong 2016-03-03 added for bug #20197 start
    private boolean mNeedDateMargin = false;
    public ConversationListAdapter(Context context, Cursor cursor, boolean needDateMargin) {
        this(context, cursor);
        mNeedDateMargin = needDateMargin;
    }
    // Aurora xuyong 2016-03-03 added for bug #20197 end
    public ConversationListAdapter(Context context, Cursor cursor, AuroraListView listview) {
        super(context, cursor, false /* auto-requery */);
        mListView = listview;
        mFactory = LayoutInflater.from(context);
        mContext = context;
        mItemHeight = mContext.getResources().getDimensionPixelOffset(com.aurora.R.dimen.aurora_list_doubleline_height);//DensityUtil.dip2px(context, ) / 3;
        mFrontHeight = mItemHeight - 1;//DensityUtil.dip2px(context, 213) / 3;
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
    
    // Aurora xuyong 2014-10-23 added for privacy feature start
    public void setPrivacy(boolean show) {
        mShowPrivacy = show;
    }
    // Aurora xuyong 2014-10-23 added for privacy feature end
     // Aurora yudingmin 2014-08-30 added for optimize start
    private int mCount = 0;

    // Aurora yudingmin 2014-09-01 added for optimize start
    public void changeCountBeforeDataChanged(int count){
        mCount = count;
    }
    // Aurora yudingmin 2014-09-01 added for optimize end
    
    public void notifyCountSetChanged(int count){
        mCount = count;
        notifyDataSetChanged();
    }
    // Aurora xuyong 2015-12-04 added for aurora2.0 new feature start
    public void setTabIndex(int index) {
        mTabIndex = index;
    }

    public int getTabIndex() {
        return mTabIndex;
    }
    // Aurora xuyong 2015-12-04 added for aurora2.0 new feature end
    public int getCount(){
        int count = 0;
        // Aurora xuyong 2014-10-30 modified for cursor excepion start
        if(mCursor != null && !mCursor.isClosed()) {
        // Aurora xuyong 2014-10-30 modified for cursor excepion end
            if(mCount < mCursor.getCount()){
                count = mCount;
            } else {
                count = mCursor.getCount();
            }
        }
        return count;
    }
    // Aurora yudingmin 2014-08-30 added for optimize end
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
          // Aurora liugj 2013-10-11 modified for aurora's new feature start
        /*if (!(view instanceof ConversationListItem)) {
            Log.e(TAG, "Unexpected bound view: " + view);
            return;
        }*/
        ConverHolder holder = (ConverHolder) view.getTag();
        // Aurora xuyong 2016-03-03 modified for bug #20197 start
        AuroraCheckBox checkBox = holder.checkBox;
        checkBox.setButtonDrawable(com.aurora.R.drawable.aurora_btn_check_material_anim);
        // Aurora xuyong 2016-03-03 modified for bug #20197 end
        View frontView = holder.front;
        ConversationListItem headerView;
        if (frontView != null && frontView instanceof ConversationListItem) {
            headerView = (ConversationListItem) frontView;
        } else {
            return;
        }
        // Aurora xuyong 2016-03-03 added for bug #20197 start
        if (mNeedDateMargin) {
            TextView dataView = (TextView)frontView.findViewById(R.id.aurora_date);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, AnimUtils.dip2px(context, 3), AnimUtils.dip2px(context, 32), 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            dataView.setLayoutParams(lp);
        }
        // Aurora xuyong 2016-03-03 added for bug #20197 end
        // Aurora xuyong 2016-01-28 added for xy-smartsms start
        headerView.setScrolling(SCROLL_STATE_FLING);
        // Aurora xuyong 2016-01-28 added for xy-smartsms end
         // Aurora liugj 2013-10-11 modified for aurora's new feature end
        // Aurora xuyong 2014-10-23 added for privacy feature start
        headerView.setPrivacy(mShowPrivacy);
        // Aurora xuyong 2014-10-23 added for privacy feature end
        Conversation conv = null;
        // Aurora xuyong 2016-02-29 added for bug #20197 start
        TextView dataView = (TextView)frontView.findViewById(R.id.aurora_date);
        // Aurora xuyong 2016-02-29 added for bug #20197 end
        if (!mIsScrolling) {
            if (mIsShowCheckBox) {
                // Aurora xuyong 2016-03-09 added for bug #20802 start
                boolean needAnim = mIsShowCheckBox && checkBox.getVisibility() != View.VISIBLE && !mNeedDateMargin;
                // Aurora xuyong 2016-03-09 added for bug #20802 end
                // Aurora xuyong 2016-03-03 modified for bug #20197 start
                checkBox.setVisibility(View.VISIBLE);
                // Aurora xuyong 2016-03-05 added for bug #20741 start
                checkBox.setAlpha(1);
                // Aurora xuyong 2016-03-05 added for bug #20741 start
                // Aurora xuyong 2016-03-03 modified for bug #20197 end
                // Aurora xuyong 2016-02-29 added for bug #20197 start
                // Aurora xuyong 2016-03-09 modified for bug #20802 start
                if (needAnim) {
                // Aurora xuyong 2016-03-09 modified for bug #20802 end
                    // Aurora xuyong 2016-03-03 modified for bug #20197 start
                    int distance = AnimUtils.dip2px(context, 34);
                    AnimUtils.move(dataView, 500, 0, AnimUtils.CURVE_SHOW, true, 0, -distance, null);
                    // Aurora xuyong 2016-03-03 modified for bug #20197 end
                    // Aurora xuyong 2016-03-03 added for bug #20197 start
                    AuroraListView.auroraStartCheckBoxAppearingAnim(view, checkBox);
                    // Aurora xuyong 2016-03-03 added for bug #20197 end
                }
                // Aurora xuyong 2016-02-29 added for bug #20197 end
                if (mAllShowCheckBox == 1 && this != null && this.getCount() > 1) {
                    // Aurora xuyong 2016-03-03 modified for bug #20197 start
                    checkBox.setChecked(true);
                    // Aurora xuyong 2016-03-03 modified for bug #20197 end
                }else if (mAllShowCheckBox == 2) {
                    // Aurora xuyong 2016-03-03 modified for bug #20197 start
                    checkBox.setChecked(false);
                    // Aurora xuyong 2016-03-03 modified for bug #20197 end
                }
                if (mSelectionManager != null) {
                    // Aurora xuyong 2016-03-03 modified for bug #20197 start
                    checkBox.setChecked(mSelectionManager.isSelected(cursor.getLong(0)));
                    // Aurora xuyong 2016-03-03 modified for bug #20197 end
                }
            } else {
                // Aurora xuyong 2016-03-09 added for bug #20802 start
                boolean needAnim = !mIsShowCheckBox && checkBox.getVisibility() == View.VISIBLE && !mNeedDateMargin;
                // Aurora xuyong 2016-03-09 added for bug #20802 end
                // Aurora xuyong 2016-03-03 modified for bug #20197 start
                checkBox.setVisibility(View.GONE);
                // Aurora xuyong 2016-03-03 modified for bug #20197 end
                // Aurora xuyong 2016-02-29 added for bug #20197 start
                // Aurora xuyong 2016-03-09 modified for bug #20802 start
                if (needAnim) {
                // Aurora xuyong 2016-03-09 modified for bug #20802 end
                    // Aurora xuyong 2016-03-03 modified for bug #20197 start
                    int distance = AnimUtils.dip2px(context, 34);
                    AnimUtils.move(dataView, 500, 0, AnimUtils.CURVE_SHOW, true, -distance, 0, null);
                    // Aurora xuyong 2016-03-03 modified for bug #20197 end
                    // Aurora xuyong 2016-03-03 added for bug #20197 start
                    AuroraListView.auroraStartCheckBoxDisappearingAnim(view, checkBox);
                    // Aurora xuyong 2016-03-03 added for bug #20197 end
                }
                // Aurora xuyong 2016-02-29 added for bug #20197 end
                // Aurora xuyong 2016-03-03 modified for bug #20197 start
                if (checkBox.getAlpha() == 1.0f){
                    checkBox.setChecked(false);
                // Aurora xuyong 2016-03-03 modified for bug #20197 end
                }
            }
            Conversation.setNeedCacheConv(false);
            conv = Conversation.from(context, cursor);
            Conversation.setNeedCacheConv(true);
        } else {
            if (mIsShowCheckBox && mSelectionManager != null) {
                // Aurora xuyong 2016-03-03 modified for bug #20197 start
                checkBox.setVisibility(View.VISIBLE);
                // Aurora xuyong 2016-03-05 added for bug #20741 start
                checkBox.setAlpha(1);
                // Aurora xuyong 2016-03-05 added for bug #20741 end
                checkBox.setChecked(mSelectionManager.isSelected(cursor.getLong(0)));
                // Aurora xuyong 2016-03-03 modified for bug #20197 end
            } else {
                // Aurora xuyong 2016-03-03 modified for bug #20197 start
                checkBox.setVisibility(View.GONE);
                // Aurora xuyong 2016-03-03 modified for bug #20197 end
            }
        }

        if (MmsApp.mGnPerfList) {
            if (!mIsScrolling) {
                // Aurora liugj 2013-11-18 modified for aurora's new feature start
                // Aurora xuyong 2016-03-10 modified for bug #18928 start
                headerView.bind(context, conv, cursor.getPosition(), mIsShowCheckBox, mTabIndex);
                // Aurora xuyong 2016-03-10 modified for bug #18928 end
                // Aurora liugj 2013-11-18 modified for aurora's new feature end
            }/* else {
                headerView.GnbindDefault();
            }*/
            return;
        }
        //Gionee <gaoj> <2013-05-13> added for CR00811367 end
        
         //gionee gaoj 2012-3-22 added for CR00555790 start
        if (MmsApp.mGnMessageSupport) {
            if (!mIsScrolling) {
                ConversationListItemData ch = new ConversationListItemData(context, conv);
                headerView.setCheckBoxVisibility(mIsShowCheckBox);
                // Aurora xuyong 2016-03-10 modified for bug #18928 start
                headerView.bind(context, ch, conv, mSelectionManager, cursor.getPosition(), mTabIndex);
                // Aurora xuyong 2016-03-10 modified for bug #18928 end
            }/* else {
                headerView.bindDefault();
            }*/
        } else {
            if (!mIsScrolling) {
                headerView.bind(context, conv, mIsShowCheckBox);
            }/* else {
                headerView.bindDefault();
            }*/
        }
    }

    public void onMovedToScrapHeap(View view) {
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Aurora xuyong 2016-03-03 added for bug #20197 start
        View view = (View) mFactory.inflate(com.aurora.R.layout.aurora_slid_listview, parent, false);
        RelativeLayout controlPadding = (RelativeLayout)view.findViewById(com.aurora.R.id.control_padding);
        controlPadding.setPadding(0, 0, 0, 0);
        RelativeLayout front = (RelativeLayout)view.findViewById(com.aurora.R.id.aurora_listview_front);
        AuroraCheckBox checkBox = (AuroraCheckBox) view.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // Aurora xuyong 2016-03-03 added for bug #20197 end
        ConversationListItem frontView = null;
        if (mTabIndex == ConvFragment.PERSONAL_TAB) {
            frontView = (ConversationListItem) mFactory.inflate(R.layout.aurora_conversation_list_item_front, null);
            // Aurora xuyong 2016-03-03 added for bug #20197 start
            layoutParams.setMargins(parent.getMeasuredWidth() - context.getResources().getDimensionPixelOffset(R.dimen.aurora_list_cb_lm),
                    context.getResources().getDimensionPixelOffset(R.dimen.aurora_list_conv_cb_tm), 0, 0);
            checkBox.setLayoutParams(layoutParams);
            // Aurora xuyong 2016-03-03 added for bug #20197 end
        } else if (mTabIndex == ConvFragment.NOTIFICATION_TAB){
            frontView = (ConversationListItem) mFactory.inflate(R.layout.aurora_notify_conversation_list_item_front, null);
            // Aurora xuyong 2016-03-03 added for bug #20197 start
            layoutParams.setMargins(parent.getMeasuredWidth() - context.getResources().getDimensionPixelOffset(R.dimen.aurora_list_cb_lm),
                    context.getResources().getDimensionPixelOffset(R.dimen.aurora_list_notify_conv_cb_tm), 0, 0);
            checkBox.setLayoutParams(layoutParams);
            // Aurora xuyong 2016-03-03 added for bug #20197 end
        }
        // Aurora xuyong 2016-03-03 modified for bug #20197 start
        front.addView(frontView, new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        // Aurora xuyong 2016-03-03 modified for bug #20197 end
        ConverHolder holder = new ConverHolder();
        holder.front = frontView;
        holder.checkBox = checkBox;
        // Aurora xuyong 2016-03-03 modified for bug #20197 start
        view.setTag(holder);
        return view;
        // Aurora xuyong 2016-03-03 modified for bug #20197 end
      // Aurora liugj 2013-10-11 modified for aurora's new feature end
    }
    
    private class ConverHolder{
        public RelativeLayout front;
        public AuroraCheckBox checkBox;
    }

    public interface OnContentChangedListener {
        void onContentChanged(ConversationListAdapter adapter);
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

    public void uncheckAll() {
        int count = getCount();
        for (int i = 0; i < count; i++) {
            Cursor cursor = (Cursor)getItem(i);
            Conversation conv = Conversation.from(mContext, cursor);
            conv.setIsChecked(false);
        }
    }
    // do not uncheck all the list item each time
    public void uncheckSelect(HashSet<Integer> idSet){
        if (idSet != null && idSet.size() > 0){
            Iterator iterator = idSet.iterator(); 
            while (iterator.hasNext()) {
                int index = (Integer) iterator.next();
                Log.d(TAG, "uncheckSelect index " + index);
                Cursor cursor = (Cursor) getItem(index);
                Conversation conv = Conversation.from(mContext, cursor);
                conv.setIsChecked(false);
            }
        }
    }

     @Override
    public void notifyDataSetChanged() {
        // TODO Auto-generated method stub
        super.notifyDataSetChanged();
        /*Log.i(TAG, "[Performance test][Mms] loading data end time ["
            + System.currentTimeMillis() + "]" );*/
    }

    public boolean isDataValid() {
        return mDataValid;
    }

    
    // gionee zhouyj 2012-07-31 add for CR00662942 start 
    public void setSelectionManager(GnSelectionManager<Long> selectionManager) {
        mSelectionManager = selectionManager;
    }
    // gionee zhouyj 2012-07-31 add for CR00662942 end 
    
    // gionee zhouyj 2012-10-12 add for CR00711214 start 
    public void showCheckBox(boolean show) {
        mIsShowCheckBox = show;
    }
    // gionee zhouyj 2012-10-12 add for CR00711214 end 
}
