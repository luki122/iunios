package com.aurora.iunivoice.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.aurora.datauiapi.data.bean.HomepageSlideInfo;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.activity.PageDetailActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * @ClassName: FrameBannerView
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author billy
 * @date 2014-5-23 下午5:17:43
 * 
 */
public class FrameBannerView extends FrameLayout {

	private static final String TAG = "FrameBannerView";
	private static final int HANDLE_CHANGE_INDEX = 100;

	private static final int TURN_TIME = 5000;

	public LoopViewPager bannerViewPager;
	private LinearLayout bannerDot;

	private List<HomepageSlideInfo> slideInfoList;
	private List<String> imageUrls;
	private int[] images;
	private List<ImageView> dotList;
	private int index;
	private int size;
	private FrameHandler mHandler;
	
	// 0-图片大于等于3张 1-图片等于2张 2-图片等于1张
	private int pic_dotype = 2;

	private boolean isRunning = false;
	
	private String formhash;

	public FrameBannerView(Context context) {
		super(context);
		initView();
	}

	public FrameBannerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}

	private void initView() {
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View view = inflater.inflate(R.layout.view_framebanner, this);
		bannerViewPager = (LoopViewPager) view
				.findViewById(R.id.bannerViewPager);
		bannerViewPager.removeAllViews();
		bannerViewPager.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				int action = event.getAction();
				switch (action) {
				case MotionEvent.ACTION_DOWN:
					if (isRunning()) {
						stop();
					}
					break;
				case MotionEvent.ACTION_MOVE:
					if (isRunning()) {
						stop();
					}
					break;
				case MotionEvent.ACTION_UP:
					if (!isRunning()) {
						start();
					}
					break;
				case MotionEvent.ACTION_CANCEL:
					if (!isRunning()) {
						start();
					}
					break;
				}
				return false;
			}
		});

		bannerDot = (LinearLayout) view.findViewById(R.id.bannerDot);
	}
	
	/**
	 * @Title: setImages
	 * @Description: 设置显示资源
	 * @throws
	 */
	public void setImages(List<HomepageSlideInfo> slideInfoList) {
		this.slideInfoList = slideInfoList;
		initData();
	}
	
