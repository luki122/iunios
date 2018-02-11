/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.phone;


import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DialerKeyListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.android.phone.Constants;
//import com.android.phone.common.HapticFeedback;
import com.android.phone.HapticFeedback;
import com.android.phone.PhoneGlobals;
import com.android.phone.PhoneUtils;

import android.widget.TextView;
import aurora.widget.*;
import aurora.app.*;
import static com.android.phone.AuroraMSimConstants.SUBSCRIPTION_KEY;
import com.android.phone.AuroraTelephony.SIMInfo;

/**
 * EmergencyDialer is a special dialer that is used ONLY for dialing emergency calls.
 *
 * It's a simplified version of the regular dialer (i.e. the TwelveKeyDialer
 * activity from apps/Contacts) that:
 *   1. Allows ONLY emergency calls to be dialed
 *   2. Disallows voicemail functionality
 *   3. Uses the FLAG_SHOW_WHEN_LOCKED window manager flag to allow this
 *      activity to stay in front of the keyguard.
 *
 * TODO: Even though this is an ultra-simplified version of the normal
 * dialer, there's still lots of code duplication between this class and
 * the TwelveKeyDialer class from apps/Contacts.  Could the common code be
 * moved into a shared base class that would live in the framework?
 * Or could we figure out some way to move *this* class into apps/Contacts
 * also?
 */
public class MSimIconSeclectActivity extends AuroraActivity implements View.OnClickListener{
	
	   private static final boolean DBG = true;
	    private static final String LOG_TAG = "MSimIconSeclectActivity";
	    
	    private ImageButton mSim1, mSim2, mSimNet, mSimHome, mSimOffice, mSimDial;
	    private ImageView mSim1Check, mSim2Check, mSimNetCheck, mSimHomeCheck, mSimOfficeCheck, mSimDialCheck;
	    Intent mIntent;
	    private int mSubscription = 0;
	    
	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mIntent = getIntent();
        mSubscription = AuroraPhoneUtils.getSlot(mIntent);
        setAuroraContentView(R.layout.aurora_pick_sim_icon);
        getAuroraActionBar().setTitle(R.string.sim_icon_title);
        
        mSim1 = (ImageButton)findViewById(R.id.sim_icon_1);
        mSim1.setOnClickListener(this);
        mSim2 = (ImageButton)findViewById(R.id.sim_icon_2);
        mSim2.setOnClickListener(this);
        mSimNet = (ImageButton)findViewById(R.id.sim_icon_net);
        mSimNet.setOnClickListener(this);
        mSimHome = (ImageButton)findViewById(R.id.sim_icon_home);
        mSimHome.setOnClickListener(this);
        mSimOffice = (ImageButton)findViewById(R.id.sim_icon_office);
        mSimOffice.setOnClickListener(this);
        mSimDial = (ImageButton)findViewById(R.id.sim_icon_dial);
        mSimDial.setOnClickListener(this);
        
        mSim1Check = (ImageView)findViewById(R.id.sim_icon_1_check);
        mSim2Check = (ImageView)findViewById(R.id.sim_icon_2_check);
        mSimNetCheck = (ImageView)findViewById(R.id.sim_icon_net_check);
        mSimHomeCheck = (ImageView)findViewById(R.id.sim_icon_home_check);
        mSimOfficeCheck =(ImageView) findViewById(R.id.sim_icon_office_check);
        mSimDialCheck = (ImageView)findViewById(R.id.sim_icon_dial_check);
        
        SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(PhoneGlobals.getInstance(), mSubscription);
        int checkId = -1;
        if (simInfo != null) {          
        	checkId = simInfo.mColor;
            switch (checkId) {
	            case 0: {
	            	mSim1Check.setVisibility(View.VISIBLE);
	            	break;
	            }
	            case 1: {
	             	mSim2Check.setVisibility(View.VISIBLE);
	            	break;
	            }         
	            case 2: {
	            	mSimNetCheck.setVisibility(View.VISIBLE);
	            	break;
	            }
	            case 3: {
	            	mSimHomeCheck.setVisibility(View.VISIBLE);
	            	break;
	            }
	            case 4: {
	            	mSimOfficeCheck.setVisibility(View.VISIBLE);
	            	break;
	            }         
	            case 5: {
	            	mSimDialCheck.setVisibility(View.VISIBLE);
	            	break;
	            }
            }
        }
        

      
    }
	
    private static void log(String msg) {
       if(DBG) Log.d(LOG_TAG, msg);
    }
    
    @Override
    public void onClick(View view) {
    	mSim1Check.setVisibility(View.GONE);
    	mSim2Check.setVisibility(View.GONE);
    	mSimNetCheck.setVisibility(View.GONE);
    	mSimHomeCheck.setVisibility(View.GONE);
    	mSimOfficeCheck.setVisibility(View.GONE);
    	mSimDialCheck.setVisibility(View.GONE);
    	int resId = SimIconUtils.getIconResFromViewId(view.getId());
        switch (view.getId()) {
            case R.id.sim_icon_1: {
            	mSim1Check.setVisibility(View.VISIBLE);
            	break;
            }
            case R.id.sim_icon_2: {
             	mSim2Check.setVisibility(View.VISIBLE);
            	break;
            }         
            case R.id.sim_icon_net: {
            	mSimNetCheck.setVisibility(View.VISIBLE);
            	break;
            }
            case R.id.sim_icon_home: {
            	mSimHomeCheck.setVisibility(View.VISIBLE);
            	break;
            }
            case R.id.sim_icon_office: {
            	mSimOfficeCheck.setVisibility(View.VISIBLE);
            	break;
            }         
            case R.id.sim_icon_dial: {
            	mSimDialCheck.setVisibility(View.VISIBLE);
            	break;
            }
        }
        if(resId != -1) {
//        	mIntent.putExtra(SUBSCRIPTION_KEY, mSubscription);
        	mIntent.putExtra("sim_icon_res", resId);
            setResult(RESULT_OK, mIntent); 
            finish();	
        }
    }
}
