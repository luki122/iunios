package com.secure.adapter;

import java.util.List;
import com.aurora.secure.R;
import com.secure.data.AppInfo;
import com.secure.data.BaseData;
import com.secure.imageloader.ImageCallback;
import com.secure.imageloader.ImageLoader;
import com.secure.model.AppInfoModel;
import com.secure.model.AutoStartModel;
import com.secure.model.ConfigModel;
import com.secure.utils.ApkUtils;
import com.secure.view.InfoDialog;
import com.secure.viewcache.AutoStartManageCache;
import com.secure.activity.AutoStartManageActivity;
import com.secure.activity.MainActivity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

public class AutoStartManageAdapter extends ArrayAdapter<BaseData> implements OnClickListener{      
	
	public AutoStartManageAdapter(Activity activity,
			List<BaseData> listData) {		
		super(activity, 0, listData);
	}
	
	@Override 
	public View getView(int position, View convertView, final ViewGroup parent) {	
		AutoStartManageCache holder;
		if (convertView == null) {
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater(); 
			convertView = inflater.inflate(R.layout.auto_start_manage_list_item, parent, false);
			holder = new AutoStartManageCache(convertView);
			convertView.setTag(holder);
		} else {
			holder = (AutoStartManageCache)convertView.getTag();
		}
		
		if(getCount()<=position){
			return convertView;
		}
		
		AppInfo item =(AppInfo)getItem(position);
		holder.getAppName().setText(item.getAppName());	
		holder.getAutoStartSwitch().setTag(position);
		holder.getAutoStartSwitch().setOnClickListener(this);	
		
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
			
			final AppInfo item =(AppInfo)getItem(position);
			switch(v.getId()){
			case R.id.autoStartSwitch:
				InfoDialog.showDialog((Activity)getContext(), 
					R.string.forbit_auto_start,
					android.R.attr.alertDialogIcon,
					R.string.are_you_sure_forbit_auto_start, 
					R.string.sure,
					new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int which) {
	                    	/*AutoStartModel.getInstance(getContext()).changeAutoStartState(item.getPackageName(),false);*/
	                    	AutoStartModel.getInstance(getContext()).tryChangeAutoStartState(item.getPackageName(),false);
	        				if( getContext() instanceof AutoStartManageActivity){
	        					((AutoStartManageActivity)getContext()).initOrUpdatetListData();    			
	        				}	
	                    }
	                },             
	                R.string.cancel,
					null,
					null);
				break;
			}			
		}
	}
	


}
