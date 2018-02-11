package com.aurora.community.adapter;

import java.util.List;

import com.aurora.community.R;
import com.aurora.community.adapter.ImageAlbumAdapter.ViewHolder;
import com.aurora.community.bean.AlbumInfo;
import com.aurora.community.utils.DefaultUtil;
import com.aurora.community.utils.ImageLoaderHelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class TwitterNoteSelectedPhotoAdapter extends BaseAdapter{

	
	private LayoutInflater inflater;

	private List<String> list;

	private Context context;
	
	private ViewHolder holder;

	public TwitterNoteSelectedPhotoAdapter(Context c,List<String> l){
		this.list = l;
		this.context = c;
		this.inflater = LayoutInflater.from(c);		
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return this.list.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.item_twitter_selected_photo, null);
			holder.img = (ImageView)convertView.findViewById(R.id.iv_selected_img);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
			String url  = list.get(position);
		    ImageLoaderHelper.disPlay(url, holder.img, DefaultUtil.getDefaultImageDrawable(context));
		return convertView;
	}
	class ViewHolder{
		public ImageView img;
	}
}
