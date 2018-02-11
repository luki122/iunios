package com.secure.receive;


import com.secure.imageloader.ImageLoader;
import com.secure.utils.LogUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class iconUpdateReceiver extends BroadcastReceiver{
	private final String TAG = iconUpdateReceiver.class.getName();

	@Override
	public void onReceive(Context context, Intent intent) { 
		LogUtils.printWithLogCat(TAG, "action:"+intent.getAction());
		
		if("com.aurora.action.pulbicres.update".equals(intent.getAction())){
		/*	ArrayList<String> str = intent.getStringArrayListExtra("updates");
		
			for(int i = 0 ; i < str.size(); i++)
			{
				LogUtils.printWithLogCat(TAG, "action the pack="+str.get(i));
			}*/
			ImageLoader.releaseObject();
		}
	}	
}

   
