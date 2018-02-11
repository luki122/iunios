package com.android.phone;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.util.Log;
import android.os.AsyncResult;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.gsm.GSMPhone;

import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import com.android.phone.PhoneGlobals.SubInfoUpdateListener;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.Connection;
import com.android.phone.AuroraMSimConstants;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneBase;
import com.android.internal.telephony.TelephonyCapabilities;

import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.app.ActivityManagerNative;
import android.content.Intent;
import android.app.KeyguardManager;

public class I2CallNotifier extends CallNotifier implements SubInfoUpdateListener{
    private static final String LOG_TAG = "I2CallNotifier";
    
    
    static I2CallNotifier init(PhoneGlobals app, Phone phone, Ringer ringer, CallLogger callLogger) {
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
        PhoneGlobals.getInstance().addSubInfoUpdateListener(this);
    }
    
    public void handleMessage(Message msg) {
        switch (msg.what) {
	        case PHONE_VOICE_RINGING_CONNECTION:
	            log("VOICERINGING... (new)");
	            ((GSMPhone) ((AsyncResult) msg.obj).result).setIncomingCallIndicationResponse(true);
	     		return;
	        case PHONE_MWI_CHANGED:
	            int subId = Integer.valueOf(String.valueOf(msg.obj));
                Log.i(LOG_TAG, "Received PHONE_MWI_CHANGED event, subId = " + subId);
                if (PhoneUtils.isValidSubId(subId)) {
                    Phone phone = PhoneUtils.getPhoneUsingSubId(subId);
                    onMwiChanged(phone.getMessageWaitingIndicator(), phone);
                }
                return;
	        default:
	             super.handleMessage(msg);
	    }
        
    }
    
    protected void registerForNotifications() {
    	super.registerForNotifications();
    	PhoneGlobals.getInstance().getPhone(0).registerForVoiceCallIncomingIndication(this, PHONE_VOICE_RINGING_CONNECTION, null);
    	PhoneGlobals.getInstance().getPhone(1).registerForVoiceCallIncomingIndication(this, PHONE_VOICE_RINGING_CONNECTION, null);
    }
    
