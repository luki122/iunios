package com.aurora.weatherdata.util;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

public class CustomeToast {

	private static Toast toast = null;
	private static String lastShowMsg = null;
	private static Handler mHandler = new Handler();
	private static int DURATION = 2000;
	private static Runnable runnable = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(toast != null)
				toast.cancel();
			toast = null;
			lastShowMsg = null;
		}
	};
	
	public static void show(Context context,int msgId){
		if(toast == null)
		{
			toast = Toast.makeText(context, msgId, DURATION);
			mHandler.postDelayed(runnable, DURATION);
			toast.show();
		}
	}
	
	
}
