package com.android.settings.lscreen;
import java.util.ArrayList;
import java.util.List;


public class DataArrayList<T> {

	private ArrayList<T> dataList = new ArrayList<T>();
	private long lastModifyTime=-1;
	
	public long getLastModifyTime()
	{
		return lastModifyTime;
	}
	
	public List<T> getDataList()
	{
		return this.dataList;
	}

	public void add(T t)
	{
		synchronized (this) {
			dataList.add(t);
			lastModifyTime=System.currentTimeMillis();
		}
	}
	
    public int size()
    {
    	synchronized (this) {
			return dataList.size();
		}
    }
	
    public T get(int position)
    {
    	synchronized (this) {
            if(position<size() && position>=0)
            {
            	return dataList.get(position);
            }
            return null;
		}
    }
    
    public void add(int position,T t)
    {
    	synchronized (this) {
			dataList.add(position, t);
			lastModifyTime=System.currentTimeMillis();
		}
    }
    
    public int indexOf(T t)
    {
    	synchronized (this) {
			return dataList.indexOf(t);
		}
    }
    
    public void removeDataIndex(int position)
    {
    	synchronized (this) {
    		if(position>=0 && position<dataList.size())
    		{
    			dataList.remove(position);
    			lastModifyTime = System.currentTimeMillis();
    		}
		}
    }
    
    public void removeDate(T t)
    {
    	synchronized (this) {
			if(t!=null && dataList.contains(t))
			{
				dataList.remove(t);
				lastModifyTime = System.currentTimeMillis();
			}
		}
    }
    public boolean contain(T t)
    {
    	synchronized (this) {
		    return dataList.contains(t);
		}
    }

	public void clear() {
		synchronized (this) {
			dataList.clear();	
			lastModifyTime = System.currentTimeMillis();
		}		
	}
	
}
