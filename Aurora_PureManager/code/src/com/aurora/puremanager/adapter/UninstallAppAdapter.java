package com.aurora.puremanager.adapter;

import java.util.List;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import com.aurora.puremanager.R;
import com.aurora.puremanager.data.AppInfo;
import com.aurora.puremanager.data.BaseData;
import com.aurora.puremanager.imageloader.ImageCallback;
import com.aurora.puremanager.imageloader.ImageLoader;
import com.aurora.puremanager.viewcache.UninstallAppCache;

public class UninstallAppAdapter extends ArrayAdapter<BaseData> implements OnClickListener{      
	private Activity activity;
	
	public UninstallAppAdapter(Activity activity,
			List<BaseData> listData) {		
		super(activity, 0, listData);
		this.activity = activity;
	}
	
	@Override 
	public View getView(int position, View convertView, final ViewGroup parent) {	
		UninstallAppCache holder;
		if (convertView == null) {
			LayoutInflater inflater = activity.getLayoutInflater(); 
			convertView = inflater.inflate(R.layout.uninstall_app_list_item, parent, false);
			holder = new UninstallAppCache(convertView);
			convertView.setTag(holder);
		} else {
			holder = (UninstallAppCache)convertView.getTag();
		}
		
		if(getCount()<=position){
			return convertView;
		}
		
		AppInfo item =(AppInfo)getItem(position);
		holder.getAppName().setText(item.getAppName());
		holder.getAddBtn().setTag(position);
		holder.getAddBtn().setOnClickListener(this);	
		
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
			
			AppInfo item =(AppInfo)getItem(position);
			switch(v.getId()){
			case R.id.uninstallBtn:
				Intent intent = new Intent();
				intent.setAction("android.intent.action.DELETE");
				intent.addCategory("android.intent.category.DEFAULT");
				intent.setData(Uri.parse("package:"+ item.getPackageName()));
				activity.startActivity(intent);
				notifyDataSetChanged();
				break;
			}			
		}
	}
}
