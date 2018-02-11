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

package com.android.settings.inputmethod;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import android.content.Context;
import android.content.Intent;
import aurora.preference.AuroraCheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
// gionee wangyaohui 2012825 add for CR00681083 begin 
import com.android.settings.GnSettingsUtils;
import android.content.Context;
// gionee wangyaohui 2012825 add for CR00681083 end 

public class CheckBoxAndSettingsPreference extends AuroraCheckBoxPreference {
    private static final float DISABLED_ALPHA = 0.4f;
    //Gionee <chenml> <2013-07-12> modify for CR00835008 begin
    private static final float ENABLED_ALPHA = 1.0f;
    //Gionee <chenml> <2013-07-12> modify for CR00835008 end
    private SettingsPreferenceFragment mFragment;
    private TextView mTitleText;
    private TextView mSummaryText;
    private ImageView mSettingsButton;
    private Intent mSettingsIntent;
    // gionee wangyaohui 2012825 add for CR00681083 begin 
    private View mSeparatorView = null;
    private Context mContext = null;
    // gionee wangyaohui 2012825 add for CR00681083 end 

    public CheckBoxAndSettingsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_inputmethod);
        setWidgetLayoutResource(R.layout.preference_inputmethod_widget);
	// gionee wangyaohui 2012825 add for CR00681083 begin 	
	mContext = context;    	
	// gionee wangyaohui 2012825 add for CR00681083 end 
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        View textLayout = view.findViewById(R.id.inputmethod_pref);
        textLayout.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        onCheckBoxClicked();
                    }
                });

        mSettingsButton = (ImageView) view.findViewById(R.id.inputmethod_settings);
        mTitleText = (TextView)view.findViewById(android.R.id.title);
        mSummaryText = (TextView)view.findViewById(android.R.id.summary);
        mSettingsButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View clickedView) {
                        onSettingsButtonClicked();
                    }
                });
        enableSettingsButton();
	// gionee wangyaohui 2012825 add for CR00681083 begin 
	mSeparatorView = view.findViewById(R.id.gn_separator);	
	if (null != mSeparatorView) {
		// Gionee fangbin 20120907 modified for CR00688796 start
		if (GnSettingsUtils.getThemeType(mContext.getApplicationContext()).equals(GnSettingsUtils.TYPE_LIGHT_THEME)){
		// Gionee fangbin 20120907 modified for CR00688796 end
			mSeparatorView.setBackgroundResource(android.R.drawable.divider_horizontal_bright);
		} else {
	       mSeparatorView.setBackgroundResource(android.R.drawable.divider_horizontal_dark);
	    	}
	}
    	// gionee wangyaohui 2012825 add for CR00681083 end 

    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        enableSettingsButton();
    }

    public void setFragmentIntent(SettingsPreferenceFragment fragment, Intent intent) {
        mFragment = fragment;
        mSettingsIntent = intent;
    }

    protected void onCheckBoxClicked() {
        if (isChecked()) {
            setChecked(false);
        } else {
            setChecked(true);
        }
    }

    protected void onSettingsButtonClicked() {
        if (mFragment != null && mSettingsIntent != null) {
            mFragment.startActivity(mSettingsIntent);
        }
    }

    private void enableSettingsButton() {
        if (mSettingsButton != null) {
            if (mSettingsIntent == null) {
                mSettingsButton.setVisibility(View.GONE);
            } else {
                final boolean checked = isChecked();
                mSettingsButton.setEnabled(checked);
                mSettingsButton.setClickable(checked);
                mSettingsButton.setFocusable(checked);
       //Gionee <chenml> <2013-07-12> modify for CR00835008 begin
                if (!checked) {
                    mSettingsButton.setAlpha(DISABLED_ALPHA);
                }else {
                    mSettingsButton.setAlpha(ENABLED_ALPHA);
               }
       //Gionee <chenml> <2013-07-12> modify for CR00835008 end
            }
        }
        if (mTitleText != null) {
            mTitleText.setEnabled(true);
        }
        if (mSummaryText != null) {
            mSummaryText.setEnabled(true);
        }
    }
}
