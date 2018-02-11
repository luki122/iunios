/*
 * Copyright (c) 2013 Qualcomm Technologies, Inc.  All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */

package com.qualcomm.listen.voicewakeup;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import android.content.*;
import android.content.SharedPreferences.Editor;
import android.os.*;
import android.util.*;

import com.qualcomm.listen.ListenTypes;
import com.qualcomm.listen.ListenTypes.ConfidenceData;
import com.qualcomm.listen.ListenTypes.SoundModelParams;


/**
 * Global object for configuration management.
 * and utility functions.
 */
public class Global {
	private final static String TAG = "ListenLog.Global";
	private final static String MYTAG = "iht";
	public final static String APP_NAME = "VoiceWakeUp";
	public static String OLD_APP_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + APP_NAME;
	public static String APP_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "." + APP_NAME;
	public final static String PREFERENCE_GROUP_NAME = "VoiceWakeUp";
	public final static String VOICE_WAKEUP_SERVICE = "VoiceWakeUpSevice";
    public final static String NO_USERNAME = "<No User>";
    public final static String RECORDINGS_FILE_NAME = "recording";
    public final static String RECORDINGS_FILE_EXT = ".wav";
    public final static String RECORDINGS_TEMP_FILE_PATH = Global.APP_PATH + "/" + "temp_recording.wav";
    public final static int TRAINING_RECORDINGS_COUNT = 5;
    public final static String SOUND_MODEL_FILE_EXT = ".vwu";
    public final static int SUCCESS = 0;
    public final static int FAILURE = -1;

    // System Settings Tags
    private final static String TAG_SETTING_SOUND_MODEL_SELECTED = "soundModelSelected";
    private final static String TAG_SETTING_KEYWORD = "keyword";
    private final static String TAG_SETTING_USERNAME = "username";

    private final static String TAG_SETTING_KEYWORD_THRESHOLD = "keywordThreshold";
    private final static String TAG_SETTING_USER_THRESHOLD = "userThreshold";
    private final static String TAG_SETTING_LISTEN_ENABLED = "listenEnabled";
    private final static String TAG_SETTING_VOICEWAKEUP_ENABLED = "voicewakeupEnabled";
    private final static String TAG_SETTING_USER_VERIFICATION_ENABLED = "userVerificationEnabled";
    private final static String TAG_SETTING_FAILURE_FEEDBACK_ENABLED = "failureFeedbackEnabled";
    private final static String TAG_SETTING_TONE_ENABLED = "toneEnabled";
    private final static String TAG_SETTING_LAUNCH_VOICEQNA = "launchVoiceqna";
    private final static String TAG_SETTING_SHOW_ADVANCED_DETAIL = "showAdvanceDetail";

	// Default values
    public final static String DEFAULT_VERSION_NUMBER = "No Version Number";
    public final static boolean DEFAULT_IS_A_SOUND_MODEL_SELECTED = false;
    public final static String DEFAULT_KEYWORD = "HeySnapdragon";
    public final static String DEFAULT_USERNAME = NO_USERNAME;
    public final static boolean DEFAULT_AUTOSTART = false;

    //关键字匹配
    public final static short DEFAULT_KEYWORD_THRESHOLD = 55;// 60;
    //用户声音匹配
    public final static short DEFAULT_USER_THRESHOLD =50; //61;
	//默认监听
	public final static boolean DEFAULT_ENABLE_LISTEN = true;
    //默认激活
    public final static boolean DEFAULT_ENABLE_VOICEWAKEUP = true;
    //用户验证
    public final static boolean DEFAULT_USER_VERIFICATION_ENABLED = true; //false;
    //错误反馈
    public final static boolean DEFAULT_FAILURE_FEEDBACK_ENABLED = false;
    //检测用户口音
    public final static boolean DEFAULT_TONE_ENABLED = false; //true;//*******0811（慎重）************
    //启动问答？？？
    public final static boolean DEFAULT_LAUNCH_VOICEQNA = false;
    //显示识别详情
    public final static boolean DEFAULT_SHOW_ADVANCED_DETAIL = false; //true;

