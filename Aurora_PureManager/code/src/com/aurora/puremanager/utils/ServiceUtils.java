package com.aurora.puremanager.utils;

import android.content.Context;
import android.content.Intent;
import com.aurora.puremanager.model.ConfigModel;
import com.aurora.puremanager.service.WatchDogService;

public class ServiceUtils {
	public static void startServiceIfNeed(Context context){
		if(context == null){
			return ;
		}
		if(isNeedStartService(context)){
			context.startService(new Intent(context,WatchDogService.class));
		}
	}
	
	private static boolean isNeedStartService(Context context){       
		/*if(!LBEmodel.getInstance(context).isBindLBEService()){
			return true;
		}*/
		
		if(!ConfigModel.getInstance(context).getAppInfoModel().isAlreadyGetAllAppInfo() && 
				!ConfigModel.getInstance(context).getAppInfoModel().isDuringGetAllAppInfo()){
			return true;
	    }
		return false;		
	}

}
