/* Gionee fangbin 20120629 added for CR00622030 */
package com.android.mms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import aurora.preference.AuroraPreferenceManager;
import android.provider.Settings;
import android.net.Uri;
import android.os.SystemProperties;
import com.android.mms.ui.MessagingPreferenceActivity;
// Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
//import com.mediatek.audioprofile.AudioProfileManager;
// Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end

public class GnMmsRingtoneReceiver extends BroadcastReceiver {
    
    //Gionee zengxuanhui 20120809 add for CR00672106/CR00718095 begin
    private static final boolean gnGeminiRingtoneSupport = SystemProperties.get("ro.gn.gemini.ringtone.support").equals("yes");
    // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
    //private AudioProfileManager mProfileManager;
    // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
    //Gionee zengxuanhui 20120809 add for CR00672106/CR00718095 end
    
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        //Gionee zengxuanhui 20121029 modify for CR00718095 begin
        String action = intent.getAction();
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
        //mProfileManager = (AudioProfileManager) context.getSystemService(Context.AUDIOPROFILE_SERVICE);
        
        /*if(Intent.ACTION_BOOT_COMPLETED.equals(action) && gnGeminiRingtoneSupport){
            Uri setValue;
            String setStr;
            if(mProfileManager == null){
                return;
            }
            if(mProfileManager.getDefaultRingtone(AudioProfileManager.TYPE_MMS) == null){
                setValue = mProfileManager.getDefaultRingtone(AudioProfileManager.TYPE_NOTIFICATION);
                setStr = (setValue == null ? null : setValue.toString());
                Settings.System.putString(context.getContentResolver(), AudioProfileManager.KEY_DEFAULT_MMS,
                        setStr);
                Settings.System.putString(context.getContentResolver(), Settings.System.MMS,
                        setStr);
            }
            if(mProfileManager.getDefaultRingtone(AudioProfileManager.TYPE_MMS2) == null){
                setValue = mProfileManager.getDefaultRingtone(AudioProfileManager.TYPE_NOTIFICATION);
                setStr = (setValue == null ? null : setValue.toString());
                Settings.System.putString(context.getContentResolver(), AudioProfileManager.KEY_DEFAULT_MMS,
                        setStr);
                Settings.System.putString(context.getContentResolver(), Settings.System.MMS2,
                        setStr);
            }
        }else{*/
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
            SharedPreferences sp = AuroraPreferenceManager.getDefaultSharedPreferences(context);
            //Gionee zengxuanhui 20120814 modify for CR00672816 begin
            String ringtone = intent.getStringExtra("MMS_RINGTONE");
            if(ringtone == null){
                ringtone = "silence";
            }
            sp.edit().putString(MessagingPreferenceActivity.NOTIFICATION_RINGTONE, ringtone).commit();
            if(gnGeminiRingtoneSupport){
                String ringtone2 = intent.getStringExtra("MMS_RINGTONE2");
                if(ringtone2 == null){
                    ringtone2 = "silence";
                }
                sp.edit().putString(MessagingPreferenceActivity.NOTIFICATION_RINGTONE2, ringtone2).commit();
            }
            //Gionee zengxuanhui 20120814 modify for CR00672816 end
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
        //}
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
        //Gionee zengxuanhui 20121029 modify for CR00718095 end
    }
}
