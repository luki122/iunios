package com.android.systemui.recent;

import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.DisplayMetrics; 
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class InvokerService extends Service {

    private static final String TAG = "InvokerService";

    /** Keeps track of all current registered clients. */
    ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    /** Holds last value set by a client. */
    int mValue = 0;

    // Aurora <Felix.Duan> <2014-9-22> <BEGIN> Pull up feature fits all display
    private int mScreenHeight; // Y-axis offset value
    // Aurora <Felix.Duan> <2014-9-22> <END> Pull up feature fits all display

    /**
     * Command to the service to register a client, receiving callbacks from the
     * service. The Message's replyTo field must be a Messenger of the client
     * where callbacks should be sent.
     */
    static final int MSG_REGISTER_CLIENT = 1;

    /**
     * Command to the service to unregister a client, ot stop receiving
     * callbacks from the service. The Message's replyTo field must be a
     * Messenger of the client as previously given with MSG_REGISTER_CLIENT.
     */
    static final int MSG_UNREGISTER_CLIENT = 2;

    /**
     * Command to service to get MotionEvent.
     */
    static final int MSG_SEND_EVENT = 3;

    /**
     *Command to client to update it`s enable state.
     */
    static final int MSG_UPDATE_MSK = 4;

    private static int sMask = 0;
    static final int MSK_DISABLE = 0X01; // Disable consuming events.
    static final int MSK_KEYBOARD_INVIEW = 0X01<<1; // Keyboard in view, disable consuming events temporary.

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            log("handleMessage()  " + msg.what);
            switch (msg.what) {
            case MSG_REGISTER_CLIENT:
                mClients.add(msg.replyTo);
                break;
            case MSG_UNREGISTER_CLIENT:
                mClients.remove(msg.replyTo);
                break;
            case MSG_SEND_EVENT:
                if (msg.obj != null) {
                    MotionEvent event = (MotionEvent) msg.obj;
                    log("HandlerMessage() MotionEvent : " + event.toString());
                    if (sView != null) {
                        // Aurora <Felix.Duan> <2015-1-29> <BEGIN> Support landscape recents panel pull out
                        // Aurora <Felix.Duan> <2014-9-22> <BEGIN> Pull up feature fits all display
                        if (mLandscape)
                            event.offsetLocation(-mScreenHeight, 0); // from a View`s viewpoint
                        else
                            event.offsetLocation(0, -mScreenHeight); // from a View`s viewpoint
                        // Aurora <Felix.Duan> <2014-9-22> <END> Pull up feature fits all display
                        // Aurora <Felix.Duan> <2015-1-29> <END> Support landscape recents panel pull out
                        sView.dispatchTouchEvent(event);
                    }
                    else {
                        log("HandlerMessage() sView = null");
                        sendBroadcast(new Intent(HandlerBar.AURORA_BIND_INVOKER_SERVICE));
                    }
                } else
                    log("HandlerMessage() msg.obj = null");
                break;
            case MSG_UPDATE_MSK:
                for (int i = mClients.size() - 1; i >= 0; i--) {
                    try {
                        mClients.get(i).send(
                                Message.obtain(null, MSG_UPDATE_MSK, sMask, 0));
                    } catch (RemoteException e) {
                        // The client is dead. Remove it from the list;
                        // we are going through the list from back to front
                        // so this is safe to do inside the loop.
                        mClients.remove(i);
                    }
                }
                break;
            default:
                super.handleMessage(msg);
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    private IncomingHandler mHandler = new IncomingHandler();
    final Messenger mMessenger = new Messenger(mHandler);
    private static InvokerService sInstance = null;
    private static View sView = null;
	// Aurora <Felix.Duan> <2015-1-29> <BEGIN> Support landscape recents panel pull out
    public static boolean mLandscape;
	// Aurora <Felix.Duan> <2015-1-29> <END> Support landscape recents panel pull out

    @Override
    public void onCreate() {
        log("onCreate()");
        sInstance = this;
		// Aurora <Steve.Tang> 2014-08-01 reset aurorapointerinterceptor state as this service restart. start
		sendBroadcast(new Intent("com.android.systemui.recent.AURORA_RESET_PI_STATE"));
		// Aurora <Steve.Tang> 2014-08-01 reset aurorapointerinterceptor state as this service restart. end

        // Aurora <Felix.Duan> <2014-9-22> <BEGIN> Pull up feature fits all display
        /*
         * Pull up view is at bottom of screen
         * Offset MotionEvent Y-axis value through whole screen height
         */
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        mScreenHeight = metrics.heightPixels;
        // Aurora <Felix.Duan> <2014-9-22> <END> Pull up feature fits all display

        // Aurora <Felix.Duan> <2014-11-13> <BEGIN> Fix BUG #9458
        sendBroadcast(new Intent(HandlerBar.AURORA_BIND_INVOKER_VIEW));
        // Aurora <Felix.Duan> <2014-11-13> <END> Fix BUG #9458
        // Aurora <Felix.Duan> <2015-1-29> <BEGIN> Support landscape recents panel pull out
		int orientation = getResources().getConfiguration().orientation;
        mLandscape =(orientation == Configuration.ORIENTATION_LANDSCAPE); 
        // Aurora <Felix.Duan> <2015-1-29> <END> Support landscape recents panel pull out
    }

    @Override
    public void onDestroy() {
        log("onDestroy()");
        sInstance = null;
    }

    /**
     * When binding to the service, we return an interface to our messenger for
     * sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        log("onBind()");
        return mMessenger.getBinder();
    }

    public void setView(View v) {
        log("setVIew()");
        sView = v;
    }

    public static InvokerService getInstance() {
        return sInstance;
    }

    private void updateMask() {
        mHandler.removeMessages(MSG_UPDATE_MSK);
        mHandler.obtainMessage(MSG_UPDATE_MSK).sendToTarget();
    }

    void updatePanelStatus(boolean enable) {
        if (enable)
            sMask &= ~MSK_DISABLE;
        else
            sMask |= MSK_DISABLE;
        log("updatePanelStatus() enable = " + enable + "  sMask = " + sMask);
        updateMask();
    }

    void updateInputMethodStatus(boolean showing) {
        if (showing)
            sMask |= MSK_KEYBOARD_INVIEW;
        else
            sMask &= ~MSK_KEYBOARD_INVIEW;
        log("updateInputMethodStatus() sMask = " + sMask);
        updateMask();
    }

    private void log(String msg) {
        Log.d(TAG, msg);
    }
}
