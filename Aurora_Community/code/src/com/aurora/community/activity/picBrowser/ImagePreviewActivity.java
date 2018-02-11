package com.aurora.community.activity.picBrowser;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.aurora.community.CommunityApp;
import com.aurora.community.R;
import com.aurora.community.activity.BaseActivity;
import com.aurora.community.activity.HackyViewPager;
import com.aurora.community.activity.RotateImageViewAware;
import com.aurora.community.activity.twitter.TwitterNoteActivity;
import com.aurora.community.bean.PhotoInfo;
import com.aurora.community.bean.PhotoSerializable;
import com.aurora.community.utils.DefaultUtil;
import com.aurora.community.utils.ImageLoaderHelper;




public class ImagePreviewActivity extends BaseActivity{

	private ArrayList<PhotoInfo> list = new ArrayList<PhotoInfo>();
	
	private ViewPager mViewPager;
	
	private TextView mTextSelected;
	
	private int  maxItem= 8;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		Bundle extras = getIntent().getExtras(); 
		PhotoSerializable photoSerializable = (PhotoSerializable) extras.getSerializable("list");
		list.clear();
		list.addAll(photoSerializable.getList());

		int current  = extras.getInt("current");
		int left =  extras.getInt("left");
		 if(left!=-1){
			 maxItem = maxItem - left;
		 }
		
		setContentView(R.layout.activity_image_preview);
		setupViews();
		
		
	//	adapter = new ImagePreviewAdapter(list, this);
		mViewPager.setAdapter(new SamplePagerAdapter(list, this,maxItem));
		mViewPager.setCurrentItem(current);
		
		setTitleText(String.valueOf(current+1)+"/"+String.valueOf(list.size()));
		
		//mViewPager.setCurrentItem(current);
	
   
	}

	private static final int ACTION_ITEM_DONE =0x1454;
	
	@Override
	public void setupAuroraActionBar() {
		// TODO Auto-generated method stub
		super.setupAuroraActionBar();
		addActionBarItem(getString(R.string.image_picker_actionbar_tv_done), ACTION_ITEM_DONE);
	}
	
	@Override
	protected void onActionBarItemClick(View view, int itemId) {
		super.onActionBarItemClick(view, itemId);
		
        switch (itemId) {
		case BACK_ITEM_ID:
			onBackItemClick();
			break;
		case ACTION_ITEM_DONE:
			onSelectedDone();
			break;
		default:
			break;
		}
		
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK){
			Bundle d = new Bundle();
			Intent  intent = new Intent(ImagePreviewActivity.this,ImagePickerActvity.class); 
			intent.putExtra("key", "imagePreview");
			PhotoSerializable photoSerializable = new PhotoSerializable();
			photoSerializable.setList(list);
			d.putSerializable("list", photoSerializable);
			intent.putExtras(d);
			startActivity(intent);
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	public void setupViews() {
		// TODO Auto-generated method stub

		mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
	
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				setTitleText(String.valueOf(arg0+1)+"/"+String.valueOf(list.size()));
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
	}

	
	static class SamplePagerAdapter extends PagerAdapter {

		private List<PhotoInfo> list;

		private LayoutInflater inflater;
		
		private Context context;
		
		private List<PhotoInfo> sPhotoList = new ArrayList<PhotoInfo>();
		
		private int maxItem;
		
		public SamplePagerAdapter(List<PhotoInfo> list,Context c,int max){
			this.list = list;
			context = c;
			inflater = LayoutInflater.from(context);
			maxItem = max;
		}
		
		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			final PhotoInfo photoInfo = list.get(position);
			
			View child= inflater.inflate(R.layout.item_img_preview, null);
			PhotoView photoView  = (PhotoView)child.findViewById(R.id.photo_img);
			CheckBox check = (CheckBox)child.findViewById(R.id.photo_choose);
			ImageLoaderHelper.disPlay(photoInfo.getPath_file(), 
					new RotateImageViewAware(photoView,photoInfo.getPath_absolute()), DefaultUtil.getDefaultImageDrawable(context));
	
			container.addView(child, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			check.setChecked(photoInfo.isChoose());
	
			check.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
						if(photoInfo.isChoose()){
						photoInfo.setChoose(false);
					}else{
						if(isSelectedMax()==maxItem){
							Toast.makeText(context, "最多选择8张图片！", Toast.LENGTH_SHORT).show();
							((CheckBox)arg0).setChecked(false);
						}else{
							photoInfo.setChoose(true);
						}
					}
				}
			});
			return child;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		
		private int isSelectedMax(){
			int hasSelected = 0;
			
			for(PhotoInfo info : list){
				if(info.isChoose()){
					hasSelected++;
				}
			}
			return hasSelected;
		}
		
	}


	public void onSelectedDone() {
		// TODO Auto-generated method stub
		ArrayList<String> urlList = new ArrayList<String>();
		for(PhotoInfo info : list){
			if(info.isChoose())
				urlList.add(info.getPath_file());
		}
		Bundle d = new Bundle();
		Intent intent = new Intent(this,TwitterNoteActivity.class);
		PhotoSerializable photoSerializable = new PhotoSerializable();
		photoSerializable.setUrlList(urlList);
		d.putSerializable("list", photoSerializable);
		intent.putExtra("preview",false);
		intent.putExtras(d);
		startActivity(intent);
		//Log.e("linp", "##########ImagePickerActvity.class.g="+);
		CommunityApp.getInstance().clearActivityByIndex(ImagePickerActvity.class.getName());
		this.finish();
	}

	public void onBackItemClick() {
		// TODO Auto-generated method stub
		Bundle d = new Bundle();
		Intent  intent = new Intent(ImagePreviewActivity.this,ImagePickerActvity.class); 
		intent.putExtra("key", "imagePreview");
		PhotoSerializable photoSerializable = new PhotoSerializable();
		photoSerializable.setList(list);
		d.putSerializable("list", photoSerializable);
		intent.putExtras(d);
		startActivity(intent);
	}
	

	
}
