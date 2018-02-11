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

import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyCapabilities;
import com.android.phone.InCallUiState.InCallScreenMode;
import com.android.phone.OtaUtils.CdmaOtaScreenState;

import java.util.List;

/**
 * Phone app "multi sim in call" screen.
 */
public class I2InCallScreen extends InCallScreen {
    private static final String LOG_TAG = "I2InCallScreen";
    private static final boolean DBG =
            (I2PhoneGlobals.DBG_LEVEL >= 1) && (SystemProperties.getInt("ro.debuggable", 0) == 1);
    private static final boolean VDBG = (I2PhoneGlobals.DBG_LEVEL >= 2);


    /**
     * End the current in call screen session.
     *
     * This must be called when an InCallScreen session has
     * complete so that the next invocation via an onResume will
     * not be in an old state.
     */
    @Override
    public void endInCallScreenSession() {
        if (DBG) log("endInCallScreenSession()... phone state = " + mCM.getState());

        // If other sub is active, do not end the call screen ans update the
        // inCall UI with other active subscription
        if (PhoneUtils.isAnyOtherSubActive(PhoneUtils.getActiveSubscription())) {
            PhoneUtils.switchToOtherActiveSub(PhoneUtils.getActiveSubscription());
            updateScreen();
            log(" We have a active sub , switching to it" );
        } else {
            // Do not end the session if a call is on progress.
            if (mCM.getState() == PhoneConstants.State.IDLE) {
                endInCallScreenSession(false);
            } else {
                Log.i(LOG_TAG, "endInCallScreenSession(): Call in progress");
            }
        }
    }


    @Override
    protected void delayedCleanupAfterDisconnect(Phone phone) {
        if (VDBG) log("delayedCleanupAfterDisconnect()...  Phone state = " + mCM.getState());

        
        mCM.clearDisconnected();

        // There are two cases where we should *not* exit the InCallScreen:
        //   (1) Phone is still in use
        // or
        //   (2) There's an active progress indication (i.e. the "Retrying..."
        //       progress dialog) that we need to continue to display.

        boolean stayHere = phoneIsInUse() || mApp.inCallUiState.isProgressIndicationActive();

        // If other sub is active, do not end the call screen.
        if (!stayHere && PhoneUtils.isAnyOtherSubActive(PhoneUtils.getActiveSubscription())) {
            if (DBG) log("- delayedCleanupAfterDisconnect: othe sub is active , switching");
            if (mLastDisconnectCause.mCause == AuroraDisconnectCause.NORMAL) {
                // if the call was ended by the remote party, we need to remain the
                // other sub's Lch state.
                PhoneUtils.switchToOtherActiveSubRemainInLch(PhoneUtils.getActiveSubscription());
            } else {
                PhoneUtils.switchToOtherActiveSub(PhoneUtils.getActiveSubscription());
            }
            updateScreen();
            stayHere = true;
        }

        if (stayHere) {
            if (DBG) log("- delayedCleanupAfterDisconnect: staying on the InCallScreen...");
            final boolean hasActiveCall = mCM.hasActiveFgCall();
            final boolean hasHoldingCall = mCM.hasActiveBgCall();
            log("onDisconnect: hasActiveCall = " + hasActiveCall + ", hasHoldingCall = " + hasHoldingCall);
    	    if (!hasActiveCall && hasHoldingCall) {
    	           PhoneUtils.switchHoldingAndActive(mCM.getFirstActiveBgCall()); 
    	    }
        } else {
            // Phone is idle!  We should exit the in-call UI now.
            if (DBG) log("- delayedCleanupAfterDisconnect: phone is idle...");

            // And (finally!) exit from the in-call screen
            // (but not if we're already in the process of pausing...)
            if (mIsForegroundActivity) {
                if (DBG) log("- delayedCleanupAfterDisconnect: finishing InCallScreen...");

                if (VDBG) log("- Post-call behavior:");
                if (VDBG) log("  - mLastDisconnectCause = " + mLastDisconnectCause);
//                if (VDBG) log("  - isPhoneStateRestricted() = " + isPhoneStateRestricted(phone));
              

                if(isDoAnimationOnDisconnect()) {
                	runDisconnecedAnimation();
		            return;
                }
                
            }
        if (VDBG) log("delayedCleanupAfterDisconnect()...  Phone state = 1");
            endInCallScreenSession();

            // Reset the call origin when the session ends and this in-call UI is being finished.
            mApp.setLatestActiveCallOrigin(null);
        }
    }

    @Override
    protected void bailOutAfterErrorDialog() {
        if (mGenericErrorDialog != null) {
            if (DBG) log("bailOutAfterErrorDialog: DISMISSING mGenericErrorDialog.");
            mGenericErrorDialog.dismiss();
            mGenericErrorDialog = null;
        }
        if (DBG) log("bailOutAfterErrorDialog(): end InCallScreen session...");

        final InCallUiState inCallUiState = mApp.inCallUiState;

        inCallUiState.clearPendingCallStatusCode();

        // If other sub is active, do not end the call screen ans update the
        // inCall UI with other active subscription
        if (PhoneUtils.isAnyOtherSubActive(PhoneUtils.getActiveSubscription())) {
            PhoneUtils.switchToOtherActiveSub(PhoneUtils.getActiveSubscription());
            updateScreen();
            log(" Switch to other active sub" );
        } else {
            // Force the InCallScreen to truly finish(), rather than just
            // moving it to the back of the activity stack (which is what
            // our finish() method usually does.)
            // This is necessary to avoid an obscure scenario where the
            // InCallScreen can get stuck in an inconsistent state, somehow
            // causing a *subsequent* outgoing call to fail (bug 4172599).
            endInCallScreenSession(false /* force a real finish() call */);
        }
    }

    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }

    private boolean isResumeU5 = false;
    
    protected void onNewRingingConnection() {
        super.onNewRingingConnection();
        if(!mIsForegroundActivity) {
        	isResumeU5 = false;
        }
        
    }
    
    
    protected void onIncomingRing() {
        super.onIncomingRing();
        
//        mHandler.postDelayed(new Runnable(){
//			@Override
//			public void run() {
//                Log.e(LOG_TAG, "resume InCallScreen Force");
//                if(!isResumeU5) {
//	   		    	mApp.displayCallScreen();
//                }
//			}				
//		}, 3000);
    }
    
    protected void onPostResume() {
        if (DBG) log("onPostResume()...");
    	super.onPostResume();
    	
    	 mHandler.postDelayed(new Runnable(){
 			@Override
 			public void run() {
 				if(mIsForegroundActivity) {
 					isResumeU5 = true;
 				}
 			}				
 		}, 100);
        
  
//    	if (!mPowerManager.isScreenOn() && mScreenOnLock != null
//				&& !mScreenOnLock.isHeld()
//				&& mCM.getState() == PhoneConstants.State.RINGING) {
//    		mScreenOnLock.acquire(3 * 1000);
//		}
    }
    
	private PowerManager.WakeLock mScreenOnLock;
	private PowerManager mPowerManager;
	
	   @Override
	    protected void onCreate(Bundle icicle) {
	        super.onCreate(icicle);
	        mPowerManager = (PowerManager) mApp.getSystemService(Context.POWER_SERVICE);
	        mScreenOnLock = mPowerManager.newWakeLock(
					PowerManager.SCREEN_BRIGHT_WAKE_LOCK
							| PowerManager.ACQUIRE_CAUSES_WAKEUP,
					"U5ScreenOnLock");
			mScreenOnLock.setReferenceCounted(false);
	   }
}
