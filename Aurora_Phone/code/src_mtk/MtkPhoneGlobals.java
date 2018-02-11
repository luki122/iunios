package com.android.phone;

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


import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.TelephonyCapabilities;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.cdma.TtyIntent;
//import com.android.phone.common.CallLogAsync;
import com.android.phone.CallLogAsync;
import com.android.phone.InCallScreen;

import com.android.phone.OtaUtils.CdmaOtaScreenState;
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
import com.android.phone.AuroraTelephony.SIMInfo;
import com.android.phone.AuroraTelephony.SimInfo;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms;
import android.provider.Telephony.MmsSms;
import android.database.ContentObserver;
import com.android.internal.telephony.test.SimulatedRadioControl;

import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.gemini.GeminiPhone;
import com.android.internal.telephony.gemini.GeminiNetworkSubUtil;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.TelephonyCapabilities;
import com.android.internal.telephony.TelephonyIntents;
import com.android.phone.OtaUtils.CdmaOtaScreenState;
import com.android.internal.telephony.cdma.TtyIntent;
import com.android.phone.OtaUtils.CdmaOtaScreenState;
import com.android.server.sip.SipService;
import android.os.Bundle;
import com.android.internal.telephony.gemini.*;
import android.os.AsyncResult;
import com.android.internal.telephony.IccCard;
import android.content.ComponentName;
import java.util.List;
import android.os.RemoteException;
import android.os.SystemService;
import com.mediatek.phone.PhoneFeatureConstants.FeatureOption;

//import com.mediatek.calloption.SimAssociateHandler;
import com.mediatek.phone.DualTalkUtils;

// ECC button should be hidden when there is no service.
import static android.provider.Telephony.Intents.SPN_STRINGS_UPDATED_ACTION;
import static android.provider.Telephony.Intents.EXTRA_PLMN;
import static android.provider.Telephony.Intents.EXTRA_SHOW_PLMN;
import android.view.WindowManager;
import android.util.DisplayMetrics;
import com.android.phone.AuroraCallLog.Calls;
import android.provider.Settings.SettingNotFoundException;
import com.mediatek.telephony.TelephonyManagerEx;


public class MtkPhoneGlobals extends PhoneGlobals {

    private static final int EVENT_SIM1_NETWORK_LOCKED = 19;
    private static final int EVENT_SIM2_NETWORK_LOCKED = 21;
    private static final int EVENT_TIMEOUT = 18;
    
    /// M: To trigger main thread looper for showing confirm dialog.
    private static final int EVENT_TRIGGER_MAINTHREAD_LOOPER = 31; 

  //Msg event for SIM Lock
    private static final int SIM1QUERY = 120;
    private static final int SIM2QUERY = 122; 
    
    public static final int MMI_INITIATE2 = 54; 
    public static final int MMI_COMPLETE2 = 55;
    public static final int MMI_CANCEL2 = 56;
    public static final int EVENT_SHOW_INCALL_SCREEN_FOR_STK_SETUP_CALL = 57;
    public static final int DELAY_SHOW_INCALL_SCREEN_FOR_STK_SETUP_CALL = 160;
    
    public static final String OLD_NETWORK_MODE = "com.android.phone.OLD_NETWORK_MODE";
    public static final String NETWORK_MODE_CHANGE = "com.android.phone.NETWORK_MODE_CHANGE";
    public static final String NETWORK_MODE_CHANGE_RESPONSE = "com.android.phone.NETWORK_MODE_CHANGE_RESPONSE";
    public static final int MESSAGE_SET_PREFERRED_NETWORK_TYPE = 10011;
    private static final String STKCALL_REGISTER_SPEECH_INFO = "com.android.stk.STKCALL_REGISTER_SPEECH_INFO";
    public static final String MISSEDCALL_DELETE_INTENT = "com.android.phone.MISSEDCALL_DELETE_INTENT";

    public static final boolean sGemini = FeatureOption.MTK_GEMINI_SUPPORT;
    public static final boolean sVideoCallSupport = true;

    private static final String ACTION_MODEM_STATE = "com.mtk.ACTION_MODEM_STATE";
    private static final int CCCI_MD_BROADCAST_EXCEPTION = 1;
    private static final int CCCI_MD_BROADCAST_RESET = 2;
    private static final int CCCI_MD_BROADCAST_READY = 3;
    
    /**
     * Allowable values for the poke lock code (timeout between a user activity and the
     * going to sleep), please refer to {@link com.android.server.PowerManagerService}
     * for additional reference.
     *   SHORT uses the short delay for the timeout (SHORT_KEYLIGHT_DELAY, 6 sec)
     *   MEDIUM uses the medium delay for the timeout (MEDIUM_KEYLIGHT_DELAY, 15 sec)
     *   DEFAULT is the system-wide default delay for the timeout (1 min)
     */
    public enum ScreenTimeoutDuration {
        SHORT,
        MEDIUM,
        DEFAULT
    }
    
    public BluetoothHandsfree mBtHandsfree;
    public MTKCallManager mCMGemini;
    
    
    private PowerManager.WakeLock mWakeLockForDisconnect;
    
    /**
     * Timeout setting used by PokeLock.
     *
     * This variable won't be effective when proximity sensor is available in the device.
     *
     * @see ScreenTimeoutDuration
     */
    private ScreenTimeoutDuration mScreenTimeoutDuration = ScreenTimeoutDuration.DEFAULT;
    /**
     * Used to set/unset {@link LocalPowerManager#POKE_LOCK_IGNORE_TOUCH_EVENTS} toward PokeLock.
     *
     * This variable won't be effective when proximity sensor is available in the device.
     */
    private boolean mIgnoreTouchUserActivity = false;
    private final IBinder mPokeLockToken = new Binder();
    
    private int mWakelockSequence = 0; 
    
 // ECC button should be hidden when there is no service.
    private boolean mIsNoService[] = {true, true};
    
    AudioManager mAudioManager = null;
    
    public boolean isEnableTTY() {
        return mTtyEnabled;
    }
    
    public int ihandledEventSIM2SIMLocked = 0;//whether handled EVENT_SIM2_NETWORK_LOCKED message, 0--not handled,1--already handled
    public int ihandledEventSIM1SIMLocked = 0;//whether handled EVENT_SIM1_NETWORK_LOCKED message, 0--not handled,1--already handled

    public static int[] arySIMLockStatus = {3,3}; //the SIM Lock deal with statu
    
    // Broadcast receiver for various intent broadcasts (see onCreate())
    private BroadcastReceiver mReceiver = new PhoneAppBroadcastReceiver();

    // Broadcast receiver purely for ACTION_MEDIA_BUTTON broadcasts
    private BroadcastReceiver mMediaButtonReceiver = new MediaButtonBroadcastReceiver();
    
    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            PhoneConstants.State phoneState;
            switch (msg.what) {
                // Starts the SIP service. It's a no-op if SIP API is not supported
                // on the deivce.
                // TODO: Having the phone process host the SIP service is only
                // temporary. Will move it to a persistent communication process
                // later.
                case EVENT_START_SIP_SERVICE:
                    SipService.start(getApplicationContext());
                    break;

                // TODO: This event should be handled by the lock screen, just
                // like the "SIM missing" and "Sim locked" cases (bug 1804111).
                case EVENT_SIM_NETWORK_LOCKED:
//                    if (getResources().getBoolean(R.bool.ignore_sim_network_locked_events)) {
//                        // Some products don't have the concept of a "SIM network lock"
//                        Log.i(LOG_TAG, "Ignoring EVENT_SIM_NETWORK_LOCKED event; "
//                              + "not showing 'SIM network unlock' PIN entry screen");
//                    } else {
//                        // Normal case: show the "SIM network unlock" PIN entry screen.
//                        // The user won't be able to do anything else until
//                        // they enter a valid SIM network PIN.
//                        Log.i(LOG_TAG, "show sim depersonal panel");
//                        IccNetworkDepersonalizationPanel ndpPanel =
//                                new IccNetworkDepersonalizationPanel(PhoneApp.getInstance());
//                        ndpPanel.show();
//                    }
                    Log.d(LOG_TAG, "handle EVENT_SIM_NETWORK_LOCKED +");
                    Intent intent3 = new Intent(getInstance(), PowerOnSetupUnlockSIMLock.class);  
                    intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent3); 
                    Log.d(LOG_TAG, "handle EVENT_SIM_NETWORK_LOCKED -");
                    break;
                 case EVENT_SIM1_NETWORK_LOCKED: //SIM 1 Network locked 
//                    try{
//                        Thread.sleep(1000);
//                    }catch(InterruptedException er){
//                        er.printStackTrace();
//                    }
//                    if(bNeedUnlockSIMLock(Phone.GEMINI_SIM_2) == false){//wait for SIM2 call PowerOnSetupUnlockSIMLock
//                        
//                    }else{
//                        ihandledEventSIM1SIMLocked = 1;//deal with EVENT_SIM2_NETWORK_LOCKED
                    Log.d(LOG_TAG, "[Received][EVENT_SIM1_NETWORK_LOCKED]");    
//                    if (arySIMLockStatus[0] == 0)//not deal with EVENT_SIM1_NETWORK_LOCKED
//                        {
                            Log.d(LOG_TAG, "handle EVENT_SIM1_NETWORK_LOCKED +");
                            Intent intent = new Intent(getInstance(), PowerOnSetupUnlockSIMLock.class);  
                            Bundle bundle = new Bundle();
                            bundle.putInt("Phone.GEMINI_SIM_ID_KEY",0);//To unlock which card  default:-1, Slot1: 0, Slot2:1
                            intent.putExtras(bundle);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent); 
                            Log.d(LOG_TAG, "handle EVENT_SIM1_NETWORK_LOCKED -");
