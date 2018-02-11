package com.android.phone;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import aurora.preference.*;
import aurora.app.*;
import aurora.widget.*;
//aurora add liguangyu 20131113

    public class dataEnableSwitchPreference extends AuroraSwitchPreference {
    	
    	Context mContext;
    	AuroraSwitch mAuroraSwitch;
    	
        public dataEnableSwitchPreference(Context context) {
            this(context, null);
        }

        public dataEnableSwitchPreference(Context context, AttributeSet attrs) {
        	this(context, attrs, com.android.internal.R.attr.switchPreferenceStyle);
        }

        public dataEnableSwitchPreference(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        	mContext = context;
        }        
        
        @Override
        protected void onBindView(View view) {
            super.onBindView(view);        
            mAuroraSwitch  = (AuroraSwitch)view.findViewById(com.aurora.R.id.aurora_switchWidget);        
           	mAuroraSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {       		
         		@Override
         		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
         	        Log.d("dataEnableSwitchPreference", "onCheckedChanged: preference == dataEnableSwitchPreference. isChecked = " + isChecked);
         	        if(PhoneUtils.isMultiSimEnabled()) {
             	        android.provider.Settings.Global.putInt(PhoneGlobals.getInstance().getContentResolver(),
             	                android.provider.Settings.Global.MOBILE_DATA + 0, isChecked ? 1 : 0);
             	        if(!DeviceUtils.isSupportDualData()) {
             				PhoneUtils.setPreferredDataSubscription(0);
             	        }
         	        }
//         		    ConnectivityManager cm =
//         		            (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
//         		    cm.setMobileDataEnabled(isChecked);
         	       AuroraPlatformUtils.setMobileDataEnabled(isChecked);
         		}         	
         	});              
        }
    }   