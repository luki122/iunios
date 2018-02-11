/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.settings;

import com.android.internal.telephony.PhoneConstants;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
//import android.preference.Preference;
import aurora.preference.*;
import android.provider.Telephony;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

public class ApnPreference extends AuroraPreference implements
        CompoundButton.OnCheckedChangeListener, OnClickListener {
    final static String TAG = "ApnPreference";

    public ApnPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWidgetLayoutResource(R.layout.aurora_apn_widgetlayout);
    }

    public ApnPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.apnPreferenceStyle);
    }

    public ApnPreference(Context context) {
        this(context, null);
    }

    private static String mSelectedKey = null;
    private static CompoundButton mCurrentChecked = null;
    private boolean mProtectFromCheckedChange = false;
    private boolean mSelectable = true;
    private boolean mEditable = true;
    private int mSubId;
  
    private View mWidget;
    private RadioButton mRadioButton;
    
    @Override
	protected void onBindView(View view) {
		// TODO Auto-generated method stub
		super.onBindView(view);
		mWidget = view.findViewById(R.id.aurora_apn_radiobutton);
	}
    
    @Override
    public View getView(View convertView, ViewGroup parent) {
        View view = super.getView(convertView, parent);

        //View widget = view.findViewById(R.id.apn_radiobutton);
        if ((mWidget != null) && mWidget instanceof RadioButton) {
        	mRadioButton = (RadioButton) mWidget;
            if (mSelectable) {
            	mRadioButton.setOnCheckedChangeListener(this);

                boolean isChecked = getKey().equals(mSelectedKey);
                if (isChecked) {
                    mCurrentChecked = mRadioButton;
                    mSelectedKey = getKey();
                }

                mProtectFromCheckedChange = true;
                mRadioButton.setChecked(isChecked);
                mProtectFromCheckedChange = false;
                mRadioButton.setVisibility(View.VISIBLE);
            } else {
            	mRadioButton.setVisibility(View.GONE);
            }
        }

        View textLayout = view.findViewById(R.id.text_layout);
        if ((textLayout != null) && textLayout instanceof RelativeLayout) {
            textLayout.setOnClickListener(this);
        }

        return view;
    }

    public boolean isChecked() {
        return getKey().equals(mSelectedKey);
    }

    public void setChecked() {
        mSelectedKey = getKey();
    }

    public void setChecked(boolean isChecked){
		if(mRadioButton != null){
			if(isChecked){
				mRadioButton.setChecked(true);
			}else{
				mRadioButton.setChecked(false);
			}
		}
	}
    
    public void startApnEditor(int pos){
    	  Context context = getContext();
    	  if(context != null){
			Uri url = ContentUris.withAppendedId(
					Telephony.Carriers.CONTENT_URI, pos);
			Intent it = new Intent(Intent.ACTION_EDIT, url);
			it.putExtra("readOnly", !mEditable);
			it.putExtra("sub_id", mSubId);
			context.startActivity(it);
          }
    }
    
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "ID: " + getKey() + " :" + isChecked);
        if (mProtectFromCheckedChange) {
            return;
        }

        if (isChecked) {
            if (mCurrentChecked != null) {
                mCurrentChecked.setChecked(false);
            }
            mCurrentChecked = buttonView;
            mSelectedKey = getKey();
            callChangeListener(mSelectedKey);
        } else {
            mCurrentChecked = null;
            mSelectedKey = null;
        }
    }

    public void onClick(android.view.View v) {
        if ((v != null) && (R.id.text_layout == v.getId())) {
            Context context = getContext();
            if (context != null) {
                int pos = Integer.parseInt(getKey());
                Uri url = ContentUris.withAppendedId(Telephony.Carriers.CONTENT_URI, pos);
                Intent it = new Intent(Intent.ACTION_EDIT, url);
                it.putExtra("readOnly", !mEditable);
                it.putExtra("sub_id", mSubId);
                context.startActivity(it);
            }
        }
    }

    public void setSelectable(boolean selectable) {
        mSelectable = selectable;
    }

    public boolean getSelectable() {
        return mSelectable;
    }

    public int getSubId() {
        return mSubId;
    }

    public void setSubId(int subId) {
        mSubId = subId;
    }

    public void setApnEditable(boolean isEditable) {
        mEditable = isEditable;
    }
}
