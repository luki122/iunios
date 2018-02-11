package com.android.phone;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.Preference;
import android.provider.Settings;
import android.telephony.ServiceState;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.util.AttributeSet;
import android.util.Log;

import com.mediatek.internal.telephony.ITelephonyEx;

import android.telephony.SubscriptionManager;
import aurora.preference.*;
import aurora.app.*;
import aurora.widget.*;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.SubscriptionInfo;

/**
 * A preference for radio switch function.
 */
public class RadioPowerPreference extends AuroraSwitchPreference {
    private static final String TAG = "RadioPowerPreference";
    protected boolean mPowerState;
    protected boolean mPowerEnabled = true;
    protected OnCheckedChangeListener mListener;
    private ITelephonyEx mTelephonyEx;
    private boolean mIsAirplaneModeOn = false;
    private static final boolean RADIO_POWER_OFF = false;
    private static final boolean RADIO_POWER_ON = true;
    private static final int MODE_PHONE1_ONLY = 1;
    private Context mContext;
    private int mSubscription = 0;
    /**
     * Construct of RadioPowerPreference.
     * @param context Context.
     */
    public RadioPowerPreference(Context context) {
        super(context, null);      
    }
    
    public RadioPowerPreference(Context context, AttributeSet attrs) {
    	this(context, attrs, com.android.internal.R.attr.switchPreferenceStyle);
    }

    public RadioPowerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mTelephonyEx = ITelephonyEx.Stub.asInterface(
                ServiceManager.getService(Context.TELEPHONY_SERVICE_EX));
        mIsAirplaneModeOn = TelephonyUtils.isAirplaneModeOn(context);     
    }

    /**
     * Set the radio switch state.
     * @param state On/off.
     */
    public void setRadioOn(boolean state) {
        mPowerState = state;
        notifyChanged();
    }

    /**
     * Set the radio switch enable state.
     * @param enable Enable.
     */
    public void setRadioEnabled(boolean enable) {
        mPowerEnabled = enable;
        notifyChanged();
    }

    /**
     * Set the listener for radio switch.
     * @param listener Listener of {@link CheckedChangeListener}.
     */
    public void setRadioPowerChangeListener(OnCheckedChangeListener listener) {
        mListener = listener;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        AuroraSwitch mAuroraSwitch  = (AuroraSwitch)view.findViewById(com.aurora.R.id.aurora_switchWidget);   
        if (mAuroraSwitch != null) {
        	mAuroraSwitch.setChecked(mPowerState);
        	mAuroraSwitch.setEnabled(mPowerEnabled);
        	mAuroraSwitch.setOnCheckedChangeListener(mListener);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        mPowerEnabled = enabled;
        super.setEnabled(enabled);
    }
    
    public void update(SubscriptionInfo mSubInfoRecord) {
        if (mSubInfoRecord != null) {
            setEnabled(true);
            setRadioEnabled(!mIsAirplaneModeOn
                    && isRadioSwitchComplete(mSubInfoRecord.getSubscriptionId()));
            setRadioOn(TelephonyUtils.isRadioOn(mSubInfoRecord.getSubscriptionId()));
            /// @}
        } else {
            setEnabled(false);
        }
    }
    
    /**
     * whether radio switch finish about subId.
     * @param subId subId
     * @return true finish
     */
    private boolean isRadioSwitchComplete(int subId) {
        boolean isComplete = true;
        int slotId = SubscriptionManager.getSlotId(subId);
        if (SubscriptionManager.isValidSlotId(slotId)) {
            Bundle bundle = null;
            try {
                if (mTelephonyEx != null) {
                    bundle = mTelephonyEx.getServiceState(subId);
                } else {
                    Log.d(TAG, "mTelephonyEx is null, returen false");
                }
            } catch (RemoteException e) {
                isComplete = false;
                Log.d(TAG, "getServiceState() error, subId: " + subId);
                e.printStackTrace();
            }
            if (bundle != null) {
                ServiceState serviceState = ServiceState.newFromBundle(bundle);
                isComplete = isRadioSwitchComplete(subId, serviceState);
            }
        }
        Log.d(TAG, "isRadioSwitchComplete(" + subId + ")" + ", slotId: " + slotId
                + ", isComplete: " + isComplete);
        return isComplete;
    }

    private boolean isRadioSwitchComplete(final int subId, ServiceState state) {
        int slotId = SubscriptionManager.getSlotId(subId);
        boolean radiosState = getRadioStateForSlotId(slotId);
        Log.d(TAG, "soltId: " + slotId + ", radiosState is : " + radiosState);
        if (radiosState && (state.getState() != ServiceState.STATE_POWER_OFF)) {
            return true;
        } else if (state.getState() == ServiceState.STATE_POWER_OFF) {
            return true;
        }
        return false;
    }
    

    private boolean getRadioStateForSlotId(final int slotId) {
        int currentSimMode = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.MSIM_MODE_SETTING, -1);
        boolean radiosState = ((currentSimMode & (MODE_PHONE1_ONLY << slotId)) == 0) ?
                RADIO_POWER_OFF : RADIO_POWER_ON;
        Log.d(TAG, "soltId: " + slotId + ", radiosState : " + radiosState);
        return radiosState;
    }
    
}
