package com.aurora.mms.ui;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.mms.R;

import com.aurora.utils.DensityUtil;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
// Aurora xuyong 2014-07-14 added for aurora's new feature start
import android.widget.ImageView;
// Aurora xuyong 2014-07-14 added for aurora's new feature end
import android.widget.TextView;
// Aurora xuyong 2014-04-09 added for aurora's new feature start
import android.widget.LinearLayout;
// Aurora xuyong 2014-04-09 added for aurora's new feature end
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraTextView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.android.mms.data.Contact;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.MessageCursorAdapter;
import com.android.mms.ui.MessageUtils;
import gionee.provider.GnTelephony.SIMInfo;
import com.android.mms.util.GnSelectionManager;

import com.gionee.internal.telephony.GnPhone;
import gionee.provider.GnTelephony.SIMInfo;

import aurora.widget.AuroraListView;

// Aurora liugj 2013-09-25 created for aurora's new feature
// Aurora liugj 2013-09-30 modified for aurora's new feature
// Aurora liugj 2013-12-03 modified for checkbox animation
//Aurora liugj 2013-12-17 modified for Contact.UpdateListener hide
public class SimMessageListAdapter extends MessageCursorAdapter implements AbsListView.RecyclerListener/*, Contact.UpdateListener*/ {
    
    private static final String TAG = "SimMessageListAdapter";
    private static final int INDEX_COLUMN_ADDRESS = 1;
    private Context mContext;
    //private AuroraListView mListView;
    private final LayoutInflater mInflater;
    private boolean mIsShowCheckBox = false;
    private GnSelectionManager<Integer> mSelectionManager;
    private HashMap<String, String> mNameMap = new HashMap<String, String>();
    // Aurora xuyong 2014-06-05 added for aurora's multisim feature start
    private boolean mShowHeaderView = false;
    private int mLastSimId = -1;
    private boolean[] showHeader;
   // Aurora xuyong 2014-06-05 added for aurora's multisim feature end
    
    public SimMessageListAdapter(Context context, Cursor c) {
        super(context, c);
        mContext = context;
        //Contact.addListener(this);
        mInflater = LayoutInflater.from(context);
    }
    // Aurora xuyong 2014-06-05 added for aurora's multisim feature start
    public SimMessageListAdapter(Context context, Cursor c, boolean showHeaderView) {
        super(context, c);
        mContext = context;
        //Contact.addListener(this);
        mInflater = LayoutInflater.from(context);
        mShowHeaderView = showHeaderView;
        showHeader = new boolean[c.getCount()];
        initShowHeader(c);
    }
    
    private void initShowHeader(Cursor c) {
        // when the cursor is actually a matrixcursor, the pointer is not point to the index before the first
        // but really is the first.so we have to do some unusual operation here!!!!
       // Aurora xuyong 2014-06-10 added for aurora's multisim feature start
        if (c == null || c.getCount() <= 0) {
            return;
        }
       // Aurora xuyong 2014-06-10 added for aurora's multisim feature end
        int index = 1;
        int lastIdValue = -1;
        int currentIdValue = c.getInt(c.getColumnIndex("_id"));
        if (lastIdValue != currentIdValue) {
            showHeader[0] = true;
        } else {
            showHeader[0] = false;
        }
        lastIdValue = currentIdValue;
        while (c.moveToNext()) {
            currentIdValue = c.getInt(c.getColumnIndex("_id"));
            if (lastIdValue != currentIdValue) {
                showHeader[index] = true;
            } else {
                showHeader[index] = false;
            }
            lastIdValue = currentIdValue;
            index ++;
        }
        if (c != null) {
            c.moveToFirst();
        }
    }
    
    @Override
    public void changeCursor(Cursor cursor) {
       // Aurora xuyong 2014-06-12 modified for bug #5628 start
        if (cursor != null) {
            showHeader = new boolean[cursor.getCount()];
            initShowHeader(cursor);
        }
       // Aurora xuyong 2014-06-12 modified for bug #5628 end
        super.changeCursor(cursor);
    }
    
