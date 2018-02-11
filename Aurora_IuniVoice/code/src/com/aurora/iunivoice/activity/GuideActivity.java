package com.aurora.iunivoice.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

import com.aurora.iunivoice.R;
import com.aurora.iunivoice.adapter.FragmentPagerAdapter;
import com.aurora.iunivoice.utils.DensityUtil;
import com.aurora.iunivoice.utils.Log;

public class GuideActivity extends BaseActivity {

	private ViewPager vp_guids;
	private LinearLayout ll_points;
	private View v_redpoint;
	private List<ImageView> guids;
	private GuideAdapter mAadapter;
	private int disPoints;
	private SharedPreferences 	sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		super.onCreate(savedInstanceState);
		enableActionBar(false);
		sp = getSharedPreferences("config", MODE_PRIVATE);
		initView();
		initData();
		initEvent();
	}
	private void initEvent() {
		v_redpoint.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						v_redpoint.getViewTreeObserver()
								.removeGlobalOnLayoutListener(this);
						disPoints = (ll_points.getChildAt(1).getLeft() - ll_points
								.getChildAt(0).getLeft());
					}
				});
//		bt_startExp.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				sp.edit().putBoolean("isFirst", false).commit();
//				sp.edit().putInt("versioncode",SystemUtils.getVersionCode(GuideActivity.this, getPackageName())).commit();
//				Intent main = new Intent(GuideActivity.this, MainActivity.class);
//				startActivity(main);
//				finish();
//			}
//		});
		vp_guids.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
//				if (position == guids.size() - 1) {
//					bt_startExp.setVisibility(View.VISIBLE);
//				} else {
//					bt_startExp.setVisibility(View.GONE);
//				}
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				Log.v("lmjssjj", "======"+position+"======");
				float leftMargin = disPoints * (position + positionOffset);
				RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v_redpoint
						.getLayoutParams();
				layoutParams.leftMargin = Math.round(leftMargin);
				v_redpoint.setLayoutParams(layoutParams);
			}
			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});
	}
	private void initData() {
	
		int[] pics = new int[] { R.drawable.ic_launcher, R.drawable.add_pic_bg_press,
				R.drawable.aurora_spinner_medium_dark };
		guids = new ArrayList<ImageView>();

		for (int i = 0; i < pics.length; i++) {
			ImageView iv_temp = new ImageView(getApplicationContext());
			iv_temp.setBackgroundResource(pics[i]);
			guids.add(iv_temp);
			View v_point = new View(getApplicationContext());
			v_point.setBackgroundResource(R.drawable.slide_select);
			int dip = 8;
			LayoutParams params = new LayoutParams(DensityUtil.dip2px(
					getApplicationContext(), dip), 4);
			if (i != 0)
				params.leftMargin = DensityUtil.dip2px(
						getApplicationContext(), 4);
			v_point.setLayoutParams(params);
			ll_points.addView(v_point);
		}
		mAadapter = new GuideAdapter(getSupportFragmentManager());
		vp_guids.setAdapter(mAadapter);
	}

	private class MyAdapter extends PagerAdapter {
		@Override
		public int getCount() {
			return guids.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View child = guids.get(position);
			container.addView(child);//
			return child;
		}
	}
	private void initView() {
		setContentView(R.layout.activity_guide);
		vp_guids = (ViewPager) findViewById(R.id.vp_guide_pages);
		ll_points = (LinearLayout) findViewById(R.id.ll_guide_points);
		v_redpoint = findViewById(R.id.v_guide_redpoint);
	}
	
	class GuideAdapter extends FragmentPagerAdapter{

		public GuideAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return GuideFragment.newInstance(position);
		}

		@Override
		public int getCount() {
			return 3;
		}
		
	}
	@Override
	public void setupViews() {		
	}
	
}
