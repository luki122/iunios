package com.aurora.community.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Thumbnails;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.aurora.community.R;
import com.aurora.community.utils.DefaultUtil;
import com.aurora.community.utils.DensityUtil;
import com.aurora.community.utils.ImageLoaderHelper;
import com.aurora.community.utils.ToastUtil;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class PostDetailImagePreviewActivity extends BaseActivity {

	private ViewPager mViewPager;
	private ArrayList<String> imageUrls = new ArrayList<String>();
	
	private LinearLayout ll_icon_container;
	
	private int currentPostion = 0;
	
	public static final String CURRENT_POSITION_KEY = "current_postion",
			                      IMAGE_URLS_KEY = "image_url";;
	
	private int[] selectIcon = {R.drawable.cityindexpoint,R.drawable.cityindexpoint_highlight};               
			                      
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		enableActionBar(false);
		enableActionBarDivider(false);
		setContentView(R.layout.activity_postdetail_image_preview);
		setupData();
		setupViews();
		setupMenu();
		setupLittleIcon();
	}
	
	private static final int MENU_CANCEL_ID = 0X41254,
            MENU_SAVE_ID = 0X45211;

	private void setupMenu() {
		addMenu(getString(R.string.image_save));
		addMenu(getString(R.string.cancel));
	}

	private void setupLittleIcon(){
		if(imageUrls.size() < 2)
		{
			return;
		}
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.leftMargin = DensityUtil.dip2px(this, 6);
		for(int i = 0;i<imageUrls.size();i++)
		{
			ImageView imageView = new ImageView(this);
			if(i == currentPostion)
			{
				imageView.setImageResource(selectIcon[1]);
			}else{
				imageView.setImageResource(selectIcon[0]);
			}
			
			if(i != 0)
			{
				imageView.setLayoutParams(lp);
			}
			ll_icon_container.addView(imageView);
		}
	}
	
	private void changeSelctIcon(){
		int childCount = ll_icon_container.getChildCount();
		for(int i = 0;i<childCount;++i)
		{
			ImageView iv = (ImageView) ll_icon_container.getChildAt(i);
			if(i == currentPostion)
			{
				iv.setImageResource(selectIcon[1]);
			}else{
				iv.setImageResource(selectIcon[0]);
			}
		}
	}
	
	@Override
	public void onMenuClick(int position, String menuText) {
		// TODO Auto-generated method stub
		super.onMenuClick(position, menuText);
		
		if(position == 0)
		{
			save();
		}else{
			hideMenu();
		}
	}
	
	@Override
	public void setupViews() {
		// TODO Auto-generated method stub
		mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
		mViewPager.setAdapter(new SamplePagerAdapter(imageUrls, this));
		mViewPager.setCurrentItem(currentPostion);
		ll_icon_container = (LinearLayout) findViewById(R.id.ll_icon_container);
		mViewPager.setOnPageChangeListener(onPageChangeListener);
	}

	private OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {
		
		@Override
		public void onPageSelected(int arg0) {
			// TODO Auto-generated method stub
			currentPostion = arg0;
			changeSelctIcon();
		}
		
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private String saveImageUrl;
	public void setSaveImageUrl(String saveImageUrl){
		this.saveImageUrl = saveImageUrl;
	}
	
	private static final String IMAGE_TEMP = "/sdcard/Miss_Puff";//图片保存的路径
	
	private String saveBitmapToLocal(Bitmap bitmap){
		String fileName = System.currentTimeMillis()+".jpg";
		File dir = new File(IMAGE_TEMP);
		if(!dir.exists())
		{
			dir.mkdirs();
		}
		File file = new File(dir,fileName);
		try {
			FileOutputStream fos = new FileOutputStream(file);
			bitmap.compress(CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();
		if(bitmap != null && !bitmap.isRecycled())
			{
				bitmap.recycle();
				bitmap = null;
			}
			return file.getAbsolutePath();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
	
	private void save(){
		ImageLoaderHelper.loadImage(saveImageUrl, new ImageLoadingListener() {
			
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onLoadingFailed(String imageUri, View view,
					FailReason failReason) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				Bitmap bitmap = Bitmap.createBitmap(loadedImage.getWidth(), loadedImage.getHeight(), Config.RGB_565);
				Canvas canvas = new Canvas(bitmap);
				canvas.drawBitmap(loadedImage, 0, 0, new Paint());
				// TODO Auto-generated method stub
				String ret = saveBitmapToLocal(bitmap);
				if (ret != null) {
					ToastUtil.longToast(getString(R.string.save_success,ret));
					Intent flush = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
					Uri uri = Uri.fromFile(new File(ret));
					flush.setData(uri);
					sendBroadcast(flush);
				} else {
					Toast.makeText(PostDetailImagePreviewActivity.this,
							R.string.save_fail, 200).show();
				}
			}
			
			@Override
			public void onLoadingCancelled(String imageUri, View view) {
				// TODO Auto-generated method stub
			}
		});
	}
	
	private void setupData(){
		currentPostion = getIntent().getIntExtra(CURRENT_POSITION_KEY, 0);
		imageUrls.addAll(getIntent().getStringArrayListExtra(IMAGE_URLS_KEY));
	}
	
	static class SamplePagerAdapter extends PagerAdapter {

		private ArrayList<String> imageUrls;

		private LayoutInflater inflater;
		
		private PostDetailImagePreviewActivity activity;
		
		
		public SamplePagerAdapter(ArrayList<String> imageUrls,PostDetailImagePreviewActivity c){
			this.imageUrls =imageUrls;
			activity = c;
			inflater = LayoutInflater.from(activity);
		}
		
		@Override
		public int getCount() {
			return imageUrls.size();
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			View child= inflater.inflate(R.layout.item_img_preview, null);
			PhotoView photoView  = (PhotoView)child.findViewById(R.id.photo_img);
			photoView.setTag(position);
			photoView.setOnLongClickListener(onLongClickListener);
			photoView.setOnPhotoTapListener(onPhotoTapListener);
			CheckBox check = (CheckBox)child.findViewById(R.id.photo_choose);
			check.setVisibility(View.GONE);
			ImageLoaderHelper.disPlay(imageUrls.get(position), photoView, DefaultUtil.getDefaultImageDrawable(activity));
			container.addView(child, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			return child;
		}

		private OnLongClickListener onLongClickListener = new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View arg0) {
				int postion = (Integer) arg0.getTag();
				activity.setSaveImageUrl(imageUrls.get(postion));
				activity.showMenu();
				return false;
			}
		};
		
		private OnPhotoTapListener onPhotoTapListener = new OnPhotoTapListener() {
			
			@Override
			public void onPhotoTap(View view, float x, float y) {
				// TODO Auto-generated method stub
				((PostDetailImagePreviewActivity)activity).finish();
			}
		};
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

	}
	
}