    private final int mSlot1 = 0;
    private final int mSlot2 = 1;
    // Aurora xuyong 2014-07-14 modified for aurora's new feature start
    private void setProperSImIcon(ImageView imageview, TextView textview, int slot) {
    // Aurora xuyong 2014-07-14 modified for aurora's new feature end
        int simId = -1;
        String text = null;
        switch(slot) {
        case mSlot1:
            SIMInfo info1 = SIMInfo.getSIMInfoBySlot(mContext
                    , GnPhone.GEMINI_SIM_1);
            if (info1 != null) {
                simId = (int)(info1.mSimId);
            }
            text = mContext.getResources().getString(R.string.aurora_sim1);
            break;
        case mSlot2:
            SIMInfo info2 = SIMInfo.getSIMInfoBySlot(mContext
                      , GnPhone.GEMINI_SIM_2);
              if (info2 != null) {
                  simId = (int)(info2.mSimId);
              }
              text = mContext.getResources().getString(R.string.aurora_sim2);
            break;
        }
        // Aurora xuyong 2014-07-14 modified for aurora's new feature start
        imageview.setImageResource(MessageUtils.getSimBigIcon(mContext, simId));
        textview.setText(text);
       // Aurora xuyong 2014-07-14 modified for aurora's new feature end
    }
   // Aurora xuyong 2014-06-05 added for aurora's multisim feature end
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Aurora liugj 2013-10-11 modified for aurora's new feature start
        /*if (!(view instanceof SimMessageListItem)) {
            Log.e(TAG, "Unexpected bound view: " + view);
            return;
        }*/
      // Aurora xuyong 2014-04-09 added for aurora's new feature start
      // Aurora xuyong 2014-06-05 added for aurora's multisim feature start
        if (cursor == null) {
            return;
        }
        
