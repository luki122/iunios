package com.aurora.callsetting;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.MenuItem;

import java.util.ArrayList;
import aurora.app.*;
import aurora.preference.*;

import static com.aurora.callsetting.AuroraMSimConstants.SUBSCRIPTION_KEY;

public class GsmUmtsAdditionalCallOptions extends
        TimeConsumingPreferenceActivity {
    private static final String LOG_TAG = "GsmUmtsAdditionalCallOptions";
    private final boolean DBG = (PhoneGlobals.DBG_LEVEL >= 2);

    private static final String BUTTON_CLIR_KEY  = "button_clir_key";
    private static final String BUTTON_CW_KEY    = "button_cw_key";

    private CLIRListPreference mCLIRButton;
    private CallWaitingCheckBoxPreference mCWButton;

    private final ArrayList<AuroraPreference> mPreferences = new ArrayList<AuroraPreference>();
    private int mInitIndex= 0;
    private int mSubscription = 0;
    private boolean mFirstResume;
    private Bundle mIcicle;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.gsm_umts_additional_options);
        
        getAuroraActionBar().setTitle(R.string.labelCW);

        mSubscription = AuroraPhoneUtils.getSlot(getIntent());
        if (DBG)
            Log.d(LOG_TAG, "GsmUmtsAdditionalCallOptions onCreate, subscription: " + mSubscription);
        AuroraPreferenceScreen prefSet = getPreferenceScreen();
        mCLIRButton = (CLIRListPreference) prefSet.findPreference(BUTTON_CLIR_KEY);
        mCWButton = (CallWaitingCheckBoxPreference) prefSet.findPreference(BUTTON_CW_KEY);

//        mPreferences.add(mCLIRButton);
        mPreferences.add(mCWButton);

//        if (icicle == null) {
//            if (DBG) Log.d(LOG_TAG, "start to init ");
////            mCLIRButton.init(this, false);
//       	    mCWButton.init(this, false);
//        } else {
//            if (DBG) Log.d(LOG_TAG, "restore stored states");
//            mInitIndex = mPreferences.size();
////            mCLIRButton.init(this, true);
//            mCWButton.init(this, false);
////            int[] clirArray = icicle.getIntArray(mCLIRButton.getKey());
////            if (clirArray != null) {
////                if (DBG) Log.d(LOG_TAG, "onCreate:  clirArray[0]="
////                        + clirArray[0] + ", clirArray[1]=" + clirArray[1]);
////                mCLIRButton.handleGetCLIRResult(clirArray);
////            } else {
////                mCLIRButton.init(this, false);
////            }
//        }
        mFirstResume = true;
        mIcicle = icicle;

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // android.R.id.home will be triggered in onOptionsItemSelected()
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        
   	   if(mCLIRButton != null) { 
			 prefSet.removePreference(mCLIRButton);
	   }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mCLIRButton.clirArray != null) {
            outState.putIntArray(mCLIRButton.getKey(), mCLIRButton.clirArray);
        }
    }

    @Override
    public void onFinished(AuroraPreference preference, boolean reading) {
        if (mInitIndex < mPreferences.size()-1 && !isFinishing()) {
            mInitIndex++;
            AuroraPreference pref = mPreferences.get(mInitIndex);
            if (pref instanceof CallWaitingCheckBoxPreference) {
                ((CallWaitingCheckBoxPreference) pref).init(this, false, mSubscription);
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
            if (mIcicle == null) {
            	mCWButton.init(this, false, mSubscription);
            } else {
		        mInitIndex = mPreferences.size();
		        mCWButton.init(this, false, mSubscription);
            }
            mFirstResume = false;
            mIcicle=null;
        }
    }
}
