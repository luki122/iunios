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

import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.telephony.NeighboringCellInfo;
import android.telephony.CellInfo;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.telephony.DefaultPhoneNotifier;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.PhoneConstants;

import android.os.IBinder;
import android.os.RemoteException;

import java.util.List;
import java.util.ArrayList;

import com.android.internal.telephony.IPhoneSubInfo;
import com.android.internal.telephony.TelephonyProperties;

import android.os.SystemProperties;
import android.telephony.BtSimapOperResponse;
import android.os.Build;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.os.SystemProperties;

//import com.android.internal.telephony.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.PhoneBase;
import com.android.internal.telephony.PhoneProxy;

/**
 * Implementation of the ITelephony interface.
 */
public class PhoneInterfaceManager extends ITelephony.Stub {
    private static final String LOG_TAG = "PhoneInterfaceManager";
    private static final boolean DBG = (PhoneGlobals.DBG_LEVEL >= 2);
    private static final boolean DBG_LOC = false;

    // Message codes used with mMainThreadHandler
    private static final int CMD_HANDLE_PIN_MMI = 1;
    private static final int CMD_HANDLE_NEIGHBORING_CELL = 2;
    private static final int EVENT_NEIGHBORING_CELL_DONE = 3;
    private static final int CMD_ANSWER_RINGING_CALL = 4;
    private static final int CMD_END_CALL = 5;  // not used yet
    private static final int CMD_SILENCE_RINGER = 6;

    /** The singleton instance. */
    private static PhoneInterfaceManager sInstance;

    PhoneGlobals mApp;
    Phone mPhone;
    CallManager mCM;
    MainThreadHandler mMainThreadHandler;

    /**
     * A request object for use with {@link MainThreadHandler}. Requesters should wait() on the
     * request after sending. The main thread will notify the request when it is complete.
     */
    private static final class MainThreadRequest {
        /** The argument to use for the request */
        public Object argument;
        /** The result of the request that is run on the main thread */
        public Object result;

        public MainThreadRequest(Object argument) {
            this.argument = argument;
        }
    }

    /**
     * A handler that processes messages on the main thread in the phone process. Since many
     * of the Phone calls are not thread safe this is needed to shuttle the requests from the
     * inbound binder threads to the main thread in the phone process.  The Binder thread
     * may provide a {@link MainThreadRequest} object in the msg.obj field that they are waiting
     * on, which will be notified when the operation completes and will contain the result of the
     * request.
     *
     * <p>If a MainThreadRequest object is provided in the msg.obj field,
     * note that request.result must be set to something non-null for the calling thread to
     * unblock.
     */
    private final class MainThreadHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            MainThreadRequest request;
            Message onCompleted;
            AsyncResult ar;