        LinearLayout headerUi = null;
        int height = 0;
        if (mShowHeaderView) {
            boolean needShowHeader = showHeader[cursor.getPosition()];
            headerUi = (LinearLayout) view
                    .findViewById(com.aurora.R.id.aurora_list_header);
            headerUi.removeAllViews();
            LinearLayout headerLayout = (LinearLayout)LayoutInflater.from(mContext).inflate(R.layout.aurora_multisim_manage_header, null);
            TextView headerView = (TextView)headerLayout.findViewById(R.id.aurora_multisim_header); 
          // Aurora xuyong 2014-07-14 added for aurora's new feature start
            ImageView imageView = (ImageView)headerLayout.findViewById(R.id.aurora_multisim_flag);
          // Aurora xuyong 2014-07-14 added for aurora's new feature end
            if (needShowHeader) {
             // Aurora xuyong 2014-07-14 modified for aurora's new feature start
                setProperSImIcon(imageView, headerView, cursor.getInt(cursor.getColumnIndex("_id")));
             // Aurora xuyong 2014-07-14 modified for aurora's new feature end
                headerUi.addView(headerLayout, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                headerUi.setVisibility(View.VISIBLE);
                height = headerUi.getMeasuredHeight();
                ViewGroup.LayoutParams  lp =  view.getLayoutParams(); 
                if(lp != null) {
                // Aurora xuyong 2014-08-01 added for bug #7092 start
                    lp.height = 146 +  (int)(mContext.getResources().getDimensionPixelOffset(com.aurora.R.dimen.aurora_list_doubleline_height));
                // Aurora xuyong 2014-08-01 added for bug #7092 end
                    view.setLayoutParams(lp);
                    LinearLayout linear = (LinearLayout)view.findViewById(com.aurora.R.id.content);
                    linear.setAlpha(255);
                }
            } else {
                headerUi.setVisibility(View.GONE);
                ViewGroup.LayoutParams  lp =  view.getLayoutParams(); 
                if(lp != null) {
                // Aurora xuyong 2014-08-01 added for bug #7092 start
                    lp.height = lp.height = (int)(mContext.getResources().getDimensionPixelOffset(com.aurora.R.dimen.aurora_list_doubleline_height));
                // Aurora xuyong 2014-08-01 added for bug #7092 end
                    view.setLayoutParams(lp);
                    LinearLayout linear = (LinearLayout)view.findViewById(com.aurora.R.id.content);
                    linear.setAlpha(255);
                }
            }
        } else {
            ViewGroup.LayoutParams  lp =  view.getLayoutParams(); 
            if(lp != null) {
             // Aurora xuyong 2014-08-01 added for bug #7092 start
                lp.height = lp.height = (int)(mContext.getResources().getDimensionPixelOffset(com.aurora.R.dimen.aurora_list_doubleline_height));
             // Aurora xuyong 2014-08-01 added for bug #7092 end
                view.setLayoutParams(lp);
                LinearLayout linear = (LinearLayout)view.findViewById(com.aurora.R.id.content);
                linear.setAlpha(255);
            }
        }
       // Aurora xuyong 2014-06-05 added for aurora's multisim feature end
      // Aurora xuyong 2014-04-09 added for aurora's new feature end
        RelativeLayout front = (RelativeLayout)view.findViewById(com.aurora.R.id.aurora_listview_front);
         // Aurora liugj 2013-12-11 modified for checkbox animation start
        AuroraCheckBox mCheckBox = (AuroraCheckBox) view.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
         // Aurora liugj 2013-12-11 modified for checkbox animation end
        SimMessageListItem frontView = (SimMessageListItem) front.getChildAt(0);
        
        if (!mIsScrolling) {
            if (mIsShowCheckBox) {
                // Aurora liugj 2014-01-09 modified for checkbox animation start
                // Aurora liugj 2014-01-07 modified for allcheck animation start
             // when the number of this child of this parent view is only 1, 
             // so the checkbox won't show here!
             // Aurora xuyong 2014-06-11 modified for upper reason start
                if (mAllShowCheckBox == 1 && this != null && this.getCount() > 1) {
             // Aurora xuyong 2014-06-11 modified for upper reason end
                    mCheckBox.auroraSetChecked(true, true);
                    // Aurora xuyong 2014-09-15 deleted for bug #8289 start
                    //return;
                    // Aurora xuyong 2014-09-15 deleted for bug #8289 end
                }else if (mAllShowCheckBox == 2) {
                    mCheckBox.auroraSetChecked(false, true);
                    // Aurora xuyong 2014-09-15 deleted for bug #8289 start
                    //return;
                    // Aurora xuyong 2014-09-15 deleted for bug #8289 end
                }
                // Aurora liugj 2014-01-07 modified for allcheck animation end
                
                // Aurora liugj 2013-12-10 modified for aurora's new feature start
                // Aurora liugj 2013-12-12 modified for hide dateView anim start
                if (mCheckBox.getAlpha() == 0.0f) {
                // Aurora liugj 2014-01-16 modified for checkbox animation end
                    if (mNeedAnim) {
                        // Aurora liugj 2013-12-09 modified for aurora's new feature start
                        aurora.widget.AuroraListView.auroraStartCheckBoxAppearingAnim(front, mCheckBox, true);
                        /*ObjectAnimator anim = ObjectAnimator.ofInt(frontView.mDateView, "left", frontView.mDateView.getLeft(), frontView.mDateView.getLeft() -82);
                        anim.setDuration(300);
                        anim.start();*/
                    }else {
                        aurora.widget.AuroraListView.auroraSetCheckBoxVisible(front, mCheckBox, true);
                    }
                    // Aurora liugj 2013-12-12 modified for hide dateView anim end
                    //mCheckBox.setVisibility(View.VISIBLE);
                    // Aurora liugj 2013-12-09 modified for aurora's new feature end
                    //frontView.setPadding(0, context.getResources().getDimensionPixelSize(R.dimen.list_item_top_margin), context.getResources().getDimensionPixelSize(com.aurora.R.dimen.aurora_subtitle_padding_right), context.getResources().getDimensionPixelSize(R.dimen.list_item_bottom_margin));
                }
                // Aurora liugj 2014-01-16 modified for allcheck animation start
                if (mSelectionManager != null) {
                    mCheckBox.setChecked(mSelectionManager.isSelected(cursor.getPosition()));
                    //mCheckBox.setVisibility(View.VISIBLE);
                }
            } else {
                // Aurora liugj 2014-01-09 modified for checkbox animation start
                if (mCheckBox.getAlpha() == 1.0f){
                    aurora.widget.AuroraListView.auroraStartCheckBoxDisappearingAnim(front, mCheckBox);
                    mCheckBox.setChecked(false);
                }
                /*mCheckBox.setVisibility(View.GONE);
                mCheckBox.setChecked(false);*/
                // Aurora liugj 2014-01-09 modified for checkbox animation end
                //frontView.setPadding(context.getResources().getDimensionPixelSize(com.aurora.R.dimen.aurora_subtitle_padding_left), context.getResources().getDimensionPixelSize(R.dimen.list_item_top_margin), context.getResources().getDimensionPixelSize(com.aurora.R.dimen.aurora_subtitle_padding_right), context.getResources().getDimensionPixelSize(R.dimen.list_item_bottom_margin));
            }
            // Aurora liugj 2013-12-10 modified for aurora's new feature end
            
            //String address = cursor.getString(cursor.getColumnIndex("address"));
            String address = cursor.getString(INDEX_COLUMN_ADDRESS);
            String name = formatName(address);
            // Aurora liugj 2013-11-18 modified for aurora's new feature start
            frontView.bind(context, cursor, name, mIsShowCheckBox);
            // Aurora liugj 2013-11-18 modified for aurora's new feature end
        } else {
            frontView.bindDefault();
            if (mIsShowCheckBox && mSelectionManager != null) {
                mCheckBox.setChecked(mSelectionManager.isSelected(cursor.getPosition()));
            }
            // Aurora liugj 2014-01-07 modified for allcheck animation end
        }
        // Aurora liugj 2013-10-11 modified for aurora's new feature end
    }
    // Aurora xuyong 2014-06-05 added for aurora's multisim feature start
    public void setShowHeaderView(boolean show) {
        mShowHeaderView = show;
    }
   // Aurora xuyong 2014-06-05 added for aurora's multisim feature end
    // Aurora liugj 2014-01-22 modified for list item margin start
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Aurora liugj 2013-10-11 modified for aurora's new feature start
        View view = (View) mInflater.inflate(com.aurora.R.layout.aurora_slid_listview, parent, false);
        // Aurora liugj 2013-10-18 modified for aurora's new feature start
        //RelativeLayout back = (RelativeLayout)view.findViewById(com.aurora.R.id.aurora_listview_back);
        RelativeLayout front = (RelativeLayout)view.findViewById(com.aurora.R.id.aurora_listview_front);
        //View backView = mInflater.inflate(R.layout.aurora_right_del_layout, back);
        // Aurora liugj 2013-10-18 modified for aurora's new feature end
        SimMessageListItem frontView = (SimMessageListItem)mInflater.inflate(R.layout.aurora_sim_list_item_front, null);
        // Aurora liugj 2013-11-04 added for aurora's new feature start
        frontView.setMinimumHeight(context.getResources().getDimensionPixelSize(R.dimen.list_item_height));
        // Aurora liugj 2013-12-30 modified for AuroraCheckBox topMargin start
        AuroraCheckBox mCheckBox = (AuroraCheckBox) view.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
        // Aurora xuyong 2014-06-05 modified for aurora's multisim feature start
        // Aurora xuyong 2014-08-01 added for bug #7092 start
        ((LayoutParams)mCheckBox.getLayoutParams()).topMargin = (int)(mContext.getResources().getDimension(com.aurora.R.dimen.aurora_list_cb_doubleline_margintop));
        // Aurora xuyong 2014-08-01 added for bug #7092 end
        // Aurora xuyong 2014-06-05 modified for aurora's multisim feature end
        // Aurora liugj 2013-12-30 modified for AuroraCheckBox topMargin end
        // Aurora liugj 2013-11-04 added for aurora's new feature end
        front.addView(frontView, new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        return view;
        // Aurora liugj 2013-10-11 modified for aurora's new feature end
    }
    // Aurora liugj 2014-01-22 modified for list item margin end

