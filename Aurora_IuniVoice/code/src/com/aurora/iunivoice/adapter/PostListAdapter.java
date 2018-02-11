package com.aurora.iunivoice.adapter;

import java.util.ArrayList;
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

import com.aurora.iunivoice.R;
import com.aurora.iunivoice.bean.PostInfo;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class PostListAdapter extends BaseAdapter {

	private ArrayList<PostInfo> postList;
	private LayoutInflater inflater;

	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions optionsImage;

	public PostListAdapter(Context context, ArrayList<PostInfo> postList) {
		this.postList = postList;
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
		return postList.size();
	}

	@Override
	public Object getItem(int position) {
		return postList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return Long.valueOf(postList.get(position).getTid());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_post, null);
			holder = new Holder();
			holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
			holder.tv_author = (TextView) convertView.findViewById(R.id.tv_author);
			holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
			holder.iv_head = (ImageView) convertView.findViewById(R.id.iv_head);
			holder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
			holder.tv_reply = (TextView) convertView.findViewById(R.id.tv_reply);
			holder.tv_praise = (TextView) convertView.findViewById(R.id.tv_praise);
			holder.tv_score = (TextView) convertView.findViewById(R.id.tv_score);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		PostInfo post = postList.get(position);

		holder.tv_title.setText(post.getSubject());
		holder.tv_author.setText(post.getAuthor());
		holder.tv_time.setText(Html.fromHtml(post.getDateline()));
		holder.tv_content.setText(post.getMessage());
		holder.tv_reply.setText(post.getReplies());
		holder.tv_praise.setText(post.getRecommends());
		holder.tv_score.setText(post.getRate());

		imageLoader.displayImage(post.getAvatar(), holder.iv_head, optionsImage, animateFirstListener);

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
		TextView tv_reply;
		TextView tv_praise;
		TextView tv_score;
	}

}