            switch (msg.what) {
                case CMD_HANDLE_PIN_MMI:
                    request = (MainThreadRequest) msg.obj;
                    request.result = Boolean.valueOf(
                            mPhone.handlePinMmi((String) request.argument));
                    // Wake up the requesting thread
                    synchronized (request) {
                        request.notifyAll();
                    }
                    break;

                case CMD_HANDLE_NEIGHBORING_CELL:
                    request = (MainThreadRequest) msg.obj;
                    onCompleted = obtainMessage(EVENT_NEIGHBORING_CELL_DONE,
                            request);
                    mPhone.getNeighboringCids(onCompleted);
                    break;

                case EVENT_NEIGHBORING_CELL_DONE:
                    ar = (AsyncResult) msg.obj;
                    request = (MainThreadRequest) ar.userObj;
                    if (ar.exception == null && ar.result != null) {
                        request.result = ar.result;
                    } else {
                        // create an empty list to notify the waiting thread
                        request.result = new ArrayList<NeighboringCellInfo>();
                    }
                    // Wake up the requesting thread
                    synchronized (request) {
                        request.notifyAll();
                    }
                    break;

                case CMD_ANSWER_RINGING_CALL:
                    answerRingingCallInternal();
                    break;

                case CMD_SILENCE_RINGER:
                    silenceRingerInternal();
                    break;

                case CMD_END_CALL:
                    request = (MainThreadRequest) msg.obj;
                    boolean hungUp = false;
                    int phoneType = mPhone.getPhoneType();
                    if (phoneType == PhoneConstants.PHONE_TYPE_CDMA) {
                        // CDMA: If the user presses the Power button we treat it as
                        // ending the complete call session
                        hungUp = PhoneUtils.hangupRingingAndActive(mPhone);
                    } else if (phoneType == PhoneConstants.PHONE_TYPE_GSM) {
                        // GSM: End the call as per the Phone state
                        hungUp = PhoneUtils.hangup(mCM);
                    } else {
                        throw new IllegalStateException("Unexpected phone type: " + phoneType);
                    }
                    if (DBG) log("CMD_END_CALL: " + (hungUp ? "hung up!" : "no call to hang up"));
                    request.result = hungUp;
                    // Wake up the requesting thread
                    synchronized (request) {
                        request.notifyAll();
                    }
                    break;

                default:
                    Log.w(LOG_TAG, "MainThreadHandler: unexpected message code: " + msg.what);
                    break;
            }
        }
    }

    /**
     * Posts the specified command to be executed on the main thread,
     * waits for the request to complete, and returns the result.
     * @see sendRequestAsync
     */
    private Object sendRequest(int command, Object argument) {
        if (Looper.myLooper() == mMainThreadHandler.getLooper()) {
            throw new RuntimeException("This method will deadlock if called from the main thread.");
        }

        MainThreadRequest request = new MainThreadRequest(argument);
        Message msg = mMainThreadHandler.obtainMessage(command, request);
        msg.sendToTarget();

        // Wait for the request to complete
        synchronized (request) {
            while (request.result == null) {
                try {
                    request.wait();
                } catch (InterruptedException e) {
                    // Do nothing, go back and wait until the request is complete
                }
            }
        }
        return request.result;
    }

    /**
     * Asynchronous ("fire and forget") version of sendRequest():
     * Posts the specified command to be executed on the main thread, and
     * returns immediately.
     * @see sendRequest
     */
    private void sendRequestAsync(int command) {
        mMainThreadHandler.sendEmptyMessage(command);
    }

    /**
     * Initialize the singleton PhoneInterfaceManager instance.
     * This is only done once, at startup, from PhoneApp.onCreate().
     */
    /* package */ static PhoneInterfaceManager init(PhoneGlobals app, Phone phone) {
        synchronized (PhoneInterfaceManager.class) {
            if (sInstance == null) {
                sInstance = new PhoneInterfaceManager(app, phone);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            return sInstance;
        }
    }

    /** Private constructor; @see init() */
    private PhoneInterfaceManager(PhoneGlobals app, Phone phone) {
        mApp = app;
        mPhone = phone;
        mCM = PhoneGlobals.getInstance().mCM;
        mMainThreadHandler = new MainThreadHandler();
        publish();
    }

    private void publish() {
        if (DBG) log("publish: " + this);
        if(DeviceUtils.isUseAuroraPhoneService()) {
        	ServiceManager.addService("radio.phone", this);
        } else {
           	ServiceManager.addService("phone", this);
        }
    }

    //
    // Implementation of the ITelephony interface.
    //

    public void dial(String number) {
        if (DBG) log("dial: " + number);
        // No permission check needed here: This is just a wrapper around the
        // ACTION_DIAL intent, which is available to any app since it puts up
        // the UI before it does anything.

        String url = createTelUrl(number);
        if (url == null) {
            return;
        }

        // PENDING: should we just silently fail if phone is offhook or ringing?
        PhoneConstants.State state = mCM.getState();
        if (state != PhoneConstants.State.OFFHOOK && state != PhoneConstants.State.RINGING) {
            Intent  intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mApp.startActivity(intent);
        }
    }

    public void call(String number) {
        if (DBG) log("call: " + number);

        // This is just a wrapper around the ACTION_CALL intent, but we still
        // need to do a permission check since we're calling startActivity()
        // from the context of the phone app.
        enforceCallPermission();

        String url = createTelUrl(number);
        if (url == null) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mApp.startActivity(intent);
    }

    private boolean showCallScreenInternal(boolean specifyInitialDialpadState,
                                           boolean initialDialpadState) {
        if (!PhoneGlobals.sVoiceCapable) {
            // Never allow the InCallScreen to appear on data-only devices.
            return false;
        }
        if (isIdle()) {
            return false;
        }
        // If the phone isn't idle then go to the in-call screen
        long callingId = Binder.clearCallingIdentity();
        try {
            Intent intent;
            if (specifyInitialDialpadState) {
                intent = PhoneGlobals.createInCallIntent(initialDialpadState);
            } else {
                intent = PhoneGlobals.createInCallIntent();
            }
            try {
                mApp.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                // It's possible that the in-call UI might not exist
                // (like on non-voice-capable devices), although we
                // shouldn't be trying to bring up the InCallScreen on
                // devices like that in the first place!
                Log.w(LOG_TAG, "showCallScreenInternal: "
                      + "transition to InCallScreen failed; intent = " + intent);
            }
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
        return true;
    }

    // Show the in-call screen without specifying the initial dialpad state.
    public boolean showCallScreen() {
        return showCallScreenInternal(false, false);
    }

    // The variation of showCallScreen() that specifies the initial dialpad state.
    // (Ideally this would be called showCallScreen() too, just with a different
    // signature, but AIDL doesn't allow that.)
    public boolean showCallScreenWithDialpad(boolean showDialpad) {
        return showCallScreenInternal(true, showDialpad);
    }

    /**
     * End a call based on call state
     * @return true is a call was ended
     */
    public boolean endCall() {
        enforceCallPermission();
        return (Boolean) sendRequest(CMD_END_CALL, null);
    }

    public void answerRingingCall() {
        if (DBG) log("answerRingingCall...");
        // TODO: there should eventually be a separate "ANSWER_PHONE" permission,
        // but that can probably wait till the big TelephonyManager API overhaul.
        // For now, protect this call with the MODIFY_PHONE_STATE permission.
        enforceModifyPermission();
        sendRequestAsync(CMD_ANSWER_RINGING_CALL);
    }

    /**
     * Make the actual telephony calls to implement answerRingingCall().
     * This should only be called from the main thread of the Phone app.
     * @see answerRingingCall
     *
     * TODO: it would be nice to return true if we answered the call, or
     * false if there wasn't actually a ringing incoming call, or some
     * other error occurred.  (In other words, pass back the return value
     * from PhoneUtils.answerCall() or PhoneUtils.answerAndEndActive().)
     * But that would require calling this method via sendRequest() rather
     * than sendRequestAsync(), and right now we don't actually *need* that
     * return value, so let's just return void for now.
     */
    private void answerRingingCallInternal() {
        final boolean hasRingingCall = !mPhone.getRingingCall().isIdle();
        if (hasRingingCall) {
            final boolean hasActiveCall = !mPhone.getForegroundCall().isIdle();
            final boolean hasHoldingCall = !mPhone.getBackgroundCall().isIdle();
            if (hasActiveCall && hasHoldingCall) {
                // Both lines are in use!
                // TODO: provide a flag to let the caller specify what
                // policy to use if both lines are in use.  (The current
                // behavior is hardwired to "answer incoming, end ongoing",
                // which is how the CALL button is specced to behave.)
                PhoneUtils.answerAndEndActive(mCM, mCM.getFirstActiveRingingCall());
                return;
            } else {
                // answerCall() will automatically hold the current active
                // call, if there is one.
                PhoneUtils.answerCall(mCM.getFirstActiveRingingCall());
                return;
            }
        } else {
            // No call was ringing.
            return;
        }
    }

    public void silenceRinger() {
        if (DBG) log("silenceRinger...");
        // TODO: find a more appropriate permission to check here.
        // (That can probably wait till the big TelephonyManager API overhaul.
        // For now, protect this call with the MODIFY_PHONE_STATE permission.)
        enforceModifyPermission();
        sendRequestAsync(CMD_SILENCE_RINGER);
    }

    /**
     * Internal implemenation of silenceRinger().
     * This should only be called from the main thread of the Phone app.
     * @see silenceRinger
     */
    private void silenceRingerInternal() {
        if ((mCM.getState() == PhoneConstants.State.RINGING)
            && mApp.notifier.isRinging()) {
            // Ringer is actually playing, so silence it.
            if (DBG) log("silenceRingerInternal: silencing...");
            mApp.notifier.silenceRinger();
        }
    }

    public boolean isOffhook() {
        return (mCM.getState() == PhoneConstants.State.OFFHOOK);
    }

    public boolean isRinging() {
        return (mCM.getState() == PhoneConstants.State.RINGING);
    }

    public boolean isIdle() {
        return (mCM.getState() == PhoneConstants.State.IDLE);
    }

    public boolean isSimPinEnabled() {
        enforceReadPermission();
        return (PhoneGlobals.getInstance().isSimPinEnabled());
    }

    public boolean supplyPin(String pin) {
        enforceModifyPermission();
        final UnlockSim checkSimPin = new UnlockSim(mPhone.getIccCard());
        checkSimPin.start();
        return checkSimPin.unlockSim(null, pin);
    }

    public boolean supplyPuk(String puk, String pin) {
        enforceModifyPermission();
        final UnlockSim checkSimPuk = new UnlockSim(mPhone.getIccCard());
        checkSimPuk.start();
        return checkSimPuk.unlockSim(puk, pin);
    }

    /**
     * Helper thread to turn async call to {@link SimCard#supplyPin} into
     * a synchronous one.
     */
    private static class UnlockSim extends Thread {

        private final IccCard mSimCard;

        private boolean mDone = false;
        private boolean mResult = false;

        // For replies from SimCard interface
        private Handler mHandler;

        // For async handler to identify request type
        private static final int SUPPLY_PIN_COMPLETE = 100;

        public UnlockSim(IccCard simCard) {
            mSimCard = simCard;
        }

        @Override
        public void run() {
            Looper.prepare();
            synchronized (UnlockSim.this) {
                mHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        AsyncResult ar = (AsyncResult) msg.obj;
                        switch (msg.what) {
                            case SUPPLY_PIN_COMPLETE:
                                Log.d(LOG_TAG, "SUPPLY_PIN_COMPLETE");
                                synchronized (UnlockSim.this) {
                                    mResult = (ar.exception == null);
                                    mDone = true;
                                    UnlockSim.this.notifyAll();
                                }
                                break;
                        }
                    }
                };
                UnlockSim.this.notifyAll();
            }
            Looper.loop();
        }

        /*
         * Use PIN or PUK to unlock SIM card
         *
         * If PUK is null, unlock SIM card with PIN
         *
         * If PUK is not null, unlock SIM card with PUK and set PIN code
         */
        synchronized boolean unlockSim(String puk, String pin) {

            while (mHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            Message callback = Message.obtain(mHandler, SUPPLY_PIN_COMPLETE);

            if (puk == null) {
                mSimCard.supplyPin(pin, callback);
            } else {
                mSimCard.supplyPuk(puk, pin, callback);
            }

            while (!mDone) {
                try {
                    Log.d(LOG_TAG, "wait for done");
                    wait();
                } catch (InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }
            Log.d(LOG_TAG, "done");
            return mResult;
        }
    }

    public void updateServiceLocation() {
        // No permission check needed here: this call is harmless, and it's
        // needed for the ServiceState.requestStateUpdate() call (which is
        // already intentionally exposed to 3rd parties.)
        mPhone.updateServiceLocation();
    }

    public boolean isRadioOn() {
        return mPhone.getServiceState().getState() != ServiceState.STATE_POWER_OFF;
    }

    public void toggleRadioOnOff() {
        enforceModifyPermission();
        mPhone.setRadioPower(!isRadioOn());
    }
    public boolean setRadio(boolean turnOn) {
        enforceModifyPermission();
        if ((mPhone.getServiceState().getState() != ServiceState.STATE_POWER_OFF) != turnOn) {
            toggleRadioOnOff();
        }
        return true;
    }

    public boolean enableDataConnectivity() {
        enforceModifyPermission();
        ConnectivityManager cm =
                (ConnectivityManager)mApp.getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.setMobileDataEnabled(true);
        return true;
    }

    public int enableApnType(String type) {
//aurora add zhouxiaobing 20131226 start
    	Log.v("AuroraContactImportExportActivity", "enableApnType type="+type);
       if(type!=null&&type.equalsIgnoreCase("SIMORUSIM"))
	   {
    	   try {
    		   Class<?> sPolicy=null;              
    	        sPolicy=Class.forName("com.android.internal.telephony.PhoneBase");                  
    		    Method method=sPolicy.getMethod("getCurrentUiccAppType");
    		    Object o=method.invoke(((PhoneBase)(((PhoneProxy)mPhone).getActivePhone())));
    		    String s=o.toString();
    		    Log.v("AuroraContactImportExportActivity", "s="+s);
    		    if(s.equalsIgnoreCase("APPTYPE_UNKNOWN"))
    		    {
    		    	return 0;
    		    }
    		    else if(s.equalsIgnoreCase("APPTYPE_SIM"))
    		    {
    		    	return 1;
    		    }
    		    else if(s.equalsIgnoreCase("APPTYPE_USIM"))
    		    {
    		    	return 2;
    		    }
    		    else if(s.equalsIgnoreCase("APPTYPE_RUIM"))
    		    {
    		    	return 3;
    		    }
    		    else if(s.equalsIgnoreCase("APPTYPE_CSIM"))
    		    {
    		    	return 4;
    		    }
    		    else if(s.equalsIgnoreCase("APPTYPE_ISIM"))
    		    {
    		    	return 5;
    		    }
    		   //GnITelephony.setUiccType();  
	           return 0;//GnITelephony.setUiccType(((PhoneBase)(((PhoneProxy)mPhone).getActivePhone())));//((PhoneBase)(((PhoneProxy)mPhone).getActivePhone())).getCurrentUiccAppType().ordinal();//AppType
	       } catch (Exception e) {
	    	   e.printStackTrace();
	       }
       }
       else
////aurora add zhouxiaobing 20131226 end
       {
        enforceModifyPermission();
        return mPhone.enableApnType(type);
       }
       return 0;
    }

    public int disableApnType(String type) {
        enforceModifyPermission();
        return mPhone.disableApnType(type);
    }

    public boolean disableDataConnectivity() {
        enforceModifyPermission();
        ConnectivityManager cm =
                (ConnectivityManager)mApp.getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.setMobileDataEnabled(false);
        return true;
    }

    public boolean isDataConnectivityPossible() {
        return mPhone.isDataConnectivityPossible();
    }

    public boolean handlePinMmi(String dialString) {
        enforceModifyPermission();
        return (Boolean) sendRequest(CMD_HANDLE_PIN_MMI, dialString);
    }

    public void cancelMissedCallsNotification() {
        enforceModifyPermission();
        mApp.notificationMgr.cancelMissedCallNotification();
    }

    public int getCallState() {
        return DefaultPhoneNotifier.convertCallState(mCM.getState());
    }

    public int getDataState() {
        return DefaultPhoneNotifier.convertDataState(mPhone.getDataConnectionState());
    }

    public int getDataActivity() {
        return DefaultPhoneNotifier.convertDataActivityState(mPhone.getDataActivityState());
    }

    @Override
    public Bundle getCellLocation() {
        try {
            mApp.enforceCallingOrSelfPermission(
                android.Manifest.permission.ACCESS_FINE_LOCATION, null);
        } catch (SecurityException e) {
            // If we have ACCESS_FINE_LOCATION permission, skip the check for ACCESS_COARSE_LOCATION
            // A failure should throw the SecurityException from ACCESS_COARSE_LOCATION since this
            // is the weaker precondition
            mApp.enforceCallingOrSelfPermission(
                android.Manifest.permission.ACCESS_COARSE_LOCATION, null);
        }

        if (checkIfCallerIsSelfOrForegoundUser()) {
            if (DBG_LOC) log("getCellLocation: is active user");
            Bundle data = new Bundle();
            mPhone.getCellLocation().fillInNotifierBundle(data);
            return data;
        } else {
            if (DBG_LOC) log("getCellLocation: suppress non-active user");
            return null;
        }
    }

    @Override
    public void enableLocationUpdates() {
        mApp.enforceCallingOrSelfPermission(
                android.Manifest.permission.CONTROL_LOCATION_UPDATES, null);
        mPhone.enableLocationUpdates();
    }

    @Override
    public void disableLocationUpdates() {
        mApp.enforceCallingOrSelfPermission(
                android.Manifest.permission.CONTROL_LOCATION_UPDATES, null);
        mPhone.disableLocationUpdates();
    }


    public List<NeighboringCellInfo> getNeighboringCellInfo() {
        try {
            mApp.enforceCallingOrSelfPermission(
                    android.Manifest.permission.ACCESS_FINE_LOCATION, null);
        } catch (SecurityException e) {
            // If we have ACCESS_FINE_LOCATION permission, skip the check
            // for ACCESS_COARSE_LOCATION
            // A failure should throw the SecurityException from
            // ACCESS_COARSE_LOCATION since this is the weaker precondition
            mApp.enforceCallingOrSelfPermission(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION, null);
        }

        if (checkIfCallerIsSelfOrForegoundUser()) {
            if (DBG_LOC) log("getNeighboringCellInfo: is active user");

            ArrayList<NeighboringCellInfo> cells = null;

            try {
                cells = (ArrayList<NeighboringCellInfo>) sendRequest(
                        CMD_HANDLE_NEIGHBORING_CELL, null);
            } catch (RuntimeException e) {
                Log.e(LOG_TAG, "getNeighboringCellInfo " + e);
            }
            return cells;
        } else {
            if (DBG_LOC) log("getNeighboringCellInfo: suppress non-active user");
            return null;
        }
    }


    @Override
    public List<CellInfo> getAllCellInfo() {
        try {
            mApp.enforceCallingOrSelfPermission(
                android.Manifest.permission.ACCESS_FINE_LOCATION, null);
        } catch (SecurityException e) {
            // If we have ACCESS_FINE_LOCATION permission, skip the check for ACCESS_COARSE_LOCATION
            // A failure should throw the SecurityException from ACCESS_COARSE_LOCATION since this
            // is the weaker precondition
            mApp.enforceCallingOrSelfPermission(
                android.Manifest.permission.ACCESS_COARSE_LOCATION, null);
        }

        if (checkIfCallerIsSelfOrForegoundUser()) {
            if (DBG_LOC) log("getAllCellInfo: is active user");
            return mPhone.getAllCellInfo();
        } else {
            if (DBG_LOC) log("getAllCellInfo: suppress non-active user");
            return null;
        }
    }

    //
    // Internal helper methods.
    //

    private boolean checkIfCallerIsSelfOrForegoundUser() {
        boolean ok;

        boolean self = Binder.getCallingUid() == Process.myUid();
        if (!self) {
            // Get the caller's user id then clear the calling identity
            // which will be restored in the finally clause.
            int callingUser = UserHandle.getCallingUserId();
            long ident = Binder.clearCallingIdentity();

            try {
                // With calling identity cleared the current user is the foreground user.
                int foregroundUser = ActivityManager.getCurrentUser();
                ok = (foregroundUser == callingUser);
                if (DBG_LOC) {
                    log("checkIfCallerIsSelfOrForegoundUser: foregroundUser=" + foregroundUser
                            + " callingUser=" + callingUser + " ok=" + ok);
                }
            } catch (Exception ex) {
                if (DBG_LOC) loge("checkIfCallerIsSelfOrForegoundUser: Exception ex=" + ex);
                ok = false;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            if (DBG_LOC) log("checkIfCallerIsSelfOrForegoundUser: is self");
            ok = true;
        }
        if (DBG_LOC) log("checkIfCallerIsSelfOrForegoundUser: ret=" + ok);
        return ok;
    }

    /**
     * Make sure the caller has the READ_PHONE_STATE permission.
     *
     * @throws SecurityException if the caller does not have the required permission
     */
    private void enforceReadPermission() {
        mApp.enforceCallingOrSelfPermission(android.Manifest.permission.READ_PHONE_STATE, null);
    }

    /**
     * Make sure the caller has the MODIFY_PHONE_STATE permission.
     *
     * @throws SecurityException if the caller does not have the required permission
     */
    private void enforceModifyPermission() {
        mApp.enforceCallingOrSelfPermission(android.Manifest.permission.MODIFY_PHONE_STATE, null);
    }

    /**
     * Make sure the caller has the CALL_PHONE permission.
     *
     * @throws SecurityException if the caller does not have the required permission
     */
    private void enforceCallPermission() {
        mApp.enforceCallingOrSelfPermission(android.Manifest.permission.CALL_PHONE, null);
    }


    private String createTelUrl(String number) {
        if (TextUtils.isEmpty(number)) {
            return null;
        }

        StringBuilder buf = new StringBuilder("tel:");
        buf.append(number);
        return buf.toString();
    }

    private void log(String msg) {
        Log.d(LOG_TAG, "[PhoneIntfMgr] " + msg);
    }

    private void loge(String msg) {
        Log.e(LOG_TAG, "[PhoneIntfMgr] " + msg);
    }

    public int getActivePhoneType() {
        return mPhone.getPhoneType();
    }

    /**
     * Returns the CDMA ERI icon index to display
     */
    public int getCdmaEriIconIndex() {
        return mPhone.getCdmaEriIconIndex();
    }

    /**
     * Returns the CDMA ERI icon mode,
     * 0 - ON
     * 1 - FLASHING
     */
    public int getCdmaEriIconMode() {
        return mPhone.getCdmaEriIconMode();
    }

    /**
     * Returns the CDMA ERI text,
     */
    public String getCdmaEriText() {
        return mPhone.getCdmaEriText();
    }

    /**
     * Returns true if CDMA provisioning needs to run.
     */
    public boolean needsOtaServiceProvisioning() {
        return mPhone.needsOtaServiceProvisioning();
    }

    /**
     * Returns the unread count of voicemails
     */
    public int getVoiceMessageCount() {
        return mPhone.getVoiceMessageCount();
    }

    /**
     * Returns the network type
     */
    public int getNetworkType() {
        return mPhone.getServiceState().getNetworkType();
    }

    /**
     * @return true if a ICC card is present
     */
    public boolean hasIccCard() {
    	TelephonyManager tm=TelephonyManager.getDefault();
    	int state=tm.getSimState();
    	Log.v("PhoneAnim", "state="+state);
    	return state!=TelephonyManager.SIM_STATE_ABSENT&&state!=TelephonyManager.SIM_STATE_UNKNOWN;
        //return /*mApp.isSimInsert();*/mPhone.getIccCard().hasIccCard();//aurora change zhouxiaobing 20140210
    }

    /**
     * Return if the current radio is LTE on CDMA. This
     * is a tri-state return value as for a period of time
     * the mode may be unknown.
     *
     * @return {@link Phone#LTE_ON_CDMA_UNKNOWN}, {@link Phone#LTE_ON_CDMA_FALSE}
     * or {@link PHone#LTE_ON_CDMA_TRUE}
     */
    public int getLteOnCdmaMode() {
        return mPhone.getLteOnCdmaMode();
    }
	

    // NFC SEEK start
    /**
     * Returns the response APDU for a command APDU sent to a logical channel
     */
 
    public String transmitIccLogicalChannel(int cla, int command, int channel,
            int p1, int p2, int p3, String data)
    {
         return "haha";
	}

    /**
     * Returns the response APDU for a command APDU sent to a logical channel for Gemini-Card
     */
   public  String transmitIccLogicalChannelGemini(int cla, int command, int channel,
            int p1, int p2, int p3, String data, int simId)
    {
        return "haha";
	}
    /**
     * Returns the response APDU for a command APDU sent to the basic channel
     */
    public String transmitIccBasicChannel(int cla, int command,
            int p1, int p2, int p3, String data)
    	{
    	  return "haha";
    	}

    /**
     * Returns the response APDU for a command APDU sent to the basic channel for Gemini-Card
     */
    public String transmitIccBasicChannelGemini(int cla, int command,
            int p1, int p2, int p3, String data, int simId)
    	{
    	  return "haha";
    	}

    /**
     * Returns the channel id of the logical channel,
     * Returns 0 on error.
     */
    //MTK 
      public int openIccLogicalChannel(String AID)
	{
	 return 1;
	}
    //change for 1+ 
//    public int[] openIccLogicalChannel(String AID)
//    	{
//    	 return null;
//   	}

    /**
     * Returns the channel id of the logical channel for Gemini-Card,
     * Returns 0 on error.
     */
   public  int openIccLogicalChannelGemini(String AID, int simId)
    	{
    	  return 1;
    	}

    /**
     * Return true if logical channel was closed successfully
     */
   public  boolean closeIccLogicalChannel(int channel)
    	{
    	  return true;
    	}

    /**
     * Return true if logical channel was closed successfully for Gemini-Card
     */
   public  boolean closeIccLogicalChannelGemini(int channel, int simId)
    	{
    	  return true;
    	}

    /**
     * Returns the error code of the last error occured.
     * Currently only used for openIccLogicalChannel
     */
    public int getLastError()
    	{
        return 0;
	}

    /**
     * Returns the error code of the last error occured for Gemini-Card.
     * Currently only used for openIccLogicalChannel
     */
    public int getLastErrorGemini(int simId)
    	{
            return 0;
	}
    
    /**
     * Returns the response APDU for a command APDU sent through SIM_IO
     */
    public byte[] transmitIccSimIO(int fileID, int command,
                                      int p1, int p2, int p3, String filePath)
    {
        
		return null;

	}

    /**
     * Returns the response APDU for a command APDU sent through SIM_IO for Gemini-Card
     */
    public byte[] transmitIccSimIOGemini(int fileID, int command,
                                      int p1, int p2, int p3, String filePath, int simId)
    {
        
		return null;

	}

    /**
     * Returns SIM's ATR in hex format
     */
   public String getIccATR()
    {
          return "haha";
	}

    /**
     * Returns SIM's ATR in hex format for Gemini-Card
     */
    public String getIccATRGemini(int simId)
    {
          return "haha";
	}


    //MTK-START [mtk04070][111117][ALPS00093395]MTK proprietary methods
    /**
     * Check if the phone is idle for voice call only.
     * @return true if the phone state is for voice call only.
     */
		public boolean isVoiceIdle(){
			PhoneConstants.State state;
			state = mPhone.getState();	// IDLE, RINGING, or OFFHOOK					
			return (state == PhoneConstants.State.IDLE);	
		}


    /**
     * Returns the IccCard type. Return "SIM" for SIM card or "USIM" for USIM card.
     */
    public String getIccCardType() {
			String simType = "unknown";

		//aurora chagne zhouxiaobing 20131224 start	
			int at=enableApnType("SIMORUSIM");//((PhoneBase)(((PhoneProxy)mPhone).getActivePhone())).getCurrentUiccAppType().ordinal();//AppType
			switch(at)
			{
			case 1://APPTYPE_SIM:
				simType="SIM";
				break;
			case 2://APPTYPE_USIM:
				simType="USIM";
				break;
			case 3://APPTYPE_RUIM:
				simType="UIM";
				break;
			case 4://APPTYPE_CSIM:
				simType="SIM";
				break;
			case 5://APPTYPE_ISIM:
				simType="SIM";
				break;
			
			}
			//GnITelephony.setUiccType(); 
			//simType=SystemProperties.get("persist.iuni.sim.type");
		//aurora chagne zhouxiaobing 20131224 end		
		  return simType;
		}

	
    /**
     * Do sim authentication and return the raw data of result.
     * Returns the hex format string of auth result.
     * <p> random string in hex format
     */
   public  String simAuth(String strRand)
	{
          return "haha";
	}

    /**
     * Do usim authentication and return the raw data of result.
     * Returns the hex format string of auth result.
     * <p> random string in hex format
     */
    public  String uSimAuth(String strRand, String strAutn)
	{
          return "haha";
	}

    /**
     * Shutdown Radio
     */
   public boolean setRadioOff()
    {
      return true;
	}

    public int getPreciseCallState() {
        return DefaultPhoneNotifier.convertCallState(mCM.getState());
    }

    /**
     * Return ture if the ICC card is a test card
     */
   public boolean isTestIccCard()
    {
            String imsi = mPhone.getSubscriberId();
            if (imsi != null) {
                return imsi.substring(0, 5).equals("00101");
            } else {
                return false;
            }
        }

    /**
    * Return true if the FDN of the ICC card is enabled
    */
   public boolean isFDNEnabled()
    {
      return mPhone.getIccCard().getIccFdnEnabled();
	}

    /**
     * refer to dial(String number);
     */
   public  void dialGemini(String number, int simId)
    {
         dial(number);   
	}
    
    /**
     * refer to call(String number);
     */
   public  void callGemini(String number, int simId)
    {
        call(number);
	}

    /**
     * refer to showCallScreen();
     */
    public boolean showCallScreenGemini(int simId)
    {
         return  showCallScreen();
	}

    /**
     * refer to showCallScreenWithDialpad(boolean showDialpad)
     */
   public  boolean showCallScreenWithDialpadGemini(boolean showDialpad, int simId)
    {
       return showCallScreenWithDialpad(showDialpad);
	}

    /**
     * refer to endCall().
     */
   public  boolean endCallGemini(int simId)
    {
       return endCall();
	}

    /**
     * refer to answerRingingCall();
     */
   public  void answerRingingCallGemini(int simId)
    {
       answerRingingCall();
	}

    /**
     * refer to silenceRinger();
     */
   public  void silenceRingerGemini(int simId)
    {
       silenceRinger();

	}

    /**
     * refer to isOffhook().
     */
   public  boolean isOffhookGemini(int simId)
    {
       return isOffhook();
	}

    /**
     * refer to isRinging().
     */
   public  boolean isRingingGemini(int simId)
    {
       return isRinging();
	}

    /**
     * refer to isIdle().
     */
   public  boolean isIdleGemini(int simId)
    {

       return isIdle();
	}
    
    public int getPendingMmiCodesGemini(int simId)
    {
      return 0;
	}
    
    /**
     * refer to cancelMissedCallsNotification();
     */
   public  void cancelMissedCallsNotificationGemini(int simId)
    	{

        cancelMissedCallsNotification();   
	}

    /**
     * refer to getCallState();
     */
  public    int getCallStateGemini(int simId)
    	{
      return getCallState();
	}
     
    /**
     * refer to getActivePhoneType();
     */
    public  int getActivePhoneTypeGemini(int simId)
    	{
         return getActivePhoneType();
	}
    
    /**
     * Check to see if the radio is on or not.
     * @return returns true if the radio is on.
     */
   public  boolean isRadioOnGemini(int simId)
    {
       return isRadioOn();
	}
    
    /**
     * Supply a pin to unlock the SIM.  Blocks until a result is determined.
     * @param pin The pin to check.
     * @return whether the operation was a success.
     */
   public  boolean supplyPinGemini(String pin, int simId)
    {
         return supplyPin(pin);
	}
    
    /**
     * Supply a PUK code to unblock the SIM pin lock.  Blocks until a result is determined.
	 * @param puk The PUK code
     * @param pin The pin to check.
     * @return whether the operation was a success.
     */
  public   boolean supplyPukGemini(String puk, String pin, int simId)
    {

        return supplyPuk(puk,pin);
	}

    /**
     * Handles PIN MMI commands (PIN/PIN2/PUK/PUK2), which are initiated
     * without SEND (so <code>dial</code> is not appropriate).
     *
     * @param dialString the MMI command to be executed.
     * @return true if MMI command is executed.
     */
  public   boolean handlePinMmiGemini(String dialString, int simId)
    {

          return handlePinMmi(dialString);
	}

    /**
     * Returns the IccCard type of Gemini phone. Return "SIM" for SIM card or "USIM" for USIM card.
     */
   public  String getIccCardTypeGemini(int simId)
   {
      return getIccCardType();
	}

    /**
     * Do sim authentication for gemini and return the raw data of result.
     * Returns the hex format string of auth result.
     * <p> random string in hex format
     * <p> int for simid
     */
  public   String simAuthGemini(String strRand, int simId)
    {
        return simAuth(strRand);
	}

    /**
     * Do usim authentication for gemini and return the raw data of result.
     * Returns the hex format string of auth result.
     * <p> random string in hex format
     * <p> int for simid
     */
  public   String uSimAuthGemini(String strRand, String strAutn, int simId)
    {
        return uSimAuth(strRand,strAutn);
	}

    /**
     * Request to update location information in service state
     */
  public   void updateServiceLocationGemini(int simId)
    	{

        updateServiceLocation();
	}

    /**
     * Enable location update notifications.
     */
   public  void enableLocationUpdatesGemini(int simId)
    	{
         enableLocationUpdates();
	}

    /**
     * Disable location update notifications.
     */
   public  void disableLocationUpdatesGemini(int simId)
    	{
       disableLocationUpdates();
	}
    

  public   Bundle getCellLocationGemini(int simId)
  	{
       return getCellLocation();
  }

    /**
     * Returns the neighboring cell information of the device.
     */
  public   List<NeighboringCellInfo> getNeighboringCellInfoGemini(int simId)
    {
       return getNeighboringCellInfo();
	}
 
    /**
    * Returns true if SIM card inserted
     * This API is valid even if airplane mode is on
    */
  public   boolean isSimInsert(int simId)
    {
       return false;
	}

    /**
    * Set GPRS connection type, ALWAYS/WHEN_NEEDED
    */
  public   void setGprsConnType(int type, int simId)
    	{

	}

    /**
    * Set GPRS transfer type, Call prefer/Data prefer
    */
  public   void setGprsTransferType(int type)
    	{

	}
  public   void setGprsTransferTypeGemini(int type, int simId)
  	{
  	  setGprsTransferType(type);
  	}

    /*Add by mtk80372 for Barcode number*/
  public   void getMobileRevisionAndIMEI(int type, Message message)
    	{
    	}

    /*Add by mtk80372 for Barcode number*/
  public   String getSN()
    	{
    	 return "haha";
    	}
    /**
    * Set default phone
    */
  public   void setDefaultPhone(int simId)
    {

	}

    /**
      * Returns the network type
      */
  public   int getNetworkTypeGemini(int simId)
    	{
       return getNetworkType();
	}
    
  public   boolean hasIccCardGemini(int simId)
  	{
        return hasIccCard();
  }

  public   int getSimState(int simId)
  	{
      return 0;
  }

  public   String getSimOperator(int simId)
  	{
    return SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC);
  }
    
  public   String getSimOperatorName(int simId)
  	{
       return SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_ALPHA);
  }

  public   String getSimCountryIso(int simId)
  	{
       return SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_ISO_COUNTRY);
  }

  public   IPhoneSubInfo getSubscriberInfo(int simId)
  	{
       return IPhoneSubInfo.Stub.asInterface(ServiceManager.getService("iphonesubinfo"));
  }

   public  String getSimSerialNumber(int simId)
   	{
             try {
            return getSubscriberInfo(simId).getIccSerialNumber();
        } catch (RemoteException ex) {
            return null;
        } catch (NullPointerException ex) {
            // This could happen before phone restarts due to crashing
            return null;
        }
   }

   public  String getSubscriberId(int simId)
   	{
              try {
            return getSubscriberInfo(simId).getSubscriberId();
        } catch (RemoteException ex) {
            return null;
        } catch (NullPointerException ex) {
            // This could happen before phone restarts due to crashing
            return null;
        }
   }

  public   String getLine1Number(int simId)
  	{
                try {
            return getSubscriberInfo(simId).getLine1Number();
        } catch (RemoteException ex) {
            return null;
        } catch (NullPointerException ex) {
            // This could happen before phone restarts due to crashing
            return null;
        }
  }

  public   String getLine1AlphaTag(int simId)
  	{

  try {
	  return getSubscriberInfo(simId).getLine1AlphaTag();
  } catch (RemoteException ex) {
	  return null;
  } catch (NullPointerException ex) {
	  // This could happen before phone restarts due to crashing
	  return null;
  }

  }

  public   String getVoiceMailNumber(int simId)
  	{
	  try {
		  return getSubscriberInfo(simId).getVoiceMailNumber();
	  } catch (RemoteException ex) {
		  return null;
	  } catch (NullPointerException ex) {
		  // This could happen before phone restarts due to crashing
		  return null;
	  }

  }

  public   String getVoiceMailAlphaTag(int simId)
  	{
         try {
            return getSubscriberInfo(simId).getVoiceMailAlphaTag();
        } catch (RemoteException ex) {
            return null;
        } catch (NullPointerException ex) {
            // This could happen before phone restarts due to crashing
            return null;
        }
  }

  public   boolean isTestIccCardGemini(int simId)
  	{
        return isTestIccCard();
  }
    
  public   int enableDataConnectivityGemini(int simId)
  	{
       return 1;
  }
    
  public   int enableApnTypeGemini(String type, int simId)
  	{
       return 1;
  }
    
 public    int disableApnTypeGemini(String type, int simId)
 	{
       return disableApnType(type);
 }

 public    int disableDataConnectivityGemini(int simId)
 	{
       return 1;
 }

  public   boolean isDataConnectivityPossibleGemini(int simId)
  	{
       return isDataConnectivityPossible();
  }

  public   int getDataStateGemini(int simId)
  	{
      return getDataState();
  }

  public   int getDataActivityGemini(int simId)
  	{
     return getDataActivity();
  }

  public   int getVoiceMessageCountGemini(int simId)
  	{
      return getVoiceMessageCount();
  }

    /**
    * Return true if the FDN of the ICC card is enabled
    */
  public   boolean isFDNEnabledGemini(int simId)
    	{
      return isFDNEnabled();
	}

  public   boolean isVTIdle()
  	{
    return true;
  }

   /**
     *send BT SIM profile of Connect SIM
     * @param simId specify which SIM to connect
     * @param btRsp fetch the response data.
     * @return success or error code.
   */
  public  int btSimapConnectSIM(int simId,  BtSimapOperResponse btRsp)
   	{
        return 1;
   }

    /**
     *send BT SIM profile of Disconnect SIM
     * @param null
     * @return success or error code.
   */
  public  int btSimapDisconnectSIM()
    	{
        return 1;
	}

   /**
     *Transfer APDU data through BT SAP
     * @param type Indicate which transport protocol is the preferred one
     * @param cmdAPDU APDU data to transfer in hex character format    
     * @param btRsp fetch the response data.
     * @return success or error code.
   */	
