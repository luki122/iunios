package com.aurora.iunivoice.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aurora.datauiapi.data.bean.ForumInfo;
import com.aurora.iunivoice.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class ForumListAdapter extends BaseAdapter {

	private ArrayList<ForumInfo> forumList;
	private LayoutInflater inflater;

	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions optionsImage;

	public ForumListAdapter(Context context, ArrayList<ForumInfo> forumList) {
		this.forumList = forumList;
		inflater = LayoutInflater.from(context);

		optionsImage = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.icon_forum_defalut)
				.showImageForEmptyUri(R.drawable.icon_forum_defalut)
				.showImageOnFail(R.drawable.icon_forum_defalut)
				.cacheInMemory(true).cacheOnDisk(true).build();
	}

	@Override
	public int getCount() {
		return forumList.size();
	}

	@Override
	public Object getItem(int position) {
		return forumList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return Long.valueOf(forumList.get(position).getFid());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_forum, null);
			holder = new Holder();
			holder.iv_head = (ImageView) convertView.findViewById(R.id.iv_head);
			holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
			holder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		ForumInfo forum = forumList.get(position);

		holder.tv_title.setText(forum.getName());
		holder.tv_content.setText(forum.getDescription());

		imageLoader.displayImage(forum.getIcon(), holder.iv_head, optionsImage, animateFirstListener);

		return convertView;
	}

	private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
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
		ImageView iv_head;
		TextView tv_title;
		TextView tv_content;
	}

}
