package com.android.phone;

import android.content.Context;
import android.content.res.TypedArray;
import android.net.ConnectivityManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import aurora.preference.*;
import aurora.app.*;
import aurora.widget.*;


    public class MSimEnableSwitchPreference extends AuroraSwitchPreference {
    	
    	Context mContext;
        private int mSubscription = 0;    
        AuroraSwitch mAuroraSwitch;
    	
    	OnCheckedChangeListener mListener = new OnCheckedChangeListener() {       		
     		@Override
     		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
     	        Log.d("MSimEnableSwitchPreference", "onCheckedChanged: preference == dataRoamSwitchPreference,ischecked= " + isChecked);
     	        
            }         	
     	};
    	
        public MSimEnableSwitchPreference(Context context) {
            this(context, null);
        }

        public MSimEnableSwitchPreference(Context context, AttributeSet attrs) {
        	this(context, attrs, com.android.internal.R.attr.switchPreferenceStyle);
        }

        public MSimEnableSwitchPreference(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        	mContext = context;
        	TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.sim_icon_slot); 
    		mSubscription = a.getInt(R.styleable.sim_icon_slot_icon_slot, 0); 				
    		a.recycle(); 
        }        
        
        void setListener (OnCheckedChangeListener l) {
        	mListener = l; 	
        	if(mAuroraSwitch != null) {
               	mAuroraSwitch.setOnCheckedChangeListener(l);    
        	}
        }
        
        @Override
        protected void onBindView(View view) {
            super.onBindView(view);        
 	        Log.d("MSimEnableSwitchPreference", "onBindView: ");
            mAuroraSwitch  = (AuroraSwitch)view.findViewById(com.aurora.R.id.aurora_switchWidget);        
           	mAuroraSwitch.setOnCheckedChangeListener(mListener);              
        }
    }   