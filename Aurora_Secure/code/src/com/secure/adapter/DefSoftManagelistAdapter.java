package com.secure.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.aurora.secure.R;
import com.secure.data.AppCategoryData;
import com.secure.data.BaseData;
import com.secure.utils.StringUtils;
import com.secure.utils.Utils;
import com.secure.viewcache.DefSoftManageListItemCache;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;

public class DefSoftManagelistAdapter extends ArrayAdapter<BaseData> {
	
	/**
	 * key: CategoryName
	 * value:DefSoftManageGridAdapter
	 * 存放某一类应用对用的gridView Adapter
	 */
	private HashMap<String ,DefSoftManageGridAdapter> gridAdapterMap;
	

	public DefSoftManagelistAdapter(Activity activity,
			List<BaseData> appCategoryDataList) {
		
		super(activity, 0, appCategoryDataList);
		gridAdapterMap = new HashMap<String ,DefSoftManageGridAdapter>();
	}
	
	private String getLabelText(int position){
		String labelText = null;
		if(position >= getCount() || position < 0){
			return labelText;
		}
		AppCategoryData item = (AppCategoryData) getItem(position);
		
		if(item == null){
			return labelText;
		}	
		if(StringUtils.isEmpty(item.getCurDefStartApkName())){
			labelText = item.getCategoryName()+
					getContext().getString(R.string.left_brackets)+
					getContext().getString(R.string.not_set)+
					getContext().getString(R.string.right_brackets);	
		}else{
			labelText = item.getCategoryName()+
					getContext().getString(R.string.left_brackets)+
					item.getCurDefStartApkName()+
					getContext().getString(R.string.right_brackets);				
		}		
		return labelText;
	}

	@Override 
	public View getView(int position, View convertView, ViewGroup parent) {
		DefSoftManageListItemCache holder;	
		if (convertView == null) {			
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater(); 
			convertView = inflater.inflate(R.layout.def_soft_manage_list_item, parent, false);
			holder = new DefSoftManageListItemCache(convertView);
			convertView.setTag(holder);
		} else {
			holder = (DefSoftManageListItemCache)convertView.getTag();
		}
		
		if(getCount()<=position){
			return convertView;
		}
				
		AppCategoryData item = (AppCategoryData)getItem(position);
		DefSoftManageGridAdapter gridAdapter = gridAdapterMap.get(item.getCategoryName());
		if(gridAdapter == null){
			gridAdapter = new DefSoftManageGridAdapter(((Activity)getContext()),item);
			gridAdapterMap.put(item.getCategoryName(), gridAdapter);
		}
		holder.getItemGridView().setAdapter(gridAdapter);
		holder.getLabelText().setText(getLabelText(position));
		return convertView;
	}
	
	public void mNotify(){
		if(gridAdapterMap != null){
			gridAdapterMap.clear();
		}
		notifyDataSetChanged();
	}
}
