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
import com.aurora.utils.DensityUtil;
// Aurora xuyong 2014-05-23 added for density-modify end
import com.android.mms.R;
import com.android.mms.LogTag;
import com.android.mms.data.Conversation;
import com.android.mms.ui.MessageCursorAdapter;

import android.animation.ObjectAnimator;

import android.content.Context;
import android.database.Cursor;
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
    public ConversationListAdapter(Context context, Cursor cursor) {
        super(context, cursor, false /* auto-requery */);
        mFactory = LayoutInflater.from(context);
        mContext = context;
        mItemHeight = mContext.getResources().getDimensionPixelOffset(com.aurora.R.dimen.aurora_list_doubleline_height);//DensityUtil.dip2px(context, ) / 3;
        mFrontHeight = mItemHeight - 1;//DensityUtil.dip2px(context, 213) / 3;
    }
    
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
        AuroraCheckBox mCheckBox = holder.mCheckBox;
        RelativeLayout front = holder.front;
        View frontView = holder.frontView;
        ConversationListItem headerView;
        if (frontView != null && frontView instanceof ConversationListItem) {
            headerView = (ConversationListItem) frontView;
        }else {
            return;
        }
         // Aurora liugj 2013-10-11 modified for aurora's new feature end
        // Aurora xuyong 2014-10-23 added for privacy feature start
        headerView.setPrivacy(mShowPrivacy);
        // Aurora xuyong 2014-10-23 added for privacy feature end
        Conversation conv = null;
        if (!mIsScrolling) {
            if (mIsShowCheckBox) {
                // Aurora liugj 2014-01-07 modified for allcheck animation start
             // when the number of this child of this parent view is only 1, 
             // so the checkbox won't show here!
             // Aurora xuyong 2014-06-11 modified for upper reason start
                if (mAllShowCheckBox == 1 && this != null && this.getCount() > 1) {
             // Aurora xuyong 2014-06-11 modified for upper reason end
                    mCheckBox.auroraSetChecked(true, true);
                // Aurora xuyong 2014-07-30 deleted for bug #7014 start
                    //return;
                // Aurora xuyong 2014-07-30 deleted for bug #7014 end
                }else if (mAllShowCheckBox == 2) {
                    mCheckBox.auroraSetChecked(false, true);
                // Aurora xuyong 2014-07-30 deleted for bug #7014 start
                    //return;
                // Aurora xuyong 2014-07-30 deleted for bug #7014 end
                }
                // Aurora liugj 2014-01-07 modified for allcheck animation end
                // Aurora liugj 2013-12-10 modified for aurora's new feature start
                // Aurora liugj 2013-12-12 modified for hide dateView anim start
                // Aurora liugj 2014-01-09 modified for checkbox animation start
                if (mCheckBox.getAlpha() == 0.0f) {
                // Aurora liugj 2014-01-16 modified for allcheck animation end
                    if (mNeedAnim) {
                        // Aurora liugj 2013-12-09 modified for aurora's new feature start
                        aurora.widget.AuroraListView.auroraStartCheckBoxAppearingAnim(front, mCheckBox);
                        /*ObjectAnimator anim = ObjectAnimator.ofInt(headerView.mDateView, "left", headerView.mDateView.getLeft(), headerView.mDateView.getLeft() -82);
                        anim.setDuration(300);
                        anim.start();*/
                        // Aurora liugj 2013-12-12 modified for hide dateView anim end
                    }else {
                        aurora.widget.AuroraListView.auroraSetCheckBoxVisible(front, mCheckBox, true);
                    }
                    //mCheckBox.setVisibility(View.VISIBLE);
                    // Aurora liugj 2013-12-09 modified for aurora's new feature end
                    //headerView.setPadding(0, context.getResources().getDimensionPixelSize(R.dimen.list_item_top_margin), context.getResources().getDimensionPixelSize(com.aurora.R.dimen.aurora_subtitle_padding_right), context.getResources().getDimensionPixelSize(R.dimen.list_item_bottom_margin));
                }
                // Aurora liugj 2014-01-16 modified for allcheck animation start
                if (mSelectionManager != null) {
                    mCheckBox.setChecked(mSelectionManager.isSelected(cursor.getLong(0)));
                    //mCheckBox.setVisibility(View.VISIBLE);
                }
            } else {
             // Aurora liugj 2014-01-16 modified for checkbox animation start   
                if (mCheckBox.getAlpha() == 1.0f){
                    /*if (!mNeedAnim) {*/
                        aurora.widget.AuroraListView.auroraStartCheckBoxDisappearingAnim(front, mCheckBox);
                    /*}else {
                        aurora.widget.AuroraListView.auroraSetCheckBoxVisible(front, mCheckBox, false);
                    }*/
                    mCheckBox.setChecked(false);
                }
                // Aurora liugj 2014-01-16 modified for checkbox animation end
                    //mCheckBox.setVisibility(View.GONE);
                //headerView.setPadding(context.getResources().getDimensionPixelSize(com.aurora.R.dimen.aurora_subtitle_padding_left), context.getResources().getDimensionPixelSize(R.dimen.list_item_top_margin), context.getResources().getDimensionPixelSize(com.aurora.R.dimen.aurora_subtitle_padding_right), context.getResources().getDimensionPixelSize(R.dimen.list_item_bottom_margin));
            }
            // Aurora liugj 2013-12-10 modified for aurora's new feature end
            Conversation.setNeedCacheConv(false);
            conv = Conversation.from(context, cursor);
            Conversation.setNeedCacheConv(true);
              // Aurora liugj 2014-01-07 modified for allcheck animation end
        }else {
            // Aurora liugj 2014-01-06 modified for checkbox animation optimize start
            if (mIsShowCheckBox && mSelectionManager != null) {
                mCheckBox.setChecked(mSelectionManager.isSelected(cursor.getLong(0)));
            }
            // Aurora liugj 2014-01-06 modified for checkbox animation optimize end
        }
        
        //Gionee <gaoj> <2013-05-13> added for CR00811367 begin
        if (MmsApp.mGnPerfList) {
            if (!mIsScrolling) {
                // Aurora liugj 2013-11-18 modified for aurora's new feature start
                headerView.bind(context, conv, cursor.getPosition(), mIsShowCheckBox);
                // Aurora liugj 2013-11-18 modified for aurora's new feature end
            } else {
                headerView.GnbindDefault();
            }
            // Aurora xuyong 2014-04-09 modified for aurora's new feature start
            ViewGroup.LayoutParams  lp =  view.getLayoutParams(); 
            ViewGroup.LayoutParams  mFront =  front.getLayoutParams(); 
              if(lp != null && mFront != null) {
                // Aurora xuyong 2014-05-23 modified for density-modify start
                // Aurora xuyong 2014-05-23 modified for density-modify end
                  lp.height = mItemHeight;
                   view.setLayoutParams(lp);
                // Aurora xuyong 2014-05-23 modified for density-modify start
                   mFront.height = mFrontHeight;
                // Aurora xuyong 2014-05-23 modified for density-modify end
                   front.setLayoutParams(mFront);
                   LinearLayout linear = holder.linear;
                   linear.setAlpha(255);
              }
            // Aurora xuyong 2014-04-09 modified for aurora's new feature end
            return;
        }
        //Gionee <gaoj> <2013-05-13> added for CR00811367 end
        
         //gionee gaoj 2012-3-22 added for CR00555790 start
        if (MmsApp.mGnMessageSupport) {
            if (!mIsScrolling) {
                
                ConversationListItemData ch = new ConversationListItemData(context, conv);

                // gionee zhouyj 2012-07-31 modify for CR00662942 start 
                // gionee zhouyj 2012-10-12 add for CR00711214 start 
                headerView.setCheckBoxVisibility(mIsShowCheckBox);
                // gionee zhouyj 2012-10-12 add for CR00711214 end 
                headerView.bind(context, ch, conv, mSelectionManager, cursor.getPosition());
                // gionee zhouyj 2012-07-31 modify for CR00662942 end 
            } else {
                headerView.bindDefault();
            }
        } else {
        //gionee gaoj 2012-3-22 added for CR00555790 end
        if (!mIsScrolling) {
              // Aurora liugj 2013-11-18 modified for aurora's new feature start
            headerView.bind(context, conv, mIsShowCheckBox);
              // Aurora liugj 2013-11-18 modified for aurora's new feature end
        } else {
            headerView.bindDefault();
        }
        //gionee gaoj 2012-3-22 added for CR00555790 start
        }
        //gionee gaoj 2012-3-22 added for CR00555790 end
    }

    public void onMovedToScrapHeap(View view) {
          // Aurora liugj 2013-10-11 modified for aurora's new feature start
//        ConversationListItem headerView = (ConversationListItem)view;
        RelativeLayout front = (RelativeLayout)view.findViewById(com.aurora.R.id.aurora_listview_front);
        ConversationListItem headerView = (ConversationListItem) front.getChildAt(0);
        // Aurora liugj 2013-10-11 modified for aurora's new feature end

        headerView.unbind();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        if (LOCAL_LOGV) Log.v(TAG, "inflating new view");
         // Aurora liugj 2013-10-11 modified for aurora's new feature start
        /*//gionee gaoj 2012-3-22 added for CR00555790 start
        if (MmsApp.mGnMessageSupport) {
            // Aurora liugj 2013-09-13 modified for aurora's new feature start
            return mFactory.inflate(R.layout.aurora_conversation_list_item, parent, false);
            // Aurora liugj 2013-09-13 modified for aurora's new feature end
        }
        //gionee gaoj 2012-3-22 added for CR00555790 end
        return mFactory.inflate(R.layout.conversation_list_item, parent, false);*/
        View view = (View) mFactory.inflate(com.aurora.R.layout.aurora_slid_listview, parent, false);
         // Aurora liugj 2013-10-18 modified for aurora's new feature start
        //view.setMinimumHeight(context.getResources().getDimensionPixelSize(R.dimen.list_item_height));
        //RelativeLayout back = (RelativeLayout)view.findViewById(com.aurora.R.id.aurora_listview_back);
        RelativeLayout front = (RelativeLayout)view.findViewById(com.aurora.R.id.aurora_listview_front);
         // Aurora liugj 2014-01-22 modified for list item margin start
        // Aurora xuyong 2014-04-02 deleted for aurora's new feature start
        //front.getLayoutParams().height = context.getResources().getDimensionPixelSize(R.dimen.list_item_height);
        // Aurora xuyong 2014-04-02 deleted for aurora's new feature end
          // Aurora liugj 2013-12-30 modified for AuroraCheckBox topMargin start
        AuroraCheckBox mCheckBox = (AuroraCheckBox) view.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
        // Aurora xuyong 2014-04-02 modified for aurora's new feature start
        // Aurora xuyong 2014-05-23 modified for density-modify start
        // Aurora xuyong 2014-08-01 added for bug #7092 start
        ((LayoutParams)mCheckBox.getLayoutParams()).topMargin = (int)(mContext.getResources().getDimension(com.aurora.R.dimen.aurora_list_cb_doubleline_margintop));
        // Aurora xuyong 2014-08-01 added for bug #7092 end
        // Aurora xuyong 2014-05-23 modified for density-modify end
        // Aurora xuyong 2014-04-02 modified for aurora's new feature end
         // Aurora liugj 2014-01-22 modified for list item margin end
          // Aurora liugj 2013-12-30 modified for AuroraCheckBox topMargin end
        //View backView = mFactory.inflate(R.layout.aurora_right_del_layout, back);
         // Aurora liugj 2013-10-18 modified for aurora's new feature end
        ConversationListItem frontView = (ConversationListItem) mFactory.inflate(R.layout.aurora_conversation_list_item_front, null);
        front.addView(frontView, new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

      ImageView iv = (ImageView) view.findViewById(com.aurora.R.id.aurora_listview_divider);
      if (iv != null) {
          iv.setVisibility(View.VISIBLE);
      }
      
      ConverHolder holder = new ConverHolder();
      holder.front = front;
      holder.iv = iv;
      holder.mCheckBox = mCheckBox;
      holder.frontView = frontView;
      holder.linear = (LinearLayout)view.findViewById(com.aurora.R.id.content);
      view.setTag(holder);
      
        return view;
      // Aurora liugj 2013-10-11 modified for aurora's new feature end
    }
    
    private class ConverHolder{
        public RelativeLayout front;
        public AuroraCheckBox mCheckBox;
        public ImageView iv;
        public View frontView;
        public LinearLayout linear;
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
