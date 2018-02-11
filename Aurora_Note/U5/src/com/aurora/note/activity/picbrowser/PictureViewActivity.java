package com.aurora.note.activity.picbrowser;

import android.content.Intent;
import android.graphics.Bitmap.Config;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aurora.note.R;
import com.aurora.note.activity.fragment.PicViewFragment;
import com.aurora.note.ui.NoteProgressDialog;
import com.aurora.note.ui.PicViewPager;
import com.aurora.note.util.SystemUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * 展示大图
 * @author jason 2014-4-11 
 */
public class PictureViewActivity extends FragmentActivity implements OnClickListener {

    private static final String TAG = "PictureViewActivity";

	private int mPosition = 0;
	private ArrayList<String> mData = new ArrayList<String>();

	private String mUrl,mContent;
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

	//private LinearLayout bottomTools;
	private RelativeLayout topTools;
	// 图片加载工具
	public ImageLoader imageLoader = ImageLoader.getInstance();
	public DisplayImageOptions options ;

	// 控件隐藏状态
	private boolean isVisible = true;

	/**
	 * 展示工具栏控件
	 *author jason
	 * 2014-3-13
	 * return void
	 */
	private void showTools(){
		topTools.startAnimation(AnimationUtils.loadAnimation(
				PictureViewActivity.this, R.anim.pic_view_push_top_in));
		//bottomTools.startAnimation(AnimationUtils.loadAnimation(
				//PictureViewActivity.this, R.anim.pic_view_push_bottom_in));
		topTools.setVisibility(View.VISIBLE);
		//bottomTools.setVisibility(View.VISIBLE);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		com.aurora.utils.SystemUtils.setStatusBarBackgroundTransparent(this);

		setContentView(R.layout.picture_view_activity);
		// 获取屏幕尺寸
		getScreenSize();
		// 初始化控件
		initViews();
		//初始化图片加载类
		initImageLoader();
		// 获取传递的数据
		getData();
		// 注册监听器
		setListener();
	}

	@Override
	protected void onResume() {
		super.onResume();

		com.aurora.utils.SystemUtils.switchStatusBarColorMode(
				com.aurora.utils.SystemUtils.STATUS_BAR_MODE_WHITE, this);
	}

	private void initImageLoader(){
		options = new DisplayImageOptions.Builder()
		.imageScaleType(ImageScaleType.IN_SAMPLE_INT) 
		/*.cacheInMemory(true)*/.bitmapConfig(Config.RGB_565).build();
	}
	
	/**
	 * 设置控件内容
	 * author jason 2014-2-25  
	 * return void
	 */
	private void setViewsContent() {
		if(mPosition == 0)
			currentIndex.setText(String.valueOf(mPosition+1));
		else
			currentIndex.setText(String.valueOf(mPosition));
		totalNum.setText("/" + picSum);

	}

	/**
	 * 注册监听器
	 * author jason 2014-2-25  
	 * return void
	 */
	private void setListener() {
		back.setOnClickListener(this);
		topTools.setOnClickListener(this);
		//download.setOnClickListener(this);
		vp.setOnPageChangeListener(pageChangeListener);
	}

	/**
	 * 为ViewPager添加适配器
	 * author jason 2014-2-25  
	 * return void
	 */
	private void setAdapter() {
		picPagerAdapter = new ImagePagerAdapter(
				getSupportFragmentManager(),mData);
		vp.setAdapter(picPagerAdapter);
		vp.setCurrentItem(mPosition,false);
	}

	/**
	 * 获取传递的数据
	 * author jason 2014-2-25  
	 * return void
	 */
	private void getData() {
		Intent intent = getIntent();
		//dis_mode = intent.getStringExtra("dis_mode");
		//title = intent.getStringExtra("title");
		//currentImageId = intent.getStringExtra("imageId");
		//tid = intent.getStringExtra("tid");
		
		mUrl = intent.getStringExtra("url");
		mContent = intent.getStringExtra("content");
		requestDatas();
	}
	
