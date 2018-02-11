package com.netmanage.receive;

import com.netmanage.data.CorrectFlowBySmsData;
import com.netmanage.model.CorrectFlowBySmsModel;
import com.netmanage.utils.AlarmUtils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
	private String TAG = AlarmReceiver.class.getName();
	
	public void onReceive(Context context, Intent intent) {		
		Log.i(TAG,"AlarmReceiver onReceive");
		if(!AlarmUtils.isValidAlarmReceiver()){
			Log.i(TAG,"AlarmReceiver is unValid Alarm Receiver");
			return ;
		}
			
		//没有设置套餐，依旧可以校正流量
		CorrectFlowBySmsModel.getInstance(context).startAutoCorrectFlow();
		
		CorrectFlowBySmsData correctFlowBySmsData = CorrectFlowBySmsData.getInstance(context);
		if(correctFlowBySmsData.getIsAutoCorrect()){
			AlarmUtils.setNextAlert(context);
		}		
	}
}
