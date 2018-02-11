package com.aurora.community.adapter;

import java.util.List;

import com.aurora.datauiapi.data.bean.CollectionDataInfo;
import com.aurora.datauiapi.data.bean.CollectionOfUserCenter;
import com.aurora.datauiapi.data.bean.ImageInfo;
import com.aurora.datauiapi.data.bean.PublishOfUserCenter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aurora.community.R;
import com.aurora.community.adapter.PublishOfUserCenterAdapter.Holder;
import com.aurora.community.utils.DefaultUtil;
import com.aurora.community.utils.ImageLoaderHelper;
import com.aurora.community.utils.TimeUtils;
public class CollectionOfUserCenterAdapter extends BaseAdapter {

	private List<CollectionDataInfo> datas;
	private LayoutInflater mInflater;
	private Context mContext;
	
	private LinearLayout.LayoutParams justOneImageParamas,moreImageParamas,hideImageParamas,showImageParamas,showImageLeftParamas;
	
	public CollectionOfUserCenterAdapter(List<CollectionDataInfo> datas,
			Context mContext) {
		super();
		this.datas = datas;
		this.mContext = mContext;
		this.mInflater = LayoutInflater.from(mContext);
		
		justOneImageParamas = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)mContext.getResources().getDimension(R.dimen.usercenter_item_one_image_height));
		moreImageParamas = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.usercenter_item_one_more_image_height));
		hideImageParamas = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
		hideImageParamas.weight = 0;
		showImageParamas = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
		showImageParamas.weight = 1;
		showImageParamas.leftMargin = (int)mContext.getResources().getDimension(R.dimen.usercenter_item_one_more_image_margin);
		showImageLeftParamas = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
		showImageLeftParamas.weight = 1;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return datas.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

class Holder{
		
		LinearLayout ll_images_container;
		TextView publish_title,publish_time,comments_count,collections_count;
		ImageView image_left,image_center,image_right;
		
	}
	
	private void setHolder(View convertView,Holder holder){
		holder.collections_count = (TextView)convertView.findViewById(R.id.collections_count);
		holder.publish_time = (TextView)convertView.findViewById(R.id.publish_time);
		holder.comments_count = (TextView)convertView.findViewById(R.id.comments_count);
		holder.publish_title = (TextView)convertView.findViewById(R.id.publish_title);
		
		holder.ll_images_container = (LinearLayout)convertView.findViewById(R.id.ll_images_container);
		holder.image_left = (ImageView)convertView.findViewById(R.id.image_left);
		holder.image_center = (ImageView)convertView.findViewById(R.id.image_center);
		holder.image_right = (ImageView)convertView.findViewById(R.id.image_right);
		convertView.setTag(holder);
	}
	
	private void setData(int position,Holder holder){
		CollectionDataInfo data = datas.get(position);
		setShowText(data, holder);
		setShowImage(data, holder);
	}
	
	private void setShowText(CollectionDataInfo data,Holder holder){
		holder.collections_count.setText(data.getFavorites());
		holder.comments_count.setText(data.getReplies());
		holder.publish_time.setText(data.getCreated_format());
		if(TextUtils.isEmpty(data.getBody()))
		{
			holder.publish_title.setVisibility(View.GONE);
		}else{
			holder.publish_title.setVisibility(View.VISIBLE);
		    holder.publish_title.setText(data.getBody());
		}
	}
	
	private void setShowImage(CollectionDataInfo data,Holder holder){
		List<ImageInfo> imageUrls = data.getImages();
		if(imageUrls == null)
		{
			holder.ll_images_container.setVisibility(View.GONE);
			return;
		}
		switch (imageUrls.size()) {
		case 0:
			holder.ll_images_container.setVisibility(View.GONE);
			break;
		case 1:
		case 2:
			holder.ll_images_container.setVisibility(View.VISIBLE);
			holder.ll_images_container.setLayoutParams(justOneImageParamas);
			holder.image_left.setLayoutParams(showImageLeftParamas);
			holder.image_center.setLayoutParams(hideImageParamas);
			holder.image_right.setLayoutParams(hideImageParamas);
			ImageLoaderHelper.disPlay(imageUrls.get(0).getThumbPath(), holder.image_left, DefaultUtil.getDefaultImageDrawable(mContext));
			break;
		default://3张及3张以上图片
			holder.ll_images_container.setVisibility(View.VISIBLE);
			holder.ll_images_container.setLayoutParams(moreImageParamas);
			holder.image_left.setLayoutParams(showImageLeftParamas);
			holder.image_center.setLayoutParams(showImageParamas);
			holder.image_right.setLayoutParams(showImageParamas);
			ImageLoaderHelper.disPlay(imageUrls.get(0).getThumbPath(3), holder.image_left, DefaultUtil.getDefaultImageDrawable(mContext));
			ImageLoaderHelper.disPlay(imageUrls.get(1).getThumbPath(3), holder.image_center, DefaultUtil.getDefaultImageDrawable(mContext));
			ImageLoaderHelper.disPlay(imageUrls.get(2).getThumbPath(3), holder.image_right, DefaultUtil.getDefaultImageDrawable(mContext));
			break;
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if(convertView == null)
		{
			holder = new Holder();
			convertView = mInflater.inflate(R.layout.item_publish_of_usercenter, null);
			setHolder(convertView, holder);
		}else{
			holder = (Holder) convertView.getTag();
		}
		setData(position, holder);
		return convertView;
	}

}
