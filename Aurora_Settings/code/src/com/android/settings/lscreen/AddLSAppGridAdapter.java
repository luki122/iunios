package com.android.settings.lscreen;

import com.android.settings.R;
import com.android.settings.lscreen.ls.LSCustomPreference;
import com.android.settings.lscreen.ls.LSOperator;
import com.secure.imageloader.ImageCallback;
import com.secure.imageloader.ImageLoader;

import android.app.Activity;
import android.content.ClipData.Item;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

public class AddLSAppGridAdapter extends ArrayAdapter<BaseData> implements
		OnClickListener {
	private DataArrayList<String> choiceAppList;// 存放选中的packageName
	private ViewGroup parent;
	private Activity activity;

	public AddLSAppGridAdapter(Activity activity,
			LSSameFirstCharAppData sameFirstCharAppData) {
		super(activity, 0, sameFirstCharAppData.getAppList().getDataList());

		this.activity = activity;
		choiceAppList = ((LSAppActivity) activity).getChoiceAppList();
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
		} else {
			holder = (LSAppGridItemCache)convertView.getTag();
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
		
		
		
		if(item.getPackageName().equals(LSAppActivity.mTransferPkgName))
		{
			holder.getAppIcon().setBackgroundResource(R.drawable.cancel_def_soft);
		}else if(item.getPackageName().equals(LSOperator.IUNI_EMAIL) && !item.getPackageName().equals(LSAppActivity.mTransferPkgName))
		{
			holder.getAppIcon().setBackground(Utils.compositeDrawable(activity, LSOperator.IUNI_EMAIL));
		}else if(item.getPackageName().equals(LSOperator.IUNI_MMS) && !item.getPackageName().equals(LSAppActivity.mTransferPkgName))
		{
			holder.getAppIcon().setBackground(Utils.compositeDrawable(activity, LSOperator.IUNI_MMS));
		}else
		{
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
		}
		return convertView;
	}

	/**
	 * 判断当前packageName对应的软件需不需要清理
	 * 
	 * @param packageName
	 * @return
	 */
	private boolean isChoiced(String packageName) {
		if (choiceAppList == null || packageName == null) {
			return false;
		}

		for (int i = 0; i < choiceAppList.size(); i++) {
			String tmpStr = choiceAppList.get(i);
			if (packageName.equals(tmpStr)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		Object tagObject = v.getTag();
		if (tagObject != null) {
			int position = Integer.parseInt(tagObject.toString());
			dealItemClick(position);
		}
	}

	/**
	 * 处理onItemClick事件
	 * 
	 * @param position
	 */
	public void dealItemClick(int position) {
		Log.d("gd", " position=" + position + " count="+ getCount());
		if (getCount() <= position) {
			return;
		}
		AppInfo item = (AppInfo) getItem(position);
		if (item == null) {
			return;
		}
		// delete		
		if (LSAppActivity.mTransferPkgName.equals(item
				.getPackageName())) {
//			((LScreenActivity) activity).delOrUpdateLScreenApp(item
//					.getPackageName());
			((LSAppActivity) activity).finish();
			return;
		}

		// limit
		if ((LSCustomPreference.getLSAppList().size() + 1) > 4) {
			return;
		}

		// add
		choiceAppList.add(item.getPackageName());
		((LSAppActivity) activity).saveLScreenApp();

	}
}