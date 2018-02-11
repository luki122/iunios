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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import gionee.telephony.GnTelephonyManager;
import com.android.contacts.R;
public class HyphonManager implements CountryListener{

    private static final String TAG = "HyphonManager/Contacts";
    private static boolean DBG = true;

    private static HyphonManager sMe;

    protected HashMap<String, String> mHyphonMaps = new HashMap<String, String>();

    protected Context mContext;

    protected String mCurrentCountryIso;

    protected BroadcastReceiver mHyphonReceiver = new HyphonReceiver();

    private HyphonManager(Context context) {
        log("HyphonManager()");
        mContext = context;
        mCurrentCountryIso = detectCountry();

        TelephonyManager telephonyManager =
            (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if(FeatureOption.MTK_GEMINI_SUPPORT) {
            GnTelephonyManager.listenGemini(mPhoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE, ContactsFeatureConstants.GEMINI_SIM_1);
            GnTelephonyManager.listenGemini(mPhoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE, ContactsFeatureConstants.GEMINI_SIM_2);
        } else {
            telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        mContext.registerReceiver(mHyphonReceiver, intentFilter);
        
        formatPattern1 = mContext.getResources().getString(R.string.today_string); 
    	formatPattern2 = mContext.getResources().getString(R.string.yesterday_string);
    	formatPattern3 = mContext.getResources().getString(R.string.formatPattern3);
    	formatPattern4 =mContext.getResources().getString(R.string.formatPattern4);
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
    
    /**
	 * 格式化时间
	 * @param timeStamp
	 * @return
	 */
    String formatPattern1;
	String formatPattern2;
	String formatPattern3;
	String formatPattern4;
	Date date;
	Calendar current;
	Calendar today;
	Calendar yesterday;
	Calendar thisyear;
	public String formatDateTime(long timeStamp) {
		date=new Date(timeStamp);	

		//liyang add:
		current = Calendar.getInstance();//当前

		today = Calendar.getInstance();	//今天		
		today.set(Calendar.YEAR, current.get(Calendar.YEAR));
		today.set(Calendar.MONTH, current.get(Calendar.MONTH));
		today.set(Calendar.DAY_OF_MONTH,current.get(Calendar.DAY_OF_MONTH));
		//  Calendar.HOUR——12小时制的小时数 Calendar.HOUR_OF_DAY——24小时制的小时数
		today.set( Calendar.HOUR_OF_DAY, 0);
		today.set( Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);

		yesterday = Calendar.getInstance();	//昨天		
		yesterday.set(Calendar.YEAR, current.get(Calendar.YEAR));
		yesterday.set(Calendar.MONTH, current.get(Calendar.MONTH));
		yesterday.set(Calendar.DAY_OF_MONTH,current.get(Calendar.DAY_OF_MONTH)-1);
		yesterday.set( Calendar.HOUR_OF_DAY, 0);
		yesterday.set( Calendar.MINUTE, 0);
		yesterday.set(Calendar.SECOND, 0);

		thisyear = Calendar.getInstance();	//今年	
		thisyear.set(Calendar.YEAR, current.get(Calendar.YEAR));
		thisyear.set(Calendar.MONTH, 0);
		thisyear.set(Calendar.DAY_OF_MONTH,0);
		thisyear.set( Calendar.HOUR_OF_DAY, 0);
		thisyear.set( Calendar.MINUTE, 0);
		thisyear.set(Calendar.SECOND, 0);
		//liyang add end.


		current.setTime(date);

		//		return new SimpleDateFormat(formatPattern4).format(date);

		if(current.after(today)){
			return formatPattern1;
		}else if(current.before(today) && current.after(yesterday)){
			return formatPattern2;
		}else if(current.before(thisyear)){
			return new SimpleDateFormat(formatPattern4).format(date);  
		}else{
			return new SimpleDateFormat(formatPattern3).format(date); 
		}
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
