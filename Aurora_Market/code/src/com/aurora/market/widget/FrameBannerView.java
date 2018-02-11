package com.aurora.market.widget;

import java.lang.ref.WeakReference;
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
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.aurora.datauiapi.data.bean.topVideoItem;
import com.aurora.market.R;
import com.aurora.market.activity.module.AppListActivity;
import com.aurora.market.activity.module.MarketDetailActivity;
import com.aurora.market.model.DownloadData;
import com.aurora.market.util.SystemUtils;
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

	private static final int TURN_TIME = 3000;

	public LoopViewPager bannerViewPager;
	private LinearLayout bannerDot;
	// private View alpha;
	// private FrameLayout.LayoutParams vpParams;
	// private RelativeLayout.LayoutParams dotParams;

	// private List<String> imageUrls;
	// private int[] images;
	private List<ImageView> dotList;
	private List<topVideoItem> video_item = new ArrayList<topVideoItem>();
	private int index;
	private int size;
	private FrameHandler mHandler;

	private int bannerViewPagerHeight = 0;
	private int marginBottom = 0;
	// 0-图片大于等于3张 1-图片等于2张 2-图片等于1张
	private int pic_dotype = 2;
	
	private int old_trans_y = 0;
	private int old_index = 0;
	
	private boolean isRunning = false;
	
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
		System.out.println("event: " + event.getAction());
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
		// alpha = view.findViewById(R.id.bannerAlpha);
	}

	/**
	 * @Title: setProgress
	 * @Description: 设置动画进度
	 * @param @param progress
	 * @return void
	 * @throws
	 */
	public void setProgress(float progress) {
		// alpha.setAlpha(progress);

		// if (vpParams == null) {
		// vpParams = new FrameLayout.LayoutParams(
		// FrameLayout.LayoutParams.MATCH_PARENT, getResources()
		// .getDimensionPixelSize(
		// R.dimen.homepgae_bannerview_height));
		// }
		if (bannerViewPagerHeight == 0) {
			bannerViewPagerHeight = bannerViewPager.getHeight();
		}
		int marginTop = (int) (bannerViewPagerHeight * progress) / 2;
		// vpParams.setMargins(0, marginTop, 0, 0);
		// bannerViewPager.setLayoutParams(vpParams);
		bannerViewPager.setTranslationY(marginTop);
		//
		// if (dotParams == null) {
		// dotParams = new RelativeLayout.LayoutParams(
		// RelativeLayout.LayoutParams.WRAP_CONTENT,
		// RelativeLayout.LayoutParams.WRAP_CONTENT);
		// dotParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		// dotParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		// }
		if (marginBottom == 0) {
			marginBottom = getResources().getDimensionPixelSize(
					R.dimen.homepage_banner_dot_margin_bottom);
		}
		// dotParams.setMargins(0, 0, 0, marginBottom - marginTop);
		// bannerDot.setLayoutParams(dotParams);
		bannerDot.setTranslationY(-(marginBottom - marginTop));
	}

	public void setTransY(int transY) {
		
		/*Log.i(TAG, "zhangwei the transY="+transY);
		int trans_y = 0;
		if(transY > 310)
			trans_y = transY -310;
		else
		{
			trans_y = 0;
		}
		int index = bannerViewPager.getCurrentItem() % video_item.size();

		if((old_trans_y == trans_y)&&(old_index==index))
			return;
		
		old_trans_y = trans_y;
		old_index = index;
		Log.i(TAG, "zhangwei the index="+index+" the old_trans_y="+old_trans_y);
		View view = ((BannerAdapter)bannerViewPager.getAdapter()).getViews().get(index);//(View)bannerViewPager.getChildAt(index);
		ImageView iv = (ImageView)view.findViewById(R.id.crop_imageView);
		
		ImageView dis_imageView= (ImageView)view.findViewById(R.id.dis_imageView);
		//ImageView iv = (ImageView) views.get(0).findViewById(R.id.crop_imageView);
		iv.setDrawingCacheEnabled(true);
		Bitmap bit = iv.getDrawingCache();
		if(null == bit)
			return;
		Bitmap output = Bitmap.createBitmap(bit.getWidth(),
				bit.getHeight(), Config.ARGB_8888);
		Canvas can = new Canvas(output);
		Paint pt = new Paint();
		pt.setColor(0xffffffff);
		
		
		
		int right = bit.getWidth();
		int bottom = bit.getHeight();
		
		Rect src = new Rect((int)0, (int)0, (int)right, (int)bit.getHeight());
      Rect dst = new Rect((int)0, (int)0, (int)right, (int)trans_y);
      //can.drawRoundRect(rectF, roundPx, roundPx, paint);
      can.drawRect(dst, pt);
      pt.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		
		can.drawBitmap(bit, src, src, pt);
		
		
		
		
		dis_imageView.setImageBitmap(output);
		iv.setDrawingCacheEnabled(false);*/
	}
	
	
	/**
	 * @Title: setImages
	 * @Description: 设置显示资源
	 * @param @param video_item
	 * @return void
	 * @throws
	 */
	public void setImages(List<topVideoItem> video_item) {

	/*	if (video_item.size() > 2) {
			this.video_item.clear();
			this.video_item.add(video_item.get(0));
			this.video_item.add(video_item.get(1));
		} else {*/
			this.video_item = video_item;
		//}
		initData();
	}

	private void initData() {
		/*
		 * if (imageUrls != null && imageUrls.size() > 0) { size =
		 * imageUrls.size(); initPagerData(imageUrls.size()); } else if (images
		 * != null && images.length > 0) { size = images.length;
		 * initPagerData(images.length); } else
		 */if (video_item != null && video_item.size() > 0) {
			size = video_item.size();
			initPagerData(video_item.size());
		}
	}

	private void initPagerData(int size) {
		List<View> advPics = new ArrayList<View>();
		//ImageView imageView = null;
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
			/*imageView = new ImageView(getContext());
			imageView.setScaleType(ScaleType.CENTER_CROP);*/
			View view = LayoutInflater.from(getContext()).inflate(R.layout.banner_main, null);

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
				R.dimen.homepage_banner_dot_margin_rigth);
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
		bannerViewPager.setOnPageChangeListener(new BannerPageChangeListener(
				advPics));
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

		public List<View> getViews() {
			return views;
		}
		
		@Override
		public int getCount() {
			return views.size();// Integer.MAX_VALUE;
		}

		@Override
		public Object instantiateItem(View parent, int position) {
			ImageView iv = (ImageView) views.get(position).findViewById(R.id.imageView);// (ImageView)
															// views.get(position
															// % views.size());
			ImageView iv_crop = (ImageView) views.get(position).findViewById(R.id.crop_imageView);
			if (video_item != null && video_item.size() > 0) {
				// iv.setImageResource(images[position]);
				// 开始头像图片异步加载
				if (SystemUtils.isLoadingImage(getContext())) {
					if (pic_dotype == 1) {
						if (position == 2)
							position = 0;
						else if (position == 3)
							position = 1;
					}
					/*imageLoader.displayImage1(video_item.get(position)
							.getPicURL(), new ImageViewAware(iv), optionsImage,
							animateFirstListener,null,getContext(),1);*/
					imageLoader.displayImage(video_item.get(position)
							.getPicURL(), new ImageViewAware(iv), optionsImage,
							animateFirstListener);
					/*imageLoader.displayImage1(video_item.get(position)
							.getPicURL(), new ImageViewAware(iv_crop), optionsImage,
							animateFirstListener,null,getContext(),1);*/
				}
				else
				{
					iv.setImageDrawable(getContext().getResources().getDrawable(R.drawable.banner_loading_default));
				}
			}

			try {
				((ViewPager) parent).addView(views.get(position));
				//Log.i(TAG, "zhangwei the addView position=" + position);
			} catch (Exception e) {
			/*	Log.i(TAG, "zhangwei the e=" + e.getMessage());
				((ViewPager) parent).removeView(iv);
				((ViewPager) parent).addView(iv);*/

			}
			iv.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					//Log.i(TAG, "zhangwei the click page bannerViewPager.getCurrentItem()="+bannerViewPager.getCurrentItem());
					String packageName;
					int bannerType;
					List<String> link;
					if(pic_dotype == 1)
					{
						 packageName = video_item.get(
									bannerViewPager.getCurrentItem() % 2)
									.getPackageName();
						 bannerType = video_item.get(
									bannerViewPager.getCurrentItem() % 2)
									.getBannerType();
						 link = video_item.get(
									bannerViewPager.getCurrentItem() % 2)
									.getDatas();
					}
					else
					{
						packageName = video_item.get(
							bannerViewPager.getCurrentItem() % views.size())
							.getPackageName();
						bannerType = video_item.get(
								bannerViewPager.getCurrentItem() % views.size())
								.getBannerType();
						link = video_item.get(
								bannerViewPager.getCurrentItem() % views.size())
								.getDatas();
					}
					if (bannerType == 1) {
						if (!TextUtils.isEmpty(packageName)) {

							Intent intent = new Intent(getContext(),
									MarketDetailActivity.class);

							DownloadData tmp = new DownloadData();
							tmp.setPackageName(packageName);
							intent.putExtra("downloaddata", tmp);

							getContext().startActivity(intent);
						}
					} else if (bannerType == 2 && link != null && link.size() == 2) {
						Intent intent = new Intent(getContext(),
								AppListActivity.class);
						intent.putExtra(AppListActivity.OPEN_TYPE,
								AppListActivity.TYPE_SPECIAL);
						intent.putExtra(AppListActivity.SPECIAL_ID, Integer.parseInt(link.get(0)));
						intent.putExtra(AppListActivity.SPECIAL_NAME, link.get(1));

						getContext().startActivity(intent);
					}

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
					
					//ImageView iv = (ImageView)((View)view.getParent()).findViewById(R.id.crop_imageView);
					//iv.setImageBitmap(blurimage);
					
				/*	imageView.setDrawingCacheEnabled(true);
					ImageView iv = (ImageView)((View)view.getParent()).findViewById(R.id.crop_imageView);
					
					Bitmap bit = imageView.getDrawingCache();
					bit = Blur.fastblur(getContext(), bit, 10);
					iv.setImageBitmap(bit);
					imageView.setDrawingCacheEnabled(false);*/
					//Bitmap bit1 = Bitmap.createBitmap(bit, 0, 0, bit.getWidth(), 200);
				/*	if(bannerViewPager.getCurrentItem() == 1)
					{
						imageView.setDrawingCacheEnabled(true);
						Bitmap bit = imageView.getDrawingCache();
						bitmap = Blur.fastblur(getContext(), bit, 10);
					}*/
			
					
					//iv.setTranslationY(-DensityUtil.dip2px(getContext(), 140) + 100);
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

		private List<View> views = null;

		public BannerPageChangeListener(List<View> views) {
			this.views = views;
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageSelected(int position) {
			if (dotList.size() == 0) {
				return;
			}
			int mPosition = position % dotList.size();
			for (int i = 0; i < dotList.size(); i++) {
				if (mPosition == i) {
					dotList.get(i).setImageResource(R.drawable.banner_dot_on);
				} else {
					dotList.get(i).setImageResource(R.drawable.banner_dot_off);
				}
			}
			index = position;
			//Log.i(TAG, "zhangwei the old_trans_y1="+old_trans_y);
			//setTransY(old_trans_y+310);
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
			mHandler = new FrameHandler(this);
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

	private static class FrameHandler extends Handler {

		private WeakReference<FrameBannerView> frame;

		public FrameHandler(FrameBannerView m_frame) {
			frame = new WeakReference<FrameBannerView>(m_frame);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_CHANGE_INDEX:
				FrameBannerView vm = frame.get();
				vm.index++;
				// index++;
				// if (vm.index >= vm.size) {
				// vm.index = 0;
				// }
				vm.bannerViewPager.setCurrentItem(vm.index);
				sendEmptyMessageDelayed(HANDLE_CHANGE_INDEX, TURN_TIME);
				break;
			}
		}

	}

	public boolean isRunning() {
		return isRunning;
	}

	public int getPic_dotype() {
		return pic_dotype;
	}

}
