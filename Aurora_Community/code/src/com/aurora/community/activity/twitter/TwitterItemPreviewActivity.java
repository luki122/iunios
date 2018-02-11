package com.aurora.community.activity.twitter;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView.ScaleType;

import com.aurora.community.R;
import com.aurora.community.activity.BaseActivity;
import com.aurora.community.activity.HackyViewPager;
import com.aurora.community.activity.RotateImageViewAware;
import com.aurora.community.bean.PhotoSerializable;
import com.aurora.community.utils.DefaultUtil;
import com.aurora.community.utils.ImageLoaderHelper;

public class TwitterItemPreviewActivity extends BaseActivity{

	private ViewPager mViewPager;

	private ArrayList<String> result = new ArrayList<String>();

	private int currentItem;
	
	private SamplePagerAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_twitter_image_preview);
		setupViews();
		Bundle d = getIntent().getExtras();
		PhotoSerializable photoSerializable = (PhotoSerializable) d
				.getSerializable("list");
		result.clear();
		result = photoSerializable.getUrlList();
		int item = d.getInt("item");

		adapter = new SamplePagerAdapter(result,this);
		
		mViewPager.setAdapter(adapter);

		String title = String.valueOf(item + 1) + "/"
				+ String.valueOf(result.size()-1);
		setTitleText(title);

		Log.e("linp","~~~~~~result.size="+result.size());
		
		mViewPager.setCurrentItem(item);

		setCurrentItem(item);

		
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			
			
			@Override
			public void onPageSelected(int arg0) {
		//		Log.e("linp", "~~~~~~~~~onPageSelected="+arg0);
				// TODO Auto-generated method stub
				String title = String.valueOf(arg0 + 1) + "/"+ String.valueOf(result.size()-1);
				setTitleText(title);
				setCurrentItem(arg0);
				
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

	private final int ACTION_BAR_ITEM_DELETE = 0x1414;
	@Override
	public void setupAuroraActionBar() {
		// TODO Auto-generated method stub
		super.setupAuroraActionBar();
		addActionBarItem(R.drawable.aurora_delete_selector, ACTION_BAR_ITEM_DELETE);
	}
	
	@Override
	protected void onActionBarItemClick(View view, int itemId) {
		super.onActionBarItemClick(view, itemId);
		switch (itemId) {
		case BACK_ITEM_ID:
			backItemClick();
			break;
		case ACTION_BAR_ITEM_DELETE:
			delete();
			break;
		default:
			break;
		}
	}
	
	@Override
	public void setupViews() {
		// TODO Auto-generated method stub
		mViewPager = (HackyViewPager) findViewById(R.id.twitter_item_view_pager);
	}

	static class SamplePagerAdapter extends PagerAdapter {

		private List<String> mlist;
        private Context context;
		
		public SamplePagerAdapter(List<String> list,Context context) {
			mlist = list;
			this.context = context;
		}

		@Override
		public int getCount() {
			return mlist.size()-1 ;
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			PhotoView photoView = new PhotoView(container.getContext());
			final String url = mlist.get(position);
			ImageLoaderHelper.disPlay(url, new RotateImageViewAware(photoView,
					url), DefaultUtil.getDefaultImageDrawable(context));
			// Now just add PhotoView to ViewPager and return it
			photoView.setScaleType(ScaleType.CENTER_CROP);
		//	if (!url.contains("drawable://")) {
				container.addView(photoView, LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT);
	//		}

			return photoView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			Log.e("linp", "~~~~~~~~~~~~~~position="+position);
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public int getItemPosition(Object object) {
			// TODO Auto-generated method stub
			return PagerAdapter.POSITION_NONE;
		}
		
	}

	private void delete() {
		// TODO Auto-generated method stub
		if(result.size()==2){
			result.remove(getCurrentItem());
			adapter.notifyDataSetChanged();
			Bundle d = new Bundle();
			Intent intent = new Intent(TwitterItemPreviewActivity.this,TwitterNoteActivity.class);
			PhotoSerializable photoSerializable = new PhotoSerializable();
			photoSerializable.setUrlList(result);
			intent.putExtra("preview",true);
			d.putSerializable("list", photoSerializable);
			intent.putExtras(d);
			startActivity(intent);
			this.finish();
		}else{
			result.remove(getCurrentItem());
			adapter.notifyDataSetChanged();
			String title = String.valueOf(getCurrentItem() + 1) + "/"+ String.valueOf(result.size()-1);
			setTitleText(title);
		}
	
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK ){
			backItemClick();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void backItemClick() {
		// TODO Auto-generated method stub
		Bundle d = new Bundle();
		Intent intent = new Intent(TwitterItemPreviewActivity.this,TwitterNoteActivity.class);
		PhotoSerializable photoSerializable = new PhotoSerializable();
		photoSerializable.setUrlList(result);
		intent.putExtra("preview",true);
		d.putSerializable("list", photoSerializable);
		intent.putExtras(d);
		startActivity(intent);
	}
	
	private void setCurrentItem(int c){
		this.currentItem = c;
	}
	
	private int getCurrentItem(){
		return this.currentItem;
	}


}
