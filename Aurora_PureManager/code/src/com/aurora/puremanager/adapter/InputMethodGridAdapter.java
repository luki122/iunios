package com.aurora.puremanager.adapter;

import java.util.List;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.aurora.puremanager.R;
import com.aurora.puremanager.activity.DefSoftManageActivity;
import com.aurora.puremanager.imageloader.ImageCallback;
import com.aurora.puremanager.imageloader.ImageLoader;
import com.aurora.puremanager.utils.ApkUtils;
import com.aurora.puremanager.viewcache.DefSoftManageGridItemCache;

public class InputMethodGridAdapter extends ArrayAdapter<InputMethodInfo> implements OnClickListener{      

	public InputMethodGridAdapter(Activity activity,
			List<InputMethodInfo> inputMethodInfos) {		
		super(activity, 0, inputMethodInfos);
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
		
		InputMethodInfo item =(InputMethodInfo)getItem(position);
		String pkgName = item.getPackageName(); 
		if(ApkUtils.isUserApp(ApkUtils.getApplicationInfo(getContext(),pkgName))){
			holder.getSysAppFlag().setVisibility(View.GONE);
		}else{
			holder.getSysAppFlag().setVisibility(View.VISIBLE);
		}
		holder.getItemLayout().setTag(position);
		holder.getItemLayout().setOnClickListener(this);
		if(item.getId().equals(ApkUtils.getCurrentInputMethodId(getContext()))){
			holder.getImgFlag().setVisibility(View.VISIBLE);
		}else{
			holder.getImgFlag().setVisibility(View.INVISIBLE);
		}
		
		
		
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

	@Override
	public void onClick(View v) {
		Object tagObject= v.getTag();
		if(tagObject != null){
			int position = Integer.parseInt(tagObject.toString());
			if(getCount()<=position){
				return ;
			}
			
			InputMethodInfo item =(InputMethodInfo)getItem(position);
			if(item != null){
				ApkUtils.setDefaultInputMethod(getContext(), item.getId());
				notifyDataSetChanged();
				((DefSoftManageActivity)getContext()).setInputMethodTitle();
			}		
		}		
	} 	
}
