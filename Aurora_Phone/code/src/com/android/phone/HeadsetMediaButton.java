/*
 * Copyright 2014, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.phone;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.session.MediaSession;
import android.os.Message;
import android.view.KeyEvent;
import android.util.Log;
import com.android.internal.telephony.PhoneConstants;
import android.os.Handler;

import com.android.internal.telephony.CallManager;

/**
 * Static class to handle listening to the headset media buttons.
 */
final class HeadsetMediaButton extends Handler{
	 static final String LOG_TAG = "HeadsetMediaButton";
    private static final int PHONE_STATE_CHANGED = 101;

    private static final AudioAttributes AUDIO_ATTRIBUTES = new AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION).build();

    private final MediaSession.Callback mSessionCallback = new MediaSession.Callback() {
        @Override
        public boolean onMediaButtonEvent(Intent intent) {
            KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            Log.v(LOG_TAG, "SessionCallback.onMediaButton()...  event = " + event);
            if ((event != null) && (event.getKeyCode() == KeyEvent.KEYCODE_HEADSETHOOK)) {
                Log.v(LOG_TAG, "SessionCallback: HEADSETHOOK");
                boolean consumed = PhoneUtils.handleHeadsetHook(PhoneGlobals.getInstance().getPhone(PhoneUtils.getActiveSubscription()), event);
                Log.v(LOG_TAG, "==> handleHeadsetHook(): consumed = " + consumed);
                return consumed;
            }
            return true;
        }
    };

    private final CallManager mCM;

    private final MediaSession mSession;

    HeadsetMediaButton(Context context, CallManager callsManager) {
    	mCM = callsManager;

        // Create a MediaSession but don't enable it yet. This is a
        // replacement for MediaButtonReceiver
        mSession = new MediaSession(context, HeadsetMediaButton.class.getSimpleName());
        mSession.setCallback(mSessionCallback);
        mSession.setFlags(MediaSession.FLAG_EXCLUSIVE_GLOBAL_PRIORITY
                | MediaSession.FLAG_HANDLES_MEDIA_BUTTONS);
        mSession.setPlaybackToLocal(AUDIO_ATTRIBUTES);
        mCM.registerForPreciseCallStateChanged(this, PHONE_STATE_CHANGED, null);
    }

    @Override
    public void handleMessage(Message msg) {
    	   switch (msg.what) {
           case PHONE_STATE_CHANGED:
               onPhoneStateChanged();
               break;
    	   }
    }
    
    private void onPhoneStateChanged() {
        PhoneConstants.State state = mCM.getState();
        if(state != PhoneConstants.State.IDLE) {
            if (!mSession.isActive()) {
                mSession.setActive(true);
            }
        } else {
            if (mSession.isActive()) {
                mSession.setActive(false);
            }
        }
    }
}
