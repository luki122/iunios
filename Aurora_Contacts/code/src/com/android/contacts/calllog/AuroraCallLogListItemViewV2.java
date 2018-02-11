/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.contacts.calllog;

import com.android.contacts.ContactsApplication;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.R;

import android.content.Context;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.CheckBox;
import aurora.widget.AuroraCheckBox;
/**
 * Simple value object containing the various views within a call log entry.
 */
public class AuroraCallLogListItemViewV2 extends RelativeLayout{
	public QuickContactBadge mQuickContactView;
    public TextView mName;
    public TextView mCallCount;
    public TextView mNumber;
    public TextView mArea;
    public TextView mDate;
    public TextView mDuration;
    public ImageView mCallType;
    public View mDividerBottom;
    public View mSecondaryContainer;
    public ImageView mSecondaryButton;
    public AuroraCheckBox mCheckBox;
    public ViewGroup mCheckBoxContainer;
    //aurora add liguangyu 20131108 for BUG #502 start
    public ViewGroup mNameContainer;
    //aurora add liguangyu 20131108 for BUG #502 end
    //aurora add liguangyu 20131110 start
	public ViewGroup mSecondLineContainer;
	//aurora dd liguangyu 20131110 end
    public View mDivider;
    public ImageView mSimIcon;
    public TextView mReject;
    public ImageView mSogouIcon;
    public ImageView mSogouDivider;
    public View mSogouLine;
    public View mContent;
    public ImageView mPirvateIcon;
    
    
    public AuroraCallLogListItemViewV2(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}
    
	public AuroraCallLogListItemViewV2(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.v("AuroraCallLogListItemViewV2", "onTouchEvent view");
		// TODO Auto-generated method stub
		
        //aurora change liguangyu 20140307 start
//		return super.onTouchEvent(event);
        int mTouchSoundSettings = Settings.System.getInt(ContactsApplication.getInstance().getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, 1);
        Settings.System.putInt(ContactsApplication.getInstance().getContentResolver(),Settings.System.SOUND_EFFECTS_ENABLED, 0);
        boolean result = super.onTouchEvent(event);
        Settings.System.putInt(ContactsApplication.getInstance().getContentResolver(),Settings.System.SOUND_EFFECTS_ENABLED, mTouchSoundSettings);
        return result;
        //aurora change liguangyu 20140307 end
	}

	@Override
	protected void onFinishInflate() {
	    super.onFinishInflate();
	    
//        mQuickContactView = (QuickContactBadge) findViewById(R.id.quick_contact_photo);
        mName = (TextView) findViewById(R.id.aurora_name);
        mCallCount = (TextView) findViewById(R.id.aurora_call_count);
        mNumber = (TextView) findViewById(R.id.aurora_number);
        mArea = (TextView) findViewById(R.id.aurora_area);
        mDate = (TextView) findViewById(R.id.aurora_call_date);
        mDuration = (TextView) findViewById(R.id.call_duration);
        mCallType = (ImageView) findViewById(R.id.aurora_call_type);
        mCheckBox = (AuroraCheckBox) findViewById(R.id.aurora_list_item_check_box);
        mSecondaryButton = (ImageView)findViewById(R.id.aurora_secondary_action_icon);
        mSecondaryContainer = findViewById(R.id.aurora_secondary_action_container);
        mCheckBoxContainer=(ViewGroup)findViewById(R.id.checkbox_container);
        //aurora add liguangyu 20131108 for BUG #502 start
        mNameContainer = (ViewGroup)findViewById(R.id.namecount);
        //aurora add liguangyu 20131108 for BUG #502 end
		//aurora change liguangyu 20131110 start
		mSecondLineContainer = (ViewGroup) findViewById(R.id.sencond_line_container);
		//aurora change liguangyu 20131110 end
		mDivider = findViewById(R.id.aurora_calllog_divider);
		mSimIcon = (ImageView) findViewById(R.id.aurora_sim_icon);
		mReject = (TextView) findViewById(R.id.aurora_reject_note);
		mSogouIcon = (ImageView) findViewById(R.id.sogou_icon);
		mSogouDivider = (ImageView) findViewById(R.id.aurora_sogou_divider);
		mSogouLine = findViewById(R.id.aurora_sogou_line);
		mContent = findViewById(R.id.content);
        mPirvateIcon = (ImageView)findViewById(R.id.aurora_private_icon);
	}

	

	public static AuroraCallLogListItemViewV2 create(Context context) {
		if(GNContactsUtils.isMultiSimEnabled()){
			return (AuroraCallLogListItemViewV2) View.inflate(context, R.layout.aurora_call_log_list_item_v2, null);
		}else{
			return (AuroraCallLogListItemViewV2) View.inflate(context, R.layout.aurora_call_log_list_item_v1, null);
		}
	    
	}

	
	public void switch2SelectionUi(boolean inSelectionMode) {
	    mCheckBox.setVisibility(inSelectionMode ? VISIBLE : GONE);
	    mSecondaryButton.setVisibility(inSelectionMode ? GONE : VISIBLE);
	}
}
