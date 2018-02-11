/*
 * Copyright (C) 2008 Esmertec AG.
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

package com.android.mms;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.android.mms.data.Contact;
import com.android.mms.data.Conversation;
import com.android.mms.layout.LayoutManager;
import com.android.mms.util.DownloadManager;
import com.android.mms.util.DraftCache;
import com.android.mms.drm.DrmUtils;
import com.android.mms.util.SmileyParser;
import com.android.mms.util.RateController;
import com.android.mms.MmsConfig;
import com.android.mms.transaction.MessagingNotification;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.MmsException;
//Aurora xuyong 2013-11-15 modified for google adapt end

import android.app.Application;
//import android.app.ThemeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.location.Country;
import android.location.CountryDetector;
import android.location.CountryListener;
import android.net.Uri;
// Aurora xuyong 2014-07-26 added for bug #6844 start
import android.os.DeadObjectException;
// Aurora xuyong 2014-07-26 added for bug #6844 end
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import aurora.preference.AuroraPreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
// Gionee zhangxx 2012-04-02 add for CR00556294 begin
import com.aurora.featureoption.FeatureOption;
// Aurora xuyong 2014-07-02 added for reject feature start
import com.aurora.mms.util.Utils;
// Aurora xuyong 2014-07-02 added for reject feature end
// Gionee zhangxx 2012-04-02 add for CR00556294 end
//Gionee guoyangxu 20120509 add for CR00594172 begin
import android.os.SystemProperties;
import android.os.storage.StorageManager;
// Aurora liugj 2013-11-12 modified for bug-554 start
import gionee.os.storage.GnStorageManager;
// Aurora liugj 2013-11-12 modified for bug-554 end
import android.os.storage.StorageVolume;
import android.os.Environment;
//Gionee guoyangxu 20120509 add for CR00594172 end

//gionee gaoj 2012-4-10 added for CR00555790 start
import com.gionee.mms.data.RecentContact;
//gionee gaoj 2012-4-10 added for CR00555790 end
// Aurora yudingmin 2014-10-27 added for sim start
import gionee.provider.GnTelephony.SimInfo;
import gionee.provider.GnTelephony.SIMInfo;
// Aurora yudingmin 2014-10-27 added for sim end

//ginoee gaoj 2012-7-16 added for CR00650452 start
import android.content.SharedPreferences;
//gionee gaoj 2012-7-16 added for CR00650452 end

//gionee gaoj 2012-8-5 added for CR00664077 start
import aurora.app.AuroraProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.os.IBinder;
import android.os.RemoteException;
import android.content.Intent;
import android.content.ServiceConnection;
//import com.gionee.aora.numarea.export.INumAreaManager;
//import com.gionee.aora.numarea.export.INumAreaObserver;
//import com.gionee.aora.numarea.export.IUpdataResult;
//gionee gaoj 2012-8-5 added for CR00664077 end
//gionee zhouyj 2012-08-08 add for CR00667984 start 
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import com.android.mms.ui.MessageUtils;
//gionee zhouyj 2012-08-08 add for CR00667984 end 
//gionee zhouyj 2012-08-08 add for CR00667838 start 
import com.gionee.mms.importexport.ConfigConstantUtils;
//gionee zhouyj 2012-08-08 add for CR00667838 end 

// Gionee fengjianyi 2012-09-14 add for CR00692545 start
import java.util.regex.Matcher;
import java.util.regex.Pattern;
// Gionee fengjianyi 2012-09-14 add for CR00692545 end

//gionee gaoj 2012-10-12 added for CR00711168 start
import android.database.ContentObserver;
import android.database.Cursor;
import android.provider.Settings;
//gionee gaoj 2012-10-12 added for CR00711168 end

//Gionee guoyx 20130218 add for CR00766605 begin
import gionee.telephony.GnTelephonyManager;
//Gionee guoyx 20130218 add for CR00766605 end

//gionee gaoj 2013-3-15 added for CR00784388 start
import com.android.mms.widget.MmsWidgetProvider;
//gionee gaoj 2013-3-15 added for CR00784388 end

import com.gionee.internal.telephony.GnPhone;
// Aurora xuyong 2014-10-23 added for privacy feature start
import com.privacymanage.service.AuroraPrivacyUtils;
import com.privacymanage.service.IPrivacyManageService;
// Aurora xuyong 2014-10-23 added for privacy feature end
// Aurora xuyong 2016-01-27 added for xy-smartsms start
import cn.com.xy.sms.sdk.ui.popu.util.XySdkUtil;
import com.xy.smartsms.manager.XySdkAction;
// Aurora xuyong 2016-01-27 added for xy-smartsms end

public class MmsApp extends Application implements Const{
    public static final String LOG_TAG = "Mms";
    public static final String TXN_TAG = "Mms/Txn";

    private SearchRecentSuggestions mRecentSuggestions;
    private TelephonyManager mTelephonyManager;
    private CountryDetector mCountryDetector;
    private CountryListener mCountryListener;
    private String mCountryIso;
    private static MmsApp sMmsApp = null;

    // for toast thread
    public static final int MSG_RETRIEVE_FAILURE_DEVICE_MEMORY_FULL = 2;
    public static final int MSG_SHOW_TRANSIENTLY_FAILED_NOTIFICATION = 4;
    public static final int MSG_MMS_TOO_BIG_TO_DOWNLOAD = 6;
    public static final int MSG_MMS_CAN_NOT_SAVE = 8;
    public static final int MSG_MMS_CAN_NOT_OPEN = 10;
    public static final int EVENT_QUIT = 100;
    private static HandlerThread mToastthread = null;
    private static Looper mToastLooper = null;
    private static ToastHandler mToastHandler = null;
    // Gionee zhangxx 2012-04-02 add for CR00556294 begin
    public static boolean mGnEMMCSupport = FeatureOption.MTK_EMMC_SUPPORT;
    // Gionee zhangxx 2012-04-02 add for CR00556294 end
    
    //Gionee guoyangxu 20120509 add for CR00594172 begin
    // Aurora liugj 2013-10-31 modified for adpter start 
    public static final boolean mGnMessageSupport = true/*SystemProperties.get("ro.gn.msgbox.prop").equals("yes")*/;
    // Aurora liugj 2013-10-31 modified for adapter end
    //Gionee guoyangxu 20120509 add for CR00594172 end
    
    //Gionee guoyangxu 20120517 add for CR00594172 begin
    public static final boolean mGnMessageSdRingSupport = SystemProperties.get("ro.gn.msgsdring.prop").equals("yes");
    //Gionee guoyangxu 20120517 add for CR00594172 end
    
    //gionee gaoj 2012-3-22 added for CR00555790 start
    // Aurora xuyong 2014-03-22 modified for aurora's new feature start
    public static final boolean mGnPopupMsgSupport = false; //SystemProperties.get("ro.gn.popupmsg.prop").equals("yes") && mGnMessageSupport;
    // Aurora xuyong 2014-03-22 modified for aurora's new feature end

    public static final boolean mEncryption = false; //SystemProperties.get("ro.gn.encryption.prop").equals("yes") && mGnMessageSupport;
    
    //Gionee <guoyx> <2013-06-05> modify for CR00823075 begin
    //Gionee <guoyx> <2013-05-07> added for CR00808519 begin 
    /**
     * Feature of switch "ro.gn.gemini.support" for the single sim solution.
     * Giving the "yes" default value is avoid others solutions no set this features.
     * (just only for MTK now!)
     */