	/**
	 *author jason
	 * 2014-3-13
	 * return void
	 */
	private void requestDatas(){
	    new DataLoader(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
	}
	
	private static class DataLoader extends AsyncTask<Void, Integer, Void> {
	    private WeakReference<PictureViewActivity> mTarget;
	    private NoteProgressDialog upd;
	    
	    public DataLoader(PictureViewActivity activity) {
	        mTarget = new WeakReference<PictureViewActivity>(activity);
	    }
	    
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            PictureViewActivity activity = mTarget.get();
            if (activity != null) {
                upd = NoteProgressDialog.createDialog(activity);
//              upd.setMessage(getResources().getString(R.string.picview_loading));
                upd.setCanceledOnTouchOutside(false);
                upd.show();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            PictureViewActivity activity = mTarget.get();
            if (activity != null) {
                Log.d(TAG, "Jim, requestDatas, content: " + activity.mContent + ", url: " + activity.mUrl);
                activity.mData = SystemUtils.getImagesExceptPreset(activity.mContent);
                activity.picSum = activity.mData.size();
                activity.mPosition = activity.mData.indexOf(activity.mUrl);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            PictureViewActivity activity = mTarget.get();
            if (activity != null) {
                if (upd.isShowing() && !activity.isFinishing()) {
                    upd.dismiss();
                }
                
                // 设置控件内容
                activity.setViewsContent();
                activity.showTools();
                activity.setAdapter();
            }
        }
	}

	/**
	 * 初始化组件
	 * author jason 
	 * 2014-2-25  
	 * return void
	 */
	private void initViews() {
		back = (ImageButton) findViewById(R.id.pic_view_go_back);
		vp = (PicViewPager) findViewById(R.id.pic_view_viewpager);

		currentIndex = (TextView) findViewById(R.id.pic_view_current_index);
		totalNum = (TextView) findViewById(R.id.pic_view_total_num);

		//bottomTools = (LinearLayout) findViewById(R.id.pic_view_bottom_tools);
		topTools = (RelativeLayout)findViewById(R.id.pic_view_top_tools);
	}

	/**
	 * 获取屏幕尺寸
	 * author jason 2014-2-25  
	 * return void
	 */
	private void getScreenSize() {
		//mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics metrics = new DisplayMetrics();
		//mWindowManager.getDefaultDisplay().getMetrics(metrics);
		screenWidth = metrics.widthPixels;
		screenHeight = metrics.heightPixels;
	}

	/**
	 * 回调方法,用于控制工具栏的隐藏和显示
	 * @author jason 2014-3-3 
	 */
	public void controlToolViews() {
		if (isVisible) {
			topTools.startAnimation(AnimationUtils.loadAnimation(
					PictureViewActivity.this, R.anim.pic_view_push_top_out));
			//bottomTools.startAnimation(AnimationUtils.loadAnimation(
					//PictureViewActivity.this, R.anim.pic_view_push_bottom_out));
			topTools.setVisibility(View.GONE);
			//bottomTools.setVisibility(View.GONE);
			isVisible = false;
		} else {
			topTools.startAnimation(AnimationUtils.loadAnimation(
					PictureViewActivity.this, R.anim.pic_view_push_top_in));
			//bottomTools.startAnimation(AnimationUtils.loadAnimation(
			//		PictureViewActivity.this, R.anim.pic_view_push_bottom_in));
			topTools.setVisibility(View.VISIBLE);
			//bottomTools.setVisibility(View.VISIBLE);
			isVisible = true;
		}
	}

	/**
	 * ViewPager适配器
	 * @author jason 2014-3-3 
	 */
	private class ImagePagerAdapter extends FragmentStatePagerAdapter {
		private ArrayList<String> data;
		public ImagePagerAdapter(FragmentManager fm , ArrayList<String> data) {
			super(fm);
			this.data = data;
		}
		
		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Fragment getItem(int position) {
			String url = null;
			try {
				url = data.get(position);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return PicViewFragment.newInstance(url, PictureViewActivity.this.mPosition == position);
		}
		
		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			super.destroyItem(container, position, object);;
		}
	}
	
	
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.pic_view_go_back:
		case R.id.pic_view_top_tools:
			finish();
			break;

		}

	}

	// 翻页监听
	private OnPageChangeListener pageChangeListener = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int arg0) {

			//lastPosition = Integer.parseInt("1");
			mPosition = arg0+1;
			currentIndex.setText(String.valueOf(arg0+1));
		}
		
		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}
	};

}
