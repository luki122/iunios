package com.aurora.iunivoice.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.aurora.iunivoice.R;
import com.aurora.iunivoice.activity.picBrowser.RotateImageViewAware;
import com.aurora.iunivoice.bean.AlbumInfo;
import com.aurora.iunivoice.utils.DefaultUtil;
import com.aurora.iunivoice.utils.ImageLoaderHelper;
import com.aurora.iunivoice.utils.ThumbnailsUtil;

public class ImageAlbumAdapter extends BaseAdapter {
	
	private LayoutInflater inflater;

	private List<AlbumInfo> list;

	private Context context;

	private ViewHolder holder;
	
	private ListView mListView;
	
	public ImageAlbumAdapter(List<AlbumInfo> l,Context c,ListView lv){
		this.list = l;
		this.context = c;
		this.inflater = LayoutInflater.from(c);
		this.mListView = lv;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return this.list.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return this.list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
	
		AlbumInfo info = list.get(position);
		if (convertView == null) {
			
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.item_album_adapter, null);
			holder.cover = (ImageView)convertView.findViewById(R.id.iv_cover);
			holder.albumName = (TextView)convertView.findViewById(R.id.tv_album_name);
			holder.albumSize = (TextView)convertView.findViewById(R.id.tv_album_size);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
	
		ImageLoaderHelper.disPlay(ThumbnailsUtil.MapgetHashValue(info.getImage_id(),info.getPath_file()), 
				new RotateImageViewAware(holder.cover,info.getPath_absolute()), DefaultUtil.getDefaultImageDrawable(context));
		holder.albumName.setText(info.getName_album());
		holder.albumSize.setText(info.getList().size()+context.getString(R.string.image_album_item_tv));
		if(info.getSelected())
			holder.albumName.setTextColor(context.getResources().getColor(R.color.common_deep_blue_color));
		else
			holder.albumName.setTextColor(context.getResources().getColor(R.color.gloab_title_color));
		return convertView;
	}

	public void refreshView(int index){
          for(int i = 0;i<list.size();i++){
        	  if(index == i){
        		  this.list.get(index).setSelected(true);
        	  }else
        		  this.list.get(i).setSelected(false);
          }
          notifyDataSetChanged();
	}
	
	
	class ViewHolder{
		public ImageView cover;
		public TextView albumName;
		public  TextView albumSize;
		
	}
}
