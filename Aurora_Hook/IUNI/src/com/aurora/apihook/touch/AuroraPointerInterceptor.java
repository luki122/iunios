/**
 * All rights owned by IUNI
 */
package com.aurora.apihook.touch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.InputChannel;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.IWindowManager;
import android.view.WindowManager;
import android.widget.Toast;

// IPC
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.content.ComponentName;

import static android.view.WindowManagerPolicy.WindowManagerFuncs;

import android.provider.Settings;

// Aurora <Felix.Duan> <2014-7-10> <New file> New SystemUI pull up implementation & New feature: ThreeFingerScreenshot
/**
 * TODO Detect if there is a navigationbar
 * TODO Be a touch event framework of all aurora gesture
 * TODO Take care of portability
 * TOOD Apply a better state machine
 *
 * @Author Felix Duan
 * @Date 2014-7-4
 */

public class AuroraPointerInterceptor {

    private static final String TAG = "felix";
    private static boolean DBG = (SystemProperties.getInt("ro.debuggable", 0) == 1);
    private static boolean DBG_V = false;

    private static final boolean THREEFINGERS_CAPTURE_SUPPORT = SystemProperties.getBoolean("ro.aurora.threefingercapture", true);

    // switcher of pull up recents panel feature
    private static final boolean PULL_UP_RECENTS = true;

    private Context mContext;
    private WindowManagerFuncs mWindowManagerFuncs;

    private IWindowManager mWindowManager;

    // TODO should apply a state machine
    private static final int INIT_Y_THRESHOLD = 1880; // Hot area > Y threshold
    private static final int HOT_ZONE = 40;// for pull up, hot zone from TP bottom edge
    private static final int DIST_THRESHOLD = 45;// 20; // default distance
    private int mDist = DIST_THRESHOLD; // can be changed by DEBUG intent
    private int mThreshold = INIT_Y_THRESHOLD; // depends on screen size
    private static Boolean mToastEnable = false; // can be changed by DEBUG intent
    private static Boolean mDowningFlag = false; // Action DOWN received
    private static Boolean mDownEventSent = false; // Action DOWN sent to target
    private static Boolean mResetLater = false; // reset consume state later
    private static Boolean mKeyguardOn = false; // give way for keyguard
    private static MotionEvent mLastDownEvent; // holder
    private ConsumeState mConsumeState = new ConsumeState();

    private static final int MSG_REGISTER_CLIENT = 1; // MUST be the same as SystemUI.InvokerService`s defination
    private static final int MSG_UNREGISTER_CLIENT = 2; // MUST be the same as SystemUI.InvokerService`s defination
    private static final int MSG_SEND_EVENT = 3; // MUST be the same as SystemUI.InvokerService`s defination
    private static final int MSG_UPDATE_MSK = 4; // MUST be the same as SystemUI.InvokerService`s defination

    private static int mMask = 0;
    private static final int MSK_DISABLE = 0x01; // MUST be the same as SystemUI.InvokerService`s defination
    private static final int MSK_KEYBOARD_INVIEW = 0x01<<1; // MUST be the same as SystemUI.InvokerService`s defination

	//add by txy
	private AuroraThreeFingerScreenShotPolicy mScreenShot;

