package com.netmanage.interfaces;

import java.util.ArrayList;
import java.util.List;

public class ConfigSubject {
	 private static ConfigSubject instance;
	
	/**
	 * 用来保存注册的观察者对象
	 */
     private List<ConfigObserver> observers = new ArrayList<ConfigObserver>();
     
     private ConfigSubject(){}
     
 	/**
 	 * 必须在UI线程中初始化,如果instance为null，则会创建一个
 	 * @return 返回值不可能为null
 	 */
 	 public static synchronized ConfigSubject getInstance() {
 		if (instance == null) {
 			instance = new ConfigSubject();
 		}
 		return instance;
 	 }
     
     /**
      * 注册观察者对象
      * @param observer
      */
     public void attach(ConfigObserver observer){
    	 observers.add(observer);
     }
     
     /**
      * 删除观察者对象
      * @param observer
      */
     public void detach(ConfigObserver observer){
    	 observers.remove(observer);
     }

     /**
      * 每月套餐流量数据更新了
      */
     public void notifyObserversOfMonthlyFlowChange(){
    	 for(ConfigObserver observer : observers){
    		 observer.updateOfMonthlyFlowChange();
    	 }
     }
     
     /**
      * 超额预警状态改变
      */
     public void notifyObserversOfExcessEarlyWarning(){
    	 for(ConfigObserver observer : observers){
    		 observer.updateOfExcessEarlyWarning();
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
}
