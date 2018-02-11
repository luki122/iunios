package com.aurora.puremanager.adapter;

import java.util.HashSet;
import java.util.List;

import com.aurora.puremanager.R;
import com.aurora.puremanager.activity.AddAutoStartAppActivity;
import com.aurora.puremanager.data.AppInfo;
import com.aurora.puremanager.data.AutoStartData;
import com.aurora.puremanager.data.BaseData;
import com.aurora.puremanager.imageloader.ImageCallback;
import com.aurora.puremanager.imageloader.ImageLoader;
import com.aurora.puremanager.model.AutoStartModel;
import com.aurora.puremanager.provider.open.AutoStartAppProvider;
import com.aurora.puremanager.utils.ApkUtils;
import com.aurora.puremanager.viewcache.AddAutoStartCache;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;

public class AddAutoStartAppAdapter extends ArrayAdapter<BaseData> implements OnClickListener{      
	private Activity activity;
	
	public AddAutoStartAppAdapter(Activity activity,
			List<BaseData> listData) {		
		super(activity, 0, listData);
		this.activity = activity;
	}
	
	@Override 
	public View getView(int position, View convertView, final ViewGroup parent) {	
		AddAutoStartCache holder;
		if (convertView == null) {
			LayoutInflater inflater = activity.getLayoutInflater(); 
			convertView = inflater.inflate(R.layout.add_auto_start_list_item, parent, false);
			holder = new AddAutoStartCache(convertView);
			convertView.setTag(holder);
		} else {
			holder = (AddAutoStartCache)convertView.getTag();
		}
		
		if(getCount()<=position){
			return convertView;
		}
		
		HashSet<String> autostartApps = AutoStartAppProvider.loadAutoStartAppListInDB(getContext().getApplicationContext());
		
		AppInfo item =(AppInfo)getItem(position);
		holder.getAppName().setText(item.getAppName());
		holder.getAddBtn().setTag(position);
		holder.getAddBtn().setOnClickListener(this);	
		AutoStartData autoStartData = AutoStartModel.getInstance(getContext()).
				getAutoStartData(item.getPackageName());
		if(autoStartData != null && autostartApps.contains(item.getPackageName())){
			holder.getAddBtn().setVisibility(View.GONE);
			holder.getAlreadyAdded().setVisibility(View.VISIBLE);
		}else{
			holder.getAddBtn().setVisibility(View.VISIBLE);
			holder.getAlreadyAdded().setVisibility(View.GONE);
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
			
			AppInfo item =(AppInfo)getItem(position);
			switch(v.getId()){
			case R.id.addBtn:	
/*				AutoStartModel.getInstance(getContext()).changeAutoStartState(item.getPackageName(),true);*/
				AutoStartModel.getInstance(getContext()).tryChangeAutoStartState(item.getPackageName(),true);
				notifyDataSetChanged();	
				break;
			}			
		}
	}

}
