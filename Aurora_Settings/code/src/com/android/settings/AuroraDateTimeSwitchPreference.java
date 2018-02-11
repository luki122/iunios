package com.android.settings;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.content.res.TypedArray;
import android.widget.Checkable;
import aurora.widget.AuroraSwitch;
import aurora.preference.AuroraSwitchPreference;

public class AuroraDateTimeSwitchPreference extends AuroraSwitchPreference{
	private AuroraSwitch m24HourAuroraSwitch;
	private Context mContext = null ;
	private static final String HOURS_12 = "12";
    private static final String HOURS_24 = "24";

	public AuroraDateTimeSwitchPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		 mContext = context;
	}

	public AuroraDateTimeSwitchPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		 mContext = context;
	}

	public AuroraDateTimeSwitchPreference(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		 mContext = context;
	}
	
	private boolean is24Hour() {
        return DateFormat.is24HourFormat(mContext);
    }
	
	 @Override
	    protected void onBindView(View view) {
	        super.onBindView(view);
	        m24HourAuroraSwitch  = (AuroraSwitch)view.findViewById(com.aurora.R.id.aurora_switchWidget);
	        if(m24HourAuroraSwitch  == null){
	        	Log.i("qy", "m24HourAuroraSwitch  == null");
	        }else{
	        	Log.i("qy", "m24HourAuroraSwitch  != null");
	        }
	        
	        if(m24HourAuroraSwitch != null){
	        	m24HourAuroraSwitch.setChecked(is24Hour());
	        	m24HourAuroraSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	        		
	        		@Override
	        		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	        			// TODO Auto-generated method stub
	        			
      			
	        			
	        			Settings.System.putString(mContext.getContentResolver(),
        		                Settings.System.TIME_12_24,
        		                isChecked? HOURS_24 : HOURS_12); 
	        			 mContext.sendBroadcast(new Intent(DateTimeSettings.ACTION_UPDATE_DATETIME));


	        			 
	        			                			
	        		}

	        		
	        	});
	        }
	 }
	 
	 protected void onClick() {
	        super.onClick();


	        if (!callChangeListener(!m24HourAuroraSwitch.isChecked())) {
	            return;
	        }
	        if(m24HourAuroraSwitch != null){
	        	m24HourAuroraSwitch.setChecked(!m24HourAuroraSwitch.isChecked());

	        }

	    }

}
