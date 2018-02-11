/*
 * Copyright (C) 2010 The Android Open Source Project
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
package com.mediatek.contacts.list;

import com.android.contacts.R;


import com.android.contacts.list.OnContactsUnavailableActionListener;
import com.android.contacts.list.ProviderStatusLoader;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.simcontact.AbstractStartSIMService;
import com.mediatek.contacts.simcontact.SimCardUtils;

import android.app.Fragment;
import android.os.Bundle;
import gionee.provider.GnContactsContract.ProviderStatus;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import aurora.widget.AuroraButton;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Fragment shown when contacts are unavailable. It contains provider status
 * messaging as well as instructions for the user.
 */
public class CallLogUnavailableFragment extends Fragment implements OnClickListener {
    private static final String TAG = "CallLogUnavailableFragment";
    private ProviderStatusLoader mProviderStatusLoader;
    private View mView;
    private TextView mMessageView;
    private TextView mSecondaryMessageView;
    
    private ProgressBar mProgress;
    

    private OnContactsUnavailableActionListener mListener;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG,"************onCreateView");
        mView = inflater.inflate(R.layout.call_log_unavailable_fragment, null);
        mMessageView = (TextView) mView.findViewById(R.id.message);
        mSecondaryMessageView = (TextView) mView.findViewById(R.id.secondary_message);
        mProgress = (ProgressBar) mView.findViewById(R.id.progress);
        update();
        return mView;
    }

    public void setOnCallLogUnavailableActionListener(
            OnContactsUnavailableActionListener callLogUnavailableFragment) {
        mListener = (OnContactsUnavailableActionListener) callLogUnavailableFragment;
    }

    public void setProviderStatusLoader(ProviderStatusLoader loader) {
        mProviderStatusLoader = loader;
    }

    public void update() {
        int providerStatus = mProviderStatusLoader.getProviderStatus();
        Log.i(TAG, "CallLogUnavailableFragment providerStatus : "+providerStatus);
        if(mDestroyed || providerStatus == ProviderStatus.STATUS_NO_ACCOUNTS_NO_CONTACTS){
            Log.i(TAG,"mDestoryed is true callLogUnavailableFragment & providerStatus : "+providerStatus);
            return;
        }
        switch (providerStatus) {
                       
            

            case ProviderStatus.STATUS_CHANGING_LOCALE:
                mMessageView.setText(R.string.locale_change_in_progress);
                mMessageView.setGravity(Gravity.CENTER_HORIZONTAL);
                mMessageView.setVisibility(View.VISIBLE);
                mProgress.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void onClick(View v) {
        // TODO Auto-generated method stub
        
    }
   
    


    public static boolean mDestroyed = false;

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        Log.i(TAG,"CallLogUnavailableFrament destory");
        mDestroyed = true;
        super.onDestroy();
    }


   
    /**
     * Set the message to be shown if no data is available for the selected tab
     *
     * @param resId - String resource ID of the message , -1 means view will not be visible
     */
    
}
