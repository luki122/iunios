/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.mediatek.phone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;

import android.net.LinkProperties;

import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.telephony.SubInfoRecord;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;


import com.android.phone.PhoneGlobals;

import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.ProxyController;
import com.android.internal.telephony.RILConstants;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;
import com.android.internal.telephony.uicc.IccIoResult;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccController;


import com.mediatek.internal.telephony.ITelephonyEx;
import com.android.internal.telephony.ITelephony;

import com.android.internal.telephony.dataconnection.DcFailCause;

import com.mediatek.internal.telephony.BtSimapOperResponse;
import java.util.ArrayList;

import java.util.Iterator;
import android.os.Messenger;
import android.os.IBinder;

// VOLTE
import com.mediatek.internal.telephony.DedicateBearerProperties;
import com.mediatek.internal.telephony.DefaultBearerConfig;
//import com.mediatek.internal.telephony.ltedc.svlte.SvlteRatController;
import com.mediatek.internal.telephony.QosStatus;

import com.mediatek.internal.telephony.RadioManager;

import com.mediatek.internal.telephony.TftStatus;

import com.mediatek.telephony.TelephonyManagerEx;


/**
 * Implementation of the ITelephony interface.
 */
public class PhoneInterfaceManagerEx extends ITelephonyEx.Stub {

    private static final String LOG_TAG = "PhoneInterfaceManagerEx";
    private static final boolean DBG = true;

    /** The singleton instance. */
    private static PhoneInterfaceManagerEx sInstance;

    PhoneGlobals mApp;
    Phone mPhone;

    MainThreadHandler mMainThreadHandler;

    // Query SIM phonebook Adn stroage info thread
    private QueryAdnInfoThread mAdnInfoThread = null;

    /* SMS Center Address start*/
    private static final int CMD_HANDLE_GET_SCA = 11;
    private static final int CMD_GET_SCA_DONE = 12;
    private static final int CMD_HANDLE_SET_SCA = 13;
    private static final int CMD_SET_SCA_DONE = 14;
    /* SMS Center Address end*/

    private static final String[] PROPERTY_RIL_TEST_SIM = {
        "gsm.sim.ril.testsim",
        "gsm.sim.ril.testsim.2",
        "gsm.sim.ril.testsim.3",
        "gsm.sim.ril.testsim.4",
    };

