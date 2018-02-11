package com.aurora.apihook.activity;

import java.util.Iterator;
import java.util.Set;

import android.app.Activity;
import android.app.KeyguardManager;
import aurora.app.AuroraActivity;
import android.app.NotificationManager;

import com.aurora.apihook.XC_MethodHook.MethodHookParam;
import android.content.Intent;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.WindowManager;
public class ActivityHook {

	private boolean onResume = false;
	public void after_onResume(MethodHookParam param){
		Activity activity = (Activity)param.thisObject;
//		Log.e("activity", "after_onResume");
		
		if(!(activity instanceof AuroraActivity)){
			KeyguardManager mKeyguardManger = (KeyguardManager)activity.getSystemService(Context.KEYGUARD_SERVICE);
//			Log.e("activity", "changeStatusBar 0");
			if(!mKeyguardManger.isKeyguardLocked()){
				changeStatusBar(activity);
				onResume = true;
//				Log.e("activity", "changeStatusBar");
			}
			
		}
	}
	
	public void changeStatusBar(Activity activity){
		 NotificationManager notificationManager = ( NotificationManager )activity.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(activity);
        builder.setSmallIcon(com.aurora.R.drawable.aurora_menu_divider_line);
        String tag="aurorawhiteBG653";
        notificationManager.notify(tag, 0, builder.build());
        notificationManager.cancel(tag, 0);
	}
	
	private boolean statusBarIsShowing(Activity activity){
		WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
		return ((attrs.flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
	
	public void after_onWindowFocusChanged(MethodHookParam param){
		Activity activity = (Activity) param.thisObject;

		// if(!onResume){
		boolean hasFocus = activity.hasWindowFocus();
		Set<String> categories = activity.getIntent().getCategories();
//		categories.iterator()
		boolean isLauncher = false;
		if (categories != null && categories.size() >0) {
			Iterator<String> it = categories.iterator();
			while (it.hasNext()) {
				String catHome = (String) it.next();
				if (Intent.CATEGORY_HOME.equals(catHome)) {
					isLauncher = true;
					break;
				}
			}
		}
		if (hasFocus) {
			KeyguardManager mKeyguardManger = (KeyguardManager) activity
					.getSystemService(Context.KEYGUARD_SERVICE);
			if (!mKeyguardManger.isKeyguardLocked()) {
				if (!(activity instanceof AuroraActivity) && isLauncher) {
					changeStatusBar(activity);
				}else{
					if(!statusBarIsShowing(activity)){
						changeStatusBar(activity);
					}
				}

			}
		}
	}
	
}
