package com.aurora.puremanager.adapter;

import java.util.List;

import com.aurora.puremanager.R;
import com.aurora.puremanager.imageloader.ImageCallback;
import com.aurora.puremanager.imageloader.ImageLoader;
import com.aurora.puremanager.model.AutoStartModel;
import com.aurora.puremanager.model.RunningState;
import com.aurora.puremanager.utils.ApkUtils;
import com.aurora.puremanager.utils.StringUtils;
import com.aurora.puremanager.utils.Utils;
import com.aurora.puremanager.view.InfoDialog;
import com.aurora.puremanager.viewcache.AllAppListItemCache;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;

import com.aurora.puremanager.activity.AllAppListActivity;
import com.aurora.puremanager.activity.AutoStartManageActivity;
import com.aurora.puremanager.data.AppInfo;
import com.aurora.puremanager.data.BaseData;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

public class AllAppListAdapter extends ArrayAdapter<BaseData> implements OnClickListener{      

	private View parent;
	private int sortType;
	
	public AllAppListAdapter(Context context,
			List<BaseData> listData) {		
		super(context, 0, listData);
	}
	
	public void setSortType(int sortType){
		this.sortType = sortType;
	}
	
	@Override 
	public View getView(int position, View convertView, final ViewGroup parent) {
		this.parent = parent;
		AllAppListItemCache holder;
		if (convertView == null){
			convertView = LayoutInflater.from(getContext()).
					inflate(R.layout.all_app_list_item, parent, false);
			holder = new AllAppListItemCache(convertView);		
			convertView.setTag(holder);
		} else {
			holder = (AllAppListItemCache)convertView.getTag();
		}
				
		if(getCount()<=position){
			return convertView;
		}
		AppInfo item =(AppInfo)getItem(position);
		
		if(sortType == AllAppListActivity.SORT_BY_RunningProcesses){
			holder.getStopBtn().setVisibility(View.VISIBLE);
			holder.getSubText().setVisibility(View.VISIBLE);
			holder.getSubText().setText(
				Utils.dealMemorySize(getContext(), item.getMemorySize(),"%.1f"));
		}else{
			holder.getStopBtn().setVisibility(View.GONE);
			holder.getSubText().setVisibility(View.GONE);
		}
					
		holder.getAppName().setText(item.getAppName());	
		holder.getStopBtn().setTag(position);
		holder.getStopBtn().setOnClickListener(this);
		
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
			case R.id.stopBtn:
				InfoDialog.showDialog((Activity)getContext(), 
					R.string.force_stop_dlg_title,
					android.R.attr.alertDialogIcon,
					R.string.force_stop_dlg_text,
					R.string.sure,
					new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int which) {
	                    	forceStopPackage(item.getPackageName());
	                    }
	                }, 
	                R.string.cancel,
					null,
					null);
				break;
			}			
		}
	}
	
	/**
     * 强行停止一个应用
     * @param pkgName
     */
    private void forceStopPackage(String pkgName) {
   	    ActivityManager am = (ActivityManager)getContext().getSystemService(Context.ACTIVITY_SERVICE);
        am.forceStopPackage(pkgName);
        RunningState.getInstance(getContext()).deleteProcess(pkgName);
    }
}
