/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.settings;

import aurora.app.AuroraAlertDialog;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.backup.IBackupManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import aurora.preference.*;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.view.ViewGroup;
import aurora.app.AuroraActivity;
import android.app.Activity;
import aurora.preference.AuroraSwitchPreference;
import aurora.widget.AuroraCheckBox;
import aurora.app.AuroraAlertDialog;

import com.android.internal.os.storage.ExternalStorageFormatter;
/**
 * Gesture lock pattern settings.
 */
public class PrivacySettings extends SettingsPreferenceFragment implements
        DialogInterface.OnClickListener {

    // Vendor specific
    private static final String GSETTINGS_PROVIDER = "com.google.settings";
    private static final String BACKUP_CATEGORY = "backup_category";
    private static final String BACKUP_DATA = "backup_data";
    private static final String AUTO_RESTORE = "auto_restore";
    private static final String CONFIGURE_ACCOUNT = "configure_account";
	private static final String DELETE_APP = "delete_app";
	private static final String FORMAT_SD = "format_sdcard";
    private IBackupManager mBackupManager;
    private AuroraCheckBoxPreference mBackup;
    private AuroraCheckBoxPreference mAutoRestore;
    private Dialog mConfirmDialog;
    private AuroraPreferenceScreen mConfigure;
	private AuroraSwitchPreference mDeleteApp;
	private AuroraSwitchPreference mFormatSD;
	private View mContentView;

    private static final int DIALOG_ERASE_BACKUP = 2;
    private int mDialogType;
    private static final int KEYGUARD_REQUEST = 55;
    public static final String ACTION_CONFIRM_KEY = "com.android.settings.ACTION_CONFIRM_KEY";
    public static final String KEY_AURORA_FACTORY_DEFAULT_PREF = "aurora_factory_default";
    //private AuroraFactoryDefaultPreference mFactoryPref;
    
    // qy add 2014 07 14 
    private String mDeviceName;
    private Button mStartFactory;
    private AuroraCheckBox mCbClearMultiData;
    private View mCbItem;
    
    private BroadcastReceiver confirmKeyReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(intent.getAction().equals(ACTION_CONFIRM_KEY)){
				Log.i("qy", "onActivityResult()****");
				showFinalConfirmation();
			}
		}
	};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    	//setContentView(R.layout.privacy_settings_main);
        //addPreferencesFromResource(R.xml.privacy_settings); // del qy 2014 07 14
		//setContentView(R.xml.privacy_settings_main);
        //mFactoryPref = (AuroraFactoryDefaultPreference) screen.findPreference(KEY_AURORA_FACTORY_DEFAULT_PREF); // del qy 
        
        /*mBackup = (AuroraCheckBoxPreference) screen.findPreference(BACKUP_DATA);
        mAutoRestore = (AuroraCheckBoxPreference) screen.findPreference(AUTO_RESTORE);
        mConfigure = (AuroraPreferenceScreen) screen.findPreference(CONFIGURE_ACCOUNT);

		mDeleteApp = (AuroraSwitchPreference)screen.findPreference(DELETE_APP);
		mFormatSD = (AuroraSwitchPreference)screen.findPreference(FORMAT_SD);

		mDeleteApp.setChecked(false);
		mFormatSD.setChecked(false);*/
        
        final AuroraPreferenceScreen screen = getPreferenceScreen();
        mBackupManager = IBackupManager.Stub.asInterface(
                ServiceManager.getService(Context.BACKUP_SERVICE));
        IntentFilter intentFilter = new IntentFilter(ACTION_CONFIRM_KEY);
        getActivity().registerReceiver(confirmKeyReceiver,intentFilter);

    }
    
    // qy add 2014 07 14 begin
    private final Button.OnClickListener mStartFactoryListener = new Button.OnClickListener() {
        public void onClick(View v) {
           //queryDataStatus();
           Log.v("gary", "---mStartFactoryListener---");
           //判断当前是否所图案解锁,如果是图案解锁，开始恢复出厂设置时，需要输入图案解锁
           boolean isUnlockPattern = runKeyguardConfirmation(KEYGUARD_REQUEST);
           if(!isUnlockPattern){
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
        Resources res = getActivity().getResources();
        return new ChooseLockSettingsHelper((AuroraActivity)getActivity())
                .launchConfirmationActivity(request,
                        res.getText(R.string.master_clear_gesture_prompt),
                        res.getText(R.string.master_clear_gesture_explanation));
    }
    
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
    	View view;
    	mDeviceName = SystemProperties.get("ro.product.name");
        if (mDeviceName.contains("IUNI")) {
        	view = inflater.inflate(R.layout.privacy_settings_main, container, false);
        	mCbClearMultiData = (AuroraCheckBox)view.findViewById(R.id.cb_clear_multi_user_data);
        	mCbItem = view.findViewById(R.id.ll_clear_multi_user_data);
        	mCbItem.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					mCbClearMultiData.setChecked(!mCbClearMultiData.isChecked());
				}
        		
        	});
        }else{
        	view = inflater.inflate(R.layout.aurora_privacy_settings_category_layout, container, false);
        }
    	
        mStartFactory = (Button)view.findViewById(R.id.start_factory_button);
		mStartFactory.setOnClickListener(mStartFactoryListener);
    	return view;
	}

    //qy add 2014 07 14 end

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}

    @Override
    public void onResume() {
        super.onResume();
        // Refresh UI
        //updateToggles();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
    public void onDestroy() {
    	super.onDestroy();
    	getActivity().unregisterReceiver(confirmKeyReceiver);
    }

    protected void showFinalConfirmation() {
		// TODO Auto-generated method stub
		AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(getActivity());
        builder.setTitle(R.string.master_clear_title)
        .setMessage(R.string.master_clear_final_desc)
        .setPositiveButton(R.string.okay_continue,new DialogInterface.OnClickListener(){
        	
            public void onClick(DialogInterface dialog, int which){
            		new AuroraAlertDialog.Builder(getActivity())
            		.setTitle(R.string.master_clear_title)
            		.setMessage(R.string.master_clear_confirm)
            		.setPositiveButton(R.string.okay_restore,new DialogInterface.OnClickListener(){
            			
            			public void onClick(DialogInterface dialog, int which){
            				startFactoryDefault();
            				}
            		 })
            		.setNegativeButton(R.string.cancel_action,null)
            	    .show()
            	    .setCanceledOnTouchOutside(false);
            }
			})
        .setNegativeButton(R.string.cancel_action,null)
        .show()
        .setCanceledOnTouchOutside(false);
	}

	private void startFactoryDefault() {
        if (Utils.isMonkeyRunning()) {
            return;
        }

        // qy 2014-04 17
        String buildModel = Build.MODEL;
        if (buildModel.contains("U810")) {        	
        	if(getCheckBoxState()){
        		Intent intent = new Intent(ExternalStorageFormatter.FORMAT_AND_FACTORY_RESET);
                intent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
                getActivity().startService(intent);
        	}else{
        		getActivity().sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
        	}
        }else{
        	//mContext.sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
        	Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
            intent.putExtra("wipe_internal_data", getCheckBoxState() ? "true":"false");  
            getActivity().sendBroadcast(intent);
        }
	}
	
    public void onClick(DialogInterface dialog, int which) {
    	
    }

}
