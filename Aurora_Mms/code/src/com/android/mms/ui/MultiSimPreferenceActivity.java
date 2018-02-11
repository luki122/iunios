package com.android.mms.ui;

import java.util.List;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.R;
import com.android.mms.ui.AdvancedCheckBoxPreference.GetSimInfo;

import android.R.color;
// Aurora liugj 2013-09-13 modified for aurora's new feature start
import android.app.ActionBar;
// Aurora liugj 2013-09-13 modified for aurora's new feature end
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import gionee.provider.GnTelephony.SIMInfo;
import android.provider.Telephony;
import com.gionee.internal.telephony.GnTelephonyManagerEx;
//Gionee:linggz 2012-7-4 modify for CR00637047 begin
import android.os.SystemProperties;
//Gionee:linggz 2012-7-4 modify for CR00637047 end

public class MultiSimPreferenceActivity extends AuroraPreferenceActivity implements GetSimInfo{
    private static final String TAG = "MultiSimPreferenceActivity";
    
    private AdvancedCheckBoxPreference mSim1;
    private AdvancedCheckBoxPreference mSim2;
    private AdvancedCheckBoxPreference mSim3;
    private AdvancedCheckBoxPreference mSim4;
    
    private int simCount;
    private List<SIMInfo> listSimInfo;
    
    private GnTelephonyManagerEx mTelephonyManager;

    protected void onCreate(Bundle icicle) {
        //gionee gaoj 2012-6-27 added for CR00628364 start
        if (MmsApp.mLightTheme) {
            setTheme(R.style.GnMmsLightTheme);
        } else if (MmsApp.mDarkStyle) {
            setTheme(R.style.GnMmsDarkTheme);
        }
        //gionee gaoj 2012-6-27 added for CR00628364 end
        super.onCreate(icicle);
        listSimInfo = SIMInfo.getInsertedSIMList(this);
        simCount = listSimInfo.size();

        addPreferencesFromResource(R.xml.multicardselection);    
        Intent intent = getIntent();
        String preference = intent.getStringExtra("preference");
        //translate key to SIM-related key;
        Log.i("MultiSimPreferenceActivity, getIntent:", intent.toString());
        Log.i("MultiSimPreferenceActivity, getpreference:", preference);

        changeMultiCardKeyToSimRelated(preference);
        
        //gionee gaoj 2012-5-29 added for CR00555790 start
        if (MmsApp.mGnMessageSupport) {
            // Aurora liugj 2013-09-13 modified for aurora's new feature start
            ActionBar actionBar = getActionBar();
            // Aurora liugj 2013-09-13 modified for aurora's new feature end
            actionBar.setDisplayShowHomeEnabled(false);
            //gionee gaoj added for CR00725602 20121201 start
            actionBar.setDisplayHomeAsUpEnabled(true);
            //gionee gaoj added for CR00725602 20121201 end
        }
        //gionee gaoj 2012-5-29 added for CR00555790 end
    }
    
    //gionee gaoj added for CR00725602 20121201 start
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            break;

        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    //gionee gaoj added for CR00725602 20121201 end

