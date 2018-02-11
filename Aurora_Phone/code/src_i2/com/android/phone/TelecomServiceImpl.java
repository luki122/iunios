/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2014 The Android Open Source Project
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

import android.Manifest;
import android.app.AppOpsManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.telecom.CallState;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;


import com.android.internal.telephony.DefaultPhoneNotifier;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.PhoneConstants;






import android.util.Log;



// TODO: Needed for move to system service: import com.android.internal.R;
import com.android.internal.telecom.ITelecomService;
import com.android.internal.util.IndentingPrintWriter;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;

/**
 * Implementation of the ITelecomm interface.
 */
public class TelecomServiceImpl extends ITelecomService.Stub {
    private static final String LOG_TAG = "TelecomServiceImpl";
    private static final boolean DBG = (PhoneGlobals.DBG_LEVEL >= 2);
    private static final String REGISTER_PROVIDER_OR_SUBSCRIPTION =
            "com.android.server.telecom.permission.REGISTER_PROVIDER_OR_SUBSCRIPTION";
    private static final String REGISTER_CONNECTION_MANAGER =
            "com.android.server.telecom.permission.REGISTER_CONNECTION_MANAGER";

    /** The context. */
    private Context mContext;
    
    PhoneGlobals mApp;
    Phone mPhone;
    CallManager mCM;

    /** ${inheritDoc} */
    @Override
    public IBinder asBinder() {
        return super.asBinder();
    }

 /**
     * A request object for use with {@link MainThreadHandler}. Requesters should wait() on the
     * request after sending. The main thread will notify the request when it is complete.
     */
    private static final class MainThreadRequest {
        /** The result of the request that is run on the main thread */
        public Object result;
    }

