/*
 * Copyright (C) 2009 The Android Open Source Project
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

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.android.internal.telephony.TelephonyProperties;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.os.SystemProperties;
// add for cmcc dir ui begin
import android.content.SharedPreferences;
import aurora.preference.AuroraPreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
// add for cmcc dir ui end
import com.gionee.internal.telephony.GnTelephonyManagerEx;
import gionee.telephony.GnTelephonyManager;
import gionee.provider.GnSettings;


public class MmsConfig {
    private static final String TAG = "MmsConfig";
    private static final boolean DEBUG = true;
    private static final boolean LOCAL_LOGV = false;

    private static final String DEFAULT_HTTP_KEY_X_WAP_PROFILE = "x-wap-profile";
    private static final String DEFAULT_USER_AGENT = "Android-Mms/2.0";

    private static final int MAX_IMAGE_HEIGHT = 480;
    private static final int MAX_IMAGE_WIDTH  = 640;
    private static final int RECIPIENTS_LIMIT = 50;
    private static final int MAX_TEXT_LENGTH = 2000;
    //MTK_OP01_PROTECT_START
    private static final int MAX_CMCC_TEXT_LENGTH = 3100;
    private static final String UAPROFURL_OP01 = "http://218.249.47.94/Xianghe/MTK_Athens15_UAProfile.xml";
    //MTK_OP01_PROTECT_END

    /**
     * Whether to hide MMS functionality from the user (i.e. SMS only).
     */
    private static boolean mTransIdEnabled = false;
    private static int mMmsEnabled = 1;                         // default to true
    private static int mMaxMessageSize = 300 * 1024;            // default to 300k max size
    private static int mUserSetMmsSizeLimit = 300;              // Mms size limit, default 300K.
    private static int mReceiveMmsSizeLimitFor2G = 200;         // Receive Mms size limit for 2G network
    private static int mReceiveMmsSizeLimitForTD = 400;         // Receive Mms size limit for TD network
    private static String mUserAgent = DEFAULT_USER_AGENT;
    private static String mUaProfTagName = DEFAULT_HTTP_KEY_X_WAP_PROFILE;
    private static String mUaProfUrl = null;
    private static String mHttpParams = null;
    private static String mHttpParamsLine1Key = null;
    private static String mEmailGateway = null;
    private static int mMaxImageHeight = MAX_IMAGE_HEIGHT;      // default value
    private static int mMaxImageWidth = MAX_IMAGE_WIDTH;        // default value
    private static int mRecipientLimit = Integer.MAX_VALUE;     // default value
    private static int mMaxRestrictedImageHeight = 1200;        // default value
    private static int mMaxRestrictedImageWidth = 1600;         // default value
    private static int mMmsRecipientLimit = 20;                 // default value
    private static int mSmsRecipientLimit = 100;                // default value
    private static int mDefaultSMSMessagesPerThread = 500;      // default value
    private static int mDefaultMMSMessagesPerThread = 50;       // default value
    private static int mMinMessageCountPerThread = 2;           // default value
    private static int mMaxMessageCountPerThread = 5000;        // default value
    private static int mHttpSocketTimeout = 60*1000;            // default to 1 min
    private static int mMinimumSlideElementDuration = 7;        // default to 7 sec
    private static boolean mNotifyWapMMSC = false;
    private static boolean mAllowAttachAudio = true;

    // See the comment below for mEnableMultipartSMS.
    private static int mSmsToMmsTextThreshold = 4;
    
    //MTK_OP01_PROTECT_START
    private static int mOP01SmsToMmsThreshold = 11;
    //MTK_OP01_PROTECT_END
    
    //MTK_OP02_PROTECT_START
    private static int mOP02SmsToMmsThreshold = 10;
    //MTK_OP02_PROTECT_END

    // This flag is somewhat confusing. If mEnableMultipartSMS is true, long sms messages are
    // always sent as multi-part sms messages, with no checked limit on the number of segments.
    // If mEnableMultipartSMS is false, then mSmsToMmsTextThreshold is used to determine the
    // limit of the number of sms segments before turning the long sms message into an mms
    // message. For example, if mSmsToMmsTextThreshold is 4, then a long sms message with three
    // or fewer segments will be sent as a multi-part sms. When the user types more characters
    // to cause the message to be 4 segments or more, the send button will show the MMS tag to
    // indicate the message will be sent as an mms.
    private static boolean mEnableMultipartSMS = true;

    private static boolean mEnableSlideDuration = true;
    private static boolean mEnableMMSReadReports = true;        // key: "enableMMSReadReports"
    private static boolean mEnableSMSDeliveryReports = true;    // key: "enableSMSDeliveryReports"
    private static boolean mEnableMMSDeliveryReports = true;    // key: "enableMMSDeliveryReports"
    private static int mMaxTextLength = -1;
    private static boolean mDeviceStorageFull = false;

    // This is the max amount of storage multiplied by mMaxMessageSize that we
    // allow of unsent messages before blocking the user from sending any more
    // MMS's.
    private static int mMaxSizeScaleForPendingMmsAllowed = 4;       // default value

    // Email gateway alias support, including the master switch and different rules
    private static boolean mAliasEnabled = false;
    private static int mAliasRuleMinChars = 2;
    private static int mAliasRuleMaxChars = 48;

    private static int mMaxSubjectLength = 40;  // maximum number of characters allowed for mms
                                                // subject

    public static void init(Context context) {
        if (LOCAL_LOGV) {
            Log.v(TAG, "MmsConfig.init()");
        }
        
        //Gionee guoyx 20130221 added for CR00773050 begin
        mContext = context;
        //Gionee guoyx 20130221 added for CR00773050 end
        
        // Always put the mnc/mcc in the log so we can tell which mms_config.xml was loaded.
        if (LOCAL_LOGV) {
            Log.v(TAG, "mnc/mcc: " + android.os.SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC));
        }
        //Gionee <guoyx> <2013-04-17> modified for CR00797658 begin
        if (MmsApp.mGnPerfOpt1Support) {
        new Thread(new Runnable() {
            public void run() {
                loadMmsSettings(mContext);
            }
        }).start();
        } else {
            loadMmsSettings(context);
        }
        //Gionee <guoyx> <2013-04-17> modified for CR00797658 end
    }

    public static int getSmsToMmsTextThreshold() {
        //MTK_OP01_PROTECT_START
        if (MmsApp.isTelecomOperator()) {
            return mOP01SmsToMmsThreshold;
        }
        //MTK_OP01_PROTECT_END
        
        //MTK_OP02_PROTECT_START
        if (MmsApp.isUnicomOperator() 
              //gionee lwzh modify for CR00629149 20120802 begin
                || MmsApp.mGnMessageSupport) {
              //gionee lwzh modify for CR00629149 20120802 end
            return mOP02SmsToMmsThreshold;
        }
        //gionee lwzh modify for CR00629149 20120802 end
        
        //MTK_OP02_PROTECT_END
        return mSmsToMmsTextThreshold;
    }

    public static boolean getMmsEnabled() {
        return mMmsEnabled == 1 ? true : false;
    }

    public static int getUserSetMmsSizeLimit(boolean isBytes) {
        if (true == isBytes) {
            return mUserSetMmsSizeLimit*1024;
        } else {
            return mUserSetMmsSizeLimit;
        }
    }
    
    public static void setUserSetMmsSizeLimit(int limit) {
        mUserSetMmsSizeLimit = limit;
    }
    public static int getMaxMessageSize() {
        if (LOCAL_LOGV) {
            Log.v(TAG, "MmsConfig.getMaxMessageSize(): " + mMaxMessageSize);
        }
        return mMaxMessageSize;
    }

    /**
     * This function returns the value of "enabledTransID" present in mms_config file.
     * In case of single segment wap push message, this "enabledTransID" indicates whether
     * TransactionID should be appended to URI or not.
     */
    public static boolean getTransIdEnabled() {
        return mTransIdEnabled;
    }

    public static String getUserAgent() {
        return mUserAgent;
    }

    public static String getUaProfTagName() {
        return mUaProfTagName;
    }

    public static String getUaProfUrl() {
        return mUaProfUrl;
    }

    public static String getHttpParams() {
        return mHttpParams;
    }

    public static String getHttpParamsLine1Key() {
        return mHttpParamsLine1Key;
    }

    public static String getEmailGateway() {
        return mEmailGateway;
    }

    public static int getMaxImageHeight() {
        return mMaxImageHeight;
    }

    public static int getMaxImageWidth() {
        return mMaxImageWidth;
    }

    public static int getRecipientLimit() {
        return mRecipientLimit;
    }

    public static int getMaxRestrictedImageHeight() {
        return mMaxRestrictedImageHeight;
    }

    public static int getMaxRestrictedImageWidth() {
        return mMaxRestrictedImageWidth;
    }

    public static int getMmsRecipientLimit() {
        return mMmsRecipientLimit;
    }

    //gionee gaoj 2012-10-30 added for CR00722769 start
    static final int SMS_RECIPIENT_LIMIT = SystemProperties.getInt("ro.gn.sms.recipient.limit", 100);
    //gionee gaoj 2012-10-30 added for CR00722769 end
    public static int getSmsRecipientLimit() {
        //gionee gaoj 2012-6-26 added for CR00626901 start
        if (MmsApp.mGnMessageSupport) {
            //gionee gaoj 2012-10-30 added for CR00722769 start
            if (SMS_RECIPIENT_LIMIT != 100) {
                return SMS_RECIPIENT_LIMIT;
            }
            //gionee gaoj 2012-10-30 added for CR00722769 end
            if (MmsApp.mGnRecipientLimit) {
                return 100;
            } else {
                return 10000;
            }
        }
        //gionee gaoj 2012-6-26 added for CR00626901 end
        return mSmsRecipientLimit;
    }

    public static int getMaxTextLimit() {
        //MTK_OP01_PROTECT_START
        if (MmsApp.isTelecomOperator()) {
            return MAX_CMCC_TEXT_LENGTH;
        }
        //MTK_OP01_PROTECT_END
        return mMaxTextLength > -1 ? mMaxTextLength : MAX_TEXT_LENGTH;
    }

    public static int getDefaultSMSMessagesPerThread() {
        return mDefaultSMSMessagesPerThread;
    }

    public static int getDefaultMMSMessagesPerThread() {
        return mDefaultMMSMessagesPerThread;
    }

    public static int getMinMessageCountPerThread() {
        return mMinMessageCountPerThread;
    }

    public static int getMaxMessageCountPerThread() {
        return mMaxMessageCountPerThread;
    }

    public static int getHttpSocketTimeout() {
        return mHttpSocketTimeout;
    }

    public static int getMinimumSlideElementDuration() {
        return mMinimumSlideElementDuration;
    }

    public static boolean getMultipartSmsEnabled() {
        return mEnableMultipartSMS;
    }

    public static boolean getSlideDurationEnabled() {
        return mEnableSlideDuration;
    }

    public static boolean getMMSReadReportsEnabled() {
        return mEnableMMSReadReports;
    }

    public static boolean getSMSDeliveryReportsEnabled() {
        return mEnableSMSDeliveryReports;
    }

    public static boolean getMMSDeliveryReportsEnabled() {
        return mEnableMMSDeliveryReports;
    }

    public static boolean getNotifyWapMMSC() {
        return mNotifyWapMMSC;
    }

    public static int getMaxSizeScaleForPendingMmsAllowed() {
        return mMaxSizeScaleForPendingMmsAllowed;
    }

    public static boolean isAliasEnabled() {
        return mAliasEnabled;
    }

    public static int getAliasMinChars() {
        return mAliasRuleMinChars;
    }

    public static int getAliasMaxChars() {
        return mAliasRuleMaxChars;
    }

    public static boolean getAllowAttachAudio() {
        return mAllowAttachAudio;
    }

    public static int getMaxSubjectLength() {
        return mMaxSubjectLength;
    }

    public static int getReceiveMmsLimitFor2G() {
        return mReceiveMmsSizeLimitFor2G;
    }

    public static int getReceiveMmsLimitForTD() {
        return mReceiveMmsSizeLimitForTD;
    }

    public static final void beginDocument(XmlPullParser parser, String firstElementName) throws XmlPullParserException, IOException
    {
        int type;
        while ((type=parser.next()) != parser.START_TAG
                   && type != parser.END_DOCUMENT) {
            ;
        }

        if (type != parser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }

        if (!parser.getName().equals(firstElementName)) {
            throw new XmlPullParserException("Unexpected start tag: found " + parser.getName() +
                    ", expected " + firstElementName);
        }
    }

    public static final void nextElement(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        int type;
        while ((type=parser.next()) != parser.START_TAG
                   && type != parser.END_DOCUMENT) {
            ;
        }
    }

    private static void loadMmsSettings(Context context) {
        XmlResourceParser parser = context.getResources().getXml(R.xml.mms_config);

        try {
            beginDocument(parser, "mms_config");

            while (true) {
                nextElement(parser);
                String tag = parser.getName();
                if (tag == null) {
                    break;
                }
                String name = parser.getAttributeName(0);
                String value = parser.getAttributeValue(0);
                String text = null;
                if (parser.next() == XmlPullParser.TEXT) {
                    text = parser.getText();
                }

                if (DEBUG) {
                    Log.v(TAG, "tag: " + tag + " value: " + value + " - " +
                            text);
                }
                if ("name".equalsIgnoreCase(name)) {
                    if ("bool".equals(tag)) {
                        // bool config tags go here
                        if ("enabledMMS".equalsIgnoreCase(value)) {
                            mMmsEnabled = "true".equalsIgnoreCase(text) ? 1 : 0;
                        } else if ("enabledTransID".equalsIgnoreCase(value)) {
                            mTransIdEnabled = "true".equalsIgnoreCase(text);
                        } else if ("enabledNotifyWapMMSC".equalsIgnoreCase(value)) {
                            mNotifyWapMMSC = "true".equalsIgnoreCase(text);
                        } else if ("aliasEnabled".equalsIgnoreCase(value)) {
                            mAliasEnabled = "true".equalsIgnoreCase(text);
                        } else if ("allowAttachAudio".equalsIgnoreCase(value)) {
                            mAllowAttachAudio = "true".equalsIgnoreCase(text);
                        } else if ("enableMultipartSMS".equalsIgnoreCase(value)) {
                            mEnableMultipartSMS = "true".equalsIgnoreCase(text);
                        } else if ("enableSlideDuration".equalsIgnoreCase(value)) {
                            mEnableSlideDuration = "true".equalsIgnoreCase(text);
                        } else if ("enableMMSReadReports".equalsIgnoreCase(value)) {
                            mEnableMMSReadReports = "true".equalsIgnoreCase(text);
                        } else if ("enableSMSDeliveryReports".equalsIgnoreCase(value)) {
                            mEnableSMSDeliveryReports = "true".equalsIgnoreCase(text);
                        } else if ("enableMMSDeliveryReports".equalsIgnoreCase(value)) {
                            mEnableMMSDeliveryReports = "true".equalsIgnoreCase(text);
                        }
                    } else if ("int".equals(tag)) {
                        // int config tags go here
                        if ("maxMessageSize".equalsIgnoreCase(value)) {
                            mMaxMessageSize = Integer.parseInt(text);
                        } else if ("maxImageHeight".equalsIgnoreCase(value)) {
                            mMaxImageHeight = Integer.parseInt(text);
                        } else if ("maxImageWidth".equalsIgnoreCase(value)) {
                            mMaxImageWidth = Integer.parseInt(text);
                        } else if ("maxRestrictedImageHeight".equalsIgnoreCase(value)) {
                            mMaxRestrictedImageHeight = Integer.parseInt(text);
                        }else if ("maxRestrictedImageWidth".equalsIgnoreCase(value)) {
                            mMaxRestrictedImageWidth = Integer.parseInt(text);
                        } else if ("defaultSMSMessagesPerThread".equalsIgnoreCase(value)) {
                            mDefaultSMSMessagesPerThread = Integer.parseInt(text);
                        } else if ("defaultMMSMessagesPerThread".equalsIgnoreCase(value)) {
                            mDefaultMMSMessagesPerThread = Integer.parseInt(text);
                        } else if ("minMessageCountPerThread".equalsIgnoreCase(value)) {
                            mMinMessageCountPerThread = Integer.parseInt(text);
                        } else if ("maxMessageCountPerThread".equalsIgnoreCase(value)) {
                            mMaxMessageCountPerThread = Integer.parseInt(text);
                        } else if ("smsToMmsTextThreshold".equalsIgnoreCase(value)) {
                            mSmsToMmsTextThreshold = Integer.parseInt(text);
                        } else if ("recipientLimit".equalsIgnoreCase(value)) {
                            //MTK_OP01_PROTECT_START
                            if (MmsApp.isTelecomOperator()) {
                                mMmsRecipientLimit = RECIPIENTS_LIMIT;
                            } else {
                            //MTK_OP01_PROTECT_END
                                mMmsRecipientLimit = Integer.parseInt(text);
                                if (mMmsRecipientLimit <= 0) {
                                    mMmsRecipientLimit = RECIPIENTS_LIMIT;
                                }
                            //MTK_OP01_PROTECT_START
                            }
                            //MTK_OP01_PROTECT_END
                        } else if ("httpSocketTimeout".equalsIgnoreCase(value)) {
                            mHttpSocketTimeout = Integer.parseInt(text);
                        } else if ("minimumSlideElementDuration".equalsIgnoreCase(value)) {
                            mMinimumSlideElementDuration = Integer.parseInt(text);
                        } else if ("maxSizeScaleForPendingMmsAllowed".equalsIgnoreCase(value)) {
                            mMaxSizeScaleForPendingMmsAllowed = Integer.parseInt(text);
                        } else if ("aliasMinChars".equalsIgnoreCase(value)) {
                            mAliasRuleMinChars = Integer.parseInt(text);
                        } else if ("aliasMaxChars".equalsIgnoreCase(value)) {
                            mAliasRuleMaxChars = Integer.parseInt(text);
                        } else if ("smsToMmsTextThreshold".equalsIgnoreCase(value)) {
                            mSmsToMmsTextThreshold = Integer.parseInt(text);
                        } else if ("maxMessageTextSize".equalsIgnoreCase(value)) {
                            mMaxTextLength = Integer.parseInt(text);
                        } else if ("maxSubjectLength".equalsIgnoreCase(value)) {
                            mMaxSubjectLength = Integer.parseInt(text);
                        }
                    } else if ("string".equals(tag)) {
                        // string config tags go here
                        if ("userAgent".equalsIgnoreCase(value)) {
                            mUserAgent = text;
                        } else if ("uaProfTagName".equalsIgnoreCase(value)) {
                            mUaProfTagName = text;
                        } else if ("uaProfUrl".equalsIgnoreCase(value)) {
                            //MTK_OP01_PROTECT_START
                            if (MmsApp.isTelecomOperator()) {
                                mUaProfUrl = UAPROFURL_OP01;
                                Log.d(TAG, "optr=OP01, UaProfUrl = " + mUaProfUrl);
                            } else {
                            //MTK_OP01_PROTECT_END
                                mUaProfUrl = text;
                            //MTK_OP01_PROTECT_START
                            }
                            //MTK_OP01_PROTECT_END
                        } else if ("httpParams".equalsIgnoreCase(value)) {
                            mHttpParams = text;
                        } else if ("httpParamsLine1Key".equalsIgnoreCase(value)) {
                            mHttpParamsLine1Key = text;
                        } else if ("emailGatewayNumber".equalsIgnoreCase(value)) {
                            mEmailGateway = text;
                        }
                    }
                }
            }
        } catch (XmlPullParserException e) {
            Log.e(TAG, "loadMmsSettings caught ", e);
        } catch (NumberFormatException e) {
            Log.e(TAG, "loadMmsSettings caught ", e);
        } catch (IOException e) {
            Log.e(TAG, "loadMmsSettings caught ", e);
        } finally {
            parser.close();
        }

        String errorStr = null;

        if (getMmsEnabled() && mUaProfUrl == null) {
            errorStr = "uaProfUrl";
        }

        if (errorStr != null) {
            String err =
                String.format("MmsConfig.loadMmsSettings mms_config.xml missing %s setting",
                        errorStr);
            Log.e(TAG, err);
        }
    }
    
    /*
     * Notes:for CMCC customization,whether to enable SL automatically lanuch.
     * default set false
     */
    private static boolean mSlAutoLanuchEnabled = false;
    public static boolean getSlAutoLanuchEnabled(){
        return mSlAutoLanuchEnabled;
    }

    public static void setDeviceStorageFullStatus(boolean bFull) {
        mDeviceStorageFull = bFull;
    }

    public static boolean getDeviceStorageFullStatus() {
        return mDeviceStorageFull;
    }

    // add for cmcc dir ui begin
    public static void setMmsDirMode(boolean mode) {
        SharedPreferences sp = AuroraPreferenceManager.getDefaultSharedPreferences(MmsApp.getApplication());
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("CmccMmsUiMode", mode);
        editor.commit();
    }

    public static boolean getMmsDirMode() {
        SharedPreferences sp = AuroraPreferenceManager.getDefaultSharedPreferences(MmsApp.getApplication());
        boolean dirMode = sp.getBoolean("CmccMmsUiMode", false);
        return dirMode;
    }
    // add for cmcc dir ui end
    
    //Gionee guoyx 20130221 add for CR00773050 begin
    private static Context mContext;
    public static final int SUBSCRIPTION_INVALID = -1;
    public static int sDefaultDataSubscription = SUBSCRIPTION_INVALID;
    
    public static boolean needDataSwitchBack() {
        //Gionee guoyx 20130320 modified for CR00787025 begin        
        int userPref = sDefaultDataSubscription;
        int currentDataSub = GnTelephonyManagerEx.getDefault().getPreferredDataSubscription();
        if (userPref == SUBSCRIPTION_INVALID) {
            Log.e(TAG, " prefer data sub is INVALID");
            return false;
        }
        //Gionee guoyx 20130320 modified for CR00787025 end    
        int userPrefState = GnTelephonyManagerEx.getDefault().getSimState(userPref);
        boolean needBack = (userPref != currentDataSub) &&
                (userPrefState != TelephonyManager.SIM_STATE_ABSENT) &&
                (userPrefState != GnTelephonyManager.SIM_STATE_DEACTIVATED);
        Log.i(TAG,"userPref="+userPref+" currentDataSub="+currentDataSub
                +" userPrefSubState="+userPrefState+" needBack="+needBack );
        return needBack;
    }
    
    public static int getUserPreferDataSubscription() {
        //Gionee guoyx 20130315 modified for CR00778697 begin
        int userPref = -1;
        try{
            // Aurora xuyong 2014-10-13 modified for bug #9032 start
            userPref = Settings.Global.getInt(mContext.getContentResolver(),
                     GnSettings.System.MULTI_SIM_DATA_CALL_SUBSCRIPTION);
            // Aurora xuyong 2014-10-13 modified for bug #9032 end
        }catch(SettingNotFoundException snfe){
            Log.e(TAG, "Settings Exception Reading Dual Sim Data Call Values");
        }
        Log.d(TAG,"getUserPreferDataSubscription user prefer data subscription:" + userPref);
        //Gionee guoyx 20130315 modified for CR00778697 end
        return userPref;
    }
    
    public static boolean restoreDataSubscription() {
        //Gionee guoyx 20130320 modified for CR00787025 begin
        int userPref = sDefaultDataSubscription;
        //Gionee guoyx 20130320 modified for CR00787025 end    
        //Gionee guoyx 20130331 modified for CR00791280 begin
        if (userPref != SUBSCRIPTION_INVALID) {
            Log.d(TAG,"restoreDataSubscription user prefer data subscription:" + userPref);
            return GnTelephonyManagerEx.getDefault().setPreferredDataSubscription(userPref);
        } else {
            Log.d(TAG,"restoreDataSubscription user prefer data subscription is -1");
            return false;
        }
        //Gionee guoyx 20130331 modified for CR00791280 end
    }
    
    public static void clearDefaultDataSubscription() {
        Log.d(TAG,"clearDefaultDataSubscription");
        sDefaultDataSubscription = SUBSCRIPTION_INVALID;
    }
    
    public static void backupDataSubscription(int subscription) {
        Log.d(TAG,"backupDataSubscription subscription:" + subscription);
        sDefaultDataSubscription = subscription;
    }
    //Gionee guoyx 20130221 add for CR00773050 end
    
}
