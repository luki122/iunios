package com.netmanage.receive;

import com.netmanage.data.CorrectFlowBySmsData;
import com.netmanage.interfaces.SimChangeSubject;
import com.netmanage.model.CorrectFlowModel;
import com.netmanage.model.NotificationModel;
import com.netmanage.service.WatchDogService;
import com.netmanage.utils.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SimChangeReceiver extends BroadcastReceiver {
	
	private final String TAG = "SimChangeReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {	
		String curImsi = Utils.getImsi(context);
		Log.i(TAG, "SimChangeReceiver onReceive curImsi: " + curImsi);
		CorrectFlowBySmsData.getInstance(context).checkAndDealSimChange(curImsi);				
		dealForSimChange(context,curImsi);
	}
	
	private void dealForSimChange(Context context,String curImsi){
		Intent intentService = new Intent(context,WatchDogService.class);
		context.startService(intentService);		  
		  // 20140320 billy
		CorrectFlowModel.getInstance(context).checkResetCorrectByChangeSimAndNoSetPackage();
		CorrectFlowModel.getInstance(context).saveLastImsi();
		NotificationModel.getInstance(context).checkSimChangeNotify(curImsi);
		SimChangeSubject.getInstance().notifyObservers();
	}
}

   
