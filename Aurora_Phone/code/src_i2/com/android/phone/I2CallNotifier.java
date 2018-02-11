package com.android.phone;

import android.os.Message;
import android.util.Log;
import android.os.AsyncResult;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.gsm.GSMPhone;

public class I2CallNotifier extends CallNotifier {
    private static final String LOG_TAG = "I2CallNotifier";
    
    
    static CallNotifier init(PhoneGlobals app, Phone phone, Ringer ringer, CallLogger callLogger) {
        synchronized (I2CallNotifier.class) {
            if (sInstance == null) {
                sInstance = new I2CallNotifier(app, phone, ringer, callLogger);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            return (I2CallNotifier) sInstance;
        }
    }

    /** Private constructor; @see init() */
    protected I2CallNotifier(PhoneGlobals app, Phone phone, Ringer ringer,
                                CallLogger callLogger) {
        super(app, phone, ringer, callLogger);
    }
    
    public void handleMessage(Message msg) {
    	if(msg.what == PHONE_VOICE_RINGING_CONNECTION) {
            log("VOICERINGING... (new)");
           ((GSMPhone) ((AsyncResult) msg.obj).result).setIncomingCallIndicationResponse(true);
    		return;
    	}
    	super.handleMessage(msg);
    }
    
    protected void registerForNotifications() {
    	super.registerForNotifications();
    	PhoneGlobals.getInstance().phone.registerForVoiceCallIncomingIndication(this, PHONE_VOICE_RINGING_CONNECTION, null);
    }
    
    void updateCallNotifierRegistrationsAfterRadioTechnologyChange() {
    	super.updateCallNotifierRegistrationsAfterRadioTechnologyChange();
    	PhoneGlobals.getInstance().phone.unregisterForVoiceCallIncomingIndication(this);
    }
}