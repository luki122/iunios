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

package com.android.phone;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.CallerInfo;
import com.android.internal.telephony.CallerInfoAsyncQuery;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneBase;
import com.android.internal.telephony.TelephonyCapabilities;
import com.android.internal.telephony.cdma.CdmaCallWaitingNotification;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaDisplayInfoRec;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaSignalInfoRec;
import com.android.internal.telephony.cdma.SignalToneUtil;
import com.android.internal.telephony.gemini.MTKCallManager;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDiskIOException;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.SystemClock;
import android.os.SystemVibrator;
import android.os.Vibrator;
import android.provider.CallLog.Calls;
import android.provider.Telephony.SIMInfo;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Log;
import android.widget.Toast;
import android.net.Uri;
import android.net.sip.SipManager;
import android.telephony.ServiceState;
import com.android.internal.telephony.sip.SipPhone;

import com.android.internal.telephony.gemini.*;
import com.android.internal.telephony.gsm.SuppCrssNotification;
import com.android.phone.Constants.CallStatusCode;
import com.mediatek.phone.PhoneFeatureConstants.FeatureOption;
import com.mediatek.phone.DualTalkUtils;

import com.mediatek.telephony.TelephonyManagerEx;

import com.mediatek.phone.SIMInfoWrapper;
import com.android.internal.telephony.PhoneConstants;
//DMLock
//import com.mediatek.audioprofile.AudioProfile;
//import com.mediatek.audioprofile.AudioProfileImpl;
//import com.mediatek.audioprofile.AudioProfileManagerImpl;

/**
 * Phone app module that listens for phone state changes and various other
 * events from the telephony layer, and triggers any resulting UI behavior
 * (like starting the Ringer and Incoming Call UI, playing in-call tones,
 * updating notifications, writing call log entries, etc.)
 */
public class MtkCallNotifier extends CallNotifier{
    private static final String LOG_TAG = "MtkCallNotifier";
    private static final boolean DBG =
//            (PhoneGlobals.DBG_LEVEL >= 1) && (SystemProperties.getInt("ro.debuggable", 0) == 1);
    		(PhoneGlobals.DBG_LEVEL >= 1);
    private static final boolean VDBG = (PhoneGlobals.DBG_LEVEL >= 2);
    
    
    private static final int PHONE_DISCONNECT_SIM1 = 31;
    private static final int PHONE_DISCONNECT_SIM2 = 32;
    /* state for sim 2*/
    private static final int PHONE_NEW_RINGING_CONNECTION2 = 33;
    private static final int PHONE_STATE_CHANGED2 = 34;
    private static final int PHONE_DISCONNECT2 = 35;
    private static final int PHONE_UNKNOWN_CONNECTION_APPEARED2 = 36;
    private static final int PHONE_INCOMING_RING2 = 37 ;
    private static final int PHONE_RINGBACK_TONE2 = 38;
    private static final int PHONE_RESEND_MUTE2 = 39;

    
    private static final int DISPLAY_BUSY_MESSAGE = 50;

    private static final int PHONE_MWI_CHANGED2 = 40;
    
    private MTKCallManager mCMGemini;
    private BluetoothHandsfree mBluetoothHandsfree;
    private PhoneConstants.State mLastState = PhoneConstants.State.IDLE;
    private boolean ok2Ring = true;
    
    public static boolean mIsWaitingQueryComplete = true;
    //Last cfi information
    boolean []cfiStatus = {false, false};
    
    DualTalkUtils mDualTalk;
    
    InCallTonePlayer mToneThread = null;
    
