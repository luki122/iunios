package com.privacymanage.interfaces;

import java.util.ArrayList;
import java.util.List;
import com.privacymanage.data.ModuleInfoData;

public class ChildModuleSubject {
	/**
	 * 用来保存注册的观察者对象
	 */
     private List<ChildModuleObserver> observers = new ArrayList<ChildModuleObserver>();
     
     /**
      * 注册观察者对象
      * @param observer
      */
     public void attach(ChildModuleObserver observer){
    	 for(ChildModuleObserver mObserver:observers){
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
     public void detach(ChildModuleObserver observer){
    	 observers.remove(observer);
     }
     
     /**
      * 子模块信息更新
      * @param moduleInfoData
      */
     protected void notifyOfChildModuleUpdate(ModuleInfoData moduleInfoData){
    	 for(ChildModuleObserver observer : observers){
    		 observer.update(this, moduleInfoData);
    	 }
     }
}
