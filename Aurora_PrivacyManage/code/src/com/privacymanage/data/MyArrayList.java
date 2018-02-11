package com.privacymanage.data;

import java.util.ArrayList;
import java.util.List;
/**
 * 加入同步与异常处理的ArrayList
 * @author 
 *
 * @param <T>
 */
public class MyArrayList<T>{
	private long lastModifyTime = 0;
	private List<T> dataList = new ArrayList<T>();
	
	/**
	 * 获取该链表最后修改的时间
	 * @return
	 */
	public long getLastModifyTime(){
		return lastModifyTime;
	}
	
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
			lastModifyTime = System.currentTimeMillis();
		}		
	}
	
	public void add(int position,T t) {		
		if(position >= 0 && position <= size()){
			synchronized (this) {
				try{
				    dataList.add(position,t);
				    lastModifyTime = System.currentTimeMillis();
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
			lastModifyTime = System.currentTimeMillis();
		}		
	}
	
	public void remove(int index){
		if (size() > 0 && size() > index) {
			synchronized (this) {
				try{
				   dataList.remove(index);
				   lastModifyTime = System.currentTimeMillis();
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
					lastModifyTime = System.currentTimeMillis();
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
			lastModifyTime = System.currentTimeMillis();
		}		
	}
}
