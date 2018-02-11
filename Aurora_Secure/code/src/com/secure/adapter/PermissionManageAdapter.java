package com.secure.adapter;

import java.util.ArrayList;
import java.util.List;
import com.aurora.secure.R;
import com.lbe.security.service.sdkhelper.SDKConstants;
import com.secure.activity.PermissionManageActivity;
import com.secure.data.AppInfo;
import com.secure.data.BaseData;
import com.secure.data.PermissionInfo;
import com.secure.imageloader.ImageCallback;
import com.secure.imageloader.ImageLoader;
import com.secure.stickylistheaders.StickyListHeadersAdapter;
import com.secure.utils.ApkUtils;
import com.secure.utils.StringUtils;
import com.secure.viewcache.PermissionManageItemCache;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

/**
 * 这个listView悬停会存在一个风险，当两个（或多个）title的第一个字母（或汉字）是一样的时候，
 * 后面的title就会显示不出来，因为title控制是否显示就是根据第一个字符是否一样，如果第一个字符不一样才显示，
 * 如果第一个字符一样的话，就不显示。解决办法：见 PermissionDetailAdapter 或 DefSoftManagelistAdapter
 */
public class PermissionManageAdapter extends ArrayAdapter<BaseData> implements 
                           StickyListHeadersAdapter{      	
	private String needOptimalStr,alreadyOptimalStr;
	private int needOptimalAppNum;//需要优化的权限数
	private ArrayList<OptimizeState> appOptimizeState = new ArrayList<OptimizeState>();
	
	public PermissionManageAdapter(Activity activity,
			List<BaseData> listData) {		
		super(activity, 0, listData);
		needOptimalStr = getContext().getString(R.string.recom_optimal);
		alreadyOptimalStr = getContext().getString(R.string.already_optimal);	
		updateAppOptimizeState();
	}
	
	public void updateAppOptimizeState(){
		needOptimalAppNum = 0;
		if(getCount() > 0){
			appOptimizeState.clear();
			for(int i=0;i<getCount();i++){
				OptimizeState state = new OptimizeState(((AppInfo)getItem(i)).getIsNeedOptimize());
				if(state.needOptimize){
					needOptimalAppNum++;
				}
				appOptimizeState.add(state);
			}
		}		
	}
	
	private String getLabelText(int position){
		String labelText = "";		
		if(position < appOptimizeState.size() && appOptimizeState.get(position).needOptimize){
			labelText = needOptimalAppNum+needOptimalStr;
		}else{
			labelText = alreadyOptimalStr;
		}
		return labelText;
	}

	@Override 
	public View getView(int position, View convertView, final ViewGroup parent) {	
		PermissionManageItemCache holder;
		if (convertView == null) {
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater(); 
			convertView = inflater.inflate(R.layout.permission_manage_list_item, parent, false);
			holder = new PermissionManageItemCache(convertView);
			convertView.setTag(holder);
		} else {
			holder = (PermissionManageItemCache)convertView.getTag();
		}
		
		if(getCount()<=position){
			return convertView;
		}
		
		AppInfo item =(AppInfo)getItem(position);
		if(item.getAppName() != null) {
			holder.getAppName().setText(item.getAppName().trim());
		}
		holder.getSubText().setText(createSubStr(item));	
		
		String iconViewTag = item.getPackageName()+"@app_icon";
		holder.getAppIcon().setTag(iconViewTag);
		Drawable cachedImage = ImageLoader.getInstance(getContext()).displayImage(
				holder.getAppIcon(),
				item.getPackageName(), 
				iconViewTag, 
			new ImageCallback() {
				public void imageLoaded(Drawable imageDrawable, Object viewTag) {
					if(parent == null || 
							imageDrawable == null || 
									viewTag == null){
						return ;
					}
					ImageView imageViewByTag = (ImageView)parent.findViewWithTag(viewTag);
					if (imageViewByTag != null) {
						imageViewByTag.setImageDrawable(imageDrawable);
					}
				}
		});
		if (cachedImage != null) {
			holder.getAppIcon().setImageDrawable(cachedImage);
		}else{
			holder.getAppIcon().setImageResource(R.drawable.def_app_icon);
		}
		return convertView;
	}
	
	/**
	 * @param item
	 * @return
	 */
	private String createSubStr(AppInfo item){
		int permissionNum = item.getPermission() == null?0:item.getPermission().size();
		
		String subTextStr = permissionNum+
				getContext().getString(R.string.jurisdiction_num);
		
		if(item.getIsNeedOptimize()){
			int perNumOfNeedClose = 0 , perNumOfNeedOpen = 0;
			for(int i=0;i<permissionNum;i++){
				PermissionInfo permissionInfo = item.getPermission().get(i);
				if(ApkUtils.isDangerPermission(getContext(), item, permissionInfo)){
					if(permissionInfo.getIsOpen()){
						perNumOfNeedClose ++;
					}
				}else{
					//如果用户已经关掉了一个应用的联网权限，按照产品需求不需要重新打开
					if(permissionInfo.permId != SDKConstants.PERM_ID_NETDEFAULT &&
							(!permissionInfo.getIsOpen())){
						perNumOfNeedOpen++;
					}
				}
			}
			if(perNumOfNeedClose>0){
				subTextStr = subTextStr+
						getContext().getString(R.string.comma)+
						perNumOfNeedClose+
						getContext().getString(R.string.need_close_permisson_num);
						
			}
			if(perNumOfNeedOpen>0){
				subTextStr = subTextStr+
						getContext().getString(R.string.comma)+
						perNumOfNeedOpen+
						getContext().getString(R.string.need_open_permisson_num);
			}				
		}	
		return subTextStr;
	}

	@Override 
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {		
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater(); 
			convertView = inflater.inflate(R.layout.permission_manage_list_header, parent, false);
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
	
	public void releaseObject(){
		appOptimizeState.clear();
	}
	
	final class OptimizeState{
		public OptimizeState(boolean needOptimize){
			this.needOptimize = needOptimize;
		}
		boolean needOptimize;
	}
}
