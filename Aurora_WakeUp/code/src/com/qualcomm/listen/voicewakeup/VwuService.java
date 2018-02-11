/*
 * Copyright (c) 2013 Qualcomm Technologies, Inc.  All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */

package com.qualcomm.listen.voicewakeup;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.speech.RecognizerIntent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.qualcomm.listen.IListenEventProcessor;
import com.qualcomm.listen.ListenMasterControl;
import com.qualcomm.listen.ListenSoundModel;
import com.qualcomm.listen.ListenTypes;
import com.qualcomm.listen.ListenTypes.DetectionData;
import com.qualcomm.listen.ListenTypes.SoundModelParams;
import com.qualcomm.listen.ListenTypes.VoiceWakeupDetectionData;
import com.qualcomm.listen.ListenVoiceWakeupSession;
import com.qualcomm.listen.voicewakeup.ux10.HomeActivity;

/**
 * This is a hub class. It receives messages from ListenEngine and distribute
 * them to registered clients.
 */
@SuppressWarnings("deprecation")
public class VwuService extends Service implements IListenEventProcessor {

	private final static String TAG = "ListenLog.VwuService";
	private final static String MYTAG = "iht";
	private final static int DISPLAY_ON_DURATION = 5000;
	private final static int VOICEWAKEUP_NOTIFICATION_ID = 1041;
	private final static String UNLOCK = "com.qualcomm.listen.voicewakeup.unlock";
	private final static String SIM_LOCKED = "com.qualcomm.listen.voicewakeup.sim_locked";

	public ArrayList<Messenger> mClients = new ArrayList<Messenger>();
	private NotificationManager notificationManager;
	private MediaPlayer successPlayer;
	private MediaPlayer failurePlayer;
	private Timer wakelockTimer = null;
    
	private PowerManager.WakeLock wakeLock = null; //唤醒锁 点亮屏幕用的
    private KeyguardLock keyguardLock = null;  //键盘锁 解锁键盘用的
	
    private ListenVoiceWakeupSession voiceWakeupSession = null;
	private ListenMasterControl listenMasterControl = null;
	private Context context;


	private static final String IUNI_INTENT_ACTION_VOICEPRINT = "iuni.intent.action.voiceprint";
	private static final String LAUNCHER_PACKAGE_NAME = "com.aurora.launcher";
	private static final String VOICEPRINT_INTENT_EXTRA_KEY_NAME = "status";
	private static final int LAUNCHER_TASK_EXIST = 1;
	private static final int LAUNCHER_TASK_NOT_EXIST = 2;
	
	

	public class LocalBinder extends Binder {
		public VwuService getService() {
			return VwuService.this;
		}
	}

