package com.mediatek.contacts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Country;
import android.location.CountryDetector;
import android.location.CountryListener;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyIntents;

import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;

import java.util.HashMap;
import com.aurora.android.contacts.AuroraTelephonyManager;

public class HyphonManager implements CountryListener{

    private static final String TAG = "HyphonManager/Contacts";
    private static boolean DBG = true;

    private static HyphonManager sMe;

     
    private HashMap<String, String> mHyphonMaps = new HashMap<String, String>();

     
    private Context mContext;

    private String mCurrentCountryIso;

    private BroadcastReceiver mHyphonReceiver = new HyphonReceiver();

    private HyphonManager(Context context) {
        log("HyphonManager()");
        mContext = context;
        mCurrentCountryIso = detectCountry();

        TelephonyManager telephonyManager =
            (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if(FeatureOption.MTK_GEMINI_SUPPORT) {
            AuroraTelephonyManager.listenGemini(mPhoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE, ContactsFeatureConstants.GEMINI_SIM_1);
            AuroraTelephonyManager.listenGemini(mPhoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE, ContactsFeatureConstants.GEMINI_SIM_2);
        } else {
            telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        mContext.registerReceiver(mHyphonReceiver, intentFilter);
    }

    public static HyphonManager getInstance() {
        if(sMe == null) {
            sMe = new HyphonManager(ContactsApplication.getInstance());
        }

        return sMe;
    }

    public static void destroy() {
        if(sMe != null)
            sMe.onDestroy();
    }

    public String formatNumber(String number) {
        if(mCurrentCountryIso == null) {
            log("mCurrentCountryIso is null, re-detect");
            // try to detect country if it's null
            mCurrentCountryIso = detectCountry();
        }

        String match = mHyphonMaps.get(number);

        if(match != null)
            return match;

        match = PhoneNumberUtils.formatNumber(number, mCurrentCountryIso);

        // invalid number...
        if(match != null)
            mHyphonMaps.put(number, match);
        else
            match = number;

        return match;
    }

    protected void onDestroy() {
        mContext.unregisterReceiver(mHyphonReceiver);
    }

    void log(String msg) {
        if(DBG) Log.d(TAG, msg);
    }

    public void setCountryIso(String countryIso) {
        log("setCountryIso, mCurrentCountryIso = " + mCurrentCountryIso + " countryIso = " + countryIso);
        if(mCurrentCountryIso != null && !mCurrentCountryIso.equals(countryIso)) {
            mCurrentCountryIso = countryIso;
            mHyphonMaps.clear();
        }
    }

    String detectCountry() {
        try {
            CountryDetector detector =
                (CountryDetector) mContext.getSystemService(Context.COUNTRY_DETECTOR);
            detector.addCountryListener(this, null);
            final Country country = detector.detectCountry();
            if(country != null) {
                log("detect country, iso = " + country.getCountryIso() + " source = " + country.getSource());
                return country.getCountryIso();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void onCountryDetected(Country country) {
        log("onCountryDetected, country = " + country);
        setCountryIso(country.getCountryIso());
    }

    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onServiceStateChanged(ServiceState serviceState) {
            if(serviceState.getState() == ServiceState.STATE_IN_SERVICE) {
                log("STATE_IN_SERVICE re-detect country iso");
                setCountryIso(detectCountry());
            }
        }
    };

    class HyphonReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(intent.getAction())) {
                log("ACTION_SIM_STATE_CHANGED , intent = " + intent.getExtras());
                //mHyphonMaps.clear();
                //mCurrentCountryIso = getCurrentCountryIso();
            }
        }
    };
}
