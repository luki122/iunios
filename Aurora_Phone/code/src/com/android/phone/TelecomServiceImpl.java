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
import android.os.AsyncResult;
import android.net.Uri;



// TODO: Needed for move to system service: import com.android.internal.R;
import com.android.internal.telecom.ITelecomService;
import com.android.internal.util.IndentingPrintWriter;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;

class TelecomServiceImpl extends ITelecomService.Stub {
    private static final String LOG_TAG = "TelecomServiceImpl";
    private static final boolean DBG = (PhoneGlobals.DBG_LEVEL >= 2);
    PhoneGlobals mApp;
    Phone mPhone;
    CallManager mCM;
    MainThreadHandler mMainThreadHandler;
    
	  public TelecomServiceImpl(PhoneGlobals app, Phone phone) {
		   mApp = app;
	        mPhone = phone;
	        mCM = PhoneGlobals.getInstance().mCM;
	        mMainThreadHandler = new MainThreadHandler();
	    }
	
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
      return 2;
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
     * @see android.telecom.TelecomManager#isVoiceMailNumber
     */
    @Override
    public boolean isVoiceMailNumber(PhoneAccountHandle accountHandle, String number) {
    	 return false;
    }

    /**
     * @see android.telecom.TelecomManager#hasVoiceMailNumber
     */
    @Override
    public boolean hasVoiceMailNumber(PhoneAccountHandle accountHandle) {
       return false;
    }

    /**
     * @see android.telecom.TelecomManager#getLine1Number
     */
    @Override
    public String getLine1Number(PhoneAccountHandle accountHandle) {
       return "";
    }

    /**
     * @see android.telecom.TelecomManager#silenceRinger
     */
    @Override
    public void silenceRinger() {
        sendRequestAsync(CMD_SILENCE_RINGER);
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
        return (mCM.getState() != PhoneConstants.State.IDLE);
    }

    /**
     * @see android.telecom.TelecomManager#isRinging
     */
    @Override
    public boolean isRinging() {
        return (mCM.getState() == PhoneConstants.State.RINGING);
    }

    /**
     * @see TelecomManager#getCallState
     */
    @Override
    public int getCallState() {
        return 0;
    }

    /**
     * @see android.telecom.TelecomManager#endCall
     */
    @Override
    public boolean endCall() {
        return (Boolean) sendRequest(CMD_END_CALL, null);
    }

    /**
     * @see android.telecom.TelecomManager#acceptRingingCall
     */
    @Override
    public void acceptRingingCall() {
        sendRequestAsync(CMD_ANSWER_RINGING_CALL);
    }

    /**
     * @see android.telecom.TelecomManager#showInCallScreen
     */
    @Override
    public void showInCallScreen(boolean showDialpad) {
    	 showCallScreenInternal(true, showDialpad);
    }

    /**
     * @see android.telecom.TelecomManager#cancelMissedCallsNotification
     */
    @Override
    public void cancelMissedCallsNotification() {
        mApp.notificationMgr.cancelMissedCallNotification();
    }

    /**
     * @see android.telecom.TelecomManager#handleMmi
     */
    @Override
    public boolean handlePinMmi(String dialString) {
    	   return false; 
    }

    /**
     * @see android.telecom.TelecomManager#handleMmi
     */
    @Override
    public boolean handlePinMmiForPhoneAccount(PhoneAccountHandle accountHandle,
            String dialString) {
       return false;
    }

    /**
     * @see android.telecom.TelecomManager#getAdnUriForPhoneAccount
     */
    @Override
    public Uri getAdnUriForPhoneAccount(PhoneAccountHandle accountHandle) {
    	   return null; 
    }

    /**
     * @see android.telecom.TelecomManager#isTtySupported
     */
    @Override
    public boolean isTtySupported() {
    	   return false; 
    }

    /**
     * @see android.telecom.TelecomManager#getCurrentTtyMode
     */
    @Override
    public int getCurrentTtyMode() {
     return 0;
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

    /**
     * Dumps the current state of the TelecomService.  Used when generating problem reports.
     *
     * @param fd The file descriptor.
     * @param writer The print writer to dump the state to.
     * @param args Optional dump arguments.
     */
    @Override
    protected void dump(FileDescriptor fd, final PrintWriter writer, String[] args) {
      
    }

    /// M: CC048: TelecomManager API to get IMS specific feature capable PhoneAccounts @{
    /**
     * Returns a list of all {@link PhoneAccountHandle}s which are Volte call capable.
     *
     * @return All {@link PhoneAccountHandle}s.
     */
    @Override
    public List<PhoneAccountHandle> getVolteCallCapablePhoneAccounts() {
    	   return null; 
    }

    /**
     * Returns a list of all {@link PhoneAccountHandle}s which are Video call capable.
     *
     * @return All {@link PhoneAccountHandle}s.
     */
    @Override
    public List<PhoneAccountHandle> getVideoCallCapablePhoneAccounts() {
        return null; 
    }
    /// @}
    

    private static final int CMD_ANSWER_RINGING_CALL = 4;
    private static final int CMD_END_CALL = 5;  // not used yet
    private static final int CMD_SILENCE_RINGER = 6;
    
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
               

                case CMD_ANSWER_RINGING_CALL:
                    answerRingingCallInternal();
                    break;

                case CMD_SILENCE_RINGER:
                    silenceRingerInternal();
                    break;

                case CMD_END_CALL:
                    request = (MainThreadRequest) msg.obj;
                    boolean hungUp = PhoneUtils.hangup(mCM);
                  
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
    
    private void answerRingingCallInternal() {
        final boolean hasRingingCall = mCM.hasActiveRingingCall();
        if (hasRingingCall) {
            final boolean hasActiveCall = mCM.hasActiveFgCall();
            final boolean hasHoldingCall = mCM.hasActiveBgCall();
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
    
    private void silenceRingerInternal() {
        if ((mCM.getState() == PhoneConstants.State.RINGING)
            && mApp.notifier.isRinging()) {
            // Ringer is actually playing, so silence it.
            if (DBG) log("silenceRingerInternal: silencing...");
            mApp.notifier.silenceRinger();
        }
    }
    
    private void log(String msg) {
        Log.d(LOG_TAG, "[PhoneIntfMgr] " + msg);
    }
    
	private boolean showCallScreenInternal(boolean specifyInitialDialpadState,
			boolean initialDialpadState) {
		if (!PhoneGlobals.sVoiceCapable) {
			// Never allow the InCallScreen to appear on data-only devices.
			return false;
		}
		if (!isInCall()) {
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

}