	public IBinder onBind(Intent intent) {
		return receiveMessenger.getBinder();
	}

	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	public Handler receiveHandler = new Handler() {
		public void handleMessage(Message msg) {
			int returnStatus = ListenTypes.STATUS_EFAILURE;
			switch (msg.what) {
			    // Called by the activities that need to communicate with VwuService.java before
			    // initiating communication.
				case MessageType.MSG_REGISTER_CLIENT:
                    Log.v(TAG, "MSG_REGISTER_CLIENT in service");
					mClients.add(msg.replyTo);
					Log.v(TAG, "MSG_REGISTER_CLIENT: mClients size now= " + mClients.size());
					break;

			    // Called by the activities that need to communicate with VwuService.java after
				// communication is no longer needed.
				case MessageType.MSG_UNREGISTER_CLIENT:
                    Log.v(TAG, "MSG_UNREGISTER_CLIENT in service");
					mClients.remove(msg.replyTo);
					break;

				// Called by the HomeActivity when starting as long as the setting wasn't
				// disabled when the app was last closed.
				// Called by the Settings Activity when the Enable/Disable Listen checkbox is
				// checked.
                case MessageType.MSG_LISTEN_SET_PARAM:   //23.27初始化工作
                	
                	checkEnableListen(msg.arg1);
                	
                	/*Log.v(MYTAG, "gain message from HomeAcitity :"+MessageType.MSG_LISTEN_SET_PARAM);  //23*****（MessageType.MSG_ENABLE）
                	//Log.v(MYTAG, "MSG_LISTEN_SET_PARAM");
                    boolean sendError = false;
                    if (Global.SUCCESS != initializeListenMasterControl(MessageType.MSG_LISTEN_SET_PARAM)) {
                        if (Global.getInstance().getLibsError()){
                            break;
                        }
                        sendError = true;
                    }

                    if (msg.arg1 == MessageType.MSG_ENABLE) {
                        if (sendError) {
                            sendMessageDataAll(MessageType.MSG_LISTEN_ENABLED, Global.FAILURE, null);
                            break;
                        }
                        returnStatus = listenMasterControl.setParam(ListenTypes.LISTEN_FEATURE, ListenTypes.ENABLE);
                        if (ListenTypes.STATUS_SUCCESS != returnStatus ) {
                            Log.e(MYTAG, "Home......MSG_LISTEN_SET_PARAM: setParam on LISTEN_FEATURE failed- return= " + returnStatus);
                            sendMessageDataAll(MessageType.MSG_LISTEN_ENABLED, Global.FAILURE, null);
                        } else {
                            Log.v(MYTAG, "Home......MSG_LISTEN_SET_PARAM: enable listen succeeded");
                            sendMessageDataAll(MessageType.MSG_LISTEN_ENABLED, Global.SUCCESS, null);
                        }
                    } else if (msg.arg1 == MessageType.MSG_DISABLE) {
                        if (sendError) {
                            sendMessageDataAll(MessageType.MSG_LISTEN_DISABLED, Global.FAILURE, null);
                            break;
                        }
                        returnStatus = listenMasterControl.setParam(ListenTypes.LISTEN_FEATURE, ListenTypes.DISABLE);
                        if (ListenTypes.STATUS_SUCCESS != returnStatus ) {
                            Log.e(MYTAG, "Home......MSG_LISTEN_SET_PARAM: setParam on LISTEN_FEATURE failed- return= " + returnStatus);
                            sendMessageDataAll(MessageType.MSG_LISTEN_DISABLED, Global.FAILURE, null);
                        } else {
                            Log.v(MYTAG, "Home......MSG_LISTEN_SET_PARAM: disable listen succeeded");
                            sendMessageDataAll(MessageType.MSG_LISTEN_DISABLED, Global.SUCCESS, null);
                        }
                    } else {
                        Log.e(MYTAG, "Home......MSG_LISTEN_SET_PARAM: unknown MessageType= " + msg.arg1);
                    }*/

                    //releaseListenMasterControl();
                    break;

                // Called by the Settings Activity when starting up to determine if the Listen
                // feature is enabled or disabled.
                case MessageType.MSG_LISTEN_GET_PARAM:
                    Log.v(TAG, "MSG_LISTEN_GET_PARAM");
                    // Initialize ListenMaster Control if not already initialized
                    if (Global.SUCCESS != initializeListenMasterControl(MessageType.MSG_LISTEN_GET_PARAM)) {
                        break;
                    }
                    String returnString = listenMasterControl.getParam(ListenTypes.LISTEN_FEATURE);
                    if (returnString.equals("enable")) {
                        Log.v(TAG, "MSG_LISTEN_GET_PARAM: listen is enabled");
                        sendMessageDataAll(MessageType.MSG_LISTEN_ENABLED, MessageType.MSG_LISTEN_GET_PARAM, null);
                    } else if (returnString.equals("disable")) {
                        Log.v(TAG, "MSG_LISTEN_GET_PARAM: listen is disabled");
                        sendMessageDataAll(MessageType.MSG_LISTEN_DISABLED, MessageType.MSG_LISTEN_GET_PARAM, null);
                    } else {
                        Log.e(TAG, "MSG_LISTEN_GET_PARAM: getParam returned unknown string= " + returnString);
                    }

                    //releaseListenMasterControl();
                    break;

                // Called by the HomeActivity when starting as long as the setting wasn't
                // disabled when the app was last closed.
                // Called by the Settings Activity when the Enable/Disable VoiceWakeup checkbox is
                // checked.
                case MessageType.MSG_VOICEWAKEUP_SET_PARAM:
                	
                	voice_wakeup_setParam(msg.arg1);
                	
                	/*Log.v(MYTAG, "Home......gain message from HomeAcitity :"+MessageType.MSG_VOICEWAKEUP_SET_PARAM); //27***
                    sendError = false;
                    if (Global.getInstance().getLibsError()){
                            break;
                    }
                    if (Global.SUCCESS != initializeListenMasterControl(MessageType.MSG_VOICEWAKEUP_GET_PARAM)) {
                        sendError = true;
                    }
                    if (msg.arg1 == MessageType.MSG_ENABLE) {
                        if (sendError) {
                            sendMessageDataAll(MessageType.MSG_VOICEWAKEUP_ENABLED, Global.FAILURE, null);
                            break;
                        }
                        returnStatus = listenMasterControl.setParam(ListenTypes.VOICEWAKEUP_FEATURE,ListenTypes.ENABLE);
                        if (ListenTypes.STATUS_SUCCESS != returnStatus ) {
                            Log.e(MYTAG, "Home......MSG_VOICEWAKEUP_SET_PARAM: setParam on VOICEWAKEUP_FEATURE failed- return= " + returnStatus);
                            sendMessageDataAll(MessageType.MSG_VOICEWAKEUP_ENABLED, Global.FAILURE, null);
                        } else {
                            Log.v(MYTAG, "Home......MSG_VOICEWAKEUP_SET_PARAM: enable voicewakeup succeeded");
                            sendMessageDataAll(MessageType.MSG_VOICEWAKEUP_ENABLED, Global.SUCCESS, null);
                        }
                    } else if (msg.arg1 == MessageType.MSG_DISABLE) {
                        if (sendError) {
                            sendMessageDataAll(MessageType.MSG_VOICEWAKEUP_DISABLED, Global.FAILURE, null);
                            break;
                        }
                        returnStatus = listenMasterControl.setParam(ListenTypes.VOICEWAKEUP_FEATURE,ListenTypes.DISABLE);
                        if (ListenTypes.STATUS_SUCCESS != returnStatus ) {
                            Log.e(MYTAG, "Home......MSG_VOICEWAKEUP_SET_PARAM: setParam on VOICEWAKEUP_FEATURE failed- return= " + returnStatus);
                            sendMessageDataAll(MessageType.MSG_VOICEWAKEUP_DISABLED, Global.FAILURE, null);
                        } else {
                            Log.v(MYTAG, "Home......MSG_VOICEWAKEUP_SET_PARAM: disable voicewakeup succeeded");
                            sendMessageDataAll(MessageType.MSG_VOICEWAKEUP_DISABLED, Global.SUCCESS, null);
                        }
                    } else {
                        Log.e(MYTAG, "Home......MSG_VOICEWAKEUP_SET_PARAM: unknown MessageType= " + msg.arg1);
                    }*/

                    //releaseListenMasterControl();
                    break;

                // Called by the Settings Activity when starting up to determine if the VoiceWakeup
                // feature is enabled or disabled.
                case MessageType.MSG_VOICEWAKEUP_GET_PARAM:
                    Log.v(TAG, "MSG_VOICEWAKEUP_GET_PARAM");
                    if (Global.SUCCESS != initializeListenMasterControl(MessageType.MSG_VOICEWAKEUP_GET_PARAM)) {
                        break;
                    }
                    returnString = listenMasterControl.getParam(ListenTypes.VOICEWAKEUP_FEATURE);
                    if (returnString.equals("enable")) {
                        Log.v(TAG, "MSG_VOICEWAKEUP_GET_PARAM: voicewakeup is enabled");
                        sendMessageDataAll(MessageType.MSG_VOICEWAKEUP_ENABLED,
                                           MessageType.MSG_VOICEWAKEUP_GET_PARAM,
                                           null);
                    } else if (returnString.equals("disable")) {
                        Log.v(TAG, "MSG_VOICEWAKEUP_GET_PARAM: voicewakeup is disabled");
                        sendMessageDataAll(MessageType.MSG_VOICEWAKEUP_DISABLED,
                                           MessageType.MSG_VOICEWAKEUP_GET_PARAM,
                                           null);
                    } else {
                        Log.e(TAG, "MSG_VOICEWAKEUP_GET_PARAM: getParam returned unknown string= " + returnString);
                    }

                    //releaseListenMasterControl();
                    break;

				// Called during training to verify the user's recorded training matched the keyword.
                case MessageType.MSG_VERIFY_RECORDING:
                    Log.v(MYTAG, "MSG_VERIFY_RECORDING......verify the user's sound....");
                    int confidenceLevel = 0;
                    try {
                    	confidenceLevel = ListenSoundModel.verifyUserRecording(Global.getInstance().getKeywordOnlySoundModel(),Global.getInstance().getLastUserRecording());
					} catch (Exception e) {
						e.printStackTrace();
						confidenceLevel = 0;
					}
                    Log.v(MYTAG, "验证客户声音结果：confidenceLevel："+confidenceLevel); //注意该值的关联性
                    if (confidenceLevel == 0) {
                        Log.e(MYTAG, "MSG_VERIFY_RECORDING: verifyUserRecording returned 0 (failure)");
                        //break;
                    }
                    Log.v(MYTAG, "MSG_VERIFY_RECORDING: mClients size now= " + mClients.size());
                    sendMessageDataAll(MessageType.MSG_RECORDING_RESULT, confidenceLevel, null);
                    break;

                // Called after a user completes training to create a sound model with user data
                case MessageType.MSG_EXTEND_SOUNDMODEL:
                    Log.v(MYTAG, "MSG_EXTEND_SOUNDMODEL");
                    new ExtendTask().execute();
                    break;

                // Called by the HomeActivity to register the sound model selected in the
                // SettingsActivity.
				case MessageType.MSG_REGISTER_SOUNDMODEL:  //注册声闻
					
					registerSoundModel();
					
                    /*Log.v(MYTAG, "Home......MSG_REGISTER_SOUNDMODEL in service");
                    if (Global.SUCCESS != openVoiceWakeupSession(MessageType.MSG_REGISTER_SOUNDMODEL)) {
                        break;				//打开声闻对话
                    }

				    SoundModelParams smParams = new SoundModelParams();
				    if (Global.getInstance().getIsASoundModelSelected()) {
				        smParams.soundModelData = Global.getInstance().getSelectedSoundModel();
				    } else {
				        Log.e(MYTAG, "Home......MSG_REGISTER_SOUNDMODEL: registerSoundModel failed because " +"no sound model was selected");
                        sendMessageDataAll(MessageType.MSG_REGISTER_SOUNDMODEL, Global.FAILURE, null);
                        break;
                    }
				    smParams.detectionMode = Global.getInstance().getDetectionMode(); //2_1 
				    smParams.minKeywordConfidence = Short.parseShort(Global.getInstance().getKeywordThresholdString()); //60
				    smParams.minUserConfidence = Short.parseShort(Global.getInstance().getUserThresholdString()); //61
				    smParams.bFailureNotification = Global.getInstance().getFailureFeedback(); //false
				    Log.v(MYTAG, "Home......MSG_REGISTER_SOUNDMODEL: " +
				    		"smp.detectionMode= " + smParams.detectionMode + ", " +
				    		"smp.minKeywordConfidence= " + smParams.minKeywordConfidence + ", " +
				    		"smp.minUserConfidence= " + smParams.minUserConfidence + ", " +
				    		"smp.bFailureNotification= " + smParams.bFailureNotification
				    		);
				    
				    
				    int status = VwuService.this.voiceWakeupSession.registerSoundModel(smParams); //ListenTypes.VOICEWAKEUP_FEATURE_ENABLE_EVENT==4
				    Log.e(MYTAG, "注册声闻的状态status："+status);
				    
				    if (status == ListenTypes.STATUS_SUCCESS) { //如果先有注册，则停止？？
				    	Global.getInstance().setSoundModelInUse(smParams.soundModelData); //注册声闻
	                    sendMessageDataAll(MessageType.MSG_REGISTER_SOUNDMODEL, Global.SUCCESS, null);
				    } else {
				        Log.e(MYTAG, "Home......MSG_REGISTER_SOUNDMODEL: voiceWakeupSession.registerSoundModel(smParams) failed");
                        sendMessageDataAll(MessageType.MSG_REGISTER_SOUNDMODEL, Global.FAILURE, null);
				    }*/
                    break;

                // Called by the HomeActivity to unregister the previously registered sound model.
				case MessageType.MSG_DEREGISTER_SOUNDMODEL:  //11  释放声闻

					deregisterSoundModel();
				    
					break;

				case MessageType.MSG_CLOSE_VWUSESSION:
                    Log.v(MYTAG, "MSG_CLOSE_VWUSESSION in service");
                    closeVoiceWakeupSession();
                    break;

				case MessageType.MSG_TEST_ONE:
                    Log.v(TAG, "MSG_TEST_ONE in service");
                    processEvent(ListenTypes.LISTEN_RUNNING_EVENT, null);
                    break;

				case MessageType.MSG_TEST_TWO:
                    Log.v(TAG, "MSG_TEST_TWO in service");
                    processEvent(ListenTypes.LISTEN_STOPPED_EVENT, null);
                    break;

				default:
					Log.e(TAG, "There is no such Message: " + msg.what);
					break;
			}
		}
	};

