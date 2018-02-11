package com.android.phone;

import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.Phone;

import static com.android.phone.TimeConsumingPreferenceActivity.RESPONSE_ERROR;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.android.internal.telephony.Phone;

import aurora.preference.*;
import aurora.app.*;
import aurora.widget.*;
import android.content.res.TypedArray;

public class CallWaitingCheckBoxPreference extends AuroraSwitchPreference {
    private static final String LOG_TAG = "CallWaitingCheckBoxPreference";
    private final boolean DBG = (PhoneGlobals.DBG_LEVEL >= 2);

    private final MyHandler mHandler = new MyHandler();
    private Phone mPhone;
    private TimeConsumingPreferenceListener mTcpListener;
    private int mSubscription = 0;    

    public CallWaitingCheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mPhone = PhoneGlobals.getPhone();
    }

    public CallWaitingCheckBoxPreference(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.switchPreferenceStyle);
    	TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.sim_icon_slot); 
		mSubscription = a.getInt(R.styleable.sim_icon_slot_icon_slot, -1); 	
	    Log.d(LOG_TAG, "CallWaitingCheckBoxPreference create, mSubscription = " + mSubscription);
	    if(mSubscription != -1) {
	    	mPhone = PhoneGlobals.getInstance().getPhone(mSubscription);
	    } else {
	    	mPhone = PhoneGlobals.getPhone();
	    }
    }

    public CallWaitingCheckBoxPreference(Context context) {
        this(context, null);
    }

    /*package*/ void init(TimeConsumingPreferenceListener listener,
            boolean skipReading, int subscription) {
        // Get the selected subscription
        if (DBG)
            Log.d(LOG_TAG, "CallWaitingCheckBoxPreference init, subscription :" + subscription);
        if(mSubscription == -1) {
        	mPhone = PhoneGlobals.getInstance().getPhone(subscription);
        }

        mTcpListener = listener;

        if (!skipReading) {
            mPhone.getCallWaiting(mHandler.obtainMessage(MyHandler.MESSAGE_GET_CALL_WAITING,
                    MyHandler.MESSAGE_GET_CALL_WAITING, MyHandler.MESSAGE_GET_CALL_WAITING));
            if (mTcpListener != null) {
                mTcpListener.onStarted(this, true);
            }
            Message m = mHandler.obtainMessage(MyHandler.MESSAGE_GET_CF_TIMEOUT);
            mHandler.sendMessageDelayed(m, 5000);
        }
    }

    @Override
    protected void onClick() {
        super.onClick();
	        Log.d("CallWaitingCheckBoxPreference", "onClick");
        mPhone.setCallWaiting(isChecked(),
                mHandler.obtainMessage(MyHandler.MESSAGE_SET_CALL_WAITING));
        mHandler.sendEmptyMessageDelayed(MyHandler.MESSAGE_SET_CF_TIMEOUT, 40 * 1000);
        if (mTcpListener != null) {
            mTcpListener.onStarted(this, false);
        }
    }

    private class MyHandler extends Handler {
        static final int MESSAGE_GET_CALL_WAITING = 0;
        static final int MESSAGE_SET_CALL_WAITING = 1;
        static final int MESSAGE_GET_CF_TIMEOUT= 2;
        static final  int MESSAGE_SET_CF_TIMEOUT = 3;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_GET_CALL_WAITING:
                    handleGetCallWaitingResponse(msg);
                    break;
                case MESSAGE_SET_CALL_WAITING:
                    handleSetCallWaitingResponse(msg);
                    break;
                case MESSAGE_GET_CF_TIMEOUT:
                	mTcpListener.onFinished(CallWaitingCheckBoxPreference.this, true);
                	break;
                case MESSAGE_SET_CF_TIMEOUT:
                    mTcpListener.onFinished(CallWaitingCheckBoxPreference.this, false);
                	break;
            }
        }

        private void handleGetCallWaitingResponse(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;

            mHandler.removeMessages(MESSAGE_SET_CF_TIMEOUT);
            if (mTcpListener != null) {
                if (msg.arg2 == MESSAGE_SET_CALL_WAITING) {
                    mTcpListener.onFinished(CallWaitingCheckBoxPreference.this, false);
                } else {
                    mTcpListener.onFinished(CallWaitingCheckBoxPreference.this, true);
                }
            }

            if (ar.exception != null) {
                if (DBG) {
                    Log.d(LOG_TAG, "handleGetCallWaitingResponse: ar.exception=" + ar.exception);
                }
                if (mTcpListener != null) {
                    mTcpListener.onException(CallWaitingCheckBoxPreference.this,
                            (CommandException)ar.exception);
                }
            } else if (ar.userObj instanceof Throwable) {
                if (mTcpListener != null) {
                    mTcpListener.onError(CallWaitingCheckBoxPreference.this, RESPONSE_ERROR);
                }
            } else {
                if (DBG) {
                    Log.d(LOG_TAG, "handleGetCallWaitingResponse: CW state successfully queried.");
                }
                int[] cwArray = (int[])ar.result;
                // If cwArray[0] is = 1, then cwArray[1] must follow,
                // with the TS 27.007 service class bit vector of services
                // for which call waiting is enabled.
                try {
                    setChecked(((cwArray[0] == 1) && ((cwArray[1] & 0x01) == 0x01)));
                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.e(LOG_TAG, "handleGetCallWaitingResponse: improper result: err ="
                            + e.getMessage());
                }
            }
        }

        private void handleSetCallWaitingResponse(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;

            if (ar.exception != null) {
                if (DBG) {
                    Log.d(LOG_TAG, "handleSetCallWaitingResponse: ar.exception=" + ar.exception);
                }
                //setEnabled(false);
            }
            if (DBG) Log.d(LOG_TAG, "handleSetCallWaitingResponse: re get");

            mPhone.getCallWaiting(obtainMessage(MESSAGE_GET_CALL_WAITING,
                    MESSAGE_SET_CALL_WAITING, MESSAGE_SET_CALL_WAITING, ar.exception));
        }
    }
    
    @Override
    protected void onBindView(View view) {
        super.onBindView(view);        
        AuroraSwitch mAuroraSwitch  = (AuroraSwitch)view.findViewById(com.aurora.R.id.aurora_switchWidget);        
       	mAuroraSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {       		
     		@Override
     		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
     	        Log.d("CallWaitingCheckBoxPreference", "onCheckedChanged: isChecked = " + isChecked);
     	       mPhone.setCallWaiting(isChecked,
     	                mHandler.obtainMessage(MyHandler.MESSAGE_SET_CALL_WAITING));
     	        if (mTcpListener != null) {
     	            mTcpListener.onStarted(CallWaitingCheckBoxPreference.this, false);
     	        }
     		}         	
     	});              
    }
}
