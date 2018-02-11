/***
 * 应用管理列表显示
 */

package com.aurora.puremanager.adapter;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aurora.puremanager.R;
import com.aurora.puremanager.data.BaseData;
import com.aurora.puremanager.data.MainActivityItemData;
import com.aurora.puremanager.utils.StorageUtil;

public class AppManageListAdapter extends ArrayAdapter<BaseData> {      
	private AbsListView.LayoutParams convertViewLayoutParams;	
	public AppManageListAdapter(Activity activity,
			List<BaseData> listData) {		
		super(activity, 0, listData);
		convertViewLayoutParams = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
	}

	@Override 
	public View getView(int position, View convertView, ViewGroup parent) {			
		if (convertView == null) {
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater(); 
			convertView = inflater.inflate(R.layout.app_manage_grid_item, parent, false);			
		}
		
		int parentHeight = parent.getHeight();
		if(parentHeight >0){
			convertViewLayoutParams.height = parentHeight/2;
			convertView.setLayoutParams(convertViewLayoutParams);
		}
		
		if(getCount()<=position){
			return convertView;
		}

		MainActivityItemData item =(MainActivityItemData)getItem(position);
		ImageView Icon = (ImageView)convertView.findViewById(R.id.Icon);
		TextView titleName = (TextView)convertView.findViewById(R.id.titleName);
		Icon.setImageResource(item.getIconRes());
		titleName.setText(item.getItemName());
		return convertView;
	}
	
	/**
	 * 存储空间是否少于1G
	 * @return
	 */
	private boolean isSpaceLow1G(){	
    	long internalAvailable = StorageUtil.getInstance(getContext()).getAvailableInternalMemorySize();
		long externalAvailable = StorageUtil.getInstance(getContext()).getAvailableExternalMemorySize();
		if(internalAvailable == StorageUtil.ERROR) internalAvailable = 0;
		if(externalAvailable == StorageUtil.ERROR) externalAvailable = 0;				
		long available = internalAvailable+externalAvailable;
		
		if(available<1024*1024*1024){
			return true;
		}else{
			return false;
		}
	}
}
