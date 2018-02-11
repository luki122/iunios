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

package com.aurora.callsetting;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.provider.Telephony;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import com.android.internal.telephony.Phone;
import aurora.preference.*;
import aurora.widget.*;
import aurora.app.*;

public class PreNetPreference extends AuroraPreference implements
        CompoundButton.OnCheckedChangeListener, OnClickListener {
    final static String TAG = "PreNetPreference";

    
    RadioButton rb;
    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public PreNetPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * @param context
     * @param attrs
     */
    public PreNetPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * @param context
     */
    public PreNetPreference(Context context) {
        super(context);
        init();
    }

    private static String mSelectedKey = null;
    private static CompoundButton mCurrentChecked = null;
    private boolean mProtectFromCheckedChange = false;
    
    
    public static String getSelectedKey() {
    	return mSelectedKey;
    }

    @Override
    public View getView(View convertView, ViewGroup parent) {
        View view = super.getView(convertView, parent);

        View widget = view.findViewById(R.id.pre_net_radiobutton);
        if ((widget != null) && widget instanceof RadioButton) {
            Log.i(TAG, "ID: getview");
            rb = (RadioButton) widget;          
            rb.setOnCheckedChangeListener(this);
            rb.setFocusable(false);
            boolean isChecked = getKey().equals(mSelectedKey);
            if (isChecked) {
                mCurrentChecked = rb;
                mSelectedKey = getKey();
            }

            mProtectFromCheckedChange = true;
            rb.setChecked(isChecked);
            mProtectFromCheckedChange = false;

        }

        View textLayout = view.findViewById(R.id.main);
        if ((textLayout != null) && textLayout instanceof LinearLayout) {
            textLayout.setOnClickListener(this);
        }

        return view;
    }

    private void init() {
        setLayoutResource(R.layout.pre_net_preference_layout);
    }

    public boolean isChecked() {
        return getKey().equals(mSelectedKey);
    }

    public void setChecked() {
        Log.i(TAG, "setChecked");
        mSelectedKey = getKey();
        //aurora add liguangyu 20140424 for BUG #4556 start
        if(rb != null) {
        	rb.setChecked(true);
        }
        //aurora add liguangyu 20140424 for BUG #4556 end
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.i(TAG, "ID: " + getKey() + " :" + isChecked);
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
        Log.i(TAG, " onClick ");
        if ((v != null) && (R.id.main == v.getId())) {
        	if(mCurrentChecked != rb) {
        		rb.setChecked(true);
        	}            
        }
    }

    
}