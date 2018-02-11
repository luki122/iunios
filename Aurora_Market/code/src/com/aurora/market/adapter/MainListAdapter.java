package com.aurora.market.adapter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.aurora.datauiapi.data.bean.MainListItem;
import com.aurora.datauiapi.data.bean.appListtem;
import com.aurora.market.R;
import com.aurora.market.activity.module.AppListActivity;
import com.aurora.market.activity.module.MarketDetailActivity;
import com.aurora.market.model.DownloadData;
import com.aurora.market.util.SystemUtils;
import com.aurora.market.widget.RoundProgressView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class MainListAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private List<appListtem> applist;
	private List<MainListItem> alist;
	private List<DownloadData> download;
	// private ProgressBtnUtil progressBtnUtil;

	public static final int VALUE_FOUR_EACH_ROW = 0;// 3种不同的布局
	public static final int VALUE_FOUR_TWO_ROW = 1;// 必备专用
	public static final int VALUE_TWO_EACH_ROW = 2;

	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	// 图片加载工具
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions optionsImage, optionsImage1;
	private boolean loadImage = true;
	private Context m_context;

	public MainListAdapter(Context context, List<MainListItem> app_list) {
		inflater = LayoutInflater.from(context);

		// this.applist = app_list;
		// this.download = down_data;
		alist = app_list;
		this.m_context = context;
		optionsImage = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.page_appicon_big)
				.showImageForEmptyUri(R.drawable.page_appicon_big)
				.showImageOnFail(R.drawable.page_appicon_big)
				.displayer(new RoundedBitmapDisplayer(context.getResources().getDimensionPixelOffset(R.dimen.app_icon_displayer))).cacheInMemory(true)
				.cacheOnDisk(true).build();
		optionsImage1 = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.page_appicon_big)
				.showImageForEmptyUri(R.drawable.page_appicon_big)
				.showImageOnFail(R.drawable.page_appicon_big)
				// .displayer(new RoundedBitmapDisplayer(10))
				.cacheInMemory(true).cacheOnDisk(true).build();

		// progressBtnUtil = new ProgressBtnUtil();
	}

	@Override
	public int getCount() {
		int size = (alist != null) ? alist.size() : 0;
		return size;
	}

	@Override
	public MainListItem getItem(int position) {
		return alist.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		MainListItem dld = alist.get(position);
		int type = dld.getType();
		return type;
	}

	@Override
	public int getViewTypeCount() {
		return 4;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = null;

		if (alist == null) {
			return null;
		}
		// appListtem listitem = null;
		//
		// if(applist.size() > 0)
		// listitem= applist.get(position);
		//
		// if (listitem == null) {
		// return null;
		// }
		Holder0 holder0 = null;
		Holder1 holder1 = null;
		Holder2 holder2 = null;

		final MainListItem data = alist.get(position);

		int type = getItemViewType(position);
		Log.e("aaa", "type----" + type);
		if (convertView == null) {

			switch (type) {
			case VALUE_FOUR_EACH_ROW:
				holder0 = new Holder0();
				convertView = inflater.inflate(R.layout.item_four_each_row,
						null);
				holder0.tv_app_type_name = (TextView) convertView
						.findViewById(R.id.tv_app_type_name);
				holder0.more = convertView.findViewById(R.id.more);

				holder0.app1 = convertView.findViewById(R.id.app1);
				holder0.iv_icon1 = (ImageView) convertView
						.findViewById(R.id.iv_icon1);
				holder0.tv_appname1 = (TextView) convertView
						.findViewById(R.id.tv_appname1);
				holder0.tv_download_count1 = (TextView) convertView
						.findViewById(R.id.tv_download_count1);

				holder0.app2 = convertView.findViewById(R.id.app2);
				holder0.iv_icon2 = (ImageView) convertView
						.findViewById(R.id.iv_icon2);
				holder0.tv_appname2 = (TextView) convertView
						.findViewById(R.id.tv_appname2);
				holder0.tv_download_count2 = (TextView) convertView
						.findViewById(R.id.tv_download_count2);

				holder0.app3 = convertView.findViewById(R.id.app3);
				holder0.iv_icon3 = (ImageView) convertView
						.findViewById(R.id.iv_icon3);
				holder0.tv_appname3 = (TextView) convertView
						.findViewById(R.id.tv_appname3);
				holder0.tv_download_count3 = (TextView) convertView
						.findViewById(R.id.tv_download_count3);

				holder0.app4 = convertView.findViewById(R.id.app4);
				holder0.iv_icon4 = (ImageView) convertView
						.findViewById(R.id.iv_icon4);
				holder0.tv_appname4 = (TextView) convertView
						.findViewById(R.id.tv_appname4);
				holder0.tv_download_count4 = (TextView) convertView
						.findViewById(R.id.tv_download_count4);
				convertView.setTag(holder0);
				break;
			case VALUE_FOUR_TWO_ROW:
				holder1 = new Holder1();
				convertView = inflater
						.inflate(R.layout.item_four_two_row, null);

				holder1.tv_app_type_name = (TextView) convertView
						.findViewById(R.id.tv_app_type_name);
				holder1.more = convertView.findViewById(R.id.more);

				holder1.app1 = convertView.findViewById(R.id.app1);
				holder1.iv_icon1 = (ImageView) convertView
						.findViewById(R.id.iv_icon1);
				holder1.tv_appname1 = (TextView) convertView
						.findViewById(R.id.tv_appname1);
				holder1.rb_score1 = (RatingBar) convertView
						.findViewById(R.id.rb_score1);
				holder1.tv_download_count1 = (TextView) convertView
						.findViewById(R.id.tv_download_count1);

				holder1.app2 = convertView.findViewById(R.id.app2);
				holder1.iv_icon2 = (ImageView) convertView
						.findViewById(R.id.iv_icon2);
				holder1.tv_appname2 = (TextView) convertView
						.findViewById(R.id.tv_appname2);
				holder1.rb_score2 = (RatingBar) convertView
						.findViewById(R.id.rb_score2);
				holder1.tv_download_count2 = (TextView) convertView
						.findViewById(R.id.tv_download_count2);

				holder1.app3 = convertView.findViewById(R.id.app3);
				holder1.iv_icon3 = (ImageView) convertView
						.findViewById(R.id.iv_icon3);
				holder1.tv_appname3 = (TextView) convertView
						.findViewById(R.id.tv_appname3);
				holder1.rb_score3 = (RatingBar) convertView
						.findViewById(R.id.rb_score3);
				holder1.tv_download_count3 = (TextView) convertView
						.findViewById(R.id.tv_download_count3);

				holder1.app4 = convertView.findViewById(R.id.app4);
				holder1.iv_icon4 = (ImageView) convertView
						.findViewById(R.id.iv_icon4);
				holder1.tv_appname4 = (TextView) convertView
						.findViewById(R.id.tv_appname4);
				holder1.rb_score4 = (RatingBar) convertView
						.findViewById(R.id.rb_score4);
				holder1.tv_download_count4 = (TextView) convertView
						.findViewById(R.id.tv_download_count4);

				convertView.setTag(holder1);
				break;
			case VALUE_TWO_EACH_ROW:
				holder2 = new Holder2();
				convertView = inflater
						.inflate(R.layout.item_two_each_row, null);
				holder2.iv_icon_banner1 = (ImageView) convertView
						.findViewById(R.id.iv_icon_banner1);
				holder2.iv_icon_banner2 = (ImageView) convertView
						.findViewById(R.id.iv_icon_banner2);
				convertView.setTag(holder2);
				break;
			default:
				break;

			}

		} else {
			switch (type) {
			case VALUE_FOUR_EACH_ROW:
				holder0 = (Holder0) convertView.getTag();
				break;
			case VALUE_FOUR_TWO_ROW:
				holder1 = (Holder1) convertView.getTag();
				break;
			case VALUE_TWO_EACH_ROW:
				holder2 = (Holder2) convertView.getTag();
				break;
			default:
				break;
			}
		}

		switch (type) {
		case VALUE_FOUR_EACH_ROW:
			if (holder0 != null && data != null) {
				if (data.getApps() != null && data.getApps().size() >= 4) {
					holder0.more.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							String title = data.getTypeName();
							int id = data.getId();

							// 跳转专题
							Intent intent = new Intent(m_context,
									AppListActivity.class);
							intent.putExtra(AppListActivity.OPEN_TYPE,
									AppListActivity.TYPE_SPECIAL);
							intent.putExtra(AppListActivity.SPECIAL_ID, id);
							intent.putExtra(AppListActivity.SPECIAL_NAME, title);

							m_context.startActivity(intent);
						}

					});
					holder0.app1.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							Intent intent = new Intent(m_context,
									MarketDetailActivity.class);
							intent.putExtra("downloaddata",
									(getDownloadData(data.getApps().get(0))));
							m_context.startActivity(intent);

						}

					});

					holder0.app2.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							Intent intent = new Intent(m_context,
									MarketDetailActivity.class);
							intent.putExtra("downloaddata",
									(getDownloadData(data.getApps().get(1))));
							m_context.startActivity(intent);

						}

					});
					holder0.app3.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							Intent intent = new Intent(m_context,
									MarketDetailActivity.class);
							intent.putExtra("downloaddata",
									(getDownloadData(data.getApps().get(2))));
							m_context.startActivity(intent);

						}

					});
					holder0.app4.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							Intent intent = new Intent(m_context,
									MarketDetailActivity.class);
							intent.putExtra("downloaddata",
									(getDownloadData(data.getApps().get(3))));
							m_context.startActivity(intent);

						}

					});

					changeViewData(holder0, data);
				}

			}

			break;
		case VALUE_FOUR_TWO_ROW:
			if (holder1 != null && data != null) {
				if (data.getApps() != null && data.getApps().size() >= 4) {
					holder1.more.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							String title = data.getTypeName();
							int id = data.getId();

							// 跳转专题
							Intent intent = new Intent(m_context,
									AppListActivity.class);
							intent.putExtra(AppListActivity.OPEN_TYPE,
									AppListActivity.TYPE_SPECIAL);
							intent.putExtra(AppListActivity.SPECIAL_ID, id);
							intent.putExtra(AppListActivity.SPECIAL_NAME, title);

							m_context.startActivity(intent);
						}

					});
					holder1.app1.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							Intent intent = new Intent(m_context,
									MarketDetailActivity.class);
							intent.putExtra("downloaddata",
									(getDownloadData(data.getApps().get(0))));
							m_context.startActivity(intent);

						}

					});

					holder1.app2.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							Intent intent = new Intent(m_context,
									MarketDetailActivity.class);
							intent.putExtra("downloaddata",
									(getDownloadData(data.getApps().get(1))));
							m_context.startActivity(intent);

						}

					});
					holder1.app3.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							Intent intent = new Intent(m_context,
									MarketDetailActivity.class);
							intent.putExtra("downloaddata",
									(getDownloadData(data.getApps().get(2))));
							m_context.startActivity(intent);

						}

					});
					holder1.app4.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							Intent intent = new Intent(m_context,
									MarketDetailActivity.class);
							intent.putExtra("downloaddata",
									(getDownloadData(data.getApps().get(3))));
							m_context.startActivity(intent);

						}

					});
					changeViewData(holder1, data);
				}
			}
			break;
		case VALUE_TWO_EACH_ROW:
			if (holder2 != null && data != null) {
				final List<String> link, link1;
				if (data.getBanners() != null && data.getBanners().size() >= 2) {
					link = data.getBanners().get(0).getDatas();
					link1 = data.getBanners().get(1).getDatas();
					holder2.iv_icon_banner1
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View arg0) {
									Intent intent = new Intent(m_context,
											AppListActivity.class);
									intent.putExtra(AppListActivity.OPEN_TYPE,
											AppListActivity.TYPE_SPECIAL);
									intent.putExtra(AppListActivity.SPECIAL_ID,
											Integer.parseInt(link.get(0)));
									intent.putExtra(
											AppListActivity.SPECIAL_NAME,
											link.get(1));

									m_context.startActivity(intent);

								}

							});
					holder2.iv_icon_banner2
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View arg0) {
									Intent intent = new Intent(m_context,
											AppListActivity.class);
									intent.putExtra(AppListActivity.OPEN_TYPE,
											AppListActivity.TYPE_SPECIAL);
									intent.putExtra(AppListActivity.SPECIAL_ID,
											Integer.parseInt(link1.get(0)));
									intent.putExtra(
											AppListActivity.SPECIAL_NAME,
											link1.get(1));

									m_context.startActivity(intent);

								}

							});
					changeViewData(holder2, data);
				}

			}
			break;
		default:
			break;
		}
		return convertView;
	}

	private DownloadData getDownloadData(appListtem listitem) {
		DownloadData tmp_data = new DownloadData();
		tmp_data.setApkId(listitem.getId());
		tmp_data.setApkDownloadPath(listitem.getDownloadURL());
		tmp_data.setApkLogoPath(listitem.getIcons().getPx256());
		tmp_data.setApkName(listitem.getTitle());
		tmp_data.setPackageName(listitem.getPackageName());
		tmp_data.setVersionCode(listitem.getVersionCode());
		tmp_data.setVersionName(listitem.getVersionName());
		return tmp_data;
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
		// ProgressBtn progressBtn;
	}

	static final class Holder0 {

		TextView tv_app_type_name;

		public View app1;
		public View app2;
		public View app3;
		public View app4;
		public View more;

		ImageView iv_icon1;
		TextView tv_appname1;
		TextView tv_download_count1;

		ImageView iv_icon2;
		TextView tv_appname2;
		TextView tv_download_count2;

		ImageView iv_icon3;
		TextView tv_appname3;
		TextView tv_download_count3;

		ImageView iv_icon4;
		TextView tv_appname4;
		TextView tv_download_count4;
	}

	static final class Holder1 {

		public View app1;
		public View app2;
		public View app3;
		public View app4;
		public View more;

		TextView tv_app_type_name;

		ImageView iv_icon1;
		TextView tv_appname1;
		RatingBar rb_score1;
		TextView tv_download_count1;

		ImageView iv_icon2;
		TextView tv_appname2;
		RatingBar rb_score2;
		TextView tv_download_count2;

		ImageView iv_icon3;
		TextView tv_appname3;
		RatingBar rb_score3;
		TextView tv_download_count3;

		ImageView iv_icon4;
		TextView tv_appname4;
		RatingBar rb_score4;
		TextView tv_download_count4;
	}

	static final class Holder2 {
		public View more;
		ImageView iv_icon_banner1;
		ImageView iv_icon_banner2;
	}

	private void changeViewData(Holder0 holder0, MainListItem listitem) {

		if (SystemUtils.isLoadingImage(inflater.getContext())) {
			if (loadImage) {
				imageLoader.displayImage(listitem.getApps().get(0).getIcons()
						.getPx256(), holder0.iv_icon1, optionsImage,
						animateFirstListener);
				imageLoader.displayImage(listitem.getApps().get(1).getIcons()
						.getPx256(), holder0.iv_icon2, optionsImage,
						animateFirstListener);
				imageLoader.displayImage(listitem.getApps().get(2).getIcons()
						.getPx256(), holder0.iv_icon3, optionsImage,
						animateFirstListener);
				imageLoader.displayImage(listitem.getApps().get(3).getIcons()
						.getPx256(), holder0.iv_icon4, optionsImage,
						animateFirstListener);
			} else {
				imageLoader.displayImage1(listitem.getApps().get(0).getIcons()
						.getPx256(), new ImageViewAware(holder0.iv_icon1),
						optionsImage, animateFirstListener, null, m_context, 2);
				imageLoader.displayImage1(listitem.getApps().get(1).getIcons()
						.getPx256(), new ImageViewAware(holder0.iv_icon2),
						optionsImage, animateFirstListener, null, m_context, 2);
				imageLoader.displayImage1(listitem.getApps().get(2).getIcons()
						.getPx256(), new ImageViewAware(holder0.iv_icon3),
						optionsImage, animateFirstListener, null, m_context, 2);
				imageLoader.displayImage1(listitem.getApps().get(3).getIcons()
						.getPx256(), new ImageViewAware(holder0.iv_icon4),
						optionsImage, animateFirstListener, null, m_context, 2);
			}
		} else {
			holder0.iv_icon1.setImageResource(R.drawable.page_appicon_big);
		}

		holder0.iv_icon1
				.setTag(listitem.getApps().get(0).getIcons().getPx256());
		holder0.iv_icon2
				.setTag(listitem.getApps().get(1).getIcons().getPx256());
		holder0.iv_icon3
				.setTag(listitem.getApps().get(2).getIcons().getPx256());
		holder0.iv_icon4
				.setTag(listitem.getApps().get(3).getIcons().getPx256());

		holder0.tv_app_type_name.setText(listitem.getTypeName());
		holder0.tv_appname1.setText(listitem.getApps().get(0).getTitle());
		holder0.tv_download_count1.setText(listitem.getApps().get(0)
				.getDownloadCountStr()
				+ inflater.getContext()
						.getString(R.string.app_adapter_down_end));

		holder0.tv_appname2.setText(listitem.getApps().get(1).getTitle());
		holder0.tv_download_count2.setText(listitem.getApps().get(1)
				.getDownloadCountStr()
				+ inflater.getContext()
						.getString(R.string.app_adapter_down_end));

		holder0.tv_appname3.setText(listitem.getApps().get(2).getTitle());
		holder0.tv_download_count3.setText(listitem.getApps().get(2)
				.getDownloadCountStr()
				+ inflater.getContext()
						.getString(R.string.app_adapter_down_end));

		holder0.tv_appname4.setText(listitem.getApps().get(3).getTitle());
		holder0.tv_download_count4.setText(listitem.getApps().get(3)
				.getDownloadCountStr()
				+ inflater.getContext()
						.getString(R.string.app_adapter_down_end));

	}

	private void changeViewData(Holder1 holder1, MainListItem listitem) {

		if (SystemUtils.isLoadingImage(inflater.getContext())) {
			if (loadImage) {
				imageLoader.displayImage(listitem.getApps().get(0).getIcons()
						.getPx256(), holder1.iv_icon1, optionsImage,
						animateFirstListener);
				imageLoader.displayImage(listitem.getApps().get(1).getIcons()
						.getPx256(), holder1.iv_icon2, optionsImage,
						animateFirstListener);
				imageLoader.displayImage(listitem.getApps().get(2).getIcons()
						.getPx256(), holder1.iv_icon3, optionsImage,
						animateFirstListener);
				imageLoader.displayImage(listitem.getApps().get(3).getIcons()
						.getPx256(), holder1.iv_icon4, optionsImage,
						animateFirstListener);
			} else {
				imageLoader.displayImage1(listitem.getApps().get(0).getIcons()
						.getPx256(), new ImageViewAware(holder1.iv_icon1),
						optionsImage, animateFirstListener, null, m_context, 2);
				imageLoader.displayImage1(listitem.getApps().get(1).getIcons()
						.getPx256(), new ImageViewAware(holder1.iv_icon2),
						optionsImage, animateFirstListener, null, m_context, 2);
				imageLoader.displayImage1(listitem.getApps().get(2).getIcons()
						.getPx256(), new ImageViewAware(holder1.iv_icon3),
						optionsImage, animateFirstListener, null, m_context, 2);
				imageLoader.displayImage1(listitem.getApps().get(3).getIcons()
						.getPx256(), new ImageViewAware(holder1.iv_icon4),
						optionsImage, animateFirstListener, null, m_context, 2);

			}
		} else {
			holder1.iv_icon1.setImageResource(R.drawable.page_appicon_big);
			holder1.iv_icon2.setImageResource(R.drawable.page_appicon_big);
			holder1.iv_icon3.setImageResource(R.drawable.page_appicon_big);
			holder1.iv_icon4.setImageResource(R.drawable.page_appicon_big);
		}

		holder1.iv_icon1
				.setTag(listitem.getApps().get(0).getIcons().getPx256());
		holder1.iv_icon2
				.setTag(listitem.getApps().get(1).getIcons().getPx256());
		holder1.iv_icon3
				.setTag(listitem.getApps().get(2).getIcons().getPx256());
		holder1.iv_icon4
				.setTag(listitem.getApps().get(3).getIcons().getPx256());

		holder1.tv_app_type_name.setText(listitem.getTypeName());
		holder1.tv_appname1.setText(listitem.getApps().get(0).getTitle());
		holder1.tv_download_count1.setText(listitem.getApps().get(0)
				.getDownloadCountStr()
				+ inflater.getContext()
						.getString(R.string.app_adapter_down_end));

		holder1.rb_score1
				.setRating(listitem.getApps().get(1).getLikesRate() / 20);

		holder1.tv_appname2.setText(listitem.getApps().get(1).getTitle());
		holder1.tv_download_count2.setText(listitem.getApps().get(1)
				.getDownloadCountStr()
				+ inflater.getContext()
						.getString(R.string.app_adapter_down_end));

		holder1.rb_score2
				.setRating(listitem.getApps().get(1).getLikesRate() / 20);
		holder1.tv_appname3.setText(listitem.getApps().get(2).getTitle());
		holder1.tv_download_count3.setText(listitem.getApps().get(2)
				.getDownloadCountStr()
				+ inflater.getContext()
						.getString(R.string.app_adapter_down_end));

		holder1.rb_score3
				.setRating(listitem.getApps().get(2).getLikesRate() / 20);
		holder1.tv_appname4.setText(listitem.getApps().get(3).getTitle());
		holder1.tv_download_count4.setText(listitem.getApps().get(3)
				.getDownloadCountStr()
				+ inflater.getContext()
						.getString(R.string.app_adapter_down_end));

		holder1.rb_score4
				.setRating(listitem.getApps().get(3).getLikesRate() / 20);

	}

	private void changeViewData(Holder2 holder2, MainListItem listitem) {
		if (SystemUtils.isLoadingImage(inflater.getContext())) {
			if (loadImage) {
				imageLoader.displayImage(listitem.getBanners().get(0)
						.getPicURL(), holder2.iv_icon_banner1, optionsImage1,
						animateFirstListener);
				imageLoader.displayImage(listitem.getBanners().get(1)
						.getPicURL(), holder2.iv_icon_banner2, optionsImage1,
						animateFirstListener);

			} else {
				imageLoader.displayImage1(listitem.getBanners().get(0)
						.getPicURL(), new ImageViewAware(
						holder2.iv_icon_banner1), optionsImage1,
						animateFirstListener, null, m_context, 2);
				imageLoader.displayImage1(listitem.getBanners().get(1)
						.getPicURL(), new ImageViewAware(
						holder2.iv_icon_banner2), optionsImage1,
						animateFirstListener, null, m_context, 2);

			}
		} else {
			holder2.iv_icon_banner1
					.setImageResource(R.drawable.page_appicon_big);
			holder2.iv_icon_banner2
					.setImageResource(R.drawable.page_appicon_big);

		}
		holder2.iv_icon_banner1
				.setTag(listitem.getBanners().get(0).getPicURL());
		holder2.iv_icon_banner2
				.setTag(listitem.getBanners().get(1).getPicURL());

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

			if (position >= alist.size()) {
				continue;
			}
			// DownloadData data = download.get(position);
			// appListtem listitem = applist.get(position);
			MainListItem mainlistitem = alist.get(position);

			View view = listView.getChildAt(i);
			Object holder = view.getTag();

			if (holder == null) {
				continue;
			}
			if (holder instanceof Holder0) {
				if (mainlistitem != null && mainlistitem.getApps().size() >= 4) {
					changeViewData((Holder0) holder, mainlistitem);
				}

			} else if (holder instanceof Holder1) {
				if (mainlistitem != null && mainlistitem.getApps().size() >= 4) {
					changeViewData((Holder1) holder, mainlistitem);
				}
			} else if (holder instanceof Holder2) {
				if (mainlistitem != null && mainlistitem.getBanners().size() >= 2) {
					changeViewData((Holder2) holder, mainlistitem);
				}
			}

		}
	}

	public void setLoadImage(boolean loadImage) {
		this.loadImage = loadImage;
	}

}
