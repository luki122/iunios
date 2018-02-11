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

// steve.tang PointerEventListener instead InputEventReceiver
import android.view.WindowManagerPolicy.PointerEventListener;


import android.content.res.Resources;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.provider.Settings;
import java.lang.Runnable;
import android.content.res.Configuration;

// Aurora <Felix.Duan> <2014-7-10> New feature: ThreeFingerScreenshot
/**
 * TODO Be a touch event framework of all aurora gesture
 * TODO Apply a better state machine
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

    // Aurora <Felix.Duan> <2014-9-25> <BEGIN> Support pull up recent panel on kitkat
    private static boolean PULL_UP_RECENTS; // feature pull up recent panel switcher
    // Aurora <Felix.Duan> <2014-9-25> <END> Support pull up recent panel on kitkat

    private Context mContext;
    private WindowManagerFuncs mWindowManagerFuncs;

    private IWindowManager mWindowManager;

    // TODO should apply a state machine
    // Aurora <Felix.Duan> <2014-7-26> <BEGIN> Feature: navigation bar swipe down/right
    Resources mResources;
    // handle landscape with X now
    private static final int INIT_THRESHOLD = 2520; // Hot area > threshold
    // Aurora <Felix.Duan> <2014-7-26> <END> Feature: navigation bar swipe down/right
    private static final int HOT_ZONE = 40;// for pull up, hot zone from TP bottom edge
    private static final int DIST_THRESHOLD = 45;// 20; // default distance
    private int mDist = DIST_THRESHOLD; // can be changed by DEBUG intent
    private int mThreshold = INIT_THRESHOLD; // depends on screen size
    private static Boolean mToastEnable = false; // can be changed by DEBUG intent
    private static Boolean mDowningFlag = false; // Action DOWN received
    private static Boolean mDownEventSent = false; // Action DOWN sent to target
    //private static Boolean mConsumed = false; // in consume state
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

	//steve.tang add for capture
	private AuroraThreeFingerScreenShotPolicy mScreenShot;

    public AuroraPointerInterceptor(Context context, WindowManagerFuncs windowManagerFuncs) {
        mContext = context;
        mWindowManagerFuncs = windowManagerFuncs;
        // Aurora <Felix.Duan> <2014-7-26> <BEGIN> Feature: navigation bar swipe down/right
        // Enable feature when there is navi bar
        mResources = mContext.getResources();
        //mHasNaviBar = mResources.getBoolean(com.android.internal.R.bool.config_showNavigationBar);
        mHasNaviBar = isIUNI_U3();

        // Aurora <Felix.Duan> <2014-9-25> <BEGIN> Support pull up recent panel on kitkat
        PULL_UP_RECENTS = !mHasNaviBar;
        // Aurora <Felix.Duan> <2014-9-25> <END> Support pull up recent panel on kitkat

        if (mHasNaviBar)
            showNaviBarRunnable = new ShowNaviBarRunnable();
        mHandler = new Handler();
        // Aurora <Felix.Duan> <2014-7-26> <END> Feature: navigation bar swipe down/right

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
        //private final PointerLocationView mView;
        //private final AuroraPointerInterceptor mInterceptor;

        public AuroraInputEventReceiver(InputChannel inputChannel, Looper looper, Context context) {
            super(inputChannel, looper);
            //mView = view;
            //mInterceptor = new AuroraPointerInterceptor(context);
        }

        @Override
        public void onInputEvent(InputEvent event) {
            boolean handled = false;
            try {
                if (event instanceof MotionEvent
                        && (event.getSource() & InputDevice.SOURCE_CLASS_POINTER) != 0) {
                    final MotionEvent motionEvent = (MotionEvent)event;
                    //mView.addPointerEvent(motionEvent);
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

    private final class ThreeFingerCaptureEventListener implements PointerEventListener {
        @Override
        public void onPointerEvent(MotionEvent motionEvent) {
            onMotionEvent(motionEvent);
        }
    }


    AuroraInputEventReceiver mAuroraInputEventReceiver;
	PointerEventListener mCaptureEventListener;
    InputChannel mAuroraInputChannel;

    public void enable() {
        log("enable");
        if (mWindowManager == null)
            mWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));

        setupHotZoneThreshold();

		// steve.tang modify start
		// kk not support mWindowManagerFuncs.monitorInput(....)
		// so use PointerEventListener instead 
       /* mAuroraInputChannel=
                mWindowManagerFuncs.monitorInput("AuroraInputChannel");
        mAuroraInputEventReceiver=
                new AuroraInputEventReceiver(mAuroraInputChannel,
                        Looper.myLooper(), mContext);*/

		

		mScreenShot = new AuroraThreeFingerScreenShotPolicy(mContext);
		
		// register PointerEventListener to get point event
		mCaptureEventListener = new ThreeFingerCaptureEventListener();
		mWindowManagerFuncs.registerPointerEventListener(mCaptureEventListener);
        
		// steve.tang modify end
		
        if (PULL_UP_RECENTS)
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
        if (mThreshold <=0) mThreshold = INIT_THRESHOLD;
        log("setupHotZoneThreshold() mThreshold = " + mThreshold + "  getRealSize " + p);
    }

    public void disable(){
        log("disable");
		if(mCaptureEventListener != null){
			mWindowManagerFuncs.unregisterPointerEventListener(mCaptureEventListener);
			mCaptureEventListener = null;
		}
		/*
        if (mAuroraInputEventReceiver != null) {
            mAuroraInputEventReceiver.dispose();
            mAuroraInputEventReceiver = null;
        }

        if (mAuroraInputChannel!= null) {
            mAuroraInputChannel.dispose();
            mAuroraInputChannel= null;
        }
		*/
		// remove some method about Pull-up
        //doUnbindService();
    }

    private void onMotionEvent(MotionEvent event) {
        //int action = event.getAction();
        //if (MotionEvent.ACTION_DOWN == action) {
        //    log("DOWN");
        //} else if (MotionEvent.ACTION_MOVE == action) {
        //    log("MOVE");
        //} else if (MotionEvent.ACTION_CANCEL == action) {
        //    log("CANCEL");
        //} else if (MotionEvent.ACTION_UP == action) {
        //    log("UP");
        //} else {
        //    log("action = " + action);
        //}

        logv("onMotionEvent()");
        // Aurora <Felix.Duan> <2014-5-31> <BEGIN> Fix BUG #5287. Not reset state at beginning
        //if (DBG) dump();
        //if (DBG) printSamples(event);
        // Aurora <Felix.Duan> <2014-5-31> <END> Fix BUG #5287. Not reset state at beginning
		if(THREEFINGERS_CAPTURE_SUPPORT && isThreeFingerScreenShotEnable())
			mScreenShot.handleEvent(event);

        // Aurora <Felix.Duan> <2014-7-26> <BEGIN> Feature: navigation bar swipe down/right
        // now we handle event for navi bar hide
        if (mMask == 0) 
			// steve.tang, at the begin, wo only use this lisenter to capture, now not support Pull-up(recent view)
			// shield by steve.tang, it may be reopen.
            handleEvent(event);
        // Aurora <Felix.Duan> <2014-7-26> <END> Feature: navigation bar swipe down/right

        //if (!mConsumed && mNext != null) {
        if (!mConsumeState.getState()) {
            // give to other filters
            //mNext.onMotionEvent(event,rawEvent,policyFlags);
        } else if (mResetLater) {
            mResetLater = false;
            mConsumeState.update(false);
            //if (mNext != null) {
            //    MotionEvent cancelEvent = MotionEvent.obtain(event);
            //    cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
            //    mNext.onMotionEvent(cancelEvent,rawEvent,policyFlags);
            //}
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

                // Aurora <Felix.Duan> <2014-5-31> <BEGIN> Fix BUG #5287. Not reset state at beginning
                //try {
                //    mKeyguardOn = mWindowManager.isKeyguardLocked();
                //} catch (RemoteException re) {
                //    /* ignore */
                //}
                //if (mKeyguardOn || !mIsBound) {
                //    log("mKeyguardOn = true DOWN");
                //    return;
                //}
                // Aurora <Felix.Duan> <2014-5-31> <END> Fix BUG #5287. Not reset state at beginning
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
        // Aurora <Felix.Duan> <2014-7-26> <BEGIN> Feature: navigation bar swipe down/right
        // Use X for landscape
        mOrientation = mResources.getConfiguration().orientation;
        // update navigation bat status
        mNavigationBarHidden = isNavigationBarHidden();
        // Aurora <Felix.Duan> <2014-7-26> <END> Feature: navigation bar swipe down/right

        // Aurora <Felix.Duan> <2014-5-31> <BEGIN> Fix BUG #5287. Not reset state at beginning
        mKeyguardOn = false;
        try {
			//steve.tang, the mWindowManager maybe null. i do not know why.
			if(mWindowManager != null)
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

        logv("detectTracking() mOrientation = " + mOrientation);
        logv("detectTracking() currentY = " + currentY + "  downY = " + downY);
        logv("detectTracking() currentX = " + currentX + "  downX = " + downX);
        logv("detectTracking() absX = " + absX + "  absY = " + absY);
        logv("detectTracking() distance = " + distance + "  duration = " + duration);

        // Aurora <Felix.Duan> <2014-9-25> <BEGIN> Support pull up recent panel on kitkat
        // Aurora <Felix.Duan> <2014-7-26> <BEGIN> Feature: navigation bar swipe down/right
        // consume when navigation bar showing & draging long enough from hot zone
        if (mHasNaviBar 
                && mNavigationBarHidden
                && ((mOrientation == Configuration.ORIENTATION_PORTRAIT?downY:downX) > mThreshold)
                && distance > mDist/*DIST_THRESHOLD*/)
            consumeEvent(event);
        // Aurora <Felix.Duan> <2014-7-26> <END> Feature: navigation bar swipe down/right
        else if (PULL_UP_RECENTS
                && (downY > mThreshold)
                && distance > mDist/*DIST_THRESHOLD*/)
            consumeEvent(event);
        // Aurora <Felix.Duan> <2014-9-25> <END> Support pull up recent panel on kitkat
    }

    // Cancel on ACTION_UP or ACTION_CANCEL, reset state
    private void cancelTracking(MotionEvent event) {
        logv("cancelTracking()");
        mDowningFlag = false;
        mDownEventSent = false;
        //mConsumed = false;
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
        log("consumeEvent() mHasNaviBar = " + mHasNaviBar );
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

		// currently this feature disabled on U3 kitkat with navi bar
        // Aurora <Felix.Duan> <2014-9-25> <BEGIN> Support pull up recent panel on kitkat
        if (mIsBound) {
        // Aurora <Felix.Duan> <2014-9-25> <END> Support pull up recent panel on kitkat
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
        
        // Aurora <Felix.Duan> <2014-7-26> <BEGIN> Feature: navigation bar swipe down/right
        // Show up navi bar when triggered
        if (mHasNaviBar) {
            if (showNaviBarRunnable != null) {
                mHandler.removeCallbacks(showNaviBarRunnable);
                mHandler.postDelayed(showNaviBarRunnable, 100); 
            }
        }
        // Aurora <Felix.Duan> <2014-7-26> <END> Feature: navigation bar swipe down/right
	}
    //private static long sLastMoveTime = 0;
    //private static final int GAP = 35;
    //private boolean timeOut(MotionEvent event) {
    //    long gap =  event.getEventTime() - sLastMoveTime;
    //    log("timeOut() event.getEventTime() = " + event.getEventTime() + " sLastMoveTime = " + sLastMoveTime + "   gap = " + gap);
    //    if (gap > GAP)
    //        return true;
    //    else
    //        return false;
    //}

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

    // Aurora <Felix.Duan> <2014-7-26> <BEGIN> Feature: navigation bar swipe down/right
    private static final String NAVI_KEY_HIDE = "navigation_key_hide";
    private ShowNaviBarRunnable showNaviBarRunnable = null;
    boolean mHasNaviBar = false;
    boolean mNavigationBarHidden = false;
    Handler mHandler = null;
    private int mOrientation  = Configuration.ORIENTATION_PORTRAIT;

    private final class ShowNaviBarRunnable implements Runnable {
        @Override
        public void run() {
            log("showNaviBar");
            ContentValues values = new ContentValues();
            values.put("name", NAVI_KEY_HIDE);
            values.put("value", 0);
            ContentResolver cr = mContext.getContentResolver();
            cr.insert(Settings.System.CONTENT_URI, values);
        }
    }
    private boolean isNavigationBarHidden() {
            int status = Settings.System.getInt(
                    mContext.getContentResolver(),
                    NAVI_KEY_HIDE, 0 /*default */);
            return status != 0;
        }
    // Aurora <Felix.Duan> <2014-7-26> <END> Feature: navigation bar swipe down/right

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

    private boolean isIUNI_U3() {
        boolean isU3 = ("U3".equals(SystemProperties.get("ro.product.model")));
        Log.d("felixxp", "isU3 = " + isU3);
        return isU3;
    }
}
