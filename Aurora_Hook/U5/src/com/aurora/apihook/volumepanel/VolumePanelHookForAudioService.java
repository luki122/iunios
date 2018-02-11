package com.aurora.apihook.volumepanel;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Stack;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.AudioService;
import android.media.AudioSystem;
import android.media.AudioService.VolumeStreamState;
import android.media.RemoteControlClient;
import android.os.Binder;
import android.os.Handler;
import android.os.UserHandle;
import android.util.Log;
import android.app.AppOpsManager;
import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.Hook;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;


public class VolumePanelHookForAudioService implements Hook{
	
	private AuroraVolumePanel mVolumePanel;
	private Context mContext;
	public static final int FLAG_FIXED_VOLUME = 1 << 5;
	
	
	   private static final int SENDMSG_REPLACE = 0;
	   private final int mSafeMediaVolumeDevices = AudioSystem.DEVICE_OUT_WIRED_HEADSET |
               AudioSystem.DEVICE_OUT_WIRED_HEADPHONE;
	private final int SAFE_MEDIA_VOLUME_INACTIVE = 2;
	private static final int UNSAFE_VOLUME_MUSIC_ACTIVE_MS_MAX = (20 * 3600 * 1000); // 20
																						// hours
	private static final int MUSIC_ACTIVE_POLL_PERIOD_MS = 60000; // 1 minute
																	// polling
																	// interval
	
	private static final int SENDMSG_QUEUE = 2;
	private static final int MSG_SET_DEVICE_VOLUME = 0;
	private static final int MSG_PERSIST_VOLUME = 1;
	private static final int MSG_PERSIST_MASTER_VOLUME = 2;
	private static final int MSG_PERSIST_RINGER_MODE = 3;
	private static final int MSG_MEDIA_SERVER_DIED = 4;
	private static final int MSG_MEDIA_SERVER_STARTED = 5;
	private static final int MSG_PLAY_SOUND_EFFECT = 6;
	private static final int MSG_BTA2DP_DOCK_TIMEOUT = 7;
	private static final int MSG_LOAD_SOUND_EFFECTS = 8;
	private static final int MSG_SET_FORCE_USE = 9;
	private static final int MSG_PERSIST_MEDIABUTTONRECEIVER = 10;
	private static final int MSG_BT_HEADSET_CNCT_FAILED = 11;
	private static final int MSG_RCDISPLAY_CLEAR = 12;
	private static final int MSG_RCDISPLAY_UPDATE = 13;
	private static final int MSG_SET_ALL_VOLUMES = 14;
	private static final int MSG_PERSIST_MASTER_VOLUME_MUTE = 15;
	private static final int MSG_REPORT_NEW_ROUTES = 16;
	private static final int MSG_REEVALUATE_REMOTE = 17;
	private static final int MSG_RCC_NEW_PLAYBACK_INFO = 18;
	private static final int MSG_RCC_NEW_VOLUME_OBS = 19;
	private static final int MSG_SET_FORCE_BT_A2DP_USE = 20;
	// start of messages handled under wakelock
	// these messages can only be queued, i.e. sent with
	// queueMsgUnderWakeLock(),
	// and not with sendMsg(..., ..., SENDMSG_QUEUE, ...)
	private static final int MSG_SET_WIRED_DEVICE_CONNECTION_STATE = 21;
	private static final int MSG_SET_A2DP_CONNECTION_STATE = 22;
	// end of messages handled under wakelock
	private static final int MSG_SET_RSX_CONNECTION_STATE = 23; // change remote
																// submix
																// connection
	private static final int MSG_BROADCAST_AUDIO_BECOMING_NOISY = 25;
	private static final int MSG_CONFIGURE_SAFE_MEDIA_VOLUME = 26;
	private static final int MSG_CONFIGURE_SAFE_MEDIA_VOLUME_FORCED = 27;

	// flags for MSG_PERSIST_VOLUME indicating if current and/or last audible
	// volume should be
	// persisted
	private static final int PERSIST_CURRENT = 0x1;
	private static final int PERSIST_LAST_AUDIBLE = 0x2;

	private static final int BTA2DP_DOCK_TIMEOUT_MILLIS = 8000;
	// Timeout for connection to bluetooth headset service
	private static final int BT_HEADSET_CNCT_TIMEOUT_MS = 3000;

	private static final int MSG_CHECK_MUSIC_ACTIVE = 24;
	
	public static final String EXTRA_VOLUME_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE";
    private static final int PERSIST_DELAY = 500;
	
	 public static final String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";
	 
	   public static final String EXTRA_VOLUME_STREAM_VALUE =
		        "android.media.EXTRA_VOLUME_STREAM_VALUE";
	   
	   
	   public static final String EXTRA_PREV_VOLUME_STREAM_VALUE =
		        "android.media.EXTRA_PREV_VOLUME_STREAM_VALUE";
	   
	   
	   public static final String MASTER_VOLUME_CHANGED_ACTION =
		        "android.media.MASTER_VOLUME_CHANGED_ACTION";
	   
	   public static final String EXTRA_PREV_MASTER_VOLUME_VALUE =
		        "android.media.EXTRA_PREV_MASTER_VOLUME_VALUE";
	   
	   public static final String EXTRA_MASTER_VOLUME_VALUE =
		        "android.media.EXTRA_MASTER_VOLUME_VALUE";
	   
	   
	/**
	 * Context
	 * @param param
	 */
	public void after_AudioService(MethodHookParam param){
		AudioService obj = (AudioService) param.thisObject;
		 
		mContext = (Context) ClassHelper.getObjectField(obj, "mContext");
		 mVolumePanel	= new AuroraVolumePanel(mContext, obj);
	}
	