public    int btSimapApduRequest(int type, String cmdAPDU, BtSimapOperResponse btRsp)
   	{
     return 1;
   }

    /**
     *send BT SIM profile of Reset SIM
     * @param type Indicate which transport protocol is the preferred one
     * @param btRsp fetch the response data.
     * @return success or error code.
   */
 public   int btSimapResetSIM(int type, BtSimapOperResponse btRsp)
    	{
        return 1;
	}

   /**
     *send BT SIM profile of Power On SIM
     * @param type Indicate which transport protocol is the preferred onet
     * @param btRsp fetch the response data.
     * @return success or error code.
   */	
 public   int btSimapPowerOnSIM(int type, BtSimapOperResponse btRsp)
   	{
     return 1;
   }

   /**
     *send BT SIM profile of PowerOff SIM
     * @return success or error code.
   */ 
 public   int btSimapPowerOffSIM()
   	{
       return 1;
   }

   /**
     *get the services state for default SIM
     * @return sim indicator state.    
     *
    */ 
 public   int getSimIndicatorState()
   	{
      return 1;
   }

   /**
     *get the services state for specified SIM
     * @param simId Indicate which sim(slot) to query
     * @return sim indicator state.
     *
    */ 
 public   int getSimIndicatorStateGemini(int simId)
   	{
       return getSimIndicatorState();
   }

   /**
     *get the network service state for default SIM
     * @return service state.    
     *
    */ 
 public   Bundle getServiceState()
   	{
       return null;
   }

   /**
     * get the network service state for specified SIM
     * @param simId Indicate which sim(slot) to query
     * @return service state.
     *
    */ 
 public   Bundle getServiceStateGemini(int simId)
   	{
      return getServiceState();
   }

    /**
     * @return true if phone book is ready.    
    */ 
 public   boolean isPhbReady()
    	{
    return  true;
	}

   /**
     * @param simId Indicate which sim(slot) to query
     * @return true if phone book is ready. 
     *
    */ 
  public  boolean isPhbReadyGemini(int simId)
   	{
     return isPhbReady();   
   }

   
 public   String getScAddressGemini( int simId)
 {
      return "haha";
 }
   
