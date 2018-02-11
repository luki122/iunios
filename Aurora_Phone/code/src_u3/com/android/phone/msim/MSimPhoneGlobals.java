/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (c) 2011-2013 The Linux Foundation. All rights reserved.
 *
 * Not a Contribution.
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

package com.android.phone;

import com.codeaurora.telephony.msim.CardSubscriptionManager;
import com.android.internal.telephony.MSimConstants.CardUnavailableReason;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.IPowerManager;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UpdateLock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Telephony;
import android.provider.Settings.System;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.Sms;
//import android.provider.Telephony.SIMInfo;
//import android.provider.Telephony.SimInfo;
import android.telephony.ServiceState;
import android.util.Log;
import android.view.KeyEvent;
import android.telephony.TelephonyManager;
import android.telephony.MSimTelephonyManager;
import android.text.TextUtils;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyCapabilities;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.cdma.TtyIntent;
//import com.android.phone.common.CallLogAsync;
import com.android.phone.CallLogAsync;
import com.android.phone.OtaUtils.CdmaOtaScreenState;
import com.android.internal.telephony.PhoneConstants;
import com.codeaurora.telephony.msim.MSimPhoneFactory;
import com.codeaurora.telephony.msim.SubscriptionManager;
import com.codeaurora.telephony.msim.MSimTelephonyIntents;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import android.net.ConnectivityManager;
import com.android.phone.AuroraTelephony.SIMInfo;
import com.android.phone.AuroraTelephony.SimInfo;
import android.content.ContentUris;
import android.content.ContentValues;
import android.app.ActivityManagerNative;
import android.os.UserHandle;
import static android.Manifest.permission.READ_PHONE_STATE;
import android.os.Handler;
import android.os.Message;
import android.database.ContentObserver;
import android.database.Cursor;
import static com.android.internal.telephony.MSimConstants.SUBSCRIPTION_KEY;
import static com.android.internal.telephony.MSimConstants.EVENT_SUBSCRIPTION_ACTIVATED;
import static com.android.internal.telephony.MSimConstants.EVENT_SUBSCRIPTION_DEACTIVATED;
import aurora.preference.*;

import com.codeaurora.telephony.msim.MSimPhoneFactory;
import com.codeaurora.telephony.msim.Subscription.SubscriptionStatus;
import com.codeaurora.telephony.msim.SubscriptionManager;
import com.codeaurora.telephony.msim.CardSubscriptionManager;
import com.codeaurora.telephony.msim.SubscriptionData;
import com.codeaurora.telephony.msim.Subscription;

/**
 * Top-level Application class for the Phone app.
 */
public class MSimPhoneGlobals extends PhoneGlobals {
    /* package */ static final String LOG_TAG = "MSimPhoneGlobals";

    /**
     * Phone app-wide debug level:
     *   0 - no debug logging
     *   1 - normal debug logging if ro.debuggable is set (which is true in
     *       "eng" and "userdebug" builds but not "user" builds)
     *   2 - ultra-verbose debug logging
     *
     * Most individual classes in the phone app have a local DBG constant,
     * typically set to
     *   (PhoneApp.DBG_LEVEL >= 1) && (SystemProperties.getInt("ro.debuggable", 0) == 1)
     * or else
     *   (PhoneApp.DBG_LEVEL >= 2)
     * depending on the desired verbosity.
     */
    /* package */ static final int DBG_LEVEL = 3;

    //TODO DSDS,restore the logging levels
    private static final boolean DBG =
            (MSimPhoneGlobals.DBG_LEVEL >= 1) && (SystemProperties.getInt("ro.debuggable", 0) == 1);
    private static final boolean VDBG = (MSimPhoneGlobals.DBG_LEVEL >= 2);

    // Broadcast receiver for various intent broadcasts (see onCreate())
    private BroadcastReceiver mReceiver = new PhoneAppBroadcastReceiver();

    // Broadcast receiver purely for ACTION_MEDIA_BUTTON broadcasts
    private BroadcastReceiver mMediaButtonReceiver = new MediaButtonBroadcastReceiver();

    /* Array of MSPhone Objects to store each phoneproxy and associated objects */
    private static MSPhone[] mMSPhones;

    private int mDefaultSubscription = 0;

    MSimPhoneInterfaceManager phoneMgrMSim;

    MSimPhoneGlobals(Context context) {
        super(context);
        Log.d(LOG_TAG,"MSPhoneApp creation"+this);
    }