    @Override
    public void onMovedToScrapHeap(View view) {
        // Aurora liugj 2013-10-11 modified for aurora's new feature start
        RelativeLayout front = (RelativeLayout)view.findViewById(com.aurora.R.id.aurora_listview_front);
        SimMessageListItem frontView = (SimMessageListItem) front.getChildAt(0);
        frontView.unbind();
        // Aurora liugj 2013-10-11 modified for aurora's new feature end
    }
    
    public void setSelectionManager(GnSelectionManager<Integer> selectionManager) {
        mSelectionManager = selectionManager;
    }
    
    public void showCheckBox(boolean show) {
        mIsShowCheckBox = show;
    }
    
    private String formatName(String address) {
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
            } else {
             // Aurora xuyong 2014-09-11 modified for bug #8251 start
                // we need assign a new value to these result which has no address, here we assign 2 to them;
                contantName = new StringBuffer("2");//new StringBuffer(mContext.getString(android.R.string.unknownName));
             // Aurora xuyong 2014-09-11 modified for bug #8251 end
            }
            mNameMap.put(address, contantName.toString());
        }
        return contantName.toString();
    }

    /*@Override
    public void onUpdate(Contact updated) {
        if (mNameMap.containsKey(updated.getNumber())) {
            mNameMap.remove(updated.getNumber());
        }
    }*/
    
    public void destroy() {
        //Contact.removeListener(this);
        mNameMap.clear();
        mNameMap = null;
    }
}
