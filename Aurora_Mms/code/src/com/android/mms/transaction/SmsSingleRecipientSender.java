package com.android.mms.transaction;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Telephony.Mms;
import gionee.provider.GnTelephony.SIMInfo;
import android.provider.Telephony.Sms;
import gionee.telephony.GnSmsManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;

import com.android.mms.data.Conversation;
import com.android.mms.LogTag;
import com.android.mms.MmsConfig;
import com.android.mms.ui.MessageUtils;

// add for gemini
import android.content.SharedPreferences;
import android.os.SystemProperties;
import aurora.preference.AuroraPreferenceManager;
import gionee.telephony.gemini.GnGeminiSmsManager;

import com.gionee.internal.telephony.GnPhone;
import com.android.mms.MmsApp;
import com.android.mms.ui.MessagingPreferenceActivity;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.MmsException;
//Aurora xuyong 2013-11-15 modified for google adapt end
import com.aurora.mms.util.Utils;


public class SmsSingleRecipientSender extends SmsMessageSender {

    private final boolean mRequestDeliveryReport;
    private String mDest;
    private Uri mUri;
    private static final String TAG = "SmsSingleRecipientSender";

    //Gionee: tianxiaolong 2012.8.3 modify for CR00664009 begin
    static final boolean gnFlyFlag = SystemProperties.get("ro.gn.oversea.custom").equals("RUSSIA_FLY");
    //Gionee: tianxiaolong 2012.8.3 modify for CR00664009 end

    public SmsSingleRecipientSender(Context context, String dest, String msgText, long threadId,
            boolean requestDeliveryReport, Uri uri) {
        super(context, null, msgText, threadId);
        mRequestDeliveryReport = requestDeliveryReport;
        mDest = dest;
        mUri = uri;
    }

    // add for gemini
    public SmsSingleRecipientSender(Context context, String dest, String msgText, long threadId,
            boolean requestDeliveryReport, Uri uri, int simId) {
        super(context, null, msgText, threadId, simId);
        mRequestDeliveryReport = requestDeliveryReport;
        mDest = dest;
        mUri = uri;
    }

