package com.aurora.apihook.activity;

import java.util.Iterator;
import java.util.Set;

import android.app.Activity;
import android.app.KeyguardManager;
import aurora.app.AuroraActivity;
import android.app.NotificationManager;

import com.aurora.apihook.XC_MethodHook.MethodHookParam;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.content.Intent;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.WindowManager;
public class ActivityHook {
	/**
	 * Uri for change statusbar colors
	 */
	public static final Uri STATUS_BAR_COLOR_URI = Uri.parse("content://"+"com.android.systemui.statusbar.phone"+"/"+"immersion"+"?notify=true");//("content://"+"com.android.systemui.statusbar.phone"+"/"+"immersion"+"?notify=true");
	
	/**
	 * key for stored status bar color in SystemUi process
	 */
	public static final String KEY_FOR_STATUS_BAR_COLOR = "imersion_color";
	
	/**
	 * used this value to change status bar icon's color to white
	 */
	private static final int STATUS_BAR_WHITE = 0;
	/**
	 * used this value to change status bar icon's color to black
	 */
	private static final int STATUS_BAR_BLACK = 1;
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
	private void updateStatusBarColor(Activity activity,int colorType){
		ContentResolver cr = activity.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(KEY_FOR_STATUS_BAR_COLOR,Integer.toHexString(colorType) );
        int count = 0;
        count = cr.update(STATUS_BAR_COLOR_URI, values, null, null);
        if(count==0){
        	cr.insert(STATUS_BAR_COLOR_URI, values);
        }
	}
	
	public void changeStatusBar(Activity activity){
		/* NotificationManager notificationManager = ( NotificationManager )activity.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(activity);
        builder.setSmallIcon(com.aurora.R.drawable.stat_notify_gmail);
        String tag="aurorawhiteBG653";
        notificationManager.notify(tag, 0, builder.build());*/
         updateStatusBarColor(activity,STATUS_BAR_WHITE);
	}
	
	private boolean statusBarIsShowing(Activity activity){
		WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
		return ((attrs.flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
	
	public void after_onWindowFocusChanged(MethodHookParam param){
		Activity activity = (Activity) param.thisObject;

		boolean hasFocus = activity.hasWindowFocus();
		Set<String> categories = activity.getIntent().getCategories();
		
		boolean isLauncher = false;
		if (categories != null && categories.size() > 0) {
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
