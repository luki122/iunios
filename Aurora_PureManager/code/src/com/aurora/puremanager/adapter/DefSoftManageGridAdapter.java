package com.aurora.puremanager.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.aurora.puremanager.R;
import com.aurora.puremanager.activity.DefSoftManageActivity;
import com.aurora.puremanager.data.AppCategoryData;
import com.aurora.puremanager.data.BaseData;
import com.aurora.puremanager.data.DefStartAppInfo;
import com.aurora.puremanager.data.SameCategoryAppInfo;
import com.aurora.puremanager.imageloader.ImageCallback;
import com.aurora.puremanager.imageloader.ImageLoader;
import com.aurora.puremanager.model.DefSoftModel;
import com.aurora.puremanager.utils.ApkUtils;
import com.aurora.puremanager.utils.StringUtils;
import com.aurora.puremanager.viewcache.DefSoftManageGridItemCache;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

public class DefSoftManageGridAdapter extends ArrayAdapter<BaseData> implements OnClickListener{      

	private AppCategoryData curAppCategoryData;
	public DefSoftManageGridAdapter(Activity activity,
			AppCategoryData curAppCategoryData) {		
		super(activity, 0, curAppCategoryData.getAppList().getDataList());
		this.curAppCategoryData = curAppCategoryData;
	}
	
	@Override 
	public View getView(int position, View convertView, final ViewGroup parent) {
		DefSoftManageGridItemCache holder;
		if (convertView == null) {
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater(); 
			convertView = inflater.inflate(R.layout.def_soft_manage_grid_item, parent, false);
			holder = new DefSoftManageGridItemCache(convertView);		
			convertView.setTag(holder);
		} else {
			holder = (DefSoftManageGridItemCache)convertView.getTag();
		}
				
		if(getCount()<=position){
			return convertView;
		}
		
		SameCategoryAppInfo item =(SameCategoryAppInfo)getItem(position);		
		if(item.getIsSysApp()){
			holder.getSysAppFlag().setVisibility(View.VISIBLE);
		}else{
			holder.getSysAppFlag().setVisibility(View.GONE);
		}
		holder.getItemLayout().setTag(position);
		holder.getItemLayout().setOnClickListener(this);
		if(position == 0){
			if(isThisCategoryHaveDefSoft()){
				holder.getImgFlag().setVisibility(View.INVISIBLE);
			}else{
				holder.getImgFlag().setVisibility(View.VISIBLE);
			}			
		}else if(isDefStartApp(item)){
			holder.getImgFlag().setVisibility(View.VISIBLE);
		}else{
			holder.getImgFlag().setVisibility(View.INVISIBLE);
		}
		
		if(position == 0){
			holder.getAppIcon().setImageResource(R.drawable.cancel_def_soft_icon);
		}else{
			String iconViewTag = item.getResolveInfo()+"@app_icon";
			holder.getAppIcon().setTag(iconViewTag);
			Drawable cachedImage = ImageLoader.getInstance(getContext()).displayImage(
					holder.getAppIcon(),
					item.getResolveInfo(), 
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
		}		
		return convertView;
	}
	
	/**
	 * 判断当前的apk是不是这个类别中的默认软件
	 * @param item
	 * @return
	 */
	private boolean isDefStartApp(SameCategoryAppInfo item){
		if(item == null || 
				item.getResolveInfo() == null || 
				item.getResolveInfo().activityInfo == null ||
				item.getResolveInfo().activityInfo.packageName == null ||
				curAppCategoryData == null){
			return false;
		}	
		
		ActivityInfo activityInfo = item.getResolveInfo().activityInfo;		
		if(activityInfo.packageName.equals(curAppCategoryData.getCurDefStartPackageName()) &&
				activityInfo.name.equals(curAppCategoryData.getCurDefStartClass()) ){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 判断当前类别是否有默认软件
	 * @return true：有默认软件   false：没有默认软件
	 */
	private boolean isThisCategoryHaveDefSoft(){
		if(curAppCategoryData == null){
			return false;
		}
		if(StringUtils.isEmpty(curAppCategoryData.getCurDefStartPackageName())){
			return false;
		}else{
			return true;
		}
	}

	@Override
	public void onClick(View v) {
		Object tagObject= v.getTag();
		if(tagObject != null){
			int position = Integer.parseInt(tagObject.toString());
			if(getCount()<=position ||
					curAppCategoryData == null){
				return ;
			}
			
			SameCategoryAppInfo item =(SameCategoryAppInfo)getItem(position);		
			if(!StringUtils.isEmpty(curAppCategoryData.getCurDefStartPackageName())){
				if(getPreferredActivitiesNum(curAppCategoryData.getCurDefStartPackageName()) > 1){
					Toast.makeText(getContext(), 
							getContext().getString(R.string.alread_clear)+
							ApkUtils.getApkName(getContext(),ApkUtils.getApplicationInfo(getContext(), 
											curAppCategoryData.getCurDefStartPackageName()))+
							getContext().getString(R.string.def_setting), 
							Toast.LENGTH_SHORT).show();
				}				
				ApkUtils.clearDefStartApp(getContext(),curAppCategoryData.getCurDefStartPackageName());
			}	
			if(position != 0){//表示取消当前选择的默认软件
				setDefaultStartApp(item);
			}
			
			if(getContext() instanceof DefSoftManageActivity){
				DefSoftModel.getInstance(getContext()).findDefSoftOfAllCategory();
				((DefSoftManageActivity)getContext()).updateListView();
			}			
		}		
	}
	
	/**
	 * 获取某个应用设置了默认属性的个数
	 * @param packageName
	 * @return
	 */
	private int getPreferredActivitiesNum(String packageName){
		List<IntentFilter> intentList = new ArrayList<IntentFilter>();
		List<ComponentName> prefActList = new ArrayList<ComponentName>();  
		getContext().getPackageManager().getPreferredActivities(intentList, prefActList, packageName);
		return prefActList.size();
	}
		
	/**
	 * 设置默认启动的应用
	 * @param curItem
	 */
	private void setDefaultStartApp(SameCategoryAppInfo curItem){ 			    
        ArrayList <ResolveInfo> resolveInfoList = new ArrayList<ResolveInfo>();
        for(int i = 1; i < getCount(); i++){
        	SameCategoryAppInfo item =(SameCategoryAppInfo)getItem(i);
			if(item == null ||
					item.getResolveInfo() == null){ 
				continue;
			}
			resolveInfoList.add(item.getResolveInfo());
        }
        
        DefSoftModel.getInstance(getContext()).setDefSoft(curItem.getResolveInfo(), 
        		curAppCategoryData.getIntent(), 
        		resolveInfoList, true);	
    } 	
}
