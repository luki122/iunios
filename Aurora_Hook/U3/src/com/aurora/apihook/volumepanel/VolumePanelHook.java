package com.aurora.apihook.volumepanel;

import com.aurora.apihook.Hook;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;

import android.content.Context;
import android.media.AudioService;
import android.util.Log;
public class VolumePanelHook implements Hook{
	
	private AuroraVolumePanel mPanel;
	/**
	 * Context AudioService
	 * @param param
	 */
	public void after_VolumePanel(MethodHookParam param){
		Context context = (Context) param.args[0];
		AudioService service = (AudioService) param.args[1];
		mPanel = new AuroraVolumePanel(context, service);
		Log.e("vol", "create volume panel");
		if(mPanel != null){
			Log.e("vol", "after_VolumePanel");
		}
	}
	
	/**
	 * void
	 * @param param
	 */
	public void before_updateStates(MethodHookParam param){
		param.setResult(null);
		if(mPanel != null){
			mPanel.updateStates();
		}
	}
	
	/**
	 * int int
	 * @param param
	 */
	public void before_postVolumeChanged(MethodHookParam param){
		param.setResult(null);
		int streamType = (Integer) param.args[0];
		int flags = (Integer) param.args[1];
		if(mPanel != null){
			Log.e("vol", "before_postVolumeChanged");
			mPanel.postVolumeChanged(streamType, flags);
		}
	}
	
	/**
	 * int int
	 * @param param
	 */
	public void before_postRemoteVolumeChanged(MethodHookParam param){
		param.setResult(null);
		int streamType = (Integer) param.args[0];
		int flags = (Integer) param.args[1];
		if(mPanel != null){
			mPanel.postRemoteVolumeChanged(streamType, flags);
		}
	}
	
	
	/**
	 * boolean
	 * @param param
	 */
	public void before_postRemoteSliderVisibility(MethodHookParam param){
		param.setResult(null);
		boolean visible = (Boolean) param.args[0];
		if(mPanel != null){
			mPanel.postRemoteSliderVisibility(visible);
		}
	}
	
	
	/**
	 * void
	 * @param param
	 */
	public void before_postHasNewRemotePlaybackInfo(MethodHookParam param){
		param.setResult(null);
		if(mPanel != null){
			mPanel.postHasNewRemotePlaybackInfo();
		}
	}
	
	
	
	/**
	 * int
	 * @param param
	 */
	public void before_postMasterVolumeChanged(MethodHookParam param){
		param.setResult(null);
		int flags = (Integer) param.args[0];
		if(mPanel != null){
			mPanel.postMasterVolumeChanged(flags);
		}
	}
	
	/**
	 * int  int
	 * @param param
	 */
	public void before_postMuteChanged(MethodHookParam param){
		param.setResult(null);
		int streamType = (Integer) param.args[0];
		int flags = (Integer) param.args[1];
		if(mPanel != null){
			mPanel.postMuteChanged(streamType, flags);
		}
	}
	
	/**
	 * int
	 * @param param
	 */
	public void before_postMasterMuteChanged(MethodHookParam param){
		param.setResult(null);
		int flags = (Integer) param.args[0];
		if(mPanel != null){
			mPanel.postMasterMuteChanged(flags);
		}
	}
	
	/**
	 * int
	 * @param param
	 */
	public void before_postDisplaySafeVolumeWarning(MethodHookParam param){
		param.setResult(null);
		int flags = 0;
		if(param.args != null && param.args.length>0){
			flags = (Integer) param.args[0];
		}
		if(mPanel != null){
			mPanel.postDisplaySafeVolumeWarning(flags);
		}
	}
	
	
	
}
