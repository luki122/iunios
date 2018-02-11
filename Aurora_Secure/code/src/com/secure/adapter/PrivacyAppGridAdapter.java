package com.secure.adapter;

import java.util.ArrayList;
import java.util.List;
import com.aurora.secure.R;
import com.secure.activity.PrivacyAppActivity;
import com.secure.data.AppInfo;
import com.secure.data.BaseData;
import com.secure.data.MyArrayList;
import com.secure.imageloader.ImageCallback;
import com.secure.imageloader.ImageLoader;
import com.secure.utils.ApkUtils;
import com.secure.utils.StringUtils;
import com.secure.viewcache.PrivacyAppGridItemCache;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class PrivacyAppGridAdapter extends ArrayAdapter<BaseData>{  
	private final MyArrayList<String> choiceAppList;//存放选中的packageName
	private final ArrayList<View> itemViewList;
	private ViewGroup parent;
	private Activity activity;

	public PrivacyAppGridAdapter(Activity activity,
			List<BaseData> appList) {		
		super(activity, 0, appList);
		this.activity = activity;
		choiceAppList = new MyArrayList<String>();
		itemViewList = new ArrayList<View>();
	}
	
	@Override 
	public View getView(int position, View convertView, final ViewGroup parent) {
		this.parent = parent;
		PrivacyAppGridItemCache holder;
		if (convertView == null) {
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater(); 
			convertView = inflater.inflate(R.layout.privacy_app_grid_item, parent, false);
			holder = new PrivacyAppGridItemCache(convertView);		
			convertView.setTag(holder);
			itemViewList.add(convertView);
		} else {
			holder = (PrivacyAppGridItemCache)convertView.getTag();
		}
				
		if(getCount()<=position){
			return convertView;
		}	
		AppInfo item =(AppInfo)getItem(position);				
		holder.getItemLayout().setVisibility(View.VISIBLE);
		holder.getItemLayout().setTag(item.getPackageName());
		holder.getImgFlag().setTag(item.getPackageName()+"@choice_flag");		
		String iconViewTag = item.getPackageName()+"@app_icon";
		holder.getAppIcon().setTag(iconViewTag);
		
		if(position == getCount()-1 && 
				PrivacyAppActivity.ADD_ITEM_FLAG.equals(item.getPackageName())){
			 holder.getAppIcon().setBackgroundResource(R.drawable.add_privacy_app_icon);
			 dealAddItemShowState(holder.getItemLayout());
			 holder.getAppName().setVisibility(View.INVISIBLE);
			 holder.getImgFlag().setVisibility(View.INVISIBLE);
			 return convertView;
		}else{
			holder.getAppName().setVisibility(View.VISIBLE);
		}
		holder.getAppName().setText(item.getAppName());
		if(isChoiced(item.getPackageName())){
			holder.getImgFlag().setVisibility(View.VISIBLE);
		}else{
			holder.getImgFlag().setVisibility(View.INVISIBLE);
		}	
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
						imageViewByTag.setBackground(imageDrawable);
					}
				}
		});
		if (cachedImage != null) {
			holder.getAppIcon().setBackground(cachedImage);
		}else{
			holder.getAppIcon().setBackgroundResource(R.drawable.def_app_icon);
		}
		
		return convertView;
	}
	
	 /**
     * 释放不需要用的对象所占用的堆内存
     */
	public void releaseObject(){		
		choiceAppList.clear();
		itemViewList.clear();
	}
	
/*************************处理选中事件代码  begin*************************/
	/**
	 * 处理添加项的显示状态（显示或者隐藏）
	 */
	public void dealAddItemShowState(){
		if(activity == null || parent == null){
			return ;
		}
		FrameLayout itemLayout = (FrameLayout)parent.findViewWithTag(
				PrivacyAppActivity.ADD_ITEM_FLAG);
		dealAddItemShowState(itemLayout);
	}
	
	/**
	 * 处理添加项的显示状态（显示或者隐藏）
	 */
	private void dealAddItemShowState(FrameLayout itemLayout){
		if(activity == null || parent == null || itemLayout == null){
			return ;
		}
		if(((PrivacyAppActivity)activity).getChoiceState()){
			 itemLayout.setVisibility(View.INVISIBLE);
		}else{
			 itemLayout.setVisibility(View.VISIBLE);
		}	
	}
	
	/**
	 * 处理onItemClick事件
	 * @param position
	 */
	public void dealItemClick(int position){
		if(getCount()<=position){
			return ;
		}
		
		AppInfo item =(AppInfo)getItem(position);
		if(item == null){
			return ;
		}
		ImageView choiceFlag = (ImageView)parent.findViewWithTag(item.getPackageName()+"@choice_flag");
		if(isChoiced(item.getPackageName())){//表示之前是选中状态
			choiceAppList.remove(item.getPackageName());
			if(choiceFlag != null){
				choiceFlag.setVisibility(View.INVISIBLE);
			}
		}else{
			if(!PrivacyAppActivity.ADD_ITEM_FLAG.equals(item.getPackageName())){
				choiceAppList.add(item.getPackageName());	
				if(choiceFlag != null){
					choiceFlag.setVisibility(View.VISIBLE);
				}
			}			
		}		
	}
	
	/**
	 * @return 返回值不可能为null
	 */
	public MyArrayList<String> getChoiceAppList(){
		return choiceAppList;
	}
	
	/**
	 * 判断当前是不是全选
	 * @return
	 */
	public boolean isAllChoice(){	
		if(choiceAppList.size() == getCount()-1){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 取消已经选择的清理项
	 * @return
	 */
	public void cancelChoiced(){
		choiceAppList.clear();
		
		PrivacyAppGridItemCache holder;
		View convertView;
		for(int i =0;i < itemViewList.size(); i++){
			convertView = itemViewList.get(i);
			holder = (PrivacyAppGridItemCache)convertView.getTag();		
			holder.getImgFlag().setVisibility(View.INVISIBLE);
		}	
	}
	
	/**
	 * 全选
	 */
	public void checkAllItem(){
		choiceAppList.clear();
		for(int i=0;i<getCount();i++){
			AppInfo item =(AppInfo)getItem(i);
			if(item == null){
				continue;
			}
			if(!PrivacyAppActivity.ADD_ITEM_FLAG.equals(item.getPackageName())){
				choiceAppList.add(item.getPackageName());
			}			
		}
		notifyDataSetChanged();
	}
	
	/**
	 * 判断当前packageName对应的软件需不需要清理
	 * @param packageName
	 * @return
	 */
	private boolean isChoiced(String packageName){
		if(StringUtils.isEmpty(packageName)){
			return false;
		}
		
		for(int i=0;i<choiceAppList.size();i++){
			String tmpStr = choiceAppList.get(i);
			if(packageName.equals(tmpStr)){
				return true;
			}
		}
		return false;		
	}
	
	public void deletePkgFromeChoiceAppList(String packageName){
		if(StringUtils.isEmpty(packageName)){
			return ;
		}
		
		for(int i=0;i<choiceAppList.size();i++){
			String tmpStr = choiceAppList.get(i);
			if(packageName.equals(tmpStr)){
				choiceAppList.remove(tmpStr);
				break;
			}
		}
	}
}