	/**
	 * int streamType, int oldIndex, int index, int flags
	 */
	public void before_sendVolumeUpdate(MethodHookParam param){
		param.setResult(null);
		Object obj = param.thisObject;
		int streamType = (Integer) param.args[0];
		int oldIndex = (Integer) param.args[1];
		int index = (Integer) param.args[2];
		int flags =  (Integer) param.args[3];
		
		boolean mVoiceCapable = ClassHelper.getBooleanField(obj, "mVoiceCapable");
		 if (!mVoiceCapable && (streamType == AudioSystem.STREAM_RING)) {
	            streamType = AudioSystem.STREAM_NOTIFICATION;
	        }

		 if(mVolumePanel != null){
			 mVolumePanel.postVolumeChanged(streamType, flags);
			 Log.e("vo", "postVolumeChanged 1");
		 }
	       
	        if ((flags & AudioManager.FLAG_FIXED_VOLUME) == 0) {
	            oldIndex = (oldIndex + 5) / 10;
	            index = (index + 5) / 10;
	            Intent intent = new Intent(AudioManager.VOLUME_CHANGED_ACTION);
	            intent.putExtra(AudioManager.EXTRA_VOLUME_STREAM_TYPE, streamType);
	            intent.putExtra(AudioManager.EXTRA_VOLUME_STREAM_VALUE, index);
	            intent.putExtra(AudioManager.EXTRA_PREV_VOLUME_STREAM_VALUE, oldIndex);
	            Log.e("vo", "postVolumeChanged 2");
//	            ClassHelper.callMethod(obj, "sendBroadcastToAll", intent);
	            sendBroadcastToAll(intent,mContext);
	        }
	}
	
	  protected void sendBroadcastToAll(Intent intent,Context context) {
	        final long ident = Binder.clearCallingIdentity();
	        try {
	        	context.sendBroadcastAsUser(intent, UserHandle.ALL);
	        } finally {
	            Binder.restoreCallingIdentity(ident);
	        }
	    }
	
	/**
	 * int flags, int oldVolume, int newVolume
	 */
	public void before_sendMasterVolumeUpdate(MethodHookParam param){
		param.setResult(null);
		int flags = (Integer) param.args[0];
		int oldVolume = (Integer) param.args[1];
		int newVolume = (Integer) param.args[2];
		
		if(mVolumePanel != null){
		    mVolumePanel.postMasterVolumeChanged(flags);
		}

	        Intent intent = new Intent(MASTER_VOLUME_CHANGED_ACTION);
	        intent.putExtra(EXTRA_PREV_MASTER_VOLUME_VALUE, oldVolume);
	        intent.putExtra(EXTRA_MASTER_VOLUME_VALUE, newVolume);
//	        ClassHelper.callMethod(param.thisObject, "sendBroadcastToAll", intent);
//	        sendBroadcastToAll(intent);
            sendBroadcastToAll(intent,mContext);
	}
	
	/**
	 * boolean muted, int flags
	 */
	public void before_sendMasterMuteUpdate(MethodHookParam param){
		boolean muted = (Boolean) param.args[0];
		int flags = (Integer) param.args[1];
		param.setResult(null);
		if(mVolumePanel != null){
		mVolumePanel.postMasterMuteChanged(flags);
		}
		ClassHelper.callMethod(isHuawei(param.thisObject),param.thisObject, "broadcastMasterMuteStatus", muted);
//        broadcastMasterMuteStatus(muted);
//		 Context mContext = (Context) ClassHelper.getObjectField(param.thisObject, "mContext");
//         sendBroadcastToAll(intent,mContext);
	}
	
	  private boolean isHuawei(Object service){
		  String clzName = service.getClass().getName();
		  String originalName = "android.media.AudioService";
		  return !originalName.equals(clzName);
	   }
	
	public void before_onCheckMusicActive(MethodHookParam param){
		param.setResult(null);
		Object obj = param.thisObject;
		Integer mSafeMediaVolumeState = (Integer) ClassHelper.getObjectField(param.thisObject, "mSafeMediaVolumeState");
		synchronized (mSafeMediaVolumeState) {
            if (mSafeMediaVolumeState == SAFE_MEDIA_VOLUME_INACTIVE) {
                int device = (Integer) ClassHelper.callMethod(isHuawei(obj),obj, "getDeviceForStream", 
                		AudioSystem.STREAM_MUSIC);

                
                if ((device & mSafeMediaVolumeDevices) != 0) {
                	 Handler mAudioHandler = (Handler) ClassHelper.getObjectField(param.thisObject, "mAudioHandler");
                  
                	 ClassHelper.callMethod(isHuawei(obj),obj,"sendMsg",mAudioHandler,
                             MSG_CHECK_MUSIC_ACTIVE,
                             SENDMSG_REPLACE,
                             0,
                             0,
                             null,
                             MUSIC_ACTIVE_POLL_PERIOD_MS);
                	 
//                	 sendMsg(mAudioHandler,
//                            MSG_CHECK_MUSIC_ACTIVE,
//                            SENDMSG_REPLACE,
//                            0,
//                            0,
//                            null,
//                            MUSIC_ACTIVE_POLL_PERIOD_MS);
                	 Object[] mStreamStates = (Object[]) ClassHelper.getObjectField(obj, "mStreamStates");
                	 Object state = mStreamStates[AudioSystem.STREAM_MUSIC];
                	 
                	 int index = (Integer) ClassHelper.callMethod(state, "getIndex", device,
                             false /*lastAudible*/);
//                	 mStreamStates[AudioSystem.STREAM_MUSIC].getIndex(device,
//                                                                            false /*lastAudible*/);
                	 int mSafeMediaVolumeIndex = ClassHelper.getIntField(obj, "mSafeMediaVolumeIndex");
                    if (AudioSystem.isStreamActive(AudioSystem.STREAM_MUSIC, 0) &&
                            (index > mSafeMediaVolumeIndex)) {
                        // Approximate cumulative active music time
                    	int mMusicActiveMs = ClassHelper.getIntField(obj, "mMusicActiveMs");
                        mMusicActiveMs += MUSIC_ACTIVE_POLL_PERIOD_MS;
                        ClassHelper.setIntField(obj, "mMusicActiveMs", mMusicActiveMs);
                        if (mMusicActiveMs > UNSAFE_VOLUME_MUSIC_ACTIVE_MS_MAX) {
                        	ClassHelper.callMethod(isHuawei(obj),obj, "setSafeMediaVolumeEnabled", true);
//                            setSafeMediaVolumeEnabled(true);
                            mMusicActiveMs = 0;

                            if(mVolumePanel != null){
                            mVolumePanel.postDisplaySafeVolumeWarning();
                            }
                        }
                    }
                }
            }
        }
	}
	
