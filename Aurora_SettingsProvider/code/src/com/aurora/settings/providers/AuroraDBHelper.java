
package com.aurora.settings.providers;

import aurora.provider.AuroraSettings;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.SystemProperties;
import android.util.Log;
import android.provider.Settings;


public class AuroraDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "AuroraSettingsProvider";
    private static final String DATABASE_NAME = "aurorasettings.db";

    private static final int DATABASE_VERSION = 1;

    private Context mContext;

    public AuroraDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e(TAG, "aurorasettings database created");
        
        db.execSQL("CREATE TABLE config (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT UNIQUE ON CONFLICT REPLACE," +
                    "value TEXT" +
                    ");");
        db.execSQL("CREATE INDEX systemIndex1 ON config (name);");

        // Load inital settings values
        loadConfigSettings(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
        Log.w(TAG, "Upgrading gnsettings database from version " + oldVersion + " to "
                + currentVersion);

        int upgradeVersion = oldVersion;

        if (upgradeVersion == 1) {
            // to do ..
            upgradeVersion = 2;
        }

        if (upgradeVersion != currentVersion) {
            // to do ..
        }
    }

    private void loadConfigSettings(SQLiteDatabase db) {
        SQLiteStatement stmt = null;
        try {
            stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                    + " VALUES(?,?);");
            initConfigValues(stmt);
        } finally {
            if (stmt != null) stmt.close();
        }
    }

    private void loadSetting(SQLiteStatement stmt, String key, Object value) {
        stmt.bindString(1, key);
        stmt.bindString(2, value.toString());
        stmt.execute();
    }

    private void loadStringSetting(SQLiteStatement stmt, String key, int resid) {
        loadSetting(stmt, key, mContext.getResources().getString(resid));
    }

    private void loadBooleanSetting(SQLiteStatement stmt, String key, int resid) {
        loadSetting(stmt, key,
                mContext.getResources().getBoolean(resid) ? "1" : "0");
    }

    private void loadIntegerSetting(SQLiteStatement stmt, String key, int resid) {
        loadSetting(stmt, key,
                Integer.toString(mContext.getResources().getInteger(resid)));
    }

    private void loadFractionSetting(SQLiteStatement stmt, String key, int resid, int base) {
        loadSetting(stmt, key,
                Float.toString(mContext.getResources().getFraction(resid, base, base)));
    }
    
    private void initConfigValues(SQLiteStatement stmt) {
        //Gionee fengjianyi 2012-08-22 modify for CR00673800 start
        if (SystemProperties.get("ro.gn.flip.support").equals("yes")) {
            loadSetting(stmt, AuroraSettings.FLIP_SOUNDS_ENABLED, 1);
            
            loadStringSetting(stmt, AuroraSettings.FLIP_ON_SOUND,
                    R.string.zzzzz_gn_def_flip_on_sound);
            loadStringSetting(stmt, AuroraSettings.FLIP_OFF_SOUND,
                    R.string.zzzzz_gn_def_flip_off_sound);
        }
        //Gionee fengjianyi 2012-08-22 modify for CR00673800 end
		
		//default input method        
		Settings.Secure.putString(mContext.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD,
				"com.sohu.inputmethod.sogouoem/.SogouIME");

        //Gionee:zhang_xin 2012-12-09 add for start
        loadBooleanSetting(stmt, AuroraSettings.POWER_SAVER, 
                R.bool.def_gn_power_saver);
        //Gionee:zhang_xin 2012-12-09 add for end
        
        //Gionee <zhang_xin><2013-03-26> add for CR00788411 begin
        loadBooleanSetting(stmt, AuroraSettings.FONT_SIZE, 
                R.integer.def_gn_font_size);
        //Gionee <zhang_xin><2013-03-26> add for CR00788411 end
        
        //gionee zengxuanhui 20121022 add for CR00716758 begin
        loadBooleanSetting(stmt, AuroraSettings.HAPTIC_VIBRATION_ENABLED,
                R.bool.def_haptic_vibration);
        //gionee zengxuanhui 20121022 add for CR00716758 end
        
        //Gionee Jingjc 20121122 modify for CR00722601 begin
        loadBooleanSetting(stmt, AuroraSettings.SWITCH_VIBRATION_ENABLED,
                R.bool.def_switch_vibration);
        loadBooleanSetting(stmt, AuroraSettings.DIALPAD_VIBRATION_ENABLED,
                R.bool.def_dialpad_vibration);
        loadBooleanSetting(stmt, AuroraSettings.LOCKSCREEN_VIBRATION_ENABLED,
                R.bool.def_lockscreen_vibration);
        loadBooleanSetting(stmt, AuroraSettings.SELECTAPP_VIBRATION_ENABLED,
                R.bool.def_selectapp_vibration);
        //Gionee Jingjc 20121122 modify for CR00722601 end
        
        //Gionee <zengxuanhui> <2013-04-26> add for CR00797390 begin
        loadBooleanSetting(stmt, AuroraSettings.RING_VIBRATION_ENABLED,
                R.bool.def_ring_vibration);
        loadBooleanSetting(stmt, AuroraSettings.MMS_VIBRATION_ENABLED,
                R.bool.def_mms_vibration);
        loadBooleanSetting(stmt, AuroraSettings.NOTIFICATION_VIBRATION_ENABLED,
                R.bool.def_notification_vibration);
        //Gionee <zengxuanhui> <2013-04-26> add for CR00797390 end
        
        // Gionee <wangyaohui><2013-05-30> add for CR00820909 begin
        if (SystemProperties.get("ro.gn.fanfanwidget.support").equals("yes")) {
            loadBooleanSetting(stmt, AuroraSettings.FANFAN_WIDGET_AUTO_PUSH,
                    R.bool.def_fanfan_widget);
        }
        // Gionee <wangyaohui><2013-05-30> add for CR00820909 end
        
        // Gionee <wangyaohui><2013-06-05> add for CR00823496 begin
        if (SystemProperties.get("ro.gn.respirationlamp.support").equals("yes")) {
            loadBooleanSetting(stmt, AuroraSettings.RESPIRATION_LAMP_LOW_POWER,
                    R.bool.def_respirationlamp_low_power);
            loadBooleanSetting(stmt, AuroraSettings.RESPIRATION_LAMP_IN_CHARGE,
                    R.bool.def_respirationlamp_in_charge);			
            loadBooleanSetting(stmt, AuroraSettings.RESPIRATION_LAMP_NOTIFICATION,
                    R.bool.def_respirationlamp_notification);
            loadBooleanSetting(stmt, AuroraSettings.RESPIRATION_LAMP_MUSIC,
                    R.bool.def_respirationlamp_music);
            loadBooleanSetting(stmt, AuroraSettings.RESPIRATION_LAMP_CALL,
                    R.bool.def_respirationlamp_call);
        }
        // Gionee <wangyaohui><2013-06-05> add for CR00823496 end

        // Gionee <wangguojing> <2013-08-19> add for CR00859362 begin
        if (SystemProperties.get("ro.gn.gesture.support").equals("yes")) {
            loadBooleanSetting(stmt, "ssg_switch", //AuroraSettings.GN_SSG_SWITCH,
                    R.bool.def_ssg_switch);
            loadBooleanSetting(stmt, "dg_switch", //AuroraSettings.GN_DG_SWITCH,
                    R.bool.def_dg_switch);
            loadBooleanSetting(stmt, AuroraSettings.SSG_AUTO_DIAL,
                    R.bool.def_ssg_auto_dial);
            loadBooleanSetting(stmt, AuroraSettings.SSG_CALL_ACCESS,
                    R.bool.def_ssg_call_access);
            loadBooleanSetting(stmt, AuroraSettings.SSG_DELAY_ALARM,
                    R.bool.def_ssg_delay_alarm);
            loadBooleanSetting(stmt, AuroraSettings.SSG_SWITCH_SCREEN,
                    R.bool.def_ssg_switch_screen);
            loadBooleanSetting(stmt, AuroraSettings.SDG_CALL_ACCESS,
                    R.bool.def_sdg_call_access);
            loadBooleanSetting(stmt, AuroraSettings.SDG_BROWSE_PHOTOS,
                    R.bool.def_sdg_browse_photos);
            loadBooleanSetting(stmt, AuroraSettings.SDG_VIDEO_PROGRESS,
                    R.bool.def_sdg_video_progress);
            loadBooleanSetting(stmt, AuroraSettings.SDG_VIDEO_VOLUME,
                    R.bool.def_sdg_video_volume);
            loadBooleanSetting(stmt, AuroraSettings.SDG_VIDEO_PAUSE,
                    R.bool.def_sdg_video_pause);
        }
        if (SystemProperties.get("ro.gn.soundctrl.support").equals("yes")) {
            loadBooleanSetting(stmt, AuroraSettings.SOUND_CONTROL_SWITCH,
                    R.bool.def_sound_control_switch);
            loadBooleanSetting(stmt, AuroraSettings.SOUND_CONTROL_CALLING,
                    R.bool.def_sound_control_calling);
            loadBooleanSetting(stmt, AuroraSettings.SOUND_CONTROL_MESSAGE,
                    R.bool.def_sound_control_message);
            loadBooleanSetting(stmt, AuroraSettings.SOUND_CONTROL_LOCKSCREEN,
                    R.bool.def_sound_control_lockscreen);
            loadBooleanSetting(stmt, AuroraSettings.SOUND_CONTROL_ALARMCLOCK,
                    R.bool.def_sound_control_alarmclock);
        }
	  // Gionee <wangguojing> <2013-08-19> add for CR00859362 end
    }
}