	// Initialize ListenMaster Control if not already initialized
    private int initializeListenMasterControl(int msgFeatureStatus) {
        Log.v(MYTAG, "Home......initializeListenMasterControl");
        int returnStatus = Global.SUCCESS;
        if (null == listenMasterControl) {
            try {
                listenMasterControl = ListenMasterControl.getInstance();
            } catch (ExceptionInInitializerError e) {
                e.printStackTrace();
                Log.e(MYTAG, "Home......initializeListenMasterControl: ListenMasterControl.getInstance() failed due" +" to missing/incompatible listenlib(s)");
                
                Global.getInstance().setLibsError(true);
                sendMessageDataAll(MessageType.MSG_LMCGETINSTANCE_FAILED, Global.FAILURE, null);
                
                return Global.FAILURE;
            }
            if (null == listenMasterControl) {
                Log.e(MYTAG, "Home......initializeListenMasterControl: ListenMasterControl.getInstance() failed- " +"returned null");
                sendMessageDataAll(msgFeatureStatus, Global.FAILURE, null);
                return Global.FAILURE;
            } else {
                Log.v(MYTAG, "Home......initializeListenMasterControl: ListenMasterControl.getInstance() succeeded");
                returnStatus = listenMasterControl.setCallback(VwuService.this);
                if (ListenTypes.STATUS_SUCCESS != returnStatus ) {
                    Log.e(MYTAG, "Home......initializeListenMasterControl: listenMasterControl.setCallback() failed- " +"return= " + returnStatus);
                    sendMessageDataAll(msgFeatureStatus, Global.FAILURE, null);
                    return Global.FAILURE;
                } else {
                    Log.v(MYTAG, "Home......initializeListenMasterControl: listenMasterControl.setCallback() succeeded");
                    return Global.SUCCESS;
                }
            }
        } else {
            Log.e(MYTAG, "Home......initializeListenMasterControl: ListenMasterControl is not null " +"and has most likely already been initialized");
            return Global.SUCCESS;
        }
    }

    // Initialize VoiceWakeup Session if not already initialized
    private int openVoiceWakeupSession(int msgFeatureStatus) {
        Log.v(MYTAG, "Home......openVoiceWakeupSession");
        int returnStatus = ListenTypes.STATUS_EFAILURE;

        if (null == voiceWakeupSession) {
            voiceWakeupSession = ListenVoiceWakeupSession.createInstance(); //建立声闻对话
            if (null == voiceWakeupSession) {
                Log.e(MYTAG, "Home......openVoiceWakeupSession: ListenVoiceWakeupSession() failed- returned null");
                sendMessageDataAll(msgFeatureStatus, Global.FAILURE, null); //发送消息至客户端
                return Global.FAILURE;
            } else {
                Log.v(MYTAG, "Home......openVoiceWakeupSession: ListenVoiceWakeupSession.getInstance() succeeded");
                returnStatus = voiceWakeupSession.setCallback(VwuService.this);
                if (ListenTypes.STATUS_SUCCESS != returnStatus ) {
                    Log.e(MYTAG, "Home......openVoiceWakeupSession: voiceWakeupSession.setCallback() failed- " +"return= " + returnStatus);
                    sendMessageDataAll(msgFeatureStatus, Global.FAILURE, null); //发送消息至客户端
                    return Global.FAILURE;
                } else {
                    Log.v(MYTAG, "Home......openVoiceWakeupSession: voiceWakeupSession.setCallback() succeeded");
                    return Global.SUCCESS;
                }
            }
        } else {
            Log.v(MYTAG, "Home......openVoiceWakeupSession: voiceWakeupSession already in progress");
            return Global.SUCCESS;
        }
    }