	/**
	 * int rccId, int key, int value
	 */
	
	public void before_setStreamVolume(MethodHookParam param){
		param.setResult(null);
		 Object obj = param.thisObject;
		 int streamType=(Integer) param.args[0];
		 int index=(Integer) param.args[1];
		 int flags=(Integer) param.args[2];
		 String callingPackage=(String) param.args[3];
		 boolean mUseFixedVolume = ClassHelper.getBooleanField(obj, "mUseFixedVolume");
		 if (mUseFixedVolume) {
	            return;
	        }
		 ClassHelper.callMethod(isHuawei(obj),obj, "ensureValidStreamType", streamType);
//	        ensureValidStreamType(streamType);
		 int[] mStreamVolumeAlias = (int[]) ClassHelper.getObjectField(obj, "mStreamVolumeAlias");
		 VolumeStreamState[] mStreamStates = (VolumeStreamState[]) ClassHelper.getObjectField(obj, "mStreamStates");
	        int streamTypeAlias = mStreamVolumeAlias[streamType];
	        VolumeStreamState streamState = mStreamStates[streamTypeAlias];

	        final int device = (Integer)ClassHelper.callMethod(isHuawei(obj),obj, "getDeviceForStream", streamType);//getDeviceForStream(streamType);
	        int oldIndex;

	        // skip a2dp absolute volume control request when the device
	        // is not an a2dp device
	        if ((device & AudioSystem.DEVICE_OUT_ALL_A2DP) == 0 &&
	            (flags & AudioManager.FLAG_BLUETOOTH_ABS_VOLUME) != 0) {
	            return;
	        }

	        AppOpsManager mAppOps = (AppOpsManager) ClassHelper.getObjectField(obj, "mAppOps");
	       
	        if (mAppOps.noteOp(STEAM_VOLUME_OPS[streamTypeAlias], Binder.getCallingUid(),
	                callingPackage) != AppOpsManager.MODE_ALLOWED) {
	            return;
	        }
	        Integer mSafeMediaVolumeState = (Integer) ClassHelper.getObjectField(param.thisObject, "mSafeMediaVolumeState");
	        synchronized (mSafeMediaVolumeState) {
	            // reset any pending volume command
//	            mPendingVolumeCommand = null;
	            ClassHelper.setObjectField(obj, "mPendingVolumeCommand", null);
	            oldIndex = (Integer)ClassHelper.callMethod(streamState, "getIndex", device);//streamState.getIndex(device);

	            index = (Integer)ClassHelper.callMethod(isHuawei(obj),obj, "rescaleIndex", index * 10, streamType, streamTypeAlias);
//	            index = rescaleIndex(index * 10, streamType, streamTypeAlias);

	            if (streamTypeAlias == AudioSystem.STREAM_MUSIC &&
	                (device & AudioSystem.DEVICE_OUT_ALL_A2DP) != 0 &&
	                (flags & AudioManager.FLAG_BLUETOOTH_ABS_VOLUME) == 0) {
	            	Object mA2dpAvrcpLock = ClassHelper.getObjectField(obj, "mA2dpAvrcpLock");
	            	Object mA2dp= ClassHelper.getObjectField(obj, "mA2dp");
	            	Boolean mAvrcpAbsVolSupported = ClassHelper.getBooleanField(obj, "mAvrcpAbsVolSupported");
	                synchronized (mA2dpAvrcpLock) {
	                    if (mA2dp != null && mAvrcpAbsVolSupported) {
	                    	ClassHelper.callMethod(mA2dp, "setAvrcpAbsoluteVolume", index / 10);
//	                        mA2dp.setAvrcpAbsoluteVolume(index / 10);
	                    }
	                }
	            }

	            flags &= ~AudioManager.FLAG_FIXED_VOLUME;
	            if ((streamTypeAlias == AudioSystem.STREAM_MUSIC) &&
	                    ((device & mFixedVolumeDevices) != 0)) {
	                flags |= AudioManager.FLAG_FIXED_VOLUME;

	                // volume is either 0 or max allowed for fixed volume devices
	                if (index != 0) {
	                    if (mSafeMediaVolumeState == SAFE_MEDIA_VOLUME_ACTIVE &&
	                            (device & mSafeMediaVolumeDevices) != 0) {
	                    	
	                        index = ClassHelper.getIntField(obj, "mSafeMediaVolumeIndex");//mSafeMediaVolumeIndex;
	                    } else {
	                        index =  (Integer)ClassHelper.callMethod(streamState, "getIndex", device);//streamState.getMaxIndex();
	                    }
	                }
	            }
	            boolean checkSafeMediaVolume = (Boolean) ClassHelper.callMethod(isHuawei(obj),obj, "checkSafeMediaVolume", 
	            		streamTypeAlias, index, device);
	            if (!checkSafeMediaVolume) {
	            	if(mVolumePanel != null){
	                mVolumePanel.postDisplaySafeVolumeWarning(flags);
	            	}
	                Object value =null;
	                try{
	                value = ClassHelper.newInstance(Class.forName("android.media.AudioService.StreamVolumeCommand"),
	                		streamType, index, flags, device);
	                ClassHelper.setObjectField(obj, "mPendingVolumeCommand", value);
	                }catch(Exception e){
	                	
	                }
//	                mPendingVolumeCommand = new StreamVolumeCommand(
//	                                                    streamType, index, flags, device);
	            } else {
	            	ClassHelper.callMethod(isHuawei(obj),obj, "onSetStreamVolume", streamType, index, flags, device);
//	                onSetStreamVolume(streamType, index, flags, device);
	                index = (Integer)ClassHelper.callMethod(mStreamStates[streamType], "getIndex", device);//(device);
	            }
	        }
	        ClassHelper.callMethod(isHuawei(obj),obj, "sendVolumeUpdate", streamType, oldIndex, index, flags);
//	        sendVolumeUpdate(streamType, oldIndex, index, flags);
	}
	
