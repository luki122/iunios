package com.aurora.community.adapter;

import java.util.ArrayList;
import java.util.List;

import com.aurora.community.R;
import com.aurora.community.utils.DefaultUtil;
import com.aurora.community.utils.ImageLoaderHelper;
import com.aurora.datauiapi.data.bean.NewsCategoryData;
import com.aurora.datauiapi.data.bean.NewsCategoryInfo;
import com.tencent.mm.sdk.platformtools.Log;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NewsCategoryAdapter extends BaseAdapter{

	private Context context;
	
	private ArrayList<NewsCategoryData> list;
	
	private LayoutInflater inflater;
	
	public NewsCategoryAdapter(Context c,ArrayList<NewsCategoryData> l){
		this.context = c;
		this.list  = l;
		this.inflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return this.list.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		NewsCategoryData info = (NewsCategoryData)getItem(position);
		ViewHolder holder = null;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = inflater.inflate(
					R.layout.news_category_secondary_list_adapter_item, parent,
					false);
			holder.im_cover =(ImageView)convertView.findViewById(R.id.lv_secondary_cover); 
			holder.tv_title = (TextView)convertView.findViewById(R.id.tv_secondary_title); 
			holder.tv_desc = (TextView)convertView.findViewById(R.id.tv_secondary_desc); 
			holder.tv_comment  = (TextView)convertView.findViewById(R.id.tv_secondary_comment_num); 
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		ImageLoaderHelper.disPlay(info.getCover(), holder.im_cover, DefaultUtil.getDefaultImageDrawable(context));
		holder.tv_title.setText(info.getTname());
		holder.tv_desc.setText(info.getDesc());
		holder.tv_comment.setText(context.getString(R.string.total_comment,info.getPost_count()));
		return convertView;
	}

	
	static final class ViewHolder{
		ImageView im_cover;
		TextView tv_title;
		TextView tv_desc;
		TextView tv_comment;
	}
}
