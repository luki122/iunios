package com.aurora.iunivoice.adapter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aurora.datauiapi.data.bean.HomepageDataInfo;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.activity.fragment.HomePageFragment;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class HomePageListAdapter extends BaseAdapter {
	
	private LayoutInflater inflater;
	private List<HomepageDataInfo> dataInfoList;
	
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions optionsImage;
	
	private Context mContext;
	
	public HomePageListAdapter(Context context, List<HomepageDataInfo> dataInfoList) {
		this.dataInfoList = dataInfoList;
		this.mContext = context;
		inflater = LayoutInflater.from(context);
		
		optionsImage = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.default_user_icon)
				.showImageForEmptyUri(R.drawable.default_user_icon)
				.showImageOnFail(R.drawable.default_user_icon)
				.displayer(new RoundedBitmapDisplayer(100))			// 设置较大值，直接为圆型图片输出
				.cacheInMemory(true).cacheOnDisk(true).build();
	}

	@Override
	public int getCount() {
		return dataInfoList == null ? 0 : dataInfoList.size();
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
		HomepageDataInfo info = dataInfoList.get(position);
		Holder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_homepage, null);
			holder = new Holder();
			holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
			holder.tv_author = (TextView) convertView.findViewById(R.id.tv_author);
			holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
			holder.iv_head = (ImageView) convertView.findViewById(R.id.iv_head);
			holder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		holder.tv_title.setText(info.getSubject());
		holder.tv_author.setText(info.getAuthor());
		holder.tv_time.setText(Html.fromHtml(info.getLastpost()));
		holder.tv_content.setText(info.getPortal_summary());
		if(HomePageFragment.mRecords.contains(info.getTid())){
			holder.tv_title.setTextColor(mContext.getResources().getColor(R.color.main_homepage_title_pressed_color));
			holder.tv_content.setTextColor(mContext.getResources().getColor(R.color.main_homepage_content_pressed_color));
		}else{
			holder.tv_title.setTextColor(mContext.getResources().getColor(R.color.main_homepage_title_color));
			holder.tv_content.setTextColor(mContext.getResources().getColor(R.color.main_homepage_content_color));
		}
		
		imageLoader.displayImage(info.getAvatar(),
				holder.iv_head, optionsImage, animateFirstListener);
		
		return convertView;
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
	
	private static class Holder {
		TextView tv_title;
		TextView tv_author;
		TextView tv_time;
		ImageView iv_head;
		TextView tv_content;
	}

}
