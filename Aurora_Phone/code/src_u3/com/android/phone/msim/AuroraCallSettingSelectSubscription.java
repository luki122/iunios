/*
 * Copyright (c) 2010-2013, The Linux Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     * Neither the name of The Linux Foundation, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.android.phone;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.telephony.MSimTelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.TabActivity;
import aurora.preference.AuroraPreferenceFragment;
import aurora.widget.*;
import aurora.app.*;

import static com.android.internal.telephony.MSimConstants.SUBSCRIPTION_KEY;

public class AuroraCallSettingSelectSubscription extends  AuroraActivity {

    private static final String LOG_TAG = "AuroraCallSettingSelectSubscription";
    private static final boolean DBG = (PhoneGlobals.DBG_LEVEL >= 2);

    public static final String PACKAGE = "PACKAGE";
    public static final String TARGET_CLASS = "TARGET_CLASS";

    private String[] tabLabel = {"SUB 1", "SUB 2", "SUB 3"};

    private TabSpec subscriptionPref;
    private FragmentManager fragmentManager;
    private int mSubscription = 0;
    private RadioGroup mRadioGroup; 

    @Override
    public void onPause() {
        super.onPause();
    }

    private AuroraPreferenceFragment[] subCallSettingsFragment;
    /*
     * Activity class methods
     */
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (DBG) log("Creating activity");

        setAuroraContentView(R.layout.aurora_multi_sim_setting);
        getAuroraActionBar().setTitle(R.string.callind_multi_sim);
//        TabHost tabHost = getTabHost();

        Intent intent =  getIntent();
        String pkg = intent.getStringExtra(PACKAGE);
        String targetClass = intent.getStringExtra(TARGET_CLASS);

        int numPhones = MSimTelephonyManager.getDefault().getPhoneCount();

        for(int i = 0; i<tabLabel.length; i++) {
        	int resID = getResources().getIdentifier("sub_" + (i + 1),
					"string", "com.android.phone");
        	tabLabel[i] = getResources().getString(resID);
        }

        subCallSettingsFragment = new AuroraPreferenceFragment[numPhones];
		fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        
        for (int i = 0; i < numPhones; i++) {
            log("Creating SelectSub activity = " + i);
//            subscriptionPref = tabHost.newTabSpec(tabLabel[i]);
//            subscriptionPref.setIndicator(tabLabel[i]);
//            intent = new Intent().setClassName(pkg, targetClass)
//                    .setAction(intent.getAction()).putExtra(SUBSCRIPTION_KEY, i);
//            subscriptionPref.setContent(intent);
//            tabHost.addTab(subscriptionPref);            
            subCallSettingsFragment[i] = new MSimCallFeaturesSubSetting(i);  
            transaction.replace(R.id.content, subCallSettingsFragment[i], "" + i);
        }        
        transaction.commit();
        
        mRadioGroup = (RadioGroup) findViewById(R.id.rg_tab);  
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {  
            @Override  
            public void onCheckedChanged(RadioGroup group, int checkedId) {  
                FragmentTransaction transaction = fragmentManager.beginTransaction();  
                transaction.replace(R.id.content, subCallSettingsFragment[checkedId == R.id.rg_tab_1 ? 0 : 1]);  
                transaction.commit();  
            }  
        });  
        mRadioGroup.check(R.id.rg_tab_1);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }

    
  @Override
  protected void onPrepareDialog(int id, Dialog dialog) {
      super.onPrepareDialog(id, dialog);
      ((MSimCallFeaturesSubSetting)subCallSettingsFragment[mSubscription]).onPrepareDialog(id, dialog);
  }

  // dialog creation method, called by showDialog()
  @Override
  protected Dialog onCreateDialog(int id) {
	  return  ((MSimCallFeaturesSubSetting)subCallSettingsFragment[mSubscription]).onCreateDialog(id);
  }
}
