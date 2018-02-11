package com.secure.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.aurora.secure.R;
import com.secure.activity.PrivacyAppActivity;
import com.secure.activity.AddPrivacyAppActivity;
import com.secure.data.AppInfo;
import com.secure.data.BaseData;
import com.secure.data.MyArrayList;
import com.secure.data.SameFirstCharAppData;
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
import android.widget.ImageView;
import android.widget.Toast;

public class AddPrivacyAppGridAdapter extends ArrayAdapter<BaseData> implements OnClickListener{      
	private MyArrayList<String> choiceAppList;//存放选中的packageName
	private ViewGroup parent;
	private Activity activity;
	
	public AddPrivacyAppGridAdapter(Activity activity,
			SameFirstCharAppData sameFirstCharAppData) {		
		super(activity, 0, sameFirstCharAppData.getAppList().getDataList());
		
		this.activity = activity;
		choiceAppList = ((AddPrivacyAppActivity)activity).getChoiceAppList();
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
		} else {
			holder = (PrivacyAppGridItemCache)convertView.getTag();
		}
				
		if(getCount()<=position){
			return convertView;
		}	
		AppInfo item =(AppInfo)getItem(position);				
		holder.getItemLayout().setTag(position);
		holder.getItemLayout().setOnClickListener(this);
		
		holder.getAppName().setText(item.getAppName());
		if(isChoiced(item.getPackageName())){
			holder.getImgFlag().setVisibility(View.VISIBLE);
		}else{
			holder.getImgFlag().setVisibility(View.INVISIBLE);
		}	
		holder.getImgFlag().setTag(item.getPackageName()+"@choice_flag");
		
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
	 * 判断当前packageName对应的软件需不需要清理
	 * @param packageName
	 * @return
	 */
	private boolean isChoiced(String packageName){
		if(choiceAppList == null || StringUtils.isEmpty(packageName)){
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

	@Override
	public void onClick(View v) {
		Object tagObject= v.getTag();
		if(tagObject != null){
			int position = Integer.parseInt(tagObject.toString());
			dealItemClick(position);			
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
			choiceAppList.add(item.getPackageName());	
			if(choiceFlag != null){
				choiceFlag.setVisibility(View.VISIBLE);
			}
		}
		((AddPrivacyAppActivity)activity).updateSureText();
	}
}
