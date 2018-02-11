package com.android.gallery3d.selectfragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraTextView;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;

import com.android.gallery3d.R;
import com.android.gallery3d.app.Gallery;
import com.android.gallery3d.util.NetworkUtil;
import com.android.gallery3d.xcloudalbum.CloudActivity;
import com.android.gallery3d.xcloudalbum.fragment.BasicFragment;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.android.gallery3d.xcloudalbum.tools.OperationUtil;
import com.android.gallery3d.xcloudalbum.tools.Utils;
import com.android.gallery3d.xcloudalbum.widget.SelectPopupAdapter;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;

public class XcloudMoveFragment extends BasicFragment{
	private CheckBox checkBox;
	private AuroraListView listView;
	private AuroraTextView titleView;
	private Button popCancel;
	private View mView;
	private List<CommonFileInfo> fileInfos;
	private SelectPopupAdapter adapter;
	private static final String TAG = "CloudSelectPopupWindow";
	private OperationUtil operationUtil;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//TODO Auto-generated method stub
		return inflater.inflate(R.layout.aurora_cloud_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		//TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
//		getAuroraActionBar().changeAuroraActionbarType(
//				AuroraActionBar.Type.Normal);
		initView();
		
		if (getActivity() instanceof CloudActivity) {
			operationUtil = ((CloudActivity) getActivity()).getOperationUtil();
		}

		
//		changeActionBarLayout();
	}

@Override
	public void onResume() {
		//TODO Auto-generated method stub
		super.onResume();
		showList();
	}

	//	public void showList(List<CommonFileInfo> fileInfos) {
//		this.fileInfos = fileInfos;
	public void showList() {
		if (this.fileInfos == null) {
			this.fileInfos = new ArrayList<CommonFileInfo>();
		}
		LayoutParams layoutParams = listView.getLayoutParams();
//		if(fileInfos.size()>3){
//			layoutParams.height = (int)getActivity().getResources().getDimension(R.dimen.aurora_album_listview_max_height);
//			listView.setLayoutParams(layoutParams);
//		}
		
		//wenyongzhe 2015.10.29 BUG:moveFragment click Home key start
		try {
			if(fileInfos != null && fileInfos.size()>=1){//wenyongzhe 2015.10.16
				for(int i=0; i<fileInfos.size(); i++){
					if(fileInfos.get(i).path ==null || "".equals(fileInfos.get(i).path)){
						fileInfos.remove(i);//wenyongzhe 去掉加号一行
						break;
					}
				}
				//wenyongzhe 2015.11.11 fileInfos=0 start
				if(fileInfos.size() == 0) {
					showEmptyView(true);
				} else {
					showEmptyView(false);
				}
				//wenyongzhe 2015.11.11 fileInfos=0 end
			}
		} catch (Exception e) {
		}
		//wenyongzhe 2015.10.29 BUG:moveFragment click Home key end
		
		adapter = new SelectPopupAdapter(getActivity(), fileInfos);
		listView.setAdapter(adapter);
		
	}
	//wenyongzhe 2015.11.11 fileInfos=0 start
	 private TextView mEmptyView;
	 private void showEmptyView(boolean show) {
    	if(show) {
	    	if(mEmptyView == null) {
	    		mEmptyView = new TextView(getActivity());
	    	}
	    	RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(
	    				RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT); 
	    	mEmptyView.setLayoutParams(params);
	    	mEmptyView.setPadding(0, 230, 0, 0);
	    	mEmptyView.setGravity(Gravity.CENTER_HORIZONTAL);
	    	mEmptyView.setText(R.string.aurora_xcloud_move);
	    	mEmptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	    	mEmptyView.setVisibility(View.GONE);
	    	
	    	((ViewGroup)listView.getParent()).removeView(mEmptyView);  
	    	((ViewGroup)listView.getParent()).addView(mEmptyView);  
	    	listView.setEmptyView(mEmptyView);
		} else {
			((ViewGroup)listView.getParent()).removeView(mEmptyView);  
			listView.setEmptyView(null);
		}
	}
	//wenyongzhe 2015.11.11 fileInfos=0 end

	public void setTitleText(String title) {
		if (titleView != null) {
			titleView.setText(title);
		}
	}

	private void initView() {
			//checkBox = (CheckBox) getView().findViewById(R.id.pop_checkbox);
			listView = (AuroraListView) getView().findViewById(R.id.pop_listView);
//			titleView = (AuroraTextView) getView().findViewById(R.id.pop_title);
//			popCancel = (Button) getView().findViewById(R.id.pop_cancel);
//			popCancel.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
////					dismiss();
//				}
//			});
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
//					if (position == 0) {
//						operationUtil.setMoveCreateAlbum(true);
//						operationUtil.setMoveCheckBox(!checkBox.isChecked());
//						operationUtil
//								.createAlbum(R.string.aurora_move_new_album,(CloudActivity)getActivity());
//						return;
//					}
					CommonFileInfo fileInfo = (CommonFileInfo) adapter
							.getItem(position);
					operationUtil.setMoveOrCopyTarget(fileInfo);
					operationUtil.moveOrCopyPhoto(fileInfo,
							!false);
				}
			});
			
	}

//	public void changeActionBarLayout() {
//		getCloudActivity().setRootView(getView());
//		getAuroraActionBar().changeAuroraActionbarType(
//				AuroraActionBar.Type.Normal);
//		getAuroraActionBar().changeItemLayout(R.layout.aurora_album_add,
//				R.id.aurora_album_add_liner);
//		ImageButton imageButton = (ImageButton) getAuroraActionBar().getRootView()
//				.findViewById(R.id.aurora_album_add);
////		LogUtil.d(TAG, "------getAuroraActionBar().getRootView():"+getAuroraActionBar()
////				.get);
////		+getAuroraActionBar().getRootView().findViewById(R.id.aurora_action_bar)
//		Log.e(TAG, "----------getAuroraActionBar():"+imageButton+"    ");
//		if (imageButton != null) {
//			if (!NetworkUtil.checkWifiNetwork(getCloudActivity())) {
//				imageButton.setClickable(false);
//				return;
//			}
//			imageButton.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {//移动到新建相册
//					operationUtil.setMoveCreateAlbum(true);
//					operationUtil.setMoveCheckBox(!checkBox.isChecked());
//					operationUtil.createAlbum(R.string.aurora_move_new_album,(CloudActivity)getActivity());
//				}
//			});
//		}
//		getCloudActivity().setAuroraActionBarTitle("移动到");
//	}
	
	public void setFileInfos(CloudActivity activity, List<CommonFileInfo> fileInfos) {
		
		//wenyongzhe 2015.10.16 disable screenshot start
		for(CommonFileInfo mCommonFileInfo : fileInfos){
			if(mCommonFileInfo.path.contains(activity.getResources().getString(R.string.aurora_system_screenshot)) 
//					||mCommonFileInfo.path.contains(activity.getResources().getString(R.string.aurora_album_screenshots))//wenyongzhe2015.11.19
							){//wenyongzhe 2015.11.10 language English bug
				fileInfos.remove(mCommonFileInfo);
				break;
			}
		}
		//wenyongzhe 2015.10.16 disable screenshot end
		
		this.fileInfos = fileInfos;
	}

}