    void updateCallNotifierRegistrationsAfterRadioTechnologyChange() {
    	super.updateCallNotifierRegistrationsAfterRadioTechnologyChange();
    	PhoneGlobals.getInstance().getPhone(0).unregisterForVoiceCallIncomingIndication(this);
    	PhoneGlobals.getInstance().getPhone(1).unregisterForVoiceCallIncomingIndication(this);
    	PhoneGlobals.getInstance().getPhone(0).registerForVoiceCallIncomingIndication(this, PHONE_VOICE_RINGING_CONNECTION, null);
    	PhoneGlobals.getInstance().getPhone(1).registerForVoiceCallIncomingIndication(this, PHONE_VOICE_RINGING_CONNECTION, null);
    }
    
    
    private SubscriptionManager mSubscriptionManager;
    @Override
    protected void listen() {
    	   mSubscriptionManager = (SubscriptionManager) mApplication.getSystemService(
                   Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        mSubscriptionManager.addOnSubscriptionsChangedListener(
                new SubscriptionManager.OnSubscriptionsChangedListener() {
                    @Override
                    public void onSubscriptionsChanged() {
                        updatePhoneStateListeners();
                    }
                });
    }
    
    private Map<Integer, CallNotifierPhoneStateListener> mPhoneStateListeners =
            new ArrayMap<Integer, CallNotifierPhoneStateListener>();
    private class CallNotifierPhoneStateListener extends PhoneStateListener {
        public CallNotifierPhoneStateListener(int subId) {
            super(subId);
        }

        @Override
        public void onMessageWaitingIndicatorChanged(boolean visible) {
            Log.i(LOG_TAG, "onMessageWaitingIndicatorChanged(): " + this.mSubId + " " + visible);
            ((I2NotificationMgr)mApplication.notificationMgr).updateMwi(visible, PhoneGlobals.getInstance().getPhone(AuroraSubUtils.getSlotBySubId(mApplication, this.mSubId)));
        }

        @Override
        public void onCallForwardingIndicatorChanged(boolean visible) {
        	Log.i(LOG_TAG, "onCallForwardingIndicatorChanged(): " + this.mSubId + " " + visible);
            ((I2NotificationMgr)mApplication.notificationMgr).updateCfi(visible, AuroraSubUtils.getSlotBySubId(mApplication, this.mSubId));
            /// M: Add for plug-in @{
//            ExtensionManager.getCallForwardExt().updateCfiIcon(this.mSubId, 
//                    mApplication.getApplicationContext(), I2CallNotifier.this);
            /// }@
        }
    };
    
    @Override
    public void handleSubInfoUpdate() {
        updatePhoneStateListeners();
        //ArrayList<Integer> list = mApplication.notificationMgr.mCfiStatusList;
        //log("handleSubInfoUpdate: mCfiStatusList size start:" + list.size());
        //handlePlugOut(list);
        //log("handleSubInfoUpdate: mCfiStatusList size end:" + list.size());
    }
    
    public void updatePhoneStateListeners() {
        log("#onSubscriptionsChanged# updatePhoneStateListeners start");
        List<SubscriptionInfo> subInfos = mSubscriptionManager.getActiveSubscriptionInfoList();

        // Unregister phone listeners for inactive subscriptions.
        Iterator<Integer> itr = mPhoneStateListeners.keySet().iterator();
        while (itr.hasNext()) {
            int subId = itr.next();
            if (subInfos == null || !containsSubId(subInfos, subId)) {
                // Hide the outstanding notifications.
                ((I2NotificationMgr)mApplication.notificationMgr).updateMwi(false, PhoneGlobals.getInstance().getPhone(AuroraSubUtils.getSlotBySubId(mApplication, subId)));
                ((I2NotificationMgr)mApplication.notificationMgr).updateCfi(false, AuroraSubUtils.getSlotBySubId(mApplication, subId));
                /// M: Add for plug-in @{
//                ExtensionManager.getCallForwardExt().updateCfiIcon(subId,
//                        mApplication.getApplicationContext(), CallNotifier.this);
                /// }@

                // Listening to LISTEN_NONE removes the listener.
                TelephonyManager.getDefault().listen(
                        mPhoneStateListeners.get(subId), PhoneStateListener.LISTEN_NONE);
                itr.remove();
            }
        }

        if (subInfos == null) {
            log("#onSubscriptionsChanged# updatePhoneStateListeners return");
            return;
        }

        // Register new phone listeners for active subscriptions.
        for (int i = 0; i < subInfos.size(); i++) {
            int subId = subInfos.get(i).getSubscriptionId();
            if (!mPhoneStateListeners.containsKey(subId)) {
                CallNotifierPhoneStateListener listener = new CallNotifierPhoneStateListener(subId);
                TelephonyManager.getDefault().listen(listener,
                        PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR
                        | PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR);
                mPhoneStateListeners.put(subId, listener);
            }
        }
        log("#onSubscriptionsChanged# updatePhoneStateListeners end");
    }
    
    private void onMwiChanged(boolean visible, Phone phone) {
        Log.i(LOG_TAG, "onMwiChanged(): " + visible);

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
        

        ((I2NotificationMgr)mApplication.notificationMgr).updateMwi(visible, phone);
    }
    
    @Override
    protected void onNewRingingConnection(AsyncResult r) {
    	Connection c = (Connection) r.result;
        Call ringing = c.getCall();
        Phone phone = ringing.getPhone();        			
                
        final int subscription = PhoneUtils.getSubscription(phone);
        int otherSub = AuroraPhoneUtils.getOtherSlot(subscription);
//        if(mCM.hasActiveRingingCall(AuroraSubUtils.getSubIdbySlot(PhoneGlobals.getInstance(), otherSub)) ) {
        if(mCM.getState(AuroraSubUtils.getSubIdbySlot(PhoneGlobals.getInstance(), otherSub)) != PhoneConstants.State.IDLE) {
            PhoneUtils.hangupRingingCall(ringing);
            return;
        }
    	super.onNewRingingConnection(r);
    }
    
    @Override
    protected void onUnknownConnectionAppeared(AsyncResult r) {
//        PhoneBase pb =  (PhoneBase)r.result;
	   Connection connection = (Connection) r.result;
	   if (connection != null) {
           Call call = connection.getCall();
           if (call != null) {
        	      int subscription = PhoneUtils.getSubscription(call.getPhone());
        	        PhoneConstants.State state = mCM.getState(AuroraSubUtils.getSubIdbySlot(PhoneGlobals.getInstance(),subscription));

        	        if (state == PhoneConstants.State.OFFHOOK) {
        	            // update the active sub before launching incall UI.
        	            PhoneUtils.setActiveSubscription(subscription);
        	            // basically do onPhoneStateChanged + display the incoming call UI
        	            onPhoneStateChanged(r);
        	            Log.i(LOG_TAG, "- showing incoming call (unknown connection appeared)...");
        	            showIncomingCall();
        	        }
           }
       }
  
    }
    

    /**
     * Posts a delayed PHONE_MWI_CHANGED event, to schedule a "retry" for a
     * failed NotificationMgr.updateMwi() call.
     */
    /* package */
    void sendMwiChangedDelayed(long delayMillis, Phone phone) {
        Message message = Message.obtain(this, PHONE_MWI_CHANGED, phone);
        sendMessageDelayed(message, delayMillis);
    }
    
    /**
     * @return {@code true} if the list contains SubscriptionInfo with the given subscription id.
     */
    private boolean containsSubId(List<SubscriptionInfo> subInfos, int subId) {
        if (subInfos == null) {
            return false;
        }

        for (int i = 0; i < subInfos.size(); i++) {
            if (subInfos.get(i).getSubscriptionId() == subId) {
                return true;
            }
        }
        return false;
    }
    
    protected void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
    
    protected void showIncomingCall() {
        log("showIncomingCall()...  phone state = " + mCM.getState());

        // Before bringing up the "incoming call" UI, force any system
        // dialogs (like "recent tasks" or the power dialog) to close first.
        try {
            ActivityManagerNative.getDefault().closeSystemDialogs("call");
        } catch (RemoteException e) {
        }

        // Go directly to the in-call screen.
        // (No need to do anything special if we're already on the in-call
        // screen; it'll notice the phone state change and update itself.)
        mApplication.requestWakeState(PhoneGlobals.WakeState.FULL);

        // Post the "incoming call" notification *and* include the
        // fullScreenIntent that'll launch the incoming-call UI.
        // (This will usually take us straight to the incoming call
        // screen, but if an immersive activity is running it'll just
        // appear as a notification.)
        log("- updating notification from showIncomingCall()...");
        if(Build.VERSION.SDK_INT >= 21) {
        	mApplication.notificationMgr.updateNotificationAndLaunchIncomingCallUi();
        }        
	    boolean isShowFullScreen = AuroraPhoneUtils.isShowFullScreenWhenRinging();
        
        if(isShowFullScreen) {
            mApplication.displayCallScreen();
        } else  {
            Intent intent = new Intent(mApplication, FloatWindowService.class);          
            mApplication.startService(intent);  
        }        

        isRingingTouchOn = false; 
        this.sendEmptyMessageDelayed(ENABLE_TOUCH_DELAY, 2000);
        
        //aurora add liguangyu 20140823 for BUG #7769 start
//        this.postDelayed(new Runnable() {
//  			public void run() {
//  			    boolean isStarted = true;
//  		   	    try {  		   		  
//  		   		    ActivityManager am = (ActivityManager) mApplication.getSystemService(Context.ACTIVITY_SERVICE);
//  		   		    ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
//  		   	        Log.v(LOG_TAG, "topActiviy = " +  cn.getClassName());
//  		   	        if(!cn.getClassName().equalsIgnoreCase("com.android.phone.I2InCallScreen")) {
//  		   	    	    isStarted = false;
//  		   	        }
//  		   	    } catch (Exception e) {
//  		   		   e.printStackTrace();
//  		   	    }
//  		   	    if(!isStarted 
//  		   			    && (mCM.getState() == PhoneConstants.State.RINGING)) {
//  		   	        Log.v(LOG_TAG, "display InCallScreen again");
//  		   		    mApplication.displayCallScreen();
//  		   	    }
//  			}    		
//    	}, 2500);
        //aurora add liguangyu 20140823 for BUG #7769 end
        
        if(mCM.hasActiveFgCall()) {
        	mBrightWakeLock.acquire(100);
        }
    }
}