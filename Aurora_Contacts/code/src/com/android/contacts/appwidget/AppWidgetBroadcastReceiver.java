package com.android.contacts.appwidget;

import java.util.ArrayList;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.android.contacts.R;

public class AppWidgetBroadcastReceiver extends BroadcastReceiver {  


	private static final String TAG = "AppWidgetBroadcastReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();    	
		Log.d(TAG, "onReceive3 : "+intent.getAction()+" this:"+this);
		if(action.equals("android.intent.action.PHONE_STATE")){
			Log.d(TAG, "android.intent.action.PHONE_STATE");	
		}
	}  

}
