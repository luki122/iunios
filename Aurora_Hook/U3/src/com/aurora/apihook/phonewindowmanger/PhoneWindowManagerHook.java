package com.aurora.apihook.phonewindowmanger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.Hook;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;
import com.aurora.apihook.phonewindowmanger.FrameAnimation;
import android.view.WindowManagerPolicy.WindowManagerFuncs;

public class PhoneWindowManagerHook implements Hook {

	private static final String TAG = "PhoneWindowManagerHook";

	private ShutdownDialogInternal dialog;
	
	private boolean mShow = false;
	FrameAnimation mAirmodeAnim,mRebootAnim,mShutDownAnim;
	public void after_init(MethodHookParam param){
		Context mContext = (Context) ClassHelper.getObjectField(
				param.thisObject, "mContext");
		mAirmodeAnim = new FrameAnimation(mContext,
		FrameAnimation.ICON_AIRMODE_DOWN);
		mRebootAnim = new FrameAnimation(mContext,
		FrameAnimation.ICON_REBOOT_DOWN);
		mShutDownAnim = new FrameAnimation(mContext,
		FrameAnimation.ICON_SHUT_DOWN);
	}
	
	/**
	 * shutdown dialog
	 * 
	 * @param param
	 */
	public void before_showGlobalActionsDialog(final MethodHookParam param) {
		param.setResult(null);
		Context mContext = (Context) ClassHelper.getObjectField(
				param.thisObject, "mContext");
		if(dialog != null){
			dialog.dismiss();
		}
			WindowManagerFuncs wmf = (WindowManagerFuncs) ClassHelper
					.getObjectField(param.thisObject, "mWindowManagerFuncs");
			dialog = new ShutdownDialogInternal(mContext, wmf);
			dialog.setFrameAnimation(mAirmodeAnim,mRebootAnim,mShutDownAnim);
			WindowManager.LayoutParams attrs = dialog.getWindow()
					.getAttributes();
			attrs.setTitle("GlobalActions");
			dialog.getWindow().setWindowAnimations(
					com.aurora.R.style.AuroraShutdownDialogAnim);
			dialog.getWindow().setAttributes(attrs);
			dialog.getWindow().setType(
					WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
		dialog.show();
		dialog.getWindow().getDecorView()
				.setSystemUiVisibility(View.STATUS_BAR_DISABLE_EXPAND);
		
	}

	/**
	 * check permission for toast
	 * 
	 * @param param
	 */
	public void after_checkAddPermission(MethodHookParam param) {
		Log.e(TAG, "after_checkAddPermission");
		WindowManager.LayoutParams wmp = (LayoutParams) param.args[0];
		if (wmp.type == WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
				|| wmp.type == WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG) {
			param.setResult(0);
		}

	}

	public void before_interceptKeyBeforeQueueing(MethodHookParam param) {
		KeyEvent event = (KeyEvent) param.args[0];
		final boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
		final int keyCode = event.getKeyCode();
		Context context = (Context) ClassHelper.getObjectField(
				param.thisObject, "mContext");
		if (keyCode == KeyEvent.KEYCODE_POWER) {
			if (!down) {
				auroraSendKeyCodePowerBroadCast(context);
			}
		}
	}

	private void auroraSendKeyCodePowerBroadCast(Context context) {
		Intent intent = new Intent(
				"android.intent.action.ACTION_KEYCODE_POWER_SHORTPRESS");

		context.sendBroadcast(intent);
	}

}
