package com.aurora.market.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aurora.datauiapi.data.bean.categoryListtem;
import com.aurora.datauiapi.data.bean.subcategoryListtem;
import com.aurora.market.R;
import com.aurora.market.util.SystemUtils;
import com.aurora.market.widget.GroupWordTextView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class CategoryAdapter extends BaseAdapter {
	
	private CustomOnItemClickListener customOnItemClickListener;
	
	private LayoutInflater inflater;
	private List<categoryListtem> cate_list;
	private int NUM = 3;
	
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	// 图片加载工具
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions optionsImage;
	public CategoryAdapter(Context context, List<categoryListtem> cate_list) {
		inflater = LayoutInflater.from(context);
	
		this.cate_list = cate_list;
		
		optionsImage = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.drawable.page_appicon_mostsmall)
			.showImageForEmptyUri(R.drawable.page_appicon_mostsmall)
			.showImageOnFail(R.drawable.page_appicon_mostsmall)
			.cacheInMemory(true).cacheOnDisc(true).build();
	}

	@Override
	public int getCount() {
		if (cate_list == null) {
			return 0;
		}
		int size = cate_list.size();
		return size % NUM == 0 ? size / NUM : (size / NUM) + 1;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_category, null);
			holder = new Holder();
			holder.item_left = (RelativeLayout) convertView.findViewById(R.id.item_left);
			holder.iv_category_left = (ImageView) convertView.findViewById(R.id.iv_category_left);
			holder.tv_category_name_left = (TextView) convertView.findViewById(R.id.tv_category_name_left);
			holder.tv_info_left = (GroupWordTextView) convertView.findViewById(R.id.tv_info_left);
			holder.tv_info_left.setVisibility(View.GONE);
			holder.item_right = (RelativeLayout) convertView.findViewById(R.id.item_right);
			holder.iv_category_right = (ImageView) convertView.findViewById(R.id.iv_category_right);
			holder.tv_category_name_right = (TextView) convertView.findViewById(R.id.tv_category_name_right);
			holder.tv_info_right = (GroupWordTextView) convertView.findViewById(R.id.tv_info_right);
			
			holder.item_middle = (RelativeLayout) convertView.findViewById(R.id.item_middle);
			holder.iv_category_middle = (ImageView) convertView.findViewById(R.id.iv_category_middle);
			holder.tv_category_name_middle = (TextView) convertView.findViewById(R.id.tv_category_name_middle);
			
			Resources res = convertView.getContext().getResources();
			float width = res.getDisplayMetrics().widthPixels / 2 
					- res.getDimension(R.dimen.category_item_padding_left_right) * 2;
			holder.tv_info_left.setWidth(width);
			holder.tv_info_left.setmHorizontalSpace(convertView.getContext()
					.getResources().getDimension(R.dimen.category_item_info_margin));
			holder.tv_info_right.setWidth(width);
			holder.tv_info_right.setmHorizontalSpace(convertView.getContext()
					.getResources().getDimension(R.dimen.category_item_info_margin));
			
			holder.item_left.setOnClickListener(onClickListener);
			holder.item_middle.setOnClickListener(onClickListener);
			holder.item_right.setOnClickListener(onClickListener);
			
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		int leftPosition = 0;
		int rightPosition = -1;
		int middlePosition = -1;
		
		boolean hasRight = false;
		boolean hasMiddle = false;
		leftPosition = position * NUM;
		
		if(leftPosition + 1 <= cate_list.size() - 1){
		    hasMiddle = true;
		    middlePosition = leftPosition + 1;
		}
		
		if (leftPosition + 2 <= cate_list.size() - 1) {
			hasRight = true;
			rightPosition = leftPosition + 2;
		}
		
		holder.item_left.setTag(leftPosition);
		holder.item_middle.setTag(middlePosition);
		holder.item_right.setTag(rightPosition);
		
		// left item
		holder.tv_category_name_left.setText(cate_list.get(leftPosition).getName());
		showSubCategoryText(holder.tv_info_left, leftPosition);
		// 开始头像图片异步加载
		if (SystemUtils.isLoadingImage(inflater.getContext())) {
			imageLoader
					.displayImage(cate_list.get(leftPosition).getIcon(),
							holder.iv_category_left, optionsImage,
							animateFirstListener);
		} else {
			holder.iv_category_left
					.setImageResource(R.drawable.page_appicon_mostsmall);
		}
		
		// middle item
		if(hasMiddle){
		    holder.iv_category_middle.setVisibility(View.VISIBLE);
            holder.tv_category_name_middle.setVisibility(View.VISIBLE);
		    
            holder.tv_category_name_middle.setText(cate_list.get(middlePosition).getName());
            if (SystemUtils.isLoadingImage(inflater.getContext())) {
                imageLoader.displayImage(
                        cate_list.get(middlePosition).getIcon(),
                        holder.iv_category_middle, optionsImage,
                        animateFirstListener);
            } else {
                holder.iv_category_middle
                        .setImageResource(R.drawable.page_appicon_mostsmall);
            }
		}else{
		    holder.iv_category_middle.setVisibility(View.GONE);
            holder.tv_category_name_middle.setVisibility(View.GONE);
            
		}
		
		// right item
		if (hasRight) {
			holder.iv_category_right.setVisibility(View.VISIBLE);
			holder.tv_category_name_right.setVisibility(View.VISIBLE);
			holder.tv_info_right.setVisibility(View.GONE);
			
			holder.tv_category_name_right.setText(cate_list.get(rightPosition).getName());
			showSubCategoryText(holder.tv_info_right, rightPosition);
			// 开始头像图片异步加载
			if (SystemUtils.isLoadingImage(inflater.getContext())) {
				imageLoader.displayImage(
						cate_list.get(rightPosition).getIcon(),
						holder.iv_category_right, optionsImage,
						animateFirstListener);
			} else {
				holder.iv_category_right
						.setImageResource(R.drawable.page_appicon_mostsmall);
			}
		} else {
			holder.iv_category_right.setVisibility(View.GONE);
			holder.tv_category_name_right.setVisibility(View.GONE);
			holder.tv_info_right.setVisibility(View.GONE);
		}
		
		return convertView;
	}
	
	private void showSubCategoryText(GroupWordTextView tv, int position) {
		if (cate_list.get(position).getSubCategories() != null && 
				cate_list.get(position).getSubCategories().size() > 0) {
			List<String> subName = new ArrayList<String>();
			for (subcategoryListtem item : cate_list.get(position).getSubCategories()) {
				subName.add(item.getName());
			}
			tv.setGroupText(subName.toArray(new String[]{}));
		}
	}
	
	private static class AnimateFirstDisplayListener extends
			SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections
				.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}
	
	public CustomOnItemClickListener getCustomOnItemClickListener() {
		return customOnItemClickListener;
	}

	public void setCustomOnItemClickListener(
			CustomOnItemClickListener customOnItemClickListener) {
		this.customOnItemClickListener = customOnItemClickListener;
	}

	private OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			int position = (Integer) view.getTag();
			if (position != -1 && customOnItemClickListener != null) {
				customOnItemClickListener.onItemClick(view, position);
			}
		}
	};
	
	static class Holder {
		RelativeLayout item_left;
		ImageView iv_category_left;
		TextView tv_category_name_left;
		GroupWordTextView tv_info_left;
		RelativeLayout item_right;
		ImageView iv_category_right;
		TextView tv_category_name_right;
		GroupWordTextView tv_info_right;
		RelativeLayout item_middle;
	    ImageView iv_category_middle;
	    TextView tv_category_name_middle;
	}
	
	public interface CustomOnItemClickListener {
		public void onItemClick(View view, int position);
	}

}