public    void setScAddressGemini( String scAddr,  int simId)
{
    
}

   /**
    * @return SMS default SIM. 
    */ 
 public   int getSmsDefaultSim()
   	{
     return 0;
   }

public    int get3GCapabilitySIM()
{
     return 0;
}
 public   boolean set3GCapabilitySIM(int simId)
 	{
       return false;
 }
 public   int aquire3GSwitchLock()
 	{
      return 1;
 }
 public   boolean release3GSwitchLock(int lockId)
 	{
      return true;
 }
public    boolean is3GSwitchLocked()
{
  return true;
}
 public   String getInterfaceName(String apnType)
 	{
   return "haha";
 }
public    String getIpAddress(String apnType)
 	{
   return "haha";
 }	
 public   String getGateway(String apnType)
 	 	{
   return "haha";
 }
 public   String getInterfaceNameGemini(String apnType, int slot)
 	 	{
   return "haha";
 }
public    String getIpAddressGemini(String apnType, int slot)
	 	{
   return "haha";
 }
public    String getGatewayGemini(String apnType, int slot)
	 	{
   return "haha";
 }
public    int[] getAdnStorageInfo(int simId)
{
  return null;
}
public    int cleanupApnTypeGemini(String apnType, int simId)
{
  return 1;
}
   //MTK-END [mtk04070][111117][ALPS00093395]MTK proprietary methods
   //MTK-START [mtk03851][111117]MTK proprietary methods
 public   void registerForSimModeChange(IBinder binder, int what)
   	{

   }
 public   void unregisterForSimModeChange(IBinder binder)
 	{
  
 }
 public   void setDataRoamingEnabledGemini(boolean enable, int simId)
 	{

 }
 public   void setRoamingIndicatorNeddedProperty(boolean property1, boolean property2)
 	{

 }

   /**
     * Get the count of missed call.
     *
     * @return Return the count of missed call. 
     */
 public    int getMissedCallCount()
   	{
	   return 0;//mApp.notificationMgr.getMissedCallCount();

   }

   /**
      Description : Adjust modem radio power for Lenovo SAR requirement.
	  AT command format: AT+ERFTX=<op>,<para1>,<para2>
	  Description : When <op>=1	 -->  TX power reduction
				    <para1>:  2G L1 reduction level, default is 0
				    <para2>:  3G L1 reduction level, default is 0
				    level scope : 0 ~ 64
      Arthur      : mtk04070
      Date        : 2012.01.09
      Return value: True for success, false for failure
    */
  public  boolean adjustModemRadioPower(int level_2G, int level_3G)
   	{
          return false;
   }

   /**
      Description      : Adjust modem radio power by band for Lenovo SAR requirement.
	  AT command format: AT+ERFTX=<op>,<rat>,<band>,<para1>...<paraX>
	  Description : <op>=3	 -->  TX power reduction by given band
                    <rat>    -->  1 for 2G, 2 for 3G
                    <band>   -->  2G or 3G band value
				    <para1>~<paraX> -->  Reduction level
				    level scope : 0 ~ 255
      Arthur      : mtk04070
      Date        : 2012.05.31
      Return value: True for success, false for failure
   */
 public   boolean adjustModemRadioPowerByBand(int rat, int band, int level)
   	{
   return false;
   }
   
   //MTK-END [mtk03851][111117]MTK proprietary methods

   // MVNO-API START
 public   String getSpNameInEfSpn()
   	{
       return "haha";
   }