    /**
     * Initialize the singleton CallNotifier instance.
     * This is only done once, at startup, from PhoneApp.onCreate().
     */
    /* package */ static CallNotifier init(PhoneGlobals app, Phone phone, Ringer ringer, BluetoothHandsfree btMgr,CallLogger callLogger) {
        synchronized (MtkCallNotifier.class) {
            if (sInstance == null) {
                sInstance = new MtkCallNotifier(app, phone, ringer, btMgr, callLogger);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            return sInstance;
        }
    }
    

    /** Private constructor; @see init() */
    private MtkCallNotifier(PhoneGlobals app, Phone phone, Ringer ringer,
                         BluetoothHandsfree btMgr, CallLogger callLogger) {
    	super(app, phone, ringer, callLogger);    

        if (DualTalkUtils.isSupportDualTalk()) {
            mDualTalk = DualTalkUtils.getInstance();
        }

        mBluetoothHandsfree = btMgr;
    }
    
    @Override
    protected void listen() {
    	  if (FeatureOption.MTK_GEMINI_SUPPORT) {
              TelephonyManagerEx telephonyManagerEx = new TelephonyManagerEx(mApplication);
              telephonyManagerEx.listen(mPhoneStateListener,
                      PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR
                              | PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR
                              | PhoneStateListener.LISTEN_SERVICE_STATE, Phone.GEMINI_SIM_1);

              telephonyManagerEx.listen(mPhoneStateListener2,
                      PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR
                              | PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR
                              | PhoneStateListener.LISTEN_SERVICE_STATE, Phone.GEMINI_SIM_2);

          } else {
              TelephonyManager telephonyManager = (TelephonyManager) mApplication
                      .getSystemService(Context.TELEPHONY_SERVICE);
              telephonyManager.listen(mPhoneStateListener,
                      PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR
                              | PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR
                              | PhoneStateListener.LISTEN_SERVICE_STATE);
          }
    }
    
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case PHONE_NEW_RINGING_CONNECTION:
            case PHONE_NEW_RINGING_CONNECTION2:
                if (DBG) log("RINGING... (new)");
                onNewRingingConnection((AsyncResult) msg.obj, msg.what);
                mSilentRingerRequested = false;
                break;

            case PHONE_INCOMING_RING:
            case PHONE_INCOMING_RING2:
                if (DBG) log("PHONE_INCOMING_RING ! ");
                // repeat the ring when requested by the RIL, and when the user has NOT
                // specifically requested silence.
                if (msg.obj != null && ((AsyncResult) msg.obj).result != null) {
                    PhoneBase pb =  (PhoneBase)((AsyncResult)msg.obj).result;
                    boolean bSipRing = pb instanceof SipPhone;
                    boolean bIsRejected = false;
                    Call ringCall = pb.getRingingCall();
                    if (null != ringCall) {
                        bIsRejected = PhoneUtils.getShouldSendToVoiceMailFlag(ringCall.getLatestConnection());
                    }
                    if ((pb.getState() == PhoneConstants.State.RINGING)
                            && (mSilentRingerRequested == false)
                            && (bIsRejected == false)
                            && ok2Ring) {
                        if (DBG) log("RINGING... (PHONE_INCOMING_RING event)");
                        boolean provisioned = Settings.Secure.getInt(mApplication.getContentResolver(),
                        Settings.Secure.DEVICE_PROVISIONED, 0) != 0;
                        //For sip call, the ringer will start in onCustomRingQueryComplete
                        if (provisioned && !bSipRing)
                        {
                            mRinger.ring();
                        }
                    } else {
                        if (DBG) log("RING before NEW_RING, skipping");
                    }
                }
                break;
                           
            


            case PHONE_STATE_CHANGED:
            case PHONE_STATE_CHANGED2:
                log("CallNotifier Phone state change");
                onPhoneStateChanged((AsyncResult) msg.obj);
                break;
            case PHONE_DISCONNECT_SIM1:
                if (DBG) log("DISCONNECT SIM1");
                AsyncResult resultSim1 = (AsyncResult) msg.obj;
                Connection connectionSim1 = (Connection) resultSim1.result;
                if ((!connectionSim1.isIncoming() 
                        || !PhoneUtils.getShouldSendToVoiceMailFlag(connectionSim1))
                        && ok2Ring) {
                    mApplication.wakeUpScreen();
                }
                onDisconnect((AsyncResult) msg.obj, Phone.GEMINI_SIM_1);
                break;
            case PHONE_DISCONNECT_SIM2:
                if (DBG) log("DISCONNECT SIM2");
                AsyncResult resultSim2 = (AsyncResult) msg.obj;
                Connection connectionSim2 = (Connection) resultSim2.result;
                if ((!connectionSim2.isIncoming() ||
                        !PhoneUtils.getShouldSendToVoiceMailFlag(connectionSim2))
                        && ok2Ring) {
                    mApplication.wakeUpScreen();
                }
                onDisconnect((AsyncResult) msg.obj, Phone.GEMINI_SIM_2);
                break;
            case PHONE_DISCONNECT:
                if (DBG) log("DISCONNECT");
                AsyncResult r = (AsyncResult) msg.obj;
                Connection connection = (Connection) r.result;
                if ((!connection.isIncoming() ||
                        !PhoneUtils.getShouldSendToVoiceMailFlag(connection))
                        && ok2Ring) {
                    mApplication.wakeUpScreen();
                }
                onDisconnect((AsyncResult) msg.obj, -1);
                break;

            case PHONE_UNKNOWN_CONNECTION_APPEARED:
            case PHONE_UNKNOWN_CONNECTION_APPEARED2:
                onUnknownConnectionAppeared((AsyncResult) msg.obj);
                break;

            case RINGER_CUSTOM_RINGTONE_QUERY_TIMEOUT:
                onCustomRingtoneQueryTimeout((String) msg.obj);
                break;

            case PHONE_MWI_CHANGED:
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    onMwiChanged(((GeminiPhone)(mApplication.phone)).getMessageWaitingIndicatorGemini(Phone.GEMINI_SIM_1), Phone.GEMINI_SIM_1);
                } else {
                    onMwiChanged(mApplication.phone.getMessageWaitingIndicator(), Phone.GEMINI_SIM_1);
                }
                break;

