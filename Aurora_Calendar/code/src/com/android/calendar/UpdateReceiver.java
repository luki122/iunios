//Gionee <Author: lihongyu> <2013-04-11> add for CR000000 begin

package com.android.calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UpdateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		boolean haveVersion = arg1.getBooleanExtra("result", false);
		
		Log.d("upgrade", "UpdateReceiver1");
		
	    if(CalendarApplication.isEnableUpgrade()){
	    	CalendarApplication application  = 
	                (CalendarApplication)arg0.getApplicationContext();
	            application.setHaveUpgradeInfo(haveVersion);
	            
	            Log.d("upgrade", "UpdateReceiver2");
	    }

	}

}

 

//Gionee <Author: lihongyu> <2013-04-11> add for CR000000 end