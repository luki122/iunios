package com.secure.interfaces;

import java.util.ArrayList;
import java.util.List;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.secure.data.AppInfo;
import com.secure.data.PermissionInfo;

public class PermissionSubject {
	 private static PermissionSubject instance;
	
	/**
	 * 用来保存注册的观察者对象
	 */
     private List<PermissionObserver> observers = new ArrayList<PermissionObserver>();
     
     private UIHandler mUIhandler;
     
     private PermissionSubject(){
    	 mUIhandler = new UIHandler(Looper.getMainLooper());
     }
         
 	/**
 	 * 必须在UI线程中初始化,如果instance为null，则会创建一个
 	 * @return 返回值不可能为null
 	 */
 	 public static synchronized PermissionSubject getInstance() {
 		if (instance == null) {
 			instance = new PermissionSubject();
 		}
 		return instance;
 	 }
     
     /**
      * 注册观察者对象
      * @param observer
      */
     public void attach(PermissionObserver observer){
    	 observers.add(observer);
     }
     
     /**
      * 删除观察者对象
      * @param observer
      */
     public void detach(PermissionObserver observer){
    	 observers.remove(observer);
     }

     /**
      * 指定应用的权限状态更新了
      * @param appInfo
      */
     protected void notifyObserversOfPermsStateChange(AppInfo appInfo,PermissionInfo permissionInfo){
    	 for(PermissionObserver observer : observers){
    		 observer.updateOfPermsStateChange(appInfo,permissionInfo);
    	 }
     }
     
     public static void releaseObject(){
    	 if(instance != null){
    		 if(instance.observers != null){
    			 instance.observers.clear();
    		 }
    		 instance = null;
    	 }
     }
  	
    final class UIHandler extends Handler{		
 		public UIHandler(Looper looper){
            super(looper);
        }
 		@Override
 	    public void handleMessage(Message msg) { 
 			TransferData transferData = (TransferData)msg.obj ;
 			if(transferData != null){
 				notifyObserversOfPermsStateChange(transferData.appInfo,transferData.permissionInfo);
 			} 			 
 	    }
 	}
  	
  	/**
  	 * 发送权限更新的消息
  	 * @param appInfo
  	 */
  	public void notifyObserversOfPermsStateChangeFunc(AppInfo appInfo,PermissionInfo permissionInfo){
  		Message msg = new Message();
  		msg.obj = new TransferData(appInfo,permissionInfo);
  		mUIhandler.sendMessage(msg);
  	}
  	
  	class TransferData{
  		AppInfo appInfo;
  		PermissionInfo permissionInfo;
  		public TransferData(AppInfo appInfo,PermissionInfo permissionInfo){
  			this.appInfo = appInfo;
  			this.permissionInfo = permissionInfo;
  		}
  	}
}
