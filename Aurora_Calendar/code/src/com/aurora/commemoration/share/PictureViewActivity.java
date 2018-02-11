package com.aurora.commemoration.share;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.calendar.R;
import com.aurora.calendar.util.Globals;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.ArrayList;

/**
 * 展示大图
 * @author jason 2014-4-11 
 */
public class PictureViewActivity extends FragmentActivity implements OnClickListener {

    private static final String TAG = "PictureViewActivity";

	private int mPosition = 0;
	private ArrayList<String> mData = new ArrayList<String>();

	private String mUrl;
	private int picSum = 0;
	private ImagePagerAdapter picPagerAdapter;

	// 屏幕宽度
	public static int screenWidth;
	// 屏幕高度
	public static int screenHeight;
	private PicViewPager vp;
	private ImageButton back;
	
	private ImageView set_pic;
	private PicViewFragment fragement;


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

	private void initImageLoader(){
		options = new DisplayImageOptions.Builder()
		.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
		/*.cacheInMemory(true)*/.bitmapConfig(Config.RGB_565).build();
	}
	


	/**
	 * 注册监听器
	 * author jason 2014-2-25  
	 * return void
	 */
	private void setListener() {
		back.setOnClickListener(this);
		set_pic.setOnClickListener(this);
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
    private String getRealPathFromURI(Uri contentUri) {  
    	  
        // can post image  
        String [] proj={MediaStore.Images.Media.DATA};  
        Cursor cursor = managedQuery( contentUri,  
                        proj, // Which columns to return  
                        null,       // WHERE clause; which rows to return (all rows)  
                        null,       // WHERE clause selection arguments (none)  
                        null); // Order-by clause (ascending by name)  
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);  
        cursor.moveToFirst();  
  
        return cursor.getString(column_index);  
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
		
		Uri mUri  = intent.getData();
		if(null != mUri)
		{
			if (mUri.getPath().contains("/external/images/media")) {
				mUrl = getRealPathFromURI(mUri);
			} else {
				mUrl = mUri.getPath();
			}
		}
		mData.add(Globals.FILE_PROTOCOL + mUrl);
		//mData = SystemUtils.getImagesExceptPreset(mContent);
		 // 设置控件内容
        
        showTools();
        setAdapter();
		
		//requestDatas();
	}
	
	/**
	 *author jason
	 * 2014-3-13
	 * return void
	 */
	private void requestDatas(){
	   // new DataLoader(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
	}
	
	/*private static class DataLoader extends AsyncTask<Void, Integer, Void> {
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
	}*/

	/**
	 * 初始化组件
	 * author jason 
	 * 2014-2-25  
	 * return void
	 */
	private void initViews() {
		back = (ImageButton) findViewById(R.id.pic_view_go_back);
		set_pic = (ImageView) findViewById(R.id.set_to_commemoration);
		vp = (PicViewPager) findViewById(R.id.pic_view_viewpager);


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
		private ArrayList<PicViewFragment> fragements = new ArrayList<PicViewFragment>();
		public ImagePagerAdapter(FragmentManager fm , ArrayList<String> data) {
			super(fm);
			this.data = data;
			
			for(int i = 0; i < data.size();i++)
			{
				String url = null;
				try {
					url = data.get(i);
				} catch (Exception e) {
					e.printStackTrace();
				}
				fragements.add(PicViewFragment.newInstance(url, PictureViewActivity.this.mPosition == i));
			}
			
			
		}
		
		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Fragment getItem(int position) {
		
			
			return fragements.get(position);
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
		case R.id.set_to_commemoration:
			
			
			new GetPicTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			
			
		
			break;
		}

	}
	 private class GetPicTask extends AsyncTask<Void, Void, String> {
	        private ProgressDialog mPd;


	        @Override
	        protected void onPreExecute() {
	            super.onPreExecute();
	            mPd = ProgressDialog.show(PictureViewActivity.this, getResources().getString(R.string.set_bg_prompt),
	                    getResources().getString(R.string.set_bg_general),
	                    true, false);
	            mPd.show();
	        }

	        @Override
	        protected void onPostExecute(String result) {
	            super.onPostExecute(result);
	            if (mPd.isShowing() && !PictureViewActivity.this.isFinishing()) {
	                mPd.dismiss();
	            }
	        	Intent intent = new Intent();
				intent.putExtra("url", result);
				setResult(RESULT_OK,intent);
				
				finish();
	        }

	        @Override
	        protected String doInBackground(Void... params) {
	        	String url = ((PicViewFragment)picPagerAdapter.getItem(0)).savePic();

	            return url;
	        }
	    }
	// 翻页监听
	private OnPageChangeListener pageChangeListener = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int arg0) {

			//lastPosition = Integer.parseInt("1");
			//mPosition = arg0+1;
			//currentIndex.setText(String.valueOf(arg0+1));
		}
		
		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}
	};

}