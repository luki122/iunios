package com.netmanage.receive;

import com.netmanage.model.NotificationModel;
import com.netmanage.service.WatchDogService;
import com.netmanage.utils.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		  Log.i(BootReceiver.class.getName(),"onReceive");
		  //现在没有用到数据库，所以不需要初始化数据库了    
		  Intent intentService = new Intent(context,WatchDogService.class);
		  context.startService(intentService);
		  
		  NotificationModel.getInstance(context).checkSimChangeNotify(Utils.getImsi(context));
	}	
}

   