    public boolean sendMessage(long token) throws MmsException {
        if (LogTag.DEBUG_SEND) {
            Log.v(TAG, "sendMessage token: " + token);
        }
        Log.d(MmsApp.TXN_TAG, "SmsSingleRecipientSender: sendMessage()");
        if (mMessageText == null) {
            // Don't try to send an empty message, and destination should be just
            // one.
            throw new MmsException("Null message body or have multiple destinations.");
        }

        int codingType = SmsMessage.ENCODING_UNKNOWN;
        //MTK_OP03_PROTECT_START
        if (MmsApp.isCmccOperator()) {
            codingType = MessageUtils.getSmsEncodingType(mContext);
        }
        //MTK_OP03_PROTECT_END
        SmsManager smsManager = null;
        if (Utils.hasLollipop()) {
            //Aurora xuyong 2015-04-02 modified for android 5.1+ new feature start
            smsManager = SmsManager.getSmsManagerForSubscriptionId(mSimId);
            //Aurora xuyong 2015-04-02 modified for android 5.1+ new feature end
        } else {
            smsManager = SmsManager.getDefault();
        }
        ArrayList<String> messages = null;
        if ((MmsConfig.getEmailGateway() != null) &&
                (Mms.isEmailAddress(mDest) || MessageUtils.isAlias(mDest))) {
            String msgText;
            msgText = mDest + " " + mMessageText;
            mDest = MmsConfig.getEmailGateway();
            //MTK_OP03_PROTECT_START
            if (MmsApp.getApplication().isCmccOperator()) {
                // Aurora xuyong 2013-11-15 modified for S4 adapt start
                messages = gionee.telephony.GnSmsManager.getDefault().divideMessage(msgText, codingType);
                // Aurora xuyong 2013-11-15 modified for S4 adapt end
            } else {
            //MTK_OP03_PROTECT_END
                messages = smsManager.divideMessage(msgText);
            //MTK_OP03_PROTECT_START
            }
            //MTK_OP03_PROTECT_END
        } else {
            //MTK_OP03_PROTECT_START
            if (MmsApp.getApplication().isCmccOperator()) {
                // Aurora xuyong 2013-11-15 modified for S4 adapt start
                messages = gionee.telephony.GnSmsManager.getDefault().divideMessage(mMessageText, codingType);
                // Aurora xuyong 2013-11-15 modified for S4 adapt end
            } else {
            //MTK_OP03_PROTECT_END
                messages = smsManager.divideMessage(mMessageText);
            //MTK_OP03_PROTECT_START
            }
            //MTK_OP03_PROTECT_END
            
            // remove spaces from destination number (e.g. "801 555 1212" -> "8015551212")
            mDest = mDest.replaceAll(" ", "");
            mDest = mDest.replaceAll("-", "");
            mDest = Conversation.verifySingleRecipient(mContext, mThreadId, mDest);
        }
        int messageCount = messages.size();
        Log.d(MmsApp.TXN_TAG, "SmsSingleRecipientSender: sendMessage(), Message Count=" + messageCount);

        if (messageCount == 0) {
            // Don't try to send an empty message.
            throw new MmsException("SmsSingleRecipientSender.sendMessage: divideMessage returned " +
                    "empty messages. Original message is \"" + mMessageText + "\"");
        }

        boolean moved = Sms.moveMessageToFolder(mContext, mUri, Sms.MESSAGE_TYPE_OUTBOX, 0);
        if (!moved) {
            throw new MmsException("SmsSingleRecipientSender.sendMessage: couldn't move message " +
                    "to outbox: " + mUri);
        }
        if (LogTag.DEBUG_SEND) {
            Log.v(TAG, "sendMessage mDest: " + mDest + " mRequestDeliveryReport: " +
                    mRequestDeliveryReport);
        }

        ArrayList<PendingIntent> deliveryIntents =  new ArrayList<PendingIntent>(messageCount);
        ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>(messageCount);
        // Aurora yudingmin 2014-12-09 modified for thread's app result start
        initSendIntents(deliveryIntents, sentIntents, messageCount);
        // Aurora yudingmin 2014-12-09 modified for thread's app result end
        try {
            Log.d(MmsApp.TXN_TAG, "\t Destination\t= " + mDest);
            Log.d(MmsApp.TXN_TAG, "\t ServiceCenter\t= " + mServiceCenter);
            Log.d(MmsApp.TXN_TAG, "\t Message\t= " + messages);
            Log.d(MmsApp.TXN_TAG, "\t uri\t= " + mUri);
            Log.d(MmsApp.TXN_TAG, "\t CodingType\t= " + codingType);
            //MTK_OP03_PROTECT_START
            if (MmsApp.getApplication().isCmccOperator()) {
                GnSmsManager.getDefault().sendMultipartTextMessageWithEncodingType(smsManager, mDest, mServiceCenter, messages, 
                        codingType, sentIntents, deliveryIntents);
            } else {
            //MTK_OP03_PROTECT_END
                smsManager.sendMultipartTextMessage(mDest, mServiceCenter, messages, sentIntents, deliveryIntents);
            //MTK_OP03_PROTECT_START
            }
            //MTK_OP03_PROTECT_END
            
        } catch (Exception ex) {
            Log.e(TAG, "SmsSingleRecipientSender.sendMessage: caught", ex);
            throw new MmsException("SmsSingleRecipientSender.sendMessage: caught " + ex +
                    " from SmsManager.sendTextMessage()");
        }
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE) || LogTag.DEBUG_SEND) {
            log("SmsSingleRecipientSender: address=" + mDest + ", threadId=" + mThreadId +
                    ", uri=" + mUri + ", msgs.count=" + messageCount);
        }
        return false;
    }
    
