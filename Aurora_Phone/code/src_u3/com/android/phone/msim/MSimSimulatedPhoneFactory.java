/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.phone;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.LocalServerSocket;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings;
import android.os.SystemProperties;
import android.telephony.MSimTelephonyManager;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.content.Intent;
import android.provider.Settings.SettingNotFoundException;

import com.android.internal.telephony.*;
import com.codeaurora.telephony.msim.*;

import static com.android.internal.telephony.TelephonyProperties.PROPERTY_DEFAULT_SUBSCRIPTION;
import com.android.internal.telephony.test.SimulatedCommands;
/**
 * {@hide}
 */
public class MSimSimulatedPhoneFactory extends MSimPhoneFactory {
    //***** Class Variables
    static final String LOG_TAG = "MSimSimulatedPhoneFactory";
    static private SimulatedCommands[] sc = null;
    static protected PhoneNotifier testPhoneNotifier = null;
    static private Phone[] sProxyPhones = null;
    static private boolean sMadeMultiSimDefaults = false;

    static private MSimProxyManager mMSimProxyManager;
    static private CardSubscriptionManager mCardSubscriptionManager;
    static private SubscriptionManager mSubscriptionManager;
    static private MSimUiccController mUiccController;

    static private DefaultPhoneProxy sDefaultPhoneProxy = null;

    public static void makeMultiSimDefaultPhones(Context context) {
        makeMultiSimDefaultPhone(context);
    }

    public static void makeMultiSimDefaultPhone(Context context) {
        synchronized(Phone.class) {
            if (!sMadeMultiSimDefaults) {
                sLooper = Looper.myLooper();
                sContext = context;

                if (sLooper == null) {
                    throw new RuntimeException(
                        "MSimPhoneFactory.makeDefaultPhone must be called from Looper thread");
                }

                int retryCount = 0;
                for(;;) {
                    boolean hasException = false;
                    retryCount ++;

                    try {
                        // use UNIX domain socket to
                        // prevent subsequent initialization
                        new LocalServerSocket("com.android.internal.telephony");
                    } catch (java.io.IOException ex) {
                        hasException = true;
                    }

                    if ( !hasException ) {
                        break;
                    } else if (retryCount > SOCKET_OPEN_MAX_RETRY) {
                        throw new RuntimeException("MSimPhoneFactory probably already running");
                    } else {
                        try {
                            Thread.sleep(SOCKET_OPEN_RETRY_MILLIS);
                        } catch (InterruptedException er) {
                        }
                    }
                }

//                sPhoneNotifier = new MSimDefaultPhoneNotifier();
                testPhoneNotifier = new TestPhoneNotifier();

                // Get preferred network mode
                int preferredNetworkMode = RILConstants.PREFERRED_NETWORK_MODE;
     
                /* In case of multi SIM mode two instances of PhoneProxy, RIL are created,
                   where as in single SIM mode only instance. isMultiSimEnabled() function checks
                   whether it is single SIM or multi SIM mode */
                int numPhones = MSimTelephonyManager.getDefault().getPhoneCount();
                int[] networkModes = new int[numPhones];
                sProxyPhones = new MSimPhoneProxy[numPhones];
                sc = new SimulatedCommands[numPhones];

                for (int i = 0; i < numPhones; i++) {
                    //reads the system properties and makes commandsinterface
                    try {
                        networkModes[i]  = MSimTelephonyManager.getIntAtIndex(
                                context.getContentResolver(),
                                Settings.Global.PREFERRED_NETWORK_MODE, i);
                    } catch (SettingNotFoundException snfe) {
                        Rlog.e(LOG_TAG, "Settings Exception Reading Value At Index for"+
                                " Settings.Global.PREFERRED_NETWORK_MODE");
                        networkModes[i] = preferredNetworkMode;
                    }

                    if (SystemProperties.getBoolean("persist.env.phone.global", false) &&
                            i == MSimConstants.SUB1) {
                        networkModes[i] = Phone.NT_MODE_LTE_CMDA_EVDO_GSM_WCDMA;
                        MSimTelephonyManager.putIntAtIndex( context.getContentResolver(),
                            Settings.Global.PREFERRED_NETWORK_MODE, i, networkModes[i]);
                    }
                    sc[i] = new SimulatedCommands();
                }

                // Instantiate MSimUiccController so that all other classes can just
                // call getInstance()
                mUiccController = MSimUiccController.make(context, sc);

                mCardSubscriptionManager = CardSubscriptionManager.getInstance(context,
                        mUiccController, sc);
                mSubscriptionManager = SubscriptionManager.getInstance(context,
                        mUiccController, sc);

                for (int i = 0; i < numPhones; i++) {
                    PhoneBase phone = null;
                    phone = new MSimGSMPhone(context,
                                sc[i], testPhoneNotifier, true, i);

                    if (sDefaultPhoneProxy == null) {
                        sDefaultPhoneProxy = new DefaultPhoneProxy(phone);
                    }

                    sProxyPhones[i] = new MSimPhoneProxy(phone);
                }
                mMSimProxyManager = MSimProxyManager.getInstance(context, sProxyPhones,
                        mUiccController, sc);

                sMadeMultiSimDefaults = true;

                // Set the default phone in base class
                sProxyPhone = sProxyPhones[MSimConstants.DEFAULT_SUBSCRIPTION];
                sMadeDefaults = true;

                sDefaultPhoneProxy.updateDefaultPhoneInSubInfo(sProxyPhone);
                sDefaultPhoneProxy.updateDefaultSMSIntfManager(0);

            }
        }
    }

    public static Phone getMSimCdmaPhone(int subscription) {
        Phone phone;
        synchronized(PhoneProxy.lockForRadioTechnologyChange) {
            phone = new MSimCDMALTEPhone(sContext, sc[subscription],
            		testPhoneNotifier, true, subscription);
        }
        return phone;
    }

    public static Phone getMSimGsmPhone(int subscription) {
        synchronized(PhoneProxy.lockForRadioTechnologyChange) {
            Phone phone = new MSimGSMPhone(sContext, sc[subscription],
            		testPhoneNotifier, true, subscription);
            return phone;
        }
    }

    public static Phone getPhone(int subscription) {
        if (sLooper != Looper.myLooper()) {
            throw new RuntimeException(
                "MSimPhoneFactory.getPhone must be called from Looper thread");
        }
        if (!sMadeMultiSimDefaults) {
            if (!sMadeDefaults) {
                throw new IllegalStateException("Default phones haven't been made yet!");
            } else if (subscription == MSimConstants.DEFAULT_SUBSCRIPTION) {
                return sProxyPhone;
            }
        }
        return sProxyPhones[subscription];
    }


    public static Phone getSimulatedGsmPhone() {
        synchronized(PhoneProxy.lockForRadioTechnologyChange) {
            Phone phone = new MSimGSMPhone(PhoneGlobals.getInstance(), sc[MSimConstants.DEFAULT_SUBSCRIPTION], testPhoneNotifier, true, MSimConstants.DEFAULT_SUBSCRIPTION);
//            Phone phone = new GSMPhone(sContext, sCommandsInterface, sPhoneNotifier);
            return phone;
        }
    }
    
    public static Phone getDefaultPhone() {
        if (sLooper != Looper.myLooper()) {
            throw new RuntimeException(
                "PhoneFactory.getDefaultPhone must be called from Looper thread");
        }

        if (!sMadeDefaults) {
            throw new IllegalStateException("Default phones haven't been made yet!");
        }
       return sProxyPhone;
    }


}
