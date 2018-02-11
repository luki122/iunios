package com.android.phone;

import com.android.phone.AuroraTelephony.SIMInfo;
import com.android.phone.AuroraTelephony.SimInfo;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;

import aurora.preference.*;
import aurora.widget.*;
import com.codeaurora.telephony.msim.MSimPhoneFactory;
import com.codeaurora.telephony.msim.Subscription.SubscriptionStatus;
import com.codeaurora.telephony.msim.SubscriptionManager;

public class AuroraDataSpinnerPreference extends AuroraPreference {
    private static final String LOG_TAG = "AuroraDataSpinnerPreference";
    private static final boolean DBG = true;
    private AuroraSpinner mLabel;
    private String[] mNetworks;
    private String[] networkValues;
    private ArrayAdapter<String> mAdapter;
    private boolean mFirst = true;
    SubscriptionManager subManager = SubscriptionManager.getInstance();
    private Context mContext;
    static final int EVENT_SET_DATA_SUBSCRIPTION_DONE = 1;
    
    public AuroraDataSpinnerPreference(Context context) {
        this(context, null);
    }

    public AuroraDataSpinnerPreference(Context context, AttributeSet attrs) {
    	this(context, attrs, com.android.internal.R.attr.preferenceStyle);
    }

    public AuroraDataSpinnerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWidgetLayoutResource(R.layout.aurora_spinner);  
        mContext = context;
        mNetworks = mContext.getResources().getStringArray(R.array.aurora_3rd_sim_choices);
        networkValues = mContext.getResources().getStringArray(R.array.aurora_3rd_sim_choices_values);
        mAdapter = new ArrayAdapter<String>(mContext, com.aurora.R.layout.aurora_spinner_list_item, mNetworks);
    }
 
    
    @Override
    protected void onBindView(View view) {
        log("onBindView");
        super.onBindView(view);         
        mLabel = (AuroraSpinner) view.findViewById(R.id.edit_spinner);
        mLabel.setAdapter(mAdapter);  
        updatedataSubState();
        mFirst = true;
        mLabel.setOnItemSelectedListener(mSpinnerListener); 
        updateEnableState();
    }
 
    private OnItemSelectedListener mSpinnerListener = new OnItemSelectedListener() {

        @Override
        public void onItemSelected(
                AdapterView<?> parent, View view, int position, long id) {
            log("mSpinnerListener onItemSelected: position = " + position);
            if(mFirst) {
            	mFirst = false;
            	return;            
            }
        
            int prioritySubIndex = Integer.parseInt(networkValues[position]);
            if (subManager.getCurrentSubscription(prioritySubIndex).subStatus
                    == SubscriptionStatus.SUB_ACTIVATED) {                                  
                Message setDdsMsg = Message.obtain(mHandler, EVENT_SET_DATA_SUBSCRIPTION_DONE, null);
                subManager.setDataSubscription(prioritySubIndex, setDdsMsg);                
            } else {
                Toast.makeText(PhoneGlobals.getInstance(), R.string.set_priority_sub_error, Toast.LENGTH_SHORT).show();
            }
            
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };
      
    
    private void updatedataSubState() {
        int dataSub = MSimPhoneFactory.getDataSubscription();
        Log.d(LOG_TAG, "updatedataSubState: Data Subscription : = " + dataSub);
        if(mLabel != null) {
        	mLabel.setSelection(dataSub);
        }
    }
    
    
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			AsyncResult ar;

			switch (msg.what) {
			case EVENT_SET_DATA_SUBSCRIPTION_DONE:
				Log.d(LOG_TAG, "EVENT_SET_DATA_SUBSCRIPTION_DONE");
				
		        updatedataSubState();
		        updateEnableState();

				ar = (AsyncResult) msg.obj;
				String status;
				if (ar.exception != null) {
					status = mContext.getResources().getString(R.string.set_dds_error) + " " + ar.exception.getMessage();
					Toast toast = Toast.makeText(mContext,status, Toast.LENGTH_LONG);
					toast.show();
					break;
				}

				boolean result = (Boolean) ar.result;

				Log.d(LOG_TAG, "SET_DATA_SUBSCRIPTION_DONE: result = " + result);
				status = mContext.getResources().getString(result ? R.string.set_dds_success : R.string.set_dds_failed);
				Toast toast = Toast.makeText(mContext, status, Toast.LENGTH_LONG);
				toast.show();

				break;
			default:
				Log.w(LOG_TAG, "Unknown Event " + msg.what);
				break;
			}
		}
	};
    
    
    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }

    private static void loge(String msg) {
        Log.e(LOG_TAG, msg);
    }
    
    
    private void updateEnableState() {
    	boolean simOldstate[] = new boolean[SubscriptionManager.NUM_SUBSCRIPTIONS];  
        simOldstate[0] = false;
        simOldstate[1] = false;
        SharedPreferences mSharedPreferences = mContext.getSharedPreferences("sim_enable_state", Context.MODE_PRIVATE);
        SIMInfo sim1 = SIMInfo.getSIMInfoBySlot(mContext, 0);
        if(sim1 != null) {
        	simOldstate[0] = mSharedPreferences.getBoolean(String.valueOf(sim1.mSimId), true);
        } 
        SIMInfo sim2 = SIMInfo.getSIMInfoBySlot(mContext, 1);
        if(sim2 != null) {
        	simOldstate[1] = mSharedPreferences.getBoolean(String.valueOf(sim2.mSimId), true);
        }         
    	boolean enable = SIMInfo.getInsertedSIMCount(mContext) > 1;
    	enable = enable && simOldstate[0] && simOldstate[1];
    	this.setEnabled(enable); 
    	if(mLabel != null) {
    		mLabel.setEnabled(enable);
    	}
    }
    
    public void update(){
        updateEnableState();
    }

}
