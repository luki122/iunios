package com.aurora.market.adapter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.aurora.datauiapi.data.bean.appListtem;
import com.aurora.market.MarketMainActivity;
import com.aurora.market.R;
import com.aurora.market.activity.module.AppListActivity;
import com.aurora.market.activity.module.AppRankingActivity;
import com.aurora.market.model.DownloadData;
import com.aurora.market.util.ProgressBtnUtil;
import com.aurora.market.util.SystemUtils;
import com.aurora.market.widget.ProgressBtn;
import com.aurora.market.widget.RoundProgressView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class AppAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private List<appListtem> applist;
	private List<DownloadData> download;
	private ProgressBtnUtil progressBtnUtil;

	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	// 图片加载工具
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions optionsImage;
	private boolean loadImage = true;
	private Context m_context;
	public AppAdapter(Context context, List<appListtem> app_list,
			List<DownloadData> down_data) {
		inflater = LayoutInflater.from(context);
		
		this.applist = app_list;
		this.download = down_data;
		this.m_context = context;
		optionsImage = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.page_appicon_big)
				.showImageForEmptyUri(R.drawable.page_appicon_big)
				.showImageOnFail(R.drawable.page_appicon_big)
				.displayer(new RoundedBitmapDisplayer(context.getResources().getDimensionPixelOffset(R.dimen.app_icon_displayer)))
				.cacheInMemory(true).cacheOnDisk(true).build();
		
		progressBtnUtil = new ProgressBtnUtil();
	}

	@Override
	public int getCount() {
		int size = (download.size()<applist.size())?download.size():applist.size();
		return size;
	}

	@Override
	public DownloadData getItem(int position) {
		return download.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if (applist == null) {
			return null;
		}
		appListtem listitem = null;
		
		if(applist.size() > 0)
		  listitem= applist.get(position);

		if (listitem == null) {
			return null;
		}

		DownloadData data = download.get(position);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_app, null);
			holder = new Holder();
			holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
	
			holder.tv_appname = (TextView) convertView
					.findViewById(R.id.tv_appname);
			holder.rb_score = (RatingBar) convertView
					.findViewById(R.id.rb_score);
			holder.tv_download_count = (TextView) convertView
					.findViewById(R.id.tv_download_count);

			holder.progressBtn = (ProgressBtn) convertView
					.findViewById(R.id.progressBtn);
			convertView.setTag(holder);
			
			holder.progressBtn.setTag(data.getApkId());
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		changeViewData(holder, listitem, data);
		
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

	static final class Holder {
		 ImageView iv_icon;
	
		 TextView tv_appname;
		 RatingBar rb_score;
		 TextView tv_download_count;
		 RoundProgressView round_progress_view;
		 ProgressBtn progressBtn;
	}

	private void changeViewData(Holder holder, appListtem listitem, DownloadData data) {
		// 开始头像图片异步加载
	
		if (SystemUtils.isLoadingImage(inflater.getContext())) {
			if(loadImage)
				imageLoader.displayImage(listitem.getIcons().getPx256(),
					holder.iv_icon, optionsImage, animateFirstListener);
			else
				imageLoader.displayImage1(listitem.getIcons().getPx256(),
						new ImageViewAware(holder.iv_icon), optionsImage, animateFirstListener,null,m_context,2);
				/*imageLoader.displayImage(listitem.getIcons().getPx256(),
						holder.iv_icon, optionsImage, animateFirstListener);*/
		} else {
			holder.iv_icon.setImageResource(R.drawable.page_appicon_big);
		}
		
		
		final ImageView view = holder.iv_icon;
		view.setDrawingCacheEnabled(true);
		holder.progressBtn.setOnButtonClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (inflater.getContext() instanceof MarketMainActivity) {
					((MarketMainActivity) inflater.getContext())
							.setAnimal1(view);
				}
				else if(inflater.getContext() instanceof AppRankingActivity) {
					((AppRankingActivity) inflater.getContext())
					.setAnimal1(view);
		}
				else if(inflater.getContext() instanceof AppListActivity) {
					((AppListActivity) inflater.getContext())
					.setAnimal1(view);
		}
			}
		});
		//holder.iv_icon.setTag(listitem.getIcons().getPx256());

		holder.tv_appname.setText(listitem.getTitle());
		holder.tv_download_count.setText(listitem.getDownloadCountStr()
				+ inflater.getContext()
						.getString(R.string.app_adapter_down_end) + "    " + listitem.getAppSizeStr());
		
		holder.rb_score.setRating(listitem.getLikesRate() / 20);
		
		progressBtnUtil.updateProgressBtn(holder.progressBtn, data);
	}
	
	public void updateView(ListView listView) {
		if (listView == null) {
			return;
		}
		
		int headerCount = listView.getHeaderViewsCount();
		int firstVisiblePosition = listView.getFirstVisiblePosition();
		int offset = headerCount - firstVisiblePosition;
		boolean containerHeader = false;
		if (headerCount > 0) {
			if (firstVisiblePosition < headerCount) {
				containerHeader = true;
			}
		}
		int count = listView.getChildCount();
		
		for (int i = 0; i < count; i++) {
			int position = 0;
			if (containerHeader) {
				if (i < offset) {
					continue;
				}
				position = i - offset;
			} else {
				position = i + firstVisiblePosition - headerCount;
			}
			
			if (position >= download.size()) {
				continue;
			}
			DownloadData data = download.get(position);
			appListtem listitem = applist.get(position);
			
			View view = listView.getChildAt(i);
			Holder holder = (Holder)view.getTag();
			
			if (holder == null) {
				continue;
			}
		
			changeViewData(holder, listitem, data);
		}
	}

	public void setLoadImage(boolean loadImage) {
		this.loadImage = loadImage;
	}
	
	public void clearProgressBtnTag(ListView listView) {
		if (listView == null) {
			return;
		}
		
		int headerCount = listView.getHeaderViewsCount();
		int firstVisiblePosition = listView.getFirstVisiblePosition();
		int offset = headerCount - firstVisiblePosition;
		boolean containerHeader = false;
		if (headerCount > 0) {
			if (firstVisiblePosition < headerCount) {
				containerHeader = true;
			}
		}
		int count = listView.getChildCount();
		
		for (int i = 0; i < count; i++) {
			int position = 0;
			if (containerHeader) {
				if (i < offset) {
					continue;
				}
				position = i - offset;
			} else {
				position = i + firstVisiblePosition - headerCount;
			}
			
			if (position >= download.size()) {
				continue;
			}
			
			DownloadData data = download.get(position);
			View view = listView.getChildAt(i);
			Holder holder = (Holder)view.getTag();
			
			if (holder == null) {
				continue;
			}
		
			holder.progressBtn.setTag(0);
		}
	}

}
