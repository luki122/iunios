package com.aurora.reject.receiver;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.os.Parcelable;
import android.util.Log;

public class WifiConnectReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(final Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
			Parcelable parcelableExtra = intent
					.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			if (null != parcelableExtra) {
				NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
				State state = networkInfo.getState();
				boolean isConnected = state == State.CONNECTED;

				if (isConnected) {
					System.out.println("isConnected");
					new Thread(){
						public void run() {
							GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance(); gc.setTime(new Date()); 
							int i= gc.get(Calendar.DAY_OF_WEEK); //返回值为1的话为周日 返回值为7的话 为周六 
							if(i==2){
								
							}
						};
					}.start();
					
				} else {
					System.out.println("isNotConnected");
				}
			}
		}
		
	}

}
