package com.secure.interfaces;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import android.os.Handler;
import android.os.Message;

public class LBEServiceBindSubject {
	 private static LBEServiceBindSubject instance;
	
	/**
	 * 用来保存注册的观察者对象
	 */
     private List<LBEServiceBindObserver> observers = new ArrayList<LBEServiceBindObserver>();
     
     private LBEServiceBindSubject(){}
     
 	/**
 	 * 必须在UI线程中初始化,如果instance为null，则会创建一个
 	 * @return 返回值不可能为null
 	 */
 	 public static synchronized LBEServiceBindSubject getInstance() {
 		if (instance == null) {
 			instance = new LBEServiceBindSubject();
 		}
 		return instance;
 	 }
 	 
 	/**
  	 * @return 返回值可能为null
  	 */
  	 public static synchronized LBEServiceBindSubject getInstanceOfNotCreate() {
  		return instance;
  	 }
     
     /**
      * 注册观察者对象
      * @param observer
      */
     public void attach(LBEServiceBindObserver observer){
    	 observers.add(observer);
     }
     
     /**
      * 删除观察者对象
      * @param observer
      */
     public void detach(LBEServiceBindObserver observer){
    	 observers.remove(observer);
     }

     /**
      * 指定应用的权限状态更新了
      * @param appInfo
      */
     protected void notifyObserversOfBindStateChange(boolean isConnection){
    	 for(LBEServiceBindObserver observer : observers){
    		 observer.LBEServiceBindStateChange(isConnection);
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
     
     private final Handler BindStateChangeHandler = new Handler() {
  	   @Override
  	   public void handleMessage(Message msg) {	 
  		   AtomicBoolean state = (AtomicBoolean)msg.obj;
  		   notifyObserversOfBindStateChange(state.get());
  	   }
  	};
  	
  	/**
  	 * 发送更新的消息
  	 * @param appInfo
  	 */
  	public void notifyObserversOfBindStateChangeFunc(boolean isConnection){
  		AtomicBoolean state = new AtomicBoolean(isConnection);
  		Message msg = new Message();
  		msg.obj = state;
  		BindStateChangeHandler.sendMessage(msg);
  	}
}
