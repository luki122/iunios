package com.android.phone;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;

import aurora.preference.*;
import aurora.widget.*;

public class AuroraImagePreferenceScreen extends AuroraPreference {
    private static final String LOG_TAG = "AuroraImagePreferenceScreen";
    private static final boolean DBG = true;
    private ImageView mImage, mArrow;
    private Context mContext;
    private int mSubscription = 0;
    private boolean mEnabled = false;
    
    public AuroraImagePreferenceScreen(Context context) {
        this(context, null);
    }

    public AuroraImagePreferenceScreen(Context context, AttributeSet attrs) {
    	this(context, attrs, com.android.internal.R.attr.preferenceStyle);
    }

    public AuroraImagePreferenceScreen(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWidgetLayoutResource(R.layout.aurora_image_preference); 
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.sim_icon_slot); 
		mSubscription = a.getInt(R.styleable.sim_icon_slot_icon_slot, 0); 				
		a.recycle(); 
        mContext = context;       
    }
     
    @Override
    protected void onBindView(View view) {
        log("onBindView");
        super.onBindView(view);         
        mImage = (ImageView) view.findViewById(R.id.sim_icon);
        mImage.setBackgroundResource(SimIconUtils.getSimIcon(mSubscription));    
        mArrow = (ImageView) view.findViewById(R.id.sim_icon_arrow);
   		mArrow.setImageResource(mEnabled ? R.drawable.right_arrow_normal:R.drawable.right_arrow_disable);
    } 
    
    public void setSimIcon(int resId) {
    	log("setSimIcon");
    	if(mImage!= null) {
        	log("setSimIcon enter");
    		mImage.setBackgroundResource(resId);
    	}
    }
    
    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }

    private static void loge(String msg) {
        Log.e(LOG_TAG, msg);
    }
    
    public void update(boolean enable){
    	mEnabled = enable;
    	this.setEnabled(mEnabled);
    	if(mImage!= null) {
        	log("setSimIcon enter");
    		mImage.setBackgroundResource(SimIconUtils.getSimIcon(mSubscription));
    	}
    	if(mArrow != null) {
    		mArrow.setImageResource(enable ? R.drawable.right_arrow_normal:R.drawable.right_arrow_disable);
    	}
    }
   
}
