package com.android.utils;

import java.util.ArrayList;
import com.android.packageinstaller.R;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;

import com.android.adapter.InstallConfirmAdapter;
import com.android.data.MyPermissionInfo;

public class ExpandableListViewUtils {
    private ArrayList<ArrayList<MyPermissionInfo>> groups;
    private ArrayList<String> groupsTitle;
    private ArrayList<MyPermissionInfo> privacyPermissionList;//隐私权限
    private ArrayList<MyPermissionInfo> otherPermissionList;//其他权限
    private ArrayList<MyPermissionInfo> newPermissionList;//新权限
    private String noPermissionHintStr;//没有权限提示语

    private InstallConfirmAdapter adapter;
    private ExpandableListView expandableListView;
    private TextView permissionHintText;
    AppSecurityPermissions appSecurityPermissions;
    private Activity activity;
    private boolean isFirstInstall;
    private boolean needExpandGroup;
    private RelativeLayout permissionListLayout;
    private TextView modifyHintText;
    
    
    public ExpandableListViewUtils(Activity activity,
    		AppSecurityPermissions appSecurityPermissions,
    		boolean isFirstInstall,
    		boolean needExpandGroup){
    	if(activity == null || 
    			activity.isFinishing() || 
    			appSecurityPermissions == null){
    		return ;
    	}
    	this.activity = activity;
    	this.appSecurityPermissions = appSecurityPermissions;
    	this.isFirstInstall = isFirstInstall;
    	this.needExpandGroup = needExpandGroup;
    	initView();
    	initData();
    }
    
    private void initView(){
    	expandableListView = (ExpandableListView)activity.findViewById(R.id.expandableListView);
    	permissionHintText = (TextView)activity.findViewById(R.id.permissionHintText);	
    	permissionListLayout = (RelativeLayout)activity.findViewById(R.id.permissionListLayout);
    	modifyHintText =  (TextView)activity.findViewById(R.id.modifyHintText);	
    }
        
    private void initData(){
    	initOrUpdateListData();
    }
    
    /**
     * 初始化或更新列表数据
     */
    private void initOrUpdateListData(){ 	
    	if(expandableListView == null){
    		expandableListView = (ExpandableListView)activity.findViewById(R.id.expandableListView);
    	}

    	if(isFirstInstall){
    		noPermissionHintStr = activity.getResources().getString(R.string.app_not_apply_permission);
    		initOrUpdateListDataForFirstInstall();
    	}else{
    		noPermissionHintStr = activity.getResources().getString(R.string.new_version_not_apply_new_permission);
    		initOrUpdateListDataForSoftUpdate();
    	}
    	   	
    	if(adapter == null){
    		adapter = new InstallConfirmAdapter(activity,groups,groupsTitle);
    		expandableListView.setAdapter(adapter);
        	expandableListView.setOnGroupCollapseListener(new OnGroupCollapseListener(){
    			@Override
    			public void onGroupCollapse(int groupPosition) {
    				// TODO Auto-generated method stub   				
    			}});
        	expandableListView.setOnGroupExpandListener(new OnGroupExpandListener(){
    			@Override
    			public void onGroupExpand(int groupPosition) {
    				// TODO Auto-generated method stub				
    			}});
    	}else{
    		adapter.notifyDataSetChanged();
    	}
    	
    	if(permissionListLayout != null && modifyHintText != null){
        	permissionListLayout.setVisibility(View.VISIBLE);
        	modifyHintText.setVisibility(View.VISIBLE);
    	}
   	
    	if(groups.size() == 0){
//    		if(isFirstInstall && permissionListLayout != null && modifyHintText != null){
    		if(permissionListLayout != null && modifyHintText != null){
    	    	permissionListLayout.setVisibility(View.GONE);
    	    	modifyHintText.setVisibility(View.GONE);
    		}else{
        		permissionHintText.setVisibility(View.VISIBLE);
        		permissionHintText.setText(noPermissionHintStr);
        		expandableListView.setVisibility(View.GONE);
    		}
//    		View marginView104 = activity.findViewById(R.id.marginView104);
//    		if (marginView104 != null) {
//    			marginView104.setVisibility(View.VISIBLE);
//    		}
    	}else{
    		permissionHintText.setVisibility(View.GONE);
    		expandableListView.setVisibility(View.VISIBLE);
    		if(needExpandGroup){
    			expandableListView.expandGroup(0);
    		}
//    		View marginView22 = activity.findViewById(R.id.marginView22);
//    		if (marginView22 != null) {
//    			marginView22.setVisibility(View.VISIBLE);
//    		}
    	} 	
    }
    
