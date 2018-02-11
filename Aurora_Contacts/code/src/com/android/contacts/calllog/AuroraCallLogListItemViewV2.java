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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
//import android.widget.QuickContactBadge;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.CheckBox;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraRoundedImageView;
/**
 * Simple value object containing the various views within a call log entry.
 */
public class AuroraCallLogListItemViewV2 extends RelativeLayout{
//	public QuickContactBadge mQuickContactView;
	public TextView mName;
	public TextView mCallCount;
	public TextView mArea;
	public TextView mDate;
	public ImageView mCallType;
	public CheckBox mCheckBox;
	public View mDivider;
	public ImageView mSimIcon;
	public TextView mReject;
	public AuroraRoundedImageView contactPhoto;
	public LinearLayout headerUi;
	public View expand,call_date_ll;
	public View dividerLine;
//	public TextView aurora_reject_divider;


	public AuroraCallLogListItemViewV2(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public AuroraCallLogListItemViewV2(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/*	@Override
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
	}*/

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mName = (TextView) findViewById(R.id.aurora_name);
		mCallCount = (TextView) findViewById(R.id.aurora_call_count);
		mArea = (TextView) findViewById(R.id.aurora_area);
		mDate = (TextView) findViewById(R.id.aurora_call_date);
		mCallType = (ImageView) findViewById(R.id.aurora_call_type);


//		contact_expand_layout=(RelativeLayout)findViewById(R.id.contact_expand_layout);


//		mDivider = findViewById(R.id.aurora_calllog_divider);
		mSimIcon = (ImageView) findViewById(R.id.aurora_sim_icon);
		mReject = (TextView) findViewById(R.id.aurora_reject_note);

		contactPhoto= (AuroraRoundedImageView)findViewById(R.id.contact_photo);

		expand=findViewById(R.id.expand);
		call_date_ll=findViewById(R.id.call_date_ll);
		

	}



	public static AuroraCallLogListItemViewV2 create(Context context) {
		if(ContactsApplication.isMultiSimEnabled){
			return (AuroraCallLogListItemViewV2) View.inflate(context, R.layout.aurora_call_log_list_item_v2, null);
		}else{
			return (AuroraCallLogListItemViewV2) View.inflate(context, R.layout.aurora_call_log_list_item_v1, null);
		}

	}


	public void switch2SelectionUi(boolean inSelectionMode) {
		//	    mCheckBox.setVisibility(inSelectionMode ? VISIBLE : GONE);
		//	    mSecondaryButton.setVisibility(inSelectionMode ? GONE : VISIBLE);
	}
}
