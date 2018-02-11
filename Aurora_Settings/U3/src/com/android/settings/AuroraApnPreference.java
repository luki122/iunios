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
import android.provider.Telephony;

// add by steve.tang 2014-06-11, start.
import gionee.telephony.GnTelephonyManager;
// add by steve.tang 2014-06-11, end.

public class AuroraApnPreference extends AuroraPreference implements CompoundButton.OnCheckedChangeListener{
	
	private static boolean mChecked = false;
	private  RadioButton rb = null;
	public static String mSelectedKey = null;
	public static String mLastSelectedKey = null;

	// add by steve.tang 2014-06-11, start.
	public static int sub_id = 0;
	private static final boolean MULTI_SIM_SUPPORT = GnTelephonyManager.isMultiSimEnabled();
	// add by steve.tang 2014-06-11, end.

	private String mcurrentKey =null;
	private Context mContext;
	private boolean mProtectFromCheckedChange = false;
	public static final String ACTION_UPDATE_RADIOBTN = "com.android.settings.action.UPDATE_RADIOBTN";
		
	private final BroadcastReceiver mUpdateRadioBtnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_UPDATE_RADIOBTN)) {
            	
            	String strSelectedKey = intent.getStringExtra("SELECTED_KEY");
            	if(rb != null){
            		if(mcurrentKey.equals(strSelectedKey)){
                		rb.setChecked(true);
                	}else{
                		rb.setChecked(false);
                	}
            	}           	          	
            	        	
      
            }
        }
    };

	public AuroraApnPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
		// TODO Auto-generated constructor stub
	}

	public AuroraApnPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
		// TODO Auto-generated constructor stub
	}

	public AuroraApnPreference(Context context) {
		super(context);
		init(context);
		// TODO Auto-generated constructor stub
	}

	private void init(Context context) {
    	
    	setWidgetLayoutResource(R.layout.aurora_apn_widgetlayout);
    	IntentFilter filter = new IntentFilter(ACTION_UPDATE_RADIOBTN);
        context.registerReceiver(mUpdateRadioBtnReceiver, filter);
        mContext = context;
    	
    }
	
//	public void setChecked(boolean checked) {
//        // Always persist/notify the first time; don't assume the field's default of false.
//		super.setChecked(checked);
//        final boolean changed = mChecked != checked;
//        if (changed ) {
//            mChecked = checked;
//            
//        }
//    }
	
//	@Override
//	public boolean isChecked() {
//		// TODO Auto-generated method stub
//		return super.isChecked();
//	}

	@Override
	protected void onBindView(View view) {
		// TODO Auto-generated method stub
		super.onBindView(view);
		View widget = view.findViewById(R.id.apn_radiobutton);
        if ((widget != null) && widget instanceof RadioButton) {
        	rb = (RadioButton) widget;
        	rb.setOnCheckedChangeListener(this);
//        	String str =  getKey();
//        	
//        	mChecked = str.equals(mSelectedKey);
        	boolean isChecked = getKey().equals(mSelectedKey);
            if (isChecked) {
               
                mSelectedKey = getKey();
            }

            mProtectFromCheckedChange = true;
        	
        	if(mcurrentKey.equals(mSelectedKey)){
        		rb.setChecked(true);        		 
                
        	}else{
        		rb.setChecked(false);
        	}
        	 mProtectFromCheckedChange = false;
          }
	}

	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (mProtectFromCheckedChange) {
            return;
        }
       
        if (isChecked) {
        	         
            callChangeListener(mSelectedKey);
        /*} else {            
            mSelectedKey = null;*/
        }
    }
	
	@Override
	protected void onClick() {
		// TODO Auto-generated method stub
		super.onClick();
//		mChecked = true;
		if(rb != null){			
			rb.setChecked(true);	
			
		}
		
		// set the last value 
		if(!getcurrentKey().equals(mSelectedKey) ){
			mLastSelectedKey = mSelectedKey;
		}
		
		mSelectedKey = getcurrentKey();		
		
		
		SharedPreferences apnSh = mContext.getSharedPreferences("apn",Context.MODE_PRIVATE);
		String apnId = apnSh.getString("content://telephony/carriers/" +mSelectedKey, null);		
		
		if(mSelectedKey.equals(apnId)){			
			mContext.startActivity(new Intent(Intent.ACTION_EDIT, Uri.parse("content://telephony/carriers/" +mSelectedKey)));
		}
		
		mContext.sendBroadcast(new Intent(ACTION_UPDATE_RADIOBTN).putExtra("SELECTED_KEY",mSelectedKey));
		updateSelectedApnUri(mSelectedKey);
		
		
		
	}
	
	private void updateSelectedApnUri(String key){
		ContentResolver resolver = mContext.getContentResolver();

        ContentValues values = new ContentValues();
        values.put(ApnSettings.APN_ID, key);
	// add by steve.tang 2014-06-11, for dual sim apn choose start.
		if(MULTI_SIM_SUPPORT) {
			resolver.update(Uri.parse("content://telephony/carriers/preferapn/" + sub_id), values, null, null);
		} else {
			resolver.update(Uri.parse("content://telephony/carriers/preferapn"), values, null, null);
		}
	// add by steve.tang 2014-06-11, for dual sim apn choose end.
	}
	
	
	public void setChecked(boolean isChecked){
		if(rb != null){
			if(isChecked){
				rb.setChecked(true);
				mSelectedKey = getcurrentKey();	
				updateSelectedApnUri(mSelectedKey);
				
			}else{
				rb.setChecked(false);
			}
		}
		
		
	}

	public static String getSelectedKey() {
		return mSelectedKey;
	}

	public static void setSelectedKey(String mSelectedKey) {
		AuroraApnPreference.mSelectedKey = mSelectedKey;		
	}

	// add by steve.tang 2014-06-11, set sub id, start.
	public static void setCurrentSubid(int subId) {
		AuroraApnPreference.sub_id = subId;		
	}
	// add by steve.tang 2014-06-11, set sub id, end.

	public String getcurrentKey() {
		return mcurrentKey;
	}

	public void setcurrentKey(String mcurrentKey) {
		this.mcurrentKey = mcurrentKey;
	}
	
	public void releaseResource(){
		mContext.unregisterReceiver(mUpdateRadioBtnReceiver);
	}
	
	public BroadcastReceiver getUpdateRadioBtnReceiver() {
		return mUpdateRadioBtnReceiver;
	}

}