//                        }
//                    }


                  break;    
                case EVENT_SIM2_NETWORK_LOCKED: //SIM 2 Network locked
//                    try{
//                        Thread.sleep(1000);
//                    }catch(InterruptedException er){
//                        er.printStackTrace();
//                    }
//                    if(bNeedUnlockSIMLock(Phone.GEMINI_SIM_1) == false){//wait for SIM2 call PowerOnSetupUnlockSIMLock
//                        
//                    }else{
//                        ihandledEventSIM2SIMLocked = 1;//deal with EVENT_SIM2_NETWORK_LOCKED
                    Log.d(LOG_TAG, "[Received][EVENT_SIM2_NETWORK_LOCKED]");        
//                    if (arySIMLockStatus[1] == 0)//not deal with EVENT_SIM1_NETWORK_LOCKED
//                        {
                            Log.d(LOG_TAG, "handle EVENT_SIM2_NETWORK_LOCKED +");
                            
                            Intent intent2 = new Intent(getInstance(), PowerOnSetupUnlockSIMLock.class); 
                            Bundle bundle2 = new Bundle();
                            bundle2.putInt("Phone.GEMINI_SIM_ID_KEY",1);//To unlock which card  default:-1, Slot1: 0, Slot2:1
                            intent2.putExtras(bundle2);
                            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent2);
                            Log.d(LOG_TAG, "handle EVENT_SIM2_NETWORK_LOCKED -");
//                        }
//
//                    }


                    break;    
                case EVENT_UPDATE_INCALL_NOTIFICATION:
                    // Tell the NotificationMgr to update the "ongoing
                    // call" icon in the status bar, if necessary.
                    // Currently, this is triggered by a bluetooth headset
                    // state change (since the status bar icon needs to
                    // turn blue when bluetooth is active.)
                    if (DBG) Log.d (LOG_TAG, "- updating in-call notification from handler...");
                    notificationMgr.updateInCallNotification();
                    break;

                case EVENT_DATA_ROAMING_DISCONNECTED:
                    ((MtkNotificationMgr)notificationMgr).showDataDisconnectedRoaming(msg.arg1);
                    break;

                case EVENT_DATA_ROAMING_OK:
                    notificationMgr.hideDataDisconnectedRoaming();
                    break;
                    
                case MMI_INITIATE:
                    if (mInCallScreen == null) {
                        inCallUiState.setPendingUssdMessage(
                                Message.obtain(mHandler, Phone.GEMINI_SIM_1, (AsyncResult) msg.obj));
                        //mInCallScreen.onMMIInitiate((AsyncResult) msg.obj, Phone.GEMINI_SIM_1);
                    }
                    break;
                    
                case MMI_INITIATE2:
                    if (mInCallScreen == null) {
                        inCallUiState.setPendingUssdMessage(
                                Message.obtain(mHandler, Phone.GEMINI_SIM_1, (AsyncResult) msg.obj));
                        //mInCallScreen.onMMIInitiate((AsyncResult) msg.obj, Phone.GEMINI_SIM_2);
                    }
                    break;

                case MMI_COMPLETE:
                    inCallUiState.setPendingUssdMessage(null);
                    onMMIComplete((AsyncResult) msg.obj);
                    break;

                case MMI_COMPLETE2:
                    inCallUiState.setPendingUssdMessage(null);
                    onMMIComplete2((AsyncResult) msg.obj);
                    break;

                case MMI_CANCEL:
                    PhoneUtils.cancelMmiCodeExt(phone, Phone.GEMINI_SIM_1);
                    break;

                case MMI_CANCEL2:                    
                    PhoneUtils.cancelMmiCodeExt(phone, Phone.GEMINI_SIM_2);
                    break;        

                case EVENT_WIRED_HEADSET_PLUG:
                    // Since the presence of a wired headset or bluetooth affects the
                    // speakerphone, update the "speaker" state.  We ONLY want to do
                    // this on the wired headset connect / disconnect events for now
                    // though, so we're only triggering on EVENT_WIRED_HEADSET_PLUG.

                    phoneState = mCM.getState();
                    // Do not change speaker state if phone is not off hook
                    if (phoneState == PhoneConstants.State.OFFHOOK) {
                        if (!isShowingCallScreen() &&
                            (mBtHandsfree == null || !mBtHandsfree.isAudioOn())) {
                            if (!isHeadsetPlugged()) {
                                // if the state is "not connected", restore the speaker state.
                                PhoneUtils.restoreSpeakerMode(getApplicationContext());
                            } else {
                                // if the state is "connected", force the speaker off without
                                // storing the state.
                                PhoneUtils.turnOnSpeaker(getApplicationContext(), false, false);
                            }
                        }
                    }
                    // Update the Proximity sensor based on headset state
                    updateProximitySensorMode(phoneState);

                    // Force TTY state update according to new headset state
                    if (mTtyEnabled) {
                        sendMessage(obtainMessage(EVENT_TTY_PREFERRED_MODE_CHANGED, 0));
                    }
                    break;

                case EVENT_SIM_STATE_CHANGED:
                    // Marks the event where the SIM goes into ready state.
                    // Right now, this is only used for the PUK-unlocking
                    // process.
                    if (msg.obj.equals(IccCard.INTENT_VALUE_ICC_READY)) {
                        // when the right event is triggered and there
                        // are UI objects in the foreground, we close
                        // them to display the lock panel.
                        if (mPUKEntryActivity != null) {
                            mPUKEntryActivity.finish();
                            mPUKEntryActivity = null;
                        }
                        if (mPUKEntryProgressDialog != null) {
                            mPUKEntryProgressDialog.dismiss();
                            mPUKEntryProgressDialog = null;
                        }
                    }
                    break;

                case EVENT_UNSOL_CDMA_INFO_RECORD:
                    //TODO: handle message here;
                    break;

                case EVENT_DOCK_STATE_CHANGED:
                    // If the phone is docked/undocked during a call, and no wired or BT headset
                    // is connected: turn on/off the speaker accordingly.
                    boolean inDockMode = false;
                    if (mDockState != Intent.EXTRA_DOCK_STATE_UNDOCKED) {
                        inDockMode = true;
                    }
                    if (VDBG) Log.d(LOG_TAG, "received EVENT_DOCK_STATE_CHANGED. Phone inDock = "
                            + inDockMode);

                    phoneState = mCM.getState();
                    if (phoneState == PhoneConstants.State.OFFHOOK &&
                            !isHeadsetPlugged() &&
                            !(mBtHandsfree != null && mBtHandsfree.isAudioOn())) {
                        PhoneUtils.turnOnSpeaker(getApplicationContext(), inDockMode, true);
                        updateInCallScreen();  // Has no effect if the InCallScreen isn't visible
                    }
                    break;

                case EVENT_TTY_PREFERRED_MODE_CHANGED:
                    // TTY mode is only applied if a headset is connected
                    int ttyMode;
                    if (isHeadsetPlugged()) {
                        ttyMode = mPreferredTtyMode;
                    } else {
                        ttyMode = Phone.TTY_MODE_OFF;
                    }
                    if (FeatureOption.MTK_GEMINI_SUPPORT) {
                        ((GeminiPhone)phone).setTTYModeGemini(convertTTYmodeToRadio(ttyMode), 
                                mHandler.obtainMessage(EVENT_TTY_MODE_SET), Phone.GEMINI_SIM_1);
                        ((GeminiPhone)phone).setTTYModeGemini(convertTTYmodeToRadio(ttyMode), 
                                mHandler.obtainMessage(EVENT_TTY_MODE_SET), Phone.GEMINI_SIM_2);
                    } else {
                        phone.setTTYMode(convertTTYmodeToRadio(ttyMode), mHandler.obtainMessage(EVENT_TTY_MODE_SET));
                    }
                    break;

                case EVENT_TTY_MODE_GET:
                    handleQueryTTYModeResponse(msg);
                    break;

                case EVENT_TTY_MODE_SET:
                    handleSetTTYModeResponse(msg);
                    break;

                case EVENT_TIMEOUT:
                    handleTimeout(msg.arg1);
                    break;
                    
                case MESSAGE_SET_PREFERRED_NETWORK_TYPE:
                    AsyncResult ar = (AsyncResult) msg.obj;
                    Intent it = new Intent(NETWORK_MODE_CHANGE_RESPONSE);
                    if (ar.exception == null) {
                        it.putExtra(NETWORK_MODE_CHANGE_RESPONSE, true);
                        it.putExtra("NEW_NETWORK_MODE", msg.arg2);
                    } else {
                        it.putExtra(NETWORK_MODE_CHANGE_RESPONSE, false);
                        it.putExtra(OLD_NETWORK_MODE, msg.arg1);
                    }
                    sendBroadcast(it);
                    break;
                                      

                case EVENT_SHOW_INCALL_SCREEN_FOR_STK_SETUP_CALL:
                    PhoneUtils.showIncomingCallUi();
                    break;
                
                /// M: To trigger main thread looper for showing confirm dialog.	
                case EVENT_TRIGGER_MAINTHREAD_LOOPER:
                  Log.d(LOG_TAG, "handle EVENT_TRIGGER_MAINTHREAD_LOOPER");
                  break;
            }
        }
    
    };
    
    MtkPhoneGlobals(Context context) {
        super(context);
    }
    
    @Override
    public void onCreate() {

        if (VDBG) Log.v(LOG_TAG, "onCreate()...");

        String state = SystemProperties.get("vold.decrypt");

        if (!SystemProperties.getBoolean("gsm.phone.created", false) && ("".equals(state) || "trigger_restart_framework".equals(state))) {
            Log.d(LOG_TAG, "set System Property gsm.phone.created = true");
            SystemProperties.set("gsm.phone.created", "true");
            Settings.System.putLong(getApplicationContext().getContentResolver(),
                    Settings.System.SIM_LOCK_STATE_SETTING, 0x0L);
        }

        ContentResolver resolver = getContentResolver();

        // Cache the "voice capable" flag.
        // This flag currently comes from a resource (which is
        // overrideable on a per-product basis):
        sVoiceCapable =
                getResources().getBoolean(com.android.internal.R.bool.config_voice_capable);
        // ...but this might eventually become a PackageManager "system
        // feature" instead, in which case we'd do something like:
        // sVoiceCapable =
        //   getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY_VOICE_CALLS);

        if (phone == null) {
            Log.v(LOG_TAG, "onCreate(), start to make default phone");    
            // Initialize the telephony framework
            PhoneFactory.makeDefaultPhones(this);
            Log.v(LOG_TAG, "onCreate(), make default phone complete");
            // Get the default phone
            phone = PhoneFactory.getDefaultPhone();

            // Start TelephonyDebugService After the default phone is created.
            Intent intent = new Intent(this, TelephonyDebugService.class);
            startService(intent);

            mCM = CallManager.getInstance();
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                mCMGemini = MTKCallManager.getInstance();
                mCMGemini.registerPhoneGemini(phone); 
            } else {
                mCM.registerPhone(phone);
            }

            // Create the NotificationMgr singleton, which is used to display
            // status bar icons and control other status bar behavior.
            notificationMgr = MtkNotificationMgr.init(this);

            Log.v(LOG_TAG, "onCreate(), start to new phone interface");

            phoneMgr = PhoneInterfaceManager.init(this, phone);
            
            if(DeviceUtils.isUseAuroraPhoneService()) {
            	Intent phoneserviceintent = new Intent("com.aurora.phone.PHONE_SERVICE_SET_UP");
            	sendBroadcast(phoneserviceintent);
            }
            

            mHandler.sendEmptyMessage(EVENT_START_SIP_SERVICE);

            int phoneType = phone.getPhoneType();

            if (phoneType == Phone.PHONE_TYPE_CDMA) {
                // Create an instance of CdmaPhoneCallState and initialize it to IDLE
                cdmaPhoneCallState = new CdmaPhoneCallState();
                cdmaPhoneCallState.CdmaPhoneCallStateInit();
            }

            Log.v(LOG_TAG, "onCreate(), start to get BT default adapter");            

            if (BluetoothAdapter.getDefaultAdapter() != null) {
                // Start BluetoothHandsree even if device is not voice capable.
                // The device can still support VOIP.
                mBtHandsfree = BluetoothHandsfree.init(this, mCM);
                startService(new Intent(this, BluetoothHeadsetService.class));
            } else {
                // Device is not bluetooth capable
                mBtHandsfree = null;
            }

            ringer = Ringer.init(this);

            // before registering for phone state changes
            mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK
                    | PowerManager.ACQUIRE_CAUSES_WAKEUP
                    | PowerManager.ON_AFTER_RELEASE, LOG_TAG);

            mWakeLockForDisconnect = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK
                    | PowerManager.ACQUIRE_CAUSES_WAKEUP
                    | PowerManager.ON_AFTER_RELEASE, LOG_TAG);
   
            Log.v(LOG_TAG, "onCreate(), new partial wakelock");            

            // lock used to keep the processor awake, when we don't care for the display.
            mPartialWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                    | PowerManager.ON_AFTER_RELEASE, LOG_TAG);
            // Wake lock used to control proximity sensor behavior.
            if (mPowerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
                mProximityWakeLock = mPowerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, LOG_TAG);
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

            // Get UpdateLock to suppress system-update related events (e.g. dialog show-up)
            // during phone calls.
            mUpdateLock = new UpdateLock("phone");

            if (DBG) Log.d(LOG_TAG, "onCreate: mUpdateLock: " + mUpdateLock);

            // Create the CallController singleton, which is the interface
            // to the telephony layer for user-initiated telephony functionality
            // (like making outgoing calls.)
            CallLogger callLogger = new CallLogger(this, new CallLogAsync());
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
            Log.v(LOG_TAG, "onCreate(), new callnotifier");
