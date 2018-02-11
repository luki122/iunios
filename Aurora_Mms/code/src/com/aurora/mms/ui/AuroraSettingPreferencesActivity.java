package com.aurora.mms.ui;
// Aurora xuyong 2014-09-29 for bug #8949 created
import com.android.mms.MmsApp;
import com.android.mms.R;

import com.gionee.internal.telephony.GnTelephonyManagerEx;
import gionee.telephony.GnTelephonyManager;
import gionee.provider.GnTelephony.SIMInfo;
import com.android.internal.telephony.TelephonyIntents;
import gionee.provider.GnTelephony.SimInfo;

import aurora.app.AuroraProgressDialog;
import aurora.preference.AuroraListPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraPreferenceManager;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraSwitchPreference;
import aurora.widget.AuroraActionBar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class AuroraSettingPreferencesActivity extends AuroraPreferenceActivity implements AuroraPreference.OnPreferenceChangeListener {

    private static final String TAG = "AuroraSettingPreferencesActivity";
    
    private boolean mNeedShowSSCN = false;
    
    //sms settings
    public static final String SMS_DELIVERY_REPORT_MODE = "pref_key_sms_delivery_reports";
    // Aurora xuyong 2015-12-15 deleted for aurora 2.0 new feature start
    //public static final String SMS_MANAGE_SIM_MESSAGES  = "pref_key_manage_sim_messages";
    // Aurora xuyong 2015-12-15 deleted for aurora 2.0 new feature end
    
    //mms settings
    public static final String MMS_DELIVERY_REPORT_MODE = "pref_key_mms_delivery_reports";
    public static final String READ_REPORT_MODE         = "pref_key_mms_read_reports";
    public static final String AUTO_RETRIEVAL           = "pref_key_mms_auto_retrieval";
    public static final String RETRIEVAL_DURING_ROAMING = "pref_key_mms_retrieval_during_roaming";
    
    //service center
    public static final String MESSAGE_SERVICE_CENTER   = "pref_aurora_key_sms_service_center";
    public static final String AURORA_SIM1_SMS_CENTER  = "pref_key_sms_center_sim1";
    public static final String AURORA_SIM2_SMS_CENTER  = "pref_key_sms_center_sim2";
    
    private GnTelephonyManagerEx mTelephonyManager;

    private AuroraSwitchPreference mSmsDeliveryReport;
    // Aurora xuyong 2015-12-15 deleted for aurora 2.0 new feature start
    //private AuroraPreference mManageSimPref;
    // Aurora xuyong 2015-12-15 deleted for aurora 2.0 new feature end
    
    private AuroraSwitchPreference mMmsDeliveryReport;
    private AuroraSwitchPreference mMmsReadReport;
    private AuroraSwitchPreference mMmsAutoRetrieval;
    private AuroraSwitchPreference mMmsRetrievalDuringRoaming;
    
    private AuroraPreferenceCategory mSmsCenterCategory;
    private AuroraSmsCenterPreference mSim1SmsCenterPreference;
    private AuroraSmsCenterPreference mSim2SmsCenterPreference;
    
    private IntentFilter mSimStateChangedFilter = new IntentFilter();
    private BroadcastReceiver mSimStateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(intent.getAction())
                    || intent.getAction().equals("android.intent.action.PHB_STATE_CHANGED")) {
                initSmsCenterPeferences();
             // Aurora xuyong 2014-10-25 added for bug #9340 start
             // Aurora xuyong 2015-12-15 deleted for aurora 2.0 new feature start
             //   setManageSimPref();
             // Aurora xuyong 2015-12-15 deleted for aurora 2.0 new feature end
             // Aurora xuyong 2014-10-25 added for bug #9340 end
            }
        }
    };
    
    private ContentObserver mSimInfoObserver = new ContentObserver(new Handler()) {

            @Override
            public void onChange(boolean selfChange) { 
                 super.onChange(selfChange);
                 initSmsCenterPeferences();
                 // Aurora xuyong 2014-10-25 added for bug #9340 start
                 // Aurora xuyong 2015-12-15 deleted for aurora 2.0 new feature start
                 //setManageSimPref();
                 // Aurora xuyong 2015-12-15 deleted for aurora 2.0 new feature end
                 // Aurora xuyong 2014-10-25 added for bug #9340 end
            }   

        };
    
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        AuroraActionBar actionBar = getAuroraActionBar();
        actionBar.setTitle(R.string.menu_preferences);
        actionBar.setDisplayHomeAsUpEnabled(true);
        setMessagePreferences();
    }
    
    private void setMessagePreferences() {
        addPreferencesFromResource(R.xml.aurora_message_setting_preferences);
        init();
    }
    
    private void init() {
        initSMSSettingCategory();
        initMMSSettingCategory();
        initScCategory();
        initValues();
    }
    
    private void initSMSSettingCategory() {
        mSmsDeliveryReport = (AuroraSwitchPreference) findPreference(SMS_DELIVERY_REPORT_MODE);
        // Aurora xuyong 2015-12-15 deleted for aurora 2.0 new feature start
        //mManageSimPref     = findPreference(SMS_MANAGE_SIM_MESSAGES);
        // Aurora xuyong 2015-12-15 deleted for aurora 2.0 new feature end
      // Aurora xuyong 2014-10-25 added for bug #9340 start
        // Aurora xuyong 2015-12-15 deleted for aurora 2.0 new feature start
        //setManageSimPref();
        // Aurora xuyong 2015-12-15 deleted for aurora 2.0 new feature end
      // Aurora xuyong 2014-10-25 added for bug #9340 end
    }
    // Aurora xuyong 2014-10-25 added for bug #9340 start
    // Aurora xuyong 2015-12-15 deleted for aurora 2.0 new feature start
    /*private void setManageSimPref() {
        if (MmsApp.mGnMultiSimMessage) {
            if (!GnTelephonyManager.hasIccCardGemini(0) && !GnTelephonyManager.hasIccCardGemini(1) && mManageSimPref != null) {
                mManageSimPref.setEnabled(false);
            } else if (mManageSimPref != null) {
                   mManageSimPref.setEnabled(true);
               }
        } else {
             if (!GnTelephonyManager.hasIccCard() && mManageSimPref != null) {
                 mManageSimPref.setEnabled(false);
             } else if (mManageSimPref != null) {
                 mManageSimPref.setEnabled(true);
             }
        }
    }*/
    // Aurora xuyong 2015-12-15 deleted for aurora 2.0 new feature end
    // Aurora xuyong 2014-10-25 added for bug #9340 end
    private void initMMSSettingCategory() {
        mMmsDeliveryReport = (AuroraSwitchPreference) findPreference(MMS_DELIVERY_REPORT_MODE);
        mMmsReadReport     = (AuroraSwitchPreference) findPreference(READ_REPORT_MODE);
        mMmsAutoRetrieval  = (AuroraSwitchPreference) findPreference(AUTO_RETRIEVAL);
        // Aurora xuyong 2014-10-22 added for bug #9227 start
        mMmsAutoRetrieval.setOnPreferenceChangeListener(this);
        // Aurora xuyong 2014-10-22 added for bug #9227 end
        mMmsRetrievalDuringRoaming = (AuroraSwitchPreference) findPreference(RETRIEVAL_DURING_ROAMING);
    }
    
    private void initValues() {
        SharedPreferences sp = getSharedPreferences("com.android.mms_preferences", MODE_WORLD_READABLE);
        if (mSmsDeliveryReport != null) {
            if (MmsApp.mHasIndiaFeature) {
                mSmsDeliveryReport.setChecked(sp.getBoolean(mSmsDeliveryReport.getKey(), true));
            } else {
                mSmsDeliveryReport.setChecked(sp.getBoolean(mSmsDeliveryReport.getKey(), false));
            }
        }
        if (mMmsDeliveryReport != null) {
            mMmsDeliveryReport.setChecked(sp.getBoolean(mMmsDeliveryReport.getKey(), true));
        }
        if (mMmsReadReport != null) {
            mMmsReadReport.setChecked(sp.getBoolean(mMmsReadReport.getKey(), true));
        }
        if (mMmsAutoRetrieval != null) {
            mMmsAutoRetrieval.setChecked(sp.getBoolean(mMmsAutoRetrieval.getKey(), false));
        }
        if (mMmsRetrievalDuringRoaming != null) {
            mMmsRetrievalDuringRoaming.setChecked(sp.getBoolean(mMmsRetrievalDuringRoaming.getKey(), false));
        }
        this.getContentResolver().registerContentObserver(SimInfo.CONTENT_URI, true, mSimInfoObserver);
          mSimStateChangedFilter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
          mSimStateChangedFilter.addAction("android.intent.action.PHB_STATE_CHANGED");
          registerReceiver(mSimStateChangedReceiver, mSimStateChangedFilter);
    }
    
    private void initScCategory() {
        mSmsCenterCategory = (AuroraPreferenceCategory)findPreference("pref_aurora_key_sms_service_center");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }
    
    @Override
    protected void onPause(){
        super.onPause();
    }
    
    @Override
    protected void onDestroy() {
        unregisterReceiver(mSimStateChangedReceiver);
           if (mSimInfoObserver != null) {
               this.getContentResolver().unregisterContentObserver(mSimInfoObserver);
               mSimInfoObserver = null;
           }
        super.onDestroy();
    }
    
    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen,
            AuroraPreference preference) {
        // Aurora xuyong 2015-12-15 deleted for aurora 2.0 new feature start
        /*if (preference == mManageSimPref) {
            if(MmsApp.mGnMultiSimMessage == true){
                Intent intent = new Intent();
                if (SIMInfo.getInsertedSIMCount(this) == 1) {
                    int slotId = SIMInfo.getInsertedSIMList(this).get(0).mSlot;
                    if (slotId != -1) {
                        intent.putExtra("SlotId", slotId);
                    }
                }                      
                intent.setClass(this, AuroraMultiSimManageActivity.class);
                startActivity(intent);
            } else {
                startActivity(new Intent(this, AuroraManageSimMessages.class));
            }
        }*/
        // Aurora xuyong 2015-12-15 deleted for aurora 2.0 new feature end
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    
    @Override
    public boolean onPreferenceChange(AuroraPreference arg0, Object arg1) {
        final String key = arg0.getKey();
      // Aurora xuyong 2014-10-22 deleted for bug #9227 start
        //final String stored = (String)arg1;
      // Aurora xuyong 2014-10-22 deleted for bug #9227 end
        if (AURORA_SIM1_SMS_CENTER.equals(key)) {
          // Aurora xuyong 2014-10-22 added for bug #9227 start
            final String stored1 = (String)arg1;
          // Aurora xuyong 2014-10-22 added for bug #9227 end
            if (mSim1SmsCenterPreference != null) {
             // Aurora xuyong 2014-10-22 modified for bug #9227 start
                final String newSmsCenterNumber1 = stored1;
             // Aurora xuyong 2014-10-22 modified for bug #9227 end
                 mSim1SmsCenterPreference.setSmsCenterNumber(newSmsCenterNumber1);
                 mTelephonyManager = GnTelephonyManagerEx.getDefault();
                 new Thread(new Runnable() {
                     public void run() {
                        if (MmsApp.mQcMultiSimEnabled) {
                            String newSmsCenterNumber = "\"" + newSmsCenterNumber1 + mSlot1ScAddressEntireNail;
                             mTelephonyManager.setScAddress(newSmsCenterNumber, 0);
                            } else {
                                mTelephonyManager.setScAddress(newSmsCenterNumber1, 0);
                            }
                     }
                 }).start();
            }
        } else if (AURORA_SIM2_SMS_CENTER.equals(key)) {
          // Aurora xuyong 2014-10-22 added for bug #9227 start
            final String stored2 = (String)arg1;
          // Aurora xuyong 2014-10-22 added for bug #9227 end
            if (mSim2SmsCenterPreference != null) {
             // Aurora xuyong 2014-10-22 modified for bug #9227 start
                final String newSmsCenterNumber2 = stored2;
             // Aurora xuyong 2014-10-22 modified for bug #9227 end
                mSim2SmsCenterPreference.setSmsCenterNumber(newSmsCenterNumber2);
                mTelephonyManager = GnTelephonyManagerEx.getDefault();
                new Thread(new Runnable() {
                    public void run() {
                        if (MmsApp.mQcMultiSimEnabled) {
                            String newSmsCenterNumber = "\"" + newSmsCenterNumber2 + mSlot2ScAddressEntireNail;
                            mTelephonyManager.setScAddress(newSmsCenterNumber, 1);
                           } else {
                               mTelephonyManager.setScAddress(newSmsCenterNumber2, 1);
                           }
                    }
                }).start();
            }
         // Aurora xuyong 2014-08-04 added for sms center feature end
        // Aurora xuyong 2014-10-22 added for bug #9227 start
        } else if (AUTO_RETRIEVAL.equals(key)) {
            final Boolean stored3 = (Boolean)arg1;
            if (mMmsRetrievalDuringRoaming != null) {
                if (!stored3.booleanValue()) {
                    SharedPreferences sp = getSharedPreferences("com.android.mms_preferences", MODE_WORLD_READABLE);
                   if (sp.getBoolean(RETRIEVAL_DURING_ROAMING, false)) {
                       mMmsRetrievalDuringRoaming.setChecked(false);
                   }
                }
            }
        }
        // Aurora xuyong 2014-10-22 added for bug #9227 end
        return true;
    }
    
    private void initSmsCenterPeferences() {
        mTelephonyManager = GnTelephonyManagerEx.getDefault();
        if (mSmsCenterCategory == null) {
            return;
        }
        if (!mNeedShowSSCN) {
            getPreferenceScreen().removePreference(mSmsCenterCategory);
            return;
        }
        if (MmsApp.mGnMultiSimMessage) {
            SIMInfo info1 = SIMInfo.getSIMInfoBySlot(this, 0);
            SIMInfo info2 = SIMInfo.getSIMInfoBySlot(this, 1);
            mSim1SmsCenterPreference = (AuroraSmsCenterPreference)findPreference(AURORA_SIM1_SMS_CENTER);
            mSim2SmsCenterPreference = (AuroraSmsCenterPreference)findPreference(AURORA_SIM2_SMS_CENTER);
            if (mSim1SmsCenterPreference != null) {
                mSim1SmsCenterPreference.setDialogTitle(R.string.aurora_sms_center_tip);
                mSim1SmsCenterPreference.setPositiveButtonText(R.string.aurora_sms_center_modify);
                mSim1SmsCenterPreference.setNegativeButtonText(R.string.cancel);
                mSim1SmsCenterPreference.setOnPreferenceChangeListener(this);
            }
            if (mSim2SmsCenterPreference != null) {
                mSim2SmsCenterPreference = (AuroraSmsCenterPreference)findPreference(AURORA_SIM2_SMS_CENTER);
                mSim2SmsCenterPreference.setDialogTitle(R.string.aurora_sms_center_tip);
                mSim2SmsCenterPreference.setPositiveButtonText(R.string.aurora_sms_center_modify);
                mSim2SmsCenterPreference.setNegativeButtonText(R.string.cancel);
                mSim2SmsCenterPreference.setOnPreferenceChangeListener(this);
            }
            if (info1 == null && info2 == null) {
                getPreferenceScreen().removePreference(mSmsCenterCategory);
            } else if (info1 == null) {
                getPreferenceScreen().addPreference(mSmsCenterCategory);
                if (mSim1SmsCenterPreference != null) {
                    mSmsCenterCategory.removePreference(mSim1SmsCenterPreference);
                }
                if (mSim2SmsCenterPreference != null) {
                    mSim2SmsCenterPreference.setOperator(GnTelephonyManager.getSimOperatorGemini(1));
                    mSim2SmsCenterPreference.setSimThumbNail(1);
                    mSim2SmsCenterPreference.setSmsCenterNumber(rebuidScAddress(mTelephonyManager.getScAddress(1), 1));
                }
            } else if (info2 == null) {
                getPreferenceScreen().addPreference(mSmsCenterCategory);
                if (mSim2SmsCenterPreference != null) {
                    mSmsCenterCategory.removePreference(mSim2SmsCenterPreference);
                }
                if (mSim1SmsCenterPreference != null) {
                    mSim1SmsCenterPreference.setOperator(GnTelephonyManager.getSimOperatorGemini(0));
                    mSim1SmsCenterPreference.setSimThumbNail(0);
                    mSim1SmsCenterPreference.setSmsCenterNumber(rebuidScAddress(mTelephonyManager.getScAddress(0), 0));
                }
            } else {
                getPreferenceScreen().addPreference(mSmsCenterCategory);
                if (mSim1SmsCenterPreference != null) {
                    mSim1SmsCenterPreference.setOperator(GnTelephonyManager.getSimOperatorGemini(0));
                    mSim1SmsCenterPreference.setSimThumbNail(0);
                    mSim1SmsCenterPreference.setSmsCenterNumber(rebuidScAddress(mTelephonyManager.getScAddress(0), 0));
                }
                if (mSim2SmsCenterPreference != null) {
                    mSim2SmsCenterPreference.setOperator(GnTelephonyManager.getSimOperatorGemini(0));
                    mSim2SmsCenterPreference.setSimThumbNail(1);
                    mSim2SmsCenterPreference.setSmsCenterNumber(rebuidScAddress(mTelephonyManager.getScAddress(1), 1));
                }
            }
        } else {
            if (!GnTelephonyManager.hasIccCard()) {
                getPreferenceScreen().removePreference(mSmsCenterCategory);
            } else {
                getPreferenceScreen().addPreference(mSmsCenterCategory);
                mSim1SmsCenterPreference = (AuroraSmsCenterPreference)findPreference(AURORA_SIM1_SMS_CENTER);
                mSim2SmsCenterPreference = (AuroraSmsCenterPreference)findPreference(AURORA_SIM2_SMS_CENTER);
                if (mSim1SmsCenterPreference != null) {
                    mSim1SmsCenterPreference.setDialogTitle(R.string.aurora_sms_center_tip);
                    mSim1SmsCenterPreference.setPositiveButtonText(R.string.aurora_sms_center_modify);
                    mSim1SmsCenterPreference.setNegativeButtonText(R.string.cancel);
                    mSim1SmsCenterPreference.setOnPreferenceChangeListener(this);
                }
                if (mSim2SmsCenterPreference != null) {
                    mSim2SmsCenterPreference = (AuroraSmsCenterPreference)findPreference(AURORA_SIM2_SMS_CENTER);
                    mSim2SmsCenterPreference.setDialogTitle(R.string.aurora_sms_center_tip);
                    mSim2SmsCenterPreference.setPositiveButtonText(R.string.aurora_sms_center_modify);
                    mSim2SmsCenterPreference.setNegativeButtonText(R.string.cancel);
                    mSim2SmsCenterPreference.setOnPreferenceChangeListener(this);
                }
                if (mSim2SmsCenterPreference != null) {
                    mSmsCenterCategory.removePreference(mSim2SmsCenterPreference);
                }
                if (mSim1SmsCenterPreference != null) {
                    mSim1SmsCenterPreference.setOperator(GnTelephonyManager.getSimOperatorGemini(0));
                    mSim1SmsCenterPreference.setSmsCenterNumber(rebuidScAddress(mTelephonyManager.getScAddress(0), 0));
                }
            }
        }
    }
    
    private String mSlot1ScAddressEntireNail = null;
    private String mSlot2ScAddressEntireNail = null;
    private String rebuidScAddress(String gotScNumber, int slot) {
        if (MmsApp.mQcMultiSimEnabled) {
            if (gotScNumber != null && !"".equals(gotScNumber)) {
                Log.d(TAG, "getServiceCenter is:" + gotScNumber + " before substring.");
                int index = gotScNumber.lastIndexOf("\"");
                switch (slot) {
                    case 0:
                        mSlot1ScAddressEntireNail = gotScNumber.substring(index);
                        break;
                    case 1:
                        mSlot2ScAddressEntireNail = gotScNumber.substring(index);
                        break;
                }
                gotScNumber = gotScNumber.substring(1, index);
            } else {
                Log.e(TAG, "getServiceCenter is: fail !");
            }
        }
        return gotScNumber;
    }
}
