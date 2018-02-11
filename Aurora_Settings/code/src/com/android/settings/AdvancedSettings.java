package com.android.settings;


import android.os.Bundle;
import aurora.app.AuroraActivity;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreference.OnPreferenceClickListener;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import aurora.widget.AuroraListView;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.R.integer;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import gionee.telephony.GnTelephonyManager;
import android.widget.AbsListView.OnScrollListener;
import android.os.SystemProperties;
import gionee.telephony.AuroraTelephoneManager;



// Add begin by aurora.jiangmx
import com.android.settings.nfc.NfcEnabler;
import com.gionee.settings.utils.GnUtils;

import android.nfc.NfcAdapter;
import aurora.preference.AuroraSwitchPreference;
// Add end

public class AdvancedSettings extends SettingsPreferenceFragment implements OnPreferenceClickListener {

    private static final String KEY_WIRELESS_SETTINGS = "wireless_settings";
    private static final String KEY_FACTORY_SETTINGS = "factory_settings";
	private static final String KEY_SIM_SETTINGS = "sim_settings";
	private static final String KEY_SIM_02_SETTINGS = "sim_02_settings";
	private static final String DEVELOPMENT_SETTINGS = "development_settings";

	private static final String KEY_TWO_SIM_SETTINGS = "two_sim_settings";
	private static final String PRIVACY_SETTINGS = "privacy_settings";

	
	private static final boolean MUILTI_SIM_ENABLE = GnTelephonyManager.isMultiSimEnabled();
	private AuroraListView mListView;
	private int firstVisibleItem;
	private int mScrolledX;	
	
	// Add begin by aurora.jiangmx
	private static final String KEY_TOGGLE_NFC = "toggle_nfc";
    private static final String KEY_ANDROID_BEAM_SETTINGS = "android_beam_settings";
    
	private AuroraSwitchPreference mNfcSwitch;
    private NfcEnabler mNfcEnabler;
    private NfcAdapter mNfcAdapter;
 	// Add end
	
	// qy add 2014 07 16 begin
	PhoneStateListener sPhoneStateListener = new PhoneStateListener() {
	    @Override
	        public void onServiceStateChanged(ServiceState serviceState) {
	            // TODO Auto-generated method stub
	            if (serviceState != null) {
	                if (serviceState.getState() == ServiceState.STATE_IN_SERVICE) {
	                   
	                    Log.i("gary", "serviceState.getState() == STATE_IN_SERVICE");
	                    if(mTwoSimSettings == null && getPreferenceManager()!=null && getActivity()!=null){
	                    	mTwoSimSettings = getPreferenceManager().createPreferenceScreen(
	                                getActivity());
	                    	 Log.i("gary", "-----------------sssssss----------");
	                    	mTwoSimSettings.setOrder(2);
	                    	mTwoSimSettings.setKey(KEY_TWO_SIM_SETTINGS);
	                    	mTwoSimSettings.setTitle(R.string.aurora_sim_settings_title);
	                    	mTwoSimSettings.setFragment(TwoSimSettings.class.getName());
	                    	getPreferenceScreen().addPreference(mTwoSimSettings);
	                    }
	                    
	                    
	                    if(mSimSettings == null && getActivity() != null){
	                    	mSimSettings = new AuroraPreference(getActivity());
	                    	mSimSettings.setTitle(R.string.aurora_sim_settings_title);
	                    	mSimSettings.auroraSetArrowText("", true);
	                    	mSimSettings.setOrder(3);
	                    	mSimSettings.setOnPreferenceClickListener(AdvancedSettings.this);
	                		getPreferenceScreen().addPreference(mSimSettings);
	                	}
	                    if(mSim02Settings == null && getActivity() != null){
	                		mSim02Settings  = new AuroraPreference(getActivity());
	                		mSim02Settings.setTitle(R.string.aurora_sim_02_settings_title);
	                		mSim02Settings.auroraSetArrowText("", true);
	                		mSim02Settings.setOrder(4);
	                		mSim02Settings.setOnPreferenceClickListener(AdvancedSettings.this);
	                		getPreferenceScreen().addPreference(mSim02Settings);
	                	}
	                    selectShowPref();
	                } else {
	                	Log.i("gary", "serviceState.getState() == not STATE_IN_SERVICE");
	                	removeTwoSimSettings();
	                	
	                	if(mSimSettings != null){
	                		getPreferenceScreen().removePreference(mSimSettings);
	                		mSimSettings = null;
	                	}
	                	if(mSim02Settings != null){
	                		getPreferenceScreen().removePreference(mSim02Settings);
	                		mSim02Settings = null;
	                	}
	                }
	            } 
	            super.onServiceStateChanged(serviceState);
	        }
	      };
      private TelephonyManager mPhone;
      
