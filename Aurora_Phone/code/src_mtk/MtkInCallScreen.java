/*
 * Copyright (c) 2011-2013 The Linux Foundation. All rights reserved.
 * Not a Contribution.
 *
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

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.ComponentName;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import com.android.phone.AuroraTelephony.SIMInfo;
import com.android.phone.AuroraTelephony.SimInfo;
import android.telephony.ServiceState;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.method.DialerKeyListener;
import android.telephony.PhoneNumberUtils;
import android.util.EventLog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.MenuInflater;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import com.android.internal.telephony.sip.SipPhone;
import android.widget.ListAdapter;

import android.telephony.TelephonyManager;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallerInfo;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyCapabilities;
import com.android.phone.Constants.CallStatusCode;
import com.android.phone.InCallUiState.InCallScreenMode;
import com.android.phone.OtaUtils.CdmaOtaScreenState;
import com.android.phone.sip.SipSharedPreferences;
import com.android.internal.telephony.TelephonyProperties;
import com.android.internal.telephony.gsm.SuppServiceNotification;
import com.android.internal.telephony.gemini.MTKCallManager;
import com.android.internal.telephony.gemini.GeminiPhone;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import com.android.internal.telephony.gsm.SuppCrssNotification;
import android.widget.Button;
import android.view.MenuItem;
import com.mediatek.common.MediatekClassFactory;
import com.mediatek.common.telephony.IServiceStateExt;
import android.view.SurfaceView;
import java.io.File;
import java.io.IOException;
import android.os.StatFs;
import android.content.ContextWrapper;
import java.util.Timer; 
import java.util.TimerTask; 
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.view.SurfaceHolder;
import android.os.PowerManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.app.ProgressDialog;
import java.util.Timer; 
import java.util.TimerTask; 
import android.text.format.DateFormat;
import java.util.Date;
import android.graphics.drawable.AnimationDrawable;
import android.os.RemoteException;
import android.provider.Telephony.Intents;
import static android.provider.Telephony.Intents.ACTION_UNLOCK_KEYGUARD;
import java.util.List;

import com.android.internal.telephony.cdma.CdmaMmiCode;
import com.android.internal.telephony.gsm.GsmMmiCode;

import com.mediatek.phone.PhoneFeatureConstants.FeatureOption;
import android.os.Process;

import com.mediatek.phone.CallPickerAdapter;
import com.mediatek.phone.DualTalkUtils;
import com.android.internal.telephony.PhoneConstants;

/**
 * Phone app "multi sim in call" screen.
 */
public class MtkInCallScreen extends InCallScreen {
    private static final String LOG_TAG = "MtkInCallScreen";
    private static final boolean DBG =
            (MtkPhoneGlobals.DBG_LEVEL >= 1) && (SystemProperties.getInt("ro.debuggable", 0) == 1);
    private static final boolean VDBG = (MtkPhoneGlobals.DBG_LEVEL >= 2);

    private static final int SUPP_SERVICE_NOTIFICATION = 140;
    private static final int CRSS_SUPP_SERVICE = 141;
    private static final int SUPP_SERVICE_FAILED2 = 142;   
    private static final int PHONE_STATE_CHANGED2 = 143;
    private static final int PHONE_DISCONNECT2 = 144;
    private static final int CRSS_SUPP_SERVICE2 = 145;
    private static final int POST_ON_DIAL_CHARS2 = 146;
    public static final int DELAYED_CLEANUP_AFTER_DISCONNECT2 = 147;
    private static final int SUPP_SERVICE_NOTIFICATION2 = 148;
    private static final int PHONE_INCOMING_RING2 = 149;
    private static final int PHONE_NEW_RINGING_CONNECTION2 = 150;
    private static final int DELAY_AUTO_ANSWER = 200;
    
     static final String EXTRA_FORCE_SPEAKER_ON =
            "com.android.phone.extra.FORCE_SPEAKER_ON";
    
    private Call mForegroundCall;
    private Call mBackgroundCall;
    private Call mRingingCall;
    private Call.State mForegroundLastState = Call.State.IDLE;
    private Call.State mBackgroundLastState = Call.State.IDLE;
    private Call.State mRingingLastState = Call.State.IDLE;
    
    private MTKCallManager mCMGemini;
    private BluetoothHandsfree mBluetoothHandsfree;
    
    private AlertDialog callSelectDialog;
    