//    public static final boolean mGnGeminiSupport = (SystemProperties.get(FEATURE_GN_GEMINI_SUPPORT, "yes")).equals("yes");
    //Gionee <zhouyj> <2013-06-18> modify for CR00827380 begin
    private static final boolean mGnMtkSingleSupport = (SystemProperties.get(FEATURE_GN_GEMINI_SUPPORT, "yes")).equals("no");
    //Gionee <zhouyj> <2013-06-18> modify for CR00827380 end
    //Gionee <guoyx> <2013-05-07> added for CR00808519 end
    //Gionee <guoyx> <2013-06-05> modify for CR00823075 end
    
    //Gionee guoyx 20121229 added for Qualcomm Multi Sim begin
    //Gionee guoyx 20130218 modified for CR00766605 begin
    public static final boolean mQcMultiSimEnabled = GnTelephonyManager.isMultiSimEnabled();
    //Gionee guoyx 20130218 modified for CR00766605 end
    //Gionee <guoyx> <2013-05-07> modified for CR00808519 begin
    /**
     * Multi sim of the MTK or Qualcomm solutions
     */
    public static final boolean mGnMultiSimMessage = mGnMessageSupport 
            && (mQcMultiSimEnabled || (FeatureOption.MTK_GEMINI_SUPPORT && !mGnMtkSingleSupport));
    //Gionee <guoyx> <2013-05-07> added for CR00808519 end
    //Gionee guoyx 20121229 added for Qualcomm Multi Sim end

    public static boolean mTransparent = false;
    public static boolean mDarkTheme = false;
    public static boolean mLightTheme = false;
    
    public static boolean mDarkStyle = false;
    //gionee gaoj 2012-3-22 added for CR00555790 end
    // gionee zhouyj 2012-04-21 add for CR00573852 start
    public static boolean mGnMsgTopOption = false;
    // gionee zhouyj 2012-04-21 add for CR00573852 end
    //gionee gaoj 2012-6-26 added for CR00626901 start
    public static boolean mGnRecipientLimit = SystemProperties.get("ro.gn.recipientlimit.prop").equals("yes") && mGnMessageSupport; 
    public static boolean mGnPopDefaultValue = SystemProperties.get("ro.gn.popdefaultvalue.prop").equals("yes") && mGnPopupMsgSupport; 
    //gionee gaoj 2012-6-26 added for CR00626901 end

    //gionee gaoj 2012-8-5 added for CR00664077 start
    private static final String ACTION_NUMAREA = "gionee.aora.numarea";
    private AuroraProgressDialog mProgressDialog;
    public static boolean mIsConnected = false;
    //public static INumAreaManager mManager = null;
    //final static public int DIALOG_WAIT_FOR_INIT = IUpdataResult.RESULT_INIT_PROCESSING;
    //final static public int DIALOG_WAIT_FOR_CONNECTING = 2 << 10;
    //gionee gaoj 2012-8-5 added for CR00664077 end

    //gionee gaoj 2012-8-7 added for CR00671408 start
    public static boolean sIsDoctorAnExist;
    public static boolean sIsExchangeExist;
    public static boolean mDoctorAnOpenApi = SystemProperties.get("ro.doctoran.openapi.prop").equals("yes");
    //gionee gaoj 2012-8-7 added for CR00671408 end

    //gionee gaoj 2012-8-14 added for CR00623375 start
    public static boolean mGnRegularlyMsgSend = SystemProperties.get("ro.gn.newfeature.v3").equals("yes") && mGnMessageSupport;
    //gionee gaoj 2012-8-14 added for CR00623375 end
    // Gionee lixiaohu 2012-08-28 added for CR00681687 start
    public static final boolean mGnGeminiMessageSupport = SystemProperties.get("ro.gn.gemini.message.support").equals("yes");
    // Gionee lixiaohu 2012-08-28 added for CR00681687 end
    
    // Gionee fengjianyi 2012-09-14 add for CR00692545 start
    public static boolean mIsVoiceSupportEnable = false;
    // Gionee fengjianyi 2012-09-14 add for CR00692545 end

    //gionee gaoj 2012-9-21 added for CR00687379 start
    //Gionee lixiaohu 2012-10-12 added for CR00710917 start
    public static boolean mGnMtkGeminiSupport = !mGnGeminiMessageSupport && com.aurora.featureoption.FeatureOption.MTK_GEMINI_SUPPORT;
    //Gionee lixiaohu 2012-10-12 added for CR00710917 end
    //gionee gaoj 2012-9-21 added for CR00687379 end

    //gionee gaoj 2012-10-12 added for CR00711168 start
    public static boolean mIsSafeModeSupport = false;
    //gionee gaoj 2012-10-12 added for CR00711168 end

    //gionee wangym 2012-11-22 add for CR00735223 start
    public static boolean mIsTouchModeSupport = false;
   //gionee wangym 2012-11-22 add for CR00735223 end

    // gionee zhouyj 2012-11-26 add for CR00735999 start 
    public static boolean mIsSupportPlatform_4_1 = SystemProperties.get("ro.gn.cell.conn.platform.4.1").equals("yes");
    // gionee zhouyj 2012-11-26 add for CR00735999 end 
    
    // gionee zhouyj 2012-12-10 add for CR00741356 start 
    public static boolean mIsNeedOnLine = SystemProperties.get("ro.gn.mms.online.support").equals("yes");
    // gionee zhouyj 2012-12-10 add for CR00741356 end 

    //gionee gaoj 2012-12-11 added for CR00742048 start
    public static final boolean sIsHotLinesSupport = true;
    //gionee gaoj 2012-12-11 added for CR00742048 end

    //gionee gaoj 2013-3-28 added for CR00790333 start
    public static int mFontSizeType  = 0;
    //gionee gaoj 2013-3-28 added for CR00790333 end

    //gionee gaoj 2013-4-1 added for CR00788343 start
    public static boolean mIsDraftOpen = !com.gionee.featureoption.FeatureOption.GN_FEATURE_DELETE_DRAF;
    //gionee gaoj 2013-4-1 added for CR00788343 end

    //Gionee <gaoj> <2013-4-11> added for CR00796538 start
    public static boolean mDisplaySendTime = com.gionee.featureoption.FeatureOption.GN_FEATURE_DISPLAY_SENT_TIME;
    //Gionee <gaoj> <2013-4-11> added for CR00796538 end
    
    //Gionee <zhouyj> <2013-04-17> add for CR00798863 start
    public static boolean mGnAlphbetIndexSupport = com.gionee.featureoption.FeatureOption.GN_FEATURE_ALPHBETINDEX;
    //Gionee <zhouyj> <2013-04-17> add for CR00798863 end
    
    //Gionee <zhouyj> <2013-04-26> add for CR00802651 start
    public static boolean mGnCloudBackupSupport = com.gionee.featureoption.FeatureOption.GN_FEATURE_CLOUD_BACKUP;
    public static boolean mGnAddContactSupport = com.gionee.featureoption.FeatureOption.GN_FEATURE_ADD_CONTACT;
    public static boolean mGnVoiceHelperSupport = com.gionee.featureoption.FeatureOption.GN_FEATURE_VOICE_HELPER;
    public static boolean mGnDeleteRecordSupport = com.gionee.featureoption.FeatureOption.GN_FEATURE_DELETE_RECORD;
    //Gionee <zhouyj> <2013-04-26> add for CR00802651 end
    //Gionee <guoyx> <2013-05-03> modified for CR00797658 begin
    public static boolean mGnPerfOpt1Support = com.gionee.featureoption.FeatureOption.GN_FEATURE_PERFOMANCE_OPTIMIZATION_1;
    public static boolean mGnPerfOpt2Support = com.gionee.featureoption.FeatureOption.GN_FEATURE_PERFOMANCE_OPTIMIZATION_2;
    //Gionee <guoyx> <2013-05-03> modified for CR00797658 end
    //Gionee <zhouyj> <2013-05-09> add for CR00810588 begin
    public static boolean mGnVoiceInputSupport = false; //com.gionee.featureoption.FeatureOption.GN_FEATURE_VOICE_INPUT;
    public static boolean mGnVoiceReadMsgSupport = false; //com.gionee.featureoption.FeatureOption.GN_FEATURE_VOICE_READ_MSG;
    //Gionee <zhouyj> <2013-05-09> add for CR00810588 end

    //Gionee <gaoj> <2013-05-13> added for CR00811367 begin
    public static boolean mGnPerfList = com.gionee.featureoption.FeatureOption.GN_FEATURE_PERF_LIST;
    //Gionee <gaoj> <2013-05-13> added for CR00811367 end

    //Gionee <gaoj> <2013-05-21> added for CR00817770 begin
    public static boolean mGnHideEncryption = com.gionee.featureoption.FeatureOption.GN_FEATURE_HIDE_ENCRYPTION;
    //Gionee <gaoj> <2013-05-21> added for CR00817770 end

    //Gionee <Gaoj> <2013-05-22> modify for CR00808743 begin
    public static int sThemeChangTag = 0;
    //Gionee <Gaoj> <2013-05-22> modify for CR00808743 end
    
    //Gionee <zhouyj> <2013-05-22> modify for CR00818496 begin
    public static boolean mGnOptimizeAutoComplete = com.gionee.featureoption.FeatureOption.GN_FEATURE_OPTIMIZE_AUTOCOMPLETE;
    //Gionee <zhouyj> <2013-05-22> modify for CR00818496 end
    
    //Gionee <guoyx> <2013-05-22> add for CR00818517 begin
    public static boolean mAddContactDriectly = com.gionee.featureoption.FeatureOption.GN_FEATURE_INSERT_CONTACT_DRIECTLY;
    //Gionee <guoyx> <2013-05-22> add for CR00818517 end

    //Gionee <gaoj> <2013-05-28> add for CR00817770 begin
    public static boolean mGnSmartGuide = com.gionee.featureoption.FeatureOption.GN_FEATURE_SMART_GUIDE;
    //Gionee <gaoj> <2013-05-28> add for CR00817770 end

    //gionee <gaoj> <2013-06-14> add for CR00826240 begin
    public static final boolean mGnLockScrnSupport = SystemProperties.get("ro.gn.navil.lock.scrn").equals("yes");
    //gionee <gaoj> <2013-06-14> add for CR00826240 end
    
    //Gionee <zhouyj> <2013-06-20> add for CR00828119 begin
    public static final boolean mGnSettingSoundSupport = SystemProperties.get("ro.gn.soundctrl.support").equals("yes");
    //Gionee <zhouyj> <2013-06-20> add for CR00828119 end

    //gionee <gaoj> <2013-07-02> add for CR00832502 begin
    public static boolean mGnNewPopSupport = com.gionee.featureoption.FeatureOption.GN_FEATURE_POPUP_NEW;
    //gionee <gaoj> <2013-07-02> add for CR00832502 end
    //Gionee <zhouyj> <2013-08-02> add for CR00845643 begin
    public static boolean mGnUnreadIconChange = com.gionee.featureoption.FeatureOption.GN_FEATURE_UNREAD_ICON_CHANGE;
    //Gionee <zhouyj> <2013-08-02> add for CR00845643 end
    
    // Aurora yudingmin 2014-10-27 added for sim start
    public static Map<Long, SIMInfo> mSimInfoMap = new HashMap<Long, SIMInfo>();
    // Aurora yudingmin 2014-10-27 added for sim end

    // Aurora xuyong 2016-01-27 added for xy-smartsms start
    public static final String DUOQU_SDK_CHANNEL ="5zZZdrFQIUNI";
    private static final String DUOQU_SDK_CHANNEL_SECRETKEY="MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAKmxAiUTxdvOUrZl8jo1EfmqX/bqxyYmhiVcUzpcEINzzBNv4YyRKGPJzcbLf8rBR2p7E8oYYXhbY4I0Wwt13EGwTz7591fn0+1tX72y5nz4cgPJaZTowZUtSZxgmxfnn5zODrjA6vZ6ksSxJzqvLH9+vxb3Il/2kMwoznNll2sVAgMBAAECgYEAhTGEHng7uMFlGPhCPkADwPVjZ9t2v9JDPzYNueQjrhqJV8RcxMn3awQuudhnkU1OchzzNpEWiLMrEztnL0Kuw7DL1Mmh1ReMDjoC4qXVGraS9sCXBo9PrbFeyxUHZ7/AyM69zkit5py4m86CML1rZ0Hxh5Q2FO8kKeAre5L78q0CQQD3a1nWvqtPg84S1cszsSj8oWIQR8lYtcrEb4aZDSZiOlUh9n28tvscA73NmgH25PANeiW400URpN5AoJrC93lPAkEAr5OR+0+nBPImo5gsCFG07c65XTc9DUKuqPKglkdqoVYg5GQ/Yijtb9TPkE75K8FZQsEa9VWN0TGn8dEiSAD0WwJBALoZ/j4/tr2dh5C1TR35oLm9bfSO+o0GWJk+xgAzWu7Br61XKaab/+9HhSm0MMwT0dhhSyRljWtDFoWICECkQpMCQBZ7hw3vPSvc9iWsrrbB/7ET75iIIkE4cLUhnH6h1n2iUcPtMlCXfQ/86DdKZY28zHlH0PPpaKeI/EByzzj5JlECQG9bwBYzPt0rNbXiM06e8ryRwGdd/3Zb4K/Y8JAG380hs40hRb25J7nwDk81C5jPzUEt1XCAqDbMx4kljO4PvZU=";
    // Aurora xuyong 2016-01-27 added for xy-smartsms end
    
    @Override
    public void onCreate() {
        super.onCreate();

        //gionee gaoj 2013-3-28 added for CR00790333 start
          // Aurora liugj 2013-12-12 modified for App start optimize start 
        /*mFontSizeType  = Settings.System.getInt(
                getContentResolver(), "gn_font_size", 0);*/
          // Aurora liugj 2013-12-12 modified for App start optimize end
        //gionee gaoj 2013-3-28 added for CR00790333 end

        //gionee gaoj 2012-10-19 added for CR00711168 start
        mIsSafeModeSupport = Settings.Secure.getInt(
                getContentResolver(), "gionee_guest_mode", 0) == 1;
        //gionee gaoj 2012-10-19 added for CR00711168 end
        // Gionee fengjianyi 2012-09-14 add for CR00692545 start
        //Gionee <guoyx> <2013-04-17> modified for CR00797658 begin
        if (mGnPerfOpt1Support) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                initVoiceSupportSwitch();
            }
        }).start();
        } else {
            initVoiceSupportSwitch();
        }
       // Aurora xuyong 2014-07-21 added for check whether the HmtSdkManager instance is initialed start
        final Context appContext = this.getApplicationContext();
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (!Utils.isInit()) {
                    Utils.init(appContext);
                }
            }
            
        }).start();
       // Aurora xuyong 2014-07-21 added for check whether the HmtSdkManager instance is initialed end
        //Gionee <guoyx> <2013-04-17> modified for CR00797658 end
        // Gionee fengjianyi 2012-09-14 add for CR00692545 end
        
        //gionee gaoj 2012-11-1 added for CR00723596 start
        //sIsDoctorAnExist = (null != MessageUtils.getResolveInfo(getApplicationContext(), "com.tencent.qqpimsecure")) && mGnMessageSupport && mDoctorAnOpenApi;
        sIsExchangeExist = (null != MessageUtils.getResolveInfo(getApplicationContext(), "com.gionee.aora.pim")) && mGnMessageSupport;
        //sIsDoctorAnExist = sIsDoctorAnExist && MessageUtils.getIntentActivities(getApplicationContext(), new Intent("com.tencent.gionee.interceptcenter"));
        //gionee gaoj 2012-11-1 added for CR00723596 end

        sMmsApp = this;

        // Load the default preference values
        //gionee gaoj 2012-4-10 added for CR00555790 start
         // Aurora liugj 2013-12-12 modified for App start optimize start 
        /*if (mGnMessageSupport) {
        //Gionee <guoyx> <2013-04-17> modified for CR00797658 begin
        if (mGnPerfOpt1Support) {
            new Thread (new Runnable() {
                @Override
                public void run() {
                    //getGnTheme();
                    //gionee gaoj 2012-7-24 added for CR00650452 start
                    if (MmsApp.mGnPopDefaultValue) {
                        AuroraPreferenceManager.setDefaultValues(sMmsApp, R.xml.gn_preferences_popfalse, false);
                    } else {
                        AuroraPreferenceManager.setDefaultValues(sMmsApp, R.xml.gn_preferences, false);
                    }
                    //gionee gaoj 2012-7-24 added for CR00650452 end
                }
            }).start();
        } else {
                    //getGnTheme();
                    //gionee gaoj 2012-7-24 added for CR00650452 start
                    if (MmsApp.mGnPopDefaultValue) {
                        AuroraPreferenceManager.setDefaultValues(this, R.xml.gn_preferences_popfalse, false);
                    } else {
                        AuroraPreferenceManager.setDefaultValues(this, R.xml.gn_preferences, false);
                    }
                    //gionee gaoj 2012-7-24 added for CR00650452 end
        }
        //Gionee <guoyx> <2013-04-17> modified for CR00797658 end
        } else {
            //gionee gaoj 2012-4-10 added for CR00555790 end
        AuroraPreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        //gionee gaoj 2012-4-10 added for CR00555790 start
        }*/
        // Aurora liugj 2013-12-12 modified for App start optimize end
        //gionee gaoj 2012-4-10 added for CR00555790 end
        //Gionee <guoyx> <2013-04-17> modified for CR00797658 begin
        if (mGnPerfOpt1Support) {
        new Thread (new Runnable() {
            @Override
            public void run() {
                if (MmsApp.getApplication().getTelephonyManager() == null) {
                    mTelephonyManager = (TelephonyManager) getApplicationContext()
                            .getSystemService(Context.TELEPHONY_SERVICE);
                }
            }
        }).start();
        } else {
               if (MmsApp.getApplication().getTelephonyManager() == null) {
                    mTelephonyManager = (TelephonyManager) getApplicationContext()
                            .getSystemService(Context.TELEPHONY_SERVICE);
                }
        }
        //Gionee <guoyx> <2013-04-17> modified for CR00797658 end
        MmsConfig.init(this);
        Contact.init(this);
        DraftCache.init(this);
        // Aurora xuyong 2014-11-29 modified for bug #10180 start
        Conversation.init(this);
        // Aurora xuyong 2014-11-29 modified for bug #10180 end
        DownloadManager.init(this);
        RateController.init(this);
        //DrmUtils.cleanupStorage(this);
        LayoutManager.init(this);
        SmileyParser.init(this);
        MessagingNotification.init(this);
        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (mGnMessageSupport) {
            RecentContact.init(this);
        }
        //Gionee <guoyx> <2013-04-17> modified for CR00797658 begin
        if (mGnPerfOpt1Support) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // gionee gaoj 2012-4-10 added for CR00555790 end
                mCountryDetector = (CountryDetector) getSystemService(Context.COUNTRY_DETECTOR);
                mCountryListener = new CountryListener() {
                    @Override
                    public synchronized void onCountryDetected(Country country) {
                        mCountryIso = country.getCountryIso();
                    }
                };
                mCountryDetector.addCountryListener(mCountryListener,
                        getMainLooper());
                mCountryDetector.detectCountry();
                InitToastThread();
                // gionee gaoj 2012-9-20 added for CR00699291 start
            }
        }).start();
        } else {
        //gionee gaoj 2012-4-10 added for CR00555790 end
        mCountryDetector = (CountryDetector) getSystemService(Context.COUNTRY_DETECTOR);
        mCountryListener = new CountryListener() {
            @Override
            public synchronized void onCountryDetected(Country country) {
                mCountryIso = country.getCountryIso();
            }
        };
        mCountryDetector.addCountryListener(mCountryListener, getMainLooper());
        mCountryDetector.detectCountry();
        InitToastThread();
        //gionee gaoj 2012-9-20 added for CR00699291 start
        }
        //Gionee <guoyx> <2013-04-17> modified for CR00797658 end
        if (MmsApp.mGnMessageSupport) {
            initInThreadDelay(sMmsApp.getApplicationContext());
        }
        //gionee gaoj 2012-9-20 added for CR00699291 end
        // Aurora xuyong 2014-10-23 added for privacy feature start
        if (sHasPrivacyFeature) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    initPrivacyStatus();
                }
                
            }).start();
        }
        // Aurora xuyong 2014-10-23 added for privacy feature end

        // Aurora yudingmin 2014-10-27 added for sim start
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (mGnMultiSimMessage) {
                    initSimInfo();
                }
            }
            
        }).start();
        // Aurora yudingmin 2014-10-27 added for sim end
        // Aurora xuyong 2016-01-27 added for xy-smartsms start
        // Aurora xuyong 2016-02-29 modified for bug #18972 start
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (mGnMultiSimMessage) {
                    initXyUtil();
                }
            }

        }).start();
        // Aurora xuyong 2016-02-29 modified for bug #18972 end
        // Aurora xuyong 2016-01-27 added for xy-smartsms end
    }
    // Aurora xuyong 2016-02-29 added for bug #18972 start
    private void initXyUtil() {
        XySdkUtil.init(MmsApp.this, DUOQU_SDK_CHANNEL, DUOQU_SDK_CHANNEL_SECRETKEY, new XySdkAction());
    }
    // Aurora xuyong 2016-02-29 added for bug #18972 end
    // Aurora yudingmin 2014-10-27 added for sim start
    private void initSimInfo() {
        // Aurora xuyong 2014-12-23 added for aurora's new feature start
        mSimInfoMap.clear();
        // Aurora xuyong 2014-12-23 added for aurora's new feature end
        Uri simInfoUri = Uri.parse("content://telephony/siminfo");
        Cursor cursor = getContentResolver().query(simInfoUri, new String[]{SimInfo._ID}, null, null, null);
       // Aurora xuyong 2014-10-28 modified for Nullpointerexception start
        if (cursor != null) {
            while(cursor.moveToNext()){
                long simId = cursor.getLong(0);
                SIMInfo simInfo = SIMInfo.getSIMInfoById(getApplicationContext(), simId);
                mSimInfoMap.put(simId, simInfo);
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        // Aurora xuyong 2014-10-28 modified for Nullpointerexception end
        }
    }
    // Aurora yudingmin 2014-10-27 added for sim end
    
    // Aurora xuyong 2014-10-23 added for privacy feature start
    private void initPrivacyStatus() {
        AuroraPrivacyUtils.bindService(this.getApplicationContext());
    }
    // Aurora xuyong 2014-10-23 added for privacy feature end
    private void initInThreadDelay(final Context context) {
        // TODO Auto-generated method stub
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                }
                // TODO Auto-generated method stub

                //gionee zhouyj 2012-08-08 add for CR00667838 start 
                getContentResolver().bulkInsert(ConfigConstantUtils.IMPORT_SMS_CANCEL_URI, null);
                //gionee zhouyj 2012-08-08 add for CR00667838 end 
                // gionee zhouyj 2012-08-01 add for CR00662594 start 
                initSdCards(context);
                // gionee zhouyj 2012-08-01 add for CR00662594 end 
                // gionee zhouyj 2012-08-08 add for CR00667984 start 
                IntentFilter filter = new IntentFilter();
                filter.addAction(Intent.ACTION_BOOT_COMPLETED);
                filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
                filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
                filter.addDataScheme("file");
                registerReceiver(mReceiver, filter);
                // gionee zhouyj 2012-08-08 add for CR00667984 end 

                //gionee gaoj 2012-8-5 added for CR00664077 start
                //Intent aoraArea = new Intent(ACTION_NUMAREA);
                //bindService(aoraArea, mConnection, Context.BIND_AUTO_CREATE);
                //gionee gaoj 2012-8-5 added for CR00664077 end

                //gionee gaoj 2012-10-12 added for CR00711168 start
                getContentResolver().registerContentObserver(
                        Settings.Secure.getUriFor("gionee_guest_mode"), false, mSettingsObserver);
                //gionee gaoj 2012-10-12 added for CR00711168 end
                // gionee zhouyj 2013-03-14 add for CR00783435 start
                Conversation.cachesmspsw(context);
                // gionee zhouyj 2013-03-14 add for CR00783435 end
            }
        }.start();
    }

    //gionee gaoj 2012-10-12 added for CR00711168 start
    ContentObserver mSettingsObserver =
    new ContentObserver(new Handler(Looper.getMainLooper())) {

                @Override
                public void onChange(boolean selfChange) {
                    Log.d("Test", "MmsApp        Guest mode changed!");
                    mIsSafeModeSupport = Settings.Secure.getInt(
                            getContentResolver(), "gionee_guest_mode", 0) == 1;

                    //gionee gaoj 2013-3-15 added for CR00784388 start
                    // Aurora liugj 2013-11-07 deleted for hide widget start
                        //MmsWidgetProvider.notifyDatasetChanged(sMmsApp);
                          // Aurora liugj 2013-11-07 deleted for hide widget end
                    //gionee gaoj 2013-3-15 added for CR00784388 end

                }
            };
    //gionee gaoj 2012-10-12 added for CR00711168 end

    //gionee gaoj 2012-8-5 added for CR00664077 start
    // ********AORA area start
    /*private Handler mAreaHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            
            super.handleMessage(msg);
        }
    };
    
    private INumAreaObserver.Stub mNumAreaObserver = new INumAreaObserver.Stub() {
        @Override
        public void updata(int aResultCode) throws RemoteException {
            switch (aResultCode)
            {
                case IUpdataResult.RESULT_INIT_FINISH:
                case IUpdataResult.RESULT_ERROR_CONNECT_FAILD:
                case IUpdataResult.RESULT_ERROR_CONNECT_TIMEOUT:
                case IUpdataResult.RESULT_SUCCESS:
                case IUpdataResult.RESULT_DB_IS_LAST_VERSION:
                    if (mProgressDialog != null)
                        mProgressDialog.dismiss();
                    mIsConnected = true;
                    break;
            }
            
            mAreaHandler.sendMessageAtFrontOfQueue(Message.obtain(mAreaHandler, aResultCode));
         }
    };
    
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                if (null != mManager) {
                // Aurora xuyong 2014-07-26 modified for bug #6844 start
                    try {
                        if (mNumAreaObserver.isBinderAlive()) {
                            mManager.unregistObserver(mNumAreaObserver);
                        }
                    } catch (DeadObjectException e) {
                        e.printStackTrace();
                    }
                // Aurora xuyong 2014-07-26 modified for bug #6844 end
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mManager = null;
        }
        
        @Override
        public void onServiceConnected(ComponentName name , IBinder service) {
          // Aurora xuyong 2014-07-26 added for bug #6844 start
            if (!service.isBinderAlive()) {
                return;
            }
          // Aurora xuyong 2014-07-26 added for bug #6844 end
            mManager = INumAreaManager.Stub.asInterface(service);
            try {
                if (null != mManager) {
                // Aurora xuyong 2014-07-26 modified for bug #6844 start
                    try {
                        if (mNumAreaObserver.isBinderAlive()) {
                            mManager.registObserver(mNumAreaObserver);
                        }
                    } catch (DeadObjectException e) {
                        e.printStackTrace();
                    }
                // Aurora xuyong 2014-07-26 modified for bug #6844 end
                }

            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    protected android.app.Dialog onCreateDialog(int id) {
        String message = "";
        if (id == DIALOG_WAIT_FOR_CONNECTING || id == DIALOG_WAIT_FOR_INIT) {
            mProgressDialog = new AuroraProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setProgressStyle(AuroraProgressDialog.STYLE_SPINNER);
            switch (id) {
                case DIALOG_WAIT_FOR_INIT:
                    message = getString(R.string.wait_init);
                    break;
                case DIALOG_WAIT_FOR_CONNECTING:
                    message = getString(R.string.wait_connecting);
                    mProgressDialog.setButton("cancle", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialoginterface, int i) {
                            try {
                                mManager.cancelUpdata();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    break;
            }
            mProgressDialog.setMessage(message);
        }
        return mProgressDialog;
    
    };*/

    // ********AORA area end
    //gionee gaoj 2012-8-5 added for CR00664077 end

    synchronized public static MmsApp getApplication() {
        return sMmsApp;
    }

    @Override
    public void onTerminate() {
        DrmUtils.cleanupStorage(this);
        mCountryDetector.removeCountryListener(mCountryListener);
    }

     // Aurora liugj 2013-12-12 modified for App start optimize start 
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //gionee gaoj 2012-5-30 added for CR00555790 start
        /*if (MmsApp.mGnMessageSupport) {
            getGnTheme();
        }*/
        //gionee gaoj 2012-5-30 added for CR00555790 end
        LayoutManager.getInstance().onConfigurationChanged(newConfig);

        //gionee gaoj 2013-3-28 added for CR00790333 start
        /*mFontSizeType  = Settings.System.getInt(
                getContentResolver(), "gn_font_size", 0);*/
        //gionee gaoj 2013-3-28 added for CR00790333 end
    }
     // Aurora liugj 2013-12-12 modified for App start optimize end

    //gionee gaoj 2012-5-30 added for CR00555790 start
    //Gionee <guoyx> <2013-04-17> modified for CR00797658 begin
    private void getGnTheme() {
        /*String style = THEME_STYLE_DEFAULT;
        
        if (FeatureOption.GN_APP_THEME_SUPPORT) {
            ThemeManager themeManager = (ThemeManager) getSystemService(Context.THEME_MANAGER_SERVICE);
            String tmpStyle = themeManager.getAppThemeStyle(ThemeManager.MMS);
            if (!TextUtils.isEmpty(tmpStyle)) {
                style = tmpStyle.substring(2, 3);
            } else {
                style = SystemProperties.get(FEATURE_GN_THEME_STYLE, THEME_STYLE_DEFAULT);
            }
        } else {
            style = SystemProperties.get(FEATURE_GN_THEME_STYLE, THEME_STYLE_DEFAULT);
        }*/

//        if (THEME_STYLE_LIGHT.equals(style)) {
            mDarkTheme   = false;
            mLightTheme  = true;
//        } else {
//            mDarkTheme   = true;
//            mLightTheme  = false;
//        }
        
        mTransparent = false;
        mDarkStyle = mDarkTheme;
        //Gionee <Gaoj> <2013-05-22> modify for CR00808743 begin
        sThemeChangTag ++;
        //Gionee <Gaoj> <2013-05-22> modify for CR00808743 end
    }
    //Gionee <guoyx> <2013-04-17> modified for CR00797658 end
    //gionee gaoj 2012-5-30 added for CR00555790 end
    /**
     * @return Returns the TelephonyManager.
     */
    public TelephonyManager getTelephonyManager() {
        if (mTelephonyManager == null) {
            mTelephonyManager = (TelephonyManager)getApplicationContext()
                    .getSystemService(Context.TELEPHONY_SERVICE);
        }
        return mTelephonyManager;
    }

    /**
     * Returns the content provider wrapper that allows access to recent searches.
     * @return Returns the content provider wrapper that allows access to recent searches.
     */
    public SearchRecentSuggestions getRecentSuggestions() {
        /*
        if (mRecentSuggestions == null) {
            mRecentSuggestions = new SearchRecentSuggestions(this,
                    SuggestionsProvider.AUTHORITY, SuggestionsProvider.MODE);
        }
        */
        return mRecentSuggestions;
    }

    public String getCurrentCountryIso() {
        return mCountryIso == null ? Locale.getDefault().getCountry() : mCountryIso;
    }

    private void InitToastThread() {
        if (null == mToastHandler) {
            HandlerThread thread = new HandlerThread("MMSAppToast");
            thread.start();
            mToastLooper = thread.getLooper();
            if (null != mToastLooper) {
                mToastHandler = new ToastHandler(mToastLooper);
            }
        }
    }

    public synchronized static ToastHandler getToastHandler() {
        /*if (null == mToastHandler) {
            HandlerThread thread = new HandlerThread("MMSAppToast");
            thread.start();
            mToastLooper = thread.getLooper();
            mToastHandler = new ToastHandler(mToastLooper);
        }*/
        return mToastHandler;
    }

    public final class ToastHandler extends Handler {
        public ToastHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(MmsApp.TXN_TAG, "Toast Handler handleMessage :" + msg);
            
            switch (msg.what) {
                case EVENT_QUIT: {
                    Log.d(MmsApp.TXN_TAG, "EVENT_QUIT");
                    getLooper().quit();
                    return;
                }

                case MSG_RETRIEVE_FAILURE_DEVICE_MEMORY_FULL: {
                    Toast.makeText(sMmsApp, R.string.download_failed_due_to_full_memory, Toast.LENGTH_LONG).show();
                    break;
                }

                case MSG_SHOW_TRANSIENTLY_FAILED_NOTIFICATION: {
                    Toast.makeText(sMmsApp, R.string.transmission_transiently_failed, Toast.LENGTH_LONG).show();
                    break;
                }

                case MSG_MMS_TOO_BIG_TO_DOWNLOAD: {
                    Toast.makeText(sMmsApp, R.string.mms_too_big_to_download, Toast.LENGTH_LONG).show();
                    break;
                }

                case MSG_MMS_CAN_NOT_SAVE: {
                    Toast.makeText(sMmsApp, R.string.cannot_save_message, Toast.LENGTH_LONG).show();
                    break;
                }

                case MSG_MMS_CAN_NOT_OPEN: {
                    String str = sMmsApp.getResources().getString(R.string.unsupported_media_format, (String)msg.obj);
                    Toast.makeText(sMmsApp, str, Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    
    /**
     * The Operator string value.
     */
    private static final String mOperator = SystemProperties.get("ro.operator.optr");
    
    /**
     * check the Operator is TELECOM "OP01"
     * 
     * @return
     */
    public static boolean isTelecomOperator() {
        return (mOperator != null && OPERATOR_TELECOM.equals("OP02"));
    }
    
    /**
     * check the Operator is UNICOM "OP02"
     * @return
     */
    public static boolean isUnicomOperator() {
        return (mOperator != null && OPERATOR_UNICOM.equals(mOperator));
    }
    
    /**
     * check the Operator is CMCC "OP03"
     * @return
     */
    public static boolean isCmccOperator() {
        return (mOperator != null && OPERATOR_CMCC.equals("OP02"));
    }
    
    /**
     * get the Operator string value
     * @see #OPERATOR_CMCC
     * @see #OPERATOR_TELECOM
     * @see #OPERATOR_UNICOM
     * @return
     */
    public String getOperator() {
        return mOperator;
    }

    // gionee zhouyj 2012-08-01 add for CR00662594 start 
    private StorageManager mStorageManager;
    private File[] mSDCardMountPointPathList = null;
    private int mStorageCount;
    public static int mStorageMountedCount;
    public static String mSDCardPath = null;
    public static String mSDCard2Path = null;
    public  static String mDefaultSDCardPath = null;
    
    private void initSdCards(Context context) {
        mStorageManager = (StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
        StorageVolume[] storageVolume = mStorageManager.getVolumeList();
        mStorageCount = storageVolume.length;
        File[] systemSDCardMountPointPathList = new File[mStorageCount];
        for (int i = 0; i < mStorageCount; i++) {
            systemSDCardMountPointPathList[i] = new File(storageVolume[i].getPath());
        }
        mSDCardMountPointPathList = updateMountedPointList(systemSDCardMountPointPathList);
        mStorageMountedCount = mSDCardMountPointPathList.length;
        if (mStorageMountedCount >= 2) {
            // Aurora xuyong 2014-04-17 modified for bug #4337 start
            if (mSDCardMountPointPathList[0] != null) {
                mSDCardPath = mSDCardMountPointPathList[0].getAbsolutePath();
            }
            if (mSDCardMountPointPathList[1] != null) {
                mSDCard2Path = mSDCardMountPointPathList[1].getAbsolutePath();
            }
        } else if(mStorageMountedCount == 1){
            if (mSDCardMountPointPathList[0] != null) {
                mSDCardPath = mSDCardMountPointPathList[0].getAbsolutePath();
            }
            // Aurora xuyong 2014-04-17 modified for bug #4337 end
        }
        mDefaultSDCardPath = mSDCardPath;
    }
    
    // get mounted sdcard
    private File[] updateMountedPointList(File[] systemSDCardMountPointPathList){
        int mountCount = 0;
        for (int i = 0; i < systemSDCardMountPointPathList.length; i++) {
            if (checkSDCardMount(systemSDCardMountPointPathList[i].getAbsolutePath())) {
                mountCount++;
            }
        }
        File[] sdCardMountPointPathList = new File[mountCount];
        // gionee zhouyj 2012-08-07 modify for CR00667984 start 
        for (int i = 0, j = 0; i < systemSDCardMountPointPathList.length && j < mountCount; i++) {
            if (checkSDCardMount(systemSDCardMountPointPathList[i].getAbsolutePath())) {
                sdCardMountPointPathList[j++] = systemSDCardMountPointPathList[i];
            }
        }
        // gionee zhouyj 2012-08-07 modify for CR00667984 end 
        
        return sdCardMountPointPathList;
    }
    /**
     * This method checks whether SDcard is mounted or not
     @param  mountPoint   the mount point that should be checked
     @return               true if SDcard is mounted, false otherwise
     */ 
    protected boolean checkSDCardMount(String mountPoint) {
        if(mountPoint == null){
            return false;
        }
        String state = null;
          // Aurora liugj 2013-11-12 modified for bug-554 start
        state = GnStorageManager.getVolumeState(mountPoint);
          // Aurora liugj 2013-11-12 modified for bug-554 end
        return Environment.MEDIA_MOUNTED.equals(state);
    }
    // gionee zhouyj 2012-08-01 add for CR00662594 end 
    
    // gionee zhouyj 2012-08-08 add for CR00667984 start 
    private BroadcastReceiver mReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
                    Intent.ACTION_MEDIA_MOUNTED.equals(intent.getAction()) || 
                    Intent.ACTION_MEDIA_UNMOUNTED.equals(intent.getAction())) {
                //Gionee <zhouyj> <2013-07-02> modify CR00831595 begin
                Log.i("MmsApp", "MEDIA state changed.   action = " + intent.getAction());
                if (Intent.ACTION_MEDIA_UNMOUNTED.equals(intent.getAction())) {
                    mStorageMountedCount = 0;
                } else {
                    initSdCards(MmsApp.this);
                }
                //Gionee <zhouyj> <2013-07-02> modify CR00831595 end
            }
        }
    };
    // gionee zhouyj 2012-08-08 add for CR00667984 end 

    // Gionee fengjianyi 2012-09-14 add for CR00692545 start
    private void initVoiceSupportSwitch() {
        String voiceSupportStr = SystemProperties.get("ro.gn.voicehelper.support");
        if (voiceSupportStr != null && voiceSupportStr.contains("yes")) {
            //Gionee <zhouyj> <2013-08-06> modify for CR00844736 begin
            mIsVoiceSupportEnable = true;
            /*Matcher digit = Pattern.compile("\\d").matcher(voiceSupportStr);
            if(digit.find()) {
                Matcher pureDigit = Pattern.compile("\\D").matcher(voiceSupportStr);
                String version = pureDigit.find() ? pureDigit.replaceAll("") : voiceSupportStr;
                Log.d(LOG_TAG, "voice: version is " + version);
                // Gionee fengjianyi 2012-09-17 modify for CR00692545 start
                if (version != null && Integer.parseInt(version) > 12) {
                    mIsVoiceSupportEnable = true;
                }
                // Gionee fengjianyi 2012-09-17 modify for CR00692545 end
            }*/
            //Gionee <zhouyj> <2013-08-06> modify for CR00844736 end
        }
    }
    // Gionee fengjianyi 2012-09-14 add for CR00692545 end

    //gionee gaoj 2013-1-21 added for CR00764025 start
    public static boolean isOpenApi() {
        if (MmsApp.mGnMessageSupport && MmsApp.mDoctorAnOpenApi &&
                null != MessageUtils.getResolveInfo(sMmsApp.getApplicationContext(), "com.tencent.qqpimsecure") &&
                MessageUtils.getIntentActivities(sMmsApp.getApplicationContext(), new Intent("com.tencent.gionee.aidl.GioneeService"))) {
            return true;
        }
        return false;
    }
    //gionee gaoj 2013-1-21 added for CR00764025 end
    
    //Gionee <zhouyj> <2013-04-25> add for CR00802357 start
    public static boolean isGnSynchronizerSupport() {
        return mGnMessageSupport && MessageUtils.isPackageExist(sMmsApp, "gn.com.android.synchronizer");
    }
    //Gionee <zhouyj> <2013-04-25> add for CR00802357 end
    // Aurora xuyong 2014-07-02 added for reject feature start
    // Aurora reject feature switch
    public static boolean sHasRejectFeature = "yes".equals(SystemProperties.get("ro.aurora.reject.support"));
    // Aurora xuyong 2014-07-02 added for reject feature end
    // Aurora xuyong 2014-09-25 added for INDIA REQUIREMENT start
    public static boolean mHasIndiaFeature = "IN".equals(SystemProperties.get("ro.iuni.country.option"));
    // Aurora xuyong 2014-09-25 added for INDIA REQUIREMENT end
    // Aurora xuyong 2014-10-23 added for privacy feature start
    public static boolean sHasPrivacyFeature = true;
    // Aurora xuyong 2014-10-23 added for privacy feature end
    public static boolean sNotCNFeature    = "true".equals(SystemProperties.get("phone.type.oversea"));
    // Aurora yudingmin 2014-04-30 added for huxideng feature start
    public static final String mProductModel = SystemProperties.get("ro.product.model");
    // Aurora yudingmin 2014-04-30 end for huxideng feature end
    // Aurora xuyong 2016-01-28 added for xy-smartsms start
    public static boolean sHasXySmartSmsFeature = true;
    // Aurora xuyong 2016-01-28 added for xy-smartsms end
}
