package com.android.contacts.appwidget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.android.contacts.ContactLoader;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.calllog.CallLogQuery;
import com.android.contacts.calllog.CallLogQueryHandler;
import com.android.contacts.calllog.DefaultVoicemailNotifier.NewCallsQuery;
import com.android.contacts.socialwidget.SocialWidgetSettings;
import com.android.contacts.util.ContactBadgeUtil;
import com.android.contacts.util.IntentFactory;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.android.contacts.R;
import com.mediatek.sensorhub.ContextInfo;

public class MyAppWidgetProvider extends AppWidgetProvider{  

	private static final String TAG = "liyang-MyAppWidgetProvider"; 
	public static final String COLLECTION_VIEW_ACTION = "com.android.contacts.COLLECTION_VIEW_ACTION";
	public static final String BT_REFRESH_ACTION = "com.android.contacts.BT_REFRESH_ACTION";
	public static final String appwidgetproviderService="appwidgetproviderService";
	private Handler mHandler=new Handler(); 
	
	@Override  
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {  
		Log.d(TAG, "onUpdate");  
		Log.d(TAG,"appwidget Count:"+appWidgetIds.length);

		Intent mIntent = new Intent();
		mIntent.setAction("appwidgetproviderService");
		mIntent.putExtra("onstart", true);
		mIntent.putExtra("delay", 300);
		mIntent.setPackage(ContactsApplication.context.getPackageName());//应用的包名
		ContactsApplication.context.startService(mIntent);

	}
	@Override  
	public void onDeleted(Context context, int[] appWidgetIds) {  
		Log.d(TAG, "onDeleted");  
	}  
	

//	private Runnable mRunnable=new Runnable(){    
//		public void run() {    
//			Intent mIntent = new Intent();
//			mIntent.setAction("appwidgetproviderService");
//			mIntent.putExtra("onstart", true);
//			mIntent.setPackage(ContactsApplication.context.getPackageName());//应用的包名
//			ContactsApplication.context.startService(mIntent);
//		} 
//	};
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();    	
		Log.d(TAG, "onReceive2 : "+intent.getAction());
		if (action.equals(COLLECTION_VIEW_ACTION)) {
			// 接受“gridview”的点击事件的广播
			int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
			String contactUri=intent.getStringExtra("contactUri");
			String contactNumber=intent.getStringExtra("contactNumber");

			if(TextUtils.isEmpty(contactNumber)||TextUtils.isEmpty(contactUri)) return;

			Intent intent1 = IntentFactory
					.newDialNumberIntent(contactNumber);
			intent1.putExtra("contactUri", contactUri);
			ContactsApplication.context.startActivity(intent1);
		} else if (action.equals(BT_REFRESH_ACTION)) {
			// 接受“bt_refresh”的点击事件的广播
		}else if(action.equals("android.intent.action.PHONE_STATE")){
			Log.d(TAG, "android.intent.action.PHONE_STATE");	

			Intent mIntent = new Intent();
			mIntent.setAction("appwidgetproviderService");
			mIntent.putExtra("onstart", false);
			mIntent.setPackage(ContactsApplication.context.getPackageName());//应用的包名
			ContactsApplication.context.startService(mIntent);
		}else if(action.equals("android.appwidget.action.APPWIDGET_UPDATE1")){
			Intent mIntent = new Intent();
			mIntent.setAction("appwidgetproviderService");
			mIntent.putExtra("onstart", false);
			mIntent.setPackage(ContactsApplication.context.getPackageName());//应用的包名
			ContactsApplication.context.startService(mIntent);
		}

		super.onReceive(ContactsApplication.context, intent);
	}

	@Override  
	public void onEnabled(Context context) {  
		Log.d(TAG, "onEnabled");  

	}  

	@Override  
	public void onDisabled(Context context) {  

	} 
}
