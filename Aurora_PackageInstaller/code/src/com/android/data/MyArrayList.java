package com.android.data;

import java.util.ArrayList;
import java.util.List;

public class MyArrayList<T>{
	private List<T> dataList = new ArrayList<T>();
	
	public List<T> getDataList(){
		return this.dataList;
	}
	
	public int size() {
		synchronized (this) {
			return dataList.size();		
		}		
	}

	public void add(T t) {
		synchronized (this) {
			dataList.add(t);	
		}		
	}
	
	public void add(int position,T t) {		
		if(position >= 0 && position <= size()){
			synchronized (this) {
				try{
				    dataList.add(position,t);
				}catch(IndexOutOfBoundsException e){
					//
				}
			}		
		}		
	}

	public T get(int index) {
		if (size() > 0 && size() > index) {
			synchronized (this) {
				try{
					return dataList.get(index);
				}catch(IndexOutOfBoundsException e){
					//
				}				
			}			
		}
		return null;
	}

	public void clear() {
		synchronized (this) {
			dataList.clear();	
		}		
	}
	
	public void remove(int index){
		if (size() > 0 && size() > index) {
			synchronized (this) {
				try{
				   dataList.remove(index);	
				}catch(IndexOutOfBoundsException e){
					//
				}	
			}			
		}
	}

	public void remove(T t){
		if(t != null){
			try{
				synchronized (this) {
					dataList.remove(t);
				}				
			}catch(Exception e){
				//
			}			
		}
	}
	
	public boolean contains(T t){
		synchronized (this) {		
			return dataList.contains(t);
		}	
	}
	
	public int indexOf(T t){
		synchronized (this) {		
			return dataList.indexOf(t);
		}	
	}
	
	public void releaseObject(){
		synchronized (this) {
			dataList.clear();
		}		
	}
}