	public void after_handleConfigurationChanged(MethodHookParam param){
		if(param == null){
			return;
		}
		Context context = (Context) param.args[0];
		if(context== null){
			return;
		}
		if(mVolumePanel != null){
			 Configuration config = context.getResources().getConfiguration();
			mVolumePanel.setLayoutDirection(config.getLayoutDirection());
		}
	}
	
//	/home/alexluo/luofu/software/jdk1.6.0_32
	/**
	 * int streamType, int direction, int flags
	 */
	public void before_adjustRemoteVolume(MethodHookParam param){
		param.setResult(null);
		int streamType = (Integer) param.args[0];
		int flags = (Integer) param.args[2];
		int direction = (Integer) param.args[1];
		Object obj = param.thisObject;
		  int rccId = RCSE_ID_UNREGISTERED;
	        boolean volFixed = false;
	        Object mMainRemote = ClassHelper.getObjectField(obj, "mMainRemote");
	        synchronized (mMainRemote) {
	        	boolean mMainRemoteIsActive = ClassHelper.getBooleanField(obj, "mMainRemoteIsActive");
	            if (!mMainRemoteIsActive) {
//	                if (DEBUG_VOL) Log.w(TAG, "adjustRemoteVolume didn't find an active client");
	                return;
	            }
	            int mRccId = ClassHelper.getIntField(mMainRemote, "mRccId");
	            int mVolumeHandling =  ClassHelper.getIntField(mMainRemote, "mVolumeHandling");
	            rccId = mRccId;
	            volFixed = (mVolumeHandling ==PLAYBACK_VOLUME_FIXED);
	        }
	        // unlike "local" stream volumes, we can't compute the new volume based on the direction,
	        // we can only notify the remote that volume needs to be updated, and we'll get an async'
	        // update through setPlaybackInfoForRcc()
	        if (!volFixed) {
	        	ClassHelper.callMethod(isHuawei(obj),obj, "sendVolumeUpdateToRemote", rccId, direction);
	        	
	        }

	        // fire up the UI
	        if(mVolumePanel != null){
//		mVolumePanel.postRemoteVolumeChanged(streamType, flags);
	        }
	}
	
	
	
	/**
	 * int streamType, int index, int device
	 */
	public void before_checkSafeMediaVolume(MethodHookParam param){
		int streamType = (Integer) param.args[0];
		int index= (Integer) param.args[1];
		int device= (Integer) param.args[2];
		Object obj = param.thisObject;
		Integer mSafeMediaVolumeState = (Integer) ClassHelper.getObjectField(param.thisObject, "mSafeMediaVolumeState");
		  synchronized (mSafeMediaVolumeState) {
			  int mSafeMediaVolumeIndex = ClassHelper.getIntField(obj, "mSafeMediaVolumeIndex");
			  int[] mStreamVolumeAlias = (int[]) ClassHelper.getObjectField(obj, "mStreamVolumeAlias");
	            if ((mSafeMediaVolumeState == SAFE_MEDIA_VOLUME_ACTIVE) &&
	                    (mStreamVolumeAlias[streamType] == AudioSystem.STREAM_MUSIC) &&
	                    ((device & mSafeMediaVolumeDevices) != 0) &&
	                    (index > mSafeMediaVolumeIndex)) {
	            	if(mVolumePanel != null){
	                mVolumePanel.postDisplaySafeVolumeWarning();
	            	}
	               param.setResult(false);
	            }
	            param.setResult(true);
	        }
	}
	
