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

package com.android.incallui;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.incallui.Call;
import com.android.incallui.CallList;
import com.android.incallui.InCallApp;
import com.android.incallui.InCallPresenter;
import com.android.incallui.ManageDisconnect;
import com.android.incallui.ManagePhbReading;
import com.android.incallui.ManagePrivate;
import com.android.incallui.ManageRecord;
import com.android.incallui.ManageVibrator;
import com.android.incallui.PreventTouchManager;
import com.android.incallui.YuLoreUtils;

import android.os.AsyncResult;

public class AuroraCallMonitor implements CallList.Listener {
	private static final String LOG_TAG = "AuroraCallMonitor";

	Context mContext;
	ManagePrivate mManagePrivate;
	PreventTouchManager mPreventTouchManager;
	ManageRecord mManageRecord;
	ManageVibrator mManageVibrator;
	ManageDisconnect mManageDisconnect;
	ManagePhbReading mManagePhbReading;
	ManageVcardReading mManageVcardReading;
	ManagePower mManagePower;

    private static AuroraCallMonitor mAuroraCallMonitor;

	private AuroraCallMonitor(Context app) {
		mContext = app;
		mPreventTouchManager = new PreventTouchManager(app);
		mManageRecord = new ManageRecord(app);
		mManageVibrator = new ManageVibrator(app);
		mManageDisconnect = new ManageDisconnect(app);
		mManagePhbReading = new ManagePhbReading(app);
		mManageVcardReading = new ManageVcardReading(app);
		mManagePower = new ManagePower(app);

		if (AuroraPrivacyUtils.isSupportPrivate()) {
			mManagePrivate = ManagePrivate.init(app);
		}
	}
	
    public static synchronized AuroraCallMonitor getInstance() {
        if (mAuroraCallMonitor == null) {
        	mAuroraCallMonitor = new AuroraCallMonitor(InCallApp.getInstance());
        }
        return mAuroraCallMonitor;
    }

	public void onIncomingCall(Call call) {
		mPreventTouchManager.handle();
		mManagePower.handle();
	}

	public void onCallListChange(CallList callList) {
		mPreventTouchManager.handle();
		mManageRecord.handleRecordProc();
		mManageVibrator.onPhoneStateChanged();
		mManagePower.handle();
	}

	public void onDisconnect(Call call) {
		mManageVibrator.onDisconnect(call);
		mManageDisconnect.handle();
	}

	// / M: Add for recording. @{
	public void onStorageFull() {

	}

	public void onUpdateRecordState(final int state, final int customValue) {

	}

}
