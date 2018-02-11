/*
 * Copyright (c) 2013 The Linux Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     * Neither the name of The Linux Foundation, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.android.phone;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.PhoneConstants;
import android.os.AsyncResult;
import java.io.File;
import java.io.FileWriter;
import android.os.SystemProperties;

public class ManagePower extends Handler{
	private static final String LOG_TAG = "ManagePower";

	private PhoneGlobals mApp;
	private CallManager mCM;
    private static final int PHONE_STATE_CHANGED = 101;

	public ManagePower(CallManager cm, PhoneGlobals app) {
		mCM = cm;
		mApp = app;
		mCM.registerForPreciseCallStateChanged(this, PHONE_STATE_CHANGED, null);

	}

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case PHONE_STATE_CHANGED:
			onPhoneStateChanged((AsyncResult) msg.obj);
			break;
		}
	}

	private void onPhoneStateChanged(AsyncResult r) {
		PhoneConstants.State state = mCM.getState();
		writePowerFile(state != PhoneConstants.State.IDLE ? 1 : 0);
	}

	private void writePowerFile(int value) {
        // Aurora xuyong 2015-08-25 deleted for Charging_CallState start
		/*String path = String
				.format("/sys/bus/platform/devices/battery/Charging_CallState");
		try {
			String state = String.valueOf(value);
			FileWriter fw = new FileWriter(path);
			fw.write(state);
			fw.close();
			Log.v(LOG_TAG, "writePowerFile " + value);
		} catch (Exception e) {
			e.printStackTrace();
		}*/
        // Aurora xuyong 2015-08-25 deleted for Charging_CallState end
        // Aurora xuyong 2015-08-25 modified for Charging_CallState start
		SystemProperties.set("debug.sys.custom.current" , value > 0 ? 1000000 + "" : 0 + "");
        // Aurora xuyong 2015-08-25 modified for Charging_CallState end
	}
}