    /**
     * 第一次安装软件初始化列表数据
     */
    private void initOrUpdateListDataForFirstInstall(){
    	if(privacyPermissionList == null){
    		privacyPermissionList = new ArrayList<MyPermissionInfo>();
    	}else{
    		privacyPermissionList.clear();
    	}
    	
    	if(otherPermissionList == null){
    		otherPermissionList = new ArrayList<MyPermissionInfo>();
    	}else{
    		otherPermissionList.clear();
    	}
    	
    	ArrayList<MyPermissionInfo> permissionNameList = 
    			appSecurityPermissions.getPermissionList();
    	
    	if(permissionNameList == null){
    		return ;
    	}
    	
    	for(int i=0;i< permissionNameList.size();i++){
    		MyPermissionInfo tmpPermissionInfo = permissionNameList.get(i);
    		if(tmpPermissionInfo == null){
    			continue;
    		}
    		if(ApkUtils.isDangerPermission(tmpPermissionInfo)){
    			privacyPermissionList.add(tmpPermissionInfo);
    		}else{
    			otherPermissionList.add(tmpPermissionInfo);
    		}
    	}
    	
    	//更新 groups
    	if(groups == null){
    		groups = new ArrayList<ArrayList<MyPermissionInfo>>();
    	}else{
    		groups.clear();
    	}
    	if(privacyPermissionList.size()>0){
    		groups.add(privacyPermissionList);	
    	}
	    if(otherPermissionList.size()>0){
	    	groups.add(otherPermissionList);
	    }    
	       
	    //更新 groupsTitle
	    if(groupsTitle == null){
	    	groupsTitle = new ArrayList<String>();
	    }else{
	    	groupsTitle.clear();
	    }
	    if(privacyPermissionList.size()>0){
	    	groupsTitle.add(
	    			activity.getResources().getString(R.string.privacy_permission)+
		    		activity.getResources().getString(R.string.left_brackets)+
		    		privacyPermissionList.size()+
		    		activity.getResources().getString(R.string.right_brackets));	
    	}
	    if(otherPermissionList.size()>0){
	    	groupsTitle.add(
	    			activity.getResources().getString(R.string.other_permission)+
		    		activity.getResources().getString(R.string.left_brackets)+
		    		otherPermissionList.size()+
		    		activity.getResources().getString(R.string.right_brackets));
	    }	    
    }
    
    /**
     * 软件更新初始化列表数据
     */
    private void initOrUpdateListDataForSoftUpdate(){
    	if(newPermissionList == null){
    		newPermissionList = new ArrayList<MyPermissionInfo>();
    	}else{
    		newPermissionList.clear();
    	}
    	
    	ArrayList<MyPermissionInfo> permissionNameList = 
    			appSecurityPermissions.getNewPermissionList();
    	if(permissionNameList == null){
    		return ;
    	}
    	
    	for(int i=0;i< permissionNameList.size();i++){
    		MyPermissionInfo tmpPermissionInfo = permissionNameList.get(i);
    		if(tmpPermissionInfo == null){
    			continue;
    		}
    		newPermissionList.add(tmpPermissionInfo);
    	}
    	
    	//更新 groups
    	if(groups == null){
    		groups = new ArrayList<ArrayList<MyPermissionInfo>>();
    	}else{
    		groups.clear();
    	}
    	if(newPermissionList.size()>0){
    		groups.add(newPermissionList);	
    	}  
	       
	    //更新 groupsTitle
	    if(groupsTitle == null){
	    	groupsTitle = new ArrayList<String>();
	    }else{
	    	groupsTitle.clear();
	    }
	    if(newPermissionList.size()>0){
	    	groupsTitle.add(
	    			activity.getResources().getString(R.string.new_permission)+
		    		activity.getResources().getString(R.string.left_brackets)+
		    		newPermissionList.size()+
		    		activity.getResources().getString(R.string.right_brackets));	
    	}	    
    }
}
