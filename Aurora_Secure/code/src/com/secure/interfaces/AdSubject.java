package com.secure.interfaces;

import java.util.ArrayList;
import java.util.List;

public class AdSubject {
	/**
	 * 用来保存注册的观察者对象
	 */
     private List<AdObserver> observers = new ArrayList<AdObserver>();
     
     /**
      * 注册观察者对象
      * @param observer
      */
     public void attach(AdObserver observer){
    	 for(AdObserver mObserver:observers){
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
     public void detach(AdObserver observer){
    	 observers.remove(observer);
     }
     
     /**
      * 完成初始化,对所有观察者对象更新
      */
     protected void notifyObserversOfInit(){
    	 for(AdObserver observer : observers){
    		 observer.updateOfInit(this);
    	 }
     }
     
     /**
      * 安装一个应用,对所有观察者对象更新
      */
     protected void notifyObserversOfInStall(String pkgName){
    	 for(AdObserver observer : observers){
    		 observer.updateOfInStall(this, pkgName);
    	 }
     }
     
     /**
      * 覆盖安装一个应用,对所有观察者对象更新
      */
     protected void notifyObserversOfCoverInStall(String pkgName){
    	 for(AdObserver observer : observers){
    		 observer.updateOfCoverInStall(this, pkgName);
    	 }
     }
     
     /**
      * 删除一个应用,对所有观察者对象更新,
      */
     protected void notifyObserversOfUnInstall(String pkgName){
    	 for(AdObserver observer : observers){
    		 observer.updateOfUnInstall(this, pkgName);
    	 }
     }
     
     /**
      * 安装在外部sd卡中的应用变得可用
      * @param pkgList
      */
     protected void notifyObserversOfExternalAppAvailable(List<String> pkgList){
    	 for(AdObserver observer : observers){
    		 observer.updateOfExternalAppAvailable(this,pkgList);
    	 }
     }
     
     /**
      * 安装在外部sd卡中的应用变得不可用
      * @param pkgList
      */
     protected void notifyObserversOfExternalAppUnAvailable(List<String> pkgList){
    	 for(AdObserver observer : observers){
    		 observer.updateOfExternalAppUnAvailable(this,pkgList);
    	 }
     }
     
     /**
      * 广告库更新
      */
     protected void notifyObserversOfAdLibUpdate(){
    	 for(AdObserver observer : observers){
    		 observer.updateOfAdLibUpdate(this);
    	 }
     }
     
     /**
      * 用户手动更新（即重新扫描所有应用）
      */
     protected void notifyObserversOfManualUpdate(){
    	 for(AdObserver observer : observers){
    		 observer.updateOfManualUpdate(this);
    	 }
     }
     
     /**
      * 用户手动更新（即重新扫描所有应用）
      */
     protected void notifyObserversOfSwitchChange(String pkgName,boolean switchState){
    	 for(AdObserver observer : observers){
    		 observer.updateOfSwitchChange(this, pkgName, switchState);
    	 }
     }
}