	/**
	 * int streamType, int direction, int flags
	 * @param param
	 */
    public static final int DEVICE_BIT_DEFAULT = 0x40000000;
	public static final int DEVICE_OUT_EARPIECE = 0x1;
    public static final int DEVICE_OUT_SPEAKER = 0x2;
    public static final int DEVICE_OUT_WIRED_HEADSET = 0x4;
    public static final int DEVICE_OUT_WIRED_HEADPHONE = 0x8;
    public static final int DEVICE_OUT_BLUETOOTH_SCO = 0x10;
    public static final int DEVICE_OUT_BLUETOOTH_SCO_HEADSET = 0x20;
    public static final int DEVICE_OUT_BLUETOOTH_SCO_CARKIT = 0x40;
    public static final int DEVICE_OUT_BLUETOOTH_A2DP = 0x80;
    public static final int DEVICE_OUT_BLUETOOTH_A2DP_HEADPHONES = 0x100;
    public static final int DEVICE_OUT_BLUETOOTH_A2DP_SPEAKER = 0x200;
    public static final int DEVICE_OUT_AUX_DIGITAL = 0x400;
    public static final int DEVICE_OUT_ANLG_DOCK_HEADSET = 0x800;
    public static final int DEVICE_OUT_DGTL_DOCK_HEADSET = 0x1000;
    public static final int DEVICE_OUT_USB_ACCESSORY = 0x2000;
    public static final int DEVICE_OUT_USB_DEVICE = 0x4000;
    public static final int DEVICE_OUT_REMOTE_SUBMIX = 0x8000;
    public static final int DEVICE_OUT_PROXY = 0x40000;
    public static final int DEVICE_OUT_FM = 0x80000;
    public static final int DEVICE_OUT_FM_TX = 0x100000;
    public static final int DEVICE_OUT_DEFAULT = DEVICE_BIT_DEFAULT;
    
