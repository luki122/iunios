package com.android.settings;

/*Gionee fangbin 20120619 added for CR00622030*/
import android.app.Application;
import android.content.res.Configuration;


// GIONEE liuran 2012-8-27 add for CR00680899 start
import android.util.Log;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemProperties;

import com.android.settings.config.SettingConfigUtils;
import com.gionee.settings.push.NotifyPushReceiver;
// GIONEE liuran 2012-8-27 add for CR00680899 end

public class GnSettingsApp extends Application {

	// GIONEE liuran 2012-8-27 add for CR00680899 start
	private final String TAG = "GnSettingsApp";
	private final boolean mGpeSupport = SystemProperties.get("ro.gn.gpe.support").equals("yes");
	// GIONEE liuran 2012-8-27 add for CR00680899 end
	
    @Override
    public void onCreate() {
        // Gionee fangbin 20120719 modified for CR00651589 start
        if (GnSettingsUtils.getThemeType(getApplicationContext())
                .equals(GnSettingsUtils.TYPE_LIGHT_THEME)) {
            setTheme(R.style.GnSettingsLightTheme);
        } else {
            setTheme(R.style.GnSettingsDarkTheme);
        }
        
    	Log.e("SettingConfigUtils","settings  onCreate" );
        SettingConfigUtils.parseConfig();
        // Gionee fangbin 20120719 modified for CR00651589 end
        super.onCreate();
        
        // GIONEE liuran 2012-8-27 add for CR00680899 start
        if(mGpeSupport)
        {
        	//ACTION_CLOSE_SYSTEM_DIALOGS action can only be sent to those who register it dynamically
        	//rather than those receiver who register it in AndroidManifest.xml
        	//register this listener for PushCenter to close our system dialog
            IntentFilter closeSysDlg = new IntentFilter();
            closeSysDlg.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
    		registerReceiver(mCloseSysDlgReceiver, closeSysDlg);
        }
		// GIONEE liuran 2012-8-27 add for CR00680899 end
        
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // Gionee fangbin 20120719 modified for CR00651589 start
        if (GnSettingsUtils.getThemeType(getApplicationContext())
                .equals(GnSettingsUtils.TYPE_LIGHT_THEME)) {
            setTheme(R.style.GnSettingsLightTheme);
        } else {
            setTheme(R.style.GnSettingsDarkTheme);
        }
        // Gionee fangbin 20120719 modified for CR00651589 end
        super.onConfigurationChanged(newConfig);
    }
    
    // GIONEE liuran 2012-8-27 add for CR00680899 start
    @Override
    public void onTerminate() {
    	// TODO Auto-generated method stub
    	super.onTerminate();
    	
    	if(mGpeSupport)
    	{
    		unregisterReceiver(mCloseSysDlgReceiver);
    	}
    }
    
	private BroadcastReceiver mCloseSysDlgReceiver = new BroadcastReceiver() 
	{
	    public void onReceive(Context context, Intent intent) 
	    {
	        String action = intent.getAction();
	        
	        if(Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action))
	        {
	        	Intent closePushDlg = new Intent();
	        	
	        	closePushDlg.setAction("com.gionee.action.CLOSE_SYSTEM_DIALOGS");
	        	closePushDlg.setClassName(GnSettingsApp.this, NotifyPushReceiver.class.getName());
	        	
	        	sendBroadcast(closePushDlg);
	        }
	    }
	};
	// GIONEE liuran 2012-8-27 add for CR00680899 end
}
