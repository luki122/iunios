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

import java.util.List;

import android.app.Activity;
import android.app.NotificationManager;
import android.os.Bundle;
import android.content.Context;

import com.android.internal.telephony.*;
import com.android.internal.telephony.test.SimulatedRadioControl;

import android.telephony.CellInfo;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.internal.telephony.test.SimulatedCommands;
import com.android.internal.telephony.uicc.UiccController;
import android.content.Context;
import android.net.LocalServerSocket;
import android.os.Looper;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.cdma.CDMAPhone;
import com.android.internal.telephony.cdma.CDMALTEPhone;
import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
import com.android.internal.telephony.gsm.GSMPhone;
import com.android.internal.telephony.sip.SipPhone;
import com.android.internal.telephony.sip.SipPhoneFactory;
import com.android.internal.telephony.uicc.UiccController;


/**
 * A simple activity that presents you with a UI for faking incoming phone operations.
 */
public class SimulatedPhoneFactory extends PhoneFactory {
    private static final String TAG = "SimulatedPhoneFactory";
    static private SimulatedCommands sc;
    static protected PhoneNotifier testPhoneNotifier;
    
    
    static protected final int SOCKET_OPEN_RETRY_MILLIS = 2 * 1000;
    static protected final int SOCKET_OPEN_MAX_RETRY = 3;

    //***** Class Variables

    static protected Phone sProxyPhone = null;
    static protected CommandsInterface sCommandsInterface = null;
    static protected CommandsInterface[] sCommandsInterfaces = null;

    static protected boolean sMadeDefaults = false;
    static protected PhoneNotifier sPhoneNotifier;
    static protected Looper sLooper;
    static protected Context sContext;

    protected static final int sPreferredCdmaSubscription =
                         CdmaSubscriptionSourceManager.PREFERRED_CDMA_SUBSCRIPTION;
    
    public static void makeDefaultPhones(Context context) {
        makeDefaultPhone(context);
//        testPhoneNotifier = new TestPhoneNotifier();
//        sc = new SimulatedCommands();
//        sProxyPhone = new SimulatedPhoneProxy((GSMPhone)((PhoneProxy)sProxyPhone).getActivePhone());
    }           
    
    
    public static void makeDefaultPhone(Context context) {
        synchronized(Phone.class) {
            if (!sMadeDefaults) {
                sLooper = Looper.myLooper();
                sContext = context;

                if (sLooper == null) {
                    throw new RuntimeException(
                        "PhoneFactory.makeDefaultPhone must be called from Looper thread");
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
                        throw new RuntimeException("PhoneFactory probably already running");
                    } else {
                        try {
                            Thread.sleep(SOCKET_OPEN_RETRY_MILLIS);
                        } catch (InterruptedException er) {
                        }
                    }
                }

//                sPhoneNotifier = new DefaultPhoneNotifier();
                testPhoneNotifier = new TestPhoneNotifier();

                // Get preferred network mode
                int preferredNetworkMode = RILConstants.PREFERRED_NETWORK_MODE;
                if (TelephonyManager.getLteOnCdmaModeStatic() == PhoneConstants.LTE_ON_CDMA_TRUE) {
                    preferredNetworkMode = Phone.NT_MODE_GLOBAL;
                }
                int networkMode = Settings.Global.getInt(context.getContentResolver(),
                        Settings.Global.PREFERRED_NETWORK_MODE, preferredNetworkMode);

                // As per certain operator requirement, the device is expected to be in global
                // mode from boot up, by enabling the property persist.env.phone.global the
                // network mode is set to global during boot up.
                if (SystemProperties.getBoolean("persist.env.phone.global", false)) {
//                    networkMode = Phone.NT_MODE_LTE_CMDA_EVDO_GSM_WCDMA;
                	 networkMode = 10;
                    Settings.Global.putInt(context.getContentResolver(),
                            Settings.Global.PREFERRED_NETWORK_MODE, networkMode);
                }

                // Get cdmaSubscription mode from Settings.Global
                int cdmaSubscription;
                cdmaSubscription = Settings.Global.getInt(context.getContentResolver(),
                                Settings.Global.CDMA_SUBSCRIPTION_MODE,
                                sPreferredCdmaSubscription);

                //reads the system properties and makes commandsinterface
                sCommandsInterface = new RIL(context, networkMode, cdmaSubscription);
                sc = new SimulatedCommands();

                // Instantiate UiccController so that all other classes can just call getInstance()
                UiccController.make(context, sc);

                int phoneType = TelephonyManager.getPhoneType(networkMode);
//                if (phoneType == PhoneConstants.PHONE_TYPE_GSM) {
                    sProxyPhone = new SimulatedPhoneProxy(new GSMPhone(context,
                            sc, testPhoneNotifier));
//                } else if (phoneType == PhoneConstants.PHONE_TYPE_CDMA) {
//                    switch (TelephonyManager.getLteOnCdmaModeStatic()) {
//                        case PhoneConstants.LTE_ON_CDMA_TRUE:
//                            sProxyPhone = new SimulatedPhoneProxy(new CDMALTEPhone(context,
//                                sc, testPhoneNotifier));
//                            break;
//                        case PhoneConstants.LTE_ON_CDMA_FALSE:
//                        default:
//                            sProxyPhone = new SimulatedPhoneProxy(new CDMAPhone(context,
//                                    sc, testPhoneNotifier));
//                            break;
//                    }
//                }

                sMadeDefaults = true;
            }
        }
    }
    
    
    public static Phone getSimulatedGsmPhone() {
        synchronized(PhoneProxy.lockForRadioTechnologyChange) {
        	Log.i("TAG", " simulated phone here");
            Phone phone = new GSMPhone(PhoneGlobals.getInstance(), sc, testPhoneNotifier, true);
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