    public void onCreate() {
        if (VDBG) Log.v(LOG_TAG, "onCreate()...");
        Log.d(LOG_TAG, "MSimPhoneApp:"+this);

        ContentResolver resolver = getContentResolver();

        // Cache the "voice capable" flag.
        // This flag currently comes from a resource (which is
        // overrideable on a per-product basis):
//        sVoiceCapable =
//                getResources().getBoolean(com.android.internal.R.bool.config_voice_capable);
        sVoiceCapable = true;
        // ...but this might eventually become a PackageManager "system
        // feature" instead, in which case we'd do something like:
        // sVoiceCapable =
        //   getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY_VOICE_CALLS);

        if (phone == null) {
            
        	if(AuroraPhoneUtils.isSimulate()) {
	        	MSimSimulatedPhoneFactory.makeMultiSimDefaultPhones(this);
	            phone = MSimSimulatedPhoneFactory.getDefaultPhone();
        	} else {
                // Initialize the telephony framework
                MSimPhoneFactory.makeMultiSimDefaultPhones(this);

                // Get the default phone
                phone = MSimPhoneFactory.getDefaultPhone();
        	}

            // Start TelephonyDebugService After the default phone is created.
            Intent intent = new Intent(this, TelephonyDebugService.class);
            startService(intent);

            mCM = CallManager.getInstance();

            int numPhones = MSimTelephonyManager.getDefault().getPhoneCount();
            // Create MSPhone which hold phone proxy and its corresponding memebers.
            mMSPhones = new MSPhone[numPhones];
            for(int i = 0; i < numPhones; i++) {
                mMSPhones [i] = new MSPhone(i);
                mCM.registerPhone(mMSPhones[i].mPhone);
            }

            // Get the default subscription from the system property
            mDefaultSubscription = getDefaultSubscription();

            // Set Default PhoneApp variables
            setDefaultPhone(mDefaultSubscription);
            mCM.registerPhone(phone);

            // Create the NotificationMgr singleton, which is used to display
            // status bar icons and control other status bar behavior.
            notificationMgr = MSimNotificationMgr.init(this);

            phoneMgr = PhoneInterfaceManager.init(this, phone);
            phoneMgrMSim = MSimPhoneInterfaceManager.init(this, phone);

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
                startService(new Intent(this, BluetoothPhoneService.class));
                bindService(new Intent(this, BluetoothPhoneService.class),
                            mBluetoothPhoneConnection, 0);
            } else {
                // Device is not bluetooth capable
                mBluetoothPhone = null;
            }

            ringer = Ringer.init(this);

            mReceiver = new MSimPhoneAppBroadcastReceiver();
            mMediaButtonReceiver = new MSimMediaButtonBroadcastReceiver();

            // before registering for phone state changes
            mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, LOG_TAG);
            // lock used to keep the processor awake, when we don't care for the display.
            mPartialWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                    | PowerManager.ON_AFTER_RELEASE, LOG_TAG);
            // Wake lock used to control proximity sensor behavior.
            if (mPowerManager.isWakeLockLevelSupported(
                    PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
                mProximityWakeLock = mPowerManager.newWakeLock(
                        PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, LOG_TAG);
            }
            if (DBG) Log.d(LOG_TAG, "onCreate: mProximityWakeLock: " + mProximityWakeLock);

            // create mAccelerometerListener only if we are using the proximity sensor
            if (proximitySensorModeEnabled()) {
                mAccelerometerListener = new AccelerometerListener(this, this);
            }

            mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

            // get a handle to the service so that we can use it later when we
            // want to set the poke lock.
            mPowerManagerService = IPowerManager.Stub.asInterface(
                    ServiceManager.getService("power"));

            // TODO DSDS: See if something specific needs to be done for DSDS
            // Get UpdateLock to suppress system-update related events (e.g. dialog show-up)
            // during phone calls.
            mUpdateLock = new UpdateLock("phone");

            if (DBG) Log.d(LOG_TAG, "onCreate: mUpdateLock: " + mUpdateLock);

            CallLogger callLogger = new CallLogger(this, new CallLogAsync());

            // Create the CallController singleton, which is the interface
            // to the telephony layer for user-initiated telephony functionality
            // (like making outgoing calls.)
            callController = MSimCallController.init(this, callLogger);
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
            notifier = MSimCallNotifier.init(this, phone, ringer, callLogger);

            // Create the Managed Roaming singleton class, used to show popup
            // to user for initiating network search when location update is rejected
            mManagedRoam = ManagedRoaming.init(this);

            XDivertUtility.init(this, phone, (MSimCallNotifier)notifier, this);

            // register for ICC status
            for (int i = 0; i < MSimTelephonyManager.getDefault().getPhoneCount(); i++) {
                IccCard sim = getPhone(i).getIccCard();
                if (sim != null) {
                    if (VDBG) Log.v(LOG_TAG, "register for ICC status on subscription: " + i);
                    sim.registerForPersoLocked(mHandler,
                            EVENT_PERSO_LOCKED, new Integer(i));
                }
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
            intentFilter.addAction(MSimTelephonyIntents.ACTION_DEFAULT_SUBSCRIPTION_CHANGED);
            if (mTtyEnabled) {
                intentFilter.addAction(TtyIntent.TTY_PREFERRED_MODE_CHANGE_ACTION);
            }
            intentFilter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
            registerReceiver(mReceiver, intentFilter);

            // Use a separate receiver for ACTION_MEDIA_BUTTON broadcasts,
            // since we need to manually adjust its priority (to make sure
            // we get these intents *before* the media player.)
            IntentFilter mediaButtonIntentFilter =
                    new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
            // TODO verify the independent priority doesn't need to be handled thanks to the
            //  private intent handler registration
            // Make sure we're higher priority than the media player's
            // MediaButtonIntentReceiver (which currently has the default
            // priority of zero; see apps/Music/AndroidManifest.xml.)
            mediaButtonIntentFilter.setPriority(1);
            //
            registerReceiver(mMediaButtonReceiver, mediaButtonIntentFilter);
            // register the component so it gets priority for calls
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            am.registerMediaButtonEventReceiverForCalls(new ComponentName(this.getPackageName(),
                    MediaButtonBroadcastReceiver.class.getName()));

            //set the default values for the preferences in the phone.
            AuroraPreferenceManager.setDefaultValues(this, R.xml.network_setting, false);

            AuroraPreferenceManager.setDefaultValues(this, R.xml.call_feature_setting, false);

            // Make sure the audio mode (along with some
            // audio-mode-related state of our own) is initialized
            // correctly, given the current state of the phone.
            PhoneUtils.setAudioMode(mCM);
        }

        for (int i = 0; i < MSimTelephonyManager.getDefault().getPhoneCount(); i++) {
            updatePhoneAppCdmaVariables(i);
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
        if (mTtyEnabled) {
            mPreferredTtyMode = android.provider.Settings.Secure.getInt(
                    phone.getContext().getContentResolver(),
                    android.provider.Settings.Secure.PREFERRED_TTY_MODE,
                    Phone.TTY_MODE_OFF);
            mHandler.sendMessage(mHandler.obtainMessage(EVENT_TTY_PREFERRED_MODE_CHANGED, 0));
        }
        // Read HAC settings and configure audio hardware
        if (getResources().getBoolean(R.bool.hac_enabled)) {
            int hac = android.provider.Settings.System.getInt(
                    phone.getContext().getContentResolver(),
                    android.provider.Settings.System.HEARING_AID, 0);
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.setParameter(CallFeaturesSetting.HAC_KEY, hac != 0 ?
                                      CallFeaturesSetting.HAC_VAL_ON :
                                      CallFeaturesSetting.HAC_VAL_OFF);
        }
                
        initAuroraObjects();
    }

    /**
     * Returns an Intent that can be used to go to the "Call log"
     * UI (aka CallLogActivity) in the Contacts app.
     *
     * Watch out: there's no guarantee that the system has any activity to
     * handle this intent.  (In particular there may be no "Call log" at
     * all on on non-voice-capable devices.)
     */
    /* package */ Intent createCallLogIntent(int subscription) {
        Intent  intent = new Intent(Intent.ACTION_VIEW, null);
        intent.putExtra(SUBSCRIPTION_KEY, subscription);
        intent.setType("vnd.android.cursor.dir/calls");
        return intent;
    }

    /**
     * Return an Intent that can be used to bring up the in-call screen.
     *
     * This intent can only be used from within the Phone app, since the
     * InCallScreen is not exported from our AndroidManifest.
     */
    @Override
    /* package */ Intent createInCallIntent(int subscription) {
        Log.d(LOG_TAG, "createInCallIntent subscription:");
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.putExtra(SUBSCRIPTION_KEY, subscription);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        intent.setClassName("com.android.phone", getCallScreenClassName());
        return intent;
    }

    protected static String getCallScreenClassName() {
        return MSimInCallScreen.class.getName();
    }

    @Override
    /* package */ void displayCallScreen() {
        if (VDBG) Log.d(LOG_TAG, "displayCallScreen()...");

        // On non-voice-capable devices we shouldn't ever be trying to
        // bring up the InCallScreen in the first place.
        if (!sVoiceCapable) {
            Log.w(LOG_TAG, "displayCallScreen() not allowed: non-voice-capable device",
                  new Throwable("stack dump"));  // Include a stack trace since this warning
                                                 // indicates a bug in our caller
            return;
        }

        try {
            startActivity(createInCallIntent(mCM.getPhoneInCall().getSubscription()));
        } catch (ActivityNotFoundException e) {
            // It's possible that the in-call UI might not exist (like on
            // non-voice-capable devices), so don't crash if someone
            // accidentally tries to bring it up...
            Log.w(LOG_TAG, "displayCallScreen: transition to InCallScreen failed: " + e);
        }
        Profiler.callScreenRequested();
    }

    boolean isSimPinEnabled(int subscription) {
        MSPhone msPhone = getMSPhone(subscription);
        return msPhone.mIsSimPinEnabled;
    }

    /**
     * Dismisses the in-call UI.        private CardSubscriptionManager mCardSubMgr;
     *
     * This also ensures that you won't be able to get back to the in-call
     * UI via the BACK button (since this call removes the InCallScreen
     * from the activity history.)
     * For OTA Call, it call InCallScreen api to handle OTA Call End scenario
     * to display OTA Call End screen.
     */
    /* package */
    void dismissCallScreen(Phone phone) {
        if (mInCallScreen != null) {
            if ((TelephonyCapabilities.supportsOtasp(phone)) &&
                    (mInCallScreen.isOtaCallInActiveState()
                    || mInCallScreen.isOtaCallInEndState()
                    || ((cdmaOtaScreenState != null)
                    && (cdmaOtaScreenState.otaScreenState
                            != CdmaOtaScreenState.OtaScreenState.OTA_STATUS_UNDEFINED)))) {
                // TODO: During OTA Call, display should not become dark to
                // allow user to see OTA UI update. Phone app needs to hold
                // a SCREEN_DIM_WAKE_LOCK wake lock during the entire OTA call.
                wakeUpScreen();
                // If InCallScreen is not in foreground we resume it to show the OTA call end screen
                // Fire off the InCallScreen intent
                displayCallScreen();

                mInCallScreen.handleOtaCallEnd();
                return;
            } else {
                mInCallScreen.finish();
            }
        }
    }


    @Override
    public void onMMIComplete(AsyncResult r) {
        if (VDBG) Log.d(LOG_TAG, "onMMIComplete()...");
        MmiCode mmiCode = (MmiCode) r.result;
        Phone localPhone = (Phone) mmiCode.getPhone();
        PhoneUtils.displayMMIComplete(localPhone, getInstance(), mmiCode, null, null);
    }

    void initForNewRadioTechnology(int subscription) {
        if (DBG) Log.d(LOG_TAG, "initForNewRadioTechnology...");
        MSPhone msPhone = getMSPhone(subscription);

        Phone phone = msPhone.mPhone;

        if (TelephonyCapabilities.supportsOtasp(phone)) {
           // Create an instance of CdmaPhoneCallState and initialize it to IDLE
           msPhone.initializeCdmaVariables();
           updatePhoneAppCdmaVariables(subscription);
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
            sim.registerForPersoLocked(mHandler, EVENT_PERSO_LOCKED, null);
        }
    }

    /**
     * Receiver for misc intent broadcasts the Phone app cares about.
     */
    private class MSimPhoneAppBroadcastReceiver extends PhoneGlobals.PhoneAppBroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v(LOG_TAG,"Action intent recieved:"+action);
            //gets the subscription information ( "0" or "1")
            int subscription = intent.getIntExtra(SUBSCRIPTION_KEY, getDefaultSubscription());
            if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                boolean enabled = System.getInt(getContentResolver(),
                        System.AIRPLANE_MODE_ON, 0) == 0;
                // Set the airplane mode property for RIL to read on boot up
                // to know if the phone is in airplane mode so that RIL can
                // power down the ICC card.
                Log.d(LOG_TAG, "Setting property " + PROPERTY_AIRPLANE_MODE_ON);
                // enabled here implies airplane mode is OFF from above condition
                SystemProperties.set(PROPERTY_AIRPLANE_MODE_ON, (enabled ? "0" : "1"));
                for (int i = 0; i < MSimTelephonyManager.getDefault().getPhoneCount(); i++) {
                    getPhone(i).setRadioPower(enabled);
                }

            } else if ((action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) &&
                    (mPUKEntryActivity != null)) {
                // if an attempt to un-PUK-lock the device was made, while we're
                // receiving this state change notification, notify the handler.
                // NOTE: This is ONLY triggered if an attempt to un-PUK-lock has
                // been attempted.
                mHandler.sendMessage(mHandler.obtainMessage(EVENT_SIM_STATE_CHANGED,
                        intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE)));
            } else if (action.equals(TelephonyIntents.ACTION_RADIO_TECHNOLOGY_CHANGED)) {
                String newPhone = intent.getStringExtra(PhoneConstants.PHONE_NAME_KEY);
                Log.d(LOG_TAG, "Radio technology switched. Now " + newPhone + " is active.");
                initForNewRadioTechnology(subscription);
            } else if (action.equals(TelephonyIntents.ACTION_SERVICE_STATE_CHANGED)) {
                Phone phone = getPhone(subscription);
                handleServiceStateChanged(intent, phone);
            } else if (action.equals(TelephonyIntents.ACTION_EMERGENCY_CALLBACK_MODE_CHANGED)) {
                Phone phone = getPhone(subscription);
                if (TelephonyCapabilities.supportsEcm(phone)) {
                    Log.d(LOG_TAG, "Emergency Callback Mode arrived in PhoneApp"
                            + " on Sub =" + subscription);
                    // Start Emergency Callback Mode service
                    if (intent.getBooleanExtra("phoneinECMState", false)) {
                        Intent ecbmIntent = new Intent(context, EmergencyCallbackModeService.class);
                        ecbmIntent.putExtra(SUBSCRIPTION_KEY, subscription);
                        context.startService(ecbmIntent);
                    }
                } else {
                    // It doesn't make sense to get ACTION_EMERGENCY_CALLBACK_MODE_CHANGED
                    // on a device that doesn't support ECM in the first place.
                    Log.e(LOG_TAG, "Got ACTION_EMERGENCY_CALLBACK_MODE_CHANGED, "
                          + "but ECM isn't supported for phone: " + phone.getPhoneName());
                }
            } else if (action.equals(MSimTelephonyIntents.ACTION_DEFAULT_SUBSCRIPTION_CHANGED)) {
                Log.d(LOG_TAG, "Default subscription changed, subscription: " + subscription);
                mDefaultSubscription = subscription;
                setDefaultPhone(subscription);
                phoneMgr.setPhone(phone);
            } else {
                super.onReceive(context, intent);
            }
        }
    }

    /**
     * Broadcast receiver for the ACTION_MEDIA_BUTTON broadcast intent.
     *
     * This functionality isn't lumped in with the other intents in
     * PhoneAppBroadcastReceiver because we instantiate this as a totally
     * separate BroadcastReceiver instance, since we need to manually
     * adjust its IntentFilter's priority (to make sure we get these
     * intents *before* the media player.)
     */
    private class MSimMediaButtonBroadcastReceiver extends
            PhoneGlobals.MediaButtonBroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            Log.d(LOG_TAG, "MediaButtonBroadcastReceiver.onReceive() event = " + event);
            if ((event != null)
                    && (event.getKeyCode() == KeyEvent.KEYCODE_HEADSETHOOK)) {
                if (VDBG) Log.d(LOG_TAG, "MediaButtonBroadcastReceiver: HEADSETHOOK");

                for (int i = 0; i < MSimTelephonyManager.getDefault().getPhoneCount(); i++) {
                    boolean consumed = PhoneUtils.handleHeadsetHook(getPhone(i), event);
                    Log.d(LOG_TAG, "handleHeadsetHook(): consumed = " + consumed +
                            " on SUB ["+i+"]");
                    if (consumed) {
                        // If a headset is attached and the press is consumed, also update
                        // any UI items (such as an InCallScreen mute button) that may need to
                        // be updated if their state changed.
                        updateInCallScreen();  // Has no effect if the InCallScreen isn't visible
                        abortBroadcast();
                        break;
                    }
                }
            } else {
                if (mCM.getState() != PhoneConstants.State.IDLE) {
                    // If the phone is anything other than completely idle,
                    // then we consume and ignore any media key events,
                    // Otherwise it is too easy to accidentally start
                    // playing music while a phone call is in progress.
                    if (VDBG) Log.d(LOG_TAG, "MediaButtonBroadcastReceiver: consumed");
                    abortBroadcast();
                }
            }
        }
    }

    // updates cdma variables of PhoneApp
    private void updatePhoneAppCdmaVariables(int subscription) {
        Log.v(LOG_TAG,"updatePhoneAppCdmaVariables for SUB" + subscription);
        MSPhone msPhone = getMSPhone(subscription);

        if ((msPhone != null) &&(msPhone.mPhone.getPhoneType() == PhoneConstants.PHONE_TYPE_CDMA)) {
            cdmaPhoneCallState = msPhone.mCdmaPhoneCallState;
            cdmaOtaProvisionData = msPhone.mCdmaOtaProvisionData;
            cdmaOtaConfigData = msPhone.mCdmaOtaConfigData;
            cdmaOtaScreenState = msPhone.mCdmaOtaScreenState;
            cdmaOtaInCallScreenUiState = msPhone.mCdmaOtaInCallScreenUiState;
        }
    }

    private void clearCdmaVariables(int subscription) {
        MSPhone msPhone = getMSPhone(subscription);
        msPhone.clearCdmaVariables();
        cdmaPhoneCallState = null;
        cdmaOtaProvisionData = null;
        cdmaOtaConfigData = null;
        cdmaOtaScreenState = null;
        cdmaOtaInCallScreenUiState = null;
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

    // gets the MSPhone corresponding to a subscription
    static private MSPhone getMSPhone(int subscription) {
        try {
            return mMSPhones[subscription];
        } catch (IndexOutOfBoundsException e) {
            Log.e(LOG_TAG,"subscripton Index out of bounds "+e);
            return null;
        }
    }

    // gets the Default Phone
    Phone getDefaultPhone() {
        return getPhone(getDefaultSubscription());
    }

    // Gets the Phone correspoding to a subscription
    // Access this method using MSimPhoneGlobals.getInstance().getPhone(sub);
    public Phone getPhone(int subscription) {
        MSPhone msPhone= getMSPhone(subscription);
        if (msPhone != null) {
            return msPhone.mPhone;
        } else {
            Log.w(LOG_TAG, "msPhone object is null returning default phone");
            return phone;
        }
    }

    /**
      * Get the subscription that has service
      * Following are the conditions applicable when deciding the subscription for dial
      * 1. Place E911 call on a sub whichever is IN_SERVICE/Limited Service(sub need not be
      *    user preferred voice sub)
      * 2. If both subs are activated and out of service(i.e. other than limited/in service)
      *    place call on voice pref sub.
      * 3. If both subs are not activated(i.e NO SIM/PIN/PUK lock state) then choose
      *    the first sub by default for placing E911 call.
      */
    @Override
    public int getVoiceSubscriptionInService() {
        int voiceSub = getVoiceSubscription();
        //Emergency Call should always go on 1st sub .i.e.0
        //when both the subscriptions are out of service
        int sub = -1;
        MSimTelephonyManager tm = MSimTelephonyManager.getDefault();
        int count = tm.getPhoneCount();
        SubscriptionManager subManager = SubscriptionManager.getInstance();

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

    CdmaPhoneCallState getCdmaPhoneCallState (int subscription) {
        MSPhone msPhone = getMSPhone(subscription);
        if (msPhone == null) {
            return null;
        }
        return msPhone.mCdmaPhoneCallState;
    }

    //Sets the default phoneApp variables
    void setDefaultPhone(int subscription){
        //When default phone dynamically changes need to handle
        MSPhone msPhone = getMSPhone(subscription);
        phone = msPhone.mPhone;
        mLastPhoneState = msPhone.mLastPhoneState;
        updatePhoneAppCdmaVariables(subscription);
        mDefaultSubscription = subscription;
    }
    /*
     * Gets the default subscription
     */
    @Override
    public int getDefaultSubscription() {
    	if(AuroraPhoneUtils.isSimulate()) {
    		return 0;
    	} else {
            return MSimPhoneFactory.getDefaultSubscription();
    	}
    }

    /*
     * Gets User preferred Voice subscription setting
     */
    @Override
    public int getVoiceSubscription() {
    	if(AuroraPhoneUtils.isSimulate()) {
    		return 0;
    	} else {
            return MSimPhoneFactory.getVoiceSubscription();
    	}
    }

    /*
     * Gets User preferred Data subscription setting
     */
    @Override
    public int getDataSubscription() {
    	//only for U3
    	if(AuroraPhoneUtils.isSimulate()) {
    		return 0;
    	} else {
            return MSimPhoneFactory.getDataSubscription();
    	}
//    	return 0;
    }

    /*
     * Gets default/user preferred Data subscription setting
     */
//    @Override
//    public int getDefaultDataSubscription() {
//        return MSimPhoneFactory.getDefaultDataSubscription();
//    }

    /*
     * Gets User preferred SMS subscription setting
     */
    public int getSMSSubscription() {        
    	if(AuroraPhoneUtils.isSimulate()) {
    		return 0;
    	} else {
            return MSimPhoneFactory.getSMSSubscription();
    	}
    }
    
    SubscriptionManager mSubscriptionManager;
    private CardSubscriptionManager mCardSubMgr;
    private static final int EVENT_CARD_INFO_AVAILABLE = 0;
    private static final int EVENT_CARD_INFO_NOT_AVAILABLE = 1;
    private static final int EVENT_SET_NEW_SUBSCRIPTION_TIMEOUT = 2;
    private static final int EVENT_DDS_SWITCH = 3;
    private static final int MESSAGE_SET_PREFERRED_NETWORK_TYPE = 5;
    private static final int EVENT_PROCESS_DDS_SWITCH_LATER = 6;
    
    private Handler mCardHandler = new Handler() {
        	@Override
	        public void handleMessage(Message msg) {
		        AsyncResult ar;
		        Integer subId;
		        switch(msg.what) {
			        case EVENT_CARD_INFO_AVAILABLE:
			            Log.d(LOG_TAG,"EVENT_CARD_INFO_AVAILABLE");
			            processCardInfoAvailable((AsyncResult)msg.obj);
			            break;
			
			        case EVENT_CARD_INFO_NOT_AVAILABLE:
			        	Log.d(LOG_TAG,"EVENT_CARD_INFO_NOT_AVAILABLE");
			            processCardInfoNotAvailable((AsyncResult)msg.obj);
			            break;
			        case EVENT_SET_NEW_SUBSCRIPTION_TIMEOUT:
			        	Log.d(LOG_TAG,"EVENT_SET_NEW_SUBSCRIPTION_TIMEOUT");
			        	if(mIsNewCardPending) {
	                        setNewCardSubscription();
	                    	mIsNewCardPending = false;
	                    }
			        	break;
		            case EVENT_SUBSCRIPTION_ACTIVATED:
		            	Log.d(LOG_TAG, "EVENT_SUBSCRIPTION_ACTIVATED");
		                onSubscriptionActivated((AsyncResult)msg.obj);
		                break;

		            case EVENT_SUBSCRIPTION_DEACTIVATED:
		            	Log.d(LOG_TAG, "EVENT_SUBSCRIPTION_DEACTIVATED");
		                onSubscriptionDeactivated((AsyncResult)msg.obj);
		                break;
		            case EVENT_DDS_SWITCH:
		            	Log.d(LOG_TAG, "EVENT_DDS_SWITCH");
		            	if(this.hasMessages(EVENT_PROCESS_DDS_SWITCH_LATER)) {
		            		this.removeMessages(EVENT_PROCESS_DDS_SWITCH_LATER);
		            	}
		            	this.sendEmptyMessageDelayed(EVENT_PROCESS_DDS_SWITCH_LATER, 2000);
		            	break;
		            case EVENT_PROCESS_DDS_SWITCH_LATER:
		            	handleDdsSwitchStep1();
		            	break;
		            case MESSAGE_SET_PREFERRED_NETWORK_TYPE:
		            	Log.d(LOG_TAG, "MESSAGE_SET_PREFERRED_NETWORK_TYPE");
		            	handleDdsSwitchStep2(msg);
		            	break;
		            default:
		                break;
		        }
               Intent intent = new Intent("PhoneServiceStateChanged");
               //aurora add liguangyu 20140825 for BUG #7913 start
               android.provider.Settings.Global.putInt(getContentResolver(),
            		   "mobile_data"+ 2, simcardstate);
               //aurora add liguangyu 20140825 for BUG #7913 end
               sendBroadcast(intent);
        }
    };
    
    
    private void processCardInfoAvailable(AsyncResult ar) {
        Integer cardIndex = (Integer)ar.userObj;

        
        getIccIdsDone(true ,cardIndex);
//        if (!mRadioOn[cardIndex]) {
//        	Log.d(LOG_TAG,"processCardInfoAvailable: Radio Not Available on cardIndex = " + cardIndex);
//           return;
//        }
//
//        mCardInfoAvailable[cardIndex] = true;
//
//        Log.d(LOG_TAG,"processCardInfoAvailable: CARD:" + cardIndex + " is available");
//
//        // Card info on slot cardIndex is available.
//        // Check if any user preferred subscriptions are available in
//        // this card.  If there is any, and which are not yet activated,
//        // activate them!
//        updateActivatePendingList(cardIndex);
//
//        if (!isAllCardsInfoAvailable()) {
//        	Log.d(LOG_TAG,"All cards info not available!! Waiting for all info before processing");
//            return;
//        }
//
//        logd("processCardInfoAvailable: mSetSubscriptionInProgress = "
//                + mSetSubscriptionInProgress);
//
//        if (!mSetSubscriptionInProgress) {
//            processActivateRequests();
//        }
//
//        notifyIfAnyNewCardsAvailable();
    }
    
    
    private void processCardInfoNotAvailable(AsyncResult ar) {
        if (ar.exception != null || ar.result == null) {
        	Log.d(LOG_TAG,"processCardInfoNotAvailable - Exception");
            return;
        }

        Integer cardIndex = (Integer)ar.userObj;
        CardUnavailableReason reason = (CardUnavailableReason)ar.result;

        Log.d(LOG_TAG,"processCardInfoNotAvailable on cardIndex = " + cardIndex
                + " reason = " + reason);
        getIccIdsDone(false ,cardIndex);
//        mCardInfoAvailable[cardIndex] = false;
//
//        // Set subscription is required if both the cards are unavailable
//        // and when those are available next time!
//
//        boolean subscriptionRequired = true;
//        for (int i = 0; i < mNumPhones; i++) {
//            subscriptionRequired = subscriptionRequired && !mCardInfoAvailable[i];
//        }
//
//        // Reset the current subscription and notify the subscriptions deactivated.
//        // Notify only in case of radio off and SIM Refresh reset.
//        if (reason == CardUnavailableReason.REASON_RADIO_UNAVAILABLE
//                || reason == CardUnavailableReason.REASON_SIM_REFRESH_RESET) {
//            // Card has been removed from slot - cardIndex.
//            // Mark the active subscription from this card as de-activated!!
//            for (int i = 0; i < mNumPhones; i++) {
//                SubscriptionId sub = SubscriptionId.values()[i];
//                if (getCurrentSubscription(sub).slotId == cardIndex) {
//                    resetCurrentSubscription(sub);
//                    notifySubscriptionDeactivated(sub.ordinal());
//                }
//            }
//        }
//
//        if (reason == CardUnavailableReason.REASON_RADIO_UNAVAILABLE) {
//            mAllCardsStatusAvailable = false;
//        }
    }
    
    
    
    
    
    
    
    public static final long DEFAULT_SIM_NOT_SET = -5;
    public static int simcardstate=0;//aurora add zhouxiaobing 20140605 for sim2 can't internet;
    
	public Runnable runnable_pre = new Runnable() {
		@Override
		public void run() {

			MSimTelephonyManager.getDefault().setPreferredDataSubscription(0);
		}
	};
	
	public Runnable runnable_apn1 = new Runnable() {
		@Override
		public void run() {

			if (simcardstate == 1) {
				phoneMgrMSim.disableApnType("SIM2APN");
				Thread th = new Thread(runnable_pre);
				th.start();
			}
		}
	};
	
	public Runnable runnable_apn2 = new Runnable() {
		@Override
		public void run() {

			if (simcardstate == 2) {
				phoneMgrMSim.disableApnType("SIM2APN");
			}

		}
	};
	
	public Runnable runnable_apn3 = new Runnable() {
		@Override
		public void run() {
			if (simcardstate == 3) {
				phoneMgrMSim.disableApnType("SIM2APN");
				Thread th = new Thread(runnable_pre);
				th.start();
			}

		}
	};
    private void getIccIdsDone(boolean hasSIM, int mSimId) {
        String oldIccIdInSlot = null;
        Log.d(LOG_TAG, "getIccIdsDone  mSimId = " + mSimId +" hasSIM="+hasSIM+" simcardstate="+simcardstate);
        
      //aurora add zhouxiaobing 20140605 for sim2 can't internet;        

        //aurora add zhouxiaobing 20140605 for sim2 can't internet;        
//        if(hasSIM)
//        {
//        	simcardstate|=(mSimId+1);
//        }
//        else
//        {
//        	simcardstate&=(~(mSimId+1));
//        }
//        if(simcardstate==1)
//        {
// 		    mCardHandler.postDelayed(runnable_apn1,3000);	
//        }
//        else if(simcardstate==2)
//        {
//
// 		    mCardHandler.postDelayed(runnable_apn2,3000);
//        }
//        else if(simcardstate==3)
//        {
// 		    mCardHandler.postDelayed(runnable_apn3,3000);
//        }
      //aurora add zhouxiaobing 20140605 for sim2 can't internet;  

        SIMInfo oldSimInfo = SIMInfo.getSIMInfoBySlot(this, mSimId);
        if (oldSimInfo != null) {
            oldIccIdInSlot = oldSimInfo.mICCId;
            if(oldIccIdInSlot == null) {
	            Cursor cursor = getContentResolver().query(SimInfo.CONTENT_URI, 
	                    null, SimInfo.SLOT + "=?", new String[]{String.valueOf(mSimId)}, null);
	            try {
	                if (cursor != null) {
	                    if (cursor.moveToFirst()) {
	                    	oldIccIdInSlot =  cursor.getString(cursor.getColumnIndexOrThrow(SimInfo.ICC_ID));
	                    }
	                }
	            } catch (Exception e) {
	            	  Log.d(LOG_TAG, "getIccIdsDone  e = " + e );
	            } finally {
	                if (cursor != null) {
	                    cursor.close();
	                }
	            }
            }
            Log.d(LOG_TAG, "getIccIdsDone old IccId In Slot" + mSimId +" is " + oldIccIdInSlot);
            ContentValues value = new ContentValues(1);
            value.put(SimInfo.SLOT, -1);
            this.getContentResolver().update(ContentUris.withAppendedId(SimInfo.CONTENT_URI, oldSimInfo.mSimId), 
                        value, null, null);                 
        } else {
            Log.d(LOG_TAG, "getIccIdsDone No sim in slot" + mSimId + " for last time " );
        }
        
        if(!hasSIM) {
        	return;
        }
        
        //check if the Inserted sim is new
        int nNewCardCount = 0;        
//        String iccid = getPhone(mSimId).getIccSerialNumber();
        String iccid = mCardSubMgr.getCardSubscriptions(mSimId).getIccId();
        String otherIccid = "";
        if(mCardSubMgr.getCardSubscriptions(mSimId > 0 ? 0 : 1) != null) {
        	otherIccid = mCardSubMgr.getCardSubscriptions(mSimId > 0 ? 0 : 1).getIccId();
//            otherIccid = "123456";
        }
        
//        iccid = "123456";
        Log.d(LOG_TAG, "getIccIdsDone  iccid =" + iccid  + " otherIccid = " + otherIccid );
        
                 
        if (iccid != null && !iccid.equals("")) { 
        	SIMInfo simInfoByIcc = SIMInfo.getSIMInfoByICCId(this, iccid);
        	if(iccid.equalsIgnoreCase(otherIccid)) {
    			long secondId = getSIMInfoIdByICCIdAndNumber(iccid, mSimId);
    			if(secondId == -1) {
                    nNewCardCount++;
					if (simInfoByIcc != null) {
						long otherSimId = simInfoByIcc.mSimId;
						Uri uri = ContentUris.withAppendedId(SimInfo.CONTENT_URI, otherSimId);
						ContentValues values = new ContentValues(1);
						values.put(SimInfo.NUMBER, (mSimId > 0 ? 0 : 1) + "");
						this.getContentResolver().update(uri, values, null,null);
					}  
    			}
                   
                Uri uri= insertICCIdAndNumber(iccid, mSimId); 	
        	} else {
	            if (!iccid.equals(oldIccIdInSlot) && simInfoByIcc == null){
	                //one new card inserted into slot1
	                nNewCardCount++;                      
	            }
	
	            Uri uri= SIMInfo.insertICCId(this, iccid, mSimId); 	
	            Log.d(LOG_TAG, "uri  iccid =" + uri  );
        	}
		} else {
        	SIMInfo simInfoByIcc = SIMInfo.getSIMInfoByICCId(this, "NULL");
        	if(TextUtils.isEmpty(otherIccid) && mCardSubMgr.getCardSubscriptions(mSimId > 0 ? 0 : 1) != null ){            	
    			long secondId = getSIMInfoIdByICCIdAndNumber("NULL", mSimId);
    			if(secondId == -1) {
                    nNewCardCount++;
					if (simInfoByIcc != null) {
						long otherSimId = simInfoByIcc.mSimId;
						Uri uri = ContentUris.withAppendedId(SimInfo.CONTENT_URI, otherSimId);
						ContentValues values = new ContentValues(1);
						values.put(SimInfo.NUMBER, (mSimId > 0 ? 0 : 1) + "");
						this.getContentResolver().update(uri, values, null,null);
					}
    			}    
                   
                Uri uri= insertICCIdAndNumber("NULL", mSimId); 		
        	} else {
	            if (simInfoByIcc == null){
	                //one new card inserted into slot1
	                nNewCardCount++;                      
	            }
	
	            Uri uri= SIMInfo.insertICCId(this, "NULL", mSimId); 	
	            Log.d(LOG_TAG, "uri  iccid =" + uri  );
        	}
		}   
     
        
        if (nNewCardCount > 0 ) {   
        	Log.d(LOG_TAG,"getIccIdsDone New SIM detected. " );
            setColorForNewSIM(mSimId);
            notifyNewCardsAvailable(mSimId);
            if (!mSubscriptionManager.isSetSubscriptionInProgress()) {
        	    setNewCardSubscription();
	        } else {
	     	    Log.d(LOG_TAG, "setNewCardSubscription: delay");
	     	    mIsNewCardPending = true;
	     	    mCardHandler.sendEmptyMessageDelayed(EVENT_SET_NEW_SUBSCRIPTION_TIMEOUT, 4000);
	        }
        } else {
     	    mHandler.postDelayed(new Runnable(){
     	    	public void run(){
                    updateCardState();
     	    	}
     	    }, 1000);
        }
  
	
    }
    
    private void setColorForNewSIM(int slot) {
        //int[] colors = new int[8];
        SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(this, slot);
        int simColor = -1;
        if (simInfo!= null) {          
            ContentValues valueColor1 = new ContentValues(1);
            simColor = SimIconUtils.getNewCardSimColor(slot);
            valueColor1.put(SimInfo.COLOR, simColor);
            this.getContentResolver().update(ContentUris.withAppendedId(SimInfo.CONTENT_URI, simInfo.mSimId), 
                    valueColor1, null, null);   
            Log.d(LOG_TAG, "setColorForNewSIM SimInfo simColor is " + simColor);
        }
    }
    
    
    public void setDefaultNameForNewSIM(String strName, int mSimId){
        long nameSource = SimInfo.SIM_SOURCE;
        // the source is from default name if strName is null
        if(strName == null) {
            nameSource = SimInfo.DEFAULT_SOURCE;
        }
        
        SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(this, mSimId);
        if (simInfo!= null) {
            // ALPS00384376
            // overwrite sim display name if the name stored in db is not input by user
//            long OriNameSource = simInfo.mNameSource;
            String simDisplayName = simInfo.mDisplayName;
//            Log.d(LOG_TAG, "setDefaultNameForNewSIM SimInfo simId is " + simInfo.mSimId + " simDisplayName is " + simDisplayName + " newName is " + strName + " OriNameSource = " + OriNameSource + "NewNameSource = " + nameSource);
            if (simDisplayName == null || 
//                (OriNameSource == SimInfo.DEFAULT_SOURCE && strName != null) ) {
            		strName != null) {
//                SIMInfo.setDefaultNameEx(this,simInfo.mSimId, strName, nameSource);
                SIMInfo.setDefaultName(this,simInfo.mSimId, strName);
                broadCastSetDefaultNameDone(mSimId);
            }
        }
    }
    
	private boolean isSIMRemoved(long defSIMId,long curSIM){     
        if (defSIMId <= 0) {
            return false;
        } else if (defSIMId != curSIM) {
            return true;
        } else {
            return false;
        }
    }
    public static final String GEMINI_SIM_ID_KEY = "simId";
	  public void broadCastSetDefaultNameDone(int mSimId){
	        Intent intent = new Intent("android.intent.action.SIM_NAME_UPDATE");
	        intent.putExtra(GEMINI_SIM_ID_KEY, mSimId);        
	        ActivityManagerNative.broadcastStickyIntent(intent, READ_PHONE_STATE, UserHandle.USER_ALL);
	        Log.d(LOG_TAG,"broadCast intent ACTION_SIM_NAME_UPDATE for sim " + mSimId);
	    }
	  
   private Queue<Integer> mNewCardQueue = new LinkedList<Integer>();
   
   public Integer pollNewCardSlot() {
	   return mNewCardQueue.poll();
   }    
   
   private void notifyNewCardsAvailable(int slot) {
    	Log.d(LOG_TAG,"notifyNewCardsAvailable, slot =" + slot);
        mNewCardQueue.offer(slot);
    	if(!isNeedNotifyNewCard()) {
    		return;
    	}
        Intent setSubscriptionIntent = new Intent(Intent.ACTION_MAIN);
        setSubscriptionIntent.setClassName("com.android.phone",
                "com.android.phone.SetSubscriptionDialog");
        setSubscriptionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        setSubscriptionIntent.putExtra("NOTIFY_NEW_CARD_AVAILABLE2", true); 
        startActivity(setSubscriptionIntent);
    }
   
   private static final int SUBSCRIPTION_INDEX_INVALID = 99999;
   private boolean isPhoneInCall() {
       boolean phoneInCall = false;
       for (int i = 0; i < SubscriptionManager.NUM_SUBSCRIPTIONS; i++) {
           if (MSimTelephonyManager.getDefault().getCallState(i)
                   != TelephonyManager.CALL_STATE_IDLE) {
               phoneInCall = true;
               break;
           }
       }
       return phoneInCall;
   }
   private void updateCardState() {
	   
	 //aurora add liguangyu 201411027 for #9368 start
//	   String iccid1 = "", iccid2 = "";
//	   if(mCardSubMgr.getCardSubscriptions(0) != null) {
//		   iccid1 = mCardSubMgr.getCardSubscriptions(0).getIccId();
//	   }
//	   if(mCardSubMgr.getCardSubscriptions(1) != null) {
//		   iccid2 = mCardSubMgr.getCardSubscriptions(1).getIccId();
//	   }
//		if(iccid1.equalsIgnoreCase(iccid2) 
//				&& !TextUtils.isEmpty(iccid1) 
//				&& !TextUtils.isEmpty(iccid2)) {
//		    Log.d(LOG_TAG, "updateCardState iccid equal return" );  
//			return;
//		}	   
		//aurora add liguangyu 201411027 for #9368 end
	   
	   
	   CardSubscriptionManager mCardSubscriptionManager = CardSubscriptionManager.getInstance();
	   SubscriptionData mCurrentSelSub = new SubscriptionData(SubscriptionManager.NUM_SUBSCRIPTIONS);
	   SubscriptionData mUserSelSub = new SubscriptionData(SubscriptionManager.NUM_SUBSCRIPTIONS);;
	   SubscriptionData[] mCardSubscrInfo = new SubscriptionData[SubscriptionManager.NUM_SUBSCRIPTIONS];
	   boolean[] mIsSimEnable = new boolean[SubscriptionManager.NUM_SUBSCRIPTIONS];
       for (int j = 0; j < SubscriptionManager.NUM_SUBSCRIPTIONS; j++) {
           mCardSubscrInfo[j] = mCardSubscriptionManager.getCardSubscriptions(j);
       }
       for (int i = 0; i < SubscriptionManager.NUM_SUBSCRIPTIONS; i++) {
           Subscription sub = mSubscriptionManager.getCurrentSubscription(i);
                   mCurrentSelSub.subscription[i].copyFrom(sub);
       }

       if (mCurrentSelSub != null) {
           for (int i = 0; i < SubscriptionManager.NUM_SUBSCRIPTIONS; i++) {
               if (mCurrentSelSub.subscription[i].subStatus ==
                       Subscription.SubscriptionStatus.SUB_ACTIVATED) {
                    mIsSimEnable[i] = true;                    
               } else {
               	    mIsSimEnable[i] = false;    
               }
           }
           mUserSelSub.copyFrom(mCurrentSelSub);
       }
       Log.d(LOG_TAG, "updateCardState sim1 = " + mIsSimEnable[0] + " sim2 = " + mIsSimEnable[1]);   

        boolean simOldstate[] = new boolean[SubscriptionManager.NUM_SUBSCRIPTIONS];  
        simOldstate[0] = true;
        simOldstate[1] = true;
        SharedPreferences mSharedPreferences = getSharedPreferences("sim_enable_state", Context.MODE_PRIVATE);
        SIMInfo sim1 = SIMInfo.getSIMInfoBySlot(this, 0);
        if(sim1 != null) {
        	simOldstate[0] = mSharedPreferences.getBoolean(String.valueOf(sim1.mSimId), true);
        } 
        SIMInfo sim2 = SIMInfo.getSIMInfoBySlot(this, 1);
        if(sim2 != null) {
        	simOldstate[1] = mSharedPreferences.getBoolean(String.valueOf(sim2.mSimId), true);
        } 
        
        if(mIsSimEnable[0] == simOldstate[0] && mIsSimEnable[1] == simOldstate[1]) {
        	return;
        }
        
        Log.d(LOG_TAG, "updateCardState restore sim1 = " + simOldstate[0] + " sim2 = " + simOldstate[1]);
        
        int deactRequiredCount = 0;

        if (isPhoneInCall()) {
//            displayErrorDialog(R.string.set_subimport src.com.android.contacts.GNContactsUtils;_not_supported_phone_in_call);
        } else {
            for (int i = 0; i < SubscriptionManager.NUM_SUBSCRIPTIONS; i++) {
                if (simOldstate[i] == false) {
                    if (mCurrentSelSub.subscription[i].subStatus ==
                            Subscription.SubscriptionStatus.SUB_ACTIVATED) {
                        Log.d(LOG_TAG, "setSubscription: Sub " + i + " not selected. Setting 99999");
                        mUserSelSub.subscription[i].slotId = SUBSCRIPTION_INDEX_INVALID;
                        mUserSelSub.subscription[i].m3gppIndex = SUBSCRIPTION_INDEX_INVALID;
                        mUserSelSub.subscription[i].m3gpp2Index = SUBSCRIPTION_INDEX_INVALID;
                        mUserSelSub.subscription[i].subId = i;
                        mUserSelSub.subscription[i].subStatus = Subscription.
                                SubscriptionStatus.SUB_DEACTIVATE;

                        deactRequiredCount++;
                    }
                } else {
                    int slotId = i;
                    int subIndex = 0;

                    if (mCardSubscrInfo[slotId] == null) {
                        Log.d(LOG_TAG, "setSubscription: mCardSubscrInfo is not in sync with SubscriptionManager");
                        mUserSelSub.subscription[i].slotId = SUBSCRIPTION_INDEX_INVALID;
                        mUserSelSub.subscription[i].m3gppIndex = SUBSCRIPTION_INDEX_INVALID;
                        mUserSelSub.subscription[i].m3gpp2Index = SUBSCRIPTION_INDEX_INVALID;
                        mUserSelSub.subscription[i].subId = i;
                        mUserSelSub.subscription[i].subStatus = Subscription.
                                SubscriptionStatus.SUB_DEACTIVATE;

                        if (mCurrentSelSub.subscription[i].subStatus ==
                                Subscription.SubscriptionStatus.SUB_ACTIVATED) {
                            deactRequiredCount++;
                        }
                        continue;
                    }


                    // Compate the user selected subscriptio with the current subscriptions.
                    // If they are not matching, mark it to activate.
                    mUserSelSub.subscription[i].copyFrom(mCardSubscrInfo[slotId].
                            subscription[subIndex]);
                    mUserSelSub.subscription[i].subId = i;
                    if (mCurrentSelSub != null) {
                        // subStatus used to store the activation status as the mCardSubscrInfo
                        // is not keeping track of the activation status.
                        Subscription.SubscriptionStatus subStatus =
                                mCurrentSelSub.subscription[i].subStatus;
                        mUserSelSub.subscription[i].subStatus = subStatus;
                        if ((subStatus != Subscription.SubscriptionStatus.SUB_ACTIVATED) ||
                            (!mUserSelSub.subscription[i].equals(mCurrentSelSub.subscription[i]))) {
                            // User selected a new subscription.  Need to activate this.
                            mUserSelSub.subscription[i].subStatus = Subscription.
                            SubscriptionStatus.SUB_ACTIVATE;
                        }

                        if (mCurrentSelSub.subscription[i].subStatus == Subscription.
                                 SubscriptionStatus.SUB_ACTIVATED
                                 && mUserSelSub.subscription[i].subStatus == Subscription.
                                 SubscriptionStatus.SUB_ACTIVATE) {
                            deactRequiredCount++;
                        }
                    } else {
                        mUserSelSub.subscription[i].subStatus = Subscription.
                                SubscriptionStatus.SUB_ACTIVATE;
                    }
                }
            }
            mSubscriptionManager.setSubscription(mUserSelSub);
        }
    
   }
   
   
   private boolean mIsNewCardPending = false;
   private void setNewCardSubscription() {
	   CardSubscriptionManager mCardSubscriptionManager = CardSubscriptionManager.getInstance();
	   SubscriptionData mCurrentSelSub = new SubscriptionData(SubscriptionManager.NUM_SUBSCRIPTIONS);
	   SubscriptionData mUserSelSub = new SubscriptionData(SubscriptionManager.NUM_SUBSCRIPTIONS);;
	   SubscriptionData[] mCardSubscrInfo = new SubscriptionData[SubscriptionManager.NUM_SUBSCRIPTIONS];
	   boolean[] mIsSimEnable = new boolean[SubscriptionManager.NUM_SUBSCRIPTIONS];
       for (int j = 0; j < SubscriptionManager.NUM_SUBSCRIPTIONS; j++) {
           mCardSubscrInfo[j] = mCardSubscriptionManager.getCardSubscriptions(j);
       }
       for (int i = 0; i < SubscriptionManager.NUM_SUBSCRIPTIONS; i++) {
           Subscription sub = mSubscriptionManager.getCurrentSubscription(i);
                   mCurrentSelSub.subscription[i].copyFrom(sub);
       }

       if (mCurrentSelSub != null) {
           for (int i = 0; i < SubscriptionManager.NUM_SUBSCRIPTIONS; i++) {
               if (mCurrentSelSub.subscription[i].subStatus ==
                       Subscription.SubscriptionStatus.SUB_ACTIVATED) {
                    mIsSimEnable[i] = true;                    
               } else {
               	    mIsSimEnable[i] = false;    
               }
           }
           mUserSelSub.copyFrom(mCurrentSelSub);
       }
       Log.d(LOG_TAG, "setNewCardSubscription sim1 = " + mIsSimEnable[0] + " sim2 = " + mIsSimEnable[1]); 
        
        boolean simstate[] = {mIsSimEnable[0], mIsSimEnable[1]};
        Integer slot = pollNewCardSlot();
        if(slot == null) {
 	        return;
        } else {
        	if(simstate[slot]) {
        		return;
        	} else {
        		simstate[slot] = true;
        	}
        }
        
        Log.d(LOG_TAG, "setNewCardSubscription sim1 = " + simstate[0] + " sim2 = " + simstate[1]);
        
        int deactRequiredCount = 0;
//        subErr = false;

        if (isPhoneInCall()) {
//            displayErrorDialog(R.string.set_sub_not_supported_phone_in_call);
        } else {
            for (int i = 0; i < SubscriptionManager.NUM_SUBSCRIPTIONS; i++) {
                if (simstate[i] == false) {
                    if (mCurrentSelSub.subscription[i].subStatus ==
                            Subscription.SubscriptionStatus.SUB_ACTIVATED) {
                        Log.d(LOG_TAG, "setNewCardSubscription: Sub " + i + " not selected. Setting 99999");
                        mUserSelSub.subscription[i].slotId = SUBSCRIPTION_INDEX_INVALID;
                        mUserSelSub.subscription[i].m3gppIndex = SUBSCRIPTION_INDEX_INVALID;
                        mUserSelSub.subscription[i].m3gpp2Index = SUBSCRIPTION_INDEX_INVALID;
                        mUserSelSub.subscription[i].subId = i;
                        mUserSelSub.subscription[i].subStatus = Subscription.
                                SubscriptionStatus.SUB_DEACTIVATE;

                        deactRequiredCount++;
                    }
                } else {
                    int slotId = i;
                    int subIndex = 0;

                    if (mCardSubscrInfo[slotId] == null) {
                        Log.d(LOG_TAG, "setNewCardSubscription: mCardSubscrInfo is not in sync with SubscriptionManager");
                        mUserSelSub.subscription[i].slotId = SUBSCRIPTION_INDEX_INVALID;
                        mUserSelSub.subscription[i].m3gppIndex = SUBSCRIPTION_INDEX_INVALID;
                        mUserSelSub.subscription[i].m3gpp2Index = SUBSCRIPTION_INDEX_INVALID;
                        mUserSelSub.subscription[i].subId = i;
                        mUserSelSub.subscription[i].subStatus = Subscription.
                                SubscriptionStatus.SUB_DEACTIVATE;

                        if (mCurrentSelSub.subscription[i].subStatus ==
                                Subscription.SubscriptionStatus.SUB_ACTIVATED) {
                            deactRequiredCount++;
                        }
                        continue;
                    }


                    // Compate the user selected subscriptio with the current subscriptions.
                    // If they are not matching, mark it to activate.
                    mUserSelSub.subscription[i].copyFrom(mCardSubscrInfo[slotId].
                            subscription[subIndex]);
                    mUserSelSub.subscription[i].subId = i;
                    if (mCurrentSelSub != null) {
                        // subStatus used to store the activation status as the mCardSubscrInfo
                        // is not keeping track of the activation status.
                        Subscription.SubscriptionStatus subStatus =
                                mCurrentSelSub.subscription[i].subStatus;
                        mUserSelSub.subscription[i].subStatus = subStatus;
                        if ((subStatus != Subscription.SubscriptionStatus.SUB_ACTIVATED) ||
                            (!mUserSelSub.subscription[i].equals(mCurrentSelSub.subscription[i]))) {
                            // User selected a new subscription.  Need to activate this.
                            mUserSelSub.subscription[i].subStatus = Subscription.
                            SubscriptionStatus.SUB_ACTIVATE;
                        }

                        if (mCurrentSelSub.subscription[i].subStatus == Subscription.
                                 SubscriptionStatus.SUB_ACTIVATED
                                 && mUserSelSub.subscription[i].subStatus == Subscription.
                                 SubscriptionStatus.SUB_ACTIVATE) {
                            deactRequiredCount++;
                        }
                    } else {
                        mUserSelSub.subscription[i].subStatus = Subscription.
                                SubscriptionStatus.SUB_ACTIVATE;
                    }
                }
            }
            mSubscriptionManager.setSubscription(mUserSelSub);
        }
    }
   
   //aurora add liguangyu 20140618 for BUG #5897 start
   void handleNetMode(Phone phone) {
       MSimTelephonyManager telephonyManager = MSimTelephonyManager.getDefault();
	   ServiceState ss = getPhone(0).getServiceState();
	   try {
	       	int sub = phone.getSubscription();
	       	if(sub == 0) {
			      handleIfSimCardChanged();
	       	}
       } catch (Exception e) {
       	e.printStackTrace();
       }
   }
   
   private void handleIfSimCardChanged() {
       try {
       	    MSimTelephonyManager telephonyManager = MSimTelephonyManager.getDefault();
	            if(telephonyManager.getSimState(0) == TelephonyManager.SIM_STATE_READY) {
		            SharedPreferences sp = getSharedPreferences("com.android.phone_preferences", Context.MODE_PRIVATE);  
		            String preIccid = sp.getString("aurora_iccid", "");	            
		       	    String iccid = telephonyManager.getSimSerialNumber(0);
	                Log.d(LOG_TAG, "- handleIfSimCardChanged preIccid = " + preIccid + " iccid = " + iccid);
		       	    if(!TextUtils.isEmpty(iccid) && !preIccid.equalsIgnoreCase(iccid)) {
			       	    SharedPreferences.Editor editor = sp.edit();
			            editor.putString("aurora_iccid", iccid);
			            editor.commit();
           	        MSimTelephonyManager.putIntAtIndex(
                               getContentResolver(),
                               android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
                               0, Constants.NETWORK_MODE_TD_SCDMA_GSM_WCDMA_LTE);
           	        getPhone(0).setPreferredNetworkType(Constants.NETWORK_MODE_TD_SCDMA_GSM_WCDMA_LTE, null);
		       	    }
	            }	        
	       	    
       } catch (Exception e) {
       	e.printStackTrace();
       }
   }
   //aurora add liguangyu 20140618 for BUG #5897 end
   
	private void onSubscriptionActivated(AsyncResult ar) {
		if (ar.exception != null) {
			return;
		}

		Integer mSimId = (Integer) ar.userObj;
		// aurora add zhouxiaobing 20140605 for sim2 can't internet;
		simcardstate |= (mSimId + 1);
		if (!DeviceUtils.isSupportDualData()) {
			if (simcardstate == 1) {
				mCardHandler.postDelayed(runnable_apn1, 3000);
			} else if (simcardstate == 2) {
				mCardHandler.postDelayed(runnable_apn2, 3000);
			} else if (simcardstate == 3) {
				mCardHandler.postDelayed(runnable_apn3, 3000);
			}
		//aurora add liguangyu 201411110 for #7917 start
		}
		if (simcardstate == 3) {
	        SharedPreferences sp = getSharedPreferences("com.android.phone_preferences", Context.MODE_PRIVATE);  
	        Boolean init = sp.getBoolean("aurora_slot_init", false);	            
	   	    if(!init) { 
	            try {
	           	    SharedPreferences.Editor editor = sp.edit();
	                editor.putBoolean("aurora_slot_init", true);
	                editor.commit();		            		                
	                phone.setPrioritySub(0, null);                
	                phone.setDefaultVoiceSub(0, null);
	                MSimPhoneFactory.setSMSSubscription(0);
		            MSimPhoneFactory.setVoiceSubscription(0);
	                MSimPhoneFactory.setPrioritySubscription(0);  
//	                MSimPhoneFactory.setDataSubscription(0);  
//	                MSimPhoneFactory.setDefaultDataSubscription(0);  
	                SubscriptionManager.getInstance().setDataSubscription(0, null);
	                getPhone(1).setPreferredNetworkType(Constants.NETWORK_MODE_GSM_ONLY, null);
	                getPhone(0).setPreferredNetworkType(Constants.NETWORK_MODE_TD_SCDMA_GSM_WCDMA_LTE, null);
	            } catch (Exception e) {
	            	e.printStackTrace();
	            }                   
	   	    }  
	   	    if(DeviceUtils.isSupportDualData()) {
	            int data = sp.getInt("aurora_data_slot", MSimPhoneFactory.getDataSubscription());
	            if(data != MSimPhoneFactory.getDataSubscription()) {
	                isUserSwitchData = true;
	            	SubscriptionManager.getInstance().setDataSubscription(data, null);
	            }
	   	    }
		}
	   	//aurora add liguangyu 201411110 for #7917 end
		// aurora add zhouxiaobing 20140605 for sim2 can't internet;
	}

	private void onSubscriptionDeactivated(AsyncResult ar) {
		if (ar.exception != null) {
			return;
		}

		Integer mSimId = (Integer) ar.userObj;
		// aurora add zhouxiaobing 20140605 for sim2 can't internet;
		simcardstate &= (~(mSimId + 1));
		if (!DeviceUtils.isSupportDualData()) {
			if (simcardstate == 1) {
				mCardHandler.postDelayed(runnable_apn1, 3000);
			} else if (simcardstate == 2) {
				mCardHandler.postDelayed(runnable_apn2, 3000);
			} else if (simcardstate == 3) {
				mCardHandler.postDelayed(runnable_apn3, 3000);
			}
		}
		// aurora add zhouxiaobing 20140605 for sim2 can't internet;
	}
	
    protected void initAuroraObjects() {
        mSubscriptionManager = SubscriptionManager.getInstance();        
        mCardSubMgr = CardSubscriptionManager.getInstance();
        int mNumPhones = MSimTelephonyManager.getDefault().getPhoneCount();
        for (int i=0; i < mNumPhones; i++) {
            mCardSubMgr.registerForCardInfoAvailable(i, mCardHandler, EVENT_CARD_INFO_AVAILABLE, new Integer(i));
            mCardSubMgr.registerForCardInfoUnavailable(i, mCardHandler, EVENT_CARD_INFO_NOT_AVAILABLE, new Integer(i));
            mSubscriptionManager.registerForSubscriptionActivated(i, mCardHandler, EVENT_SUBSCRIPTION_ACTIVATED, new Integer(i));
            mSubscriptionManager.registerForSubscriptionDeactivated(i, mCardHandler, EVENT_SUBSCRIPTION_DEACTIVATED, new Integer(i));
        }
//        mSubscriptionManager.getInstance().registerForDdsSwitch(mCardHandler, EVENT_DDS_SWITCH, null);
        
        
        SharedPreferences sp = getSharedPreferences("com.android.phone_preferences", Context.MODE_PRIVATE);  
        Boolean init = sp.getBoolean("aurora_phone_init", false);	            
   	    if(!init) { 
            try {
            	Settings.Global.putInt(getContentResolver(),
                        Settings.Global.MULTI_SIM_DATA_CALL_SUBSCRIPTION, 0);
                android.provider.Settings.Global.putInt(getContentResolver(),
                        android.provider.Settings.Global.MOBILE_DATA + "0", 0);
                android.provider.Settings.Global.putInt(getContentResolver(),
                        android.provider.Settings.Global.DATA_ROAMING + "0", 0);
                android.provider.Settings.Global.putInt(getContentResolver(),
                        android.provider.Settings.Global.MOBILE_DATA + "1", 0);
                android.provider.Settings.Global.putInt(getContentResolver(),
                        android.provider.Settings.Global.DATA_ROAMING + "1", 0);
	            android.telephony.MSimTelephonyManager.putIntAtIndex(
	                    getContentResolver(),
	                    android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
	                    1, Constants.NETWORK_MODE_GSM_ONLY);
	            getPhone(1).setPreferredNetworkType(Constants.NETWORK_MODE_GSM_ONLY, null);
    	        MSimTelephonyManager.putIntAtIndex(
                        getContentResolver(),
                        android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
                        0, Constants.NETWORK_MODE_TD_SCDMA_GSM_WCDMA_LTE);
                getPhone(0).setPreferredNetworkType(Constants.NETWORK_MODE_TD_SCDMA_GSM_WCDMA_LTE, null);
                
                phone.setPrioritySub(0, null);                
                phone.setDefaultVoiceSub(0, null);
                MSimPhoneFactory.setSMSSubscription(0);
                MSimPhoneFactory.setPromptEnabled(false);
	            MSimPhoneFactory.setVoiceSubscription(0);
                MSimPhoneFactory.setPrioritySubscription(0);  
//                MSimPhoneFactory.setDataSubscription(0);  
//                MSimPhoneFactory.setDefaultDataSubscription(0);
            } catch (Exception e) {
            	e.printStackTrace();
            }                   
   	    }   	           	       
   	    
		if (DeviceUtils.isSupportDualData()) {
			mLastDataSub = MSimPhoneFactory.getDataSubscription();
	   	    mDataSubObserver = new DataSubObserver();
	        getContentResolver().registerContentObserver(Settings.Global.CONTENT_URI, true, mDataSubObserver);
		}

        super.initAuroraObjects();
    }
    
    private long getSIMInfoIdByICCIdAndNumber(String iccid , int slot) {
        if (iccid == null) return -1;
        Cursor cursor = this.getContentResolver().query(SimInfo.CONTENT_URI, 
                null, SimInfo.ICC_ID + "=? and " + SimInfo.NUMBER + "=? ", new String[]{iccid, slot + ""}, null);
        try {
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    return cursor.getLong(cursor.getColumnIndexOrThrow(SimInfo._ID));
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return -1;
    }
    
    private Uri insertICCIdAndNumber(String ICCId, int slot) {
        Uri uri;
        ContentResolver resolver = this.getContentResolver();
        String selection = SimInfo.ICC_ID + "=? and " + SimInfo.NUMBER + "=? ";
        Cursor cursor = resolver.query(SimInfo.CONTENT_URI, new String[]{SimInfo._ID, SimInfo.SLOT}, selection, new String[]{ICCId, slot + ""}, null);
        try {
            if (cursor == null || !cursor.moveToFirst()) {
                ContentValues values = new ContentValues();
                values.put(SimInfo.ICC_ID, ICCId);
                values.put(SimInfo.COLOR, -1);
                values.put(SimInfo.SLOT, slot);
                values.put(SimInfo.NUMBER,  slot + "");
                uri = resolver.insert(SimInfo.CONTENT_URI, values);
            } else {
                long simId = cursor.getLong(0);
                int oldSlot = cursor.getInt(1);
                uri = ContentUris.withAppendedId(SimInfo.CONTENT_URI, simId);
                if (slot != oldSlot) {
                    ContentValues values = new ContentValues(1);
                    values.put(SimInfo.SLOT, slot);
                    resolver.update(uri, values, null, null);
                } 
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return uri;
    }
    
    private int mLastDataSub = 0;
    
    private void handleDdsSwitchStep1() {
    	int currentSub = MSimPhoneFactory.getDataSubscription();
    	int sub = mLastDataSub;
        if(sub != currentSub) {
        	mLastDataSub = currentSub;
        } else {
        	return;
        }
        Log.d(LOG_TAG,"handleDdsSwitchStep1: sub = " + sub);
		if(!isUserSwitchData && simcardstate == 3) {
			return;
		}
                 
        MSimTelephonyManager.putIntAtIndex(
                getContentResolver(),
                android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
                sub, Constants.NETWORK_MODE_GSM_ONLY);
        getPhone(sub).setPreferredNetworkType(Constants.NETWORK_MODE_GSM_ONLY, mCardHandler.obtainMessage(MESSAGE_SET_PREFERRED_NETWORK_TYPE, sub, -1));
        
    }            
        
       
    private void handleDdsSwitchStep2(Message msg) {       
        AsyncResult ar = (AsyncResult) msg.obj;
        final int sub = msg.arg1;
        final int otherSub = sub > 0 ? 0 : 1;
        SharedPreferences mSharedPreferences = getSharedPreferences("last_nettype", Context.MODE_PRIVATE);
        int lastNetType = mSharedPreferences.getInt("nettype" + otherSub, Constants.NETWORK_MODE_TD_SCDMA_GSM_WCDMA_LTE);
        if (ar.exception == null) {
            Log.d(LOG_TAG,"handleDdsSwitchStep2 lastNetType = " + lastNetType);         
            MSimTelephonyManager.putIntAtIndex(
                    getContentResolver(),
                    android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
                    otherSub, lastNetType);
            getPhone(otherSub).setPreferredNetworkType(lastNetType, null);
        }    
        
        mCardHandler.postDelayed(new Runnable() {
        	public void run(){
                boolean isUserDataEnabledSlot = android.provider.Settings.Global.getInt(getContentResolver(), android.provider.Settings.Global.MOBILE_DATA + 0 , 0) == 1;
                if(isUserDataEnabledSlot) {
                    Log.d(LOG_TAG, "handleDdsSwitchStep2 getMobileDataEnabled");
    	 		    ConnectivityManager cm =
    	 		            (ConnectivityManager)PhoneGlobals.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
    	 		    cm.setMobileDataEnabled(true);
                }        		
        	}
        }, 3000);
        isUserSwitchData = false;
    }
    
    boolean isUserSwitchData = false;       
    private ContentObserver mDataSubObserver;
    public class DataSubObserver extends ContentObserver {
		public final String TAG = "DataSubObserver";

		public DataSubObserver() {
			super(new Handler());
		}

		@Override
		public void onChange(boolean selfChange) {
			Log.i(TAG, "onChange :");
			super.onChange(selfChange);
			
		    handleDdsSwitchStep1();
			
		}
    }
    
    private boolean isNeedNotifyNewCard() {
     	  boolean isStarted = true;
     	  try {
     		 ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
     		 ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
     	     Log.v(LOG_TAG, "topActiviy = " +  cn.getClassName());
     	     if(cn.getClassName().equalsIgnoreCase("com.android.phone.AuroraMultiSimSettings")) {
     	    	 isStarted = false;
     	     }
     	  } catch (Exception e) {
     		  e.printStackTrace();
     	  }
           Log.v(LOG_TAG, "isNeedNotifyNewCard = " + isStarted);
     	  return isStarted;
       }    
    

}