    private static int mPreHeadsetPlugState = -1;
    
    
    DualTalkUtils mDualTalk;
    
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mIsDestroyed) {
                if (DBG) log("Handler: ignoring message " + msg + "; we're destroyed!");
                return;
            }
            if (!mIsForegroundActivity) {
                if (DBG) log("Handler: handling message " + msg + " while not in foreground");
                // Continue anyway; some of the messages below *want* to
                // be handled even if we're not the foreground activity
                // (like DELAYED_CLEANUP_AFTER_DISCONNECT), and they all
                // should at least be safe to handle if we're not in the
                // foreground...
            }

            switch (msg.what) {
                case SUPP_SERVICE_FAILED:
                case SUPP_SERVICE_FAILED2:
                    onSuppServiceFailed((AsyncResult) msg.obj);
                    break;

                    
                case CRSS_SUPP_SERVICE:
                case CRSS_SUPP_SERVICE2:
                    onSuppCrssSuppServiceNotification((AsyncResult) msg.obj);
                    break;

                case PHONE_STATE_CHANGED:
                case PHONE_STATE_CHANGED2:
                    if (DBG) {
                        log("----------------------------------------InCallScreen Phone state change----------------------------------");
                    }
                    onPhoneStateChanged((AsyncResult) msg.obj);
                    break;

                case PHONE_DISCONNECT:
                case PHONE_DISCONNECT2:
                    onDisconnect((AsyncResult) msg.obj, msg.what);
                    break;

                case EVENT_HEADSET_PLUG_STATE_CHANGED:
                    if ( mPreHeadsetPlugState != msg.arg1 || mPreHeadsetPlugState == -1) {
                        // Update the in-call UI, since some UI elements (in
                        // particular the "Speaker" menu button) change state
                        // depending on whether a headset is plugged in.
                        // TODO: A full updateScreen() is overkill here, since
                        // the value of PhoneApp.isHeadsetPlugged() only affects
                        // a single menu item. (But even a full updateScreen()
                        // is still pretty cheap, so let's keep this simple
                        // for now.)
                        if (!isBluetoothAudioConnected()) {
                            if (msg.arg1 != 1) {
                                // If the state is "not connected", restore the speaker state.
                                // We ONLY want to do this on the wired headset connect /
                                // disconnect events for now though, so we're only triggering
                                // on EVENT_HEADSET_PLUG_STATE_CHANGED.
                                PhoneUtils.restoreSpeakerMode(MtkInCallScreen.this);
                            } else {
                                // If the state is "connected", force the speaker off without
                                // storing the state.
                                PhoneUtils.turnOnSpeaker(MtkInCallScreen.this, false, false);
                                // If the dialpad is open, we need to start the timer that will
                                // eventually bring up the "touch lock" overlay.
                                /*
                                if (mDialer.isOpened() && !isTouchLocked()) {
                                    resetTouchLockTimer();
                                }*/
                            }
                        } 
                        mPreHeadsetPlugState = msg.arg1;
                    }
                    updateScreen();

                    // Also, force the "audio mode" popup to refresh itself if
                    // it's visible, since one of its items is either "Wired
                    // headset" or "Handset earpiece" depending on whether the
                    // headset is plugged in or not.
                    mInCallTouchUi.refreshAudioModePopup();  // safe even if the popup's not active
                    break;


                case PhoneGlobals.MMI_CANCEL:
                    onMMICancel(Phone.GEMINI_SIM_1);
                    break;

                case MtkPhoneGlobals.MMI_CANCEL2:
                    onMMICancel(Phone.GEMINI_SIM_2);
                    break;

                // handle the mmi complete message.
                // since the message display class has been replaced with
                // a system dialog in PhoneUtils.displayMMIComplete(), we
                // should finish the activity here to close the window.
                case PhoneGlobals.MMI_COMPLETE:
                case MtkPhoneGlobals.MMI_COMPLETE2:
                    onMMIComplete((MmiCode) ((AsyncResult) msg.obj).result);
                    break;

                case POST_ON_DIAL_CHARS:
                case POST_ON_DIAL_CHARS2:
                    handlePostOnDialChars((AsyncResult) msg.obj, (char) msg.arg1);
                    break;

                case ADD_VOICEMAIL_NUMBER:
                    addVoiceMailNumberPanel();
                    break;

                case DONT_ADD_VOICEMAIL_NUMBER:
                    dontAddVoiceMailNumber();
                    break;

//                case DELAYED_CLEANUP_AFTER_DISCONNECT:
//                    log("mHandler() DELAYED_CLEANUP_AFTER_DISCONNECT  : SIM1");
//                    delayedCleanupAfterDisconnect(DELAYED_CLEANUP_AFTER_DISCONNECT);
//                    break;
//
//                case DELAYED_CLEANUP_AFTER_DISCONNECT2:
//                    log("mHandler() DELAYED_CLEANUP_AFTER_DISCONNECT  : SIM2");
//                    delayedCleanupAfterDisconnect(DELAYED_CLEANUP_AFTER_DISCONNECT2);
//                    break;

                case REQUEST_UPDATE_BLUETOOTH_INDICATION:
                    if (VDBG) log("REQUEST_UPDATE_BLUETOOTH_INDICATION...");
                    // The bluetooth headset state changed, so some UI
                    // elements may need to update.  (There's no need to
                    // look up the current state here, since any UI
                    // elements that care about the bluetooth state get it
                    // directly from PhoneApp.showBluetoothIndication().)
                    updateScreen();
                    break;

                case PHONE_CDMA_CALL_WAITING:
                    if (DBG) log("Received PHONE_CDMA_CALL_WAITING event ...");
                    Connection cn = mCM.getFirstActiveRingingCall().getLatestConnection();

                    // Only proceed if we get a valid connection object
                    if (cn != null) {
                        // Finally update screen with Call waiting info and request
                        // screen to wake up
                        updateScreen();
                        mApp.updateWakeState();
                    }
                    break;

                case REQUEST_CLOSE_SPC_ERROR_NOTICE:
                    if (mApp.otaUtils != null) {
                        mApp.otaUtils.onOtaCloseSpcNotice();
                    }
                    break;

                case REQUEST_CLOSE_OTA_FAILURE_NOTICE:
                    if (mApp.otaUtils != null) {
                        mApp.otaUtils.onOtaCloseFailureNotice();
                    }
                    break;

                case EVENT_PAUSE_DIALOG_COMPLETE:
                    if (mPausePromptDialog != null) {
                        if (DBG) log("- DISMISSING mPausePromptDialog.");
                        mPausePromptDialog.dismiss();  // safe even if already dismissed
                        mPausePromptDialog = null;
                    }
                    break;

                case EVENT_HIDE_PROVIDER_INFO:
                    mApp.inCallUiState.providerInfoVisible = false;
                    if (mCallCard != null) {
                        mCallCard.updateState(mCM);
                    }
                    break;


                case DELAY_AUTO_ANSWER:
                    try {
                        Context friendContext = createPackageContext("com.mediatek.engineermode",
                                CONTEXT_IGNORE_SECURITY);
                        SharedPreferences sh = friendContext.getSharedPreferences("AutoAnswer",
                                MODE_WORLD_READABLE);

                        if (sh.getBoolean("flag", false)) {
                            if (null != mCM) {
                                PhoneUtils.answerCall(mCM.getFirstActiveRingingCall());
                            }
                        }
                    } catch (NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;


                case REQUEST_UPDATE_SCREEN:
                    updateScreen();
                    break;

                case PHONE_INCOMING_RING:
                case PHONE_INCOMING_RING2:
                    onIncomingRing();
                    break;

                case PHONE_NEW_RINGING_CONNECTION:
                case PHONE_NEW_RINGING_CONNECTION2:
                    onNewRingingConnection();
                    break;

                    
                case PhoneUtils.PHONE_SPEECH_INFO:
                case PhoneUtils.PHONE_SPEECH_INFO2:
//                    AsyncResult ar = (AsyncResult) msg.obj;
//                    if (((int[])ar.result)[0] == 1) {
//                        log("- handler : PHONE_SPEECH_INFO enabled!");
//                        mSpeechEnabled = true;
//                        //CR:ALPS00251944 start
//                        if (mInCallControlState.dialpadEnabled == false) {
//                            log("- handler : PHONE_SPEECH_INFO updateInCallTouchUi!");
//                            updateInCallTouchUi();
//                        }
//                        //CR:ALPS00251944 end
//                    } else {
//                        log("- handler : PHONE_SPEECH_INFO disabled!");
//                        mSpeechEnabled = false;
//                    }
                    break;
                    
                case EVENT_DIALING:
                    if (DBG) log("- EVENT_DIALING value = " + (Boolean) msg.obj);
                	if(!(Boolean) msg.obj) {
                		mIsDialing = false;
                	}
                    mWaitingSpinner.setVisibility((Boolean) msg.obj ? View.VISIBLE : View.GONE);
                    if (mApp.inCallUiState.hasPendingCallStatusCode()) {
                        showStatusIndication(mApp.inCallUiState.getPendingCallStatusCode());
                    }
                	break;

                default:
                    Log.wtf(LOG_TAG, "mHandler: unexpected message: " + msg);
                    break;
            }
        }
    };
    
        private static String ACTION_LOCKED = "com.mediatek.dm.LAWMO_LOCK";
        private static String ACTION_UNLOCK = "com.mediatek.dm.LAWMO_UNLOCK";

        private final BroadcastReceiver mDMLockReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(ACTION_LOCKED)) {
                    int msg = R.string.dm_lock;
                    PhoneConstants.State state = mCM.getState() ;
                    if (state == PhoneConstants.State.IDLE) {
                        return;
                    } else {
                        Toast.makeText(MtkInCallScreen.this, msg, Toast.LENGTH_LONG).show();
                    }
                } else if (action.equals(ACTION_UNLOCK)) {
                    int msg = R.string.dm_unlock;
                    PhoneConstants.State state = mCM.getState() ;
                    if (state == PhoneConstants.State.IDLE) {
                        return;
                    } else {
                        Toast.makeText(MtkInCallScreen.this, msg, Toast.LENGTH_LONG).show();
                    }
                }

                mCallCard.updateState(mCM);
                updateInCallTouchUi();   

            }
        };

        
        
        /* package */ void setPhone(Phone phone) {
            mPhone = phone;
            // Hang onto the three Call objects too; they're singletons that
            // are constant (and never null) for the life of the Phone.        
            mForegroundCall = mCM.getActiveFgCall();
            mBackgroundCall = mCM.getFirstActiveBgCall();
            mRingingCall = mCM.getFirstActiveRingingCall();
        }
        
        protected void onResume() {
            // !!!!! Need to check below line code
            //Profiler.trace(Profiler.InCallScreenEnterOnResume);
            if (DBG) log("onResume()...");
            super.onResume();
            if (DualTalkUtils.isSupportDualTalk() && mDualTalk == null) {
                mDualTalk = DualTalkUtils.getInstance();
            }
            
            
            
            mHandler.sendEmptyMessageDelayed(DELAY_AUTO_ANSWER, 2000);
            
            adjustProcessPriority();
        }
        
        protected void onPause() {
            if (DBG) log("onPause()...");
            super.onPause();
            
//            if (mHandler.hasMessages(DELAYED_CLEANUP_AFTER_DISCONNECT2)
//                    && (mCM.getState() == PhoneConstants.State.IDLE)) {
//                if (DBG) log("DELAYED_CLEANUP_AFTER_DISCONNECT detected, moving UI to background.");
//                endInCallScreenSession();
//            }
        }
        
        @Override
        protected void onStop() {
            if (DBG) log("onStop()...");
            super.onStop();
            PhoneConstants.State state = mCM.getState();
            if (state == PhoneConstants.State.IDLE) {                                
                ///M: ALPS00383496 Add for the situation that on the process of disconnect a call,
                ///  user change to bluetooth mode when bluetooth is avilable, but bluetooth need
                ///  a disconnect order to resume the A2DP, so we need make sure the phone has
                ///  disconnected the bluetooth when it exits. @{
                if (isBluetoothAvailable() && isBluetoothAudioConnected()) {
                    log("Call disconnectBluetoothAudio from onStop()");
                    disconnectBluetoothAudio();
                }
                ///M: @}
            }
        }
        
        @Override
        protected void onDestroy() {
            Log.i(LOG_TAG, "onDestroy()...  this = " + this);
            super.onDestroy();
            unregisterReceiver(mDMLockReceiver);
        }

        @Override
        public void finish() {
            if (DBG) log("finish()...");
            dismissAllDialogs();
            moveTaskToBack(true);
        }
        
        
        protected void registerForPhoneStates(){
        	mHandler.post(new Runnable(){
        		public void run(){
            		registerForPhoneStatesInternal();	
        		}
        	});        	
        }
        
        protected void registerForPhoneStatesInternal() {
            if (!mRegisteredForPhoneStates) {
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    mCMGemini.registerForPreciseCallStateChangedGemini(mHandler, PHONE_STATE_CHANGED, null, Phone.GEMINI_SIM_1);
                    mCMGemini.registerForDisconnectGemini(mHandler, PHONE_DISCONNECT, null,Phone.GEMINI_SIM_1);
                    mCMGemini.registerForCrssSuppServiceNotificationGemini(mHandler, CRSS_SUPP_SERVICE, null, Phone.GEMINI_SIM_1);

                    mCMGemini.registerForPreciseCallStateChangedGemini(mHandler, PHONE_STATE_CHANGED2, null, Phone.GEMINI_SIM_2);
                    mCMGemini.registerForDisconnectGemini(mHandler, PHONE_DISCONNECT2, null,Phone.GEMINI_SIM_2);
                    mCMGemini.registerForCrssSuppServiceNotificationGemini(mHandler, CRSS_SUPP_SERVICE2, null, Phone.GEMINI_SIM_2);

                    mCMGemini.registerForPostDialCharacterGemini(mHandler, POST_ON_DIAL_CHARS, null, Phone.GEMINI_SIM_1);
                    mCMGemini.registerForPostDialCharacterGemini(mHandler, POST_ON_DIAL_CHARS2, null, Phone.GEMINI_SIM_2);

                    mCMGemini.registerForSuppServiceFailedGemini(mHandler, SUPP_SERVICE_FAILED, null, Phone.GEMINI_SIM_1);
                    mCMGemini.registerForSuppServiceFailedGemini(mHandler, SUPP_SERVICE_FAILED2, null, Phone.GEMINI_SIM_2);

                    mCMGemini.registerForSuppServiceNotificationGemini(mHandler, SUPP_SERVICE_NOTIFICATION, null, Phone.GEMINI_SIM_1);
                    mCMGemini.registerForSuppServiceNotificationGemini(mHandler, SUPP_SERVICE_NOTIFICATION2, null, Phone.GEMINI_SIM_2);
                    mCMGemini.registerForIncomingRingGemini(mHandler, PHONE_INCOMING_RING, null, Phone.GEMINI_SIM_1);
                    mCMGemini.registerForIncomingRingGemini(mHandler, PHONE_INCOMING_RING2, null, Phone.GEMINI_SIM_2);
                    mCMGemini.registerForNewRingingConnectionGemini(mHandler, PHONE_NEW_RINGING_CONNECTION, null, Phone.GEMINI_SIM_1);
                    mCMGemini.registerForNewRingingConnectionGemini(mHandler, PHONE_NEW_RINGING_CONNECTION2, null, Phone.GEMINI_SIM_2);

                    mCMGemini.registerForSpeechInfoGemini(mHandler, PhoneUtils.PHONE_SPEECH_INFO, null, Phone.GEMINI_SIM_1);
                    mCMGemini.registerForSpeechInfoGemini(mHandler, PhoneUtils.PHONE_SPEECH_INFO2, null, Phone.GEMINI_SIM_2);
                } else {
                    mCM.registerForPreciseCallStateChanged(mHandler, PHONE_STATE_CHANGED, null);
                    mCM.registerForDisconnect(mHandler, PHONE_DISCONNECT, null);
                    mCM.registerForCrssSuppServiceNotification(mHandler, CRSS_SUPP_SERVICE, null);
                    mCM.registerForPostDialCharacter(mHandler, POST_ON_DIAL_CHARS, null);
                    mCM.registerForSuppServiceFailed(mHandler, SUPP_SERVICE_FAILED, null);
                    mCM.registerForSuppServiceNotification(mHandler, SUPP_SERVICE_NOTIFICATION, null);
                    mCM.registerForIncomingRing(mHandler, PHONE_INCOMING_RING, null);
                    mCM.registerForNewRingingConnection(mHandler, PHONE_NEW_RINGING_CONNECTION, null);
                    
                    mCM.registerForSpeechInfo(mHandler, PhoneUtils.PHONE_SPEECH_INFO, null);
                }
                // TODO: sort out MMI code (probably we should remove this method entirely).
                // See also MMI handling code in onResume()
                // mCM.registerForMmiInitiate(mHandler, PhoneApp.MMI_INITIATE, null);

                //For C+G dualtalk, the mPhone's type is CDMA
                if (FeatureOption.EVDO_DT_SUPPORT) {
                    mCM.registerForMmiComplete(mHandler, PhoneGlobals.MMI_COMPLETE, null);
                }
                
                int phoneType = mPhone.getPhoneType();
                if (phoneType == Phone.PHONE_TYPE_GSM) {
                    if (FeatureOption.MTK_GEMINI_SUPPORT) {
                        mCMGemini.registerForMmiCompleteGemini(mHandler, PhoneGlobals.MMI_COMPLETE, null, Phone.GEMINI_SIM_1);
                        mCMGemini.registerForMmiCompleteGemini(mHandler, MtkPhoneGlobals.MMI_COMPLETE2, null, Phone.GEMINI_SIM_2);
                    } else {
                        // register for the MMI complete message.  Upon completion,
                        // PhoneUtils will bring up a system dialog instead of the
                        // message display class in PhoneUtils.displayMMIComplete().
                        // We'll listen for that message too, so that we can finish
                        // the activity at the same time.
                        mCM.registerForMmiComplete(mHandler, PhoneGlobals.MMI_COMPLETE, null);
                    }
                } else if (phoneType == Phone.PHONE_TYPE_CDMA) {
                    if (DBG) {
                        log("Registering for Call Waiting.");
                    }
                    mCM.registerForCallWaiting(mHandler, PHONE_CDMA_CALL_WAITING, null);
                } else {
                    throw new IllegalStateException("Unexpected phone type: " + phoneType);
                }

                mRegisteredForPhoneStates = true;
            }
        }

        
        protected void unregisterForPhoneStates() {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                mCMGemini.unregisterForPreciseCallStateChangedGemini(mHandler, Phone.GEMINI_SIM_1);
                mCMGemini.unregisterForDisconnectGemini(mHandler, Phone.GEMINI_SIM_1);
                mCMGemini.unregisterForCrssSuppServiceNotificationGemini(mHandler, Phone.GEMINI_SIM_1);

                mCMGemini.unregisterForPreciseCallStateChangedGemini(mHandler, Phone.GEMINI_SIM_2);
                mCMGemini.unregisterForDisconnectGemini(mHandler, Phone.GEMINI_SIM_2);
                mCMGemini.unregisterForCrssSuppServiceNotificationGemini(mHandler, Phone.GEMINI_SIM_2);

                mCMGemini.unregisterForPostDialCharacterGemini(mHandler, Phone.GEMINI_SIM_1);
                mCMGemini.unregisterForPostDialCharacterGemini(mHandler, Phone.GEMINI_SIM_2);

                mCMGemini.unregisterForSuppServiceFailedGemini(mHandler, Phone.GEMINI_SIM_1);
                mCMGemini.unregisterForSuppServiceFailedGemini(mHandler, Phone.GEMINI_SIM_2);

                mCMGemini.unregisterForSpeechInfoGemini(mHandler, Phone.GEMINI_SIM_1);
                mCMGemini.unregisterForSpeechInfoGemini(mHandler, Phone.GEMINI_SIM_2);
                //Need Wenqi API: mCMGemini.unregisterForSuppServiceNotificationGemini(mHandler, Phone.GEMINI_SIM_1);
                //Need Wenqi API: mCMGemini.unregisterForSuppServiceNotificationGemini(mHandler, Phone.GEMINI_SIM_2);
            } else {
                mCM.unregisterForPreciseCallStateChanged(mHandler);
                mCM.unregisterForDisconnect(mHandler);
                mCM.unregisterForCrssSuppServiceNotification(mHandler);

                mCM.unregisterForPostDialCharacter(mHandler);
                mCM.unregisterForSuppServiceFailed(mHandler);
                mCM.unregisterForSuppServiceNotification(mHandler);
                
                mCM.unregisterForSpeechInfo(mHandler);
            }

            int phoneType = mPhone.getPhoneType();
            if (phoneType == Phone.PHONE_TYPE_GSM) {
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    mCMGemini.unregisterForMmiCompleteGemini(mHandler, Phone.GEMINI_SIM_1);
                    mCMGemini.unregisterForMmiCompleteGemini(mHandler, Phone.GEMINI_SIM_2);
                } else {
                    mCM.unregisterForMmiInitiate(mHandler);
                    mCM.unregisterForMmiComplete(mHandler);
                }
            } else if (phoneType == Phone.PHONE_TYPE_CDMA) {
                if (DBG) {
                    log("Registering for Call Waiting.");
                }
                mCM.unregisterForCallWaiting(mHandler);
            }
            //For C+G dualtalk, the mPhone's type is CDMA
            if (FeatureOption.EVDO_DT_SUPPORT) {
                mCM.unregisterForMmiComplete(mHandler);
            }

            mCM.unregisterForPostDialCharacter(mHandler);
            //mCM.unregisterForSuppServiceFailed(mHandler);
            mCM.unregisterForIncomingRing(mHandler);
            mCM.unregisterForNewRingingConnection(mHandler);
            mRegisteredForPhoneStates = false;
        }
        
        
        
        
        
        
        protected void internalResolveIntent(Intent intent) {
            if (intent == null || intent.getAction() == null) {
                return;
            }
            super.internalResolveIntent(intent);
            String action = intent.getAction();
            if (DBG) log("internalResolveIntent: action=" + action);
            
            if (action.equals(intent.ACTION_MAIN)) {
				if (FeatureOption.MTK_TB_APP_CALL_FORCE_SPEAKER_ON == true) {
					if (intent.hasExtra(EXTRA_FORCE_SPEAKER_ON)) {
						boolean forceSpeakerOn = intent.getBooleanExtra(
								EXTRA_FORCE_SPEAKER_ON, false);
						if (forceSpeakerOn) {
							Log.e("MTK_TB_APP_CALL_FORCE_SPEAKER_ON",
									"forceSpeakerOn is true");
							if (!PhoneGlobals.getInstance().isHeadsetPlugged()
									&& !(mBluetoothHandsfree != null && mBluetoothHandsfree
											.isAudioOn())) {
								// Only force the speaker ON while not video call
								// and speaker is not ON
								if (!intent.getBooleanExtra(
										Constants.EXTRA_IS_VIDEO_CALL, false)
										&& !PhoneUtils.isSpeakerOn(mApp)) {
									Log.e("MTK_TB_APP_CALL_FORCE_SPEAKER_ON",
											"PhoneUtils.turnOnSpeaker");
									PhoneUtils
											.turnOnSpeaker(mApp, true, true, true);
								}
							}
						}
					}
				}
            }
            
            if (action.equals(Intent.ACTION_ANSWER)) {
                internalAnswerCall();

                mApp.setRestoreMuteOnInCallResume(false);
                return;
            }
        }
        
        
        
    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
    
    protected boolean handleCallKey() {

        if (DualTalkUtils.isSupportDualTalk()) {
            if (mDualTalk != null && mDualTalk.isCdmaAndGsmActive()) {
                return handleCallKeyForDualTalk();
            }
        }
        return super.handleCallKey();
    }
    
  //MTK begin:
    static final int SUP_TYPE = 0x91;
    void onSuppServiceNotification(AsyncResult r) {
        SuppServiceNotification notification = (SuppServiceNotification) r.result;
        if (DBG) {
            log("onSuppServiceNotification: " + notification);
        }

        String msg = null;
        // MO
        if (notification.notificationType == 0) {
            msg = getSuppServiceMOStringId(notification);   
        } else if (notification.notificationType == 1) {
            // MT 
            String str = "";
            msg = getSuppServiceMTStringId(notification);
            // not 0x91 should add + .
            if (notification.type == SUP_TYPE) {
                if (notification.number != null && notification.number.length() != 0) {
                    str = " +" + notification.number;
                }
            }
            msg = msg + str;
        }
        // Display Message
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
    
    private String getSuppServiceMOStringId(SuppServiceNotification notification) {
        // TODO Replace the strings.
        String retStr = "";
        switch (notification.code) {
        case SuppServiceNotification.MO_CODE_UNCONDITIONAL_CF_ACTIVE:
            retStr = getResources().getString(R.string.mo_code_unconditional_cf_active);
            break;
        case SuppServiceNotification.MO_CODE_SOME_CF_ACTIVE:
            retStr = getResources().getString(R.string.mo_code_some_cf_active);
            break;
        case SuppServiceNotification.MO_CODE_CALL_FORWARDED :
            retStr = getResources().getString(R.string.mo_code_call_forwarded);
            break;
        case SuppServiceNotification.MO_CODE_CALL_IS_WAITING:
            retStr = getResources().getString(R.string.mo_code_call_is_waiting);
            break;
        case SuppServiceNotification.MO_CODE_CUG_CALL:
            retStr = getResources().getString(R.string.mo_code_cug_call);
            retStr = retStr + " " + notification.index ;
            break;
        case SuppServiceNotification.MO_CODE_OUTGOING_CALLS_BARRED:
            retStr = getResources().getString(R.string.mo_code_outgoing_calls_barred);
            break;
        case SuppServiceNotification.MO_CODE_INCOMING_CALLS_BARRED:
            retStr = getResources().getString(R.string.mo_code_incoming_calls_barred);
            break;
        case SuppServiceNotification.MO_CODE_CLIR_SUPPRESSION_REJECTED:
            retStr = getResources().getString(R.string.mo_code_clir_suppression_rejected);
            break;
        case SuppServiceNotification.MO_CODE_CALL_DEFLECTED:
            retStr = getResources().getString(R.string.mo_code_call_deflected);
            break;
        default:
            // Attempt to use a service we don't recognize or support
            // ("Unsupported service" or "Selected service failed")
            retStr = getResources().getString(R.string.incall_error_supp_service_unknown);
            break;
        }
        return retStr;
    }

    private String getSuppServiceMTStringId(SuppServiceNotification notification) {
        // TODO Replace the strings.
        String retStr = "";
        switch (notification.code) {
        case SuppServiceNotification.MT_CODE_FORWARDED_CALL:
            retStr = getResources().getString(R.string.mt_code_forwarded_call);
            break;
        case SuppServiceNotification.MT_CODE_CUG_CALL:
            retStr = getResources().getString(R.string.mt_code_cug_call);
            retStr = retStr + " " + notification.index ;
            break;
        case SuppServiceNotification.MT_CODE_CALL_ON_HOLD :
            retStr = getResources().getString(R.string.mt_code_call_on_hold);
            break;
        case SuppServiceNotification.MT_CODE_CALL_RETRIEVED:
            retStr = getResources().getString(R.string.mt_code_call_retrieved);
            break;
        case SuppServiceNotification.MT_CODE_MULTI_PARTY_CALL:
            retStr = getResources().getString(R.string.mt_code_multi_party_call);
            break;
        case SuppServiceNotification.MT_CODE_ON_HOLD_CALL_RELEASED:
            retStr = getResources().getString(R.string.mt_code_on_hold_call_released);
            break;
        case SuppServiceNotification.MT_CODE_FORWARD_CHECK_RECEIVED:
            retStr = getResources().getString(R.string.mt_code_forward_check_received);
            break;
        case SuppServiceNotification.MT_CODE_CALL_CONNECTING_ECT:
            retStr = getResources().getString(R.string.mt_code_call_connecting_ect);
            break;
        case SuppServiceNotification.MT_CODE_CALL_CONNECTED_ECT:
            retStr = getResources().getString(R.string.mt_code_call_connected_ect);
            break;
        case SuppServiceNotification.MT_CODE_DEFLECTED_CALL:
            retStr = getResources().getString(R.string.mt_code_deflected_call);
            break;
        case SuppServiceNotification.MT_CODE_ADDITIONAL_CALL_FORWARDED:
            retStr = getResources().getString(R.string.mt_code_additional_call_forwarded);
            break;
        case SuppServiceNotification.MT_CODE_FORWARDED_CF:
            retStr = getResources().getString(R.string.mt_code_forwarded_call)
                    + "(" + getResources().getString(R.string.mt_code_forwarded_cf) + ")";
            break;
        case SuppServiceNotification.MT_CODE_FORWARDED_CF_UNCOND:
            retStr = getResources().getString(R.string.mt_code_forwarded_call)
                    + "(" + getResources().getString(R.string.mt_code_forwarded_cf_uncond) + ")";
            break;
        case SuppServiceNotification.MT_CODE_FORWARDED_CF_COND:
            retStr = getResources().getString(R.string.mt_code_forwarded_call)
                    + "(" + getResources().getString(R.string.mt_code_forwarded_cf_cond) + ")";
            break;
        case SuppServiceNotification.MT_CODE_FORWARDED_CF_BUSY:
            retStr = getResources().getString(R.string.mt_code_forwarded_call)
                    + "(" + getResources().getString(R.string.mt_code_forwarded_cf_busy) + ")";
            break;
        case SuppServiceNotification.MT_CODE_FORWARDED_CF_NO_REPLY:
            retStr = getResources().getString(R.string.mt_code_forwarded_call)
                    + "(" + getResources().getString(R.string.mt_code_forwarded_cf_no_reply) + ")";
            break;
        case SuppServiceNotification.MT_CODE_FORWARDED_CF_NOT_REACHABLE:
            retStr = getResources().getString(R.string.mt_code_forwarded_call)
                    + "(" + getResources().getString(R.string.mt_code_forwarded_cf_not_reachable) + ")";
            break;
        default:
            // Attempt to use a service we don't recognize or support
            // ("Unsupported service" or "Selected service failed")
            retStr = getResources().getString(R.string.incall_error_supp_service_unknown);
            break;
        }
        return retStr;
    }

    void doSuppCrssSuppServiceNotification(String number) {
        Connection conn = null;
        if (mForegroundCall != null) {
            int phoneType = mPhone.getPhoneType();
            if (phoneType == Phone.PHONE_TYPE_CDMA) {
                conn = mForegroundCall.getLatestConnection();
            } else if (phoneType == Phone.PHONE_TYPE_GSM) {
                conn = mForegroundCall.getEarliestConnection();
            } else {
                throw new IllegalStateException("Unexpected phone type: "
                + phoneType);
            }
        }
        if (conn == null) {
            // TODO
            if (DBG) {
                log(" Connnection is null");
            }
            return;
        } else {
            Object o = conn.getUserData();
            if (o instanceof AuroraCallerInfo) {
            	AuroraCallerInfo ci = (AuroraCallerInfo) o;
                // Update CNAP information if Phone state change occurred
                if (DBG) {
                    log("SuppCrssSuppService ci.phoneNumber:" + ci.phoneNumber);
                }
                if (!ci.isVoiceMailNumber() && !ci.isEmergencyNumber()) {
                    ci.phoneNumber = number;
                }
            } else if (o instanceof PhoneUtils.CallerInfoToken) {
            	AuroraCallerInfo ci = ((PhoneUtils.CallerInfoToken) o).currentInfo;
                if (!ci.isVoiceMailNumber()) {
                    ci.phoneNumber = number;
                }
            } 
            conn.setUserData(o);
            updateScreen();
        }
    }

    void onSuppCrssSuppServiceNotification(AsyncResult r) {
        SuppCrssNotification notification = (SuppCrssNotification) r.result;
        if (DBG) {
            log("SuppCrssNotification: " + notification);
        }
        switch (notification.code) {
        case SuppCrssNotification.CRSS_CALL_WAITING:
            break;
        case SuppCrssNotification.CRSS_CALLED_LINE_ID_PREST:
            doSuppCrssSuppServiceNotification(notification.number);
            break;
        case SuppCrssNotification.CRSS_CALLING_LINE_ID_PREST:
            break;
        case SuppCrssNotification.CRSS_CONNECTED_LINE_ID_PREST:
            doSuppCrssSuppServiceNotification(PhoneNumberUtils.stringFromStringAndTOA(notification.number,notification.type));
            break;
        }
        return;
    }
//MTK end

    
    void onSuppServiceFailed(AsyncResult r) {
    	super.onSuppServiceFailed(r);
        Phone.SuppService service = (Phone.SuppService) r.result;
        if (DBG) log("onSuppServiceFailed: " + service);

        int errorMessageResId;
        switch (service) {
	        case SWITCH:
	            if (DualTalkUtils.isSupportDualTalk() && mDualTalk != null) {
	                if (mDualTalk.isCdmaAndGsmActive()) {
	                    log("onSuppServiceFailed: can't hold, so hangup!");
	                    PhoneUtils.hangup(mCM.getActiveFgCall());
	                    Toast.makeText(MtkInCallScreen.this, R.string.end_call_because_can_not_hold, Toast.LENGTH_LONG).show();
	                    return;
	                }
	            }
	            break;
        }
        
    }
    
    private void updateLocalCache() {
        mForegroundCall = mCM.getActiveFgCall();
        mBackgroundCall = mCM.getFirstActiveBgCall();
        mRingingCall = mCM.getFirstActiveRingingCall();
    }
    
    protected void onPhoneStateChanged(AsyncResult r) {
    	super.onPhoneStateChanged(r);
        PhoneConstants.State state = mCM.getState();
        if (DBG) log("onPhoneStateChanged: current state = " + state);
        if (state != PhoneConstants.State.RINGING) {
            // if now is not Ringing, reset incoming call mute
//            muteIncomingCall(false);
            CallNotifier notifier = PhoneGlobals.getInstance().notifier;
            notifier.silenceRinger();
        }

        updateLocalCache();
    }
    
    private void onDisconnect(AsyncResult r , int msg) {
    	super.onDisconnect(r);
        if (null != callSelectDialog && callSelectDialog.isShowing()) {
            callSelectDialog.dismiss();
            callSelectDialog = null;
        }
    }
    
    /**
     * Handles an MMI_CANCEL event, which is triggered by the button
     * (labeled either "OK" or "Cancel") on the "MMI Started" dialog.
     * @see PhoneUtils#cancelMmiCode(Phone)
     */
    private void onMMICancel(int simId) {
        if (VDBG) log("onMMICancel()...");

        // First of all, cancel the outstanding MMI code (if possible.)
        PhoneUtils.cancelMmiCodeExt(mPhone, simId);

        // Regardless of whether the current MMI code was cancelable, the
        // PhoneApp will get an MMI_COMPLETE event very soon, which will
        // take us to the MMI Complete dialog (see
        // PhoneUtils.displayMMIComplete().)
        //
        // But until that event comes in, we *don't* want to stay here on
        // the in-call screen, since we'll be visible in a
        // partially-constructed state as soon as the "MMI Started" dialog
        // gets dismissed.  So let's forcibly bail out right now.
        if (DBG) log("onMMICancel: finishing InCallScreen...");
        dismissAllDialogs();
        if (mCM.getState() == PhoneConstants.State.IDLE) {
            endInCallScreenSession();
        } else {
            log("Got MMI_COMPLETE, Phone isn't in idle, don't finishing InCallScreen...");
        }
        if (null != mMmiStartedDialog) {
            mMmiStartedDialog.dismiss();
            mMmiStartedDialog = null;
            log("Got MMI_COMPLETE, Phone isn't in idle, dismiss the start progress dialog...");
        }
    }
    
    
    
    @Override
    protected void onCreate(Bundle icicle) {
        Log.i(LOG_TAG, "onCreate()...  this = " + this);
        super.onCreate(icicle);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        mCMGemini = ((MtkPhoneGlobals)mApp).mCMGemini;

        mBluetoothHandsfree = ((MtkPhoneGlobals)mApp).getBluetoothHandsfree();
        if (VDBG) log("- mBluetoothHandsfree: " + mBluetoothHandsfree);
        
//        if (mBluetoothHandsfree != null) {
//            // The PhoneApp only creates a BluetoothHandsfree instance in the
//            // first place if BluetoothAdapter.getDefaultAdapter()
//            // succeeds.  So at this point we know the device is BT-capable.
//        	mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        	mBluetoothAdapter.getProfileProxy(getApplicationContext(), mBluetoothProfileServiceListener,
//                                    BluetoothProfile.HEADSET);
//
//        }

        mApp.getBluetoothHeadsetOnCreate();

        IntentFilter dmLockFilter = new IntentFilter(ACTION_LOCKED);
        dmLockFilter.addAction(ACTION_UNLOCK);

        registerReceiver(mDMLockReceiver, dmLockFilter);
        
        mHandler.sendEmptyMessageDelayed(DELAY_AUTO_ANSWER, 2000);
//        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
    }
    
    /**
     * Handles an MMI_COMPLETE event, which is triggered by telephony,
     * implying MMI
     */
    protected void onMMIComplete(MmiCode mmiCode) {
        //if (hasMessages(DELAY_TO_SHOW_MMI_INIT)) {
        //    removeMessages(DELAY_TO_SHOW_MMI_INIT);
        //}
        // Check the code to see if the request is ready to
        // finish, this includes any MMI state that is not
        // PENDING.
        // if phone is a CDMA phone display feature code completed message
        int phoneType = Phone.PHONE_TYPE_GSM;
        if (mmiCode instanceof GsmMmiCode) {
            phoneType = Phone.PHONE_TYPE_GSM;
        } else if (mmiCode instanceof CdmaMmiCode) {
            phoneType = Phone.PHONE_TYPE_CDMA;
        } else {
            phoneType = mPhone.getPhoneType();
        }
        if (phoneType == Phone.PHONE_TYPE_CDMA) {
            PhoneUtils.displayMMIComplete(mPhone, mApp, mmiCode, null, null);
        } else if (phoneType == Phone.PHONE_TYPE_GSM) {
            if (mmiCode.getState() != MmiCode.State.PENDING) {
                if (DBG) log("Got MMI_COMPLETE, finishing InCallScreen...");
                if (mCM.getState() == PhoneConstants.State.IDLE) {
                    endInCallScreenSession();
                } else {
                    log("Got MMI_COMPLETE, Phone isn't in idle, don't finishing InCallScreen...");
                }
                
                if (null != mMmiStartedDialog) {
                    mMmiStartedDialog.dismiss();
                    mMmiStartedDialog = null;
                    log("Got MMI_COMPLETE, Phone isn't in idle, dismiss the start progress dialog...");
                }
            }
        }
    }
    
    private void cleanupAfterDisconnect(int msg) {
        mCM.clearDisconnected();
    }

    /*
     * do not show the 'accpt waiting and hang up active' menu item
     *  when it's not a fta test card'
     */
    boolean okToShowFTAMenu() {
        log("okToAcceptWaitingAndHangupActive");
        boolean retval = false;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            GeminiPhone phone = (GeminiPhone) PhoneGlobals.getInstance().phone;
            int slot = -1;
            if (phone.getStateGemini(Phone.GEMINI_SIM_2) != PhoneConstants.State.IDLE) {
                slot = Phone.GEMINI_SIM_2;
            } else if (phone.getStateGemini(Phone.GEMINI_SIM_1) != PhoneConstants.State.IDLE) {
                slot = Phone.GEMINI_SIM_1;
            }
            log("slot = " + slot);
            if (slot != -1) {
                retval = PhoneGlobals.getInstance().phoneMgr.isTestIccCardGemini(slot);
            }
        } else {
            retval = PhoneGlobals.getInstance().phoneMgr.isTestIccCard();
        }
        log("retval = " + retval);
        return retval;
    }

    private boolean canHangupAll() {
        Call fgCall = mCM.getActiveFgCall();
        Call bgCall = mCM.getFirstActiveBgCall();
        boolean retval = false;
        if (null != bgCall && bgCall.getState() == Call.State.HOLDING) {
            if (null != fgCall && fgCall.getState() == Call.State.ACTIVE) {
                retval = true;
            } else if (PhoneUtils.hasActivefgEccCall(mCM)) {
                retval = true;
            }
        }
        log("canHangupAll = " + retval);
        return retval;
    }

    private void onAddCallClick() {
        PhoneUtils.startNewCall(mCM);
    }

    
    public void handleOnscreenButtonClick(int id) {
        switch (id) {
	        // Actions while an incoming call is ringing:
	        case R.id.swapButton:
	            if (DualTalkUtils.isSupportDualTalk() && mDualTalk.isDualTalkMultipleHoldCase()) {
	                List<Call> list = mDualTalk.getAllNoIdleCalls();
	                selectWhichCallActive(list);
	            } else {
	                internalSwapCalls();
	            }
	            updateInCallTouchUi();
	            return;
	        case R.id.dim_effect_for_third_photo:
	        case R.id.dim_effect_for_fourth_photo:{
	            //mInCallScreen.askWhichCallDisconnected(mDualTalk.getAllNoIdleCalls(), false);
	            if (mDualTalk.isSupportDualTalk() && mDualTalk.isDualTalkMultipleHoldCase()) {
	                handleUnholdAndEnd(mDualTalk.getActiveFgCall());
	            } else if (mDualTalk.isSupportDualTalk() && mDualTalk.isMultiplePhoneActive() && !mDualTalk.hasMultipleRingingCall()) {
	                PhoneUtils.switchHoldingAndActive(mDualTalk.getSecondActiveBgCall());
	            } else {
	                PhoneUtils.switchHoldingAndActive(mApp.mCM.getFirstActiveBgCall());
	            }        
	            updateScreen();        
	        	return;
	        }
        }
    	super.handleOnscreenButtonClick(id);
    }
    
    protected void showStatusIndication(CallStatusCode status) {
    	 switch (status) {
	
	         /**
	          * add by mediatek .inc
	          * description : show the FDN block dialog
	          */
	         case FDN_BLOCKED:
	             log("showGenericErrorDialog, fdn_only");
	             showGenericErrorDialog(R.string.callFailed_fdn_only, false);
	             return;
    	 }
    	super.showStatusIndication(status);
    }
    
    protected void dismissAllDialogs() {
    	super.dismissAllDialogs();
    	
        if (null != callSelectDialog) {
            callSelectDialog.dismiss();
            callSelectDialog = null;
        }
    }
    
    protected void internalAnswerCall() {
        if (DualTalkUtils.isSupportDualTalk()) {
            if (mDualTalk.hasMultipleRingingCall()
                    || mDualTalk.isDualTalkAnswerCase()
                    || mDualTalk.isRingingWhenOutgoing()) {
                internalAnswerCallForDualTalk();
                return;
            }
        }
    	super.internalAnswerCall();
    }
    
    /* package */ void hangupRingingCall() {
        if (DBG) log("hangupRingingCall()...");
        if (VDBG) PhoneUtils.dumpCallManager();
        // In the rare case when multiple calls are ringing, the UI policy
        // it to always act on the first ringing call.
        if (DualTalkUtils.isSupportDualTalk() && mDualTalk.hasMultipleRingingCall()) {
            PhoneUtils.hangupForDualTalk(mDualTalk.getFirstActiveRingingCall());
//            PhoneGlobals.getInstance().notifier.switchRingToneByNeeded(mDualTalk.getSecondActiveRingCall());
        } else {
            PhoneUtils.hangupRingingCall(mCM.getFirstActiveRingingCall());
        }
    }
    
    /**
     * Hang up the all calls.
     */
    public void internalHangupAll() {
        if (DBG) {
            log("internalHangupAll()...");
        }
        try {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                ((GeminiPhone)mPhone).hangupAllGemini(Phone.GEMINI_SIM_1);
                ((GeminiPhone)mPhone).hangupAllGemini(Phone.GEMINI_SIM_2);
            } else {
                mPhone.hangupAll(); 
            }
        } catch (CallStateException ex) {
            log("Error, cannot hangup All Calls");
        }
    }
    
    /**
     * InCallScreen-specific wrapper around PhoneUtils.switchHoldingAndActive().
     */
    private void internalSwapCalls() {
        if (DBG) log("internalSwapCalls()...");
        // Any time we swap calls, force the DTMF dialpad to close.
        // (We want the regular in-call UI to be visible right now, so the
        // user can clearly see which call is now in the foreground.)
        closeDialpadInternal(false);//aurora change zhouxiaobing 20131125

        // Also, clear out the "history" of DTMF digits you typed, to make
        // sure you don't see digits from call #1 while call #2 is active.
        // (Yes, this does mean that swapping calls twice will cause you
        // to lose any previous digits from the current call; see the TODO
        // comment on DTMFTwelvKeyDialer.clearDigits() for more info.)
        mDialer.clearDigits();

        // Swap the fg and bg calls.
        // In the future we may provides some way for user to choose among
        // multiple background calls, for now, always act on the first background calll.
        if (DualTalkUtils.isSupportDualTalk() && mDualTalk.isCdmaAndGsmActive()) {
            handleSwapCdmaAndGsm();
        } else if (DualTalkUtils.isSupportDualTalk() && mDualTalk.hasDualHoldCallsOnly()) {
            //According to planner's define:
            //If there are two calls both in hold status, when tap the
            //swap call button, it will unhold the "background hold call"
            Call bgHoldCall = mDualTalk.getSecondActiveBgCall();
            try {
                bgHoldCall.getPhone().switchHoldingAndActive();
            } catch (CallStateException e) {
                log("internalSwapCalls exception = " + e);
            }
            //Before solution: only switch the two hold call but not change the status
            //mDualTalk.switchCalls();
            //updateScreen();
        } else if (DualTalkUtils.isSupportDualTalk() && mDualTalk.isDualTalkMultipleHoldCase()) {
            Call fgCall = mDualTalk.getActiveFgCall();
            Phone fgPhone = fgCall.getPhone();
            if (fgPhone.getBackgroundCall().getState().isAlive()) {
                if (DBG) {
                    log("Cal foreground phone's switchHoldingAndActive");
                }
                try {
                    fgPhone.switchHoldingAndActive();
                } catch (CallStateException e) {
                    log(e.toString());
                }
            } else {
                if (DBG) {
                    log("PhoneUtils.switchHoldingAndActive");
                }
                PhoneUtils.switchHoldingAndActive(mDualTalk.getFirstActiveBgCall());
            }
            //PhoneUtils.switchHoldingAndActive(mCM.getFirstActiveBgCall());
        } else {
            PhoneUtils.switchHoldingAndActive(mCM.getFirstActiveBgCall());
        }

        // If we have a valid BluetoothHandsfree then since CDMA network or
        // Telephony FW does not send us information on which caller got swapped
        // we need to update the second call active state in BluetoothHandsfree internally
        if (mCM.getBgPhone().getPhoneType() == Phone.PHONE_TYPE_CDMA) {
            BluetoothHandsfree bthf = ((MtkPhoneGlobals)mApp).getBluetoothHandsfree();
            if (bthf != null) {
                bthf.cdmaSwapSecondCallState();
            }
        }

    }
    
    
    public boolean isBluetoothAvailable() {
        if (mBluetoothHandsfree == null) {
            // Device is not BT capable.
            if (VDBG) log("  ==> FALSE (not BT capable)");
            return false;
        }
        return super.isBluetoothAvailable();
    }
    
    /* package */public boolean isBluetoothAudioConnected() {
        if (mBluetoothHandsfree == null) {
            if (VDBG) log("isBluetoothAudioConnected: ==> FALSE (null mBluetoothHandsfree)");
            return false;
        }
        boolean isAudioOn = mBluetoothHandsfree.isAudioOn();
        if (VDBG) log("isBluetoothAudioConnected: ==> isAudioOn = " + isAudioOn);
        return isAudioOn;
    }
    
    
    /* package */ void connectBluetoothAudio() {
        if (VDBG) log("connectBluetoothAudio()...");
        if (mBluetoothHandsfree != null) {
            mBluetoothHandsfree.userWantsAudioOn();
        }

        // Watch out: The bluetooth connection doesn't happen instantly;
        // the userWantsAudioOn() call returns instantly but does its real
        // work in another thread.  The mBluetoothConnectionPending flag
        // is just a little trickery to ensure that the onscreen UI updates
        // instantly. (See isBluetoothAudioConnectedOrPending() above.)
        mBluetoothConnectionPending = true;
        mBluetoothConnectionRequestTime = SystemClock.elapsedRealtime();
    }

    /* package */ void disconnectBluetoothAudio() {
        if (VDBG) log("disconnectBluetoothAudio()...");
        if (mBluetoothHandsfree != null) {
            mBluetoothHandsfree.userWantsAudioOff();
        }
        mBluetoothConnectionPending = false;
    }
    

       

    void adjustProcessPriority() {
        final int myId = Process.myPid();
        if (Process.getThreadPriority(myId) != Process.THREAD_PRIORITY_DEFAULT) {
            Process.setThreadPriority(myId, Process.THREAD_PRIORITY_DEFAULT);
        }
    }
    
    /**
     * Change Feature by mediatek .inc
     * description : support for dualtalk
     */
    void internalAnswerCallForDualTalk() {
        Call ringing = mDualTalk.getFirstActiveRingingCall();
        //In order to make the answer process simply, firtly, check there is outgoingcall, if exist, disconnect it;
        
        if (mDualTalk.isRingingWhenOutgoing()) {
            if (DBG) {
                log("internalAnswerCallForDualTalk: " + "ringing when dialing");
            }
            Call call = mDualTalk.getSecondActivieFgCall();
            if (call.getState().isDialing()) {
                try {
                    Phone phone = call.getPhone();
                    if (phone instanceof SipPhone) {
                        call.hangup();
                    } else {
                        phone.hangupActiveCall();
                    }
                } catch (Exception e) {
                    
                }
            }
        }
        
        List<Call> list = mDualTalk.getAllNoIdleCalls();
        int callCount = list.size();
        
        try {
            if (callCount > 2) {
                if (DBG) {
                    log("internalAnswerCallForDualTalk: " + "has more than two calls exist.");
                }
                //This offen occurs in W+G platform.
                //On C+G platform, the only case is: CDMA has an active(real be hold in network) call,
                //and the GSM has active + hold call, then GSM has a ringing call
                if (mDualTalk.hasActiveCdmaPhone()) {
                    //In this case, the ringing call must be exist in the same phone with active call
                    handleAnswerAndEnd(ringing.getPhone().getForegroundCall());
                    if (DBG) {
                        log("internalAnswerCallForDualTalk (C+G): hangup the gsm active call!");
                    }
                } else {
                    handleAnswerAndEnd(mCM.getActiveFgCall());
                }
                return ;
            } else if (callCount == 2) {
                if (DBG) {
                    log("internalAnswerCallForDualTalk: " + "has two calls exist.");
                }
                if (list.get(0).getPhone() == list.get(1).getPhone()) {
                    if (DBG) {
                        log("internalAnswerCallForDualTalk: " + "two calls exist in the same phone.");
                    }
                    handleAnswerAndEnd(mCM.getActiveFgCall());
                    return ;
                } else {
                    if (DBG) {
                        log("internalAnswerCallForDualTalk: " + "two calls exist in diffrent phone.");
                    }
                    if (mDualTalk.hasActiveOrHoldBothCdmaAndGsm()) {
                        //because gsm has the exact status, so we deduce the cdma call status and then
                        //decide if hold operation is needed by cdma call.
                        Phone gsmPhone = mDualTalk.getActiveGsmPhone();
                        Phone cdmaPhone = mDualTalk.getActiveCdmaPhone();
                        
                        Call cCall = cdmaPhone.getForegroundCall();
                        if (PhoneUtils.hasMultipleConnections(cCall)) {
                            log("internalAnswerCallForDualTalk: cdma has multiple connections, disconneted it!");
                            cCall.hangup();
                            PhoneUtils.answerCall(ringing);
                            return ;
                        }
                        if (gsmPhone.getForegroundCall().getState().isAlive()) {
                            //cdma call is hold, and the ringing call must be gsm call
                            ringing.getPhone().acceptCall();
                            if (DBG) {
                                log("internalAnswerCallForDualTalk: " + "cdma hold + gsm active + gsm ringing");
                            }
                        } else {
                            //gsm has hold call
                            if (DBG) {
                                log("internalAnswerCallForDualTalk: " + "cdma active + gsm holding + cdma ringing/gsm ringing");
                            }
                            PhoneUtils.answerCall(ringing);
                        }
                    } else {
                        //This is for W+G handler
                        for (Call call : list) {
                            Call.State state = call.getState();
                            if (state == Call.State.ACTIVE) {
                                if (ringing.getPhone() != call.getPhone()) {
                                    call.getPhone().switchHoldingAndActive();
                                }
                                PhoneUtils.answerCall(ringing);
                                break;
                            } else if (state == Call.State.HOLDING) {
                                //this maybe confuse, need further check: this happend when the dialing is disconnected
                                PhoneUtils.answerCall(ringing);
                            }
                        }
                    }
                }
            } else if (callCount == 1) {
                if (DBG) {
                    log("internalAnswerCallForDualTalk: " + "there is one call exist.");
                }
                Call call = list.get(0);
                //First check if the only ACTIVE call is CDMA (three-way or call-waitting) call
                if (call.getPhone().getPhoneType() == Phone.PHONE_TYPE_CDMA
                        && PhoneUtils.hasMultipleConnections(call)) {
                    //The ring call must be GSM call
                    log("internalAnswerCallForDualTalk: hangup the cdma multiple call and answer the gsm call!");
                    call.hangup();
                    PhoneUtils.answerCall(ringing);
                    
                } else if (call.getPhone() == ringing.getPhone()) {
                    PhoneUtils.answerCall(ringing);
                } else if (call.getState() == Call.State.ACTIVE) {
                    PhoneUtils.answerCall(ringing);
                } else {
                    PhoneUtils.answerCall(ringing);
                }
            } else if (callCount == 0) {
                if (DBG) {
                    log("internalAnswerCallForDualTalk: " + "there is no call exist.");
                }
                PhoneUtils.answerCall(ringing);
            }
        } catch (Exception e) {
            log(e.toString());
        }
    }
    
    
    void handleAnswerAndEnd(Call call) {
        log("+handleAnswerAndEnd");
        List<Call> list = mDualTalk.getAllNoIdleCalls();
        int size = list.size();
        try {
            if (call.getState().isAlive()) {
                Phone phone = call.getPhone();
                //call.hangup();
                
                if (call.getState() == Call.State.ACTIVE) {
                    log("+handleAnswerAndEnd: " + "hangup Call.State.ACTIVE");
                    if (phone instanceof SipPhone) {
                        call.hangup();
                    } else {
                        phone.hangupActiveCall();
                    }
                } else if (call.getState() == Call.State.HOLDING) {
                    log("+handleAnswerAndEnd: " + "hangup Call.State.HOLDING and switch H&A");
                    call.hangup();
                    phone.switchHoldingAndActive();
                }
            }
        } catch (Exception e) {
            log(e.toString());
        }
        
        Call ringCall = mDualTalk.getFirstActiveRingingCall();
        if (mDualTalk.hasActiveCdmaPhone() && (ringCall.getPhone().getPhoneType() != Phone.PHONE_TYPE_CDMA)) {
            if (DBG) {
                log("handleAnswerAndEnd: cdma phone has acttive call, don't switch it and answer the ringing only");
            }
            try {
                ringCall.getPhone().acceptCall();
            } catch (Exception e) {
                log(e.toString());
            }
        } else {
            PhoneUtils.answerCall(mDualTalk.getFirstActiveRingingCall());
        }
        
        log("-handleAnswerAndEnd");
    }
    
    void handleUnholdAndEnd(Call call) {
        log("+handleUnholdAndEnd");
        List<Call> list = mDualTalk.getAllNoIdleCalls();
        int size = list.size();
        try {
            if (call.getState().isAlive()) {
                Phone phone = call.getPhone();
                //call.hangup();
                
                if (call.getState() == Call.State.ACTIVE) {
                    log("+handleUnholdAndEnd: " + "hangup Call.State.ACTIVE");
                    if (phone instanceof SipPhone) {
                        call.hangup();
                    } else {
                        phone.hangupActiveCall();
                    }
                } else if (call.getState() == Call.State.HOLDING) {
                    log("+handleUnholdAndEnd: " + "hangup Call.State.HOLDING and switch H&A");
                    call.hangup();
                    phone.switchHoldingAndActive();
                }
            }
            
            mDualTalk.getSecondActiveBgCall().getPhone().switchHoldingAndActive();
            
        } catch (Exception e) {
            log(e.toString());
        }
        
        log("-handleUnholdAndEnd");
    }
    
    void handleHoldAndUnhold() {
        if (!DualTalkUtils.isSupportDualTalk()) {
            return ;
        }
        Call fgCall = mDualTalk.getActiveFgCall();
        Call bgCall = mDualTalk.getFirstActiveBgCall();
        try {
            if (fgCall.getState().isAlive()) {
                fgCall.getPhone().switchHoldingAndActive();
            } else if (bgCall.getState().isAlive()) {
                bgCall.getPhone().switchHoldingAndActive();
            }
        } catch (Exception e) {
            log("handleHoldAndUnhold: " + e.toString());
        }
    }
    
    /**
     *  we can go here, means both CDMA and GSM are active:
     *  1.cdma has one call, gsm has one call: switch between gsm and cdma phone
     *  2.cdma has one call, gsm has two call: switch between gsm's active and hold call
     *  3.cdma has two call, gsm has one call: switch gsm, and don't switch cdma phone, but switch the audio path
     */
    private void handleSwapCdmaAndGsm() {
       
        Call fgCall = mDualTalk.getActiveFgCall();
        Call bgCall = mDualTalk.getFirstActiveBgCall();
        
        int fgCallPhoneType = fgCall.getPhone().getPhoneType();
        int bgCallPhoneType = bgCall.getPhone().getPhoneType();
        
        if (DBG) {
            log("handleSwapCdmaAndGsm fgCall = " + fgCall.getConnections());
            log("handleSwapCdmaAndGsm bgCall = " + bgCall.getConnections());
        }
        
        //cdma has one call, gsm has two call: switch between gsm's active and hold call
        if (fgCallPhoneType == Phone.PHONE_TYPE_GSM
                && bgCallPhoneType == Phone.PHONE_TYPE_GSM) {
            log("handleSwapCdmaAndGsm: switch between two GSM calls.");
            try {
                fgCall.getPhone().switchHoldingAndActive();
            } catch (Exception e) {
                log(e.toString());
            }
            //Call CallManager's special api
        } else if (fgCallPhoneType == Phone.PHONE_TYPE_CDMA) {
            if (PhoneUtils.hasMultipleConnections(fgCall)) {
                log("handleSwapCdmaAndGsm: cdma has multiple calls and in foreground, only switch the audio.");
                //off cdma audio
                try {
                    bgCall.getPhone().switchHoldingAndActive();
                } catch (Exception e) {
                    log(e.toString());
                }
            } else {
                log("handleSwapCdmaAndGsm: cdma has single call and in foreground, switch by phone");
                try {
                    fgCall.getPhone().switchHoldingAndActive();
                    bgCall.getPhone().switchHoldingAndActive();
                } catch (Exception e) {
                    log(e.toString());
                }
            }
            
        } else if (fgCallPhoneType == Phone.PHONE_TYPE_GSM) {
            if (PhoneUtils.hasMultipleConnections(bgCall)) {
                log("handleSwapCdmaAndGsm: cdma has multiple calls and in background, only switch the audio");
                //on cdma audio
                try {
                    fgCall.getPhone().switchHoldingAndActive();
                } catch (Exception e) {
                    log(e.toString());
                }
            } else {
                log("handleSwapCdmaAndGsm: cdma has single call and in background, switch by phone");
                try {
                    fgCall.getPhone().switchHoldingAndActive();
                    bgCall.getPhone().switchHoldingAndActive();
                } catch (Exception e) {
                    log(e.toString());
                }
            }
        }
    }
    
    
    private boolean handleCallKeyForDualTalk() {
        if (mCM.getState() == PhoneConstants.State.RINGING) {
            //we assume that the callkey shouldn't be here when there is ringing call
            if (DBG) {
                log("handleCallKeyForDualTalk: rev call-key when ringing!");
            }
            return false;
        }
        
        return false;
    }
    

    private void selectWhichCallActive(final List<Call> list) {
        List<Call> holdList = new ArrayList<Call>();
        if (DBG) {
            log("+selectWhichCallActive");
        }
        for (Call call : list) {
            if (call.getState() == Call.State.HOLDING) {
                holdList.add(call);
            }
        }

        if (null == callSelectDialog || !callSelectDialog.isShowing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this).setNegativeButton(
                    getResources().getString(android.R.string.cancel),
                    (DialogInterface.OnClickListener) null);

            CallPickerAdapter callPickerAdapter = new CallPickerAdapter(this, holdList);
            if (2 == holdList.size()) {
                callPickerAdapter.setOperatorName(getOperatorNameByCall(holdList.get(0)), getOperatorNameByCall(holdList.get(1)));
                callPickerAdapter.setOperatorColor(getOperatorColorByCall(holdList.get(0)), getOperatorColorByCall(holdList.get(1)));
                callPickerAdapter.setCallerInfoName(getCallInfoName(1), getCallInfoName(2));

                builder.setSingleChoiceItems(callPickerAdapter, -1,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                final AlertDialog alert = (AlertDialog) dialog;
                                final ListAdapter listAdapter = alert.getListView().getAdapter();
                                Call call = (Call) listAdapter.getItem(which);
                                Call firstBgCall = mDualTalk.getFirstActiveBgCall();
                                Call secondBgCall = mDualTalk.getSecondActiveBgCall();
                                if (null != call && null != firstBgCall && null != secondBgCall) {
                                    Phone firstPhone = firstBgCall.getPhone();
                                    Phone secondPhone = secondBgCall.getPhone();
                                    if (DBG) {
                                        log("select call at phone :" + call.getPhone() + " firstPhone " + firstPhone
                                                + " secondPhone " + secondPhone);
                                    }

                                    if (call.getPhone() == firstPhone) {
                                        PhoneUtils.switchHoldingAndActive(call);
                                    } else {
                                        handleUnholdAndEnd(mDualTalk.getActiveFgCall());
                                    }
                                }
                                dialog.dismiss();
                            }
                        }).setTitle(getResources().getString(R.string.which_call_to_activate));
                callSelectDialog = builder.create();
                callSelectDialog.show();
            }
        }
        if (DBG) {
            log("-selectWhichCallActive");
        }
    }
    
    String getOperatorNameByCall(Call call) {
        if (call == null) {
            return null;
        }

        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            SIMInfo info = getSimInfoByCall(call);
            if (info != null && !TextUtils.isEmpty(info.mDisplayName)
                        && (call.getPhone().getPhoneType() != Phone.PHONE_TYPE_SIP)) {
                return info.mDisplayName;
            } else if (call.getPhone().getPhoneType() == Phone.PHONE_TYPE_SIP) {
                return getResources().getString(R.string.incall_call_type_label_sip);
            }
        } else {
            return SystemProperties.get(TelephonyProperties.PROPERTY_OPERATOR_ALPHA);
        }
        
        return null;
    }

    int getOperatorColorByCall(Call call) {
        if (call == null) {
            return -1;
        }

        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            SIMInfo info = getSimInfoByCall(call);
            if (info != null && !TextUtils.isEmpty(info.mDisplayName)
                        && (call.getPhone().getPhoneType() != Phone.PHONE_TYPE_SIP)) {
                return info.mColor;
            } 
        }

        return -1;
    }
    
    public String getCallInfoName(int position) {
  return "todo";
    }
    
    SIMInfo getSimInfoByCall(Call call) {
        //if (FeatureOption.MTK_GEMINI_SUPPORT) {
            if (call == null || call.getPhone() == null) {
                return null;
            }

            Phone phone = call.getPhone();
            // This an temp solution for VIA, the cdma is always in slot 2.
            if (phone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
                return SIMInfo.getSIMInfoBySlot(this,Phone.GEMINI_SIM_2);
            }

            String serialNumber = phone.getIccSerialNumber();
            SIMInfo info = SIMInfo.getSIMInfoBySlot(this,Phone.GEMINI_SIM_1);
            if (info != null && (info.mICCId != null) && (info.mICCId.equals(serialNumber))) {
                return info;
            }

            SIMInfo info2 = SIMInfo.getSIMInfoBySlot(this, Phone.GEMINI_SIM_2);
            if (info2 != null && (info2.mICCId != null) && (info2.mICCId.equals(serialNumber))) {
                return info2;
            }

            return null;
        /*} else {
            return null;
        }*/
    }

}
