
package com.amigo.settings.providers;

import amigo.provider.AmigoSettings;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.os.SystemProperties;
import android.util.Log;
//Gionee <wangguojing> <2013-09-04> add for CR00873246 begin
import android.provider.Settings;
import android.content.ContentResolver;
import android.content.Intent;
//Gionee <wangguojing> <2013-09-04> add for CR00873246 end

// Gionee <bug> <wangyaohui> <2013-10-24> modify for CR00933322 begin
import android.database.Cursor;
// Gionee <bug> <wangyaohui> <2013-10-24> modify for CR00933322 end

//Gionee <wangguojing> <2014-05-12> add for CR01237681 begin
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.util.Xml;

import com.android.internal.util.XmlUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
//Gionee <wangguojing> <2014-05-12> add for CR01237681 end

public class AmigoDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "AmigoSettingsProvider";
    private static final String DATABASE_NAME = "amigosettings.db";
    // Gionee <bug> <wangyaohui> <2013-10-07> modify for CR00911478 begin
    // private static final int DATABASE_VERSION = 1;

    // Gionee <bug> <wangyaohui> <2013-10-24> modify for CR00933322 begin
    private static final int DATABASE_VERSION = 42;
    // private static final String SSG_QUICK_OPERATING = AmigoSettings.SSG_QUICK_OPERATING;
    public  static final String SSG_QUICK_OPERATING = "ssg_quick_operating";
    public  static final String SSG_QUICK_OPERATING_UP_GESTURE_CHOICE = "ssg_quick_operating_up_choice"; 
    public  static final String SSG_QUICK_OPERATING_UP_GESTURE_APP_NAME_OR_PHONE_NUMBER = "ssg_quick_operating_up_app_or_phone_number"; 
    public  static final String SSG_QUICK_OPERATING_RIGHT_GESTURE_CHOICE = "ssg_quick_operating_right_choice"; 
    public  static final String SSG_QUICK_OPERATING_RIGHT_GESTURE_APP_NAME_OR_PHONE_NUMBER = "ssg_quick_operating_right_app_or_phone_number"; 
    public  static final String SSG_QUICK_OPERATING_LEFT_GESTURE_CHOICE = "ssg_quick_operating_left_choice"; 
    public  static final String SSG_QUICK_OPERATING_LEFT_GESTURE_APP_NAME_OR_PHONE_NUMBER = "ssg_quick_operating_left_app_or_phone_number"; 
    public  static final String SSG_QUICK_OPERATING_DOWN_GESTURE_CHOICE = "ssg_quick_operating_down_choice"; 
    public  static final String SSG_QUICK_OPERATING_DOWN_GESTURE_APP_NAME_OR_PHONE_NUMBER = "ssg_quick_operating_down_app_or_phone_number"; 
    // Gionee <bug> <wangyaohui> <2013-10-24> modify for CR00933322 end 
    //Gionee <huangyuncai> <2014-03-04> add for CR01087530 begin
    public static final String NOTIFICATION_DISABLE_REMINDER = "notification_disable_reminder";
    public static final String NETWORK_SPEED_DISPLAY = "network_speed_display";
    //Gionee <huangyuncai> <2014-03-04> add for CR01087530 end
    // Gionee <bug> <wangyaohui> <2013-10-07> modify for CR00911478 end
    private Context mContext;
    //Gionee <wangguojing> <2014-05-12> add for CR01237681 begin
    private static final String AMIGOSETTING_DB_CONFIG_FILE    = "/system/etc/AmigoSettingConfig.xml";     
    //Gionee <wangguojing> <2014-05-12> add for CR01237681 end

    public AmigoDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e(TAG, "amigosettings database created");
        
        db.execSQL("CREATE TABLE config (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT UNIQUE ON CONFLICT REPLACE," +
                    "value TEXT" +
                    ");");
        db.execSQL("CREATE INDEX systemIndex1 ON config (name);");

        // Load inital settings values
        loadConfigSettings(db);
        //Gionee <wangguojing> <2013-10-11> add for CR00932764 begin
        copySettingsDB(db);
        //Gionee <wangguojing> <2013-10-11> add for CR00932764 end
        //Gionee <wangguojing> <2014-05-12> add for CR01237681 begin
        updateDBValue(db);
        //Gionee <wangguojing> <2014-05-12> add for CR01237681 end
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
        Log.e(TAG, "Upgrading gnsettings database from version " + oldVersion + " to "
                + currentVersion);

        int upgradeVersion = oldVersion;

        if (upgradeVersion == 1) {
			// to do ..
            // Gionee <bug> <wangyaohui> <2013-10-07> add for CR00911478 begin
           	db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadBooleanSetting(stmt, AmigoSettings.SOUND_CONTROL_SWITCH,
                    R.bool.def_sound_control_switch);
				loadBooleanSetting(stmt, AmigoSettings.SOUND_CONTROL_CALLING,
                    R.bool.def_sound_control_calling);
				loadBooleanSetting(stmt, AmigoSettings.SOUND_CONTROL_MESSAGE,
                    R.bool.def_sound_control_message);
				loadBooleanSetting(stmt, AmigoSettings.SOUND_CONTROL_LOCKSCREEN,
                    R.bool.def_sound_control_lockscreen);
				loadBooleanSetting(stmt, AmigoSettings.SOUND_CONTROL_ALARMCLOCK,
                    R.bool.def_sound_control_alarmclock);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            // Gionee <bug> <wangyaohui> <2013-10-07> add for CR00911478 end
            upgradeVersion = 2;
        }

        // Gionee <bug> <wangyaohui> <2013-10-24> modify for CR00933322 begin
         if (upgradeVersion == 2) {
           	db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                //if (SystemProperties.get("ro.gn.gesture.support").equals("yes")) {
                    loadBooleanSetting(stmt,SSG_QUICK_OPERATING,R.bool.def_fast_operation);
                    loadSetting(stmt,SSG_QUICK_OPERATING_UP_GESTURE_CHOICE,"1");
                    loadSetting(stmt,SSG_QUICK_OPERATING_UP_GESTURE_APP_NAME_OR_PHONE_NUMBER,
                                              "com.android.camera/com.android.camera.CameraLauncher");
                    loadSetting(stmt,SSG_QUICK_OPERATING_RIGHT_GESTURE_CHOICE,"1");
                    loadSetting(stmt,SSG_QUICK_OPERATING_RIGHT_GESTURE_APP_NAME_OR_PHONE_NUMBER,
                                              "com.tencent.mm/com.tencent.mm.ui.LauncherUI");				
                    loadSetting(stmt,SSG_QUICK_OPERATING_LEFT_GESTURE_CHOICE,0);
                    loadSetting(stmt,SSG_QUICK_OPERATING_LEFT_GESTURE_APP_NAME_OR_PHONE_NUMBER,"");
                    loadSetting(stmt,SSG_QUICK_OPERATING_DOWN_GESTURE_CHOICE,0);
                    loadSetting(stmt,SSG_QUICK_OPERATING_DOWN_GESTURE_APP_NAME_OR_PHONE_NUMBER,"");
                    
                    
                    //Gionee <wangguojing> <2013-10-31> add for CR00922210 begin
                    loadBooleanSetting(stmt, AmigoSettings.SDG_DEL_PHOTOS,
                        R.bool.def_sdg_del_photos);
                    loadBooleanSetting(stmt, AmigoSettings.SDG_DEL_PHOTOS_SHOW,
                        R.bool.def_sdg_del_photos);
                    
                    loadBooleanSetting(stmt, AmigoSettings.SDG_TASK_CONTROL,
                        R.bool.def_sdg_task_control);
                    loadBooleanSetting(stmt, AmigoSettings.SDG_TASK_CONTROL_SHOW,
                        R.bool.def_sdg_task_control);
                    
                    loadBooleanSetting(stmt, AmigoSettings.SSG_DOUBLECLICK_WAKE,
                        R.bool.def_ssg_doubleclick_wake);
                    loadBooleanSetting(stmt, AmigoSettings.SSG_DOUBLECLICK_WAKE_SHOW,
                        R.bool.def_ssg_doubleclick_wake_show);
                    //Gionee <wangguojing> <2013-10-31> add for CR00922210 end
                    
                //}
                //Gionee <wangguojing> <2013-10-11> add for GPS Optimization begin
                loadBooleanSetting(stmt, AmigoSettings.GPS_PROMPT_REMIND,R.bool.def_gps_prompt_remind);
                //Gionee <wangguojing> <2013-10-11> add for GPS Optimization end
                
                //Gionee <wangguojing> <2013-10-24> add for CR00933390 begin
                //if (SystemProperties.get("ro.gn.floatingwindow.support").equals("yes")) {
                    loadStringSetting(stmt, AmigoSettings.VIRTUAL_DISPLAY_DEVICES,R.string.def_virtual_display_devices);
                //}
                //Gionee <wangguojing> <2013-10-24> add for CR00933390 end
                //Gionee <wangguojing> <2013-10-31> add for CR00942710 begin
                //if (SystemProperties.get("ro.gn.glove_patterns.support").equals("yes")) {
                    loadBooleanSetting(stmt, AmigoSettings.GLOVE_PATTERNS,R.bool.def_glove_patterns);
                //}
                //Gionee <wangguojing> <2013-10-31> add for CR00942710 end
				
                //Gionee <wangguojing> <2013-11-02> add for CR00943428 begin
                //if (SystemProperties.get("ro.gn.voicewake.support").equals("yes")) {
                    loadBooleanSetting(stmt, AmigoSettings.VOICE_WAKE_SWITCH,R.bool.def_voice_wake_switch);
                    loadBooleanSetting(stmt, AmigoSettings.VOICE_WAKE_REMIND,R.bool.def_voice_wake_remind);
                    loadBooleanSetting(stmt, "voice_wake_word"/*AmigoSettings.VOICE_WAKE_WORD*/,R.bool.def_voice_wake_word);
                //}
                //Gionee <wangguojing> <2013-11-02> add for CR00943428 end
                
                //Gionee <wangguojing> <2013-11-04> add for CR00942651 begin
                //if (SystemProperties.get("ro.gn.lcd.effect.support").equals("yes")) {
                    loadBooleanSetting(stmt, AmigoSettings.LCD_EFFECT_MODE,R.bool.def_lcd_effect_adjust);
                //}
                //Gionee <wangguojing> <2013-11-02> add for CR00942651 end
                
                //Gionee <wangguojing> <2013-11-04> add for CR00944538 begin
                loadBooleanSetting(stmt, "show_powersave_dialog"/*AmigoSettings.SHOW_POWERSAVE_DIALOG*/,R.bool.def_show_powersave_dialog);
                //Gionee <wangguojing> <2013-11-04> add for CR00944538 end

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            
            upgradeVersion = 3;
        }
        // Gionee <bug> <wangyaohui> <2013-10-24> modify for CR00933322 end 
        
        //Gionee <wangguojing> <2013-12-05> add for CR00966365 begin
        if (upgradeVersion == 3) {
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                //if (SystemProperties.get("ro.gn.flip.support").equals("yes")) {
              	  loadSetting(stmt, AmigoSettings.FLIP_SOUNDS_ENABLED, 1);
              	  
              	  loadStringSetting(stmt, AmigoSettings.FLIP_ON_SOUND,
              			  R.string.zzzzz_gn_def_flip_on_sound);
              	  loadStringSetting(stmt, AmigoSettings.FLIP_OFF_SOUND,
              			  R.string.zzzzz_gn_def_flip_off_sound);
                //}
              
                loadBooleanSetting(stmt, AmigoSettings.FONT_SIZE, 
              		  R.integer.def_gn_font_size);
              
                loadBooleanSetting(stmt, AmigoSettings.HAPTIC_VIBRATION_ENABLED,
              		  R.bool.def_haptic_vibration);
              
                loadBooleanSetting(stmt, AmigoSettings.SWITCH_VIBRATION_ENABLED,
              		  R.bool.def_switch_vibration);
                loadBooleanSetting(stmt, AmigoSettings.DIALPAD_VIBRATION_ENABLED,
              		  R.bool.def_dialpad_vibration);
                loadBooleanSetting(stmt, AmigoSettings.LOCKSCREEN_VIBRATION_ENABLED,
              		  R.bool.def_lockscreen_vibration);
                loadBooleanSetting(stmt, AmigoSettings.SELECTAPP_VIBRATION_ENABLED,
              		  R.bool.def_selectapp_vibration);
              
                loadBooleanSetting(stmt, AmigoSettings.RING_VIBRATION_ENABLED,
              		  R.bool.def_ring_vibration);
                loadBooleanSetting(stmt, AmigoSettings.MMS_VIBRATION_ENABLED,
              		  R.bool.def_mms_vibration);
                loadBooleanSetting(stmt, AmigoSettings.NOTIFICATION_VIBRATION_ENABLED,
              		  R.bool.def_notification_vibration);
              
                //if (SystemProperties.get("ro.gn.fanfanwidget.support").equals("yes")) {
              	  loadBooleanSetting(stmt, AmigoSettings.FANFAN_WIDGET_AUTO_PUSH,
              			  R.bool.def_fanfan_widget);
                //}
              
                //if (SystemProperties.get("ro.gn.respirationlamp.support").equals("yes")) {
              	  loadBooleanSetting(stmt, AmigoSettings.RESPIRATION_LAMP_LOW_POWER,
              			  R.bool.def_respirationlamp_low_power);
              	  loadBooleanSetting(stmt, AmigoSettings.RESPIRATION_LAMP_IN_CHARGE,
              			  R.bool.def_respirationlamp_in_charge);		  
              	  loadBooleanSetting(stmt, AmigoSettings.RESPIRATION_LAMP_NOTIFICATION,
              			  R.bool.def_respirationlamp_notification);
              	  loadBooleanSetting(stmt, AmigoSettings.RESPIRATION_LAMP_MUSIC,
              			  R.bool.def_respirationlamp_music);
              	  loadBooleanSetting(stmt, AmigoSettings.RESPIRATION_LAMP_CALL,
              			  R.bool.def_respirationlamp_call);
                //}
              
                
                //if (SystemProperties.get("ro.gn.soundctrl.support").equals("yes")) {
              	  loadBooleanSetting(stmt, AmigoSettings.SOUND_CONTROL_OPEN_REMIND,
              			  R.bool.def_sound_control_open_remind);
              	  loadBooleanSetting(stmt, AmigoSettings.SOUND_CONTROL_CLOSE_REMIND,
              			  R.bool.def_sound_control_close_remind);
                //}
              
              //if (SystemProperties.get("ro.gn.guestmode.support").equals("yes")) {
              	loadBooleanSetting(stmt, AmigoSettings.GUEST_MODE,
              			R.bool.def_guest_mode);
              	loadBooleanSetting(stmt, AmigoSettings.FIRST_OPEN_GUEST_MODE,
              			R.bool.def_first_open_guest_mode);
              	loadBooleanSetting(stmt, AmigoSettings.GUEST_PASS_ENABLE,
              			R.bool.def_guest_pass_enable);
              	loadStringSetting(stmt, AmigoSettings.GUEST_PASS,
              			R.string.zzzzz_gn_def_guest_pass);
              //}
              
              //if (SystemProperties.get("ro.gn.flip.support").equals("yes")) {
              	loadIntegerSetting(stmt, AmigoSettings.FLIP_HANGUP_CALL_SWITCH,
              			R.integer.def_flip_hangup_call_default);
              	loadIntegerSetting(stmt, AmigoSettings.FLIP_ANSWER_CALL_SWITCH,
              			R.integer.def_flip_answer_call_default);
              //}
              loadBooleanSetting(stmt, AmigoSettings.Button_Light_State,
              		R.bool.def_button_light_state_default);
              
              //if (SystemProperties.get("ro.gn.networkalert.support", "yes").equals("yes")) {
              	loadBooleanSetting(stmt, AmigoSettings.WIFI_AUTO_NOTIFY,
              			R.bool.def_wifi_auto_notify_default);
              //}
              loadBooleanSetting(stmt, AmigoSettings.ALIGN_WAKE,
              		R.bool.def_align_wake_default);
              
              loadIntegerSetting(stmt, AmigoSettings.SCREEN_OFF_TIMEOUT_BACK_UP,
              		R.integer.def_screen_off_timeout);		
              
              //if (SystemProperties.get("ro.gn.mms.alertMissMsg").equals("yes")) {
              	loadBooleanSetting(stmt, AmigoSettings.ALERT_MISS_MSG, R.bool.gn_def_alert_miss_msg);
              	loadIntegerSetting(stmt, AmigoSettings.ALERT_MISS_MSG_INTERVAL, R.integer.gn_def_alert_miss_msg_interval);
              //}
              
              loadBooleanSetting(stmt, AmigoSettings.AUTO_LCM_ACL,R.bool.def_auto_lcm_acl);
              
              loadStringSetting(stmt, AmigoSettings.ALARM_RING,R.string.def_alarm_ring);
              
              loadStringSetting(stmt, AmigoSettings.RINGTONE2,R.string.def_ringtone2);
              loadStringSetting(stmt, AmigoSettings.MMS,R.string.def_mms);
              loadStringSetting(stmt, AmigoSettings.MMS2,R.string.def_mms2);
              
              loadStringSetting(stmt, AmigoSettings.THEME_PACKEAGE_NAME,R.string.def_theme_package_name); 
              loadIntegerSetting(stmt, AmigoSettings.THEME_CHECK_PACKEAGE,R.integer.def_theme_check_package); 	  
              
              //if (SystemProperties.get("ro.gn.suspendbutton.support").equals("yes")) {
              	loadBooleanSetting(stmt, AmigoSettings.SUSPEND_BUTTON,R.bool.def_suspend_button);
              //}
              
              //if (SystemProperties.get("ro.gn.single.hand.support").equals("yes")) {
              	loadBooleanSetting(stmt, AmigoSettings.PHONE_KEYBOARD,R.bool.def_phone_keyboard);
              	loadBooleanSetting(stmt, AmigoSettings.INPUT_METHOD_KEYBOARD,R.bool.def_input_method_keyboard);
              	loadBooleanSetting(stmt, AmigoSettings.PATTERN_UNLOCKSCREEN,R.bool.def_pattern_unlockscreen);
              	loadBooleanSetting(stmt, AmigoSettings.SMALL_SCREEN_MODE,R.bool.def_small_screen_mode);
              	loadBooleanSetting(stmt, AmigoSettings.SCREEN_SIZE,R.bool.def_screen_size);
              	loadBooleanSetting(stmt, "phone_keyboard_place"/*AmigoSettings.PHONE_KEYBOARD_PLACE*/,R.bool.def_phone_keyboard);
              //}
				
				  
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }

			
            copySettingsDB(db);

            upgradeVersion = 4;
        }
        //Gionee <wangguojing> <2013-12-05> add for CR00966365 end
        
        //Gionee <wangguojing> <2013-12-23> add for CR00989404 begin
        if (upgradeVersion == 4) {
            // to do ..
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadStringSetting(stmt, AmigoSettings.VIRTUAL_DISPLAY_DEVICES,R.string.def_virtual_display_devices);
                //Gionee <wangguojing> <2013-12-23> add for CR00989512 begin
                loadBooleanSetting(stmt, AmigoSettings.INTERNATIONAL_ROAMING_SWITCH,
                        R.bool.def_international_roaming_switch);
                //Gionee <wangguojing> <2013-12-23> add for CR00989512 end
                //Gionee <wangguojing> <2013-12-26> add for CR00992408 begin
                loadIntegerSetting(stmt, AmigoSettings.LCD_COLOR_VALUE,
                        R.integer.def_lcd_color_value);
                //Gionee <wangguojing> <2013-12-26> add for CR00992408 end
                //Gionee <wangguojing> <2013-12-27> add for CR00993259 begin
                loadBooleanSetting(stmt, AmigoSettings.ROTATE_CAMERA_OPEN_APP,
                        R.bool.def_rotate_camera_open_app);
                loadBooleanSetting(stmt, AmigoSettings.USER_EXPERIENCE,
                        R.bool.def_user_experience);
                //Gionee <wangguojing> <2013-12-27> add for CR00993259 end
                //Gionee <wangguojing> <2013-12-30> add for CR00997383 begin
                loadIntegerSetting(stmt, AmigoSettings.DIAl_SOUND_TYPE,
                        R.integer.def_dial_sound_type);
                //Gionee <wangguojing> <2013-12-30> add for CR00997383 end
                
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 5;
        }
        //Gionee <wangguojing> <2013-12-23> add for CR00989404 end

        //Gionee <wangguojing> <2014-01-17> add for CR01025430 begin
        if (upgradeVersion == 5) {
            // to do ..
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
				loadBooleanSetting(stmt, AmigoSettings.LCD_EFFECT_MODE,R.bool.def_lcd_effect_adjust);
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 6;
        }
        //Gionee <wangguojing> <2014-01-17> add for CR01025430 end

        //Gionee <wangguojing> <2014-02-11> add for CR01031420 begin
        if (upgradeVersion == 6) {
            // to do ..
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadIntegerSetting(stmt, AmigoSettings.DIAl_SOUND_TYPE, R.integer.def_dial_sound_type);
                loadStringSetting(stmt, AmigoSettings.VIRTUAL_DISPLAY_DEVICES,R.string.def_virtual_display_devices);
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 7;
        }
        //Gionee <wangguojing> <2014-02-11> add for CR01031420 end

        if (upgradeVersion == 7) {
        	
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                //Gionee <huangyuncai> <2014-03-04> add for CR01087530 begin
                loadIntegerSetting(stmt, NOTIFICATION_DISABLE_REMINDER, R.integer.def_notification_disable_reminder);
                loadIntegerSetting(stmt, NETWORK_SPEED_DISPLAY, R.integer.def_network_speed_display);
                //Gionee <huangyuncai> <2014-03-04> add for CR01087530 end
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 8;
        }
            
        if (upgradeVersion == 8) {
            
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                //  Gionee <chenml> <2014-05-04> add for CR01215994 begin
                loadBooleanSetting(stmt, AmigoSettings.SILENT_MODE_ENABLED,R.bool.def_silent_mode);
                loadIntegerSetting(stmt, AmigoSettings.LAST_MUSIC_VOLUME,R.integer.def_last_volume);
                loadIntegerSetting(stmt, AmigoSettings.VOLUME_MUSIC,R.integer.def_volume_music);
              //  Gionee <chenml> <2014-05-04> add for CR01215994 end
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 9;
        }
        //Gionee <wangguojing> <2014-05-12> add for CR01237681 begin
        if (upgradeVersion == 9) {
        
            updateDBValue(db);
            upgradeVersion = 10;
        }
        //Gionee <wangguojing> <2014-05-12> add for CR01237681 end
        
        //Gionee <wangguojing> <2014-05-19> add for CR01257437 begin
        if (upgradeVersion == 10) {
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadBooleanSetting(stmt, "adjust_screen_tone"/*AmigoSettings.ADJUST_SCREEN_TONE*/,
                        R.bool.def_adjust_screen_tone);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 11;
        }
        //Gionee <wangguojing> <2014-05-19> add for CR01257437 end
        
        //Gionee <wangguojing> <2014-05-29> add for CR01272670 begin
        if (upgradeVersion == 11) {
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadBooleanSetting(stmt, "user_experience_remind"/*AmigoSettings.USER_EXPERIENCE_REMIND*/,
                        R.bool.def_user_experience_remind);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 12;
        }
        //Gionee <wangguojing> <2014-05-29> add for CR01272670 end

        //Gionee <chenml> <2014-06-04> add for CR01274386 begin
        if (upgradeVersion == 12) {
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadBooleanSetting(stmt, AmigoSettings.RESPIRATION_LAMP_LOW_POWER,
                        R.bool.def_respirationlamp_low_power);
                loadBooleanSetting(stmt, AmigoSettings.RESPIRATION_LAMP_IN_CHARGE,
                        R.bool.def_respirationlamp_in_charge);          
                loadBooleanSetting(stmt, AmigoSettings.RESPIRATION_LAMP_NOTIFICATION,
                        R.bool.def_respirationlamp_notification);
                loadBooleanSetting(stmt, AmigoSettings.RESPIRATION_LAMP_MUSIC,
                        R.bool.def_respirationlamp_music);
                loadBooleanSetting(stmt, AmigoSettings.RESPIRATION_LAMP_CALL,
                        R.bool.def_respirationlamp_call);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 13;
        }
        //Gionee <chenml> <2014-06-04> add for CR01274386 end
        
        //Gionee <chenml> <2014-06-18> add for CR01272876 begin
        if(upgradeVersion == 13){
            
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                afreshDBvalue(stmt,db);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 14;
        }
        //Gionee <chenml> <2014-06-18> add for CR01272876 end
        //Gionee <wangguojing> <2014-09-18> add for CR01386770 begin
        if(upgradeVersion == 14){
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadBooleanSetting(stmt, /*AmigoSettings.AAL_CABC_BACKUP*/"aal_cabc_backup",
                        R.bool.def_aal_cabc_backup);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 15;
        }
        //Gionee <wangguojing> <2014-09-18> add for CR01386770 end
        
        //Gionee <chenml> <2014-09-25> add for CR01387135 begin
        if(upgradeVersion == 15){
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadStringSetting(stmt, /*AmigoSettings.KEY_RINGTONE_DATA*/"key_ringtone_data",
                        R.string.def_key_ringtone_data);
                loadStringSetting(stmt, /*AmigoSettings.KEY_RINGTONE2_DATA*/"key_ringtone2_data",
                        R.string.def_key_ringtone2_data);
                loadStringSetting(stmt, /*AmigoSettings.KEY_NOTIFICATION_DATA*/"key_notification_data",
                        R.string.def_key_notification_data);
                loadStringSetting(stmt, /*AmigoSettings.KEY_MMS_DATA*/"key_mms_data",
                        R.string.def_key_mms_data);
                loadStringSetting(stmt, /*AmigoSettings.KEY_MMS2_DATA*/"key_mms2_data",
                        R.string.def_key_mms2_data);
                loadStringSetting(stmt, /*AmigoSettings.KEY_VIDEO_DATA*/"key_video_data",
                        R.string.def_key_video_data);
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 16;
        }
        //Gionee <chenml> <2014-09-25> add for CR01387135 end
        //Gionee <chenml> <2014-09-27> add for CR01390965 begin
        if(upgradeVersion == 16){
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadBooleanSetting(stmt, AmigoSettings.SOUND_CONTROL_SWITCH,
                    R.bool.def_sound_control_switch);
                loadBooleanSetting(stmt, AmigoSettings.SOUND_CONTROL_CALLING,
                    R.bool.def_sound_control_calling);
                loadBooleanSetting(stmt, AmigoSettings.SOUND_CONTROL_MESSAGE,
                    R.bool.def_sound_control_message);
                loadBooleanSetting(stmt, AmigoSettings.SOUND_CONTROL_LOCKSCREEN,
                    R.bool.def_sound_control_lockscreen);
                loadBooleanSetting(stmt, AmigoSettings.SOUND_CONTROL_ALARMCLOCK,
                    R.bool.def_sound_control_alarmclock);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 17;
        }
        //Gionee <chenml> <2014-09-27> add for CR01390965 end
        if(upgradeVersion == 17){
            
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadIntegerSetting(stmt, /*AmigoSettings.IS_NEW_SSG*/"is_new_ssg",
                    R.integer.def_is_new_ssg);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 18;
        }
        // Gionee <chenml> <2014-09-29> modify for CR001392602 begin
        if(upgradeVersion == 18){
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadIntegerSetting(stmt, /*AmigoSettings.IS_PSENSOR_BROWSE_PICTURE_DG*/
                        "is_psensor_browse_picture_dg", R.integer.def_is_psensor_browse_dg);
                loadIntegerSetting(stmt, /*AmigoSettings.IS_PSENSOR_CTRL_VIDEO_DG*/"is_psensor_ctrl_video_dg",
                        R.integer.def_is_psensor_ctrl_video_dg);
                
                loadIntegerSetting(stmt, /* AmigoSettings.SDG_BROWSE_PHOTOS_PSENSOR*/
                        "sdg_browse_photos_psensor", R.integer.def_sdg_browse_photos_psensor);
                loadIntegerSetting(stmt, /*AmigoSettings.SDG_BROWSE_PHOTOS_SHOW_PSENSOR*/
                        "sdg_browse_photos_show_psensor", R.integer.def_sdg_browse_photos_show_psensor);
                loadIntegerSetting(stmt,/*AmigoSettings.SDG_VIDEO_PAUSE_psensor*/
                        "sdg_video_pause_psensor", R.integer.def_sdg_video_pause_psensor);
                loadIntegerSetting(stmt,  /*AmigoSettings.SDG_VIDEO_PAUSE_SHOW_psensor*/
                        "sdg_video_pause_show_psensor", R.integer.def_sdg_video_pause_show_psensor);
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 19;
        }
        // Gionee <chenml> <2014-09-29> modify for CR001392602 end
        //Gionee <wangguojing> <2014-10-17> add for CR01397189 begin
        if(upgradeVersion == 19){
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadIntegerSetting(stmt, /*AmigoSettings.IS_VIBRATION_ALERT_ON*/
                        "is_vibration_alert_on", R.integer.def_is_vibration_alert_on);
                
                loadIntegerSetting(stmt, /* AmigoSettings.SSG_VIBRATION_ALERT*/
                        "ssg_vibration_alert", R.integer.def_ssg_vibration_alert);
                loadIntegerSetting(stmt, /*AmigoSettings.SSG_VIBRATION_ALERT_SHOW*/
                        "ssg_vibration_alert_show", R.integer.def_ssg_vibration_alert_show);
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 20;
        }
        //Gionee <wangguojing> <2014-10-17> add for CR01397189 end
		
        //Gionee <wangguojing> <2014-10-28> add for CR01403182 begin
        if(upgradeVersion == 20){
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                
                loadIntegerSetting(stmt, /* AmigoSettings.VIRTUAL_KEY_VALUE*/
                        "virtual_key_value", R.integer.def_virtual_key_value);
                
                //Gionee <wangguojing> <2014-11-04> add for CR01406835 begin
                loadStringSetting(stmt, "black_gesture_e"/*AmigoSettings.BLACK_GESTURE_E*/,
                	R.string.def_black_gesture_e);
                loadStringSetting(stmt, "black_gesture_u_right"/*AmigoSettings.BLACK_GESTURE_U_RIGHT*/
                	,R.string.def_black_gesture_u_right);
                loadStringSetting(stmt, "black_gesture_m"/*AmigoSettings.BLACK_GESTURE_M*/,
                	R.string.def_black_gesture_m);
                loadStringSetting(stmt, "black_gesture_triangular"/*AmigoSettings.BLACK_GESTURE_TRIANGULAR*/
                	,R.string.def_black_gesture_triangular);
                loadStringSetting(stmt, "black_gesture_up_slide"/*AmigoSettings.BLACK_GESTURE_UP_SLIDE*/,
                	R.string.def_black_gesture_up_slide);
                loadStringSetting(stmt, "black_gesture_down_slide"/*AmigoSettings.BLACK_GESTURE_DOWN_SLIDE*/
                	,R.string.def_black_gesture_down_slide);
                loadStringSetting(stmt, "black_gesture_w"/*AmigoSettings.BLACK_GESTURE_W*/,
                	R.string.def_black_gesture_default);
                loadStringSetting(stmt, "black_gesture_z"/*AmigoSettings.BLACK_GESTURE_Z*/
                	,R.string.def_black_gesture_default);
                loadStringSetting(stmt, "black_gesture_o"/*AmigoSettings.BLACK_GESTURE_O*/,
                	R.string.def_black_gesture_default);
                loadStringSetting(stmt, "black_gesture_s"/*AmigoSettings.BLACK_GESTURE_S*/
                	,R.string.def_black_gesture_default);
                loadStringSetting(stmt, "black_gesture_u_left"/*AmigoSettings.BLACK_GESTURE_U_LEFT*/
                	,R.string.def_black_gesture_default);
                loadStringSetting(stmt, "black_gesture_u_up"/*AmigoSettings.BLACK_GESTURE_U_UP*/
                	,R.string.def_black_gesture_default);
                loadStringSetting(stmt, "black_gesture_u_down"/*AmigoSettings.BLACK_GESTURE_U_DOWN*/
                	,R.string.def_black_gesture_default);
                loadStringSetting(stmt, "black_gesture_left_slide"/*AmigoSettings.BLACK_GESTURE_LEFT_SLIDE*/
                	,R.string.def_black_gesture_default);
                loadStringSetting(stmt, "black_gesture_right_slide"/*AmigoSettings.BLACK_GESTURE_RIGHT_SLIDE*/
                	,R.string.def_black_gesture_default);
                loadBooleanSetting(stmt, /* AmigoSettings.IS_NEW_BLACK_GESTURE_ON*/
                        "is_new_black_gesture_on", R.bool.def_is_new_black_gesture_on);
                
                loadBooleanSetting(stmt, "ssg_smart_light_screen" /*AmigoSettings.SSG_SMART_LIGHT_SCREEN*/,
                	R.bool.def_ssg_smart_light_screen);
                loadBooleanSetting(stmt, "ssg_smart_light_screen_show" /*AmigoSettings.SSG_SMART_LIGHT_SCREEN_SHOW*/,
                	R.bool.def_ssg_smart_light_screen_show);
                loadBooleanSetting(stmt, /* AmigoSettings.IS_SMART_LIGHT_SCREEN_ON*/
                        "is_smart_light_screen_on", R.bool.def_is_smart_light_screen_on);
                //Gionee <wangguojing> <2014-11-04> add for CR01406835 end
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 21;
        }
        //Gionee <wangguojing> <2014-10-28> add for CR01403182 end
        //Gionee <wangguojing> <2014-11-17> add for CR01412715 begin
        if(upgradeVersion == 21){
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                
                loadBooleanSetting(stmt, "sdg_light_screen" /*AmigoSettings.SDG_LIGHT_SCREEN*/,
                	R.bool.def_sdg_light_screen);
                loadBooleanSetting(stmt, "sdg_light_screen_show" /*AmigoSettings.SDG_LIGHT_SCREEN_SHOW*/,
                	R.bool.def_sdg_light_screen_show);
                loadBooleanSetting(stmt, /* AmigoSettings.IS_SDG_LIGHT_SCREEN_ON*/
                        "is_sdg_light_screen_on", R.bool.def_is_sdg_light_screen_on);


                loadBooleanSetting(stmt, "sdg_unlock" /*AmigoSettings.SDG_UNLOCK*/,
                	R.bool.def_sdg_unlock);
                loadBooleanSetting(stmt, "sdg_unlock_show" /*AmigoSettings.SDG_UNLOCK_SHOW*/,
                	R.bool.def_sdg_unlock_show);
                loadBooleanSetting(stmt, /* AmigoSettings.IS_SDG_UNLOCK_ON*/
                        "is_sdg_unlock_on", R.bool.def_is_sdg_unlock_on);


                loadBooleanSetting(stmt, "sdg_desktop_slide" /*AmigoSettings.SDG_DESKTOP_SLIDE*/,
                	R.bool.def_sdg_desktop_slide);
                loadBooleanSetting(stmt, "sdg_desktop_slide_show" /*AmigoSettings.SDG_DESKTOP_SLIDE_SHOW*/,
                	R.bool.def_sdg_desktop_slide_show);
                loadBooleanSetting(stmt, /* AmigoSettings.IS_SDG_DESKTOP_SLIDE_ON*/
                        "is_sdg_desktop_slide_on", R.bool.def_is_sdg_desktop_slide_on);


                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 22;
        }
        //Gionee <wangguojing> <2014-11-17> add for CR01412715 end
        //Gionee <wangguojing> <2014-12-01> modify for CR01418765 begin
        if(upgradeVersion == 22){
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                
				loadBooleanSetting(stmt, "amigo_silent_switch"/*AmigoSettings.AMIGO_SILENT_SWITCH*/,
						R.bool.def_amigo_silent_switch);
				loadBooleanSetting(stmt, "amigo_vibration_switch"/*AmigoSettings.AMIGO_VIBRATION_SWITCH*/,
						R.bool.def_amigo_vibration_switch);
				
				loadBooleanSetting(stmt, AmigoSettings.LOCKSCREEN_ACCESS,
						R.bool.def_lockscreen_access);
				loadBooleanSetting(stmt, AmigoSettings.APPLICATIONS_ACCESS,
						R.bool.def_applications_access);

				loadIntegerSetting(stmt, /*AmigoSettings.BLACK_GESTURE_CONFIG_VALUE*/
						"black_gesture_config_value", R.integer.def_black_gesture_config_value);

                loadBooleanSetting(stmt, /*AmigoSettings.TIMING_ZEN_MODE*/"timing_zen_mode"
                        ,R.bool.def_timing_zen_mode);
                loadStringSetting(stmt, /*AmigoSettings.ZEN_MODE_DAYS*/"zen_mode_days"
                        ,R.string.def_zen_mode_days);
                loadIntegerSetting(stmt, /*AmigoSettings.ZEN_MODE_CONDITION_INDEX*/"zen_mode_condition_index"
                        ,R.integer.def_zen_mode_condition_index);
				
                loadBooleanSetting(stmt,/*AmigoSettings.CONTROL_CENTER_SWITCH*/"control_center_switch",
                        R.bool.def_control_center_switch);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 23;
        }
        //Gionee <wangguojing> <2014-12-01> modify for CR01418765 end
        if(upgradeVersion == 23){
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                
                loadBooleanSetting(stmt,/*AmigoSettings.SKYLIGHT_SWITCH*/"skylight_switch",
                        R.bool.def_skylight_switch);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 24;
        }
        if(upgradeVersion == 24){
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                
               loadBooleanSetting(stmt, "sdg_switch_music" /*AmigoSettings.SDG_SWITCH_MUSIC*/,
                       R.bool.def_sdg_switch_music);
               loadBooleanSetting(stmt, "is_sdg_switch_music_on"/* AmigoSettings.IS_SDG_DESKTOP_SLIDE_ON*/, 
                       R.bool.def_is_sdg_switch_music_on);
               
               loadBooleanSetting(stmt, "sdg_reader" /*AmigoSettings.SDG_READER*/,
                       R.bool.def_sdg_reader);
               loadBooleanSetting(stmt, "is_sdg_reader_on"/* AmigoSettings.IS_SDG_READER_ON*/, 
                       R.bool.def_is_sdg_reader_on);

               loadBooleanSetting(stmt, "blackgesture_support_doublefinger"/* AmigoSettings.BLACKGESTURE_SUPPORT_DOUBLEFINGER*/, 
                       R.bool.def_blackgesture_support_doublefinger);

               loadBooleanSetting(stmt, "is_fast_camera_on"/* AmigoSettings.IS_FAST_CAMERA_ON*/, 
                       R.bool.def_is_fast_camera_on);

               loadIntegerSetting(stmt, "low_battery_alert_value"/* AmigoSettings.LOW_BATTERY_ALERT_VALUE*/,
                       R.integer.def_low_battery_alert_value);

               loadBooleanSetting(stmt, "is_glove_patterns_on"/* AmigoSettings.IS_GLOVE_PATTERNS_ON*/, 
                       R.bool.def_is_glove_patterns_on);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 25;
        }
        if(upgradeVersion == 25){
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                
                loadBooleanSetting(stmt, "is_mtk_ipo_on"/* AmigoSettings.IS_MTK_IPO_ON*/, 
                        R.bool.def_is_mtk_ipo_on);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 26;
        }
        //Gionee <wangguojing> <2015-06-15> add for CR01501989 begin
        if(upgradeVersion == 26){
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                
                updateIPOValue(db);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 27;
        }
        //Gionee <wangguojing> <2015-06-15> add for CR01501989 end
        //Gionee <wangguojing> <2015-06-15> add for CR01501989 begin
        if(upgradeVersion == 27){
        
            //Gionee <lizhipeng> <2015-07-11> add for CR01518122 begin
            SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                    + " VALUES(?,?);");
                
                initCustomGestureData(stmt);
                loadBooleanSetting(stmt, "is_vibration_effect_on"/* AmigoSettings.IS_VIBRATION_EFFECT_ON*/, 
                        R.bool.def_is_vibration_effect_on);
                loadStringSetting(stmt, "vibration_effect_name"/* AmigoSettings.VIBRATION_EFFECT_NAME*/, 
                        R.string.def_vibration_effect_name);
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            //Gionee <lizhipeng> <2015-07-11> add for CR01518122 end
            upgradeVersion = 28;
        }
        //Gionee <wangguojing> <2015-06-15> add for CR01501989 end
        
        //Gionee <chenml> <2015-07-24> add for CR01526572 begin
        if(upgradeVersion == 28){

        	SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                    + " VALUES(?,?);");
                
                loadBooleanSetting(stmt, "is_remove_wifi_display"/* AmigoSettings.IS_REMOVE_WIFI_DISPLAY*/, 
                        R.bool.def_is_remove_wifi_display);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 29;
        }
        //Gionee <chenml> <2015-07-24> add for CR01526572 begin
        
        //Gionee <lizhipeng> <2015-07-29> add for CR01527696 begin
        if(upgradeVersion == 29){

        	SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                    + " VALUES(?,?);");
                
                loadBooleanSetting(stmt,"8605and8609_gesture_switch",
                        R.bool.def_8605_gesture_switch);
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 30;
        }
        //Gionee <lizhipeng> <2015-07-29> add for CR01527696 end

        //Gionee <wangguojing> <2015-08-03> add for CR01511884 begin
        if(upgradeVersion == 30){

        	SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                    + " VALUES(?,?);");
                
                loadStringSetting(stmt, "wirte_diag_config"/* AmigoSettings.WIRTE_DIAG_CONFIG*/, 
                        R.string.def_wirte_diag_config);
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 31;
        }
        //Gionee <wangguojing> <2015-08-03> add for CR01511884 end
        
        //Gionee <chenml> <2015-08-19> add for CR01540669 begin
        if(upgradeVersion == 31){

        	SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                    + " VALUES(?,?);");
                
                loadStringSetting(stmt, "tranferdata_progress"/* AmigoSettings.TRANFERDATA_PROGRESS*/, 
                        R.string.def_tranferdata_progress);
                loadBooleanSetting(stmt, "is_exsit_tranferdata"/* AmigoSettings.IS_EXSIT_TRANFERDATA*/, 
                        R.bool.def_is_exsit_tranferdata);
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 32;
        }
        //Gionee <chenml> <2015-08-19> add for CR01540669 end
        //Gionee <wangguojing> <2015-08-28> add for CR01542795 begin
        if(upgradeVersion == 32){

        	SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                    + " VALUES(?,?);");
                
                updateDiagConfigValue(db);
              //Gionee <lizhipeng> <2015-08-28> add for CR01545605 begin
                updateConfigValue(db);
              //Gionee <lizhipeng> <2015-08-28> add for CR01545605 end
                updateGlovePatternsSwitchValue(db);
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 33;
        }
        //Gionee <wangguojing> <2015-08-28> add for CR01542795 end
        
        //Gionee <chenml> <2015-08-31> add for CR01546929 begin
        if(upgradeVersion == 33){

        	SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                    + " VALUES(?,?);");
                
                loadBooleanSetting(stmt, "is_exist_private_space"/* AmigoSettings.IS_EXIST_PRIVATE_SPACE*/, 
                        R.bool.def_is_exist_private_space);
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 34;
        }
        //Gionee <chenml> <2015-08-19> add for CR01546929 end
        
        //Gionee <lizhipeng> <2015-09-06> add for CR01549142 begin
        if(upgradeVersion == 34){

        	SQLiteStatement stmt = null;
            try {
            	 db.beginTransaction();
                 stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                     + " VALUES(?,?);");
                 updateConfigValue(db,"black_gesture_u_right");
               //Gionee <lizhipeng> <2015-09-06> add for CR01548979 begin
                 updateConfigValue(db,"black_gesture_e");
                 updateConfigValue(db,"black_gesture_m");
               //Gionee <lizhipeng> <2015-09-06> add for CR01548979 begin
                 db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 35;
        }
        //Gionee <lizhipeng> <2015-09-06> add for CR01549142 end
        
      //Gionee <lizhipeng> <2015-09-11> add for CR01549480 begin
        if(upgradeVersion == 35){

        	SQLiteStatement stmt = null;
            try {
            	 db.beginTransaction();
                 stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                     + " VALUES(?,?);");              
                 updateLcmAclValue();              
                 db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 36;
        }
      //Gionee <lizhipeng> <2015-09-11> add for CR01549480 end 
        
        //Gionee <chenml> <2015-09-18> add for 111 begin
        if(upgradeVersion == 36){

        	SQLiteStatement stmt = null;
            try {
            	 db.beginTransaction();
                 stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                     + " VALUES(?,?);");          
                 
          loadBooleanSetting(stmt, "is_exist_default_storage",R.bool.def_is_exist_default_storage);       
          loadBooleanSetting(stmt, "is_exist_home_key_settings",R.bool.def_is_exist_home_key_settings);       
          loadIntegerSetting(stmt, "home_key_value", R.integer.def_home_key_value);
          
                 db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 37;
        }
        //Gionee <chenml> <2015-09-18> add for 111 begin home_key_value
        
        if(upgradeVersion == 37){
        
            SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                     + " VALUES(?,?);");          
                
                loadBooleanSetting(stmt, "fingerprint_is_front",R.bool.def_fingerprint_is_front);       
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 38;
        }
        //Gionee <lizhipeng> <2015-09-23> modify for CR01556254 begin
        if(upgradeVersion == 38){
            
            SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                     + " VALUES(?,?);");          
                
                loadBooleanSetting(stmt, AmigoSettings.GUEST_MODE,
      				  R.bool.def_guest_mode);
                loadBooleanSetting(stmt, "is_screen_saving_on",R.bool.def_is_screen_saving_on); 	  
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 39;
        }
      //Gionee <lizhipeng> <2015-09-23> modify for CR01556254 end

        //Gionee <wangguojing> <2015-10-21> add for CR01572592 begin
        if(upgradeVersion == 39){
        
            upgradeVersion = 40;
        }
        //Gionee <wangguojing> <2015-10-21> add for CR01572592 end
        
        //Gionee <lizhipneg> <2015-10-21> add for CR01556254 begin
        if(upgradeVersion == 40){
        	 SQLiteStatement stmt = null;
             try {
                 db.beginTransaction();
                 stmt = db.compileStatement("REPLACE INTO config(name,value)"
                    + " VALUES(?,?);");
                 loadBooleanSetting(stmt, AmigoSettings.GUEST_MODE,
     				  R.bool.def_guest_mode);
                 db.setTransactionSuccessful();
             } finally {
                 db.endTransaction();
                 if (stmt != null) stmt.close();
             }
            upgradeVersion = 41;
        }
      //Gionee <lizhipneg> <2015-10-21> add for CR01556254 end
        if(upgradeVersion == 41){
        
            SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                updateConfigValue(db,"is_exsit_tranferdata");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 42;
        }

        
        if (upgradeVersion != currentVersion) {
            // to do ..
        }
        //Gionee <wangguojing> <2015-10-21> add for CR01572592 begin
        updateDBValueForOTA(db);
        //Gionee <wangguojing> <2015-10-21> add for CR01572592 end
        
    }

    private void loadConfigSettings(SQLiteDatabase db) {
		Log.e(TAG,"loadConfigSettings");
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
		Log.e(TAG,"loadConfigSettings:" + key);
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
        //if (SystemProperties.get("ro.gn.flip.support").equals("yes")) {
            loadSetting(stmt, AmigoSettings.FLIP_SOUNDS_ENABLED, 1);
            
            loadStringSetting(stmt, AmigoSettings.FLIP_ON_SOUND,
                    R.string.zzzzz_gn_def_flip_on_sound);
            loadStringSetting(stmt, AmigoSettings.FLIP_OFF_SOUND,
                    R.string.zzzzz_gn_def_flip_off_sound);
        //}
        //Gionee fengjianyi 2012-08-22 modify for CR00673800 end
        
        //Gionee:zhang_xin 2012-12-09 add for start
        loadBooleanSetting(stmt, AmigoSettings.POWER_SAVER, 
                R.bool.def_gn_power_saver);
        //Gionee:zhang_xin 2012-12-09 add for end
        
        //Gionee <zhang_xin><2013-03-26> add for CR00788411 begin
        loadBooleanSetting(stmt, AmigoSettings.FONT_SIZE, 
                R.integer.def_gn_font_size);
        //Gionee <zhang_xin><2013-03-26> add for CR00788411 end
        
        //gionee zengxuanhui 20121022 add for CR00716758 begin
        loadBooleanSetting(stmt, AmigoSettings.HAPTIC_VIBRATION_ENABLED,
                R.bool.def_haptic_vibration);
        //gionee zengxuanhui 20121022 add for CR00716758 end
        
        //Gionee Jingjc 20121122 modify for CR00722601 begin
        loadBooleanSetting(stmt, AmigoSettings.SWITCH_VIBRATION_ENABLED,
                R.bool.def_switch_vibration);
        loadBooleanSetting(stmt, AmigoSettings.DIALPAD_VIBRATION_ENABLED,
                R.bool.def_dialpad_vibration);
        loadBooleanSetting(stmt, AmigoSettings.LOCKSCREEN_VIBRATION_ENABLED,
                R.bool.def_lockscreen_vibration);
        loadBooleanSetting(stmt, AmigoSettings.SELECTAPP_VIBRATION_ENABLED,
                R.bool.def_selectapp_vibration);
        //Gionee Jingjc 20121122 modify for CR00722601 end
        
        //Gionee <zengxuanhui> <2013-04-26> add for CR00797390 begin
        loadBooleanSetting(stmt, AmigoSettings.RING_VIBRATION_ENABLED,
                R.bool.def_ring_vibration);
        loadBooleanSetting(stmt, AmigoSettings.MMS_VIBRATION_ENABLED,
                R.bool.def_mms_vibration);
        loadBooleanSetting(stmt, AmigoSettings.NOTIFICATION_VIBRATION_ENABLED,
                R.bool.def_notification_vibration);
        //Gionee <zengxuanhui> <2013-04-26> add for CR00797390 end
        
      //  Gionee <chenml> <2014-05-04> add for CR01215994 begin
        loadBooleanSetting(stmt, AmigoSettings.SILENT_MODE_ENABLED,
                R.bool.def_silent_mode);
        loadIntegerSetting(stmt, AmigoSettings.LAST_MUSIC_VOLUME,
                R.integer.def_last_volume);
        loadIntegerSetting(stmt, AmigoSettings.VOLUME_MUSIC,
                R.integer.def_volume_music);
      //  Gionee <chenml> <2014-05-04> add for CR01215994 end
        // Gionee <wangyaohui><2013-05-30> add for CR00820909 begin
        //if (SystemProperties.get("ro.gn.fanfanwidget.support").equals("yes")) {
            loadBooleanSetting(stmt, AmigoSettings.FANFAN_WIDGET_AUTO_PUSH,
                    R.bool.def_fanfan_widget);
        //}
        // Gionee <wangyaohui><2013-05-30> add for CR00820909 end
        
        // Gionee <wangyaohui><2013-06-05> add for CR00823496 begin
        //if (SystemProperties.get("ro.gn.respirationlamp.support").equals("yes")) {
            loadBooleanSetting(stmt, AmigoSettings.RESPIRATION_LAMP_LOW_POWER,
                    R.bool.def_respirationlamp_low_power);
            loadBooleanSetting(stmt, AmigoSettings.RESPIRATION_LAMP_IN_CHARGE,
                    R.bool.def_respirationlamp_in_charge);			
            loadBooleanSetting(stmt, AmigoSettings.RESPIRATION_LAMP_NOTIFICATION,
                    R.bool.def_respirationlamp_notification);
            loadBooleanSetting(stmt, AmigoSettings.RESPIRATION_LAMP_MUSIC,
                    R.bool.def_respirationlamp_music);
            loadBooleanSetting(stmt, AmigoSettings.RESPIRATION_LAMP_CALL,
                    R.bool.def_respirationlamp_call);
        //}
        // Gionee <wangyaohui><2013-06-05> add for CR00823496 end

        // Gionee <wangguojing> <2013-08-19> add for CR00859362 begin
        //if (SystemProperties.get("ro.gn.gesture.support").equals("yes")) {
            loadBooleanSetting(stmt, "ssg_switch", //AmigoSettings.GN_SSG_SWITCH,
                    R.bool.def_ssg_switch);
            loadBooleanSetting(stmt, "dg_switch", //AmigoSettings.GN_DG_SWITCH,
                    R.bool.def_dg_switch);
            loadBooleanSetting(stmt, AmigoSettings.SSG_AUTO_DIAL,
                    R.bool.def_ssg_auto_dial);
            loadBooleanSetting(stmt, AmigoSettings.SSG_CALL_ACCESS,
                    R.bool.def_ssg_call_access);
            loadBooleanSetting(stmt, AmigoSettings.SSG_DELAY_ALARM,
                    R.bool.def_ssg_delay_alarm);
            loadBooleanSetting(stmt, AmigoSettings.SSG_SWITCH_SCREEN,
                    R.bool.def_ssg_switch_screen);
            loadBooleanSetting(stmt, AmigoSettings.SDG_CALL_ACCESS,
                    R.bool.def_sdg_call_access);
            loadBooleanSetting(stmt, AmigoSettings.SDG_BROWSE_PHOTOS,
                    R.bool.def_sdg_browse_photos);
            loadBooleanSetting(stmt, AmigoSettings.SDG_VIDEO_PROGRESS,
                    R.bool.def_sdg_video_progress);
            loadBooleanSetting(stmt, AmigoSettings.SDG_VIDEO_VOLUME,
                    R.bool.def_sdg_video_volume);
            loadBooleanSetting(stmt, AmigoSettings.SDG_VIDEO_PAUSE,
                    R.bool.def_sdg_video_pause);
            loadBooleanSetting(stmt, AmigoSettings.SSG_AUTO_DIAL_SHOW,
                    R.bool.def_ssg_auto_dial);
            loadBooleanSetting(stmt, AmigoSettings.SSG_CALL_ACCESS_SHOW,
                    R.bool.def_ssg_call_access);
            if (SystemProperties.get("ro.gn.only.disp.alarm.gesture").equals("yes")) {
                loadBooleanSetting(stmt, AmigoSettings.SSG_DELAY_ALARM_SHOW,
                        R.bool.def_ssg_delay_alarm);
            }else{
                loadBooleanSetting(stmt, AmigoSettings.SSG_DELAY_ALARM_SHOW,
                        R.bool.def_ssg_delay_alarm_show);
            }
            loadBooleanSetting(stmt, AmigoSettings.SSG_SWITCH_SCREEN_SHOW,
                    R.bool.def_ssg_switch_screen);
            loadBooleanSetting(stmt, AmigoSettings.SDG_CALL_ACCESS_SHOW,
                    R.bool.def_sdg_call_access);
            loadBooleanSetting(stmt, AmigoSettings.SDG_BROWSE_PHOTOS_SHOW,
                    R.bool.def_sdg_browse_photos_show);
            loadBooleanSetting(stmt, AmigoSettings.SDG_VIDEO_PROGRESS_SHOW,
                    R.bool.def_sdg_video_progress);
            loadBooleanSetting(stmt, AmigoSettings.SDG_VIDEO_VOLUME_SHOW,
                    R.bool.def_sdg_video_volume);
            loadBooleanSetting(stmt, AmigoSettings.SDG_VIDEO_PAUSE_SHOW,
                    R.bool.def_sdg_video_pause);
            //Gionee <wangguojing> <2013-10-11> add for CR00922210 begin
            loadBooleanSetting(stmt, AmigoSettings.SDG_DEL_PHOTOS,
                    R.bool.def_sdg_del_photos);
            loadBooleanSetting(stmt, AmigoSettings.SDG_DEL_PHOTOS_SHOW,
                    R.bool.def_sdg_del_photos);

            loadBooleanSetting(stmt, AmigoSettings.SDG_TASK_CONTROL,
                    R.bool.def_sdg_task_control);
            loadBooleanSetting(stmt, AmigoSettings.SDG_TASK_CONTROL_SHOW,
                    R.bool.def_sdg_task_control);
			
            loadBooleanSetting(stmt, AmigoSettings.SSG_DOUBLECLICK_WAKE,
                    R.bool.def_ssg_doubleclick_wake);
            loadBooleanSetting(stmt, AmigoSettings.SSG_DOUBLECLICK_WAKE_SHOW,
                    R.bool.def_ssg_doubleclick_wake_show);
            //Gionee <wangguojing> <2013-10-11> add for CR00922210 end

			
      	    // Gionee <bug> <wangyaohui> <2013-10-24> modify for CR00933322 begin	  		
            loadBooleanSetting(stmt,SSG_QUICK_OPERATING,R.bool.def_fast_operation);
	        loadSetting(stmt,SSG_QUICK_OPERATING_UP_GESTURE_CHOICE,1);
		    loadStringSetting(stmt,SSG_QUICK_OPERATING_UP_GESTURE_APP_NAME_OR_PHONE_NUMBER,R.string.def_fast_operation_up_app);
	        loadSetting(stmt,SSG_QUICK_OPERATING_RIGHT_GESTURE_CHOICE,1);
		    loadStringSetting(stmt,SSG_QUICK_OPERATING_RIGHT_GESTURE_APP_NAME_OR_PHONE_NUMBER,R.string.def_fast_operation_right_app);
			loadSetting(stmt,SSG_QUICK_OPERATING_LEFT_GESTURE_CHOICE,0);
			loadSetting(stmt,SSG_QUICK_OPERATING_LEFT_GESTURE_APP_NAME_OR_PHONE_NUMBER,"");
			loadSetting(stmt,SSG_QUICK_OPERATING_DOWN_GESTURE_CHOICE,0);
			loadSetting(stmt,SSG_QUICK_OPERATING_DOWN_GESTURE_APP_NAME_OR_PHONE_NUMBER,"");
      	    // Gionee <bug> <wangyaohui> <2013-10-24> modify for CR00933322 end
        //}
		
//        if (SystemProperties.get("ro.gn.soundctrl.support").equals("yes")) {
            loadBooleanSetting(stmt, AmigoSettings.SOUND_CONTROL_SWITCH,
                    R.bool.def_sound_control_switch);
            loadBooleanSetting(stmt, AmigoSettings.SOUND_CONTROL_CALLING,
                    R.bool.def_sound_control_calling);
            loadBooleanSetting(stmt, AmigoSettings.SOUND_CONTROL_MESSAGE,
                    R.bool.def_sound_control_message);
            loadBooleanSetting(stmt, AmigoSettings.SOUND_CONTROL_LOCKSCREEN,
                    R.bool.def_sound_control_lockscreen);
            loadBooleanSetting(stmt, AmigoSettings.SOUND_CONTROL_ALARMCLOCK,
                    R.bool.def_sound_control_alarmclock);
            //Gionee <chenml> <2013-09-25> add for CR00906622 begin
            loadBooleanSetting(stmt, AmigoSettings.SOUND_CONTROL_OPEN_REMIND,
					R.bool.def_sound_control_open_remind);
            loadBooleanSetting(stmt, AmigoSettings.SOUND_CONTROL_CLOSE_REMIND,
					R.bool.def_sound_control_close_remind);

            //Gionee <chenml> <2013-09-25> add for CR00906622 end
//        }
	  
	  //if (SystemProperties.get("ro.gn.guestmode.support").equals("yes")) {
		  loadBooleanSetting(stmt, AmigoSettings.GUEST_MODE,
				  R.bool.def_guest_mode);
		  loadBooleanSetting(stmt, AmigoSettings.FIRST_OPEN_GUEST_MODE,
				  R.bool.def_first_open_guest_mode);
		  loadBooleanSetting(stmt, AmigoSettings.GUEST_PASS_ENABLE,
				  R.bool.def_guest_pass_enable);
		  loadStringSetting(stmt, AmigoSettings.GUEST_PASS,
				  R.string.zzzzz_gn_def_guest_pass);
	  //}
	  // Gionee <wangguojing> <2013-08-19> add for CR00859362 end

	  //if (SystemProperties.get("ro.gn.flip.support").equals("yes")) {
		  loadIntegerSetting(stmt, AmigoSettings.FLIP_HANGUP_CALL_SWITCH,
				  R.integer.def_flip_hangup_call_default);
		  loadIntegerSetting(stmt, AmigoSettings.FLIP_ANSWER_CALL_SWITCH,
				  R.integer.def_flip_answer_call_default);
	  //}
	  loadBooleanSetting(stmt, AmigoSettings.Button_Light_State,
			  R.bool.def_button_light_state_default);
	  
	  //if (SystemProperties.get("ro.gn.networkalert.support", "yes").equals("yes")) {
		  loadBooleanSetting(stmt, AmigoSettings.WIFI_AUTO_NOTIFY,
				  R.bool.def_wifi_auto_notify_default);
	  //}
	  loadBooleanSetting(stmt, AmigoSettings.ALIGN_WAKE,
			  R.bool.def_align_wake_default);
	  
	  loadIntegerSetting(stmt, AmigoSettings.SCREEN_OFF_TIMEOUT_BACK_UP,
			  R.integer.def_screen_off_timeout);	  
	  
	  //if (SystemProperties.get("ro.gn.mms.alertMissMsg").equals("yes")) {
		  loadBooleanSetting(stmt, AmigoSettings.ALERT_MISS_MSG, R.bool.gn_def_alert_miss_msg);
		  loadIntegerSetting(stmt, AmigoSettings.ALERT_MISS_MSG_INTERVAL, R.integer.gn_def_alert_miss_msg_interval);
	  //}

	  //Gionee Jingjc 2012-09-16 add for CR00693470 start
	  loadBooleanSetting(stmt, AmigoSettings.AUTO_LCM_ACL,R.bool.def_auto_lcm_acl);
	  //Gionee Jingjc 2012-09-16 add for CR00693470 end
	
	  loadStringSetting(stmt, AmigoSettings.ALARM_RING,R.string.def_alarm_ring);
	
	  loadStringSetting(stmt, AmigoSettings.RINGTONE2,R.string.def_ringtone2);
	  loadStringSetting(stmt, AmigoSettings.MMS,R.string.def_mms);
	  loadStringSetting(stmt, AmigoSettings.MMS2,R.string.def_mms2);

	  loadStringSetting(stmt, AmigoSettings.THEME_PACKEAGE_NAME,R.string.def_theme_package_name); 
	  loadIntegerSetting(stmt, AmigoSettings.THEME_CHECK_PACKEAGE,R.integer.def_theme_check_package);		
	  
	  //if (SystemProperties.get("ro.gn.suspendbutton.support").equals("yes")) {
	      loadBooleanSetting(stmt, AmigoSettings.SUSPEND_BUTTON,R.bool.def_suspend_button);
	  //}

	  //if (SystemProperties.get("ro.gn.single.hand.support").equals("yes")) {
	      loadBooleanSetting(stmt, AmigoSettings.PHONE_KEYBOARD,R.bool.def_phone_keyboard);
	      loadBooleanSetting(stmt, AmigoSettings.INPUT_METHOD_KEYBOARD,R.bool.def_input_method_keyboard);
	      loadBooleanSetting(stmt, AmigoSettings.PATTERN_UNLOCKSCREEN,R.bool.def_pattern_unlockscreen);
	      loadBooleanSetting(stmt, AmigoSettings.SMALL_SCREEN_MODE,R.bool.def_small_screen_mode);
	      loadBooleanSetting(stmt, AmigoSettings.SCREEN_SIZE,R.bool.def_screen_size);
	      loadBooleanSetting(stmt, "phone_keyboard_place"/*AmigoSettings.PHONE_KEYBOARD_PLACE*/,R.bool.def_phone_keyboard);
	  //}
	  
	  //Gionee <wangguojing> <2013-10-11> add for GPS Optimization begin
	  loadBooleanSetting(stmt, AmigoSettings.GPS_PROMPT_REMIND,R.bool.def_gps_prompt_remind);
	  //Gionee <wangguojing> <2013-10-11> add for GPS Optimization end

	  //Gionee <wangguojing> <2013-10-24> add for CR00933390 begin
	  //if (SystemProperties.get("ro.gn.floatingwindow.support").equals("yes")) {
	      loadStringSetting(stmt, AmigoSettings.VIRTUAL_DISPLAY_DEVICES,R.string.def_virtual_display_devices);
	  //}
	  //Gionee <wangguojing> <2013-10-24> add for CR00933390 end

	  //Gionee <wangguojing> <2013-10-31> add for CR00942710 begin
	  //if (SystemProperties.get("ro.gn.glove_patterns.support").equals("yes")) {
	      loadBooleanSetting(stmt, AmigoSettings.GLOVE_PATTERNS,R.bool.def_glove_patterns);
	  //}
	  //Gionee <wangguojing> <2013-10-31> add for CR00942710 end
	  
	  //Gionee <wangguojing> <2013-11-02> add for CR00943428 begin
	  //if (SystemProperties.get("ro.gn.voicewake.support").equals("yes")) {
	      loadBooleanSetting(stmt, AmigoSettings.VOICE_WAKE_SWITCH,R.bool.def_voice_wake_switch);
	      loadBooleanSetting(stmt, AmigoSettings.VOICE_WAKE_REMIND,R.bool.def_voice_wake_remind);
	      loadBooleanSetting(stmt, AmigoSettings.VOICE_WAKE_WORD,R.bool.def_voice_wake_word);
	  //}
	  //Gionee <wangguojing> <2013-11-02> add for CR00943428 end
	  
	  //Gionee <wangguojing> <2013-11-04> add for CR00942651 begin
	  //if (SystemProperties.get("ro.gn.lcd.effect.support").equals("yes")) {
	      loadBooleanSetting(stmt, AmigoSettings.LCD_EFFECT_MODE,R.bool.def_lcd_effect_adjust);
	  //}
	  //Gionee <wangguojing> <2013-11-02> add for CR00942651 end
	  
	  //Gionee <wangguojing> <2013-11-04> add for CR00944538 begin
	  loadBooleanSetting(stmt, "show_powersave_dialog"/*AmigoSettings.SHOW_POWERSAVE_DIALOG*/,R.bool.def_show_powersave_dialog);
	  //Gionee <wangguojing> <2013-11-04> add for CR00944538 end
	  //Gionee <wangguojing> <2013-12-23> add for CR00989512 begin
	  loadBooleanSetting(stmt, AmigoSettings.INTERNATIONAL_ROAMING_SWITCH,
	          R.bool.def_international_roaming_switch);
	  //Gionee <wangguojing> <2013-12-23> add for CR00989512 end
	  //Gionee <wangguojing> <2013-12-26> add for CR00992408 begin
	  loadIntegerSetting(stmt, AmigoSettings.LCD_COLOR_VALUE,
	          R.integer.def_lcd_color_value);
	  //Gionee <wangguojing> <2013-12-26> add for CR00992408 end
      //Gionee <wangguojing> <2013-12-27> add for CR00993259 begin
      loadBooleanSetting(stmt, AmigoSettings.ROTATE_CAMERA_OPEN_APP,
              R.bool.def_rotate_camera_open_app);
      loadBooleanSetting(stmt, AmigoSettings.USER_EXPERIENCE,
              R.bool.def_user_experience);
      //Gionee <wangguojing> <2013-12-27> add for CR00993259 end
      //Gionee <wangguojing> <2013-12-30> add for CR00997383 begin
      loadIntegerSetting(stmt, AmigoSettings.DIAl_SOUND_TYPE,
              R.integer.def_dial_sound_type);
      //Gionee <wangguojing> <2013-12-30> add for CR00997383 end
      //Gionee <huangyuncai> <2014-03-04> add for CR01087530 begin
      loadIntegerSetting(stmt, NOTIFICATION_DISABLE_REMINDER, R.integer.def_notification_disable_reminder);
      loadIntegerSetting(stmt, NETWORK_SPEED_DISPLAY, R.integer.def_network_speed_display);
      //Gionee <huangyuncai> <2014-03-04> add for CR01087530 end

      //Gionee <wangguojing> <2014-05-19> add for CR01257437 begin
      loadBooleanSetting(stmt, "adjust_screen_tone"/*AmigoSettings.ADJUST_SCREEN_TONE*/,
              R.bool.def_adjust_screen_tone);
      //Gionee <wangguojing> <2014-05-19> add for CR01257437 end

      //Gionee <wangguojing> <2014-05-29> add for CR01272670 begin
	  loadBooleanSetting(stmt, "user_experience_remind"/*AmigoSettings.USER_EXPERIENCE_REMIND*/,
			  R.bool.def_user_experience_remind);
      //Gionee <wangguojing> <2014-05-29> add for CR01272670 end
      //Gionee <wangguojing> <2014-09-18> add for CR01386770 begin
      loadBooleanSetting(stmt, /*AmigoSettings.AAL_CABC_BACKUP*/"aal_cabc_backup",
			  R.bool.def_aal_cabc_backup);
      //Gionee <wangguojing> <2014-05-29> add for CR01386770 end
	  
      //Gionee <chenml> <2014-09-25> add for CR01387135 begin
      loadStringSetting(stmt, /*AmigoSettings.KEY_RINGTONE_DATA*/"key_ringtone_data",
              R.string.def_key_ringtone_data);
      loadStringSetting(stmt, /*AmigoSettings.KEY_RINGTONE2_DATA*/"key_ringtone2_data",
              R.string.def_key_ringtone2_data);
      loadStringSetting(stmt, /*AmigoSettings.KEY_NOTIFICATION_DATA*/"key_notification_data",
              R.string.def_key_notification_data);
      loadStringSetting(stmt, /*AmigoSettings.KEY_MMS_DATA*/"key_mms_data",
              R.string.def_key_mms_data);
      loadStringSetting(stmt, /*AmigoSettings.KEY_MMS2_DATA*/"key_mms2_data",
              R.string.def_key_mms2_data);
      loadStringSetting(stmt, /*AmigoSettings.KEY_VIDEO_DATA*/"key_video_data",
              R.string.def_key_video_data);
      //Gionee <chenml> <2014-09-25> add for CR01387135 end
      
      // Gionee <chenml> <2014-09-29> modify for CR001392602 begin
        // Gionee <chenml> <2014-09-29> modify for CR001392602 begin
        loadIntegerSetting(stmt, /*AmigoSettings.IS_NEW_SSG*/"is_new_ssg", R.integer.def_is_new_ssg);
        // Gionee <chenml> <2014-09-29> modify for CR001392602 end
        loadIntegerSetting(stmt, /*AmigoSettings.IS_PSENSOR_BROWSE_PICTURE_DG*/
                "is_psensor_browse_picture_dg", R.integer.def_is_psensor_browse_dg);
        loadIntegerSetting(stmt, /*AmigoSettings.IS_PSENSOR_CTRL_VIDEO_DG*/"is_psensor_ctrl_video_dg",
                R.integer.def_is_psensor_ctrl_video_dg);

        loadIntegerSetting(stmt, /* AmigoSettings.SDG_BROWSE_PHOTOS_PSENSOR*/
                "sdg_browse_photos_psensor", R.integer.def_sdg_browse_photos_psensor);
        loadIntegerSetting(stmt, /*AmigoSettings.SDG_BROWSE_PHOTOS_SHOW_PSENSOR*/
                "sdg_browse_photos_show_psensor", R.integer.def_sdg_browse_photos_show_psensor);
        loadIntegerSetting(stmt,/*AmigoSettings.SDG_VIDEO_PAUSE_PSENSOR*/
                "sdg_video_pause_psensor", R.integer.def_sdg_video_pause_psensor);
        loadIntegerSetting(stmt, /*AmigoSettings.SDG_VIDEO_PAUSE_SHOW_PSENSOR*/
                "sdg_video_pause_show_psensor", R.integer.def_sdg_video_pause_show_psensor);
        // Gionee <chenml> <2014-09-29> modify for CR001392602 end
        //Gionee <wangguojing> <2014-10-17> add for CR01397189 begin
        loadIntegerSetting(stmt, /*AmigoSettings.IS_VIBRATION_ALERT_ON*/
                 "is_vibration_alert_on", R.integer.def_is_vibration_alert_on);
        
        loadIntegerSetting(stmt, /* AmigoSettings.SSG_VIBRATION_ALERT*/
                "ssg_vibration_alert", R.integer.def_ssg_vibration_alert);
        loadIntegerSetting(stmt, /*AmigoSettings.SSG_VIBRATION_ALERT_SHOW*/
                "ssg_vibration_alert_show", R.integer.def_ssg_vibration_alert_show);
        //Gionee <wangguojing> <2014-10-17> add for CR01397189 end
        //Gionee <wangguojing> <2014-10-28> add for CR01403182 begin
        loadIntegerSetting(stmt, /* AmigoSettings.VIRTUAL_KEY_VALUE*/
                "virtual_key_value", R.integer.def_virtual_key_value);
        //Gionee <wangguojing> <2014-10-28> add for CR01403182 end
        //Gionee <wangguojing> <2014-11-04> add for CR01406835 begin
        loadStringSetting(stmt, "black_gesture_e"/*AmigoSettings.BLACK_GESTURE_E*/,
        	R.string.def_black_gesture_e);
        loadStringSetting(stmt, "black_gesture_u_right"/*AmigoSettings.BLACK_GESTURE_U_RIGHT*/
        	,R.string.def_black_gesture_u_right);
        loadStringSetting(stmt, "black_gesture_m"/*AmigoSettings.BLACK_GESTURE_M*/,
        	R.string.def_black_gesture_m);
        loadStringSetting(stmt, "black_gesture_triangular"/*AmigoSettings.BLACK_GESTURE_TRIANGULAR*/
        	,R.string.def_black_gesture_triangular);
        loadStringSetting(stmt, "black_gesture_up_slide"/*AmigoSettings.BLACK_GESTURE_UP_SLIDE*/,
        	R.string.def_black_gesture_up_slide);
        loadStringSetting(stmt, "black_gesture_down_slide"/*AmigoSettings.BLACK_GESTURE_DOWN_SLIDE*/
        	,R.string.def_black_gesture_down_slide);
        loadStringSetting(stmt, "black_gesture_w"/*AmigoSettings.BLACK_GESTURE_W*/,
        	R.string.def_black_gesture_default);
        loadStringSetting(stmt, "black_gesture_z"/*AmigoSettings.BLACK_GESTURE_Z*/
        	,R.string.def_black_gesture_default);
        loadStringSetting(stmt, "black_gesture_o"/*AmigoSettings.BLACK_GESTURE_O*/,
        	R.string.def_black_gesture_default);
        loadStringSetting(stmt, "black_gesture_s"/*AmigoSettings.BLACK_GESTURE_S*/
        	,R.string.def_black_gesture_default);
        loadStringSetting(stmt, "black_gesture_u_left"/*AmigoSettings.BLACK_GESTURE_U_LEFT*/
        	,R.string.def_black_gesture_default);
        loadStringSetting(stmt, "black_gesture_u_up"/*AmigoSettings.BLACK_GESTURE_U_UP*/
        	,R.string.def_black_gesture_default);
        loadStringSetting(stmt, "black_gesture_u_down"/*AmigoSettings.BLACK_GESTURE_U_DOWN*/
        	,R.string.def_black_gesture_default);
        loadStringSetting(stmt, "black_gesture_left_slide"/*AmigoSettings.BLACK_GESTURE_LEFT_SLIDE*/
        	,R.string.def_black_gesture_default);
        loadStringSetting(stmt, "black_gesture_right_slide"/*AmigoSettings.BLACK_GESTURE_RIGHT_SLIDE*/
        	,R.string.def_black_gesture_default);
        loadBooleanSetting(stmt, /* AmigoSettings.IS_NEW_BLACK_GESTURE_ON*/
        		"is_new_black_gesture_on", R.bool.def_is_new_black_gesture_on);
        
        loadBooleanSetting(stmt, "ssg_smart_light_screen" /*AmigoSettings.SSG_SMART_LIGHT_SCREEN*/,
            R.bool.def_ssg_smart_light_screen);
        loadBooleanSetting(stmt, "ssg_smart_light_screen_show" /*AmigoSettings.SSG_SMART_LIGHT_SCREEN_SHOW*/,
            R.bool.def_ssg_smart_light_screen_show);
        loadBooleanSetting(stmt, /* AmigoSettings.IS_SMART_LIGHT_SCREEN_ON*/
        		"is_smart_light_screen_on", R.bool.def_is_smart_light_screen_on);
        //Gionee <wangguojing> <2014-11-04> add for CR01406835 end
        
        //Gionee <wangguojing> <2014-11-17> add for CR01412715 begin
        loadBooleanSetting(stmt, "sdg_light_screen" /*AmigoSettings.SDG_LIGHT_SCREEN*/,
        	R.bool.def_sdg_light_screen);
        loadBooleanSetting(stmt, "sdg_light_screen_show" /*AmigoSettings.SDG_LIGHT_SCREEN_SHOW*/,
        	R.bool.def_sdg_light_screen_show);
        loadBooleanSetting(stmt, /* AmigoSettings.IS_SDG_LIGHT_SCREEN_ON*/
        		"is_sdg_light_screen_on", R.bool.def_is_sdg_light_screen_on);
        
        
        loadBooleanSetting(stmt, "sdg_unlock" /*AmigoSettings.SDG_UNLOCK*/,
        	R.bool.def_sdg_unlock);
        loadBooleanSetting(stmt, "sdg_unlock_show" /*AmigoSettings.SDG_UNLOCK_SHOW*/,
        	R.bool.def_sdg_unlock_show);
        loadBooleanSetting(stmt, /* AmigoSettings.IS_SDG_UNLOCK_ON*/
        		"is_sdg_unlock_on", R.bool.def_is_sdg_unlock_on);
        
        
        loadBooleanSetting(stmt, "sdg_desktop_slide" /*AmigoSettings.SDG_DESKTOP_SLIDE*/,
        	R.bool.def_sdg_desktop_slide);
        loadBooleanSetting(stmt, "sdg_desktop_slide_show" /*AmigoSettings.SDG_DESKTOP_SLIDE_SHOW*/,
        	R.bool.def_sdg_desktop_slide_show);
        loadBooleanSetting(stmt, /* AmigoSettings.IS_SDG_DESKTOP_SLIDE_ON*/
        		"is_sdg_desktop_slide_on", R.bool.def_is_sdg_desktop_slide_on);
        //Gionee <wangguojing> <2014-11-17> add for CR01412715 end
        
        loadBooleanSetting(stmt, "amigo_silent_switch"/*AmigoSettings.AMIGO_SILENT_SWITCH*/,
                R.bool.def_amigo_silent_switch);
        loadBooleanSetting(stmt, "amigo_vibration_switch"/*AmigoSettings.AMIGO_VIBRATION_SWITCH*/,
                R.bool.def_amigo_vibration_switch);
		
        loadBooleanSetting(stmt, AmigoSettings.LOCKSCREEN_ACCESS,
                R.bool.def_lockscreen_access);
        loadBooleanSetting(stmt, AmigoSettings.APPLICATIONS_ACCESS,
                R.bool.def_applications_access);

        loadIntegerSetting(stmt, /*AmigoSettings.BLACK_GESTURE_CONFIG_VALUE*/
                "black_gesture_config_value", R.integer.def_black_gesture_config_value);

        loadBooleanSetting(stmt, /*AmigoSettings.TIMING_ZEN_MODE*/"timing_zen_mode"
                ,R.bool.def_timing_zen_mode);
        loadStringSetting(stmt, /*AmigoSettings.ZEN_MODE_DAYS*/"zen_mode_days"
                ,R.string.def_zen_mode_days);
        loadIntegerSetting(stmt, /*AmigoSettings.ZEN_MODE_CONDITION_INDEX*/"zen_mode_condition_index"
                ,R.integer.def_zen_mode_condition_index);

        loadBooleanSetting(stmt,/*AmigoSettings.CONTROL_CENTER_SWITCH*/"control_center_switch",
                R.bool.def_control_center_switch);

        loadBooleanSetting(stmt,/*AmigoSettings.SKYLIGHT_SWITCH*/"skylight_switch",
                R.bool.def_skylight_switch);

        loadBooleanSetting(stmt, "sdg_switch_music" /*AmigoSettings.SDG_SWITCH_MUSIC*/,
                R.bool.def_sdg_switch_music);
        loadBooleanSetting(stmt, "is_sdg_switch_music_on"/* AmigoSettings.IS_SDG_DESKTOP_SLIDE_ON*/, 
                R.bool.def_is_sdg_switch_music_on);
        
        loadBooleanSetting(stmt, "sdg_reader" /*AmigoSettings.SDG_READER*/,
                R.bool.def_sdg_reader);
        loadBooleanSetting(stmt, "is_sdg_reader_on"/* AmigoSettings.IS_SDG_READER_ON*/, 
                R.bool.def_is_sdg_reader_on);
        loadBooleanSetting(stmt, "blackgesture_support_doublefinger"/* AmigoSettings.BLACKGESTURE_SUPPORT_DOUBLEFINGER*/, 
                R.bool.def_blackgesture_support_doublefinger);

        loadBooleanSetting(stmt, "is_fast_camera_on"/* AmigoSettings.IS_FAST_CAMERA_ON*/, 
                R.bool.def_is_fast_camera_on);
		
        loadIntegerSetting(stmt, "low_battery_alert_value"/* AmigoSettings.LOW_BATTERY_ALERT_VALUE*/,
                R.integer.def_low_battery_alert_value);

        loadBooleanSetting(stmt, "is_glove_patterns_on"/* AmigoSettings.IS_GLOVE_PATTERNS_ON*/, 
                R.bool.def_is_glove_patterns_on);

        loadBooleanSetting(stmt, "is_mtk_ipo_on"/* AmigoSettings.IS_MTK_IPO_ON*/, 
                R.bool.def_is_mtk_ipo_on);
        //Gionee <lizhipeng> <2015-07-11> add for CR01518122 begin
        initCustomGestureData(stmt);
        //Gionee <lizhipeng> <2015-07-11> add for CR01518122 end
        loadBooleanSetting(stmt, "is_vibration_effect_on"/* AmigoSettings.IS_VIBRATION_EFFECT_ON*/, 
                R.bool.def_is_vibration_effect_on);
        loadStringSetting(stmt, "vibration_effect_name"/* AmigoSettings.VIBRATION_EFFECT_NAME*/, 
                R.string.def_vibration_effect_name);
        
        //Gionee <chenml> <2015-07-24> add for CR01526572 begin      
        loadBooleanSetting(stmt, "is_remove_wifi_display"/* AmigoSettings.IS_REMOVE_WIFI_DISPLAY*/, 
                R.bool.def_is_remove_wifi_display);
        //Gionee <chenml> <2015-07-24> add for CR01526572 end 
        //Gionee <wangguojing> <2015-08-03> add for CR01511884 begin
        loadStringSetting(stmt, "wirte_diag_config"/* AmigoSettings.WIRTE_DIAG_CONFIG*/, 
                R.string.def_wirte_diag_config);
        //Gionee <wangguojing> <2015-08-03> add for CR01511884 end
        
        //Gionee <chenml> <2015-08-19> add for CR01540669 begin
        loadStringSetting(stmt, "tranferdata_progress"/* AmigoSettings.TRANFERDATA_PROGRESS*/, 
                R.string.def_tranferdata_progress);
        loadBooleanSetting(stmt, "is_exsit_tranferdata"/* AmigoSettings.IS_EXSIT_TRANFERDATA*/, 
                R.bool.def_is_exsit_tranferdata);
        //Gionee <chenml> <2015-08-19> add for CR01540669 end
       
        //Gionee <chenml> <2015-08-31> add for CR01546929 begin
        loadBooleanSetting(stmt, "is_exist_private_space"/* AmigoSettings.IS_EXIST_PRIVATE_SPACE*/, 
                R.bool.def_is_exist_private_space);
        //Gionee <chenml> <2015-08-31> add for CR01546929 end
        
        loadBooleanSetting(stmt, "is_exist_default_storage",R.bool.def_is_exist_default_storage);       
        loadBooleanSetting(stmt, "is_exist_home_key_settings",R.bool.def_is_exist_home_key_settings);       
        loadIntegerSetting(stmt, "home_key_value", R.integer.def_home_key_value);
        loadBooleanSetting(stmt, "fingerprint_is_front",R.bool.def_fingerprint_is_front);       
        loadBooleanSetting(stmt, "is_screen_saving_on",R.bool.def_is_screen_saving_on);       
        
    }
    
  //Gionee <lizhipeng> <2015-07-11> add for CR01518122 begin
    private void initCustomGestureData(SQLiteStatement stmt){
        loadBooleanSetting(stmt,"custom_gesture_switch",
                R.bool.def_custom_gesture_switch);
        for(int i=1;i<=15;i++){
            loadStringSetting(stmt, "custom_gesture"+i ,R.string.def_custom_gesture_value);
        }    
 
    }
  //Gionee <lizhipeng> <2015-07-11> add for CR01518122 end

    //Gionee <wangguojing> <2013-10-11> add for CR00932764 begin
    private void startCopySettingsValues(SQLiteStatement stmt) {
        int temp;
        String str;
        ContentResolver cr = mContext.getContentResolver();
        temp = Settings.System.getInt(mContext.getContentResolver(),"gn_font_size", 0);
        if(temp != 0){
            loadSetting(stmt, AmigoSettings.FONT_SIZE,temp);
            Settings.System.putInt(mContext.getContentResolver(), "gn_font_size", 0);
        }
        //if(SystemProperties.get("ro.gn.respirationlamp.support").equals("yes")){
            temp = Settings.System.getInt(cr,"gn_respirationlamp_low_power", 1);
            if(temp != 1){
                loadSetting(stmt, AmigoSettings.RESPIRATION_LAMP_LOW_POWER, temp);
                Settings.System.putInt(cr, "gn_respirationlamp_low_power", 1);
            }
            
            temp = Settings.System.getInt(cr,"gn_respirationlamp_in_charge", 1);
            if(temp != 1){
                loadSetting(stmt, AmigoSettings.RESPIRATION_LAMP_IN_CHARGE, temp);
                Settings.System.putInt(cr, "gn_respirationlamp_in_charge", 1);
            }
            
            temp = Settings.System.getInt(cr,"gn_respirationlamp_notification", 1);
            if(temp != 1){
                loadSetting(stmt, AmigoSettings.RESPIRATION_LAMP_NOTIFICATION, temp);
                Settings.System.putInt(cr, "gn_respirationlamp_notification", 1);
            }
            
            temp = Settings.System.getInt(cr,"gn_respirationlamp_music", 1);
            if(temp != 1){
                loadSetting(stmt, AmigoSettings.RESPIRATION_LAMP_MUSIC, temp);
                Settings.System.putInt(cr, "gn_respirationlamp_music", 1);
            }
            
            temp = Settings.System.getInt(cr,"gn_respirationlamp_call", 1);
            if(temp != 1){
                loadSetting(stmt, AmigoSettings.RESPIRATION_LAMP_CALL, temp);
                Settings.System.putInt(cr, "gn_respirationlamp_call", 1);
            }
        //}
		
        //if (SystemProperties.get("ro.gn.gesture.support").equals("yes")) {
        
            temp = Settings.System.getInt(cr,"ssg_auto_dial", 0);
            if(temp != 0){
                loadSetting(stmt, AmigoSettings.SSG_AUTO_DIAL, temp);
                loadSetting(stmt, AmigoSettings.SSG_AUTO_DIAL_SHOW, temp);
                Settings.System.putInt(cr, "ssg_auto_dial", 0);
            }
            
            temp = Settings.System.getInt(cr,"ssg_call_access", 0);
            if(temp != 0){
                loadSetting(stmt, AmigoSettings.SSG_CALL_ACCESS, temp);
                loadSetting(stmt, AmigoSettings.SSG_CALL_ACCESS_SHOW, temp)	;			
                Settings.System.putInt(cr, "ssg_call_access", 0);
            }
            
            temp = Settings.System.getInt(cr,"ssg_delay_alarm", 0);
            if(temp != 0){
                loadSetting(stmt, AmigoSettings.SSG_DELAY_ALARM, temp);
                loadSetting(stmt, AmigoSettings.SSG_DELAY_ALARM_SHOW, temp);
                
                Settings.System.putInt(cr, "ssg_delay_alarm", 0);
            }
            
            temp = Settings.System.getInt(cr,"ssg_switch_screen", 0);
            if(temp != 0){
                loadSetting(stmt, AmigoSettings.SSG_SWITCH_SCREEN, temp);
                loadSetting(stmt, AmigoSettings.SSG_SWITCH_SCREEN_SHOW, temp);
                Settings.System.putInt(cr, "ssg_switch_screen", 0);
            }
            
            temp = Settings.System.getInt(cr,"sdg_call_access", 0);
            if(temp != 0){
                loadSetting(stmt, AmigoSettings.SDG_CALL_ACCESS, temp);
                loadSetting(stmt, AmigoSettings.SDG_CALL_ACCESS_SHOW, temp);
                Settings.System.putInt(cr, "sdg_call_access", 0);
            }
            
            temp = Settings.System.getInt(cr,"sdg_browse_photos", 0);
            if(temp != 0){
                loadSetting(stmt, AmigoSettings.SDG_BROWSE_PHOTOS, temp);
                loadSetting(stmt, AmigoSettings.SDG_BROWSE_PHOTOS_SHOW, temp);
                Settings.System.putInt(cr, "sdg_browse_photos", 0);
            }
            
            temp = Settings.System.getInt(cr,"sdg_video_progress", 0);
            if(temp != 0){
                loadSetting(stmt, AmigoSettings.SDG_VIDEO_PROGRESS, temp);
                loadSetting(stmt, AmigoSettings.SDG_VIDEO_PROGRESS_SHOW, temp);
                Settings.System.putInt(cr, "sdg_video_progress", 0);
            }
            
            temp = Settings.System.getInt(cr,"sdg_video_volume", 0);
            if(temp != 0){
                loadSetting(stmt, AmigoSettings.SDG_VIDEO_VOLUME, temp);
                loadSetting(stmt, AmigoSettings.SDG_VIDEO_VOLUME_SHOW, temp);
                Settings.System.putInt(cr, "sdg_video_volume", 0);
            }
            
            temp = Settings.System.getInt(cr,"sdg_video_pause", 0);
            if(temp != 0){
                loadSetting(stmt, AmigoSettings.SDG_VIDEO_PAUSE, temp);
                loadSetting(stmt, AmigoSettings.SDG_VIDEO_PAUSE_SHOW, temp);
                
                Settings.System.putInt(cr, "sdg_video_pause", 0);
            }
            
            temp = Settings.System.getInt(cr,"ssg_switch", 0);
            if(temp != 0){
                loadSetting(stmt, AmigoSettings.GN_SSG_SWITCH, temp);
                Settings.System.putInt(cr, "ssg_switch", 0);
            }
            
            temp = Settings.System.getInt(cr,"dg_switch", 0);
            if(temp != 0){
                loadSetting(stmt, AmigoSettings.GN_DG_SWITCH, temp);
                Settings.System.putInt(cr, "dg_switch", 0);
            }
        //}
		
        //if(SystemProperties.get("ro.gn.soundctrl.support").equals("yes")){
        
            temp = Settings.System.getInt(cr,"gn_sound_control_switch", 0);
            if(temp != 0){
                loadSetting(stmt, AmigoSettings.SOUND_CONTROL_SWITCH, temp);
                Settings.System.putInt(cr, "gn_sound_control_switch", 0);
            }
            
            temp = Settings.System.getInt(cr,"gn_sound_control_calling", 0);
            if(temp != 0){
                loadSetting(stmt, AmigoSettings.SOUND_CONTROL_CALLING, temp);
                Settings.System.putInt(cr, "gn_sound_control_calling", 0);
            }
            
            temp = Settings.System.getInt(cr,"gn_sound_control_message", 0);
            if(temp != 0){
                loadSetting(stmt, AmigoSettings.SOUND_CONTROL_MESSAGE, temp);
                Settings.System.putInt(cr, "gn_sound_control_message", 0);
            }
            
            temp = Settings.System.getInt(cr,"gn_sound_control_lockscreen", 0);
            if(temp != 0){
                loadSetting(stmt, AmigoSettings.SOUND_CONTROL_LOCKSCREEN, temp);
                Settings.System.putInt(cr, "gn_sound_control_lockscreen", 0);
            }
            
            temp = Settings.System.getInt(cr,"gn_sound_control_alarmclock", 0);
            if(temp != 0){
                loadSetting(stmt, AmigoSettings.SOUND_CONTROL_ALARMCLOCK, temp);
                Settings.System.putInt(cr, "gn_sound_control_alarmclock", 0);
            }
        
        //}

        //if (SystemProperties.get("ro.gn.flip.support").equals("yes")) {
            temp = Settings.System.getInt(cr,"gn_flip_sounds_enabled", 1);
            if(temp != 1){
                loadSetting(stmt, AmigoSettings.FLIP_SOUNDS_ENABLED, temp);
                Settings.System.putInt(cr, "gn_flip_sounds_enabled", 1);
            }
            temp = Settings.System.getInt(cr,"flip_answer_call_switch", 1);
            if(temp != 1){
                loadSetting(stmt, AmigoSettings.FLIP_ANSWER_CALL_SWITCH, temp);
                Settings.System.putInt(cr, "flip_answer_call_switch", 1);
            }
        //}
		
        temp = Settings.System.getInt(cr,"haptic_vibration_enabled", 1);
        if(temp != 1){
            loadSetting(stmt, AmigoSettings.HAPTIC_VIBRATION_ENABLED, temp);
            Settings.System.putInt(cr, "haptic_vibration_enabled", 1);
        }
        
        temp = Settings.System.getInt(cr,"switch_vibration_enabled", 1);
        if(temp != 1){
            loadSetting(stmt, AmigoSettings.SWITCH_VIBRATION_ENABLED, temp);
            Settings.System.putInt(cr, "switch_vibration_enabled", 1);
        }
        temp = Settings.System.getInt(cr,"dialpad_vibration_enabled", 1);
        if(temp != 1){
            loadSetting(stmt, AmigoSettings.DIALPAD_VIBRATION_ENABLED, temp);
            Settings.System.putInt(cr, "dialpad_vibration_enabled", 1);
        }
        temp = Settings.System.getInt(cr,"lockscreen_vibration_enabled", 1);
        if(temp != 1){
            loadSetting(stmt, AmigoSettings.LOCKSCREEN_VIBRATION_ENABLED, temp);
            Settings.System.putInt(cr, "lockscreen_vibration_enabled", 1);
        }
        temp = Settings.System.getInt(cr,"selectapp_vibration_enabled", 1);
        if(temp != 1){
            loadSetting(stmt, AmigoSettings.SELECTAPP_VIBRATION_ENABLED, temp);
            Settings.System.putInt(cr, "selectapp_vibration_enabled", 1);
        }
    	
    	
        temp = Settings.System.getInt(cr,"ring_vibration_enabled", 1);
        if(temp != 1){
            loadSetting(stmt, AmigoSettings.RING_VIBRATION_ENABLED, temp);
            Settings.System.putInt(cr, "ring_vibration_enabled", 1);
        }
        temp = Settings.System.getInt(cr,"mms_vibration_enabled", 1);
        if(temp != 1){
            loadSetting(stmt, AmigoSettings.MMS_VIBRATION_ENABLED, temp);
            Settings.System.putInt(cr, "mms_vibration_enabled", 1);
        }
        temp = Settings.System.getInt(cr,"notification_vibration_enabled", 1);
        if(temp != 1){
            loadSetting(stmt, AmigoSettings.NOTIFICATION_VIBRATION_ENABLED, temp);
            Settings.System.putInt(cr, "notification_vibration_enabled", 1);
        }
        //if (SystemProperties.get("ro.gn.fanfanwidget.support").equals("yes")) {
            temp = Settings.System.getInt(cr,"gn_fanfan_widget_auto_push", 0);
            if(temp != 0){
                loadSetting(stmt, AmigoSettings.FANFAN_WIDGET_AUTO_PUSH, temp);
                Settings.System.putInt(cr, "gn_fanfan_widget_auto_push", 0);
            }
        //}
        //if (SystemProperties.get("ro.gn.guestmode.support").equals("yes")) {
            temp = Settings.Secure.getInt(cr,"gionee_guest_mode", 0);
            if(temp != 0){
                loadSetting(stmt, AmigoSettings.GUEST_MODE, temp);
                Settings.Secure.putInt(cr, "gionee_guest_mode", 0);
            }
            temp = Settings.Secure.getInt(cr,"gionee_first_open_guest", 0);
            if(temp != 0){
                loadSetting(stmt, AmigoSettings.FIRST_OPEN_GUEST_MODE, temp);
                Settings.Secure.putInt(cr, "gionee_first_open_guest", 0);
            }
            temp = Settings.Secure.getInt(cr,"gionee_guest_pass_enable", 0);
            if(temp != 0){
                loadSetting(stmt, AmigoSettings.GUEST_PASS_ENABLE, temp);
                Settings.Secure.putInt(cr, "gionee_guest_pass_enable", 0);
            }
            str = Settings.Secure.getString(cr,"gionee_guest_pass");
            if(str != null){
                loadSetting(stmt, AmigoSettings.GUEST_PASS, str);
                Settings.Secure.putString(cr, "gionee_guest_pass", null);
            }
        //}
    	
        temp = Settings.System.getInt(cr,"gn_button_light", 0);
        if(temp != 0){
            loadSetting(stmt, AmigoSettings.Button_Light_State, temp);
            Settings.System.putInt(cr, "gn_button_light", 0);
        }
        
        //if (SystemProperties.get("ro.gn.networkalert.support").equals("yes")) {
            temp = Settings.System.getInt(cr,"wifi_auto_notify", 0);
            if(temp != 0){
                loadSetting(stmt, AmigoSettings.WIFI_AUTO_NOTIFY, temp);
                Settings.System.putInt(cr, "wifi_auto_notify", 0);
            }
        
        //}
		
         temp = Settings.System.getInt(cr,"align_wake", 1);
         if(temp != 1){
             loadSetting(stmt, AmigoSettings.ALIGN_WAKE, temp);
             Settings.System.putInt(cr, "align_wake", 1);
         }
         
         temp = Settings.System.getInt(cr,"align_wake", 1);
         if(temp != 1){
             loadSetting(stmt, AmigoSettings.ALIGN_WAKE, temp);
             Settings.System.putInt(cr, "align_wake", 1);
         }
         
         temp = Settings.System.getInt(cr,"screen_off_timeout_back_up",30000);
         if(temp != 30000){
             loadSetting(stmt, AmigoSettings.SCREEN_OFF_TIMEOUT_BACK_UP, temp);
             Settings.System.putInt(cr, "screen_off_timeout_back_up", 30000);
         }
    	
        //if (SystemProperties.get("ro.gn.mms.alertMissMsg").equals("yes")) {
            temp = Settings.System.getInt(cr,"alert_miss_msg", 0);
            if(temp != 0){
                loadSetting(stmt, AmigoSettings.ALERT_MISS_MSG, temp);
                Settings.System.putInt(cr, "alert_miss_msg", 0);
            }
            temp = Settings.System.getInt(cr,"alert_miss_msg_interval", 5);
            if(temp != 5){
                loadSetting(stmt, AmigoSettings.ALERT_MISS_MSG_INTERVAL, temp);
                Settings.System.putInt(cr, "alert_miss_msg_interval", 5);
            }
        //}
    	
        temp = Settings.System.getInt(cr,"auto_lcm_acl", 0);
        if(temp != 0){
            loadSetting(stmt, AmigoSettings.AUTO_LCM_ACL, temp);
            Settings.System.putInt(cr, "auto_lcm_acl", 0);
        }
        
        str = Settings.System.getString(cr,"alarmring_default");
        if(str != null){
            loadSetting(stmt, AmigoSettings.ALARM_RING, str);
            Settings.System.putString(cr, "alarmring_default", null);
        }
        
        str = Settings.System.getString(cr,"ringtone2");
        if(str != null){
            loadSetting(stmt, AmigoSettings.RINGTONE2, str);
            Settings.System.putString(cr, "ringtone2", null);
        }
        
        str = Settings.System.getString(cr,"mms");
        if(str != null){
            loadSetting(stmt, AmigoSettings.MMS, str);
            Settings.System.putString(cr, "mms", null);
        }
        
        str = Settings.System.getString(cr,"mms2");
        if(str != null){
            loadSetting(stmt, AmigoSettings.MMS2, str);
            Settings.System.putString(cr, "mms2", null);
        }
        
        
        str = Settings.System.getString(cr,"theme_package_name");
        if(str != "theme" && str != null){
            loadSetting(stmt, AmigoSettings.THEME_PACKEAGE_NAME, str);
            Settings.System.putString(cr, "theme_package_name", "theme");
        }
        
        temp = Settings.System.getInt(cr,"theme_check_package", 0);
        if(temp != 0){
            loadSetting(stmt, AmigoSettings.THEME_CHECK_PACKEAGE, temp);
            Settings.System.putInt(cr, "theme_check_package", 0);
        }
        
        //if (SystemProperties.get("ro.gn.suspendbutton.support").equals("yes")) {
            temp = Settings.System.getInt(cr,"gn_suspend_button", 0);
            if(temp != 0){
                loadSetting(stmt, AmigoSettings.SUSPEND_BUTTON, temp);
                Settings.System.putInt(cr, "gn_suspend_button", 0);
            }
        //}
    	
        // if (SystemProperties.get("ro.gn.single.hand.support").equals("yes")) {
             temp = Settings.System.getInt(cr,"gn_phone_keyboard", 0);
             if(temp != 0){
                 loadSetting(stmt, AmigoSettings.PHONE_KEYBOARD, temp);
                 Settings.System.putInt(cr, "gn_phone_keyboard", 0);
             }
             temp = Settings.System.getInt(cr,"gn_input_method_keyboard", 0);
             if(temp != 0){
                 loadSetting(stmt, AmigoSettings.INPUT_METHOD_KEYBOARD, temp);
                 Settings.System.putInt(cr, "gn_input_method_keyboard", 0);
             }
             temp = Settings.System.getInt(cr,"gn_pattern_unlockscreen", 0);
             if(temp != 0){
                 loadSetting(stmt, AmigoSettings.PATTERN_UNLOCKSCREEN, temp);
                 Settings.System.putInt(cr, "gn_pattern_unlockscreen", 0);
             }
             temp = Settings.System.getInt(cr,"gn_small_screen_mode", 0);
             if(temp != 0){
                 loadSetting(stmt, AmigoSettings.SMALL_SCREEN_MODE, temp);
                 Settings.System.putInt(cr, "gn_small_screen_mode", 0);
             }
             temp = Settings.System.getInt(cr,"gn_screen_size", 0);
             if(temp != 0){
                 loadSetting(stmt, AmigoSettings.SCREEN_SIZE, temp);
                 Settings.System.putInt(cr, "gn_screen_size", 0);
             }
         //}

		
    }	
	
    private void copySettingsDB(SQLiteDatabase db) {
		Log.e(TAG,"copySettingsDB");
        SQLiteStatement stmt = null;
        try {
            stmt = db.compileStatement("REPLACE INTO config(name,value)"
                    + " VALUES(?,?);");
            startCopySettingsValues(stmt);
        } finally {
            if (stmt != null) stmt.close();
        }
    }
    //Gionee <wangguojing> <2013-10-11> add for CR00932764 end

	// Gionee <bug> <wangyaohui> <2013-10-24> modify for CR00933322 begi
	  private String getStringValueFromTable(SQLiteDatabase db, String table, String name,
            String defaultValue) {
        Cursor c = null;
        try {
            c = db.query(table, new String[] { "value" }, "name='" + name + "'",
                    null, null, null, null);
            if (c != null && c.moveToFirst()) {
                String val = c.getString(0);
                return val == null ? defaultValue : val;
            }
        } finally {
            if (c != null) c.close();
        }
        return defaultValue;
    }
	// Gionee <bug> <wangyaohui> <2013-10-24> modify for CR00933322 end

    //Gionee <wangguojing> <2014-05-12> add for CR01237681 begin
    private void updateDBValue(SQLiteDatabase db) {
        FileReader dbReader;
        final File dbConfigFile = new File(AMIGOSETTING_DB_CONFIG_FILE);
        Log.d(TAG, "updateDBValue");
        
        try {
            dbReader = new FileReader(dbConfigFile);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Can't open " + AMIGOSETTING_DB_CONFIG_FILE);
            return ;
        }
        
        SQLiteStatement stmt = null;
        try {
            stmt = db.compileStatement("REPLACE INTO config(name,value)"
                + " VALUES(?,?);");
            
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(dbReader);
                
                XmlUtils.beginDocument(parser, "amigosetting");
                
                while (true) {
                    XmlUtils.nextElement(parser);
                    
                    String name = parser.getName();
                    if (!"amigosetting".equals(name)) {
                        break;
                    }
                    
                    String amigoname = parser.getAttributeValue(null, "amigoname");
                    String amigovalue = parser.getAttributeValue(null, "amigovalue");
                    Log.w(TAG, "updateDBValue  amigoname=" + amigoname);
                    Log.w(TAG, "updateDBValue  amigovalue=" + amigovalue);
                    loadSetting(stmt, amigoname,amigovalue);
                    
                }
                
                dbReader.close();
            } catch (XmlPullParserException e) {
                Log.w(TAG, "Exception in font config parser " + e);
            } catch (IOException e) {
                Log.w(TAG, "Exception in font config parser " + e);
            } 
        } finally {
            if (stmt != null) stmt.close();
        }
    
    
    }
    //Gionee <wangguojing> <2014-05-12> add for CR01237681 end
	
    //Gionee <chenml> <2014-06-18> add for CR01272876 begin
    private void afreshDBvalue(SQLiteStatement stmt, SQLiteDatabase db) {
        int clickWakeswitch = 0;
        String switchValue = null;
        Cursor cursor = db.query(AmigoSettings.TABLE_CONFIG, null, null, null, null, null, null);
        Log.w(TAG, "afreshDBvalue cursor : " + cursor);
        try {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    switchValue = cursor.getString(cursor.getColumnIndex(AmigoSettings.NAME));
                    if (switchValue.equals(AmigoSettings.GN_SSG_SWITCH)) {
                        clickWakeswitch = cursor.getInt(cursor.getColumnIndex(AmigoSettings.VALUE));
                        break;
                    }
                }
            }
        } finally {
            cursor.close();
            Log.w(TAG, "whether cursor is closed ? cursor : "+cursor);
        }
        int defaulClickValue = mContext.getResources().getBoolean(R.bool.def_ssg_doubleclick_wake_show) ? 1 : 0;
        Log.w(TAG, "afreshDBvalue  defaulClickValue : " + defaulClickValue + " clickWakeswitch : "
                + clickWakeswitch);
        if (clickWakeswitch == 1 && defaulClickValue == 1) {
            loadBooleanSetting(stmt, AmigoSettings.SSG_DOUBLECLICK_WAKE,
                    R.bool.def_ssg_doubleclick_wake_other);
        }
    }
    //Gionee <chenml> <2014-06-18> add for CR01272876 end

    //Gionee <wangguojing> <2015-06-15> add for CR01501989 end
    private void updateIPOValue(SQLiteDatabase db) {
        FileReader dbReader;
        final File dbConfigFile = new File(AMIGOSETTING_DB_CONFIG_FILE);
        Log.d(TAG, "updateIPOValue");
        
        try {
            dbReader = new FileReader(dbConfigFile);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Can't open " + AMIGOSETTING_DB_CONFIG_FILE);
            return ;
        }
        
        SQLiteStatement stmt = null;
        try {
            stmt = db.compileStatement("REPLACE INTO config(name,value)"
                + " VALUES(?,?);");
            
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(dbReader);
                
                XmlUtils.beginDocument(parser, "amigosetting");
                
                while (true) {
                    XmlUtils.nextElement(parser);
                    
                    String name = parser.getName();
                    if (!"amigosetting".equals(name)) {
                        break;
                    }
                    String amigoname = parser.getAttributeValue(null, "amigoname");
                    if (!"is_mtk_ipo_on".equals(amigoname)) {
                        continue;
                    }
                    
                    String amigovalue = parser.getAttributeValue(null, "amigovalue");
                    Log.w(TAG, "updateIPOValue  amigoname=" + amigoname);
                    Log.w(TAG, "updateIPOValue  amigovalue=" + amigovalue);
                    loadSetting(stmt, amigoname,amigovalue);
                    
                }
                
                dbReader.close();
            } catch (XmlPullParserException e) {
                Log.w(TAG, "Exception in font config parser " + e);
            } catch (IOException e) {
                Log.w(TAG, "Exception in font config parser " + e);
            } 
        } finally {
            if (stmt != null) stmt.close();
        }
    
    
    }
    //Gionee <wangguojing> <2015-06-15> add for CR01501989 end
    
    //Gionee <wangguojing> <2015-08-28> add for CR01501989 begin
    private void updateDiagConfigValue(SQLiteDatabase db) {
        FileReader dbReader;
        final File dbConfigFile = new File(AMIGOSETTING_DB_CONFIG_FILE);
        Log.d(TAG, "updateDiagConfigValue");
        
        try {
            dbReader = new FileReader(dbConfigFile);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Can't open " + AMIGOSETTING_DB_CONFIG_FILE);
            return ;
        }
        
        SQLiteStatement stmt = null;
        try {
            stmt = db.compileStatement("REPLACE INTO config(name,value)"
                + " VALUES(?,?);");
            
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(dbReader);
                
                XmlUtils.beginDocument(parser, "amigosetting");
                
                while (true) {
                    XmlUtils.nextElement(parser);
                    
                    String name = parser.getName();
                    if (!"amigosetting".equals(name)) {
                        break;
                    }
                    String amigoname = parser.getAttributeValue(null, "amigoname");
                    if (!"wirte_diag_config".equals(amigoname)) {
                        continue;
                    }
                    
                    String amigovalue = parser.getAttributeValue(null, "amigovalue");
                    Log.w(TAG, "updateDiagConfigValue  amigoname=" + amigoname);
                    Log.w(TAG, "updateDiagConfigValue  amigovalue=" + amigovalue);
                    loadSetting(stmt, amigoname,amigovalue);
                    
                }
                
                dbReader.close();
            } catch (XmlPullParserException e) {
                Log.w(TAG, "Exception in font config parser " + e);
            } catch (IOException e) {
                Log.w(TAG, "Exception in font config parser " + e);
            } 
        } finally {
            if (stmt != null) stmt.close();
        }
    
    
    }
    //Gionee <wangguojing> <2015-08-28> add for CR01501989 end
    
    
    //Gionee <lizhipeng> <2015-08-28> add for CR01545605 begin
    private void updateConfigValue(SQLiteDatabase db) {
        FileReader dbReader;
        final File dbConfigFile = new File(AMIGOSETTING_DB_CONFIG_FILE);
        Log.d(TAG, "updateConfigValue");
        
        try {
            dbReader = new FileReader(dbConfigFile);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Can't open " + AMIGOSETTING_DB_CONFIG_FILE);
            return ;
        }
        
        SQLiteStatement stmt = null;
        try {
            stmt = db.compileStatement("REPLACE INTO config(name,value)"
                + " VALUES(?,?);");
            
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(dbReader);
                
                XmlUtils.beginDocument(parser, "amigosetting");
                
                while (true) {
                    XmlUtils.nextElement(parser);
                    
                    String name = parser.getName();
                    if (!"amigosetting".equals(name)) {
                        break;
                    }
                    String amigoname = parser.getAttributeValue(null, "amigoname");
                    if (!"8605and8609_gesture_switch".equals(amigoname)) {
                        continue;
                    }
                    
                    String amigovalue = parser.getAttributeValue(null, "amigovalue");
                    Log.w(TAG, "updateDiagConfigValue  amigoname=" + amigoname);
                    Log.w(TAG, "updateDiagConfigValue  amigovalue=" + amigovalue);
                    loadSetting(stmt, amigoname,amigovalue);
                    
                }
                
                dbReader.close();
            } catch (XmlPullParserException e) {
                Log.w(TAG, "Exception in font config parser " + e);
            } catch (IOException e) {
                Log.w(TAG, "Exception in font config parser " + e);
            } 
        } finally {
            if (stmt != null) stmt.close();
        }
    
    
    }
    //Gionee <lizhipeng> <2015-08-28> add for CR01545605 end
    //Gionee <wangguojing> <2015-08-29> add for CR01545815 begin
    private void updateGlovePatternsSwitchValue(SQLiteDatabase db) {
        FileReader dbReader;
        final File dbConfigFile = new File(AMIGOSETTING_DB_CONFIG_FILE);
        Log.d(TAG, "updateGlovePatternsSwitchValue");
        
        try {
            dbReader = new FileReader(dbConfigFile);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Can't open " + AMIGOSETTING_DB_CONFIG_FILE);
            return ;
        }
        
        SQLiteStatement stmt = null;
        try {
            stmt = db.compileStatement("REPLACE INTO config(name,value)"
                + " VALUES(?,?);");
            
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(dbReader);
                
                XmlUtils.beginDocument(parser, "amigosetting");
                
                while (true) {
                    XmlUtils.nextElement(parser);
                    
                    String name = parser.getName();
                    if (!"amigosetting".equals(name)) {
                        break;
                    }
                    String amigoname = parser.getAttributeValue(null, "amigoname");
                    if (!"is_glove_patterns_on".equals(amigoname)) {
                        continue;
                    }
                    
                    String amigovalue = parser.getAttributeValue(null, "amigovalue");
                    Log.w(TAG, "updateGlovePatternsSwitchValue  amigoname=" + amigoname);
                    Log.w(TAG, "updateGlovePatternsSwitchValue  amigovalue=" + amigovalue);
                    loadSetting(stmt, amigoname,amigovalue);
                    
                }
                
                dbReader.close();
            } catch (XmlPullParserException e) {
                Log.w(TAG, "Exception in font config parser " + e);
            } catch (IOException e) {
                Log.w(TAG, "Exception in font config parser " + e);
            } 
        } finally {
            if (stmt != null) stmt.close();
        }
    
    
    }
    //Gionee <wangguojing> <2015-08-29> add for CR01545815 end    
    
    
  //Gionee <lizhipeng> <2015-09-06> add for CR01549142 begin
    private void updateConfigValue(SQLiteDatabase db,String AmigoName) {
        FileReader dbReader;
        final File dbConfigFile = new File(AMIGOSETTING_DB_CONFIG_FILE);
        Log.d(TAG, "updateConfigValue");
        
        try {
            dbReader = new FileReader(dbConfigFile);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Can't open " + AMIGOSETTING_DB_CONFIG_FILE);
            return ;
        }
        
        SQLiteStatement stmt = null;
        try {
            stmt = db.compileStatement("REPLACE INTO config(name,value)"
                + " VALUES(?,?);");
            
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(dbReader);
                
                XmlUtils.beginDocument(parser, "amigosetting");
                
                while (true) {
                    XmlUtils.nextElement(parser);
                    
                    String name = parser.getName();
                    if (!"amigosetting".equals(name)) {
                        break;
                    }
                    String amigoname = parser.getAttributeValue(null, "amigoname");
                    if (!AmigoName.equals(amigoname)) {
                        continue;
                    }
                    String amigovalue = parser.getAttributeValue(null, "amigovalue");
                    Log.w(TAG, "updateDiagConfigValue  amigoname=" + amigoname);
                    Log.w(TAG, "updateDiagConfigValue  amigovalue=" + amigovalue);
                    loadSetting(stmt, amigoname,amigovalue);
                    
                }
                
                dbReader.close();
            } catch (XmlPullParserException e) {
                Log.w(TAG, "Exception in font config parser " + e);
            } catch (IOException e) {
                Log.w(TAG, "Exception in font config parser " + e);
            } 
        } finally {
            if (stmt != null) stmt.close();
        }
    
    
    }
    //Gionee <lizhipeng> <2015-09-06> add for CR01549142 end
    //Gionee <lizhipeng> <2015-09-11> add for CR01549480 begin
    public void updateLcmAclValue(){
    	ContentResolver cr = mContext.getContentResolver();
    	if(AmigoSettings.getInt(cr,"8605and8609_gesture_switch", 0) == 1){
    		boolean mcheck=(AmigoSettings.getInt(cr,
                    "adjust_screen_tone"/*AmigoSettings.ADJUST_SCREEN_TONE*/, 1) == 1);
       	 	AmigoSettings.putInt(cr,AmigoSettings.AUTO_LCM_ACL,mcheck ? 1: 0);
    	}
    }
    //Gionee <lizhipeng> <2015-09-11> add for CR01549480 end
    //Gionee <wangguojing> <2015-10-21> add for CR01572592 begin
    private void updateDBValueForOTA(SQLiteDatabase db) {
        FileReader dbReader;
        final File dbConfigFile = new File(AMIGOSETTING_DB_CONFIG_FILE);
        Log.d(TAG, "updateDBValueForOTA");
        
        try {
            dbReader = new FileReader(dbConfigFile);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Can't open " + AMIGOSETTING_DB_CONFIG_FILE);
            return ;
        }
        
        SQLiteStatement stmt = null;
        try {
            stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
            
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(dbReader);
                
                XmlUtils.beginDocument(parser, "amigosetting");
                
                while (true) {
                    XmlUtils.nextElement(parser);
                    
                    String name = parser.getName();
                    if (!"amigosetting".equals(name)) {
                        break;
                    }
                    
                    String amigoname = parser.getAttributeValue(null, "amigoname");
                    String amigovalue = parser.getAttributeValue(null, "amigovalue");
                    Log.w(TAG, "updateDBValueForOTA  amigoname=" + amigoname);
                    Log.w(TAG, "updateDBValueForOTA  amigovalue=" + amigovalue);
                    loadSetting(stmt, amigoname,amigovalue);
                    
                }
                
                dbReader.close();
            } catch (XmlPullParserException e) {
                Log.w(TAG, "Exception in font config parser " + e);
            } catch (IOException e) {
                Log.w(TAG, "Exception in font config parser " + e);
            } 
        } finally {
            if (stmt != null) stmt.close();
        }
    
    
    }
    //Gionee <wangguojing> <2015-10-21> add for CR01572592 end

}