      private AuroraPreferenceScreen mTwoSimSettings;
      private AuroraPreferenceScreen mPrivacySettings;
      
      private AuroraPreference mSimSettings;
      private AuroraPreference mSim02Settings;
      private AuroraPreferenceScreen mApplicationPreferenceScreen;
      private void selectShowPref(){
    	  if(MUILTI_SIM_ENABLE){
    		 if(mSimSettings !=null){
    			 boolean isSim01Remove = false;
    			 boolean isSim02Remove = false;
    			 mSimSettings.setTitle(R.string.aurora_sim_01_settings_title);
    			 Log.i("qy", "isCanUseSim(0) == "+ isCanUseSim(0));
    	  			Log.i("qy", "isCanUseSTK(0) == "+ isCanUseSTK(0));
    	  			if(!isCanUseSim(0) || !isCanUseSTK(0)){
    	  				getPreferenceScreen().removePreference(mSimSettings);
    	  				mSimSettings = null;
                        Log.v("gary", "---isSim01Remove---");
    	  				isSim01Remove = true;
    	  			}
    	  			if(!isCanUseSim(1) || !isCanUseSTK(1)){
    	  				getPreferenceScreen().removePreference(mSim02Settings);
    	  				mSim02Settings = null;
    	  				 Log.v("gary", "---isSim02Remove---");
    	  				isSim02Remove = true;
    	  			}
    	  			
    	  		   if(!isSim01Remove  && !isSim02Remove){
    	  			   getPreferenceScreen().removePreference(mSimSettings);
 	  				   mSimSettings = null;
 	  				   getPreferenceScreen().removePreference(mSim02Settings);
	  				   mSim02Settings = null;
    	  		   }else{
    	  			   removeTwoSimSettings();
    	  		   }

    	  		} else {
    	  			if(!isCanUseSim() || !isCanUseSTK()) {
    	  				if(getPreferenceScreen().findPreference(KEY_SIM_SETTINGS) != null){
    	  						getPreferenceScreen().removePreference(mSimSettings);
    	  						mSimSettings = null;
    	  					}
    	  			}
    	  			getPreferenceScreen().removePreference(mSim02Settings);
    	  			mSim02Settings = null;
    	  			
    	  			removeTwoSimSettings();
    	  		}
			}else{//如果是单卡手机，就去掉sim2
				removeTwoSimSettings();
				if(mSim02Settings != null){
					getPreferenceScreen().removePreference(mSim02Settings);
					mSim02Settings = null;
				}
  				
  				//Begin add by gary.gou,for BUG #8661
  				if(!isCanUseSim() || !isCanUseSTK()) {
	  				getPreferenceScreen().removePreference(mSimSettings);
	  				mSimSettings = null;
	  			}
  			   //End add by gary.gou,for BUG #8661
			}
  			
      }
   // qy add 2014 07 16 end
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    	addPreferencesFromResource(R.xml.aurora_advanced_settings);
    	
    	mPhone = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    	mPhone.listen(sPhoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE);

    	mTwoSimSettings = (AuroraPreferenceScreen)findPreference(KEY_TWO_SIM_SETTINGS);
    	
