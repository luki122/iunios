package com.aurora.note.adapter;

import java.util.List;

import com.aurora.note.R;
import com.aurora.note.activity.picbrowser.ImageManager2;
import com.aurora.note.activity.picbrowser.PictureInfo;



import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;



public class PicBrowserAdapter extends BaseAdapter{
	private List<PictureInfo> list ;
	private Context context;
	private LayoutInflater inflater;
	private ImageManager2 imageLoader ;
	public PicBrowserAdapter(List<PictureInfo> s , Context context){
		this.list = s ;
		this.context = context;
		inflater = LayoutInflater.from(this.context);
		imageLoader = ImageManager2.from(this.context);
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int arg0) {
		return list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh;
		if(convertView == null){
			convertView = inflater.inflate(R.layout.pic_browser_gridview_item, null);
			vh = new ViewHolder();
			vh.image = (ImageView)convertView.findViewById(R.id.icon);
			DisplayMetrics metrics = context.getResources().getDisplayMetrics();
			LayoutParams params = (LayoutParams) vh.image.getLayoutParams();
			params.width = (int) ((metrics.widthPixels - (5 * metrics.density))/4);
			params.height = params.width;
			vh.image.setLayoutParams(params);
			vh.selectedMark = (CheckBox)convertView.findViewById(R.id.selectMark);
			convertView.setTag(vh);
		}else{
			vh = (ViewHolder)convertView.getTag();
		}
		imageLoader.displayImage(vh.image, list.get(position).getimagePath(), R.drawable.forum_loading_default, 100, 100);
		vh.selectedMark.setChecked(list.get(position).isSelected());
		vh.selectedMark.setTag(position);
		return convertView;
	}
	static class ViewHolder {
		ImageView image;
		CheckBox selectedMark;
	}
}
