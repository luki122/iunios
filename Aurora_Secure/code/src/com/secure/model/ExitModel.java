package com.secure.model;

import com.secure.imageloader.ImageLoader;
import com.secure.interfaces.PermissionSubject;

public class ExitModel {
    public static void clear(){
    	DefSoftModel.releaseObject();
    	CacheSizeModel.releaseObject();
    	GetRecomPermsModel.releaseObject();
    	ImageLoader.releaseObject();
    	PermissionSubject.releaseObject();
    	RunningState.releaseObject();
    	/**
    	 * change 20140215 
    	 * 在退出应用后，不能销毁自启动相关对象，
    	 * 因为在WatchDogService中会监测应用的使用情况，
    	 * 并根据自启动表对应用的广播权限做适当的开始和关闭。
    	 * 详见：WatchDogService.dealAutoStart()
    	 */
//    	AutoStartModel.releaseObject(); 
    }
}
