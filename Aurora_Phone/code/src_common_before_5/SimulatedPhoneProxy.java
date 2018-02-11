/*
 * Copyright (c) 2012-13, The Linux Foundation. All rights reserved.
 * Not a Contribution.
 *
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

package com.android.phone;


import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.LinkProperties;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.util.Log;

import com.android.internal.telephony.test.SimulatedRadioControl;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneBase;
import com.android.internal.telephony.PhoneFactory;
import android.content.SharedPreferences;
import com.android.internal.telephony.PhoneProxy;

import java.util.List;
import java.util.Map;

public class SimulatedPhoneProxy extends PhoneProxy {
    public SimulatedPhoneProxy(PhoneBase phone) {
    	super(phone);
    }          
        
    
//    protected void createNewPhone(int newVoiceRadioTech) {
//    	Log.i("SimulatedPhoneProxy", "createNewPhone");
//        if (ServiceState.isCdma(newVoiceRadioTech)) {
//        	mActivePhone = PhoneFactory.getCdmaPhone();
//        } else if (ServiceState.isGsm(newVoiceRadioTech)) {
//        	mActivePhone = PhoneFactory.getGsmPhone();
//        }          
//        
//        if(newVoiceRadioTech == 99) {
//        	mActivePhone = SimulatedPhoneFactory.getSimulatedGsmPhone();
//        }
//        
//    }
    
}