    public AuroraPointerInterceptor(Context context, WindowManagerFuncs windowManagerFuncs) {
        mContext = context;
        mWindowManagerFuncs = windowManagerFuncs;

        // debug intent
        IntentFilter filter = new IntentFilter("felix.duan.togglePanelInvoker");
        filter.addAction("felix.duan.setDist");
        filter.addAction("felix.duan.toggleToast");
        filter.addAction("felix.duan.toggleLog");
        filter.addAction("felix.duan.resetAuroraInvoker");
        filter.addAction("com.android.systemui.recent.AURORA_BIND_INVOKER_SERVICE");
        filter.addAction("felix.duan.toggleInterceptor");
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
		// Aurora <Steve.Tang> 2014-08-01 reset state as invokerservice create. start
		// if systemui crash, mMask state maybe MSK_DISABLE, then can not pull-up recent panel.
		// reset this object's state as systemui restart
		// this broadcast send by com.android.systemui.recent.InvokerService in methid onCreate().
        filter.addAction("com.android.systemui.recent.AURORA_RESET_PI_STATE");
		// Aurora <Steve.Tang> 2014-08-01 reset state as invokerservice create. end
        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("felix.duan.togglePanelInvoker")) {
                    if ((mMask & MSK_DISABLE) == MSK_DISABLE)
                        mMask ^= ~MSK_DISABLE;
                    else
                        mMask |= MSK_DISABLE;
                    log("AuroraRecentPanelInvoker onReceive() " + intent.getAction() + "  mMask = " + mMask);
                } else if (intent.getAction().equals("felix.duan.setDist")) {
                    mDist = intent.getIntExtra("dist",DIST_THRESHOLD);
                    log("AuroraRecentPanelInvoker onReceive() " + intent.getAction() + "  mDist = " + mDist);
                } else if (intent.getAction().equals("felix.duan.toggleToast")) {
                    mToastEnable = !mToastEnable;
                    log("AuroraRecentPanelInvoker onReceive() " + intent.getAction() + "  mToastEnable = " + mToastEnable);
                } else if (intent.getAction().equals("felix.duan.toggleLog")) {
                    DBG = !DBG;
                    DBG_V = DBG;
                    log("AuroraRecentPanelInvoker onReceive() " + intent.getAction() + "  DBG = " + DBG);
                } else if (intent.getAction().equals("felix.duan.resetAuroraInvoker")) {
                    log("AuroraRecentPanelInvoker onReceive() " + intent.getAction());
                    debugReset();
                } else if (intent.getAction().equals("com.android.systemui.recent.AURORA_BIND_INVOKER_SERVICE")) {
                    doBindService();
                } else if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                    enable();
                } else if ("felix.duan.toggleInterceptor".equals(intent.getAction())) {
                    if (intent.getBooleanExtra("state",false))
                        enable();
                    else
                        disable();
				// Aurora <Steve.Tang> 2014-08-01 reset state as invokerservice create. start
                } else if ("com.android.systemui.recent.AURORA_RESET_PI_STATE".equals(intent.getAction())) {
					log ("AuroraRecentPanelInvoker onReceive() " + intent.getAction());
				    reset();
				// Aurora <Steve.Tang> 2014-08-01 reset state as invokerservice create. end
				}
            }
        }, filter);
    }

    private final class AuroraInputEventReceiver extends InputEventReceiver {

        public AuroraInputEventReceiver(InputChannel inputChannel, Looper looper, Context context) {
            super(inputChannel, looper);
        }

        @Override
        public void onInputEvent(InputEvent event) {
            boolean handled = false;
            try {
                if (event instanceof MotionEvent
                        && (event.getSource() & InputDevice.SOURCE_CLASS_POINTER) != 0) {
                    final MotionEvent motionEvent = (MotionEvent)event;
                    handled = true;
                    logv("AuroraInputEventReceiver MotionEvent");
                    onMotionEvent(motionEvent);
                } else {
                    logv("AuroraInputEventReceiver else");
                }
            } finally {
                finishInputEvent(event, handled);
            }
        }
    }

    AuroraInputEventReceiver mAuroraInputEventReceiver;
    InputChannel mAuroraInputChannel;

    public void enable() {
        log("enable");
        if (mWindowManager == null)
            mWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));

        setupHotZoneThreshold();

        mAuroraInputChannel=
                mWindowManagerFuncs.monitorInput("AuroraInputChannel");
        mAuroraInputEventReceiver=
                new AuroraInputEventReceiver(mAuroraInputChannel,
                        Looper.myLooper(), mContext);
		mScreenShot = new AuroraThreeFingerScreenShotPolicy(mContext);

        doBindService();
    }

    private void setupHotZoneThreshold() {
        Point p = new Point(0,0);
        WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        display.getRealSize(p);
        // Typically on a phone, longer side is portrait Y axis. That`s where we apply pull up feature. 
        mThreshold = (p.x > p.y) ? p.x : p.y;
        mThreshold -= HOT_ZONE;
        if (mThreshold <=0) mThreshold = INIT_Y_THRESHOLD;
        log("setupHotZoneThreshold() mThreshold = " + mThreshold + "  getRealSize " + p);
    }

    public void disable(){
        log("disable");
        if (mAuroraInputEventReceiver != null) {
            mAuroraInputEventReceiver.dispose();
            mAuroraInputEventReceiver = null;
        }

        if (mAuroraInputChannel!= null) {
            mAuroraInputChannel.dispose();
            mAuroraInputChannel= null;
        }
        doUnbindService();
    }

    private void onMotionEvent(MotionEvent event) {
        logv("onMotionEvent()");
        // Aurora <Felix.Duan> <2014-5-31> <BEGIN> Fix BUG #5287. Not reset state at beginning
        //if (DBG) dump();
        //if (DBG) printSamples(event);
        // Aurora <Felix.Duan> <2014-5-31> <END> Fix BUG #5287. Not reset state at beginning
		if(THREEFINGERS_CAPTURE_SUPPORT && isThreeFingerScreenShotEnable())
			mScreenShot.handleEvent(event);

        if (mMask == 0) 
            handleEvent(event);

        if (!mConsumeState.getState()) {
            // give to other filters
            //mNext.onMotionEvent(event,rawEvent,policyFlags);
        } else if (mResetLater) {
            mResetLater = false;
            mConsumeState.update(false);
        }
    }

    // Core logic function 
    private void handleEvent(MotionEvent event){
        logv("handleEvent()");
        if (event.getPointerCount() > 1 ) {
            // cancelTracking();
            return;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                beginTracking(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!mKeyguardOn)
                    cancelTracking(event);
                else {
                    mKeyguardOn = false;
                    logd("mKeyguardOn = false UP/CANCEL");
                    return;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mKeyguardOn) {
                    logd("mKeyguardOn = true MOVE");
                    return;
                }
                detectTracking(event);
                break;
        }
    }

    // Begin track motion event
    private void beginTracking(MotionEvent event) {
        logv("beginTracking()");
        mDowningFlag = true;
        mDownEventSent = false;
        mConsumeState.update(false);
        mLastDownEvent = MotionEvent.obtain(event);

        // Aurora <Felix.Duan> <2014-5-31> <BEGIN> Fix BUG #5287. Not reset state at beginning
        mKeyguardOn = false;
        try {
            mKeyguardOn = mWindowManager.isKeyguardLocked();
        } catch (RemoteException re) {
            /* ignore, good luck with no WindowManager */
        }
        if (mKeyguardOn || !mIsBound) {
            logv("mKeyguardOn = true DOWN || !mIsBound");
            return;
        }
        // Aurora <Felix.Duan> <2014-5-31> <END> Fix BUG #5287. Not reset state at beginning
    }

    /**
     * Detect finger movement
     * If ACTION_DOWN point is at touch panel bottom & dragged up for certain distance. Consume!
     * TODO only comparing with initial down point. Not good enough!
     */
    private void detectTracking(MotionEvent event){
        logv("detectTracking()");
        if (mConsumeState.getState()) {
            consumeEvent(event);
            return;
        }
        if (mLastDownEvent == null) {
            return;
        }
        double currentX = event.getX();
        double currentY = event.getY();
        double downX = mLastDownEvent.getX();
        double downY = mLastDownEvent.getY();
        double absX = Math.abs(currentX - downX);
        double absY = Math.abs(currentY - downY);
        double distance = Math.hypot(absX, absY);
        long duration = event.getEventTime() - event.getDownTime();

        logv("detectTracking() currentY = " + currentY + "  downY = " + downY);
        logv("detectTracking() absX = " + absX + "  absY = " + absY);
        logv("detectTracking() distance = " + distance + "  duration = " + duration);

        if (downY > mThreshold && distance > mDist/*DIST_THRESHOLD*/)
            consumeEvent(event);
    }

    // Cancel on ACTION_UP or ACTION_CANCEL, reset state
    private void cancelTracking(MotionEvent event) {
        logv("cancelTracking()");
        mDowningFlag = false;
        mDownEventSent = false;
        mResetLater = true;

        if (mLastDownEvent != null)
            mLastDownEvent = null;
            //mLastDownEvent.recycle();

        // finish with handOver Up event
        if (mConsumeState.getState())
            sendMsg(event); 
    }

    // Pass MotionEvent to SystemUI
    private void consumeEvent(MotionEvent event){
        log("consumeEvent()");
        mConsumeState.update(true);

        // debug code. easy monitor
        if (mToastEnable)
            Toast.makeText(mContext,
                "consumed consumed consumed consumed consumed consumed consumed consumed consumed consumed ",
                Toast.LENGTH_SHORT).show();

        // tell the world, we have ate the motion
        MotionEvent cancelEvent = MotionEvent.obtain(event);
        cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
        //mNext.onMotionEvent(cancelEvent,rawEvent,policyFlags);

        if (mIsBound) {
                if (!mDownEventSent) {
                    logv("consumeEvent() handOverMotionEvent()  mLastDownEvent");
                    // hand over touch down event
                    sendMsg(mLastDownEvent); 
                    mDownEventSent = true;
                } else {
                    //if (timeOut(event)) {
                        logv("consumeEvent() handOverMotionEvent");
                        //sLastMoveTime = event.getEventTime();
                        sendMsg(event);
                    //}
                }
        }
    }

    // Send IPC message
    private void sendMsg(MotionEvent event) {
        if (!PULL_UP_RECENTS) return;
        logv("sendMsg()");
        Message msg = Message.obtain(null, MSG_SEND_EVENT, event);
        msg.replyTo = mMessenger;
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            logd("sendMsg() caught RemoteException " + e);
        } catch (NullPointerException e) {
            logd("sendMsg() caught NullPointerException " + e);
            // rebind service
            doUnbindService();
            doBindService();

        }
    }

    private class ConsumeState {
        private boolean mConsumed;

        public ConsumeState() {
            mConsumed = false;
        }

        public void update(boolean newState){
            mConsumed = newState;
            if (mConsumed)
                SystemProperties.set("sys.aurora.input.intercept", "1");
            else
                SystemProperties.set("sys.aurora.input.intercept", "0");

            logv("update() " + mConsumed + "  " + SystemProperties.getBoolean("sys.aurora.input.intercept", false));
        }

        public boolean getState() {
            return mConsumed;
        }
    }

