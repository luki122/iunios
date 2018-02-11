package com.aurora.apihook.volumepanel;

import com.aurora.apihook.Hook;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;
import com.aurora.apihook.ClassHelper;

import android.media.IAudioService;
import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioService;
import android.util.Log;
import android.os.ServiceManager;
public class VolumePanelHook implements Hook{
	private static final String TAG = "VolumePanelHook";
	private static final boolean DEBUG = true;
	private AuroraVolumePanel mPanel;
	/**
	 * Context AudioService
	 * @param param
	 */
	public void after_AudioService(MethodHookParam param){
		Context context = (Context) param.args[0];
		AudioService service = (AudioService) param.thisObject;
		mPanel = new AuroraVolumePanel(context, service);
	}
	/**
	 * int int
	 * @param param
	 */
	public void after_sendVolumeUpdate(MethodHookParam param){
	
		Object obj = param.thisObject;
		int streamType = (Integer)param.args[0];
		int flags = (Integer)param.args[1];
		Log.e(TAG, "after_postMasterVolumeChanged 000");
		if(mPanel != null){
			Log.e(TAG, "after_postMasterVolumeChanged");
			mPanel.postVolumeChanged(streamType,flags);
		}
	}
	
	/**
	 * int int
	 * @param param
	 */
	public void after_sendMasterVolumeUpdate(MethodHookParam param){
		int flags = (Integer)param.args[0];
		if(mPanel != null){
			mPanel.postMasterVolumeChanged(flags);
			Log.e(TAG, "after_sendMasterVolumeUpdate");
		}
	}
	
	
	/**
	 * boolean
	 * @param param
	 */
	public void after_sendMasterMuteUpdate(MethodHookParam param){
		int flags = (Integer)param.args[0];
		if(mPanel != null){
			mPanel.postMasterMuteChanged(flags);
			Log.e(TAG, "after_sendMasterMuteUpdate");
		}
	}
	
	
	/**
	 * void
	 * @param param
	 */
	public void after_handleConfigurationChanged(MethodHookParam param){
		Context context = (Context)param.args[0];
		if(mPanel != null){
			if(context != null){
				Configuration config = context.getResources().getConfiguration();
				mPanel.setLayoutDirection(config.getLayoutDirection());
			}
			Log.e(TAG, "after_handleConfigurationChanged");
		}
	}
	
	
	
	
	
}