// Aurora yudingmin 2014-12-09 added for thread's app result start
    private void initSendIntents(ArrayList<PendingIntent> deliveryIntents, ArrayList<PendingIntent> sentIntents, int messageCount){
        String thirdResponse = getThirdResponse();
        if(TextUtils.isEmpty(thirdResponse)){
            for (int i = 0; i < messageCount; i++) {
                if (mRequestDeliveryReport && (i == (messageCount - 1))) {
                    // TODO: Fix: It should not be necessary to
                    // specify the class in this intent.  Doing that
                    // unnecessarily limits customizability.
                    Intent intent = new Intent(
                                    MessageStatusReceiver.MESSAGE_STATUS_RECEIVED_ACTION,
                                    mUri,
                                    mContext,
                                    MessageStatusReceiver.class);
                    //mark the last part of a sms. only the last part sms status report will be record if sent ok.
                    intent.putExtra(SmsReceiverService.EXTRA_MESSAGE_SENT_SEND_NEXT, true);

                    //the second the parameter is used now! not as the google doc says "currently not used"
                    deliveryIntents.add(PendingIntent.getBroadcast(mContext, i, intent, 0));
                }
                Intent intent  = new Intent(SmsReceiverService.MESSAGE_SENT_ACTION,
                        mUri,
                        mContext,
                        SmsReceiver.class);
                if (i == messageCount -1) {
                    intent.putExtra(SmsReceiverService.EXTRA_MESSAGE_SENT_SEND_NEXT, true);
                }

                // add for concatenation msg
                if (messageCount > 1) {
                    intent.putExtra(SmsReceiverService.EXTRA_MESSAGE_CONCATENATION, true);
                }
                if (LogTag.DEBUG_SEND) {
                    Log.v(TAG, "SmsSingleRecipientSender sendIntent: " + intent);
                }
                sentIntents.add(PendingIntent.getBroadcast(mContext, i, intent, 0));
            }
        } else {
            Log.v(TAG, "Third_Response and uri is " + thirdResponse + ";" + mUri.toString());
            for (int i = 0; i < messageCount; i++) {
                if (mRequestDeliveryReport && (i == (messageCount - 1))) {
                    // TODO: Fix: It should not be necessary to
                    // specify the class in this intent.  Doing that
                    // unnecessarily limits customizability.
                    Intent intent = new Intent(
                                    MessageStatusReceiver.MESSAGE_STATUS_RECEIVED_ACTION,
                                    mUri,
                                    mContext,
                                    MessageStatusReceiver.class);
                    //mark the last part of a sms. only the last part sms status report will be record if sent ok.
                    intent.putExtra(SmsReceiverService.EXTRA_MESSAGE_SENT_SEND_NEXT, true);
                    intent.putExtra(Third_Response, thirdResponse);

                    //the second the parameter is used now! not as the google doc says "currently not used"
                    deliveryIntents.add(PendingIntent.getBroadcast(mContext, i, intent, 0));
                }
                Intent intent  = new Intent(SmsReceiverService.MESSAGE_SENT_ACTION,
                        mUri,
                        mContext,
                        SmsReceiver.class);
                intent.putExtra(Third_Response, thirdResponse);
                if (i == messageCount -1) {
                    intent.putExtra(SmsReceiverService.EXTRA_MESSAGE_SENT_SEND_NEXT, true);
                }

                // add for concatenation msg
                if (messageCount > 1) {
                    intent.putExtra(SmsReceiverService.EXTRA_MESSAGE_CONCATENATION, true);
                }
                if (LogTag.DEBUG_SEND) {
                    Log.v(TAG, "SmsSingleRecipientSender sendIntent: " + intent);
                }
                sentIntents.add(PendingIntent.getBroadcast(mContext, i, intent, 0));
            }
        }
    }
