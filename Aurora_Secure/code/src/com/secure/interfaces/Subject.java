package com.secure.interfaces;

import java.util.ArrayList;
import java.util.List;

public class Subject {
	/**
	 * 用来保存注册的观察者对象
	 */
     private List<Observer> observers = new ArrayList<Observer>();
     
     /**
      * 注册观察者对象
      * @param observer
      */
     public void attach(Observer observer){
    	 for(Observer mObserver:observers){
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
     public void detach(Observer observer){
    	 observers.remove(observer);
     }
     
     /**
      * 完成初始化,对所有观察者对象更新
      */
     protected void notifyObserversOfInit(){
    	 for(Observer observer : observers){
    		 observer.updateOfInit(this);
    	 }
     }
     
     /**
      * 安装一个应用,对所有观察者对象更新
      */
     protected void notifyObserversOfInStall(String pkgName){
    	 for(Observer observer : observers){
    		 observer.updateOfInStall(this, pkgName);
    	 }
     }
     
     /**
      * 覆盖安装一个应用,对所有观察者对象更新
      */
     protected void notifyObserversOfCoverInStall(String pkgName){
    	 for(Observer observer : observers){
    		 observer.updateOfCoverInStall(this, pkgName);
    	 }
     }
     
     /**
      * 删除一个应用,对所有观察者对象更新,
      */
     protected void notifyObserversOfUnInstall(String pkgName){
    	 for(Observer observer : observers){
    		 observer.updateOfUnInstall(this, pkgName);
    	 }
     }
     
     /**
      * LBE推荐的权限配置更新
      */
     protected void notifyObserversOfRecomPermsChange(){
    	 for(Observer observer : observers){
    		 observer.updateOfRecomPermsChange(this);
    	 }
     } 
     
     /**
      * 安装在外部sd卡中的应用变得可用
      * @param pkgList
      */
     protected void notifyObserversOfExternalAppAvailable(List<String> pkgList){
    	 for(Observer observer : observers){
    		 observer.updateOfExternalAppAvailable(this,pkgList);
    	 }
     }
     
     /**
      * 安装在外部sd卡中的应用变得不可用
      * @param pkgList
      */
     protected void notifyObserversOfExternalAppUnAvailable(List<String> pkgList){
    	 for(Observer observer : observers){
    		 observer.updateOfExternalAppUnAvailable(this,pkgList);
    	 }
     }
}