//            notifier = CallNotifier.init(this, phone, ringer, mBtHandsfree, new CallLogAsync());
            notifier = MtkCallNotifier.init(this, phone, ringer, mBtHandsfree, callLogger);

            // register for ICC status
            /*IccCard sim = phone.getIccCard();
            if (sim != null) {
                if (VDBG) Log.v(LOG_TAG, "register for ICC status");
                sim.registerForNetworkLocked(mHandler, EVENT_SIM_NETWORK_LOCKED, null);
            }*/

            // register for MMI/USSD
            if (phoneType == Phone.PHONE_TYPE_GSM) {
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    mCMGemini.registerForMmiCompleteGemini(mHandler, MMI_COMPLETE, null, Phone.GEMINI_SIM_1);                
                    mCMGemini.registerForMmiCompleteGemini(mHandler, MMI_COMPLETE2, null, Phone.GEMINI_SIM_2);  
                    mCMGemini.registerForMmiInitiateGemini(mHandler, MMI_INITIATE, null, Phone.GEMINI_SIM_1);
                    mCMGemini.registerForMmiInitiateGemini(mHandler, MMI_INITIATE2, null, Phone.GEMINI_SIM_2);
                } else {
                    mCM.registerForMmiComplete(mHandler, MMI_INITIATE, null);
                    mCM.registerForMmiComplete(mHandler, MMI_COMPLETE, null);
                }
            }
            
            if (FeatureOption.EVDO_DT_SUPPORT) {
                mCM.registerForMmiComplete(mHandler, MMI_INITIATE, null);
                mCM.registerForMmiComplete(mHandler, MMI_COMPLETE, null);
            }

            Log.v(LOG_TAG, "onCreate(), initialize connection handler");

            // register connection tracking to PhoneUtils
            PhoneUtils.initializeConnectionHandler(mCM);

            // Read platform settings for TTY feature
            if (PhoneUtils.isSupportFeature("TTY")) {
                mTtyEnabled = getResources().getBoolean(R.bool.tty_enabled);
            } else {
                mTtyEnabled = false;
            }

            Log.v(LOG_TAG, "onCreate(), new intentfilter");

            // Register for misc other intent broadcasts.
            IntentFilter intentFilter =
                    new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intentFilter.addAction(Intent.ACTION_DUAL_SIM_MODE_CHANGED);
            intentFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
            intentFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
//            intentFilter.addAction(BluetoothHeadset.ACTION_STATE_CHANGED);
            intentFilter.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
            intentFilter.addAction(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED);
            intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
            intentFilter.addAction(Intent.ACTION_DOCK_EVENT);
            intentFilter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
            intentFilter.addAction(TelephonyIntents.ACTION_RADIO_TECHNOLOGY_CHANGED);
            intentFilter.addAction(TelephonyIntents.ACTION_SERVICE_STATE_CHANGED);
            intentFilter.addAction(TelephonyIntents.ACTION_EMERGENCY_CALLBACK_MODE_CHANGED);
            if (mTtyEnabled) {
                intentFilter.addAction(TtyIntent.TTY_PREFERRED_MODE_CHANGE_ACTION);
            }
            intentFilter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
            intentFilter.addAction(Intent.ACTION_SHUTDOWN);
            intentFilter.addAction(STKCALL_REGISTER_SPEECH_INFO);
            intentFilter.addAction(MISSEDCALL_DELETE_INTENT);
            intentFilter.addAction("out_going_call_to_phone_app");
            //Handle the network mode change for enhancement
            intentFilter.addAction(NETWORK_MODE_CHANGE);
            intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN_IPO");
            intentFilter.addAction("android.intent.action.ACTION_PREBOOT_IPO");
            intentFilter.addAction(GeminiPhone.EVENT_3G_SWITCH_START_MD_RESET);
            intentFilter.addAction(TelephonyIntents.ACTION_RADIO_OFF);
            intentFilter.addAction(ACTION_MODEM_STATE);
            /// M: To trigger main thread looper for showing confirm dialog.
            intentFilter.addAction("TRIGGER_MAINTHREAD_LOOPER"); 

            // ECC button should be hidden when there is no service.
            intentFilter.addAction(SPN_STRINGS_UPDATED_ACTION);

            mReceiver = new MtkPhoneAppBroadcastReceiver();
            mMediaButtonReceiver = new MtkMediaButtonBroadcastReceiver();
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
        if (mTtyEnabled) {
            mPreferredTtyMode = android.provider.Settings.Secure.getInt(
                    phone.getContext().getContentResolver(),
                    android.provider.Settings.Secure.PREFERRED_TTY_MODE,
                    Phone.TTY_MODE_OFF);
            mHandler.sendMessage(mHandler.obtainMessage(EVENT_TTY_PREFERRED_MODE_CHANGED, 0));
        }
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

        /**
         * Change Feature by mediatek .inc
         * description : initilize SimAssociateHandler
         */
