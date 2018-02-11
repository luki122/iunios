package com.android.phone;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import aurora.preference.*;
import aurora.app.*;
import aurora.widget.*;
import com.android.internal.telephony.Phone;
import android.widget.Toast;
//aurora add liguangyu 20131113

    public class dataRoamSwitchPreference extends AuroraSwitchPreference {
    	
    	Context mContext;
    	Phone mPhone;
    	AuroraSwitch mAuroraSwitch;
    	
    	OnCheckedChangeListener mListener = new OnCheckedChangeListener() {       		
     		@Override
     		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
     	        Log.d("dataRoamSwitchPreference", "onCheckedChanged: preference == dataRoamSwitchPreference,ischecked= " + isChecked);
                if (isChecked) {
                    Toast.makeText(mContext, R.string.roaming_warning, Toast.LENGTH_LONG).show();
                    mPhone.setDataRoamingEnabled(true);
                } else {
                    mPhone.setDataRoamingEnabled(false);
                }
            }         	
     	};
     	
    	
        public dataRoamSwitchPreference(Context context) {
            this(context, null);
        }

        public dataRoamSwitchPreference(Context context, AttributeSet attrs) {           
        	this(context, attrs, com.android.internal.R.attr.switchPreferenceStyle);
        }

        public dataRoamSwitchPreference(Context context, AttributeSet attrs, int defStyle) {          
            super(context, attrs, defStyle);
            mPhone = PhoneGlobals.getPhone();
        	mContext = context;
        }        
        
        void setListener (OnCheckedChangeListener l) {
        	mListener = l; 	
        }
        
        @Override
        protected void onBindView(View view) {
            super.onBindView(view);        
            mAuroraSwitch  = (AuroraSwitch)view.findViewById(com.aurora.R.id.aurora_switchWidget);        
            mAuroraSwitch.setOnCheckedChangeListener(mListener);
        }

    }
    