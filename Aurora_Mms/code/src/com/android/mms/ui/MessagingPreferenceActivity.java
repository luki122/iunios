/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
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

package com.android.mms.ui;

import java.util.List;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.R;
// Aurora liugj 2013-09-17 modified for aurora's new feature start
//import android.app.ActionBar;
// Aurora liugj 2013-09-17 modified for aurora's new feature end
import aurora.app.AuroraAlertDialog;
import android.app.Dialog;
// Aurora xuyong 2014-08-04 added for sms center feature start
import android.content.BroadcastReceiver;
// Aurora xuyong 2014-08-04 added for sms center feature end
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
// Aurora xuyong 2014-08-04 added for sms center feature start
import android.content.IntentFilter;
// Aurora xuyong 2014-08-04 added for sms center feature end
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
// Aurora liugj 2013-10-12 modified for change AuroraCheckBoxPreference to AuroraSwitchPreference start
import aurora.preference.AuroraSwitchPreference;
// Aurora liugj 2013-10-12 modified for change AuroraCheckBoxPreference to AuroraSwitchPreference end
import aurora.preference.AuroraListPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraPreferenceManager;
import aurora.preference.AuroraPreferenceScreen;
import android.provider.SearchRecentSuggestions;
import android.text.InputFilter;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import aurora.widget.AuroraEditText;
import android.widget.Toast;
//gionee gaoj 2013-2-19 adde for CR00771935 start
import com.android.mms.data.Contact;
//gionee gaoj 2013-2-19 adde for CR00771935 end
import com.android.mms.data.WorkingMessage;
import com.android.mms.util.Recycler;
import com.aurora.featureoption.FeatureOption;
import com.gionee.internal.telephony.GnPhone;
import gionee.provider.GnTelephony.SIMInfo;
import com.gionee.internal.telephony.GnTelephonyManagerEx;
import android.os.Handler;
import android.view.inputmethod.EditorInfo;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import android.os.SystemProperties;

import android.database.sqlite.SqliteWrapper;
import aurora.app.AuroraProgressDialog;
import aurora.widget.AuroraActionBar;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import gionee.provider.GnTelephony.Sms;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteDiskIOException;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Message;
// Gionee zhangxx 2012-04-02 add for CR00556294 begin
import java.lang.reflect.Field;
import android.os.storage.StorageManager;
// Gionee zhangxx 2012-04-02 add for CR00556294 end

//gionee gaoj 2012-4-10 added for CR00555790 start
import com.android.mms.ui.NumberPickerDialog;
import aurora.app.AuroraProgressDialog;
import android.inputmethodservice.Keyboard.Key;
import android.provider.Settings;
import com.android.mms.data.Conversation;
import com.android.internal.widget.LockPatternUtils;
// Aurora liugj 2013-09-25 modified for aurora's new feature start
import com.aurora.mms.ui.AuroraManageSimMessages;
// Aurora xuyong 2014-08-04 added for sms center feature start
import com.aurora.mms.ui.AuroraSmsCenterPreference;
// Aurora xuyong 2014-08-04 added for sms center feature end
// Aurora liugj 2013-09-25 modified for aurora's new feature end
// Aurora xuyong 2014-05-31 added for multisim feature start
import com.aurora.mms.ui.AuroraMultiSimManageActivity;
// Aurora xuyong 2014-05-31 added for multisim feature end
import android.app.admin.DevicePolicyManager;

import com.gionee.mms.ui.ConvFragment;
import com.gionee.mms.ui.MmsRingtonePreference;
import com.gionee.mms.ui.SecuritySettingActivity;
import com.gionee.mms.ui.MsgChooseLockPassword;
// Aurora xuyong 2014-08-12 added for bug #7474 start
import gionee.telephony.GnTelephonyManager;
// Aurora xuyong 2014-08-12 added for bug #7474 end
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
//gionee gaoj 2012-4-10 added for CR00555790 end
// gionee zhouyj 2012-05-18 add for CR00601523 start
import aurora.app.AuroraActivity;
import android.graphics.Color;
import android.content.SharedPreferences.Editor;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
// Aurora liugj 2013-09-13 modified for aurora's new feature start
import android.app.ActionBar;
// Aurora liugj 2013-09-13 modified for aurora's new feature end
// gionee zhouyj 2012-05-18 add for CR00601523 end

//gionee gaoj 2012-8-5 added for CR00663940 start
import android.content.AsyncQueryHandler;
//gionee gaoj 2012-8-5 added for CR00663940 end
// Aurora xuyong 2014-08-04 added for sms center feature start
import android.telephony.TelephonyManager;
import com.android.internal.telephony.TelephonyIntents;
import gionee.provider.GnTelephony.SimInfo;
// Aurora xuyong 2014-08-04 added for sms center feature end
//gionee zengxuanhui 20120815 add for CR00673549 begin
// Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
//import com.mediatek.audioprofile.AudioProfileManager;
// Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
//gionee zengxuanhui 20120815 add for CR00673549 end
/**
 * With this activity, users can set preferences for MMS and SMS and
 * can access and manipulate SMS messages stored on the SIM.
 */
