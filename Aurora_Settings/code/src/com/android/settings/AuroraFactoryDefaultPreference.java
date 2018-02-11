package com.android.settings;


import java.io.File;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.os.Build;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.content.SharedPreferences;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceCategory;
import aurora.app.AuroraAlertDialog;
import android.content.DialogInterface;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import aurora.widget.AuroraCheckBox;

import com.android.internal.os.storage.ExternalStorageFormatter;

import android.view.LayoutInflater;
import android.content.res.Resources;



public class AuroraFactoryDefaultPreference extends /*SettingsPreferenceFragment*/ AuroraPreferenceCategory {

    private static final String TAG = "FactoryDefault";
    private static final String DELETE_APP = "delete_app";
    private static final String FORMAT_SD = "format_sdcard";
    
    private Context mContext;
	private View mView;
	private Button mStartFactory;
	private AuroraCheckBox mCbClearMultiData;
    private boolean mIsDeleteApp;
	private boolean mIsFormatSD;
    private String mAlterdlgSummary;
    private static final int KEYGUARD_REQUEST = 55;  
    private String mDeviceName;

	public AuroraFactoryDefaultPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
   
        mContext = context;
        setLayoutResource(R.layout.aurora_privacy_settings_category_layout);
        mDeviceName = SystemProperties.get("ro.product.name");
        if (mDeviceName.contains("IUNI")) {
        	setLayoutResource(R.layout.privacy_settings_main);
        }
	}
	
	
	@Override
	protected View onCreateView(ViewGroup parent) {
		// TODO Auto-generated method stub
		DisplayMetrics dm = new DisplayMetrics();
		dm = mContext.getResources().getDisplayMetrics();			
		int screenHeight = dm.heightPixels;	
		Log.i("qy", "screenHeight = "+screenHeight);
		RelativeLayout layout=new RelativeLayout (mContext);
		
		View factoryView = null;
		
		if (mDeviceName.contains("IUNI")) {
			factoryView =((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.privacy_settings_main, null);
        }else{
        	factoryView =((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.aurora_privacy_settings_category_layout, null);
        }
		
		layout.addView(factoryView,  new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, screenHeight - 297));
		return layout;
	}

	@Override
    protected void onBindView(View view) {
        super.onBindView(view);

		mView = view;
		mStartFactory = (Button)view.findViewById(R.id.start_factory_button);
		mStartFactory.setOnClickListener(mStartFactoryListener);
		
		
        if (mDeviceName.contains("IUNI")) {
        	mCbClearMultiData = (AuroraCheckBox)view.findViewById(R.id.cb_clear_multi_user_data);
        }
		
		/*mCbClearMultiData.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				
			}
		});*/
		
		

		mIsDeleteApp = false;
		mIsFormatSD = false;
		Log.i("qy", "onBindView()****");
    }

    private final Button.OnClickListener mStartFactoryListener = new Button.OnClickListener() {
        public void onClick(View v) {
//			queryDataStatus();
            Log.v(TAG, "mStartFactoryListener onClick, mIsDeleteApp = " + mIsDeleteApp + " mIsFormatSD = " + mIsFormatSD);
            boolean b = runKeyguardConfirmation(KEYGUARD_REQUEST);
            Log.i("qy", "mCbClearMultiData=="+ getCheckBoxState());
            Log.i("qy", "runKeyguardConfirmation=="+b );
            if (!b) {
                showFinalConfirmation();
            }

            
        }
    };
    
    public boolean getCheckBoxState(){
    	if(mCbClearMultiData !=null){
    		return mCbClearMultiData.isChecked();
    	}
    	return false;
    }
    
    private boolean runKeyguardConfirmation(int request) {
        Resources res = mContext.getResources();
        return new ChooseLockSettingsHelper((AuroraActivity)mContext)
                .launchConfirmationActivity(request,
                        res.getText(R.string.master_clear_gesture_prompt),
                        res.getText(R.string.master_clear_gesture_explanation));
    }

    private void queryDataStatus() {
        final SharedPreferences preferences = getSharedPreferences();
		//getSharedPreferences  getSharedPreferences(PREFERENCES_NAME, Activity.MODE_PRIVATE);
        mIsDeleteApp = preferences.getBoolean(DELETE_APP, false);
		mIsFormatSD = preferences.getBoolean(FORMAT_SD, false);

        if (!mIsDeleteApp && !mIsFormatSD) {
            mAlterdlgSummary = mContext.getResources().getString(R.string.start_factory_alterdlg_summary_none);
        } else if (mIsDeleteApp && !mIsFormatSD) {
            mAlterdlgSummary = mContext.getResources().getString(R.string.start_factory_alterdlg_summary_delapp);
        } else if (!mIsDeleteApp && mIsFormatSD) {
            mAlterdlgSummary = mContext.getResources().getString(R.string.start_factory_alterdlg_summary_sdcard);
        } else if (mIsDeleteApp && mIsFormatSD) {
            mAlterdlgSummary = mContext.getResources().getString(R.string.start_factory_alterdlg_summary_delappandsdcard);
        }

    }
	
	protected void showFinalConfirmation() {
		// TODO Auto-generated method stub
		AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(mContext);
        builder.setTitle(R.string.master_clear_title).setMessage(R.string.master_clear_final_desc)
        .setPositiveButton(R.string.okay_continue,new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
            	// add confirm dialog
            	new AuroraAlertDialog.Builder(mContext).setTitle(R.string.master_clear_title).setMessage(R.string.master_clear_confirm)
            	.setPositiveButton(R.string.okay_restore,new DialogInterface.OnClickListener(){
            		public void onClick(DialogInterface dialog, int which)
                    {
            			startFactoryDefault();
    					Log.v(TAG, "Click ok button");
                    }
            	}).setNegativeButton(R.string.cancel_action,new DialogInterface.OnClickListener()
                {
            		public void onClick(DialogInterface dialog, int which)
                    {
                        Log.v(TAG, "Click cancel button");
                        // dismiss();
                    }
                }
            	).show().setCanceledOnTouchOutside(false);
                
            }
		}
        )
        .setNegativeButton(R.string.cancel_action,new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                Log.v(TAG, "Click cancel button");
                // dismiss();
            }
        }
        ).show().setCanceledOnTouchOutside(false);
	}


	private void startFactoryDefault() {
        if (Utils.isMonkeyRunning()) {
            return;
        }
        Log.v(TAG, "factory_default, startfactorydefault");
		/*if (mIsFormatSD) {
            Intent intent = new Intent(ExternalStorageFormatter.FORMAT_AND_FACTORY_RESET);
            intent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
            mContext.startService(intent);
            Log.v(TAG, "factory_default, startservice");
            getPreferenceActivity.startService(intent);

            mContext.sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
		} else if (mIsDeleteApp) {
          mContext.sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
		}*/
           
            // qy 2014-04 17
        String buildModel = Build.MODEL;
        if (buildModel.contains("U810")) {        	
        	if(mCbClearMultiData.isChecked()){
        		Intent intent = new Intent(ExternalStorageFormatter.FORMAT_AND_FACTORY_RESET);
                intent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
                mContext.startService(intent);
        	}else{
        		mContext.sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
        	}
        	
             
        }else{
//        	mContext.sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
        	
        	Intent intent = new Intent("android.intent.action.MASTER_CLEAR");

            intent.putExtra("wipe_internal_data", getCheckBoxState() ? "true":"false");  

            mContext.sendBroadcast(intent);
        }
        
	}
	
	
}
