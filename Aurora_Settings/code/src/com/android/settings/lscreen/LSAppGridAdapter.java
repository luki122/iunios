package com.android.settings.lscreen;

import java.util.ArrayList;
import java.util.List;

import com.android.settings.R;
import com.android.settings.lscreen.ls.LSCustomPreference;
import com.android.settings.lscreen.ls.LSOperator;

import com.secure.imageloader.ImageCallback;
import com.secure.imageloader.ImageLoader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class LSAppGridAdapter extends ArrayAdapter<BaseData>{

	private final ArrayList<View> itemViewList;
	private ViewGroup parent;
	private Activity activity;

	public LSAppGridAdapter(Activity activity, List<BaseData> appList) {
		super(activity, 0, appList);
		this.activity = activity;
		itemViewList = new ArrayList<View>();
	}
	
	@Override 
	public View getView(int position, View convertView, final ViewGroup parent) {
		this.parent = parent;
		LSAppGridItemCache holder;
		if (convertView == null) {
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater(); 
			convertView = inflater.inflate(R.layout.privacy_app_grid_item, parent, false);
			holder = new LSAppGridItemCache(convertView);		
			convertView.setTag(holder);
			itemViewList.add(convertView);
		} else {
			holder = (LSAppGridItemCache)convertView.getTag();
		}
				
		if(getCount()<=position){
			return convertView;
		}	
		AppInfo item =(AppInfo)getItem(position);				
		holder.getItemLayout().setVisibility(View.VISIBLE);
		holder.getItemLayout().setTag(item.getPackageName());
//		holder.getImgFlag().setTag(item.getPackageName()+"@choice_flag");		
		String iconViewTag = item.getPackageName()+"@app_icon";
		holder.getAppIcon().setTag(iconViewTag);
		
		if(position == getCount()-1 && 
				LSCustomPreference.ADD_ITEM_FLAG.equals(item.getPackageName())){
			 holder.getAppIcon().setBackgroundResource(R.drawable.add_privacy_app_icon);
//			 dealAddItemShowState(holder.getItemLayout());
			 holder.getAppName().setVisibility(View.INVISIBLE);
			 holder.getImgFlag().setVisibility(View.INVISIBLE);
			 return convertView;
		}else{
			holder.getAppName().setVisibility(View.VISIBLE);
		}
		holder.getAppName().setText(item.getAppName());
//		if(isChoiced(item.getPackageName())){
//			holder.getImgFlag().setVisibility(View.VISIBLE);
//		}else{
//			holder.getImgFlag().setVisibility(View.INVISIBLE);
//		}
		//  图片加载技术
//		Log.d("gd", "item.getPackageName()="+item.getPackageName()+" iconViewTag="+iconViewTag );
		Drawable cachedImage = ImageLoader.getInstance(getContext()).displayImage(
				holder.getAppIcon(),
				item.getPackageName(), 
				iconViewTag, 
			new ImageCallback() {
				public void imageLoaded(Drawable imageDrawable, Object viewTag) {
					if(parent == null || imageDrawable == null || viewTag == null){
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
		itemViewList.clear();
	}
	
	public void dealClickEvent(int position)
	{
		if(position>getCount()-1)
		{
			return ;
		}
	    AppInfo item= (AppInfo) getItem(position);
	    if(item==null)
	    {
	    	return ;
	    }
	    if(item.getPackageName().equals(LSOperator.IUNI_CONTACTS))
	    {
	    	return ;
	    }
		final String packageName = item.getPackageName();
		Log.d("gd", "onclick packagename="+packageName+"  appname="+item.getAppName());
		Intent intent = new Intent(activity, LSAppActivity.class);
		Bundle bundle=new Bundle();
		bundle.putString(LSAppActivity.PKGNAME, packageName);
		intent.putExtra(LSAppActivity.BUNDLE_STAG, bundle);
		activity.startActivity(intent);
		
    	final DataArrayList<String> appDatas =new DataArrayList<String>();
        if(packageName!=null)
        {
        	appDatas.add(packageName);
        	AuroraLSManageModel.getInstance(activity).delLSApp(appDatas);
        }
		
	}

}
