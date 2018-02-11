package com.aurora.iunivoice.activity;

import java.util.List;

import uk.co.senab.photoview.PhotoView;

import com.aurora.iunivoice.R;
import com.aurora.iunivoice.activity.picBrowser.HackyViewPager;
import com.aurora.iunivoice.utils.DefaultUtil;
import com.aurora.iunivoice.utils.ImageLoaderHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

public class PageDetailPicBrowserActivity extends BaseActivity {

	private ViewPager mViewPager;
	private String[] urls ;
	private int position;
	private static final int ACTIONBAR_PICBROWSER_BACK=1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		urls = getIntent().getStringArrayExtra("urls");
		position = getIntent().getIntExtra("position", 0);
		setTitleText(position+1+"/"+urls.length);
		//addActionBarItem(getString(R.string.back), ACTIONBAR_PICBROWSER_BACK);
		
		setContentView(R.layout.activity_pager_detail_picbrowser);
		
		
		
		mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
		mViewPager.setOffscreenPageLimit(3);
		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				setTitleText(arg0+1+"/"+urls.length);
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
		mViewPager.setAdapter(new SamplePagerAdapter());
		mViewPager.setCurrentItem(position);
	}

	
	@Override
	public void setupAuroraActionBar() {
	}
	@Override
	public void setupViews() {

	}

	 class SamplePagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return urls==null?0:urls.length;
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			PhotoView photoView = new PhotoView(container.getContext());
			//photoView.setImageResource(sDrawables[position]);
			ImageLoaderHelper.disPlay(urls[position], photoView,  DefaultUtil.getDefaultImageDrawable(PageDetailPicBrowserActivity.this));
			//ImageLoader.getInstance().displayImage(urls.get(position), photoView);
			// Now just add PhotoView to ViewPager and return it
			container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

			return photoView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

	}
	 @Override
	protected void onActionBarItemClick(View view, int itemId) {
		super.onActionBarItemClick(view, itemId);
		
		switch (itemId) {
		case BACK_ITEM_ID:
			
		
		//case ACTIONBAR_PICBROWSER_BACK:
			finish();
			break;
		}
		
	}

}
