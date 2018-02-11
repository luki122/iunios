package com.secure.receive;

import com.secure.activity.PermissionRemindActivity;
import com.secure.data.PermissionRemindData;
import com.secure.model.LBEmodel;
import com.secure.model.LBEmodel.BindServiceCallback;
import com.secure.model.PermissionRemindModel;
import com.secure.utils.ApkUtils;
import com.secure.utils.LogUtils;
import com.secure.utils.StringUtils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

public class PermissionRemindReceiver extends BroadcastReceiver {
	private final String TAG = PermissionRemindReceiver.class.getName();
	private final Handler handler = new Handler();
	private Context context;
	private String packageName;
	private int DELAY_TIME = 1200;
	
	@Override
	public void onReceive(final Context context, Intent intent) {
		LogUtils.printWithLogCat(TAG,"onReceive");	
		if(intent == null || intent.getExtras() == null){
            return;
        }
		final String packageName = intent.getExtras().getString("package");
		if(StringUtils.isEmpty(packageName)){
			return ;
		}
		LogUtils.printWithLogCat(TAG,"onReceive,packageName:"+packageName);
		
		this.context = context;
		this.packageName = packageName;
		if(LBEmodel.getInstance(context).isBindLBEService()){
			handler.postDelayed(runnable, DELAY_TIME);
		}else{
			LBEmodel.getInstance(context).bindLBEService(new BindServiceCallback(){
				@Override
				public void callback(boolean result) {
					handler.postDelayed(runnable, DELAY_TIME);
				}}
			);
		}	
	}	
	
	 private Runnable runnable = new Runnable() {
	        public void run() {        	
	        	startPermissionRemindActivity(context,packageName);
	        }
	    };
	
	 
	private void startPermissionRemindActivity(Context context,String packageName){
		 if(context == null || StringUtils.isEmpty(packageName)){
				return ;
		 }
		 
		 PermissionRemindData perRemindData = PermissionRemindModel.
 				getInstance(context).getPermissionRemindData(packageName);
 		 if(perRemindData != null && perRemindData.getVersionCode() == 
 				ApkUtils.getApkVersionCode(context, packageName)){
 			LogUtils.printWithLogCat(TAG,"this app and this version is showed ");
 			return ;
 		 }
 		
		 Intent intent = new Intent();
	 	 intent.setClass(context, PermissionRemindActivity.class);
	 	 intent.putExtra("packageName", packageName);
	 	 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	 	 context.startActivity(intent); 
	}
}

   
