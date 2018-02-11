/*
 * Copyright (c) 2013 Qualcomm Technologies, Inc.  All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */

package com.qualcomm.listen.voicewakeup.ux10;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.qualcomm.listen.voicewakeup.Global;
import com.qualcomm.listen.voicewakeup.MessageType;
import com.qualcomm.listen.voicewakeup.R;
import com.qualcomm.listen.voicewakeup.VwuService;


public class SettingsActivity extends Activity {
	private final static String TAG = "ListenLog.Settings";

    private Messenger sendToServiceMessenger;

	private EditText uiKeywordThreshold;
	private EditText uiUserThreshold;
	private CheckBox uiListen;
    private CheckBox uiVoicewakeup;
    private CheckBox uiUserVerification;
    private CheckBox uiFailureFeedback;
    private CheckBox uiTone;
	private CheckBox uiVoiceqna;
	private CheckBox uiDetail;
	private RelativeLayout uiLayoutListen;
    private RelativeLayout uiLayoutVoicewakeup;
    private RelativeLayout uiLayoutUserVerification;
    private RelativeLayout uiLayoutFailureFeedback;
	private RelativeLayout uiLayoutTone;
	private RelativeLayout uiLayoutVoiceqna;
	private RelativeLayout uiLayoutDetail;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_settings);

	    Intent intent = new Intent(this, VwuService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	// Connects to the Service
	private ServiceConnection mConnection = new ServiceConnection() {
        // Registers as a client to receive messages.
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.v(TAG, "onServiceConnected");
            sendToServiceMessenger = new Messenger(service);
            // Register clients
            sendReply(MessageType.MSG_REGISTER_CLIENT, null);
            Log.v(TAG, "connected service");
            initializeUserInterface();
        }

        // Unregisters as a client to receive messages.
        public void onServiceDisconnected(ComponentName name) {
            Log.v(TAG, "onServiceDisconnected");
            sendReply(MessageType.MSG_UNREGISTER_CLIENT, null);
            sendToServiceMessenger = null;
            Log.v(TAG, "disconnected service");
        }
    };

    // Handles incoming messages from the Service
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.v(TAG, "handleMessage");
            switch(msg.what) {
                case MessageType.MSG_LISTEN_ENABLED:
                    if (msg.arg1 == MessageType.MSG_LISTEN_GET_PARAM) {
                        Log.v(TAG, "handleMessage: MSG_LISTEN_ENABLED msg received- " +
                        		    "get param returned listen is enabled");
                        uiListen.setChecked(true);
                    } else if (msg.arg1 == Global.SUCCESS) {
                        Log.v(TAG, "handleMessage: MSG_LISTEN_ENABLED msg received with status= " + msg.arg1);
                        showFeatureToast("Listen enable ", msg.arg1);
                        uiListen.setChecked(true);
                    } else if (msg.arg1 == Global.FAILURE) {
                        Log.v(TAG, "handleMessage: MSG_LISTEN_ENABLED msg received with status= " + msg.arg1);
                        showFeatureToast("Listen enable ", msg.arg1);
                    }
                    break;
                case MessageType.MSG_LISTEN_DISABLED:
                    if (msg.arg1 == MessageType.MSG_LISTEN_GET_PARAM) {
                        Log.v(TAG, "handleMessage: MSG_LISTEN_DISABLED msg received- " +
                                    "get param returned listen is disabled");
                        uiListen.setChecked(false);
                    } else if (msg.arg1 == Global.SUCCESS) {
                        Log.v(TAG, "handleMessage: MSG_LISTEN_DISABLED msg received with status= " + msg.arg1);
                        showFeatureToast("Listen disable ", msg.arg1);
                        uiListen.setChecked(false);
                    } else if (msg.arg1 == Global.FAILURE) {
                        Log.v(TAG, "handleMessage: MSG_LISTEN_DISABLED msg received with status= " + msg.arg1);
                        showFeatureToast("Listen disable ", msg.arg1);
                    }
                    break;
                case MessageType.MSG_VOICEWAKEUP_ENABLED:
                    if (msg.arg1 == MessageType.MSG_VOICEWAKEUP_GET_PARAM) {
                        Log.v(TAG, "handleMessage: MSG_VOICEWAKEUP_ENABLED msg received- " +
                                    "get param returned voicewakeup is enabled");
                        uiVoicewakeup.setChecked(true);
                    } else if (msg.arg1 == Global.SUCCESS) {
                        Log.v(TAG, "handleMessage: MSG_VOICEWAKEUP_ENABLED msg received with status= " + msg.arg1);
                        showFeatureToast("VoiceWakeup enable ", msg.arg1);
                        uiVoicewakeup.setChecked(true);
                    } else if (msg.arg1 == Global.FAILURE) {
                        Log.v(TAG, "handleMessage: MSG_VOICEWAKEUP_ENABLED msg received with status= " + msg.arg1);
                        showFeatureToast("VoiceWakeup enable ", msg.arg1);
                    }
                    break;
                case MessageType.MSG_VOICEWAKEUP_DISABLED:
                    if (msg.arg1 == MessageType.MSG_LISTEN_GET_PARAM) {
                        Log.v(TAG, "handleMessage: MSG_VOICEWAKEUP_DISABLED msg received- " +
                                    "get param returned listen is disabled");
                        uiVoicewakeup.setChecked(false);
                    } else if (msg.arg1 == Global.SUCCESS) {
                        Log.v(TAG, "handleMessage: MSG_VOICEWAKEUP_DISABLED msg received with status= " + msg.arg1);
                        showFeatureToast("VoiceWakeup disable ", msg.arg1);
                        uiVoicewakeup.setChecked(false);
                    } else if (msg.arg1 == Global.FAILURE) {
                        Log.v(TAG, "handleMessage: MSG_VOICEWAKEUP_DISABLED msg received with status= " + msg.arg1);
                        showFeatureToast("VoiceWakeup disable ", msg.arg1);
                    }
                    break;

                default:
                   Log.v(TAG, "handleMessage: no such case: " + msg.what);
            }
        }

        private void showFeatureToast(String inFeature, int inStatus) {
            if (Global.SUCCESS == inStatus) {
                Toast.makeText(getApplicationContext(), inFeature + "succeeded", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), inFeature + "failed", Toast.LENGTH_LONG).show();
            }
        }
    };

    private final Messenger mMessenger = new Messenger(mHandler);

    // Sends messages to the Service
    private void sendReply(int what, Object obj) {
        Log.v(TAG, "sendReply");
        if (null == sendToServiceMessenger) {
            return;
        }

        Message msg = Message.obtain(null, what, obj);
        msg.replyTo = mMessenger;
        try {
            sendToServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    // Sends messages with data to the Service
    void sendDataReply(int what, int msgArg1, Integer msgArg2) {
        Log.v(TAG, "sendMessageDataAll");
        if (null == sendToServiceMessenger) {
            return;
        }

        Message msg = Message.obtain(null, what);
        msg.arg1 = msgArg1;
        if (null != msgArg2) {
            msg.arg2 = msgArg2.intValue();
        }
        try {
            sendToServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

	private void initializeUserInterface() {
		uiKeywordThreshold = (EditText)findViewById(R.id.settings_keyword_threshold);
		uiUserThreshold = (EditText)findViewById(R.id.settings_speaker_threshold);

		uiListen = (CheckBox) findViewById(R.id.settings_listen);
        uiVoicewakeup = (CheckBox) findViewById(R.id.settings_voicewakeup);
        uiUserVerification = (CheckBox) findViewById(R.id.settings_userverification);
		uiFailureFeedback = (CheckBox) findViewById(R.id.settings_failurefeedback);
		uiTone = (CheckBox) findViewById(R.id.settings_tone);
		uiVoiceqna = (CheckBox)findViewById(R.id.settings_voiceqna);
		uiDetail = (CheckBox)findViewById(R.id.settings_detail);

		uiLayoutListen = (RelativeLayout)findViewById(R.id.settings_layout_listen);
        uiLayoutVoicewakeup = (RelativeLayout)findViewById(R.id.settings_layout_voicewakeup);
        uiLayoutUserVerification = (RelativeLayout)findViewById(R.id.settings_layout_userverification);
        uiLayoutFailureFeedback = (RelativeLayout)findViewById(R.id.settings_layout_failurefeedback);
		uiLayoutTone = (RelativeLayout)findViewById(R.id.settings_layout_tone);
		uiLayoutVoiceqna = (RelativeLayout)findViewById(R.id.settings_layout_voiceqna);
		uiLayoutDetail = (RelativeLayout)findViewById(R.id.settings_layout_advanced);

		uiKeywordThreshold.setText(Global.getInstance().getKeywordThresholdString());
		uiUserThreshold.setText(Global.getInstance().getUserThresholdString());
		sendReply(MessageType.MSG_LISTEN_GET_PARAM, null);
		sendReply(MessageType.MSG_VOICEWAKEUP_GET_PARAM, null);
        uiUserVerification.setChecked(Global.getInstance().getUserVerification());
		uiFailureFeedback.setChecked(Global.getInstance().getFailureFeedback());
		uiTone.setChecked(Global.getInstance().getTone());
		uiVoiceqna.setChecked(Global.getInstance().getLaunchVoiceqna());
		
		//uiDetail.setChecked(Global.getInstance().getShowAdvancedDetail());

		findViewById(R.id.settings_layout_focus).requestFocus();

		uiLayoutListen.setTag(uiListen);
        uiLayoutVoicewakeup.setTag(uiVoicewakeup);
        uiLayoutUserVerification.setTag(uiUserVerification);
        uiLayoutFailureFeedback.setTag(uiFailureFeedback);
		uiLayoutTone.setTag(uiTone);
		uiLayoutVoiceqna.setTag(uiVoiceqna);
		uiLayoutDetail.setTag(uiDetail);

		uiLayoutListen.setOnClickListener(onClickListener);
        uiLayoutVoicewakeup.setOnClickListener(onClickListener);
        uiLayoutUserVerification.setOnClickListener(onClickListener);
        uiLayoutFailureFeedback.setOnClickListener(onClickListener);
        uiLayoutTone.setOnClickListener(onClickListener);
        uiLayoutVoiceqna.setOnClickListener(onClickListener);
		uiLayoutDetail.setOnClickListener(onClickListener);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();

		Global.getInstance().setKeywordThreshold(uiKeywordThreshold.getText().toString());
		Global.getInstance().setUserThreshold(uiUserThreshold.getText().toString());
		Global.getInstance().setEnableListen(uiListen.isChecked());
		Global.getInstance().setEnableVoiceWakeup(uiVoicewakeup.isChecked());
		Global.getInstance().setUserVerification(uiUserVerification.isChecked());
        Global.getInstance().setFailureFeeback(uiFailureFeedback.isChecked());
        Global.getInstance().setTone(uiTone.isChecked());
        Global.getInstance().setLaunchVoiceqna(uiVoiceqna.isChecked());
        
        //Global.getInstance().setDetail(uiDetail.isChecked());

		Global.getInstance().saveSettingsToSharedPreferences(getApplicationContext());
	}

	// Enables/disables the feature that the user checked/unchecked
	private OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			CheckBox uiCheckbox = (CheckBox)v.getTag();
			boolean checked = !uiCheckbox.isChecked();
			if (uiCheckbox == uiListen){
                Log.v(TAG, "checkbox onclick for uiListen checkbox, checked= " + checked);
                if (checked) {
                    sendDataReply(MessageType.MSG_LISTEN_SET_PARAM, MessageType.MSG_ENABLE, null);
                } else {
                    sendDataReply(MessageType.MSG_LISTEN_SET_PARAM, MessageType.MSG_DISABLE, null);
                }
			} else if (uiCheckbox == uiVoicewakeup) {
                Log.v(TAG, "checkbox onclick for uiVoicewakeup checkbox, checked= " + checked);
                if (checked) {
                    sendDataReply(MessageType.MSG_VOICEWAKEUP_SET_PARAM, MessageType.MSG_ENABLE, null);
                } else {
                    sendDataReply(MessageType.MSG_VOICEWAKEUP_SET_PARAM, MessageType.MSG_DISABLE, null);
                }
		    } else if (uiCheckbox == uiUserVerification || uiCheckbox == uiFailureFeedback) {
                Log.v(TAG, "checkbox onclick for one of the 2 SM-dependent checkboxes checked");
                uiCheckbox.setChecked(checked);
                Toast.makeText(SettingsActivity.this, R.string.settings_toast_smdependentcheckbox,
                        Toast.LENGTH_LONG).show();
		    } else if (uiCheckbox == uiVoiceqna) {
                Log.v(TAG, "checkbox onclick for uiVoiceqna checkbox, checked= " + checked);
                if (checked) {
                    PackageManager pm = getApplicationContext().getPackageManager();
                    Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
                    List<ResolveInfo> receivers = pm.queryIntentActivities(intent, 0);
                    if (receivers.size() > 0) {
                        uiCheckbox.setChecked(checked);
                    } else {
                        Log.e(TAG, "No voice recognition app installed");
                        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                        builder.setTitle(R.string.app_name_ux10)
                            .setMessage(R.string.settings_moreoption_voiceqna_error)
                            .setCancelable(true)
                            .setNegativeButton(R.string.dialog_cancel, null);
                        builder.show();
                    }
                } else {
                    uiCheckbox.setChecked(checked);
                }
            } else {
			    uiCheckbox.setChecked(checked);
			}
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		sendReply(MessageType.MSG_UNREGISTER_CLIENT, null);
        if (null != sendToServiceMessenger) {
            unbindService(mConnection);
        }
	}
}