//	/**
//	 * @Title: setImages
//	 * @Description: 设置显示资源
//	 * @throws
//	 */
//	public void setImages(List<String> imageUrls) {
//		this.imageUrls = imageUrls;
//		initData();
//	}
//	
//	/**
//	 * @Title: setImages
//	 * @Description: 设置显示资源
//	 * @throws
//	 */
//	public void setImages(int[] images) {
//		this.images = images;
//		initData();
//	}

	private void initData() {
		if (imageUrls != null && imageUrls.size() > 0) {
			size = imageUrls.size();
			initPagerData(imageUrls.size());
		} else if (images != null && images.length > 0) {
			size = images.length;
			initPagerData(images.length);
		} else if (slideInfoList != null && slideInfoList.size() > 0) {
			size = slideInfoList.size();
			initPagerData(slideInfoList.size());
		}
	}

	private void initPagerData(int size) {
		List<View> advPics = new ArrayList<View>();
		// ImageView imageView = null;
		int dot_size = 0;
		if (size == 2) {
			pic_dotype = 1;
			size = 4;
			dot_size = 2;
		} else if (size == 1) {
			pic_dotype = 2;
			dot_size = 0;
		} else {
			pic_dotype = 0;
			dot_size = size;
		}

		for (int i = 0; i < size; i++) {
			/*
			 * imageView = new ImageView(getContext());
			 * imageView.setScaleType(ScaleType.CENTER_CROP);
			 */
			View view = LayoutInflater.from(getContext()).inflate(
					R.layout.view_framebanner_main, null);

			advPics.add(view);
		}
		bannerViewPager.setAdapter(new BannerAdapter(advPics));
		dotList = new ArrayList<ImageView>();
		bannerDot.removeAllViews();
		ImageView dotIv = null;
		LinearLayout.LayoutParams paramsMargin = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		int marginRight = getResources().getDimensionPixelSize(
				R.dimen.main_homepage_banner_dot_margin_rigth);
		paramsMargin.setMargins(0, 0, marginRight, 0);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 0, 0, 0);
		for (int i = 0; i < dot_size; i++) {
			dotIv = new ImageView(getContext());
			// 默认第一个为开
			if (i == 0) {
				dotIv.setImageResource(R.drawable.banner_dot_on);
			} else {
				dotIv.setImageResource(R.drawable.banner_dot_off);
			}
			if (i == dot_size - 1) {
				dotIv.setLayoutParams(params);
			} else {
				dotIv.setLayoutParams(paramsMargin);
			}
			dotList.add(dotIv);
			bannerDot.addView(dotIv);
		}
		bannerViewPager.setOnPageChangeListener(new BannerPageChangeListener());
		// bannerViewPager.setOffscreenPageLimit(1);
		bannerViewPager.setCurrentItem(0);
		if (pic_dotype != 2)
			start();
	}

	private class BannerAdapter extends PagerAdapter {

		private List<View> views = null;
		private DisplayImageOptions optionsImage;
		private ImageLoader imageLoader = ImageLoader.getInstance();
		private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

		public BannerAdapter(List<View> views) {
			this.views = views;

			optionsImage = new DisplayImageOptions.Builder()
					.showImageOnLoading(R.drawable.banner_loading_default)
					.showImageForEmptyUri(R.drawable.banner_loading_default)
					.showImageOnFail(R.drawable.banner_loading_default)
					.cacheInMemory(true).cacheOnDisk(true).build();
		}

		@Override
		public void destroyItem(View parent, int position, Object obj) {
			// ((ViewPager) parent).removeView(views.get(position %
			// views.size()));
			Log.i(TAG, "destroyItem");
		}

		@Override
		public int getCount() {
			return views.size();// Integer.MAX_VALUE;
		}

		@Override
		public Object instantiateItem(View parent, int position) {
			ImageView iv = (ImageView) views.get(position).findViewById(
					R.id.imageView);
			
			if (slideInfoList != null && slideInfoList.size() > 0) {
				// 开始头像图片异步加载
				if (pic_dotype == 1) {
					if (position == 2)
						position = 0;
					else if (position == 3)
						position = 1;
				}

				imageLoader.displayImage(slideInfoList.get(position).getImage(),
						new ImageViewAware(iv), optionsImage,
						animateFirstListener);
			}

			try {
				((ViewPager) parent).addView(views.get(position));
			} catch (Exception e) {
//				e.printStackTrace();
			}
			iv.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					// TODO Auto-generated method stub

					String title;
					String url;

					if(pic_dotype == 1)
					{
						title = slideInfoList.get(bannerViewPager.getCurrentItem() % 2).getTitle();
						url = slideInfoList.get(bannerViewPager.getCurrentItem() % 2).getUrl();
					}
					else
					{
						title = slideInfoList.get(bannerViewPager.getCurrentItem() % views.size()).getTitle();
						url = slideInfoList.get(bannerViewPager.getCurrentItem() % views.size()).getUrl();
					}
					Intent intent = new Intent(getContext(), PageDetailActivity.class);
					intent.putExtra(PageDetailActivity.POST_URL, url);
					intent.putExtra(PageDetailActivity.PAGE_TITLE_KEY, title);
					intent.putExtra(PageDetailActivity.POST_BANNER, 1);
					intent.putExtra(PageDetailActivity.FORM_HASH_KEY, formhash);
					getContext().startActivity(intent);
				}
			});
			return views.get(position);
		}

		private class AnimateFirstDisplayListener extends
				SimpleImageLoadingListener {

			private List<String> displayedImages = Collections
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

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			return view == obj;
		}

	}

	private class BannerPageChangeListener implements OnPageChangeListener {


		public BannerPageChangeListener() {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageSelected(int position) {
			int mPosition = position % dotList.size();
			for (int i = 0; i < dotList.size(); i++) {
				if (mPosition == i) {
					dotList.get(i).setImageResource(R.drawable.banner_dot_on);
				} else {
					dotList.get(i).setImageResource(R.drawable.banner_dot_off);
				}
			}
			index = position;
		}

	}
	
	class FrameHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_CHANGE_INDEX:
				index++;
				bannerViewPager.setCurrentItem(index);
				sendEmptyMessageDelayed(HANDLE_CHANGE_INDEX, TURN_TIME);
				break;
			}
		}

	}

	/**
	 * @Title: start
	 * @Description: 开始切换动画
	 * @param
	 * @return void
	 * @throws
	 */
	public void start() {
		if (mHandler == null) {
			mHandler = new FrameHandler();
		}
		isRunning = true;
		mHandler.removeMessages(HANDLE_CHANGE_INDEX);
		mHandler.sendEmptyMessageDelayed(HANDLE_CHANGE_INDEX, TURN_TIME);
	}

	/**
	 * @Title: stop
	 * @Description: 停止切换动画
	 * @param
	 * @return void
	 * @throws
	 */
	public void stop() {
		if (mHandler != null) {
			mHandler.removeMessages(HANDLE_CHANGE_INDEX);
			mHandler = null;
		}
		isRunning = false;
	}

	/**
	 * @Title: exit
	 * @Description: 退出
	 * @param
	 * @return void
	 * @throws
	 */
	public void exit() {
		if (mHandler != null) {
			mHandler.removeMessages(HANDLE_CHANGE_INDEX);
			mHandler = null;
		}
	}

	public boolean isRunning() {
		return isRunning;
	}

	public int getPic_dotype() {
		return pic_dotype;
	}

	public String getFormhash() {
		return formhash;
	}

	public void setFormhash(String formhash) {
		this.formhash = formhash;
	}

}
