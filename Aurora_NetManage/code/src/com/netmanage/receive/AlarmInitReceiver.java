package com.netmanage.receive;

import com.netmanage.data.CorrectFlowBySmsData;
import com.netmanage.model.CorrectFlowBySmsModel;
import com.netmanage.utils.AlarmUtils;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.util.Log;

public class AlarmInitReceiver extends BroadcastReceiver {
	private static String TAG = AlarmInitReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.v(TAG, "AlarmInitReceiver action:" + action);
        
        AlarmUtils.disableAlarm(context);
        
        CorrectFlowBySmsData correctFlowBySmsData = CorrectFlowBySmsData.getInstance(context);
		if(correctFlowBySmsData.getIsAutoCorrect()){
			Log.v(TAG, "AlarmInitReceiver Is arrow Auto Correct ");     
	        AlarmUtils.setNextAlert(context);
		}
    }
}
