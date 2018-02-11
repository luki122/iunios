package com.aurora.reject.receiver;

import com.aurora.reject.util.RejectApplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Notifi extends BroadcastReceiver {
    private int countCall;
    private int countSms;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if(intent.getAction().equals("AURORA_HANGUP_CALL")){
			boolean b=intent.getBooleanExtra("isCall", false);
			if(b){
				countCall=RejectApplication.getInstance().getCountCall()+1;
				RejectApplication.getInstance().setCountCall(countCall);
				System.out.println("countCall="+countCall);
			}else{
				countSms=RejectApplication.getInstance().getCountSms()+1;
				RejectApplication.getInstance().setCountSms(countSms);
				System.out.println("countSms="+countSms);
			}
		}

	}

}
