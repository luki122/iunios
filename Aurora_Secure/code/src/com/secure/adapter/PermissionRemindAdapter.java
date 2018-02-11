package com.secure.adapter;

import java.util.HashMap;
import java.util.List;
import com.aurora.secure.R;
import com.secure.model.LBEmodel;
import com.secure.utils.ApkUtils;
import com.secure.utils.StringUtils;
import com.secure.utils.mConfig;
import com.secure.viewcache.PermissionHintListItemCache;
import android.app.Activity;
import android.content.Context;

import com.secure.activity.PermissionRemindActivity;
import com.secure.data.PermissionInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import aurora.widget.AuroraSwitch;

public class PermissionRemindAdapter extends ArrayAdapter<PermissionInfo> {      

	private View parent;
	private String packageName;
	private HashMap<PermissionInfo,CheckBox> checkBoxMap = 
			new HashMap<PermissionInfo,CheckBox>();
	
	public PermissionRemindAdapter(Context context,
			String packageName,
			List<PermissionInfo> listData) {		
		super(context, 0, listData);
		this.packageName = packageName;
	}
	
	@Override 
	public View getView(int position, View convertView, ViewGroup parent) {
		this.parent = parent;
		PermissionHintListItemCache holder;
		if (convertView == null){
			convertView = LayoutInflater.from(getContext()).
					inflate(R.layout.permission_remind_list_item, parent, false);
			holder = new PermissionHintListItemCache(convertView);		
			convertView.setTag(holder);
		} else {
			holder = (PermissionHintListItemCache)convertView.getTag();
		}
				
		if(getCount()<=position){
			return convertView;
		}
		
		PermissionInfo item =(PermissionInfo)getItem(position);
		String permName = LBEmodel.getInstance(getContext()).getPermName(item.permId);
		if(ApkUtils.isDangerPermission(getContext(), packageName, item.permId)){
			holder.getPermissionName().setText(permName+
					getContext().getString(R.string.left_brackets)+
					getContext().getString(R.string.suggest_close)+
		    		getContext().getString(R.string.right_brackets) );
		}else{
			holder.getPermissionName().setText(permName);
		}		
			
		holder.getCheckBox().setClickable(false);//设为不允许点击
		holder.getCheckBox().setLongClickable(false);
		holder.getCheckBox().setTag(position);
		checkBoxMap.put(item, holder.getCheckBox());
		if(item.getIsOpen()){
			holder.getCheckBox().setChecked(true);
		}else{
			holder.getCheckBox().setChecked(false);
		}
		return convertView;
	}

	/**
	 * 处理onItemClick事件
	 * @param position
	 */
	public void dealItemClick(int position){
		if(getCount()<=position){
			return ;
		}
		
		PermissionInfo item =(PermissionInfo)getItem(position);	
		CheckBox checkView = (CheckBox)parent.findViewWithTag(position);
		if(item == null || checkView == null){
			return ;
		}
		
		if(item.getIsOpen()){
			checkView.setChecked(false);
			ApkUtils.closeApkAppointPermission(getContext(), 
					((PermissionRemindActivity)getContext()).getCurAppInfo(), item);
		}else{			
			checkView.setChecked(true);
			ApkUtils.openApkAppointPermission(getContext(), 
					((PermissionRemindActivity)getContext()).getCurAppInfo(), item);
		}		
	}
	
	/**
	 * 更新指定权限的开关状态
	 * @param permissionInfo
	 */
	public void updateOfPermState(PermissionInfo permissionInfo){
		if(permissionInfo == null){
			return ;
		}
		CheckBox checkBox =checkBoxMap.get(permissionInfo);
		if(checkBox != null && 
				checkBox.isChecked() != permissionInfo.getIsOpen()){
			checkBox.setChecked(permissionInfo.getIsOpen());
		}
	}
	
	public void realseObject(){		
		if(checkBoxMap != null){
			checkBoxMap.clear();
		}	
	}
}
