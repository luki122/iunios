package com.secure.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.aurora.secure.R;
import com.secure.data.AppInfo;
import com.secure.data.BaseData;
import com.secure.data.SameFirstCharAppData;
import com.secure.stickylistheaders.StickyListHeadersAdapter;
import com.secure.utils.StringUtils;
import com.secure.utils.Utils;
import com.secure.view.GridViewForEmbed;
import com.secure.viewcache.AddPrivacyAppListItemCache;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class AddPrivacyApplistAdapter extends ArrayAdapter<BaseData> implements 
                                                      StickyListHeadersAdapter{
	
	private HashMap<String ,AddPrivacyAppGridAdapter> gridAdapterMap;	
	private HashMap<String ,View> convertViewMap;
	private final boolean isUseGetViewFunc1 = false;	

	public AddPrivacyApplistAdapter(Activity activity,
			List<BaseData> sameFirstCharAppData) {
		
		super(activity, 0, sameFirstCharAppData);
		gridAdapterMap = new HashMap<String ,AddPrivacyAppGridAdapter>();
		convertViewMap = new HashMap<String ,View>();
	}
	
	private String getLabelText(int position){
		String labelText = null;
		if(position >= getCount() || position < 0){
			return labelText;
		}
		SameFirstCharAppData item = (SameFirstCharAppData)getItem(position);
		
		if(item == null){
			return labelText;
		}		
		labelText = item.getFirstChar(); 
		return labelText;
	}

	@Override 
	public View getView(int position, View convertView, ViewGroup parent) {
		//比较发现，在滑动时getViewFunc2比getViewFunc1要流畅很多
		if(isUseGetViewFunc1){
			return getViewFunc1(position,convertView,parent);
		}else{
			return getViewFunc2(position,convertView,parent);
		}		
	}
	
	private View getViewFunc1(int position, View convertView, ViewGroup parent){
		AddPrivacyAppListItemCache holder;	
		if (convertView == null) {			
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater(); 
			convertView = inflater.inflate(R.layout.add_privacy_app_list_item, parent, false);
			holder = new AddPrivacyAppListItemCache(convertView);
			convertView.setTag(holder);
		} else {
			holder = (AddPrivacyAppListItemCache)convertView.getTag();
		}
		
		if(getCount()<=position){
			return convertView;
		}
				
		SameFirstCharAppData item = (SameFirstCharAppData)getItem(position);
		AddPrivacyAppGridAdapter gridAdapter = gridAdapterMap.get(item.getFirstChar());
		if(gridAdapter == null){
			gridAdapter = new AddPrivacyAppGridAdapter(((Activity)getContext()),item);
			gridAdapterMap.put(item.getFirstChar(), gridAdapter);
		}
		holder.getItemGridView().setAdapter(gridAdapter);
		return convertView;
	}
	
	private View getViewFunc2(int position, View convertView, ViewGroup parent){
		if(getCount()<=position){
			return convertView;
		}
		SameFirstCharAppData item = (SameFirstCharAppData)getItem(position);
		View itemView = convertViewMap.get(item.getFirstChar());
		if(itemView == null){
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater(); 
			itemView = inflater.inflate(R.layout.add_privacy_app_list_item, parent, false);
			GridViewForEmbed gridView = (GridViewForEmbed)itemView.findViewById(R.id.itemGridView);		
			AddPrivacyAppGridAdapter gridAdapter = new AddPrivacyAppGridAdapter(
					((Activity)getContext()),item);		
			gridView.setAdapter(gridAdapter);			
			convertViewMap.put(item.getFirstChar(), itemView);
		}
		return itemView;
	}
	
	public void mNotify(){
		if(isUseGetViewFunc1){
			if(gridAdapterMap != null){
				gridAdapterMap.clear();
			}
		}else{
			if(convertViewMap != null){
				convertViewMap.clear();
			}
		}		
		notifyDataSetChanged();
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {		
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater(); 
			convertView = inflater.inflate(R.layout.add_privacy_app_list_header, parent, false);
		}
		
		TextView labelText = (TextView)convertView.findViewById(R.id.labelText);
		labelText.setText(getLabelText(position));
		return convertView;
	}

	@Override
	public long getHeaderId(int position) {
		String labelText = getLabelText(position);
		if(!StringUtils.isEmpty(labelText)){
			return labelText.charAt(0);
		}else{
			return 0;
		}
	}
}
