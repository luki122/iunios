package com.privacymanage.interfaces;

import java.util.ArrayList;
import java.util.List;

import com.privacymanage.data.AccountData;

public class AccountSubject {
	/**
	 * 用来保存注册的观察者对象
	 */
     private List<AccountObserver> observers = new ArrayList<AccountObserver>();
     
     /**
      * 注册观察者对象
      * @param observer
      */
     public void attach(AccountObserver observer){
    	 for(AccountObserver mObserver:observers){
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
     public void detach(AccountObserver observer){
    	 observers.remove(observer);
     }
     
     /**
      * 账户切换 
      * @param accountData 当前身份信息
      */
     protected void notifyObserversOfSwitchAccount(AccountData accountData){
    	 for(AccountObserver observer : observers){
    		 observer.switchAccount(accountData);
    	 }
     }
     
     /**
	  * 删除隐私账户
	  * @param accountData  被删除的账户信息
	  * @param delete true：删除隐私空间数据，false：还原隐私空间数据
      */
     protected void notifyObserversOfDeleteAccount(AccountData accountData,boolean delete){
    	 for(AccountObserver observer : observers){
    		 observer.deleteAccount(accountData, delete);
    	 }
     }
}