// Aurora <Felix.Duan> <2014-5-28> <BEGIN> Add debug code
    private void dump() {
        log("dump: mDowningFlag = " + mDowningFlag
            + " mDist = " + mDist
            + " mConsumed = " + mConsumeState.getState()
            + " mResetLater = " + mResetLater
            + " mKeyguardOn = " + mKeyguardOn
            + " mDownEventSent = " + mDownEventSent 
            //+ " mNext " + ((mNext == null) ? " = null" : " != null")
            + " mLastDownEvent = " + ((mLastDownEvent == null) ? "null" : mLastDownEvent)
            + " mMask = " + mMask);
    }

    // debug code
    void printSamples(MotionEvent ev) {
        final int historySize = ev.getHistorySize();
        final int pointerCount = ev.getPointerCount();
        for (int h = 0; h < historySize; h++) {
            log("\tAt time " + ev.getHistoricalEventTime(h) + "  action = " + ev.getAction());
            for (int p = 0; p < pointerCount; p++) {
                log("\t  pointer " + ev.getPointerId(p)
                   + " : (" + ev.getHistoricalX(p, h)
                   + "," + ev.getHistoricalY(p, h)+")");
            }
        }
        log("\tAt time " + ev.getEventTime() + "  action : " + ev.getAction());
        for (int p = 0; p < pointerCount; p++) {
           log("\t  pointer " + ev.getPointerId(p)
              + " : (" + ev.getX(p)
              + "," + ev.getY(p)+")");
        }
        //log("ev.getEdgeFlags() = " + ev.getEdgeFlags());
    }

    // Aurora <Felix.Duan> <2014-5-31> <BEGIN> Fix BUG #5287. Not reset state at beginning
    // Something unexpected happened
    private void reset() {
        log("reset()");
        mDowningFlag = false;
        mConsumeState.update(false);
        mResetLater = false; 
        mKeyguardOn = false;
        mDownEventSent = false; 
        mLastDownEvent = null; 
		// Aurora <Steve.Tang> 2014-08-01 reset mMask state. start 
        mMask = 0;
		// Aurora <Steve.Tang> 2014-08-01 reset mMask state. end
    }

    // Debug reset, don`t use!
    private void debugReset() {
        logd("debugReset()");
        mDowningFlag = false;
        mDist = DIST_THRESHOLD;
        mConsumeState.update(false);
        mResetLater = false; 
        mKeyguardOn = false;
        mDownEventSent = false; 
        mLastDownEvent = null; 
        mMask = 0;
    }
    // Aurora <Felix.Duan> <2014-5-31> <END> Fix BUG #5287. Not reset state at beginning
