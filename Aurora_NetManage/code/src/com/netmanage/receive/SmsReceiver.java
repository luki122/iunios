package com.netmanage.receive;

import com.netmanage.model.CorrectFlowBySmsModel;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SmsReceiver extends BroadcastReceiver {  
	  
	   @Override
		public void onReceive(Context context, Intent intent) {		   
		   if(intent != null && 
				 intent.getAction() != null &&
				 intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
			   CorrectFlowBySmsModel model = CorrectFlowBySmsModel.getInstance();
			   if(model != null){
				   model.dealSms(intent);
			   }
		   }	      
	}
	   
}