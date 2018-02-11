package com.aurora.reject.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;


public class SelectionManager<T> {


    private Set<T> mSelectedSet;//选择的
    private Set<T> mDataSet;//可选择的
    private SelectionListener mListener;
    private Integer mDataSetSize;
    
    public static final int SELECT_ALL_MODE = 1;
    public static final int DESELECT_ALL_MODE = 2;

    public interface SelectionListener {
        public void onSelectionModeChange(int mode);//全选和反选
        public void onSelectionChange(Object item, boolean selected);
        public Set getDataSet();
    }

    public SelectionManager(Context context) {        
        mSelectedSet = new HashSet<T>();
    }
    

    public void setSelectionListener(SelectionListener listener) {
        mListener = listener;
    }

    public void setDataSet(Set<T> dataAll) {
        mDataSet = dataAll;
        mDataSetSize = (null == mDataSet ? null : mDataSet.size()); 
    }
    
    public Integer getSelectableSize() {
    	return mDataSetSize;
    }
    
    public void selectAll() {
    	
    	mSelectedSet.addAll(mDataSet);
        
        if (mListener != null) {
            mListener.onSelectionModeChange(SELECT_ALL_MODE);
        }
    }

    public void deseletcAll() {
        mSelectedSet.clear();
        
        if (mListener != null) {
            mListener.onSelectionModeChange(DESELECT_ALL_MODE);
        }
    }
    
    public void toggle(T item) {
        if (null == item) {
            return;
        }
        
        if (mSelectedSet.contains(item)) {
            mSelectedSet.remove(item);
        } else {            
            mSelectedSet.add(item);
        }

        notifySelectionChange(item);
    }
    
    private void notifySelectionChange(T item) {
    	if (mListener != null) {    		
    		mListener.onSelectionChange(item, isSelected(item));
    		
    		if (mSelectedSet.isEmpty()) {
    			mListener.onSelectionModeChange(DESELECT_ALL_MODE);
    		} else if (isAllSelected()) {
    			mListener.onSelectionModeChange(SELECT_ALL_MODE);
    		}
    	}
    }

    public boolean isSelected(T item) {
        return mSelectedSet.contains(item);
    }

    public int getSelectedCount() {
        return mSelectedSet.size();        
    }

    public ArrayList<T> getSelected() {
        ArrayList<T> selected = new ArrayList<T>();
        for (T id : mSelectedSet) {
            selected.add(id);
        }
        return selected;
    }
    
    public boolean isDataSetReady() {
    	return null != mDataSet;
    }
    
    public boolean isAllSelected() {
    	if (null != mDataSet && null != mSelectedSet) {
    		return mDataSet.size() == mSelectedSet.size();
    	}
    	
    	return false;
    }


}