// Aurora <Felix.Duan> <2014-5-28> <END> Add debug code
 
    ///////////////////////// IPC Client Part START ////////////////////////////
    /** Messenger for communicating with service. */
    Messenger mService = null;
    /** Flag indicating whether we have called bind on the service. */
    boolean mIsBound;

    private Handler mClientHandler = new IncomingHandler();

    /**
     * Handler of incoming messages from service.
     * for update msk
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            logv("handleMessage() " + msg.what + "  " + msg.arg1 + "  " + msg.arg2);
            switch (msg.what) {
            case MSG_SEND_EVENT:
                break;
            case MSG_UPDATE_MSK:
                // Aurora <Felix.Duan> <2014-6-14> <BEGIN> Fix BUG #5711. Dead lock freeze caused by mMask
                if (!mDowningFlag)
                    mMask = msg.arg1;
                else
                    logd("handleMessage() ignore for downing");
                // Aurora <Felix.Duan> <2014-6-14> <END> Fix BUG #5711. Dead lock freeze caused by mMask
                break;
            default:
                super.handleMessage(msg);
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(mClientHandler);

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            log("onServiceConnected()");
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = new Messenger(service);

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                Message msg = Message.obtain(null, MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);

            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            log("onServiceDisconnected()");
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
        }
    };

    void doBindService() {
        if (!PULL_UP_RECENTS) return;
        log("doBindService()");
        // TODO should apply better security policy!
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        Intent intent = new Intent();
        intent.setClassName("com.android.systemui", "com.android.systemui.recent.InvokerService");
        mContext.bindServiceAsUser(intent, mConnection, Context.BIND_AUTO_CREATE,new UserHandle(UserHandle.USER_CURRENT));
        mIsBound = true;
    }

    void doUnbindService() {
        if (!PULL_UP_RECENTS) return;
        log("doUnbindService()");
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }

            // Detach our existing connection.
            mContext.unbindService(mConnection);
            mIsBound = false;
        }
    }
    ///////////////////////// IPC Client Part END ////////////////////////////

    // normal log
    private void log(String msg) {
        Log.d(TAG, "auroraPointerInterceptor: " + msg);
    }

    // deubg log
    private void logd(String msg) {
        if (DBG) Log.d(TAG, "auroraPointerInterceptor: " + msg);
    }

    // verbose debug log
    private void logv(String msg) {
        if (DBG_V) Log.d(TAG, "auroraPointerInterceptor: " + msg);
    }
	
	// Aurora <Steve.Tang> 2014-10-21 add a switcher for three finger snapshot. start
	private boolean isThreeFingerScreenShotEnable(){
		return Settings.System.getInt(mContext.getContentResolver(), "three_finger_screenshot_enable", 1) == 1;
	}
	// Aurora <Steve.Tang> 2014-10-21 add a switcher for three finger snapshot. end
}