		mSimSettings = (AuroraPreference)findPreference(KEY_SIM_SETTINGS);
		mSimSettings.auroraSetArrowText("", true);
		mSimSettings.setOnPreferenceClickListener(this);

		// add by steve.tang, 2014-05-30, check STK app can use. start
		mSim02Settings = (AuroraPreference)findPreference(KEY_SIM_02_SETTINGS);
		mSim02Settings.auroraSetArrowText("", true);
		mSim02Settings.setOnPreferenceClickListener(this);
		selectShowPref();
		Log.v("qy", "-----MUILTI_SIM_ENABLE-----"+MUILTI_SIM_ENABLE);
		
		if(GnUtils.isAbroadVersion()){
			 final boolean showDev =  getActivity().getSharedPreferences(DevelopmentSettings.PREF_FILE,
		                Context.MODE_PRIVATE).getBoolean(
		                DevelopmentSettings.PREF_SHOW,
		                android.os.Build.TYPE.equals("eng"));
			 if(!showDev){
				 AuroraPreferenceScreen developmentScreen = (AuroraPreferenceScreen)findPreference(DEVELOPMENT_SETTINGS);
				 getPreferenceScreen().removePreference(developmentScreen);
			 }
			 //海外版本名称显示"备份与还原"
			 mPrivacySettings = (AuroraPreferenceScreen)findPreference(PRIVACY_SETTINGS);
			 mPrivacySettings.setTitle(R.string.privacy_settings_title);
			 mPrivacySettings.setFragment(NativePrivacySettings.class.getName());
		}
/*		if(MUILTI_SIM_ENABLE){
			mSimSettings.setTitle(R.string.aurora_sim_01_settings_title);
			if(!isCanUseSim(0) || !isCanUseSTK(0)){
				getPreferenceScreen().removePreference(mSimSettings);
				mSimSettings = null;
			}
			if(!isCanUseSim(1) || !isCanUseSTK(1)){
				getPreferenceScreen().removePreference(mSim02Settings);
				mSim02Settings = null;
			}

		} else {
			if(!isCanUseSim() || !isCanUseSTK()) {
				getPreferenceScreen().removePreference(mSimSettings);
				mSimSettings = null;
			}
			getPreferenceScreen().removePreference(mSim02Settings);
			mSim02Settings = null;
		}*/
		// add by steve.tang, 2014-05-30, check STK app can use. end

        AuroraPreferenceScreen wirelessScreen = (AuroraPreferenceScreen)findPreference(KEY_WIRELESS_SETTINGS);
        if (wirelessScreen != null) {
//            getPreferenceScreen().removePreference(wirelessScreen);
        }

        AuroraPreferenceScreen factoryScreen = (AuroraPreferenceScreen)findPreference(KEY_FACTORY_SETTINGS);
        /*
        String buildModel = Build.MODEL;
//        Log.v("xiaoyong", "productname is " + buildModel);
        
        if (factoryScreen != null && !buildModel.equals("GT-I9500") && !buildModel.contains("SM-N900")) {
            getPreferenceScreen().removePreference(factoryScreen);
            getPreferenceScreen().removePreference((AuroraPreference)findPreference("pref_category_factory_settings"));
            
        }
        */
        String deviceName = SystemProperties.get("ro.gn.iuniznvernumber"); 
        Log.v("gary","deviceName======"+deviceName);
        
        
        if(factoryScreen != null){
        	if(deviceName.contains("I9500") || deviceName.contains("N900")){
        		
        		getPreferenceScreen().removePreference(factoryScreen);
        		getPreferenceScreen().removePreference((AuroraPreference)findPreference("pref_category_factory_settings"));
        		
        		//factoryScreen.setTitle(R.string.factory_settings_title);
        		//factoryScreen.setFragment("com.android.settings.LocalFactorySettings");
        	}else  if(deviceName.contains("OnePlusOne") /*|| deviceName.contains("FIND7")*/){
        		getPreferenceScreen().removePreference(factoryScreen);
        		getPreferenceScreen().removePreference((AuroraPreference)findPreference("pref_category_factory_settings"));
        		/*
        		if(deviceName.contains("OnePlusOne")){
        			factoryScreen.setTitle(R.string.aurora_oneplus_factory_settings_title);
        		}else{
        			factoryScreen.setTitle(R.string.aurora_find7_factory_settings_title);
        		}
        	
        		factoryScreen.setFragment("com.android.settings.OnePlusFactorySettings");
        		*/
        	}else{
        		getPreferenceScreen().removePreference(factoryScreen);
        		getPreferenceScreen().removePreference((AuroraPreference)findPreference("pref_category_factory_settings"));
        	}
        }
        
        
        mApplicationPreferenceScreen = (AuroraPreferenceScreen)findPreference("application_settings");
        
