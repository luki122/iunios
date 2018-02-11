package amigo.provider;

import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.content.IContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
//Gionee <liuyb> <2013-12-03> add for CR00966303 begin
import java.lang.reflect.Method;
//Gionee <liuyb> <2013-12-03> add for CR00966303 end

public class AmigoSettings {
    private static final String TAG = "AmigoSettings";

    public static final String AMIGO_PROP_SETTING_VERSION = "sys.settings_amigo_version";
    public static final String AUTHORITY = "amigosettings";
    public static final String TABLE_CONFIG = "config";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_CONFIG);
    
    private static Object sLock = new Object();
    
    private static IContentProvider mAmigoContentProvider = null;
    public static final String CALL_METHOD_GET_CONFIG = "GET_config";
    public static final String CALL_METHOD_SET_CONFIG = "SET_config";
    public static final String NAME = "name";
    public static final String VALUE = "value";
    
    //Gionee fengjianyi 2012-08-22 add for CR00673800 start
    public static final String FLIP_SOUNDS_ENABLED = "gn_flip_sounds_enabled";
    public static final String FLIP_ON_SOUND = "gn_flip_on_sound";
    public static final String FLIP_OFF_SOUND = "gn_flip off_sound";
    //Gionee fengjianyi 2012-08-22 add for CR00673800 end
    
    //Gionee:zhang_xin 2012-12-09 add for start
    public static final String POWER_SAVER = "gn_power_saver";
    //Gionee:zhang_xin 2012-12-09 add for end
    //Gionee <zhang_xin><2013-03-26> add for CR00788411 begin
    public static final int FONT_SIZE_SYSTEM = 0;
    public static final int FONT_SIZE_LARGE = 1;
    public static final int FONT_SIZE_EXTRA_LARGE = 2;
    //Gionee <zhang_xin><2013-03-26> add for CR00788411 end
    
    //Gionee <zhang_xin><2013-03-26> add for CR00788411 begin          
    public static final String FONT_SIZE = "gn_font_size";
    //Gionee <zhang_xin><2013-03-26> add for CR00788411 end
    
    //gionee zengxuanhui 20121022 add for CR00716758 begin     
    public static final String HAPTIC_VIBRATION_ENABLED = "haptic_vibration_enabled";
    //gionee zengxuanhui 20121022 add for CR00716758 end
    
    //Gionee Jingjc 20121122 modify for CR00722601 begin
    public static final String SWITCH_VIBRATION_ENABLED = "switch_vibration_enabled";
    public static final String DIALPAD_VIBRATION_ENABLED = "dialpad_vibration_enabled";
    public static final String LOCKSCREEN_VIBRATION_ENABLED = "lockscreen_vibration_enabled";
    public static final String SELECTAPP_VIBRATION_ENABLED = "selectapp_vibration_enabled";
    //Gionee Jingjc 20121122 modify for CR00722601 end
    
    // Gionee <zengxuanhui> <2013-04-26> add for CR00797390 begin
    public static final String RING_VIBRATION_ENABLED = "ring_vibration_enabled";
    public static final String MMS_VIBRATION_ENABLED = "mms_vibration_enabled";
    public static final String NOTIFICATION_VIBRATION_ENABLED = "notification_vibration_enabled";
    // Gionee <zengxuanhui> <2013-04-26> add for CR00797390 end
    
    // Gionee <wangyaohui><2013-05-30> add for CR00820909 begin 
    public static final String FANFAN_WIDGET_AUTO_PUSH = "gn_fanfan_widget_auto_push";           
    // Gionee <wangyaohui><2013-05-30> add for CR00820909 end
    
    // Gionee <wangyaohui><2013-06-05> add for CR00823496 begin 
    public static final String RESPIRATION_LAMP_LOW_POWER = "gn_respirationlamp_low_power";
    public static final String RESPIRATION_LAMP_IN_CHARGE = "gn_respirationlamp_in_charge";
    public static final String RESPIRATION_LAMP_NOTIFICATION = "gn_respirationlamp_notification"; 
    public static final String RESPIRATION_LAMP_MUSIC = "gn_respirationlamp_music";
    public static final String RESPIRATION_LAMP_CALL = "gn_respirationlamp_call";                 
    // Gionee <wangyaohui><2013-06-05> add for CR00823496 end

    //Gionee <zengxh><2013-06-21> add for CR00828066 begin
    /**
    * @hide
    */		
    public static final String SSG_AUTO_DIAL = "ssg_auto_dial";
    public static final String SSG_CALL_ACCESS = "ssg_call_access";
    public static final String SSG_DELAY_ALARM = "ssg_delay_alarm";
    public static final String SSG_SWITCH_SCREEN = "ssg_switch_screen";
    public static final String SDG_CALL_ACCESS = "sdg_call_access";
    public static final String SDG_BROWSE_PHOTOS = "sdg_browse_photos";
    public static final String SDG_VIDEO_PROGRESS = "sdg_video_progress";
    public static final String SDG_VIDEO_VOLUME = "sdg_video_volume";
    public static final String SDG_VIDEO_PAUSE = "sdg_video_pause";
    //Gionee <zengxh><2013-06-21> add for CR00828066 end
    
    //Gionee <chenml><2013-05-20> add for CR00817466  begin    
    public static final String SOUND_CONTROL_SWITCH = "sound_control_switch";
    public static final String SOUND_CONTROL_CALLING= "sound_control_calling";
    public static final String SOUND_CONTROL_MESSAGE= "sound_control_message";
    public static final String SOUND_CONTROL_LOCKSCREEN= "sound_control_lockscreen";
    public static final String SOUND_CONTROL_ALARMCLOCK= "sound_control_alarmclock";
    //Gionee <chenml><2013-05-20> add for CR00817466 end  

    //Gionee <chenml><2013-05-30> add for CR00821135  begin        
    public static final String SUSPEND_BUTTON = "suspend_button";
    public static final String PHONE_KEYBOARD = "phone_keyboard";
    public static final String INPUT_METHOD_KEYBOARD = "input_method_keyboard";
    public static final String PATTERN_UNLOCKSCREEN = "pattern_unlockscreen";
    public static final String SMALL_SCREEN_MODE = "small_screen_mode";
    public static final String SCREEN_SIZE = "screen_size";
    public static final String PHONE_KEYBOARD_PLACE = "phone_keyboard_place";
    //Gionee <chenml><2013-05-30> add for CR00821135 end
    
    // Gionee <liuyb> <2013-06-08> add for CR00824683 begin
    /**
     * @hide
     */
    public static final String ALIGN_WAKE = "align_wake";
    /**
     * @hide
     */
    public static final int ALIGN_WAKE_ON = 1;
    /**
     * @hide
     */
    public static final int ALIGN_WAKE_OFF = 0;
    /**
     * @hide
     */
    public static final int ALIGN_WAKE_DEFAULT = 1;
    // Gionee <liuyb> <2013-06-08> add for CR00824683 end     

    //Gionee <wangguojing><2013-07-27> add for CR00844271 begin
    public static final String GN_SSG_SWITCH = "ssg_switch";
    public static final String GN_DG_SWITCH = "dg_switch";
    public static final String SSG_AUTO_DIAL_SHOW = "ssg_auto_dial_show";
    public static final String SSG_CALL_ACCESS_SHOW = "ssg_call_access_show";
    public static final String SSG_DELAY_ALARM_SHOW = "ssg_delay_alarm_show";
    public static final String SSG_SWITCH_SCREEN_SHOW = "ssg_switch_screen_show";
    public static final String SDG_CALL_ACCESS_SHOW = "sdg_call_access_show";
    public static final String SDG_BROWSE_PHOTOS_SHOW = "sdg_browse_photos_show";
    public static final String SDG_VIDEO_PROGRESS_SHOW = "sdg_video_progress_show";
    public static final String SDG_VIDEO_VOLUME_SHOW = "sdg_video_volume_show";
    public static final String SDG_VIDEO_PAUSE_SHOW = "sdg_video_pause_show";

    public static final String SDG_DEL_PHOTOS_SHOW = "sdg_del_photos_show";
    public static final String SDG_DEL_PHOTOS = "sdg_del_photos";
	
    public static final String SDG_TASK_CONTROL_SHOW = "sdg_task_control_show";
    public static final String SDG_TASK_CONTROL = "sdg_task_control";
	
    public static final String SSG_DOUBLECLICK_WAKE_SHOW = "ssg_doubleclick_wake_show";
    public static final String SSG_DOUBLECLICK_WAKE = "ssg_doubleclick_wake";
    //Gionee <wangguojing><2013-07-27> add for CR00844271 end
    
    // Gionee fengjianyi 2012-09-28 add for CR00705430 start
    public static final String GUEST_MODE = "gionee_guest_mode";
    // Gionee fengjianyi 2012-09-28 add for CR00705430 end
    
    // Gionee <qiuxd> <2013-06-14>	add for CR00826405 start
    public static final String FIRST_OPEN_GUEST_MODE = "gionee_first_open_guest";
    // Gionee <qiuxd> <2013-06-14>	add for CR00826405 end
    
    // Gionee <qiuxd> <2013-04-26> modify for CR00802506 start
    public static final String GUEST_PASS_ENABLE = "gionee_guest_pass_enable";
    public static final String GUEST_PASS = "gionee_guest_pass";
    // Gionee <qiuxd> <2013-04-26> modify for CR00802506 end

	
    //Gionee <wangguojing><2013-08-23> add for CR00868463 begin
    /**
     * Voice call and sms setting as always ask
     * 
     * @hide
     */    
    public static final long DEFAULT_SIM_SETTING_ALWAYS_ASK = -1;
    //Gionee <wangguojing><2013-08-23> add for CR00868463 end
    //Gionee fengjianyi 2012-08-22 add for CR00673800 start

    public static final String FLIP_ANSWER_CALL_SWITCH = "flip_answer_call_switch";

    public static final int FLIP_ANSWER_CALL_UNABLE = 0x1;

    public static final int FLIP_ANSWER_CALL_ENABLE = 0x2;
			
    public static final int FLIP_ANSWER_CALL_DEFAULT = FLIP_ANSWER_CALL_UNABLE;
    //Gionee fengjianyi 2012-08-22 add for CR00673800 end
    ////Gionee: <guozj><2013-5-4> add for CR00808118 begin
    public static final String FLIP_HANGUP_CALL_SWITCH = "flip_answer_call_switch";
    public static final int FLIP_HANGUP_CALL_UNABLE = 0x1;
    public static final int FLIP_HANGUP_CALL_ENABLE = 0x2;
    public static final int FLIP_HANGUP_CALL_DEFAULT = FLIP_ANSWER_CALL_UNABLE;
    ////Gionee: <guozj><2013-5-4> add for CR00808118 end
	
    //Gionee <chenml><2013-04-10> add for CR00795467 begin
    public static final String Button_Light_State= "gn_button_light";
    //Gionee <chenml><2013-04-10> add for CR00795467 end

    // Gionee liuyanbo 2012-08-23 add for CR00680327 start
    public static final String WIFI_AUTO_NOTIFY = "wifi_auto_notify";

    public static final int WIFI_AUTO_NOTIFY_ON = 1;

    public static final int WIFI_AUTO_NOTIFY_OFF = 0;
    // Gionee liuyanbo 2012-08-23 add for CR00680327 end
	
    // Gionee: 20120929 tianliang modify for CR00696821 begin
    public static final String SCREEN_OFF_TIMEOUT_BACK_UP = "screen_off_timeout_back_up";
    // Gionee: 20120929 tianliang modify for CR00696821 end 
	
    // Gionee: 20120918 chenrui add for CR00696600 begin
    public static String ALERT_MISS_MSG = "alert_miss_msg";
    public static String ALERT_MISS_MSG_INTERVAL = "alert_miss_msg_interval";
    // Gionee: 20120918 chenrui add for CR00696600 end
    //Gionee Jingjc 2012-09-16 add for CR00693470 start
    public static final String AUTO_LCM_ACL = "auto_lcm_acl";
    //Gionee Jingjc 2012-09-16 add for CR00693470 end
    //Gionee zengxuanhui 20120807 modify for CR00672106 begin
	
    public static final String RINGTONE2 = "ringtone2";

    public static final String MMS = "mms";

    public static final String MMS2 = "mms2";
	
	
    public static final Uri DEFAULT_RINGTONE2_URI = getUriFor(RINGTONE2);
	

    public static final Uri DEFAULT_MMS_URI = getUriFor(MMS);
	

    public static final Uri DEFAULT_MMS2_URI = getUriFor(MMS2);
    //Gionee zengxuanhui 20120807 modify for CR00672106 end
	
    // Gionee baorui 2012-08-29 modify for CR00673437 begin
    public static final String ALARM_RING = "alarmring_default";
    // Gionee baorui 2012-08-29 modify for CR00673437 end	
	
    // Gionee huming 2012/03/27 add for CR00556892 begin
    public static final String THEME_PACKEAGE_NAME = "theme_package_name";
    // Gionee huming 2012/03/27 add for CR00556892 end

    // Gionee chenql 2012-08-16 add for CR00672927 begin
    public static final String THEME_CHECK_PACKEAGE = "theme_check_package"; 
    // Gionee chenql 2012-08-16 add for CR00672927 end 	
    
    //Gionee guoyx 20130223 add for Qualcomm solution CR00773050 begin
    /**
    * Subscription to be used for data call on a multi sim device. The supported values
    * are 0 = SUB1, 1 = SUB2.
    * @hide
    */
    public static String MULTI_SIM_DATA_CALL_SUBSCRIPTION = "multi_sim_data_call";
    //Gionee guoyx 20130223 add for Qualcomm solution CR00773050 end

    //Gionee <chenml> <2013-09-25> add for CR00906622 begin
    public static final String SOUND_CONTROL_OPEN_REMIND = "sound_control_open_remind";
    public static final String SOUND_CONTROL_CLOSE_REMIND = "sound_control_close_remind";
    //Gionee <chenml> <2013-09-25> add for CR00906622 end

	//Gionee <wangguojing> <2013-10-11> add for GPS Optimization begin
    public static final String GPS_PROMPT_REMIND = "gps_prompt_remind";	
	//Gionee <wangguojing> <2013-10-11> add for GPS Optimization end

    //Gionee <wangguojing> <2013-10-24> add for CR00933390 begin
    public static final String VIRTUAL_DISPLAY_DEVICES = "virtual_display_devices";	
	
    public static final String LCD_EFFECT_MODE = "lcd_effect_adjust";	
	
    public static final String GLOVE_PATTERNS = "glove_patterns";	
    //Gionee <wangguojing> <2013-10-24> add for CR00933390 end

    //Gionee <wangguojing> <2013-11-02> add for CR00943428 begin
    public static final String VOICE_WAKE_SWITCH = "voice_wake_switch";	
    public static final String VOICE_WAKE_REMIND = "voice_wake_remind";	    
    public static final String VOICE_WAKE_WORD = "voice_wake_word";	    
    //Gionee <wangguojing> <2013-11-02> add for CR00943428 end
	
    //Gionee <wangguojing> <2013-11-04> add for CR00944538 begin
    public static final String SHOW_POWERSAVE_DIALOG = "show_powersave_dialog";	
    //Gionee <wangguojing> <2013-11-04> add for CR00944538 end
	

    /**
    * author:huming
    * mail:hum@gionee.com 
    * date:2011/08/15 14:37
    * description:the vars are ralative with System Color
    *@hide
    */
    public static final String CFG_SYSTEM_COLOR = "config_system_color";
	
    //white background and black fount
    /**
    * @hide
    */			
    public static final int SYSTEM_COLOR_WHITE_BG_BLACK_FG = 2;

    // Gionee <bug> <wangyaohui> <2013-10-24> modify for CR00933322 begin
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
	
    //Gionee <wangguojing> <2013-12-23> add for CR00989512 begin
    public static final String INTERNATIONAL_ROAMING_SWITCH = "international_roaming_switch";	
    //Gionee <wangguojing> <2013-12-23> add for CR00989512 end

    //Gionee <wangguojing> <2013-12-26> add for CR00992408 begin
    public static final String LCD_COLOR_VALUE = "lcd_color_value";	
    //Gionee <wangguojing> <2013-12-26> add for CR00992408 end
    //Gionee <wangguojing> <2013-12-27> add for CR00993259 begin
    public static final String ROTATE_CAMERA_OPEN_APP = "rotate_camera_open_app";	
    public static final String USER_EXPERIENCE = "user_experience";	
    //Gionee <wangguojing> <2013-12-27> add for CR00993259 end

    //Gionee <wangguojing> <2013-12-30> add for CR00997383 begin
    public static final String DIAl_SOUND_TYPE = "dial_sound_type";	
    //Gionee <wangguojing> <2013-12-30> add for CR00997383 end

    //Gionee <huangyc> <2014-03-06> add for CR01087530 begin
    public static final String NOTIFICATION_DISABLE_REMINDER = "notification_disable_reminder";
    public static final String NETWORK_SPEED_DISPLAY = "network_speed_display";
    //Gionee <huangyc> <2014-03-06> add for CR01087530 end

    //  Gionee <chenml> <2014-05-04> add for CR01215994 begin
    public static final String SILENT_MODE_ENABLED = "silent_mode_enabled";
    public static final String LAST_MUSIC_VOLUME = "last_music_volume";
    public static final String VOLUME_MUSIC = "volume_music";
    //  Gionee <chenml> <2014-05-04> add for CR01215994 end

    //Gionee <wangguojing> <2014-05-29> add for CR01272670 begin
    public static final String ADJUST_SCREEN_TONE = "adjust_screen_tone";
    public static final String USER_EXPERIENCE_REMIND = "user_experience_remind";
    //Gionee <wangguojing> <2014-05-29> add for CR01272670 end
    
	//Gionee <wangguojing> <2014-12-01> modify for CR01418765 begin
    public static final String AAL_CABC_BACKUP = "aal_cabc_backup";
	
    public static final String KEY_RINGTONE_DATA = "key_ringtone_data";
    public static final String KEY_RINGTONE2_DATA = "key_ringtone2_data";
    public static final String KEY_NOTIFICATION_DATA = "key_notification_data";
    public static final String KEY_MMS_DATA = "key_mms_data";
    public static final String KEY_MMS2_DATA = "key_mms2_data";
    public static final String KEY_VIDEO_DATA = "key_video_data";

	
    public static final String IS_NEW_SSG = "is_new_ssg";
    public static final String IS_PSENSOR_BROWSE_PICTURE_DG = "is_psensor_browse_picture_dg";
    public static final String IS_PSENSOR_CTRL_VIDEO_DG = "is_psensor_ctrl_video_dg";	
    public static final String SDG_BROWSE_PHOTOS_PSENSOR = "sdg_browse_photos_psensor";
    public static final String SDG_VIDEO_PAUSE_PSENSOR = "sdg_video_pause_psensor";


    public static final String IS_VIBRATION_ALERT_ON = "is_vibration_alert_on";
    public static final String SSG_VIBRATION_ALERT = "ssg_vibration_alert";
	
    public static final String VIRTUAL_KEY_VALUE = "virtual_key_value";
	

    public static final String BLACK_GESTURE_E = "black_gesture_e";
    public static final String BLACK_GESTURE_U_RIGHT = "black_gesture_u_right";
    public static final String BLACK_GESTURE_M = "black_gesture_m";
    public static final String BLACK_GESTURE_TRIANGULAR = "black_gesture_triangular";
    public static final String BLACK_GESTURE_UP_SLIDE = "black_gesture_up_slide";
    public static final String BLACK_GESTURE_DOWN_SLIDE = "black_gesture_down_slide";
    public static final String BLACK_GESTURE_W = "black_gesture_w";
    public static final String BLACK_GESTURE_Z = "black_gesture_z";
    public static final String BLACK_GESTURE_O = "black_gesture_o";
    public static final String BLACK_GESTURE_S = "black_gesture_s";
    public static final String BLACK_GESTURE_U_LEFT = "black_gesture_u_left";
    public static final String BLACK_GESTURE_U_UP = "black_gesture_u_up";
    public static final String BLACK_GESTURE_U_DOWN = "black_gesture_u_down";
    public static final String BLACK_GESTURE_LEFT_SLIDE = "black_gesture_left_slide";
    public static final String BLACK_GESTURE_RIGHT_SLIDE = "black_gesture_right_slide";


    public static final String SDG_LIGHT_SCREEN = "sdg_light_screen";
    public static final String IS_SDG_LIGHT_SCREEN_ON = "is_sdg_light_screen_on";
	
    public static final String SDG_UNLOCK = "sdg_unlock";
    public static final String IS_SDG_UNLOCK_ON = "is_sdg_unlock_on";
	
    public static final String SDG_DESKTOP_SLIDE = "sdg_desktop_slide";
    public static final String IS_SDG_DESKTOP_SLIDE_ON = "is_sdg_desktop_slide_on";

	public static final String AMIGO_SILENT_SWITCH = "amigo_silent_switch";
	public static final String AMIGO_VIBRATION_SWITCH = "amigo_vibration_switch";
	
	public static final String LOCKSCREEN_ACCESS = "lockscreen_access";
	public static final String APPLICATIONS_ACCESS = "applications_access";

	//Gionee <wangguojing> <2015-2-11> modify for CR01448325 begin
	public static final String SSG_SMART_LIGHT_SCREEN = "ssg_smart_light_screen";
	public static final String IS_SMART_LIGHT_SCREEN_ON = "is_smart_light_screen_on";

	public static final String BLACK_GESTURE_CONFIG_VALUE = "black_gesture_config_value";
	public static final String TIMING_ZEN_MODE = "timing_zen_mode";
	public static final String ZEN_MODE_DAYS = "zen_mode_days";
	public static final String ZEN_MODE_CONDITION_INDEX = "zen_mode_condition_index";
	public static final String CONTROL_CENTER_SWITCH = "control_center_switch";

	public static final String SKYLIGHT_SWITCH = "skylight_switch";
	public static final String SDG_SWITCH_MUSIC = "sdg_switch_music";
	public static final String IS_SDG_SWITCH_MUSIC_ON = "is_sdg_switch_music_on";
	public static final String SDG_READER = "sdg_reader";
	public static final String IS_SDG_READER_ON = "is_sdg_reader_on";
	//Gionee <wangguojing> <2015-2-11> modify for CR01448325 end

	//Gionee <wangguojing> <2014-12-01> modify for CR01418765 end
		
    private static IContentProvider lazyGetProvider(ContentResolver cr) {
        IContentProvider cp = null;
        synchronized (sLock) {
            cp = mAmigoContentProvider;
            if (cp == null) {
                cp = mAmigoContentProvider = cr.acquireProvider(CONTENT_URI.getAuthority());
            }
        }
        return cp;
    }

    public static int getInt(ContentResolver cr, String name, int defaultValue) {
        String value = getStringValueFromTable(cr, AmigoSettings.TABLE_CONFIG, name, null);
        return (value != null) ? Integer.parseInt(value) : defaultValue;
    }
    
    public static String getString(ContentResolver cr, String name, String defaultValue) {
        return getStringValueFromTable(cr, AmigoSettings.TABLE_CONFIG, name, defaultValue);
    }
    //Gionee <wangguojing> <2013-11-01> add for CR00943056 begin
    public static String getString(ContentResolver cr, String name) {
        return getStringValueFromTable(cr, AmigoSettings.TABLE_CONFIG, name, null);
    }
    //Gionee <wangguojing> <2013-11-011> add for CR00943056 end

    private static String getStringValueFromTable(ContentResolver cr, String table, String name, String defaultValue) {
        try {
            Log.d(TAG, "AmigoSettings get string name = " + name + " , defaultValue = " + defaultValue);

            IContentProvider cp = lazyGetProvider(cr);
            //Gionee <liuyb> <2013-12-03> modify for CR00966303 begin
            Bundle b = null;
            try {
                //b = cp.call(CALL_METHOD_GET_CONFIG, name, null);
                // android4.3
                b = invokeCallMethod(cr, cp,CALL_METHOD_GET_CONFIG, name, null);
            } catch (NoSuchMethodError e) {
            }
            //Gionee <liuyb> <2013-12-03> modify for CR00943056 end
            if (b != null) {
                String value = b.getString(AmigoSettings.VALUE);
                Log.d(TAG, "AmigoSettings get string name = " + name + " , value = " + value);
                return value;
            }
        } catch (Exception e) {
            Log.e(TAG, "Can't get name " + name, e);
            return defaultValue;
        }
        return defaultValue;
    }
    
    public static boolean putInt(ContentResolver cr, String name, int value) {
        return putString(cr, name, Integer.toString(value));
    }
    
    public static boolean putString(ContentResolver cr, String name, String value) {
        try {
            Log.d(TAG, "AmigoSettings put string name = " + name + " , value = " + value);
            
            Bundle arg = new Bundle();
            arg.putString(AmigoSettings.VALUE, value);
            IContentProvider cp = lazyGetProvider(cr);
            //Gionee <liuyb> <2013-12-03> modify for CR00966303 begin
            Bundle result = null;
            try {
                //result = cp.call(CALL_METHOD_SET_CONFIG, name, arg);
                // android4.3
                result = invokeCallMethod(cr, cp,CALL_METHOD_SET_CONFIG, name, arg);
            } catch (NoSuchMethodError e) {
            }
            //Gionee <liuyb> <2013-12-03> modify for CR00966303 end
            return result.getBoolean(AmigoSettings.VALUE, true);
        } catch (Exception e) {
            Log.e(TAG, "Can't set key " + name, e);
            return false;
        }
    }
    
    public static Uri getUriFor(String name) {
        return getUriFor(CONTENT_URI, name);
    }
    
    public static Uri getUriFor(Uri uri, String name) {
        return Uri.withAppendedPath(uri, name);
    }
    
    //Gionee <liuyb> <2013-12-03> add for CR00966303 begin
    public static Bundle invokeCallMethod(ContentResolver cr, IContentProvider cp,  String methodtype, String name, Bundle extras) {
        Bundle result = null;
        try {
            Class<ContentResolver> crClass = ContentResolver.class;
            Method method = crClass.getDeclaredMethod("getPackageName");
            String packageName = (String) method.invoke(cr);

            Class<IContentProvider> cpClass = IContentProvider.class;
            method = cpClass
                    .getDeclaredMethod("call", new Class[] {String.class,String.class, String.class, Bundle.class});
            result = (Bundle) method.invoke(cp,
                    new Object[] {packageName, methodtype, name, extras});
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
    //Gionee <liuyb> <2013-12-03> add for CR00966303 end
}
