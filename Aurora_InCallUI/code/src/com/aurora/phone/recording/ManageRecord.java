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

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.ActivityInfo;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.telephony.PhoneNumberUtils;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.DialerKeyListener;
import android.util.EventLog;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.android.incallui.CallList;
import com.android.incallui.InCallPresenter;
import com.android.incallui.ContactInfoCache.ContactCacheEntry;
import com.android.incallui.InCallPresenter.InCallState;

import android.widget.Toast;

//import aurora.widget.*;
//import aurora.app.*;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.System;

public class ManageRecord implements PhoneRecorderHandler.Listener {
	private static final String LOG_TAG = "ManageRecord";

	private Context mApp;


	private boolean mIsUserStopRecord = false;

	public ManageRecord(Context app) {
		mApp = app;

		PhoneRecorderHandler.getInstance().setListener(this);
		PhoneRecorderHandler.getInstance().setRecordTimeCallBack(
				mRecordTimeCallBack);

	}
	
	private int getRecordMode() {
 		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mApp);
 		return sp.getInt("record_mode", 0);
	}


	// aurora add liguangyu 20140331 for record end

	public void requestUpdateRecordState(final int state, final int customValue) {
	}

	public void onStorageFull() {
		log("onStorageFull");
		// handleStorageFull(false); // false for recording case
	}

	public void handleStorageFull(final boolean isForCheckingOrRecording) {
		// Gionee fangbin 20130131 added for CR00762482 start
		// if (PhoneApp.ISGNPHONE) {
		// return;
		// }
		// Gionee fangbin 20130131 added for CR00762482 end
		// if (PhoneUtils.getMountedStorageCount() > 1) {
		// // SD card case
		// log("handleStorageFull(), mounted storage count > 1");
		// if (Constants.STORAGE_TYPE_SD_CARD ==
		// PhoneUtils.getDefaultStorageType()) {
		// log("handleStorageFull(), SD card is using");
		// showStorageFullDialog(com.mediatek.internal.R.string.storage_sd,
		// true);
		// } else if (Constants.STORAGE_TYPE_PHONE_STORAGE ==
		// PhoneUtils.getDefaultStorageType()) {
		// log("handleStorageFull(), phone storage is using");
		// showStorageFullDialog(com.mediatek.internal.R.string.storage_withsd,
		// true);
		// } else {
		// // never happen here
		// log("handleStorageFull(), never happen here");
		// }
		// } else if (1 == PhoneUtils.getMountedStorageCount()) {
		// log("handleStorageFull(), mounted storage count == 1");
		// if (Constants.STORAGE_TYPE_SD_CARD ==
		// PhoneUtils.getDefaultStorageType()) {
		// log("handleStorageFull(), SD card is using, " +
		// (isForCheckingOrRecording ? "checking case" : "recording case"));
		// String toast = isForCheckingOrRecording ?
		// getResources().getString(R.string.vt_sd_not_enough) :
		// getResources().getString(R.string.vt_recording_saved_sd_full);
		// Toast.makeText(InCallApp.getInstance(), toast,
		// Toast.LENGTH_LONG).show();
		// } else if (Constants.STORAGE_TYPE_PHONE_STORAGE ==
		// PhoneUtils.getDefaultStorageType()) {
		// // only Phone storage case
		// log("handleStorageFull(), phone storage is using");
		// showStorageFullDialog(com.mediatek.internal.R.string.storage_withoutsd,
		// false);
		// } else {
		// // never happen here
		// log("handleStorageFull(), never happen here");
		// }
		// }
	}

	private PhoneRecorderHandler.RecordTimeCallBack mRecordTimeCallBack = new PhoneRecorderHandler.RecordTimeCallBack() {

		@Override
		public void callBack(long time, boolean visible) {
			// TODO Auto-generated method stub
			requestRecordButton(visible);
			// mCallCard.updateRecordTime(DateUtils.formatElapsedTime(time /
			// 1000), visible);
			InCallPresenter.getInstance().getInCallActivity().getCallButtonFragment().updateRecordTime(
					DateUtils.formatElapsedTime(time / 1000), visible);
		}

		@Override
		public void onError() {
			stopRecord();
		}
	};

	private void requestRecordButton(boolean visible) {		
		if (InCallPresenter.getInstance().getInCallActivity().getCallButtonFragment().mRecordButton.isChecked() != visible) {
			InCallPresenter.getInstance().getInCallActivity().getCallButtonFragment().mRecordButton
					.setChecked(visible);
		}
	}

	public void onRecordClick() {
		if (!GnPhoneRecordHelper.isExternalStorageMounted()) {
			Toast.makeText(
					mApp,
					mApp.getResources().getString(
							R.string.aurora_error_sdcard_access),
					Toast.LENGTH_LONG).show();
			return;
		}
		if (!GnPhoneRecordHelper
				.diskSpaceAvailable(AuroraConstants.PHONE_RECORD_LOW_STORAGE_THRESHOLD)) {
			// if (PhoneApp.ISGNPHONE) {
			InCallPresenter.getInstance().getInCallActivity().getCallButtonFragment().mRecordButton
					.setChecked(false);
			Toast.makeText(mApp,
					mApp.getResources().getString(R.string.aurora_no_storage),
					Toast.LENGTH_LONG).show();
			// }
			return;
		}

		PhoneRecorder phoneRecorder = PhoneRecorder.getInstance(mApp);
		if (!phoneRecorder.ismFlagRecord()) {
			startRecord();
		} else {
			AuroraCallMonitor.getInstance().mManageRecord.setUserStop(true);
			stopRecord();
		}
	}

	private void startRecord() {
		try {
			ReporterUtils.addRecordCount();
		} catch (Exception e) {
			e.printStackTrace();
		}
		PhoneRecorderHandler.getInstance().setRecordTimeCallBack(
				mRecordTimeCallBack);
		String contacts = "";
		String name = "";
		String number = "";
		Call call = CallList.getInstance().getActiveCall();
		if(call != null) {
	        final ContactInfoCache cache = ContactInfoCache.getInstance(InCallApp.getInstance());
	        ContactCacheEntry cce= cache.getInfo(call.getId());
	    	contacts = cce.mRawContactId + "";
			name = cce.name;
			number = cce.number;
		}
		PhoneRecorderHandler.getInstance().startVoiceRecord(
				AuroraConstants.PHONE_RECORDING_VOICE_CALL_CUSTOM_VALUE, contacts,
				name, number);
		// mHandler.postDelayed(mRecordDiskCheck, 500);
	}

	/**
	 * Stop recording service.
	 */
	private void stopRecord() {
		PhoneRecorderHandler.getInstance().stopVoiceRecord();
		PhoneRecorderHandler.getInstance().setRecordTimeCallBack(null);
	}

	public void handleRecordProc() {
		log("handleRecordProc");
		InCallState state = InCallPresenter.getInstance().getInCallState();
		PhoneRecorder phoneRecorder = PhoneRecorder.getInstance(mApp);
		if (state == InCallState.NO_CALLS) {
			log("the record custom value is "
					+ PhoneRecorderHandler.getInstance().getCustomValue());
			if (AuroraConstants.PHONE_RECORDING_VOICE_CALL_CUSTOM_VALUE == PhoneRecorderHandler
					.getInstance().getCustomValue()) {
				if (PhoneRecorder.isRecording()) {
					stopRecord();
				}
			}
			setUserStop(false);
		}
		// This code is added for CALLWAITING state.
		// When in CALLWAITING state, a new call is incoming, even if recording
		// now,
		// need hide recording flash icon, so here needs update
		handleAutoRecord();
	}

	public void handleAutoRecord() {
		InCallState state = InCallPresenter.getInstance().getInCallState();
		if (state == InCallState.INCALL) {
			boolean isAutoRecord = false;
			if (getRecordMode() == 0) {
				isAutoRecord = false;
			} else if (getRecordMode() == 1) {
				isAutoRecord = true;
			} else {
				Call call = CallList.getInstance().getActiveCall();
				String number = "";
				if(call != null) {
					number = call.getNumber();
				}
				log("-handleAutoRecord c.getAddress() = " + number);
				if (RecordUtils.mRecordMap.get(number) != null
						&& RecordUtils.mRecordMap.get(number)) {
					isAutoRecord = true;
					RecordUtils.mRecordMap.put(number, false);
				}
			}		
			PhoneRecorder phoneRecorder = PhoneRecorder.getInstance(mApp);
			log("handleAutoRecord isAutoRecord = " + isAutoRecord + " mIsUserStopRecord = " + mIsUserStopRecord + " phoneRecorder.ismFlagRecord() = " + phoneRecorder.ismFlagRecord());
			if (!phoneRecorder.ismFlagRecord() && isAutoRecord
					&& !mIsUserStopRecord) {
				log("-handleAutoRecord start");
//				mApp.notifier.playRecordTone();
				onRecordClick();
				InCallPresenter.getInstance().getInCallActivity().getCallButtonFragment().updateRecordBtnState();
			}
		}
		InCallState phonestate = InCallPresenter.getInstance().getInCallState();
		if (phonestate == InCallState.NO_CALLS) {
			RecordUtils.mRecordMap.clear();
		}
	}

	// aurora add liguangyu 20140331 for record end

	public void handleHoldClick() {
		if (PhoneRecorder.isRecording()) {
			mIsUserStopRecord = true;
			stopRecord();
		}
	}

	public void setUserStop(boolean value) {
		log("setUserStop = " + value); 
		mIsUserStopRecord = value;
	}

	public void clear() {
		PhoneRecorderHandler.getInstance().clearListener(this);
		PhoneRecorderHandler.getInstance().setRecordTimeCallBack(null);
		PhoneRecorderHandler.getInstance().setListener(null);
	}

	private void log(String msg) {
		Log.d(LOG_TAG, msg);
	}

}