public    String getSpNameInEfSpnGemini(int simId)
{
   return "haha";
}

 public   String isOperatorMvnoForImsi()
{
   return "haha";
}
 public   String isOperatorMvnoForImsiGemini(int simId)
{
   return "haha";
}

 public   String isOperatorMvnoForEfPnn()
{
   return "haha";
}
  public  String isOperatorMvnoForEfPnnGemini(int simId)
{
   return "haha";
}

 public   boolean isIccCardProviderAsMvno()
 	{
     return false;
 }
 public   boolean isIccCardProviderAsMvnoGemini(int simId)
 	{
     return false;
 }
   // MVNO-API END

    /**
     * Gemini
     * Returns the alphabetic name of current registered operator.
     * <p>
     * Availability: Only when user is registered to a network. Result may be
     * unreliable on CDMA networks (use {@link #getPhoneType()} to determine if
     * on a CDMA network).
     */
 public    String getNetworkOperatorNameGemini(int simId)
{
   return "haha";
}

    /**
     * Gemini
     * Returns the numeric name (MCC+MNC) of current registered operator.
     * <p>
     * Availability: Only when user is registered to a network. Result may be
     * unreliable on CDMA networks (use {@link #getPhoneType()} to determine if
     * on a CDMA network).
     */
  public   String getNetworkOperatorGemini(int simId)
{
   return "haha";
}

    /**
     * Gemini
     * Returns true if the device is considered roaming on the current
     * network, for GSM purposes.
     * <p>
     * Availability: Only when user registered to a network.
     */
   public  boolean isNetworkRoamingGemini(int simId)
    	{
  return false;
	}

    /**
     * Gemini
     * Returns the ISO country code equivilent of the current registered
     * operator's MCC (Mobile Country Code).
     * <p>
     * Availability: Only when user is registered to a network. Result may be
     * unreliable on CDMA networks (use {@link #getPhoneType()} to determine if
     * on a CDMA network).
     */
 public    String getNetworkCountryIsoGemini(int simId)
    	{
      return "haha";
	}
 
 