            case PHONE_MWI_CHANGED2:
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    onMwiChanged(((GeminiPhone)(mApplication.phone)).getMessageWaitingIndicatorGemini(Phone.GEMINI_SIM_2), Phone.GEMINI_SIM_2);
                } else {
                    onMwiChanged(mApplication.phone.getMessageWaitingIndicator(), Phone.GEMINI_SIM_2);
                }
                break;

            case PHONE_CDMA_CALL_WAITING:
                if (DBG) log("Received PHONE_CDMA_CALL_WAITING event");
                onCdmaCallWaiting((AsyncResult) msg.obj);
                break;

            case CDMA_CALL_WAITING_REJECT:
                Log.i(LOG_TAG, "Received CDMA_CALL_WAITING_REJECT event");
                onCdmaCallWaitingReject();
                break;

            case CALLWAITING_CALLERINFO_DISPLAY_DONE:
                Log.i(LOG_TAG, "Received CALLWAITING_CALLERINFO_DISPLAY_DONE event");
                mCallWaitingTimeOut = true;
                onCdmaCallWaitingReject();
                break;

            case CALLWAITING_ADDCALL_DISABLE_TIMEOUT:
                if (DBG) log("Received CALLWAITING_ADDCALL_DISABLE_TIMEOUT event ...");
                // Set the mAddCallMenuStateAfterCW state to true
                mApplication.cdmaPhoneCallState.setAddCallMenuStateAfterCallWaiting(true);
                mApplication.updateInCallScreen();
                break;

            case PHONE_STATE_DISPLAYINFO:
                if (DBG) log("Received PHONE_STATE_DISPLAYINFO event");
                onDisplayInfo((AsyncResult) msg.obj);
                break;

            case PHONE_STATE_SIGNALINFO:
                if (DBG) log("Received PHONE_STATE_SIGNALINFO event");
                onSignalInfo((AsyncResult) msg.obj);
                break;

            case DISPLAYINFO_NOTIFICATION_DONE:
                if (DBG) log("Received Display Info notification done event ...");
                CdmaDisplayInfo.dismissDisplayInfoRecord();
                break;

            case EVENT_OTA_PROVISION_CHANGE:
                if (DBG) log("EVENT_OTA_PROVISION_CHANGE...");
                mApplication.handleOtaspEvent(msg);
                break;

            case PHONE_ENHANCED_VP_ON:
                if (DBG) log("PHONE_ENHANCED_VP_ON...");
                if (!mVoicePrivacyState) {
                    int toneToPlay = InCallTonePlayer.TONE_VOICE_PRIVACY;
                    new InCallTonePlayer(toneToPlay).start();
                    mVoicePrivacyState = true;
                    // Update the VP icon:
                    if (DBG) log("- updating notification for VP state...");
                    mApplication.notificationMgr.updateInCallNotification();
                }
                break;

            case PHONE_ENHANCED_VP_OFF:
                if (DBG) log("PHONE_ENHANCED_VP_OFF...");
                if (mVoicePrivacyState) {
                    int toneToPlay = InCallTonePlayer.TONE_VOICE_PRIVACY;
                    new InCallTonePlayer(toneToPlay).start();
                    mVoicePrivacyState = false;
                    // Update the VP icon:
                    if (DBG) log("- updating notification for VP state...");
                    mApplication.notificationMgr.updateInCallNotification();
                }
                break;

            case PHONE_RINGBACK_TONE:
            case PHONE_RINGBACK_TONE2:
                if (DBG) log("- receive the ring back...");
                onRingbackTone((AsyncResult) msg.obj);
                break;

            case PHONE_RESEND_MUTE:
            case PHONE_RESEND_MUTE2:
                onResendMute();
                break;

            case DISPLAY_BUSY_MESSAGE:
                //This is request by brazil vivo
                if (FeatureOption.MTK_BRAZIL_CUSTOMIZATION_VIVO) {
                    Toast.makeText(PhoneGlobals.getInstance().getApplicationContext(),
                            R.string.callFailed_userBusy,
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case UPDATE_IN_CALL_NOTIFICATION:
                mApplication.notificationMgr.updateInCallNotification();
                break;
            case ENABLE_TOUCH_DELAY:
            	isRingingTouchOn = true;            	
            	break;
                
            default:
                 super.handleMessage(msg);
        }
    }
    
    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        boolean inAirplaneMode = true;
        @Override
        public void onMessageWaitingIndicatorChanged(boolean mwi) {
            onMwiChanged(mwi, Phone.GEMINI_SIM_1);
        }
        @Override
        public void onCallForwardingIndicatorChanged(boolean cfi) {
            cfiStatus[Phone.GEMINI_SIM_1] = cfi;
            if (!inAirplaneMode)
            {
                onCfiChanged(cfi, Phone.GEMINI_SIM_1);
            }
        }

        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            Log.i(LOG_TAG, "PhoneStateListener.onServiceStateChanged: serviceState=" + serviceState);
            //final boolean inAirplaneMode;
            inAirplaneMode = serviceState.getState() == ServiceState.STATE_POWER_OFF;
            if (inAirplaneMode) {
                onCfiChanged(false , Phone.GEMINI_SIM_1);
            } else {
                boolean isSimInserted = PhoneGlobals.getInstance().phoneMgr.isSimInsert(Phone.GEMINI_SIM_1);
                if ((cfiStatus[Phone.GEMINI_SIM_1]) && (serviceState.getState() == ServiceState.STATE_IN_SERVICE)) {
                    onCfiChanged(true , Phone.GEMINI_SIM_1);
                } else if (cfiStatus[Phone.GEMINI_SIM_1] && !isSimInserted) {
                    onCfiChanged(false , Phone.GEMINI_SIM_1);
                }
            }
        }

    };

    PhoneStateListener mPhoneStateListener2 = new PhoneStateListener() {
        
        boolean inAirplaneMode = true;
        
        @Override
        public void onMessageWaitingIndicatorChanged(boolean mwi) {
            onMwiChanged(mwi, Phone.GEMINI_SIM_2);
        }

        @Override
        public void onCallForwardingIndicatorChanged(boolean cfi) {
            cfiStatus[Phone.GEMINI_SIM_2] = cfi;
            
            if (!inAirplaneMode)
            {
                onCfiChanged(cfi, Phone.GEMINI_SIM_2);
            }
        }

        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            Log.i(LOG_TAG, "PhoneStateListener222.onServiceStateChanged: serviceState=" + serviceState);
            //final boolean inAirplaneMode;
            inAirplaneMode = serviceState.getState() == ServiceState.STATE_POWER_OFF;
            if (inAirplaneMode == true) {
                onCfiChanged(false , Phone.GEMINI_SIM_2);
            } else {
                boolean isSimInserted = PhoneGlobals.getInstance().phoneMgr.isSimInsert(Phone.GEMINI_SIM_2);
                if ((cfiStatus[Phone.GEMINI_SIM_2]) && (serviceState.getState() == ServiceState.STATE_IN_SERVICE)) {
                    onCfiChanged(true , Phone.GEMINI_SIM_2);
                } else if (cfiStatus[Phone.GEMINI_SIM_2] && !isSimInserted) {
                    onCfiChanged(false , Phone.GEMINI_SIM_2);
                }
            }
        }
    };
    
    protected void onNewRingingConnection(AsyncResult r, int msgId) {
    	super.onNewRingingConnection(r);
        Connection c = (Connection) r.result;
        log("onNewRingingConnection(): state = " + mCM.getState() + ", conn = { " + c + " }");
        Call ringing = c.getCall();
        
        if (DualTalkUtils.isSupportDualTalk()) {
            if (mDualTalk == null) {
                mDualTalk = DualTalkUtils.getInstance();
            }
            
            //Check if this ringcall is allowed            
            if (ringing != null && mDualTalk.isAllowedIncomingCall(ringing)) {
                mDualTalk.switchPhoneByNeededForRing(ringing.getPhone());
            } else {
                try {
                    ringing.hangup();
                } catch (CallStateException e) {
                    log(e.toString());
                }
                return;
            }
        }
    }
    
    
    protected void onPhoneStateChanged(AsyncResult r) {
        if (DualTalkUtils.isSupportDualTalk()) {
            if (mDualTalk == null) {
                mDualTalk = DualTalkUtils.getInstance();
            }
            mDualTalk.updateState();
        }
        PhoneConstants.State state = mCM.getState();
        
        if (state == PhoneConstants.State.RINGING) {
            //ALPS00311901: Trigger the call waiting tone after user accept one incoming call.
            if ((DualTalkUtils.isSupportDualTalk()) && (mCM.hasActiveFgCall() || mCM.hasActiveBgCall())) {
                if (mCallWaitingTonePlayer == null) {
                    mCallWaitingTonePlayer = new MtkInCallTonePlayer(InCallTonePlayer.TONE_CALL_WAITING);
                    mCallWaitingTonePlayer.start();
                    log("Start waiting tone.");
                }
            }
        }
        
        super.onPhoneStateChanged(r);
    }
    
    

       void updateCallNotifierRegistrationsAfterRadioTechnologyChange() {
           if (DBG) Log.d(LOG_TAG, "updateCallNotifierRegistrationsAfterRadioTechnologyChange...");
           // Unregister all events from the old obsolete phone
           if (FeatureOption.MTK_GEMINI_SUPPORT)        
           {
               mCMGemini.unregisterForNewRingingConnectionGemini(this, Phone.GEMINI_SIM_1);
               mCMGemini.unregisterForPreciseCallStateChangedGemini(this, Phone.GEMINI_SIM_1);
               mCMGemini.unregisterForDisconnectGemini(this, Phone.GEMINI_SIM_1);
               mCMGemini.unregisterForUnknownConnectionGemini(this, Phone.GEMINI_SIM_1);
               mCMGemini.unregisterForIncomingRingGemini(this, Phone.GEMINI_SIM_1);
               mCMGemini.unregisterForCallWaitingGemini(this, Phone.GEMINI_SIM_1);
               mCMGemini.unregisterForDisplayInfoGemini(this, Phone.GEMINI_SIM_1);
               mCMGemini.unregisterForSignalInfoGemini(this, Phone.GEMINI_SIM_1);
               mCMGemini.unregisterForCdmaOtaStatusChangeGemini(this, Phone.GEMINI_SIM_1);
               mCMGemini.unregisterForRingbackToneGemini(this, Phone.GEMINI_SIM_1);

               mCMGemini.unregisterForNewRingingConnectionGemini(this, Phone.GEMINI_SIM_2);
               mCMGemini.unregisterForPreciseCallStateChangedGemini(this, Phone.GEMINI_SIM_2);
               mCMGemini.unregisterForDisconnectGemini(this, Phone.GEMINI_SIM_2);
               mCMGemini.unregisterForUnknownConnectionGemini(this, Phone.GEMINI_SIM_2);
               mCMGemini.unregisterForIncomingRingGemini(this, Phone.GEMINI_SIM_2);
               mCMGemini.unregisterForCallWaitingGemini(this, Phone.GEMINI_SIM_2);
               mCMGemini.unregisterForDisplayInfoGemini(this, Phone.GEMINI_SIM_2);
               mCMGemini.unregisterForSignalInfoGemini(this, Phone.GEMINI_SIM_2);
               mCMGemini.unregisterForCdmaOtaStatusChangeGemini(this, Phone.GEMINI_SIM_2);
               mCMGemini.unregisterForRingbackToneGemini(this, Phone.GEMINI_SIM_2);
               
               mCM.unregisterForCdmaOtaStatusChange2(this);
               mCM.unregisterForCallWaiting2(this);
               mCM.unregisterForDisplayInfo2(this);
               mCM.unregisterForSignalInfo2(this);
               mCM.unregisterForInCallVoicePrivacyOn2(this);
               mCM.unregisterForInCallVoicePrivacyOff2(this);
           }
           else
           {
               mCM.unregisterForNewRingingConnection(this);
               mCM.unregisterForPreciseCallStateChanged(this);
               mCM.unregisterForDisconnect(this);
               mCM.unregisterForUnknownConnection(this);
               mCM.unregisterForIncomingRing(this);
               mCM.unregisterForCallWaiting(this);
               mCM.unregisterForDisplayInfo(this);
               mCM.unregisterForSignalInfo(this);
               mCM.unregisterForCdmaOtaStatusChange(this);
               mCM.unregisterForRingbackTone(this);
               mCM.unregisterForResendIncallMute(this);
           }

           // Release the ToneGenerator used for playing SignalInfo and CallWaiting
           if (mSignalInfoToneGenerator != null) {
               mSignalInfoToneGenerator.release();
           }

           // Clear ringback tone player
           mInCallRingbackTonePlayer = null;

           // Clear call waiting tone player
           mCallWaitingTonePlayer = null;

           if (FeatureOption.MTK_GEMINI_SUPPORT)        
           {
               mCMGemini.unregisterForInCallVoicePrivacyOnGemini(this, Phone.GEMINI_SIM_1);
               mCMGemini.unregisterForInCallVoicePrivacyOffGemini(this, Phone.GEMINI_SIM_1);

               mCMGemini.unregisterForInCallVoicePrivacyOnGemini(this, Phone.GEMINI_SIM_2);
               mCMGemini.unregisterForInCallVoicePrivacyOffGemini(this, Phone.GEMINI_SIM_2);
           }
           else
           {
               mCM.unregisterForInCallVoicePrivacyOn(this);
               mCM.unregisterForInCallVoicePrivacyOff(this);
           }

           // Register all events new to the new active phone
           registerForNotifications();
       }

       protected void registerForNotifications() {
		if (FeatureOption.MTK_GEMINI_SUPPORT) {
			if (mCMGemini == null) {
				mCMGemini = ((MtkPhoneGlobals) mApplication).mCMGemini;
			}
			mCMGemini.registerForNewRingingConnectionGemini(this,
					PHONE_NEW_RINGING_CONNECTION, null, Phone.GEMINI_SIM_1);
			mCMGemini.registerForPreciseCallStateChangedGemini(this,
					PHONE_STATE_CHANGED, null, Phone.GEMINI_SIM_1);
			mCMGemini
					.registerForUnknownConnectionGemini(this,
							PHONE_UNKNOWN_CONNECTION_APPEARED, null,
							Phone.GEMINI_SIM_1);
			mCMGemini.registerForIncomingRingGemini(this, PHONE_INCOMING_RING,
					null, Phone.GEMINI_SIM_1);
			mCMGemini.registerForDisconnectGemini(this, PHONE_DISCONNECT_SIM1,
					null, Phone.GEMINI_SIM_1);
			mCMGemini.registerForDisconnectGemini(this, PHONE_DISCONNECT_SIM2,
					null, Phone.GEMINI_SIM_2);
			mCMGemini.registerForNewRingingConnectionGemini(this,
					PHONE_NEW_RINGING_CONNECTION2, null, Phone.GEMINI_SIM_2);
			mCMGemini.registerForPreciseCallStateChangedGemini(this,
					PHONE_STATE_CHANGED2, null, Phone.GEMINI_SIM_2);
			mCMGemini.registerForUnknownConnectionGemini(this,
					PHONE_UNKNOWN_CONNECTION_APPEARED2, null,
					Phone.GEMINI_SIM_2);
			mCMGemini.registerForIncomingRingGemini(this, PHONE_INCOMING_RING2,
					null, Phone.GEMINI_SIM_2);

			// need compiler option for C+G project
			// we always register these message to avoid use too many feature
			// option
			mCM.registerForCdmaOtaStatusChange2(this,
					EVENT_OTA_PROVISION_CHANGE, null);
			mCM.registerForCallWaiting2(this, PHONE_CDMA_CALL_WAITING, null);
			mCM.registerForDisplayInfo2(this, PHONE_STATE_DISPLAYINFO, null);
			mCM.registerForSignalInfo2(this, PHONE_STATE_SIGNALINFO, null);
			mCM.registerForInCallVoicePrivacyOn2(this, PHONE_ENHANCED_VP_ON,
					null);
			mCM.registerForInCallVoicePrivacyOff2(this, PHONE_ENHANCED_VP_OFF,
					null);
		} else {
			mCM.registerForNewRingingConnection(this,
					PHONE_NEW_RINGING_CONNECTION, null);
			mCM.registerForPreciseCallStateChanged(this, PHONE_STATE_CHANGED,
					null);
			mCM.registerForDisconnect(this, PHONE_DISCONNECT, null);
			mCM.registerForUnknownConnection(this,
					PHONE_UNKNOWN_CONNECTION_APPEARED, null);
			mCM.registerForIncomingRing(this, PHONE_INCOMING_RING, null);

		}
		if (mCM.getFgPhone().getPhoneType() == Phone.PHONE_TYPE_GSM) {
			if (FeatureOption.MTK_GEMINI_SUPPORT) {
				mCMGemini.registerForRingbackToneGemini(this,
						PHONE_RINGBACK_TONE, null, Phone.GEMINI_SIM_1);
				mCMGemini.registerForRingbackToneGemini(this,
						PHONE_RINGBACK_TONE2, null, Phone.GEMINI_SIM_2);
			} else {
				mCM.registerForRingbackTone(this, PHONE_RINGBACK_TONE, null);
				mCM.registerForResendIncallMute(this, PHONE_RESEND_MUTE, null);
			}
		} else {
			mCM.registerForCdmaOtaStatusChange(this,
					EVENT_OTA_PROVISION_CHANGE, null);
			mCM.registerForCallWaiting(this, PHONE_CDMA_CALL_WAITING, null);
			mCM.registerForDisplayInfo(this, PHONE_STATE_DISPLAYINFO, null);
			mCM.registerForSignalInfo(this, PHONE_STATE_SIGNALINFO, null);
			mCM.registerForInCallVoicePrivacyOn(this, PHONE_ENHANCED_VP_ON,
					null);
			mCM.registerForInCallVoicePrivacyOff(this, PHONE_ENHANCED_VP_OFF,
					null);

		}
       }
       
       private void onDisconnect(AsyncResult r, final int simId) {
           if (VDBG) log("onDisconnect()...  CallManager state: " + mCM.getState() + ", sim id " + simId);
           super.onDisconnect(r);
       }
       
       /**
        * Resets the audio mode and speaker state when a call ends.
        */
       protected void resetAudioStateAfterDisconnect() {
           if (VDBG) log("resetAudioStateAfterDisconnect()...");

           if (mBluetoothHandsfree != null) {
               mBluetoothHandsfree.audioOff();
           }

           // call turnOnSpeaker() with state=false and store=true even if speaker
           // is already off to reset user requested speaker state.
           PhoneUtils.turnOnSpeaker(mApplication, false, true);

           PhoneUtils.setAudioMode(mCM);
       }
       
       private void onMwiChanged(boolean visible, int simId) {
           if (VDBG) log("onMwiChanged(): " + visible + "simid:" + simId);

           // "Voicemail" is meaningless on non-voice-capable devices,
           // so ignore MWI events.
           if (!PhoneGlobals.sVoiceCapable) {
               // ...but still log a warning, since we shouldn't have gotten this
               // event in the first place!
               // (PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR events
               // *should* be blocked at the telephony layer on non-voice-capable
               // capable devices.)
               Log.w(LOG_TAG, "Got onMwiChanged() on non-voice-capable device! Ignoring...");
               return;
           }

           ((MtkNotificationMgr)mApplication.notificationMgr).updateMwi(visible, simId);
       }
	
       /**
        * Posts a delayed PHONE_MWI_CHANGED event, to schedule a "retry" for a
        * failed NotificationMgr.updateMwi() call.
        */
       /* package */ void sendMwiChangedDelayed(long delayMillis, int simId) {
           Message message = Message.obtain();

           if (simId == Phone.GEMINI_SIM_1)
           {
               message.what = PHONE_MWI_CHANGED;        
           }
           else
           {
               message.what = PHONE_MWI_CHANGED2;        
           }

           sendMessageDelayed(message, delayMillis);
       }
       
       
       private void onCfiChanged(boolean visible, int simId) {
           if (VDBG) log("onCfiChanged(): " + visible + "simId:" + simId);
           ((MtkNotificationMgr)mApplication.notificationMgr).updateCfi(visible, simId);
       }
       
       
       /**
        * Helper class to play tones through the earpiece (or speaker / BT)
        * during a call, using the ToneGenerator.
        *
        * To use, just instantiate a new InCallTonePlayer
        * (passing in the TONE_* constant for the tone you want)
        * and start() it.
        *
        * When we're done playing the tone, if the phone is idle at that
        * point, we'll reset the audio routing and speaker state.
        * (That means that for tones that get played *after* a call
        * disconnects, like "busy" or "congestion" or "call ended", you
        * should NOT call resetAudioStateAfterDisconnect() yourself.
        * Instead, just start the InCallTonePlayer, which will automatically
        * defer the resetAudioStateAfterDisconnect() call until the tone
        * finishes playing.)
        */
       private class MtkInCallTonePlayer extends InCallTonePlayer {
           private int mToneId;
           private int mState;
           // The possible tones we can play.
           public static final int TONE_NONE = 0;
           public static final int TONE_CALL_WAITING = 1;
           public static final int TONE_BUSY = 2;
           public static final int TONE_CONGESTION = 3;
           public static final int TONE_CALL_ENDED = 4;
           public static final int TONE_VOICE_PRIVACY = 5;
           public static final int TONE_REORDER = 6;
           public static final int TONE_INTERCEPT = 7;
           public static final int TONE_CDMA_DROP = 8;
           public static final int TONE_OUT_OF_SERVICE = 9;
           public static final int TONE_REDIAL = 10;
           public static final int TONE_OTA_CALL_END = 11;
           public static final int TONE_RING_BACK = 12;
           public static final int TONE_UNOBTAINABLE_NUMBER = 13;
           public static final int TONE_CALL_REMINDER = 15;

           // The tone volume relative to other sounds in the stream

           private static final int TONE_RELATIVE_VOLUME_HIPRIEST = 100;
           private static final int TONE_RELATIVE_VOLUME_HIPRI = 80;
           private static final int TONE_RELATIVE_VOLUME_LOPRI = 50;

           // Buffer time (in msec) to add on to tone timeout value.
           // Needed mainly when the timeout value for a tone is the
           // exact duration of the tone itself.
           static final int TONE_TIMEOUT_BUFFER = 20;

           // The tone state
           static final int TONE_OFF = 0;
           static final int TONE_ON = 1;
           static final int TONE_STOPPED = 2;

           MtkInCallTonePlayer(int toneId) {
               super(toneId);
               mToneId = toneId;
               mState = TONE_OFF;
           }

           @Override
           public void run() {
               log("InCallTonePlayer.run(toneId = " + mToneId + ")...");

               int toneType = 0;  // passed to ToneGenerator.startTone()
               int toneVolume;  // passed to the ToneGenerator constructor
               int toneLengthMillis;
               int phoneType = mCM.getFgPhone().getPhoneType();

               switch (mToneId) {
                   case TONE_CALL_WAITING:
                       toneType = ToneGenerator.TONE_SUP_CALL_WAITING;
                       toneVolume = TONE_RELATIVE_VOLUME_HIPRI;
                       // Call waiting tone is stopped by stopTone() method
                       toneLengthMillis = Integer.MAX_VALUE - TONE_TIMEOUT_BUFFER;
                       break;
                   case TONE_BUSY:
                       //display a "Line busy" message while play the busy tone
                       if (FeatureOption.MTK_BRAZIL_CUSTOMIZATION_VIVO) {
                           CallNotifier me = PhoneGlobals.getInstance().notifier;
                           me.sendMessage(me.obtainMessage(DISPLAY_BUSY_MESSAGE));
                       }
                       if (phoneType == Phone.PHONE_TYPE_CDMA) {
                           toneType = ToneGenerator.TONE_CDMA_NETWORK_BUSY_ONE_SHOT;
                           toneVolume = TONE_RELATIVE_VOLUME_LOPRI;
                           toneLengthMillis = 1000;
                       } else if ((phoneType == Phone.PHONE_TYPE_GSM)
                               || (phoneType == Phone.PHONE_TYPE_SIP)) {
                           toneType = ToneGenerator.TONE_SUP_BUSY;
                           toneVolume = TONE_RELATIVE_VOLUME_HIPRI;
                           toneLengthMillis = 4000;
                       } else {
                           throw new IllegalStateException("Unexpected phone type: " + phoneType);
                       }
                       break;
                   case TONE_CONGESTION:
                       toneType = ToneGenerator.TONE_SUP_CONGESTION;
                       toneVolume = TONE_RELATIVE_VOLUME_HIPRI;
                       toneLengthMillis = 4000;
                       break;

                   case TONE_CALL_ENDED:
                       toneType = ToneGenerator.TONE_PROP_PROMPT;
                       //aurora change liguangyu 201311128 for BUG #1048 start
//                     toneVolume = TONE_RELATIVE_VOLUME_HIPRI;
                       toneVolume = 60;
                       //aurora change liguangyu 201311128 for BUG #1048 end
                       //According to audio's request, we change this time from 200 to 512
                       //200ms is too short and maybe cause the tone play time less than expected
                       //toneLengthMillis = 200;
                       toneLengthMillis = 512;
                       break;
                    case TONE_OTA_CALL_END:
                       if (mApplication.cdmaOtaConfigData.otaPlaySuccessFailureTone ==
                               OtaUtils.OTA_PLAY_SUCCESS_FAILURE_TONE_ON) {
                           toneType = ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD;
                           toneVolume = TONE_RELATIVE_VOLUME_HIPRI;
                           toneLengthMillis = 750;
                       } else {
                           toneType = ToneGenerator.TONE_PROP_PROMPT;
                           toneVolume = TONE_RELATIVE_VOLUME_HIPRI;
                           toneLengthMillis = 200;
                       }
                       break;
                   case TONE_VOICE_PRIVACY:
                       toneType = ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE;
                       toneVolume = TONE_RELATIVE_VOLUME_HIPRI;
                       toneLengthMillis = 5000;
                       break;
                   case TONE_REORDER:
                       toneType = ToneGenerator.TONE_CDMA_REORDER;
                       toneVolume = TONE_RELATIVE_VOLUME_HIPRI;
                       toneLengthMillis = 4000;
                       break;
                   case TONE_INTERCEPT:
                       toneType = ToneGenerator.TONE_CDMA_ABBR_INTERCEPT;
                       toneVolume = TONE_RELATIVE_VOLUME_LOPRI;
                       toneLengthMillis = 500;
                       break;
                   case TONE_CDMA_DROP:
                   case TONE_OUT_OF_SERVICE:
                       toneType = ToneGenerator.TONE_CDMA_CALLDROP_LITE;
                       toneVolume = TONE_RELATIVE_VOLUME_LOPRI;
                       toneLengthMillis = 375;
                       break;
                   case TONE_REDIAL:
                       toneType = ToneGenerator.TONE_CDMA_ALERT_AUTOREDIAL_LITE;
                       toneVolume = TONE_RELATIVE_VOLUME_LOPRI;
                       toneLengthMillis = 5000;
                       break;
                   case TONE_RING_BACK:
                       toneType = ToneGenerator.TONE_SUP_RINGTONE;
                       /** M: Change feature: make video call ring back tone clear to hear
                        * below modify from TONE_RELATIVE_VOLUME_HIPRI to 450 due to
                        * video call ring back tone should be clear to hear with speaker opening
                        * voice call's volume does not be influenced by this value */
                       toneVolume = 450;
                       //toneVolume = TONE_RELATIVE_VOLUME_HIPRI;
                       /** M: Change feature @{ */
                       // Call ring back tone is stopped by stopTone() method
                       toneLengthMillis = Integer.MAX_VALUE - TONE_TIMEOUT_BUFFER;
                       break;
                   case TONE_UNOBTAINABLE_NUMBER:
                       toneType = ToneGenerator.TONE_SUP_ERROR;
                       toneVolume = TONE_RELATIVE_VOLUME_HIPRI;
                       toneLengthMillis = 1000;
                       break;
                   case TONE_CALL_REMINDER:
                       if (VDBG) log("InCallTonePlayer.TONE_CALL_NOTIFY ");
                       toneType = ToneGenerator.TONE_PROP_PROMPT;
                       toneVolume = TONE_RELATIVE_VOLUME_HIPRIEST;
                       toneLengthMillis = 500;
                   break;
                   default:
                       throw new IllegalArgumentException("Bad toneId: " + mToneId);
               }

               // If the mToneGenerator creation fails, just continue without it.  It is
               // a local audio signal, and is not as important.
               ToneGenerator toneGenerator;
               try {
                   int stream;
                   if (mBluetoothHandsfree != null) {
                       stream = mBluetoothHandsfree.isAudioOn() ? AudioManager.STREAM_BLUETOOTH_SCO :
                           AudioManager.STREAM_VOICE_CALL;
                   } else {
                       stream = AudioManager.STREAM_VOICE_CALL;
                   }
                   if ((stream == AudioManager.STREAM_VOICE_CALL) && (mToneId == TONE_CALL_REMINDER))
                   {
                       stream = AudioManager.STREAM_SYSTEM;
                   }
                   log("toneVolume is " + toneVolume);
                   toneGenerator = new ToneGenerator(stream, toneVolume);
                   // if (DBG) log("- created toneGenerator: " + toneGenerator);
               } catch (RuntimeException e) {
                   Log.w(LOG_TAG,
                         "InCallTonePlayer: Exception caught while creating ToneGenerator: " + e);
                   toneGenerator = null;
               }

               // Using the ToneGenerator (with the CALL_WAITING / BUSY /
               // CONGESTION tones at least), the ToneGenerator itself knows
               // the right pattern of tones to play; we do NOT need to
               // manually start/stop each individual tone, or manually
               // insert the correct delay between tones.  (We just start it
               // and let it run for however long we want the tone pattern to
               // continue.)
               //
               // TODO: When we stop the ToneGenerator in the middle of a
               // "tone pattern", it sounds bad if we cut if off while the
               // tone is actually playing.  Consider adding API to the
               // ToneGenerator to say "stop at the next silent part of the
               // pattern", or simply "play the pattern N times and then
               // stop."
               boolean needToStopTone = true;
               boolean okToPlayTone = false;

               if (toneGenerator != null) {
                   int ringerMode = mAudioManager.getRingerMode();
                   if (phoneType == Phone.PHONE_TYPE_CDMA) {
                       if (toneType == ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD) {
                           if ((ringerMode != AudioManager.RINGER_MODE_SILENT) &&
                                   (ringerMode != AudioManager.RINGER_MODE_VIBRATE)) {
                               if (DBG) log("- InCallTonePlayer: start playing call tone=" + toneType);
                               okToPlayTone = true;
                               needToStopTone = false;
                           }
                       } else if ((toneType == ToneGenerator.TONE_CDMA_NETWORK_BUSY_ONE_SHOT) ||
                               (toneType == ToneGenerator.TONE_CDMA_REORDER) ||
                               (toneType == ToneGenerator.TONE_CDMA_ABBR_REORDER) ||
                               (toneType == ToneGenerator.TONE_CDMA_ABBR_INTERCEPT) ||
                               (toneType == ToneGenerator.TONE_CDMA_CALLDROP_LITE)) {
                           if (ringerMode != AudioManager.RINGER_MODE_SILENT) {
                               if (DBG) log("InCallTonePlayer:playing call fail tone:" + toneType);
                               okToPlayTone = true;
                               needToStopTone = false;
                           }
                       } else if ((toneType == ToneGenerator.TONE_CDMA_ALERT_AUTOREDIAL_LITE) ||
                                  (toneType == ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE)) {
                           if ((ringerMode != AudioManager.RINGER_MODE_SILENT) &&
                                   (ringerMode != AudioManager.RINGER_MODE_VIBRATE)) {
                               if (DBG) log("InCallTonePlayer:playing tone for toneType=" + toneType);
                               okToPlayTone = true;
                               needToStopTone = false;
                           }
                       } else { // For the rest of the tones, always OK to play.
                           okToPlayTone = true;
                       }
                   } else {  // Not "CDMA"
                       okToPlayTone = true;
                   }

                   synchronized (this) {
                       if (okToPlayTone && mState != TONE_STOPPED) {
                           mState = TONE_ON;
               if (DBG) log("- InCallTonePlayer: startTone");
                           
                         //Tell AudioManager play the waiting tone || TONE_CALL_REMINDER
                           if ((mToneId == TONE_CALL_WAITING || mToneId == TONE_CALL_REMINDER) && DualTalkUtils.isSupportDualTalk()) {
                               mAudioManager.setParameters("SetWarningTone=14");
                           }
                           
                           toneGenerator.startTone(toneType);
                           try {
                               wait(toneLengthMillis + TONE_TIMEOUT_BUFFER);
                           } catch  (InterruptedException e) {
                               Log.w(LOG_TAG,
                                     "InCallTonePlayer stopped: " + e);
                           }
                           if (needToStopTone) {
                               toneGenerator.stopTone();
                           }
                       }
                       // if (DBG) log("- InCallTonePlayer: done playing.");
                       toneGenerator.release();
                       mState = TONE_OFF;
                       
                       if (DBG) log("- InCallTonePlayer: stopTone");
                   }
               }

               // Finally, do the same cleanup we otherwise would have done
               // in onDisconnect().
               //
               // (But watch out: do NOT do this if the phone is in use,
               // since some of our tones get played *during* a call (like
               // CALL_WAITING) and we definitely *don't*
               // want to reset the audio mode / speaker / bluetooth after
               // playing those!
               // This call is really here for use with tones that get played
               // *after* a call disconnects, like "busy" or "congestion" or
               // "call ended", where the phone has already become idle but
               // we need to defer the resetAudioStateAfterDisconnect() call
               // till the tone finishes playing.)
               if (mCM.getState() == PhoneConstants.State.IDLE) {
                   resetAudioStateAfterDisconnect();
               }
               
               mToneThread = null;
           }

           public void stopTone() {
               synchronized (this) {
                   if (mState == TONE_ON) {
                       notify();
                   }
                   mState = TONE_STOPPED;
               }
           }
       }
       
       public void resetBeforeCall() {
           if (mToneThread != null && mToneThread.isAlive()) {
               mToneThread.stopTone();
               if (DBG) {
                   log("resetBeforeCall: notify the tone thread to exit.");
               }
           } else {
               if (DBG) {
                   log("resetBeforeCall: do nothing.");
               }
           }
       }
       
       protected void log(String msg) {
           Log.d(LOG_TAG, msg);
       }

}