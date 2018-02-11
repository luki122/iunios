package com.android.phone;

/*
 * Copyright (C) 2006 The Android Open Source Project
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

import com.android.internal.telephony.imsphone.ImsPhone;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.ActivityManagerNative;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.IBluetoothHeadsetPhone;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IPowerManager;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UpdateLock;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.view.KeyEvent;
import android.widget.Toast;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.TelephonyCapabilities;
import com.android.internal.telephony.TelephonyIntents;
//import com.android.phone.common.CallLogAsync;
import com.android.phone.OtaUtils.CdmaOtaScreenState;
import com.android.phone.PhoneGlobals.SubInfoUpdateListener;
import com.android.server.sip.SipService;

import aurora.preference.*;
import aurora.app.*;
import android.os.Build;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.os.SystemProperties;
import android.provider.Settings;
import static com.android.phone.AuroraMSimConstants.DEFAULT_SUBSCRIPTION;
import static com.android.phone.AuroraMSimConstants.SUBSCRIPTION_KEY;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms;
import android.provider.Telephony.MmsSms;
import android.database.ContentObserver;

import com.android.internal.telephony.test.SimulatedRadioControl;
import com.android.internal.telephony.uicc.UiccController;
import com.mediatek.phone.PhoneInterfaceManagerEx;

import com.mediatek.internal.telephony.RadioManager;
import com.mediatek.internal.telephony.cdma.CdmaFeatureOptionUtils;
import com.mediatek.internal.telephony.ltedc.svlte.SvltePhoneProxy;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

public class I2PhoneGlobals extends PhoneGlobals{
	static final String LOG_TAG = "I2PhoneGlobals";
    private Phone[] mPhones = null;
    protected TelecomServiceImpl mTelecomService;
    PhoneInterfaceManagerEx phoneMgrEx;
    private SubscriptionManager mSubscriptionManager;
    
    
    I2PhoneGlobals(Context context) {
        super(context);
    }
    
	  public void onCreate() {
		  if (VDBG) Log.v(LOG_TAG, "onCreate()...");

	        ContentResolver resolver = getContentResolver();

	        // Cache the "voice capable" flag.
	        // This flag currently comes from a resource (which is
	        // overrideable on a per-product basis):
	        sVoiceCapable =true;
	               // getResources().getBoolean(com.android.internal.R.bool.config_voice_capable);
	        // ...but this might eventually become a PackageManager "system
	        // feature" instead, in which case we'd do something like:
	        // sVoiceCapable =
	        //   getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY_VOICE_CALLS);

	        if (phone == null) {
	            // Initialize the telephony framework
//	        	if(AuroraPhoneUtils.isSimulate()) {
//	        		  if (VDBG) Log.v(LOG_TAG, "onCreate()...isSimulate");
//		        	SimulatedPhoneFactory.makeDefaultPhones(this);
//		
//		            // Get the default phone
//		//            phone = PhoneFactory.getDefaultPhone();
//		            phone = SimulatedPhoneFactory.getDefaultPhone();
//		            mCM = CallManager.getInstance();
//		            mCM.registerPhone(phone);
//	        	} else {
	        		PhoneFactory.makeDefaultPhones(this);
	                Intent intent = new Intent(this, TelephonyDebugService.class);
	                startService(intent);
	        	    phone = PhoneFactory.getDefaultPhone();
	        	      mPhones = PhoneFactory.getPhones();
	                  mCM = CallManager.getInstance();
//	  	            mCM.registerPhone(phone);
	  	            registerPhone(mCM);

//	        	}

	

	            // Create the NotificationMgr singleton, which is used to display
	            // status bar icons and control other status bar behavior.
//	            notificationMgr = NotificationMgr.init(this);
	            notificationMgr = I2NotificationMgr.init(this);

	            phoneMgr = PhoneInterfaceManager.init(this, phone);
	            phoneMgrEx = PhoneInterfaceManagerEx.init(this, phone);
	                        
	            if(DeviceUtils.isUseAuroraPhoneService()) {
	            	Intent phoneserviceintent = new Intent("com.aurora.phone.PHONE_SERVICE_SET_UP");
	            	sendBroadcast(phoneserviceintent);
	            }

	            mHandler.sendEmptyMessage(EVENT_START_SIP_SERVICE);

	            int phoneType = phone.getPhoneType();

	            if (phoneType == PhoneConstants.PHONE_TYPE_CDMA) {
	                // Create an instance of CdmaPhoneCallState and initialize it to IDLE
	                cdmaPhoneCallState = new CdmaPhoneCallState();
	                cdmaPhoneCallState.CdmaPhoneCallStateInit();
	            }

	            if (BluetoothAdapter.getDefaultAdapter() != null) {
	                // Start BluetoothPhoneService even if device is not voice capable.
	                // The device can still support VOIP.
	            	Intent blueIntent = new Intent();
	            	blueIntent.setClassName(this, "com.android.phone.BluetoothPhoneService");
	                startService(blueIntent);
	                bindService(blueIntent, mBluetoothPhoneConnection, 0);
	            } else {
	                // Device is not bluetooth capable
	                mBluetoothPhone = null;
	            }

	            ringer = Ringer.init(this);

	            // before registering for phone state changes
	            mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
	            mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, LOG_TAG);
	            // lock used to keep the processor awake, when we don't care for the display.
	            mPartialWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
	                    | PowerManager.ON_AFTER_RELEASE, LOG_TAG);
	            // Wake lock used to control proximity sensor behavior.
	            
	            //aurora delete liguangyu 20140331 for proximity sensor start
//	            if (mPowerManager.isWakeLockLevelSupported(
//	                    PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
//	                mProximityWakeLock = mPowerManager.newWakeLock(
//	                        PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, LOG_TAG);
//	            }
//	            if (DBG) Log.d(LOG_TAG, "onCreate: mProximityWakeLock: " + mProximityWakeLock);
	            //aurora delete liguangyu 20140331 for proximity sensor end

	            // create mAccelerometerListener only if we are using the proximity sensor
	            if (proximitySensorModeEnabled()) {
	                mAccelerometerListener = new AccelerometerListener(this, this);
	            }

	            mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

	            // get a handle to the service so that we can use it later when we
	            // want to set the poke lock.
	            mPowerManagerService = IPowerManager.Stub.asInterface(
	                    ServiceManager.getService("power"));

	            // Get UpdateLock to suppress system-update related events (e.g. dialog show-up)
	            // during phone calls.
	            mUpdateLock = new UpdateLock("phone");

	            if (DBG) Log.d(LOG_TAG, "onCreate: mUpdateLock: " + mUpdateLock);

	            CallLogger callLogger = new CallLogger(this, new CallLogAsync());

	            // Create the CallController singleton, which is the interface
	            // to the telephony layer for user-initiated telephony functionality
	            // (like making outgoing calls.)
	            callController = CallController.init(this, callLogger);
	            // ...and also the InCallUiState instance, used by the CallController to
	            // keep track of some "persistent state" of the in-call UI.
	            inCallUiState = InCallUiState.init(this);

	            // Create the CallerInfoCache singleton, which remembers custom ring tone and
	            // send-to-voicemail settings.
	            //
	            // The asynchronous caching will start just after this call.
	            callerInfoCache = CallerInfoCache.init(this);

	            // Create the CallNotifer singleton, which handles
	            // asynchronous events from the telephony layer (like
	            // launching the incoming-call UI when an incoming call comes
	            // in.)
	            notifier = I2CallNotifier.init(this, phone, ringer, callLogger);

	            // Create the Managed Roaming singleton class, used to show popup
	            // to user for initiating network search when location update is rejected
	            mManagedRoam = ManagedRoaming.init(this);

	            // register for ICC status
	            IccCard sim = phone.getIccCard();
	            if (sim != null) {
	                if (VDBG) Log.v(LOG_TAG, "register for ICC status");
					//aurora change zhouxiaobing 20131206 for 4.3qc start
	                //sim.registerForNetworkLocked(mHandler, EVENT_SIM_NETWORK_LOCKED, null);
					  if(!(Build.VERSION.SDK_INT>=18&&SystemProperties.get("ro.product.board").contains("MSM")))    
					  	{           
					  	try{                  
							Class<?> sPolicy=null;                  
						    sPolicy=Class.forName("com.android.internal.telephony.IccCard");                 
						    Method method=sPolicy.getMethod("registerForNetworkLocked", Handler.class,int.class,Object.class);                 
						    method.invoke(sim,mHandler, EVENT_SIM_NETWORK_LOCKED, null);          }catch(Exception e){}    
						}  
					  else  
					  	{    
							try{                  
								Class<?> sPolicy=null;                  
							    sPolicy=Class.forName("com.android.internal.telephony.IccCard");                  
							    Method method=sPolicy.getMethod("registerForPersoLocked", Handler.class,int.class,Object.class);                  
							    method.invoke(sim,mHandler, EVENT_PERSO_LOCKED, null);      }catch(Exception e){}  
						} 
				//aurora change zhouxiaobing 20131206 for 4.3qc end	  
	            }

	            // register for MMI/USSD
	            mCM.registerForMmiComplete(mHandler, MMI_COMPLETE, null);

	            // register connection tracking to PhoneUtils
	            PhoneUtils.initializeConnectionHandler(mCM);

	            // Read platform settings for TTY feature
	            mTtyEnabled = getResources().getBoolean(R.bool.tty_enabled);

	            // Register for misc other intent broadcasts.
	            IntentFilter intentFilter =
	                    new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
	            intentFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
	            intentFilter.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
	            intentFilter.addAction(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED);
	            intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
	            intentFilter.addAction(Intent.ACTION_DOCK_EVENT);
	            intentFilter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
	            intentFilter.addAction(TelephonyIntents.ACTION_RADIO_TECHNOLOGY_CHANGED);
	            intentFilter.addAction(TelephonyIntents.ACTION_SERVICE_STATE_CHANGED);
	            intentFilter.addAction(TelephonyIntents.ACTION_EMERGENCY_CALLBACK_MODE_CHANGED);
	            intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
	            intentFilter.addAction(Intent.ACTION_SCREEN_ON);
	            intentFilter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
	            
	            intentFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
	            intentFilter.addAction(Intent.ACTION_MSIM_MODE_CHANGED);
	            intentFilter.addAction(ACTION_SHUTDOWN_IPO);
	            intentFilter.addAction(ACTION_PREBOOT_IPO);
	            mReceiver = new MSimPhoneAppBroadcastReceiver();
	            registerReceiver(mReceiver, intentFilter);

//	            // Use a separate receiver for ACTION_MEDIA_BUTTON broadcasts,
//	            // since we need to manually adjust its priority (to make sure
//	            // we get these intents *before* the media player.)
//	            IntentFilter mediaButtonIntentFilter =
//	                    new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
//	            // TODO verify the independent priority doesn't need to be handled thanks to the
//	            //  private intent handler registration
//	            // Make sure we're higher priority than the media player's
//	            // MediaButtonIntentReceiver (which currently has the default
//	            // priority of zero; see apps/Music/AndroidManifest.xml.)
//	            mediaButtonIntentFilter.setPriority(1);
//	            //
//	            registerReceiver(mMediaButtonReceiver, mediaButtonIntentFilter);
//	            // register the component so it gets priority for calls
//	            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//	            am.registerMediaButtonEventReceiver(new ComponentName(this.getPackageName(),
//	                  MediaButtonBroadcastReceiver.class.getName()));
	            mHeadsetMediaButton = new HeadsetMediaButton(this, mCM);


	            //set the default values for the preferences in the phone.
	            AuroraPreferenceManager.setDefaultValues(this, R.xml.network_setting, false);

	            AuroraPreferenceManager.setDefaultValues(this, R.xml.call_feature_setting, false);

	            // Make sure the audio mode (along with some
	            // audio-mode-related state of our own) is initialized
	            // correctly, given the current state of the phone.
	            PhoneUtils.setAudioMode(mCM);
	        }

	        if (TelephonyCapabilities.supportsOtasp(phone)) {
	            cdmaOtaProvisionData = new OtaUtils.CdmaOtaProvisionData();
	            cdmaOtaConfigData = new OtaUtils.CdmaOtaConfigData();
	            cdmaOtaScreenState = new OtaUtils.CdmaOtaScreenState();
	            cdmaOtaInCallScreenUiState = new OtaUtils.CdmaOtaInCallScreenUiState();
	        }

	        // XXX pre-load the SimProvider so that it's ready
	        resolver.getType(Uri.parse("content://icc/adn"));

	        // start with the default value to set the mute state.
	        mShouldRestoreMuteOnInCallResume = false;

	        // TODO: Register for Cdma Information Records
	        // phone.registerCdmaInformationRecord(mHandler, EVENT_UNSOL_CDMA_INFO_RECORD, null);

	        // Read TTY settings and store it into BP NV.
	        // AP owns (i.e. stores) the TTY setting in AP settings database and pushes the setting
	        // to BP at power up (BP does not need to make the TTY setting persistent storage).
	        // This way, there is a single owner (i.e AP) for the TTY setting in the phone.
//	        if (mTtyEnabled) {
//	            mPreferredTtyMode = android.provider.Settings.Secure.getInt(
//	                    phone.getContext().getContentResolver(),
//	                    android.provider.Settings.Secure.PREFERRED_TTY_MODE,
//	                    Phone.TTY_MODE_OFF);
//	            mHandler.sendMessage(mHandler.obtainMessage(EVENT_TTY_PREFERRED_MODE_CHANGED, 0));
//	        }
	        // Read HAC settings and configure audio hardware
	        if (getResources().getBoolean(R.bool.hac_enabled)) {
	            int hac = android.provider.Settings.System.getInt(phone.getContext().getContentResolver(),
	                                                              android.provider.Settings.System.HEARING_AID,
	                                                              0);
	            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	            audioManager.setParameter(CallFeaturesSetting.HAC_KEY, hac != 0 ?
	                                      CallFeaturesSetting.HAC_VAL_ON :
	                                      CallFeaturesSetting.HAC_VAL_OFF);
	        }
	        
	        mTelecomService = new TelecomServiceImpl(this, phone);
	        ServiceManager.addService("phone_msim", mTelecomService);
	        
	        // register for subscriptions change
            SubscriptionManager.from(this)
                    .addOnSubscriptionsChangedListener(mOnSubscriptionsChangeListener);
            mSubscriptionInfos = PhoneUtils.getActiveSubInfoList();
	        
	        // To prevent PhoneGlobals init too long, lose the first AIRPLANE_MODE_CHANGED intent
            boolean isAirplaneMode = System.getInt(getContentResolver(), System.AIRPLANE_MODE_ON, 0) != 0;
            if (DBG) {
                Log.d(LOG_TAG, "Notify RadioManager with airplane mode:" + isAirplaneMode);
            }
            RadioManager.getInstance().notifyAirplaneModeChange(isAirplaneMode);
            
	        IntentFilter intentFilter =
                    new IntentFilter("clearMissCallLog");
            registerReceiver(mCallLogReceiver, intentFilter);
	        
	        mSubscriptionManager = SubscriptionManager.from(this);
	        
            IntentFilter ddsIntentFilter = new IntentFilter(TelephonyIntents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED);
            registerReceiver(mDdsReceiver, ddsIntentFilter);
	        
	        initAuroraObjects();   	
	  }
	  
	  protected static String getCallScreenClassName() {
	        return I2InCallScreen.class.getName();
	    }

	    private static final String ACTION_SHUTDOWN_IPO = "android.intent.action.ACTION_SHUTDOWN_IPO";
	    private static final String ACTION_PREBOOT_IPO = "android.intent.action.ACTION_PREBOOT_IPO";
	    private boolean mDataDisconnectedDueToRoaming = false;
	  
	  private class MSimPhoneAppBroadcastReceiver extends PhoneGlobals.PhoneAppBroadcastReceiver {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	            String action = intent.getAction();
	            Log.v(LOG_TAG,"Action intent recieved:"+action);
	            if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
	                boolean enabled = intent.getBooleanExtra("state", false);
	                Log.d(LOG_TAG, "mReceiver: ACTION_AIRPLANE_MODE_CHANGED, enabled = " + enabled);
	                /// @}
	                RadioManager.getInstance().notifyAirplaneModeChange(enabled);

	                if (RadioManager.getInstance().isPowerOnFeatureAllClosed()) {
	                    for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
		                    getPhone(i).setRadioPower(!enabled);
		                }
	                }

	            } else if (action.equals(TelephonyIntents.ACTION_RADIO_TECHNOLOGY_CHANGED)) {
	            	String newPhone = intent.getStringExtra(PhoneConstants.PHONE_NAME_KEY);
	                int phoneId = intent.getIntExtra(PhoneConstants.PHONE_KEY,
	                        SubscriptionManager.INVALID_PHONE_INDEX);
	                Log.d(LOG_TAG, "Radio technology switched. Now " + newPhone + " (" + phoneId
	                        + ") is active.");
	                initForNewRadioTechnology(phoneId);
	            } else if (action.equals(TelephonyIntents.ACTION_SERVICE_STATE_CHANGED)) {
	            	int phoneId = intent.getIntExtra(PhoneConstants.PHONE_KEY,
	                        SubscriptionManager.INVALID_PHONE_INDEX);
	                Phone phone = getPhone(phoneId);
	                handleServiceStateChanged(intent, phone);
	            } else if (action.equals(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED)) {
	                int subId = intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY,
	                        SubscriptionManager.INVALID_SUBSCRIPTION_ID);
	                int phoneId = SubscriptionManager.getPhoneId(subId);
	                String state = intent.getStringExtra(PhoneConstants.STATE_KEY);
	                if (VDBG) {
	                    Log.d(LOG_TAG, "mReceiver: ACTION_ANY_DATA_CONNECTION_STATE_CHANGED");
	                    Log.d(LOG_TAG, "- state: " + state);
	                    Log.d(LOG_TAG, "- reason: "
	                    + intent.getStringExtra(PhoneConstants.STATE_CHANGE_REASON_KEY));
	                    Log.d(LOG_TAG, "- subId: " + subId);
	                    Log.d(LOG_TAG, "- phoneId: " + phoneId);
	                }
	                Phone phone = SubscriptionManager.isValidPhoneId(phoneId) ?
	                        PhoneFactory.getPhone(phoneId) : PhoneFactory.getDefaultPhone();
	                // The "data disconnected due to roaming" notification is shown
	                // if (a) you have the "data roaming" feature turned off, and
	                // (b) you just lost data connectivity because you're roaming.
	                boolean disconnectedDueToRoaming =
	                        !phone.getDataRoamingEnabled()
	                        && PhoneConstants.DataState.DISCONNECTED.equals(state)
	                        && Phone.REASON_ROAMING_ON.equals(
	                            intent.getStringExtra(PhoneConstants.STATE_CHANGE_REASON_KEY));
	                if (mDataDisconnectedDueToRoaming != disconnectedDueToRoaming) {
	                    mDataDisconnectedDueToRoaming = disconnectedDueToRoaming;
	                    mHandler.sendEmptyMessage(disconnectedDueToRoaming
	                            ? EVENT_DATA_ROAMING_DISCONNECTED : EVENT_DATA_ROAMING_OK);
	                }
	            }
	            
	            else if (action.equals(TelephonyIntents.ACTION_EMERGENCY_CALLBACK_MODE_CHANGED)) {
	                int subId = intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY,
	                        SubscriptionManager.INVALID_SUBSCRIPTION_ID);
	                int phoneId = SubscriptionManager.getPhoneId(subId);
	                Phone phone = getPhone(phoneId);
	                if (TelephonyCapabilities.supportsEcm(phone)) {
	                    Log.d(LOG_TAG, "Emergency Callback Mode arrived in PhoneApp"
	                            + " on Sub =" + phoneId);
	                    // Start Emergency Callback Mode service
	                    if (intent.getBooleanExtra("phoneinECMState", false)) {
	                        Intent ecbmIntent = new Intent(context, EmergencyCallbackModeService.class);	                 
	                        ecbmIntent.putExtra(SUBSCRIPTION_KEY, phoneId);
	                        context.startService(ecbmIntent);
	                    }
	                } else {
	                    // It doesn't make sense to get ACTION_EMERGENCY_CALLBACK_MODE_CHANGED
	                    // on a device that doesn't support ECM in the first place.
	                    Log.e(LOG_TAG, "Got ACTION_EMERGENCY_CALLBACK_MODE_CHANGED, "
	                          + "but ECM isn't supported for phone: " + phone.getPhoneName());
	                }
	            } else if (action.equals(Intent.ACTION_CONFIGURATION_CHANGED)) {
	                for (Phone item : PhoneFactory.getPhones()) {
	                    if (item != null) {
	                        Log.d(LOG_TAG, "phone = " + item);
	                        //[ALPS02051538]-statr
	                        if (CdmaFeatureOptionUtils.isCdmaLteDcSupport()
	                                && item instanceof SvltePhoneProxy) {
	                            Log.d(LOG_TAG, "C2K refreshSpnDisplay");
	                            item.refreshSpnDisplay();
	                            ((SvltePhoneProxy)item).getLtePhone().refreshSpnDisplay();
	                        } else {
	                            item.refreshSpnDisplay();
	                        }
	                        //[ALPS02051538]-end
	                    }
	                }
	            } else if (action.equals(Intent.ACTION_MSIM_MODE_CHANGED)) {
	                int mode = intent.getIntExtra(Intent.EXTRA_MSIM_MODE, -1);
	                RadioManager.getInstance().notifyMSimModeChange(mode);
	            } else if (action.equals(ACTION_SHUTDOWN_IPO)) {
	                Log.d(LOG_TAG, "notify RadioManager of IPO shutdown");
	                RadioManager.getInstance().notifyIpoShutDown();
	            } else if (action.equals(ACTION_PREBOOT_IPO)) {
	                RadioManager.getInstance().notifyIpoPreBoot();
	            }  else {
	                super.onReceive(context, intent);
	            }
	        }
	    }
	  
	  
	    private void registerPhone(CallManager cm) {
	        final int simCount = TelephonyManager.getDefault().getSimCount();
	        Log.i(LOG_TAG, "simCount = " + simCount);
	        for (int i = 0; i < simCount; i++) { 
	        	Phone ss= PhoneFactory.getPhone(i);
	            cm.registerPhone(ss);
	            Log.i(LOG_TAG, "phone" + i + " = " + (ss instanceof ImsPhone));
	            Log.i(LOG_TAG, "phone" + i + " = " + PhoneFactory.getPhone(i).getForegroundCall());
	        }
	    }
	    
	    public Phone getPhone(int subscription) {
        	if(!AuroraPhoneUtils.isSimulate()) {
        		return PhoneFactory.getPhone(subscription);
        	} else {
        		return SimulatedPhoneFactory.getPhone(subscription);
        	}
	    }
	    
	    private HeadsetMediaButton mHeadsetMediaButton;
	    
	    public int getVoiceSubscription() {
	    	int subId = mSubscriptionManager.getDefaultVoiceSubId();
	        Log.i(LOG_TAG, "getVoiceSubscription subId = " + subId);
	        int slot = mSubscriptionManager.getSlotId(subId);
	        Log.i(LOG_TAG, "getVoiceSubscription slot = " + slot);
	        if(slot < 0) slot = 0; 
	    	return slot;
	    }
	    
	    @Override
	    public int getVoiceSubscriptionInService() {
	        int voiceSub = getVoiceSubscription();
	        //Emergency Call should always go on 1st sub .i.e.0
	        //when both the subscriptions are out of service
	        int sub = -1;
	        TelephonyManager tm = TelephonyManager.getDefault();
	        int count = PhoneUtils.getPhoneCount();

	        for (int i = 0; i < count; i++) {
	            Phone phone = getPhone(i);
	            int ss = phone.getServiceState().getState();
	            if ((ss == ServiceState.STATE_IN_SERVICE)
	                    || (phone.getServiceState().isEmergencyOnly())) {
	                sub = i;
	                if (sub == voiceSub) break;
	            }
	        }
	        if (DBG) Log.d(LOG_TAG, "Voice sub in service = "+ sub);

	        if (sub == -1) {
	            for (int i = 0; i < count; i++) {
	                if (tm.getSimState(i) == TelephonyManager.SIM_STATE_READY) {
	                    sub = i;
	                    if (sub == voiceSub) break;
	                }
	            }
	            if (sub == -1)
	                sub = 0;
	        }
	        Log.d(LOG_TAG, "Voice sub in service="+ sub +" preferred sub=" + voiceSub);

	        return sub;
	    }
	    
	    private void initForNewRadioTechnology(int phoneId) {
	        if (DBG) Log.d(LOG_TAG, "initForNewRadioTechnology...");

	        final Phone phone = PhoneFactory.getPhone(phoneId);

	        if (phone.getPhoneType() == PhoneConstants.PHONE_TYPE_CDMA) {
	            // Create an instance of CdmaPhoneCallState and initialize it to IDLE
	            cdmaPhoneCallState = new CdmaPhoneCallState();
	            cdmaPhoneCallState.CdmaPhoneCallStateInit();
	        }
	        if (!TelephonyCapabilities.supportsOtasp(phone)) {
	            //Clean up OTA data in GSM/UMTS. It is valid only for CDMA
	            clearOtaState();
	        }

	        ringer.updateRingerContextAfterRadioTechnologyChange(this.phone);
	        notifier.updateCallNotifierRegistrationsAfterRadioTechnologyChange();
	        if (mBluetoothPhone != null) {
	            try {
	                mBluetoothPhone.updateBtHandsfreeAfterRadioTechnologyChange();
	            } catch (RemoteException e) {
	                Log.e(LOG_TAG, Log.getStackTraceString(new Throwable()));
	            }
	        }
	        if (mInCallScreen != null) {
	            mInCallScreen.updateAfterRadioTechnologyChange();
	        }

	        // Update registration for ICC status after radio technology change
	        IccCard sim = phone.getIccCard();
	        if (sim != null) {
	            if (DBG) Log.d(LOG_TAG, "Update registration for ICC status...");

	            //Register all events new to the new active phone
	            sim.registerForNetworkLocked(mHandler, EVENT_SIM_NETWORK_LOCKED, phone);
	        }
	    }
	    
	    private void handleServiceStateChanged(Intent intent, Phone phone) {
	        // This function used to handle updating EriTextWidgetProvider

	        // If service just returned, start sending out the queued messages
	        ServiceState ss = ServiceState.newFromBundle(intent.getExtras());

	        if (ss != null) {
	            int state = ss.getState();
	            notificationMgr.updateNetworkSelection(state, phone);
	        }
	    }
	    
	    protected void onMMIComplete(AsyncResult r) {
	        if (VDBG) Log.d(LOG_TAG, "onMMIComplete()...");
	        MmiCode mmiCode = (MmiCode) r.result;
	        
	        final Message message = Message.obtain(mHandler, MMI_CANCEL);
	        
	        PhoneUtils.displayMMIComplete(mmiCode.getPhone(), getInstance(), mmiCode, message, null);
	    }
	    
	    

	    private List<SubscriptionInfo> mSubscriptionInfos;
	    
	    
	    /**
	     * Add listener to update screen if need.
	     * @param listener to monitor the change
	     */
	    public void addSubInfoUpdateListener(SubInfoUpdateListener listener) {
//	        Preconditions.checkNotNull(listener);
	        mSubInfoUpdateListeners.add(listener);
	    }

	    /**
	     * Remove listener that used update screen.
	     * @param listener to monitor the change
	     */
	    public void removeSubInfoUpdateListener(SubInfoUpdateListener listener) {
//	        Preconditions.checkNotNull(listener);
	        mSubInfoUpdateListeners.remove(listener);
	    }
	    
	private final SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangeListener = new SubscriptionManager.OnSubscriptionsChangedListener() {
		@Override
		public void onSubscriptionsChanged() {
			Log.d(LOG_TAG, "onSubscriptionsChanged start");
			
        	if(mSubscriptionManager.getDefaultDataSubId() < 0) {
        		List<SubscriptionInfo> l = PhoneUtils.getActiveSubInfoList();
        		if(l != null && l.size() > 0) {
	        		int subId = l.get(0).getSubscriptionId();
	        		mSubscriptionManager.setDefaultDataSubId(subId);
	        		mSubscriptionManager.setDefaultSmsSubId(subId);
	        		mSubscriptionManager.setDefaultVoiceSubId(subId);
        		}
        	}
			
			if (TelephonyUtils.isHotSwapHanppened(mSubscriptionInfos,
					PhoneUtils.getActiveSubInfoList())) {
				mSubscriptionInfos = PhoneUtils.getActiveSubInfoList();
				for (SubInfoUpdateListener listener : mSubInfoUpdateListeners) {
					listener.handleSubInfoUpdate();
				}
			}
			Log.d(LOG_TAG, "onSubscriptionsChanged end");
		}
	};
	    
	    
	    protected void initAuroraObjects() {
//	    	mAuroraHandAnswerManager = new AuroraHandAnswerManager(mCM, this);
//	    	mAuroraInCallGestureManager = new AuroraInCallGestureManager(mCM, this);
//    	    mStateHandler = new PhoneServiceStateHandler(this);
//            mStateHandler.addPhoneServiceStateListener(this);
	    	mManagePower = new ManagePower(mCM, this);
	        super.initAuroraObjects();
	        mManageReject.setNotificationMgr(I2RejectNotificationMgr.init(this));
	    }
	    
	    AuroraInCallGestureManager mAuroraInCallGestureManager;
	    AuroraHandAnswerManager mAuroraHandAnswerManager; 
	    ManagePower mManagePower;
	    
	    private BroadcastReceiver mCallLogReceiver = new BroadcastReceiver(){

	        @Override
	        public void onReceive(Context context, Intent intent) {
	        	notificationMgr.cancelMissedCallNotification();
	        }
	    
	    };
	    
	    private BroadcastReceiver mDdsReceiver = new BroadcastReceiver(){

	        @Override
	        public void onReceive(Context context, Intent intent) {
	        	int slot = mSubscriptionManager.getDefaultDataPhoneId();
	    		Log.d(LOG_TAG, "mDdsReceiver slot = " + slot);
	        	if (slot > -1) {
	    			AuroraNetworkUtils.setPreferredNetworkMode(Constants.NETWORK_MODE_LTE_GSM_WCDMA, slot);
	        	   getPhone(slot).setPreferredNetworkType(Constants.NETWORK_MODE_LTE_GSM_WCDMA, null);
	        	}
	        }
	    
	    };
	    
	    
//	    public void setDataEnableAfterSwitch(boolean value) {
//	    	mIsEnableDataWhenSwitchDone = value;
//	    	needDataSwitch = true;
//	    }
//	    
//	    private PhoneServiceStateHandler mStateHandler;
//		private boolean needDataSwitch = false;
//		private boolean mIsEnableDataWhenSwitchDone = false;
//	    
//	    public void onServiceStateChanged(ServiceState state, int subId) {
//	        Log.d(LOG_TAG, "PhoneStateListener:onServiceStateChanged: subId: " + subId
//	                + ", state: " + state);
//	        if(needDataSwitch) {
//	        	if(subId == mSubscriptionManager.getDefaultDataSubId()) {
//	        		if(state.getState() == ServiceState.STATE_IN_SERVICE) {
//	        			Log.d(LOG_TAG, "PhoneStateListener:onServiceStateChanged done");
//	        			TelephonyManager mTelephonyManager = TelephonyManager.from(this);
//	        			mTelephonyManager.setDataEnabled(subId, mIsEnableDataWhenSwitchDone);
//	        			needDataSwitch = false;
//	        		}
//	        	}
//	        }
//	    }
}