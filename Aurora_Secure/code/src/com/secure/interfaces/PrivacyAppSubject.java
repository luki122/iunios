package com.secure.interfaces;

import java.util.ArrayList;
import java.util.List;

import com.privacymanage.data.AidlAccountData;
import com.secure.data.PrivacyAppData;

public class PrivacyAppSubject {
	/**
	 * 用来保存注册的观察者对象
	 */
     private List<PrivacyAppObserver> observers = new ArrayList<PrivacyAppObserver>();
     
     /**
      * 注册观察者对象
      * @param observer
      */
     public void attach(PrivacyAppObserver observer){
    	 for(PrivacyAppObserver mObserver:observers){
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
     public void detach(PrivacyAppObserver observer){
    	 observers.remove(observer);
     }
     
     /**
      * 完成初始化,对所有观察者对象更新
      */
     protected void notifyObserversOfInit(){
    	 for(PrivacyAppObserver observer : observers){
    		 observer.updateOfPrivacyAppInit(this);
    	 }
     }
     
     /**
      * 增加隐私应用,对所有观察者对象更新
      */
     protected void notifyObserversOfPrivacyAppAdd(List<PrivacyAppData>PrivacyAppList){
    	 for(PrivacyAppObserver observer : observers){
    		 observer.updateOfPrivacyAppAdd(this, PrivacyAppList);
    	 }
     }
     
     /**
      * 删除隐私应用,对所有观察者对象更新
      */
     protected void notifyObserversOfPrivacyAppDelete(List<PrivacyAppData>PrivacyAppList){
    	 for(PrivacyAppObserver observer : observers){
    		 observer.updateOfPrivacyAppDelete(this, PrivacyAppList);
    	 }
     }
     
     /**
      * 隐私身份切换,对所有观察者对象更新
      * @param accountData 当前账户
      */
     protected void notifyObserversOfPrivacyAccountSwitch(AidlAccountData accountData){
    	 for(PrivacyAppObserver observer : observers){
    		 observer.updateOfPrivacyAccountSwitch(this, accountData);
    	 }
     }
     
     /**
      * 删除隐私账户,对所有观察者对象更新
      * @param accountData 被删除的隐私账户
      */
     protected void notifyObserversOfDeletePrivacyAccount(AidlAccountData accountData){
    	 for(PrivacyAppObserver observer : observers){
    		 observer.updateOfPrivacyAccountSwitch(this, accountData);
    	 }
     }
     
     
}