        mApplicationPreferenceScreen.setOnPreferenceClickListener(new AuroraPreference.OnPreferenceClickListener() {  
            
            @Override  
            public boolean onPreferenceClick(AuroraPreference preference) {  
                startAppManager();  
                return true;  
            }  
        });
        
        
        /*Intent  applicationIntent = new Intent();
        ComponentName applicationName = new ComponentName("com.aurora.secure",
        		"com.secure.activity.AllAppListActivity");
        applicationIntent.setComponent(applicationName);
        applicationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        applicationPreferenceScreen.setIntent(applicationIntent);*/

        AuroraPreferenceScreen  accountPreferenceScreen = (AuroraPreferenceScreen)findPreference("account");
        Intent  intent = new Intent();
        ComponentName accountName = new ComponentName("com.aurora.auroraaccountsettings",
        		"com.aurora.auroraaccountsettings.AccountActivity");
        intent.setComponent(accountName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        accountPreferenceScreen.setIntent(intent);
        
        // Add begin by aurora.jiangmx
        mNfcSwitch = (AuroraSwitchPreference) findPreference(KEY_TOGGLE_NFC);
        AuroraPreferenceScreen androidBeam = (AuroraPreferenceScreen) findPreference(KEY_ANDROID_BEAM_SETTINGS);
        
		if(Build.MODEL.equals("IUNI U810")) {
			mNfcAdapter = null;
		} else {
			mNfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
		}
		
		mNfcEnabler = new NfcEnabler(getActivity(), mNfcSwitch, androidBeam);
		
        if (mNfcAdapter == null) {
             /*if (FeatureOption.MTK_NFC_ADDON_SUPPORT) {
                 getPreferenceScreen().removePreference(mNfcPreference);
                 mMTKNfcEnabler = null;
             } else {*/
                 getPreferenceScreen().removePreference(mNfcSwitch);
                 mNfcEnabler = null;
             //}
             getPreferenceScreen().removePreference(androidBeam);
         }
        //所有机型都屏蔽掉android beam
        getPreferenceScreen().removePreference(androidBeam);
        // Add end
    }
    
    private void startAppManager(){
    	Intent intent = new Intent();
		
		intent.setAction("android.settings.MANAGE_APPLICATIONS_SETTINGS");

		getActivity().startActivity(intent);
    }
    private void removeTwoSimSettings(){
    	if(mTwoSimSettings != null){
    		getPreferenceScreen().removePreference(mTwoSimSettings);
    		mTwoSimSettings = null;
    	}
    }
    
	//use for single card project
    public boolean isCanUseSTK() {
    	if(getPackageManager() == null){
    		return false;
    	}
    	
    	PackageManager packageManager = getPackageManager();
    	ComponentName componentName = new ComponentName("com.android.stk", "com.android.stk.StkLauncherActivity");
        int res = packageManager.getComponentEnabledSetting(componentName);
        boolean state = false;
        if (res == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            state = false;
        } else {
            state = true;
        }
        return state;
    }