// Aurora liugj 2013-10-12 modified for change AuroraCheckBoxPreference to AuroraSwitchPreference
public class MessagingPreferenceActivity extends AuroraPreferenceActivity
    implements AuroraPreference.OnPreferenceChangeListener{
    // Aurora xuyong 2014-08-06 added for hidden SSCN feature start
     private boolean mNeedShowSSCN = false;
    // Aurora xuyong 2014-08-06 added for hidden SSCN feature end
    private static final String TAG = "MessagingPreferenceActivity";
    private static final boolean DEBUG = false;
    // Symbolic names for the keys used for preference lookup
    public static final String MMS_DELIVERY_REPORT_MODE = "pref_key_mms_delivery_reports";
    public static final String EXPIRY_TIME              = "pref_key_mms_expiry";
    public static final String PRIORITY                 = "pref_key_mms_priority";
    public static final String READ_REPORT_MODE         = "pref_key_mms_read_reports";
    // M: add this for read report
    public static final String READ_REPORT_AUTO_REPLY   = "pref_key_mms_auto_reply_read_reports";    
    public static final String SMS_DELIVERY_REPORT_MODE = "pref_key_sms_delivery_reports";
    public static final String NOTIFICATION_ENABLED     = "pref_key_enable_notifications";
    public static final String NOTIFICATION_RINGTONE    = "pref_key_ringtone";
    public static final String AUTO_RETRIEVAL           = "pref_key_mms_auto_retrieval";
    public static final String RETRIEVAL_DURING_ROAMING = "pref_key_mms_retrieval_during_roaming";
    public static final String AUTO_DELETE              = "pref_key_auto_delete";
    public static final String CREATION_MODE            = "pref_key_mms_creation_mode";
    public static final String MMS_SIZE_LIMIT           = "pref_key_mms_size_limit";
    public static final String SMS_QUICK_TEXT_EDITOR    = "pref_key_quick_text_editor";
    public static final String SMS_SERVICE_CENTER       = "pref_key_sms_service_center";
    public static final String SMS_MANAGE_SIM_MESSAGES  = "pref_key_manage_sim_messages";
    public static final String SMS_SAVE_LOCATION        = "pref_key_sms_save_location";
    public static final String MMS_ENABLE_TO_SEND_DELIVERY_REPORT = "pref_key_mms_enable_to_send_delivery_reports";
    public static final String MSG_IMPORT               = "pref_key_import_msg";
    public static final String MSG_EXPORT               = "pref_key_export_msg";
    public static final String SMS_INPUT_MODE           = "pref_key_sms_input_mode";
    public static final String CELL_BROADCAST           = "pref_key_cell_broadcast";
    public static final String SMS_FORWARD_WITH_SENDER  = "pref_key_forward_with_sender";
    // Aurora xuyong 2014-08-04 added for sms center feature start
    public static final String AURORA_SIM1_SMS_CENTER  = "pref_key_sms_center_sim1";
    public static final String AURORA_SIM2_SMS_CENTER  = "pref_key_sms_center_sim2";
    // Aurora xuyong 2014-08-04 added for sms center feature end
    // Menu entries
    private static final int MENU_RESTORE_DEFAULTS    = 1;
    private final int MAX_EDITABLE_LENGTH = 20;
    private AuroraPreference mStorageStatusPref;
    private AuroraPreference mSmsLimitPref;
    private AuroraPreference mSmsQuickTextEditorPref;
    private AuroraPreference mMmsLimitPref;
    private AuroraPreference mManageSimPref;
    private AuroraPreference mClearHistoryPref;
    private Recycler mSmsRecycler;
    private Recycler mMmsRecycler;
    private AuroraPreference mSmsServiceCenterPref;
    private AuroraPreference mImportMessages;
    private AuroraPreference mExportMessages;
    private AuroraPreference mCBsettingPref;

    // all preferences need change key for single sim card
    private AuroraSwitchPreference mSmsDeliveryReport;
    private AuroraSwitchPreference mMmsDeliveryReport;
    private AuroraSwitchPreference mMmsEnableToSendDeliveryReport;
    private AuroraSwitchPreference mMmsReadReport;
    // M: add this for read report
    private AuroraSwitchPreference mMmsAutoReplyReadReport;    
    private AuroraSwitchPreference mMmsAutoRetrieval;
    private AuroraSwitchPreference mMmsRetrievalDuringRoaming;
    private AuroraSwitchPreference mEnableNotificationsPref;
    private AuroraSwitchPreference mSmsForwardWithSender;

    // all preferences need change key for multiple sim card
    private AuroraPreference mSmsDeliveryReportMultiSim;
    private AuroraPreference mMmsDeliveryReportMultiSim;
    private AuroraPreference mMmsEnableToSendDeliveryReportMultiSim;
    private AuroraPreference mMmsReadReportMultiSim;
    // M: add this for read report
    private AuroraPreference mMmsAutoReplyReadReportMultiSim;
    private AuroraPreference mMmsAutoRetrievalMultiSim;
    private AuroraPreference mMmsRetrievalDuringRoamingMultiSim;
    private AuroraPreference mSmsServiceCenterPrefMultiSim;
    private AuroraPreference mManageSimPrefMultiSim;
    private AuroraPreference mCellBroadcastMultiSim;
    private AuroraPreference mSmsSaveLoactionMultiSim;

    private AuroraListPreference mMmsPriority;
    private AuroraListPreference mSmsLocation;
    private AuroraListPreference mMmsCreationMode;
    private AuroraListPreference mMmsSizeLimit;
    private AuroraListPreference mSmsInputMode;
    // Aurora xuyong 2014-08-04 added for sms center feature start
    private AuroraSmsCenterPreference mSim1SmsCenterPreference;
    private AuroraSmsCenterPreference mSim2SmsCenterPreference;
    // Aurora xuyong 2014-08-04 added for sms center feature end
    private static final int CONFIRM_CLEAR_SEARCH_HISTORY_DIALOG = 3;
    private static final String PRIORITY_HIGH= "High";
    private static final String PRIORITY_LOW= "Low";
    private static final String PRIORITY_NORMAL= "Normal";
    
    private static final String LOCATION_PHONE = "Phone";
    private static final String LOCATION_SIM = "Sim";
    
    private static final String CREATION_MODE_RESTRICTED = "RESTRICTED";
    private static final String CREATION_MODE_WARNING = "WARNING";
    private static final String CREATION_MODE_FREE = "FREE";
    
    private static final String SIZE_LIMIT_100 = "100";
    private static final String SIZE_LIMIT_200 = "200";
    private static final String SIZE_LIMIT_300 = "300";
     

    private Handler mSMSHandler = new Handler();
    private Handler mMMSHandler = new Handler();
    private AuroraEditText mNumberText;
    private AuroraAlertDialog mNumberTextDialog;
    private List<SIMInfo> listSimInfo;
    private GnTelephonyManagerEx mTelephonyManager;
    int slotId;
    private NumberPickerDialog mSmsDisplayLimitDialog;
    private NumberPickerDialog mMmsDisplayLimitDialog;
    private AuroraEditText inputNumber;
    /*import or export SD card*/
    private AuroraProgressDialog progressdialog = null;
    private static final String TABLE_SMS = "sms";
    private String mFileNamePrefix = "sms";
    private String mFileNameSuffix = "";
    private String mFileNameExtension = "db";
    private static final Uri SMS_URI = Uri.parse("content://sms");
    private static final Uri CANADDRESS_URI = Uri.parse("content://mms-sms/canonical-addresses");
    public static final String SDCARD_DIR_PATH = "//sdcard//message//";
    // Gionee zhangxx 2012-04-02 add for CR00556294 begin
    public static final String SDCARD2_DIR_PATH = "/mnt/sdcard2/message/";
    // Gionee zhangxx 2012-04-02 add for CR00556294 end
    public static final String MEM_DIR_PATH = "//data//data//com.android.mms//message//sms001.db";
    private static final String[] SMS_COLUMNS =
    { "thread_id", "address","m_size", "person", "date", "protocol", "read", "status", "type", "reply_path_present",
      "subject", "body", "service_center", "locked", Sms.SIM_ID, "error_code", "seen"};
    public Handler mMainHandler; 
    private static final String[] ADDRESS_COLUMNS = {"address"};
    private static final int EXPORT_SMS    = 2;    
    private static final int EXPORT_SUCCES = 3;
    private static final int EXPORT_FAILED = 4;
    private static final int IMPORT_SUCCES = 5;
    private static final int IMPORT_FAILED = 6;
    private static final int EXPORT_EMPTY_SMS = 7;
    private static final int DISK_IO_FAILED = 8;
    private static final int MIN_FILE_NUMBER = 1;
    private static final int MAX_FILE_NUMBER = 999;
    public String SUB_TITLE_NAME = "sub_title_name";
    private int currentSimCount = 0;

    //gionee gaoj 2012-4-10 added for CR00555790 start
    private static final int MIN_PASSWORD_LENGTH = 4;
    private static AuroraPreference         mSetEncryPref;
    public static final String SETTING_ENCRYPTION    = "pref_key_mms_encryption_setting";
    //gionee gaoj 2012-4-10 added for CR00555790 end
    // gionee zhouyj 2012-05-18 add for CR00601523 start
    private AuroraPreference  mEditSignaturePref;
    private AuroraSwitchPreference mSignaturePrefCheckBox;
    private static final String SIGNATURE_PREF = "com.gionee.mms.signature_prefences";
    private final String SETTING_EDIT_SIGNATURE    = "pref_key_edit_signature";
    private AuroraAlertDialog mEditDialog;
    // gionee zhouyj 2012-05-18 add for CR00601523 end
    // Gionee fangbin 20120223 addded for CR00527995 start
    private Uri mSmsUri = Uri.parse("content://sms");
    private Uri mMmsUri = Uri.parse("content://mms");
    private Uri mPushUri = Uri.parse("content://wappush");
    private Uri mThreadUri = Uri.parse("content://cb/threads");
    private Uri mRegisterUri = Uri.parse("content://mms-sms");
    private AuroraPreferenceCategory mPhoneMsgStatsCategory = null;
    private AuroraPreference mUnreadPreference = null;
    private AuroraPreference mSessionPreference = null;
    private AuroraPreference mMsgTotalCountPreference = null;
    private DbChangeResolver mResolver = null;
    // Gionee fangbin 20120223 addded for CR00527995 end

    //gionee gaoj 2012-8-5 added for CR00663940 start
    private QueryHandler mQueryHandler;
    private static final int MMS_QUERY_TOKEN = 40;
    private static final int SMS_QUERY_TOKEN = 41;
    private static final int PUS_QUERY_TOKEN = 42;
    private static final int THEAD_QUERY_TOKEN = 43;
    private int mUnReadNum = 0;
    private int mSessionNum = 0;
    private int mMsgCount = 0;
    //gionee gaoj 2012-8-5 added for CR00663940 end
    
    //Gionee zengxuanhui 20120809 add for CR00672106 begin
    private static final boolean gnGeminiRingtoneSupport = SystemProperties.get("ro.gn.gemini.ringtone.support").equals("yes");
    public static final String NOTIFICATION_RINGTONE2    = "pref_key_ringtone2";
    // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
    //private AudioProfileManager mProfileManager = null;
    // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
    //Gionee zengxuanhui 20120809 add for CR00672106 end
    //Gionee liuxiangrong 2012-10-16 add for CR00714584 start
    private static final Boolean gnQMflag = SystemProperties.get("ro.gn.oversea.custom").equals("PAKISTAN_QMOBILE");
    //Gionee liuxiangrong 2012-10-16 add for CR00714584 end  
    //gionee gaoj 2012-9-20 added for CR00699291 start
    private boolean mIsonResume = false;
    //gionee gaoj 2012-9-20 added for CR00699291 end

    //gionee gaoj 2013-2-19 adde for CR00771935 start
    public static String PHOTO_STYLE_KEY = "photoStyle";
    private String[] mPhotoOptionsEntries;
    private String[] mPhotoOptionsEntrieValues;
    //gionee gaoj 2013-2-19 adde for CR00771935 end
    
    //Gionee <guoyx> <2013-06-08> add for CR00822607 begin
    private MmsRingtonePreference mRingtonePref;
    //Gionee <guoyx> <2013-06-08> add for CR00822607 end

    private static AuroraPreference         mRestoreDefault;
    public static final String RESTORE_DEFAULT    = "pref_key_restore_default";
    
    @Override
    protected void onPause(){
        super.onPause();
        //gionee gaoj 2012-9-20 added for CR00699291 start
        mIsonResume = false;
        //gionee gaoj 2012-9-20 added for CR00699291 end
        if (mSmsDisplayLimitDialog != null ) {
            mSmsDisplayLimitDialog.dismiss();
        }
        if (mMmsDisplayLimitDialog != null ) {
            mMmsDisplayLimitDialog.dismiss();
        }
        if (progressdialog != null){
            progressdialog.dismiss();
        }
        // Gionee fangbin 20120629 added for CR00622030 start
        //Gionee zengxuanhui 20120814 modify for CR00673472 begin
        if (android.os.SystemProperties.get("ro.gn.settings.prop").equals("yes") && !gnGeminiRingtoneSupport) {
            sendMmsRingtoneReceiver();
        }
        //Gionee zengxuanhui 20120814 modify for CR00673472 end
        // Gionee fangbin 20120629 added for CR00622030 start
    }
  
    @Override
    protected void onResume() {
        super.onResume();
        //gionee gaoj 2012-9-20 added for CR00699291 start
        mIsonResume = true;
        //gionee gaoj 2012-9-20 added for CR00699291 end
        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (MmsApp.mEncryption) {
            updateSetEncrypView();
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
        // Gionee fangbin 20120712 added for CR00643058 start
        //Gionee zengxuanhui modify for CR00673472 begin
        if (android.os.SystemProperties.get("ro.gn.settings.prop").equals("yes") && !gnGeminiRingtoneSupport) {
            sendMmsRingtoneReceiver();
        }
        //Gionee zengxuanhui modify for CR00673472 end
        // Gionee fangbin 20120712 added for CR00643058 end
        setListPrefSummary();
        // Since the enabled notifications pref can be changed outside of this activity,
        // we have to reload it whenever we resume.
        setEnabledNotificationsPref();
    }
        
    private void setListPrefSummary(){
        SharedPreferences sp = AuroraPreferenceManager.getDefaultSharedPreferences(this);
        //For mMmsPriority;
        String stored = sp.getString(PRIORITY, getString(R.string.priority_normal));
        mMmsPriority.setSummary(getVisualTextName(stored, R.array.pref_key_mms_priority_choices,
                R.array.pref_key_mms_priority_values));
        
        //For mSmsLocation;
        //MTK_OP02_PROTECT_START
        if (MmsApp.isUnicomOperator()) {
            if (MmsApp.mGnMultiSimMessage == true) {
                int currentSimCount = SIMInfo.getInsertedSIMCount(this);
                int slotId = 0;
                if (currentSimCount == 1) {
                    slotId = SIMInfo.getInsertedSIMList(this).get(0).mSlot;
                    stored = sp.getString((Long.toString(slotId) + "_" + SMS_SAVE_LOCATION), "Phone");
                }
                Log.d(TAG, "setListPrefSummary stored slotId = "+ slotId + " stored =" + stored);
            } else {
                stored = sp.getString(SMS_SAVE_LOCATION, "Phone");
                Log.d(TAG, "setListPrefSummary stored 2 =" + stored);
            }
        } else {
        //MTK_OP02_PROTECT_END
        stored = sp.getString(SMS_SAVE_LOCATION, "Phone");
              Log.d(TAG, "setListPrefSummary stored 3 =" + stored);
       //MTK_OP02_PROTECT_START
        }
        //MTK_OP02_PROTECT_END
        mSmsLocation.setSummary(getVisualTextName(stored, R.array.pref_sms_save_location_choices,
                R.array.pref_sms_save_location_values));
        
        //For mMmsCreationMode
        stored = sp.getString(CREATION_MODE, "FREE");
        mMmsCreationMode.setSummary(getVisualTextName(stored, R.array.pref_mms_creation_mode_choices,
                R.array.pref_mms_creation_mode_values));
        
        //For mMmsSizeLimit
        stored = sp.getString(MMS_SIZE_LIMIT, "300");
        mMmsSizeLimit.setSummary(getVisualTextName(stored, R.array.pref_mms_size_limit_choices,
                R.array.pref_mms_size_limit_values));
        
        //Gionee <guoyx> <2013-06-08> add for CR00822607 begin
        setRingtoneSummary();
        //Gionee <guoyx> <2013-06-08> add for CR00822607 end
    }

    private void newMainHandler(){
        mMainHandler=new Handler() {
            @Override
            public void handleMessage(Message msg) { 
                int output = R.string.export_message_empty;  
                switch(msg.what){ 
                case EXPORT_SUCCES: 
                    output = R.string.export_message_success;
                    break;
                case EXPORT_FAILED: 
                    output = R.string.export_message_fail;
                    break;
                case IMPORT_SUCCES: 
                    output = R.string.import_message_success;
                    break;
                case IMPORT_FAILED: 
                    output = R.string.import_message_fail;
                    break;
                case EXPORT_EMPTY_SMS: 
                    output = R.string.export_message_empty;
                    break;    
                case DISK_IO_FAILED: 
                    output = R.string.export_disk_problem;
                    break;
                default: 
                    break;
                }
                showToast(output);
                
            }
        };
    }
    
    @Override
    protected void onCreate(Bundle icicle) {
        // Aurora liugj 2013-09-17 modified for aurora's new feature start
        //gionee gaoj 2012-6-27 added for CR00628364 start
        /*if (MmsApp.mLightTheme) {
            setTheme(R.style.GnMmsLightTheme);
        } else if (MmsApp.mDarkStyle) {
            setTheme(R.style.GnMmsDarkTheme);
        }*/
        //gionee gaoj 2012-6-27 added for CR00628364 end
        // Aurora liugj 2013-09-17 modified for aurora's new feature end
        super.onCreate(icicle);
        Log.d(TAG, "onCreate");
        newMainHandler();
        // Aurora liugj 2013-09-17 modified for aurora's new feature start
        AuroraActionBar actionBar = getAuroraActionBar();
        actionBar.setTitle(R.string.menu_preferences);
        // Aurora liugj 2013-09-17 modified for aurora's new feature end
        actionBar.setDisplayHomeAsUpEnabled(true);

        //gionee gaoj 2012-8-5 added for CR00663940 start
        if (mQueryHandler == null) {
            mQueryHandler = new QueryHandler(this);
        }
        //gionee gaoj 2012-8-5 added for CR00663940 end
        //gionee gaoj 2012-5-29 added for CR00555790 start
        // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//        if (MmsApp.mGnMessageSupport) {
//            actionBar.setDisplayShowHomeEnabled(false);
//        }
        // Aurora liugj 2013-09-13 deleted for aurora's new feature end
        //gionee gaoj 2012-5-29 added for CR00555790 end
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
        //mProfileManager = (AudioProfileManager)getSystemService(Context.AUDIOPROFILE_SERVICE);
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
        setMessagePreferences();
        mSmsCenterCategory =
                (AuroraPreferenceCategory)findPreference("pref_aurora_key_sms_service_center");
          this.getContentResolver().registerContentObserver(SimInfo.CONTENT_URI, true, mSimInfoObserver);
          mSimStateChangedFilter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
          mSimStateChangedFilter.addAction("android.intent.action.PHB_STATE_CHANGED");
          registerReceiver(mSimStateChangedReceiver, mSimStateChangedFilter);
    }
    
    private IntentFilter mSimStateChangedFilter = new IntentFilter();
    private BroadcastReceiver mSimStateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(intent.getAction())
                    || intent.getAction().equals("android.intent.action.PHB_STATE_CHANGED")) {
                initSmsCenterPeferences();
            }
        }
    };
    
    private ContentObserver mSimInfoObserver = new ContentObserver(new Handler()) {

            @Override
            public void onChange(boolean selfChange) { 
                 super.onChange(selfChange);
                 initSmsCenterPeferences();
            }   

        };
        
    private AuroraPreferenceCategory mSmsCenterCategory;
    // Aurora xuyong 2014-08-12 added for bug #7474 start
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
    // Aurora xuyong 2014-08-12 added for bug #7474 end  
    private void initSmsCenterPeferences() {
        mTelephonyManager = GnTelephonyManagerEx.getDefault();
        if (mSmsCenterCategory == null) {
            return;
        }
       // Aurora xuyong 2014-08-06 added for hidden SSCN feature start
        if (!mNeedShowSSCN) {
            getPreferenceScreen().removePreference(mSmsCenterCategory);
            return;
        }
       // Aurora xuyong 2014-08-06 added for hidden SSCN feature end
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
                // Aurora xuyong 2014-08-07 modified for bug #7342 start
                // Aurora xuyong 2014-08-12 modified for bug #7474 start
                    mSim2SmsCenterPreference.setOperator(GnTelephonyManager.getSimOperatorGemini(1));
                // Aurora xuyong 2014-08-12 modified for bug #7474 end
                // Aurora xuyong 2014-08-07 modified for bug #7342 end
                    mSim2SmsCenterPreference.setSimThumbNail(1);
                // Aurora xuyong 2014-08-12 modified for bug #7474 start
                    mSim2SmsCenterPreference.setSmsCenterNumber(rebuidScAddress(mTelephonyManager.getScAddress(1), 1));
                // Aurora xuyong 2014-08-12 modified for bug #7474 end
                }
            } else if (info2 == null) {
                getPreferenceScreen().addPreference(mSmsCenterCategory);
                if (mSim2SmsCenterPreference != null) {
                    mSmsCenterCategory.removePreference(mSim2SmsCenterPreference);
                }
                if (mSim1SmsCenterPreference != null) {
                // Aurora xuyong 2014-08-07 modified for bug #7342 start
                // Aurora xuyong 2014-08-12 modified for bug #7474 start
                    mSim1SmsCenterPreference.setOperator(GnTelephonyManager.getSimOperatorGemini(0));
                // Aurora xuyong 2014-08-12 modified for bug #7474 end
                // Aurora xuyong 2014-08-07 modified for bug #7342 end
                    mSim1SmsCenterPreference.setSimThumbNail(0);
                // Aurora xuyong 2014-08-12 modified for bug #7474 start
                    mSim1SmsCenterPreference.setSmsCenterNumber(rebuidScAddress(mTelephonyManager.getScAddress(0), 0));
                // Aurora xuyong 2014-08-12 modified for bug #7474 end
                }
            } else {
                getPreferenceScreen().addPreference(mSmsCenterCategory);
                if (mSim1SmsCenterPreference != null) {
                // Aurora xuyong 2014-08-07 modified for bug #7342 start
                // Aurora xuyong 2014-08-12 modified for bug #7474 start
                    mSim1SmsCenterPreference.setOperator(GnTelephonyManager.getSimOperatorGemini(0));
                // Aurora xuyong 2014-08-12 modified for bug #7474 end
                // Aurora xuyong 2014-08-07 modified for bug #7342 end
                    mSim1SmsCenterPreference.setSimThumbNail(0);
                // Aurora xuyong 2014-08-12 modified for bug #7474 start
                    mSim1SmsCenterPreference.setSmsCenterNumber(rebuidScAddress(mTelephonyManager.getScAddress(0), 0));
                // Aurora xuyong 2014-08-12 modified for bug #7474 end
                }
                if (mSim2SmsCenterPreference != null) {
                // Aurora xuyong 2014-08-07 modified for bug #7342 start
                // Aurora xuyong 2014-08-12 modified for bug #7474 start
                    mSim2SmsCenterPreference.setOperator(GnTelephonyManager.getSimOperatorGemini(0));
                // Aurora xuyong 2014-08-12 modified for bug #7474 end
                // Aurora xuyong 2014-08-07 modified for bug #7342 end
                    mSim2SmsCenterPreference.setSimThumbNail(1);
                // Aurora xuyong 2014-08-12 modified for bug #7474 start
                    mSim2SmsCenterPreference.setSmsCenterNumber(rebuidScAddress(mTelephonyManager.getScAddress(1), 1));
                // Aurora xuyong 2014-08-12 modified for bug #7474 end
                }
            }
        } else {
          // Aurora xuyong 2014-08-29 modified for bug #8021 start
            if (!GnTelephonyManager.hasIccCard()) {
          // Aurora xuyong 2014-08-29 modified for bug #8021 end
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
                // Aurora xuyong 2014-08-07 modified for bug #7342 start
                // Aurora xuyong 2014-08-12 modified for bug #7474 start
                    mSim1SmsCenterPreference.setOperator(GnTelephonyManager.getSimOperatorGemini(0));
                // Aurora xuyong 2014-08-12 modified for bug #7474 end
                // Aurora xuyong 2014-08-07 modified for bug #7342 end
                // Aurora xuyong 2014-08-12 modified for bug #7474 start
                    mSim1SmsCenterPreference.setSmsCenterNumber(rebuidScAddress(mTelephonyManager.getScAddress(0), 0));
                // Aurora xuyong 2014-08-12 modified for bug #7474 end
                }
            }
        }
    }
    // Aurora xuyong 2014-08-04 added for sms center feature end
    private void setMessagePreferences() {
        // aurora: wangth 20140516 remove for multi phone start
//        if (MmsApp.mGnMultiSimMessage == true) {
//            Log.d(TAG, "MTK_GEMINI_SUPPORT is true");
//            currentSimCount = SIMInfo.getInsertedSIMCount(this);
//            Log.d(TAG, "currentSimCount is :" + currentSimCount);
//            if (currentSimCount <= 1) {
//                //gionee gaoj 2012-4-10 added for CR00555790 start
//                if (MmsApp.mGnMessageSupport) {
//                    //gionee gaoj 2012-7-24 added for CR00650452 start
//                    if (MmsApp.mGnPopDefaultValue) {
//                        //Gionee zengxuanhui 20120809 modify for CR00672106 begin
//                        if(gnGeminiRingtoneSupport){
//                            addPreferencesFromResource(R.xml.gn_gemini_ringtone_preferences_popfalse);
//                        }else{
//                            addPreferencesFromResource(R.xml.gn_preferences_popfalse);
//                        }
//                        //Gionee zengxuanhui 20120809 modify for CR00672106 end
//                    } else {
//                        //Gionee zengxuanhui 20120821 modify for CR00673469 begin
//                        if(gnGeminiRingtoneSupport){
//                            addPreferencesFromResource(R.xml.gn_gemini_ringtone_preferences);
//                        }else{
//                            addPreferencesFromResource(R.xml.gn_preferences);
//                        }
//                        //Gionee zengxuanhui 20120821 modify for CR00673469 end
//                    }
//                    //gionee gaoj 2012-7-24 added for CR00650452 end
//                } else {
//                    //gionee gaoj 2012-4-10 added for CR00555790 end
//                    addPreferencesFromResource(R.xml.preferences);
//                //gionee gaoj 2012-4-10 added for CR00555790 start
//                }
//                //gionee gaoj 2012-4-10 added for CR00555790 end
//                // MTK_OP01_PROTECT_START
//                if (MmsApp.isTelecomOperator()) {
//                mMmsEnableToSendDeliveryReport = (AuroraSwitchPreference) findPreference(MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
//                } else
//                // MTK_OP01_PROTECT_END
//                {
//                mMmsEnableToSendDeliveryReport = (AuroraSwitchPreference) findPreference(MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
//                AuroraPreferenceCategory mmsCategory = (AuroraPreferenceCategory) findPreference("pref_key_mms_settings");
//                mmsCategory.removePreference(mMmsEnableToSendDeliveryReport);
//                } 
//                
//            } else {
//                //gionee gaoj 2012-4-10 added for CR00555790 start
//                if (MmsApp.mGnMessageSupport) {
//                    //gionee gaoj 2012-7-24 added for CR00650452 start
//                    if (MmsApp.mGnPopDefaultValue) {
//                        //Gionee zengxuanhui 20120809 modify for CR00673469 begin
//                        if(gnGeminiRingtoneSupport){
//                            addPreferencesFromResource(R.xml.gn_gemini_ringtone_multicardpreferences_popfalse);
//                        }else{
//                            addPreferencesFromResource(R.xml.gn_multicardpreferences_popfalse);
//                        }
//                        
//                    } else {
//                        if(gnGeminiRingtoneSupport){
//                            addPreferencesFromResource(R.xml.gn_gemini_ringtone_multicardpreferences);
//                        }else{
//                            addPreferencesFromResource(R.xml.gn_multicardpreferences);
//                        }
//                        //Gionee zengxuanhui 20120809 modify for CR00673469 end
//                    }
//                    //gionee gaoj 2012-7-24 added for CR00650452 end
//                } else {
//                    //gionee gaoj 2012-4-10 added for CR00555790 end
//                addPreferencesFromResource(R.xml.multicardpreferences);
//                //gionee gaoj 2012-4-10 added for CR00555790 start
//                }
//                //gionee gaoj 2012-4-10 added for CR00555790 end
//            }
//        } else {
        // aurora: wangth 20140516 remove for multi phone end
            //gionee gaoj 2012-4-10 added for CR00555790 start
            if (MmsApp.mGnMessageSupport) {
                //gionee gaoj 2012-7-24 added for CR00650452 start
                if (MmsApp.mGnPopDefaultValue) {
                    // Aurora liugj 2013-09-16 deleted for aurora's new feature start
                    addPreferencesFromResource(R.xml.aurora_set_preferences); //aurora_message_set_preferences
                    // Aurora liugj 2013-09-16 deleted for aurora's new feature end
                } else {
                    addPreferencesFromResource(R.xml.gn_preferences);
                }
                //gionee gaoj 2012-7-24 added for CR00650452 end
                //Gionee <guoyx> <2013-06-28> add for CR00822607 begin
                mRingtonePref = (MmsRingtonePreference) findPreference(NOTIFICATION_RINGTONE);
                //Gionee <guoyx> <2013-06-28> add for CR00822607 end
            } else {
                //gionee gaoj 2012-4-10 added for CR00555790 end
                    // Aurora liugj 2013-12-24 modified for iuni-4.3 adapter error start
                //addPreferencesFromResource(R.xml.preferences);
                addPreferencesFromResource(R.xml.aurora_set_preferences);
                    // Aurora liugj 2013-12-24 modified for iuni-4.3 adapter error end
                //gionee gaoj 2012-4-10 added for CR00555790 start
            }
            //gionee gaoj 2012-4-10 added for CR00555790 end
             // MTK_OP01_PROTECT_START
            if (MmsApp.isTelecomOperator()) {
                mMmsEnableToSendDeliveryReport = (AuroraSwitchPreference) findPreference(MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
            } else
               // MTK_OP01_PROTECT_END
               {
                mMmsEnableToSendDeliveryReport = (AuroraSwitchPreference) findPreference(MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
                AuroraPreferenceCategory mmsCategory = (AuroraPreferenceCategory) findPreference("pref_key_mms_settings");
                mmsCategory.removePreference(mMmsEnableToSendDeliveryReport);
               } 
            // aurora: wangth 20140516 remove for multi phone start
//        }
            // aurora: wangth 20140516 remove for multi phone end
        // M: add for read report
        if (FeatureOption.MTK_SEND_RR_SUPPORT == false) {
            // remove read report entry
            Log.d(MmsApp.TXN_TAG, "remove the read report entry, it should be hidden.");
            AuroraPreferenceCategory mmOptions = (AuroraPreferenceCategory)findPreference("pref_key_mms_settings");
            mmOptions.removePreference(findPreference(READ_REPORT_AUTO_REPLY));          
        }

        // MTK_OP01_PROTECT_START
        if (MmsApp.isTelecomOperator()) { 
            mStorageStatusPref = findPreference("pref_key_storage_status");
        } else
        // MTK_OP01_PROTECT_END
        {  
            mStorageStatusPref = findPreference("pref_key_storage_status");
            AuroraPreferenceCategory storageCategory = (AuroraPreferenceCategory) findPreference("pref_key_storage_settings");
            storageCategory.removePreference(mStorageStatusPref);
        }
        
        mCBsettingPref = findPreference(CELL_BROADCAST); 
        mSmsLimitPref = findPreference("pref_key_sms_delete_limit"); 
        mMmsLimitPref = findPreference("pref_key_mms_delete_limit");
        mClearHistoryPref = findPreference("pref_key_mms_clear_history");
        mSmsQuickTextEditorPref = findPreference("pref_key_quick_text_editor");
        
        
        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (MmsApp.mGnMessageSupport) {
            // Gionee fangbin 20120223 addded for CR00527995 start
            mPhoneMsgStatsCategory = (AuroraPreferenceCategory) findPreference("gn_phone_msg_stats");
            mUnreadPreference = (AuroraPreference) findPreference("pref_key_msg_unread");
            mSessionPreference = (AuroraPreference) findPreference("pref_key_session_count");
            mMsgTotalCountPreference = (AuroraPreference) findPreference("pref_key_msg_total_count");
            initPhoneMsgCount();
            mResolver = new DbChangeResolver(new Handler());
            this.getContentResolver().registerContentObserver(mRegisterUri, true, mResolver);
            // Gionee fangbin 20120223 addded for CR00527995 end
          // Aurora liugj 2013-12-24 modified for iuni-4.3 adapter error start
        /*if (!MmsApp.mGnPopupMsgSupport) {
            AuroraPreferenceCategory category = (AuroraPreferenceCategory) findPreference("pref_key_notification_settings");
            category.removePreference(findPreference("pref_key_enable_pop_notifications"));
        }*/
        /*if (!MmsApp.mEncryption) {
            AuroraPreferenceCategory encryptionCategory = (AuroraPreferenceCategory) findPreference("pref_key_enable_encryption");
            getPreferenceScreen().removePreference(encryptionCategory);
        }*/
          // Aurora liugj 2013-12-24 modified for iuni-4.3 adapter error end
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
        mMmsPriority = (AuroraListPreference) findPreference("pref_key_mms_priority");
        mMmsPriority.setOnPreferenceChangeListener(this);
        mSmsLocation = (AuroraListPreference) findPreference(SMS_SAVE_LOCATION);
        mSmsLocation.setOnPreferenceChangeListener(this);
        mMmsCreationMode = (AuroraListPreference) findPreference("pref_key_mms_creation_mode");
        mMmsCreationMode.setOnPreferenceChangeListener(this);
        mMmsSizeLimit = (AuroraListPreference) findPreference("pref_key_mms_size_limit");
        mMmsSizeLimit.setOnPreferenceChangeListener(this);
        mEnableNotificationsPref = (AuroraSwitchPreference) findPreference(NOTIFICATION_ENABLED);
        AuroraPreferenceCategory smsCategory =
            (AuroraPreferenceCategory)findPreference("pref_key_sms_settings");
        if(MmsApp.mGnMultiSimMessage == true){ 
            if (currentSimCount == 0){
                
                // No SIM card, remove the SIM-related prefs
                //smsCategory.removePreference(mManageSimPref);
                //If there is no SIM, this item will be disabled and can not be accessed.
                mManageSimPref = findPreference(SMS_MANAGE_SIM_MESSAGES);
                // Aurora xuyong 2014-05-26 deleted for multisim feature start
                // Aurora xuyong 2014-05-31 added for multisim feature start
                if (SIMInfo.getInsertedSIMCount(this) <= 0) {
                        mManageSimPref.setEnabled(false);
                }
                // Aurora xuyong 2014-08-04 added for sms center feature start
                initSmsCenterPeferences();
                // Aurora xuyong 2014-08-04 added for sms center feature end
                // Aurora xuyong 2014-05-31 added for multisim feature end
                // Aurora xuyong 2014-05-26 deleted for multisim feature end
                //gionee gaoj 2012-5-31 modified for CR00608428 start
                //MTK_OP02_PROTECT_START
                /*String optr = SystemProperties.get("ro.operator.optr");
                if ("OP02".equals(optr)) {
                    smsCategory.removePreference(mManageSimPref);
                }*/
                //MTK_OP02_PROTECT_END
                //gionee gaoj 2012-5-31 modified for CR00608428 end
                
                //Gionee liuxiangrong 2012-10-16 add for CR00714584 start
                if (gnQMflag){
                    smsCategory.removePreference(mManageSimPref);
                }
                //Gionee liuxiangrong 2012-10-16 add for CR00714584 end
                
                mSmsServiceCenterPref = findPreference("pref_key_sms_service_center");
                mSmsServiceCenterPref.setEnabled(false);
            }
        } else {
               // Aurora xuyong 2014-08-29 modified for bug #8021 start
             if (!GnTelephonyManager.hasIccCard()) {
             // Aurora xuyong 2014-08-29 modified for bug #8021 end
                 //smsCategory.removePreference(mManageSimPref);
                 //If there is no SIM, this item will be disabled and can not be accessed.
                 mManageSimPref = findPreference(SMS_MANAGE_SIM_MESSAGES);
                 mManageSimPref.setEnabled(false);
                 //gionee gaoj 2012-5-31 modified for CR00608428 start
                 //MTK_OP02_PROTECT_START
                 /*String optr = SystemProperties.get("ro.operator.optr");
                 if ("OP02".equals(optr)) {
                    smsCategory.removePreference(mManageSimPref);
                 }*/
                 //MTK_OP02_PROTECT_END
                 //gionee gaoj 2012-5-31 modified for CR00608428 end
                 
                 //Gionee liuxiangrong 2012-10-16 add for CR00714584 start
                 if (gnQMflag){
                    smsCategory.removePreference(mManageSimPref);
                 }
                 //Gionee liuxiangrong 2012-10-16 add for CR00714584 end
                 
                 mSmsServiceCenterPref = findPreference("pref_key_sms_service_center");
                 mSmsServiceCenterPref.setEnabled(false);
             } else {
                 mManageSimPref = findPreference(SMS_MANAGE_SIM_MESSAGES);
                 //gionee gaoj 2012-5-31 modified for CR00608428 start
                 //MTK_OP02_PROTECT_START
                 /*String optr = SystemProperties.get("ro.operator.optr");
                 if ("OP02".equals(optr)) {
                     smsCategory.removePreference(mManageSimPref);
                 }*/
                 //MTK_OP02_PROTECT_END
                 //gionee gaoj 2012-5-31 modified for CR00608428 end
                 
                 //Gionee liuxiangrong 2012-10-16 add for CR00714584 start
                 if (gnQMflag){
                    smsCategory.removePreference(mManageSimPref);
                 }
                 //Gionee liuxiangrong 2012-10-16 add for CR00714584 end
                 
                 mSmsServiceCenterPref = findPreference("pref_key_sms_service_center");
             }
             // Aurora xuyong 2014-08-04 added for sms center feature start
             initSmsCenterPeferences();
             // Aurora xuyong 2014-08-04 added for sms center feature end
        }
        if (!MmsConfig.getMmsEnabled()) {
            // No Mms, remove all the mms-related preferences
            AuroraPreferenceCategory mmsOptions =
                (AuroraPreferenceCategory)findPreference("pref_key_mms_settings");
            getPreferenceScreen().removePreference(mmsOptions);

            AuroraPreferenceCategory storageOptions =
                (AuroraPreferenceCategory)findPreference("pref_key_storage_settings");
            storageOptions.removePreference(findPreference("pref_key_mms_delete_limit"));
        }
        
        setEnabledNotificationsPref();

        enablePushSetting();
        
        mSmsRecycler = Recycler.getSmsRecycler();
        mMmsRecycler = Recycler.getMmsRecycler();

        // Fix up the recycler's summary with the correct values
        setSmsDisplayLimit();
        setMmsDisplayLimit();
        addSmsInputModePreference();
        // Change the key to the SIM-related key, if has one SIM card, else set default value.
        if (MmsApp.mGnMultiSimMessage == true) {
            Log.d(TAG, "MTK_GEMINI_SUPPORT is true");
            // Aurora xuyong 2014-09-26 modified for india requirement start
            //if (currentSimCount == 1) {
                Log.d(TAG, "single sim");
                changeSingleCardKeyToSimRelated();
            //} else if (currentSimCount > 1) {
            //    setMultiCardPreference();
            //}
            // Aurora xuyong 2014-09-26 modified for india requirement end
        }
        addBankupMessages();

        //MTK_OP01_PROTECT_START
        if (MmsApp.isTelecomOperator()) {
            mSmsForwardWithSender = (AuroraSwitchPreference) findPreference(SMS_FORWARD_WITH_SENDER);
            SharedPreferences sp = getSharedPreferences("com.android.mms_preferences", MODE_WORLD_READABLE);
            if (mSmsForwardWithSender != null) {
                mSmsForwardWithSender.setChecked(sp.getBoolean(mSmsForwardWithSender.getKey(), false));
            }
            mManageSimPref = findPreference(SMS_MANAGE_SIM_MESSAGES);
            if (MmsConfig.getMmsDirMode() && mManageSimPref != null){
                ((AuroraPreferenceCategory)findPreference("pref_key_sms_settings")).removePreference(mManageSimPref);
            }
        } else
        //MTK_OP01_PROTECT_END
        {
            mSmsForwardWithSender = (AuroraSwitchPreference) findPreference(SMS_FORWARD_WITH_SENDER);
            smsCategory.removePreference(mSmsForwardWithSender);
        }
        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (MmsApp.mEncryption) {
            mSetEncryPref = (AuroraPreference) findPreference(SETTING_ENCRYPTION);
            mSetEncryPref.setOnPreferenceChangeListener(this);
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
        // gionee zhouyj 2012-05-17 add for CR00601523 start
        if(MmsApp.mGnMessageSupport) {
            mEditSignaturePref = (AuroraPreference) findPreference(SETTING_EDIT_SIGNATURE);
            mEditSignaturePref.setOnPreferenceChangeListener(this);
            
            //gionee gaoj 2013-2-19 adde for CR00771935 start
            AuroraPreference preference = findPreference(PHOTO_STYLE_KEY);
            if (null != preference && preference instanceof AuroraListPreference) {
                preference.setSummary(((AuroraListPreference)preference).getEntry());
                preference.setOnPreferenceChangeListener(this);
            }
            mPhotoOptionsEntries = getResources().getStringArray(
                    R.array.gn_entries_list_photo_options);
            mPhotoOptionsEntrieValues = getResources().getStringArray(
                    R.array.gn_entryvalues_list_photo_options);
            //gionee gaoj 2013-2-19 adde for CR00771935 end

              // Aurora liugj 2013-12-24 modified for iuni-4.3 adapter error start
            /*if (!com.gionee.featureoption.FeatureOption.GN_FEATURE_PHOTO_STYLE) {
                AuroraPreferenceCategory photostyleCategory = (AuroraPreferenceCategory) findPreference("gn_key_photo_style_options");
                getPreferenceScreen().removePreference(photostyleCategory);
            }*/
              // Aurora liugj 2013-12-24 modified for iuni-4.3 adapter error end
            //Gionee <zhouyj> <2013-05-09> add for CR00810588 begin
            /*if (!MmsApp.mGnVoiceInputSupport && !MmsApp.mGnVoiceReadMsgSupport) {
                AuroraPreferenceCategory pc =
                    (AuroraPreferenceCategory)findPreference("pref_key_voice_setting");
                if (pc != null) {
                    getPreferenceScreen().removePreference(pc);
                }
            } else if(!MmsApp.mGnVoiceReadMsgSupport) {
                AuroraPreferenceCategory pc =
                    (AuroraPreferenceCategory)findPreference("pref_key_voice_setting");
                AuroraSwitchPreference cp = (AuroraSwitchPreference)findPreference("pref_key_voice_read");
                if (pc != null && cp != null) {
                    pc.removePreference(cp);
                }
            } else if(!MmsApp.mGnVoiceInputSupport) {
                AuroraPreferenceCategory pc =
                    (AuroraPreferenceCategory)findPreference("pref_key_voice_setting");
                AuroraSwitchPreference cp = (AuroraSwitchPreference)findPreference("pref_key_voice_input");
                if (pc != null && cp != null) {
                    pc.removePreference(cp);
                }
            }*/
            AuroraPreferenceCategory pc =
                    (AuroraPreferenceCategory)findPreference("pref_key_voice_setting");
            if (pc != null) {
                getPreferenceScreen().removePreference(pc);
            }
            //Gionee <zhouyj> <2013-05-09> add for CR00810588 end
        }
        // gionee zhouyj 2012-05-17 add for CR00601523 end

        mRestoreDefault = (AuroraPreference) findPreference(RESTORE_DEFAULT);
        mRestoreDefault.setOnPreferenceChangeListener(this);
        // Aurora liugj 2013-09-13 added for aurora's new feature start
        if (mSmsLocation != null) {
            smsCategory.removePreference(mSmsLocation);
        }
          // Aurora liugj 2013-10-25 added for remove service center start 
        smsCategory.removePreference(mSmsServiceCenterPref);
          // Aurora liugj 2013-10-25 added for remove service center end
        AuroraPreferenceCategory mmsCategory =
                (AuroraPreferenceCategory)findPreference("pref_key_mms_settings");
         // Aurora liugj 2013-12-24 modified for iuni-4.3 adapter error start
        if (mmsCategory != null) {
            // aurora wangth 20150516 modify for multi phone start
            try {
            mmsCategory.removePreference(findPreference(READ_REPORT_AUTO_REPLY));
            mmsCategory.removePreference(findPreference(CREATION_MODE));
            mmsCategory.removePreference(findPreference(MMS_SIZE_LIMIT));
            mmsCategory.removePreference(findPreference(PRIORITY));
            } catch (Exception e) {
                e.printStackTrace();
            }
            // aurora: wangth 20140516 remove for multi phone end
        }
        // Aurora liugj 2013-12-24 modified for iuni-4.3 adapter error end
        
        AuroraPreferenceCategory restoreDefaultCategory = (AuroraPreferenceCategory) findPreference("gn_restore_default");
        getPreferenceScreen().removePreference(restoreDefaultCategory);
        
        AuroraPreferenceCategory storageCategory = (AuroraPreferenceCategory) findPreference("pref_key_storage_settings");
        // Aurora liugj 2013-12-24 modified for iuni-4.3 adapter error start
        if (storageCategory != null) {
            getPreferenceScreen().removePreference(storageCategory);
        }
        // Aurora liugj 2013-12-24 modified for iuni-4.3 adapter error end
        
        AuroraPreferenceCategory nfCategory = (AuroraPreferenceCategory) findPreference("pref_key_notification_settings");
        getPreferenceScreen().removePreference(nfCategory);
        
        AuroraPreferenceCategory encryptionCategory = (AuroraPreferenceCategory) findPreference("pref_key_enable_encryption");
        getPreferenceScreen().removePreference(encryptionCategory);
        
        AuroraPreferenceCategory wappushCategory = (AuroraPreferenceCategory) findPreference("pref_key_wappush_settings");
        // Aurora liugj 2013-12-24 modified for iuni-4.3 adapter error start
        if (wappushCategory != null) {
            getPreferenceScreen().removePreference(wappushCategory);
        }
        // Aurora liugj 2013-12-24 modified for iuni-4.3 adapter error end
        
//        AuroraPreferenceCategory voiceCategory = (AuroraPreferenceCategory) findPreference("pref_key_voice_setting");
//        getPreferenceScreen().removePreference(voiceCategory);
        
        AuroraPreferenceCategory otherCategory = (AuroraPreferenceCategory) findPreference("pref_key_others");
        getPreferenceScreen().removePreference(otherCategory);
        
        AuroraPreferenceCategory psCategory = (AuroraPreferenceCategory) findPreference("gn_key_photo_style_options");
        getPreferenceScreen().removePreference(psCategory);
        
        AuroraPreferenceCategory pmCategory = (AuroraPreferenceCategory) findPreference("gn_phone_msg_stats");
        getPreferenceScreen().removePreference(pmCategory);
        // Aurora liugj 2013-09-13 added for aurora's new feature end
    }
    
    //add import/export Message 
    private void addBankupMessages(){
        mImportMessages = findPreference(MSG_IMPORT);
        mExportMessages = findPreference(MSG_EXPORT); 
    }

    //add input mode setting for op03 request, if not remove it.
    private void addSmsInputModePreference(){
        //MTK_OP03_PROTECT_START 
        Log.i(TAG, "addSmsInputModePreference optr3 = "+ MmsApp.getApplication().getOperator());
        if (MmsApp.isCmccOperator()) {
             mSmsInputMode = (AuroraListPreference) findPreference(SMS_INPUT_MODE);
        } else
        //MTK_OP03_PROTECT_END
        {
             AuroraPreferenceCategory smsCategory = (AuroraPreferenceCategory)findPreference("pref_key_sms_settings");
             mSmsInputMode = (AuroraListPreference) findPreference(SMS_INPUT_MODE);
             if(mSmsInputMode != null ){
                smsCategory.removePreference(mSmsInputMode);
             }
        }
    }

    private void changeSingleCardKeyToSimRelated() {
        // get to know which one
        listSimInfo = SIMInfo.getInsertedSIMList(this);
        SIMInfo singleCardInfo = null;
        if (listSimInfo.size() != 0) {
            singleCardInfo = listSimInfo.get(0);
        }
        if (singleCardInfo == null) {
            return;
        }
        Long simId = listSimInfo.get(0).mSimId;
        Log.d(TAG,"changeSingleCardKeyToSimRelated Got simId = " + simId);
        //translate all key to SIM-related key;
        mSmsDeliveryReport = (AuroraSwitchPreference) findPreference(SMS_DELIVERY_REPORT_MODE);
        mMmsDeliveryReport = (AuroraSwitchPreference) findPreference(MMS_DELIVERY_REPORT_MODE);
        //gionee gaoj 2012-5-27 added for CR00608337 start
        //gionee gaoj 2012-8-7 modified for CR00671500 start
        if (MmsApp.mGnMessageSupport && MessageUtils.mUnicomCustom) {
            //gionee gaoj 2012-8-7 modified for CR00671500 end
            AuroraPreferenceCategory mmsSettingCategory =
                (AuroraPreferenceCategory)findPreference("pref_key_mms_settings");
            mMmsReadReport = (AuroraSwitchPreference) findPreference(READ_REPORT_MODE);
            mMmsAutoReplyReadReport = (AuroraSwitchPreference) findPreference(READ_REPORT_AUTO_REPLY);
            mmsSettingCategory.removePreference(mMmsReadReport);
            mmsSettingCategory.removePreference(mMmsAutoReplyReadReport);
        } else {
        //gionee gaoj 2012-5-27 added for CR00608337 end
        mMmsReadReport = (AuroraSwitchPreference) findPreference(READ_REPORT_MODE);
        // M: add this for read report
        mMmsAutoReplyReadReport = (AuroraSwitchPreference) findPreference(READ_REPORT_AUTO_REPLY);
        //gionee gaoj 2012-5-27 added for CR00608337 start
        }
        //gionee gaoj 2012-5-27 added for CR00608337 end
        mMmsAutoRetrieval = (AuroraSwitchPreference) findPreference(AUTO_RETRIEVAL);
        mMmsRetrievalDuringRoaming = (AuroraSwitchPreference) findPreference(RETRIEVAL_DURING_ROAMING);
        mSmsServiceCenterPref = findPreference(SMS_SERVICE_CENTER);
        mManageSimPref = findPreference(SMS_MANAGE_SIM_MESSAGES);
        mManageSimPrefMultiSim = null;
        //MTK_OP02_PROTECT_START
        AuroraPreferenceCategory smsCategory =
            (AuroraPreferenceCategory)findPreference("pref_key_sms_settings");
        if (MmsApp.isUnicomOperator() || gnQMflag) {
            //gionee gaoj 2012-5-31 modified for CR00608428 start
            /*if (mManageSimPref != null) {
                smsCategory.removePreference(mManageSimPref);
            }*/
            //gionee gaoj 2012-5-31 modified for CR00608428 end
            if (gnQMflag){
                if (mManageSimPref != null) {
                    smsCategory.removePreference(mManageSimPref);
                }
            }
        //Gionee liuxiangrong 2012-10-16 add for CR00714584 end
        //GiONEE liuxiangrong 2012-07-20 modify for CR00652132 start(yuanqingqing merged 20120908 in CR00688212)
        if (MmsApp.isUnicomOperator()){
            int slotid = listSimInfo.get(0).mSlot;
            mSmsLocation = (AuroraListPreference) findPreference(SMS_SAVE_LOCATION);
            mSmsLocation.setKey(Long.toString(slotid) + "_" + SMS_SAVE_LOCATION);
            //get the stored value
            SharedPreferences spr = getSharedPreferences("com.android.mms_preferences", MODE_WORLD_READABLE);
            mSmsLocation.setValue(spr.getString((Long.toString(slotid) + "_" + SMS_SAVE_LOCATION), "Phone"));
        }
        //GiONEE liuxiangrong 2012-07-20 modify for CR00652132 end(yuanqingqing merged 20120908 in CR00688212)
        }
        //MTK_OP02_PROTECT_END
        // Aurora xuyong 2014-09-26 deleted for india requirement start
        //mSmsDeliveryReport.setKey(Long.toString(simId) + "_" + SMS_DELIVERY_REPORT_MODE);
        //mMmsDeliveryReport.setKey(Long.toString(simId) + "_" + MMS_DELIVERY_REPORT_MODE);  
        //mMmsReadReport.setKey(Long.toString(simId) + "_" + READ_REPORT_MODE);
        // Aurora xuyong 2014-09-26 deleted for india requirement end
        // M: add this for read report
        if (mMmsAutoReplyReadReport != null) {
            mMmsAutoReplyReadReport.setKey(Long.toString(simId) + "_" +READ_REPORT_AUTO_REPLY); 
        }
        mMmsAutoRetrieval.setKey(Long.toString(simId) + "_" + AUTO_RETRIEVAL);
        mMmsRetrievalDuringRoaming.setDependency(Long.toString(simId) + "_" + AUTO_RETRIEVAL);
        mMmsRetrievalDuringRoaming.setKey(Long.toString(simId) + "_" + RETRIEVAL_DURING_ROAMING);
        
        //MTK_OP01_PROTECT_START
        if (MmsApp.isTelecomOperator()) {
            mMmsEnableToSendDeliveryReport = (AuroraSwitchPreference) findPreference(MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
            mMmsEnableToSendDeliveryReport.setKey(Long.toString(simId) + "_" + MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
        } else
        //MTK_OP01_PROTECT_END
        {
            mMmsEnableToSendDeliveryReport = (AuroraSwitchPreference) findPreference(MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
            if(mMmsEnableToSendDeliveryReport != null){
                mMmsEnableToSendDeliveryReport.setKey(Long.toString(simId) + "_" + MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
                AuroraPreferenceCategory mmsCategory = (AuroraPreferenceCategory)findPreference("pref_key_mms_settings");
                mmsCategory.removePreference(mMmsEnableToSendDeliveryReport);
            } 
        }
        
        
        //get the stored value
        SharedPreferences sp = getSharedPreferences("com.android.mms_preferences", MODE_WORLD_READABLE);
        if (mSmsDeliveryReport != null) {
            //Gionee:linggz 2012-7-4 modify for CR00637047 begin
        //Gionee :tangzepeng 2012-10-09 modify for polytron preset the deliver report as "on " begin
            if (SystemProperties.get("ro.gn.oversea.custom").equals("RUSSIA_FLY")) {
                mSmsDeliveryReport.setChecked(sp.getBoolean(mSmsDeliveryReport.getKey(), true));
            } else if (SystemProperties.get("ro.gn.oversea.custom").equals("INDONESIA_POLYTRON")){
                mSmsDeliveryReport.setChecked(sp.getBoolean(mSmsDeliveryReport.getKey(), true));
             // Aurora xuyong 2014-09-26 modified for india requirement start
             }else if (MmsApp.mHasIndiaFeature) {
                mSmsDeliveryReport.setChecked(sp.getBoolean(mSmsDeliveryReport.getKey(), true));
            } else {
                mSmsDeliveryReport.setChecked(sp.getBoolean(mSmsDeliveryReport.getKey(), false));
              // Aurora xuyong 2014-09-26 modified for india requirement end
            }
        //Gionee :tangzepeng 2012-10-09 modify for polytron preset the deliver report as "on " end
            //Gionee:linggz 2012-7-4 modify for CR00637047 end
        }
        if (mMmsDeliveryReport != null) {
            if (SystemProperties.get("ro.gn.oversea.custom").equals("RUSSIA_FLY")) {
                mMmsDeliveryReport.setChecked(sp.getBoolean(mMmsDeliveryReport.getKey(), true));
            } else {
                mMmsDeliveryReport.setChecked(sp.getBoolean(mMmsDeliveryReport.getKey(), false));
            }
        }
        if (mMmsEnableToSendDeliveryReport != null) {
            mMmsEnableToSendDeliveryReport.setChecked(sp.getBoolean(mMmsEnableToSendDeliveryReport.getKey(), true));
        }
        if (mMmsReadReport != null) {
            mMmsReadReport.setChecked(sp.getBoolean(mMmsReadReport.getKey(), false));
        }
        // M: add for read report
        if (mMmsAutoReplyReadReport != null) {
            mMmsAutoReplyReadReport.setChecked(sp.getBoolean(mMmsAutoReplyReadReport.getKey(), false));
        }        
        if (mMmsAutoRetrieval != null) {
            mMmsAutoRetrieval.setChecked(sp.getBoolean(mMmsAutoRetrieval.getKey(), true));
        }
        if (mMmsRetrievalDuringRoaming != null) {

        //Gionee:songganggang 2012-10-23 modify for CR00717052 begin
        if (SystemProperties.get("ro.gn.oversea.custom").equals("VISUALFAN")) {
            mMmsRetrievalDuringRoaming.setChecked(sp.getBoolean(mMmsRetrievalDuringRoaming.getKey(), true));
        } else {
            mMmsRetrievalDuringRoaming.setChecked(sp.getBoolean(mMmsRetrievalDuringRoaming.getKey(), false));
        }
        //Gionee:songganggang 2012-10-23 modify for CR00717052 end
        }
    }
    
    private void setMultiCardPreference() {    
        mSmsDeliveryReportMultiSim = findPreference(SMS_DELIVERY_REPORT_MODE);
        mMmsDeliveryReportMultiSim = findPreference(MMS_DELIVERY_REPORT_MODE);
        //MTK_OP01_PROTECT_START
        if (MmsApp.isTelecomOperator()) {
            mMmsEnableToSendDeliveryReportMultiSim = findPreference(MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
        } else
        //MTK_OP01_PROTECT_END
        {
            mMmsEnableToSendDeliveryReportMultiSim = findPreference(MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
            AuroraPreferenceCategory mmsCategory =
                (AuroraPreferenceCategory)findPreference("pref_key_mms_settings");
            mmsCategory.removePreference(mMmsEnableToSendDeliveryReportMultiSim);
        }
        
        
        //gionee gaoj 2012-5-27 added for CR00608337 start
        //gionee gaoj 2012-8-7 modified for CR00671500 start
        if (MmsApp.mGnMessageSupport && MessageUtils.mUnicomCustom) {
            //gionee gaoj 2012-8-7 modified for CR00671500 end
            AuroraPreferenceCategory mmsSettingCategory =
                (AuroraPreferenceCategory)findPreference("pref_key_mms_settings");
            mMmsReadReportMultiSim = findPreference(READ_REPORT_MODE);
            mMmsAutoReplyReadReportMultiSim = findPreference(READ_REPORT_AUTO_REPLY);
            mmsSettingCategory.removePreference(mMmsReadReportMultiSim);
            mmsSettingCategory.removePreference(mMmsAutoReplyReadReportMultiSim);
        } else {
            //gionee gaoj 2012-5-27 added for CR00608337 end
        mMmsReadReportMultiSim = findPreference(READ_REPORT_MODE);
        // M: add this for read report
        mMmsAutoReplyReadReportMultiSim = findPreference(READ_REPORT_AUTO_REPLY);
        //gionee gaoj 2012-5-27 added for CR00608337 start
        }
        //gionee gaoj 2012-5-27 added for CR00608337 end
        mMmsAutoRetrievalMultiSim = findPreference(AUTO_RETRIEVAL);
        mMmsRetrievalDuringRoamingMultiSim = findPreference(RETRIEVAL_DURING_ROAMING);
        mSmsServiceCenterPrefMultiSim = findPreference(SMS_SERVICE_CENTER);
        mManageSimPrefMultiSim = findPreference(SMS_MANAGE_SIM_MESSAGES);
        mManageSimPref = null;
        //MTK_OP02_PROTECT_START
        AuroraPreferenceCategory smsCategory =
            (AuroraPreferenceCategory)findPreference("pref_key_sms_settings");
        //MTK_OP02_PROTECT_START 
        //Gionee liuxiangrong 2012-10-16 add for CR00714584 start
        if (MmsApp.isUnicomOperator() || gnQMflag) {
            //gionee gaoj 2012-5-31 modified for CR00608428 start
            /*if (mManageSimPrefMultiSim != null) {
                smsCategory.removePreference(mManageSimPrefMultiSim);
            }*/
            //gionee gaoj 2012-5-31 modified for CR00608428 start
            if(gnQMflag){
                if (mManageSimPrefMultiSim != null) {
                    smsCategory.removePreference(mManageSimPrefMultiSim);
                }
            }
        //Gionee liuxiangrong 2012-10-16 add for CR00714584 end
             //GiONEE liuxiangrong 2012-07-20 modify for CR00652132 start(yuanqingqing merged 20120908 in CR00688212)
            if (MmsApp.isUnicomOperator()){
                if (mSmsLocation != null) {
                    smsCategory.removePreference(mSmsLocation);
                    AuroraPreference saveLocationMultiSim = new AuroraPreference(this);
                    saveLocationMultiSim.setKey(SMS_SAVE_LOCATION);
                    saveLocationMultiSim.setTitle(R.string.sms_save_location);
                    saveLocationMultiSim.setSummary(R.string.sms_save_location);
                    smsCategory.addPreference(saveLocationMultiSim);
                    mSmsSaveLoactionMultiSim = findPreference(SMS_SAVE_LOCATION);
               }
            }
            //GiONEE liuxiangrong 2012-07-20 modify for CR00652132 end (yuanqingqing merged 20120908 in CR00688212)
        }
        //MTK_OP02_PROTECT_END
        mCellBroadcastMultiSim = findPreference(CELL_BROADCAST);
    }

    private void setEnabledNotificationsPref() {
        // The "enable notifications" setting is really stored in our own prefs. Read the
        // current value and set the checkbox to match.
        mEnableNotificationsPref.setChecked(getNotificationEnabled(this));
    }

    private void setSmsDisplayLimit() {
        mSmsLimitPref.setSummary(
                getString(R.string.pref_summary_delete_limit,
                        mSmsRecycler.getMessageLimit(this)));
    }

    private void setMmsDisplayLimit() {
        mMmsLimitPref.setSummary(
                getString(R.string.pref_summary_delete_limit,
                        mMmsRecycler.getMessageLimit(this)));
    }
    
    // Aurora liugj 2013-09-17 modified for aurora's new feature start
    /*public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
        menu.clear();
        menu.add(0, MENU_RESTORE_DEFAULTS, 0, R.string.restore_default);

        return super.onCreateOptionsMenu(menu);
//        return true;
    }*/

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESTORE_DEFAULTS:
                restoreDefaultPreferences();
                // gionee zhouyj 2012-12-21 add for CR00745926 start 
                updateSetEncrypView();
                // gionee zhouyj 2012-12-21 add for CR00745926 end 
//                return true;

            case android.R.id.home:
                // The user clicked on the Messaging icon in the action bar. Take them back from
                // wherever they came from
                finish();
//                return true;
        }
//        return false;
        return super.onOptionsItemSelected(item);
    }*/
    // Aurora liugj 2013-09-17 modified for aurora's new feature end

    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen,
            AuroraPreference preference) {
        if (preference == mStorageStatusPref) {
            final String memoryStatus = MessageUtils.getStorageStatus(getApplicationContext());
            /*new AuroraAlertDialog.Builder(MessagingPreferenceActivity.this)
                    .setTitle(R.string.pref_title_storage_status)
                    .setIcon(R.drawable.ic_dialog_info_holo_light)
                    .setMessage(memoryStatus)
                    .setPositiveButton(android.R.string.ok, null)
                    .setCancelable(true)
                    .show();*/
        } else if (preference == mSmsLimitPref) {
            //gionee gaoj 2012-6-1 added for CR00614080 start
            if (MmsApp.mDarkStyle) {
                mSmsDisplayLimitDialog = 
                    new NumberPickerDialog(this,
                            /*AuroraAlertDialog.THEME_AMIGO_FULLSCREEN,*/
                            mSmsLimitListener,
                            mSmsRecycler.getMessageLimit(this),
                            mSmsRecycler.getMessageMinLimit(),
                            mSmsRecycler.getMessageMaxLimit(),
                            R.string.pref_title_sms_delete);
            } else {
                //gionee gaoj 2012-6-1 added for CR00614080 end
            mSmsDisplayLimitDialog = 
            new NumberPickerDialog(this,
                    /*AuroraAlertDialog.THEME_AMIGO_FULLSCREEN,*/
                    mSmsLimitListener,
                    mSmsRecycler.getMessageLimit(this),
                    mSmsRecycler.getMessageMinLimit(),
                    mSmsRecycler.getMessageMaxLimit(),
                    R.string.pref_title_sms_delete);
            //gionee gaoj 2012-6-1 added for CR00614080 start
            }
            //gionee gaoj 2012-6-1 added for CR00614080 end
            mSmsDisplayLimitDialog.show();
        } else if (preference == mMmsLimitPref) {
            //gionee gaoj 2012-6-1 added for CR00614080 start
            if (MmsApp.mDarkStyle) {
                mMmsDisplayLimitDialog = 
                    new NumberPickerDialog(this,
                            /*AuroraAlertDialog.THEME_AMIGO_FULLSCREEN,*/
                            mMmsLimitListener,
                            mMmsRecycler.getMessageLimit(this),
                            mMmsRecycler.getMessageMinLimit(),
                            mMmsRecycler.getMessageMaxLimit(),
                            R.string.pref_title_mms_delete);
            } else {
                //gionee gaoj 2012-6-1 added for CR00614080 end
            mMmsDisplayLimitDialog = 
            new NumberPickerDialog(this,
                    /*AuroraAlertDialog.THEME_AMIGO_FULLSCREEN,*/
                    mMmsLimitListener,
                    mMmsRecycler.getMessageLimit(this),
                    mMmsRecycler.getMessageMinLimit(),
                    mMmsRecycler.getMessageMaxLimit(),
                    R.string.pref_title_mms_delete);
            //gionee gaoj 2012-6-1 added for CR00614080 start
            }
            //gionee gaoj 2012-6-1 added for CR00614080 end
            mMmsDisplayLimitDialog.show();
        } else if (preference == mManageSimPref) {
            if(MmsApp.mGnMultiSimMessage == true){
                // Aurora xuyong 2014-05-31 deleted for multisim feature start
                /*listSimInfo = SIMInfo.getInsertedSIMList(this);
                int slotId = listSimInfo.get(0).mSlot;
                Log.d(TAG, "slotId is : " + slotId);
                if (slotId != -1) {
                    Intent it = new Intent();
                        // Aurora liugj 2013-09-25 modified for aurora's new feature start
                    it.setClass(this, AuroraManageSimMessages.class);
                        // Aurora liugj 2013-09-25 modified for aurora's new feature end
                    it.putExtra("SlotId", slotId);
                    startActivity(it);
                }*/
                // Aurora xuyong 2014-05-31 deleted for multisim feature end
             // Aurora xuyong 2014-05-31 added for multisim feature start
                Intent intent = new Intent();
                if (SIMInfo.getInsertedSIMCount(this) == 1) {
                    int slotId = SIMInfo.getInsertedSIMList(this).get(0).mSlot;
                    if (slotId != -1) {
                        intent.putExtra("SlotId", slotId);
                   // Aurora xuyong 2014-06-03 deleted for multisim feature start
                        //intent.setClass(this, AuroraMultiSimManageActivity.class);
                        //startActivity(intent);
                   // Aurora xuyong 2014-06-03 deleted for multisim feature end
                    }
                }                      
                intent.setClass(this, AuroraMultiSimManageActivity.class);
                startActivity(intent);
             // Aurora xuyong 2014-05-31 added for multisim feature end
            } else {
                    // Aurora liugj 2013-09-25 modified for aurora's new feature start
                startActivity(new Intent(this, AuroraManageSimMessages.class));
                    // Aurora liugj 2013-09-25 modified for aurora's new feature end
            }
        } else if (preference == mClearHistoryPref) {
            showDialog(CONFIRM_CLEAR_SEARCH_HISTORY_DIALOG);
            return true;
        } else if (preference == mSmsQuickTextEditorPref) {
            Intent intent = new Intent();
            intent.setClass(this, SmsTemplateEditActivity.class);
            startActivity(intent);
        } else if (preference == mSmsDeliveryReportMultiSim 
                || preference == mMmsDeliveryReportMultiSim
                || preference == mMmsEnableToSendDeliveryReportMultiSim
                || preference == mMmsReadReportMultiSim 
                // M: add this for read report
                || preference == mMmsAutoReplyReadReportMultiSim
                || preference == mMmsAutoRetrievalMultiSim 
                || preference == mMmsRetrievalDuringRoamingMultiSim) {
            
            Intent it = new Intent();
            it.setClass(this, MultiSimPreferenceActivity.class);
            it.putExtra("preference", preference.getKey());
            startActivity(it);
        } else if (preference == mSmsServiceCenterPref) {
            AuroraAlertDialog.Builder dialog = new AuroraAlertDialog.Builder(this/*, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN*/);

            LayoutInflater factory = LayoutInflater.from(this);
                // Aurora liugj 2013-10-11 modified for aurora's new feature start
            final View textEntryView = factory.inflate(R.layout.aurora_add_quick_text_dialog, null);
                // Aurora liugj 2013-10-11 modified for aurora's new feature end
            mNumberText = (AuroraEditText) textEntryView.findViewById(R.id.add_quick_text_content);
            mNumberText.setHint(R.string.type_to_compose_text_enter_to_send);
            mNumberText.computeScroll();
            mNumberText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_EDITABLE_LENGTH)});
            //mNumberText.setKeyListener(new DigitsKeyListener(false, true));
            mNumberText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_CLASS_PHONE);
            mTelephonyManager = GnTelephonyManagerEx.getDefault();
            String gotScNumber;
            if(MmsApp.mGnMultiSimMessage == true){
                int slotId = listSimInfo.get(0).mSlot;
                gotScNumber = mTelephonyManager.getScAddress(slotId);
                // gionee zhouyj 2013-03-29 modify for CR00790884 start
                if (MmsApp.mQcMultiSimEnabled) {
                    //Gionee guoyx 20130309 modified for CR00780148 begin
                    if (gotScNumber != null && !"".equals(gotScNumber)) {
                        Log.d(TAG, "getServiceCenter is:" + gotScNumber + " before substring.");
                        int index = gotScNumber.lastIndexOf("\"");
                        gotScNumber = gotScNumber.substring(1, index);
                    } else {
                        Log.e(TAG, "getServiceCenter is: fail !");
                    }
                    //Gionee guoyx 20130309 modified for CR00780148 end
                }
                // gionee zhouyj 2013-03-29 modify for CR00790884 end
            } else {
                gotScNumber = mTelephonyManager.getScAddress(0);
            }
            Log.d(TAG, "gotScNumber is: " + gotScNumber);
            mNumberText.setText(gotScNumber);
            mNumberTextDialog = dialog
            //Gionee <Gaoj><2013-05-06> delete for CR00808328 begin
            /*.setIcon(R.drawable.ic_dialog_info_holo_light)*/
            //Gionee <Gaoj><2013-05-06> delete for CR00808328 end
            .setTitle(R.string.sms_service_center)
            .setView(textEntryView)
            .setPositiveButton(R.string.OK, new PositiveButtonListener())
            .setNegativeButton(R.string.Cancel, new NegativeButtonListener())
            .show();
        } else if (preference == mSmsServiceCenterPrefMultiSim
                || preference == mManageSimPrefMultiSim
                || preference == mCellBroadcastMultiSim
                ||(preference == mSmsSaveLoactionMultiSim && currentSimCount > 1
                        && MmsApp.isUnicomOperator())) {
            Intent it = new Intent();
            it.setClass(this, SelectCardPreferenceActivity.class);
            it.putExtra("preference", preference.getKey());
            startActivity(it);
        } else if (preference == mEnableNotificationsPref) {
            // Update the actual "enable notifications" value that is stored in secure settings.
            enableNotifications(mEnableNotificationsPref.isChecked(), this);
        } else if(preference == mImportMessages){
            //importMessages
            Intent it = new Intent();
            it.setClass(this, ImportSmsActivity.class);
            startActivity(it);
        } else if(preference == mExportMessages){
            showDialog(EXPORT_SMS);
        } else if(preference == mCBsettingPref){
             listSimInfo = SIMInfo.getInsertedSIMList(this);
             if(listSimInfo != null && listSimInfo.isEmpty()){
                 Log.d(TAG, "there is no sim card");
                 return true;
             }
             int slotId = listSimInfo.get(0).mSlot;
             Log.d(TAG, "mCBsettingPref slotId is : " + slotId);
             Intent it = new Intent();
             // gionee zhouyj 2012-11-26 modify for CR00735999 start 
             if (MmsApp.mIsSupportPlatform_4_1) {
                 it.setClassName("com.android.phone", "com.mediatek.settings.CellBroadcastActivity");
             } else {
                 it.setClassName("com.android.phone", "com.android.phone.CellBroadcastActivity");
             }
             // gionee zhouyj 2012-11-26 modify for CR00735999 end 
             it.setAction(Intent.ACTION_MAIN);
             it.putExtra(GnPhone.GEMINI_SIM_ID_KEY, slotId);
             it.putExtra(SUB_TITLE_NAME, SIMInfo.getSIMInfoBySlot(this, slotId).mDisplayName);
             startActivity(it);
        }
        //Gionee zengxuanhui 20120809 add for CR00672106 begin
        else if(gnGeminiRingtoneSupport && NOTIFICATION_RINGTONE.equals(preference.getKey())){
            Intent intent = new Intent();
            intent.setClass(this, com.gionee.mms.ui.GnRingtonePreference.class);
            startActivity(intent);
        }
        //Gionee zengxuanhui 20120809 add for CR00672106 end
        
        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (MmsApp.mEncryption) {
            if (preference == mSetEncryPref) {
                if (Conversation.getFirstEncryption() == true) {
                    inputencryption();
                } else {
                    inputdecryption();
                }
            }
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
        // gionee zhouyj 2012-05-17 add for CR00601523 start
        if(MmsApp.mGnMessageSupport && preference == mEditSignaturePref) {
            editNewSignature();
        }
        // gionee zhouyj 2012-05-17 add for CR00601523 end
        if (preference == mRestoreDefault) {
            restoreDefaultPreferences();
            updateSetEncrypView();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private class PositiveButtonListener implements OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            // write to the SIM Card.
            mTelephonyManager = GnTelephonyManagerEx.getDefault();
            if(MmsApp.mGnMultiSimMessage == true){
                slotId = listSimInfo.get(0).mSlot;
            } else {
                slotId = 0;
            }
            new Thread(new Runnable() {
                public void run() {
                    mTelephonyManager.setScAddress(mNumberText.getText().toString(), slotId);
                }
            }).start();
        }
    }

    private class NegativeButtonListener implements OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            // cancel
            dialog.dismiss();
        }
    }
    private void restoreDefaultPreferences() {
        AuroraPreferenceManager.getDefaultSharedPreferences(this)
                .edit().clear().apply();
        // Gionee fangbin 20120709 added for CR00637267 start
        if (android.os.SystemProperties.get("ro.gn.settings.prop").equals("yes")) {
            //gionee zengxuanhui 20120815 modify for CR00673549 begin
            // Gionee fangbin 20120713 modified for CR00647095 start
            String defaultUri = "";
            // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
            //defaultUri = Settings.System.getString(getContentResolver(), AudioProfileManager.KEY_DEFAULT_MMS);
            // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
            // Gionee fangbin 20120713 modified for CR00647095 end
            SharedPreferences sp = AuroraPreferenceManager.getDefaultSharedPreferences(this);
            sp.edit().putString(NOTIFICATION_RINGTONE, defaultUri).commit();
            //Gionee zengxuanhui 20121009 modify for CR00704782 begin
            // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
            /*if(mProfileManager != null){
                Uri uri1 = (defaultUri == null ? null : Uri.parse(defaultUri));
                mProfileManager.setRingtoneUri(mProfileManager.getActiveProfileKey(), 
                        AudioProfileManager.TYPE_MMS, uri1);
            }*/
            // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
            if(gnGeminiRingtoneSupport){
                String defaultUri2 = "";
                // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
                //defaultUri2 = Settings.System.getString(getContentResolver(), AudioProfileManager.KEY_DEFAULT_MMS2);
                // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
                sp.edit().putString(NOTIFICATION_RINGTONE2, defaultUri2).commit();
                // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
                /*if(mProfileManager != null){
                    Uri uri2 = (defaultUri2 == null ? null : Uri.parse(defaultUri2));
                    mProfileManager.setRingtoneUri(mProfileManager.getActiveProfileKey(), 
                            AudioProfileManager.TYPE_MMS2, uri2);
                }*/
                // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
            }
            //Gionee zengxuanhui 20121009 modify for CR00704782 begin
            //gionee zengxuanhui 20120815 modify for CR00673549 end
            sendMmsRingtoneReceiver();
        }
        // Gionee fangbin 20120709 added for CR00637267 end
        setPreferenceScreen(null);
        setMessagePreferences();
        setListPrefSummary();
        //gionee gaoj 2012-6-6 added for CR00614279 start
        if (MmsApp.mGnMessageSupport) {
            // gionee zhouyj 2013-04-04 add for CR00793394 start 
            if (mPhotoOptionsEntrieValues != null && mPhotoOptionsEntrieValues.length > 1) {
                Contact.setContactPhotoOptions(Integer.valueOf(mPhotoOptionsEntrieValues[0]));
            }
            // gionee zhouyj 2013-04-04 add for CR00793394 end 
            WriteIgnaturePref(null);
        }
        //gionee gaoj 2012-6-6 added for CR00614279 end
    }

    NumberPickerDialog.OnNumberSetListener mSmsLimitListener =
        new NumberPickerDialog.OnNumberSetListener() {
            public void onNumberSet(int limit) {
                if (limit <= mSmsRecycler.getMessageMinLimit()){
                    limit = mSmsRecycler.getMessageMinLimit();
                }else if( limit >= mSmsRecycler.getMessageMaxLimit()) {
                    limit = mSmsRecycler.getMessageMaxLimit();
                }
                mSmsRecycler.setMessageLimit(MessagingPreferenceActivity.this, limit);
                setSmsDisplayLimit();
                mSMSHandler.post(new Runnable() {
                    public void run() {
                        new Thread(new Runnable() {
                            public void run() {
                               Recycler.getSmsRecycler().deleteOldMessages(getApplicationContext());
                               if (FeatureOption.MTK_WAPPUSH_SUPPORT) {
                                   Recycler.getWapPushRecycler().deleteOldMessages(getApplicationContext());
                               }
                             }
                        }, "DeleteSMSOldMsgAfterSetNum").start();
                    }
                });
            }
    };

    NumberPickerDialog.OnNumberSetListener mMmsLimitListener =
        new NumberPickerDialog.OnNumberSetListener() {
            public void onNumberSet(int limit) {
                if (limit <= mMmsRecycler.getMessageMinLimit()){
                    limit = mMmsRecycler.getMessageMinLimit();
                }else if( limit >= mMmsRecycler.getMessageMaxLimit()) {
                    limit = mMmsRecycler.getMessageMaxLimit();
                } 
                mMmsRecycler.setMessageLimit(MessagingPreferenceActivity.this, limit);
                setMmsDisplayLimit();
                mMMSHandler.post(new Runnable() {
                    public void run() {
                        new Thread(new Runnable() {
                            public void run() {
                                Log.d("Recycler", "mMmsLimitListener");
                                Recycler.getMmsRecycler().deleteOldMessages(getApplicationContext());                            
                            } 
                        }, "DeleteMMSOldMsgAfterSetNum").start();
                    }
                });
            }
    };

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case CONFIRM_CLEAR_SEARCH_HISTORY_DIALOG:
                return new AuroraAlertDialog.Builder(MessagingPreferenceActivity.this)
                    .setTitle(R.string.confirm_clear_search_title)
                    .setMessage(R.string.confirm_clear_search_text)
                    .setPositiveButton(android.R.string.ok, new AuroraAlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SearchRecentSuggestions recent =
                                ((MmsApp)getApplication()).getRecentSuggestions();
                            if (recent != null) {
                                recent.clearHistory();
                            }
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .create();
            case EXPORT_SMS:  
                return new AuroraAlertDialog.Builder(this)
                .setMessage(getString(R.string.whether_export_item))
                .setTitle(R.string.pref_summary_export_msg).setPositiveButton(
                        android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                exportMessages();
                                return;
                            }
                        }).setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                            }
                        }).create();
        }
        return super.onCreateDialog(id);
    }

    public static boolean getNotificationEnabled(Context context) {
        SharedPreferences prefs = AuroraPreferenceManager.getDefaultSharedPreferences(context);
        boolean notificationsEnabled =
            prefs.getBoolean(MessagingPreferenceActivity.NOTIFICATION_ENABLED, true);
        return notificationsEnabled;
    }

    public static void enableNotifications(boolean enabled, Context context) {
        // Store the value of notifications in SharedPreferences
        SharedPreferences.Editor editor =
            AuroraPreferenceManager.getDefaultSharedPreferences(context).edit();

        editor.putBoolean(MessagingPreferenceActivity.NOTIFICATION_ENABLED, enabled);

        editor.apply();
    }
    /*
     * Notes: if wap push is not support, wap push setting should be removed
     * 
     */
    private void enablePushSetting(){
        
        AuroraPreferenceCategory wapPushOptions =
            (AuroraPreferenceCategory)findPreference("pref_key_wappush_settings");
        
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){  
            if(!MmsConfig.getSlAutoLanuchEnabled()){
                wapPushOptions.removePreference(findPreference("pref_key_wappush_sl_autoloading"));
            }
        }else{
            getPreferenceScreen().removePreference(wapPushOptions);
        }
    }

    @Override
    public boolean onPreferenceChange(AuroraPreference arg0, Object arg1) {
        final String key = arg0.getKey();
        int slotId = 0;
        if (MmsApp.mGnMultiSimMessage == true &&
                MmsApp.isUnicomOperator()) {
            int currentSimCount = SIMInfo.getInsertedSIMCount(this);
            if (currentSimCount == 1){
                slotId = SIMInfo.getInsertedSIMList(this).get(0).mSlot;
            }
        }
        String stored = (String)arg1;
        if (PRIORITY.equals(key)) {
            mMmsPriority.setSummary(getVisualTextName(stored, R.array.pref_key_mms_priority_choices,
                    R.array.pref_key_mms_priority_values));
        } else if (CREATION_MODE.equals(key)) {
            mMmsCreationMode.setSummary(getVisualTextName(stored, R.array.pref_mms_creation_mode_choices,
                    R.array.pref_mms_creation_mode_values));
            mMmsCreationMode.setValue(stored);
            WorkingMessage.updateCreationMode(this);
        } else if (MMS_SIZE_LIMIT.equals(key)) {
            mMmsSizeLimit.setSummary(getVisualTextName(stored, R.array.pref_mms_size_limit_choices,
                    R.array.pref_mms_size_limit_values));
            MmsConfig.setUserSetMmsSizeLimit(Integer.valueOf(stored));
            
        } else if (SMS_SAVE_LOCATION.equals(key) && !(currentSimCount > 1 &&
                MmsApp.isUnicomOperator())) {
            mSmsLocation.setSummary(getVisualTextName(stored, R.array.pref_sms_save_location_choices,
                    R.array.pref_sms_save_location_values));
        } else if((Long.toString(slotId) + "_" + SMS_SAVE_LOCATION).equals(key)){
            mSmsLocation.setSummary(getVisualTextName(stored, R.array.pref_sms_save_location_choices,
                    R.array.pref_sms_save_location_values));
        // Aurora xuyong 2014-08-04 added for sms center feature start
        } else if (AURORA_SIM1_SMS_CENTER.equals(key)) {
            if (mSim1SmsCenterPreference != null) {
                final String newSmsCenterNumber1 = stored;
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
            if (mSim2SmsCenterPreference != null) {
                final String newSmsCenterNumber2 = stored;
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
        }
        //gionee gaoj 2013-2-19 adde for CR00771935 start
        if (MmsApp.mGnMessageSupport && PHOTO_STYLE_KEY.equals(key)) {
            CharSequence summary = null;
            if (null != mPhotoOptionsEntries &&
                    null != mPhotoOptionsEntrieValues &&
                        mPhotoOptionsEntrieValues.length == mPhotoOptionsEntries.length) {
                for (int i = 0; i < mPhotoOptionsEntrieValues.length; i++) {
                    if (mPhotoOptionsEntrieValues[i].equals(arg1)) {
                        summary = mPhotoOptionsEntries[i]; 
                        break;
                    }
                }
            
                arg0.setSummary(summary);
            }
            
            Contact.setContactPhotoOptions(Integer.valueOf(stored));
        }
        //gionee gaoj 2013-2-19 adde for CR00771935 end
        return true;
    }
    private CharSequence getVisualTextName(String enumName, int choiceNameResId, int choiceValueResId) {
        CharSequence[] visualNames = getResources().getTextArray(
                choiceNameResId);
        CharSequence[] enumNames = getResources().getTextArray(
                choiceValueResId);

        // Sanity check
        if (visualNames.length != enumNames.length) {
            return "";
        }

        for (int i = 0; i < enumNames.length; i++) {
            if (enumNames[i].equals(enumName)) {
                return visualNames[i];
            }
        }
        return "";
    }

    private boolean exportMessages(){
        Log.d(TAG,"exportMessages");
        if(!isSDcardReady()){
            return false;
        }
        progressdialog = AuroraProgressDialog.show(this, "", getString(R.string.export_message_ongoing), true); 
        new Thread() {
            public void run() {
                Cursor cursor = null;
                int quiteCode = 0;
                String storeFileName = "";
                try { 
                    // Gionee zhangxx 2012-04-02 add for CR00556294 begin
                    File dir = null;
                    String storageDirectory = getDefaultStorePath();
                    if (MmsApp.mGnEMMCSupport && storageDirectory != null && storageDirectory.equals("/mnt/sdcard2")) {
                        dir = new File(SDCARD2_DIR_PATH);
                    } else {
                        dir = new File(SDCARD_DIR_PATH);
                    }
                    // Gionee zhangxx 2012-04-02 add for CR00556294 end
                    if (!dir.exists()) {
                        dir.mkdir();
                    }
                    // Gionee zhangxx 2012-04-02 add for CR00556294 begin
                    if (MmsApp.mGnEMMCSupport && storageDirectory != null && storageDirectory.equals("/mnt/sdcard2")) {
                        storeFileName = getAppropriateFileName(SDCARD2_DIR_PATH);
                    } else {
                        storeFileName = getAppropriateFileName(SDCARD_DIR_PATH);
                    }
                    // Gionee zhangxx 2012-04-02 add for CR00556294 end
                    if (null == storeFileName) {
                        Log.w(TAG, "exportMessages sms file name is null");
                        return;
                    }
                    cursor = getContentResolver().query(SMS_URI, SMS_COLUMNS, null, null, null);
                    if (cursor == null || cursor.getCount() == 0) {
                        Log.w(TAG, "exportMessages query sms cursor is null");
                        quiteCode = EXPORT_EMPTY_SMS;
                        return;
                    }
                    Log.d(TAG, "exportMessages query sms cursor count is "+cursor.getCount());
                    int exportCount = copyToPhoneMemory(cursor, MEM_DIR_PATH);
                    if (exportCount >0){
                         copyToSDMemory(MEM_DIR_PATH, storeFileName);
                         mMainHandler.sendEmptyMessage(EXPORT_SUCCES);
                         Log.d(TAG, "ExportDict success");
                    } else {
                          Log.d(TAG, "ExportDict failure there is no message to export");
                          quiteCode = EXPORT_EMPTY_SMS;
                    }
                } catch (SQLiteDiskIOException e) {
                    mMainHandler.sendEmptyMessage(DISK_IO_FAILED);
                    //if the file is created, erase it
                    File file = new File(storeFileName);
                    if (file.exists()) {
                        file.delete();
                    } 
                    e.printStackTrace();
                } catch (Exception e) {
                    Log.e(TAG, "exportMessages can't create the database file");
                    //if the file is created, erase it
                    File file = new File(storeFileName);
                    if (file.exists()) {
                        file.delete();
                    } 
                    mMainHandler.sendEmptyMessage(EXPORT_FAILED);
                    e.printStackTrace();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                    File file = new File(MEM_DIR_PATH);
                    if (file.exists()) {
                      file.delete();
                    } 
                    if (null != progressdialog) {
                        progressdialog.dismiss();
                    } 
                    if(quiteCode == EXPORT_EMPTY_SMS){
                        mMainHandler.sendEmptyMessage(EXPORT_EMPTY_SMS);
                    }
                } 
            }
        }.start();
        return true;
    } 
    
    /**
     * Tries to get an appropriate filename. Returns null if it fails.
     */
    private String getAppropriateFileName(final String destDirectory) {
        //get max number of  digital
        int fileNumberStringLength = 0;
        {
            int tmp;
            for (fileNumberStringLength = 0, tmp = MAX_FILE_NUMBER; tmp > 0;
                fileNumberStringLength++, tmp /= 10) {
            }
        }
        String bodyFormat = "%s%0" + fileNumberStringLength + "d%s";
        for (int i = MIN_FILE_NUMBER; i <= MAX_FILE_NUMBER; i++) {
            boolean isExitFile = false;
            String body = String.format(bodyFormat, mFileNamePrefix, i, mFileNameSuffix);
            String fileName = String.format("%s%s.%s", destDirectory, body, mFileNameExtension);
            File file = new File(fileName);
            if (file.exists()) {
                isExitFile = true;
            } 
            if (!isExitFile){
                Log.w(TAG, "exportMessages getAppropriateFileName fileName =" + fileName);
                return fileName;
            }
        }
        return null;
    }
    
    private int copyToPhoneMemory(Cursor cursor, String dest){
        SQLiteDatabase db =  openOrCreateDatabase(dest, 1, null);
        // Aurora xuyong 2015-01-04 modified for bug #10832 start
        db.beginTransaction();
        Log.d(TAG, "export mem begin");
        int count = 0;
        try {
            db.execSQL("CREATE TABLE sms ("
                    + "_id INTEGER PRIMARY KEY,"
                    + "thread_id INTEGER,"
                    + "address TEXT,"
                    + "m_size INTEGER,"
                    + "person INTEGER,"
                    + "date INTEGER,"
                    + "date_sent INTEGER DEFAULT 0,"
                    + "protocol INTEGER,"
                    + "read INTEGER DEFAULT 0,"
                    + "status INTEGER DEFAULT -1,"
                    + "type INTEGER," + "reply_path_present INTEGER,"
                    + "subject TEXT," + "body TEXT," + "service_center TEXT,"
                    + "locked INTEGER DEFAULT 0," + Sms.SIM_ID + " INTEGER DEFAULT -1,"
                    + "error_code INTEGER DEFAULT 0," + "seen INTEGER DEFAULT 0"
                    + ");");
            while (cursor.moveToNext()) {
                int messageType = cursor.getInt(cursor.getColumnIndexOrThrow("type"));
                if (messageType == 3) {
                    continue;
                }
                ContentValues smsValue = new ContentValues();
                int threadId = cursor.getInt(cursor.getColumnIndexOrThrow("thread_id"));
                String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                Long date = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
                int simId = cursor.getInt(cursor.getColumnIndexOrThrow(Sms.SIM_ID));
                int read = cursor.getInt(cursor.getColumnIndexOrThrow("read")); 
                int seen = cursor.getInt(cursor.getColumnIndexOrThrow("seen")); 
                String serviceCenter = cursor.getString(cursor.getColumnIndexOrThrow("service_center"));
                smsValue.put(Sms.READ, read);
                smsValue.put(Sms.SEEN, seen);
                smsValue.put(Sms.BODY, body);
                smsValue.put(Sms.DATE, date);
                smsValue.put(Sms.SIM_ID, simId);
                smsValue.put(Sms.SERVICE_CENTER, serviceCenter);
                smsValue.put(Sms.TYPE, messageType);
                smsValue.put(Sms.ADDRESS, address);
                db.insert(TABLE_SMS, null, smsValue);
                count++;
            } 
            Log.d(TAG, "export mem end count = " + count);
            db.setTransactionSuccessful();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();   
        }
        // Aurora xuyong 2015-01-04 modified for bug #10832 end
        return count;
   }

    private void copyToSDMemory(String src, String dst) throws Exception{
        Log.d(TAG, "export sdcard begin dst = "  +dst);
        InputStream myInput = null;
        OutputStream myOutput = null;
        try {
            myInput = new FileInputStream(src);
            // Gionee zhangxx 2012-04-02 add for CR00556294 begin
            File dir = null;
            String storageDirectory = getDefaultStorePath();
            if (MmsApp.mGnEMMCSupport && storageDirectory != null && storageDirectory.equals("/mnt/sdcard2")) {
                dir = new File(SDCARD2_DIR_PATH);
            } else {
                dir = new File(SDCARD_DIR_PATH);
            }
            // Gionee zhangxx 2012-04-02 add for CR00556294 end
            if (!dir.exists()) {
                dir.mkdir();
            }
            File dstFile = new File(dst);
            if (!dstFile.exists()){
                dstFile.createNewFile();
            }
            myOutput = new FileOutputStream(dstFile);
             //transfer bytes from the inputfile to the outputfile
             byte[] buffer = new byte[1024];
             int length;
             while ((length = myInput.read(buffer))>0){
                 myOutput.write(buffer, 0, length);
             }
             myOutput.flush();
             myOutput.close();
             myInput.close();
        } catch (FileNotFoundException e){
            e.printStackTrace();
            Log.e(TAG, "export sdcard FileNotFoundException");
        } catch (IOException e){
            Log.e(TAG, "export sdcard IOException");
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "export sdcard end");
    }
    
    private boolean isSDcardReady(){
        boolean isSDcard = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED); 
        if (!isSDcard) {
            showToast(R.string.no_sd_card);
            Log.d(TAG, "there is no SD card");
            return false;
        }
        return true;
    }
    
    private void showToast(int id) { 
        Toast t = Toast.makeText(getApplicationContext(), getString(id), Toast.LENGTH_SHORT);
        t.show();
    } 
    
    // Gionee zhangxx 2012-04-02 add for CR00556294 begin
    private String getDefaultStorePath() {
        StorageManager storageManager = null;
        storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        // Aurora xuyong 2013-11-15 modified for S4 adapt start
       // Aurora xuyong 2014-04-18 modified for bug #4361 start
        return gionee.os.storage.GnStorageManager.getInstance(this).getDefaultPath_ex();
       // Aurora xuyong 2014-04-18 modified for bug #4361 end
        // Aurora xuyong 2013-11-15 modified for S4 adapt end
    }
    // Gionee zhangxx 2012-04-02 add for CR00556294 end
    //gionee gaoj 2012-4-10 added for CR00555790 start
    private void inputencryption() {
        DevicePolicyManager DPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        int quality = DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC;
        int minQuality = DPM.getPasswordQuality(null);
        if (quality < minQuality) {
            quality = minQuality;
        }
        if (quality >= DevicePolicyManager.PASSWORD_QUALITY_NUMERIC) {
            int minLength = DPM.getPasswordMinimumLength(null);
            if (minLength < MIN_PASSWORD_LENGTH) {
                minLength = MIN_PASSWORD_LENGTH;
            }
            final int maxLength = DPM.getPasswordMaximumLength(quality);
            Intent intent = new Intent(this, MsgChooseLockPassword.class);
            intent.putExtra(LockPatternUtils.PASSWORD_TYPE_KEY, quality);
            intent.putExtra(MsgChooseLockPassword.PASSWORD_MIN_KEY, minLength);
            intent.putExtra(MsgChooseLockPassword.PASSWORD_MAX_KEY, maxLength);
            startActivityForResult(intent, ConvFragment.UPDATE_PASSWORD_REQUEST);
        }
    }

    private void inputdecryption(){
        final Intent intent = new Intent();
        intent.setClass(this, MsgChooseLockPassword.class);
        intent.putExtra("isdecryption", true);
        this.startActivityForResult(intent, ConvFragment.CONFIRM_PASSWORD_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        switch (requestCode) {
            case ConvFragment.UPDATE_PASSWORD_REQUEST:
                if (data != null && data.getAction().equals("succeed")) {
                    Conversation.setFirstEncryption(false);
                    updateSetEncrypView();
                    Toast.makeText(this, R.string.pref_summary_mms_password_setting_succeed, Toast.LENGTH_SHORT).show();
                }
                break;
            case ConvFragment.CONFIRM_PASSWORD_REQUEST:
                if (data != null && data.getAction().equals("confirm")) {
                    //mIsConfirm = true;
                    Intent it = new Intent();
                    it.setClass(this, SecuritySettingActivity.class);
                    startActivity(it);
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void updateSetEncrypView() {
        if (Conversation.getFirstEncryption() == true) {
            mSetEncryPref.setTitle(R.string.pref_title_mms_encryption_setting);
            mSetEncryPref.setSummary(R.string.pref_summary_mms_encryption_setting);
        } else {
            mSetEncryPref.setTitle(R.string.pref_title_mms_encryption_change);
            mSetEncryPref.setSummary(R.string.pref_summary_mms_encryption_change);
        }
        // gionee zhouyj 2013-04-09 add for CR00795077 start
        mSetEncryPref.setEnabled(!MmsApp.mIsSafeModeSupport);
        // gionee zhouyj 2013-04-09 add for CR00795077 end
    }
    //gionee gaoj 2012-4-10 added for CR00555790 end
    
    // gionee zhouyj 2012-05-17 add for CR00601523 start
    // gionee zhouyj 2012-12-12 add for CR00741770 start 
    private void editNewSignature() {
        LayoutInflater inflater = LayoutInflater.from(MessagingPreferenceActivity.this);
        final View view = inflater.inflate(R.layout.gn_edit_signature, null);
        final AuroraEditText quickText = (AuroraEditText) view.findViewById(R.id.gn_signature_content);
        String text = ReadIgnaturePref();
        quickText.setText(text);
        quickText.addTextChangedListener(mTextWatcher);
        mEditDialog = new AuroraAlertDialog.Builder(MessagingPreferenceActivity.this/*, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN*/)
        .setTitle(getString(R.string.gn_signature_dlg_title))
        .setView(view)
        .setPositiveButton(R.string.add_quick_text_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        WriteIgnaturePref(" "+quickText.getText().toString());
                    }
                })
        .setNegativeButton(R.string.add_quick_text_cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }).create();
        mEditDialog.show();
        mEditDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(!TextUtils.isEmpty(text));
    }
    
    private TextWatcher mTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // TODO Auto-generated method stub
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // TODO Auto-generated method stub
        }

        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            if (mEditDialog != null) {
                if (TextUtils.isEmpty(s.toString())) {
                    mEditDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    mEditDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                    }
                }
            }
    };
    // gionee zhouyj 2012-12-12 add for CR00741770 end 
    
    private String ReadIgnaturePref() {
        String ignature;
        SharedPreferences user = getSharedPreferences(SIGNATURE_PREF, AuroraActivity.MODE_PRIVATE);
        ignature = user.getString("signature", null);
        // save the signature string add a space " "
        return ignature != null ? ignature.substring(1, ignature.length()) : null;
    }

    private void WriteIgnaturePref(String value) {
        SharedPreferences user = getSharedPreferences(SIGNATURE_PREF, AuroraActivity.MODE_PRIVATE);
        Editor editor = user.edit();
        editor.putString("signature", value);
        editor.commit();
    }
    // gionee zhouyj 2012-05-17 add for CR00601523 end
    
    // Gionee fangbin 20120629 added for CR00622030 start
    private void sendMmsRingtoneReceiver() {
        SharedPreferences sp = AuroraPreferenceManager.getDefaultSharedPreferences(this);
        String content = sp.getString(NOTIFICATION_RINGTONE, null);
        // Gionee fangbin 20120712 added for CR00643058 start
        if (null != content && content.equals("")) {
            content = null;
        }
        // Gionee fangbin 20120712 added for CR00643058 end
        boolean enable = sp.getBoolean(MessagingPreferenceActivity.NOTIFICATION_ENABLED, true);
        Intent intent = new Intent("com.android.settings.gnsetmmsringtone");
        intent.putExtra("MMS_RINGTONE", content);
        intent.putExtra("MMS_RINGTONE_ENABLE", enable);
        //Gionee zengxuanhui add for CR00673472 begin
        if(gnGeminiRingtoneSupport){
            String content2 = sp.getString(NOTIFICATION_RINGTONE2, null);
            if("silence".equals(content2)){
                content2 = null;
            }
            intent.putExtra("MMS_RINGTONE2", content2);
        }
        //Gionee zengxuanhui add for CR00673472 end
        sendBroadcast(intent);
    }
    // Gionee fangbin 20120629 added for CR00622030 end
    
    // Gionee fangbin 20120223 addded for CR00527995 start
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        // Aurora yudingmin 2014-08-30 modified for optimize start
          unregisterReceiver(mSimStateChangedReceiver);
       // Aurora xuyong 2014-09=10 added for uptimize start
          if (mSimInfoObserver != null) {
              this.getContentResolver().unregisterContentObserver(mSimInfoObserver);
              mSimInfoObserver = null;
          }
       // Aurora xuyong 2014-09=10 added for uptimize end
        // Aurora yudingmin 2014-08-30 modified for optimize end
        if (MmsApp.mGnMessageSupport) {
            this.getContentResolver().unregisterContentObserver(mResolver);
        }
        super.onDestroy();
    }

    private void initPhoneMsgCount() {
        //gionee gaoj 2012-8-5 added for CR00663940 start
        // gionee zhouyj 2012-09-03 add for CR00678334 start 
        mUnReadNum = 0;
        mMsgCount = 0;
        // gionee zhouyj 2012-09-03 add for CR00678334 end 
        mQueryHandler.cancelOperation(MMS_QUERY_TOKEN);
        mQueryHandler.cancelOperation(SMS_QUERY_TOKEN);
        mQueryHandler.cancelOperation(PUS_QUERY_TOKEN);
        mQueryHandler.cancelOperation(THEAD_QUERY_TOKEN);
        mQueryHandler.startQuery(MMS_QUERY_TOKEN, null, mMmsUri, null, "read != 1", null, null);
        //gionee gaoj 2012-8-5 added for CR00663940 end
    }
    
    // gionee zhouyj 2012-09-03 add for CR00678334 start 
    private Runnable mQueryRunnable = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            initPhoneMsgCount();
        }
    };
    // gionee zhouyj 2012-09-03 add for CR00678334 end 
    
    class DbChangeResolver extends ContentObserver {

        public DbChangeResolver(Handler handler) {
            super(handler);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void onChange(boolean selfChange) {
            // TODO Auto-generated method stub
            super.onChange(selfChange);
            //gionee gaoj 2012-9-20 added for CR00699291 start
            if (mIsonResume) {
                // gionee zhouyj 2012-09-03 modify for CR00678334 start 
                mQueryHandler.removeCallbacks(mQueryRunnable);
                mQueryHandler.postDelayed(mQueryRunnable, 300);
                // gionee zhouyj 2012-09-03 modify for CR00678334 end 
            }
            //gionee gaoj 2012-9-20 added for CR00699291 end
        }
    }
    // Gionee fangbin 20120223 addded for CR00527995 end 

    //gionee gaoj 2012-8-5 added for CR00663940 start
    private class QueryHandler extends AsyncQueryHandler {

        Context mContext;
        public QueryHandler(Context  context) {
            super(context.getContentResolver());
            // TODO Auto-generated constructor stub
            mContext = context;
        }

        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (token == MMS_QUERY_TOKEN) {
                if (cursor != null) {
                    mUnReadNum += cursor.getCount();
                    cursor.close();
                }
                mQueryHandler.startQuery(SMS_QUERY_TOKEN, null, mSmsUri, null, "read != 1", null, null);
            } else if (token == SMS_QUERY_TOKEN) {
                if (cursor != null) {
                    mUnReadNum += cursor.getCount();
                    cursor.close();
                }
                mQueryHandler.startQuery(PUS_QUERY_TOKEN, null, mPushUri, null, "read != 1", null, null);
            } else if (token == PUS_QUERY_TOKEN) {
                if (cursor != null) {
                    mUnReadNum += cursor.getCount();
                    cursor.close();
                }
                mQueryHandler.startQuery(THEAD_QUERY_TOKEN, null, mThreadUri, new String[]{"message_count"}, "message_count > 0", null, null);
            } else if (token == THEAD_QUERY_TOKEN) {
                if (cursor != null) {
                    mSessionNum = cursor.getCount();
                    cursor.moveToPosition(-1);
                    while(cursor.moveToNext()) {
                        mMsgCount += cursor.getInt(0);
                    }
                    cursor.close();
                }
                mUnreadPreference.setTitle(getString(R.string.gn_unread_msg_count) + mUnReadNum);
                mSessionPreference.setTitle(getString(R.string.gn_session_count) + mSessionNum);
                mMsgTotalCountPreference.setTitle(getString(R.string.gn_totle_msg_count) + mMsgCount);
            }
        }
    }
    //gionee gaoj 2012-8-5 added for CR00663940 start
    
    //Gionee <guoyx> <2013-06-08> add for CR00822607 begin
    private String getRingtoneSummary(int ringtoneType) {

        SharedPreferences sp = AuroraPreferenceManager.getDefaultSharedPreferences(this);
        Uri ringtoneUri = null;
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
        /*if (mProfileManager != null) {
            ringtoneUri = mProfileManager.getRingtoneUri(mProfileManager.getActiveProfileKey(), ringtoneType);
        }*/
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
        if (ringtoneUri == null) {
            return null;
        }

        // Gionee <lwzh> <2013-04-29> add for CR00803696 begin
        if (MmsApp.mQcMultiSimEnabled) {
            if (MessageUtils.isRingtoneExist(this, ringtoneUri)) {
                return MessageUtils.gnGetRingtoneTile(this, ringtoneUri);
            } else {
                return MessageUtils.gnGetRingtoneTile(this, RingtoneManager.getDefaultUri(ringtoneType));
            }
        }
        // Gionee <lwzh> <2013-04-29> add for CR00803696 end

        Ringtone ringtone = null;
        if (MessageUtils.isRingtoneExist(this, ringtoneUri)) {
            ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
        }

        if (ringtone == null) {
            ringtone = RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(ringtoneType));
        }

        if (ringtone != null) {
            return ringtone.getTitle(this);
        } else {
            return null;
        }
    }
    
    private void setRingtoneSummary() {
        if (mRingtonePref == null) {
            Log.d(TAG, "setRingtoneSummary mRingtonePref is null.");
            return ;
        }
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
        //mRingtonePref.setRingtoneType(AudioProfileManager.TYPE_MMS);
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
        mRingtonePref.setShowDefault(false);
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
        /*String summary = getRingtoneSummary(AudioProfileManager.TYPE_MMS);
        if(summary != null){
            mRingtonePref.setSummary(summary);
            Log.d(TAG, "mRingtonePref.setSummary " + summary);
        }else{*/
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
            mRingtonePref.setSummary(R.string.notification_summary_silent);
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
        //}
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
    }
    
    //Gionee <guoyx> <2013-06-08> add for CR00822607 end
    
    //Gionee <guoyx> <2013-08-12> add for CR00845622 begin
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.d(TAG, "onWindowFocusChanged focus state:" + hasFocus);
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            setRingtoneSummary();
        }
    }
    //Gionee <guoyx> <2013-08-12> add for CR00845622 end
}
