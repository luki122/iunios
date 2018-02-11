package com.android.settings;


import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.util.Log;
import aurora.preference.*;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;

public class AuroraUsbPreference extends AuroraPreference {
	
	private  RadioButton rb = null;
	private boolean mIsChecked;
	private Context mContext;



	public AuroraUsbPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
		// TODO Auto-generated constructor stub
	}

	public AuroraUsbPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
		// TODO Auto-generated constructor stub
	}

	public AuroraUsbPreference(Context context) {
		super(context);
		init(context);
		// TODO Auto-generated constructor stub
	}

	private void init(Context context) {
    	
    	setWidgetLayoutResource(R.layout.aurora_apn_widgetlayout);
        mContext = context;
    	
    }
	


	@Override
	protected void onBindView(View view) {
		// TODO Auto-generated method stub
		super.onBindView(view);
		View widget = view.findViewById(R.id.apn_radiobutton);
        if ((widget != null) && widget instanceof RadioButton) {
        	rb = (RadioButton) widget;
        	rb.setChecked(mIsChecked);
        	
          }
	}


	
	public void setChecked(boolean isChecked){
	
		mIsChecked = isChecked;
		notifyChanged();
	}


}
