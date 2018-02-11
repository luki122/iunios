package com.secure.interfaces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.netmanage.data.FlowData;

public class FlowChangeSubject {
	/**
	 * 用来保存注册的观察者对象
	 */
     private List<FlowChangeObserver> observers = new ArrayList<FlowChangeObserver>();
     
     /**
      * 注册观察者对象
      * @param observer
      */
     public void attach(FlowChangeObserver observer){
    	 for(FlowChangeObserver mObserver:observers){
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
     public void detach(FlowChangeObserver observer){
    	 observers.remove(observer);
     }
     
     /**
      * 完成初始化,对所有观察者对象更新
      */
     protected void notifyObserversOfFlowChange(HashMap<String ,FlowData> flowMap){
    	 for(FlowChangeObserver observer : observers){
    		 observer.updateOfFlowChange(flowMap);
    	 }
     }     
}
