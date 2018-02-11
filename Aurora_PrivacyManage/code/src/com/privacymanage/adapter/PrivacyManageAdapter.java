package com.privacymanage.adapter;

import java.util.List;
import com.aurora.privacymanage.R;
import com.privacymanage.data.BaseData;
import com.privacymanage.data.ModuleInfoData;
import com.privacymanage.utils.ApkUtils;
import com.privacymanage.utils.AsyncApkIconLoader;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PrivacyManageAdapter extends ArrayAdapter<BaseData> {  
	private AbsListView.LayoutParams convertViewLayoutParams;
	public PrivacyManageAdapter(Activity activity,
			List<BaseData> listData) {		
		super(activity, 0, listData);
		convertViewLayoutParams = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
	}

	@Override 
	public View getView(int position, View convertView, final ViewGroup parent) {			
		if (convertView == null) {
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater(); 
			convertView = inflater.inflate(R.layout.privacy_manage_grid_item, parent, false);			
		}
		int parentHeight = parent.getHeight();
		if(parentHeight >0){
			convertViewLayoutParams.height = parentHeight/2;
			convertView.setLayoutParams(convertViewLayoutParams);
		}
		
		if(getCount()<=position){
			return convertView;
		}

		ImageView icon = (ImageView)convertView.findViewById(R.id.Icon);
		TextView titleName = (TextView)convertView.findViewById(R.id.titleName);
		TextView hintText = (TextView)convertView.findViewById(R.id.hintText);
		
		ModuleInfoData item =(ModuleInfoData)getItem(position);
		titleName.setText(""+ApkUtils.getActivityLabel(getContext(),item.getPkgName(),item.getClassName()));
		hintText.setText(""+item.getItemNum());
		
		String iconViewTag = item.getPkgName()+"_"+item.getClassName()+"@app_icon";
		icon.setTag(iconViewTag);
		Drawable cachedImage = AsyncApkIconLoader.getInstance(getContext()).loadDrawable(
				item.getPkgName(), 
				item.getClassName(),
				iconViewTag, 
			new AsyncApkIconLoader.ImageCallback() {
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
			icon.setImageDrawable(cachedImage);
		}else{
//			icon.setImageResource(R.drawable.def_app_icon);
		}
		return convertView;
	}
}
