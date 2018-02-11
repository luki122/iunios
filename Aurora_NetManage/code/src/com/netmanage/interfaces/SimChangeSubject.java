package com.netmanage.interfaces;

import java.util.ArrayList;
import java.util.List;

public class SimChangeSubject {
	private static SimChangeSubject instance;
	
	private SimChangeSubject(){}
	
	 public static synchronized SimChangeSubject getInstance() {
	 		if (instance == null) {
	 			instance = new SimChangeSubject();
	 		}
	 		return instance;
	 	 }
	 
	/**
	 * 用来保存注册的观察者对象
	 */
     private List<SimChangeObserver> observers = new ArrayList<SimChangeObserver>();
     
     /**
      * 注册观察者对象
      * @param observer
      */
     public void attach(SimChangeObserver observer){
    	 for(SimChangeObserver mObserver:observers){
    		 if(mObserver != null && mObserver == observer){
    			 return ;
    		 }
    	 }
    	 observers.add(observer);
     }
     
     /**
      * 删除观察者对象
      * @param observer
      */
     public void detach(SimChangeObserver observer){
    	 observers.remove(observer);
     }
     
     /**
      *对所有观察者对象更新
      */
     public void notifyObservers(){
    	 for(SimChangeObserver observer : observers){
    		 observer.simChange(this);
    	 }
     }
     
 
}
