package com.gionee.mms.ui;

import java.io.FileNotFoundException;
import java.io.IOException;
// Aurora liugj 2013-09-13 modified for aurora's new feature start
import android.app.ActionBar;
// Aurora liugj 2013-09-13 modified for aurora's new feature end
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceGroup;
import aurora.preference.AuroraPreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;

import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.MessagingPreferenceActivity;
// Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
//import com.mediatek.audioprofile.AudioProfileManager;
//import com.mediatek.audioprofile.AudioProfileManager.Scenario;
// Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end

public class GnRingtonePreference extends AuroraPreferenceActivity {

    public static String KEY_SIM1_RINGTONE = "sim1_notification";
    public static String KEY_SIM2_RINGTONE = "sim2_notification";
    public static String KEY_SIM1_SUMMARY = "sim1_notification_summary";
    public static String KEY_SIM2_SUMMARY = "sim1_notification_summary";
    public static String KEY_DEFAULT_RINGTONE = "def_notification";
    private String   mKey;
    private MmsRingtonePreference ringtone1, ringtone2;
    // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
    //private AudioProfileManager mProfileManager = null;
    // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
    private String TAG = "GnRingtonePreference";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        //Gionee zengxuanhui 20120903 add for CR00686714 begin
        if (MmsApp.mLightTheme) {
            setTheme(R.style.GnMmsLightTheme);
        } else if (MmsApp.mDarkStyle) {
            setTheme(R.style.GnMmsDarkTheme);
        }
        //Gionee zengxuanhui 20120903 add for CR00686714 end
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.gn_ringtone_prefs);
        String summary = null;
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
        //mProfileManager = (AudioProfileManager)getSystemService(Context.AUDIOPROFILE_SERVICE);
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
        
        ringtone1 = (MmsRingtonePreference) findPreference(KEY_SIM1_RINGTONE);
        if(ringtone1 != null){
            ringtone1.setSimId(1);
            // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
            //ringtone1.setRingtoneType(AudioProfileManager.TYPE_MMS);
            // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
            ringtone1.setShowDefault(false);
            //Gionee <guoyx> <2013-08-12> add for CR00845622 begin
            // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
            //setRingtoneSummary(ringtone1, AudioProfileManager.TYPE_MMS);
            // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
            //Gionee <guoyx> <2013-08-12> add for CR00845622 end
        }
        
        ringtone2 = (MmsRingtonePreference) findPreference(KEY_SIM2_RINGTONE);
        if(ringtone2 != null){
            ringtone2.setSimId(2);
            // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
            //ringtone2.setRingtoneType(AudioProfileManager.TYPE_MMS2);
            // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
            ringtone2.setShowDefault(false);
            //Gionee <guoyx> <2013-08-12> add for CR00845622 begin
            // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
            //setRingtoneSummary(ringtone2, AudioProfileManager.TYPE_MMS2);
            // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
            //Gionee <guoyx> <2013-08-12> add for CR00845622 end
        }
        //gionee gaoj added for CR00725602 20121201 start
        if (MmsApp.mGnMessageSupport) {
            // Aurora liugj 2013-09-13 modified for aurora's new feature start
            getActionBar().setDisplayHomeAsUpEnabled(true);
            // Aurora liugj 2013-09-13 modified for aurora's new feature end
        }
        //gionee gaoj added for CR00725602 20121201 end
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

    //Gionee zengxuanhui 20120814 add for CR00673472 begin
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        //sendMmsRingtoneReceiver();
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        //sendMmsRingtoneReceiver();
        //Gionee <guoyx> <2013-08-12> add for CR00845622 begin
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
        //setRingtoneSummary(ringtone1, AudioProfileManager.TYPE_MMS);
        //(ringtone2, AudioProfileManager.TYPE_MMS2);
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
        //Gionee <guoyx> <2013-08-12> add for CR00845622 end
        super.onResume();
    }

    private void sendMmsRingtoneReceiver() {
        SharedPreferences sp = AuroraPreferenceManager.getDefaultSharedPreferences(this);
        String content = sp.getString("pref_key_ringtone", null);
        if ("silence".equals(content) || "".equals(content)) {
            content = null;
            sp.edit().putString(MessagingPreferenceActivity.NOTIFICATION_RINGTONE, 
                    "silence").commit();
        }
        String content2 = sp.getString(MessagingPreferenceActivity.NOTIFICATION_RINGTONE2, null);
        if("silence".equals(content2) || "".equals(content2)){
            content2 = null;
            sp.edit().putString(MessagingPreferenceActivity.NOTIFICATION_RINGTONE2, 
                    "silence").commit();
        }

        boolean enable = sp.getBoolean(MessagingPreferenceActivity.NOTIFICATION_ENABLED, true);
        Intent intent = new Intent("com.android.settings.gnsetmmsringtone");
        intent.putExtra("MMS_RINGTONE", content);
        intent.putExtra("MMS_RINGTONE_ENABLE", enable);
        intent.putExtra("MMS_RINGTONE2", content2);
        sendBroadcast(intent);
    }
    
    //Gionee zengxuanhui 20120814 add for CR00673472 end
    
    private String getRingtoneSummary(int ringtoneType){
        
        SharedPreferences sp = AuroraPreferenceManager.getDefaultSharedPreferences(this);
        Uri ringtoneUri = null;
        //Gionee <guoyx> <2013-06-21> modify for CR00825766 begin
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
        /*if (mProfileManager == null) {
            mProfileManager = (AudioProfileManager)getSystemService(Context.AUDIOPROFILE_SERVICE);
        }*/
        
        //String activeKey = mProfileManager.getActiveProfileKey();
        /*Scenario scenario = AudioProfileManager.getScenario(activeKey);
        if (scenario.equals(Scenario.SILENT)
                || scenario.equals(Scenario.MEETING)) {
            Log.d(TAG, "getRingtoneSummary: get the GENERAL audio profile ring tone.");
            String proGeneral = mProfileManager.getProfileKey(Scenario.GENERAL);
            ringtoneUri = mProfileManager.getRingtoneUri(
                    proGeneral, ringtoneType);
        } else {
            ringtoneUri = mProfileManager.getRingtoneUri(
                    mProfileManager.getActiveProfileKey(), ringtoneType);
        }*/
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
        //Gionee <guoyx> <2013-06-21> modify for CR00825766 end

        String ringtoneStr = null;
        if(ringtoneUri != null){
            ringtoneStr = ringtoneUri.toString();
        }
    
        if ("silence".equals(ringtoneStr)) {
            ringtoneStr = null;
        }
        
        if(ringtoneUri != null){
            //Gionee <lwzh> <2013-04-29> add for CR00803696 begin
            if (MmsApp.mQcMultiSimEnabled) {
                if (MessageUtils.isRingtoneExist(this, ringtoneUri)) {
                    return MessageUtils.gnGetRingtoneTile(this, ringtoneUri);
                } else {
                    return MessageUtils.gnGetRingtoneTile(this, RingtoneManager.getDefaultUri(ringtoneType));
                }
            }
            //Gionee <lwzh> <2013-04-29> add for CR00803696 end
            
            Ringtone r;
            if(MessageUtils.isRingtoneExist(this, ringtoneUri)){
                r = RingtoneManager.getRingtone(this, ringtoneUri);
            }else{
                r = null;
            }
            if (r == null) {
                r = RingtoneManager.getRingtone(this,RingtoneManager.getDefaultUri(ringtoneType));
            }
            if (r != null) {
                return r.getTitle(this);
            }
        }else{
            return null;
        }
        return null;
    }
    
    //Gionee <guoyx> <2013-08-12> add for CR00845622 begin
    private void setRingtoneSummary(MmsRingtonePreference ringtone, int audioType) {
        if (ringtone == null) {
            Log.d(TAG, "setRingtoneSummary ringtone = null return !");
            return;
        }
        String summary;
        summary = getRingtoneSummary(audioType);
        if(summary != null){
            ringtone.setSummary(summary);
            Log.d(TAG, "setRingtoneSummary set the summary = " + summary);
        }else{
            ringtone.setSummary(R.string.notification_summary_silent);
        }
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.d(TAG, "onWindowFocusChanged focus state:" + hasFocus);
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
           // setRingtoneSummary(ringtone1, AudioProfileManager.TYPE_MMS);
           // setRingtoneSummary(ringtone2, AudioProfileManager.TYPE_MMS2);
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
        }
    }
    //Gionee <guoyx> <2013-08-12> add for CR00845622 end
}
