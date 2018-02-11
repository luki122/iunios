package com.android.settings.lscreen;

import java.util.ArrayList;
import java.util.List;



public class LSAppSubject {
	
	private List<LSAppObserver> observers = new ArrayList<LSAppObserver>();
	
	public void attach(LSAppObserver appObserver)
	{
		for(LSAppObserver appObserver2 : observers)
		{
			if(appObserver2.equals(appObserver))
			{
				return ;
			}
		}
		observers.add(appObserver);
	}
	
	public void detach(LSAppObserver observer)
	{
		if(observers.size()==0)
		{
			return ;
		}
		if(observers.contains(observer))
		{
			observers.remove(observer);
		}
	}
	
	public void notifyObserverOfInit()
	{
		for(LSAppObserver observer : observers)
		{
			observer.initOrUpdateLSApp(this);
		}
	}
	
	public void notifyObserverAdd(List<AppInfo> appDatas)
	{
		for(LSAppObserver observer : observers)
		{
			observer.addOrUpdateLSApp(this, appDatas);
		}
	}
	
	public void notifyObserserDel(List<AppInfo> appDatas)
	{
		for(LSAppObserver observer : observers)
		{
			observer.delOrUpdateLSApp(this, appDatas);
		}
	}
	
	public void notifyObserverAll(List<AppInfo> appDatas)
	{
		for(LSAppObserver observer : observers)
		{
			observer.allAppAchieve(this, appDatas);
		}
	}
}