    /**
     * Initialize the singleton PhoneInterfaceManagerEx instance.
     * This is only done once, at startup, from PhoneGlobals.onCreate().
     */
    /* package */
    public static PhoneInterfaceManagerEx init(PhoneGlobals app, Phone phone) {
        synchronized (PhoneInterfaceManagerEx.class) {
            if (sInstance == null) {
                sInstance = new PhoneInterfaceManagerEx(app, phone);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            return sInstance;
        }
    }

    /** Private constructor; @see init() */
    private PhoneInterfaceManagerEx(PhoneGlobals app, Phone phone) {
        mApp = app;
        mPhone = phone;
        mMainThreadHandler = new MainThreadHandler();
        publish();
    }

    private void publish() {
        if (DBG) log("publish: " + this);

        ServiceManager.addService("phoneEx", this);
    }

    private static void log(String msg) {
        Log.d(LOG_TAG, "[PhoneIntfMgrEx] " + msg);
    }

    private static void loge(String msg) {
        Log.e(LOG_TAG, "[PhoneIntfMgrEx] " + msg);
    }

    /**
     * A request object for use with {@link MainThreadHandler}. Requesters should wait() on the
     * request after sending. The main thread will notify the request when it is complete.
     */
    private static final class MainThreadRequest {
        /** The argument to use for the request */
        public Object argument;
        /** The result of the request that is run on the main thread */
        public Object result;
        public Object argument2;

        public MainThreadRequest(Object argument) {
            this.argument = argument;
        }

        public MainThreadRequest(Object argument, Object argument2) {
            this.argument = argument;
            this.argument = argument2;
        }
    }

    /**
     * A handler that processes messages on the main thread in the phone process. Since many
     * of the Phone calls are not thread safe this is needed to shuttle the requests from the
     * inbound binder threads to the main thread in the phone process.  The Binder thread
     * may provide a {@link MainThreadRequest} object in the msg.obj field that they are waiting
     * on, which will be notified when the operation completes and will contain the result of the
     * request.
     *
     * <p>If a MainThreadRequest object is provided in the msg.obj field,
     * note that request.result must be set to something non-null for the calling thread to
     * unblock.
     */
    private final class MainThreadHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            MainThreadRequest request;
            Message onCompleted;
            AsyncResult ar;
            long subId;

            switch (msg.what) {
                case CMD_HANDLE_GET_SCA:
                    request = (MainThreadRequest) msg.obj;
                    onCompleted = obtainMessage(CMD_GET_SCA_DONE, request);

                    if (request.argument == null) {
                        // no argument, ignore
                        log("[sca get sc address but no argument");
                    } else {
                        subId = ((Long) request.argument).longValue();
                        getPhone(subId).getSmscAddress(onCompleted);
                    }
                    break;

                case CMD_GET_SCA_DONE:
                    ar = (AsyncResult) msg.obj;
                    request = (MainThreadRequest) ar.userObj;

                    Bundle result = new Bundle();
                    if (ar.exception == null && ar.result != null) {
                        log("[sca get result" + ar.result);
                        result.putByte(GET_SC_ADDRESS_KEY_RESULT,
                                ERROR_CODE_NO_ERROR);
                        result.putCharSequence(GET_SC_ADDRESS_KEY_ADDRESS,
                                (String) ar.result);
                    } else {
                        log("[sca Fail to get sc address");
                        // Currently modem will return generic error without specific error cause,
                        // So we treat all exception as the same error cause.
                        result.putByte(GET_SC_ADDRESS_KEY_RESULT,
                                ERROR_CODE_GENERIC_ERROR);
                        result.putCharSequence(GET_SC_ADDRESS_KEY_ADDRESS, "");
                    }
                    request.result = result;

                    synchronized (request) {
                        log("[sca notify sleep thread");
                        request.notifyAll();
                    }
                    break;

                case CMD_HANDLE_SET_SCA:
                    request = (MainThreadRequest) msg.obj;
                    onCompleted = obtainMessage(CMD_SET_SCA_DONE, request);

                    ScAddress sca = (ScAddress) request.argument;
                    if (sca.mSubId == SubscriptionManager.INVALID_SUB_ID) {
                        // invalid subscription ignore
                        log("[sca invalid subscription");
                    } else {
                        getPhone(sca.mSubId).setSmscAddress(sca.mAddress, onCompleted);
                    }
                    break;

                case CMD_SET_SCA_DONE:
                    ar = (AsyncResult) msg.obj;
                    request = (MainThreadRequest) ar.userObj;
                    if (ar.exception != null) {
                        Log.d(LOG_TAG, "[sca Fail: set sc address");
                        request.result = new Boolean(false);
                    } else {
                        Log.d(LOG_TAG, "[sca Done: set sc address");
                        request.result = new Boolean(true);
                    }

                    synchronized (request) {
                        request.notifyAll();
                    }
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * Posts the specified command to be executed on the main thread,
     * waits for the request to complete, and returns the result.
     * @see sendRequestAsync
     */
    private Object sendRequest(int command, Object argument) {
        if (Looper.myLooper() == mMainThreadHandler.getLooper()) {
            throw new RuntimeException("This method will deadlock if called from the main thread.");
        }

        MainThreadRequest request = new MainThreadRequest(argument);
        Message msg = mMainThreadHandler.obtainMessage(command, request);
        msg.sendToTarget();

        // Wait for the request to complete
        synchronized (request) {
            while (request.result == null) {
                try {
                    request.wait();
                } catch (InterruptedException e) {
                    // Do nothing, go back and wait until the request is complete
                }
            }
        }
        return request.result;
    }


    private static Phone getPhone(long subId) {
        // FIXME: getPhone by subId
        return PhoneFactory.getPhone(SubscriptionManager.getPhoneId(subId));
    }

    private Phone getPhone(int phoneId) {
        // FIXME: getPhone by phoneId
        return PhoneFactory.getPhone(phoneId);
    }

    private long getSubIdBySlot(int slot) {
        long [] subIds = SubscriptionManager.getSubId(slot);
        long subId = ((subIds == null) ? SubscriptionManager.getDefaultSubId() : subIds[0]);
        if (DBG) log("getSubIdBySlot, simId " + slot + "subId " + subId);
        return subId;
    }

    private class UnlockSim extends Thread {

        /* Query network lock start */

        // Verify network lock result.
        public static final int VERIFY_RESULT_PASS = 0;
        public static final int VERIFY_INCORRECT_PASSWORD = 1;
        public static final int VERIFY_RESULT_EXCEPTION = 2;

        // Total network lock count.
        public static final int NETWORK_LOCK_TOTAL_COUNT = 5;
        public static final String QUERY_SIMME_LOCK_RESULT = "com.mediatek.phone.QUERY_SIMME_LOCK_RESULT";
        public static final String SIMME_LOCK_LEFT_COUNT = "com.mediatek.phone.SIMME_LOCK_LEFT_COUNT";

        /* Query network lock end */


        private final IccCard mSimCard;

        private boolean mDone = false;
        private boolean mResult = false;

        // For replies from SimCard interface
        private Handler mHandler;

        private static final int QUERY_NETWORK_STATUS_COMPLETE = 100;
        private static final int SET_NETWORK_LOCK_COMPLETE = 101;

        private int mVerifyResult = -1;
        private int mSIMMELockRetryCount = -1;

        public UnlockSim(IccCard simCard) {
            mSimCard = simCard;
        }

        @Override
        public void run() {
            Looper.prepare();
            synchronized (UnlockSim.this) {
                mHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        AsyncResult ar = (AsyncResult) msg.obj;
                        switch (msg.what) {
                            case QUERY_NETWORK_STATUS_COMPLETE:
                                synchronized (UnlockSim.this) {
                                    int [] LockState = (int []) ar.result;
                                    if (ar.exception != null) { //Query exception occurs
                                        log("Query network lock fail");
                                        mResult = false;
                                        mDone = true;
                                    } else {
                                        mSIMMELockRetryCount = LockState[2];
                                        log("[SIMQUERY] Category = " + LockState[0]
                                            + " ,Network status =" + LockState[1]
                                            + " ,Retry count = " + LockState[2]);

                                        mDone = true;
                                        mResult = true;
                                        UnlockSim.this.notifyAll();
                                    }
                                }
                                break;
                            case SET_NETWORK_LOCK_COMPLETE:
                                log("SUPPLY_NETWORK_LOCK_COMPLETE");
                                synchronized (UnlockSim.this) {
                                    if ((ar.exception != null) &&
                                           (ar.exception instanceof CommandException)) {
                                        log("ar.exception " + ar.exception);
                                        if (((CommandException) ar.exception).getCommandError()
                                            == CommandException.Error.PASSWORD_INCORRECT) {
                                            mVerifyResult = VERIFY_INCORRECT_PASSWORD;
                                       } else {
                                            mVerifyResult = VERIFY_RESULT_EXCEPTION;
                                       }
                                    } else {
                                        mVerifyResult = VERIFY_RESULT_PASS;
                                    }
                                    mDone = true;
                                    UnlockSim.this.notifyAll();
                                }
                                break;
                        }
                    }
                };
                UnlockSim.this.notifyAll();
            }
            Looper.loop();
        }

        synchronized Bundle queryNetworkLock(int category) {

            while (mHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            log("Enter queryNetworkLock");
            Message callback = Message.obtain(mHandler, QUERY_NETWORK_STATUS_COMPLETE);
            mSimCard.queryIccNetworkLock(category, callback);

            while (!mDone) {
                try {
                    log("wait for done");
                    wait();
                } catch (InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }

            Bundle bundle = new Bundle();
            bundle.putBoolean(QUERY_SIMME_LOCK_RESULT, mResult);
            bundle.putInt(SIMME_LOCK_LEFT_COUNT, mSIMMELockRetryCount);

            log("done");
            return bundle;
        }

        synchronized int supplyNetworkLock(String strPasswd) {

            while (mHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            log("Enter supplyNetworkLock");
            Message callback = Message.obtain(mHandler, SET_NETWORK_LOCK_COMPLETE);
            mSimCard.supplyNetworkDepersonalization(strPasswd, callback);

            while (!mDone) {
                try {
                    log("wait for done");
                    wait();
                } catch (InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }

            log("done");
            return mVerifyResult;
        }
    }

    public Bundle queryNetworkLock(long subId, int category) {
        final UnlockSim queryNetworkLockState;

        log("queryNetworkLock");

        queryNetworkLockState = new UnlockSim(getPhone(subId).getIccCard());
        queryNetworkLockState.start();

        return queryNetworkLockState.queryNetworkLock(category);
    }

    public int supplyNetworkDepersonalization(long subId, String strPasswd) {
        final UnlockSim supplyNetworkLock;

        log("supplyNetworkDepersonalization");

        supplyNetworkLock = new UnlockSim(getPhone(subId).getIccCard());
        supplyNetworkLock.start();

        return supplyNetworkLock.supplyNetworkLock(strPasswd);
    }

    /**
     * Modem SML change feature.
     * This function will query the SIM state of the given slot. And broadcast
     * ACTION_UNLOCK_SIM_LOCK if the SIM state is in network lock.
     *
     * @param subId: Indicate which sub to query
     * @param needIntent: The caller can deside to broadcast ACTION_UNLOCK_SIM_LOCK or not
     *                    in this time, because some APs will receive this intent (eg. Keyguard).
     *                    That can avoid this intent to effect other AP.
     */
    public void repollIccStateForNetworkLock(long subId, boolean needIntent) {
        if (TelephonyManager.getDefault().getPhoneCount() > 1) {
            getPhone(subId).getIccCard().repollIccStateForModemSmlChangeFeatrue(needIntent);
        } else {
            log("Not Support in Single SIM.");
        }
    }

    private static class SetMsisdn extends Thread {
        private long mSubId;
        private Phone myPhone;
        private boolean mDone = false;
        private int mResult = 0;
        private Handler mHandler;

        private static final String DEFAULT_ALPHATAG = "Default Tag";
        private static final int CMD_SET_MSISDN_COMPLETE = 100;


        public SetMsisdn(Phone myP, long subId) {
            mSubId = subId;
            myPhone = myP;
        }


        @Override
        public void run() {
            Looper.prepare();
            synchronized (SetMsisdn.this) {
                mHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        AsyncResult ar = (AsyncResult) msg.obj;
                        switch (msg.what) {
                            case CMD_SET_MSISDN_COMPLETE:
                                synchronized (SetMsisdn.this) {
                                    if (ar.exception != null) { //Query exception occurs
                                        Log.e(LOG_TAG, "Set msisdn fail");
                                        mDone = true;
                                        mResult = 0;
                                    } else {
                                        Log.d(LOG_TAG, "Set msisdn success");
                                        mDone = true;
                                        mResult = 1;
                                    }
                                    SetMsisdn.this.notifyAll();
                                }
                                break;
                        }
                    }
                };
                SetMsisdn.this.notifyAll();
            }
            Looper.loop();
        }

        synchronized int setLine1Number(String alphaTag, String number) {

            while (mHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            Log.d(LOG_TAG, "Enter setLine1Number");
            Message callback = Message.obtain(mHandler, CMD_SET_MSISDN_COMPLETE);
            String myTag = alphaTag;

            myTag = myPhone.getLine1AlphaTag();

            if (myTag == null || myTag.equals("")) {
                myTag = DEFAULT_ALPHATAG;
            }

            Log.d(LOG_TAG, "sub = " + mSubId + ", Tag = " + myTag + " ,number = " + number);

            myPhone.setLine1Number(myTag, number, callback);


            while (!mDone) {
                try {
                    Log.d(LOG_TAG, "wait for done");
                    wait();
                } catch (InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }

            Log.d(LOG_TAG, "done");
            return mResult;
        }
    }

    //@Override
    public int setLine1Number(long subId, String alphaTag, String number) {
        if (DBG) log("setLine1NumberUsingSubId, subId " + subId);
        if (number == null) {
            loge("number = null");
            return 0;
        }
        if (subId <= 0) {
            loge("Error subId: " + subId);
            return 0;
        }

        final SetMsisdn setMsisdn;

        setMsisdn = new SetMsisdn(getPhone(subId), subId);
        setMsisdn.start();

        return setMsisdn.setLine1Number(alphaTag, number);
    }

    /**
    * Return true if the FDN of the ICC card is enabled
    */
    //@Override
    public boolean isFdnEnabled(long subId) {
        log("isFdnEnabled  subId=" + subId);

        if (subId <= 0) {
            loge("Error subId: " + subId);
            return false;
        }

        return getPhone(subId).getIccCard().getIccFdnEnabled();
    }

    //@Override
    public String getIccCardType(long subId) {
        log("getIccCardType  subId=" + subId);

        if (subId <= 0) {
            loge("Error subId: " + subId);
            return null;
        }

        return getPhone(subId).getIccCard().getIccCardType();
    }

    //@Override
    public boolean isTestIccCard(int slotId) {
        String mTestCard = null;

        mTestCard = SystemProperties.get(PROPERTY_RIL_TEST_SIM[slotId], "");
        if (DBG) log("getIccCardType(): slot id =" + slotId + ", iccType = " + mTestCard);
        return (mTestCard != null && mTestCard.equals("1"));
    }

    /**
     * Gets the ISO country code equivalent for the SIM provider's country code.
     * <p>
     * @param simId  Indicates which SIM (slot) to query
     * <p>
     * @return       Gets the ISO country code equivalent for the SIM provider's country code.
     */
    //@Override
    public String getSimCountryIso(long subId) {
        if (subId <= 0) {
            loge("getSimCountryIsoUsingSubId, Error subId: " + subId);
            return "";
        }

        String prop = TelephonyManager.getTelephonyProperty(TelephonyProperties.PROPERTY_ICC_OPERATOR_ISO_COUNTRY, subId, "");
        Log.d(LOG_TAG, "getSimCountryIso subId = " + subId + " ,prop = " + prop);
        return prop;
    }

    /**
     * Gets the unique subscriber ID, for example, the IMSI for a GSM phone.
     * <p>
     * Required Permission:
     *   {@link android.Manifest.permission#READ_PHONE_STATE READ_PHONE_STATE}
     * <p>
     * @param simId  Indicates which SIM (slot) to query
     * <p>
     * @return       Unique subscriber ID, for example, the IMSI for a GSM phone. Null is returned if it is unavailable.
     */
    //@Override
    public String getSubscriberId(long subId) {
        if (subId <= 0) {
            loge("getSubscriberIdUsingSubId, Error subId: " + subId);
            return null;
        }

        String imsi = getPhone(subId).getSubscriberId();
        if (DBG) log("getSubscriberId, subId " + subId + " ,imsi " + imsi);

        return imsi;
    }

    /**
     * Gets a constant indicating the state of the device SIM card.
     * <p>
     * @param simId  Indicates which SIM (slot) to query
     * <p>
     * @return       Constant indicating the state of the device SIM card
     */
    public int getSimState(int simId) {
        long [] sub = SubscriptionManager.getSubId(simId);
        if (sub != null) {
            long subId = sub[0];
            String prop = TelephonyManager.getTelephonyProperty(
                    TelephonyProperties.PROPERTY_SIM_STATE, subId, "");
            if (DBG) {
                log("getSimState simId = " + simId + " ,sub = " + subId +
                        " ,prop = " + prop);
            }

            if ("ABSENT".equals(prop)) {
                return android.telephony.TelephonyManager.SIM_STATE_ABSENT;
            } else if ("PIN_REQUIRED".equals(prop)) {
                return android.telephony.TelephonyManager.SIM_STATE_PIN_REQUIRED;
            } else if ("PUK_REQUIRED".equals(prop)) {
                return android.telephony.TelephonyManager.SIM_STATE_PUK_REQUIRED;
            } else if ("NETWORK_LOCKED".equals(prop)) {
                return android.telephony.TelephonyManager.SIM_STATE_NETWORK_LOCKED;
            } else if ("READY".equals(prop)) {
                return android.telephony.TelephonyManager.SIM_STATE_READY;
            } else {
                return android.telephony.TelephonyManager.SIM_STATE_UNKNOWN;
            }
        } else {
            if (DBG) {
                log("getSimState simId = " + simId + " ,sub is null");
            }
            return android.telephony.TelephonyManager.SIM_STATE_UNKNOWN;
        }
    }

    /**
     * Gets the MCC+MNC (mobile country code + mobile network code) of the provider of the SIM. 5 or 6 decimal digits.
     *
     * Availability: SIM state must be SIM_STATE_READY.
     * <p>
     * @param simId  Indicates which SIM (slot) to query
     * <p>
     * @return       MCC+MNC (mobile country code + mobile network code) of the provider of the SIM. 5 or 6 decimal digits.
     */
    public String getSimOperator(long subId) {
        String prop = TelephonyManager.getTelephonyProperty(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC, subId, "");
        if (DBG) log("getSimOperatorUsingSub sub = " + subId + " ,prop = " + prop);
        return prop;
    }

    /**
     * Gets the Service Provider Name (SPN).
     *
     * Availability: SIM state must be SIM_STATE_READY.
     * <p>
     * @param simId  Indicates which SIM (slot) to query
     * <p>
     * @return       Service Provider Name (SPN).
     */
    public String getSimOperatorName(long subId) {
        String prop = TelephonyManager.getTelephonyProperty(TelephonyProperties.PROPERTY_ICC_OPERATOR_ALPHA, subId, "");
        if (DBG) log("getSimOperatorNameUsingSub sub = " + subId + " ,prop = " + prop);
        return prop;
    }

    /**
     * Gets the serial number of the SIM, if applicable
     * <p>
     * Required Permission:
     *   {@link android.Manifest.permission#READ_PHONE_STATE READ_PHONE_STATE}
     * <p>
     * @param simId  Indicates which SIM (slot) to query
     * <p>
     * @return       Serial number of the SIM, if applicable. Null is returned if it is unavailable.
     */
    public String getSimSerialNumber(long subId) {
        String iccid = getPhone(subId).getIccSerialNumber();
        if (DBG) log("getSimSerialNumberUsingSub sub = " + subId + " ,iccid = " + iccid);
        return iccid;
    }

    /**
     * Gemini
     * Returns the alphabetic identifier associated with the line 1 number.
     * Return null if it is unavailable.
     * <p>
     * Requires Permission:
     *   {@link android.Manifest.permission#READ_PHONE_STATE READ_PHONE_STATE}
     * @hide
     */
    public String getLine1AlphaTag(long subId) {
        String alpha = getPhone(subId).getLine1AlphaTag();
        if (DBG) log("getLine1AlphaTagUsingSub sub = " + subId + " ,alpha = " + alpha);
        return alpha;
    }

    /**
     * Gets the voice mail number.
     * <p>
     * Required Permission:
     *   {@link android.Manifest.permission#READ_PHONE_STATE READ_PHONE_STATE}
     * <p>
     * @param simId  Indicates which SIM (slot) to query
     * <p>
     * @return       Voice mail number. Null is returned if it is unavailable.
     */
    public String getVoiceMailNumber(long subId) {
        String vMailNumber = getPhone(subId).getVoiceMailNumber();
        if (DBG) log("getVoiceMailNumberUsingSub sub = " + subId + " ,vMailNumber = " + vMailNumber);
        return vMailNumber;
    }

    /**
     * Retrieves the alphabetic identifier associated with the voice mail number.
     * <p>
     * Required Permission:
     *   {@link android.Manifest.permission#READ_PHONE_STATE READ_PHONE_STATE}
     * <p>
     * @param simId  Indicates which SIM (slot) to query
     * <p>
     * @return       the Alphabetic identifier associated with the voice mail number
     */
    public String getVoiceMailAlphaTag(long subId) {
        String vMailAlphaTag = getPhone(subId).getVoiceMailAlphaTag();
        if (DBG) log("getVoiceMailAlphaTagUsingSub sub = " + subId + " ,vMailAlphaTag = " + vMailAlphaTag);
        return vMailAlphaTag;
    }

    @Deprecated
    public int getVoiceMessageCountGemini(int simId) {
        int count = 0;
        long [] sub = SubscriptionManager.getSubId(simId);
        if (sub != null) {
            long subId = sub[0];
            if (DBG) {
                log("Deprecated! getVoiceMessageCountGemini simId = " + simId
                        + " ,sub = " + subId);
            }
            ITelephony iTelephony = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));

            try {
                if (iTelephony != null) {
                    count = iTelephony.getVoiceMessageCountForSubscriber(subId);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            if (DBG) {
                log("Deprecated! getVoiceMessageCountGemini sub is null");
            }
        }
        if (DBG) log("Deprecated! getVoiceMessageCountGemini count " + count);
        return count;
    }

    /**
     * Gemini
     * Returns the alphabetic name of current registered operator.
     * <p>
     * Availability: Only when user is registered to a network. Result may be
     * unreliable on CDMA networks (use {@link #getPhoneType()} to determine if
     * on a CDMA network).
     */
    @Deprecated
    public String getNetworkOperatorNameGemini(int slotId) {
        long subId = getSubIdBySlot(slotId);
        if (DBG) log("Deprecated! getNetworkOperatorNameGemini simId = " + slotId + " ,sub = " + subId);
        return getNetworkOperatorNameUsingSub(subId);
    }

    public String getNetworkOperatorNameUsingSub(long subId) {
        String prop = TelephonyManager.getTelephonyProperty(TelephonyProperties.PROPERTY_OPERATOR_ALPHA, subId, "");
        if (DBG) log("getNetworkOperatorNameUsingSub sub = " + subId + " ,prop = " + prop);
        return prop;
    }

    /**
     * Gemini
     * Returns the numeric name (MCC+MNC) of current registered operator.
     * <p>
     * Availability: Only when user is registered to a network. Result may be
     * unreliable on CDMA networks (use {@link #getPhoneType()} to determine if
     * on a CDMA network).
     */
    @Deprecated
    public String getNetworkOperatorGemini(int slotId) {
        long subId = getSubIdBySlot(slotId);
        if (DBG) log("Deprecated! getNetworkOperatorGemini simId = " + slotId + " ,sub = " + subId);
        return getNetworkOperatorUsingSub(subId);
    }

    public String getNetworkOperatorUsingSub(long subId) {
        String prop = TelephonyManager.getTelephonyProperty(TelephonyProperties.PROPERTY_OPERATOR_NUMERIC, subId, "");
        if (DBG) log("getNetworkOperatorUsingSub sub = " + subId + " ,prop = " + prop);
        return prop;
    }

    /* BT SIM operation begin */
    public int btSimapConnectSIM(int simId,  BtSimapOperResponse btRsp) {
       long[] subs = SubscriptionManager.getSubIdUsingSlotId(simId);
       if (subs != null) {
           Log.d(LOG_TAG, "btSimapConnectSIM, simId " + simId + ", subId " + subs[0]);
           Phone btPhone = getPhone(subs[0]);
           final SendBtSimapProfile sendBtSapTh = SendBtSimapProfile.getInstance(btPhone);
           sendBtSapTh.setBtOperResponse(btRsp);
           if (sendBtSapTh.getState() == Thread.State.NEW) {
             sendBtSapTh.start();
           }
           int ret = sendBtSapTh.btSimapConnectSIM(simId);
           Log.d(LOG_TAG, "btSimapConnectSIM ret is " + ret + " btRsp.curType " + btRsp.getCurType()
            + " suptype " + btRsp.getSupportType() + " atr " + btRsp.getAtrString());
           return ret;
       } else {
           Log.e(LOG_TAG, "btSimapConnectSIM subs is null");
           return -1;
       }
   }

   public int btSimapDisconnectSIM() {
       int simId = UiccController.getInstance().getBtConnectedSimId();
       long[] subs = SubscriptionManager.getSubIdUsingSlotId(simId);
       if (subs != null) {
           Log.d(LOG_TAG, "btSimapDisconnectSIM, simId " + simId + ", subId " + subs[0]);
           Phone btPhone = getPhone(subs[0]);
           final SendBtSimapProfile sendBtSapTh = SendBtSimapProfile.getInstance(btPhone);
           if (sendBtSapTh.getState() == Thread.State.NEW) {
               sendBtSapTh.start();
           }
           return sendBtSapTh.btSimapDisconnectSIM();
       } else {
           Log.e(LOG_TAG, "btSimapDisconnectSIM subs is null");
           return -1;
       }
   }

   public int btSimapApduRequest(int type, String cmdAPDU,  BtSimapOperResponse btRsp) {
       int simId = UiccController.getInstance().getBtConnectedSimId();
       long[] subs = SubscriptionManager.getSubIdUsingSlotId(simId);
       if (subs != null) {
           Log.d(LOG_TAG, "btSimapApduRequest, simId " + simId + ", subId " + subs[0]);
           Phone btPhone = getPhone(subs[0]);
           final SendBtSimapProfile sendBtSapTh = SendBtSimapProfile.getInstance(btPhone);
           sendBtSapTh.setBtOperResponse(btRsp);
           if (sendBtSapTh.getState() == Thread.State.NEW) {
               sendBtSapTh.start();
           }
           return sendBtSapTh.btSimapApduRequest(type, cmdAPDU);
       } else {
           Log.e(LOG_TAG, "btSimapApduRequest subs is null");
           return -1;
       }
   }

   public int btSimapResetSIM(int type,  BtSimapOperResponse btRsp) {
       int simId = UiccController.getInstance().getBtConnectedSimId();
       long[] subs = SubscriptionManager.getSubIdUsingSlotId(simId);
       if (subs != null) {
           Log.d(LOG_TAG, "btSimapResetSIM, simId " + simId + ", subId " + subs[0]);
           Phone btPhone = getPhone(subs[0]);
           final SendBtSimapProfile sendBtSapTh = SendBtSimapProfile.getInstance(btPhone);
           sendBtSapTh.setBtOperResponse(btRsp);
           if (sendBtSapTh.getState() == Thread.State.NEW) {
               sendBtSapTh.start();
           }
           return sendBtSapTh.btSimapResetSIM(type);
       } else {
           Log.e(LOG_TAG, "btSimapResetSIM subs is null");
           return -1;
       }
   }

   public int btSimapPowerOnSIM(int type,  BtSimapOperResponse btRsp) {
       int simId = UiccController.getInstance().getBtConnectedSimId();
       long[] subs = SubscriptionManager.getSubIdUsingSlotId(simId);
       if (subs != null) {
           Log.d(LOG_TAG, "btSimapPowerOnSIM, simId " + simId + ", subId " + subs[0]);
           Phone btPhone = getPhone(subs[0]);
           final SendBtSimapProfile sendBtSapTh = SendBtSimapProfile.getInstance(btPhone);
           sendBtSapTh.setBtOperResponse(btRsp);
           if (sendBtSapTh.getState() == Thread.State.NEW) {
               sendBtSapTh.start();
           }
           return sendBtSapTh.btSimapPowerOnSIM(type);
       } else {
           Log.e(LOG_TAG, "btSimapPowerOnSIM subs is null");
           return -1;
       }
   }

   public int btSimapPowerOffSIM() {
       int simId = UiccController.getInstance().getBtConnectedSimId();
       long[] subs = SubscriptionManager.getSubIdUsingSlotId(simId);
       if (subs != null) {
           Log.d(LOG_TAG, "btSimapPowerOffSIM, simId " + simId + ", subId " + subs[0]);
           Phone btPhone = getPhone(subs[0]);
           final SendBtSimapProfile sendBtSapTh = SendBtSimapProfile.getInstance(btPhone);
           if (sendBtSapTh.getState() == Thread.State.NEW) {
               sendBtSapTh.start();
           }
           return sendBtSapTh.btSimapPowerOffSIM();
       } else {
           Log.e(LOG_TAG, "btSimapPowerOffSIM subs is null");
           return -1;
       }
   }

    private static class SendBtSimapProfile extends Thread {
        private Phone mBtSapPhone;
        private boolean mDone = false;
        private String mStrResult = null;
        private ArrayList mResult;
        private int mRet = 1;
        private BtSimapOperResponse mBtRsp;
        private Handler mHandler;

        private static SendBtSimapProfile sInstance;
        static final Object sInstSync = new Object();
        // For async handler to identify request type
        private static final int BTSAP_CONNECT_COMPLETE = 300;
        private static final int BTSAP_DISCONNECT_COMPLETE = 301;
        private static final int BTSAP_POWERON_COMPLETE = 302;
        private static final int BTSAP_POWEROFF_COMPLETE = 303;
        private static final int BTSAP_RESETSIM_COMPLETE = 304;
        private static final int BTSAP_TRANSFER_APDU_COMPLETE = 305;

        public static SendBtSimapProfile getInstance(Phone phone) {
            synchronized (sInstSync) {
                if (sInstance == null) {
                    sInstance = new SendBtSimapProfile(phone);
                }
            }
            return sInstance;
        }
        private SendBtSimapProfile(Phone phone) {
            mBtSapPhone = phone;
            mBtRsp = null;
        }


        public void setBtOperResponse(BtSimapOperResponse btRsp) {
            mBtRsp = btRsp;
        }

        private Phone getPhone(long subId) {
            // FIXME: getPhone by subId
            return null;
        }

        @Override
        public void run() {
            Looper.prepare();
            synchronized (SendBtSimapProfile.this) {
                mHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        AsyncResult ar = (AsyncResult) msg.obj;
                        switch (msg.what) {
                            case BTSAP_CONNECT_COMPLETE:
                                Log.d(LOG_TAG, "BTSAP_CONNECT_COMPLETE");
                                synchronized (SendBtSimapProfile.this) {
                                    if (ar.exception != null) {
                                        CommandException ce = (CommandException) ar.exception;
                                        if (ce.getCommandError() == CommandException.Error.BT_SAP_CARD_REMOVED) {
                                            mRet = 4;
                                        } else if (ce.getCommandError() == CommandException.Error.BT_SAP_NOT_ACCESSIBLE) {
                                            mRet = 2;
                                        } else {
                                            mRet = 1;
                                        }
                                        Log.e(LOG_TAG, "Exception BTSAP_CONNECT, Exception:" + ar.exception);
                                    } else {
                                        mStrResult = (String) (ar.result);
                                        Log.d(LOG_TAG, "BTSAP_CONNECT_COMPLETE  mStrResult " + mStrResult);
                                        String[] splited = mStrResult.split(",");

                                        try {
                                            mBtRsp.setCurType(Integer.parseInt(splited[0].trim()));
                                            mBtRsp.setSupportType(Integer.parseInt(splited[1].trim()));
                                            mBtRsp.setAtrString(splited[2]);
                                            Log.d(LOG_TAG, "BTSAP_CONNECT_COMPLETE curType " + mBtRsp.getCurType() + " SupType " + mBtRsp.getSupportType() + " ATR " + mBtRsp.getAtrString());
                                        } catch (NumberFormatException e) {
                                            Log.e(LOG_TAG, "NumberFormatException");
                                        }

                                        mRet = 0;
                                        //log("BTSAP_CONNECT_COMPLETE curType " + (String)(mResult.get(0)) + " SupType " + (String)(mResult.get(1)) + " ATR " + (String)(mResult.get(2)));
                                    }

                                    //log("BTSAP_CONNECT_COMPLETE curType " + mBtRsp.getCurType() + " SupType " + mBtRsp.getSupportType() + " ATR " + mBtRsp.getAtrString());
                                    mDone = true;
                                    SendBtSimapProfile.this.notifyAll();
                                }
                                break;
                            case BTSAP_DISCONNECT_COMPLETE:
                                Log.d(LOG_TAG, "BTSAP_DISCONNECT_COMPLETE");
                                synchronized (SendBtSimapProfile.this) {
                                    if (ar.exception != null) {
                                        CommandException ce = (CommandException) ar.exception;
                                        if (ce.getCommandError() == CommandException.Error.BT_SAP_CARD_REMOVED) {
                                            mRet = 4;
                                        } else if (ce.getCommandError() == CommandException.Error.BT_SAP_NOT_ACCESSIBLE) {
                                            mRet = 2;
                                        } else {
                                            mRet = 1;
                                        }
                                        Log.e(LOG_TAG, "Exception BTSAP_DISCONNECT, Exception:" + ar.exception);
                                    } else {
                                        mRet = 0;
                                    }
                                    Log.d(LOG_TAG, "BTSAP_DISCONNECT_COMPLETE result is " + mRet);
                                    mDone = true;
                                    SendBtSimapProfile.this.notifyAll();
                                }
                                break;
                            case BTSAP_POWERON_COMPLETE:
                                Log.d(LOG_TAG, "BTSAP_POWERON_COMPLETE");
                                synchronized (SendBtSimapProfile.this) {
                                    if (ar.exception != null) {
                                        CommandException ce = (CommandException) ar.exception;
                                        if (ce.getCommandError() == CommandException.Error.BT_SAP_CARD_REMOVED) {
                                            mRet = 4;
                                        } else if (ce.getCommandError() == CommandException.Error.BT_SAP_NOT_ACCESSIBLE) {
                                            mRet = 2;
                                        } else {
                                            mRet = 1;
                                        }
                                        loge("Exception POWERON_COMPLETE, Exception:" + ar.exception);
                                    } else {
                                        mStrResult = (String) (ar.result);
                                        Log.d(LOG_TAG, "BTSAP_POWERON_COMPLETE  mStrResult " + mStrResult);
                                        String[] splited = mStrResult.split(",");

                                        try {
                                            mBtRsp.setCurType(Integer.parseInt(splited[0].trim()));
                                            mBtRsp.setAtrString(splited[1]);
                                            Log.d(LOG_TAG, "BTSAP_POWERON_COMPLETE curType " + mBtRsp.getCurType() + " ATR " + mBtRsp.getAtrString());
                                        } catch (NumberFormatException e) {
                                            Log.e(LOG_TAG, "NumberFormatException");
                                        }
                                        mRet = 0;
                                    }

                                    mDone = true;
                                    SendBtSimapProfile.this.notifyAll();
                                }
                                break;
                            case BTSAP_POWEROFF_COMPLETE:
                                Log.d(LOG_TAG, "BTSAP_POWEROFF_COMPLETE");
                                synchronized (SendBtSimapProfile.this) {
                                    if (ar.exception != null) {
                                        CommandException ce = (CommandException) ar.exception;
                                        if (ce.getCommandError() == CommandException.Error.BT_SAP_CARD_REMOVED) {
                                            mRet = 4;
                                        } else if (ce.getCommandError() == CommandException.Error.BT_SAP_NOT_ACCESSIBLE) {
                                            mRet = 2;
                                        } else {
                                            mRet = 1;
                                        }
                                        Log.e(LOG_TAG, "Exception BTSAP_POWEROFF, Exception:" + ar.exception);
                                    } else {
                                        mRet = 0;
                                    }
                                    Log.d(LOG_TAG, "BTSAP_POWEROFF_COMPLETE result is " + mRet);
                                    mDone = true;
                                    SendBtSimapProfile.this.notifyAll();
                                }
                                break;
                            case BTSAP_RESETSIM_COMPLETE:
                                Log.d(LOG_TAG, "BTSAP_RESETSIM_COMPLETE");
                                synchronized (SendBtSimapProfile.this) {
                                    if (ar.exception != null) {
                                        CommandException ce = (CommandException) ar.exception;
                                        if (ce.getCommandError() == CommandException.Error.BT_SAP_CARD_REMOVED) {
                                            mRet = 4;
                                        } else if (ce.getCommandError() == CommandException.Error.BT_SAP_NOT_ACCESSIBLE) {
                                            mRet = 2;
                                        } else {
                                            mRet = 1;
                                        }
                                        loge("Exception BTSAP_RESETSIM, Exception:" + ar.exception);
                                    } else {
                                        mStrResult = (String) (ar.result);
                                        Log.d(LOG_TAG, "BTSAP_RESETSIM_COMPLETE  mStrResult " + mStrResult);
                                        String[] splited = mStrResult.split(",");

                                        try {
                                            mBtRsp.setCurType(Integer.parseInt(splited[0].trim()));
                                            mBtRsp.setAtrString(splited[1]);
                                            Log.d(LOG_TAG, "BTSAP_RESETSIM_COMPLETE curType " + mBtRsp.getCurType() + " ATR " + mBtRsp.getAtrString());
                                        } catch (NumberFormatException e) {
                                            Log.e(LOG_TAG, "NumberFormatException");
                                        }
                                        mRet = 0;
                                    }

                                    mDone = true;
                                    SendBtSimapProfile.this.notifyAll();
                                }
                                break;
                            case BTSAP_TRANSFER_APDU_COMPLETE:
                                Log.d(LOG_TAG, "BTSAP_TRANSFER_APDU_COMPLETE");
                                synchronized (SendBtSimapProfile.this) {
                                    if (ar.exception != null) {
                                        CommandException ce = (CommandException) ar.exception;
                                        if (ce.getCommandError() == CommandException.Error.BT_SAP_CARD_REMOVED) {
                                            mRet = 4;
                                        } else if (ce.getCommandError() == CommandException.Error.BT_SAP_NOT_ACCESSIBLE) {
                                            mRet = 2;
                                        } else {
                                            mRet = 1;
                                        }

                                        Log.e(LOG_TAG, "Exception BTSAP_TRANSFER_APDU, Exception:" + ar.exception);
                                    } else {
                                        mBtRsp.setApduString((String) (ar.result));
                                        Log.d(LOG_TAG, "BTSAP_TRANSFER_APDU_COMPLETE result is " + mBtRsp.getApduString());
                                        mRet = 0;
                                    }

                                    mDone = true;
                                    SendBtSimapProfile.this.notifyAll();
                                }
                                break;
                        }
                    }
                };
                SendBtSimapProfile.this.notifyAll();
            }
            Looper.loop();
        }

        synchronized int btSimapConnectSIM(int simId) {
            int ret = 0;
            while (mHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            mDone = false;
            Message callback = Message.obtain(mHandler, BTSAP_CONNECT_COMPLETE);
            mBtSapPhone.sendBtSimProfile(0, 0, null, callback);

            while (!mDone) {
                try {
                    Log.d(LOG_TAG, "wait for done");
                    wait();
                } catch (InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }

            Log.d(LOG_TAG, "done");
            if (mRet == 0) {
                // parse result
                UiccController.getInstance().setBtConnectedSimId(simId);
                Log.d(LOG_TAG, "synchronized btSimapConnectSIM connect Sim is "
                            + UiccController.getInstance().getBtConnectedSimId());
                Log.d(LOG_TAG, "btSimapConnectSIM curType " + mBtRsp.getCurType() + " SupType "
                        + mBtRsp.getSupportType() + " ATR " + mBtRsp.getAtrString());
            } else {
                ret = mRet;
            }

            Log.d(LOG_TAG, "synchronized btSimapConnectSIM ret " + ret);
            return ret;
        }

        synchronized int btSimapDisconnectSIM() {
            int ret = 0;
            while (mHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            Log.d(LOG_TAG, "synchronized btSimapDisconnectSIM");
            mDone = false;
            Message callback = Message.obtain(mHandler, BTSAP_DISCONNECT_COMPLETE);
            final int slotId = UiccController.getInstance().getBtConnectedSimId();
            // TODO: Wait for GeminiUtils ready
            /*
            if (!GeminiUtils.isValidSlot(slotId)) {
                ret = 7; // No sim has been connected
                return ret;
            }
            */
            mBtSapPhone.sendBtSimProfile(1, 0, null, callback);

            while (!mDone) {
                try {
                    Log.d(LOG_TAG, "wait for done");
                    wait();
                } catch (InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }
            Log.d(LOG_TAG, "done");
            if (mRet == 0) {
                UiccController.getInstance().setBtConnectedSimId(-1);
            }
            ret = mRet;
            Log.d(LOG_TAG, "synchronized btSimapDisconnectSIM ret " + ret);
            return ret;
        }

        synchronized int btSimapResetSIM(int type) {
            int ret = 0;
            while (mHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            mDone = false;
            Message callback = Message.obtain(mHandler, BTSAP_RESETSIM_COMPLETE);

            final int slotId = UiccController.getInstance().getBtConnectedSimId();
            // TODO: Wait for GeminiUtils ready
            /*
            if (!GeminiUtils.isValidSlot(slotId)) {
                ret = 7; // No sim has been connected
                return ret;
            }
            */
            mBtSapPhone.sendBtSimProfile(4, type, null, callback);

            while (!mDone) {
                try {
                    Log.d(LOG_TAG, "wait for done");
                    wait();
                } catch (InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }
            Log.d(LOG_TAG, "done");
            if (mRet == 0)  {
                Log.d(LOG_TAG, "btSimapResetSIM curType " + mBtRsp.getCurType() + " ATR " + mBtRsp.getAtrString());
            } else {
                ret = mRet;
            }

            Log.d(LOG_TAG, "synchronized btSimapResetSIM ret " + ret);
            return ret;
        }

        synchronized int btSimapPowerOnSIM(int type)  {
            int ret = 0;
            while (mHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            mDone = false;
            Message callback = Message.obtain(mHandler, BTSAP_POWERON_COMPLETE);

            final int slotId = UiccController.getInstance().getBtConnectedSimId();
            // TODO: Wait for GeminiUtils ready
            /*
            if (!GeminiUtils.isValidSlot(slotId)) {
                ret = 7; // No sim has been connected
                return ret;
            }
            */
            mBtSapPhone.sendBtSimProfile(2, type, null, callback);

            while (!mDone) {
                try {
                    Log.d(LOG_TAG, "wait for done");
                    wait();
                } catch (InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }
            Log.d(LOG_TAG, "done");
            if (mRet == 0)  {
                Log.d(LOG_TAG, "btSimapPowerOnSIM curType " + mBtRsp.getCurType() + " ATR " + mBtRsp.getAtrString());
            } else {
            ret = mRet;
            }
            Log.d(LOG_TAG, "synchronized btSimapPowerOnSIM ret " + ret);
            return ret;
        }

        synchronized int btSimapPowerOffSIM() {
            int ret = 0;
            while (mHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            mDone = false;
            Message callback = Message.obtain(mHandler, BTSAP_POWEROFF_COMPLETE);

            final int slotId = UiccController.getInstance().getBtConnectedSimId();
            // TODO: Wait for GeminiUtils ready
            /*
            if (!GeminiUtils.isValidSlot(slotId)) {
                ret = 7; // No sim has been connected
                return ret;
            }
            */
            mBtSapPhone.sendBtSimProfile(3, 0, null, callback);

            while (!mDone) {
                try {
                    Log.d(LOG_TAG, "wait for done");
                    wait();
                } catch (InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }
            Log.d(LOG_TAG, "done");
            ret = mRet;
            Log.d(LOG_TAG, "synchronized btSimapPowerOffSIM ret " + ret);
            return ret;
        }

        synchronized int btSimapApduRequest(int type, String cmdAPDU) {
            int ret = 0;
            while (mHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            mDone = false;
            Message callback = Message.obtain(mHandler, BTSAP_TRANSFER_APDU_COMPLETE);

            final int slotId = UiccController.getInstance().getBtConnectedSimId();
            // TODO: Wait for GeminiUtils ready
            /*
            if (!GeminiUtils.isValidSlot(slotId)) {
                ret = 7; // No sim has been connected
                return ret;
            }
            */
            Log.d(LOG_TAG, "btSimapApduRequest start " + type + ", mBtSapPhone " + mBtSapPhone);
            mBtSapPhone.sendBtSimProfile(5, type, cmdAPDU, callback);

            while (!mDone) {
                try {
                    Log.d(LOG_TAG, "wait for done");
                    wait();
                } catch (InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }
            Log.d(LOG_TAG, "done");
            if (mRet == 0)  {
                Log.d(LOG_TAG, "btSimapApduRequest APDU " + mBtRsp.getApduString());
            } else {
                ret = mRet;
            }

            Log.d(LOG_TAG, "synchronized btSimapApduRequest ret " + ret);
            return ret;
        }
    }
    /* BT SIM operation end */

    // MVNO-API START
    public String getMvnoMatchType(long subId) {
        String type = getPhone(subId).getMvnoMatchType();
        if (DBG) log("getMvnoMatchTypeUsingSub sub = " + subId + " ,vMailAlphaTag = " + type);
        return type;
    }

    public String getMvnoPattern(long subId, String type) {
        String pattern = getPhone(subId).getMvnoPattern(type);
        if (DBG) log("getMvnoPatternUsingSub sub = " + subId + " ,vMailAlphaTag = " + pattern);
        return pattern;
    }
    // MVNO-API END

    /**
     * Make sure the caller has the READ_PRIVILEGED_PHONE_STATE permission.
     *
     * @throws SecurityException if the caller does not have the required permission
     */
    private void enforcePrivilegedPhoneStatePermission() {
        mApp.enforceCallingOrSelfPermission(android.Manifest.permission.READ_PRIVILEGED_PHONE_STATE,
                null);
    }

    /**
     * Request to run AKA authenitcation on UICC card by indicated family.
     *
     * @param slotId indicated sim id
     * @param family indiacted family category
     *        UiccController.APP_FAM_3GPP =  1; //SIM/USIM
     *        UiccController.APP_FAM_3GPP2 = 2; //RUIM/CSIM
     *        UiccController.APP_FAM_IMS   = 3; //ISIM
     * @param byteRand random challenge in byte array
     * @param byteAutn authenication token in byte array
     *
     * @return reponse paramenters/data from UICC
     *
     */
    public byte[] simAkaAuthentication(int slotId, int family, byte[] byteRand, byte[] byteAutn) {
        enforcePrivilegedPhoneStatePermission();

        // TODO: new thread every time?
        final SimAuth doSimAuth = new SimAuth(mPhone);
        String strRand = "";
        String strAutn = "";
        log("simAkaAuth session is " + family + " simId " + slotId);

        if (byteRand != null && byteRand.length > 0) {
            strRand = IccUtils.bytesToHexString(byteRand).substring(0, byteRand.length * 2);
        }

        if (byteAutn != null && byteAutn.length > 0) {
            strAutn = IccUtils.bytesToHexString(byteAutn).substring(0, byteAutn.length * 2);
        }
        log("simAkaAuth strRand is " + strRand + " strAutn " + strAutn);

        doSimAuth.start();

        return doSimAuth.doGeneralSimAuth(slotId, family, 0, 0, strRand, strAutn);
    }

    /**
     * Request to run GBA authenitcation (Bootstrapping Mode)on UICC card
     * by indicated family.
     *
     * @param slotId indicated sim id
     * @param family indiacted family category
     *        UiccController.APP_FAM_3GPP =  1; //SIM/USIM
     *        UiccController.APP_FAM_3GPP2 = 2; //RUIM/CSIM
     *        UiccController.APP_FAM_IMS   = 3; //ISIM
     * @param byteRand random challenge in byte array
     * @param byteAutn authenication token in byte array
     *
     * @return reponse paramenters/data from UICC
     *
     */
    public byte[] simGbaAuthBootStrapMode(int slotId, int family, byte[] byteRand, byte[] byteAutn) {
        enforcePrivilegedPhoneStatePermission();

        final SimAuth doSimAuth = new SimAuth(mPhone);
        String strRand = "";
        String strAutn = "";
        log("simGbaAuthBootStrap session is " + family + " simId " + slotId);

        if (byteRand != null && byteRand.length > 0) {
            strRand = IccUtils.bytesToHexString(byteRand).substring(0, byteRand.length * 2);
        }

        if (byteAutn != null && byteAutn.length > 0) {
            strAutn = IccUtils.bytesToHexString(byteAutn).substring(0, byteAutn.length * 2);
        }
        log("simGbaAuthBootStrap strRand is " + strRand + " strAutn " + strAutn);

        doSimAuth.start();

        return doSimAuth.doGeneralSimAuth(slotId, family, 1, 0xDD, strRand, strAutn);
    }

    /**
     * Request to run GBA authenitcation (NAF Derivation Mode)on UICC card
     * by indicated family.
     *
     * @param slotId indicated sim id
     * @param family indiacted family category
     *        UiccController.APP_FAM_3GPP =  1; //SIM/USIM
     *        UiccController.APP_FAM_3GPP2 = 2; //RUIM/CSIM
     *        UiccController.APP_FAM_IMS   = 3; //ISIM
     * @param byteNafId network application function id in byte array
     * @param byteImpi IMS private user identity in byte array
     *
     * @return reponse paramenters/data from UICC
     *
     */
    public byte[] simGbaAuthNafMode(int slotId, int family, byte[] byteNafId, byte[] byteImpi) {
        enforcePrivilegedPhoneStatePermission();

        final SimAuth doSimAuth = new SimAuth(mPhone);
        String strNafId = "";
        String strImpi = "";
        log("simGbaAuthBootStrap session is " + family + " simId " + slotId);

        if (byteNafId != null && byteNafId.length > 0) {
            strNafId = IccUtils.bytesToHexString(byteNafId).substring(0, byteNafId.length * 2);
        }

        /* ISIM GBA NAF mode parameter should be NAF_ID.
         * USIM GAB NAF mode parameter should be NAF_ID + IMPI
         * If getIccApplicationChannel got 0, mean that ISIM not support */
        if (UiccController.getInstance().getIccApplicationChannel(slotId, family) == 0) {
            log("simGbaAuthBootStrap ISIM not support.");
            if (byteImpi != null && byteImpi.length > 0) {
                strImpi = IccUtils.bytesToHexString(byteImpi).substring(0, byteImpi.length * 2);
            }
        }
        log("simGbaAuthBootStrap NAF ID is " + strNafId + " IMPI " + strImpi);

        doSimAuth.start();

        return doSimAuth.doGeneralSimAuth(slotId, family, 1, 0xDE, strNafId, strImpi);
    }

    /**
     * Since MTK keyguard has dismiss feature, we need to retrigger unlock event
     * when user try to access the SIM card.
     *
     * @param subId inidicated subscription
     *
     * @return true represent broadcast a unlock intent to notify keyguard
     *         false represent current state is not LOCKED state. No need to retrigger.
     *
     */
    public boolean broadcastIccUnlockIntent(long subId) {
        int state = TelephonyManager.getDefault().getSimState(SubscriptionManager.getSlotId(subId));

        log("[broadcastIccUnlockIntent] subId:" + subId + " state: " + state);

        String lockedReasion = "";

        switch (state) {
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                lockedReasion = IccCardConstants.INTENT_VALUE_LOCKED_ON_PIN;
                break;
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                lockedReasion = IccCardConstants.INTENT_VALUE_LOCKED_ON_PUK;
                break;
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                switch (getPhone(subId).getIccCard().getNetworkPersoType()) {
                    case PERSOSUBSTATE_SIM_NETWORK:
                        lockedReasion = IccCardConstants.INTENT_VALUE_LOCKED_NETWORK;
                        break;
                    case PERSOSUBSTATE_SIM_NETWORK_SUBSET:
                        lockedReasion = IccCardConstants.INTENT_VALUE_LOCKED_NETWORK_SUBSET;
                        break;
                    case PERSOSUBSTATE_SIM_CORPORATE:
                        lockedReasion = IccCardConstants.INTENT_VALUE_LOCKED_CORPORATE;
                        break;
                    case PERSOSUBSTATE_SIM_SERVICE_PROVIDER:
                        lockedReasion = IccCardConstants.INTENT_VALUE_LOCKED_SERVICE_PROVIDER;
                        break;
                    case PERSOSUBSTATE_SIM_SIM:
                        lockedReasion = IccCardConstants.INTENT_VALUE_LOCKED_SIM;
                        break;
                    default:
                        lockedReasion = IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
                }
                break;
            default:
                return false;
        }

        Intent intent = new Intent(TelephonyIntents.ACTION_UNLOCK_SIM_LOCK);

        intent.putExtra(IccCardConstants.INTENT_KEY_ICC_STATE,
                         IccCardConstants.INTENT_VALUE_ICC_LOCKED);
        intent.putExtra(IccCardConstants.INTENT_KEY_LOCKED_REASON, lockedReasion);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, SubscriptionManager.getPhoneId(subId));
        log("[broadcastIccUnlockIntent] Broadcasting intent ACTION_UNLOCK_SIM_LOCK "
            + " reason " + state + " for slotId : " + SubscriptionManager.getSlotId(subId));

        mApp.sendBroadcastAsUser(intent, UserHandle.ALL);

        return true;
    }

    /**
     * Query if the radio is turned off by user.
     *
     * @param subId inidicated subscription
     *
     * @return true radio is turned off by user.
     *         false radio isn't turned off by user.
     *
     */
    public boolean isRadioOffBySimManagement(long subId) {
        boolean result = true;
        try {
            Context otherAppsContext = mApp.createPackageContext(
                    "com.android.phone", Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences mIccidPreference =
                    otherAppsContext.getSharedPreferences("RADIO_STATUS", 0);

            SubInfoRecord subInfo = SubscriptionManager.getSubInfoForSubscriber(subId);
            if ((subInfo != null) && (mIccidPreference != null)) {
                log("[isRadioOffBySimManagement]SharedPreferences: "
                        + mIccidPreference.getAll().size() + ", IccId: " + subInfo.iccId);
                result = mIccidPreference.contains(subInfo.iccId);
            }
            log("[isRadioOffBySimManagement]result: " + result);
        } catch (NameNotFoundException e) {
            log("Fail to create com.android.phone createPackageContext");
        }
        return result;
    }

    // SIM switch
    /**
     * Get current phone capability
     *
     * @return the capability of phone. (@see PhoneConstants)
     * @internal
     */
    public int getPhoneCapability(int phoneId) {
        //return PhoneConstants.CAPABILITY_34G;
        return 0;
    }

    /**
     * Set capability to phones
     *
     * @param phoneId phones want to change capability
     * @param capability new capability for each phone
     * @internal
     */
    public void setPhoneCapability(int[] phoneId, int[] capability) {

    }

    /**
     * To config SIM swap mode(for dsda).
     *
     * @return true if config SIM Swap mode successful, or return false
     * @internal
     */
    public boolean configSimSwap(boolean toSwapped) {
        return true;
    }

    /**
     * To check SIM is swapped or not(for dsda).
     *
     * @return true if swapped, or return false
     * @internal
     */
    public boolean isSimSwapped() {
        return false;
    }

    /**
     * To Check if Capability Switch Manual Control Mode Enabled.
     *
     * @return true if Capability Switch manual control mode is enabled, else false;
     * @internal
     */
    public boolean isCapSwitchManualEnabled() {
        return true;
    }

    /**
     * Get item list that will be displayed on manual switch setting
     *
     * @return String[] contains items
     * @internal
     */
    public String[] getCapSwitchManualList() {
        return null;
    }


  /**
     * To get located PLMN from sepcified SIM modem  protocol
     * Returns current located PLMN string(ex: "46000") or null if not availble (ex: in flight mode or no signal area or this SIM is turned off)
     * @param subId Indicate which SIM subscription to query
     * @internal
     */
    public String getLocatedPlmn(long subId) {
        return getPhone(subId).getLocatedPlmn();
    }

   /**
     * Check if phone is hiding network temporary out of service state.
     * @param subId Indicate which SIM subscription to query
     * @return if phone is hiding network temporary out of service state.
     * @internal
    */
    public int getNetworkHideState(long subId) {
        return getPhone(subId).getNetworkHideState();
    }

   /**
     * Enable or Disable IMS Service
     * @param subId Indicate which SIM subscription to set
     * @param set enable or not (0: disable, 1:enable)
     * @internal
    */
    public void setImsEnable(long subId, boolean enable) {
       log("setImsEnable subId:" + subId + " enable:" + enable);
       if (SystemProperties.get("ro.mtk_ims_support").equals("1")) {
           if (!SystemProperties.get("ro.mtk_gemini_support").equals("1")) {
               RadioManager.getInstance().setIMSEnabled(enable, RadioManager.SwitchImsScenario.SWITCH_IMS_RUNTIME);
           }
       }
    }

   /**
     * Get IMS state.
     * @param subId Indicate which SIM subscription to set
     * @return IMS state
     * @internal
     */
    public int getImsState(long subId) {
        log("getImsEnable subId:" + subId);
        if (SystemProperties.get("ro.mtk_ims_support").equals("1")) {
            if (!SystemProperties.get("ro.mtk_gemini_support").equals("1")) {
                return RadioManager.getInstance().getIMSState();
            }
        }
        return PhoneConstants.IMS_STATE_DISABLED;
    }

   /**
     * Get the network service state for specified SIM.
     * @param subId Indicate which SIM subscription to query
     * @return service state.
     * @internal
     */
    public Bundle getServiceState(long subId) {
        Bundle data = new Bundle();
        getPhone(subId).getServiceState().fillInNotifierBundle(data);

        return data;
    }

    /**
     * Helper thread to turn async call to {@link #SimAuthentication} into
     * a synchronous one.
     */
    private static class SimAuth extends Thread {
        private Phone mTargetPhone;
        private boolean mDone = false;
        private IccIoResult mResponse = null;

        // For replies from SimCard interface
        private Handler mHandler;

        // For async handler to identify request type
        private static final int SIM_AUTH_GENERAL_COMPLETE = 300;

        public SimAuth(Phone phone) {
            mTargetPhone = phone;
        }

        @Override
        public void run() {
            Looper.prepare();
            synchronized (SimAuth.this) {
                mHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        AsyncResult ar = (AsyncResult) msg.obj;
                        switch (msg.what) {
                            case SIM_AUTH_GENERAL_COMPLETE:
                                log("SIM_AUTH_GENERAL_COMPLETE");
                                synchronized (SimAuth.this) {
                                    if (ar.exception != null) {
                                        log("SIM Auth Fail");
                                        mResponse = (IccIoResult) (ar.result);
                                    } else {
                                        mResponse = (IccIoResult) (ar.result);
                                    }
                                    log("SIM_AUTH_GENERAL_COMPLETE result is " + mResponse);
                                    mDone = true;
                                    SimAuth.this.notifyAll();
                                }
                                break;
                        }
                    }
                };
                SimAuth.this.notifyAll();
            }
            Looper.loop();
        }

        synchronized byte[] doGeneralSimAuth(int slotId, int family, int mode, int tag,
                String strRand, String strAutn) {

            while (mHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            Message callback = Message.obtain(mHandler, SIM_AUTH_GENERAL_COMPLETE);

            int sessionId = UiccController.getInstance().getIccApplicationChannel(slotId, family);
            log("family = " + family + ", sessionId = " + sessionId);

            long[] subId = SubscriptionManager.getSubId(slotId);
            if (subId == null) {
                log("slotId = " + slotId + ", subId is invalid.");
                return null;
            } else {
                getPhone(subId[0]).doGeneralSimAuthentication(sessionId, mode, tag, strRand, strAutn, callback);
            }

            while (!mDone) {
                try {
                    log("wait for done");
                    wait();
                } catch (InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }
            int len = 0;
            byte[] result = null;

            if (mResponse != null) {
                // 2 bytes for sw1 and sw2
                len = 2 + ((mResponse.payload == null) ? 0 : mResponse.payload.length);
                result = new byte[len];

                if (mResponse.payload != null) {
                    System.arraycopy(mResponse.payload, 0, result, 0, mResponse.payload.length);
                }

                result[len - 1] = (byte) mResponse.sw2;
                result[len - 2] = (byte) mResponse.sw1;

                for (int i = 0; i < len ; i++) {
                    log("Result = " + result[i]);
                }
                log("Result = " + new String(result));
            } else {
                log("mResponse is null.");
            }

            log("done");
            return result;
        }
    }

   /**
    * This function is used to get SIM phonebook storage information
    * by sim id.
    *
    * @param simId Indicate which sim(slot) to query
    * @return int[] which incated the storage info
    *         int[0]; // # of remaining entries
    *         int[1]; // # of total entries
    *         int[2]; // # max length of number
    *         int[3]; // # max length of alpha id
    *
    */
    public int[] getAdnStorageInfo(long subId) {
        Log.d(LOG_TAG, "getAdnStorageInfo " + subId);

        if (SubscriptionManager.isValidSubId(subId) == true) {
            if (mAdnInfoThread == null) {
                Log.d(LOG_TAG, "getAdnStorageInfo new thread ");
                mAdnInfoThread  = new QueryAdnInfoThread(subId);
                mAdnInfoThread.start();
            } else {
                mAdnInfoThread.setSubId(subId);
                Log.d(LOG_TAG, "getAdnStorageInfo old thread ");
            }
            return mAdnInfoThread.GetAdnStorageInfo();
        } else {
            Log.d(LOG_TAG, "getAdnStorageInfo subId is invalid.");
            int[] recordSize;
            recordSize = new int[4];
            recordSize[0] = 0; // # of remaining entries
            recordSize[1] = 0; // # of total entries
            recordSize[2] = 0; // # max length of number
            recordSize[3] = 0; // # max length of alpha id
            return recordSize;
        }
    }

    private static class QueryAdnInfoThread extends Thread {

        private long mSubId;
        private boolean mDone = false;
        private int[] recordSize;

        private Handler mHandler;

        // For async handler to identify request type
        private static final int EVENT_QUERY_PHB_ADN_INFO = 100;

        public QueryAdnInfoThread(long subId) {
            mSubId = subId;
        }
        public void setSubId(long subId) {
            mSubId = subId;
            mDone = false;
        }

        @Override
        public void run() {
            Looper.prepare();
            synchronized (QueryAdnInfoThread.this) {
                mHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        AsyncResult ar = (AsyncResult) msg.obj;

                        switch (msg.what) {
                            case EVENT_QUERY_PHB_ADN_INFO:
                                Log.d(LOG_TAG, "EVENT_QUERY_PHB_ADN_INFO");
                                synchronized (QueryAdnInfoThread.this) {
                                    mDone = true;
                                    int[] info = (int[]) (ar.result);
                                    if (info != null) {
                                        recordSize = new int[4];
                                        recordSize[0] = info[0]; // # of remaining entries
                                        recordSize[1] = info[1]; // # of total entries
                                        recordSize[2] = info[2]; // # max length of number
                                        recordSize[3] = info[3]; // # max length of alpha id
                                        Log.d(LOG_TAG, "recordSize[0]=" + recordSize[0] + ",recordSize[1]=" + recordSize[1] +
                                                         "recordSize[2]=" + recordSize[2] + ",recordSize[3]=" + recordSize[3]);
                                    }
                                    else {
                                        recordSize = new int[4];
                                        recordSize[0] = 0; // # of remaining entries
                                        recordSize[1] = 0; // # of total entries
                                        recordSize[2] = 0; // # max length of number
                                        recordSize[3] = 0; // # max length of alpha id
                                    }
                                    QueryAdnInfoThread.this.notifyAll();

                                }
                                break;
                            }
                      }
                };
                QueryAdnInfoThread.this.notifyAll();
            }
            Looper.loop();
        }

        public int[] GetAdnStorageInfo() {
            synchronized (QueryAdnInfoThread.this) {
                while (mHandler == null) {
                    try {
                        QueryAdnInfoThread.this.wait();

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                Message response = Message.obtain(mHandler, EVENT_QUERY_PHB_ADN_INFO);

                getPhone(mSubId).queryPhbStorageInfo(RILConstants.PHB_ADN, response);

                while (!mDone) {
                    try {
                        Log.d(LOG_TAG, "wait for done");
                        QueryAdnInfoThread.this.wait();
                    } catch (InterruptedException e) {
                        // Restore the interrupted status
                        Thread.currentThread().interrupt();
                    }
                }
                Log.d(LOG_TAG, "done");
                return recordSize;
            }
        }
    }

   /**
    * This function is used to check if the SIM phonebook is ready
    * by sim id.
    *
    * @param simId Indicate which sim(slot) to query
    * @return true if phone book is ready.
    *
    */
    public boolean isPhbReady(long subId) {
        String strPhbReady = "false";
        String strAllSimState = "";
        String strCurSimState = "";
        boolean isSimLocked = false;
        int phoneId = SubscriptionManager.getPhoneId(subId);
        int slotId = SubscriptionManager.getSlotId(subId);

        if (SubscriptionManager.isValidSlotId(slotId) == true) {
            strAllSimState = SystemProperties.get(TelephonyProperties.PROPERTY_SIM_STATE);

            if ((strAllSimState != null) && (strAllSimState.length() > 0)) {
                String values[] = strAllSimState.split(",");
                if ((phoneId >= 0) && (phoneId < values.length) && (values[phoneId] != null)) {
                    strCurSimState = values[phoneId];
                }
            }

            isSimLocked = (strCurSimState.equals("NETWORK_LOCKED") || strCurSimState.equals("PIN_REQUIRED")); //In PUK_REQUIRED state, phb can be accessed.

            if (PhoneConstants.SIM_ID_2 == slotId) {
                strPhbReady = SystemProperties.get("gsm.sim.ril.phbready.2", "false");
            } else if (PhoneConstants.SIM_ID_3 == slotId) {
                strPhbReady = SystemProperties.get("gsm.sim.ril.phbready.3", "false");
            } else if (PhoneConstants.SIM_ID_4 == slotId) {
                strPhbReady = SystemProperties.get("gsm.sim.ril.phbready.4", "false");
            } else {
                strPhbReady = SystemProperties.get("gsm.sim.ril.phbready", "false");
            }
        }

        log("[isPhbReady] subId:" + subId + ", slotId: " + slotId + ", isPhbReady: " + strPhbReady + ",strSimState: " + strAllSimState);

        return (strPhbReady.equals("true") && !isSimLocked);
    }

    public boolean isAirplanemodeAvailableNow() {
        return true;
    }

    // SMS parts
    private class ScAddress {
        public String mAddress;
        public long mSubId = SubscriptionManager.INVALID_SUB_ID;

        public ScAddress(long subId, String addr) {
            mAddress = addr;
            mSubId = subId;
        }
    }

    /**
     * Get service center address
     *
     * @param subId subscription identity
     *
     * @return bundle value with error code and service message center address
     */
    public Bundle getScAddressUsingSubId(long subId) {
        log("getScAddressUsingSubId, subId: " + subId);

        int phoneId = SubscriptionManager.getPhoneId(subId);
        if (phoneId == SubscriptionManager.INVALID_PHONE_ID) {
            log("no corresponding phone id");
            return null;
        }

        Bundle result = (Bundle) sendRequest(CMD_HANDLE_GET_SCA, subId);

        log("getScAddressUsingSubId: exit with " + result.toString());

        return result;
    }

    /**
     * Set service message center address
     *
     * @param subId subscription identity
     * @param address service message center addressto be set
     *
     * @return true for success, false for failure
     */
    public boolean setScAddressUsingSubId(long subId, String address) {
        log("setScAddressUsingSubId, subId: " + subId);

        int phoneId = SubscriptionManager.getPhoneId(subId);
        if (phoneId == SubscriptionManager.INVALID_PHONE_ID) {
            log("no corresponding phone id");
            return false;
        }

        ScAddress scAddress = new ScAddress(subId, address);

        Boolean result = (Boolean) sendRequest(CMD_HANDLE_SET_SCA, scAddress);

        log("setScAddressUsingSubId: exit with " + result.booleanValue());
        return result.booleanValue();
    }
    // SMS part end

    // support IMS enable/disable
    private ArrayList<MessengerWrapper> mMessengerWrapperList = new ArrayList<MessengerWrapper>();

    private class MessengerWrapper {
        private Messenger mMessenger;

        private Handler mInternalHandler = new Handler(mMainThreadHandler.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                try {
                    Log.d(LOG_TAG, "MessengerWrapper callback triggered: " + msg.what);
                    mMessenger.send(Message.obtain(this, msg.what));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        };

        public MessengerWrapper(IBinder binder) {
            mMessenger = new Messenger(binder);
        }

        public Messenger getMessenger() {
            return mMessenger;
        }

        public Handler getHandler() {
            return mInternalHandler;
        }
    };

    public void registerForImsDisableDone(long subId, IBinder binder, int what) {
        if (binder != null) {
            Log.d(LOG_TAG, "registerForImsDisableDone: " + binder + ", " + what);
            MessengerWrapper messengerWrapper = new MessengerWrapper(binder);
            mMessengerWrapperList.add(messengerWrapper);
            if (SystemProperties.get("ro.mtk_ims_support").equals("1")) {
                if (!SystemProperties.get("ro.mtk_gemini_support").equals("1")) {
                    RadioManager.getInstance().registerForImsDisableDone(messengerWrapper.getHandler(), what, null);
                }
            }
        }
    }

    public void unregisterForImsDisableDone(long subId, IBinder binder) {
        Iterator<MessengerWrapper> iter = mMessengerWrapperList.iterator();
        while (iter.hasNext()) {
            MessengerWrapper messengerWrapper = (MessengerWrapper) iter.next();
            if (messengerWrapper.getMessenger().getBinder() == binder) {
                if (SystemProperties.get("ro.mtk_ims_support").equals("1")) {
                    if (!SystemProperties.get("ro.mtk_gemini_support").equals("1")) {
                        RadioManager.getInstance().unregisterForImsDisableDone(messengerWrapper.getHandler());
                    }
                }
                mMessengerWrapperList.remove(messengerWrapper);
                break;
            }
        }
    }

    // VOLTE
    /**
    * This function will check if the input cid is dedicate bearer.
    *
    * @param cid for checking is dedicate bearer or not
    * @param phoneId for getting the current using phone
    * @return boolean return dedicated bearer or not
    *                true: is a dedicated bearer for input cid
    *                false: not a dedicated bearer for input cid
    */
    public boolean isDedicateBearer(int cid, int phoneId) {
        return PhoneFactory.getPhone(phoneId).isDedicateBearer(cid);
    }

    /**
    * This function will disable Dedicate bearer.
    * @param reason for indicating what reason for disabling dedicate bearer
    * @param ddcid for indicating which dedicate beare cide need to be disable
    * @param phoneId for getting the current using phone
    * @return int return ddcid of disable dedicated bearer
    *            -1: some thing wrong
    */
    public int disableDedicateBearer(String reason, int ddcid, int phoneId) {
        return PhoneFactory.getPhone(phoneId).disableDedicateBearer(reason, ddcid);
    }

    /**
    * This function will enable Dedicate bearer.
    * <p>
    * @param apnType input apnType for enable dedicate bearer
    * @param signalingFlag boolean value for indicating signaling or not
    * @param qosStatus input qosStatus info
    * @param tftStatus input tftStatus info
    * @param phoneId for getting the current using phone
    * @return int return ddcid of enable dedicated bearer
    *            -1: some thing wrong
    */
    public int enableDedicateBearer(String apnType, boolean signalingFlag, QosStatus qosStatus,
                            TftStatus tftStatus, int phoneId) {
        return PhoneFactory.getPhone(phoneId).enableDedicateBearer(apnType, signalingFlag
                                , qosStatus, tftStatus);
    }

    /**
    * This function will abort Dedicate bearer.
    * @param reason for indicating what reason for abort enable dedicate bearer
    * @param ddcid for indicating which dedicate beare cide need to be abort
    * @param phoneId for getting the current using phone
    * @return int return ddcid of abort dedicated bearer
    *            -1: some thing wrong
    */
    public int abortEnableDedicateBearer(String reason, int ddcid, int phoneId) {
        return PhoneFactory.getPhone(phoneId).abortEnableDedicateBearer(reason, ddcid);
    }

    /**
     * This function will modify Dedicate bearer.
     *
     * @param cid for indicating which dedicate cid to modify
     * @param qosStatus input qosStatus for modify
     * @param tftStatus input tftStatus for modify
     * @param phoneId for getting the current using phone
     * @return int: return ddcid of modify dedicated bearer
     *            -1: some thing wrong
     */
    public int modifyDedicateBearer(int cid, QosStatus qosStatus, TftStatus tftStatus
                        , int phoneId) {
        return PhoneFactory.getPhone(phoneId).modifyDedicateBearer(cid, qosStatus, tftStatus);
    }

    /**
     * This function will set Default Bearer Config for apnContext.
     *
     * @param apnType for indicating which apnType to set default bearer config
     * @param defaultBearerConfig config of default bearer config to be set
     * @param phoneId for getting the current using phone
     * @return int: return success or not
     *            0: set default bearer config successfully
     */
    public int setDefaultBearerConfig(String apnType, DefaultBearerConfig defaultBearerConfig
                        , int phoneId) {
        log("setDefaultBearerConfig: apnType: " + apnType + " defaultBearerConfig: "
            + defaultBearerConfig);
        return PhoneFactory.getPhone(phoneId).setDefaultBearerConfig(apnType, defaultBearerConfig);
    }

    /**
     * This function will get Default Bearer properties for apn type.
     *
     * @param apnType input apn type for get the mapping default bearer properties
     * @param phoneId for getting the current using phone
     * @return DedicateBearerProperties return the default beare properties for input apn type
     *                             return null if something wrong
     *
     */
    public DedicateBearerProperties getDefaultBearerProperties(String apnType, int phoneId) {
        return PhoneFactory.getPhone(phoneId).getDefaultBearerProperties(apnType);
    }

    /**
     * This function will get DcFailCause with int format.
     *
     * @param apnType for geting which last error of apnType
     * @param phoneId for getting the current using phone
     * @return int: return int failCause value
     */
    public int getLastDataConnectionFailCause(String apnType, int phoneId) {
        DcFailCause failCause = PhoneFactory.getPhone(phoneId).
                                    getLastDataConnectionFailCause(apnType);
        return failCause.getErrorCode();
    }

    /**
     * This function will get deactivate cids.
     *
     * @param apnType for getting which apnType deactivate cid array
     * @param phoneId for getting the current using phone
     * @return int []: int array about cids which is(are) deactivated
     */
    public int [] getDeactivateCidArray(String apnType, int phoneId) {
        return PhoneFactory.getPhone(phoneId).getDeactivateCidArray(apnType);
    }

    /**
     * This function will get link properties of input apn type.
     *
     * @param apnType input apn type for geting link properties
     * @param phoneId for getting the current using phone
     * @return LinkProperties: return correspondent link properties with input apn type
     */
    public LinkProperties getLinkProperties(String apnType, int phoneId) {
        return PhoneFactory.getPhone(phoneId).getLinkProperties(apnType);
    }

    /**
     * This function will do pcscf Discovery.
     *
     * @param apnType input apn type for geting pcscf
     * @param cid input cid
     * @param phoneId for getting the current using phone
     * @param onComplete for response event while pcscf discovery done
     * @return int: return 0: OK, -1: failed
     */
    public int pcscfDiscovery(String apnType, int cid, int phoneId,
                        Message onComplete) {
        return PhoneFactory.getPhone(phoneId).pcscfDiscovery(apnType, cid, onComplete);
    }

    /**
     * Check if under capability switching.
     *
     * @return true if switching
     */
    public boolean isCapabilitySwitching() {
//        return ProxyController.getInstance().isCapabilitySwitching();
    	return false;
    }

    /// M: [C2K] Switch SVLTE RAT mode. @{
    /**
     * Switch SVLTE RAT mode.
     * @param mode the RAT mode.
     */
    public void switchSvlteRatMode(int mode) {
//        SvlteRatController.getInstance().setSvlteRatMode(mode, null);
    }
    /// @}

    public static final String GET_SC_ADDRESS_KEY_ADDRESS = "scAddress";
    public static final String GET_SC_ADDRESS_KEY_RESULT = "errorCode";
    public static final byte ERROR_CODE_GENERIC_ERROR = 0x01;
    public static final byte ERROR_CODE_NO_ERROR = 0x00;
}
