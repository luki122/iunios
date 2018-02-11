/*
 * Copyright (C) 2013 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.incallui;

import android.app.KeyguardManager;
import android.content.Context;

import java.util.List;

import com.android.incallui.Call;
import com.android.incallui.CallList;
import com.android.incallui.InCallActivity;
import com.android.incallui.InCallApp;
import com.android.incallui.Log;
import com.android.incallui.CallButtonPresenter.CallButtonUi;
import com.android.incallui.InCallPresenter.InCallState;

import android.net.Uri;
import android.content.Intent;
import android.telecom.VideoProfile;
/**
 * Presenter for the Incoming call widget.
 */
public class AuroraAnswerPresenter extends Presenter<AuroraAnswerPresenter.AnswerUi>
        implements CallList.CallUpdateListener, CallList.Listener {

    private static final String TAG = AnswerPresenter.class.getSimpleName();

    private String mCallId;
    private Call mCall = null;
    private boolean mHasTextMessages = false;

    @Override
    public void onUiReady(AnswerUi ui) {
        super.onUiReady(ui);

        final CallList calls = CallList.getInstance();
        Call call;
        call = calls.getIncomingCall();
        if (call != null) {
            processIncomingCall(call);
        }
        call = calls.getVideoUpgradeRequestCall();
        if (call != null) {
            processVideoUpgradeRequestCall(call);
        }

        // Listen for incoming calls.
        calls.addListener(this);
    }

    @Override
    public void onUiUnready(AnswerUi ui) {
        super.onUiUnready(ui);

        CallList.getInstance().removeListener(this);

        // This is necessary because the activity can be destroyed while an incoming call exists.
        // This happens when back button is pressed while incoming call is still being shown.
        if (mCallId != null) {
            CallList.getInstance().removeCallUpdateListener(mCallId, this);
        }
    }

    @Override
    public void onCallListChange(CallList callList) {
        // no-op
    }

    @Override
    public void onDisconnect(Call call) {
        // no-op
    	InCallApp.getInCallActivity().mInCallTouchUiAnimationController.resetInCallTouchUiAnimation();
    	InCallApp.getInCallActivity().mCallCardAnimationController.ResetCallCardAnimation();
    	if(mIsAnswerCall) {
    		mIsAnswerCall = false;
    		if(isCallIncoming()) {
        		onAnswer(VideoProfile.VideoState.AUDIO_ONLY, getUi().getContext());
    		}
    	}
    }

    @Override
    public void onIncomingCall(Call call) {
        // TODO: Ui is being destroyed when the fragment detaches.  Need clean up step to stop
        // getting updates here.
        Log.d(this, "onIncomingCall: " + this);
        if (getUi() != null) {
            if (!call.getId().equals(mCallId)) {
                // A new call is coming in.
                processIncomingCall(call);
            }
        }
    }

    private void processIncomingCall(Call call) {
        mCallId = call.getId();
        mCall = call;

        // Listen for call updates for the current call.
        CallList.getInstance().addCallUpdateListener(mCallId, this);

        Log.d(TAG, "Showing incoming for call id: " + mCallId + " " + this);
        final List<String> textMsgs = CallList.getInstance().getTextResponses(call.getId());
        getUi().showAnswerUi(true);
    }

    private void processVideoUpgradeRequestCall(Call call) {
        mCallId = call.getId();
        mCall = call;

        // Listen for call updates for the current call.
        CallList.getInstance().addCallUpdateListener(mCallId, this);
        getUi().showAnswerUi(true);
    }

    @Override
    public void onCallChanged(Call call) {
        Log.d(this, "onCallStateChange() " + call + " " + this);
        if (call.getState() != Call.State.INCOMING) {
            // Stop listening for updates.
            CallList.getInstance().removeCallUpdateListener(mCallId, this);

            getUi().showAnswerUi(false);

            // mCallId will hold the state of the call. We don't clear the mCall variable here as
            // it may be useful for sending text messages after phone disconnects.
            mCallId = null;
            mHasTextMessages = false;
        } 
    }

    public void onAnswer(int videoState, Context context) {
        if (mCallId == null) {
            return;
        }

        Log.d(this, "onAnswer " + mCallId);
        TelecomAdapter.getInstance().answerCall(mCall.getId(), videoState);
        
    }

    /**
     * TODO: We are using reject and decline interchangeably. We should settle on
     * reject since it seems to be more prevalent.
     */
    public void onDecline() {
        Log.d(this, "onDecline " + mCallId);
        TelecomAdapter.getInstance().rejectCall(mCall.getId(), false, null);
    }
    
    private boolean mIsAnswerCall = false;
    public void onDeclineActive() {
    	String callId = CallList.getInstance().getActiveCall().getId();
        Log.d(this, "onDecline " + callId);
//        TelecomAdapter.getInstance().rejectCall(callId, false, null);
        TelecomAdapter.getInstance().disconnectCall(callId);
        mIsAnswerCall = true;
        
    }

    public void onText() {
        if (getUi() != null) {
            getUi().showMessageDialog();
        }
    }

    public void rejectCallWithMessage(String message) {
        Log.d(this, "sendTextToDefaultActivity()...");
        TelecomAdapter.getInstance().rejectCall(mCall.getId(), true, message);

        onDismissDialog();
    }

    public void onDismissDialog() {
        InCallPresenter.getInstance().onDismissDialog();
    }

    
    public void launchGnSmsCompose() {
        Log.d(this, "launchSmsCompose: number " + mCall.getNumber());
        Uri uri = Uri.fromParts("sms", mCall.getNumber(), null);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        getUi().getContext().startActivity(intent);        
    }
    
    
    public boolean isCallWating() {
    	if(mCall != null) {
//    		return mCall.getState() == Call.State.CALL_WAITING;
    		if(CallList.getInstance().getActiveAndHoldCallsCount() > 0 && isCallIncoming()) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public boolean isCallIncoming() {
    	if(mCall != null) {
    		return mCall.getState() == Call.State.INCOMING;
    	}
    	return false;
    }
    
    public Call getCall() {
    	return mCall;
    }

    interface AnswerUi extends Ui {
        public void showAnswerUi(boolean show);
        public void showMessageDialog();
        public Context getContext();
    }
    
  
    
    public void onStorageFull() {
        // TODO Auto-generated method stub
    }

    public void onUpdateRecordState(int state, int customValue) {
        // TODO Auto-generated method stub
    }
}
