package com.mediatek.contacts;

import android.app.Activity;
import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.ServiceManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.telephony.TelephonyManager;
import android.provider.Settings;
import gionee.provider.GnTelephony.SIMInfo;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.android.contacts.R;
import com.android.contacts.activities.ContactsLog;
import com.android.contacts.util.Constants;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.ContactsApplication;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.ITelephony;

import android.app.ProgressDialog;
import android.view.WindowManager;

import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import com.mediatek.contacts.CallOptionHelper.CallbackArgs;
import com.mediatek.contacts.widget.SimPickerDialog;
//import com.mediatek.CellConnService.CellConnMgr;
import com.gionee.CellConnService.GnCellConnMgr;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;

import gionee.telephony.GnTelephonyManager;

import com.gionee.internal.telephony.GnITelephony;

//GIONEE:liuying 2012-7-5 modify for CR00637517 start
import java.util.List;
//GIONEE:liuying 2012-7-5 modify for CR00637517 end


import aurora.app.AuroraProgressDialog;

public class CallOptionHandler implements CallOptionHelper.Callback,
        DialogInterface.OnDismissListener, DialogInterface.OnClickListener {

    private static final String TAG = "CallOptionHandler";

    private static final String PACKAGE = "com.android.phone";
    private static final String OUTGOING_CALL_RECEIVER = "com.android.phone.OutgoingCallReceiver";
    private AuroraProgressDialog mProgressDialog = null;

    protected Intent mIntent;

    protected String mNumber;

    protected Dialog[] mDialogs = new Dialog[CallOptionHelper.MAKE_CALL_REASON_MAX+1];

    protected OnHandleCallOption mOnHandleCallOption;

    protected int mReason = CallOptionHelper.MAKE_CALL_REASON_OK;

    protected ContactsApplication mApp;
    protected Context mContext;
    protected CallOptionHelper mCallOptionHelper;
    protected GnCellConnMgr mCellConnMgr;

    protected boolean mClicked = false;
    protected boolean mAssociateSimMissingClicked = false;
    protected CallOptionHelper.AssociateSimMissingArgs mAssociateSimMissingArgs;

    //GIONEE:liuying 2012-7-5 modify for CR00637517 start
    private int mSimCount = 0;
	List<SIMInfo> mSimInfoList = null;
    //GIONEE:liuying 2012-7-5 modify for CR00637517 end

    public CallOptionHandler(Context context) {
        mContext = context;

        mCallOptionHelper = CallOptionHelper.getInstance(mContext);
        mCallOptionHelper.setCallback(this);

        mApp = ContactsApplication.getInstance();
        mCellConnMgr = mApp.cellConnMgr;
    }

    public void onCreate(Bundle savedInstanceState) {
        //
    }

    public void onStop() {
        for (Dialog dialog : mDialogs) {
            if (dialog != null) dialog.dismiss();
        }
		
        dismissProgressIndication();
    }

    public void setOnHandleCallOption(OnHandleCallOption onHandleCallOption) {
        mOnHandleCallOption = onHandleCallOption;
    }

    public void onDismiss(DialogInterface arg0) {
        log("onDismiss, mClicked = " + mClicked);
        if (arg0 == mDialogs[CallOptionHelper.MAKE_CALL_REASON_ASK]) {
            if (!mClicked)
                handleCallOptionComplete();
            mDialogs[CallOptionHelper.MAKE_CALL_REASON_ASK] = null;
        } else if(arg0 == mDialogs[CallOptionHelper.MAKE_CALL_REASON_ASSOCIATE_MISSING]) {
            if(!mAssociateSimMissingClicked)
                handleCallOptionComplete();
        } else
            handleCallOptionComplete();
    }

    protected void handleCallOptionComplete() {
        if(mOnHandleCallOption != null)
            mOnHandleCallOption.onHandleCallOption(mReason);
    }

    public void onClick(DialogInterface dialog, int which) {
        Profiler.trace(Profiler.CallOptionHandlerEnterOnClick);
        log("onClick, dialog = "+dialog+" which = "+which);
        Intent intent;
        if(dialog == mDialogs[CallOptionHelper.MAKE_CALL_REASON_ASK]) {
            final AuroraAlertDialog alert = (AuroraAlertDialog) dialog;
            final ListAdapter listAdapter = alert.getListView().getAdapter();
            final int slot = ((Integer)listAdapter.getItem(which)).intValue();

            log("onClick, slot = "+slot);
            if(slot == (int)ContactsFeatureConstants.VOICE_CALL_SIM_SETTING_INTERNET) {
                if (mIntent.getBooleanExtra(Constants.EXTRA_IS_IP_DIAL, false)) {
                    Toast.makeText(mContext, R.string.ip_dial_error_toast_for_sip_call_selected, Toast.LENGTH_SHORT)
                    .show();
                } else {
                    intent = newCallBroadcastIntent(mNumber, CallOptionHelper.DIAL_TYPE_SIP, 0);
                    //make sip call
                    CallbackArgs callbackArgs = mCallOptionHelper.new CallbackArgs();
                    //callbackArgs.args = mAssociateSimMissingArgs.suggested;
                    callbackArgs.reason = CallOptionHelper.MAKE_CALL_REASON_OK;
                    callbackArgs.type = CallOptionHelper.DIAL_TYPE_SIP;
                    onMakeCall(callbackArgs);
                }
                
                handleCallOptionComplete();
            } else {
                mIntent.putExtra(Constants.EXTRA_SLOT_ID, slot);

                if(needToCheckSIMStatus(slot)) {
                    if(slot >= 0) {
                        final int result = mCellConnMgr.handleCellConn(slot, GnCellConnMgr.REQUEST_TYPE_ROAMING, mRunnable);
                        log("result = "+result);
                        if(result == mCellConnMgr.RESULT_WAIT){
                            showProgressIndication();
                        }         						
                    } else
                        handleCallOptionComplete();
                } else {
                    final boolean bailout = afterCheckSIMStatus(GnCellConnMgr.RESULT_STATE_NORMAL, slot);
                    if(bailout)
                        handleCallOptionComplete();
                }
            }
            dialog.dismiss();
            mClicked = true;
        } else if(dialog == mDialogs[CallOptionHelper.MAKE_CALL_REASON_ASSOCIATE_MISSING]) {
            AuroraAlertDialog alert = (AuroraAlertDialog) dialog;
            if(mAssociateSimMissingArgs != null) {
                if(which == alert.BUTTON_POSITIVE) {
                    if(mAssociateSimMissingArgs.viaSimInfo != null) {
                        // via SIM
                        final int slot = mAssociateSimMissingArgs.viaSimInfo.mSlot;
                        mIntent.putExtra(Constants.EXTRA_SLOT_ID, slot);

                        // do not call CellConnService to avoid performance issues
                        if(needToCheckSIMStatus(slot)) {
                            if(slot >= 0) {
                                final int result = mCellConnMgr.handleCellConn(slot, GnCellConnMgr.REQUEST_TYPE_ROAMING, mRunnable);
                                log("result = "+result);
                                if(result == mCellConnMgr.RESULT_WAIT){
                                    showProgressIndication();
                                }							
                            } else
                                handleCallOptionComplete();
                        } else {
                            final boolean bailout = afterCheckSIMStatus(GnCellConnMgr.RESULT_STATE_NORMAL, slot);
                            if(bailout)
                                handleCallOptionComplete();
                        }
                    } else {
                        // via internet
                        intent = newCallBroadcastIntent(mNumber, CallOptionHelper.DIAL_TYPE_SIP, 0);
                        mContext.sendBroadcast(intent);
                        handleCallOptionComplete();
                    }
                } else if(which == alert.BUTTON_NEGATIVE) {
                    // user click 'other' button, show SIM selection dialog
                    // with default SIM suggested
                    CallbackArgs callbackArgs = mCallOptionHelper.new CallbackArgs();
                    callbackArgs.args = mAssociateSimMissingArgs.suggested;
                    callbackArgs.reason = CallOptionHelper.MAKE_CALL_REASON_ASK;
                    onMakeCall(callbackArgs);
                }
                mAssociateSimMissingClicked = true;
                mAssociateSimMissingArgs = null;
            }
            dialog.dismiss();
        }
        Profiler.trace(Profiler.CallOptionHandlerLeaveOnClick);
    }

    void log(String msg) {
        Log.d(TAG, msg);
    }

    public void startActivity(Intent intent) {
        Profiler.trace(Profiler.CallOptionHandlerEnterStartActivity);
        log("startActivity, intent = "+intent);
        mIntent = intent;
        if(FeatureOption.MTK_GEMINI_SUPPORT) {
            try {
                mNumber = PhoneNumberUtils.getNumberFromIntent(intent, mContext);
            } catch(Exception e) {
                log(e.getMessage());
            }
            ContactsLog.log("mNumber="+mNumber);

            if(PhoneNumberUtils.isEmergencyNumber(mNumber) || intent.getIntExtra(Constants.EXTRA_SLOT_ID, -1) != -1) {
                // user ask to use one specific sim, do the things~
                final Intent broadcastIntent = newCallBroadcastIntent(mNumber, CallOptionHelper.DIAL_TYPE_VOICE, 0);
                mContext.sendBroadcast(broadcastIntent);
                if(mOnHandleCallOption != null)
                    handleCallOptionComplete();
                return;
            }
            ContactsLog.log("mNumber2="+mNumber);
            mCallOptionHelper.makeCall(intent);
        } else {
            intent.setClassName(Constants.PHONE_PACKAGE, Constants.OUTGOING_CALL_BROADCASTER);
            mContext.startActivity(intent);
        }
        Profiler.trace(Profiler.CallOptionHandlerLeaveStartActivity);
    }

    public void onMakeCall(final CallbackArgs args) {

        log("onMakeCall, reason = "+args.reason+" args = "+args.args);

        mReason = args.reason;

        switch (args.reason) {
            case CallOptionHelper.MAKE_CALL_REASON_OK: {
                int slot = -1;

                if((args.type == CallOptionHelper.DIAL_TYPE_SIP)&&(!(Constants.VOICEMAIL_URI.equals(mIntent.getData().toString())))) {
                    if (mIntent.getBooleanExtra(Constants.EXTRA_IS_IP_DIAL, false)) {
                        Toast.makeText(mContext, R.string.ip_dial_error_toast_for_sip_call_selected, Toast.LENGTH_SHORT)
                        .show();
                    } else {
                        // start sip call option handler
                        Intent broadcastIntent = newCallBroadcastIntent(mNumber, CallOptionHelper.DIAL_TYPE_SIP, 0);
                        mContext.sendBroadcast(broadcastIntent);
                    }
                    handleCallOptionComplete();
                    break;
                } else if (args.type == CallOptionHelper.DIAL_TYPE_VIDEO) {
                    // args.id is already slot id if video call
                    slot = (int)args.id;
                } else {
                    slot = SIMInfoWrapper.getDefault().getSimSlotById((int)args.id);
                    //GIONEE:liuying 2012-7-5 modify for CR00637517 start
					//Gionee:tianliang 2012.10.9 modify for CR00693683 begin
                    if (ContactsApplication.sGnGeminiDialSupport && !Constants.VOICEMAIL_URI.equals(mIntent.getData().toString())) {
					//Gionee:tianliang 2012.10.9 modify for CR00693683 end
						mSimInfoList = SIMInfo.getInsertedSIMList(mContext);
						mSimCount = mSimInfoList.isEmpty() ? 0 : mSimInfoList.size();						
						if (mSimCount>1){
							slot = (int)args.id;
						}
                    }
                    //GIONEE:liuying 2012-7-5 modify for CR00637517 end

                    // if slot == -1, it's likely that no sim cards inserted
                    // using slot 0 by default
                    if(slot == -1) {
                        slot = 0;
                    }
                }
                mIntent.putExtra(Constants.EXTRA_SLOT_ID, slot);

                if(needToCheckSIMStatus(slot)) {
                    if(slot >= 0) {
                        final int result = mCellConnMgr.handleCellConn(slot, GnCellConnMgr.REQUEST_TYPE_ROAMING, mRunnable);
                        log("result = "+result);
                        if(result == mCellConnMgr.RESULT_WAIT){
                            showProgressIndication();
                        }
                    } else
                        handleCallOptionComplete();
                } else {
                    final boolean bialout = afterCheckSIMStatus(GnCellConnMgr.RESULT_STATE_NORMAL, slot);
                    if(bialout)
                        handleCallOptionComplete();
                }
            }
            break;

            case CallOptionHelper.MAKE_CALL_REASON_3G_SERVICE_OFF: {
                AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(mContext);

                boolean bIsInsertSim = true;
                final ITelephony phoneMgr = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
                if (null != phoneMgr) {
                    try {
                        if (!GnITelephony.isSimInsert(phoneMgr, ContactsFeatureConstants.GEMINI_SIM_1)
                                && !GnITelephony.isSimInsert(phoneMgr, ContactsFeatureConstants.GEMINI_SIM_2)) {
                            bIsInsertSim = false;
                        }
                    } catch(Exception e) {
                        bIsInsertSim = false;
                    }
                }
                if (!bIsInsertSim) {
                    builder.setTitle(R.string.reminder)// R.string.reminder)
                           .setMessage(R.string.callFailed_simError)
                           .setNegativeButton(android.R.string.ok, (DialogInterface.OnClickListener) null);
                } else {
                    builder.setTitle(R.string.reminder)//R.string.reminder)
                            .setMessage(R.string.turn_on_3g_service_message).setNegativeButton(
                                    android.R.string.no, (DialogInterface.OnClickListener) null)
                            .setPositiveButton(android.R.string.yes,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent();
                                            intent.setClassName("com.android.phone",
                                                    "com.android.phone.Modem3GCapabilitySwitch");
                                            mContext.startActivity(intent);
                                        }
                                    });
                }
                Dialog dialog = builder.create();
                dialog.setOnDismissListener(this);
                mDialogs[args.reason] = dialog;
                dialog.show();
            }
            break;

            case CallOptionHelper.MAKE_CALL_REASON_SIP_DISABLED: {
                AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(mContext);
                builder.setTitle(R.string.reminder)//(R.string.reminder)
                       .setMessage(R.string.enable_sip_dialog_message)//(R.string.enable_sip_dialog_message)
                       .setNegativeButton(android.R.string.no, (DialogInterface.OnClickListener)null)
                       .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setClassName("com.android.phone", "com.android.phone.SipCallSetting");
                            mContext.startActivity(intent);
                        }
                    });
                Dialog dialog = builder.create();
                dialog.setOnDismissListener(this);
                mDialogs[args.reason] = dialog;
                dialog.show();
            }
            break;

            case CallOptionHelper.MAKE_CALL_REASON_SIP_NO_INTERNET: {

            }
            break;

            case CallOptionHelper.MAKE_CALL_REASON_SIP_START_SETTINGS: {

            }
            break;

            case CallOptionHelper.MAKE_CALL_REASON_ASK: {
                final boolean isVoicemail = Constants.VOICEMAIL_URI.equals(mIntent.getData().toString());
                final boolean internet = !isVoicemail;
                final Resources r = mContext.getResources();
                final String title = isVoicemail ? r.getString(R.string.voicemail) : r.getString(R.string.sim_manage_call_via);
                if(mDialogs[args.reason] != null && mDialogs[args.reason].isShowing()) {
                    log("original SIM selection is showing, bail out...");
                    return;
                }

                AuroraAlertDialog dialog = SimPickerDialog.create(mContext, title, ((Long)args.args).longValue(), internet, this);
                dialog.setOnDismissListener(this);
                mDialogs[args.reason] = dialog;
                mClicked = false;
                dialog.show();
            }
            break;

            case CallOptionHelper.MAKE_CALL_REASON_ASSOCIATE_MISSING: {
                Resources resources = mContext.getResources();
                CallOptionHelper.AssociateSimMissingArgs associateSimMissingArgs = (CallOptionHelper.AssociateSimMissingArgs) args.args;

                AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(mContext);

                final long associateSim = args.id;
                SIMInfo associateSimInfo = SIMInfoWrapper.getDefault().getSimInfoById((int)associateSim);
                String associateSimName = "";
                if (associateSimInfo != null)
                    associateSimName = associateSimInfo.mDisplayName;

                String viaSimName;
                if (associateSimMissingArgs.viaSimInfo != null)
                    viaSimName = associateSimMissingArgs.viaSimInfo.mDisplayName;
                else
                    viaSimName = mContext.getResources().getString(
                            R.string.label_sip_address);

                String message = mContext.getResources().getString(
                        R.string.associate_sim_missing_message, associateSimName, viaSimName);
                builder.setTitle(args.number).setMessage(message).setPositiveButton(
                        android.R.string.yes, this);

                if(associateSimMissingArgs.type == CallOptionHelper.AssociateSimMissingArgs.ASSOCIATE_SIM_MISSING_YES_NO)
                    builder.setNegativeButton(resources.getString(android.R.string.cancel), null);
                else if(associateSimMissingArgs.type == CallOptionHelper.AssociateSimMissingArgs.ASSOCIATE_SIM_MISSING_YES_OTHER)
                    builder.setNegativeButton(resources.getString(R.string.associate_sim_missing_other), this);

                Dialog dialog = builder.create();
                dialog.setOnDismissListener(this);
                mDialogs[args.reason] = dialog;
                dialog.show();
                mAssociateSimMissingArgs = associateSimMissingArgs;
                mAssociateSimMissingClicked = false;
            }
            break;

            case CallOptionHelper.MAKE_CALL_REASON_MISSING_VOICE_MAIL_NUMBER: {
                AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(mContext);
                builder.setTitle(R.string.no_vm_number)
                       .setMessage(R.string.no_vm_number_msg)
                       .setNegativeButton(android.R.string.no, (DialogInterface.OnClickListener)null)
                       .setPositiveButton(R.string.add_vm_number_str, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.setClassName(Constants.PHONE_PACKAGE, Constants.CALL_SETTINGS_CLASS_NAME);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra(ContactsFeatureConstants.GEMINI_SIM_ID_KEY, (int)args.id);
                            mContext.startActivity(intent);
                        }
                    });
                Dialog dialog = builder.create();
                dialog.setOnDismissListener(this);
                mDialogs[args.reason] = dialog;
                dialog.show();
            }
            break;
        }
    }

    private Intent newCallBroadcastIntent(String number, int type, int slot) {
        Intent intent = new Intent(OUTGOING_CALL_RECEIVER);
        intent.setClassName(PACKAGE, OUTGOING_CALL_RECEIVER);

        if(type == CallOptionHelper.DIAL_TYPE_VIDEO)
            intent.putExtra(Constants.EXTRA_IS_VIDEO_CALL, true);

        if(type == CallOptionHelper.DIAL_TYPE_SIP)
            intent.setData(Uri.fromParts("sip", number, null));
        else {
            intent.setData(Uri.fromParts("tel", number, null));
            intent.putExtra(IntentFactory.KEY_AUTO_IP_DIAL_IF_SUPPORT, true);
        }

        intent.putExtra(Constants.EXTRA_SLOT_ID, slot);

        return intent;
    }

    private String queryIPPrefix(int slot) {
        final SIMInfo simInfo = SIMInfoWrapper.getDefault().getSimInfoBySlot(slot);
        StringBuilder builder = new StringBuilder();
        builder.append("ipprefix");
        builder.append(simInfo.mSimId);
        final String key = builder.toString();
        
        final String ipPrefix = Settings.System.getString(mContext.getContentResolver(), key);
        log("queryIPPrefix, ipPrefix = "+ipPrefix);
        return ipPrefix;
    }

    /**
     * 
     * @param result
     * @param slot
     * @return true  : force OutgoingCallBroadcaster to be finished
     *         false : do not finish OutgoingCallbroadcaster
     */
    private boolean afterCheckSIMStatus(int result, int slot) {
        log("afterCheckSIMStatus, result = "+result+" slot = "+slot);

        if(result != GnCellConnMgr.RESULT_STATE_NORMAL) {
            return true;
        }

        // ip dial only support voice call
        boolean noSim = SIMInfoWrapper.getDefault().getInsertedSimCount() == 0;
        log("afterCheckSIMStatus, noSim="+noSim);
        if(!mIntent.getBooleanExtra(Constants.EXTRA_IS_VIDEO_CALL, false) && mIntent.getBooleanExtra(Constants.EXTRA_IS_IP_DIAL, false) && !noSim) {
            final String ipPrefix = queryIPPrefix(slot);
            if(TextUtils.isEmpty(ipPrefix)) {
                final Intent intent = new Intent("com.android.phone.MAIN");
                intent.setClassName("com.android.phone", "com.android.phone.CallSettings");
                final SIMInfo simInfo = SIMInfoWrapper.getDefault().getSimInfoBySlot(slot);
                intent.putExtra(ContactsFeatureConstants.GEMINI_SIM_ID_KEY, simInfo.mSimId);
                mContext.startActivity(intent);
                //pop toast to notify user.
                Toast.makeText(mContext, R.string.ip_dial_error_toast_for_no_ip_prefix_number, Toast.LENGTH_SHORT)
                .show();
                return true;
            } else {
                if(mNumber.indexOf(ipPrefix) != 0)
                    mIntent.putExtra(Constants.EXTRA_ACTUAL_NUMBER_TO_DIAL, ipPrefix+mNumber);
            }
        }

        // a little tricky here, check the voice mail number
        if(Constants.VOICEMAIL_URI.equals(mIntent.getData().toString())) {
            TelephonyManager telephonyManager =
                (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            if(TextUtils.isEmpty(GnTelephonyManager.getVoiceMailNumberGemini(slot))) {
                CallbackArgs callbackArgs = mCallOptionHelper.new CallbackArgs();
                callbackArgs.reason = CallOptionHelper.MAKE_CALL_REASON_MISSING_VOICE_MAIL_NUMBER;
                callbackArgs.type = CallOptionHelper.DIAL_TYPE_VOICE;
                callbackArgs.id = slot;
                onMakeCall(callbackArgs);
                return false;
            } else {
                // put the voicemail number to the intent
                final String voicemailNumber = GnTelephonyManager.getVoiceMailNumberGemini(slot);
                mIntent.putExtra(Constants.EXTRA_ACTUAL_NUMBER_TO_DIAL, voicemailNumber);
            }
        }
        
        String number = getInitialNumber(mIntent);
        final int type = mIntent.getBooleanExtra(Constants.EXTRA_IS_VIDEO_CALL, false) ? CallOptionHelper.DIAL_TYPE_VIDEO : CallOptionHelper.DIAL_TYPE_VOICE;
        final Intent broadcastIntent = newCallBroadcastIntent(number, type, slot);
        mContext.sendBroadcast(broadcastIntent);
        log("afterCheckSIMStatus, number="+number);
        return true;
    }

    private Runnable mRunnable = new Runnable() {
        public void run() {
            Profiler.trace(Profiler.CallOptionHandlerEnterRun);
            final int result = mCellConnMgr.getResult();
            final int slot = mCellConnMgr.getPreferSlot();
            log("run, result = "+result+" slot = "+slot);
            dismissProgressIndication();

            final boolean bailout = afterCheckSIMStatus(result, slot);
            Profiler.trace(Profiler.CallOptionHandlerLeaveRun);
            if(bailout)
                handleCallOptionComplete();
        }
    };

    private boolean isRoamingNeeded(int slot) {
        log("isRoamingNeeded slot = " + slot);
        if (slot == ContactsFeatureConstants.GEMINI_SIM_2) {
            log("isRoamingNeeded = " + SystemProperties.getBoolean("gsm.roaming.indicator.needed.2", false));
            return SystemProperties.getBoolean("gsm.roaming.indicator.needed.2", false);
        } else {
            log("isRoamingNeeded = " + SystemProperties.getBoolean("gsm.roaming.indicator.needed", false));
            return SystemProperties.getBoolean("gsm.roaming.indicator.needed", false);
        }
    }

    private boolean roamingRequest(int slot) {
        log("roamingRequest slot = " + slot);
        boolean bRoaming = false;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            bRoaming = GnTelephonyManager.isNetworkRoamingGemini(slot);
        } else {
            bRoaming = TelephonyManager.getDefault().isNetworkRoaming();
        }

        if (bRoaming) {
            log("roamingRequest slot = " + slot + " is roaming");
        } else {
            log("roamingRequest slot = " + slot + " is not roaming");
            return false;
        }

        if (0 == Settings.System.getInt(mContext.getContentResolver(),
                ContactsFeatureConstants.ROAMING_REMINDER_MODE_SETTING, -1)
                && isRoamingNeeded(slot)) {
            log("roamingRequest reminder once and need to indicate");
            return true;
        }

        if (1 == Settings.System.getInt(mContext.getContentResolver(),
                ContactsFeatureConstants.ROAMING_REMINDER_MODE_SETTING, -1)) {
            log("roamingRequest reminder always");
            return true;
        }

        log("roamingRequest result = false");
        return false;
    }

    private boolean needToCheckSIMStatus(int slot) {
        final ITelephony phoneMgr = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
        try {
            if (slot < 0 || !GnITelephony.isSimInsert(phoneMgr, slot)) {
                log("the sim not insert, bail out!");
                return false;
            }
            
            if (!GnITelephony.isRadioOnGemini(phoneMgr, slot)) {
                return true;
            }
        } catch (Exception e) {
            log(e.toString());
        }
            
        return GnTelephonyManager.getSimStateGemini(slot) != TelephonyManager.SIM_STATE_READY
                || roamingRequest(slot);
    }

    public String getInitialNumber(Intent intent) {
        log("getInitialNumber(): " + intent);

        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return null;
        }

        // If the EXTRA_ACTUAL_NUMBER_TO_DIAL extra is present, get the phone
        // number from there. (That extra takes precedence over the actual data
        // included in the intent.)
        if (intent.hasExtra(Constants.EXTRA_ACTUAL_NUMBER_TO_DIAL)) {
            String actualNumberToDial = intent
                    .getStringExtra(Constants.EXTRA_ACTUAL_NUMBER_TO_DIAL);
            return actualNumberToDial;
        }

        return PhoneNumberUtils.getNumberFromIntent(intent, mContext);
    }

    public interface OnHandleCallOption {
        public void onHandleCallOption(int reason);
    }

    /**
     * Show an onscreen "progress indication" with the specified title and message.
     */
    private void showProgressIndication() {
        log("showProgressIndication(searching network message )");

        // TODO: make this be a no-op if the progress indication is
        // already visible with the exact same title and message.

        dismissProgressIndication();  // Clean up any prior progress indication

        mProgressDialog = new AuroraProgressDialog(mContext);
        //mProgressDialog.setTitle(getText(titleResId));
        mProgressDialog.setMessage(mContext.getResources().getString(R.string.sum_search_networks));	
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(true);
        mProgressDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        mProgressDialog.show();
    }

    /**
     * Dismiss the onscreen "progress indication" (if present).
     */
    private void dismissProgressIndication() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss(); // safe even if already dismissed
            mProgressDialog = null;
        }
    }    	
}