//        SimAssociateHandler.getInstance(this).prepair();
//        SimAssociateHandler.getInstance(this).load();
        /**
         * Change Feature by mediatek .inc end
         */

        /**
         * Change Feature by mediatek .inc
         * description : set the global flag that support dualtalk
         */
        if (FeatureOption.MTK_DT_SUPPORT) {
            DualTalkUtils.init();
        }
        /**
         * Change Feature by mediatek .inc end
         */

        // init SimInfoWrapper
//        SIMInfoWrapper.getDefault().init(this);

        // init CallHistory
//        CallHistoryDatabaseHelper.getInstance(this).initDatabase();

        // init PhoneNumberUtil
        PhoneNumberUtil.getInstance();

        
        initAuroraObjects();

        Log.v(LOG_TAG, "onCreate(), exit.");
   
    }
    
    protected void initAuroraObjects() {
//        mSubscriptionManager = SubscriptionManager.getInstance();        
//        mCardSubMgr = CardSubscriptionManager.getInstance();
//        int mNumPhones = MSimTelephonyManager.getDefault().getPhoneCount();
//        for (int i=0; i < mNumPhones; i++) {
//            mCardSubMgr.registerForCardInfoAvailable(i, mCardHandler, EVENT_CARD_INFO_AVAILABLE, new Integer(i));
//            mCardSubMgr.registerForCardInfoUnavailable(i, mCardHandler, EVENT_CARD_INFO_NOT_AVAILABLE, new Integer(i));
//            mSubscriptionManager.registerForSubscriptionActivated(i, mCardHandler, EVENT_SUBSCRIPTION_ACTIVATED, new Integer(i));
//            mSubscriptionManager.registerForSubscriptionDeactivated(i, mCardHandler, EVENT_SUBSCRIPTION_DEACTIVATED, new Integer(i));
//        }
        
        
        SharedPreferences sp = getSharedPreferences("com.android.phone_preferences", Context.MODE_PRIVATE);  
        Boolean init = sp.getBoolean("aurora_phone_init", false);	            
   	    if(!init) { 
            try {
//            	Settings.Global.putInt(getContentResolver(),
//                        Settings.Global.MULTI_SIM_DATA_CALL_SUBSCRIPTION, 0);
//                android.provider.Settings.Global.putInt(getContentResolver(),
//                        android.provider.Settings.Global.MOBILE_DATA + "0", 0);
//                android.provider.Settings.Global.putInt(getContentResolver(),
//                        android.provider.Settings.Global.DATA_ROAMING + "0", 0);
//                android.provider.Settings.Global.putInt(getContentResolver(),
//                        android.provider.Settings.Global.MOBILE_DATA + "1", 0);
//                android.provider.Settings.Global.putInt(getContentResolver(),
//                        android.provider.Settings.Global.DATA_ROAMING + "1", 0);
//	            android.telephony.MSimTelephonyManager.putIntAtIndex(
//	                    getContentResolver(),
//	                    android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
//	                    1, Constants.NETWORK_MODE_GSM_ONLY);
//	            getPhone(1).setPreferredNetworkType(Constants.NETWORK_MODE_GSM_ONLY, null);
//    	        MSimTelephonyManager.putIntAtIndex(
//                        getContentResolver(),
//                        android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
//                        0, Constants.NETWORK_MODE_TD_SCDMA_GSM_WCDMA_LTE);
//                getPhone(0).setPreferredNetworkType(Constants.NETWORK_MODE_TD_SCDMA_GSM_WCDMA_LTE, null);
//                
//                phone.setPrioritySub(0, null);                
//                phone.setDefaultVoiceSub(0, null);
//                MSimPhoneFactory.setSMSSubscription(0);
//                MSimPhoneFactory.setPromptEnabled(false);
//	            MSimPhoneFactory.setVoiceSubscription(0);
//                MSimPhoneFactory.setPrioritySubscription(0);  
//                MSimPhoneFactory.setDataSubscription(0);  
//                MSimPhoneFactory.setDefaultDataSubscription(0);  
            } catch (Exception e) {
            	e.printStackTrace();
            }                   
   	    }   	           	              

        super.initAuroraObjects();
    }
    
    public Phone getPhone(int subscription) {
    	if(sGemini) {
    		return ((GeminiPhone)phone).getPhonebyId(subscription);
    	} else {
    		return phone;
    	}
    }
	
    BluetoothHandsfree getBluetoothHandsfree() {
        return mBtHandsfree;
    }
    
    protected static String getCallScreenClassName() {
        return MtkInCallScreen.class.getName();
    }
    
    void clearInCallScreenInstance(InCallScreen inCallScreen) {
        if (DBG) Log.d(LOG_TAG, "clearInCallScreenInstance(), inCallScreen = " + inCallScreen);
        // Here we need judge whether mInCallScreen is same as
        // inCallScreen because there may be 2 InCallScreen instance
        // exiting in some case even if InCallScreen activity is single instance.
        // if mInCallScreen != inCallScreen, that means another InCallScreen
        // is active, no need set mInCallScreen as null
        if (mInCallScreen == inCallScreen) {
            if (DBG) Log.d(LOG_TAG, "same InCallScreen instance");
            mInCallScreen = null;
        }
    }

    public InCallScreen getInCallScreenInstance() {
        return mInCallScreen;
    }
    
    
    
    void wakeUpScreenForDisconnect(int holdMs) {
        synchronized (this) {
            if (mWakeState == WakeState.SLEEP && !mPowerManager.isScreenOn()) { 
                if (DBG) Log.d(LOG_TAG, "wakeUpScreenForDisconnect(" + holdMs + ")");
                mWakeLockForDisconnect.acquire();
                mHandler.removeMessages(EVENT_TIMEOUT);
                mWakelockSequence++;
                Message msg = mHandler.obtainMessage(EVENT_TIMEOUT, mWakelockSequence, 0);
                mHandler.sendMessageDelayed(msg, holdMs);
            }
        }
    }
    
    void handleTimeout(int seq) {
        synchronized (this) {
            if (DBG) Log.d(LOG_TAG, "handleTimeout");
            if (seq == mWakelockSequence) {
                mWakeLockForDisconnect.release();
            }
        }
    }
    

    
    
    /**
     * Sets the wake state and screen timeout based on the current state
     * of the phone, and the current state of the in-call UI.
     *
     * This method is a "UI Policy" wrapper around
     * {@link PhoneApp#requestWakeState} and {@link PhoneApp#setScreenTimeout}.
     *
     * It's safe to call this method regardless of the state of the Phone
     * (e.g. whether or not it's idle), and regardless of the state of the
     * Phone UI (e.g. whether or not the InCallScreen is active.)
     */
    /* package */ void updateWakeState() {
        PhoneConstants.State state = mCM.getState();

        // True if the in-call UI is the foreground activity.
        // (Note this will be false if the screen is currently off,
        // since in that case *no* activity is in the foreground.)
        boolean isShowingCallScreen = isShowingCallScreen();

        // True if the InCallScreen's DTMF dialer is currently opened.
        // (Note this does NOT imply whether or not the InCallScreen
        // itself is visible.)
        boolean isDialerOpened = (mInCallScreen != null) && mInCallScreen.isDialerOpened();

        // True if the speakerphone is in use.  (If so, we *always* use
        // the default timeout.  Since the user is obviously not holding
        // the phone up to his/her face, we don't need to worry about
        // false touches, and thus don't need to turn the screen off so
        // aggressively.)
        // Note that we need to make a fresh call to this method any
        // time the speaker state changes.  (That happens in
        // PhoneUtils.turnOnSpeaker().)
        boolean isSpeakerInUse = (state == PhoneConstants.State.OFFHOOK) && PhoneUtils.isSpeakerOn(this);

        // TODO (bug 1440854): The screen timeout *might* also need to
        // depend on the bluetooth state, but this isn't as clear-cut as
        // the speaker state (since while using BT it's common for the
        // user to put the phone straight into a pocket, in which case the
        // timeout should probably still be short.)

        if (DBG) Log.d(LOG_TAG, "updateWakeState: callscreen " + isShowingCallScreen
                       + ", dialer " + isDialerOpened
                       + ", speaker " + isSpeakerInUse + "...");

        //
        // (2) Decide whether to force the screen on or not.
        //
        // Force the screen to be on if the phone is ringing or dialing,
        // or if we're displaying the "Call ended" UI for a connection in
        // the "disconnected" state.
        //
        boolean isRinging = (state == PhoneConstants.State.RINGING);
        boolean isDialing;
        if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
            isDialing = (((GeminiPhone)phone).getForegroundCall().getState() == Call.State.DIALING || ((GeminiPhone)phone).getForegroundCall().getState() == Call.State.ALERTING);
        } else {
            isDialing = (phone.getForegroundCall().getState() == Call.State.DIALING || phone.getForegroundCall().getState() == Call.State.ALERTING);
        }
        boolean showingDisconnectedConnection =
                PhoneUtils.hasDisconnectedConnections(mCM) && isShowingCallScreen;
        boolean keepScreenOn = isRinging || isDialing || showingDisconnectedConnection;
        if (DBG) Log.d(LOG_TAG, "updateWakeState: keepScreenOn = " + keepScreenOn
                       + " (isRinging " + isRinging
                       + ", isDialing " + isDialing
                       + ", showingDisc " + showingDisconnectedConnection + ")");
        // keepScreenOn == true means we'll hold a full wake lock:
        requestWakeState(keepScreenOn ? WakeState.FULL : WakeState.SLEEP);
    }
    
      
    
    /**
     * @return true if this device supports the "proximity sensor
     * auto-lock" feature while in-call (see updateProximitySensorMode()).
     */
    /* package */ boolean proximitySensorModeEnabled() {
        return (mProximityWakeLock != null);
    }
    
    protected void onMMIComplete(AsyncResult r) {
        if (VDBG) Log.d(LOG_TAG, "onMMIComplete()...");
        MmiCode mmiCode = (MmiCode) r.result;
        MmiCode.State state = mmiCode.getState();
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            if (state != MmiCode.State.PENDING) {
            Intent intent = new Intent();
             intent.setAction("com.android.phone.mmi");
             sendBroadcast(intent);
            }
        }
        PhoneUtils.displayMMIComplete(phone, getInstance(), mmiCode, null, null);
    }

    private void onMMIComplete2(AsyncResult r) {
        if (VDBG) Log.d(LOG_TAG, "onMMIComplete2()...");
        MmiCode mmiCode = (MmiCode) r.result;
        MmiCode.State state = mmiCode.getState();
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            if (state != MmiCode.State.PENDING) {
            Intent intent = new Intent();
             intent.setAction("com.android.phone.mmi");
             sendBroadcast(intent);
            }
        }
        PhoneUtils.displayMMICompleteExt(phone, getInstance(), mmiCode, null, null, Phone.GEMINI_SIM_2);
    }
    
    protected void initForNewRadioTechnology() {
        if (DBG) Log.d(LOG_TAG, "initForNewRadioTechnology...");

         if (phone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
            // Create an instance of CdmaPhoneCallState and initialize it to IDLE
            cdmaPhoneCallState = new CdmaPhoneCallState();
            cdmaPhoneCallState.CdmaPhoneCallStateInit();
        }
        if (TelephonyCapabilities.supportsOtasp(phone)) {
            //create instances of CDMA OTA data classes
            if (cdmaOtaProvisionData == null) {
                cdmaOtaProvisionData = new OtaUtils.CdmaOtaProvisionData();
            }
            if (cdmaOtaConfigData == null) {
                cdmaOtaConfigData = new OtaUtils.CdmaOtaConfigData();
            }
            if (cdmaOtaScreenState == null) {
                cdmaOtaScreenState = new OtaUtils.CdmaOtaScreenState();
            }
            if (cdmaOtaInCallScreenUiState == null) {
                cdmaOtaInCallScreenUiState = new OtaUtils.CdmaOtaInCallScreenUiState();
            }
        } else {
            //Clean up OTA data in GSM/UMTS. It is valid only for CDMA
            clearOtaState();
        }

        ringer.updateRingerContextAfterRadioTechnologyChange(this.phone);
        notifier.updateCallNotifierRegistrationsAfterRadioTechnologyChange();
        if (mBtHandsfree != null) {
            mBtHandsfree.updateBtHandsfreeAfterRadioTechnologyChange();
        }
        if (mInCallScreen != null) {
            mInCallScreen.updateAfterRadioTechnologyChange();
        }

        // Update registration for ICC status after radio technology change       
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            GeminiPhone mGeminiPhone = (GeminiPhone)PhoneFactory.getDefaultPhone();
            IccCard sim1Gemini = mGeminiPhone.getIccCardGemini(Phone.GEMINI_SIM_1);
            IccCard sim2Gemini = mGeminiPhone.getIccCardGemini(Phone.GEMINI_SIM_2);
            sim1Gemini.registerForNetworkLocked(mHandler, EVENT_SIM1_NETWORK_LOCKED, null);
            sim2Gemini.registerForNetworkLocked(mHandler, EVENT_SIM2_NETWORK_LOCKED, null);
        } else {
            IccCard sim = phone.getIccCard();
            if (sim != null) {
                if (VDBG) Log.v(LOG_TAG, "register for ICC status");
                sim.registerForNetworkLocked(mHandler, EVENT_SIM_NETWORK_LOCKED, null);
            }
        }
    }
    
    /**
     * Recomputes the mShowBluetoothIndication flag based on the current
     * bluetooth state and current telephony state.
     *
     * This needs to be called any time the bluetooth headset state or the
     * telephony state changes.
     *
     * @param forceUiUpdate if true, force the UI elements that care
     *                      about this flag to update themselves.
     */
    /* package */ void updateBluetoothIndication(boolean forceUiUpdate) {
        mShowBluetoothIndication = shouldShowBluetoothIndication(mBluetoothHeadsetState,
                                                                 mBluetoothHeadsetAudioState,
                                                                 mCM);
        if (forceUiUpdate) {
            // Post Handler messages to the various components that might
            // need to be refreshed based on the new state.
            if (isShowingCallScreen()){
                /// M: Call the new requestUpdateBluetoothIndication(),
                /// and pass the BluetoothHeadsetAudioState @{
                /// For ALPS00389674
                mInCallScreen.requestUpdateBluetoothIndication(mBluetoothHeadsetAudioState);
                /// @}
            }
            if (DBG) Log.d (LOG_TAG, "- updating in-call notification for BT state change...");
            mHandler.sendEmptyMessage(EVENT_UPDATE_INCALL_NOTIFICATION);
        }

        // Update the Proximity sensor based on Bluetooth audio state
        updateProximitySensorMode(mCM.getState());
    }
    
    /**
     * Receiver for misc intent broadcasts the Phone app cares about.
     */
    private class MtkPhoneAppBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (VDBG) Log.d(LOG_TAG, "MtkPhoneAppBroadcastReceiver -----action=" + action);
            if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                boolean enabled = intent.getBooleanExtra("state", false);
                if (VDBG) Log.d(LOG_TAG, "MtkPhoneAppBroadcastReceiver ------ AIRPLANEMODE enabled=" + enabled);
                if (enabled == true) {
                    PhoneUtils.DismissMMIDialog();
                }

                ///M: Solve [ALPS00409547][Rose][Free Test][Call]The ECC call cannot hang up after turn on airplane mode.(Once). @{
                try {
                   if (enabled && (mCM.getState() != PhoneConstants.State.IDLE)) {
                      Log.d(LOG_TAG, "Hangup all calls before turning on airplane mode");
                      mCM.hangupAllEx();
                   }
                } catch (Exception e) {
                }
                /// @}

                if (FeatureOption.MTK_FLIGHT_MODE_POWER_OFF_MD) {
                    if (FeatureOption.MTK_GEMINI_SUPPORT) {
                        if (enabled)
                            ((GeminiPhone)phone).setRadioMode(GeminiNetworkSubUtil.MODE_POWER_OFF);
                        else
                            ((GeminiPhone)phone).setRadioPowerOn();
                    } else {
                        if (enabled)
                            phone.setRadioPower(false, true);
                        else
                            phone.setRadioPowerOn();
                    }
                } else {
                    if (FeatureOption.MTK_GEMINI_SUPPORT) {
                        if (enabled) {
                            ((GeminiPhone)phone).setRadioMode(GeminiNetworkSubUtil.MODE_FLIGHT_MODE);
                        } else {
                            int dualSimModeSetting = System.getInt(getContentResolver(),
                                    System.DUAL_SIM_MODE_SETTING, GeminiNetworkSubUtil.MODE_DUAL_SIM);
                            ((GeminiPhone)phone).setRadioMode(dualSimModeSetting);
                        }
                    } else {
                        /* for consistent UI ,SIM Management for single sim project START */					
                        if (!enabled) {					
                            int SimModeSetting = System.getInt(getContentResolver(),
                                    System.DUAL_SIM_MODE_SETTING, GeminiNetworkSubUtil.MODE_SIM1_ONLY);
                            if(SimModeSetting == GeminiNetworkSubUtil.MODE_FLIGHT_MODE){    					
                                if (VDBG) Log.d(LOG_TAG, "Turn off airplane mode, but Radio still off due to sim mode setting is off");
                                enabled = true;
                            }
                        }
                        /* for consistent UI ,SIM Management for single sim project END */				
                        phone.setRadioPower(!enabled);
                    }
                }
            } else if (action.equals(Intent.ACTION_DUAL_SIM_MODE_CHANGED)) {
                int mode = intent.getIntExtra(Intent.EXTRA_DUAL_SIM_MODE, GeminiNetworkSubUtil.MODE_DUAL_SIM);
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    ((GeminiPhone)phone).setRadioMode(mode);
                } else {
                    boolean radioStatus = (0 == mode) ? false : true;
                    phone.setRadioPower(radioStatus);
                }
            } else if (action.equals(Intent.ACTION_CONFIGURATION_CHANGED)) {
                if (FeatureOption.MTK_GEMINI_SUPPORT != true) {
                    phone.refreshSpnDisplay();
                } else {
                    ((GeminiPhone)phone).refreshSpnDisplay();
                }
            } else if (action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)) {
                mBluetoothHeadsetState = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE,
                                                          BluetoothHeadset.STATE_DISCONNECTED);
                if (VDBG) Log.d(LOG_TAG, "mReceiver: HEADSET_STATE_CHANGED_ACTION");
                if (VDBG) Log.d(LOG_TAG, "==> new state: " + mBluetoothHeadsetState);
                updateBluetoothIndication(true);  // Also update any visible UI if necessary
            } else if (action.equals(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)) {
                mBluetoothHeadsetAudioState =
                        intent.getIntExtra(BluetoothHeadset.EXTRA_STATE,
                                           BluetoothHeadset.STATE_AUDIO_DISCONNECTED);
                if (VDBG) Log.d(LOG_TAG, "mReceiver: HEADSET_AUDIO_STATE_CHANGED_ACTION");
                if (VDBG) Log.d(LOG_TAG, "==> new state: " + mBluetoothHeadsetAudioState);
                updateBluetoothIndication(true);  // Also update any visible UI if necessary
            } else if (action.equals(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED)) {
                String state = intent.getStringExtra(PhoneConstants.STATE_KEY);
                String reason = intent.getStringExtra(PhoneConstants.STATE_CHANGE_REASON_KEY);
                if (VDBG) Log.d(LOG_TAG, "mReceiver: ACTION_ANY_DATA_CONNECTION_STATE_CHANGED state:" + state
                     + " reason:" + reason);
    
                // The "data disconnected due to roaming" notification is shown
                // if (a) you have the "data roaming" feature turned off, and
                // (b) you just lost data connectivity because you're roaming.
                boolean disconnectedDueToRoaming = "DISCONNECTED".equals(state) &&
                    Phone.REASON_ROAMING_ON.equals(reason) && !phone.getDataRoamingEnabled();
                //since getDataRoamingEnabled will access database, put it at last.
    
                mHandler.sendEmptyMessage(disconnectedDueToRoaming
                                          ? EVENT_DATA_ROAMING_DISCONNECTED
                                          : EVENT_DATA_ROAMING_OK);
            } else if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
                if (VDBG) Log.d(LOG_TAG, "mReceiver: ACTION_HEADSET_PLUG");
                if (VDBG) Log.d(LOG_TAG, "    state: " + intent.getIntExtra("state", 0));
                if (VDBG) Log.d(LOG_TAG, "    name: " + intent.getStringExtra("name"));
                mIsHeadsetPlugged = (intent.getIntExtra("state", 0) == 1);
                mHandler.sendMessage(mHandler.obtainMessage(EVENT_WIRED_HEADSET_PLUG, 0));
            } else if (action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
                // if an attempt to un-PUK-lock the device was made, while we're
                // receiving this state change notification, notify the handler.
                // NOTE: This is ONLY triggered if an attempt to un-PUK-lock has
                // been attempted.
                // below is MTK version
                int unlockSIMID = intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY,-1);
                String unlockSIMStatus = intent.getStringExtra(IccCard.INTENT_KEY_ICC_STATE);
                Log.d(LOG_TAG, "[unlock SIM card NO switched. Now] " + unlockSIMID + " is active.");
                Log.d(LOG_TAG, "[unlockSIMStatus] : "  + unlockSIMStatus);
                if ((unlockSIMID == Phone.GEMINI_SIM_1) && ((IccCard.INTENT_VALUE_LOCKED_NETWORK).equals(unlockSIMStatus)) ){
                    Log.d(LOG_TAG, "[unlockSIMID :Phone.GEMINI_SIM_1]");

                    arySIMLockStatus[0] = 2;//need to deal with SIM1 SIM Lock
                    if ((arySIMLockStatus[1] != 1) && (arySIMLockStatus[1] != 4)){
                        arySIMLockStatus[0] = 1;
                        mHandler.sendMessage(mHandler.obtainMessage(EVENT_SIM1_NETWORK_LOCKED,
                        intent.getStringExtra(IccCard.INTENT_KEY_ICC_STATE)));
                    }
                    Log.d(LOG_TAG,"[SIM1][changed][arySIMLockStatus]: ["+ arySIMLockStatus[0] + " , " + arySIMLockStatus[1] + " ]");
                }else if((unlockSIMID == Phone.GEMINI_SIM_2) && ((IccCard.INTENT_VALUE_LOCKED_NETWORK).equals(unlockSIMStatus))){
                    Log.d(LOG_TAG, "[unlockSIMID :Phone.GEMINI_SIM_2]");                    
                    arySIMLockStatus[1] = 2;//need to deal with SIM2 SIM Lock
                    if ((arySIMLockStatus[0] != 1) && (arySIMLockStatus[0] != 4)) {
                        arySIMLockStatus[1] = 1;
                        mHandler.sendMessage(mHandler.obtainMessage(EVENT_SIM2_NETWORK_LOCKED,
                                intent.getStringExtra(IccCard.INTENT_KEY_ICC_STATE)));
                    }
                    Log.d(LOG_TAG,"[SIM2][changed][arySIMLockStatus]: [" + arySIMLockStatus[0] + " , " + arySIMLockStatus[1] + " ]");
                } else if (unlockSIMStatus.equals(IccCard.INTENT_VALUE_ICC_READY)) {
                    int delaySendMessage = 2000;
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(EVENT_SIM_STATE_CHANGED,IccCard.INTENT_VALUE_ICC_READY), delaySendMessage);
                } else {
                    Log.d(LOG_TAG, "[unlockSIMID : Other information]: " + unlockSIMStatus);
                }
            } else if (action.equals(TelephonyIntents.ACTION_RADIO_TECHNOLOGY_CHANGED)) {
                String newPhone = intent.getStringExtra(Phone.PHONE_NAME_KEY);
                Log.d(LOG_TAG, "Radio technology switched. Now " + newPhone + " is active.");
                initForNewRadioTechnology();
            } else if (action.equals(TelephonyIntents.ACTION_SERVICE_STATE_CHANGED)) {
                handleServiceStateChanged(intent);
            } else if (action.equals(TelephonyIntents.ACTION_EMERGENCY_CALLBACK_MODE_CHANGED)) {
                if (TelephonyCapabilities.supportsEcm(phone)) {
                    Log.d(LOG_TAG, "Emergency Callback Mode arrived in PhoneApp.");
                    // Start Emergency Callback Mode service
                    if (intent.getBooleanExtra("phoneinECMState", false)) {
                        context.startService(new Intent(context,
                                EmergencyCallbackModeService.class));
                    }
                } else {
                    // It doesn't make sense to get ACTION_EMERGENCY_CALLBACK_MODE_CHANGED
                    // on a device that doesn't support ECM in the first place.
                    Log.e(LOG_TAG, "Got ACTION_EMERGENCY_CALLBACK_MODE_CHANGED, "
                          + "but ECM isn't supported for phone: " + phone.getPhoneName());
                }
            } else if (action.equals(Intent.ACTION_DOCK_EVENT)) {
                mDockState = intent.getIntExtra(Intent.EXTRA_DOCK_STATE,
                        Intent.EXTRA_DOCK_STATE_UNDOCKED);
                if (VDBG) Log.d(LOG_TAG, "ACTION_DOCK_EVENT -> mDockState = " + mDockState);
                mHandler.sendMessage(mHandler.obtainMessage(EVENT_DOCK_STATE_CHANGED, 0));
            } else if (action.equals(TtyIntent.TTY_PREFERRED_MODE_CHANGE_ACTION)) {
                mPreferredTtyMode = intent.getIntExtra(TtyIntent.TTY_PREFFERED_MODE,
                                                       Phone.TTY_MODE_OFF);
                if (VDBG) Log.d(LOG_TAG, "mReceiver: TTY_PREFERRED_MODE_CHANGE_ACTION");
                if (VDBG) Log.d(LOG_TAG, "    mode: " + mPreferredTtyMode);
                mHandler.sendMessage(mHandler.obtainMessage(EVENT_TTY_PREFERRED_MODE_CHANGED, 0));
            } else if (action.equals(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
                int ringerMode = intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE,
                        AudioManager.RINGER_MODE_NORMAL);
                if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
                    notifier.silenceRinger();
                }
            } else if (action.equals(Intent.ACTION_SHUTDOWN)) {
                Log.d(LOG_TAG, "ACTION_SHUTDOWN received");
                addCallSync();
            } else if (action.equals(STKCALL_REGISTER_SPEECH_INFO)) {
                PhoneUtils.placeCallRegister(phone);
                mHandler.sendEmptyMessageDelayed(EVENT_SHOW_INCALL_SCREEN_FOR_STK_SETUP_CALL, DELAY_SHOW_INCALL_SCREEN_FOR_STK_SETUP_CALL);
            } else if (action.equals(MISSEDCALL_DELETE_INTENT)) {
                Log.d(LOG_TAG, "MISSEDCALL_DELETE_INTENT");
                ((MtkNotificationMgr)notificationMgr).resetMissedCallNumber();
            } else if (action.equals(NETWORK_MODE_CHANGE)) {
                int modemNetworkMode = intent.getIntExtra(NETWORK_MODE_CHANGE, 0);
                int simId = intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, 0);
                int oldmode = intent.getIntExtra(OLD_NETWORK_MODE, -1);
                if (FeatureOption.MTK_GEMINI_SUPPORT)
                {
                    GeminiPhone dualPhone = (GeminiPhone)phone;
                    dualPhone.setPreferredNetworkTypeGemini(modemNetworkMode, mHandler
                            .obtainMessage(MESSAGE_SET_PREFERRED_NETWORK_TYPE, oldmode, modemNetworkMode), simId);
                } else {
                    phone.setPreferredNetworkType(modemNetworkMode, mHandler
                            .obtainMessage(MESSAGE_SET_PREFERRED_NETWORK_TYPE, oldmode, modemNetworkMode));
                }
            } else if (action.equals("android.intent.action.ACTION_SHUTDOWN_IPO")) {
                Log.d(LOG_TAG, "ACTION_SHUTDOWN_IPO received");

                if (PhoneFactory.isFlightModePowerOffMD())
                    SystemProperties.set("ril.shutdown.ipo", "yes");

                SystemProperties.set("gsm.ril.uicctype", "");
                SystemProperties.set("gsm.ril.uicctype.2", "");
                SystemProperties.set("ril.iccid.sim1", null);
                SystemProperties.set("ril.iccid.sim2", null);

                phone.setRadioPower(false, true);
                if (null != inCallUiState) {
                    inCallUiState.clearState();
                }
                if (mInCallScreen != null) {
                    mInCallScreen.internalHangupAllCalls(mCM);
                }
            } else if (action.equals("android.intent.action.ACTION_PREBOOT_IPO")) {
                Log.d(LOG_TAG, "ACTION_PREBOOT_IPO received");
                if (PhoneFactory.isFlightModePowerOffMD())
                    SystemProperties.set("ril.shutdown.ipo", null);

                Settings.System.putLong(getApplicationContext().getContentResolver(), Settings.System.SIM_LOCK_STATE_SETTING, 0x0L);
                phone.setRadioPowerOn();
                if (null != inCallUiState) {
                    inCallUiState.clearState();
                }
            } else if (action.equals(GeminiPhone.EVENT_3G_SWITCH_START_MD_RESET)) {
                Log.d(LOG_TAG, "EVENT_3G_SWITCH_START_MD_RESET");
                Settings.System.putLong(getApplicationContext().getContentResolver(), Settings.System.SIM_LOCK_STATE_SETTING, 0x0L);
                arySIMLockStatus[0] = 3;
                arySIMLockStatus[1] = 3;
            } else if (action.equals(TelephonyIntents.ACTION_RADIO_OFF)) {
                int slot = intent.getIntExtra(TelephonyIntents.INTENT_KEY_ICC_SLOT, 0);
                Log.d(LOG_TAG, "ACTION_RADIO_OFF slot = " + slot);
                clearSimSettingFlag(slot);
                Log.i(LOG_TAG,"[xp Test][MODEM RESET]");
                arySIMLockStatus[0] = 3;
                arySIMLockStatus[1] = 3;
            } else if (action.equals(ACTION_MODEM_STATE)) {
                SystemService.start("md_minilog_util");
                /*int mdState = intent.getIntExtra("state", -1);
                Log.i(LOG_TAG, "Get MODEM STATE [" + mdState + "]");
                switch (mdState) {
                    case CCCI_MD_BROADCAST_EXCEPTION:
                        SystemService.start("md_minilog_util");
                        break;
                    case CCCI_MD_BROADCAST_RESET:
                        SystemService.start("md_minilog_util");
                        break;
                    case CCCI_MD_BROADCAST_READY:
                        SystemService.start("md_minilog_util");
                        break;
                    defaut:
                        SystemService.start("md_minilog_util");
                }*/
            } else if (action.equals("TRIGGER_MAINTHREAD_LOOPER")) {
                /// M: To trigger main thread looper for showing confirm dialog.
                Log.d(LOG_TAG, "TRIGGER_MAINTHREAD_LOOPER received");
                mHandler.sendEmptyMessage(EVENT_TRIGGER_MAINTHREAD_LOOPER);
            }else if(SPN_STRINGS_UPDATED_ACTION.equals(action)) {
                // ECC button should be hidden when there is no service.
                if (intent.getBooleanExtra(EXTRA_SHOW_PLMN, false)) {
                   String plmn = intent.getStringExtra(EXTRA_PLMN);
                   int simId = intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, Phone.GEMINI_SIM_1);
                   int index = simId - Phone.GEMINI_SIM_1;
                   Log.d(LOG_TAG, "[SPN_STRINGS_UPDATED_ACTION]index = " + index);
                   Log.d(LOG_TAG, "[SPN_STRINGS_UPDATED_ACTION]plmn = " + plmn);
                   if (index < 2) {
                      mIsNoService[index] = (plmn == null);
                   }
                }  
            }/* End of SPN_STRINGS_UPDATED_ACTION */
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
    private class MtkMediaButtonBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (VDBG) Log.d(LOG_TAG,
                           "MtkMediaButtonBroadcastReceiver.onReceive()...  event = " + event);
            //Not sure why add the ACTION_DOWN condition, but this will not answer the incomig call
            //so change the ACTION_DOWN to ACTION_UP (ALPS00287837)
            if ((event != null)
                && (event.getKeyCode() == KeyEvent.KEYCODE_HEADSETHOOK)
                && (event.getAction() == KeyEvent.ACTION_UP)) {

                if (event.getRepeatCount() == 0) {
                    // Mute ONLY on the initial keypress.
                    if (VDBG) Log.d(LOG_TAG, "MediaButtonBroadcastReceiver: HEADSETHOOK down!");
                    boolean consumed = PhoneUtils.handleHeadsetHook(phone, event);
                    if (VDBG) Log.d(LOG_TAG, "==> handleHeadsetHook(): consumed = " + consumed);
                    if (consumed) {
                        // If a headset is attached and the press is consumed, also update
                        // any UI items (such as an InCallScreen mute button) that may need to
                        // be updated if their state changed.
                        updateInCallScreen();  // Has no effect if the InCallScreen isn't visible
                        abortBroadcast();
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
    }
    
    private void handleServiceStateChanged(Intent intent) {
        /**
         * This used to handle updating EriTextWidgetProvider this routine
         * and and listening for ACTION_SERVICE_STATE_CHANGED intents could
         * be removed. But leaving just in case it might be needed in the near
         * future.
         */

        // If service just returned, start sending out the queued messages
        ServiceState ss = ServiceState.newFromBundle(intent.getExtras());

        if (ss != null) {
            int state = ss.getState();
            notificationMgr.updateNetworkSelection(state, getPhone(ss.getMySimId()));
        }
    }
    
    private void handleQueryTTYModeResponse(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar.exception != null) {
            if (DBG) Log.d(LOG_TAG, "handleQueryTTYModeResponse: Error getting TTY state.");
        } else {
            if (DBG) Log.d(LOG_TAG,
                           "handleQueryTTYModeResponse: TTY enable state successfully queried.");
            //We will get the tty mode from the settings directly
            //int ttymode = ((int[]) ar.result)[0];
            int ttymode = Phone.TTY_MODE_OFF;
            if (isHeadsetPlugged()) {
                ttymode = mPreferredTtyMode;
            }
            if (DBG) Log.d(LOG_TAG, "handleQueryTTYModeResponse:ttymode=" + ttymode);

            Intent ttyModeChanged = new Intent(TtyIntent.TTY_ENABLED_CHANGE_ACTION);
            ttyModeChanged.putExtra("ttyEnabled", ttymode != Phone.TTY_MODE_OFF);
            sendBroadcast(ttyModeChanged);

            String audioTtyMode;
            switch (ttymode) {
            case Phone.TTY_MODE_FULL:
                audioTtyMode = "tty_full";
                break;
            case Phone.TTY_MODE_VCO:
                audioTtyMode = "tty_vco";
                break;
            case Phone.TTY_MODE_HCO:
                audioTtyMode = "tty_hco";
                break;
            case Phone.TTY_MODE_OFF:
            default:
                audioTtyMode = "tty_off";
                break;
            }
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.setParameters("tty_mode=" + audioTtyMode);
            PhoneUtils.setTtyMode(audioTtyMode);
        }
    }

    private int convertTTYmodeToRadio(int ttyMode) {
        int radioMode = 0;
        
        switch (ttyMode) {
            case Phone.TTY_MODE_FULL:
            case Phone.TTY_MODE_HCO:
            case Phone.TTY_MODE_VCO:
                radioMode = Phone.TTY_MODE_FULL;
                break;
                
            default:
            radioMode = Phone.TTY_MODE_OFF;     
        }
        
        return radioMode;
    }
    
    private void handleSetTTYModeResponse(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;

        if (ar.exception != null) {
            if (DBG) Log.d (LOG_TAG,
                    "handleSetTTYModeResponse: Error setting TTY mode, ar.exception"
                    + ar.exception);
        }

       //Now Phone doesn't support ttymode query, so we make a fake response to trigger the set to audio
        //phone.queryTTYMode(mHandler.obtainMessage(EVENT_TTY_MODE_GET));
        Message m = mHandler.obtainMessage(EVENT_TTY_MODE_GET);
        m.obj = new AsyncResult(null, null, null);
        m.sendToTarget();
    }
    
    
    public boolean isQVGA() {
        boolean retval = false;
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        if ((dm.widthPixels == 320 && dm.heightPixels == 240) 
                || (dm.widthPixels == 240 && dm.heightPixels == 320)) {
            retval = true;
        }
        return retval;
    }

    static Intent createVTInCallIntent() {
        Intent intent = createInCallIntent();
        intent.putExtra(Constants.EXTRA_IS_VIDEO_CALL, true);
        return intent;
    }
    
    
    //To judge whether current sim card need to unlock sim lock:default false
    public static boolean bNeedUnlockSIMLock(int iSIMNum) {
            GeminiPhone mGeminiPhone = (GeminiPhone)PhoneFactory.getDefaultPhone();
            if ((mGeminiPhone.getIccCardGemini(iSIMNum).getState() == IccCardConstants.State.PIN_REQUIRED) ||
                (mGeminiPhone.getIccCardGemini(iSIMNum).getState() == IccCardConstants.State.PUK_REQUIRED) ||
                (mGeminiPhone.getIccCardGemini(iSIMNum).getState() == IccCardConstants.State.NOT_READY)) {               
                
                Log.d(LOG_TAG, "[bNeedUnlockSIMLock][NO Card/PIN/PUK]: " +  iSIMNum);                
                return false;
            } else {
                return true;
            }
        
    }

    void addCallSync() {
//    	
//        Call fgCall = mCM.getActiveFgCall();
//        Call bgCall = mCM.getFirstActiveBgCall();
//        
//        List<Connection> connections = null;
//        AuroraCallerInfo ci = null;
//        int callType = Calls.OUTGOING_TYPE;
//        int simId = Phone.GEMINI_SIM_1;
//        int isVideo = 0;
//
//        if (FeatureOption.MTK_GEMINI_SUPPORT) {
//            GeminiPhone phone = (GeminiPhone)phone;
//            SIMInfo simInfo = null;
//            if (phone.getStateGemini(Phone.GEMINI_SIM_2) != PhoneConstants.State.IDLE) {
//                simId = Phone.GEMINI_SIM_2;
//            } else if (phone.getStateGemini(Phone.GEMINI_SIM_1) != PhoneConstants.State.IDLE) {
//                simId = Phone.GEMINI_SIM_1;
//            }
//            if (mInCallScreen != null) {
//                simInfo = SIMInfo.getSIMInfoBySlot(mInCallScreen, simId);
//            }
//            if (simInfo != null) {
//                simId = (int)simInfo.mSimId;
//            } else {
//                simId = 0;
//            }
//        }
//
//        if (fgCall.getState() != Call.State.IDLE) {
//            connections = fgCall.getConnections();
//            for (Connection c : connections) {
//                if (c.isAlive()) {
//                    ci = notifier.getCallerInfoFromConnection(c);
//                    if (c.isIncoming())
//                        callType = Calls.INCOMING_TYPE;
//                    if (c.isVideo()) {
//                        isVideo = 1;
//                    } else {
//                        isVideo = 0;
//                    }
//                    Calls.addCall(ci, mInCallScreen, c.getAddress(),
//                            notifier.getPresentation(c, ci), callType, c.getCreateTime(), (int)(c.getDurationMillis()/1000), simId, isVideo, ci.mRawContactId, ProviderUtils.getDataIdByRawContactId(ci.mRawContactId), ci.mPrivateId);//, false);
//                }
//            }
//        }
//        
//        if (bgCall.getState() != Call.State.IDLE) {
//            connections = bgCall.getConnections();
//            for (Connection c : connections) {
//                if (c.isAlive()) {
//                    ci = notifier.getCallerInfoFromConnection(c);
//                    if (c.isIncoming()) {
//                        callType = Calls.INCOMING_TYPE;
//                    }
//                    if (c.isVideo()) {
//                        isVideo = 1;
//                    } else {
//                        isVideo = 0;
//                    }
//                    Calls.addCall(ci, mInCallScreen, c.getAddress(),
//                            notifier.getPresentation(c, ci), callType, c.getCreateTime(), (int)(c.getDurationMillis()/1000), simId, isVideo, ci.mRawContactId, ProviderUtils.getDataIdByRawContactId(ci.mRawContactId), ci.mPrivateId);//, false);
//                }
//            }
//        }
    }


    private void clearSimSettingFlag(int slot) {

        Long bitSetMask = (0x3L << (2 * slot));

        Long simLockState = 0x0L;

        try {
            simLockState = Settings.System.getLong(getApplicationContext()
                    .getContentResolver(), Settings.System.SIM_LOCK_STATE_SETTING);

            simLockState = simLockState & (~bitSetMask);

            Settings.System.putLong(getApplicationContext().getContentResolver(),
                    Settings.System.SIM_LOCK_STATE_SETTING, simLockState);
        } catch (SettingNotFoundException e) {
            Log.e(LOG_TAG, "clearSimSettingFlag exception");
            e.printStackTrace();
        }
    }


    public Intent createPhoneEndIntent() {
        Intent intent = null;
        if (FeatureOption.MTK_BRAZIL_CUSTOMIZATION_VIVO) {
            intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory (Intent.CATEGORY_HOME);
            return intent;
        }

        if (TextUtils.equals(inCallUiState.latestActiveCallOrigin, ALLOWED_EXTRA_CALL_ORIGIN)) {
            if (VDBG) Log.d(LOG_TAG, "Valid latestActiveCallOrigin("
                    + inCallUiState.latestActiveCallOrigin + ") was found. "
                    + "Go back to the previous screen.");
            // Right now we just launch the Activity which launched in-call UI. Note that we're
            // assuming the origin is from "com.android.contacts", which may be incorrect in the
            // future.
            intent = new Intent();
            intent.setClassName(DEFAULT_CALL_ORIGIN_PACKAGE, inCallUiState.latestActiveCallOrigin);
            return intent;
        }

        return intent;
    }

    /**
     * Use to check if there is no service right now.
     * If the plmn in SPN_STRINGS_UPDATED_ACTION intent is null, it means that there is no service.
     *
     * @return true if there is no service, else return false.
     */
    public boolean isCurrentlyNoService() {
       // ECC button should be hidden when there is no service.
       Log.d(LOG_TAG, "[isCurrentlyNoService]mIsNoService[0] = " + mIsNoService[0]);
       Log.d(LOG_TAG, "[isCurrentlyNoService]mIsNoService[1] = " + mIsNoService[1]);
       return (mIsNoService[0] && mIsNoService[1]);
    }    
    
    /*
     * Gets User preferred Voice subscription setting
     */
    @Override
    public int getVoiceSubscription() {
    	if(AuroraPhoneUtils.isSimulate()) {
    		return 0;
    	} else {
    		return PhoneUtils.getMtkSub(Settings.System.VOICE_CALL_SIM_SETTING);
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
            return PhoneUtils.getMtkSub(Settings.System.GPRS_CONNECTION_SIM_SETTING);
    	}
    }
    
    /*
     * Gets User preferred SMS subscription setting
     */
    public int getSMSSubscription() {        
    	if(AuroraPhoneUtils.isSimulate()) {
    		return 0;
    	} else {
            return PhoneUtils.getMtkSub(Settings.System.SMS_SIM_SETTING);
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
       TelephonyManagerEx tm = TelephonyManagerEx.getDefault();
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
   
   @Override
   void displayCallScreen() {
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
           startActivity(createInCallIntent(PhoneUtils.getActiveSubscription()));
       } catch (ActivityNotFoundException e) {
           // It's possible that the in-call UI might not exist (like on
           // non-voice-capable devices), so don't crash if someone
           // accidentally tries to bring it up...
           Log.w(LOG_TAG, "displayCallScreen: transition to InCallScreen failed: " + e);
       }
       Profiler.callScreenRequested();
   }
    
   /**
    * Return an Intent that can be used to bring up the in-call screen.
    *
    * This intent can only be used from within the Phone app, since the
    * InCallScreen is not exported from our AndroidManifest.
    */
   @Override
    Intent createInCallIntent(int subscription) {
       Log.d(LOG_TAG, "createInCallIntent subscription: " + subscription);
       Intent intent = new Intent(Intent.ACTION_MAIN, null);
       intent.putExtra(SUBSCRIPTION_KEY, subscription);
       intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
               | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
               | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
       intent.setClassName("com.android.phone", getCallScreenClassName());
       return intent;
   }
}