    // Set default values
    private String versionNumber = DEFAULT_VERSION_NUMBER;
	private boolean isASoundModelSelected = DEFAULT_IS_A_SOUND_MODEL_SELECTED;
    private String keyword = DEFAULT_KEYWORD;
	private String username = DEFAULT_USERNAME;
	// autoStart is only for APT, does not autoStart if a SM was registered when the app last closed
	private boolean autoStart = DEFAULT_AUTOSTART;

    private int keywordThreshold = DEFAULT_KEYWORD_THRESHOLD;
    private int userThreshold = DEFAULT_USER_THRESHOLD;
    private boolean enableListen = DEFAULT_ENABLE_LISTEN;
    private boolean enableVoiceWakeup = DEFAULT_ENABLE_VOICEWAKEUP;
    private boolean userVerificationEnabled = DEFAULT_USER_VERIFICATION_ENABLED;
    private boolean failureFeedbackEnabled = DEFAULT_FAILURE_FEEDBACK_ENABLED;
    private boolean toneEnabled = DEFAULT_TONE_ENABLED;
    private boolean launchVoiceqna = DEFAULT_LAUNCH_VOICEQNA;
	//private boolean showAdvancedDetail = DEFAULT_SHOW_ADVANCED_DETAIL;

    private ByteBuffer soundModelInUse = null;
    private ByteBuffer extendedSoundModel = null;
    protected SoundModelParams sound_model_params = null;
    private ByteBuffer keywordOnlySoundModel = null;
    private ConfidenceData confidenceData = new ConfidenceData();
    private ShortBuffer[] userRecordings = new ShortBuffer[5];
    private int numUserRecordings = 0;
    private boolean libsError = false;

    private boolean isVoiceWakeUpServiceStarted;

    
    private Global() {
	}

	private static class GlobalInstance {
		public static Global Instance = new Global();
	}

	public static Global getInstance() {
		return GlobalInstance.Instance;
	}

	// Save settings to Share Preferences
	public void saveSettingsToSharedPreferences(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_GROUP_NAME, 0);

        Editor editor = sp.edit();

        editor.putInt(TAG_SETTING_KEYWORD_THRESHOLD, this.keywordThreshold);
        editor.putInt(TAG_SETTING_USER_THRESHOLD, this.userThreshold);
        editor.putBoolean(TAG_SETTING_LISTEN_ENABLED, this.enableListen);
        editor.putBoolean(TAG_SETTING_VOICEWAKEUP_ENABLED, this.enableVoiceWakeup);
        editor.putBoolean(TAG_SETTING_USER_VERIFICATION_ENABLED, this.userVerificationEnabled);
        editor.putBoolean(TAG_SETTING_FAILURE_FEEDBACK_ENABLED, this.failureFeedbackEnabled);
        editor.putBoolean(TAG_SETTING_TONE_ENABLED, this.toneEnabled);
        editor.putBoolean(TAG_SETTING_LAUNCH_VOICEQNA, this.launchVoiceqna);
        //editor.putBoolean(TAG_SETTING_SHOW_ADVANCED_DETAIL, this.showAdvancedDetail);

