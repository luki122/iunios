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

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.IBluetoothHeadsetPhone;
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
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyCapabilities;
import com.android.phone.Constants.CallStatusCode;
import com.android.phone.InCallUiState.InCallScreenMode;
import com.android.phone.InCallUiState.ProgressIndicationType;
import com.android.phone.OtaUtils.CdmaOtaScreenState;

import android.os.SystemVibrator;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import aurora.widget.*;
import aurora.app.*;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.Settings.System;
import android.view.MenuInflater;
import android.view.Menu;
import static com.android.phone.AuroraMSimConstants.SUBSCRIPTION_KEY;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.ViewConfiguration;

public class ManageRecord extends Handler implements PhoneRecorderHandler.Listener {
	private static final String LOG_TAG = "ManageRecord";

	private CallManager mCM;
	private PhoneGlobals mApp;
	private static final String AURORA_CALL_RECORD_TYPE = "aurora.call.record.type";
	private static final String AURORA_CALL_RECORD_ACTION = "com.android.contacts.AURORA_CALL_RECORD_ACTION";
	private final RecordReceiver mRecordReceiver;
	private static int mRecordSelection = 0;
	private boolean mIsUserStopRecord = false;
	private static final int PHONE_STATE_CHANGED = 1;
    protected static final int PHONE_DISCONNECT = 2;