//samsung interface anruro add zhouxiaobing 20131018
    public byte[] getSelectResponse() {

        return null;
    }
    public int getDataServiceState() {
        return 0;//mPhone.getDataServiceState();
    }
//samsung interface anruro add zhouxiaobing 20131018
//htc interface aurora add zhouxiaobing 20131126
    public int  getGprsState()
{
    return 0;
}

public boolean htcModemLinkOn()
{
  return false;
}

public int getHtcNetworkType(String[] s)
{
  return 0;
}
//htc interface aurora add zhouxiaobing 20131126
//aurora add zhouxiaobing 20131125 start
    public void setCellInfoListRate(int rateInMillis) {
        
    }
    public int getVoiceNetworkType() {
        return 0;
    }
    public int getDataNetworkType() {
//        return getNetworkType();
        Class<?> sPolicy=null;
        try{
        	sPolicy=Class.forName("android.telephony.ServiceState");
        	Method method=sPolicy.getMethod("getDataNetworkType");
        	return ((Integer)method.invoke(mPhone.getServiceState())).intValue();

        }catch(Exception e)
        {
        }
        return 0;
    }
public List<NeighboringCellInfo> getNeighboringCellInfo(String callingPackage) 
{
     return  getNeighboringCellInfo();
}
    public boolean setRadioPower(boolean turnOn) {
        enforceModifyPermission();
        mPhone.setRadioPower(turnOn);
        return true;
    }