        editor.commit();
    }

	// Load settings from Shared Preferences.
	// If the item is not in the saved context, use the value set by default.
	public void loadSettingsFromSharedPreferences(Context context) {
		SharedPreferences sp = context.getSharedPreferences(PREFERENCE_GROUP_NAME, 0);

		this.keywordThreshold = sp.getInt(TAG_SETTING_KEYWORD_THRESHOLD, this.keywordThreshold);
		this.userThreshold = sp.getInt(TAG_SETTING_USER_THRESHOLD, this.userThreshold);
		
		this.enableListen = sp.getBoolean(TAG_SETTING_LISTEN_ENABLED, this.enableListen);
		this.enableVoiceWakeup = sp.getBoolean(TAG_SETTING_VOICEWAKEUP_ENABLED, this.enableVoiceWakeup);
		
		this.userVerificationEnabled = sp.getBoolean(TAG_SETTING_USER_VERIFICATION_ENABLED, this.userVerificationEnabled);
		this.failureFeedbackEnabled = sp.getBoolean(TAG_SETTING_FAILURE_FEEDBACK_ENABLED, this.failureFeedbackEnabled);
		this.toneEnabled = sp.getBoolean(TAG_SETTING_TONE_ENABLED, this.toneEnabled);
		this.launchVoiceqna = sp.getBoolean(TAG_SETTING_LAUNCH_VOICEQNA, this.launchVoiceqna);
		//this.showAdvancedDetail = sp.getBoolean(TAG_SETTING_SHOW_ADVANCED_DETAIL, this.showAdvancedDetail);
	}

	// Save selected sound model to Share Preferences and set global values for it
	public void setAndSaveSelectedSoundModel(Context context, String keyword, String username) {
        Log.v(TAG, "setSelectedSoundModel: keyword= " + keyword + ", username= " + username);
        //this.keyword = keyword;
        //this.username = username;
        setKeyword(keyword);
        setUsername(username);
        setIsASoundModelSelected(true);
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_GROUP_NAME, 0); //保存当前的，选中声闻

        Editor editor = sp.edit();
        editor.putBoolean(TAG_SETTING_SOUND_MODEL_SELECTED, getIsASoundModelSelected());
        editor.putString(TAG_SETTING_KEYWORD, keyword);
        editor.putString(TAG_SETTING_USERNAME, username);
        editor.commit();
    }

	// Remove saved sound model from Share Preferences
    public void unselectAndUnsaveSoundModel(Context context) {
        Log.v(TAG, "unselectSoundModel");
        this.setIsASoundModelSelected(false);
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_GROUP_NAME, 0);

        Editor editor = sp.edit();
        editor.putBoolean(TAG_SETTING_SOUND_MODEL_SELECTED, getIsASoundModelSelected());
        editor.commit();
    }

    // Load the sound model that was saved in Share Preferences
    public void loadSavedSoundModel(Context context) {
        Log.v(TAG, "loadSavedSoundModel");
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_GROUP_NAME, 0);
        this.isASoundModelSelected = sp.getBoolean(TAG_SETTING_SOUND_MODEL_SELECTED, this.isASoundModelSelected);
        Log.v(TAG, "loadSavedSoundModel: this.isASoundModelSelected= " + this.isASoundModelSelected);
        if (this.isASoundModelSelected) {
            this.keyword = sp.getString(TAG_SETTING_KEYWORD, this.keyword);
            this.username = sp.getString(TAG_SETTING_USERNAME, this.username);
            Log.v(TAG, "loadSavedSoundModel: this.keyword= " + this.keyword + ", this.username= " + this.username);
        }
    }


	// Setters and getters

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

	public void setIsASoundModelSelected(boolean selected) {
	    this.isASoundModelSelected = selected;
	}

	public boolean getIsASoundModelSelected() {
        return this.isASoundModelSelected;
    }

    public void setKeyword(String inKeyword) {
        this.keyword = inKeyword;
    }

    public String getKeyword() {
        return this.keyword;
    }

    public void setUsername(String inUsername) {
        this.username = inUsername;
    }

	public String getUsername() {
        return this.username;
    }

	public void setAutoStart(boolean inAutoStart) {
        this.autoStart = inAutoStart;
    }

    public boolean getAutoStart() {
        return this.autoStart;
    }

	/*public boolean getShowAdvancedDetail() {
		return showAdvancedDetail;
	}*/

    public void setLibsError(boolean libsError) {
        this.libsError = libsError;
    }

    public boolean getLibsError() {
        return libsError;
    }


	// Check if a sound model file exists by its keyword and username
	public boolean soundModelFileExists(String keyword, String username) {
		File soundModelFile = new File(generateSoundModelFilePath(keyword, username));
		return soundModelFile.exists();
	}

	// Given a keyword and user name provide the name of the sound model
	public String generateSoundModelFilePath(String keyword, String username) {
	    if (null == username || username.equalsIgnoreCase(Global.NO_USERNAME)) {
	        return Global.APP_PATH + "/" + keyword + SOUND_MODEL_FILE_EXT;
	    }
		return Global.APP_PATH + "/" + keyword + "_" + username + SOUND_MODEL_FILE_EXT;
	}

	public void setSoundModelInUse(ByteBuffer listenSoundModel) {
        this.soundModelInUse = listenSoundModel;
        if(listenSoundModel == null){
        	this.isVoiceWakeUpServiceStarted = false;
        }else{
        	this.isVoiceWakeUpServiceStarted = true; //已经处在服务状态
        }
    }
	
	//HT
	public void writeVoiceWakeUpPreference(Context context, boolean bool){
		SharedPreferences pre = context.getSharedPreferences(PREFERENCE_GROUP_NAME, 0);
		pre.edit().putBoolean(VOICE_WAKEUP_SERVICE, bool).commit();

		this.isVoiceWakeUpServiceStarted = bool;
	}

	public void readVoiceWakeUpPreference(Context context){
		SharedPreferences pre = context.getSharedPreferences(PREFERENCE_GROUP_NAME, 0);
		this.isVoiceWakeUpServiceStarted = pre.getBoolean(VOICE_WAKEUP_SERVICE, false);
	}
	
	public boolean getVoiceWakeUpServiceStatus(){
		return this.isVoiceWakeUpServiceStarted;
	}

    public ByteBuffer getSoundModelInUse() {
        return this.soundModelInUse;
    }

    // Returns the sound model (in byte buffer form) that's currently selected
    public ByteBuffer getSelectedSoundModel() {
        Log.v(TAG, "getSelectedSoundModel: with keyword= " + keyword + ", username= " + username);
        String filePath = generateSoundModelFilePath(this.keyword, this.username);
        ByteBuffer bb = null;
        try {
            bb = Utils.readByteBufferFromFile(filePath);
        } catch (IOException e) {
            Log.e(TAG, "getSelectedSoundModel: failed to read in user-independent SM");
            e.printStackTrace();
        }
        return bb;
    }

    // Sets the sound model to the global variable once a sound model
    // has been extended with a call to extend.
    public void setExtendedSoundModel(int size) {
        this.extendedSoundModel = ByteBuffer.allocate(size);
    }

    // Gets a sound model once it has been extended with a call to extend
    public ByteBuffer getExtendedSoundModel() {
        if (null == this.extendedSoundModel) {
            Log.v(TAG, "getExtendedSoundModel: must call setExtendedSoundModel before get");
        }
        return this.extendedSoundModel ;
    }

    // Saves the selected sound model to a file on the internal storage
    public void saveSoundModelToFile() {
        Log.v(TAG, "saveSoundModelToFile");
        String filePath = generateSoundModelFilePath(this.keyword, this.username); //新的文件的路径：/../../HelloIUNI_JK.vwm
        Log.v(TAG, "outputExtendedSoundModel: filePath= " + filePath);
        Utils.saveByteBufferToFile(this.extendedSoundModel, filePath);
        
        Log.v(MYTAG, "file.exist()------------------------------>>"+new File("filePath").exists());
    }

    // Returns whether a sound model is currently registered
    public boolean isASoundModelRegistered() {
        if (null == this.soundModelInUse) {
            return false;
        } else {
            return true;
        }
    }

    // Returns the keyword only sound model associated with the currently selected sound model
    public ByteBuffer getKeywordOnlySoundModel() {
        Log.v(MYTAG, "getKeywordOnlySoundModel: keyword= " + this.keyword);
        if (null == this.keywordOnlySoundModel) {
            Log.v(MYTAG, "getKeywordOnlySoundModel: generating with keyword= " + this.keyword);
            String filePath = generateSoundModelFilePath(this.keyword, null);
            Log.v(MYTAG, "filePath="+filePath);
            try {
                this.keywordOnlySoundModel = Utils.readByteBufferFromFile(filePath);
            } catch (IOException e) {
                Log.e(MYTAG, "getKeywordOnlySoundModel: failed to read in user-independent SM");
                e.printStackTrace();
            }
        }
        return this.keywordOnlySoundModel;
    }

    public ShortBuffer[] getUserRecordings() {
        return this.userRecordings;
    }

    public ConfidenceData getConfidenceData() {
        return this.confidenceData;
    }

    // Returns a shortBuffer of the last user training recorded
    public ShortBuffer getLastUserRecording() {
        Log.v(TAG, "getUserRecording: getNumUserRecordings() before insert= " + getNumUserRecordings());
        try {
            String filePath = getLastUserRecordingFilePath();
            this.userRecordings[this.numUserRecordings++] = Utils.readWavFile(filePath);
        } catch (IOException e) {
            Log.e(TAG, "Unable to readWaveFile: " + e.getMessage());
        }
        Log.v(TAG, "getUserRecording: getNumUserRecordings() after insert= " + getNumUserRecordings());
        return this.userRecordings[getNumUserRecordings()-1];
    }

    // Returns a filepath for the last user training recorded
    public String getLastUserRecordingFilePath() {
        String recordingNumber = Integer.toString(1 + this.numUserRecordings);
        String filePath = Global.APP_PATH + "/" + RECORDINGS_FILE_NAME + recordingNumber + RECORDINGS_FILE_EXT;
        Log.v(TAG, "getLastUserRecordingFilePath: filePath= " + filePath);
        return filePath;
    }

    // Removes the recording files
    public void removeExistingRecordingFiles() {
        for (int i=0; i<TRAINING_RECORDINGS_COUNT; i++) {
            String recordingNumber = Integer.toString(1 + i);
            String filePath = Global.APP_PATH + "/" + RECORDINGS_FILE_NAME + recordingNumber + RECORDINGS_FILE_EXT;
            
            //重新录制时，文件路径
            Log.v(MYTAG, "重新录制时，文件路径:::removeExistingRecordingFiles: filePath= " + filePath);
            File recordingFile = new File(filePath);
            if(recordingFile.exists()) {
                recordingFile.delete();
                Log.v(MYTAG, "removeExistingRecordingFiles: file deleted= " + filePath);
            }
        }
    }

    // Resets the user recordings for the next training session
    public void removeUserRecordings() {
        Log.v(MYTAG, "removeUserRecordings: getNumUserRecordings() before remove= " + getNumUserRecordings());
        Arrays.fill(this.userRecordings, null);
        this.numUserRecordings = 0;
        Log.v(MYTAG, "removeUserRecordings: getNumUserRecordings() after remove= " + getNumUserRecordings());
    }

    public int getNumUserRecordings() {
        return this.numUserRecordings;
    }

    // Removes the last recording trained by a user when it's unsuccessful
    public void discardLastUserRecording() {
        Log.v(TAG, "discardLastUserRecording: getNumUserRecordings() before discard= " + getNumUserRecordings());
        this.numUserRecordings--;
        Log.v(TAG, "discardLastUserRecording: getNumUserRecordings() after discard= " + getNumUserRecordings());
    }

    public int getDetectionMode() {
        if (userVerificationEnabled) {
            return ListenTypes.USER_KEYWORD_DETECTION_MODE;
        } else {
            return ListenTypes.KEYWORD_ONLY_DETECTION_MODE;
        }
    }


    // Settings setters and getters

    public void setKeywordThreshold(String threshold) {
        keywordThreshold = Integer.parseInt(threshold.trim());
    }

    public int getKeywordThreshold() {
        return keywordThreshold;
    }

    public String getKeywordThresholdString() {
        return String.valueOf(keywordThreshold);
    }

    public void setUserThreshold(String threshold) {
        userThreshold = Integer.parseInt(threshold.trim());
    }

    public int getUserThreshold() {
        return userThreshold;
    }

    public String getUserThresholdString() {
        return String.valueOf(userThreshold);
    }

    public void setEnableListen(boolean checked) {
        enableListen = checked;
    }

    public boolean getEnableListen() {
        return enableListen;
    }

    public void setEnableVoiceWakeup(boolean checked) {
        enableVoiceWakeup = checked;
    }

    public boolean getEnableVoiceWakeup() {
        return enableVoiceWakeup;
    }

    public void setUserVerification(boolean checked) {
        userVerificationEnabled = checked;
    }

    public boolean getUserVerification() {  
        return userVerificationEnabled;
    }

    public void setFailureFeeback(boolean checked) {
        failureFeedbackEnabled = checked;
    }

    public boolean getFailureFeedback() {
        return failureFeedbackEnabled;
    }
    public void setTone(boolean checked) {
        toneEnabled = checked;
    }

    public boolean getTone() {
        return toneEnabled;
    }

    public void setLaunchVoiceqna(boolean b) {
        launchVoiceqna = b;
    }

    public boolean  getLaunchVoiceqna() {
        return launchVoiceqna;
    }

    /*public void setDetail(boolean b) {
        showAdvancedDetail = b;
    }*/
}
