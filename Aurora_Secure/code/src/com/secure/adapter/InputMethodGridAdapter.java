package com.secure.adapter;

import java.util.List;
import java.util.Set;
import com.aurora.secure.R;
import com.secure.activity.DefSoftManageActivity;
import com.secure.data.AppCategoryData;
import com.secure.data.BaseData;
import com.secure.data.DefStartAppInfo;
import com.secure.data.SameCategoryAppInfo;
import com.secure.imageloader.ImageCallback;
import com.secure.imageloader.ImageLoader;
import com.secure.utils.ApkUtils;
import com.secure.utils.StringUtils;
import com.secure.viewcache.DefSoftManageGridItemCache;
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
import android.view.inputmethod.InputMethodInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

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
