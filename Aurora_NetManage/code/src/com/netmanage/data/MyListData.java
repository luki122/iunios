package com.netmanage.data;

import java.util.ArrayList;
import java.util.List;

public class MyListData extends BaseData{
	public MyListData() {
		super("MyListData");
	}

	private List<BaseData> dataList = null;
	
	public List<BaseData> getDataList(){
		if (dataList == null) {
			dataList = new ArrayList<BaseData>();
		}
		return dataList;
	}
	
	public int Count() {
		if (dataList == null) {
			return 0;
		}else{
			return dataList.size();
		}		
	}

	public void AddData(BaseData bdata) {
		if (dataList == null) {
			dataList = new ArrayList<BaseData>();
		}
		dataList.add(bdata);
	}
	
	public void AddData(int position,BaseData bdata) {
		if (dataList == null) {
			dataList = new ArrayList<BaseData>();
		}
		
		if(position >= 0 && position <= Count()){
			dataList.add(position,bdata);
		}
		
		
	}

	public BaseData GetData(int index) {
		if (Count() > 0 && Count() > index) {
			return dataList.get(index);
		}
		return null;
	}

	public void ClearDataList() {
		if (dataList != null) {
			dataList.clear();
		}
	}
	
	public void removeDataByIndex(int index){
		if(dataList!=null){
			if (Count() > 0 && Count() > index) {
				dataList.remove(index);
			}
		}
	}

	public void removeData(BaseData data){
		if(dataList!=null && data != null){
			dataList.remove(data);
		}
	}
	
	public void releaseObject(){
		if(dataList != null){
			dataList.clear();
			dataList = null;
		}
	}
}
