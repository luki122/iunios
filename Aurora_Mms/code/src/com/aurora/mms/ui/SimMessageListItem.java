package com.aurora.mms.ui;

import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.ui.MessageUtils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

// Aurora liugj 2013-09-25 created for aurora's new feature
// Aurora liugj 2013-11-18 modified for aurora's new feature 
public class SimMessageListItem extends RelativeLayout {
    
    private static final int INDEX_COLUMN_BODY = 3;
    private static final int INDEX_COLUMN_DATE = 4;
    private Context mContext;

    // Aurora liugj 2013-10-11 deleted for aurora's new feature start    
//    private LinearLayout mDelView;
    // Aurora liugj 2013-10-11 deleted for aurora's new feature end
    private TextView mFromView;
    private TextView mSubjectView;
    public TextView mDateView;
    // Aurora liugj 2013-12-03 modified for checkbox animation start
    //public CheckBox mCheckBox;
    // Aurora liugj 2013-12-03 modified for checkbox animation end
    
     private ColorStateList mFromColor;
    private ColorStateList mDateColor;
    private ColorStateList mSubjectColor;
    
    private Cursor mCursor;
    
    public SimMessageListItem(Context context) {
        super(context);
        mContext = context;
        mFromColor = (ColorStateList) mContext.getResources().getColorStateList(R.color.aurora_list_text_from_color);  
        mDateColor = (ColorStateList) mContext.getResources().getColorStateList(R.color.aurora_list_text_date_color); 
        mSubjectColor = (ColorStateList) mContext.getResources().getColorStateList(R.color.aurora_list_text_subject_color);  
    }
    
    public SimMessageListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mFromColor = (ColorStateList) mContext.getResources().getColorStateList(R.color.aurora_list_text_from_color);  
        mDateColor = (ColorStateList) mContext.getResources().getColorStateList(R.color.aurora_list_text_date_color); 
        mSubjectColor = (ColorStateList) mContext.getResources().getColorStateList(R.color.aurora_list_text_subject_color);  
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        
        mFromView = (TextView) findViewById(R.id.sim_list_from);
        mDateView = (TextView) findViewById(R.id.sim_list_date);
        mSubjectView = (TextView) findViewById(R.id.sim_list_subject);
        // Aurora liugj 2013-12-03 modified for checkbox animation start
        //mCheckBox = (CheckBox) findViewById(R.id.sim_list_check_box);
        // Aurora liugj 2013-12-03 modified for checkbox animation end
        
        // Aurora liugj 2013-10-11 deleted for aurora's new feature start    
//        mDelView = (LinearLayout) findViewById(R.id.back);
//        mDelView.setOnClickListener(new OnClickListener() {
//            
//            @Override
//            public void onClick(View v) {
//                new Thread(new Runnable() {
//                    public void run() {
//                        AuroraManageSimMessages.mSimMessages.deleteFromSim(mCursor);
//                    }
//                }, "AuroraManageSimMessages").start();
//            }
//            
//        });
         // Aurora liugj 2013-10-11 deleted for aurora's new feature end
    }
    
    // Aurora liugj 2013-09-30 modified for aurora's new feature start
    // Aurora liugj 2013-12-20 modified for list scroll optimize start
    public void bind(Context context, Cursor cursor, String name, boolean isChecked) {
        mCursor = cursor;
//        String address = cursor.getString(cursor
//                .getColumnIndexOrThrow("address"));
        //String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
        //Long date = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
        String body = cursor.getString(INDEX_COLUMN_BODY);
        Long date = cursor.getLong(INDEX_COLUMN_DATE);

        // From.
        mFromView.setVisibility(VISIBLE);
        mFromView.setText(name);
                
        mDateView.setVisibility(VISIBLE);
        mDateView.setText(MessageUtils.formatAuroraTimeStampString(context,
                date, true));

        mSubjectView.setVisibility(VISIBLE);
        mSubjectView.setText(body);
        
        if (isChecked) {
            mFromView.setTextColor(mContext.getResources().getColor(R.color.aurora_text_color_title)); 
            mDateView.setTextColor(mContext.getResources().getColor(R.color.aurora_text_color_date));
            mSubjectView.setTextColor(mContext.getResources().getColor(R.color.aurora_text_color_subject));
        }else {
            if (mFromColor != null) {
                mFromView.setTextColor(mFromColor);
            }
            if (mDateColor != null) {
                mDateView.setTextColor(mDateColor);
            }
            if (mSubjectColor != null) {
                mSubjectView.setTextColor(mSubjectColor);
            }
        }

    }
    // Aurora liugj 2013-12-20 modified for list scroll optimize end
    // Aurora liugj 2013-09-30 modified for aurora's new feature end
    
    /*private String formatName(String address) {
        StringBuffer contantName;
        if(!TextUtils.isEmpty(address)) {
            contantName = new StringBuffer(Contact.get(address, true).getName());
        } else {
            contantName = new StringBuffer(mContext.getString(android.R.string.unknownName));
        }
        return contantName.toString();
    }*/
    
    public void bindDefault() {
        mFromView.setText(R.string.refreshing);
        mDateView.setVisibility(View.GONE);
        mSubjectView.setVisibility(GONE);
    }
    
    public void unbind() {
        
        
    }
    
}
