package com.android.adapter;

import java.util.ArrayList;

import com.android.utils.ApkUtils;
import com.android.viewcache.InstallConfirmListHeaderCache;
import com.android.viewcache.InstallConfirmListItemCache;
import com.android.data.MyPermissionInfo;
import com.android.packageinstaller.R;
import android.app.Activity;
import android.content.pm.PermissionInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class InstallConfirmAdapter extends BaseExpandableListAdapter {	
	private Activity activity;
	private ArrayList<ArrayList<MyPermissionInfo>> groups;
	private ArrayList<String> groupsTitle;
	
	public InstallConfirmAdapter(Activity activity,
			ArrayList<ArrayList<MyPermissionInfo>> groups,
			ArrayList<String> groupsTitle){
	    this.activity = activity;
	    this.groups = groups;
	    this.groupsTitle = groupsTitle;
	}
		
	/**
	 * 获取Group 的title
	 * @param position
	 * @return
	 */
	private String getGroupTitle(int position){
		if(groupsTitle == null || position >= groupsTitle.size()){
			return null;
		}else{
			return groupsTitle.get(position);
		}
	}
	
	private void showTitle(int position,TextView labelText,TextView numText){
		if(groups == null || position >= groups.size()){
			return ;
		}
		
		ArrayList<MyPermissionInfo> tmpList = groups.get(position);
		if(tmpList != null && tmpList.size()>0){
			if(ApkUtils.isDangerPermission(tmpList.get(0))){
				labelText.setText(R.string.privacy_permission);
			}else{
				labelText.setText(R.string.other_permission);
			}
			numText.setText(activity.getString(R.string.left_brackets)+
					tmpList.size()+
		    		activity.getString(R.string.right_brackets));
		}
	}
    
    public Object getChild(int groupPosition, int childPosition) {
    	if(groups == null || groupPosition >= groups.size()){
    		return null;
    	}
    	
    	ArrayList<MyPermissionInfo> tmpList = groups.get(groupPosition);
    	if(tmpList == null || childPosition >= tmpList.size()){
    		return null;
    	}else{
    		return tmpList.get(childPosition);
    	}    	
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    public int getChildrenCount(int groupPosition) {
    	if(groups == null || groupPosition >= groups.size()){
    		return 0;
    	}
    	
    	ArrayList<MyPermissionInfo> tmpList = groups.get(groupPosition);
    	if(tmpList == null){
    		return 0;
    	}else{
    		return tmpList.size();
    	}
    }
    
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent) {
    	InstallConfirmListItemCache holder;
		if (convertView == null) {
			LayoutInflater inflater = activity.getLayoutInflater(); 
			convertView = inflater.inflate(R.layout.install_confirm_list_item, parent, false);
			holder = new InstallConfirmListItemCache(convertView);
			convertView.setTag(holder);
		} else {
			holder = (InstallConfirmListItemCache)convertView.getTag();
		}
		
		if(groupPosition == getGroupCount()-1 && isLastChild){
			holder.getItemLayout().setBackgroundResource(R.drawable.item_of_bottom_bg);
		}else{
			holder.getItemLayout().setBackgroundResource(R.drawable.item_bg);
		}
		
//		if(childPosition == 0){
//			holder.getTopSpaceView().setVisibility(View.VISIBLE);
//		}else{
//			holder.getTopSpaceView().setVisibility(View.GONE);
//		}
//		
//		if(isLastChild){
//			holder.getBottomSpaceView().setVisibility(View.VISIBLE);
//		}else{
//			holder.getBottomSpaceView().setVisibility(View.GONE);
//		}
        
		MyPermissionInfo item = (MyPermissionInfo)getChild(groupPosition, childPosition);      
        holder.getPermissionName().setText(item.mLabel);
        return convertView;
    }

    public Object getGroup(int groupPosition) {
    	if(groups == null || groupPosition >= groups.size()){
    		return null;
    	}
    	
    	return groups.get(groupPosition);
    }

    public int getGroupCount() {
    	if(groups == null){
    		return 0;
    	}else{
    		return groups.size();
    	}
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    private boolean firstGroupOpen = false;
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
            ViewGroup parent) {   	
    	InstallConfirmListHeaderCache holder;
		if (convertView == null) {
			LayoutInflater inflater = activity.getLayoutInflater(); 
			convertView = inflater.inflate(R.layout.install_confirm_list_header, parent, false);
			holder = new InstallConfirmListHeaderCache(convertView);
			convertView.setTag(holder);
		} else {
			holder = (InstallConfirmListHeaderCache)convertView.getTag();
		}
    	
//		holder.getLabelText().setText(getGroupTitle(groupPosition));
		showTitle(groupPosition,holder.getLabelText(),holder.getNumText());
		if(isExpanded){
			holder.getArrowImg().setImageResource(R.drawable.up_arrow_bg);	
		}else{
			holder.getArrowImg().setImageResource(R.drawable.down_arrow_bg);
		}	
		
		if(groupPosition == 0){
			firstGroupOpen = isExpanded;
		}
		
		if(groupPosition == 0 && getGroupCount() == 1){
			if(isExpanded){
//				holder.getLabelLayout().setBackgroundResource(R.drawable.item_of_up);
//				holder.getSpaceView().setVisibility(View.VISIBLE);
			}else{
//				holder.getLabelLayout().setBackgroundResource(R.drawable.item_of_alone);
//				holder.getSpaceView().setVisibility(View.VISIBLE);
			}		
		}else if(groupPosition == 0 && getGroupCount() > 1){
//			holder.getLabelLayout().setBackgroundResource(R.drawable.item_of_up);
//			holder.getSpaceView().setVisibility(View.VISIBLE);
		}else if(groupPosition ==1){
			if(isExpanded){
//				holder.getLabelLayout().setBackgroundResource(R.drawable.item_of_middle);
				holder.getSpaceView().setVisibility(View.GONE);
			}else{
				if(firstGroupOpen){
//					holder.getLabelLayout().setBackgroundResource(R.drawable.item_of_bottom_line);
					holder.getSpaceView().setVisibility(View.GONE);
				}else{
//					holder.getLabelLayout().setBackgroundResource(R.drawable.item_of_bottom);
					holder.getSpaceView().setVisibility(View.GONE);
				}				
			}			
		}
        return convertView;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public boolean hasStableIds() {
        return true;
    }

}
