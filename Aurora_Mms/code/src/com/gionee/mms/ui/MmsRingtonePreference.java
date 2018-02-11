/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gionee.mms.ui;

import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.MessagingPreferenceActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import aurora.preference.AuroraPreferenceManager;
import aurora.preference.AuroraRingtonePreference;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import com.android.mms.MmsApp;
import com.android.mms.R;
// Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
//import com.mediatek.audioprofile.AudioProfileManager;
//import com.mediatek.audioprofile.AudioProfileManager.Scenario;
// Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
/**
 * The AuroraRingtonePreference does not have a way to get/set the current ringtone so
 * we override onSaveRingtone and onRestoreRingtone to get the same behavior.
 */
public class MmsRingtonePreference extends AuroraRingtonePreference {
    private static final String TAG = "MmsRingtonePreference";
    
    private Uri mAlert;
    
    public static final int SIM1_ID = 1;
    public static final int SIM2_ID = 2;
    
    private int mSimId = 1;
    private AsyncTask mRingtoneTask;
    // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
    //private AudioProfileManager mProfileManager = null;
    // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
    
    public MmsRingtonePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Aurora xuyong 2013-11-15 modified for S4 adapt start
        setRingtoneType(32/*RingtoneManager.TYPE_MMS*/);
        // Aurora xuyong 2013-11-15 modified for S4 adapt end
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
        //mProfileManager = (AudioProfileManager)context.getSystemService(Context.AUDIOPROFILE_SERVICE);
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
    }
    
    
    
    @Override
    protected void onPrepareRingtonePickerIntent(Intent ringtonePickerIntent) {
        // TODO Auto-generated method stub
        super.onPrepareRingtonePickerIntent(ringtonePickerIntent);
        
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
    }



    @Override
    protected void onSaveRingtone(Uri ringtoneUri) {
        //Gionee zengxuanhui 20120806 modify for CR00664477 begin
        Log.d(TAG, "onSaveRingtone: ringtoneUri="+ringtoneUri);
        setAlert(ringtoneUri);
            // Update the default alert in the system.
        //Gionee <guoyx> <2013-06-21> add for CR00825766 begin
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
        /*if (mProfileManager == null) {
            mProfileManager = (AudioProfileManager)getContext().getSystemService(Context.AUDIOPROFILE_SERVICE);
        }*/
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
        //Gionee <guoyx> <2013-06-21> add for CR00825766 end
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
        /*mProfileManager.setRingtoneUri(mProfileManager.getActiveProfileKey(), 
                    getRingtoneType(), ringtoneUri);*/
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
        setRingtoneSummary(ringtoneUri);
        //Gionee zengxuanhui 20120806 modify for CR00664477 end
        //Gionee zengxuanhui 20121220 modify for CR00746745 begin
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
        //String activeKey = mProfileManager.getActiveProfileKey();
        /*Scenario scenario = AudioProfileManager.getScenario(activeKey);
        if(scenario.equals(Scenario.SILENT) 
                || scenario.equals(Scenario.MEETING)){
            Log.d(TAG, "onSaveRingtone: no general or customer,return!");
            //Gionee <guoyx> <2013-06-21> modify for CR00825766 begin
            String proGeneral = mProfileManager.getProfileKey(Scenario.GENERAL);
            mProfileManager.setRingtoneUri(proGeneral, getRingtoneType(), ringtoneUri);
            //Gionee <guoyx> <2013-06-21> modify for CR00825766 end
            return;
        }*/
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
        SharedPreferences sp = AuroraPreferenceManager.getDefaultSharedPreferences(getContext());            
        if(ringtoneUri != null){
            if(mSimId == SIM2_ID){
                sp.edit().putString(MessagingPreferenceActivity.NOTIFICATION_RINGTONE2,
                        ringtoneUri.toString()).apply();
            }else {
                sp.edit().putString(MessagingPreferenceActivity.NOTIFICATION_RINGTONE,
                        ringtoneUri.toString()).apply();
            }

        }
        else{
            if(mSimId == SIM2_ID){
                sp.edit().putString(MessagingPreferenceActivity.NOTIFICATION_RINGTONE2,
                        "silence").apply();
            }else{
                sp.edit().putString(MessagingPreferenceActivity.NOTIFICATION_RINGTONE,
                        "silence").apply();
            }

        }
        //Gionee zengxuanhui 20121220 modify for CR00746745 end
    }

    @Override
    protected Uri onRestoreRingtone() {
        Log.d(TAG, "onRestoreRingtone");
        SharedPreferences sp = AuroraPreferenceManager.getDefaultSharedPreferences(getContext());
        //Gionee <guoyx> <2013-06-21> add for CR00825766 begin
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
        /*if (mProfileManager == null) {
            mProfileManager = (AudioProfileManager)getContext().getSystemService(Context.AUDIOPROFILE_SERVICE);
        }*/
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
        //Gionee <guoyx> <2013-06-21> add for CR00825766 end
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
        /*Uri ringtoneStr = mProfileManager.getRingtoneUri(mProfileManager.getActiveProfileKey(), 
                getRingtoneType());*/
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
        //Gionee zengxuanhui 20120806 modify for CR00664477 begin
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
        /*if (ringtoneStr == null) {
            Log.d(TAG, "ringtoneStr is null , reset default.");
            return null;
        }*/
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
        //Gionee zengxuanhui 20120806 modify for CR00664477 end
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
        //mAlert = ringtoneStr;
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
        //Gionee <lwzh> <2013-04-29> add for CR00803696 begin
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
        /*boolean isRingtoneExist = false;
        if (MmsApp.mQcMultiSimEnabled) {
            isRingtoneExist = MessageUtils.isRingtoneExist(getContext(), ringtoneStr);
        } else {
            isRingtoneExist = RingtoneManager.getRingtone(getContext(), mAlert) == null ? false : true;
        }*/
        
        /*if (!isRingtoneExist) {
        //Gionee <lwzh> <2013-04-29> add for CR00803696 end
            /*if (mSimId == SIM2_ID) {
                mAlert = Uri.parse(Settings.System.getString(getContext()
                        .getContentResolver(),
                        AudioProfileManager.KEY_DEFAULT_MMS2));
            } else {
                mAlert = Uri.parse(Settings.System.getString(getContext()
                        .getContentResolver(),
                        AudioProfileManager.KEY_DEFAULT_MMS));
            }*/
            Log.d(TAG, "alert is no easit, change to default:" + mAlert);
        //}
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
        Log.d(TAG, "alert:" + mAlert);
        return mAlert;
        
    }

    public void setAlert(Uri alert) {
        mAlert = alert;
    }

    public void setSimId(int curId){
        mSimId = curId;
    }
    
    //Gionee zengxuanhui 20120814 add for CR00673440 begin
    private void setRingtoneSummary(Uri ringtoneUri){
        //Gionee <guoyx> <2013-06-24> modify for CR00829143 begin
        if (ringtoneUri == null) {
            setSummary(R.string.notification_summary_silent);
            return ;
        }
        //Gionee <guoyx> <2013-06-24> modify for CR00829143 end
        
        setSummary(R.string.loading_ringtone);
        if (mRingtoneTask != null) {
            mRingtoneTask.cancel(true);
        }
        mRingtoneTask = new AsyncTask<Uri, Void, String>() {
            @Override
            protected String doInBackground(Uri... params) {
                // Gionee <lwzh> <2013-04-29> add for CR00803696 begin
                int type = getRingtoneType();
                if (MmsApp.mQcMultiSimEnabled) {
                    if (MessageUtils.isRingtoneExist(getContext(), params[0])) {
                        return MessageUtils.gnGetRingtoneTile(getContext(),
                                params[0]);
                    } else {
                        return MessageUtils.gnGetRingtoneTile(getContext(),
                                RingtoneManager.getDefaultUri(type));
                    }
                }
                // Gionee <lwzh> <2013-04-29> add for CR00803696 end

                Ringtone r = RingtoneManager.getRingtone(getContext(),
                        params[0]);

                if (r == null) {
                    r = RingtoneManager.getRingtone(getContext(),
                            RingtoneManager.getDefaultUri(type));
                }
                if (r != null) {
                    return r.getTitle(getContext());
                }
                return null;
            }

            @Override
            protected void onPostExecute(String title) {
                if (!isCancelled()) {
                    setSummary(title);
                    mRingtoneTask = null;
                }
            }
        }.execute(ringtoneUri);
    }
    //Gionee zengxuanhui 20120814 add for CR00673440 end
}