    public static final int FLAG_BLUETOOTH_ABS_VOLUME = 1 << 6;
	 public static final int DEVICE_OUT_ALL_A2DP = (DEVICE_OUT_BLUETOOTH_A2DP |
             DEVICE_OUT_BLUETOOTH_A2DP_HEADPHONES |
             DEVICE_OUT_BLUETOOTH_A2DP_SPEAKER);
	  private static final int[] STEAM_VOLUME_OPS = new int[] {
	        AppOpsManager.OP_AUDIO_VOICE_VOLUME,            // STREAM_VOICE_CALL
	        AppOpsManager.OP_AUDIO_MEDIA_VOLUME,            // STREAM_SYSTEM
	        AppOpsManager.OP_AUDIO_RING_VOLUME,             // STREAM_RING
	        AppOpsManager.OP_AUDIO_MEDIA_VOLUME,            // STREAM_MUSIC
	        AppOpsManager.OP_AUDIO_ALARM_VOLUME,            // STREAM_ALARM
	        AppOpsManager.OP_AUDIO_NOTIFICATION_VOLUME,     // STREAM_NOTIFICATION
	        AppOpsManager.OP_AUDIO_BLUETOOTH_VOLUME,        // STREAM_BLUETOOTH_SCO
	        AppOpsManager.OP_AUDIO_MEDIA_VOLUME,            // STREAM_SYSTEM_ENFORCED
	        AppOpsManager.OP_AUDIO_MEDIA_VOLUME,            // STREAM_DTMF
	        AppOpsManager.OP_AUDIO_MEDIA_VOLUME,            // STREAM_TTS
	    };
	public void before_adjustStreamVolume(MethodHookParam param){
		param.setResult(null);
		int streamType = (Integer) param.args[0];
		int direction = (Integer) param.args[1];
		int flags = (Integer) param.args[2];
		String callingPackage = (String)param.args[3];
		Object obj = param.thisObject;
		Handler mAudioHandler = (Handler) ClassHelper.getObjectField(obj, "mAudioHandler");
		ClassHelper.callMethod(isHuawei(obj),obj, "ensureValidDirection", direction);
		ClassHelper.callMethod(isHuawei(obj),obj, "ensureValidStreamType", streamType);
//		    ensureValidDirection(direction);
//	        ensureValidStreamType(streamType);

	        // use stream type alias here so that streams with same alias have the same behavior,
	        // including with regard to silent mode control (e.g the use of STREAM_RING below and in
	        // checkForRingerModeChange() in place of STREAM_RING or STREAM_NOTIFICATION)
		    int[] mStreamVolumeAlias = (int[]) ClassHelper.getObjectField(obj, "mStreamVolumeAlias");
		    VolumeStreamState[] mStreamStates = (VolumeStreamState[]) ClassHelper.getObjectField(obj, "mStreamStates");
	        int streamTypeAlias = mStreamVolumeAlias[streamType];
	        VolumeStreamState streamState = mStreamStates[streamTypeAlias];

	        final int device = (Integer) ClassHelper.callMethod(isHuawei(obj),obj, "getDeviceForStream", streamTypeAlias);//getDeviceForStream(streamTypeAlias);
	        // get last audible index if stream is muted, current index otherwise
	      //  int value = (Integer) ClassHelper.callMethod(streamState, "muteCount");
	        int aliasIndex = (Integer) ClassHelper.callMethod(streamState, "getIndex", device/*,(value != 0)*/);
//	        final int aliasIndex = streamState.getIndex(device,(streamState.muteCount() != 0) /* lastAudible */);
	        boolean adjustVolume = true;

	        // convert one UI step (+/-1) into a number of internal units on the stream alias
	        int step ;//= (Integer) ClassHelper.callMethod(obj, "rescaleIndex", 10, streamType, streamTypeAlias);//rescaleIndex(10, streamType, streamTypeAlias);

	        if ((device & DEVICE_OUT_ALL_A2DP) == 0 &&
	                (flags & FLAG_BLUETOOTH_ABS_VOLUME) != 0) {
	                return;
	            }
	        AppOpsManager mAppOps = (AppOpsManager)ClassHelper.getObjectField(obj, "mAppOps");
	        if (mAppOps.noteOp(STEAM_VOLUME_OPS[streamTypeAlias], Binder.getCallingUid(),
	                callingPackage) != AppOpsManager.MODE_ALLOWED) {
	            return;
	        }
	     // reset any pending volume command
	        Integer mSafeMediaVolumeState = (Integer)ClassHelper.getObjectField(obj, "mSafeMediaVolumeState");
	        synchronized (mSafeMediaVolumeState) {
	        	ClassHelper.setObjectField(obj, "mPendingVolumeCommand", null);
//	            mPendingVolumeCommand = null;
	        }

	        flags &= ~AudioManager.FLAG_FIXED_VOLUME;
	        if ((streamTypeAlias == AudioSystem.STREAM_MUSIC) &&
	               ((device & mFixedVolumeDevices) != 0)) {
	            flags |= AudioManager.FLAG_FIXED_VOLUME;

	            // Always toggle between max safe volume and 0 for fixed volume devices where safe
	            // volume is enforced, and max and 0 for the others.
	            // This is simulated by stepping by the full allowed volume range
	            if (mSafeMediaVolumeState == SAFE_MEDIA_VOLUME_ACTIVE &&
	                    (device & mSafeMediaVolumeDevices) != 0) {
	                step = ClassHelper.getIntField(obj, "mSafeMediaVolumeIndex");
	            } else {
	                step = (Integer) ClassHelper.callMethod(streamState, "getMaxIndex");//streamState.getMaxIndex();
	            }
	            if (aliasIndex != 0) {
	                aliasIndex = step;
	            }
	        } else {
	            // convert one UI step (+/-1) into a number of internal units on the stream alias
	        	step = (Integer) ClassHelper.callMethod(isHuawei(obj),obj, "rescaleIndex", 10, streamType, streamTypeAlias);
//	            step = rescaleIndex(10, streamType, streamTypeAlias);
	        }

	        // If either the client forces allowing ringer modes for this adjustment,
	        // or the stream type is one that is affected by ringer modes
	        int getMasterStreamType = (Integer) ClassHelper.callMethod(isHuawei(obj),obj, "getMasterStreamType");
	        if (((flags & AudioManager.FLAG_ALLOW_RINGER_MODES) != 0) ||
	                (streamTypeAlias == getMasterStreamType)) {
	            int ringerMode = (Integer) ClassHelper.callMethod(isHuawei(obj),obj, "getRingerMode"); //getRingerMode();
	            // do not vibrate if already in vibrate mode
	            if (ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
	                flags &= ~AudioManager.FLAG_VIBRATE;
	            }
	            // Check if the ringer mode changes with this volume adjustment. If
	            // it does, it will handle adjusting the volume, so we won't below
	            adjustVolume =(Boolean) ClassHelper.callMethod(isHuawei(obj),obj, "checkForRingerModeChange", aliasIndex, direction, step);
//	            adjustVolume = checkForRingerModeChange(aliasIndex, direction, step);
	        }

	        int oldIndex = (Integer) ClassHelper.callMethod(mStreamStates[streamType], "getIndex", device/*,(value != 0)*/);//mStreamStates[streamType].getIndex(device);

	        if (adjustVolume && (direction != AudioManager.ADJUST_SAME)) {

	            // Check if volume update should be send to AVRCP
	            if (streamTypeAlias == AudioSystem.STREAM_MUSIC &&
	                (device & AudioSystem.DEVICE_OUT_ALL_A2DP) != 0 &&
	                (flags & AudioManager.FLAG_BLUETOOTH_ABS_VOLUME) == 0) {
	            	Object mA2dpAvrcpLock = ClassHelper.getObjectField(obj, "mA2dpAvrcpLock");
	            	Object mA2dp= ClassHelper.getObjectField(obj, "mA2dp");
	            	Boolean mAvrcpAbsVolSupported = ClassHelper.getBooleanField(obj, "mAvrcpAbsVolSupported");
	                synchronized (mA2dpAvrcpLock) {
	                    if (mA2dp != null && mAvrcpAbsVolSupported) {
	                    	ClassHelper.callMethod(mA2dp, "adjustAvrcpAbsoluteVolume", direction);
//	                        mA2dp.adjustAvrcpAbsoluteVolume(direction);
	                    }
	                }
	            }

	            boolean checkSafeMediaVolume = (Boolean) ClassHelper.callMethod(isHuawei(obj),obj, "checkSafeMediaVolume", streamTypeAlias, aliasIndex + step, device);
	            if ((direction == AudioManager.ADJUST_RAISE) &&
	                    !checkSafeMediaVolume) {
//	                Log.e(TAG, "adjustStreamVolume() safe volume index = "+oldIndex);
	            	if(mVolumePanel != null){
	                mVolumePanel.postDisplaySafeVolumeWarning(flags);
	            	}
	            } else if ((Boolean) ClassHelper.callMethod(streamState, "adjustIndex", direction * step, device)
	            		/*streamState.adjustIndex(direction * step, device)*/) {
	                // Post message to set system volume (it in turn will post a message
	                // to persist). Do not change volume if stream is muted.
	            	ClassHelper.callMethod(isHuawei(obj),obj, "sendMsg", mAudioHandler,
	                        MSG_SET_DEVICE_VOLUME,
	                        SENDMSG_QUEUE,
	                        device,
	                        0,
	                        streamState,
	                        0);
//	                sendMsg(mAudioHandler,
//	                        MSG_SET_DEVICE_VOLUME,
//	                        SENDMSG_QUEUE,
//	                        device,
//	                        0,
//	                        streamState,
//	                        0);
	            }
	        }
	        int index = (Integer) ClassHelper.callMethod(mStreamStates[streamType], "getIndex", device/*,(value != 0)*/);
	        //mStreamStates[streamType].getIndex(device);
	        ClassHelper.callMethod(isHuawei(obj),obj, "sendVolumeUpdate", streamType, oldIndex, index, flags);
//	        sendVolumeUpdate(streamType, oldIndex, index, flags);

	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	final int mFixedVolumeDevices = AudioSystem.DEVICE_OUT_AUX_DIGITAL |
            AudioSystem.DEVICE_OUT_DGTL_DOCK_HEADSET |
            AudioSystem.DEVICE_OUT_ANLG_DOCK_HEADSET |
            AudioSystem.DEVICE_OUT_ALL_USB;
	
	
	
	
	
	
	
    private final int SAFE_MEDIA_VOLUME_ACTIVE = 3;
	 /**
     * @hide
     * Default value for the unique identifier
     */
    public final static int RCSE_ID_UNREGISTERED = -1;
    /**
     * Unique identifier of the RemoteControlStackEntry in AudioService with which
     * this RemoteControlClient is associated.
     */
    private int mRcseId = RCSE_ID_UNREGISTERED;
	
	 /**
     * Playback state of a RemoteControlClient which is stopped.
     *
     * @see #setPlaybackState(int)
     */
    public final static int PLAYSTATE_STOPPED            = 1;
    /**
     * Playback state of a RemoteControlClient which is paused.
     *
     * @see #setPlaybackState(int)
     */
    public final static int PLAYSTATE_PAUSED             = 2;
    /**
     * Playback state of a RemoteControlClient which is playing media.
     *
     * @see #setPlaybackState(int)
     */
    public final static int PLAYSTATE_PLAYING            = 3;
    /**
     * Playback state of a RemoteControlClient which is fast forwarding in the media
     *    it is currently playing.
     *
     * @see #setPlaybackState(int)
     */
    public final static int PLAYSTATE_FAST_FORWARDING    = 4;
    /**
     * Playback state of a RemoteControlClient which is fast rewinding in the media
     *    it is currently playing.
     *
     * @see #setPlaybackState(int)
     */
    public final static int PLAYSTATE_REWINDING          = 5;
    /**
     * Playback state of a RemoteControlClient which is skipping to the next
     *    logical chapter (such as a song in a playlist) in the media it is currently playing.
     *
     * @see #setPlaybackState(int)
     */
    public final static int PLAYSTATE_SKIPPING_FORWARDS  = 6;
    /**
     * Playback state of a RemoteControlClient which is skipping back to the previous
     *    logical chapter (such as a song in a playlist) in the media it is currently playing.
     *
     * @see #setPlaybackState(int)
     */
    public final static int PLAYSTATE_SKIPPING_BACKWARDS = 7;
    /**
     * Playback state of a RemoteControlClient which is buffering data to play before it can
     *    start or resume playback.
     *
     * @see #setPlaybackState(int)
     */
    public final static int PLAYSTATE_BUFFERING          = 8;
    /**
     * Playback state of a RemoteControlClient which cannot perform any playback related
     *    operation because of an internal error. Examples of such situations are no network
     *    connectivity when attempting to stream data from a server, or expired user credentials
     *    when trying to play subscription-based content.
     *
     * @see #setPlaybackState(int)
     */
    public final static int PLAYSTATE_ERROR              = 9;
    /**
     * @hide
     * The value of a playback state when none has been declared.
     * Intentionally hidden as an application shouldn't set such a playback state value.
     */
    public final static int PLAYSTATE_NONE               = 0;

    /**
     * @hide
     * The default playback type, "local", indicating the presentation of the media is happening on
     * the same device (e.g. a phone, a tablet) as where it is controlled from.
     */
    public final static int PLAYBACK_TYPE_LOCAL = 0;
    /**
     * @hide
     * A playback type indicating the presentation of the media is happening on
     * a different device (i.e. the remote device) than where it is controlled from.
     */
    public final static int PLAYBACK_TYPE_REMOTE = 1;
    private final static int PLAYBACK_TYPE_MIN = PLAYBACK_TYPE_LOCAL;
    private final static int PLAYBACK_TYPE_MAX = PLAYBACK_TYPE_REMOTE;
    /**
     * @hide
     * Playback information indicating the playback volume is fixed, i.e. it cannot be controlled
     * from this object. An example of fixed playback volume is a remote player, playing over HDMI
     * where the user prefer to control the volume on the HDMI sink, rather than attenuate at the
     * source.
     * @see #PLAYBACKINFO_VOLUME_HANDLING.
     */
    public final static int PLAYBACK_VOLUME_FIXED = 0;
    /**
     * @hide
     * Playback information indicating the playback volume is variable and can be controlled from
     * this object.
     * @see #PLAYBACKINFO_VOLUME_HANDLING.
     */
    public final static int PLAYBACK_VOLUME_VARIABLE = 1;
    /**
     * @hide (to be un-hidden)
     * The playback information value indicating the value of a given information type is invalid.
     * @see #PLAYBACKINFO_VOLUME_HANDLING.
     */
    public final static int PLAYBACKINFO_INVALID_VALUE = Integer.MIN_VALUE;

    //==========================================
    // Public keys for playback information
    /**
     * @hide
     * Playback information that defines the type of playback associated with this
     * RemoteControlClient. See {@link #PLAYBACK_TYPE_LOCAL} and {@link #PLAYBACK_TYPE_REMOTE}.
     */
    public final static int PLAYBACKINFO_PLAYBACK_TYPE = 1;
    /**
     * @hide
     * Playback information that defines at what volume the playback associated with this
     * RemoteControlClient is performed. This information is only used when the playback type is not
     * local (see {@link #PLAYBACKINFO_PLAYBACK_TYPE}).
     */
    public final static int PLAYBACKINFO_VOLUME = 2;
    /**
     * @hide
     * Playback information that defines the maximum volume volume value that is supported
     * by the playback associated with this RemoteControlClient. This information is only used
     * when the playback type is not local (see {@link #PLAYBACKINFO_PLAYBACK_TYPE}).
     */
    public final static int PLAYBACKINFO_VOLUME_MAX = 3;
    /**
     * @hide
     * Playback information that defines how volume is handled for the presentation of the media.
     * @see #PLAYBACK_VOLUME_FIXED
     * @see #PLAYBACK_VOLUME_VARIABLE
     */
    public final static int PLAYBACKINFO_VOLUME_HANDLING = 4;
    /**
     * @hide
     * Playback information that defines over what stream type the media is presented.
     */
    public final static int PLAYBACKINFO_USES_STREAM = 5;

    //==========================================
    // Private keys for playback information
    /**
     * @hide
     * Used internally to relay playback state (set by the application with
     * {@link #setPlaybackState(int)}) to AudioService
     */
    public final static int PLAYBACKINFO_PLAYSTATE = 255;


    /**
     * Flag indicating a RemoteControlClient makes use of the "previous" media key.
     *
     * @see #setTransportControlFlags(int)
     * @see android.view.KeyEvent#KEYCODE_MEDIA_PREVIOUS
     */
    public final static int FLAG_KEY_MEDIA_PREVIOUS = 1 << 0;
    /**
     * Flag indicating a RemoteControlClient makes use of the "rewind" media key.
     *
     * @see #setTransportControlFlags(int)
     * @see android.view.KeyEvent#KEYCODE_MEDIA_REWIND
     */
    public final static int FLAG_KEY_MEDIA_REWIND = 1 << 1;
    /**
     * Flag indicating a RemoteControlClient makes use of the "play" media key.
     *
     * @see #setTransportControlFlags(int)
     * @see android.view.KeyEvent#KEYCODE_MEDIA_PLAY
     */
    public final static int FLAG_KEY_MEDIA_PLAY = 1 << 2;
    /**
     * Flag indicating a RemoteControlClient makes use of the "play/pause" media key.
     *
     * @see #setTransportControlFlags(int)
     * @see android.view.KeyEvent#KEYCODE_MEDIA_PLAY_PAUSE
     */
    public final static int FLAG_KEY_MEDIA_PLAY_PAUSE = 1 << 3;
    /**
     * Flag indicating a RemoteControlClient makes use of the "pause" media key.
     *
     * @see #setTransportControlFlags(int)
     * @see android.view.KeyEvent#KEYCODE_MEDIA_PAUSE
     */
    public final static int FLAG_KEY_MEDIA_PAUSE = 1 << 4;
    /**
     * Flag indicating a RemoteControlClient makes use of the "stop" media key.
     *
     * @see #setTransportControlFlags(int)
     * @see android.view.KeyEvent#KEYCODE_MEDIA_STOP
     */
    public final static int FLAG_KEY_MEDIA_STOP = 1 << 5;
    /**
     * Flag indicating a RemoteControlClient makes use of the "fast forward" media key.
     *
     * @see #setTransportControlFlags(int)
     * @see android.view.KeyEvent#KEYCODE_MEDIA_FAST_FORWARD
     */
    public final static int FLAG_KEY_MEDIA_FAST_FORWARD = 1 << 6;
    /**
     * Flag indicating a RemoteControlClient makes use of the "next" media key.
     *
     * @see #setTransportControlFlags(int)
     * @see android.view.KeyEvent#KEYCODE_MEDIA_NEXT
     */
    public final static int FLAG_KEY_MEDIA_NEXT = 1 << 7;

    /**
     * @hide
     * The flags for when no media keys are declared supported.
     * Intentionally hidden as an application shouldn't set the transport control flags
     *     to this value.
     */
    public final static int FLAGS_KEY_MEDIA_NONE = 0;

    /**
     * @hide
     * Flag used to signal some type of metadata exposed by the RemoteControlClient is requested.
     */
    public final static int FLAG_INFORMATION_REQUEST_METADATA = 1 << 0;
    /**
     * @hide
     * Flag used to signal that the transport control buttons supported by the
     *     RemoteControlClient are requested.
     * This can for instance happen when playback is at the end of a playlist, and the "next"
     * operation is not supported anymore.
     */
    public final static int FLAG_INFORMATION_REQUEST_KEY_MEDIA = 1 << 1;
    /**
     * @hide
     * Flag used to signal that the playback state of the RemoteControlClient is requested.
     */
    public final static int FLAG_INFORMATION_REQUEST_PLAYSTATE = 1 << 2;
    /**
     * @hide
     * Flag used to signal that the album art for the RemoteControlClient is requested.
     */
    public final static int FLAG_INFORMATION_REQUEST_ALBUM_ART = 1 << 3;
	
}
