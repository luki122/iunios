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

package com.android.settings.nfc;

import aurora.widget.AuroraActionBar;
import android.app.ActionBar;
import aurora.app.AuroraActivity;
import android.app.Activity;
import android.app.Fragment;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import aurora.preference.AuroraPreferenceActivity;
import android.preference.PreferenceActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import aurora.widget.AuroraSwitch;
import com.android.settings.R;

public class AndroidBeam extends Fragment
        implements CompoundButton.OnCheckedChangeListener {
    private View mView;
    private ImageView mImageView;
    private NfcAdapter mNfcAdapter;
    private AuroraSwitch mActionBarSwitch;
    private CharSequence mOldActivityTitle;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AuroraActivity activity = (AuroraActivity)getActivity();

        mActionBarSwitch = new AuroraSwitch(activity);

        if (activity instanceof AuroraPreferenceActivity) {
            AuroraPreferenceActivity preferenceActivity = (AuroraPreferenceActivity) activity;
                final int padding = activity.getResources().getDimensionPixelSize(
                        R.dimen.action_bar_switch_padding);
                mActionBarSwitch.setPadding(0, 0, padding, 0);
		//AURORA-START::delete temporarily for compile::waynelin::2013-9-14 
      		/*
                activity.getAuroraActionBar().setDisplayOptions(AuroraActionBar.DISPLAY_SHOW_CUSTOM,
                        AuroraActionBar.DISPLAY_SHOW_CUSTOM);
                activity.getAuroraActionBar().setCustomView(mActionBarSwitch, new AuroraActionBar.LayoutParams(
                        AuroraActionBar.LayoutParams.WRAP_CONTENT,
                        AuroraActionBar.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER_VERTICAL | Gravity.END));
		*/
        	//AURORA-END::delete temporarily for compile::waynelin::2013-9-14
                if (!preferenceActivity.onIsMultiPane() || preferenceActivity.onIsHidingHeaders()) {
                    mOldActivityTitle = getActivity().getTitle();
                    activity.getAuroraActionBar().setTitle(R.string.android_beam_settings_title);
                }
        }

        mActionBarSwitch.setOnCheckedChangeListener(this);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        mActionBarSwitch.setChecked(mNfcAdapter.isNdefPushEnabled());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.android_beam, container, false);
        initView(mView);
        return mView;
    }
    @Override
    public void onDestroyView() {
        AuroraActivity activity = (AuroraActivity)getActivity();

//        activity.getAuroraActionBar().setCustomView(null);
        if (mOldActivityTitle != null) {
            activity.getAuroraActionBar().setTitle(mOldActivityTitle);
        }
        super.onDestroyView();
    }

    private void initView(View view) {
        mActionBarSwitch.setOnCheckedChangeListener(this);
        mActionBarSwitch.setChecked(mNfcAdapter.isNdefPushEnabled());
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean desiredState) {
        boolean success = false;
        mActionBarSwitch.setEnabled(false);
        if (desiredState) {
            success = mNfcAdapter.enableNdefPush();
        } else {
            success = mNfcAdapter.disableNdefPush();
        }
        if (success) {
            mActionBarSwitch.setChecked(desiredState);
        }
        mActionBarSwitch.setEnabled(true);
    }
}