	//use for single card project
	public boolean isCanUseSim() { 
	    try { 
//	        TelephonyManager mgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE); // del qy 2014 07 16
	 
	        return TelephonyManager.SIM_STATE_READY == mPhone.getSimState(); 
	    } catch (Exception e) { 
	        e.printStackTrace(); 
	    } 
	    return false; 
	} 

	// add by steve.tang, 2014-05-30, check STK app can use. start
	//use for multi card project
    public boolean isCanUseSTK(int slotId) {
    	if(getPackageManager() == null){
    		return false;
    	}
    	
		PackageManager packageManager = getPackageManager();
		ComponentName componentName = null;
		if(slotId == 0) {
			componentName = new ComponentName("com.android.stk", "com.android.stk.StkLauncherActivity");
		} else {
			if(AuroraTelephoneManager.isMtkGemini()){
				 componentName = new ComponentName("com.android.stk", "com.android.stk.StkLauncherActivityII");
			 }else{
				 componentName = new ComponentName("com.android.stk", "com.android.stk.StkLauncherActivity2");
			}			
		}
		if(componentName == null || packageManager == null) return false;

        int res = packageManager.getComponentEnabledSetting(componentName);
        boolean state = false;
        if (res == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            state = false;
        } else {
            state = true;
        }
        return state;
    }


	//use for multi card project
	public boolean isCanUseSim(int slotId) { 
		return TelephonyManager.SIM_STATE_READY == GnTelephonyManager.getSimStateGemini(slotId);
	}

	@Override
	public boolean onPreferenceClick(AuroraPreference arg0) {
		// TODO Auto-generated method stub
		if(arg0 == mSimSettings){
			Log.i("qy", "onPreferenceClick***");
			try{
				Intent intent = new Intent();
				intent.setClassName("com.android.stk", "com.android.stk.StkLauncherActivity");
				getActivity().startActivity(intent);
			}catch(Exception e){
				Log.i("qy", "mSimSettings---exception for start activity ---com.android.stk.StkLauncherActivity");
			}
//			((AuroraActivity)getActivity()).overridePendingTransition(com.aurora.R.anim.aurora_activity_open_enter,com.aurora.R.anim.aurora_activity_open_exit);
		}else if(arg0 == mSim02Settings){
			try{
			Intent in = new Intent();
			
			if(AuroraTelephoneManager.isMtkGemini()){
				 in.setClassName("com.android.stk", "com.android.stk.StkLauncherActivityII");
			 }else{
				 in.setClassName("com.android.stk", "com.android.stk.StkLauncherActivity2");
			}	
			
			getActivity().startActivity(in);
//			((AuroraActivity)getActivity()).overridePendingTransition(com.aurora.R.anim.aurora_activity_open_enter,com.aurora.R.anim.aurora_activity_open_exit);
			}catch(Exception e){
				Log.i("qy", "mSim02Settings--exception for start activity ----");
			}
		}
		return false;
	}
	// add by steve.tang, 2014-05-30, check STK app can use. end
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		mListView = getListView();
		mListView.setOnScrollListener(new OnScrollListener() {
			
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {  		             	               
					firstVisibleItem = mListView.getFirstVisiblePosition();
	                View v = mListView.getChildAt(0);
	                mScrolledX = (v == null) ? 0 : v.getTop();
		               
		                
		        } 
				
			}
			
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();		
		mListView.setSelectionFromTop(firstVisibleItem, mScrolledX);
		
		// Add begin by aurora.jiangmx
		 if (mNfcEnabler != null) {
             mNfcEnabler.resume();
         }
		// Add end
	}

	@Override
	public void onPause() {
	   super.onPause();
	   
	   // Add begin by aurora.jiangmx
       if (mNfcEnabler != null) {
           mNfcEnabler.pause();
       }
       // Add end
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mPhone.listen(sPhoneStateListener, PhoneStateListener.LISTEN_NONE);
	}
	
	

}
