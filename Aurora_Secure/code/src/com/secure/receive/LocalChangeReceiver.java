package com.secure.receive;

import com.aurora.secure.R;
import com.secure.activity.SettingActivity;
import com.secure.interfaces.LBEServiceBindSubject;
import com.secure.model.LBEmodel;
import com.secure.model.LBEmodel.BindServiceCallback;
import com.secure.utils.ApkUtils;
import com.secure.utils.LogUtils;
import com.secure.utils.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class LocalChangeReceiver extends BroadcastReceiver {
	public static final String ACTION_LOCALE_CHANGED = "android.intent.action.LOCALE_CHANGED";
	/**
	 * 打开或关闭LBE服务
	 * @param isOpen
	 */
	private void openOrCloseLBEService(final boolean isOpen,final Context context){
		if(isOpen == ApkUtils.isLBEServiceConnect()){
			return ;
		}
		
		if(LBEmodel.getInstance(context).isBindLBEService()){		
			if(isOpen){
				LBEmodel.getInstance(context).openLBEFunc();				
			}else{
				LBEmodel.getInstance(context).closeLBEFunc();
			}
			updateAllAppsPermission(isOpen,context);
		}else{
			LBEmodel.getInstance(context).bindLBEService(new BindServiceCallback(){
				@Override
				public void callback(boolean result) {
					if(isOpen){
						LBEmodel.getInstance(context).openLBEFunc();
					}else{
						LBEmodel.getInstance(context).closeLBEFunc();
					}
					updateAllAppsPermission(isOpen,context);
				}}
			);
		}
	}
	
	private void updateAllAppsPermission(final boolean isOpen,final Context context){
		
		new Thread() {
			@Override
			public void run() {
				if(isOpen){
					//开启权限监控，需要重新加载所有应用的权限，比较耗时，所以弹等待对话框
					ApkUtils.updateAllAppsPermission(context);
				
				}			
				if(LBEServiceBindSubject.getInstanceOfNotCreate() != null){
					LBEServiceBindSubject.getInstanceOfNotCreate().
					   notifyObserversOfBindStateChangeFunc(isOpen);
				}
			}
		}.start();
	}
	@Override
	public void onReceive(final Context context, Intent intent) {
		Log.i("zhangwei",
				"zhangwei onReceive  intent.getAction(): "
						+ intent.getAction());
		if (intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
			if(Utils.isChinaSetting())
			{
				Log.i("zhangwei",
						"zhangwei the local is china:");
				openOrCloseLBEService(true,context);
			}
			else
			{
				Log.i("zhangwei",
						"zhangwei the local is not china:");
				openOrCloseLBEService(false,context);
			}
		}
	}
}