    private void changeMultiCardKeyToSimRelated(String preference) {

        mSim1 = (AdvancedCheckBoxPreference) findPreference("pref_key_sim1");
        mSim1.init(this, 0);
        mSim2 = (AdvancedCheckBoxPreference) findPreference("pref_key_sim2");
        mSim2.init(this, 1);
        mSim3 = (AdvancedCheckBoxPreference) findPreference("pref_key_sim3");
        mSim3.init(this, 2);
        mSim4 = (AdvancedCheckBoxPreference) findPreference("pref_key_sim4");
        mSim4.init(this, 3);
        //get the stored value
        SharedPreferences sp = getSharedPreferences("com.android.mms_preferences", MODE_WORLD_READABLE);
        

        if (simCount == 1) {
            getPreferenceScreen().removePreference(mSim2);
            getPreferenceScreen().removePreference(mSim3);
            getPreferenceScreen().removePreference(mSim4);
            mSim1.setKey(Long.toString(listSimInfo.get(0).mSimId) + "_" + preference);
            if (preference.equals(MessagingPreferenceActivity.RETRIEVAL_DURING_ROAMING)) {
                mSim1.setEnabled(sp.getBoolean(Long.toString(listSimInfo.get(0).mSimId)    
                       + "_" + MessagingPreferenceActivity.AUTO_RETRIEVAL, true));
            }
        } else if (simCount == 2) {
            getPreferenceScreen().removePreference(mSim3);
            getPreferenceScreen().removePreference(mSim4);
            
            mSim1.setKey(Long.toString(listSimInfo.get(0).mSimId) + "_" + preference);
            mSim2.setKey(Long.toString(listSimInfo.get(1).mSimId) + "_" + preference);
            if (preference.equals(MessagingPreferenceActivity.RETRIEVAL_DURING_ROAMING)) {
                mSim1.setEnabled(sp.getBoolean(Long.toString(listSimInfo.get(0).mSimId)    
                       + "_" + MessagingPreferenceActivity.AUTO_RETRIEVAL, true));
                mSim2.setEnabled(sp.getBoolean(Long.toString(listSimInfo.get(1).mSimId)    
                       + "_" + MessagingPreferenceActivity.AUTO_RETRIEVAL, true));
            }
        } else if (simCount == 3) {
            getPreferenceScreen().removePreference(mSim4);
        
            mSim1.setKey(Long.toString(listSimInfo.get(0).mSimId) + "_" + preference);
            mSim2.setKey(Long.toString(listSimInfo.get(1).mSimId) + "_" + preference);
            mSim3.setKey(Long.toString(listSimInfo.get(2).mSimId) + "_" + preference);
            if (preference.equals(MessagingPreferenceActivity.RETRIEVAL_DURING_ROAMING)) {
                mSim1.setEnabled(sp.getBoolean(Long.toString(listSimInfo.get(0).mSimId)    
                       + "_" + MessagingPreferenceActivity.AUTO_RETRIEVAL, true));
                mSim2.setEnabled(sp.getBoolean(Long.toString(listSimInfo.get(1).mSimId)    
                       + "_" + MessagingPreferenceActivity.AUTO_RETRIEVAL, true));
                mSim3.setEnabled(sp.getBoolean(Long.toString(listSimInfo.get(2).mSimId)    
                       + "_" + MessagingPreferenceActivity.AUTO_RETRIEVAL, true));
            }
        } else{
            
            mSim1.setKey(Long.toString(listSimInfo.get(0).mSimId) + "_" + preference);
            mSim2.setKey(Long.toString(listSimInfo.get(1).mSimId) + "_" + preference);
            mSim3.setKey(Long.toString(listSimInfo.get(2).mSimId) + "_" + preference);
            mSim4.setKey(Long.toString(listSimInfo.get(3).mSimId) + "_" + preference);
            if (preference.equals(MessagingPreferenceActivity.RETRIEVAL_DURING_ROAMING)) {
                mSim1.setEnabled(sp.getBoolean(Long.toString(listSimInfo.get(0).mSimId)    
                       + "_" + MessagingPreferenceActivity.AUTO_RETRIEVAL, true));
                mSim2.setEnabled(sp.getBoolean(Long.toString(listSimInfo.get(1).mSimId)    
                       + "_" + MessagingPreferenceActivity.AUTO_RETRIEVAL, true));
                mSim3.setEnabled(sp.getBoolean(Long.toString(listSimInfo.get(2).mSimId)    
                       + "_" + MessagingPreferenceActivity.AUTO_RETRIEVAL, true));
                mSim4.setEnabled(sp.getBoolean(Long.toString(listSimInfo.get(3).mSimId)    
                       + "_" + MessagingPreferenceActivity.AUTO_RETRIEVAL, true));
            } 
        }
        
        if (mSim1 != null) {
            if (preference.equals(MessagingPreferenceActivity.AUTO_RETRIEVAL) 
                    || preference.equals(MessagingPreferenceActivity.MMS_ENABLE_TO_SEND_DELIVERY_REPORT)) {
                mSim1.setChecked(sp.getBoolean(mSim1.getKey(), true));
            //Gionee:linggz 2012-7-4 modify for CR00637047 begin
            } else if (SystemProperties.get("ro.gn.oversea.custom").equals("RUSSIA_FLY") &&
                    preference.equals(MessagingPreferenceActivity.SMS_DELIVERY_REPORT_MODE)) {
                mSim1.setChecked(sp.getBoolean(mSim1.getKey(), true));
            //Gionee:linggz 2012-7-4 modify for CR00637047 end
        //Gionee:tangzepeng 2012-10-09 modify for polytron preset the deliver report as "on" begin
            } else if (SystemProperties.get("ro.gn.oversea.custom").equals("INDONESIA_POLYTRON") &&
                    preference.equals(MessagingPreferenceActivity.SMS_DELIVERY_REPORT_MODE)) {
                mSim1.setChecked(sp.getBoolean(mSim1.getKey(), true));
            //Gionee:tangzepeng 2012-10-09 modify for polytron preset the deliver report as "on" begin
            //Gionee:songganggang 2012-10-23 modify for CR00717052 begin
            } else if (SystemProperties.get("ro.gn.oversea.custom").equals("VISUALFAN") &&
                    preference.equals(MessagingPreferenceActivity.RETRIEVAL_DURING_ROAMING)) {
                mSim1.setChecked(sp.getBoolean(mSim1.getKey(), true));
            //Gionee:songganggang 2012-10-23 modify for CR00717052 end
            } else {
                mSim1.setChecked(sp.getBoolean(mSim1.getKey(), false));
            }
        }
        if (mSim2 != null) {
            if (preference.equals(MessagingPreferenceActivity.AUTO_RETRIEVAL) 
                    || preference.equals(MessagingPreferenceActivity.MMS_ENABLE_TO_SEND_DELIVERY_REPORT)) {
                mSim2.setChecked(sp.getBoolean(mSim2.getKey(), true));
            //Gionee:linggz 2012-7-4 modify for CR00637047 begin
            } else if (SystemProperties.get("ro.gn.oversea.custom").equals("RUSSIA_FLY") &&
                    preference.equals(MessagingPreferenceActivity.SMS_DELIVERY_REPORT_MODE)) {
                mSim2.setChecked(sp.getBoolean(mSim2.getKey(), true));
            //Gionee:linggz 2012-7-4 modify for CR00637047 end
        //Gionee:tangzepeng 2012-10-09 modify for polytron preset the deliver report as "on" begin
            } else if (SystemProperties.get("ro.gn.oversea.custom").equals("INDONESIA_POLYTRON") &&
                    preference.equals(MessagingPreferenceActivity.SMS_DELIVERY_REPORT_MODE)) {
                mSim2.setChecked(sp.getBoolean(mSim2.getKey(), true));
            //Gionee:tangzepeng 2012-10-09 modify for polytron preset the deliver report as "on" begin
            //Gionee:songganggang 2012-10-23 modify for CR00717052 begin
            } else if (SystemProperties.get("ro.gn.oversea.custom").equals("VISUALFAN") &&
                    preference.equals(MessagingPreferenceActivity.RETRIEVAL_DURING_ROAMING)) {
                mSim2.setChecked(sp.getBoolean(mSim2.getKey(), true));
            //Gionee:songganggang 2012-10-23 modify for CR00717052 end
            }  else {
                mSim2.setChecked(sp.getBoolean(mSim2.getKey(), false));
            }
        }
        if (mSim3 != null) {
            if (preference.equals(MessagingPreferenceActivity.AUTO_RETRIEVAL) 
                    || preference.equals(MessagingPreferenceActivity.MMS_ENABLE_TO_SEND_DELIVERY_REPORT)) {
                mSim3.setChecked(sp.getBoolean(mSim3.getKey(), true));
            //Gionee:linggz 2012-7-4 modify for CR00637047 begin
            } else if (SystemProperties.get("ro.gn.oversea.custom").equals("RUSSIA_FLY") &&
                    preference.equals(MessagingPreferenceActivity.SMS_DELIVERY_REPORT_MODE)) {
                mSim3.setChecked(sp.getBoolean(mSim3.getKey(), true));
            //Gionee:linggz 2012-7-4 modify for CR00637047 end
        //Gionee:tangzepeng 2012-10-09 modify for polytron preset the deliver report as "on" begin
            } else if (SystemProperties.get("ro.gn.oversea.custom").equals("INDONESIA_POLYTRON") &&
                    preference.equals(MessagingPreferenceActivity.SMS_DELIVERY_REPORT_MODE)) {
                mSim3.setChecked(sp.getBoolean(mSim3.getKey(), true));
            //Gionee:tangzepeng 2012-10-09 modify for polytron preset the deliver report as "on" begin
            //Gionee:songganggang 2012-10-23 modify for CR00717052 begin
            } else if (SystemProperties.get("ro.gn.oversea.custom").equals("VISUALFAN") &&
                    preference.equals(MessagingPreferenceActivity.RETRIEVAL_DURING_ROAMING)) {
                mSim3.setChecked(sp.getBoolean(mSim3.getKey(), true));
            //Gionee:songganggang 2012-10-23 modify for CR00717052 end
            } else {
                mSim3.setChecked(sp.getBoolean(mSim3.getKey(), false));
            }
        }
        if (mSim4 != null) {
            if (preference.equals(MessagingPreferenceActivity.AUTO_RETRIEVAL) 
                    || preference.equals(MessagingPreferenceActivity.MMS_ENABLE_TO_SEND_DELIVERY_REPORT)) {
                mSim4.setChecked(sp.getBoolean(mSim4.getKey(), true));
            //Gionee:linggz 2012-7-4 modify for CR00637047 begin
            } else if (SystemProperties.get("ro.gn.oversea.custom").equals("RUSSIA_FLY") &&
                    preference.equals(MessagingPreferenceActivity.SMS_DELIVERY_REPORT_MODE)) {
                mSim4.setChecked(sp.getBoolean(mSim4.getKey(), true));
            //Gionee:linggz 2012-7-4 modify for CR00637047 end
        //Gionee:tangzepeng 2012-10-09 modify for polytron preset the deliver report as "on" begin
            } else if (SystemProperties.get("ro.gn.oversea.custom").equals("INDONESIA_POLYTRON") &&
                    preference.equals(MessagingPreferenceActivity.SMS_DELIVERY_REPORT_MODE)) {
                mSim4.setChecked(sp.getBoolean(mSim4.getKey(), true));
            //Gionee:tangzepeng 2012-10-09 modify for polytron preset the deliver report as "on" begin
            //Gionee:songganggang 2012-10-23 modify for CR00717052 begin
            } else if (SystemProperties.get("ro.gn.oversea.custom").equals("VISUALFAN") &&
                    preference.equals(MessagingPreferenceActivity.RETRIEVAL_DURING_ROAMING)) {
                mSim4.setChecked(sp.getBoolean(mSim4.getKey(), true));
            //Gionee:songganggang 2012-10-23 modify for CR00717052 end
            } else {
                mSim4.setChecked(sp.getBoolean(mSim4.getKey(), false));
            }
        }
    }
    
    public String getSimName(int id) {
        return listSimInfo.get(id).mDisplayName;
    }

    
    public String getSimNumber(int id) {
        return listSimInfo.get(id).mNumber;
    }
    
    public int getSimColor(int id) {
        return listSimInfo.get(id).mSimBackgroundRes;
    }
    
    public int getNumberFormat(int id) {
        return listSimInfo.get(id).mDispalyNumberFormat;
    }
    
    public int getSimStatus(int id) {
        mTelephonyManager = GnTelephonyManagerEx.getDefault();
        //int slotId = SIMInfo.getSlotById(this,listSimInfo.get(id).mSimId);
        int slotId = listSimInfo.get(id).mSlot;
        if (slotId != -1) {
            return mTelephonyManager.getSimIndicatorStateGemini(slotId);
        }
        return -1;
    }
    
    public boolean is3G(int id)    {
        //int slotId = SIMInfo.getSlotById(this, listSimInfo.get(id).mSimId);
        int slotId = listSimInfo.get(id).mSlot;
        Log.i(TAG, "SIMInfo.getSlotById id: " + id + " slotId: " + slotId);
        if (slotId == MessageUtils.get3GCapabilitySIM()) {
            return true;
        }
        return false;
    }
}
