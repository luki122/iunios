/*
 * File Description:defined a CheckBoxPreference , add a widget Switch
 * Author:baorui
 * Create Date:2012/11/23
 * Change List:CR00733082
 */

package com.android.deskclock;


import aurora.widget.AuroraSwitch;

import android.content.Context;
import aurora.preference.AuroraCheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class GnVibratePreference extends AuroraCheckBoxPreference {

    interface OnPreferenceClickCallback {
        public void onPreferenceClick();
    }
    
    private Context mContext;
    private AuroraSwitch mSwitch;
    private boolean mVibrateOnOff;
    private OnPreferenceClickCallback mClickCallback;
    private OnCheckedChangeListener mSwitchChangeListener;
    
    public GnVibratePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        // TODO Auto-generated constructor stub
    }

    public GnVibratePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public GnVibratePreference(Context context) {
        super(context, null);
        mContext = context;
    }

    @Override
    protected void onBindView(View view) {
        // TODO Auto-generated method stub
        super.onBindView(view);
        
        AuroraSwitch mSwitch = (AuroraSwitch) view.findViewById(R.id.vibrate_switch);
        if (mSwitch != null && mSwitch instanceof Checkable) {
            mSwitch.setClickable(true);
            mSwitch.setChecked(mVibrateOnOff);
            mSwitch.setOnCheckedChangeListener(mSwitchChangeListener);
        }
    }
    
    void setSwitchClickListener(OnCheckedChangeListener listener) {
        mSwitchChangeListener = listener;
    }

    void setClickCallback(OnPreferenceClickCallback callback) {
        mClickCallback = callback;
    }

    boolean isSwitchOn() {
        return mVibrateOnOff;
    }

    void setSwitchOn(boolean switchOn) {
        mVibrateOnOff = switchOn;
    }
}