	public ManageRecord(CallManager cm, PhoneGlobals app) {
		mCM = cm;
		mApp = app;
		mRecordReceiver = new RecordReceiver();
		IntentFilter recordFilter = new IntentFilter(AURORA_CALL_RECORD_ACTION);
		mApp.registerReceiver(mRecordReceiver, recordFilter);
		SharedPreferences sp = mApp.getSharedPreferences(
				"com.android.phone_preferences", Context.MODE_PRIVATE);
		mRecordSelection = sp.getInt("record_mode", 0);
		PhoneRecorderHandler.getInstance().setListener(this);
		PhoneRecorderHandler.getInstance().setRecordTimeCallBack(
				mRecordTimeCallBack);
		mCM.registerForPreciseCallStateChanged(this, PHONE_STATE_CHANGED,null);
		mCM.registerForDisconnect(this, PHONE_DISCONNECT, null);

	}
	
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case PHONE_STATE_CHANGED:
			handleRecordProc();
			break;			
		case PHONE_DISCONNECT:
		    onDisconnect((AsyncResult) msg.obj);
			break;
		}
	}
	
	  private void onDisconnect(AsyncResult r) {
	        Connection c = (Connection) r.result;
	        RecordUtils.mRecordMap.remove(c.getAddress());
	  }

	private class RecordReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(AURORA_CALL_RECORD_ACTION)) {
				// send to phone (0:close; 1:all; 2:select)
				mRecordSelection = intent.getIntExtra(AURORA_CALL_RECORD_TYPE,
						0);
				SharedPreferences sp = mApp.getSharedPreferences(
						"com.android.phone_preferences", Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();
				editor.putInt("record_mode", mRecordSelection);
				editor.commit();
			}
		}
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
		// Toast.makeText(PhoneGlobals.getInstance(), toast,
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
			getInCallScreen().getInCallTouchUi().updateRecordTime(
					DateUtils.formatElapsedTime(time / 1000), visible);
		}

		@Override
		public void onError() {
			stopRecord();
		}
	};

	private void requestRecordButton(boolean visible) {
		if (getInCallScreen().getInCallTouchUi().mRecordButton.isChecked() != visible) {
			getInCallScreen().getInCallTouchUi().mRecordButton
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
				.diskSpaceAvailable(Constants.PHONE_RECORD_LOW_STORAGE_THRESHOLD)) {
			// if (PhoneApp.ISGNPHONE) {
			getInCallScreen().getInCallTouchUi().mRecordButton
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
			mApp.mManageRecord.setUserStop(true);
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
		CallCard mCallCard = getInCallScreen().getCallCard();
		String contacts = mCallCard.getContacts();
		String name = mCallCard.getNameStr();
		String number = mCallCard.getNumberStr();
		PhoneRecorderHandler.getInstance().startVoiceRecord(
				Constants.PHONE_RECORDING_VOICE_CALL_CUSTOM_VALUE, contacts,
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

	private static List<Connection> prevPhonenums = new ArrayList<Connection>();
	private static List<Connection> prevBgPhonenums = new ArrayList<Connection>();

	/**
	 * Compare the current foreground phone numbers with the last one.
	 * 
	 * @return false if different, otherwise true.
	 */
	private boolean comparePhoneNumbers() {
		if (prevPhonenums == null || prevPhonenums.size() == 0) {
			return true;
		}

		List<Connection> fgCalls;

		fgCalls = mCM.getActiveFgCall().getConnections();

		if (prevPhonenums.size() != fgCalls.size()) {
			return false;
		}

		for (int i = 0; i < fgCalls.size(); i++) {
			if (!prevPhonenums.contains(fgCalls.get(i))) {
				return false;
			} else if (prevPhonenums.size() == 1 && !fgCalls.get(i).isAlive()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * If the previous phone numbers is different from the current one. Set it
	 * the current one.
	 */
	private void updatePrevPhonenums() {
		log("-updatePrevPhonenums:update the previous phone number list.");

		List<Connection> fgCalls, bgCalls;

		fgCalls = mCM.getActiveFgCall().getConnections();

		prevPhonenums.clear();
		for (int i = 0; i < fgCalls.size(); i++) {
			prevPhonenums.add(fgCalls.get(i));
		}

		bgCalls = mCM.getBgCallConnections();

		prevBgPhonenums.clear();
		for (int i = 0; i < bgCalls.size(); i++) {
			prevBgPhonenums.add(bgCalls.get(i));
		}

	}

	public void handleRecordProc() {
		PhoneRecorder phoneRecorder = PhoneRecorder.getInstance(mApp);
		if (((prevPhonenums == null || prevPhonenums.size() == 0) && (prevBgPhonenums == null || prevBgPhonenums
				.size() == 0)) || mCM.getState() == PhoneConstants.State.IDLE) {
			log("the record custom value is "
					+ PhoneRecorderHandler.getInstance().getCustomValue());
			if (Constants.PHONE_RECORDING_VOICE_CALL_CUSTOM_VALUE == PhoneRecorderHandler
					.getInstance().getCustomValue()) {
				if (PhoneRecorder.isRecording()) {
					stopRecord();
				}
			}
//			updatePrevPhonenums();
//			return;
		}
		// boolean recordFlag = false;
		// recordFlag = phoneRecorder.ismFlagRecord();
		// boolean isDifferent = false;
		// if (recordFlag) {
		// isDifferent = !comparePhoneNumbers();
		// if (isDifferent) {
		// if (Constants.PHONE_RECORDING_VOICE_CALL_CUSTOM_VALUE ==
		// PhoneRecorderHandler.getInstance().getCustomValue()) {
		// if (PhoneRecorder.isRecording()) {
		// stopRecord();
		// }
		// }
		// }
		// }
		updatePrevPhonenums();
		// This code is added for CALLWAITING state.
		// When in CALLWAITING state, a new call is incoming, even if recording
		// now,
		// need hide recording flash icon, so here needs update
		handleAutoRecord();
	}
	
	private static final int AUTO_RECORD_DISABLE = 0; 
	private static final int AUTO_RECORD_ALL = 1; 
	private static final int AUTO_RECORD_CONTACT = 2; 

	public void handleAutoRecord() {
		Call.State state = mCM.getFirstActiveRingingCall().getState();
        if(state == Call.State.IDLE) {
        	state = mCM.getActiveFgCallState();
        }
		if (state == Call.State.ACTIVE) {
			boolean isAutoRecord = false;
			if (mRecordSelection == AUTO_RECORD_DISABLE) {
				isAutoRecord = false;
			} else if (mRecordSelection == AUTO_RECORD_ALL) {
				for (Connection c : prevPhonenums) {
					// if(c.isIncoming()) {
					isAutoRecord = true;
					// }
				}
			} else {
				for (Connection c : prevPhonenums) {
					log("-handleAutoRecord c.getAddress() = " + c.getAddress());
					if (RecordUtils.mRecordMap.get(c.getAddress()) != null
							&& RecordUtils.mRecordMap.get(c.getAddress())) {
						// && c.isIncoming()) {
						isAutoRecord = true;
//						RecordUtils.mRecordMap.put(c.getAddress(), false);
						break;
					}
				}
			}
			PhoneRecorder phoneRecorder = PhoneRecorder.getInstance(mApp);
			if (!phoneRecorder.ismFlagRecord() && isAutoRecord
					&& !mIsUserStopRecord) {
				log("-handleAutoRecord start");
				mApp.notifier.playRecordTone();
				onRecordClick();
				getInCallScreen().getInCallTouchUi().updateRecordBtnState();
			} else if(mRecordSelection == AUTO_RECORD_CONTACT) {
				if(phoneRecorder.ismFlagRecord() && !isAutoRecord && !mIsUserStopRecord) {
					onRecordClick();
					getInCallScreen().getInCallTouchUi().updateRecordBtnState();
				}
			}
		}
		PhoneConstants.State phonestate = mCM.getState();
		if (phonestate == PhoneConstants.State.IDLE) {
			RecordUtils.mRecordMap.clear();
		}
	}

	// aurora add liguangyu 20140331 for record end

	public void handleHoldClick() {
		if (PhoneRecorder.isRecording()) {
			mIsUserStopRecord = true;
			stopRecord();
			updatePrevPhonenums();
		}
	}

	public void setUserStop(boolean value) {
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

	 private InCallScreen getInCallScreen() {
	    	return  PhoneGlobals.getInCallScreen();
	    }
}