public void call(String callingPackage, String number)
{
     call(number);   
}
    public int getIccPin1RetryCount() {
       // return mPhone.getIccCard().getIccPin1RetryCount();
			  if(!(Build.VERSION.SDK_INT>=18&&SystemProperties.get("ro.product.board").contains("MSM")))    
				  	{           
				  	return 0;  
					}  
		     else  
				  	{    
						try{                  
							Class<?> sPolicy=null;                  
						    sPolicy=Class.forName("com.android.internal.telephony.IccCard");                  
						    Method method=sPolicy.getMethod("getIccPin1RetryCount");                  
						    return ((Integer)method.invoke(mPhone.getIccCard())).intValue();      }catch(Exception e){}  
					} 
			 return 0;
    }
    public int supplyPukReportResult(String puk, String pin) {
        return 0;
    }	
	public int supplyPinReportResult(String pin) {
        return 0;
    }
//aurora add zhouxiaobing 20131125 end
	
	//aurora add liguangyu 20131208 start
	public boolean getPowerKeyFlag() {
		return false;
	}
	
	public void setPowerKeyFlag() {}
	//aurora add liguangyu 20131208 end
	
	//aurora add liguangyu 20140328 start
	public void invokeOemRilRequestRaw(byte[] data) {
		
	} 
	//aurora add liguangyu 20140328 end
	
	
	//aurora add liguangyu 20140415 for 4225 start
	public boolean setTransmitPower(int power) {
		return true;
	} 
    //aurora add liguangyu 20140415 for 4225 end
	
	//aurora add liguangyu 20140606 for BUG #5420 start
	public void setVoiceCallVolume(float volume) {	
	}
	//aurora add liguangyu 20140606 for BUG #5420 end
	
	public boolean getAudioRecordState() {
        PhoneRecorder phoneRecorder = PhoneRecorder.getInstance(PhoneGlobals.getInstance());
        return phoneRecorder.ismFlagRecord();
		
	}
	

	public boolean getOppoAudioRecordStatus() {
		return getAudioRecordState();
	}
	
	
	public void oppoChangeIccLockPassword(String oldPin, String newPin1, Message callBack) {
	       IccCard iccCardInterface = mPhone.getIccCard();
           iccCardInterface.changeIccLockPassword(oldPin, newPin1, callBack);        
	}
	
	public boolean  oppoGetIccLockEnabled() {
	      IccCard iccCardInterface = mPhone.getIccCard();
         return  iccCardInterface.getIccLockEnabled();
	}
	
	public int  oppoGetIccPin1RetryCount() {
		return getIccPin1RetryCount();
	}
	
	public boolean oppoIsDialing() {
		return mCM.getActiveFgCallState().isDialing();
	}
	
	public void oppoRestartRinger() {
		PhoneGlobals.getInstance().ringer.ring();
	}
	
	public void oppoSetIccLockEnabled(boolean enable, String password, Message callBack) {
	       IccCard iccCardInterface = mPhone.getIccCard();
           iccCardInterface.setIccLockEnabled(enable, password, callBack);
	}
	
	public int oppoSimPhonebookIsReady() {
		return 0;
	}
	
	
}

