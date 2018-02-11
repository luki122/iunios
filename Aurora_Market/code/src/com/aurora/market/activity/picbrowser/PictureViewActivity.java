package com.aurora.market.activity.picbrowser;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aurora.market.R;
import com.aurora.market.activity.fragment.PicViewFragment;
import com.aurora.market.ui.PicViewPager;
import com.aurora.market.ui.UploadProgressDialog;
import com.aurora.market.util.PicBrowseUtils;
import com.aurora.market.util.SystemUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jason 2014-4-11 TODO 展示大图
 */
public class PictureViewActivity extends FragmentActivity implements
		OnClickListener {
	public int position = 0;
    
	private Bitmap mCurBitmap;
	
	private String[] m_shootdata;
	private int picSum = 0;
	private ImagePagerAdapter picPagerAdapter;

	// 屏幕宽度
	public static int screenWidth;
	// 屏幕高度
	public static int screenHeight;
	private PicViewPager vp;
	private ImageButton back;

	private TextView currentIndex;
	private TextView totalNum;

	// private LinearLayout bottomTools;
	private RelativeLayout topTools;
	// 图片加载工具
	public ImageLoader imageLoader = ImageLoader.getInstance();
	public DisplayImageOptions options;

	// 控件隐藏状态
	private boolean isVisible = true;
	// 记录上一个页面的位置，用以判断viewpager的滑动方向
	private int lastPosition;
	// 常量标示
	private static final int UPDATE_UI = 0;
	private static final int SET_CONTENT = 1;
	// 是否正在请求数据
	boolean isRequestData = false;
	// 进度提示
	private UploadProgressDialog upd;
	private LinearLayout bannerDot;
	private FrameLayout.LayoutParams vpParams;
	private RelativeLayout.LayoutParams dotParams;
	private List<ImageView> dotList;
	// 消息处理类
	private Handler picViewHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case UPDATE_UI:
				// 更新界面
				picPagerAdapter.changeData(m_shootdata);
				isRequestData = false;
				break;

			case SET_CONTENT:
				upd.dismiss();
				// 设置控件内容
				setViewsContent();
				//showTools();
				setAdapter();
				isRequestData = false;
				break;
			}
		};
	};

	/**
	 * author jason 2014-3-13 TODO 展示工具栏控件 return void
	 */
	private void showTools() {
		topTools.startAnimation(AnimationUtils.loadAnimation(
				PictureViewActivity.this, R.anim.pic_view_push_top_in));
		// bottomTools.startAnimation(AnimationUtils.loadAnimation(
		// PictureViewActivity.this, R.anim.pic_view_push_bottom_in));
		topTools.setVisibility(View.VISIBLE);
		// bottomTools.setVisibility(View.VISIBLE);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.picture_view);
		Intent intent = getIntent();
		// dis_mode = intent.getStringExtra("dis_mode");
		// title = intent.getStringExtra("title");
		// currentImageId = intent.getStringExtra("imageId");
		// tid = intent.getStringExtra("tid");

		position = intent.getIntExtra("index", 0);
		PicBrowseUtils.setDefaultPicIndex(position);
		/* byte [] bis=intent.getByteArrayExtra("bitmap");  
		 mCurBitmap=BitmapFactory.decodeByteArray(bis, 0, bis.length);*/
	
		
		m_shootdata = intent.getStringArrayExtra("content");
		// 获取屏幕尺寸
		getScreenSize();
		// 初始化控件
		initViews();
		// 初始化图片加载类
		initImageLoader();
		// 获取传递的数据
		getData();

		// 注册监听器
		setListener();
	}

	private void initImageLoader() {
		options = new DisplayImageOptions.Builder()
				.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
				.cacheInMemory(true).cacheOnDisc(true)
				.bitmapConfig(Config.RGB_565).build();

	}

	/**
	 * author jason 2014-2-25 TODO 设置控件内容 return void
	 */
	private void setViewsContent() {
		if (position == 0)
			currentIndex.setText(String.valueOf(position + 1));
		else
			currentIndex.setText(String.valueOf(position));
		totalNum.setText("/" + picSum);

	}

	/**
	 * author jason 2014-2-25 TODO 注册监听器 return void
	 */
	private void setListener() {
		back.setOnClickListener(this);
		// download.setOnClickListener(this);
		vp.setOnPageChangeListener(pageChangeListener);
	}

	/**
	 * author jason 2014-2-25 TODO 为ViewPager添加适配器 return void
	 */
	private void setAdapter() {
		dotList = new ArrayList<ImageView>();
		picPagerAdapter = new ImagePagerAdapter(getSupportFragmentManager(),
				m_shootdata);
		vp.setAdapter(picPagerAdapter);
		vp.setOffscreenPageLimit(6);
		vp.setCurrentItem(position, false);
		
		bannerDot.removeAllViews();
		ImageView dotIv = null;
		LinearLayout.LayoutParams paramsMargin = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		int marginRight = getResources().getDimensionPixelSize(
				R.dimen.homepage_banner_dot_margin_rigth);
		paramsMargin.setMargins(0, 0, marginRight, 10);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 0, 0, 10);
		for (int i = 0; i < m_shootdata.length; i++) {
			dotIv = new ImageView(this);
			// 默认第一个为开
			if (i == position) {
				dotIv.setImageResource(R.drawable.banner_dot_on);
			} else {
				dotIv.setImageResource(R.drawable.banner_dot_off);
			}
			if (i ==  m_shootdata.length - 1) {
				dotIv.setLayoutParams(params);
			} else {
				dotIv.setLayoutParams(paramsMargin);
			}
			dotList.add(dotIv);
			bannerDot.addView(dotIv);
		}
		
	}

	/** 
	* @Title: setProgress
	* @Description: 设置动画进度
	* @param @param progress
	* @return void
	* @throws 
	*/ 
	public void setProgress(float progress) {
		//alpha.setAlpha(progress);

		if (vpParams == null) {
			vpParams = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.MATCH_PARENT, getResources()
							.getDimensionPixelSize(
									R.dimen.homepgae_bannerview_height));
		}
		int marginTop = (int) (vp.getHeight() * progress) / 2;
		vpParams.setMargins(0, marginTop, 0, 0);
		vp.setLayoutParams(vpParams);

		if (dotParams == null) {
			dotParams = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			dotParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
			dotParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		}
		int marginBottom = getResources().getDimensionPixelSize(
				R.dimen.homepage_banner_dot_margin_bottom);
		dotParams.setMargins(0, 0, 0, marginBottom - marginTop);
		bannerDot.setLayoutParams(dotParams);
	}
	
	/**
	 * author jason 2014-2-25 TODO 获取传递的数据 return void
	 */
	private void getData() {
	
		upd.show();
		requestDatas();
	}

	/**
	 * author jason 2014-3-13 TODO return void
	 */
	private void requestDatas() {
		isRequestData = true;

		// private ArrayList<LargeImageObject> data = new
		// ArrayList<LargeImageObject>();
		new Thread() {
			@Override
			public void run() {

				picSum = m_shootdata.length;

				picViewHandler.sendEmptyMessage(SET_CONTENT);

			}
		}.start();

	}

	/**
	 * author jason 2014-2-25 TODO 初始化组件 return void
	 */
	private void initViews() {
		back = (ImageButton) findViewById(R.id.pic_view_go_back);
		vp = (PicViewPager) findViewById(R.id.pic_view_viewpager);
		bannerDot = (LinearLayout)findViewById(R.id.bannerDot);
		currentIndex = (TextView) findViewById(R.id.pic_view_current_index);
		totalNum = (TextView) findViewById(R.id.pic_view_total_num);
		// bottomTools = (LinearLayout)
		// findViewById(R.id.pic_view_bottom_tools);
		topTools = (RelativeLayout) findViewById(R.id.pic_view_top_tools);
		upd = UploadProgressDialog.createDialog(this);
		upd.setMessage(getResources().getString(R.string.picview_loading));
		upd.setCanceledOnTouchOutside(false);
		setProgress(position);
	}

	/**
	 * author jason 2014-2-25 TODO 获取屏幕尺寸 return void
	 */
	private void getScreenSize() {
		// mWindowManager = (WindowManager)
		// getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics metrics = new DisplayMetrics();
		// mWindowManager.getDefaultDisplay().getMetrics(metrics);
		screenWidth = metrics.widthPixels;
		screenHeight = metrics.heightPixels;
	}

	/**
	 * @author jason 2014-3-3 TODO 回调方法,用于控制工具栏的隐藏和显示
	 */
	public void controlToolViews() {
		if (isVisible) {
			topTools.startAnimation(AnimationUtils.loadAnimation(
					PictureViewActivity.this, R.anim.pic_view_push_top_out));
			// bottomTools.startAnimation(AnimationUtils.loadAnimation(
			// PictureViewActivity.this, R.anim.pic_view_push_bottom_out));
			topTools.setVisibility(View.GONE);
			// bottomTools.setVisibility(View.GONE);
			isVisible = false;
		} else {
			topTools.startAnimation(AnimationUtils.loadAnimation(
					PictureViewActivity.this, R.anim.pic_view_push_top_in));
			// bottomTools.startAnimation(AnimationUtils.loadAnimation(
			// PictureViewActivity.this, R.anim.pic_view_push_bottom_in));
			topTools.setVisibility(View.VISIBLE);
			// bottomTools.setVisibility(View.VISIBLE);
			isVisible = true;
		}
	}

	/**
	 * @author jason 2014-3-3 TODO ViewPager适配器
	 */
	private class ImagePagerAdapter extends FragmentStatePagerAdapter {
		private String[] data;

		public ImagePagerAdapter(FragmentManager fm, String[] data) {
			super(fm);
			this.data = data;
		}

		public void changeData(String[] data) {
			this.data = data;
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return data.length;
		}

		@Override
		public Fragment getItem(int position) {
			String url = null;
			try {
				url = data[position];
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		    return PicViewFragment.newInstance(url, position);
			 
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// TODO Auto-generated method stub
			super.destroyItem(container, position, object);
			;
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.pic_view_go_back:
			finish();
			break;

		}

	}

	// 翻页监听
	private OnPageChangeListener pageChangeListener = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int arg0) {

			// lastPosition = Integer.parseInt("1");
			position = arg0;
			currentIndex.setText(String.valueOf(arg0 + 1));
			
			for (int i = 0; i < dotList.size(); i++) {
				if (arg0 == i) {
					dotList.get(i).setImageResource(R.drawable.banner_dot_on);
				} else {
					dotList.get(i).setImageResource(R.drawable.banner_dot_off);
				}
			}
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}
	};

}