    // Release ListenMaster Control
    private int releaseListenMasterControl() {
        Log.v(TAG, "releaseListenMasterControl");
        int returnStatus = ListenTypes.STATUS_EFAILURE;
        if (null != listenMasterControl) {
            returnStatus = listenMasterControl.releaseInstance();
            if (returnStatus != ListenTypes.STATUS_SUCCESS) {
                Log.e(TAG, "releaseListenMasterControl: release instance failed- return= " + returnStatus);
                return ListenTypes.STATUS_EFAILURE;
            }
            listenMasterControl = null;
            Log.v(TAG, "releaseListenMasterControl: ListenMasterControl release instance succeeded");
        } else {
            Log.e(TAG, "releaseListenMasterControl: ListenMasterControl instance is null and " +
                       "therefore cannot be released");
            return ListenTypes.STATUS_EFAILURE;
        }
        return ListenTypes.STATUS_SUCCESS;
    }

	// Release the VoiceWakeup Session
    private int closeVoiceWakeupSession() {
        Log.v(MYTAG, "Home....closeVoiceWakeupSession");
        int returnStatus = ListenTypes.STATUS_EFAILURE;
        if (null != voiceWakeupSession) {
            returnStatus = voiceWakeupSession.releaseInstance(); //释放声闻对话
            if (returnStatus != ListenTypes.STATUS_SUCCESS) {
                Log.e(MYTAG, "Home....closeVoiceWakeupSession: voiceWakeupSession release instance failed- return= "+ returnStatus);
                return ListenTypes.STATUS_EFAILURE;
            }
            voiceWakeupSession = null;
            Log.v(MYTAG, "Home....closeVoiceWakeupSession: voiceWakeupSession release instance succeeded");
        } else {
            Log.v(MYTAG, "Home....closeVoiceWakeupSession: no voiceWakeupSession currently in progress");
            return ListenTypes.STATUS_EFAILURE;
        }
        return ListenTypes.STATUS_SUCCESS;
    }

	// A reference for remote Service message receiving
	public final Messenger receiveMessenger = new Messenger(receiveHandler);

	// Send a message with int data to all registered clients
	void sendMessageDataAll(int what, int msgArg1, Integer msgArg2) {
	    Log.v(TAG, "sendMessageDataAll");
	    for (int i = mClients.size() - 1; i >= 0; i--) {
	        try {
	            Log.v(TAG, "sendMessageDataAll: to client= " + i);
	            Message msg = Message.obtain(null, what);
	            msg.arg1 = msgArg1;
	            if (null != msgArg2) {
	                msg.arg2 = msgArg2.intValue();
	            }
	            mClients.get(i).send(msg);
	            Log.v(TAG, "sendMessageDataAll: after send");
	        } catch (RemoteException e) {
                Log.v(TAG, "sendMessageDataAll: removing client= " + i);
	            mClients.remove(i);
	        }
	    }
	}