    /**
     * A handler that processes messages on the main thread in the phone process. Since many
     * of the Phone calls are not thread safe this is needed to shuttle the requests from the
     * inbound binder threads to the main thread in the phone process.
     */
    private final class MainThreadHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.obj instanceof MainThreadRequest) {
                MainThreadRequest request = (MainThreadRequest) msg.obj;
                Object result = null;
                switch (msg.what) {
                    case MSG_SILENCE_RINGER:
                        silenceRingerInternal();
                        break;
                    case MSG_SHOW_CALL_SCREEN:
                        showCallScreenInternal(false, false);
                        break;
                        /// M: Disconnect all calls @{
                    case MSG_DISCONNECT_ALL_CALLS:
                    case MSG_END_CALL:
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
                        if (DBG) Log.i(LOG_TAG,"CMD_END_CALL: " + (hungUp ? "hung up!" : "no call to hang up"));
                        request.result = hungUp;
                        // Wake up the requesting thread
                        synchronized (request) {
                            request.notifyAll();
                        }
                        break;
                    case MSG_ACCEPT_RINGING_CALL:
                    	answerRingingCallInternal();
                        break;
                    case MSG_CANCEL_MISSED_CALLS_NOTIFICATION:
                        mApp.notificationMgr.cancelMissedCallNotification();
                        break;
                    case MSG_IS_TTY_SUPPORTED:
                        result = false;
                        break;
                    case MSG_GET_CURRENT_TTY_MODE:
                        result = 0;
                        break;
                }

                if (result != null) {
                    request.result = result;
                    synchronized(request) {
                        request.notifyAll();
                    }
                }
            }
        }
    }

    /** Private constructor; @see init() */
    private static final String TAG = TelecomServiceImpl.class.getSimpleName();

    private static final String SERVICE_NAME = "telecomm";

    private static final int MSG_SILENCE_RINGER = 1;
    private static final int MSG_SHOW_CALL_SCREEN = 2;
    private static final int MSG_END_CALL = 3;
    private static final int MSG_ACCEPT_RINGING_CALL = 4;
    private static final int MSG_CANCEL_MISSED_CALLS_NOTIFICATION = 5;
    private static final int MSG_IS_TTY_SUPPORTED = 6;
    private static final int MSG_GET_CURRENT_TTY_MODE = 7;
    /// M: Disconnect all calls @{
    private static final int MSG_DISCONNECT_ALL_CALLS = 8;
    /// @}

    /** The singleton instance. */
    private static TelecomServiceImpl sInstance;

    private final MainThreadHandler mMainThreadHandler = new MainThreadHandler();
    private final AppOpsManager mAppOpsManager;

    public TelecomServiceImpl(PhoneGlobals app, Phone phone) {
    	 mApp = app;
         mPhone = phone;
         mCM = PhoneGlobals.getInstance().mCM;
        mContext = app;
        mAppOpsManager = (AppOpsManager) mContext.getSystemService(Context.APP_OPS_SERVICE);
    }

    //
    // Implementation of the ITelecommService interface.
    //

    @Override
    public PhoneAccountHandle getDefaultOutgoingPhoneAccount(String uriScheme) {
   return null;
    }

    @Override
    public PhoneAccountHandle getUserSelectedOutgoingPhoneAccount() {
    	   return null;
    }

    @Override
    public void setUserSelectedOutgoingPhoneAccount(PhoneAccountHandle accountHandle) {
        enforceModifyPermission();
    }

    @Override
    public List<PhoneAccountHandle> getCallCapablePhoneAccounts() {
    	   return null;
    }

    @Override
    public List<PhoneAccountHandle> getPhoneAccountsSupportingScheme(String uriScheme) {
    	   return null;
    }

    @Override
    public List<PhoneAccountHandle> getPhoneAccountsForPackage(String packageName) {
    	   return null;
    }

    @Override
    public PhoneAccount getPhoneAccount(PhoneAccountHandle accountHandle) {
    	   return null;
    }

    @Override
    public int getAllPhoneAccountsCount() {
    	   return 1;
    }

    @Override
    public List<PhoneAccount> getAllPhoneAccounts() {
    	   return null;
    }

    @Override
    public List<PhoneAccountHandle> getAllPhoneAccountHandles() {
    	   return null;
    }

    @Override
    public PhoneAccountHandle getSimCallManager() {
    	   return null;
    }

    @Override
    public void setSimCallManager(PhoneAccountHandle accountHandle) {
        enforceModifyPermission();
    }

    @Override
    public List<PhoneAccountHandle> getSimCallManagers() {
    	   return null;
    }

    @Override
    public void registerPhoneAccount(PhoneAccount account) {
    
    }

    @Override
    public void unregisterPhoneAccount(PhoneAccountHandle accountHandle) {

    }

    @Override
    public void clearAccounts(String packageName) {
 
    }

    /**
     * @see android.telecom.TelecomManager#silenceRinger
     */
    @Override
    public void silenceRinger() {
        Log.d(LOG_TAG, "silenceRinger");
        enforceModifyPermission();
        sendRequestAsync(MSG_SILENCE_RINGER, 0);
    }
    
    private void silenceRingerInternal() {
        if ((mCM.getState() == PhoneConstants.State.RINGING)
            && mApp.notifier.isRinging()) {
            // Ringer is actually playing, so silence it.
            if (DBG) Log.i(LOG_TAG, "silenceRingerInternal: silencing...");
            mApp.notifier.silenceRinger();
        }
    }

    /**
     * @see android.telecom.TelecomManager#getDefaultPhoneApp
     */
    @Override
    public ComponentName getDefaultPhoneApp() {
    	   return null;
    }

    /**
     * @see android.telecom.TelecomManager#isInCall
     */
    @Override
    public boolean isInCall() {
        enforceReadPermission();
        return (mCM.getState() != PhoneConstants.State.IDLE);
    }

    /**
     * @see android.telecom.TelecomManager#isRinging
     */
    @Override
    public boolean isRinging() {
        enforceReadPermission();
        return (mCM.getState() == PhoneConstants.State.RINGING);
    }

    
    public boolean isIdle() {
        return (mCM.getState() == PhoneConstants.State.IDLE);
    }
    
    /**
     * @see TelecomManager#getCallState
     */
    @Override
    public int getCallState() {
        return DefaultPhoneNotifier.convertCallState(mCM.getState());
    }

    /**
     * @see android.telecom.TelecomManager#endCall
     */
    @Override
    public boolean endCall() {
        enforceModifyPermission();
        return (boolean) sendRequest(MSG_END_CALL);
    }

    /**
     * @see android.telecom.TelecomManager#acceptRingingCall
     */
    @Override
    public void acceptRingingCall() {
        enforceModifyPermission();
        sendRequestAsync(MSG_ACCEPT_RINGING_CALL, 0);
    }

    /**
     * @see android.telecom.TelecomManager#showInCallScreen
     */
    @Override
    public void showInCallScreen(boolean showDialpad) {
        enforceReadPermissionOrDefaultDialer();
        sendRequestAsync(MSG_SHOW_CALL_SCREEN, showDialpad ? 1 : 0);
    }

    /**
     * @see android.telecom.TelecomManager#cancelMissedCallsNotification
     */
    @Override
    public void cancelMissedCallsNotification() {
        enforceModifyPermissionOrDefaultDialer();
        sendRequestAsync(MSG_CANCEL_MISSED_CALLS_NOTIFICATION, 0);
    }

    /**
     * @see android.telecom.TelecomManager#handleMmi
     */
    @Override
    public boolean handlePinMmi(String dialString) {
        return mPhone.handlePinMmi(dialString);  
    }

    /**
     * @see android.telecom.TelecomManager#isTtySupported
     */
    @Override
    public boolean isTtySupported() {
        enforceReadPermission();
        return (boolean) sendRequest(MSG_IS_TTY_SUPPORTED);
    }

    /**
     * @see android.telecom.TelecomManager#getCurrentTtyMode
     */
    @Override
    public int getCurrentTtyMode() {
        enforceReadPermission();
        return (int) sendRequest(MSG_GET_CURRENT_TTY_MODE);
    }

    /**
     * @see android.telecom.TelecomManager#addNewIncomingCall
     */
    @Override
    public void addNewIncomingCall(PhoneAccountHandle phoneAccountHandle, Bundle extras) {

    }

    /**
     * @see android.telecom.TelecomManager#addNewUnknownCall
     */
    @Override
    public void addNewUnknownCall(PhoneAccountHandle phoneAccountHandle, Bundle extras) {
    
    }

    //
    // Supporting methods for the ITelecomService interface implementation.
    //

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



    private void enforcePhoneAccountModificationForPackage(String packageName) {
    }

    private void enforceReadPermissionOrDefaultDialer() {
        if (!isDefaultDialerCalling()) {
            enforceReadPermission();
        }
    }

    private void enforceModifyPermissionOrDefaultDialer() {
        if (!isDefaultDialerCalling()) {
            enforceModifyPermission();
        }
    }

    private void enforceCallingPackage(String packageName) {
        mAppOpsManager.checkPackage(Binder.getCallingUid(), packageName);
    }

    private void enforceConnectionServiceFeature() {
        enforceFeature(PackageManager.FEATURE_CONNECTION_SERVICE);
    }

    private void enforceRegisterProviderOrSubscriptionPermission() {
        enforcePermission(REGISTER_PROVIDER_OR_SUBSCRIPTION);
    }

    private void enforceRegisterConnectionManagerPermission() {
        enforcePermission(REGISTER_CONNECTION_MANAGER);
    }

    private void enforceReadPermission() {
        enforcePermission(Manifest.permission.READ_PHONE_STATE);
    }

    private void enforceModifyPermission() {
        enforcePermission(Manifest.permission.MODIFY_PHONE_STATE);
    }

    private void enforcePermission(String permission) {
        mContext.enforceCallingOrSelfPermission(permission, null);
    }

    private void enforceFeature(String feature) {
        PackageManager pm = mContext.getPackageManager();
        if (!pm.hasSystemFeature(feature)) {
            throw new UnsupportedOperationException(
                    "System does not support feature " + feature);
        }
    }

    private boolean isDefaultDialerCalling() {
return false;
    }

    private TelephonyManager getTelephonyManager() {
        return (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
    }

    private MainThreadRequest sendRequestAsync(int command, int arg1) {
        MainThreadRequest request = new MainThreadRequest();
        mMainThreadHandler.obtainMessage(command, arg1, 0, request).sendToTarget();
        return request;
    }

    /**
     * Posts the specified command to be executed on the main thread, waits for the request to
     * complete, and returns the result.
     */
    private Object sendRequest(int command) {
        if (Looper.myLooper() == mMainThreadHandler.getLooper()) {
            MainThreadRequest request = new MainThreadRequest();
            mMainThreadHandler.handleMessage(mMainThreadHandler.obtainMessage(command, request));
            return request.result;
        } else {
            MainThreadRequest request = sendRequestAsync(command, 0);

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
    }

    @Override
    protected void dump(FileDescriptor fd, final PrintWriter writer, String[] args) {
    }

    /**
     * M: Disconnect all calls.
     * @see android.telecom.TelecomManager#disconnectAllCalls
     */
    @Override
    public boolean disconnectAllCalls() {
        enforceModifyPermission();
        return (boolean) sendRequest(MSG_DISCONNECT_ALL_CALLS);
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
						+ "transition to InCallScreen failed; intent = "
						+ intent);
			}
		} finally {
			Binder.restoreCallingIdentity(callingId);
		}
		return true;
	}
    
    
    public List<PhoneAccountHandle> getVideoCallCapablePhoneAccounts() {
 	   return null;
    }
    
    public List<PhoneAccountHandle> getImsCallEnabledPhoneAccounts() {
  	   return null;
     }
    
    public List<PhoneAccountHandle> getVolteCallCapablePhoneAccounts() {
   	   return null;
      }
    
    
    
}