// Aurora yudingmin 2014-12-09 added for thread's app result end

    // add for gemini
    public boolean sendMessageGemini(long token, int simId) throws MmsException {
        // convert sim id to slot id
        int slotId = SIMInfo.getSlotById(mContext, mSimId);
        Log.d(MmsApp.TXN_TAG, "SmsSingleRecipientSender: sendMessageGemini() simId = " + simId +", slotId = " + slotId);
        if (mMessageText == null) {
            // Don't try to send an empty message, and destination should be just
            // one.
            throw new MmsException("Null message body or have multiple destinations.");
        }
        
        int codingType = SmsMessage.ENCODING_UNKNOWN;
        //MTK_OP03_PROTECT_START
        if (MmsApp.isCmccOperator()) {
            codingType = MessageUtils.getSmsEncodingType(mContext);
        }
        //MTK_OP03_PROTECT_END
        //Aurora xuyong 2015-04-02 modified for android 5.1+ new feature start
        SmsManager smsManager = null;
        if (Utils.hasLollipop()) {
            smsManager = SmsManager.getSmsManagerForSubscriptionId(mSimId);
        } else {
            smsManager = SmsManager.getDefault();
        }
        //Aurora xuyong 2015-04-02 modified for android 5.1+ new feature end
        ArrayList<String> messages = null;
        if ((MmsConfig.getEmailGateway() != null) &&
                (Mms.isEmailAddress(mDest) || MessageUtils.isAlias(mDest))) {
            String msgText;
            msgText = mDest + " " + mMessageText;
            mDest = MmsConfig.getEmailGateway();
            //MTK_OP03_PROTECT_START
            if (MmsApp.getApplication().isCmccOperator()) {
                // Aurora xuyong 2013-11-15 modified for S4 adapt start
                messages = gionee.telephony.GnSmsManager.getDefault().divideMessage(msgText, codingType);
                // Aurora xuyong 2013-11-15 modified for S4 adapt end
            } else {
            //MTK_OP03_PROTECT_END
                messages = smsManager.divideMessage(msgText);
            //MTK_OP03_PROTECT_START
            }
            //MTK_OP03_PROTECT_END
        } else {
            //MTK_OP03_PROTECT_START
            if (MmsApp.getApplication().isCmccOperator()) {
                // Aurora xuyong 2013-11-15 modified for S4 adapt start
                messages = gionee.telephony.GnSmsManager.getDefault().divideMessage(mMessageText, codingType);
                // Aurora xuyong 2013-11-15 modified for S4 adapt end
            } else {
            //MTK_OP03_PROTECT_END
                messages = smsManager.divideMessage(mMessageText);
            //MTK_OP03_PROTECT_START
            }
            //MTK_OP03_PROTECT_END
           
            // remove spaces from destination number (e.g. "801 555 1212" -> "8015551212")
            mDest = mDest.replaceAll(" ", "");
            mDest = mDest.replaceAll("-", "");
            mDest = Conversation.verifySingleRecipient(mContext, mThreadId, mDest);
        }
        int messageCount = messages.size();
        Log.d(MmsApp.TXN_TAG, "SmsSingleRecipientSender: sendMessageGemini(), Message Count=" + messageCount);

        if (messageCount == 0) {
            // Don't try to send an empty message.
            throw new MmsException("SmsSingleRecipientSender.sendMessageGemini: divideMessage returned " +
                    "empty messages. Original message is \"" + mMessageText + "\"");
        }

        boolean moved = Sms.moveMessageToFolder(mContext, mUri, Sms.MESSAGE_TYPE_OUTBOX, 0);
        if (!moved) {
            throw new MmsException("SmsSingleRecipientSender.sendMessageGemini: couldn't move message " +
                    "to outbox: " + mUri);
        }

        ArrayList<PendingIntent> deliveryIntents =  new ArrayList<PendingIntent>(messageCount);
        ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>(messageCount);
        // Aurora yudingmin 2014-12-09 modified for thread's app result start
        initSendIntentsGemini(deliveryIntents, sentIntents, messageCount, slotId);
        // Aurora yudingmin 2014-12-09 modified for thread's app result end
        try {
            Log.d(MmsApp.TXN_TAG, "\t Destination\t= " + mDest);
            Log.d(MmsApp.TXN_TAG, "\t ServiceCenter\t= " + mServiceCenter);
            Log.d(MmsApp.TXN_TAG, "\t Message\t= " + messages);
            Log.d(MmsApp.TXN_TAG, "\t uri\t= " + mUri);
            Log.d(MmsApp.TXN_TAG, "\t slotId\t= "+ slotId/*mSimId*/);
            Log.d(MmsApp.TXN_TAG, "\t CodingType\t= " + codingType);
            //MTK_OP03_PROTECT_START
            if (MmsApp.getApplication().isCmccOperator()) {               
                GnGeminiSmsManager.sendMultipartTextMessageWithEncodingTypeGemini(mDest, mServiceCenter, messages, 
                    codingType, slotId/*mSimId*/, sentIntents, deliveryIntents);
            } else {
            //MTK_OP03_PROTECT_END
                //Aurora xuyong 2015-04-02 modified for android 5.1+ new feature end
            	smsManager.sendMultipartTextMessageWithEncodingType(mDest,
                        mServiceCenter, messages, codingType, sentIntents,
                        deliveryIntents);
                /*GnGeminiSmsManager.sendMultipartTextMessageGemini(mDest, mServiceCenter, messages, 
                                slotIdmSimId, sentIntents, deliveryIntents);*/
                //Aurora xuyong 2015-04-02 modified for android 5.1+ new feature end
            //MTK_OP03_PROTECT_START
            }
            //MTK_OP03_PROTECT_END
            Log.d(MmsApp.TXN_TAG, "\t after sendMultipartTextMessageGemini");
        } catch (Exception ex) {
            throw new MmsException("SmsSingleRecipientSender.sendMessageGemini: caught " + ex +
                    " from SmsManager.sendTextMessage()");
        }
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            log("SmsSingleRecipientSender:sendMessageGemini: address=" + mDest + ", threadId=" + mThreadId +
                    ", uri=" + mUri + ", msgs.count=" + messageCount);
        }
        return false;
    }
    
