package com.secure.receive;

import java.util.ArrayList;
import java.util.List;

import com.secure.model.ConfigModel;
import com.secure.model.LBEmodel;
import com.secure.model.LBEmodel.BindServiceCallback;
import com.secure.utils.LogUtils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 监听安装在sd卡上应用可用性的改变
 * @author chengrq
 *
 */
public class ExternalAppChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {	
		if(intent == null){
			return ;
		}		
        String[] packages = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
        if(packages != null && packages.length > 0){      	
        	dealFunc(intent.getAction(),packages,context);
        }
	}	
	
	private void dealFunc(final String action, final String[] packages, final Context context) {
		if(LBEmodel.getInstance(context).isBindLBEService()){
			updateAppsInfo(action,packages,context);			
		}else{
			LBEmodel.getInstance(context).bindLBEService(new BindServiceCallback(){
				@Override
				public void callback(boolean result) {
					updateAppsInfo(action,packages,context);
				}}
			);
		}
    }
	
	private void updateAppsInfo(String action,String[] packages,Context context){
		List<String> pkgList = new ArrayList<String>();
		String pkgStr ="";
		for(int i=0;i<packages.length;i++){
			pkgStr=pkgStr+packages[i]+"; ";
			pkgList.add(packages[i]);
		}
		/**
		 * 注意：sd拔出，sd插入，记录安装在sd卡中应用的对象地址改变，
		 * 类似“应用详情”界面 ，“应用权限”界面，“缓存详情”界面等，这些界面却是全局变量（固定地址）记录的应用信息，
		 * 所以要考虑对象数据的唯一和同步
		 */
		LogUtils.printWithLogCat(
				ExternalAppChangeReceiver.class.getName(), 
				"ACTION="+action+",packages="+pkgStr);
		if(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action)){
			ConfigModel.getInstance(context).getAppInfoModel().externalAppAvailable(pkgList);
		}else if(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(action)){
			ConfigModel.getInstance(context).getAppInfoModel().externalAppUnAvailable(pkgList);
		}
	}
}

   
