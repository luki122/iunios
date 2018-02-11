package com.aurora.thememanager.receiver;

import com.aurora.thememanager.activity.Action;
import com.aurora.utils.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ThemeChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		if(Action.ACTION_THEME_CHANGED.equals(action)){
			
		}
	}

}