// Aurora yudingmin 2014-12-09 added for thread's app result start
    private void initSendIntentsGemini(ArrayList<PendingIntent> deliveryIntents, ArrayList<PendingIntent> sentIntents, int messageCount, int slotId){
        String thirdResponse = getThirdResponse();
        if(TextUtils.isEmpty(thirdResponse)){
            for (int i = 0; i < messageCount; i++) {
                // gionee zhouyj 2012-09-08 modify for CR00686893 start
                //Gionee: tianxiaolong 2012.8.3 modify for CR00664009 begin
                if(gnFlyFlag || MmsApp.mGnMessageSupport){
                    if (mRequestDeliveryReport && (i == (messageCount - 1))) {
                        // TODO: Fix: It should not be necessary to
                        // specify the class in this intent.  Doing that
                        // unnecessarily limits customizability.
                        Intent drIt = new Intent(
                                        MessageStatusReceiver.MESSAGE_STATUS_RECEIVED_ACTION,
                                        mUri,
                                        mContext,
                                        MessageStatusReceiver.class);
                        drIt.putExtra(GnPhone.GEMINI_SIM_ID_KEY, slotId/*mSimId*/);             
                        //mark the last part of a sms. only the last part sms status report will be record if sent ok.
                        drIt.putExtra(SmsReceiverService.EXTRA_MESSAGE_SENT_SEND_NEXT, true);  
                        //the second the parameter is used now! not as the google doc says "currently not used"                
                        deliveryIntents.add(PendingIntent.getBroadcast(mContext, i, drIt, 0));
                    } else {
                        deliveryIntents.add(null);
                    }
                    if(i == (messageCount - 1)) {
                        Intent intent  = new Intent(SmsReceiverService.MESSAGE_SENT_ACTION,
                                mUri,
                                mContext,
                                SmsReceiver.class);
                        intent.putExtra(SmsReceiverService.EXTRA_MESSAGE_SENT_SEND_NEXT, true);
                        // add for concatenation msg
                        intent.putExtra(GnPhone.GEMINI_SIM_ID_KEY, slotId/*mSimId*/);
                        sentIntents.add(PendingIntent.getBroadcast(mContext, i, intent, 0));
                    }
                }else{
                if (mRequestDeliveryReport) {
                    // TODO: Fix: It should not be necessary to
                    // specify the class in this intent.  Doing that
                    // unnecessarily limits customizability.
                    Intent drIt = new Intent(
                                    MessageStatusReceiver.MESSAGE_STATUS_RECEIVED_ACTION,
                                    mUri,
                                    mContext,
                                    MessageStatusReceiver.class);
                    drIt.putExtra(GnPhone.GEMINI_SIM_ID_KEY, slotId/*mSimId*/);
                    //mark the last part of a sms. only the last part sms status report will be record if sent ok.
                    if (i == messageCount -1) {
                        drIt.putExtra(SmsReceiverService.EXTRA_MESSAGE_SENT_SEND_NEXT, true);
                    }
                    //the second the parameter is used now! not as the google doc says "currently not used"                
                    deliveryIntents.add(PendingIntent.getBroadcast(mContext, i, drIt, 0));
                }
                Intent intent  = new Intent(SmsReceiverService.MESSAGE_SENT_ACTION,
                        mUri,
                        mContext,
                        SmsReceiver.class);
                if (i == messageCount -1) {
                    intent.putExtra(SmsReceiverService.EXTRA_MESSAGE_SENT_SEND_NEXT, true);
                }
                
                // add for concatenation msg
                if (messageCount > 1) {
                    intent.putExtra(SmsReceiverService.EXTRA_MESSAGE_CONCATENATION, true);
                }
                
                intent.putExtra(GnPhone.GEMINI_SIM_ID_KEY, slotId/*mSimId*/);
                sentIntents.add(PendingIntent.getBroadcast(mContext, i, intent, 0));
                }    
                //Gionee: tianxiaolong 2012.8.3 modify for CR00664009 end
                // gionee zhouyj 2012-09-08 modify for CR00686893 end
            }
        } else {
            Log.v(TAG, "Third_Response and uri is " + thirdResponse + ";" + mUri.toString());
            for (int i = 0; i < messageCount; i++) {
                // gionee zhouyj 2012-09-08 modify for CR00686893 start
                //Gionee: tianxiaolong 2012.8.3 modify for CR00664009 begin
                if(gnFlyFlag || MmsApp.mGnMessageSupport){
                    if (mRequestDeliveryReport && (i == (messageCount - 1))) {
                        // TODO: Fix: It should not be necessary to
                        // specify the class in this intent.  Doing that
                        // unnecessarily limits customizability.
                        Intent drIt = new Intent(
                                        MessageStatusReceiver.MESSAGE_STATUS_RECEIVED_ACTION,
                                        mUri,
                                        mContext,
                                        MessageStatusReceiver.class);
                        drIt.putExtra(Third_Response, thirdResponse);
                        drIt.putExtra(GnPhone.GEMINI_SIM_ID_KEY, slotId/*mSimId*/);             
                        //mark the last part of a sms. only the last part sms status report will be record if sent ok.
                        drIt.putExtra(SmsReceiverService.EXTRA_MESSAGE_SENT_SEND_NEXT, true);  
                        //the second the parameter is used now! not as the google doc says "currently not used"                
                        deliveryIntents.add(PendingIntent.getBroadcast(mContext, i, drIt, 0));
                    } else {
                        deliveryIntents.add(null);
                    }
                    if(i == (messageCount - 1)) {
                        Intent intent  = new Intent(SmsReceiverService.MESSAGE_SENT_ACTION,
                                mUri,
                                mContext,
                                SmsReceiver.class);
                        intent.putExtra(Third_Response, thirdResponse);
                        intent.putExtra(SmsReceiverService.EXTRA_MESSAGE_SENT_SEND_NEXT, true);
                        // add for concatenation msg
                        intent.putExtra(GnPhone.GEMINI_SIM_ID_KEY, slotId/*mSimId*/);
                        sentIntents.add(PendingIntent.getBroadcast(mContext, i, intent, 0));
                    }
                }else{
                if (mRequestDeliveryReport) {
                    // TODO: Fix: It should not be necessary to
                    // specify the class in this intent.  Doing that
                    // unnecessarily limits customizability.
                    Intent drIt = new Intent(
                                    MessageStatusReceiver.MESSAGE_STATUS_RECEIVED_ACTION,
                                    mUri,
                                    mContext,
                                    MessageStatusReceiver.class);
                    drIt.putExtra(Third_Response, thirdResponse);
                    drIt.putExtra(GnPhone.GEMINI_SIM_ID_KEY, slotId/*mSimId*/);
                    //mark the last part of a sms. only the last part sms status report will be record if sent ok.
                    if (i == messageCount -1) {
                        drIt.putExtra(SmsReceiverService.EXTRA_MESSAGE_SENT_SEND_NEXT, true);
                    }
                    //the second the parameter is used now! not as the google doc says "currently not used"                
                    deliveryIntents.add(PendingIntent.getBroadcast(mContext, i, drIt, 0));
                }
                Intent intent  = new Intent(SmsReceiverService.MESSAGE_SENT_ACTION,
                        mUri,
                        mContext,
                        SmsReceiver.class);
                intent.putExtra(Third_Response, thirdResponse);
                if (i == messageCount -1) {
                    intent.putExtra(SmsReceiverService.EXTRA_MESSAGE_SENT_SEND_NEXT, true);
                }
                
                // add for concatenation msg
                if (messageCount > 1) {
                    intent.putExtra(SmsReceiverService.EXTRA_MESSAGE_CONCATENATION, true);
                }
                
                intent.putExtra(GnPhone.GEMINI_SIM_ID_KEY, slotId/*mSimId*/);
                sentIntents.add(PendingIntent.getBroadcast(mContext, i, intent, 0));
                }    
                //Gionee: tianxiaolong 2012.8.3 modify for CR00664009 end
                // gionee zhouyj 2012-09-08 modify for CR00686893 end
            }
        }
    }
// Aurora yudingmin 2014-12-09 added for thread's app result end


    private void log(String msg) {
        Log.d(LogTag.TAG, "[SmsSingleRecipientSender] " + msg);
    }
}