	public class ExtendTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
            Log.v(MYTAG, "doInBackground......................保存声音文本");
            int size = ListenSoundModel.getSizeWhenExtended(Global.getInstance().getKeywordOnlySoundModel());
            Log.v(MYTAG, "doInBackground......................size:"+size);
            if (size == 0) {
                return ListenTypes.STATUS_EFAILURE;
            }
            Global.getInstance().setExtendedSoundModel(size); //不出意外的话，size = 5;
            int returnStatus = ListenSoundModel.extend(
                    Global.getInstance().getKeywordOnlySoundModel(), //HelloIUNI
                    Global.TRAINING_RECORDINGS_COUNT,                //5
                    Global.getInstance().getUserRecordings(),        //5遍的录音
                    Global.getInstance().getExtendedSoundModel(),    //Global.getInstance().setExtendedSoundModel(size); return extendedSoundModel;
                    Global.getInstance().getConfidenceData());
            return returnStatus; //0：成功； 非0：失败
        }

        @Override
        protected void onPostExecute(Integer returnStatus) {
            Log.v(TAG, "onPostExecute");
            
            Global.getInstance().saveSoundModelToFile(); // 将5个录音的ByteBuffer流，保存成文件
            
            if (returnStatus == ListenTypes.STATUS_SUCCESS) {
                Log.v(MYTAG, "MSG_EXTEND_SOUNDMODEL: succeeded******************************");
                
                Global.getInstance().removeUserRecordings(); //删除原先录制的（5个）声纹文件
                
                sendMessageDataAll(MessageType.MSG_EXTEND_SOUNDMODEL, Global.SUCCESS, null);
            } else {
                Log.e(MYTAG, "MSG_EXTEND_SOUNDMODEL: failed");
                sendMessageDataAll(MessageType.MSG_EXTEND_SOUNDMODEL, Global.FAILURE, null);
            }
        };
    }

	public int onStartCommand(Intent _intent, int flags, int startId) {
		return START_STICKY;
	}

	public void onCreate() {
        //Log.v(TAG, "onCreate");
		
		Log.v(MYTAG, "**********************************VwuService.onCreate()*****************************");
		
		context = getApplicationContext();
		//Global.getInstance().readVoiceWakeUpPreference(context); //获取开机时的启动信息		
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		//gain the enable/disable Listen (miche)  //*****************************
		/*try {
			boolean bool = Utils.readFile("/data/data/com.aurora.voiceassistant/switch.txt", "wakeup");
			Global.getInstance().setEnableListen(bool);
			Global.getInstance().setEnableVoiceWakeup(bool);
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		
		// Get, save, and output version number
		try {
            String versionNumber = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
            Global.getInstance().setVersionNumber(versionNumber);
            //Log.v(TAG, "onCreate: version number= " + Global.getInstance().getVersionNumber());
        } catch (NameNotFoundException e) {
            //Log.v(TAG, "onCreate: error getting version number: " + e.getMessage());
        }

		// Load saved settings
		Global.getInstance().loadSettingsFromSharedPreferences(this);
		//
		Global.getInstance().readVoiceWakeUpPreference(this);
		
		
		// Create tone players for success and failure
		successPlayer = MediaPlayer.create(this, R.raw.succeed);
        Log.v(TAG, "onCreate: successPlayer created");
        Log.v(TAG, "onCreate: try creating failurePlayer");
        failurePlayer = MediaPlayer.create(this, R.raw.fail_m);
        Log.v(TAG, "onCreate: failurePlayer created");

		// Copy sound models to internal storage
		copyAssets();

		Log.v(MYTAG, "启动Service---------------------------------------->user sound model output location= " + Global.APP_PATH);
		new Timer(true).schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(Global.getInstance().getVoiceWakeUpServiceStatus()){
					test();
				}
			}
		}, 200);
	}
	
	//**********************************************************************************************************************************
	//设置之后，默认启动唤醒服务
	private void test(){
        Log.v(MYTAG, "MSG_REGISTER_SOUNDMODEL in service.test()");
        //获得当前保存的“声闻模板”
        Global.getInstance().loadSavedSoundModel(getApplicationContext());
        
        //准备阶段的参数：
        Log.v(MYTAG, "test():Global.getEnableListen()=  " + Global.getInstance().getEnableListen()+ 
        		", Global.getEnableVoiceWakeup= " + Global.getInstance().getEnableVoiceWakeup());
        Log.v(MYTAG, "\n"); //初始化操作
        //23号消息
        if(Global.getInstance().getEnableListen()){
        	checkEnableListen(MessageType.MSG_ENABLE);
        }else{
        	return;
        }
        
        //27号消息
        if(Global.getInstance().getEnableVoiceWakeup()){
        	
        	Log.v(MYTAG, "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++27");
        	
        	voice_wakeup_setParam(MessageType.MSG_ENABLE); //初始化，实例化主监听控制器
        }else{
        	return;
        }
        
        //注册声闻
        registerSoundModel();
	}
	
    //23号消息
    private void checkEnableListen(int flag){
    	/*Log.v(MYTAG, "\n\n");
    	Log.v(MYTAG, "test(23)....checkEnableListen():"+MessageType.MSG_LISTEN_SET_PARAM);  //23*****（MessageType.MSG_ENABLE）
    	int returnStatus = ListenTypes.STATUS_EFAILURE ;
        boolean sendError = false;
        //if (Global.SUCCESS != initializeListenMasterControlTest(MessageType.MSG_LISTEN_SET_PARAM)) {
        if (Global.SUCCESS != initializeListenMasterControl(MessageType.MSG_LISTEN_SET_PARAM)) {
            if (Global.getInstance().getLibsError()){
            	Log.e(MYTAG, "设置参数23失败: Global.getInstance().getLibsError()="+Global.getInstance().getLibsError());
            	return;
            }
            sendError = true;
        }

        if (sendError) {
        	Log.e(MYTAG, "设置参数23失败: sendError="+sendError);
            //sendMessageDataAll(MessageType.MSG_LISTEN_ENABLED, Global.FAILURE, null);
            return;
        }
        returnStatus = listenMasterControl.setParam(ListenTypes.LISTEN_FEATURE, ListenTypes.ENABLE);
        if (ListenTypes.STATUS_SUCCESS != returnStatus ) {
            Log.e(MYTAG, "设置参数23失败: setParam on LISTEN_FEATURE failed- return= " + returnStatus);
            //sendMessageDataAll(MessageType.MSG_LISTEN_ENABLED, Global.FAILURE, null); //发送消息
        } else {
            Log.v(MYTAG, "成功响应23号消息：MSG_LISTEN_SET_PARAM: enable listen succeeded");
            sendMessageDataAll(MessageType.MSG_LISTEN_ENABLED, Global.SUCCESS, null);
        }*/
    	
    	Log.v(MYTAG, "gain message from HomeAcitity :"+MessageType.MSG_LISTEN_SET_PARAM);  //23*****（MessageType.MSG_ENABLE）
    	int returnStatus = ListenTypes.STATUS_EFAILURE ;
        boolean sendError = false;
        if (Global.SUCCESS != initializeListenMasterControl(MessageType.MSG_LISTEN_SET_PARAM)) {
            if (Global.getInstance().getLibsError()){
                return;
            }
            sendError = true;
        }

        if (flag == MessageType.MSG_ENABLE) { //启用
            if (sendError) {
                sendMessageDataAll(MessageType.MSG_LISTEN_ENABLED, Global.FAILURE, null);
                return;
            }
            
            if(listenMasterControl == null){
            	Log.e(MYTAG, "VwuService.............>>:listenMasterControl == null");
            	return;
            }
            returnStatus = listenMasterControl.setParam(ListenTypes.LISTEN_FEATURE, ListenTypes.ENABLE);
            if (ListenTypes.STATUS_SUCCESS != returnStatus ) {
                Log.e(MYTAG, "Home......MSG_LISTEN_SET_PARAM: setParam on LISTEN_FEATURE failed- return= " + returnStatus);
                sendMessageDataAll(MessageType.MSG_LISTEN_ENABLED, Global.FAILURE, null);
            } else {
                Log.v(MYTAG, "Home......MSG_LISTEN_SET_PARAM: enable listen succeeded");
                sendMessageDataAll(MessageType.MSG_LISTEN_ENABLED, Global.SUCCESS, null);
            }
        } else if (flag == MessageType.MSG_DISABLE) { //禁用
            if (sendError) {
                sendMessageDataAll(MessageType.MSG_LISTEN_DISABLED, Global.FAILURE, null);
                return;
            }
            returnStatus = listenMasterControl.setParam(ListenTypes.LISTEN_FEATURE, ListenTypes.DISABLE);
            if (ListenTypes.STATUS_SUCCESS != returnStatus ) {
                Log.e(MYTAG, "Home......MSG_LISTEN_SET_PARAM: setParam on LISTEN_FEATURE failed- return= " + returnStatus);
                sendMessageDataAll(MessageType.MSG_LISTEN_DISABLED, Global.FAILURE, null);
            } else {
                Log.v(MYTAG, "Home......MSG_LISTEN_SET_PARAM: disable listen succeeded");
                sendMessageDataAll(MessageType.MSG_LISTEN_DISABLED, Global.SUCCESS, null);
            }
        } else {
            Log.e(MYTAG, "Home......MSG_LISTEN_SET_PARAM: unknown MessageType= " + flag);
        }
    	
    }
    
    //27号消息
    private void voice_wakeup_setParam(int flag){
    	/*Log.v(MYTAG, "gain message from HomeAcitity :"+MessageType.MSG_VOICEWAKEUP_SET_PARAM); //27***
        boolean sendError = false;
        int returnStatus = ListenTypes.STATUS_EFAILURE;
        if (Global.getInstance().getLibsError()){
                return;
        }
        //if (Global.SUCCESS != initializeListenMasterControlTest(MessageType.MSG_VOICEWAKEUP_GET_PARAM)) {
        if (Global.SUCCESS != initializeListenMasterControl(MessageType.MSG_VOICEWAKEUP_GET_PARAM)) {
            sendError = true;
        }
        if (sendError) {
            //sendMessageDataAll(MessageType.MSG_VOICEWAKEUP_ENABLED, Global.FAILURE, null);
            return;
        }
        returnStatus = listenMasterControl.setParam(ListenTypes.VOICEWAKEUP_FEATURE,ListenTypes.ENABLE);
        if (ListenTypes.STATUS_SUCCESS != returnStatus ) {
            Log.e(MYTAG, "voice_wakeup_setParam()......: setParam on VOICEWAKEUP_FEATURE failed- return= " + returnStatus);
            //sendMessageDataAll(MessageType.MSG_VOICEWAKEUP_ENABLED, Global.FAILURE, null);
        } else {
            Log.v(MYTAG, "voice_wakeup_setParam()......: enable voicewakeup succeeded");
            sendMessageDataAll(MessageType.MSG_VOICEWAKEUP_ENABLED, Global.SUCCESS, null);
        }*/
    	
    	Log.v(MYTAG, "Home......gain message from HomeAcitity :"+MessageType.MSG_VOICEWAKEUP_SET_PARAM); //27***
    	int returnStatus = ListenTypes.STATUS_EFAILURE;
        boolean sendError = false;
        if (Global.getInstance().getLibsError()){
                return;
        }
        if (Global.SUCCESS != initializeListenMasterControl(MessageType.MSG_VOICEWAKEUP_GET_PARAM)) {
            sendError = true;
        }
        if (flag == MessageType.MSG_ENABLE) {
            if (sendError) {
                sendMessageDataAll(MessageType.MSG_VOICEWAKEUP_ENABLED, Global.FAILURE, null);
                return;
            }
            returnStatus = listenMasterControl.setParam(ListenTypes.VOICEWAKEUP_FEATURE,ListenTypes.ENABLE);
            if (ListenTypes.STATUS_SUCCESS != returnStatus ) {
                Log.e(MYTAG, "Home......MSG_VOICEWAKEUP_SET_PARAM: setParam on VOICEWAKEUP_FEATURE failed- return= " + returnStatus);
                sendMessageDataAll(MessageType.MSG_VOICEWAKEUP_ENABLED, Global.FAILURE, null);
            } else {
                Log.v(MYTAG, "Home......MSG_VOICEWAKEUP_SET_PARAM: enable voicewakeup succeeded");
                sendMessageDataAll(MessageType.MSG_VOICEWAKEUP_ENABLED, Global.SUCCESS, null);
            }
        } else if (flag == MessageType.MSG_DISABLE) {
            if (sendError) {
                sendMessageDataAll(MessageType.MSG_VOICEWAKEUP_DISABLED, Global.FAILURE, null);
                return;
            }
            returnStatus = listenMasterControl.setParam(ListenTypes.VOICEWAKEUP_FEATURE,ListenTypes.DISABLE);
            if (ListenTypes.STATUS_SUCCESS != returnStatus ) {
                Log.e(MYTAG, "Home......MSG_VOICEWAKEUP_SET_PARAM: setParam on VOICEWAKEUP_FEATURE failed- return= " + returnStatus);
                sendMessageDataAll(MessageType.MSG_VOICEWAKEUP_DISABLED, Global.FAILURE, null);
            } else {
                Log.v(MYTAG, "Home......MSG_VOICEWAKEUP_SET_PARAM: disable voicewakeup succeeded");
                sendMessageDataAll(MessageType.MSG_VOICEWAKEUP_DISABLED, Global.SUCCESS, null);
            }
        } else {
            Log.e(MYTAG, "Home......MSG_VOICEWAKEUP_SET_PARAM: unknown MessageType= " + flag);
        }
    }
    
    //MSG_REGISTER_SOUNDMODEL
    private void registerSoundModel(){
        Log.v(MYTAG, "Home......MSG_REGISTER_SOUNDMODEL in service");
        if (Global.SUCCESS != openVoiceWakeupSession(MessageType.MSG_REGISTER_SOUNDMODEL)) {
            return;				//打开声闻对话
        }

	    SoundModelParams smParams = new SoundModelParams();
	    if (Global.getInstance().getIsASoundModelSelected()) {
	        smParams.soundModelData = Global.getInstance().getSelectedSoundModel();
	    } else {
	        Log.e(MYTAG, "Home......MSG_REGISTER_SOUNDMODEL: registerSoundModel failed because " +"no sound model was selected"); //没有模板
            sendMessageDataAll(MessageType.MSG_REGISTER_SOUNDMODEL, Global.FAILURE, null); //没有模板--失败
            return;
        }
	    smParams.detectionMode = Global.getInstance().getDetectionMode(); //2_1 
	    smParams.minKeywordConfidence = Short.parseShort(Global.getInstance().getKeywordThresholdString()); //60
	    smParams.minUserConfidence = Short.parseShort(Global.getInstance().getUserThresholdString()); //61
	    smParams.bFailureNotification = Global.getInstance().getFailureFeedback(); //false
	    
	    Log.v(MYTAG, "Home......MSG_REGISTER_SOUNDMODEL: " +
	    		"smp.detectionMode= " + smParams.detectionMode + ", " +
	    		"smp.minKeywordConfidence= " + smParams.minKeywordConfidence + ", " +
	    		"smp.minUserConfidence= " + smParams.minUserConfidence + ", " +
	    		"smp.bFailureNotification= " + smParams.bFailureNotification
	    		);
	    
	    
	    int status = VwuService.this.voiceWakeupSession.registerSoundModel(smParams); //ListenTypes.VOICEWAKEUP_FEATURE_ENABLE_EVENT==4
	    Log.e(MYTAG, "注册声闻的状态status："+status);
	    
	    if (status == ListenTypes.STATUS_SUCCESS) { //如果先有注册，则停止？？
	    	Global.getInstance().setSoundModelInUse(smParams.soundModelData); //注册声闻
            sendMessageDataAll(MessageType.MSG_REGISTER_SOUNDMODEL, Global.SUCCESS, null); // 成功
	    } else {
	        Log.e(MYTAG, "Home......MSG_REGISTER_SOUNDMODEL: voiceWakeupSession.registerSoundModel(smParams) failed");
            sendMessageDataAll(MessageType.MSG_REGISTER_SOUNDMODEL, Global.FAILURE, null);
	    }
    }
    
    private void deregisterSoundModel(){
        Log.v(MYTAG, "Home....MSG_DEREGISTER_SOUNDMODEL in service");
	    if (Global.getInstance().isASoundModelRegistered() &&  null != VwuService.this.voiceWakeupSession) {
	        if (VwuService.this.voiceWakeupSession.deregisterSoundModel() == ListenTypes.STATUS_SUCCESS) {
	        	
	        	Global.getInstance().setSoundModelInUse(null);
	            closeVoiceWakeupSession(); //关闭声闻对话（核心）
	          
	            Log.v(MYTAG, "Home....MSG_DEREGISTER_SOUNDMODEL: deregisterSoundModel succeeded");
	            sendMessageDataAll(MessageType.MSG_DEREGISTER_SOUNDMODEL, Global.SUCCESS, null);
             
	        } else {
             Log.e(MYTAG, "Home....MSG_DEREGISTER_SOUNDMODEL: voiceWakeupSession.deregisterSoundModel() failed");
             sendMessageDataAll(MessageType.MSG_DEREGISTER_SOUNDMODEL, Global.FAILURE, null);
	        }
	    } else {
         Log.e(MYTAG, "Home....MSG_DEREGISTER_SOUNDMODEL: deregister sound model failed, " +"Global.isASoundModelRegistered()= " + Global.getInstance().isASoundModelRegistered());
	        sendMessageDataAll(MessageType.MSG_DEREGISTER_SOUNDMODEL, Global.FAILURE, null);
	    }
    }
    //**********************************************************************************************************************************

	public void onDestroy() {
		Log.v(TAG, "ondestroy");
		
		/*if(Global.getInstance().isASoundModelRegistered()){
			deregisterSoundModel();
		}*/
		
		releaseListenMasterControl();
		closeVoiceWakeupSession();
		notificationManager.cancelAll();
		
		/*
		Global.getInstance().unselectAndUnsaveSoundModel(this); //释放声纹模板
		Global.getInstance().writeVoiceWakeUpPreference(this, false);
		*/
		
		successPlayer.release();
		failurePlayer.release();
		
		Log.v(MYTAG, "sssssssssssssssssssssssssservice-------------这个服务被结束拉！！");
		super.onDestroy();
	}

	private void launchVoiceQna() {
	    Log.v(TAG, "launchVoiceQna");
		int defaultFlag = Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP
		                      | Intent.FLAG_ACTIVITY_NEW_TASK;
        Log.v(TAG, "launchVoiceQna: inside if statement");
    	Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
        Log.v(TAG, "launchVoiceQna: intent created");
		intent.setFlags(defaultFlag);
        Log.v(TAG, "launchVoiceQna: flags set");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "launchVoiceQna: No voice recognition app installed");
        }
        Log.v(TAG, "launchVoiceQna: intent launched");
	}

	// Turns off the display after DISPLAY_ON_LENGTH milliseconds
	private void startWakelockTimer() {
	    Log.v(TAG, "startWakelockTimer");
	    stopWakelockTimer();
	    wakelockTimer = new Timer();
	    wakelockTimer.schedule(new TimerTask() {
	        @Override
	        public void run() {
	            Log.v(TAG, "startWakelockTimer: run()- release wakelock");
	            stopWakelockTimer();
	            if (null != wakeLock) {
	                if (wakeLock.isHeld()) {
	                    wakeLock.release();
	                }
                    wakeLock = null;
                }
	        }
	    }, DISPLAY_ON_DURATION);
	}

	private void stopWakelockTimer() {
	    if (null != wakelockTimer) {
	        wakelockTimer.cancel();
	        wakelockTimer = null;
	    }
	}

	// Copy sound models to device's internal storage
	private void copyAssets() {
		final File oldFilePath = new File(Global.OLD_APP_PATH);
		Log.v(MYTAG, "************************copyAssets*******Global.OLD_APP_PATH-----exists() = "+oldFilePath.exists());
		if (oldFilePath.exists()) {
			Utils.copyExistedFileToNewPath(Global.OLD_APP_PATH, Global.APP_PATH);
			Log.v(MYTAG, "********************copyAssets*******Global.OLD_APP_PATH-----exists()-----copy file end!!!!");
		}

		Utils.copyAssetsToStorage(this, Global.APP_PATH);
	}

	// Show the icon in the notification bar that Listen is enabled
	private void showNotification(int icon, String status) {
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, null, when);
		Intent intent = new Intent(this, HomeActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

		CharSequence contentTitle = "Voice Activation";
		notification.setLatestEventInfo(context, contentTitle, status, contentIntent);

        notificationManager.notify(VOICEWAKEUP_NOTIFICATION_ID, notification);
    }

	// Callback method from APIs（***********点亮屏幕-->解锁*************）
	@SuppressLint("Wakelock")
    @Override
    public void processEvent(int eventType, ListenTypes.EventData eventData) {
	    Log.v(TAG, "processEvent of type " + eventType + " size = " + eventData.size);
	    DetectionData detectionData = null;
        int keywordOnlyConfidenceLevel;
        int userConfidenceLevel;
        switch (eventType) {
            /** Event notifying VoiceWakeup detection was successful - minimum keyword and
                user confidence levels were met */
            case ListenTypes.DETECT_SUCCESS_EVENT: //声音检测成功-->解锁
                Log.i(TAG, "processEvent: DETECT_SUCCESS_EVENT");
                detectionData = ListenSoundModel.parseDetectionEventData(Global.getInstance().getSoundModelInUse(), eventData);
                Log.v(TAG, "processEvent: DETECT_SUCCESS_EVENT- parseDetectionEventData returned " + detectionData);
                if (null != detectionData && null != detectionData.type) {
                   if (!detectionData.type.equals("VoiceWakeup_DetectionData_v0100")) {
                       Log.e(TAG, "Unknown DetectionData Type " + detectionData.type);
                       break;
                   }
                   Log.v(TAG, "processEvent: DETECT_SUCCESS_EVENT- parseDetectionEventData returned:");
                   Log.v(TAG, "   DetectionData status = " + detectionData.status);
                   Log.v(TAG, "   DetectionData type = " + detectionData.type);
                   VoiceWakeupDetectionData vwuDetectionData = (VoiceWakeupDetectionData)detectionData;
                   Log.v(TAG, "detectionData successfully cast to VoiceWakeupDetectionData type");
                   if (null == vwuDetectionData.keyword) {
                       Log.e(TAG, "vwuDetectionData keyword is null");
                       break;
                   }
                   Log.v(TAG, "   vwuDetectionData keyword = " + vwuDetectionData.keyword);
                   Log.v(TAG, "   vwuDetectionData keywordConfidenceLevel = "+ vwuDetectionData.keywordConfidenceLevel);
                   Log.v(TAG, "   vwuDetectionData userConfidenceLevel = "+ vwuDetectionData.userConfidenceLevel);
                   keywordOnlyConfidenceLevel = vwuDetectionData.keywordConfidenceLevel;
                   userConfidenceLevel = vwuDetectionData.userConfidenceLevel;
                   
                   if (Global.getInstance().getLaunchVoiceqna()) {//目前默认关闭 
                       launchVoiceQna();
                   }
                   
                   if (Global.getInstance().getTone()) {
                       successPlayer.start();
                   }
                   sendMessageDataAll(MessageType.MSG_DETECT_SUCCEEDED,keywordOnlyConfidenceLevel,userConfidenceLevel);

                   
                   // Get the wakelock to be able to turn on the display
                   /*if (null != wakeLock) {
                       wakeLock.release();
                       wakeLock = null;
                   }
                   PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                   //（保持CPU 运转，允许保持屏幕显示但有可能是灰的，允许关闭键盘灯 | 它使WalkLock不再依赖组件就可以点亮屏幕了）
                   wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "voicewakeup_lock");
                   wakeLock.acquire(); //在屏幕休眠的状态下唤醒屏幕
                   startWakelockTimer();*/
                   
                   

                   // Get the keyguardLock so no lock screen is shown when a detection occurs
                   
                   /*if (null != keyguardLock) {
                       keyguardLock.reenableKeyguard(); //启用
                       keyguardLock = null;
                   }
                   
                   KeyguardManager keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
                   keyguardLock =  keyguardManager.newKeyguardLock(TAG);
                   keyguardLock.disableKeyguard();*/ //解锁键盘
             
                   //startLauncher(keyguardManager); 
                  
                   TelephonyManager teleMng = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                   int simState = teleMng.getSimState();
                   if(simState == TelephonyManager.SIM_STATE_PIN_REQUIRED || simState == TelephonyManager.SIM_STATE_PUK_REQUIRED){
                	   //发送消息亮屏幕
                       Intent intent_unlock = new Intent(SIM_LOCKED); //private final static String SIM_LOCKED = "com.qualcomm.listen.voicewakeup.sim_locked";
                       sendBroadcast(intent_unlock);
                       
                       //Log.v("sim", "SIM卡被锁来..........");
                	   
                   }else{
                       //发送解锁广播com.qualcomm.listen.voicewakeup.unlock
                       Intent intent_unlock = new Intent(UNLOCK);
                       sendBroadcast(intent_unlock);
                       
                       //启动Activity
                       startLauncher();
                   }

                } else {
                    Log.e(TAG, "DetectionData or DetectionData.type is null");
                }
                break;

            /** Event notifying VoiceWakeup detection have failed. This
             *  event is returned only when special notify-all mode is
             *  enabled
             */
            case ListenTypes.DETECT_FAILED_EVENT:
                Log.i(TAG, "processEvent: DETECT_FAILED_EVENT");
                detectionData = null;
                detectionData = ListenSoundModel.parseDetectionEventData(
                                Global.getInstance().getSoundModelInUse(), eventData);
                Log.v(TAG, "processEvent: DETECT_FAILED_EVENT- parseDetectionEventData returned " + detectionData);
                if (null != detectionData && null != detectionData.type) {
                    if (!detectionData.type.equals("VoiceWakeup_DetectionData_v0100")) {
                        Log.e(TAG, "Unknown DetectionData Type " + detectionData.type);
                        break;
                    }
                    Log.v(TAG, "processEvent: DETECT_FAILED_EVENT- parseDetectionEventData returned:");
                    Log.v(TAG, "   DetectionData status = " + detectionData.status);
                    Log.v(TAG, "   DetectionData type = " + detectionData.type);
                    VoiceWakeupDetectionData vwuDetectionData = (VoiceWakeupDetectionData)detectionData;
                    Log.v(TAG, "detectionData successfully cast to VoiceWakeupDetectionData type");
                    if (null == vwuDetectionData.keyword) {
                        Log.e(TAG, "vwuDetectionData keyword is null");
                        break;
                    }
                    Log.v(TAG, "   vwuDetectionData keyword = " + vwuDetectionData.keyword);
                    Log.v(TAG, "   vwuDetectionData keywordConfidenceLevel = " + vwuDetectionData.keywordConfidenceLevel);
                    Log.v(TAG, "   vwuDetectionData userConfidenceLevel = " + vwuDetectionData.userConfidenceLevel);
                    keywordOnlyConfidenceLevel = vwuDetectionData.keywordConfidenceLevel;
                    userConfidenceLevel = vwuDetectionData.userConfidenceLevel;
                    if (Global.getInstance().getTone()) {
                        try {
                           Log.v(TAG, "DETECT_FAILED_EVENT: play failure tone");
                           failurePlayer.start();
                        } catch (NullPointerException e) {
                            Log.e(TAG, "DETECT_FAILED_EVENT: failurePlayer was null e= " + e.getMessage());
                        }
                    }
                    sendMessageDataAll(MessageType.MSG_DETECT_FAILED,
                           keywordOnlyConfidenceLevel,
                           userConfidenceLevel);
                } else {
                    Log.e(TAG, "DetectionData or DetectionData.type is null");
                }
                break;

            /** SoundModel deregistered implicitly by ListenEngine */
            case ListenTypes.DEREGISTERED_EVENT:
                Log.v(TAG, "processEvent: DEREGISTERED_EVENT- sound model was implicitly deregistered by ListenEngine");
                Global.getInstance().setSoundModelInUse(null);
                // Signals to all registered clients that listening to
                // the SoundModel has been stopped.
                sendMessageDataAll(MessageType.MSG_DEREGISTER_SOUNDMODEL, Global.SUCCESS, null);
                break;

            /** Event notifying that VoiceWakeup feature is enabled  */
            case ListenTypes.VOICEWAKEUP_FEATURE_ENABLE_EVENT:
                Log.v(TAG, "processEvent: VOICEWAKEUP_FEATURE_ENABLE_EVENT");
                break;

            /** Event notifying that Listen feature is enabled  */
            case ListenTypes.LISTEN_FEATURE_ENABLE_EVENT:
                Log.v(TAG, "processEvent: LISTEN_FEATURE_ENABLE_EVENT");
                break;

            /** Event notifying that Listen feature is disabled  */
            case ListenTypes.LISTEN_FEATURE_DISABLE_EVENT:
                Log.v(TAG, "processEvent: LISTEN_FEATURE_DISABLE_EVENT");
                break;

            /** Event notifying that VoiceWakeup feature is disabled  */
            case ListenTypes.VOICEWAKEUP_FEATURE_DISABLE_EVENT:
                Log.v(TAG, "processEvent: VOICEWAKEUP_FEATURE_DISABLE_EVENT");
                break;

            /** Event notifying that Listen detection is active  */
            case ListenTypes.LISTEN_RUNNING_EVENT: //显示内容在通知栏
                Log.v(TAG, "processEvent: LISTEN_RUNNING_EVENT");
                // Display the Voice Activation icon in notification bar
                
                //********************************最后解决********************************************************
                
                //showNotification(R.drawable.logo_status, "Voice Activation is running");
                //sendMessageDataAll(MessageType.MSG_LISTEN_RUNNING, Global.SUCCESS, null);
                
                break;

            /** Event notifying that Listen detection is not active  */
            case ListenTypes.LISTEN_STOPPED_EVENT:
                Log.v(TAG, "processEvent: LISTEN_STOPPED_EVENT");
                // Remove the Voice Activation icon from the notification bar
                notificationManager.cancelAll();
                sendMessageDataAll(MessageType.MSG_LISTEN_STOPPED, Global.SUCCESS, null);
                break;

            default:
                Log.v(TAG, "processEvent: event type unknown");
        }
    }
	
	/**
	 * started iuni Launcher that given specific action
	 * */
	//private void startLauncher(KeyguardManager km) {
	private void startLauncher() {
		receiveHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				try {
					Intent it = new Intent();
					it.addCategory(Intent.CATEGORY_HOME);
					it.setAction(IUNI_INTENT_ACTION_VOICEPRINT);
					it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					if (Utils.isAppRunning(getBaseContext(),
							LAUNCHER_PACKAGE_NAME)) {
						it.putExtra(VOICEPRINT_INTENT_EXTRA_KEY_NAME,
								LAUNCHER_TASK_EXIST);
					} else {
						it.putExtra(VOICEPRINT_INTENT_EXTRA_KEY_NAME,
								LAUNCHER_TASK_NOT_EXIST);
					}
					startActivity(it);
				} catch (ActivityNotFoundException e) {
					// TODO: handle exception
					Log.e("linp","-----[VwuService.java(Voiceprint) ]startLauncher but cause ActivityNoFound Exception!\n");
				}
			}
		}, 0);		
	}
}
