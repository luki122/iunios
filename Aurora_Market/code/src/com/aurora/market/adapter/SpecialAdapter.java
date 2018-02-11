package com.aurora.market.adapter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aurora.datauiapi.data.bean.appListtem;
import com.aurora.datauiapi.data.bean.specials;
import com.aurora.market.R;
import com.aurora.market.activity.module.AppListActivity;
import com.aurora.market.activity.module.MarketDetailActivity;
import com.aurora.market.activity.module.SpecialActivity;
import com.aurora.market.model.DownloadData;
import com.aurora.market.util.AsyncImageLoader;
import com.aurora.market.util.ProgressBtnUtil;
import com.aurora.market.util.SystemUtils;
import com.aurora.market.widget.ProgressBtn;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class SpecialAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private List<specials> specialList;

	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	// 图片加载工具
	private ImageLoader imageLoader = ImageLoader.getInstance();

	private ProgressBtnUtil progressBtnUtil;

	private DisplayImageOptions optionsImageBanner;
	private DisplayImageOptions optionsImageApp;
	private Context m_context;
	private AsyncImageLoader asyncImageLoader;

	public SpecialAdapter(Context context, List<specials> specialList,
			ListView listview) {
		inflater = LayoutInflater.from(context);
		this.specialList = specialList;
		this.m_context = context;
		asyncImageLoader = new AsyncImageLoader(m_context);
		optionsImageBanner = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.special_loading_default)
				.showImageForEmptyUri(R.drawable.special_loading_default)
				.showImageOnFail(R.drawable.special_loading_default)
				.cacheInMemory(true).cacheOnDisk(true).build();

		optionsImageApp = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.page_appicon_big)
				.showImageForEmptyUri(R.drawable.page_appicon_big)
				.showImageOnFail(R.drawable.page_appicon_big)
				.displayer(new RoundedBitmapDisplayer(10)).cacheInMemory(true)
				.cacheOnDisk(true).build();

		progressBtnUtil = new ProgressBtnUtil();
	}

	@Override
	public int getCount() {
		return specialList == null ? 0 : specialList.size();
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
		final specials specialListItem = specialList.get(position);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_special, null);

			holder = new Holder();
			holder.iv_special = (ImageView) convertView
					.findViewById(R.id.iv_special);
			holder.tv_app_count = (TextView) convertView
					.findViewById(R.id.tv_app_count);
			holder.rl_tab = (RelativeLayout) convertView
					.findViewById(R.id.rl_tab);
			
			holder.rl_appitem1 = (RelativeLayout) convertView
					.findViewById(R.id.rl_appitem1);
			holder.iv_icon1 = (ImageView) convertView
					.findViewById(R.id.iv_icon1);
			holder.tv_appname1 = (TextView) convertView
					.findViewById(R.id.tv_appname1);
			holder.rb_score1 = (RatingBar) convertView
					.findViewById(R.id.rb_score1);
			holder.tv_download_count1 = (TextView) convertView
					.findViewById(R.id.tv_download_count1);
			holder.progressBtn1 = (ProgressBtn) convertView
					.findViewById(R.id.progressBtn1);
			holder.rl_appitem2 = (RelativeLayout) convertView
					.findViewById(R.id.rl_appitem2);
			holder.iv_icon2 = (ImageView) convertView
					.findViewById(R.id.iv_icon2);
			holder.tv_appname2 = (TextView) convertView
					.findViewById(R.id.tv_appname2);
			holder.rb_score2 = (RatingBar) convertView
					.findViewById(R.id.rb_score2);
			holder.tv_download_count2 = (TextView) convertView
					.findViewById(R.id.tv_download_count2);
			holder.progressBtn2 = (ProgressBtn) convertView
					.findViewById(R.id.progressBtn2);
			holder.rl_appitem3 = (RelativeLayout) convertView
					.findViewById(R.id.rl_appitem3);
			holder.iv_icon3 = (ImageView) convertView
					.findViewById(R.id.iv_icon3);
			holder.tv_appname3 = (TextView) convertView
					.findViewById(R.id.tv_appname3);
			holder.rb_score3 = (RatingBar) convertView
					.findViewById(R.id.rb_score3);
			holder.tv_download_count3 = (TextView) convertView
					.findViewById(R.id.tv_download_count3);
			holder.progressBtn3 = (ProgressBtn) convertView
					.findViewById(R.id.progressBtn3);
			holder.divider_height = convertView
					.findViewById(R.id.divider_height);
			
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		if (SystemUtils.isLoadingImage(inflater.getContext())) {/*
			 holder.iv_special.setDrawingCacheEnabled(true); 
			holder.iv_special.setTag(specialListItem.getSpecial().getBannerURL());
			final Bitmap drawable = asyncImageLoader.loadDrawable(
					specialListItem.getSpecial().getBannerURL(), new ImageCallback() {
						@Override
						public void imageLoaded(final Bitmap imageDrawable,
								String imageUrl) {
							ImageView imageViewByTag = (ImageView) m_listview
									.findViewWithTag(imageUrl);
							if ( (null!=imageViewByTag)  &&  (null!=imageDrawable) ) {
								imageViewByTag.setImageBitmap(imageDrawable);

								FadeInBitmapDisplayer.animate(imageViewByTag,
										500);
							} else {
								// load image failed from Internet

							}

						}
					});
			if (drawable == null) {
				holder.iv_special.setImageResource(R.drawable.special_loading_default);
			} else {
				holder.iv_special.setImageBitmap(drawable);
			}

		*/
			
			imageLoader.displayImage(specialListItem.getSpecial().getBannerURL(), holder.iv_special,
					optionsImageBanner, animateFirstListener);
		} else {
			holder.iv_special
					.setImageResource(R.drawable.banner_loading_default);
		}
		
		holder.tv_app_count.setText(m_context.getString(R.string.special_app_count_text, 
				specialListItem.getSpecial().getAppCount()));
		
		OnClickListener showAllClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(m_context, AppListActivity.class);
				i.putExtra(AppListActivity.OPEN_TYPE,
						AppListActivity.TYPE_SPECIAL);
				i.putExtra(AppListActivity.SPECIAL_NAME, specialListItem.getSpecial().getName());
				i.putExtra(AppListActivity.SPECIAL_ID, specialListItem.getSpecial().getId());
				m_context.startActivity(i);
			}
		};
		holder.iv_special.setOnClickListener(showAllClickListener);
		holder.rl_tab.setOnClickListener(showAllClickListener);

		for (int i = 0; i < specialListItem.getApps().size(); i++) {
			appListtem item = specialListItem.getApps().get(i);
			changeViewData(holder, i, item, initDownLoadData(item));
		}

		if (position == getCount() - 1) {
			holder.divider_height.setVisibility(View.GONE);
		} else {
			holder.divider_height.setVisibility(View.VISIBLE);
		}

		return convertView;
	}

	private DownloadData initDownLoadData(appListtem item) {
		DownloadData tmp_data = new DownloadData();
		tmp_data.setApkId(item.getId());
		tmp_data.setApkDownloadPath(item.getDownloadURL());
		tmp_data.setApkLogoPath(item.getIcons().getPx256());
		tmp_data.setApkName(item.getTitle());
		tmp_data.setPackageName(item.getPackageName());
		tmp_data.setVersionCode(item.getVersionCode());
		tmp_data.setVersionName(item.getVersionName());
		return tmp_data;
	}

	private void changeViewData(Holder holder, int position,
			appListtem listitem, final DownloadData data) {
		RelativeLayout rl_appitem = null;
		ImageView iv_icon = null;
		ProgressBtn progressBtn = null;
		TextView tv_appname = null;
		TextView tv_download_count = null;
		RatingBar rb_score = null;
		switch (position) {
		case 0:
			rl_appitem = holder.rl_appitem1;
			iv_icon = holder.iv_icon1;
			progressBtn = holder.progressBtn1;
			tv_appname = holder.tv_appname1;
			tv_download_count = holder.tv_download_count1;
			rb_score = holder.rb_score1;
			break;
		case 1:
			rl_appitem = holder.rl_appitem2;
			iv_icon = holder.iv_icon2;
			progressBtn = holder.progressBtn2;
			tv_appname = holder.tv_appname2;
			tv_download_count = holder.tv_download_count2;
			rb_score = holder.rb_score2;
			break;
		case 2:
			rl_appitem = holder.rl_appitem3;
			iv_icon = holder.iv_icon3;
			progressBtn = holder.progressBtn3;
			tv_appname = holder.tv_appname3;
			tv_download_count = holder.tv_download_count3;
			rb_score = holder.rb_score3;
			break;
		}
		
		rl_appitem.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(m_context,
						MarketDetailActivity.class);
				intent.putExtra("downloaddata", data);
				m_context.startActivity(intent);
			}
		});

		// 开始头像图片异步加载
		if (SystemUtils.isLoadingImage(inflater.getContext())) {
			imageLoader.displayImage(listitem.getIcons().getPx256(), iv_icon,
					optionsImageApp, animateFirstListener);
		} else {
			iv_icon.setImageResource(R.drawable.page_appicon_big);
		}

		final ImageView view = iv_icon;
		view.setDrawingCacheEnabled(true);
		progressBtn.setOnButtonClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (m_context instanceof SpecialActivity) {
					((SpecialActivity) m_context).setAnimal1(view);
				}
			}
		});
		// holder.iv_icon.setTag(listitem.getIcons().getPx256());

		tv_appname.setText(listitem.getTitle());
		tv_download_count.setText(listitem.getDownloadCountStr()
				+ inflater.getContext()
						.getString(R.string.app_adapter_down_end) + "    "
				+ listitem.getAppSizeStr());

		rb_score.setRating(listitem.getLikesRate() / 20);

		progressBtnUtil.updateProgressBtn(progressBtn, data);
	}

	private static class AnimateFirstDisplayListener extends
			SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections
				.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view,Bitmap loadedImage) {
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
		ImageView iv_special;
		RelativeLayout rl_tab;
		TextView tv_app_count;

		RelativeLayout rl_appitem1;
		ImageView iv_icon1;
		TextView tv_appname1;
		RatingBar rb_score1;
		TextView tv_download_count1;
		ProgressBtn progressBtn1;

		RelativeLayout rl_appitem2;
		ImageView iv_icon2;
		TextView tv_appname2;
		RatingBar rb_score2;
		TextView tv_download_count2;
		ProgressBtn progressBtn2;

		RelativeLayout rl_appitem3;
		ImageView iv_icon3;
		TextView tv_appname3;
		RatingBar rb_score3;
		TextView tv_download_count3;
		ProgressBtn progressBtn3;

		View divider_height;
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

			if (position >= specialList.size()) {
				continue;
			}

			View view = listView.getChildAt(i);
			Holder holder = (Holder) view.getTag();

			if (holder == null) {
				continue;
			}

			for (int j = 0; j < specialList.get(position).getApps().size(); j++) {
				appListtem item = specialList.get(position).getApps().get(j);
				changeViewData(holder, j, item, initDownLoadData(item));
			}

		}
	}

}
