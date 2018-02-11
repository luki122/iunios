package com.android.phone;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;

import com.android.internal.telephony.Phone;

import aurora.app.*;
import aurora.preference.*;
import aurora.widget.AuroraButton;
import aurora.widget.AuroraEditText;
import static com.android.phone.AuroraMSimConstants.SUBSCRIPTION_KEY;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.PhoneConstants;

import android.telephony.SubscriptionManager;
import android.os.SystemProperties;

public class MsimCallWaitingSettings extends
        TimeConsumingPreferenceActivity implements AuroraPreference.OnPreferenceChangeListener {
    private static final String LOG_TAG = "MsimCallWaitingSettings";
    private final boolean DBG = (PhoneGlobals.DBG_LEVEL >= 2);

    private static final String BUTTON_CW_KEY    = "button_cw_key_";
   
    private CallWaitingCheckBoxPreference[] mCWButton;

    private final ArrayList<CallWaitingCheckBoxPreference> mPreferences = new ArrayList<CallWaitingCheckBoxPreference>();
    private int mInitIndex= 0;
  
    private boolean mFirstResume;
    private Bundle mIcicle;
    private static int DIALOG_CDMA_CW = 1;
    
	private static final int mNumPhones = PhoneUtils.getPhoneCount();
    private AuroraPreference mSimCategory1, mSimCategory2;
    private AuroraPreference mCdmaSimCategory;
    private AuroraSwitchPreference mCdmaCwButton;
    
    private Phone[] phoneList;
    private int mSubId = -1;
    private Context mContext;
    private static final String[] CW_HEADERS = {
        SystemProperties.get("ro.cdma.cw.enable"), SystemProperties.get("ro.cdma.cw.disable")
    };

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        mContext = this;
    	phoneList = new Phone[2];
		phoneList[0] = PhoneGlobals.getInstance().getPhone(0);
		phoneList[1] = PhoneGlobals.getInstance().getPhone(1);
	    int phonetype1 = phoneList[0].getPhoneType();
        int phonetype2 = phoneList[1].getPhoneType();

        addPreferencesFromResource(R.xml.msim_call_waiting_settings);
        
        getAuroraActionBar().setTitle(R.string.labelCW);

        AuroraPreferenceScreen prefSet =  (AuroraPreferenceScreen)findPreference("main");;
        
        boolean iscard1Enable = PhoneUtils.getSimState(0) != TelephonyManager.SIM_STATE_ABSENT;
     		boolean iscard2Enable = PhoneUtils.getSimState(1) != TelephonyManager.SIM_STATE_ABSENT;
             mSimCategory1= findPreference("sim1_category_key");
             mSimCategory2= findPreference("sim2_category_key");
      	    mSimCategory1.setEnabled(iscard1Enable);
      	    mSimCategory2.setEnabled(iscard2Enable);	
      	  mCdmaSimCategory= findPreference("cdma_category_key");
       
        mCWButton = new CallWaitingCheckBoxPreference[mNumPhones];
        for (int i = 0; i < mNumPhones; ++i) {
            mCWButton[i] = (CallWaitingCheckBoxPreference) prefSet.findPreference(BUTTON_CW_KEY+i);
        }

        mCdmaCwButton = (AuroraSwitchPreference)prefSet.findPreference("cdma_button_cw_key");
        mCdmaCwButton.setOnPreferenceChangeListener(this);
        
        if(iscard1Enable && phonetype1 != PhoneConstants.PHONE_TYPE_CDMA ) {
        	 mPreferences.add(mCWButton[0]);
        }  else {
        	prefSet.removePreference(mSimCategory1);
        }
        
        if(iscard2Enable && phonetype2 != PhoneConstants.PHONE_TYPE_CDMA) {
       	     mPreferences.add(mCWButton[1]);
       }  else {
       	   prefSet.removePreference(mSimCategory2);
       }
        
        if(phonetype1 == PhoneConstants.PHONE_TYPE_CDMA && iscard1Enable) {
        	mCdmaSimCategory.setTitle(R.string.sub_1);
        	mSubId =  AuroraSubUtils.getSubIdbySlot(this, 0);
        }else if(phonetype2 == PhoneConstants.PHONE_TYPE_CDMA && iscard2Enable) {
        	mCdmaSimCategory.setTitle(R.string.sub_2);
        	mSubId =  AuroraSubUtils.getSubIdbySlot(this, 1);
        } else {
        	prefSet.removePreference(mCdmaSimCategory);
        }

        mFirstResume = true;
        mIcicle = icicle;

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // android.R.id.home will be triggered in onOptionsItemSelected()
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
           	
    }


    @Override
    public void onFinished(AuroraPreference preference, boolean reading) {
        if (mInitIndex < mPreferences.size()-1 && !isFinishing()) {
            mInitIndex++;
            AuroraPreference pref = mPreferences.get(mInitIndex);
            if (pref instanceof CallWaitingCheckBoxPreference) {
                ((CallWaitingCheckBoxPreference) pref).init(this, false, -1);
            }
        }
        super.onFinished(preference, reading);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {  // See ActionBar#setDisplayHomeAsUpEnabled()
            CallFeaturesSetting.goUpToTopLevelSetting(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onResume() {
        super.onResume();

        if (mFirstResume) {
        	if(mPreferences.size() == 0) {
        		
        	} else if (mIcicle == null) {
            	mPreferences.get(mInitIndex).init(this, false, -1);
            } else {
		        mInitIndex = mPreferences.size();
		        mPreferences.get(0).init(this, false, -1);
            }
            mFirstResume = false;
            mIcicle=null;
        }
    }
    
//    @Override
//    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
//        if (preference == mCdmaCwButton) {
//            showDialog(DIALOG_CDMA_CW);
//        }
//        return true;
//    }
    
    public boolean onPreferenceChange(AuroraPreference preference,
			Object objValue) {
    	if (preference == mCdmaCwButton) {
			boolean isChecked = (Boolean) objValue;
			  int select = isChecked ? 0 : 1;
		     String cw = CW_HEADERS[select];
             setCallWait(cw);
    	}
    	return true;
    }
    
    @Override
    protected Dialog onCreateDialog(final int id) {
     	if(id > 1 ) {
    		return super.onCreateDialog(id);
    	}
    	return createDialog();
    }
    
    /**
     * Create the call wait setting dialog.
     *
     * @return the created dialog object.
     */
    public AuroraDialog createDialog() {
        final AuroraDialog dialog = new AuroraDialog(mContext);
        dialog.setContentView(R.layout.mtk_cdma_cf_dialog);
        dialog.setTitle(R.string.labelCW);

        final RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.group);

        final TextView textView = (TextView) dialog.findViewById(R.id.dialog_sum);
        if (textView != null) {
            textView.setVisibility(View.GONE);
        } else {
            Log.d(LOG_TAG, "--------------[text view is null]---------------");
        }

        AuroraEditText editText = (AuroraEditText) dialog.findViewById(R.id.EditNumber);
        if (editText != null) {
            editText.setVisibility(View.GONE);
        }

        ImageButton addContactBtn = (ImageButton) dialog.findViewById(R.id.select_contact);
        if (addContactBtn != null) {
            addContactBtn.setVisibility(View.GONE);
        }

        AuroraButton dialogSaveBtn = (AuroraButton) dialog.findViewById(R.id.save);
        if (dialogSaveBtn != null) {
            dialogSaveBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (radioGroup.getCheckedRadioButtonId() == -1) {
                        dialog.dismiss();
                        return;
                    }
                    int select = radioGroup.getCheckedRadioButtonId() == R.id.enable ? 0 : 1;
                    String cw = CW_HEADERS[select];
                    dialog.dismiss();
                    setCallWait(cw);
                }
            });
        }

        AuroraButton dialogCancelBtn = (AuroraButton) dialog.findViewById(R.id.cancel);
        if (dialogCancelBtn != null) {
                dialogCancelBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }
        return dialog;
    }

    /**
     * Create the call wait setting dialog.
     *
     * @param cw the string need to pass to dailer.
     */
    private void setCallWait(String cw) {
        Log.d(LOG_TAG, "[setCallWait][cw = " + cw + "], subId = " + mSubId);
        if (mSubId == SubscriptionManager.INVALID_SUBSCRIPTION_ID ||
            cw == null || cw.isEmpty()) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + cw));
//        ComponentName pstnConnectionServiceName =
//            new ComponentName(mContext, TelephonyConnectionService.class);
//        String id = "" + String.valueOf(mSubId);
//        PhoneAccountHandle phoneAccountHandle =
//            new PhoneAccountHandle(pstnConnectionServiceName, id);
//        intent.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(SUBSCRIPTION_KEY, AuroraSubUtils.getSlotBySubId(this, mSubId));
        mContext.startActivity(intent);
    }
}
